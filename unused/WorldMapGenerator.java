

/**
 * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto an ellipse that should be
 * twice as wide as it is tall (although you can stretch it by width and height that don't have that ratio).
 * This uses the <a href="https://en.wikipedia.org/wiki/Hammer_projection">Hammer projection</a>, so the latitude
 * lines are curved instead of flat. The Mollweide projection that {@link EllipticalMap} uses has flat lines, but
 * the two projection are otherwise very similar, and are both equal-area (Hammer tends to have less significant
 * distortion around the edges, but the curvature of the latitude lines can be hard to visualize).
 * <a href="https://i.imgur.com/nmN6lMK.gifv">Preview image link of a world rotating</a>.
 */
public static class EllipticalHammerMap extends WorldMapGenerator {
    //        protected static final double terrainFreq = 1.35, terrainRidgedFreq = 1.8, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375, riverRidgedFreq = 21.7;
    protected static final double terrainFreq = 1.45, terrainRidgedFreq = 2.6, heatFreq = 2.1, moistureFreq = 2.125, otherFreq = 3.375;
    protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
            minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
            minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

    public final Noise terrain, heat, moisture, otherRidged, terrainLayered;
    public final double[][] xPositions,
            yPositions,
            zPositions;
    protected final int[] edges;


    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Always makes a 200x100 map.
     * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     * If you were using {@link EllipticalHammerMap#EllipticalHammerMap(long, int, int, Noise, double)}, then this would be the
     * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0}.
     */
    public EllipticalHammerMap() {
        this(0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     *
     * @param mapWidth  the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    public EllipticalHammerMap(int mapWidth, int mapHeight) {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     */
    public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
     */
    public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
        this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise
     * implementation to use is {@link Noise#instance}.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth    the width of the map(s) to generate; cannot be changed later
     * @param mapHeight   the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     */
    public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
        this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
    }

    /**
     * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
     * ellipse without distortion of the sizes of features but with significant distortion of shape. This is very
     * similar to {@link EllipticalMap}, but has curved latitude lines instead of flat ones (it also may see more
     * internal usage because some operations on this projection are much faster and simpler).
     * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
     * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
     * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
     * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
     * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
     * cannot be changed after the fact, but you can zoom in. Noise will be the fastest 3D generator to use for
     * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
     * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
     * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
     * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
     * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
     * that don't require zooming.
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
     * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
     */
    public EllipticalHammerMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
        super(initialSeed, mapWidth, mapHeight);
        xPositions = new double[width][height];
        yPositions = new double[width][height];
        zPositions = new double[width][height];
        edges = new int[height << 1];
        terrain = new Noise.Maelstrom3D(new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 10), terrainFreq));
        terrainLayered = new Noise.Scaled3D(noiseGenerator,  terrainRidgedFreq * 0.325);
        heat = new Noise.Scaled3D(noiseGenerator,  heatFreq);
        moisture = new Noise.Scaled3D(noiseGenerator,  moistureFreq);
        otherRidged = new Noise.Maelstrom3D(new Noise.Ridged3D(noiseGenerator, (int) (0.5 + octaveMultiplier * 6), otherFreq));
    }

    /**
     * Copies the EllipticalHammerMap {@code other} to construct a new one that is exactly the same. References will only
     * be shared to Noise classes.
     * @param other an EllipticalHammerMap to copy
     */
    public EllipticalHammerMap(EllipticalHammerMap other)
    {
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
        if(x < edges[y << 1])
            return edges[y << 1 | 1];
        else if(x > edges[y << 1 | 1])
            return edges[y << 1];
        else return x;
    }

    @Override
    public int wrapY(final int x, final int y)  {
        return Math.max(0, Math.min(y, height - 1));
    }

    protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                              double landMod, double heatMod, int stateA, int stateB)
    {
        boolean fresh = false;
        if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
        {
            minHeight = Double.POSITIVE_INFINITY;
            maxHeight = Double.NEGATIVE_INFINITY;
            minHeightActual = Double.POSITIVE_INFINITY;
            maxHeightActual = Double.NEGATIVE_INFINITY;
            minHeat0 = Double.POSITIVE_INFINITY;
            maxHeat0 = Double.NEGATIVE_INFINITY;
            minHeat1 = Double.POSITIVE_INFINITY;
            maxHeat1 = Double.NEGATIVE_INFINITY;
            minHeat = Double.POSITIVE_INFINITY;
            maxHeat = Double.NEGATIVE_INFINITY;
            minWet0 = Double.POSITIVE_INFINITY;
            maxWet0 = Double.NEGATIVE_INFINITY;
            minWet = Double.POSITIVE_INFINITY;
            maxWet = Double.NEGATIVE_INFINITY;
            cacheA = stateA;
            cacheB = stateB;
            fresh = true;
        }
        rng.setState(stateA, stateB);
        long seedA = rng.nextLong(), seedB = rng.nextLong(), seedC = rng.nextLong();
        int t;

        landModifier = (landMod <= 0) ? rng.nextDouble(0.2) + 0.91 : landMod;
        heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

        double p,
                ps, pc,
                qs, qc,
                h, temp, yPos, xPos,
                z, th, lon, lat,
                rx = width * 0.5, hw = width * 0.5, root2 = Math.sqrt(2.0),
                irx = 1.0 / rx, iry = 2.0 / (double) height,
                xAdj, yAdj,
                i_uw = usedWidth / (double)(width),
                i_uh = usedHeight / (double)(height);

        yPos = (startY - height * 0.5);
        for (int y = 0; y < height; y++, yPos += i_uh) {
            boolean inSpace = true;
            yAdj = yPos * iry;
            xPos = (startX - hw);
            for (int x = 0; x < width; x++, xPos += i_uw) {
                xAdj = xPos * irx;
                z = Math.sqrt(1.0 - 0.5 * xAdj * xAdj - 0.5 * yAdj * yAdj);
                th = z * yAdj * root2;
                lon = 2.0 * TrigTools.atan2((2.0 * z * z - 1.0), (z * xAdj * root2));
                if(th != th || lon < 0.0) {
                    heightCodeData[x][y] = 10000;
                    inSpace = true;
                    continue;
                }
                lat = TrigTools.asin(th);
                qc = TrigTools.cos(lat);
                qs = th;
                th = Math.PI - lon + centerLongitude;
                if(inSpace)
                {
                    inSpace = false;
                    edges[y << 1] = x;
                }
                edges[y << 1 | 1] = x;
                ps = TrigTools.sin(th) * qc;
                pc = TrigTools.cos(th) * qc;
                xPositions[x][y] = pc;
                yPositions[x][y] = ps;
                zPositions[x][y] = qs;
                heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                        ps, qs, seedA) + landModifier - 1.0);
                heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                + 0.375 * otherRidged.getNoiseWithSeed(pc, ps, qs,seedB + seedC)
                        , qs, seedB));
                moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qs
                                + 0.375 * otherRidged.getNoiseWithSeed(pc, ps, qs, seedC + seedA)
                        , seedC));
                minHeightActual = Math.min(minHeightActual, h);
                maxHeightActual = Math.max(maxHeightActual, h);
                if(fresh) {
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
        double  heatDiff = 0.8 / (maxHeat0 - minHeat0),
                wetDiff = 1.0 / (maxWet0 - minWet0),
                hMod,
                halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
        yPos = startY + i_uh;
        ps = Double.POSITIVE_INFINITY;
        pc = Double.NEGATIVE_INFINITY;

        for (int y = 0; y < height; y++, yPos += i_uh) {
            temp = Math.abs(yPos - halfHeight) * i_half;
            temp *= (2.4 - temp);
            temp = 2.2 - temp;
            for (int x = 0; x < width; x++) {
                h = heightData[x][y];
                if(heightCodeData[x][y] == 10000) {
                    heightCodeData[x][y] = 1000;
                    continue;
                }
                else
                    heightCodeData[x][y] = (t = codeHeight(h));
                hMod = 1.0;
                switch (t) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        h = 0.4;
                        hMod = 0.2;
                        break;
                    case 6:
                        h = -0.1 * (h - forestLower - 0.08);
                        break;
                    case 7:
                        h *= -0.25;
                        break;
                    case 8:
                        h *= -0.4;
                        break;
                    default:
                        h *= 0.05;
                }
                heatData[x][y] = (h = (((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6) * temp);
                if (fresh) {
                    ps = Math.min(ps, h); //minHeat0
                    pc = Math.max(pc, h); //maxHeat0
                }
            }
        }
        if(fresh)
        {
            minHeat1 = ps;
            maxHeat1 = pc;
        }
        heatDiff = heatModifier / (maxHeat1 - minHeat1);
        qs = Double.POSITIVE_INFINITY;
        qc = Double.NEGATIVE_INFINITY;
        ps = Double.POSITIVE_INFINITY;
        pc = Double.NEGATIVE_INFINITY;


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
        if(fresh)
        {
            minHeat = qs;
            maxHeat = qc;
            minWet = ps;
            maxWet = pc;
        }
        landData.refill(heightCodeData, 4, 999);
    }
}

