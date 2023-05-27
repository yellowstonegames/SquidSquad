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

package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.world.BiomeMapper.*;

/**
 * Takes a {@link WorldMapGenerator}, such as a {@link LocalMap}, {@link RotatingGlobeMap}, or {@link StretchWorldMap},
 * and wraps it so that you can call {@link #generate()} on this to coordinate calls to
 * {@link WorldMapGenerator#generate()} and {@link BiomeMapper#makeBiomes(WorldMapGenerator)}.
 * For extra convenience, you can get a possible interpretation of how the generated world would look by calling
 * {@link #show()}, which returns a 2D array of ints as RGBA8888 colors.
 * <br>
 * The main implementations of this so far are {@link DetailedWorldMapView} and {@link BlendedWorldMapView}, which use
 * a {@link DetailedBiomeMapper} and a {@link BlendedBiomeMapper}, respectively.
 */
public interface WorldMapView {
    int getWidth();

    int getHeight();

    int[][] getColorMap();

    int[][] getColorMapOklab();

    BiomeMapper getBiomeMapper();

    void setBiomeMapper(BiomeMapper biomeMapper);

    WorldMapGenerator getWorld();

    void setWorld(WorldMapGenerator world);

    void initialize();

    /**
     * Initializes the color tables this uses for all biomes, but allows rotating all hues and adjusting
     * brightness/saturation/contrast to produce maps of non-Earth-like planets.
     *
     * @param hue        hue rotation; 0.0 and 1.0 are no rotation, and 0.5 is maximum rotation
     * @param saturation added to the saturation of a biome color; usually close to 0.0, always between -1 and 1
     * @param brightness added to the lightness of a biome color; often close to 0.0, always between -1 and 1
     * @param contrast   multiplies the darkening factor for the dark sections of biomes; typically very close to 1
     */
    void initialize(float hue, float saturation, float brightness, float contrast);

    /**
     * Partly-randomizes the colors used for biomes. This may request random numbers from the MizuchiRandom in
     * {@link #getWorld() the WorldMapGenerator}.
     * This should map similar colors in the input color table, like varieties of dark green forest, into similar output
     * colors. It will not change color 60 (empty space), but will change everything else. Typically, colors like white
     * ice will still map to white, and different shades of ocean blue will become different shades of some color (which
     * could still be some sort of blue). This can be a useful alternative to
     * {@link #initialize(float, float, float, float)}, because that method hue-rotates all colors by the same amount,
     * while this method adjusts each input hue differently and based on their original value. You may want to call
     * {@link #initialize()} (either with no arguments or with four) before each call to this, because changes this
     * makes to the color table would be read back the second time this is called without reinitialization.
     */
    void alter();

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
     * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
     * {@link DescriptiveColor#describeOklab(CharSequence)}, or might be made manually, in advanced cases, with
     * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
     *
     * @param similarColors an array or vararg of packed int Oklab colors with at least one element
     */
    void match(int... similarColors);

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
     * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
     * {@link DescriptiveColor#describeOklab(CharSequence)}, or might be made manually, in advanced cases, with
     * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
     *
     * @param seed          any long; used to determine how colors are matched to biomes
     * @param similarColors an array or vararg of packed int Oklab colors with at least one element
     */
    void match(long seed, int... similarColors);

    void generate();

    void generate(float landMod, float heatMod);

    void generate(long seedA, long seedB, float landMod, float heatMod);

    /**
     * After calling {@link #generate()}, you can call this to assign values to {@link #getColorMap()} and
     * {@link #getColorMapOklab()}. This method returns the RGBA colorMap, but it assigns to colorMapOklab
     * at the same time, so you can use the Oklab colors with methods like
     * {@link DescriptiveColor#lighten(int, float)} and {@link DescriptiveColor#lerpColors(int, int, float)}.
     *
     * @return the RGBA8888 colorMap
     */
    int[][] show();
}
