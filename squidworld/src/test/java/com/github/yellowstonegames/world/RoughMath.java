/*=====================================================================*
 *                   Copyright (C) 2011 Paul Mineiro                   *
 * All rights reserved.                                                *
 *                                                                     *
 * Redistribution and use in source and binary forms, with             *
 * or without modification, are permitted provided that the            *
 * following conditions are met:                                       *
 *                                                                     *
 *     * Redistributions of source code must retain the                *
 *     above copyright notice, this list of conditions and             *
 *     the following disclaimer.                                       *
 *                                                                     *
 *     * Redistributions in binary form must reproduce the             *
 *     above copyright notice, this list of conditions and             *
 *     the following disclaimer in the documentation and/or            *
 *     other materials provided with the distribution.                 *
 *                                                                     *
 *     * Neither the name of Paul Mineiro nor the names                *
 *     of other contributors may be used to endorse or promote         *
 *     products derived from this software without specific            *
 *     prior written permission.                                       *
 *                                                                     *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND              *
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,         *
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES               *
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE             *
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER               *
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                 *
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES            *
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE           *
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR                *
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF          *
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT           *
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY              *
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE             *
 * POSSIBILITY OF SUCH DAMAGE.                                         *
 *                                                                     *
 * Contact: Paul Mineiro <paul@mineiro.com>                            *
 *=====================================================================*/

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.BitConversion;

/**
 * Fast approximations of math methods that reach roughly the right answer most of the time.
 * The math methods this covers include exponential ({@link #expRough(float)}, {@link #pow2Rough(float)}), logarithmic
 * ({@link #logRough(float)}, {@link #log2Rough(float)}), hyperbolic ({@link #sinhRough(float)},
 * {@link #coshRough(float)}), {@link #tanhRough(float)}), and logistic ({@link #logisticRough(float)}) functions, as
 * well as less-precise but usually-faster versions with the suffix "Rougher" instead of "Rough".
 * <br>
 * Ported from <a href="https://code.google.com/archive/p/fastapprox/">fastapprox</a>, which is open source
 * under the New BSD License.
 */
public final class RoughMath {
    private RoughMath() {
    }

    // EXPONENTIAL AND LOGARITHMIC FUNCTIONS

    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, somewhat roughly.
     * @param p the power to raise 2 to; can be any float
     * @return an approximation of 2 raised to the p power; can be any float greater than 0
     */
    public static float pow2Rough (float p)
    {
        final float clip = Math.max(-126.0f, p);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return BitConversion.intBitsToFloat((int) (0x800000 * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z)));
    }
    /**
     * Approximates {@code Math.pow(2.0, p)} with single-precision, very roughly.
     * @param p the power to raise 2 to; can be any float
     * @return an approximation of 2 raised to the p power; can be any float greater than 0
     */
    public static float pow2Rougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, p) + 126.94269504f)));
    }

    /**
     * Approximates {@code Math.pow(Math.E, p)} with single-precision, somewhat roughly.
     * @param p the power to raise E to; can be any float
     * @return an approximation of E raised to the p power; can be any float greater than 0
     */
    public static float expRough (float p)
    {
        final float clip = Math.max(-126.0f, p * 1.442695040f);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return BitConversion.intBitsToFloat((int) (0x800000 * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z)));
    }

    /**
     * Approximates {@code Math.pow(Math.E, p)} with single-precision, very roughly.
     * @param p the power to raise E to; can be any float
     * @return an approximation of E raised to the p power; can be any float greater than 0
     */
    public static float expRougher (float p)
    {
        return BitConversion.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, 1.442695040f * p) + 126.94269504f)));
    }

    /**
     * Approximates the logarithm of {@code x} with base 2, using single-precision, somewhat roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base 2; can be any float
     */
    public static float log2Rough (float x)
    {
        final int vx = BitConversion.floatToIntBits(x);
        final float mx = BitConversion.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx);
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, somewhat roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base E; can be any float
     */
    public static float logRough (float x)
    {
        final int vx = BitConversion.floatToIntBits(x);
        final float mx = BitConversion.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return (vx * 1.1920928955078125e-7f - 124.22551499f - 1.498030302f * mx - 1.72587999f / (0.3520887068f + mx)) * 0.69314718f;
    }

    /**
     * Approximates the logarithm of {@code x} with base 2, using single-precision, very roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base 2; can be any float
     */
    public static float log2Rougher (float x){
        return BitConversion.floatToIntBits(x) * 1.1920928955078125e-7f - 126.94269504f;
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, very roughly.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base E; can be any float
     */
    public static float logRougher (float x){
        return BitConversion.floatToIntBits(x) * 8.2629582881927490e-8f - 87.989971088f;
    }

    // HYPERBOLIC TRIGONOMETRIC FUNCTIONS

    /**
     * Approximates {@code Math.sinh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to sinh; can be any float
     * @return an approximation of the hyperbolic sine of p; can be any float
     */
    public static float sinhRough (float p)
    {
        return 0.5f * (expRough (p) - expRough (-p));
    }

    /**
     * Approximates {@code Math.sinh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to sinh; can be any float
     * @return an approximation of the hyperbolic sine of p; can be any float
     */
    public static float sinhRougher (float p)
    {
        return 0.5f * (expRougher (p) - expRougher (-p));
    }

    /**
     * Approximates {@code Math.cosh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to cosh; can be any float
     * @return an approximation of the hyperbolic cosine of p; can be any float greater than or equal to 1
     */
    public static float coshRough (float p)
    {
        return 0.5f * (expRough (p) + expRough (-p));
    }

    /**
     * Approximates {@code Math.cosh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to cosh; can be any float
     * @return an approximation of the hyperbolic cosine of p; can be any float greater than or equal to 1
     */
    public static float coshRougher (float p)
    {
        return 0.5f * (expRougher (p) + expRougher (-p));
    }

    /**
     * Approximates {@code Math.tanh(p)}, somewhat roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to tanh; can be any float
     * @return an approximation of the hyperbolic tangent of p; between -1 and 1 inclusive
     */
    public static float tanhRough (float p)
    {
        // -1f + 2f / (1f + exp(-2f * x))
        final float clip = Math.max(-126.0f, -2.885390043258667f * p);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return -1.0f + 2.0f / (1.0f + BitConversion.intBitsToFloat((int) (0x800000 * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z))));
    }

    /**
     * Approximates {@code Math.tanh(p)}, very roughly.
     * <a href="https://en.wikipedia.org/wiki/Hyperbolic_functions">Wikipedia's page on hyperbolic functions</a> may be useful.
     * @param p the argument to tanh; can be any float
     * @return an approximation of the hyperbolic tangent of p; between -1 and 1 inclusive
     */
    public static float tanhRougher (float p)
    {
        // -1f + 2f / (1f + exp(-2f * x))
        return -1.0f + 2.0f / (1.0f + BitConversion.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * p) + 126.94269504f))));
    }

    // LOGISTIC FUNCTION

    /**
     * Approximates the <a href="https://en.wikipedia.org/wiki/Logistic_function">standard logistic function</a>, somewhat roughly.
     * This is also called the sigmoid function, or expit. It is the same as {@link #tanhRough(float)}, scaled, with an offset.
     * @param x the parameter to the standard logistic function; can be any float
     * @return an approximation of the logistic function of x; between 0 and 1 inclusive
     */
    public static float logisticRough (float x)
    {
        // 1f / (1f + exp(-x))
        final float clip = Math.max(-126.0f, x * -1.442695040f);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return 1.0f / (1.0f + BitConversion.intBitsToFloat((int) (0x800000 * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z))));
    }

    /**
     * Approximates the <a href="https://en.wikipedia.org/wiki/Logistic_function">standard logistic function</a>, very roughly.
     * This is also called the sigmoid function, or expit. It is the same as {@link #tanhRough(float)}, scaled, with an offset.
     * @param x the parameter to the standard logistic function; can be any float
     * @return an approximation of the logistic function of x; between 0 and 1 inclusive
     */
    public static float logisticRougher (float x)
    {
        // 1f / (1f + exp(-x))
        return 1.0f / (1.0f + BitConversion.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -1.442695040f * x) + 126.94269504f))));
    }
}
