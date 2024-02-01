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

import com.badlogic.gdx.math.Interpolation;
import com.github.yellowstonegames.core.DescriptiveColor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Allows specifying a smoothly-changing color using an int-based color (often RGBA8888, but possibly produced by
 * {@link DescriptiveColor} and in Oklab space) for the start and the end, with a change amount that affects how far a
 * color has been edited from start to end. Typical usage sets the start with
 * {@link #setStart(int)} once and the end more than once with {@link #setEnd(int)}, changing the end each time the
 * move is complete. The current color is automatically calculated in {@link #getColor()}, and its
 * value will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 * <br>
 * This is a type of Glider, and so is compatible with other Gliders (it can also be merged with them).
 */
public class IntColorGlider extends Glider {

    /**
     * Constructs an empty IntColorGlider that needs to have its {@link #setStart(int)} and {@link #setEnd(int)} methods
     * called before it can be meaningfully used.
     */
    public IntColorGlider() {
    }

    /**
     * Creates an IntColorGlider that linear-interpolates the start color to fully transparent. Unlike a transition from
     * start to {@link DescriptiveColor#TRANSPARENT}, this will only change the alpha channel, keeping the color intact
     * except for its alpha.
     * @param start the color that will fade to transparent
     */
    public IntColorGlider(int start) {
        super(new Changer("color", start, start & 0xFFFFFF00, IntSmoother.COLOR));
    }

    /**
     * Creates an IntColorGlider that linear-interpolates the start color to the end color.
     * @param start the start color, as an int (this could be RGBA or Oklab)
     * @param end the end color, as an int (this could be RGBA or Oklab, but should be the same kind as start)
     */
    public IntColorGlider(int start, int end) {
        super(new Changer("color", start, end, IntSmoother.COLOR));
    }

    /**
     * Creates an IntColorGlider that linear-interpolates the start color to the end color, and runs the given Runnable
     * upon completion.
     * @param start the start color, as an int (this could be RGBA or Oklab)
     * @param end the end color, as an int (this could be RGBA or Oklab, but should be the same kind as start)
     * @param completeRunner a Runnable that, if non-null, will be run when the glide completes
     */
    public IntColorGlider(int start, int end, @Nullable Runnable completeRunner) {
        super(Interpolation.linear, completeRunner, new Changer("color", start, end, IntSmoother.COLOR));
    }

    /**
     * Creates an IntColorGlider that interpolates the start color to the end color using the given Interpolation, and
     * runs the given Runnable upon completion.
     * @param start the start color, as an int (this could be RGBA or Oklab)
     * @param end the end color, as an int (this could be RGBA or Oklab, but should be the same kind as start)
     * @param interpolation how to interpolate from start to end; typically a constant from {@link Interpolation}
     * @param completeRunner a Runnable that, if non-null, will be run when the glide completes
     */
    public IntColorGlider(int start, int end, @NonNull Interpolation interpolation, @Nullable Runnable completeRunner) {
        super(interpolation, completeRunner, new Changer("color", start, end, IntSmoother.COLOR));
    }

    public IntColorGlider(IntColorGlider other) {
        super(other);
    }
    /**
     * Gets the current color value, which is different when {@link #getChange()} is.
     * @return the current int color
     */
    public int getColor()
    {
        return getInt("color");
    }

    /**
     *
     * @return start color
     */
    public int getStart() {
        return getStartInt("color");
    }

    /**
     *
     * @param start start color
     */
    public void setStart(int start) {
        setStartInt("color", start);
    }

    /**
     *
     * @return end color
     */
    public int getEnd() {
        return getEndInt("color");
    }

    /**
     *
     * @param end end color
     */
    public void setEnd(int end) {
        setEndInt("color", end);
    }
}
