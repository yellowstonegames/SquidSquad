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

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.ObjectFloatOrderedMap;
import com.github.tommyettinger.ds.OrderType;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.tommyettinger.ds.support.sort.FloatComparator;

import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectFloatOrderedMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectFloatOrderedMap} instead, since some optimizations here require Coord keys to be in
 * the pool.
 * <br>
 * A potential use for this class is to sort Coord positions by float values, such as the {@code float[][]} maps
 * produced by {@link FOV}. Since those have a logical start point for iteration wherever a light source is, and a light
 * source has the highest value, you could fill a CoordFloatOrderedMap with all Coords you wanted to iterate through,
 * associated with their values in the {@code float[][]}, and call {@link #sortByValue(FloatComparator)} to change the
 * keys' iteration order. There is code for this in {@link #fromArray2D(float[][], float, float)}.
 */
public class CoordFloatOrderedMap extends ObjectFloatOrderedMap<Coord> {
    public CoordFloatOrderedMap(OrderType type) {
        this(51, 0.9f, type);
    }

    public CoordFloatOrderedMap(int initialCapacity, OrderType type) {
        super(initialCapacity, 0.9f, type);
    }

    public CoordFloatOrderedMap(int initialCapacity, float loadFactor, OrderType type) {
        super(initialCapacity, loadFactor, type);
    }

    public CoordFloatOrderedMap(ObjectFloatOrderedMap<? extends Coord> map, OrderType type) {
        super(map, type);
    }

    public CoordFloatOrderedMap(ObjectFloatMap<? extends Coord> map, OrderType type) {
        super(map, type);
    }

    public CoordFloatOrderedMap(Coord[] keys, float[] values, OrderType type) {
        this(Math.min(keys.length, values.length), type);
        this.putAll(keys, values);
    }

    public CoordFloatOrderedMap(Collection<? extends Coord> keys, PrimitiveCollection.OfFloat values, OrderType type) {
        this(Math.min(keys.size(), values.size()), type);
        this.putAll(keys, values);
    }

    public CoordFloatOrderedMap(ObjectFloatOrderedMap<? extends Coord> other, int offset, int count, OrderType type) {
        this(count, type);
        this.putAll(0, other, offset, count);
    }

    public CoordFloatOrderedMap() {
        this(51, 0.9f);
    }

    public CoordFloatOrderedMap(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordFloatOrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordFloatOrderedMap(ObjectFloatOrderedMap<? extends Coord> map) {
        super(map);
    }

    public CoordFloatOrderedMap(ObjectFloatMap<? extends Coord> map) {
        super(map);
    }

    public CoordFloatOrderedMap(Coord[] keys, float[] values) {
        this(Math.min(keys.length, values.length));
        this.putAll(keys, values);
    }

    public CoordFloatOrderedMap(Collection<? extends Coord> keys, PrimitiveCollection.OfFloat values) {
        this(Math.min(keys.size(), values.size()));
        this.putAll(keys, values);
    }

    public CoordFloatOrderedMap(ObjectFloatOrderedMap<? extends Coord> other, int offset, int count) {
        this(count);
        this.putAll(0, other, offset, count);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), precalculated
    }

    @Override
    protected boolean equate(Object left, @Nullable Object right) {
        return left == right;
    }

    /**
     * Constructs an empty map.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     * <br>
     * This needs to be named differently because of shadowing issues with the parent class.
     *
     * @return a new map containing nothing
     */
    public static CoordFloatOrderedMap withNothing () {
        return new CoordFloatOrderedMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number value to a primitive float, regardless of which Number type was used.
     *
     * @param key0   the first and only key
     * @param value0 the first and only value; will be converted to primitive float
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordFloatOrderedMap with (Coord key0, Number value0) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(1);
        map.put(key0, value0.floatValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive floats, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive float
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive float
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap with (Coord key0, Number value0, Coord key1, Number value1) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(2);
        map.put(key0, value0.floatValue());
        map.put(key1, value1.floatValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive floats, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive float
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive float
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive float
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(3);
        map.put(key0, value0.floatValue());
        map.put(key1, value1.floatValue());
        map.put(key2, value2.floatValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive floats, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive float
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive float
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive float
     * @param key3   a Coord key
     * @param value3 a Number for a value; will be converted to primitive float
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2, Coord key3, Number value3) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(4);
        map.put(key0, value0.floatValue());
        map.put(key1, value1.floatValue());
        map.put(key2, value2.floatValue());
        map.put(key3, value3.floatValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This can be useful in some code-generation scenarios, or when you want to make a
     * map conveniently by-hand and have it populated at the start. You can also use
     * {@link #CoordFloatOrderedMap(Coord[], float[])}, which takes all keys and then all values.
     * This needs all keys to have the same type, because it gets a generic type from the
     * first key parameter. All values must be some type of boxed Number, such as {@link Integer}
     * or {@link Double}, and will be converted to primitive {@code float}s. Any keys that don't
     * have Coord as their type or values that aren't {@code Number}s have that entry skipped.
     *
     * @param key0   the first key; will be used to determine the type of all keys
     * @param value0 the first value; will be converted to primitive float
     * @param rest   an array or varargs of alternating Coord, Number, Coord, Number... elements, inferred from key0
     * @return a new map containing the given keys and values
     */
    public static CoordFloatOrderedMap with (Coord key0, Number value0, Object... rest) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(1 + (rest.length >>> 1));
        map.put(key0, value0.floatValue());
        for (int i = 1; i < rest.length; i += 2) {
            try {
                map.put((Coord)rest[i - 1], ((Number)rest[i]).floatValue());
            } catch (ClassCastException ignored) {
            }
        }
        return map;
    }

    /**
     * Constructs an empty map given the key type as a generic type argument.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     *
     * @return a new map containing nothing
     */
    public static CoordFloatOrderedMap withPrimitive () {
        return new CoordFloatOrderedMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed float as
     * its value type, and will not box it.
     *
     * @param key0   a Coord for a key
     * @param value0 a float for a value
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordFloatOrderedMap withPrimitive (Coord key0, float value0) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(1);
        map.put(key0, value0);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed float as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a float for a value
     * @param key1   a Coord key
     * @param value1 a float for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap withPrimitive (Coord key0, float value0, Coord key1, float value1) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(2);
        map.put(key0, value0);
        map.put(key1, value1);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed float as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a float for a value
     * @param key1   a Coord key
     * @param value1 a float for a value
     * @param key2   a Coord key
     * @param value2 a float for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap withPrimitive (Coord key0, float value0, Coord key1, float value1, Coord key2, float value2) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(3);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed float as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a float for a value
     * @param key1   a Coord key
     * @param value1 a float for a value
     * @param key2   a Coord key
     * @param value2 a float for a value
     * @param key3   a Coord key
     * @param value3 a float for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordFloatOrderedMap withPrimitive (Coord key0, float value0, Coord key1, float value1, Coord key2, float value2, Coord key3, float value3) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(4);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Given a 2D float array (which should usually be rectangular), this finds the positions with values between min
     * (inclusive) and max (also inclusive), and places them in a newly-allocated CoordFloatOrderedMap. You may want to
     * sort this by value, using {@link #sortByValue(FloatComparator)}, so the highest or lowest float values belong to
     * the first or last items in the order.
     * @param array a usually-rectangular 2D float array
     * @param min the inclusive minimum value
     * @param max the inclusive maximum value
     * @return a new CoordFloatOrderedMap containing all positions with in-range values, mapped to those values.
     */
    public static CoordFloatOrderedMap fromArray2D(float[][] array, float min, float max) {
        final int width = array.length;
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(width * array[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < array[x].length; y++) {
                final float f = array[x][y];
                if (f >= min && f <= max) {
                    map.put(Coord.get(x, y), f);
                }
            }
        }
        return map;
    }
}
