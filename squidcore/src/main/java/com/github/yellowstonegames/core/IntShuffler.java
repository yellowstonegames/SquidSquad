package com.github.yellowstonegames.core;

import com.github.yellowstonegames.core.annotations.Beta;
import com.github.tommyettinger.ds.support.Base;

import javax.annotation.Nonnull;

/**
 * Gets a sequence of distinct pseudo-random ints (typically used as indices) from 0 to some bound, without storing all
 * the sequence in memory. Uses a Feistel network, as described in
 * <a href="https://blog.demofox.org/2013/07/06/fast-lightweight-random-shuffle-functionality-fixed/">this post by Alan Wolfe</a>.
 * The API is very simple; you construct an IntShuffler by specifying how many items it can shuffle, and you can
 * optionally use a seed (it will be random if you don't specify a seed). Call {@link #next()} on an IntShuffler
 * to get the next distinct int in the shuffled ordering; next() will return -1 if there are no more distinct ints (if
 * {@link #bound} items have already been returned). You can go back to the previous item with {@link #previous()},
 * which similarly returns -1 if it can't go earlier in the sequence. You can restart the sequence with
 * {@link #restart()} to use the same sequence over again, or {@link #restart(long)} to use a different seed (the bound
 * is fixed).
 * <br>
 * This differs from the version in Alan Wolfe's example code and blog post; it uses a very different round function,
 * and it only uses 2 rounds of it (instead of 4). Wolfe's round function is MurmurHash2, but as far as I can tell the
 * version he uses doesn't have anything like MurmurHash3's fmix32() to adequately avalanche bits, and since all keys
 * are small keys with the usage of MurmurHash2 in his code, avalanche is the most important thing. It's also perfectly
 * fine to use irreversible operations in a Feistel network round function, and I do that since it seems to improve
 * randomness slightly. The {@link #round(long, long)} method used here reuses the {@link Hasher#wow(long, long)} method;
 * it acts like an unbalanced, irreversible PRNG with two states, and that turns out to be just fine for a Feistel
 * network. Using 4 rounds turns out to be overkill in this case. This also uses a different seed for each round.
 *
 * @author Alan Wolfe
 * @author Tommy Ettinger
 */
@Beta
public class IntShuffler {
    public final int bound;
    protected int index, pow4, halfBits, leftMask, rightMask;
    protected long key0, key1;

    /**
     * Constructs an IntShuffler with a random seed and a bound of 10.
     */
    public IntShuffler(){
        this(10);
    }
    /**
     * Constructs an IntShuffler with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public IntShuffler(int bound)
    {
        this(bound, (long) ((Math.random() - 0.5) * 0x1p52)
                ^ (long) ((Math.random() - 0.5) * 0x1p64));
    }

    /**
     * Constructs an IntShuffler with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public IntShuffler(int bound, long seed)
    {
        // initialize our state
        this.bound = bound;
        restart(seed);
        // calculate next power of 4.  Needed since the balanced Feistel network needs
        // an even number of bits to work with
        pow4 = MathTools.nextPowerOfTwo(bound);
        pow4 = ((pow4 | pow4 << 1) & 0x55555554) - 1;
        // calculate our left and right masks to split our indices for the Feistel network
        halfBits = Integer.bitCount(pow4) >>> 1;
        rightMask = pow4 >>> halfBits;
        leftMask = pow4 ^ rightMask;
    }

    /**
     * Gets the next distinct int in the sequence, or -1 if all distinct ints have been returned that are non-negative
     * and less than {@link #bound}.
     * @return the next item in the sequence, or -1 if the sequence has been exhausted
     */
    public int next()
    {
        int shuffleIndex;
        // index is the index to start searching for the next number at
        while (index <= pow4)
        {
            // get the next number
            shuffleIndex = encode(index++);

            // if we found a valid index, return it!
            if (shuffleIndex < bound)
                return shuffleIndex;
        }

        // end of shuffled list if we got here.
        return -1;
    }
    /**
     * Gets the previous returned int from the sequence (as yielded by {@link #next()}), or -1 if next() has never been
     * called (or the IntShuffler has reached the beginning from repeated calls to this method).
     * @return the previously-given item in the sequence, or -1 if this can't go earlier
     */
    public int previous()
    {
        int shuffleIndex;
        // index is the index to start searching for the next number at
        while (index > 0)
        {
            // get the next number
            shuffleIndex = encode(--index);

            // if we found a valid index, return success!
            if (shuffleIndex < bound)
                return shuffleIndex;
        }

        // end of shuffled list if we got here.
        return -1;
    }

    /**
     * Starts the same sequence over from the beginning.
     */
    public void restart()
    {
        index = 0;
    }

//    /**
//     * Used to rearrange the bits of seeds this is given in a way that should partly randomize them.
//     * This is, at the time of writing, the best two-multiply unary hash that skeeto's hash-prospector
//     * (<a href="https://github.com/skeeto/hash-prospector">available here</a>) has found.
//     * @param z any int
//     * @return a pseudo-random but deterministic change of z
//     */
//    public static int determine(int z)
//    {
//        z ^= z >>> 15;
//        z *= 0xD168AAADL;
//        z ^= z >>> 15;
//        z *= 0xAF723597L;
//        return z ^ z >>> 15;
//    }
    /**
     * Starts the sequence over, but can change the seed (completely changing the sequence). If {@code seed} is the same
     * as the seed given in the constructor, this will use the same sequence, acting like {@link #restart()}.
     * @param seed any int; will be used to get several seeds used internally
     */
    public void restart(long seed)
    {
        index = 0;
        key0 = Hasher.randomize(seed + Hasher.b3 * bound);
        key1 = Hasher.randomize(seed ^ Hasher.b4 * ~bound);
    }

    public static int round(long data, long seed)
    {
        return (int)Hasher.wow(data + Hasher.b1, seed - Hasher.b2);
//        final int s = seed + data;
//        final int x = (s ^ s >>> 17) * (seed - data + 0x9E3779BB >> 12) - s;
//        return x ^ x >>> 15;
    }

    /**
     * Encodes an index with a 2-round Feistel network. It is possible that someone would want to override this method
     * to use more or less rounds, but there must always be an even number.
     * @param index the index to cipher; must be between 0 and {@link #pow4}, inclusive
     * @return the ciphered index, which might not be less than bound but will be less than or equal to {@link #pow4}
     */
    public int encode(int index)
    {
        // break our index into the left and right half
        int left = (index & leftMask) >>> halfBits;
        int right = (index & rightMask);
        // do 2 Feistel rounds
        int newRight = left ^ (round(right, key0) & rightMask);
        left = right;
        right = newRight;
        newRight = left ^ (round(right, key1) & rightMask);
//        left = right;
//        right = newRight;
//        newRight = left ^ (round(right, key2) & rightMask);
//        left = right;
//        right = newRight;
//        newRight = left ^ (round(right, key3) & rightMask);

        // put the left and right back together to form the encrypted index
        return right << halfBits | newRight;
    }

    /**
     * Fully copies this IntShuffler, including its current index in its sequence and internal seeds.
     * @return an exact copy of this IntShuffler
     */
    public IntShuffler copy() {
        IntShuffler next = new IntShuffler(bound, 0);
        next.index = index;
        next.key0 = key0;
        next.key1 = key1;
        return next;
    }

    public String serializeToString() {
        StringBuilder sb = new StringBuilder("`");
        Base.BASE36.appendSigned(sb, bound);
        sb.append('~');
        Base.BASE36.appendSigned(sb, index);
        sb.append('~');
        Base.BASE36.appendSigned(sb, key0);
        sb.append('~');
        Base.BASE36.appendSigned(sb, key1);
        return sb.append('`').toString();
    }

    public static IntShuffler deserializeFromString(@Nonnull String data) {
        if(data.length() < 9) return null;
        int idx = 0;
        int bound = Base.BASE36.readInt(data, idx + 1, idx = data.indexOf('~', idx + 1));
        int index = Base.BASE36.readInt(data, idx + 1, idx = data.indexOf('~', idx + 1));
        long key0 = Base.BASE36.readLong(data, idx + 1, idx = data.indexOf('~', idx + 1));
        long key1 = Base.BASE36.readLong(data, idx + 1, data.indexOf('`', idx + 1));
        IntShuffler is = new IntShuffler(bound, 0);
        is.index = index;
        is.key0 = key0;
        is.key1 = key1;
        return is;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntShuffler that = (IntShuffler) o;

        if (bound != that.bound) return false;
        if (index != that.index) return false;
        if (key0 != that.key0) return false;
        return key1 == that.key1;
    }

    @Override
    public int hashCode() {
        int result = bound;
        result = 31 * result + index;
        result = 31 * result + (int) (key0 ^ (key0 >>> 32));
        result = 31 * result + (int) (key1 ^ (key1 >>> 32));
        return result;
    }
}
