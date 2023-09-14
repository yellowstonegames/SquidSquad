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

import com.github.yellowstonegames.core.annotations.Beta;

/**
 * A mix of {@link CyclicNoise} with value noise; much less periodic than CyclicNoise alone. Largely based upon
 * <a href="https://www.shadertoy.com/view/3tcyD7">this ShaderToy by jeyko</a>, which in turn is based on
 * <a href="https://www.shadertoy.com/view/wl3czN">this ShaderToy by nimitz</a>. This uses cyclic noise with a
 * dimension one higher than requested, and uses a call to {@link ValueNoise#valueNoise} to fill that parameter.
 */
@Beta
public class SorbetNoise extends CyclicNoise {

    public SorbetNoise() {
        this(0xD1CEDBEEFBABBL, 3);
    }

    public SorbetNoise(long seed, int octaves) {
        super(seed, octaves);
    }

    public SorbetNoise stringDeserialize(String data) {
        super.stringDeserialize(data);
        return this;
    }

    public float getNoise(float x, float y) {
        float xx = x * 0.25f;
        float yy = y * 0.25f;
        return super.getNoise(x, y, 2.00f * SimplexNoise.noise(xx, yy, seed));
    }

    public float getNoise(float x, float y, float z) {
        float xx = x * 0.25f;
        float yy = y * 0.25f;
        float zz = z * 0.25f;
        return super.getNoise(x, y, z, 2.00f * SimplexNoise.noise(xx, yy, zz, seed));
    }

    public float getNoise(float x, float y, float z, float w) {
        float xx = x * 0.25f;
        float yy = y * 0.25f;
        float zz = z * 0.25f;
        float ww = w * 0.25f;
        return super.getNoise(x, y, z, w, 2.00f * SimplexNoise.noise(xx, yy, zz, ww, seed));
    }

    public float getNoise(float x, float y, float z, float w, float u) {
        float xx = x * 0.25f;
        float yy = y * 0.25f;
        float zz = z * 0.25f;
        float ww = w * 0.25f;
        float uu = u * 0.25f;
        return super.getNoise(x, y, z, w, u, 2.00f * SimplexNoise.noise(xx, yy, zz, ww, uu, seed));
    }

    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float xx = x * 0.25f;
        float yy = y * 0.25f;
        float zz = z * 0.25f;
        float ww = w * 0.25f;
        float uu = u * 0.25f;
        float vv = v * 0.25f;
        return super.getNoise(x, y, z, w, u, v, 2.00f * SimplexNoise.noise(xx, yy, zz, ww, uu, vv, seed));
    }

    @Override
    public String toString() {
        return "SorbetNoise with seed: " + seed + ", octaves:" + octaves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SorbetNoise that = (SorbetNoise) o;

        if (octaves != that.octaves) return false;
        return seed == that.seed;
    }
}
