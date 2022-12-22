/*
 * Copyright (c) 2022 See AUTHORS file.
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
import com.github.tommyettinger.ds.annotations.NotNullDefault;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectOrderedSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold items placed into here, you
 * should use a normal {@link ObjectOrderedSet} instead, since some optimizations here require Coord items to be in the
 * pool.
 */
@NotNullDefault
public class CoordOrderedSet extends ObjectOrderedSet<Coord> {
    public CoordOrderedSet() {
        super();
    }

    public CoordOrderedSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordOrderedSet(int initialCapacity) {
        super(initialCapacity);
    }

    public CoordOrderedSet(ObjectOrderedSet<? extends Coord> set) {
        super(set);
    }

    public CoordOrderedSet(Collection<? extends Coord> coll) {
        super(coll);
    }

    public CoordOrderedSet(Coord[] array, int offset, int length) {
        super(array, offset, length);
    }

    public CoordOrderedSet(Coord[] items) {
        super(items);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), currently Rosenberg-Strong
    }

    @Override
    protected boolean equate(Object left, @Nullable Object right) {
        return left == right;
    }

    public static CoordOrderedSet with(Coord item) {
        CoordOrderedSet set = new CoordOrderedSet(1);
        set.add(item);
        return set;
    }

    public static CoordOrderedSet with (Coord... array) {
        return new CoordOrderedSet(array);
    }
}
