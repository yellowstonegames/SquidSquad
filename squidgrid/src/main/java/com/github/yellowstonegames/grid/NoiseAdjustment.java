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
 */
public class NoiseAdjustment implements INoise {
    public INoise wrapped;
    public Interpolator adjustment;
    public NoiseAdjustment(){
        this(new SimplexNoise(123), Interpolations.smooth);
    }
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
}
