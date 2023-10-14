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

package com.github.yellowstonegames.smooth.dawnlike;

import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.FOV;
import com.github.yellowstonegames.grid.Region;

/**
 * Encapsulates currently-visible and previously-seen cell data, and allows updating and modifying light levels/colors.
 */
public class VisionFramework {
    /**
     * The dungeon map using only {@code '#'} for walls and {@code '.'} for floors.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public char[][] bareDungeon;
    /**
     * The dungeon map using box-drawing characters or {@code '#'} for walls, and any other chars for other terrain.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public char[][] lineDungeon;
    /**
     * The same as {@link #lineDungeon}, but with any branches of walls that can't be seen trimmed off to only show what
     * is actually visible given the current field of view and the cells seen earlier.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public char[][] prunedDungeon;
    /**
     * A resistance map, as produced by {@link FOV#generateSimpleResistances(char[][])} from the current dungeon map.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public float[][] resistance;
    /**
     * The fraction of how lit a cell is currently, with 1.0 fully lit and 0.0 pitch-black.
     * In most roguelikes, there would only need to be one of these variables at a time.
     */
    public float[][] lightLevels;
    /**
     * The value that {@link #lightLevels} held in the previous turn or short period of time.
     * In most roguelikes, there would only need to be one of these variables at a time.
     */
    public float[][] previousLightLevels;
    /**
     * The background color tints as packed Oklab int colors, which avoid the overhead of creating new Color objects.
     * Use {@link DescriptiveColor} to get, describe, or create Oklab int colors.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public int[][] bgColors;

    // Region is a hard-to-explain class, but it's an incredibly useful one for map generation and many other
    // tasks; it stores a region of "on" cells where everything not in that region is considered "off," and can be used
    // as a Collection of Coord points. However, it's more than that! Because of how it is implemented, it can perform
    // bulk operations on as many as 64 points at a time, and can efficiently do things like expanding the "on" area to
    // cover adjacent cells that were "off", retracting the "on" area away from "off" cells to shrink it, getting the
    // surface ("on" cells that are adjacent to "off" cells) or fringe ("off" cells that are adjacent to "on" cells),
    // and generally useful things like picking a random point from all "on" cells.
    // Here, we use a Region to store:
    // all floors that the player can walk on,
    // a small rim of cells just beyond the player's vision that blocks pathfinding to areas we can't see a path to,
    // all cells that we have seen in the past,
    // all cells we just became able to see on this turn or short period of time,
    // and all cells we were able to see, but just became hidden on this turn or short period of time.
    // In most roguelikes, there would be one "floors" and one "seen" per dungeon floor, but just one of the rest total.
    /**
     * All floors that the player can walk on.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public Region floors;
    /**
     * A small rim of cells just beyond the player's vision that blocks pathfinding to areas we can't see a path to.
     * In most roguelikes, this would be temporary and only one would need to exist in total.
     */
    public Region blockage;
    /**
     * All cells that we have seen in the past, on this dungeon map.
     * In most roguelikes, there would be one of these per dungeon floor, and it would persist.
     */
    public Region seen;
    /**
     * All cells we just became able to see on this turn or short period of time.
     * In most roguelikes, this would be temporary and only one would need to exist in total.
     */
    public Region justSeen;
    /**
     * All cells we were able to see, but just became hidden on this turn or short period of time.
     * In most roguelikes, this would be temporary and only one would need to exist in total.
     */
    public Region justHidden;

    // TODO: Change player and fovRange to a CoordFloatOrderedMap or something similar.
    /**
     * How far the player can see without other light sources.
     */
    public int fovRange = 8;

    /**
     * The 2D position of the player (the moving character who the FOV centers upon).
     */
    public Coord player;

}
