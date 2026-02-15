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

import com.github.tommyettinger.ds.ObjectOrderedSet;

import com.github.tommyettinger.ds.OrderType;
import com.github.tommyettinger.ds.Utilities;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A variant on jdkgdxds' {@link ObjectOrderedSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold items placed into here, you
 * should use a normal {@link ObjectOrderedSet} instead, since some optimizations here require Coord items to be in the
 * pool.
 * <br>
 * If no initialCapacity is supplied, or if this must resize to enter a Coord, this will use a capacity at least as
 * large as the Coord cache, as defined by {@link Coord#getCacheWidth()} by {@link Coord#getCacheHeight()}. While this
 * means that any resizing will potentially make this use much more memory, it avoids a situation where some dense key
 * sets could take hundreds of times longer than they should. It also usually doesn't use drastically more memory unless
 * the Coord pool has been expanded quite a bit. If the Coord pool hasn't been expanded, each set should use about 1MB
 * of memory or less when created with the default constructor, plus size proportional to {@link #size()} for the order.
 * <br>
 * This tends to perform significantly better with a high low factor, such as 0.9f, instead of a lower one like 0.5f .
 * It also performs its best when the initial capacity is sufficient to hold every item this needs without resizing, but
 * it typically only has to resize once if it has to resize at all.
 */
public class CoordOrderedSet extends ObjectOrderedSet<Coord> implements ISerializersNeeded {
    public CoordOrderedSet(OrderType type) {
        this(Coord.getCacheWidth() * Coord.getCacheHeight(), 0.9f, type);
    }

    public CoordOrderedSet(int initialCapacity, float loadFactor, OrderType type) {
        super(initialCapacity, loadFactor, type);
    }

    public CoordOrderedSet(int initialCapacity, OrderType type) {
        super(initialCapacity, 0.9f, type);
    }

    public CoordOrderedSet(ObjectOrderedSet<? extends Coord> set, OrderType type) {
        super(set, type);
    }

    public CoordOrderedSet(Collection<? extends Coord> coll, OrderType type) {
        this(coll.size(), type);
        this.addAll(coll);
    }

    public CoordOrderedSet(Coord[] array, int offset, int length, OrderType type) {
        this(length, type);
        this.addAll(array, offset, length);
    }

    public CoordOrderedSet(Coord[] items, OrderType type) {
        this(items.length, type);
        this.addAll(items);
    }
    public CoordOrderedSet() {
        this(51, 0.9f);
    }

    public CoordOrderedSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordOrderedSet(int initialCapacity) {
        super(initialCapacity, 0.9f);
    }

    public CoordOrderedSet(ObjectOrderedSet<? extends Coord> set) {
        super(set);
    }

    public CoordOrderedSet(Collection<? extends Coord> coll) {
        this(coll.size());
        this.addAll(coll);
    }

    public CoordOrderedSet(Coord[] array, int offset, int length) {
        this(length);
        this.addAll(array, offset, length);
    }

    public CoordOrderedSet(Coord[] items) {
        this(items.length);
        this.addAll(items);
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
     * Constructs an empty set given the type as a generic type argument.
     * This is usually less useful than just using the constructor, but can be handy
     * in some code-generation scenarios when you don't know how many arguments you will have.
     *
     * @return a new set containing nothing
     */
    public static CoordOrderedSet with () {
        return new CoordOrderedSet(0);
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given item, but can be resized.
     * @param item one Coord item
     * @return a new CoordOrderedSet that holds the given item
     */
    public static CoordOrderedSet with (Coord item) {
        CoordOrderedSet set = new CoordOrderedSet(1);
        set.add(item);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1) {
        CoordOrderedSet set = new CoordOrderedSet(2);
        set.add(item0, item1);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2) {
        CoordOrderedSet set = new CoordOrderedSet(3);
        set.add(item0, item1, item2);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2, Coord item3) {
        CoordOrderedSet set = new CoordOrderedSet(4);
        set.add(item0, item1, item2, item3);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4) {
        CoordOrderedSet set = new CoordOrderedSet(5);
        set.add(item0, item1, item2, item3);
        set.add(item4);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5) {
        CoordOrderedSet set = new CoordOrderedSet(6);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6) {
        CoordOrderedSet set = new CoordOrderedSet(7);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * @param item0 a Coord item
     * @param item1 a Coord item
     * @param item2 a Coord item
     * @param item3 a Coord item
     * @param item4 a Coord item
     * @param item5 a Coord item
     * @param item6 a Coord item
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord item0, Coord item1, Coord item2, Coord item3, Coord item4, Coord item5, Coord item6, Coord item7) {
        CoordOrderedSet set = new CoordOrderedSet(8);
        set.add(item0, item1, item2, item3);
        set.add(item4, item5, item6, item7);
        return set;
    }

    /**
     * Creates a new CoordOrderedSet that holds only the given items, but can be resized.
     * This overload will only be used when an array is supplied and the type of the
     * items requested is the component type of the array, or if varargs are used and
     * there are 9 or more arguments.
     * @param varargs a Coord varargs or Coord array; remember that varargs allocate
     * @return a new CoordOrderedSet that holds the given items
     */
    public static CoordOrderedSet with (Coord... varargs) {
        return new CoordOrderedSet(varargs);
    }

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Collections.singletonList(Coord.class);
    }
}
