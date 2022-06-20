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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.MathUtils;
import com.github.yellowstonegames.core.DescriptiveColor;

/**
 * Allows using various ways of interpolating an int value to another int value as a first-class function.
 */
@FunctionalInterface
public interface IntInterpolator {
    int apply(int start, int end, float change);

    /**
     * Interpolates between start and end by change using {@link MathUtils#lerp(float, float, float)}, then rounds the
     * result to an int with {@link MathUtils#round(float)}.
     */
    IntInterpolator LINEAR = ((start, end, change) -> MathUtils.round(MathUtils.lerp(start, end, change)));

    /**
     * A method reference to {@link DescriptiveColor#lerp(float, float, float)}.
     */
    IntInterpolator COLOR = DescriptiveColor::lerpColors;
}
