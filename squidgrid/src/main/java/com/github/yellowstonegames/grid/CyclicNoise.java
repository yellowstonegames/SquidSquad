/*
 * Copyright (c) 2022 See AUTHORS file.
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

import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.core.DigitTools;
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

    protected int octaves;
    protected float total = 1f, start = 1f, frequency = 2f;
    protected final float lacunarity = 1.6f;
    protected final float gain = 0.625f;
    protected long seed;
    protected transient float[][][] rotations = new float[5][4][];
    protected transient float[][] inputs = new float[][]{new float[2], new float[3], new float[4], new float[5], new float[6]};
    protected transient float[][] outputs = new float[][]{new float[2], new float[3], new float[4], new float[5], new float[6]};
    public CyclicNoise() {
        this(3);
    }
    public CyclicNoise(int octaves) {
        setOctaves(octaves);
        setSeed(0xBEEF1E57CA77L);
    }

    public CyclicNoise(long seed, int octaves) {
        setOctaves(octaves);
        setSeed(seed);
    }

    public CyclicNoise(long seed, int octaves, float frequency) {
        setOctaves(octaves);
        setSeed(seed, frequency);
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);

        start = gain;
        total = 0f;
        for (int i = 0; i < this.octaves; i++) {
            start /= gain;
            total += start;
        }
        total = 1f / total;
    }

    @Override
    public boolean canUseSeed() {
        return false;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Sets the seed, and in doing so creates 20 new rotation matrices for different dimensions to use. Note that this
     * may be considerably more expensive than a typical setter, because of how much it allocates.
     * @param seed any long
     */
    @Override
    public void setSeed(long seed) {
        setSeed(seed, frequency);
    }
    /**
     * Sets the seed, and in doing so creates 20 new rotation matrices for different dimensions to use. Note that this
     * may be considerably more expensive than a typical setter, because of how much it allocates.
     * @param seed any long
     * @param frequency a multiplier that will apply to all coordinates; higher changes faster, lower changes slower
     */
    public void setSeed(long seed, float frequency) {
        this.seed = seed;
        this.frequency = frequency;
        for (int i = 0; i < 4; i++) {
            rotations[0][i] = RotationTools.randomRotation2D(seed);
            rotations[1][i] = RotationTools.randomRotation3D(seed, rotations[0][i]);
            rotations[2][i] = RotationTools.randomRotation4D(seed, rotations[1][i]);
            rotations[3][i] = RotationTools.randomRotation5D(seed, rotations[2][i]);
            rotations[4][i] = RotationTools.randomRotation6D(seed, rotations[3][i]);
        }
    }

    public float getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency; note that this works by setting the seed with {@link #setSeed(long, float)}, so if you want
     * to change both seed and frequency, use that method instead.
     * @param frequency a multiplier that will apply to all coordinates; higher changes faster, lower changes slower
     */
    public void setFrequency(float frequency) {
        setSeed(seed, frequency);
    }

    public String serializeToString() {
        return "`" + seed + '~' + octaves + '~' + frequency + '`';
    }

    public static CyclicNoise deserializeFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int octaves = DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1));

        return new CyclicNoise(seed, octaves, freq);
    }

    @Override
    public float getNoise(float x, float y) {
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

            int xs = (int) (xx * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (yy * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                            SIN_TABLE[xc] * SIN_TABLE[ys] + SIN_TABLE[yc] * SIN_TABLE[xs]
//                            + LineWobble.wobble(123, x) + LineWobble.wobble(456, y) + LineWobble.wobble(789, z)
                    ) * (0.5f/2f)
            ) * amp;

            x = xx * lacunarity;
            y = yy * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z) {
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
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;

            inputs[1][0] = x + zz;
            inputs[1][1] = y + xx;
            inputs[1][2] = z + yy;
            Arrays.fill(outputs[1], 0f);
            RotationTools.rotate(inputs[1], rotations[1][i & 3], outputs[1]);
            xx = outputs[1][0];
            yy = outputs[1][1];
            zz = outputs[1][2];

            int xs = (int) (xx * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (yy * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (zz * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                    SIN_TABLE[xc] * SIN_TABLE[zs] +
                    SIN_TABLE[yc] * SIN_TABLE[xs] +
                    SIN_TABLE[zc] * SIN_TABLE[ys]
                    ) * (0.5f/3f)
            ) * amp;

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
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

            int xs = (int) (xx * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (yy * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (zz * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;
            int ws = (int) (ww * radToIndex) & TABLE_MASK, wc = ws + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                    + SIN_TABLE[xc] * SIN_TABLE[ws]
                    + SIN_TABLE[yc] * SIN_TABLE[xs]
                    + SIN_TABLE[zc] * SIN_TABLE[ys]
                    + SIN_TABLE[wc] * SIN_TABLE[zs]
                    ) * (0.5f/4f)
            ) * amp;

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;
            w = ww * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
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

            int xs = (int) (xx * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (yy * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (zz * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;
            int ws = (int) (ww * radToIndex) & TABLE_MASK, wc = ws + SIN_TO_COS & TABLE_MASK;
            int us = (int) (uu * radToIndex) & TABLE_MASK, uc = us + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                    + SIN_TABLE[xc] * SIN_TABLE[us]
                    + SIN_TABLE[yc] * SIN_TABLE[xs]
                    + SIN_TABLE[zc] * SIN_TABLE[ys]
                    + SIN_TABLE[wc] * SIN_TABLE[zs]
                    + SIN_TABLE[uc] * SIN_TABLE[ws]
                    ) * (0.5f/5f)
            ) * amp;

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;
            w = ww * lacunarity;
            u = uu * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
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

            int xs = (int) (xx * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (yy * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (zz * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;
            int ws = (int) (ww * radToIndex) & TABLE_MASK, wc = ws + SIN_TO_COS & TABLE_MASK;
            int us = (int) (uu * radToIndex) & TABLE_MASK, uc = us + SIN_TO_COS & TABLE_MASK;
            int vs = (int) (vv * radToIndex) & TABLE_MASK, vc = vs + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                    + SIN_TABLE[xc] * SIN_TABLE[vs]
                    + SIN_TABLE[yc] * SIN_TABLE[xs]
                    + SIN_TABLE[zc] * SIN_TABLE[ys]
                    + SIN_TABLE[wc] * SIN_TABLE[zs]
                    + SIN_TABLE[uc] * SIN_TABLE[ws]
                    + SIN_TABLE[vc] * SIN_TABLE[us]
                    ) * (0.5f/6f)
            ) * amp;

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;
            w = ww * lacunarity;
            u = uu * lacunarity;
            v = vv * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }
    private static final float radToIndex = TABLE_SIZE / PI2;

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