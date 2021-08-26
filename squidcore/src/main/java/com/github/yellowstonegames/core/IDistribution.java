package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.EnhancedRandom;

/**
 * A way to take an {@link EnhancedRandom} and get one or more random numbers from it to produce a float in some statistical
 * distribution, such as Gaussian (also called the normal distribution), exponential, or various simpler schemes that
 * don't have common mathematical names. An example of the last category is "spike" for a distribution that is very
 * likely to be 0 and quickly drops off to being less likely for positive or negative results between 0 and -1 or 1, or
 * "bathtub" for the "spike" distribution's fractional part from 0 to 1 (which is likely to be 0 or 1 and very unlikely
 * to be near 0.5).
 */
public interface IDistribution {
    /**
     * Gets a float between {@link #getLowerBound()} and {@link #getUpperBound()} that obeys this distribution.
     * @param rng an EnhancedRandom that this will get one or more random numbers from
     * @return a float within the range of {@link #getLowerBound()} and {@link #getUpperBound()}
     */
    double nextDouble(EnhancedRandom rng);

    /**
     * Gets the lower bound of the distribution. The documentation should specify whether the bound is inclusive or
     * exclusive; if unspecified, it can be assumed to be inclusive (like {@link EnhancedRandom#nextDouble()}).
     * @return the lower bound of the distribution
     */
    default float getLowerBound(){
        return 0f;
    }
    /**
     * Gets the upper bound of the distribution. The documentation should specify whether the bound is inclusive or
     * exclusive; if unspecified, it can be assumed to be exclusive (like {@link EnhancedRandom#nextDouble()}).
     * @return the upper bound of the distribution
     */
    default float getUpperBound(){
        return 1f;
    }
    
    abstract class SimpleDistribution implements IDistribution {

        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by getting the fractional component of a result from {@code otherDistribution}.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that gets the fractional part of {@code otherDistribution}.
         */
        public static SimpleDistribution fractionalDistribution(final IDistribution otherDistribution)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(EnhancedRandom rng) {
                    final double v = otherDistribution.nextDouble(rng);
                    return v - (v >= 0.0 ? (int) v : (int)v - 1);
                }
            };
        }

        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by getting the fractional component of {@code offset} plus a result from {@code otherDistribution}.
         * Using the offset allows distributions like {@link GaussianDistribution}, which are centered on 0.0, to become
         * centered halfway on 0.5, making the result of this distribution have a Gaussian-like peak on 0.5 instead of
         * peaking at the bounds when offset is 0.0.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that gets the fractional part of {@code otherDistribution}.
         */
        public static SimpleDistribution fractionalOffsetDistribution(final IDistribution otherDistribution, final float offset)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(EnhancedRandom rng) {
                    final double v = otherDistribution.nextDouble(rng) + offset;
                    return v - (v >= 0.0 ? (int) v : (int)v - 1);
                }
            };
        }

        /**
         * Makes a new SimpleDistribution implementation given any IDistribution (typically one with large or infinite
         * bounds) by simply clamping results that are below 0 to 0 and at least 1 to 0.9999999999999999 (the largest
         * float less than 1.0 than can be represented). This will behave very oddly for distributions that are
         * centered on 0.0; for those you probably want {@link #fractionalOffsetDistribution(IDistribution, float)}.
         * @param otherDistribution any other IDistribution
         * @return a new anonymous implementation of SimpleDistribution that clamps {@code otherDistribution} in range.
         */
        public static SimpleDistribution clampedDistribution(final IDistribution otherDistribution)
        {
            return new SimpleDistribution() {
                @Override
                public double nextDouble(EnhancedRandom rng) {
                    return Math.max(0.0, Math.min(0.9999999999999999, otherDistribution.nextDouble(rng)));
                }
            };
        }

        /**
         * Gets the lower inclusive bound, which is 0.0.
         *
         * @return the lower inclusive bound of the distribution, 0.0
         */
        @Override
        public float getLowerBound() {
            return 0f;
        }

        /**
         * Gets the upper exclusive bound of the distribution, which is 1.0.
         *
         * @return the upper exclusive bound of the distribution, 1.0
         */
        @Override
        public float getUpperBound() {
            return 1f;
        }
    }

    /**
     * On each call to getDouble(), this gets one Gaussian ("normally") distributed double value with mean 0.0 and
     * standard deviation 1. It simply calls {@link EnhancedRandom#nextGaussian()}. If you want to change the standard
     * deviation, multiply the result of nextDouble() by your desired standard deviation; if you then want to change the
     * mean, add your desired mean to that last result.
     */
    class GaussianDistribution implements IDistribution {
        public static final GaussianDistribution instance = new GaussianDistribution();

        @Override
        public double nextDouble(EnhancedRandom rng) {
            return rng.nextGaussian();
        }

        /**
         * The lower exclusive bound is technically negative infinity, but in practice is only -7.929080009460449 .
         * @return negative infinity
         */
        @Override
        public float getLowerBound() {
            return Float.NEGATIVE_INFINITY;
        }

        /**
         * The upper exclusive bound is technically positive infinity, but in practice is only 7.929080009460449 .
         * @return positive infinity
         */
        @Override
        public float getUpperBound() {
            return Float.POSITIVE_INFINITY;
        }
    }
    /**
     * An IDistribution that produces results between 0.0 inclusive and 1.0 exclusive, but is much more likely to produce
     * results near 0.0 or 1.0, further from 0.5.
     */
    class BathtubDistribution extends IDistribution.SimpleDistribution implements IDistribution {
        public static final BathtubDistribution instance = new BathtubDistribution();

        @Override
        public double nextDouble(EnhancedRandom rng) {
            double d = (rng.nextDouble() - 0.5) * 2.0;
            d = d * d * d + 1.0;
            return d - (int)d;
        }
    }

}
