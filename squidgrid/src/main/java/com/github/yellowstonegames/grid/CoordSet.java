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

import com.github.tommyettinger.ds.ObjectSet;

import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold items placed into here, you
 * should use a normal {@link ObjectSet} instead, since some optimizations here require Coord items to be in the pool.
 */
public class CoordSet extends ObjectSet<Coord> {
    public CoordSet() {
        this(51, 0.9f);
    }

    public CoordSet(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordSet(ObjectSet<? extends Coord> set) {
        super(set);
    }

    public CoordSet(Collection<? extends Coord> coll) {
        this(coll.size());
        this.addAll(coll);
    }

    public CoordSet(Coord[] array, int offset, int length) {
        this(length);
        this.addAll(array, offset, length);
    }

    public CoordSet(Coord[] array) {
        this(array, 0, array.length);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), precalculated
    }

    @Override
    protected boolean equate(Object left, Object right) {
        return left == right;
    }
    /**
     * Constructs an empty set given the type as a generic type argument.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     *
     * @return a new set containing nothing
     */
    public static CoordSet with () {
        return new CoordSet(0);
    }

    /**
     * Creates a new CoordSet that holds only the given item, but can be resized.
     * @param item one Coord item
     * @return a new CoordSet that holds the given item
     */
    public static CoordSet with (Coord item) {
        CoordSet set = new CoordSet(1);
        set.add(item);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1) {
        CoordSet set = new CoordSet(2);
        set.add(item0, item1);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2) {
        CoordSet set = new CoordSet(3);
        set.add(item0, item1, item2);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2, Coord item3) {
        CoordSet set = new CoordSet(4);
        set.add(item0, item1, item2, item3);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4) {
        CoordSet set = new CoordSet(5);
        set.add(item0, item1, item2, item3);
        set.add(item4);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5) {
        CoordSet set = new CoordSet(6);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6) {
        CoordSet set = new CoordSet(7);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordSet that holds the given items
     */
    public static CoordSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6, Coord item7) {
        CoordSet set = new CoordSet(8);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6, item7);
        return set;
    }

    /**
     * Creates a new CoordSet that holds only the given items, but can be resized.
     * This overload will only be used when an array is supplied and the type of the
     * items requested is the component type of the array, or if varargs are used and
     * there are 9 or more arguments.
     * @param varargs a Coord varargs or Coord array; remember that varargs allocate
     * @return a new CoordSet that holds the given items
     */
    @SafeVarargs
    public static CoordSet with (Coord... varargs) {
        return new CoordSet(varargs);
    }
}
