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

import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.DigitTools;

import static com.github.yellowstonegames.grid.SimplexNoise.noise;
import static com.github.yellowstonegames.grid.ValueNoise.valueNoise;

/**
 * An INoise implementation that combines and accentuates {@link SimplexNoise} and {@link ValueNoise}.
 * This allows its {@link #sharpness} to be adjusted if you want more or less distinct high/low areas.
 */
public class HoneyNoise implements INoise {

    public static final HoneyNoise instance = new HoneyNoise();

    public long seed = 0xD1CEDBEEF0FFAL;

    /**
     * Affects how distinct high and low areas should be from each other.
     * Should be between 0.0f and 1.0f, both exclusive. The default is 0.6f.
     * If this is assigned 1.0 or greater, calls to any noise methods can sometimes
     * result in division by 0 and an infinite result.
     */
    public float sharpness = 0.6f;

    public HoneyNoise() {
    }

    public HoneyNoise(long seed) {
        this.seed = seed;
    }

    /**
     *
     * @param seed any int or long; if you pass a long, make sure different seeds will still be different when cast to int
     * @param sharpness a float between 0.0 and 1.0, both exclusive; higher values result in more distinct high and low areas
     */
    public HoneyNoise(long seed, float sharpness) {
        this.seed = seed;
        this.sharpness = sharpness;
    }

    @Override
    public String getTag() {
        return "HnyN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + BitConversion.floatToReversedIntBits(sharpness) + '`';
    }

    public HoneyNoise stringDeserialize(String data) {
        if (data == null || data.length() < 3)
            return this;
        int idx;
        this.seed = DigitTools.longFromDec(data, 1, idx = data.indexOf('~', 2));
        this.sharpness = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, idx+1, data.indexOf('`', idx+1)));
        return this;
    }

    public static HoneyNoise recreateFromString(String data) {
        if (data == null || data.length() < 3)
            return null;
        int idx;
        long seed = DigitTools.longFromDec(data, 1, idx = data.indexOf('~', 2));
        float sharpness = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, idx+1, data.indexOf('`', idx+1)));
        return new HoneyNoise(seed, sharpness);
    }

    @Override
    public HoneyNoise copy() {
        return new HoneyNoise(seed, sharpness);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HoneyNoise that = (HoneyNoise) o;

        return seed == that.seed && BitConversion.floatToIntBits(sharpness) == BitConversion.floatToIntBits(that.sharpness);
    }

    @Override
    public int hashCode() {
        return BitConversion.floatToIntBits(sharpness) ^ (int) (seed ^ seed >>> 32);
    }

    @Override
    public String toString() {
        return "HoneyNoise{" +
                "seed=" + seed +
                ", sharpness=" + sharpness +
                '}';
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
    public float getNoise(float x, float y) {
        float n = (valueNoise(x, y, (int) seed) + noise(x, y, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = (valueNoise(x, y, z, (int) seed) + noise(x, y, z, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = (valueNoise(x, y, z, w, (int) seed) + noise(x, y, z, w, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = (valueNoise(x, y, z, w, u, (int) seed) + noise(x, y, z, w, u, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = (valueNoise(x, y, z, w, u, v, (int) seed) + noise(x, y, z, w, u, v, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    public float getSharpness() {
        return sharpness;
    }

    public void setSharpness(float sharpness) {
        this.sharpness = sharpness;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        float n = (valueNoise(x, y, (int) seed) + noise(x, y, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        float n = (valueNoise(x, y, z, (int) seed) + noise(x, y, z, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        float n = (valueNoise(x, y, z, w, (int) seed) + noise(x, y, z, w, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        float n = (valueNoise(x, y, z, w, u, (int) seed) + noise(x, y, z, w, u, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        float n = (valueNoise(x, y, z, w, u, v, (int) seed) + noise(x, y, z, w, u, v, seed)) * 0.5f;
        return n / (sharpness * Math.abs(n) + (1f - sharpness));
    }
}
