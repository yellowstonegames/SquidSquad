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

package com.github.yellowstonegames.grid;


import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.PouchRandom;

/**
 * A drunkard's-walk-like algorithm for line-drawing "wobbly" paths.
 * This produces lines as {@link ObjectDeque} of {@link Coord}, where Coords that are adjacent in the ObjectDeque are
 * guaranteed to be orthogonally adjacent, but the path as a whole is not guaranteed to have all unique Coords (that is,
 * the line may cross over its previous path). This doesn't implement {@link LineDrawer} because it has the restriction
 * that the line can't extend off the bounds of some rectangular grid (usually the map).
 * <br>
 * The line() methods here use an EnhancedRandom (and will make their own FourWheelRandom if they don't take one as a
 * parameter) to make a choice between orthogonal directions to travel in. Because they can go around the target instead
 * of straight to it, they also need a width and height for the map, so they don't wander over the edge. You can also
 * pass a weight to one of the line() methods, which affects how straight the wobbly path will be (1.0 being just about
 * perfectly straight, 0.5 being very chaotic, and less than 0.5 being almost unrecognizable as a path). Lower weights
 * make the case where the path crosses itself more likely.
 * <br>
 * Based on <a href="https://github.com/mpatraw/butterfly">Michael Patraw's C code, used for cave carving in his map
 * generator</a> (broken link).
 */
public final class DrunkenWalk {
    private DrunkenWalk(){}
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the Drunkard's Walk algorithm. Returns a List of Coord
     * in order.
     * <br>
     * Equivalent to calling {@code line(startX, startY, endX, endY, width, height, 0.75, new RNG())} .
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param width maximum map width
     * @param height maximum map height
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectDeque<Coord> line(int startX, int startY, int endX, int endY, int width, int height) {
        return line(startX, startY, endX, endY, width, height, 0.75f, new PouchRandom());
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the Drunkard's Walk algorithm. Returns a List of Coord
     * in order.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param width maximum map width
     * @param height maximum map height
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random walks, 0.9 is almost a straight line.
     * @param rng the random number generator to use
     * @return List of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectDeque<Coord> line(int startX, int startY, int endX, int endY,
                                   int width, int height, float weight, EnhancedRandom rng) {
        return line(startX, startY, endX, endY, width, height, weight, rng,
                new ObjectDeque<>(Math.abs(startX - endX) + Math.abs(startY - endY)));
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using the Drunkard's Walk algorithm. Returns a List of Coord
     * in order. Modifies buffer if it is non-null, or creates an ObjectDeque if buffer is null.
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param width maximum map width
     * @param height maximum map height
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random walks, 0.9 is almost a straight line.
     * @param rng the random number generator to use
     * @param buffer an ObjectDeque of Coord that will be appended to if non-null or created if null
     * @return buffer, after changes, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectDeque<Coord> line(int startX, int startY, int endX, int endY,
                                   int width, int height, float weight, EnhancedRandom rng, ObjectDeque<Coord> buffer) {
        if(buffer == null) buffer = new ObjectDeque<>(Math.abs(startX - endX) + Math.abs(startY - endY));
        Coord start = Coord.get(startX, startY);
        Direction dir;
        do {
            buffer.add(start);
            dir = stepWobbly(start.x, start.y, endX, endY, weight, width, height, rng);
            start = start.translate(dir);
            if(start.x < 1 || start.y < 1 || start.x >= width - 1 || start.y >= height - 1)
                break;
        }while (dir != Direction.NONE);
        return buffer;
    }

    /**
     * Internal use. Drunkard's walk algorithm, single step.
     * <a href="http://mpatraw.github.io/libdrunkard/">Based on Michael Patraw's C code, used for cave carving</a>
     * (broken link).
     * @param currentX the x coordinate of the current point
     * @param currentY the y coordinate of the current point
     * @param targetX the x coordinate of the point to wobble towards
     * @param targetY the y coordinate of the point to wobble towards
     * @param weight between 0.5 and 1.0, usually. 0.6 makes very random walks, 0.9 is almost a straight line.
     * @param width maximum map width
     * @param height maximum map height
     * @param rng the random number generator to use
     * @return a Direction, either UP, DOWN, LEFT, or RIGHT if we should move, or NONE if we have reached our target
     */
    private static Direction stepWobbly(int currentX, int currentY, int targetX, int targetY, float weight,
                                        int width, int height, EnhancedRandom rng)
    {
        int dx = targetX - currentX;
        int dy = targetY - currentY;

        // integer signum or sign-of, thanks to project nayuki
        dx = (dx >> 31 | -dx >>> 31);
        dy = (dy >> 31 | -dy >>> 31);

        float r = rng.nextFloat();
        Direction dir;
        if (dx == 0 && dy == 0)
        {
            return Direction.NONE;
        }
        else if (dx == 0 || dy == 0)
        {
            int dx2 = 0, dy2 = (dx == 0) ? dy : dx;
            if (r >= (weight * 0.5f))
            {
                r -= weight * 0.5f;
                if (r < (weight + (1 - weight) * 2) * (1f / 6))
                {
                    dx2 = -1;
                    dy2 = 0;
                }
                else if (r < (weight + (1 - weight) * 2) * (1f / 3))
                {
                    dx2 = 1;
                    dy2 = 0;
                }
                else
                {
                    dy2 = -dy2;
                }
            }
            dir = Direction.getCardinalDirection(dx2, dy2);

        }
        else
        {
            if (r < weight * 0.5f)
            {
                dy = 0;
            }
            else if (r < weight)
            {
                dx = 0;
            }
            else if (r < weight + (1 - weight) * 0.5f)
            {
                dx = -dx;
                dy = 0;
            }
            else
            {
                dx = 0;
                dy = -dy;
            }
            dir = Direction.getCardinalDirection(dx, dy);
        }
        if(currentX + dir.deltaX <= 0 || currentX + dir.deltaX >= width - 1) {
            if (currentY < targetY) dir = Direction.DOWN;
            else if (currentY > targetY) dir = Direction.UP;
        }
        else if(currentY + dir.deltaY <= 0 || currentY + dir.deltaY >= height - 1) {
            if (currentX < targetX) dir = Direction.RIGHT;
            else if (currentX > targetX) dir = Direction.LEFT;
        }
        return dir;
    }

    /**
     * Draws a line from start to end using the Drunkard's Walk algorithm. Returns a List of Coord in order.
     * @param start starting point
     * @param end ending point
     * @param width maximum map width
     * @param height maximum map height
     * @return List of Coord, including start and end and all points walked between
     */
    public static ObjectDeque<Coord> line(Coord start, Coord end, int width, int height)
    {
        return line(start.x, start.y, end.x, end.y, width, height);
    }
}
