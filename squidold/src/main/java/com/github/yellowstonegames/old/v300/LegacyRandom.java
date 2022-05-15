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

package com.github.yellowstonegames.old.v300;

import com.github.tommyettinger.random.EnhancedRandom;

/**
 * An alternate version of {@link EnhancedRandom} that intentionally implements certain methods incorrectly, for
 * compatibility with SquidLib 3.0.0. Currently, {@link #nextSignedInt(int)} and {@link #nextSignedLong(long)} are
 * incorrectly implemented for compatibility, and that makes {@link #nextSignedInt(int, int)} incorrect too. Other
 * methods here are implemented as {@link EnhancedRandom} does in its default implementations.
 */
public abstract class LegacyRandom extends EnhancedRandom {

    /**
     * This is slightly different from how EnhancedRandom implements this method, and this version may have some bias
     * for large bounds (more bias than EnhancedRandom has).
     *
     * @param bound the outer exclusive bound; if non-positive, this returns 0
     * @return a random int between 0 (inclusive) and bound (exclusive); never negative
     */
    @Override
    public int nextInt(final int bound) {
        return (int) ((bound * (long) next(31)) >>> 31) & ~(bound >> 31);
    }

    /**
     * This is subtly incorrect; if outerBound is 0, then this returns 0 only very rarely, even if outerBound is small,
     * like -1 or -2. It is implemented as SquidLib 3.0.0 implemented it to reproduce behavior.
     *
     * @param outerBound outer bound, may be positive or negative
     * @return an int between 0 (inclusive, but very rarely) and outerBound (exclusive)
     */
    @Override
    public int nextSignedInt(int outerBound) {
        return (int) ((outerBound * (long) next(31)) >> 31);
    }

    /**
     * This is buggier than a bumblebee; do not use this in production. It is only here to reproduce bugs from 3.0.0.
     *
     * @param outerBound the outer bound, positive or negative; will probably not be respected
     * @return honestly, this could return anything -- a whale and a potted plant are possibilities
     */
    @Override
    public long nextSignedLong(long outerBound) {
        long rand = nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = outerBound & 0xFFFFFFFFL;
        rand >>>= 32;
        outerBound >>= 32;
        final long t = rand * boundLow + (randLow * boundLow >>> 32);
        return rand * outerBound + (t >> 32) + (randLow * outerBound + (t & 0xFFFFFFFFL) >> 32);
    }

    /**
     * Gets a random double between 0 and 1 (inclusive on 0, exclusive on 1).
     * This one isn't incorrectly implemented, it's just different from how EnhancedRandom does it.
     * @return a random double between 0 (inclusive) and 1 (exclusive)
     */
    @Override
    public double nextDouble() {
        return (nextLong() & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a random float between 0 and 1 (inclusive on 0, exclusive on 1).
     * This one isn't incorrectly implemented, it's just different from how EnhancedRandom does it.
     * @return a random float between 0 (inclusive) and 1 (exclusive)
     */
    public float nextFloat() {
        return next(24) * 0x1p-24f;
    }

}