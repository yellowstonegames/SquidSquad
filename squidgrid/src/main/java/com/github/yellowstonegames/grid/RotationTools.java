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
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.Ziggurat;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Random;

import static com.github.tommyettinger.digital.MathTools.ROOT2;
import static com.github.tommyettinger.digital.MathTools.ROOT2_D;

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
     * @return output, potentially after modification
     */
    public static float[] rotate(float[] input, float[] rotation, float[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; m < rotation.length && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
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
     * @return output, potentially after modification
     */
    public static float[] rotate(float[] input, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / input.length;
        for (int r = 0; r < input.length; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
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
     * @return output, potentially after modification
     */
    public static float[] rotate(float[] input, int offsetIn, int sizeIn, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / sizeIn, inEnd = offsetIn + sizeIn;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), an output vector to write to (as a 1D float array), and offsets/sizes for those arrays to
     * use only part of each one, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code sizeRotation / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, sizeRotation is {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector that has length at least equal to {@code offsetIn + sizeIn}
     * @param offsetIn the index in {@code input} to start reading
     * @param sizeIn how many items to read from {@code input}
     * @param rotation a rotation matrix of length {@code sizeIn * m}, where {@code m} is the length of an output vector
     * @param offsetRotation the first index in {@code rotation} to start reading a rotation matrix from
     * @param sizeRotation how many items to read from {@code rotation} to use as a rotation matrix
     * @param output the output vector of length {@code m} or greater; only {@code sizeRotation / sizeIn} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     * @return output, potentially after modification
     */
    public static float[] rotate(float[] input, int offsetIn, int sizeIn, float[] rotation, int offsetRotation,
                                 int sizeRotation, float[] output, int offsetOut) {
        int m = offsetRotation;
        final int outEnd = offsetOut + sizeRotation / sizeIn, inEnd = offsetIn + sizeIn,
                rotEnd = offsetRotation + sizeRotation;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotEnd && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
    }

    /**
     * A geometric "slerp" (spherical linear interpolation) from the input n-dimensional point {@code start} to the
     * point in the same dimension {@code end}, moving a fraction of the distance equal to {@code alpha}, and placing
     * the result in {@code output} (modifying it in-place). This does not allocate. This has undefined behavior if
     * start and end are polar opposites; that is, points where for any coordinate {@code a} in start, that coordinate
     * in end is {@code -a} or any positive linear scale of the point where that is true. This degenerates to a linear
     * interpolation if either start or end is the origin, and simply returns the start if both are the origin.
     * Otherwise, this can smoothly move points that aren't already on the unit sphere towards the distance of the other
     * point from the origin.
     * <br>
     * Based on the non-approximation code from
     * <a href="https://observablehq.com/@mourner/approximating-geometric-slerp">an article by Volodymyr Agafonkin</a>.
     * Note that this is the "geometric slerp" rather than the version using quaternions in 3D (or rotors in other
     * dimensions). It has been augmented slightly to handle start and end vectors that don't have unit length.
     *
     * @param start an n-dimensional point, where {@code start.length} is n
     * @param end another n-dimensional point, where {@code end.length} is also the same n
     * @param alpha between 0 and 1, inclusive; how much to travel from start towards end
     * @param output the first n items in this will receive the interpolated position, modifying it in-place
     * @return output, after modifications.
     */
    public static float[] slerp(float[] start, float[] end, float alpha, float[] output) {
        return slerp(start.length, start, 0, end, 0, alpha, output, 0);
    }
    
    /**
     * A geometric "slerp" (spherical linear interpolation) from the input n-dimensional point {@code start} to the
     * point in the same dimension {@code end}, moving a fraction of the distance equal to {@code alpha}, and placing
     * the result in {@code output} (modifying it in-place). This does not allocate. This has undefined behavior if
     * start and end are polar opposites; that is, points where for any coordinate {@code a} in start, that coordinate
     * in end is {@code -a} or any positive linear scale of the point where that is true. This degenerates to a linear
     * interpolation if either start or end is the origin, and simply returns the start if both are the origin.
     * Otherwise, this can smoothly move points that aren't already on the unit sphere towards the distance of the other
     * point from the origin.
     * <br>
     * Based on the non-approximation code from
     * <a href="https://observablehq.com/@mourner/approximating-geometric-slerp">an article by Volodymyr Agafonkin</a>.
     * Note that this is the "geometric slerp" rather than the version using quaternions in 3D (or rotors in other
     * dimensions). It has been augmented slightly to handle start and end vectors that don't have unit length.
     *
     * @param n the dimension of the points in start, end, and output; must be 2 or more
     * @param start an n-dimensional point to rotate from
     * @param startOffset what array index to start reading from in {@code start}
     * @param end another n-dimensional point to rotate to
     * @param endOffset what array index to start reading from in {@code start}
     * @param alpha between 0 and 1, inclusive; how much to travel from start towards end
     * @param output will be modified in-place so n items, starting at outputOffset, have the result
     * @param outputOffset what array index to start writing to in {@code output}
     * @return output, after modifications.
     */
    public static float[] slerp(int n, float[] start, int startOffset, float[] end, int endOffset,
                                float alpha, float[] output, int outputOffset) {
        n = Math.max(2, n);
        int startEnd = startOffset + n;
        if(startEnd > start.length) throw new IllegalArgumentException("start is not large enough; must have "
                + startEnd + " items.");
        int endEnd = endOffset + n;
        if(endEnd > end.length) throw new IllegalArgumentException("end is not large enough; must have "
                + endEnd + " items.");
        float magS = 0f, magE = 0f;
        for (int i = startOffset, j = endOffset; i < startEnd; i++, j++) {
            magS += start[i] * start[i];
            magE += end[j] * end[j];
        }
        // if both start and end are the origin
        if(MathTools.isZero(magS + magE)) {
            System.arraycopy(start, startOffset, output, outputOffset, n);
        }
        // if only the start is the origin
        else if(MathTools.isZero(magS)){
            for (int i = endOffset, j = outputOffset; i < endEnd; i++, j++) {
                output[j] = end[i] * alpha;
            }
        }
        // if only the end is the origin
        else if(MathTools.isZero(magE)){
            for (int i = startOffset, j = outputOffset; i < startEnd; i++, j++) {
                output[j] = start[i] * (1f - alpha);
            }
        }
        else {
            magS = (float) Math.sqrt(magS);
            magE = (float) Math.sqrt(magE);

            float k = 0, invDistance = 1f / (magS * (1f - alpha) + magE * alpha);
            for (int i = startOffset, j = endOffset; i < startEnd; i++, j++) {
                k += (start[i] / magS) * (end[j] / magE);
            }
            k = TrigTools.acos(k);
            float s = TrigTools.sin(k * (1f - alpha));
            float e = TrigTools.sin(k * alpha);

            for (int i = startOffset, j = endOffset, h = outputOffset; i < startEnd; i++, j++, h++) {
                output[h] = (start[i] * s + end[j] * e) * invDistance;
            }
        }
        return output;
    }

    /**
     * Multiplies two square matrices with side length {@code side}, and stores the result in {@code out}. The inputs
     * {@code lf} and {@code rt} are 1D float arrays treated as row-major matrices.
     * @param lf the left input matrix, as row-major
     * @param rt the right input matrix, as row-major
     * @param out will be modified; this is where the output is summed into, and it is not cleared beforehand
     * @param side side length of each input matrix and the output matrix
     * @return out, potentially after modification
     */
    public static float[] matrixMultiply(float[] lf, float[] rt, float[] out, int side) {
        int o = 0;
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                for (int i = 0; i < side; i++) {
                    out[o] += lf[r * side + i] * rt[side * i + c];
                }
                ++o;
            }
        }
        return out;
    }

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
        sum = ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. To avoid
     * allocating arrays on each call to this, this method also takes four float arrays that this will clear and modify,
     * to be used as temporary workspace. As long as the last four arguments have enough length, their contents don't
     * matter. While {@code gauss} must have length of at least {@code targetSize}, the last three must have length of
     * at least {@code targetSize * targetSize}.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed; may be a long or an int
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}, and will not be modified
     * @param targetSize the side length of the square matrix to be returned
     * @param gauss a temporary float array that will be cleared; must have length of at least {@code targetSize}
     * @param house a temporary float array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param large a temporary float array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param out the float array that will be cleared and returned; must have length of at least {@code targetSize * targetSize}
     * @return {@code out}, which can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(long seed, final float[] small, int targetSize, float[] gauss, float[] house,
                                     float[] large, float[] out) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
            large[i + 1] = 0f;
            large[i * targetSize + targetSize] = 0f;
        }
        large[0] = 1f;
        long sd = randomize(seed + squareSize);
        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = (float) Ziggurat.normal(randomize((sd += 0x9E3779B97F4A7C15L)));
            sum += t * t;
        }
        final float inv = (float) (1.0 / Math.sqrt(sum));
        sum = 0f;
        t = 1f;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0f;
        }
        sum = (float) (ROOT2 / Math.sqrt(sum)); // reused as what the subgroup paper calls c
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
        Arrays.fill(out, 0, squareSize, 0f);
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
        final float c = TrigTools.COS_TABLE[index];
        return new float[]{c, s, -s, c};
    }

    /**
     * Fills {@code out} with a 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Scrambles the given seed with {@link #randomize(long)},
     * then gets an angle using {@link TrigTools#SIN_TABLE} and {@link TrigTools#COS_TABLE}.
     * @param seed any long; will be scrambled
     * @param out a float array that must have at least 4 elements; will be cleared and returned
     * @return {@code out}, meant as effectively a 2D rotation matrix
     */
    public static float[] fillRandomRotation2D(long seed, float[] out) {
        final int index = (int)(randomize(seed * 0x9E3779B97F4A7C15L) >>> 50); // 50 == 64 - TrigTools.SIN_BITS
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.COS_TABLE[index];
        out[0] = out[3] = c;
        out[2] = -(out[1] = s);
        return out;
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
    
    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(long seed) {
        return rotateStep(seed, randomRotation6D(seed - 7), 7);
    }

    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 6D rotation matrix to avoid redoing
     * any generation work already done for 6D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 7D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @param rotation6D an existing 6D rotation matrix stored in a 1D float array; often produced by {@link #randomRotation6D(long)}
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(long seed, float[] rotation6D) {
        return rotateStep(seed, rotation6D, 7);
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this only
     * allocates the array it returns.
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(float[], float[], float[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param seed any long; will be scrambled
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @return a newly-allocated {@code dimension * dimension}-element float array, meant as effectively a
     * {@code dimension}-D rotation matrix
     */
    public static float[] randomRotation(long seed, int dimension) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        final float[] base = fillRandomRotation2D(seed, new float[dimensionSq]);
        if(dimension > 2) {
            final float[] gauss = new float[dimension], house = new float[dimensionSq],
                    large = new float[dimensionSq], temp = new float[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(seed += d, base, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, base, 0, d * d);
            }
        }
        return base;
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this doesn't
     * allocate at all if {@code out} has at least length 4 (so it can store the resulting matrix).
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(float[], float[], float[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param seed any long; will be scrambled
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @param out a float array that should have at least {@code dimension * dimension} elements; will be modified
     * @return {@code out}, after modifications, unless it was too small or null (then this returns a new array)
     */
    public static float[] fillRandomRotation(long seed, int dimension, float[] out) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        if(out == null || out.length < dimensionSq) out = new float[dimensionSq];
        fillRandomRotation2D(seed, out);
        if(dimension > 2) {
            final float[] gauss = new float[dimension], house = new float[dimensionSq],
                    large = new float[dimensionSq], temp = new float[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(seed += d, out, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, out, 0, d * d);
            }
        }
        return out;
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
     * @param random random number generator; any {@link Random} from the JDK or from the juniper library works
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D float array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(Random random, float[] small, int targetSize) {
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
        sum = ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. To avoid
     * allocating arrays on each call to this, this method also takes four float arrays that this will clear and modify,
     * to be used as temporary workspace. As long as the last four arguments have enough length, their contents don't
     * matter. While {@code gauss} must have length of at least {@code targetSize}, the last three must have length of
     * at least {@code targetSize * targetSize}.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param random random number generator; any {@link Random} from the JDK or from the juniper library works
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}, and will not be modified
     * @param targetSize the side length of the square matrix to be returned
     * @param gauss a temporary float array that will be cleared; must have length of at least {@code targetSize}
     * @param house a temporary float array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param large a temporary float array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param out the float array that will be cleared and returned; must have length of at least {@code targetSize * targetSize}
     * @return {@code out}, which can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(Random random, final float[] small, int targetSize, float[] gauss, float[] house,
                                     float[] large, float[] out) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
            large[i + 1] = 0f;
            large[i * targetSize + targetSize] = 0f;
        }
        large[0] = 1f;
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
        sum = ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
        Arrays.fill(out, 0, squareSize, 0f);
        matrixMultiply(house, large, out, targetSize);
        return out;
    }

    /**
     * Creates a new 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(Random random) {
        final int index = random.nextInt() & TrigTools.TABLE_MASK;
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.COS_TABLE[index];
        return new float[]{c, s, -s, c};
    }

    /**
     * Fills {@code out} with a 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Scrambles the given seed with {@link #randomize(long)},
     * then gets an angle using {@link TrigTools#SIN_TABLE_D} and {@link TrigTools#COS_TABLE_D}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param out a float array that must have at least 4 elements; will be cleared and returned
     * @return {@code out}, meant as effectively a 2D rotation matrix
     */
    public static float[] fillRandomRotation2D(Random random, float[] out) {
        final int index = random.nextInt() & TrigTools.TABLE_MASK; // 50 == 64 - TrigTools.SIN_BITS
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.COS_TABLE[index];
        out[0] = out[3] = c;
        out[2] = -(out[1] = s);
        return out;
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(Random random) {
        return rotateStep(random, randomRotation2D(random), 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation3D(Random random, float[] rotation2D) {
        return rotateStep(random, rotation2D, 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(Random random) {
        return rotateStep(random, randomRotation3D(random), 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(Random random, float[] rotation3D) {
        return rotateStep(random, rotation3D, 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(Random random) {
        return rotateStep(random, randomRotation4D(random), 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(Random random, float[] rotation4D) {
        return rotateStep(random, rotation4D, 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(Random random) {
        return rotateStep(random, randomRotation5D(random), 6);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(Random random, float[] rotation5D) {
        return rotateStep(random, rotation5D, 6);
    }
    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Ziggurat}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(Random random) {
        return rotateStep(random, randomRotation6D(random), 7);
    }

    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given Random random to get Gaussian floats using
     * {@link Ziggurat}, and uses an existing 6D rotation matrix to avoid redoing
     * any generation work already done for 6D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 7D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param rotation6D an existing 6D rotation matrix stored in a 1D float array; often produced by {@link #randomRotation6D(long)}
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(Random random, float[] rotation6D) {
        return rotateStep(random, rotation6D, 7);
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this only
     * allocates the array it returns.
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(float[], float[], float[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @return a newly-allocated {@code dimension * dimension}-element float array, meant as effectively a
     * {@code dimension}-D rotation matrix
     */
    public static float[] randomRotation(Random random, int dimension) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        final float[] base = fillRandomRotation2D(random, new float[dimensionSq]);
        if(dimension > 2) {
            final float[] gauss = new float[dimension], house = new float[dimensionSq],
                    large = new float[dimensionSq], temp = new float[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(random, base, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, base, 0, d * d);
            }
        }
        return base;
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this doesn't
     * allocate at all if {@code out} has at least length 4 (so it can store the resulting matrix).
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(float[], float[], float[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @param out a float array that should have at least {@code dimension * dimension} elements; will be modified
     * @return {@code out}, after modifications, unless it was too small or null (then this returns a new array)
     */
    public static float[] fillRandomRotation(Random random, int dimension, float[] out) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        if(out == null || out.length < dimensionSq) out = new float[dimensionSq];
        fillRandomRotation2D(random, out);
        if(dimension > 2) {
            final float[] gauss = new float[dimension], house = new float[dimensionSq],
                    large = new float[dimensionSq], temp = new float[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(random, out, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, out, 0, d * d);
            }
        }
        return out;
    }

    // double code

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
    public static double[] rotate(double[] input, double[] rotation, double[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; m < rotation.length && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
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
    public static double[] rotate(double[] input, double[] rotation, double[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / input.length;
        for (int r = 0; r < input.length; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
    }
    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D double array), an input vector to rotate
     * (as a 1D double array), an output vector to write to (as a 1D double array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. If input has length {@code n} and
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
    public static double[] rotate(double[] input, int offsetIn, int sizeIn, double[] rotation, double[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / sizeIn, inEnd = offsetIn + sizeIn;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D double array), an input vector to rotate
     * (as a 1D double array), an output vector to write to (as a 1D double array), and offsets/sizes for those arrays to
     * use only part of each one, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code sizeRotation / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, sizeRotation is {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector that has length at least equal to {@code offsetIn + sizeIn}
     * @param offsetIn the index in {@code input} to start reading
     * @param sizeIn how many items to read from {@code input}
     * @param rotation a rotation matrix of length {@code sizeIn * m}, where {@code m} is the length of an output vector
     * @param offsetRotation the first index in {@code rotation} to start reading a rotation matrix from
     * @param sizeRotation how many items to read from {@code rotation} to use as a rotation matrix
     * @param output the output vector of length {@code m} or greater; only {@code sizeRotation / sizeIn} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     * @return output, potentially after modification
     */
    public static double[] rotate(double[] input, int offsetIn, int sizeIn, double[] rotation, int offsetRotation,
                                 int sizeRotation, double[] output, int offsetOut) {
        int m = offsetRotation;
        final int outEnd = offsetOut + sizeRotation / sizeIn, inEnd = offsetIn + sizeIn,
                rotEnd = offsetRotation + sizeRotation;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotEnd && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
        return output;
    }

    /**
     * A geometric "slerp" (spherical linear interpolation) from the input n-dimensional point {@code start} to the
     * point in the same dimension {@code end}, moving a fraction of the distance equal to {@code alpha}, and placing
     * the result in {@code output} (modifying it in-place). This does not allocate. This has undefined behavior if
     * start and end are polar opposites; that is, points where for any coordinate {@code a} in start, that coordinate
     * in end is {@code -a} or any positive linear scale of the point where that is true. This degenerates to a linear
     * interpolation if either start or end is the origin, and simply returns the start if both are the origin.
     * Otherwise, this can smoothly move points that aren't already on the unit sphere towards the distance of the other
     * point from the origin.
     * <br>
     * Unlike the {@code float} version of this method, this calls {@link Math#acos(double)} and
     * {@link Math#sin(double)} for higher precision. This is expected to be somewhat slower than using the
     * approximations from {@link TrigTools}.
     * <br>
     * Based on the non-approximation code from
     * <a href="https://observablehq.com/@mourner/approximating-geometric-slerp">an article by Volodymyr Agafonkin</a>.
     * Note that this is the "geometric slerp" rather than the version using quaternions in 3D (or rotors in other
     * dimensions). It has been augmented slightly to handle start and end vectors that don't have unit length.
     *
     * @param start an n-dimensional point, where {@code start.length} is n
     * @param end another n-dimensional point, where {@code end.length} is also the same n
     * @param alpha between 0 and 1, inclusive; how much to travel from start towards end
     * @param output the first n items in this will receive the interpolated position, modifying it in-place
     * @return output, after modifications.
     */
    public static double[] slerp(double[] start, double[] end, double alpha, double[] output) {
        return slerp(start.length, start, 0, end, 0, alpha, output, 0);
    }

    /**
     * A geometric "slerp" (spherical linear interpolation) from the input n-dimensional point {@code start} to the
     * point in the same dimension {@code end}, moving a fraction of the distance equal to {@code alpha}, and placing
     * the result in {@code output} (modifying it in-place). This does not allocate. This has undefined behavior if
     * start and end are polar opposites; that is, points where for any coordinate {@code a} in start, that coordinate
     * in end is {@code -a} or any positive linear scale of the point where that is true. This degenerates to a linear
     * interpolation if either start or end is the origin, and simply returns the start if both are the origin.
     * Otherwise, this can smoothly move points that aren't already on the unit sphere towards the distance of the other
     * point from the origin.
     * <br>
     * Unlike the {@code float} version of this method, this calls {@link Math#acos(double)} and
     * {@link Math#sin(double)} for higher precision. This is expected to be somewhat slower than using the
     * approximations from {@link TrigTools}.
     * <br>
     * Based on the non-approximation code from
     * <a href="https://observablehq.com/@mourner/approximating-geometric-slerp">an article by Volodymyr Agafonkin</a>.
     * Note that this is the "geometric slerp" rather than the version using quaternions in 3D (or rotors in other
     * dimensions). It has been augmented slightly to handle start and end vectors that don't have unit length.
     *
     * @param n the dimension of the points in start, end, and output; must be 2 or more
     * @param start an n-dimensional point to rotate from
     * @param startOffset what array index to start reading from in {@code start}
     * @param end another n-dimensional point to rotate to
     * @param endOffset what array index to start reading from in {@code start}
     * @param alpha between 0 and 1, inclusive; how much to travel from start towards end
     * @param output will be modified in-place so n items, starting at outputOffset, have the result
     * @param outputOffset what array index to start writing to in {@code output}
     * @return output, after modifications.
     */
    public static double[] slerp(int n, double[] start, int startOffset, double[] end, int endOffset,
                                double alpha, double[] output, int outputOffset) {
        n = Math.max(2, n);
        int startEnd = startOffset + n;
        if(startEnd > start.length) throw new IllegalArgumentException("start is not large enough; must have "
                + startEnd + " items.");
        int endEnd = endOffset + n;
        if(endEnd > end.length) throw new IllegalArgumentException("end is not large enough; must have "
                + endEnd + " items.");
        double magS = 0f, magE = 0f;
        for (int i = startOffset, j = endOffset; i < startEnd; i++, j++) {
            magS += start[i] * start[i];
            magE += end[j] * end[j];
        }
        // if both start and end are the origin
        if(MathTools.isZero(magS + magE, 1E-9)) {
            System.arraycopy(start, startOffset, output, outputOffset, n);
        }
        // if only the start is the origin
        else if(MathTools.isZero(magS, 1E-9)){
            for (int i = endOffset, j = outputOffset; i < endEnd; i++, j++) {
                output[j] = end[i] * alpha;
            }
        }
        // if only the end is the origin
        else if(MathTools.isZero(magE, 1E-9)){
            for (int i = startOffset, j = outputOffset; i < startEnd; i++, j++) {
                output[j] = start[i] * (1f - alpha);
            }
        }
        else {
            magS = Math.sqrt(magS);
            magE = Math.sqrt(magE);

            double k = 0, invDistance = 1f / (magS * (1f - alpha) + magE * alpha);
            for (int i = startOffset, j = endOffset; i < startEnd; i++, j++) {
                k += (start[i] / magS) * (end[j] / magE);
            }
            k = Math.acos(k);
            double s = Math.sin(k * (1f - alpha));
            double e = Math.sin(k * alpha);

            for (int i = startOffset, j = endOffset, h = outputOffset; i < startEnd; i++, j++, h++) {
                output[h] = (start[i] * s + end[j] * e) * invDistance;
            }
        }
        return output;
    }

    /**
     * Multiplies two square matrices with side length {@code side}, and stores the result in {@code out}. The inputs
     * {@code lf} and {@code rt} are 1D double arrays treated as row-major matrices.
     * @param lf the left input matrix, as row-major
     * @param rt the right input matrix, as row-major
     * @param out will be modified; this is where the output is summed into, and it is not cleared beforehand
     * @param side side length of each input matrix and the output matrix
     */
    public static double[] matrixMultiply(double[] lf, double[] rt, double[] out, int side) {
        int o = 0;
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                for (int i = 0; i < side; i++) {
                    out[o] += lf[r * side + i] * rt[side * i + c];
                }
                ++o;
            }
        }
        return out;
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
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. To avoid
     * allocating arrays on each call to this, this method also takes four double arrays that this will clear and modify,
     * to be used as temporary workspace. As long as the last four arguments have enough length, their contents don't
     * matter. While {@code gauss} must have length of at least {@code targetSize}, the last three must have length of
     * at least {@code targetSize * targetSize}.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed; may be a long or an int
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}, and will not be modified
     * @param targetSize the side length of the square matrix to be returned
     * @param gauss a temporary double array that will be cleared; must have length of at least {@code targetSize}
     * @param house a temporary double array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param large a temporary double array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param out the double array that will be cleared and returned; must have length of at least {@code targetSize * targetSize}
     * @return {@code out}, which can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static double[] rotateStep(long seed, final double[] small, int targetSize, double[] gauss, double[] house,
                                     double[] large, double[] out) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
            large[i + 1] = 0.0;
            large[i * targetSize + targetSize] = 0.0;
        }
        large[0] = 1.0;
        long sd = randomize(seed + squareSize);
        double sum = 0.0, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = Ziggurat.normal(randomize((sd += 0x9E3779B97F4A7C15L)));
            sum += t * t;
        }
        final double inv = 1.0 / Math.sqrt(sum);
        sum = 0.0;
        t = 1.0;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0.0;
        }
        sum = ROOT2_D / Math.sqrt(sum); // reused as what the subgroup paper calls c
        t = 1.0;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0.0;
        }
        for (int row = 0, h = 0; row < targetSize; row++) {
            for (int col = 0; col < targetSize; col++, h++) {
                house[h] = gauss[row] * gauss[col];
            }
        }
        for (int i = 0; i < targetSize; i++) {
            house[targetSize * i + i]--;
        }
        Arrays.fill(out, 0, squareSize, 0.0);
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
        final double c = TrigTools.COS_TABLE_D[index];
        return new double[]{c, s, -s, c};
    }

    /**
     * Fills {@code out} with a 1D double array that can be used as a 2D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Scrambles the given seed with {@link #randomize(long)},
     * then gets an angle using {@link TrigTools#SIN_TABLE_D} and {@link TrigTools#COS_TABLE_D}.
     * @param seed any long; will be scrambled
     * @param out a double array that must have at least 4 elements; will be cleared and returned
     * @return {@code out}, meant as effectively a 2D rotation matrix
     */
    public static double[] fillRandomDoubleRotation2D(long seed, double[] out) {
        final int index = (int)(randomize(seed * 0x9E3779B97F4A7C15L) >>> 50); // 50 == 64 - TrigTools.SIN_BITS
        final double s = TrigTools.SIN_TABLE_D[index];
        final double c = TrigTools.COS_TABLE_D[index];
        out[0] = out[3] = c;
        out[2] = -(out[1] = s);
        return out;
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

    /**
     * Creates a new 1D double array that can be used as a 7D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE_D} and Gaussian doubles using {@link Ziggurat}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 49-element double array, meant as effectively a 7D rotation matrix
     */
    public static double[] randomDoubleRotation7D(long seed) {
        return rotateStep(seed, randomDoubleRotation6D(seed - 7), 7);
    }

    /**
     * Creates a new 1D double array that can be used as a 7D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given long seed to get Gaussian doubles using
     * {@link Ziggurat}, and uses an existing 6D rotation matrix to avoid redoing
     * any generation work already done for 6D. There will probably be some correlation between the appearance of the 6D
     * rotation this will build upon and the 7D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 49-element double array, meant as effectively a 7D rotation matrix
     */
    public static double[] randomDoubleRotation7D(long seed, double[] rotation6D) {
        return rotateStep(seed, rotation6D, 7);
    }
    
    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomDoubleRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this only
     * allocates the array it returns.
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(double[], double[], double[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param seed any long; will be scrambled
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @return a newly-allocated {@code dimension * dimension}-element double array, meant as effectively a
     * {@code dimension}-D rotation matrix
     */
    public static double[] randomDoubleRotation(long seed, int dimension) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        final double[] base = fillRandomDoubleRotation2D(seed, new double[dimensionSq]);
        if(dimension > 2) {
            final double[] gauss = new double[dimension], house = new double[dimensionSq],
                    large = new double[dimensionSq], temp = new double[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(seed += d, base, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, base, 0, d * d);
            }
        }
        return base;
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this doesn't
     * allocate at all if {@code out} has at least length 4 (so it can store the resulting matrix).
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(double[], double[], double[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param seed any long; will be scrambled
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @param out a float array that should have at least {@code dimension * dimension} elements; will be modified
     * @return {@code out}, after modifications, unless it was too small or null (then this returns a new array)
     */
    public static double[] fillRandomDoubleRotation(long seed, int dimension, double[] out) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        if(out == null || out.length < dimensionSq) out = new double[dimensionSq];
        fillRandomDoubleRotation2D(seed, out);
        if(dimension > 2) {
            final double[] gauss = new double[dimension], house = new double[dimensionSq],
                    large = new double[dimensionSq], temp = new double[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(seed += d, out, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, out, 0, d * d);
            }
        }
        return out;
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
     * @param random random number generator; any {@link Random} from the JDK or from the juniper library works
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D double array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static double[] rotateStep(Random random, double[] small, int targetSize) {
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
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. To avoid
     * allocating arrays on each call to this, this method also takes four double arrays that this will clear and modify,
     * to be used as temporary workspace. As long as the last four arguments have enough length, their contents don't
     * matter. While {@code gauss} must have length of at least {@code targetSize}, the last three must have length of
     * at least {@code targetSize * targetSize}.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param random random number generator; any {@link Random} from the JDK or from the juniper library works
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}, and will not be modified
     * @param targetSize the side length of the square matrix to be returned
     * @param gauss a temporary double array that will be cleared; must have length of at least {@code targetSize}
     * @param house a temporary double array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param large a temporary double array that will be cleared; must have length of at least {@code targetSize * targetSize}
     * @param out the double array that will be cleared and returned; must have length of at least {@code targetSize * targetSize}
     * @return {@code out}, which can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static double[] rotateStep(Random random, final double[] small, int targetSize, double[] gauss, double[] house,
                                     double[] large, double[] out) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
            large[i + 1] = 0.0;
            large[i * targetSize + targetSize] = 0.0;
        }
        large[0] = 1.0;
        double sum = 0.0, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = random.nextGaussian();
            sum += t * t;
        }
        final double inv = 1.0 / Math.sqrt(sum);
        sum = 0.0;
        t = 1.0;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0.0;
        }
        sum = ROOT2_D / Math.sqrt(sum); // reused as what the subgroup paper calls c
        t = 1.0;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0.0;
        }
        for (int row = 0, h = 0; row < targetSize; row++) {
            for (int col = 0; col < targetSize; col++, h++) {
                house[h] = gauss[row] * gauss[col];
            }
        }
        for (int i = 0; i < targetSize; i++) {
            house[targetSize * i + i]--;
        }
        Arrays.fill(out, 0, squareSize, 0.0);
        matrixMultiply(house, large, out, targetSize);
        return out;
    }

    /**
     * Creates a new 1D double array that can be used as a 2D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 4-element double array, meant as effectively a 2D rotation matrix
     */
    public static double[] randomDoubleRotation2D(Random random) {
        final int index = random.nextInt() & TrigTools.TABLE_MASK;
        final double s = TrigTools.SIN_TABLE_D[index];
        final double c = TrigTools.COS_TABLE_D[index];
        return new double[]{c, s, -s, c};
    }

    /**
     * Fills {@code out} with a 1D double array that can be used as a 2D rotation matrix by
     * {@link #rotate(double[], double[], double[])}. Scrambles the given seed with {@link #randomize(long)},
     * then gets an angle using {@link TrigTools#SIN_TABLE_D} and {@link TrigTools#COS_TABLE_D}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param out a double array that must have at least 4 elements; will be cleared and returned
     * @return {@code out}, meant as effectively a 2D rotation matrix
     */
    public static double[] fillRandomDoubleRotation2D(Random random, double[] out) {
        final int index = random.nextInt() & TrigTools.TABLE_MASK; // 50 == 64 - TrigTools.SIN_BITS
        final double s = TrigTools.COS_TABLE_D[index];
        final double c = TrigTools.COS_TABLE_D[index];
        out[0] = out[3] = c;
        out[2] = -(out[1] = s);
        return out;
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 9-element double array, meant as effectively a 3D rotation matrix
     */
    public static double[] randomDoubleRotation3D(Random random) {
        return rotateStep(random, randomDoubleRotation2D(random), 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 3D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation3D(Random random, double[] rotation2D) {
        return rotateStep(random, rotation2D, 3);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(Random random) {
        return rotateStep(random, randomDoubleRotation3D(random), 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 4D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 16-element double array, meant as effectively a 4D rotation matrix
     */
    public static double[] randomDoubleRotation4D(Random random, double[] rotation3D) {
        return rotateStep(random, rotation3D, 4);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(Random random) {
        return rotateStep(random, randomDoubleRotation4D(random), 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 5D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 25-element double array, meant as effectively a 5D rotation matrix
     */
    public static double[] randomDoubleRotation5D(Random random, double[] rotation4D) {
        return rotateStep(random, rotation4D, 5);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(Random random) {
        return rotateStep(random, randomDoubleRotation5D(random), 6);
    }

    /**
     * Creates a new 1D double array that can be used as a 6D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 36-element double array, meant as effectively a 6D rotation matrix
     */
    public static double[] randomDoubleRotation6D(Random random, double[] rotation5D) {
        return rotateStep(random, rotation5D, 6);
    }

    /**
     * Creates a new 1D double array that can be used as a 7D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 49-element double array, meant as effectively a 7D rotation matrix
     */
    public static double[] randomDoubleRotation7D(Random random) {
        return rotateStep(random, randomDoubleRotation6D(random), 7);
    }

    /**
     * Creates a new 1D double array that can be used as a 7D rotation matrix by
     * {@link #rotate(double[], double[], double[])}.
     * Uses the given {@link Random} to get an angle using
     * {@link TrigTools#SIN_TABLE_D}, and also calls {@link Random#nextGaussian()}.
     * @param random any {@link Random} from the JDK or from the juniper library
     * @return a newly-allocated 49-element double array, meant as effectively a 7D rotation matrix
     */
    public static double[] randomDoubleRotation7D(Random random, double[] rotation6D) {
        return rotateStep(random, rotation6D, 7);
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomDoubleRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this only
     * allocates the array it returns.
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(double[], double[], double[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @return a newly-allocated {@code dimension * dimension}-element double array, meant as effectively a
     * {@code dimension}-D rotation matrix
     */
    public static double[] randomDoubleRotation(Random random, int dimension) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        final double[] base = fillRandomDoubleRotation2D(random, new double[dimensionSq]);
        if(dimension > 2) {
            final double[] gauss = new double[dimension], house = new double[dimensionSq],
                    large = new double[dimensionSq], temp = new double[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(random, base, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, base, 0, d * d);
            }
        }
        return base;
    }

    /**
     * Iteratively calculates a rotation matrix for the given {@code dimension}, randomly generating it with the given
     * {@code seed}. For dimensions 3 and higher, this allocates some temporary arrays once per call, but unlike methods
     * such as {@link #randomRotation4D(long)}, this doesn't allocate per dimension. For dimension 2, this doesn't
     * allocate at all if {@code out} has at least length 4 (so it can store the resulting matrix).
     * <br>
     * If allocation is a concern because you are making many random rotations, you may want to consider creating a
     * {@link Rotator} and using it to rotate vectors instead of using {@link #rotate(double[], double[], double[])}
     * directly. Rotator allocates its memory upon construction and doesn't allocate after that.
     *
     * @param random any {@link Random} from the JDK or from the juniper library
     * @param dimension will be clamped to at minimum 2, but there is technically no maximum
     * @param out a float array that should have at least {@code dimension * dimension} elements; will be modified
     * @return {@code out}, after modifications, unless it was too small or null (then this returns a new array)
     */
    public static double[] fillRandomDoubleRotation(Random random, int dimension, double[] out) {
        dimension = Math.max(2, dimension);
        final int dimensionSq = dimension * dimension;
        if(out == null || out.length < dimensionSq) out = new double[dimensionSq];
        fillRandomDoubleRotation2D(random, out);
        if(dimension > 2) {
            final double[] gauss = new double[dimension], house = new double[dimensionSq],
                    large = new double[dimensionSq], temp = new double[dimensionSq];
            for (int d = 3; d <= dimension; d++) {
                rotateStep(random, out, d, gauss, house, large, temp);
                System.arraycopy(temp, 0, out, 0, d * d);
            }
        }
        return out;
    }

    /**
     * A wrapper around similar logic to {@link RotationTools}, but with no allocation after construction.
     * Operates on a fixed dimension; create different Rotator instances to rotate different dimensions.
     * You can rotate a float array with length equal to {@link #dimension} with {@link #rotate(float[], float[])}.
     */
    public static class Rotator {
        public final int dimension;
        public @NonNull Random random;
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
        public Rotator(int dimension, Random random) {
            this.dimension = Math.max(2, dimension);
            final int dimSq = dimension * dimension;
            this.random = random == null ? new AceRandom() : random;
            gauss = new float[this.dimension];
            house = new float[dimSq];
            large = new float[dimSq];
            rotation = new float[dimSq];

            randomize();
        }

        public void randomize() {
            final int index = random.nextInt() & TrigTools.TABLE_MASK;
            rotation[2] = -(rotation[1] = TrigTools.SIN_TABLE[index]);
            rotation[0] = rotation[3] = TrigTools.COS_TABLE[index];

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
                sum = ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
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
            }
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
            rotate(input, 0, output, offsetOut);
        }
        /**
         * A rotation method that uses this Rotator's rotation matrix and takes an input vector to rotate (as a 1D
         * float array), an output vector to write to (as a 1D float array), and offsets into the input and output
         * vectors to start reading from and writing to, then does the math to rotate {@code input} using this Rotator,
         * and adds the results into {@code output} starting at {@code offsetOut}. This does not erase output before
         * writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
         * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means
         * this affects {@link #dimension} items in output, and likewise reads the same amount from input. Both
         * the part of {@code input} after {@code offsetIn} and the writeable part of {@code output} should have a
         * length at least equal to {@link #dimension}.
         *
         * @param input an input vector of length {@link #dimension} or greater
         * @param offsetIn the index in {@code input} to read {@link #dimension} items from
         * @param output the output vector of length {@link #dimension} or greater
         * @param offsetOut the index in {@code output} to start writing the rotated output
         */
        public void rotate(float[] input, int offsetIn, float[] output, int offsetOut) {
            int m = 0;
            final int outEnd = offsetOut + dimension, dim2 = rotation.length;
            for (int r = 0; r < dimension; r++) {
                for (int c = offsetOut; m < dim2 && c < outEnd && c < output.length; c++) {
                    output[c] += rotation[m++] * input[offsetIn + r];
                }
            }
        }
    }
}
