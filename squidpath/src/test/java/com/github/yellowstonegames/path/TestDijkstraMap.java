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

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Measurement;
import org.junit.Assert;
import org.junit.Test;

public class TestDijkstraMap {
    /**
     * Prints with y pointing down, matching how 2D arrays are entered in source code.
     * @param level a 2D char array that will be printed to stdout with y-down
     */
    public static void print(char[][] level) {
        for (int y = 0; y < level[0].length; y++) {
            for (int x = 0; x < level.length; x++) {
                System.out.print(level[x][y]);
            }
            System.out.println();
        }
    }
    @Test
    public void testMultipleGoals() {
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
        ObjectDeque<Coord> path = new ObjectDeque<>(16);

        Coord start = Coord.get(4, 4), goal0 = Coord.get(5, 5), goal1 = Coord.get(5, 6);

//        dm.setGoal(goal0);
//        dm.setGoal(goal1);
//        dm.partialScan(10, null);
//        dm.findPathPreScanned(path, start);

        dm.findPath(path, 10, 10, null, null, start, goal0, goal1);

        char ch = '1';
        for(Coord c : path) {
            map[c.x][c.y] = ch++;
        }
        map[start.x][start.y] = '0';
        print(map);
        // currently fails even though goal1 is the shortest path
        Assert.assertEquals(goal1, path.last());
    }
}
