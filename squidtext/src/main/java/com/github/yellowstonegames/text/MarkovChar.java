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

package com.github.yellowstonegames.text;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.ds.*;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.core.annotations.Beta;
import regexodus.Category;
import regexodus.Matcher;
import regexodus.Pattern;

import java.util.Collection;

/**
 * A simple Markov chain text generator; call {@link #analyze(CharSequence)} once on a large sample text, then you can
 * call {@link #chain(long)} many times to get odd-sounding "remixes" of the sample text, one word at a time. This is
 * meant to allow easy serialization of the necessary data to call chain(); if you can store the {@link #chars} and
 * {@link #processed} arrays in some serialized form, then you can reassign them to the same fields to avoid calling
 * analyze(). One way to do this conveniently is to use {@link #stringSerialize()} after calling analyze() once and to
 * save the resulting String; then, rather than calling analyze() again on future runs, you would call
 * {@link #stringDeserialize(String)} to create the MarkovChar without needing any repeated analysis.
 */
@Beta
public class MarkovChar {

    /**
     * All chars (case-sensitive and only counting chars that are letters in Unicode, plus {@code "'"}) that this
     * encountered during the latest call to {@link #analyze(CharSequence)}. Will be null if
     * {@link #analyze(CharSequence)} was never called.
     */
    public char[] chars;

    /**
     * Map of all pairs of chars encountered to the position in the order they were encountered. Pairs are stored using
     * their 16-bit {@link #chars} indices placed into the most-significant bits for the first word and the
     * least-significant bits for the second word. The size of this IntIntOrderedMap is likely to be larger than the
     * char array {@link #chars}, but should be equal to {@code processed.length}. Will be null if
     * {@link #analyze(CharSequence)} was never called.
     */
    public IntIntMap pairs;
    /**
     * Complicated data that mixes probabilities of chars using their indices in {@link #chars} and the indices of word
     * pairs in {@link #pairs}, generated during the latest call to {@link #analyze(CharSequence)}. This is a jagged 2D
     * array. Will be null if {@link #analyze(CharSequence)} was never called.
     */
    public int[][] processed;

    private static final char INITIAL = '^', END = ' ';

    /**
     * Creates an empty MarkovChar; you should call {@link #analyze(CharSequence)} before doing anything else with this
     * new object.
     */
    public MarkovChar()
    {
    }

    /**
     * This is the main necessary step before using a MarkovChar; you must call this method at some point before you can
     * call any other methods. You can serialize this MarkovChar after calling to avoid needing to call this again on later
     * runs, or even include serialized MarkovChar objects with a game to only need to call this during pre-processing.
     * This method analyzes the pairings of words in a (typically large) corpus text, including some punctuation as part
     * of words and some kinds as their own "words." It only uses one preceding word to determine the subsequent word.
     * When it finishes processing, it stores the results in {@link #chars} and {@link #processed}, which allows other
     * methods to be called (they will throw a {@link NullPointerException} if analyze() hasn't been called).
     * @param corpus a typically-large sample text in the style that should be mimicked
     */
    public void analyze(CharSequence corpus)
    {
        NumberedSet<Character> body = new NumberedSet<>((corpus.length() >> 4) + 5);
        pairs = new IntIntMap(corpus.length() / 5 + 5);
        ObjectList<IntList> working = new ObjectList<>(corpus.length() / 5 + 5);
        working.add(new IntList(128));
        body.add(INITIAL);
        pairs.put(0, 0);
        body.add(END);

        int current, pair = 0, pre = 0, post;
        for (int i = 0; i < corpus.length(); i++) {
            char c = corpus.charAt(i);

            if('\'' != c && !Category.L.contains(c))
                c = END;
//            current = body.addOrIndex(c);
            current = body.indexOfOrDefault(c, -1);
            if(current == -1) {
                current = body.size();
                body.add(c);
            }

            pair = pair << 16 | (current & 0xFFFF);
            if(pair == 1)
                continue;
            pairs.putIfAbsent(pair, pairs.size()); // used putIfAbsent()'s incorrect behavior before
            post = pairs.get(pair);



            if(working.size() != pairs.size())
            {
                working.add(new IntList(16));
            }
            working.get(pre).add(current);
            if(current == 1)
            {
                working.get(post).add(0);
                pair = 0;
                pre = 0;
            }
            else
            {
                pre = post;
            }
        }
        IntList w = working.get(pre), v;
        if(w.isEmpty()) w.add(0);
        final int len = body.size(), pairLen = working.size();
        chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = body.getAt(i);
        }

        processed = new int[pairLen][];
        w = new IntList(128);
        IntList small = new IntList(128);
        IntList large = new IntList(128);
        IntList probabilities = new IntList(128);
        for(int iv = 0; iv < pairLen; iv++ )
        {
            v = working.get(iv);
            w.clear();
            probabilities.clear();
            if(v.isEmpty())
            {
                v.add(1);
            }
            int vv, sum = 0;
            final int vs = v.size();
            OUTER:
            for (int i = 0; i < vs; ++i) {
                vv = v.get(i);
                for (int j = 0; j < w.size(); j++) {
                    if (w.get(j) == vv) {
                        probabilities.plus(j, 0x10000);
                        sum += 0x10000;
                        continue OUTER;
                    }
                }
                w.add(vv);
                probabilities.add(0x10000);
                sum += 0x10000;
            }
            int iAverage = (sum / w.size());

            small.clear();
            large.clear();
            /* Populate the stacks with the input probabilities. */
            for (int i = 0; i < probabilities.size(); i++) {
                /* If the probability is below the average probability, then we add
                 * it to the small list; otherwise we add it to the large list.
                 */
                if (probabilities.get(i) >= iAverage)
                    large.add(i);
                else
                    small.add(i);
            }

            processed[iv] = new int[w.size() * 3];

            while (!small.isEmpty() && !large.isEmpty()) {
                /* Get the index of the small and the large probabilities. */
                int less = small.pop(), less2 = less * 3;
                int more = large.pop();

                /* These probabilities have not yet been scaled up to be such that
                 * sum/n is given weight 1.0.  We do this here instead.
                 */
                processed[iv][less2] = (probabilities.size() * probabilities.get(less)) / (sum >> 16);
                processed[iv][less2+1] = w.get(less);
                processed[iv][less2+2] = w.get(more);
                vv = probabilities.get(less) - iAverage;
                probabilities.plus(more, vv);
                if (probabilities.get(more) >= iAverage)
                    large.add(more);
                else
                    small.add(more);
            }
            int t;
            while (!small.isEmpty())
            {
                processed[iv][(t = small.pop()) * 3] = 0xFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
            while (!large.isEmpty())
            {
                processed[iv][(t = large.pop()) * 3] = 0xFFFF;
                processed[iv][t * 3 + 1] = processed[iv][t * 3 + 2] = w.get(t);
            }
        }
    }

    /**
     * Generate a word-like String based on the previously analyzed corpus text (using {@link #analyze(CharSequence)})
     * that terminates when a non-letter character other than {@code "'"} is encountered, or once
     * the length would be greater than 200 characters without stopping.
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @return a word generated from the analyzed corpus text's char placement
     */
    public String chain(long seed) {
        return chain(seed, 200);
    }

    /**
     * Generate a word-like String based on the previously analyzed corpus text (using {@link #analyze(CharSequence)})
     * that terminates when a non-letter character other than {@code "'"} is encountered, or once
     * the length would be greater than {@code maxLength} characters without stopping.
     * @param seed the seed for the random decisions this makes, as a long; any long can be used
     * @param maxLength the maximum length for the generated String, in number of characters
     * @return a word generated from the analyzed corpus text's char placement
     */
    public String chain(long seed, int maxLength) {
        int before, pair = 0;
        long state;
        StringBuilder sb = new StringBuilder(1000);
        int[] rf;
        while (sb.length() < maxLength) {
            rf = processed[pairs.get(pair)];
            // This is MX3 to generate a random long given sequential states
            state = Hasher.randomize3(++seed);
            // get a random int (using half the bits of our previously-calculated state) that is less than size
            int column = (int) ((rf.length * (state & 0xFFFFFFFFL)) / 0x300000000L) * 3; // divide by 2^32, round down to multiple of 3
            // use the other half of the bits of state to get a double, compare to probability and choose either the
            // current column or the alias for that column based on that probability
            //before = ((state >>> 33) > rf[column]) ? rf[column + 1] : rf[column + 2];
            if((state >>> 48) > rf[column])
                before = rf[column + 1];
            else
                before = rf[column + 2];
            if(before > 1)
            {
                if(sb.length() + 1 < maxLength)
                {
                    sb.append(chars[before]);
                    pair = pair << 16 | (before & 0xFFFF);
                }
                else
                {
                    break;
                }
            }
            else
            {
                break;
            }
        }
        return sb.toString();
    }

    /**
     * Returns a representation of this MarkovChar as a String; use {@link #stringDeserialize(String)} to get a
     * MarkovChar back from this String. The {@link #chars} and {@link #processed} fields must have been given values by
     * either direct assignment, calling {@link #analyze(CharSequence)}, or building this MarkovTest with the
     * aforementioned destringSerialize method. Uses spaces to separate words and a tab to separate the fields.
     * @return a String that can be used to store the analyzed words and frequencies in this MarkovChar
     */
    public String stringSerialize()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(chars);
        sb.append('\t');
        StringTools.appendJoined(sb, ",", pairs.keySet().toArray());
        sb.append('\t');
        return Base.BASE16.appendJoined2D(sb, ",", ",", processed).toString();
    }

    /**
     * Recreates an already-analyzed MarkovChar given a String produced by {@link #stringSerialize()}.
     * @param data a String returned by {@link #stringSerialize()}
     * @return a MarkovChar that is ready to generate text with {@link #chain(long)}
     */
    public static MarkovChar stringDeserialize(String data)
    {
        int split = data.indexOf('\t');
        MarkovChar markov = new MarkovChar();
        markov.chars = data.substring(0, split).toCharArray();
        int[] arr = Base.BASE16.intSplit(data, ",", split+1, split = data.indexOf('\t', split + 1));
        markov.pairs = new IntIntMap(arr, ArrayTools.range(arr.length));
        markov.processed = Base.BASE16.intSplit2D(data, ";", ",", split + 1, data.length());
        return markov;
    }

    /**
     * Copies the char array {@link #chars}, the IntIntMap {@code pairs}, and the 2D jagged int array
     * {@link #processed} into a new MarkovChar. None of the arrays or objects will be equivalent references (this is a
     * deep copy).
     * @return a copy of this MarkovChar
     */
    public MarkovChar copy()
    {
        MarkovChar other = new MarkovChar();
        other.chars = new char[chars.length];
        System.arraycopy(chars, 0, other.chars, 0, chars.length);
        other.pairs = new IntIntMap(pairs);
        other.processed = new int[processed.length][];
        int len;
        for (int i = 0; i < processed.length; i++) {
            other.processed[i] = new int[len = processed[i].length];
            System.arraycopy(processed[i], 0, other.processed[i], 0, len);
        }
        return other;
    }
}
