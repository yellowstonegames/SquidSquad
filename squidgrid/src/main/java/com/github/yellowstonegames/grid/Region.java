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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.DigitTools;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.LZSEncoding;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Region encoding of on/off information about areas using bitsets; uncompressed but fast at bulk operations.
 * This can handle any size of 2D data, but it's almost always best for the Coord pool to be at least as large
 * as the max dimensions of a Region (use {@link Coord#expandPoolTo(int, int)} to ensure this). It stores several long
 * arrays and uses each bit in one of those numbers to represent a single point, though sometimes this does waste bits
 * if the height of the area this encodes is not a multiple of 64 (if you store a 80x64 map, this uses 80 longs; if you
 * store an 80x65 map, this uses 160 longs, 80 for the first 64 rows and 80 more to store the next row). It's very
 * fast at certain bulk operations (anything that expands or retracts an area, including {@link #expand()},
 * {@link #retract()}, {@link #fringe()}, {@link #surface()}, and {@link #flood(Region)}, and maintains the high speed
 * at "bitwise-like" operations that bitsets typically have (for example, on {@link #and(Region)}, {@link #or(Region)},
 * and {@link #xor(Region)}).
 * <br>
 * Each Region is mutable, and instance methods typically modify that instance and return it for chaining. There
 * are exceptions, usually where multiple Region values are returned and the instance is not modified.
 * <br>
 * Typical usage involves constructing a Region from some input data, like a char[][] for a map or a float[][]
 * from DijkstraMap, and modifying it spatially with expand(), retract(), flood(), etc. It's common to mix in data from
 * other Regions with and() (which gets the intersection of two Regions and stores it in one), or() (which
 * is like and() but for the union), xor() (like and() but for exclusive or, finding only cells that are on in exactly
 * one of the two Regions), and andNot() (which can be considered the "subtract another region from me" method).
 * There are 8-way (Chebyshev distance) variants on all of the spatial methods, and methods without "8way" in the name
 * are either 4-way (Manhattan distance) or not affected by distance measurement. There's really quite a lot of methods
 * here that modify a Region, so here's a partial list:
 * <ul>
 *     <li>{@link #expand()} changes any cells next to a currently "on" cell to also be "on." It's 4-way, and has an
 *     8-way variant {@link #expand8way()}. There's an overload that expands several times as if by a loop,
 *     {@link #expand(int)}, which also has an 8-way variant. You can use {@link #expandSeries(int)} to get multiple
 *     Regions produced as intermediate values of a series of expansions; this also has an 8-way variant.
 *     {@link #expandSeriesToLimit()} returns an ObjectList of as many Regions as it takes to expand until no more
 *     cells can change.</li>
 *     <li>{@link #retract()} is just like {@link #expand()}, but changes any "on" cells that are next to an "off" cell
 *     to also be "off." It can be thought of as expanding the "off" cells, with the subtle difference that the edges
 *     are also considered "off" here. All of the above variants of {@link #expand()} listed above have equivalents for
 *     {@link #retract()}, like {@link #retractSeriesToLimit()}.</li>
 *     <li>{@link #fringe()} is like {@link #expand()}, but doesn't keep the old contents of the Region, only the
 *     cells that are added. This has some differences in how {@link #fringe(int)} works (it returns a
 *     multiple-cell-thick fringe section, still not containing any of the original), and how {@link #fringeSeries(int)}
 *     works (it returns many single-cell-thick fringe sections). There's a {@link #fringeSeriesToLimit()}, too.</li>
 *     <li>{@link #surface()} is something like removing the result of {@link #retract()} from this Region; it
 *     gets only those "on" cells that are next to an "off" cell. Like with {@link #fringe()}, the variants are a little
 *     different; {@link #surface(int)} gets a multiple-cell deep surface, and {@link #surfaceSeries(int)} gets a series
 *     of single-cell deep rings from further and further inside the original Region. Yes, there's also a
 *     {@link #surfaceSeriesToLimit()}.</li>
 *     <li>{@link #flood(Region)} is useful; it acts like {@link #expand()}, but won't change any cells unless
 *     they are "on" in its {@code bounds} argument, another Region. There's also
 *     {@link #flood(Region, int)}, which may be most useful with a very large {@code amount} parameter to fill
 *     up the bounds completely with whatever the original Region could reach. {@link #flood8way(Region)},
 *     {@link #floodSeries(Region, int)}, and {@link #floodSeriesToLimit(Region)} are all here, too.</li>
 *     <li>{@link #spill(Region, int, EnhancedRandom)} is like calling {@link #flood(Region)} many times, but only
 *     expanding one cell on the edge each time, randomly choosing it. As long as {@code volume} is not enough to fully
 *     fill the reachable part of {@code bounds}, the filled area will be random but always connected.</li>
 *     <li>Various random modifications: {@link #randomRegion(EnhancedRandom, int)} simply chooses "on" points from this
 *     Region until it reaches {@code size}, {@link #deteriorate(EnhancedRandom, float)} randomly removes
 *     points but stops when the fraction of cells remaining is equal to {@code preservation}, {@link #fray(float)}
 *     acts like {@link #deteriorate(EnhancedRandom, float)} but only affects the surface (what {@link #surface()}
 *     would return), and {@link #disperseRandom(EnhancedRandom)} randomly removes one of each pair of "on" cells.
 *     </li>
 *     <li>Various quasi-random modifications: Mostly you should use {@link #separatedRegionBlue(float)} if you want
 *     to get an approximate fraction of well-separated "on" cells from a Region. {@link #fray(float)} also has a quasi-random version that
 *     doesn't use a RandomnessSource.</li>
 * </ul>
 * <br>
 * Once you have a Region, you may want to:
 * <ul>
 *     <li>get a single random point from it (use {@link #singleRandom(EnhancedRandom)}),</li>
 *     <li>get several random points from it with random sampling (use {@link #randomPortion(EnhancedRandom, int)}),</li>
 *     <li>mutate the current Region to keep random points from it with random sampling (use {@link #randomRegion(EnhancedRandom, int)}),</li>
 *     <li>get random points that are likely to be separated (use {@link #randomScatter(EnhancedRandom, int)} on a copy, or
 *     {@link #separatedBlue(float)} if you don't want a random aspect),</li>
 *     <li>do what any of the above "separated" methods can do, but mutate the current Region (use
 *     {@link #separatedRegionBlue(float)} ),</li>
 *     <li>get all points from it (use {@link #asCoords()} to get a Coord array, or produce a 2D array of the contents
 *     with {@link #decode()} or {@link #toChars(char, char)}),</li>
 *     <li>use it to modify other 2D data, such as with {@link #writeCharsToOff(char[][], char)},
 *     {@link #writeChars(char[][], char)}, {@link #writeFloats(float[][], float)}, or
 *     {@link #writeIntsInto(int[][], int)}, along with variations on those.</li>
 * </ul>
 * <br>
 * You may also want to produce some 2D data from one or more Regions, as with {@link #sum(Region...)} or
 * {@link #toChars()}. The most effective techniques regarding Region involve multiple methods, like getting a
 * few random points from an existing Region representing floor tiles in a dungeon with
 * {@link #randomRegion(EnhancedRandom, int)}, then finding a random expansion of those initial points with
 * {@link #spill(Region, int, EnhancedRandom)}, giving the original Region of floor tiles as the first argument.
 * This could be used to position puddles of water or toxic waste in a dungeon level, while still keeping the starting
 * points and finished points within the boundaries of valid (floor) cells. If you wanted to place something like mold
 * that can be on floors or on cells immediately adjacent to floors (like walls), you could call {@link #expand()} on
 * the floor tiles before calling spill, allowing the spill to spread onto non-floor cells that are next to floors.
 * <br>
 * For efficiency, you can place one Region into another (typically a temporary value that is no longer needed
 * and can be recycled) using {@link #remake(Region)}, or give the information that would normally be used to
 * construct a fresh Region to an existing one of the same dimensions with {@link #refill(boolean[][])} or any
 * of the overloads of refill(). These re-methods don't do as much work as a constructor does if the width and height
 * of their argument are identical to their current width and height, and don't create more garbage for the GC.
 */
public class Region implements Collection<Coord> {
    public long[] data;
    public int height;
    public int width;
    private int ySections;
    private long yEndMask;
    private boolean tallied;
    private int ct;
    private int[] counts;

    /**
     * A 256-element array of {@link Region}s, each 64x64, where the first Region has an impossibly strict
     * threshold on what points to include (it is empty), but the second has some points far apart, the third has more,
     * and so on until the last element includes almost all points.
     */
    public static final Region[] BLUE_LEVELS = new Region[256];
    static {
        BLUE_LEVELS[0] = new Region(64, 64);
        for (int i = -127; i < 128; i++) {
            BLUE_LEVELS[i + 128] = new Region(BlueNoise.RAW_2D, -128, i);
        }
    }

    /**
     * Constructs an empty 4x64 Region.
     * Regions are mutable, so you can add to this with insert() or insertSeveral(), among others.
     * You can also resize this and clear it with {@link #resizeAndEmpty(int, int)}, or replace its
     * contents with those of another Region with {@link #remake(Region)}.
     */
    public Region()
    {
        width = 4;
        height = 64;
        ySections = 1;
        yEndMask = -1L;
        data = new long[4];
        counts = new int[4];
        ct = 0;
        tallied = true;
    }

    /**
     * Constructs a Region with the given rectangular boolean array, with width of bits.length and height of
     * bits[0].length, any value of true considered "on", and any value of false considered "off."
     * @param bits a rectangular 2D boolean array where true is on and false is off
     */
    public Region(final boolean[][] bits)
    {
        width = bits.length;
        height = bits[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0, xs = 0; x < width; x++, xs += ySections) {
            for (int y = 0; y < height; y++) {
                if(bits[x][y]) data[xs + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular boolean array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value of true in map is considered "on", and any value of false in map is
     * considered "off."
     * @param map a rectangular 2D boolean array where true is on and false is off
     * @return this for chaining
     */
    public Region refill(final boolean[][] map) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= (map[x][y] ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs a Region with the given rectangular char array, with width of map.length and height of
     * map[0].length, any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     */
    public Region(final char[][] map, final char yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular char array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     * @return this for chaining
     */
    public Region refill(final char[][] map, final char yes) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs a Region with the given rectangular char array, with width of map.length and height of
     * map[0].length, any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     */
    public Region(final char[][] map, final char[] yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for(char e : yes)
                {
                    if(map[x][y] == e) {
                        data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                        break;
                    }
                }
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular char array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D char array where yes is on and everything else is off
     * @param yes which char to encode as "on"
     * @return this for chaining
     */
    public Region refill(final char[][] map, final char[] yes) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for(char e : yes)
                    {
                        if(map[x][y] == e) {
                            data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                            break;
                        }
                    }
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for(char e : yes)
                    {
                        if(map[x][y] == e) {
                            data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                            break;
                        }
                    }
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Weird constructor that takes a String array, _as it would be printed_, so each String is a row and indexing would
     * be done with y, x instead of the normal x, y.
     * @param map String array (as printed, not the normal storage) where each String is a row
     * @param yes the char to consider "on" in the Region
     */
    public Region(final String[] map, final char yes)
    {
        height = map.length;
        width = map[0].length();
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[y].charAt(x) == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Weird refill method that takes a String array, _as it would be printed_, so each String is a row and indexing
     * would be done with y, x instead of the normal x, y.
     * @param map String array (as printed, not the normal storage) where each String is a row
     * @param yes the char to consider "on" in the Region
     * @return this for chaining
     */
    public Region refill(final String[] map, final char yes) {
        if (map != null && map.length > 0 && height == map.length && width == map[0].length()) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[y].charAt(x) == yes) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            height = (map == null) ? 0 : map.length;
            width = (map == null || map.length <= 0) ? 0 : map[0].length();
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[y].charAt(y) == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }
    /**
     * Constructs a Region with the given rectangular int array, with width of map.length and height of
     * map[0].length, any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D int array where an int == yes is on and everything else is off
     * @param yes which int to encode as "on"
     */
    public Region(final int[][] map, final int yes)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular int array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then any value that equals yes is considered "on", and any other value considered "off."
     * @param map a rectangular 2D int array where an int == yes is on and everything else is off
     * @param yes which int to encode as "on"
     * @return this for chaining
     */
    public Region refill(final int[][] map, final int yes) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((map[x][y] == yes) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] == yes) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs this Region using an int[][], treating cells as on if they are greater than or equal to lower
     * and less than upper, or off otherwise.
     * @param map an int[][] that should have some ints between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     */
    public Region(final int[][] map, final int lower, final int upper)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        int[] column;
        for (int x = 0; x < width; x++) {
            column = map[x];
            for (int y = 0; y < height; y++) {
                if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Reassigns this Region with the given rectangular int array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D int array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public Region refill(final int[][] map, final int lower, final int upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            int[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            int[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs this Region using a byte[][], treating cells as on if they are greater than or equal to lower
     * and less than upper, or off otherwise.
     * @param map a byte[][] that should have some bytes between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     */
    public Region(final byte[][] map, final int lower, final int upper)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        byte[] column;
        for (int x = 0; x < width; x++) {
            column = map[x];
            for (int y = 0; y < height; y++) {
                if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Reassigns this Region with the given rectangular byte array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D byte array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public Region refill(final byte[][] map, final int lower, final int upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            byte[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            byte[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }


    /**
     * Constructs this Region using a short[][], treating cells as on if they are greater than or equal to lower
     * and less than upper, or off otherwise.
     * @param map a short[][] that should have some shorts between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     */
    public Region(final short[][] map, final int lower, final int upper)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        short[] column;
        for (int x = 0; x < width; x++) {
            column = map[x];
            for (int y = 0; y < height; y++) {
                if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Reassigns this Region with the given rectangular short array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D short array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public Region refill(final short[][] map, final int lower, final int upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            short[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            short[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }
    /**
     * Constructs this Region using a float[][] (typically one generated by
     * DijkstraMap) that only stores two relevant states:  an "on" state for values less than
     * or equal to upperBound (inclusive), and an "off" state for anything else.
     * @param map a float[][] that probably relates in some way to DijkstraMap.
     * @param upperBound upper inclusive; any float greater than this will be off, any others will be on
     */
    public Region(final float[][] map, final float upperBound)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] <= upperBound)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular float array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are less than or equal to upperBound, or off
     * otherwise.
     * @param map a rectangular 2D float array that should usually have some values less than or equal to upperBound
     * @param upperBound upper bound, inclusive; all on cells will have values in map that are less than or equal to this
     * @return this for chaining
     */
    public Region refill(final float[][] map, final float upperBound) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] <= upperBound) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if(map[x][y] <= upperBound) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs this Region using a float[][] (typically one generated by
     * DijkstraMap) that only stores two relevant states:  an "on" state for values between
     * lowerBound (inclusive) and upperBound (exclusive), and an "off" state for anything else.
     * @param map a float[][] that probably relates in some way to DijkstraMap.
     * @param lowerBound lower inclusive; any float lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any float greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     */
    public Region(final float[][] map, final float lowerBound, final float upperBound)
    {
        width = map.length;
        height = map[0].length;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] >= lowerBound && map[x][y] < upperBound)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular float array, reusing the current data storage (without
     * extra allocations) if this.width == map.length and this.height == map[0].length. The current values stored in
     * this are always cleared, then cells are treated as on if they are greater than or equal to lower and less than
     * upper, or off otherwise.
     * @param map a rectangular 2D float array that should have some values between lower and upper
     * @param lower lower bound, inclusive; all on cells will have values in map that are at least equal to lower
     * @param upper upper bound, exclusive; all on cells will have values in map that are less than upper
     * @return this for chaining
     */
    public Region refill(final float[][] map, final float lower, final float upper) {
        if (map != null && map.length > 0 && width == map.length && height == map[0].length) {
            Arrays.fill(data, 0L);
            float[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    data[x * ySections + (y >> 6)] |= ((column[y] >= lower && column[y] < upper) ? 1L : 0L) << (y & 63);
                }
            }
            tallied = false;
            return this;
        } else {
            width = (map == null) ? 0 : map.length;
            height = (map == null || map.length <= 0) ? 0 : map[0].length;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            float[] column;
            for (int x = 0; x < width; x++) {
                column = map[x];
                for (int y = 0; y < height; y++) {
                    if(column[y] >= lower && column[y] < upper) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs this Region using a float[][] that only stores two relevant states: an "on" state for values
     * between lowerBound (inclusive) and upperBound (exclusive), and an "off" state for anything else. This variant
     * scales the input so each "on" position in map produces a 2x2 on area if scale is 2, a 3x3 area if scale is 3, and
     * so on.
     * @param map a float[][]; depending on scale, the Region may have different width and height
     * @param lowerBound lower inclusive; any float lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any float greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     * @param scale      the size of the square of cells in this that each "on" value in map will correspond to
     */
    public Region(final float[][] map, final float lowerBound, final float upperBound, int scale)
    {
        scale = Math.min(63, Math.max(1, scale));
        int baseWidth = map.length, baseHeight = map[0].length;
        width =  baseWidth * scale;
        height = baseHeight * scale;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        long shape = (1L << scale) - 1L, leftover;
        for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
            for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                if(map[bx][by] >= lowerBound && map[bx][by] < upperBound) {
                    for (int i = 0; i < scale; i++) {
                        data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                        if((leftover = (y + scale - 1 & 63) + 1) < (y & 63) + 1 && (y + leftover >> 6) < ySections)
                        {
                            data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                        }
                    }
                }
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given rectangular float array, reusing the current data storage (without
     * extra allocations) if {@code this.width == map.length * scale && this.height == map[0].length * scale}. The
     * current values stored in this are always cleared, then cells are treated as on if they are greater than or equal
     * to lower and less than upper, or off otherwise, before considering scaling. This variant scales the input so each
     * "on" position in map produces a 2x2 on area if scale is 2, a 3x3 area if scale is 3, and so on.
     * @param map a float[][]; depending on scale, the Region may have different width and height
     * @param lowerBound lower inclusive; any float lower than this will be off, any equal to or greater than this,
     *                   but less than upper, will be on
     * @param upperBound upper exclusive; any float greater than or equal to this this will be off, any doubles both
     *                   less than this and equal to or greater than lower will be on
     * @param scale      the size of the square of cells in this that each "on" value in map will correspond to
     * @return this for chaining
     */
    public Region refill(final float[][] map, final float lowerBound, final float upperBound, int scale) {
        scale = Math.min(63, Math.max(1, scale));
        if (map != null && map.length > 0 && width == map.length * scale && height == map[0].length * scale) {
            Arrays.fill(data, 0L);
            float[] column;
            int baseWidth = map.length, baseHeight = map[0].length;
            long shape = (1L << scale) - 1L, leftover;
            for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
                column = map[bx];
                for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                    if(column[by] >= lowerBound && column[by] < upperBound) {
                        for (int i = 0; i < scale; i++) {
                            data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                            if((leftover = (y + scale - 1 & 63) + 1) < (y & 63) + 1 && (y + leftover >> 6) < ySections)
                            {
                                data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                            }
                        }
                    }
                }
            }
            tallied = false;
            return this;
        } else {
            int baseWidth = (map == null) ? 0 : map.length,
                    baseHeight = (map == null || map.length <= 0) ? 0 : map[0].length;
            width =  baseWidth * scale;
            height = baseHeight * scale;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            long shape = (1L << scale) - 1L, leftover;
            for (int bx = 0, x = 0; bx < baseWidth; bx++, x += scale) {
                for (int by = 0, y = 0; by < baseHeight; by++, y += scale) {
                    if(map[bx][by] >= lowerBound && map[bx][by] < upperBound) {
                        for (int i = 0; i < scale; i++) {
                            data[(x + i) * ySections + (y >> 6)] |= shape << (y & 63);
                            if((leftover = (y + scale - 1 & 63)) < y && y < height - leftover)
                            {
                                data[(x + i) * ySections + (y >> 6) + 1] |= (1L << leftover) - 1L;
                            }
                        }
                    }
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructs a Region with the given 1D boolean array, with the given width and height, where an [x][y]
     * position is obtained from bits given an index n with x = n / height, y = n % height, any value of true
     * considered "on", and any value of false considered "off."
     * @param bits a 1D boolean array where true is on and false is off
     * @param width the width of the desired Region; width * height should equal bits.length
     * @param height the height of the desired Region; width * height should equal bits.length
     */
    public Region(final boolean[] bits, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
            if(bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region with the given 1D boolean array, reusing the current data storage (without
     * extra allocations) if this.width == width and this.height == height, where an [x][y]
     * position is obtained from bits given an index n with x = n / height, y = n % height, any value of true
     * considered "on", and any value of false considered "off."
     * @param bits a 1D boolean array where true is on and false is off
     * @param width the width of the desired Region; width * height should equal bits.length
     * @param height the height of the desired Region; width * height should equal bits.length
     * @return this for chaining
     */
    public Region refill(final boolean[] bits, final int width, final int height) {
        if (bits != null && this.width == width && this.height == height) {
            Arrays.fill(data, 0L);
            for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / height, y = a % height) {
                data[x * ySections + (y >> 6)] |= (bits[a] ? 1L : 0L) << (y & 63);
            }
            tallied = false;
            return this;
        } else {
            this.width = (bits == null || width < 0) ? 0 : width;
            this.height = (bits == null || bits.length <= 0 || height < 0) ? 0 : height;
            ySections = (this.height + 63) >> 6;
            yEndMask = -1L >>> (64 - (this.height & 63));
            data = new long[this.width * ySections];
            if(bits != null) {
                for (int a = 0, x = 0, y = 0; a < bits.length; a++, x = a / this.height, y = a % this.height) {
                    if (bits[a]) data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                }
            }
            counts = new int[width * ySections];
            tallied = false;
            return this;
        }
    }

    /**
     * Constructor for an empty Region of the given width and height.
     * Regions are mutable, so you can add to this with insert() or insertSeveral(), among others.
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     */
    public Region(final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        counts = new int[width  * ySections];
        ct = 0;
        tallied = true;
    }

    /**
     * If this Region has the same width and height passed as parameters, this acts the same as {@link #empty()},
     * makes no allocations, and returns this Region with its contents all "off"; otherwise, this does allocate
     * a differently-sized amount of internal data to match the new width and height, sets the fields to all match the
     * new width and height, and returns this Region with its new width and height, with all contents "off". This
     * is meant for cases where a Region may be reused effectively, but its size may not always be the same.
     * @param width the width to potentially resize this Region to
     * @param height the height to potentially resize this Region to
     * @return this Region, always with all contents "off", and with the height and width set.
     */
    public Region resizeAndEmpty(final int width, final int height) {
        if (width == this.width && height == this.height) {
            Arrays.fill(data, 0L);
            Arrays.fill(counts, 0);
            ct = 0;
            tallied = true;
        } else {
            this.width = Math.max(width, 0);
            this.height = Math.max(height, 0);
            ySections = (this.height + 63) >> 6;
            yEndMask = -1L >>> (64 - (this.height & 63));
            data = new long[this.width * ySections];
            counts = new int[this.width * ySections];
            ct = 0;
            tallied = true;
        }
        return this;

    }

    /**
     * Constructor for a Region that contains a single "on" cell, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes multiple Coord values, this takes the single "on"
     * Coord first, while the multiple-Coord constructor takes its vararg or array of Coords last.
     * @param single the one (x,y) point to store as "on" in this Region
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     */
    public Region(final Coord single, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        counts = new int[width * ySections];
        if(single.x < width && single.y < height && single.x >= 0 && single.y >= 0)
        {
            data[ct = single.x * ySections + (single.y >> 6)] |= 1L << (single.y & 63);
            counts[ct] = 1;
            ct = 1;
            tallied = true;
        }
        else
        {
            ct = 0;
            tallied = true;
        }
    }

    /**
     * Constructor for a Region that can have several "on" cells specified, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes one Coord value, this takes the vararg or array of
     * Coords last, while the single-Coord constructor takes its one Coord first.
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     * @param points an array or vararg of Coord to store as "on" in this Region
     */
    public Region(final int width, final  int height, final Coord... points)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        if(points != null)
        {
            for (int i = 0, x, y; i < points.length; i++) {
                x = points[i].x;
                y = points[i].y;
                if(x < width && y < height && x >= 0 && y >= 0)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Constructor for a Region that can have several "on" cells specified, and has the given width and height.
     * Note that to avoid confusion with the constructor that takes one Coord value, this takes the Iterable of
     * Coords last, while the single-Coord constructor takes its one Coord first.
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     * @param points an array or vararg of Coord to store as "on" in this Region
     */
    public Region(final int width, final int height, final Iterable<Coord> points)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        if(points != null) {
            int x, y;
            for (Coord c : points) {
                x = c.x;
                y = c.y;
                if (x < width && y < height && x >= 0 && y >= 0)
                    data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            }
        }
        counts = new int[width * ySections];
        tallied = false;

    }

    /**
     * Constructor for a random Region of the given width and height, typically assigning approximately half of
     * the cells in this to "on" and the rest to off.
     * @param random a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     */
    public Region(final EnhancedRandom random, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            data[i] = random.nextLong();
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region by filling it with random values from random, reusing the current data storage
     * (without extra allocations) if this.width == width and this.height == height, and typically assigning
     * approximately half of the cells in this to "on" and the rest to off.
     * @param random a EnhancedRandom or a recommended subclass, like {@link WhiskerRandom}
     * @param width the width of the desired Region
     * @param height the height of the desired Region
     * @return this for chaining
     */
    public Region refill(final EnhancedRandom random, final int width, final int height) {
        if (random != null){
            if(this.width == width && this.height == height) {
                for (int i = 0; i < width * ySections; i++) {
                    data[i] = random.nextLong();
                }
            } else {
                this.width = Math.max(width, 0);
                this.height = Math.max(height, 0);
                ySections = (this.height + 63) >> 6;
                yEndMask = -1L >>> (64 - (this.height & 63));
                data = new long[this.width * ySections];
                for (int i = 0; i < this.width * ySections; i++) {
                    data[i] = random.nextLong();
                }
                counts = new int[width * ySections];
            }
            if(ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }
            tallied = false;
        }
        return this;
    }

    /**
     * Constructor for a random Region of the given width and height, trying to set the given fraction of cells
     * to on. Depending on the value of fraction, this makes between 0 and 6 calls to the nextLong() method of random's
     * internal EnhancedRandom, per 64 cells of this Region (if height is not a multiple of 64, round up to get
     * the number of calls this makes). As such, this sacrifices the precision of the fraction to obtain significantly
     * better speed than generating one random number per cell, although the precision is probably good enough (fraction
     * is effectively rounded down to the nearest multiple of 0.015625, and clamped between 0.0 and 1.0). The parameter
     * {@code random} can be a plain {@link EnhancedRandom}, but will be much faster and have less potential for patterns or
     * artifacts in the output if you use {@link WhiskerRandom} or another higher-quality subclass.
     * @param random a EnhancedRandom or a recommended subclass, like {@link WhiskerRandom}
     * @param fraction between 0.0 and 1.0 (clamped), only considering a precision of 1/64.0 (0.015625) between steps
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     */
    public Region(final EnhancedRandom random, final float fraction, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        int bitCount = (int) (fraction * 64);
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        for (int i = 0; i < width * ySections; i++) {
            data[i] = approximateBits(random, bitCount);
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Reassigns this Region randomly, reusing the current data storage (without extra allocations) if this.width
     * == width and this.height == height, while trying to set the given fraction of cells to on. Depending on the value
     * of fraction, this makes between 0 and 6 calls to the nextLong() method of random's internal EnhancedRandom, per
     * 64 cells of this Region (if height is not a multiple of 64, round up to get the number of calls this
     * makes). As such, this sacrifices the precision of the fraction to obtain significantly better speed than
     * generating one random number per cell, although the precision is probably good enough (fraction is effectively
     * rounded down to the nearest multiple of 0.015625, and clamped between 0.0 and 1.0). The parameter
     * {@code random} can be a plain {@link EnhancedRandom}, but will be much faster and have less potential for patterns or
     * artifacts in the output if you use {@link WhiskerRandom} or another higher-quality subclass.
     * @param random a EnhancedRandom or a recommended subclass, like {@link WhiskerRandom}
     * @param fraction between 0.0 and 1.0 (clamped), only considering a precision of 1/64.0 (0.015625) between steps
     * @param width the maximum width for the Region
     * @param height the maximum height for the Region
     * @return this for chaining
     */
    public Region refill(final EnhancedRandom random, final float fraction, final int width, final int height) {
        if (random != null){
            int bitCount = (int) (fraction * 64);
            if(this.width == width && this.height == height) {
                for (int i = 0; i < width * ySections; i++) {
                    data[i] = approximateBits(random, bitCount);
                }
            } else {
                this.width = Math.max(width, 0);
                this.height = Math.max(height, 0);
                ySections = (this.height + 63) >> 6;
                yEndMask = -1L >>> (64 - (this.height & 63));
                data = new long[this.width * ySections];
                for (int i = 0; i < this.width * ySections; i++) {
                    data[i] = approximateBits(random, bitCount);
                }
                counts = new int[width * ySections];
            }
            if(ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Copy constructor that takes another Region and copies all of its data into this new one.
     * If you find yourself frequently using this constructor and assigning it to the same variable, consider using the
     * {@link #remake(Region)} method on the variable instead, which will, if it has the same width and height
     * as the other Region, avoid creating garbage and quickly fill the variable with the other's contents.
     * @see #copy() for a convenience method that just uses this constructor
     * @param other another Region that will be copied into this new Region
     */
    public Region(final Region other)
    {
        width = other.width;
        height = other.height;
        ySections = other.ySections;
        yEndMask = other.yEndMask;
        data = new long[width * ySections];
        counts = new int[width * ySections];
        System.arraycopy(other.data, 0, data, 0, width * ySections);
        System.arraycopy(other.counts, 0, counts, 0, width * ySections);
        ct  = other.ct;
        tallied = other.tallied;
    }

    /**
     * Primarily for internal use, this constructor copies data2 exactly into the internal long array the new
     * Region will use, and does not perform any validation steps to ensure that cells that would be "on" but are
     * outside the actual height of the Region are actually removed (this only matters if height is not a
     * multiple of 64).
     * @param data2 a long array that is typically from another Region, and would be hard to make otherwise
     * @param width the width of the Region to construct
     * @param height the height of the Region to construct
     */
    public Region(final long[] data2, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];
        System.arraycopy(data2, 0, data, 0, width * ySections);
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }

    /**
     * Primarily for internal use, this constructor copies data2 into the internal long array the new Region will
     * use, but treats data2 as having the dimensions [dataWidth][dataHeight], and uses the potentially-different
     * dimensions [width][height] for the constructed Region. This will truncate data2 on width, height, or both
     * if width or height is smaller than dataWidth or dataHeight. It will fill extra space with all "off" if width or
     * height is larger than dataWidth or dataHeight. It will interpret data2 as the same 2D shape regardless of the
     * width or height it is being assigned to, and data2 will not be reshaped by truncation.
     * @param data2 a long array that is typically from another Region, and would be hard to make otherwise
     * @param dataWidth the width to interpret data2 as having
     * @param dataHeight the height to interpret data2 as having
     * @param width the width of the Region to construct
     * @param height the height of the Region to construct
     */
    public Region(final long[] data2, final int dataWidth, final int dataHeight, final int width, final int height)
    {
        this.width = width;
        this.height = height;
        ySections = (height + 63) >> 6;
        yEndMask = -1L >>> (64 - (height & 63));
        data = new long[width * ySections];

        final int ySections2 = (dataHeight + 63) >> 6;
        if(ySections2 == 0)
        {
            counts = new int[0];
            tallied = false;
            return;
        }
        if(ySections == 1) {
            System.arraycopy(data2, 0, data, 0, Math.min(dataWidth, width));
        }
        else
        {
            if(dataHeight >= height) {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections);
                }
            }
            else
            {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections2);
                }
            }
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        counts = new int[width * ySections];
        tallied = false;
    }
    /**
     * Primarily for internal use, this method copies data2 into the internal long array the new Region will
     * use, but treats data2 as having the dimensions [dataWidth][dataHeight], and uses the potentially-different
     * dimensions [width][height] for this Region, potentially re-allocating the internal data this uses if width
     * and/or height are different from what they were. This will truncate data2 on width, height, or both if width or
     * height is smaller than dataWidth or dataHeight. It will fill extra space with all "off" if width or height is
     * larger than dataWidth or dataHeight. It will interpret data2 as the same 2D shape regardless of the width or
     * height it is being assigned to, and data2 will not be reshaped by truncation.
     * @param data2 a long array that is typically from another Region, and would be hard to make otherwise
     * @param dataWidth the width to interpret data2 as having
     * @param dataHeight the height to interpret data2 as having
     * @param width the width to set this Region to have
     * @param height the height to set this Region to have
     */
    public Region refill(final long[] data2, final int dataWidth, final int dataHeight, final int width, final int height)
    {
        if(width != this.width || height != this.height) {
            this.width = width;
            this.height = height;
            ySections = (height + 63) >> 6;
            yEndMask = -1L >>> (64 - (height & 63));
            data = new long[width * ySections];
            counts = new int[width * ySections];
        }
        else {
            Arrays.fill(data, 0L);
        }
        final int ySections2 = (dataHeight + 63) >> 6;
        if(ySections2 == 0)
            return this;
        if(ySections == 1) {
            System.arraycopy(data2, 0, data, 0, Math.min(dataWidth, width));
        }
        else
        {
            if(dataHeight >= height) {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections);
                }
            }
            else
            {
                for (int i = 0, j = 0; i < width && i < dataWidth; i += ySections2, j += ySections) {
                    System.arraycopy(data2, i, data, j, ySections2);
                }
            }
        }
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    /**
     * A useful method for efficiency, remake() reassigns this Region to have its contents replaced by other. If
     * other and this Region have identical width and height, this is very efficient and performs no additional
     * allocations, simply replacing the cell data in this with the cell data from other. If width and height are not
     * both equal between this and other, this does allocate a new data array, but still reassigns this Region
     * in-place and acts similarly to when width and height are both equal (it just uses some more memory).
     * <br>
     * Using remake() or the similar refill() methods in chains of operations on multiple Regions can be key to
     * maintaining good performance and memory usage. You often can recycle a no-longer-used Region by assigning
     * a Region you want to keep to it with remake(), then mutating either the remade value or the one that was
     * just filled into this but keeping one version around for later usage.
     * @param other another Region to replace the data in this Region with
     * @return this for chaining
     */
    public Region remake(Region other) {
        if (width == other.width && height == other.height) {
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            System.arraycopy(other.counts, 0, counts, 0, width * ySections);
            ct = other.ct;
            tallied = other.tallied;
            return this;
        } else {
            width = other.width;
            height = other.height;
            ySections = other.ySections;
            yEndMask = other.yEndMask;
            data = new long[width * ySections];
            counts = new int[width * ySections];
            System.arraycopy(other.data, 0, data, 0, width * ySections);
            System.arraycopy(other.counts, 0, counts, 0, width * ySections);
            ct = other.ct;
            tallied = other.tallied;
            return this;
        }
    }

    /**
     * Changes the width and/or height of this Region, enlarging or shrinking starting at the edges where
     * {@code x == width - 1} and {@code y == height - 1}. There isn't an especially efficient way to expand from the
     * other edges, but this method is able to copy data in bulk, so at least this method should be very fast. You can
     * use {@code insert(int, int, Region)} if you want to place one Region inside another one,
     * potentially with a different size. The space created by any enlargement starts all off; shrinking doesn't change
     * the existing data where it isn't removed by the shrink.
     * @param widthChange the amount to change width by; can be positive, negative, or zero
     * @param heightChange the amount to change height by; can be positive, negative, or zero
     * @return this for chaining
     */
    public Region alterBounds(int widthChange, int heightChange)
    {
        int newWidth = width + widthChange;
        int newHeight = height + heightChange;
        if(newWidth <= 0 || newHeight <= 0)
        {
            width = 0;
            height = 0;
            ySections= 0;
            yEndMask = -1;
            data = new long[0];
            counts = new int[0];
            ct = 0;
            tallied = true;
            return this;
        }
        int newYSections = (newHeight + 63) >> 6;
        yEndMask = -1L >>> (64 - (newHeight & 63));
        long[] newData = new long[newWidth * newYSections];         
        counts = new int[newWidth * newYSections];
        
        for (int x = 0; x < width && x < newWidth; x++) {
            for (int ys = 0; ys < ySections && ys < newYSections; ys++) {
                newData[x * newYSections + ys] = data[x * ySections + ys];
            }
        }
        ySections = newYSections;
        width = newWidth;
        height = newHeight;
        data = newData;
        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    private void tally()
    {
        ct = 0;
        for (int i = 0, tmp; i < counts.length; i++) {
            tmp = Long.bitCount(data[i]);
            counts[i] = tmp == 0 ? 0 : (ct += tmp);
        }
        tallied = true;
    }

    /**
     * Makes a copy of this Region that has been rotated 90 degrees {@code turns} times. If using y-down
     * coordinates, then these rotations are clockwise; otherwise, they are counter-clockwise. This uses a copy because
     * in many caseswhere the Region has non-equal width and height, the rotated version has different
     * dimensions, and that requires allocating most of a new Region anyway. This Region is never modifed
     * as a result of this method.
     * @param turns how many times to rotate the copy (clockwise if using y-down, counterclockwise otherwise)
     * @return a potentially-rotated copy of this Region
     */
    public Region copyRotated(int turns)
    {
        switch (turns & 3) {
            case 0: {
                return copy();
            }
            case 1: {
                Region next = new Region(height, width);
                for (int x = 0; x < width; x++) {
                    for (int y = 0, iy = height - 1; y < height; y++, iy--) {
                        if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0L)
                            next.data[iy * ySections + (x >> 6)] |= 1L << (x & 63);
                    }
                }
                return next;
            }
            case 2: {
                Region next = new Region(width, height);
                for (int x = 0, ix = width - 1; x < width; x++, ix--) {
                    for (int y = 0, iy = height - 1; y < height; y++, iy--) {
                        if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0L)
                            next.data[ix * ySections + (iy >> 6)] |= 1L << (iy & 63);
                    }
                }
                return next;
            }
            default: {
                Region next = new Region(height, width);
                for (int x = 0, ix = width - 1; x < width; x++, ix--) {
                    for (int y = 0; y < height; y++) {
                        if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0L)
                            next.data[y * ySections + (ix >> 6)] |= 1L << (ix & 63);
                    }
                }
                return next;
            }
        }
    }
    
    public Region flip(boolean leftRight, boolean upDown) {
        if(ySections <= 0) return this;
        if(leftRight) {
            long t;
            for (int x = 0, o = width - 1; x < (width >>> 1); x++, o--) {
                for (int y = 0; y < ySections; y++) {
                    t = data[x * ySections + y];
                    data[x * ySections + y] = data[o * ySections + y];
                    data[o * ySections + y] = t;
                }
            }
        }
        if(upDown) {
            if(yEndMask == -1L) {
                long t;
                for (int x = 0; x < width; x++) {
                    for (int y = 0, o = ySections - 1; y < (ySections >>> 1); y++, o--) {
                        t = Long.reverse(data[x * ySections + y]);
                        data[x * ySections + y] = Long.reverse(data[x * ySections + o]);
                        data[x * ySections + o] = t;
                    }
                    if((ySections & 1) == 1){
                        data[x * ySections + (ySections >>> 1)] = Long.reverse(data[x * ySections + (ySections >>> 1)]);
                    }
                }
            }
            else {
                int shift = BitConversion.countLeadingZeros(yEndMask);
                if (ySections == 1) {
                    for (int x = 0; x < width; x++) {
                        data[x] = Long.reverse(data[x]) >>> shift;
                    }
                } else {
                    for (int x = 0; x < width; x++) {
                        int ie = x * ySections + ySections - 1;
                        int ib = x * ySections + ySections - 2;
                        int il = x * ySections + 1;
                        int is = x * ySections;
                        long end = Long.reverse(data[ie]);
                        long big = Long.reverse(data[ib]);
                        long little = Long.reverse(data[il]);
                        long start = Long.reverse(data[is]);
                        data[ie] = start >>> shift;
                        data[is] = end >>> shift;
                        data[is] |= big << 64 - shift;
                        data[ib] = start << 64 - shift;
                        data[ib] |= little >>> shift;
                        
                        for (int y = 1; y < (ySections >>> 1); y++) {
                            end = big;
                            start = little;
                            big = Long.reverse(data[--ib]);
                            little = Long.reverse(data[++il]);
                            ++is;
                            data[is] = end >>> shift;
                            data[is] |= big << 64 - shift;
                            data[ib] = start << 64 - shift;
                            data[ib] |= little >>> shift;
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * Sets the cell at x,y to on if value is true or off if value is false. Does nothing if x,y is out of bounds.
     * @param value the value to set in the cell
     * @param x the x-position of the cell
     * @param y the y-position of the cell
     * @return this for chaining
     */
    public Region set(boolean value, int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0) {
            if(value)
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            else
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
            tallied = false;
        }
        return this;
    }

    /**
     * Sets the cell at point to on if value is true or off if value is false. Does nothing if point is out of bounds,
     * or if point is null.
     * @param value the value to set in the cell
     * @param point the x,y Coord of the cell to set
     * @return this for chaining
     */
    public Region set(boolean value, Coord point)
    {
        if(point == null) return this;
        return set(value, point.x, point.y);
    }

    /**
     * Sets the cell at x,y to "on". Does nothing if x,y is out of bounds.
     * More efficient, slightly, than {@link #set(boolean, int, int)} if you just need to set a cell to "on".
     * @param x the x-position of the cell
     * @param y the y-position of the cell
     * @return this for chaining
     */
    public Region insert(int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0)
        {
            data[x * ySections + (y >> 6)] |= 1L << (y & 63);
            tallied = false;
        }
        return this;
    }

    /**
     * Sets the given cell, "tightly" encoded for a specific width/height as by {@link #asTightEncoded()}, to "on".
     * Does nothing if the cell is out of bounds.
     * @param tight a cell tightly encoded for this Region's width and height
     * @return this for chaining
     */
    public Region insert(int tight)
    {
        if(tight < width * height && tight >= 0)
        {
            data[(tight % width) * ySections + ((tight / width) >>> 6)] |= 1L << ((tight / width) & 63);
            tallied = false;
        }
        return this;
    }
    /**
     * Sets the cell at point to "on". Does nothing if point is out of bounds, or if point is null.
     * More efficient, slightly, than {@link #set(boolean, Coord)} if you just need to set a cell to "on".
     * @param point the x,y Coord of the cell
     * @return this for chaining
     */
    public Region insert(Coord point)
    {

        if(point == null) return this;
        return insert(point.x, point.y);
    }

    /**
     * Takes another Region, called other, with potentially different size and inserts its "on" cells into thi
     * Region at the given x,y offset, allowing negative x and/or y to put only part of other in this.
     * <br>
     * This is a rather complex method internally, but should be about as efficient as a general insert-region method
     * can be.
     * @param x the x offset to start inserting other at; may be negative
     * @param y the y offset to start inserting other at; may be negative
     * @param other the other Region to insert
     * @return this for chaining
     */
    public Region insert(int x, int y, Region other)
    {
        if(other == null || other.ySections <= 0 || other.width <= 0)
            return this;

        int start = Math.max(0, x), len = Math.min(width, other.width + start) - start,
        oys = other.ySections, jump = (y == 0) ? 0 : (y < 0) ? -(-y >>> 6) : (y >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
        originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[width * ySections];

        long prev, tmp;
        if(oys == ySections) {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            }
        }
        else if(oys < ySections)
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {// oi < oys - Math.max(0, jump)
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }
        else
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        len = Math.min(width, start + len);
        for (int i = start; i < len; i++) {
            for (int j = 0; j < ySections; j++) {
                data[i * ySections + j] |= data2[i * ySections + j];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    public Region insertSeveral(Coord... points)
    {
        for (int i = 0, x, y; i < points.length; i++) {
            x = points[i].x;
            y = points[i].y;
            if(x < width && y < height && x >= 0 && y >= 0)
            {
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                tallied = false;
            }
        }
        return this;
    }

    public Region insertSeveral(final int[] points)
    {
        for (int i = 0, tight; i < points.length; i++) {
            tight = points[i];
            if(tight < width * height && tight >= 0)
            {
                data[(tight % width) * ySections + ((tight / width) >>> 6)] |= 1L << ((tight / width) & 63);
                tallied = false;
            }
        }
        return this;
    }

    public Region insertSeveral(Iterable<Coord> points)
    {
        int x, y;
        for (Coord pt : points) {
            x = pt.x;
            y = pt.y;
            if(x < width && y < height && x >= 0 && y >= 0)
            {
                data[x * ySections + (y >> 6)] |= 1L << (y & 63);
                tallied = false;
            }
        }
        return this;
    }

    public Region insertRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight)
    {
        if(rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
            return this;
        if(startX < 0)
            startX = 0;
        else if(startX >= width)
            startX = width - 1;
        if(startY < 0)
            startY = 0;
        else if(startY >= height)
            startY = height - 1;
        int endX = Math.min(width, startX + rectangleWidth) - 1,
                endY = Math.min(height, startY + rectangleHeight) - 1,
                startSection = startY >> 6, endSection = endY >> 6;
        if(startSection < endSection)
        {
            long startMask = -1L << (startY & 63),
                    endMask = -1L >>> (~endY & 63);
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] |= startMask;
            }
            if(endSection - startSection > 1)
            {
                for (int b = 1; b < endSection - startSection; b++) {
                    for (int a = startX * ySections + startSection + b; a < endX * ySections + ySections; a += ySections) {
                        data[a] = -1;
                    }
                }
            }
            for (int a = startX * ySections + endSection; a <= endX * ySections + endSection; a += ySections) {
                data[a] |= endMask;
            }
        }
        else
        {
            long mask = (-1L << (startY & 63)) & (-1L >>> (~endY & 63));
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] |= mask;
            }
        }

        if(yEndMask != -1L) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    public Region insertCircle(Coord center, int radius)
    {
        float high, changedX;
        int rndX, rndY;
        for (int dx = -radius; dx <= radius; ++dx) {
            changedX = dx - 0.25f * (dx >> 31 | -dx >>> 31); // project nayuki signum
            rndX = Math.round(changedX);
            high = (float) Math.sqrt(radius * radius - changedX * changedX);             
            insert(center.x + rndX, center.y);
            for (float dy = high; dy >= 0.75f; --dy) {
                rndY = Math.round(dy - 0.25f);                 
                insert(center.x + rndX, center.y + rndY);
                insert(center.x + rndX, center.y - rndY);
            }
        }
        return this;
    }

    public Region remove(int x, int y)
    {
        if(x < width && y < height && x >= 0 && y >= 0)
        {
            data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
            tallied = false;
        }
        return this;
    }
    public Region remove(Coord point)
    {
        return remove(point.x, point.y);
    }
    /**
     * Takes another Region, called other, with potentially different size and removes its "on" cells from this
     * Region at the given x,y offset, allowing negative x and/or y to remove only part of other in this.
     * <br>
     * This is a rather complex method internally, but should be about as efficient as a general remove-region method
     * can be. The code is identical to {@link #insert(int, int, Region)} except that where insert only adds
     * cells, this only removes cells. Essentially, insert() is to {@link #or(Region)} as remove() is to
     * {@link #andNot(Region)}.
     * @param x the x offset to start removing other from; may be negative
     * @param y the y offset to start removing other from; may be negative
     * @param other the other Region to remove
     * @return this for chaining
     */
    public Region remove(int x, int y, Region other)
    {
        if(other == null || other.ySections <= 0 || other.width <= 0)
            return this;

        int start = Math.max(0, x), len = Math.min(width, Math.min(other.width, other.width + x) - start),
                oys = other.ySections, jump = (y == 0) ? 0 : (y < 0) ? -(-y >>> 6) : (y-1 >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
        long[] data2 = new long[other.width * ySections];

        long prev, tmp;
        if(oys == ySections) {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * ySections + oi];
                    }
                }
            }
        }
        else if(oys < ySections)
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {// oi < oys - Math.max(0, jump)
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }
        else
        {
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = other.data[j * oys + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < oys; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = other.data[j * oys + oi];
                    }
                }
            }
        }

        if(lily < 0) {
            for (int i = start; i < len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                    data2[i * ySections + j] >>>= -lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        else if(lily > 0) {
            for (int i = start; i < start + len; i++) {
                prev = 0L;
                for (int j = 0; j < ySections; j++) {
                    tmp = prev;
                    prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                    data2[i * ySections + j] <<= lily;
                    data2[i * ySections + j] |= tmp;
                }
            }
        }
        len = Math.min(width, start + len);
        for (int i = start; i < len; i++) {
            for (int j = 0; j < ySections; j++) {
                data[i * ySections + j] &= ~data2[i * ySections + j];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;

        return this;
    }
    public Region removeSeveral(Coord... points)
    {
        for (int i = 0, x, y; i < points.length; i++) {
            x = points[i].x;
            y = points[i].y;
            if(x < width && y < height && x >= 0 && y >= 0)
            {
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
                tallied = false;
            }
        }
        return this;
    }

    public Region removeSeveral(Iterable<Coord> points)
    {
        int x, y;
        for (Coord pt : points) {
            x = pt.x;
            y = pt.y;
            if(x < width && y < height && x >= 0 && y >= 0)
            {
                data[x * ySections + (y >> 6)] &= ~(1L << (y & 63));
                tallied = false;
            }
        }
        return this;
    }

    /**
     * Removes all "on" cells from (startX, startY) inclusive 
     * to (startX+rectangleWidth, startY+rectangleHeight) exclusive, removing a total width of rectangleWidth and a
     * total height of rectangleHeight in cells.
     * @param startX left side x-coordinate
     * @param startY top side (or bottom if positive y is up) y-coordinate
     * @param rectangleWidth how many cells wide the area to remove is
     * @param rectangleHeight how many cells tal the area to remove is
     * @return this, after modification, for chaining
     */
    public Region removeRectangle(int startX, int startY, int rectangleWidth, int rectangleHeight)
    {
        if(startX < 0)
        {
            rectangleWidth += startX;
            startX = 0;
        }
        else if(startX >= width)
        {
            rectangleWidth = 1;
            startX = width - 1;
        }
        if(startY < 0)
        {
            rectangleHeight += startY;
            startY = 0;
        }
        else if(startY >= height)
        {
            rectangleHeight = 1;
            startY = height - 1;
        }
        if(rectangleWidth < 1 || rectangleHeight < 1 || ySections <= 0)
            return this;
        int endX = Math.min(width, startX + rectangleWidth) - 1,
                endY = Math.min(height, startY + rectangleHeight) - 1,
                startSection = startY >> 6, endSection = endY >> 6;
        if(startSection < endSection)
        {
            long startMask = ~(-1L << (startY & 63)),
                    endMask = ~(-1L >>> (~endY & 63));
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] &= startMask;
            }
            if(endSection - startSection > 1)
            {
                for (int b = 1; b < endSection - startSection; b++) {
                    for (int a = startX * ySections + startSection + b; a < endX * ySections + ySections; a += ySections) {
                        data[a] = 0;
                    }
                }
            }
            for (int a = startX * ySections + endSection; a <= endX * ySections + endSection; a += ySections) {
                data[a] &= endMask;
            }
        }
        else
        {
            long mask = ~((-1L << (startY & 63)) & (-1L >>> (~endY & 63)));
            for (int a = startX * ySections + startSection; a <= endX * ySections + startSection; a += ySections) {
                data[a] &= mask;
            }
        }
        tallied = false;
        return this;
    }

    public Region removeCircle(Coord center, int radius)
    {
        float high, changedX;
        int rndX, rndY;
        for (int dx = -radius; dx <= radius; ++dx) {
            changedX = dx - 0.25f * (dx >> 31 | -dx >>> 31); // project nayuki signum
            rndX = Math.round(changedX);
            high = (float) Math.sqrt(radius * radius - changedX * changedX);
            remove(center.x + rndX, center.y);
            for (float dy = high; dy >= 0.75f; --dy) {
                rndY = Math.round(dy - 0.25f);
                remove(center.x + rndX, center.y + rndY);
                remove(center.x + rndX, center.y - rndY);
            }
        }
        return this;
    }

    /**
     * Equivalent to {@link #clear()}, setting all cells to "off," but also returns this for chaining.
     * @return this for chaining
     */
    public Region empty()
    {
        Arrays.fill(data, 0L);
        Arrays.fill(counts, 0);
        ct = 0;
        tallied = true;
        return this;
    }

    /**
     * Sets all cells in this to "on."
     * @return this for chaining
     */
    public Region allOn()
    {
        if(ySections > 0)
        {
            if(yEndMask == -1) {
                Arrays.fill(data, -1);
                Arrays.fill(counts, 64);
                ct = ySections * width << 6;
                tallied = true;
            }
            else
            {
                ct = Long.bitCount(yEndMask);
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] = yEndMask;
                    counts[a] = ct;
                    for (int i = 0; i < ySections - 1; i++) {
                        data[a-i-1] = -1;
                        counts[a-i-1] = 64;
                    }
                }
                ct *= width;
                ct += (ySections - 1) * width << 6;
                tallied = true;
            }
        }
        return this;
    }

    /**
     * Sets all cells in this to "on" if contents is true, or "off" if contents is false.
     * @param contents true to set all cells to on, false to set all cells to off
     * @return this for chaining
     */
    public Region fill(boolean contents)
    {
        if(contents)
        {
            if(ySections > 0)
            {
                if(yEndMask == -1) {
                    Arrays.fill(data, -1);
                    Arrays.fill(counts, 64);
                    ct = ySections * width << 6;
                    tallied = true;
                }
                else
                {
                    ct = Long.bitCount(yEndMask);
                    for (int a = ySections - 1; a < data.length; a += ySections) {
                        data[a] = yEndMask;
                        counts[a] = ct;
                        for (int i = 0; i < ySections - 1; i++) {
                            data[a-i-1] = -1;
                            counts[a-i-1] = 64;
                        }
                    }
                    ct *= width;
                    ct += (ySections - 1) * width << 6;
                    tallied = true;
                }
            }
            //else... what, if ySections is 0 there's nothing to do
        }
        else
        {
            Arrays.fill(data, 0L);
            Arrays.fill(counts, 0);
            ct = 0;
            tallied = true;
        }
        return this;
    }
    /**
     * Turns all cells that are adjacent to the boundaries of the Region to "off".
     * @return this for chaining
     */
    public Region removeEdges()
    {
        if(ySections > 0) {
            for (int i = 0; i < ySections; i++) {
                data[i] = 0L;
                data[width * ySections - 1 - i] = 0L;
            }
            if (ySections == 1) {
                for (int i = 0; i < width; i++) {
                    data[i] &= yEndMask >>> 1 & -2L;
                }
            } else {
                for (int i = ySections; i < data.length - ySections; i += ySections) {
                    data[i] &= -2L;
                }
                for (int a = ySections * 2 - 1; a < data.length - ySections; a += ySections) {
                    data[a] &= yEndMask >>> 1;
                }
            }
            tallied = false;
        }
        return this;
    }

    /**
     * Simple method that returns a newly-allocated copy of this Region; modifications to one won't change the
     * other, and this method returns the copy while leaving the original unchanged.
     * @return a copy of this Region; the copy can be changed without altering the original
     */
    public Region copy()
    {
        return new Region(this);
    }

    /**
     * Returns this Region's data as a 2D boolean array, [width][height] in size, with on treated as true and off
     * treated as false.
     * @return a 2D boolean array that represents this Region's data
     */
    public boolean[][] decode()
    {
        boolean[][] bools = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bools[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0;
            }
        }
        return bools;
    }

    /**
     * Fills this Region's data into the given 2D char array, modifying it and returning it, with "on" cells
     * filled with the char parameter {@code on} and "off" cells with the parameter {@code off}.
     * @param chars a 2D char array that will be modified; must not be null, nor can it contain null elements
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a 2D char array that represents this Region's data
     */
    public char[][] intoChars(char[][] chars, char on, char off)
    {
        for (int x = 0; x < width && x < chars.length; x++) {
            for (int y = 0; y < height && y < chars[x].length; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }

    /**
     * Fills this Region's data into the given 2D char array, modifying it and returning it, with "on" cells
     * filled with the char parameter {@code on} and "off" cells left as-is.
     * @param chars a 2D char array that will be modified; must not be null, nor can it contain null elements
     * @param on the char to use for "on" cells
     * @return a 2D char array that represents the "on" cells in this Region's data written over chars
     */
    public char[][] intoChars(char[][] chars, char on)
    {
        for (int x = 0; x < width && x < chars.length; x++) {
            for (int y = 0; y < height && y < chars[x].length; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    chars[x][y] = on;
            }
        }
        return chars;
    }

    /**
     * Returns this Region's data as a 2D char array,  [width][height] in size, with "on" cells filled with the
     * char parameter on and "off" cells with the parameter off.
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a 2D char array that represents this Region's data
     */
    public char[][] toChars(char on, char off)
    {
        char[][] chars = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return chars;
    }

    /**
     * Returns this Region's data as a 2D char array,  [width][height] in size, with "on" cells filled with '.'
     * and "off" cells with '#'.
     * @return a 2D char array that represents this Region's data
     */

    public char[][] toChars()
    {
        return toChars('.', '#');
    }

    /**
     * Returns this Region's data as a StringBuilder, with each row made of the parameter on for "on" cells and
     * the parameter off for "off" cells, separated by newlines, with no trailing newline at the end.
     * @param on the char to use for "on" cells
     * @param off the char to use for "off" cells
     * @return a StringBuilder that stores each row of this Region as chars, with rows separated by newlines.
     */
    public StringBuilder show(char on, char off)
    {
        StringBuilder sb = new StringBuilder((width+1) * height);
        for (int y = 0; y < height;) {
            for (int x = 0; x < width; x++) {
                sb.append((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off);
            }
            if(++y < height)
                sb.append('\n');
        }
        return sb;
    }

    /**
     * Returns a legible String representation of this that can be printed over multiple lines, with all "on" cells
     * represented by '.' and all "off" cells by '#', in roguelike floors-on walls-off convention, separating each row
     * by newlines (without a final trailing newline, so you could append text right after this).
     * @return a String representation of this Region using '.' for on, '#' for off, and newlines between rows
     */
    @Override
    public String toString()
    {
        return show('.', '#').toString();
    }

    /**
     * Returns a copy of map where if a cell is "on" in this Region, this keeps the value in map intact,
     * and where a cell is "off", it instead writes the char filler.
     * @param map a 2D char array that will not be modified
     * @param filler the char to use where this Region stores an "off" cell
     * @return a masked copy of map, with "off" cells from this changed to filler
     */
    public char[][] writeCharsToOff(char[][] map, char filler)
    {
        if(map == null || map.length == 0)
            return new char[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        char[][] chars = new char[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
            }
        }
        return chars;
    }

    /**
     * Returns a copy of map where if a cell is "on" in this Region, this keeps the value in map intact,
     * and where a cell is "off", it instead writes the short filler. Meant for use with MultiSpill, but may be
     * used anywhere you have a 2D short array. {@link #writeCharsToOff(char[][], char)} is more likely to be useful.
     * @param map a 2D short array that will not be modified
     * @param filler the short to use where this Region stores an "off" cell
     * @return a masked copy of map, with "off" cells from this changed to filler
     */
    public short[][] writeShortsToOff(short[][] map, short filler)
    {
        if(map == null || map.length == 0)
            return new short[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        short[][] shorts = new short[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                shorts[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? map[x][y] : filler;
            }
        }
        return shorts;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this Region, this keeps the value in map intact,
     * and where a cell is "on", it instead writes the char toWrite.
     * @param map a 2D char array that will not be modified
     * @param toWrite the char to use where this Region stores an "on" cell
     * @return a masked copy of map, with "on" cells from this changed to toWrite
     */
    public char[][] writeChars(char[][] map, char toWrite)
    {
        if(map == null || map.length == 0)
            return new char[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        char[][] chars = new char[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                chars[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return chars;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this Region, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the int toWrite.
     * @param map a 2D int array that will not be modified
     * @param toWrite the int to use where this Region stores an "on" cell
     * @return a masked copy of map, with "on" cells from this changed to toWrite
     */
    public int[][] writeInts(int[][] map, int toWrite)
    {
        if(map == null || map.length == 0)
            return new int[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        int[][] ints = new int[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                ints[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return ints;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this Region, this keeps the value in map intact,
     * and where a cell is "on", it instead writes the int toWrite. Modifies map in-place, unlike
     * {@link #writeInts(int[][], int)}.
     * @param map a 2D int array that <b>will</b> be modified
     * @param toWrite the int to use where this Region stores an "on" cell
     * @return map, with "on" cells from this changed to toWrite; not a copy
     */
    public int[][] writeIntsInto(int[][] map, int toWrite)
    {
        if(map == null || map.length == 0)
            return map;
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    map[x][y] = toWrite;
            }
        }
        return map;
    }

    /**
     * Returns this Region's data as a 2D int array,  [width][height] in size, with "on" cells filled with the
     * int parameter on and "off" cells with the parameter off.
     * @param on the int to use for "on" cells
     * @param off the int to use for "off" cells
     * @return a 2D int array that represents this Region's data
     */
    public int[][] toInts(int on, int off)
    {
        int[][] ints = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ints[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? on : off;
            }
        }
        return ints;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this Region, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the float toWrite.
     * @param map a 2D float array that will not be modified
     * @param toWrite the float to use where this Region stores an "on" cell
     * @return an altered copy of map, with "on" cells from this changed to toWrite
     */
    public float[][] writeFloats(float[][] map, float toWrite)
    {
        if(map == null || map.length == 0)
            return new float[0][0];
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        float[][] doubles = new float[width2][height2];
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                doubles[x][y] = (data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
            }
        }
        return doubles;
    }

    /**
     * Returns a copy of map where if a cell is "off" in this Region, this keeps
     * the value in map intact, and where a cell is "on", it instead writes the float toWrite. Modifies map in-place,
     * unlike {@link #writeFloats(float[][], float)}.
     * @param map a 2D float array that <b>will</b> be modified
     * @param toWrite the float to use where this Region stores an "on" cell
     * @return map, with "on" cells from this changed to toWrite; not a copy
     */
    public float[][] writeFloatsInto(float[][] map, float toWrite)
    {
        if(map == null || map.length == 0)
            return map;
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    map[x][y] = toWrite;
            }
        }
        return map;
    }
    /**
     * Like {@link #writeChars(char[][], char)}, but modifies {@code map} in-place and returns it. If a cell is "off"
     * in this Region, this keeps the value in map intact, and where a cell is "on", it instead writes the char
     * toWrite. Modifies map in-place, unlike {@link #writeChars(char[][], char)}.
     * @param map a 2D char array that <b>will</b> be modified
     * @param toWrite the char to use where this Region stores an "on" cell
     * @return map, with "on" cells from this changed to toWrite; not a copy
     */
    public char[][] writeCharsInto(char[][] map, char toWrite)
    {
        if(map == null || map.length == 0)
            return map;
        int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
        for (int x = 0; x < width2; x++) {
            for (int y = 0; y < height2; y++) {
                if((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0)
                    map[x][y] = toWrite;
            }
        }
        return map;
    }

    /**
     * Union of two Regions, assigning the result into this Region. Any cell that is "on" in either
     * Region will be made "on" in this Region.
     * @param other another Region that will not be modified
     * @return this, after modification, for chaining
     */
    public Region or(Region other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] |= other.data[x * other.ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;

        return this;
    }

    /**
     * Intersection of two Regions, assigning the result into this Region. Any cell that is "on" in both
     * Regions will be kept "on" in this Region, but all other cells will be made "off."
     * @param other another Region that will not be modified
     * @return this, after modification, for chaining
     */
    public Region and(Region other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= other.data[x * other.ySections + y];
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Intersection of two Regions, assigning the result into this Region, with the special requirement
     * that other must be a 64x64 area, and the special property that other will be considered tiled to cover all of the
     * area of this Region. Any cell that is "on" in both Regions (treating other as tiling) will be kept
     * "on" in this Region, but all other cells will be made "off."
     * @param other another Region that will not be modified but must be 64x64 in size; will act as if it tiles
     * @return this, after modification, for chaining
     */
    public Region andWrapping64(Region other)
    {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < ySections; y++) {
                data[x * ySections + y] &= other.data[x & 63];
            }
        }
        tallied = false;
        return this;
    }
    /**
     * Difference of two Regions, assigning the result into this Region. Any cell that is "on" in this
     * Region and "off" in other will be kept "on" in this Region, but all other cells will be made "off."
     * @param other another Region that will not be modified
     * @return this, after modification, for chaining
     * @see #notAnd(Region) notAnd is a very similar method that acts sort-of in reverse of this method
     */
    public Region andNot(Region other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] &= ~other.data[x * other.ySections + y];
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Like andNot, but subtracts this Region from other and stores the result in this Region, without
     * mutating other.
     * @param other another Region that will not be modified
     * @return this, after modification, for chaining
     * @see #andNot(Region) andNot is a very similar method that acts sort-of in reverse of this method
     */
    public Region notAnd(Region other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] = other.data[x * other.ySections + y] & ~data[x * ySections + y];
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Symmetric difference (more commonly known as exclusive or, hence the name) of two Regions, assigning the
     * result into this Region. Any cell that is "on" in this and "off" in other, or "off" in this and "on" in
     * other, will be made "on" in this; all other cells will be made "off." Useful to find cells that are "on" in
     * exactly one of two Regions (not "on" in both, or "off" in both).
     * @param other another Region that will not be modified
     * @return this, after modification, for chaining
     */
    public Region xor(Region other)
    {
        for (int x = 0; x < width && x < other.width; x++) {
            for (int y = 0; y < ySections && y < other.ySections; y++) {
                data[x * ySections + y] ^= other.data[x * other.ySections + y];
            }
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Negates this Region, turning "on" to "off" and "off" to "on."
     * @return this, after modification, for chaining
     */
    public Region not()
    {
        for (int a = 0; a < data.length; a++)
        {
            data[a] = ~data[a];
        }

        if(ySections > 0 && yEndMask != -1) {
            for (int a = ySections - 1; a < data.length; a += ySections) {
                data[a] &= yEndMask;
            }
        }
        tallied = false;
        return this;
    }

    /**
     * Moves the "on" cells in this Region to the given x and y offset, removing cells that move out of bounds.
     * @param x the x offset to translate by; can be negative
     * @param y the y offset to translate by; can be negative
     * @return this for chaining
     */
    public Region translate(int x, int y) {
        Region result = this;
        if (width < 1 || ySections <= 0 || (x == 0 && y == 0)) {
        } else {
            int start = Math.max(0, x), len = Math.min(width, width + x) - start,
                    jump = (y == 0) ? 0 : (y < 0) ? -(-y >>> 6) : (y >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                    originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
            long[] data2 = new long[width * ySections];
            long prev, tmp;
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = data[j * ySections + oi];
                    }
                }
            }
            if (lily < 0) {
                for (int i = start; i < len; i++) {
                    prev = 0L;
                    for (int j = 0; j < ySections; j++) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                        data2[i * ySections + j] >>>= -lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            } else if (lily > 0) {
                for (int i = start; i < start + len; i++) {
                    prev = 0L;
                    for (int j = 0; j < ySections; j++) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                        data2[i * ySections + j] <<= lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            }
            if (yEndMask != -1) {
                for (int a = ySections - 1; a < data2.length; a += ySections) {
                    data2[a] &= yEndMask;
                }
            }
            data = data2;
            tallied = false;
        }


        return result;
    }

    /**
     * Adds to this Region with a moved set of its own "on" cells, moved to the given x and y offset.
     * Ignores cells that would be added out of bounds. Keeps all cells that are currently "on" unchanged.
     * @param x the x offset to translate by; can be negative
     * @param y the y offset to translate by; can be negative
     * @return this for chaining
     */
    public Region insertTranslation(int x, int y) {
        Region result = this;
        if (width < 1 || ySections <= 0 || (x == 0 && y == 0)) {
        } else {
            int start = Math.max(0, x), len = Math.min(width, width + x) - start,
                    jump = (y == 0) ? 0 : (y < 0) ? -(-y >>> 6) : (y >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                    originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump);
            long[] data2 = new long[width * ySections];
            long prev, tmp;
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i < ySections && oi < ySections; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = data[j * ySections + oi];
                    }
                }
            }
            if (lily < 0) {
                for (int i = start; i < len; i++) {
                    prev = 0L;
                    for (int j = 0; j < ySections; j++) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                        data2[i * ySections + j] >>>= -lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            } else if (lily > 0) {
                for (int i = start; i < start + len; i++) {
                    prev = 0L;
                    for (int j = 0; j < ySections; j++) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                        data2[i * ySections + j] <<= lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            }
            for (int i = 0; i < width * ySections; i++) {
                data2[i] |= data[i];
            }
            if (yEndMask != -1) {
                for (int a = ySections - 1; a < data2.length; a += ySections) {
                    data2[a] &= yEndMask;
                }
            }
            data = data2;
            tallied = false;
        }


        return result;
    }

    /**
     * Effectively doubles the x and y values of each cell this contains (not scaling each cell to be larger, so each
     * "on" cell will be surrounded by "off" cells), and re-maps the positions so the given x and y in the doubled space
     * become 0,0 in the resulting Region (which is this, assigning to itself).
     * @param x in the doubled coordinate space, the x position that should become 0 x in the result; can be negative
     * @param y in the doubled coordinate space, the y position that should become 0 y in the result; can be negative
     * @return this for chaining
     */
    public Region zoom(int x, int y) {
        Region result = this;
        if (width < 1 || ySections <= 0) {
        } else {
            x = -x;
            y = -y;
            int
                    width2 = width + 1 >>> 1, ySections2 = ySections + 1 >>> 1,
                    start = Math.max(0, x), len = Math.min(width, width + x) - start,
                    //tall = (Math.min(height, height + y) - Math.max(0, y)) + 63 >> 6,
                    jump = (y == 0) ? 0 : (y < 0) ? -(-y >>> 6) : (y >>> 6), lily = (y < 0) ? -(-y & 63) : (y & 63),
                    originalJump = Math.max(0, -jump), alterJump = Math.max(0, jump),
                    oddX = (x & 1), oddY = (y & 1);
            long[] data2 = new long[width * ySections];
            long prev, tmp, yEndMask2 = -1L >>> (64 - ((height + 1 >>> 1) & 63));
            if (x < 0) {
                for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                    for (int j = Math.max(0, -x), jj = 0; jj < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else if (x > 0) {
                for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                    for (int j = 0, jj = start; j < len; j++, jj++) {
                        data2[jj * ySections + i] = data[j * ySections + oi];
                    }
                }
            } else {
                for (int i = alterJump, oi = originalJump; i <= ySections2 && oi < ySections; i++, oi++) {
                    for (int j = 0; j < len; j++) {
                        data2[j * ySections + i] = data[j * ySections + oi];
                    }
                }
            }
            if (lily < 0) {
                for (int i = start; i < len; i++) {
                    prev = 0L;
                    for (int j = ySections2; j >= 0; j--) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L << -lily)) << (64 + lily);
                        data2[i * ySections + j] >>>= -lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            } else if (lily > 0) {
                for (int i = start; i < start + len; i++) {
                    prev = 0L;
                    for (int j = 0; j < ySections2; j++) {
                        tmp = prev;
                        prev = (data2[i * ySections + j] & ~(-1L >>> lily)) >>> (64 - lily);
                        data2[i * ySections + j] <<= lily;
                        data2[i * ySections + j] |= tmp;
                    }
                }
            }
            if (yEndMask2 != -1) {
                for (int a = ySections2 - 1; a < data2.length; a += ySections) {
                    data2[a] &= yEndMask2;
                    if (ySections2 < ySections)
                        data2[a + 1] = 0L;
                }
            }
            for (int i = 0; i < width2; i++) {
                for (int j = 0; j < ySections2; j++) {
                    prev = data2[i * ySections + j];
                    tmp = prev >>> 32;
                    prev &= 0xFFFFFFFFL;
                    prev = (prev | (prev << 16)) & 0x0000FFFF0000FFFFL;
                    prev = (prev | (prev << 8)) & 0x00FF00FF00FF00FFL;
                    prev = (prev | (prev << 4)) & 0x0F0F0F0F0F0F0F0FL;
                    prev = (prev | (prev << 2)) & 0x3333333333333333L;
                    prev = (prev | (prev << 1)) & 0x5555555555555555L;
                    prev <<= oddY;
                    if (oddX == 1) {
                        if (i * 2 + 1 < width)
                            data[(i * ySections + j) * 2 + ySections] = prev;
                        if (i * 2 < width)
                            data[(i * ySections + j) * 2] = 0L;
                    } else {
                        if (i * 2 < width)
                            data[(i * ySections + j) * 2] = prev;
                        if (i * 2 + 1 < width)
                            data[(i * ySections + j) * 2 + ySections] = 0L;
                    }
                    if (j * 2 + 1 < ySections) {
                        tmp = (tmp | (tmp << 16)) & 0x0000FFFF0000FFFFL;
                        tmp = (tmp | (tmp << 8)) & 0x00FF00FF00FF00FFL;
                        tmp = (tmp | (tmp << 4)) & 0x0F0F0F0F0F0F0F0FL;
                        tmp = (tmp | (tmp << 2)) & 0x3333333333333333L;
                        tmp = (tmp | (tmp << 1)) & 0x5555555555555555L;
                        tmp <<= oddY;
                        if (oddX == 1) {
                            if (i * 2 + 1 < width)
                                data[(i * ySections + j) * 2 + ySections + 1] = tmp;
                            if (i * 2 < width)
                                data[(i * ySections + j) * 2 + 1] = 0L;
                        } else {
                            if (i * 2 < width)
                                data[(i * ySections + j) * 2 + 1] = tmp;
                            if (i * 2 + 1 < width)
                                data[(i * ySections + j) * 2 + ySections + 1] = 0L;
                        }
                    }
                }
            }
            if (yEndMask != -1) {
                for (int a = ySections - 1; a < data.length; a += ySections) {
                    data[a] &= yEndMask;
                }
            }
            tallied = false;
        }


        return result;
    }

    /**
     * Takes the pairs of "on" cells in this Region that are separated by exactly one cell in an orthogonal line,
     * and changes the gap cells to "on" as well.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region connect() {
        Region result = this;
        if (width < 2 || ySections == 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, 0, next, 0, width * ySections);
            for (int a = 0; a < ySections; a++) {
                next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a + ySections];
                next[(width - 1) * ySections + a] |= ((data[(width - 1) * ySections + a] << 1) & (data[(width - 1) * ySections + a] >>> 1)) | data[(width - 2) * ySections + a];

                for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                    next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections]);
                }

                if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1);
                    }
                } else {
                    for (int i = ySections; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] >>> 1 & 1L);
                    }
                }

                if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i + 1] & 1L) << 63 & (data[i] << 1);
                    }
                } else {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] << 1 & 0x8000000000000000L);
                    }

                }
            }
            if (ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    /**
     * Takes the pairs of "on" cells in this Region that are separated by exactly one cell in an orthogonal or
     * diagonal line, and changes the gap cells to "on" as well.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region connect8way() {
        Region result = this;
        if (width < 2 || ySections == 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, 0, next, 0, width * ySections);
            for (int a = 0; a < ySections; a++) {
                next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a + ySections] | (data[a + ySections] << 1) | (data[a + ySections] >>> 1);
                next[(width - 1) * ySections + a] |= ((data[(width - 1) * ySections + a] << 1) & (data[(width - 1) * ySections + a] >>> 1))
                        | data[(width - 2) * ySections + a] | (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1);

                for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                    next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections])
                            | ((data[i - ySections] << 1) & (data[i + ySections] >>> 1))
                            | ((data[i + ySections] << 1) & (data[i - ySections] >>> 1));
                }

                if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1)) |
                                ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i + ySections] >>> 1)) |
                                ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i - ySections] >>> 1));
                    }
                } else {
                    for (int i = ySections; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] >>> 1 & 1L) | (data[i - ySections] >>> 1 & 1L) | (data[i + ySections] >>> 1 & 1L);
                    }
                }

                if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i + 1] & 1L) << 63 & (data[i] << 1)) |
                                ((data[i - ySections + 1] & 1L) << 63 & (data[i + ySections] << 1)) |
                                ((data[i + ySections + 1] & 1L) << 63 & (data[i - ySections] << 1));
                    }
                } else {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] << 1 & 0x8000000000000000L)
                                | (data[i - ySections] << 1 & 0x8000000000000000L) | (data[i + ySections] << 1 & 0x8000000000000000L);
                    }

                }
            }
            if (ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }
    /**
     * Takes the pairs of "on" cells in this Region that are separated by exactly one cell in an orthogonal or
     * diagonal line, and changes the gap cells to "on" as well. As a special case, this requires diagonals to either
     * have no "on" cells adjacent along the perpendicular diagonal, or both cells on that perpendicular diagonal need
     * to be "on." This is useful to counteract some less-desirable behavior of {@link #connect8way()}, where a right
     * angle would always get the inner corners filled because it was considered a diagonal.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region connectLines() {
        Region result = this;
        if (width < 2 || ySections == 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, 0, next, 0, width * ySections);
            for (int a = 0; a < ySections; a++) {
                next[a] |= ((data[a] << 1) & (data[a] >>> 1)) | data[a + ySections] | (data[a + ySections] << 1) | (data[a + ySections] >>> 1);
                next[(width - 1) * ySections + a] |= ((data[(width - 1) * ySections + a] << 1) & (data[(width - 1) * ySections + a] >>> 1))
                        | data[(width - 2) * ySections + a] | (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1);

                for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                    next[i] |= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections])
                            | (((data[i - ySections] << 1) & (data[i + ySections] >>> 1))
                            ^ ((data[i + ySections] << 1) & (data[i - ySections] >>> 1)));
                }

                if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63 & (data[i] >>> 1))
                                | (((data[i - ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i + ySections] >>> 1))
                                ^ ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63 & (data[i - ySections] >>> 1)));
                    }
                } else {
                    for (int i = ySections; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] >>> 1 & 1L) | (data[i - ySections] >>> 1 & 1L) | (data[i + ySections] >>> 1 & 1L);
                    }
                }

                if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i + 1] & 1L) << 63 & (data[i] << 1))
                                | (((data[i - ySections + 1] & 1L) << 63 & (data[i + ySections] << 1))
                                ^ ((data[i + ySections + 1] & 1L) << 63 & (data[i - ySections] << 1)));
                    }
                } else {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i] << 1 & 0x8000000000000000L)
                                | (data[i - ySections] << 1 & 0x8000000000000000L) | (data[i + ySections] << 1 & 0x8000000000000000L);
                    }

                }
            }
            if (ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    /**
     * Like {@link #retract()}, this reduces the width of thick areas of this Region, but thin() will not remove
     * areas that would be identical in a subsequent call to retract(), such as if the area would be eliminated. This
     * is useful primarily for adjusting areas so they do not exceed a width of 2 cells, though their length (the longer
     * of the two dimensions) will be unaffected by this. Especially wide, irregularly-shaped areas may have unintended
     * appearances if you call this repeatedly or use {@link #thinFully()}; consider using this sparingly, or primarily
     * when an area has just gotten thicker than desired.
     * <br>
     * This currently uses 4-way adjacency, but had previously used 8-way; if you want the behavior this previously had,
     * you can use {@link #thin8way()}, but it may be a good idea to try this method as well (some of the old behavior
     * had problems where it yielded significantly larger minimum widths in some areas).
     * @return this for chaining
     */
    public Region thin() {
        Region result = this;
        if (width <= 2 || ySections <= 0) {
        } else {
            Region c1 = new Region(this).retract(),
                    c2 = new Region(c1).expand().xor(this).expand().and(this);
            remake(c1).or(c2);/*
        System.out.println("\n\nc1:\n" + c1.toString() + "\n");
        System.out.println("\n\nc2:\n" + c2.toString() + "\n");
        System.out.println("\n\nthis:\n" + toString() + "\n");
        */
        }
        return result;
    }

    /**
     * Calls {@link #thin()} repeatedly, until the result is unchanged from the last call. Consider using the idiom
     * {@code expand8way().retract().thinFully()} to help change a possibly-strange appearance when the Region
     * this is called on touches the edges of the grid. In general, this method is likely to go too far when it tries to
     * thin a round or irregular area, and this often results in many diagonal lines spanning the formerly-thick area.
     * <br>
     * This currently uses 4-way adjacency, but had previously used 8-way; if you want the behavior this previously had,
     * you can use {@link #thinFully8way()}, but it may be a good idea to try this method as well (some of the old
     * behavior had problems where it yielded significantly larger minimum widths in some areas).
     * @return this for chaining
     */
    public Region thinFully()
    {
        while (size() != thin().size());
        return this;
    }


    /**
     * Like {@link #retract8way()}, this reduces the width of thick areas of this Region, but thin8way() will not
     * remove areas that would be identical in a subsequent call to retract8way(), such as if the area would be
     * eliminated. This is useful primarily for adjusting areas so they do not exceed a width of 2 cells, though their
     * length (the longer of the two dimensions) will be unaffected by this. Especially wide, irregularly-shaped areas
     * may have unintended appearances if you call this repeatedly or use {@link #thinFully8way()}; consider using this
     * sparingly, or primarily when an area has just gotten thicker than desired.
     * <br>
     * This method was called {@link #thin()}, but now that name refers to a variant that uses 4-way adjacency.
     * @return this for chaining
     */
    public Region thin8way() {
        Region result = this;
        if (width <= 2 || ySections <= 0) {
        } else {
            Region c1 = new Region(this).retract8way(),
                    c2 = new Region(c1).expand8way().xor(this).expand8way().and(this);
            remake(c1).or(c2);
        }
        return result;
    }

    /**
     * Calls {@link #thin8way()} repeatedly, until the result is unchanged from the last call. Consider using the idiom
     * {@code expand8way().retract().thinFully8way()} to help change a strange appearance when the Region this is
     * called on touches the edges of the grid. In general, this method is likely to go too far when it tries to thin a
     * round or irregular area, and this often results in many diagonal lines spanning the formerly-thick area.
     * <br>
     * This method was called {@link #thinFully()}, but now that name refers to a variant that uses 4-way adjacency.
     * @return this for chaining
     */
    public Region thinFully8way()
    {
        while (size() != thin8way().size());
        return this;
    }


    /**
     * Removes "on" cells that are orthogonally adjacent to other "on" cells, keeping at least one cell in a group "on."
     * Uses a "checkerboard" pattern to determine which cells to turn  off, with all cells that would be black on a
     * checkerboard turned off and all others kept as-is.
     * @return this for chaining
     */
    public Region disperse() {
        Region result = this;
        if (width < 1 || ySections <= 0) {
        } else {
            long mask = 0x5555555555555555L;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < ySections; j++) {
                    data[j] &= mask;
                }
                mask = ~mask;
            }
            tallied = false;
        }
        return result;
    }
    /**
     * Removes "on" cells that are 8-way adjacent to other "on" cells, keeping at least one cell in a group "on."
     * Uses a "grid-like" pattern to determine which cells to turn off, with all cells with even x and even y kept as-is
     * but all other cells (with either or both odd x or odd y) turned off.
     * @return this for chaining
     */
    public Region disperse8way() {
        Region result = this;
        if (width < 1 || ySections <= 0) {
        } else {
            int len = data.length;
            long mask = 0x5555555555555555L;
            for (int j = 0; j < len - 1; j += 2) {
                data[j] &= mask;
                data[j + 1] = 0;
            }
            tallied = false;
        }
        return result;
    }
    /**
     * Removes "on" cells that are nearby other "on" cells, with a random factor to which bits are actually turned off
     * that still ensures exactly half of the bits are kept as-is (the one exception is when height is an odd number,
     * which makes the bottom row slightly random).
     * @param random the RNG used for a random factor
     * @return this for chaining
     */
    public Region disperseRandom(EnhancedRandom random) {
        Region result = this;
        if (width < 1 || ySections <= 0) {
        } else {
            int len = data.length;
            for (int j = 0; j < len; j++) {
                data[j] &= randomInterleave(random);
            }
            tallied = false;
        }
        return result;
    }
    /**
     * Generates a random 64-bit long with a number of '1' bits (Hamming weight) equal on average to bitCount.
     * For example, calling this with a parameter of 32 will be equivalent to calling nextLong() on the given
     * EnhancedRandom, which is recommended to be a subclass like {@link WhiskerRandom}.
     * Calling this with a parameter of 16 will have on average 16 of the 64 bits in the returned long set to '1',
     * distributed pseudo-randomly, while a parameter of 47 will have on average 47 bits set. This can be useful for
     * certain code that uses bits to represent data but needs a different ratio of set bits to unset bits than 1:1.
     * <br>
     * The parameter {@code random} can be a plain {@link EnhancedRandom}, but will be much faster and have less potential
     * for patterns or artifacts in the output if you use {@link WhiskerRandom} or another higher-quality subclass.
     * @param random a EnhancedRandom or a recommended subclass, like {@link WhiskerRandom}
     * @param bitCount an int, only considered if between 0 and 64, that is the average number of bits to set
     * @return a 64-bit long that, on average, should have bitCount bits set to 1, potentially anywhere in the long
     */
    public static long approximateBits(EnhancedRandom random, int bitCount) {
        long result;
        if (bitCount <= 0) {
            result = 0L;
        } else if (bitCount >= 64) {
            result = -1L;
        } else if (bitCount == 32) {
            result = random.nextLong();
        } else {
            boolean high = bitCount > 32;
            int altered = (high ? 64 - bitCount : bitCount), lsb = BitConversion.lowestOneBit(altered);
            long data = random.nextLong();
            for (int i = lsb << 1; i <= 16; i <<= 1) {
                if ((altered & i) == 0)
                    data &= random.nextLong();
                else
                    data |= random.nextLong();
            }
            result = high ? ~(random.nextLong() & data) : (random.nextLong() & data);
        }
        return result;
    }

    /**
     * Gets a somewhat-random long with exactly 32 bits set; in each pair of bits starting at bit 0 and bit 1, then bit
     * 2 and bit 3, up to bit 62 and bit 3, one bit will be 1 and one bit will be 0 in each pair.
     * <br>
     * Not exactly general-use; meant for generating data for Region.
     * @return a random long with 32 "1" bits, distributed so exactly one bit is "1" for each pair of bits
     */
    public static long randomInterleave(EnhancedRandom random) {
        long bits = random.nextLong() & 0xFFFFFFFFL, ib = ~bits & 0xFFFFFFFFL;
        bits |= (bits << 16);
        ib |= (ib << 16);
        bits &= 0x0000FFFF0000FFFFL;
        ib &= 0x0000FFFF0000FFFFL;
        bits |= (bits << 8);
        ib |= (ib << 8);
        bits &= 0x00FF00FF00FF00FFL;
        ib &= 0x00FF00FF00FF00FFL;
        bits |= (bits << 4);
        ib |= (ib << 4);
        bits &= 0x0F0F0F0F0F0F0F0FL;
        ib &= 0x0F0F0F0F0F0F0F0FL;
        bits |= (bits << 2);
        ib |= (ib << 2);
        bits &= 0x3333333333333333L;
        ib &= 0x3333333333333333L;
        bits |= (bits << 1);
        ib |= (ib << 1);
        bits &= 0x5555555555555555L;
        ib &= 0x5555555555555555L;
        return (bits | (ib << 1));
    }

    /**
     * Takes the "on" cells in this Region and expands them by one cell in the 4 orthogonal directions, making
     * each "on" cell take up a plus-shaped area that may overlap with other "on" cells (which is just a normal "on"
     * cell then).
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including expand(), {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region expand() {
        Region result = this;
        if (width < 2 || ySections == 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, 0, next, 0, width * ySections);
            for (int a = 0; a < ySections; a++) {
                next[a] |= (data[a] << 1) | (data[a] >>> 1) | data[a + ySections];
                next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a] << 1) | (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a];

                for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                    next[i] |= (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections];
                }

                if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i - 1] & 0x8000000000000000L) >>> 63;
                    }
                }

                if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= (data[i + 1] & 1L) << 63;
                    }
                }
            }
            if (ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }
    /**
     * Takes the "on" cells in this Region and expands them by amount cells in the 4 orthogonal directions,
     * making each "on" cell take up a plus-shaped area that may overlap with other "on" cells (which is just a normal
     * "on" cell then).
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region expand(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand();
        }
        return this;
    }
    /**
     * Takes the "on" cells in this Region and produces amount Regions, each one expanded by 1 cell in
     * the 4 orthogonal directions relative to the previous Region, making each "on" cell take up a plus-shaped
     * area that may overlap with other "on" cells (which is just a normal "on" cell then). This returns an array of
     * Regions with progressively greater expansions, and does not modify this Region.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return an array of new Regions, length == amount, where each one is expanded by 1 relative to the last
     */
    public Region[] expandSeries(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                regions[i] = new Region(temp.expand());
            }
            result = regions;
        }
        return result;
    }

    public ObjectList<Region> expandSeriesToLimit()
    {
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (temp.size() != temp.expand().size()) {
            regions.add(new Region(temp));
        }
        return regions;
    }
    /**
     * Takes the "on" cells in this Region and expands them by one cell in the 4 orthogonal directions, producing
     * a diamoond shape, then removes the original area before expansion, producing only the cells that were "off" in
     * this and within 1 cell (orthogonal-only) of an "on" cell. This method is similar to {@link #surface()}, but
     * surface finds cells inside the current Region, while fringe finds cells outside it.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time. The surface and fringe methods do allocate one
     * temporary Region to store the original before modification, but the others generally don't.
     * @return this for chaining
     */
    public Region fringe()
    {
        Region cpy = new Region(this);
        expand();
        return andNot(cpy);
    }
    /**
     * Takes the "on" cells in this Region and expands them by amount cells in the 4 orthogonal directions
     * (iteratively, producing a diamond shape), then removes the original area before expansion, producing only the
     * cells that were "off" in this and within amount cells (orthogonal-only) of an "on" cell. This method is similar
     * to {@link #surface()}, but surface finds cells inside the current Region, while fringe finds cells outside
     * it.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time. The surface and fringe methods do allocate one
     * temporary Region to store the original before modification, but the others generally don't.
     * @return this for chaining
     */

    public Region fringe(int amount)
    {
        Region cpy = new Region(this);
        expand(amount);
        return andNot(cpy);
    }

    /**
     * Takes the "on" cells in this Region and produces amount Regions, each one expanded by 1 cell in
     * the 4 orthogonal directions relative to the previous Region, making each "on" cell take up a diamond-
     * shaped area. After producing the expansions, this removes the previous Region from the next Region
     * in the array, making each "fringe" in the series have 1 "thickness," which can be useful for finding which layer
     * of expansion a cell lies in. This returns an array of Regions with progressively greater expansions
     * without the cells of this Region, and does not modify this Region.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return an array of new Regions, length == amount, where each one is a 1-depth fringe pushed further out from this
     */
    public Region[] fringeSeries(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            regions[0] = new Region(temp);
            for (int i = 1; i < amount; i++) {
                regions[i] = new Region(temp.expand());
            }
            for (int i = 0; i < amount - 1; i++) {
                regions[i].xor(regions[i + 1]);
            }
            regions[amount - 1].fringe();
            result = regions;
        }
        return result;
    }
    public ObjectList<Region> fringeSeriesToLimit()
    {
        ObjectList<Region> regions = expandSeriesToLimit();
        for (int i = regions.size() - 1; i > 0; i--) {
            regions.get(i).xor(regions.get(i-1));
        }
        regions.get(0).xor(this);
        return regions;
    }

    /**
     * Takes the "on" cells in this Region and retracts them by one cell in the 4 orthogonal directions,
     * making each "on" cell that was orthogonally adjacent to an "off" cell into an "off" cell.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, retract(), {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region retract() {
        Region result = this;
        if (width <= 2 || ySections <= 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
            for (int a = 0; a < ySections; a++) {
                if (a > 0 && a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                                & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                                & data[i - ySections]
                                & data[i + ySections];
                    }
                } else if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                                & (data[i] >>> 1)
                                & data[i - ySections]
                                & data[i + ySections];
                    }
                } else if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= (data[i] << 1)
                                & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                                & data[i - ySections]
                                & data[i + ySections];
                    }
                } else // only the case when ySections == 1
                {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= (data[i] << 1) & (data[i] >>> 1) & data[i - ySections] & data[i + ySections];
                    }
                }
            }
            if (yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    /**
     * Takes the "on" cells in this Region and retracts them by one cell in the 4 orthogonal directions, doing
     * this iteeratively amount times, making each "on" cell that was within amount orthogonal distance to an "off" cell
     * into an "off" cell.
     * <br>
     * This method is very efficient due to how the class is implemented, and the various spatial increase/decrease
     * methods (including {@link #expand()}, {@link #retract()}, {@link #fringe()}, and {@link #surface()}) all perform
     * very well by operating in bulk on up to 64 cells at a time.
     * @return this for chaining
     */
    public Region retract(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract();
        }
        return this;
    }

    public Region[] retractSeries(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                regions[i] = new Region(temp.retract());
            }
            result = regions;
        }
        return result;
    }

    public ObjectList<Region> retractSeriesToLimit()
    {
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (!temp.retract().isEmpty()) {
            regions.add(new Region(temp));
        }
        return regions;
    }

    public Region surface()
    {
        Region cpy = new Region(this).retract();
        return xor(cpy);
    }
    public Region surface(int amount)
    {
        Region cpy = new Region(this).retract(amount);
        return xor(cpy);
    }

    public Region[] surfaceSeries(int amount)
    {
        if(amount <= 0) return new Region[0];
        Region[] regions = new Region[amount];
        Region temp = new Region(this);
        regions[0] = new Region(temp);
        for (int i = 1; i < amount; i++) {
            regions[i] = new Region(temp.retract());
        }
        for (int i = 0; i < amount - 1; i++) {
            regions[i].xor(regions[i + 1]);
        }
        regions[amount - 1].surface();
        return regions;
    }

    public ObjectList<Region> surfaceSeriesToLimit() {
        ObjectList<Region> result;
        ObjectList<Region> regions = retractSeriesToLimit();
        if (regions.isEmpty()) {
            result = regions;
        } else {
            regions.add(0, regions.get(0).copy().xor(this));
            for (int i = 1; i < regions.size() - 1; i++) {
                regions.get(i).xor(regions.get(i + 1));
            }
            result = regions;
        }
        return result;
    }
    public Region expand8way() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, 0, next, 0, width * ySections);
            for (int a = 0; a < ySections; a++) {
                next[a] |= (data[a] << 1) | (data[a] >>> 1)
                        | data[a + ySections] | (data[a + ySections] << 1) | (data[a + ySections] >>> 1);
                next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a] << 1) | (data[(width - 1) * ySections + a] >>> 1)
                        | data[(width - 2) * ySections + a] | (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1);

                for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                    next[i] |= (data[i] << 1) | (data[i] >>> 1)
                            | data[i - ySections] | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                            | data[i + ySections] | (data[i + ySections] << 1) | (data[i + ySections] >>> 1);
                }

                if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                                ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                                ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                    }
                }

                if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] |= ((data[i + 1] & 1L) << 63) |
                                ((data[i - ySections + 1] & 1L) << 63) |
                                ((data[i + ySections + 1] & 1L) << 63);
                    }
                }
            }
            if (ySections > 0 && yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    public Region expand8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            expand8way();
        }
        return this;
    }

    public Region[] expandSeries8way(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                regions[i] = new Region(temp.expand8way());
            }
            result = regions;
        }
        return result;
    }
    public ObjectList<Region> expandSeriesToLimit8way()
    {
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (temp.size() != temp.expand8way().size()) {
            regions.add(new Region(temp));
        }
        return regions;
    }

    public Region fringe8way()
    {
        Region cpy = new Region(this);
        expand8way();
        return andNot(cpy);
    }
    public Region fringe8way(int amount)
    {
        Region cpy = new Region(this);
        expand8way(amount);
        return andNot(cpy);
    }

    public Region[] fringeSeries8way(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            regions[0] = new Region(temp);
            for (int i = 1; i < amount; i++) {
                regions[i] = new Region(temp.expand8way());
            }
            for (int i = 0; i < amount - 1; i++) {
                regions[i].xor(regions[i + 1]);
            }
            regions[amount - 1].fringe8way();
            result = regions;
        }
        return result;
    }
    public ObjectList<Region> fringeSeriesToLimit8way()
    {
        ObjectList<Region> regions = expandSeriesToLimit8way();
        for (int i = regions.size() - 1; i > 0; i--) {
            regions.get(i).xor(regions.get(i-1));
        }
        regions.get(0).xor(this);
        return regions;
    }

    public Region retract8way() {
        Region result = this;
        if (width <= 2 || ySections <= 0) {
        } else {
            final long[] next = new long[width * ySections];
            System.arraycopy(data, ySections, next, ySections, (width - 2) * ySections);
            for (int a = 0; a < ySections; a++) {
                if (a > 0 && a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                                & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                                & data[i - ySections]
                                & data[i + ySections]
                                & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                                & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                                & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                                & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                    }
                } else if (a > 0) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= ((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                                & (data[i] >>> 1)
                                & data[i - ySections]
                                & data[i + ySections]
                                & ((data[i - ySections] << 1) | ((data[i - 1 - ySections] & 0x8000000000000000L) >>> 63))
                                & ((data[i + ySections] << 1) | ((data[i - 1 + ySections] & 0x8000000000000000L) >>> 63))
                                & (data[i - ySections] >>> 1)
                                & (data[i + ySections] >>> 1);
                    }
                } else if (a < ySections - 1) {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= (data[i] << 1)
                                & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63))
                                & data[i - ySections]
                                & data[i + ySections]
                                & (data[i - ySections] << 1)
                                & (data[i + ySections] << 1)
                                & ((data[i - ySections] >>> 1) | ((data[i + 1 - ySections] & 1L) << 63))
                                & ((data[i + ySections] >>> 1) | ((data[i + 1 + ySections] & 1L) << 63));
                    }
                } else // only the case when ySections == 1
                {
                    for (int i = ySections + a; i < (width - 1) * ySections; i += ySections) {
                        next[i] &= (data[i] << 1)
                                & (data[i] >>> 1)
                                & data[i - ySections]
                                & data[i + ySections]
                                & (data[i - ySections] << 1)
                                & (data[i + ySections] << 1)
                                & (data[i - ySections] >>> 1)
                                & (data[i + ySections] >>> 1);
                    }
                }
            }
            if (yEndMask != -1) {
                for (int a = ySections - 1; a < next.length; a += ySections) {
                    next[a] &= yEndMask;
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    public Region retract8way(int amount)
    {
        for (int i = 0; i < amount; i++) {
            retract8way();
        }
        return this;
    }

    public Region[] retractSeries8way(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                regions[i] = new Region(temp.retract8way());
            }
            result = regions;
        }
        return result;
    }

    public ObjectList<Region> retractSeriesToLimit8way()
    {
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (!temp.retract8way().isEmpty()) {
            regions.add(new Region(temp));
        }
        return regions;
    }

    public Region surface8way()
    {
        Region cpy = new Region(this).retract8way();
        return xor(cpy);
    }

    public Region surface8way(int amount)
    {
        Region cpy = new Region(this).retract8way(amount);
        return xor(cpy);
    }

    public Region[] surfaceSeries8way(int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            Region[] regions = new Region[amount];
            Region temp = new Region(this);
            regions[0] = new Region(temp);
            for (int i = 1; i < amount; i++) {
                regions[i] = new Region(temp.retract8way());
            }
            for (int i = 0; i < amount - 1; i++) {
                regions[i].xor(regions[i + 1]);
            }
            regions[amount - 1].surface8way();
            result = regions;
        }
        return result;
    }
    public ObjectList<Region> surfaceSeriesToLimit8way() {
        ObjectList<Region> result;
        ObjectList<Region> regions = retractSeriesToLimit8way();
        if (regions.isEmpty()) {
            result = regions;
        } else {
            regions.add(0, regions.get(0).copy().xor(this));
            for (int i = 1; i < regions.size() - 1; i++) {
                regions.get(i).xor(regions.get(i + 1));
            }
            result = regions;
        }
        return result;
    }

    /**
     * Like {@link #expand()}, but limits expansion to the "on" cells of {@code bounds}. Expands in all 4-way directions
     * by one cell simultaneously, and only successfully affects the cells that are adjacent to this and are in bounds.
     * @param bounds the set of "on" cells that limits where this can expand into
     * @return this, after expanding, for chaining
     */
    public Region flood(Region bounds) {
        Region result = this;
        if (width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0) {
        } else {
            final long[] next = new long[width * ySections];
            for (int a = 0; a < ySections && a < bounds.ySections; a++) {
                next[a] |= (data[a] | (data[a] << 1) | (data[a] >>> 1) | data[a + ySections]) & bounds.data[a];
                next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a] | (data[(width - 1) * ySections + a] << 1)
                        | (data[(width - 1) * ySections + a] >>> 1) | data[(width - 2) * ySections + a]) & bounds.data[(width - 1) * bounds.ySections + a];

                for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections &&
                        j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1) | data[i - ySections] | data[i + ySections]) & bounds.data[j];
                }

                if (a > 0) {
                    for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections && j < (bounds.width - 1) * bounds.ySections;
                         i += ySections, j += bounds.ySections) {
                        next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                    }
                }

                if (a < ySections - 1 && a < bounds.ySections - 1) {
                    for (int i = ySections + a, j = bounds.ySections + a;
                         i < (width - 1) * ySections && j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
                        next[i] |= (data[i] | ((data[i + 1] & 1L) << 63)) & bounds.data[j];
                    }
                }
            }
            if (yEndMask != -1 && bounds.yEndMask != -1) {
                if (ySections == bounds.ySections) {
                    long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                            ? yEndMask : bounds.yEndMask;
                    for (int a = ySections - 1; a < next.length; a += ySections) {
                        next[a] &= mask;
                    }
                } else if (ySections < bounds.ySections) {
                    for (int a = ySections - 1; a < next.length; a += ySections) {
                        next[a] &= yEndMask;
                    }
                } else {
                    for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                        next[a] &= bounds.yEndMask;
                    }
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }

    /**
     * Like {@link #expand(int)}, but limits expansion to the "on" cells of {@code bounds}. Repeatedly expands in the
     * 4-way directions by one cell simultaneously, and only successfully affects the cells that are adjacent to the
     * previous expansion and are in bounds. This won't skip over gaps in bounds, even if amount is high enough that a
     * call to {@link #expand(int)} would reach past the gap; it will stop at the gap and only pass it if expansion
     * takes it around.
     * @param bounds the set of "on" cells that limits where this can expand into
     * @param amount how far to expand this outward by, in cells
     * @return this, after expanding, for chaining
     */
    public Region flood(Region bounds, int amount)
    {
        int ct = size(), ct2;
        for (int i = 0; i < amount; i++) {
            flood(bounds);
            if(ct == (ct2 = size()))
                break;
            else
                ct = ct2;

        }
        return this;
    }

    /**
     * Repeatedly calls {@link #flood(Region)} {@code amount} times and returns the intermediate steps in a
     * Region array of size {@code amount}. Doesn't modify this Region, and doesn't return it in the array
     * (it may return a copy of it if and only if no flood8way() calls can expand the area). If this fills
     * {@code bounds} as fully as possible and still has steps left, the remaining steps are all copies of the
     * fully-filled area.
     * @param bounds the set of "on" cells that this will attempt to fill in steps
     * @param amount how many steps to flood outward, and the size of the array to return
     * @return an array of Region, {@code amount} in size, containing larger and larger expansions of this
     */
    public Region[] floodSeries(Region bounds, int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            int ct = size(), ct2;
            Region[] regions = new Region[amount];
            boolean done = false;
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                if (done) {
                    regions[i] = new Region(temp);
                } else {
                    regions[i] = new Region(temp.flood(bounds));
                    if (ct == (ct2 = temp.size()))
                        done = true;
                    else
                        ct = ct2;
                }
            }
            result = regions;
        }
        return result;
    }

    /**
     * Repeatedly generates new Regions, each one cell expanded in 4 directions from the previous Region
     * and staying inside the "on" cells of {@code bounds}, until it can't expand any more. Returns an ObjectList of the
     * Region steps this generated; this list does not include this Region (or any unmodified copy of this
     * Region), and this method does not modify it.
     * @param bounds the set of "on" cells that this will attempt to fill in steps
     * @return an ObjectList of steps from one {@link #flood(Region)} call to possibly many chained after it
     */
    public ObjectList<Region> floodSeriesToLimit(Region bounds) {
        int ct = size(), ct2;
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (true) {
            temp.flood(bounds);
            if (ct == (ct2 = temp.size()))
                return regions;
            else {
                ct = ct2;
                regions.add(new Region(temp));
            }
        }
    }

    /**
     * Like {@link #expand8way()}, but limits expansion to the "on" cells of {@code bounds}. Expands in all directions
     * by one cell simultaneously, and only successfully affects the cells that are adjacent to this and are in bounds.
     * @param bounds the set of "on" cells that limits where this can expand into
     * @return this, after expanding, for chaining
     */
    public Region flood8way(Region bounds) {
        Region result = this;
        if (width < 2 || ySections <= 0 || bounds == null || bounds.width < 2 || bounds.ySections <= 0) {
        } else {
            final long[] next = new long[width * ySections];
            for (int a = 0; a < ySections && a < bounds.ySections; a++) {
                next[a] |= (data[a] | (data[a] << 1) | (data[a] >>> 1)
                        | data[a + ySections] | (data[a + ySections] << 1) | (data[a + ySections] >>> 1)) & bounds.data[a];
                next[(width - 1) * ySections + a] |= (data[(width - 1) * ySections + a]
                        | (data[(width - 1) * ySections + a] << 1) | (data[(width - 1) * ySections + a] >>> 1)
                        | data[(width - 2) * ySections + a] | (data[(width - 2) * ySections + a] << 1) | (data[(width - 2) * ySections + a] >>> 1))
                        & bounds.data[(width - 1) * bounds.ySections + a];

                for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections &&
                        j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
                    next[i] |= (data[i] | (data[i] << 1) | (data[i] >>> 1)
                            | data[i - ySections] | (data[i - ySections] << 1) | (data[i - ySections] >>> 1)
                            | data[i + ySections] | (data[i + ySections] << 1) | (data[i + ySections] >>> 1))
                            & bounds.data[j];
                }

                if (a > 0) {
                    for (int i = ySections + a, j = bounds.ySections + a; i < (width - 1) * ySections && j < (bounds.width - 1) * bounds.ySections;
                         i += ySections, j += bounds.ySections) {
                        next[i] |= (data[i] | ((data[i - 1] & 0x8000000000000000L) >>> 63) |
                                ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63) |
                                ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63)) & bounds.data[j];
                    }
                }

                if (a < ySections - 1 && a < bounds.ySections - 1) {
                    for (int i = ySections + a, j = bounds.ySections + a;
                         i < (width - 1) * ySections && j < (bounds.width - 1) * bounds.ySections; i += ySections, j += bounds.ySections) {
                        next[i] |= (data[i] | ((data[i + 1] & 1L) << 63) |
                                ((data[i - ySections + 1] & 1L) << 63) |
                                ((data[i + ySections + 1] & 1L) << 63)) & bounds.data[j];
                    }
                }
            }
            if (yEndMask != -1 && bounds.yEndMask != -1) {
                if (ySections == bounds.ySections) {
                    long mask = ((yEndMask >>> 1) <= (bounds.yEndMask >>> 1))
                            ? yEndMask : bounds.yEndMask;
                    for (int a = ySections - 1; a < next.length; a += ySections) {
                        next[a] &= mask;
                    }
                } else if (ySections < bounds.ySections) {
                    for (int a = ySections - 1; a < next.length; a += ySections) {
                        next[a] &= yEndMask;
                    }
                } else {
                    for (int a = bounds.ySections - 1; a < next.length; a += ySections) {
                        next[a] &= bounds.yEndMask;
                    }
                }
            }
            data = next;
            tallied = false;
        }

        return result;
    }
    /**
     * Like {@link #expand8way(int)}, but limits expansion to the "on" cells of {@code bounds}. Repeatedly expands in
     * all directions by one cell simultaneously, and only successfully affects the cells that are adjacent to the
     * previous expansion and are in bounds. This won't skip over gaps in bounds, even if amount is high enough that a
     * call to {@link #expand8way(int)} would reach past the gap; it will stop at the gap and only pass it if expansion
     * takes it around.
     * @param bounds the set of "on" cells that limits where this can expand into
     * @param amount how far to expand this outward by, in cells
     * @return this, after expanding, for chaining
     */
    public Region flood8way(Region bounds, int amount)
    {
        int ct = size(), ct2;
        for (int i = 0; i < amount; i++) {
            flood8way(bounds);
            if(ct == (ct2 = size()))
                break;
            else
                ct = ct2;
        }
        return this;
    }

    /**
     * Repeatedly calls {@link #flood8way(Region)} {@code amount} times and returns the intermediate steps in a
     * Region array of size {@code amount}. Doesn't modify this Region, and doesn't return it in the array
     * (it may return a copy of it if and only if no flood8way() calls can expand the area). If this fills
     * {@code bounds} as fully as possible and still has steps left, the remaining steps are all copies of the
     * fully-filled area.
     * @param bounds the set of "on" cells that this will attempt to fill in steps
     * @param amount how many steps to flood outward, and the size of the array to return
     * @return an array of Region, {@code amount} in size, containing larger and larger expansions of this
     */
    public Region[] floodSeries8way(Region bounds, int amount) {
        Region[] result;
        if (amount <= 0) {
            result = new Region[0];
        } else {
            int ct = size(), ct2;
            Region[] regions = new Region[amount];
            boolean done = false;
            Region temp = new Region(this);
            for (int i = 0; i < amount; i++) {
                if (done) {
                    regions[i] = new Region(temp);
                } else {
                    regions[i] = new Region(temp.flood8way(bounds));
                    if (ct == (ct2 = temp.size()))
                        done = true;
                    else
                        ct = ct2;
                }
            }
            result = regions;
        }
        return result;
    }

    /**
     * Repeatedly generates new Regions, each one cell expanded in 8 directions from the previous Region
     * and staying inside the "on" cells of {@code bounds}, until it can't expand any more. Returns an ObjectList of the
     * Region steps this generated; this list does not include this Region (or any unmodified copy of this
     * Region), and this method does not modify it.
     * @param bounds the set of "on" cells that this will attempt to fill in steps
     * @return an ObjectList of steps from one {@link #flood8way(Region)} call to possibly many chained after it
     */
    public ObjectList<Region> floodSeriesToLimit8way(Region bounds) {
        int ct = size(), ct2;
        ObjectList<Region> regions = new ObjectList<>();
        Region temp = new Region(this);
        while (true) {
            temp.flood8way(bounds);
            if (ct == (ct2 = temp.size()))
                return regions;
            else {
                ct = ct2;
                regions.add(new Region(temp));
            }
        }
    }

    /**
     * A randomized flood-fill that modifies this Region so it randomly adds one adjacent cell if it can while staying
     * inside the "on" cells of {@code bounds}. This Region acts as the initial state, and often contains just one cell
     * before this is called. This method is useful for imitating the movement of fluids like water or smoke within some
     * boundaries, stepping one-cell-at-a-time instead of how {@link #spill(Region, int, EnhancedRandom)} fills a whole volume.
     * @param bounds this Region will only expand to a cell that is "on" in bounds; bounds should overlap with this
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @return this, after expanding randomly once, for chaining
     */
    public Region splash(Region bounds, EnhancedRandom rng) {
        if (width >= 2 && ySections > 0 && bounds != null && bounds.width >= 2 && bounds.ySections > 0) {
            insert(new Region(this).fringe().and(bounds).singleRandom(rng));
        }
        return this;
    }

    /**
     * A randomized flood-fill that modifies this Region so it randomly adds adjacent cells while staying inside
     * the "on" cells of {@code bounds}, until {@link #size()} is equal to {@code volume} or there are no more cells
     * this can expand into. This Region acts as the initial state, and often contains just one cell before this
     * is called. This method is useful for imitating the movement of fluids like water or smoke within some boundaries.
     * @param bounds this Region will only expand to cells that are "on" in bounds; bounds should overlap with this
     * @param volume the maximum {@link #size()} this Region can reach before this stops expanding
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @return this, after expanding randomly, for chaining
     */
    public Region spill(Region bounds, int volume, EnhancedRandom rng) {
        return spill(bounds, volume, rng, null, null);
    }
    /**
     * A randomized flood-fill that modifies this Region so it randomly adds adjacent cells while staying inside
     * the "on" cells of {@code bounds}, until {@link #size()} is equal to {@code volume} or there are no more cells
     * this can expand into. This Region acts as the initial state, and often contains just one cell before this
     * is called. This method is useful for imitating the movement of fluids like water or smoke within some boundaries.
     * <br>
     * This overload is just like the one that doesn't take a temp Region, but it can avoid allocating a new Region if
     * you have one with the same size as this, to use as working space.
     * @param bounds this Region will only expand to cells that are "on" in bounds; bounds should overlap with this
     * @param volume the maximum {@link #size()} this Region can reach before this stops expanding
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @param temp another Region that will be cleared and used as a temporary buffer; optimally the same size as this
     * @param temp2 another Region that will be cleared and used as a temporary buffer; optimally the same size as this
     * @return this, after expanding randomly, for chaining
     */
    public Region spill(Region bounds, int volume, EnhancedRandom rng, Region temp, Region temp2) {
        Region result = this;
        if (width >= 2 && ySections > 0 && bounds != null && bounds.width >= 2 && bounds.ySections > 0) {
            int current = size();
            if (current < volume) {
                if(temp == null) temp = new Region(this);
                else temp.remake(this);
                if(temp2 == null) temp2 = new Region(this);
                else temp2.remake(this);
                temp.notAnd(bounds);
                long[] boundsData = temp.data;
                temp2.remake(this).fringe().and(bounds).tally();
                if (temp2.ct > 0) {
                    Coord c;
                    int x, y, p;
                    for (int i = current; i < volume; i++) {
                        c = temp2.singleRandom(rng);
                        x = c.x;
                        y = c.y;
                        if (data[p = x * ySections + (y >> 6)] != (data[p] |= 1L << (y & 63))) {
                            counts[p]++;
                            for (int j = p + 1; j < data.length; j++) {
                                if (counts[j] > 0) ++counts[j];
                            }
                            ct++;
                            temp2.data[p] &= ~(1L << (y & 63));
                            if (x < width - 1 && (boundsData[p = (x + 1) * ySections + (y >> 6)] & 1L << (y & 63)) != 0) {
                                temp2.data[p] |= 1L << (y & 63);
                            }
                            if (y < height - 1 && (boundsData[p = x * ySections + (y + 1 >> 6)] & 1L << (y + 1 & 63)) != 0) {
                                temp2.data[p] |= 1L << (y + 1 & 63);
                            }
                            if (y > 0 && (boundsData[p = x * ySections + (y - 1 >> 6)] & 1L << (y - 1 & 63)) != 0) {
                                temp2.data[p] |= 1L << (y - 1 & 63);
                            }
                            if (x > 0 && (boundsData[p = (x - 1) * ySections + (y >> 6)] & 1L << (y & 63)) != 0) {
                                temp2.data[p] |= 1L << (y & 63);
                            }
                            temp2.tally();
                            if (temp2.ct <= 0) break;
                        }
                    }
                    tallied = false;
                }
            }
        }
        return result;
    }

    /**
     * Where a cell is "on" but forms a right-angle with exactly two orthogonally-adjacent "on" cells and exactly two
     * orthogonally-adjacent "off" cells, this turns each of those cells "off." This won't affect east-west lines of
     * flat "on" cells, nor north-south lines.
     * @return this, after removing right-angle corner "on" cells, for chaining
     */
    public Region removeCorners()
    {
        if(width <= 2 || ySections <= 0)
            return this;

        final long[] next = new long[width * ySections];
        System.arraycopy(data, 0, next, 0, width * ySections);
        for (int a = 0; a < ySections; a++) {
            if(a > 0 && a < ySections - 1) {
                next[a] &= (((data[a] << 1) | ((data[a - 1] & 0x8000000000000000L) >>> 63))
                        & ((data[a] >>> 1) | ((data[a + 1] & 1L) << 63)));
                next[(width - 1) * ySections + a] &= (((data[(width - 1) * ySections + a] << 1)
                        | ((data[(width - 1) * ySections + a - 1] & 0x8000000000000000L) >>> 63))
                        & ((data[(width - 1) * ySections + a] >>> 1)
                        | ((data[(width - 1) * ySections + a + 1] & 1L) << 63)));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else if(a > 0) {
                next[a] &= (((data[a] << 1) | ((data[a - 1] & 0x8000000000000000L) >>> 63))
                        & (data[a] >>> 1));
                next[(width - 1) * ySections + a] &= (((data[(width - 1) * ySections + a] << 1)
                        | ((data[(width - 1) * ySections + a - 1] & 0x8000000000000000L) >>> 63))
                        & (data[(width - 1) * ySections + a] >>> 1));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= (((data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63))
                            & (data[i] >>> 1))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else if(a < ySections - 1) {
                next[a] &= ((data[a] << 1)
                        & ((data[a] >>> 1)
                        | ((data[a + 1] & 1L) << 63)));
                next[(width - 1) * ySections + a] &= ((data[(width - 1) * ySections + a] << 1)
                        & ((data[(width - 1) * ySections + a] >>> 1)
                        | ((data[(width - 1) * ySections + a + 1] & 1L) << 63)));
                for (int i = ySections+a; i < (width - 1) * ySections; i+= ySections) {
                    next[i] &= ((data[i] << 1)
                            & ((data[i] >>> 1) | ((data[i + 1] & 1L) << 63)))
                            | (data[i - ySections]
                            & data[i + ySections]);
                }
            }
            else // only the case when ySections == 1
            {
                next[0] &= (data[0] << 1) & (data[0] >>> 1);
                next[width-1] &= (data[width-1] << 1) & (data[width-1] >>> 1);
                for (int i = 1+a; i < (width - 1); i++) {
                    next[i] &= ((data[i] << 1) & (data[i] >>> 1)) | (data[i - ySections] & data[i + ySections]);
                }
            }
        }

        if(yEndMask != -1) {
            for (int a = ySections - 1; a < next.length; a += ySections) {
                next[a] &= yEndMask;
            }
        }
        data = next;
        tallied = false;
        return this;
    }

    /**
     * If this Region stores multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered separate from each other) and returns it as an element in an
     * ObjectList of Region, with one Region per isolated area. Not to be confused with
     * {@link #split8way()}, which considers diagonally-adjacent cells as part of one region, while this method requires
     * cells to be orthogonally adjacent.
     * <br>
     * Useful when you have, for example, all the rooms in a dungeon with their connecting corridors removed, but want
     * to separate the rooms. You can get the aforementioned data assuming a bare dungeon called map using:
     * <br>
     * {@code Region floors = new Region(map, '.'),
     * rooms = floors.copy().retract8way().flood(floors, 2),
     * corridors = floors.copy().andNot(rooms),
     * doors = rooms.copy().and(corridors.copy().fringe());}
     * <br>
     * You can then get all rooms as separate regions with {@code List<Region> apart = split(rooms);}, or
     * substitute {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors
     * by a radius of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then
     * flood-filling the floors out from the area that produces by 2 cells (4-way this time) to restore the original
     * size of non-corridor areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing
     * the rooms from floors. The example code also gets the doors (which overlap with rooms, not corridors) by finding
     * where the a room and a corridor are adjacent. This technique is used with some enhancements in the RoomFinder
     * class.
     * @return an ObjectList containing each unconnected area from packed as a Region element
     */
    public ObjectList<Region> split()
    {
        ObjectList<Region> scattered = new ObjectList<>(32);
        int fst = firstTight();
        Region remaining = new Region(this);
        while (fst >= 0) {
            Region filled = new Region(width, height).insert(fst).flood(remaining, width * height);
            scattered.add(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return scattered;
    }
    /**
     * If this Region stores multiple unconnected "on" areas, this finds each isolated area (areas that
     * are only adjacent diagonally are considered <b>one area</b> with this) and returns it as an element in an
     * ObjectList of Region, with one Region per isolated area. This should not be confused with
     * {@link #split()}, which is almost identical except that split() considers only orthogonal connections, while this
     * method considers both orthogonal and diagonal connections between cells as joining an area.
     * <br>
     * Useful when you have, for example, all the rooms in a dungeon with their connecting corridors removed, but want
     * to separate the rooms. You can get the aforementioned data assuming a bare dungeon called map using:
     * <br>
     * {@code Region floors = new Region(map, '.'),
     * rooms = floors.copy().retract8way().flood(floors, 2),
     * corridors = floors.copy().andNot(rooms),
     * doors = rooms.copy().and(corridors.copy().fringe());}
     * <br>
     * You can then get all rooms as separate regions with {@code List<Region> apart = split(rooms);}, or
     * substitute {@code split(corridors)} to get the corridors. The room-finding technique works by shrinking floors
     * by a radius of 1 (8-way), which causes thin areas like corridors of 2 or less width to be removed, then
     * flood-filling the floors out from the area that produces by 2 cells (4-way this time) to restore the original
     * size of non-corridor areas (plus some extra to ensure odd shapes are kept). Corridors are obtained by removing
     * the rooms from floors. The example code also gets the doors (which overlap with rooms, not corridors) by finding
     * where the a room and a corridor are adjacent. This technique is used with some enhancements in the RoomFinder
     * class.
     * @return an ObjectList containing each unconnected area from packed as a Region element
     */
    public ObjectList<Region> split8way()
    {
        ObjectList<Region> scattered = new ObjectList<>(32);
        int fst = firstTight();
        Region remaining = new Region(this);
        while (fst >= 0) {
            Region filled = new Region(width, height).insert(fst).flood8way(remaining, width * height);
            scattered.add(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return scattered;
    }

    /**
     * Finds the largest contiguous area of "on" cells in this Region and returns it; does not modify this
     * Region. If there are multiple areas that are all equally large with no larger area, this returns the
     * region it checks first and still is largest (first determined by the same ordering {@link #nth(int)} takes).
     * This may return an empty Region if there are no "on" cells, but it will never return null.
     * Here, contiguous means adjacent on an orthogonal direction, and this doesn't consider diagonally-connected cells
     * as contiguous unless they also have an orthogonal connection.
     * @return a new Region that corresponds to the largest contiguous sub-region of "on" cells in this.
     */
    public Region largestPart()
    {
        int fst = firstTight(), bestSize = 0, currentSize;
        Region remaining = new Region(this), filled = new Region(width, height),
                choice = new Region(width, height);
        while (fst >= 0) {
            filled.empty().insert(fst).flood(remaining, width * height);
            if((currentSize = filled.size()) > bestSize)
            {
                bestSize = currentSize;
                choice.remake(filled);
            }
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return choice;
    }

    /**
     * Finds the largest contiguous area of "on" cells in this Region and returns it; does not modify this
     * Region. If there are multiple areas that are all equally large with no larger area, this returns the
     * region it checks first and still is largest (first determined by the same ordering {@link #nth(int)} takes).
     * This may return an empty Region if there are no "on" cells, but it will never return null.
     * Here, contiguous means adjacent on any 8-way direction, and considers cells as part of a contiguous area even if
     * all connections but one, which can be orthogonal or diagonal, are blocked by "off" cells.
     * @return a new Region that corresponds to the largest contiguous sub-region of "on" cells in this.
     */
    public Region largestPart8way()
    {
        int fst = firstTight(), bestSize = 0, currentSize;
        Region remaining = new Region(this), filled = new Region(width, height),
                choice = new Region(width, height);
        while (fst >= 0) {
            filled.empty().insert(fst).flood8way(remaining, width * height);
            if((currentSize = filled.size()) > bestSize)
            {
                bestSize = currentSize;
                choice.remake(filled);
            }
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return choice;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor upwards when this is called.
     * Up is defined as negative y. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborUp() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = ySections - 1; a >= 0; a--) {
                if (a > 0) {
                    for (int i = a; i < width * ySections; i += ySections) {
                        data[i] = (data[i] << 1) | ((data[i - 1] & 0x8000000000000000L) >>> 63);
                    }
                } else {
                    for (int i = a; i < width * ySections; i += ySections) {
                        data[i] = (data[i] << 1);
                    }
                }
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor downwards when this is called.
     * Down is defined as positive y. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborDown() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = 0; a < ySections; a++) {
                if (a < ySections - 1) {
                    for (int i = a; i < width * ySections; i += ySections) {
                        data[i] = (data[i] >>> 1) | ((data[i + 1] & 1L) << 63);
                    }
                } else {
                    for (int i = a; i < width * ySections; i += ySections) {
                        data[i] = (data[i] >>> 1);
                    }
                }
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor to the left when this is called.
     * Left is defined as negative x. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborLeft() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = 0; a < ySections; a++) {
                for (int i = ySections * (width - 1) + a; i >= ySections; i -= ySections) {
                    data[i] = data[i - ySections];
                }
                data[a] = 0L;
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor to the right when this is called.
     * Right is defined as positive x. Neighbors are "on" cells exactly one cell away. A cell can have a neighbor
     * without itself being on; this is useful when finding the "shadow" cast away from "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborRight() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = 0; a < ySections; a++) {
                for (int i = a; i < (width - 1) * ySections; i += ySections) {
                    data[i] = data[i + ySections];
                }
                data[(width - 1) * ySections + a] = 0L;
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor upwards and to the left when this
     * is called. Up is defined as negative y, left as negative x. Neighbors are "on" cells exactly one cell away. A
     * cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from "on"
     * cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborUpLeft() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = ySections - 1; a >= 0; a--) {
                if (a > 0) {
                    for (int i = ySections * (width - 1) + a; i >= ySections; i -= ySections) {
                        data[i] = (data[i - ySections] << 1) | ((data[i - ySections - 1] & 0x8000000000000000L) >>> 63);
                    }
                    data[a] = 0L;
                } else {
                    for (int i = ySections * (width - 1) + a; i >= ySections; i -= ySections) {
                        data[i] = (data[i - ySections] << 1);
                    }
                    data[a] = 0L;
                }
            }
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor upwards and to the right when
     * this is called. Up is defined as negative y, right as positive x. Neighbors are "on" cells exactly one cell away.
     * A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborUpRight() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = ySections - 1; a >= 0; a--) {
                if (a > 0) {
                    for (int i = a; i < (width - 1) * ySections; i += ySections) {
                        data[i] = (data[i + ySections] << 1) | ((data[i + ySections - 1] & 0x8000000000000000L) >>> 63);
                    }
                    data[(width - 1) * ySections + a] = 0L;
                } else {
                    for (int i = a; i < (width - 1) * ySections; i += ySections) {
                        data[i] = (data[i + ySections] << 1);
                    }
                    data[(width - 1) * ySections + a] = 0L;
                }
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor downwards and to the left when
     * this is called. Down is defined as positive y, left as negative x. Neighbors are "on" cells exactly one cell
     * away. A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborDownLeft() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = 0; a < ySections; a++) {
                if (a < ySections - 1) {
                    for (int i = ySections * (width - 1) + a; i >= ySections; i -= ySections) {
                        data[i] = (data[i - ySections] >>> 1) | ((data[i - ySections + 1] & 1L) << 63);
                    }
                    data[a] = 0L;
                } else {
                    for (int i = ySections * (width - 1) + a; i >= ySections; i -= ySections) {
                        data[i] = (data[i - ySections] >>> 1);
                    }
                    data[a] = 0L;
                }
            }
            tallied = false;
        }
        return result;
    }

    /**
     * Modifies this Region so the only cells that will be "on" have a neighbor downwards and to the right when
     * this is called. Down is defined as positive y, right as positive x. Neighbors are "on" cells exactly one cell
     * away. A cell can have a neighbor without itself being on; this is useful when finding the "shadow" cast away from
     * "on" cells in one direction.
     * @return this, after modifications, for chaining
     */
    public Region neighborDownRight() {
        Region result = this;
        if (width < 2 || ySections <= 0) {
        } else {
            for (int a = 0; a < ySections; a++) {
                if (a < ySections - 1) {
                    for (int i = a; i < (width - 1) * ySections; i += ySections) {
                        data[i] = (data[i + ySections] >>> 1) | ((data[i + ySections + 1] & 1L) << 63);
                    }
                    data[(width - 1) * ySections + a] = 0L;
                } else {
                    for (int i = a; i < (width - 1) * ySections; i += ySections) {
                        data[i] = (data[i + ySections] >>> 1);
                    }
                    data[(width - 1) * ySections + a] = 0L;
                }
            }
            tallied = false;
        }
        return result;
    }

    public Region removeIsolated()
    {
        int fst = firstTight();
        Region remaining = new Region(this), filled = new Region(this);
        while (fst >= 0) {
            filled.empty().insert(fst).flood(remaining, 8);
            if(filled.size() <= 4)
                andNot(filled);
            remaining.andNot(filled);
            fst = remaining.firstTight();
        }
        return this;
    }

    /**
     * Returns true if any cell is "on" in both this Region and in other; returns false otherwise. For example,
     * if (1,1) is "on" in this and (1,1) is "on" in other, this would return true, regardless of other cells.
     * @param other another Region; its size does not have to match this Region's size
     * @return true if this shares any "on" cells with other
     */
    public boolean intersects(Region other) {
        boolean result = false;
        if (other != null) {
            OUTER:
            for (int x = 0; x < width && x < other.width; x++) {
                for (int y = 0; y < ySections && y < other.ySections; y++) {
                    if ((data[x * ySections + y] & other.data[x * ySections + y]) != 0) {
                        result = true;
                        break OUTER;
                    }
                }
            }
        }
        return result;
    }

    public static ObjectOrderedSet<Region> whichContain(int x, int y, Region ... packed)
    {
        ObjectOrderedSet<Region> found = new ObjectOrderedSet<>(packed.length);
        Region tmp;
        for (int i = 0; i < packed.length; i++) {
            if((tmp = packed[i]) != null && tmp.contains(x, y))
                found.add(tmp);
        }
        return found;
    }

    public static ObjectOrderedSet<Region> whichContain(int x, int y, Collection<Region> packed)
    {
        ObjectOrderedSet<Region> found = new ObjectOrderedSet<>(packed.size());
        for (Region tmp : packed) {
            if(tmp != null && tmp.contains(x, y))
                found.add(tmp);
        }
        return found;
    }
    /**
     * Tries to look up the position x,y in each Region in packed; each Region that contains that x,y
     * point is appended into the Collection {@code into}.
     * @param into a Collection of Region that will be modified if this succeeds
     * @param x the x-coordinate to look up
     * @param y the y-coordinate to look up
     * @param packed the array or varargs of Region to try to look up the given position in
     * @return {@code into}, potentially modified
     */
    public static Collection<Region> appendContaining(Collection<Region> into, int x, int y, Region ... packed)
    {
        Region tmp;
        for (int i = 0; i < packed.length; i++) {
            if((tmp = packed[i]) != null && tmp.contains(x, y))
                into.add(tmp);
        }
        return into;
    }

    /**
     * Tries to look up the position x,y in each Region in packed; each Region that contains that x,y
     * point is appended into the Collection {@code into}.
     * @param into a Collection of Region that will be modified if this succeeds
     * @param x the x-coordinate to look up
     * @param y the y-coordinate to look up
     * @param packed the Collection of Region to try to look up the given position in
     * @return {@code into}, potentially modified
     */
    public static Collection<Region> appendContaining(Collection<Region> into, int x, int y, Collection<Region> packed)
    {
        for (Region tmp : packed) {
            if(tmp != null && tmp.contains(x, y))
                into.add(tmp);
        }
        return into;
    }


    public int size()
    {
        if(!tallied)
            tally();
        return ct;
    }

    public Coord fit(float xFraction, float yFraction) {
        Coord result = null;
        boolean finished = false;
        int tmp, xTotal = 0, yTotal = 0, xTarget, yTarget, bestX = -1;
        long t;
        int[] xCounts = new int[width];
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                t = data[x * ySections + s];
                if (t != 0) {
                    tmp = Long.bitCount(t);
                    xCounts[x] += tmp;
                    xTotal += tmp;
                }
            }
        }
        xTarget = (int) (xTotal * xFraction);
        for (int x = 0; x < width; x++) {
            if ((xTarget -= xCounts[x]) < 0) {
                bestX = x;
                yTotal = xCounts[x];
                break;
            }
        }
        if (bestX < 0) {
            result = Coord.get(-1, -1);
        } else {
            yTarget = (int) (yTotal * yFraction);
            for (int s = 0, y = 0; s < ySections; s++) {
                t = data[bestX * ySections + s];
                for (long cy = 1L; cy != 0L && y < height; y++, cy <<= 1) {
                    if ((t & cy) != 0 && --yTarget < 0) {
                        result = Coord.get(bestX, y);
                        finished = true;
                        break;
                    }
                }
                if (finished) break;
            }
            if (!finished) {
                result = Coord.get(-1, -1);
            }
        }

        return result;
    }

    public int[][] fit(int[][] basis, int defaultValue) {
        int[][] result;
        int[][] next = ArrayTools.fill(defaultValue, width, height);
        if (basis == null || basis.length <= 0 || basis[0] == null || basis[0].length <= 0) {
            result = next;
        } else {
            int tmp, xTotal = 0, yTotal, xTarget, yTarget, bestX, oX = basis.length, oY = basis[0].length, ao;
            long t;
            int[] xCounts = new int[width];
            for (int x = 0; x < width; x++) {
                for (int s = 0; s < ySections; s++) {
                    t = data[x * ySections + s] | 0L;
                    if (t != 0) {
                        tmp = Long.bitCount(t);
                        xCounts[x] += tmp;
                        xTotal += tmp;
                    }
                }
            }
            if (xTotal <= 0) {
                result = next;
            } else {
                for (int aX = 0; aX < oX; aX++) {
                    CELL_WISE:
                    for (int aY = 0; aY < oY; aY++) {
                        if ((ao = basis[aX][aY]) == defaultValue)
                            continue;
                        xTarget = xTotal * aX / oX;
                        for (int x = 0; x < width; x++) {
                            if ((xTarget -= xCounts[x]) < 0) {
                                bestX = x;
                                yTotal = xCounts[x];
                                yTarget = yTotal * aY / oY;
                                for (int s = 0, y = 0; s < ySections; s++) {
                                    t = data[bestX * ySections + s] | 0L;
                                    for (long cy = 1L; cy != 0L && y < height; y++, cy <<= 1) {
                                        if ((t & (cy | 0L)) != 0 && --yTarget < 0) {
                                            next[bestX][y] = ao;
                                            continue CELL_WISE;
                                        }
                                    }
                                }
                                continue CELL_WISE;
                            }
                        }

                    }
                }
                result = next;
            }
        }

        return result;
    }

    /**
     * Like {@link #retract()}, this removes the "on" cells that are 4-way-adjacent to any "off" cell, but unlike that
     * method it keeps a fraction of those surface cells, quasi-randomly selecting them. This can be thought of as
     * running {@link #surface()} on a copy of this Region, running {@link #separatedRegionBlue(float)} on that
     * surface with the given fractionKept, taking the original Region and removing its whole surface with
     * {@link #retract()}, then inserting the quasi-randomly-removed surface into this Region to replace its
     * surface with a randomly "damaged" one.
     * @param fractionKept the fraction between 0.0 and 1.0 of how many cells on the outer surface of this to keep "on"
     * @return this for chaining
     */
    public Region fray(float fractionKept)
    {
        Region cpy = new Region(this).retract();
        return xor(cpy).separatedRegionBlue(fractionKept).or(cpy);
    }

    /**
     * Like {@link #retract()}, this removes the "on" cells that are 4-way-adjacent to any "off" cell, but unlike that
     * method it keeps a fraction of those surface cells, randomly selecting them. This can be thought of as running 
     * {@link #surface()} on a copy of this Region, running {@link #deteriorate(EnhancedRandom, float)} on
     * that surface with the given fractionKept, taking the original Region and removing its whole surface with
     * {@link #retract()}, then inserting the randomly-removed surface into this Region to replace its surface
     * with a randomly "damaged" one.
     * @param random a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @param fractionKept the fraction between 0.0 and 1.0 of how many cells on the outer surface of this to keep "on"
     * @return this for chaining
     */
    public Region fray(EnhancedRandom random, float fractionKept)
    {
        Region cpy = new Region(this).retract();
        return xor(cpy).deteriorate(random, fractionKept).or(cpy);
    }

    /**
     * Modifies this Region so it contains a random subset of its previous contents, choosing cells so that the
     * distance between any two "on" cells is at least {@code minimumDistance}, with at least one cell as "on" if any
     * were "on" in this originally. Does not limit the count of "on" cells in the result.
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @param minimumDistance the minimum distance between "on" cells in the result
     * @return this for chaining
     */
    public Region randomScatter(EnhancedRandom rng, int minimumDistance) {
        return randomScatter(rng, minimumDistance, -1);
    }
    /**
     * Modifies this Region so it contains a random subset of its previous contents, choosing cells so that the
     * distance between any two "on" cells is at least {@code minimumDistance}, with at least one cell as "on" if any
     * were "on" in this originally.
     * Restricts the total count of "on" cells after this returns to a maximum of {@code limit} (minimum is 0 if no
     * cells are "on"). If limit is negative, this will not restrict the count.
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @param minimumDistance the minimum distance between "on" cells in the result
     * @param limit the maximum count of "on" cells to keep
     * @return this for chaining
     */
    public Region randomScatter(EnhancedRandom rng, int minimumDistance, int limit) {
        Region result = this;
        int tmp, total = 0, ct;
        tally();
        if (this.ct == 0) {
        } else if (limit == 0) {
            result = empty();
        } else {
            if (limit < 0)
                limit = width * height;
            long t, w;
            long[] data2 = new long[data.length];
            MAIN_LOOP:
            while (total < limit) {
                if (!tallied)
                    tally();
                tmp = rng.nextInt(this.ct);

                for (int s = 0; s < ySections; s++) {
                    for (int x = 0; x < width; x++) {
                        if ((ct = counts[x * ySections + s]) > tmp) {
                            t = data[x * ySections + s];
                            w = BitConversion.lowestOneBit(t);
                            for (--ct; w != 0; ct--) {
                                if (ct == tmp) {
                                    removeRectangle(x - minimumDistance,
                                            ((s << 6) | Long.numberOfTrailingZeros(w)) - minimumDistance,
                                            minimumDistance << 1 | 1, minimumDistance << 1 | 1);
                                    data2[x * ySections + s] |= w;
                                    ++total;
                                    continue MAIN_LOOP;
                                }
                                t ^= w;
                                w = BitConversion.lowestOneBit(t);
                            }
                        }
                    }
                }
                break;
            }
            data = data2;
            tallied = false;
        }
        return result;
    }

    public float rateDensity() {
        float result = 0;
        float sz = height * width;
        if (sz != 0) {
            float onAmount = sz - size(), retractedOn = sz - copy().retract().size();
            result = (onAmount + retractedOn) / (sz * 2f);
        }
        return result;
    }
    public float rateRegularity() {
        float result = 0;
        Region me2 = copy().surface8way();
        float irregularCount = me2.size();
        if (irregularCount != 0) {
            result = me2.remake(this).surface().size() / irregularCount;
        }
        return result;
    }

    private static int median(int[] working, int start, int amount) {
        int result;
        Arrays.sort(working, start, start + amount);
        if ((amount & 1) == 0) {
            result = working[start + (amount >> 1) - 1] + working[start + (amount >> 1)] >>> 1;
        } else {
            result = working[start + (amount >> 1)];
        }
        return result;
    }

    /**
     * Calculates a perceptual hash for this Region using a method that is only precise for some sizes of
     * Region; it writes a result to into, and uses working as a temporary buffer. The lengths of into and
     * working should be related; if into is length 1, then working should be length 64, and though the hash won't be
     * very detailed, it will work well for images with width and height that are multiples of 8; if into is length 4,
     * then working should be length 256, and this will work with more detail on images that have width and height that
     * are multiples of 16. If working is null or is too small, then this won't reuse it and will allocate an
     * appropriately-sized array for internal use.
     * <br>
     * Ported from <a href="https://github.com/commonsmachinery/blockhash/blob/master/blockhash.c">blockhash by commonsmachinery</a>,
     * which is MIT-licensed.
     * @param into should be a long array of length 1 or 4; the contents don't matter and this will be where output is written to
     * @param working should be an int array of length 64 (if into has length 1) or 256 (if into has length 4); may be null if you like garbage collection
     */
    public void perceptualHashQuick(long[] into, int[] working)
    {
        final int bits = 8 << (Integer.numberOfTrailingZeros(Integer.highestOneBit(into.length)) >> 1);
        if(working == null || working.length < bits * bits)
            working = new int[bits * bits];
        final int blockWidth = width / bits, blockHeight = height / bits, blockWidthSections = blockWidth * ySections;
        if(blockHeight == 1)
        {
            for (int y = 0; y < bits; y++) {
                for (int x = 0; x < bits; x++) {
                    int value = 0;
                    for (int ix = 0; ix < blockWidthSections; ix += ySections) {
                        value += (data[x * blockWidthSections + ix + (y >> 6)] >>> (y & 63) & 1L);
                    }
                    working[x * bits + y] = value;
                }
            }
        }
        else if(blockHeight < 64 && Integer.bitCount(blockHeight) == 1) {
            final long yBlockMask = ~(-1L << blockHeight);
            final int divisorMask = (64 / blockHeight) - 1;
            long currentMask;
            int blockY = 0;
            for (int y = 0; y < bits; y++, blockY += blockHeight) {
                currentMask = yBlockMask << ((y & divisorMask) << blockHeight);
                for (int x = 0; x < bits; x++) {
                    int value = 0;
                    for (int ix = 0; ix < blockWidthSections; ix += ySections) {
                        value += Long.bitCount(data[x * blockWidthSections + ix + (blockY >> 6)] & currentMask);
                    }
                    working[x * bits + y] = value;
                }
            }
        }
        final int cellsPerBlock = blockWidth * blockHeight, numBlocks = bits * bits,
                halfCellCount = cellsPerBlock >>> 1;
        int bandSize = numBlocks >>> 2;
        int m, v;
        int currentInto = 0;
        long currentIntoPos = 1L;
        for (int i = 0; i < 4; i++) {
            m = median(working, i * bandSize, bandSize);
            for (int j = i * bandSize; j < (i + 1) * bandSize; j++) {
                v = working[j];
                if(v > m || (v - m == 0 && m > halfCellCount)) into[currentInto] |= currentIntoPos;
                if((currentIntoPos <<= 1) == 0)
                {
                    ++currentInto;
                    currentIntoPos = 1L;
                }
            }
        }
    }

    /*
    // This showed a strong x-y correlation because it didn't have a way to use a non-base-2 van der Corput sequence.
    // It also produced very close-together points, unfortunately.
    public static float quasiRandomX(int idx)
    {
        return atVDCSequence(26 + idx * 5);
    }
    public static float quasiRandomY(int idx)
    {
        return atVDCSequence(19 + idx * 3);
    }

    private static float atVDCSequence(int idx)
    {
        int leading = BitConversion.countLeadingZeros(idx);
        return (Integer.reverse(idx) >>> leading) / (1.0 * (1 << (32 - leading)));
    }
    */
    public Coord[] asCoords()
    {
        return asCoords(new Coord[size()]);

    }
    public Coord[] asCoords(Coord[] points) {
        Coord[] result = null;
        if (points == null)
            points = new Coord[size()];
        int idx = 0, len = points.length;
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((t = data[x * ySections + s]) != 0) {
                    w = BitConversion.lowestOneBit(t);
                    while (w != 0) {
                        if (idx >= len) {
                            result = points;
                            break;
                        }
                        points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                    if (result != null) break;
                }
            }
            if (result != null) break;
        }
        if (result == null) {
            result = points;
        }
        return result;
    }
    public int[] asEncoded()
    {
        int ct = size(), idx = 0;
        int[] points = new int[ct];
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = BitConversion.lowestOneBit(t);
                    while (w != 0) {
                        points[idx++] = Coord.pureEncode(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }
    public int[] asTightEncoded()
    {
        int ct = size(), idx = 0;
        int[] points = new int[ct];
        long t, w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = BitConversion.lowestOneBit(t);
                    while (w != 0) {
                        points[idx++] =  ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }

    /**
     * Gets the first Coord in the iteration order, or (-1,-1) if this Region is empty.
     * @return the first Coord in the iteration order, or (-1,-1) if this Region is empty
     */
    public Coord first()
    {
        long w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((w = (data[x * ySections + s])) != 0) {
                    return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int firstTight()
    {
        long w;
        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((w = (data[x * ySections + s])) != 0) {
                    return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the last Coord in the iteration order, or (-1,-1) if this Region is empty.
     * @return the last Coord in the iteration order, or (-1,-1) if this Region is empty
     */
    public Coord last()
    {
        long w;
        for (int x = width - 1; x >= 0; x--) {
            for (int s = ySections - 1; s >= 0; s--) {
                if ((w = (data[x * ySections + s])) != 0) {
                    return Coord.get(x, (s << 6) | 63 - BitConversion.countLeadingZeros(w));
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int lastTight()
    {
        long w;
        for (int x = width - 1; x >= 0; x--) {
            for (int s = ySections - 1; s >= 0; s--) {
                if ((w = (data[x * ySections + s])) != 0) {
                    return ((s << 6) | 63 - BitConversion.countLeadingZeros(w)) * width + x;
                }
            }
        }
        return -1;
    }

    public Coord nth(final int index)
    {
        if(index < 0)
            return Coord.get(-1, -1);
        int ct = size(), tmp;
        if(index >= ct)
            return Coord.get(-1, -1);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > index) {
                    t = data[x * ySections + s];
                    w = BitConversion.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == index)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public Coord atFraction(final float fraction)
    {
        int ct = size(), tmp;
        if(ct <= 0) return Coord.get(-1, -1);
        tmp = Math.abs((int)(fraction * ct) % ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = BitConversion.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int atFractionTight(final float fraction)
    {
        int ct = size(), tmp;
        if(ct <= 0) return -1;
        tmp = Math.abs((int)(fraction * ct) % ct);
        long t, w;

        for (int x = 0; x < width; x++) {
            for (int s = 0; s < ySections; s++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = BitConversion.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Gets a single random Coord from the "on" positions in this Region, or the Coord (-1,-1) if this is empty.
     * Uses the given EnhancedRandom to generate one random int, which is used as an index. The technique this uses to iterate
     * over bits can be credited to Erling Ellingsen and Daniel Lemire, found
     * <a href="https://lemire.me/blog/2013/12/23/even-faster-bitmap-decoding/">here</a>, and seems to be a little
     * faster than the previous method. The fastest way to get a random Coord from a Region is to avoid iterating
     * over the bits at all, so if your region data doesn't change you should get it as a Coord array with
     * {@link #asCoords()} and call {@link EnhancedRandom#nextInt(int)} to get an index inside that Coord array. If you
     * take the asCoords() call out of consideration, getting random elements out of an array (especially a large one)
     * can be hundreds of times faster.
     * @param rng a EnhancedRandom, or a recommended subclass like {@link WhiskerRandom}
     * @return a single randomly-chosen Coord from the "on" positions in this Region, or (-1,-1) if empty
     */
    public Coord singleRandom(EnhancedRandom rng)
    {
        int ct = size(), tmp = rng.nextInt(ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s] | 0L;
                    for (--ct; t != 0; ct--) {
                        w = BitConversion.lowestOneBit(t);
                        if (ct == tmp)
                            return Coord.get(x, (s << 6) | Long.bitCount(w-1));
                        t ^= w;
                    }
                }
            }
        }
        return Coord.get(-1, -1);
    }

    public int singleRandomTight(EnhancedRandom rng)
    {
        int ct = size(), tmp = rng.nextInt(ct);
        long t, w;
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if ((ct = counts[x * ySections + s]) > tmp) {
                    t = data[x * ySections + s];
                    w = BitConversion.lowestOneBit(t);
                    for (--ct; w != 0; ct--) {
                        if (ct == tmp)
                            return ((s << 6) | Long.numberOfTrailingZeros(w)) * width + x;
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Narrow-purpose; takes an x and a y value, each between 0 and 65535 inclusive, and interleaves their bits so the
     * least significant bit and every other bit after it are filled with the bits of x, while the
     * second-least-significant bit and every other bit after that are filled with the bits of y. Essentially, this
     * takes two numbers with bits labeled like {@code a b c} for x and {@code R S T} for y and makes a number with
     * those bits arranged like {@code R a S b T c}. Numbers made by interleaving other numbers like this are sometimes
     * called Morton codes, and the sequence of Morton codes forms a pattern called the Z-order curve.
     * @param x an int between 0 and 65535, inclusive
     * @param y an int between 0 and 65535, inclusive
     * @return an int that interleaves x and y, with x in the least significant bit position
     */
    public static int interleaveBits(int x, int y)
    {
        x |= y << 16;
        x =    ((x & 0x0000ff00) << 8) | ((x >>> 8) & 0x0000ff00) | (x & 0xff0000ff);
        x =    ((x & 0x00f000f0) << 4) | ((x >>> 4) & 0x00f000f0) | (x & 0xf00ff00f);
        x =    ((x & 0x0c0c0c0c) << 2) | ((x >>> 2) & 0x0c0c0c0c) | (x & 0xc3c3c3c3);
        return ((x & 0x22222222) << 1) | ((x >>> 1) & 0x22222222) | (x & 0x99999999);
    }

    /**
     * Narrow-purpose; takes an int that represents a distance down the Z-order curve and moves its bits around so that
     * its x component is stored in the bottom 16 bits (use {@code (n & 0xffff)} to obtain) and its y component is
     * stored in the upper 16 bits (use {@code (n >>> 16)} to obtain). This may be useful for ordering traversals of all
     * points in a Region less predictably. Numbers made by interleaving other numbers, like {@code n}, are sometimes
     * called Morton codes, and the sequence of Morton codes forms a pattern called the Z-order curve.
     * @param n an int that has already been interleaved, though this can really be any int
     * @return an int with x in its lower bits ({@code x = n & 0xffff;}) and y in its upper bits ({@code y = n >>> 16;})
     */
    public static int disperseBits(int n)
    {
        n =    ((n & 0x22222222) << 1) | ((n >>> 1) & 0x22222222) | (n & 0x99999999);
        n =    ((n & 0x0c0c0c0c) << 2) | ((n >>> 2) & 0x0c0c0c0c) | (n & 0xc3c3c3c3);
        n =    ((n & 0x00f000f0) << 4) | ((n >>> 4) & 0x00f000f0) | (n & 0xf00ff00f);
        return ((n & 0x0000ff00) << 8) | ((n >>> 8) & 0x0000ff00) | (n & 0xff0000ff);
    }

    /**
     * Gets a Coord array from the "on" contents of this Region, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it uses blue noise to sharply limit the likelihood of nearby points being
     * chosen when fraction is small. If you request too many cells (too high of a value for fraction), it will start to
     * have nearby cells, however. Does not restrict the size of the returned array other than only using up to
     * {@code fraction * size()} cells.
     * <br>
     * Internally, this is a thin wrapper around {@link #separatedRegionBlue(float, int)}, and won't be more efficient
     * than that method.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] separatedBlue(float fraction)
    {
        return separatedBlue(fraction, -1);
    }

    /**
     * Gets a Coord array from the "on" contents of this Region, using a quasi-random scattering of chosen cells
     * with a count that matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it uses blue noise to sharply limit the likelihood of nearby points being
     * chosen when fraction is small. If you request too many cells (too high of a value for fraction), it will start to
     * have nearby cells, however. Restricts the total size of the returned array to a maximum of {@code limit} (minimum
     * is 0 if no cells are "on"). If limit is negative, this will not restrict the size.
     * <br>
     * Internally, this is a thin wrapper around {@link #separatedRegionBlue(float, int)}, and won't be more efficient
     * than that method.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the array to return; may return less
     * @return a freshly-allocated Coord array containing the quasi-random cells
     */
    public Coord[] separatedBlue(float fraction, int limit)
    {
        if(fraction <= 0)
            return new Coord[0];
        if(fraction > 0xFFp-8f)
            fraction = 0xFFp-8f;
        int ct = size();
        if(limit >= ct)
            return asCoords();
        return copy().separatedRegionBlue(fraction, limit).asCoords();
    }
    /**
     * Modifies this Region so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it uses blue noise to sharply limit the likelihood of nearby points being
     * chosen when fraction is small. If you request too many cells (too high of a value for fraction), it will start to
     * have nearby cells, however. Does not restrict the size of the returned Region other than only using up to
     * {@code fraction * size()} cells.
     * @param fraction the fraction of "on" cells to randomly select, between 0.0 and 1.0
     * @return this, after modifications, for chaining
     */
    public Region separatedRegionBlue(float fraction)
    {
        return separatedRegionBlue(fraction, -1);
    }

    /**
     * Modifies this Region so it contains a quasi-random subset of its previous contents, choosing cells so that
     * the {@link #size()} matches the given {@code fraction} of the total amount of "on" cells in this. This is quasi-
     * random instead of pseudo-random because it uses blue noise to sharply limit the likelihood of nearby points being
     * chosen when fraction is small. If you request too many cells (too high of a value for fraction), it will start to
     * have nearby cells, however. Restricts the total size of the returned Region to a maximum of {@code limit}
     * (minimum is 0 if no cells are "on"). If limit is negative, this will not restrict the size.
     * @param fraction the fraction of "on" cells to quasi-randomly select, between 0.0 and 1.0
     * @param limit the maximum size of the Region to return; a negative number will make this have no limit
     * @return this, after modifications, for chaining
     */
    public Region separatedRegionBlue(float fraction, int limit)
    {
        if(fraction <= 0)
            return empty();
        if(fraction > 0xFFp-8f)
            fraction = 0xFFp-8f;
        int ct = size();
        if(limit >= ct)
            return this;
        ct *= fraction;
        if(limit >= 0 && limit < ct)
            ct = limit;
        for (int i = 255; i >= 0; i--) {
            if(ct >= andWrapping64(BLUE_LEVELS[i]).size())
                return this;
        }
        return this;
    }

    /**
     * Gets all but a quasi-random group of "on" cells that each has at least minimumDistance between itself and any
     * other cell in the returned array. Works on a copy of this Region, so this object won't be modified.
     * @param rng an EnhancedRandom to randomly choose points; may be queried quite a lot for large Regions
     * @param minimumDistance the minimum distance between points to allow; this is a float, and distances will be rounded
     * @return a new Coord array containing some of the "on" cells in this Region
     */
    public Coord[] separatedPoisson(EnhancedRandom rng, float minimumDistance) {
        return PoissonDisk.separatedPoisson(new Region(this), minimumDistance, rng, -1).asCoords();
    }

    /**
     * Gets all but a quasi-random group of "on" cells that each has at least minimumDistance between itself and any
     * other cell in the returned array. Works on a copy of this Region, so this object won't be modified. This overload
     * takes a limit; if limit is non-negative, it will be used as the maximum number of "on" cells to permit in the
     * result. When there would otherwise be more "on" cells than the limit, this randomly chooses which ones to keep.
     * @param rng an EnhancedRandom to randomly choose points; may be queried quite a lot for large Regions
     * @param minimumDistance the minimum distance between points to allow; this is a float, and distances will be rounded
     * @param limit the maximum size of the array to return; a negative number will make this have no limit
     * @return a new Coord array containing some of the "on" cells in this Region
     */
    public Coord[] separatedPoisson(EnhancedRandom rng, float minimumDistance, int limit) {
        return PoissonDisk.separatedPoisson(new Region(this), minimumDistance, rng, limit).asCoords();
    }

    /**
     * Removes all but a quasi-random group of "on" cells that each has at least minimumDistance between itself and any
     * other cell in the returned Region. Modifies this Region in-place.
     * @param rng an EnhancedRandom to randomly choose points; may be queried quite a lot for large Regions
     * @param minimumDistance the minimum distance between points to allow; this is a float, and distances will be rounded
     * @return this, after modifications, for chaining
     */
    public Region separatedRegionPoisson(EnhancedRandom rng, float minimumDistance) {
        return PoissonDisk.separatedPoisson(this, minimumDistance, rng, -1);
    }

    /**
     * Removes all but a quasi-random group of "on" cells that each has at least minimumDistance between itself and any
     * other cell in the returned Region. Modifies this Region in-place. This overload takes a limit; if limit is
     * non-negative, it will be used as the maximum number of "on" cells to permit in the result. When there would
     * otherwise be more "on" cells than the limit, this randomly chooses which ones to keep.
     * @param rng an EnhancedRandom to randomly choose points; may be queried quite a lot for large Regions
     * @param minimumDistance the minimum distance between points to allow; this is a float, and distances will be rounded
     * @param limit the maximum size of the Region to return; a negative number will make this have no limit
     * @return this, after modifications, for chaining
     */
    public Region separatedRegionPoisson(EnhancedRandom rng, float minimumDistance, int limit) {
        return PoissonDisk.separatedPoisson(this, minimumDistance, rng, limit);
    }

    public Coord[] randomPortion(EnhancedRandom rng, int size)
    {
        int ct = size(), idx = 0, run = 0;
        if(ct <= 0 || size <= 0)
            return new Coord[0];
        if(ct <= size)
            return asCoords();
        Coord[] points = new Coord[size];
        int[] order = ArrayTools.range(ct);
        rng.shuffle(order);
        Arrays.sort(order, 0, size);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = BitConversion.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            points[idx++] = Coord.get(x, (s << 6) | Long.numberOfTrailingZeros(w));
                            if (idx >= size) break ALL;
                        }
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        return points;
    }

    public Region randomRegion(EnhancedRandom rng, int size)
    {
        int ct = size(), idx = 0, run = 0;
        if(ct <= 0 || size <= 0)
            return empty();
        if(ct <= size)
            return this;
        int[] order = ArrayTools.range(ct);
        rng.shuffle(order);
        Arrays.sort(order, 0, size);
        long t, w;
        ALL:
        for (int s = 0; s < ySections; s++) {
            for (int x = 0; x < width; x++) {
                if((t = data[x * ySections + s]) != 0)
                {
                    w = BitConversion.lowestOneBit(t);
                    while (w != 0) {
                        if (run++ == order[idx]) {
                            if(++idx >= size) break ALL;
                        }
                        else
                        {
                            data[x * ySections + s] &= ~(1L << Long.numberOfTrailingZeros(w));
                        }
                        t ^= w;
                        w = BitConversion.lowestOneBit(t);
                    }
                }
            }
        }
        tallied = false;
        return this;
    }

    public boolean contains(int x, int y)
    {
        return x >= 0 && y >= 0 && x < width && y < height && ySections > 0 &&
                ((data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0);
    }


    /**
     * @return Whether this zone is empty.
     */
    @Override
    public boolean isEmpty() {
        if(tallied) return ct > 0;
        for (int i = 0; i < data.length; i++) {
            if(data[i] != 0L) return false;
        }
        return true;
    }

    /**
     * Generates a 2D int array from an array or vararg of Regions, starting at all 0 and adding 1 to the int at
     * a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions an array or vararg of Regions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the number of given Regions that had an "on" cell at that position
     */
    public static int[][] sum(Region... regions)
    {
        if(regions == null || regions.length <= 0)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D int array from a List of Regions, starting at all 0 and adding 1 to the int at
     * a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions a List of Regions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the number of given Regions that had an "on" cell at that position
     */
    public static int[][] sum(List<Region> regions)
    {
        if(regions == null || regions.isEmpty())
            return new int[0][0];
        Region t = regions.get(0);
        int w = t.width, h = t.height, l = regions.size(), ys = t.ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D float array from an array or vararg of Regions, starting at all 0 and adding 1 to the float at
     * a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions an array or vararg of Regions; must all have the same width and height
     * @return a 2D float array with the same width and height as the regions, where an float cell equals the number of given Regions that had an "on" cell at that position
     */
    public static float[][] sumFloat(Region... regions)
    {
        if(regions == null || regions.length <= 0)
            return new float[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        float[][] numbers = new float[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1f : 0f;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D float array from a List of Regions, starting at all 0 and adding 1 to the float at
     * a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can produce any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * produce any number between 0 and 16 in a cell.
     * @param regions a List of Regions; must all have the same width and height
     * @return a 2D float array with the same width and height as the regions, where an float cell equals the number of given Regions that had an "on" cell at that position
     */
    public static float[][] sumFloat(List<Region> regions)
    {
        if(regions == null || regions.isEmpty())
            return new float[0][0];
        Region t = regions.get(0);
        int w = t.width, h = t.height, l = regions.size(), ys = t.ySections;
        float[][] numbers = new float[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions.get(i).data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1f : 0f;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D int array from an array of Regions and an array of weights, starting the 2D result at all 0
     * and, for every Region that has that cell as "on," adding the int in the corresponding weights array at
     * the position of that cell. This means if you give an array of 4 Regions to this method along with the
     * weights {@code 1, 2, 3, 4}, it can produce a number between 0 and 10 in a cell (where 10 is used when all 4
     * Regions have a cell "on," since {@code 1 + 2 + 3 + 4 == 10}); if the weights are instead
     * {@code 1, 10, 100, 1000}, then the results can vary between 0 and 1111, where 1111 is only if all Regions
     * have a cell as "on." The weights array must have a length at least equal to the length of the regions array.
     * @param regions an array of Regions; must all have the same width and height
     * @param weights an array of ints; must have length at least equal to regions' length
     * @return a 2D int array with the same width and height as the regions, where an int cell equals the sum of the weights corresponding to Regions that had an "on" cell at that position
     */
    public static int[][] sumWeighted(Region[] regions, int[] weights)
    {
        if(regions == null || regions.length <= 0 || weights == null || weights.length < regions.length)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? weights[i] : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * Generates a 2D float array from an array of Regions and an array of weights, starting the 2D result at
     * all 0 and, for every Region that has that cell as "on," adding the float in the corresponding weights
     * array at the position of that cell. This means if you give an array of 4 Regions to this method along with
     * the weights {@code 1, 2, 3, 4}, it can produce a number between 0 and 10 in a cell (where 10 is used when all 4
     * Regions have a cell "on," since {@code 1 + 2 + 3 + 4 == 10}); if the weights are instead
     * {@code 1, 10, 100, 1000}, then the results can vary between 0 and 1111, where 1111 is only if all Regions
     * have a cell as "on." The weights array must have a length at least equal to the length of the regions array.
     * @param regions an array of Regions; must all have the same width and height
     * @param weights an array of doubles; must have length at least equal to regions' length
     * @return a 2D float array with the same width and height as the regions, where an float cell equals the sum of the weights corresponding to Regions that had an "on" cell at that position
     */
    public static float[][] sumWeightedFloat(Region[] regions, float[] weights)
    {
        if(regions == null || regions.length <= 0 || weights == null || weights.length < regions.length)
            return new float[0][0];
        int w = regions[0].width, h = regions[0].height, l = regions.length, ys = regions[0].ySections;
        float[][] numbers = new float[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? weights[i] : 0.0;
                }
            }
        }
        return numbers;
    }

    /**
     * Adds to an existing 2D int array with an array or vararg of Regions, adding 1 to the int in existing at
     * a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can increment by any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * increase the value in existing by any number between 0 and 16 in a cell.
     * @param existing a non-null 2D int array that will have each cell incremented by the sum of the Regions
     * @param regions an array or vararg of Regions; must all have the same width and height
     * @return existing, after modification, where an int cell will be changed by the number of given Regions that had an "on" cell at that position
     */
    public static int[][] sumInto(int[][] existing, Region... regions)
    {
        if(regions == null || regions.length <= 0 || existing == null || existing.length == 0 || existing[0].length == 0)
            return existing;
        int w = existing.length, h = existing[0].length, l = regions.length, ys;
        for (int i = 0; i < l; i++) {
            Region region = regions[i];
            ys = region.ySections;
            for (int x = 0; x < w && x < region.width; x++) {
                for (int y = 0; y < h && y < region.height; y++) {
                    existing[x][y] += (region.data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 : 0;
                }
            }
        }
        return existing;
    }


    /**
     * Adds to an existing 2D float array with an array or vararg of Regions, adding 1 to the float in existing
     * at a position once for every Region that has that cell as "on." This means if you give 8 Regions to
     * this method, it can increment by any number between 0 and 8 in a cell; if you give 16 Regions, then it can
     * increase the value in existing by any number between 0 and 16 in a cell.
     * @param existing a non-null 2D float array that will have each cell incremented by the sum of the Regions
     * @param regions an array or vararg of Regions; must all have the same width and height
     * @return existing, after modification, where a float cell will be changed by the number of given Regions that had an "on" cell at that position
     */
    public static float[][] sumIntoFloat(float[][] existing, Region... regions)
    {
        if(regions == null || regions.length <= 0 || existing == null || existing.length == 0 || existing[0].length == 0)
            return existing;
        int w = existing.length, h = existing[0].length, l = regions.length, ys = regions[0].ySections;
        for (int i = 0; i < l; i++) {
            for (int x = 0; x < w && x < regions[i].width; x++) {
                for (int y = 0; y < h && y < regions[i].height; y++) {
                    existing[x][y] += (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1.0 : 0.0;
                }
            }
        }
        return existing;
    }

    /**
     * Generates a 2D int array from an array or vararg of Regions, treating each cell in the nth region as the
     * nth bit of the int at the corresponding x,y cell in the int array. This means if you give 8 Regions to
     * this method, it can produce any 8-bit number in a cell (0-255); if you give 16 Regions, then it can
     * produce any 16-bit number (0-65535).
     * @param regions an array or vararg of Regions; must all have the same width and height
     * @return a 2D int array with the same width and height as the regions, with bits per int taken from the regions
     */
    public static int[][] bitSum(Region... regions)
    {
        if(regions == null || regions.length <= 0)
            return new int[0][0];
        int w = regions[0].width, h = regions[0].height, l = Math.min(32, regions.length), ys = regions[0].ySections;
        int[][] numbers = new int[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                for (int i = 0; i < l; i++) {
                    numbers[x][y] |= (regions[i].data[x * ys + (y >> 6)] & (1L << (y & 63))) != 0 ? 1 << i : 0;
                }
            }
        }
        return numbers;
    }

    /**
     * A crude approximation of DijkstraMap's scan() method for when DijkstraMap isn't available.
     * Starting at {@code goal}, this floods outward (4-way) as with {@link #flood(Region)}, modifying
     * {@code into} so each cell reached during the course of repeated flooding has an int value equal to the
     * (Manhattan) distance from {@code goal} to that cell, and all other cells will be set to
     * {@link Integer#MAX_VALUE}. This will call {@link #flood(Region)} until it can't reach any more cells.
     * This returns {@code into}, after modifications.
     * @param into a 2D int array that will have all reachable cells assigned their distance from goal, and all others to {@link Integer#MAX_VALUE}
     * @param goal the starting point for the scan/flood-fill; {@code into} will be assigned 0 at this point
     * @return {@code into}, after modifications
     */
    public int[][] dijkstraScan(int[][] into, Coord goal) {
        return dijkstraScan(into, goal, Integer.MAX_VALUE);
    }
    /**
     * A crude approximation of DijkstraMap's scan() method for when DijkstraMap isn't available.
     * Starting at {@code goal}, this floods outward (4-way) as with {@link #flood(Region)}, modifying
     * {@code into} so each cell reached during the course of repeated flooding has an int value equal to the
     * (Manhattan) distance from {@code goal} to that cell, and all other cells will be set to
     * {@link Integer#MAX_VALUE}. The {@code limit} is the exclusive upper bound for the greatest distance from
     * the {@code goal} this will change; this will call {@link #flood(Region)} at most {@code limit} times, and
     * can call it fewer times if it tries to flood but can't reach any more cells. This returns {@code into},
     * after modifications.
     * @param into a 2D int array that will have all reachable cells assigned their distance from goal, and all others to {@link Integer#MAX_VALUE}
     * @param goal the starting point for the scan/flood-fill; {@code into} will be assigned 0 at this point
     * @param limit the exclusive upper bound on the distance this can travel from the {@code goal}
     * @return {@code into}, after modifications
     */
    public int[][] dijkstraScan(int[][] into, Coord goal, int limit){
        Region goals = new Region(goal, width, height);
        ArrayTools.fill(into, Integer.MAX_VALUE);
        for (int i = 0; i < limit; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if((goals.data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 && into[x][y] > i)
                        into[x][y] = i;
                }
            }
            int sz = goals.size();
            goals.flood(this);
            if(sz == goals.size()) break;
        }
        return into;
    }
    /**
     * A crude approximation of DijkstraMap's scan() method for when DijkstraMap isn't available.
     * Starting at each "on" cell in {@code goals}, this floods outward (4-way) as with {@link #flood(Region)},
     * modifying {@code into} so each cell reached during the course of repeated flooding has an int value equal to the
     * (Manhattan) distance from {@code goal} to that cell, and all other cells will be set to
     * {@link Integer#MAX_VALUE}. This will call {@link #flood(Region)} until it can't reach any more cells. Note, this
     * modifies both {@code into} and {@code goals}, but usually the result in {@code goals} isn't very useful (you may
     * want to get its {@link #size()} to know how many cells were affected, but it can't tell you the distance for any
     * particular cell). This returns {@code into}, after modifications.
     * @param into a 2D int array that will have all reachable cells assigned their distance from goal, and all others to {@link Integer#MAX_VALUE}
     * @param goals the starting points for the scan/flood-fill; will be modified by repeatedly calling flood()
     * @return {@code into}, after modifications
     */
    public int[][] dijkstraScan(int[][] into, Region goals){
        return dijkstraScan(into, goals, Integer.MAX_VALUE);
    }
    /**
     * A crude approximation of DijkstraMap's scan() method for when DijkstraMap isn't available.
     * Starting at each "on" cell in {@code goals}, this floods outward (4-way) as with {@link #flood(Region)},
     * modifying {@code into} so each cell reached during the course of repeated flooding has an int value equal to the
     * (Manhattan) distance from {@code goal} to that cell, and all other cells will be set to
     * {@link Integer#MAX_VALUE}. The {@code limit} is the exclusive upper bound for the greatest distance from a goal
     * this will change; this will call {@link #flood(Region)} at most {@code limit} times, and can call it fewer times
     * if it tries to flood but can't reach any more cells. Note, this modifies both {@code into} and {@code goals}, but
     * usually the result in {@code goals} isn't very useful (you may want to get its {@link #size()} to know how many
     * cells were affected, but it can't tell you the distance for any particular cell). This returns {@code into},
     * after modifications.
     * @param into a 2D int array that will have all reachable cells assigned their distance from goal, and all others to {@link Integer#MAX_VALUE}
     * @param goals the starting points for the scan/flood-fill; will be modified by repeatedly calling flood()
     * @param limit the exclusive upper bound on the distance this can travel from the {@code goal}
     * @return {@code into}, after modifications
     */
    public int[][] dijkstraScan(int[][] into, Region goals, int limit){
        ArrayTools.fill(into, Integer.MAX_VALUE);
        for (int i = 0; i < limit; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if((goals.data[x * ySections + (y >> 6)] & (1L << (y & 63))) != 0 && into[x][y] > i)
                        into[x][y] = i;
                }
            }
            int sz = goals.size();
            goals.flood(this);
            if(sz == goals.size()) break;
        }
        return into;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region that = (Region) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        if (ySections != that.ySections) return false;
        if (yEndMask != that.yEndMask) return false;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Hasher.gremory.hash(data) ^ (int)Hasher.randomize2(Hasher.randomize2(height) + width);
    }
    public long hash64() {
        return Hasher.gremory.hash64(data) ^ Hasher.randomize2(Hasher.randomize2(height) + width);
    }

    /**
     * Computes a 64-bit hash code of this Region given a 64-bit seed; even if given two very similar seeds,
     * this should produce very different hash codes for the same Region.
     * <br>
     * Meant for potential use in Bloom filters. Uses {@link Hasher}'s algorithm(s).
     * @param seed a seed that will determine how the hashing algorithm works; all 64 bits are used.
     * @return a 64-bit hash code for this Region
     */
    public long hash64(long seed)
    {
        return Hasher.hash64(seed, data) ^ Hasher.randomize2(Hasher.randomize2(height) + width);
    }

    public String stringSerialize()
    {
        return width +
                "," + height +
                "," + Base.SIMPLE64.join(",", data);
    }
    public static Region stringDeserialize(String s)
    {
        if(s == null || s.isEmpty())
            return null;
        int gap = s.indexOf(','), w = Integer.parseInt(s.substring(0, gap)),
                gap2 = s.indexOf(',', gap+1), h = Integer.parseInt(s.substring(gap+1, gap2));
        long[] data = Base.SIMPLE64.longSplit(s, ",", gap2 + 1, s.length());
//        String[] splits = StringTools.split(s.substring(gap2+1), ",");
//        long[] data = new long[splits.length];
//        for (int i = 0; i < splits.length; i++) {
//            data[i] = DigitTools.longFromDec(splits[i]);
//        }
        return new Region(data, w, h);
    }

    /**
     * Constructs a Region using a vararg for data. Primarily meant for generated code, since
     * {@link #stringSerialize()} produces a String that happens to be a valid parameter list for this method.
     * @param width width of the Region to produce
     * @param height height of the Region to produce
     * @param data array or vararg of long containing the exact data, probably from an existing Region
     * @return a new Region with the given width, height, and data
     */
    public static Region of(final int width, final int height, final long... data)
    {
        return new Region(data, width, height);
    }

    /**
     * Compresses this Region into a UTF-16 String and returns the String without modifying this Region.
     * Uses {@link HilbertCurve}'s algorithm and data to compress this Region in 256x128 blocks, storing the
     * HilbertCurve data as chars with values from 256 to 33023 (a concept also used in {@link LZSEncoding}),
     * and using ASCII semicolons to separate them or store other info (just width and height, which are given first as
     * 16 hex digits). This finishes by running the result through {@link LZSEncoding}, a combination which typically
     * gets very good compression.
     * @return a String that could be used to reconstruct this Region using {@link #decompress(String)}
     */
    public String toCompressedString()
    {
        HilbertCurve.init2D();
        StringBuilder packing = new StringBuilder(width * height >> 3);
        DigitTools.appendHex(packing, width);
        DigitTools.appendHex(packing, height);
        final int chunksX = width + 255 >> 8, chunksY = height + 127 >> 7;
        for (int bigX = 0, baseX = 0; bigX < chunksX; bigX++, baseX += 256) {
            for (int bigY = 0, baseY = 0; bigY < chunksY; bigY++, baseY += 128) {
                packing.append(';');
                boolean on = false, current;
                char skip = 0, hx, hy;
                int xSize = Math.min(256, width - baseX), ySize = Math.min(128, height - baseY),
                        limit = 0x8000, mapLimit = xSize * ySize;
                if (xSize <= 128) {
                    limit >>= 1;
                    if (xSize <= 64) {
                        limit >>= 1;
                        if (ySize <= 64) {
                            limit >>= 1;
                            if (ySize <= 32) {
                                limit >>= 1;
                                if (xSize <= 32) {
                                    limit >>= 1;
                                }
                            }
                        }
                    }
                }
                for (int i = 0, ml = 0; i < limit && ml < mapLimit; i++, skip++) {
                    hx = HilbertCurve.hilbertX[i];
                    hy = HilbertCurve.hilbertY[i];
                    if (hx >= xSize || hy >= ySize) {
                        if (on) {
                            on = false;
                            packing.append((char) (skip + 256));
                            skip = 0;
                        }
                        continue;
                    }
                    ml++;
                    current = ((data[(baseX + hx) * ySections + (baseY + hy >> 6)] & (1L << hy)) != 0);
                    if (current != on) {
                        packing.append((char) (skip + 256));
                        skip = 0;
                        on = current;
                    }
                }
                if (on)
                    packing.append((char) (skip + 256));
            }
        }
        return LZSEncoding.compressToUTF16(packing.toString());
    }

    /**
     * Decompresses a String returned by {@link #toCompressedString()}, returning a new Region with identical
     * width, height, and contents to the Region before compression. This decompresses the {@link LZSEncoding}
     * applied to the data, then decompresses the {@link HilbertCurve} RLE data to get the original
     * Region back.
     * @param compressed a String that was compressed by {@link #toCompressedString()}, without changes
     * @return a new copy of the Region that was previously compressed
     */
    public static Region decompress(String compressed)
    {
        HilbertCurve.init2D();
        Region target;
        compressed = LZSEncoding.decompressFromUTF16(compressed);
        final int width = DigitTools.intFromHex(compressed), height = DigitTools.intFromHex(compressed, 8, 16);
        target = new Region(width, height);
        final int chunksX = width + 255 >> 8, chunksY = height + 127 >> 7;
        int startPack = 16, endPack, idx;//, hy;
        boolean on;
        for (int bigX = 0, baseX = 0; bigX < chunksX; bigX++, baseX += 256) {
            for (int bigY = 0, baseY = 0; bigY < chunksY; bigY++, baseY += 128) {
                ++startPack;
                endPack = compressed.indexOf(';', startPack);
                if(endPack < 0) endPack = compressed.length();
                on = false;
                idx = 0;
                for(int p = startPack; p < endPack; p++, on = !on) {
                    if (on) {
                        for (int toSkip = idx + (compressed.charAt(p) - 256); idx < toSkip && idx < 0x8000; idx++) {
                            target.insert(HilbertCurve.hilbertX[idx] + baseX, HilbertCurve.hilbertY[idx] + baseY);
                        }
                    } else {
                        idx += compressed.charAt(p) - 256;
                    }
                }
                startPack = endPack;
            }
        }
        return target;
    }

    /**
     * Decompresses a String returned by {@link #toCompressedString()}, and assigns into this region the
     * width, height, and contents of the data before compression. This decompresses the {@link LZSEncoding}
     * applied to the data, then decompresses the {@link HilbertCurve} RLE data to get the original
     * Region's size and contents back.
     * @param compressed a String that was compressed by {@link #toCompressedString()}, without changes
     * @return this, for chaining
     */
    public Region decompressInto(String compressed) {
        HilbertCurve.init2D();
        compressed = LZSEncoding.decompressFromUTF16(compressed);
        final int width = DigitTools.intFromHex(compressed), height = DigitTools.intFromHex(compressed, 8, 16);
        resizeAndEmpty(width, height);
        final int chunksX = width + 255 >> 8, chunksY = height + 127 >> 7;
        int startPack = 16, endPack, idx;//, hy;
        boolean on;
        for (int bigX = 0, baseX = 0; bigX < chunksX; bigX++, baseX += 256) {
            for (int bigY = 0, baseY = 0; bigY < chunksY; bigY++, baseY += 128) {
                ++startPack;
                endPack = compressed.indexOf(';', startPack);
                if(endPack < 0) endPack = compressed.length();
                on = false;
                idx = 0;
                for(int p = startPack; p < endPack; p++, on = !on) {
                    if (on) {
                        for (int toSkip = idx + (compressed.charAt(p) - 256); idx < toSkip && idx < 0x8000; idx++) {
                            insert(HilbertCurve.hilbertX[idx] + baseX, HilbertCurve.hilbertY[idx] + baseY);
                        }
                    } else {
                        idx += compressed.charAt(p) - 256;
                    }
                }
                startPack = endPack;
            }
        }
        return this;
    }

    @Override
    public boolean contains(Object o) {
        if(o instanceof Coord)
            return contains((Coord)o);
        return false;
    }

    @Override
    public @NonNull Iterator<Coord> iterator() {
        return new GRIterator();
    }

    @Override
    public Object[] toArray() {
        return asCoords();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        if(a instanceof Coord[])
            return (T[])asCoords((Coord[])a);
        return a;
    }

    @Override
    public boolean add(Coord coord) {
        if(contains(coord))
            return false;
        insert(coord);
        return true;
    }
    @Override
    public void clear()
    {
        Arrays.fill(data, 0L);
    }

    @Override
    public boolean remove(Object o) {
        if(o instanceof Coord)
        {
            if(contains((Coord)o))
            {
                remove((Coord)o);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c)
        {
            if(!contains(o))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Coord> c) {
        boolean changed = false;
        for(Coord co : c)
        {
            changed |= add(co);
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for(Object o : c)
        {
            changed |= remove(o);
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Region g2 = new Region(width, height);
        for(Object o : c)
        {
            if(contains(o) && o instanceof Coord)
            {
                g2.add((Coord)o);
            }
        }
        boolean changed = equals(g2);
        remake(g2);
        return changed;
    }

    /**
     * Randomly removes points from a Region, with larger values for preservation keeping more of the existing
     * shape intact. If preservation is 1, roughly 1/2 of all points will be removed; if 2, roughly 1/4, if 3, roughly
     * 1/8, and so on, so that preservation can be thought of as a negative exponent of 2.
     * @param rng used to determine random factors
     * @param preservation roughly what degree of points to remove (higher keeps more); removes about {@code 1/(2^preservation)} points
     * @return a randomly modified change to this Region
     */
    public Region deteriorate(EnhancedRandom rng, int preservation) {
        if(rng == null || width <= 2 || ySections <= 0 || preservation <= 0)
            return this;
        long mash;
        for (int i = 0; i < width * ySections; i++) {
            mash = rng.nextLong();
            for (int j = i; j < preservation; j++) {
                mash |= rng.nextLong();
            }
            data[i] &= mash;
        }
        tallied = false;
        return this;
    }

    /**
     * Randomly removes points from a Region, with preservation as a fraction between 1.0 (keep all) and 0.0
     * (remove all). If preservation is 0.5, roughly 1/2 of all points will be removed; if 0.25, roughly 3/4 will be
     * removed (roughly 0.25 will be _kept_), if 0.8, roughly 1/5 will be removed (and about 0.8 will be kept), and so
     * on. Preservation must be between 0.0 and 1.0 for this to have the intended behavior; 1.0 or higher will keep all
     * points without change (returning this Region), while anything less than 0.015625 (1.0/64) will empty this
     * Region (using {@link #empty()}) and then return it. The parameter {@code random} can be a plain {@link EnhancedRandom},
     * but will be much faster and have less potential for patterns or artifacts in the output if you use
     * {@link WhiskerRandom} or another higher-quality subclass.
     * @param random a EnhancedRandom or a recommended subclass, like {@link WhiskerRandom}
     * @param preservation the rough fraction of points to keep, between 0.0 and 1.0
     * @return a randomly modified change to this Region
     */
    public Region deteriorate(final EnhancedRandom random, final float preservation) {
        if(random == null || width <= 2 || ySections <= 0 || preservation >= 1)
            return this;
        if(preservation <= 0)
            return empty();
        int bitCount = (int) (preservation * 64);
        for (int i = 0; i < width * ySections; i++) {
            data[i] &= approximateBits(random, bitCount);
        }
        tallied = false;
        return this;
    }

    /**
     * Changes the on/off state of the cell with the given x and y, making an on cell into an off cell, or an off cell
     * into an on cell.
     * <br>
     * This was called flip(), but that name would be confusing since flipping a rectangular area usually means
     * reversing an axis.
     * @param x the x position of the cell to flip
     * @param y the y position of the cell to flip
     * @return this for chaining, modified
     */
    public Region toggle(int x, int y) {
        if(x >= 0 && y >= 0 && x < width && y < height && ySections > 0)
        {
            data[x * ySections + (y >> 6)] ^= (1L << (y & 63));
            tallied = false;
        }
        return this;

    }

    /**
     * Returns a new Region that has been mirrored along the rightmost edge, parallel to the y-axis. The new
     * Region will have exactly twice the width, the additional width will have the contents of the original
     * GreasesRegion in reversed order. The positions shared by both Regions will be the same, that is, any area
     * not added to the original will be equal to the original.
     * @return a new Region with twice the width of {@code this}, that is mirrored along the rightmost edge
     */
    public Region mirrorY()
    {
        Region next = new Region(data, width, height, width * 2, height);
        for (int i = 0, o = width * 2 - 1; i < width; i++, o--) {
            System.arraycopy(data, ySections * i, next.data, ySections * o, ySections);
        }
        return next;
    }

    /**
     * Checks if {@code c} is present in this Region. Returns true if and only if c is present in this
     * Region as an "on" cell. This will never be true if c is null, has negative x or y, has a value for x that
     * is equal to or greater than {@link #width}, or has a value for y that is equal to or greater than
     * {@link #height}, but none of those conditions will cause Exceptions to be thrown.
     * @param c a Coord to try to find in this Region; if null this will always return false
     * @return true if {@code c} is an "on" cell in this Region, or false otherwise, including if c is null
     */
    public boolean contains(Coord c) {
        return c != null && contains(c.x, c.y);
    }

    /**
     * @param findSmallest if true, finds the smallest x-coordinate value;
     *                          if false, finds the biggest.
     * @return The x-coordinate of the Coord within {@code this} that has the
     * smallest (or biggest) x-coordinate. Or -1 if the zone is empty.
     */
    public int xBound(boolean findSmallest) {
        if(findSmallest)
            return first().x;
        else
            return last().x;
    }

    /**
     * @param findSmallest if true, finds the smallest y-coordinate value;
     *                          if false, finds the biggest.
     * @return The y-coordinate of the Coord within {@code this} that has the
     * smallest (or biggest) y-coordinate. Or -1 if the zone is empty.
     */
    public int yBound(boolean findSmallest) {
        long t = 0L;
        if(findSmallest) {
            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    t |= data[x * ySections + s];
                }
                if(t != 0L)
                    return s << 6 | Long.numberOfTrailingZeros(t);
            }
            return -1;
        }
        else
        {
            for (int s = ySections - 1; s >= 0; s--) {
                for (int x = 0; x < width; x++) {
                    t |= data[x * ySections + s];
                }
                if(t != 0L)
                    return s << 6 | 63 - BitConversion.countLeadingZeros(t);
            }
            return -1;
        }
    }

//    /**
//     * The object implements the writeExternal method to save its contents
//     * by calling the methods of DataOutput for its primitive values or
//     * calling the writeObject method of ObjectOutput for objects, strings,
//     * and arrays.
//     *
//     * @param out the stream to write the object to
//     * @throws IOException Includes any I/O exceptions that may occur
//     * @serialData Overriding methods should use this tag to describe
//     * the data layout of this Externalizable object.
//     * List the sequence of element types and, if possible,
//     * relate the element to a public/protected field and/or
//     * method of this Externalizable class.
//     */
//    @GwtIncompatible
//    public void writeExternal(ObjectOutput out) throws IOException {
//        out.writeUTF(toCompressedString());
//    }
//
//    /**
//     * The object implements the readExternal method to restore its
//     * contents by calling the methods of DataInput for primitive
//     * types and readObject for objects, strings and arrays.  The
//     * readExternal method must read the values in the same sequence
//     * and with the same types as were written by writeExternal.
//     *
//     * @param in the stream to read data from in order to restore the object
//     * @throws IOException            if I/O errors occur
//     */
//    @GwtIncompatible
//    public void readExternal(ObjectInput in) throws IOException {
//        decompressInto(in.readUTF());
//    }

    public class GRIterator implements Iterator<Coord>
    {
        public int index;
        private long t, w;
        public GRIterator()
        {
            if(!tallied)
                tally();
        }
        @Override
        public boolean hasNext() {
            return index < ct;
        }

        @Override
        public Coord next() {
            int c;
            if(index >= ct)
                return null;
            for (int s = 0; s < ySections; s++) {
                for (int x = 0; x < width; x++) {
                    if ((c = counts[x * ySections + s]) > index) {
                        t = data[x * ySections + s] | 0L;
                        for (--c; t != 0; c--) {
                            w = BitConversion.lowestOneBit(t);
                            if (c == index)
                            {
                                index++;
                                return Coord.get(x, (s << 6) | Long.bitCount(w-1));
                            }
                            t ^= w;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() is not supported on this Iterator.");
        }
    }
}
