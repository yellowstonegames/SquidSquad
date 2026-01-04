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

import com.github.tommyettinger.ds.ObjectLongMap;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.tommyettinger.ds.Utilities;

import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectLongMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectLongMap} instead, since some optimizations here require Coord keys to be in the
 * pool.
 * <br>
 * If no initialCapacity is supplied, or if this must resize to enter a Coord, this will use a capacity at least as
 * large as the Coord cache, as defined by {@link Coord#getCacheWidth()} by {@link Coord#getCacheHeight()}. While this
 * means that any resizing will potentially make this use much more memory, it avoids a situation where some dense key
 * sets could take hundreds of times longer than they should. It also usually doesn't use drastically more memory unless
 * the Coord pool has been expanded quite a bit.
 * <br>
 * This tends to perform significantly better with a high low factor, such as 0.9f, instead of a lower one like 0.5f .
 * It also performs its best when the initial capacity is sufficient to hold every key this needs without resizing, but
 * it typically only has to resize once if it has to resize at all.
 * <br>
 * You can create a CoordLongMap with {@link #fromArray2D(long[][], long, long)} if you have a 2D long array and
 * want to get the positions within some range of values.
 */
public class CoordLongMap extends ObjectLongMap<Coord> {
    public CoordLongMap() {
        this(Coord.getCacheWidth() * Coord.getCacheHeight(), 0.9f);
    }

    public CoordLongMap(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordLongMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordLongMap(ObjectLongMap<? extends Coord> map) {
        super(map);
    }

    public CoordLongMap(Coord[] keys, long[] values) {
        this(Math.min(keys.length, values.length));
        this.putAll(keys, values);
    }

    public CoordLongMap(Collection<? extends Coord> keys, PrimitiveCollection.OfLong values) {
        this(Math.min(keys.size(), values.size()));
        this.putAll(keys, values);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), precalculated
    }

    @Override
    protected boolean equate(Object left, Object right) {
        return left == right;
    }

    @Override
    protected void resize(int newSize) {
        super.resize(Math.max(newSize, Utilities.tableSize(Coord.getCacheWidth() * Coord.getCacheHeight(), loadFactor)));
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
    public static CoordLongMap withNothing () {
        return new CoordLongMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number value to a primitive long, regardless of which Number type was used.
     *
     * @param key0   the first and only key
     * @param value0 the first and only value; will be converted to primitive long
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordLongMap with (Coord key0, Number value0) {
        CoordLongMap map = new CoordLongMap(1);
        map.put(key0, value0.longValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive longs, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive long
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive long
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap with (Coord key0, Number value0, Coord key1, Number value1) {
        CoordLongMap map = new CoordLongMap(2);
        map.put(key0, value0.longValue());
        map.put(key1, value1.longValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive longs, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive long
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive long
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive long
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2) {
        CoordLongMap map = new CoordLongMap(3);
        map.put(key0, value0.longValue());
        map.put(key1, value1.longValue());
        map.put(key2, value2.longValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number values to primitive longs, regardless of which Number type was used.
     *
     * @param key0   a Coord key
     * @param value0 a Number for a value; will be converted to primitive long
     * @param key1   a Coord key
     * @param value1 a Number for a value; will be converted to primitive long
     * @param key2   a Coord key
     * @param value2 a Number for a value; will be converted to primitive long
     * @param key3   a Coord key
     * @param value3 a Number for a value; will be converted to primitive long
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap with (Coord key0, Number value0, Coord key1, Number value1, Coord key2, Number value2, Coord key3, Number value3) {
        CoordLongMap map = new CoordLongMap(4);
        map.put(key0, value0.longValue());
        map.put(key1, value1.longValue());
        map.put(key2, value2.longValue());
        map.put(key3, value3.longValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This can be useful in some code-generation scenarios, or when you want to make a
     * map conveniently by-hand and have it populated at the start. You can also use
     * {@link #CoordLongMap(Coord[], long[])}, which takes all keys and then all values.
     * This needs all keys to have the same type, because it gets a generic type from the
     * first key parameter. All values must be some type of boxed Number, such as {@link Integer}
     * or {@link Double}, and will be converted to primitive {@code long}s. Any keys that don't
     * have Coord as their type or values that aren't {@code Number}s have that entry skipped.
     *
     * @param key0   the first key; will be used to determine the type of all keys
     * @param value0 the first value; will be converted to primitive long
     * @param rest   an array or varargs of alternating Coord, Number, Coord, Number... elements, inferred from key0
     * @return a new map containing the given keys and values
     */
    public static CoordLongMap with (Coord key0, Number value0, Object... rest) {
        CoordLongMap map = new CoordLongMap(1 + (rest.length >>> 1));
        map.put(key0, value0.longValue());
        for (int i = 1; i < rest.length; i += 2) {
            try {
                map.put((Coord)rest[i - 1], ((Number)rest[i]).longValue());
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
    public static CoordLongMap withPrimitive () {
        return new CoordLongMap(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed long as
     * its value type, and will not box it.
     *
     * @param key0   a Coord for a key
     * @param value0 a long for a value
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordLongMap withPrimitive (Coord key0, long value0) {
        CoordLongMap map = new CoordLongMap(1);
        map.put(key0, value0);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Unlike with(), this takes unboxed long as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a long for a value
     * @param key1   a Coord key
     * @param value1 a long for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap withPrimitive (Coord key0, long value0, Coord key1, long value1) {
        CoordLongMap map = new CoordLongMap(2);
        map.put(key0, value0);
        map.put(key1, value1);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed long as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a long for a value
     * @param key1   a Coord key
     * @param value1 a long for a value
     * @param key2   a Coord key
     * @param value2 a long for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap withPrimitive (Coord key0, long value0, Coord key1, long value1, Coord key2, long value2) {
        CoordLongMap map = new CoordLongMap(3);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values.  Unlike with(), this takes unboxed long as
     * its value type, and will not box it.
     *
     * @param key0   a Coord key
     * @param value0 a long for a value
     * @param key1   a Coord key
     * @param value1 a long for a value
     * @param key2   a Coord key
     * @param value2 a long for a value
     * @param key3   a Coord key
     * @param value3 a long for a value
     * @return a new map containing the given key-value pairs
     */
    public static CoordLongMap withPrimitive (Coord key0, long value0, Coord key1, long value1, Coord key2, long value2, Coord key3, long value3) {
        CoordLongMap map = new CoordLongMap(4);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Given a 2D long array (which should usually be rectangular), this finds the positions with values between min
     * (inclusive) and max (also inclusive), and places them in a newly-allocated CoordLongMap. If you need to
     * get a sorted ordering, use {@link CoordLongOrderedMap#fromArray2D(long[][], long, long)} and sort the result.
     * @param array a usually-rectangular 2D long array
     * @param min the inclusive minimum value
     * @param max the inclusive maximum value
     * @return a new CoordLongMap containing all positions with in-range values, mapped to those values.
     */
    public static CoordLongMap fromArray2D(long[][] array, long min, long max) {
        final int width = array.length;
        CoordLongMap map = new CoordLongMap(width * array[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < array[x].length; y++) {
                final long n = array[x][y];
                if (n >= min && n <= max) {
                    map.put(Coord.get(x, y), n);
                }
            }
        }
        return map;
    }

}
