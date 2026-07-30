/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.*;

import java.util.*;

/**
 * A static class that can be used to modify the char[][] dungeons that other generators produce.
 * Includes constants to describe environment types encountered in possible dungeons, which are used elsewhere.
 * Here, 2D char arrays are always indexed with x, then y.
 * Has methods to open and close doors represented by {@code '+'} and {@code '/'}. Can double the width of a 2D char
 * array while respecting both ASCII and Unicode box drawing chars for walls, or undo that operation. Can simplify 2D
 * char arrays to use {@code '#'} for any wall and {@code '.'} for any floor. Can add random paths to existing maps to
 * guarantee they can be traversed from one point to another. Can wrap a 2D char array with {@code '#'} chars for walls.
 * <br>
 * The earlier DungeonUtility class in SquidLib had various methods that have since been moved to {@link LineTools},
 * such as the often-used {@link LineTools#hashesToLines(char[][])}. This class still has {@link #debugPrint(char[][])},
 * but new code may want to prefer {@link StringTools#printChar2D(char[][])}, which calls the same code.
 */
public final class DungeonTools {

    /**
     * Constant for environment tiles that are not near a cave, room, or corridor. Value is 0.
     * Used by several classes that distinguish types of dungeon environment.
     * <br>
     * This isn't really a bit flag, but if used as one in conjunction with {@link #ANY_FLOOR}, it is treated
     * as a wall.
     */
    public static final int UNTOUCHED = 0;

    /**
     * Bit flag used by other constants to indicate they are floors, or if absent, that they are walls.
     * All floors are considered possible to pass through for pathfinding purposes, and all walls are
     * considered impassable. This constant should not be used on its own in an environment 2D array.
     * You can check if an environment value is any type of floor with {@code (env & ANY_FLOOR) == ANY_FLOOR}, or
     * is any type of wall with {@code (env & ANY_FLOOR) != ANY_FLOOR}.
     */
    public static final int ANY_FLOOR = 1;

    /**
     * Constant for environment tiles that are walls near a room. Value is 2.
     * Used by several classes that distinguish types of dungeon environment.
     * <br>
     * This is a bit flag.
     * It can be used with {@code env == ROOM_WALL} to check if an environment int value is
     * a wall in a room (and nothing else).
     * Use {@code (env & ROOM_WALL) == ROOM_WALL} to check if an environment
     * int value is any kind of room value (room wall or room floor, even if other flags are set).
     * Use {@code (env & ROOM_FLOOR) == ROOM_WALL} to check if env represents a wall in a room, potentially with
     * other flags.
     */
    public static final int ROOM_WALL = (1 << 1);
    /**
     * Constant for environment tiles that are floors for a room. Value is 3.
     * Used by several classes that distinguish types of dungeon environment.
     * <br>
     * This is a bit flag.
     * It can be treated as {@code ROOM_WALL | ANY_FLOOR}.
     * You can compare an environment int value to this with {@code env == ROOM_FLOOR} to check if it represents a
     * floor in a room (and nothing else).
     * Use {@code (env & ROOM_FLOOR) == ROOM_FLOOR} to check if env represents a floor in a room, potentially with
     * other flags.
     * Use {@code (env & ROOM_FLOOR) == ROOM_WALL} to check if env represents a wall in a room, potentially with
     * other flags.
     */
    public static final int ROOM_FLOOR = ROOM_WALL | ANY_FLOOR;
    /**
     * Constant for environment tiles that are walls near a cave or other natural part of a map.
     * Value is 4. Used by several classes that distinguish types of dungeon environment. May be
     * used by {@link WildernessGenerator} for ledges and other natural obstacles.
     * <br>
     * This is a bit flag.
     * It can be used with {@code env == NATURAL_WALL} to check if an environment int value is
     * a wall in a natural area (and nothing else).
     * Use {@code (env & NATURAL_WALL) == NATURAL_WALL} to check if an environment
     * int value is any kind of natural area (wall or floor, even if other flags are set).
     * Use {@code (env & NATURAL_FLOOR) == NATURAL_WALL} to check if env represents a wall in a natural area,
     * potentially with other flags.
     */
    public static final int NATURAL_WALL = (1 << 2);
    /**
     * Constant for environment tiles that are floors for a cave or other natural part of a map.
     * Value is 5. Used by several classes that distinguish types of dungeon environment. Also
     * used by {@link WildernessGenerator} for almost everything it generates.
     * <br>
     * This is a bit flag.
     * It can be treated as {@code NATURAL_WALL | ANY_FLOOR}.
     * You can compare an environment int value to this with {@code env == NATURAL_FLOOR} to check if it represents a
     * floor in a natural area (and nothing else).
     * Use {@code (env & NATURAL_FLOOR) == NATURAL_FLOOR} to check if env represents a floor in a natural area,
     * potentially with other flags.
     * Use {@code (env & NATURAL_FLOOR) == NATURAL_WALL} to check if env represents a wall in a natural area,
     * potentially with other flags.
     */
    public static final int NATURAL_FLOOR = NATURAL_WALL | ANY_FLOOR;
    /**
     * Constant for environment tiles that are walls near a corridor. Value is 8.
     * Used by several classes that distinguish types of dungeon environment.
     * <br>
     * This is a bit flag.
     * It can be treated as {@code CORRIDOR_WALL | ANY_FLOOR}.
     * You can compare an environment int value to this with {@code env == CORRIDOR_FLOOR} to check if it represents a
     * floor in a corridor (and nothing else).
     * Use {@code (env & CORRIDOR_FLOOR) == CORRIDOR_FLOOR} to check if env represents a floor in a corridor,
     * potentially with other flags.
     * Use {@code (env & CORRIDOR_FLOOR) == CORRIDOR_WALL} to check if env represents a wall in a corridor,
     * potentially with other flags.
     */
    public static final int CORRIDOR_WALL = (1 << 3);
    /**
     * Constant for environment tiles that are floors for a corridor. Value is 9.
     * Used by several classes that distinguish types of dungeon environment.
     * <br>
     * This is a bit flag.
     * It can be treated as {@code CORRIDOR_WALL | ANY_FLOOR}.
     * You can compare an environment int value to this with {@code env == CORRIDOR_FLOOR} to check if it represents a
     * floor in a corridor (and nothing else).
     * Use {@code (env & CORRIDOR_FLOOR) == CORRIDOR_FLOOR} to check if env represents a floor in a corridor,
     * potentially with other flags.
     * Use {@code (env & CORRIDOR_FLOOR) == CORRIDOR_WALL} to check if env represents a wall in a corridor,
     * potentially with other flags.
     */
    public static final int CORRIDOR_FLOOR = CORRIDOR_WALL | ANY_FLOOR;
    /**
     * A bit flag mask that can be used to isolate the bits that indicate whether an environment int value is a room,
     * natural area, or corridor, regardless of floor/wall status. Use {@code (env & AREA_MASK) == wallConstant},
     * where {@code wallConstant} is your choice of {@link #ROOM_WALL}, {@link #NATURAL_WALL}, or
     * {@link #CORRIDOR_WALL}, to identify if that int is your selected area.
     */
    public static final int AREA_MASK = ROOM_WALL | NATURAL_WALL | CORRIDOR_WALL;
    /**
     * A bit flag mask that can be used to isolate only the bits used in constants defined by DungeonTools.
     * Use {@code (env & CORE_ENVIRONMENT_MASK) == someConstant}, where{@code someConstant} is any constant from
     * DungeonTools, to tell which one it is even if there are other flags present. Note that this will identify bit
     * flags with no bits from DungeonTools constants as {@link #UNTOUCHED}.
     */
    public static final int CORE_ENVIRONMENT_MASK = ROOM_WALL | NATURAL_WALL | CORRIDOR_WALL | ANY_FLOOR;

    /**
     * A lock for the optional lock-and-key system that indicates an area can only be accessed by creatures who can open
     * doors with a free hand. This should block animals from entering areas that require opening a door to access. It
     * may also temporarily block people who are frozen numb, for instance, or creatures like vampires who aren't
     * allowed to open a door themselves in some versions.
     * <br>
     * This is lock 0.
     */
    public static final int LOCK_NEEDS_HAND = 1 << 4;

    /**
     * A lock for the optional lock-and-key system that indicates an area can only be accessed by creatures who can swim
     * or otherwise move through deep water.
     * <br>
     * This is lock 1.
     */
    public static final int LOCK_DEEP_WATER = 1 << 5;

    /**
     * A lock for the optional lock-and-key system that indicates an area can only be accessed by creatures who can move
     * without risking death through intense heat and/or fire, such as a path blocked by lava.
     * <br>
     * This is lock 2.
     */
    public static final int LOCK_FIRE = 1 << 6;

    /**
     * A lock for the optional lock-and-key system that indicates an area can only be accessed by creatures who can jump
     * or fly some higher-than-normal amount vertically, such as to get up and out of a pit.
     * <br>
     * This is lock 3.
     */
    public static final int LOCK_HIGH_JUMP = 1 << 7;

    /**
     * A lock for the optional lock-and-key system that indicates an area can only be accessed by creatures who can jump
     * or fly some longer-than-normal amount horizontally, such as to get over a low hazard.
     * <br>
     * This is lock 4.
     */
    public static final int LOCK_LONG_JUMP = 1 << 8;

    private DungeonTools() {
    }

    /**
     * Checks an environment int and returns true if it represents any type of floor (passable cell).
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return true if the int represents any passable terrain cell
     */
    public boolean isFloor(int environment) {
        return (environment & ANY_FLOOR) != 0;
    }

    /**
     * Checks an environment int and returns true if it represents any type of wall (impassable cell).
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return true if the int represents any impassable terrain cell
     */
    public boolean isWall(int environment) {
        return (environment & ANY_FLOOR) == 0;
    }

    /**
     * Checks an environment int and returns true if it represents any type of room cell (floor or wall).
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return true if the int represents any room cell
     */
    public boolean isRoom(int environment) {
        return (environment & ROOM_WALL) != 0;
    }

    /**
     * Checks an environment int and returns true if it represents any type of corridor cell (floor or wall).
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return true if the int represents any corridor cell
     */
    public boolean isCorridor(int environment) {
        return (environment & CORRIDOR_WALL) != 0;
    }

    /**
     * Checks an environment int and returns true if it represents any type of natural cell (floor or wall).
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return true if the int represents any natural cell (such as cave walls or floors)
     */
    public boolean isNatural(int environment) {
        return (environment & NATURAL_WALL) != 0;
    }

    /**
     * For the optional lock-and-key puzzle system, checks if an environment cell has access prohibited by a given lock.
     * There are 20 locks, traditionally ranging from 0 to 19.
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @param lock between 0 and 19, both inclusive
     * @return true if the environment cell has locked access by the given lock number
     */
    public boolean checkLock(int environment, int lock) {
        return (environment >>> 4 + lock & 1) == 1;
    }

    /**
     * Gets the abstract "level" concept that can mean anything and is associated with a given environment cell.
     * The "level" starts at 0 and can go up to 255, inclusive. It can be used to mean the approximate power of enemies
     * in an area, as one meaning, or a linear score of some kind needed or preferred to enter the area. It is separate
     * from the optional lock-and-key system, which allows any locks to be passed independently of any others.
     * @param environment an environment int, typically taken from {@link PlaceGenerator#getEnvironment()}
     * @return an int between 0 and 255, drawn from the given environment cell
     */
    public int getLevel(int environment) {
        return environment >>> 24;
    }

    /**
     * When a map is generated by DungeonProcessor with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '+', which is useful if you
     * want '/' to be used for a different purpose and/or to distinguish open and closed doors.
     *
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '+' for all doors
     */
    public static char[][] closeDoors(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '/') portion[i][j] = '+';
                else portion[i][j] = map[i][j];

            }
        }
        return portion;
    }

    /**
     * When a map is generated by DungeonProcessor with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '+', which is useful if you
     * want '/' to be used for a different purpose and/or to distinguish open and closed doors. This also takes and
     * modifies an int 2D array, which is often generated by {@link PlaceGenerator#getEnvironment()}, and sets the
     * optional lock value on all doors to enable {@link #LOCK_NEEDS_HAND}. That lock is used to mean an area that can
     * only be opened by someone with an available and working hand, but not an animal or mindless creature. This does
     * not set the lock value on areas past closed doors, since that would need to know an entry point.
     *
     * @param map a char[][] that may have both '+' and '/' for doors, which will be modified in-place
     * @param environment an environment int[][], which will be modified in-place; must be at least as large as map
     * @return map, after changes in-place
     */
    public static char[][] closeDoorsInPlace(char[][] map, int[][] environment) {
        int width = map.length;
        int height = map[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '+') {
                    environment[i][j] |= LOCK_NEEDS_HAND;
                }
                else if (map[i][j] == '/') {
                    map[i][j] = '+';
                    environment[i][j] |= LOCK_NEEDS_HAND;
                }

            }
        }
        return map;
    }

    /**
     * When a map is generated by DungeonProcessor with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '/', which is useful if you
     * want '+' to be used for a different purpose and/or to distinguish open and closed doors.
     *
     * @param map a char[][] that may have both '+' and '/' for doors
     * @return a char[][] that only uses '/' for all doors
     */
    public static char[][] openDoors(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '+') portion[i][j] = '/';
                else portion[i][j] = map[i][j];
            }
        }
        return portion;
    }

    /**
     * When a map is generated by DungeonProcessor with addDoors enabled, different chars are used for vertical and
     * horizontal doors ('+' for vertical and '/' for horizontal).  This makes all doors '/', which is useful if you
     * want '+' to be used for a different purpose and/or to distinguish open and closed doors. This also takes and
     * modifies an int 2D array, which is often generated by {@link PlaceGenerator#getEnvironment()}, and sets the
     * optional lock value on all doors to disable {@link #LOCK_NEEDS_HAND}. That lock is used to mean an area that can
     * only be opened by someone with an available and working hand, but not an animal or mindless creature. This does
     * not change the lock value on areas past open doors, since that would need to know an entry point.
     *
     * @param map a char[][] that may have both '+' and '/' for doors, which will be modified in-place
     * @param environment an environment int[][], which will be modified in-place; must be at least as large as map
     * @return map, after changes in-place
     */
    public static char[][] openDoorsInPlace(char[][] map, int[][] environment) {

        int width = map.length;
        int height = map[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (map[i][j] == '+') {
                    map[i][j] = '/';
                    environment[i][j] &= ~LOCK_NEEDS_HAND;
                }
            }
        }
        return map;
    }


    /**
     * Takes a char[][] dungeon map and returns a copy with all box drawing chars, special placeholder chars, or '#'
     * chars changed to '#' and everything else changed to '.' .
     *
     * @param map a char[][] with different characters that can be simplified to "wall" or "floor"
     * @return a copy of map with all box-drawing, placeholder, wall or space characters as '#' and everything else '.'
     */
    public static char[][] simplifyDungeon(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        char[][] portion = new char[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case ' ':
                    case '#':
                        portion[i][j] = '#';
                        break;
                    default:
                        portion[i][j] = '.';
                }
            }
        }
        return portion;
    }

    /**
     * Takes a dungeon map with either '#' as the only wall character or the Unicode box drawing characters used by
     * {@link LineTools#hashesToLines(char[][])}.
     * Returns a new char[][] dungeon map with two characters per cell, mostly filling the spaces
     * next to non-walls with space characters. Only does anything different if a box-drawing character would
     * continue into an adjacent cell, or if a '#' wall needs another '#' wall next to it. The recommended approach is
     * to keep both the original non-double-width map and the newly-returned double-width map, since the single-width
     * maps can be used more easily for pathfinding. If you need to undo this function, call unDoubleWidth().
     *
     * @param map a char[][] that uses either '#' or box-drawing characters for walls, but one per cell
     * @return a widened copy of map that uses two characters for every cell, connecting box-drawing chars correctly
     */
    public static char[][] doubleWidth(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        char[][] paired = new char[width * 2][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0, px = 0; x < width; x++, px += 2) {
                paired[px][y] = map[x][y];
                switch (paired[px][y]) {
                    //                        case '┼ ├ ┤ ┴ ┬ ┌ ┐ └ ┘ │ ─'
                    case '┼':
                    case '├':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '└':
                    case '─':
                        paired[px + 1][y] = '─';
                        break;
                    case '#':
                        paired[px + 1][y] = '#';
                        break;

                    default:
                        paired[px + 1][y] = ' ';
                        break;
                        /*
                    case '.':
                    case '┤':
                    case '┐':
                    case '┘':
                    case '│':
                         */
                }
            }
        }
        return paired;
    }

    /**
     * Takes a dungeon map that uses two characters per cell, and condenses it to use only the left (lower index)
     * character in each cell. This should (probably) only be called on the result of doubleWidth(), and will throw an
     * exception if called on a map with an odd number of characters for width, such as "#...#" .
     *
     * @param map a char[][] that has been widened by doubleWidth()
     * @return a copy of map that uses only one char per cell
     */
    public static char[][] unDoubleWidth(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        if ((width & 1) != 0)
            throw new IllegalArgumentException("Argument must be a char[width][height] with an even width.");
        char[][] unpaired = new char[width >>> 1][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0, px = 0; px < width; x++, px += 2) {
                unpaired[x][y] = map[px][y];
            }
        }
        return unpaired;
    }

    /**
     * @param level dungeon/map level as 2D char array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(char[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level dungeon/map level as 2D char array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(char[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }

    /**
     * @param level dungeon/map level as 2D float array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(float[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level dungeon/map level as 2D float array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static boolean inLevel(float[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }

    /**
     * @param level a dungeon/map level as 2D array. x,y indexed
     * @param c     Coord to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static <T> boolean inLevel(T[][] level, Coord c) {
        return inLevel(level, c.x, c.y);
    }

    /**
     * @param level a dungeon/map level as 2D array. x,y indexed
     * @param x     x coordinate to check
     * @param y     y coordinate to check
     * @return {@code true} if {@code c} is valid in {@code level}, {@code false} otherwise.
     */
    public static <T> boolean inLevel(T[][] level, int x, int y) {
        return 0 <= x && x < level.length && 0 <= y && y < level[x].length;
    }

    /**
     * Quickly counts the number of char elements in level that are equal to match.
     *
     * @param level the 2D char array to count cells in
     * @param match the char to search for
     * @return the number of cells that matched
     */
    public static int countCells(char[][] level, char match) {
        if (level == null || level.length == 0)
            return 0;
        int counter = 0;
        for (int x = 0; x < level.length; x++) {
            for (int y = 0; y < level[x].length; y++) {
                if (level[x][y] == match) counter++;
            }
        }
        return counter;
    }

    /**
     * Prints a 2D char array without padding. Prints on multiple lines, with a trailing newline.
     * To match how libGDX usually displays on the screen, this prints with the y-axis pointing up, that is,
     * row 0 is at the bottom and the highest y-value is at the top.
     * <br>
     * This delegates to {@link StringTools#printChar2D(char[][])}.
     *
     * @param level a 2D char array to print with a trailing newline
     */
    public static void debugPrint(char[][] level) {
        StringTools.printChar2D(level);
    }

    /**
     * Changes the outer edge of a char[][] to the wall char, '#'.
     *
     * @param map A char[][] that stores map data; will be modified in place
     * @return the modified-in-place map with its edge replaced with '#'
     */
    public static char[][] wallWrap(char[][] map) {
        int upperY = map[0].length - 1;
        int upperX = map.length - 1;
        for (int i = 0; i < map.length; i++) {
            map[i][0] = '#';
            map[i][upperY] = '#';
        }
        for (int i = 0; i < map[0].length; i++) {
            map[0][i] = '#';
            map[upperX][i] = '#';
        }
        return map;
    }
    public static ObjectList<Coord> pointPath(int width, int height, EnhancedRandom rng) {
        if (width <= 2 || height <= 2)
            throw new IllegalArgumentException("width and height must be greater than 2");
        HilbertCurve.init2D();
        long columnAlterations = (rng.nextLong() & 0xFFFFFFFFFFFFL);
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = (rng.nextLong() & 0xFFFFFFFFFFFFL);
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        int[] columns = new int[16], rows = new int[16];
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
            cs3 = cs3 * (i - 8) >> 3;
            rs3 = rs3 * (i - 8) >> 3;
            columns[i] += cs3;
            rows[i] += rs3;
        }

        ObjectList<Coord> points = new ObjectList<>(80);
        int m = rng.next(6);
        Coord temp = HilbertCurve.mooreToCoord(m), next;
        temp = Coord.get(columns[temp.x], rows[temp.y]);
        for (int i = 0, r; i < 256; r = rng.nextInt(4, 12), i += r, m += r) {
            next = HilbertCurve.mooreToCoord(m);
            next = Coord.get(columns[next.x], rows[next.y]);
            points.addAll(OrthoLine.line(temp, next));
            temp = next;
        }
        points.add(points.get(0));
        return points;
    }

    /**
     * Ensures a path exists in a rough ring around the map by first creating the path (using
     * {@link #pointPath(int, int, EnhancedRandom)} with the given EnhancedRandom), then finding chars in blocking that are on
     * that path and replacing them with replacement. Modifies map in-place and returns an ObjectList of Coord points
     * that will always be on the path.
     *
     * @param map         a 2D char array, x then y, etc. that will be modified directly; this is the "returned map"
     * @param rng         used for random factors in the path choice
     * @param replacement the char that will fill be used where a path needs to be carved out; usually '.'
     * @param blocking    an array or vararg of char that are considered blocking for the path and will be replaced if
     *                    they are in the way
     * @return the ObjectList of Coord points that are on the carved path, including existing non-blocking cells; will be empty if any parameters are invalid
     */
    public static ObjectList<Coord> ensurePath(char[][] map, EnhancedRandom rng, char replacement, char... blocking) {
        if (map == null || map.length == 0 || blocking == null || blocking.length == 0)
            return new ObjectList<>(0);
        int width = map.length, height = map[0].length;
        ObjectList<Coord> points = pointPath(width, height, rng);
        char[] blocks = new char[blocking.length];
        System.arraycopy(blocking, 0, blocks, 0, blocking.length);
        Arrays.sort(blocks);
        for (Coord c : points) {
            if (c.x >= 0 && c.x < width && c.y >= 0 && c.y < height && Arrays.binarySearch(blocks, map[c.x][c.y]) >= 0) {
                map[c.x][c.y] = replacement;
            }
        }
        return points;
    }

    public static ObjectList<Coord> allMatching(char[][] map, char... matching) {
        if (map == null || map.length == 0 || matching == null || matching.length == 0)
            return new ObjectList<>(0);
        return new ObjectList<>(new Region(map, matching));
    }

    /**
     * Gets a List of Coord that are within radius distance of (x,y), and appends them to buf if it is non-null or makes
     * a fresh List to append to otherwise. Returns buf if non-null, else the fresh List of Coord. May produce Coord
     * values that are not within the boundaries of a map, such as (-5,-4), if the center is too close to the edge or
     * radius is too high. You can use {@link Radius#inCircle(int, int, int, boolean, int, int, List)}
     * with surpassEdges as false if you want to limit Coords to within the map, or the more general
     * {@link Radius#pointsInside(int, int, int, boolean, int, int, List)} on a Radius.SQUARE or
     * Radius.DIAMOND enum value if you want a square or diamond shape.
     *
     * @param x      center x of the circle
     * @param y      center y of the circle
     * @param radius inclusive radius to extend from the center; radius 0 gives just the center
     * @param buf    Where to add the coordinates, or null for this method to
     *               allocate a fresh list.
     * @return The coordinates of a circle centered {@code (x, y)}, whose
     * diameter is {@code (radius * 2) + 1}.
     * @see Radius#inCircle(int, int, int, boolean, int, int, List) if you want to keep the Coords within the bounds of the map
     */
    public static List<Coord> circle(int x, int y, int radius, /* */ List<Coord> buf) {
        final List<Coord> result = buf == null ? new ObjectList<>() : buf;
        radius = Math.max(0, radius);
        for (int dx = -radius; dx <= radius; ++dx) {
            final int high = (int) Math.floor(Math.sqrt(radius * radius - dx * dx));
            for (int dy = -high; dy <= high; ++dy) {
                result.add(Coord.get(x + dx, y + dy));
            }
        }
        return result;
    }
}
