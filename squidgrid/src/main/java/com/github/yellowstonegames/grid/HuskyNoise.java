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

import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.annotations.Beta;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A variant on {@link CyclicNoise} that always uses 7D noise internally, filling in any dimensions that it doesn't have
 * with 1s, and uses {@link LineWobble#bicubicWobble(long, float)} instead of sine/cosine waves.
 * <br>
 * Called HuskyNoise because it's a variant on PuffyNoise, and husky dogs are puffy or fluffy.
 */
@Beta
public class HuskyNoise implements INoise {
    protected static final float LACUNARITY = 1.6f;
    protected static final float GAIN = 0.625f;

    protected int octaves = 1;
    protected float total = 1f;
    protected float start = 1f;
    protected float frequency = 1f;
    protected long seed;
    protected transient RotationTools.Rotator[] rotations = new RotationTools.Rotator[4];
    protected transient float[] inputs = new float[7];
    protected transient float[] outputs = new float[7];
    public HuskyNoise() {
        this(3);
    }
    public HuskyNoise(int octaves) {
        this(0xBEEF1E57, octaves, 1f);
    }

    public HuskyNoise(long seed, int octaves) {
        this(seed, octaves, 1f);
    }

    public HuskyNoise(long seed, int octaves, float frequency) {
        setOctaves(octaves);
        setFrequency(frequency);
        this.seed = seed;
        for (int i = 0; i < 4; i++) {
            rotations[i] = new RotationTools.Rotator(7, new DistinctRandom((i ^ 5L) * 5555555555555555555L));
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
        return true;
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
    }

    public float getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency; the default is 1. Higher frequencies produce output that changes more quickly.
     * @param frequency a multiplier that will apply to all coordinates; higher changes faster, lower changes slower
     */
    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getTag() {
        return "HskN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + octaves + '~' + frequency + '`';
    }

    @Override
    public HuskyNoise copy() {
        return new HuskyNoise(seed, octaves, frequency);
    }

    public HuskyNoise stringDeserialize(String data) {
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

    public static HuskyNoise recreateFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        int octaves = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        float freq  = Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos+1));
        return new HuskyNoise(seed, octaves, freq);
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

        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        u *= frequency;
        v *= frequency;
        m *= frequency;

        inputs[0] = x;
        inputs[1] = y;
        inputs[2] = z;
        inputs[3] = w;
        inputs[4] = u;
        inputs[5] = v;
        inputs[6] = m;

        long cSeed = seed, sSeed = 5555555555555555555L - seed;

        for (int i = 0; i < octaves;) {
            cSeed += 0x9E3779B97F4A7C15L; // 2 to the 64 divided by golden ratio
            sSeed -= cSeed;

            inputs[0] = x;
            inputs[1] = y;
            inputs[2] = z;
            inputs[3] = w;
            inputs[4] = u;
            inputs[5] = v;
            inputs[6] = m;
            Arrays.fill(outputs, 0f);
            rotations[i & 3].rotate(inputs, outputs);
            x = outputs[0];
            y = outputs[1];
            z = outputs[2];
            w = outputs[3];
            u = outputs[4];
            v = outputs[5];
            m = outputs[6];

// repeatedly changing t and incorporating it into the output does a domain warp effect
            float t = 0f;
            t += LineWobble.bicubicWobble(++cSeed, x + t) * LineWobble.bicubicWobble(++sSeed, m - t);
            t += LineWobble.bicubicWobble(++cSeed, y + t) * LineWobble.bicubicWobble(++sSeed, x - t);
            t += LineWobble.bicubicWobble(++cSeed, z + t) * LineWobble.bicubicWobble(++sSeed, y - t);
            t += LineWobble.bicubicWobble(++cSeed, w + t) * LineWobble.bicubicWobble(++sSeed, z - t);
            t += LineWobble.bicubicWobble(++cSeed, u + t) * LineWobble.bicubicWobble(++sSeed, w - t);
            t += LineWobble.bicubicWobble(++cSeed, v + t) * LineWobble.bicubicWobble(++sSeed, u - t);
            t += LineWobble.bicubicWobble(++cSeed, m + t) * LineWobble.bicubicWobble(++sSeed, v - t);

// t is run through a sigmoid function, which limits it to the -1 to 1 range, then multiplied by amp and added to noise
            noise += (t / (float)Math.sqrt(t * t + 0.25f)) * amp;

            if(++i == octaves) break;

            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
            w *= LACUNARITY;
            u *= LACUNARITY;
            v *= LACUNARITY;
            m *= LACUNARITY;

            amp *= GAIN;
        }
        return noise * total;
    }

    @Override
    public String toString() {
        return "HuskyNoise with seed: " + seed + ", octaves:" + octaves + ", frequency: " + frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HuskyNoise that = (HuskyNoise) o;

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