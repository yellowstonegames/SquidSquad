package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.github.yellowstonegames.grid.Coord;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows specifying a smoothly-changing float position using an exact Coord for the start and the end, with a change
 * amount that affects how far a position has moved from start to end. Typical usage sets the start with
 * {@link #setStart(Coord)} once and the end more than once with {@link #setEnd(Coord)}, changing the end each time the
 * move is complete. The x and y positions are automatically calculated in {@link #getX()} and {@link #getY()}, and
 * their values will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 */
public class CoordGlider implements IGlider {
    protected float change = 0f;
    protected @Nonnull Coord start;
    protected @Nonnull Coord end;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected @Nullable Runnable completeRunner;

    public CoordGlider() {
        start = Coord.get(0, 0);
        end = Coord.get(0, 0);
    }
    public CoordGlider(@Nonnull Coord start) {
        this.start = start;
        this.end = start;
    }
    public CoordGlider(@Nonnull Coord start, @Nonnull Coord end) {
        this.start = start;
        this.end = end;
    }

    public float getX()
    {
        return interpolation.apply(start.x, end.x, change);
    }

    public float getY()
    {
        return interpolation.apply(start.y, end.y, change);
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

    @Nonnull
    public Coord getStart() {
        return start;
    }

    public void setStart(@Nonnull Coord start) {
        this.start = start;
        change = 0f;
    }

    @Nonnull
    public Coord getEnd() {
        return end;
    }

    public void setEnd(@Nonnull Coord end) {
        this.end = end;
        change = 0f;
    }

    @Nullable
    public Runnable getCompleteRunner() {
        return completeRunner;
    }

    public void setCompleteRunner(@Nullable Runnable completeRunner) {
        this.completeRunner = completeRunner;
    }

    @Override
    public void onComplete() {
        start = end;
        if(completeRunner != null)
            completeRunner.run();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordGlider that = (CoordGlider) o;

        if (!start.equals(that.start)) return false;
        if (!end.equals(that.end)) return false;
        return interpolation.equals(that.interpolation);
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        result = 31 * result + interpolation.hashCode();
        return result;
    }
}
