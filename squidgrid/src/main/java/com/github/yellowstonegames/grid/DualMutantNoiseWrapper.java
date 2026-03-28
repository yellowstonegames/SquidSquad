/*
 * Copyright (c) 2020-2026 See AUTHORS file.
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
package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Collections;
import java.util.List;

/**
 * A wrapper around an {@link INoise} that allows two extra degrees of freedom to adjust the inputs by a small amount,
 * continuously rather than in jumps (as adjusting the seed would do). You can change the {@link #mutationA} and/or
 * {@link #mutationB} by any amount at a time, though usually it is a small change, and often the two mutations change
 * in a looping cycle. To make the mutations loop seamlessly, you can pass the same values to getNoise and only change
 * mutations, assigning one {@link TrigTools#sin(float)} and the other {@link TrigTools#cos(float)} with the same
 * angle. By changing the angle from 0 to {@link TrigTools#PI2} over one full loop, you can make the noise change
 * seamlessly.
 * <br>
 * Internally this works by calling the same method you call here on an internal INoise, {@link #basis}, but using
 * a higher dimension and passing {@link #mutationA} and {@link #mutationB} in as higher-dimensional inputs. This means
 * if you call {@link #getNoise(float, float)}, passing x and y, internally this calls
 * {@link INoise#getNoise(float, float, float, float)} and passes x, y, mutationA, and mutationB.
 * <br>
 * This defaults to using a FoamNoise for its basis INoise, which allows dimensions 2 through 5 to be produced with a
 * mutation value.
 */
@Beta
public class DualMutantNoiseWrapper implements INoise, ISerializersNeeded {
    /**
     * The INoise this uses for all its internal noise generation.
     * This defaults to a FoamNoise, but can be any INoise with {@link INoise#getMaxDimension()} greater than 3.
     */
    public INoise basis;
    /**
     * The first extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps." This defaults to 0.
     */
    public float mutationA;
    /**
     * The second extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps." This defaults to 0.
     */
    public float mutationB;

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with 1234567890L) and mutations both 0.0f .
     */
    public DualMutantNoiseWrapper() {
        this(1234567890L);
    }

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with the given seed) and mutations both 0.0f.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     */
    public DualMutantNoiseWrapper(long seed)
    {
        this(new FoamNoise(seed), 0f, 0f);
    }

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with the given seed) and the given mutations.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     * @param mutationA the initial value for the first extra input to be passed in addition to the inputs to each noise call
     * @param mutationB the initial value for the second extra input to be passed in addition to the inputs to each noise call
     */
    public DualMutantNoiseWrapper(long seed, float mutationA, float mutationB)
    {
        this(new FoamNoise(seed), mutationA, mutationB);
    }

    /**
     * Makes a MutantNoise with the given basis INoise and mutations both 0.0f.
     * @param base the INoise to use as a basis
     */
    public DualMutantNoiseWrapper(INoise base)
    {
        this(base, 0f, 0f);
    }

    /**
     * Makes a MutantNoise with the given basis INoise and mutation.
     * @param base the INoise to use as a basis
     * @param mutationA the initial value for the first extra input to be passed in addition to the inputs to each noise call
     * @param mutationB the initial value for the second extra input to be passed in addition to the inputs to each noise call
     */
    public DualMutantNoiseWrapper(INoise base, float mutationA, float mutationB)
    {
        this.basis = base;
        this.mutationA = mutationA;
        this.mutationB = mutationB;
    }

    /**
     * Minimum dimension is 2, unless the basis INoise has a minimum dimension of 4 or greater.
     * With the default basis, a FoamNoise, this will be 2.
     * @return 2, in almost all cases
     */
    @Override
    public int getMinDimension() {
        return Math.min(basis.getMaxDimension() - 1, Math.max(2, basis.getMinDimension() - 1));
    }

    /**
     * Maximum dimension is one less than the maximum dimension of the basis INoise. With the default basis, a
     * FoamNoise, this will be 6.
     * @return one less than {@code basis.getMaxDimension()}
     */
    @Override
    public int getMaxDimension() {
        return basis.getMaxDimension() - 1;
    }

    /**
     * Setting the seed is efficient here if and only if it is efficient for the basis INoise.
     * @return {@code basis.hasEfficientSetSeed()}
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return basis.hasEfficientSetSeed();
    }

    @Override
    public float getNoise(float x, float y) {
        return basis.getNoise(x, y, mutationA);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return basis.getNoise(x, y, z, mutationA);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return basis.getNoise(x, y, z, w, mutationA);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return basis.getNoise(x, y, z, w, u, mutationA);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return basis.getNoise(x, y, z, w, u, v, mutationA);
    }

    @Override
    public void setSeed(long seed) {
        basis.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return basis.getSeed();
    }

    /**
     * Gets the mutation value.
     * The mutation is an extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     */
    public float getMutationA() {
        return mutationA;
    }

    /**
     * Sets the mutation value.
     * The mutation is an extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     * @param mutationA the float value to use for the mutation
     */
    public void setMutationA(float mutationA) {
        this.mutationA = mutationA;
    }

    /**
     * Gets the second mutation value.
     * This is the second extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     */
    public float getMutationB() {
        return mutationB;
    }

    /**
     * Sets the second mutation value.
     * This is the second extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     * @param mutationB the float value to use for the second mutation
     */
    public void setMutationB(float mutationB) {
        this.mutationB = mutationB;
    }

    /**
     * The String "DMut" .
     * @return "DMut"
     */
    @Override
    public String getTag() {
        return "DMut";
    }

    @Override
    public String stringSerialize() {
        return "`" + mutationA + "~" + mutationB + "~" + Serializer.serialize(basis) + "`";
    }

    @Override
    public DualMutantNoiseWrapper stringDeserialize(String data) {
        int idx = 2;
        mutationA = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        mutationB = Base.BASE10.readFloat(data, idx+1, idx = data.indexOf('~', idx));
        basis = Serializer.deserialize(data.substring(idx+1, data.lastIndexOf('`')-1));
        return this;
    }

    public static DualMutantNoiseWrapper recreateFromString(String data) {
        int idx = 2;
        float mutationA = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        float mutationB = Base.BASE10.readFloat(data, idx+1, idx = data.indexOf('~', idx));
        INoise basis = Serializer.deserialize(data.substring(idx+1, data.lastIndexOf('`')-1));
        return new DualMutantNoiseWrapper(basis, mutationA, mutationB);

    }

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Collections.singletonList(basis.getClass());
    }

    @Override
    public DualMutantNoiseWrapper copy() {
        return new DualMutantNoiseWrapper(basis.copy(), mutationA, mutationB);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return basis.getNoiseWithSeed(x, y, mutationA, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return basis.getNoiseWithSeed(x, y, z, mutationA, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, mutationA, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, u, mutationA, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, u, v, mutationA, seed);
    }
}
