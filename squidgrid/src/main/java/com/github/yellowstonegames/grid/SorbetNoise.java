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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A mix of {@link CyclicNoise} with value noise; much less periodic than CyclicNoise alone. Largely based upon
 * <a href="https://www.shadertoy.com/view/3tcyD7">this ShaderToy by jeyko</a>, which in turn is based on
 * <a href="https://www.shadertoy.com/view/wl3czN">this ShaderToy by nimitz</a>.
 */
@Beta
public class SorbetNoise {

    protected int octaves;
    protected float total = 1f, start = 1f;
    protected final float lacunarity = 1.6f;
    protected final float gain = 0.6f;
    public int seed;

    public SorbetNoise() {
        this(0xD1CEBEEF, 3);
    }

    public SorbetNoise(int seed, int octaves) {
        this.seed = seed;
        setOctaves(octaves);
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);

        start = gain;
        total = 0f;
        for (int i = 0; i < this.octaves; i++) {
            start /= gain;
            total += start;
        }
        total = 1f / total;
    }
    public String serializeToString() {
        return "`" + seed + '~' + octaves + '`';
    }

    public static SorbetNoise deserializeFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        int seed =   DigitTools.intFromDec(data, 1, pos = data.indexOf('~'));
        int octaves =     DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1));

        return new SorbetNoise(seed, octaves);
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
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(int seed, float x, float y, float z)
    {
        final int STEPX = 0xDB4F1;
        final int STEPY = 0xBBE05;
        final int STEPZ = 0xA0F2F;
        int xFloor = MathTools.floor(x);
        x -= xFloor;
        x *= x * (3 - 2 * x);
        int yFloor = MathTools.floor(y);
        y -= yFloor;
        y *= y * (3 - 2 * y);
        int zFloor = MathTools.floor(z);
        z -= zFloor;
        z *= z * (3 - 2 * z);
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, seed)))
                + z *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed)))
        ) * (0x1.0040100401004p-11f) - 0.25f;
    }


    public float getNoise(float x, float y, float z) {
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        float xx, yy, zz;
        int sd = seed;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;

            x += zz;// - yy;
            y += xx;// - zz;
            z += yy;// - xx;

            int xs = (int) (x * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (y * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (z * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sinTurns((
                    SIN_TABLE[xc] * SIN_TABLE[zs] + SIN_TABLE[yc] * SIN_TABLE[xs] + SIN_TABLE[zc] * SIN_TABLE[ys]
//                            + LineWobble.wobble(123, x) + LineWobble.wobble(456, y) + LineWobble.wobble(789, z)
                    ) * valueNoise(sd ^ i, x, y, z)
            ) * amp;

            xx = Noise.rotateX3D(x, y, z);
            yy = Noise.rotateY3D(x, y, z);
            zz = Noise.rotateZ3D(x, y, z);

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }
    private static final float radToIndex = TABLE_SIZE / PI2;
}
