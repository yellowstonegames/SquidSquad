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
package com.github.yellowstonegames.old.v300;

import com.github.yellowstonegames.core.DigitTools;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * This is a SplittableRandom-style generator, meant to have a tiny state
 * that permits storing many different generators with low overhead.
 * It should be rather fast, though no guarantees can be made on all hardware.
 * It should be faster than using SplittableRandom from Java 8 because it has a
 * single "gamma" value that prevents it from being split but also makes runtime
 * a bit quicker. It is based on using a unary hash on a large-increment counter,
 * which makes even the first item obtained from similarly-seeded LightRNGs very
 * likely to be very different. This is an advantage over non-hash-based RNGs.
 * <br>
 * This generator is especially fast on OpenJ9, version 0.17.0 or later (from
 * late 2019), and can be several times faster there than on HotSpot.
 * <br>
 * Written in 2015 by Sebastiano Vigna (vigna@acm.org)
 * @author Sebastiano Vigna
 * @author <a href="https://github.com/tommyettinger">Tommy Ettinger</a>
 */
public class LightRNG extends LegacyRandom implements StatefulRandomness {
    @Override
    public String getTag() {
        return "LigL";
    }

    public long state; /* The state can be seeded with any value. */

    /** Creates a new generator seeded using Math.random. */
    public LightRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public LightRNG(final long seed ) {
        setSeed(seed);
    }

    /**
     * Gets a pseudo-random int with at most the specified count of bits; for example, if bits is 3 this can return any
     * int between 0 and 2 to the 3 (that is, 8), exclusive on the upper end. That would mean 7 could be returned, but
     * no higher ints, and 0 could be returned, but no lower ints.
     *
     * @param bits the number of bits to be returned; if 0 or less, or if 32 or greater, can return any 32-bit int
     * @return a pseudo-random int that uses at most the specified amount of bits
     */
    @Override
    public int next( int bits ) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int)(z ^ (z >>> 31)) >>> (32 - bits);
    }
    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     * @return any long, all 64 bits are random
     */
    @Override
    public long nextLong() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @NonNull
    @Override
    public LightRNG copy() {
        return new LightRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) (z ^ (z >>> 31)); 
    }

    /**
     * Returns a random non-negative integer between 0 (inclusive) and the given bound (exclusive),
     * or 0 if the bound is 0. The bound can be negative, which will produce 0 or a negative result.
     * Uses an aggressively optimized technique that has some bias, but mostly for values of
     * bound over 1 billion. This method will always advance state exactly once (equivalent to one call to
     * {@link #nextLong()}).
     * <br>
     * <a href="http://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/">Credit goes to Daniel Lemire</a>.
     *
     * @param bound the outer bound (exclusive), can be negative or positive
     * @return the found number
     */
    public int nextInt( final int bound ) {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return (int) ((bound * ((z ^ z >>> 31) >>> 1 & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Inclusive lower, exclusive upper. Although you should usually use a higher value for upper than for lower, you
     * can use a lower "upper" than "lower" and this will still work, producing an int between the two bounds.
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, can be positive or negative, usually should be greater than lower
     * @return a random int between lower (inclusive) and upper (exclusive)
     */
    public int nextInt( final int lower, final int upper ) {
        return lower + nextInt(upper - lower);
    }

    /**
     * Exclusive on the outer bound; the inner bound is 0. The bound may be negative, which will produce a non-positive
     * result.
     * @param bound the outer exclusive bound; may be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = nextLong();
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long t = rand * boundLow + (randLow * boundLow >>> 32);
        return rand * bound + (t >> 32) + (randLow * bound + (t & 0xFFFFFFFFL) >> 32);
    }
    
    /**
     * Inclusive inner, exclusive outer; both inner and outer can be positive or negative.
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative and may be greater than or less than inner
     * @return a random long that may be equal to inner and will otherwise be between inner and outer
     */
    public long nextLong(final long inner, final long outer) {
        return inner + nextLong(outer - inner);
    }

    /**
     * Gets a uniform random double in the range [0.0,1.0)
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public double nextDouble() {
        long z = state += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return ((z ^ (z >>> 31)) & 0x1fffffffffffffL) * 0x1p-53;
    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public double nextDouble(final double outer) {
        return nextDouble() * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public float nextFloat() {
        return ((nextLong() >>> 8) & 0xffffffL) * 0x1p-24f;

    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     * @return a random true or false value.
     */
    public boolean nextBoolean() {
        return nextLong() < 0L;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public void nextBytes( final byte[] bytes ) {
        int i = bytes.length, n;
        while( i != 0 ) {
            n = Math.min( i, 8 );
            for ( long bits = nextLong(); n-- != 0; bits >>>= 8 ) bytes[ --i ] = (byte)bits;
        }
    }



    /**
     * Sets the seed of this generator (which is also the current state).
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    public void setSeed( final long seed ) {
        state = seed;
    }
    /**
     * Sets the seed (also the current state) of this generator.
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    public void setState( final long seed ) {
        state = seed;
    }
    /**
     * Gets the current state of this generator.
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    public long getState() {
        return state;
    }

    @Override
    public int getStateCount() {
        return 1;
    }

    @Override
    public long getSelectedState(int selection) {
        return state;
    }

    @Override
    public void setSelectedState(int selection, long value) {
        state = value;
    }

    /**
     * Advances or rolls back the LightRNG's state without actually generating each number. Skips forward
     * or backward a number of steps specified by advance, where a step is equal to one call to nextLong(),
     * and returns the random number produced at that step (you can get the state with {@link #getState()}).
     *
     * @param advance Number of future generations to skip over; can be negative to backtrack, 0 gets the most recent generated number
     * @return the random long generated after skipping advance numbers
     */
    @Override
    public long skip(long advance) {
        long z = (state += 0x9E3779B97F4A7C15L * advance);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }


    @Override
    public String toString() {
        return "LightRNG with state 0x" + DigitTools.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightRNG lightRNG = (LightRNG) o;

        return state == lightRNG.state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    public static long determine(long state)
    {
        return ((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);
    }
    public static long determine(final int a, final int b)
    {
        long state = 0x9E3779B97F4A7C15L + (a & 0xFFFFFFFFL) + ((long)b << 32);
        state = ((state >>> 30) ^ state) * 0xBF58476D1CE4E5B9L;
        state = (state ^ (state >>> 27)) * 0x94D049BB133111EBL;
        return state ^ (state >>> 31);
    }

    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0x7FFFFFFFL)) >> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g. {@code determine(++state)},
     * where the increment for state should be odd but otherwise doesn't really matter. This multiplies state by
     * {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much different from using a
     * very large one, as long as it is odd. The period is 2 to the 64 if you increment or decrement by 1, but there are
     * only 2 to the 30 possible floats between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float determineFloat(long state) { return (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0xFFFFFF) * 0x1p-24f; }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code determine(++state)}, where the increment for state should be odd but otherwise doesn't really matter. This
     * multiplies state by {@code 0x9E3779B97F4A7C15L} within this method, so using a small increment won't be much
     * different from using a very large one, as long as it is odd. The period is 2 to the 64 if you increment or
     * decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code determine(++state)} is recommended to go forwards or {@code determine(--state)} to
     *              generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double determineDouble(long state) { return (((state = ((state = ((state *= 0x9E3779B97F4A7C15L) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31) & 0x1FFFFFFFFFFFFFL) * 0x1p-53; }
}
