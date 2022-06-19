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
import com.github.yellowstonegames.core.DescriptiveColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows specifying a smoothly-changing color using an int-based color (often RGBA8888, but possibly produced by
 * {@link DescriptiveColor} and in Oklab space) for the start and the end, with a change amount that affects how far a
 * color has been edited from start to end. Typical usage sets the start with
 * {@link #setStart(int)} once and the end more than once with {@link #setEnd(int)}, changing the end each time the
 * move is complete. The current color is automatically calculated in {@link #getColor()}, and its
 * value will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 */
public class IntColorGlider implements IGlider {
    protected float change = 0f;
    protected int start = 0;
    protected int end = 0;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected @Nullable Runnable completeRunner;

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
        this.start = start;
        this.end = start & 0xFFFFFF00;
    }

    /**
     * Creates an IntColorGlider that linear-interpolates the start color to the end color.
     * @param start the start color, as an int (this could be RGBA or Oklab)
     * @param end the end color, as an int (this could be RGBA or Oklab, but should be the same kind as start)
     */
    public IntColorGlider(int start, int end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Creates an IntColorGlider that linear-interpolates the start color to the end color, and runs the given Runnable
     * upon completion.
     * @param start the start color, as an int (this could be RGBA or Oklab)
     * @param end the end color, as an int (this could be RGBA or Oklab, but should be the same kind as start)
     * @param completeRunner a Runnable that, if non-null, will be run when the glide completes
     */
    public IntColorGlider(int start, int end, @Nullable Runnable completeRunner) {
        this.start = start;
        this.end = end;
        this.completeRunner = completeRunner;
    }

    /**
     * Gets the current color value, which is different when {@link #getChange()} is.
     * @return the current int color
     */
    public int getColor()
    {
        return DescriptiveColor.lerpColors(start, end, interpolation.apply(change));
    }

    @Override
    public float getChange() {
        return change;
    }

    @Override
    public void setChange(float change) {
        if(this.change != (this.change = Math.max(0f, Math.min(1f, change))) && this.change == 1f) {
            onComplete();
        }
    }

    @Override
    @Nonnull
    public Interpolation getInterpolation() {
        return interpolation;
    }

    @Override
    public void setInterpolation(@Nonnull Interpolation interpolation) {
        this.interpolation = interpolation;
        change = 0f;
    }

    /**
     *
     * @return start color
     */
    public int getStart() {
        return start;
    }

    /**
     *
     * @param start start color
     */
    public void setStart(int start) {
        this.start = start;
        change = 0f;
    }

    /**
     *
     * @return end color
     */
    public int getEnd() {
        return end;
    }

    /**
     *
     * @param end end color
     */
    public void setEnd(int end) {
        this.end = end;
        change = 0f;
    }

    @Override
    public void onComplete() {
        start = end;
        if(completeRunner != null) {
            completeRunner.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntColorGlider that = (IntColorGlider) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        return interpolation.equals(that.interpolation);
    }

    @Override
    public int hashCode() {
        int result = interpolation.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        return result;
    }
}
