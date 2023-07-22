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

    // SHARED

    protected static final double
            F3 = (1.0 / 3.0),
            G3 = (1.0 / 6.0),
            LIMIT3 = 0.6,
            F4 = ((Math.sqrt(5.0) - 1.0) * 0.25),
            G4 = ((5.0 - Math.sqrt(5.0)) * 0.05),
            LIMIT4 = 0.62;

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
    /**
     * Gets a 5-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y,z point with the given state
     */
    public static int hash32(long x, long y, long z, long s) {
        s ^= x * 0x89E182857D9ED689L ^ y * 0xA0F2EC75A1FE1575L ^ z * 0xBBE0563303A4615FL;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 59);
    }

    /**
     * Gets an 8-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long s) {
        s ^= x * 0x881403B9339BD42DL ^ y * 0x9A69443F36F710E7L ^ z * 0xAF36D01EF7518DBBL ^ w * 0xC6D1D6C8ED0C9631L;
        return (int)((s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L >>> 56);
    }

    /**
     * Computes the hash for a 3D int point and its dot product with a 3D double point as one step.
     * @return a double between -1.2571 and 1.2571, exclusive
     */
    protected static double gradCoord3D(long seed, int x, int y, int z, double xd, double yd, double zd) {
        final int hash = hash32(x, y, z, seed) << 2;
        return xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2];
    }

    // 2D SECTION

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
     * Gets noise with the lowest, fastest level of detail. Uses the given seed
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
        x *= x * (3 - 2 * x);
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
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
            x += x;
            y += y;

            amp *= 0.5;
            sum += noiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << (Math.max(1, octaves))) - 1));
    }

    // 3D SECTION

    /**
     * Gets noise with the lowest, fastest level of detail. Uses
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
     * 2D value noise with the lowest, fastest level of detail. Uses the
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
     * Gets noise with the lowest, fastest level of detail. Uses the given seed
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
        long xFloor = (int)Math.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        long yFloor = (int)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        long zFloor = (int)Math.floor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
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
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
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
     * Gets noise with the lowest, fastest level of detail. Uses
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
     * 2D simplex noise with the lowest, fastest level of detail. Uses the
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
     * Gets noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, z, or w.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(final double x, final double y, final double z, final double w, final long seed) {
        double n0, n1, n2, n3, n4;
        double[] gradient4DLUT = GRADIENTS_4D;
        double t = (x + y + z + w) * F4;
        int i = (int)Math.floor(x + t);
        int j = (int)Math.floor(y + t);
        int k = (int)Math.floor(z + t);
        int l = (int)Math.floor(w + t);
        t = (i + j + k + l) * G4;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double W0 = l - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        double w0 = w - W0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;

        int i1 = 2 - rankx >>> 31;
        int j1 = 2 - ranky >>> 31;
        int k1 = 2 - rankz >>> 31;
        int l1 = 2 - rankw >>> 31;

        int i2 = 1 - rankx >>> 31;
        int j2 = 1 - ranky >>> 31;
        int k2 = 1 - rankz >>> 31;
        int l2 = 1 - rankw >>> 31;

        int i3 = -rankx >>> 31;
        int j3 = -ranky >>> 31;
        int k3 = -rankz >>> 31;
        int l3 = -rankw >>> 31;

        double x1 = x0 - i1 + G4;
        double y1 = y0 - j1 + G4;
        double z1 = z0 - k1 + G4;
        double w1 = w0 - l1 + G4;

        double x2 = x0 - i2 + 2 * G4;
        double y2 = y0 - j2 + 2 * G4;
        double z2 = z0 - k2 + 2 * G4;
        double w2 = w0 - l2 + 2 * G4;

        double x3 = x0 - i3 + 3 * G4;
        double y3 = y0 - j3 + 3 * G4;
        double z3 = z0 - k3 + 3 * G4;
        double w3 = w0 - l3 + 3 * G4;

        double x4 = x0 - 1 + 4 * G4;
        double y4 = y0 - 1 + 4 * G4;
        double z4 = z0 - 1 + 4 * G4;
        double w4 = w0 - 1 + 4 * G4;

        double t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            final int h0 = (hash256(i, j, k, l, seed) & 0xFC);
            t0 *= t0;
            n0 = t0 * t0 * (x0 * gradient4DLUT[h0] + y0 * gradient4DLUT[h0 | 1] + z0 * gradient4DLUT[h0 | 2] + w0 * gradient4DLUT[h0 | 3]);
        }
        else n0 = 0;
        double t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            final int h1 = (hash256(i + i1, j + j1, k + k1, l + l1, seed) & 0xFC);
            t1 *= t1;
            n1 = t1 * t1 * (x1 * gradient4DLUT[h1] + y1 * gradient4DLUT[h1 | 1] + z1 * gradient4DLUT[h1 | 2] + w1 * gradient4DLUT[h1 | 3]);
        }
        else n1 = 0;
        double t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            final int h2 = (hash256(i + i2, j + j2, k + k2, l + l2, seed) & 0xFC);
            t2 *= t2;
            n2 = t2 * t2 * (x2 * gradient4DLUT[h2] + y2 * gradient4DLUT[h2 | 1] + z2 * gradient4DLUT[h2 | 2] + w2 * gradient4DLUT[h2 | 3]);
        }
        else n2 = 0;
        double t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            final int h3 = (hash256(i + i3, j + j3, k + k3, l + l3, seed) & 0xFC);
            t3 *= t3;
            n3 = t3 * t3 * (x3 * gradient4DLUT[h3] + y3 * gradient4DLUT[h3 | 1] + z3 * gradient4DLUT[h3 | 2] + w3 * gradient4DLUT[h3 | 3]);
        }
        else n3 = 0;
        double t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            final int h4 = (hash256(i + 1, j + 1, k + 1, l + 1, seed) & 0xFC);
            t4 *= t4;
            n4 = t4 * t4 * (x4 * gradient4DLUT[h4] + y4 * gradient4DLUT[h4 | 1] + z4 * gradient4DLUT[h4 | 2] + w4 * gradient4DLUT[h4 | 3]);
        }
        else n4 = 0;

        t = (n0 + n1 + n2 + n3 + n4) * 14.7279f;
        return t / (-0.3f * (1f - Math.abs(t)) + 1f);// gain function for [-1, 1] domain and range
    }

    /**
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
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
     * Gets noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
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
        return "SimplexNoiseStandalone{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueNoiseStandalone that = (ValueNoiseStandalone) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }


    // CONSTANTS

    /**
     * 256 x,y pairs, all on the unit circle.
     */
    protected static final double[] GRADIENTS_2D = {
            +0.6499429579167653, +0.7599829941876370,
            -0.1551483029088119, +0.9878911904175052,
            -0.8516180517334043, +0.5241628506120981,
            -0.9518580082090311, -0.3065392833036837,
            -0.3856887670108717, -0.9226289476282616,
            +0.4505066120763985, -0.8927730912586049,
            +0.9712959670388622, -0.2378742197339624,
            +0.8120673355833279, +0.5835637432865366,
            +0.0842989251943661, +0.9964405106232257,
            -0.7024883500032670, +0.7116952424385647,
            -0.9974536374007479, -0.0713178886116052,
            -0.5940875849508908, -0.8044003613917750,
            +0.2252075529515288, -0.9743108118529653,
            +0.8868317111719171, -0.4620925405802277,
            +0.9275724981153959, +0.3736432265409930,
            +0.3189067150428103, +0.9477861083074618,
            -0.5130301507665112, +0.8583705868705491,
            -0.9857873824221494, +0.1679977281313266,
            -0.7683809836504446, -0.6399927061806058,
            -0.0130202362193748, -0.9999152331316848,
            +0.7514561619680513, -0.6597830223946701,
            +0.9898275175279653, +0.1422725748147741,
            +0.5352066871710182, +0.8447211386057674,
            -0.2941198828144364, +0.9557685360657266,
            -0.9175289804081126, +0.3976689202229027,
            -0.8985631161871687, -0.4388443075032474,
            -0.2505005588110731, -0.9681164547900940,
            +0.5729409678802212, -0.8195966369650838,
            +0.9952584535626074, -0.0972656702653466,
            +0.7207814785200723, +0.6931623620930514,
            -0.0583247612407003, +0.9982976621360060,
            -0.7965970142012075, +0.6045107087270838,
            -0.9771604781144960, -0.2125027058911242,
            -0.4736001288089817, -0.8807399831914728,
            +0.3615343409387538, -0.9323587937709286,
            +0.9435535266854258, -0.3312200813348966,
            +0.8649775992346886, +0.5018104750024599,
            +0.1808186720712497, +0.9835164502083277,
            -0.6299339540895539, +0.7766487066139361,
            -0.9996609468975833, +0.0260382650694516,
            -0.6695112313914258, -0.7428019325774111,
            +0.1293727267195084, -0.9915960354807594,
            +0.8376810167470904, -0.5461597881403947,
            +0.9595170289111490, +0.2816506190824391,
            +0.4095816551369482, +0.9122734610714476,
            -0.4271076040148479, +0.9042008043530463,
            -0.9647728141412515, +0.2630844295924223,
            -0.8269869890664444, -0.5622210596507540,
            -0.1102159255238020, -0.9939076666174438,
            +0.6837188597775012, -0.7297455178242300,
            +0.9989724417383330, +0.0453217458550843,
            +0.6148313475439905, +0.7886586169422362,
            -0.1997618324529528, +0.9798444827088829,
            -0.8744989400706802, +0.4850274258382270,
            -0.9369870231562731, -0.3493641630687752,
            -0.3434772946489506, -0.9391609809082988,
            +0.4905057254335028, -0.8714379687143274,
            +0.9810787787756657, -0.1936089611460388,
            +0.7847847614201463, +0.6197684069414349,
            +0.0390518795551629, +0.9992371844077906,
            -0.7340217731995672, +0.6791259356474049,
            -0.9931964444524306, -0.1164509455824639,
            -0.5570202966000876, -0.8304988796955420,
            +0.2691336060685578, -0.9631028512493016,
            +0.9068632806061000, -0.4214249521425399,
            +0.9096851999779008, +0.4152984913783901,
            +0.2756236986873733, +0.9612656119522284,
            -0.5514058359842319, +0.8342371389734039,
            -0.9923883787916933, +0.1231474954645637,
            -0.7385858406439617, -0.6741594440488484,
            +0.0323110469045428, -0.9994778618098213,
            +0.7805865154410089, -0.6250477517051506,
            +0.9823623706068018, +0.1869870926448790,
            +0.4963724943556111, +0.8681096398768929,
            -0.3371347561867868, +0.9414564016304079,
            -0.9346092156607797, +0.3556762769737983,
            -0.8777506000588920, -0.4791178185960681,
            -0.2063664269701996, -0.9784747813917093,
            +0.6094977881394418, -0.7927877687333024,
            +0.9986440175043460, -0.0520588734297966,
            +0.6886255051458764, +0.7251171723677399,
            -0.1035094220814735, +0.9946284731196666,
            -0.8231759450656516, +0.5677863713275190,
            -0.9665253951623188, -0.2565709658288005,
            -0.4331968034012919, -0.9012993562201753,
            +0.4034189716368784, -0.9150153732716426,
            +0.9575954428121146, -0.2881162402667889,
            +0.8413458575409575, +0.5404971304259356,
            +0.1360581877502697, +0.9907008476558967,
            -0.6644857355505560, +0.7473009482463117,
            -0.9998138366647180, -0.0192948701414780,
            -0.6351581891853917, -0.7723820781910558,
            +0.1741806522163015, -0.9847137149413040,
            +0.8615731658120597, -0.5076334109892543,
            +0.9457661714829020, +0.3248481935898273,
            +0.3678149601703667, +0.9298990026206456,
            -0.4676486851245607, +0.8839144230643990,
            -0.9757048995218635, +0.2190889067228882,
            -0.8006563717736747, -0.5991238388999518,
            -0.0650570415691071, -0.9978815467490495,
            +0.7160896397121960, -0.6980083293893113,
            +0.9958918787052943, +0.0905503502413954,
            +0.5784561871098056, +0.8157134543418942,
            -0.2439648281544816, +0.9697840804135497,
            -0.8955826311865743, +0.4448952131872543,
            -0.9201904205900768, -0.3914710587696841,
            -0.3005599364234082, -0.9537629289384008,
            +0.5294967923694863, -0.8483119396014800,
            +0.9888453593035162, -0.1489458135829932,
            +0.7558893631265085, +0.6546993743025888,
            -0.0062754222469803, +0.9999803093439501,
            -0.7640466961212760, +0.6451609459244744,
            -0.9868981170802014, -0.1613446822909051,
            -0.5188082666339063, -0.8548906260290385,
            +0.3125065582647844, -0.9499156020623616,
            +0.9250311403279032, -0.3798912863223621,
            +0.8899283927548960, +0.4561002694240463,
            +0.2317742435145519, +0.9727696027545563,
            -0.5886483179573486, +0.8083892365475831,
            -0.9969499014064180, +0.0780441803450664,
            -0.7072728176724660, -0.7069407057042696,
            +0.0775759270620736, -0.9969864470194466,
            +0.8081126726681943, -0.5890279350532263,
            +0.9728783545459001, +0.2313173302112532,
            +0.4565181982253288, +0.8897140746830408,
            -0.3794567783511009, +0.9252094645881026,
            -0.9497687200714887, +0.3129526775309106,
            -0.8551342041690687, -0.5184066867432686,
            -0.1618081880753845, -0.9868222283024238,
            +0.6448020194233159, -0.7643496292585048,
            +0.9999772516247822, -0.0067450895432855,
            +0.6550543261176665, +0.7555817823601425,
            -0.1484813589986064, +0.9889152066936411,
            -0.8480631534437840, +0.5298951667745091,
            -0.9539039899003245, -0.3001119425351840,
            -0.3919032080850608, -0.9200064540494471,
            +0.4444745293405786, -0.8957914895596358,
            +0.9696693887216105, -0.2444202867526717,
            +0.8159850520735595, +0.5780730012658526,
            +0.0910180879994953, +0.9958492394217692,
            -0.6976719213969089, +0.7164173993520435,
            -0.9979119924958648, -0.0645883521459785,
            -0.5994998228898376, -0.8003748886334786,
            +0.2186306161766729, -0.9758076929755208,
            +0.8836946816279001, -0.4680637880274058,
            +0.9300716543684309, +0.3673781672069940,
            +0.3252923626016029, +0.9456134933645286,
            -0.5072286936943775, +0.8618114946396893,
            -0.9846317976415725, +0.1746431306210620,
            -0.7726803123417516, -0.6347953488483143,
            -0.0197644578133314, -0.9998046640256011,
            +0.7469887719961158, -0.6648366525032559,
            +0.9907646418168752, +0.1355928631067248,
            +0.5408922318074902, +0.8410919055432124,
            -0.2876664477065717, +0.9577306588304888,
            -0.9148257956391065, +0.4038486890325085,
            -0.9015027194859215, -0.4327734358292892,
            -0.2570248925062563, -0.9664047830139022,
            +0.5673996816983953, -0.8234425306046317,
            +0.9945797473944409, -0.1039765650173647,
            +0.7254405241129018, +0.6882848581617921,
            -0.0515898273251730, +0.9986683582233687,
            -0.7925014140531963, +0.6098700752813540,
            -0.9785715990807187, -0.2059068368767903,
            -0.4795300252265173, -0.8775254725113429,
            +0.3552372730694574, -0.9347761656258549,
            +0.9412979532686209, -0.3375768996425928,
            +0.8683426789873530, +0.4959647082697184,
            +0.1874484652642005, +0.9822744386728669,
            -0.6246810590458048, +0.7808800000444446,
            -0.9994625758058275, +0.0327804753409776,
            -0.6745062666468870, -0.7382691218343610,
            +0.1226813796500722, -0.9924461089082646,
            +0.8339780641890598, -0.5517975973592748,
            +0.9613949601033843, +0.2751721837101493,
            +0.4157257040026583, +0.9094900433932711,
            -0.4209989726203348, +0.9070611142875780,
            -0.9629763390922247, +0.2695859238694348,
            -0.8307604078465821, -0.5566301687427484,
            -0.1169174144996730, -0.9931416405461567,
            +0.6787811074228051, -0.7343406622310046,
            +0.9992554159724470, +0.0385825562881973,
            +0.6201369341201711, +0.7844935837468874,
            -0.1931481494214682, +0.9811696042861612,
            -0.8712074932224428, +0.4909149659086258,
            -0.9393222007870077, -0.3430361542296271,
            -0.3498042060103595, -0.9368228314134226,
            +0.4846166400948296, -0.8747266499559725,
            +0.9797505510481769, -0.2002220210685972,
            +0.7889473022428521, +0.6144608647291752,
            +0.0457909354721791, +0.9989510449609544,
            -0.7294243101497431, +0.6840615292227530,
            -0.9939593229024027, -0.1097490975607407,
            -0.5626094146025390, -0.8267228354174018,
            +0.2626312687452330, -0.9648962724963078,
            +0.9040001019019392, -0.4275322394408211,
            +0.9124657316291773, +0.4091531358824348,
            +0.2821012513235693, +0.9593846381935018,
            -0.5457662881946498, +0.8379374431723614,
            -0.9915351626845509, +0.1298384425357957,
            -0.7431163048326799, -0.6691622803863227,
            +0.0255687442062853, -0.9996730662170076,
            +0.7763527553119807, -0.6302986588273021,
            +0.9836012681423212, +0.1803567168386515,
            +0.5022166799422209, +0.8647418148718223,
            -0.3307768791887710, +0.9437089891455613,
            -0.9321888864830543, +0.3619722087639923,
            -0.8809623252471085, -0.4731864130500873,
            -0.2129616324856343, -0.9770605626515961,
            +0.6041364985661350, -0.7968808512571063,
            +0.9982701582127194, -0.0587936324949578,
            +0.6935008202914851, +0.7204558364362367,
            -0.0967982092968079, +0.9953040272584711,
            -0.8193274492343137, +0.5733258505694586,
            -0.9682340024187017, -0.2500458289199430,
            -0.4392662937408502, -0.8983569018954422,
            +0.3972379338845546, -0.9177156552457467,
            +0.9556302892322005, -0.2945687530984589,
            +0.8449724198323217, +0.5348098818484104,
            +0.1427374585755972, +0.9897605861618151,
            -0.6594300077680133, +0.7517659641504648,
            -0.9999212381512442, -0.0125505973595986,
            -0.6403535266476091, -0.7680803088935230,
            +0.1675347077076747, -0.9858661784001437,
            +0.8581295336101056, -0.5134332513054668,
            +0.9479357869928937, +0.3184615263075951,
            +0.3740788450165170, +0.9273969040875156,
            -0.4616759649446430, +0.8870486477034012,
            -0.9742049295269273, +0.2256651397213017,
            -0.8046793020829978, -0.5937097108850584,
            -0.0717863620135296, -0.9974200309943962,
            +0.7113652211526822, -0.7028225395748172,
            +0.9964799940037152, +0.0838309104707540,
            +0.5839450884626246, +0.8117931594072332,
            -0.2374179978909748, +0.9714075840127259,
            -0.8925614000865144, +0.4509258775847768,
            -0.9228099950981292, -0.3852553866553855,
            -0.3069863155319683, -0.9517139286971200,
            +0.5237628071845146, -0.8518641451605984,
            +0.9878182118285335, -0.1556122758007173,
            +0.7602881737752754, +0.6495859395164404,
            +0.0004696772366984, +0.9999998897016406,
            -0.7596776469502666, +0.6502998329417794,
            -0.9879639510809196, -0.1546842957917130,
            -0.5245627784110601, -0.8513717704420726,
            +0.3060921834538644, -0.9520018777441807,
            +0.9224476966294768, -0.3861220622846781,
            +0.8929845854878761, +0.4500872471877493,
            +0.2383303891026603, +0.9711841358002995,
            -0.5831822693781987, +0.8123413326200348,
            -0.9964008074312266, +0.0847669213219385,
            -0.7120251067268070, -0.7021540054650968,
            +0.0708493994771745, -0.9974870237721009,
            +0.8041212432524677, -0.5944653279629567,
            +0.9744164792492415, +0.2247499165016809,
            +0.4625090142797330, +0.8866145790082576,
    };

    /**
     * The 32 vertices of a rhombic triacontahedron, scaled to have a magnitude of about 1.2571 .
     * These are organized into groups of four; the fourth is always ignored, and is 0.
     */
    protected static final double[] GRADIENTS_3D =
            {
                    -0.448549002408981, +1.174316525459290, +0.000000000000001, +0.0,
                    +0.000000000000001, +1.069324374198914, +0.660878777503967, +0.0,
                    +0.448549002408981, +1.174316525459290, +0.000000000000001, +0.0,
                    +0.000000000000001, +1.069324374198914, -0.660878777503967, +0.0,
                    -0.725767493247986, +0.725767493247986, -0.725767493247986, +0.0,
                    -1.069324374198914, +0.660878777503967, +0.000000000000001, +0.0,
                    -0.725767493247986, +0.725767493247986, +0.725767493247986, +0.0,
                    +0.725767493247986, +0.725767493247986, +0.725767493247986, +0.0,
                    +1.069324374198914, +0.660878777503967, +0.000000000000000, +0.0,
                    +0.725767493247986, +0.725767493247986, -0.725767493247986, +0.0,
                    -0.660878777503967, +0.000000000000003, -1.069324374198914, +0.0,
                    -1.174316525459290, +0.000000000000003, -0.448549002408981, +0.0,
                    +0.000000000000000, +0.448549002408981, -1.174316525459290, +0.0,
                    -0.660878777503967, +0.000000000000001, +1.069324374198914, +0.0,
                    +0.000000000000001, +0.448549002408981, +1.174316525459290, +0.0,
                    -1.174316525459290, +0.000000000000001, +0.448549002408981, +0.0,
                    +0.660878777503967, +0.000000000000001, +1.069324374198914, +0.0,
                    +1.174316525459290, +0.000000000000001, +0.448549002408981, +0.0,
                    +0.660878777503967, +0.000000000000001, -1.069324374198914, +0.0,
                    +1.174316525459290, +0.000000000000001, -0.448549002408981, +0.0,
                    -0.725767493247986, -0.725767493247986, -0.725767493247986, +0.0,
                    -1.069324374198914, -0.660878777503967, -0.000000000000001, +0.0,
                    -0.000000000000001, -0.448549002408981, -1.174316525459290, +0.0,
                    -0.000000000000001, -0.448549002408981, +1.174316525459290, +0.0,
                    -0.725767493247986, -0.725767493247986, +0.725767493247986, +0.0,
                    +0.725767493247986, -0.725767493247986, +0.725767493247986, +0.0,
                    +1.069324374198914, -0.660878777503967, +0.000000000000001, +0.0,
                    +0.725767493247986, -0.725767493247986, -0.725767493247986, +0.0,
                    -0.000000000000004, -1.069324374198914, -0.660878777503967, +0.0,
                    -0.448549002408981, -1.174316525459290, -0.000000000000003, +0.0,
                    -0.000000000000003, -1.069324374198914, +0.660878777503967, +0.0,
                    +0.448549002408981, -1.174316525459290, +0.000000000000003, +0.0,
            };
    /**
     * Vertices from some 4D polytope, probably; maybe center points of edges.
     * I can't remember and I don't feel like checking the 4D geometry pages of Wikipedia.
     * Each gradient vector here is longer than a unit vector.
     */
    protected static final double[] GRADIENTS_4D =
            {
                    -0.5875167, +1.4183908, +1.4183908, +1.4183908,
                    -0.5875167, +1.4183908, +1.4183908, -1.4183908,
                    -0.5875167, +1.4183908, -1.4183908, +1.4183908,
                    -0.5875167, +1.4183908, -1.4183908, -1.4183908,
                    -0.5875167, -1.4183908, +1.4183908, +1.4183908,
                    -0.5875167, -1.4183908, +1.4183908, -1.4183908,
                    -0.5875167, -1.4183908, -1.4183908, +1.4183908,
                    -0.5875167, -1.4183908, -1.4183908, -1.4183908,
                    +1.4183908, -0.5875167, +1.4183908, +1.4183908,
                    +1.4183908, -0.5875167, +1.4183908, -1.4183908,
                    +1.4183908, -0.5875167, -1.4183908, +1.4183908,
                    +1.4183908, -0.5875167, -1.4183908, -1.4183908,
                    -1.4183908, -0.5875167, +1.4183908, +1.4183908,
                    -1.4183908, -0.5875167, +1.4183908, -1.4183908,
                    -1.4183908, -0.5875167, -1.4183908, +1.4183908,
                    -1.4183908, -0.5875167, -1.4183908, -1.4183908,
                    +1.4183908, +1.4183908, -0.5875167, +1.4183908,
                    +1.4183908, +1.4183908, -0.5875167, -1.4183908,
                    +1.4183908, -1.4183908, -0.5875167, +1.4183908,
                    +1.4183908, -1.4183908, -0.5875167, -1.4183908,
                    -1.4183908, +1.4183908, -0.5875167, +1.4183908,
                    -1.4183908, +1.4183908, -0.5875167, -1.4183908,
                    -1.4183908, -1.4183908, -0.5875167, +1.4183908,
                    -1.4183908, -1.4183908, -0.5875167, -1.4183908,
                    +1.4183908, +1.4183908, +1.4183908, -0.5875167,
                    +1.4183908, +1.4183908, -1.4183908, -0.5875167,
                    +1.4183908, -1.4183908, +1.4183908, -0.5875167,
                    +1.4183908, -1.4183908, -1.4183908, -0.5875167,
                    -1.4183908, +1.4183908, +1.4183908, -0.5875167,
                    -1.4183908, +1.4183908, -1.4183908, -0.5875167,
                    -1.4183908, -1.4183908, +1.4183908, -0.5875167,
                    -1.4183908, -1.4183908, -1.4183908, -0.5875167,
                    +0.5875167, +1.4183908, +1.4183908, +1.4183908,
                    +0.5875167, +1.4183908, +1.4183908, -1.4183908,
                    +0.5875167, +1.4183908, -1.4183908, +1.4183908,
                    +0.5875167, +1.4183908, -1.4183908, -1.4183908,
                    +0.5875167, -1.4183908, +1.4183908, +1.4183908,
                    +0.5875167, -1.4183908, +1.4183908, -1.4183908,
                    +0.5875167, -1.4183908, -1.4183908, +1.4183908,
                    +0.5875167, -1.4183908, -1.4183908, -1.4183908,
                    +1.4183908, +0.5875167, +1.4183908, +1.4183908,
                    +1.4183908, +0.5875167, +1.4183908, -1.4183908,
                    +1.4183908, +0.5875167, -1.4183908, +1.4183908,
                    +1.4183908, +0.5875167, -1.4183908, -1.4183908,
                    -1.4183908, +0.5875167, +1.4183908, +1.4183908,
                    -1.4183908, +0.5875167, +1.4183908, -1.4183908,
                    -1.4183908, +0.5875167, -1.4183908, +1.4183908,
                    -1.4183908, +0.5875167, -1.4183908, -1.4183908,
                    +1.4183908, +1.4183908, +0.5875167, +1.4183908,
                    +1.4183908, +1.4183908, +0.5875167, -1.4183908,
                    +1.4183908, -1.4183908, +0.5875167, +1.4183908,
                    +1.4183908, -1.4183908, +0.5875167, -1.4183908,
                    -1.4183908, +1.4183908, +0.5875167, +1.4183908,
                    -1.4183908, +1.4183908, +0.5875167, -1.4183908,
                    -1.4183908, -1.4183908, +0.5875167, +1.4183908,
                    -1.4183908, -1.4183908, +0.5875167, -1.4183908,
                    +1.4183908, +1.4183908, +1.4183908, +0.5875167,
                    +1.4183908, +1.4183908, -1.4183908, +0.5875167,
                    +1.4183908, -1.4183908, +1.4183908, +0.5875167,
                    +1.4183908, -1.4183908, -1.4183908, +0.5875167,
                    -1.4183908, +1.4183908, +1.4183908, +0.5875167,
                    -1.4183908, +1.4183908, -1.4183908, +0.5875167,
                    -1.4183908, -1.4183908, +1.4183908, +0.5875167,
                    -1.4183908, -1.4183908, -1.4183908, +0.5875167,
            };

}
