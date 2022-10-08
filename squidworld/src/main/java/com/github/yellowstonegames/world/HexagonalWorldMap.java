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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.grid.Noise;

import java.util.Arrays;

/**
 * A modified version of the Eckert II projection, shaped like an elongated hexagon. This tries to keep the angles
 * of a regular hexagon, so it could be used for a map with a hexagonal grid; Eckert II uses different angles from
 * a regular hexagon. It looks best when its width is twice its height.
 */
@Beta
public class HexagonalWorldMap extends WorldMapGenerator {
    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final Noise terrain, heat, moisture, otherRidged, terrainLayered;
    public final float[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;


    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Always makes a 200x100 map.
     * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     * If you were using {@link HexagonalWorldMap#HexagonalWorldMap(long, int, int, Noise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0}.
     */
    public HexagonalWorldMap() {
        this(0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    public HexagonalWorldMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses FastNoise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public HexagonalWorldMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses FastNoise as its noise generator, with the given octave multiplier affecting detail.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
     */
    public HexagonalWorldMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise3D
     * implementation to use is {@link Noise#instance}
     *
     * @param initialSeed    the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator a {@link Noise} instance
     */
    public HexagonalWorldMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
     * {@link Noise} instance, and a
     * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
     * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
     * cannot be changed after the fact, but you can zoom in. FastNoise will be the fastest 3D generator to use for
     * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
     * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
     * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
     * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
     * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
     * that don't require zooming.
     *
     * @param initialSeed      the seed for the MizuchiRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise
     * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
     */
    public HexagonalWorldMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];
        edges = new int[height << 1];

        terrain = new Noise(noiseGenerator);
        terrain.setFrequency(terrain.getFrequency() * terrainFreq);
        terrain.setNoiseType(terrain.getNoiseType() | 1);
        terrain.setFractalOctaves((int) (0.5f + octaveMultiplier * 10));
        terrain.setFractalType(Noise.RIDGED_MULTI);

        terrainLayered = new Noise(noiseGenerator);
        terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
        terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1);
        terrainLayered.setFractalOctaves((int) (0.5f + octaveMultiplier * 8));

        heat = new Noise(noiseGenerator);
        heat.setFrequency(heat.getFrequency() * heatFreq);
        heat.setNoiseType(heat.getNoiseType() | 1);
        heat.setFractalOctaves((int) (0.5f + octaveMultiplier * 3));

        moisture = new Noise(noiseGenerator);
        moisture.setFrequency(moisture.getFrequency() * moistureFreq);
        moisture.setNoiseType(moisture.getNoiseType() | 1);
        moisture.setFractalOctaves((int) (0.5f + octaveMultiplier * 4));

        otherRidged = new Noise(noiseGenerator);
        otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
        otherRidged.setNoiseType(otherRidged.getNoiseType() | 1);
        otherRidged.setFractalOctaves((int) (0.5f + octaveMultiplier * 6));
        otherRidged.setFractalType(Noise.RIDGED_MULTI);
    }

    /**
     * Copies the HexagonalMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a HexagonalMap to copy
     */
    public HexagonalWorldMap(HexagonalWorldMap other) {
        super(other);
        terrain = other.terrain;
        terrainLayered = other.terrainLayered;
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
    public int wrapX(final int x, int y) {
        y = Math.max(0, Math.min(y, height - 1));
        if (x < edges[y << 1])
            return edges[y << 1 | 1];
        else if (x > edges[y << 1 | 1])
            return edges[y << 1];
        else return x;
    }

    @Override
    public int wrapY(final int x, final int y) {
        return Math.max(0, Math.min(y, height - 1));
    }

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
                h, temp, yPos, xPos,
                i_uw = usedWidth / (float) width,
                i_uh = usedHeight / (float) height,
                th, thb, thx, thy, lon, lat,
                rx = width * (0.25f) - 0.5f, irx = 1.5f / rx, hw = width * 0.5f,
                ry = height * (0.5f) - 0.5f, iry = 1f / ry;

        yPos = startY - ry;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            thy = yPos * iry;
            thb = 2 - Math.abs(thy) / 1.4472025091165353f;
            thx = 2 - Math.abs(thy) * (0.75f / 1.4472025091165353f);
            //1.4472025091165353 == Math.sqrt(2 * 3.14159265358979323846 / 3);
            lon = 4.3416075273496055f / (thx + thx);
            //4.3416075273496055 == Math.sqrt(6.0 * 3.14159265358979323846)
            qs = Math.signum(thy) * (4f - thb * thb) * (1f / 3f);
            lat = TrigTools.asin(qs);

            qc = TrigTools.cos(lat);

            boolean inSpace = true;
            xPos = startX - hw;
            for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                th = lon * xPos * irx;
                if (th < -3.141592653589793 || th > 3.141592653589793) {
                    heightCodeData[x][y] = 10000;
                    inSpace = true;
                    continue;
                }
                if (inSpace) {
                    inSpace = false;
                    edges[y << 1] = x;
                }
                edges[y << 1 | 1] = x;
                th += centerLongitude;
                ps = TrigTools.sin(th) * qc;
                pc = TrigTools.cos(th) * qc;
                xPositions[x][y] = pc;
                yPositions[x][y] = ps;
                zPositions[x][y] = qs;
                heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                terrain.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                        ps, qs, seedA) + landModifier - 1f);
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
