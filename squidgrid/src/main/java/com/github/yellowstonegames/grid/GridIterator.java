/*
 * Copyright (c) 2022-2024 See AUTHORS file.
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

package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.annotations.Beta;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

@Beta
public abstract class GridIterator implements Iterator<Coord> {
    public int width;
    public int height;

    /**
     * An iterator that returns cells in a square around a location. Cells are
     * iterated from bottom left to top right in this square. A square size of
     * {@code 0} creates an iterator that returns one location (the starting
     * one); a square of size {@code 1} is an iterator that returns at most 9
     * locations, (start.x-1,start.y+1), (start.x,start.y+1), ...; a square of
     * size {@code 2} returns at most ((2*2)+1)*((2*2)+1) = 25 locations, etc..
     *
     * <p>
     * Instances of this iterator never return a coordinate outside the map.
     * </p>
     *
     * @author smelC
     */
    public static class CenteredSquare extends GridIterator {
        protected @Nullable Coord previous;

        protected int xstart;
        protected int ystart;

        protected int size;

        protected boolean done;

        /**
         * An iterator to iterate in the square of size {@code size} around
         * {@code (x, y)}.
         *
         * @param width
         *            The map's width
         * @param height
         *            The map's height
         * @param x
         *            The starting x coordinate.
         * @param y
         *            The starting y coordinate.
         * @param size
         *            The square's size. Can be {@code 0} but not negative.
         * @throws IllegalStateException
         *             If {@code width <= 0 || height <= 0 || size < 0}.
         */
        public CenteredSquare(int width, int height, int x, int y, int size) {
            this.width = width;
            if (width <= 0)
                throw new IllegalStateException("Cannot build a centered square iterator over an empty grid");
            this.height = height;
            if (height <= 0)
                throw new IllegalStateException("Cannot build a centered square iterator over an empty grid");

            this.xstart = x;
            this.ystart = y;

            if (size < 0)
                throw new IllegalStateException("Cannot build a square iterator with a negative size");

            this.size = size;
        }

        /**
         * An iterator to iterate in the square of size {@code size} around
         * {@code start}.
         *
         * @param width
         *            The grid's width
         * @param height
         *            The grid's height
         * @param start
         *            The starting coordinate.
         */
        public CenteredSquare(int width, int height, Coord start, int size) {
            this(width, height, start.x, start.y, size);
        }

        @Override
        public boolean hasNext() {
            return findNext(false) != null;
        }

        @Override
        public Coord next() {
            final Coord next = findNext(true);
            if (next == null)
                throw new NoSuchElementException();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected @Nullable Coord findNext(boolean mutate) {
            while (!done) {
                final Coord result = findNext0();
                if (result != null) {
                    if (isInGrid(result.x, result.y)) {
                        if (mutate)
                            previous = result;
                        return result;
                    }
                    /*
                     * We need to record progression, even if mutation isn't
                     * required. This is correct, because this is progression
                     * that isn't observable (skipping cells outside the map).
                     */
                    previous = result;
                }
            }
            return null;
        }

        /*
         * This method doesn't care about validity, findNext(boolean) handles it
         */
        protected @Nullable Coord findNext0() {
            if (previous == null) {
                /* Init */
                /* We're in SquidSquad coordinates here ((0,0) is bottom left) */
                return Coord.get(xstart - size, ystart - size);
            }

            assert xstart - size <= previous.x && previous.x <= xstart + size;
            assert ystart - size <= previous.y && previous.y <= ystart + size;

            if (previous.x == xstart + size) {
                /* Need to go up and left (one column up, go left) */
                if (previous.y == ystart + size) {
                    /* We're done */
                    done = true;
                    return null;
                } else
                    return Coord.get(xstart - size, previous.y + 1);
            } else {
                /* Can go right in the same line */
                return Coord.get(previous.x + 1, previous.y);
            }
        }

        protected boolean isInGrid(int x, int y) {
            return 0 <= x && x < width && 0 <= y && y < height;
        }
    }


    /**
     * An iterator to iterate from a starting position (exclusive) and going up.
     * This iterator cycles when reaching the map's bound, but it iterates at
     * most once on a cell, i.e. it does at most one roll over a column of the
     * map.
     *
     * @author smelC
     */
    public static class VerticalUp extends GridIterator {

        /** The starting X-coordinate */
        protected int startx;

        /** The starting Y-coordinate */
        protected int starty;

        /* Initially null */
        protected Coord prev;

        /**
         * An iterator to iterate vertically, starting AFTER
         * {@code (startx, starty)}. This iterates cycles when it reaches the
         * map's bound, but it iterates at most once on a cell, i.e. it does at
         * most one roll over a column of the map.
         *
         * @param startx
         *            The starting X-coordinate.
         * @param starty
         *            The starting vertical-coordinate.
         * @param width
         *            The map's width.
         * @param height
         *            The map's height.
         */
        public VerticalUp(int startx, int starty, int width, int height) {
            if (startx < 0 || width <= startx)
                throw new IllegalStateException(
                        "Illegal x-coordinate: " + startx + " (map's width: " + width + ")");
            this.startx = startx;
            if (starty < 0 || height <= starty)
                throw new IllegalStateException(
                        "Illegal y-coordinate: " + starty + " (map's width: " + height + ")");
            this.starty = starty;

            this.width = width;
            this.height = height;
        }

        /**
         * An iterator to iterate vertically, starting AFTER {@code start}. This
         * iterates cycles when it reaches the map's bound, but it iterates at
         * most once on a cell, i.e. it does at most one roll over a column of
         * the map.
         *
         * @param start
         *            The starting coordinate.
         * @param width
         *            The map's width.
         * @param height
         *            The map's height.
         */
        public VerticalUp(Coord start, int width, int height) {
            this(start.x, start.y, width, height);
        }

        @Override
        public boolean hasNext() {
            final Coord n = findNext();
            if (prev == null)
                return n != null;
            else {
                /* Not done && has next */
                return (prev.x != startx || prev.y != starty) && n != null;
            }
        }

        @Override
        public Coord next() {
            prev = findNext();
            if (prev == null)
                throw new NoSuchElementException();
            return prev;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected Coord findNext() {
            if (prev == null) {
                /* First iteration */
                if (starty == 0)
                    /* Start from the bottom */
                    return Coord.get(startx, 0);
                else
                    /* Start from the cell above (startx, starty) */
                    return Coord.get(startx, starty + 1);
            } else {
                if (prev.x == startx && prev.y == starty)
                    /* Done iterating */
                    return null;
                else if (prev.y == 0) {
                    /* Continue from the bottom */
                    return Coord.get(startx, 0);
                } else {
                    /* Go up */
                    assert 0 < prev.y && prev.y < height;
                    final Coord r = Coord.get(startx, prev.y + 1);
                    if (r.y == starty)
                        /* We would return the starting position */
                        return null;
                    else
                        return r;
                }
            }
        }

    }

}
