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

import com.badlogic.gdx.math.Interpolation;

/**
 * Allows specifying a smoothly-changing float value that represents an angle, using floats measured in turns (between
 * 0 and 1) for start and end, with a change amount that affects how far the value has changed from start to end. This
 * always chooses between clockwise or counterclockwise by preferring the shorter rotation distance. Typical usage sets
 * the start with {@link #setStart(float)} once and the end more than once with {@link #setEnd(float)}, changing the end
 * each time the transition is complete. The value is automatically calculated in {@link #getAngle()}, and its value
 * will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 * <br>
 * Note that because this measures angles in turns, it is meant to either be used with code that already expects turns,
 * like {@link com.github.tommyettinger.digital.TrigTools#sinTurns(float)}, or to have the angle in turns converted to
 * radians (by multiplying an angle in turns by {@link com.github.tommyettinger.digital.TrigTools#PI2}) or to degrees
 * (by multiplying an angle in turns by {@code 360)}).
 * <br>
 * This is a type of MultiGlider, and so is compatible with other MultiGliders (it can also be merged with them).
 */
public class AngleGlider extends MultiGlider {
    public AngleGlider() {
        super(new Changer("angle", 0f, 0f, FloatInterpolator.ANGLE));
    }

    public AngleGlider(float start) {
        super(new Changer("angle", start, start, FloatInterpolator.ANGLE));
    }

    public AngleGlider(float start, float end) {
        super(new Changer("angle", start, end, FloatInterpolator.ANGLE));
    }

    public float getAngle() {
        return getFloat("angle");
    }

    public float getStart() {
        return getStartFloat("angle");
    }

    public void setStart(float start) {
        setStartFloat("angle", start);
        change = 0f;
    }

    public float getEnd() {
        return getEndFloat("angle");
    }

    public void setEnd(float end) {
        setEndFloat("angle", end);
        change = 0f;
    }
}
