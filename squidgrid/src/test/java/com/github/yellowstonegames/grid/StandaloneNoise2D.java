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
package com.github.yellowstonegames.grid;

/**
 * Simplex noise function in 2D only. This is a smooth type of noise that works well if you can request multiple octaves
 * from it. You can call {@link #getNoiseWithOctaves(float, float, long, int)} with 2 or more octaves to get higher
 * levels of detail.
 */
public class StandaloneNoise2D {

    /**
     * Use the same seed for any noise that should be continuous (smoothly) across calls to nearby points.
     */
    public long seed;
    /**
     * You will probably need to change the frequency to be much closer or much further from 0, depending on the scale
     * of your noise.
     */
    public float frequency = 1f;

    public static final StandaloneNoise2D instance = new StandaloneNoise2D();
    public static final float[] GRADIENTS_2D = new float[512];

    public StandaloneNoise2D() {
        this(1234567890L, 1f);
    }

    public StandaloneNoise2D(long seed, float frequency)
    {
        this.seed = seed;
        this.frequency = frequency;
        double angle = Math.E; // arbitrary start, will be incremented by 1/phi, where phi is the golden ratio.
        for (int i = 0; i < 512; i += 2, angle += 0.6180339887498949) {
            GRADIENTS_2D[i] = (float) Math.cos(angle);
            GRADIENTS_2D[i+1] = (float) Math.sin(angle);
        }

    }
    private static final int BIG_ENOUGH_INT = 16384;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns an int.
     * Doesn't consider "weird floats" like INFINITY and NaN. This method will only properly floor
     * floats from {@code -16384} to {@code Integer.MAX_VALUE - 16384}, or {@code 2147467263}.
     * <br>
     * Taken from libGDX MathUtils.
     *
     * @param t a float from -16384 to 2147467263 (both inclusive)
     * @return the floor of t, as an int
     */
    public static int fastFloor(final float t) {
        return ((int) (t + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT);
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

    protected static float gradCoord2D(long seed, int x, int y, float xd, float yd) {
        final int h = hash256(x, y, seed) << 1;
        return xd * GRADIENTS_2D[h] + yd * GRADIENTS_2D[h+1];
    }

    /**
     * Gets noise with the lowest, fastest level of detail. Uses the
     * default seed and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @return noise between -1 and 1
     */
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    public static final float F2 = 0.36602540378443864676372317075294f;
    public static final float G2 = 0.21132486540518711774542560974902f;
    public static final float H2 = G2 * 2.0f;
    public static final float LIMIT2 = 0.5f;

    /**
     * Gets noise with the lowest, fastest level of detail. Ignores the
     * default seed (this uses the given seed) and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise, ignoring the default seed
     * @return noise between -1 and 1
     */
    public static float getNoiseWithSeed(final float x, final float y, final long seed) {
        float t = (x + y) * F2;
        int i = fastFloor(x + t);
        int j = fastFloor(y + t);

        t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1 + H2;
        float y2 = y0 - 1 + H2;

        float n0, n1, n2;

        n0 = LIMIT2 - x0 * x0 - y0 * y0;
        if (n0 > 0) {
            n0 *= n0;
            n0 *= n0 * gradCoord2D(seed, i, j, x0, y0);
        }
        else n0 = 0.0f;

        n1 = LIMIT2 - x1 * x1 - y1 * y1;
        if (n1 > 0) {
            n1 *= n1;
            n1 *= n1 * gradCoord2D(seed, i + i1, j + j1, x1, y1);
        }
        else n1 = 0.0f;

        n2 = LIMIT2 - x2 * x2 - y2 * y2;
        if (n2 > 0)  {
            n2 *= n2;
            n2 *= n2 * gradCoord2D(seed, i + 1, j + 1, x2, y2);
        }
        else n2 = 0.0f;

        return (n0 + n1 + n2) * 99.20689070704672f; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the
     * default seed and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    protected float getNoiseWithOctaves(float x, float y, int octaves) {
        x *= frequency;
        y *= frequency;
        float sum = getNoiseWithSeed(x, y, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += getNoiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Ignores the
     * default seed (this uses the given seed) and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise, ignoring the default seed
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    protected float getNoiseWithOctaves(float x, float y, long seed, int octaves) {
        x *= frequency;
        y *= frequency;
        float sum = getNoiseWithSeed(x, y, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += getNoiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "StandaloneNoise2D{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StandaloneNoise2D that = (StandaloneNoise2D) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }
}
