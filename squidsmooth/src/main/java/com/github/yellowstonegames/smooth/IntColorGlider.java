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

    public IntColorGlider() {
    }

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

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
        change = 0f;
    }

    public int getEnd() {
        return end;
    }

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
