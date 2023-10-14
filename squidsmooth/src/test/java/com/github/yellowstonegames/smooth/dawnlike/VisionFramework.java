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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

/**
 * Encapsulates currently-visible and previously-seen cell data, and allows updating and modifying light levels/colors.
 */
public class VisionFramework {

    /**
     * The x-size of all 2D arrays here (the first index).
     */
    public int dungeonWidth;
    /**
     * The y-size of all 2D arrays here (the second index).
     */
    public int dungeonHeight;
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
    public int[][] backgroundColors;

    // Region is a hard-to-explain class, but it's an incredibly useful one for map generation and many other
    // tasks; it stores a region of "on" cells where everything not in that region is considered "off," and can be used
    // as a Collection of Coord points. However, it's more than that! Because of how it is implemented, it can perform
    // bulk operations on as many as 64 points at a time, and can efficiently do things like expanding the "on" area to
    // cover adjacent cells that were "off", retracting the "on" area away from "off" cells to shrink it, getting the
    // surface ("on" cells that are adjacent to "off" cells) or fringe ("off" cells that are adjacent to "on" cells),
    // and generally useful things like picking a random point from all "on" cells.
    // Here, we use a Region to store:
    // a small rim of cells just beyond the player's vision that blocks pathfinding to areas we can't see a path to,
    // all cells that we have seen in the past,
    // all cells we just became able to see on this turn or short period of time,
    // and all cells we were able to see, but just became hidden on this turn or short period of time.
    // In most roguelikes, there would be one "seen" per dungeon floor, but just one of the rest total.
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

    public VisionFramework() {

    }
    public void restart(char[][] dungeon, Coord playerPosition) {
        if(dungeon == null || dungeon.length == 0 || dungeon[0] == null || dungeon[0].length == 0)
            return;
        dungeonWidth = dungeon.length;
        dungeonHeight = dungeon[0].length;
        player = playerPosition;
        lineDungeon = LineTools.hashesToLines(dungeon, true);
        prunedDungeon = ArrayTools.copy(lineDungeon);
        resistance = FOV.generateSimpleResistances(lineDungeon);
        lightLevels = new float[dungeonWidth][dungeonHeight];
        previousLightLevels = new float[dungeonWidth][dungeonHeight];
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        FOV.reuseFOV(resistance, lightLevels, player.x, player.y, fovRange, Radius.CIRCLE);
        // Stores the current light level as the previous light level, to avoid fade-in artifacts.
        ArrayTools.set(lightLevels, previousLightLevels);
        // 0.0 is the upper bound (inclusive), so any Coord in visible that is more well-lit than 0.0 will _not_ be in
        // the blockage Collection, but anything 0.0 or less will be in it. This lets us use blockage to prevent access
        // to cells we can't see from the start of the move.
        blockage = blockage == null ? new Region(lightLevels, 0f) : blockage.refill(lightLevels, 0f);
        // Here we mark the initially seen cells as anything that wasn't included in the unseen "blocked" region.
        // We invert the copy's contents to prepare for a later step, which makes blockage contain only the cells that
        // are above 0.0, then copy it to save this step as the seen cells. We will modify seen later independently of
        // the blocked cells, so a copy is correct here. Most methods on Region objects will modify the
        // Region they are called on, which can greatly help efficiency on long chains of operations.
        seen = seen == null ? blockage.not().copy() : seen.remake(blockage.not());
        justSeen = justSeen == null ? seen.copy() : justSeen.remake(seen);
        justHidden = justHidden == null ? new Region(dungeonWidth, dungeonHeight) : justHidden.resizeAndEmpty(dungeonWidth, dungeonHeight);
        // Here is one of those methods on a Region; fringe8way takes a Region (here, the set of cells
        // that are visible to the player), and modifies it to contain only cells that were not in the last step, but
        // were adjacent to a cell that was present in the last step. This can be visualized as taking the area just
        // beyond the border of a region, using 8-way adjacency here because we specified fringe8way instead of fringe.
        // We do this because it means pathfinding will only have to work with a small number of cells (the area just
        // out of sight, and no further) instead of all invisible cells when figuring out if something is currently
        // impossible to enter.
        blockage.fringe8way();
        LineTools.pruneLines(lineDungeon, seen, prunedDungeon);
        // 0xFF7F7F50 is fully opaque, pure gray, and about 30% lightness.
        // It affects the default color each cell has, and could change when there is (for instance) a stain or a mark.
        backgroundColors = ArrayTools.fill(0xFF7F7F50, dungeonWidth, dungeonHeight);

    }
}
