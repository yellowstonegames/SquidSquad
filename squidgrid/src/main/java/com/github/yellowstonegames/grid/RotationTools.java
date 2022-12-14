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

import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.Ziggurat;

/**
 * This has tools for matrix rotations, potentially in higher dimensions than the typical 2 or 3.
 * <br>
 * My head hurts. Thanks to spenc on the libGDX Discord for carefully guiding me through this code.
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
        for (int c = 0, m = 0; c < input.length; c++) {
            for (int r = 0; r < output.length; r++) {
                output[r] += rotation[m++] * input[c];
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
        for (int r = 0, o = 0; r < side; r++) {
            for (int c = 0; c < side; c++, o++) {
                for (int i = 0; i < side; i++) {
                    out[o] += lf[r * side + i] * rt[c + side * i];
                }
            }
        }
    }

    /**
     * TODO: Does this work? This is really complicated.
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     * @param seed
     * @param small
     * @param targetSize
     * @return
     */
    private static float[] rotateStep(long seed, float[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        float[] gauss = new float[targetSize], house = new float[squareSize], large = new float[squareSize],
                out = new float[squareSize];
        for (int i = 0; i < smallSize; i++) {
            System.arraycopy(small, i * smallSize, large, i * targetSize, smallSize);
        }
        large[squareSize - 1] = 1;
        seed = Hasher.randomize2(seed + squareSize);
        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = (float) Ziggurat.normal(Hasher.randomize2(++seed));
            sum += t * t;
        }
        final float inv = MathTools.ROOT2 / (float) Math.sqrt(sum);
        for (int i = 0; i < targetSize; i++) {
            gauss[i] *= inv;
        }
        for (int row = 0, h = 0; row < targetSize; row++) {
            for (int col = 0; col < targetSize; col++, h++) {
                house[h] = gauss[row] * gauss[col];
            }
        }
        for (int i = 0; i < targetSize; i++) {
            house[i + targetSize * i]--;
        }
        matrixMultiply(house, large, out, targetSize);
        return out;
    }

    /**
     * Creates a new 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given seed to get an angle using
     * {@link Hasher#randomize2(long)} and {@link TrigTools#SIN_TABLE}.
     * @param seed any long; will be scrambled with {@link Hasher#randomize2(long)}
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(long seed) {
        final int index = (int)(Hasher.randomize2(seed) >>> 50);
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new float[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and three Gaussian floats using {@link Hasher#randomize2(long)} and {@link Ziggurat}.
     * @param seed any long; will be scrambled with {@link Hasher#randomize2(long)}
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(long seed) {
        final int index = (int)((seed = Hasher.randomize2(seed)) >>> 50);
        float x = (float) Ziggurat.normal(Hasher.randomize2(++seed));
        float y = (float) Ziggurat.normal(Hasher.randomize2(++seed));
        float z = (float) Ziggurat.normal(Hasher.randomize2(++seed));
        final float inv = MathTools.ROOT2 / (float) Math.sqrt(x * x + y * y + z * z);
        x *= inv;
        y *= inv;
        z *= inv;
        final float xx = x * x - 1;
        final float yy = y * y - 1;
        final float zz = z * z - 1;
        final float xy = x * y;
        final float xz = x * z;
        final float yz = y * z;

        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        final float sxy = s * xy, cxy = c * xy;
        return new float[]{
                c * xx - sxy   , s * xx + cxy   , xz,
                cxy    - s * yy, sxy    + c * yy, yz,
                c * xz - s * yz, s * xz + c * yz, zz};
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link TrigTools#SIN_TABLE} and Gaussian floats using {@link Hasher#randomize2(long)} and {@link Ziggurat}.
     * @param seed any long; will be scrambled with {@link Hasher#randomize2(long)}
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed) {
        return rotateStep(seed, randomRotation3D(seed - 4), 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Hasher#randomize2(long)} and {@link Ziggurat}, and uses an existing 3D rotation matrix to avoid redoing
     * that work.
     * @param seed any long; will be scrambled with {@link Hasher#randomize2(long)}
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed, float[] rotation3D) {
        return rotateStep(seed, rotation3D, 4);
    }
}
