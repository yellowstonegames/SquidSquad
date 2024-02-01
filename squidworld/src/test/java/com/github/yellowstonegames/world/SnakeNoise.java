/*
 * Copyright (c) 2023-2022-2024 See AUTHORS file.
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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.grid.*;

/**
 * Combines value noise with simplex noise, like {@link HoneyNoise}.
 */
public class SnakeNoise implements INoise {

    public static final float EQ_ADD_2 = 1.0f/1.75f;
    public static final float EQ_ADD_3 = 0.8f/1.75f;
    public static final float EQ_ADD_4 = 0.6f/1.75f;
    public static final float EQ_ADD_5 = 0.4f/1.75f;
    public static final float EQ_ADD_6 = 0.2f/1.75f;

    public static final float EQ_MUL_2 = 1.2535664f;
    public static final float EQ_MUL_3 = 1.2071217f;
    public static final float EQ_MUL_4 = 1.1588172f;
    public static final float EQ_MUL_5 = 1.1084094f;
    public static final float EQ_MUL_6 = 1.0555973f;

    public int seed;
    public SnakeNoise() {
        this(0x9E3779B97F4A7C15L);
    }
    public SnakeNoise(long seed) {
        this.seed = (int)seed;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

    @Override
    public boolean canUseSeed() {
        return true;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = (int)seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public String getTag() {
        return "SnkN";
    }

    @Override
    public float getNoise(float x, float y) {
        float n = ValueNoise.valueNoise(x, y, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^1, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^2, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD_2, EQ_MUL_2);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = ValueNoise.valueNoise(x, y, z, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^1, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^2, n + z) * 0.3f,
                z + LineWobble.bicubicWobble(seed^3, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD_3, EQ_MUL_3);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = SimplexNoise.noise(x, y, z, w, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.29f + n * n);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = SimplexNoise.noise(x, y, z, w, u, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, u, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.29f + n * n);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = SimplexNoise.noise(x, y, z, w, u, v, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, u, v, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.29f + n * n);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public SnakeNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = Base.BASE10.readInt(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static SnakeNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed =   Base.BASE10.readInt(data, 1, data.indexOf('`', 2));

        return new SnakeNoise(seed);
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This just calls a copy constructor.
     *
     * @return a copy of this SnakeNoise
     */
    @Override
    public SnakeNoise copy() {
        return new SnakeNoise(seed);
    }

    @Override
    public String toString() {
        return "SnakeNoise{" +
                "state=" + seed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SnakeNoise that = (SnakeNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }
}
