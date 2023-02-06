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

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.core.annotations.Beta;

public interface INoise {
    /**
     * Gets the minimum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 3 for one that is only defined for 3D or higher-dimensional spaces.
     * @return the minimum supported dimension, from 2 to 6 inclusive
     */
    int getMinDimension();

    /**
     * Gets the maximum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 6 for one that is defined up to the highest dimension this interface knows about (6D).
     * @return the maximum supported dimension, from 2 to 6 inclusive
     */
    int getMaxDimension();

    /**
     * Returns true if this generator can be seeded with {@link #setSeed(long)} (and if so, retrieved with
     * {@link #getSeed()}).
     * @return true if {@link #setSeed(long)} and {@link #getSeed()} are supported, false if either isn't supported
     */
    boolean canUseSeed();
    /**
     * Gets 2D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    float getNoise(float x, float y);

    /**
     * Gets 3D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    float getNoise(float x, float y, float z);

    /**
     * Gets 4D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    float getNoise(float x, float y, float z, float w);

    /**
     * Gets 5D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    float getNoise(float x, float y, float z, float w, float u);

    /**
     * Gets 6D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    float getNoise(float x, float y, float z, float w, float u, float v);

    /**
     * Sets the seed to the given long, if long seeds are supported, or {@code (int)seed} if only int seeds are
     * supported. If {@link #canUseSeed()} returns true, this must be implemented and must set the seed given a long
     * input. If this generator cannot be seeded, this is permitted to either do nothing or throw an
     * {@link UnsupportedOperationException}. If this operation allocates or is time-intensive, then that performance
     * cost will be passed along to {@link #getNoiseWithSeed}, since that calls this twice unless overridden. In the
     * case where seeding is expensive to perform, setSeed() can still be implemented while {@link #canUseSeed()}
     * returns false. This makes the {@link #getNoiseWithSeed} methods avoid reseeding, and instead move their inputs
     * around in space.
     * @param seed a long or int seed, with no restrictions unless otherwise documented
     */
    default void setSeed(long seed) {
        throw new UnsupportedOperationException("setSeed() is not supported.");
    }

    /**
     * Gets the current seed of the generator, as a long even if the seed is stored internally as an int.
     * If {@link #canUseSeed()} returns true, this must be implemented, but if canUseSeed() returns false, this is
     * permitted to either still be implemented (but typically only if it is time- or space-intensive to call getSeed())
     * or to throw an {@link UnsupportedOperationException}.
     * @return the current seed, as a long
     */
    default long getSeed() {
        throw new UnsupportedOperationException("getSeed() is not supported.");
    }

    default String getTag() {
        return "(NO)";
    }
    
    default String serializeToString() {
        throw new UnsupportedOperationException("serializeToString() is not supported.");
    }

    default INoise deserializeFromString(String data) {
        throw new UnsupportedOperationException("deserializeFromString() is not supported.");
    }

    /**
     * Gets 2D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, long seed) {
        if(!canUseSeed()) {
            float s = seed * 0x1p-48f;
            return getNoise(x + s, y + s);
        }
        final long s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y);
        setSeed(s);
        return r;
    }

    /**
     * Gets 3D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, long seed) {
        if(!canUseSeed()) {
            float s = seed * 0x1p-48f;
            return getNoise(x + s, y + s, z + s);
        }
        final long s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z);
        setSeed(s);
        return r;
    }

    /**
     * Gets 4D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        if(!canUseSeed()) {
            float s = seed * 0x1p-48f;
            return getNoise(x + s, y + s, z + s, w + s);
        }
        final long s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w);
        setSeed(s);
        return r;
    }

    /**
     * Gets 5D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        if(!canUseSeed()) {
            float s = seed * 0x1p-48f;
            return getNoise(x + s, y + s, z + s, w + s, u + s);
        }
        final long s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w, u);
        setSeed(s);
        return r;
    }

    /**
     * Gets 6D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        if(!canUseSeed()) {
            float s = seed * 0x1p-48f;
            return getNoise(x + s, y + s, z + s, w + s, u + s, v + s);
        }
        final long s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w, u, v);
        setSeed(s);
        return r;
    }
}
