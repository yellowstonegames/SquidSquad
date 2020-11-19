package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;

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
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return a ObjectList (internally, an ObjectList) of Coord points along the line
     */
    public static List<Coord> line(int startx, int starty, int endx, int endy) {
        // largest positive int for maxLength; a ObjectList cannot actually be given that many elements on the JVM
        return line(startx, starty, endx, endy, 0x7fffffff);
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
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an ObjectList of Coord points along the line
     */
    public static List<Coord> line(int startx, int starty, int endx, int endy, int maxLength) {
        int dx = endx - startx;
        int dy = endy - starty;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        ObjectList<Coord> result = new ObjectList<>(Math.max(ax, ay));

        ax <<= 1;
        ay <<= 1;
        
        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (result.size() < maxLength) {
                result.add(Coord.get(x, y));
                if (x == endx) {
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
                if (y == endy) {
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

    public static boolean isReachable(int startx, int starty, int targetx, int targety, int maxLength, float[][] resistanceMap, ObjectList<Coord> buffer) {
        int dx = targetx - startx;
        int dy = targety - starty;

        int ax = Math.abs(dx);
        int ay = Math.abs(dy);

        int dist = ax + ay;

        if(buffer == null) {
            buffer = new ObjectList<>(dist);
        }
        else {
            buffer.clear();
        }
        if(startx == targetx && starty == targety) {
            buffer.add(Coord.get(startx, starty));
            return true; // already at the point; we can see our own feet just fine!
        }
        float decay = 1f / dist;
        float currentForce = 1f;
        Coord p;


        ax <<= 1;
        ay <<= 1;

        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int x = startx;
        int y = starty;

        int deltax, deltay;
        if (ax >= ay) /* x dominant */ {
            deltay = ay - (ax >> 1);
            while (buffer.size() < maxLength) {
                buffer.add(Coord.get(x, y));
                if (x == targetx) {
                    return true;
                }

                if (x != startx || y != starty) {//don't discount the start location even if on resistant cell
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
                if (y == targety) {
                    return true;
                }

                if (x != startx || y != starty) {//don't discount the start location even if on resistant cell
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
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @return an array of Coord points along the line
     */
    public static Coord[] lineArray(int startx, int starty, int endx, int endy) {
        // largest positive int for maxLength; it is extremely unlikely that this could be reached
        return lineArray(startx, starty, endx, endy, 0x7fffffff);
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
     * @param startx the x coordinate of the starting point
     * @param starty the y coordinate of the starting point
     * @param endx the x coordinate of the starting point
     * @param endy the y coordinate of the starting point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    public static Coord[] lineArray(int startx, int starty, int endx, int endy, int maxLength) {
        int dx = endx - startx;
        int dy = endy - starty;

        int signx = (dx >> 31 | -dx >>> 31); // project nayuki signum
        int signy = (dy >> 31 | -dy >>> 31); // project nayuki signum

        int ax = (dx = Math.abs(dx)) << 1;
        int ay = (dy = Math.abs(dy)) << 1;

        int x = startx;
        int y = starty;

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
