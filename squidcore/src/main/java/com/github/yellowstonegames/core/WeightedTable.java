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

package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.ds.FloatList;
import com.github.tommyettinger.ds.IntList;

import java.util.Arrays;
import java.util.Random;

/**
 * A different approach to the same task as a probability table, though this only looks up an appropriate index
 * instead of also storing items it can choose; allows positive floats for weights but does not allow nested tables for
 * simplicity. This doesn't store a Random in this class, and instead expects either a Random as a parameter, or a long
 * to be given for each random draw from the table (these long parameters can be random, sequential, or in some other
 * way different every time).
 * <br>
 * Uses <a href="http://www.keithschwarz.com/darts-dice-coins/">Vose's Alias Method</a>, and is based fairly-closely on
 * the code given by Keith Schwarz at that link. Because Vose's Alias Method is remarkably fast (it takes O(1) time to
 * get a random index, and takes O(n) time to construct a WeightedTable instance), this may be useful to consider if you
 * don't need all the features of ProbabilityTable or if you want deeper control over the random aspects of it.
 */
public class WeightedTable {
    protected int[] mixed;
    protected int size;

    /**
     * Constructs a useless WeightedTable that always returns the index 0.
     */
    public WeightedTable()
    {
        this(1);
    }

    /**
     * Constructs a WeightedTable with the given array of weights for each index. The array can also be a varargs for
     * convenience. The weights can be any positive non-zero floats, but should usually not be so large or small that
     * precision loss is risked. Each weight will be used to determine the likelihood of that weight's index being
     * returned by {@link #random(long)}.
     * @param probabilities an array or varargs of positive floats representing the weights for their own indices
     */
    public WeightedTable(float... probabilities) {
        this(probabilities, 0, probabilities.length);
    }

    /**
     * Constructs a WeightedTable with the given array of weights for each index. The array can also be a varargs for
     * convenience. The weights can be any positive non-zero floats, but should usually not be so large or small that
     * precision loss is risked. Each weight will be used to determine the likelihood of that weight's index being
     * returned by {@link #random(long)}.
     * @param probabilities a FloatList of positive floats representing the weights for their own indices
     */
    public WeightedTable(FloatList probabilities) {
        this(probabilities.items, 0, probabilities.size());
    }

    /**
     * Constructs a WeightedTable with the given array of weights for each index. The array can also be a varargs for
     * convenience. The weights can be any positive non-zero floats, but should usually not be so large or small that
     * precision loss is risked. Each weight will be used to determine the likelihood of that weight's index being
     * returned by {@link #random(long)}.
     * @param probabilities an array or varargs of positive floats representing the weights for their own indices
     */
    public WeightedTable(float[] probabilities, int offset, int length) {
        /* Begin by doing basic structural checks on the inputs. */
        if ((size = Math.min(probabilities.length - offset, length)) == 0)
            throw new IllegalArgumentException("Array 'probabilities' given to WeightedTable must be nonempty.");

        mixed = new int[size<<1];

        float sum = 0.0f;

        /* Make a copy of the probabilities array, since we will be making
         * changes to it.
         */
        float[] probs = new float[size];
        for (int i = offset, idx = 0; idx < size; ++i, ++idx) {
            if(probabilities[i] <= 0) continue;
            sum += (probs[idx] = probabilities[i]);
        }
        if(sum <= 0)
            throw new IllegalArgumentException("At least one probability must be positive");
        final float average = sum / size, invAverage = 1.0f / average;

        /* Create two stacks to act as worklists as we populate the tables. */
        IntList small = new IntList(size);
        IntList large = new IntList(size);

        /* Populate the stacks with the input probabilities. */
        for (int i = 0; i < size; ++i) {
            /* If the probability is below the average probability, then we add
             * it to the small list; otherwise we add it to the large list.
             */
            if (probs[i] >= average)
                large.add(i);
            else
                small.add(i);
        }

        /* As a note: in the mathematical specification of the algorithm, we
         * will always exhaust the small list before the big list.  However,
         * due to floating point inaccuracies, this is not necessarily true.
         * Consequently, this inner loop (which tries to pair small and large
         * elements) will have to check that both lists aren't empty.
         */
        while (!small.isEmpty() && !large.isEmpty()) {
            /* Get the index of the small and the large probabilities. */
            int less = small.pop(), less2 = less << 1;
            int more = large.pop();

            /* These probabilities have not yet been scaled up to be such that
             * sum/n is given weight 1.0.  We do this here instead.
             */
            mixed[less2] = (int)(0x7FFFFFFF * (probs[less] * invAverage));
            mixed[less2|1] = more;

            probs[more] += probs[less] - average;

            if (probs[more] >= average)
                large.add(more);
            else
                small.add(more);
        }

        while (!small.isEmpty())
            mixed[small.pop()<<1] = 0x7FFFFFFF;
        while (!large.isEmpty())
            mixed[large.pop()<<1] = 0x7FFFFFFF;
    }

    private WeightedTable(int[] mixed, boolean ignored)
    {
        size = mixed.length >> 1;
        this.mixed = mixed;
    }

    /**
     * Copy constructor; avoids sharing any state between this and the original.
     * @param other another WeightedTable to copy; no state will be shared
     */
    public WeightedTable(WeightedTable other){
        this(Arrays.copyOf(other.mixed, other.mixed.length), true);
    }

    /**
     * Copies this WeightedTable; avoids sharing any state between this and the copy.
     * @return an exact copy of this WeightedTable
     */
    public WeightedTable copy() {
        return new WeightedTable(this);
    }
    /**
     * Gets an index of one of the weights in this WeightedTable, with the choice determined deterministically by the
     * given long, but higher weights will be returned by more possible inputs than lower weights. The state parameter
     * can be from a random source, but this will randomize it again anyway, so it is also fine to just give sequential
     * longs. The important thing is that each state input this is given will produce the same result for this
     * WeightedTable every time, so you should give different state values when you want random-seeming results. You may
     * want to call this like {@code weightedTable.random(++state)}, where state is a long, to ensure the inputs change.
     * This will always return an int between 0 (inclusive) and {@link #size} (exclusive).
     * <br>
     * Internally, this uses a unary hash (a function that converts one number to a random-seeming but deterministic
     * other number) to generate two ints, one used for probability and treated as a 31-bit integer and the other used
     * to determine the chosen column, which is bounded to an arbitrary positive int. It does this with just one
     * randomized 64-bit value, allowing the state parameter to be just one long.
     * <br>
     * You can also use {@link #random(Random)} to avoid handling state directly; this can be faster if using
     * a particularly fast Random implementation such as one from juniper.
     * @param state a long that should be different every time; consider calling with {@code ++state}
     * @return a random-seeming index from 0 to {@link #size} - 1, determined by weights and the given state
     */
    public int random(long state)
    {
        // This uses the MX3 algorithm to generate a random long given sequential states
        state = Hasher.randomize3(state);
        // get a random int (using half the bits of our previously-calculated state) that is less than size
        int column = (int)((size * (state & 0xFFFFFFFFL)) >>> 32);
        // use the other half of the bits of state to get a 31-bit int, compare to probability and choose either the
        // current column or the alias for that column based on that probability
        return ((state >>> 33) <= mixed[column << 1]) ? column : mixed[column << 1 | 1];
    }
    /**
     * Gets an index of one of the weights in this WeightedTable, with the choice determined by the given random number
     * generator, but higher weights will be returned more frequently than lower weights. The rng parameter can
     * be any implementation of Random, and some implementations are likely to be faster than the unary hash
     * used by {@link #random(long)}. This will return an int between 0 (inclusive) and {@link #size} (exclusive).
     * @param rng an Random; its {@link Random#nextLong()} method will be called once
     * @return a random index from 0 to {@link #size} - 1, determined by weights and the given rng
     */
    public int random(final Random rng)
    {
        final long state = rng.nextLong();
        // get a random int (using half the bits of our previously-calculated state) that is less than size
        int column = (int)((size * (state & 0xFFFFFFFFL)) >> 32);
        // use the other half of the bits of state to get a 31-bit int, compare to probability and choose either the
        // current column or the alias for that column based on that probability
        return ((state >>> 33) <= mixed[column << 1]) ? column : mixed[column << 1 | 1];
    }

    public String stringSerialize()
    {
        return Base.SIMPLE64.join(",", mixed);
    }
    public static WeightedTable stringDeserialize(String data)
    {
        if(data == null || data.isEmpty())
            return null;
//        int pos = -1;
//        int count = StringTools.count(data, ',') + 1;
//        int[] mixed = new int[count];
//        for (int i = 0; i < count; i++) {
//            int next = data.indexOf(',', pos+1);
//            if(next == -1) next = data.length();
//            mixed[i] = Base.BASE10.readInt(data, pos+1, pos = next);
//        }
        return new WeightedTable(Base.SIMPLE64.intSplit(data, ","), true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeightedTable that = (WeightedTable) o;

        if (size != that.size) return false;
        return Arrays.equals(mixed, that.mixed);
    }

    @Override
    public int hashCode() {
        return Hasher.hash(size ^ 0xFEDCBA9876543210L, mixed);
    }


    public int getSize() {
        return size;
    }
}
