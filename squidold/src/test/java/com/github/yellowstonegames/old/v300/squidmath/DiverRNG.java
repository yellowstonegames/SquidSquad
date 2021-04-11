package com.github.yellowstonegames.old.v300.squidmath;

import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.Hasher;

import java.io.Serializable;

public final class DiverRNG implements StatefulRandomness, Serializable {

    private static final long serialVersionUID = 153186732328748834L;

    private long state; /* The state can be seeded with any value. */

    /**
     * Creates a new generator seeded using Math.random.
     */
    public DiverRNG() {
        this((long) ((Math.random() - 0.5) * 0x10000000000000L)
                ^ (long) (((Math.random() - 0.5) * 2.0) * 0x8000000000000000L));
    }

    public DiverRNG(final long seed) {
        state = seed;
    }

    public DiverRNG(final String seed) {
        state = Hasher.andras.hash64(seed);
    }

    @Override
    public final int next(int bits)
    {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)(z ^ z >>> 25) >>> (32 - bits);
    }

    /**
     * Can return any long, positive or negative, of any size permissible in a 64-bit signed integer.
     *
     * @return any long, all 64 bits are random
     */
    @Override
    public final long nextLong() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (z ^ z >>> 25);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just need to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public DiverRNG copy() {
        return new DiverRNG(state);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     *
     * @return any int, all 32 bits are random
     */
    public final int nextInt() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)(z ^ z >>> 25);
    }

    /**
     * Exclusive on the outer bound.  The inner bound is 0.
     * The bound can be negative, which makes this produce either a negative int or 0.
     *
     * @param bound the upper bound; should be positive
     * @return a random int between 0 (inclusive) and bound (exclusive)
     */
    public final int nextInt(final int bound) {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return (int)((bound * ((z ^ z >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    /**
     * Inclusive inner, exclusive outer.
     *
     * @param inner the inner bound, inclusive, can be positive or negative
     * @param outer the outer bound, exclusive, can be positive or negative, usually greater than inner
     * @return a random int between inner (inclusive) and outer (exclusive)
     */
    public final int nextInt(final int inner, final int outer) {
        return inner + nextInt(outer - inner);
    }

    /**
     * Exclusive on bound (which may be positive or negative), with an inner bound of 0.
     * If bound is negative this returns a negative long; if bound is positive this returns a positive long. The bound
     * can even be 0, which will cause this to return 0L every time.
     * <br>
     * Credit for this method goes to <a href="https://oroboro.com/large-random-in-range/">Rafael Baptista's blog</a>
     * for the original idea, and the JDK10 Math class' usage of Hacker's Delight code for the current algorithm. 
     * This method is drastically faster than the previous implementation when the bound varies often (roughly 4x
     * faster, possibly more). It also always gets exactly one random long, so by default it advances the state as much
     * as {@link #nextLong()}.
     *
     * @param bound the outer exclusive bound; can be positive or negative
     * @return a random long between 0 (inclusive) and bound (exclusive)
     */
    public long nextLong(long bound) {
        long rand = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        rand = (rand << 27 | rand >>> 37) * 0xDB4F0B9175AE2165L;
        rand ^= rand >>> 25;
        final long randLow = rand & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        rand >>>= 32;
        bound >>= 32;
        final long t = rand * boundLow + (randLow * boundLow >>> 32);
        return rand * bound + (t >> 32) + (randLow * bound + (t & 0xFFFFFFFFL) >> 32);
    }
    /**
     * Inclusive inner, exclusive outer; lower and upper can be positive or negative and there's no requirement for one
     * to be greater than or less than the other.
     *
     * @param lower the lower bound, inclusive, can be positive or negative
     * @param upper the upper bound, exclusive, can be positive or negative
     * @return a random long that may be equal to lower and will otherwise be between lower and upper
     */
    public final long nextLong(final long lower, final long upper) {
        return lower + nextLong(upper - lower);
    }
    
    /**
     * Gets a uniform random double in the range [0.0,1.0)
     *
     * @return a random double at least equal to 0.0 and less than 1.0
     */
    public final double nextDouble() {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;

    }

    /**
     * Gets a uniform random double in the range [0.0,outer) given a positive parameter outer. If outer
     * is negative, it will be the (exclusive) lower bound and 0.0 will be the (inclusive) upper bound.
     *
     * @param outer the exclusive outer bound, can be negative
     * @return a random double between 0.0 (inclusive) and outer (exclusive)
     */
    public final double nextDouble(final double outer) {
        long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        z = (z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L;
        return ((z ^ z >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53 * outer;
    }

    /**
     * Gets a uniform random float in the range [0.0,1.0)
     *
     * @return a random float at least equal to 0.0 and less than 1.0
     */
    public final float nextFloat() {
        final long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        return ((z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;
    }

    /**
     * Gets a random value, true or false.
     * Calls nextLong() once.
     *
     * @return a random true or false value.
     */
    public final boolean nextBoolean() {
        final long z = (state = (state ^ 0x6C8E9CF570932BD5L) * 0xC6BC279692B5CC83L);
        return ((z << 27 | z >>> 37) * 0xDB4F0B9175AE2165L) < 0;
    }

    /**
     * Given a byte array as a parameter, this will fill the array with random bytes (modifying it
     * in-place). Calls nextLong() {@code Math.ceil(bytes.length / 8.0)} times.
     *
     * @param bytes a byte array that will have its contents overwritten with random bytes.
     */
    public final void nextBytes(final byte[] bytes) {
        int i = bytes.length, n;
        while (i != 0) {
            n = Math.min(i, 8);
            for (long bits = nextLong(); n-- != 0; bits >>>= 8) bytes[--i] = (byte) bits;
        }
    }

    /**
     * Sets the seed (also the current state) of this generator.
     *
     * @param seed the seed to use for this LightRNG, as if it was constructed with this seed.
     */
    @Override
    public final void setState(final long seed) {
        state = seed;
    }

    /**
     * Gets the current state of this generator.
     *
     * @return the current seed of this LightRNG, changed once per call to nextLong()
     */
    @Override
    public final long getState() {
        return state;
    }

    @Override
    public String toString() {
        return "DiverRNG with state 0x" + DigitTools.hex(state) + 'L';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return state == ((DiverRNG) o).state;
    }

    @Override
    public int hashCode() {
        return (int) (state ^ (state >>> 32));
    }

    public static long determine(long state)
    {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    public static long randomize(long state)
    {
        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }

    public static int determineBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0xFFFFFFFFL)) >> 32);
    }

    public static int randomizeBounded(long state, final int bound)
    {
        return (int)((bound * (((state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28) & 0xFFFFFFFFL)) >> 32);
    }

    public static float determineFloat(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

    public static float randomizeFloat(long state) {
        return (((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L >>> 40) * 0x1p-24f;

    }

    public static double determineDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    public static double randomizeDouble(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

}
