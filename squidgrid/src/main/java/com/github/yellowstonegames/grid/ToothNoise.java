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

import com.github.tommyettinger.random.LineWobble;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.TrigTools.sin;
import static com.github.yellowstonegames.grid.QuasiRandomTools.GOLDEN_FLOATS;

/**
 * Not very good right now! Use only experimentally.
 */
@Beta
public class ToothNoise implements INoise{
    public ToothNoise() {
        // no state!
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6; // this is currently a lie!
    }

    @Override
    public boolean canUseSeed() {
        return false;
    }

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public long getSeed() {
        return 0;
    }

    @Override
    public String getTag() {
        return "TooN";
    }

    @Override
    public float getNoise(float x, float y) {
        return noise(x, y);
    }
    public static float noise(float x, float y) {
        float sx = sin(1+x*GOLDEN_FLOATS[0][0] + sin(2+x*GOLDEN_FLOATS[2][0] + sin(3+x*GOLDEN_FLOATS[4][0] + y)));
        float sy = sin(4+y*GOLDEN_FLOATS[1][1] + sin(5+y*GOLDEN_FLOATS[3][1] + sin(6+y*GOLDEN_FLOATS[5][1] + x)));
        float q = (sx + sy) * (12f/2f);
        return sin(7+q*GOLDEN_FLOATS[6][0] + sin(8+q*GOLDEN_FLOATS[6][1] + sin(9+q*GOLDEN_FLOATS[6][2])));
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return noise(x, y, z);
    }
    public static float noise(float x, float y, float z) {
        float sx = sin(1+x*GOLDEN_FLOATS[0][0] + sin(2+z*GOLDEN_FLOATS[3][0] + sin(3+y*GOLDEN_FLOATS[6][0])));
        float sy = sin(4+z*GOLDEN_FLOATS[1][1] + sin(5+y*GOLDEN_FLOATS[4][1] + sin(6+x*GOLDEN_FLOATS[7][1])));
        float sz = sin(7+y*GOLDEN_FLOATS[2][2] + sin(8+x*GOLDEN_FLOATS[5][2] + sin(9+z*GOLDEN_FLOATS[8][2])));
        float qx = sin((sy + sz) + x) * 3f;
        float qy = sin((sx + sz) + y) * 3f;
        float qz = sin((sx + sy) + z) * 3f;
        return (
                sin(10+qz*GOLDEN_FLOATS[9][0] + sin(11+qy*GOLDEN_FLOATS[9][1] + sin(12+qx*GOLDEN_FLOATS[9][2]))) +
                sin(13+qy*GOLDEN_FLOATS[9][3] + sin(14+qx*GOLDEN_FLOATS[9][4] + sin(15+qz*GOLDEN_FLOATS[9][5]))) +
                sin(16+qx*GOLDEN_FLOATS[9][6] + sin(17+qz*GOLDEN_FLOATS[9][7] + sin(18+qy*GOLDEN_FLOATS[9][8])))) * (1f/3f)
                ;
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return 0;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return 0;
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return 0;
    }
}
