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

import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.world.BiomeMapper.BlendedBiomeMapper;

/**
 * Takes a {@link WorldMapGenerator}, such as a {@link LocalMap}, {@link RotatingGlobeMap}, or {@link StretchWorldMap},
 * and wraps it so that you can call {@link #generate()} on this to coordinate calls to
 * {@link WorldMapGenerator#generate()} and {@link BlendedBiomeMapper#makeBiomes(WorldMapGenerator)}.
 * For extra convenience, you can get a possible interpretation of how the generated world would look by calling
 * {@link #show()}, which returns a 2D array of ints as RGBA8888 colors.
 */
public class BlendedWorldMapView implements WorldMapView {
    protected int width, height;
    protected WorldMapGenerator world;
    protected BlendedBiomeMapper biomeMapper;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[][] getColorMap() {
        return biomeMapper.colorDataRgba;
    }

    public int[][] getColorMapOklab() {
        return biomeMapper.colorDataOklab;
    }

    public BlendedBiomeMapper getBiomeMapper() {
        return biomeMapper;
    }

    public void setBiomeMapper(BiomeMapper biomeMapper) {
        if(biomeMapper instanceof BlendedBiomeMapper)
            this.biomeMapper = (BlendedBiomeMapper) biomeMapper;
    }

    public WorldMapGenerator getWorld() {
        return world;
    }

    public void setWorld(WorldMapGenerator world) {
        this.world = world;
        if(this.width != world.width || this.height != world.height)
        {
            width = world.width;
            height = world.height;
        }
    }
    public BlendedWorldMapView(){
        this(123, 10, 10);
    }
    public BlendedWorldMapView(WorldMapGenerator worldMapGenerator)
    {
        world = worldMapGenerator == null ? new LocalMap() : worldMapGenerator;
        width = world.width;
        height = world.height;
        this.biomeMapper = new BlendedBiomeMapper();
        initialize();
    }

    public BlendedWorldMapView(long seed, int width, int height)
    {
        this(new LocalMap(seed, width, height));
    }

    public void initialize()
    {
        biomeMapper.initialize();
    }

    /**
     * Initializes the color tables this uses for all biomes, but allows rotating all hues and adjusting
     * brightness/saturation/contrast to produce maps of non-Earth-like planets.
     * @param hue hue rotation; 0.0 and 1.0 are no rotation, and 0.5 is maximum rotation
     * @param saturation added to the saturation of a biome color; usually close to 0.0, always between -1 and 1
     * @param brightness added to the lightness of a biome color; often close to 0.0, always between -1 and 1
     */
    public void initialize(float hue, float saturation, float brightness, float contrast)
    {
        biomeMapper.initialize(hue, saturation, brightness, contrast);
    }

    /**
     * Uses the current colors for biomes in {@link #biomeMapper} to partly-randomize themselves, and also incorporates
     * three random floats from the {@link com.github.tommyettinger.random.MizuchiRandom} stored in {@link #getWorld()}.
     * This should map similar colors in the input color table, like varieties of dark green forest, into similar output
     * colors. It will not change color 60 (empty space), but will change everything else. Typically, colors like white
     * ice will still map to white, and different shades of ocean blue will become different shades of some color (which
     * could still be some sort of blue). This can be a useful alternative to
     * {@link #initialize(float, float, float, float)}, because that method hue-rotates all colors by the same amount,
     * while this method adjusts each input hue differently and based on their original value. You may want to call
     * {@link #initialize()} (either with no arguments or with four) before each call to this, because changes this
     * makes to the color table would be read back the second time this is called without reinitialization.
     */
    public void alter()
    {
        biomeMapper.alter(world.rng);
    }

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
     * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
     * {@link DescriptiveColor#describeOklab(String)}, or might be made manually, in advanced cases, with
     * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
     * @param similarColors an array or vararg of packed int Oklab colors with at least one element
     */
    public void match(int... similarColors)
    {
        biomeMapper.match(1234567890L, similarColors);
    }

    /**
     * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
     * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
     * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
     * {@link DescriptiveColor#describeOklab(String)}, or might be made manually, in advanced cases, with
     * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
     * @param seed any long; used to determine how colors are matched to biomes
     * @param similarColors an array or vararg of packed int Oklab colors with at least one element
     */
    public void match(long seed, int... similarColors)
    {
        biomeMapper.match(seed, similarColors);
    }

    public void generate()
    {
        final long landA = Hasher.randomize2(world.seedA), landB = Hasher.randomize2(landA ^ world.seedB);
        final long heat = Hasher.randomize2(landB);
        generate(world.seedA, world.seedB, 1f + ((landA & 0xFFFFFF) + (landA >>> 40) - (landB & 0xFFFFFF) - (landB >>> 40)) * 0x1p-27f,
                (heat >>> 40) * 0x1p-24f * 0.375f + 1.0625f);
    }
    public void generate(float landMod, float heatMod)
    {
        generate(world.seedA, world.seedB, landMod, heatMod);
    }
    
    public void generate(long seedA, long seedB, float landMod, float heatMod) {
        world.generate(landMod, heatMod, seedA, seedB | 1L);
        biomeMapper.makeBiomes(world);
    }

    /**
     * After calling {@link #generate()}, which assigns values to {@link #getColorMap()} and
     * {@link #getColorMapOklab()}, you can call this to get the RGBA colorMap. This is effectively the same as
     * {@link #getColorMap()}, and you can use {@link #getColorMapOklab()} if you want Oklab colors for further mixing
     * or adjusting.
     * @return the RGBA8888 colorMap
     */
    public int[][] show()
    {
        return biomeMapper.colorDataRgba;
    }
}
