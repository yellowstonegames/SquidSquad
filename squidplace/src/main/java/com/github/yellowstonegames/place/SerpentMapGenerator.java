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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.HilbertCurve;

/**
 * Generate dungeons based on a random, winding, looping path through 2D space. Uses techniques from MixedGenerator.
 * Uses a Moore Curve, which is related to Hilbert Curves but loops back to its starting point, and stretches and
 * distorts the grid to make sure a visual correlation isn't obvious. This supports the getEnvironment() method, which
 * can be used in conjunction with RoomFinder to find where separate room, corridor, and cave areas have been placed.
 * <br>
 * To get a sense of what kinds of map this generates, you can look at a sample map on
 * <a href="https://gist.github.com/tommyettinger/93b47048fc8a209a9712">this Gist</a>, which also includes a snippet of
 * Java code that can generate that map.
 * <br>
 * The name comes from a vivid dream I had about gigantic, multi-colored snakes that completely occupied a roguelike
 * dungeon. Shortly after, I made the connection to the Australian mythology I'd heard about the Rainbow Serpent, which
 * in some stories dug water-holes and was similarly gigantic.
 */
public class SerpentMapGenerator implements PlaceGenerator {
    private MixedGenerator mix;
    private int[] columns, rows;

    /**
     * This prepares a map generator that will generate a map with width 80 and height 80, using a random seed.
     * The intended purpose is to carve a long path that loops through the whole dungeon, while hopefully maximizing
     * the amount of rooms the player encounters. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @see MixedGenerator
     */
    public SerpentMapGenerator() {
        this(80, 80, new WhiskerRandom(), false);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * The intended purpose is to carve a long path that loops through the whole dungeon, while hopefully maximizing
     * the amount of rooms the player encounters. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width  the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng    an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @see MixedGenerator
     */
    public SerpentMapGenerator(int width, int height, EnhancedRandom rng) {
        this(width, height, rng, false);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * The intended purpose is to carve a long path that loops through the whole dungeon, while hopefully maximizing
     * the amount of rooms the player encounters. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width       the width of the final map in cells
     * @param height      the height of the final map in cells
     * @param random      an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param symmetrical true if this should generate a bi-radially symmetric map, false for a typical map
     * @see MixedGenerator
     */
    public SerpentMapGenerator(int width, int height, EnhancedRandom random, boolean symmetrical) {
        if (width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        HilbertCurve.init2D();
        long columnAlterations = random.nextLong() >>> 16;
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = random.nextLong() >>> 16;
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        columns = new int[16];
        rows = new int[16];
        int csum = 0, rsum = 0;
        long b = 7;
        for (int i = 0; i < 16; i++, b <<= 3) {
            columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
            csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
            rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
        }
        int cs = width - csum;
        int rs = height - rsum;
        int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
        for (int i = 0; i <= 7; i++) {
            cs2 = 0;
            rs2 = 0;
            columns[i] -= cs2;
            rows[i] -= rs2;
        }
        for (int i = 15; i >= 8; i--) {
            cs3 = cs3 * (i - 8) / 8;
            rs3 = rs3 * (i - 8) / 8;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        ObjectList<Coord> points = new ObjectList<>(80);
        Coord temp;
        for (int i = 0, m = random.next(6), r; i < 256; r = random.nextInt(4, 12), i += r, m += r) {
            temp = HilbertCurve.mooreToCoord(m);
            points.add(Coord.get(columns[temp.x], rows[temp.y]));
        }
        points.add(points.get(0));
        if (symmetrical) {
            mix = new SymmetryDungeonGenerator(width, height, random,
                    SymmetryDungeonGenerator.removeSomeOverlap(width, height, points));
        } else
            mix = new MixedGenerator(width, height, random, points);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * The intended purpose is to carve a long path that loops through the whole dungeon, while hopefully maximizing
     * the amount of rooms the player encounters. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width           the width of the final map in cells
     * @param height          the height of the final map in cells
     * @param rng             an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param branchingChance the chance from 0.0 to 1.0 that each room will branch at least once
     * @see MixedGenerator
     */
    public SerpentMapGenerator(int width, int height, EnhancedRandom rng, double branchingChance) {
        this(width, height, rng, branchingChance, false);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * The intended purpose is to carve a long path that loops through the whole dungeon, while hopefully maximizing
     * the amount of rooms the player encounters. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     *
     * @param width           the width of the final map in cells
     * @param height          the height of the final map in cells
     * @param random          an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param branchingChance the chance from 0.0 to 1.0 that each room will branch at least once
     * @param symmetrical     true if this should generate a bi-radially symmetric map, false for a typical map
     * @see MixedGenerator
     */
    public SerpentMapGenerator(int width, int height, EnhancedRandom random, double branchingChance, boolean symmetrical) {
        if (width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        HilbertCurve.init2D();
        long columnAlterations = random.nextLong() >>> 16;
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = random.nextLong() >>> 16;
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        columns = new int[16];
        rows = new int[16];
        int csum = 0, rsum = 0;
        long b = 7;
        for (int i = 0; i < 16; i++, b <<= 3) {
            columns[i] = csum + (int) (columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
            csum += (int) (columnBase * (3 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int) (rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
            rsum += (int) (rowBase * (3 + Long.bitCount(rowAlterations & b)));
        }
        int cs = width - csum;
        int rs = height - rsum;
        int cs2 = cs, rs2 = rs, cs3 = cs, rs3 = rs;
        for (int i = 0; i <= 7; i++) {
            cs2 = 0;
            rs2 = 0;
            columns[i] -= cs2;
            rows[i] -= rs2;
        }
        for (int i = 15; i >= 8; i--) {
            cs3 = cs3 * (i - 8) / 8;
            rs3 = rs3 * (i - 8) / 8;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        ObjectObjectOrderedMap<Coord, ObjectList<Coord>> connections = new ObjectObjectOrderedMap<>(80);
        Coord temp, t;
        int m = random.nextInt(64), r = random.nextInt(4, 12);
        temp = HilbertCurve.mooreToCoord(m);
        Coord starter = HilbertCurve.mooreToCoord(m);
        m += r;
        for (int i = r; i < 256; i += r, m += r) {
            ObjectList<Coord> cl = new ObjectList<>(4);
            cl.add(Coord.get(columns[temp.x], rows[temp.y]));
            temp = HilbertCurve.mooreToCoord(m);
            r = random.nextInt(4, 12);
            for (int j = 0, p = r - 1;
                 j < 3 && p > 2 && Math.min(random.nextDouble(), random.nextDouble()) < branchingChance;
                 j++, p -= random.nextInt(1, p)) {
                t = HilbertCurve.mooreToCoord(m + p);
                cl.add(Coord.get(columns[t.x], rows[t.y]));
            }
            connections.put(Coord.get(columns[temp.x], rows[temp.y]), cl);
        }
        connections.get(Coord.get(columns[temp.x], rows[temp.y])).add(
                Coord.get(columns[starter.x], rows[starter.y]));
        if (symmetrical) {
            mix = new SymmetryDungeonGenerator(width, height, random,
                    SymmetryDungeonGenerator.removeSomeOverlap(width, height, connections));
        } else
            mix = new MixedGenerator(width, height, random, connections);

    }

    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * nextInt the count of putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making caves nextInt rooms; only matters in relation to other carvers
     * @see MixedGenerator
     */
    public void putCaveCarvers(int count) {
        mix.putCaveCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 nextInt the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors nextInt them; only matters in relation
     *              to other carvers
     * @see MixedGenerator
     */
    public void putBoxRoomCarvers(int count) {
        mix.putBoxRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. This also
     * ensures walls will be placed around the room, only allowing corridors and small cave openings to pass. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 nextInt the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making box-shaped rooms and corridors nextInt them; only matters in relation
     *              to other carvers
     * @see MixedGenerator
     */
    public void putWalledBoxRoomCarvers(int count) {
        mix.putWalledBoxRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 nextInt the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors nextInt them; only matters in relation
     *              to other carvers
     * @see MixedGenerator
     */
    public void putRoundRoomCarvers(int count) {
        mix.putRoundRoomCarvers(count);
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. This also ensures walls will be placed around the room, only allowing corridors and small cave openings to
     * pass. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 nextInt the count of putCaveCarvers(), putBoxRoomCarvers(),
     * and putRoundRoomCarvers() is reasonable.
     *
     * @param count the number of carvers making circular rooms and corridors nextInt them; only matters in relation
     *              to other carvers
     * @see MixedGenerator
     */
    public void putWalledRoundRoomCarvers(int count) {
        mix.putWalledRoundRoomCarvers(count);
    }

    /**
     * This generates a new map by stretching a 16x16 grid of potential rooms to fit the width and height passed to the
     * constructor, randomly expanding columns and rows before contracting the whole to fit perfectly. This uses the
     * Moore Curve, a space-filling curve that loops around on itself, to guarantee that the rooms will always have a
     * long path through the dungeon that, if followed completely, will take you back to your starting room. Some small
     * branches are possible, and large rooms may merge with other rooms nearby. This uses MixedGenerator.
     *
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x first y second
     * @see MixedGenerator
     */
    public char[][] generate() {
        return mix.generate();
    }


    /**
     * Gets a 2D array of int constants, each representing a type of environment corresponding to a static field of
     * DungeonTools. This array will have the same size as the last char 2D array produced by generate(); the value
     * of this method if called before generate() is undefined, but probably will be a 2D array of all 0 (UNTOUCHED).
     * <ul>
     * <li>DungeonTools.UNTOUCHED, equal to 0, is used for any cells that aren't near a floor.</li>
     * <li>DungeonTools.ROOM_FLOOR, equal to 1, is used for floor cells inside wide room areas.</li>
     * <li>DungeonTools.ROOM_WALL, equal to 2, is used for wall cells around wide room areas.</li>
     * <li>DungeonTools.NATURAL_FLOOR, equal to 3, is used for floor cells inside rough natural/cave areas.</li>
     * <li>DungeonTools.NATURAL_WALL, equal to 4, is used for wall cells around rough natural/cave areas.</li>
     * <li>DungeonTools.CORRIDOR_FLOOR, equal to 5, is used for floor cells inside narrow corridor areas.</li>
     * <li>DungeonTools.CORRIDOR_WALL, equal to 6, is used for wall cells around narrow corridor areas.</li>
     * </ul>
     *
     * @return a 2D int array where each element is an environment type constant in DungeonTools
     */
    @Override
    public int[][] getEnvironment() {
        return mix.getEnvironment();
    }

    @Override
    public char[][] getPlaceGrid() {
        return mix.getPlaceGrid();
    }

    @Override
    public String toString() {
        return "SerpentMapGenerator{" +
                "width=" + mix.width +
                ", height=" + mix.height +
                ", roomWidth=" + mix.roomWidth +
                ", roomHeight=" + mix.roomHeight +
                '}';
    }
}
