/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.world.i;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.NoiseWrapper;
import com.github.yellowstonegames.world.RotatingGlobeMap;
import com.github.yellowstonegames.world.WorldMapGenerator;

import java.util.Arrays;

/**
 * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
 * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). This is meant for
 * single-image views of planets as if viewing a globe from its side. This uses an
 * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
 * the latitude always at the equator. If you want to rotate a globe in real-time, consider {@link RotatingGlobeMap}
 * instead; rotating a SpaceViewWorldMap can be done with {@link #setCenterLongitude(float)}, but it is somewhat slow.
 * <a href="http://yellowstonegames.github.io/SquidLib/SpaceViewMap.png" >Example map, showing circular shape as if
 * viewed from afar</a>
 */
public class GlobeMap extends WorldMapGenerator {

    protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
//    protected static final float terrainFreq = 2.2f, terrainLayeredFreq = 1.8f, heatFreq = 2.2f, moistureFreq = 2.3f, otherFreq = 4.6f;
//    protected static final float terrainFreq = 2.2f, terrainLayeredFreq = 1.6f, heatFreq = 3.7f, moistureFreq = 3.9f, otherFreq = 4.8f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final NoiseWrapper terrainRidged, heat, moisture, otherRidged, terrainBasic;
    public final float[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Always makes a 100x100 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link GlobeMap#GlobeMap(long, int, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1f}.
     */
    public GlobeMap() {
        this(0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    public GlobeMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses the given noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed    the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed, the width/height of the map, and parameters for noise
     * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
     * multiplier on how many octaves of noise to use, with 1f being normal (high) detail and higher multipliers
     * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
     * cannot be changed after the fact, but you can zoom in. Noise will be the fastest 3D generator to use for
     * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
     * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
     * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
     * probably be no lower than 0.5f, but can be arbitrarily high if you're willing to spend much more time on
     * generating detail only noticeable at very high zoom; normally 1f is fine and may even be too high for maps
     * that don't require zooming.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];
        edges = new int[height << 1];

        terrainRidged = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 8));
        terrainBasic = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainLayeredFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 3));
        heat = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), heatFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 2));
        moisture = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), moistureFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 2));
        otherRidged = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), otherFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 5));
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed, the width/height of the map, and parameters for noise
     * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
     * multiplier on how many octaves of noise to use, with 1f being normal (high) detail and higher multipliers
     * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
     * cannot be changed after the fact, but you can zoom in. Noise will be the fastest 3D generator to use for
     * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
     * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
     * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
     * probably be no lower than 0.5f, but can be arbitrarily high if you're willing to spend much more time on
     * generating detail only noticeable at very high zoom; normally 1f is fine and may even be too high for maps
     * that don't require zooming.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param terrainRidgedNoise a Noise generator (or subclass) that will be used to generate ridged terrain
     * @param terrainBasicNoise a Noise generator (or subclass) that will be used to generate general terrain
     * @param heatNoise a Noise generator (or subclass) that will be used to generate heat values
     * @param moistureNoise a Noise generator (or subclass) that will be used to generate moisture values
     * @param otherRidgedNoise a Noise generator (or subclass) that will be used to generate ridges
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, INoise terrainRidgedNoise, INoise terrainBasicNoise,
                    INoise heatNoise, INoise moistureNoise, INoise otherRidgedNoise, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];
        edges = new int[height << 1];

        this.terrainRidged = new NoiseWrapper(terrainRidgedNoise, terrainRidgedNoise.getSeed(), terrainFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 8));
        this.terrainBasic = new NoiseWrapper(terrainBasicNoise, terrainBasicNoise.getSeed(), terrainLayeredFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 3));
        this.heat = new NoiseWrapper(heatNoise, heatNoise.getSeed(), heatFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 2));
        this.moisture = new NoiseWrapper(moistureNoise, moistureNoise.getSeed(), moistureFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 2));
        this.otherRidged = new NoiseWrapper(otherRidgedNoise, otherRidgedNoise.getSeed(), otherFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 5f));
    }

    /**
     * Copies the SpaceViewWorldMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a SpaceViewWorldMap to copy
     */
    public GlobeMap(GlobeMap other) {
        super(other);
        terrainRidged = other.terrainRidged;
        terrainBasic = other.terrainBasic;
        heat = other.heat;
        moisture = other.moisture;
        otherRidged = other.otherRidged;
        minHeat0 = other.minHeat0;
        maxHeat0 = other.maxHeat0;
        minHeat1 = other.minHeat1;
        maxHeat1 = other.maxHeat1;
        minWet0 = other.minWet0;
        maxWet0 = other.maxWet0;
        xPositions = ArrayTools.copy(other.xPositions);
        yPositions = ArrayTools.copy(other.yPositions);
        zPositions = ArrayTools.copy(other.zPositions);
        edges = Arrays.copyOf(other.edges, other.edges.length);
    }

    @Override
    public int wrapX(int x, int y) {
        y = Math.max(0, Math.min(y, height - 1));
        return Math.max(edges[y << 1], Math.min(x, edges[y << 1 | 1]));
    }

    @Override
    public int wrapY(final int x, final int y) {
        return Math.max(0, Math.min(y, height - 1));
    }

    //private static final float root2 = Math.sqrt(2f), inverseRoot2 = 1f / root2, halfInverseRoot2 = 0.5f / root2;

    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              float landMod, float heatMod, long stateA, long stateB) {
        boolean fresh = false;
        if (cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier) {
            minHeight = Float.POSITIVE_INFINITY;
            maxHeight = Float.NEGATIVE_INFINITY;
            minHeightActual = Float.POSITIVE_INFINITY;
            maxHeightActual = Float.NEGATIVE_INFINITY;
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

        landModifier = (landMod <= 0) ? rng.nextFloat(0.2f) + 0.91f : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

        float p,
                ps, pc,
                qs, qc,
                h, temp, yPos, xPos, iyPos, ixPos,
                i_uw = usedWidth / (float) width,
                i_uh = usedHeight / (float) height,
                th, lon, lat, rho,
                rx = width * 0.5f - 0.5f, irx = i_uw / rx,
                ry = height * 0.5f - 0.5f, iry = i_uh / ry;

        yPos = startY - ry;
        iyPos = yPos / ry;
        for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {

            boolean inSpace = true;
            xPos = startX - rx;
            ixPos = xPos / rx;
            for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                rho = (float) Math.sqrt(ixPos * ixPos + iyPos * iyPos);
                if (rho > 1f) {
                    heightCodeData[x][y] = 10000;
                    inSpace = true;
                    continue;
                }
                if (inSpace) {
                    inSpace = false;
                    edges[y << 1] = x;
                }
                edges[y << 1 | 1] = x;
                th = TrigTools.asin(rho); // c
                lat = TrigTools.asin(iyPos);
                lon = centerLongitude + TrigTools.atan2(ixPos * rho, rho * TrigTools.cos(th));

                qc = TrigTools.cos(lat);
                qs = TrigTools.sin(lat);

                pc = TrigTools.cos(lon) * qc;
                ps = TrigTools.sin(lon) * qc;

                xPositions[x][y] = pc;
                yPositions[x][y] = ps;
                zPositions[x][y] = qs;
                heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                        ps, qs, seedA) + landModifier - 1f);
//                    heightData[x][y] = (h = terrain4D.getNoiseWithSeed(pc, ps, qs,
//                            (terrainLayered.getNoiseWithSeed(pc, ps, qs, seedB - seedA)
//                                    + terrain.getNoiseWithSeed(pc, ps, qs, seedC - seedB)) * 0.5f,
//                            seedA) * landModifier);
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
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
        float heatDiff = 0.8f / (maxHeat0 - minHeat0),
                wetDiff = 1f / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5f, i_half = 1f / halfHeight;
        yPos = startY + i_uh;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4f - temp);
            temp = 2.2f - temp;
            for (int x = 0; x < width; x++) {
                h = heightData[x][y];
                if (heightCodeData[x][y] == 10000) {
                    heightCodeData[x][y] = 1000;
                    continue;
                } else
                    heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1f;
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
