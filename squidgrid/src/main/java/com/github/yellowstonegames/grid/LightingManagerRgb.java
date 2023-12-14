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

import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;

/**
 * A convenience class that makes dealing with multiple colored light sources easier.
 * All fields are public and documented to encourage their use alongside the API methods. The typical usage case for
 * this class is when a game has complex lighting needs that should be consolidated into one LightingManager per level,
 * where a level corresponds to a {@code char[][]}. After constructing a LightingManager with the resistances for a
 * level, you should add all light sources with their positions, either using {@link #addLight(int, int, Radiance)} or
 * by directly putting keys and values into {@link #lights}. Then you can calculate the visible cells once lighting is
 * considered (which may include distant lit cells with unseen but unobstructing cells between the viewer and the light)
 * using {@link #calculateFOV(Coord)}, which should be called every time the viewer moves. You can update the flicker
 * and strobe effects on all Radiance objects, which is typically done every frame, using {@link #update()} or
 * {@link #updateAll()} (updateAll() is for when there is no viewer), and once that update() call has been made you can
 * call {@link #draw(int[][])} to change a 2D int array that holds packed int colors (such as the backgrounds in a
 * GlyphMap). To place user-interface lighting effects that don't affect the in-game-world lights, you can use
 * {@link #updateUI(Coord, Radiance)}, which is called after {@link #update()} but before {@link #draw(int[][])}.
 * <br>
 * This class uses the normal RGBA color space, like {@link DescriptiveColorRgb}, for almost every place it deals with
 * color. The exception here is {@link #drawOklab(int[][])}, which uses RGBA internally but converts the colors to OKlab
 * when it outputs them.
 * <br>
 * Honestly, this class is quite complex, and you should really take a look at a demo that uses it to see how the
 * different parts fit together. If you have the SquidSquad test sources, LightingTest in squidglyph provides a
 * relatively simple example using many colors of light. {@link VisionFramework} also can be used to handle much of the
 * boilerplate associated with vision and lighting.
 */
public class LightingManagerRgb extends LightingManager {
    /**
     * Unlikely to be used except during serialization; makes a LightingManager for a 20x20 fully visible level.
     * The viewer vision range will be 4.0f, and lights will use a circular shape.
     */
    public LightingManagerRgb()
    {
        this(new float[20][20], 0, Radius.CIRCLE, 4.0f);
    }

    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations. This will use a solid
     * black background when it casts light on cells without existing lighting. The viewer vision range will be 4.0f, and
     * lights will use a circular shape.
     * @param resistance a resistance array as produced by DungeonUtility
     */
    public LightingManagerRgb(float[][] resistance)
    {
        this(resistance, 0, Radius.CIRCLE, 4.0f);
    }
    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as a color description
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManagerRgb(float[][] resistance, String backgroundColor, Radius radiusStrategy, float viewerVisionRange)
    {
        this(resistance, DescriptiveColorRgb.describe(backgroundColor), radiusStrategy, viewerVisionRange);
    }
    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as an RGBA8888 int
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManagerRgb(float[][] resistance, int backgroundColor, Radius radiusStrategy, float viewerVisionRange) {
        this(resistance, backgroundColor, radiusStrategy, viewerVisionRange, SymmetryMode.SYMMETRICAL);
    }
    /**
     * Given a resistance array as produced by {@link FOV#generateResistances(char[][])}
     * or {@link FOV#generateSimpleResistances(char[][])}, makes a
     * LightingManager that can have {@link Radiance} objects added to it in various locations.
     * @param resistance a resistance array as produced by DungeonUtility
     * @param backgroundColor the background color to use, as an RGBA8888 int
     * @param radiusStrategy the shape lights should take, typically {@link Radius#CIRCLE} for "realistic" lights or one
     *                       of {@link Radius#DIAMOND} or {@link Radius#SQUARE} to match game rules for distance
     * @param viewerVisionRange how far the player can see without light, in cells
     */
    public LightingManagerRgb(float[][] resistance, int backgroundColor, Radius radiusStrategy, float viewerVisionRange, SymmetryMode symmetry)
    {
        super(resistance, backgroundColor, radiusStrategy, viewerVisionRange, symmetry);
    }

    /**
     * An extension point for subclasses that don't use the Oklab color space; this subclass returns
     * {@link DescriptiveColorRgb#WHITE}. There is no setter or field for the neutral color.
     * @return in this subclass, {@link DescriptiveColorRgb#WHITE}
     */
    public int getNeutralColor() {
        return DescriptiveColorRgb.WHITE;
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
     * @param backgrounds a 2D int array, which will be modified in-place; visible cells will receive RGBA8888 colors
     */
    public void draw(int[][] backgrounds)
    {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f && fovResult[x][y] > 0.0f) {
                    backgrounds[x][y] = DescriptiveColorRgb.lerpColorsBlended(backgroundColor,
                            colorLighting[x][y], lightingStrength[x][y]);
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
     * @param backgrounds a 2D int array, which will be modified in-place; visible cells will receive Oklab colors
     */
    public void drawOklab(int[][] backgrounds)
    {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (losResult[x][y] > 0.0f && fovResult[x][y] > 0.0f) {
                        backgrounds[x][y] = DescriptiveColor.lerpColorsBlended(DescriptiveColor.fromRGBA8888(backgroundColor),
                                DescriptiveColor.fromRGBA8888(colorLighting[x][y]), lightingStrength[x][y]);
                }
            }
        }
    }

    public String toString() {
        return "LightingManagerRgb{size:" + width + "x" + height + "}";
    }
}
