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
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.annotations.Beta;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A variant on {@link CyclicNoise} that always uses 7D noise internally, filling in any dimensions that it doesn't have
 * with 1s.
 * <br>
 * Called PuffyNoise because it "puffs up" the dimensionality to a higher value even though most other noise generators
 * don't do that very well. CyclicNoise is unusually fast in high dimensions and unusually low-quality in 2D, so maybe
 * using more dimensions will help?
 */
@Beta
public class PuffyNoise implements INoise {
    protected static final float LACUNARITY = 1.6f;
    protected static final float GAIN = 0.625f;

    protected int octaves;
    protected float total = 1f;
    protected float start = 1f;
    protected float frequency = 2f;
    protected long seed;
    protected transient RotationTools.Rotator[] rotations = new RotationTools.Rotator[4];
    protected transient float[] inputs = new float[7];
    protected transient float[] outputs = new float[7];
    public PuffyNoise() {
        this(3);
    }
    public PuffyNoise(int octaves) {
        this(0xBEEF1E57, octaves, 2f);
    }

    public PuffyNoise(long seed, int octaves) {
        this(seed, octaves, 2f);
    }

    public PuffyNoise(long seed, int octaves, float frequency) {
        setOctaves(octaves);
        setFrequency(frequency);
        for (int i = 0; i < 4; i++) {
            rotations[i] = new RotationTools.Rotator(7, new DistinctRandom(seed ^ i));
        }
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
     * Sets the seed, and in doing so edits 4 Rotator objects to use. Note that this
     * may be considerably more expensive than a typical setter, because all matrices are set whenever the seed changes.
     * @param seed any long
     */
    public void setSeed(long seed) {
        this.seed = seed;
        for (int i = 0; i < 4; i++) {
            rotations[i].random.setSeed(seed ^ i);
            rotations[i].randomize();
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
        return "PufN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + octaves + '~' + frequency + '`';
    }

    @Override
    public PuffyNoise copy() {
        return new PuffyNoise(seed, octaves, frequency);
    }

    public PuffyNoise stringDeserialize(String data) {
        if(data == null || data.length() < 5)
            return this;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        setSeed(seed);
        setOctaves(octaves);
        setFrequency(freq);
        return this;
    }

    public static PuffyNoise recreateFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        return new PuffyNoise(seed, octaves, freq);
    }

    @Override
    public float getNoise(float x, float y) {
        return getNoise(x, y, 1f, 1f, 1f, 1f, 1f);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return getNoise(x, y, z, 1f, 1f, 1f, 1f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return getNoise(x, y, z, w, 1f, 1f, 1f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoise(x, y, z, w, u, 1f, 1f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoise(x, y, z, w, u, v, 1f);
    }

    public float getNoise(float x, float y, float z, float w, float u, float v, float m) {
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
        for (int i = 0; i < octaves;) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;
            uu = TrigTools.sin((u-2) * warpTrk) * warp;
            vv = TrigTools.sin((v-2) * warpTrk) * warp;
            mm = TrigTools.sin((m-2) * warpTrk) * warp;

            inputs[0] = x + mm;
            inputs[1] = y + xx;
            inputs[2] = z + yy;
            inputs[3] = w + zz;
            inputs[4] = u + ww;
            inputs[5] = v + uu;
            inputs[6] = m + vv;
            Arrays.fill(outputs, 0f);
            rotations[i & 3].rotate(inputs, outputs);
            xx = outputs[0];
            yy = outputs[1];
            zz = outputs[2];
            ww = outputs[3];
            uu = outputs[4];
            vv = outputs[5];
            mm = outputs[6];

            int xs = radiansToTableIndex(xx);
            int ys = radiansToTableIndex(yy);
            int zs = radiansToTableIndex(zz);
            int ws = radiansToTableIndex(ww);
            int us = radiansToTableIndex(uu);
            int vs = radiansToTableIndex(vv);
            int ms = radiansToTableIndex(mm);

            noise += TrigTools.sinSmootherTurns((
                                    + COS_TABLE[xs] * SIN_TABLE[ms]
                                    + COS_TABLE[ys] * SIN_TABLE[xs]
                                    + COS_TABLE[zs] * SIN_TABLE[ys]
                                    + COS_TABLE[ws] * SIN_TABLE[zs]
                                    + COS_TABLE[us] * SIN_TABLE[ws]
                                    + COS_TABLE[vs] * SIN_TABLE[us]
                                    + COS_TABLE[ms] * SIN_TABLE[vs]
                    ) * (0.5f/7f)
            ) * amp;

            if(++i == octaves) break;

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
        return "PuffyNoise with seed: " + seed + ", octaves:" + octaves + ", frequency: " + frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PuffyNoise that = (PuffyNoise) o;

        if (octaves != that.octaves) return false;
        if (Float.compare(that.frequency, frequency) != 0) return false;
        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        int result = octaves;
        result = 31 * result + (frequency != +0.0f ? BitConversion.floatToIntBits(frequency) : 0);
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