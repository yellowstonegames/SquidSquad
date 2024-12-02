/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.annotations.Beta;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A periodic type of continuous noise that looks good when frequencies are low, and rather bad when frequencies are
 * high. From <a href="https://www.shadertoy.com/view/3tcyD7">this ShaderToy by jeyko</a>, based on
 * <a href="https://www.shadertoy.com/view/wl3czN">this ShaderToy by nimitz</a>. It's hard to tell, but it seems like
 * this might be rather fast, especially when more octaves or a higher dimension are needed.
 */
@Beta
public class CyclicNoise implements INoise {
    /* // Mostly the original GLSL code, with few changes, for comparison and archival purposes.
// From https://www.shadertoy.com/view/3tcyD7 by jeyko, based on https://www.shadertoy.com/view/wl3czN by nimitz
float cyclicNoise(vec3 p){
    float noise = 0.;

    // These are the variables. I renamed them from the original by nimitz
    // So they are more similar to the terms used be other types of noise
    float amp = 0.5;
    const float gain = 0.5;
    const float lacunarity = 2.;
    const int octaves = 2;

    const float warp = 0.3;
    float warpTrk = 1.2 ;
    const float warpTrkGain = 1.5;

    // Step 1: Get a simple arbitrary rotation, defined by the direction.
    vec3 seed = vec3(-1,-2.,0.5);
    mat3 rotMatrix = getOrthogonalBasis(seed);

    for(int i = 0; i < octaves; i++){

        // Step 2: Do some domain warping, Similar to fbm. Optional.

        p += sin(p.zxy*warpTrk)*warp;

        // Step 3: Calculate a noise value.
        // This works in a way vaguely similar to Perlin/Simplex noise,
        // but instead of in a square/triangle lattice, it is done in a sine wave.

        noise += sin(dot(cos(p), sin(p.zxy )))*amp;

        // Step 4: Rotate and scale.

        p *= rotMatrix;
        p *= lacunarity;

        warpTrk *= warpTrkGain;
        amp *= gain;
    }


    #ifdef TURBULENT
    return 1. - abs(noise);
    #else
    return (noise*0.5 + 0.5);
    #endif
}
     */

    protected static final float LACUNARITY = 1.6f;
    protected static final float GAIN = 0.625f;

    protected int octaves;
    protected float total = 1f;
    protected float start = 1f;
    protected float frequency = 2f;
    protected long seed;
    protected transient float[][][] rotations = new float[6][4][];
    protected transient float[][] inputs = new float[][]{new float[2], new float[3], new float[4], new float[5], new float[6], new float[7]};
    protected transient float[][] outputs = new float[][]{new float[2], new float[3], new float[4], new float[5], new float[6], new float[7]};
    protected transient float[] gauss = new float[7], house = new float[49], large = new float[49], temp = new float[49];
    public CyclicNoise() {
        this(3);
    }
    public CyclicNoise(int octaves) {
        this(0xBEEF1E57, octaves, 2f);
    }

    public CyclicNoise(long seed, int octaves) {
        this(seed, octaves, 2f);
    }

    public CyclicNoise(long seed, int octaves, float frequency) {
        setOctaves(octaves);
        for (int i = 0, s = 2; i < 6; i++, s++) {
            for (int j = 0; j < 4; j++) {
                rotations[i][j] = new float[s * s];
            }
        }
        setSeed(seed, frequency);
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);

        start = GAIN;
        total = 0f;
        for (int i = 0; i < this.octaves; i++) {
            start /= GAIN;
            total += start;
        }
        total = 1f / total;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return false;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Sets the seed, and in doing so edits 24 rotation matrices for different dimensions to use. Note that this
     * may be considerably more expensive than a typical setter, because all matrices are set whenever the seed changes.
     * @param seed any long
     */
    @Override
    public void setSeed(long seed) {
        setSeed(seed, frequency);
    }
    /**
     * Sets the seed, and in doing so edits 24 rotation matrices for different dimensions to use. Note that this
     * may be considerably more expensive than a typical setter, because all matrices are set whenever the seed changes.
     * Also sets the frequency; the default is 2.
     * @param seed any long
     * @param frequency a multiplier that will apply to all coordinates; higher changes faster, lower changes slower
     */
    public void setSeed(long seed, float frequency) {
        this.seed = seed;
        this.frequency = frequency;
        for (int i = 0; i < 4; i++) {
            seed = this.seed ^ i;
            RotationTools.fillRandomRotation2D(seed, rotations[0][i]);
            System.arraycopy(RotationTools.rotateStep(seed, rotations[0][i], 3, gauss, house, large, temp), 0, rotations[1][i], 0, 9);
            System.arraycopy(RotationTools.rotateStep(seed, rotations[1][i], 4, gauss, house, large, temp), 0, rotations[2][i], 0, 16);
            System.arraycopy(RotationTools.rotateStep(seed, rotations[2][i], 5, gauss, house, large, temp), 0, rotations[3][i], 0, 25);
            System.arraycopy(RotationTools.rotateStep(seed, rotations[3][i], 6, gauss, house, large, temp), 0, rotations[4][i], 0, 36);
            System.arraycopy(RotationTools.rotateStep(seed, rotations[4][i], 7, gauss, house, large, temp), 0, rotations[5][i], 0, 49);
        }
    }

    public float getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency; the default is 2. Higher frequencies produce output that changes more quickly.
     * @param frequency a multiplier that will apply to all coordinates; higher changes faster, lower changes slower
     */
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getTag() {
        return "CycN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + octaves + '~' + frequency + '`';
    }

    @Override
    public CyclicNoise copy() {
        return new CyclicNoise(seed, octaves, frequency);
    }

    public CyclicNoise stringDeserialize(String data) {
        if(data == null || data.length() < 5)
            return this;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        setSeed(seed, freq);
        setOctaves(octaves);
        return this;
    }

    public static CyclicNoise recreateFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        return new CyclicNoise(seed, octaves, freq);
    }

    @Override
    public float getNoise(float x, float y) {
//        return getNoise(x, y, LineWobble.bicubicWobble(seed, x), LineWobble.bicubicWobble(~seed, y));
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;

        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;

        float xx, yy;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;

            inputs[0][0] = x + yy;
            inputs[0][1] = y + xx;
            Arrays.fill(outputs[0], 0f);
            RotationTools.rotate(inputs[0], rotations[0][i & 3], outputs[0]);
            xx = outputs[0][0];
            yy = outputs[0][1];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);

            noise += TrigTools.sinTurns((
                            COS_TABLE[xs] * SIN_TABLE[ys] + COS_TABLE[ys] * SIN_TABLE[xs]
                    ) * (0.5f/2f)
            ) * amp;

            x = xx * LACUNARITY;
            y = yy * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z) {
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;
        z += LineWobble.bicubicWobble(seed + 2, z) * 0.5f;
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;
        z *= frequency;

        float xx, yy, zz;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sinSmoother((x-2) * warpTrk) * warp;
            yy = TrigTools.sinSmoother((y-2) * warpTrk) * warp;
            zz = TrigTools.sinSmoother((z-2) * warpTrk) * warp;

            inputs[1][0] = x + zz;
            inputs[1][1] = y + xx;
            inputs[1][2] = z + yy;
            Arrays.fill(outputs[1], 0f);
            RotationTools.rotate(inputs[1], rotations[1][i & 3], outputs[1]);
            xx = outputs[1][0];
            yy = outputs[1][1];
            zz = outputs[1][2];

//            float xr = (xx * TrigTools.radToIndex); int xf = MathTools.fastFloor(xr); xr -= xf; int xs = xf & TABLE_MASK;
//            float yr = (yy * TrigTools.radToIndex); int yf = MathTools.fastFloor(yr); yr -= yf; int ys = yf & TABLE_MASK;
//            float zr = (zz * TrigTools.radToIndex); int zf = MathTools.fastFloor(zr); zr -= zf; int zs = zf & TABLE_MASK;

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
//
            noise += TrigTools.sinSmootherTurns((
                            COS_TABLE[xs] * SIN_TABLE[zs] +
                            COS_TABLE[ys] * SIN_TABLE[xs] +
                            COS_TABLE[zs] * SIN_TABLE[ys]
                    ) * (0.5f/3f)
            ) * amp;

//            noise += TrigTools.sinSmootherTurns((
//                            TrigTools.cosSmoother(xx) * TrigTools.sinSmoother(zz) +
//                            TrigTools.cosSmoother(yy) * TrigTools.sinSmoother(xx) +
//                            TrigTools.cosSmoother(zz) * TrigTools.sinSmoother(yy)
//                    ) * (0.5f/3f)
//            ) * amp;

//            noise += TrigTools.sinSmootherTurns((
//                            MathTools.lerp(COS_TABLE[xs], COS_TABLE[xs+1], xr) * MathTools.lerp(SIN_TABLE[zs], SIN_TABLE[zs+1], zr) +
//                            MathTools.lerp(COS_TABLE[ys], COS_TABLE[ys+1], yr) * MathTools.lerp(SIN_TABLE[xs], SIN_TABLE[xs+1], xr) +
//                            MathTools.lerp(COS_TABLE[zs], COS_TABLE[zs+1], zr) * MathTools.lerp(SIN_TABLE[ys], SIN_TABLE[ys+1], yr)
//                    ) * (0.5f/3f)
//            ) * amp;

//            noise += TrigTools.sinSmootherTurns((float) (
//                            Math.cos(xx) * Math.sin(zz) +
//                            Math.cos(yy) * Math.sin(xx) +
//                            Math.cos(zz) * Math.sin(yy)
//                    ) * (0.5f/3f)
//            ) * amp;


            x = xx * LACUNARITY;
            y = yy * LACUNARITY;
            z = zz * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;
        z += LineWobble.bicubicWobble(seed + 2, z) * 0.5f;
        w += LineWobble.bicubicWobble(seed + 3, w) * 0.5f;

        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;

        float xx, yy, zz, ww;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;

            inputs[2][0] = x + ww;
            inputs[2][1] = y + xx;
            inputs[2][2] = z + yy;
            inputs[2][3] = w + zz;
            Arrays.fill(outputs[2], 0f);
            RotationTools.rotate(inputs[2], rotations[2][i & 3], outputs[2]);
            xx = outputs[2][0];
            yy = outputs[2][1];
            zz = outputs[2][2];
            ww = outputs[2][3];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
            int ws = radiansToTableIndex(ww);

            noise += TrigTools.sinTurns((
                    + COS_TABLE[xs] * SIN_TABLE[ws]
                    + COS_TABLE[ys] * SIN_TABLE[xs]
                    + COS_TABLE[zs] * SIN_TABLE[ys]
                    + COS_TABLE[ws] * SIN_TABLE[zs]
                    ) * (0.5f/4f)
            ) * amp;

            x = xx * LACUNARITY;
            y = yy * LACUNARITY;
            z = zz * LACUNARITY;
            w = ww * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;
        z += LineWobble.bicubicWobble(seed + 2, z) * 0.5f;
        w += LineWobble.bicubicWobble(seed + 3, w) * 0.5f;
        u += LineWobble.bicubicWobble(seed + 4, u) * 0.5f;

        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;

        float xx, yy, zz, ww, uu;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;
            uu = TrigTools.sin((u-2) * warpTrk) * warp;

            inputs[3][0] = x + uu;
            inputs[3][1] = y + xx;
            inputs[3][2] = z + yy;
            inputs[3][3] = w + zz;
            inputs[3][4] = u + ww;
            Arrays.fill(outputs[3], 0f);
            RotationTools.rotate(inputs[3], rotations[3][i & 3], outputs[3]);
            xx = outputs[3][0];
            yy = outputs[3][1];
            zz = outputs[3][2];
            ww = outputs[3][3];
            uu = outputs[3][4];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
            int ws = radiansToTableIndex(ww);
            int us = radiansToTableIndex(uu);

            noise += TrigTools.sinTurns((
                    + COS_TABLE[xs] * SIN_TABLE[us]
                    + COS_TABLE[ys] * SIN_TABLE[xs]
                    + COS_TABLE[zs] * SIN_TABLE[ys]
                    + COS_TABLE[ws] * SIN_TABLE[zs]
                    + COS_TABLE[us] * SIN_TABLE[ws]
                    ) * (0.5f/5f)
            ) * amp;

            x = xx * LACUNARITY;
            y = yy * LACUNARITY;
            z = zz * LACUNARITY;
            w = ww * LACUNARITY;
            u = uu * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;
        z += LineWobble.bicubicWobble(seed + 2, z) * 0.5f;
        w += LineWobble.bicubicWobble(seed + 3, w) * 0.5f;
        u += LineWobble.bicubicWobble(seed + 4, u) * 0.5f;
        v += LineWobble.bicubicWobble(seed + 5, v) * 0.5f;

        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;

        float xx, yy, zz, ww, uu, vv;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;
            uu = TrigTools.sin((u-2) * warpTrk) * warp;
            vv = TrigTools.sin((v-2) * warpTrk) * warp;

            inputs[4][0] = x + vv;
            inputs[4][1] = y + xx;
            inputs[4][2] = z + yy;
            inputs[4][3] = w + zz;
            inputs[4][4] = u + ww;
            inputs[4][5] = v + uu;
            Arrays.fill(outputs[4], 0f);
            RotationTools.rotate(inputs[4], rotations[4][i & 3], outputs[4]);
            xx = outputs[4][0];
            yy = outputs[4][1];
            zz = outputs[4][2];
            ww = outputs[4][3];
            uu = outputs[4][4];
            vv = outputs[4][5];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
            int ws = radiansToTableIndex(ww);
            int us = radiansToTableIndex(uu);
            int vs = radiansToTableIndex(vv);

            noise += TrigTools.sinTurns((
                    + COS_TABLE[xs] * SIN_TABLE[vs]
                    + COS_TABLE[ys] * SIN_TABLE[xs]
                    + COS_TABLE[zs] * SIN_TABLE[ys]
                    + COS_TABLE[ws] * SIN_TABLE[zs]
                    + COS_TABLE[us] * SIN_TABLE[ws]
                    + COS_TABLE[vs] * SIN_TABLE[us]
                    ) * (0.5f/6f)
            ) * amp;

            x = xx * LACUNARITY;
            y = yy * LACUNARITY;
            z = zz * LACUNARITY;
            w = ww * LACUNARITY;
            u = uu * LACUNARITY;
            v = vv * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    public float getNoise(float x, float y, float z, float w, float u, float v, float m) {
        x += LineWobble.bicubicWobble(seed    , x) * 0.5f;
        y += LineWobble.bicubicWobble(seed + 1, y) * 0.5f;
        z += LineWobble.bicubicWobble(seed + 2, z) * 0.5f;
        w += LineWobble.bicubicWobble(seed + 3, w) * 0.5f;
        u += LineWobble.bicubicWobble(seed + 4, u) * 0.5f;
        v += LineWobble.bicubicWobble(seed + 5, v) * 0.5f;
        m += LineWobble.bicubicWobble(seed + 6, m) * 0.5f;

        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;
        m *= frequency;

        float xx, yy, zz, ww, uu, vv, mm;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;
            uu = TrigTools.sin((u-2) * warpTrk) * warp;
            vv = TrigTools.sin((v-2) * warpTrk) * warp;
            mm = TrigTools.sin((m-2) * warpTrk) * warp;

            inputs[5][0] = x + mm;
            inputs[5][1] = y + xx;
            inputs[5][2] = z + yy;
            inputs[5][3] = w + zz;
            inputs[5][4] = u + ww;
            inputs[5][5] = v + uu;
            inputs[5][6] = m + vv;
            Arrays.fill(outputs[5], 0f);
            RotationTools.rotate(inputs[5], rotations[5][i & 3], outputs[5]);
            xx = outputs[5][0];
            yy = outputs[5][1];
            zz = outputs[5][2];
            ww = outputs[5][3];
            uu = outputs[5][4];
            vv = outputs[5][5];
            mm = outputs[5][6];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
            int ws = radiansToTableIndex(ww);
            int us = radiansToTableIndex(uu);
            int vs = radiansToTableIndex(vv);
            int ms = radiansToTableIndex(mm);

            noise += TrigTools.sinTurns((
                                    + COS_TABLE[xs] * SIN_TABLE[ms]
                                    + COS_TABLE[ys] * SIN_TABLE[xs]
                                    + COS_TABLE[zs] * SIN_TABLE[ys]
                                    + COS_TABLE[ws] * SIN_TABLE[zs]
                                    + COS_TABLE[us] * SIN_TABLE[ws]
                                    + COS_TABLE[vs] * SIN_TABLE[us]
                                    + COS_TABLE[ms] * SIN_TABLE[vs]
                    ) * (0.5f/7f)
            ) * amp;

            x = xx * LACUNARITY;
            y = yy * LACUNARITY;
            z = zz * LACUNARITY;
            w = ww * LACUNARITY;
            u = uu * LACUNARITY;
            v = vv * LACUNARITY;
            m = mm * LACUNARITY;

            warpTrk *= warpTrkGain;
            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public String toString() {
        return "CyclicNoise with seed: " + seed + ", octaves:" + octaves + ", frequency: " + frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CyclicNoise that = (CyclicNoise) o;

        if (octaves != that.octaves) return false;
        if (Float.compare(that.frequency, frequency) != 0) return false;
        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        int result = octaves;
        result = 31 * result + (frequency != +0.0f ? Float.floatToIntBits(frequency) : 0);
        result = 31 * result + (int) (seed ^ (seed >>> 32));
        return result;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }


}