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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordOrderedSet;
import com.github.yellowstonegames.grid.PoissonDisk;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.place.tileset.DungeonBoneGen;
import com.github.yellowstonegames.place.tileset.TilesetType;

import java.util.Arrays;

/**
 * A good way to create a more-complete dungeon, layering different effects and modifications on top of a dungeon
 * produced by DungeonBoneGen or another dungeon without such effects. This class uses
 * environment information for the dungeons it is given (or quickly generates such information if using DungeonBoneGen),
 * and uses that information to only place effects like grass or water where you specify, like "only in caves", or
 * "boulders should never be in rooms". Ensures only connected regions of the map are used by filling unreachable areas
 * with walls, and can find far-apart staircase positions if generate() is used or can keep existing staircases in a map
 * if generateRespectingStairs() is used.
 * <br>
 * The main technique for using this is simple: Construct a DungeonGenerator, usually with the desired width and height,
 * then call any feature adding methods that you want in the dungeon, like addWater(), addTraps, addGrass(), or
 * addDoors(). All of these methods except addDoors() take an int argument that corresponds to a constant in this class,
 * CAVE, CORRIDOR, or ROOM, or ALL, and they will only cause the requested feature to show up in that environment. Some
 * of these take different parameters, like addDoors() which needs to know if it should check openings that are two
 * cells wide to add a door and a wall to, or whether it should only add doors to single-cell openings. In the case of
 * addDoors(), it doesn't take an environment argument since doors almost always are between environments (rooms and
 * corridors), so placing them only within one or the other doesn't make sense. This class, unlike the normal
 * DungeonGenerator, also has an addLake() method, which, like addDoors(), doesn't take an environment parameter. It can
 * be used to turn a large section of what would otherwise be walls into a lake (of some character for deep lake cells
 * and some character for shallow lake cells), and corridors that cross the lake become bridges, shown as ':'. It should
 * be noted that because the lake fills walls, it doesn't change the connectivity of the map unless you can cross the
 * lake. There's also addMaze(), which does change the connectivity by replacing sections of impassable walls with
 * twisty, maze-like passages.
 * <br>
 * Once you've added any features to the generator's effects list, call generate() to get a char[][] with the
 * desired dungeon map, using a fixed repertoire of chars to represent the different features, with the exception of the
 * customization that can be requested from addLake(). If you use the libGDX text-based display module, you can change
 * what chars are shown by using addSwap() in TextCellFactory. After calling generate(), you can safely get the values
 * from the stairsUp and stairsDown fields, which are Coords that should be a long distance from each other but
 * connected in the dungeon. You may want to change those to staircase characters, but there's no requirement to do
 * anything with them. It's recommended that you keep the resulting char[][] maps in some collection that can be saved,
 * since DungeonProcessor only stores a temporary copy of the most recently-generated map. The Placement field of this
 * class, placement, can be used to find parts of a dungeon that fit certain qualities for the placement of items,
 * terrain features, or NPCs. If you don't need Placement, the simplest way to get random cells from the map is probably
 * to use {@link Region#Region(char[][], char)} with the dungeon this made and {@code '.'} as the parameters, then call
 * {@link Region#singleRandom(EnhancedRandom)} on that to choose random cells, optionally removing the chosen cells if you don't
 * want duplicates.
 * <br>
 * Example map with a custom-representation lake: https://gist.github.com/tommyettinger/0055075f9de59c452d25
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class DungeonProcessor implements PlaceGenerator{

    /**
     * The effects that can be applied to this dungeon. More may be added in future releases.
     */
    public enum FillEffect
    {
        /**
         * Water, represented by '~'
         */
        WATER,
        /**
         * Traps, represented by '^'
         */
        TRAPS,
        /**
         * Grass, represented by '"'
         */
        GRASS,
        /**
         * Boulders strewn about open areas, represented by '#' and treated as walls
         */
        BOULDERS,
        /**
         * Islands of ground, '.', surrounded by shallow water, ',', to place in water at evenly spaced points
         */
        ISLANDS
    }

    /**
     * Constant for features being added to all environment types.
     */
    public static final int ALL = 0,
    /**
     * Constant for features being added only to rooms.
     */
    ROOM = 1,
    /**
     * Constant for features being added only to corridors.
     */
    CORRIDOR = 2,
    /**
     * Constant for features being added only to caves.
     */
    CAVE = 3;

    /**
     * The effects that will be applied when generate is called. Strongly prefer using addWater, addDoors, addTraps,
     * and addGrass.
     */
    public ObjectIntMap<FillEffect> roomFX, corridorFX, caveFX;

    /**
     * Percentage of viable positions to fill with doors, represented by '+' for east-to-west connections or '/' for
     * north-to-south ones; this number will be negative if filling two-cell wide positions but will be made positive
     * when needed.
     */
    public int doorFX;
    /**
     * The char to use for deep lake cells.
     */
    public char deepLakeGlyph = '~';
    /**
     * The char to use for shallow lake cells.
     */
    public char shallowLakeGlyph = ',';
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake. Corridors
     * that are covered by a lake will become bridges, the glyph ':'.
     */
    public int lakeFX;
    /**
     * The approximate percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with maze. Corridors
     * that are covered by a maze will become part of its layout.
     */
    public int mazeFX;
    protected int height, width;
    public Coord stairsUp, stairsDown;
    public EnhancedRandom rng;
    protected EnhancedRandom rebuildRNG;
    protected boolean seedFixed;
    protected int environmentType = 1;

    protected char[][] dungeon;

    protected int[][] environment;

    private static final char[] passableChars = new char[]{'.', '"', '+', '/', '^', ',', '~', ':', '\ufefe', '\ufeff'};
    /**
     * Potentially important if you need to identify specific rooms, corridors, or cave areas in a map.
     */
    public RoomFinder finder;
    /**
     * Configured by this class after you call generate(), this Placement can be used to locate areas of the dungeon
     * that fit certain properties, like "out of sight from a door" or "a large flat section of wall that could be used
     * to place a straight-line object." You can use this as-needed; it does only a small amount of work at the start,
     * and does the calculations for what areas have certain properties on request.
     */
    public Placement placement;

    /**
     * Get the most recently generated char[][] dungeon out of this class. The
     * dungeon may be null if generate() or setDungeon() have not been called.
     * @return a char[][] dungeon, or null.
     */
    @Override
    public char[][] getPlaceGrid() {
        return dungeon;
    }
    /**
     * Get the most recently generated char[][] dungeon out of this class without any chars other than '#' or '.', for
     * walls and floors respectively. The dungeon may be null if generate() or setDungeon() have not been called.
     * @return a char[][] dungeon with only '#' for walls and '.' for floors, or null.
     */
    public char[][] getBarePlaceGrid() {
        return DungeonTools.simplifyDungeon(dungeon);
    }

    /**
     * Change the underlying char[][]; only affects toString(), getPlaceGrid(), and getBarePlaceGrid().
     * This always treats the environment of the dungeon as a plain rooms-and-corridors building, using
     * a {@link RoomFinder} to evaluate what is room and what is corridor.
     * @param dungeon a char[][], probably produced by an earlier call to this class and then modified.
     */
    public void setPlaceGrid(char[][] dungeon) {
        this.dungeon = dungeon;
        if(dungeon == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = dungeon.length;
        if(width > 0)
            height = dungeon[0].length;
        finder.reset(dungeon);
        environment = finder.environment;
    }

    /**
     * Change the underlying char[][]; only affects toString(), getPlaceGrid(), and getBarePlaceGrid().
     * Sets the environment to the specified one, which typically would have been produced by
     * {@link PlaceGenerator#getEnvironment()}.
     * @param dungeon a char[][], possibly produced by {@link PlaceGenerator#getPlaceGrid()}
     * @param environment an int[][], possibly produced at the same time as dungeon by {@link PlaceGenerator#getEnvironment()}
     */
    public void setPlaceGrid(char[][] dungeon, int[][] environment) {
        this.dungeon = dungeon;
        this.environment = environment;
        if(dungeon == null)
        {
            width = 0;
            height = 0;
            return;
        }
        width = dungeon.length;
        if(width > 0)
            height = dungeon[0].length;
        if(environment == null)
            finder.reset(dungeon);
        else finder.reset(dungeon, environment);
    }

    @Override
    public int[][] getEnvironment() {
        return environment;
    }

    /**
     * Height of the dungeon in cells.
     * @return Height of the dungeon in cells.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Width of the dungeon in cells.
     * @return Width of the dungeon in cells.
     */
    public int getWidth() {
        return width;
    }


    /**
     * Make a DungeonProcessor with a LaserRandom using a random seed, height 40, and width 40.
     */
    public DungeonProcessor()
    {
        rng = new LaserRandom();
        rebuildRNG = rng.copy();
        height = 40;
        width = 40;
        roomFX = new ObjectIntMap<>();
        corridorFX = new ObjectIntMap<>();
        caveFX = new ObjectIntMap<>();
    }

    /**
     * Make a DungeonProcessor with the given height and width; the RNG used for generating a dungeon and
     * adding features will be a LaserRandom using a random seed. If width or height is greater than 256, then this will
     * expand the Coord pool from its 256x256 default so it stores a reference to each Coord that might be used in the
     * creation of the dungeon (if width and height are 300 and 300, the Coord pool will be 300x300; if width and height
     * are 500 and 100, the Coord pool will be 500x256 because it won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     */
    public DungeonProcessor(int width, int height)
    {
    	this(width, height, new LaserRandom());
    }

    /**
     * Make a DungeonProcessor with the given height, width, and EnhancedRandom. Use this if you want to seed the random
     * number generator. If width or height is greater than 256, then this will expand the Coord pool from its 256x256
     * default, so it stores a reference to each Coord that might be used in the creation of the dungeon (if width and
     * height are 300 and 300, the Coord pool will be 300x300; if width and height are 500 and 100, the Coord pool will
     * be 500x256 because it won't shrink below the default size of 256x256).
     * @param width The width of the dungeon in cells
     * @param height The height of the dungeon in cells
     * @param rng The EnhancedRandom to use for all purposes in this class; can be any implementation that allows reading its state
     */
    public DungeonProcessor(int width, int height, EnhancedRandom rng)
    {
        Coord.expandPoolTo(width, height);
        this.rng = rng;
        rebuildRNG = this.rng.copy();
        this.height = height;
        this.width = width;
        roomFX = new ObjectIntMap<>();
        corridorFX = new ObjectIntMap<>();
        caveFX = new ObjectIntMap<>();
    }

    /**
     * Copies all fields from copying and makes a new DungeonGenerator.
     * @param copying the DungeonGenerator to copy
     */
    public DungeonProcessor(DungeonProcessor copying)
    {
        rng = copying.rng.copy();
        rebuildRNG = rng.copy();
        height = copying.height;
        width = copying.width;
        Coord.expandPoolTo(width, height);
        roomFX = new ObjectIntMap<>();
        corridorFX = new ObjectIntMap<>();
        caveFX = new ObjectIntMap<>();
        doorFX = copying.doorFX;
        lakeFX = copying.lakeFX;
        mazeFX = copying.mazeFX;
        deepLakeGlyph = copying.deepLakeGlyph;
        shallowLakeGlyph = copying.shallowLakeGlyph;
        dungeon = copying.dungeon;
        environment = copying.environment;
        environmentType = copying.environmentType;
        seedFixed = copying.seedFixed;
        stairsDown = copying.stairsDown;
        stairsUp = copying.stairsUp;
        finder = copying.finder;
        placement = copying.placement;
    }

    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage.
     * Each pool will have randomized volume that should fill or get very close to filling the requested
     * percentage, unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater
     * called, the latest call will take precedence. No islands will be placed with this variant, but the edge of the
     * water will be shallow, represented by ','.
     * @param env the environment to affect; 0 for "all environments," 1 for "rooms", 2 for "corridors," 3 for "caves"
     * @param percentage the percentage of floor cells to fill with water
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addWater(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                roomFX.remove(FillEffect.WATER);
                roomFX.put(FillEffect.WATER, percentage);
                break;
            case CORRIDOR:
                corridorFX.remove(FillEffect.WATER);
                corridorFX.put(FillEffect.WATER, percentage);
                break;
            case CAVE:
                caveFX.remove(FillEffect.WATER);
                caveFX.put(FillEffect.WATER, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.WATER))
                    roomFX.put(FillEffect.WATER, Math.min(100, roomFX.get(FillEffect.WATER) + percentage));
                else
                    roomFX.put(FillEffect.WATER, percentage);
                if(corridorFX.containsKey(FillEffect.WATER))
                    corridorFX.put(FillEffect.WATER, Math.min(100, corridorFX.get(FillEffect.WATER) + percentage));
                else
                    corridorFX.put(FillEffect.WATER, percentage);
                if(caveFX.containsKey(FillEffect.WATER))
                    caveFX.put(FillEffect.WATER, Math.min(100, caveFX.get(FillEffect.WATER) + percentage));
                else
                    caveFX.put(FillEffect.WATER, percentage);
        }
        return this;
    }
    /**
     * Turns the majority of the given percentage of floor cells into water cells, represented by '~'. Water will be
     * clustered into a random number of pools, with more appearing if needed to fill the percentage. Each pool will
     * have randomized volume that should fill or get very close to filling the requested percentage,
     * unless the pools encounter too much tight space. If this DungeonGenerator previously had addWater called, the
     * latest call will take precedence. If islandSpacing is greater than 1, then this will place islands of floor, '.',
     * surrounded by shallow water, ',', at about the specified distance with Euclidean measurement.
     * @param env the environment to affect; 0 for "all environments," 1 for "rooms", 2 for "corridors," 3 for "caves"
     * @param percentage the percentage of floor cells to fill with water
     * @param islandSpacing if greater than 1, islands will be placed randomly this many cells apart.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addWater(int env, int percentage, int islandSpacing)
    {
        addWater(env, percentage);

        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env) {
            case ROOM:
                roomFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    roomFX.put(FillEffect.ISLANDS, percentage);
                break;
            case CORRIDOR:
                corridorFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    corridorFX.put(FillEffect.ISLANDS, percentage);
                break;
            case CAVE:
                caveFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    caveFX.put(FillEffect.ISLANDS, percentage);
                break;
            default:
                roomFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    roomFX.put(FillEffect.ISLANDS, percentage);
                corridorFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    corridorFX.put(FillEffect.ISLANDS, percentage);
                caveFX.remove(FillEffect.ISLANDS);
                if(islandSpacing > 1)
                    caveFX.put(FillEffect.ISLANDS, percentage);
        }

        return this;
    }

    /**
     * Turns the majority of the given percentage of floor cells into grass cells, represented by '"'. Grass will be
     * clustered into a random number of patches, with more appearing if needed to fill the percentage. Each area will
     * have randomized volume that should fill or get very close to filling (two thirds of) the requested percentage,
     * unless the patches encounter too much tight space. If this DungeonGenerator previously had addGrass called, the
     * latest call will take precedence.
     * @param env the environment to affect; 0 for "all environments," 1 for "rooms", 2 for "corridors," 3 for "caves"
     * @param percentage the percentage of floor cells to fill with grass; this can vary quite a lot. It may be
     *                   difficult to fill very high (over 66%) percentages of map with grass, though you can do this by
     *                   giving a percentage of between 100 and 150.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addGrass(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                roomFX.remove(FillEffect.GRASS);
                roomFX.put(FillEffect.GRASS, percentage);
                break;
            case CORRIDOR:
                corridorFX.remove(FillEffect.GRASS);
                corridorFX.put(FillEffect.GRASS, percentage);
                break;
            case CAVE:
                caveFX.remove(FillEffect.GRASS);
                caveFX.put(FillEffect.GRASS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.GRASS))
                    roomFX.put(FillEffect.GRASS, Math.min(100, roomFX.get(FillEffect.GRASS) + percentage));
                else
                    roomFX.put(FillEffect.GRASS, percentage);
                if(corridorFX.containsKey(FillEffect.GRASS))
                    corridorFX.put(FillEffect.GRASS, Math.min(100, corridorFX.get(FillEffect.GRASS) + percentage));
                else
                    corridorFX.put(FillEffect.GRASS, percentage);
                if(caveFX.containsKey(FillEffect.GRASS))
                    caveFX.put(FillEffect.GRASS, Math.min(100, caveFX.get(FillEffect.GRASS) + percentage));
                else
                    caveFX.put(FillEffect.GRASS, percentage);
        }
        return this;
    }
    /**
     * Turns the given percentage of floor cells not already adjacent to walls into wall cells, represented by '#'.
     * If this DungeonGenerator previously had addBoulders called, the latest call will take precedence.
     * @param env the environment to affect; 0 for "all environments," 1 for "rooms", 2 for "corridors," 3 for "caves"
     * @param percentage the percentage of floor cells not adjacent to walls to fill with boulders.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addBoulders(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                roomFX.remove(FillEffect.BOULDERS);
                roomFX.put(FillEffect.BOULDERS, percentage);
                break;
            case CORRIDOR:
                corridorFX.remove(FillEffect.BOULDERS);
                corridorFX.put(FillEffect.BOULDERS, percentage);
                break;
            case CAVE:
                caveFX.remove(FillEffect.BOULDERS);
                caveFX.put(FillEffect.BOULDERS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.BOULDERS))
                    roomFX.put(FillEffect.BOULDERS, Math.min(100, roomFX.get(FillEffect.BOULDERS) + percentage));
                else
                    roomFX.put(FillEffect.BOULDERS, percentage);
                if(corridorFX.containsKey(FillEffect.BOULDERS))
                    corridorFX.put(FillEffect.BOULDERS, Math.min(100, corridorFX.get(FillEffect.BOULDERS) + percentage));
                else
                    corridorFX.put(FillEffect.BOULDERS, percentage);
                if(caveFX.containsKey(FillEffect.BOULDERS))
                    caveFX.put(FillEffect.BOULDERS, Math.min(100, caveFX.get(FillEffect.BOULDERS) + percentage));
                else
                    caveFX.put(FillEffect.BOULDERS, percentage);
        }
        return this;
    }
    /**
     * Turns the given percentage of viable doorways into doors, represented by '+' for doors that allow travel along
     * the x-axis and '/' for doors that allow travel along the y-axis. If doubleDoors is true,
     * 2-cell-wide openings will be considered viable doorways and will fill one cell with a wall, the other a door.
     * If this DungeonGenerator previously had addDoors called, the latest call will take precedence.
     * @param percentage the percentage of valid openings to corridors to fill with doors; should be between 10 and
     *                   20 if you want doors to appear more than a few times, but not fill every possible opening.
     * @param doubleDoors true if you want two-cell-wide openings to receive a door and a wall; false if only
     *                    one-cell-wide openings should receive doors. Usually, this should be true.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addDoors(int percentage, boolean doubleDoors)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        if(doubleDoors) percentage *= -1;
        doorFX = percentage;
        return this;
    }

    /**
     * Instructs the generator to add a winding section of corridors into a large area that can be filled without
     * overwriting rooms, caves, or the edge of the map; wall cells will become either '#' or '.' and corridors will be
     * overwritten. If the percentage is too high (40% is probably too high to adequately fill), this will fill less than
     * the requested percentage rather than fill multiple mazes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with maze.
     * @return this for chaining
     */
    public DungeonProcessor addMaze(int percentage)
    {

        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        mazeFX = percentage;
        return this;
    }

    /**
     * Instructs the generator to add a lake (here, of water) into a large area that can be filled without overwriting
     * rooms, caves, or the edge of the map; wall cells will become the deep lake glyph (here, '~'), unless they are
     * close to an existing room or cave, in which case they become the shallow lake glyph (here, ','), and corridors
     * that are "covered" by a lake will become bridges, the glyph ':'. If the percentage is too high (40% is probably
     * too high to adequately fill), this will fill less than the requested percentage rather than fill multiple lakes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake.
     * @return this for chaining
     */
    public DungeonProcessor addLake(int percentage)
    {
        return addLake(percentage, '~', ',');
    }

    /**
     * Instructs the generator to add a lake into a large area that can be filled without overwriting rooms, caves, or
     * the edge of the map; wall cells will become the char deepLake, unless they are close to an existing room or cave,
     * in which case they become the char shallowLake, and corridors that are "covered" by a lake will become bridges,
     * the glyph ':'. If the percentage is too high (40% is probably too high to adequately fill), this will fill less
     * than the requested percentage rather than fill multiple lakes.
     * @param percentage The percentage of non-room, non-cave, non-edge-of-map wall cells to try to fill with lake.
     * @param deepLake the char to use for deep lake cells, such as '~'
     * @param shallowLake the char to use for shallow lake cells, such as ','
     * @return this for chaining
     */
    public DungeonProcessor addLake(int percentage, char deepLake, char shallowLake)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        lakeFX = percentage;
        deepLakeGlyph = deepLake;
        shallowLakeGlyph = shallowLake;
        return this;
    }

    /**
     * Turns the given percentage of open area floor cells into trap cells, represented by '^'. Corridors that have no
     * possible way to move around a trap will not receive traps, ever. If this DungeonGenerator previously had
     * addTraps called, the latest call will take precedence.
     * @param env the environment to affect; 0 for "all environments," 1 for "rooms", 2 for "corridors," 3 for "caves"
     * @param percentage the percentage of valid cells to fill with traps; should be no higher than 5 unless
     *                   the dungeon floor is meant to be a kill screen or minefield.
     * @return this DungeonGenerator; can be chained
     */
    public DungeonProcessor addTraps(int env, int percentage)
    {
        if(percentage < 0) percentage = 0;
        if(percentage > 100) percentage = 100;
        switch (env)
        {
            case ROOM:
                roomFX.remove(FillEffect.TRAPS);
                roomFX.put(FillEffect.TRAPS, percentage);
                break;
            case CORRIDOR:
                corridorFX.remove(FillEffect.TRAPS);
                corridorFX.put(FillEffect.TRAPS, percentage);
                break;
            case CAVE:
                caveFX.remove(FillEffect.TRAPS);
                caveFX.put(FillEffect.TRAPS, percentage);
                break;
            default:
                if(roomFX.containsKey(FillEffect.TRAPS))
                    roomFX.put(FillEffect.TRAPS, Math.min(100, roomFX.get(FillEffect.TRAPS) + percentage));
                else
                    roomFX.put(FillEffect.TRAPS, percentage);
                if(corridorFX.containsKey(FillEffect.TRAPS))
                    corridorFX.put(FillEffect.TRAPS, Math.min(100, corridorFX.get(FillEffect.TRAPS) + percentage));
                else
                    corridorFX.put(FillEffect.TRAPS, percentage);
                if(caveFX.containsKey(FillEffect.TRAPS))
                    caveFX.put(FillEffect.TRAPS, Math.min(100, caveFX.get(FillEffect.TRAPS) + percentage));
                else
                    caveFX.put(FillEffect.TRAPS, percentage);
        }
        return this;
    }

    /**
     * Removes any door, water, or trap insertion effects that this DungeonGenerator would put in future dungeons.
     * @return this DungeonGenerator, with all effects removed. Can be chained.
     */
    public DungeonProcessor clearEffects()
    {
        roomFX.clear();
        corridorFX.clear();
        caveFX.clear();
        lakeFX = 0;
        mazeFX = 0;
        doorFX = 0;
        return this;
    }

    protected ObjectOrderedSet<Coord> removeAdjacent(ObjectOrderedSet<Coord> coll, Coord pt)
    {
        for(Coord temp : new Coord[]{Coord.get(pt.x + 1, pt.y), Coord.get(pt.x - 1, pt.y),
                Coord.get(pt.x, pt.y + 1), Coord.get(pt.x, pt.y - 1)})
        {
            coll.remove(temp);
        }

        return coll;
    }
    protected ObjectOrderedSet<Coord> removeAdjacent(ObjectOrderedSet<Coord> coll, Coord pt1, Coord pt2)
    {

        for(Coord temp : new Coord[]{Coord.get(pt1.x + 1, pt1.y), Coord.get(pt1.x - 1, pt1.y),
                Coord.get(pt1.x, pt1.y + 1), Coord.get(pt1.x, pt1.y - 1),
                Coord.get(pt2.x + 1, pt2.y), Coord.get(pt2.x - 1, pt2.y),
                Coord.get(pt2.x, pt2.y + 1), Coord.get(pt2.x, pt2.y - 1),})
        {
            if(!(temp.x == pt1.x && temp.y == pt1.y) && !(temp.x == pt2.x && temp.y == pt2.y))
                coll.remove(temp);
        }

        return coll;
    }
    protected ObjectOrderedSet<Coord> removeNearby(ObjectOrderedSet<Coord> coll, char[][] disallowed)
    {
        if(coll == null || disallowed == null || disallowed.length == 0 || disallowed[0].length == 0)
            return new ObjectOrderedSet<>();
        ObjectOrderedSet<Coord> next = new ObjectOrderedSet<>(coll.size());
        int width = disallowed.length, height = disallowed[0].length;
        COORD_WISE:
        for(Coord c : coll)
        {
            for (int x = Math.max(0, c.x - 1); x <= Math.min(width - 1, c.x + 1); x++) {

                for (int y = Math.max(0, c.y - 1); y <= Math.min(height - 1, c.y + 1); y++) {
                    if(disallowed[x][y] != '#')
                        continue COORD_WISE;
                }
            }
            next.add(c);
        }

        return next;
    }


    protected ObjectOrderedSet<Coord> viableDoorways(boolean doubleDoors, char[][] map, char[][] allCaves,
                                                  char[][] allCorridors)
    {
        ObjectOrderedSet<Coord> doors = new ObjectOrderedSet<>();
        ObjectOrderedSet<Coord> blocked = new ObjectOrderedSet<>(4);
        Region bounds = new Region(width, height), reuse = new Region(width, height);
        int[][] scan = new int[width][height];
        for(int x = 1; x < map.length - 1; x++) {
            for (int y = 1; y < map[x].length - 1; y++) {
                if(map[x][y] == '#' || allCorridors[x][y] != '#')
                    continue;
                if (doubleDoors) {
                    if (x >= map.length - 2 || y >= map[x].length - 2)
                        continue;
                    else {
                        if (map[x+1][y] != '#' &&
                                map[x + 2][y] == '#' && map[x - 1][y] == '#'
                                && map[x][y + 1] != '#' && map[x][y - 1] != '#'
                                && map[x+1][y + 1] != '#' && map[x+1][y - 1] != '#') {
                            if (map[x + 2][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 2][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                                reuse.clear();
                                reuse.insert(x, y+1);
                                bounds.refill(map, '.');
                                bounds.remove(x, y);
                                bounds.remove(x+1, y);
                                bounds.dijkstraScan(scan, reuse, 16);
                                if(scan[x][y-1] < Integer.MAX_VALUE)
                                    continue;
                                doors.add(Coord.get(x, y));
                                doors.add(Coord.get(x + 1, y));
                                doors = removeAdjacent(doors, Coord.get(x, y), Coord.get(x + 1, y));
                                continue;
                            }
                        } else if (map[x][y+1] != '#' &&
                                map[x][y + 2] == '#' && map[x][y - 1] == '#'
                                && map[x + 1][y] != '#' && map[x - 1][y] != '#'
                                && map[x + 1][y+1] != '#' && map[x - 1][y+1] != '#') {
                            if (map[x + 1][y + 2] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 2] != '#' || map[x - 1][y - 1] != '#') {
                                reuse.clear();
                                reuse.insert(x+1, y);
                                bounds.refill(map, '.');
                                bounds.remove(x, y);
                                bounds.remove(x, y+1);
                                bounds.dijkstraScan(scan, reuse, 16);
                                if(scan[x-1][y] < Integer.MAX_VALUE)
                                    continue;
                                doors.add(Coord.get(x, y));
                                doors.add(Coord.get(x, y+1));
                                doors = removeAdjacent(doors, Coord.get(x, y), Coord.get(x, y + 1));
                                continue;
                            }
                        }
                    }
                }
                if (map[x + 1][y] == '#' && map[x - 1][y] == '#' && map[x][y + 1] != '#' && map[x][y - 1] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x - 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y - 1] != '#') {
                        reuse.clear();
                        reuse.insert(x, y+1);
                        bounds.refill(map, '.');
                        bounds.remove(x, y);
                        bounds.dijkstraScan(scan, reuse, 16);
                        if(scan[x][y-1] < Integer.MAX_VALUE)
                            continue;
                        doors.add(Coord.get(x, y));
                        doors = removeAdjacent(doors, Coord.get(x, y));
                    }
                } else if (map[x][y + 1] == '#' && map[x][y - 1] == '#' && map[x + 1][y] != '#' && map[x - 1][y] != '#') {
                    if (map[x + 1][y + 1] != '#' || map[x + 1][y - 1] != '#' || map[x - 1][y + 1] != '#' || map[x - 1][y - 1] != '#') {
                        reuse.clear();
                        reuse.insert(x+1, y);
                        bounds.refill(map, '.');
                        bounds.remove(x, y);
                        bounds.dijkstraScan(scan, reuse, 16);
                        if(scan[x-1][y] < Integer.MAX_VALUE)
                            continue;
                        doors.add(Coord.get(x, y));
                        doors = removeAdjacent(doors, Coord.get(x, y));
                    }
                }

            }
        }

        return removeNearby(doors, allCaves);
    }

    /**
     * Generate a char[][] dungeon using TilesetType.DEFAULT_DUNGEON; this produces a dungeon appropriate for a level
     * of ruins or a partially constructed dungeon. This uses '#' for walls, '.' for floors, '~' for deep water, ',' for
     * shallow water, '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that provide
     * vertical passage. Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in
     * the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * @return a char[][] dungeon
     */
    public char[][] generate() {
        return generate(TilesetType.DEFAULT_DUNGEON);
    }

    /**
     * Generate a char[][] dungeon given a TilesetType; the comments in that class provide some opinions on what
     * each TilesetType value could be used for in a game. This uses '#' for walls, '.' for floors, '~' for deep water,
     * ',' for shallow water, '^' for traps, '+' for doors that provide horizontal passage, and '/' for doors that
     * provide vertical passage. Use the addDoors, addWater, addGrass, and addTraps methods of this class to request
     * these in the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * @see TilesetType
     * @param kind a TilesetType enum value, such as TilesetType.DEFAULT_DUNGEON
     * @return a char[][] dungeon
     */
    public char[][] generate(TilesetType kind)
    {
        rebuildRNG.setWith(rng);
        environmentType = kind.environment();
        DungeonBoneGen gen = new DungeonBoneGen(rng);
        char[][] map = DungeonTools.wallWrap(gen.generate(kind, width, height));

        seedFixed = false;
        Region flooder = new Region(width, height), reuse = new Region(width, height);
        int[][] scan = new int[width][height];
        int frustrated = 0;
        do {
            flooder.refill(map, '.');
            stairsUp = flooder.singleRandom(rng);
            reuse.empty().insert(stairsUp);
            flooder.dijkstraScan(scan, reuse);
            frustrated++;
        }while (reuse.size() < flooder.size() * 0.125f && frustrated < 32);
        int maxDijkstra = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(scan[i][j] == Integer.MAX_VALUE) {
                    map[i][j] = '#';
                }
                else if(scan[i][j] > maxDijkstra) {
                    maxDijkstra = scan[i][j];
                }
            }
        }
        stairsDown = reuse.refill(scan, (int) Math.ceil(maxDijkstra * 0.7),
                Integer.MAX_VALUE - 1).singleRandom(rng);
        finder = new RoomFinder(map, environmentType);
        innerGenerate();
        if(mazeFX > 0) {
            if(lakeFX > 0) {
                passableChars[passableChars.length - 2] = shallowLakeGlyph;
                passableChars[passableChars.length - 1] = deepLakeGlyph;
            } else {
                passableChars[passableChars.length - 2] = '\ufefe';
                passableChars[passableChars.length - 1] = '\ufeff';
            }

            flooder.refill(dungeon, passableChars);
            reuse.empty().insert(stairsUp).flood(flooder, Integer.MAX_VALUE).not().writeCharsInto(dungeon, '#');
            reuse.writeCharsInto(map, '#');
            finder.reset(map, environmentType);
        }
        return dungeon;
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated and an
     * environment as an int[][], which can be obtained with {@link PlaceGenerator#getEnvironment()}.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing maps while avoiding placing incongruous features in
     * areas where they don't fit, like a door in a cave or moss in a room.
     * This uses '#' for walls, '.' for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for doors
     * that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in the generated map.
     * Also sets the fields stairsUp and stairsDown to two randomly chosen, distant, connected, walkable cells.
     * <br>
     * Special behavior here: If tab characters are present in the 2D char array, they will be replaced with '.' in the
     * final dungeon, but will also be tried first as valid staircase locations (with a high distance possible to travel
     * away from the starting staircase). If no tab characters are present this will search for '.' floors to place
     * stairs on, as normal. This tab-first behavior is useful in conjunction with some methods that establish a good
     * path in an existing dungeon; an example is {@code DungeonTools.ensurePath(dungeon, rng, '\t', '#');} then
     * passing dungeon (which that code modifies) in as baseDungeon to this method. Because tabs will always be replaced
     * by floors ('.'), this considers any tabs that overlap with what the environment considers a wall (cave wall, room
     * wall, corridor wall, or untouched) to really refer to a corridor floor, but doesn't reconsider tabs that overlap
     * with floors already (it keeps the state of actual room, cave, and corridor floors). This is useful so you only
     * have to call ensurePath or a similar method on the 2D char array and can leave the 2D int array alone.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors; may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave; getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generate(char[][] baseDungeon, int[][] environment)
    {
        if(!seedFixed)
        {
            rebuildRNG.setWith(rng);
        }
        seedFixed = false;
        char[][] map = DungeonTools.wallWrap(baseDungeon);
        width = map.length;
        height = map[0].length;
        int[][] env2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(environment[x], 0, env2[x], 0, height);
        }
        Region flooder = new Region(map, '\t'), reuse = new Region(width, height);
        int[][] scan = new int[width][height];
        int frustrated = 0;
        if(flooder.size() > 0) {
            stairsUp = flooder.singleRandom(rng);
            reuse.insert(stairsUp);
            flooder.dijkstraScan(scan, reuse);
            if(reuse.size() < flooder.size() * 0.125f) flooder.clear();
        }
        if(flooder.size() == 0) {
            do {
                flooder.refill(map, '.');
                stairsUp = flooder.singleRandom(rng);
                reuse.clear();
                reuse.insert(stairsUp);
                flooder.dijkstraScan(scan, reuse);
                frustrated++;
            } while (reuse.size() < flooder.size() * 0.125f && frustrated < 32);
        }
        if(frustrated >= 32)
        {
            return generate();
        }

        int maxDijkstra = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if(scan[i][j] == Integer.MAX_VALUE) {
                    map[i][j] = '#';
                    env2[i][j] = DungeonTools.UNTOUCHED;
                }
                else if(scan[i][j] > maxDijkstra) {
                    maxDijkstra = scan[i][j];
                }
                if (map[i][j] == '\t') {
                    map[i][j] = '.';
                    if((env2[i][j] & 1) == 0) // environment is a wall here
                        env2[i][j] = DungeonTools.CORRIDOR_FLOOR;
                }

            }
        }
        if(maxDijkstra < (width + height) >>> 2) {
            return generate();
        }
        stairsDown = reuse.refill(scan, (int) Math.ceil(maxDijkstra * 0.7),
                Integer.MAX_VALUE - 1).singleRandom(rng);
        finder = new RoomFinder(map, env2);
        innerGenerate();
        if(mazeFX > 0) {
            flooder.refill(dungeon, passableChars);
            reuse.empty().insert(stairsUp).flood(flooder, Integer.MAX_VALUE).not().writeCharsInto(dungeon, '#');
            reuse.writeCharsInto(map, '#');
            reuse.writeIntsInto(env2, DungeonTools.UNTOUCHED);
            finder.reset(map, env2);
        }
        return dungeon;
    }

    /**
     * Generate a char[][] dungeon with extra features given a baseDungeon that has already been generated, with
     * staircases represented by greater than and less than signs, and an environment as an int[][], which can often be
     * obtained with {@link PlaceGenerator#getEnvironment()}.
     * Typically, you want to call generate with a TilesetType or no argument for the easiest generation; this method
     * is meant for adding features like water and doors to existing maps while avoiding placing incongruous features in
     * areas where they don't fit, like a door in a cave or moss in a room.
     * This uses '#' for walls, '.' for floors, '~' for deep water, ',' for shallow water, '^' for traps, '+' for doors
     * that provide horizontal passage, and '/' for doors that provide vertical passage.
     * Use the addDoors, addWater, addGrass, and addTraps methods of this class to request these in the generated map.
     * Also sets the fields stairsUp and stairsDown to null, and expects stairs to be already handled.
     * @param baseDungeon a pre-made dungeon consisting of '#' for walls and '.' for floors, with stairs already in;
     *                    may be modified in-place
     * @param environment stores whether a cell is room, corridor, or cave; getEnvironment() typically gives this
     * @return a char[][] dungeon
     */
    public char[][] generateRespectingStairs(char[][] baseDungeon, int[][] environment) {
        if(!seedFixed)
        {
            rebuildRNG.setWith(rng);
        }
        seedFixed = false;
        char[][] map = DungeonTools.wallWrap(baseDungeon);
        int[][] env2 = new int[width][height];
        for (int x = 0; x < width; x++) {
            System.arraycopy(environment[x], 0, env2[x], 0, height);
        }
        stairsUp = null;
        stairsDown = null;

        Region flooder = new Region(map, '.');
        Region reuse = new Region(map, new char[]{ '<', '>'});
        int[][] scan = new int[width][height];
        flooder.dijkstraScan(scan, reuse);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (scan[i][j] == Integer.MAX_VALUE) {
                    map[i][j] = '#';
                    env2[i][j] = DungeonTools.UNTOUCHED;
                }
            }
        }
        finder = new RoomFinder(map, env2);
        innerGenerate();
        if(mazeFX > 0) {
            flooder.refill(dungeon, passableChars);
            reuse.empty().insert(stairsUp).flood(flooder, Integer.MAX_VALUE).not().writeCharsInto(dungeon, '#');
            reuse.writeCharsInto(map, '#');
            reuse.writeIntsInto(env2, DungeonTools.UNTOUCHED);
            finder.reset(map, env2);
        }
        return dungeon;
    }



    protected char[][] innerGenerate() {
        dungeon = ArrayTools.fill('#', width, height);
        ObjectList<char[][]> rm = finder.findRooms(),
                cr = finder.findCorridors(),
                cv = finder.findCaves();
        char[][] roomMap = innerGenerate(RoomFinder.merge(rm, width, height), roomFX),
                allCorridors = RoomFinder.merge(cr, width, height),
                corridorMap = innerGenerate(allCorridors, corridorFX),
                allCaves = RoomFinder.merge(cv, width, height),
                caveMap = innerGenerate(allCaves, caveFX),
                doorMap;
        char[][][] lakesAndMazes = makeLake(rm, cv);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (corridorMap[x][y] != '#' && lakesAndMazes[0][x][y] != '#')
                    dungeon[x][y] = ':';
                else if (roomMap[x][y] != '#')
                    dungeon[x][y] = roomMap[x][y];
                else if (lakesAndMazes[1][x][y] != '#') {
                    dungeon[x][y] = lakesAndMazes[1][x][y];
                    finder.environment[x][y] = DungeonTools.CORRIDOR_FLOOR;
                } else if (corridorMap[x][y] != '#')
                    dungeon[x][y] = corridorMap[x][y];
                else if (caveMap[x][y] != '#')
                    dungeon[x][y] = caveMap[x][y];
                else if (lakesAndMazes[0][x][y] != '#') {
                    dungeon[x][y] = lakesAndMazes[0][x][y];
                    finder.environment[x][y] = DungeonTools.NATURAL_FLOOR;
                }
            }
        }
        finder = new RoomFinder(dungeon, finder.environment);
        rm = finder.findRooms();
        cr = finder.findCorridors();
        cv = finder.findCaves();
        cv.add(lakesAndMazes[0]);
        allCaves = RoomFinder.merge(cv, width, height);
        doorMap = makeDoors(rm, cr, allCaves, allCorridors);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (doorMap[x][y] == '+' || doorMap[x][y] == '/')
                    dungeon[x][y] = doorMap[x][y];
                else if (doorMap[x][y] == '*')
                    dungeon[x][y] = '#';

            }
        }
        placement = new Placement(finder);
        return dungeon;

    }
    protected char[][] makeDoors(ObjectList<char[][]> rooms, ObjectList<char[][]> corridors, char[][] allCaves,
                               char[][] allCorridors)
    {
        char[][] map = new char[width][height];
        for (int x = 0; x < width; x++) {
            Arrays.fill(map[x], '#');
        }
        if(doorFX == 0 || (rooms.isEmpty() && corridors.isEmpty()))
            return map;
        boolean doubleDoors = false;
        int doorFill = doorFX;
        if(doorFill < 0)
        {
            doubleDoors = true;
            doorFill *= -1;
        }
        ObjectList<char[][]> fused = new ObjectList<>(rooms.size() + corridors.size());
        fused.addAll(rooms);
        fused.addAll(corridors);

        map = RoomFinder.merge(fused, width, height);

        ObjectOrderedSet<Coord> doorways = viableDoorways(doubleDoors, map, allCaves, allCorridors);


        int total = doorways.size() * doorFill / 100;

        BigLoop:
        for(int i = 0; i < total; i++) {
            Coord entry = doorways.random(rng);
            if (map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '>')
                continue;
            if (map[entry.x - 1][entry.y] != '#' && map[entry.x + 1][entry.y] != '#' &&
                    map[entry.x - 1][entry.y] != '*' && map[entry.x + 1][entry.y] != '*') {
                map[entry.x][entry.y] = '+';
            } else {
                map[entry.x][entry.y] = '/';
            }
            Coord[] adj = new Coord[]{Coord.get(entry.x + 1, entry.y), Coord.get(entry.x - 1, entry.y),
                    Coord.get(entry.x, entry.y + 1), Coord.get(entry.x, entry.y - 1)};
            for (Coord near : adj) {
                if (doorways.contains(near)) {
                    map[near.x][near.y] = '*';
                    doorways.remove(near);
                    doorways.remove(entry);
                    i++;
                    continue BigLoop;
                }
            }
            doorways.remove(entry);
        }
        return map;

    }
    protected char[][][] makeLake(ObjectList<char[][]> rooms, ObjectList<char[][]> caves)
    {
        char[][][] maps = new char[2][width][height];
        char[][] fusedMap;
        for (int x = 0; x < width; x++) {
            Arrays.fill(maps[0][x], '#');
            Arrays.fill(maps[1][x], '#');
        }
        if((lakeFX == 0 && mazeFX == 0) || (rooms.isEmpty() && caves.isEmpty()))
            return maps;
        int lakeFill = lakeFX, mazeFill = mazeFX;
        if(lakeFX + mazeFX > 100)
        {
            lakeFill -= (lakeFX + mazeFX - 100) / 2;
            mazeFill -= (lakeFX + mazeFX - 99) / 2;
        }

        ObjectList<char[][]> fused = new ObjectList<>(rooms.size() + caves.size());
        fused.addAll(rooms);
        fused.addAll(caves);

        fusedMap = RoomFinder.merge(fused, width, height);
        Region limit = new Region(width, height).insertRectangle(1, 1, width - 2, height - 2),
                potential = new Region(fusedMap, '#').and(limit),
                flooded = new Region(width, height), chosen, tmp = new Region(width, height),
                deep = new Region(width, height);
        int ctr = potential.size(), potentialMazeSize = ctr * mazeFill / 100, potentialLakeSize = ctr * lakeFill / 100;
        ObjectList<Region> viable;
        int minSize;
        Coord center;
        if(potentialMazeSize > 0) {
            viable = potential.split();
            if (viable.isEmpty())
                return maps;

            chosen = viable.get(0);
            minSize = chosen.size();
            for (Region sa : viable) {
                int sz = sa.size();
                if (sz > minSize) {
                    chosen = sa;
                    minSize = sz;
                }
            }
            ConnectingMapGenerator cmg = new ConnectingMapGenerator(width, height, 1, 1, rng, 1);
            char[][] cmgMap = cmg.generate();
            center = chosen.singleRandom(rng);
            flooded.empty().insert(center).spill(chosen, potentialMazeSize, rng).and(limit);
            deep.refill(cmgMap, '.').and(flooded).removeIsolated();

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    if (deep.contains(x, y))
                        maps[1][x][y] = cmgMap[x][y];
                }
            }
            finder.corridors.put(deep, new ObjectList<Region>());
            finder.allCorridors.or(deep);
            finder.allFloors.or(deep);
            potential.andNot(flooded);
        }
        if(potentialLakeSize > 0) {
            viable = potential.split();
            if (viable.isEmpty())
                return maps;
            chosen = viable.get(0);
            minSize = chosen.size();
            for (Region sa : viable) {
                int sz = sa.size();
                if (sz > minSize) {
                    chosen = sa;
                    minSize = sz;
                }
            }
            center = chosen.singleRandom(rng);
            flooded.empty().insert(center).spill(chosen, potentialLakeSize, rng).and(limit);

            deep.remake(flooded);
            flooded.flood(new Region(fusedMap, '.').fringe8way(3), 3).and(limit);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (deep.contains(x, y))
                        maps[0][x][y] = deepLakeGlyph;
                    else if (flooded.contains(x, y))
                        maps[0][x][y] = shallowLakeGlyph;
                }
            }
            ObjectList<Region> change = new ObjectList<>();
            for (Region room : finder.rooms.keySet()) {
                if(flooded.intersects(tmp.remake(room).expand8way()))
                    change.add(room);
            }
            for (Region region : change) {
                finder.caves.put(region, finder.rooms.remove(region));
                finder.allRooms.andNot(region);
                finder.allCaves.or(region);
            }
        }
        return maps;
    }

    protected char[][] innerGenerate(char[][] map, ObjectIntMap<FillEffect> fx)
    {
        ObjectOrderedSet<Coord> hazards = new ObjectOrderedSet<>();
        int floorCount = DungeonTools.countCells(map, '.'),
                doorFill = 0,
                waterFill = 0,
                grassFill = 0,
                trapFill = 0,
                boulderFill = 0,
                islandSpacing = 0;

        if(fx.containsKey(FillEffect.GRASS)) {
            grassFill = fx.get(FillEffect.GRASS);
        }
        if(fx.containsKey(FillEffect.WATER)) {
            waterFill = fx.get(FillEffect.WATER);
        }
        if(fx.containsKey(FillEffect.BOULDERS)) {
            boulderFill = fx.get(FillEffect.BOULDERS) * floorCount / 100;
        }
        if(fx.containsKey(FillEffect.ISLANDS)) {
            islandSpacing = fx.get(FillEffect.ISLANDS);
        }
        if(fx.containsKey(FillEffect.TRAPS)) {
            trapFill = fx.get(FillEffect.TRAPS);
        }
        if (boulderFill > 0.0) {
            /*
            short[] floor = pack(map, '.');
            short[] viable = retract(floor, 1, width, height, true);
            ObjectList<Coord> boulders = randomPortion(viable, boulderFill, rng);
            for (Coord boulder : boulders) {
                map[boulder.x][boulder.y] = '#';
            }
            */
            Coord[] boulders = new Region(map, '.').retract8way(1).randomPortion(rng, boulderFill);
            Coord t;
            for (int i = 0; i < boulders.length; i++) {
                t = boulders[i];
                map[t.x][t.y] = '#';
            }
        }


        if(trapFill > 0) {
            for (int x = 1; x < map.length - 1; x++) {
                for (int y = 1; y < map[x].length - 1; y++) {
                    if (map[x][y] == '.') {
                        int ctr = 0;
                        if (map[x + 1][y] != '#') ++ctr;
                        if (map[x - 1][y] != '#') ++ctr;
                        if (map[x][y + 1] != '#') ++ctr;
                        if (map[x][y - 1] != '#') ++ctr;
                        if (map[x + 1][y + 1] != '#') ++ctr;
                        if (map[x - 1][y + 1] != '#') ++ctr;
                        if (map[x + 1][y - 1] != '#') ++ctr;
                        if (map[x - 1][y - 1] != '#') ++ctr;
                        if (ctr >= 5) hazards.add(Coord.get(x, y));
                    }
                }
            }
        }
        Region floors = new Region(map, '.'), working = new Region(width, height);
        floorCount = floors.size();
        float waterRate = waterFill / 100.0f, grassRate = grassFill / 100.0f;
        if(waterRate + grassRate > 1.0f)
        {
            waterRate /= (waterFill + grassFill) / 100.0f;
            grassRate /= (waterFill + grassFill) / 100.0f;
        }
        int targetWater = Math.round(floorCount * waterRate),
                targetGrass = Math.round(floorCount * grassRate),
                sizeWaterPools = targetWater / rng.nextInt(3, 6),
                sizeGrassPools = targetGrass / rng.nextInt(2, 5);

        Coord[] scatter;
        int remainingWater = targetWater, remainingGrass = targetGrass;
        if(targetWater > 0) {
            scatter = floors.separatedBlue(1f / 7f);
            ArrayTools.shuffle(scatter, rng);
            Region allWater = new Region(width, height);
            for (int i = 0; i < scatter.length; i++) {
                if (remainingWater > 5)
                {
                    if(!floors.contains(scatter[i]))
                        continue;
                    working.empty().insert(scatter[i]).spill(floors, rng.nextInt(4, Math.min(remainingWater, sizeWaterPools)), rng);

                    floors.andNot(working);
                    remainingWater -= working.size();
                    allWater.addAll(working);
                } else
                    break;
            }

            for (Coord pt : allWater) {
                hazards.remove(pt);
                //obstacles.add(pt);
                if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>')
                    map[pt.x][pt.y] = '~';
            }
            for (Coord pt : allWater) {
                if (map[pt.x][pt.y] != '<' && map[pt.x][pt.y] != '>' &&
                        (map[pt.x - 1][pt.y] == '.' || map[pt.x + 1][pt.y] == '.' ||
                                map[pt.x][pt.y - 1] == '.' || map[pt.x][pt.y + 1] == '.'))
                    map[pt.x][pt.y] = ',';
            }
        }

        if(targetGrass > 0) {
            scatter = floors.separatedBlue(1.03f/6.7f);
            ArrayTools.shuffle(scatter, rng);
            for (int i = 0; i < scatter.length; i++) {
                if (remainingGrass > 5) //remainingGrass >= targetGrass * 0.02 &&
                {
                    working.empty().insert(scatter[i]).spill(floors, rng.nextInt(4, Math.min(remainingGrass, sizeGrassPools)), rng);
                    if (working.isEmpty())
                        continue;
                    floors.andNot(working);
                    remainingGrass -= working.size();
                    map = working.intoChars(map, '"');
                } else
                    break;
            }
        }

        if(islandSpacing > 1 && targetWater > 0) {
            CoordOrderedSet islands = PoissonDisk.sampleMap(map, islandSpacing, rng, '#', '.', '"', '+', '/', '^', '<', '>');
            for (Coord c : islands) {
                map[c.x][c.y] = '.';
                if (map[c.x - 1][c.y] != '#' && map[c.x - 1][c.y] != '<' && map[c.x - 1][c.y] != '>')
                    map[c.x - 1][c.y] = ',';
                if (map[c.x + 1][c.y] != '#' && map[c.x + 1][c.y] != '<' && map[c.x + 1][c.y] != '>')
                    map[c.x + 1][c.y] = ',';
                if (map[c.x][c.y - 1] != '#' && map[c.x][c.y - 1] != '<' && map[c.x][c.y - 1] != '>')
                    map[c.x][c.y - 1] = ',';
                if (map[c.x][c.y + 1] != '#' && map[c.x][c.y + 1] != '<' && map[c.x][c.y + 1] != '>')
                    map[c.x][c.y + 1] = ',';
            }
        }

        if(trapFill > 0)
        {
            int total = hazards.size() * trapFill / 100;

            for(int i = 0; i < total; i++)
            {
                Coord entry = hazards.random(rng);
                if(map[entry.x][entry.y] == '<' || map[entry.x][entry.y] == '>')
                    continue;
                map[entry.x][entry.y] = '^';
                hazards.remove(entry);
            }
        }

        return map;

    }

    /**
     * Gets the seed EnhancedRandom that can be used to rebuild an identical dungeon to the latest one generated (or the
     * seed that will be used to generate the first dungeon if none has been made yet). You can pass the EnhancedRandom
     * this returns to {@link EnhancedRandom#setWith(EnhancedRandom)} to fill an existing EnhancedRandom of the same
     * type with this state. Assuming all other calls to generate a dungeon are identical, this will ensure generate()
     * or generateRespectingStairs() will produce the same dungeon output as the dungeon originally generated with the
     * seed this returned.
     * <br>
     * You can also just copy {@link #rng} yourself immediately before generating a dungeon, but this method
     * handles some complexities of when the state is actually used to generate a dungeon; since EnhancedRandom objects
     * can be shared between different classes that use random numbers, the state could change between when you copy the
     * state and when this class generates a dungeon. Using the rebuild seed RNG eliminates that confusion.
     * @return a seed as an EnhancedRandom that can be assigned to {@link #rng} to recreate a dungeon
     */
    public EnhancedRandom getRebuildRNG() {
        return rebuildRNG;
    }

    /**
     * Given a StringBuilder, this appends to it a readable representation of the map this stores.
     * The first line appended is the one with the highest y-coordinates, matching y-axis-up behavior.
     * The last line appended is the one where y is 0. Lines are separated by newline chars.
     * @param sb a StringBuilder this will append to
     * @return sb, after modifications, for chaining
     */
    public StringBuilder appendTo(StringBuilder sb) {
        if(dungeon != null) {
            for (int y = height - 1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    sb.append(dungeon[x][y]);
                }
                sb.append('\n');
            }
        }
        return sb;
    }

    /**
     * Provides a string representation of the latest generated dungeon.
     *
     * @return a printable string version of the latest generated dungeon.
     */
    @Override
	public String toString() {
        return appendTo(new StringBuilder((width + 1) * height)).toString();
    }

}
