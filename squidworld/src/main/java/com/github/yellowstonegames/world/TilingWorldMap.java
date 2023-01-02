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

import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Noise;

/**
 * A concrete implementation of {@link WorldMapGenerator} that tiles both east-to-west and north-to-south. It tends
 * to not appear distorted like {@link StretchWorldMap} does in some areas, even though this is inaccurate for a
 * rectangular projection of a spherical world (that inaccuracy is likely what players expect in a map, though).
 * The map this makes wraps toroidally, which matches the behavior in many older games, but nothing in the real world.
 * You may want LocalMap instead, for non-world maps that don't tile.
 * <a href="http://yellowstonegames.github.io/SquidLib/DetailedWorldMapDemo.png" >Example map</a>.
 */
public class TilingWorldMap extends WorldMapGenerator {

    protected static final float terrainFreq = 0.95f, terrainRidgedFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    private float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final Noise terrain, terrainRidged, heat, moisture, otherRidged;

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
     * as north-to-south. Always makes a 256x256 map.
     * Uses Noise as its noise generator, with 1.0f as the octave multiplier affecting detail.
     * If you were using {@link TilingWorldMap#TilingWorldMap(long, int, int, Noise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256, WorldMapGenerator.DEFAULT_NOISE, 1.0f}.
     */
    public TilingWorldMap() {
        this(0x1337BABE1337D00DL, 256, 256, WorldMapGenerator.DEFAULT_NOISE, 1.0f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
     * as north-to-south.
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1.0f as the octave multiplier affecting detail.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    public TilingWorldMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1.0f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
     * as north-to-south.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1.0f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public TilingWorldMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
     * as north-to-south. Takes an initial seed, the width/height of the map, and a noise generator (a
     * {@link Noise} implementation, which is usually {@link Noise#instance}. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call
     * {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact, but you can zoom
     * in. Any seed supplied to the Noise given to this (if it takes one) will be ignored, and
     * {@link Noise#getNoiseWithSeed(float, float, float, float, int)} will be used to specify the seed many
     * times. The detail level, which is the {@code octaveMultiplier} parameter that can be passed to another
     * constructor, is always 1.0f with this constructor.
     *
     * @param initialSeed    the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 4D noise, recommended to be {@link Noise#instance}
     */
    public TilingWorldMap(long initialSeed, int mapWidth, int mapHeight, final Noise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
     * as north-to-south. Takes an initial seed, the width/height of the map, and parameters for noise
     * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
     * multiplier on how many octaves of noise to use, with 1.0f being normal (high) detail and higher multipliers
     * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
     * cannot be changed after the fact, but you can zoom in. Any seed supplied to the Noise given to this will be
     * ignored, and {@link Noise#getNoiseWithSeed(float, float, float, float, int)} will be used to specify the seed
     * many times. The {@code octaveMultiplier} parameter should probably be no lower than 0.5f, but can be arbitrarily
     * high if you're willing to spend much more time on generating detail only noticeable at very high zoom;
     * normally 1.0f is fine and may even be too high for maps that don't require zooming.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 4D noise, almost always {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1.0f normal
     */
    public TilingWorldMap(long initialSeed, int mapWidth, int mapHeight, final Noise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        terrain = new Noise(noiseGenerator);
        terrain.setFrequency(terrain.getFrequency() * terrainFreq);
        terrain.setNoiseType(terrain.getNoiseType() | 1);
        terrain.setFractalOctaves((int) (0.5f + octaveMultiplier * 8));
        terrain.setFractalLacunarity(0.5f);
        terrain.setFractalGain(2f);

        terrainRidged = new Noise(noiseGenerator);
        terrainRidged.setFrequency(terrainRidged.getFrequency() * terrainRidgedFreq);
        terrainRidged.setNoiseType(terrainRidged.getNoiseType() | 1);
        terrainRidged.setFractalOctaves((int) (0.5f + octaveMultiplier * 10));
        terrainRidged.setFractalType(Noise.RIDGED_MULTI);

        heat = new Noise(noiseGenerator);
        heat.setFrequency(heat.getFrequency() * heatFreq);
        heat.setNoiseType(heat.getNoiseType() | 1);
        heat.setFractalOctaves((int) (0.5f + octaveMultiplier * 3));
        heat.setFractalLacunarity(0.5f);
        heat.setFractalGain(2f);

        moisture = new Noise(noiseGenerator);
        moisture.setFrequency(moisture.getFrequency() * moistureFreq);
        moisture.setNoiseType(moisture.getNoiseType() | 1);
        moisture.setFractalOctaves((int) (0.5f + octaveMultiplier * 4));
        moisture.setFractalLacunarity(0.5f);
        moisture.setFractalGain(2f);

        otherRidged = new Noise(noiseGenerator);
        otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
        otherRidged.setNoiseType(otherRidged.getNoiseType() | 1);
        otherRidged.setFractalOctaves((int) (0.5f + octaveMultiplier * 6));
        otherRidged.setFractalType(Noise.RIDGED_MULTI);
    }

    /**
     * Copies the TilingWorldMap {@code other} to construct a new one that is exactly the same. References will only be
     * shared to Noise classes.
     *
     * @param other a TilingWorldMap to copy
     */
    public TilingWorldMap(TilingWorldMap other) {
        super(other);
        terrain = other.terrain;
        terrainRidged = other.terrainRidged;
        heat = other.heat;
        moisture = other.moisture;
        otherRidged = other.otherRidged;
        minHeat0 = other.minHeat0;
        maxHeat0 = other.maxHeat0;
        minHeat1 = other.minHeat1;
        maxHeat1 = other.maxHeat1;
        minWet0 = other.minWet0;
        maxWet0 = other.maxWet0;
    }

    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              float landMod, float heatMod, long stateA, long stateB) {
        boolean fresh = false;
        if (cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier) {
            minHeight = Float.POSITIVE_INFINITY;
            maxHeight = Float.NEGATIVE_INFINITY;
            minHeat0 = Float.POSITIVE_INFINITY;
            maxHeat0 = Float.NEGATIVE_INFINITY;
            minHeat1 = Float.POSITIVE_INFINITY;
            maxHeat1 = Float.NEGATIVE_INFINITY;
            minHeat = Float.POSITIVE_INFINITY;
            maxHeat = Float.NEGATIVE_INFINITY;
            minWet0 = Float.POSITIVE_INFINITY;
            maxWet0 = Float.NEGATIVE_INFINITY;
            minWet = Float.POSITIVE_INFINITY;
            maxWet = Float.NEGATIVE_INFINITY;
            cacheA = stateA;
            cacheB = stateB;
            fresh = true;
        }
        rng.setState(stateA, stateB);
        long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
        int t;

        landModifier = (landMod <= 0) ? rng.nextFloat(0.1875f) + 0.99f : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

        float p, q,
                ps, pc,
                qs, qc,
                h, temp,
                i_w = 6.283185307179586f / width, i_h = 6.283185307179586f / height,
                xPos = startX, yPos = startY, i_uw = usedWidth / (float) width, i_uh = usedHeight / (float) height;
        float[] trigTable = new float[width << 1];
        for (int x = 0; x < width; x++, xPos += i_uw) {
            p = xPos * i_w;
            trigTable[x << 1] = TrigTools.sin(p);
            trigTable[x << 1 | 1] = TrigTools.cos(p);
        }
        for (int y = 0; y < height; y++, yPos += i_uh) {
            q = yPos * i_h;
            qs = TrigTools.sin(q);
            qc = TrigTools.cos(q);
            for (int x = 0, xt = 0; x < width; x++) {
                ps = trigTable[xt++];//TrigTools.sin(p);
                pc = trigTable[xt++];//TrigTools.cos(p);
                heightData[x][y] = (h = terrain.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB - seedA) * 0.25f,
                        ps, qc, qs, seedA) + landModifier - 1.0f);
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qc
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qc, qs
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC + seedA)
                        , seedC));
                minHeightActual = Math.min(minHeightActual, h);
                maxHeightActual = Math.max(maxHeightActual, h);
                if (fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet0 = Math.min(minWet0, temp);
                    maxWet0 = Math.max(maxWet0, temp);

                }
            }
            minHeightActual = Math.min(minHeightActual, minHeight);
            maxHeightActual = Math.max(maxHeightActual, maxHeight);

        }
        float
                heatDiff = 0.8f / (maxHeat0 - minHeat0),
                wetDiff = 1.0f / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5f, i_half = 1.0f / halfHeight;
        yPos = startY;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4f - temp);
            temp = 2.2f - temp;
            for (int x = 0; x < width; x++) {
                h = heightData[x][y];
                heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1.0f;
                switch (t) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        h = 0.4f;
                        hMod = 0.2f;
                        break;
                    case 6:
                        h = -0.1f * (h - forestLower - 0.08f);
                        break;
                    case 7:
                        h *= -0.25f;
                        break;
                    case 8:
                        h *= -0.4f;
                        break;
                    default:
                        h *= 0.05f;
                }
                heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6f) * temp);
                if (fresh) {
                    ps = Math.min(ps, h); //minHeat0
                    pc = Math.max(pc, h); //maxHeat0
                }
            }
        }
        if (fresh) {
            minHeat1 = ps;
            maxHeat1 = pc;
        }
        heatDiff = heatModifier / (maxHeat1 - minHeat1);
        qs = Float.POSITIVE_INFINITY;
        qc = Float.NEGATIVE_INFINITY;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                heatData[x][y] = (h = ((heatData[x][y] - minHeat1) * heatDiff));
                moistureData[x][y] = (temp = (moistureData[x][y] - minWet0) * wetDiff);
                if (fresh) {
                    qs = Math.min(qs, h);
                    qc = Math.max(qc, h);
                    ps = Math.min(ps, temp);
                    pc = Math.max(pc, temp);
                }
            }
        }
        if (fresh) {
            minHeat = qs;
            maxHeat = qc;
            minWet = ps;
            maxWet = pc;
        }
        landData.refill(heightCodeData, 4, 999);
    }
}
