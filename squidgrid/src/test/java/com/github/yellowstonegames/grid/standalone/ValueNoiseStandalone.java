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
package com.github.yellowstonegames.grid.standalone;

import com.badlogic.gdx.utils.NumberUtils;

/**
 * Value noise code that depends only on libGDX.
 * <br>
 * This is a somewhat-smooth type of noise that mostly works well when you request multiple octaves from it.
 * You can call {@link #noiseWithOctaves(double, double, long, int, double)} with 2 or more octaves to get higher
 * levels of detail. Below about 3 or 4 octaves, the quality will be poor and this will look artificial.
 * <br>
 * This is a drop-in replacement for the SimplexNoise.java file written by Stefan Gustavson in 2012.
 * It (obviously) uses value noise instead of simplex noise, but otherwise follows the same ideas as my other
 * standalone noise class(es), which could be distributed in the same folder.
 */
public class ValueNoiseStandalone {

    /**
     * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
     */
    protected long seed;
    /**
     * You will probably need to change the frequency to be much closer or much further from 0, depending on the scale
     * of your noise.
     */
    protected double frequency = 1.0;

    public static final ValueNoiseStandalone instance = new ValueNoiseStandalone();

    public ValueNoiseStandalone() {
        this(1234567890L, 1.0);
    }

    public ValueNoiseStandalone(long seed, double frequency)
    {
        this.seed = seed;
        this.frequency = frequency;
    }

    // 2D SECTION

    /**
     * Gets 2D value noise with the lowest, fastest level of detail. Uses
     * {@link #getSeed()} and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @return noise between -1 and 1, inclusive
     */
    public double getNoise(final double x, final double y) {
        return noiseWithSeed(x * frequency, y * frequency, seed);
    }

    /**
     * 2D value noise with the lowest, fastest level of detail. Uses the
     * seed {@code 12345L} and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public static double noise(final double x, final double y) {
        return noiseWithSeed(x, y, 12345L);
    }

    /**
     * Gets 2D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(double x, double y, final long seed) {
        final long STEPX = 0xC13FA9A902A6328FL;
        final long STEPY = 0x91E10DA5C79E7B1DL;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        return ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, seed))
                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, seed)))
                * 0x1p-63;
    }

    /**
     * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
     * (2 to the 64).
     * @param x should be premultiplied by 0xC13FA9A902A6328FL
     * @param y should be premultiplied by 0x91E10DA5C79E7B1DL
     * @param s state, any long
     * @return a mediocre 64-bit hash
     */
    private static long hashPart(final long x, final long y, long s) {
        s ^= x ^ y;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets 2D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets 2D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets 2D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
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
            x += x;
            y += y;

            amp *= 0.5;
            sum += noiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    // 3D SECTION

    /**
     * Gets 3D value noise with the lowest, fastest level of detail. Uses
     * {@link #getSeed()} and multiplies x, y, and z by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @return noise between -1 and 1, inclusive
     */
    public double getNoise(final double x, final double y, final double z) {
        return noiseWithSeed(x * frequency, y * frequency, z * frequency, seed);
    }

    /**
     * 3D value noise with the lowest, fastest level of detail. Uses the
     * seed {@code 12345L} and does not change x, y, or z.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return noise between -1 and 1, inclusive
     */
    public static double noise(final double x, final double y, final double z) {
        return noiseWithSeed(x, y, z, 12345L);
    }

    /**
     * Gets 3D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, or z.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(double x, double y, double z, final long seed) {
        final long STEPX = 0xD1B54A32D192ED03L;
        final long STEPY = 0xABC98388FB8FAC03L;
        final long STEPZ = 0x8CB92BA72F3D8DD7L;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        long zFloor = (long)Math.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, seed)))
                + z *
                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, seed))
                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed)))
        ) * 0x1p-63;
    }

    /**
     * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
     * (2 to the 64).
     * @param x should be premultiplied by 0xD1B54A32D192ED03L
     * @param y should be premultiplied by 0xABC98388FB8FAC03L
     * @param z should be premultiplied by 0x8CB92BA72F3D8DD7L
     * @param s state, any long
     * @return a mediocre 64-bit hash
     */
    private static long hashPart(final long x, final long y, final long z, long s) {
        s ^= x ^ y ^ z;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets 3D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * {@link #getSeed()} and multiplies x, y, and z by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public double getNoiseWithOctaves(double x, double y, double z, int octaves) {
        return noiseWithOctaves(x, y, z, seed, octaves, 1.0);
    }

    /**
     * Gets 3D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * the given seed, and multiplies x, y, and z by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, double z, long seed, int octaves) {
        return noiseWithOctaves(x, y, z, seed, octaves, 1.0);
    }

    /**
     * Gets 3D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
     * seed instead of {@link #getSeed()}, and multiplies x, y, and z by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, double z, long seed, int octaves, double frequency) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        double sum = noiseWithSeed(x, y, z, seed);
        double amp = 1;

        for (int i = 1; i < octaves; i++) {
            x += x;
            y += y;
            z += z;

            amp *= 0.5;
            sum += noiseWithSeed(x, y, z, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    // 4D SECTION

    /**
     * Gets 4D value noise with the lowest, fastest level of detail. Uses
     * {@link #getSeed()} and multiplies x, y, z, and w by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param w w coordinate, will be adjusted by frequency
     * @return noise between -1 and 1, inclusive
     */
    public double getNoise(final double x, final double y, final double z, final double w) {
        return noiseWithSeed(x * frequency, y * frequency, z * frequency, w * frequency, seed);
    }

    /**
     * 4D value noise with the lowest, fastest level of detail. Uses the
     * seed {@code 12345L} and does not change x, y, z, or w.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @return noise between -1 and 1, inclusive
     */
    public static double noise(final double x, final double y, final double z, final double w) {
        return noiseWithSeed(x, y, z, w, 12345L);
    }

    /**
     * Gets 4D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, z, or w.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(double x, double y, double z, double w, final long seed) {
        final long STEPX = 0xDB4F0B9175AE2165L;
        final long STEPY = 0xBBE0563303A4615FL;
        final long STEPZ = 0xA0F2EC75A1FE1575L;
        final long STEPW = 0x89E182857D9ED689L;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        long zFloor = (long)Math.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        long wFloor = (long)Math.floor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, seed))
                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed)))
                ))) * 0x1p-63;
    }

    /**
     * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
     * (2 to the 64).
     * @param x should be premultiplied by 0xDB4F0B9175AE2165L
     * @param y should be premultiplied by 0xBBE0563303A4615FL
     * @param z should be premultiplied by 0xA0F2EC75A1FE1575L
     * @param w should be premultiplied by 0x89E182857D9ED689L
     * @param s state, any long
     * @return a mediocre 64-bit hash
     */
    private static long hashPart(final long x, final long y, final long z, final long w, long s) {
        s ^= x ^ y ^ z ^ w;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * Gets 4D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * {@link #getSeed()} and multiplies x, y, z, and w by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param w w coordinate, will be adjusted by frequency
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public double getNoiseWithOctaves(double x, double y, double z, double w, int octaves) {
        return noiseWithOctaves(x, y, z, w, seed, octaves, 1.0);
    }

    /**
     * Gets 4D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
     * the given seed, and multiplies x, y, z, and w by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param w w coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, double z, double w, long seed, int octaves) {
        return noiseWithOctaves(x, y, z, w, seed, octaves, 1.0);
    }

    /**
     * Gets 4D value noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
     * seed instead of {@link #getSeed()}, and multiplies x, y, z, and w by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @param z z coordinate, will be adjusted by frequency
     * @param w w coordinate, will be adjusted by frequency
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @param octaves level of detail, from 1 to about 16 as a practical maximum
     * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
     * @return noise between -1 and 1
     */
    public static double noiseWithOctaves(double x, double y, double z, double w, long seed, int octaves, double frequency) {
        x *= frequency;
        y *= frequency;
        z *= frequency;
        w *= frequency;
        double sum = noiseWithSeed(x, y, z, w, seed);
        double amp = 1;

        for (int i = 1; i < octaves; i++) {
            x += x;
            y += y;
            z += z;
            w += w;

            amp *= 0.5;
            sum += noiseWithSeed(x, y, z, w, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    // OTHER

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
        return "ValueNoiseStandalone{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueNoiseStandalone that = (ValueNoiseStandalone) o;

        if (seed != that.seed) return false;
        return Double.compare(that.frequency, frequency) == 0;
    }

    @Override
    public int hashCode() {
        final long bits = NumberUtils.doubleToLongBits(frequency) * 421L;
        return (int) (seed ^ seed >>> 32 ^ bits ^ bits >>> 32);
    }
}
