package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.EnhancedRandom;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * An unusual RNG that's extremely fast on HotSpot JDK 16 and higher, and still fairly fast on earlier JDKs. It has
 * three {@code long} states, which as far as I can tell can be initialized to any values without hitting any known
 * problems for initialization. These states, a, b, and c, are passed around so b and c affect the next result for a,
 * but the previous value of a will only affect the next results for b and c, not a itself. The next values for b and c
 * involve a rotation with {@link Long#rotateLeft(long, int)} for one state, and an extra operation using another state
 * (b uses XOR, c uses add). The next value for a just adds {@code b + ~c}, which is equivalent to {@code b - 1 - c}.
 * This complicated transfer of states happens to be optimized very nicely by recent JVM versions (mostly for HotSpot,
 * but OpenJ9 also does well), since a, b, and c can all be updated in parallel instructions. It passes 64TB of
 * PractRand testing with no anomalies and also passes Dieharder.
 * <br>
 * Other useful traits of this generator are that it almost certainly has a longer period than you need for a game, and
 * that all values are permitted for the states (that we know of). It is possible that some initialization will put the
 * generator in a shorter-period subcycle, but the odds of this being a subcycle that's small enough to run out of
 * period during a game are effectively 0. It's also possible that the generator only has one cycle of length 2 to the
 * 192, though this doesn't seem likely.
 * <br>
 * This is closely related to Mark Overton's <a href="https://www.romu-random.org/">Romu generators</a>, specifically
 * RomuTrio, but this avoids multiplication. TricycleRandom is usually about as fast as RomuTrio or a little faster.
 * I have also called this generator HarpoRandom, and it's related to GrouchoRandom, both ARX generators (using only
 * add, rotate, and XOR operations or their equivalents).
 */
public class TricycleRandom extends Random implements EnhancedRandom {

    private long stateA, stateB, stateC;

    /**
     * Creates a new DistinctRandom with a random state.
     */
    public TricycleRandom() {
        super();
        stateA = super.nextLong();
        stateB = super.nextLong();
        stateC = super.nextLong();
    }

    /**
     * Creates a new DistinctRandom with the given seed; all {@code long} values are permitted.
     * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
     * @param seed any {@code long} value
     */
    public TricycleRandom(long seed) {
        super(seed);
        setSeed(seed);
    }

    /**
     * Creates a new DistinctRandom with the given three states; all {@code long} values are permitted.
     * These states will be used verbatim.
     * @param stateA any {@code long} value
     * @param stateB any {@code long} value
     * @param stateC any {@code long} value
     */
    public TricycleRandom(long stateA, long stateB, long stateC) {
        super(stateA + stateB ^ stateC);
        this.stateA = stateA;
        this.stateB = stateB;
        this.stateC = stateC;
    }

    @Override
    public int getStateCount() {
        return 3;
    }

    @Override
    public long getSelectedState(int selection) {
        switch (selection & 3) {
            case 0:
                return stateA;
            case 1:
                return stateB;
            default:
                return stateC;
        }
    }

    public long getStateA() {
        return stateA;
    }

    public void setStateA(long stateA) {
        this.stateA = stateA;
    }

    public long getStateB() {
        return stateB;
    }

    public void setStateB(long stateB) {
        this.stateB = stateB;
    }

    public long getStateC() {
        return stateC;
    }

    public void setStateC(long stateC) {
        this.stateC = stateC;
    }

    @Override
    public void setSelectedState(int selection, long value) {
        switch (selection & 3) {
            case 0:
                stateA = value;
            case 1:
                stateB = value;
            default:
                stateC = value;
        }
    }

    @Override
    public void setSeed(long seed) {
        long x = (seed += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateA = x ^ x >>> 27;
        x = (seed += 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateB = x ^ x >>> 27;
        x = (seed + 0x9E3779B97F4A7C15L);
        x ^= x >>> 27;
        x *= 0x3C79AC492BA7B653L;
        x ^= x >>> 33;
        x *= 0x1C69B3F74AC4AE35L;
        stateC = x ^ x >>> 27;
    }

    @Override
    public long nextLong() {
        final long a0 = stateA;
        final long b0 = stateB;
        final long c0 = stateC;
        stateA = b0 + ~c0;
        stateB = Long.rotateLeft(a0, 46) ^ c0;
        stateC = Long.rotateLeft(b0, 23) - a0;
        return a0 + b0;
    }

    @Override
    public int next(int bits) {
        final long a0 = stateA;
        final long b0 = stateB;
        final long c0 = stateC;
        stateA = b0 + ~c0;
        stateB = Long.rotateLeft(a0, 46) ^ c0;
        stateC = Long.rotateLeft(b0, 23) - a0;
        return (int) (a0 + b0) >>> (32 - bits);
    }

    @Nonnull
    @Override
    public TricycleRandom copy() {
        return new TricycleRandom(stateA, stateB, stateC);
    }

    @Override
    public void nextBytes(@Nonnull byte[] bytes) {
        EnhancedRandom.super.nextBytes(bytes);
    }

    @Override
    public String toString() {
        return "TricycleRandom{" +
                "stateA=0x" + DigitTools.hex(stateA) +
                "L, stateB=0x" + DigitTools.hex(stateB) +
                "L, stateC=0x" + DigitTools.hex(stateC) +
                "L}";
    }
}
