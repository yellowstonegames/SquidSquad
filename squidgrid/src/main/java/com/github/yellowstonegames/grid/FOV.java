/*
 * Copyright (c) 2022 See AUTHORS file.
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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectList;

import javax.annotation.Nonnull;
import java.util.Iterator;

/**
 * This class provides methods for calculating Field of View in grids. Field of
 * View (FOV) algorithms determine how much area surrounding a point can be
 * seen. They return a 2D array of floats, representing the amount of view
 * (typically sight, but perhaps sound, smell, etc.) which the origin has of
 * each cell. In the returned 2D array, 1.0f is always "fully seen," while 0.0f
 * is always "unseen." Values in between are much more common, and they enable
 * this class to be used for lighting effects.
 * <br>
 * The input resistanceMap is considered the percent of opacity. This resistance
 * is on top of the resistance applied from the light spreading out. You can
 * obtain a resistance map easily with the {@link #generateResistances(char[][])}
 * method, which uses defaults for common chars used in SquidSquad, but you may
 * also want to create a resistance map manually if a given char means something
 * very different in your game. This is easy enough to do by looping over all the
 * x,y positions in your char[][] map and running a switch statement on each char,
 * assigning a float to the same x,y position in a float[][]. The value should
 * be between 0.0f (unblocked) for things light passes through, 1.0f (blocked) for
 * things light can't pass at all, and possibly other values if you have
 * translucent obstacles. There's {@link #generateSimpleResistances(char[][])} as
 * well, which only returns 1.0f (fully blocked) or 0.0f (passable), and 3x3 subcell
 * variants, which produce a resistance map that is 3 times wider and 3 times
 * taller than the input map. The subcell variants have especially useful behavior
 * when using maps with box-drawing characters, since these 3x3 resistance maps
 * will line up blocking cells to where a box-drawing line is.
 * <br>
 * The returned light map is considered the percent of light in the cells.
 * <br>
 * All implementations for FOV here (that is, Ripple FOV and Shadow FOV) provide
 * percentage levels for partially-lit or partially-seen cells. This leads to a
 * straightforward implementation of soft lighting using an FOV result -- just
 * mix the background or floor color of a cell, however you represent it, with
 * a very light color (like pastel yellow), with the percentage of the light
 * color to mix in equal to the percent of light in the FOV map.
 * <br>
 * All solvers perform bounds checking so solid borders in the map are not
 * required.
 * <br>
 * For calculating FOV maps, this class only provides static methods, which take
 * a light 2D array as an argument and edit it in-place. The Ripple methods
 * {@link #reuseRippleFOV(float[][], float[][], int, int, int, float, Radius)}
 * and {@link #reuseRippleFOV(float[][], float[][], int, int, int, float, Radius, float, float)}
 * use internal static state, resetting it on each call; this makes them
 * ineligible for use in multi-threaded code. The other methods use Shadow FOV,
 * and are potentially usable in multi-threaded code.
 * <br>
 * Static methods are provided to add together FOV maps in the simple way
 * (disregarding visibility of distant FOV from a given cell), or the more
 * practical way for roguelikes (where a cell needs to be within line-of-sight
 * in the first place for a distant light to illuminate it). The second method
 * relies on an LOS map, which is essentially the same as a very-high-radius
 * FOV map and can be easily obtained with calculateLOSMap(). The simple way uses
 * {@link #addFOVs(float[][]...)} or {@link #addFOVsInto(float[][], float[][]...)},
 * while the way that respects LOS uses {@link #mixVisibleFOVs(float[][], float[][]...)}
 * or {@link #mixVisibleFOVsInto(float[][], float[][], float[][]...)}.
 * <br>
 * If you want to iterate through cells that are visible in a float[][] returned
 * by FOV, you can pass that float[][] to the constructor for Region, and
 * you can use the Region as a reliably-ordered Collection of Coord (among
 * other things). The order Region iterates in is somewhat strange, and
 * doesn't, for example, start at the center of an FOV map, but it will be the
 * same every time you create a Region with the same FOV map (or the same
 * visible Coords).
 * <br>
 * This class is not thread-safe. This is generally true for most of SquidSquad.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger
 */
public class FOV {
    private static final Direction[] ccw = new Direction[]
            {Direction.UP_RIGHT, Direction.UP_LEFT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT, Direction.UP_RIGHT};
    private static final Direction[] ccw_full = new Direction[]{Direction.RIGHT, Direction.UP_RIGHT, Direction.UP,
            Direction.UP_LEFT, Direction.LEFT, Direction.DOWN_LEFT, Direction.DOWN, Direction.DOWN_RIGHT};
    private static final ObjectDeque<Coord> dq = new ObjectDeque<>(256);
    private static final Region lightWorkspace = new Region(256, 256);
    private static final ObjectList<Coord> neighbors = new ObjectList<>(8);
    private static final float[] directionRanges = new float[8];


    /**
     * Static usage only.
     */
    private FOV() {
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses Shadow FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter.  The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light a non-null 2D float array that will have its contents overwritten, modified, and returned
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid (the same as {@code light})
     */
    public static float[][] reuseFOV(float[][] resistanceMap, float[][] light, int startx, int starty) {
        return reuseFOV(resistanceMap, light, startx, starty, Integer.MAX_VALUE, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses Shadow FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public static float[][] reuseFOV(float[][] resistanceMap, float[][] light,
                                      int startx, int starty, float radius) {
        return reuseFOV(resistanceMap, light, startx, starty, radius, Radius.CIRCLE);
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses Shadow FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static float[][] reuseFOV(float[][] resistanceMap, float[][] light,
                                      int startX, int startY, float radius,
                                      Radius radiusTechnique)
    {
        float decay = 1f / radius;
        ArrayTools.fill(light, 0);
        light[startX][startY] = Math.min(1f, radius);//make the starting space full power unless radius is tiny


        shadowCast(0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(-1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(-1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        return light;
    }
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses Shadow FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static float[][] reuseFOVSymmetrical(float[][] resistanceMap, float[][] light, int startX, int startY, float radius, Radius radiusTechnique)
    {
        float decay = 1.0f / radius;
        ArrayTools.fill(light, 0);
        light[startX][startY] = Math.min(1.0f, radius);//make the starting space full power unless radius is tiny


        shadowCast(0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int row = 0; row <= radius + 1.0f; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0f; col++) {
                if(startX - col >= 0 && startY - row >= 0 && resistanceMap[startX - col][startY - row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 0, -1, -1, 0, radius, startX - col, startY - row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY - row] = 0.0f;
            }
        }
        shadowCast(1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int col = 0; col <= radius + 1.0f; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0f; row++) {
                if(startX - col >= 0 && startY - row >= 0 && resistanceMap[startX - col][startY - row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, -1, 0, 0, -1, radius, startX - col, startY - row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY - row] = 0.0f;
            }
        }

        shadowCast(0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int row = 0; row <= radius + 1.0f; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0f; col++) {
                if(startX - col >= 0 && startY + row < light[0].length &&  resistanceMap[startX - col][startY + row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 0, -1, 1, 0, radius, startX - col, startY + row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY + row] = 0.0f;
            }
        }
        shadowCast(1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int col = 0; col <= radius + 1.0f; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0f; row++) {
                if(startX - col >= 0 && startY + row < light[0].length && resistanceMap[startX - col][startY + row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, -1, 0, 0, 1, radius, startX - col, startY + row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY + row] = 0.0f;
            }
        }

        shadowCast(0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int row = 0; row <= radius + 1.0f; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0f; col++) {
                if(startX + col < light.length && startY + row < light[0].length && resistanceMap[startX + col][startY + row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 0, 1, 1, 0, radius, startX + col, startY + row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY + row] = 0.0f;
            }
        }
        shadowCast(-1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int col = 0; col <= radius + 1.0f; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0f; row++) {
                if(startX + col < light.length && startY + row < light[0].length && resistanceMap[startX + col][startY + row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 1, 0, 0, 1, radius, startX + col, startY + row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY + row] = 0.0f;
            }
        }

        shadowCast(0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int row = 0; row <= radius + 1.0f && startY + row < light[0].length; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0f; col++) {
                if(startX + col < light.length && startY - row >= 0 && resistanceMap[startX + col][startY - row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 0, 1, -1, 0, radius, startX + col, startY - row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY - row] = 0.0f;
            }
        }
        shadowCast(-1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        for (int col = 0; col <= radius + 1.0f; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0f; row++) {
                if(startX + col < light.length && startY - row >= 0 && resistanceMap[startX + col][startY - row] < 1.0f &&
                        !shadowCastCheck(1, 1.0f, 0.0f, 1, 0, 0, -1, radius, startX + col, startY - row, decay, light, resistanceMap, radiusTechnique, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY - row] = 0.0f;
            }
        }
        return light;
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light map where the values
     * are always either 0.0f for "not in line of sight" or 1.0f for "in line of
     * sight," which doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses Shadow FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results. The given {@code resistanceMap} only considers cells to
     * block light if they have values of 1.0f or greater.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are pretty much irrelevant because
     * the distance doesn't matter, only the presence of a clear line, but this uses
     * {@link Radius#SQUARE} if it matters.
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "no line"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static float[][] reuseLOS(float[][] resistanceMap, float[][] light, int startX, int startY)
    {
        return reuseLOS(resistanceMap, light, startX, startY, 0, 0, light.length, light[0].length);
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light map where the values
     * are always either 0.0f for "not in line of sight" or 1.0f for "in line of
     * sight," which doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses Shadow FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results. The given {@code resistanceMap} only considers cells to
     * block light if they have values of 1.0f or greater.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are pretty much irrelevant because
     * the distance doesn't matter, only the presence of a clear line, but this uses
     * {@link Radius#SQUARE} if it matters.
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "no line"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param minX inclusive lowest x position to assign to or process in {@code light}
     * @param minY inclusive lowest y position to assign to or process in {@code light}
     * @param maxX exclusive highest x position to assign to or process in {@code light}
     * @param maxY exclusive highest y position to assign to or process in {@code light}
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static float[][] reuseLOS(float[][] resistanceMap, float[][] light, int startX, int startY,
                                      int minX, int minY, int maxX, int maxY)
    {
        float radius = light.length + light[0].length;
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        
        shadowCastBinary(1, 1.0f, 0.0f, 0, 1, 1, 0, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 1, 0, 0, 1, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, 1, -1, 0, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 1, 0, 0, -1, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, -1, -1, 0, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, -1, 0, 0, -1, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, -1, 1, 0, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, -1, 0, 0, 1, radius, startX, startY, light, resistanceMap, minX, minY, maxX, maxY);
        
        return light;
    }

    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light Region where the false or "off" indicates
     * "not in line of sight" and true or "on" indicated "in line of sight,"
     * though this doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses Shadow FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results. The given {@code blockingMap} only considers cells to
     * block light if they are true or "on".
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell.
     *
     * @param blockingMap the grid of which cells block light ("on" cells are blocking)
     * @param light the grid of cells to assign to; will be cleared before modifying
     * @param startX        the horizontal component of the starting location
     * @param startY        the vertical component of the starting location
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static Region reuseLOS(@Nonnull Region blockingMap, @Nonnull Region light, int startX, int startY)
    {
        return reuseLOS(blockingMap, light, startX, startY, 0, 0, light.width, light.height);
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light Region where the false or "off" indicates
     * "not in line of sight" and true or "on" indicated "in line of sight,"
     * though this doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses Shadow FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results. The given {@code blockingMap} only considers cells to
     * block light if they are true or "on".
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell.
     *
     * @param blockingMap the grid of which cells block light ("on" cells are blocking)
     * @param light the grid of cells to assign to; will be cleared before modifying
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param minX inclusive lowest x position to assign to or process in {@code light}
     * @param minY inclusive lowest y position to assign to or process in {@code light}
     * @param maxX exclusive highest x position to assign to or process in {@code light}
     * @param maxY exclusive highest y position to assign to or process in {@code light}
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static Region reuseLOS(@Nonnull Region blockingMap, @Nonnull Region light, int startX, int startY,
                                  int minX, int minY, int maxX, int maxY)
    {
        float radius = (maxX - minX) + (maxY - minY);
        light.clear();
        light.insert(startX, startY);//make the starting space full power

        shadowCastBinary(1, 1.0f, 0.0f, 0, 1, 1, 0, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 1, 0, 0, 1, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, 1, -1, 0, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 1, 0, 0, -1, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, -1, -1, 0, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, -1, 0, 0, -1, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, 0, -1, 1, 0, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0f, 0.0f, -1, 0, 0, 1, radius, startX, startY, light, blockingMap, minX, minY, maxX, maxY);

        return light;
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting at the given angle in  degrees and covering a span
     * centered on that angle, also in degrees. Assigns to, and returns, a light
     * map where the values represent a percentage of fully lit. Always uses
     * Shadow FOV, which allows this method to be static since it doesn't
     * need to keep any state around, and can reuse the state the user gives it
     * via the {@code light} parameter. The values in light are cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.  A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to shape the FOV by changing distance calculation (circle, square, etc.)
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    public static float[][] reuseFOV(float[][] resistanceMap, float[][] light, int startX, int startY,
                                          float radius, Radius radiusTechnique, float angle, float span) {
        float decay = 1.0f / radius;
        ArrayTools.fill(light, 0);
        light[startX][startY] = Math.min(1.0f, radius);//make the starting space full power unless radius is tiny
        angle *= 0.002777777777777778f;
        angle -= MathTools.fastFloor(angle);
        span *= 0.002777777777777778f;


        light = shadowCastLimited(1, 1.0f, 0.0f, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0f, 0.0f, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0f, 0.0f, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0f, 0.0f, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0f, 0.0f, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0f, 0.0f, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0f, 0.0f, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0f, 0.0f, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        return light;
    }

    /**
     * Like the {@link #reuseFOV(float[][], float[][], int, int, float, Radius)} method, but this uses Ripple FOV
     * with a tightness/looseness of 2. Other parameters are similar; you
     * can get a resistance map from {@link #generateResistances(char[][])}, {@code light} will be modified and returned
     * (it will be overwritten, but its size should be the same as the resistance map), there's a starting x,y position,
     * a radius in cells, and a {@link Radius} enum constant to choose the distance measurement.
     * <br>
     * This method should not be used from multiple threads; it uses internal static state. You can use the Shadow FOV
     * methods instead if you need multi-threading.
     * <br>
     * Ripple is a significantly slower algorithm than Shadow, typically by more than an order of magnitude.
     * Still, unless you are making many FOV calls per frame rendered, it's unlikely to be a severe bottleneck,
     * although this is possible. {@link Radiance}, which typically makes an FOV call once per frame per Radiance,
     * should always use Shadow, but if you only calculate FOV for the player, or only when they move, then either
     * Shadow or Ripple can be suitable.
     * @param resistanceMap probably calculated with {@link #generateResistances(char[][])}; 1.0f blocks light, 0.0f allows it
     * @param light will be overwritten! Should be initialized with the same size as {@code resistanceMap}
     * @param x starting x position to look from
     * @param y starting y position to look from
     * @param radius the distance to extend from the starting x,y position
     * @param radiusTechnique how to measure distance; typically {@link Radius#CIRCLE}.
     * @return {@code light}, after writing the FOV map into it; 1.0f is fully lit and 0.0f is unseen
     */
    public static float[][] reuseRippleFOV(float[][] resistanceMap, float[][] light, int x, int y, float radius, Radius radiusTechnique) {
        return reuseRippleFOV(resistanceMap, light, 2, x, y, radius, radiusTechnique);
    }
    /**
     * Like the {@link #reuseFOV(float[][], float[][], int, int, float, Radius)} method, but this uses Ripple FOV
     * with a configurable tightness/looseness (between 1, tightest, and 6, loosest). Other parameters are similar; you
     * can get a resistance map from {@link #generateResistances(char[][])}, {@code light} will be modified and returned
     * (it will be overwritten, but its size should be the same as the resistance map), there's a starting x,y position,
     * a radius in cells, and a {@link Radius} enum constant to choose the distance measurement.
     * <br>
     * This method should not be used from multiple threads; it uses internal static state. You can use the Shadow FOV
     * methods instead if you need multi-threading.
     * <br>
     * Ripple is a significantly slower algorithm than Shadow, typically by more than an order of magnitude.
     * Still, unless you are making many FOV calls per frame rendered, it's unlikely to be a severe bottleneck,
     * although this is possible. {@link Radiance}, which typically makes an FOV call once per frame per Radiance,
     * should always use Shadow, but if you only calculate FOV for the player, or only when they move, then either
     * Shadow or Ripple can be suitable.
     * @param resistanceMap probably calculated with {@link #generateResistances(char[][])}; 1.0f blocks light, 0.0f allows it
     * @param light will be overwritten! Should be initialized with the same size as {@code resistanceMap}
     * @param rippleLooseness affects spread; between 1 and 6, inclusive; 1 is tightest, 2 is normal, and 6 is loosest
     * @param x starting x position to look from
     * @param y starting y position to look from
     * @param radius the distance to extend from the starting x,y position
     * @param radiusTechnique how to measure distance; typically {@link Radius#CIRCLE}.
     * @return {@code light}, after writing the FOV map into it; 1.0f is fully lit and 0.0f is unseen
     */
    public static float[][] reuseRippleFOV(float[][] resistanceMap, float[][] light, int rippleLooseness, int x, int y, float radius, Radius radiusTechnique) {
        ArrayTools.fill(light, 0);
        light[x][y] = Math.min(1.0f, radius);//make the starting space full power unless radius is tiny
        lightWorkspace.resizeAndEmpty(light.length, light[0].length);
        doRippleFOV(light, Math.min(Math.max(rippleLooseness, 1), 6), x, y, 1.0f / radius, radius, resistanceMap, radiusTechnique);
        return light;
    }

    /**
     * Like the {@link #reuseFOV(float[][], float[][], int, int, float, Radius)} method, but this is meant for sound
     * rather than light, and so uses Ripple FOV with maximum looseness, and expects a sound resistance map rather than
     * a light one. Other parameters are similar; you can get a sound resistance map from
     * {@link #generateSoundResistances(char[][])}, {@code sound} will be modified and returned (it will be overwritten,
     * but its size should be the same as the resistance map), there's a starting x,y position for the sound, a radius
     * in cells, and a {@link Radius} enum constant to choose the distance measurement.
     * <br>
     * If you have loud background noise in a map, you can simulate all other sounds being harder to hear by subtracting
     * some value from all results in {@code sound}. With the default settings in
     * {@link #generateSoundResistances(char[][])}, thin walls (one cell thick) and doors will allow some sound through,
     * while thick walls (two or more cells) will allow none.
     * <br>
     * This method should not be used from multiple threads; it uses internal static state.
     * @param resistanceMap probably calculated with {@link #generateSoundResistances(char[][])}; 1.0f blocks sound, 0.0f allows it
     * @param sound will be overwritten! Should be initialized with the same size as {@code resistanceMap}
     * @param x starting x position to emit sound from
     * @param y starting y position to emit sound from
     * @param radius the maximum distance to extend from the starting x,y position
     * @param radiusTechnique how to measure distance; typically {@link Radius#CIRCLE}.
     * @return {@code sound}, after writing the sound field into it; 1.0f is max volume and 0.0f is inaudible
     */
    public static float[][] reuseSoundField(float[][] resistanceMap, float[][] sound, int x, int y, float radius, Radius radiusTechnique) {
        return reuseRippleFOV(resistanceMap, sound, 6, x, y, radius, radiusTechnique);
    }

    /**
     * Like the {@link #reuseFOV(float[][], float[][], int, int, float, Radius, float, float)} method, but this
     * uses Ripple FOV with a configurable tightness/looseness (between 1, tightest, and 6, loosest). Other parameters
     * are similar; you can get a resistance map from {@link #generateResistances(char[][])}, {@code light} will be
     * modified and returned (it will be overwritten, but its size should be the same as the resistance map), there's 
     * starting x,y position, a radius in cells, a {@link Radius} enum constant to choose the distance measurement, and
     * the angle/span combination to specify a conical section of FOV (span is the total in degrees, centered on angle).
     * <br>
     * This method should not be used from multiple threads; it uses internal static state. You can use the Shadow FOV
     * methods instead if you need multi-threading.
     * <br>
     * Ripple is a significantly slower algorithm than Shadow, typically by more than an order of magnitude.
     * Still, unless you are making many FOV calls per frame rendered, it's unlikely to be a severe bottleneck,
     * although this is possible. {@link Radiance}, which typically makes an FOV call once per frame per Radiance,
     * should always use Shadow, but if you only calculate FOV for the player, or only when they move, then either
     * Shadow or Ripple can be suitable.
     * @param resistanceMap probably calculated with {@link #generateResistances(char[][])}; 1.0f blocks light, 0.0f allows it
     * @param light will be overwritten! Should be initialized with the same size as {@code resistanceMap}
     * @param rippleLooseness affects spread; between 1 and 6, inclusive; 1 is tightest, 2 is normal, and 6 is loosest
     * @param x starting x position to look from
     * @param y starting y position to look from
     * @param radius the distance to extend from the starting x,y position
     * @param radiusTechnique how to measure distance; typically {@link Radius#CIRCLE}.
     * @param angle the angle to center the conical FOV on
     * @param span the total span in degrees for the conical FOV to cover
     * @return {@code light}, after writing the FOV map into it; 1.0f is fully lit and 0.0f is unseen
     */
    public static float[][] reuseRippleFOV(float[][] resistanceMap, float[][] light, int rippleLooseness, int x, int y, float radius, Radius radiusTechnique, float angle, float span) {
        ArrayTools.fill(light, 0);
        light[x][y] = Math.min(1.0f, radius);//make the starting space full power unless radius is tiny
        lightWorkspace.resizeAndEmpty(light.length, light[0].length);
        angle *= 0.002777777777777778f;
        angle -= MathTools.fastFloor(angle);
        span *= 0.002777777777777778f;
        doRippleFOV(light, Math.min(Math.max(rippleLooseness, 1), 6), x, y, 1.0f / radius, radius, resistanceMap, radiusTechnique, angle, span);
        return light;
    }

        /**
         * Reuses the existing light 2D array and fills it with a straight-line bouncing path of light that reflects its way
         * through the given resistanceMap from startX, startY until it uses up the given distance. The angle the path
         * takes is given in degrees, and the angle used can change as obstacles are hit (reflecting backwards if it hits a
         * corner pointing directly into or away from its path). This can be used something like an LOS method, but because
         * the path can be traveled back over, an array or Queue becomes somewhat more complex, and the decreasing numbers
         * for a straight line that stack may make more sense for how this could be used (especially with visual effects).
         * This currently allows the path to pass through single-cell wall-like obstacles without changing direction, e.g.
         * it passes through pillars, but will bounce if it hits a bigger wall.
         * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
         * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "unlit"
         * @param startX the horizontal component of the starting location
         * @param startY the vertical component of the starting location
         * @param distance the distance the light will extend to
         * @param angle in degrees, the angle to start the path traveling in
         * @return the given light parameter, after modifications
         */
    public static float[][] bouncingLine(float[][] resistanceMap, float[][] light, int startX, int startY, float distance, float angle)
    {
        float rad = Math.max(1, distance);
        float decay = 1.0f / rad;
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        float s = TrigTools.sinDeg(angle),
                c = TrigTools.cosDeg(angle);
        float deteriorate = 1.0f;
        int dx, dy, width = resistanceMap.length, height = resistanceMap[0].length;
        for (int d = 1; d <= rad; ) {
            dx = startX + Math.round(c * d);
            if(dx < 0 || dx > width)
                break;
            dy = startY + Math.round(s * d);
            if(dy < 0 || dy > height)
                break;
            deteriorate -= decay;
            //check if it's within the lightable area and light if needed
            if (deteriorate > 0.0f) {
                light[dx][dy] = Math.min(light[dx][dy] + deteriorate, 1.0f);
                if (resistanceMap[dx][dy] >= 1 && deteriorate > decay)
                {
                    startX = dx;
                    startY = dy;
                    d = 1;
                    float flipX = resistanceMap[startX + Math.round(-c * d)][dy],
                            flipY = resistanceMap[dx][startY + Math.round(-s * d)];
                    if(flipX >= 1.0f)
                        s = -s;
                    if(flipY >= 1.0f)
                        c = -c;
                }
                else ++d;

            }
            else break;
        }

        return light;
    }

    private static void doRippleFOV(float[][] lightMap, int ripple, int x, int y, float decay, float radius,
                                    float[][] map, Radius radiusStrategy) {
        dq.clear();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.addLast(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || FOV.lightWorkspace.contains(p)) {
                continue;//no light to spread
            }

            for (Direction dir : Direction.OUTWARDS) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(x, y, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }

                float surroundingLight = nearRippleLight(x2, y2, ripple, x, y, decay, lightMap, map, radiusStrategy);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.addLast(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }



    private static void doRippleFOV(float[][] lightMap, int ripple, int x, int y, float decay, float radius,
                                    float[][] map, Radius radiusStrategy, float angle, float span) {
	    dq.clear();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.addLast(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || FOV.lightWorkspace.contains(p)) {
                continue;//no light to spread
            }

            for (Direction dir : ccw_full) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(x, y, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }
                float newAngle = TrigTools.atan2Turns(y2 - y, x2 - x) - angle;
                newAngle -= MathTools.fastFloor(newAngle);
                if (newAngle > span * 0.5f && newAngle < 1.0f - span * 0.5f) 
                    continue;

                float surroundingLight = nearRippleLight(x2, y2, ripple, x, y, decay, lightMap, map, radiusStrategy );
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.addLast(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private static float nearRippleLight(int x, int y, int rippleNeighbors, int startx, int starty, float decay, float[][] lightMap, float[][] map, Radius radiusStrategy) {
        if (x == startx && y == starty) {
            return 1;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;
        neighbors.clear();
        float tmpDistance = 0, testDistance;
        Coord c;
        for (Direction di : Direction.OUTWARDS) {
            int x2 = x + di.deltaX;
            int y2 = y + di.deltaY;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                tmpDistance = radiusStrategy.radius(startx, starty, x2, y2);
                int idx = 0;
                for(int i = 0; i < neighbors.size() && i <= rippleNeighbors; i++)
                {
                    c = neighbors.get(i);
                    testDistance = radiusStrategy.radius(startx, starty, c.x, c.y);
                    if(tmpDistance < testDistance) {
                        break;
                    }
                    idx++;
                }
                neighbors.add(idx, Coord.get(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }
        int max = Math.min(rippleNeighbors, neighbors.size());
        float light = 0;
        int lit = 0, indirects = 0;
        for (int i = 0; i < max; i++) {
            Coord p = neighbors.get(i);
            if (lightMap[p.x][p.y] > 0) {
                lit++;
                if (FOV.lightWorkspace.contains(p)) {
                    indirects++;
                }
                float dist = radiusStrategy.radius(x, y, p.x, p.y);
                light = Math.max(light, lightMap[p.x][p.y] - dist * decay - map[p.x][p.y]);
            }
        }

        if (map[x][y] >= 1 || indirects >= lit) {
            FOV.lightWorkspace.insert(x, y);
        }
        return light;
    }

    private static void shadowCast(int xx, int xy, int yx, int yy,
                                   float radius, int startx, int starty, float decay, float[][] lightMap,
                                   float[][] map, Radius radiusStrategy) {
	    shadowCast(1, 1.0f, 0.0f, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy,
                0, 0, lightMap.length, lightMap[0].length);
    }

    private static void shadowCastBinary(int row, float start, float end, int xx, int xy, int yx, int yy,
                                         float radius, int startx, int starty, float[][] lightMap,
                                         float[][] blockMap,
                                         int minX, int minY, int maxX, int maxY) {
        float newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                lightMap[currentX][currentY] = 1.0f;

                if (blocked) { //previous cell was a blocking one
                    if (blockMap[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (blockMap[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCastBinary(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty,
                                lightMap, blockMap, minX, minY, maxX, maxY);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }


    private static void shadowCastBinary(int row, float start, float end, int xx, int xy, int yx, int yy,
                                         float radius, int startx, int starty, Region lightMap,
                                         Region map,
                                         int minX, int minY, int maxX, int maxY) {
        if (start < end)
            return;
        float newStart = 0;
        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                lightMap.insert(currentX, currentY);

                if (blocked) { //previous cell was a blocking one
                    if (map.contains(currentX, currentY)) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map.contains(currentX, currentY) && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCastBinary(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty,
                                lightMap, map, minX, minY, maxX, maxY);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }

    private static boolean shadowCastCheck(int row, float start, float end, int xx, int xy, int yx, int yy,
                                         float radius, int startx, int starty, float decay, float[][] lightMap,
                                         float[][] map, Radius radiusStrategy,
                                         int minX, int minY, int maxX, int maxY, int targetX, int targetY) {
        float newStart = 0;
        if (start < end) {
            return false;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                if(currentX == targetX && currentY == targetY) return true;

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1.0f) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1.0f && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        if(shadowCastCheck(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay,
                                lightMap, map, radiusStrategy, minX, minY, maxX, maxY, targetX, targetY))
                            return true;
                        newStart = rightSlope;
                    }
                }
            }
        }
        return false;
    }

    private static void shadowCast(int row, float start, float end, int xx, int xy, int yx, int yy,
                                   float radius, int startx, int starty, float decay, float[][] lightMap,
                                   float[][] map, Radius radiusStrategy,
                                   int minX, int minY, int maxX, int maxY) {
        float newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                float deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    lightMap[currentX][currentY] = 1.0f - decay * deltaRadius; 
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay,
                                lightMap, map, radiusStrategy, minX, minY, maxX, maxY);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }
    private static float[][] shadowCastLimited(int row, float start, float end, int xx, int xy, int yx, int yy,
                                                float radius, int startx, int starty, float decay, float[][] lightMap,
                                                float[][] map, Radius radiusStrategy, float angle, float span) {
        float newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                float deltaRadius = radiusStrategy.radius(deltaX, deltaY),
                        at2 = Math.abs(angle - TrigTools.atan2Turns(currentY - starty, currentX - startx));// + 1.0f) % 1.0f;
                //check if it's within the light-able area and light if needed
                if (deltaRadius <= radius
                        && (at2 <= span * 0.5f
                        || at2 >= 1.0f - span * 0.5f)) {
                    float bright = 1 - decay * deltaRadius;
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy, angle, span);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting with the view "pointed at" the given {@code angle} in degrees,
     * extending to different ranges based on the direction the light is traveling.
     * The direction ranges are {@code forward}, {@code sideForward}, {@code side},
     * {@code sideBack}, and {@code back}; all are multiplied by {@code radius}.
     * Assigns to, and returns, a light map where the values represent a percentage of fully
     * lit. The values in light are cleared before this is run, because prior state can make
     * this give incorrect results. You can use {@link #addFOVsInto(float[][], float[][]...)}
     * if you want to mix FOV results, which works as an alternative to using the prior light state.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. If all direction ranges are the same, this acts like
     * {@link #reuseFOV(float[][], float[][], int, int, float, Radius)}; otherwise
     * may produce conical shapes (potentially more than one, or overlapping ones).
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by {@link #generateResistances(char[][])}
     * @param light the grid of cells to assign to; may have existing values, and 0.0f is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to (roughly); direction ranges will be multiplied by this
     * @param radiusTechnique provides a means to shape the FOV by changing distance calculation (circle, square, etc.)
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param forward the range to extend when the light is within 22.5f degrees of angle; will be interpolated with sideForward
     * @param sideForward the range to extend when the light is between 22.5f and 67.5f degrees of angle; will be interpolated with forward or side
     * @param side the range to extend when the light is between 67.5f and 112.5f degrees of angle; will be interpolated with sideForward or sideBack
     * @param sideBack the range to extend when the light is between 112.5f and 157.5f degrees of angle; will be interpolated with side or back
     * @param back the range to extend when the light is more than 157.5f degrees away from angle; will be interpolated with sideBack
     * @return the computed light grid (the same as {@code light})
     */
    public static float[][] reuseFOV(float[][] resistanceMap, float[][] light, int startX, int startY,
                                      float radius, Radius radiusTechnique, float angle,
                                      float forward, float sideForward, float side, float sideBack, float back) {
        directionRanges[0] = forward * radius;
        directionRanges[7] = directionRanges[1] = sideForward * radius;
        directionRanges[6] = directionRanges[2] = side * radius;
        directionRanges[5] = directionRanges[3] = sideBack * radius;
        directionRanges[4] = back * radius;

        radius = Math.max(1, radius);
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        angle *= 0.002777777777777778f;
        angle -= MathTools.fastFloor(angle);

        light = shadowCastPersonalized(1, 1.0f, 0.0f, 0, 1, 1, 0,   radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, 1, 0, 0, 1,   radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, 0, -1, 1, 0,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, -1, 0, 0, 1,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, 0, -1, -1, 0, radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, -1, 0, 0, -1, radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, 0, 1, -1, 0,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0f, 0.0f, 1, 0, 0, -1,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        return light;
    }

    private static float[][] shadowCastPersonalized(int row, float start, float end, int xx, int xy, int yx, int yy,
                                                     float radius, int startx, int starty, float[][] lightMap,
                                                     float[][] map, Radius radiusStrategy, float angle, final float[] directionRanges) {
        float newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                float leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                float rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                float at2 = Math.abs(angle - TrigTools.atan2Turns(currentY - starty, currentX - startx)) * 8.0f,
                        deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                int ia = (int)(at2), low = ia & 7, high = ia + 1 & 7;
                float a = at2 - ia, adjRadius = (1.0f - a) * directionRanges[low] + a * directionRanges[high];
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= adjRadius) {
                    lightMap[currentX][currentY] = 1.0f - (deltaRadius / (adjRadius + 1.0f)); // how bright the tile is
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < adjRadius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastPersonalized(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, lightMap, map, radiusStrategy, angle, directionRanges);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    /**
     * Adds an FOV map to another in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0f. Modifies the basis parameter in-place and makes no
     * allocations; this is different from {@link #addFOVs(float[][][])}, which creates a new 2D array.
     * @param basis a 2D float array, which can be empty or returned by calculateFOV() or reuseFOV(); modified!
     * @param addend another 2D float array that will be added into basis; this one will not be modified
     * @return the sum of the 2D float arrays passed, using the dimensions of basis if they don't match
     */
    public static float[][] addFOVsInto(float[][] basis, float[][] addend)
    {
        for (int x = 0; x < basis.length && x < addend.length; x++) {
                for (int y = 0; y < basis[x].length && y < addend[x].length; y++) {
                    basis[x][y] = Math.min(1.0f, basis[x][y] + addend[x][y]);
                }
            }
        return basis;
    }
    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0f. Allocates a new 2D float array and returns it.
     * @param maps an array or vararg of 2D float arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D float arrays passed, using the dimensions of the first if they don't all match
     */
    public static float[][] addFOVs(float[][]... maps)
    {
        if(maps == null || maps.length == 0)
            return new float[0][0];
        float[][] map = ArrayTools.copy(maps[0]);
        for(int i = 1; i < maps.length; i++)
        {
            for (int x = 0; x < map.length && x < maps[i].length; x++) {
                for (int y = 0; y < map[x].length && y < maps[i][x].length; y++) {
                    map[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0f) map[x][y] = 1.0f;
            }
        }
        return map;
    }
    /**
     * Adds multiple FOV maps to basis cell-by-cell, modifying basis; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0f. Returns basis without allocating new objects.
     * @param basis a 2D float array that will be modified by adding values in maps to it and clamping to 1.0f or less
     * @param maps an array or vararg of 2D float arrays, each usually returned by calculateFOV()
     * @return basis, with all elements in all of maps added to the corresponding cells and clamped
     */
    public static float[][] addFOVsInto(float[][] basis, float[][]... maps) {
        if (maps == null || maps.length == 0)
            return basis;
        for (int i = 1; i < maps.length; i++) {
            for (int x = 0; x < basis.length && x < maps[i].length; x++) {
                for (int y = 0; y < basis[x].length && y < maps[i][x].length; y++) {
                    basis[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < basis.length; x++) {
            for (int y = 0; y < basis[x].length; y++) {
                if (basis[x][y] > 1.0f) basis[x][y] = 1.0f;
            }
        }
        return basis;
    }

    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0f. Allocates a new 2D float array and returns it.
     * @param maps an Iterable of 2D float arrays (most collections implement Iterable),
     *             each usually returned by calculateFOV()
     * @return the sum of all the 2D float arrays passed, using the dimensions of the first if they don't all match
     */
    public static float[][] addFOVs(Iterable<float[][]> maps)
    {
        if(maps == null)
            return new float[0][0];
        Iterator<float[][]> it = maps.iterator();
        if(!it.hasNext())
            return new float[0][0];
        float[][] map = ArrayTools.copy(it.next()), t;
        while (it.hasNext())
        {
            t = it.next();
            for (int x = 0; x < map.length && x < t.length; x++) {
                for (int y = 0; y < map[x].length && y < t[x].length; y++) {
                    map[x][y] += t[x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0f) map[x][y] = 1.0f;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(float[][], float[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0f.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(float[][], float[][], int, int)}
     * @param maps an array or vararg of 2D float arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D float arrays in maps where a cell was visible in losMap
     */
    public static float[][] mixVisibleFOVs(float[][] losMap, float[][]... maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        float[][] map = new float[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001f) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0f) map[x][y] = 1.0f;
            }
        }

        return map;
    }
    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(float[][], float[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0f.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(float[][], float[][], int, int)}
     * @param basis an existing 2D float array that should have matching width and height to losMap; will be modified
     * @param maps an array or vararg of 2D float arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D float arrays in maps where a cell was visible in losMap
     */
    public static float[][] mixVisibleFOVsInto(float[][] losMap, float[][] basis, float[][]... maps)

    {
        if(losMap == null || losMap.length <= 0 || losMap[0].length <= 0)
            return addFOVsInto(basis, maps);
        final int width = losMap.length, height = losMap[0].length;
        float[][] map = new float[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001f) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0f) map[x][y] = 1.0f;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(float[][], float[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0f.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(float[][], float[][], int, int)}
     * @param maps an Iterable of 2D float arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D float arrays in maps where a cell was visible in losMap
     */
    public static float[][] mixVisibleFOVs(float[][] losMap, Iterable<float[][]> maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        float[][] map = new float[width][height];
        if(maps == null)
            return map;
        for (float[][] map1 : maps) {
            for (int x = 0; x < width && x < map1.length; x++) {
                for (int y = 0; y < height && y < map1[x].length; y++) {
                    if (losMap[x][y] > 0.0001f) {
                        map[x][y] += map1[x][y];
                        if (map[x][y] > 1.0f) map[x][y] = 1.0f;
                    }
                }
            }
        }
        return map;
    }

    /**
     * Given a char[][] for the map, produces a float[][] that can be used with most of the methods in FOV, like
     * {@link #reuseFOV(float[][], float[][], int, int, float)}. It expects any doors to be represented by '+' if
     * closed or '/' if open, any walls to be '#' or box drawing characters, and it doesn't care what other chars are
     * used (only doors, including open ones, and walls obscure light and thus have a resistance by default). Open doors
     * slightly obscure light, closed doors obscure almost all light coming from the other side, walls block all light,
     * and anything else does not obscure light.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class, with clear cells assigned 0.0f and blocked ones 1.0f
     */
    public static float[][] generateResistances(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float[][] portion = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ' ':
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 1.0f;
                        break;
                    case '/':
                        portion[i][j] = 0.15f;
                        break;
                    case '+':
                        portion[i][j] = 0.95f;
                        break;
                    default:
                        portion[i][j] = 0.0f;
                }
            }
        }
        return portion;
    }

    /**
     * Given a char[][] for the map that should use box drawing characters, produces a float[][] with triple width and
     * triple height that can be used with FOV methods like {@link #reuseFOV(float[][], float[][], int, int, float)}
     * in classes that use subcell lighting. Importantly, this only considers a "thin line" of wall to be blocking
     * (matching the box drawing character), instead of the whole 3x3 area. This expects any doors to be represented by
     * '+' if closed or '/' if open, any normal walls to be box drawing characters, any cells that block all subcells to
     * be '#', and it doesn't care what other chars are used (only doors, including open ones, and walls obscure light
     * and thus have a resistance normally).
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class and subcell lighting, with triple width/height
     */
    public static float[][] generateResistances3x3(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float[][] portion = new float[width * 3][height * 3];
        for (int i = 0, x = 0; i < width; i++, x+=3) {
            for (int j = 0, y = 0; j < height; j++, y+=3) {
                switch (map[i][j]) {
                    case '\1':
                    case '#':
                        portion[x][y] = portion[x+1][y] = portion[x+2][y] =
                                portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                        portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] = 1.0f;
                        break;
                    case '├':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┤':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                    /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┴':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                    /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┬':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┌':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┐':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '└':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┘':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                    /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '│':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '─':
                        portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] = 1.0f;
                        break;
                    case '╴':
                        portion[x][y+1] = portion[x+1][y+1] = 1.0f;
                        break;
                    case '╵':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/ 1.0f;
                        break;
                    case '╶':
                        portion[x+1][y+1] = portion[x+2][y+1] = 1.0f;
                        break;
                    case '╷':
                        /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┼':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                    /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '/':
                        portion[x][y] = portion[x+1][y] = portion[x+2][y] =
                                portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                        portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] = 0.15f;
                        break;
                    case '+':
                        portion[x][y] = portion[x+1][y] = portion[x+2][y] =
                                portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                        portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] = 0.95f;
                        break;
                }
            }
        }
        return portion;
    }

    /**
     * Given a char[][] for the map, produces a float[][] that can be used with any FOV methods that expect a
     * resistance map (like {@link #reuseFOV(float[][], float[][], int, int, float)}), but does not treat
     * any cells as partly transparent, only fully-blocking or fully-permitting light. This is mainly useful if you
     * expect the FOV radius to be very high or (effectively) infinite, since anything less than complete blockage would
     * be passed through by infinite-radius FOV. This expects any doors to be represented by '+' if closed or '/' if
     * open, and any walls to be '#' or box drawing characters. This will assign 1.0f resistance to walls and closed
     * doors or 0.0f for any other cell.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class, but with no partially transparent cells
     */
    public static float[][] generateSimpleResistances(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float[][] portion = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ' ':
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                    case '+':
                        portion[i][j] = 1.0f;
                        break;
                    default:
                        portion[i][j] = 0.0f;
                }
            }
        }
        return portion;
    }

    /**
     * Given a char[][] for the map that should use box drawing characters, produces a float[][] with triple width and
     * triple height that can be used with FOV's methods that expect a resistance map (like
     * {@link #reuseFOV(float[][], float[][], int, int, float)}) in classes that use subcell lighting. This expects
     * any doors to be represented by '+' if closed or '/' if open, any walls to be box drawing characters, and any
     * cells that block all subcells within their area to be '#'. This will assign 1.0f resistance to walls and closed
     * doors where a line of the box drawing char would block light, or 0.0f for any other subcell.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map suitable for use with the FOV class and subcell lighting, with triple width/height
     */
    public static float[][] generateSimpleResistances3x3(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float[][] portion = new float[width * 3][height * 3];
        for (int i = 0, x = 0; i < width; i++, x+=3) {
            for (int j = 0, y = 0; j < height; j++, y+=3) {
                switch (map[i][j]) {
                    case '\1':
                    case '#':
                    case '+':
                        portion[x][y] = portion[x+1][y] = portion[x+2][y] =
                                portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                        portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] = 1.0f;
                        break;
                    case '├':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┤':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                    /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┴':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                    /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┬':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┌':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┐':
                        /*portion[x][y] = portion[x+1][y] = portion[x+2][y] =*/
                        portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '└':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = portion[x+2][y+1] =
                            /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┘':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                                    /*portion[x][y+2] = portion[x+1][y+2] = portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '│':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '─':
                        portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] = 1.0f;
                        break;
                    case '╴':
                        portion[x][y+1] = portion[x+1][y+1] = 1.0f;
                        break;
                    case '╵':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/ 1.0f;
                        break;
                    case '╶':
                        portion[x+1][y+1] = portion[x+2][y+1] = 1.0f;
                        break;
                    case '╷':
                        /*portion[x][y+1] =*/ portion[x+1][y+1] = /*portion[x+2][y+1] =*/
                            /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                    case '┼':
                        /*portion[x][y] =*/ portion[x+1][y] = /*portion[x+2][y] =*/
                            portion[x][y+1] = portion[x+1][y+1] = portion[x+2][y+1] =
                                    /*portion[x][y+2] =*/ portion[x+1][y+2] = /*portion[x+2][y+2] =*/ 1.0f;
                        break;
                }
            }
        }
        return portion;
    }
    /**
     * Given a char[][] for the map, produces a float[][] that can be used with the sound-related methods here, allowing
     * sound to pass through thin-enough walls and doors. It expects any doors to be represented by '+' if
     * closed or '/' if open, any walls to be '#' or box drawing characters, anything that can't possibly let sound
     * through to be ' ' or Unicode u0001, and lets everything else permit sound to pass freely. Open doors slightly
     * obscure sound (by 5%), closed doors obscure 30% of sound coming from the other side, walls block 55% of the
     * sound (making walls that are 2-cells-thick block all sound, but 1-cell-thick walls won't), the ' ' and Unicode 1
     * cells block all sound, and everything else lets sound through.
     *
     * @param map a dungeon, width by height, with any closed doors as '+' and open doors as '/' as per closeDoors()
     * @return a resistance map meant for sound resistance rather than light, with clear cells assigned 0.0f and fully-blocked ones 1.0f
     */
    public static float[][] generateSoundResistances(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float[][] portion = new float[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ' ':
                    case '\1':
                        portion[i][j] = 1.0f;
                        break;
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = 0.55f;
                        break;
                    case '/':
                        portion[i][j] = 0.05f;
                        break;
                    case '+':
                        portion[i][j] = 0.3f;
                        break;
                    default:
                        portion[i][j] = 0.0f;
                }
            }
        }
        return portion;
    }

}
