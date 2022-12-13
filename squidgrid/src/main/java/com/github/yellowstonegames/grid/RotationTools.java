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
}
