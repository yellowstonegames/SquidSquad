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
 */

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.TrigTools;

import static com.github.yellowstonegames.grid.Noise.*;

public class NoiseWrapper implements INoise {
    public INoise wrapped;
    protected long seed;
    public float frequency;
    public int mode;
    protected int octaves;
    public boolean fractalSpiral = false;

    public NoiseWrapper() {
        this(new SimplexNoise(123L), 123L, 0.03125f, 0, 1);

    }

    public NoiseWrapper(INoise toWrap, long seed, float frequency, int mode, int octaves){
        wrapped = toWrap;
        setSeed(seed);
        this.frequency = frequency;
        this.mode = mode;
        this.octaves = octaves;
    }

    public INoise getWrapped() {
        return wrapped;
    }

    public void setWrapped(INoise wrapped) {
        this.wrapped = wrapped;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    /**
     * Wraps {@link #getFractalType()}.
     * @return an int between 0 and 3, corresponding to {@link Noise#FBM}, {@link Noise#BILLOW}, {@link Noise#RIDGED_MULTI}, or {@link Noise#DOMAIN_WARP}
     */
    public int getMode() {
        return getFractalType();
    }

    /**
     * Wra[s {@link #setFractalType(int)}
     * @param mode an int between 0 and 3, corresponding to {@link Noise#FBM}, {@link Noise#BILLOW}, {@link Noise#RIDGED_MULTI}, or {@link Noise#DOMAIN_WARP}
     */
    public void setMode(int mode) {
        setFractalType(mode);
    }

    public int getFractalType() {
        return mode;
    }

    /**
     * @param mode an int between 0 and 3, corresponding to {@link Noise#FBM}, {@link Noise#BILLOW}, {@link Noise#RIDGED_MULTI}, or {@link Noise#DOMAIN_WARP}
     */
    public void setFractalType(int mode) {
        this.mode = mode;
    }

    /**
     * Wraps {@link #getFractalOctaves()}.
     * @return how many octaves this uses to increase detail
     */
    public int getOctaves() {
        return getFractalOctaves();
    }

    /**
     * Wraps {@link #setFractalOctaves(int)}.
     * @param octaves how many octaves to use to increase detail; must be at least 1.
     */
    public void setOctaves(int octaves) {
        setFractalOctaves(octaves);
    }

    public int getFractalOctaves() {
        return octaves;
    }

    /**
     * @param octaves how many octaves to use to increase detail; must be at least 1.
     */
    public void setFractalOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);
    }

    public boolean isFractalSpiral() {
        return fractalSpiral;
    }

    public void setFractalSpiral(boolean fractalSpiral) {
        this.fractalSpiral = fractalSpiral;
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
        switch (mode) {
            default: return fbm(x, y, seed);
            case 1: return billow(x, y, seed);
            case 2: return ridged(x, y, seed);
            case 3: return warp(x, y, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z) {
        switch (mode) {
            default: return fbm(x, y, z, seed);
            case 1: return billow(x, y, z, seed);
            case 2: return ridged(x, y, z, seed);
            case 3: return warp(x, y, z, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        switch (mode) {
            default: return fbm(x, y, z, w, seed);
            case 1: return billow(x, y, z, w, seed);
            case 2: return ridged(x, y, z, w, seed);
            case 3: return warp(x, y, z, w, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        switch (mode) {
            default: return fbm(x, y, z, w, u, seed);
            case 1: return billow(x, y, z, w, u, seed);
            case 2: return ridged(x, y, z, w, u, seed);
            case 3: return warp(x, y, z, w, u, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        switch (mode) {
            default: return fbm(x, y, z, w, u, v, seed);
            case 1: return billow(x, y, z, w, u, v, seed);
            case 2: return ridged(x, y, z, w, u, v, seed);
            case 3: return warp(x, y, z, w, u, v, seed);
        }
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        if(wrapped.canUseSeed())
            wrapped.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
        }
    }

    // 2D
    
    protected float fbm(float x, float y, long seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, long seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i)) * 2 - 1) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    protected float ridged(float x, float y, long seed) {
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x *= 2f;
            y *= 2f;
        }
        return sum * 2f / correction - 1f;
    }

    protected float warp(float x, float y, long seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, seed);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX2D(x, y);
                final float y2 = rotateY2D(x, y);
                x = x2; y = y2;
            }
            x = x * 2f;
            y = y * 2f;
            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx],
                    b = TrigTools.SIN_TABLE[idx + (8192 / 2) & TrigTools.TABLE_MASK];

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, seed + i)) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    // 3D
    
    protected float fbm(float x, float y, float z, long seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, long seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i)) * 2 - 1) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    protected float ridged(float x, float y, float z, long seed) {
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
        }
        return sum * 2f / correction - 1f;
    }

    protected float warp(float x, float y, float z, long seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, seed);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX3D(x, y, z);
                final float y2 = rotateY3D(x, y, z);
                final float z2 = rotateZ3D(x, y, z);
                x = x2; y = y2; z = z2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;

            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 3) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 3) & TrigTools.TABLE_MASK];

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, seed + i)) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    // 4D
    
    protected float fbm(float x, float y, float z, float w, long seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, long seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed + i)) * 2 - 1) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    protected float ridged(float x, float y, float z, float w, long seed) {
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed + i));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
        }
        return sum * 2f / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, long seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, seed);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX4D(x, y, z, w);
                final float y2 = rotateY4D(x, y, z, w);
                final float z2 = rotateZ4D(x, y, z, w);
                final float w2 = rotateW4D(x, y, z, w);
                x = x2; y = y2; z = z2; w = w2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 4) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 4) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 4) & TrigTools.TABLE_MASK];

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, seed + i)) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    // 5D

    protected float fbm(float x, float y, float z, float w, float u, long seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, u, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, long seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i)) * 2 - 1) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    protected float ridged(float x, float y, float z, float w, float u, long seed) {
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
        }
        return sum * 2f / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, float u, long seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, u, seed);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX5D(x, y, z, w, u);
                final float y2 = rotateY5D(x, y, z, w, u);
                final float z2 = rotateZ5D(x, y, z, w, u);
                final float w2 = rotateW5D(x, y, z, w, u);
                final float u2 = rotateU5D(x, y, z, w, u);
                x = x2; y = y2; z = z2; w = w2; u = u2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 5) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 5) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 5) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 5) & TrigTools.TABLE_MASK];

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, u + e, seed + i)) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    // 5D

    protected float fbm(float x, float y, float z, float w, float u, float v, long seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed);
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, float v, long seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i)) * 2 - 1) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

    protected float ridged(float x, float y, float z, float w, float u, float v, long seed) {
        float sum = 0f, exp = 2f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i));
            correction += (exp *= 0.5f);
            sum += spike * exp;
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;
        }
        return sum * 2f / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, float u, float v, long seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed);
        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            if(fractalSpiral){
                final float x2 = rotateX6D(x, y, z, w, u, v);
                final float y2 = rotateY6D(x, y, z, w, u, v);
                final float z2 = rotateZ6D(x, y, z, w, u, v);
                final float w2 = rotateW6D(x, y, z, w, u, v);
                final float u2 = rotateU6D(x, y, z, w, u, v);
                final float v2 = rotateV6D(x, y, z, w, u, v);
                x = x2; y = y2; z = z2; w = w2; u = u2; v = v2;
            }
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            final int idx = (int) (latest * 8192) & TrigTools.TABLE_MASK;
            float a = TrigTools.SIN_TABLE[idx];
            float b = TrigTools.SIN_TABLE[idx + (8192 / 6) & TrigTools.TABLE_MASK];
            float c = TrigTools.SIN_TABLE[idx + (8192 * 2 / 6) & TrigTools.TABLE_MASK];
            float d = TrigTools.SIN_TABLE[idx + (8192 * 3 / 6) & TrigTools.TABLE_MASK];
            float e = TrigTools.SIN_TABLE[idx + (8192 * 4 / 6) & TrigTools.TABLE_MASK];
            float f = TrigTools.SIN_TABLE[idx + (8192 * 5 / 6) & TrigTools.TABLE_MASK];

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, u + e, v + f, seed + i)) * amp;
        }

        return sum * ((1 << octaves) - 1);
    }

}
