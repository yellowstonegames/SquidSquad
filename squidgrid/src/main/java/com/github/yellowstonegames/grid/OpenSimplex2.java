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
 * OpenSimplex2.java was originally by KdotJPG, who released it into the public domain.
 * Original license: https://github.com/KdotJPG/OpenSimplex2/blob/45cc32d5bf09ec89732ea108669417b6cad4546d/LICENSE
 */

package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.DigitTools;

/**
 * K.jpg's OpenSimplex 2, faster variant.
 * This is a variant on the Simplex Noise algorithm in 2D, and a subtly different algorithm in 3D and 4D. This does not
 * support 5D or 6D noise. This is very similar to {@link OpenSimplex2Smooth}, but has more separate "spots" or
 * "surflets" with noticeable gaps between areas of similar results. It is faster than OpenSimplex2Smooth, though.
 * <br>
 * OpenSimplex2.java was originally by KdotJPG, who released it into the public domain.
 * <a href="https://github.com/KdotJPG/OpenSimplex2/blob/45cc32d5bf09ec89732ea108669417b6cad4546d/LICENSE">Original license</a>.
 */
public class OpenSimplex2 implements INoise {
    public long seed;

    public OpenSimplex2() {
        seed = 0x31337CA770E5L;
    }

    public OpenSimplex2(long seed) {
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
    public OpenSimplex2 copy() {
        return new OpenSimplex2(seed);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public OpenSimplex2 stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = DigitTools.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static OpenSimplex2 recreateFromString(String data) {
        return new OpenSimplex2(DigitTools.longFromDec(data, 1, data.length() - 1));
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
        return "OSFN";
    }



    private static final long PRIME_X = 0x5205402B9270C86FL;
    private static final long PRIME_Y = 0x598CD327003817B5L;
    private static final long PRIME_Z = 0x5BCC226E9FA0BACBL;
    private static final long PRIME_W = 0x56CC5227E58F554BL;
    private static final long HASH_MULTIPLIER = 0x53A3F72DEEC546F5L;
    private static final long SEED_FLIP_3D = -0x52D547B2E96ED629L;
    private static final long SEED_OFFSET_4D = 0xE83DC3E0DA7164DL;

    private static final float ROOT2OVER2 = 0.7071067811865476F;
    private static final float SKEW_2D = 0.366025403784439F;
    private static final float UNSKEW_2D = -0.21132486540518713F;

    private static final float ROOT3OVER3 = 0.577350269189626F;
    private static final float FALLBACK_ROTATE_3D = (2F / 3F);
    private static final float ROTATE_3D_ORTHOGONALIZER = UNSKEW_2D;

    private static final float SKEW_4D = -0.138196601125011f;
    private static final float UNSKEW_4D = 0.309016994374947f;
    private static final float LATTICE_STEP_4D = 0.2f;

    private static final int N_GRADS_2D_EXPONENT = 7;
    private static final int N_GRADS_3D_EXPONENT = 8;
    private static final int N_GRADS_4D_EXPONENT = 9;
    private static final int N_GRADS_2D = 1 << N_GRADS_2D_EXPONENT;
    private static final int N_GRADS_3D = 1 << N_GRADS_3D_EXPONENT;
    private static final int N_GRADS_4D = 1 << N_GRADS_4D_EXPONENT;

    private static final float NORMALIZER_2D = 0.01001634121365712F;
    private static final float NORMALIZER_3D = 0.07969837668935331F;
    private static final float NORMALIZER_4D = 0.0220065933241897F;

    private static final float RSQUARED_2D = 0.5f;
    private static final float RSQUARED_3D = 0.6f;
    private static final float RSQUARED_4D = 0.6f;


    /*
     * Noise Evaluators
     */

    /**
     * 2D Simplex noise, standard lattice orientation.
     */
    public static float noise2(long seed, float x, float y) {

        // Get points for A2* lattice
        float s = SKEW_2D * (x + y);
        float xs = x + s, ys = y + s;

        return noise2_UnskewedBase(seed, xs, ys);
    }

    /**
     * 2D Simplex noise, with Y pointing down the main diagonal.
     * Might be better for a 2D sandbox style game, where Y is vertical.
     * Probably slightly less optimal for heightmaps or continent maps,
     * unless your map is centered around an equator. It's a subtle
     * difference, but the option is here to make it an easy choice.
     */
    public static float noise2_ImproveX(long seed, float x, float y) {

        // Skew transform and rotation baked into one.
        float xx = x * ROOT2OVER2;
        float yy = y * (ROOT2OVER2 * (1 + 2 * SKEW_2D));

        return noise2_UnskewedBase(seed, yy + xx, yy - xx);
    }

    /**
     * 2D Simplex noise base.
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
        float value = 0;
        float a0 = RSQUARED_2D - dx0 * dx0 - dy0 * dy0;
        if (a0 > 0) {
            value = (a0 * a0) * (a0 * a0) * grad(seed, xsbp, ysbp, dx0, dy0);
        }

        // Second vertex.
        float a1 = 2 * (1 + 2 * UNSKEW_2D) * (1 / UNSKEW_2D + 2) * t + ((-2 * (1 + 2 * UNSKEW_2D) * (1 + 2 * UNSKEW_2D)) + a0);
        if (a1 > 0) {
            float dx1 = dx0 - (1 + 2 * UNSKEW_2D);
            float dy1 = dy0 - (1 + 2 * UNSKEW_2D);
            value += (a1 * a1) * (a1 * a1) * grad(seed, xsbp + PRIME_X, ysbp + PRIME_Y, dx1, dy1);
        }

        // Third vertex.
        if (dy0 > dx0) {
            float dx2 = dx0 - UNSKEW_2D;
            float dy2 = dy0 - (UNSKEW_2D + 1);
            float a2 = RSQUARED_2D - dx2 * dx2 - dy2 * dy2;
            if (a2 > 0) {
                value += (a2 * a2) * (a2 * a2) * grad(seed, xsbp, ysbp + PRIME_Y, dx2, dy2);
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

        return value;
    }

    /**
     * 3D OpenSimplex2 noise, with better visual isotropy in (X, Y).
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
        float s2 = xy * ROTATE_3D_ORTHOGONALIZER;
        float zz = z * ROOT3OVER3;
        float xr = x + s2 + zz;
        float yr = y + s2 + zz;
        float zr = xy * -ROOT3OVER3 + zz;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * 3D OpenSimplex2 noise, with better visual isotropy in (X, Z).
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
        float s2 = xz * ROTATE_3D_ORTHOGONALIZER;
        float yy = y * ROOT3OVER3;
        float xr = x + s2 + yy;
        float zr = z + s2 + yy;
        float yr = xz * -ROOT3OVER3 + yy;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * 3D OpenSimplex2 noise, fallback rotation option
     * Use noise3_ImproveXY or noise3_ImproveXZ instead, wherever appropriate.
     * They have less diagonal bias. This function's best use is as a fallback.
     */
    public static float noise3_Fallback(long seed, float x, float y, float z) {

        // Re-orient the cubic lattices via rotation, to produce a familiar look.
        // Orthonormal rotation. Not a skew transform.
        float r = FALLBACK_ROTATE_3D * (x + y + z);
        float xr = r - x, yr = r - y, zr = r - z;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * Generate overlapping cubic lattices for 3D OpenSimplex2 noise.
     */
    private static float noise3_UnrotatedBase(long seed, float xr, float yr, float zr) {

        // Get base points and offsets.
        int xrb = fastRound(xr), yrb = fastRound(yr), zrb = fastRound(zr);
        float xri = xr - xrb, yri = yr - yrb, zri = zr - zrb;

        // -1 if positive, 1 if negative.
        int xNSign = (int)(-1.0f - xri) | 1, yNSign = (int)(-1.0f - yri) | 1, zNSign = (int)(-1.0f - zri) | 1;

        // Compute absolute values, using the above as a shortcut. This was faster in my tests for some reason.
        float ax0 = xNSign * -xri, ay0 = yNSign * -yri, az0 = zNSign * -zri;

        // Prime pre-multiplication for hash.
        long xrbp = xrb * PRIME_X, yrbp = yrb * PRIME_Y, zrbp = zrb * PRIME_Z;

        // Loop: Pick an edge on each lattice copy.
        float value = 0;
        float a = (RSQUARED_3D - xri * xri) - (yri * yri + zri * zri);
        for (int l = 0; ; l++) {

            // Closest point on cube.
            if (a > 0) {
                value += (a * a) * (a * a) * grad(seed, xrbp, yrbp, zrbp, xri, yri, zri);
            }

            // Second-closest point.
            if (ax0 >= ay0 && ax0 >= az0) {
                float b = a + ax0 + ax0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * grad(seed, xrbp - xNSign * PRIME_X, yrbp, zrbp, xri + xNSign, yri, zri);
                }
            }
            else if (ay0 > ax0 && ay0 >= az0) {
                float b = a + ay0 + ay0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * grad(seed, xrbp, yrbp - yNSign * PRIME_Y, zrbp, xri, yri + yNSign, zri);
                }
            }
            else
            {
                float b = a + az0 + az0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * grad(seed, xrbp, yrbp, zrbp - zNSign * PRIME_Z, xri, yri, zri + zNSign);
                }
            }

            // Break from loop if we're done, skipping updates below.
            if (l == 1) break;

            // Update absolute value.
            ax0 = 0.5f - ax0;
            ay0 = 0.5f - ay0;
            az0 = 0.5f - az0;

            // Update relative coordinate.
            xri = xNSign * ax0;
            yri = yNSign * ay0;
            zri = zNSign * az0;

            // Update falloff.
            a += (0.75f - ax0) - (ay0 + az0);

            // Update prime for hash.
            xrbp += (xNSign >> 1) & PRIME_X;
            yrbp += (yNSign >> 1) & PRIME_Y;
            zrbp += (zNSign >> 1) & PRIME_Z;

            // Update the reverse sign indicators.
            xNSign = -xNSign;
            yNSign = -yNSign;
            zNSign = -zNSign;

            // And finally update the seed for the other lattice copy.
            seed ^= SEED_FLIP_3D;
        }

        return value;
    }

    /**
     * 4D OpenSimplex2 noise, with XYZ oriented like noise3_ImproveXY
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Z is vertical
     */
    public static float noise4_ImproveXYZ_ImproveXY(long seed, float x, float y, float z, float w) {

        float xy = x + y;
        float s2 = xy * -0.21132486540518699998f;
        float zz = z * 0.28867513459481294226f;
        float ww = w * 0.2236067977499788f;
        float xr = x + (zz + ww + s2), yr = y + (zz + ww + s2);
        float zr = xy * -0.57735026918962599998f + (zz + ww);
        float wr = z * -0.866025403784439f + ww;

        return noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    /**
     * 4D OpenSimplex2 noise, with XYZ oriented like noise3_ImproveXZ
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * in a space where Y is vertical
     */
    public static float noise4_ImproveXYZ_ImproveXZ(long seed, float x, float y, float z, float w) {

        float xz = x + z;
        float s2 = xz * -0.21132486540518699998f;
        float yy = y * 0.28867513459481294226f;
        float ww = w * 0.2236067977499788f;
        float xr = x + (yy + ww + s2), zr = z + (yy + ww + s2);
        float yr = xz * -0.57735026918962599998f + (yy + ww);
        float wr = y * -0.866025403784439f + ww;

        return noise4_UnskewedBase(seed, xr, yr, zr, wr);
    }

    /**
     * 4D OpenSimplex2 noise, with XYZ oriented like noise3_Fallback
     * and W for an extra degree of freedom. W repeats eventually.
     * Recommended for time-varied animations which texture a 3D object (W=time)
     * where there isn't a clear distinction between horizontal and vertical
     */
    public static float noise4_ImproveXYZ(long seed, float x, float y, float z, float w) {

        float xyz = x + y + z;
        float ww = w * 0.2236067977499788f;
        float s2 = xyz * -0.16666666666666666f + ww;
        float xs = x + s2, ys = y + s2, zs = z + s2, ws = (float) (-0.5f * xyz + ww);

        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }
    
    /**
     * 4D OpenSimplex2 noise, with XY and ZW forming orthogonal triangular-based planes.
     * Recommended for 3D terrain, where X and Y (or Z and W) are horizontal.
     * Recommended for noise(x, y, sin(time), cos(time)) trick.
     */
    public static float noise4_ImproveXY_ImproveZW(long seed, float x, float y, float z, float w) {
        
        float s2 = (x + y) * -0.178275657951399372f + (z + w) * 0.215623393288842828f;
        float t2 = (z + w) * -0.403949762580207112f + (x + y) * -0.375199083010075342f;
        float xs = x + s2, ys = y + s2, zs = z + t2, ws = w + t2;
        
        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    /**
     * 4D OpenSimplex2 noise, fallback lattice orientation.
     */
    public static float noise4_Fallback(long seed, float x, float y, float z, float w) {

        // Get points for A4 lattice
        float s = SKEW_4D * (x + y + z + w);
        float xs = x + s, ys = y + s, zs = z + s, ws = w + s;

        return noise4_UnskewedBase(seed, xs, ys, zs, ws);
    }

    /**
     * 4D OpenSimplex2 noise base.
     */
    private static float noise4_UnskewedBase(long seed, float xs, float ys, float zs, float ws) {

        // Get base points and offsets
        int xsb = fastFloor(xs), ysb = fastFloor(ys), zsb = fastFloor(zs), wsb = fastFloor(ws);
        float xsi = xs - xsb, ysi = ys - ysb, zsi = zs - zsb, wsi = ws - wsb;

        // Determine which lattice we can be confident has a contributing point its corresponding cell's base simplex.
        // We only look at the spaces between the diagonal planes. This proved effective in all of my tests.
        float siSum = (xsi + ysi) + (zsi + wsi);
        int startingLattice = (int)(siSum * 1.25f);

        // Offset for seed based on first lattice copy.
        seed += startingLattice * SEED_OFFSET_4D;

        // Offset for lattice point relative positions (skewed)
        float startingLatticeOffset = startingLattice * -LATTICE_STEP_4D;
        xsi += startingLatticeOffset; ysi += startingLatticeOffset; zsi += startingLatticeOffset; wsi += startingLatticeOffset;

        // Prep for vertex contributions.
        float ssi = (siSum + startingLatticeOffset * 4) * UNSKEW_4D;

        // Prime pre-multiplication for hash.
        long xsvp = xsb * PRIME_X, ysvp = ysb * PRIME_Y, zsvp = zsb * PRIME_Z, wsvp = wsb * PRIME_W;

        // Five points to add, total, from five copies of the A4 lattice.
        float value = 0;
        for (int i = 0; ; i++) {

            // Next point is the closest vertex on the 4-simplex whose base vertex is the aforementioned vertex.
            float score0 = 1.0f + ssi * (-1.0f / UNSKEW_4D); // Seems slightly faster than 1.0-xsi-ysi-zsi-wsi
            if (xsi >= ysi && xsi >= zsi && xsi >= wsi && xsi >= score0) {
                xsvp += PRIME_X;
                xsi -= 1;
                ssi -= UNSKEW_4D;
            }
            else if (ysi > xsi && ysi >= zsi && ysi >= wsi && ysi >= score0) {
                ysvp += PRIME_Y;
                ysi -= 1;
                ssi -= UNSKEW_4D;
            }
            else if (zsi > xsi && zsi > ysi && zsi >= wsi && zsi >= score0) {
                zsvp += PRIME_Z;
                zsi -= 1;
                ssi -= UNSKEW_4D;
            }
            else if (wsi > xsi && wsi > ysi && wsi > zsi && wsi >= score0) {
                wsvp += PRIME_W;
                wsi -= 1;
                ssi -= UNSKEW_4D;
            }

            // gradient contribution with falloff.
            float dx = xsi + ssi, dy = ysi + ssi, dz = zsi + ssi, dw = wsi + ssi;
            float a = (dx * dx + dy * dy) + (dz * dz + dw * dw);
            if (a < RSQUARED_4D) {
                a -= RSQUARED_4D;
                a *= a;
                value += a * a * grad(seed, xsvp, ysvp, zsvp, wsvp, dx, dy, dz, dw);
            }

            // Break from loop if we're done, skipping updates below.
            if (i == 4) break;

            // Update for next lattice copy shifted down by <-0.2, -0.2, -0.2, -0.2>.
            xsi += LATTICE_STEP_4D; ysi += LATTICE_STEP_4D; zsi += LATTICE_STEP_4D; wsi += LATTICE_STEP_4D;
            ssi += LATTICE_STEP_4D * 4 * UNSKEW_4D;
            seed -= SEED_OFFSET_4D;

            // Because we don't always start on the same lattice copy, there's a special reset case.
            if (i == startingLattice) {
                xsvp -= PRIME_X;
                ysvp -= PRIME_Y;
                zsvp -= PRIME_Z;
                wsvp -= PRIME_W;
                seed += SEED_OFFSET_4D * 5;
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

    private static int fastRound(float x) {
        return x < 0 ? (int)(x - 0.5f) : (int)(x + 0.5f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenSimplex2 that = (OpenSimplex2) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ (seed >>> 32));
    }

    @Override
    public String toString() {
        return "OpenSimplex2{" +
                "seed=" + seed +
                '}';
    }
    /*
     * gradients
     */

    private static final float[] GRADIENTS_2D;
    private static final float[] GRADIENTS_3D;
    private static final float[] GRADIENTS_4D;
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
    }
}