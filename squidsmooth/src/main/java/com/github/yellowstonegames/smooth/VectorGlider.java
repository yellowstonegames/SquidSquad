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
import com.badlogic.gdx.math.Vector2;

import com.github.tommyettinger.digital.Interpolations;
import org.jetbrains.annotations.NotNull;

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
 * <br>
 * This is a type of Glider, and so is compatible with other Gliders (it can also be merged with them).
 */
public class VectorGlider extends Glider {
    protected @NotNull Vector2 start;
    protected @NotNull Vector2 end;

    public VectorGlider() {
        super();
        start = new Vector2();
        end = new Vector2();
    }

    /**
     * Assigns {@link Vector2#Zero} into start and end into end; does not continue to use the reference to the parameter end, or Zero.
     * @param end will be copied into end
     */
    public VectorGlider(@NotNull Vector2 end) {
        super(new Changer("x", 0f, end.x), new Changer("y", 0f, end.y));
        this.start = new Vector2(0f, 0f);
        this.end = end.cpy();
    }

    /**
     * Copies start into start and end into end; does not continue to use references to the parameters.
     * @param start will be copied into start
     * @param end will be copied into end
     */
    public VectorGlider(@NotNull Vector2 start, @NotNull Vector2 end) {
        super(new Changer("x", start.x, end.x), new Changer("y", start.y, end.y));
        this.start = start.cpy();
        this.end = end.cpy();
    }

    /**
     * Copies start into start and end into end; does not continue to use references to the parameters.
     * @param start will be copied into start
     * @param end will be copied into end
     * @param interpolation how to interpolate from start to end; typically a constant from {@link Interpolation}
     * @param completeRunner a Runnable that, if non-null, will be run when the glide completes
     */
    public VectorGlider(@NotNull Vector2 start, @NotNull Vector2 end, Interpolations.@NotNull Interpolator interpolation, Runnable completeRunner) {
        super(interpolation, completeRunner, new Changer("x", start.x, end.x), new Changer("y", start.y, end.y));
        this.start = start.cpy();
        this.end = end.cpy();
    }

    public VectorGlider(VectorGlider other) {
        super(other);
        this.start = other.start.cpy();
        this.end = other.end.cpy();
        this.change = other.change;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
    }

    public VectorGlider(Glider other) {
        super(other);
        this.start = new Vector2(other.getStartFloat("x"), other.getStartFloat("y"));
        this.end = new Vector2(other.getEndFloat("x"), other.getEndFloat("y"));
        this.change = other.change;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
    }

    public float getX()
    {
        return getFloat("x");
    }

    public float getY()
    {
        return getFloat("y");
    }

    @NotNull
    public Vector2 getStart() {
        return start;
    }

    public void setStart(@NotNull Vector2 start) {
        this.start.set(start);
        setStartFloat("x", start.x);
        setStartFloat("y", start.y);
        change = 0f;
    }

    @NotNull
    public Vector2 getEnd() {
        return end;
    }

    public void setEnd(@NotNull Vector2 end) {
        this.end.set(end);
        setEndFloat("x", end.x);
        setEndFloat("y", end.y);
        change = 0f;
    }

    @Override
    public void onComplete() {
        start.set(end);
        super.onComplete();
    }

    @Override
    public String toString() {
        return "VectorGlider{" +
                "start=" + start +
                ", end=" + end +
                ", changers=" + changers +
                ", change=" + change +
                '}';
    }
}
