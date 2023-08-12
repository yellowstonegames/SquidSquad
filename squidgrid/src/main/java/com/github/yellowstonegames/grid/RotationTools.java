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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.Ziggurat;

import java.util.Arrays;

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
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m} or greater
     * @param output the output vector of length {@code m}
     */
    public static void rotate(float[] input, float[] rotation, float[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; m < rotation.length && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), an output vector to write to (as a 1D float array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m}
     * @param output the output vector of length {@code m} or greater; only {@code rotation.length / input.length} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     */
    public static void rotate(float[] input, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / input.length;
        for (int r = 0; r < input.length; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), an output vector to write to (as a 1D float array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector that has length at least equal to {@code offsetIn + sizeIn}
     * @param offsetIn the index in {@code input} to start reading
     * @param sizeIn how many elements to read from {@code input}
     * @param rotation a rotation matrix of length {@code sizeIn * m}, where {@code m} is the length of an output vector
     * @param output the output vector of length {@code m} or greater; only {@code rotation.length / sizeIn} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     */
    public static void rotate(float[] input, int offsetIn, int sizeIn, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / sizeIn, inEnd = offsetIn + sizeIn;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
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

    // Section using a long seed and randomize()

    /**
     * A more-specific variation on {@link com.github.tommyettinger.digital.Hasher#randomize1(long)}, this expects its
     * inputs to change by a very large amount between every call, such as {@code 0x9E3779B97F4A7C15L}.
     * This is not as high-quality on its own as any of Hasher's randomize methods, but this class assumes the other
     * random factors in things like {@link Ziggurat} will make up for any lacking quality here.
     * @param state change this with {@code randomize((state += 0x9E3779B97F4A7C15L))}
     * @return a long that has been deterministically randomized from state
     */
    public static long randomize(long state) {
        return (state = ((state = (state ^ 0xDB4F0B9175AE2165L) * 0xC6BC279692B5CC83L) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. This
     * allocates a new {@code targetSize * targetSize}-element float array on every call and returns it. It also
     * allocates some more temporary float arrays to work with.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D float array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(long seed, float[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        // might be able to get rid of these allocations by holding onto some space...
        float[] gauss = new float[targetSize], house = new float[squareSize], large = new float[squareSize],
                out = new float[squareSize];
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;
        seed = randomize(seed + squareSize);
        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            // use Juniper's Ziggurat algorithm to generate Gaussians directly, which requires randomizing the seed
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
        sum = MathTools.ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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

    // Section using a Random generator

    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * generator, and uses the seed and some matrix operations to generate a random rotation based on {@code small}.
     * This allocates a new {@code targetSize * targetSize}-element float array on every call and returns it. It also
     * allocates some more temporary float arrays to work with.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param random random number generator; an EnhancedRandom from the juniper library
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D float array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(EnhancedRandom random, float[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        // might be able to get rid of these allocations by holding onto some space...
        float[] gauss = new float[targetSize], house = new float[squareSize], large = new float[squareSize],
                out = new float[squareSize];
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;

        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = (float) random.nextGaussian();
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
        sum = MathTools.ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
     * {@link #rotate(float[], float[], float[])}. Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(EnhancedRandom random) {
        final int index = random.next(TrigTools.SIN_BITS);
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new float[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(EnhancedRandom random) {
        return rotateStep(random, randomRotation2D(random), 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation3D(EnhancedRandom random, float[] rotation2D) {
        return rotateStep(random, rotation2D, 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(EnhancedRandom random) {
        return rotateStep(random, randomRotation3D(random), 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(EnhancedRandom random, float[] rotation3D) {
        return rotateStep(random, rotation3D, 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(EnhancedRandom random) {
        return rotateStep(random, randomRotation4D(random), 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(EnhancedRandom random, float[] rotation4D) {
        return rotateStep(random, rotation4D, 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(EnhancedRandom random) {
        return rotateStep(random, randomRotation5D(random), 6);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(EnhancedRandom random, float[] rotation5D) {
        return rotateStep(random, rotation5D, 6);
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D double array), an input vector to rotate
     * (as a 1D double array), and an output vector to write to (as a 1D double array), and does the math to
     * rotate {@code input} using {@code rotation}, and add the results into {@code output}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m} or greater
     * @param output the output vector of length {@code m}
     */
    public static void rotate(double[] input, double[] rotation, double[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; m < rotation.length && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D double array), an input vector to rotate
     * (as a 1D double array), an output vector to write to (as a 1D double array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m}
     * @param output the output vector of length {@code m} or greater; only {@code rotation.length / input.length} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     */
    public static void rotate(double[] input, double[] rotation, double[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / input.length;
        for (int r = 0; r < input.length; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * Multiplies two square matrices with side length {@code side}, and stores the result in {@code out}. The inputs
     * {@code lf} and {@code rt} are 1D double arrays treated as row-major matrices.
     * @param lf the left input matrix, as row-major
     * @param rt the right input matrix, as row-major
     * @param out will be modified; this is where the output is summed into, and it is not cleared beforehand
     * @param side side length of each input matrix and the output matrix
     */
    public static void matrixMultiply(double[] lf, double[] rt, double[] out, int side) {
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

    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. This
     * allocates a new {@code targetSize * targetSize}-element double array on every call and returns it. It also
     * allocates some more temporary double arrays to work with.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D double array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static double[] rotateStep(long seed, double[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        // might be able to get rid of these allocations by holding onto some space...
        double[] gauss = new double[targetSize], house = new double[squareSize], large = new double[squareSize],
                out = new double[squareSize];
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;
        seed = randomize(seed + squareSize);
        double sum = 0, t;
        for (int i = 0; i < targetSize; i++) {
            // use Juniper's Ziggurat algorithm to generate Gaussians directly, which requires randomizing the seed
            gauss[i] = t = Ziggurat.normal(randomize((seed += 0x9E3779B974A7C15L)));
            sum += t * t;
        }
        final double inv = 1 / Math.sqrt(sum);
        sum = 0;
        t = 1;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0;
        }
        sum = MathTools.ROOT2_D / Math.sqrt(sum); // reused as what the subgroup paper calls c
        t = 1;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0;
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
     * Creates a new 1D double array that can be used as a 2D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 4-element double array, meant as effectively a 2D rotation matrix
     */
    public static double[] randomDoubleRotation2D(long seed) {
        final int index = (int)(randomize(seed * 0x9E3779B974A7C15L) >>> 50); // 50 == 64 - TrigTools.SIN_BITS
        final double s = TrigTools.SIN_TABLE_D[index];
        final double c = TrigTools.SIN_TABLE_D[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new double[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D} and Gaussian doubles using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 9-element double array, meant as effectively a 3D rotation matrix
     */
    public static double[] randomDoubleRotation3D(long seed) {
        return rotateStep(seed, randomDoubleRotation2D(seed - 3), 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get Gaussian doubles using
     * {@link Ziggurat}, and uses an existing 2D rotation matrix to avoid redoing
     * any generation work already done for 2D. There will probably be some correlation between the appearance of the 2D
     * rotation this will build upon and the 3D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation3D(long seed, double[] rotation2D) {
        return rotateStep(seed, rotation2D, 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D} and Gaussian doubles using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(long seed) {
        return rotateStep(seed, randomDoubleRotation3D(seed - 4), 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get Gaussian doubles using
     * {@link Ziggurat}, and uses an existing 3D rotation matrix to avoid redoing
     * any generation work already done for 3D. There will probably be some correlation between the appearance of the 3D
     * rotation this will build upon and the 4D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(long seed, double[] rotation3D) {
        return rotateStep(seed, rotation3D, 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D} and Gaussian doubles using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(long seed) {
        return rotateStep(seed, randomDoubleRotation4D(seed - 5), 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get Gaussian doubles using
     * {@link Ziggurat}, and uses an existing 4D rotation matrix to avoid redoing
     * any generation work already done for 4D. There will probably be some correlation between the appearance of the 4D
     * rotation this will build upon and the 5D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(long seed, double[] rotation4D) {
        return rotateStep(seed, rotation4D, 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D} and Gaussian doubles using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(long seed) {
        return rotateStep(seed, randomDoubleRotation5D(seed - 6), 6);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get Gaussian doubles using
     * {@link Ziggurat}, and uses an existing 5D rotation matrix to avoid redoing
     * any generation work already done for 5D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 6D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(long seed, double[] rotation5D) {
        return rotateStep(seed, rotation5D, 6);
    }

    // Section using a Random generator

    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * generator, and uses the seed and some matrix operations to generate a random rotation based on {@code small}.
     * This allocates a new {@code targetSize * targetSize}-element double array on every call and returns it. It also
     * allocates some more temporary double arrays to work with.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param random random number generator; an EnhancedRandom from the juniper library
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D double array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static double[] rotateStep(EnhancedRandom random, double[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        // might be able to get rid of these allocations by holding onto some space...
        double[] gauss = new double[targetSize], house = new double[squareSize], large = new double[squareSize],
                out = new double[squareSize];
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;

        double sum = 0, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = random.nextGaussian();
            sum += t * t;
        }
        final double inv = 1 / Math.sqrt(sum);
        sum = 0;
        t = 1;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0;
        }
        sum = MathTools.ROOT2_D / Math.sqrt(sum); // reused as what the subgroup paper calls c
        t = 1;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0;
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
     * Creates a new 1D double array that can be used as a 2D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 4-element double array, meant as effectively a 2D rotation matrix
     */
    public static double[] randomDoubleRotation2D(EnhancedRandom random) {
        final int index = random.next(TrigTools.SIN_BITS);
        final double s = TrigTools.SIN_TABLE_D[index];
        final double c = TrigTools.SIN_TABLE_D[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new double[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 9-element double array, meant as effectively a 3D rotation matrix
     */
    public static double[] randomDoubleRotation3D(EnhancedRandom random) {
        return rotateStep(random, randomDoubleRotation2D(random), 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation3D(EnhancedRandom random, double[] rotation2D) {
        return rotateStep(random, rotation2D, 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(EnhancedRandom random) {
        return rotateStep(random, randomDoubleRotation3D(random), 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(EnhancedRandom random, double[] rotation3D) {
        return rotateStep(random, rotation3D, 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(EnhancedRandom random) {
        return rotateStep(random, randomDoubleRotation4D(random), 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(EnhancedRandom random, double[] rotation4D) {
        return rotateStep(random, rotation4D, 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(EnhancedRandom random) {
        return rotateStep(random, randomDoubleRotation5D(random), 6);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link EnhancedRandom} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link EnhancedRandom#nextGaussian()}.
     * @param random an EnhancedRandom from juniper
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(EnhancedRandom random, double[] rotation5D) {
        return rotateStep(random, rotation5D, 6);
    }

    /**
     * A wrapper around similar logic to {@link RotationTools}, but with no allocation after construction.
     * Operates on a fixed dimension; create different Rotator instances to rotate different dimensions.
     * You can rotate a float array with length equal to {@link #dimension} with {@link #rotate(float[], float[])}.
     */
    public static class Rotator {
        public final int dimension;
        public EnhancedRandom random;
        // size is dimension
        private final float[] gauss;
        // size of each is dimension*dimension
        private final float[] house;
        private final float[] large;
        private final float[] rotation;

        public Rotator(){
            this(2, null);
        }
        public Rotator(int dimension){
            this(dimension, null);
        }
        public Rotator(int dimension, EnhancedRandom random) {
            this.dimension = Math.max(2, dimension);
            this.random = random == null ? new AceRandom() : random;
            gauss = new float[this.dimension];
            house = new float[this.dimension * this.dimension];
            large = new float[this.dimension * this.dimension];
            rotation = new float[this.dimension * this.dimension];

            randomize();
        }

        public void randomize() {
//            String store = random.stringSerialize();
            final int index = random.next(TrigTools.SIN_BITS);
            rotation[2] = -(rotation[1] = TrigTools.SIN_TABLE[index]);
            rotation[0] = rotation[3] = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];

            for (int targetSize = 3; targetSize <= dimension; targetSize++) {
                final int smallSize = targetSize - 1;

                for (int i = 0; i < smallSize; i++) {
                    // copy the small matrix into the bottom right corner of the large matrix
                    System.arraycopy(rotation, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
                }
                large[0] = 1;
                for (int i = 1, t = targetSize; i < targetSize; i++, t += targetSize) {
                    large[i] = 0;
                    large[t] = 0;
                }
                float sum = 0f, t;
                for (int i = 0; i < targetSize; i++) {
                    gauss[i] = t = (float) random.nextGaussian();
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
                sum = MathTools.ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
                Arrays.fill(rotation, 0);
                matrixMultiply(house, large, rotation, targetSize);
//                if(Float.isNaN(rotation[1])) {
//                    break;
//                }
            }
//            if(Float.isNaN(rotation[1])) {
//                System.out.println("TRASH! " + store);
//                System.exit(0);
//            }
        }

        /**
         * A rotation method that uses this Rotator's rotation matrix and takes an input vector to rotate (as a 1D
         * float array), and an output vector to write to (as a 1D float array), does the math to rotate {@code input}
         * using this Rotator, and adds the results into {@code output}. This does not erase output before writing to
         * it, so it can be called more than once to sum multiple rotations if so desired. The length of output can be
         * arbitrarily large, so this is complete when it has completely processed rotation. That means this affects
         * {@link #dimension} items in output. Almost always, both {@code input} and the writeable part of
         * {@code output} should have a length equal to {@link #dimension}.
         *
         * @param input an input vector of length {@link #dimension}
         * @param output the output vector of length {@link #dimension}
         */
        public void rotate(float[] input, float[] output) {
            int m = 0;
            final int dim2 = rotation.length;
            for (int r = 0; r < dimension; r++) {
                for (int c = 0; m < dim2 && c < output.length; c++) {
                    output[c] += rotation[m++] * input[r];
                }
            }
        }

        /**
         * A rotation method that uses this Rotator's rotation matrix and takes an input vector to rotate (as a 1D
         * float array), and an output vector to write to (as a 1D float array), and an offset into the output vector
         * to start writing there, does the math to rotate {@code input} using this Rotator, and adds the results
         * into {@code output} starting at {@code offsetOut}. This does not erase output before writing to
         * it, so it can be called more than once to sum multiple rotations if so desired. The length of output can be
         * arbitrarily large, so this is complete when it has completely processed rotation. That means this affects
         * {@link #dimension} items in output. Almost always, both {@code input} and the writeable part of
         * {@code output} should have a length equal to {@link #dimension}.
         *
         * @param input an input vector of length {@link #dimension}
         * @param output the output vector of length {@link #dimension}
         * @param offsetOut the index in {@code output} to start writing the rotated output
         */
        public void rotate(float[] input, float[] output, int offsetOut) {
            int m = 0;
            final int outEnd = offsetOut + dimension, dim2 = rotation.length;
            for (int r = 0; r < input.length; r++) {
                for (int c = offsetOut; m < dim2 && c < outEnd && c < output.length; c++) {
                    output[c] += rotation[m++] * input[r];
                }
            }
        }

    }
}
