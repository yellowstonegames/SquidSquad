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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.grid.PhantomNoise;

import static com.github.tommyettinger.digital.TrigTools.*;

public class TaffyNoise extends PhantomNoise {
    public TaffyNoise() {
        super();
    }

    public TaffyNoise(long seed, int dimension) {
        super(seed, dimension);
    }

    public TaffyNoise(long seed, int dimension, float sharpness) {
        super(seed, dimension, sharpness);
    }

    @Override
    protected double valueNoise() {
        int s = (int)(hasher.seed ^ hasher.seed >>> 32 ^ BitConversion.floatToRawIntBits(working[dim]));
        float sum = 0f;
        for (int i = 0, j = 1; i < dim; i++, j++) {
            float cx = working[i];
            float cy = working[j];
            int idx = (int) (s + cx * 1357 + cy * 421);
            sum += (cos(cx)
                    + SIN_TABLE[idx & TABLE_MASK]
//                    + SIN_TABLE[idx + 4096 & TABLE_MASK]
                    + SIN_TABLE[s & TABLE_MASK]*cy
                    + sin(SIN_TABLE[s + 4096 & TABLE_MASK]*cx)
            );
            s ^= (s << 11 | s >>> 21) + 123456789;
        }
        return wobbleTight(s, sum * inverse);
    }

    @Override
    protected double valueNoise2D() {
        int bits = BitConversion.floatToIntBits(working[dim]);
        int sx = (int)(hasher.seed ^ bits);
        int sy = (int)(hasher.seed >>> 32 ^ (bits << 13 | bits >>> 19));
        float cx = working[0];
        float cy = working[1];
        int idx = (int) (sx + cx * 1357 + cy * 421);
        float sum = (cos(cx)
                + SIN_TABLE[idx & TABLE_MASK]
//                + SIN_TABLE[idx + 4096 & TABLE_MASK]
                + SIN_TABLE[sx & TABLE_MASK]*cy
                + sin(SIN_TABLE[sx + 4096 & TABLE_MASK]*cx)
        );
        idx = (int) (sy + cy * 1357 + cx * 421);
        sum += (cos(cy)
                + SIN_TABLE[idx & TABLE_MASK]
//                + SIN_TABLE[idx + 4096 & TABLE_MASK]
                + SIN_TABLE[sy & TABLE_MASK]*cx
                + sin(SIN_TABLE[sy + 4096 & TABLE_MASK]*cy)
        );
        return wobbleTight(sx + sy, sum * inverse);
    }

    /**
     * A smooth 1D noise function that produces results between 0.0 and 1.0, and is optimized for
     * usage on GWT. This uses cubic interpolation between random peak or valley points.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between 0f and 1f (both inclusive), smoothly changing with value
     */
    public static float wobbleTight(final int seed, float value)
    {
        final int floor = value >= 0f ? (int) value : (int) value - 1;
        int z = seed + floor, x = z++;
        final float start = ((x = ((x = (x ^ x >>> 16) * 0x21fad) ^ x >>> 15) * 0x73597) >>> 1 ^ x >>> 16) * 0x1p-31f,
                end = ((x = ((x = (z ^ z >>> 16) * 0x21fad) ^ x >>> 15) * 0x73597) >>> 1 ^ x >>> 16) * 0x1p-31f;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }

}
