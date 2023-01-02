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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectList;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A shared interface for line-drawing classes, so instances can be interchanged.
 * One of these can draw a line as an ObjectList of Coord (which may reuse an existing ObjectList) or as a
 * newly-allocated array of Coord. It can also check whether there is a complete line that can be drawn between a start
 * and an end point, given a 2D array of the resistance of cells along the way that might block the line's passage.
 */
public interface LineDrawer {
    /**
     * Gets the last line drawn using the internal buffer this carries, rather than an explicitly-specified buffer.
     * @return an ObjectList of Coord that contains the last line drawn with this LineDrawer's internal buffer
     */
    ObjectList<Coord> getLastLine();
    
    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #getLastLine()}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    ObjectList<Coord> drawLine(Coord a, Coord b);

    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #getLastLine()}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX  the x coordinate of the starting point
     * @param startY  the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY);

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). Reuses {@link #getLastLine()}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX  the x coordinate of the starting point
     * @param startY  the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength);

    /**
     * Generates a 2D Bresenham line between two points. If you want to save
     * some memory, you can reuse an ObjectList of Coord, {@code buffer},
     * which will be cleared and filled with the resulting line of Coord.
     * If {@code buffer} is null, this will create a new ObjectList of Coord
     * and return that.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX  the x coordinate of the starting point
     * @param startY  the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param buffer  an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, ObjectList<Coord> buffer);

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     *
     * @param start         the starting point
     * @param target        the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer        an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap,
                        ObjectList<Coord> buffer);

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). If you want to save
     * some memory, you can reuse an ObjectList of Coord, {@code buffer},
     * which will be cleared and filled with the resulting line of Coord.
     * If {@code buffer} is null, this will create a new ObjectList of Coord
     * and return that.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX    the x coordinate of the starting point
     * @param startY    the y coordinate of the starting point
     * @param targetX   the x coordinate of the target point
     * @param targetY   the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @param buffer    an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer);

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     *
     * @param startX        the x-coordinate of the starting point
     * @param startY        the y-coordinate of the starting point
     * @param targetX       the x-coordinate of the target point
     * @param targetY       the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer        an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(int startX, int startY, int targetX, int targetY,
                        @NonNull float[][] resistanceMap, ObjectList<Coord> buffer);

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. The {@code maxLength} is measured using Chebyshev distance, where diagonally adjacent cells are
     * considered exactly as distant as orthogonally-adjacent cells.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     *
     * @param startX        the x-coordinate of the starting point
     * @param startY        the y-coordinate of the starting point
     * @param targetX       the x-coordinate of the target point
     * @param targetY       the y-coordinate of the target point
     * @param maxLength     the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer        an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                        @NonNull float[][] resistanceMap, ObjectList<Coord> buffer);

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     *
     * @param start         the starting point
     * @param target        the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap);

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     *
     * @param startX        the x-coordinate of the starting point
     * @param startY        the y-coordinate of the starting point
     * @param targetX       the x-coordinate of the target point
     * @param targetY       the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(int startX, int startY, int targetX, int targetY,
                        @NonNull float[][] resistanceMap);

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     *
     * @param startX        the x-coordinate of the starting point
     * @param startY        the y-coordinate of the starting point
     * @param targetX       the x-coordinate of the target point
     * @param targetY       the y-coordinate of the target point
     * @param maxLength     the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                        @NonNull float[][] resistanceMap);

    /**
     * Generates a 2D Bresenham line between two points.
     * This allocates a new array with each call, sized to fit the
     * line exactly. It does not change {@link #getLastLine()}.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    Coord[] drawLineArray(Coord a, Coord b);

    /**
     * Generates a 2D Bresenham line between two points. Returns an array
     * of Coord instead of a ObjectList.
     * This allocates a new array with each call, sized to fit the
     * line exactly.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX  the x coordinate of the starting point
     * @param startY  the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return an array of Coord points along the line
     */
    Coord[] drawLineArray(int startX, int startY, int targetX, int targetY);

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength. Returns an array
     * of Coord instead of an ObjectList.
     * This allocates a new array with each call, sized to fit the
     * line exactly.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX    the x coordinate of the starting point
     * @param startY    the y coordinate of the starting point
     * @param targetX   the x coordinate of the target point
     * @param targetY   the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    Coord[] drawLineArray(int startX, int startY, int targetX, int targetY, int maxLength);
}
