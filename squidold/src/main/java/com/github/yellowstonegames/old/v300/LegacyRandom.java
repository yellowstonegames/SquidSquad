package com.github.yellowstonegames.old.v300;

import com.github.tommyettinger.ds.support.EnhancedRandom;

/**
 * An alternate version of {@link EnhancedRandom} that intentionally implements certain methods incorrectly, for
 * compatibility with SquidLib 3.0.0.
 */
public interface LegacyRandom extends EnhancedRandom {

    /**
     * This is subtly incorrect; if outerBound is 0, then this returns 0 only very rarely, even if outerBound is small,
     * like -1 or -2. It is implemented as SquidLib 3.0.0 implemented it to reproduce behavior.
     * @param outerBound outer bound, may be positive or negative
     * @return an int between 0 (inclusive, but very rarely) and outerBound (exclusive)
     */
    @Override
    default int nextSignedInt(int outerBound) {
        return (int) ((outerBound * (long)next(31)) >> 31);
    }

    /**
     * This is buggier than a bumblebee; do not use this in production. It is only here to reproduce bugs from 3.0.0.
     * @param outerBound the outer bound, positive or negative; will probably not be respected
     * @return honestly, this could return anything -- a whale and a potted plant are possibilities
     */
    @Override
    default long nextSignedLong(long outerBound) {
        long rand = nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = outerBound & 0xFFFFFFFFL;
        rand >>>= 32;
        outerBound >>= 32;
        final long t = rand * boundLow + (randLow * boundLow >>> 32);
        return rand * outerBound + (t >> 32) + (randLow * outerBound + (t & 0xFFFFFFFFL) >> 32);
    }
}
