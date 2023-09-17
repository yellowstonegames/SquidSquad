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
import com.github.tommyettinger.digital.BitConversion;

/**
 * Foam noise code as an {@link INoise} implementation.
 * <br>
 * This is a very-smooth type of noise that can work well using fewer octaves than simplex noise or value noise.
 * It also tends to look similar in lower dimensions and higher dimensions, where some other kinds of noise (such as
 * {@link SimplexNoise} change their appearance quite a bit in 2D vs. 6D.
 */
public class FoamNoise implements INoise {

    /**
     * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
     */
    protected long seed;

    public static final FoamNoise instance = new FoamNoise();

    public FoamNoise() {
        this(1234567890L);
    }

    public FoamNoise(long seed)
    {
        this.seed = seed;
    }

    // 2D SECTION

    /**
     * Gets 2D noise using {@link #getSeed()}.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    /**
     * Gets 2D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        final float p0 = x;
        final float p1 = x * -0.5f + y * 0.8660254037844386f;
        final float p2 = x * -0.5f + y * -0.8660254037844387f;

        float xin = p1;
        float yin = p2;
        final float a = valueNoise(xin, yin, seed);
        xin = p2;
        yin = p0;
        final float b = valueNoise(xin + a, yin, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        final float c = valueNoise(xin + b, yin, seed + 0x3504F333F9DE6486L);
        final float result = (a + b + c) * 0.3333333333333333f;
        // Barron spline
        final float sharp = 0.75f * 2.2f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    /**
     * Gets 2D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x or y. This has a different output range (0 to 1) than foam noise.
     * @param x x coordinate
     * @param y y coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between 0 and 1
     */
    public static float valueNoise(float x, float y, final long seed) {
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
                * 0x1p-64f + 0.5f;
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

    // 3D SECTION

    /**
     * Gets 3D noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    /**
     * Gets 3D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        final float p0 = x;
        final float p1 = x * -0.3333333333333333f + y * 0.9428090415820634f;
        final float p2 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * 0.816496580927726f;
        final float p3 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * -0.816496580927726f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        final float a = valueNoise(xin, yin, zin, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        final float b = valueNoise(xin + a, yin, zin, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        zin = p3;
        final float c = valueNoise(xin + b, yin, zin, seed + 0x3504F333F9DE6486L);
        xin = p0;
        yin = p1;
        zin = p2;
        final float d = valueNoise(xin + c, yin, zin, seed + 0xCF876CCDF6CD96C9L);
        final float result = (a + b + c + d) * 0.25f;
        // Barron spline
        final float sharp = 0.75f * 3.3f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final long sign = BitConversion.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }
    /**
     * Gets value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, or z. This has a different output range (0 to 1) than foam noise.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between 0 and 1
     */
    public static float valueNoise(float x, float y, float z, final long seed) {
        final long STEPX = 0xD1B54A32D192ED03L;
        final long STEPY = 0xABC98388FB8FAC03L;
        final long STEPZ = 0x8CB92BA72F3D8DD7L;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        long zFloor = (long)Math.floor(z);
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
        ) * 0x1p-64f + 0.5f;
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

    // 4D SECTION

    /**
     * Gets 4D foam noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    /**
     * Gets 4D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param seed can be any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        final float p0 = x;
        final float p1 = x * -0.25f + y *  0.9682458365518543f;
        final float p2 = x * -0.25f + y * -0.3227486121839514f + z * 0.9128709291752769f;
        final float p3 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * 0.7905694150420949f;
        final float p4 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * -0.7905694150420947f;
        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        final float a = valueNoise(xin, yin, zin, win, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final float b = valueNoise(xin + a, yin, zin, win, seed + 0x9A827999FCEF3243L);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final float c = valueNoise(xin + b, yin, zin, win, seed + 0x3504F333F9DE6486L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final float d = valueNoise(xin + c, yin, zin, win, seed + 0xCF876CCDF6CD96C9L);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final float e = valueNoise(xin + d, yin, zin, win, seed + 0x6A09E667F3BCC90CL);

        final float result = (a + b + c + d + e) * 0.2f;
        // Barron spline
        final float sharp = 0.75f * 4.4f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    /**
     * Gets 4D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, z, or w. This has a different output range (0 to 1) than foam noise.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between 0 and 1
     */
    public static float valueNoise(float x, float y, float z, float w, final long seed) {
        final long STEPX = 0xDB4F0B9175AE2165L;
        final long STEPY = 0xBBE0563303A4615FL;
        final long STEPZ = 0xA0F2EC75A1FE1575L;
        final long STEPW = 0x89E182857D9ED689L;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        long zFloor = (long)Math.floor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        long wFloor = (long)Math.floor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
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
                ))) * 0x1p-64f + 0.5f;
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

    // 5D SECTION

    /**
     * Gets 5D noise with {@link #getSeed()}.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    /**
     * Gets 5D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param seed any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        final float p0 = x *  0.8157559148337911f + y *  0.5797766823136037f;
        final float p1 = x * -0.7314923478726791f + y *  0.6832997137249108f;
        final float p2 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * 0.9486832980505138f;
        final float p3 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w *   0.8944271909999159f;
        final float p4 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u *  0.7745966692414833f;
        final float p5 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u * -0.7745966692414836f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        float uin = p5;
        final float a = valueNoise(seed, xin, yin, zin, win, uin);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final float b = valueNoise(seed + 0x9A827999FCEF3243L, xin + a, yin, zin, win, uin);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final float c = valueNoise(seed + 0x3504F333F9DE6486L, xin + b, yin, zin, win, uin);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final float d = valueNoise(seed + 0xCF876CCDF6CD96C9L, xin + c, yin, zin, win, uin);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final float e = valueNoise(seed + 0x6A09E667F3BCC90CL, xin + d, yin, zin, win, uin);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final float f = valueNoise(seed + 0x48C6001F0ABFB4FL, xin + e, yin, zin, win, uin);

        final float result = (a + b + c + d + e + f) * 0.16666666666666666f;
        final float sharp = 0.75f * 5.5f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    /**
     * Gets 5D value noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x, y, z, w, or u. This has a different output range (0 to 1) than foam noise.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @param u u coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between 0 and 1
     */
    public static float valueNoise(long seed, float x, float y, float z, float w, float u) {
        final long STEPX = 0xE19B01AA9D42C633L;
        final long STEPY = 0xC6D1D6C8ED0C9631L;
        final long STEPZ = 0xAF36D01EF7518DBBL;
        final long STEPW = 0x9A69443F36F710E7L;
        final long STEPU = 0x881403B9339BD42DL;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        long zFloor = (long)Math.floor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        long wFloor = (long)Math.floor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
        long uFloor = (long)Math.floor(u);
        u -= uFloor;
        u *= u * (3 - 2 * u);
        //STEPX, STEPY, STEPZ, STEPW, STEPU
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed)))
                        ))))
        ) * 0x1p-64f + 0.5f;
    }

    /**
     * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
     * (2 to the 64).
     * @param x should be premultiplied by 0xE19B01AA9D42C633L
     * @param y should be premultiplied by 0xC6D1D6C8ED0C9631L
     * @param z should be premultiplied by 0xAF36D01EF7518DBBL
     * @param w should be premultiplied by 0x9A69443F36F710E7L
     * @param u should be premultiplied by 0x881403B9339BD42DL
     * @param s state, any long
     * @return a mediocre 64-bit hash
     */
    private static long hashPart(final long x, final long y, final long z, final long w, final long u, long s) {
        s ^= x ^ y ^ z ^ w ^ u;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    // 6D SECTION

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
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    /**
     * Gets 6D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param v    v position; can be any finite float
     * @param seed any long
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        final float p0 = x;
        final float p1 = x * -0.16666666666666666f + y *  0.98601329718326940f;
        final float p2 = x * -0.16666666666666666f + y * -0.19720265943665383f + z *  0.96609178307929590f;
        final float p3 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w *  0.93541434669348530f;
        final float p4 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u *  0.8819171036881969f;
        final float p5 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v *  0.7637626158259734f;
        final float p6 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v * -0.7637626158259732f;
        float xin = p0;
        float yin = p5;
        float zin = p3;
        float win = p6;
        float uin = p1;
        float vin = p4;
        final float a = valueNoise(seed, xin, yin, zin, win, uin, vin);
        xin = p2;
        yin = p6;
        zin = p0;
        win = p4;
        uin = p5;
        vin = p3;
        final float b = valueNoise(seed + 0x9A827999FCEF3243L, xin + a, yin, zin, win, uin, vin);
        xin = p1;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p6;
        vin = p5;
        final float c = valueNoise(seed + 0x3504F333F9DE6486L, xin + b, yin, zin, win, uin, vin);
        xin = p6;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p1;
        final float d = valueNoise(seed + 0xCF876CCDF6CD96C9L, xin + c, yin, zin, win, uin, vin);
        xin = p2;
        yin = p1;
        zin = p5;
        win = p0;
        uin = p3;
        vin = p6;
        final float e = valueNoise(seed + 0x6A09E667F3BCC90CL, xin + d, yin, zin, win, uin, vin);
        xin = p0;
        yin = p4;
        zin = p6;
        win = p3;
        uin = p1;
        vin = p2;
        final float f = valueNoise(seed + 0x48C6001F0ABFB4FL, xin + e, yin, zin, win, uin, vin);
        xin = p5;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p0;
        final float g = valueNoise(seed + 0x9F0ED99BED9B2D92L, xin + f, yin, zin, win, uin, vin);
        final float result = (a + b + c + d + e + f + g) * 0.14285714285714285f;
        final float sharp = 0.75f * 6.6f;
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    float valueNoise(long seed, float x, float y, float z, float w, float u, float v) {
        final long STEPX = 0xE60E2B722B53AEEBL;
        final long STEPY = 0xCEBD76D9EDB6A8EFL;
        final long STEPZ = 0xB9C9AA3A51D00B65L;
        final long STEPW = 0xA6F5777F6F88983FL;
        final long STEPU = 0x9609C71EB7D03F7BL;
        final long STEPV = 0x86D516E50B04AB1BL;
        long xFloor = (long)Math.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        long yFloor = (long)Math.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        long zFloor = (long)Math.floor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        long wFloor = (long)Math.floor(w);
        w -= wFloor;
        w *= w * (3 - 2 * w);
        long uFloor = (long)Math.floor(u);
        u -= uFloor;
        u *= u * (3 - 2 * u);
        long vFloor = (long)Math.floor(v);
        v -= vFloor;
        v *= v * (3 - 2 * v);
        //STEPX, STEPY, STEPZ, STEPW, STEPU, STEPV
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        vFloor *= STEPV;
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + 0xA127B, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + 0xA127B, vFloor + STEPV, seed)))
                                ))))))
        ) * 0x1p-64f + 0.5f;
    }
    
    /**
     * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
     * (2 to the 64).
     * @param x should be premultiplied by 0xE60E2B722B53AEEBL
     * @param y should be premultiplied by 0xCEBD76D9EDB6A8EFL
     * @param z should be premultiplied by 0xB9C9AA3A51D00B65L
     * @param w should be premultiplied by 0xA6F5777F6F88983FL
     * @param u should be premultiplied by 0x9609C71EB7D03F7BL
     * @param v should be premultiplied by 0x86D516E50B04AB1BL
     * @param s state, any long
     * @return a mediocre 64-bit hash
     */
    private static long hashPart(final long x, final long y, final long z, final long w, final long u, final long v, long s) {
        s ^= x ^ y ^ z ^ w ^ u ^ v;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    // OTHER

    /**
     * Returns the String "FoaN", to be used as a unique tag for this generator.
     *
     * @return the String "FoaN"
     */
    @Override
    public String getTag() {
        return "FoaN";
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, which is 2 inclusive
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, which is 6 inclusive
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
    public boolean canUseSeed() {
        return true;
    }
    
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    @Override
    public FoamNoise stringDeserialize(String data) {
        setSeed(Base.BASE10.readLong(data, 1, data.length() - 1));
        return this;
    }

    public static FoamNoise recreateFromString(String data) {
        return new FoamNoise(Base.BASE10.readLong(data, 1, data.length() - 1));
    }

    @Override
    public FoamNoise copy() {
        return new FoamNoise(this.seed);
    }

    @Override
    public String toString() {
        return "FoamNoise{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FoamNoise that = (FoamNoise) o;

        return (seed == that.seed);
    }

    @Override
    public int hashCode() {
        return (int) (seed ^ seed >>> 32);
    }
}
