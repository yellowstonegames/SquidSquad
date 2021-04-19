package com.github.yellowstonegames.old.v300;

public class CrossHash {
    /**
     * Big constant 0.
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1.
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2.
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3.
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4.
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5.
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits).
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long mum(final long a, final long b) {
        final long n = a * b;
        return n - (n >>> 32);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * <br>
     * This was changed so it distributes bits from both inputs a little better on July 6, 2019.
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    public static long hash64(final CharSequence data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public static long hash64(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = 9069147967908697017L;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return mum(seed ^ seed << 16, len - start ^ b0);
    }

    public static int hash(final CharSequence data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
    }

}
