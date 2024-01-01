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

import com.github.tommyettinger.ds.ObjectObjectMap;

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

    /**
     * Returns a typically-four-character String constant that should uniquely identify this INoise as well as possible.
     * If a duplicate tag is already registered and {@link Serializer#register(INoise)} attempts to register the same
     * tag again, a message is printed to {@code System.err}. The default implementation returns the String
     * {@code (NO)}, which is already registered in Serializer to a null value. Implementing this is required for any
     * usage of Serializer.
     * @return a short String constant that identifies this INoise type
     */
    default String getTag() {
        return "(NO)";
    }

    /**
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another INoise. The syntax here
     * should always start and end with the {@code `} character, which is used by
     * {@link #stringDeserialize(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another INoise that this uses.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #stringDeserialize(String)}, and
     * {@link #copy()}.
     * @return a String that describes this INoise for serialization
     */
    default String stringSerialize() {
        throw new UnsupportedOperationException("stringSerialize() is not supported.");
    }

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this INoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #stringSerialize()}, and
     * {@link #copy()}.
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this INoise, after being modified (if possible)
     */
    default INoise stringDeserialize(String data) {
        throw new UnsupportedOperationException("stringDeserialize() is not supported.");
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. Implementors are strongly
     * encouraged to implement this in general, and that is required to use an INoise class with {@link Serializer}.
     * @return a copy of this INoise
     */
    default INoise copy() {
        throw new UnsupportedOperationException("copy() is not supported.");
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

    class Serializer {
        /**
         * Not instantiable.
         */
        private Serializer() {
        }

        private static final ObjectObjectMap<String, INoise> NOISE_BY_TAG = new ObjectObjectMap<>(16);

        /**
         * Given a (typically freshly-constructed and never-reused) INoise, this registers that instance by its
         * {@link INoise#getTag()} in a Map, so that this type of INoise can be deserialized correctly by
         * {@link #deserialize(String)}. The INoise type must implement {@link INoise#getTag()},
         * {@link INoise#stringSerialize()}, {@link INoise#stringDeserialize(String)}, and {@link INoise#copy()}.
         *
         * @param random a (typically freshly-constructed) INoise that should never be reused elsewhere
         */
        public static void register(INoise random) {
            String tag = random.getTag();
            if (!NOISE_BY_TAG.containsKey(tag))
                NOISE_BY_TAG.put(tag, random);
            else
                System.err.println("When registering an INoise, a duplicate tag failed to register: " + tag);
        }

        static {
            NOISE_BY_TAG.put("(NO)", null); // for classes that cannot be serialized
            register(new BasicHashNoise());
            register(new CyclicNoise());
            register(new FlanNoise());
            register(new FoamNoise());
            register(new HighDimensionalValueNoise());
            register(new HoneyNoise());
            register(new Noise());
            register(new OpenSimplex2());
            register(new OpenSimplex2Smooth());
            register(new PerlinNoise());
            register(new PhantomNoise());
            register(new SimplexNoise());
            register(new SimplexNoiseHard());
            register(new SimplexNoiseScaled());
            register(new SorbetNoise());
            register(new TaffyNoise());
            register(new ValueNoise());

            register(new NoiseAdjustment());
            register(new NoiseWrapper());
            register(new RadialNoiseWrapper());
        }

        /**
         * Gets a copy of the INoise registered with the given tag, or null if this has nothing registered for the
         * given tag.
         *
         * @param tag a non-null String that could be used as a tag for an INoise registered with this class
         * @return a new copy of the corresponding INoise, or null if none was found
         */
        public static INoise get(String tag) {
            INoise r = NOISE_BY_TAG.get(tag);
            if (r == null) return null;
            return r.copy();
        }

        /**
         * Given an INoise that implements {@link #stringSerialize()} and {@link #getTag()}, this produces a
         * serialized String that stores the exact state of the INoise. This serialized String can be read back in by
         * {@link #deserialize(String)}.
         *
         * @param noise an INoise that implements {@link #stringSerialize()} and {@link #getTag()}
         * @return a String that can be read back in by {@link #deserialize(String)}
         */
        public static String serialize(INoise noise) {
            return noise.getTag() + noise.stringSerialize();
        }

        /**
         * Given a String produced by calling {@link #serialize(INoise)} on any registered implementation
         * (as with {@link #register(INoise)}), this reads in the deserialized data and returns a new INoise
         * of the appropriate type. This relies on the {@link INoise#getTag() tag} of the type being registered at
         * deserialization time, though it doesn't actually need to be registered at serialization time. This cannot
         * read back the direct output of {@link INoise#stringSerialize()}; it needs the tag prepended by
         * {@link #serialize(INoise)} to work.
         *
         * @param data serialized String data probably produced by {@link #serialize(INoise)}
         * @return a new INoise with the appropriate type internally, using the state from data
         */
        public static INoise deserialize(String data) {
            int idx = data.indexOf('`');
            if (idx == -1)
                throw new IllegalArgumentException("String given cannot represent a valid INoise.");
            String tagData = data.substring(0, idx);
            INoise root = NOISE_BY_TAG.get(tagData);
            if (root == null)
                throw new RuntimeException("Tag in given data is invalid or unknown.");
            return root.copy().stringDeserialize(data.substring(idx));
        }

    }
}
