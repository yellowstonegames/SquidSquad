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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.random.Deserializer;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.FourWheelRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.random.distribution.ContinuousUniformDistribution;
import com.github.tommyettinger.random.distribution.Distribution;

import javax.annotation.Nonnull;

/**
 * An EnhancedRandom that delegates to a {@link Distribution} to distribute any floats, ints, or doubles as by that
 * distribution.
 */
public class DistributedRandom extends EnhancedRandom {
    /**
     * One of two possible modes this can use to limit the range of a Distribution. {@code FRACTION} simply gets the
     * fractional part of a distribution by subtracting the floor from a result. {@code CLAMP} uses
     * {@link Math#min(double, double)} and {@link Math#max(double, double)} to cap the values at both ends, inclusive
     * on 0.0 and exclusive on 1.0 .
     */
    public enum ReductionMode {
        FRACTION {
            @Override
            public double applyAsDouble(double n) {
                return n - Math.floor(n);
            }
        },
        CLAMP {
            @Override
            public double applyAsDouble(double n) {
                return Math.min(Math.max(n, 0.0), 0.9999999999999999);
            }
        };

        ReductionMode() {
        }

        public abstract double applyAsDouble(double n);
    }
    private static final ReductionMode[] MODES = ReductionMode.values();
    @Override
    public String getTag() {
        return "DsrR";
    }

    protected Distribution distribution;

    protected ReductionMode reduction;

    public DistributedRandom() {
        distribution = new ContinuousUniformDistribution(0.0, 1.0);
        reduction = ReductionMode.FRACTION;
    }

    public DistributedRandom(long seed) {
        distribution = new ContinuousUniformDistribution(new WhiskerRandom(seed), 0.0, 1.0);
        reduction = ReductionMode.FRACTION;
    }

    public DistributedRandom(long stateA, long stateB, long stateC, long stateD) {
        distribution = new ContinuousUniformDistribution(new WhiskerRandom(stateA, stateB, stateC, stateD), 0.0, 1.0);
        reduction = ReductionMode.FRACTION;
    }

    public DistributedRandom(Distribution distribution, boolean useClamping) {
        this.distribution = distribution.copy();
        if(useClamping) reduction = ReductionMode.CLAMP;
        else reduction = ReductionMode.FRACTION;
    }

    public DistributedRandom(Distribution distribution, boolean useClamping, long seed) {
        this.distribution = distribution.copy();
        distribution.generator.setSeed(seed);
        if(useClamping) reduction = ReductionMode.CLAMP;
        else reduction = ReductionMode.FRACTION;

    }

    public DistributedRandom(Distribution distribution, boolean useClamping, long stateA, long stateB, long stateC, long stateD) {
        this.distribution = distribution.copy();
        this.distribution.generator = new WhiskerRandom(stateA, stateB, stateC, stateD);
        if(useClamping) reduction = ReductionMode.CLAMP;
        else reduction = ReductionMode.FRACTION;
    }

    public ReductionMode getReduction(){
        return reduction;
    }

    public void setReduction(ReductionMode reduction) {
        if(reduction != null)
            this.reduction = reduction;
    }

    public boolean isClamping() {
        return reduction == ReductionMode.CLAMP;
    }
    public void useClamping(boolean useClamping) {
        if(useClamping) reduction = ReductionMode.CLAMP;
        else reduction = ReductionMode.FRACTION;
    }
    @Override
    public long nextLong() {
        return (distribution.generator.getSelectedState(0) >>> 52) | ((long)(nextDouble() * 0x1p52) << 12);
    }

    @Override
    public int next(int bits) {
        return (int)(long)((1L << bits) * nextDouble());
    }

    @Override
    public double nextDouble() {
        return reduction.applyAsDouble(distribution.nextDouble());
    }

    @Override
    public float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public void nextBytes(@Nonnull byte[] bytes) {
        for (int i = 0; i < bytes.length; ) { for (int n = Math.min(bytes.length - i, 8); n-- > 0;) { bytes[i++] = (byte)(256 * nextDouble()); } }
    }

    @Override
    public int nextInt() {
        return (int)(long)(0x1p32 * nextDouble());
    }

    @Override
    public int nextInt(int bound) {
        return (int)(bound * nextDouble()) & ~(bound >> 31);
    }

    @Override
    public int nextSignedInt(int outerBound) {
        return (int)(outerBound * nextDouble());
    }

    @Override
    public boolean nextBoolean() {
        return nextDouble() < 0.5f;
    }

    /**
     * This runs {@link EnhancedRandom#probit(double)} on a distributed double this produces.
     * @return a "Gaussian-ized" result of {@link #nextDouble()}
     */
    @Override
    public double nextGaussian() {
        return EnhancedRandom.probit(nextDouble());
    }

    /**
     * This acts the same as {@link FourWheelRandom#nextExclusiveDoubleEquidistant()}; it does not use the optimizations
     * from {@link FourWheelRandom#nextExclusiveDouble()}, because those aren't reasonable when distributed.
     * @return a pseudo-random double between 0.0, exclusive, and 1.0, exclusive
     */
    @Override
    public double nextExclusiveDouble() {
        return (nextLong(0x1FFFFFFFFFFFFFL) + 1L) * 0x1p-53;
    }

    /**
     * This acts the same as {@link FourWheelRandom#nextExclusiveFloatEquidistant()}; it does not use the optimizations
     * from {@link FourWheelRandom#nextExclusiveFloat()}, because those aren't reasonable when distributed.
     * @return a pseudo-random float between 0.0, exclusive, and 1.0, exclusive
     */
    @Override
    public float nextExclusiveFloat() {
        return (nextInt(0xFFFFFF) + 1) * 0x1p-24f;
    }

    @Override
    @Nonnull
    public DistributedRandom copy() {
        return new DistributedRandom(distribution, reduction == ReductionMode.CLAMP);
    }

    @Override
    public void setState(long stateA) {
        distribution.generator.setState(stateA);
    }
    @Override
    public void setState(long stateA, long stateB) {
        distribution.generator.setState(stateA, stateB);
    }
    @Override
    public void setState(long stateA, long stateB, long stateC) {
        distribution.generator.setState(stateA, stateB, stateC);
    }
    @Override
    public void setState(long stateA, long stateB, long stateC, long stateD) {
        distribution.generator.setState(stateA, stateB, stateC, stateD);
    }

    @Override
    public int getStateCount() {
        return distribution.generator.getStateCount();
    }

    @Override
    public long getSelectedState(int selection) {
        return distribution.generator.getSelectedState(selection);
    }

    @Override
    public void setSelectedState(int selection, long value) {
        distribution.generator.setSelectedState(selection, value);
    }

    @Override
    public void setSeed(long seed) {
        if(distribution != null)
            distribution.generator.setSeed(seed);
    }

    public Distribution getDistribution() {
        return distribution;
    }

    public void setDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    public EnhancedRandom getRandom() {
        return distribution.generator;
    }

    public void setRandom(EnhancedRandom random) {
        this.distribution.generator = random;
    }

    /**
     * @param base which Base to use, from the "digital" library, such as {@link Base#BASE10}
     * @return this, for chaining
     */
    @Override
    public String stringSerialize(Base base) {
        return getTag() + "`" + reduction.ordinal() + "~" + distribution.stringSerialize(base);
    }

    /**
     * @param data a String probably produced by {@link #stringSerialize(Base)}
     * @param base which Base to use, from the "digital" library, such as {@link Base#BASE10}
     * @return this, for chaining
     */
    @Override
    public DistributedRandom stringDeserialize(String data, Base base) {
        int idx = data.indexOf('`');
        useClamping(base.readInt(data, idx + 1, (idx = data.indexOf('~', idx + 1))) != 0);
        distribution = Deserializer.deserializeDistribution(data.substring(idx + 1), base);
        return this;
    }
}
