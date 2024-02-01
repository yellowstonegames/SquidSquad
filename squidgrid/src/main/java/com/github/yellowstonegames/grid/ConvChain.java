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

import com.github.tommyettinger.random.EnhancedRandom;

import java.util.Collection;

/**
 * A class that imitates patterns in an existing {@link Region} and uses it to fill another Region so it has a
 * similar visual style. Useful for creating procedural filler around a desired path or series of known rooms. Can also
 * convert between Regions (samples) and 2D char arrays (maps).
 * <br>
 * Ported from <a href="https://github.com/mxgmn/ConvChain">ConvChain</a> (which is in the public domain).
 */
public final class ConvChain {

    private ConvChain()
    {

    }
    /**
     * Converts a 2D char array map to a Region, where any chars in the array or vararg yes will result in
     * true in the returned array at that position and any other chars will result in false. The result can be given to
     * fill() as its sample parameter.
     * @param toFill
     * @param map a 2D char array that you want converted to a Region
     * @param yes an array or vararg of the chars to consider true in map
     * @return a 2D boolean array that can be given to fill()
     */
    public static Region mapToSample(Region toFill, char[][] map, char... yes)
    {
        if(map == null || map.length == 0)
            return toFill;
        if(yes == null || yes.length == 0)
            return toFill.empty();
        return toFill.refill(map, yes);
    }

    /**
     * Converts a Region to a 2D char array, where "on" will be written as the char on, and "off" in the Region will be
     * written as the char off
     * @param sample a 2D boolean array that you want converted to a 2D char array
     * @param on true in sample will be mapped to this char; usually '.'
     * @param off false in sample will be mapped to this char; usually '#'
     * @return a 2D char array containing only the chars yes and no
     */
    public static char[][] sampleToMap(Region sample, char on, char off)
    {

        if(sample == null || sample.width == 0 || sample.height == 0)
            return new char[0][0];
        return sample.toChars(on, off);
    }

    /**
     * Given a Region sample (usually a final product of this class' fill() method) and an Iterable of Coord
     * (such as a List or Set of Coord, but a Region can also work), marks every Coord in points as
     * true if it is in-bounds, and returns sample after modifications. Does not alter the original sample. You may
     * want to use this with techniques like drawing a long line with Bresenham, OrthoLine, or WobblyLine, potentially
     * widening the line with Radius.expand(), and then passing the result as the Iterable of Coord. You could then make
     * the start and end of the line into an entrance and exit and be sure the player can get from one to the other
     * (except for Bresenham and other lines that may make diagonal movements, if the player cannot move diagonally).
     * @param sample a Region; will be modified directly
     * @param points an Iterable (such as a List or Set) of Coord that will be marked as true if in-bounds
     * @return sample, after modifications
     */
    public static Region markSample(Region sample, Collection<Coord> points)
    {
        if(sample == null || sample.width == 0 || sample.height == 0)
            return sample;
        sample.addAll(points);
        return sample;
    }

    /*
    public static boolean[][] fill(boolean[][] sample, int size, double temperature, int iterations, RNG random) {
        boolean[][] field = new boolean[size][size];
        double[] weights = new double[1 << (N * N)];

        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[x].length; y++) {
                Pattern[] p = new Pattern[8];

                p[0] = new Pattern(sample, x, y, N);
                p[1] = p[0].rotate();
                p[2] = p[1].rotate();
                p[3] = p[2].rotate();
                p[4] = p[0].reflect();
                p[5] = p[1].reflect();
                p[6] = p[2].reflect();
                p[7] = p[3].reflect();

                for (int k = 0; k < 8; k++) {
                    weights[p[k].index()]++;
                }
            }
        }

        for (int k = 0; k < weights.length; k++) {
            if (weights[k] <= 0)
                weights[k] = 0.1;
        }
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                field[x][y] = random.nextBoolean();
            }
        }

        int i, j;
        double p, q;
        for (int k = 0; k < iterations * size * size; k++) {
            i = random.nextInt(size);
            j = random.nextInt(size);

            p = 1.0;
            for (int y = j - N + 1; y <= j + N - 1; y++)
                for (int x = i - N + 1; x <= i + N - 1; x++) p *= weights[Pattern.index(field, x, y, N)];

            field[i][j] = !field[i][j];

            q = 1.0;
            for (int y = j - N + 1; y <= j + N - 1; y++)
                for (int x = i - N + 1; x <= i + N - 1; x++) q *= weights[Pattern.index(field, x, y, N)];


            if (Math.pow(q / p, 1.0 / temperature) < random.nextDouble())
                field[i][j] = !field[i][j];
        }
        return field;
    }
    */
    /**
     * The main part of MimicFill; generates a Region that mimics the patterns present in the Region
     * sample, but can produce a larger or smaller output Region than the sample.
     * Takes a Region to fill with the output, a Region as a "sample" (often one of the constants in this class) and
     * several other parameters that affect how closely the output
     * will match the input. The temperature is how "agitated" or "chaotic" the changes to the map can be; 0.2f is
     * definitely recommended for map-like data but other values (between 0 and 1, both exclusive) may be better fits
     * for certain kinds of output. The iterations parameter should usually be between 3 and 5, with higher values
     * taking longer but fitting more closely; values over 5 are barely different from 5 here.
     * @param field a Region that will be modified; should be square, otherwise it will have its size increased
     * @param sample a Region to mimic visually; you can use mapToSample() if you have a 2D char array
     * @param temperature typically 0.2f works well for this, but other numbers between 0 and 1 may work
     * @param iterations typically 3-5 works well for this; lower numbers may have slight problems with quality,
     *                   and higher numbers make this slightly slower
     * @param random an EnhancedRandom to use for the random components of this technique
     * @return a new Region, width = size, height = size, mimicking the visual style of sample
     */
    public static Region fill(Region field, Region sample, double temperature, int iterations, EnhancedRandom random)
    {
        return fill(field, sample, temperature, iterations, random, 3);
    }
    /**
     * The main part of MimicFill; generates a Region that mimics the patterns present in the Region
     * sample, but can produce a larger or smaller output Region than the sample.
     * Takes a Region to fill with the output, a Region as a "sample" (often one of the constants in this class) and
     * several other parameters that affect how closely the output
     * will match the input. The temperature is how "agitated" or "chaotic" the changes to the map can be; 0.2f is
     * definitely recommended for map-like data but other values (between 0 and 1, both exclusive) may be better fits
     * for certain kinds of output. The iterations parameter should usually be between 3 and 5, with higher values
     * taking longer but fitting more closely; values over 5 are barely different from 5 here.
     * @param field a Region that will be modified; should be square, otherwise it will have its size increased
     * @param sample a Region to mimic visually; you can use mapToSample() if you have a 2D char array
     * @param temperature typically 0.2f works well for this, but other numbers between 0 and 1 may work
     * @param iterations typically 3-5 works well for this; lower numbers may have slight problems with quality,
     *                   and higher numbers make this slightly slower
     * @param random an EnhancedRandom to use for the random components of this technique
     * @param order the order of the algorithm from 2 to 5 inclusive; this determines how big each sample is (the edge
     *             length of a square sample).
     * @return a new Region, width = size, height = size, mimicking the visual style of sample
     */
    public static Region fill(Region field, Region sample, double temperature, int iterations, EnhancedRandom random, int order)
    {
        if(field == null) field = new Region(64, 64);
        if(sample == null) return field;
        int size = Math.max(field.width, field.height);
        order = Math.min(Math.max(order, 2), 5);
        float[] weights = new float[1 << (order * order)];
        for (int x = 0; x < sample.width; x++) {
            for (int y = 0; y < sample.height; y++) {
                weights[index(sample, x, y, order, false, false, false)]++;
                weights[index(sample, x, y, order, false, true, true)]++;
                weights[index(sample, x, y, order, true, true, false)]++;
                weights[index(sample, x, y, order, true, false, true)]++;
                weights[index(sample, x, y, order, true, false, false)]++;
                weights[index(sample, x, y, order, true, true, true)]++;
                weights[index(sample, x, y, order, false, true, false)]++;
                weights[index(sample, x, y, order, false, false, true)]++;
            }
        }

        for (int k = 0; k < weights.length; k++)
        {
            if (weights[k] < 0.1f)
                weights[k] = 0.1f;
        }
        field.refill(random, size, size);
        for (int k = 0; k < iterations * size * size; k++)
        {
            int x = random.nextInt(size), y = random.nextInt(size);

            double q = 1;
            for (int sy = y - order + 1; sy <= y + order - 1; sy++)
            {
                for (int sx = x - order + 1; sx <= x + order - 1; sx++)
                {
                    int ind = 0, difference = 0;
                    for (int dy = 0; dy < order; dy++)
                    {
                        for (int dx = 0; dx < order; dx++)
                        {
                            int X = sx + dx;
                            if (X < 0) X += size;
                            else if (X >= size) X -= size;

                            int Y = sy + dy;
                            if (Y < 0) Y += size;
                            else if (Y >= size) Y -= size;

                            boolean value = field.contains(X, Y);
                            int power = 1 << (dy * order + dx);
                            if(value) {
                                ind += power;
                                if (X == x && Y == y)
                                    difference = power;
                            }
                            else {
                                if (X == x && Y == y)
                                    difference = -power;
                            }
                        }
                    }

                    q *= weights[ind - difference] / weights[ind];
                }
            }

            if (q >= 1) {
                field.toggle(x, y);
                continue;
            }
            if (temperature != 1) {
                q = Math.pow(q, 1.0 / temperature);
            }
            if(q > random.nextDouble()){
                field.toggle(x, y);
            }
        }

        return field;
    }

    private static int index(Region field, int x, int y, int order, boolean flipX, boolean flipY, boolean swap) {
        int result = 0;
        int width = field.width, height = field.height;
        if(swap)
        {
            for (int i = 0; i < order; i++) {
                for (int j = 0; j < order; j++) {
                    if(field.contains((height * 2 + (flipY ? -(y + j) : (y + j))) % height,
                            (width * 2 + (flipX ? -(x + i) : (x + i))) % width))
                        result += 1 << (j * order + i);
                }
            }
        }
        else
        {
            for (int i = 0; i < order; i++) {
                for (int j = 0; j < order; j++) {
                    if(field.contains((width * 2 + (flipX ? -(x + i) : (x + i))) % width,
                            (height * 2 + (flipY ? -(y + j) : (y + j))) % height))
                        result += 1 << (j * order + i);
                }
            }
        }
        return result;
    }


    /**
     * Predefined sample; many small separate squares.
     */
    public static final Region boulders = new Region(new char[][]{
            "...........##...".toCharArray(),
            "..##..##...##...".toCharArray(),
            "..##..##........".toCharArray(),
            ".........##..##.".toCharArray(),
            "...##.##.##..##.".toCharArray(),
            "...##.##........".toCharArray(),
            "##..............".toCharArray(),
            "##..##.##.......".toCharArray(),
            "....##.##...##..".toCharArray(),
            ".##.........##..".toCharArray(),
            ".##.##..........".toCharArray(),
            "....##...##.....".toCharArray(),
            ".........##.##..".toCharArray(),
            ".##...##....##..".toCharArray(),
            ".##...##........".toCharArray(),
            "................".toCharArray()
    }, '.');

    /**
     * Predefined sample; a large, enclosed, organic space that usually makes large cave-like rooms.
     */
    public static final Region cave = new Region(new char[][]{
            "################".toCharArray(),
            "####.####.######".toCharArray(),
            "###...##......##".toCharArray(),
            "##..........####".toCharArray(),
            "##............##".toCharArray(),
            "###............#".toCharArray(),
            "###..........###".toCharArray(),
            "####........####".toCharArray(),
            "####.........###".toCharArray(),
            "###...........##".toCharArray(),
            "##............##".toCharArray(),
            "##.............#".toCharArray(),
            "###.....##.....#".toCharArray(),
            "####...####..###".toCharArray(),
            "################".toCharArray(),
            "################".toCharArray()
    }, '.');

    /**
     * Predefined sample; several medium-sized organic spaces that usually make tight, chaotic tunnels.
     */
    public static final Region caves = new Region(new char[][]{
            "###.########.###".toCharArray(),
            "###.#######..###".toCharArray(),
            "##...######..###".toCharArray(),
            "#.....####....##".toCharArray(),
            "#..............#".toCharArray(),
            "........#.......".toCharArray(),
            "##.....###....##".toCharArray(),
            "###...#####..###".toCharArray(),
            "####..#####.####".toCharArray(),
            "####.#####...###".toCharArray(),
            "##....###.....##".toCharArray(),
            "#..............#".toCharArray(),
            ".......##.......".toCharArray(),
            "#.....###.....##".toCharArray(),
            "##....####....##".toCharArray(),
            "###.########.###".toCharArray()
    }, '.');

    /**
     * Predefined sample; a checkerboard pattern that typically loses recognition as such after generation.
     */
    public static final Region chess = new Region(new char[][]{
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray(),
            ".#.#.#.#.#.#.#.#".toCharArray(),
            "#.#.#.#.#.#.#.#.".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces rectangular rooms with small corridors between them.
     */
    public static final Region lessRooms = new Region(new char[][]{
            "###.########.###".toCharArray(),
            "###.#####......#".toCharArray(),
            "###.#####......#".toCharArray(),
            "###.#####......#".toCharArray(),
            "##.............#".toCharArray(),
            "...######.......".toCharArray(),
            "##.######......#".toCharArray(),
            "##.######......#".toCharArray(),
            "##.########.####".toCharArray(),
            "##.########.####".toCharArray(),
            "##......###.####".toCharArray(),
            "........###.....".toCharArray(),
            "##...........###".toCharArray(),
            "##......####.###".toCharArray(),
            "##......####.###".toCharArray(),
            "###.########.###".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces a suitable filler for a maze (but it is unlikely to connect both ends like a maze).
     */
    public static final Region maze = new Region(new char[][]{
            ".....####...#..#".toCharArray(),
            ".###.#..#.######".toCharArray(),
            ".#...#.####...#.".toCharArray(),
            ".#####.#....#.##".toCharArray(),
            "...#...####.###.".toCharArray(),
            "######....#.#.##".toCharArray(),
            ".....#..#.#.#.#.".toCharArray(),
            "######..#...#.#.".toCharArray(),
            "........#####.##".toCharArray(),
            "###.###..#....#.".toCharArray(),
            "..#.#...########".toCharArray(),
            ".##.#.#.#..#.#..".toCharArray(),
            "....###.##.#.##.".toCharArray(),
            "##..#......#..##".toCharArray(),
            ".########..##..#".toCharArray(),
            "........#...#..#".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces weird, large areas of "on" and "off" that suddenly change to the other.
     */
    public static final Region quarterBlack = new Region(new char[][]{
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray(),
            "####............".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces multiple directions of flowing, river-like shapes made of "off".
     */
    public static final Region river = new Region(new char[][]{
            "......###.......".toCharArray(),
            "......###.......".toCharArray(),
            ".......###......".toCharArray(),
            ".......###......".toCharArray(),
            ".......###......".toCharArray(),
            "........###.....".toCharArray(),
            "........###.....".toCharArray(),
            "........###.....".toCharArray(),
            ".......###......".toCharArray(),
            ".......###......".toCharArray(),
            "......###.......".toCharArray(),
            "......###.......".toCharArray(),
            ".....###........".toCharArray(),
            ".....###........".toCharArray(),
            "......###.......".toCharArray(),
            "......###.......".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces rectangular rooms with a dense packing.
     */
    public static final Region rooms = new Region(new char[][]{
            "###.########.###".toCharArray(),
            "###.#####......#".toCharArray(),
            "#.....###......#".toCharArray(),
            "#.....###......#".toCharArray(),
            "#..............#".toCharArray(),
            "......###.......".toCharArray(),
            "#.....###......#".toCharArray(),
            "##.######......#".toCharArray(),
            "##.########.####".toCharArray(),
            "##.########.####".toCharArray(),
            "##......##....##".toCharArray(),
            "........##......".toCharArray(),
            "##............##".toCharArray(),
            "##......##....##".toCharArray(),
            "##......####.###".toCharArray(),
            "###.########.###".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces an uncanny imitation of a maze with a tiny sample size.
     */
    public static final Region simpleMaze = new Region(new char[][]{
            "....".toCharArray(),
            ".###".toCharArray(),
            ".#.#".toCharArray(),
            ".###".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces mostly rectangular rooms with very few corridor-like areas.
     */
    public static final Region simpleRooms = new Region(new char[][]{
            "#####.#####".toCharArray(),
            "#####.#####".toCharArray(),
            "##.......##".toCharArray(),
            "##.......##".toCharArray(),
            "##.......##".toCharArray(),
            "...........".toCharArray(),
            "##.......##".toCharArray(),
            "##.......##".toCharArray(),
            "##.......##".toCharArray(),
            "#####.#####".toCharArray(),
            "#####.#####".toCharArray()
    }, '.');

    /**
     * Predefined sample; produces largely rectangular rooms with a good amount of thin corridors.
     */
    public static final Region thickWalls = new Region(new char[][]{
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray(),
            "####.......####".toCharArray(),
            "####.......####".toCharArray(),
            "####.......####".toCharArray(),
            "...............".toCharArray(),
            "####.......####".toCharArray(),
            "####.......####".toCharArray(),
            "####.......####".toCharArray(),
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray(),
            "#######.#######".toCharArray()
    }, '.');

    public static final Region ruins = new Region(new char[][]{
            "#.#####..........#########......".toCharArray(),
            "#.#####..######.......########..".toCharArray(),
            "#.######.######..###............".toCharArray(),
            "#.....##.######......#######.##.".toCharArray(),
            "#......#.######.###.####.....##.".toCharArray(),
            "#...............###.###......##.".toCharArray(),
            "......####..........####........".toCharArray(),
            "#.##.......##.......####....####".toCharArray(),
            "#.########..#......#######..####".toCharArray(),
            "#.########........########..####".toCharArray(),
            "...###........##.#########......".toCharArray(),
            "...####.......############..####".toCharArray(),
            "...####.......####..#####...####".toCharArray(),
            "...####......######.#######.##..".toCharArray(),
            "#######....#.##.###.#######.....".toCharArray(),
            "#########.##.##.###.######..####".toCharArray(),
            "#########.#..##.###.........####".toCharArray(),
            "####.####.##.##.###...#.....####".toCharArray(),
            "###..####.##.##..###..#.##.#####".toCharArray(),
            "###..####.##.##..####.#.##.#####".toCharArray(),
            "####.##.#.##.###.####.#.##.###.#".toCharArray(),
            "#.##.......#.###.####.#....###.#".toCharArray(),
            ".......##.##..........########..".toCharArray(),
            "........#.##.###......#####.....".toCharArray(),
            "######.##.##.####.##..##########".toCharArray(),
            "######.##.....######..####...###".toCharArray(),
            "######.####...######..####...###".toCharArray(),
            "........###...######...###......".toCharArray(),
            "######.......####...........##.#".toCharArray(),
            "###.......########.#######...###".toCharArray(),
            "#######..#########.#######...###".toCharArray(),
            "#.#####..######....#####........".toCharArray(),
    }, '.');

    public static final Region openRooms = new Region(new char[][]{
            "......#......#......##..........".toCharArray(),
            "......#.........................".toCharArray(),
            "....###...............####......".toCharArray(),
            "....###...##..........#..#...#..".toCharArray(),
            "..#####....#.....######..#...#..".toCharArray(),
            ".....####..########......#...###".toCharArray(),
            ".....####..####..........#....##".toCharArray(),
            ".....#........#.......######..##".toCharArray(),
            ".....#........###.....#....#####".toCharArray(),
            ".....#..##....###.....#....#####".toCharArray(),
            ".....#..#####.........#....#####".toCharArray(),
            ".....#..#####.........#....#####".toCharArray(),
            "######..#...###.......#.......##".toCharArray(),
            "........#...####......#.........".toCharArray(),
            "......###......#####..#.........".toCharArray(),
            "#.....###......#####..#######..#".toCharArray(),
            "#..#####.......#..###########..#".toCharArray(),
            "#..##.............#####........#".toCharArray(),
            "#..##.............#...#........#".toCharArray(),
            "#####.......#.....#...#........#".toCharArray(),
            "............#.....#####........#".toCharArray(),
            "............#..................#".toCharArray(),
            ".....########..................#".toCharArray(),
            "........###########......###....".toCharArray(),
            ".........####.....#......###....".toCharArray(),
            ".........####.....#...######....".toCharArray(),
            ".........###......#...#....#....".toCharArray(),
            ".......###........#...#....###..".toCharArray(),
            ".#########........#####....###..".toCharArray(),
            ".##...............############..".toCharArray(),
            ".##.................##..........".toCharArray(),
            ".######......#####..##..........".toCharArray(),
    }, '.');

    public static final Region[] samples = new Region[]{
            boulders, cave, caves, chess, lessRooms, maze, quarterBlack,
            river, rooms, simpleMaze, simpleRooms, thickWalls, ruins, openRooms
    };
}
