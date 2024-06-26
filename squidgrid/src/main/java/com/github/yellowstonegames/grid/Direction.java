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

import com.github.tommyettinger.digital.TrigTools;

/**
 * Represents the eight grid directions and the deltaX, deltaY values associated
 * with those directions.
 * <br>
 * The grid referenced has x positive to the right and y positive upwards on
 * screen.
 *
 * @author <a href="http://squidpony.com">Eben Howard</a> - howard@squidpony.com
 */
public enum Direction {

    UP(0, 1), DOWN(0, -1), LEFT(-1, 0), RIGHT(1, 0), UP_LEFT(-1, 1), UP_RIGHT(1, 1), DOWN_LEFT(-1, -1), DOWN_RIGHT(1, -1), NONE(0, 0);
    /**
     * An array which holds only the four cardinal directions.
     */
    public static final Direction[] CARDINALS = {UP, DOWN, LEFT, RIGHT};
    /**
     * An array which holds only the four cardinal directions in clockwise order.
     */
    public static final Direction[] CARDINALS_CLOCKWISE = {UP, RIGHT, DOWN, LEFT};
    /**
     * An array which holds only the four cardinal directions in counter-clockwise order.
     */
    public static final Direction[] CARDINALS_COUNTERCLOCKWISE = {UP, LEFT, DOWN, RIGHT};
    /**
     * An array which holds only the four diagonal directions.
     */
    public static final Direction[] DIAGONALS = {UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * An array which holds all eight OUTWARDS directions.
     */
    public static final Direction[] OUTWARDS = {UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT};
    /**
     * An array which holds all eight OUTWARDS directions in clockwise order.
     */
    public static final Direction[] CLOCKWISE = {UP, UP_RIGHT, RIGHT, DOWN_RIGHT, DOWN, DOWN_LEFT, LEFT, UP_LEFT};
    /**
     * An array which holds all eight OUTWARDS directions in counter-clockwise order.
     */
    public static final Direction[] COUNTERCLOCKWISE = {UP, UP_LEFT, LEFT, DOWN_LEFT, DOWN, DOWN_RIGHT, RIGHT, UP_RIGHT};
    /**
     * The cached result of {@link #values()}, so you can avoid repeatedly allocating {@code Direction[]} objects.
     * DO NOT MODIFY THIS ARRAY.
     */
    public static final Direction[] ALL = values();
    /**
     * The x coordinate difference for this direction.
     */
    public final int deltaX;
    /**
     * The y coordinate difference for this direction.
     */
    public final int deltaY;
    
    public final Coord coord;
    
    /**
     * Returns the direction that most closely matches the input. This can be any
     * direction, including cardinal and diagonal directions as well as {@link #NONE}
     * in the case that x and y are both 0.
     * <br>
     * This can be used to get the primary intercardinal direction
     * from an origin point to an event point, such as a mouse click on a grid.
     * If the point given is exactly on a boundary between directions then the
     * direction clockwise is returned.
     *
     * @param x the x position relative to the origin (0,0)
     * @param y the y position relative to the origin (0,0)
     * @return the closest matching Direction enum, which may be {@link #NONE}
     */
    public static Direction getDirection(int x, int y) {
        if ((x | y) == 0) return NONE;
        return COUNTERCLOCKWISE[(int)(TrigTools.atan2Turns(y, x) * 8f + 6.5f) & 7];
    }

    /**
     * Gets an estimate at the correct direction that a position lies in given the distance towards it on the x and y
     * axes. If x and y are both between -1 and 1 inclusive, this will always be accurate, and should be faster than
     * {@link #getDirection(int, int)} by avoiding trigonometry or any other math on floats. If at least one of x or y
     * is 0, then this will also be accurate and will produce either a cardinal direction or NONE if both are 0. If x
     * and y are both non-zero, this will always produce a diagonal, even if a cardinal direction should be more
     * accurate; this behavior may sometimes be desirable to detect when some position is even slightly off from a true
     * cardinal direction.
     * @param x the x position relative to the origin (0,0)
     * @param y the y position relative to the origin (0,0)
     * @return the Direction that x,y lies in, roughly; will always be accurate for arguments between -1 and 1 inclusive
     */
    public static Direction getRoughDirection(int x, int y)
    {
        x = (x >> 31 | -x >>> 31); // project nayuki signum
        y = (y >> 31 | -y >>> 31); // project nayuki signum
        switch (x)
        {
            case -1:
                switch (y)
                {
                    case -1: return DOWN_LEFT;
                    case 1: return UP_LEFT;
                    default: return LEFT;
                }
            case 1:
                switch (y)
                {
                    case -1: return DOWN_RIGHT;
                    case 1: return UP_RIGHT;
                    default: return RIGHT;
                }
            default:
                switch (y)
                {
                    case -1: return DOWN;
                    case 1: return UP;
                    default: return NONE;
                }
        }
    }

    /**
     * Returns the direction that most closely matches the input.
     * This can be any of the four cardinal directions as well as {@link #NONE}
     * in the case that x and y are both 0.
     * <br>
     * This can be used to get the primary cardinal direction from an
     * origin point to an event point, such as a mouse click on a grid.
     * If the point given is directly diagonal then the direction clockwise is
     * returned.
     *
     * @param x the x position relative to the origin (0,0)
     * @param y the y position relative to the origin (0,0)
     * @return the closest matching cardinal Direction enum, which may also be {@link #NONE}
     */
    public static Direction getCardinalDirection(int x, int y) {
        if ((x | y) == 0) return NONE;
        return CARDINALS_COUNTERCLOCKWISE[(int)(TrigTools.atan2Turns(y, x) * 4f + 3.5f) & 3];
    }

	/**
	 * @param from
	 *            The starting point.
	 * @param to
	 *            The desired point to reach.
	 * @return The direction to follow to go from {@code from} to {@code to}. It
	 *         can be cardinal or diagonal.
	 */
	public static Direction toGoTo(Coord from, Coord to) {
		return getDirection(to.x - from.x, to.y - from.y);
	}

    /**
     * Returns the Direction one step clockwise, including diagonals.
     *
     * If considering only Cardinal directions, calling this twice will get the
     * next clockwise cardinal direction.
     *
     * @return the Direction one step clockwise, including diagonals
     */
    public Direction clockwise() {
        switch (this) {
            case UP:
                return UP_RIGHT;
            case DOWN:
                return DOWN_LEFT;
            case LEFT:
                return UP_LEFT;
            case RIGHT:
                return DOWN_RIGHT;
            case UP_LEFT:
                return UP;
            case UP_RIGHT:
                return RIGHT;
            case DOWN_LEFT:
                return LEFT;
            case DOWN_RIGHT:
                return DOWN;
            case NONE:
            default:
                return NONE;
        }
    }

    /**
     * Returns the Direction one step counterclockwise, including diagonals.
     *
     * If considering only Cardinal directions, calling this twice will get the
     * next counterclockwise cardinal direction.
     *
     * @return the Direction one step counterclockwise, including diagonals
     */
    public Direction counterClockwise() {
        switch (this) {
            case UP:
                return UP_LEFT;
            case DOWN:
                return DOWN_RIGHT;
            case LEFT:
                return DOWN_LEFT;
            case RIGHT:
                return UP_RIGHT;
            case UP_LEFT:
                return LEFT;
            case UP_RIGHT:
                return UP;
            case DOWN_LEFT:
                return DOWN;
            case DOWN_RIGHT:
                return RIGHT;
            case NONE:
            default:
                return NONE;
        }
    }

    /**
     * Returns the direction directly opposite to this one.
     *
     * @return the direction directly opposite to this one
     */
    public Direction opposite() {
        switch (this) {
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            case UP_LEFT:
                return DOWN_RIGHT;
            case UP_RIGHT:
                return DOWN_LEFT;
            case DOWN_LEFT:
                return UP_RIGHT;
            case DOWN_RIGHT:
                return UP_LEFT;
            case NONE:
            default:
                return NONE;
        }
    }

    /**
     * @return Whether this is a diagonal move.
     */
    public boolean isDiagonal() {
        return (deltaX & deltaY) != 0;
    }

    /**
     * @return Whether this is a cardinal-direction move.
     */
    public boolean isCardinal() {
        return (deltaX + deltaY & 1) == 1;
    }

	/**
	 * @return {@code true} if {@code this} has an upward component.
	 */
	public boolean hasUp() {
		switch (this) {
            case UP:
            case UP_LEFT:
            case UP_RIGHT:
                return true;
            default:
                return false;
        }
	}

	/**
	 * @return {@code true} if {@code this} has a downward component.
	 */
	public boolean hasDown() {
		switch (this) {
            case DOWN:
            case DOWN_LEFT:
            case DOWN_RIGHT:
                return true;
            default:
                return false;
        }
	}

	/**
	 * @return {@code true} if {@code this} has a left component.
	 */
	public boolean hasLeft() {
		switch (this) {
            case DOWN_LEFT:
            case LEFT:
            case UP_LEFT:
                return true;
            default:
                return false;
        }
	}

	/**
	 * @return {@code true} if {@code this} has a right component.
	 */
	public boolean hasRight() {
		switch (this) {
            case RIGHT:
            case DOWN_RIGHT:
            case UP_RIGHT:
                return true;
            default:
                return false;
        }
	}

    Direction(int x, int y) {
        deltaX = x;
        deltaY = y;
        coord = Coord.get(x, y);
    }
}
