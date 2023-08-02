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

import com.badlogic.gdx.math.Interpolation;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.Interpolations.Interpolator;

/**
 * A simple wrapper around a digital {@link Interpolator} so it can act as a libGDX {@link Interpolation}.
 * Just pass an Interpolator to the constructor, and you have an Interpolation that uses it.
 * This also acts as a {@link FloatSmoother} using the wrapped Interpolator.
 * If you need to go in the reverse direction and create an {@link Interpolator} from an {@link Interpolation} (let's
 * call it {@code myInterpolation}, you can use {@code new Interpolator("myInterpolation", myInterpolation::apply)}.
 */
public class WrapperInterpolation extends Interpolation implements FloatSmoother {
    public final Interpolator wrapped;
    public WrapperInterpolation() {
        this(Interpolations.linear);
    }

    public WrapperInterpolation(Interpolator toWrap) {
        super();
        wrapped = toWrap;
    }

    @Override
    public float apply(float v) {
        return wrapped.apply(v);
    }

    @Override
    public float apply(float start, float end, float a) {
        return wrapped.apply(start, end, a);
    }
}
