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

import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;

import com.github.tommyettinger.ds.OrderType;
import com.github.tommyettinger.ds.Utilities;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A variant on jdkgdxds' {@link ObjectObjectMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectObjectOrderedMap} instead, since some optimizations here require Coord keys to be in
 * the pool.
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
 */
public class CoordObjectOrderedMap<V> extends ObjectObjectOrderedMap<Coord, V> implements ISerializersNeeded {
    public CoordObjectOrderedMap(OrderType type) {
        this(Coord.getCacheWidth() * Coord.getCacheHeight(), 0.9f, type);
    }

    public CoordObjectOrderedMap(int initialCapacity, OrderType type) {
        super(initialCapacity, 0.9f, type);
    }

    public CoordObjectOrderedMap(int initialCapacity, float loadFactor, OrderType type) {
        super(initialCapacity, loadFactor, type);
    }

    public CoordObjectOrderedMap(ObjectObjectOrderedMap<? extends Coord, ? extends V> map, OrderType type) {
        super(map, type);
    }

    public CoordObjectOrderedMap(Map<? extends Coord, ? extends V> map, OrderType type) {
        this(map.size(), type);
        putAll(map);
    }

    public CoordObjectOrderedMap(Coord[] keys, V[] values, OrderType type) {
        this(Math.min(keys.length, values.length), type);
        this.putAll(keys, values);
    }

    public CoordObjectOrderedMap(Collection<? extends Coord> keys, Collection<? extends V> values, OrderType type) {
        this(Math.min(keys.size(), values.size()), type);
        this.putAll(keys, values);
    }

    public CoordObjectOrderedMap() {
        this(Coord.getCacheWidth() * Coord.getCacheHeight(), 0.9f);
    }

    public CoordObjectOrderedMap(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordObjectOrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordObjectOrderedMap(ObjectObjectOrderedMap<? extends Coord, ? extends V> map) {
        super(map);
    }

    public CoordObjectOrderedMap(Map<? extends Coord, ? extends V> map) {
        this(map.size());
        putAll(map);
    }

    public CoordObjectOrderedMap(Coord[] keys, V[] values) {
        this(Math.min(keys.length, values.length));
        this.putAll(keys, values);
    }

    public CoordObjectOrderedMap(Collection<? extends Coord> keys, Collection<? extends V> values) {
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
     * Constructs an empty map given the types as generic type arguments.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     * <br>
     * This needs to be named differently because of shadowing issues with the parent class.
     *
     * @param <V>    the type of values
     * @return a new map containing nothing
     */
    public static <V> CoordObjectOrderedMap<V> withNothing () {
        return new CoordObjectOrderedMap<>(0);
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Object, Object...)}
     * when there's no "rest" of the keys or values.
     *
     * @param key0   the first and only key
     * @param value0 the first and only value
     * @param <V>    the type of value0
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static <V> CoordObjectOrderedMap<V> with (Coord key0, V value0) {
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(1);
        map.put(key0, value0);
        return map;
    }

    /**
     * Constructs a single-entry map given two key-value pairs.
     * This is mostly useful as an optimization for {@link #with(Object, Object, Object...)}
     * when there's no "rest" of the keys or values.
     *
     * @param key0   a Coord key
     * @param value0 a V value
     * @param key1   a Coord key
     * @param value1 a V value
     * @param <V>    the type of value0
     * @return a new map containing entries mapping each key to the following value
     */
    public static <V> CoordObjectOrderedMap<V> with (Coord key0, V value0, Coord key1, V value1) {
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(2);
        map.put(key0, value0);
        map.put(key1, value1);
        return map;
    }

    /**
     * Constructs a single-entry map given three key-value pairs.
     * This is mostly useful as an optimization for {@link #with(Object, Object, Object...)}
     * when there's no "rest" of the keys or values.
     *
     * @param key0   a Coord key
     * @param value0 a V value
     * @param key1   a Coord key
     * @param value1 a V value
     * @param key2   a Coord key
     * @param value2 a V value
     * @param <V>    the type of value0
     * @return a new map containing entries mapping each key to the following value
     */
    public static <V> CoordObjectOrderedMap<V> with (Coord key0, V value0, Coord key1, V value1, Coord key2, V value2) {
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(3);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    /**
     * Constructs a single-entry map given four key-value pairs.
     * This is mostly useful as an optimization for {@link #with(Object, Object, Object...)}
     * when there's no "rest" of the keys or values.
     *
     * @param key0   a Coord key
     * @param value0 a V value
     * @param key1   a Coord key
     * @param value1 a V value
     * @param key2   a Coord key
     * @param value2 a V value
     * @param key3   a Coord key
     * @param value3 a V value
     * @param <V>    the type of value0
     * @return a new map containing entries mapping each key to the following value
     */
    public static <V> CoordObjectOrderedMap<V> with (Coord key0, V value0, Coord key1, V value1, Coord key2, V value2, Coord key3, V value3) {
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(4);
        map.put(key0, value0);
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This can be useful in some code-generation scenarios, or when you want to make a
     * map conveniently by-hand and have it populated at the start. You can also use
     * {@link #CoordObjectOrderedMap(Coord[], Object[])}, which takes all keys and then all values.
     * This needs all keys to be Coord and all values to have the same type, because
     * it gets the value type from the first value parameter. Any keys that don't
     * have Coord as their type or values that don't have V as their type have that entry skipped.
     * @param key0 the first key; will be used to determine the type of all keys
     * @param value0 the first value; will be used to determine the type of all values
     * @param rest an array or varargs of alternating Coord, V, Coord, V... elements
     * @param <V> the type of values, inferred from value0
     * @return a new map containing the given keys and values
     */
    @SuppressWarnings("unchecked")
    public static <V> CoordObjectOrderedMap<V> with(Coord key0, V value0, Object... rest){
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(1 + (rest.length >>> 1));
        map.put(key0, value0);
        map.putPairs(rest);
        return map;
    }

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Arrays.asList(Coord.class, defaultValue.getClass());
    }
}
