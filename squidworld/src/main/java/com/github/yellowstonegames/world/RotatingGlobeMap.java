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

import java.util.Arrays;

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
     * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
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
     * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
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
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
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

    /**
     * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}, but this also
     * requires width and height that match the first two lines of the given String (in {@link Base#BASE86}). It is
     * almost always easier to use {@link #recreateFromString(String)} instead.
     * @param width width of the map or maps to generate; must match the first line of the given String in {@link Base#BASE86}
     * @param height height of the map or maps to generate; must match the second line of the given String in {@link Base#BASE86}
     * @param serialized should have been produced by {@link #stringSerialize()}
     */
    public RotatingGlobeMap(int width, int height, String serialized) {
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

        storedMap = StretchWorldMap.recreateFromString(serialized.substring(serialized.indexOf("\n\n")+2));
    }

    /**
     * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
     * type with {@link #RotatingGlobeMap(int, int, String)} or (preferably) {@link #recreateFromString(String)}.
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

        sb.append("\n\n").append(storedMap.stringSerialize());

        return sb.toString();
    }

    /**
     * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
     * This can get the width and height from the String, which makes this probably preferable to using the constructor
     * {@link #RotatingGlobeMap(int, int, String)}. This stores the last-generated map in this WorldMapGenerator, where
     * it can be used by other code like a {@link WorldMapView}.
     * @param data the output of {@link #stringSerialize()}
     * @return the map that was serialized, as a new generator
     */
    public static RotatingGlobeMap recreateFromString(String data) {
        int mid = data.indexOf('\n');
        int width = Base.BASE86.readInt(data, 0, mid);
        int height = Base.BASE86.readInt(data, mid + 1, data.indexOf('\n', mid+1));
        return new RotatingGlobeMap(width, height, data);
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

            }
        }

    }

    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              float landMod, float heatMod, long stateA, long stateB) {
        if (cacheA != stateA || cacheB != stateB)// || landMod != storedMap.landModifier || coolMod != storedMap.coolingModifier)
        {
            storedMap.regenerate(0, 0, width << 1, height, landMod, heatMod, stateA, stateB);

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RotatingGlobeMap)) return false;

        RotatingGlobeMap that = (RotatingGlobeMap) o;
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
        if(!storedMap.equals(that.storedMap)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Hasher.marax.hashBulk(stringSerialize());
    }

    @Override
    public String toString() {
        return "RotatingGlobeMap { width: " + width + ", height: " + height
                + ", landModifier: " + landModifier + ", heatModifier: " + heatModifier
                + ", seedA: " + seedA + ", seedB: " + seedB
                + ", zoom: " + zoom + ", noise tag: " + storedMap.terrainBasic.getTag()
                + "}";
    }
}
