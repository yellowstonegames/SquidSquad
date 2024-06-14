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
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.NoiseWrapper;

import java.util.Arrays;

/**
 * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto a shape that resembles a
 * mix part-way between an ellipse and a rectangle. This is an equal-area projection, like EllipticalWorldMap, so effects that fill
 * areas on a map like PoliticalMapper will fill (almost) equally on any part of the map. This has less
 * distortion around all the edges than the other maps here, especially when comparing the North and South poles
 * with RoundSideWorldMap. This uses the
 * <a href="https://en.wikipedia.org/wiki/Tobler_hyperelliptical_projection">Tobler hyperelliptical projection</a>.
 * <br>
 * This is my (Tommy Ettinger's) personal favorite projection here, because it can be close enough to the familiar
 * rectangular map projections used in many atlases, while still keeping the poles as single points and minimizing most
 * kinds of shape distortion. Thank you, Waldo Tobler, RIP.
 * <br>
 * <a href="https://yellowstonegames.github.io/SquidLib/HyperellipseWorld.png">Example map</a>.
 */
public class HyperellipticalWorldMap extends WorldMapGenerator {
    protected static final float terrainFreq = 2.1f, terrainLayeredFreq = 0.9f, heatFreq = 1.9f, moistureFreq = 2.1f, otherFreq = 4.6f;
    //    protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
    protected float minHeat0 = Float.POSITIVE_INFINITY, maxHeat0 = Float.NEGATIVE_INFINITY,
            minHeat1 = Float.POSITIVE_INFINITY, maxHeat1 = Float.NEGATIVE_INFINITY,
            minWet0 = Float.POSITIVE_INFINITY, maxWet0 = Float.NEGATIVE_INFINITY;

    public final NoiseWrapper terrainRidged, heat, moisture, otherRidged, terrainBasic;
    public final float[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;
    public final float alpha, kappa, epsilon;
    private final float[] buffer;


    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Always makes a 200x100 map.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     * If you were using {@link HyperellipticalWorldMap#HyperellipticalWorldMap(long, int, int, INoise, float)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, new Noise(DEFAULT_NOISE), 1f}.
     * <a href="http://yellowstonegames.github.io/SquidLib/HyperellipseWorld.png" >Example map, showing special shape</a>
     */
    public HyperellipticalWorldMap() {
        this(0x1337BABE1337D00DL, 200, 100, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    public HyperellipticalWorldMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1f as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
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
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, new Noise(DEFAULT_NOISE), octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses the given noise generator, with 1f as the octave multiplier affecting detail. The suggested Noise
     * implementation to use is {@link Noise#instance}.
     *
     * @param initialSeed    the seed for the FlowRandom this uses; this may also be set per-call to generate
     * @param mapWidth       the width of the map(s) to generate; cannot be changed later
     * @param mapHeight      the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
     * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
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
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator, float octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, octaveMultiplier, 0.0625f, 2.5f);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
     * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
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
     * @param alpha            one of the Tobler parameters;  0.0625f is the default and this can range from 0f to 1f at least
     * @param kappa            one of the Tobler parameters; 2.5f is the default but 2f-5f range values are also often used
     */
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight, INoise noiseGenerator,
                                   float octaveMultiplier, float alpha, float kappa) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new float[width][height];
        yPositions = new float[width][height];
        zPositions = new float[width][height];
        edges = new int[height << 1];

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

        this.alpha = alpha;
        this.kappa = kappa;
        this.buffer = new float[height << 2];
        this.epsilon = ProjectionTools.simpsonIntegrateHyperellipse(0f, 1f, 0.25f / height, kappa);
        ProjectionTools.simpsonODESolveHyperellipse(1, this.buffer, 0.25f / height, alpha, kappa, epsilon);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape.
     * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
     * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
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
     * @param terrainRidgedNoise     an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param terrainBasicNoise     an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param heatNoise     an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param moistureNoise     an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param otherRidgedNoise     an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5f at the bare-minimum detail and 1f normal
     * @param alpha            one of the Tobler parameters;  0.0625f is the default and this can range from 0f to 1f at least
     * @param kappa            one of the Tobler parameters; 2.5f is the default but 2f-5f range values are also often used
     */
    public HyperellipticalWorldMap(long initialSeed, int mapWidth, int mapHeight, Noise terrainRidgedNoise, Noise terrainBasicNoise,
                                   Noise heatNoise, Noise moistureNoise, Noise otherRidgedNoise,
                                   float octaveMultiplier, float alpha, float kappa) {
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

        this.alpha = alpha;
        this.kappa = kappa;
        this.buffer = new float[height << 2];
        this.epsilon = ProjectionTools.simpsonIntegrateHyperellipse(0f, 1f, 0.25f / height, kappa);
        ProjectionTools.simpsonODESolveHyperellipse(1, this.buffer, 0.25f / height, alpha, kappa, epsilon);
    }

    /**
     * Copies the HyperellipticalWorldMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     *
     * @param other a HyperellipticalWorldMap to copy
     */
    public HyperellipticalWorldMap(HyperellipticalWorldMap other) {
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
        alpha = other.alpha;
        kappa = other.kappa;
        epsilon = other.epsilon;
        buffer = Arrays.copyOf(other.buffer, other.buffer.length);
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
        final float z0 = Math.abs(TrigTools.sin(latitude));
        final int i = Arrays.binarySearch(buffer, z0);
        final float y;
        if (i >= 0)
            y = i / (buffer.length - 1f);
        else if (-i - 1 >= buffer.length)
            y = buffer[buffer.length - 1];
        else
            y = ((z0 - buffer[-i - 2]) / (buffer[-i - 1] - buffer[-i - 2]) + (-i - 2)) / (buffer.length - 1f);
        final int xx = (int) (((longitude - getCenterLongitude() + 12.566370614359172f) % 6.283185307179586f) * Math.abs(alpha + (1 - alpha) * Math.pow(1 - Math.pow(Math.abs(y), kappa), 1 / kappa)) + 0.5f);
        final int yy = (int) (y * Math.signum(latitude) * height * 0.5f + 0.5f);
        return Coord.get(wrapX(xx, yy), wrapY(xx, yy));
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
                th, lat,
                rx = width * 0.5f - 0.5f, irx = TrigTools.PI / rx,
                ry = height * 0.5f, iry = 1f / ry;

        yPos = startY - ry;
        for (int y = 0; y < height; y++, yPos += i_uh) {
            lat = TrigTools.asin(buffer[(int) (0.5f + Math.abs(yPos * iry) * (buffer.length - 1))]) * Math.signum(yPos);
            qs = TrigTools.sin(lat);
            qc = TrigTools.cos(lat);

            boolean inSpace = true;
            xPos = startX - rx;
            for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                th = xPos * irx / Math.abs(alpha + (1 - alpha) * ProjectionTools.hyperellipse(yPos * iry, kappa));
                if (th < -3.141592653589793f || th > 3.141592653589793f) {
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
                heightData[x][y] = (h = terrainBasic.getNoiseWithSeed(pc +
                                terrainRidged.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5f,
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
