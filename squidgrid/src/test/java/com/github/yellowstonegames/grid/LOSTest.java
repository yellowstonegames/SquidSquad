package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.core.ArrayTools;
import org.junit.Test;

public class LOSTest {
    @Test
    public void testBresenham() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        ObjectList<Coord> buffer = new ObjectList<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    grid[x][y] = (BresenhamLine.isReachable(10, 10, x, y, length, res)) ? '*' : '0';
                }
            }
            for (int y = 0; y < 21; y++) {
                for (int x = 0; x < 21; x++) {
                    System.out.print(grid[x][y]);
                }
                System.out.println();
            }
        }
    }
    @Test
    public void testBresenhamEuclidean() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.fill(floors, 0f);
        ArrayTools.insert(floors, res, 1, 1);
        ObjectList<Coord> buffer = new ObjectList<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    grid[x][y] = (BresenhamLine.isReachableEuclidean(10, 10, x, y, length, res, buffer)) ? '*' : '0';
                }
            }
            for (int y = 0; y < 21; y++) {
                for (int x = 0; x < 21; x++) {
                    System.out.print(grid[x][y]);
                }
                System.out.println();
            }
        }
    }
}
