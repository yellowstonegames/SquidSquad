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
import com.github.tommyettinger.random.LineWobble;

/**
 * Combines value noise with simplex noise, like {@link HoneyNoise}, but has more sinuous squashing and stretching of
 * its lattice because each axis is run through {@link LineWobble#bicubicWobble(int, float)}. This was developed at
 * about the same time as {@link BadgerNoise}, but BadgerNoise focuses more on having flatter areas of mid-range values,
 * and SnakeNoise focuses on removing any predictable patterns for extreme values present in SimplexNoise. SnakeNoise
 * tends to look like SimplexNoise or HoneyNoise with multiple octaves, while BadgerNoise does not.
 */
public class SnakeNoise implements INoise {

    public static final float EQ_ADD = 0.8f/1.75f;

    public static final float EQ_MUL = 1.2071217f;

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
    public boolean hasEfficientSetSeed() {
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
                x + LineWobble.bicubicWobble(seed^0x11111111, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^0x22222222, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = ValueNoise.valueNoise(x, y, z, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^0x11111111, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^0x22222222, n + z) * 0.3f,
                z + LineWobble.bicubicWobble(seed^0x33333333, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = ValueNoise.valueNoise(x, y, z, w, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^0x11111111, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^0x22222222, n + z) * 0.3f,
                z + LineWobble.bicubicWobble(seed^0x33333333, n + w) * 0.3f,
                w + LineWobble.bicubicWobble(seed^0x44444444, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = ValueNoise.valueNoise(x, y, z, w, u, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^0x11111111, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^0x22222222, n + z) * 0.3f,
                z + LineWobble.bicubicWobble(seed^0x33333333, n + w) * 0.3f,
                w + LineWobble.bicubicWobble(seed^0x44444444, n + u) * 0.3f,
                u + LineWobble.bicubicWobble(seed^0x55555555, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = ValueNoise.valueNoise(x, y, z, w, u, v, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(seed^0x11111111, n + y) * 0.3f,
                y + LineWobble.bicubicWobble(seed^0x22222222, n + z) * 0.3f,
                z + LineWobble.bicubicWobble(seed^0x33333333, n + w) * 0.3f,
                w + LineWobble.bicubicWobble(seed^0x44444444, n + u) * 0.3f,
                u + LineWobble.bicubicWobble(seed^0x55555555, n + v) * 0.3f,
                v + LineWobble.bicubicWobble(seed^0x66666666, n + x) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
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
