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

package com.github.yellowstonegames.path;

import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Measurement;

public class TestDijkstraMap {
    public static void print(char[][] level) {
        for (int y = level[0].length - 1; y >= 0; y--) {
            for (int x = 0; x < level.length; x++) {
                System.out.print(level[x][y]);
            }
            System.out.println();
        }
    }
    public static void main(String[] args) {
        char[][] map = {
                "#########".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#....#..#".toCharArray(),
                "#...#...#".toCharArray(),
                "#.......#".toCharArray(),
                "#.......#".toCharArray(),
                "#########".toCharArray(),
        };
        DijkstraMap dm = new DijkstraMap(map, Measurement.EUCLIDEAN);
        dm.setBlockingRequirement(2);
        ObjectList<Coord> path = new ObjectList<>(16);
        dm.setGoal(3, 3);
        dm.partialScan(10, null);
        dm.findPathPreScanned(path, Coord.get(5, 5));
        char ch = '0';
        for(Coord c : path) {
            map[c.x][c.y] = ch++;
        }
        print(map);
    }
}
