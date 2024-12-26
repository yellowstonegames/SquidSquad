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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.RoughMath;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.asin;

/**
 * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
 * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). It should
 * look as a world would when viewed from space, or as a globe would from its side, and implements rotation differently
 * to allow the planet to be rotated without recalculating all the data. Note that calling
 * {@link #setCenterLongitude(float)} does a lot more work than in other classes, but less than fully calling
 * {@link #generate()} in those classes, since it doesn't remake the map data at a slightly different rotation and
 * instead keeps a single map in use the whole time, using sections of it. This uses an
 * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
 * the latitude always at the equator; the internal map is stored using the
 * <a href="https://en.wikipedia.org/wiki/Cylindrical_equal-area_projection#Discussion">cylindrical equal-area
 * projection</a>, specifically the Lambert equal-area projection.
 * <br>
 * <a href="https://i.imgur.com/WNa5nQ1.gifv">Example view of a planet rotating</a>.
 * <a href="https://i.imgur.com/NV5IMd6.gifv">Another example</a>.
 */
public class RotatingGlobeMapBundle extends WorldMapGenerator {

    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final float[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;
    public final StoredWorldMap storedMap;

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
     * showing only one hemisphere at a time.
     * Always makes a 100x100 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link RotatingGlobeMapBundle#RotatingGlobeMapBundle(long, int, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, new Noise(DEFAULT_NOISE), 1f}.
     */
    public RotatingGlobeMapBundle() {
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
    public RotatingGlobeMapBundle(int mapWidth, int mapHeight) {
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
     * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public RotatingGlobeMapBundle(long initialSeed, int mapWidth, int mapHeight) {
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public RotatingGlobeMapBundle(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
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
     * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public RotatingGlobeMapBundle(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public RotatingGlobeMapBundle(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[mapWidth][mapHeight];
        yPositions = new float[mapWidth][mapHeight];
        zPositions = new float[mapWidth][mapHeight];
        edges = new int[height << 1];
        storedMap = new StoredWorldMap(initialSeed, mapWidth << 1, mapHeight, noiseGenerator, octaveMultiplier);
    }

    /**
     * Copies the RotatingGlobeMapBundle {@code other} to construct a new one that is exactly the same. References will only
     * be shared to INoise classes.
     *
     * @param other a RotatingGlobeMapBundle to copy
     */
    public RotatingGlobeMapBundle(RotatingGlobeMapBundle other) {
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
        storedMap = new StoredWorldMap(other.storedMap);
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
                i_pi = 0.31830984f,/* MathTools.towardsZero(TrigTools.PI_INVERSE) */
                rx = width * 0.5f, irx = i_uw / rx,
                ry = height * 0.5f, iry = i_uh / ry;

        yPos = startY - ry;
        iyPos = yPos / ry;
        for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {
            boolean inSpace = true;
            xPos = startX - rx;
            ixPos = xPos / rx;
            lat = TrigTools.asin(iyPos);

            float radians, from, to;
            int floor, masked;
            radians = lat * TrigTools.radToIndex;
            floor = (int)(radians + 16384.0) - 16384;
            radians -= floor;
            masked = floor & TrigTools.TABLE_MASK;

//            qs = TrigTools.SIN_TABLE[masked];
//            qc = TrigTools.COS_TABLE[masked];

            from = TrigTools.SIN_TABLE[masked];
            to = TrigTools.SIN_TABLE[masked+1];
            qs = from + (to - from) * (radians);

            from = TrigTools.COS_TABLE[masked];
            to = TrigTools.COS_TABLE[masked+1];
            qc = from + (to - from) * (radians);

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
                lon = removeExcess((centerLongitude + (TrigTools.atan2(ixPos * rho, rho * TrigTools.cosSmoother(th)))) * 0.5f);

                radians = lon * TrigTools.radToIndex;
                floor = (int)(radians + 16384.0) - 16384;
                masked = floor & TrigTools.TABLE_MASK;

//                ps = TrigTools.SIN_TABLE[masked];
//                pc = TrigTools.COS_TABLE[masked];
//
                from = TrigTools.SIN_TABLE[masked];
                to = TrigTools.SIN_TABLE[masked+1];
                ps = from + (to - from) * (radians);

                from = TrigTools.COS_TABLE[masked];
                to = TrigTools.COS_TABLE[masked+1];
                pc = from + (to - from) * (radians);

                ax = (int) ((lon * i_pi + 1f) * width);
                ay = (int) ((qs + 1f) * ry);

//                    // Hammer projection, not an inverse projection like we usually use
//                    z = 1f / Math.sqrt(1 + qc * TrigTools.cosSmoother(lon * 0.5f));
//                    ax = (int)((qc * TrigTools.sinSmoother(lon * 0.5f) * z + 1f) * width);
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


    public static class StoredWorldMap extends WorldMapGenerator {
        protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
        //    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
                minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
                minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

        public final PlanetNoiseWrapper terrainRidged, heat, moisture, otherRidged, terrainBasic;
        public final float[][] xPositions,
                yPositions,
                zPositions;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Always makes a 256x128 map.
         * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
         * If you were using {@link StoredWorldMap#StoredWorldMap(long, int, int, INoise, float)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, new Noise(DEFAULT_NOISE), 1f}.
         */
        public StoredWorldMap() {
            this(0x1337BABE1337D00DL, 256, 128, new Noise(DEFAULT_NOISE), 1f);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public StoredWorldMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public StoredWorldMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
         * @param mapWidth         the width of the map(s) to generate; cannot be changed later
         * @param mapHeight        the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
         */
        public StoredWorldMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1f as the octave multiplier affecting detail.
         *
         * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
         * @param mapWidth       the width of the map(s) to generate; cannot be changed later
         * @param mapHeight      the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public StoredWorldMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1f);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
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
         * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
         * @param mapWidth         the width of the map(s) to generate; cannot be changed later
         * @param mapHeight        the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise#instance}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
         */
        public StoredWorldMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new float[width][height];
            yPositions = new float[width][height];
            zPositions = new float[width][height];

            terrainRidged = new PlanetNoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainFreq,
                    Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 8)); // was 10
            terrainBasic = new PlanetNoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainLayeredFreq,
                    Noise.FBM, (int) (0.5f + octaveMultiplier * 3)); // was 8
            heat = new PlanetNoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), heatFreq,
                    Noise.FBM, (int) (0.5f + octaveMultiplier * 5)); // was 3, then 2
            moisture = new PlanetNoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), moistureFreq,
                    Noise.FBM, (int) (0.5f + octaveMultiplier * 2)); // was 4
            otherRidged = new PlanetNoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), otherFreq,
                    Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 5)); // was 6
        }

        @Override
        public int wrapY(final int x, final int y) {
            return Math.max(0, Math.min(y, height - 1));
        }

        /**
         * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
         * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
         * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
         * the projection for a given lat-lon coordinate, this returns null. This implementation never returns null.
         * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
         * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
         * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
         *
         * @param latitude  the latitude, from {@code -TrigTools.HALF_PI} to {@code TrigTools.HALF_PI}
         * @param longitude the longitude, from {@code 0f} to {@code TrigTools.PI2}
         * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
         */
        @Override
        public Coord project(float latitude, float longitude) {
            int x = (int) ((((longitude - getCenterLongitude()) + TrigTools.PI2 + TrigTools.PI) % TrigTools.PI2) * TrigTools.PI_INVERSE * 0.5f * width),
                    y = (int) ((TrigTools.sinSmoother(latitude) * 0.5f + 0.5f) * height);
            return Coord.get(
                    wrapX(x, y),
                    wrapY(x, y));
        }

        /**
         * Copies the StoredWorldMap {@code other} to construct a new one that is exactly the same. References will only be
         * shared to Noise classes.
         *
         * @param other a StoredWorldMap to copy
         */
        public StoredWorldMap(StoredWorldMap other) {
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

            landModifier = (landMod <= 0) ? rng.nextFloat(0.29f) + 0.91f : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

            float p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 6.283185307179586f / width, i_h = 2f / (height + 2f),//(3.141592653589793f) / (height+2f),
                    xPos = startX, yPos, i_uw = usedWidth / (float) width, i_uh = usedHeight * i_h / (height + 2f);
            final float[] trigTable = new float[width << 1];
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w + centerLongitude + TrigTools.PI;
                // 0.7978845608028654f 1.2533141373155001f
                trigTable[x << 1] = TrigTools.sinSmoother(p);// * 1.2533141373155001f;
                trigTable[x << 1 | 1] = TrigTools.cosSmoother(p);// * 0.7978845608028654f;
            }
            yPos = startY * i_h + i_uh;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                qs = -1 + yPos;//-1.5707963267948966f + yPos;
                qc = TrigTools.cosSmoother(asin(qs));
                for (int x = 0, xt = 0; x < width; x++) {
                    ps = trigTable[xt++] * qc;//TrigTools.sinSmoother(p);
                    pc = trigTable[xt++] * qc;//TrigTools.cosSmoother(p);
                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(pc, ps, qs, seedA) + landModifier - 1f);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs, seedC));

//                    heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(pc, ps, qs, seedA) * 0.667f
//                            + terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.333f + landModifier - 1f);
//                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qs, seedB) * 0.625f
//                            + otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC) * 0.375f);
//                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs, seedC) * 0.625f
//                            + otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA) * 0.375f);
//
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
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Float.POSITIVE_INFINITY;
            pc = Float.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = (yPos - 1f);
                temp = RoughMath.expRough(-temp*temp) * 2.2f;
                for (int x = 0; x < width; x++) {
                    if (heightCodeData[x][y] == 10000) {
                        heightCodeData[x][y] = 1000;
                        continue;
                    } else {
                        heightCodeData[x][y] = codeHeight(h = heightData[x][y]);
                    }
                    hMod = (RoughMath.logisticRough(h*2.75f-1f)+0.18f);
                    h = 0.39f - RoughMath.logisticRough(h*4f) * (h+0.1f) * 0.82f;
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

}
