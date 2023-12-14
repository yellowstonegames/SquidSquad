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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * Encapsulates currently-visible and previously-seen cell data, and allows updating and modifying light levels/colors.
 * <br>
 * After creating a VisionFramework, You can call {@link #restart(char[][], CoordFloatOrderedMap, int)} when you have a
 * 2D char array to use as the map and one or more viewer positions. After that, add light sources to {@link #lighting}.
 * Then you can render the map "background" colors by calling {@link #update(float)} (every frame) and getting the
 * colors/opacities for anything that can move around (monsters, some traps or effects) using
 * {@link #getForegroundColor(int, int, float)} (also every frame, once per creature/effect). The update() and
 * getForegroundColor() methods take a float, millisSinceLastMove, which is measured in milliseconds since the last
 * action the player took; this allows cells to fade into or out of view over a short period of time. The background
 * colors are available at {@link #backgroundColors} once update() has been called. When the player character or
 * characters change position (or any special vision the player has moves, like a remote camera), call
 * {@link #moveViewer(Coord, Coord)}. You probably want to move any light source a character carries immediately before
 * calling moveViewer(), so the new position shows the changed lighting right away; moving lights uses
 * {@link LightingManager#moveLight(Coord, Coord)}. When a small part of the world changes, call
 * {@link #editSingle(Coord, char)} (such as for a door opening), or {@link #editAll(char[][])} if a large
 * part of the world changes at once. After any viewer, light source, or map feature changes (or several if they all
 * changed at once), you should call {@link #finishChanges()}, which updates {@link #prunedPlaceMap} with the actually
 * visible map cells, handles lighting changes, and so on.
 * <br>
 * This class uses the Oklab color space throughout. Use {@link VisionFrameworkRgb} if you prefer RGBA colors.
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
    public LightingManager lighting;

    /**
     * The value that {@code lighting.fovResult} held in the previous turn or short period of time.
     * In most roguelikes, there would only need to be one of these variables at a time.
     */
    public float[][] previousLightLevels;

    /**
     * The background color tints; these are the finished colors produced by {@link #update(float)}.
     * These colors are overwritten every time update() is called.
     * <br>
     * This uses packed Oklab int colors, which avoid the overhead of creating new Color objects.
     * Use {@link DescriptiveColor} to get, describe, or create Oklab int colors.
     * You can convert to RGBA8888 (which libGDX uses) with {@link DescriptiveColor#oklabIntToFloat(int)}
     * if you want a "packed float color" that can be given to libGDX directly, or to a Color using
     * {@code Color.rgba8888ToColor(changingColor, DescriptiveColor.toRGBA8888(oklabColor))}.
     * <br>
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
    public Region newlyVisible;
    /**
     * Contains only the cells that have a value greater than 0 in {@code lighting.fovResult}, meaning
     * they are visible right now.
     */
    public Region inView;

    /**
     * Maps the positions of "things that can view the map for the player" to how far each of those things can see.
     * In a traditional roguelike, there is probably just one viewer here unless the game includes remote viewing in
     * some form. In a party-based game, each member of the exploration party is probably a viewer.
     */
    public CoordFloatOrderedMap viewers = new CoordFloatOrderedMap(4);

    /**
     * The int color used to tint cells that could have been seen previously, but aren't currently visible.
     * In VisionFramework, this defaults to the Oklab int color {@code 0xFF7F7F50}, which is fully opaque, pure gray,
     * and has about 30% lightness. You can get Oklab int colors using {@link DescriptiveColor}. In VisionFrameworkRgb,
     * this defaults to the RGBA8888 color 0x505050FF, which is also fully opaque, pure gray, and has about 30%
     * lightness. You can get RGBA8888 int colors using {@link com.github.yellowstonegames.core.DescriptiveColorRgb}.
     */
    public int rememberedColor = 0xFF7F7F50;

    /**
     * The empty constructor. You can call {@link #restart(char[][], CoordFloatOrderedMap, int)} when you have a 2D char
     * array to use as the map and one or more viewer positions. See the {@link VisionFramework class docs} for more
     * usage information.
     */
    public VisionFramework() {
    }

    /**
     * Some form of restart() must be called when the map is first created and whenever the whole local map changes.
     * <br>
     * This overload simplifies the viewers to just the common case of one viewer, the player character. You can specify
     * an {@code fovRange} for how much the player can see without a light source, and you can also choose to add a
     * light at the player's position with {@link #lighting} and its {@link LightingManager#addLight(Coord, Radiance)}
     * method. Remember to move the player with {@link #moveViewer(Coord, Coord)} and any light they carry with
     * {@link LightingManager#moveLight(Coord, Coord)}. If the player's FOV range changes, you can update it with
     * {@link #putViewer(Coord, float)} using the player's current position.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param playerPosition where the one viewer will be put in the place; must be in-bounds for place
     * @param fovRange how far, in grid cells, the player can see without needing a light source
     */
    public void restart(char[][] place, Coord playerPosition, float fovRange) {
        restart(place, CoordFloatOrderedMap.with(playerPosition, fovRange), rememberedColor);
    }

    /**
     * Some form of restart() must be called when the map is first created and whenever the whole local map changes.
     * <br>
     * This overload simplifies the viewers to just the common case of one viewer, the player character. You can specify
     * an {@code fovRange} for how much the player can see without a light source, and you can also choose to add a
     * light at the player's position with {@link #lighting} and its {@link LightingManager#addLight(Coord, Radiance)}
     * method. Remember to move the player with {@link #moveViewer(Coord, Coord)} and any light they carry with
     * {@link LightingManager#moveLight(Coord, Coord)}. If the player's FOV range changes, you can update it with
     * {@link #putViewer(Coord, float)} using the player's current position.
     * <br>
     * This overload allows specifying a {@code baseColor} that will be used cells that were seen previously but can't
     * be seen now; typically this is dark gray or very close to that, and it is an Oklab int color as produced by
     * {@link DescriptiveColor}. The default, if not specified, is {@link #rememberedColor}.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param playerPosition where the one viewer will be put in the place; must be in-bounds for place
     * @param fovRange how far, in grid cells, the player can see without needing a light source
     * @param baseColor the packed Oklab color used for previously-seen, but not in-view, cells
     */

    public void restart(char[][] place, Coord playerPosition, float fovRange, int baseColor) {
        restart(place, CoordFloatOrderedMap.with(playerPosition, fovRange), baseColor);
    }

    /**
     * Some form of restart() must be called when the map is first created and whenever the whole local map changes.
     * <br>
     * This overload allows having one or more viewer positions at the start, with all viewers sharing seen information
     * with each other (enemy characters are not generally viewers here). Each Coord position for a viewer is associated
     * with how much that viewer can see without a light source; you can also choose to add a
     * light at a viewer's position with {@link #lighting} and its {@link LightingManager#addLight(Coord, Radiance)}
     * method. Remember to move any viewer with {@link #moveViewer(Coord, Coord)} and any light they carry with
     * {@link LightingManager#moveLight(Coord, Coord)}. If a viewer's FOV range changes, you can update it with
     * {@link #putViewer(Coord, float)} using that viewer's current position. You can also add new viewers that way.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param viewers a CoordFloatOrderedMap of the positions of viewers to their viewing ranges; directly referenced
     */
    public void restart(char[][] place, CoordFloatOrderedMap viewers) {
        restart(place, viewers, rememberedColor);
    }

    /**
     * Some form of restart() must be called when the map is first created and whenever the whole local map changes.
     * <br>
     * This overload allows having one or more viewer positions at the start, with all viewers sharing seen information
     * with each other (enemy characters are not generally viewers here). Each Coord position for a viewer is associated
     * with how much that viewer can see without a light source; you can also choose to add a
     * light at a viewer's position with {@link #lighting} and its {@link LightingManager#addLight(Coord, Radiance)}
     * method. Remember to move any viewer with {@link #moveViewer(Coord, Coord)} and any light they carry with
     * {@link LightingManager#moveLight(Coord, Coord)}. If a viewer's FOV range changes, you can update it with
     * {@link #putViewer(Coord, float)} using that viewer's current position. You can also add new viewers that way.
     * <br>
     * This overload allows specifying a {@code baseColor} that will be used cells that were seen previously but can't
     * be seen now; typically this is dark gray or very close to that, and it is an Oklab int color as produced by
     * {@link DescriptiveColor}. The default, if not specified, is {@link #rememberedColor}.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param viewers a CoordFloatOrderedMap of the positions of viewers to their viewing ranges; directly referenced
     * @param baseColor the packed Oklab color used for previously-seen, but not in-view, cells
     */
    public void restart(char[][] place, CoordFloatOrderedMap viewers, int baseColor) {
        if (place == null || place.length == 0 || place[0] == null || place[0].length == 0)
            return;
        placeWidth = place.length;
        placeHeight = place[0].length;
        this.viewers = viewers;
        linePlaceMap = linePlaceMap == null || linePlaceMap.length != placeWidth || linePlaceMap[0].length != placeHeight
                ? LineTools.hashesToLines(place, true) : LineTools.hashesToLinesInto(place, linePlaceMap, true);
        prunedPlaceMap = prunedPlaceMap == null || prunedPlaceMap.length != placeWidth || prunedPlaceMap[0].length != placeHeight
                ? ArrayTools.copy(linePlaceMap) : ArrayTools.set(linePlaceMap, prunedPlaceMap);
        if(lighting == null || lighting.width != placeWidth || lighting.height != placeHeight)
            lighting = new LightingManager(FOV.generateSimpleResistances(linePlaceMap), rememberedColor, Radius.CIRCLE, 4f);
        else{
            FOV.fillSimpleResistancesInto(linePlaceMap, lighting.resistances);
            lighting.backgroundColor = rememberedColor;
            lighting.radiusStrategy = Radius.CIRCLE;
            lighting.viewerRange = 4f;
            lighting.lights.clear();
        }
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
        rememberedColor = baseColor;
        if(backgroundColors == null) backgroundColors = ArrayTools.fill(rememberedColor, placeWidth, placeHeight);
        else ArrayTools.fill(backgroundColors, rememberedColor);
    }

    /**
     * Changes the char at {@code newX,newY} to be {@code newCell} and adjusts the light resistance
     * for that cell only in {@link #lighting}. You must call {@link #finishChanges()} when you are
     * done changing the place map, in order to update the lighting with the latest changes.
     * @param newX the x-position to change, as an int
     * @param newY the y-position to change, as an int
     * @param newCell the char value to use at the given position (in {@link #linePlaceMap})
     */
    public void editSingle(int newX, int newY, char newCell) {
        linePlaceMap[newX][newY] = newCell;
        prunedPlaceMap[newX][newY] = newCell;
        lighting.resistances[newX][newY] = FOV.simpleResistance(newCell);
    }

    /**
     * Changes the char at {@code position} to be {@code newCell} and adjusts the light resistance
     * for that cell only in {@link #lighting}. You must call {@link #finishChanges()} when you are
     * done changing the place map, in order to update the lighting with the latest changes.
     * @param position the position to change, as a non-null Coord
     * @param newCell the char value to use at the given position (in {@link #linePlaceMap})
     */
    public void editSingle(Coord position, char newCell) {
        editSingle(position.x, position.y, newCell);
    }

    /**
     * Fully replaces the contents of {@link #linePlaceMap} with those of {@code newPlaceMap},
     * and changes the light resistances for the whole place map in {@link #lighting}.
     * The existing place map should really be the same size as the new place map.You must call
     * {@link #finishChanges()} when you are done changing the place map, in order to update
     * the lighting with the latest changes.
     * @param newPlaceMap
     */
    public void editAll(char[][] newPlaceMap) {
        ArrayTools.set(newPlaceMap, linePlaceMap);
        ArrayTools.set(newPlaceMap, prunedPlaceMap);
        FOV.fillSimpleResistancesInto(linePlaceMap, lighting.resistances);
    }

    /**
     * If a viewer is present at {@code oldX,oldY} in {@link #viewers} and no view is present
     * at {@code newX,newY}, this moves the viewer to {@code newX,newY} and returns true.
     * Otherwise, this does nothing and returns false. You must call {@link #finishChanges()}
     * when you are done changing the place map or viewers, in order to update the lighting
     * with the latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and moving the light source should use {@link LightingManager#moveLight}.
     * @param oldX the x-position of the viewer to move, if one is present
     * @param oldY the y-position of the viewer to move, if one is present
     * @param newX the x-position to move the viewer to, if possible
     * @param newY the y-position to move the viewer to, if possible
     * @return true if the viewer moved, or false otherwise
     */
    public boolean moveViewer(int oldX, int oldY, int newX, int newY) {
        return moveViewer(Coord.get(oldX, oldY), Coord.get(newX, newY));
    }

    /**
     * If a viewer is present at {@code previousPosition} in {@link #viewers} and no view is present
     * at {@code nextPosition}, this moves the viewer to {@code nextPosition} and returns true.
     * Otherwise, this does nothing and returns false. You must call {@link #finishChanges()} when
     * you are done changing the place map or viewers, in order to update the lighting with the
     * latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and moving the light source should use {@link LightingManager#moveLight}.
     * @param previousPosition the position of the viewer to move, if one is present
     * @param nextPosition the position to move the viewer to, if possible
     * @return true if the viewer moved, or false otherwise
     */
    public boolean moveViewer(Coord previousPosition, Coord nextPosition) {
        if(!viewers.containsKey(previousPosition) || viewers.containsKey(nextPosition)) return false;

        return viewers.alter(previousPosition, nextPosition);
    }

    /**
     * If a viewer is present at {@code x,y} in {@link #viewers}, this removes that
     * viewer and returns true. Otherwise, this does nothing and returns false. You must call
     * {@link #finishChanges()} when you are done changing the place map or viewers, in order to
     * update the lighting with the latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and removing the light source should use {@link LightingManager#removeLight}.
     * @param x the x-position of the viewer to remove, if one is present
     * @param y the y-position of the viewer to remove, if one is present
     * @return true if a viewer was removed, or false otherwise
     */
    public boolean removeViewer(int x, int y) {
        return removeViewer(Coord.get(x, y));
    }

    /**
     * If a viewer is present at {@code position} in {@link #viewers}, this removes that
     * viewer and returns true. Otherwise, this does nothing and returns false. You must call
     * {@link #finishChanges()} when you are done changing the place map or viewers, in order to
     * update the lighting with the latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and removing the light source should use {@link LightingManager#removeLight}.
     * @param position the position of the viewer to remove, if one is present
     * @return true if a viewer was removed, or false otherwise
     */
    public boolean removeViewer(Coord position) {
        if(!viewers.containsKey(position)) return false;

        viewers.remove(position);
        return true;
    }

    /**
     * Adds a viewer to {@link #viewers} with the given viewing distance, if there is no viewer already
     * present at {@code x,y}. If a viewer is present at {@code x,y} in {@link #viewers}, then
     * this edits its viewing distance. You must call
     * {@link #finishChanges()} when you are done changing the place map or viewers, in order to
     * update the lighting with the latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and adding a light source should use {@link LightingManager#addLight(int, int, Radiance)}.
     * @param x the x-position of the viewer to place or edit
     * @param y the y-position of the viewer to place or edit
     * @param viewRange the viewing distance to use for the placed or edited viewer
     */
    public void putViewer(int x, int y, float viewRange) {
        viewers.put(Coord.get(x, y), viewRange);
    }

    /**
     * Adds a viewer to {@link #viewers} with the given viewing distance, if there is no viewer already
     * present at {@code position}. If a viewer is present at {@code position} in {@link #viewers}, then
     * this edits its viewing distance. You must call
     * {@link #finishChanges()} when you are done changing the place map or viewers, in order to
     * update the lighting with the latest changes.
     * <br>
     * If a viewer represents a character with a light source, you should probably have the light
     * source known via {@link #lighting}, and adding a light source should use {@link LightingManager#addLight(Coord, Radiance)}.
     * @param position the position of the viewer to place or edit
     * @param viewRange the viewing distance to use for the placed or edited viewer
     */
    public void putViewer(Coord position, float viewRange) {
        viewers.put(position, viewRange);
    }

    /**
     * This completes the changes started by {@link #moveViewer}, {@link #editSingle}, or {@link #editAll(char[][])}
     * and updates the lighting according to those changes. This affects almost all variables present in
     * this object. It should be noted that the methods that change a place map only change {@link #linePlaceMap};
     * that is because the end of this method uses linePlaceMap and the currently seen cells to update
     * {@link #prunedPlaceMap}. Rendering should typically determine what to draw by using prunedPlaceMap.
     */
    public void finishChanges() {
        // store our current lightLevels value into previousLightLevels, since we will calculate a new lightLevels.
        // the previousLightLevels are used to smoothly change the visibility when a cell just becomes hidden.
        ArrayTools.set(lighting.fovResult, previousLightLevels);
        // assigns to justHidden all cells that were visible in lightLevels in the last turn.
        justHidden.refill(previousLightLevels, 0f).not();
        // recalculate all FOV fields for viewers, combine them, store it in lightLevels for the render to use.
        lighting.calculateFOV(viewers, 0, 0, placeWidth, placeHeight);
        // assigns to blockage all cells that were NOT visible in the latest lightLevels calculation.
        blockage.refill(lighting.fovResult, 0f);
        // store current previously-in-view cells as justSeen, so they can be used to ease those cells into being seen.
        justSeen.remake(justHidden);
        // blockage.not() flips its values so now it stores all cells that ARE visible in the latest lightLevels calc.
        inView.remake(blockage.not());
        // stores cells that are currently visible but had never been seen at all (transparent) in earlier frames.
        newlyVisible.remake(inView).andNot(seen);
        // then, seen has all of those cells that have been visible (ever) included in with its cells.
        seen.or(inView);
        // this is roughly `justSeen = inView - justSeen;`, if subtraction worked on Regions.
        justSeen.notAnd(inView);
        // this is roughly `justHidden = justHidden - inView;`, where justHidden had included all previously visible
        // cells, and now will have all currently visible cells removed from it. This leaves the just-hidden cells.
        justHidden.andNot(inView);
        // changes blockage so instead of all currently visible cells, it now stores the cells that would have been
        // adjacent to those cells.
        blockage.fringe8way();
        // takes box-drawing characters (walls) in linePlaceMap that would have segments that aren't visible in
        // seen, then removes the segments that shouldn't be visible and stores the result in prunedPlaceMap.
        LineTools.pruneLines(linePlaceMap, seen, prunedPlaceMap);
    }

    /**
     * For a "foreground" creature or effect that can move between cells, call this every frame to get the color to draw
     * that thing with. The color is a packed Oklab int, as {@link DescriptiveColor} produces. This can return 0 if a
     * creature or thing cannot be seen and is not fading in or out of view. Otherwise, the int color this returns will
     * be white with some level of transparency -- if the creature is in view now and was in view previously, then it
     * will be opaque white, otherwise it will have some transparency between 0 and 1.
     * This takes a float argument, {@code millisSinceLastMove}, that is the number of milliseconds since the player
     * last entered an input that changed the map or its inhabitants. This is used to handle fading creatures into view
     * when the player's input suddenly revealed those creatures, or fading them out of view if they become hidden. You
     * don't need to give milliseconds precisely; while the input is effectively clamped between 0 and 1000, you can
     * multiply the actual milliseconds that have passed by (for example) 4 to reduce the time a fade effect takes to
     * complete (to a quarter-second). Multiplying by a large number will make fades instantaneous.
     * @param pos the position to get the "foreground" color for; does not actually have to have a creature in it
     * @param millisSinceLastMove how many milliseconds have elapsed since the human player last entered an input, for fading
     * @return the packed Oklab int color to tint any foreground creature or object with at {@code pos}
     */
    public int getForegroundColor(Coord pos, float millisSinceLastMove) {
        return getForegroundColor(pos.x, pos.y, millisSinceLastMove);
    }

    /**
     * For a "foreground" creature or effect that can move between cells, call this every frame to get the color to draw
     * that thing with. The color is a packed Oklab int, as {@link DescriptiveColor} produces. This can return 0 if a
     * creature or thing cannot be seen and is not fading in or out of view. Otherwise, the int color this returns will
     * be white with some level of transparency -- if the creature is in view now and was in view previously, then it
     * will be opaque white, otherwise it will have some transparency between 0 and 1. Note that because white is the
     * lightest color that can be represented, and it is the "neutral color" for a tint like this, there is no way for
     * this tint to be used to lighten a sprite or other visual object.
     * <br>
     * This takes a float argument, {@code millisSinceLastMove}, that is the number of milliseconds since the player
     * last entered an input that changed the map or its inhabitants. This is used to handle fading creatures into view
     * when the player's input suddenly revealed those creatures, or fading them out of view if they become hidden. You
     * don't need to give milliseconds precisely; while the input is effectively clamped between 0 and 1000, you can
     * multiply the actual milliseconds that have passed by (for example) 4 to reduce the time a fade effect takes to
     * complete (to a quarter-second). Multiplying by a large number will make fades instantaneous.
     * @param x the x-position to get the "foreground" color for; does not actually have to have a creature in it
     * @param y the y-position to get the "foreground" color for; does not actually have to have a creature in it
     * @param millisSinceLastMove how many milliseconds have elapsed since the human player last entered an input, for fading
     * @return the packed Oklab int color to tint any foreground creature or object with at {@code x,y}
     */
    public int getForegroundColor(int x, int y, float millisSinceLastMove) {
        if (lighting.fovResult[x][y] > 0.01) {
                if(justSeen.contains(x, y))
                    return DescriptiveColor.oklab(1f, 0.5f, 0.5f, millisSinceLastMove * 0.001f);
                return DescriptiveColor.WHITE;
        }
        else if(justHidden.contains(x, y)) {
            return DescriptiveColor.oklab(1f, 0.5f, 0.5f, 1f - millisSinceLastMove * 0.001f);
        }
        return DescriptiveColor.TRANSPARENT;
    }

    /**
     * Updates the lighting effects and writes to {@link #backgroundColors} so the current tint color is stored in every
     * cell of backgroundColors, for every cell in the place map. Call this every frame while lights are updating (for
     * flicker and strobe effects); this does not need to be called if the game has rendering paused for any reason.
     * This takes a float argument, {@code millisSinceLastMove}, that is the number of milliseconds since the player
     * last entered an input that changed the map or its inhabitants. This is used to handle fading creatures into view
     * when the player's input suddenly revealed those creatures, or fading them out of view if they become hidden. You
     * don't need to give milliseconds precisely; while the input is effectively clamped between 0 and 1000, you can
     * multiply the actual milliseconds that have passed by (for example) 4 to reduce the time a fade effect takes to
     * complete (to a quarter-second). Multiplying by a large number will make fades instantaneous.
     * <br>
     * This sets {@link #backgroundColors} to hold visible Oklab int colors where a cell is visible.
     * @param millisSinceLastMove how many milliseconds have elapsed since the human player last entered an input, for fading
     */
    public void update(float millisSinceLastMove) {
        lighting.update();
        final float change = Math.min(Math.max(millisSinceLastMove * 0.001f, 0f), 1f);
        lighting.drawOklab(backgroundColors);

        for (int x = 0; x < placeWidth; x++) {
            for (int y = 0; y < placeHeight; y++) {
                if(lighting.fovResult[x][y] > 0.01) {
                    if(newlyVisible.contains(x, y)){
                        // if a cell just became visible in the last frame, we fade it in over a short animation.
                        backgroundColors[x][y] = DescriptiveColor.fade(backgroundColors[x][y], 1f - change);
                    } else if(justSeen.contains(x, y)){
                        backgroundColors[x][y] = DescriptiveColor.lerpColors(backgroundColors[x][y],
                                rememberedColor, 1f - change);
                    }
                } else if(justHidden.contains(x, y)) {
                    // if a cell was visible in the previous frame but isn't now, we fade it out to the seen color.
                    backgroundColors[x][y] = DescriptiveColor.lerpColors(backgroundColors[x][y],
                            rememberedColor, change);
                } else if(seen.contains(x, y)) {
                    // cells that were seen more than one frame ago, and aren't visible now, appear as a gray memory.
                    backgroundColors[x][y] = rememberedColor;
                }
                else {
                    // cells we have never seen are just not drawn (transparent).
                    backgroundColors[x][y] = 0;
                }
            }
        }

    }
}
