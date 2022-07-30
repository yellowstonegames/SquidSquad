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
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * A variant on {@link PhantomNoise} that also produces arbitrary-dimensional continuous noise, but that is optimized
 * for higher-dimensional output (4 and up, in particular). FlanNoise doesn't slow down as rapidly as other forms of
 * noise do when dimensions are added. This has configurable detail; 4 is typically the minimum for decent noise in low
 * dimensions, and detail 5 seems to be high-quality up to at least 6D.
 */
@Beta
public class FlanNoise {
    public long seed;
    public final int dim, vc;
    public final float sharpness;
    protected float inverse;
    protected final float[] points;
    protected final float[][] vertices;
    public final int detail;

    public FlanNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    public FlanNoise(long seed, int dimension) {
        this(seed, dimension, 3f * Math.max(2, dimension));
    }

    public FlanNoise(long seed, int dimension, float sharpness) {
        this(seed, dimension, sharpness, 5);
    }
    public FlanNoise(long seed, int dimension, float sharpness, int detail) {
        dim = Math.max(2, dimension);
        this.sharpness = 1f/sharpness;
        this.detail = detail;
        vc = dim * detail;
        points = new float[vc];
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
            sum = 707.17 / Math.sqrt(sum);
            for (int d = 0; d < dim; d++) {
                vertices[v][d] *= sum;
            }
        }

        inverse = 0.75f / vc;
        printDebugInfo();
    }

    private void printDebugInfo() {
        System.out.println("FlanNoise with dimension " + dim + " and detail " + detail + ": \n" +
                "        float result = 0.0f;\n" +
                "        float warp = 0.0f;");

        final String dimNames = "xyzwuvmnopqrstabcdefghijkl";
        for (int v = 0; v < vc; v++) {
//            System.out.print("final float p" + v + (v < 10 ? " " : "") +  " = ");
//            System.out.print("points[" + v + "] = ");
            System.out.println("            result += warp = TrigTools.SIN_TABLE[((seed = (seed << 17 | seed >>> 15) * 0xBCFD)) + (int) (");
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
            System.out.println("\n                            + 3301f * warp) & 0x3FFF];");
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
        System.out.println("        result = (float) Math.pow(sharpness * " + (3 * dim) + ".0f, result * " + (1f/vc) + "f);\n" +
                "        return (result - 1f) / (result + 1f);\n");
    }
    //LineWobble.generateSplineLookupTable((int)(seed ^ seed >>> 32), 0x4000, 64, 1, 1f, 0.5f);

    public void setSeed(long seed) {
        this.seed = seed;
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
        float result = 0.0f;
        float warp = 0.0f;
        int seed = (int)(this.seed ^ this.seed >>> 32);
        for (int i = 0; i < vc; i++) {
            seed = (seed << 17 | seed >>> 15) * 0xBCFD;
            result += warp = TrigTools.SIN_TABLE[seed + (int) (
                    points[i]
                            + 3301f * warp) & 0x3FFF];
        }
        result *= inverse;
        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);

//        result = (float) Math.pow(sharpness, result * inverse);
//        return (result - 1f) / (result + 1f);

//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
//        return (barronSpline(result, sharpness, 0.5f) - 0.5f) * 2f;
//        result = TrigTools.sin(result);
//        return result / (((sharpness - 1f) * (1f - Math.abs(result))) + 1.0000001f);
    }
}
