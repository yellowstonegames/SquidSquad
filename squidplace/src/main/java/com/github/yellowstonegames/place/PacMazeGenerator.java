package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;

/**
 * Meant to produce the sort of narrow, looping, not-quite-maze-like passages found in a certain famous early arcade game.
 */
public class PacMazeGenerator implements PlaceGenerator {
    public EnhancedRandom rng;
    public int width, height;
    private boolean[][] map;
    private int[][] env;
    private char[][] maze;

    public PacMazeGenerator() {
        this(250, 250);
    }

    public PacMazeGenerator(int width, int height) {
        this.height = height;
        this.width = width;
        rng = new LaserRandom();
    }

    public PacMazeGenerator(int width, int height, EnhancedRandom rng) {
        this.height = height;
        this.width = width;
        this.rng = rng;
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

    private void write(boolean[][] m, int x, int y, int xOffset, int yOffset) {
        final int nx = x * 3 + xOffset + 1, ny = y * 3 + yOffset + 1;
        if (nx >= 0 && nx < m.length && ny >= 0 && ny < m[nx].length) {
            m[nx][ny] = true;
        }
    }

    public boolean[][] create() {
        map = new boolean[width][height];
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
        for (int i = 0; i < width; i++) {
            map[i][0] = false;
            map[i][upperY] = false;
        }
        for (int i = 0; i < height; i++) {
            map[0][i] = false;
            map[upperX][i] = false;
        }
        return map;
    }

    @Override
    public char[][] generate() {
        create();
        maze = new char[width][height];
        env = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                maze[x][y] = map[x][y] ? '.' : '#';
                env[x][y] = map[x][y] ? DungeonTools.CORRIDOR_FLOOR : DungeonTools.CORRIDOR_WALL;
            }
        }

        return maze;
    }

    @Override
    public int[][] getEnvironment() {
        if (env == null)
            return ArrayTools.fill(DungeonTools.CORRIDOR_WALL, width, height);
        return env;
    }

    /**
     * Gets the maze as a 2D array of true for passable or false for blocked.
     *
     * @return a 2D boolean array; true is passable and false is not.
     */
    public boolean[][] getBooleanMaze() {
        if (map == null)
            return create();
        return map;
    }

    /**
     * Gets the maze as a 2D array of ',' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    @Override
    public char[][] getPlaceGrid() {
        if (maze == null)
            return generate();
        return maze;
    }
}
