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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.MathTools.barronSpline;

/**
 * A variant on {@link PhantomNoise} that also produces arbitrary-dimensional continuous noise, but that is optimized
 * for higher-dimensional output (4 and up, in particular). FlanNoise doesn't slow down as rapidly as other forms of
 * noise do when dimensions are added.
 */
@Beta
public class FlanNoise {
    protected final long seed;
    public final int dim, vc;
    public final float sharpness;
    protected float inverse;
    protected final float[] points, wobbleTable;
    protected final float[][] vertices;
    public final int detail;

    public FlanNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public FlanNoise(long seed, int dimension) {
        this(seed, dimension, 8f);
    }

    public FlanNoise(long seed, int dimension, float sharpness) {
        this(seed, dimension, sharpness, 6);
    }
    public FlanNoise(long seed, int dimension, float sharpness, int detail) {
        dim = Math.max(2, dimension);
        this.sharpness = sharpness;
        this.detail = detail;
        vc = dim * detail;
        points = new float[vc];
        vertices = new float[vc][dim];
        this.seed = seed;
        for (int i = 0; i < dim; i++) {
            points[i] = Hasher.randomize3Float(seed+i);
        }
        for (int v = 0; v < vc; v++) {
            double sum = 0.0;
            for (int d = 0; d < dim; d++) {
                double g = QuasiRandomTools.goldenFloat[dim-1][d] * (v + 1) + points[d];
                g -= (int)g;
                g = EnhancedRandom.probit(g);
                vertices[v][d] = (float) g;
                sum += g * g;
            }
            sum = 64.0 / Math.sqrt(sum);
            for (int d = 0; d < dim; d++) {
                vertices[v][d] *= sum;
            }
        }
        inverse = 1f / vc;
        wobbleTable = LineWobble.generateSplineLookupTable((int)(seed ^ seed >>> 32), 0x4000, 64, 1, 1f, 0.5f);
//        printDebugInfo();
    }

    public String serializeToString() {
        return "`" + seed + '~' + dim + '~' + detail + '~' + BitConversion.floatToReversedIntBits(1f/sharpness) + '`';
    }

    public static FlanNoise deserializeFromString(String data) {
        if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int detail =  DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return new FlanNoise(seed, dim, sharp, detail);

    }

    public float getNoise(float... args) {
        for (int v = 0; v < vc; v++) {
            points[v] = 0.0f;
            for (int d = 0; d < dim; d++) {
                points[v] += args[d] * vertices[v][d];
            }
        }
        float result = 0f;
        float warp = 0.5f;
        int s = (int)(seed ^ seed >>> 32);
        for (int i = 0, ctr = 1111111; i < vc; i++, ctr += 1234567) {
            warp = wobbleTable[(s ^= (s << 21 | s >>> 11) + ctr) + (int) (points[i] + 512f * warp) & 0x3FFF];
            result += warp;
        }
//            warp = wobbleTable[(s = (s << 21 | s >>> 11) + 1234567) + (int)(points[dim] + warp) & 0x3FFF];
//            result += warp;
        result *= inverse;
        return (barronSpline(result, sharpness, 0.5f) - 0.5f) * 2f;
//        result = (float) Math.pow(sharpness, result);
//        return (result - 1f) / (result + 1f);
//        result = TrigTools.sin(result);
//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }
}
