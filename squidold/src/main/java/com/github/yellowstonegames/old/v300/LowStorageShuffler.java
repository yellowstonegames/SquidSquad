/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.digital.MathTools;

/**
 * Gets a sequence of distinct pseudo-random ints (typically used as indices) from 0 to some bound, without storing all
 * the sequence in memory. Uses a Feistel network, as described in
 * <a href="https://blog.demofox.org/2013/07/06/fast-lightweight-random-shuffle-functionality-fixed/">this post by Alan Wolfe</a>.
 * The API is very simple; you construct a LowStorageShuffler by specifying how many items it can shuffle, and you can
 * optionally use a seed (it will be random if you don't specify a seed). Call {@link #next()} on a LowStorageShuffler
 * to get the next distinct int in the shuffled ordering; next() will return -1 if there are no more distinct ints (if
 * {@link #bound} items have already been returned). You can go back to the previous item with {@link #previous()},
 * which similarly returns -1 if it can't go earlier in the sequence. You can restart the sequence with
 * {@link #restart()} to use the same sequence over again, or {@link #restart(int)} to use a different seed (the bound
 * is fixed).
 * <br>
 * This differs from the version in Alan Wolfe's example code and blog post; it uses a very different round function,
 * and it only uses 2 rounds of it (instead of 4). Wolfe's round function is MurmurHash2, but as far as I can tell the
 * version he uses doesn't have anything like MurmurHash3's fmix32() to adequately avalanche bits, and since all keys
 * are small keys with the usage of MurmurHash2 in his code, avalanche is the most important thing. It's also perfectly
 * fine to use irreversible operations in a Feistel network round function, and I do that since it seems to improve
 * randomness slightly. The {@link #round(int, int)} method used here acts like an unbalanced, irreversible PRNG with
 * two states, and that turns out to be just fine for a Feistel network. Using 4 rounds turns out to be overkill in this
 * case. This also uses a different seed for each round.
 * <br>
 * Created by Tommy Ettinger on 9/22/2018.
 * @author Alan Wolfe
 * @author Tommy Ettinger
 */
public class LowStorageShuffler {
    public final int bound;
    protected int index, pow4, halfBits, leftMask, rightMask;
    protected int key0, key1;

    /**
     * Constructs a LowStorageShuffler with a random seed and a bound of 10.
     */
    public LowStorageShuffler(){
        this(10);
    }
    /**
     * Constructs a LowStorageShuffler with the given exclusive upper bound and a random seed.
     * @param bound how many distinct ints this can return
     */
    public LowStorageShuffler(int bound)
    {
        this(bound, (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }

    /**
     * Constructs a LowStorageShuffler with the given exclusive upper bound and int seed.
     * @param bound how many distinct ints this can return
     * @param seed any int; will be used to get several seeds used internally
     */
    public LowStorageShuffler(int bound, int seed)
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
     * Constructs a LowStorageShuffler with the given exclusive upper bound and ints k0 and k1.
     * Here, k0 and k1 will be used as the exact keys; this is meant for recreating a LowStorageShuffler,
     * such as for deserialization.
     * @param bound how many distinct ints this can return
     * @param k0 the first key; will be used verbatim
     * @param k1 the second key; will be used verbatim
     */
    public LowStorageShuffler(int bound, int k0, int k1)
    {
        // initialize our state
        this.bound = bound;
        index = 0;
        key0 = k0;
        key1 = k1;
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
     * called (or the LowStorageShuffler has reached the beginning from repeated calls to this method).
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

    /**
     * Used to rearrange the bits of seeds this is given in a way that should partly randomize them.
     * A high-quality 32-bit input, 32-bit output unary hash is pretty hard to find.
     * @param z any int
     * @return a pseudo-random but deterministic change of z
     */
    public static int determine(int z)
    {
        return (z = ((z = ((z = (z ^ 0xC1C64E6D) * 0xDAB) ^ z >>> 13 ^ 0x9E3779B9) * 0x7FFFF) ^ z >>> 12) * 0x1FFFF) ^ z >>> 15;

    }    
    /**
     * Starts the sequence over, but can change the seed (completely changing the sequence). If {@code seed} is the same
     * as the seed given in the constructor, this will use the same sequence, acting like {@link #restart()}.
     * @param seed any int; will be used to get several seeds used internally
     */
    public void restart(int seed)
    {
        index = 0;
        key0 = determine(seed ^ 0xDE4D * ~bound);
        key1 = determine(key0 ^ 0xBA55 * bound);
        key0 ^= determine(~key1 ^ 0xBEEF * bound);
        key1 ^= determine(~key0 ^ 0x1337 * bound);
    }

    public static int round(int data, int seed)
    {
        final int s = seed + data;
        final int x = (s ^ s >>> 17) * (seed - data + 0x9E3779BB >> 12) - s;
        return x ^ x >>> 15;
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

    public int getBound() {
        return bound;
    }

    public int getKey0() {
        return key0;
    }

    public void setKey0(int key0) {
        this.key0 = key0;
    }

    public int getKey1() {
        return key1;
    }

    public void setKey1(int key1) {
        this.key1 = key1;
    }

    public int getIndex() {
        return index;
    }

    public LowStorageShuffler setIndex(int index) {
        this.index = (index & pow4);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LowStorageShuffler that = (LowStorageShuffler) o;

        if (bound != that.bound) return false;
        if (index != that.index) return false;
        if (pow4 != that.pow4) return false;
        if (halfBits != that.halfBits) return false;
        if (leftMask != that.leftMask) return false;
        if (rightMask != that.rightMask) return false;
        if (key0 != that.key0) return false;
        return key1 == that.key1;
    }

    @Override
    public int hashCode() {
        int result = bound;
        result = 31 * result + index;
        result = 31 * result + pow4;
        result = 31 * result + halfBits;
        result = 31 * result + leftMask;
        result = 31 * result + rightMask;
        result = 31 * result + key0;
        result = 31 * result + key1;
        return result;
    }

    @Override
    public String toString() {
        return "LowStorageShuffler{" +
                "bound=" + bound +
                '}';
    }
}
