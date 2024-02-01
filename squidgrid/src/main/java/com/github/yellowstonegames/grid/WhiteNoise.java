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

/**
 * Makes 2D through 6D value noise that effectively produces a random float result for every different set of arguments
 * given to a {@link #getNoise} or {@link #getNoiseWithSeed} call.
 */
public class WhiteNoise implements INoise {
    public static final WhiteNoise instance = new WhiteNoise();
    public int seed;
    public WhiteNoise() {
        this(0xBEEFCAFE);
    }

    public WhiteNoise(final int seed) {
        this.seed = seed;
    }

    public WhiteNoise(final long seed) {
        this.seed = (int) seed;
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, 2
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, 6
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true because this generator can be seeded with {@link #setSeed(long)} and retrieved with
     * {@link #getSeed()}.
     *
     * @return true
     */
    @Override
    public boolean canUseSeed() {
        return true;
    }

    /**
     * Sets the seed to the given long, if long seeds are supported, or {@code (int)seed} if only int seeds are
     * supported. If {@link #canUseSeed()} returns true, this must be implemented and must set the seed given a long
     * input. If this generator cannot be seeded, this is permitted to either do nothing or throw an
     * {@link UnsupportedOperationException}. If this operation allocates or is time-intensive, then that performance
     * cost will be passed along to {@link #getNoiseWithSeed}, since that calls this twice unless overridden. In the
     * case where seeding is expensive to perform, setSeed() can still be implemented while {@link #canUseSeed()}
     * returns false. This makes the {@link #getNoiseWithSeed} methods avoid reseeding, and instead move their inputs
     * around in space.
     *
     * @param seed a long or int seed, with no restrictions unless otherwise documented
     */
    @Override
    public void setSeed(long seed) {
        this.seed = (int)seed;
    }

    /**
     * Gets the current seed of the generator, as a long.
     *
     * @return the current seed, as a long
     */
    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the constant String {@code "WhtN"} that identifies this in serialized Strings.
     *
     * @return a short String constant that identifies this INoise type, {@code "WhtN"}
     */
    @Override
    public String getTag() {
        return "WhtN";
    }

    public String stringSerialize() {
        return "`" + seed + "`";
    }

    public WhiteNoise stringDeserialize(String data) {
        seed = (Base.BASE10.readInt(data, 1, data.length() - 1));
        return this;
    }

    public static WhiteNoise recreateFromString(String data) {
        return new WhiteNoise(Base.BASE10.readInt(data, 1, data.length() - 1));
    }

    /**
     * Creates a copy of this WhiteNoise.
     * @return a copy of this WhiteNoise
     */
    @Override
    public WhiteNoise copy() {
        return new WhiteNoise(this.seed);
    }

    @Override
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, final long seed) {
        return IntPointHash.hashAll(
                BitConversion.floatToIntBits(x),
                BitConversion.floatToIntBits(y),
                (int)seed) * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, final long seed) {
        return IntPointHash.hashAll(
                BitConversion.floatToIntBits(x),
                BitConversion.floatToIntBits(y),
                BitConversion.floatToIntBits(z),
                (int)seed) * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, final long seed) {
        return IntPointHash.hashAll(
                BitConversion.floatToIntBits(x),
                BitConversion.floatToIntBits(y),
                BitConversion.floatToIntBits(z),
                BitConversion.floatToIntBits(w),
                (int)seed) * 0x1p-31f;
    }


    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return IntPointHash.hashAll(
                BitConversion.floatToIntBits(x),
                BitConversion.floatToIntBits(y),
                BitConversion.floatToIntBits(z),
                BitConversion.floatToIntBits(w),
                BitConversion.floatToIntBits(u),
                (int)seed) * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return IntPointHash.hashAll(
                BitConversion.floatToIntBits(x),
                BitConversion.floatToIntBits(y),
                BitConversion.floatToIntBits(z),
                BitConversion.floatToIntBits(w),
                BitConversion.floatToIntBits(u),
                BitConversion.floatToIntBits(v),
                (int)seed) * 0x1p-31f;
    }

    @Override
    public String toString() {
        return "WhiteNoise{" +
                "seed=" + seed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhiteNoise that = (WhiteNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }
}
