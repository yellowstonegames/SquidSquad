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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.function.IntIntPredicate;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import org.junit.Assert;
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
        ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    grid[x][y] = (BresenhamLine.reachable(10, 10, x, y, length, res, buffer)) ? '*' : '0';
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
        ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    grid[x][y] = (BresenhamLine.reachableEuclidean(10, 10, x, y, length, res, buffer)) ? '*' : '0';
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
    public void testOrtho() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    grid[x][y] = (OrthoLine.reachable(10, 10, x, y, length, res, buffer)) ? '*' : '0';
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
    public void testOrthoSymmetry() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        for (int x = 1; x < 20; x++) {
            for (int y = 1; y < 20; y++) {
                for (int i = 1; i < 20; i++) {
                    for (int j = 1; j < 20; j++) {
                        if(x == i && y == j) continue;
                        Assert.assertEquals(OrthoLine.reachable(x, y, i, j, 42, res),
                                OrthoLine.reachable(i, j, x, y, 42, res));
                    }
                }
            }
        }
    }
    public static boolean sadLine(int x1, int y1, int x2, int y2, IntIntPredicate plot)
    {
        int w = x2 - x1;
        int h = y2 - y1;
        int dx1 = 0, dy1 = 0, dx2 = 0, dy2 = 0;
        if (w < 0) dx1 = -1; else if (w > 0) dx1 = 1;
        if (h < 0) dy1 = -1; else if (h > 0) dy1 = 1;
        if (w < 0) dx2 = -1; else if (w > 0) dx2 = 1;
        int longest = Math.abs(w);
        int shortest = Math.abs(h);
        if (!(longest > shortest))
        {
            longest = Math.abs(h);
            shortest = Math.abs(w);
            if (h < 0) dy2 = -1; else if (h > 0) dy2 = 1;
            dx2 = 0;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++)
        {
            if(!plot.test(x1, y1)) return false;
            numerator += shortest;
            if (!(numerator < longest))
            {
                numerator -= longest;
                x1 += dx1;
                y1 += dy1;
            }
            else
            {
                x1 += dx2;
                y1 += dy2;
            }
        }
        return true;
    }
    @Test
    public void testSadLine() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        final ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            for (int x = 1; x < 20; x++) {
                for (int y = 1; y < 20; y++) {
                    buffer.clear();
                    int finalLength = length;
                    grid[x][y] = (sadLine(10, 10, x, y, (a, b) -> res[a][b] < 1f && buffer.add(Coord.get(a, b)) && buffer.size() <= finalLength
                    )) ? '*' : '0';
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
    public void testBresenhamLineDraw() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        final ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        final EnhancedRandom random = new WhiskerRandom(123);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            {
                buffer.clear();
                int finalLength = length;
                float theta = random.nextFloat();
                int x = Math.round(TrigTools.cosTurns(theta) * length + 10);
                int y = Math.round(TrigTools.sinTurns(theta) * length + 10);

                BresenhamLine.reachable(10, 10, x, y, length, res, buffer);
                for(Coord c : buffer)
                    grid[c.x][c.y] = '*';
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
    public void testOrthoLineDraw() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        final ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        final EnhancedRandom random = new WhiskerRandom(123);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            {
                buffer.clear();
                int finalLength = length;
                float theta = random.nextFloat();
                int x = Math.round(TrigTools.cosTurns(theta) * length + 10);
                int y = Math.round(TrigTools.sinTurns(theta) * length + 10);

                OrthoLine.reachable(10, 10, x, y, length, res, buffer);
                for(Coord c : buffer)
                    grid[c.x][c.y] = '*';
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
    public void testSadLineDraw() {
        char[][] grid = new char[21][21], interior = new char[19][19];
        ArrayTools.fill(grid, '#');
        ArrayTools.fill(interior, '.');
        ArrayTools.insert(interior, grid, 1, 1);
        float[][] res = new float[21][21], floors = new float[19][19];
        ArrayTools.fill(res, 1f);
        ArrayTools.insert(floors, res, 1, 1);
        final ObjectDeque<Coord> buffer = new ObjectDeque<>(40);
        final EnhancedRandom random = new WhiskerRandom(123);
        for (int length = 0; length < 10; length++) {
            System.out.println("\n" + length);
            ArrayTools.insert(interior, grid, 1, 1);
            {
                buffer.clear();
                int finalLength = length;
                float theta = random.nextFloat();
                int x = Math.round(TrigTools.cosTurns(theta) * length + 10);
                int y = Math.round(TrigTools.sinTurns(theta) * length + 10);

                sadLine(10, 10, x, y, (a, b) -> res[a][b] < 1f && buffer.add(Coord.get(a, b)) && buffer.size() < finalLength);
                for(Coord c : buffer)
                    grid[c.x][c.y] = '*';
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
