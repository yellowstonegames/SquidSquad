package com.github.yellowstonegames.seek;

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.gand.utils.GridMetric;
import com.github.yellowstonegames.grid.Coord;
import org.junit.Assert;
import org.junit.Test;

public class DijkstraMapTest {
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
        DijkstraMap dm = new DijkstraMap(map, GridMetric.EUCLIDEAN);
        dm.setBlockingRequirement(2);
        ObjectDeque<Coord> path = new ObjectDeque<>(16);

        Coord start = Coord.get(4, 4), goal0 = Coord.get(5, 5), goal1 = Coord.get(5, 6);

//        dm.setGoal(goal0);
//        dm.setGoal(goal1);
//        dm.partialScan(10, null);
//        dm.findPathPreScanned(path, start);

        dm.findPath(path, 10, 10, null, null, start, ObjectDeque.with(goal0, goal1));

        System.out.println(dm.show(false));
        char ch = '1';
        for(Coord c : path) {
            map[c.x][c.y] = ch++;
        }
        map[start.x][start.y] = '0';
        print(map);
        Assert.assertEquals(goal1, path.last());
    }

}
