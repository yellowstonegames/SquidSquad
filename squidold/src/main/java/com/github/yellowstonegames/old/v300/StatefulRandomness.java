package com.github.yellowstonegames.old.v300;

/**
 * Intended for compatibility with StatefulRandomness from SquidLib 3.x, this should be applied to LegacyRandom
 * implementations that have one {@code long} value or two {@code int} values for state, and allow freely getting and
 * setting those states. Optimally, all states should be allowed, but this doesn't have to be the case as long as
 * {@link #setState(long)} can correct an invalid state and set the state accordingly.
 */
public interface StatefulRandomness {
    long getState();
    void setState(long state);
}
