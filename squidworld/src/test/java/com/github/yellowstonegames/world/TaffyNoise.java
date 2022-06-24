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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.grid.PhantomNoise;

import static com.github.tommyettinger.digital.TrigTools.*;
import static com.github.tommyettinger.digital.TrigTools.TABLE_MASK;

public class TaffyNoise extends PhantomNoise {
    public TaffyNoise() {
        super();
    }

    public TaffyNoise(long seed, int dimension) {
        super(seed, dimension);
    }

    public TaffyNoise(long seed, int dimension, float sharpness) {
        super(seed, dimension, sharpness);
    }

    @Override
    protected double valueNoise() {
        int s = (int)(hasher.seed ^ hasher.seed >>> 32 ^ BitConversion.floatToRawIntBits(working[dim]));
        float sum = 0f;
        for (int i = 0, j = 1; i < dim; i++, j++) {
            float cx = working[i];
            float cy = working[j];
            int idx = (int) (s + cx * 1357 + cy * 421);
            sum += (cos(cx)
                    + SIN_TABLE[idx & TABLE_MASK]
                    + SIN_TABLE[idx + 4096 & TABLE_MASK]
                    + SIN_TABLE[s & TABLE_MASK]*cy
                    + sin(SIN_TABLE[s + 4096 & TABLE_MASK]*cx)
            );
        }
        return MathTools.swayTight(sum * 0.12f);
    }

    @Override
    protected double valueNoise2D() {
        int sx = (int)(hasher.seed ^ BitConversion.floatToRawIntBits(working[dim]));
        int sy = (int)(hasher.seed >>> 32 ^ BitConversion.floatToRawIntBits(working[dim]));
        float cx = working[0];
        float cy = working[1];
        int idx = (int) (sx + cx * 1357 + cy * 421);
        float sum = (cos(cx)
                + SIN_TABLE[idx & TABLE_MASK]
                + SIN_TABLE[idx + 4096 & TABLE_MASK]
                + SIN_TABLE[sx & TABLE_MASK]*cy + sin(SIN_TABLE[sx + 4096 & TABLE_MASK]*cx)
        );
        idx = (int) (sy + cy * 1357 + cx * 421);
        sum += (cos(cy)
                + SIN_TABLE[idx & TABLE_MASK]
                + SIN_TABLE[idx + 4096 & TABLE_MASK]
                + SIN_TABLE[sy & TABLE_MASK]*cx + sin(SIN_TABLE[sy + 4096 & TABLE_MASK]*cy)
        );
        return MathTools.swayTight(sum * 0.12f);
    }
}
