package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.ds.annotations.NotNullDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectSet} class that only holds Coord items, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y.
 */
@NotNullDefault
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
    protected int place(Object item) {
        final int x = ((Coord)item).x, y = ((Coord)item).y;
        return y + ((x + y) * (x + y + 1) >> 1) & mask;
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
