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

import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

/**
 * Mathematical operations not provided by {@link Math java.lang.Math}.
 * <br>
 * Includes code that was originally part of the
 * <a href="http://maths.uncommons.org/">Uncommon Maths software package</a> as Maths.
 * Also includes code adapted from libGDX as their MathUtils class. There's also
 * {@link #cbrt(float)} by Marc B. Reynolds, building on the legendary fast inverse square root,
 * and a generalized bias/gain function, {@link #barronSpline(float, float, float)}, popularized by Jon Barron.
 * @author Daniel Dyer
 * @author Tommy Ettinger
 * @author Marc B. Reynolds
 * @author Jon Barron
 */
public final class MathTools
{
    private MathTools ()
    {
        // Prevent instantiation.
    }

    /**
     * The {@code float} value that is closer than any other to
     * <i>e</i>, the base of the natural logarithms.
     */
    public static final float E = 2.7182818284590452354f;

    /**
     * Calculate the first argument raised to the power of the second.
     * This method only supports non-negative powers.
     * @param value The number to be raised.
     * @param power The exponent (must be positive).
     * @return {@code value} raised to {@code power}.
     */
    public static long raiseToPower(int value, int power)
    {
        if (power < 0)
        {
            throw new IllegalArgumentException("This method does not support negative powers.");
        }
        long result = 1;
        for (int i = 0; i < power; i++)
        {
            result *= value;
        }
        return result;
    }


    /**
     * Calculate logarithms for arbitrary bases.
     * @param base The base for the logarithm.
     * @param arg The value to calculate the logarithm for.
     * @return The log of {@code arg} in the specified {@code base}.
     */
    public static double log(double base, double arg)
    {
        // Use natural logarithms and change the base.
        return Math.log(arg) / Math.log(base);
    }


    /**
     * Equivalent to libGDX's isEqual() method in MathUtils; this compares two doubles for equality and allows the given
     * tolerance during comparison. An example is {@code 0.3 - 0.2 == 0.1} vs. {@code isEqual(0.3 - 0.2, 0.1, 0.000001)};
     * the first is incorrectly false, while the second is correctly true.
     * @param a the first float to compare
     * @param b the second float to compare
     * @param tolerance the maximum difference between a and b permitted for this to return true, inclusive   
     * @return true if a and b have a difference less than or equal to tolerance, or false otherwise.
     */
    public static boolean isEqual (double a, double b, double tolerance)
    {
        return Math.abs(a - b) <= tolerance;
    }

    /**
     * A float that is meant to be used as the smallest reasonable tolerance for methods like {@link #isEqual(float, float, float)}.
     */
    public static final float FLOAT_ROUNDING_ERROR = 0.000001f;
    /**
     * Equivalent to libGDX's isEqual() method in MathUtils; this compares two floats for equality and allows just enough
     * tolerance to ignore a rounding error. An example is {@code 0.3f - 0.2f == 0.1f} vs. {@code isEqual(0.3f - 0.2f, 0.1f)};
     * the first is incorrectly false, while the second is correctly true.
     * @param a the first float to compare
     * @param b the second float to compare
     * @return true if a and b are equal or extremely close to equal, or false otherwise.
     */
    public static boolean isEqual (float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }
    /**
     * Equivalent to libGDX's isEqual() method in MathUtils; this compares two floats for equality and allows the given
     * tolerance during comparison. An example is {@code 0.3f - 0.2f == 0.1f} vs. {@code isEqual(0.3f - 0.2f, 0.1f, 0.000001f)};
     * the first is incorrectly false, while the second is correctly true.
     * @param a the first float to compare
     * @param b the second float to compare
     * @param tolerance the maximum difference between a and b permitted for this to return true, inclusive   
     * @return true if a and b have a difference less than or equal to tolerance, or false otherwise.
     */
    public static boolean isEqual (float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * <br>
     * Note that it can often be just as easy to directly call the same code this calls, while being slightly friendlier
     * to inlining in large method: {@code Math.min(Math.max(value, min), max)}.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static int clamp(int value, int min, int max)
    {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * <br>
     * Note that it can often be just as easy to directly call the same code this calls, while being slightly friendlier
     * to inlining in large method: {@code Math.min(Math.max(value, min), max)}.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static long clamp(long value, long min, long max)
    {
        return Math.min(Math.max(value, min), max);
    }


    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * <br>
     * Note that it can often be just as easy to directly call the same code this calls, while being slightly friendlier
     * to inlining in large method: {@code Math.min(Math.max(value, min), max)}.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static double clamp(double value, double min, double max)
    {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * If the specified value is not greater than or equal to the specified minimum and
     * less than or equal to the specified maximum, adjust it so that it is.
     * <br>
     * Note that it can often be just as easy to directly call the same code this calls, while being slightly friendlier
     * to inlining in large method: {@code Math.min(Math.max(value, min), max)}.
     * @param value The value to check.
     * @param min The minimum permitted value.
     * @param max The maximum permitted value.
     * @return {@code value} if it is between the specified limits, {@code min} if the value
     * is too low, or {@code max} if the value is too high.
     */
    public static float clamp(float value, float min, float max)
    {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Like the modulo operator {@code %}, but the result will always match the sign of {@code d} instead of {@code op}.
     * @param op the dividend; negative values are permitted and wrap instead of producing negative results
     * @param d the divisor; if this is negative then the result will be negative, otherwise it will be positive
     * @return the remainder of the division of op by d, with a sign matching d
     */
    public static double remainder(final double op, final double d) {
        return (op % d + d) % d;
    }

    /**
     * Determines the greatest common divisor of a pair of natural numbers
     * using the Euclidean algorithm.  This method only works with natural
     * numbers.  If negative integers are passed in, the absolute values will
     * be used.  The return value is always positive.
     * @param a The first value.
     * @param b The second value.
     * @return The greatest common divisor.
     */
    public static long greatestCommonDivisor(long a, long b)
    {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0)
        {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Given any odd int {@code a}, this finds another odd int {@code b} such that {@code a * b == 1}.
     * <br>
     * This is incompatible with GWT, but it should usually only find uses in exploratory code or in tests anyway...
     * It is only incompatible because it tends to rely on multiplication overflow to work.
     * @param a any odd int; note that even numbers do not have inverses modulo 2 to the 32
     * @return the multiplicative inverse of {@code a} modulo 4294967296 (or, 2 to the 32)
     */
    @GwtIncompatible
    public static int modularMultiplicativeInverse(final int a)
    {
        int x = 2 ^ a * 3;
        x *= 2 - a * x;
        x *= 2 - a * x;
        x *= 2 - a * x;
        return x;
    }

    /**
     * Given any odd long {@code a}, this finds another odd long {@code b} such that {@code a * b == 1L}.
     * @param a any odd long; note that even numbers do not have inverses modulo 2 to the 64
     * @return the multiplicative inverse of {@code a} modulo 18446744073709551616 (or, 2 to the 64)
     */
    public static long modularMultiplicativeInverse(final long a)
    {
        long x = 2 ^ a * 3;
        x *= 2 - a * x;
        x *= 2 - a * x;
        x *= 2 - a * x;
        x *= 2 - a * x;
        return x;
    }

    /**
     * Integer square root (using floor), maintaining correct results even for very large {@code long} values. This
     * version treats negative inputs as unsigned and returns positive square roots for them (these are usually large).
     * <br>
     * This is based on <a href="https://github.com/python/cpython/pull/13244">code recently added to Python</a>, but
     * isn't identical. Notably, this doesn't branch except in the for loop, and it handles negative inputs differently.
     * @param n a {@code long} value that will be treated as if unsigned
     * @return the square root of n, rounded down to the next lower {@code long} if the result isn't already a {@code long}
     */
    public static long isqrt(final long n) {
        final int c = 63 - Long.numberOfLeadingZeros(n) >> 1;
        long a = 1, d = 0, e;
        for(int s = 31 & 32 - Integer.numberOfLeadingZeros(c); s > 0;) {
            e = d;
            d = c >>> --s;
            a = (a << d - e - 1) + (n >>> c + c - e - d + 1) / a;
        }
        return a - (n - a * a >>> 63);
    }

    /**
     * An approximation of the cube-root function for float inputs and outputs.
     * This can be about twice as fast as {@link Math#cbrt(double)}. It
     * correctly returns negative results when given negative inputs.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between -512 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">Wikipedia</a> for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds, posted in his
     * <a href="https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/master/src/Posts/ballcube.c#L182-L197">"Stand-alone-junk" repo</a> .
     * @param x any finite float to find the cube root of
     * @return the cube root of x, approximated
     */
    public static float cbrt(float x) {
        int ix = BitConversion.floatToIntBits(x);
        final int sign = ix & 0x80000000;
        ix &= 0x7FFFFFFF;
        final float x0 = x;
        ix = (ix>>>2) + (ix>>>4);
        ix += (ix>>>4);
        ix = ix + (ix>>>8) + 0x2A5137A0 | sign;
        x  = BitConversion.intBitsToFloat(ix);
        x  = 0.33333334f*(2f * x + x0/(x*x));
        x  = 0.33333334f*(2f * x + x0/(x*x));
        return x;
    }

    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     * @param x progress through the spline, from 0 to 1, inclusive
     * @param shape must be greater than or equal to 0; values greater than 1 are "normal interpolations" 
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a float between 0 and 1, inclusive
     */
    public static float barronSpline(final float x, final float shape, final float turning) {
        final float d = turning - x;
        final int f = BitConversion.floatToIntBits(d) >> 31, n = f | 1;
        return ((turning * n - f) * (x + f)) / (Float.MIN_NORMAL - f + (x + shape * d) * n) - f;
    }

    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     * @param x progress through the spline, from 0 to 1, inclusive
     * @param shape must be greater than or equal to 0; values greater than 1 are "normal interpolations" 
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a double between 0 and 1, inclusive
     */
    public static double barronSpline(final double x, final double shape, final double turning) {
        final double d = turning - x;
        final int f = BitConversion.doubleToHighIntBits(d) >> 31, n = f | 1;
        return ((turning * n - f) * (x + f)) / (Double.MIN_NORMAL - f + (x + shape * d) * n) - f;
    }

    /**
     * Like {@link Math#floor}, but returns a long.
     * Doesn't consider "weird doubles" like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as a long
     */
    public static long longFloor(double t) {
        return t >= 0.0 ? (long) t : (long) t - 1L;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns a long.
     * Doesn't consider "weird floats" like INFINITY and NaN.
     *
     * @param t the double to find the floor for
     * @return the floor of t, as a long
     */
    public static long longFloor(float t) {
        return t >= 0f ? (long) t : (long) t - 1L;
    }
    /**
     * Like {@link Math#floor(double)} , but returns an int.
     * Doesn't consider "weird doubles" like INFINITY and NaN.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(double t) {
        return t >= 0.0 ? (int) t : (int) t - 1;
    }
    /**
     * Like {@link Math#floor(double)}, but takes a float and returns an int.
     * Doesn't consider "weird floats" like INFINITY and NaN. This method will only properly floor
     * floats from {@code -16384} to {@code Float.MAX_VALUE - 16384}.
     * <br>
     * Taken from libGDX MathUtils.
     * @param t the float to find the floor for
     * @return the floor of t, as an int
     */
    public static int fastFloor(float t) {
        return ((int)(t + 0x1p14) - 0x4000);
    }
    /**
     * Like {@link Math#ceil(double)}, but returns an int.
     * Doesn't consider "weird doubles" like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(double t) {
        return t >= 0.0 ? -(int) -t + 1: -(int)-t;
    }
    /**
     * Like {@link Math#ceil(double)}, but takes a float and returns an int.
     * Doesn't consider "weird floats" like INFINITY and NaN.
     * @param t the float to find the ceiling for
     * @return the ceiling of t, as an int
     */
    public static int fastCeil(float t) {
        return t >= 0f ? -(int) -t + 1: -(int)-t;
    }

    /**
     * Returns the next higher power of two relative to {@code n}, or n if it is already a power of two. This returns 2
     * if n is any value less than 2 (including negative numbers, but also 1, which is a power of two).
     * @param n the lower bound for the result
     * @return the next higher power of two that is greater than or equal to n
     */
    public static int nextPowerOfTwo(final int n) {
        return 1 << -Integer.numberOfLeadingZeros(Math.max(2, n) - 1);
    }

    /**
     * Forces precision loss on the given float so very small fluctuations away from an integer will be erased.
     * This is meant primarily for cleaning up floats, so they can be presented without needing scientific notation.
     * It leaves about 3 decimal digits after the point intact, and should make any digits after that simply 0.
     * @param n any float, but typically a fairly small one (between -8 and 8, as a guideline)
     * @return {@code n} with its 13 least significant bits effectively removed
     */
    public static float truncate(final float n){
        long i = (long) (n * 0x1p13f); // 0x1p13f is 2 raised to the 13 as a float, or 8192.0f
        return i * 0x1p-13f;           // 0x1p-13f is 1 divided by (2 raised to the 13) as a float, or 1.0f/8192.0f
    }

    /**
     * Forces precision loss on the given double so very small fluctuations away from an integer will be erased.
     * This is meant primarily for cleaning up doubles, so they can be presented without needing scientific notation.
     * It leaves about 3 decimal digits after the point intact, and should make any digits after that simply 0.
     * @param n any double, but typically a fairly small one (between -8 and 8, as a guideline)
     * @return {@code n} with its 42 least significant bits effectively removed
     */
    public static double truncate(final double n){
        long i = (long)(n * 0x1p42); // 0x1p42 is 2 raised to the 42 as a double
        return i * 0x1p-42;          // 0x1p-42 is 1 divided by (2 raised to the 42) as a double
    }

    /**
     *  Linearly interpolates between fromValue to toValue on progress position.
     * @param fromValue starting float value; can be any finite float
     * @param toValue ending float value; can be any finite float
     * @param progress how far the interpolation should go, between 0 (equal to fromValue) and 1 (equal to toValue)
     */
    public static float lerp (final float fromValue, final float toValue, final float progress) {
        return fromValue + (toValue - fromValue) * progress;
    }

    /** Linearly interpolates between two angles in turns. Takes into account that angles wrap at 1.0 and always takes
     * the direction with the smallest delta angle.
     *
     * @param fromTurns start angle in turns
     * @param toTurns target angle in turns
     * @param progress interpolation value in the range [0, 1]
     * @return the interpolated angle in the range [0, 1) */
    public static float lerpAngleTurns(float fromTurns, float toTurns, float progress) {
        float d = toTurns - fromTurns + 0.5f;
        d = fromTurns + progress * (d - fastFloor(d) - 0.5f);
        return d - fastFloor(d);
    }
    /**
     * Limited-use; takes any double and produces a double in the -1.0 to 1.0 range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value. An
     * input of any even number should produce something very close to -1.0, any odd
     * number should produce something very close to 1.0, and any number halfway between two incremental integers (like
     * 8.5 or -10.5) should produce 0.0 or a very small fraction. This method is closely related to
     * {@link #sway(double)}, which will smoothly curve its output to produce more values that are close to -1 or 1.
     * @param value any double
     * @return a double from -1.0 (inclusive) to 1.0 (inclusive)
     */
    public static double zigzag(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any float and produces a float in the -1f to 1f range, with similar inputs producing
     * close to a consistent rate of up and down through the range. This is meant for noise, where it may be useful to
     * limit the amount of change between nearby points' noise values and prevent sudden "jumps" in noise value. An
     * input of any even number should produce something very close to -1f, any odd
     * number should produce something very close to 1f, and any number halfway between two incremental integers (like
     * 8.5f or -10.5f) should produce 0f or a very small fraction. This method is closely related to
     * {@link #sway(float)}, which will smoothly curve its output to produce more values that are close to -1 or 1.
     * @param value any float
     * @return a float from -1f (inclusive) to 1f (inclusive)
     */
    public static float zigzag(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * (floor << 1) - floor;
    }

    /**
     * Very similar to {@link TrigTools#sinTurns(double)} with half frequency, or {@link Math#sin(double)} with {@link Math#PI}
     * frequency, but optimized (and shaped) a little differently. This looks like a squished sine wave when graphed,
     * and is essentially just interpolating between each pair of odd and even inputs using what FastNoise calls
     * {@code QUINTIC} interpolation. This interpolation is slightly flatter at peaks and valleys than a sine wave is.
     * <br>
     * An input of any even number should produce something very close to -1.0, any odd number should produce something
     * very close to 1.0, and any number halfway between two incremental integers (like 8.5 or -10.5) should produce 0.0
     * or a very small fraction. In the (unlikely) event that this is given a double that is too large to represent
     * many or any non-integer values, this will simply return -1.0 or 1.0.
     * @param value any double other than NaN or infinite values; extremely large values can't work properly
     * @return a double from -1.0 (inclusive) to 1.0 (inclusive)
     */
    public static double sway(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (floor << 1) - floor;
    }

    /**
     * Very similar to {@link TrigTools#sinTurns(float)} with half frequency, or {@link Math#sin(double)} with {@link Math#PI}
     * frequency, but optimized (and shaped) a little differently. This looks like a squished sine wave when graphed,
     * and is essentially just interpolating between each pair of odd and even inputs using what FastNoise calls
     * {@code QUINTIC} interpolation. This interpolation is slightly flatter at peaks and valleys than a sine wave is.
     * <br>
     * An input of any even number should produce something very close to -1f, any odd number should produce something
     * very close to 1f, and any number halfway between two incremental integers (like 8.5f or -10.5f) should produce 0f
     * or a very small fraction. In the (unlikely) event that this is given a float that is too large to represent
     * many or any non-integer values, this will simply return -1f or 1f.
     * @param value any float other than NaN or infinite values; extremely large values can't work properly
     * @return a float from -1f (inclusive) to 1f (inclusive)
     */
    public static float sway(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (floor << 1) - floor;
    }

    /**
     * Very similar to {@link TrigTools#sinTurns(double)} with half frequency, or {@link Math#sin(double)} with {@link Math#PI}
     * frequency, but optimized (and shaped) a little differently. This looks like a squished sine wave when graphed,
     * and is essentially just interpolating between each pair of odd and even inputs using what FastNoise calls
     * {@code HERMITE} interpolation. This interpolation is rounder at peaks and valleys than a sine wave is; it is
     * also called {@code smoothstep} in GLSL, and is called Cubic here because it gets the third power of a value.
     * <br>
     * An input of any even number should produce something very close to -1.0, any odd number should produce something
     * very close to 1.0, and any number halfway between two incremental integers (like 8.5 or -10.5) should produce 0.0
     * or a very small fraction. In the (unlikely) event that this is given a double that is too large to represent
     * many or any non-integer values, this will simply return -1.0 or 1.0.
     * @param value any double other than NaN or infinite values; extremely large values can't work properly
     * @return a double from -1.0 (inclusive) to 1.0 (inclusive)
     */
    public static double swayCubic(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor = (-(floor & 1L) | 1L);
        return value * value * (3.0 - value * 2.0) * (floor << 1) - floor;
    }

    /**
     * Very similar to {@link TrigTools#sinTurns(float)} with half frequency, or {@link Math#sin(double)} with {@link Math#PI}
     * frequency, but optimized (and shaped) a little differently. This looks like a squished sine wave when graphed,
     * and is essentially just interpolating between each pair of odd and even inputs using what FastNoise calls
     * {@code HERMITE} interpolation. This interpolation is rounder at peaks and valleys than a sine wave is; it is
     * also called {@code smoothstep} in GLSL, and is called Cubic here because it gets the third power of a value.
     * <br>
     * An input of any even number should produce something very close to -1f, any odd number should produce something
     * very close to 1f, and any number halfway between two incremental integers (like 8.5f or -10.5f) should produce 0f
     * or a very small fraction. In the (unlikely) event that this is given a float that is too large to represent
     * many or any non-integer values, this will simply return -1f or 1f.
     * @param value any float other than NaN or infinite values; extremely large values can't work properly
     * @return a float from -1f (inclusive) to 1f (inclusive)
     */
    public static float swayCubic(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor = (-(floor & 1) | 1);
        return value * value * (3f - value * 2f) * (floor << 1) - floor;
    }

    /**
     * Limited-use; takes any float and produces a float in the 0f to 1f range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle.
     * <br>
     * An input of any even number should produce something very close to 0f, any odd number should produce something
     * very close to 1f, and any number halfway between two incremental integers (like 8.5f or -10.5f) should produce
     * 0.5f. In the (unlikely) event that this is given a float that is too large to represent many or any non-integer
     * values, this will simply return 0f or 1f. This version is called "Tight" because its range is tighter than
     * {@link #sway(float)}.
     * @param value any float other than NaN or infinite values; extremely large values can't work properly
     * @return a float from 0f (inclusive) to 1f (inclusive)
     */
    public static float swayTight(float value)
    {
        int floor = (value >= 0f ? (int) value : (int) value - 1);
        value -= floor;
        floor &= 1;
        return value * value * value * (value * (value * 6f - 15f) + 10f) * (-floor | 1) + floor;
    }

    /**
     * Limited-use; takes any double and produces a double in the 0.0 to 1.0 range, with a graph of input to output that
     * looks much like a sine wave, curving to have a flat slope when given an integer input and a steep slope when the
     * input is halfway between two integers, smoothly curving at any points between those extremes. This is meant for
     * noise, where it may be useful to limit the amount of change between nearby points' noise values and prevent both
     * sudden "jumps" in noise value and "cracks" where a line takes a sudden jagged movement at an angle.
     * <br>
     * An input of any even number should produce something very close to 0.0, any odd number should produce something
     * very close to 1.0, and any number halfway between two incremental integers (like 8.5 or -10.5) should produce
     * 0.5f. In the (unlikely) event that this is given a double that is too large to represent many or any non-integer
     * values, this will simply return 0.0 or 1.0. This version is called "Tight" because its range is tighter than
     * {@link #sway(double)}.
     * @param value any double other than NaN or infinite values; extremely large values can't work properly
     * @return a double from 0.0 (inclusive) to 1.0 (inclusive)
     */
    public static double swayTight(double value)
    {
        long floor = (value >= 0.0 ? (long) value : (long) value - 1L);
        value -= floor;
        floor &= 1L;
        return value * value * value * (value * (value * 6.0 - 15.0) + 10.0) * (-floor | 1L) + floor;
    }

    /**
     * A mix of the smooth transitions of {@link #sway(double)} with (seeded) random peaks and valleys between -1.0 and
     * 1.0 (both exclusive). The pattern this will produces will be completely different if the seed changes, and it is
     * suitable for 1D noise. Uses a simple method of cubic interpolation between random values, where a random value is
     * used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(double)}, which uses quintic (this causes wobble() to produce more
     * outputs in the mid-range and less at extremes; it is also slightly faster and simpler).
     * <br>
     * Performance note: HotSpot seems to be much more able to optimize wobble(long, float) than
     * wobble(long, double), with the float version almost twice as fast after JIT warms up. On GWT, the
     * reverse should be expected because floats must be emulated there.
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a double that typically changes slowly, by less than 1.0, with direction changes at integer inputs
     * @return a pseudo-random double between -1.0 and 1.0 (both exclusive), smoothly changing with value
     */
    public static double wobble(long seed, double value)
    {
        final long floor = value >= 0.0 ? (long) value : (long) value - 1L; // the closest long that is less than value
        // gets a random start and endpoint. there's a sequence of start and end values for each seed, and changing the
        // seed changes the start and end values unpredictably (so use the same seed for one curving line).
        final double start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.fffffffffffffbp-63;
        // gets the fractional part of value
        value -= floor;
        // cubic interpolation to smooth the curve
        value *= value * (3.0 - 2.0 * value);
        // interpolate between start and end based on how far we are between the start and end points of this section
        return (1.0 - value) * start + value * end;
    }

    /**
     * A mix of the smooth transitions of {@link #sway(float)} with (seeded) random peaks and valleys between -1f and
     * 1f (both exclusive). The pattern this will produces will be completely different if the seed changes, and it is
     * suitable for 1D noise. Uses a simple method of cubic interpolation between random values, where a random value is
     * used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(float)}, which uses quintic (this causes wobble() to produce more
     * outputs in the mid-range and less at extremes; it is also slightly faster and simpler).
     * <br>
     * Performance note: HotSpot seems to be much more able to optimize wobble(long, float) than
     * wobble(long, double), with the float version almost twice as fast after JIT warms up. On GWT, the
     * reverse should be expected because floats must be emulated there.
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float wobble(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        final float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffp-63f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L)) * 0x0.ffffffp-63f;
        value -= floor;
        value *= value * (3f - 2f * value);
        return (1f - value) * start + value * end;
    }

    /**
     * A variant on {@link #wobble(long, double)} that takes an int seed instead of a long, and is optimized for
     * usage on GWT. Like the version with a long seed, this uses cubic interpolation between random peak or valley
     * points; only the method of generating those random peaks and valleys has changed.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a double that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random double between -1.0 and 1.0 (both exclusive), smoothly changing with value
     */
    public static double wobble(final int seed, double value)
    {
        final int floor = value >= 0.0 ? (int) value : (int) value - 1;
        int z = seed + floor;
        final double start = (((z = (z ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x1D2BC3)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z ^ z << 11) * 0x0.ffffffp-31;
        value -= floor;
        value *= value * (3.0 - 2.0 * value);
        return (1.0 - value) * start + value * end;
    }

    /**
     * A variant on {@link #wobble(long, float)} that takes an int seed instead of a long, and is optimized for
     * usage on GWT. Like the version with a long seed, this uses cubic interpolation between random peak or valley
     * points; only the method of generating those random peaks and valleys has changed.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float wobble(final int seed, float value)
    {
        final int floor = value >= 0f ? (int) value : (int) value - 1;
        int z = seed + floor;
        final float start = (((z = (z ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f,
                end = (((z = (seed + floor + 1 ^ 0xD1B54A35) * 0x102473) ^ (z << 11 | z >>> 21) ^ (z << 19 | z >>> 13)) * ((z ^ z >>> 15) | 0xFFE00001) ^ z) * 0x0.ffffffp-31f;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }

    /**
     * A 1D "noise" method that produces smooth transitions like {@link #sway(float)}, but also wrapping around at pi *
     * 2 so this can be used to get smoothly-changing random angles. Has (seeded) random peaks and valleys where it
     * slows its range of change, but can return any value from 0 to 6.283185307179586f, or pi * 2. The pattern this
     * will produce will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(float)}, which uses quintic (this causes wobbleAngle() to be slightly
     * faster and simpler).
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @return a pseudo-random float between 0f and 6.283185307179586f (both inclusive), smoothly changing with value and wrapping
     */
    public static float wobbleAngle(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f;
        value -= floor;
        value *= value * (3f - 2f * value);
        end = end - start + 1.5f;
        end -= (long)end + 0.5f;
        start += end * value + 1;
        return (start - (long)start) * 6.283185307179586f;
    }

    /**
     * A 1D "noise" method that produces smooth transitions like {@link #sway(float)}, but also wrapping around at 1.0
     * so this can be used to get smoothly-changing random angles in turns. Has (seeded) random peaks and valleys where
     * it slows its range of change, but can return any value from 0 to 1.0. The pattern this
     * will produce will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than {@link #sway(float)}, which uses quintic (this causes wobbleAngleTurns() to be slightly
     * faster and simpler).
     * @param seed a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @return a pseudo-random float between 0.0f and 1.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float wobbleAngleTurns(long seed, float value)
    {
        final long floor = value >= 0f ? (long) value : (long) value - 1L;
        float start = (((seed += floor * 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f,
                end = (((seed += 0x6C8E9CF570932BD5L) ^ (seed >>> 25)) * (seed | 0xA529L) >>> 1) * 0x0.ffffffp-62f;
        value -= floor;
        value *= value * (3f - 2f * value);
        end = end - start + 1.5f;
        end -= (long)end + 0.5f;
        start += end * value + 1;
        return (start - (long)start);
    }
}
