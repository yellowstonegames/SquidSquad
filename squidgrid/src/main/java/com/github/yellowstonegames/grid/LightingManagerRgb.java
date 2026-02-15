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

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Arrays;
import java.util.List;

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
public class LightingManagerRgb extends LightingManager implements ISerializersNeeded {
    /**
     * Unlikely to be used except during serialization; makes a LightingManager for a 20x20 fully visible level.
     * The viewer vision range will be 4.0f, and lights will use a circular shape.
     */
    public LightingManagerRgb()
    {
        this(generateDefaultFloatArray(), 0, Radius.CIRCLE, 4.0f);
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
     * Edits {@link #colorLighting} by adding in and mixing the colors in {@link #fovLightColors}, with the strength
     * of light in fovLightColors boosted by flare (which can be any finite float greater than -1f, but is usually
     * from 0f to 1f when increasing strength). The strengths of each colored light is determined by
     * {@link #lightFromFOV} and the colors of lights are determined by {@link #fovLightColors}. If a color of light is
     * fully transparent, this skips that light.
     * <br>
     * This is very limited-use; the related method {@link #mixColoredLighting(float, int)} is used as part of
     * {@link #update()}, but this method is meant for when multiple colors of FOV light need to be mixed at once.
     * @param flare boosts the effective strength of lighting in {@link #fovLightColors}; usually from 0 to 1
     */
    public void mixColoredLighting(float flare)
    {
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
                        if(os > 0f) o = other[x][y];
                        else continue;
                    } else {
                        os = otherStrength[x][y];
                        o = other[x][y];
                    }
                    if (os <= 0f || (o & 0xFE) == 0)
                        continue;
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFFFFFF00) == 0xFFFFFF00) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFFFFFF00) != 0xFFFFFF00) {
                            final int
                                    sR = (b >>> 24), sG = (b >>> 16) & 0xFF, sB = (b >>> 8) & 0xFF, sAlpha = b & 0xFE,
                                    eR = (o >>> 24), eG = (o >>> 16) & 0xFF, eB = (o >>> 8) & 0xFF, eAlpha = o & 0xFE;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * (1f/254f);
                            basis[x][y] = (((int) (sR + change * (eR - sR)) & 0xFF) << 24
                                    | ((int) (sG + change * (eG - sG)) & 0xFF) << 16
                                    | ((int) (sB + change * (eB - sB)) & 0xFF) << 8
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
     * @param flare boosts the effective strength of lighting in {@link #lightFromFOV}; usually from 0 to 1
     * @param color the RGBA8888 int color to mix in where the light strength in {@link #lightFromFOV} is greater than 0
     */
    @Override
    public void mixColoredLighting(float flare, int color)
    {
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
                        if(os > 0f) o = color;
                        else continue;
                    } else {
                        if((os = otherStrength[x][y]) != 0) o = color;
                        else continue;
                    }
                    bs = basisStrength[x][y];
                    b = basis[x][y];
                    if ((b & 0xFFFFFF00) == 0xFFFFFF00) {
                        basis[x][y] = o;
                        basisStrength[x][y] = Math.min(1.0f, bs + os * flare);
                    } else {
                        if ((o & 0xFFFFFF00) != 0xFFFFFF00) {
                            final int
                                    sR = (b >>> 24), sG = (b >>> 16) & 0xFF, sB = (b >>> 8) & 0xFF, sAlpha = b & 0xFE,
                                    eR = (o >>> 24), eG = (o >>> 16) & 0xFF, eB = (o >>> 8) & 0xFF, eAlpha = o & 0xFE;
                            final float change = ((os - bs) * 0.5f + 0.5f) * eAlpha * (1f/254f);
                            basis[x][y] = (((int) (sR + change * (eR - sR)) & 0xFF) << 24
                                    | ((int) (sG + change * (eG - sG)) & 0xFF) << 16
                                    | ((int) (sB + change * (eB - sB)) & 0xFF) << 8
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

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Arrays.asList(int[].class, int[][].class, float[].class, float[][].class, Coord.class, Region.class,
                ObjectDeque.class, Radiance.class, LightSource.class);
    }
}
