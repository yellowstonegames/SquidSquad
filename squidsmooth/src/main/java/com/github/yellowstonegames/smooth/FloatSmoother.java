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

package com.github.yellowstonegames.smooth;

import com.github.tommyettinger.digital.MathTools;

/**
 * Allows using various ways of smoothly interpolating a float value to another float value as a first-class function.
 * This is a functional interface whose functional method is {@link #apply(float, float, float)}.
 */
public interface FloatSmoother {
    float apply(float start, float end, float change);

    /**
     * A method reference to {@link MathTools#lerp(float, float, float)}.
     */
    FloatSmoother LINEAR = MathTools::lerp;

    /**
     * A method reference to {@link MathTools#lerpAngleTurns(float, float, float)}.
     */
    FloatSmoother ANGLE = MathTools::lerpAngleTurns;
}
