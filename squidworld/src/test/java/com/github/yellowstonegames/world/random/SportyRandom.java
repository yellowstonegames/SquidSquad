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

/**
 * 253 bits of state. Period is 2 to the 64.
 * There are 2 to the 189 possible streams.
 * Each of streamB, streamC, and streamD are always odd numbers,
 * which is why this has a slightly smaller state size.
 * <br>
 * Maybe this is overkill. {@link SpoonRandom} is like this, but
 * "only" has 190 bits of state.
 */
public class SportyRandom extends EnhancedRandom {
	@Override
	public String getTag() {
		return "SpoR";
	}

	/**
	 * The first state; can be any long.
	 */
	protected long stateA;
	/**
	 * The second state; can be any odd long.
	 */
	protected long stateB;
	/**
	 * The third state; can be any odd long.
	 */
	protected long stateC;
	/**
	 * The fourth state; can be any odd long.
	 */
	protected long stateD;

	/**
	 * Creates a new SportyRandom with a random state.
	 */
	public SportyRandom() {
		stateA = EnhancedRandom.seedFromMath();
		stateB = EnhancedRandom.seedFromMath() | 1L;
		stateC = EnhancedRandom.seedFromMath() | 1L;
		stateD = EnhancedRandom.seedFromMath() | 1L;
	}

	/**
	 * Creates a new SportyRandom with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public SportyRandom(long seed) {
		setSeed(seed);
	}

	/**
	 * Creates a new SportyRandom with the given two states; all {@code long} values are permitted.
	 * These states will be used verbatim for stateA and stateB. Both stateC and stateD will be assigned 1.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any odd {@code long} value
	 */
	public SportyRandom(long stateA, long stateB) {
		this.stateA = stateA;
		this.stateB = stateB | 1L;
		this.stateC = 1L;
		this.stateD = 1L;
	}

	/**
	 * Creates a new SportyRandom with the given four states; all {@code long} values are permitted.
	 * These states will be used verbatim.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any odd {@code long} value
	 * @param stateC any odd {@code long} value
	 * @param stateD any odd {@code long} value
	 */
	public SportyRandom(long stateA, long stateB, long stateC, long stateD) {
		this.stateA = stateA;
		this.stateB = stateB | 1L;
		this.stateC = stateC | 1L;
		this.stateD = stateD | 1L;
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
	 * Selections 0, 1, 2, and 3 refer to states A, B, C, and D, and if the selection is anything
	 * else, this ignores it and sets nothing. States B, C, and D are always odd, so if they would
	 * be set with an even number this adds 1 to value before setting.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, or 3
	 * @param value     the value to use for the selected state, if valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		switch (selection) {
		case 0:
			stateA = value;
			break;
		case 1:
			stateB = value | 1L;
			break;
		case 2:
			stateC = value | 1L;
			break;
		case 3:
			stateD = value | 1L;
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
		stateA = seed ^ 0xC6BC279692B5C323L;
		stateB = (seed ^ ~0xC6BC279692B5C323L) | 1L;
		seed ^= seed >>> 32;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 29;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 32;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 29;
		stateC = ~seed | 1L;
		stateD = seed | 1L;
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
		this.stateB = stateB | 1L;
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
		this.stateC = stateC | 1L;
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
		this.stateD = stateD | 1L;
	}

	/**
	 * Sets each state variable to either {@code stateA} or {@code stateB}, alternating.
	 * This uses {@link #setSelectedState(int, long)} to set the values. If there is one
	 * state variable ({@link #getStateCount()} is 1), then this only sets that state
	 * variable to stateA. If there are two state variables, the first is set to stateA,
	 * and the second to stateB. If there are more, it reuses stateA, then stateB, then
	 * stateA, and so on until all variables are set.
	 *
	 * @param stateA the long value to use for states at index 0, 2, 4, 6...
	 * @param stateB the long value to use for states at index 1, 3, 5, 7...
	 */
	@Override
	public void setState(long stateA, long stateB) {
		setState(stateA, stateB, 1L, 1L);
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
		this.stateB = stateB | 1L;
		this.stateC = stateC | 1L;
		this.stateD = stateD | 1L;
	}

	@Override
	public long nextLong () {
		long x = (stateA += 0xDB4F0B9175AE2165L);
		x ^= x >>> 32;
		x *= (stateB += 0xBBE0563303A4615EL);
		x ^= x >>> 33;
		x *= (stateC += 0xA0F2EC75A1FE1576L);
		x ^= x >>> 32;
		x *= (stateD += 0x89E182857D9ED68AL);
		x ^= x >>> 31;
		return x;
	}

	@Override
	public long previousLong () {
		long x = (stateA);
		x ^= x >>> 32;
		x *= (stateB);
		x ^= x >>> 33;
		x *= (stateC);
		x ^= x >>> 32;
		x *= (stateD);
		x ^= x >>> 31;
		stateA -= 0xDB4F0B9175AE2165L;
		stateB -= 0xBBE0563303A4615EL;
		stateC -= 0xA0F2EC75A1FE1576L;
		stateD -= 0x89E182857D9ED68AL;
		return x;
	}

	@Override
	public long skip(long advance) {
		long x = (stateA += 0xDB4F0B9175AE2165L * advance);
		x ^= x >>> 32;
		x *= (stateB += 0xBBE0563303A4615EL * advance);
		x ^= x >>> 33;
		x *= (stateC += 0xA0F2EC75A1FE1576L * advance);
		x ^= x >>> 32;
		x *= (stateD += 0x89E182857D9ED68AL * advance);
		x ^= x >>> 31;
		return x;
	}

	@Override
	public int next (int bits) {
		long x = (stateA += 0xDB4F0B9175AE2165L);
		x ^= x >>> 32;
		x *= (stateB += 0xBBE0563303A4615EL);
		x ^= x >>> 33;
		x *= (stateC += 0xA0F2EC75A1FE1576L);
		x ^= x >>> 32;
		x *= (stateD += 0x89E182857D9ED68AL);
		x ^= x >>> 31;
		return (int)x >>> (32 - bits);
	}

	@Override
	public SportyRandom copy () {
		return new SportyRandom(stateA, stateB, stateC, stateD);
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SportyRandom that = (SportyRandom)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC && stateD == that.stateD;
	}

	public String toString () {
		return "SportyRandom{" + "stateA=" + (stateA) + "L, stateB=" + (stateB) + "L, stateC=" + (stateC) + "L, stateD=" + (stateD) + "L}";
	}

//	public static void main(String[] args) {
//		SportyRandom random = new SportyRandom(1L);
//		long n0 = random.nextLong();
//		long n1 = random.nextLong();
//		long n2 = random.nextLong();
//		long n3 = random.nextLong();
//		long n4 = random.nextLong();
//		long n5 = random.nextLong();
//		long n6 = random.nextLong();
//		long p5 = random.previousLong();
//		long p4 = random.previousLong();
//		long p3 = random.previousLong();
//		long p2 = random.previousLong();
//		long p1 = random.previousLong();
//		long p0 = random.previousLong();
//		System.out.println(n0 == p0);
//		System.out.println(n1 == p1);
//		System.out.println(n2 == p2);
//		System.out.println(n3 == p3);
//		System.out.println(n4 == p4);
//		System.out.println(n5 == p5);
//		System.out.println(n0 + " vs. " + p0);
//		System.out.println(n1 + " vs. " + p1);
//		System.out.println(n2 + " vs. " + p2);
//		System.out.println(n3 + " vs. " + p3);
//		System.out.println(n4 + " vs. " + p4);
//		System.out.println(n5 + " vs. " + p5);
//	}
}
