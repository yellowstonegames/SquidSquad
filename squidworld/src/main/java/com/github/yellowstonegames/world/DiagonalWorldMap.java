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
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.NoiseWrapper;

import java.util.Arrays;

import static com.github.tommyettinger.digital.TrigTools.asin;

/**
 * A concrete implementation of {@link WorldMapGenerator} that is like {@link StretchWorldMap} but rotates the world so
 * the South Pole is in the lower-left corner (0,0), the North Pole is in the upper right corner (near (width,height)),
 * and wrapping transposes rather than wraps.
 */
public class DiagonalWorldMap extends WorldMapGenerator {
    protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
    //    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final NoiseWrapper terrainRidged, heat, moisture, otherRidged, terrainBasic;
    public final float[][] xPositions,
            yPositions,
            zPositions;
    public final float alpha = Float.MIN_NORMAL, kappa = 1f, epsilon;
    private final float[] buffer;


    /**
     * Always makes a 256x256 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link #DiagonalWorldMap(long, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256, new Noise(DEFAULT_NOISE), 1f}.
     */
    public DiagonalWorldMap() {
        this(0x1337BABE1337D00DL, 256, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     *
     * @param mapSize  the width and height of the map(s) to generate; cannot be changed later
     */
    public DiagonalWorldMap(int mapSize) {
        this(0x1337BABE1337D00DL, mapSize, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     *
     * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapSize  the width and height of the map(s) to generate; cannot be changed later
     */
    public DiagonalWorldMap(long initialSeed, int mapSize) {
        this(initialSeed, mapSize, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     *
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapSize  the width and height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public DiagonalWorldMap(long initialSeed, int mapSize, float octaveMultiplier) {
        this(initialSeed, mapSize, new Noise(DEFAULT_NOISE), octaveMultiplier);
    }

    /**
     *
     * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapSize  the width and height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public DiagonalWorldMap(long initialSeed, int mapSize, INoise noiseGenerator) {
        this(initialSeed, mapSize, noiseGenerator, 1f);
    }

    /**
     *
     * @param initialSeed      the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapSize  the width and height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise#instance}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public DiagonalWorldMap(long initialSeed, int mapSize, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapSize, mapSize);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];

        terrainRidged = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 8)); // was 10
        terrainBasic = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), terrainLayeredFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 3)); // was 8
        heat = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), heatFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 5)); // was 3, then 2
        moisture = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), moistureFreq,
                Noise.FBM, (int) (0.5f + octaveMultiplier * 2)); // was 4
        otherRidged = new NoiseWrapper(noiseGenerator, noiseGenerator.getSeed(), otherFreq,
                Noise.RIDGED_MULTI, (int) (0.5f + octaveMultiplier * 5)); // was 6
        int diag = height << 1;
        this.buffer = new float[diag << 2];
        this.epsilon = ProjectionTools.simpsonIntegrateHyperellipse(0f, 1f, 0.25f / diag, kappa);
        ProjectionTools.simpsonODESolveHyperellipse(1f, this.buffer, 0.25f / diag, alpha, kappa, epsilon);
    }

    /**
     * Copies the DiagonalWorldMap {@code other} to construct a new one that is exactly the same. References will only be
     * shared to Noise classes.
     *
     * @param other a DiagonalWorldMap to copy
     */
    public DiagonalWorldMap(DiagonalWorldMap other) {
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
        epsilon = other.epsilon;
        buffer = Arrays.copyOf(other.buffer, other.buffer.length);
    }

    /**
     * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}, but this also
     * requires width and height that match the first two lines of the given String (in {@link Base#BASE86}). It is
     * almost always easier to use {@link #recreateFromString(String)} instead.
     * @param mapSize width and height of the map or maps to generate; must match the first line of the given String in {@link Base#BASE86}
     * @param serialized should have been produced by {@link #stringSerialize()}
     */
    public DiagonalWorldMap(int mapSize, String serialized) {
        super(mapSize, mapSize);
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
        epsilon = Base.BASE86.readFloatExact(parts[i++]);
        buffer = Base.BASE86.floatSplitExact(parts[i++], " ");
    }

    /**
     * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
     * type with {@link #DiagonalWorldMap(int, String)} or (preferably) {@link #recreateFromString(String)}.
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

        // fields of this class:
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
        b.appendUnsigned(sb, epsilon ).append('\n');
        b.appendJoinedExact(sb, " ", buffer);

        return sb.toString();
    }

    /**
     * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
     * This can get the width and height from the String, which makes this probably preferable to using the constructor
     * {@link #DiagonalWorldMap(int, String)}. This stores the last-generated map in this WorldMapGenerator, where
     * it can be used by other code like a {@link WorldMapView}.
     * @param data the output of {@link #stringSerialize()}
     * @return the map that was serialized, as a new generator
     */
    public static DiagonalWorldMap recreateFromString(String data) {
        int mid = data.indexOf('\n');
        int mapSize = Base.BASE86.readInt(data, 0, mid);
        return new DiagonalWorldMap(mapSize, data);
    }

    @Override
    public int wrapX(int x, int y) {
        if(x < 0 || x > width) return y;
        if(y < 0) return 0;
        if(y >= height) return width - 1;
        return x;
    }

    @Override
    public int wrapY(final int x, final int y) {
        if(y < 0 || y > height) return y;
        if(x < 0) return 0;
        if(x >= width) return height - 1;
        return y;
    }

    @Override
    public void zoomIn(int zoomAmount, int zoomCenterX, int zoomCenterY) {
        throw new UnsupportedOperationException("Zooming is not supported by DiagonalWorldMap.");
    }

    @Override
    public void zoomOut(int zoomAmount, int zoomCenterX, int zoomCenterY) {
        throw new UnsupportedOperationException("Zooming is not supported by DiagonalWorldMap.");
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
        float lat = (latitude * 2f + TrigTools.PI + longitude) * MathTools.ROOT2_INVERSE * 0.5f - TrigTools.HALF_PI;
        float lon = (latitude * 2f + TrigTools.PI - longitude + TrigTools.PI2) * MathTools.ROOT2_INVERSE;
        int x = (int) ((((lon - getCenterLongitude()) + TrigTools.PI2 + TrigTools.PI) % TrigTools.PI2) * TrigTools.PI_INVERSE * 0.5f * width),
                y = (int) ((TrigTools.sinSmoother(lat) * 0.5f + 0.5f) * height);
        return Coord.get(
                wrapX(x, y),
                wrapY(x, y));
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

        landModifier = (landMod <= 0) ? rng.nextFloat(0.2f) + 0.91f : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

        int widthArray = this.width;
        int heightArray = this.height;
        int width = this.width << 1;
        int height = this.height << 1;
        float p,
                ps, pc,
                qs, qc,
                h, temp, yPos, xPos,
                i_uw = 1f,
                i_uh = 1f,
                th, lat,
                rx = width * 0.5f - 0.5f, irx = TrigTools.PI / rx,
                ry = height * 0.5f, iry = 1f / ry;

        yPos = startY - ry;
        int ax = 0, ay = 0;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            lat = TrigTools.asin(buffer[(int) (0.5f + Math.abs(yPos * iry) * (buffer.length - 1))]) * Math.signum(yPos);
            qs = TrigTools.sinSmoother(lat);
            qc = TrigTools.cosSmoother(lat);

            if(y > heightArray) {
                ax = y - heightArray;
                ay = heightArray - 1;
            }
            else {
                ax = 0;
                ay = y - 1;
            }
            int postHalf = 1;
            xPos = startX - rx;
            for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                th = xPos * irx / Math.abs(alpha + (1 - alpha) * ProjectionTools.hyperellipse(yPos * iry, kappa));
                if (th < -TrigTools.PI || th > TrigTools.PI || ax < 0 || ay < 0) {
                    continue;
                }
                postHalf ^= 1;
                th += centerLongitude;
                ps = TrigTools.sinSmoother(th) * qc;
                pc = TrigTools.cosSmoother(th) * qc;
                if(postHalf == 0) {
                    xPositions[ax][ay] = pc;
                    yPositions[ax][ay] = ps;
                    zPositions[ax][ay] = qs;
                    heightData[ax][ay] = (h = terrainBasic.getNoiseWithSeed(pc +
                                    terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                            ps, qs, seedA) + landModifier - 1f);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                    + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC));
                } else {
                    heightData[ax][ay] = (h = (heightData[ax][ay] + terrainBasic.getNoiseWithSeed(pc +
                                    terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
                            ps, qs, seedA) + landModifier - 1f) * 0.5f);
                    heatData[x][y] = (p = (heatData[x][y] + heat.getNoiseWithSeed(pc, ps
                                    + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
                            , qs, seedB)) * 0.5f);
                    moistureData[x][y] = (temp = (moistureData[x][y] + moisture.getNoiseWithSeed(pc, ps, qs
                                    + 0.375f * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                            , seedC)) * 0.5f);
                    ax--;
                    ay--;
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
            if(y > heightArray) {
                ax = y - heightArray;
                ay = heightArray - 1;
            }
            else {
                ax = 0;
                ay = y - 1;
            }
            int postHalf = 1;

            for (int x = 0; x < width; x++) {
                if (ax < 0 || ay < 0) {
                    continue;
                }
                postHalf ^= 1;
                if(postHalf == 1) {
                    heightCodeData[ax][ay] = codeHeight(th = heightData[ax][ay]);
                    hMod = (RoughMath.logisticRough(th * 2.75f - 1f) + 0.18f);
                    h = 0.39f - RoughMath.logisticRough(th * 4f) * (th + 0.1f) * 0.82f;
                    heatData[ax][ay] = (h = (((heatData[ax][ay] - minHeat0) * heatDiff * hMod) + h + 0.6f) * temp);
                    ax--;
                    ay--;
                    if (fresh) {
                        ps = Math.min(ps, h); //minHeat0
                        pc = Math.max(pc, h); //maxHeat0
                    }
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


        for (int y = 0; y < heightArray; y++) {
            for (int x = 0; x < widthArray; x++) {
                heatData[x][y] = (h = (heatData[x][y] - minHeat1) * heatDiff);
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
        if (!(o instanceof DiagonalWorldMap)) return false;

        DiagonalWorldMap that = (DiagonalWorldMap) o;
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

        return true;
    }

    @Override
    public int hashCode() {
        return Hasher.marax.hashBulk(stringSerialize());
    }

    @Override
    public String toString() {
        return "DiagonalWorldMap { width: " + width + ", height: " + height
                + ", landModifier: " + landModifier + ", heatModifier: " + heatModifier
                + ", seedA: " + seedA + ", seedB: " + seedB
                + ", zoom: " + zoom + ", noise tag: " + terrainBasic.getTag()
                + "}";
    }
}
