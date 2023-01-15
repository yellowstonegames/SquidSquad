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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Region;

/**
 * Meant to produce the sort of narrow, looping, not-quite-maze-like passages found in a certain famous early arcade
 * game. This can generate either the pair of {@link #getPlaceGrid()} and {@link #getEnvironment()} that classes like
 * {@link DungeonProcessor} use, or can produce a {@link Region} with {@link #getRegion()}.
 */
public class PacMazeGenerator implements PlaceGenerator {
    public EnhancedRandom rng;
    public int width, height;
    private Region map;
    private int[][] env;
    private char[][] maze;

    public PacMazeGenerator() {
        this(80, 80);
    }

    public PacMazeGenerator(int width, int height) {
        this(width, height, new WhiskerRandom());
    }

    public PacMazeGenerator(int width, int height, EnhancedRandom rng) {
        this.height = height;
        this.width = width;
        this.rng = rng;
        this.map = new Region(width, height);
    }

    private static final byte[] //unbiased_connections = new byte[]{3, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15},
            connections = new byte[]{
            3, 5, 6, 9, 10, 12,/*
                    3, 5, 6, 9, 10, 12,
                    3, 5, 6, 9, 10, 12,
                    3, 5, 6, 9, 10, 12,
                    7, 11, 13, 14,
                    7, 11, 13, 14,
                    15*/
    };
    private static final int connections_length = connections.length;

    private void write(Region m, int x, int y, int xOffset, int yOffset) {
        final int nx = x * 3 + xOffset + 1, ny = y * 3 + yOffset + 1;
        m.insert(nx, ny);
    }

    public Region create() {
        map.resizeAndEmpty(width, height);
        byte[][] conns = new byte[(width + 2) / 3][(height + 2) / 3];
        int xOff = (width % 3 == 1) ? -1 : 0, yOff = (height % 3 == 1) ? -1 : 0;
        for (int x = 0; x < (width + 2) / 3; x++) {
            for (int y = 0; y < (height + 2) / 3; y++) {
                conns[x][y] = connections[rng.nextInt(connections_length)];
            }
        }
        for (int x = 0; x < (width + 2) / 3; x++) {
            for (int y = 0; y < (height + 2) / 3; y++) {
                write(map, x, y, xOff, yOff);
                if (x > 0 && ((conns[x - 1][y] & 1) != 0 || (conns[x][y] & 2) != 0)) {
                    conns[x - 1][y] |= 1;
                    conns[x][y] |= 2;
                }
                if (x < conns.length - 1 && ((conns[x + 1][y] & 2) != 0 || (conns[x][y] & 1) != 0)) {
                    conns[x + 1][y] |= 2;
                    conns[x][y] |= 1;
                }
                if (y > 0 && ((conns[x][y - 1] & 4) != 0 || (conns[x][y] & 8) != 0)) {
                    conns[x][y - 1] |= 4;
                    conns[x][y] |= 8;
                }
                if (y < conns[0].length - 1 && ((conns[x][y + 1] & 8) != 0 || (conns[x][y] & 4) != 0)) {
                    conns[x][y + 1] |= 8;
                    conns[x][y] |= 4;
                }
            }
        }

        for (int x = 1; x < (width - 1) / 3; x++) {
            for (int y = 1; y < (height - 1) / 3; y++) {
                if (Integer.bitCount(conns[x][y]) >= 4) {
                    //byte temp = connections[rng.nextInt(connections_length)];
                    int temp = 1 << rng.nextInt(4);
                    conns[x][y] ^= temp;
                    if ((temp & 2) != 0) conns[x - 1][y] ^= 1;
                    else if ((temp & 1) != 0) conns[x + 1][y] ^= 2;
                    else if ((temp & 8) != 0) conns[x][y - 1] ^= 4;
                    else if ((temp & 4) != 0) conns[x][y + 1] ^= 8;
                }
            }
        }
        for (int x = 0; x < (width + 2) / 3; x++) {
            for (int y = 0; y < (height + 2) / 3; y++) {
                write(map, x, y, xOff, yOff);
                if (x > 0 && (conns[x][y] & 2) != 0)
                    write(map, x, y, xOff - 1, yOff);
                if (x < conns.length - 1 && (conns[x][y] & 1) != 0)
                    write(map, x, y, xOff + 1, yOff);
                if (y > 0 && (conns[x][y] & 8) != 0)
                    write(map, x, y, xOff, yOff - 1);
                if (y < conns[0].length - 1 && (conns[x][y] & 4) != 0)
                    write(map, x, y, xOff, yOff + 1);
            }
        }
        int upperY = height - 1;
        int upperX = width - 1;
        map.removeEdges();
        return map;
    }

    @Override
    public char[][] generate() {
        create();
        maze = new char[width][height];
        env = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean b = map.contains(x, y);
                maze[x][y] = b ? '.' : '#';
                env[x][y] = b ? DungeonTools.CORRIDOR_FLOOR : DungeonTools.CORRIDOR_WALL;
            }
        }

        return maze;
    }

    @Override
    public int[][] getEnvironment() {
        if (env == null) {
            generate();
        }
        return env;
    }

    /**
     * Gets the maze as a {@link Region}, where true means passable or false means blocked.
     *
     * @return a {@link Region} with the same width and height as this; true is passable and false is not.
     */
    public Region getRegion() {
        if (map == null)
            return create();
        return map;
    }

    /**
     * Gets the maze as a 2D array of '.' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    @Override
    public char[][] getPlaceGrid() {
        if (maze == null)
            return generate();
        return maze;
    }

    @Override
    public String toString() {
        return "PacMazeGenerator{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
