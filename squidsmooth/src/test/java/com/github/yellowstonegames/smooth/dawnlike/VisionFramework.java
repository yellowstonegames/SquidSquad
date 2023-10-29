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
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.grid.*;

/**
 * Encapsulates currently-visible and previously-seen cell data, and allows updating and modifying light levels/colors.
 */
@Beta
public class VisionFramework {

    /**
     * The x-size of all 2D arrays here (the first index).
     */
    public int placeWidth;
    /**
     * The y-size of all 2D arrays here (the second index).
     */
    public int placeHeight;
    /**
     * The place map using box-drawing characters or {@code '#'} for walls, and any other chars for other terrain.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public char[][] linePlaceMap;
    /**
     * The same as {@link #linePlaceMap}, but with any branches of walls that can't be seen trimmed off to only show
     * what is actually visible given the current field of view and the cells seen earlier.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public char[][] prunedPlaceMap;
    /**
     * A resistance map, as produced by {@link FOV#generateSimpleResistances(char[][])} from the current place map.
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
    private float[][] workingLightLevels;
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
     * All cells that we have seen in the past, on this place map.
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
    /**
     * Contains only the cells that have a value greater than 0 in {@link #lightLevels}, meaning
     * they are visible right now.
     */
    public Region inView;

    /**
     * The 2D position of the player (the moving character who the FOV centers upon).
     */
    public Coord player;

    public CoordFloatOrderedMap viewers = new CoordFloatOrderedMap(4);

    public int rememberedOklabColor = DescriptiveColor.GRAY;
    
    public VisionFramework() {

    }

    public void restart(char[][] dungeon, Coord playerPosition, float fovRange) {
        if (dungeon == null || dungeon.length == 0 || dungeon[0] == null || dungeon[0].length == 0)
            return;
        placeWidth = dungeon.length;
        placeHeight = dungeon[0].length;
        player = playerPosition;
        viewers.put(playerPosition, fovRange);
        linePlaceMap = LineTools.hashesToLines(dungeon, true);
        prunedPlaceMap = ArrayTools.copy(linePlaceMap);
        resistance = FOV.generateSimpleResistances(linePlaceMap);
        lightLevels = new float[placeWidth][placeHeight];
        previousLightLevels = new float[placeWidth][placeHeight];
        workingLightLevels = new float[placeWidth][placeHeight];
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        for(ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
            FOV.reuseFOV(resistance, workingLightLevels, e.key.x, e.key.y, e.value, Radius.CIRCLE);
            FOV.addFOVsInto(lightLevels, workingLightLevels);
        }
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
        justHidden = justHidden == null ? new Region(placeWidth, placeHeight) : justHidden.resizeAndEmpty(placeWidth, placeHeight);
        LineTools.pruneLines(linePlaceMap, seen, prunedPlaceMap);
        // 0xFF7F7F50 is fully opaque, pure gray, and about 30% lightness.
        // It affects the default color each cell has, and could change when there is (for instance) a stain or a mark.
        backgroundColors = ArrayTools.fill(0xFF7F7F50, placeWidth, placeHeight);
    }

    public void edit(int newX, int newY, char newCell) {
        edit(Coord.get(newX, newY), newCell);
    }

    public void edit(Coord changedPosition, char newCell) {
        linePlaceMap[changedPosition.x][changedPosition.y] = newCell;
        prunedPlaceMap[changedPosition.x][changedPosition.y] = newCell;
        resistance[changedPosition.x][changedPosition.y] = FOV.simpleResistance(newCell);

        // store our current lightLevels value into previousLightLevels, since we will calculate a new lightLevels.
        // the previousLightLevels are used to smoothly change the visibility when a cell just becomes hidden.
        ArrayTools.set(lightLevels, previousLightLevels);
        // assigns to justHidden all cells that were visible in lightLevels in the last turn.
        justHidden.refill(previousLightLevels, 0f).not();
        // recalculate all FOV fields for viewers, combine them, store it in lightLevels for the render to use.
        for(ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
            FOV.reuseFOV(resistance, workingLightLevels, e.key.x, e.key.y, e.value, Radius.CIRCLE);
            FOV.addFOVsInto(lightLevels, workingLightLevels);
        }
        // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
        blockage.refill(lightLevels, 0f);
        // store current previously-seen cells as justSeen, so they can be used to ease those cells into being seen.
        justSeen.remake(seen);
        // blockage.not() flips its values so now it stores all cells that ARE visible in the latest lightLevels calc.
        // then, seen has all of those cells that have been visible (ever) included in with its cells.
        seen.or(blockage.not());
        // this is roughly `justSeen = seen - justSeen;`, if subtraction worked on Regions.
        justSeen.notAnd(seen);
        // this is roughly `justHidden = justHidden - blockage;`, where justHidden had included all previously visible
        // cells, and now will have all currently visible cells removed from it. This leaves the just-hidden cells.
        justHidden.andNot(blockage);
        // changes blockage so instead of all currently visible cells, it now stores the cells that would have been
        // adjacent to those cells.
        blockage.fringe8way();
    }

    public void move(int oldX, int oldY, int newX, int newY) {
        move(Coord.get(oldX, oldY), Coord.get(newX, newY));
    }

    public void move(Coord previousPosition, Coord nextPosition) {
        if(!viewers.containsKey(previousPosition)) return;

        viewers.alter(previousPosition, nextPosition);
        // store our current lightLevels value into previousLightLevels, since we will calculate a new lightLevels.
        // the previousLightLevels are used to smoothly change the visibility when a cell just becomes hidden.
        ArrayTools.set(lightLevels, previousLightLevels);
        // assigns to justHidden all cells that were visible in lightLevels in the last turn.
        justHidden.refill(previousLightLevels, 0f).not();
        // recalculate all FOV fields for viewers, combine them, store it in lightLevels for the render to use.
        for(ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
            FOV.reuseFOV(resistance, workingLightLevels, e.key.x, e.key.y, e.value, Radius.CIRCLE);
            FOV.addFOVsInto(lightLevels, workingLightLevels);
        }
        // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
        blockage.refill(lightLevels, 0f);
        // store current previously-seen cells as justSeen, so they can be used to ease those cells into being seen.
        justSeen.remake(seen);
        // blockage.not() flips its values so now it stores all cells that ARE visible in the latest lightLevels calc.
        // that gets stored in inView.
        inView.remake(blockage.not());
        // then, seen has all of those cells that have been visible (ever) included in with its cells.
        seen.or(inView);
        // this is roughly `justSeen = seen - justSeen;`, if subtraction worked on Regions.
        justSeen.notAnd(seen);
        // this is roughly `justHidden = justHidden - blockage;`, where justHidden had included all previously visible
        // cells, and now will have all currently visible cells removed from it. This leaves the just-hidden cells.
        justHidden.andNot(blockage);
        // changes blockage so instead of all currently visible cells, it now stores the cells that would have been
        // adjacent to those cells.
        blockage.fringe8way();
    }

    public int getMovingCreatureColor(int x, int y, float timeSpent) {
        if (lightLevels[x][y] > 0.0) {
                if(justSeen.contains(x, y))
                    return DescriptiveColor.fade(rememberedOklabColor, 1f - timeSpent);
                else
                    return rememberedOklabColor;
        }
        else if(justHidden.contains(x, y)) {
            return DescriptiveColor.fade(rememberedOklabColor, timeSpent);
        }
        return DescriptiveColor.TRANSPARENT;
    }
}
