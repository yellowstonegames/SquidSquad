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

import com.github.tommyettinger.ds.IntIntOrderedMap;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Region;

/**
 * A room placing algorithm developed by Rayvolution for his game Fail To Hero, this was simple to implement but
 * delivers complex connectivity. It is meant to ensure all rooms are connected, but usually not directly, and many
 * routes need to wind throughout the map to get to a goal.
 * <br>
 * With {@link #roomWidth} and {@link #roomHeight} set to 3, and {@link #wallThickness} set to 2:
 * <pre>{@code
 * ┌────────────────────────────┐┌────────┐┌────────┐┌────────┐
 * │............................││........││........││........│
 * │............................││........││........││........│
 * │............................││........││........││........│
 * │...┌──────────┐...┌─────┐...││...┌┐...│└────┐...││...┌────┘
 * │...│┌───┐┌────┘...│┌────┘...└┘...││...└────┐│...││...└────┐
 * │...││...││........││.............││........││...││........│
 * │...││...││........││.............││........││...││........│
 * │...││...││........││.......<.....││........││...││........│
 * └───┘│...││...┌────┘│...┌─────────┘└────────┘│...│└────┐...│
 * ┌────┘...││...└────┐│...│┌───────────────────┘...└─────┘...│
 * │........││........││...││.................................│
 * │.......>││........││...││.................................│
 * │........││........││...││.................................│
 * │...┌────┘└────┐...│└───┘│...┌─────────────────────────────┘
 * │...│┌────────┐│...└─────┘...└────┐┌───┐┌────────┐┌────────┐
 * │...││........││..................││...││........││........│
 * │...││........││..................││...││........││........│
 * │...││........││..................││...││........││........│
 * │...││...┌┐...│└────┐...┌─────────┘│...│└────┐...│└────┐...│
 * │...││...││...└─────┘...│┌────────┐│...└────┐│...└─────┘...│
 * │...││...││.............││........││........││.............│
 * │...││...││.............││........││........││.............│
 * │...││...││.............││........││........││.............│
 * │...││...││...┌─────────┘│...┌┐...││...┌────┘│...┌─────┐...│
 * │...└┘...││...└──────────┘...││...└┘...└─────┘...│┌────┘...│
 * │........││..................││..................││........│
 * │........││..................││..................││........│
 * │........││..................││..................││........│
 * └────────┘└──────────────────┘└──────────────────┘└────────┘
 * }</pre>
 * <br>
 * With {@link #roomWidth}, {@link #roomHeight}, and {@link #wallThickness} all set to 1:
 * <pre>{@code
 * ┌───────────────┬─┬───────────┬─────┬───┬─────────┬─┬───┬─┐
 * │...............│.│...........│.....│...│.........│.│...│.│
 * │.┌──────────.┌─┘.│.┌──.────┬─┤.┌───┤.│.│.──┐.──┐.│.│.│.│.│
 * │.│...........│.....│.......│.│.│...│.│.....│...│.│...│...│
 * ├─┘.┌────.┌─┐.└─────┘.┌──.│.│.│.│.┌─┘.│.│.──┼───┤.└─┬─┘.│.│
 * │...│.....│.│.........│...│.│...│.│...│.│...│...│...│...│.│
 * │.┌─┴───┬─┘.│.┌──.┌───┤.──┤.│.┌─┤.│.┌─┴─┼─┐.│.│.└───┤.│.└─┤
 * │.│.....│...│.│...│...│...│.│.│.│...│...│.│...│.....│.│...│
 * ├─┤.│.│.│.──┘.│.│.└─┐.├───┤.│.│.│.──┤.│.│.│.──┤.│.│.│.└─┐.│
 * │.│.│.│.......│.│...│.│...│.│.......│.│...│...│.│.│.│...│.│
 * │.│.└─┼────.┌─┘.└───┘.│.│.└─┴─┬─┬──.├─┤.──┴───┼─┤.├─┴─┐.└─┤
 * │.│...│>....│.........│.│.....│.│...│.│.......│.│.│...│...│
 * │.└─┐.│.┌───┴────.│.│.│.└─┬───┘.│.┌─┘.├───┐.┌─┘.├─┘.│.│.│<│
 * │...│.│.│.........│.│.....│.......│...│...│.│...│...│...│.│
 * ├──.├─┼─┴──.│.│.┌─┘.├───┐.└──.──┬─┘.┌─┘.│.│.│.┌─┘.──┴───┤.│
 * │...│.│.....│.│.│...│...│.......│...│...│...│.│.........│.│
 * ├─┐.│.│.──┬─┘.├─┘.┌─┤.──┼───┐.│.│.│.└──.└───┤.│.│.┌─┐.│.│.│
 * │.│...│...│...│...│.│...│...│.│.│.│.........│.│.│.│.│.│.│.│
 * │.│.│.└─┬─┴─┬─┴─┬─┤.├──.│.──┘.│.│.├──────.│.│.└─┤.│.│.└─┤.│
 * │...│...│...│...│.│.│.........│...│.......│.....│.│.│...│.│
 * │.┌─┤.│.│.│.│.│.│.│.│.──┐.──┐.├──.└───────┴─────┘.│.├──.├─┤
 * │.│.│.│.│.│.│.│...│.│...│...│.│...................│.│...│.│
 * │.│.├─┘.│.│.│.├──.│.└───┴─┐.│.└───────┐.──┐.──┬─┐.│.│.──┘.│
 * │.│.│.....│.│.│...│.......│.│.........│...│...│.│...│.....│
 * ├─┘.│.┌──.│.└─┘.┌─┴────.│.│.├───────┐.└─┐.├──.│.└─┬─┘.┌──.│
 * │.....│...│.....│.......│...│.......│...│.│.......│...│...│
 * │.────┴─┐.├────.│.│.────┤.──┘.┌────.├───┘.│.┌────.├──.│.──┤
 * │.......│.│.......│.....│.....│.....│.....│.│.....│...│...│
 * └───────┴─┴───────┴─────┴─────┴─────┴─────┴─┴─────┴───┴───┘
 * }</pre>
 * <br>
 * With {@link #divideRooms} set to 0.65:
 * <pre>{@code
 * ┌─────┬───────────┬───────────┬─────┬─────┬─────┬─────┬─────┐
 * │.....+...........│...........│.....│.....│.....│.....+.....│
 * │.....│...........│...........│.....+.....│.....│.....│.....│
 * │.....│...........│...........+.....│.....│.....│.....│.....│
 * │.....│...........│...........│.....│.....│.....│.....│.....│
 * │.....│.......................│.....│.....│.....│.....│.....│
 * │.....│.....┌─────┬───+─┬─────┼─────┼─────┼──+──┼────+┼──+──┤
 * │.....│.....│.....│.....│.....│.....│.....│.....│.....│.....│
 * │.....│.....│.....│.....│.....│.....│.....│.....│.....│.....│
 * │.....│.....│.....│.....│.....│.....+.....│.....│.....│.....│
 * │.....│.....│.....│.....│.....+.....│.....│.....│.....│.....│
 * │.....│.....│.....│.....+.....│.....│.....+.....│.....│.....│
 * ├────+┼───+─┼────+┼─────┴─────┴─────┘.....├─────┼─────┤.....│
 * │.....│.....│.....│.......................│.....│....>│.....│
 * │.....│.....│.....│.....│.................│.....│.....│.....│
 * │.....│.....│..<..│.....│.................│.....│.....│.....│
 * │.....│.....│.....+.....│.................│.....│.....+.....│
 * │.....│.....│.....│.....│.................│.....│.....│.....│
 * │.────┼────.├─────┼─────┴───────────┬────.├────.├──+──┼──+──┤
 * │.....│.....│.....│.................│.....│.....│.....│.....│
 * │.....│.....│.....│.................│.....│.....│.....│.....│
 * │.....│.....│.....│.................│.....│.....│.....│.....│
 * │.....│.....│.....│.................│.....│.....+.....│.....│
 * │.....│.....+.....│.................│.....│.....│.....│.....│
 * ├───+─┼─────┼───+─┼────+──────┐.....└+────┼─────┼─────┼─.───┤
 * │.....│.....│.....│...........│...........│.....│.....+.....│
 * │.....│.....│.....│...........│...........│...........│.....│
 * │.....│.....│.....│...........│...........+.....│.....│.....│
 * │.....│.....│.....│...........│...........│.....│.....│.....│
 * │.....+.....│.....│...........│...........│.....│.....│.....│
 * └─────┴─────┴─────┴───────────┴───────────┴─────┴─────┴─────┘
 * }</pre>
 */
public class ConnectingMapGenerator implements PlaceGenerator {

    /**
     * The width of the place grid to generate.
     */
    public int width;
    /**
     * The height of the place grid to generate.
     */
    public int height;
    /**
     * The width of each room in cells; 1 is the minimum.
     */
    public int roomWidth;
    /**
     * The height of each room in cells; 1 is the minimum.
     */
    public int roomHeight;
    /**
     * How thick a wall between two rooms should be, in cells; 1 is the minimum, and this usually
     * shouldn't be much more than roomWidth or roomHeight.
     */
    public int wallThickness;
    /**
     * The place grid as a 2D char array. Returned by {@link #getPlaceGrid()}.
     */
    public char[][] dungeon;
    /**
     * The environment as a 2D int array, with each int a constant in {@link DungeonTools}.
     * Returned by {@link #getEnvironment()}.
     */
    public int[][] environment;
    /**
     * Reset on every call to {@link #generate()}. This can be reassigned, but it must not be null.
     */
    public Region region;
    /**
     * If greater than 0, this is the chance that two connected rooms should only have a 1-cell-wide
     * connection (which can be made into a door using {@link DungeonProcessor}).
     */
    public double divideRooms;
    private final transient Region tempRegion;
    /**
     * Can be swapped out for another EnhancedRandom implementation, but must not be null.
     */
    public EnhancedRandom rng;

    /**
     * This was set to 15 in the earliest version of the code, but it seems to be fine set to as low as 1.
     * The default value is now 1.
     */
    public int initialAttempts = 1;

    /**
     * This was set to 5 in the earliest version of the code, but it seems to be fine set to as low as 1.
     * The default value is now 1.
     */
    public int attempts = 1;

    /**
     * Calls {@link #ConnectingMapGenerator(int, int, int, int, EnhancedRandom, int)} with width 80, height 80, roomWidth 8,
     * roomHeight 8, a new {@link WhiskerRandom} for random, and wallThickness 2.
     */
    public ConnectingMapGenerator()
    {
        this(80, 80, 8, 8, new WhiskerRandom(), 2);
    }
    /**
     * Determines room width and room height by dividing width or height by 10; wallThickness is 2. 
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param random an IRNG to make random choices for connecting rooms
     */

    public ConnectingMapGenerator(int width, int height, EnhancedRandom random)
    {
        this(width, height, width / 10, height / 10, random, 2);
    }
    /**
     * Exactly like {@link #ConnectingMapGenerator(int, int, int, int, EnhancedRandom, int)} with wallThickness 2.
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param roomWidth target width of each room, in cells; only counts the center floor area of a room
     * @param roomHeight target height of each room, in cells; only counts the center floor area of a room
     * @param random an IRNG to make random choices for connecting rooms
     */
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, EnhancedRandom random)
    {
        this(width, height, roomWidth, roomHeight, random, 2);
    }

    /**
     * 
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param roomWidth target width of each room, in cells; only counts the center floor area of a room
     * @param roomHeight target height of each room, in cells; only counts the center floor area of a room
     * @param random an EnhancedRandom to make random choices for connecting rooms
     * @param wallThickness how thick a wall between two rooms should be, in cells; 1 is minimum, and this usually
     *                      shouldn't be much more than roomWidth or roomHeight
     */
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, EnhancedRandom random, int wallThickness) {
        this(width, height, roomWidth, roomHeight, random, wallThickness, 0.0);
    }
    /**
     * 
     * @param width total width of the map, in cells
     * @param height total height of the map, in cells
     * @param roomWidth target width of each room, in cells; only counts the center floor area of a room
     * @param roomHeight target height of each room, in cells; only counts the center floor area of a room
     * @param random an EnhancedRandom to make random choices for connecting rooms
     * @param wallThickness how thick a wall between two rooms should be, in cells; 1 is minimum, and this usually
     *                      shouldn't be much more than roomWidth or roomHeight
     * @param divideRooms if greater than 0, this is the chance that two connected rooms should only have a 1-cell-wide
     *                    connection (which can be made into a door using {@link DungeonProcessor})
     */
    public ConnectingMapGenerator(int width, int height, int roomWidth, int roomHeight, EnhancedRandom random, int wallThickness,
                                  double divideRooms)
    {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.region = new Region(this.width, this.height);
        tempRegion = new Region(this.width, this.height);
        this.roomWidth = Math.max(1, roomWidth);
        this.roomHeight = Math.max(1, roomHeight);
        this.wallThickness = Math.max(1, wallThickness);
        dungeon = ArrayTools.fill(' ', this.width, this.height);
        environment = new int[this.width][this.height];
        this.divideRooms = divideRooms;
        rng = random;
    }
    /**
     * Generates a dungeon or other map as a 2D char array. Uses the convention of '#' representing a wall and '.'
     * representing a bare floor, and also fills {@link #environment} with appropriate constants from DungeonTools,
     * like {@link DungeonTools#ROOM_FLOOR} and {@link DungeonTools#ROOM_WALL}.
     * 
     * @return a 2D char array representing a room-based map, using standard conventions for walls/floors
     */
    @Override
    public char[][] generate() {
        int gridWidth = (width + wallThickness - 2) / (roomWidth + wallThickness), gridHeight = (height + wallThickness - 2) / (roomHeight + wallThickness), gridMax = gridWidth * gridHeight;
        if(gridWidth <= 0 || gridHeight <= 0)
            return dungeon;
        ArrayTools.fill(dungeon, '#');
        ArrayTools.fill(environment, DungeonTools.UNTOUCHED);
        region.resizeAndEmpty(width, height);
        IntIntOrderedMap links = new IntIntOrderedMap(gridMax), surface = new IntIntOrderedMap(gridMax);
        IntList choices = new IntList(4);
        int dx = rng.nextSignedInt(gridWidth), dy = rng.nextSignedInt(gridHeight),
                d = dy << 16 | dx;
        links.put(d, 0);
        surface.put(d, 0);
        for (int i = 0; i < initialAttempts && links.size() < gridMax && !surface.isEmpty(); i++) {
            choices.clear();
            if (dx < gridWidth - 1 && !links.containsKey(d + 1)) choices.add(1);
            if (dy < gridHeight - 1 && !links.containsKey(d + 0x10000)) choices.add(2);
            if (dx > 0 && !links.containsKey(d - 1)) choices.add(4);
            if (dy > 0 && !links.containsKey(d - 0x10000)) choices.add(8);
            if (choices.isEmpty()) {
                surface.remove(d);
                break;
            }
            int choice = choices.random(rng);
            links.replace(d, links.get(d) | choice);
            if (choices.size() == 1)
                surface.remove(d);
            switch (choice) {
                case 1:
                    d += 1;
                    links.put(d, 4);
                    surface.put(d, 4);
                    break;
                case 2:
                    d += 0x10000;
                    links.put(d, 8);
                    surface.put(d, 8);
                    break;
                case 4:
                    d -= 1;
                    links.put(d, 1);
                    surface.put(d, 1);
                    break;
                default:
                    d -= 0x10000;
                    links.put(d, 2);
                    surface.put(d, 2);
                    break;
            }
            dx = d & 0xFFFF;
            dy = d >>> 16;
        }
        while(links.size() < gridMax)
        {
            d = surface.random(rng);
            dx = d & 0xFFFF;
            dy = d >>> 16;
            for (int i = 0; i < attempts && links.size() < gridMax && !surface.isEmpty(); i++) {
                choices.clear();
                if (dx < gridWidth - 1 && !links.containsKey(d + 1)) choices.add(1);
                if (dy < gridHeight - 1 && !links.containsKey(d + 0x10000)) choices.add(2);
                if (dx > 0 && !links.containsKey(d - 1)) choices.add(4);
                if (dy > 0 && !links.containsKey(d - 0x10000)) choices.add(8);
                if (choices.isEmpty()) {
                    surface.remove(d);
                    break;
                }
                int choice = choices.random(rng);
                links.replace(d, links.get(d) | choice);
                if (choices.size() == 1)
                    surface.remove(d);
                switch (choice) {
                    case 1:
                        d += 1;
                        links.put(d, 4);
                        surface.put(d, 4);
                        break;
                    case 2:
                        d += 0x10000;
                        links.put(d, 8);
                        surface.put(d, 8);
                        break;
                    case 4:
                        d -= 1;
                        links.put(d, 1);
                        surface.put(d, 1);
                        break;
                    default:
                        d -= 0x10000;
                        links.put(d, 2);
                        surface.put(d, 2);
                        break;
                }
                dx = d & 0xFFFF;
                dy = d >>> 16;
            }
        }
        for (int i = 0; i < links.size(); i++) {
            d = links.keyAt(i);
            dx = d & 0xFFFF;
            dy = d >>> 16;
            int conn = links.getAt(i);

            region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness), roomWidth, roomHeight);
            if (divideRooms > 0.0 && rng.nextDouble() < divideRooms) {
                if ((conn & 1) != 0)
                {
                    region.removeRectangle(1 + dx * (roomWidth + wallThickness) + roomWidth, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) + roomWidth, 1 + dy * (roomHeight + wallThickness) + rng.nextSignedInt(roomHeight), wallThickness, 1);
                }
                if ((conn & 2) != 0)
                {
                    region.removeRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) + roomHeight, roomWidth, wallThickness);
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) + rng.nextSignedInt(roomWidth), 1 + dy * (roomHeight + wallThickness) + roomHeight, 1, wallThickness);
                }
                if ((conn & 4) != 0)
                {
                    region.removeRectangle(1 + dx * (roomWidth + wallThickness) - wallThickness, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) - wallThickness, 1 + dy * (roomHeight + wallThickness) + rng.nextSignedInt(roomHeight), wallThickness, 1);
                }
                if ((conn & 8) != 0)
                {
                    region.removeRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) - wallThickness, roomWidth, wallThickness);
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) + rng.nextSignedInt(roomWidth), 1 + dy * (roomHeight + wallThickness) - wallThickness, 1, wallThickness);
                }
            } else {
                if ((conn & 1) != 0)
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) + roomWidth, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
                if ((conn & 2) != 0)
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) + roomHeight, roomWidth, wallThickness);
                if ((conn & 4) != 0)
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness) - wallThickness, 1 + dy * (roomHeight + wallThickness), wallThickness, roomHeight);
                if ((conn & 8) != 0)
                    region.insertRectangle(1 + dx * (roomWidth + wallThickness), 1 + dy * (roomHeight + wallThickness) - wallThickness, roomWidth, wallThickness);
            }
        }
        region.writeCharsInto(dungeon, '.');
        region.writeIntsInto(environment, DungeonTools.ROOM_FLOOR);
        tempRegion.remake(region).fringe8way().writeIntsInto(environment, DungeonTools.ROOM_WALL);
        return dungeon;
    }

    /**
     * Gets the most recently-produced dungeon as a 2D char array, usually produced by calling {@link #generate()} or
     * some similar method present in a specific implementation. This normally passes a direct reference and not a copy,
     * so you can normally modify the returned array to propagate changes back into this IDungeonGenerator.
     *
     * @return the most recently-produced dungeon/map as a 2D char array
     */
    @Override
    public char[][] getPlaceGrid() {
        return dungeon;
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
        return environment;
    }

    @Override
    public String toString() {
        return "ConnectingMapGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", roomWidth=" + roomWidth +
                ", roomHeight=" + roomHeight +
                ", wallThickness=" + wallThickness +
                ", divideRooms=" + divideRooms +
                '}';
    }
}
