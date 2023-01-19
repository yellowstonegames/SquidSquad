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

import com.github.tommyettinger.function.FloatToFloatFunction;

public class NoiseAdjustment implements INoise {
    public INoise wrapped;
    public FloatToFloatFunction adjustment;
    public NoiseAdjustment(){
        this(new SimplexNoise(123), f -> -f);
    }
    public NoiseAdjustment(INoise toWrap, FloatToFloatFunction adjust) {
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

    public FloatToFloatFunction getAdjustment() {
        return adjustment;
    }

    public NoiseAdjustment setAdjustment(FloatToFloatFunction adjustment) {
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
    public float getNoise(float x, float y) {
        return adjustment.applyAsFloat(wrapped.getNoise(x, y));
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return adjustment.applyAsFloat(wrapped.getNoise(x, y, z));
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return adjustment.applyAsFloat(wrapped.getNoise(x, y, z, w));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return adjustment.applyAsFloat(wrapped.getNoise(x, y, z, w, u));
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return adjustment.applyAsFloat(wrapped.getNoise(x, y, z, w, u, v));
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
        return adjustment.applyAsFloat(wrapped.getNoiseWithSeed(x, y, seed));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return adjustment.applyAsFloat(wrapped.getNoiseWithSeed(x, y, z, seed));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return adjustment.applyAsFloat(wrapped.getNoiseWithSeed(x, y, z, w, seed));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return adjustment.applyAsFloat(wrapped.getNoiseWithSeed(x, y, z, w, u, seed));
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return adjustment.applyAsFloat(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed));
    }
}
