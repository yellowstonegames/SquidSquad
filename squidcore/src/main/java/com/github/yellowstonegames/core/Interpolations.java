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

import com.github.tommyettinger.digital.Base;

import java.util.Arrays;

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

    public static class Interpolator implements InterpolationFunction {
        public String tag;
        public InterpolationFunction fn;
        public float[] parameters;

        public Interpolator() {
            this.tag = "linear";
            this.fn = linearFunction;
            this.parameters = new float[0];
        }

        public Interpolator(String tag, InterpolationFunction fn, float... parameters) {
            this.tag = tag;
            this.fn = fn;
            this.parameters = parameters;
        }

        @Override
        public float apply(float alpha) {
            return fn.apply(alpha);
        }

        public Interpolator copy() {
            return new Interpolator(tag, fn, Arrays.copyOf(parameters, parameters.length));
        }

        /**
         * Serializes the tag and any parameters of this Interpolator to a String that can be used by
         * {@link #stringDeserialize(String)} to load this Interpolator at another time. This always uses
         * {@link Base#SIMPLE64} as its base.
         * @return a String storing all data from this Interpolator along with its name
         */
        public String stringSerialize() {
            return stringSerialize(Base.SIMPLE64);
        }

        /**
         * Serializes the tag and any parameters of this Interpolator to a String that can be used by
         * {@link #stringDeserialize(String)} to load this Interpolator at another time.
         * @param base which {@link Base} to use to store any parameters using {@link Base#joinExact(String, float[])}
         * @return a String storing all data from this Interpolator along with its name
         */
        public String stringSerialize(Base base) {
            return tag + "`" + base.joinExact("~", parameters) + "`";
        }

        /**
         * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this
         * Interpolator to match the state in the serialized data. This only works if this Interpolator is the same
         * implementation that was serialized. Always uses {@link Base#SIMPLE64}. Returns this Interpolator, after
         * possibly changing its state.
         * <br>
         * This isn't very useful on its own; use Interpolations.Deserializer to acquire a known Interpolator.
         *
         * @param data a String probably produced by {@link #stringSerialize()}
         * @return this, after setting its state
         */
        public Interpolator stringDeserialize(String data) {
            return stringDeserialize(data, Base.SIMPLE64);
        }

        /**
         * Given a String in the format produced by {@link #stringSerialize(Base)}, and the same {@link Base} used by
         * the serialization, this will attempt to set this Interpolator to match the state in the serialized data.
         * This only works if this Interpolator is the same implementation that was serialized, and also needs
         * the Bases to be identical. Returns this Interpolator, after possibly changing its state.
         * <br>
         * This isn't very useful on its own; use Interpolations.Deserializer to acquire a known Interpolator.
         *
         * @param data a String probably produced by {@link #stringSerialize(Base)}
         * @param base which Base to use, from the "digital" library, such as {@link Base#BASE10}
         * @return this, after setting its state
         */
        //TODO: link to Deserializer once it is written
        public Interpolator stringDeserialize(String data, Base base) {
            int idx = data.indexOf('`');
            parameters = base.floatSplitExact(data, "~", idx + 1, data.indexOf('`', idx + 2));
            return this;
        }
    }

    /**
     * Linear interpolation; just returns its argument.
     */
    public static final InterpolationFunction linearFunction = (a -> a);

    public static final Interpolator linear = new Interpolator("linear", linearFunction);
    /**
     * "Smoothstep" or a cubic Hermite spline.
     */
    public static final Interpolator smooth = new Interpolator("smooth", a -> a * a * (3 - 2 * a));

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
