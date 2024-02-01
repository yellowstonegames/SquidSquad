/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.github.yellowstonegames.core.DescriptiveColor;

/**
 * Allows using various ways of smoothly interpolating an int value to another int value as a first-class function.
 * This is a functional interface whose functional method is {@link #apply(int, int, float)}.
 */
public interface IntSmoother {
    int apply(int start, int end, float change);

    /**
     * Interpolates between start and end by change using {@link MathUtils#lerp(float, float, float)}, then rounds the
     * result to an int with {@link MathUtils#round(float)}.
     */
    IntSmoother LINEAR = ((start, end, change) -> MathUtils.round(MathUtils.lerp(start, end, change)));

    /**
     * A method reference to {@link DescriptiveColor#lerpColors(int, int, float)}.
     */
    IntSmoother COLOR = DescriptiveColor::lerpColors;
}
