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

/*
 * OpenSimplex2S.java was originally by KdotJPG, who released it into the public domain.
 * Original license: https://github.com/KdotJPG/OpenSimplex2/blob/45cc32d5bf09ec89732ea108669417b6cad4546d/LICENSE
 */

package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.DigitTools;

/**
 * K.jpg's OpenSimplex 2, smoother variant.
 * This is a variant on the Simplex Noise algorithm in 2D, and a subtly different algorithm in 3D and 4D. This does not
 * support 5D or 6D noise. This is very similar to {@link OpenSimplex2}, but has less noticeably-separate "spots" or
 * "surflets" of similar results, and tends to smooth results into continuous gradients.
 * <br>
 * OpenSimplex2S.java was originally by KdotJPG, who released it into the public domain.
 * <a href="https://github.com/KdotJPG/OpenSimplex2/blob/45cc32d5bf09ec89732ea108669417b6cad4546d/LICENSE">Original license</a>.
 */
public class OpenSimplex2Smooth implements INoise {
    public long seed;

    public OpenSimplex2Smooth() {
        seed = 0x31337CA770E5L;
    }

    public OpenSimplex2Smooth(long seed) {
        this.seed = seed;
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2 here. This generator can produce 2D, 3D, or 4D
     * noise.
     *
     * @return the minimum supported dimension, which is 2 here
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 4 here. This generator can produce 2D, 3D, or 4D
     * noise.
     *
     * @return the maximum supported dimension, which is 4 here
     */
    @Override
    public int getMaxDimension() {
        return 4;
    }

    /**
     * Returns true if this generator can be seeded with {@link #setSeed(long)} and retrieved with {@link #getSeed()},
     * which this can be.
     *
     * @return true, because {@link #setSeed(long)} and {@link #getSeed()} are supported
     */
    @Override
    public boolean canUseSeed() {
        return true;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * Gets 2D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y) {
        return noise2(seed, x, y);
    }

    /**
     * Gets 3D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z) {
        return noise3_Fallback(seed, x, y, z);
    }

    /**
     * Gets 4D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w) {
        return noise4_Fallback(seed, x, y, z, w);
    }

    /**
     * Gets 5D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        throw new UnsupportedOperationException("5D noise is not supported.");
    }

    /**
     * Gets 6D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        throw new UnsupportedOperationException("6D noise is not supported.");
    }

    /**
     * Gets 2D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return noise2(seed, x, y);
    }

    /**
     * Gets 3D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return noise3_Fallback(seed, x, y, z);
    }

    /**
     * Gets 4D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return noise4_Fallback(seed, x, y, z, w);
    }

    /**
     * Gets 5D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        throw new UnsupportedOperationException("5D noise is not supported.");

    }

    /**
     * Gets 6D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * {@link #getNoise}; you can check if this will happen with {@link #canUseSeed()}.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param v    v position; can be any finite float
     * @param seed
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        throw new UnsupportedOperationException("6D noise is not supported.");
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. Implementors are strongly
     * encouraged to implement this in general, and that is required to use an INoise class with {@link Serializer}.
     *
     * @return a copy of this INoise
     */
    @Override
    public OpenSimplex2Smooth copy() {
        return new OpenSimplex2Smooth(seed);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public OpenSimplex2Smooth stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = DigitTools.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    /**
     * Returns a typically-four-character String constant that should uniquely identify this INoise as well as possible.
     * If a duplicate tag is already registered and {@link Serializer#register(INoise)} attempts to register the same
     * tag again, a message is printed to {@code System.err}. The default implementation returns the String
     * {@code (NO)}, which is already registered in Serializer to a null value. Implementing this is required for any
     * usage of Serializer.
     *
     * @return a short String constant that identifies this INoise type
     */
    @Override
    public String getTag() {
        return "OSSN";
    }



    private static final long PRIME_X = 0x5205402B9270C86FL;
    private static final long PRIME_Y = 0x598CD327003817B5L;
    private static final long PRIME_Z = 0x5BCC226E9FA0BACBL;
    private static final long PRIME_W = 0x56CC5227E58F554BL;
    private static final long HASH_MULTIPLIER = 0x53A3F72DEEC546F5L;
    private static final long SEED_FLIP_3D = -0x52D547B2E96ED629L;

    private static final float ROOT2OVER2 = 0.7071067811865476F;
    private static final float SKEW_2D = 0.366025403784439F;
    private static final float UNSKEW_2D = -0.21132486540518713F;

    private static final float ROOT3OVER3 = 0.577350269189626F;
    private static final float FALLBACK_ROTATE3 = (2f / 3f);
    private static final float ROTATE3_ORTHOGONALIZER = UNSKEW_2D;

    private static final float SKEW_4D = 0.309016994374947f;
    private static final float UNSKEW_4D = -0.138196601125011f;

    private static final int N_GRADS_2D_EXPONENT = 7;
    private static final int N_GRADS_3D_EXPONENT = 8;
    private static final int N_GRADS_4D_EXPONENT = 9;
    private static final int N_GRADS_2D = 1 << N_GRADS_2D_EXPONENT;
    private static final int N_GRADS_3D = 1 << N_GRADS_3D_EXPONENT;
    private static final int N_GRADS_4D = 1 << N_GRADS_4D_EXPONENT;

    private static final float NORMALIZER_2D = 0.05481866495625118F;
    private static final float NORMALIZER_3D = 0.2781926117527186F;
    private static final float NORMALIZER_4D = 0.11127401889945551F;

    private static final float RSQUARED_2D = 2.0f / 3.0f;
    private static final float RSQUARED_3D = 3.0f / 4.0f;
    private static final float RSQUARED_4D = 4.0f / 5.0f;

    /*
     * Noise Evaluators
     */

    /**
     * 2D OpenSimplex2Smooth/SuperSimplex noise, standard lattice orientation.
     */
    public static float noise2(long seed, float x, float y) {

        // Get points for A2* lattice
        float s = SKEW_2D * (x + y);
        float xs = x + s, ys = y + s;

        return noise2_UnskewedBase(seed, xs, ys);
    }

    /**
     * 2D OpenSimplex2Smooth/SuperSimplex noise, with Y pointing down the main diagonal.
     * Might be better for a 2D sandbox style game, where Y is vertical.
     * Probably slightly less optimal for heightmaps or continent maps,
     * unless your map is centered around an equator. It's a slight
     * difference, but the option is here to make it easy.
     */
    public static float noise2_ImproveX(long seed, float x, float y) {

        // Skew transform and rotation baked into one.
        float xx = x * ROOT2OVER2;
        float yy = y * (ROOT2OVER2 * (1 + 2 * SKEW_2D));

        return noise2_UnskewedBase(seed, yy + xx, yy - xx);
    }

    /**
     * 2D  OpenSimplex2Smooth/SuperSimplex noise base.
     */
    private static float noise2_UnskewedBase(long seed, float xs, float ys) {

        // Get base points and offsets.
        int xsb = fastFloor(xs), ysb = fastFloor(ys);
        float xi = xs - xsb, yi = ys - ysb;

        // Prime pre-multiplication for hash.
        long xsbp = xsb * PRIME_X, ysbp = ysb * PRIME_Y;

        // Unskew.
        float t = (xi + yi) * UNSKEW_2D;
        float dx0 = xi + t, dy0 = yi + t;

        // First vertex.
        float a0 = RSQUARED_2D - dx0 * dx0 - dy0 * dy0;
        float value = (a0 * a0) * (a0 * a0) * grad(seed, xsbp, ysbp, dx0, dy0);

        // Second vertex.
        float a1 = 2 * (1 + 2 * UNSKEW_2D) * (1 / UNSKEW_2D + 2) * t + ((-2 * (1 + 2 * UNSKEW_2D) * (1 + 2 * UNSKEW_2D)) + a0);
        float dx1 = dx0 - (1 + 2 * UNSKEW_2D);
        float dy1 = dy0 - (1 + 2 * UNSKEW_2D);
        value += (a1 * a1) * (a1 * a1) * grad(seed, xsbp + PRIME_X, ysbp + PRIME_Y, dx1, dy1);

        // Third and fourth vertices.
        // Nested conditionals were faster than compact bit logic/arithmetic.
        float xmyi = xi - yi;
        if (t < UNSKEW_2D) {
            if (xi + xmyi > 1) {
                float dx2 = dx0 - (3 * UNSKEW_2D + 2);
                float dy2 = dy0 - (3 * UNSKEW_2D + 1);
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp + (PRIME_X << 1), ysbp + PRIME_Y, dx2, dy2);
                }
            }
            else
            {
                float dx2 = dx0 - UNSKEW_2D;
                float dy2 = dy0 - (UNSKEW_2D + 1);
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp + PRIME_Y, dx2, dy2);
                }
            }

            if (yi - xmyi > 1) {
                float dx3 = dx0 - (3 * UNSKEW_2D + 1);
                float dy3 = dy0 - (3 * UNSKEW_2D + 2);
                float a3 = RSQUARED_2D - dx3 * dx3 - dy3 * dy3;
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * grad(seed, xsbp + PRIME_X, ysbp + (PRIME_Y << 1), dx3, dy3);
                }
            }
            else
            {
                float dx3 = dx0 - (UNSKEW_2D + 1);
                float dy3 = dy0 - UNSKEW_2D;
                float a3 = RSQUARED_2D - dx3 * dx3 - dy3 * dy3;
                if (a3 > 0) {
                    value += (a3 * a3) * (a3 * a3) * grad(seed, xsbp + PRIME_X, ysbp, dx3, dy3);
                }
            }
        }
        else
        {
            if (xi + xmyi < 0) {
                float dx2 = dx0 + (1 + UNSKEW_2D);
                float dy2 = dy0 + UNSKEW_2D;
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp - PRIME_X, ysbp, dx2, dy2);
                }
            }
            else
            {
                float dx2 = dx0 - (UNSKEW_2D + 1);
                float dy2 = dy0 - UNSKEW_2D;
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp + PRIME_X, ysbp, dx2, dy2);
                }
            }

            if (yi < xmyi) {
                float dx2 = dx0 + UNSKEW_2D;
                float dy2 = dy0 + (UNSKEW_2D + 1);
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp - PRIME_Y, dx2, dy2);
                }
            }
            else
            {
                float dx2 = dx0 - UNSKEW_2D;
                float dy2 = dy0 - (UNSKEW_2D + 1);
                float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
                if (a2 > 0) {
                    value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp + PRIME_Y, dx2, dy2);
                }
            }
        }

        return value;
    }

    /**
     * 3D OpenSimplex2Smooth/SuperSimplex noise, with better visual isotropy in (X, Y).
     * Recommended for 3D terrain and time-varied animations.
     * The Z coordinate should always be the "different" coordinate in whatever your use case is.
     * If Y is vertical in world coordinates, call noise3_ImproveXZ(x, z, Y) or use noise3_XZBeforeY.
     * If Z is vertical in world coordinates, call noise3_ImproveXZ(x, y, Z).
     * For a time varied animation, call noise3_ImproveXY(x, y, T).
     */
    public static float noise3_ImproveXY(long seed, float x, float y, float z) {

        // Re-orient the cubic lattices without skewing, so Z points up the main lattice diagonal,
        // and the planes formed by XY are moved far out of alignment with the cube faces.
        // Orthonormal rotation. Not a skew transform.
        float xy = x + y;
        float s2 = xy * ROTATE3_ORTHOGONALIZER;
        float zz = z * ROOT3OVER3;
        float xr = x + s2 + zz;
        float yr = y + s2 + zz;
        float zr = xy * -ROOT3OVER3 + zz;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * 3D OpenSimplex2Smooth/SuperSimplex noise, with better visual isotropy in (X, Z).
     * Recommended for 3D terrain and time-varied animations.
     * The Y coordinate should always be the "different" coordinate in whatever your use case is.
     * If Y is vertical in world coordinates, call noise3_ImproveXZ(x, Y, z).
     * If Z is vertical in world coordinates, call noise3_ImproveXZ(x, Z, y) or use noise3_ImproveXY.
     * For a time varied animation, call noise3_ImproveXZ(x, T, y) or use noise3_ImproveXY.
     */
    public static float noise3_ImproveXZ(long seed, float x, float y, float z) {

        // Re-orient the cubic lattices without skewing, so Y points up the main lattice diagonal,
        // and the planes formed by XZ are moved far out of alignment with the cube faces.
        // Orthonormal rotation. Not a skew transform.
        float xz = x + z;
        float s2 = xz * -0.211324865405187f;
        float yy = y * ROOT3OVER3;
        float xr = x + s2 + yy;
        float zr = z + s2 + yy;
        float yr = xz * -ROOT3OVER3 + yy;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * 3D OpenSimplex2Smooth/SuperSimplex noise, fallback rotation option
     * Use noise3_ImproveXY or noise3_ImproveXZ instead, wherever appropriate.
     * They have less diagonal bias. This function's best use is as a fallback.
     */
    public static float noise3_Fallback(long seed, float x, float y, float z) {

        // Re-orient the cubic lattices via rotation, to produce a familiar look.
        // Orthonormal rotation. Not a skew transform.
        float r = FALLBACK_ROTATE3 * (x + y + z);
        float xr = r - x, yr = r - y, zr = r - z;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * Generate overlapping cubic lattices for 3D Re-oriented BCC noise.
     * Lookup table implementation inspired by DigitalShadow.
     * It was actually faster to narrow down the points in the loop itself,
     * than to build up the index with enough info to isolate 8 points.
     */
    private static float noise3_UnrotatedBase(long seed, float xr, float yr, float zr) {

        // Get base points and offsets.
        int xrb = fastFloor(xr), yrb = fastFloor(yr), zrb = fastFloor(zr);
        float xi = xr - xrb, yi = yr - yrb, zi = zr - zrb;

        // Prime pre-multiplication for hash. Also flip seed for second lattice copy.
        long xrbp = xrb * PRIME_X, yrbp = yrb * PRIME_Y, zrbp = zrb * PRIME_Z;
        long seed2 = seed ^ SEED_FLIP_3D;

        // -1 if positive, 0 if negative.
        int xNMask = (int)(-0.5f - xi), yNMask = (int)(-0.5f - yi), zNMask = (int)(-0.5f - zi);

        // First vertex.
        float x0 = xi + xNMask;
        float y0 = yi + yNMask;
        float z0 = zi + zNMask;
        float a0 = RSQUARED_3D - x0 * x0 - y0 * y0 - z0 * z0;
        float value = (a0 * a0) * (a0 * a0) * grad(seed,
                xrbp + (xNMask & PRIME_X), yrbp + (yNMask & PRIME_Y), zrbp + (zNMask & PRIME_Z), x0, y0, z0);

        // Second vertex.
        float x1 = xi - 0.5f;
        float y1 = yi - 0.5f;
        float z1 = zi - 0.5f;
        float a1 = RSQUARED_3D - x1 * x1 - y1 * y1 - z1 * z1;
        value += (a1 * a1) * (a1 * a1) * grad(seed2,
                xrbp + PRIME_X, yrbp + PRIME_Y, zrbp + PRIME_Z, x1, y1, z1);

        // Shortcuts for building the remaining falloffs.
        // Derived by subtracting the polynomials with the offsets plugged in.
        float xAFlipMask0 = ((xNMask | 1) << 1) * x1;
        float yAFlipMask0 = ((yNMask | 1) << 1) * y1;
        float zAFlipMask0 = ((zNMask | 1) << 1) * z1;
        float xAFlipMask1 = (-2 - (xNMask << 2)) * x1 - 1.0f;
        float yAFlipMask1 = (-2 - (yNMask << 2)) * y1 - 1.0f;
        float zAFlipMask1 = (-2 - (zNMask << 2)) * z1 - 1.0f;

        boolean skip5 = false;
        float a2 = xAFlipMask0 + a0;
        if (a2 > 0) {
            float x2 = x0 - (xNMask | 1);
            float y2 = y0;
            float z2 = z0;
            value += (a2 * a2) * (a2 * a2) * grad(seed,
                    xrbp + (~xNMask & PRIME_X), yrbp + (yNMask & PRIME_Y), zrbp + (zNMask & PRIME_Z), x2, y2, z2);
        }
        else
        {
            float a3 = yAFlipMask0 + zAFlipMask0 + a0;
            if (a3 > 0) {
                float x3 = x0;
                float y3 = y0 - (yNMask | 1);
                float z3 = z0 - (zNMask | 1);
                value += (a3 * a3) * (a3 * a3) * grad(seed,
                        xrbp + (xNMask & PRIME_X), yrbp + (~yNMask & PRIME_Y), zrbp + (~zNMask & PRIME_Z), x3, y3, z3);
            }

            float a4 = xAFlipMask1 + a1;
            if (a4 > 0) {
                float x4 = (xNMask | 1) + x1;
                float y4 = y1;
                float z4 = z1;
                value += (a4 * a4) * (a4 * a4) * grad(seed2,
                        xrbp + (xNMask & (PRIME_X * 2)), yrbp + PRIME_Y, zrbp + PRIME_Z, x4, y4, z4);
                skip5 = true;
            }
        }

        boolean skip9 = false;
        float a6 = yAFlipMask0 + a0;
        if (a6 > 0) {
            float x6 = x0;
            float y6 = y0 - (yNMask | 1);
            float z6 = z0;
            value += (a6 * a6) * (a6 * a6) * grad(seed,
                    xrbp + (xNMask & PRIME_X), yrbp + (~yNMask & PRIME_Y), zrbp + (zNMask & PRIME_Z), x6, y6, z6);
        }
        else
        {
            float a7 = xAFlipMask0 + zAFlipMask0 + a0;
            if (a7 > 0) {
                float x7 = x0 - (xNMask | 1);
                float y7 = y0;
                float z7 = z0 - (zNMask | 1);
                value += (a7 * a7) * (a7 * a7) * grad(seed,
                        xrbp + (~xNMask & PRIME_X), yrbp + (yNMask & PRIME_Y), zrbp + (~zNMask & PRIME_Z), x7, y7, z7);
            }

            float a8 = yAFlipMask1 + a1;
            if (a8 > 0) {
                float x8 = x1;
                float y8 = (yNMask | 1) + y1;
                float z8 = z1;
                value += (a8 * a8) * (a8 * a8) * grad(seed2,
                        xrbp + PRIME_X, yrbp + (yNMask & (PRIME_Y << 1)), zrbp + PRIME_Z, x8, y8, z8);
                skip9 = true;
            }
        }

        boolean skipD = false;
        float aA = zAFlipMask0 + a0;
        if (aA > 0) {
            float xA = x0;
            float yA = y0;
            float zA = z0 - (zNMask | 1);
            value += (aA * aA) * (aA * aA) * grad(seed,
                    xrbp + (xNMask & PRIME_X), yrbp + (yNMask & PRIME_Y), zrbp + (~zNMask & PRIME_Z), xA, yA, zA);
        }
        else
        {
            float aB = xAFlipMask0 + yAFlipMask0 + a0;
            if (aB > 0) {
                float xB = x0 - (xNMask | 1);
                float yB = y0 - (yNMask | 1);
                float zB = z0;
                value += (aB * aB) * (aB * aB) * grad(seed,
                        xrbp + (~xNMask & PRIME_X), yrbp + (~yNMask & PRIME_Y), zrbp + (zNMask & PRIME_Z), xB, yB, zB);
            }

            float aC = zAFlipMask1 + a1;
            if (aC > 0) {
                float xC = x1;
                float yC = y1;
                float zC = (zNMask | 1) + z1;
                value += (aC * aC) * (aC * aC) * grad(seed2,
                        xrbp + PRIME_X, yrbp + PRIME_Y, zrbp + (zNMask & (PRIME_Z << 1)), xC, yC, zC);
                skipD = true;
            }
        }

        if (!skip5) {
            float a5 = yAFlipMask1 + zAFlipMask1 + a1;
            if (a5 > 0) {
                float x5 = x1;
                float y5 = (yNMask | 1) + y1;
                float z5 = (zNMask | 1) + z1;
                value += (a5 * a5) * (a5 * a5) * grad(seed2,
                        xrbp + PRIME_X, yrbp + (yNMask & (PRIME_Y << 1)), zrbp + (zNMask & (PRIME_Z << 1)), x5, y5, z5);
            }
        }

        if (!skip9) {
            float a9 = xAFlipMask1 + zAFlipMask1 + a1;
            if (a9 > 0) {
                float x9 = (xNMask | 1) + x1;
                float y9 = y1;
                float z9 = (zNMask | 1) + z1;
                value += (a9 * a9) * (a9 * a9) * grad(seed2,
                        xrbp + (xNMask & (PRIME_X * 2)), yrbp + PRIME_Y, zrbp + (zNMask & (PRIME_Z << 1)), x9, y9, z9);
            }
        }

        if (!skipD) {
            float aD = xAFlipMask1 + yAFlipMask1 + a1;
            if (aD > 0) {
                float xD = (xNMask | 1) + x1;
                float yD = (yNMask | 1) + y1;
                float zD = z1;
                value += (aD * aD) * (aD * aD) * grad(seed2,
                        xrbp + (xNMask & (PRIME_X << 1)), yrbp + (yNMask & (PRIME_Y << 1)), zrbp + PRIME_Z, xD, yD, zD);
            }
        }

        return value;
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_ImproveXY
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Z is vertical
     */
    public static float noise4_ImproveXYZ_ImproveXY(long seed, float x, float y, float z, float w) {
        float xy = x + y;
        float s2 = xy * -0.21132486540518699998f;
        float zz = z * 0.28867513459481294226f;
        float ww = w * 1.118033988749894f;
        float xr = x + (zz + ww + s2), yr = y + (zz + ww + s2);
        float zr = xy * -0.57735026918962599998f + (zz + ww);
        float wr = z * -0.866025403784439f + ww;

        return noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_ImproveXZ
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Y is vertical
     */
    public static float noise4_ImproveXYZ_ImproveXZ(long seed, float x, float y, float z, float w) {
        float xz = x + z;
        float s2 = xz * -0.21132486540518699998f;
        float yy = y * 0.28867513459481294226f;
        float ww = w * 1.118033988749894f;
        float xr = x + (yy + ww + s2), zr = z + (yy + ww + s2);
        float yr = xz * -0.57735026918962599998f + (yy + ww);
        float wr = y * -0.866025403784439f + ww;

        return noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    /**
     * 4D SuperSimplex noise, with XYZ oriented like noise3_Fallback
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * where there isn't a clear distinction between horizontal and vertical
     */
    public static float noise4_ImproveXYZ(long seed, float x, float y, float z, float w) {
        float xyz = x + y + z;
        float ww = w * 1.118033988749894f;
        float s2 = xyz * -0.16666666666666666f + ww;
        float xs = x + s2, ys = y + s2, zs = z + s2, ws = -0.5f * xyz + ww;

        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }
    
    /**
     * 4D SuperSimplex noise, with XY and ZW forming orthogonal triangular-based planes.
     * Recommended for 3D terrain, where X and Y (or Z and W) are horizontal.
     * Recommended for noise(x, y, sin(time), cos(time)) trick.
     */
    public static float noise4_ImproveXY_ImproveZW(long seed, float x, float y, float z, float w) {
        
        float s2 = (x + y) * -0.28522513987434876941f + (z + w) * 0.83897065470611435718f;
        float t2 = (z + w) * 0.21939749883706435719f + (x + y) * -0.48214856493302476942f;
        float xs = x + s2, ys = y + s2, zs = z + t2, ws = w + t2;
        
        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    /**
     * 4D SuperSimplex noise, fallback lattice orientation.
     */
    public static float noise4_Fallback(long seed, float x, float y, float z, float w) {

        // Get points for A4 lattice
        float s = SKEW_4D * (x + y + z + w);
        float xs = x + s, ys = y + s, zs = z + s, ws = w + s;

        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    /**
     * 4D SuperSimplex noise base.
     * Using ultra-simple 4x4x4x4 lookup partitioning.
     * This isn't as elegant or SIMD/GPU/etc. portable as other approaches,
     * but it competes performance-wise with optimized 2014 OpenSimplex.
     */
    private static float noise4_UnskewedBase(long seed, float xs, float ys, float zs, float ws) {

        // Get base points and offsets
        int xsb = fastFloor(xs), ysb = fastFloor(ys), zsb = fastFloor(zs), wsb = fastFloor(ws);
        float xsi = xs - xsb, ysi = ys - ysb, zsi = zs - zsb, wsi = ws - wsb;

        // Unskewed offsets
        float ssi = (xsi + ysi + zsi + wsi) * UNSKEW_4D;
        float xi = xsi + ssi, yi = ysi + ssi, zi = zsi + ssi, wi = wsi + ssi;

        // Prime pre-multiplication for hash.
        long xsvp = xsb * PRIME_X, ysvp = ysb * PRIME_Y, zsvp = zsb * PRIME_Z, wsvp = wsb * PRIME_W;

        // Index into initial table.
        int index = ((fastFloor(xs * 4) & 3) << 0)
                | ((fastFloor(ys * 4) & 3) << 2)
                | ((fastFloor(zs * 4) & 3) << 4)
                | ((fastFloor(ws * 4) & 3) << 6);

        // Point contributions
        float value = 0;
        int secondaryIndexStartAndStop = LOOKUP_4D_A[index];
        int secondaryIndexStart = secondaryIndexStartAndStop & 0xFFFF;
        int secondaryIndexStop = secondaryIndexStartAndStop >> 16;
        for (int i = secondaryIndexStart; i < secondaryIndexStop; i++) {
            LatticeVertex4D c = LOOKUP_4D_B[i];
            float dx = xi + c.dx, dy = yi + c.dy, dz = zi + c.dz, dw = wi + c.dw;
            float a = (dx * dx + dy * dy) + (dz * dz + dw * dw);
            if (a < RSQUARED_4D) {
                a -= RSQUARED_4D;
                a *= a;
                value += a * a * grad(seed, xsvp + c.xsvp, ysvp + c.ysvp, zsvp + c.zsvp, wsvp + c.wsvp, dx, dy, dz, dw);
            }
        }
        return value;
    }

    /*
     * Utility
     */

    private static float grad(long seed, long xsvp, long ysvp, float dx, float dy) {
        long hash = seed ^ xsvp ^ ysvp;
        hash *= HASH_MULTIPLIER;
        hash ^= hash >> (64 - N_GRADS_2D_EXPONENT + 1);
        int gi = (int)hash & ((N_GRADS_2D - 1) << 1);
        return GRADIENTS_2D[gi | 0] * dx + GRADIENTS_2D[gi | 1] * dy;
    }

    private static float grad(long seed, long xrvp, long yrvp, long zrvp, float dx, float dy, float dz) {
        long hash = (seed ^ xrvp) ^ (yrvp ^ zrvp);
        hash *= HASH_MULTIPLIER;
        hash ^= hash >> (64 - N_GRADS_3D_EXPONENT + 2);
        int gi = (int)hash & ((N_GRADS_3D - 1) << 2);
        return GRADIENTS_3D[gi | 0] * dx + GRADIENTS_3D[gi | 1] * dy + GRADIENTS_3D[gi | 2] * dz;
    }

    private static float grad(long seed, long xsvp, long ysvp, long zsvp, long wsvp, float dx, float dy, float dz, float dw) {
        long hash = seed ^ (xsvp ^ ysvp) ^ (zsvp ^ wsvp);
        hash *= HASH_MULTIPLIER;
        hash ^= hash >> (64 - N_GRADS_4D_EXPONENT + 2);
        int gi = (int)hash & ((N_GRADS_4D - 1) << 2);
        return (GRADIENTS_4D[gi | 0] * dx + GRADIENTS_4D[gi | 1] * dy) + (GRADIENTS_4D[gi | 2] * dz + GRADIENTS_4D[gi | 3] * dw);
    }

    private static int fastFloor(float x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenSimplex2Smooth that = (OpenSimplex2Smooth) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }

    @Override
    public String toString() {
        return "OpenSimplex2Smooth{" +
                "seed=" + seed +
                '}';
    }

    /*
     * Lookup Tables & Gradients
     */

    private static float[] GRADIENTS_2D;
    private static float[] GRADIENTS_3D;
    private static float[] GRADIENTS_4D;
    private static int[] LOOKUP_4D_A;
    private static LatticeVertex4D[] LOOKUP_4D_B;
    static {

        GRADIENTS_2D = new float[N_GRADS_2D * 2];
        float[] grad2 = {
                0.38268343236509f,   0.923879532511287f,
                0.923879532511287f,  0.38268343236509f,
                0.923879532511287f, -0.38268343236509f,
                0.38268343236509f,  -0.923879532511287f,
                -0.38268343236509f,  -0.923879532511287f,
                -0.923879532511287f, -0.38268343236509f,
                -0.923879532511287f,  0.38268343236509f,
                -0.38268343236509f,   0.923879532511287f,
                //-------------------------------------//
                0.130526192220052f,  0.99144486137381f,
                0.608761429008721f,  0.793353340291235f,
                0.793353340291235f,  0.608761429008721f,
                0.99144486137381f,   0.130526192220051f,
                0.99144486137381f,  -0.130526192220051f,
                0.793353340291235f, -0.60876142900872f,
                0.608761429008721f, -0.793353340291235f,
                0.130526192220052f, -0.99144486137381f,
                -0.130526192220052f, -0.99144486137381f,
                -0.608761429008721f, -0.793353340291235f,
                -0.793353340291235f, -0.608761429008721f,
                -0.99144486137381f,  -0.130526192220052f,
                -0.99144486137381f,   0.130526192220051f,
                -0.793353340291235f,  0.608761429008721f,
                -0.608761429008721f,  0.793353340291235f,
                -0.130526192220052f,  0.99144486137381f,
        };
        for (int i = 0; i < grad2.length; i++) {
            grad2[i] = grad2[i] / NORMALIZER_2D;
        }
        for (int i = 0, j = 0; i < GRADIENTS_2D.length; i++, j++) {
            if (j == grad2.length) j = 0;
            GRADIENTS_2D[i] = grad2[j];
        }

        GRADIENTS_3D = new float[N_GRADS_3D * 4];
        float[] grad3 = {
                2.22474487139f,       2.22474487139f,      -1.0f,                 0.0f,
                2.22474487139f,       2.22474487139f,       1.0f,                 0.0f,
                3.0862664687972017f,  1.1721513422464978f,  0.0f,                 0.0f,
                1.1721513422464978f,  3.0862664687972017f,  0.0f,                 0.0f,
                -2.22474487139f,       2.22474487139f,      -1.0f,                 0.0f,
                -2.22474487139f,       2.22474487139f,       1.0f,                 0.0f,
                -1.1721513422464978f,  3.0862664687972017f,  0.0f,                 0.0f,
                -3.0862664687972017f,  1.1721513422464978f,  0.0f,                 0.0f,
                -1.0f,                -2.22474487139f,      -2.22474487139f,       0.0f,
                1.0f,                -2.22474487139f,      -2.22474487139f,       0.0f,
                0.0f,                -3.0862664687972017f, -1.1721513422464978f,  0.0f,
                0.0f,                -1.1721513422464978f, -3.0862664687972017f,  0.0f,
                -1.0f,                -2.22474487139f,       2.22474487139f,       0.0f,
                1.0f,                -2.22474487139f,       2.22474487139f,       0.0f,
                0.0f,                -1.1721513422464978f,  3.0862664687972017f,  0.0f,
                0.0f,                -3.0862664687972017f,  1.1721513422464978f,  0.0f,
                //--------------------------------------------------------------------//
                -2.22474487139f,      -2.22474487139f,      -1.0f,                 0.0f,
                -2.22474487139f,      -2.22474487139f,       1.0f,                 0.0f,
                -3.0862664687972017f, -1.1721513422464978f,  0.0f,                 0.0f,
                -1.1721513422464978f, -3.0862664687972017f,  0.0f,                 0.0f,
                -2.22474487139f,      -1.0f,                -2.22474487139f,       0.0f,
                -2.22474487139f,       1.0f,                -2.22474487139f,       0.0f,
                -1.1721513422464978f,  0.0f,                -3.0862664687972017f,  0.0f,
                -3.0862664687972017f,  0.0f,                -1.1721513422464978f,  0.0f,
                -2.22474487139f,      -1.0f,                 2.22474487139f,       0.0f,
                -2.22474487139f,       1.0f,                 2.22474487139f,       0.0f,
                -3.0862664687972017f,  0.0f,                 1.1721513422464978f,  0.0f,
                -1.1721513422464978f,  0.0f,                 3.0862664687972017f,  0.0f,
                -1.0f,                 2.22474487139f,      -2.22474487139f,       0.0f,
                1.0f,                 2.22474487139f,      -2.22474487139f,       0.0f,
                0.0f,                 1.1721513422464978f, -3.0862664687972017f,  0.0f,
                0.0f,                 3.0862664687972017f, -1.1721513422464978f,  0.0f,
                -1.0f,                 2.22474487139f,       2.22474487139f,       0.0f,
                1.0f,                 2.22474487139f,       2.22474487139f,       0.0f,
                0.0f,                 3.0862664687972017f,  1.1721513422464978f,  0.0f,
                0.0f,                 1.1721513422464978f,  3.0862664687972017f,  0.0f,
                2.22474487139f,      -2.22474487139f,      -1.0f,                 0.0f,
                2.22474487139f,      -2.22474487139f,       1.0f,                 0.0f,
                1.1721513422464978f, -3.0862664687972017f,  0.0f,                 0.0f,
                3.0862664687972017f, -1.1721513422464978f,  0.0f,                 0.0f,
                2.22474487139f,      -1.0f,                -2.22474487139f,       0.0f,
                2.22474487139f,       1.0f,                -2.22474487139f,       0.0f,
                3.0862664687972017f,  0.0f,                -1.1721513422464978f,  0.0f,
                1.1721513422464978f,  0.0f,                -3.0862664687972017f,  0.0f,
                2.22474487139f,      -1.0f,                 2.22474487139f,       0.0f,
                2.22474487139f,       1.0f,                 2.22474487139f,       0.0f,
                1.1721513422464978f,  0.0f,                 3.0862664687972017f,  0.0f,
                3.0862664687972017f,  0.0f,                 1.1721513422464978f,  0.0f,
        };
        for (int i = 0; i < grad3.length; i++) {
            grad3[i] = grad3[i] / NORMALIZER_3D;
        }
        for (int i = 0, j = 0; i < GRADIENTS_3D.length; i++, j++) {
            if (j == grad3.length) j = 0;
            GRADIENTS_3D[i] = grad3[j];
        }

        GRADIENTS_4D = new float[N_GRADS_4D * 4];
        float[] grad4 = {
                -0.6740059517812944f,   -0.3239847771997537f,   -0.3239847771997537f,    0.5794684678643381f,
                -0.7504883828755602f,   -0.4004672082940195f,    0.15296486218853164f,   0.5029860367700724f,
                -0.7504883828755602f,    0.15296486218853164f,  -0.4004672082940195f,    0.5029860367700724f,
                -0.8828161875373585f,    0.08164729285680945f,   0.08164729285680945f,   0.4553054119602712f,
                -0.4553054119602712f,   -0.08164729285680945f,  -0.08164729285680945f,   0.8828161875373585f,
                -0.5029860367700724f,   -0.15296486218853164f,   0.4004672082940195f,    0.7504883828755602f,
                -0.5029860367700724f,    0.4004672082940195f,   -0.15296486218853164f,   0.7504883828755602f,
                -0.5794684678643381f,    0.3239847771997537f,    0.3239847771997537f,    0.6740059517812944f,
                -0.6740059517812944f,   -0.3239847771997537f,    0.5794684678643381f,   -0.3239847771997537f,
                -0.7504883828755602f,   -0.4004672082940195f,    0.5029860367700724f,    0.15296486218853164f,
                -0.7504883828755602f,    0.15296486218853164f,   0.5029860367700724f,   -0.4004672082940195f,
                -0.8828161875373585f,    0.08164729285680945f,   0.4553054119602712f,    0.08164729285680945f,
                -0.4553054119602712f,   -0.08164729285680945f,   0.8828161875373585f,   -0.08164729285680945f,
                -0.5029860367700724f,   -0.15296486218853164f,   0.7504883828755602f,    0.4004672082940195f,
                -0.5029860367700724f,    0.4004672082940195f,    0.7504883828755602f,   -0.15296486218853164f,
                -0.5794684678643381f,    0.3239847771997537f,    0.6740059517812944f,    0.3239847771997537f,
                -0.6740059517812944f,    0.5794684678643381f,   -0.3239847771997537f,   -0.3239847771997537f,
                -0.7504883828755602f,    0.5029860367700724f,   -0.4004672082940195f,    0.15296486218853164f,
                -0.7504883828755602f,    0.5029860367700724f,    0.15296486218853164f,  -0.4004672082940195f,
                -0.8828161875373585f,    0.4553054119602712f,    0.08164729285680945f,   0.08164729285680945f,
                -0.4553054119602712f,    0.8828161875373585f,   -0.08164729285680945f,  -0.08164729285680945f,
                -0.5029860367700724f,    0.7504883828755602f,   -0.15296486218853164f,   0.4004672082940195f,
                -0.5029860367700724f,    0.7504883828755602f,    0.4004672082940195f,   -0.15296486218853164f,
                -0.5794684678643381f,    0.6740059517812944f,    0.3239847771997537f,    0.3239847771997537f,
                0.5794684678643381f,   -0.6740059517812944f,   -0.3239847771997537f,   -0.3239847771997537f,
                0.5029860367700724f,   -0.7504883828755602f,   -0.4004672082940195f,    0.15296486218853164f,
                0.5029860367700724f,   -0.7504883828755602f,    0.15296486218853164f,  -0.4004672082940195f,
                0.4553054119602712f,   -0.8828161875373585f,    0.08164729285680945f,   0.08164729285680945f,
                0.8828161875373585f,   -0.4553054119602712f,   -0.08164729285680945f,  -0.08164729285680945f,
                0.7504883828755602f,   -0.5029860367700724f,   -0.15296486218853164f,   0.4004672082940195f,
                0.7504883828755602f,   -0.5029860367700724f,    0.4004672082940195f,   -0.15296486218853164f,
                0.6740059517812944f,   -0.5794684678643381f,    0.3239847771997537f,    0.3239847771997537f,
                //------------------------------------------------------------------------------------------//
                -0.753341017856078f,    -0.37968289875261624f,  -0.37968289875261624f,  -0.37968289875261624f,
                -0.7821684431180708f,   -0.4321472685365301f,   -0.4321472685365301f,    0.12128480194602098f,
                -0.7821684431180708f,   -0.4321472685365301f,    0.12128480194602098f,  -0.4321472685365301f,
                -0.7821684431180708f,    0.12128480194602098f,  -0.4321472685365301f,   -0.4321472685365301f,
                -0.8586508742123365f,   -0.508629699630796f,     0.044802370851755174f,  0.044802370851755174f,
                -0.8586508742123365f,    0.044802370851755174f, -0.508629699630796f,     0.044802370851755174f,
                -0.8586508742123365f,    0.044802370851755174f,  0.044802370851755174f, -0.508629699630796f,
                -0.9982828964265062f,   -0.03381941603233842f,  -0.03381941603233842f,  -0.03381941603233842f,
                -0.37968289875261624f,  -0.753341017856078f,    -0.37968289875261624f,  -0.37968289875261624f,
                -0.4321472685365301f,   -0.7821684431180708f,   -0.4321472685365301f,    0.12128480194602098f,
                -0.4321472685365301f,   -0.7821684431180708f,    0.12128480194602098f,  -0.4321472685365301f,
                0.12128480194602098f,  -0.7821684431180708f,   -0.4321472685365301f,   -0.4321472685365301f,
                -0.508629699630796f,    -0.8586508742123365f,    0.044802370851755174f,  0.044802370851755174f,
                0.044802370851755174f, -0.8586508742123365f,   -0.508629699630796f,     0.044802370851755174f,
                0.044802370851755174f, -0.8586508742123365f,    0.044802370851755174f, -0.508629699630796f,
                -0.03381941603233842f,  -0.9982828964265062f,   -0.03381941603233842f,  -0.03381941603233842f,
                -0.37968289875261624f,  -0.37968289875261624f,  -0.753341017856078f,    -0.37968289875261624f,
                -0.4321472685365301f,   -0.4321472685365301f,   -0.7821684431180708f,    0.12128480194602098f,
                -0.4321472685365301f,    0.12128480194602098f,  -0.7821684431180708f,   -0.4321472685365301f,
                0.12128480194602098f,  -0.4321472685365301f,   -0.7821684431180708f,   -0.4321472685365301f,
                -0.508629699630796f,     0.044802370851755174f, -0.8586508742123365f,    0.044802370851755174f,
                0.044802370851755174f, -0.508629699630796f,    -0.8586508742123365f,    0.044802370851755174f,
                0.044802370851755174f,  0.044802370851755174f, -0.8586508742123365f,   -0.508629699630796f,
                -0.03381941603233842f,  -0.03381941603233842f,  -0.9982828964265062f,   -0.03381941603233842f,
                -0.37968289875261624f,  -0.37968289875261624f,  -0.37968289875261624f,  -0.753341017856078f,
                -0.4321472685365301f,   -0.4321472685365301f,    0.12128480194602098f,  -0.7821684431180708f,
                -0.4321472685365301f,    0.12128480194602098f,  -0.4321472685365301f,   -0.7821684431180708f,
                0.12128480194602098f,  -0.4321472685365301f,   -0.4321472685365301f,   -0.7821684431180708f,
                -0.508629699630796f,     0.044802370851755174f,  0.044802370851755174f, -0.8586508742123365f,
                0.044802370851755174f, -0.508629699630796f,     0.044802370851755174f, -0.8586508742123365f,
                0.044802370851755174f,  0.044802370851755174f, -0.508629699630796f,    -0.8586508742123365f,
                -0.03381941603233842f,  -0.03381941603233842f,  -0.03381941603233842f,  -0.9982828964265062f,
                -0.3239847771997537f,   -0.6740059517812944f,   -0.3239847771997537f,    0.5794684678643381f,
                -0.4004672082940195f,   -0.7504883828755602f,    0.15296486218853164f,   0.5029860367700724f,
                0.15296486218853164f,  -0.7504883828755602f,   -0.4004672082940195f,    0.5029860367700724f,
                0.08164729285680945f,  -0.8828161875373585f,    0.08164729285680945f,   0.4553054119602712f,
                -0.08164729285680945f,  -0.4553054119602712f,   -0.08164729285680945f,   0.8828161875373585f,
                -0.15296486218853164f,  -0.5029860367700724f,    0.4004672082940195f,    0.7504883828755602f,
                0.4004672082940195f,   -0.5029860367700724f,   -0.15296486218853164f,   0.7504883828755602f,
                0.3239847771997537f,   -0.5794684678643381f,    0.3239847771997537f,    0.6740059517812944f,
                -0.3239847771997537f,   -0.3239847771997537f,   -0.6740059517812944f,    0.5794684678643381f,
                -0.4004672082940195f,    0.15296486218853164f,  -0.7504883828755602f,    0.5029860367700724f,
                0.15296486218853164f,  -0.4004672082940195f,   -0.7504883828755602f,    0.5029860367700724f,
                0.08164729285680945f,   0.08164729285680945f,  -0.8828161875373585f,    0.4553054119602712f,
                -0.08164729285680945f,  -0.08164729285680945f,  -0.4553054119602712f,    0.8828161875373585f,
                -0.15296486218853164f,   0.4004672082940195f,   -0.5029860367700724f,    0.7504883828755602f,
                0.4004672082940195f,   -0.15296486218853164f,  -0.5029860367700724f,    0.7504883828755602f,
                0.3239847771997537f,    0.3239847771997537f,   -0.5794684678643381f,    0.6740059517812944f,
                -0.3239847771997537f,   -0.6740059517812944f,    0.5794684678643381f,   -0.3239847771997537f,
                -0.4004672082940195f,   -0.7504883828755602f,    0.5029860367700724f,    0.15296486218853164f,
                0.15296486218853164f,  -0.7504883828755602f,    0.5029860367700724f,   -0.4004672082940195f,
                0.08164729285680945f,  -0.8828161875373585f,    0.4553054119602712f,    0.08164729285680945f,
                -0.08164729285680945f,  -0.4553054119602712f,    0.8828161875373585f,   -0.08164729285680945f,
                -0.15296486218853164f,  -0.5029860367700724f,    0.7504883828755602f,    0.4004672082940195f,
                0.4004672082940195f,   -0.5029860367700724f,    0.7504883828755602f,   -0.15296486218853164f,
                0.3239847771997537f,   -0.5794684678643381f,    0.6740059517812944f,    0.3239847771997537f,
                -0.3239847771997537f,   -0.3239847771997537f,    0.5794684678643381f,   -0.6740059517812944f,
                -0.4004672082940195f,    0.15296486218853164f,   0.5029860367700724f,   -0.7504883828755602f,
                0.15296486218853164f,  -0.4004672082940195f,    0.5029860367700724f,   -0.7504883828755602f,
                0.08164729285680945f,   0.08164729285680945f,   0.4553054119602712f,   -0.8828161875373585f,
                -0.08164729285680945f,  -0.08164729285680945f,   0.8828161875373585f,   -0.4553054119602712f,
                -0.15296486218853164f,   0.4004672082940195f,    0.7504883828755602f,   -0.5029860367700724f,
                0.4004672082940195f,   -0.15296486218853164f,   0.7504883828755602f,   -0.5029860367700724f,
                0.3239847771997537f,    0.3239847771997537f,    0.6740059517812944f,   -0.5794684678643381f,
                -0.3239847771997537f,    0.5794684678643381f,   -0.6740059517812944f,   -0.3239847771997537f,
                -0.4004672082940195f,    0.5029860367700724f,   -0.7504883828755602f,    0.15296486218853164f,
                0.15296486218853164f,   0.5029860367700724f,   -0.7504883828755602f,   -0.4004672082940195f,
                0.08164729285680945f,   0.4553054119602712f,   -0.8828161875373585f,    0.08164729285680945f,
                -0.08164729285680945f,   0.8828161875373585f,   -0.4553054119602712f,   -0.08164729285680945f,
                -0.15296486218853164f,   0.7504883828755602f,   -0.5029860367700724f,    0.4004672082940195f,
                0.4004672082940195f,    0.7504883828755602f,   -0.5029860367700724f,   -0.15296486218853164f,
                0.3239847771997537f,    0.6740059517812944f,   -0.5794684678643381f,    0.3239847771997537f,
                -0.3239847771997537f,    0.5794684678643381f,   -0.3239847771997537f,   -0.6740059517812944f,
                -0.4004672082940195f,    0.5029860367700724f,    0.15296486218853164f,  -0.7504883828755602f,
                0.15296486218853164f,   0.5029860367700724f,   -0.4004672082940195f,   -0.7504883828755602f,
                0.08164729285680945f,   0.4553054119602712f,    0.08164729285680945f,  -0.8828161875373585f,
                -0.08164729285680945f,   0.8828161875373585f,   -0.08164729285680945f,  -0.4553054119602712f,
                -0.15296486218853164f,   0.7504883828755602f,    0.4004672082940195f,   -0.5029860367700724f,
                0.4004672082940195f,    0.7504883828755602f,   -0.15296486218853164f,  -0.5029860367700724f,
                0.3239847771997537f,    0.6740059517812944f,    0.3239847771997537f,   -0.5794684678643381f,
                0.5794684678643381f,   -0.3239847771997537f,   -0.6740059517812944f,   -0.3239847771997537f,
                0.5029860367700724f,   -0.4004672082940195f,   -0.7504883828755602f,    0.15296486218853164f,
                0.5029860367700724f,    0.15296486218853164f,  -0.7504883828755602f,   -0.4004672082940195f,
                0.4553054119602712f,    0.08164729285680945f,  -0.8828161875373585f,    0.08164729285680945f,
                0.8828161875373585f,   -0.08164729285680945f,  -0.4553054119602712f,   -0.08164729285680945f,
                0.7504883828755602f,   -0.15296486218853164f,  -0.5029860367700724f,    0.4004672082940195f,
                0.7504883828755602f,    0.4004672082940195f,   -0.5029860367700724f,   -0.15296486218853164f,
                0.6740059517812944f,    0.3239847771997537f,   -0.5794684678643381f,    0.3239847771997537f,
                0.5794684678643381f,   -0.3239847771997537f,   -0.3239847771997537f,   -0.6740059517812944f,
                0.5029860367700724f,   -0.4004672082940195f,    0.15296486218853164f,  -0.7504883828755602f,
                0.5029860367700724f,    0.15296486218853164f,  -0.4004672082940195f,   -0.7504883828755602f,
                0.4553054119602712f,    0.08164729285680945f,   0.08164729285680945f,  -0.8828161875373585f,
                0.8828161875373585f,   -0.08164729285680945f,  -0.08164729285680945f,  -0.4553054119602712f,
                0.7504883828755602f,   -0.15296486218853164f,   0.4004672082940195f,   -0.5029860367700724f,
                0.7504883828755602f,    0.4004672082940195f,   -0.15296486218853164f,  -0.5029860367700724f,
                0.6740059517812944f,    0.3239847771997537f,    0.3239847771997537f,   -0.5794684678643381f,
                0.03381941603233842f,   0.03381941603233842f,   0.03381941603233842f,   0.9982828964265062f,
                -0.044802370851755174f, -0.044802370851755174f,  0.508629699630796f,     0.8586508742123365f,
                -0.044802370851755174f,  0.508629699630796f,    -0.044802370851755174f,  0.8586508742123365f,
                -0.12128480194602098f,   0.4321472685365301f,    0.4321472685365301f,    0.7821684431180708f,
                0.508629699630796f,    -0.044802370851755174f, -0.044802370851755174f,  0.8586508742123365f,
                0.4321472685365301f,   -0.12128480194602098f,   0.4321472685365301f,    0.7821684431180708f,
                0.4321472685365301f,    0.4321472685365301f,   -0.12128480194602098f,   0.7821684431180708f,
                0.37968289875261624f,   0.37968289875261624f,   0.37968289875261624f,   0.753341017856078f,
                0.03381941603233842f,   0.03381941603233842f,   0.9982828964265062f,    0.03381941603233842f,
                -0.044802370851755174f,  0.044802370851755174f,  0.8586508742123365f,    0.508629699630796f,
                -0.044802370851755174f,  0.508629699630796f,     0.8586508742123365f,   -0.044802370851755174f,
                -0.12128480194602098f,   0.4321472685365301f,    0.7821684431180708f,    0.4321472685365301f,
                0.508629699630796f,    -0.044802370851755174f,  0.8586508742123365f,   -0.044802370851755174f,
                0.4321472685365301f,   -0.12128480194602098f,   0.7821684431180708f,    0.4321472685365301f,
                0.4321472685365301f,    0.4321472685365301f,    0.7821684431180708f,   -0.12128480194602098f,
                0.37968289875261624f,   0.37968289875261624f,   0.753341017856078f,     0.37968289875261624f,
                0.03381941603233842f,   0.9982828964265062f,    0.03381941603233842f,   0.03381941603233842f,
                -0.044802370851755174f,  0.8586508742123365f,   -0.044802370851755174f,  0.508629699630796f,
                -0.044802370851755174f,  0.8586508742123365f,    0.508629699630796f,    -0.044802370851755174f,
                -0.12128480194602098f,   0.7821684431180708f,    0.4321472685365301f,    0.4321472685365301f,
                0.508629699630796f,     0.8586508742123365f,   -0.044802370851755174f, -0.044802370851755174f,
                0.4321472685365301f,    0.7821684431180708f,   -0.12128480194602098f,   0.4321472685365301f,
                0.4321472685365301f,    0.7821684431180708f,    0.4321472685365301f,   -0.12128480194602098f,
                0.37968289875261624f,   0.753341017856078f,     0.37968289875261624f,   0.37968289875261624f,
                0.9982828964265062f,    0.03381941603233842f,   0.03381941603233842f,   0.03381941603233842f,
                0.8586508742123365f,   -0.044802370851755174f, -0.044802370851755174f,  0.508629699630796f,
                0.8586508742123365f,   -0.044802370851755174f,  0.508629699630796f,    -0.044802370851755174f,
                0.7821684431180708f,   -0.12128480194602098f,   0.4321472685365301f,    0.4321472685365301f,
                0.8586508742123365f,    0.508629699630796f,    -0.044802370851755174f, -0.044802370851755174f,
                0.7821684431180708f,    0.4321472685365301f,   -0.12128480194602098f,   0.4321472685365301f,
                0.7821684431180708f,    0.4321472685365301f,    0.4321472685365301f,   -0.12128480194602098f,
                0.753341017856078f,     0.37968289875261624f,   0.37968289875261624f,   0.37968289875261624f,
        };
        for (int i = 0; i < grad4.length; i++) {
            grad4[i] = grad4[i] / NORMALIZER_4D;
        }
        for (int i = 0, j = 0; i < GRADIENTS_4D.length; i++, j++) {
            if (j == grad4.length) j = 0;
            GRADIENTS_4D[i] = grad4[j];
        }

        int[][] lookup4DVertexCodes = {
                new int[] { 0x15, 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
                new int[] { 0x01, 0x05, 0x11, 0x15, 0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA },
                new int[] { 0x01, 0x15, 0x16, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
                new int[] { 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
                new int[] { 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA },
                new int[] { 0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xAA, 0xAB },
                new int[] { 0x04, 0x05, 0x14, 0x15, 0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA },
                new int[] { 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA },
                new int[] { 0x05, 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA },
                new int[] { 0x05, 0x15, 0x16, 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x5B, 0x6A, 0x9A, 0xAA, 0xAB },
                new int[] { 0x04, 0x15, 0x19, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xAA, 0xAE },
                new int[] { 0x05, 0x15, 0x19, 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x5E, 0x6A, 0x9A, 0xAA, 0xAE },
                new int[] { 0x05, 0x15, 0x1A, 0x45, 0x4A, 0x55, 0x56, 0x59, 0x5A, 0x5B, 0x5E, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x11, 0x15, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
                new int[] { 0x11, 0x15, 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA },
                new int[] { 0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA6, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x15, 0x16, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x6B, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xAA },
                new int[] { 0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x6B, 0x9A, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x19, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x6E, 0x9A, 0xAA, 0xAE },
                new int[] { 0x15, 0x1A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x10, 0x11, 0x14, 0x15, 0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA },
                new int[] { 0x11, 0x15, 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA },
                new int[] { 0x11, 0x15, 0x16, 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x67, 0x6A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA },
                new int[] { 0x15, 0x16, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x6B, 0xA6, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA },
                new int[] { 0x15, 0x16, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0xAA, 0xAB },
                new int[] { 0x14, 0x15, 0x19, 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x6D, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x19, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x6E, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x19, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0xAA, 0xAE },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x6B, 0x6E, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x10, 0x15, 0x25, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xAA, 0xBA },
                new int[] { 0x11, 0x15, 0x25, 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0x76, 0xA6, 0xAA, 0xBA },
                new int[] { 0x11, 0x15, 0x26, 0x51, 0x55, 0x56, 0x62, 0x65, 0x66, 0x67, 0x6A, 0x76, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x25, 0x55, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x25, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA6, 0xAA, 0xBA },
                new int[] { 0x15, 0x26, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x14, 0x15, 0x25, 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0x79, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x25, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x25, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x7A, 0xAA, 0xBA },
                new int[] { 0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0x7A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x14, 0x15, 0x29, 0x54, 0x55, 0x59, 0x65, 0x68, 0x69, 0x6A, 0x6D, 0x79, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x15, 0x29, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6E, 0x7A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x6B, 0x6E, 0x7A, 0xAA, 0xAB, 0xAE, 0xBA, 0xBF },
                new int[] { 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
                new int[] { 0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x45, 0x46, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
                new int[] { 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x9A, 0x9B, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x49, 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x99, 0x9A, 0x9E, 0xAA, 0xAE },
                new int[] { 0x45, 0x4A, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x59, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB },
                new int[] { 0x51, 0x52, 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x56, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x15, 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x45, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x58, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE },
                new int[] { 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
                new int[] { 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA6, 0xA7, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA },
                new int[] { 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
                new int[] { 0x15, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA9, 0xAA, 0xAD, 0xAE },
                new int[] { 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x66, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x51, 0x55, 0x61, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x6A, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x62, 0x65, 0x66, 0x6A, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x64, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA },
                new int[] { 0x55, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x55, 0x56, 0x65, 0x66, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x6A, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA },
                new int[] { 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x15, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x68, 0x69, 0x6A, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE },
                new int[] { 0x55, 0x59, 0x65, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x15, 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0xAA, 0xAB, 0xAE, 0xBA, 0xBF },
                new int[] { 0x40, 0x41, 0x44, 0x45, 0x50, 0x51, 0x54, 0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA },
                new int[] { 0x41, 0x45, 0x46, 0x51, 0x52, 0x55, 0x56, 0x95, 0x96, 0x97, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA },
                new int[] { 0x45, 0x46, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA },
                new int[] { 0x45, 0x46, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xAA, 0xAB },
                new int[] { 0x44, 0x45, 0x49, 0x54, 0x55, 0x58, 0x59, 0x95, 0x99, 0x9A, 0x9D, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x49, 0x55, 0x59, 0x5A, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x49, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xAA, 0xAE },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA },
                new int[] { 0x51, 0x52, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x51, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xEA },
                new int[] { 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA },
                new int[] { 0x45, 0x54, 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x58, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE },
                new int[] { 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA },
                new int[] { 0x51, 0x52, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x54, 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x51, 0x55, 0x56, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB },
                new int[] { 0x54, 0x55, 0x58, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE },
                new int[] { 0x54, 0x55, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x66, 0x69, 0x6A, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xAF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x61, 0x64, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB5, 0xBA },
                new int[] { 0x51, 0x55, 0x61, 0x65, 0x66, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x61, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xB6, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x64, 0x65, 0x69, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA },
                new int[] { 0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x6A, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x59, 0x64, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xB9, 0xBA },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x55, 0x56, 0x59, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA },
                new int[] { 0x55, 0x56, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xBB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xBE },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x6A, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x55, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xBE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xBA },
                new int[] { 0x40, 0x45, 0x51, 0x54, 0x55, 0x85, 0x91, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xAA, 0xEA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x85, 0x91, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xD6, 0xEA },
                new int[] { 0x41, 0x45, 0x51, 0x55, 0x56, 0x86, 0x92, 0x95, 0x96, 0x97, 0x9A, 0xA6, 0xAA, 0xAB, 0xD6, 0xEA, 0xEB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x55, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xDA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xDA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x86, 0x95, 0x96, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x85, 0x94, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xD9, 0xEA },
                new int[] { 0x45, 0x55, 0x59, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xDA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x85, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xDA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0xA6, 0xAA, 0xAB, 0xDA, 0xEA, 0xEB },
                new int[] { 0x44, 0x45, 0x54, 0x55, 0x59, 0x89, 0x95, 0x98, 0x99, 0x9A, 0x9D, 0xA9, 0xAA, 0xAE, 0xD9, 0xEA, 0xEE },
                new int[] { 0x45, 0x55, 0x59, 0x89, 0x95, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE },
                new int[] { 0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9E, 0xA9, 0xAA, 0xAE, 0xDA, 0xEA, 0xEE },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0x9B, 0x9E, 0xAA, 0xAB, 0xAE, 0xDA, 0xEA, 0xEF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0x96, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x51, 0x55, 0x91, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x91, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x92, 0x95, 0x96, 0x9A, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x94, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA },
                new int[] { 0x55, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x55, 0x56, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x55, 0x56, 0x95, 0x96, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x59, 0x94, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA },
                new int[] { 0x55, 0x59, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x45, 0x55, 0x56, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x59, 0x95, 0x98, 0x99, 0x9A, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE },
                new int[] { 0x55, 0x59, 0x95, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
                new int[] { 0x45, 0x55, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x95, 0x96, 0x99, 0x9A, 0xAA, 0xAB, 0xAE, 0xEA, 0xEF },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x91, 0x94, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xE5, 0xEA },
                new int[] { 0x51, 0x55, 0x65, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xE6, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x91, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xE6, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xE6, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x65, 0x94, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xE9, 0xEA },
                new int[] { 0x55, 0x65, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x51, 0x55, 0x56, 0x66, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xAA, 0xAB, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x94, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xE9, 0xEA },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x55, 0x56, 0x59, 0x65, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xEA },
                new int[] { 0x55, 0x56, 0x5A, 0x66, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xEA, 0xEB },
                new int[] { 0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xE9, 0xEA, 0xEE },
                new int[] { 0x54, 0x55, 0x59, 0x69, 0x95, 0x99, 0x9A, 0xA5, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
                new int[] { 0x55, 0x59, 0x5A, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xEA, 0xEE },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xEA },
                new int[] { 0x50, 0x51, 0x54, 0x55, 0x65, 0x95, 0xA1, 0xA4, 0xA5, 0xA6, 0xA9, 0xAA, 0xB5, 0xBA, 0xE5, 0xEA, 0xFA },
                new int[] { 0x51, 0x55, 0x65, 0x95, 0xA1, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA },
                new int[] { 0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xB6, 0xBA, 0xE6, 0xEA, 0xFA },
                new int[] { 0x51, 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA7, 0xAA, 0xAB, 0xB6, 0xBA, 0xE6, 0xEA, 0xFB },
                new int[] { 0x54, 0x55, 0x65, 0x95, 0xA4, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA },
                new int[] { 0x55, 0x65, 0x95, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
                new int[] { 0x51, 0x55, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
                new int[] { 0x55, 0x56, 0x65, 0x66, 0x95, 0x96, 0xA5, 0xA6, 0xAA, 0xAB, 0xBA, 0xEA, 0xFB },
                new int[] { 0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xB9, 0xBA, 0xE9, 0xEA, 0xFA },
                new int[] { 0x54, 0x55, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
                new int[] { 0x55, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xBA, 0xEA, 0xFA },
                new int[] { 0x55, 0x56, 0x65, 0x66, 0x6A, 0x95, 0x96, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xBA, 0xEA },
                new int[] { 0x54, 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAD, 0xAE, 0xB9, 0xBA, 0xE9, 0xEA, 0xFE },
                new int[] { 0x55, 0x59, 0x65, 0x69, 0x95, 0x99, 0xA5, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA, 0xFE },
                new int[] { 0x55, 0x59, 0x65, 0x69, 0x6A, 0x95, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAE, 0xBA, 0xEA },
                new int[] { 0x55, 0x56, 0x59, 0x5A, 0x65, 0x66, 0x69, 0x6A, 0x95, 0x96, 0x99, 0x9A, 0xA5, 0xA6, 0xA9, 0xAA, 0xAB, 0xAE, 0xBA, 0xEA },
        };
        LatticeVertex4D[] latticeVerticesByCode = new LatticeVertex4D[256];
        for (int i = 0; i < 256; i++) {
            int cx = ((i >> 0) & 3) - 1;
            int cy = ((i >> 2) & 3) - 1;
            int cz = ((i >> 4) & 3) - 1;
            int cw = ((i >> 6) & 3) - 1;
            latticeVerticesByCode[i] = new LatticeVertex4D(cx, cy, cz, cw);
        }
        int nLatticeVerticesTotal = 0;
        for (int i = 0; i < 256; i++) {
            nLatticeVerticesTotal += lookup4DVertexCodes[i].length;
        }
        LOOKUP_4D_A = new int[256];
        LOOKUP_4D_B = new LatticeVertex4D[nLatticeVerticesTotal];
        for (int i = 0, j = 0; i < 256; i++) {
            LOOKUP_4D_A[i] = j | ((j + lookup4DVertexCodes[i].length) << 16);
            for (int k = 0; k < lookup4DVertexCodes[i].length; k++) {
                LOOKUP_4D_B[j++] = latticeVerticesByCode[lookup4DVertexCodes[i][k]];
            }
        }
    }

    private static class LatticeVertex4D {
        public final float dx, dy, dz, dw;
        public final long xsvp, ysvp, zsvp, wsvp;
        public LatticeVertex4D(int xsv, int ysv, int zsv, int wsv) {
            this.xsvp = xsv * PRIME_X; this.ysvp = ysv * PRIME_Y;
            this.zsvp = zsv * PRIME_Z; this.wsvp = wsv * PRIME_W;
            float ssv = (xsv + ysv + zsv + wsv) * UNSKEW_4D;
            this.dx = -xsv - ssv;
            this.dy = -ysv - ssv;
            this.dz = -zsv - ssv;
            this.dw = -wsv - ssv;
        }
    }
}