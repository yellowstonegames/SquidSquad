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

package com.github.yellowstonegames.path;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntDeque;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.FlowRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordFloatOrderedMap;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.CoordSet;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.LineDrawer;
import com.github.yellowstonegames.grid.Measurement;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.path.technique.Technique;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * A group of pathfinding algorithms that explore in all directions equally, and are commonly used when there is more
 * than one valid goal, or when you want a gradient floodfill to mark each cell in an area with its distance from a
 * goal. This type of pathfinding is called a Dijkstra Map because it produces the same type of grid of
 * distances from the nearest goal as Dijkstra's Pathfinding Algorithm can, but the actual algorithm used here is
 * simpler than Dijkstra's Algorithm, and is more comparable to an optimized breadth-first search that doesn't consider
 * edge costs. You can set more than one goal with {@link #setGoal(Coord)} or {@link #setGoals(Iterable)}, unlike A*;
 * having multiple goals enables such features as pathfinding for creatures that can attack targets between a specified
 * minimum and maximum distance, and the standard uses of Dijkstra Maps such as finding ideal paths to run away. All
 * these features have some price; when paths are short or unobstructed, A* tends to be faster, though some convoluted
 * map shapes can slow down A* more than DijkstraMap.
 * <br>
 * One unique optimization made possible by Dijkstra Maps is for when only one endpoint of a path can change in some
 * section of a game, such as when you want to draw a path from the (stationary) player's current cell to the cell the
 * mouse is over, and the mouse can move quickly. This can be done very efficiently by setting the player as a goal with
 * {@link #setGoal(Coord)}, scanning the map to find distances with {@link #scan(Iterable)}, and then as long as the
 * player's position is unchanged (and no obstacles are added/moved), you can get the path by calling
 * {@link #findPathPreScanned(Coord)} and giving it the mouse position as a Coord. If various parts of the path can
 * change instead of just one (such as other NPCs moving around), then you should set a goal or goals and call
 * {@link #findPath(int, Collection, Collection, Coord, Coord...)}. The parameters for this are used in various methods
 * in this class with only slight differences: length is the length of path that can be moved "in one go," so 1 for most
 * roguelikes and more for most strategy games, impassable used for enemies and solid moving obstacles, onlyPassable can
 * be null in most roguelikes but in strategy games should contain ally positions that can be moved through as long as
 * no one stops in them (it can also contain terrain that must be jumped over without falling in, like lava), start is
 * the pathfinding NPC's starting position, and targets is an array or vararg of Coord that the NPC should pathfind
 * toward (it could be just one Coord, with or without explicitly putting it in an array, or it could be more and the
 * NPC will pick the closest).
 * <br>
 * As a bit of introduction, <a href="http://www.roguebasin.com/index.php?title=Dijkstra_Maps_Visualized">this article
 * on RogueBasin</a> can provide some useful information on how these work and how to visualize the information they can
 * produce, while
 * <a href="http://www.roguebasin.com/index.php?title=The_Incredible_Power_of_Dijkstra_Maps">the original article that
 * introduced Dijkstra Maps</a> is an inspiring list of the various features Dijkstra Maps can enable.
 * <br>
 * If you can't remember how to spell this, just remember: Does It Just Know Stuff? That's Really Awesome!
 * <br>
 * You shouldn't use DijkstraMap for all purposes; it isn't very good at handling terrains with a cost to enter, and
 * can't handle directional costs like a one-way ledge. For those tasks, {@link com.github.yellowstonegames.path.DefaultGraph}
 * or {@link com.github.yellowstonegames.path.CostlyGraph} will be better fits. CostlyGraph and similar versions of
 * {@link com.github.yellowstonegames.path.DirectedGraph} can handle even very complicated kinds
 * of map, including the types of pathfinding that were handled by CustomDijkstraMap in earlier versions of SquidLib.
 */
public class DijkstraMap {
    /**
     * Goals are by default marked with 0f. Some situations may have positions with lower values that are especially
     * urgent to move towards.
     */
    public static final float GOAL = 0f;
    /**
     * Floor cells, which include any walkable cell, are marked with a high number equal to 999200f .
     */
    public static final float FLOOR = 999200f;
    /**
     * Walls, which are solid no-entry cells, are marked with a high number equal to 999500f .
     */
    public static final float WALL = 999500f;
    /**
     * This is used to mark cells that the scan couldn't reach, and these dark cells are marked with a high number
     * equal to 999800f .
     */
    public static final float DARK = 999800f;


    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal directions. MANHATTAN should form a
     * diamond shape on a featureless map, while CHEBYSHEV and EUCLIDEAN will form a square. EUCLIDEAN does not affect
     * the length of paths, though it will change the DijkstraMap's gradientMap to have many non-integer values, and
     * that in turn will make paths this finds much more realistic and smooth (favoring orthogonal directions unless a
     * diagonal one is a better option).
     */
    public Measurement measurement = Measurement.MANHATTAN;


    /**
     * Stores which parts of the map are accessible and which are not. Should not be changed unless the actual physical
     * terrain has changed. You should call initialize() with a new map instead of changing this directly.
     */
    public float[][] physicalMap;
    /**
     * The frequently-changing values that are often the point of using this class; goals will have a value of 0, and
     * any cells that can have a character reach a goal in n steps will have a value of n. Cells that cannot be
     * entered because they are solid will have a very high value equal to the WALL constant in this class, and cells
     * that cannot be entered because they cannot reach a goal will have a different very high value equal to the
     * DARK constant in this class.
     */
    public float[][] gradientMap;
    /**
     * This stores the entry cost multipliers for each cell; that is, a value of 1f is a normal, unmodified cell, but
     * a value of 0.5f can be entered easily (two cells of its cost can be entered for the cost of one 1f cell), and a
     * value of 2f can only be entered with difficulty (one cell of its cost can be entered for the cost of two 1f
     * cells). Unlike the measurement field, this does affect the length of paths, as well as the numbers assigned
     * to gradientMap during a scan. The values for walls are identical to the value used by gradientMap, that is, this
     * class' WALL static final field. Floors, however, are never given FLOOR as a value, and default to 1f .
     */
    public float[][] costMap;

    public boolean standardCosts = true;
    /**
     * Height of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int height;
    /**
     * Width of the map. Exciting stuff. Don't change this, instead call initialize().
     */
    public int width;
    /**
     * The latest path that was obtained by calling findPath(). It will not contain the value passed as a starting
     * cell; only steps that require movement will be included, and so if the path has not been found or a valid
     * path toward a goal is impossible, this ObjectDeque will be empty.
     */
    public ObjectDeque<Coord> path;

    private CoordSet impassable2;

    private CoordSet friends;

    public boolean cutShort;

    /**
     * Goals that pathfinding will seek out. Each item is an encoded point, as done by {@link #encode(int, int)}.
     */
    protected IntList goals = new IntList(256);
    /**
     * Working data used during scanning, this tracks the perimeter of the scanned area so far. This is a member
     * variable and not a local one to avoid reallocating the data structure. Each item is an encoded point, as done by
     * {@link #encode(int, int)}.
     */
    protected IntDeque fresh = new IntDeque(256);

    /**
     * The FlowRandom used to decide which one of multiple equally-short paths to take; this has its state set
     * deterministically before any usage. There will only be one path produced for a given set of parameters, and it
     * will be returned again and again if the same parameters are requested.
     */
    protected FlowRandom rng = new FlowRandom(0L, 0x9E3779B97F4A7C15L);
    private int frustration;
    public Coord[][] targetMap;

    private final Direction[] dirs = new Direction[9];

    private boolean initialized;

    private int mappedCount;

    private int blockingRequirement = 2;

    private float cachedLongerPaths = 1.2f;
    private final CoordSet cachedImpassable = new CoordSet(32);
    private Coord[] cachedFearSources;
    private float[][] cachedFleeMap;
    private int cachedSize = 1;

    /**
     * Construct a DijkstraMap without a level to actually scan. If you use this constructor, you must call an
     * initialize() method before using this class.
     */
    public DijkstraMap() {
        path = new ObjectDeque<>();
    }

    /**
     * Used to construct a DijkstraMap from the output of another.
     *
     * @param level
     */
    public DijkstraMap(final float[][] level) {
        this(level, Measurement.MANHATTAN, false);
    }

    /**
     * Used to construct a DijkstraMap from the output of another, specifying a distance calculation.
     *
     * @param level
     * @param measurement
     */
    public DijkstraMap(final float[][] level, Measurement measurement) {
        this(level, measurement, false);
    }

    /**
     * Used to construct a DijkstraMap from either the output of another DijkstraMap, or from a resistance map of the
     * type used by {@link com.github.yellowstonegames.grid.FOV}. ALso specifies a distance calculation.
     *
     * @param level                a 2D float array that either was produced by {@link #scan()} or is a resistance map, depending on the last parameter
     * @param measurement          the distance calculation to use
     * @param levelIsResistanceMap if true, {@code level} will be treated as a resistance map; if false, the output of another DijkstraMap
     */
    public DijkstraMap(final float[][] level, Measurement measurement, boolean levelIsResistanceMap) {
        this.measurement = measurement;
        path = new ObjectDeque<>();
        if (levelIsResistanceMap)
            initializeByResistance(level);
        else
            initialize(level);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. This uses {@link Measurement#MANHATTAN}, allowing only 4-way
     * movement.
     *
     * @param level
     */
    public DijkstraMap(final char[][] level) {
        this(level, Measurement.MANHATTAN);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where one char means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. You can specify the character used for walls. This uses
     * {@link Measurement#MANHATTAN}, allowing only 4-way movement.
     *
     * @param level
     */
    public DijkstraMap(final char[][] level, char alternateWall) {
        path = new ObjectDeque<>();

        initialize(level, alternateWall);
    }

    /**
     * Constructor meant to take a char[][] returned by DungeonBoneGen.generate(), or any other
     * char[][] where '#' means a wall and anything else is a walkable tile. If you only have
     * a map that uses box-drawing characters, use DungeonUtility.linesToHashes() to get a
     * map that can be used here. Also takes a distance measurement, which you may want to set
     * to {@link Measurement#CHEBYSHEV} for unpredictable 8-way movement or
     * {@link Measurement#EUCLIDEAN} for more reasonable 8-way movement that prefers straight
     * lines.
     *
     * @param level       a char[x][y] map where '#' is a wall, and anything else is walkable
     * @param measurement how this should measure orthogonal vs. diagonal measurement, such as {@link Measurement#MANHATTAN} for 4-way only movement
     */
    public DijkstraMap(final char[][] level, Measurement measurement) {
        path = new ObjectDeque<>();
        this.measurement = measurement;

        initialize(level);
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D float array that should be used as the physicalMap for this DijkstraMap
     * @return this for chaining
     */
    public DijkstraMap initialize(final float[][] level) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
            costMap = new float[width][height];
            targetMap = new Coord[width][height];
        } else {
            ArrayTools.fill(targetMap, null);
        }
        for (int x = 0; x < width; x++) {
            System.arraycopy(level[x], 0, gradientMap[x], 0, height);
            System.arraycopy(level[x], 0, physicalMap[x], 0, height);
            Arrays.fill(costMap[x], 1f);
        }
        if (impassable2 == null)
            impassable2 = new CoordSet(32);
        else
            impassable2.clear();
        if (friends == null)
            friends = new CoordSet(32);
        else
            friends.clear();
        standardCosts = true;
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     *
     * @param level a 2D char array that this will use to establish which cells are walls ('#' as wall, others as floor)
     * @return this for chaining
     */
    public DijkstraMap initialize(final char[][] level) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
            costMap = new float[width][height];
            targetMap = new Coord[width][height];
        } else {
            ArrayTools.fill(targetMap, null);
        }
        for (int x = 0; x < width; x++) {
            Arrays.fill(costMap[x], 1f);
            for (int y = 0; y < height; y++) {
                float t = (level[x][y] == '#') ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        if (impassable2 == null)
            impassable2 = new CoordSet(32);
        else
            impassable2.clear();
        if (friends == null)
            friends = new CoordSet(32);
        else
            friends.clear();
        standardCosts = true;
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new PhysicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ). This
     * initialize() method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level         a 2D char array that this will use to establish which cells are walls (alternateWall defines the wall char, everything else is floor)
     * @param alternateWall the char to consider a wall when it appears in level
     * @return this for chaining
     */
    public DijkstraMap initialize(final char[][] level, char alternateWall) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
            costMap = new float[width][height];
            targetMap = new Coord[width][height];
        } else {
            ArrayTools.fill(targetMap, null);
        }
        for (int x = 0; x < width; x++) {
            Arrays.fill(costMap[x], 1f);
            for (int y = 0; y < height; y++) {
                float t = (level[x][y] == alternateWall) ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        if (impassable2 == null)
            impassable2 = new CoordSet(32);
        else
            impassable2.clear();
        if (friends == null)
            friends = new CoordSet(32);
        else
            friends.clear();
        standardCosts = true;
        initialized = true;
        return this;
    }

    /**
     * Used to initialize or re-initialize a DijkstraMap that needs a new physicalMap because it either wasn't given
     * one when it was constructed, or because the contents of the terrain have changed permanently (not if a
     * creature moved; for that you pass the positions of creatures that block paths to scan() or findPath() ).
     * This version takes a 2D float array that represents a resistance map, as is used by
     * {@link com.github.yellowstonegames.grid.FOV} and {@link com.github.yellowstonegames.grid.BresenhamLine}. A
     * resistance map has walls and other impassable cells use 1.0 or higher for their value, and fully passable cells
     * use 0.0 for their value.
     *
     * @param level a 2D float array that should be used as the physicalMap for this DijkstraMap
     * @return this for chaining
     */
    public DijkstraMap initializeByResistance(final float[][] level) {
        int oldWidth = width, oldHeight = height;
        width = level.length;
        height = level[0].length;
        if (width != oldWidth || height != oldHeight) {
            gradientMap = new float[width][height];
            physicalMap = new float[width][height];
            costMap = new float[width][height];
            targetMap = new Coord[width][height];
        } else {
            ArrayTools.fill(targetMap, null);
        }
        for (int x = 0; x < width; x++) {
            Arrays.fill(costMap[x], 1f);
            for (int y = 0; y < height; y++) {
                float t = (level[x][y] >= 1f) ? WALL : FLOOR;
                gradientMap[x][y] = t;
                physicalMap[x][y] = t;
            }
        }
        if (impassable2 == null)
            impassable2 = new CoordSet(32);
        else
            impassable2.clear();
        if (friends == null)
            friends = new CoordSet(32);
        else
            friends.clear();
        standardCosts = true;
        initialized = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a char[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, treating the '#' char as a wall (impassable) and anything else as having a normal cost to enter.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     *
     * @param level a 2D char array that uses '#' for walls
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final char[][] level) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        ArrayTools.fill(costMap, 1f);
        standardCosts = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a char[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, treating the '#' char as a wall (impassable) and anything else as having a normal cost to enter.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost().
     * <p/>
     * This method allows you to specify an alternate wall char other than the default character, '#' .
     *
     * @param level         a 2D char array that uses alternateChar for walls.
     * @param alternateWall a char to use to represent walls.
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final char[][] level, char alternateWall) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        ArrayTools.fill(costMap, 1f);
        standardCosts = true;
        return this;
    }

    /**
     * Used to initialize the entry cost modifiers for games that require variable costs to enter squares. This expects
     * a float[][] of the same exact dimensions as the 2D array that was used to previously initialize() this
     * DijkstraMap, using the exact values given in costs as the values to enter cells, even if they aren't what this
     * class would assign normally -- walls and other impassable values should be given WALL as a value, however.
     * The costs can be accessed later by using costMap directly (which will have a valid value when this does not
     * throw an exception), or by calling setCost(). Causes findPath() to always explore the full map instead of
     * stopping as soon as it finds any path, since unequal costs could make some paths cost less but be discovered
     * later in the pathfinding process.
     * <p/>
     * This method should be slightly more efficient than the other initializeCost methods.
     *
     * @param costs a 2D float array that already has the desired cost values
     * @return this DijkstraMap for chaining.
     */
    public DijkstraMap initializeCost(final float[][] costs) {
        if (!initialized) throw new IllegalStateException("DijkstraMap must be initialized first!");
        costMap = new float[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(costs[x], 0, costMap[x], 0, height);
        }
        standardCosts = false;
        return this;
    }

    /**
     * Internally, DijkstraMap uses int primitives instead of Coord objects. This method converts from a Coord to an
     * encoded int that stores the same information, but is somewhat more efficient to work with.
     *
     * @param point a Coord to find an encoded int for
     * @return an int that encodes the given Coord
     */
    public int encode(final Coord point) {
        return point.y << 16 | (point.x & 0xFFFF);
    }

    /**
     * Internally, DijkstraMap uses int primitives instead of Coord objects. This method converts from an x,y point to
     * an encoded int that stores the same information, but is somewhat more efficient to work with.
     *
     * @param x the x component of the point to find an encoded int for
     * @param y the y component of the point to find an encoded int for
     * @return an int that encodes the given x,y point
     */
    public int encode(final int x, final int y) {
        return y << 16 | (x & 0xFFFF);
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will convert
     * it back to a Coord if you need it as such. You may prefer using {@link #decodeX(int)} and  {@link #decodeY(int)}
     * to get the x and y components independently and without involving objects.
     *
     * @param encoded an encoded int specific to this DijkstraMap's height and width; see {@link #encode(Coord)}
     * @return the Coord that represents the same x,y position that the given encoded int stores
     */
    public Coord decode(final int encoded) {
        return Coord.get(encoded & 0xFFFF, encoded >>> 16);
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will decode
     * the x component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded & 0xFFFF}. You probably would use this method in
     * conjunction with {@link #decodeY(int)}, or would instead use {@link #decode(int)} to get a Coord.
     *
     * @param encoded an encoded int; see {@link #encode(Coord)}
     * @return the x component of the position that the given encoded int stores
     */
    public int decodeX(final int encoded) {
        return encoded & 0xFFFF;
    }

    /**
     * If you for some reason have one of the internally-used ints produced by {@link #encode(Coord)}, this will decode
     * the y component of the point encoded in that int. This is an extremely simple method that is equivalent to the
     * code {@code encoded >>> 16}. You probably would use this method in
     * conjunction with {@link #decodeX(int)}, or would instead use {@link #decode(int)} to get a Coord.
     *
     * @param encoded an encoded int; see {@link #encode(Coord)}
     * @return the y component of the position that the given encoded int stores
     */
    public int decodeY(final int encoded) {
        return encoded >>> 16;
    }

    /**
     * Resets the gradientMap to its original value from physicalMap.
     */
    public void resetMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            System.arraycopy(physicalMap[x], 0, gradientMap[x], 0, height);
        }
    }

    /**
     * Resets the targetMap (which is only assigned in the first place if you use findTechniquePath() ).
     */
    public void resetTargetMap() {
        if (!initialized) return;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                targetMap[x][y] = null;
            }
        }
    }


    /**
     * Resets this DijkstraMap to a state with no goals, no discovered path, and no changes made to gradientMap
     * relative to physicalMap.
     */
    public void reset() {
        resetMap();
        resetTargetMap();
        goals.clear();
        path.clear();
        fresh.clear();
        frustration = 0;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param x
     * @param y
     */
    public void setGoal(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        if (physicalMap[x][y] > FLOOR) {
            return;
        }

        goals.add(encode(x, y));
        gradientMap[x][y] = 0f;
    }

    /**
     * Marks a cell as a goal for pathfinding, unless the cell is a wall or unreachable area (then it does nothing).
     *
     * @param pt
     */
    public void setGoal(Coord pt) {
        if (!initialized || !pt.isWithin(width, height)) return;
        if (physicalMap[pt.x][pt.y] > FLOOR) {
            return;
        }

        goals.add(encode(pt));
        gradientMap[pt.x][pt.y] = 0f;

    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Possibly more efficient
     * than a loop that calls {@link #setGoal(Coord)} over and over, since this doesn't need to do a bounds check. The
     * Region passed to this should have the same (or smaller) width and height as this DijkstraMap.
     *
     * @param pts a Region containing "on" cells to treat as goals; should have the same width and height as this
     */
    public void setGoals(Region pts) {
        if (!initialized || pts.width > width || pts.height > height) return;
        for (Coord c : pts) {
            if (physicalMap[c.x][c.y] <= FLOOR) {
                goals.add(encode(c));
                gradientMap[c.x][c.y] = 0f;
            }
        }
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Coord)} on each Coord in pts.
     * If you have a Region, you should use it with {@link #setGoals(Region)}, which is faster.
     *
     * @param pts any Iterable of Coord, which can be a List, Set, Queue, etc. of Coords to mark as goals
     */
    public void setGoals(Iterable<Coord> pts) {
        if (!initialized) return;
        for (Coord c : pts) {
            setGoal(c);
        }
    }

    /**
     * Marks many cells as goals for pathfinding, ignoring cells in walls or unreachable areas. Simply loops through
     * pts and calls {@link #setGoal(Coord)} on each Coord in pts.
     *
     * @param pts an array of Coord to mark as goals
     */
    public void setGoals(Coord[] pts) {
        if (!initialized) return;
        for (int i = 0; i < pts.length; i++) {
            setGoal(pts[i]);
        }
    }

    /**
     * Marks a cell's cost for pathfinding as cost, unless the cell is a wall or unreachable area (then it always sets
     * the cost to the value of the WALL field).
     *
     * @param pt
     * @param cost
     */
    public void setCost(Coord pt, float cost) {
        if (!initialized || !pt.isWithin(width, height)) return;
        if (physicalMap[pt.x][pt.y] > FLOOR) {
            costMap[pt.x][pt.y] = 1f;
            return;
        }
        if (cost != 1f)
            standardCosts = false;
        costMap[pt.x][pt.y] = cost;
    }

    /**
     * Marks a cell's cost for pathfinding as cost, unless the cell is a wall or unreachable area (then it always sets
     * the cost to the value of the WALL field).
     *
     * @param x
     * @param y
     * @param cost
     */
    public void setCost(int x, int y, float cost) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        if (physicalMap[x][y] > FLOOR) {
            costMap[x][y] = 1f;
            return;
        }
        if (cost != 1f)
            standardCosts = false;
        costMap[x][y] = cost;
    }

    /**
     * Marks a specific cell in gradientMap as completely impossible to enter.
     *
     * @param x
     * @param y
     */
    public void setOccupied(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = WALL;
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param x
     * @param y
     */
    public void resetCell(int x, int y) {
        if (!initialized || x < 0 || x >= width || y < 0 || y >= height) return;
        gradientMap[x][y] = physicalMap[x][y];
    }

    /**
     * Reverts a cell to the value stored in the original state of the level as known by physicalMap.
     *
     * @param pt
     */
    public void resetCell(Coord pt) {
        if (!initialized || !pt.isWithin(width, height)) return;
        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
    }

    /**
     * Used to remove all goals and undo any changes to gradientMap made by having a goal present.
     */
    public void clearGoals() {
        if (!initialized)
            return;
        int sz = goals.size(), t;
        for (int i = 0; i < sz; i++) {
            resetCell(decodeX(t = goals.pop()), decodeY(t));
        }
    }

    private void setFresh(final int x, final int y, float counter) {
        if (x < 0 || x >= width || y < 0 || y >= height || gradientMap[x][y] < counter)
            return;
        gradientMap[x][y] = counter;
        fresh.addFirst(encode(x, y));
    }

    private void setFresh(final Coord pt, float counter) {
        if (!pt.isWithin(width, height) || gradientMap[pt.x][pt.y] < counter)
            return;
        gradientMap[pt.x][pt.y] = counter;
        fresh.addFirst(encode(pt));
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan() {
        return scan(null);
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan(final Iterable<Coord> impassable) {
        scan(null, impassable);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Iterable)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link ArrayTools#copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start      a Coord representing the location of the pathfinder; may be null, which has this scan the whole map
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void scan(final Coord start, final Iterable<Coord> impassable) {
        scan(start, impassable, false);
    }

    /**
     * Recalculate the Dijkstra map and return it. Cells in {@link #gradientMap} that had the lowest value
     * will be treated as goals if {@code nonZeroOptimum} is true; otherwise, only cells marked as goals with
     * {@link #setGoal(Coord)} will be considered goals and some overhead will be saved. The cells adjacent
     * to goals will have a value of 1, and cells progressively further from goals will have a value equal to
     * the distance from the nearest goal. The exceptions are walls, which will have a value defined by the
     * {@link #WALL} constant in this class, and areas that the scan was unable to reach, which will have a
     * value defined by the {@link #DARK} constant in this class (typically, these areas should not be used to
     * place NPCs or items and should be filled with walls). This uses the current {@link #measurement}. The
     * result is stored in the {@link #gradientMap} field, and nothing is returned. If you want the data
     * returned, you can use {@link #scan(Iterable)} (which calls this method with null for the start
     * parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link ArrayTools#copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param start          a Coord representing the location of the pathfinder; may be null, which has this scan the whole map
     * @param impassable     An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                       path that cannot be moved through; this can be null if there are no such obstacles.
     * @param nonZeroOptimum if the cell to pathfind toward should have a value of {@link #GOAL} (0f), this should be
     *                       false; if it should have a different value or if you don't know, it should be true
     */
    public void scan(final Coord start, final Iterable<Coord> impassable, final boolean nonZeroOptimum) {

        if (!initialized) return;
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;
        fresh.clear();
        fresh.addAll(goals);
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        float cs, dist;
        if (nonZeroOptimum) {
            float currentLowest = 999000f;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (gradientMap[x][y] <= FLOOR) {
                        if (gradientMap[x][y] < currentLowest) {
                            currentLowest = gradientMap[x][y];
                            fresh.clear();
                            fresh.add(encode(x, y));
                        } else if (gradientMap[x][y] == currentLowest) {
                            fresh.add(encode(x, y));
                        }
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && start.x == adjX && start.y == adjY && standardCosts) {
                            if (impassable != null) {
                                for (Coord pt : impassable) {
                                    if (pt != null && pt.isWithin(width, height))
                                        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
            }
        }
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit The maximum number of steps to scan outward from a goal.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit) {
        return partialScan(limit, null);
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, it will have a value of DARK if it was passable instead of the distance. The
     * exceptions are walls, which will have a value defined by the WALL constant in this class, and areas that the scan
     * was unable to reach, which will have a value defined by the DARK constant in this class. This uses the
     * current measurement. The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable A Collection of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit, final Iterable<Coord> impassable) {
        partialScan(null, limit, impassable);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }

        return gradientClone;
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. If a cell would take more steps to
     * reach than the given limit, or if it was otherwise unreachable, it will have a value of {@link #FLOOR} or greater
     * if it was passable instead of the distance. The exceptions are walls, which will have a value defined by the
     * {@link #WALL} constant in this class. This uses the current {@link #measurement}. The result is stored in the
     * {@link #gradientMap} field, and nothing is returned.If you want the data returned, you can use
     * {@link #partialScan(int, Iterable)} (which calls this method with null for the start parameter, then modifies
     * the gradientMap field and returns a copy), or you can just retrieve the gradientMap (maybe copying it;
     * {@link ArrayTools#copy(float[][])} is a convenient option for copying a 2D float array).
     * <br>
     * If start is non-null, which is usually used when finding a single path, then cells that didn't need to be
     * explored (because they were further than the path needed to go from start to goal) will have the value
     * {@link #FLOOR}. You may wish to assign a different value to these cells in some cases (especially if start is
     * null, which means any cells that are still FLOOR could not be reached from any goal), and the overloads of
     * partialScan that return 2D float arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to
     * {@link #WALL}.
     *
     * @param start      a Coord representing the location of the pathfinder; may be null to have this scan more of the map
     * @param limit      The maximum number of steps to scan outward from a goal.
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     */
    public void partialScan(final Coord start, final int limit, final Iterable<Coord> impassable) {
        partialScan(start, limit, impassable, false);
    }

    /**
     * Recalculate the Dijkstra map up to a limit and return it. Cells in {@link #gradientMap} that had the lowest value
     * will be treated as goals if {@code nonZeroOptimum} is true; otherwise, only cells marked as goals with
     * {@link #setGoal(Coord)} will be considered goals and some overhead will be saved. If a cell would take more steps
     * to reach than the given limit, or if it was otherwise unreachable, it will have a value of {@link #FLOOR} or
     * greater if it was passable instead of the distance. The exceptions are walls, which will have a value defined by
     * the {@link #WALL} constant in this class. This uses the current {@link #measurement}. The result is stored in the
     * {@link #gradientMap} field, and nothing is returned.If you want the data returned, you can use
     * {@link #partialScan(int, Iterable)} (which calls this method with null for the start parameter, then modifies
     * the gradientMap field and returns a copy), or you can just retrieve the gradientMap (maybe copying it;
     * {@link ArrayTools#copy(float[][])} is a convenient option for copying a 2D float array).
     * <br>
     * If start is non-null, which is usually used when finding a single path, then cells that didn't need to be
     * explored (because they were further than the path needed to go from start to goal) will have the value
     * {@link #FLOOR}. You may wish to assign a different value to these cells in some cases (especially if start is
     * null, which means any cells that are still FLOOR could not be reached from any goal), and the overloads of
     * partialScan that return 2D float arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to
     * {@link #WALL}.
     *
     * @param start          a Coord representing the location of the pathfinder; may be null to have this scan more of the map
     * @param limit          The maximum number of steps to scan outward from a goal.
     * @param impassable     An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                       path that cannot be moved through; this can be null if there are no such obstacles.
     * @param nonZeroOptimum if the cell to pathfind toward should have a value of {@link #GOAL} (0f), this should be
     *                       false; if it should have a different value or if you don't know, it should be true
     */
    public void partialScan(final Coord start, final int limit, final Iterable<Coord> impassable, final boolean nonZeroOptimum) {

        if (!initialized || limit <= 0) return;
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;
        fresh.clear();
        fresh.addAll(goals);
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            gradientMap[decodeX(dec)][decodeY(dec)] = GOAL;
        }
        float cs, dist;
        if (nonZeroOptimum) {
            float currentLowest = 999000;
            if (start == null) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (gradientMap[x][y] <= FLOOR) {
                            if (gradientMap[x][y] < currentLowest) {
                                currentLowest = gradientMap[x][y];
                                fresh.clear();
                                fresh.add(encode(x, y));
                            } else if (gradientMap[x][y] == currentLowest) {
                                fresh.add(encode(x, y));
                            }
                        }
                    }
                }
            } else {
                final int x0 = Math.max(0, start.x - limit), x1 = Math.min(start.x + limit + 1, width),
                        y0 = Math.max(0, start.y - limit), y1 = Math.min(start.y + limit + 1, height);
                for (int x = x0; x < x1; x++) {
                    for (int y = y0; y < y1; y++) {
                        if (gradientMap[x][y] <= FLOOR) {
                            if (gradientMap[x][y] < currentLowest) {
                                currentLowest = gradientMap[x][y];
                                fresh.clear();
                                fresh.add(encode(x, y));
                            } else if (gradientMap[x][y] == currentLowest) {
                                fresh.add(encode(x, y));
                            }
                        }
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && start.x == adjX && start.y == adjY && standardCosts) {
                            if (impassable != null) {
                                for (Coord pt : impassable) {
                                    if (pt != null && pt.isWithin(width, height))
                                        gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = physicalMap[pt.x][pt.y];
            }
        }
    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the Coords that this is trying to find; it will stop once it finds one
     * @return the Coord that it found first.
     */
    public Coord findNearest(Coord start, Collection<Coord> targets) {
        if (!initialized) return null;
        if (targets == null || targets.isEmpty())
            return null;
        if (targets.contains(start))
            return start;
        resetMap();
        Coord start2 = start;
        int xShift = width / 6, yShift = height / 6;
        rng.setState(start.hashCode(), targets.size());
        int frustration = 0;
        while (physicalMap[start2.x][start2.y] >= WALL && frustration++ < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        gradientMap[start2.x][start2.y] = 0f;
        int adjX, adjY, cen, cenX, cenY;
        float cs, dist;
        Coord adj;
        fresh.clear();
        fresh.add(encode(start2));
        int fsz, numAssigned = 1;
        mappedCount = 1;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        ++mappedCount;
                        if (targets.contains(adj = Coord.get(adjX, adjY))) {
                            return adj;
                        }
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first target found.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest target
     * @param targets the Coords that this is trying to find; it will stop once it finds one
     * @return the Coord that it found first.
     */
    public Coord findNearest(Coord start, Coord... targets) {
        return findNearest(start, CoordSet.with(targets));
    }

    /**
     * If you have a target or group of targets you want to pathfind to without scanning the full map, this can be good.
     * It may find sub-optimal paths in the presence of costs to move into cells. It is useful when you want to move in
     * a straight line to a known nearby goal.
     *
     * @param start   your starting location
     * @param targets an array or vararg of Coords to pathfind to the nearest of
     * @return an ObjectDeque of Coord that goes from a cell adjacent to start and goes to one of the targets. Copy of path.
     */
    public ObjectDeque<Coord> findShortcutPath(Coord start, Coord... targets) {
        if (targets.length == 0) {
            cutShort = true;
            path.clear();
            return new ObjectDeque<>(path);
        }
        Coord currentPos = findNearest(start, targets);
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = currentPos.x + dirs[d].deltaX;
                int adjY = currentPos.y + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][currentPos.y] > FLOOR ? 1 : 0)
                            + (gradientMap[currentPos.x][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                Coord pt = Coord.get(adjX, adjY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                return new ObjectDeque<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
            path.add(currentPos);
        }
        cutShort = false;
        path.reverse();
        return new ObjectDeque<>(path);
    }

    /**
     * Recalculate the Dijkstra map until it reaches a Coord in targets, then returns the first several targets found,
     * up to limit or less if the map is fully searched without finding enough.
     * This uses the current measurement.
     *
     * @param start   the cell to use as the origin for finding the nearest targets
     * @param limit   the maximum number of targets to find before returning
     * @param targets the Coords that this is trying to find; it will stop once it finds enough (based on limit)
     * @return the Coords that it found first.
     */
    public ObjectDeque<Coord> findNearestMultiple(Coord start, int limit, Collection<Coord> targets) {
        if (!initialized) return null;
        ObjectDeque<Coord> found = new ObjectDeque<>(limit);
        if (targets == null || targets.isEmpty())
            return found;
        if (targets.contains(start))
            return found;
        resetMap();
        Coord start2 = start;
        int xShift = width / 6, yShift = height / 6;
        rng.setState(start.hashCode(), targets.size());
        while (physicalMap[start2.x][start2.y] >= WALL && frustration < 50) {
            start2 = Coord.get(Math.min(Math.max(1, start.x + rng.nextInt(1 + xShift * 2) - xShift), width - 2),
                    Math.min(Math.max(1, start.y + rng.nextInt(1 + yShift * 2) - yShift), height - 2));
        }
        gradientMap[start2.x][start2.y] = 0f;
        int adjX, adjY, cen, cenX, cenY;
        float cs, dist;
        Coord adj;
        fresh.clear();
        fresh.add(encode(start2));
        int fsz, numAssigned = 1;
        mappedCount = 1;
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientMap[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientMap[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientMap[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientMap[adjX][adjY] <= FLOOR && cs < gradientMap[adjX][adjY]) {
                        ++mappedCount;
                        if (targets.contains(adj = Coord.get(adjX, adjY))) {
                            found.add(adj);
                            if (found.size() >= limit) {
                                fresh.clear();
                                return found;
                            }
                        }
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                    }
                }
            }
        }
        return found;
    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] scan(final Iterable<Coord> impassable, final int size) {
        scan(null, impassable, size);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #scan(Iterable, int)} (which calls this method with
     * null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link ArrayTools#copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of scan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void scan(final Coord start, final Iterable<Coord> impassable, final int size) {

        if (!initialized) return;
        float[][] gradientClone = ArrayTools.copy(gradientMap);
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if (gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if (physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        float currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        while (numAssigned > 0) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && start.x == adjX && start.y == adjY && standardCosts) {
                            if (impassable != null) {
                                for (Coord pt : impassable) {
                                    if (pt != null && pt.isWithin(width, height)) {
                                        for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height)) {
                    for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }


    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field and a copy is returned.
     *
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     * @return A 2D float[width][height] using the width and height of what this knows about the physical map.
     */
    public float[][] partialScan(final int limit, final Iterable<Coord> impassable, final int size) {
        partialScan(limit, null, impassable, size);
        float[][] gradientClone = new float[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
            System.arraycopy(gradientMap[x], 0, gradientClone[x], 0, height);
        }
        return gradientClone;

    }

    /**
     * Recalculate the Dijkstra map for a creature that is potentially larger than 1x1 cell and return it. The value of
     * a cell in the returned Dijkstra map assumes that a creature is square, with a side length equal to the passed
     * size, that its minimum-x, minimum-y cell is the starting cell, and that any cell with a distance number
     * represents the distance for the creature's minimum-x, minimum-y cell to reach it. Cells that cannot be entered
     * by the minimum-x, minimum-y cell because of sizing (such as a floor cell next to a maximum-x and/or maximum-y
     * wall if size is &gt; 1) will be marked as DARK. Cells that were marked as goals with setGoal will have
     * a value of 0, the cells adjacent to goals will have a value of 1, and cells progressively further
     * from goals will have a value equal to the distance from the nearest goal. The exceptions are walls,
     * which will have a value defined by the WALL constant in this class, and areas that the scan was
     * unable to reach, which will have a value defined by the DARK constant in this class. (typically,
     * these areas should not be used to place NPCs or items and should be filled with walls). This uses the
     * current measurement.  The result is stored in the {@link #gradientMap} field, and nothing is returned.
     * If you want the data returned, you can use {@link #partialScan(int, Iterable, int)} (which calls this method
     * with null for the start parameter, then modifies the gradientMap field and returns a copy), or you can
     * just retrieve the gradientMap (maybe copying it; {@link ArrayTools#copy(float[][])} is a
     * convenient option for copying a 2D float array). If start is non-null, which is usually used when
     * finding a single path, then cells that didn't need to be explored (because they were further than the
     * path needed to go from start to goal) will have the value {@link #FLOOR}. You may wish to assign a
     * different value to these cells in some cases (especially if start is null, which means any cells that
     * are still FLOOR could not be reached from any goal), and the overloads of partialScan that return 2D float
     * arrays do change FLOOR to {@link #DARK}, which is usually treated similarly to {@link #WALL}.
     *
     * @param impassable An Iterable of Coord keys representing the locations of enemies or other moving obstacles to a
     *                   path that cannot be moved through; this can be null if there are no such obstacles.
     * @param size       The length of one side of a square creature using this to find a path, i.e. 2 for a 2x2 cell
     *                   creature. Non-square creatures are not supported because turning is really hard.
     */
    public void partialScan(final int limit, final Coord start, final Iterable<Coord> impassable, final int size) {

        if (!initialized || limit <= 0) return;
        float[][] gradientClone = ArrayTools.copy(gradientMap);
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height))
                    gradientMap[pt.x][pt.y] = WALL;
            }
        }
        for (int xx = size; xx < width; xx++) {
            for (int yy = size; yy < height; yy++) {
                if (gradientMap[xx][yy] > FLOOR) {
                    for (int xs = xx, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = yy, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = WALL;
                        }
                    }
                }
            }
        }
        int dec, adjX, adjY, cen, cenX, cenY;

        PER_GOAL:
        for (int i = 0; i < goals.size(); i++) {
            dec = goals.get(i);
            for (int xs = decodeX(dec), xi = 0; xi < size && xs >= 0; xs--, xi++) {
                for (int ys = decodeY(dec), yi = 0; yi < size && ys >= 0; ys--, yi++) {
                    if (physicalMap[xs][ys] > FLOOR)
                        continue PER_GOAL;
                    gradientClone[xs][ys] = GOAL;
                }
            }
        }
        float currentLowest = 999000, cs, dist;
        fresh.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientClone[x][y] <= FLOOR) {
                    if (gradientClone[x][y] < currentLowest) {
                        currentLowest = gradientClone[x][y];
                        fresh.clear();
                        fresh.add(encode(x, y));
                    } else if (gradientClone[x][y] == currentLowest) {
                        fresh.add(encode(x, y));
                    }
                }
            }
        }
        int fsz, numAssigned = fresh.size();
        mappedCount = goals.size();
        Direction[] moveDirs = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;

        int iter = 0;
        while (numAssigned > 0 && iter++ < limit) {
            numAssigned = 0;
            fsz = fresh.size();
            for (int ci = fsz; ci > 0; ci--) {
                cen = fresh.removeLast();
                cenX = decodeX(cen);
                cenY = decodeY(cen);
                dist = gradientClone[cenX][cenY];

                for (int d = 0; d < moveDirs.length; d++) {
                    adjX = cenX + moveDirs[d].deltaX;
                    adjY = cenY + moveDirs[d].deltaY;
                    if (adjX < 0 || adjY < 0 || width <= adjX || height <= adjY)
                        /* Outside the map */
                        continue;
                    if (d >= 4 && blockingRequirement > 0) // diagonal
                    {
                        if ((gradientClone[adjX][cenY] > FLOOR ? 1 : 0)
                                + (gradientClone[cenX][adjY] > FLOOR ? 1 : 0)
                                >= blockingRequirement) {
                            continue;
                        }
                    }
                    float h = measurement.heuristic(moveDirs[d]);
                    cs = dist + h * costMap[adjX][adjY];
                    if (gradientClone[adjX][adjY] <= FLOOR && cs < gradientClone[adjX][adjY]) {
                        setFresh(adjX, adjY, cs);
                        ++numAssigned;
                        ++mappedCount;
                        if (start != null && start.x == adjX && start.y == adjY && standardCosts) {
                            if (impassable != null) {
                                for (Coord pt : impassable) {
                                    if (pt != null && pt.isWithin(width, height)) {
                                        for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                                            for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                                                gradientClone[xs][ys] = physicalMap[xs][ys];
                                            }
                                        }
                                    }
                                }
                            }
                            gradientMap = gradientClone;
                            return;
                        }
                    }
                }
            }
        }
        if (impassable != null) {
            for (Coord pt : impassable) {
                if (pt != null && pt.isWithin(width, height)) {
                    for (int xs = pt.x, xi = 0; xi < size && xs >= 0; xs--, xi++) {
                        for (int ys = pt.y, yi = 0; yi < size && ys >= 0; ys--, yi++) {
                            gradientClone[xs][ys] = physicalMap[xs][ys];
                        }
                    }
                }
            }
        }
        gradientMap = gradientClone;
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length, which represents movement in a system where
     * a single move can be multiple cells if length is greater than 1 and should usually be 1 in standard roguelikes;
     * if moving the full length of the list would place the mover in a position shared  by one of the positions in
     * onlyPassable (which is typically filled with friendly units that can be passed through in multi-cell-movement
     * scenarios), it will recalculate a move so that it does not pass into that cell. The keys in impassable should
     * be the positions of enemies and obstacles that cannot be moved  through, and will be ignored if there is a goal
     * overlapping one. This overload always scans the whole map; use
     * {@link #findPath(int, int, Collection, Collection, Coord, Coord...)} to scan a smaller area for performance reasons.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length       the length of the path to calculate
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findPath(int length, Collection<Coord> impassable,
                                       Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPath(length, -1, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed goals and start
     * point, and returns a list of Coord positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findPath(int length, int scanLimit, Collection<Coord> impassable,
                                       Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPath(null, length, scanLimit, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed goals and start
     * point, and returns a list of Coord positions (using the current measurement) needed to get closer
     * to the closest reachable goal. The maximum length of the returned list is given by length, which represents
     * movement in a system where a single move can be multiple cells if length is greater than 1 and should usually
     * be 1 in standard roguelikes; if moving the full length of the list would place the mover in a position shared
     * by one of the positions in onlyPassable (which is typically filled with friendly units that can be passed
     * through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of Coord, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer       an existing ObjectDeque of Coord that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param length       the length of the path to calculate
     * @param scanLimit    how many cells away from a goal to actually process; negative to process whole map
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findPath(ObjectDeque<Coord> buffer, int length, int scanLimit, Collection<Coord> impassable,
                                       Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        path.clear();
        if (!initialized || length <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2){
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        setGoals(targets);
        if (goals.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (scanLimit <= 0 || scanLimit < length)
            scan(start, impassable2);
        else
            partialScan(start, scanLimit, impassable2);
        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = currentPos.x + dirs[d].deltaX;
                int adjY = currentPos.y + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][currentPos.y] > FLOOR ? 1 : 0)
                            + (gradientMap[currentPos.x][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                Coord pt = Coord.get(adjX, adjY);
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findPath(buffer, length, scanLimit, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until preferredRange is
     * reached, or further from a goal if the preferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength     the length of the path to calculate
     * @param preferredRange the distance this unit will try to keep from a target
     * @param los            a BresenhamLine, OrthoLine, or other LineDrawer if the preferredRange should try to stay in line of sight, or null if LoS
     *                       should be disregarded.
     * @param impassable     a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findAttackPath(int moveLength, int preferredRange, LineDrawer los, Collection<Coord> impassable,
                                             Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(moveLength, preferredRange, preferredRange, los, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a BresenhamLine, OrthoLine, or other LineDrawer if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findAttackPath(int moveLength, int minPreferredRange, int maxPreferredRange, LineDrawer los,
                                             Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findAttackPath(null, moveLength, minPreferredRange, maxPreferredRange, los, impassable, onlyPassable, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell. In most roguelikes where
     * movement happens one cell at a time, moveLength should be 1; if it is higher then the path will prefer getting
     * further away from the target (using up most or all of moveLength) while minPreferredRange and maxPreferredRange
     * can be satisfied. This does ensure a pathfinder with a ranged weapon stays far from melee range, but it may not
     * be the expected behavior because it will try to find the best path rather than the shortest it can attack from.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of Coord, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer            an existing ObjectDeque of Coord that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param moveLength        the length of the path to calculate; almost always, the pathfinder will try to use this length in full to obtain the best range
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a BresenhamLine, OrthoLine, or other LineDrawer if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findAttackPath(ObjectDeque<Coord> buffer, int moveLength, int minPreferredRange, int maxPreferredRange, LineDrawer los,
                                             Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized || moveLength <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        float[][] resMap = new float[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1f : 0f;
                }
            }
        }
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2) {
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && moveLength == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(null, impassable2);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
        }
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {

                    for (Coord goal : targets) {
                        if (los == null || los.isReachable(x, y, goal.x, goal.y, resMap)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(null, impassable2);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
        }
        if (gradientMap[start.x][start.y] <= 0f) {
            cutShort = false;
            frustration = 0;
            goals.clear();
            if (buffer == null)
                return new ObjectDeque<>(path);
            else {
                buffer.addAll(path);
                return buffer;
            }

        }
        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = currentPos.x + dirs[d].deltaX;
                int adjY = currentPos.y + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][currentPos.y] > FLOOR ? 1 : 0)
                            + (gradientMap[currentPos.x][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                Coord pt = Coord.get(adjX, adjY);
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(Coord.get(currentPos.x, currentPos.y));
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findAttackPath(buffer, moveLength, minPreferredRange, maxPreferredRange, los, impassable2,
                            onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, where goals are
     * considered valid if they are at a valid range for the given Technique to hit at least one target
     * and ideal if that Technique can affect as many targets as possible from a cell that can be moved
     * to with at most movelength steps.
     * <br>
     * The return value of this method is the path to get to a location to attack, but on its own it
     * does not tell the user how to perform the attack.  It does set the targetMap 2D Coord array field
     * so that if your position at the end of the returned path is non-null in targetMap, it will be
     * a Coord that can be used as a target position for Technique.apply() . If your position at the end
     * of the returned path is null, then an ideal attack position was not reachable by the path.
     * <br>
     * This needs a char[][] dungeon as an argument because DijkstraMap does not always have a char[][]
     * version of the map available to it, and certain AOE implementations that a Technique uses may
     * need a char[][] specifically to determine what they affect.
     * <br>
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in allies
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios, and is also used considered an undesirable thing to affect for the Technique),
     * it will recalculate a move so that it does not pass into that cell.
     * <br>
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a target overlapping one.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param moveLength the maximum distance to try to pathfind out to; if a spot to use a Technique can be found
     *                   while moving no more than this distance, then the targetMap field in this object will have a
     *                   target Coord that is ideal for the given Technique at the x, y indices corresponding to the
     *                   last Coord in the returned path.
     * @param tech       a Technique that we will try to find an ideal place to use, and/or a path toward that place.
     * @param dungeon    a char 2D array with '#' for walls.
     * @param los        a BresenhamLine, OrthoLine, or other LineDrawer if the preferred range should try to stay in line of sight, or null if LoS
     *                   should be disregarded.
     * @param impassable locations of enemies or mobile hazards/obstacles that aren't in the map as walls
     * @param allies     called onlyPassable in other methods, here it also represents allies for Technique things
     * @param start      the Coord the pathfinder starts at.
     * @param targets    a Set of Coord, not an array of Coord or variable argument list as in other methods.
     * @return an ObjectDeque of Coord that represents a path to travel to get to an ideal place to use tech. Copy of path.
     */
    public ObjectDeque<Coord> findTechniquePath(int moveLength, Technique tech, float[][] dungeon, LineDrawer los,
                                                Collection<Coord> impassable, Collection<Coord> allies, Coord start, Collection<Coord> targets) {
        return findTechniquePath(null, moveLength, tech, dungeon, los, impassable, allies, start, targets);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, where goals are
     * considered valid if they are at a valid range for the given Technique to hit at least one target
     * and ideal if that Technique can affect as many targets as possible from a cell that can be moved
     * to with at most movelength steps.
     * <br>
     * The return value of this method is the path to get to a location to attack, but on its own it
     * does not tell the user how to perform the attack.  It does set the targetMap 2D Coord array field
     * so that if your position at the end of the returned path is non-null in targetMap, it will be
     * a Coord that can be used as a target position for Technique.apply() . If your position at the end
     * of the returned path is null, then an ideal attack position was not reachable by the path.
     * <br>
     * This needs a char[][] dungeon as an argument because DijkstraMap does not always have a char[][]
     * version of the map available to it, and certain AOE implementations that a Technique uses may
     * need a char[][] specifically to determine what they affect.
     * <br>
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in allies
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios, and is also used considered an undesirable thing to affect for the Technique),
     * it will recalculate a move so that it does not pass into that cell.
     * <br>
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a target overlapping one.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of Coord, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer     an existing ObjectDeque of Coord that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param moveLength the maximum distance to try to pathfind out to; if a spot to use a Technique can be found
     *                   while moving no more than this distance, then the targetMap field in this object will have a
     *                   target Coord that is ideal for the given Technique at the x, y indices corresponding to the
     *                   last Coord in the returned path.
     * @param tech       a Technique that we will try to find an ideal place to use, and/or a path toward that place.
     * @param dungeon    a char 2D array with '#' for walls.
     * @param los        a BresenhamLine, OrthoLine, or other LineDrawer if the preferred range should try to stay in line of sight, or null if LoS
     *                   should be disregarded.
     * @param impassable locations of enemies or mobile hazards/obstacles that aren't in the map as walls
     * @param allies     called onlyPassable in other methods, here it also represents allies for Technique things
     * @param start      the Coord the pathfinder starts at.
     * @param targets    a Collection of Coord, not an array of Coord or variable argument list as in other methods.
     * @return an ObjectDeque of Coord that represents a path to travel to get to an ideal place to use tech. Copy of path.
     */
    public ObjectDeque<Coord> findTechniquePath(ObjectDeque<Coord> buffer, int moveLength, Technique tech, float[][] dungeon, LineDrawer los,
                                                Collection<Coord> impassable, Collection<Coord> allies, Coord start, Collection<Coord> targets) {
        if (!initialized || moveLength <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        tech.setMap(dungeon);
        float[][] worthMap = new float[width][height];
        float[][] userDistanceMap;
        float paidLength = 0f;

        ArrayTools.fill(targetMap, null);

        path.clear();
        if (targets == null || targets.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2){
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (allies == null)
            friends.clear();
        else {
            friends.clear();
            friends.addAll(allies);
            friends.remove(start);
        }

        resetMap();
        setGoal(start);
        userDistanceMap = scan(impassable2);
        clearGoals();
        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }

        Measurement mess = measurement;

        scan(null, impassable2);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == FLOOR) {
                    gradientMap[x][y] = DARK;
                }
            }
        }

        clearGoals();

        Coord tempPt;
        CoordObjectOrderedMap<ObjectList<Coord>> ideal;
        // generate an array of the single best location to attack when you are in a given cell.
        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                tempPt = Coord.get(x, y);
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK || userDistanceMap[x][y] > moveLength * 2f)
                    continue;
                if (gradientMap[x][y] >= tech.aoe.getMinRange() && gradientMap[x][y] <= tech.aoe.getMaxRange()) {
                    for (Coord tgt : targets) {
                        if (los == null || los.isReachable(x, y, tgt.x, tgt.y, dungeon)) {
                            ideal = tech.idealLocations(tempPt, targets, allies);
                            if (!ideal.isEmpty()) {
                                targetMap[x][y] = ideal.keyAt(0);
                                worthMap[x][y] = ideal.getAt(0).size();
                                setGoal(x, y);
                            }
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        scan(null, impassable2);

        float currentDistance = gradientMap[start.x][start.y];
        if (currentDistance <= moveLength) {
            int[] g_arr = goals.toArray();

            goals.clear();
            setGoal(start);
            scan(null, impassable2, false);
            gradientMap[start.x][start.y] = moveLength;
            int decX, decY;
            float bestWorth = 0f;
            for (int g, ig = 0; ig < g_arr.length; ig++) {
                g = g_arr[ig];
                decX = decodeX(g);
                decY = decodeY(g);
                if (gradientMap[decX][decY] <= moveLength && worthMap[decX][decY] > bestWorth) {
                    goals.clear();
                    goals.add(g);
                    bestWorth = worthMap[decX][decY];
                } else if (gradientMap[decX][decY] <= moveLength && bestWorth > 0 && worthMap[decX][decY] == bestWorth) {
                    goals.add(g);
                }
            }
            resetMap();
            scan(impassable2);

        }

        measurement = mess;

        Coord currentPos = Coord.get(start.x, start.y);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y]) {
                if (friends.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findTechniquePath(buffer, moveLength, tech, dungeon, los, impassable2,
                            friends, start, targets);
                }
                break;
            }
            if (best > gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            frustration++;
            if (paidLength > moveLength - 1f) {
                if (friends.contains(currentPos)) {
                    impassable2.add(currentPos);
                    return findTechniquePath(buffer, moveLength, tech, dungeon, los, impassable2,
                            friends, start, targets);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * Scans the dungeon using DijkstraMap.scan() with the listed fearSources and start point, and returns a list
     * of Coord positions (using Manhattan distance) needed to get further from the closest fearSources, meant
     * for running away. The maximum length of the returned list is given by length; if moving the full
     * length of the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<Coord> findFleePath(int length, float preferLongerPaths, Collection<Coord> impassable,
                                           Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        return findFleePath(null, length, -1, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed fearSources and start
     * point, and returns a list of Coord positions (using this DijkstraMap's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<Coord> findFleePath(int length, int scanLimit, float preferLongerPaths, Collection<Coord> impassable,
                                           Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        return findFleePath(null, length, scanLimit, preferLongerPaths, impassable, onlyPassable, start, fearSources);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan or DijkstraMap.partialScan with the listed fearSources and start
     * point, and returns a list of Coord positions (using this DijkstraMap's metric) needed to get further from
     * the closest fearSources, meant for running away. The maximum length of the returned list is given by length,
     * which represents movement in a system where a single move can be multiple cells if length is greater than 1 and
     * should usually be 1 in standard roguelikes; if moving the full length of the list would place the mover in a
     * position shared by one of the positions in onlyPassable (which is typically filled with friendly units that can
     * be passed through in multi-cell-movement scenarios), it will recalculate a move so that it does not pass into
     * that cell. The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. However, scanLimit is not cached; if you use scanLimit then it is assumed you are using some
     * value for it that shouldn't change relative to the other parameters (like twice the length).
     * The full map will only be scanned if scanLimit is 0 or less; for positive scanLimit values this will scan only
     * that distance out from each goal, which can save processing time on maps where only a small part matters.
     * Generally, scanLimit should be significantly greater than length.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of Coord, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer            an existing ObjectDeque of Coord that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param length            the length of the path to calculate
     * @param scanLimit         how many steps away from a fear source to calculate; negative scans the whole map
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<Coord> findFleePath(ObjectDeque<Coord> buffer, int length, int scanLimit, float preferLongerPaths, Collection<Coord> impassable,
                                           Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        if (!initialized || length <= 0) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        path.clear();
        if (fearSources == null || fearSources.length < 1) {
            cutShort = true;
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }

        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2){
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            impassable2.addAll(onlyPassable);
        if (cachedSize == 1 && preferLongerPaths == cachedLongerPaths && impassable2.equals(cachedImpassable) &&
                Arrays.equals(fearSources, cachedFearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable.clear();
            cachedImpassable.addAll(impassable2);
            cachedFearSources = new Coord[fearSources.length];
            System.arraycopy(fearSources, 0, cachedFearSources, 0, fearSources.length);
            cachedSize = 1;
            resetMap();
            setGoals(fearSources);
            if (goals.isEmpty()) {
                cutShort = true;
                if (buffer == null)
                    return new ObjectDeque<>();
                else {
                    return buffer;
                }
            }

            if (scanLimit <= 0 || scanLimit < length)
                cachedFleeMap = scan(impassable2);
            else
                cachedFleeMap = partialScan(scanLimit, impassable2);


            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1f : -preferLongerPaths;
                }
            }

            if (scanLimit <= 0 || scanLimit < length) {
                scan(null, impassable2, true);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (gradientMap[x][y] == FLOOR) {
                            gradientMap[x][y] = DARK;
                        }
                    }
                    System.arraycopy(gradientMap[x], 0, cachedFleeMap[x], 0, height);
                }
            } else
                cachedFleeMap = partialScan(scanLimit, impassable2);
        }
        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), fearSources.length);

        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = currentPos.x + dirs[d].deltaX;
                int adjY = currentPos.y + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][currentPos.y] > FLOOR ? 1 : 0)
                            + (gradientMap[currentPos.x][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                Coord pt = Coord.get(adjX, adjY);
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[start.x][start.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            if (!path.isEmpty()) {
                Coord last = path.peekLast();
                if (gradientMap[last.x][last.y] <= gradientMap[currentPos.x][currentPos.y])
                    break;
            }
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findFleePath(buffer, length, scanLimit, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }
    }

    /**
     * For pathfinding creatures larger than 1x1 cell; scans the dungeon using DijkstraMap.scan with the listed goals
     * and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to the closest reachable
     * goal. The maximum length of the returned list is given by length; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size         the side length of the creature trying to find a path
     * @param length       the length of the path to calculate
     * @param impassable   a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start        the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets      a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */

    public ObjectDeque<Coord> findPathLarge(final int size, int length, Collection<Coord> impassable,
                                            Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        return findPathLarge(size, length, -1, impassable, onlyPassable, start, targets);
    }

    public ObjectDeque<Coord> findPathLarge(final int size, int length, final int scanLimit, Collection<Coord> impassable,
                                            Collection<Coord> onlyPassable, Coord start, Coord... targets) {

        if (!initialized) return null;
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2) {
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ObjectDeque<>(path);
        }

        if (length < 0)
            length = 0;
        if (scanLimit <= 0 || scanLimit < length)
            scan(start, impassable2, size);
        else
            partialScan(scanLimit, start, impassable2, size);

        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ObjectDeque<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findPathLarge(size, length, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ObjectDeque<>(path);
    }

    /**
     * For pathfinding creatures larger than 1x1 cell; scans the dungeon using DijkstraMap.scan with the listed goals
     * and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until preferredRange is
     * reached, or further from a goal if the preferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size           the side length of the creature trying to find a path
     * @param moveLength     the length of the path to calculate
     * @param preferredRange the distance this unit will try to keep from a target
     * @param los            a BresenhamLine, OrthoLine, or other LineDrawer if the preferredRange should try to stay in line of sight, or null if LoS
     *                       should be disregarded.
     * @param impassable     a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable   a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start          the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets        a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findAttackPathLarge(int size, int moveLength, int preferredRange, LineDrawer los, Collection<Coord> impassable,
                                                  Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (preferredRange < 0) preferredRange = 0;
        float[][] resMap = new float[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1f : 0f;
                }
            }
        }
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2) {
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && moveLength == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ObjectDeque<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2, size);
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (x + 2 < width && y + 2 < height && gradientMap[x][y] == preferredRange) {
                    for (Coord goal : targets) {
                        if (los == null
                                || los.isReachable(x, y, goal.x, goal.y, resMap)
                                || los.isReachable(x + 1, y, goal.x, goal.y, resMap)
                                || los.isReachable(x, y + 1, goal.x, goal.y, resMap)
                                || los.isReachable(x + 1, y + 1, goal.x, goal.y, resMap)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(impassable2, size);

        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ObjectDeque<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findAttackPathLarge(size, moveLength, preferredRange, los, impassable2, onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ObjectDeque<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed goals and start point, and returns a list
     * of Coord positions (using the current measurement) needed to get closer to a goal, until a cell is reached with
     * a distance from a goal that is at least equal to minPreferredRange and no more than maxPreferredRange,
     * which may go further from a goal if the minPreferredRange has not been met at the current distance.
     * The maximum length of the returned list is given by moveLength; if moving the full length of
     * the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a goal overlapping one.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size              the side length of the creature trying to find a path
     * @param moveLength        the length of the path to calculate
     * @param minPreferredRange the (inclusive) lower bound of the distance this unit will try to keep from a target
     * @param maxPreferredRange the (inclusive) upper bound of the distance this unit will try to keep from a target
     * @param los               a BresenhamLine, OrthoLine, or other LineDrawer if the preferredRange should try to stay in line of sight, or null if LoS
     *                          should be disregarded.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param targets           a vararg or array of Coord that this will try to pathfind toward
     * @return an ObjectDeque of Coord that will contain the min-x, min-y locations of this creature as it goes toward a target. Copy of path.
     */
    public ObjectDeque<Coord> findAttackPathLarge(int size, int moveLength, int minPreferredRange, int maxPreferredRange, LineDrawer los,
                                                  Collection<Coord> impassable, Collection<Coord> onlyPassable, Coord start, Coord... targets) {
        if (!initialized) return null;
        if (minPreferredRange < 0) minPreferredRange = 0;
        if (maxPreferredRange < minPreferredRange) maxPreferredRange = minPreferredRange;
        float[][] resMap = new float[width][height];
        if (los != null) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    resMap[x][y] = (physicalMap[x][y] == WALL) ? 1f : 0f;
                }
            }
        }
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2) {
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && moveLength == 1)
            impassable2.addAll(onlyPassable);

        resetMap();
        for (Coord goal : targets) {
            setGoal(goal);
        }
        if (goals.isEmpty()) {
            cutShort = true;
            return new ObjectDeque<>(path);
        }

        Measurement mess = measurement;
        if (measurement == Measurement.EUCLIDEAN) {
            measurement = Measurement.CHEBYSHEV;
        }
        scan(impassable2, size);
        goals.clear();

        for (int x = 0; x < width; x++) {
            CELL:
            for (int y = 0; y < height; y++) {
                if (gradientMap[x][y] == WALL || gradientMap[x][y] == DARK)
                    continue;
                if (x + 2 < width && y + 2 < height && gradientMap[x][y] >= minPreferredRange && gradientMap[x][y] <= maxPreferredRange) {
                    for (Coord goal : targets) {
                        if (los == null
                                || los.isReachable(x, y, goal.x, goal.y, resMap)
                                || los.isReachable(x + 1, y, goal.x, goal.y, resMap)
                                || los.isReachable(x, y + 1, goal.x, goal.y, resMap)
                                || los.isReachable(x + 1, y + 1, goal.x, goal.y, resMap)) {
                            setGoal(x, y);
                            gradientMap[x][y] = 0;
                            continue CELL;
                        }
                    }
                    gradientMap[x][y] = FLOOR;
                } else
                    gradientMap[x][y] = FLOOR;
            }
        }
        measurement = mess;
        scan(impassable2, size);

        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), targets.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ObjectDeque<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > moveLength - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findAttackPathLarge(size, moveLength, minPreferredRange, maxPreferredRange, los, impassable2,
                            onlyPassable, start, targets);
                }
                break;
            }
            if (gradientMap[currentPos.x][currentPos.y] == 0)
                break;
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ObjectDeque<>(path);
    }

    /**
     * Scans the dungeon using DijkstraMap.scan with the listed fearSources and start point, and returns a list
     * of Coord positions (using Manhattan distance) needed to get further from the closest fearSources, meant
     * for running away. The maximum length of the returned list is given by length; if moving the full
     * length of the list would place the mover in a position shared by one of the positions in onlyPassable
     * (which is typically filled with friendly units that can be passed through in multi-tile-
     * movement scenarios), it will recalculate a move so that it does not pass into that cell.
     * The keys in impassable should be the positions of enemies and obstacles that cannot be moved
     * through, and will be ignored if there is a fearSource overlapping one. The preferLongerPaths parameter
     * is meant to be tweaked and adjusted; higher values should make creatures prefer to escape out of
     * doorways instead of hiding in the closest corner, and a value of 1.2f should be typical for many maps.
     * The parameters size, preferLongerPaths, impassable, and the varargs used for fearSources will be cached, and
     * any subsequent calls that use the same values as the last values passed will avoid recalculating
     * unnecessary scans. Calls to findFleePath will cache as if size is 1, and may share a cache with this function.
     * The parameter size refers to the side length of a square unit, such as 2 for a 2x2 unit. The
     * parameter start must refer to the minimum-x, minimum-y cell of that unit if size is &gt; 1, and
     * all positions in the returned path will refer to movement of the minimum-x, minimum-y cell.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param size              the side length of the creature trying the find a path
     * @param length            the length of the path to calculate
     * @param preferLongerPaths Set this to 1.2f if you aren't sure; it will probably need tweaking for different maps.
     * @param impassable        a Set of impassable Coord positions that may change (not constant like walls); can be null
     * @param onlyPassable      a Set of Coord positions that this pathfinder cannot end a path occupying (typically allies); can be null
     * @param start             the start of the path, should correspond to the minimum-x, minimum-y position of the pathfinder
     * @param fearSources       a vararg or array of Coord positions to run away from
     * @return an ObjectDeque of Coord that will contain the locations of this creature as it goes away from fear sources. Copy of path.
     */
    public ObjectDeque<Coord> findFleePathLarge(int size, int length, float preferLongerPaths, Collection<Coord> impassable,
                                                Collection<Coord> onlyPassable, Coord start, Coord... fearSources) {
        if (!initialized) return null;
        path.clear();
        if (impassable == null)
            impassable2.clear();
        else if(impassable != impassable2) {
            impassable2.clear();
            impassable2.addAll(impassable);
        }
        if (onlyPassable != null && length == 1)
            impassable2.addAll(onlyPassable);
        if (fearSources == null || fearSources.length < 1) {
            cutShort = true;
            return new ObjectDeque<>(path);
        }
        if (size == cachedSize && preferLongerPaths == cachedLongerPaths && impassable2.equals(cachedImpassable)
                && Arrays.equals(fearSources, cachedFearSources)) {
            gradientMap = cachedFleeMap;
        } else {
            cachedLongerPaths = preferLongerPaths;
            cachedImpassable.clear();
            cachedImpassable.addAll(impassable2);
            cachedFearSources = new Coord[fearSources.length];
            System.arraycopy(fearSources, 0, cachedFearSources, 0, fearSources.length);
            cachedSize = size;
            resetMap();
            setGoals(fearSources);
            if (goals.isEmpty()) {
                cutShort = true;
                return new ObjectDeque<>(path);
            }

            scan(impassable2, size);

            for (int x = 0; x < gradientMap.length; x++) {
                for (int y = 0; y < gradientMap[x].length; y++) {
                    gradientMap[x][y] *= (gradientMap[x][y] >= FLOOR) ? 1f : (0f - preferLongerPaths);
                }
            }
            cachedFleeMap = scan(impassable2, size);
        }
        Coord currentPos = start;
        float paidLength = 0f;
        rng.setState(start.hashCode(), fearSources.length);
        while (true) {
            if (frustration > 500) {
                path.clear();
                break;
            }

            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                Coord pt = Coord.get(currentPos.x + dirs[d].deltaX, currentPos.y + dirs[d].deltaY);
                if (!pt.isWithin(width, height))
                    continue;
                if (gradientMap[pt.x][pt.y] < best && !impassable2.contains(pt)) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }
            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                frustration = 0;
                return new ObjectDeque<>(path);
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);

            if (!path.isEmpty()) {
                Coord last = path.peekLast();
                if (gradientMap[last.x][last.y] <= gradientMap[currentPos.x][currentPos.y])
                    break;
            }
            path.add(currentPos);
            paidLength += costMap[currentPos.x][currentPos.y];
            if (paidLength > length - 1f) {
                if (onlyPassable != null && onlyPassable.contains(currentPos)) {
                    frustration++;
                    impassable2.add(currentPos);
                    return findFleePathLarge(size, length, preferLongerPaths, impassable2, onlyPassable, start, fearSources);
                }
                break;
            }
        }
        cutShort = false;
        frustration = 0;
        goals.clear();
        return new ObjectDeque<>(path);
    }


    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This caches its result in a member field, path, which can be fetched after finding a path and will change with
     * each call to a pathfinding method.
     *
     * @param target the target cell
     * @return an ObjectDeque of Coord that make up the best path. Copy of path.
     */
    public ObjectDeque<Coord> findPathPreScanned(Coord target) {
        return findPathPreScanned(null, target);
    }

    /**
     * When you can control how often the (relatively time-intensive) scan() method is called, but may need simple paths
     * very frequently (such as for a path that follows the mouse), you can use this method to reduce the amount of work
     * needed to find paths. Needs scan() or partialScan() to already be called and at least one goal to already be set,
     * and does not restrict the length of the path or behave as if the pathfinder has allies or enemies.
     * <br>
     * This overload takes a buffer parameter, an ObjectDeque of Coord, that the results will be appended to. If the
     * buffer is null, a new ObjectDeque will be made and appended to. This caches its result in a member field, path,
     * which can be fetched after finding a path and will change with each call to a pathfinding method. Any existing
     * contents of buffer will not affect the path field of this DijkstraMap.
     *
     * @param buffer an existing ObjectDeque of Coord that will have the result appended to it (in-place); if null, this will make a new ObjectDeque
     * @param target the target cell
     * @return an ObjectDeque of Coord that make up the best path, appended to buffer (if non-null)
     */
    public ObjectDeque<Coord> findPathPreScanned(ObjectDeque<Coord> buffer, Coord target) {
        path.clear();
        if (!initialized || goals == null || goals.isEmpty()) {
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        Coord currentPos = target;
        if (gradientMap[currentPos.x][currentPos.y] <= FLOOR)
            path.add(currentPos);
        else {
            if (buffer == null)
                return new ObjectDeque<>();
            else {
                return buffer;
            }
        }
        rng.setState(target.hashCode(), 0x9E3779B97F4A7C15L);
        do {
            float best = gradientMap[currentPos.x][currentPos.y];
            appendDirToShuffle(rng);
            int choice = 0;

            for (int d = 0; d <= measurement.directionCount(); d++) {
                int adjX = currentPos.x + dirs[d].deltaX;
                int adjY = currentPos.y + dirs[d].deltaY;
                if (adjX < 0 || adjY < 0 || adjX >= width || adjY >= height)
                    /* Outside the map */
                    continue;
                if (dirs[d].isDiagonal() && blockingRequirement > 0) // diagonal
                {
                    if ((gradientMap[adjX][currentPos.y] > FLOOR ? 1 : 0)
                            + (gradientMap[currentPos.x][adjY] > FLOOR ? 1 : 0)
                            >= blockingRequirement)
                        continue;
                }
                Coord pt = Coord.get(adjX, adjY);
                if (gradientMap[pt.x][pt.y] < best) {
                    if (dirs[choice] == Direction.NONE || !path.contains(pt)) {
                        best = gradientMap[pt.x][pt.y];
                        choice = d;
                    }
                }
            }

            if (best >= gradientMap[currentPos.x][currentPos.y] || physicalMap[currentPos.x + dirs[choice].deltaX][currentPos.y + dirs[choice].deltaY] > FLOOR) {
                cutShort = true;
                if (buffer == null)
                    return new ObjectDeque<>(path);
                else {
                    buffer.addAll(path);
                    return buffer;
                }
            }
            currentPos = currentPos.translate(dirs[choice].deltaX, dirs[choice].deltaY);
            path.addFirst(currentPos);

        } while (gradientMap[currentPos.x][currentPos.y] != 0);
        cutShort = false;
        if (buffer == null)
            return new ObjectDeque<>(path);
        else {
            buffer.addAll(path);
            return buffer;
        }

    }

    /**
     * A simple limited flood-fill that returns a OrderedMap of Coord keys to the float values in the DijkstraMap, only
     * calculating out to a number of steps determined by limit. This can be useful if you need many flood-fills and
     * don't need a large area for each, or if you want to have an effect spread to a certain number of cells away.
     *
     * @param radius the number of steps to take outward from each starting position.
     * @param starts a vararg group of Coords to step outward from; this often will only need to be one Coord.
     * @return a CoordFloatOrderedMap, with float values; the starts are included in this with the value 0f .
     */
    public CoordFloatOrderedMap floodFill(int radius, Coord... starts) {
        if (!initialized) return null;
        CoordFloatOrderedMap fill = new CoordFloatOrderedMap();

        resetMap();
        for (Coord goal : starts) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return fill;

        partialScan(radius, null);
        float temp;
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                temp = gradientMap[x][y];
                if (temp < FLOOR) {
                    fill.put(Coord.get(x, y), temp);
                }
            }
        }
        goals.clear();
        return fill;
    }

    /**
     * A simple limited flood-fill that returns a OrderedMap of Coord keys to the float values in the DijkstraMap, only
     * calculating out to a number of steps determined by limit. This can be useful if you need many flood-fills and
     * don't need a large area for each, or if you want to have an effect spread to a certain number of cells away.
     *
     * @param radius the number of steps to take outward from each starting position.
     * @param starts any Iterable of Coords to step outward from; {@link java.util.Collections#singletonList(Object)}
     *               may be useful if you have only one start
     * @return a new CoordFloatOrderedMap; the starts are included in this with the value 0f .
     */
    public CoordFloatOrderedMap floodFill(int radius, Iterable<Coord> starts) {
        if (!initialized) return null;
        CoordFloatOrderedMap fill = new CoordFloatOrderedMap();

        resetMap();
        for (Coord goal : starts) {
            setGoal(goal.x, goal.y);
        }
        if (goals.isEmpty())
            return fill;

        partialScan(radius, null);
        float temp;
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                temp = gradientMap[x][y];
                if (temp < FLOOR) {
                    fill.put(Coord.get(x, y), temp);
                }
            }
        }
        goals.clear();
        return fill;
    }

    public int getMappedCount() {
        return mappedCount;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @return the current level of blocking required to stop a diagonal move
     */
    public int getBlockingRequirement() {
        return blockingRequirement;
    }

    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make thin diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move. If you haven't set
     * this yet, then the default is 2.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2 (the default), having two orthogonal obstacles adjacent to both the current cell and the cell the
     * pathfinder is trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the
     * north and a wall to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     *
     * @param blockingRequirement the desired level of blocking required to stop a diagonal move
     */
    public void setBlockingRequirement(int blockingRequirement) {
        this.blockingRequirement = Math.min(Math.max(blockingRequirement, 0), 2);
    }

    private void appendDirToShuffle(Random rng) {
        switch (measurement){
            case MANHATTAN:
                System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
                for (int i = 3; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }

                dirs[4] = Direction.NONE;
            break;
            case CHEBYSHEV:
                System.arraycopy(Direction.OUTWARDS, 0, dirs, 0, 8);
                for (int i = 7; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }
                dirs[8] = Direction.NONE;
            break;
            default:
                System.arraycopy(Direction.OUTWARDS, 0, dirs, 0, 8);
                for (int i = 3; i > 0; i--) {
                    // equivalent to rng.nextInt(i+1), but here it can omit an unnecessary check and be inlined.
                    final int r = (int) ((i + 1) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[i];
                    dirs[i] = t;
                }
                for (int j = 7; j > 4; j--) {
                    // equivalent to 4+rng.nextInt(j-3), but here it can omit an unnecessary check and be inlined.
                    final int r = 4 + (int) ((j - 3) * (rng.nextLong() & 0xFFFFFFFFL) >>> 32);
                    Direction t = dirs[r];
                    dirs[r] = dirs[j];
                    dirs[j] = t;
                }
                dirs[8] = Direction.NONE;
        }
    }
}
