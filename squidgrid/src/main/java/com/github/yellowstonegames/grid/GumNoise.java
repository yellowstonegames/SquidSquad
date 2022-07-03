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

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.random.LineWobble;

/**
 * A variant on {@link TaffyNoise}, and so also a variant on {@link PhantomNoise}, that produces arbitrary-dimensional
 * continuous noise, but that is optimized for higher-dimensional output (4 and up, in particular). GumNoise and
 * TaffyNoise don't slow down as rapidly as other forms of noise do when dimensions are added. GumNoise relies on a
 * large, pre-calculated array of smoothly-changing 1D noise to quickly obtain an equivalent to value noise. Whereas a
 * call to n-dimensional value noise requires {@code pow(2, n)} points to
 * be hashed, each one taking {@code O(n)} operations, GumNoise only requires {@code O(n)} operations for its
 * comparable-to-value-noise primitive step. Phantom uses n+1 calls to n-dimensional value noise per point, and Gum
 * uses n+1 of its faster primitive steps per call. Noise types like Simplex only need to hash n+1 points (each
 * {@code O(n)}), but Simplex loses quality in dimensions past 4 or so. Gum actually gains quality in higher
 * dimensions, and it looks considerably worse in 2 or 3 dimensions than it does in 6 or more.
 */
public class GumNoise extends PhantomNoise {

    private static float[] SIN_TABLE = null;
    private static final int TABLE_MASK = (1 << 14) - 1;

    public GumNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public GumNoise(long seed, int dimension) {
        this(seed, dimension, 0.825f * Math.max(2, dimension));
    }

    public GumNoise(long seed, int dimension, float sharpness) {
        super(seed, dimension, sharpness);
        if(SIN_TABLE == null) SIN_TABLE = LineWobble.generateSplineLookupTable(12345, 1 << 14, 1 << 6, 4, 2f, 0.5f);
    }

    protected static float sin(float wobbles) {
        return SIN_TABLE[(int) (wobbles * 32f) & TABLE_MASK];
    }

    protected static float cos(float wobbles) {
        return SIN_TABLE[(int) (wobbles * 32f) + 4096 & TABLE_MASK];
    }

    @Override
    protected float valueNoise() {
        int s = (int)(hasher.seed ^ hasher.seed >>> 32 ^ BitConversion.floatToRawIntBits(working[dim]));
        float sum = 0f;
        for (int i = 0, j = 1; i < dim; i++, j++) {
            float cx = working[i];
            float cy = working[j];
            int idx = (int) (s + cx * 167 + cy * 71);
            sum += (cos(cx)
                    + SIN_TABLE[idx & TABLE_MASK]
                    - SIN_TABLE[s & TABLE_MASK]*cy
                    - sin(SIN_TABLE[s + 4096 & TABLE_MASK]*cx)
            );
            s ^= (s << 11 | s >>> 21) + 123456789;
        }
        return sin(sum);
    }

    @Override
    protected float valueNoise2D() {
        int bits = BitConversion.floatToIntBits(working[dim]);
        int sx = (int)(hasher.seed ^ bits);
        int sy = (int)(hasher.seed >>> 32 ^ (bits << 13 | bits >>> 19));
        float cx = working[0];
        float cy = working[1];
        int idx = (int) (sx + cx * 167 + cy * 71);
        float sum = (cos(cx)
                + SIN_TABLE[idx & TABLE_MASK]
                - SIN_TABLE[sx & TABLE_MASK]*cy
                - sin(SIN_TABLE[sx + 4096 & TABLE_MASK]*cx)
        );
        idx = (int) (sy + cy * 167 + cx * 71);
        sum += (cos(cy)
                + SIN_TABLE[idx & TABLE_MASK]
                - SIN_TABLE[sy & TABLE_MASK]*cx
                - sin(SIN_TABLE[sy + 4096 & TABLE_MASK]*cy)
        );
        return sin(sum);
    }

}
