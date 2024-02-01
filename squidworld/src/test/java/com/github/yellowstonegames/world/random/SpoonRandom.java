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

package com.github.yellowstonegames.world.random;

import com.github.tommyettinger.random.EnhancedRandom;

/**
 * 190 bits of state. Period is 2 to the 64.
 * There are 2 to the 126 possible streams.
 * Both streamB and streamC are always odd numbers,
 * which is why this has a slightly smaller state size.
 * <br>
 * I wanna change
 * your mind
 * Said I wanna set it right
 * this time
 * I'm looking through you
 * You know who you are
 */
public class SpoonRandom extends EnhancedRandom {
	@Override
	public String getTag() {
		return "SpnR";
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
	 * Creates a new SpoonRandom with a random state.
	 */
	public SpoonRandom() {
		stateA = EnhancedRandom.seedFromMath();
		stateB = EnhancedRandom.seedFromMath() | 1L;
		stateC = EnhancedRandom.seedFromMath() | 1L;
	}

	/**
	 * Creates a new SpoonRandom with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public SpoonRandom(long seed) {
		setSeed(seed);
	}

	/**
	 * Creates a new SpoonRandom with the given two states; all {@code long} values are permitted.
	 * These states will be used verbatim for stateA and stateB. stateC will be assigned 1.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any odd {@code long} value (if even, will be made odd)
	 */
	public SpoonRandom(long stateA, long stateB) {
		this.stateA = stateA;
		this.stateB = stateB | 1L;
		this.stateC = 1L;
	}

	/**
	 * Creates a new SpoonRandom with the given four states; all {@code long} values are permitted.
	 * These states will be used verbatim.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any odd {@code long} value (if even, will be made odd)
	 * @param stateC any odd {@code long} value (if even, will be made odd)
	 */
	public SpoonRandom(long stateA, long stateB, long stateC) {
		this.stateA = stateA;
		this.stateB = stateB | 1L;
		this.stateC = stateC | 1L;
	}

	/**
	 * This generator has 3 {@code long} states, so this returns 3.
	 *
	 * @return 3 (three)
	 */
	@Override
	public int getStateCount () {
		return 3;
	}

	/**
	 * Gets the state determined by {@code selection}, as-is. The value for selection should be
	 * between 0 and 2, inclusive; if it is any other value this gets state C as if 2 was given.
	 *
	 * @param selection used to select which state variable to get; generally 0, 1, or 2
	 * @return the value of the selected state
	 */
	@Override
	public long getSelectedState (int selection) {
		switch (selection) {
			case 0:
				return stateA;
			case 1:
				return stateB;
			default:
				return stateC;
		}
	}

	/**
	 * Sets one of the states, determined by {@code selection}, to {@code value}, as-is.
	 * Selections 0, 1, and 2 refer to states A, B, and C, and if the selection is anything
	 * else, this ignores it and sets nothing. States B and C are always odd, so if they would
	 * be set with an even number this adds 1 to value before setting.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, or 2
	 * @param value     the exact value to use for the selected state, if valid
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
		}
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here, though there are
	 * (2 to the 190) possible states in total.
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		stateA = seed;
		seed ^= seed >>> 32;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 29;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 32;
		seed *= 0xbea225f9eb34556dL;
		seed ^= seed >>> 29;
		stateB = (seed ^ 0xC6BC279692B5C323L) | 1L;
		stateC = (seed ^ ~0xC6BC279692B5C323L) | 1L;
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
	 * @param stateB can be any odd long (if even, will be made odd)
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
	 * @param stateC can be any odd long (if even, will be made odd)
	 */
	public void setStateC (long stateC) {
		this.stateC = stateC | 1L;
	}

	/**
	 * Equivalent to {@code setState(stateA, stateB, 1L)}.
	 *
	 * @param stateA the long value to use for stateA
	 * @param stateB the odd long value to use for stateB (if even, will be made odd)
	 */
	@Override
	public void setState(long stateA, long stateB) {
		setState(stateA, stateB, 1L);
	}

	/**
	 * Sets the state completely to the given four state variables.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * and {@link #setStateC(long)} as a group.
	 *
	 * @param stateA the first state; can be any long
	 * @param stateB the second state; can be any odd long (if even, will be made odd)
	 * @param stateC the third state; can be any odd long (if even, will be made odd)
	 */
	@Override
	public void setState (long stateA, long stateB, long stateC) {
		this.stateA = stateA;
		this.stateB = stateB | 1L;
		this.stateC = stateC | 1L;
	}

	@Override
	public long nextLong () {
		long x = (stateA += 0xDB4F0B9175AE2165L);
		x ^= x >>> 32;
		x *= (stateB += 0xBBE0563303A4615EL);
		x ^= x >>> 33;
		x *= (stateC += 0xA0F2EC75A1FE1576L);
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
		x ^= x >>> 31;
		stateA -= 0xDB4F0B9175AE2165L;
		stateB -= 0xBBE0563303A4615EL;
		stateC -= 0xA0F2EC75A1FE1576L;
		return x;
	}

	@Override
	public long skip(long advance) {
		long x = (stateA += 0xDB4F0B9175AE2165L * advance);
		x ^= x >>> 32;
		x *= (stateB += 0xBBE0563303A4615EL * advance);
		x ^= x >>> 33;
		x *= (stateC += 0xA0F2EC75A1FE1576L * advance);
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
		x ^= x >>> 31;
		return (int)x >>> (32 - bits);
	}


	@Override
	public SpoonRandom copy () {
		return new SpoonRandom(stateA, stateB, stateC);
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		SpoonRandom that = (SpoonRandom)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC;
	}

	public String toString () {
		return "SpoonRandom{" + "stateA=" + (stateA) + "L, stateB=" + (stateB) + "L, stateC=" + (stateC) + "L}";
	}

//	public static void main(String[] args) {
//		SpoonRandom random = new SpoonRandom(1L);
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
