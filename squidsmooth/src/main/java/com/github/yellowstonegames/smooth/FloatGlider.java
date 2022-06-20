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
import com.github.tommyettinger.digital.BitConversion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows specifying a smoothly-changing float value using any float for the start and the end, with a change
 * amount that affects how far the value has changed from start to end. Typical usage sets the start with
 * {@link #setStart(float)} once and the end more than once with {@link #setEnd(float)}, changing the end each time the
 * transition is complete. The value is automatically calculated in {@link #getValue()}, and its value will be different
 * every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 */
public class FloatGlider implements IGlider {
    protected float change = 0f;
    protected float start;
    protected float end;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    public @Nullable Runnable completeRunner;

    public FloatGlider() {
        start = 0f;
        end = 0f;
    }

    public FloatGlider(float start) {
        this.start = start;
        this.end = start;
    }

    public FloatGlider(float start, float end) {
        this.start = start;
        this.end = end;
    }

    public float getValue() {
        return interpolation.apply(start, end, change);
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

    public float getStart() {
        return start;
    }

    public void setStart(float start) {
        this.start = start;
        change = 0f;
    }

    public float getEnd() {
        return end;
    }

    public void setEnd(float end) {
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

        FloatGlider that = (FloatGlider) o;

        if (start != that.start) return false;
        if (end != that.end) return false;
        return interpolation.equals(that.interpolation);
    }

    @Override
    public int hashCode() {
        return (int) (BitConversion.floatToRawIntBits(start) * 0xD1B54A32D192ED03L
                + BitConversion.floatToRawIntBits(end) * 0xABC98388FB8FAC03L
                + interpolation.hashCode() * 0x8CB92BA72F3D8DD7L >>> 32);
    }
}
