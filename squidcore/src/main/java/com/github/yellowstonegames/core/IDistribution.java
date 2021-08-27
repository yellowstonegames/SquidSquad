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
    default double getLowerBound(){
        return 0.0;
    }
    /**
     * Gets the upper bound of the distribution. The documentation should specify whether the bound is inclusive or
     * exclusive; if unspecified, it can be assumed to be exclusive (like {@link EnhancedRandom#nextDouble()}).
     * @return the upper bound of the distribution
     */
    default double getUpperBound(){
        return 1.0;
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
        public double getLowerBound() {
            return 0.0;
        }

        /**
         * Gets the upper exclusive bound of the distribution, which is 1.0.
         *
         * @return the upper exclusive bound of the distribution, 1.0
         */
        @Override
        public double getUpperBound() {
            return 1.0;
        }
    }

    /**
     * The simplest possible distribution; this returns every possible double in its range with approximately equal
     * likelihood. Its range is the same as {@link EnhancedRandom#nextDouble()}: inclusive on 0, exclusive on 1.
     */
    class UniformDistribution extends SimpleDistribution {
        @Override
        public double nextDouble(EnhancedRandom rng) {
            return rng.nextDouble();
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
        public double getLowerBound() {
            return Double.NEGATIVE_INFINITY;
        }

        /**
         * The upper exclusive bound is technically positive infinity, but in practice is only 7.929080009460449 .
         * @return positive infinity
         */
        @Override
        public double getUpperBound() {
            return Double.POSITIVE_INFINITY;
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
    /**
     * An IDistribution that allows a parameter to determine how many calls to {@link EnhancedRandom#nextDouble()} to make and average
     * whenever a double is requested. When this parameter {@code degree} is 1, this is uniform; when it is 2, this is a
     * triangular distribution, and when it is 3 or more it is an increasingly centralized bell curve. The average value is
     * always very close to 0.5, and the bounds are inclusive on 0.0, exclusive on 1.0 .
     */
    class CurvedBoundedDistribution extends IDistribution.SimpleDistribution implements IDistribution {
        public static final CurvedBoundedDistribution instance = new CurvedBoundedDistribution(3);
        public static final CurvedBoundedDistribution instanceTriangular = new CurvedBoundedDistribution(2);
        public static final CurvedBoundedDistribution instanceGaussianLike = new CurvedBoundedDistribution(6);
        private int degree;
        private double i_degree;
        public CurvedBoundedDistribution()
        {
            this(3);
        }
        public CurvedBoundedDistribution(int degree)
        {
            this.degree = Math.max(degree, 1);
            i_degree = 1.0 / this.degree;
        }

        public int getDegree() {
            return degree;
        }

        public void setDegree(int degree) {
            this.degree = Math.max(degree, 1);
            i_degree = 1.0 / this.degree;
        }

        @Override
        public double nextDouble(EnhancedRandom rng) {
            double sum = 0.0;
            for (int i = 0; i < degree; i++) {
                sum += rng.nextDouble();
            }
            return sum * i_degree;
        }
    }
    /**
     * An IDistribution that implements the <a href="https://en.wikipedia.org/wiki/Exponential_distribution">Exponential
     * distribution</a>. Takes lambda as a parameter during construction (default 1), and lambda also has
     * getters/setters. Note that lambda is just the parameter name, and doesn't have anything to do with Java lambdas.
     */
    class ExponentialDistribution implements IDistribution {
        public static final ExponentialDistribution instance = new ExponentialDistribution();
        public static final ExponentialDistribution instance_0_5 = new ExponentialDistribution(0.5);
        public static final ExponentialDistribution instance_1_5 = new ExponentialDistribution(1.5);
        private double i_lambda;
        public ExponentialDistribution()
        {
            i_lambda = 1.0;
        }
        public ExponentialDistribution(double lambda)
        {
            i_lambda = 1.0 / lambda;
        }

        public double getLambda() {
            return 1.0 / i_lambda;
        }

        public void setLambda(double lambda) {
            this.i_lambda = 1.0 / lambda;
        }

        @Override
        public double nextDouble(EnhancedRandom rng) {
            return Math.log(rng.nextExclusiveDouble()) * i_lambda;
        }
        /**
         * The lower exclusive bound is 0 while lambda is positive; it is negative infinity if lambda is negative.
         * @return zero, or negative infinity if lambda is negative.
         */
        @Override
        public double getLowerBound() {
            return i_lambda < 0.0 ? Double.NEGATIVE_INFINITY : 0.0;
        }

        /**
         * The upper exclusive bound is infinity while lambda is positive; it is 0 if lambda is negative.
         * @return positive infinity, or zero if lambda is negative.
         */
        @Override
        public double getUpperBound() {
            return i_lambda < 0.0 ? 0.0 : Double.POSITIVE_INFINITY;
        }
    }
    /**
     * An IDistribution that produces results between -1.0 inclusive and 1.0 exclusive, but is much more likely to produce
     * results near 0.0, and does not "round off" like a Gaussian curve around the midpoint.
     */
    class SpikeDistribution implements IDistribution {
        public static final SpikeDistribution instance = new SpikeDistribution();

        @Override
        public double nextDouble(EnhancedRandom rng) {
            final double d = (rng.nextDouble() - 0.5) * 2.0;
            return d * d * d;
        }

        /**
         * Gets the lower bound of the distribution, which is -1, inclusive.
         * @return the lower bound of the distribution
         */
        @Override
        public double getLowerBound() {
            return -1.0;
        }

        /**
         * Gets the upper bound of the distribution, which is 1, exclusive.
         *
         * @return the upper bound of the distribution
         */
        @Override
        public double getUpperBound() {
            return 1.0;
        }

    }

    /**
     * A variant on SpikeDistribution that has its range shrunk and moved from {@code [-1,1)} to {@code [0,1)}. It is a
     * {@link SimpleDistribution}, and the spike is centered on 0.5.
     */
    class SimpleSpikeDistribution extends SimpleDistribution implements IDistribution
    {
        public static final SimpleSpikeDistribution instance = new SimpleSpikeDistribution();

        @Override
        public double nextDouble(EnhancedRandom rng) {
            final double d = (rng.nextDouble() - 0.5) * 2.0;
            return d * d * d * 0.5 + 0.5;
        }
    }
}
