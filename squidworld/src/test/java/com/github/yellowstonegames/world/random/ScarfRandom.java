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
 *
 */

package com.github.yellowstonegames.world.random;

import com.github.tommyettinger.random.EnhancedRandom;

public class ScarfRandom extends EnhancedRandom {
	@Override
	public String getTag() {
		return "SafR";
	}

	/**
	 * The first state; can be any odd-number long.
	 */
	protected long stateA;
	/**
	 * The second state; can be any long.
	 */
	protected long stateB;
	/**
	 * The third state; can be any long.
	 */
	protected long stateC;
	/**
	 * The fourth state; can be any long.
	 */
	protected long stateD;

	/**
	 * Creates a new ScarfRandom with a random state.
	 */
	public ScarfRandom() {
		stateA = EnhancedRandom.seedFromMath();
		stateB = EnhancedRandom.seedFromMath();
		stateC = EnhancedRandom.seedFromMath();
		stateD = EnhancedRandom.seedFromMath();
	}

	/**
	 * Creates a new ScarfRandom with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public ScarfRandom(long seed) {
		setSeed(seed);
	}

	/**
	 * Creates a new ScarfRandom with the given four states; all {@code long} values are permitted except that
	 * {@code stateA} must be odd, otherwise it will have 1 added to it to make it odd.
	 * Other than that, these states will be used verbatim.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any {@code long} value
	 * @param stateC any {@code long} value
	 * @param stateD any {@code long} value
	 */
	public ScarfRandom(long stateA, long stateB, long stateC, long stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	/**
	 * This generator has 4 {@code long} states, so this returns 4.
	 *
	 * @return 4 (four)
	 */
	@Override
	public int getStateCount () {
		return 4;
	}

	/**
	 * Gets the state determined by {@code selection}, as-is. The value for selection should be
	 * between 0 and 3, inclusive; if it is any other value this gets state D as if 3 was given.
	 *
	 * @param selection used to select which state variable to get; generally 0, 1, 2, or 3
	 * @return the value of the selected state
	 */
	@Override
	public long getSelectedState (int selection) {
		switch (selection) {
			case 0:
				return stateA;
			case 1:
				return stateB;
			case 2:
				return stateC;
			default:
				return stateD;
		}
	}

	/**
	 * Sets one of the states, determined by {@code selection}, to {@code value}, as-is.
	 * Selections 0, 1, 2, and 3 refer to states A, B, C, and D,  and if the selection is anything
	 * else, this treats it as 3 and sets stateD.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, or 3
	 * @param value     the exact value to use for the selected state, if valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		switch (selection) {
			case 0:
				stateA = value;
				break;
			case 1:
				stateB = value;
				break;
			case 2:
				stateC = value;
				break;
			default:
				stateD = value;
				break;
		}
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here, all with a different
	 * first value returned by {@link #nextLong()}.
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		seed ^= 0xEFA239AADFF080FFL; // somewhat-arbitrary choice from the array in MathTools.GOLDEN_LONGS
		stateA = seed;
		stateC = ~seed;
		seed ^= seed >>> 32;
		seed *= 0xBEA225F9EB34556DL;
		seed ^= seed >>> 29;
		seed *= 0xBEA225F9EB34556DL;
		seed ^= seed >>> 32;
		seed *= 0xBEA225F9EB34556DL;
		seed ^= seed >>> 29;
		stateB = seed ^ 0xC6BC279692B5C323L;
		stateD = seed ^ ~0xC6BC279692B5C323L;
	}

	public long getStateA () {
		return stateA;
	}

	/**
	 * Sets the first part of the state.
	 *
	 * @param stateA can be any long
	 */
	public void setStateA (long stateA) {
		this.stateA = stateA;
	}

	public long getStateB () {
		return stateB;
	}

	/**
	 * Sets the second part of the state.
	 *
	 * @param stateB can be any long
	 */
	public void setStateB (long stateB) {
		this.stateB = stateB;
	}

	public long getStateC () {
		return stateC;
	}

	/**
	 * Sets the third part of the state.
	 *
	 * @param stateC can be any long
	 */
	public void setStateC (long stateC) {
		this.stateC = stateC;
	}

	public long getStateD () {
		return stateD;
	}

	/**
	 * Sets the fourth part of the state.
	 *
	 * @param stateD can be any long
	 */
	public void setStateD (long stateD) {
		this.stateD = stateD;
	}

	/**
	 * Sets the state completely to the given four state variables.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group.
	 *
	 * @param stateA the first state; can be any long
	 * @param stateB the second state; can be any long
	 * @param stateC the third state; can be any long
	 * @param stateD the fourth state; can be any long
	 */
	@Override
	public void setState (long stateA, long stateB, long stateC, long stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	@Override
	public long nextLong () {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		stateA = fa + 0x9E3779B97F4A7C15L;
		stateB = fd * 0xD1342543DE82EF95L;
		stateC = fa + fb;
		stateD = (fc << 21 | fc >>> 43);
		return fc - fb;
	}

	@Override
	public long previousLong () {
		final long c = stateC;
		stateC = (stateD << 43 | stateD >>> 21);
		stateD = stateB * 0x572B5EE77A54E3BDL; // modular multiplicative inverse of 0xD1342543DE82EF95L
		stateB = c ^ (stateA -= 0x9E3779B97F4A7C15L);
		return stateC - stateB;
	}
	/**
	 * Jumps extremely far in the generator's sequence, such that one call to leap() advances the state as many as
	 * {@code Math.pow(2, 48)} calls to {@link #nextLong()}. This can be used to create 65536 substreams of this
	 * generator's sequence, each with a period of at least {@code Math.pow(2, 48)} but likely much more.
	 * @return the result of what nextLong() would return if it was called at the state this jumped to
	 */
	public long leap () {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		stateA = fa + 0x7C15000000000000L;
		stateB = fd * 0xD1342543DE82EF95L;
		stateC = fa + fb;
		stateD = (fc << 21 | fc >>> 43);
		return fc - fb;
	}

	@Override
	public int next (int bits) {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		stateA = fa + 0x9E3779B97F4A7C15L;
		stateB = fd * 0xD1342543DE82EF95L;
		stateC = fa + fb;
		stateD = (fc << 21 | fc >>> 43);
		return (int)(fc - fb) >>> (32 - bits);
	}

	@Override
	public ScarfRandom copy () {
		return new ScarfRandom(stateA, stateB, stateC, stateD);
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ScarfRandom that = (ScarfRandom)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC && stateD == that.stateD;
	}

	public String toString () {
		return "ScarfRandom{" + "stateA=" + (stateA) + "L, stateB=" + (stateB) + "L, stateC=" + (stateC) + "L, stateD=" + (stateD) + "L}";
	}
}
