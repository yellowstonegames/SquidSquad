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
import com.github.tommyettinger.digital.BitConversion;
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
     * Minimum dimension is 2, unless the basis INoise has a minimum dimension of 5 or greater.
     * With the default basis, a FoamNoise, this will be 2.
     * @return 2, in almost all cases
     */
    @Override
    public int getMinDimension() {
        return Math.min(basis.getMaxDimension() - 2, Math.max(2, basis.getMinDimension() - 2));
    }

    /**
     * Maximum dimension is two less than the maximum dimension of the basis INoise. With the default basis, a
     * FoamNoise, this will be 5.
     * @return two less than {@code basis.getMaxDimension()}
     */
    @Override
    public int getMaxDimension() {
        return basis.getMaxDimension() - 2;
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
        return basis.getNoise(x, y, mutationA, mutationB);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return basis.getNoise(x, y, z, mutationA, mutationB);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return basis.getNoise(x, y, z, w, mutationA, mutationB);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return basis.getNoise(x, y, z, w, u, mutationA, mutationB);
    }

    /**
     * Not supported.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return never returns
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        throw new UnsupportedOperationException("Generating 6D noise with DualMutantNoiseWrapper is not supported.");
    }

    /**
     * Not supported.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @param m m position; can be any finite float
     * @return never returns
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v, float m) {
        throw new UnsupportedOperationException("Generating 7D noise with DualMutantNoiseWrapper is not supported.");
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
     *
     * @return the first mutation value
     */
    public float getMutationA() {
        return mutationA;
    }

    /**
     * Sets the mutation value.
     * The mutation is an extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     *
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
     *
     * @return the second mutation value
     */
    public float getMutationB() {
        return mutationB;
    }

    /**
     * Sets the second mutation value.
     * This is the second extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     *
     * @param mutationB the float value to use for the second mutation
     */
    public void setMutationB(float mutationB) {
        this.mutationB = mutationB;
    }

    /**
     * Gets the magnitude of the circle that contains the current mutation values.
     *
     * @return the Euclidean distance from 0,0 to mutationA,mutationB
     */
    public float getMutationMagnitude() {
        return (float) Math.sqrt(mutationA * mutationA + mutationB * mutationB);
    }

    /**
     * Gets the squared magnitude of the circle that contains the current mutation values.
     *
     * @return the squared Euclidean distance from 0,0 to mutationA,mutationB
     */
    public float getMutationMagnitudeSquared() {
        return (mutationA * mutationA + mutationB * mutationB);
    }

    /**
     * Gets the angle in radians of the current mutation values.
     *
     * @return the angle in radians
     */
    public float getMutationAngle() {
        return TrigTools.atan2Finite(mutationB, mutationA);
    }

    /**
     * Gets the angle in degrees, from -180 to 180, of the current mutation values.
     *
     * @return the angle in degrees
     */
    public float getMutationAngleDegrees() {
        return TrigTools.atan2DegFinite(mutationB, mutationA);
    }

    /**
     * Gets the angle in degrees, from 0 to 360, of the current mutation values.
     *
     * @return the angle in degrees
     */
    public float getMutationAngleDegrees360() {
        return TrigTools.atan2Deg360Finite(mutationB, mutationA);
    }

    /**
     * Gets the angle in turns, from 0 to 1.0, of the current mutation values.
     *
     * @return the angle in turns
     */
    public float getMutationAngleTurns() {
        return TrigTools.atan2TurnsFinite(mutationB, mutationA);
    }

    /**
     * Sets both mutation values so they are on a circle with the given magnitude at the given angle, in radians.
     * If the magnitude does not change and the angle makes a full circle, the noise produced will loop seamlessly.
     * Larger magnitude values will make changes to the angle more significant.
     *
     * @param angleRadians the angle on the circle, in radians
     * @param magnitude the radius of the circle; larger values will change more for the same change in angle
     */
    public void setMutationAngle(float angleRadians, float magnitude) {
        mutationA = TrigTools.cos(angleRadians) * magnitude;
        mutationB = TrigTools.sin(angleRadians) * magnitude;
    }

    /**
     * Sets both mutation values so they are on a circle with the given magnitude at the given angle, in degrees.
     * If the magnitude does not change and the angle makes a full circle, the noise produced will loop seamlessly.
     * Larger magnitude values will make changes to the angle more significant.
     *
     * @param angleDegrees the angle on the circle, in degrees
     * @param magnitude the radius of the circle; larger values will change more for the same change in angle
     */
    public void setMutationAngleDegrees(float angleDegrees, float magnitude) {
        mutationA = TrigTools.cosDeg(angleDegrees) * magnitude;
        mutationB = TrigTools.sinDeg(angleDegrees) * magnitude;
    }

    /**
     * Sets both mutation values so they are on a circle with the given magnitude at the given angle, in turns.
     * If the magnitude does not change and the angle makes a full circle, the noise produced will loop seamlessly.
     * Larger magnitude values will make changes to the angle more significant.
     *
     * @param angleTurns the angle on the circle, in turns
     * @param magnitude the radius of the circle; larger values will change more for the same change in angle
     */
    public void setMutationAngleTurns(float angleTurns, float magnitude) {
        mutationA = TrigTools.cosTurns(angleTurns) * magnitude;
        mutationB = TrigTools.sinTurns(angleTurns) * magnitude;
    }

    /**
     * The String "DuMu" .
     * @return "DuMu"
     */
    @Override
    public String getTag() {
        return "DuMu";
    }

    @Override
    public String stringSerialize() {
        return "`" + mutationA + "~" + mutationB + "~" + Serializer.serialize(basis) + "`";
    }

    @Override
    public DualMutantNoiseWrapper stringDeserialize(String data) {
        int idx = 2;
        mutationA = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        mutationB = Base.BASE10.readFloat(data, idx+1, idx = data.indexOf('~', idx+1));
        basis = Serializer.deserialize(data.substring(idx+1, data.lastIndexOf('`')-1));
        return this;
    }

    public static DualMutantNoiseWrapper recreateFromString(String data) {
        int idx = 2;
        float mutationA = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        float mutationB = Base.BASE10.readFloat(data, idx+1, idx = data.indexOf('~', idx+1));
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
    public final boolean equals(Object o) {
        if (!(o instanceof DualMutantNoiseWrapper)) return false;

        DualMutantNoiseWrapper that = (DualMutantNoiseWrapper) o;
        return Float.compare(mutationA, that.mutationA) == 0 && Float.compare(mutationB, that.mutationB) == 0 && basis.equals(that.basis);
    }

    @Override
    public int hashCode() {
        int result = basis.hashCode();
        result = 31 * result + BitConversion.floatToIntBits(mutationA);
        result = 31 * result + BitConversion.floatToIntBits(mutationB);
        return result;
    }

    @Override
    public String toString() {
        return "DualMutantNoiseWrapper{" +
                "basis=" + basis +
                ", mutationA=" + mutationA +
                ", mutationB=" + mutationB +
                '}';
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return basis.getNoiseWithSeed(x, y, mutationA, mutationB, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return basis.getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, mutationA, mutationB, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, u, mutationA, mutationB, seed);
    }

    /**
     * Not supported.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @param seed can be any long
     * @return never returns
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        throw new UnsupportedOperationException("Generating 6D noise with DualMutantNoiseWrapper is not supported.");
    }

    /**
     * Not supported.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @param m m position; can be any finite float
     * @param seed can be any long
     * @return never returns
     * @throws UnsupportedOperationException not supported
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, float m, long seed) {
        throw new UnsupportedOperationException("Generating 7D noise with DualMutantNoiseWrapper is not supported.");
    }
}
