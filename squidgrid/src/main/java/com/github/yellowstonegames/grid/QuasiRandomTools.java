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
 * Static methods to produce numbers and points from quasi-random sequences (which seem like random sequences, but have
 * lower "discrepancy" between results). In practice, this means that the points this produces from a sequence tend to
 * be distant from other points from nearby in the sequence.
 */
public class QuasiRandomTools {
    /**
     * Gets the {@code index}-th element from the base-{@code base} van der Corput sequence. The base should usually be
     * a prime number. The index must be greater than 0 and should be less than 16777216. The number this returns is a
     * float between 0 (inclusive) and 1 (exclusive).
     *
     * @param base  a prime number to use as the base/radix of the van der Corput sequence
     * @param index the position in the sequence of the requested base, as a positive int
     * @return a quasi-random float between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static float vanDerCorput(final int base, final int index) {
        if (base <= 2) {
            return (Integer.reverse(index) >>> 8) * 0x1p-24f;
        }
        float denominator = base, res = 0.0f;
        int n = (index & 0x00ffffff);
        while (n > 0) {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return res;
    }
    /**
     * Gets a 2D point from the Halton sequence with the bases 3 (for x) and 5 (for y).
     * @param width the exclusive upper bound on the possible values for x
     * @param height the exclusive upper bound on the possible values for y
     * @param index the index in the Halton sequence with the given bases, as a positive int
     * @return a Coord with x between 0 (inclusive) and width (exclusive), and y between 0 (inclusive) and height (exclusive)
     */
    public static Coord halton2D(final int width, final int height, final int index) {
        return halton2D(3, 5, width, height, index);
    }

    /**
     * Gets a 2D point from the Halton sequence with the specified bases, which must be different primes.
     * @param baseX should be prime and different from baseY
     * @param baseY should be prime and different from baseX
     * @param width the exclusive upper bound on the possible values for x
     * @param height the exclusive upper bound on the possible values for y
     * @param index the index in the Halton sequence with the given bases, as a positive int
     * @return a Coord with x between 0 (inclusive) and width (exclusive), and y between 0 (inclusive) and height (exclusive)
     */
    public static Coord halton2D(final int baseX, final int baseY, final int width, final int height, final int index) {
        return Coord.get((int)(vanDerCorput(baseX, index) * width), (int)(vanDerCorput(baseY, index) * height));
    }
}
