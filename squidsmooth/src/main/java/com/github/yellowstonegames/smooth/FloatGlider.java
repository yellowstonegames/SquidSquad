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

/**
 * Allows specifying a smoothly-changing float value using any float for the start and the end, with a change
 * amount that affects how far the value has changed from start to end. Typical usage sets the start with
 * {@link #setStart(float)} once and the end more than once with {@link #setEnd(float)}, changing the end each time the
 * transition is complete. The value is automatically calculated in {@link #getValue()}, and its value will be different
 * every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 * <br>
 * This is a type of Glider, and so is compatible with other Gliders (it can also be merged with them).
 */
public class FloatGlider extends Glider {
    public FloatGlider() {
        super(new Changer("f", 0f, 0f));
    }

    public FloatGlider(float start) {
        super(new Changer("f", start, start));
    }

    public FloatGlider(float start, float end) {
        super(new Changer("f", start, end));
    }

    public float getValue() {
        return getFloat("f");
    }

    public float getStart() {
        return getStartFloat("f");
    }

    public void setStart(float start) {
        setStartFloat("f", start);
        change = 0f;
    }

    public float getEnd() {
        return getEndFloat("f");
    }

    public void setEnd(float end) {
        setEndFloat("f", end);
        change = 0f;
    }
}
