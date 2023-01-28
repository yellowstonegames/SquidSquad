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

package com.github.yellowstonegames.core;

import static com.github.tommyettinger.digital.MathTools.barronSpline;

public final class Interpolations {

    /**
     * No need to instantiate.
     */
    private Interpolations() {
    }

    /**
     * A type of function that takes a float from 0 to 1 (usually, and also usually inclusive) and returns a float that
     * is typically in the 0 to 1 range or just outside it. Meant for easier communication with libGDX's Interpolation
     * class by using one of its Interpolation constants with a method reference. This is a functional interface whose
     * functional method is {@link #apply(float)}.
     */
    @FunctionalInterface
    public interface InterpolationFunction {
        float apply(float alpha);

        default float apply(float start, float end, float alpha) {
            return start + apply(alpha) * (end - start);
        }
    }

    /**
     * Linear interpolation; just returns its argument.
     */
    public static InterpolationFunction linear = (f -> f);

    /**
     * A wrapper around {@link com.github.tommyettinger.digital.MathTools#barronSpline(float, float, float)} to use it
     * as an InterpolationFunction. Useful because it can imitate the wide variety of symmetrical interpolations by
     * setting turning to 0.5 and shape to some value greater than 1, while also being able to produce the inverse of
     * those interpolations by setting shape to some value between 0 and 1. It can also produce asymmetrical
     * interpolations by using a turning value other than 0.5 .
     */
    public static class BiasGain implements InterpolationFunction {
        /**
         * The shape parameter will cause this to imitate "smoothstep-like" splines when greater than 1 (where the
         * values ease into their starting and ending levels), or to be the inverse when less than 1 (where values
         * start like square root does, taking off very quickly, but also end like square does, landing abruptly at
         * the ending level).
         */
        public final float shape;
        /**
         * A value between 0.0 and 1.0, inclusive, where the shape changes.
         */
        public final float turning;

        public BiasGain(float shape, float turning) {
            this.shape = shape;
            this.turning = turning;
        }

        public float apply(float a) {
            return barronSpline(a, shape, turning);
        }
    }

}
