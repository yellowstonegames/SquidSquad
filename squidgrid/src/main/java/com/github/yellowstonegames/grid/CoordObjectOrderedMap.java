package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * A variant on jdkgdxds' {@link ObjectObjectMap} class that only uses Coord keys, and can do so more efficiently.
 */
public class CoordObjectOrderedMap<V> extends ObjectObjectOrderedMap<Coord, V> {
    public CoordObjectOrderedMap() {
        super();
    }

    public CoordObjectOrderedMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CoordObjectOrderedMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordObjectOrderedMap(ObjectObjectOrderedMap<? extends Coord, ? extends V> map) {
        super(map);
    }

    public CoordObjectOrderedMap(Map<? extends Coord, ? extends V> map) {
        super(map);
    }

    public CoordObjectOrderedMap(Coord[] keys, V[] values) {
        super(keys, values);
    }

    public CoordObjectOrderedMap(Collection<? extends Coord> keys, Collection<? extends V> values) {
        super(keys, values);
    }

    @Override
    protected int place(@Nonnull Object item) {
        final int x = ((Coord)item).x, y = ((Coord)item).y;
        return y + ((x + y) * (x + y + 1) >> 1) & mask;
    }
    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Object, Object, Object...)}
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
     * This needs all keys to have the same type and all values to have the same type, because
     * it gets those types from the first key parameter and first value parameter. Any keys that don't
     * have K as their type or values that don't have V as their type have that entry skipped.
     * @param key0 the first key; will be used to determine the type of all keys
     * @param value0 the first value; will be used to determine the type of all values
     * @param rest an array or varargs of alternating K, V, K, V... elements
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
