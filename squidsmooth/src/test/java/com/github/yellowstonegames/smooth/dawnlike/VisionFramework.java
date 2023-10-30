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
     * Handles all light sources, with varying colors, strengths, and flicker/strobe patterns.
     */
    private LightingManager lighting;

    /**
     * The value that {@code lighting.fovResult} held in the previous turn or short period of time.
     * In most roguelikes, there would only need to be one of these variables at a time.
     */
    public float[][] previousLightLevels;
    /**
     * The background color tints as packed Oklab int colors, which avoid the overhead of creating new Color objects.
     * Use {@link DescriptiveColor} to get, describe, or create Oklab int colors.
     * In most roguelikes, there would be one of these per dungeon floor.
     */
    public int[][] backgroundColors;

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
     * All cells that became visible for the first time on this turn or short period of time. These cells
     * will be fading in from fully-transparent, rather than from a previously-seen gray color.
     */
    private Region newlyVisible;
    /**
     * Contains only the cells that have a value greater than 0 in {@code lighting.fovResult}, meaning
     * they are visible right now.
     */
    public Region inView;

    /**
     * The 2D position of the player (the moving character who the FOV centers upon).
     */
    public Coord player;

    /**
     * Maps the positions of "things that can view the map for the player" to how far each of those things can see.
     * In a traditional roguelike, there is probably just one viewer here unless the game includes remote viewing in
     * some form. In a party-based game, each member of the exploration party is probably a viewer.
     */
    public CoordFloatOrderedMap viewers = new CoordFloatOrderedMap(4);

    /**
     * The Oklab int color used to tint cells that could have been seen previously, but aren't currently visible.
     * This defaults to {@code 0xFF7F7F20}, which is fully opaque, pure gray, and has about 30% lightness.
     * You can get Oklab int colors using {@link DescriptiveColor}.
     */
    public int rememberedOklabColor = 0xFF7F7F50;
    
    public VisionFramework() {

    }

    public void restart(char[][] dungeon, Coord playerPosition, float fovRange) {
        restart(dungeon, CoordFloatOrderedMap.with(playerPosition, fovRange), rememberedOklabColor);
    }
    public void restart(char[][] dungeon, CoordFloatOrderedMap viewers, int baseColor) {
        if (dungeon == null || dungeon.length == 0 || dungeon[0] == null || dungeon[0].length == 0)
            return;
        placeWidth = dungeon.length;
        placeHeight = dungeon[0].length;
        this.viewers = viewers;
        linePlaceMap = LineTools.hashesToLines(dungeon, true);
        prunedPlaceMap = ArrayTools.copy(linePlaceMap);
        lighting = new LightingManager(FOV.generateSimpleResistances(linePlaceMap), rememberedOklabColor, Radius.CIRCLE, 4f);
        // Uses shadowcasting FOV and reuses the visible array without creating new arrays constantly.
        // This gets the FOV with no light on this call, to make sure all data is non-null.
        lighting.calculateFOV(this.viewers, 0, 0, placeWidth, placeHeight);
        // Stores the current light level as the previous light level, to avoid fade-in artifacts.
        previousLightLevels = previousLightLevels == null ? ArrayTools.copy(lighting.fovResult) : ArrayTools.set(lighting.fovResult, previousLightLevels);
        inView = inView == null ? new Region(lighting.fovResult, 0.01f, 2f) : inView.refill(lighting.fovResult, 0.01f, 2f);
        seen = seen == null ? inView.copy() : seen.remake(inView);
        blockage = blockage == null ? new Region(seen).not() : blockage.remake(seen).not();
        inView = inView == null ? seen.copy() : inView.remake(seen);
        justSeen = justSeen == null ? seen.copy() : justSeen.remake(seen);
        justHidden = justHidden == null ? new Region(placeWidth, placeHeight) : justHidden.resizeAndEmpty(placeWidth, placeHeight);
        newlyVisible = newlyVisible == null ? seen.copy() : newlyVisible.remake(seen);
        LineTools.pruneLines(linePlaceMap, seen, prunedPlaceMap);
        rememberedOklabColor = baseColor;
        if(backgroundColors == null) backgroundColors = ArrayTools.fill(rememberedOklabColor, placeWidth, placeHeight);
        else ArrayTools.fill(backgroundColors, rememberedOklabColor);
    }

    public void edit(int newX, int newY, char newCell) {
        edit(Coord.get(newX, newY), newCell);
    }

    public void edit(Coord changedPosition, char newCell) {
        linePlaceMap[changedPosition.x][changedPosition.y] = newCell;
        prunedPlaceMap[changedPosition.x][changedPosition.y] = newCell;
        lighting.resistances[changedPosition.x][changedPosition.y] = FOV.simpleResistance(newCell);

        // store our current light levels value into previousLightLevels, since we will calculate new light levels.
        // the previousLightLevels are used to smoothly change the visibility when a cell just becomes hidden.
        ArrayTools.set(lighting.fovResult, previousLightLevels);
        // assigns to justHidden all cells that were visible in lighting in the last turn.
        justHidden.refill(previousLightLevels, 0f).not();
        // recalculate all FOV fields for viewers, combine them, store it in lighting for the render to use.
//        for(ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
//            FOV.reuseFOV(lighting.resistances, workingLightLevels, e.key.x, e.key.y, e.value, Radius.CIRCLE);
//            FOV.addFOVsInto(lighting.fovResult, workingLightLevels);
//        }
        lighting.calculateFOV(viewers, 0, 0, placeWidth, placeHeight);
        // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
        blockage.refill(lighting.fovResult, 0f);
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
        ArrayTools.set(lighting.fovResult, previousLightLevels);
        // assigns to justHidden all cells that were visible in lightLevels in the last turn.
        justHidden.refill(previousLightLevels, 0f).not();
        // recalculate all FOV fields for viewers, combine them, store it in lightLevels for the render to use.
        lighting.calculateFOV(viewers, 0, 0, placeWidth, placeHeight);
        // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
        blockage.refill(lighting.fovResult, 0f);
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
        if (lighting.fovResult[x][y] > 0.0) {
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

    // TODO: we will need a method that gets called during every frame, that calls the appropriate
    // LightingManager.update() method. Of course, we also need lights to be handled somehow.
}
