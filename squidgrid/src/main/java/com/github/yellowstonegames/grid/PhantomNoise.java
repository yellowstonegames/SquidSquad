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

import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.DigitTools;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;

/**
 * Arbitrary-dimensional continuous noise that maintains most of the same style even as the dimensionality gets fairly
 * high. If you know what dimension of noise you need, and it's covered by {@link Noise} (meaning it's 2D, 3D, 4D, 5D,
 * or 6D noise), then using Noise with the {@link Noise#FOAM} noise type will give you a significantly faster version of
 * approximately the same algorithm. If your noise has an unknown dimension count, this won't really work either, since
 * PhantomNoise needs to do some preparation and allocation for a specific dimension in its constructor. But, if you
 * know what the range of dimensions is, and the lowest is at least 2D, you can make one PhantomNoise per dimension
 * easily enough.
 * <br>
 * The algorithm this uses is surprisingly simple. To produce N-D noise, it makes N+1 calls to N-D value noise, with
 * each call using a different rotation of the same size of grid. It "domain warps" value noise calls after the first,
 * adding the previous value noise result to one coordinate of the next value noise call. To get a PhantomNoise result,
 * it averages all the value noise calls and curves the output range so it doesn't get more biased toward 0 with higher
 * dimensions (which would happen with a pure average of a rising number of variables). The curving here uses the
 * {@link MathTools#barronSpline(double, double, double)} method for adjustable bias/gain; it looks
 * <a href="https://i.imgur.com/fchQOuP.png">like this for 2D</a>,
 * <a href="https://i.imgur.com/vpc2XGq.png">like this for 3D</a>, and
 * <a href="https://i.imgur.com/fNWJOEp.png">like this for 4D</a>.
 * There's some preparation this does in the constructor, which eliminates the need for allocations during noise
 * generation. For N-D PhantomNoise, this makes N+1 double arrays, one for each rotation for a value noise call, and
 * each rotation array has N items. The rotations match the vertices of an N-simplex, so a triangle in 2D, a tetrahedron
 * in 3D, etc. It also stores two working-room arrays, each with N+1 double items, two frequently-edited int arrays of
 * the floors of doubles it's using, and of modified versions of those floors to be hashed (each with N+1 items), and
 * interestingly, a {@link Hasher} hash functor, seeded in the PhantomNoise constructor.
 * <br>
 * At higher dimensions, Simplex noise (what {@link Noise#SIMPLEX} produces) starts to change how it looks compared to
 * lower dimensions. PhantomNoise, on the other hand, maintains a fairly consistent blob-like organic look, such as
 * <a href="https://i.imgur.com/wxomHAc.png">this 6D PhantomNoise sample</a>. Red and purple mark the highest and lowest
 * possible values, respectively, and while they appear plenty in PhantomNoise, they are absent in
 * <a href="https://i.imgur.com/2t7hRtk.png">this 6D SeededNoise sample</a>. There may be differences in how the inputs
 * are handled between the two samples, but 6D Simplex generally suffers from "the curse of dimensionality" more-so than
 * PhantomNoise (or Perlin noise like {@link Noise#PERLIN}, somewhat surprisingly), with the "curse" affecting the
 * density of information in higher-dimensional space.
 */
public class PhantomNoise implements INoise {
    /**
     * Effectively, this contains the seed for the noise.
     */
    public Hasher hasher;
    /**
     * How many dimensions of noise to generate; usually at least 2.
     */
    public int dim;
    /**
     * This should go up linearly with dimension, typically, and is usually 0.825 times {@link #dim}.
     * It can be raised to make the noise more starkly black and white, or lowered to have more mid-gray values.
     */
    public float sharpness;
    /**
     * Just {@code 1f / (dim + 1f)} .
     */
    protected float inverse;
    /**
     * Assigned in getNoise() to a rotated version of the input, and used in valueNoise() to get the rotated-grid value
     * noise result. Has {@code dim + 1} elements, but one is effectively a changing "seed-like" input to differentiate
     * each rotation.
     */
    protected transient float[] working;
    /**
     * Stores the {@code dim + 1} dot-products of each vertex with each input.
     */
    protected transient float[] points;
    /**
     * Used with the INoise methods to assign individual arguments into a saved array, rather than allocating a new one.
     */
    protected transient float[] input;
    /**
     * Stores {@code dim + 1} vertices, each an array made of {@code dim} floats. These are the vertices of a simplex
     * (an analogue to a triangle or tetrahedron) in {@code dim} dimensions.
     */
    protected transient float[][] vertices;
    /**
     * Stores the floors of each input to valueNoise(). These are used to compute cubic interpolations of
     * {@code working} to smooth the value noise. This has {@code dim + 1} items, because {@code working} stores an
     * extra float to differentiate rotations.
     */
    protected transient int[] floors;
    /**
     * Passed to {@link Hasher#hash(long, float[])} after modifications to get a random-seeming int for each vertex on
     * the square-like grid in valueNoise(). Has {@code dim + 1} items, because {@code working} stores an
     * extra float to differentiate rotations.
     */
    protected transient int[] hashFloors;

    /**
     * Builds a PhantomNoise with the {@code 0xFEEDBEEF1337CAFEL} as its seed, that takes 3 inputs per noise call, and
     * uses {@code 0.825f * 3} as the sharpness (for contrast).
     */
    public PhantomNoise() {
        this(0xFEEDBEEF1337CAFEL, 3);
    }

    /**
     * Builds a PhantomNoise with the specified {@code seed}, that takes {@code dimension} inputs per noise call, and
     * uses {@code 0.825f * dimension} to determine the sharpness (for contrast).
     * @param seed may be any long; if the seed is the same and the same inputs are given, the noise will be identical
     * @param dimension how many inputs it will take each time {@link #getNoise(float...)} is called
     */
    public PhantomNoise(long seed, int dimension) {
        this(seed, dimension, 0.825f * Math.max(2, dimension));
    }

    /**
     * Builds a PhantomNoise with the specified {@code seed}, that takes {@code dimension} inputs per noise call, and
     * uses the given {@code sharpness} to determine contrast.
     * @param seed may be any long; if the seed is the same and the same inputs are given, the noise will be identical
     * @param dimension how many inputs it will take each time {@link #getNoise(float...)} is called
     * @param sharpness typically about {@code 0.825f * dimension}; higher makes more contrasting noise
     */
    public PhantomNoise(long seed, int dimension, float sharpness) {
        dim = Math.max(2, dimension);
        this.sharpness = sharpness;
        input = new float[dim];
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
        //I'm not sure if there is a good way to reliably randomize the shape or rotation here.
        //This can cause issues, which is why it is now commented out.
//        for (int v = 0; v <= dim; v++) {
//            final float theta = TrigTools.atan2TurnsFinite(vertices[v][1], vertices[v][0]) + Hasher.randomize3Float(v - seed),
//                    dist = (float) Math.sqrt(vertices[v][1] * vertices[v][1] + vertices[v][0] * vertices[v][0]);
//            vertices[v][0] = TrigTools.cosSmootherTurns(theta) * dist;
//            vertices[v][1] = TrigTools.sinSmootherTurns(theta) * dist;
//        }
        hasher = new Hasher(seed);
        floors = new int[dim+1];
        hashFloors = new int[dim+1];
        inverse = 1f / (dim + 1f);
//        printDebugInfo();
    }

    /**
     * Reassigns all members of this class at once, doing the same work the constructor does, but without creating a new
     * PhantomNoise. This can also avoid recreating arrays if {@link #dim} is equal to {@code dimension}.
     * @param seed may be any long; if the seed is the same and the same inputs are given, the noise will be identical
     * @param dimension how many inputs it will take each time {@link #getNoise(float...)} is called
     * @param sharpness typically about {@code 0.825f * dimension}; higher makes more contrasting noise
     * @return this PhantonNoise, after modification
     */
    public PhantomNoise reassign(long seed, int dimension, float sharpness) {
        this.sharpness = sharpness;
        boolean unchanged = (dim == Math.max(2, dimension)) && hasher.seed == seed;
        if(!unchanged) {
            dim = Math.max(2, dimension);
            input = new float[dim];
            working = new float[dim + 1];
            points = new float[dim + 1];
            vertices = new float[dim + 1][dim];

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
            floors = new int[dim+1];
            hashFloors = new int[dim+1];
            inverse = 1f / (dim + 1f);
        }
//        for (int v = 0; v <= dim; v++) {
//            final float theta = TrigTools.atan2Finite(vertices[v][1], vertices[v][0]) + Hasher.randomize3Float(v - seed),
//                    dist = (float) Math.sqrt(vertices[v][1] * vertices[v][1] + vertices[v][0] * vertices[v][0]);
//            vertices[v][0] = TrigTools.cos(theta) * dist;
//            vertices[v][1] = TrigTools.sin(theta) * dist;
//        }
        hasher = new Hasher(seed);
        return this;
    }

    @Override
    public String getTag() {
        return "PhaN";
    }

    public String stringSerialize() {
        return "`" + hasher.seed + '~' + dim + '~' + BitConversion.floatToReversedIntBits(sharpness) + '`';
    }

    public PhantomNoise stringDeserialize(String data) {
                if(data == null || data.length() < 7)
            return this;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return reassign(seed, dim, sharp);
    }

    public static PhantomNoise recreateFromString(String data) {
                if(data == null || data.length() < 7)
            return null;
        int pos;
        long seed =   DigitTools.longFromDec(data, 1, pos = data.indexOf('~'));
        int dim =     DigitTools.intFromDec(data, pos+1, pos = data.indexOf('~', pos+1));
        float sharp = BitConversion.reversedIntBitsToFloat(DigitTools.intFromDec(data, pos+1, data.indexOf('`', pos+1)));

        return new PhantomNoise(seed, dim, sharp);
    }

    @Override
    public PhantomNoise copy() {
        return new PhantomNoise(hasher.seed, dim, sharpness);
    }

    protected float valueNoise(int dim) {
        hashFloors[dim] = BitConversion.floatToRawIntBits(working[dim]);
        for (int i = 0; i < dim; i++) {
            floors[i] = working[i] >= 0f ? (int)working[i] : (int)working[i] - 1;
            working[i] -= floors[i];
            working[i] *= working[i] * (3f - 2f * working[i]);
        }
        float sum = 0f, temp;
        final int limit = 1 << dim;
        int bit;
        for (int i = 0; i < limit; i++) {
            temp = 1.0f;
            for (int j = 0; j < dim; j++) {
                bit = (i >>> j & 1);
                temp *= bit + (1|-bit) * working[j];
                hashFloors[j] = floors[j] - bit;
            }
            sum += temp * hasher.hash(hashFloors, dim + 1);
        }
        return (sum * 0x1p-32f + 0.5f);
    }

    protected float valueNoise2D() {
        hashFloors[2] = BitConversion.floatToRawIntBits(working[2]);
        for (int i = 0; i < 2; i++) {
            floors[i] = working[i] >= 0f ? (int)working[i] : (int)working[i] - 1;
            working[i] -= floors[i];
            working[i] *= working[i] * (3f - 2f * working[i]);
        }
        float sum = 0f, temp;
        int bit;
        for (int i = 0; i < 4; i++) {
            temp = 1f;
            
            bit = i & 1;
            temp *= bit + (1|-bit) * working[0];
            hashFloors[0] = floors[0] - bit;
            
            bit = i >>> 1;
            temp *= bit + (1|-bit) * working[1];
            hashFloors[1] = floors[1] - bit;
            
            sum += temp * hasher.hash(hashFloors, 3);
        }
        return (sum * 0x1p-32f + 0.5f);
    }
    
    public float getNoise(float... args) {
        return noise(args.length, args);
    }
    public float noise(int used, float... args) {
        for (int v = 0; v <= dim; v++) {
            points[v] = 0.0f;
            for (int d = 0; d < dim; d++) {
                points[v] += args[d] * vertices[v][d];
            }
        }
        // working[dim] stores what is effectively a changing seed in the array of floats, so the Hasher uses it
        working[dim] = 0.6180339887498949f; // inverse golden ratio; irrational, so its bit representation nears random
        float result = 0f;
        float warp = 0f;
        for (int i = 0; i <= dim; i++) {
            for (int j = 0, d = 0; j < dim; j++, d++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            working[0] += warp;
            warp = valueNoise(used);
            result += warp;
            working[dim] += -0.423310825130748f; // e - pi
        }
        result *= inverse;
        // Barron Spline with turning = 0.5 and shape = sharpness
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharpness * diff) * one) - sign - sign) - 1f;
//        return (result <= 0.5)
//                ? Math.pow(result * 2, dim) - 1.0
//                : Math.pow((result - 1) * 2, dim) * (((dim & 1) << 1) - 1) + 1.0;
        
//        for (int i = 1; i < dim; i++) {
//            result *= result * (3.0 - 2.0 * result);
//        }
//        return  (result * result * (6.0 - 4.0 * result) - 1.0);
    }

    public float getNoise2D(float x, float y) {
        points[0] = -0.4161468365471422f * x + 0.9092974268256818f * y;
        points[1] = -0.5794012529532914f * x + -0.8150424455671962f * y;
        points[2] = 0.9955480895004332f * x + -0.09425498125848553f * y;
        working[2] = 0.6180339887498949f;
        float result = 0f;
        float warp = 0f;
        for (int i = 0; i <= 2; i++) {
            for (int j = 0, d = 0; j < 2; j++, d++) {
                if(d == i) d++;
                working[j] = points[d];
            }
            working[0] += warp;
            warp = valueNoise2D();
            result += warp;
            working[2] += -0.423310825130748f;
        }
        result *= inverse;
        // Barron Spline with turning = 0.5 and shape = sharpness
        final float diff = 0.5f - result;
        final int sign = BitConversion.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharpness * diff) * one) - sign - sign) - 1f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhantomNoise that = (PhantomNoise) o;

        if (dim != that.dim) return false;
        if (Float.compare(that.sharpness, sharpness) != 0) return false;
        return hasher.seed == that.hasher.seed;
    }

    void printDebugInfo() {
        System.out.println("PhantomNoise with Dimension " + dim + ":");
        final String dimNames = "xyzwuvmnopqrstabcdefghijkl";
        for (int v = 0; v <= dim; v++) {
            System.out.print("final float p" + v + " = ");
//            System.out.print("points[" + v + "] = ");
            for (int i = 0; i < dim; i++) {
                if(vertices[v][i] != 0f) {
                    if(i > 0)
                        System.out.print(" + ");
                    if(vertices[v][i] == 1f)
                        System.out.print(dimNames.charAt(i % dimNames.length()));
                    else 
                        System.out.print(dimNames.charAt(i % dimNames.length()) + " * " + vertices[v][i] + "f");
                }
            }
            System.out.println(';');
        }
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
        return false;
    }

    @Override
    public long getSeed() {
        return hasher.seed;
    }

    @Override
    public void setSeed(long seed) {
        reassign(seed, dim, sharpness);
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

    public static final PhantomNoise instance2D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[2][0], 2);
    public static final PhantomNoise instance3D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[3][0], 3);
    public static final PhantomNoise instance4D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[4][0], 4);
    public static final PhantomNoise instance5D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[5][0], 5);
    public static final PhantomNoise instance6D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[6][0], 6);
    public static final PhantomNoise instance7D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[7][0], 7);
    public static final PhantomNoise instance8D = new PhantomNoise(QuasiRandomTools.GOLDEN_LONGS[8][0], 8);
}
