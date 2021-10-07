package com.github.yellowstonegames.old.v300;


import com.github.yellowstonegames.core.DigitTools;

import javax.annotation.Nonnull;

public final class GWTRNG implements LegacyRandom, StatefulRandomness {
    public int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public GWTRNG()
    {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this GWTRNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public GWTRNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this GWTRNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public GWTRNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this GWTRNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is unless they are both 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     */
    public GWTRNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }

    /**
     * Hashes {@code seed} using both {@link CrossHash#hash(CharSequence)} and {@link String#hashCode()} and uses those
     * two results as the two states with {@link #setState(int, int)}. If seed is null, this won't call
     * String.hashCode() on it and will instead use 1 as that state (to avoid the forbidden double-zero case).
     * @param seed any String; may be null
     */
    public GWTRNG(final String seed) {
        setState(CrossHash.hash(seed), seed == null ? 1 : seed.hashCode());
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
    public final int next(int bits) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 28 | result >>> 4) + 0x9E3779BD >>> (32 - bits);
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public final int nextInt() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (result << 28 | result >>> 4) + 0x9E3779BD;
    }
    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public final int nextInt(final int bound) {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (int) ((bound * ((result << 28 | result >>> 4) + 0x9E3779BD & 0xFFFFFFFFL)) >>> 32) & ~(bound >> 31);
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
    public final long nextLong() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = s0 * 31;
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        final int low = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return ((high << 28 | high >>> 4) + 0x9E3779BDL) << 32
                | ((low << 28 | low >>> 4) + 0x9E3779BD & 0xFFFFFFFFL);
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This implementation uses a sign check as a safeguard, since its algorithm is based on (but is not equivalent to)
     * xoroshiro, which recommends a sign check instead of using the least significant bit.
     *
     * @return a random boolean.
     */
    @Override
    public final boolean nextBoolean() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = s0 * 31;
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return (high << 28 | high >>> 4) + 0x9E3779BD < 0;
    }

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public final double nextDouble() {
        int s0 = stateA;
        int s1 = stateB ^ s0;
        final int high = s0 * 31;
        s0 = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        s1 = (s1 << 13 | s1 >>> 19) ^ s0;
        final int low = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return  ((((high << 28 | high >>> 4) + 0x9E3779BDL) << 32
                | ((low << 28 | low >>> 4) + 0x9E3779BD & 0xFFFFFFFFL))
                & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public final float nextFloat() {
        final int s0 = stateA;
        final int s1 = stateB ^ s0;
        final int result = s0 * 31;
        stateA = (s0 << 26 | s0 >>> 6) ^ s1 ^ (s1 << 9);
        stateB = (s1 << 13 | s1 >>> 19);
        return ((result << 28 | result >>> 4) + 0x9E3779BD >>> 8) * 0x1p-24f;
    }

    /**
     * Creates a copy of this GWTRNG; it will generate the same random numbers, given the same calls in order, as this
     * GWTRNG at the point copy() is called. The copy will not share references with this GWTRNG.
     * 
     * @return a copy of this GWTRNG
     */
    @Nonnull
    @Override
    public GWTRNG copy() {
        return new GWTRNG(stateA, stateB);
    }

    /**
     * Sets the state of this generator using one int, running it through Zog32RNG's algorithm two times to get 
     * two ints. If the states would both be 0, state A is assigned 1 instead.
     * @param seed the int to use to produce this generator's state
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55, a = seed ^ z;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateA = (z ^ z >>> 20) + (a ^= a << 13);
        z = seed + 0x8E9D5AAA;
        a ^= a >>> 14;
        z = (z ^ z >>> 10) * 0xA5CB3;
        a ^= a >>> 15;
        stateB = (z ^ z >>> 20) + (a ^ a << 13);
        if((stateA | stateB) == 0)
            stateA = 1;
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and stateB is
     * already 0, this will set stateA to 1 instead, since both states cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateA any int
     */

    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and stateA is
     * already 0, this will set stateA to 1 and stateB to 0, since both cannot be 0 at the same time. Usually, you
     * should use {@link #setState(int, int)} to set both states at once, but the result will be the same if you call
     * setStateA() and then setStateB() or if you call setStateB() and then setStateA().
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateB | stateA) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this GWTRNG with three ints, where stateA and stateB can each be any int
     * unless they are both 0 (which will be treated as if stateA is 1 and stateB is 0).
     * @param stateA any int (if stateA and stateB are both 0, this will be treated as 1)
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA == 0 && stateB == 0 ? 1 : stateA;
        this.stateB = stateB;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0; this implementation will treat it as 1.
     */
    public void setState(long state) {
        stateA = state == 0 ? 1 : (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    /**
     * Sets both int states of the SilkRNG with one long value.
     * @param seed any long value except 0 (this will handle 0 by changing it to 1)
     */
    @Override
    public void setSeed(long seed) {
        setState(seed);
    }

    /**
     * Always returns 1 (counting in terms of long states).
     * @return 1 (one)
     */
    @Override
    public int getStateCount() {
        return 1;
    }

    /**
     * Identical to {@link #getState()}, this gets both int states as one long value.
     * @param selection doesn't matter; unused
     * @return the combined states as one long
     */
    @Override
    public long getSelectedState(int selection) {
        return getState();
    }

    /**
     * Sets both int states of the SilkRNG with one long value.
     * @param selection doesn't matter; unused
     * @param value the long value that will be split up into the two int states this uses
     */
    @Override
    public void setSelectedState(int selection, long value) {
        setState(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTRNG gwtrng = (GWTRNG) o;

        if (stateA != gwtrng.stateA) return false;
        return stateB == gwtrng.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
    
    @Override
    public String toString() {
        return "GWTRNG with stateA 0x" + DigitTools.hex(stateA) + " and stateB 0x" + DigitTools.hex(stateB);
    }

    /**
     * A deterministic random int generator that, given one int {@code state} as input, irreversibly returns an 
     * almost-always-different int as a result. Unlike the rest of GWTRNG, this will not produce all possible ints given
     * all ints as inputs, and probably a third of all possible ints cannot be returned. You should call this with
     * {@code GWTRNG.determineInt(state = state + 1 | 0)} (you can subtract 1 to go backwards instead of forwards),
     * which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code GWTRNG.determineInt(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique int that is usually very different from {@code state}
     */
    public static int determineInt(int state) {
        return (state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19;
    }
    
    /**
     * A deterministic random int generator that, given one int {@code state} and an outer int {@code bound} as input,
     * returns an int between 0 (inclusive) and {@code bound} (exclusive) as a result, which should have no noticeable
     * correlation between {@code state} and the result. You should call this with
     * {@code GWTRNG.determineBound(state = state + 1 | 0, bound)} (you can subtract 1 to go backwards instead of
     * forwards), which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * Like most bounded int generation in SquidLib, this uses some long math, but most of the function uses ints.
     * @param state an int that should go up or down by 1 each call, as with {@code GWTRNG.determineBounded(state = state + 1 | 0, bound)} to handle overflow
     * @param bound the outer exclusive bound, as an int; may be positive or negative
     * @return an int between 0 (inclusive) and {@code bound} (exclusive)
     */
    public static int determineBounded(int state, final int bound)
    {
        return (int) ((((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL) * bound >> 32);
    }
    /**
     * A deterministic random long generator that, given one int {@code state} as input, returns an 
     * almost-always-different long as a result. This can only return a tiny fraction of all possible longs, since there
     * are at most 2 to the 32 possible ints and this doesn't even return different values for each of those. You should
     * call this with {@code GWTRNG.determine(state = state + 1 | 0)} (you can subtract 1 to go backwards instead of
     * forwards), which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code GWTRNG.determine(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique long that is usually very different from {@code state}
     */
    public static long determine(int state)
    {
        int r = (state ^ 0xD1B54A35) * 0x102473;
        return ((long) ((r = (r ^ r >>> 11 ^ r >>> 21) * (r | 0xFFE00001)) ^ r >>> 13 ^ r >>> 19) << 32) 
                | (((state = ((state = (r ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFFFFL);
    }
    /**
     * A deterministic random float generator that, given one int {@code state} as input, returns an 
     * almost-always-different float between 0.0f and 1.0f as a result. Unlike the rest of GWTRNG, this might not
     * produce all possible floats given all ints as inputs, and some fraction of possible floats cannot be returned.
     * You should call this with {@code GWTRNG.determineFloat(state = state + 1 | 0)} (you can subtract 1 to go
     * backwards instead of forwards), which will allow overflow in the incremented state to be handled the same on GWT
     * as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code GWTRNG.determineFloat(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique float from 0.0f to 1.0f that is usually very different from {@code state}
     */
    public static float determineFloat(int state) {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFF) * 0x1p-24f;
    }
    /**
     * A deterministic random double generator that, given one int {@code state} as input, returns an 
     * almost-always-different double between 0.0 and 1.0 as a result. This cannot produce more than a tiny fraction of
     * all possible doubles because the input is 32 bits and at least 53 bits are needed to represent most doubles from
     * 0.0 to 1.0. You should call this with {@code GWTRNG.determineDouble(state = state + 1 | 0)} (you can subtract 1
     * to go backwards instead of forwards), which will allow overflow in the incremented state to be handled the same
     * on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code GWTRNG.determineDouble(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique double from 0.0 to 1.0 that is usually very different from {@code state}
     */
    public static double determineDouble(int state)
    {
        return ((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) * 0x1p-32 + 0.5;
    }
}
