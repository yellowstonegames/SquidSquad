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
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.CellularAutomaton;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.place.tileset.DungeonBoneGen;
import com.github.yellowstonegames.place.tileset.TilesetType;

/**
 * An IDungeonGenerator that distorts and smooths an ordinary dungeon map to make it appear like a cave complex.
 * This usually exhibits the complex connectivity that dungeons made with a {@link TilesetType} like
 * {@link TilesetType#DEFAULT_DUNGEON} have, but shouldn't have noticeable room/corridor areas, and should appear as
 * all one cave.
 * <br>
 * An example map this can produce:
 * <br>
 * <pre>
 * {@code
 *   ┌─────┐ ┌───────┬─┐ ┌───┐                ┌────┐   ┌────┐   ┌────┐    ┌──┐                  ┌───┐   ┌─────┐
 *  ┌┘.....└─┘.......│.└─┘...└─┐    ┌─┐    ┌──┘....│   │....└──┬┘....└┬───┘..└──┐  ┌───┐       ┌┘...│  ┌┘.....└┐
 *  │..........................└┐ ┌─┘.└┐   │.......└┐  │.......│......│.........└─┬┘...│  ┌───┬┘....│  │.......│
 *  └┐.........┌┐...............└─┘....└┐ ┌┘........│ ┌┘....│.....................│...─┤┌─┘...│.....│  │.......│
 *   │........┌┘│.......................└─┘.........└┐│.....│.....┌┐...................││...........│  │.......│
 *   │........│┌┘...............................#....││.....│.....│└┐..................└┘...........│  │.......└┐
 *   │.......┌┼┘.....┌┐............┌─┐..............┌┘│.....├─┐...│ │...............................│  │........│
 *   │......┌┘│....┌─┘└┐.........┌─┘ └──────┐.......│ │....┌┘ │...└─┤......#.......│................│  └┐.......│
 *   │.....┌┘ └┐.┌─┘   └┐.......┌┘          │......┌┘ │...┌┘  └─┐...│....│........┌┤........│.......│  ┌┘......┌┘
 *   └┐...─┤   └─┘      │......─┤  ┌──┐     │......│  │...│    ┌┘.......─┘.......┌┘└─┐....─┬┤.......└┐ │.......│
 *   ┌┘....└┐  ┌─┐  ┌───┘.......└──┘..└┐    └┐.....│ ┌┘...└┐ ┌─┘.................│  ┌┘.....│└┐.......└─┘.......└┐
 *  ┌┘......└──┘.└┐┌┘......─┐..........└─┐  ┌┘.....└─┘.....│┌┘...................│ ┌┘......│ │..................│
 * ┌┘.............└┘........│............└┬─┘..│...........└┘.....│.........───..└┬┘.......│ │....┌┐............│
 * │........................│......┌─┐....│....├┐...............──┘...............│........└─┘....│└┐...........│
 * │...┌─┐........................┌┘ └┐.......┌┘└┐................................................│ └┐..........│
 * │..┌┘ └┐.....................┌─┘  ┌┘.......│  │...............................................┌┘  │..........│
 * └──┘   └┐...................┌┘   ┌┘........│  └─┐...│...─┐...........│........................│   └┐.........│
 *         │..................┌┘    │.........│ ┌─┐└───┴┐...│...........├─┐........│.............└─┐  │.........│
 *     ┌─┐ └──┐.....┌──┐......│    ┌┘...┌───┐.└─┘.└┐    └┐..├┐..........│ └┐......┌┤...............│  └─┐.......│
 *     │.└─┐  └┐...┌┘  └┐.....└┐   │....│  ┌┘......└┐    │..│└───┐.....┌┘  └┐....┌┘├─.....┌──┐.....│    │......┌┘
 *     │...└┐  └┐..│   ┌┘......└─┬─┘...─┤  │........└┐ ┌─┘.─┤    │.....│    │....│ │......└┐ └┐....│    └┐....─┤
 *     └┐...│  ┌┘..│   │.........│......│ ┌┘.........└─┘....└─┐ ┌┘.....│   ┌┘....├─┘.......└┐ │....│     │.....│
 *      └┐..│ ┌┘...│   │................│┌┘...................│ │......└───┘.....│..........│ │....└─┐   │...┌─┘
 *       │..├─┘...─┤  ┌┘................├┘....................└─┘................│..........└┬┘......└─┐┌┘...│
 *       ├─.│......│ ┌┘..<........│...#.│...........................┌┬─......................│.........└┘...┌┘
 *       │..│......│┌┘............│................................─┴┘......................................│
 *      ┌┘......┌──┘│....................┌─┐.....................................┌┐.........................│
 *      │....┌──┘   └─┐..................│ └─┐.┌─┐....┌─┐....┌─┐.........┌─┐.....│└─┐......┌─┐....┌──┐......│
 *      │...┌┘        └─┐...........┌────┘   └─┘┌┴─┬──┘ └────┘ │........┌┘ │....┌┘  │.....┌┘ └────┘  └──┐...│
 *      │..┌┘           └┐.....┌─┐..└┐          │..│   ┌──┐    └┐......┌┘ ┌┘....└┐  └┐....│    ┌────┐   │...└┐
 *     ┌┘..└┐ ┌─────┐   ┌┘....┌┘ └┐..└┐        ┌┘..│ ┌─┘..└─┐   └┐.....│  │......└───┘....└┐  ┌┘....└┐  └┐...│
 *    ┌┘....└─┘.....└┐ ┌┘.....└┐ ┌┘...└┐       │...│┌┘......│    │.....│  └┐..........#....│ ┌┘......└┐  │...│
 *   ┌┘..........│...└┬┘.......└┐│.....└─┐   ┌─┘...││.......├─┐  │.....│   │...............└─┘........└┐ └┐..└┐
 *  ┌┘.......┌───┴┐...│.........││.......│  ┌┘....┌┘│.......│.└──┼┬─...│  ┌┘...........................└┐ │...│
 * ┌┘.......┌┘    │.............└┘.......└┐ │...┌─┘ │...│.#......└┘....│ ┌┘........................│....└─┴─..└┐
 * │........│    ┌┘.......................│ │..┌┘   └┐..│..............└─┘......──.................│...........│
 * │........│   ┌┘..........┌─┐..........┌┘ │..│     └┐.............................┌─┐........................└┐
 * │........│  ┌┘.....┌─────┘ └──┐.......│  │..│      │.........................┌───┘ ├─...┌────┐...............│
 * │........│  │.....┌┘          └┐.....┌┘  └┐.└┐     └┐......┌─┬─┐....┌──┐.....│    ┌┘...┌┘    │....┌─┐........│
 * └┐......┌┘  │.....│    ┌────┐  │.....│   ┌┘..└┐     └┐....┌┘┌┘.│....│  └┐....│   ┌┘....│     │....│ │........│
 *  │....┌─┘   │.....└┐ ┌─┘....│  └┐...─┤  ┌┘....│      └┐...└┬┘.......│   │....└┐ ┌┘.....│  ┌──┘....└┬┘........│
 *  │....└─┐   └┐.....└─┘......└┐  │....└──┘.....└─┐  ┌─┬┘....│........└┐ ┌┘.....└┐│......└──┘........│.........│
 *  └┐.....└┐ ┌─┴─..............│ ┌┘...............└──┘.│...............│┌┘.......└┘......................┌┐....│
 *   │......└─┘.................└┐│.....................................└┘...............................─┤└┐...│
 *  ┌┘.........│.................└┘.........................│................┌──┐.#.#.....................│ └┐.┌┘
 *  │..........│..........................................──┼──┐.....#......┌┘  │....................│....└┐ ├─┤
 *  └┐.........│.....................┌─┐....................│  └─┐..........│   └─┐................#.├─┐...└─┘.└┐
 *   │....┌┐.........┌─┐.........│..┌┘ └─┐..................│    │...┌───┐..└┐    │..................│ │........│
 *   └┐...│└─┐.....┌─┘ └┐......┌─┴┐.└┐   └─┐..........│.....│    │...└┐  └┐..└─┐  └─┐....┌──┐.......┌┘ └┐.......│
 *   ┌┘..┌┘  └┐...┌┘    └┐.....│  │..│ ┌─┐ └┐.......┌─┤....┌┘    └┐...└─┬─┘....└─┐  └┐...└┐ └┐......│   │......┌┘
 *   │...│    │...└───┐  │.....└┐┌┘..│┌┘.└┐ └┐.....┌┘ │....│      ├─....│........│   └┐...└┐ └┐.....└┐ ┌┘......│
 *  ┌┘..┌┘   ┌┘.......│  │......└┘...││...└┐ │.....└┐┌┘....└┐┌────┘.........┌─┐..└┐   └┐...└┐ └┐.....└─┘.......└┐
 * ┌┘..┌┘    │........│  └┐..........└┘....└─┘......└┘......└┘..............│ └┐..└┐   │....│ ┌┘................│
 * │...│    ┌┘....┌───┘   └┐...............................................─┤  └┐..└┐  └┐...└─┤.................│
 * │..┌┘   ┌┘....┌┘        │..........................................┌┐....│   │...└┐  │.....│.................│
 * │.┌┘   ┌┘.....└┐        │..........................................│└─┐.┌┘  ┌┘....└─┐│.......................│
 * │.└┐  ┌┘.......└───┐   ┌┘.......┌┐......┌─┐..................│.....│  └─┘   │.......└┘.....>.........#.......│
 * │..└┬─┘............│┌──┘......┌─┘└┐.....│ └┐..─┐.............│.....│        │.............┌─┐...............┌┘
 * │...│..┌──┐........││........┌┘   └┐...┌┘  └┐..├───┐..............┌┘        │..┌─┐......┌─┘ ├─....┌┐........│
 * │......│  └┐......┌┘└┐......┌┘     └───┘    └──┘   └┐....┌──┐....┌┘       ┌─┴─┬┘ │.....┌┘   │....┌┘└┐......┌┘
 * └─┐....│   ├─.....└┐ │......└┐     ┌──┐             │....│  └┐..┌┘       ┌┘...└┐ │.....│   ┌┘....│  └┐.....│
 *   ├─..┌┘  ┌┘.......│ └┐......└┐   ┌┘..│             └┐...│   └──┘       ┌┘.....└─┘.....└─┐┌┘.....└┐  │.....│
 * ┌─┘...└┐ ┌┘........└┐ │.......└───┘...│              │..┌┘              │................└┘.......│  └┐....└─┐
 * │......└─┘..........│ └─┐.............└─┬───┐       ┌┘.┌┘               │.........................└┐ ┌┘......│
 * │...................└┐  └───┐...........│...└───────┘..└┐               │.......┌─┐................│ │.......│
 * └┐...................│      │..........┌┴┐..............└┐              ├─......│ └┐...............└─┘.......│
 *  └┐.#................│      │..........│ └┐..............└┐         ┌───┘.....┌─┘  │.........................│
 *   └┐...............┌─┘      │.........┌┘  └┐.....┌───┐....│         │.........│    │......│....┌──────┐.....┌┘
 *    └┐.┌───┐........│        └───┐....┌┘    └─┐.┌─┘   └─┐.┌┘         └┐......┌─┘    └─┐...┌┴────┘      └─┐..┌┘
 *     └─┘   └────────┘            └────┘       └─┘       └─┘           └──────┘        └───┘              └──┘
 * }
 * </pre>
 */
public class FlowingCaveGenerator implements PlaceGenerator {
    public DungeonBoneGen gen;
    public final int width;
    public final int height;
    public TilesetType type;
    public EnhancedRandom rng;
    public final int[][] environment;
    private boolean remakeEnvironment = true;
    protected CellularAutomaton ca;

    /**
     * Default constructor that makes a 80x80 cave map with a random seed.
     */
    public FlowingCaveGenerator()
    {
        this(80, 80);
    }

    /**
     * Makes a cave map with the specified dimensions and a random seed.
     * @param width the width of the dungeon map(s) to generate
     * @param height the height of the dungeon map(s) to generate
     */
    public FlowingCaveGenerator(int width, int height) {
        this(width, height, TilesetType.DEFAULT_DUNGEON, new WhiskerRandom());
    }

    /**
     *
     * @param width the width of the dungeon map(s) to generate
     * @param height the height of the dungeon map(s) to generate
     * @param type a TilesetType enum value; {@link TilesetType#DEFAULT_DUNGEON} is used if null or unspecified
     * @param rng a random number generator to use when generating the caves; if null this will use a default RNG
     */
    public FlowingCaveGenerator(int width, int height, TilesetType type, EnhancedRandom rng) {
        this.width = Math.max(3, width);
        this.height = Math.max(3, height);
        this.type = type == null ? TilesetType.DEFAULT_DUNGEON : type;
        this.rng = rng == null ? new WhiskerRandom() : rng;
        gen = new DungeonBoneGen(this.rng);
        ca = new CellularAutomaton(this.width, this.height);
        environment = new int[this.width][this.height];
    }

    /**
     * Generates a flowing cave dungeon withthe same {@link TilesetType} this was made with, or
     * {@link TilesetType#DEFAULT_DUNGEON} if none was specified. This uses the
     * convention of '#' representing a wall and '.' representing a bare floor.
     *
     * @return a 2D char array representing a cave system with '#' for walls and '.' for floors
     */
    @Override
    public char[][] generate() {
        return generate(type);
    }

    /**
     * Generates a flowing cave dungeon with a different {@link TilesetType} than this generator was made with.
     * The default type is {@link TilesetType#DEFAULT_DUNGEON} if unspecified in the constructor.
     * @param type a TilesetType enum value
     * @return a 2D char array for the cave system
     */
    public char[][] generate(TilesetType type) {
        remakeEnvironment = true;
        gen.generate(type, width, height);
        ca.remake(gen.region);
        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.85f);
        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.85f);
        ca.current.remake(gen.region.deteriorate(rng, 0.9f));
        gen.region.or(ca.runBasicSmoothing());
        ca.current.remake(gen.region.removeEdges().largestPart());
        gen.region.remake(ca.runDiagonalGapCleanup());
        return gen.region.intoChars(gen.getDungeon(), '.', '#');
    }

    /**
     * Generates a flowing cave dungeon with a different {@link TilesetType} than this generator was made with, and
     * specifying a chance to keep the original walls of rooms before the flowing smoothing step is performed.
     * {@code roomChance} can be between 0.0 and 1.0, and if a room (identified with a similar technique to
     * {@link RoomFinder}, but not using it directly) is randomly selected to be preserved (the probability per room is
     * roomChance), then most of its walls will be kept in-place, generally with more right angles than the caves will
     * have. It may be best to keep roomChance above 0.5 if you want the effect to be noticeable. Starting with
     * {@link TilesetType#DEFAULT_DUNGEON} is a good choice for {@code type}.
     * @param type a TilesetType enum value
     * @param roomChance the chance, from 0.0 to 1.0, to preserve each room, keeping its walls where they start
     * @return a 2D char array for the cave system
     */
    public char[][] generate(TilesetType type, double roomChance) {
        remakeEnvironment = true;
        gen.generate(type, width, height);
        ObjectList<Region> rooms = gen.region.copy().retract8way().flood8way(gen.region, 1).split();
        ca.remake(gen.region);
        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.9f);
        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.9f);
        ca.current.remake(gen.region.deteriorate(rng, 0.9f));
        gen.region.or(ca.runBasicSmoothing());
        for (int i = 0; i < rooms.size(); i++) {
            if(rng.nextDouble() < roomChance)
            {
                gen.region.andNot(rooms.get(i).fringe8way().deteriorate(rng, 0.81f));
            }
        }
        gen.region.remake(gen.region.removeEdges());
        gen.region.insertSeveral(DungeonTools.ensurePath(gen.region.intoChars(gen.getDungeon(), '.', '#'), rng, '.', '#'));
        ca.current.remake(gen.region.largestPart());
        gen.region.remake(ca.runDiagonalGapCleanup());
        return gen.region.intoChars(gen.getDungeon(), '.', '#');
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
        return gen.getDungeon();
    }

    /**
     * Gets an environment map as a 2D int array that {@link DungeonProcessor} can use along with the normal
     * 2D char array dungeon map to add dungeon features. This marks cells as either {@link DungeonTools#UNTOUCHED}
     * (equal to 0), {@link DungeonTools#NATURAL_FLOOR}, or {@link DungeonTools#NATURAL_WALL}.
     * If the environment has not yet been retrieved since generate() was last called, this assigns the environment map
     * to match the dungeon map; otherwise it uses the cached environment map.
     * @return a 2D int array that can be used as an environment map with SectionDungeonGenerator.
     */
    public int[][] getEnvironment()
    {
        if(remakeEnvironment)
        {
            gen.region.writeIntsInto(environment, DungeonTools.NATURAL_FLOOR);
            gen.workingRegion.remake(gen.region).fringe8way().writeIntsInto(environment, DungeonTools.NATURAL_WALL);
            remakeEnvironment = false;
        }
        return environment;
    }

    @Override
    public String toString() {
        return "FlowingCaveGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", type=" + type +
                '}';
    }
}
