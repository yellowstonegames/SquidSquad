package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectSet;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectSet} class that only holds Coord items, and can do so more efficiently.
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
    protected int place(@Nonnull Object item) {
        final int x = ((Coord)item).x, y = ((Coord)item).y;
        return y + ((x + y) * (x + y + 1) >> 1) & mask;
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
