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
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;

import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * A variant on jdkgdxds' {@link ObjectObjectMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectObjectOrderedMap} instead, since some optimizations here require Coord keys to be in
 * the pool. This does much better if all Coord keys use non-negative x and y values.
 */
public class CoordObjectOrderedMap<V> extends ObjectObjectOrderedMap<Coord, V> {
    public CoordObjectOrderedMap() {
        this(51, 0.9f);
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
    protected boolean equate(Object left, @Nullable Object right) {
        return left == right;
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Object, Object...)}
     * when there's no "rest" of the keys or values.
     * @param key0 the first and only key
     * @param value0 the first and only value
     * @param <V> the type of value0
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static <V> CoordObjectOrderedMap<V> with(Coord key0, V value0) {
        CoordObjectOrderedMap<V> map = new CoordObjectOrderedMap<>(1);
        map.put(key0, value0);
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
        for (int i = 1; i < rest.length; i += 2) {
            try {
                map.put((Coord) rest[i - 1], (V)rest[i]);
            }catch (ClassCastException ignored){
            }
        }
        return map;
    }

}
