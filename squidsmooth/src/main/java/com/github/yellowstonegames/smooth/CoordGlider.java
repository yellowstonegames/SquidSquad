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
import com.github.yellowstonegames.grid.Coord;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Allows specifying a smoothly-changing float position using an exact Coord for the start and the end, with a change
 * amount that affects how far a position has moved from start to end. Typical usage sets the start with
 * {@link #setStart(Coord)} once and the end more than once with {@link #setEnd(Coord)}, changing the end each time the
 * move is complete. The x and y positions are automatically calculated in {@link #getX()} and {@link #getY()}, and
 * their values will be different every time {@link #setChange(float)} is called with a different amount. You can
 * optionally use an {@link Interpolation} to make the rate of change different.
 * <br>
 * This is a type of Glider, and so is compatible with other Gliders (it can also be merged with them).
 */
public class CoordGlider extends Glider {
    protected @NonNull Coord start;
    protected @NonNull Coord end;

    public CoordGlider() {
        super(new Changer("x", 0f, 0f), new Changer("y", 0f, 0f));
        end = start = Coord.get(0, 0);
    }
    public CoordGlider(@NonNull Coord start) {
        super(new Changer("x", start.x(), start.x()), new Changer("y", start.y(), start.y()));
        this.start = start;
        this.end = start;
    }
    public CoordGlider(@NonNull Coord start, @NonNull Coord end) {
        super(new Changer("x", start.x(), end.x()), new Changer("y", start.y(), end.y()));
        this.start = start;
        this.end = end;
    }
    public CoordGlider(@NonNull Interpolation interpolation, Runnable completeRunner, @NonNull Coord start, @NonNull Coord end) {
        super(interpolation, completeRunner, new Changer("x", start.x(), end.x()), new Changer("y", start.y(), end.y()));
        this.start = start;
        this.end = end;
    }

    public CoordGlider(@NonNull CoordGlider other) {
        super(other);
        this.start = other.start;
        this.end = other.end;
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

    @NonNull
    public Coord getStart() {
        return start;
    }

    public void setStart(@NonNull Coord start) {
        this.start = start;
        setStartFloat("x", start.x());
        setStartFloat("y", start.y());
        change = 0f;
    }

    @NonNull
    public Coord getEnd() {
        return end;
    }

    public void setEnd(@NonNull Coord end) {
        this.end = end;
        setEndFloat("x", end.x());
        setEndFloat("y", end.y());
        change = 0f;
    }

    @Override
    public void onComplete() {
        start = end;
        resetToCurrent();
        super.onComplete();
    }
}
