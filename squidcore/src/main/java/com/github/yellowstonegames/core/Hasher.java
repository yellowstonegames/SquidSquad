/*
 * Copyright (c) 2022 See AUTHORS file.
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

import java.util.Iterator;
import java.util.List;

import static com.github.tommyettinger.ds.support.BitConversion.doubleToRawLongBits;
import static com.github.tommyettinger.ds.support.BitConversion.floatToRawIntBits;

/**
 * 64-bit and 32-bit hashing functions that we can rely on staying the same cross-platform.
 * This uses a family of algorithms all based on Wang Yi's wyhash, but using at most 64-bit
 * math. Wyhash was designed foremost for speed and also general-purpose usability, but not
 * cryptographic security. The functions here pass the stringent SMHasher test battery,
 * including the "bad seeds" test that wyhash itself fails.
 * <br>
 * This provides an object-based API and a static API, where a Hasher object is
 * instantiated with a seed, and the static methods take a seed as their first argument.
 * The hashes this returns are always 0 when given null to hash. Arrays with
 * identical elements of identical types will hash identically. Arrays with identical
 * numerical values but different types will sometimes hash differently. This class
 * always provides 64-bit hashes via hash64() and 32-bit hashes via hash().
 * The hash64() and hash() methods use 64-bit math even when producing
 * 32-bit hashes, for GWT reasons. GWT doesn't have the same behavior as desktop and
 * Android applications when using ints because it treats ints mostly like doubles,
 * sometimes, due to it using JavaScript. If we use mainly longs, though, GWT emulates
 * the longs with a more complex technique behind-the-scenes, that behaves the same on
 * the web as it does on desktop or on a phone. Since Hasher is supposed to be stable
 * cross-platform, this is the way we need to go, despite it being slightly slower.
 * <br>
 * This class also provides static {@link #randomize1(long)} and {@link #randomize2(long)}
 * methods, which are unary hashes (hashes of one item, a number) with variants such as
 * {@link #randomize1Bounded(long, int)} and {@link #randomize2Float(long)}. The randomize1()
 * methods are faster but more sensitive to patterns in their input; they are meant to
 * work well on sequential inputs, like 1, 2, 3, etc. with relatively-short sequences
 * (ideally under a million, but if statistical quality isn't a concern, they can handle any
 * length). The randomize2() methods are more-involved, but should be able to handle most
 * kinds of input pattern across even rather-large sequences (billions) while returning
 * random results. The randomize3() methods are likely complete overkill for many cases, but
 * provide extremely strong randomization for any possible input pattern, using the MX3 unary
 * hash with an extra XOR at the beginning to prevent a fixed point at 0.
 * <br>
 * There are also 192 predefined instances of Hasher that you can either
 * select from the array {@link #predefined} or select by hand, such as {@link #omega}.
 * The predefined instances are named after the 24 greek letters, then the same letters
 * with a trailing underscore, then 72 names of demons from the Ars Goetia, then the
 * names of those demons with trailing underscores. The greek letters are traditional,
 * and the demons are perfectly fitting for video games.
 * <br>
 * This changed from using the earlier "Curlup" algorithm on September 14, 2021, because
 * the latest SMHasher found various test failures with it (they weren't found with an
 * older SMHasher version). It is now much closer to "Yolk" from SquidLib, with some
 * extra updates to pass SMHasher in full. Yolk is just WaterHash with a seed.
 * @author Tommy Ettinger
 */
public class Hasher {
    /**
     * The seed used by all non-static hash() and hash64() methods in this class (the methods that don't take a seed).
     * You can create many different Hasher objects, all with different seeds, and get very different hashes as a result
     * of any calls on them. Because making this field hidden in some way doesn't meaningfully contribute to security,
     * and only makes it harder to use this class, {@code seed} is public (and final, so it can't be accidentally
     * modified, but still can if needed via reflection).
     */
    public final long seed;

    public Hasher() {
        this.seed = 0xC4CEB9FE1A85EC53L;
    }

    /**
     * Initializes this Hasher with the given seed, verbatim; it is recommended to use {@link #randomize2(long)} on the
     * seed if you don't know if it is adequately-random.
     * @param seed a long that will be used to change the output of hash() and hash64() methods on the new Hasher
     */
    public Hasher(long seed) {
        this.seed = seed;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter; state is expected to change between calls to
     * this. It is recommended that you use {@code randomize1(++state)} or {@code randomize1(--state)}
     * to produce a sequence of different numbers, and you may have slightly worse quality with increments or decrements
     * other than 1. All longs are accepted by this method, and all longs can be produced. Passing 0 here does not
     * cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize1(long state) {
        return (state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25;
    }

    /**
     * Mid-quality static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code DiverRNG.randomize(++state)} or
     * {@code DiverRNG.randomize(--state)} to produce a sequence of different numbers, but any increments are allowed
     * (even-number increments won't be able to produce all outputs, but their quality will be fine for the numbers they
     * can produce). All longs are accepted by this method, and all longs can be produced. Passing 0 here does not
     * cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize2(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return (state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23 ^ state >>> 51;
        // older Pelican mixer
//        return (state = ((state = (state ^ (state << 41 | state >>> 23) ^ (state << 17 | state >>> 47) ^ 0xD1B54A32D192ED03L) * 0xAEF17502108EF2D9L) ^ state >>> 43 ^ state >>> 31 ^ state >>> 23) * 0xDB4F0B9175AE2165L) ^ state >>> 28;
    }

    /**
     * Very thorough static randomizing method that takes its state as a parameter; state is expected to change between
     * calls to this. It is suggested that you use {@code randomize3(++state)} or {@code randomize3(--state)}
     * to produce a sequence of different numbers, but any odd-number increment should work well, as could another
     * source of different longs, such as a flawed random number generator. All longs are accepted by this method, and
     * all longs can be produced. Passing 0 here does not cause this to return 0.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @return any long
     */
    public static long randomize3(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return state ^ state >>> 29;
    }

    /**
     * Fast static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is recommended that you
     * use {@code randomize1Bounded(++state, bound)} or {@code randomize1Bounded(--state, bound)} to
     * produce a sequence of different numbers. All longs are accepted
     * by this method, but not all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any
     * odd-number values for bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */
    public static int randomize1Bounded(long state, int bound) {
        return (bound = (int) ((bound * (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Mid-quality static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code randomize2Bounded(++state)} or {@code randomize2Bounded(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomize2Bounded(long state, int bound) {
        state ^= 0xD1B54A32D192ED03L;
        return (bound = (int) ((bound * (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23 ^ state >>> 51) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Very thorough static randomizing method that takes its state as a parameter and limits output to an int between 0
     * (inclusive) and bound (exclusive); state is expected to change between calls to this. It is suggested that you
     * use {@code randomize3Bounded(++state)} or {@code randomize3(--state)} to produce a sequence of
     * different numbers, but any increments are allowed (even-number increments won't be able to produce all outputs,
     * but their quality will be fine for the numbers they can produce). All longs are accepted by this method, but not
     * all ints between 0 and bound are guaranteed to be produced with equal likelihood (for any odd-number values for
     * bound, this isn't possible for most generators). The bound can be negative.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state any long; subsequent calls should change by an odd number, such as with {@code ++state}
     * @param bound the outer exclusive bound, as an int
     * @return an int between 0 (inclusive) and bound (exclusive)
     */

    public static int randomize3Bounded(long state, int bound) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (bound = (int) ((bound * ((state ^ state >>> 29) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize1Float(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 30 possible floats between 0 and 1, and this can
     * only return 2 to the 24 of them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize1Float(++state)} is recommended to go forwards or
     *              {@code randomize1Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize1Float(long state) {
        return ((((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize2Float(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1, and this can only return 2 to the 24 of
     * them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize2Float(++state)} is recommended to go forwards or
     *              {@code randomize2Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize2Float(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return ((((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random float that is deterministic based on state; if state is the same on two calls to this, this will
     * return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize3Float(++state)}, where the increment for state can be any value and should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by any odd
     * number, but there are only 2 to the 30 possible floats between 0 and 1, and this can only return 2 to the 24 of
     * them (a requirement for the returned values to be uniform).
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize3Float(++state)} is recommended to go forwards or
     *              {@code randomize3Float(--state)} to generate numbers in reverse order
     * @return a pseudo-random float between 0f (inclusive) and 1f (exclusive), determined by {@code state}
     */
    public static float randomize3Float(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (state >>> 40) * 0x1p-24f;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize1Double(++state)}, where the increment for state should generally be 1. The period is 2 to the 64
     * if you increment or decrement by 1, but there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize1Double(++state)} is recommended to go forwards or
     *              {@code randomize1Double(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize1Double(long state) {
        return (((state = ((state = (((state * 0x632BE59BD9B4E019L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L)) ^ state >>> 27) * 0xAEF17502108EF2D9L) ^ state >>> 25) & 0x1FFFFFFFFFFFFFL) * 0x1p-53;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize2Double(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomizeDouble(++state)} is recommended to go forwards or
     *              {@code randomizeDouble(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize2Double(long state) {
        state ^= 0xD1B54A32D192ED03L;
        return (((state = ((state = (state ^ (state << 39 | state >>> 25) ^ (state << 17 | state >>> 47)) * 0x9E6C63D0676A9A99L) ^ state >>> 23 ^ state >>> 51) * 0x9E6D62D06F6A9A9BL) ^ state >>> 23) >>> 11) * 0x1p-53;
    }

    /**
     * Returns a random double that is deterministic based on state; if state is the same on two calls to this, this
     * will return the same float. This is expected to be called with a changing variable, e.g.
     * {@code randomize3Double(++state)}, where the increment for state can be any number but should usually be odd
     * (even-number increments reduce the period). The period is 2 to the 64 if you increment or decrement by 1, but
     * there are only 2 to the 62 possible doubles between 0 and 1.
     * <br>
     * You have a choice between different randomize strengths in this class. {@code randomize1()} is simpler, and will
     * behave well when the inputs are sequential, while {@code randomize2()} is a completely different algorithm, Pelle
     * Evensen's <a href="https://mostlymangling.blogspot.com/2020/01/nasam-not-another-strange-acronym-mixer.html">xNASAM</a>;
     * it will have excellent quality for most patterns in input but will be about 30% slower than
     * {@code randomize1()}, though this is rarely detectable. {@code randomize3()} is the slowest and most robust; it
     * uses MX3 by Jon Maiga, which the aforementioned author of xNASAM now recommends for any unary hashing. All
     * randomizeN methods will produce all long outputs if given all possible longs as input. Technically-speaking,
     * {@code randomize1(long)}, {@code randomize2(long)}, and {@code randomize3(long)} are bijective functions, which
     * means they are reversible; it is, however, somewhat harder to reverse the xor-rotate-xor-rotate stage used in
     * randomize2() (reversing randomize3() is easy, but takes more steps), and the methods that produce any output
     * other than a full-range long are not reversible (such as {@link #randomize1Bounded(long, int)} and
     * {@link #randomize2Double(long)}).
     *
     * @param state a variable that should be different every time you want a different random result;
     *              using {@code randomize3Double(++state)} is recommended to go forwards or
     *              {@code randomize3Double(--state)} to generate numbers in reverse order
     * @return a pseudo-random double between 0.0 (inclusive) and 1.0 (exclusive), determined by {@code state}
     */
    public static double randomize3Double(long state) {
        state ^= 0xABC98388FB8FAC03L;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 29;
        state *= 0xBEA225F9EB34556DL;
        state ^= state >>> 32;
        state *= 0xBEA225F9EB34556DL;
        return (state >>> 11 ^ state >>> 40) * 0x1p-53;
    }

    public Hasher(final CharSequence seed) {
        this(randomize2(hash64(1L, seed)));
    }

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
     *
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
     * This was changed so that it distributes bits from both inputs a little better on July 6, 2019.
     *
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    public static final Hasher alpha = new Hasher("alpha"), beta = new Hasher("beta"), gamma = new Hasher("gamma"),
            delta = new Hasher("delta"), epsilon = new Hasher("epsilon"), zeta = new Hasher("zeta"),
            eta = new Hasher("eta"), theta = new Hasher("theta"), iota = new Hasher("iota"),
            kappa = new Hasher("kappa"), lambda = new Hasher("lambda"), mu = new Hasher("mu"),
            nu = new Hasher("nu"), xi = new Hasher("xi"), omicron = new Hasher("omicron"), pi = new Hasher("pi"),
            rho = new Hasher("rho"), sigma = new Hasher("sigma"), tau = new Hasher("tau"),
            upsilon = new Hasher("upsilon"), phi = new Hasher("phi"), chi = new Hasher("chi"), psi = new Hasher("psi"),
            omega = new Hasher("omega"),
            alpha_ = new Hasher("ALPHA"), beta_ = new Hasher("BETA"), gamma_ = new Hasher("GAMMA"),
            delta_ = new Hasher("DELTA"), epsilon_ = new Hasher("EPSILON"), zeta_ = new Hasher("ZETA"),
            eta_ = new Hasher("ETA"), theta_ = new Hasher("THETA"), iota_ = new Hasher("IOTA"),
            kappa_ = new Hasher("KAPPA"), lambda_ = new Hasher("LAMBDA"), mu_ = new Hasher("MU"),
            nu_ = new Hasher("NU"), xi_ = new Hasher("XI"), omicron_ = new Hasher("OMICRON"), pi_ = new Hasher("PI"),
            rho_ = new Hasher("RHO"), sigma_ = new Hasher("SIGMA"), tau_ = new Hasher("TAU"),
            upsilon_ = new Hasher("UPSILON"), phi_ = new Hasher("PHI"), chi_ = new Hasher("CHI"), psi_ = new Hasher("PSI"),
            omega_ = new Hasher("OMEGA"),
            baal = new Hasher("baal"), agares = new Hasher("agares"), vassago = new Hasher("vassago"), samigina = new Hasher("samigina"),
            marbas = new Hasher("marbas"), valefor = new Hasher("valefor"), amon = new Hasher("amon"), barbatos = new Hasher("barbatos"),
            paimon = new Hasher("paimon"), buer = new Hasher("buer"), gusion = new Hasher("gusion"), sitri = new Hasher("sitri"),
            beleth = new Hasher("beleth"), leraje = new Hasher("leraje"), eligos = new Hasher("eligos"), zepar = new Hasher("zepar"),
            botis = new Hasher("botis"), bathin = new Hasher("bathin"), sallos = new Hasher("sallos"), purson = new Hasher("purson"),
            marax = new Hasher("marax"), ipos = new Hasher("ipos"), aim = new Hasher("aim"), naberius = new Hasher("naberius"),
            glasya_labolas = new Hasher("glasya_labolas"), bune = new Hasher("bune"), ronove = new Hasher("ronove"), berith = new Hasher("berith"),
            astaroth = new Hasher("astaroth"), forneus = new Hasher("forneus"), foras = new Hasher("foras"), asmoday = new Hasher("asmoday"),
            gaap = new Hasher("gaap"), furfur = new Hasher("furfur"), marchosias = new Hasher("marchosias"), stolas = new Hasher("stolas"),
            phenex = new Hasher("phenex"), halphas = new Hasher("halphas"), malphas = new Hasher("malphas"), raum = new Hasher("raum"),
            focalor = new Hasher("focalor"), vepar = new Hasher("vepar"), sabnock = new Hasher("sabnock"), shax = new Hasher("shax"),
            vine = new Hasher("vine"), bifrons = new Hasher("bifrons"), vual = new Hasher("vual"), haagenti = new Hasher("haagenti"),
            crocell = new Hasher("crocell"), furcas = new Hasher("furcas"), balam = new Hasher("balam"), alloces = new Hasher("alloces"),
            caim = new Hasher("caim"), murmur = new Hasher("murmur"), orobas = new Hasher("orobas"), gremory = new Hasher("gremory"),
            ose = new Hasher("ose"), amy = new Hasher("amy"), orias = new Hasher("orias"), vapula = new Hasher("vapula"),
            zagan = new Hasher("zagan"), valac = new Hasher("valac"), andras = new Hasher("andras"), flauros = new Hasher("flauros"),
            andrealphus = new Hasher("andrealphus"), kimaris = new Hasher("kimaris"), amdusias = new Hasher("amdusias"), belial = new Hasher("belial"),
            decarabia = new Hasher("decarabia"), seere = new Hasher("seere"), dantalion = new Hasher("dantalion"), andromalius = new Hasher("andromalius"),
            baal_ = new Hasher("BAAL"), agares_ = new Hasher("AGARES"), vassago_ = new Hasher("VASSAGO"), samigina_ = new Hasher("SAMIGINA"),
            marbas_ = new Hasher("MARBAS"), valefor_ = new Hasher("VALEFOR"), amon_ = new Hasher("AMON"), barbatos_ = new Hasher("BARBATOS"),
            paimon_ = new Hasher("PAIMON"), buer_ = new Hasher("BUER"), gusion_ = new Hasher("GUSION"), sitri_ = new Hasher("SITRI"),
            beleth_ = new Hasher("BELETH"), leraje_ = new Hasher("LERAJE"), eligos_ = new Hasher("ELIGOS"), zepar_ = new Hasher("ZEPAR"),
            botis_ = new Hasher("BOTIS"), bathin_ = new Hasher("BATHIN"), sallos_ = new Hasher("SALLOS"), purson_ = new Hasher("PURSON"),
            marax_ = new Hasher("MARAX"), ipos_ = new Hasher("IPOS"), aim_ = new Hasher("AIM"), naberius_ = new Hasher("NABERIUS"),
            glasya_labolas_ = new Hasher("GLASYA_LABOLAS"), bune_ = new Hasher("BUNE"), ronove_ = new Hasher("RONOVE"), berith_ = new Hasher("BERITH"),
            astaroth_ = new Hasher("ASTAROTH"), forneus_ = new Hasher("FORNEUS"), foras_ = new Hasher("FORAS"), asmoday_ = new Hasher("ASMODAY"),
            gaap_ = new Hasher("GAAP"), furfur_ = new Hasher("FURFUR"), marchosias_ = new Hasher("MARCHOSIAS"), stolas_ = new Hasher("STOLAS"),
            phenex_ = new Hasher("PHENEX"), halphas_ = new Hasher("HALPHAS"), malphas_ = new Hasher("MALPHAS"), raum_ = new Hasher("RAUM"),
            focalor_ = new Hasher("FOCALOR"), vepar_ = new Hasher("VEPAR"), sabnock_ = new Hasher("SABNOCK"), shax_ = new Hasher("SHAX"),
            vine_ = new Hasher("VINE"), bifrons_ = new Hasher("BIFRONS"), vual_ = new Hasher("VUAL"), haagenti_ = new Hasher("HAAGENTI"),
            crocell_ = new Hasher("CROCELL"), furcas_ = new Hasher("FURCAS"), balam_ = new Hasher("BALAM"), alloces_ = new Hasher("ALLOCES"),
            caim_ = new Hasher("CAIM"), murmur_ = new Hasher("MURMUR"), orobas_ = new Hasher("OROBAS"), gremory_ = new Hasher("GREMORY"),
            ose_ = new Hasher("OSE"), amy_ = new Hasher("AMY"), orias_ = new Hasher("ORIAS"), vapula_ = new Hasher("VAPULA"),
            zagan_ = new Hasher("ZAGAN"), valac_ = new Hasher("VALAC"), andras_ = new Hasher("ANDRAS"), flauros_ = new Hasher("FLAUROS"),
            andrealphus_ = new Hasher("ANDREALPHUS"), kimaris_ = new Hasher("KIMARIS"), amdusias_ = new Hasher("AMDUSIAS"), belial_ = new Hasher("BELIAL"),
            decarabia_ = new Hasher("DECARABIA"), seere_ = new Hasher("SEERE"), dantalion_ = new Hasher("DANTALION"), andromalius_ = new Hasher("ANDROMALIUS");
    /**
     * Has a length of 192, which may be relevant if automatically choosing a predefined hash functor.
     */
    public static final Hasher[] predefined = new Hasher[]{alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota,
            kappa, lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega,
            alpha_, beta_, gamma_, delta_, epsilon_, zeta_, eta_, theta_, iota_,
            kappa_, lambda_, mu_, nu_, xi_, omicron_, pi_, rho_, sigma_, tau_, upsilon_, phi_, chi_, psi_, omega_,
            baal, agares, vassago, samigina, marbas, valefor, amon, barbatos,
            paimon, buer, gusion, sitri, beleth, leraje, eligos, zepar,
            botis, bathin, sallos, purson, marax, ipos, aim, naberius,
            glasya_labolas, bune, ronove, berith, astaroth, forneus, foras, asmoday,
            gaap, furfur, marchosias, stolas, phenex, halphas, malphas, raum,
            focalor, vepar, sabnock, shax, vine, bifrons, vual, haagenti,
            crocell, furcas, balam, alloces, caim, murmur, orobas, gremory,
            ose, amy, orias, vapula, zagan, valac, andras, flauros,
            andrealphus, kimaris, amdusias, belial, decarabia, seere, dantalion, andromalius,
            baal_, agares_, vassago_, samigina_, marbas_, valefor_, amon_, barbatos_,
            paimon_, buer_, gusion_, sitri_, beleth_, leraje_, eligos_, zepar_,
            botis_, bathin_, sallos_, purson_, marax_, ipos_, aim_, naberius_,
            glasya_labolas_, bune_, ronove_, berith_, astaroth_, forneus_, foras_, asmoday_,
            gaap_, furfur_, marchosias_, stolas_, phenex_, halphas_, malphas_, raum_,
            focalor_, vepar_, sabnock_, shax_, vine_, bifrons_, vual_, haagenti_,
            crocell_, furcas_, balam_, alloces_, caim_, murmur_, orobas_, gremory_,
            ose_, amy_, orias_, vapula_, zagan_, valac_, andras_, flauros_,
            andrealphus_, kimaris_, amdusias_, belial_, decarabia_, seere_, dantalion_, andromalius_};

    public long hash64(final boolean[] data) {
        if (data == null) return 0;
        long seed = this.seed;//seed = b1 ^ b1 >>> 29 ^ b1 >>> 43 ^ b1 << 7 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public long hash64(final byte[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final short[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final char[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[] data, final int length) {
        if (data == null) return 0;
        long seed = this.seed;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (length ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final long[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= data[i-3] * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= data[i-2] * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= data[i-1] * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= data[i  ] * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 ^ data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) + wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }
    public long hash64(final float[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i-3]) ^ b1, floatToRawIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i-1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToRawIntBits(data[len-1]) >>> 16), b3 ^ (floatToRawIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToRawIntBits(data[len-2]), b0 ^ floatToRawIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToRawIntBits(data[len-3]), b2 ^ floatToRawIntBits(data[len-2])) ^ mum(seed ^ floatToRawIntBits(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public long hash64(final double[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= doubleToRawLongBits(data[i-3]) * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i-2]) * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i-1]) * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i  ]) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ doubleToRawLongBits(data[len-1])); break;
            case 2: seed = wow(seed + doubleToRawLongBits(data[len-2]), b2 ^ doubleToRawLongBits(data[len-1])); break;
            case 3: seed = wow(seed + doubleToRawLongBits(data[len-3]), b2 + doubleToRawLongBits(data[len-2])) + wow(seed + doubleToRawLongBits(data[len-1]), seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        return mum(seed ^ seed << 16, len - start ^ b0);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 64-bit hash code for the requested section of data
     */
    public long hash64(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        return mum(seed ^ seed << 16, len - start ^ b0);
    }


    public long hash64(final char[][] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final int[][] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final long[][] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final CharSequence[]... data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data.get(i-3)) ^ b1, hash(data.get(i-2)) ^ b2) + seed,
                    mum(hash(data.get(i-1)) ^ b3, hash(data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data.get(len-2)), b0 ^ hash(data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(data.get(len-3)), b2 ^ hash(data.get(len-2))) ^ mum(seed ^ hash(data.get(len-1)), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);

    }

    public long hash64(final Object[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public long hash64(final Object data) {
        if (data == null)
            return 0;
        final long h = (data.hashCode() + seed) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }

    public int hash(final boolean[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public int hash(final byte[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final short[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final char[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
                seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final CharSequence data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public int hash(final int[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public int hash(final int[] data, final int length) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (length ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final long[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= data[i-3] * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= data[i-2] * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= data[i-1] * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= data[i  ] * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 ^ data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) + wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int)(seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public int hash(final float[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i-3]) ^ b1, floatToRawIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i-1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToRawIntBits(data[len-1]) >>> 16), b3 ^ (floatToRawIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToRawIntBits(data[len-2]), b0 ^ floatToRawIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToRawIntBits(data[len-3]), b2 ^ floatToRawIntBits(data[len-2])) ^ mum(seed ^ floatToRawIntBits(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public int hash(final double[] data) {
        if (data == null) return 0;
        long seed = this.seed, a = this.seed + b4, b = this.seed + b3, c = this.seed + b2, d = this.seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= doubleToRawLongBits(data[i-3]) * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i-2]) * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i-1]) * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i  ]) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ doubleToRawLongBits(data[len-1])); break;
            case 2: seed = wow(seed + doubleToRawLongBits(data[len-2]), b2 ^ doubleToRawLongBits(data[len-1])); break;
            case 3: seed = wow(seed + doubleToRawLongBits(data[len-3]), b2 + doubleToRawLongBits(data[len-2])) + wow(seed + doubleToRawLongBits(data[len-1]), seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int)(seed ^ seed >>> 23 ^ seed >>> 42);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public int hash(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }


    public int hash(final char[][] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final int[][] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final long[][] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final CharSequence[] data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final CharSequence[]... data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
                seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = this.seed;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data.get(i-3)) ^ b1, hash(data.get(i-2)) ^ b2) + seed,
                    mum(hash(data.get(i-1)) ^ b3, hash(data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data.get(len-2)), b0 ^ hash(data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(data.get(len-3)), b2 ^ hash(data.get(len-2))) ^ mum(seed ^ hash(data.get(len-1)), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final Object[] data) {
        if (data == null) return 0;
        long seed = this.seed;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public int hash(final Object data) {
        if (data == null) return 0;
        return (int)((data.hashCode() + seed) * 0x9E3779B97F4A7C15L >>> 32);
    }


    public static long hash64(long seed, final boolean[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public static long hash64(long seed, final byte[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final short[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final char[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[] data, final int length) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (length ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final long[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= data[i-3] * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= data[i-2] * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= data[i-1] * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= data[i  ] * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 ^ data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) + wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }
    public static long hash64(long seed, final float[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i-3]) ^ b1, floatToRawIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i-1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToRawIntBits(data[len-1]) >>> 16), b3 ^ (floatToRawIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToRawIntBits(data[len-2]), b0 ^ floatToRawIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToRawIntBits(data[len-3]), b2 ^ floatToRawIntBits(data[len-2])) ^ mum(seed ^ floatToRawIntBits(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public static long hash64(long seed, final double[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= doubleToRawLongBits(data[i-3]) * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i-2]) * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i-1]) * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i  ]) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ doubleToRawLongBits(data[len-1])); break;
            case 2: seed = wow(seed + doubleToRawLongBits(data[len-2]), b2 ^ doubleToRawLongBits(data[len-1])); break;
            case 3: seed = wow(seed + doubleToRawLongBits(data[len-3]), b2 + doubleToRawLongBits(data[len-2])) + wow(seed + doubleToRawLongBits(data[len-1]), seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return seed ^ seed >>> 23 ^ seed >>> 42;
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(long seed, final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(long seed, final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }


    public static long hash64(long seed, final char[][] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final int[][] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final long[][] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final CharSequence[]... data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(seed, it.next()) ^ b1, (it.hasNext() ? hash(seed, it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data.get(i-3)) ^ b1, hash(seed, data.get(i-2)) ^ b2) + seed,
                    mum(hash(seed, data.get(i-1)) ^ b3, hash(seed, data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data.get(len-2)), b0 ^ hash(seed, data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(seed, data.get(len-3)), b2 ^ hash(seed, data.get(len-2))) ^ mum(seed ^ hash(seed, data.get(len-1)), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);

    }

    public static long hash64(long seed, final Object[] data) {
        if (data == null) return 0L;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(long seed, final Object data) {
        if (data == null)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final long h = (data.hashCode() + seed) * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }


    public static int hash(long seed, final boolean[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public static int hash(long seed, final byte[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final short[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final char[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public static int hash(long seed, final int[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public static int hash(long seed, final int[] data, final int length) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (length ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final long[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= data[i-3] * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= data[i-2] * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= data[i-1] * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= data[i  ] * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 ^ data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) + wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int)(seed ^ seed >>> 23 ^ seed >>> 42);
    }

    public static int hash(long seed, final float[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToRawIntBits(data[i-3]) ^ b1, floatToRawIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToRawIntBits(data[i-1]) ^ b3, floatToRawIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToRawIntBits(data[len-1]) >>> 16), b3 ^ (floatToRawIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToRawIntBits(data[len-2]), b0 ^ floatToRawIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToRawIntBits(data[len-3]), b2 ^ floatToRawIntBits(data[len-2])) ^ mum(seed ^ floatToRawIntBits(data[len-1]), b4); break;
        }
                seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }
    public static int hash(long seed, final double[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        long a = seed + b4, b = seed + b3, c = seed + b2, d = seed + b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a ^= doubleToRawLongBits(data[i-3]) * b1; a = (a << 23 | a >>> 41) * b3;
            b ^= doubleToRawLongBits(data[i-2]) * b2; b = (b << 25 | b >>> 39) * b4;
            c ^= doubleToRawLongBits(data[i-1]) * b3; c = (c << 29 | c >>> 35) * b5;
            d ^= doubleToRawLongBits(data[i  ]) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ doubleToRawLongBits(data[len-1])); break;
            case 2: seed = wow(seed + doubleToRawLongBits(data[len-2]), b2 ^ doubleToRawLongBits(data[len-1])); break;
            case 3: seed = wow(seed + doubleToRawLongBits(data[len-3]), b2 + doubleToRawLongBits(data[len-2])) + wow(seed + doubleToRawLongBits(data[len-1]), seed ^ b3); break;
        }
        seed = (seed ^ seed >>> 16) * (b0 ^ (len + seed) << 4);
        return (int)(seed ^ seed >>> 23 ^ seed >>> 42);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(long seed, final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], data[len-1] ^ b0); break;
            case 3: seed = mum(seed ^ data[len-3], data[len-2] ^ b2) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(long seed, final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed, b3 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), data.charAt(len-1) ^ b0); break;
            case 3: seed = mum(seed ^ data.charAt(len-3), data.charAt(len-2) ^ b2) ^ mum(seed ^ data.charAt(len-1), b4); break;
        }
        seed = (seed ^ seed << 16) * (len - start ^ b0);
        return (int)(seed - (seed >>> 32));
    }


    public static int hash(long seed, final char[][] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final int[][] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final long[][] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final CharSequence[]... data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(seed, it.next()) ^ b1, (it.hasNext() ? hash(seed, it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(seed, it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(seed, it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final List<? extends CharSequence> data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data.get(i-3)) ^ b1, hash(seed, data.get(i-2)) ^ b2) + seed,
                    mum(hash(seed, data.get(i-1)) ^ b3, hash(seed, data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data.get(len-2)), b0 ^ hash(seed, data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(seed, data.get(len-3)), b2 ^ hash(seed, data.get(len-2))) ^ mum(seed ^ hash(seed, data.get(len-1)), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final Object[] data) {
        if (data == null) return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(seed, data[i-3]) ^ b1, hash(seed, data[i-2]) ^ b2) + seed,
                    mum(hash(seed, data[i-1]) ^ b3, hash(seed, data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(seed, data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(seed, data[len-2]), b0 ^ hash(seed, data[len-1])); break;
            case 3: seed = mum(seed ^ hash(seed, data[len-3]), b2 ^ hash(seed, data[len-2])) ^ mum(seed ^ hash(seed, data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(long seed, final Object data) {
        if (data == null)
            return 0;
        seed += b1; seed ^= seed >>> 23 ^ seed >>> 48 ^ seed << 7 ^ seed << 53;
        return (int)((data.hashCode() + seed) * 0x9E3779B97F4A7C15L >>> 32);
    }
}
