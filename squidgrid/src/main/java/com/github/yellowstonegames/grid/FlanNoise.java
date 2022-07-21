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
    public final int dim;
    public final float sharpness;
    protected float inverse;
    protected final float[] working, points, wobbleTable;
    protected final float[][] vertices;

    public FlanNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public FlanNoise(long seed, int dimension) {
        this(seed, dimension, 5f);
    }

    public FlanNoise(long seed, int dimension, float sharpness) {
        dim = Math.max(2, dimension);
        this.sharpness = sharpness;
        working = new float[dim+1];
        points = new float[dim+1];
        vertices = new float[dim+1][dim];
        float id = -1f / dim;
        vertices[0][0] = 1f;
        for (int v = 1; v <= dim; v++) {
            vertices[v][0] = id;
        }
        for (int d = 1; d < dim; d++) {
            float t = 0f;
            for (int i = 0; i < d; i++) {
                t += vertices[d][i] * vertices[d][i];
            }
            vertices[d][d] = (float) Math.sqrt(1f - t);
            t = (id - t) / vertices[d][d];
            for (int v = d + 1; v <= dim; v++) {
                vertices[v][d] = t;
            }
        }
        for (int v = 0; v <= dim; v++) {
            final float theta = TrigTools.atan2(vertices[v][1], vertices[v][0]) + Hasher.randomize3Float(v - seed),
                    dist = (float) Math.sqrt(vertices[v][1] * vertices[v][1] + vertices[v][0] * vertices[v][0]);
            vertices[v][0] = TrigTools.cos(theta) * dist;
            vertices[v][1] = TrigTools.sin(theta) * dist;
        }
        for (int v = 0; v <= dim; v++) {
            for (int d = 0; d < dim; d++) {
                vertices[v][d] *= 43f;
            }
        }
        this.seed = seed;
        inverse = 1f / (5f * (dim + 1f));
        wobbleTable = LineWobble.generateSplineLookupTable((int)(seed ^ seed >>> 32), 0x4000, 64, 1, 1f, 0.5f);
//        printDebugInfo();
    }

    public String serializeToString() {
        return "`" + seed + '~' + dim + '~' + BitConversion.floatToReversedIntBits(1f/sharpness) + '`';
    }

    public static FlanNoise deserializeFromString(String data) {
        if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return new FlanNoise(seed, dim, sharp);

    }

    public float getNoise(float... args) {
        for (int v = 0; v <= dim; v++) {
            points[v] = 0.0f;
            for (int d = 0; d < dim; d++) {
                points[v] += (args[d] - 2f) * vertices[v][d];
            }
        }
        float result = 0f;
        float warp = 0.5f, t;
        int s = (int)(seed ^ seed >>> 32);
        for (int v = 1; v < 16384; v += 3275) {
            for (int i = 0; i < dim; i++) {
//                s = (s << 13 | s >>> 19) + 1234567;
                warp += t = wobbleTable[s + (int)(points[i+1]) & 0x3FFF] - wobbleTable[(s += v) + (int)(points[i] + warp) & 0x3FFF];
                result += t;
                for (int d = 0; d < dim; d++) {
                    points[i] += vertices[i][d];
                }
            }
//            s = (s << 13 | s >>> 19) + 1234567;
            warp += t = wobbleTable[s + (int)(points[0]) & 0x3FFF] - wobbleTable[(s += v) + (int)(points[dim] + warp) & 0x3FFF];
            result += t;
            for (int d = 0; d < dim; d++) {
                points[dim] += vertices[dim][d];
            }
        }
//        result *= inverse;
//        return (barronSpline(result, 10f, 0.5f) - 0.5f) * 2f;
        result = (float) Math.pow(sharpness, result);
        return (result - 1f) / (result + 1f);
//        result = TrigTools.sin(result);
//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }
}
