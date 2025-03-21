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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.grid.INoise;

import static com.github.yellowstonegames.core.DescriptiveColor.lerpColors;

/**
 * A different type of noise that produces packed float colors rather than standard scalar noise values.
 * This handles lerping between colors (in Oklab color space), and outputs a ready to use ABGR7888 float,
 * which Batch can take as a "packed color," and various parts of libGDX can as well.
 */
@Beta
public class ColorNoise implements INoise {

    public static final ColorNoise instance = new ColorNoise();

    public int seed = 0xD1CEBEEF;
    public ColorNoise() {
    }

    public ColorNoise(int seed) {
        this.seed = seed;
    }

    public ColorNoise(long seed) {
        this.seed = (int) (seed ^ seed >>> 32);
    }

    @Override
    public String getTag() {
        return "ClrN";
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public ColorNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = DigitTools.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static ColorNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed =   DigitTools.intFromDec(data, 1, data.indexOf('`', 2));

        return new ColorNoise(seed);
    }

    @Override
    public ColorNoise copy() {
        return new ColorNoise(seed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorNoise that = (ColorNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "ValueNoise{" +
                "seed=" + seed +
                '}';
    }

    public static float valueNoise(float x, float y, int seed)
    {
        final int STEPX = 0xD1B55;
        final int STEPY = 0xABC99;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        return DescriptiveColor.oklabIntToFloat(
                lerpColors(
                lerpColors(hashPart1024(xFloor, yFloor, seed), hashPart1024(xFloor + STEPX, yFloor, seed), x),
                lerpColors(hashPart1024(xFloor, yFloor + STEPY, seed), hashPart1024(xFloor + STEPX, yFloor + STEPY, seed), x),
                y)
        );
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 3D version of R2, but we only use 2 of the 3 constants.
     * @param x should be premultiplied by 0xD1B55
     * @param y should be premultiplied by 0xABC99
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return DescriptiveColor.LIST.get((int)(49 * ((s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 & 0xFFFFFFFFL) >>> 32));
    }

    public static float valueNoise(float x, float y, float z, int seed)
    {
        final int STEPX = 0xDB4F1;
        final int STEPY = 0xBBE05;
        final int STEPZ = 0xA0F2F;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        return DescriptiveColor.oklabIntToFloat(
                lerpColors(
                lerpColors(lerpColors(hashPart1024(xFloor, yFloor, zFloor, seed), hashPart1024(xFloor + STEPX, yFloor, zFloor, seed), x)
                        , lerpColors(hashPart1024(xFloor, yFloor + STEPY, zFloor, seed), hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, seed), x), y)
                , lerpColors(lerpColors(hashPart1024(xFloor, yFloor, zFloor + STEPZ, seed), hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, seed), x)
                        , lerpColors(hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, seed), hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed), x), y)
                , z)
        );
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 4D version of R2, but we only use 3 of the 4 constants.
     * @param x should be premultiplied by 0xDB4F1
     * @param y should be premultiplied by 0xBBE05
     * @param z should be premultiplied by 0xA0F2F
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return DescriptiveColor.LIST.get((int)(49 * ((s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 & 0xFFFFFFFFL) >>> 32));
    }

    public static float valueNoise(float x, float y, float z, float w, int seed)
    {
        final int STEPX = 0xE19B1;
        final int STEPY = 0xC6D1D;
        final int STEPZ = 0xAF36D;
        final int STEPW = 0x9A695;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed)))
                ))) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 5D version of R2, but we only use 4 of the 5 constants.
     * @param x should be premultiplied by 0xE19B1
     * @param y should be premultiplied by 0xC6D1D
     * @param z should be premultiplied by 0xAF36D
     * @param w should be premultiplied by 0x9A695
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, int s) {
        s += x ^ y ^ z ^ w;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, int seed)
    {
        final int STEPX = 0xE60E3;
        final int STEPY = 0xCEBD7;
        final int STEPZ = 0xB9C9B;
        final int STEPW = 0xA6F57;
        final int STEPU = 0x9609D;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed)))
                        ))))
        ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 6D version of R2, but we only use 5 of the 6 constants.
     * @param x should be premultiplied by 0xE60E3
     * @param y should be premultiplied by 0xCEBD7
     * @param z should be premultiplied by 0xB9C9B
     * @param w should be premultiplied by 0xA6F57
     * @param u should be premultiplied by 0x9609D
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, int s) {
        s += x ^ y ^ z ^ w ^ u;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, float v, int seed)
    {
        final int STEPX = 0xE95E1;
        final int STEPY = 0xD4BC7;
        final int STEPZ = 0xC1EDB;
        final int STEPW = 0xB0C8B;
        final int STEPU = 0xA127B;
        final int STEPV = 0x92E85;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        int vFloor = MathTools.fastFloor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); /* Won't go outside 0f to 1f range. */
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
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                ))))))
        ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 7D version of R2, but we only use 6 of the 7 constants.
     * @param x should be premultiplied by 0xE95E1
     * @param y should be premultiplied by 0xD4BC7
     * @param z should be premultiplied by 0xC1EDB
     * @param w should be premultiplied by 0xB0C8B
     * @param u should be premultiplied by 0xA127B
     * @param v should be premultiplied by 0x92E85
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, float v, float m, int seed)
    {
        final int STEPX = 0xEBEDF;
        final int STEPY = 0xD96EB;
        final int STEPZ = 0xC862B;
        final int STEPW = 0xB8ACD;
        final int STEPU = 0xAA323;
        final int STEPV = 0x9CDA5;
        final int STEPM = 0x908E3;
        int xFloor = MathTools.fastFloor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); /* Won't go outside 0f to 1f range. */
        int yFloor = MathTools.fastFloor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); /* Won't go outside 0f to 1f range. */
        int zFloor = MathTools.fastFloor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); /* Won't go outside 0f to 1f range. */
        int wFloor = MathTools.fastFloor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); /* Won't go outside 0f to 1f range. */
        int uFloor = MathTools.fastFloor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); /* Won't go outside 0f to 1f range. */
        int vFloor = MathTools.fastFloor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); /* Won't go outside 0f to 1f range. */
        int mFloor = MathTools.fastFloor(m);
        m -= mFloor;
        m *= m * (1 - m - m + 2); /* Won't go outside 0f to 1f range. */
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        vFloor *= STEPV;
        mFloor *= STEPM;
        return
                ((1 - m) *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                )))))))
                        + (m *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                ))))))))
                ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from PhantomNoise, incremented if even.
     * They should normally be used for the 8D version of R2, but we only use 7 of the 8 constants.
     * @param x should be premultiplied by 0xEBEDF
     * @param y should be premultiplied by 0xD96EB
     * @param z should be premultiplied by 0xC862B
     * @param w should be premultiplied by 0xB8ACD
     * @param u should be premultiplied by 0xAA323
     * @param v should be premultiplied by 0x9CDA5
     * @param m should be premultiplied by 0x908E3
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, final int m, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v ^ m;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 7;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public float getNoise(float x, float y) {
        return valueNoise(x, y, seed);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return valueNoise(x, y, z, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return valueNoise(x, y, z, w, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return valueNoise(x, y, z, w, u, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return valueNoise(x, y, z, w, u, v, seed);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = (int)seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return valueNoise(x, y, (int) seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return valueNoise(x, y, z, (int) seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return valueNoise(x, y, z, w, (int) seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return valueNoise(x, y, z, w, u, (int) seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return valueNoise(x, y, z, w, u, v, (int) seed);
    }
}
