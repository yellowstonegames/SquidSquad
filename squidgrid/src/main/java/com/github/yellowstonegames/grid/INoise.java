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

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.ObjectSet;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public interface INoise extends Externalizable {
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
     * Returns true if this generator can be seeded with {@link #setSeed(long)} during each call to obtain noise, or
     * false if calling setSeed() is slow enough or allocates enough that alternative approaches should be used. You
     * can always call setSeed() on your own, but generators that don't have any seed won't do anything, and generators
     * that return false for this method will generally behave differently when comparing how
     * {@link #getNoiseWithSeed(float, float, long)} changes the seed and how setSeed() does.
     *
     * @return whether {@link #setSeed(long)} should be safe to call in every {@link #getNoiseWithSeed} call
     */
    boolean hasEfficientSetSeed();

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
     * supported.
     * If this generator cannot be seeded, this should do nothing, and should not throw an exception. If this operation
     * allocates or is time-intensive, then {@link #hasEfficientSetSeed()} should return false. That method is checked
     * in {@link #getNoiseWithSeed}, and if it returns false, the noise call will avoid calling setSeed(). You can
     * always at least try to set the seed, even if it does nothing or is heavy on performance, and doing it a few times
     * each frame should typically be fine for any generator. In the case this is called thousands of times each frame,
     * check {@link #hasEfficientSetSeed()}.
     * @param seed a long or int seed, with no restrictions unless otherwise documented
     */
    void setSeed(long seed);

    /**
     * Gets the current seed of the generator, as a long even if the seed is stored internally as an int. This must be
     * implemented, but if the generator doesn't have a seed that can be expressed as a long (potentially using
     * {@link com.github.tommyettinger.digital.BitConversion#floatToIntBits(float)}), this can just return {@code 0}.
     * @return the current seed, as a long
     */
    long getSeed();

    /**
     * Returns a typically-four-character String constant that should uniquely identify this INoise as well as possible.
     * If a duplicate tag is already registered and {@link Serializer#register(INoise)} attempts to register the same
     * tag again, a message is printed to {@code System.err}. The default implementation returns the String
     * {@code (NO)}, which is essentially an invalid tag, meant to indicate that this was not fully implemented.
     * Implementing this is required for any usage of Serializer.
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
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @GwtIncompatible
    default void writeExternal(ObjectOutput out) throws IOException{
        out.writeUTF(stringSerialize());
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     */
    @GwtIncompatible
    default void readExternal(ObjectInput in) throws IOException {
        stringDeserialize(in.readUTF());
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

    boolean equals(Object other);

    /**
     * Gets 2D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, long seed) {
        if(!hasEfficientSetSeed()) {
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
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, long seed) {
        if(!hasEfficientSetSeed()) {
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
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        if(!hasEfficientSetSeed()) {
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
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    default float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        if(!hasEfficientSetSeed()) {
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
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
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
        if(!hasEfficientSetSeed()) {
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
            register(new BadgerNoise());
            register(new BasicHashNoise());
            register(new CellularNoise());
            register(new CyclicNoise());
            register(new FlanNoise());
            register(new FoamNoise());
            register(new FoamplexNoise());
            register(new HighDimensionalValueNoise());
            register(new HoneyNoise());
            register(new Noise());
            register(new OpenSimplex2());
            register(new OpenSimplex2Smooth());
            register(new PerlinNoise());
            register(new PerlueNoise());
            register(new PhantomNoise());
            register(new ShapedFoamNoise());
            register(new SimplexNoise());
            register(new SimplexNoiseHard());
            register(new SimplexNoiseScaled());
            register(new SnakeNoise());
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
         * <br>
         * This is preferred over calling {@link #stringSerialize()} on its own, because it prepends the
         * {@link #getTag()} to allow {@link #deserialize(String)} to load it back regardless of implementation.
         * If you know only one INoise class will be used, you can use its {@link #stringSerialize()} and its
         * {@link #stringDeserialize(String)} methods to save and restore that single class.
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
         * <br>
         * This is preferred over calling {@link #stringDeserialize(String)} if the implementation of the serialized
         * INoise is not known. This will return an INoise with the correct implementation, though the compile-time
         * type will still be INoise and not anything more specific.
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
                throw new RuntimeException("Tag: '" + tagData + "' in given data: '" + data + "' is invalid or unknown.");
            return root.copy().stringDeserialize(data.substring(idx));
        }

    /**
     * Creates an unordered Set of all String tags for INoise types Serializer knows, and returns it.
     * A tag can be used with {@link #get(String)} to retrieve the corresponding INoise.
     * @return an ObjectSet of all String tags for INoise types this knows
     */
    public static ObjectSet<String> copyTags() {
        return new ObjectSet<>(NOISE_BY_TAG.keySet());
    }

    /**
     * Returns a List (in no particular order) of copies of the INoise "prototype" objects this uses during
     * deserialization. Each INoise copy is seeded with {@code -1L} before it is put in the List.
     * This includes wrapper types such as {@link NoiseWrapper}, using their default INoise and other settings
     * when retrieved.
     * @return an ObjectList of copies of the INoise instances this knows
     */
    public static ObjectList<INoise> copyNoises() {
        ObjectList<INoise> list = new ObjectList<>(NOISE_BY_TAG.size());
        for(INoise e : NOISE_BY_TAG.values()){
            INoise r = e.copy();
            r.setSeed(-1L);
            list.add(r);
        }
        return list;
    }

    }
}
