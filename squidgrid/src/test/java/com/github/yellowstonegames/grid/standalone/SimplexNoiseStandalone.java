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
package com.github.yellowstonegames.grid.standalone;

/**
 * Simplex noise code that depends only on libGDX.
 * <br>
 * This is a smooth type of noise that works well if you can request multiple octaves from it.
 * You can call {@link #noiseWithOctaves(double, double, long, int, double)} with 2 or more octaves to get higher
 * levels of detail.
 * <br>
 * This is a drop-in replacement for the SimplexNoise.java file written by Stefan Gustavson in 2012.
 * Unlike that code, this allows a seed to be specified per-call because this uses hashing rather than a perm table.
 */
public class SimplexNoiseStandalone {

    /**
     * Use the same seed for any noise that should be continuous (smoothly) across calls to nearby points.
     */
    protected long seed;
    /**
     * You will probably need to change the frequency to be much closer or much further from 0, depending on the scale
     * of your noise.
     */
    protected double frequency = 1.0;

    public static final SimplexNoiseStandalone instance = new SimplexNoiseStandalone();
    protected static final double[] GRADIENTS_2D = new double[512];

    public SimplexNoiseStandalone() {
        this(1234567890L, 1.0);
    }

    public SimplexNoiseStandalone(long seed, double frequency)
    {
        this.seed = seed;
        this.frequency = frequency;
        double angle = Math.E; // arbitrary start, will be incremented by 1/phi, where phi is the golden ratio.
        for (int i = 0; i < 512; i += 2, angle += 0.6180339887498949) {
            GRADIENTS_2D[i] = Math.cos(angle);
            GRADIENTS_2D[i+1] = Math.sin(angle);
        }

    }
    /**
     * Gets an 8-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y point with the given state
     */
    public static int hash256(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }

    protected static double gradCoord2D(long seed, int x, int y, double xd, double yd) {
        final int h = hash256(x, y, seed) << 1;
        return xd * GRADIENTS_2D[h] + yd * GRADIENTS_2D[h+1];
    }

    /**
     * Gets noise with the lowest, fastest level of detail. Uses
     * {@link #getSeed()} and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @return noise between -1 and 1, inclusive
     */
    public double getNoise(final double x, final double y) {
        return noiseWithSeed(x * frequency, y * frequency, seed);
    }

    /**
     * 2D simplex noise with the lowest, fastest level of detail. Uses the
     * seed {@code 12345L} and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public static double noise(final double x, final double y) {
        return noiseWithSeed(x, y, 12345L);
    }

    public static final double F2 = 0.36602540378443864676372317075294;
    public static final double G2 = 0.21132486540518711774542560974902;
    public static final double H2 = G2 * 2.0;
    public static final double LIMIT2 = 0.5;

    /**
     * Gets noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(final double x, final double y, final long seed) {
        double t = (x + y) * F2;
        // casting Math.floor used to be slow on older JVMs; it shouldn't be since Java 16 or so.
        int i = (int)Math.floor(x + t);
        int j = (int)Math.floor(y + t);

        t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;

        double x0 = x - X0;
        double y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1 + H2;
        double y2 = y0 - 1 + H2;

        double n0, n1, n2;

        n0 = LIMIT2 - x0 * x0 - y0 * y0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord2D(seed, i, j, x0, y0);
        }
        else n0 = 0.0;

        n1 = LIMIT2 - x1 * x1 - y1 * y1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }
        else n1 = 0.0;

        n2 = LIMIT2 - x2 * x2 - y2 * y2;
        if (n2 > 0)  {
            n2 *= n2;
            n2 *= n2 * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }
        else n2 = 0.0;

        return (n0 + n1 + n2) * 99.20689070704672; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * {@link #getSeed()} and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public double getNoiseWithOctaves(double x, double y, int octaves) {
        return noiseWithOctaves(x, y, seed, octaves, 1.0);
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * the given seed, and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, long seed, int octaves) {
        return noiseWithOctaves(x, y, seed, octaves, 1.0);
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
     * seed instead of {@link #getSeed()}, and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, long seed, int octaves, double frequency) {
        x *= frequency;
        y *= frequency;
        double sum = noiseWithSeed(x, y, seed);
        double amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2;
            y *= 2;

            amp *= 0.5;
            sum += noiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "SimplexNoiseStandalone{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplexNoiseStandalone that = (SimplexNoiseStandalone) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }
}
