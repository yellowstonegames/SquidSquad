package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;

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
        return (int)((1L << bits) * distribution.nextDouble(random));
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
}
