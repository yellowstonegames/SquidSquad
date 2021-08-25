package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allows specifying a smoothly-changing float position using a libGDX Vector2 for the start and the end, with a change
 * amount that affects how far a position has moved from start to end. Typical usage sets the start with
 * {@link #setStart(Vector2)} once and the end more than once with {@link #setEnd(Vector2)}, changing the end each time the
 * move is complete. The x and y positions are automatically calculated in {@link #getX()} and {@link #getY()}, and
 * their values will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 * <br>
 * This is extremely similar to {@link CoordGlider}, but instead of locking positions to integer coordinates, this
 * allows movement to non-integer positions. This should be useful for small movements like shaking or bumping.
 */
public class VectorGlider implements Glider {
    protected float change = 0f;
    protected @Nonnull Vector2 start;
    protected @Nonnull Vector2 end;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected @Nullable Runnable completeRunner;

    public VectorGlider() {
        start = new Vector2();
        end = new Vector2();
    }

    /**
     * Assigns {@link Vector2#Zero} into start and end into end; does not continue to use the reference to the parameter end, or Zero.
     * @param end will be copied into end
     */
    public VectorGlider(@Nonnull Vector2 end) {
        this.start = new Vector2(0f, 0f);
        this.end = end.cpy();
    }

    /**
     * Copies start into start and end into end; does not continue to use references to the parameters.
     * @param start will be copied into start
     * @param end will be copied into end
     */
    public VectorGlider(@Nonnull Vector2 start, @Nonnull Vector2 end) {
        this.start = start.cpy();
        this.end = end.cpy();
    }

    public VectorGlider(VectorGlider other) {
        this(other.start, other.end);
        this.change = other.change;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
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
    public Vector2 getStart() {
        return start;
    }

    public void setStart(@Nonnull Vector2 start) {
        this.start.set(start);
        change = 0f;
    }

    @Nonnull
    public Vector2 getEnd() {
        return end;
    }

    public void setEnd(@Nonnull Vector2 end) {
        this.end.set(end);
        change = 0f;
    }

    @Override
    public void onComplete() {
        start.set(end);
        if(completeRunner != null) {
            completeRunner.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VectorGlider that = (VectorGlider) o;

        if (!start.epsilonEquals(that.start)) return false;
        if (!end.epsilonEquals(that.end)) return false;
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
