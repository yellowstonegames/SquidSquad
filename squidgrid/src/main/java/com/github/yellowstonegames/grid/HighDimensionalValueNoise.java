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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * Arbitrary-dimensional continuous noise that always looks very blocky.
 */
@Beta
public class HighDimensionalValueNoise implements INoise {
    /**
     * The seed for the noise; can be any long.
     */
    public long seed;
    /**
     * How many dimensions of noise to generate; usually at least 2.
     */
    public int dim;
    protected transient float[] working, input;
    protected transient long[] floors;

    public HighDimensionalValueNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public HighDimensionalValueNoise(long seed, int dimension) {
        dim = Math.max(2, dimension);
        input = new float[dim];
        working = new float[dim<<1];
        floors = new long[dim];
        this.seed = seed;
    }

    public HighDimensionalValueNoise reassign(long seed, int dimension) {
        if(dim != Math.max(2, dimension)) {
            dim = Math.max(2, dimension);
            input = new float[dim];
            working = new float[dim << 1];
            floors = new long[dim];
        }
        this.seed = seed;
        return this;
    }

    @Override
    public String getTag() {
        return "HDVN";
    }

    public String stringSerialize() {
        return "`" + seed + '~' + dim + '`';
    }

    public HighDimensionalValueNoise stringDeserialize(String data) {
        if(data == null || data.length() < 5)
            return this;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1));

        return reassign(seed, dim);
    }

    public static HighDimensionalValueNoise recreateFromString(String data) {
        if(data == null || data.length() < 5)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1));

        return new HighDimensionalValueNoise(seed, dim);
    }

    @Override
    public HighDimensionalValueNoise copy() {
        return new HighDimensionalValueNoise(seed, dim);
    }

    public float getNoise(float... args) {
        return noise(args.length, args);
    }

    protected float noise(final int dim, float... args) {
        final long[] gold = QuasiRandomTools.GOLDEN_LONGS[dim];
        final long hashSeed = gold[dim] * seed;
        long hash;
        for (int i = 0; i < dim; i++) {
            floors[i] = MathTools.longFloor(args[i]);
            final float w = args[i] - floors[i];
            working[i<<1|1] = -(working[i<<1] = w * w * (3f - 2f * w));
            floors[i] *= gold[i];
        }
        float sum = 0f, temp;
        final int limit = 1 << dim;
        int bit;
        for (int i = 0; i < limit; i++) {
            temp = 1.0f;
            hash = hashSeed;
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + working[j<<1|bit];
                hash += floors[j] - (-bit & gold[j]);
            }
            hash ^= hash * hash | 1L; // xqo, a xorsquare operation
            sum += temp * (hash >> 32);
        }
        return (sum * 0x1p-31f);
    }

    protected float getNoise2D(float x, float y) {
        final long[] gold = QuasiRandomTools.GOLDEN_LONGS[2];
        final long hashSeed = gold[2] * seed;
        long hash;
        floors[0] = MathTools.longFloor(x);
        x -= floors[0];
        x *= x * (3f - 2f * x);
        floors[0] *= gold[0];

        floors[1] = MathTools.longFloor(y);
        y -= floors[1];
        y *= y * (3f - 2f * y);
        floors[1] *= gold[1];

        float sum = 0f, temp;
        int bit;
        for (int i = 0; i < 4; i++) {
            temp = 1f;
            hash = hashSeed;
            bit = i & 1;
            temp *= bit + (1|-bit) * x;
            hash += floors[0] - (-bit & gold[0]);

            bit = i >>> 1;
            temp *= bit + (1|-bit) * y;
            hash += floors[1] - (-bit & gold[1]);

            hash ^= hash * hash | 1L; // xqo, a xorsquare operation
            sum += temp * (hash >> 32);
        }
        return (sum * 0x1p-31f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HighDimensionalValueNoise that = (HighDimensionalValueNoise) o;

        if (dim != that.dim) return false;
        return seed == that.seed;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return dim;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public float getNoise(float x, float y) {
        if(dim >= 2) {
            return getNoise2D(x, y);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 2D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z) {
        if(dim >= 3) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            return noise(3, input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 3D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        if(dim >= 4) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            return noise(4, input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 4D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        if(dim >= 5) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            input[4] = u;
            return noise(5, input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 5D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        if(dim >= 6) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            input[4] = u;
            input[5] = v;
            return noise(6, input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 6D noise.");
    }

    public static final HighDimensionalValueNoise instance2D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[12][0], 2);
    public static final HighDimensionalValueNoise instance3D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[13][0], 3);
    public static final HighDimensionalValueNoise instance4D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[14][0], 4);
    public static final HighDimensionalValueNoise instance5D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[15][0], 5);
    public static final HighDimensionalValueNoise instance6D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[16][0], 6);
    public static final HighDimensionalValueNoise instance7D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[17][0], 7);
    public static final HighDimensionalValueNoise instance8D = new HighDimensionalValueNoise(QuasiRandomTools.GOLDEN_LONGS[18][0], 8);
}
