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

import static com.github.tommyettinger.digital.TrigTools.sin;
import static com.github.yellowstonegames.grid.QuasiRandomTools.GOLDEN_FLOATS;

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
        return sin(7+q*GOLDEN_FLOATS[6][0] + sin(8+q*GOLDEN_FLOATS[6][1] + sin(9+q*GOLDEN_FLOATS[6][2] + x + y)));
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return noise(x, y, z);
    }
    public static float noise(float x, float y, float z) {
        float sx = LineWobble.wobble(121212, 1+x*GOLDEN_FLOATS[0][0] + sin(2+x*GOLDEN_FLOATS[3][0] + sin(3+x*GOLDEN_FLOATS[6][0] + z - y)));
        float sy = LineWobble.wobble(343434, 4+y*GOLDEN_FLOATS[1][1] + sin(5+y*GOLDEN_FLOATS[4][1] + sin(6+y*GOLDEN_FLOATS[7][1] + x - z)));
        float sz = LineWobble.wobble(565656, 7+z*GOLDEN_FLOATS[2][2] + sin(8+z*GOLDEN_FLOATS[5][2] + sin(9+z*GOLDEN_FLOATS[8][2] + y - x)));
        float q = (sx + sy + sz) * (12f/3f);
        return LineWobble.wobble(123456789, 10+q*GOLDEN_FLOATS[9][0] + sin(11+q*GOLDEN_FLOATS[9][1] + sin(12+q*GOLDEN_FLOATS[9][2])));
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
