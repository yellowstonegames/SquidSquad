package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectList;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Provides a means to generate Bresenham lines in 2D and 3D.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Lewis Potter
 * @author Tommy Ettinger
 * @author smelC
 */
public class Bresenham {

    /**
     * Prevents any instances from being created
     */
    private Bresenham() {
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link List} interface for the returned reference, consider
     * using {@link #lineArray(Coord, Coord)} to save some memory.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static List<Coord> line(Coord a, Coord b) {
        return line(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    public static Coord[] lineArray(Coord a, Coord b) {
        return lineArray(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points. If you don't need
     * the {@link List} interface for the returned reference, consider
     * using {@link #lineArray(int, int, int, int)} to save some memory.
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
     * @return a List (internally, an ObjectList) of Coord points along the line
     */
    public static List<Coord> line(int startX, int startY, int targetX, int targetY) {
        // largest positive int for maxLength; a ObjectList cannot actually be given that many elements on the JVM
        return line(startX, startY, targetX, targetY, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength. If you don't need
     * the {@link ObjectList} interface for the returned reference, consider
     * using {@link #lineArray(int, int, int, int, int)} to save some memory.
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
    public static List<Coord> line(int startX, int startY, int targetX, int targetY, int maxLength) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        ObjectList<Coord> result = new ObjectList<>(Math.max(ax, ay));

        ax <<= 1;
        ay <<= 1;
        
        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startX;
        int y = startY;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (result.size() < maxLength) {
                result.add(Coord.get(x, y));
                if (x == targetX) {
                    return result;
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
            while (result.size() < maxLength) {
                result.add(Coord.get(x, y));
                if (y == targetY) {
                    return result;
                }

                if (deltax >= 0) {
                    x += signx;
                    deltax -= ay;
                }


                y += signy;
                deltax += ax;
            }
        }
        return result;
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
    public static boolean isReachable(@Nonnull Coord start, @Nonnull Coord target, @Nonnull float[][] resistanceMap,
                                      ObjectList<Coord> buffer){
        return isReachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap, buffer);
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
    public static boolean isReachable(int startX, int startY, int targetX, int targetY,
                                      @Nonnull float[][] resistanceMap, ObjectList<Coord> buffer){
        return isReachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap, buffer);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
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
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                      @Nonnull float[][] resistanceMap, ObjectList<Coord> buffer) {
        int dx = targetX - startX;
        int dy = targetY - startY;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        int dist = ax + ay;

        if(buffer == null) {
            buffer = new ObjectList<>(dist + 1);
        }
        else {
            buffer.clear();
        }
        if(startX == targetX && startY == targetY) {
            buffer.add(Coord.get(startX, startY));
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

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (buffer.size() < maxLength) {
                buffer.add(Coord.get(x, y));
                if (x == targetX) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= 0) {
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
            while (buffer.size() < maxLength) {
                buffer.add(Coord.get(x, y));
                if (y == targetY) {
                    return true;
                }

                if (x != startX || y != startY) {//don't discount the start location even if on resistant cell
                    currentForce -= resistanceMap[x][y];
                }
                currentForce -= decay;
                if (currentForce <= 0) {
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
     * of Coord instead of a List.
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

}
