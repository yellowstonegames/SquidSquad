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

import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.random.FlowRandom;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.NoiseWrapper;

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
// From i/ :
    protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
// Original values:
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
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, new Noise(DEFAULT_NOISE), 1f}.
     */
    public GlobeMap() {
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
    public GlobeMap(int mapWidth, int mapHeight) {
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
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight) {
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
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
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
    public GlobeMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
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

    /**
     * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}, but this also
     * requires width and height that match the first two lines of the given String (in {@link Base#BASE86}). It is
     * almost always easier to use {@link #recreateFromString(String)} instead.
     * @param width width of the map or maps to generate; must match the first line of the given String in {@link Base#BASE86}
     * @param height height of the map or maps to generate; must match the second line of the given String in {@link Base#BASE86}
     * @param serialized should have been produced by {@link #stringSerialize()}
     */
    public GlobeMap(int width, int height, String serialized) {
        super(width, height);
        String[] parts = TextTools.split(serialized, "\n");

        int i = 2;
        // WorldMapGenerator's many fields:
//        width = Base.BASE86.readInt(parts[0]);
//        height = Base.BASE86.readInt(parts[1]);
        usedWidth = Base.BASE86.readInt(parts[i++]);
        usedHeight = Base.BASE86.readInt(parts[i++]);
        landModifier = Base.BASE86.readFloatExact(parts[i++]);
        heatModifier = Base.BASE86.readFloatExact(parts[i++]);
        minHeat = Base.BASE86.readFloatExact(parts[i++]);
        maxHeat = Base.BASE86.readFloatExact(parts[i++]);
        minHeight = Base.BASE86.readFloatExact(parts[i++]);
        maxHeight = Base.BASE86.readFloatExact(parts[i++]);
        minWet = Base.BASE86.readFloatExact(parts[i++]);
        maxWet = Base.BASE86.readFloatExact(parts[i++]);
        centerLongitude = Base.BASE86.readFloatExact(parts[i++]);
        zoom = Base.BASE86.readInt(parts[i++]);
        startX = Base.BASE86.readInt(parts[i++]);
        startY = Base.BASE86.readInt(parts[i++]);
        startCacheX.addAll(Base.BASE86.intSplit(parts[i++], " "));
        startCacheY.addAll(Base.BASE86.intSplit(parts[i++], " "));
        zoomStartX = Base.BASE86.readInt(parts[i++]);
        zoomStartY = Base.BASE86.readInt(parts[i++]);
        seedA = Base.BASE86.readLong(parts[i++]);
        seedB = Base.BASE86.readLong(parts[i++]);
        cacheA = Base.BASE86.readLong(parts[i++]);
        cacheB = Base.BASE86.readLong(parts[i++]);
        rng = new FlowRandom(Base.BASE86.readLong(parts[i++]), Base.BASE86.readLong(parts[i++]));
        ArrayTools.set(Base.BASE86.floatSplitExact2D(parts[i++], "\t", " "), heightData);
        ArrayTools.set(Base.BASE86.floatSplitExact2D(parts[i++], "\t", " "), heatData);
        ArrayTools.set(Base.BASE86.floatSplitExact2D(parts[i++], "\t", " "), moistureData);
        landData.decompressInto(parts[i++]);
        ArrayTools.set(Base.BASE86.intSplit2D(parts[i++], "\t", " "), heightCodeData);

        // Fields of this class:
        terrainRidged = new NoiseWrapper().stringDeserialize(parts[i++]);
        terrainBasic =  new NoiseWrapper().stringDeserialize(parts[i++]);
        heat =          new NoiseWrapper().stringDeserialize(parts[i++]);
        moisture =      new NoiseWrapper().stringDeserialize(parts[i++]);
        otherRidged =   new NoiseWrapper().stringDeserialize(parts[i++]);
        minHeat0 = Base.BASE86.readFloatExact(parts[i++]);
        maxHeat0 = Base.BASE86.readFloatExact(parts[i++]);
        minHeat1 = Base.BASE86.readFloatExact(parts[i++]);
        maxHeat1 = Base.BASE86.readFloatExact(parts[i++]);
        minWet0 = Base.BASE86.readFloatExact(parts[i++]);
        maxWet0 = Base.BASE86.readFloatExact(parts[i++]);
        xPositions = Base.BASE86.floatSplitExact2D(parts[i++], "\t", " ");
        yPositions = Base.BASE86.floatSplitExact2D(parts[i++], "\t", " ");
        zPositions = Base.BASE86.floatSplitExact2D(parts[i++], "\t", " ");
        edges = Base.BASE86.intSplit(parts[i++], " ");
    }

    /**
     * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
     * type with {@link #GlobeMap(int, int, String)} or (preferably) {@link #recreateFromString(String)}.
     * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
     * tends to be very long because it includes several 2D arrays and a Region as Strings.
     * @return a String that stores the entire state of this generator
     */
    public String stringSerialize(){
        StringBuilder sb = new StringBuilder(1024);
        Base b = Base.BASE86;

        // WorldMapGenerator fields:
        b.appendUnsigned(sb, width).append('\n');
        b.appendUnsigned(sb, height).append('\n');
        b.appendUnsigned(sb, usedWidth).append('\n');
        b.appendUnsigned(sb, usedHeight).append('\n');
        b.appendUnsigned(sb, landModifier).append('\n');
        b.appendUnsigned(sb, heatModifier).append('\n');
        b.appendUnsigned(sb, minHeat  ).append('\n');
        b.appendUnsigned(sb, maxHeat  ).append('\n');
        b.appendUnsigned(sb, minHeight).append('\n');
        b.appendUnsigned(sb, maxHeight).append('\n');
        b.appendUnsigned(sb, minWet   ).append('\n');
        b.appendUnsigned(sb, maxWet   ).append('\n');
        b.appendUnsigned(sb, centerLongitude).append('\n');
        b.appendUnsigned(sb, zoom).append('\n');
        b.appendUnsigned(sb, startX).append('\n');
        b.appendUnsigned(sb, startY).append('\n');
        b.appendJoined(sb, " ", startCacheX.items, 0, startCacheX.size()).append('\n');
        b.appendJoined(sb, " ", startCacheY.items, 0, startCacheY.size()).append('\n');
        b.appendUnsigned(sb, zoomStartX).append('\n');
        b.appendUnsigned(sb, zoomStartY).append('\n');
        b.appendUnsigned(sb, seedA).append('\n');
        b.appendUnsigned(sb, seedB).append('\n');
        b.appendUnsigned(sb, cacheA).append('\n');
        b.appendUnsigned(sb, cacheB).append('\n');
        b.appendUnsigned(sb, rng.getStateA()).append('\n');
        b.appendUnsigned(sb, rng.getStateB()).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", heightData).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", heatData).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", moistureData).append('\n');
        sb.append(landData.toCompressedString()).append('\n');
        b.appendJoined2D(sb, "\t", " ", heightCodeData).append('\n');

        // FIELDS Of this class:
        sb.append(terrainRidged.stringSerialize()).append('\n');
        sb.append(terrainBasic .stringSerialize()).append('\n');
        sb.append(heat         .stringSerialize()).append('\n');
        sb.append(moisture     .stringSerialize()).append('\n');
        sb.append(otherRidged  .stringSerialize()).append('\n');
        b.appendUnsigned(sb, minHeat0).append('\n');
        b.appendUnsigned(sb, maxHeat0).append('\n');
        b.appendUnsigned(sb, minHeat1).append('\n');
        b.appendUnsigned(sb, maxHeat1).append('\n');
        b.appendUnsigned(sb, minWet0 ).append('\n');
        b.appendUnsigned(sb, maxWet0 ).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", xPositions).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", yPositions).append('\n');
        b.appendJoinedExact2D(sb, "\t", " ", zPositions).append('\n');
        b.appendJoined(sb, " ", edges);

        return sb.toString();
    }

    /**
     * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
     * This can get the width and height from the String, which makes this probably preferable to using the constructor
     * {@link #GlobeMap(int, int, String)}. This stores the last-generated map in this WorldMapGenerator, where
     * it can be used by other code like a {@link WorldMapView}.
     * @param data the output of {@link #stringSerialize()}
     * @return the map that was serialized, as a new generator
     */
    public static GlobeMap recreateFromString(String data) {
        int mid = data.indexOf('\n');
        int width = Base.BASE86.readInt(data, 0, mid);
        int height = Base.BASE86.readInt(data, mid + 1, data.indexOf('\n', mid+1));
        return new GlobeMap(width, height, data);
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

            lat = TrigTools.asin(iyPos);

            qc = TrigTools.cosSmoother(lat);
            qs = TrigTools.sinSmoother(lat);

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

                lon = centerLongitude + TrigTools.atan2(ixPos * rho, rho * TrigTools.cosSmoother(th));

                pc = TrigTools.cosSmoother(lon) * qc;
                ps = TrigTools.sinSmoother(lon) * qc;

                xPositions[x][y] = pc;
                yPositions[x][y] = ps;
                zPositions[x][y] = qs;
                heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                        ps, qs, seedA) + landModifier - 1f);
//                if(Math.abs(h) < 0.01f) {
//                    System.out.println("zoomStartX = " + zoomStartX + "; zoomStartY = " + zoomStartY + "; zoom = " + zoom + "; width = " + width + "; height = " + height + ";");
//                    System.out.println("regenerate("+startX+", "+startY+", "+usedWidth+", "+usedHeight+", "+landMod+", "+heatMod+", "+stateA+", "+stateB+");");
//
////                    System.out.println("height value: " + h + " produced at x: " + pc + ", y: " + ps + ", z: " + qs);
//                }
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                        , seedC));
                if (fresh) {
                    minHeight = Math.min(minHeight, h);
                    maxHeight = Math.max(maxHeight, h);

                    minHeat0 = Math.min(minHeat0, p);
                    maxHeat0 = Math.max(maxHeat0, p);

                    minWet0 = Math.min(minWet0, temp);
                    maxWet0 = Math.max(maxWet0, temp);
                }
            }
        }
        float heatDiff = 0.8f / (maxHeat0 - minHeat0),
                wetDiff = 1f / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5f, i_half = 1f / halfHeight;
        yPos = startY + i_uh;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = (yPos - halfHeight) * i_half;
            temp = RoughMath.expRough(-temp*temp) * 2.2f;
            for (int x = 0; x < width; x++) {
                if (heightCodeData[x][y] == 10000) {
                    heightCodeData[x][y] = 1000;
                    continue;
                } else {
                    heightCodeData[x][y] = codeHeight(th = heightData[x][y]);
                }
                hMod = (RoughMath.logisticRough(th*2.75f-1f)+0.18f);
                h = 0.39f - RoughMath.logisticRough(th*4f) * (th+0.1f) * 0.82f;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GlobeMap)) return false;

        GlobeMap that = (GlobeMap) o;
        // WorldMapGenerator fields:
        if(width != that.width) return false;
        if(height != that.height) return false;
        if(usedWidth != that.usedWidth) return false;
        if(usedHeight != that.usedHeight) return false;
        if(Float.compare(landModifier, that.landModifier) != 0) return false;
        if(Float.compare(heatModifier, that.heatModifier) != 0) return false;
        if(Float.compare(minHeat  , that.minHeat  ) != 0) return false;
        if(Float.compare(maxHeat  , that.maxHeat  ) != 0) return false;
        if(Float.compare(minHeight, that.minHeight) != 0) return false;
        if(Float.compare(maxHeight, that.maxHeight) != 0) return false;
        if(Float.compare(minWet   , that.minWet   ) != 0) return false;
        if(Float.compare(maxWet   , that.maxWet   ) != 0) return false;
        if(Float.compare(centerLongitude, that.centerLongitude) != 0) return false;
        if(zoom != that.zoom) return false;
        if(startX != that.startX) return false;
        if(startY != that.startY) return false;
        if(!startCacheX.equals(that.startCacheX)) return false;
        if(!startCacheY.equals(that.startCacheY)) return false;
        if(zoomStartX != that.zoomStartX) return false;
        if(zoomStartY != that.zoomStartY) return false;
        if(seedA != that.seedA) return false;
        if(seedB != that.seedB) return false;
        if(cacheA != that.cacheA) return false;
        if(cacheB != that.cacheB) return false;
        if(rng.getStateA() != that.rng.getStateA()) return false;
        if(rng.getStateB() != that.rng.getStateB()) return false;
        if(!Arrays.deepEquals(heightData  , that.heightData  )) return false;
        if(!Arrays.deepEquals(heatData    , that.heatData    )) return false;
        if(!Arrays.deepEquals(moistureData, that.moistureData)) return false;
        if(!landData.equals(that.landData)) return false;
        if(!Arrays.deepEquals(heightCodeData, that.heightCodeData)) return false;

        // Fields Of this class:
        if(!terrainRidged.equals(that.terrainRidged)) return false;
        if(!terrainBasic .equals(that.terrainBasic )) return false;
        if(!heat         .equals(that.heat         )) return false;
        if(!moisture     .equals(that.moisture     )) return false;
        if(!otherRidged  .equals(that.otherRidged  )) return false;
        if(Float.compare(minHeat0, that.minHeat0) != 0) return false;
        if(Float.compare(maxHeat0, that.maxHeat0) != 0) return false;
        if(Float.compare(minHeat1, that.minHeat1) != 0) return false;
        if(Float.compare(maxHeat1, that.maxHeat1) != 0) return false;
        if(Float.compare(minWet0 , that.minWet0 ) != 0) return false;
        if(Float.compare(maxWet0 , that.maxWet0 ) != 0) return false;
        if(!Arrays.deepEquals(xPositions, that.xPositions)) return false;
        if(!Arrays.deepEquals(yPositions, that.yPositions)) return false;
        if(!Arrays.deepEquals(zPositions, that.zPositions)) return false;
        if(!Arrays.equals(edges, that.edges)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Hasher.marax.hashBulk(stringSerialize());
    }

    @Override
    public String toString() {
        return "GlobeMap { width: " + width + ", height: " + height
                + ", landModifier: " + landModifier + ", heatModifier: " + heatModifier
                + ", seedA: " + seedA + ", seedB: " + seedB
                + ", zoom: " + zoom + ", noise tag: " + terrainBasic.getTag()
                + "}";
    }
}
