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
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A variant on {@link PhantomNoise} that also produces arbitrary-dimensional continuous noise, but that is optimized
 * for higher-dimensional output (4 and up, in particular). TaffyNoise doesn't slow down as rapidly as other forms of
 * noise do when dimensions are added. Whereas a call to n-dimensional value noise requires {@code pow(2, n)} points to
 * be hashed, each one taking {@code O(n)} operations, TaffyNoise only requires {@code O(n)} operations for its
 * comparable-to-value-noise primitive step. Phantom uses n+1 calls to n-dimensional value noise per point, and Taffy
 * uses n+1 of its faster primitive steps per call. Noise types like Simplex only need to hash n+1 points (each
 * {@code O(n)}), but Simplex loses quality in dimensions past 4 or so. Taffy actually gains quality in higher
 * dimensions, and even though it doesn't look best in 2 or 3 dimensions, it looks pretty good with just one octave.
 * <br>
 * Consider using {@link Noise} with its {@link Noise#TAFFY} or {@link Noise#TAFFY_FRACTAL} noise type if you only need
 * noise in 3-7 dimensions (these are produced when requesting 2-6-dimensional noise, with the extra dimension editable
 * via {@link Noise#setMutation(float)}). Noise is faster than this class because it isn't as generalized to operate in
 * arbitrarily-high dimensions.
 */
@Beta
public class TaffyNoise {
    public long seed;
    public final int dim;
    public final float sharpness;
    protected float inverse;
    protected final float[] working, points;
    protected final float[][] vertices;

    public TaffyNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public TaffyNoise(long seed, int dimension) {
        this(seed, dimension, 0.5f + 0.25f * Math.max(2, dimension));
    }

    public TaffyNoise(long seed, int dimension, float sharpness) {
        dim = Math.max(2, dimension);
        this.sharpness = 1f / sharpness;
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
        this.seed = seed;
        inverse = 1f / (dim + 1f);
//        printDebugInfo();
    }

    public String serializeToString() {
        return "`" + seed + '~' + dim + '~' + BitConversion.floatToReversedIntBits(1f/sharpness) + '`';
    }

    public static TaffyNoise deserializeFromString(String data) {
        if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return new TaffyNoise(seed, dim, sharp);

    }

    protected float valueNoise() {
        int s = (int)(seed ^ seed >>> 32 ^ BitConversion.floatToRawIntBits(working[dim]));
        float sum = 0f;
        for (int i = 0, j = 1; i < dim; i++, j++) {
            s = (s << 13 | s >>> 19) + 1234567;
            float cx = working[i];
            if(j == dim) j = 0;
            float cy = working[j];
            int idx = s + (int) (cx * 95 + cy * 21);
            sum += (cos(cx)
                    - SIN_TABLE[idx & TABLE_MASK]
                    - cos(SIN_TABLE[s & TABLE_MASK]*cy)
                    + sin(SIN_TABLE[s + 4096 & TABLE_MASK]*cx)
            );
        }
        return sinTurns(sum * 0.75f * inverse);
    }

    protected float valueNoise2D() {
        int bits = BitConversion.floatToIntBits(working[dim]);
        int sx = (int)(seed ^ seed >>> 32 ^ bits);
        int sy = sx + 1234567 ^ (bits << 13 | bits >>> 19);
        float cx = working[0];
        float cy = working[1];
        int idx = sx + (int) (cx * 95 + cy * 21);
        float sum = (cos(cx)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sx & TABLE_MASK]*cy)
                + sin(SIN_TABLE[sx + 4096 & TABLE_MASK]*cx)
        );
        idx = sy + (int) (cy * 95 + cx * 21);
        sum += (cos(cy)
                - SIN_TABLE[idx & TABLE_MASK]
                - cos(SIN_TABLE[sy & TABLE_MASK]*cx)
                + sin(SIN_TABLE[sy + 4096 & TABLE_MASK]*cy)
        );
        return sinTurns(sum * 0.75f * inverse);
    }

    public float getNoise(float... args) {
        for (int v = 0; v <= dim; v++) {
            points[v] = 0.0f;
            for (int d = 0; d < dim; d++) {
                points[v] += args[d] * vertices[v][d];
            }
        }
        // working[dim] stores what is effectively a changing seed in the array of floats
        working[dim] = 0.6180339887498949f; // inverse golden ratio; irrational, so its bit representation nears random
        float result = 0f;
        float warp = 0f;
        for (int i = 0; i <= dim; i++) {
            for (int j = 0, d = 0; j < dim; j++, d++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            working[0] += warp;
            warp = valueNoise();
            result += warp;
            working[dim] += -0.423310825130748f; // e - pi
        }
        result *= inverse;
        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }

    public float getNoise2D(float x, float y) {
        points[0] = x *  0.60535836f + y *  0.79595304f;
        points[1] = x * -0.99609566f + y *  0.08828044f;
        points[2] = x *  0.45730183f + y * -0.88913614f;

        working[2] = 0.6180339887498949f;
        float result = 0f;
        float warp = 0f;
        for (int i = 0; i <= 2; i++) {
            for (int j = 0, d = 0; j < 2; j++, d++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            working[0] += warp;
            warp = valueNoise();
            result += warp;
            working[2] += -0.423310825130748f;
        }
        result *= inverse;
        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }
}
