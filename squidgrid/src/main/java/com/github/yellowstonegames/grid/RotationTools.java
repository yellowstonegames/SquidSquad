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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.Ziggurat;

/**
 * This has tools for generating and applying matrix rotations, potentially in higher dimensions than the typical 2 or
 * 3. You can use {@link #randomRotation2D(long)} to very quickly generate a single 2D rotation matrix, and to get a 3D
 * matrix, you can either build on that 2D matrix using {@link #randomRotation3D(long, float[])} or use
 * {@link #randomRotation3D(long)} to make a completely new one. The same follows for making a 4D rotation using a 3D
 * one, and so on. You can apply a rotation to a vector with {@link #rotate(float[], float[], float[])}, and there's
 * also a method to multiply square matrices in {@link #matrixMultiply(float[], float[], float[], int)}.
 * <br>
 * My head hurts. Thanks to spenc on the libGDX Discord for carefully guiding me through this code, among several other
 * people who helped a lot.
 */
public final class RotationTools {
    /**
     * No instantiation.
     */
    private RotationTools() {
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), and an output vector to write to (as a 1D float array), and does the math to
     * rotate {@code input} using {@code rotation}, and add the results into {@code output}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * rotation must be equal to or greater than the length of input times the length of output. Typically, if input has
     * length {@code n} and output has length {@code m}, rotation has length {@code n*m}. This does no bounds checking
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m} or greater
     * @param output the output vector of length {@code m}
     */
    public static void rotate(float[] input, float[] rotation, float[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * Multiplies two square matrices with side length {@code side}, and stores the result in {@code out}. The inputs
     * {@code lf} and {@code rt} are 1D float arrays treated as row-major matrices.
     * @param lf the left input matrix, as row-major
     * @param rt the right input matrix, as row-major
     * @param out will be modified; this is where the output is summed into, and it is not cleared beforehand
     * @param side side length of each input matrix and the output matrix
     */
    public static void matrixMultiply(float[] lf, float[] rt, float[] out, int side) {
        int o = 0;
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                for (int i = 0; i < side; i++) {
                    out[o] += lf[r * side + i] * rt[side * i + c];
                }
                ++o;
            }
        }
    }

//    public static void main(String[] args) {
//        float[] pts = {.11f,.12f,.13f,.21f,.22f,.23f,.31f,.32f,.33f},
//        its = {11,12,13,21,22,23,31,32,33}, out = new float[9], vc = {11,12,13}, o3 = new float[3];
//        matrixMultiply(pts, its, out, 3);
//        for (int y = 0, m = 0; y < 3; y++) {
//            for (int x = 0; x < 3; x++, m++) {
//                System.out.printf("%5.2f, ", out[m]);
//            }
//            System.out.println();
//        }
//        rotate(vc, pts, o3);
//        for (int i = 0; i < 3; i++) {
//            System.out.printf("%5.2f ", o3[i]);
//        }
//    }

    /**
     *
     * @param state change this with {@code randomize((state += 0x9E3779B97F4A7C15L))}
     * @return
     */
    private static long randomize(long state) {
        return (state = ((state = (state ^ 0xDB4F0B9175AE2165L) * 0xC6BC279692B5CC83L) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }
    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. This
     * allocates a new {@code targetSize * targetSize}-element float array on every call and returns it. It also
     * allocates some more temporary float arrays to work with.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D float array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    private static float[] rotateStep(long seed, float[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        float[] gauss = new float[targetSize], house = new float[squareSize], large = new float[squareSize],
                out = new float[squareSize];
        for (int i = 0; i < smallSize; i++) {
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;
        // use Juniper's Ziggurat algorithm directly, which requires randomizing the seed
        seed = randomize(seed + squareSize);
        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = (float) Ziggurat.normal(randomize((seed += 0x9E3779B97F4A7C15L)));
            sum += t * t;
        }
        final float inv = 1f / (float) Math.sqrt(sum);
        sum = 0f;
        t = 1f;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0f;
        }
        sum = MathTools.ROOT2 / (float) Math.sqrt(sum); // reused as c
        t = 1f;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0f;
        }
        for (int row = 0, h = 0; row < targetSize; row++) {
            for (int col = 0; col < targetSize; col++, h++) {
                house[h] = gauss[row] * gauss[col];
            }
        }
        for (int i = 0; i < targetSize; i++) {
            house[targetSize * i + i]--;
        }
        matrixMultiply(house, large, out, targetSize);
        return out;
    }

    /**
     * Creates a new 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given seed to get an angle using
     * {@link TrigTools#SIN_TABLE}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(long seed) {
        final int index = (int)(randomize(seed * 0x9E3779B97F4A7C15L) >>> 50); // 50 == 64 - TrigTools.SIN_BITS
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new float[]{c, s, -s, c};
    }

//    /**
//     * Creates a new 1D float array that can be used as a 3D rotation matrix by
//     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
//     * {@link TrigTools#SIN_TABLE} and three Gaussian floats using {@link Ziggurat}.
//     * @param seed any long; will be scrambled
//     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
//     */
//    public static float[] randomRotation3D(long seed) {
//        final int index = (int)((seed = randomize(seed ^ 0x9E3779B97F4A7C15L)) >>> -TrigTools.SIN_BITS);
//        float x = (float) Ziggurat.normal(randomize(seed += 0x9E3779B97F4A7C15L));
//        float y = (float) Ziggurat.normal(randomize(seed += 0x9E3779B97F4A7C15L));
//        float z = (float) Ziggurat.normal(randomize(seed += 0x9E3779B97F4A7C15L));
//        float sum = x * x + y * y + z * z;
//        final float inv = 1f / (float) Math.sqrt(sum);
//        float t = 1f;
//        sum = 0f;
//        t -= x *= inv; sum += t * t; t = 0f;
//        t -= y *= inv; sum += t * t; t = 0f;
//        t -= z *= inv; sum += t * t; t = 0f;
//
//        sum = MathTools.ROOT2 / (float) Math.sqrt(sum); // reused as c
//        x = (1 - x) * sum;
//        y = (0 - x) * sum;
//        z = (0 - x) * sum;
//
//        final float xx = x * x - 1;
//        final float yy = y * y - 1;
//        final float zz = z * z - 1;
//        final float xy = x * y;
//        final float xz = x * z;
//        final float yz = y * z;
//
//        final float s = TrigTools.SIN_TABLE[index];
//        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
//        final float sxy = s * xy, cxy = c * xy;
//        return new float[]{
//                c * xx - sxy   , s * xx + cxy   , xz,
//                cxy    - s * yy, sxy    + c * yy, yz,
//                c * xz - s * yz, s * xz + c * yz, zz};
//    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(long seed) {
        return rotateStep(seed, randomRotation2D(seed - 3), 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 2D rotation matrix to avoid redoing
     * any generation work already done for 2D. There will probably be some correlation between the appearance of the 2D
     * rotation this will build upon and the 3D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation3D(long seed, float[] rotation2D) {
        return rotateStep(seed, rotation2D, 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed) {
        return rotateStep(seed, randomRotation3D(seed - 4), 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 3D rotation matrix to avoid redoing
     * any generation work already done for 3D. There will probably be some correlation between the appearance of the 3D
     * rotation this will build upon and the 4D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed, float[] rotation3D) {
        return rotateStep(seed, rotation3D, 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(long seed) {
        return rotateStep(seed, randomRotation4D(seed - 5), 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 4D rotation matrix to avoid redoing
     * any generation work already done for 4D. There will probably be some correlation between the appearance of the 4D
     * rotation this will build upon and the 5D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(long seed, float[] rotation4D) {
        return rotateStep(seed, rotation4D, 5);
    }
    
    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(long seed) {
        return rotateStep(seed, randomRotation5D(seed - 6), 6);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 5D rotation matrix to avoid redoing
     * any generation work already done for 5D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 6D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(long seed, float[] rotation5D) {
        return rotateStep(seed, rotation5D, 6);
    }
}
