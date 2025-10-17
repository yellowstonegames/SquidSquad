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
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.core.DescriptiveColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A convenience class that makes dealing with multiple colored light sources easier.
 * All fields are public and documented to encourage their use alongside the API methods. The typical usage case for
 * this class is when a game has complex lighting needs that should be consolidated into one LightingManager per level,
 * where a level corresponds to a {@code char[][]}. After constructing a LightingManager with the resistances for a
 * level, you should add all light sources with their positions, such as by using {@link #addLight(int, int, Radiance)}
 * to create a {@link LightSource} with a specific {@link Radiance} at a position, or by directly putting keys and
 * values into {@link #lights}. Then you can calculate the visible cells once lighting is considered (which may include
 * distant lit cells with unseen but unobstructing cells between the viewer and the light) using
 * {@link #calculateFOV(Coord)}, which should be called every time the viewer moves. You can update the flicker
 * and strobe effects on all light sources, which is typically done every frame, using {@link #update()} or
 * {@link #updateAll()} (updateAll() is for when there is no viewer), and once that update() call has been made you can
 * call {@link #draw(int[][])} to change a 2D int array that holds packed int colors (such as the backgrounds in a
 * GlyphMap). To place user-interface lighting effects that don't affect the in-game-world lights, you can use
 * {@link #updateUI(Coord, Radiance)}, which is called after {@link #update()} but before {@link #draw(int[][])}.
 * <br>
 * This class uses the same Oklab color space that {@link DescriptiveColor} uses for almost every place it deals with
 * color. The exception here is {@link #draw(int[][])}, which uses Oklab internally but converts the colors to RGBA8888
 * when it outputs them. You can use the alternative class {@link LightingManagerRgb} if you want to use RGBA colors
 * throughout the lighting code.
 * <br>
 * Honestly, this class is quite complex, and you should really take a look at a demo that uses it to see how the
 * different parts fit together. If you have the SquidSquad test sources, LightingTest in squidglyph provides a
 * relatively simple example using many colors of light. {@link VisionFramework} also can be used to handle much of the
 * boilerplate associated with vision and lighting.
 */
public class LightingManager {

    /**
     * A functional interface that is really only meant to be one of two functions:
     * {@link FOV#reuseFOV(float[][], float[][], int, int, float, Radius)} or
     * {@link FOV#reuseFOVSymmetrical(float[][], float[][], int, int, float, Radius)}.
     * This can be used to, potentially, select symmetrical mode as a slower, more precise option, or use the
     * non-symmetrical mode as a faster option.
     */
    protected interface FovFunction {
        float[][] getFov(float[][] resistanceMap, float[][] light, int startX, int startY, float radius, Radius radiusTechnique);
    }

    /**
     * Used to choose whether FOV calculations should be done as quickly as possible (without symmetry guarantees) or
     * more precisely (and more slowly, but with a symmetry guarantee). If this is SYMMETRICAL, then if cell A lights up
     * cell B, you could move the light from A to B without changing that both are lit up. That guarantee is not present
     * if this is set to FAST.
     */
    public enum SymmetryMode implements FovFunction {
        FAST(FOV::reuseFOV), SYMMETRICAL(FOV::reuseFOVSymmetrical);

        private final FovFunction fun;

        SymmetryMode(FovFunction fun) {
            this.fun = fun;
        }

        public float[][] getFov(float[][] resistanceMap, float[][] light, int startX, int startY, float radius, Radius radiusTechnique) {
            return fun.getFov(resistanceMap, light, startX, startY, radius, radiusTechnique);
        }
    }

    /**
     * How light should spread; usually {@link Radius#CIRCLE} unless gameplay reasons need it to be SQUARE or DIAMOND.
     */
    public Radius radiusStrategy;
    /**
     * The 2D array of light-resistance values from 0.0f to 1.0f for each cell on the map, as produced by
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     */
    public float[][] resistances;
    /**
     * What the "viewer" (as passed to {@link #calculateFOV(Coord)}) can see either nearby without light or because an
     * area in line-of-sight has light in it. Edited by {@link #calculateFOV(Coord)} and {@link #update()}, but
     * not {@link #updateUI(Coord, Radiance)} (which is meant for effects that are purely user-interface).
     */
    public float[][] fovResult;
    /**
     * A 2D array of floats that are either 0.0f if a cell has an obstruction between it and the viewer, or greater than
     * 0.0f otherwise.
     */
    public float[][] losResult;
    /**
     * Used for calculations involving {@link #fovResult}, generally with each colored light individually updating this
     * 2D array and then having this array wiped clean. This serves as an intermediate storage step between the update
     * methods and {@link #mixColoredLighting(float, int)}; the latter depends on this to be set by an update method or
     * by {@link #calculateFOV}.
     */
    public float[][] lightFromFOV;

    /**
     * Used to determine the lighting power, but not colors, of all lights in the scene. This is set in the update
     * methods and used in {@link #mixColoredLighting(float)} and {@link #draw(int[][])}.
     */
    public float[][] lightingStrength;
    /**
     * A temporary work-area variable used to add up or combine multiple float[][] variables.
     */
    private final float[][] floatCombining;
    /**
     * A 2D array that stores the color of light in each cell, as a packed Oklab int color. This 2D array is the size of
     * the map, as defined by {@link #resistances} initially and later available in {@link #width} and {@link #height}.
     */
    public int[][] colorLighting;
    /**
     * Represents the Oklab int colors associated with lights in {@link #lightFromFOV}.
     * To make effective use of this field, you will probably need
     * to be reading the source for LightingManager.
     */
    public int[][] fovLightColors;
    /**
     * Width of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int width;
    /**
     * Height of the 2D arrays used in this, as obtained from {@link #resistances}.
     */
    public int height;
    /**
     * The packed Oklab int color to mix background cells with when a cell has lighting and is within line-of-sight, but
     * has no background color to start with. Its color defaults to the (RGBA or Oklab int) {@code 0}, which is always
     * transparent.
     */
    public int backgroundColor;
    /**
     * How far the viewer can see without light; defaults to 4.0f cells, and you are encouraged to change this member
     * field if the vision range changes after construction.
     */
    public float viewerRange;
    /**
     * A mapping from positions as {@link Coord} objects to {@link Radiance} objects that describe the color, lighting
     * radius, and changes over time of any in-game lights that should be shown on the map and change FOV. You can edit
     * this manually or by using {@link #moveLight(int, int, int, int)}, {@link #addLight(int, int, Radiance)}, and
     * {@link #removeLight(int, int)}.
     */
    public ObjectDeque<LightSource> lights;

    /**
     * A GreasedRegion that stores any cells that are in line-of-sight or are close enough to a cell in line-of-sight to
     * potentially cast light into such a cell. Depends on the highest {@link Radiance#range} in {@link #lights}.
     */
    public Region noticeable;

    public SymmetryMode symmetry;

    /**
     * Unlikely to be used except during serialization; makes a LightingManager for a 20x20 fully visible level.
     * The viewer vision range will be 4.0f, and lights will use a circular shape.
     */
    public LightingManager() {
        this(new float[20][20], 0, Radius.CIRCLE, 4.0f);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations. This will use a solid
     * black background when it casts light on cells without existing lighting. The viewer vision range will be 4.0f, and
     * lights will use a circular shape.
     *
     * @param resistance a resistance array as produced by DungeonUtility
     */
    public LightingManager(float[][] resistance) {
        this(resistance, 0, Radius.CIRCLE, 4.0f);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     *
     * @param resistance        a resistance array as produced by DungeonUtility
     * @param backgroundColor   the background color to use, as a color description
     * @param radiusStrategy    the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                          of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManager(float[][] resistance, String backgroundColor, Radius radiusStrategy, float viewerVisionRange) {
        this(resistance, DescriptiveColor.describeOklab(backgroundColor), radiusStrategy, viewerVisionRange);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     *
     * @param resistance        a resistance array as produced by DungeonUtility
     * @param backgroundColor   the background color to use, as a packed Oklab int
     * @param radiusStrategy    the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                          of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManager(float[][] resistance, int backgroundColor, Radius radiusStrategy, float viewerVisionRange) {
        this(resistance, backgroundColor, radiusStrategy, viewerVisionRange, SymmetryMode.SYMMETRICAL);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     *
     * @param resistance        a resistance array as produced by DungeonUtility
     * @param backgroundColor   the background color to use, as a packed Oklab int
     * @param radiusStrategy    the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                          of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManager(float[][] resistance, int backgroundColor, Radius radiusStrategy, float viewerVisionRange, SymmetryMode symmetry) {
        this.radiusStrategy = radiusStrategy;
        this.symmetry = symmetry;
        viewerRange = viewerVisionRange;
        this.backgroundColor = backgroundColor;
        resistances = resistance;
        width = resistances.length;
        height = resistances[0].length;
        fovResult = new float[width][height];
        lightFromFOV = new float[width][height];
        losResult = new float[width][height];
        colorLighting = ArrayTools.fill(getNeutralColor(), width, height);
        lightingStrength = new float[width][height];
        floatCombining = new float[width][height];
        fovLightColors = new int[width][height];
        Coord.expandPoolTo(width, height);
        lights = new ObjectDeque<>(32);
        noticeable = new Region(width, height);
    }

    public SymmetryMode getSymmetry() {
        return symmetry;
    }

    public void setSymmetry(SymmetryMode symmetry) {
        this.symmetry = symmetry;
    }

    /**
     * An extension point for subclasses that don't use the Oklab color space; this defaults to returning
     * {@link DescriptiveColor#WHITE}. There is no setter or field for the neutral color.
     *
     * @return if not overridden, {@link DescriptiveColor#WHITE}
     */
    public int getNeutralColor() {
        return DescriptiveColor.WHITE;
    }

    /**
     * Returns how many LightSources are at the given {@code position}.
     * @param position the position to count at
     * @return a non-negative int, at least 0 and less than {@code lights.size}
     */
    public int lightCount(Coord position) {
        int count = 0;
        for (int i = 0; i < lights.size; i++) {
            if (lights.get(i).position.equals(position)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Adds a Radiance as a LightSource at the given position. The LightSource will spread light in all directions.
     *
     * @param x     the x-position to add the LightSource at
     * @param y     the y-position to add the LightSource at
     * @param light a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingManager addLight(int x, int y, Radiance light) {
        return addLight(Coord.get(x, y), light);
    }

    /**
     * Adds a Radiance as a LightSource at the given position. The LightSource will spread light in all directions.
     *
     * @param position the position to add the LightSource at
     * @param light    a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @return this for chaining
     */
    public LightingManager addLight(Coord position, Radiance light) {
        lights.add(new LightSource(position, light));
        return this;
    }

    /**
     * Adds a LightSource using the given Radiance at the given position. This also restricts the lit span to
     * {@code spanTurns} (between 0 and 1) and sets the initial direction of the light to {@code angleTurns} (also
     * between 0 and 1).
     *
     * @param position   the position to add the Radiance at
     * @param light      a Radiance object that can have a changing radius, color, and various other effects on lighting
     * @param spanTurns  how wide of an arc the LightSource will cover, measured in turns
     * @param angleTurns what direction the LightSource will point towards, measured in turns
     * @return this for chaining
     */
    public LightingManager addLight(Coord position, Radiance light, float spanTurns, float angleTurns) {
        lights.add(new LightSource(position, light, spanTurns, angleTurns));
        return this;
    }

    /**
     * Adds a LightSource to the collection this has for lights.
     * This is largely irrelevant, since you can just call {@link ObjectDeque#add(Object)} on {@link #lights}.
     *
     * @param light an existing LightSource, with a position and Radiance, to add
     * @return this for chaining
     */
    public LightingManager addLight(LightSource light) {
        lights.add(light);
        return this;
    }

    /**
     * Removes the first encountered LightSource with the given position, if any is present.
     *
     * @param x the x-position to remove the LightSource from
     * @param y the y-position to remove the LightSource from
     * @return true if any light was removed, false otherwise
     */
    public boolean removeLight(int x, int y) {
        return removeLight(Coord.get(x, y), 0);
    }

    /**
     * Removes the nth encountered LightSource with the given position, where n is {@code index}, if any is present.
     *
     * @param x the x-position to remove the LightSource from
     * @param y the y-position to remove the LightSource from
     * @param index which of the possible lights at x,y to remove
     * @return true if any light was removed, false otherwise
     */
    public boolean removeLight(int x, int y, int index) {
        return removeLight(Coord.get(x, y), index);
    }

    /**
     * Removes the first encountered LightSource with the given position, if any is present.
     *
     * @param position the position to remove the LightSource from
     * @return true if any light was removed, false otherwise
     */
    public boolean removeLight(Coord position) {
        return removeLight(position, 0);
    }
    /**
     * Removes the nth encountered LightSource with the given position, where n is {@code index}, if any is present.
     *
     * @param position the position to remove the LightSource from
     * @param index which of the possible lights at position to remove
     * @return true if any light was removed, false otherwise
     */
    public boolean removeLight(Coord position, int index) {
        for (int i = 0; i < lights.size; i++) {
            if (lights.get(i).position.equals(position) && index-- <= 0) {
                lights.removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * If at least one LightSource is present at oldX,oldY, this will move the first LightSource to newX,newY.
     * If no LightSource is present at oldX,oldY, this does nothing. This will not remove any LightSource already
     * present at newX,newY.
     *
     * @param oldX the x-position to move a LightSource from
     * @param oldY the y-position to move a LightSource from
     * @param newX the x-position to move a LightSource to
     * @param newY the y-position to move a LightSource to
     * @return true if any light was moved, or false otherwise
     */
    public boolean moveLight(int oldX, int oldY, int newX, int newY) {
        return moveLight(Coord.get(oldX, oldY), Coord.get(newX, newY), 0);
    }

    /**
     * If at least one LightSource is present at oldX,oldY, this will move the nth LightSource at oldX,oldY, where
     * n is {@code index}, to newX,newY.
     * If no LightSource is present at oldX,oldY, this does nothing. This also does nothing if index is greater than
     * or equal to the number of lights at oldX,oldY.
     * This will not remove any LightSource already present at newX,newY.
     *
     * @param oldX  the x-position to move a LightSource from
     * @param oldY  the y-position to move a LightSource from
     * @param newX  the x-position to move a LightSource to
     * @param newY  the y-position to move a LightSource to
     * @param index which of the possible lights at oldPosition to move
     * @return true if any light was moved, or false otherwise
     */
    public boolean moveLight(int oldX, int oldY, int newX, int newY, int index) {
        return moveLight(Coord.get(oldX, oldY), Coord.get(newX, newY), index);
    }

    /**
     * If at least one LightSource is present at oldPosition, this will move the first LightSource to newPosition.
     * If no LightSource is present at oldPosition, this does nothing. This will not remove any LightSource already
     * present at newPosition.
     *
     * @param oldPosition the Coord to move a LightSource from
     * @param newPosition the Coord to move a LightSource to
     * @return true if any light was moved, or false otherwise
     */
    public boolean moveLight(Coord oldPosition, Coord newPosition) {
        return moveLight(oldPosition, newPosition, 0);
    }

    /**
     * If at least one LightSource is present at oldPosition, this will move the nth LightSource at oldPosition, where
     * n is {@code index}, to newPosition.
     * If no LightSource is present at oldPosition, this does nothing. This also does nothing if index is greater than
     * or equal to the number of lights at oldPosition.
     * This will not remove any LightSource already present at newPosition.
     *
     * @param oldPosition the Coord to move a LightSource from
     * @param newPosition the Coord to move a LightSource to
     * @param index       which of the possible lights at oldPosition to move
     * @return true if any light was moved, or false otherwise
     */
    public boolean moveLight(Coord oldPosition, Coord newPosition, int index) {
        LightSource ls;
        for (int i = 0; i < lights.size; i++) {
            if ((ls = lights.get(i)).position.equals(oldPosition) && index-- <= 0) {
                ls.position = newPosition;
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the nth LightSource at any position, where n is {@code index}.
     * This is largely unnecessary because you can call {@link ObjectDeque#get(int)} on {@link #lights}. The only
     * difference here is that this method returns null if {@code index} is out-of-bounds, while ObjectQueue.get(int)
     * throws an Exception then.
     * @param index which LightSource index to get
     * @return the LightSource with the given index across all positions, or null if index if out of bounds.
     */
    public LightSource get(int index) {
        if (index < 0 || index >= lights.size)
            return null;
        return lights.get(index);
    }
    /**
     * Gets the first LightSource at the given position, if present, or null if there is no LightSource there.
     *
     * @param x the x-position to look up
     * @param y the y-position to look up
     * @return the first LightSource at the given position, or null if none is present there
     */
    public LightSource get(int x, int y) {
        return get(Coord.get(x, y));
    }

    /**
     * Gets the nth LightSource at the given position, where n is {@code index},
     * or null if there is no LightSource there.
     *
     * @param x     the x-position to look up
     * @param y     the y-position to look up
     * @param index which LightSource to get from the given position
     * @return the nth LightSource at the given position, or null if none is present there
     */
    public LightSource get(int x, int y, int index) {
        return get(Coord.get(x, y), index);
    }

    /**
     * Gets the first LightSource at the given position, if present,
     * or null if there is no LightSource there.
     *
     * @param position the position to look up
     * @return the first LightSource at the given position, or null if none is present there
     */
    public LightSource get(Coord position) {
        return get(position, 0);
    }

    /**
     * Gets the nth LightSource at the given position, where n is {@code index},
     * or null if there is no LightSource there.
     *
     * @param position the position to look up
     * @param index    which LightSource to get from the given position
     * @return the nth LightSource at the given position, or null if none is present there
     */
    public LightSource get(Coord position, int index) {
        LightSource ls;
        for (int i = 0; i < lights.size; i++) {
            if ((ls = lights.get(i)).position.equals(position) && index-- <= 0) {
                return ls;
            }
        }
        return null;
    }

    /**
     * Finds all LightSources this has with the given {@code position} and adds them to a newly-allocated ObjectList,
     * which this returns.
     * @param position the position to look up LightSources at
     * @return a newly-allocated ObjectList that may have LightSource items which were found at position
     */
    public ObjectList<LightSource> lightsAt(Coord position) {
        ObjectList<LightSource> buffer = new ObjectList<>(8);
        lightsAt(buffer, position);
        return buffer;
    }

    /**
     * Finds all LightSources this has with the given {@code position} and appends them to {@code buffer}.
     * @param buffer any modifiable Collection of LightSource items; may be modified
     * @param position the position to look up LightSources at
     * @return buffer, potentially after modification
     */
    public Collection<LightSource> lightsAt(Collection<LightSource> buffer, Coord position) {
        LightSource ls;
        for (int i = 0; i < lights.size; i++) {
            if ((ls = lights.get(i)).position.equals(position)) {
                buffer.add(ls);
            }
        }
        return buffer;
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the colors in {@link #fovLightColors}, with the strength
     * of light in fovLightColors boosted by flare (which can be any finite float greater than -1f, but is usually
     * from 0f to 1f when increasing strength). The strengths of each colored light is determined by
     * {@link #lightFromFOV} and the colors of lights are determined by {@link #fovLightColors}. If a color of light is
     * fully transparent, this skips that light.
     * <br>
     * This is very limited-use; the related method {@link #mixColoredLighting(float, int)} is used as part of
     * {@link #update()}, but this method is meant for when multiple colors of FOV light need to be mixed at once.
     *
     * @param flare boosts the effective strength of lighting in {@link #fovLightColors}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare) {
        int[][] basis = colorLighting, other = fovLightColors;
        float[][] basisStrength = lightingStrength, otherStrength = lightFromFOV;
        flare += 1f;
        float bs;
        int b;
        float os;
        int o;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        os = 0f;
                        if (y > 0) {
                            if ((losResult[x][y - 1] > 0 && otherStrength[x][y - 1] > 0 && resistances[x][y - 1] < 1)
                                    || (x > 0 && losResult[x - 1][y - 1] > 0 && otherStrength[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y - 1] > 0 && otherStrength[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (y < height - 1) {
                            if ((losResult[x][y + 1] > 0 && otherStrength[x][y + 1] > 0 && resistances[x][y + 1] < 1)
                                    || (x > 0 && losResult[x - 1][y + 1] > 0 && otherStrength[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y + 1] > 0 && otherStrength[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && otherStrength[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && otherStrength[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if (os > 0f) o = other[x][y];
                        else continue;
                    } else {
                        os = otherStrength[x][y];
                        o = other[x][y];
                    }
                    if (os <= 0f || (o & 0xFE000000) == 0f)
                        continue;
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFF) == 0xFF) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFF) != 0xFF) {
                            final int
                                    sL = (b & 0xFF), sA = (b >>> 8) & 0xFF, sB = (b >>> 16) & 0xFF, sAlpha = b & 0xFE000000,
                                    eL = (o & 0xFF), eA = (o >>> 8) & 0xFF, eB = (o >>> 16) & 0xFF, eAlpha = o >>> 25;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * 0.007874016f;
                            basis[x][y] = (((int) (sL + change * (eL - sL)) & 0xFF)
                                    | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                                    | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                                    | sAlpha);
                            basisStrength[x][y] = Math.min(1.0f, bs + os * change * flare);
                        } else {
                            basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Edits {@link #colorLighting} by adding in and mixing the given color where the light strength in
     * {@link #lightFromFOV} is greater than 0, with that strength boosted by flare (which can be any finite float
     * greater than -1f, but is usually from 0f to 1f when increasing strength). This draws its existing lighting
     * strength from {@link #lightingStrength} and its existing light colors from {@link #colorLighting}; it modifies
     * both of these.
     * <br>
     * This has limited use outside this class, unless you are reimplementing part of {@link #update()} or something
     * like it.
     *
     * @param flare boosts the effective strength of lighting in {@link #lightFromFOV}; usually from 0 to 1
     * @param color the Oklab color to mix in where the light strength in {@link #lightFromFOV} is greater than 0
     */
    public void mixColoredLighting(float flare, int color) {
        final int[][] basis = colorLighting;
        final float[][] basisStrength = lightingStrength;
        final float[][] otherStrength = lightFromFOV;
        flare += 1f;
        float bs, os;
        int b, o;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0) {
                    if (resistances[x][y] >= 1) {
                        os = 0f;
                        if (y > 0) {
                            if ((losResult[x][y - 1] > 0 && otherStrength[x][y - 1] > 0 && resistances[x][y - 1] < 1)
                                    || (x > 0 && losResult[x - 1][y - 1] > 0 && otherStrength[x - 1][y - 1] > 0 && resistances[x - 1][y - 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y - 1] > 0 && otherStrength[x + 1][y - 1] > 0 && resistances[x + 1][y - 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (y < height - 1) {
                            if ((losResult[x][y + 1] > 0 && otherStrength[x][y + 1] > 0 && resistances[x][y + 1] < 1)
                                    || (x > 0 && losResult[x - 1][y + 1] > 0 && otherStrength[x - 1][y + 1] > 0 && resistances[x - 1][y + 1] < 1)
                                    || (x < width - 1 && losResult[x + 1][y + 1] > 0 && otherStrength[x + 1][y + 1] > 0 && resistances[x + 1][y + 1] < 1)) {
                                os = otherStrength[x][y];
                            }
                        }
                        if (x > 0 && losResult[x - 1][y] > 0 && otherStrength[x - 1][y] > 0 && resistances[x - 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if (x < width - 1 && losResult[x + 1][y] > 0 && otherStrength[x + 1][y] > 0 && resistances[x + 1][y] < 1) {
                            os = otherStrength[x][y];
                        }
                        if (os > 0f) o = color;
                        else continue;
                    } else {
                        if ((os = otherStrength[x][y]) != 0) o = color;
                        else continue;
                    }
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFF) == 0xFF) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFF) != 0xFF) {
                            final int
                                    sL = (b & 0xFF), sA = (b >>> 8) & 0xFF, sB = (b >>> 16) & 0xFF, sAlpha = b & 0xFE000000,
                                    eL = (o & 0xFF), eA = (o >>> 8) & 0xFF, eB = (o >>> 16) & 0xFF, eAlpha = o >>> 25;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * 0.007874016f;
                            basis[x][y] = (((int) (sL + change * (eL - sL)) & 0xFF)
                                    | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                                    | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                                    | sAlpha);
                            basisStrength[x][y] = Math.min(1.0f, bs + os * change * flare);
                        } else {
                            basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                        }
                    }
                }
            }
        }
    }

    /**
     * Typically called every frame, this updates the flicker and strobe effects of Radiance objects and applies those
     * changes in lighting color and strength to the various fields of this LightingManager. This will only have an
     * effect if {@link #calculateFOV(Coord)} or {@link #calculateFOV(int, int)} was called during the last time the
     * viewer position changed; typically calculateFOV() only needs to be called once per move, while update() needs to
     * be called once per frame. This method is usually called before each call to {@link #draw(int[][])}, but other
     * code may be between the calls and may affect the lighting in customized ways.
     */
    public void update() {
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, getNeutralColor());
        final int sz = lights.size();
        LightSource ls;
        Coord pos;
        for (int i = 0; i < sz; i++) {
            ls = lights.get(i);
            pos = ls.position;
            if (!noticeable.contains(pos))
                continue;
            Radiance radiance = ls.radiance;
            if (radiance == null) continue;
            if (symmetry == SymmetryMode.SYMMETRICAL || ls.span >= 1f)
                symmetry.getFov(resistances, lightFromFOV, pos.x, pos.y, radiance.currentRange(), radiusStrategy);
            else
                FOV.reuseFOVTurns(resistances, lightFromFOV, pos.x, pos.y, radiance.currentRange(), radiusStrategy, ls.direction, ls.span);
            mixColoredLighting(radiance.flare, radiance.color);
        }
    }

    /**
     * Typically called every frame when there isn't a single viewer, this updates the flicker and strobe effects of
     * Radiance objects and applies those changes in lighting color and strength to the various fields of this
     * LightingManager. This method is usually called before each call to {@link #draw(int[][])}, but other code may
     * be between the calls and may affect the lighting in customized ways. This overload has no viewer, so all cells
     * are considered visible unless they are fully obstructed (solid cells behind walls, for example). Unlike update(),
     * this method does not need {@link #calculateFOV(Coord)} to be called for it to work properly.
     */
    public void updateAll() {
        for (int x = 0; x < width; x++) {
            PER_CELL:
            for (int y = 0; y < height; y++) {
                for (int xx = Math.max(0, x - 1), xi = 0; xi < 3 && xx < width; xi++, xx++) {
                    for (int yy = Math.max(0, y - 1), yi = 0; yi < 3 && yy < height; yi++, yy++) {
                        if (resistances[xx][yy] < 1.0f) {
                            losResult[x][y] = 1.0f;
                            continue PER_CELL;
                        }
                    }
                }
            }
        }
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, getNeutralColor());
        final int sz = lights.size();
        for (int i = 0; i < sz; i++) {
            LightSource ls = lights.get(i);
            Coord pos = ls.position;
            Radiance radiance = ls.radiance;
            if (radiance == null) continue;
            if (symmetry == SymmetryMode.SYMMETRICAL || ls.span >= 1f)
                symmetry.getFov(resistances, lightFromFOV, pos.x, pos.y, radiance.currentRange(), radiusStrategy);
            else
                FOV.reuseFOVTurns(resistances, lightFromFOV, pos.x, pos.y, radiance.currentRange(), radiusStrategy, ls.direction, ls.span);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f) {
                    fovResult[x][y] = Math.min(Math.max(losResult[x][y] + lightingStrength[x][y], 0), 1);
                }
            }
        }
    }

    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(int[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     *
     * @param pos      the position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this
     */
    public void updateUI(Coord pos, Radiance radiance) {
        updateUI(pos.x, pos.y, radiance);
    }

    /**
     * Updates the flicker and strobe effects of a Radiance object and applies the lighting from just that Radiance to
     * just the {@link #colorLighting} field, without changing FOV. This method is meant to be used for GUI effects that
     * aren't representative of something a character in the game could interact with. It is usually called after
     * {@link #update()} and before each call to {@link #draw(int[][])}, but other code may be between the calls
     * and may affect the lighting in customized ways.
     *
     * @param lightX   the x-position of the light effect
     * @param lightY   the y-position of the light effect
     * @param radiance the Radiance to update standalone, which does not need to be already added to this
     */
    public void updateUI(int lightX, int lightY, Radiance radiance) {
        symmetry.getFov(resistances, lightFromFOV, lightX, lightY, radiance.currentRange(), radiusStrategy);
        mixColoredLighting(radiance.flare, radiance.color);
    }

    /**
     * Given a 2D array that should hold RGBA int colors, fills the 2D array with different RGBA colors based on what
     * lights are present in line of sight of the viewer and the various flicker or strobe effects that Radiance light
     * sources can do. You should usually call {@link #update()} before each call to draw(), but you may want to make
     * custom changes to the lighting in between those two calls (that is the only place those changes will be noticed).
     * A common use for this in text-based games uses a GlyphMap's backgrounds field as the parameter.
     * This always mixes the calculated lights in {@link #colorLighting} with the {@link #backgroundColor}, using
     * {@link #lightingStrength} to determine how much the lights should affect the background color.
     * <br>
     * If this class is extended, this method should be considered as one to override.
     *
     * @param backgrounds a 2D int array, which will be modified in-place; visible cells will receive RGBA8888 colors
     */
    public void draw(int[][] backgrounds) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f && fovResult[x][y] > 0.0f) {
                    backgrounds[x][y] = DescriptiveColor.toRGBA8888(DescriptiveColor.lerpColorsBlended(backgroundColor,
                            colorLighting[x][y], lightingStrength[x][y]));
                }
            }
        }
    }

    /**
     * Given a 2D array that will hold Oklab int colors, fills the 2D array with different Oklab colors based on what
     * lights are present in line of sight of the viewer and the various flicker or strobe effects that Radiance light
     * sources can do. You should usually call {@link #update()} before each call to drawOklab(), but you may want to
     * make custom changes to the lighting in between those two calls (that is the only place those changes will be
     * noticed). A common use for this in text-based games uses a GlyphMap's backgrounds field as the parameter.
     * This always mixes the calculated lights in {@link #colorLighting} with the {@link #backgroundColor}, using
     * {@link #lightingStrength} to determine how much the lights should affect the background color.
     * <br>
     * If this class is extended, this method should be considered as one to override.
     *
     * @param backgrounds a 2D int array, which will be modified in-place; visible cells will receive Oklab colors
     */
    public void drawOklab(int[][] backgrounds) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f && fovResult[x][y] > 0.0f) {
                    backgrounds[x][y] = DescriptiveColor.lerpColorsBlended(backgroundColor,
                            colorLighting[x][y], lightingStrength[x][y]);
                }
            }
        }
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     *
     * @param viewer the position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(Coord viewer) {
        return calculateFOV(viewer.x, viewer.y);
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. Sets
     * {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the given viewer position and any lights
     * in {@link #lights}.
     *
     * @param viewerX the x-position of the player or other viewer
     * @param viewerY the y-position of the player or other viewer
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(int viewerX, int viewerY) {
        return calculateFOV(viewerX, viewerY, 0, 0, width, height);
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. This overload
     * allows the area this processes to be restricted to a rectangle between {@code minX} and {@code maxX} and between
     * {@code minY} and {@code maxY}, ignoring any lights outside that area (typically because they are a long way out
     * from the map's shown area). Sets {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the
     * given viewer position and any lights in {@link #lights}.
     *
     * @param viewerX the x-position of the player or other viewer
     * @param viewerY the y-position of the player or other viewer
     * @param minX    inclusive lower bound on x to calculate
     * @param minY    inclusive lower bound on y to calculate
     * @param maxX    exclusive upper bound on x to calculate
     * @param maxY    exclusive upper bound on y to calculate
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(int viewerX, int viewerY, int minX, int minY, int maxX, int maxY) {
        Radiance radiance;
        minX = Math.min(Math.max(minX, 0), width);
        maxX = Math.min(Math.max(maxX, 0), width);
        minY = Math.min(Math.max(minY, 0), height);
        maxY = Math.min(Math.max(maxY, 0), height);
        symmetry.getFov(resistances, fovResult, viewerX, viewerY, viewerRange, radiusStrategy);
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, getNeutralColor());
        final int sz = lights.size();
        float maxRange = 0, range;
        LightSource ls;
        Coord pos;
        for (int i = 0; i < sz; i++) {
            ls = lights.get(i);
            pos = ls.position;
            radiance = ls.radiance;
            if (radiance == null) continue;
            range = radiance.range;
            if (range > maxRange &&
                    pos.x + range >= minX && pos.x - range < maxX && pos.y + range >= minY && pos.y - range < maxY)
                maxRange = range;
        }
        FOV.reuseLOS(resistances, losResult, viewerX, viewerY, minX, minY, maxX, maxY);
        noticeable.refill(losResult, 0.0001f, Float.POSITIVE_INFINITY).expand8way((int) Math.ceil(maxRange));
        for (int i = 0; i < sz; i++) {
            ls = lights.get(i);
            pos = ls.position;
            if (!noticeable.contains(pos))
                continue;
            radiance = ls.radiance;
            if (radiance == null) continue;
            if (symmetry == SymmetryMode.SYMMETRICAL || ls.span >= 1f)
                symmetry.getFov(resistances, lightFromFOV, pos.x, pos.y, radiance.range, radiusStrategy);
            else
                FOV.reuseFOVTurns(resistances, lightFromFOV, pos.x, pos.y, radiance.range, radiusStrategy, ls.direction, ls.span);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = Math.max(0, minX); x < maxX && x < width; x++) {
            for (int y = Math.max(0, minY); y < maxY && y < height; y++) {
                if (losResult[x][y] > 0.0f) {
                    fovResult[x][y] = Math.min(Math.max(fovResult[x][y] + lightingStrength[x][y], 0), 1);
                }
            }
        }
        return fovResult;
    }

    /**
     * Used to calculate what cells are visible as if any flicker or strobe effects were simply constant light sources.
     * Runs part of the calculations to draw lighting as if all radii are at their widest, but does no actual drawing.
     * This should be called any time the viewer moves to a different cell, and it is critical that this is called (at
     * least) once after a move but before {@link #update()} gets called to change lighting at the new cell. This sets
     * important information on what lights might need to be calculated during each update(Coord) call; it does not need
     * to be called before {@link #updateAll()} (with no arguments) because that doesn't need a viewer. This overload
     * allows the area this processes to be restricted to a rectangle between {@code minX} and {@code maxX} and between
     * {@code minY} and {@code maxY}, ignoring any lights outside that area (typically because they are a long way out
     * from the map's shown area). Sets {@link #fovResult}, {@link #losResult}, and {@link #noticeable} based on the
     * given viewer position and any lights in {@link #lights}.
     *
     * @param viewers an ObjectFloatMap with Coord keys representing viewer positions and float values for their vision ranges; often a {@link CoordFloatOrderedMap}
     * @param minX    inclusive lower bound on x to calculate
     * @param minY    inclusive lower bound on y to calculate
     * @param maxX    exclusive upper bound on x to calculate
     * @param maxY    exclusive upper bound on y to calculate
     * @return the calculated FOV 2D array, which is also stored in {@link #fovResult}
     */
    public float[][] calculateFOV(ObjectFloatMap<Coord> viewers, int minX, int minY, int maxX, int maxY) {
        Radiance radiance;
        minX = Math.min(Math.max(minX, 0), width);
        maxX = Math.min(Math.max(maxX, 0), width);
        minY = Math.min(Math.max(minY, 0), height);
        maxY = Math.min(Math.max(maxY, 0), height);
        ArrayTools.fill(fovResult, 0f);
        ArrayTools.fill(losResult, 0f);
        for (ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
            symmetry.getFov(resistances, floatCombining, e.key.x, e.key.y, e.value, radiusStrategy);
            FOV.addFOVsInto(fovResult, floatCombining);
        }
        ArrayTools.fill(lightingStrength, 0f);
        ArrayTools.fill(colorLighting, getNeutralColor());
        final int sz = lights.size();
        float maxRange = 0, range;
        LightSource ls;
        Coord pos;
        for (int i = 0; i < sz; i++) {
            ls = lights.get(i);
            pos = ls.position;
            radiance = ls.radiance;
            if (radiance == null) continue;
            range = radiance.range;
            if (range > maxRange &&
                    pos.x + range >= minX && pos.x - range < maxX && pos.y + range >= minY && pos.y - range < maxY)
                maxRange = range;
        }
        for (ObjectFloatMap.Entry<Coord> e : viewers.entrySet()) {
            FOV.reuseLOS(resistances, floatCombining, e.key.x, e.key.y, minX, minY, maxX, maxY);
            FOV.addFOVsInto(losResult, floatCombining);
        }
        noticeable.refill(losResult, 0.0001f, Float.POSITIVE_INFINITY).expand8way((int) Math.ceil(maxRange));
        for (int i = 0; i < sz; i++) {
            ls = lights.get(i);
            pos = ls.position;
            if (!noticeable.contains(pos))
                continue;
            radiance = ls.radiance;
            if (radiance == null) continue;
            if (symmetry == SymmetryMode.SYMMETRICAL || ls.span >= 1f)
                symmetry.getFov(resistances, lightFromFOV, pos.x, pos.y, radiance.range, radiusStrategy);
            else
                FOV.reuseFOVTurns(resistances, lightFromFOV, pos.x, pos.y, radiance.range, radiusStrategy, ls.direction, ls.span);
            mixColoredLighting(radiance.flare, radiance.color);
        }
        for (int x = Math.max(0, minX); x < maxX && x < width; x++) {
            for (int y = Math.max(0, minY); y < maxY && y < height; y++) {
                if (losResult[x][y] > 0.0f) {
                    fovResult[x][y] = Math.min(Math.max(fovResult[x][y] + lightingStrength[x][y], 0), 1);
                }
            }
        }
        return fovResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightingManager that = (LightingManager) o;

        if (width != that.width) return false;
        if (height != that.height) return false;
        if (backgroundColor != that.backgroundColor) return false;
        if (Float.compare(that.viewerRange, viewerRange) != 0) return false;
        if (radiusStrategy != that.radiusStrategy) return false;
        if (!Arrays.deepEquals(resistances, that.resistances)) return false;
        if (!Arrays.deepEquals(fovResult, that.fovResult)) return false;
        if (!Arrays.deepEquals(losResult, that.losResult)) return false;
        if (!Arrays.deepEquals(lightFromFOV, that.lightFromFOV)) return false;
        if (!Arrays.deepEquals(lightingStrength, that.lightingStrength)) return false;
        if (!Arrays.deepEquals(colorLighting, that.colorLighting)) return false;
        if (!Objects.equals(lights, that.lights)) return false;
        return Objects.equals(noticeable, that.noticeable);
    }

    @Override
    public int hashCode() {
        int result = radiusStrategy != null ? radiusStrategy.hashCode() : 0;
        result = 31 * result + Hasher.eligos.hash(resistances);
        result = 31 * result + Hasher.eligos.hash(fovResult);
        result = 31 * result + Hasher.eligos.hash(losResult);
        result = 31 * result + Hasher.eligos.hash(lightFromFOV);
        result = 31 * result + Hasher.eligos.hash(lightingStrength);
        result = 31 * result + Hasher.eligos.hash(colorLighting);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + backgroundColor;
        result = 31 * result + BitConversion.floatToIntBits(viewerRange);
        result = 31 * result + (lights != null ? lights.hashCode() : 0);
        result = 31 * result + (noticeable != null ? noticeable.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "LightingManager{size:" + width + "x" + height + "}";
    }
}
