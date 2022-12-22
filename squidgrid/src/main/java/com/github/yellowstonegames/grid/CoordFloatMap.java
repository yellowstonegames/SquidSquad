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

import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.tommyettinger.ds.annotations.NotNullDefault;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A variant on jdkgdxds' {@link ObjectFloatMap} class that only uses Coord keys, and can do so more efficiently.
 * This assumes all Coord keys are in the Coord pool; that is, {@link Coord#expandPoolTo(int, int)} has been called with
 * the maximum values for Coord x and y. If you cannot be sure that the Coord pool will hold keys placed into here, you
 * should use a normal {@link ObjectFloatMap} instead, since some optimizations here require Coord keys to be in the
 * pool.
 * <br>
 * You can create a CoordFloatMap with {@link #fromArray2D(float[][], float, float)} if you have a 2D float array and
 * want to get the positions within some range.
 */
@NotNullDefault
public class CoordFloatMap extends ObjectFloatMap<Coord> {
    public CoordFloatMap() {
        super();
    }

    public CoordFloatMap(int initialCapacity) {
        super(initialCapacity);
    }

    public CoordFloatMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public CoordFloatMap(ObjectFloatMap<? extends Coord> map) {
        super(map);
    }

    public CoordFloatMap(Coord[] keys, float[] values) {
        super(keys, values);
    }

    public CoordFloatMap(Collection<? extends Coord> keys, PrimitiveCollection.OfFloat values) {
        super(keys, values);
    }

    @Override
    protected int place(final Object item) {
        return item.hashCode() & mask; // Uses default Coord hashCode(), currently Rosenberg-Strong
    }

    @Override
    protected boolean equate(Object left, @Nullable Object right) {
        return left == right;
    }

    /**
     * Constructs a single-entry map given one key and one value.
     * This is mostly useful as an optimization for {@link #with(Coord, Number, Object...)}
     * when there's no "rest" of the keys or values. Like the more-argument with(), this will
     * convert its Number value to a primitive float, regardless of which Number type was used.
     *
     * @param key0   the first and only key; always a Coord
     * @param value0 the first and only value; will be converted to primitive float
     * @return a new map containing just the entry mapping key0 to value0
     */
    public static CoordFloatMap with (Coord key0, Number value0) {
        CoordFloatMap map = new CoordFloatMap(1);
        map.put(key0, value0.floatValue());
        return map;
    }

    /**
     * Constructs a map given alternating keys and values.
     * This can be useful in some code-generation scenarios, or when you want to make a
     * map conveniently by-hand and have it populated at the start. You can also use
     * {@link #CoordFloatMap(Coord[], float[])}, which takes all keys and then all values.
     * All values must be some type of boxed Number, such as {@link Integer}
     * or {@link Double}, and will be converted to primitive {@code float}s. Any keys that don't
     * have Coord as their type or values that aren't {@code Number}s have that entry skipped.
     *
     * @param key0   the first key; always a Coord
     * @param value0 the first value; will be converted to primitive float
     * @param rest   an array or varargs of alternating Coord, Number, Coord, Number... elements
     * @return a new map containing the given keys and values
     */
    public static CoordFloatMap with (Coord key0, Number value0, Object... rest) {
        CoordFloatMap map = new CoordFloatMap(1 + (rest.length >>> 1));
        map.put(key0, value0.floatValue());
        for (int i = 1; i < rest.length; i += 2) {
            try {
                map.put((Coord) rest[i - 1], ((Number)rest[i]).floatValue());
            } catch (ClassCastException ignored) {
            }
        }
        return map;
    }

    /**
     * Given a 2D float array (which should usually be rectangular), this finds the positions with values between min
     * (inclusive) and max (also inclusive), and places them in a newly-allocated CoordFloatMap. If you need to
     * get a sorted ordering, use {@link CoordFloatOrderedMap#fromArray2D(float[][], float, float)} and sort the result.
     * @param array a usually-rectangular 2D float array
     * @param min the inclusive minimum value
     * @param max the inclusive maximum value
     * @return a new CoordFloatMap containing all positions with in-range values, mapped to those values.
     */
    public static CoordFloatMap fromArray2D(float[][] array, float min, float max) {
        final int width = array.length;
        CoordFloatMap map = new CoordFloatMap(width * array[0].length);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < array[x].length; y++) {
                final float f = array[x][y];
                if (f >= min && f <= max) {
                    map.put(Coord.get(x, y), f);
                }
            }
        }
        return map;
    }

}
