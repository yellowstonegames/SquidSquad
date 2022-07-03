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

import static com.github.tommyettinger.digital.TrigTools.TABLE_MASK;

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

    private static float[] WOBBLE = null;

    public GumNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public GumNoise(long seed, int dimension) {
        this(seed, dimension, 0.825f * Math.max(2, dimension));
    }

    public GumNoise(long seed, int dimension, float sharpness) {
        super(seed, dimension, sharpness * 0.3f);
        if(WOBBLE == null) WOBBLE = LineWobble.generateSplineLookupTable(12345, 1 << 14, 1 << 6, 4, 2f, 0.5f);
    }

    protected static float sinW(float wobbles) {
        return WOBBLE[(int) (wobbles * 24f) & TABLE_MASK];
    }

    protected static float cosW(float wobbles) {
        return WOBBLE[(int) (wobbles * 24f) + 4096 & TABLE_MASK];
    }

    @Override
    protected float valueNoise() {
        int sx = (int)(hasher.seed ^ BitConversion.floatToRawIntBits(working[dim]));
        int sy = (int)(hasher.seed >>> 32);
        float sum = 0f;
        for (int i = 0, j = 1; i < dim; i++, j++) {
            float cx = working[i];
            float cy = working[j];
            sum += (cosW(cx)
                    + WOBBLE[sx & TABLE_MASK]
                    - WOBBLE[sx + 4096 & TABLE_MASK]*cy
                    - sinW(cx*WOBBLE[sy + (int)(cy * 7.7f) & TABLE_MASK])
            );
            sx ^= (sx << 11 | sx >>> 21) + 123456789;
            sy ^= (sy << 19 | sy >>> 13) + 987654321;
        }
        return sinW(sum);
    }

    @Override
    protected float valueNoise2D() {
        int sx = (int)(hasher.seed ^ BitConversion.floatToRawIntBits(working[dim]));
        int sy = (int)(hasher.seed >>> 32);
        float cx = working[0];
        float cy = working[1];
        float sum = (cosW(cx)
                + WOBBLE[sx & TABLE_MASK]
                - WOBBLE[sx + 4096 & TABLE_MASK]*cy
                - sinW(cx*WOBBLE[sy + (int)(cy * 7.7f) & TABLE_MASK])
        );
        sum += (cosW(cy)
                + WOBBLE[sy & TABLE_MASK]
                - WOBBLE[sy + 4096 & TABLE_MASK]*cx
                - sinW(cy*WOBBLE[sx + (int)(cx * 7.7f) & TABLE_MASK])
        );
        return sinW(sum);
    }

}
