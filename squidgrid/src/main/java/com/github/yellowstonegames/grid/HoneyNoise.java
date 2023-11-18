/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.yellowstonegames.core.DigitTools;

import static com.github.yellowstonegames.grid.SimplexNoise.noise;
import static com.github.yellowstonegames.grid.ValueNoise.valueNoise;

/**
 * An INoise implementation that combines and accentuates {@link SimplexNoise} and {@link ValueNoise}.
 */
public class HoneyNoise implements INoise {

    public static final HoneyNoise instance = new HoneyNoise();

    public long seed = 0xD1CEDBEEF0FFAL;

    public HoneyNoise() {
    }

    public HoneyNoise(long seed) {
        this.seed = seed;
    }

    @Override
    public String getTag() {
        return "HnyN";
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public HoneyNoise stringDeserialize(String data) {
        if (data == null || data.length() < 3)
            return this;
        this.seed = DigitTools.longFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static HoneyNoise recreateFromString(String data) {
        if (data == null || data.length() < 3)
            return null;
        long seed = DigitTools.longFromDec(data, 1, data.indexOf('`', 2));

        return new HoneyNoise(seed);
    }

    @Override
    public HoneyNoise copy() {
        return new HoneyNoise(seed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HoneyNoise that = (HoneyNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ seed >>> 32);
    }

    @Override
    public String toString() {
        return "ValueNoise{" +
                "seed=" + seed +
                '}';
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 7;
    }

    @Override
    public boolean canUseSeed() {
        return true;
    }

    @Override
    public float getNoise(float x, float y) {
        float n = (valueNoise(x, y, (int) seed) + noise(x, y, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = (valueNoise(x, y, z, (int) seed) + noise(x, y, z, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = (valueNoise(x, y, z, w, (int) seed) + noise(x, y, z, w, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = (valueNoise(x, y, z, w, u, (int) seed) + noise(x, y, z, w, u, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = (valueNoise(x, y, z, w, u, v, (int) seed) + noise(x, y, z, w, u, v, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        float n = (valueNoise(x, y, (int) seed) + noise(x, y, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        float n = (valueNoise(x, y, z, (int) seed) + noise(x, y, z, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        float n = (valueNoise(x, y, z, w, (int) seed) + noise(x, y, z, w, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        float n = (valueNoise(x, y, z, w, u, (int) seed) + noise(x, y, z, w, u, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        float n = (valueNoise(x, y, z, w, u, v, (int) seed) + noise(x, y, z, w, u, v, seed)) * 0.5f;
        return n / (0.4f * Math.abs(n) + 0.6f);
    }
}
