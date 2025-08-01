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

import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.support.sort.IntComparator;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectIntOrderedMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectIntOrderedMap} instead, since some optimizations here require Coord keys to be in
 * the pool.
 * <br>
 * You can create a CoordIntOrderedMap with {@link #fromArray2D(int[][], int, int)} if you have a 2D int array and
 * want to get the positions within some range of values.
 */
public class CoordIntOrderedMap extends ObjectIntOrderedMap<Coord> {
    public CoordIntOrderedMap(OrderType type) {
        this(51, 0.9f, type);
    }

    public CoordIntOrderedMap(int initialCapacity, OrderType type) {
        super(initialCapacity, 0.9f, type);
    }

    public CoordIntOrderedMap(int initialCapacity, float loadFactor, OrderType type) {
        super(initialCapacity, loadFactor, type);
    }

    public CoordIntOrderedMap(ObjectIntOrderedMap<? extends Coord> map, OrderType type) {
        super(map, type);
    }

    public CoordIntOrderedMap(ObjectIntMap<? extends Coord> map, OrderType type) {
        super(map, type);
    }

    public CoordIntOrderedMap(Coord[] keys, int[] values, OrderType type) {
        this(Math.min(keys.length, values.length), type);
        this.putAll(keys, values);
    }

    public CoordIntOrderedMap(Collection<? extends Coord> keys, PrimitiveCollection.OfInt values, OrderType type) {
        this(Math.min(keys.size(), values.size()), type);
        this.putAll(keys, values);
    }

    public CoordIntOrderedMap(ObjectIntOrderedMap<? extends Coord> other, int offset, int count, OrderType type) {
        this(count, type);
        this.putAll(0, other, offset, count);
    }

    public CoordIntOrderedMap() {
        this(51, 0.9f);
    }

    public CoordIntOrderedMap(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordIntOrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordIntOrderedMap(ObjectIntOrderedMap<? extends Coord> map) {
        super(map);
    }

    public CoordIntOrderedMap(ObjectIntMap<? extends Coord> map) {
        super(map);
    }

    public CoordIntOrderedMap(Coord[] keys, int[] values) {
        this(Math.min(keys.length, values.length));
        this.putAll(keys, values);
    }

    public CoordIntOrderedMap(Collection<? extends Coord> keys, PrimitiveCollection.OfInt values) {
        this(Math.min(keys.size(), values.size()));
        this.putAll(keys, values);
    }

    public CoordIntOrderedMap(ObjectIntOrderedMap<? extends Coord> other, int offset, int count) {
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
    public static CoordIntOrderedMap withNothing () {
        return new CoordIntOrderedMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number value to a primitive int, regardless of which Number type was used.
     *
     * @param key0   the first and only key
     * @param value0 the first and only value; will be converted to primitive int
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordIntOrderedMap with (Coord key0, Number value0) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(1);
        map.put(key0, value0.intValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive ints, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive int
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive int
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap with (Coord key0, Number value0, Coord key1, Number value1) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(2);
        map.put(key0, value0.intValue());
        map.put(key1, value1.intValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive ints, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive int
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive int
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive int
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(3);
        map.put(key0, value0.intValue());
        map.put(key1, value1.intValue());
        map.put(key2, value2.intValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive ints, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive int
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive int
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive int
     * @param key3   a Coord key
     * @param value3 a Number for a value; will be converted to primitive int
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2, Coord key3, Number value3) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(4);
        map.put(key0, value0.intValue());
        map.put(key1, value1.intValue());
        map.put(key2, value2.intValue());
        map.put(key3, value3.intValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This can be useful in some code-generation scenarios, or when you want to make a
     * map conveniently by-hand and have it populated at the start. You can also use
     * {@link #CoordIntOrderedMap(Coord[], int[])}, which takes all keys and then all values.
     * This needs all keys to have the same type, because it gets a generic type from the
     * first key parameter. All values must be some type of boxed Number, such as {@link Integer}
     * or {@link Double}, and will be converted to primitive {@code int}s. Any keys that don't
     * have Coord as their type or values that aren't {@code Number}s have that entry skipped.
     *
     * @param key0   the first key; will be used to determine the type of all keys
     * @param value0 the first value; will be converted to primitive int
     * @param rest   an array or varargs of alternating Coord, Number, Coord, Number... elements, inferred from key0
     * @return a new map containing the given keys and values
     */
    public static CoordIntOrderedMap with (Coord key0, Number value0, Object... rest) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(1 + (rest.length >>> 1));
        map.put(key0, value0.intValue());
        for (int i = 1; i < rest.length; i += 2) {
            try {
                map.put((Coord)rest[i - 1], ((Number)rest[i]).intValue());
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
    public static CoordIntOrderedMap withPrimitive () {
        return new CoordIntOrderedMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed int as
     * its value type, and will not box it.
     *
     * @param key0   a Coord for a key
     * @param value0 a int for a value
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordIntOrderedMap withPrimitive (Coord key0, int value0) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(1);
        map.put(key0, value0);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed int as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a int for a value
     * @param key1   a Coord key
     * @param value1 a int for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap withPrimitive (Coord key0, int value0, Coord key1, int value1) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(2);
        map.put(key0, value0);
        map.put(key1, value1);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed int as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a int for a value
     * @param key1   a Coord key
     * @param value1 a int for a value
     * @param key2   a Coord key
     * @param value2 a int for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap withPrimitive (Coord key0, int value0, Coord key1, int value1, Coord key2, int value2) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(3);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed int as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a int for a value
     * @param key1   a Coord key
     * @param value1 a int for a value
     * @param key2   a Coord key
     * @param value2 a int for a value
     * @param key3   a Coord key
     * @param value3 a int for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordIntOrderedMap withPrimitive (Coord key0, int value0, Coord key1, int value1, Coord key2, int value2, Coord key3, int value3) {
        CoordIntOrderedMap map = new CoordIntOrderedMap(4);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Given a 2D int array (which should usually be rectangular), this finds the positions with values between min
     * (inclusive) and max (also inclusive), and places them in a newly-allocated CoordIntOrderedMap. You may want to
     * sort this by value, using {@link #sortByValue(IntComparator)}, so the highest or lowest int values beint to
     * the first or last items in the order.
     * @param array a usually-rectangular 2D int array
     * @param min the inclusive minimum value
     * @param max the inclusive maximum value
     * @return a new CoordIntOrderedMap containing all positions with in-range values, mapped to those values.
     */
    public static CoordIntOrderedMap fromArray2D(int[][] array, int min, int max) {
        final int width = array.length;
        CoordIntOrderedMap map = new CoordIntOrderedMap(width * array[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < array[x].length; y++) {
                final int n = array[x][y];
                if (n >= min && n <= max) {
                    map.put(Coord.get(x, y), n);
                }
            }
        }
        return map;
    }
}
