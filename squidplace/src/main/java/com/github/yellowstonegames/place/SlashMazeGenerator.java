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

import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.grid.Region;

/**
 * Produces a maze-like place made of thick diagonal walls only.
 * This is the same idea used by this classic BASIC program:
 * <code>10 PRINT CHR$(205.5+RND(1)); : GOTO 10</code>
 * <br>
 * This does not make any guarantees about how much of the map will be
 * possible to enter.
 */
public class SlashMazeGenerator implements PlaceGenerator {
    public EnhancedRandom rng;
    public int width, height;
    public boolean thin = false;
    private final Region map;
    private final transient Region buffer = new Region(1, 1);
    private int[][] env;
    private char[][] maze;

    private static final Region backslash = new Region(new String[]{
            "##...#",
            "###...",
            ".###..",
            "..###.",
            "...###",
            "#...##",}, '.');
    private static final Region slash = backslash.copy().flip(true, false);

    public SlashMazeGenerator() {
        this(80, 80);
    }

    public SlashMazeGenerator(int width, int height) {
        this(width, height, new AceRandom());
    }

    public SlashMazeGenerator(int width, int height, EnhancedRandom rng) {
        this.height = height;
        this.width = width;
        this.rng = rng;
        this.map = new Region(width, height);
    }

    public Region create() {
        map.resizeAndEmpty(width, height);
        for (int x = 0; x < width; x+=6) {
            for (int y = 0; y < height; y+=6) {
                map.insert(x, y, rng.nextBoolean() ? slash : backslash, buffer);
            }
        }
        if(thin)
            map.or(map.copy().neighborDown());
        map.removeEdges();
        return map;
    }

    @Override
    public char[][] generate() {
        create();
        maze = map.toChars('.', '#');
        env = map.toInts(DungeonTools.CORRIDOR_FLOOR, DungeonTools.CORRIDOR_WALL);
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
        if (map == null) {
            generate();
        }
        return map;
    }

    /**
     * Gets the maze as a 2D array of '.' for passable or '#' for blocked.
     *
     * @return a 2D char array; '.' is passable and '#' is not.
     */
    @Override
    public char[][] getPlaceGrid() {
        if (maze == null) {
            return generate();
        }
        return maze;
    }

    public EnhancedRandom getRng() {
        return rng;
    }

    public void setRng(EnhancedRandom rng) {
        if(rng == null)
            this.rng = new AceRandom();
        else
            this.rng = rng;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isThin() {
        return thin;
    }

    public SlashMazeGenerator setThin(boolean thin) {
        this.thin = thin;
        return this;
    }

    @Override
    public String toString() {
        return "SlashMazeGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", thin=" + thin +
                '}';
    }
}
