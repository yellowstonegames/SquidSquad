/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.old.v300.squidmath;

import com.github.yellowstonegames.core.DigitTools;

import java.io.Serializable;

public class XoshiroStarPhi32RNG implements RandomnessSource, Serializable {

    private static final long serialVersionUID = 1L;

    private int stateA, stateB, stateC, stateD;

    /**
     * Creates a new generator seeded using four calls to Math.random().
     */
    public XoshiroStarPhi32RNG() {
        setState((int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000),
                (int)((Math.random() * 2.0 - 1.0) * 0x80000000), (int)((Math.random() * 2.0 - 1.0) * 0x80000000));
    }
    /**
     * Constructs this XoshiroStarPhi32RNG by dispersing the bits of seed using {@link #setSeed(int)} across the four
     * parts of state this has.
     * @param seed an int that won't be used exactly, but will affect all components of state
     */
    public XoshiroStarPhi32RNG(final int seed) {
        setSeed(seed);
    }
    /**
     * Constructs this XoshiroStarPhi32RNG by dispersing the bits of seed using {@link #setSeed(long)} across the four
     * parts of state this has.
     * @param seed a long that will be split across all components of state
     */
    public XoshiroStarPhi32RNG(final long seed) {
        setSeed(seed);
    }
    /**
     * Constructs this XoshiroStarPhi32RNG by calling {@link #setState(int, int, int, int)} on stateA and stateB as
     * given; see that method for the specific details (the states are kept as-is unless they are all 0).
     * @param stateA the number to use as the first part of the state; this will be 1 instead if both seeds are 0
     * @param stateB the number to use as the second part of the state
     * @param stateC the number to use as the third part of the state
     * @param stateD the number to use as the fourth part of the state
     */
    public XoshiroStarPhi32RNG(final int stateA, final int stateB, final int stateC, final int stateD) {
        setState(stateA, stateB, stateC, stateD);
    }

    @Override
    public int next(int bits) {
        final int result = stateB * 31;	        
        final int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
        return (result << 23 | result >>> 9) + 0x9E3779BD >>> (32 - bits);
    }

    /**
     * Can return any int, positive or negative, of any size permissible in a 32-bit signed integer.
     * @return any int, all 32 bits are random
     */
    public int nextInt() {
        final int result = stateB * 31;
        final int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
        return (result << 23 | result >>> 9) + 0x9E3779BD | 0;
    }

    @Override
    public long nextLong() {
        int result = stateB * 31;
        int t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        long high = (result << 23 | result >>> 9) + 0x9E3779BD;
        stateD = (stateD << 11 | stateD >>> 21);
        result = stateB * 31;
        t = stateB << 9;
        stateC ^= stateA;
        stateD ^= stateB;
        stateB ^= stateC;
        stateA ^= stateD;
        stateC ^= t;
        stateD = (stateD << 11 | stateD >>> 21);
        return high << 32 ^ ((result << 23 | result >>> 9) + 0x9E3779BD);
    }

    /**
     * Produces a copy of this RandomnessSource that, if next() and/or nextLong() are called on this object and the
     * copy, both will generate the same sequence of random numbers from the point copy() was called. This just needs to
     * copy the state so it isn't shared, usually, and produce a new value with the same exact state.
     *
     * @return a copy of this RandomnessSource
     */
    @Override
    public XoshiroStarPhi32RNG copy() {
        return new XoshiroStarPhi32RNG(stateA, stateB, stateC, stateD);
    }

    /**
     * Sets the state of this generator using one int, running it through a GWT-compatible variant of SplitMix32 four
     * times to get four ints of state, all guaranteed to be different.
     * @param seed the int to use to produce this generator's states
     */
    public void setSeed(final int seed) {
        int z = seed + 0xC74EAD55;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateA = z ^ (z >>> 16);
        z = seed + 0x8E9D5AAA;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateB = z ^ (z >>> 16);
        z = seed + 0x55EC07FF;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateC = z ^ (z >>> 16);
        z = seed + 0x1D3AB554;
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateD = z ^ (z >>> 16);
    }

    /**
     * Sets the state of this generator using one long, running it through a GWT-compatible variant of SplitMix32 four
     * times to get four ints of state, guaranteed to repeat a state no more than two times.
     * @param seed the long to use to produce this generator's states
     */
    public void setSeed(final long seed) {
        int z = (int)((seed & 0xFFFFFFFFL) + 0xC74EAD55);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateA = z ^ (z >>> 16);
        z = (int)((seed & 0xFFFFFFFFL) + 0x8E9D5AAA);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateB = z ^ (z >>> 16);
        z = (int)((seed >>> 32) + 0xC74EAD55);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateC = z ^ (z >>> 16);
        z = (int)((seed >>> 32) + 0x8E9D5AAA);
        z = (z ^ (z >>> 16)) * 0x85A6B;
        z = (z ^ (z >>> 13)) * 0xCAE35;
        stateD = z ^ (z >>> 16);
    }

    public int getStateA()
    {
        return stateA;
    }
    /**
     * Sets the first part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 instead. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateA any int
     */
    public void setStateA(int stateA)
    {
        this.stateA = (stateA | stateB | stateC | stateD) == 0 ? 1 : stateA;
    }
    public int getStateB()
    {
        return stateB;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateB to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateB any int
     */
    public void setStateB(int stateB)
    {
        this.stateB = stateB;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }
    public int getStateC()
    {
        return stateC;
    }

    /**
     * Sets the third part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateC to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateC any int
     */
    public void setStateC(int stateC)
    {
        this.stateC = stateC;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }
    
    public int getStateD()
    {
        return stateD;
    }

    /**
     * Sets the second part of the state to the given int. As a special case, if the parameter is 0 and this would set
     * all states to be 0, this will set stateA to 1 in addition to setting stateD to 0. Usually, you should use
     * {@link #setState(int, int, int, int)} to set all four states at once, but the result will be the same if you set
     * the four states individually.
     * @param stateD any int
     */
    public void setStateD(int stateD)
    {
        this.stateD = stateD;
        if((stateA | stateB | stateC | stateD) == 0) stateA = 1;
    }

    /**
     * Sets the current internal state of this XoshiroStarPhi32RNG with four ints, where each can be any int unless
     * they are all 0 (which will be treated as if stateA is 1 and the rest are 0).
     * @param stateA any int (if all parameters are both 0, this will be treated as 1)
     * @param stateB any int
     * @param stateC any int
     * @param stateD any int
     */
    public void setState(final int stateA, final int stateB, final int stateC, final int stateD)
    {
        this.stateA = (stateA | stateB | stateC | stateD) == 0 ? 1 : stateA;
        this.stateB = stateB;
        this.stateC = stateC;
        this.stateD = stateD;
    }
    
    @Override
    public String toString() {
        return "XoshiroStarPhi32RNG with stateA 0x" + DigitTools.hex(stateA) + ", stateB 0x" + DigitTools.hex(stateB)
                + ", stateC 0x" + DigitTools.hex(stateC) + ", and stateD 0x" + DigitTools.hex(stateD);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XoshiroStarPhi32RNG xoshiroStarPhi32RNG = (XoshiroStarPhi32RNG) o;

        return stateA == xoshiroStarPhi32RNG.stateA && stateB == xoshiroStarPhi32RNG.stateB &&
                stateC == xoshiroStarPhi32RNG.stateC && stateD == xoshiroStarPhi32RNG.stateD;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * (31 * stateA + stateB) + stateC) + stateD | 0;
    }
}
