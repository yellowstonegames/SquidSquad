/* Copyright (c) 2012 Kevin L. Stern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yellowstonegames.text;

import com.github.tommyettinger.ds.IntIntMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Estimates how different two String inputs are. This can be configured with
 * different costs for inserting, deleting, replacing, or swapping characters.
 * <br>
 * Uses the Damerau-Levenshtein Algorithm, an extension to the Levenshtein
 * Algorithm which solves the edit distance problem between a source string and
 * a target string with the following operations:
 *
 * <ul>
 * <li>Character Insertion</li>
 * <li>Character Deletion</li>
 * <li>Character Replacement</li>
 * <li>Adjacent Character Swap</li>
 * </ul>
 *
 * Note that the adjacent character swap operation is an edit that may be
 * applied when two adjacent characters in the source string match two adjacent
 * characters in the target string, but in reverse order, rather than a general
 * allowance for adjacent character swaps.
 * <br>
 * This implementation allows the client to specify the costs of the various
 * edit operations with the restriction that the cost of two swap operations
 * must not be less than the cost of a delete operation followed by an insert
 * operation. This restriction is required to preclude two swaps involving the
 * same character being required for optimality which, in turn, enables a fast
 * dynamic programming solution.
 * <br>
 * The running time of the Damerau-Levenshtein algorithm is O(n*m) where n is
 * the length of the source string and m is the length of the target string.
 * This implementation consumes O(n*m) space on the first call to
 * {@link #distance(CharSequence, CharSequence)}, but won't always consume more
 * space than that - the total this uses is O(n*m) for the largest value of n*m
 * requested on any call to distance() on the same instance.
 *
 * @author Kevin L. Stern
 */
public class StringDistance {
    /**
     * The cost to delete one char from anywhere in source on the way to target.
     * The following must be true: {@code swapCost * 2 >= insertCost + deleteCost}
     */
    public final int deleteCost;
    /**
     * The cost to insert one char into anywhere in source on the way to target.
     * The following must be true: {@code swapCost * 2 >= insertCost + deleteCost}
     */
    public final int insertCost;
    /**
     * The cost to change one char in source to any other at the same position, on the way to target.
     */
    public final int replaceCost;
    /**
     * The cost to switch the positions of two adjacent chars in source on the way to target.
     * The following must be true: {@code swapCost * 2 >= insertCost + deleteCost}
     */
    public final int swapCost;
    private int[] buffer = new int[64];
    private final IntIntMap sourceIndexByCharacter = new IntIntMap(26);

    /**
     * Default constructor; sets all costs to 1.
     */
    public StringDistance() {
        deleteCost = 1;
        insertCost = 1;
        replaceCost = 1;
        swapCost = 1;
    }

    /**
     * Used to customize the costs of different modifications.
     *
     * @param deleteCost  the cost of deleting a character.
     * @param insertCost  the cost of inserting a character.
     * @param replaceCost the cost of replacing a character.
     * @param swapCost    the cost of swapping two adjacent characters. The following must be true: {@code swapCost * 2 >= insertCost + deleteCost}
     */
    public StringDistance(int deleteCost, int insertCost,
                          int replaceCost, int swapCost) {
        /*
         * Required to facilitate the premise to the algorithm that two swaps of the
         * same character are never required for optimality.
         */
        if (2 * swapCost < insertCost + deleteCost) {
            throw new IllegalArgumentException("Unsupported cost assignment");
        }
        this.deleteCost = deleteCost;
        this.insertCost = insertCost;
        this.replaceCost = replaceCost;
        this.swapCost = swapCost;
    }

    /**
     * Compute the Damerau-Levenshtein distance between the specified source
     * string and the specified target string.
     *
     * @param source the starting String; insertion, deletion, etc. are considered as if applied to this
     * @param target the goal String; we are measuring the edit distance to get from source to target
     * @return the edit distance from source to target with the configured costs, as an int
     */
    public int distance(@NotNull CharSequence source, @NotNull CharSequence target) {
        if (source.length() == 0) {
            return target.length() * insertCost;
        }
        if (target.length() == 0) {
            return source.length() * deleteCost;
        }
        int width = target.length();
        int height = source.length();
        int total = width * height;
        if (buffer.length < total) {
            buffer = new int[total];
        } else {
            Arrays.fill(buffer, 0, total, 0);
        }
        int[] table = buffer;
        sourceIndexByCharacter.clear();
        if (source.charAt(0) != target.charAt(0)) {
            table[0] = Math.min(replaceCost, deleteCost + insertCost);
        }
        sourceIndexByCharacter.put(source.charAt(0), 0);
        for (int i = 1; i < source.length(); i++) {
            int deleteDistance = table[(i - 1) * width] + deleteCost;
            int insertDistance = (i + 1) * deleteCost + insertCost;
            int matchDistance = i * deleteCost
                    + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
            table[i * width] = Math.min(Math.min(deleteDistance, insertDistance),
                    matchDistance);
        }
        for (int j = 1; j < target.length(); j++) {
            int deleteDistance = (j + 1) * insertCost + deleteCost;
            int insertDistance = table[j - 1] + insertCost;
            int matchDistance = j * insertCost
                    + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
            table[j] = Math.min(Math.min(deleteDistance, insertDistance),
                    matchDistance);
        }
        for (int i = 1; i < source.length(); i++) {
            int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0
                    : -1;
            for (int j = 1; j < target.length(); j++) {
                int candidateSwapIndex = sourceIndexByCharacter.getOrDefault(target
                        .charAt(j), -1);
                int jSwap = maxSourceLetterMatchIndex;
                int deleteDistance = table[(i - 1) * width + j] + deleteCost;
                int insertDistance = table[i * width + j - 1] + insertCost;
                int matchDistance = table[(i - 1) * width + j - 1];
                if (source.charAt(i) != target.charAt(j)) {
                    matchDistance += replaceCost;
                } else {
                    maxSourceLetterMatchIndex = j;
                }
                int swapDistance;
                if (candidateSwapIndex != -1 && jSwap != -1) {
                    int preSwapCost;
                    if (candidateSwapIndex == 0 && jSwap == 0) {
                        preSwapCost = 0;
                    } else {
                        preSwapCost = table[Math.max(0, candidateSwapIndex - 1) * width + Math.max(0, jSwap - 1)];
                    }
                    swapDistance = preSwapCost + (i - candidateSwapIndex - 1) * deleteCost
                            + (j - jSwap - 1) * insertCost + swapCost;
                } else {
                    swapDistance = Integer.MAX_VALUE;
                }
                table[i * width + j] = Math.min(Math.min(Math
                        .min(deleteDistance, insertDistance), matchDistance), swapDistance);
            }
            sourceIndexByCharacter.put(source.charAt(i), i);
        }
        return table[(source.length() - 1) * width + target.length() - 1];
    }
}