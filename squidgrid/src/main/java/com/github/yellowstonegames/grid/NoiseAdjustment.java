/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.yellowstonegames.core.Interpolations;
import com.github.yellowstonegames.core.Interpolations.InterpolationFunction;
import com.github.yellowstonegames.core.Interpolations.Interpolator;

/**
 * Wraps another {@link INoise} and alters its output by running it through an {@link Interpolator}. The {@code [-1,1]}
 * range for noise is mapped to the {@code [0,1]} range that Interpolator uses, and mapped back afterwards. This uses
 * Interpolator rather than the slightly-simpler {@link InterpolationFunction} because an Interpolator can be serialized
 * just by storing its {@link Interpolator#getTag()}, and loaded by looking up its tag.
 * <br>
 * Example usage:
 * <pre>
 *     Interpolator spline_06_03 = new Interpolator("spline_06_03", (f) -> MathTools.barronSpline(f, 0.6f, 0.3f));
 *     NoiseAdjustment centralizeLow = new NoiseAdjustment(new SimplexNoise(12345), spline_06_03);
 * </pre>
 * The Interpolator is created separately so that it can be created (and thus registered) before any saving or loading
 * step might happen. You may want to create Interpolators in their own file, much like how the Interpolations class
 * defines many Interpolators for later usage. The NoiseAdjustment can use any INoise, such as a {@link Noise} or here
 * a {@link SimplexNoise}.
 */
public class NoiseAdjustment implements INoise {
    public INoise wrapped;
    public Interpolator adjustment;

    /**
     * The same as calling {@code new NoiseAdjustment(new SimplexNoise(123), Interpolations.smooth)}.
     */
    public NoiseAdjustment(){
        this(new SimplexNoise(123), Interpolations.smooth);
    }

    /**
     * Creates a NoiseAdjustment wrapping the given INoise and modifying its output with the given Interpolator.
     * @param toWrap an INoise, such as a {@link Noise} or {@link SimplexNoise}
     * @param adjust an Interpolator, such as {@link Interpolations#exp5}, to apply to the output of the INoise
     */
    public NoiseAdjustment(INoise toWrap, Interpolator adjust) {
        this.wrapped = toWrap;
        this.adjustment = adjust;
    }

    public INoise getWrapped() {
        return wrapped;
    }

    public NoiseAdjustment setWrapped(INoise wrapped) {
        this.wrapped = wrapped;
        return this;
    }

    public Interpolator getAdjustment() {
        return adjustment;
    }

    public NoiseAdjustment setAdjustment(Interpolator adjustment) {
        this.adjustment = adjustment;
        return this;
    }

    @Override
    public int getMinDimension() {
        return wrapped.getMinDimension();
    }

    @Override
    public int getMaxDimension() {
        return wrapped.getMaxDimension();
    }

    @Override
    public boolean canUseSeed() {
        return wrapped.canUseSeed();
    }

    @Override
    public NoiseAdjustment copy() {
        return new NoiseAdjustment(wrapped.copy(), adjustment);
    }

    @Override
    public float getNoise(float x, float y) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoise(x, y)));
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoise(x, y, z)));
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoise(x, y, z, w)));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoise(x, y, z, w, u)));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoise(x, y, z, w, u, v)));
    }

    @Override
    public void setSeed(long seed) {
        wrapped.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return wrapped.getSeed();
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoiseWithSeed(x, y, seed)));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoiseWithSeed(x, y, z, seed)));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoiseWithSeed(x, y, z, w, seed)));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoiseWithSeed(x, y, z, w, u, seed)));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return 2f * (-0.5f + adjustment.apply(0.5f + 0.5f * wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed)));
    }

    /**
     * @return "NAdj"
     */
    @Override
    public String getTag() {
        return "NAdj";
    }

    /**
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #deserializeFromString(String)} to reassign the described state to another INoise. The syntax here
     * should always start and end with the {@code `} character, which is used by
     * {@link Serializer#deserializeFromString(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another INoise that this uses.
     *
     * @return a String that describes this INoise for serialization
     */
    @Override
    public String serializeToString() {
        return "`" + Serializer.serialize(wrapped) + '~' + adjustment.getTag() + "`";
    }

    /**
     * Given a serialized String produced by {@link #serializeToString()}, reassigns this INoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     *
     * @param data a serialized String, typically produced by {@link #serializeToString()}
     * @return this INoise, after being modified (if possible)
     */
    @Override
    public INoise deserializeFromString(String data) {
        int pos = data.indexOf('`', data.indexOf('`', 2) + 1)+1;
        setWrapped(Serializer.deserialize(data.substring(1, pos)));
        setAdjustment(Interpolations.get(data.substring(pos+1, data.indexOf('`', pos+2))));
        return this;
    }
}
