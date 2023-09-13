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
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Provides a means to generate Bresenham lines in 2D.
 * You can use the static methods {@link #line(Coord, Coord)}, {@link #lineArray(Coord, Coord)},
 * and {@link #reachable(Coord, Coord, float[][])} (with many overloads possible), or their
 * equivalents using the {@link LineDrawer} interface: {@link #drawLine(Coord, Coord)},
 * {@link #drawLineArray(Coord, Coord)}, or {@link #isReachable(Coord, Coord, float[][])}.
 * The more verbose name of the pair is the instance method.
 *
 * @author <a href="http://squidpony.com">Eben Howard</a> - howard@squidpony.com
 * @author Lewis Potter
 * @author <a href="https://github.com/tommyettinger">Tommy Ettinger</a>
 * @author smelC
 */
public class BresenhamLine implements LineDrawer {

    public final ObjectList<Coord> lastLine = new ObjectList<>();

    /**
     * Makes a new BresenhamLine and initializes its only state, {@link #lastLine}.
     */
    public BresenhamLine() {
    }

    /**
     * Generates a 2D Bresenham line between two points. If you want
     * to save some memory, you can use
     * {@link #line(int, int, int, int, ObjectList)}, which reuses
     * an ObjectList of Coord as a buffer. You can also use
     * {@link #lineArray(int, int, int, int)}; although that allocates
     * a new array each time, having an array may be useful for some APIs.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static ObjectList<Coord> line(Coord a, Coord b) {
        return line(a.x, a.y, b.x, b.y, 0x7fffffff, null);
    }


    /**
     * Generates a 2D Bresenham line between two points.
     * This allocates a new array with each call, sized to fit the
     * line exactly.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Coord[] lineArray(Coord a, Coord b) {
        return lineArray(a.x, a.y, b.x, b.y, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points. If you want
     * to save some memory, you can use
     * {@link #line(int, int, int, int, ObjectList)}, which reuses
     * an ObjectList of Coord as a buffer. You can also use
     * {@link #lineArray(int, int, int, int)}; although that allocates
     * a new array each time, having an array may be useful for some APIs.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    public static ObjectList<Coord> line(int startX, int startY, int targetX, int targetY) {
        // largest positive int for maxLength; a ObjectList cannot actually be given that many elements on the JVM
        return line(startX, startY, targetX, targetY, 0x7fffffff, null);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). If you want to
     * save some memory, you can use
     * {@link #line(int, int, int, int, int, ObjectList)}, which reuses an
     * ObjectList of Coord as a buffer. You can also use
     * {@link #lineArray(int, int, int, int, int)}; although that allocates
     * a new array each time, having an array may be useful for some APIs.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an ObjectList of Coord points along the line
     */
    public static ObjectList<Coord> line(int startX, int startY, int targetX, int targetY, int maxLength) {
        return line(startX, startY, targetX, targetY, maxLength, null);
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    public static ObjectList<Coord> line(int startX, int startY, int targetX, int targetY, ObjectList<Coord> buffer) {
        return line(startX, startY, targetX, targetY, 0x7FFFFFFF, buffer);
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    public static ObjectList<Coord> line(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        if(buffer == null) {
            buffer = new ObjectList<>(Math.max(ax, ay) + 1);
        }
        else {
            buffer.clear();
        }

        // double ax and ay
        ax <<= 1;
        ay <<= 1;

        // integer signum or sign-of, thanks to project nayuki
        int signx = (dx >> 31 | -dx >>> 31);
        int signy = (dy >> 31 | -dy >>> 31);

        int x = startX;
        int y = startY;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (buffer.size() < maxLength) {
                buffer.add(Coord.get(x, y));
                if (x == targetX) {
                    return buffer;
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            while (buffer.size() < maxLength) {
                buffer.add(Coord.get(x, y));
                if (y == targetY) {
                    return buffer;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }

                y += signy;
                deltax += ax;
            }
        }
        return buffer;
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Euclidean distance, or "as the crow flies"). If you want to save
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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    public static ObjectList<Coord> lineEuclidean(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        if(buffer == null) {
            buffer = new ObjectList<>(Math.min((int)Math.sqrt(dx * dx + dy * dy) + 1, maxLength));
        }
        else {
            buffer.clear();
        }

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);
        int max2 = maxLength * maxLength;
        ax <<= 1;
        ay <<= 1;

        int signX = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signY = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startX;
        int y = startY;

        int deltaX, deltaY;
        dx = 0;
        dy = 0;
        if (ax >= ay) /* x dominant */ {
            deltaY = ay - (ax >> 1);
            while ((dx * dx + dy * dy) < max2) {
                buffer.add(Coord.get(x, y));
                if (x == targetX) {
                    return buffer;
                }

                if (deltaY >= 0) {
                    y += signY;
                    deltaY -= ax;
                    dy++;
                }

                x += signX;
                deltaY += ay;
                dx++;
            }
        } else /* y dominant */ {
            deltaX = ax - (ay >> 1);
            while ((dx * dx + dy * dy) < max2) {
                buffer.add(Coord.get(x, y));
                if (y == targetY) {
                    return buffer;
                }

                if (deltaX >= 0) {
                    x += signX;
                    deltaX -= ay;
                    dx++;
                }


                y += signY;
                deltaX += ax;
                dy++;
            }
        }
        return buffer;
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case it is ignored), or may be an existing ObjectList of Coord (which will
     * be cleared if it has any contents, and filled with the line's Coord points). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; if null, will be ignored
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap,
                                    @Nullable ObjectList<Coord> buffer){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case it is ignored), or may be an existing ObjectList of Coord (which will
     * be cleared if it has any contents, and filled with the line's Coord points). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; if null, will be ignored
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    @NonNull float[][] resistanceMap, @Nullable ObjectList<Coord> buffer){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. The {@code maxLength} is measured using Chebyshev distance, where diagonally adjacent cells are
     * considered exactly as distant as orthogonally-adjacent cells.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case it is ignored), or may be an existing ObjectList of Coord (which will
     * be cleared if it has any contents, and filled with the line's Coord points). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; if null, will be ignored
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    @NonNull float[][] resistanceMap, @Nullable ObjectList<Coord> buffer) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        int dist = Math.max(ax, ay);

        if(buffer != null) {
            buffer.clear();
        }

        if(maxLength <= 0)
            return false;

        if(startX == targetX && startY == targetY) {
            if(buffer != null)
                buffer.add(Coord.get(startX, startY));
            return true; // already at the point; we can see our own feet just fine!
        }
        float decay = 1f / dist;
        float currentForce = 1f;

        ax <<= 1;
        ay <<= 1;

        int signX = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signY = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startX;
        int y = startY;

        int deltaX, deltaY;

        int size = 0;
        if (ax >= ay) /* x dominant */ {
            deltaY = ay - (ax >> 1);
            while (size < maxLength) {
                if(buffer != null)
                    buffer.add(Coord.get(x, y));
                size++;
                if (x == targetX) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= -0.001f) {
                    return false;//too much resistance
                }

                if (deltaY >= 0) {
                    y += signY;
                    deltaY -= ax;
                }

                x += signX;
                deltaY += ay;
            }
        } else /* y dominant */ {
            deltaX = ax - (ay >> 1);
            while (size < maxLength) {
                if(buffer != null)
                    buffer.add(Coord.get(x, y));
                size++;
                if (y == targetY) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= -0.001f) {
                    return false;//too much resistance
                }

                if (deltaX >= 0) {
                    x += signX;
                    deltaX -= ay;
                }

                y += signY;
                deltaX += ax;

            }
        }
        return false;//never got to the target point
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. The {@code maxLength} is measured using Euclidean distance, where diagonally adjacent cells are
     * considered about 1.4 times as distant as orthogonally-adjacent cells; this is the natural way in the real world.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case it is ignored), or may be an existing ObjectList of Coord (which will
     * be cleared if it has any contents, and filled with the line's Coord points). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; if null, will be ignored
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachableEuclidean(int startX, int startY, int targetX, int targetY, int maxLength,
                                             @NonNull float[][] resistanceMap, @Nullable ObjectList<Coord> buffer) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        float dist = Math.min((float) Math.sqrt(dx * dx + dy * dy), maxLength);

        if(buffer != null) {
            buffer.clear();
        }
        if(maxLength <= 0) return false;

        if(startX == targetX && startY == targetY) {
            if(buffer != null) buffer.add(Coord.get(startX, startY));
            return true; // already at the point; we can see our own feet just fine!
        }
        float currentBlockage = 1f;

        ax <<= 1;
        ay <<= 1;

        int signX = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signY = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startX;
        int y = startY;

        int changeX = 0, changeY = 0;

        int deltaX, deltaY;

        int size = 0;
        if (ax >= ay) /* x dominant */ {
            deltaY = ay - (ax >> 1);
            while (size < maxLength) {
                if(buffer != null) {
                    buffer.add(Coord.get(x, y));
                }
                size++;
                if (x == targetX) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentBlockage -= resistanceMap[x][y];
                }

                if (deltaY >= 0) {
                    y += signY;
                    deltaY -= ax;
                    ++changeY;
                }

                x += signX;
                deltaY += ay;
                ++changeX;

                if ((changeX * changeX + changeY * changeY) >= dist * dist * currentBlockage + 0.5f) {
                    return false;//too much resistance
                }
            }
        } else /* y dominant */ {
            deltaX = ax - (ay >> 1);
            while (size < maxLength) {
                if(buffer != null) {
                    buffer.add(Coord.get(x, y));
                }
                size++;

                if (y == targetY) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentBlockage -= resistanceMap[x][y];
                }

                if (deltaX >= 0) {
                    x += signX;
                    deltaX -= ay;
                    ++changeX;
                }

                y += signY;
                deltaX += ax;
                ++changeY;

                if ((changeX * changeX + changeY * changeY) >= dist * dist * currentBlockage + 0.5f) {
                    return false;//too much resistance
                }
            }
        }
        return false;//never got to the target point
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    @NonNull float[][] resistanceMap) {
        if(maxLength <= 0)
            return false;
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        int dist = Math.max(ax, ay);

        if(startX == targetX && startY == targetY) {
            return true; // already at the point; we can see our own feet just fine!
        }
        float decay = 1f / dist;
        float currentForce = 1f;

        ax <<= 1;
        ay <<= 1;

        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startX;
        int y = startY;

        int deltax, deltay, traveled = 0;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (traveled < maxLength) {
                ++traveled;
                if (x == targetX) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= -0.001f) {
                    return false;//too much resistance
                }

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            while (traveled < maxLength) {
                ++traveled;
                if (y == targetY) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= -0.001f) {
                    return false;//too much resistance
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }

                y += signy;
                deltax += ax;

            }
        }
        return false;//never got to the target point
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return an array of Coord points along the line
     */
    public static Coord[] lineArray(int startX, int startY, int targetX, int targetY) {
        // largest positive int for maxLength; it is extremely unlikely that this could be reached
        return lineArray(startX, startY, targetX, targetY, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength.. Returns an array
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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    public static Coord[] lineArray(int startX, int startY, int targetX, int targetY, int maxLength) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int ax = (dx = Math.abs(dx)) << 1;
        int ay = (dy = Math.abs(dy)) << 1;

        int x = startX;
        int y = startY;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            Coord[] result = new Coord[Math.min(maxLength, dx+1)];
            for (int i = 0; i <= dx && i < maxLength; i++) {
                result[i] = Coord.get(x, y);

                if (deltay >= 0) {
                    y += signy;
                    deltay -= ax;
                }

                x += signx;
                deltay += ay;
            }
            return result;
        } else /* y dominant */ {
            deltax = ax - (ay >> 1);
            Coord[] result = new Coord[Math.min(maxLength, dy+1)];
            for (int i = 0; i <= dy && i < maxLength; i++) {
                result[i] = Coord.get(x, y);

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
            return result;
        }
    }

    /**
     * Gets the last line drawn using the internal buffer this carries, rather than an explicitly-specified buffer.
     *
     * @return an ObjectList of Coord that contains the last line drawn with this BresenhamLine's internal buffer
     */
    @Override
    public ObjectList<Coord> getLastLine() {
        return lastLine;
    }

    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #lastLine}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    @Override
    public ObjectList<Coord> drawLine(Coord a, Coord b) {
        lastLine.clear();
        return line(a.x, a.y, b.x, b.y, 0x7FFFFFFF, lastLine);
    }

    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #lastLine}
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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY) {
        lastLine.clear();
        // largest positive int for maxLength; a ObjectList cannot actually be given that many elements on the JVM
        return line(startX, startY, targetX, targetY, 0x7fffffff, lastLine);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). Reuses {@link #lastLine}
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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength) {
        lastLine.clear();
        return line(startX, startY, targetX, targetY, maxLength, lastLine);
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, ObjectList<Coord> buffer) {
        return line(startX, startY, targetX, targetY, 0x7FFFFFFF, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap,
                               ObjectList<Coord> buffer){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap, buffer);
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer) {
        return line(startX, startY, targetX, targetY, maxLength, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY,
                               @NonNull float[][] resistanceMap, ObjectList<Coord> buffer){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap, buffer);
    }

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
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                               @NonNull float[][] resistanceMap, ObjectList<Coord> buffer) {
        return reachable(startX, startY, targetX, targetY, maxLength, resistanceMap, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY,
                               @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                               @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, maxLength, resistanceMap);
    }
    /**
     * Generates a 2D Bresenham line between two points.
     * This allocates a new array with each call, sized to fit the
     * line exactly. It does not change {@link #lastLine}.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    @Override
    public Coord[] drawLineArray(Coord a, Coord b) {
        return lineArray(a.x, a.y, b.x, b.y, 0x7FFFFFFF);
    }

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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return an array of Coord points along the line
     */
    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY) {
        // largest positive int for maxLength; it is extremely unlikely that this could be reached
        return lineArray(startX, startY, targetX, targetY, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength.. Returns an array
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
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY, int maxLength) {
        return lineArray(startX, startY, targetX, targetY, maxLength);
    }
}
