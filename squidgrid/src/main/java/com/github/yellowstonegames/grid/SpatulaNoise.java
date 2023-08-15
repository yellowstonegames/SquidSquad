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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.MathTools.fastFloor;

/**
 * A non-functional kind of value noise that will probably be deleted soon.
 * This is based on Transfinite Interpolation between curving 1D lines that use varying Barron splines to adjust their
 * shapes and turning points. This doesn't seem to work at corners, probably because the turning and shape info are
 * obtained from the edge between two vertices, and the corners need to only use the vertex.
 */
@Beta
public class SpatulaNoise implements INoise {
    public long seed;
    public SpatulaNoise() {
        this(1234567890L);
    }
    public SpatulaNoise(long seed){
        this.seed = seed;
    }
    /**
     * Gets the minimum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 3 for one that is only defined for 3D or higher-dimensional spaces.
     *
     * @return the minimum supported dimension, from 2 to 6 inclusive
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 6 for one that is defined up to the highest dimension this interface knows about (6D).
     *
     * @return the maximum supported dimension, from 2 to 6 inclusive
     */
    @Override
    public int getMaxDimension() {
        return 2;
    }

    /**
     * Returns true if this generator can be seeded with {@link #setSeed(long)} (and if so, retrieved with
     * {@link #getSeed()}).
     *
     * @return true if {@link #setSeed(long)} and {@link #getSeed()} are supported, false if either isn't supported
     */
    @Override
    public boolean canUseSeed() {
        return true;
    }

    /**
     * A wobbly-line-segment method that takes some kind of hash-generated {@code long}s for the
     * {@code startBits} and {@code endBits}. This makes this usable as a building-block for noise with more than
     * one dimension. Unlike the other wobbling-line methods here, {@code value} must be between 0 and 1 (inclusive).
     * The name comes from "hash wobble."
     * This can be useful when you want a curve to seem more "natural," without the similarity between every peak or
     * every valley in {@link LineWobble#wobble(long, float)}. This can produce both fairly sharp turns and very gradual curves.
     * @param startBits any long; determines the result exactly when {@code value} is 0, and later affects it less
     * @param endBits any long; determines the result exactly when {@code value} is 1, and earlier affects it less
     * @param value a float between 0f and 1f, representing how much the result is affected by the start and end bits
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float hobble(long startBits, long endBits, float value)
    {
        long mixBits = startBits + endBits;
        mixBits ^= mixBits >>> 32;
        final float start = startBits * 0x0.ffffffp-63f,
                end = endBits  * 0x0.ffffffp-63f;
        value = MathTools.barronSpline(value, (mixBits & 0xFFFFFFFFL) * 0x1p-30f + 0.5f, (mixBits & 0xFFFFL) * 0x1.8p-17f + 0.125f);
        value *= value * (3f - 2f * value);
        return (1 - value) * start + value * end;
    }

    /**
     * Gets 2D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    /**
     * Gets 3D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z) {
        return SimplexNoise.noise(x, y, z, seed);
    }

    /**
     * Gets 4D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w) {
        return SimplexNoise.noise(x, y, z, w, seed);
    }

    /**
     * Gets 5D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return SimplexNoise.noise(x, y, z, w, u, seed);
    }

    /**
     * Gets 6D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return SimplexNoise.noise(x, y, z, w, u, v, seed);
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
        this.seed = seed;
    }

    /**
     * Gets the current seed of the generator, as a long even if the seed is stored internally as an int.
     * If {@link #canUseSeed()} returns true, this must be implemented, but if canUseSeed() returns false, this is
     * permitted to either still be implemented (but typically only if it is time- or space-intensive to call getSeed())
     * or to throw an {@link UnsupportedOperationException}.
     *
     * @return the current seed, as a long
     */
    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Returns a typically-four-character String constant that should uniquely identify this INoise as well as possible.
     * If a duplicate tag is already registered and {@link Serializer#register(INoise)} attempts to register the same
     * tag again, a message is printed to {@code System.err}. The default implementation returns the String
     * {@code (NO)}, which is already registered in Serializer to a null value. Implementing this is required for any
     * usage of Serializer.
     *
     * @return a short String constant that identifies this INoise type
     */
    @Override
    public String getTag() {
        return "SpaN";
    }

    /**
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #deserializeFromString(String)} to reassign the described state to another INoise. The syntax here
     * should always start and end with the {@code `} character, which is used by
     * {@link Serializer#deserializeFromString(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another INoise that this uses.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #deserializeFromString(String)}, and
     * {@link #copy()}.
     *
     * @return a String that describes this INoise for serialization
     */
    @Override
    public String serializeToString() {
        return Base.BASE16.unsigned(seed);
    }

    /**
     * Given a serialized String produced by {@link #serializeToString()}, reassigns this INoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #deserializeFromString(String)}, and
     * {@link #copy()}.
     *
     * @param data a serialized String, typically produced by {@link #serializeToString()}
     * @return this INoise, after being modified (if possible)
     */
    @Override
    public SpatulaNoise deserializeFromString(String data) {
        setSeed(Base.BASE16.readLong(data));
        return this;
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. Implementors are strongly
     * encouraged to implement this in general, and that is required to use an INoise class with {@link Serializer}.
     *
     * @return a copy of this INoise
     */
    @Override
    public SpatulaNoise copy() {
        return new SpatulaNoise(seed);
    }

    /**
     * Gets 2D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y);
        float xf = x - x0, yf = y - y0;
        xf *= xf * (3f - 2f * xf);
        yf *= yf * (3f - 2f * yf);

        final long x0y0 = LongPointHash.hashAll(x0, y0, seed);
        final long x1y0 = LongPointHash.hashAll(x0+1, y0, seed);
        final long x0y1 = LongPointHash.hashAll(x0, y0+1, seed);
        final long x1y1 = LongPointHash.hashAll(x0+1, y0+1, seed);

        final float xs = (1 - yf) * hobble(x0y0, x1y0, xf) + yf * hobble(x0y1, x1y1, xf);
        final float ys = (1 - xf) * hobble(x0y0, x0y1, yf) + xf * hobble(x1y0, x1y1, yf);
        final float b = ((1 - xf) * (1 - yf) * x0y0 + xf * yf * x1y1 + xf * (1 - yf) * x1y0 + (1 - xf) * yf * x0y1) * 0x0.ffffffp-63f;
        return xs + ys - b;
    }

    /**
     * Gets 3D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return SimplexNoise.noise(x, y, z, seed);
    }

    /**
     * Gets 4D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return SimplexNoise.noise(x, y, z, w, seed);
    }

    /**
     * Gets 5D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return SimplexNoise.noise(x, y, z, w, u, seed);
    }

    /**
     * Gets 6D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param v    v position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return SimplexNoise.noise(x, y, z, w, u, v, seed);
    }
}
