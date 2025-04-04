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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.WeightedTable;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.PoissonDisk;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A dungeon generator that can use a mix of techniques to have part-cave, part-room dungeons. Not entirely intended for
 * normal use outside of this library, though it can be very useful when you want to make a dungeon match a specific
 * path and existing generators that use MixedGenerator aren't sufficient. You may want to use a simpler generator based
 * on this, like SerpentMapGenerator, which generates a long, winding path that loops around on itself. This supports
 * the getEnvironment() method, which can be used in conjunction with RoomFinder to find where separate room, corridor,
 * and cave areas have been placed.
 * <br>
 * Based on <a href="http://mpatraw.github.io/libdrunkard/"></a>Michael Patraw's excellent Drunkard's Walk dungeon generator</a>.
 *
 * @see SerpentMapGenerator SerpentMapGenerator is a normal use for MixedGenerator that makes winding dungeons
 * @see SerpentDeepMapGenerator SerpentDeepMapGenerator uses MixedGenerator as it makes a multi-level dungeon
 */
public class MixedGenerator implements PlaceGenerator {
    public static final int CAVE = 0,
        BOX = 1,
        ROUND = 2,
        BOX_WALLED = 3,
        ROUND_WALLED = 4;

    protected float[] carvers;
    protected WeightedTable carverTable;
    protected int width, height;
    protected float roomWidth, roomHeight;
    public EnhancedRandom rng;
    protected char[][] dungeon;
    protected boolean generated;
    protected int[][] environment;
    protected boolean[][] marked, walled, fixedRooms;
    protected IntList points;
    protected int totalPoints;

    /**
     * Mainly for internal use; this is used by {@link #MixedGenerator(int, int, EnhancedRandom)} to get its room positions.
     * This is the default for generating a List of Coord if no other collection of Coord was supplied to the
     * constructor.
     * <br>
     * <a href="https://gist.githubusercontent.com/tommyettinger/be0ed51858cb492bc7e8cda43a04def1/raw/dae9d8e4f45dd3a3577bdd5f58b419ea5f9ed570/PoissonDungeon.txt">Preview map.</a>
     * @param width dungeon width in cells
     * @param height dungeon height in cells
     * @param rng rng to use
     * @return evenly spaced Coord points in a list made by PoissonDisk, trimmed down so they aren't all used
     * @see PoissonDisk used to make the list
     */
    public static CoordObjectOrderedMap<ObjectList<Coord>> basicPoints(int width, int height, EnhancedRandom rng) {
        return PoissonDisk.sampleRectangle(Coord.get(2, 2), Coord.get(width - 3, height - 3),
                8f * (width + height) / 120f + 1f, width, height, 20, rng);
    }

    /**
     * This prepares a map generator that will generate a map with width 80 and height 80, using a random seed.
     * This version of the constructor uses a sub-random point sequence to generate the points it will draw caves and
     * corridors between, helping to ensure a minimum distance between points, but it does not ensure that paths between
     * points  will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect
     * what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to
     * only caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     */
    public MixedGenerator() {
        this(80, 80, new AceRandom());
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a sub-random point sequence to generate the points it will draw caves and
     * corridors between, helping to ensure a minimum distance between points, but it does not ensure that paths between
     * points  will avoid overlapping with rooms or other paths. You call the different carver-adding methods to affect
     * what the dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to
     * only caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @see PoissonDisk used to ensure spacing for the points.
     */
    public MixedGenerator(int width, int height, EnhancedRandom rng) {
        this(width, height, rng, basicPoints(width, height, rng));
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given RNG.
     * This version of the constructor uses a List of Coord points from some other source to determine the path to add
     * rooms or caves to and then connect. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an RNG object to use for random choices; this make a lot of random choices.
     * @param sequence a List of Coord to connect in order; index 0 is the start, index size() - 1 is the end.
     */
    public MixedGenerator(int width, int height, EnhancedRandom rng, List<Coord> sequence) {
        this.width = width;
        this.height = height;
        this.roomWidth = width / 64.0f;
        this.roomHeight = height / 64.0f;
        if (width <= 2 || height <= 2)
            throw new IllegalStateException("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        environment = new int[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        fixedRooms = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        Arrays.fill(environment[0], DungeonTools.UNTOUCHED);
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
            System.arraycopy(environment[0], 0, environment[i], 0, height);
        }
        totalPoints = sequence.size() - 1;
        points = new IntList(totalPoints);
        for (int i = 0; i < totalPoints; i++) {
            Coord c1 = sequence.get(i), c2 = sequence.get(i + 1);
            points.add(((c1.x & 0xff) << 24) | ((c1.y & 0xff) << 16) | ((c2.x & 0xff) << 8) | (c2.y & 0xff));
        }
        carvers = new float[5];
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * This version of the constructor uses a Map with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     */
    public MixedGenerator(int width, int height, EnhancedRandom rng, Map<Coord, ? extends List<Coord>> connections) {
        this(width, height, rng, connections, 0.8f);
    }

    /**
     * This prepares a map generator that will generate a map with the given width and height, using the given EnhancedRandom.
     * This version of the constructor uses a Map with Coord keys and Coord array values to determine a
     * branching path for the dungeon to take; each key will connect once to each of the Coords in its value, and you
     * usually don't want to connect in both directions. You call the different carver-adding methods to affect what the
     * dungeon will look like, putCaveCarvers(), putBoxRoomCarvers(), and putRoundRoomCarvers(), defaulting to only
     * caves if none are called. You call generate() after adding carvers, which returns a char[][] for a map.
     * @param width the width of the final map in cells
     * @param height the height of the final map in cells
     * @param rng an EnhancedRandom object to use for random choices; this makes a lot of random choices.
     * @param connections a Map of Coord keys to arrays of Coord to connect to next; shouldn't connect both ways
     * @param roomSizeMultiplier a float multiplier that will be applied to each room's width and height
     */
    public MixedGenerator(int width, int height, EnhancedRandom rng, Map<Coord, ? extends List<Coord>> connections,
                          float roomSizeMultiplier) {
        this.width = width;
        this.height = height;
        roomWidth = (width / 64.0f) * roomSizeMultiplier;
        roomHeight = (height / 64.0f) * roomSizeMultiplier;
        if (width <= 2 || height <= 2)
            throw new IllegalStateException("width and height must be greater than 2");
        this.rng = rng;
        dungeon = new char[width][height];
        environment = new int[width][height];
        marked = new boolean[width][height];
        walled = new boolean[width][height];
        fixedRooms = new boolean[width][height];
        Arrays.fill(dungeon[0], '#');
        Arrays.fill(environment[0], DungeonTools.UNTOUCHED);
        for (int i = 1; i < width; i++) {
            System.arraycopy(dungeon[0], 0, dungeon[i], 0, height);
            System.arraycopy(environment[0], 0, environment[i], 0, height);
        }
        totalPoints = 0;
        for (List<Coord> vals : connections.values()) {
            totalPoints += vals.size();
        }
        points = new IntList(totalPoints);
        for (Map.Entry<Coord, ? extends List<Coord>> kv : connections.entrySet()) {
            Coord c1 = kv.getKey();
            for (Coord c2 : kv.getValue()) {
                points.add(((c1.x & 0xff) << 24) | ((c1.y & 0xff) << 16) | ((c2.x & 0xff) << 8) | (c2.y & 0xff));
            }
        }
        carvers = new float[5];
    }
    
    /**
     * Changes the number of "carvers" that will create caves from one room to the next. If count is 0 or less, no caves
     * will be made. If count is at least 1, caves are possible, and higher numbers relative to the other carvers make
     * caves more likely. Carvers are shuffled when used, then repeat if exhausted during generation. Since typically
     * about 30-40 rooms are carved, large totals for carver count aren't really needed; aiming for a total of 10
     * between the count of putCaveCarvers(), putBoxRoomCarvers(), putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and
     * putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making caves between rooms; only matters in relation to other carvers
     */
    public void putCaveCarvers(int count) {
        carvers[CAVE] = count;
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putBoxRoomCarvers(int count) {
        carvers[BOX] = count;
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one. If count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putRoundRoomCarvers(int count) {
        carvers[ROUND] = count;
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a box shape at the start and end, and a small room at the corner if there is one, enforcing
     * the presence of walls around the rooms even if another room is already there or would be placed there. Corridors
     * can always pass through enforced walls, but caves will open at most one cell in the wall. If count
     * is 0 or less, no box-shaped rooms will be made. If count is at least 1, box-shaped rooms are possible, and higher
     * numbers relative to the other carvers make box-shaped rooms more likely. Carvers are shuffled when used, then
     * repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making box-shaped rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledBoxRoomCarvers(int count) {
        carvers[BOX_WALLED] = count;
    }

    /**
     * Changes the number of "carvers" that will create right-angle corridors from one room to the next, create rooms
     * with a random size in a circle shape at the start and end, and a small circular room at the corner if there is
     * one, enforcing the presence of walls around the rooms even if another room is already there or would be placed
     * there. Corridors can always pass through enforced walls, but caves will open at most one cell in the wall. If
     * count is 0 or less, no circular rooms will be made. If count is at least 1, circular rooms are possible,
     * and higher numbers relative to the other carvers make circular rooms more likely. Carvers are shuffled when used,
     * then repeat if exhausted during generation. Since typically about 30-40 rooms are carved, large totals for carver
     * count aren't really needed; aiming for a total of 10 between the count of putCaveCarvers(), putBoxRoomCarvers(),
     * putRoundRoomCarvers(), putWalledBoxRoomCarvers(), and putWalledRoundRoomCarvers() is reasonable.
     * @param count the number of carvers making circular rooms and corridors between them; only matters in relation
     *              to other carvers
     */
    public void putWalledRoundRoomCarvers(int count) {
        carvers[ROUND_WALLED] = count;
    }

    /**
     * Uses the added carvers (or just makes caves if none were added) to carve from point to point in sequence, if it
     * was provided by the constructor, or evenly-spaced randomized points if it was not. This will never carve out
     * cells on the very edge of the map. Uses the numbers of the various kinds of carver that were added relative to
     * each other to determine how frequently to use a given carver type.
     * @return a char[][] where '#' is a wall and '.' is a floor or corridor; x first y second
     */
    @Override
    public char[][] generate() {
        if (carvers[0] <= 0 && carvers[1] <= 0 && carvers[2] <= 0 && carvers[3] <= 0 && carvers[4] <= 0)
            carvers[0] = 1;
        carverTable = new WeightedTable(carvers);

        for (int p = 0; p < totalPoints; p++) {
            int pair = points.get(p);
            Coord start = Coord.get(pair >>> 24 & 0xff, pair >>> 16 & 0xff),
                    end = Coord.get(pair >>> 8 & 0xff, pair & 0xff);
            int ct = carverTable.random(rng.nextLong());
            Direction dir;
            switch (ct) {
                case CAVE:
                    markPiercing(end);
                    markEnvironmentCave(end.x, end.y);
                    store();
                    float weight = 0.75f;
                    do {
                        Coord cent = markPlusCave(start);
                        if (cent != null) {
                            markPiercingCave(cent);
                            markPiercingCave(cent.translate(1, 0));
                            markPiercingCave(cent.translate(-1, 0));
                            markPiercingCave(cent.translate(0, 1));
                            markPiercingCave(cent.translate(0, -1));
                            weight = 0.95f;
                        }
                        dir = stepWobbly(start, end, weight);
                        start = start.translate(dir);
                    } while (dir != Direction.NONE);
                    break;
                case BOX:
                    markRectangle(end, rng.nextInt(1, 5), rng.nextInt(1, 5));
                    markRectangle(start, rng.nextInt(1, 4), rng.nextInt(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if (dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, dir.deltaY);
                    while (start.x != end.x && start.y != end.y) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markRectangle(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, (end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y)) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case BOX_WALLED:
                    markRectangleWalled(end, rng.nextInt(1, 5), rng.nextInt(1, 5));
                    markRectangleWalled(start, rng.nextInt(1, 4), rng.nextInt(1, 4));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if (dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, dir.deltaY);
                    while (start.x != end.x && start.y != end.y) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markRectangleWalled(start, 1, 1);
                    dir = Direction.getCardinalDirection(end.x - start.x, (end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y)) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND:
                    markCircle(end, rng.nextInt(2, 6));
                    markCircle(start, rng.nextInt(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if (dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, dir.deltaY);
                    while (start.x != end.x && start.y != end.y) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markCircle(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, (end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y)) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
                case ROUND_WALLED:
                    markCircleWalled(end, rng.nextInt(2, 6));
                    markCircleWalled(start, rng.nextInt(2, 6));
                    store();
                    dir = Direction.getDirection(end.x - start.x, end.y - start.y);
                    if (dir.isDiagonal())
                        dir = rng.nextBoolean() ? Direction.getCardinalDirection(dir.deltaX, 0)
                                : Direction.getCardinalDirection(0, dir.deltaY);
                    while (start.x != end.x && start.y != end.y) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    markCircleWalled(start, 2);
                    dir = Direction.getCardinalDirection(end.x - start.x, (end.y - start.y));
                    while (!(start.x == end.x && start.y == end.y)) {
                        markPiercing(start);
                        markEnvironmentCorridor(start.x, start.y);
                        start = start.translate(dir);
                    }
                    break;
            }
            store();
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (fixedRooms[x][y])
                    markPiercingCave(Coord.get(x, y)); // this is only used by LanesMapGenerator, where caves make sense
            }
        }
        store();
        markEnvironmentWalls();
        generated = true;
        return dungeon;
    }

    @Override
    public char[][] getPlaceGrid() {
        return dungeon;
    }

    @Override
    public int[][] getEnvironment() {
        return environment;
    }

    public boolean hasGenerated() {
        return generated;
    }

    public boolean[][] getFixedRooms() {
        return fixedRooms;
    }

    public void setFixedRooms(boolean[][] fixedRooms) {
        this.fixedRooms = fixedRooms;
    }

    /**
     * Internal use. Takes cells that have been previously marked and permanently stores them as floors in the dungeon.
     */
    protected void store() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (marked[i][j]) {
                    dungeon[i][j] = '.';
                    marked[i][j] = false;
                }
            }
        }
    }

    /**
     * Internal use. Finds all floor cells by environment and marks untouched adjacent (8-way) cells as walls, using the
     * appropriate type for the nearby floor.
     */
    protected void markEnvironmentWalls() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (environment[i][j] == DungeonTools.UNTOUCHED) {
                    boolean allWalls = true;
                    //lowest precedence, also checks for any floors
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonTools.CORRIDOR_FLOOR) {
                                markEnvironment(i, j, DungeonTools.CORRIDOR_WALL);
                            }
                            if (dungeon[x][y] == '.')
                                allWalls = false;
                        }
                    }
                    //if there are no floors we don't need to check twice again.
                    if (allWalls)
                        continue;
                    //more precedence
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonTools.NATURAL_FLOOR) {
                                markEnvironment(i, j, DungeonTools.NATURAL_WALL);
                            }
                        }
                    }
                    //highest precedence
                    for (int x = Math.max(0, i - 1); x <= Math.min(width - 1, i + 1); x++) {

                        for (int y = Math.max(0, j - 1); y <= Math.min(height - 1, j + 1); y++) {
                            if (environment[x][y] == DungeonTools.ROOM_FLOOR) {
                                markEnvironment(i, j, DungeonTools.ROOM_WALL);
                            }
                        }
                    }

                }
            }
        }
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    protected boolean mark(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && !walled[x][y]) {
            marked[x][y] = true;
            return false;
        } else return x > 0 && x < width - 1 && y > 0 && y < height - 1 && walled[x][y];
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markPiercing(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            marked[x][y] = true;
        }
    }

    /**
     * Internal use. Marks a point's environment type as the appropriate kind of environment.
     * @param x x position to mark
     * @param y y position to mark
     * @param kind an int that should be one of the constants in MixedGenerator for environment types.
     */
    protected void markEnvironment(int x, int y, int kind) {
        environment[x][y] = kind;
    }

    /**
     * Internal use. Marks a point's environment type as a corridor floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCorridor(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1
                && environment[x][y] != DungeonTools.ROOM_FLOOR
                && environment[x][y] != DungeonTools.NATURAL_FLOOR) {
            markEnvironment(x, y, DungeonTools.CORRIDOR_FLOOR);
        }
    }

    /**
     * Internal use. Marks a point's environment type as a room floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentRoom(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            markEnvironment(x, y, DungeonTools.ROOM_FLOOR);
        }
    }

    /**
     * Internal use. Marks a point's environment type as a cave floor.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void markEnvironmentCave(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && environment[x][y] != DungeonTools.ROOM_FLOOR) {
            markEnvironment(x, y, DungeonTools.NATURAL_FLOOR);
        }
    }

    /**
     * Internal use. Marks a point to be considered a hard wall.
     * @param x x position to mark
     * @param y y position to mark
     */
    protected void wallOff(int x, int y) {
        if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
            walled[x][y] = true;
        }
    }

    /**
     * Internal use. Marks a point to be made into floor.
     * @param pos position to mark
     * @return false if everything is normal, true if and only if this failed to mark because the position is walled
     */
    protected boolean mark(Coord pos) {
        return mark(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls.
     * @param pos position to mark
     */
    protected void markPiercing(Coord pos) {
        markPiercing(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also marks the point as a cave floor.
     * @param pos position to mark
     */
    protected void markPiercingCave(Coord pos) {
        markPiercing(pos.x, pos.y);
        markEnvironmentCave(pos.x, pos.y);
    }

    /**
     * Internal use. Marks a point to be made into floor, piercing walls, and also marks the point as a room floor.
     * @param x x coordinate of position to mark
     * @param y y coordinate of position to mark
     */
    protected void markPiercingRoom(int x, int y) {
        markPiercing(x, y);
        markEnvironmentRoom(x, y);
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it.
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall, otherwise the Coord of the center
     */
    private Coord markPlus(Coord pos) {
        Coord block = null;
        if (mark(pos.x, pos.y))
            block = pos;
        mark(pos.x + 1, pos.y);
        mark(pos.x - 1, pos.y);
        mark(pos.x, pos.y + 1);
        mark(pos.x, pos.y - 1);
        return block;
    }

    /**
     * Internal use. Marks a point and the four cells orthogonally adjacent to it, and also marks any cells that weren't
     * blocked as cave floors.
     * @param pos center position to mark
     * @return null if the center of the plus shape wasn't blocked by wall, otherwise the Coord of the center
     */
    private Coord markPlusCave(Coord pos) {
        Coord block = null;
        if (mark(pos.x, pos.y))
            block = pos;
        else
            markEnvironmentCave(pos.x, pos.y);
        if (!mark(pos.x + 1, pos.y))
            markEnvironmentCave(pos.x + 1, pos.y);
        if (!mark(pos.x - 1, pos.y))
            markEnvironmentCave(pos.x - 1, pos.y);
        if (!mark(pos.x, pos.y + 1))
            markEnvironmentCave(pos.x, pos.y + 1);
        if (!mark(pos.x, pos.y - 1))
            markEnvironmentCave(pos.x, pos.y - 1);
        return block;
    }

    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions. Marks all cells in the rectangle as room floors.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null if no points in the rectangle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markRectangle(Coord pos, int halfWidth, int halfHeight) {
        halfWidth = Math.max(1, Math.round(halfWidth * roomWidth));
        halfHeight = Math.max(1, Math.round(halfHeight * roomHeight));
        Coord block = null;
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                if (mark(i, j))
                    block = Coord.get(i, j);
                else
                    markEnvironmentRoom(i, j);
            }
        }
        return block;
    }

    /**
     * Internal use. Marks a rectangle of points centered on pos, extending halfWidth in both x directions and
     * halfHeight in both vertical directions. Also considers the area just beyond each wall, but not corners, to be
     * a blocking wall that can only be passed by corridors and small cave openings. Marks all cells in the rectangle as
     * room floors.
     * @param pos center position to mark
     * @param halfWidth the distance from the center to extend horizontally
     * @param halfHeight the distance from the center to extend vertically
     * @return null, always; this can't be blocked by walls
     */
    private Coord markRectangleWalled(Coord pos, int halfWidth, int halfHeight) {
        halfWidth = Math.max(1, Math.round(halfWidth * roomWidth));
        halfHeight = Math.max(1, Math.round(halfHeight * roomHeight));
        for (int i = pos.x - halfWidth; i <= pos.x + halfWidth; i++) {
            for (int j = pos.y - halfHeight; j <= pos.y + halfHeight; j++) {
                markPiercing(i, j);
                markEnvironmentRoom(i, j);
            }
        }
        for (int i = Math.max(0, pos.x - halfWidth - 1); i <= Math.min(width - 1, pos.x + halfWidth + 1); i++) {
            for (int j = Math.max(0, pos.y - halfHeight - 1); j <= Math.min(height - 1, pos.y + halfHeight + 1); j++) {
                wallOff(i, j);
            }
        }
        return null;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement. Marks
     * all cells in the circle as room floors.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null if no points in the circle were blocked by walls, otherwise a Coord blocked by a wall
     */
    private Coord markCircle(Coord pos, int radius) {
        Coord block = null;
        int high;
        radius = Math.max(1, Math.round(radius * Math.min(roomWidth, roomHeight)));
        for (int dx = -radius; dx <= radius; ++dx) {
            high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy) {
                if (mark(pos.x + dx, pos.y + dy))
                    block = pos.translate(dx, dy);
                else
                    markEnvironmentRoom(pos.x + dx, pos.y + dy);
            }
        }
        return block;
    }

    /**
     * Internal use. Marks a circle of points centered on pos, extending out to radius in Euclidean measurement.
     * Also considers the area just beyond each wall, but not corners, to be a blocking wall that can only be passed by
     * corridors and small cave openings. Marks all cells in the circle as room floors.
     * @param pos center position to mark
     * @param radius radius to extend in all directions from center
     * @return null, always; this can't be blocked by walls
     */
    private Coord markCircleWalled(Coord pos, int radius) {
        int high;
        radius = Math.max(1, Math.round(radius * Math.min(roomWidth, roomHeight)));
        for (int dx = -radius; dx <= radius; ++dx) {
            high = MathTools.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy) {
                markPiercing(pos.x + dx, pos.y + dy);
                markEnvironmentRoom(pos.x + dx, pos.y + dy);
            }
        }
        for (int dx = -radius; dx <= radius; ++dx) {
            high = MathTools.floor(Math.sqrt(radius * radius - dx * dx));
            int dx2 = Math.max(1, Math.min(pos.x + dx, width - 2));
            for (int dy = -high; dy <= high; ++dy) {
                int dy2 = Math.max(1, Math.min(pos.y + dy, height - 2));

                wallOff(dx2, dy2 - 1);
                wallOff(dx2 + 1, dy2 - 1);
                wallOff(dx2 - 1, dy2 - 1);
                wallOff(dx2, dy2);
                wallOff(dx2 + 1, dy2);
                wallOff(dx2 - 1, dy2);
                wallOff(dx2, dy2 + 1);
                wallOff(dx2 + 1, dy2 + 1);
                wallOff(dx2 - 1, dy2 + 1);

            }
        }
        return null;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step. Based on Michael Patraw's C code, used for cave carving.
     * <a href="http://mpatraw.github.io/libdrunkard/">Based on Michael Patraw's C code, used for cave carving</a>
     * (broken link).
     * @param current the current point
     * @param target the point to wobble towards
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random caves, 0.9 is almost a straight line.
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or NONE if we have reached our target
     */
    private Direction stepWobbly(Coord current, Coord target, float weight) {
        int dx = Math.min(Math.max(target.x - current.x, -1), 1);
        int dy = Math.min(Math.max(target.y - current.y, -1), 1);

        float r = rng.nextFloat();
        Direction dir;
        if ((dx | dy) == 0) {
            // both dx and dy are 0, produce no movement direction
            return Direction.NONE;
        } else if ((dx & dy) == 0) {
            // one of dx or dy is 0, so this is a cardinal direction
            // dx2 and dy2 are equal to dx and dy if dx is 0, or otherwise are swapped so dx2 is dy and dy2 is dx.
            int dx2 = (dx == 0) ? dx : dy, dy2 = (dx == 0) ? dy : dx;
            if (r >= (weight * 0.5f)) {
                r -= weight * 0.5f;
                if (r < (1 - weight * 0.5f) * (1f / 3)) {
                    dx2 = -1;
                    dy2 = 0;
                } else if (r < (1 - weight * 0.5f) * (2f / 3)) {
                    dx2 = 1;
                    dy2 = 0;
                } else {
                    dx2 = 0;
                    dy2 = -dy2;
                }
            }
            dir = Direction.getCardinalDirection(dx2, dy2);
        } else {
            // this is a diagonal; both dx and dy are non-zero
            if (r < weight * 0.5f) {
                dy = 0;
            } else if (r < weight) {
                dx = 0;
            } else if (r < (1 + weight) * 0.5f) {
                dx = -dx;
                dy = 0;
            } else {
                dx = 0;
                dy = -dy;
            }
            dir = Direction.getCardinalDirection(dx, dy);
        }
        if (current.x + dir.deltaX <= 0 || current.x + dir.deltaX >= width - 1) {
            if (current.y < target.y) dir = Direction.UP;
            else if (current.y > target.y) dir = Direction.DOWN;
            else dir = Direction.NONE;
        } else if (current.y + dir.deltaY <= 0 || current.y + dir.deltaY >= height - 1) {
            if (current.x < target.x) dir = Direction.RIGHT;
            else if (current.x > target.x) dir = Direction.LEFT;
            else dir = Direction.NONE;
        }
        return dir;
    }

    @Override
    public String toString() {
        return "MixedGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", roomWidth=" + roomWidth +
                ", roomHeight=" + roomHeight +
                '}';
    }
}
