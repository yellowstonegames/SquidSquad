package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * An EnhancedRandom that delegates to a {@link FourWheelRandom}, but runs it through a user-controlled
 * {@link IDistribution.SimpleDistribution} to distribute any floats, ints, or doubles as by that distribution.
 * Generating random long values is trickier because a distribution works on doubles, and doubles only provide 52 bits
 * of usable random data, while a long can need as many as 64.
 */
public class DistributedRandom extends Random implements EnhancedRandom {

    protected IDistribution.SimpleDistribution distribution;
    protected FourWheelRandom random;

    public DistributedRandom() {
        this(IDistribution.UniformDistribution.instance);
    }

    public DistributedRandom(long seed) {
        this(IDistribution.UniformDistribution.instance, seed);
    }

    public DistributedRandom(long stateA, long stateB, long stateC, long stateD) {
        this(IDistribution.UniformDistribution.instance, stateA, stateB, stateC, stateD);
    }

    public DistributedRandom(IDistribution.SimpleDistribution distribution) {
        random = new FourWheelRandom();
        this.distribution = distribution;
    }

    public DistributedRandom(IDistribution.SimpleDistribution distribution, long seed) {
        random = new FourWheelRandom(seed);
        this.distribution = distribution;
    }

    public DistributedRandom(IDistribution.SimpleDistribution distribution, long stateA, long stateB, long stateC, long stateD) {
        random = new FourWheelRandom(stateA, stateB, stateC, stateD);
        this.distribution = distribution;
    }

    @Override
    public long nextLong() {
        return (random.getStateB() >>> 52) | ((long)(distribution.nextDouble(random) * 0x1p52) << 12);
    }

    @Override
    public int next(int bits) {
        return (int)(long)((1L << bits) * distribution.nextDouble(random));
    }

    @Override
    public double nextDouble() {
        return distribution.nextDouble(random);
    }

    @Override
    public float nextFloat() {
        return (float) distribution.nextDouble(random);
    }

    @Override
    public void nextBytes(@Nonnull byte[] bytes) {
        for (int i = 0; i < bytes.length; ) { for (int n = Math.min(bytes.length - i, 8); n-- > 0;) { bytes[i++] = (byte)(256 * distribution.nextDouble(random)); } }
    }

    @Override
    public int nextInt() {
        return (int)(long)(0x1p32 * distribution.nextDouble(random));
    }

    @Override
    public int nextInt(int bound) {
        return (int)(bound * distribution.nextDouble(random)) & ~(bound >> 31);
    }

    @Override
    public int nextSignedInt(int outerBound) {
        return (int)(outerBound * distribution.nextDouble(random));
    }

    @Override
    public boolean nextBoolean() {
        return distribution.nextDouble(random) < 0.5f;
    }

    /**
     * Because nextGaussian() already specifies a distribution, this uses {@link FourWheelRandom#nextGaussian()} as-is.
     * @return the next pseudorandom, approximately Gaussian ("normally") distributed double value with mean 0.0 and
     *         standard deviation 1.0 from this random number generator's sequence
     */
    @Override
    public double nextGaussian() {
        return random.nextGaussian();
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
        return new DistributedRandom(distribution, random.getStateA(), random.getStateB(), random.getStateC(), random.getStateD());
    }

    public long getStateA() {
        return random.getStateA();
    }

    public void setStateA(long stateA) {
        random.setStateA(stateA);
    }

    public long getStateB() {
        return random.getStateB();
    }

    public void setStateB(long stateB) {
        random.setStateB(stateB);
    }

    public long getStateC() {
        return random.getStateC();
    }

    public void setStateC(long stateC) {
        random.setStateC(stateC);
    }

    public long getStateD() {
        return random.getStateD();
    }

    public void setStateD(long stateD) {
        random.setStateD(stateD);
    }

    @Override
    public void setState(long stateA, long stateB, long stateC, long stateD) {
        random.setState(stateA, stateB, stateC, stateD);
    }

    @Override
    public int getStateCount() {
        return 4;
    }

    @Override
    public long getSelectedState(int selection) {
        return random.getSelectedState(selection);
    }

    @Override
    public void setSelectedState(int selection, long value) {
        random.setSelectedState(selection, value);
    }

    @Override
    public void setSeed(long seed) {
        random.setSeed(seed);
    }

    public IDistribution.SimpleDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(IDistribution.SimpleDistribution distribution) {
        this.distribution = distribution;
    }

    public FourWheelRandom getRandom() {
        return random;
    }

    public void setRandom(FourWheelRandom random) {
        this.random = random;
    }
}