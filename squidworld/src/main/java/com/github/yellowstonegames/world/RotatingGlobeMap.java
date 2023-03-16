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

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.*;

/**
 * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
 * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). It should
 * look as a world would when viewed from space, or as a globe would from its side, and implements rotation differently
 * to allow the planet to be rotated without recalculating all the data. Note that calling
 * {@link #setCenterLongitude(float)} does a lot more work than in other classes, but less than fully calling
 * {@link #generate()} in those classes, since it doesn't remake the map data at a slightly different rotation and
 * instead keeps a single map in use the whole time, using sections of it. This uses an
 * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
 * the latitude always at the equator; the internal map is stored as a {@link StretchWorldMap}, which uses a
 * <a href="https://en.wikipedia.org/wiki/Cylindrical_equal-area_projection#Discussion">cylindrical equal-area
 * projection</a>, specifically the Lambert equal-area projection.
 * <br>
 * <a href="https://i.imgur.com/WNa5nQ1.gifv">Example view of a planet rotating</a>.
 * <a href="https://i.imgur.com/NV5IMd6.gifv">Another example</a>.
 */
public class RotatingGlobeMap extends WorldMapGenerator {

    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final float[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;
    public final StretchWorldMap storedMap;

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Always makes a 100x100 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link RotatingGlobeMap#RotatingGlobeMap(long, int, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, new Noise(DEFAULT_NOISE), 1f}.
     */
    public RotatingGlobeMap() {
        this(0x1337BABE1337D00DL, 100, 100, new Noise(DEFAULT_NOISE), 1f);
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
    public RotatingGlobeMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
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
    public RotatingGlobeMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
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
    public RotatingGlobeMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), octaveMultiplier);
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
    public RotatingGlobeMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Takes an initial seed, the width/height of the map, and parameters for noise
     * generation (an {@link INoise} implementation, which is usually {@link Noise#instance}, and a
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
    public RotatingGlobeMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[mapWidth][mapHeight];
        yPositions = new float[mapWidth][mapHeight];
        zPositions = new float[mapWidth][mapHeight];
        edges = new int[height << 1];
        storedMap = new StretchWorldMap(initialSeed, mapWidth << 1, mapHeight, noiseGenerator, octaveMultiplier);
    }

    /**
     * Copies the RotatingGlobeMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to INoise classes.
     *
     * @param other a RotatingGlobeMap to copy
     */
    public RotatingGlobeMap(RotatingGlobeMap other) {
        super(other);
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
        storedMap = new StretchWorldMap(other.storedMap);
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

    @Override
    public void setCenterLongitude(float centerLongitude) {
        super.setCenterLongitude(centerLongitude);
        int ax, ay;
        float
                ps, pc,
                qs, qc,
                h, yPos, xPos, iyPos, ixPos,
                i_uw = usedWidth / (float) width,
                i_uh = usedHeight / (float) height,
                th, lon, lat, rho,
                i_pi = 1f / PI,
                rx = width * 0.5f, irx = i_uw / rx,
                ry = height * 0.5f, iry = i_uh / ry;

        yPos = startY - ry;
        iyPos = yPos / ry;
        for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {
            boolean inSpace = true;
            xPos = startX - rx;
            ixPos = xPos / rx;
            lat = TrigTools.asin(iyPos);
            for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                rho = (ixPos * ixPos + iyPos * iyPos);
                if (rho > 1f) {
                    heightCodeData[x][y] = 1000;
                    inSpace = true;
                    continue;
                }
                rho = (float) Math.sqrt(rho);
                if (inSpace) {
                    inSpace = false;
                    edges[y << 1] = x;
                }
                edges[y << 1 | 1] = x;
                th = TrigTools.asin(rho); // c
                lon = removeExcess((centerLongitude + (atan2(ixPos * rho, rho * cos(th)))) * 0.5f);

                qs = lat * 0.6366197723675814f;
                qc = qs + 1f;
                int sf = (qs >= 0f ? (int) qs : (int) qs - 1) & -2;
                int cf = (qc >= 0f ? (int) qc : (int) qc - 1) & -2;
                qs -= sf;
                qc -= cf;
                qs *= 2f - qs;
                qc *= 2f - qc;
                qs = qs * (-0.775f - 0.225f * qs) * ((sf & 2) - 1);
                qc = qc * (-0.775f - 0.225f * qc) * ((cf & 2) - 1);


                ps = lon * 0.6366197723675814f;
                pc = ps + 1f;
                sf = (ps >= 0f ? (int) ps : (int) ps - 1) & -2;
                cf = (pc >= 0f ? (int) pc : (int) pc - 1) & -2;
                ps -= sf;
                pc -= cf;
                ps *= 2f - ps;
                pc *= 2f - pc;
                ps = ps * (-0.775f - 0.225f * ps) * ((sf & 2) - 1);
                pc = pc * (-0.775f - 0.225f * pc) * ((cf & 2) - 1);

                ax = (int) ((lon * i_pi + 1f) * width);
                ay = (int) ((qs + 1f) * ry);

//                    // Hammer projection, not an inverse projection like we usually use
//                    z = 1f / Math.sqrt(1 + qc * TrigTools.cos(lon * 0.5f));
//                    ax = (int)((qc * TrigTools.sin(lon * 0.5f) * z + 1f) * width);
//                    ay = (int)((qs * z + 1f) * height * 0.5f);

                if (ax >= storedMap.width || ax < 0 || ay >= storedMap.height || ay < 0) {
                    heightCodeData[x][y] = 1000;
                    continue;
                }
                if (storedMap.heightCodeData[ax][ay] >= 1000) // for the seam we get when looping around
                {
                    ay = storedMap.wrapY(ax, ay);
                    ax = storedMap.wrapX(ax, ay);
                }

                xPositions[x][y] = pc * qc;
                yPositions[x][y] = ps * qc;
                zPositions[x][y] = qs;

                heightData[x][y] = h = storedMap.heightData[ax][ay];
                heightCodeData[x][y] = codeHeight(h);
                heatData[x][y] = storedMap.heatData[ax][ay];
                moistureData[x][y] = storedMap.moistureData[ax][ay];

                minHeightActual = Math.min(minHeightActual, h);
                maxHeightActual = Math.max(maxHeightActual, h);
            }
            minHeightActual = Math.min(minHeightActual, minHeight);
            maxHeightActual = Math.max(maxHeightActual, maxHeight);
        }

    }

    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              float landMod, float heatMod, long stateA, long stateB) {
        if (cacheA != stateA || cacheB != stateB)// || landMod != storedMap.landModifier || coolMod != storedMap.coolingModifier)
        {
            storedMap.regenerate(0, 0, width << 1, height, landMod, heatMod, stateA, stateB);
            minHeightActual = Float.POSITIVE_INFINITY;
            maxHeightActual = Float.NEGATIVE_INFINITY;

            minHeight = storedMap.minHeight;
            maxHeight = storedMap.maxHeight;

            minHeat0 = storedMap.minHeat0;
            maxHeat0 = storedMap.maxHeat0;

            minHeat1 = storedMap.minHeat1;
            maxHeat1 = storedMap.maxHeat1;

            minWet0 = storedMap.minWet0;
            maxWet0 = storedMap.maxWet0;

            minHeat = storedMap.minHeat;
            maxHeat = storedMap.maxHeat;

            minWet = storedMap.minWet;
            maxWet = storedMap.maxWet;

            cacheA = stateA;
            cacheB = stateB;
        }
        setCenterLongitude(centerLongitude);
        landData.refill(heightCodeData, 4, 999);
    }

//    /**
//     * This is rather bad! Only use for tests.
//     * @param y
//     * @param x
//     * @return
//     */
//    static public float atan2 (float y, float x) {
//        if(y == 0f && x >= 0f) return 0f;
//        final float ax = Math.abs(x), ay = Math.abs(y);
//        if(ax < ay)
//        {
//            final float a = ax / ay,
//                    r = 1.57079637f - (a * (0.7853981633974483f + 0.273f * (1f - a)));
//            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
//        }
//        else {
//            final float a = ay / ax,
//                    r = (a * (0.7853981633974483f + 0.273f * (1f - a)));
//            return (x < 0f) ? (y < 0f) ? -3.14159274f + r : 3.14159274f - r : (y < 0f) ? -r : r;
//        }
////// Actually this is slightly better here, but still not very good.
////        if (x == 0f) {
////            if (y > 0f) return PI / 2;
////            if (y == 0f) return 0f;
////            return -PI / 2;
////        }
////        final float atan, z = y / x;
////        if (Math.abs(z) < 1f) {
////            atan = z / (1f + 0.28f * z * z);
////            if (x < 0f) return atan + (y < 0f ? -PI : PI);
////            return atan;
////        }
////        atan = PI / 2 - z / (z * z + 0.28f);
////        return y < 0f ? atan - PI : atan;
//    }
}
