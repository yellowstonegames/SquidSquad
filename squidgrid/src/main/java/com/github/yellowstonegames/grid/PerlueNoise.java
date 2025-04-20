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

import com.github.tommyettinger.digital.Base;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.tommyettinger.digital.MathTools.lerp;
import static com.github.yellowstonegames.grid.GradientVectors.*;

/**
 * A mix of "Classic" Perlin noise, written by Ken Perlin before he created Simplex Noise, with value noise calculated
 * at the same time. This uses quintic interpolation throughout (which was an improvement found in Simplex Noise), and
 * has a single {@code int} seed. Perlue Noise can have significant grid-aligned and 45-degree-diagonal artifacts when
 * too few octaves are used, but sometimes this is irrelevant, such as when sampling 3D noise on the surface of a
 * sphere. These artifacts sometimes manifest as "waves" of quickly-changing and then slowly-changing noise, when 3D
 * noise uses time as the z axis.
 * <br>
 * This tends to look fairly different from vanilla PerlinNoise or ValueNoise; it is capable of more chaotic
 * arrangements of high and low values than either of those, but it still tends to have clusters of values of a specific
 * size more often than clusters with very different sizes.
 */
public class PerlueNoise implements INoise {
    public static final PerlueNoise instance = new PerlueNoise();

    public static final float SCALE2 = 1.41421330f; //towardsZero(1f/ (float) Math.sqrt(2f / 4f));
    public static final float SCALE3 = 1.15470030f; //towardsZero(1f/ (float) Math.sqrt(3f / 4f));
    public static final float SCALE4 = 0.99999990f; //towardsZero(1f)                            ;
    public static final float SCALE5 = 0.89442706f; //towardsZero(1f/ (float) Math.sqrt(5f / 4f));
    public static final float SCALE6 = 0.81649643f; //towardsZero(1f/ (float) Math.sqrt(6f / 4f));

    public static final float EQ_ADD_2 = 1.0f / 0.85f;
    public static final float EQ_ADD_3 = 0.8f / 0.85f;
    public static final float EQ_ADD_4 = 0.6f / 0.85f;
    public static final float EQ_ADD_5 = 0.4f / 0.85f;
    public static final float EQ_ADD_6 = 0.2f / 0.85f;

    public static final float EQ_MUL_2 = 1.2535664f;
    public static final float EQ_MUL_3 = 1.2071217f;
    public static final float EQ_MUL_4 = 1.1588172f;
    public static final float EQ_MUL_5 = 1.1084094f;
    public static final float EQ_MUL_6 = 1.0555973f;

    public int seed;

    public PerlueNoise() {
        this(0x1337BEEF);
    }

    public PerlueNoise(final int seed) {
        this.seed = seed;
    }

    public PerlueNoise(final long seed) {
        this.seed = (int)seed;
    }

    public PerlueNoise(PerlueNoise other) {
        this.seed = other.seed;
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, 2
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, 6
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true because this generator can be seeded with {@link #setSeed(long)} and retrieved with
     * {@link #getSeed()}.
     *
     * @return true
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    /**
     * Sets the seed to the given int, or really the int portion of the given long.
     * @param seed a long seed, but only the int portion will be used (it is cast to int)
     */
    @Override
    public void setSeed(long seed) {
        this.seed = (int)seed;
    }

    /**
     * Gets the current seed of the generator, as a long (it is really an int).
     *
     * @return the current seed, as a long
     */
    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the constant String {@code "PluN"} that identifies this in serialized Strings.
     *
     * @return a short String constant that identifies this RawNoise type, {@code "PluN"}
     */
    @Override
    public String getTag() {
        return "PluN";
    }

    /**
     * Creates a copy of this PerlueNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     *
     * @return a copy of this PerlueNoise
     */
    @Override
    public PerlueNoise copy() {
        return new PerlueNoise(this.seed);
    }

    protected static float gradCoord2D(int seed, int x, int y,
                                       float xd, float yd) {
        final int h = hashAll(x, y, seed);
        final int hash = h & (255 << 1);
        return (h * 0x1p-32f) + xd * GRADIENTS_2D[hash] + yd * GRADIENTS_2D[hash + 1];
    }
    protected static float gradCoord3D(int seed, int x, int y, int z, float xd, float yd, float zd) {
        final int h = hashAll(x, y, z, seed);
        final int hash = h & (255 << 2);
        return (h * 0x1p-32f) + xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2];
    }
    protected static float gradCoord4D(int seed, int x, int y, int z, int w,
                                       float xd, float yd, float zd, float wd) {
        final int h = hashAll(x, y, z, w, seed);
        final int hash = h & (255 << 2);
        return (h * 0x1p-32f) + xd * GRADIENTS_4D[hash] + yd * GRADIENTS_4D[hash + 1] + zd * GRADIENTS_4D[hash + 2] + wd * GRADIENTS_4D[hash + 3];
    }
    protected static float gradCoord5D(int seed, int x, int y, int z, int w, int u,
                                       float xd, float yd, float zd, float wd, float ud) {
        final int h = hashAll(x, y, z, w, u, seed);
        final int hash = h & (255 << 3);
        return (h * 0x1p-32f) + xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash + 1] + zd * GRADIENTS_5D[hash + 2]
                + wd * GRADIENTS_5D[hash + 3] + ud * GRADIENTS_5D[hash + 4];
    }
    protected static float gradCoord6D(int seed, int x, int y, int z, int w, int u, int v,
                                       float xd, float yd, float zd, float wd, float ud, float vd) {
        final int h = hashAll(x, y, z, w, u, v, seed);
        final int hash = h & (255 << 3);
        return (h * 0x1p-32f) + xd * GRADIENTS_6D[hash] + yd * GRADIENTS_6D[hash + 1] + zd * GRADIENTS_6D[hash + 2]
                + wd * GRADIENTS_6D[hash + 3] + ud * GRADIENTS_6D[hash + 4] + vd * GRADIENTS_6D[hash + 5];
    }

    /**
     * Given inputs as {@code x} in the range -1.0 to 1.0 that are too biased towards 0.0, this "squashes" the range
     * softly to widen it and spread it away from 0.0 without increasing bias anywhere else.
     * <br>
     * This starts with a common sigmoid function, {@code x / sqrt(x * x + add)}, but instead of approaching -1 and 1
     * but never reaching them, this multiplies the result so the line crosses -1 when x is -1, and crosses 1 when x is
     * 1. It has a smooth derivative, if that matters to you.
     *
     * @param x a float between -1 and 1
     * @param add if greater than 1, this will have nearly no effect; the lower this goes below 1, the more this will
     *           separate results near the center of the range. This must be greater than or equal to 0.0
     * @param mul typically the result of calling {@code (float) Math.sqrt(add + 1f)}
     * @return a float with a slightly different distribution from {@code x}, but still between -1 and 1
     */
    public static float equalize(float x, float add, float mul) {
        return x * mul / (float) Math.sqrt(x * x + add);
    }

    @Override
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, final long s) {
        final int seed = (int)s;
        final int
                xi = fastFloor(x), x0 = xi * X_2,
                yi = fastFloor(y), y0 = yi * Y_2;
        final float xf = x - xi, yf = y - yi;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        return
                equalize(lerp(lerp(gradCoord2D(seed, x0, y0, xf, yf), gradCoord2D(seed, x0+X_2, y0, xf - 1, yf), xa),
                                lerp(gradCoord2D(seed, x0, y0+Y_2, xf, yf-1), gradCoord2D(seed, x0+X_2, y0+Y_2, xf - 1, yf - 1), xa),
                                ya) * SCALE2, EQ_ADD_2, EQ_MUL_2);
    }

    @Override
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, final long s) {
        final int seed = (int)s;
        final int
                xi = fastFloor(x), x0 = xi * X_3,
                yi = fastFloor(y), y0 = yi * Y_3,
                zi = fastFloor(z), z0 = zi * Z_3;
        final float xf = x - xi, yf = y - yi, zf = z - zi;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
         return
                 equalize(
                         lerp(
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0, xf, yf, zf),
                                                 gradCoord3D(seed, x0+X_3, y0, z0, xf - 1, yf, zf),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+Y_3, z0, xf, yf-1, zf),
                                                 gradCoord3D(seed, x0+X_3, y0+Y_3, z0, xf - 1, yf - 1, zf),
                                                 xa),
                                         ya),
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0+Z_3, xf, yf, zf-1),
                                                 gradCoord3D(seed, x0+X_3, y0, z0+Z_3, xf - 1, yf, zf-1),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+Y_3, z0+Z_3, xf, yf-1, zf-1),
                                                 gradCoord3D(seed, x0+X_3, y0+Y_3, z0+Z_3, xf - 1, yf - 1, zf-1),
                                                 xa),
                                         ya),
                                 za) * SCALE3, EQ_ADD_3, EQ_MUL_3);
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, final long s) {
        final int seed = (int)s;
        final int
                xi = fastFloor(x), x0 = xi * X_4,
                yi = fastFloor(y), y0 = yi * Y_4,
                zi = fastFloor(z), z0 = zi * Z_4,
                wi = fastFloor(w), w0 = wi * W_4;
        final float xf = x - xi, yf = y - yi, zf = z - zi, wf = w - wi;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        return
                equalize(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0, xf, yf, zf, wf),
                                                        gradCoord4D(seed, x0+X_4, y0, z0, w0, xf - 1, yf, zf, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+Y_4, z0, w0, xf, yf-1, zf, wf),
                                                        gradCoord4D(seed, x0+X_4, y0+Y_4, z0, w0, xf - 1, yf - 1, zf, wf),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+Z_4, w0, xf, yf, zf-1, wf),
                                                        gradCoord4D(seed, x0+X_4, y0, z0+Z_4, w0, xf - 1, yf, zf-1, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+Y_4, z0+Z_4, w0, xf, yf-1, zf-1, wf),
                                                        gradCoord4D(seed, x0+X_4, y0+Y_4, z0+Z_4, w0, xf - 1, yf - 1, zf-1, wf),
                                                        xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0+W_4, xf, yf, zf, wf - 1),
                                                        gradCoord4D(seed, x0+X_4, y0, z0, w0+W_4, xf - 1, yf, zf, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+Y_4, z0, w0+W_4, xf, yf-1, zf, wf - 1),
                                                        gradCoord4D(seed, x0+X_4, y0+Y_4, z0, w0+W_4, xf - 1, yf - 1, zf, wf - 1),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+Z_4, w0+W_4, xf, yf, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+X_4, y0, z0+Z_4, w0+W_4, xf - 1, yf, zf-1, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+Y_4, z0+Z_4, w0+W_4, xf, yf-1, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+X_4, y0+Y_4, z0+Z_4, w0+W_4, xf - 1, yf - 1, zf-1, wf - 1),
                                                        xa),
                                                ya),
                                        za),
                                wa) * SCALE4, EQ_ADD_4, EQ_MUL_4);
    }


    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, final long s) {
        final int seed = (int)s;
        final int
                xi = fastFloor(x), x0 = xi * X_5,
                yi = fastFloor(y), y0 = yi * Y_5,
                zi = fastFloor(z), z0 = zi * Z_5,
                wi = fastFloor(w), w0 = wi * W_5,
                ui = fastFloor(u), u0 = ui * U_5;
        final float xf = x - xi, yf = y - yi, zf = z - zi, wf = w - wi, uf = u - ui;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        final float ua = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 9.999998f);
        return
                equalize(
                lerp(lerp(
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0, u0, xf, yf, zf, wf, uf),
                                                gradCoord5D(seed, x0+X_5, y0, z0, w0, u0, xf-1, yf, zf, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+Y_5, z0, w0, u0, xf, yf-1, zf, wf, uf),
                                                gradCoord5D(seed, x0+X_5, y0+Y_5, z0, w0, u0, xf-1, yf-1, zf, wf, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+Z_5, w0, u0, xf, yf, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+X_5, y0, z0+Z_5, w0, u0, xf-1, yf, zf-1, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+Y_5, z0+Z_5, w0, u0, xf, yf-1, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+X_5, y0+Y_5, z0+Z_5, w0, u0, xf-1, yf-1, zf-1, wf, uf), xa),
                                        ya),
                                za),
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0+W_5, u0, xf, yf, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+X_5, y0, z0, w0+W_5, u0, xf-1, yf, zf, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+Y_5, z0, w0+W_5, u0, xf, yf-1, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+X_5, y0+Y_5, z0, w0+W_5, u0, xf-1, yf-1, zf, wf-1, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+Z_5, w0+W_5, u0, xf, yf, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+X_5, y0, z0+Z_5, w0+W_5, u0, xf-1, yf, zf-1, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+Y_5, z0+Z_5, w0+W_5, u0, xf, yf-1, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+X_5, y0+Y_5, z0+Z_5, w0+W_5, u0, xf-1, yf-1, zf-1, wf-1, uf), xa),
                                        ya),
                                za),
                        wa),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0, u0+U_5, xf, yf, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0, z0, w0, u0+U_5, xf-1, yf, zf, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+Y_5, z0, w0, u0+U_5, xf, yf-1, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0+Y_5, z0, w0, u0+U_5, xf-1, yf-1, zf, wf, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+Z_5, w0, u0+U_5, xf, yf, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0, z0+Z_5, w0, u0+U_5, xf-1, yf, zf-1, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+Y_5, z0+Z_5, w0, u0+U_5, xf, yf-1, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0+Y_5, z0+Z_5, w0, u0+U_5, xf-1, yf-1, zf-1, wf, uf-1), xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0+W_5, u0+U_5, xf, yf, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0, z0, w0+W_5, u0+U_5, xf-1, yf, zf, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+Y_5, z0, w0+W_5, u0+U_5, xf, yf-1, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0+Y_5, z0, w0+W_5, u0+U_5, xf-1, yf-1, zf, wf-1, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+Z_5, w0+W_5, u0+U_5, xf, yf, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0, z0+Z_5, w0+W_5, u0+U_5, xf-1, yf, zf-1, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+Y_5, z0+Z_5, w0+W_5, u0+U_5, xf, yf-1, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+X_5, y0+Y_5, z0+Z_5, w0+W_5, u0+U_5, xf-1, yf-1, zf-1, wf-1, uf-1), xa),
                                                ya),
                                        za),
                                wa),
                        ua) * SCALE5, EQ_ADD_5, EQ_MUL_5);
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, final long s) {
        final int seed = (int)s;
        final int
                xi = fastFloor(x), x0 = xi * X_6,
                yi = fastFloor(y), y0 = yi * Y_6,
                zi = fastFloor(z), z0 = zi * Z_6,
                wi = fastFloor(w), w0 = wi * W_6,
                ui = fastFloor(u), u0 = ui * U_6,
                vi = fastFloor(v), v0 = vi * V_6;
        final float xf = x - xi, yf = y - yi, zf = z - zi, wf = w - wi, uf = u - ui, vf = v - vi;
        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        final float ua = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 9.999998f);
        final float va = vf * vf * vf * (vf * (vf * 6.0f - 15.0f) + 9.999998f);
        return equalize(
                lerp(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xf, yf, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0, u0, v0, xf - 1, yf, zf, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0, u0, v0, xf, yf - 1, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0, u0, v0, xf - 1, yf - 1, zf, wf, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0, u0, v0, xf, yf, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0, u0, v0, xf - 1, yf, zf - 1, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0, u0, v0, xf, yf - 1, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0, u0, v0, xf - 1, yf - 1, zf - 1, wf, uf, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+W_6, u0, v0, xf, yf, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0+W_6, u0, v0, xf - 1, yf, zf, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0+W_6, u0, v0, xf, yf - 1, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0+W_6, u0, v0, xf - 1, yf - 1, zf, wf - 1, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0+W_6, u0, v0, xf, yf, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0+W_6, u0, v0, xf - 1, yf, zf - 1, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0+W_6, u0, v0, xf, yf - 1, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0+W_6, u0, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0+U_6, v0, xf, yf, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0, u0+U_6, v0, xf - 1, yf, zf, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0, u0+U_6, v0, xf, yf - 1, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0, u0+U_6, v0, xf - 1, yf - 1, zf, wf, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0, u0+U_6, v0, xf, yf, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0, u0+U_6, v0, xf - 1, yf, zf - 1, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0, u0+U_6, v0, xf, yf - 1, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0, u0+U_6, v0, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+W_6, u0+U_6, v0, xf, yf, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0+W_6, u0+U_6, v0, xf - 1, yf, zf, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0+W_6, u0+U_6, v0, xf, yf - 1, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0+W_6, u0+U_6, v0, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0+W_6, u0+U_6, v0, xf, yf, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0+W_6, u0+U_6, v0, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0+W_6, u0+U_6, v0, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0+W_6, u0+U_6, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0+V_6, xf, yf, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0, u0, v0+V_6, xf - 1, yf, zf, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0, u0, v0+V_6, xf, yf - 1, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0, u0, v0+V_6, xf - 1, yf - 1, zf, wf, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0, u0, v0+V_6, xf, yf, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0, u0, v0+V_6, xf - 1, yf, zf - 1, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0, u0, v0+V_6, xf, yf - 1, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0, u0, v0+V_6, xf - 1, yf - 1, zf - 1, wf, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+W_6, u0, v0+V_6, xf, yf, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0+W_6, u0, v0+V_6, xf - 1, yf, zf, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0+W_6, u0, v0+V_6, xf, yf - 1, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0+W_6, u0, v0+V_6, xf - 1, yf - 1, zf, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0+W_6, u0, v0+V_6, xf, yf, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0+W_6, u0, v0+V_6, xf - 1, yf, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0+W_6, u0, v0+V_6, xf, yf - 1, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0+W_6, u0, v0+V_6, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0+U_6, v0+V_6, xf, yf, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0, u0+U_6, v0+V_6, xf - 1, yf, zf, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0, u0+U_6, v0+V_6, xf, yf - 1, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0, u0+U_6, v0+V_6, xf - 1, yf - 1, zf, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0, u0+U_6, v0+V_6, xf, yf, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0, u0+U_6, v0+V_6, xf - 1, yf, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0, u0+U_6, v0+V_6, xf, yf - 1, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0, u0+U_6, v0+V_6, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+W_6, u0+U_6, v0+V_6, xf, yf, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0, w0+W_6, u0+U_6, v0+V_6, xf - 1, yf, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0, w0+W_6, u0+U_6, v0+V_6, xf, yf - 1, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0, w0+W_6, u0+U_6, v0+V_6, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+Z_6, w0+W_6, u0+U_6, v0+V_6, xf, yf, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0, z0+Z_6, w0+W_6, u0+U_6, v0+V_6, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+Y_6, z0+Z_6, w0+W_6, u0+U_6, v0+V_6, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+X_6, y0+Y_6, z0+Z_6, w0+W_6, u0+U_6, v0+V_6, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        va) * SCALE6, EQ_ADD_6, EQ_MUL_6);
    }

    /**
     * Produces a String that describes everything needed to recreate this RawNoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another RawNoise.
     * @return a String that describes this PerlueNoise for serialization
     */
    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this PerlueNoise to have the
     * described state from the given String. The serialized String must have been produced by a PerlueNoise.
     *
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this PerlueNoise, after being modified (if possible)
     */
    @Override
    public PerlueNoise stringDeserialize(String data) {
        seed = Base.BASE10.readInt(data, 1, data.indexOf('`', 1));
        return this;
    }

    public static PerlueNoise recreateFromString(String data) {
        return new PerlueNoise(Base.BASE10.readInt(data, 1, data.indexOf('`', 1)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerlueNoise that = (PerlueNoise) o;

        return (seed == that.seed);
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "PerlueNoise{seed=" + seed + '}';
    }

    /**
     * A 32-bit point hash that needs 2 dimensions pre-multiplied by constants {@link #X_2} and {@link #Y_2}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_2}
     * @param y y position, as an int pre-multiplied by {@link #Y_2}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y point with the given state s, shifted for {@link GradientVectors#GRADIENTS_2D}
     */
    private static int hashAll(int x, int y, int s) {
        final int h = (s ^ x ^ y) * 0x125493;
        return (h ^ (h << 11 | h >>> 21) ^ (h << 23 | h >>> 9));
    }
    /**
     * A 32-bit point hash that needs 3 dimensions pre-multiplied by constants {@link #X_3} through {@link #Z_3}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_3}
     * @param y y position, as an int pre-multiplied by {@link #Y_3}
     * @param z z position, as an int pre-multiplied by {@link #Z_3}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z point with the given state s, shifted for {@link GradientVectors#GRADIENTS_3D}
     */
    private static int hashAll(int x, int y, int z, int s) {
        final int h = (s ^ x ^ y ^ z) * 0x125493;
        return (h ^ (h << 11 | h >>> 21) ^ (h << 23 | h >>> 9));
    }

    /**
     * A 32-bit point hash that needs 4 dimensions pre-multiplied by constants {@link #X_4} through {@link #W_4}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_4}
     * @param y y position, as an int pre-multiplied by {@link #Y_4}
     * @param z z position, as an int pre-multiplied by {@link #Z_4}
     * @param w w position, as an int pre-multiplied by {@link #W_4}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w point with the given state s, shifted for {@link GradientVectors#GRADIENTS_4D}
     */
    private static int hashAll(int x, int y, int z, int w, int s) {
        final int h = (s ^ x ^ y ^ z ^ w) * 0x125493;
        return (h ^ (h << 11 | h >>> 21) ^ (h << 23 | h >>> 9));
    }
    /**
     * A 32-bit point hash that needs 5 dimensions pre-multiplied by constants {@link #X_5} through {@link #U_5}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_5}
     * @param y y position, as an int pre-multiplied by {@link #Y_5}
     * @param z z position, as an int pre-multiplied by {@link #Z_5}
     * @param w w position, as an int pre-multiplied by {@link #W_5}
     * @param u u position, as an int pre-multiplied by {@link #U_5}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u point with the given state s, shifted for {@link GradientVectors#GRADIENTS_5D}
     */
    private static int hashAll(int x, int y, int z, int w, int u, int s) {
        final int h = (s ^ x ^ y ^ z ^ w ^ u) * 0x125493;
        return (h ^ (h << 11 | h >>> 21) ^ (h << 23 | h >>> 9));
    }

    /**
     * A 32-bit point hash that needs 6 dimensions pre-multiplied by constants {@link #X_6} through {@link #V_6}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_6}
     * @param y y position, as an int pre-multiplied by {@link #Y_6}
     * @param z z position, as an int pre-multiplied by {@link #Z_6}
     * @param w w position, as an int pre-multiplied by {@link #W_6}
     * @param u u position, as an int pre-multiplied by {@link #U_6}
     * @param v v position, as an int pre-multiplied by {@link #V_6}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state s, shifted for {@link GradientVectors#GRADIENTS_6D}
     */
    private static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
        final int h = (s ^ x ^ y ^ z ^ w ^ u ^ v) * 0x125493;
        return (h ^ (h << 11 | h >>> 21) ^ (h << 23 | h >>> 9));
    }

    private static final int X_2 = 0x1827F5, Y_2 = 0x123C21;
    private static final int X_3 = 0x1A36A9, Y_3 = 0x157931, Z_3 = 0x119725;
    private static final int X_4 = 0x1B69E1, Y_4 = 0x177C0B, Z_4 = 0x141E5D, W_4 = 0x113C31;
    private static final int X_5 = 0x1C3361, Y_5 = 0x18DA39, Z_5 = 0x15E6DB, W_5 = 0x134D29, U_5 = 0x110281;
    private static final int X_6 = 0x1CC1C5, Y_6 = 0x19D7AF, Z_6 = 0x173935, W_6 = 0x14DEAF, U_6 = 0x12C139, V_6 = 0x10DAA3;

}
