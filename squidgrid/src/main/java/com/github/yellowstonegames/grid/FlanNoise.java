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

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

import java.util.Arrays;

/**
 * A variant on {@link PhantomNoise} that also produces arbitrary-dimensional continuous noise, but that is optimized
 * for higher-dimensional output (4 and up, in particular). FlanNoise doesn't slow down as rapidly as other forms of
 * noise do when dimensions are added. This has configurable detail; 3 is typically the minimum for decent noise in low
 * dimensions, but it can be set higher independently of fractal octaves.
 */
@Beta
public class FlanNoise implements INoise {
//    /**
//     * Generates a balanced wobbly line by shrinking sections of a sine wave, with an equal amount shrunken from the
//     * negative side as the positive side. Fills the generated data into result, which must have length 16384 (0x4000).
//     * You can call this with {@link #WOBBLE} as the result and any seed you want to change the static line data.
//     * @param result a float array that will be modified; must have length 16384 (0x4000)
//     * @param seed a long seed to determine some shuffled ordering
//     */
//    public static void generateLookupTable(float[] result, long seed) {
//        float[] high = new float[32], low = new float[32];
//        int m = 0;
//        for (float f = 0x1p-6f; f < 1f; f+= 0x1p-5f, m++) {
//            high[m] = low[m] = f;
//        }
//        WhiskerRandom random = new WhiskerRandom(seed);
//        random.shuffle(high);
//        random.shuffle(low);
//        for (int outer = 0, idx = 0; outer < 32; outer++) {
//            for (int lobe = 0; lobe < 0x2000; lobe += 32) {
//                result[idx++] = TrigTools.SIN_TABLE[lobe] * high[outer];
//            }
//            for (int lobe = 0x2000; lobe < 0x4000; lobe += 32) {
//                result[idx++] = TrigTools.SIN_TABLE[lobe] * low[outer];
//            }
//        }
//    }
//    public static final float[] WOBBLE = new float[0x4000];
//    static {
//        generateLookupTable(WOBBLE, 1234567890123L);
//    }

//    public static void main(String[] args) {
//        double sum = 0;
//        for (int i = 0; i < 0x4000; i++) {
//            sum += wobble[i];
//        }
//        System.out.println(sum);
//    }

    public long seed;
    public final int dim;
    public final float sharpness;
    public final int detail;
    protected transient final int vc;
    protected transient float inverse;
    protected transient final float[] points, input;
    protected transient final float[][] vertices;

    public FlanNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public FlanNoise(long seed, int dimension) {
        this(seed, dimension, Math.max(2, dimension));
    }

    public FlanNoise(long seed, int dimension, float sharpness) {
        this(seed, dimension, sharpness, 3);
    }
    public FlanNoise(long seed, int dimension, float sharpness, int detail) {
        dim = Math.max(2, dimension);
        this.sharpness = 0.625f / sharpness;
        this.detail = detail;
        vc = dim * detail;
        points = new float[vc];
        input = new float[dim];
        vertices = new float[vc][dim];
        this.seed = seed;
        for (int v = 0; v < vc; v++) {
            double sum = 0.0;
            for (int d = 0; d < dim; d++) {
                double g = QuasiRandomTools.goldenFloat[dim-1][d] * (v + 1);
                g -= (int)g;
                g = EnhancedRandom.probit(g);
                vertices[v][d] = (float) g;
                sum += g * g;
            }
            sum = 1f / Math.sqrt(sum);
            for (int d = 0; d < dim; d++) {
                vertices[v][d] *= sum;
            }
        }

        inverse = 1f / vc;
//        printDebugInfo();
    }

    private void printDebugInfo() {
        final String dimNames = "xyzwuvmnopqrstabcdefghijkl";
        System.out.println("FlanNoise with dimension " + dim + " and detail " + detail + ": \n" +
                "        float result = 0.0f;\n" +
                "        float warp = 0.0f;");
        {
            int v = vc - 1;
            for (int d = 0; d < dim; d++) {
                if (vertices[v][d] != 0.0) {
                    if (d == 0)
                        System.out.print("        float p = ");
                    else
                        System.out.print(" + ");
                    if (vertices[v][d] == 1.0)
                        System.out.print(dimNames.charAt(d % dimNames.length()));
                    else
                        // swap this in during dev, swap it out to work on GWT
//                        System.out.printf("%s * % 11.6ff", dimNames.charAt(d % dimNames.length()), vertices[v][d]);
                        System.out.print(dimNames.charAt(d % dimNames.length()) + " * " + vertices[v][d] + "f");
                }
            }
        }
        System.out.println(";");
        for (int v = 0; v < vc; v++) {
//            System.out.print("final float p" + v + (v < 10 ? " " : "") +  " = ");
//            System.out.print("points[" + v + "] = ");
            if((v & 1) == 0)
                System.out.println("            result += warp = TrigTools.sin(p - warp) * TrigTools.SIN_TABLE[(seed ^= (seed << 21 | seed >>> 11) + 0x9E3779B9) + (int) ((p = ");
            else
                System.out.println("            result += warp = TrigTools.sin(p - warp) * TrigTools.SIN_TABLE[(seed >>> 18) + (int) ((p = ");
            for (int d = 0; d < dim; d++) {
                if(vertices[v][d] != 0.0)
                {
                    if(d == 0)
                        System.out.print("                            ");
                    else
                        System.out.print(" + ");
                    if(vertices[v][d] == 1.0)
                        System.out.print(dimNames.charAt(d % dimNames.length()));
                    else
                        // swap this in during dev, swap it out to work on GWT
//                        System.out.printf("%s * % 11.6ff", dimNames.charAt(d % dimNames.length()), vertices[v][d]);
                        System.out.print(dimNames.charAt(d % dimNames.length()) + " * " + vertices[v][d] + "f");
                }
            }
            System.out.println("\n                            ) + 4213f * warp) & 0x3FFF];");
        }


//        for (int v = 0; v < vc; v++) {
//            System.out.print("{ ");
//            for (int d = 0; d < vertices[v].length; d++) {
//          //       swap this in during dev, swap it out to work on GWT
////                System.out.printf("% 11.6ff, ", vertices[v][d]);
//                System.out.print(vertices[v][d] + "f, ");
//            }
//            System.out.println("},");
//        }
        System.out.println("        final float sharp = sharpnessInverse * (0.75f/"+dim+"f);\n" +
                "        result *= "+(1f / vc)+";\n" +
                "        return result / (((sharp - 1f) * (1f - Math.abs(result))) + 1.0000001f);\n\n");
    }
    //LineWobble.generateSplineLookupTable((int)(seed ^ seed >>> 32), 0x4000, 64, 1, 1f, 0.5f);

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String serializeToString() {
        return "`" + seed + '~' + dim + '~' + detail + '~' + BitConversion.floatToReversedIntBits(sharpness) + '`';
    }

    public static FlanNoise deserializeFromString(String data) {
        if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        int detail =  DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return new FlanNoise(seed, dim, 0.625f / sharp, detail);
    }

    public float getNoise(float... args) {
        for (int v = 0; v < vc; v++) {
            points[v] = 0.0f;
            for (int d = 0; d < dim; d++) {
                points[v] += args[d] * vertices[v][d];
            }
        }
        float result = 0.0f;
        float warp = 0.5f;
        int seed = (int)(this.seed ^ this.seed >>> 32);
        result += warp = TrigTools.sin(points[vc-1] - warp) * TrigTools.SIN_TABLE[((seed ^= (seed << 21 | seed >>> 11) + 0x9E3779B9) + (int) (
                (points[0]) + 4213f * warp)) & 0x3FFF];
        for (int v = 1; v < vc; v++) {
            result += warp = TrigTools.sin(points[v-1] - warp) * TrigTools.SIN_TABLE[((seed ^= (seed << 21 | seed >>> 11) + 0x9E3779B9) + (int) (
                    (points[v]) + 4213f * warp)) & 0x3FFF];
        }
//        return result * inverse;

        result *= inverse;
        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);

//        result = (float) Math.pow(sharpness, result * inverse);
//        return (result - 1f) / (result + 1f);

//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        return (barronSpline(result, sharpness, 0.5f) - 0.5f) * 2f;
//        return TrigTools.sinTurns(result * inverse);
//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }

    public String toString() {
        return "FlanNoise in " + dim +"D with seed " + seed + ", detail " + detail + ", and sharpness " + (0.625f/sharpness);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlanNoise flanNoise = (FlanNoise) o;

        if (seed != flanNoise.seed) return false;
        if (dim != flanNoise.dim) return false;
        if (Float.compare(flanNoise.sharpness, sharpness) != 0) return false;
        return detail == flanNoise.detail;
    }

    @Override
    public int hashCode() {
        int result = (int) (seed ^ (seed >>> 32));
        result = 31 * result + dim;
        result = 31 * result + (sharpness != +0.0f ? BitConversion.floatToIntBits(sharpness) : 0);
        result = 31 * result + detail;
        return result;
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
        return false;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public float getNoise(float x, float y) {
        if(dim > 2) Arrays.fill(input, 2, input.length, 0f);
        if(dim >= 2) {
            input[0] = x;
            input[1] = y;
            return getNoise(input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 2D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z) {
        if(dim > 3) Arrays.fill(input, 3, input.length, 0f);
        if(dim >= 3) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            return getNoise(input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 3D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        if(dim > 4) Arrays.fill(input, 4, input.length, 0f);
        if(dim >= 4) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            return getNoise(input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 4D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        if(dim > 5) Arrays.fill(input, 5, input.length, 0f);
        if(dim >= 5) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            input[4] = u;
            return getNoise(input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 5D noise.");
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        if(dim > 6) Arrays.fill(input, 6, input.length, 0f);
        if(dim >= 6) {
            input[0] = x;
            input[1] = y;
            input[2] = z;
            input[3] = w;
            input[4] = u;
            input[5] = v;
            return getNoise(input);
        }
        throw new UnsupportedOperationException("Insufficient dimensions available for 6D noise.");
    }
}
