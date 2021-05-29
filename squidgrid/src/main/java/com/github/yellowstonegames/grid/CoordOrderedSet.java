package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.ObjectSet;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectOrderedSet} class that only holds Coord items, and can do so more efficiently.
 */
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
    protected int place(@Nonnull Object item) {
        final int x = ((Coord)item).x, y = ((Coord)item).y;
        return y + ((x + y) * (x + y + 1) >> 1) & mask;
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
