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
     *
     * @param width  the exclusive upper bound on the possible values for x
     * @param height the exclusive upper bound on the possible values for y
     * @param index  the index in the Halton sequence with the given bases, as a positive int
     * @return a Coord with x between 0 (inclusive) and width (exclusive), and y between 0 (inclusive) and height (exclusive)
     */
    public static Coord halton2D(final int width, final int height, final int index) {
        return halton2D(3, 5, width, height, 0, 0, index);
    }

    /**
     * Gets a 2D point from the Halton sequence with the specified bases, which must be different primes.
     *
     * @param baseX   should be prime and different from baseY
     * @param baseY   should be prime and different from baseX
     * @param width   the x-size of the space this can place a Coord
     * @param height  the y-size of the space this can place a Coord
     * @param offsetX the minimum x-position of a Coord
     * @param offsetY the minimum y-position of a Coord
     * @param index   the index in the Halton sequence with the given bases, as a positive int
     * @return a Coord with x between offsetX (inclusive) and offsetX + width (exclusive), and y between offsetY
     * (inclusive) and offsetY + height (exclusive)
     */
    public static Coord halton2D(final int baseX, final int baseY, final int width, final int height, final int offsetX, final int offsetY, final int index) {
        return Coord.get((int) (vanDerCorput(baseX, index) * width) + offsetX, (int) (vanDerCorput(baseY, index) * height) + offsetY);
    }

    /**
     * Martin Roberts' "unreasonably effective" quasi-random point sequence based on a 2D analogue to the golden ratio.
     * See <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">his blog</a> for
     * more detailed info, but this can be summarized as being excellent at separating points at the expense of
     * seeming less random. Produces a Coord with x between offsetX (inclusive) and offsetX + width (exclusive), and y
     * between offsetY (inclusive) and offsetY + height (exclusive), with the Coord at each {@code index} likely to be
     * different for at least {@code width * height / 4} indices (very low sizes may offer less of a guarantee).
     * <br>
     * This is also called the R2 sequence.
     *
     * @param width   the x-size of the space this can place a Coord
     * @param height  the y-size of the space this can place a Coord
     * @param offsetX the minimum x-position of a Coord
     * @param offsetY the minimum y-position of a Coord
     * @param index   the index of the Coord in the 2D Roberts sequence; should be greater than 0, but not required to be
     * @return a Coord with x,y between offsetX,offsetY inclusive and offsetX+width,offsetY+height exclusive
     */
    public static Coord roberts2D(int width, int height, int offsetX, int offsetY, int index) {
        return Coord.get((int) ((index * 0xC13FA9A9L & 0xFFFFFFFFL) * width >>> 32) + offsetX, (int) ((index * 0x91E10DA5L & 0xFFFFFFFFL) * height >>> 32) + offsetY);
    }

}
