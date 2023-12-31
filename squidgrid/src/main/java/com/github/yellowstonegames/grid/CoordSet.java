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

import com.github.tommyettinger.ds.ObjectSet;

import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold items placed into here, you
 * should use a normal {@link ObjectSet} instead, since some optimizations here require Coord items to be in the pool.
 * This does much better if all Coord items use non-negative x and y values.
 */
public class CoordSet extends ObjectSet<Coord> {
    public CoordSet() {
        super();
    }

    public CoordSet(int initialCapacity) {
        super(initialCapacity);
    }

    public CoordSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordSet(ObjectSet<? extends Coord> set) {
        super(set);
    }

    public CoordSet(Collection<? extends Coord> coll) {
        super(coll);
    }

    public CoordSet(Coord[] array, int offset, int length) {
        super(array, offset, length);
    }

    public CoordSet(Coord[] array) {
        super(array);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), precalculated
    }

    @Override
    protected boolean equate(Object left, @Nullable Object right) {
        return left == right;
    }

    public static CoordSet with(Coord item) {
        CoordSet set = new CoordSet(1);
        set.add(item);
        return set;
    }

    public static CoordSet with (Coord... array) {
        return new CoordSet(array);
    }
}
