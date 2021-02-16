package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.grid.Noise;

/**
 * A concrete implementation of {@link WorldMapGenerator} that does no projection of the map, as if the area were
 * completely flat or small enough that curvature is impossible to see. This also does not change heat levels at the
 * far north and south regions of the map, since it is meant for areas that are all about the same heat level.
 * <a href="http://yellowstonegames.github.io/SquidLib/LocalMap.png" >Example map, showing lack of polar ice</a>
 */
public class LocalMap extends WorldMapGenerator {
    private static final long serialVersionUID = 1L;
    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final Noise terrain, otherRidged, heat, moisture, terrainLayered;
    public final float[][] xPositions,
            yPositions,
            zPositions;


    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
     * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
     * have significantly-exaggerated-in-size features while the equator is not distorted.
     * Always makes a 256x128 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link com.github.yellowstonegames.world.LocalMap#LocalMap(long, int, int, Noise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1f}.
     */
    public LocalMap() {
        this(0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1f);
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
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
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
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public LocalMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1f);
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
     * @param initialSeed      the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
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
     * @param initialSeed    the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
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
     * @param initialSeed      the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth         the width of the map(s) to generate; cannot be changed later
     * @param mapHeight        the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator   an instance of a noise generator capable of 3D noise, usually {@link Noise#instance}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     */
    public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, float octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];


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
     * Copies the LocalMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a LocalMap to copy
     */
    public LocalMap(com.github.yellowstonegames.world.LocalMap other) {
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
                heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(xPos +
                                terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5f,
                        yPos, seedA) + landModifier - 1f);
                heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                + 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                        seedB));
                temp = 0.375f * otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

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
