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
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A periodic type of continuous noise that looks good when frequencies are low, and rather bad when frequencies are
 * high. From <a href="https://www.shadertoy.com/view/3tcyD7">this ShaderToy by jeyko</a>, based on
 * <a href="https://www.shadertoy.com/view/wl3czN">this ShaderToy by nimitz</a>.
 */
@Beta
public class CyclicNoise {
    /*
// From https://www.shadertoy.com/view/3tcyD7 by jeyko, based on https://www.shadertoy.com/view/wl3czN by nimitz
float cyclicNoise(vec3 p){
    float noise = 0.;

    // These are the variables. I renamed them from the original by nimitz
    // So they are more similar to the terms used be other types of noise
    float amp = 0.5;
    const float gain = 0.5;
    const float lacunarity = 2.;
    const int octaves = 2;

    const float warp = 0.3;
    float warpTrk = 1.2 ;
    const float warpTrkGain = 1.5;

    // Step 1: Get a simple arbitrary rotation, defined by the direction.
    vec3 seed = vec3(-1,-2.,0.5);
    mat3 rotMatrix = getOrthogonalBasis(seed);

    for(int i = 0; i < octaves; i++){

        // Step 2: Do some domain warping, Similar to fbm. Optional.

        p += sin(p.zxy*warpTrk)*warp;

        // Step 3: Calculate a noise value.
        // This works in a way vaguely similar to Perlin/Simplex noise,
        // but instead of in a square/triangle lattice, it is done in a sine wave.

        noise += sin(dot(cos(p), sin(p.zxy )))*amp;

        // Step 4: Rotate and scale.

        p *= rotMatrix;
        p *= lacunarity;

        warpTrk *= warpTrkGain;
        amp *= gain;
    }


    #ifdef TURBULENT
    return 1. - abs(noise);
    #else
    return (noise*0.5 + 0.5);
    #endif
}
     */

    protected int octaves;
    protected float total = 1f, start = 1f;
    protected final float lacunarity = 1.6f;
    protected final float gain = 0.6f;
    public CyclicNoise() {
        this(3);
    }
    public CyclicNoise(int octaves) {
        setOctaves(octaves);
    }

    public int getOctaves() {
        return octaves;
    }

    public void setOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);

        start = gain;
        total = 0f;
        for (int i = 0; i < this.octaves; i++) {
            start /= gain;
            total += start;
        }
        total = 1f / total;
    }

    public float getNoise(float x, float y) {
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        float xx, yy;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;

            x += yy;
            y += xx;

            int xs = (int) (x * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (y * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sin((
                            SIN_TABLE[xc] * SIN_TABLE[ys] + SIN_TABLE[yc] * SIN_TABLE[xs]
//                            + LineWobble.wobble(123, x) + LineWobble.wobble(456, y) + LineWobble.wobble(789, z)
                    )
            ) * amp;

            xx = Noise.rotateX2D(x, y);
            yy = Noise.rotateY2D(x, y);

            x = xx * lacunarity;
            y = yy * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    public float getNoise(float x, float y, float z) {
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        float xx, yy, zz;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;

            x += zz;
            y += xx;
            z += yy;

            int xs = (int) (x * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (y * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (z * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sin((
                    SIN_TABLE[xc] * SIN_TABLE[zs] +
                    SIN_TABLE[yc] * SIN_TABLE[xs] +
                    SIN_TABLE[zc] * SIN_TABLE[ys]
                    )
            ) * amp;

            xx = Noise.rotateX3D(x, y, z);
            yy = Noise.rotateY3D(x, y, z);
            zz = Noise.rotateZ3D(x, y, z);

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    public float getNoise(float x, float y, float z, float w) {
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        float xx, yy, zz, ww;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;

            x += ww;
            y += xx;
            z += yy;
            w += zz;

            int xs = (int) (x * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (y * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (z * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;
            int ws = (int) (w * radToIndex) & TABLE_MASK, wc = ws + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sin((
                    + SIN_TABLE[xc] * SIN_TABLE[ws]
                    + SIN_TABLE[yc] * SIN_TABLE[xs]
                    + SIN_TABLE[zc] * SIN_TABLE[ys]
                    + SIN_TABLE[wc] * SIN_TABLE[zs]
                    )
            ) * amp;

            xx = Noise.rotateX4D(x, y, z, w);
            yy = Noise.rotateY4D(x, y, z, w);
            zz = Noise.rotateZ4D(x, y, z, w);
            ww = Noise.rotateW4D(x, y, z, w);

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;
            w = ww * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }

    public float getNoise(float x, float y, float z, float w, float u) {
        float noise = 0f;

        float amp = start;

        final float warp = 0.3f;
        float warpTrk = 1.2f;
        final float warpTrkGain = 1.5f;

        float xx, yy, zz, ww, uu;
        for (int i = 0; i < octaves; i++) {
            xx = TrigTools.sin((x-2) * warpTrk) * warp;
            yy = TrigTools.sin((y-2) * warpTrk) * warp;
            zz = TrigTools.sin((z-2) * warpTrk) * warp;
            ww = TrigTools.sin((w-2) * warpTrk) * warp;
            uu = TrigTools.sin((u-2) * warpTrk) * warp;

            x += uu;
            y += xx;
            z += yy;
            w += zz;
            u += ww;

            int xs = (int) (x * radToIndex) & TABLE_MASK, xc = xs + SIN_TO_COS & TABLE_MASK;
            int ys = (int) (y * radToIndex) & TABLE_MASK, yc = ys + SIN_TO_COS & TABLE_MASK;
            int zs = (int) (z * radToIndex) & TABLE_MASK, zc = zs + SIN_TO_COS & TABLE_MASK;
            int ws = (int) (w * radToIndex) & TABLE_MASK, wc = ws + SIN_TO_COS & TABLE_MASK;
            int us = (int) (u * radToIndex) & TABLE_MASK, uc = us + SIN_TO_COS & TABLE_MASK;

            noise += TrigTools.sin((
                    + SIN_TABLE[xc] * SIN_TABLE[us]
                    + SIN_TABLE[yc] * SIN_TABLE[xs]
                    + SIN_TABLE[zc] * SIN_TABLE[ys]
                    + SIN_TABLE[wc] * SIN_TABLE[zs]
                    + SIN_TABLE[uc] * SIN_TABLE[ws]
                    )
            ) * amp;

            xx = Noise.rotateX5D(x, y, z, w, u);
            yy = Noise.rotateY5D(x, y, z, w, u);
            zz = Noise.rotateZ5D(x, y, z, w, u);
            ww = Noise.rotateW5D(x, y, z, w, u);
            uu = Noise.rotateU5D(x, y, z, w, u);

            x = xx * lacunarity;
            y = yy * lacunarity;
            z = zz * lacunarity;
            w = ww * lacunarity;
            u = uu * lacunarity;

            warpTrk *= warpTrkGain;
            amp *= gain;
        }
        return noise * total;
    }
    private static final float radToIndex = TABLE_SIZE / PI2;
}