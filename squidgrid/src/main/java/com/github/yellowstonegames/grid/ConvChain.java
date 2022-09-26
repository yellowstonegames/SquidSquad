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
public class ConvChain {

    /**
     * The order of the algorithm; this determines how big each sample is (the edge length of a square sample).
     */
    private static final int N = 3;

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
        if(field == null) field = new Region(64, 64);
        if(sample == null) return field;
        int size = Math.max(field.width, field.height);
        float[] weights = new float[1 << (N * N)];
        for (int x = 0; x < sample.width; x++) {
            for (int y = 0; y < sample.height; y++) {
                weights[Pattern.index(sample, x, y, N, false, false, false)]++;
                weights[Pattern.index(sample, x, y, N, false, true, true)]++;
                weights[Pattern.index(sample, x, y, N, true, true, false)]++;
                weights[Pattern.index(sample, x, y, N, true, false, true)]++;
                weights[Pattern.index(sample, x, y, N, true, false, false)]++;
                weights[Pattern.index(sample, x, y, N, true, true, true)]++;
                weights[Pattern.index(sample, x, y, N, false, true, false)]++;
                weights[Pattern.index(sample, x, y, N, false, false, true)]++;
            }
        }

        for (int k = 0; k < weights.length; k++)
        {
            if (weights[k] <= 0)
                weights[k] = 0.1f;
        }
        field.refill(random, size, size);
        for (int k = 0; k < iterations * size * size; k++)
        {
            int x = random.nextInt(size), y = random.nextInt(size);

            double q = 1;
            for (int sy = y - N + 1; sy <= y + N - 1; sy++)
            {
                for (int sx = x - N + 1; sx <= x + N - 1; sx++)
                {
                    int ind = 0, difference = 0;
                    for (int dy = 0; dy < N; dy++)
                    {
                        for (int dx = 0; dx < N; dx++)
                        {
                            int X = sx + dx;
                            if (X < 0) X += size;
                            else if (X >= size) X -= size;

                            int Y = sy + dy;
                            if (Y < 0) Y += size;
                            else if (Y >= size) Y -= size;

                            boolean value = field.contains(X, Y);
                            int power = 1 << (dy * N + dx);
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

    private static class Pattern {
        public boolean[][] data;


        Pattern(boolean[][] exact) {
            data = exact;
        }

        Pattern(boolean[][] field, int x, int y, int size) {
            data = new boolean[size][size];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    data[i][j] =
                            field[(x + i + field.length) % field.length][(y + j + field[0].length) % field[0].length];
                }
            }
        }

        Pattern rotate() {
            boolean[][] next = new boolean[data.length][data.length];
            for (int x = 0; x < data.length; x++) {
                for (int y = 0; y < data.length; y++) {
                    next[data.length - 1 - y][x] = data[x][y];
                }
            }
            return new Pattern(next);
        }

        Pattern reflect() {
            boolean[][] next = new boolean[data.length][data.length];
            for (int x = 0; x < data.length; x++) {
				System.arraycopy(data[x], 0, next[data.length - 1 - x], 0, data.length);
            }
            return new Pattern(next);
        }

        int index() {
            int result = 0, power = 1;
            for (int y = 0; y < data.length; y++) {
                for (int x = 0; x < data.length; x++) {
                    result += data[data.length - 1 - x][data.length - 1 - y] ? power : 0;
                    power <<= 1;
                }
            }
            return result;
        }

        static int index(boolean[][] field, int x, int y, int size) {
            int result = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (field[(x + i + field.length) % field.length][(y + j + field[0].length) % field[0].length])
                        result += 1 << (j * size + i);
                }
            }
            return result;
        }
        
        static int index(boolean[][] field, int x, int y, int size, boolean flipX, boolean flipY, boolean swap) {
            int result = 0;
            int width = field.length, height = field[0].length;
            if(swap)
            {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if(field[(height * 2 + (flipY ? -(y + j) : (y + j))) % height]
                                [(width * 2 + (flipX ? -(x + i) : (x + i))) % width])
                            result += 1 << (j * size + i);
                    }
                }
            }
            else 
            {
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        if(field[(width * 2 + (flipX ? -(x + i) : (x + i))) % width]
                                [(height * 2 + (flipY ? -(y + j) : (y + j))) % height])
                            result += 1 << (j * size + i);
                    }
                }
            }
            return result;
        }
    }

    /**
     * Predefined sample; many small separate squares.
     */
    public static final boolean[][] boulders = new boolean[][]{
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,false ,false ,true  ,true  ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,false ,false ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,false ,false ,true  },
            new boolean[]{true  ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,false ,false ,true  },
            new boolean[]{true  ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  }
    };

    /**
     * Predefined sample; a large, enclosed, organic space that usually makes large cave-like rooms,
     */
    public static final boolean[][] cave = new boolean[][]{
            new boolean[]{false , false , false , false , false , false , false , false , false , false , false , false , false , false , false , false },
            new boolean[]{false , false , false , false , true  , false , false , false , false , true  , false , false , false , false , false , false },
            new boolean[]{false , false , false , true  , true  , true  , false , false , true  , true  , true  , true  , true  , true  , false , false },
            new boolean[]{false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false , false , false },
            new boolean[]{false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false },
            new boolean[]{false , false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false },
            new boolean[]{false , false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false , false },
            new boolean[]{false , false , false , false , true  , true  , true  , true  , true  , true  , true  , true  , false , false , false , false },
            new boolean[]{false , false , false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false , false },
            new boolean[]{false , false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false },
            new boolean[]{false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false , false },
            new boolean[]{false , false , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , true  , false },
            new boolean[]{false , false , false , true  , true  , true  , true  , true  , false , false , true  , true  , true  , true  , true  , false },
            new boolean[]{false , false , false , false , true  , true  , true  , false , false , false , false , true  , true  , false , false , false },
            new boolean[]{false , false , false , false , false , false , false , false , false , false , false , false , false , false , false , false },
            new boolean[]{false , false , false , false , false , false , false , false , false , false , false , false , false , false , false , false }
    };

    /**
     * Predefined sample; several medium-sized organic spaces that usually make tight, chaotic tunnels.
     */
    public static final boolean[][] caves = new boolean[][]{
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false }
    };

    /**
     * Predefined sample; a checkerboard pattern that typically loses recognition as such after generation.
     */
    public static final boolean[][] chess = new boolean[][]{
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  }
    };

    /**
     * Predefined sample; produces rectangular rooms with small corridors between them.
     */
    public static final boolean[][] lessRooms = new boolean[][]{
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false }
    };

    /**
     * Predefined sample; produces a suitable filler for a maze (but it is unlikely to connect both ends like a maze).
     */
    public static final boolean[][] maze = new boolean[][]{
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,false ,true  ,true  ,false },
            new boolean[]{true  ,false ,false ,false ,true  ,false ,true  ,true  ,false ,true  ,false ,false ,false ,false ,false ,false },
            new boolean[]{true  ,false ,true  ,true  ,true  ,false ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,false ,true  },
            new boolean[]{true  ,false ,false ,false ,false ,false ,true  ,false ,true  ,true  ,true  ,true  ,false ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,false ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,true  ,false ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,true  ,false ,true  ,false ,true  ,false ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,false ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,true  },
            new boolean[]{true  ,true  ,false ,true  ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{true  ,false ,false ,true  ,false ,true  ,false ,true  ,false ,true  ,true  ,false ,true  ,false ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,false ,false ,false ,true  ,false ,false ,true  ,false ,true  ,false ,false ,true  },
            new boolean[]{false ,false ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,false },
            new boolean[]{true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  ,false }
    };

    /**
     * Predefined sample; produces weird, large areas of "true" and "false" that suddenly change to the other.
     */
    public static final boolean[][] quarterBlack = new boolean[][]{
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  }
    };

    /**
     * Predefined sample; produces multiple directions of flowing, river-like shapes made of "false".
     */
    public static final boolean[][] river = new boolean[][]{
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  }
    };

    /**
     * Predefined sample; produces rectangular rooms with a dense packing.
     */
    public static final boolean[][] rooms = new boolean[][]{
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false }
    };

    /**
     * Predefined sample; produces an uncanny imitation of a maze with a tiny sample size.
     */
    public static final boolean[][] simpleMaze = new boolean[][]{
            new boolean[]{true  ,true  ,true  ,true  },
            new boolean[]{true  ,false ,false ,false },
            new boolean[]{true  ,false ,true  ,false },
            new boolean[]{true  ,false ,false ,false }
    };

    /**
     * Predefined sample; produces mostly rectangular rooms with very few corridor-like areas.
     */
    public static final boolean[][] simpleRooms = new boolean[][]{
            new boolean[]{false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false }
    };

    /**
     * Predefined sample; produces largely rectangular rooms with a good amount of thin corridors.
     */
    public static final boolean[][] thickWalls = new boolean[][]{
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false }
    };

    public static final boolean[][] ruins = new boolean[][]{
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  },
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,true  ,true  ,false ,false ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,false ,false ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,false ,false ,false ,true  ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,true  ,false ,false ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,false ,false ,false ,false ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,false },
            new boolean[]{false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,false ,false ,false ,true  ,false ,false ,false ,false ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,false ,false ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,false ,false ,true  ,false ,false ,true  ,false ,false ,false ,false ,true  ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,false },
            new boolean[]{false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{false ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
    };

    public static final boolean[][] openRooms = new boolean[][]{
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  },
            new boolean[]{true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false },
            new boolean[]{false ,false ,false ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,true  ,true  ,true  ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,false ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,false ,true  ,true  },
            new boolean[]{true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
            new boolean[]{true  ,false ,false ,false ,false ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,false ,false ,false ,false ,false ,true  ,true  ,false ,false ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  ,true  },
    };

    public static final boolean[][][] samples = new boolean[][][]{
            boulders, cave, caves, chess, lessRooms, maze, quarterBlack,
            river, rooms, simpleMaze, simpleRooms, thickWalls, ruins, openRooms
    };

    /*
    // this would throw an ArrayIndexOutOfBoundsException before a fix added after beta 8
    public static void main(String[] args) {
        boolean[][] binarySample = new boolean[20][20];
        int length = 100;
        boolean[][] binaryOut = MimicFill.fill(binarySample, length, .01, 5, new RNG());
        squidpony.squidgrid.mapping.DungeonUtility.debugPrint(
                squidpony.squidgrid.mapping.DungeonUtility.hashesToLines(sampleToMap(binaryOut, '.', '#')));
    }
    */
}
