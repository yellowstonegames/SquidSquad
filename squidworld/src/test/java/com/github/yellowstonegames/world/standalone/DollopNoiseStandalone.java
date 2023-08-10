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
package com.github.yellowstonegames.world.standalone;

import com.badlogic.gdx.utils.NumberUtils;

/**
 * Dollop noise code that depends only on libGDX.
 * <br>
 * This is a somewhat-smooth type of noise that usually only uses one octave, so octave code is not supplied.
 */
public class DollopNoiseStandalone {

    /**
     * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
     */
    protected long seed;
    /**
     * You will probably need to change the frequency to be much closer or much further from 0, depending on the scale
     * of your noise.
     */
    protected double frequency = 1.0;

    public static final DollopNoiseStandalone instance = new DollopNoiseStandalone();

    public DollopNoiseStandalone() {
        this(1234567890L, 1.0);
    }

    public DollopNoiseStandalone(long seed, double frequency)
    {
        this.seed = seed;
        this.frequency = frequency;
    }

    // SHARED

    /**
     * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long.
     * This point hash is fast and very good at randomizing its bits when any argument changes even slightly.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y point with the given state
     */
    public static long hashAll(long x, long y, long s) {
        s ^= x * 0x8CB92BA72F3D8DD7L ^ y * 0xABC98388FB8FAC03L;
        return (s = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L) ^ s >>> 25;
    }

    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so that they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     *
     * @param x       progress through the spline, from 0 to 1, inclusive
     * @param shape   must be greater than or equal to 0; values greater than 1 are "normal interpolations"
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a double between 0 and 1, inclusive
     */
    public static double barronSpline(final double x, final double shape, final double turning) {
        final double d = turning - x;
        final long f = NumberUtils.doubleToLongBits(d) >> 63, n = f | 1L;
        return (turning * n - f) * (x + f) / (Double.MIN_NORMAL - f + (x + shape * d) * n) - f;
    }
    /**
     * A line-wobbling method that only tolerates non-negative {@code value} and wraps value when
     * it gets too close to {@code modulus}. One
     * potential use is for looping animations; if the modulus is set so that {@code value} equals {@code modulus} (or
     * an integer multiple of it) exactly when the animation starts and ends, the animation will then loop seamlessly,
     * at least for anything that depends on this method.
     * <br>
     * The wobble methods have a shape where they flatten out when {@code value} is an integer, so typically you should
     * add a value smaller than 1 to value at each call if you want it to change smoothly. For the wrapped methods, the
     * modulus is also the number of times the curve will flatten out over the course of a cycle.
     * <br>
     * Note that {@code wobbleWrappedTight(seed, 0, modulus)} and {@code wobbleWrappedTight(seed, modulus, modulus)}
     * will produce the same value as long as modulus is positive (and not large enough to incur precision loss).
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a non-negative float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @param modulus where to wrap value if it gets too high; must be positive
     * @return a pseudo-random float between 0f and 1f (both inclusive), smoothly changing with value
     */
    public static double wobbleWrappedTight(long seed, double value, int modulus)
    {
        final long floor = (long) value & 0x3FFFFFFFFFFFFFFFL;
        final double start = (((seed + floor % modulus) * 0x6C8E9CF570932BD5L ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L >>> 1) * 0x1.0p-63,
                end = (((seed + (floor + 1) % modulus) * 0x6C8E9CF570932BD5L ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L >>> 1) * 0x1.0p-63;
        value -= floor;
        value *= value * (3 - 2 * value);
        return (1 - value) * start + value * end;
    }

    // 2D SECTION

    /**
     * Gets 2D dollop noise with the lowest, fastest level of detail. Uses
     * {@link #getSeed()} and multiplies x and y by frequency.
     * @param x x coordinate, will be adjusted by frequency
     * @param y y coordinate, will be adjusted by frequency
     * @return noise between -1 and 1, inclusive
     */
    public double getNoise(final double x, final double y) {
        return noiseWithSeed(x * frequency, y * frequency, seed);
    }

    /**
     * 2D dollop noise with the lowest, fastest level of detail. Uses the
     * seed {@code 12345L} and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public static double noise(final double x, final double y) {
        return noiseWithSeed(x, y, 12345L);
    }

    /**
     * Gets 2D dollop noise with the lowest, fastest level of detail. Uses the given seed
     * and does not change x or y.
     * @param x x coordinate
     * @param y y coordinate
     * @param seed the seed to use for the noise (used in place of {@link #getSeed()})
     * @return noise between -1 and 1
     */
    public static double noiseWithSeed(final double x, final double y, final long seed) {
        // casting Math.floor used to be slow on older JVMs; it shouldn't be since Java 16 or so.
        long i = Math.round(x);
        long j = Math.round(y);

        long hash = hashAll(i, j, seed);

        if(hash >= 0x9800000000000000L) // 1 out of 24 values result in a dollop
            return -1.0;

        double x0 = x - i;
        double y0 = y - j;

        double angle = (Math.atan2(y0, x0) + Math.PI) * (3.5 / Math.PI);

        double mag = 1.0 - Math.sqrt(x0 * x0 + y0 * y0) * (3.5 + wobbleWrappedTight(seed ^ hash, angle, 7));

        return Math.max(-1.0, mag);

    }

    // OTHER

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "DollopNoiseStandalone{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DollopNoiseStandalone that = (DollopNoiseStandalone) o;

        if (seed != that.seed) return false;
        return Double.compare(that.frequency, frequency) == 0;
    }

    @Override
    public int hashCode() {
        final long bits = NumberUtils.doubleToLongBits(frequency) * 421L;
        return (int) (seed ^ seed >>> 32 ^ bits ^ bits >>> 32);
    }
}
