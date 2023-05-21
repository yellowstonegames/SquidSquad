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

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.github.tommyettinger.digital.MathTools.barronSpline;

/**
 * Provides predefined {@link Interpolator} constants and ways to generate {@link InterpolationFunction} instance, as
 * well as acting as the registry for known Interpolator values so that they can be looked up by name.
 */
public final class Interpolations {

    private static final ObjectObjectOrderedMap<String, Interpolator> REGISTRY = new ObjectObjectOrderedMap<>(128);

    /**
     * Looks up the given {@code tag} in a registry of Interpolators, and if there exists one with that name, returns
     * it. Otherwise, this returns null.
     * @param tag a tag used to register an Interpolator here
     * @return the Interpolator registered with the given tag, or null if none exists for that tag
     */
    public static @Nullable Interpolator get(String tag) {
        return REGISTRY.get(tag);
    }

    /**
     * Allocates a new String array, fills it with every tag registered for an Interpolator, and returns that array.
     * @return an array containing every String tag registered for an Interpolator
     */
    public static String[] getTagArray() {
        return REGISTRY.keySet().toArray(new String[0]);
    }

    /**
     * Allocates a new Interpolator array, fills it with every registered Interpolator, and returns that array.
     * @return an array containing every Interpolator registered
     */
    public static Interpolator[] getInterpolatorArray() {
        return REGISTRY.values().toArray(new Interpolator[0]);
    }

    /**
     * Allocates a new ObjectList, fills it with every tag registered for an Interpolator, and returns that ObjectList.
     * @return an ObjectList containing every String tag registered for an Interpolator
     */
    public static ObjectList<String> getTagList() {
        return REGISTRY.keySet().toList();
    }

    /**
     * Allocates a new ObjectList, fills it with every registered Interpolator, and returns that ObjectList.
     * @return an ObjectList containing every Interpolator registered
     */
    public static ObjectList<Interpolator> getInterpolatorList() {
        return REGISTRY.values().toList();
    }

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
     * A simple wrapper around an {@link InterpolationFunction} so it is associated with a String {@link #tag}. This
     * also implements InterpolationFunction, and wraps the {@link #fn} it stores to clamp the output if the input is
     * too low or too high.
     */
    public static class Interpolator implements InterpolationFunction {
        public final @NonNull String tag;
        public final @NonNull InterpolationFunction fn;

        /**
         * Calls {@link #Interpolator(String, InterpolationFunction)} with {@code "linear"} and {@link #linearFunction}.
         * Because {@link #linear} is already registered with that tag and function, this isn't very useful.
         */
        public Interpolator() {
            this("linear", linearFunction);
        }

        /**
         * Creates an Interpolator that will use the given {@code fn} and registers it with the given tag. The tag must
         * be unique; if {@link Interpolations#get(String)} returns a non-null value when looking up the given tag, then
         * if you create an Interpolator with that tag, the existing value will be overwritten.
         * @param tag a unique String that can be used as a key to access this with {@link Interpolations#get(String)}
         * @param fn an {@link InterpolationFunction} to wrap
         */
        public Interpolator(@NonNull String tag, @NonNull InterpolationFunction fn) {
            this.tag = tag;
            this.fn = fn;
            REGISTRY.put(tag, this);
        }

        /**
         * Does bounds-checking on the input before passing it to {@link #fn}. If alpha is less than, roughly,
         * one-millionth, then this always returns 0; if alpha is greater than or equal to 1, this returns 1, and in any
         * other case it delegates to {@link #fn}.
         * @param alpha almost always between 0 and 1, inclusive
         * @return an interpolated value based on alpha, which may (for some functions) be negative, or greater than 1
         */
        @Override
        public float apply(float alpha) {
            if (alpha < MathTools.FLOAT_ROUNDING_ERROR) return 0;
            if (alpha >= 1f) return 1f;
            return fn.apply(alpha);
        }

        /**
         * Gets the tag for this Interpolator, which is a unique String that identifies this object. If another
         * Interpolator tries to use the same tag, this Interpolator will be un-registered and will no longer be
         * returnable from {@link #get(String)}.
         * @return the tag String
         */
        public @NonNull String getTag() {
            return tag;
        }

        /**
         * Gets the InterpolationFunction this actually uses to do its math work. Calling this function on its own does
         * not behave the same way as calling {@link Interpolator#apply(float)} on this Interpolator; the Interpolator
         * method clamps the result if the {@code alpha} parameter is below 0 or above 1.
         * @return the InterpolationFunction this uses
         */
        public @NonNull InterpolationFunction getFn() {
            return fn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Interpolator that = (Interpolator) o;

            return tag.equals(that.tag);
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        @Override
        public String toString() {
            return tag;
        }
    }

    /**
     * Linear interpolation; just returns its argument.
     */
    public static final InterpolationFunction linearFunction = (a -> a);

    /**
     * Plain linear interpolation, or "lerp"; this just returns the alpha it is given.
     */
    public static final Interpolator linear = new Interpolator("linear", linearFunction);
    /**
     * "Smoothstep" or a cubic Hermite spline.
     */
    public static final Interpolator smooth = new Interpolator("smooth", a -> a * a * (3 - 2 * a));
    /**
     * "Smoothstep" or a cubic Hermite spline applied twice.
     */
    public static final Interpolator smooth2 = new Interpolator("smooth2", a -> (a *= a * (3 - 2 * a)) * a * (3 - 2 * a));
    /**
     * A quintic Hermite spline by Ken Perlin.
     */
    public static final Interpolator smoother = new Interpolator("smoother", a -> a * a * a * (a * (a * 6 - 15) + 10));
    /**
     * A quintic Hermite spline by Ken Perlin; this uses the same function as {@link #smoother}.
     */
    public static final Interpolator fade = new Interpolator("fade", a -> a * a * a * (a * (a * 6 - 15) + 10));
    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts slowly, speeds up in the middle and slows down at the end. The
     * rate of acceleration and deceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the Pow in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powFunction(final float power) {
        return a -> {
            if (a <= 0.5f) return (float) Math.pow(a + a, power) * 0.5f;
            return (float) Math.pow(2f - a - a, power) * -0.5f + 1f;
        };
    }

    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts slowly and speeds up toward the end. The
     * rate of acceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the PowIn in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powInFunction(final float power) {
        return a -> (float) Math.pow(a, power);
    }
    /**
     * Produces an InterpolationFunction that uses the given power variable.
     * When power is greater than 1, this starts quickly and slows down toward the end. The
     * rate of deceleration changes based on the parameter. Non-integer parameters are supported,
     * unlike the PowOut in libGDX. Negative powers are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction powOutFunction(final float power) {
        return a -> 1f - (float) Math.pow(1f - a, power);
    }

    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 2.
     */
    public static final Interpolator pow2 = new Interpolator("pow2", powFunction(2f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 3.
     */
    public static final Interpolator pow3 = new Interpolator("pow3", powFunction(3f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 4.
     */
    public static final Interpolator pow4 = new Interpolator("pow4", powFunction(4f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 5.
     */
    public static final Interpolator pow5 = new Interpolator("pow5", powFunction(5f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75 = new Interpolator("pow0_75", powFunction(0.75f));
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5 = new Interpolator("pow0_5", a -> {
        if (a <= 0.5f) return (float) Math.sqrt(a + a) * 0.5f;
        return (float) Math.sqrt(2f - a - a) * -0.5f + 1f;
    });
    /**
     * Accelerates and decelerates using {@link #powFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25 = new Interpolator("pow0_25", powFunction(0.25f));

//    // This might make sense to PR to libGDX, because it should avoid a modulus and conditional.
//    public static float oPow(float a, int power){
//        if (a <= 0.5f) return (float) Math.pow(a * 2, power) * 0.5f;
//        return (float) Math.pow((a - 1) * 2, power) * ((power & 1) - 0.5f) + 1;
//    }
//
//    // This might make more sense as a replacement, since it allows non-integer powers.
//    public static float aPow(float a, float power){
//        if (a <= 0.5f) return (float) Math.pow(a * 2, power) * 0.5f;
//        return (float) Math.pow((1 - a) * 2, power) * -0.5f + 1;
//    }

    /**
     * Accelerates using {@link #powInFunction(float)} and power of 2.
     */
    public static final Interpolator pow2In = new Interpolator("pow2In", powInFunction(2f));
    /**
     * Slow, then fast. This uses the same function as {@link #pow2In}.
     */
    public static final Interpolator slowFast = new Interpolator("slowFast", powInFunction(2f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 3.
     */
    public static final Interpolator pow3In = new Interpolator("pow3In", powInFunction(3f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 4.
     */
    public static final Interpolator pow4In = new Interpolator("pow4In", powInFunction(4f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 5.
     */
    public static final Interpolator pow5In = new Interpolator("pow5In", powInFunction(5f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75In = new Interpolator("pow0_75In", powInFunction(0.75f));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5In = new Interpolator("pow0_5In", a -> (float) Math.sqrt(a));
    /**
     * Accelerates using {@link #powInFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25In = new Interpolator("pow0_25In", powInFunction(0.25f));
    /**
     * An alias for {@link #pow0_5In}, this is the inverse for {@link #pow2In}. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow2InInverse = new Interpolator("pow2InInverse", a -> (float) Math.sqrt(a));
    /**
     * This is the inverse for {@link #pow3In}. Its function is simply a method reference to
     * {@link MathTools#cbrt(float)}.
     */
    public static final Interpolator pow3InInverse = new Interpolator("pow3InInverse", MathTools::cbrt);

    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 2.
     */
    public static final Interpolator pow2Out = new Interpolator("pow2Out", powOutFunction(2f));
    /**
     * Fast, then slow. This uses the same function as {@link #pow2Out}.
     */
    public static final Interpolator fastSlow = new Interpolator("fastSlow", powOutFunction(2f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 3.
     */
    public static final Interpolator pow3Out = new Interpolator("pow3Out", powOutFunction(3f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 4.
     */
    public static final Interpolator pow4Out = new Interpolator("pow4Out", powOutFunction(4f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 5.
     */
    public static final Interpolator pow5Out = new Interpolator("pow5Out", powOutFunction(5f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.75.
     */
    public static final Interpolator pow0_75Out = new Interpolator("pow0_75Out", powOutFunction(0.75f));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.5. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow0_5Out = new Interpolator("pow0_5Out", a -> 1f - (float) Math.sqrt(1f - a));
    /**
     * Decelerates using {@link #powOutFunction(float)} and power of 0.25.
     */
    public static final Interpolator pow0_25Out = new Interpolator("pow0_25Out", powOutFunction(0.25f));
    /**
     * An alias for {@link #pow0_5Out}, this is the inverse of {@link #pow2Out}. Optimized with {@link Math#sqrt(double)}.
     */
    public static final Interpolator pow2OutInverse = new Interpolator("pow2OutInverse", a -> 1f - (float) Math.sqrt(1f - a));
    /**
     * This is the inverse for {@link #pow3Out}. Optimized with {@link MathTools#cbrt(float)}.
     */
    public static final Interpolator pow3OutInverse = new Interpolator("pow3OutInverse", a -> 1f - MathTools.cbrt(1f - a));

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts slowly, speeds up in the middle and slows down at the end. The
     * rate of acceleration and deceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> {
            if (a <= 0.5f) return ((float)Math.pow(value, power * (a * 2f - 1f)) - min) * scale * 0.5f;
            return (2f - ((float)Math.pow(value, -power * (a * 2f - 1f)) - min) * scale) * 0.5f;
        };
    }

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts slowly and speeds up toward the end. The
     * rate of acceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expInFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> ((float)Math.pow(value, power * (a - 1f)) - min) * scale;
    }

    /**
     * Produces an InterpolationFunction that uses the given value and power variables.
     * When power is greater than 1, this starts quickly and slows down toward the end. The
     * rate of deceleration changes based on the parameter. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction expOutFunction(final float value, final float power) {
        final float min = (float) Math.pow(value, -power), scale = 1f / (1f - min);
        return a -> 1f - ((float)Math.pow(value, -power * a) - min) * scale;
    }

    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5 = new Interpolator("exp5", expFunction(2f, 5f));

    /**
     * Accelerates and decelerates using {@link #expFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10 = new Interpolator("exp10", expFunction(2f, 10f));

    /**
     * Accelerates using {@link #expInFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5In = new Interpolator("exp5In", expInFunction(2f, 5f));

    /**
     * Accelerates using {@link #expInFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10In = new Interpolator("exp10In", expInFunction(2f, 10f));

    /**
     * Decelerates using {@link #expOutFunction(float, float)}, value of 2 and power of 5.
     */
    public static final Interpolator exp5Out = new Interpolator("exp5Out", expOutFunction(2f, 5f));

    /**
     * Decelerates using {@link #expOutFunction(float, float)}, value of 2 and power of 10.
     */
    public static final Interpolator exp10Out = new Interpolator("exp10Out", expOutFunction(2f, 10f));


    /**
     * Produces an InterpolationFunction that uses the possible shapes of the
     * {@link com.github.tommyettinger.random.distribution.KumaraswamyDistribution}, but without involving a random
     * component. This can produce a wide range of shapes for the interpolation, and may require generating several
     * during development to get a particular shape you want. The a and b parameters must be greater than 0.0, but have
     * no other requirements. Most curves that this method produces are somewhat asymmetrical.
     * @param a the Kumaraswamy distribution's a parameter
     * @param b the Kumaraswamy distribution's b parameter
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction kumaraswamyFunction(final double a, final double b) {
        final double A = 1.0 / a;
        final double B = 1.0 / b;
        return x -> (float) Math.pow(1.0 - Math.pow(1.0 - x, B), A);
    }

    /**
     * Produces an InterpolationFunction that uses the given shape and turning variables.
     * A wrapper around {@link com.github.tommyettinger.digital.MathTools#barronSpline(float, float, float)} to use it
     * as an Interpolator or InterpolationFunction. Useful because it can imitate the wide variety of symmetrical
     * interpolations by setting turning to 0.5 and shape to some value greater than 1, while also being able to produce
     * the inverse of those interpolations by setting shape to some value between 0 and 1. It can also produce
     * asymmetrical interpolations by using a turning value other than 0.5 .
     * @param shape   must be greater than or equal to 0; values greater than 1 are "normal interpolations"
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction biasGainFunction(final float shape, final float turning) {
        return a -> barronSpline(a, shape, turning);
    }

    /**
     * Produces more results in the center; the first level of centrality. Uses {@code biasGainFunction(0.75f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredA = new Interpolator("biasGainCenteredA", biasGainFunction(0.75f, 0.5f));
    /**
     * Produces more results in the center; the second level of centrality. Uses {@code biasGainFunction(0.5f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredB = new Interpolator("biasGainCenteredB", biasGainFunction(0.5f, 0.5f));
    /**
     * Produces more results in the center; the third level of centrality. Uses {@code biasGainFunction(0.25f, 0.5f)}.
     */
    public static final Interpolator biasGainCenteredC = new Interpolator("biasGainCenteredC", biasGainFunction(0.25f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(2f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeA = new Interpolator("biasGainExtremeA", biasGainFunction(2f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(3f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeB = new Interpolator("biasGainExtremeB", biasGainFunction(3f, 0.5f));
    /**
     * Produces more results near 0 and near 1; the third level of extremity. Uses {@code biasGainFunction(4f, 0.5f)}.
     */
    public static final Interpolator biasGainExtremeC = new Interpolator("biasGainExtremeC", biasGainFunction(4f, 0.5f));
    /**
     * Produces more results near 0. Uses {@code biasGainFunction(3f, 0.9f)}.
     */
    public static final Interpolator biasGainMostlyLow = new Interpolator("biasGainMostlyLow", biasGainFunction(3f, 0.9f));
    /**
     * Produces more results near 1. Uses {@code biasGainFunction(3f, 0.1f)}.
     */
    public static final Interpolator biasGainMostlyHigh = new Interpolator("biasGainMostlyHigh", biasGainFunction(3f, 0.1f));

    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(0.75f, 0.75f)}.
     */
    public static final Interpolator kumaraswamyCentralA = new Interpolator("kumaraswamyCentralA", kumaraswamyFunction(0.75f, 0.75f));
    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(0.5f, 0.5f)}.
     */
    public static final Interpolator kumaraswamyCentralB = new Interpolator("kumaraswamyCentralB", kumaraswamyFunction(0.5f, 0.5f));
    /**
     * Produces more results in the center. Uses {@code kumaraswamyFunction(0.25f, 0.25f)}.
     */
    public static final Interpolator kumaraswamyCentralC = new Interpolator("kumaraswamyCentralC", kumaraswamyFunction(0.25f, 0.25f));
    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(2f, 2f)}.
     */
    public static final Interpolator kumaraswamyExtremeA = new Interpolator("kumaraswamyExtremeA", kumaraswamyFunction(2f, 2f));
    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(4f, 4f)}.
     */
    public static final Interpolator kumaraswamyExtremeB = new Interpolator("kumaraswamyExtremeB", kumaraswamyFunction(4f, 4f));
    /**
     * Produces more results toward the edges. Uses {@code kumaraswamyFunction(6f, 6f)}.
     */
    public static final Interpolator kumaraswamyExtremeC = new Interpolator("kumaraswamyExtremeC", kumaraswamyFunction(6f, 6f));
    /**
     * Produces more results near 0. Uses {@code kumaraswamyFunction(1f, 5f)}.
     */
    public static final Interpolator kumaraswamyMostlyLow = new Interpolator("kumaraswamyMostlyLow", kumaraswamyFunction(1f, 5f));
    /**
     * Produces more results near 1. Uses {@code kumaraswamyFunction(5f, 1f)}.
     */
    public static final Interpolator kumaraswamyMostlyHigh = new Interpolator("kumaraswamyMostlyHigh", kumaraswamyFunction(5f, 1f));


    /**
     * Moves like a sine wave does; starts slowly, rises quickly, then ends slowly.
     */
    public static final Interpolator sine = new Interpolator("sine", a -> (1f - TrigTools.cosTurns(0.5f * a)) * 0.5f);
    // when we check against libGDX, the above (approximate) and below (exact) both aren't equal to libGDX's
    // very-approximate code...
    //    public static final Interpolator sine = new Interpolator("sine", a -> (1f - (float) Math.cos(Math.PI * a)) * 0.5f);

    /**
     * Moves like a sine wave does; starts slowly and rises quickly.
     */
    public static final Interpolator sineIn = new Interpolator("sineIn", a -> (1f - TrigTools.cosTurns(0.25f * a)));
    /**
     * Moves like a sine wave does; starts quickly and slows down.
     */
    public static final Interpolator sineOut = new Interpolator("sineOut", a -> (TrigTools.sinTurns(0.25f * a)));

// This is here so that we can validate the old circle output against the new.
//    public static final Interpolator circleOld = new Interpolator("circle", a -> {
//        if (a <= 0.5f) {
//            a *= 2;
//            return (1 - (float)Math.sqrt(1 - a * a)) / 2;
//        }
//        a--;
//        a *= 2;
//        return ((float)Math.sqrt(1 - a * a) + 1) / 2;
//    });
//
    /**
     * When graphed, forms two circular arcs; it starts slowly, accelerating rapidly towards the middle, then slows down
     * towards the end.
     */
    public static final Interpolator circle = new Interpolator("circle", a -> (a <= 0.5f
            ? (1f - (float)Math.sqrt(1f - a * a * 4f)) * 0.5f
            : ((float)Math.sqrt(1f - 4f * (a * (a - 2f) + 1f)) + 1f) * 0.5f));
    /**
     * When graphed, forms one circular arc, starting slowly and accelerating at the end.
     */
    public static final Interpolator circleIn = new Interpolator("circleIn", a -> (1f - (float)Math.sqrt(1f - a * a)));
    /**
     * When graphed, forms one circular arc, starting rapidly and decelerating at the end.
     */
    public static final Interpolator circleOut = new Interpolator("circleOut", a -> ((float)Math.sqrt(a * (2f - a))));

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * Unlike {@link #bounceOutFunction(float...)}, this bounces at both the start and end of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceFunction(final float... pairs) {
        final InterpolationFunction bOut = bounceOutFunction(pairs), iOut = o -> {
            float test = o + pairs[0] * 0.5f;
            if (test < pairs[0]) return test / (pairs[0] * 0.5f) - 1f;
            return bOut.apply(o);
        };

        return a -> {
            if(a <= 0.5f) return (1f - iOut.apply(1f - a - a)) * 0.5f;
            return iOut.apply(a + a - 1) * 0.5f + 0.5f;
        };
    }

    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2 = new Interpolator("bounce2", bounceFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3 = new Interpolator("bounce3", bounceFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4 = new Interpolator("bounce4", bounceFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4} use 4 bounces, this matches the behavior of bounce in libGDX.
     */
    public static final Interpolator bounce = new Interpolator("bounce", bounceFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Accelerates and decelerates using {@link #bounceFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5 = new Interpolator("bounce5", bounceFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * This bounces at the end of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4Out}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceOutFunction(final float... pairs) {
        return a -> {
            a += pairs[0] * 0.5f;
            float width = 0f, height = 0f;
            for (int i = 0, n = (pairs.length & -2) - 1; i < n; i += 2) {
                width = pairs[i];
                if (a <= width) {
                    height = pairs[i + 1];
                    break;
                }
                a -= width;
            }
            float z = 4f / (width * width) * height * a;
            return 1f - (z * width - z * a);
            // pretty sure this is equivalent to the 2 lines above. Not certain.
//            a /= width;
//            float z = 4 / width * height * a;
//            return 1 - (z - z * a) * width;
        };
    }

    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2Out = new Interpolator("bounce2Out", bounceOutFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3Out = new Interpolator("bounce3Out", bounceOutFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4Out = new Interpolator("bounce4Out", bounceOutFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4Out} use 4 bounces, this matches the behavior of bounceOut in libGDX.
     */
    public static final Interpolator bounceOut = new Interpolator("bounceOut", bounceOutFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceOutFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5Out = new Interpolator("bounce5Out", bounceOutFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Produces an InterpolationFunction that uses the given {@code width, height, width, height, ...} float array.
     * This bounces at the start of its interpolation.
     * Fair warning; using this is atypically complicated, and you should generally stick to using a predefined
     * Interpolator, such as {@link #bounce4In}. You can also hand-edit the values in pairs; if you do, every even
     * index is a width, and every odd index is a height. Later widths are no greater than earlier ones; this is also
     * true for heights. No width is typically greater than 1.5f, and they are always positive and less than 2f.
     *
     * @param pairs width, height, width, height... in pairs; typically none are larger than 1.5f, and all are positive
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction bounceInFunction(final float... pairs) {
        final InterpolationFunction bOut = bounceOutFunction(pairs);
        return a -> 1f - bOut.apply(1f - a);
    }

    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 2 bounces.
     */
    public static final Interpolator bounce2In = new Interpolator("bounce2In", bounceInFunction(1.2f, 1f, 0.4f, 0.33f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 3 bounces.
     */
    public static final Interpolator bounce3In = new Interpolator("bounce3In", bounceInFunction(0.8f, 1f, 0.4f, 0.33f, 0.2f, 0.1f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 4 bounces.
     */
    public static final Interpolator bounce4In = new Interpolator("bounce4In", bounceInFunction(0.65f, 1f, 0.325f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 4 bounces. While both this and
     * {@link #bounce4In} use 4 bounces, this matches the behavior of bounceIn in libGDX.
     */
    public static final Interpolator bounceIn = new Interpolator("bounceIn", bounceInFunction(0.68f, 1f, 0.34f, 0.26f, 0.2f, 0.11f, 0.15f, 0.03f));
    /**
     * Decelerates using {@link #bounceInFunction(float...)}, with 5 bounces.
     */
    public static final Interpolator bounce5In = new Interpolator("bounce5In", bounceInFunction(0.61f, 1f, 0.31f, 0.45f, 0.21f, 0.3f, 0.11f, 0.15f, 0.06f, 0.06f));

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This drops below 0.0 at the start of the range, accelerates very rapidly, exceeds 1.0 at the middle of the input
     * range, and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingFunction(final float scale) {
        final float sc = scale + scale;
        return a -> {
            if (a <= 0.5f) return ((sc + 1f) * (a += a) - sc) * a * a * 0.5f;
            return ((sc + 1f) * (a += a - 2f) + sc) * a * a * 0.5f + 1f;
        };
    }
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2 = new Interpolator("swing2", swingFunction(2f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 1.5.
     */
    public static final Interpolator swing = new Interpolator("swing", swingFunction(1.5f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3 = new Interpolator("swing3", swingFunction(3f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75 = new Interpolator("swing0_75", swingFunction(0.75f));
    /**
     * Goes extra low, then extra-high, using {@link #swingFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5 = new Interpolator("swing0_5", swingFunction(0.5f));

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This accelerates very rapidly, exceeds 1.0 at the middle of the input range, and ends returning 1.0. Negative
     * parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingOutFunction(final float scale) {
        return a -> ((scale + 1f) * --a + scale) * a * a + 1f;
    }
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2Out = new Interpolator("swing2Out", swingOutFunction(2f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 2. This uses the same function as
     * {@link #swing2Out}.
     */
    public static final Interpolator swingOut = new Interpolator("swingOut", swingOutFunction(2f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3Out = new Interpolator("swing3Out", swingOutFunction(3f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75Out = new Interpolator("swing0_75Out", swingOutFunction(0.75f));
    /**
     * Goes extra-high, using {@link #swingOutFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5Out = new Interpolator("swing0_5Out", swingOutFunction(0.5f));

    /**
     * Produces an InterpolationFunction that uses the given scale variable.
     * This drops below 0.0 before the middle of the input range, later speeds up rapidly, and ends returning 1.0.
     * Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction swingInFunction(final float scale) {
        return a -> a * a * ((scale + 1f) * a - scale);
    }

    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 2.
     */
    public static final Interpolator swing2In = new Interpolator("swing2In", swingInFunction(2f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 2. This uses the same function as
     * {@link #swing2In}.
     */
    public static final Interpolator swingIn = new Interpolator("swingIn", swingInFunction(2f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 3.
     */
    public static final Interpolator swing3In = new Interpolator("swing3In", swingInFunction(3f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 0.75.
     */
    public static final Interpolator swing0_75In = new Interpolator("swing0_75In", swingInFunction(0.75f));
    /**
     * Goes extra-low, using {@link #swingInFunction(float)} and scale of 0.5.
     */
    public static final Interpolator swing0_5In = new Interpolator("swing0_5In", swingInFunction(0.5f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This drops below 0.0 near the middle of the range, accelerates near-instantly, exceeds 1.0 just after that,
     * and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction elasticFunction(final float value, final float power, final int bounces,
                                                        final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> (a <= 0.5f)
                ? (float)Math.pow(value, power * ((a += a) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f
                : 1f - (float)Math.pow(value, power * ((a = 2f - a - a) - 1f)) * TrigTools.sinTurns(a * bounce) * scale * 0.5f;
    }
    /**
     * Goes extra low, then extra-high, using {@link #elasticFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 7, and scale is 1.
     */
    public static final Interpolator elastic = new Interpolator("elastic", elasticFunction(2f, 10f, 7, 1f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This exceeds 1.0 just after the start of the range,
     * and ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction elasticOutFunction(final float value, final float power, final int bounces,
                                                        final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> (1f - (float)Math.pow(value, power * -a) * TrigTools.sinTurns(bounce - a * bounce) * scale);
    }
    /**
     * Goes extra-high near the start, using {@link #elasticOutFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 7, and scale is 1.
     */
    public static final Interpolator elasticOut = new Interpolator("elasticOut", elasticOutFunction(2f, 10f, 7, 1f));

    /**
     * Produces an InterpolationFunction that uses the given value, power, bounces, and scale variables.
     * This drops below 0.0 just before the end of the range,
     * but jumps up so that it ends returning 1.0. Negative parameters are not supported.
     * <br>
     * The functions this method produces are not well-behaved when their {@code a} parameter is less than 0 or greater
     * than 1.
     * @return an InterpolationFunction that will use the given configuration
     */
    public static InterpolationFunction elasticInFunction(final float value, final float power, final int bounces,
                                                           final float scale) {
        final float bounce = bounces * (0.5f - (bounces & 1));
        return a -> (float)Math.pow(value, power * (a - 1)) * TrigTools.sinTurns(a * bounce) * scale;
    }
    /**
     * Goes extra-low near the end, using {@link #elasticInFunction(float, float, int, float)}. Value is 2, power is
     * 10, bounces are 6, and scale is 1.
     */
    public static final Interpolator elasticIn = new Interpolator("elasticIn", elasticInFunction(2f, 10f, 6, 1f));
}
