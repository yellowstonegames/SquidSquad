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

package com.github.yellowstonegames.old.v300.squidmath;

import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.old.v300.CrossHash;

import java.io.Serializable;

public class SilkRNG extends AbstractRNG implements IStatefulRNG, Serializable {
    private static final long serialVersionUID = 5L;

    public int stateA, stateB;

    /**
     * Creates a new generator seeded using two calls to Math.random().
     */
    public SilkRNG()
    {
        setState((int)((Math.random() - 0.5) * 0x1p32), (int)((Math.random() - 0.5) * 0x1p32));
    }
    /**
     * Constructs this SilkRNG by dispersing the bits of seed using {@link #setSeed(int)} across the two parts of state
     * this has.
     * @param seed an int that won't be used exactly, but will affect both components of state
     */
    public SilkRNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this SilkRNG by splitting the given seed across the two parts of state this has with
     * {@link #setState(long)}.
     * @param seed a long that will be split across both components of state
     */
    public SilkRNG(final long seed) {
        setState(seed);
    }
    /**
     * Constructs this SilkRNG by calling {@link #setState(int, int)} on stateA and stateB as given; see that method
     * for the specific details (stateA and stateB are kept as-is).
     * @param stateA the number to use as the first part of the state
     * @param stateB the number to use as the second part of the state
     */
    public SilkRNG(final int stateA, final int stateB) {
        setState(stateA, stateB);
    }

    public SilkRNG(final String seed) {
        setState(CrossHash.hash(seed), seed == null ? 0 : seed.hashCode());
    }

    /**
     * Get up to 32 bits (inclusive) of random output; the int this produces
     * will not require more than {@code bits} bits to represent.
     *
     * @param bits an int between 1 and 32, both inclusive
     * @return a random number that fits in the specified number of bits
     */
    @Override
    public int next(int bits) {
        final int s = (stateA += 0xC1C64E6D);
        final int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        return (x ^ x >>> 15) >>> (32 - bits);
    }

    /**
     * Get a random integer between Integer.MIN_VALUE to Integer.MAX_VALUE (both inclusive).
     *
     * @return a 32-bit random int.
     */
    @Override
    public int nextInt() {
        final int s = (stateA += 0xC1C64E6D); // Weyl sequence, period is 2 to the 32
        final int t = (stateB += (s | -s) >> 31 & 0x9E3779BB); // updates stateB only when s != 0
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE); // mix s and t; (xorshifted s) gets multiplied by a negative odd number
        x = (x ^ x >>> 16) * 0xAC451; // extra strengthening step; multiplier needs to be small for GWT
        return (x ^ x >>> 15); // closing xorshift to bring the randomizing effect from multiplication to lower bits
    }
    /**
     * Returns a random non-negative integer below the given bound, or 0 if the bound is 0 or
     * negative.
     *
     * @param bound the upper bound (exclusive)
     * @return the found number
     */
    @Override
    public int nextInt(final int bound) {
        final int s = (stateA += 0xC1C64E6D);
        final int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        return (int) ((bound * ((x ^ x >>> 15) & 0xFFFFFFFFL)) >>> 32) & ~(bound >> 31);
    }

    /**
     * Get a random long between Long.MIN_VALUE to Long.MAX_VALUE (both inclusive).
     *
     * @return a 64-bit random long.
     */
    @Override
    public long nextLong() {
        int s = (stateA + 0xC1C64E6D);
        int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        final long high = (x ^ x >>> 15);
        s = (stateA += 0x838C9CDA);
        t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        return (high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL);
    }

    /**
     * Get a random bit of state, interpreted as true or false with approximately equal likelihood.
     * This implementation uses a sign check as an optimization.
     *
     * @return a random boolean.
     */
    @Override
    public boolean nextBoolean() {
        final int s = (stateA += 0xC1C64E6D);
        final int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        return (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE) < 0;
    }

    /**
     * Gets a random double between 0.0 inclusive and 1.0 exclusive.
     * This returns a maximum of 0.9999999999999999 because that is the largest double value that is less than 1.0 .
     *
     * @return a double between 0.0 (inclusive) and 0.9999999999999999 (inclusive)
     */
    @Override
    public double nextDouble() {
        int s = (stateA + 0xC1C64E6D);
        int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        final long high = (x ^ x >>> 15);
        s = (stateA += 0x838C9CDA);
        t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        return  (((high << 32) | ((x ^ x >>> 15) & 0xFFFFFFFFL))
                & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Gets a random float between 0.0f inclusive and 1.0f exclusive.
     * This returns a maximum of 0.99999994 because that is the largest float value that is less than 1.0f .
     *
     * @return a float between 0f (inclusive) and 0.99999994f (inclusive)
     */
    @Override
    public float nextFloat() {
        final int s = (stateA += 0xC1C64E6D);
        final int t = (stateB += (s | -s) >> 31 & 0x9E3779BB);
        int x = (s ^ s >>> 17) * ~((t ^ t >>> 12) & 0x1FFFFE);
        x = (x ^ x >>> 16) * 0xAC451;
        return ((x ^ x >>> 15) & 0xffffff) * 0x1p-24f;
    }

    /**
     * Creates a copy of this SilkRNG; it will generate the same random numbers, given the same calls in order, as this
     * SilkRNG at the point copy() is called. The copy will not share references with this SilkRNG.
     * 
     * @return a copy of this SilkRNG
     */
    @Override
    public SilkRNG copy() {
        return new SilkRNG(stateA, stateB);
    }

    /**
     * Gets a view of this IRNG in a way that implements {@link Serializable}, which is simply this IRNG.
     * @return a {@link Serializable} view of this IRNG or a similar one; always {@code this}
     */
    @Override
    public Serializable toSerializable() {
        return this;
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
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int.
     * @param stateA any int
     */

    public void setStateA(int stateA)
    {
        this.stateA = stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int.
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
    }

    /**
     * Sets the current internal state of this SilkRNG with two ints, where stateA and stateB can each be any int.
     * @param stateA any int
     * @param stateB any int
     */
    public void setState(int stateA, int stateB)
    {
        this.stateA = stateA;
        this.stateB = stateB;
    }

    /**
     * Get the current internal state of the StatefulRandomness as a long.
     *
     * @return the current internal state of this object.
     */
    @Override
    public long getState() {
        return stateA & 0xFFFFFFFFL | ((long)stateB) << 32;
    }

    /**
     * Set the current internal state of this StatefulRandomness with a long.
     *
     * @param state a 64-bit long. You should avoid passing 0; this implementation will treat it as 1.
     */
    @Override
    public void setState(final long state) {
        stateA = (int)(state & 0xFFFFFFFFL);
        stateB = (int)(state >>> 32);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SilkRNG silkRNG = (SilkRNG) o;

        if (stateA != silkRNG.stateA) return false;
        return stateB == silkRNG.stateB;
    }

    @Override
    public int hashCode() {
        return 31 * stateA + stateB;
    }
    
    @Override
    public String toString() {
        return "SilkRNG with stateA 0x" + DigitTools.hex(stateA) + " and stateB 0x" + DigitTools.hex(stateB);
    }

    /**
     * A deterministic random int generator that, given one int {@code state} as input, irreversibly returns an 
     * almost-always-different int as a result. Unlike the rest of SilkRNG, this will not produce all possible ints given
     * all ints as inputs, and probably a third of all possible ints cannot be returned. You should call this with
     * {@code SilkRNG.determineInt(state = state + 1 | 0)} (you can subtract 1 to go backwards instead of forwards),
     * which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code SilkRNG.determineInt(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique int that is usually very different from {@code state}
     */
    public static int determineInt(int state) {
        return (state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19;
    }
    
    /**
     * A deterministic random int generator that, given one int {@code state} and an outer int {@code bound} as input,
     * returns an int between 0 (inclusive) and {@code bound} (exclusive) as a result, which should have no noticeable
     * correlation between {@code state} and the result. You should call this with
     * {@code SilkRNG.determineBound(state = state + 1 | 0, bound)} (you can subtract 1 to go backwards instead of
     * forwards), which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * Like most bounded int generation in SquidLib, this uses some long math, but most of the function uses ints.
     * @param state an int that should go up or down by 1 each call, as with {@code SilkRNG.determineBounded(state = state + 1 | 0, bound)} to handle overflow
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
     * call this with {@code SilkRNG.determine(state = state + 1 | 0)} (you can subtract 1 to go backwards instead of
     * forwards), which will allow overflow in the incremented state to be handled the same on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code SilkRNG.determine(state = state + 1 | 0)} to handle overflow 
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
     * almost-always-different float between 0.0f and 1.0f as a result. Unlike the rest of SilkRNG, this might not
     * produce all possible floats given all ints as inputs, and some fraction of possible floats cannot be returned.
     * You should call this with {@code SilkRNG.determineFloat(state = state + 1 | 0)} (you can subtract 1 to go
     * backwards instead of forwards), which will allow overflow in the incremented state to be handled the same on GWT
     * as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code SilkRNG.determineFloat(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique float from 0.0f to 1.0f that is usually very different from {@code state}
     */
    public static float determineFloat(int state) {
        return (((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) & 0xFFFFFF) * 0x1p-24f;
    }
    /**
     * A deterministic random double generator that, given one int {@code state} as input, returns an 
     * almost-always-different double between 0.0 and 1.0 as a result. This cannot produce more than a tiny fraction of
     * all possible doubles because the input is 32 bits and at least 53 bits are needed to represent most doubles from
     * 0.0 to 1.0. You should call this with {@code SilkRNG.determineDouble(state = state + 1 | 0)} (you can subtract 1
     * to go backwards instead of forwards), which will allow overflow in the incremented state to be handled the same
     * on GWT as on desktop.
     * @param state an int that should go up or down by 1 each call, as with {@code SilkRNG.determineDouble(state = state + 1 | 0)} to handle overflow 
     * @return a not-necessarily-unique double from 0.0 to 1.0 that is usually very different from {@code state}
     */
    public static double determineDouble(int state)
    {
        return ((state = ((state = (state ^ 0xD1B54A35) * 0x102473) ^ state >>> 11 ^ state >>> 21) * (state | 0xFFE00001)) ^ state >>> 13 ^ state >>> 19) * 0x1p-32 + 0.5;
    }
}
