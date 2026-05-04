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
 * with a single call to {@link PerlinNoise}.
 * <br>
 * Called PerfyNoise because it acts like {@link PuffyNoise} using Perlin Noise to puff it up instead of a constant 1.
 */
@Beta
public class PerfyNoise implements INoise {
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
    protected transient NoiseWrapper perlin;

    public PerfyNoise() {
        this(3);
    }
    public PerfyNoise(int octaves) {
        this(0xBEEF1E57, octaves, 2f);
    }

    public PerfyNoise(long seed, int octaves) {
        this(seed, octaves, 2f);
    }

    public PerfyNoise(long seed, int octaves, float frequency) {
        perlin = new NoiseWrapper(new PerlinNoise(seed), frequency, NoiseWrapper.FBM, 1);
        setOctaves(octaves);
        setFrequency(frequency);
        this.seed = seed;
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
        perlin.setSeed(seed);
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
        perlin.setFrequency(frequency);
    }

    @Override
    public String getTag() {
        return "PrfN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + octaves + '~' + frequency + '`';
    }

    @Override
    public PerfyNoise copy() {
        return new PerfyNoise(seed, octaves, frequency);
    }

    public PerfyNoise stringDeserialize(String data) {
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

    public static PerfyNoise recreateFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        return new PerfyNoise(seed, octaves, freq);
    }

    @Override
    public float getNoise(float x, float y) {
        final float p = perlin.getNoise(x, y) * 0.06250f * frequency;
        return getNoise(x, y, p, p, p, p, p);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        final float p = perlin.getNoise(x, y, z) * 0.06250f * frequency;
        return getNoise(x, y, z, p, p, p, p);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        final float p = perlin.getNoise(x, y, z, w) * 0.06250f * frequency;
        return getNoise(x, y, z, w, p, p, p);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        final float p = perlin.getNoise(x, y, z, w, u) * 0.06250f * frequency;
        return getNoise(x, y, z, w, u, p, p);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        final float p = perlin.getNoise(x, y, z, w, u, v) * 0.06250f * frequency;
        return getNoise(x, y, z, w, u, v, p);
    }

    @Override
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
        return "PerfyNoise with seed: " + seed + ", octaves:" + octaves + ", frequency: " + frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerfyNoise that = (PerfyNoise) o;

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
        return 7;
    }
}