package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.ds.annotations.NotNullDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectOrderedSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y.
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
    protected int place(Object item) {
        final int x = ((Coord)item).x, y = ((Coord)item).y;
        return y + ((x + y) * (x + y + 1) >> 1) & mask;
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
