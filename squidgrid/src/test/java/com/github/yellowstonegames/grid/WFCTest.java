/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.FourWheelRandom;
import com.github.yellowstonegames.place.DungeonProcessor;
import com.github.yellowstonegames.place.DungeonTools;

public class WFCTest {
    public static void main(String[] args)
    {
        EnhancedRandom random = new FourWheelRandom(123456789);
        int[][] grid = new int[32][32];
        char[][] dungeon = new char[][]{
                "  ┌───────┐ ┌─────┐ ┌────────┐  ".toCharArray(),
                "┌─┤.......│ │.....└─┤........│  ".toCharArray(),
                "│.└┐......│┌┴───....│........│  ".toCharArray(),
                "│..├───┐..││.................│  ".toCharArray(),
                "│..│   │..││.................├─┐".toCharArray(),
                "│..└┐┌─┘..││....┌┐.....──────┘.│".toCharArray(),
                "│...└┘....││..──┤│.............│".toCharArray(),
                "│.........││....└┼─┐..........┌┘".toCharArray(),
                "└┐.....┌──┘│.....└┐└┬────────┬┘ ".toCharArray(),
                " │.....│   │......│ │........│  ".toCharArray(),
                " ├─...┌┘  ┌┴─..┌──┴─┘........│  ".toCharArray(),
                " │....│   │....│.............└─┐".toCharArray(),
                "┌┘...┌┘   │....│...............│".toCharArray(),
                "│....└─┐  │..┌─┴────...........│".toCharArray(),
                "│......└┐ │..│...............─.│".toCharArray(),
                "│.......└─┘..│.................│".toCharArray(),
                "│..┌┐...........┌───...........│".toCharArray(),
                "└──┘└─┐.........│............┌─┘".toCharArray(),
                "      └───┐..│..│............│  ".toCharArray(),
                "    ┌────┐└┬─┘..└┬───┐......┌┘  ".toCharArray(),
                " ┌──┘....│┌┘.....└─┐┌┘..─┬──┘   ".toCharArray(),
                "┌┘.......││........├┘....└┐     ".toCharArray(),
                "│........├┘........│......└┐    ".toCharArray(),
                "│........│...─┐....│.......└┐   ".toCharArray(),
                "└┐....│..│....│....│........│   ".toCharArray(),
                " └─┬──┘.......│..──┘..┌┐....│   ".toCharArray(),
                "   │..........│.......││....│   ".toCharArray(),
                "  ┌┘.....│....│......┌┘│...┌┘   ".toCharArray(),
                "  │......├────┤..──┬─┘ │...│    ".toCharArray(),
                "  │.....┌┘    │....│ ┌─┘..─┤    ".toCharArray(),
                "  └──┐..│     │....│ │.....│    ".toCharArray(),
                "     └──┘     └────┘ └─────┘    ".toCharArray(),
        };
//        System.out.println("new char[][]{");
        for (int y = 0; y < 32; y++) {
//            System.out.print('"');
            for (int x = 0; x < 32; x++) {
                grid[y][x] = dungeon[x][y];
//                System.out.print(dungeon[x][y]);
            }
//            System.out.println("\".toCharArray(),");
        }
//        System.out.println("};");
        WaveFunctionCollapse wfc = new WaveFunctionCollapse(grid, 3, 64, 64, random, true, true, 1, 0);
        while (!wfc.run(0));
        int[][] grid2 = wfc.result();
        char[][] map = new char[128][128];
        int[][] env = new int[128][128];
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                System.out.print((char) grid2[x & 63][y & 63]);
                switch (map[x][127-y] = (char) grid2[x & 63][y & 63])
                {
                    case ' ': env[x][127-y] = DungeonTools.UNTOUCHED;
                    break;
                    case '.': env[x][127-y] = DungeonTools.ROOM_FLOOR;
                    break;
                    default: env[x][127-y] = DungeonTools.ROOM_WALL;
                }

            }
            System.out.println();
        }
        System.out.println();
        DungeonProcessor proc = new DungeonProcessor(128, 128);
        map = LineTools.hashesToLines(proc.generate(map, env));
        proc.setPlaceGrid(map);
        System.out.println(proc);
        System.out.println();
        while (!wfc.run(0));
        grid2 = wfc.result();
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                System.out.print((char) grid2[x & 63][y & 63]);
            }
            System.out.println();
        }
        
    }
}
