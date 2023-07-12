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
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.MathTools.fastFloor;

/**
 * A type of noise that tries to find out what is normal behavior for noise... and what is sus.
 * Big ups to Rogue Frontier Guy for coming up with the name.
 */
@Beta
public class AmogusNoise implements INoise {
    public long seed;

    public AmogusNoise() {
        this(0xBEEF1E571337D00DL);
    }

    public AmogusNoise(long seed) {
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
        return 2; // FOR NOW
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
        return "AmoN";
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
    public INoise copy() {
        return new AmogusNoise(seed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
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
        return "`" + seed + "`";
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
    public AmogusNoise deserializeFromString(String data) {
        seed = (DigitTools.longFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static AmogusNoise recreateFromString(String data) {
        return new AmogusNoise(DigitTools.longFromDec(data, 1, data.length() - 1));
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
        long
                x0 = fastFloor(x),
                y0 = fastFloor(y);
        final float xf = x - x0, yf = y - y0;

        long s = seed;
        x = xf * xf * (3 - 2 * xf);
        y = yf * yf * (3 - 2 * yf);
//        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        return (((1 - y) * ((1 - x) * (LongPointHash.hashAll(x0, y0, s)) + x * (LongPointHash.hashAll(x0+1, y0, s)))
                + y * ((1 - x) * (LongPointHash.hashAll(x0, y0 + 1, s)) + x * (LongPointHash.hashAll(x0+1, y0+1, s)))) * 0x1p-63f);
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
        return 0;
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
        return 0;
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
        return 0;
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
        return 0;
    }
}
