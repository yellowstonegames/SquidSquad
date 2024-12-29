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
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.TextTools;
import com.github.tommyettinger.random.FlowRandom;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.NoiseWrapper;

/**
 * A concrete implementation of {@link WorldMapGenerator} that does no projection of the map, as if the area were
 * completely flat or small enough that curvature is impossible to see. This also does not change heat levels at the
 * far north and south regions of the map, since it is meant for areas that are all about the same heat level.
 * <a href="http://yellowstonegames.github.io/SquidLib/LocalMap.png" >Example map, showing lack of polar ice</a>
 */
public class LocalMap extends WorldMapGenerator {
    protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
    //    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final NoiseWrapper terrainRidged, heat, moisture, otherRidged, terrainBasic;
    public final float[][] xPositions,
            yPositions,
            zPositions;


    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
     * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
     * have significantly-exaggerated-in-size features while the equator is not distorted.
     * Always makes a 256x128 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link com.github.yellowstonegames.world.LocalMap#LocalMap(long, int, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, new Noise(DEFAULT_NOISE), 1f}.
     */
    public LocalMap() {
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
    public LocalMap(int mapWidth, int mapHeight) {
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
    public LocalMap(long initialSeed, int mapWidth, int mapHeight) {
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
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
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
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
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
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, often {@link Noise#instance}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
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
    }

    /**
     * Copies the LocalMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a LocalMap to copy
     */
    public LocalMap(com.github.yellowstonegames.world.LocalMap other) {
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


    /**
     * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}, but this also
     * requires width and height that match the first two lines of the given String (in {@link Base#BASE86}). It is
     * almost always easier to use {@link #recreateFromString(String)} instead.
     * @param width width of the map or maps to generate; must match the first line of the given String in {@link Base#BASE86}
     * @param height height of the map or maps to generate; must match the second line of the given String in {@link Base#BASE86}
     * @param serialized should have been produced by {@link #stringSerialize()}
     */
    public LocalMap(int width, int height, String serialized) {
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
    }

    /**
     * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
     * type with {@link #LocalMap(int, int, String)} or (preferably) {@link #recreateFromString(String)}.
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

        //TODO: Fields of this class:
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
        b.appendJoinedExact2D(sb, "\t", " ", zPositions);

        return sb.toString();
    }

    /**
     * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
     * This can get the width and height from the String, which makes this probably preferable to using the constructor
     * {@link #LocalMap(int, int, String)}. This stores the last-generated map in this WorldMapGenerator, where
     * it can be used by other code like a {@link WorldMapView}.
     * @param data the output of {@link #stringSerialize()}
     * @return the map that was serialized, as a new generator
     */
    public static LocalMap recreateFromString(String data) {
        int mid = data.indexOf('\n');
        int width = Base.BASE86.readInt(data, 0, mid);
        int height = Base.BASE86.readInt(data, mid + 1, data.indexOf('\n', mid+1));
        return new LocalMap(width, height, data);
    }

    @Override
    public int wrapX(final int x, final int y) {
        return Math.max(0, Math.min(x, width - 1));
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

        landModifier = (landMod <= 0) ? rng.nextFloat(0.29f) + 0.91f : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextFloat(0.45f) * (rng.nextFloat() - 0.5f) + 1.1f : heatMod;

        float p,
                ps, pc,
                qs, qc,
                h, temp,
                i_w = 1f / width, i_h = 1f / (height), ii = Math.max(i_w, i_h),
                i_uw = usedWidth * i_w * ii, i_uh = usedHeight * i_h * ii, xPos, yPos = startY * i_h;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            xPos = startX * i_w;
            for (int x = 0; x < width; x++, xPos += i_uw) {
                xPositions[x][y] = xPos;
                yPositions[x][y] = yPos;
                zPositions[x][y] = 0f;
                heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(xPos +
                                terrainRidged.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
                        yPos, seedA) + landModifier - 1f);
                heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                + 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                        seedB));
                temp = 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

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
                hMod;
        yPos = startY * i_h + i_uh;
        ps = Float.POSITIVE_INFINITY;
        pc = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            for (int x = 0; x < width; x++) {
                h = heightData[x][y];
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
                heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6f);
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
