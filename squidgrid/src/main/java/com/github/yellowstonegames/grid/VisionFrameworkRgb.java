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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
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
 * To recap, the methods here get called in this order:
 * <ol>
 *     <li>Call {@link #restart} when an area map is loaded, giving it the viewer(s) and their vision range(s).</li>
 *     <li>Call {@link LightingManager#addLight(Coord, Radiance)} on {@link #lighting} for every light source.</li>
 *     <li>Every "turn" (when an input is entered), call {@link LightingManager#moveLight(Coord, Coord)} if a light source moved.</li>
 *     <li>Every "turn" (when an input is entered), call {@link #removeViewer(Coord)} if a viewer was removed.</li>
 *     <li>Every "turn" (when an input is entered), call {@link #moveViewer(Coord, Coord)} if a viewer moved.</li>
 *     <li>Every "turn" (when an input is entered), call {@link #editSingle(Coord, char)} if a map cell was altered (such as a door opening).</li>
 *     <li>Every "turn" (when an input is entered), call {@link #editAll(char[][])} if the whole current map was altered (such as going down stairs to a new area).</li>
 *     <li>Every "turn" (when an input is entered), if any of the previous every-turn methods was called, call {@link #finishChanges()} to complete the change.</li>
 *     <li>Every frame, call {@link #update(float)}, passing it the number of milliseconds since the last turn was handled (this number can be altered).</li>
 *     <li>Every frame, call {@link #getForegroundColor(Coord, float)} for every position with a moving creature or object in it, passing it a position to query and the number of milliseconds since the last turn was handled (this number can be altered).</li>
 *     <li>You can get the current colors every frame from {@link #backgroundColors}, which update() changes.</li>
 * </ol>
 * <br>
 * This class uses the RGBA color space throughout. Use {@link VisionFramework} if you prefer Oklab colors.
 */
@Beta
public class VisionFrameworkRgb extends VisionFramework {
    /**
     * The empty constructor. You can call {@link #restart(char[][], CoordFloatOrderedMap, int)} when you have a 2D char
     * array to use as the map and one or more viewer positions. See the {@link VisionFrameworkRgb class docs} for more
     * usage information.
     * <br>
     * This sets the default {@link #rememberedColor} to {@code 0x505050FF}, but otherwise does nothing. Almost all
     * initialization happens in a {@link #restart} method.
     */
    public VisionFrameworkRgb() {
        rememberedColor = 0x505050FF;
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
     * be seen now; typically this is dark gray or very close to that, and it is an RGBA8888 int color as produced by
     * {@link DescriptiveColorRgb}. The default, if not specified, is {@link #rememberedColor}.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param playerPosition where the one viewer will be put in the place; must be in-bounds for place
     * @param fovRange how far, in grid cells, the player can see without needing a light source
     * @param baseColor the RGBA8888 int color used for previously-seen, but not in-view, cells
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
     * <br>
     * This overload allows specifying a {@code baseColor} that will be used cells that were seen previously but can't
     * be seen now; typically this is dark gray or very close to that, and it is an RGBA8888 int color as produced by
     * {@link DescriptiveColorRgb}. The default, if not specified, is {@link #rememberedColor}.
     * @param place a 2D char array representing a local map; {@code '#'} or box drawing characters represent walls
     * @param viewers a CoordFloatOrderedMap of the positions of viewers to their viewing ranges; directly referenced
     * @param baseColor the RGBA8888 int color used for previously-seen, but not in-view, cells
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
            lighting = new LightingManagerRgb(FOV.generateSimpleResistances(linePlaceMap), rememberedColor, Radius.CIRCLE, 4f);
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
     * For a "foreground" creature or effect that can move between cells, call this every frame to get the color to draw
     * that thing with. The color is an RGBA8888 int, as {@link DescriptiveColorRgb} produces. This can return 0 if a
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
     * @return the RGBA8888 int color to tint any foreground creature or object with at {@code pos}
     */
    public int getForegroundColor(Coord pos, float millisSinceLastMove) {
        return getForegroundColor(pos.x, pos.y, millisSinceLastMove);
    }

    /**
     * For a "foreground" creature or effect that can move between cells, call this every frame to get the color to draw
     * that thing with. The color is an RGBA8888 int, as {@link DescriptiveColorRgb} produces. This can return 0 if a
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
     * @return the RGBA8888 int color to tint any foreground creature or object with at {@code x,y}
     */
    public int getForegroundColor(int x, int y, float millisSinceLastMove) {
        if (lighting.fovResult[x][y] > 0.01) {
                if(justSeen.contains(x, y))
                    return 0xFFFFFF00 | Math.min(Math.max((int)(millisSinceLastMove * 0.255999f), 0), 255);
                return DescriptiveColorRgb.WHITE;
        }
        else if(justHidden.contains(x, y)) {
            return 0xFFFFFF00 | 255 - Math.min(Math.max((int)(millisSinceLastMove * 0.255999f), 0), 255);
        }
        return DescriptiveColorRgb.TRANSPARENT;
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
     * This sets {@link #backgroundColors} to hold visible RGBA8888 int colors where a cell is visible.
     * @param millisSinceLastMove how many milliseconds have elapsed since the human player last entered an input, for fading
     */
    public void update(float millisSinceLastMove) {
        lighting.update();
        final float change = Math.min(Math.max(millisSinceLastMove * 0.001f, 0f), 1f);
        lighting.draw(backgroundColors);

        for (int x = 0; x < placeWidth; x++) {
            for (int y = 0; y < placeHeight; y++) {
                if(lighting.fovResult[x][y] > 0.01) {
                    if(newlyVisible.contains(x, y)){
                        // if a cell just became visible in the last frame, we fade it in over a short animation.
                        backgroundColors[x][y] = DescriptiveColorRgb.fade(backgroundColors[x][y], 1f - change);
                    } else if(justSeen.contains(x, y)){
                        backgroundColors[x][y] = DescriptiveColorRgb.lerpColors(backgroundColors[x][y],
                                rememberedColor, 1f - change);
                    }
                } else if(justHidden.contains(x, y)) {
                    // if a cell was visible in the previous frame but isn't now, we fade it out to the seen color.
                    backgroundColors[x][y] = DescriptiveColorRgb.lerpColors(backgroundColors[x][y],
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
