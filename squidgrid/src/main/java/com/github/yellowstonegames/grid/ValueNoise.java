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

import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * Arbitrary-dimensional continuous noise that always looks very blocky.
 */
@Beta
public class ValueNoise implements INoise {
    /**
     * The seed for the noise; can be any long.
     */
    public long seed;
    /**
     * How many dimensions of noise to generate; usually at least 2.
     */
    public final int dim;
    protected transient final float[] working, input;
    protected transient final long[] floors;

    public ValueNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public ValueNoise(long seed, int dimension) {
        dim = Math.max(2, dimension);
        input = new float[dim];
        working = new float[dim+1];
        floors = new long[dim+1];
        this.seed = seed;
    }

    public String serializeToString() {
        return "`" + seed + '~' + dim + '`';
    }

    public static ValueNoise deserializeFromString(String data) {
                if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1));

        return new ValueNoise(seed, dim);
    }

    public float getNoise(float... args) {
        return noise(args.length, args);
    }

    protected float noise(int dim, float... args) {
        final long[] gold = QuasiRandomTools.goldenLong[dim];
        final long hashSeed = gold[dim] * seed;
        long hash;
        for (int i = 0; i < dim; i++) {
            floors[i] = args[i] >= 0f ? (long) args[i] : (long) args[i] - 1L;
            working[i] = args[i] - floors[i];
            working[i] *= working[i] * (3f - 2f * working[i]);
        }
        float sum = 0f, temp;
        final int limit = 1 << dim;
        int bit;
        for (int i = 0; i < limit; i++) {
            temp = 1.0f;
            hash = hashSeed;
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + (1|-bit) * working[j];
                hash += (floors[j] - bit) * gold[j];
            }
            hash ^= hash * hash | 1L; // xqo, a xorsquare operation
            sum += temp * (hash >> 32);
        }
        return (sum * 0x1p-31f);
    }

    protected float getNoise2D(float x, float y) {
        final long[] gold = QuasiRandomTools.goldenLong[2];
        final long hashSeed = gold[2] * seed;
        long hash;
        floors[0] = x >= 0f ? (long) x : (long) x - 1L;
        x -= floors[0];
        x *= x * (3f - 2f * x);

        floors[1] = y >= 0f ? (long) y : (long) y - 1L;
        y -= floors[1];
        y *= y * (3f - 2f * y);

        float sum = 0f, temp;
        int bit;
        for (int i = 0; i < 4; i++) {
            temp = 1f;
            hash = hashSeed;
            bit = i & 1;
            temp *= bit + (1|-bit) * x;
            hash += (floors[0] - bit) * gold[0];

            bit = i >>> 1;
            temp *= bit + (1|-bit) * y;
            hash += (floors[1] - bit) * gold[1];

            hash ^= hash * hash | 1L; // xqo, a xorsquare operation
            sum += temp * (hash >> 32);
        }
        return (sum * 0x1p-31f);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValueNoise that = (ValueNoise) o;

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
    public boolean canUseSeed() {
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

    public static final ValueNoise instance2D = new ValueNoise(QuasiRandomTools.goldenLong[12][0], 2);
    public static final ValueNoise instance3D = new ValueNoise(QuasiRandomTools.goldenLong[13][0], 3);
    public static final ValueNoise instance4D = new ValueNoise(QuasiRandomTools.goldenLong[14][0], 4);
    public static final ValueNoise instance5D = new ValueNoise(QuasiRandomTools.goldenLong[15][0], 5);
    public static final ValueNoise instance6D = new ValueNoise(QuasiRandomTools.goldenLong[16][0], 6);
    public static final ValueNoise instance7D = new ValueNoise(QuasiRandomTools.goldenLong[17][0], 7);
    public static final ValueNoise instance8D = new ValueNoise(QuasiRandomTools.goldenLong[18][0], 8);
}
