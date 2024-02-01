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

import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.grid.LineTools;

public class ConnectingMapGeneratorDemo {

    public static void main(String[] args) {
        DistinctRandom random = new DistinctRandom(0xB0BAFE77L);
        // Use to generate 1x1 rooms with 1x1 connecting corridors between some rooms.
        // There will always be a path between any pair of rooms.
        /*
        ConnectingMapGenerator gen = new ConnectingMapGenerator(101, 101, 1, 1, random, 1, 1.0);
         */
        ConnectingMapGenerator gen = new ConnectingMapGenerator(41, 41, 4, 4, random, 1, 0.8f);
        //// Use when you want '#' to indicate "no connection" and '.' to indicate a connection
//        char[][] map = gen.generate();
        //// Use when you want box-drawing character lines to indicate "no connection"; '.' indicates a connection
        char[][] map = LineTools.hashesToLines(gen.generate());
        //// SquidSquad uses the [x][y] convention to index 2D arrays. If you aren't using box-drawing, though, the map
        //// is indistinguishable when indexed with [y][x], it just will be transposed (left-right swaps with up-down).
        //// It uses y-up, like most of libGDX, even for 2D arrays.

        // Use this to mark rooms with 'R'
        /*
        //// This marks every room position with the char 'R' to tell them apart from connections, which use '.'
        //// Room positions are the spots in the array where x is odd and y is odd.
        //// If you want to know if a given position is a room, you can use: ((x & y & 1) == 1)
        //// If that is true, the x,y position is a room. Otherwise, it is either "connection" or "no connection"
        for (int x = 1; x < map.length - 1; x += 2) {
            for (int y = 1; y < map[x].length - 1; y += 2) {
                map[x][y] = 'R';
            }
        }
        //// 'R' will indicate a room.
        //// You can get a random room position with:
//        int roomX = random.nextInt(50)*2+1;
//        int roomY = random.nextInt(50)*2+1;
        //// Every room can (eventually) get to every other room by traversing '.' connections.
        //// The 1-4 neighbors each R room has are connected via a '.'; if there is anything else, they don't connect.
         */
        //// This just shows the map in the console.
        DungeonTools.debugPrint(map);
    }
}
