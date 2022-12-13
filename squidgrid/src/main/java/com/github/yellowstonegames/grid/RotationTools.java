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
import com.github.tommyettinger.random.EnhancedRandom;

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
     * Creates a new 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given random number generator to get just one int, looking
     * it up in {@link TrigTools#SIN_TABLE}.
     * @param random any EnhancedRandom generator
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(EnhancedRandom random) {
        final int index = random.next(14);
        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new float[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given random number generator to get one int and three
     * Gaussian floats.
     * @param random any EnhancedRandom generator
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(EnhancedRandom random) {
        final int index = random.next(14);
        final float x = (float) random.nextGaussian() * MathTools.ROOT2;
        final float y = (float) random.nextGaussian() * MathTools.ROOT2;
        final float z = (float) random.nextGaussian() * MathTools.ROOT2;
        final float xx = x * x - 1;
        final float yy = y * y - 1;
        final float zz = z * z - 1;
        final float xy = x * y;
        final float xz = x * z;
        final float yz = y * z;

        final float s = TrigTools.SIN_TABLE[index];
        final float c = TrigTools.SIN_TABLE[index + TrigTools.SIN_TO_COS & TrigTools.TABLE_MASK];
        return new float[]{
                c * xx - s * xy, s * xx + c * xy, xz,
                c * xy - s * yy, s * xy + c * yy, yz,
                c * xz - s * yz, s * xz + c * yz, zz};
    }
}
