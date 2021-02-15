package com.github.yellowstonegames.world;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.MathTools;
import com.github.yellowstonegames.core.TrigTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Can be used to generate world maps with a wide variety of data, starting with height, temperature and moisture.
 * From there, you can determine biome information in as much detail as your game needs, with default implementations
 * available; one assigns a single biome to each cell based on heat/moisture, and the other gives a gradient between two
 * biome types for every cell. The maps this produces with StretchMap are valid for spherical world projections,
 * while the maps from {@link TilingWorldMap} are for toroidal world projections and will wrap from edge to opposite edge
 * seamlessly thanks to <a href="https://www.gamedev.net/blog/33/entry-2138456-seamless-noise/">a technique from the
 * Accidental Noise Library</a> that involves getting a 2D slice of 4D Simplex noise. Because of how Simplex noise
 * works, this also allows extremely high zoom levels for all types of map as long as certain parameters are within
 * reason. Other world maps produce more conventional shapes, like SpaceViewMap and RotatingSpaceMap
 * make a view of a marble-like world from space, and others make more unconventional shapes, like EllipticalMap,
 * which forms a 2:1 ellipse shape that accurately keeps sizes but not relative shapes,
 * RoundSideMap, which forms a pill-shape, and HyperellipticalMap, which takes parameters so it can fit
 * any shape between a circle or ellipse and a rectangle (the default is a slightly squared-off ellipse). You can access
 * the height map with the {@link #heightData} field, the heat map with the {@link #heatData} field, the moisture map
 * with the {@link #moistureData} field, and a special map that stores ints representing the codes for various ranges of
 * elevation (0 to 8 inclusive, with 0 the deepest ocean and 8 the highest mountains) with {@link #heightCodeData}. The
 * last map should be noted as being the simplest way to find what is land and what is water; any height code 4 or
 * greater is land, and any height code 3 or less is water.
 * <br>
 * Biome mapping is likely to need customization per-game, but some good starting points are {@link BiomeMapper.SimpleBiomeMapper},
 * which stores one biome per cell, and {@link BiomeMapper.DetailedBiomeMapper}, which gives each cell a midway value between two
 * biomes.
 */
public abstract class WorldMapGenerator implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width, height;
    public long seedA;
    public long seedB;
    public long cacheA;
    public long cacheB;
    public LaserRandom rng;
    public final double[][] heightData, heatData, moistureData;
    public final Region landData;
    public final int[][] heightCodeData;
    public double landModifier = -1.0, heatModifier = 1.0,
            minHeight = Double.POSITIVE_INFINITY, maxHeight = Double.NEGATIVE_INFINITY,
            minHeightActual = Double.POSITIVE_INFINITY, maxHeightActual = Double.NEGATIVE_INFINITY,
            minHeat = Double.POSITIVE_INFINITY, maxHeat = Double.NEGATIVE_INFINITY,
            minWet = Double.POSITIVE_INFINITY, maxWet = Double.NEGATIVE_INFINITY;
    protected double centerLongitude;

    public int zoom, startX, startY, usedWidth, usedHeight;
    protected IntList startCacheX = new IntList(8), startCacheY = new IntList(8);
    protected int zoomStartX, zoomStartY;

    /**
     * A Noise that has a higher frequency than that class defaults to, which is useful for maps here. With the
     * default Noise frequency of 1f/32f, the maps this produces are giant blurs.
     * <br>
     * Even though this is a Noise and so technically can be edited, that seems to have issues when there's more
     * than one WorldMapGenerator that uses this field. So you can feel free to use this as a Noise when
     * generators need one, but don't change it too much, if at all.
     */
    public static final Noise DEFAULT_NOISE = new Noise(0x1337CAFE, 1f, Noise.HONEY_FRACTAL, 1);

    /**
     * Used to implement most of the copy constructor for subclasses; this cannot copy Noise implementations and leaves
     * that up to the subclass, but will copy all non-static fields defined in WorldMapGenerator from other.
     * @param other a WorldMapGenerator (subclass) to copy fields from
     */
    protected WorldMapGenerator(WorldMapGenerator other) {
        width = other.width;
        height = other.height;
        usedWidth = other.usedWidth;
        usedHeight = other.usedHeight;
        landModifier = other.landModifier;
        heatModifier = other.heatModifier;
        minHeat = other.minHeat;
        maxHeat = other.maxHeat;
        minHeight = other.minHeight;
        maxHeight = other.maxHeight;
        minHeightActual = other.minHeightActual;
        maxHeightActual = other.maxHeightActual;
        minWet = other.minWet;
        maxWet = other.maxWet;
        centerLongitude = other.centerLongitude;
        zoom = other.zoom;
        startX = other.startX;
        startY = other.startY;
        startCacheX.addAll(other.startCacheX);
        startCacheY.addAll(other.startCacheY);
        zoomStartX = other.zoomStartX;
        zoomStartY = other.zoomStartY;
        seedA = other.seedA;
        seedB = other.seedB;
        cacheA = other.cacheA;
        cacheB = other.cacheB;
        rng = other.rng.copy();
        heightData = ArrayTools.copy(other.heightData);
        heatData = ArrayTools.copy(other.heatData);
        moistureData = ArrayTools.copy(other.moistureData);
        landData = other.landData.copy();
        heightCodeData = ArrayTools.copy(other.heightCodeData);
    }

    /**
     * Gets the longitude line the map is centered on, which should usually be between 0 and 2 * PI.
     * @return the longitude line the map is centered on, in radians from 0 to 2 * PI
     */
    public double getCenterLongitude() {
        return centerLongitude;
    }

    /**
     * Sets the center longitude line to a longitude measured in radians, from 0 to 2 * PI. Positive arguments will be
     * corrected with modulo, but negative ones may not always act as expected, and are strongly discouraged.
     * @param centerLongitude the longitude to center the map projection on, from 0 to 2 * PI (can be any non-negative double).
     */
    public void setCenterLongitude(double centerLongitude) {
        this.centerLongitude = centerLongitude % 6.283185307179586;
    }

    public static final double
            deepWaterLower = -1.0, deepWaterUpper = -0.7,        // 0
            mediumWaterLower = -0.7, mediumWaterUpper = -0.3,    // 1
            shallowWaterLower = -0.3, shallowWaterUpper = -0.1,  // 2
            coastalWaterLower = -0.1, coastalWaterUpper = 0.02,   // 3
            sandLower = 0.02, sandUpper = 0.12,                   // 4
            grassLower = 0.14, grassUpper = 0.35,                // 5
            forestLower = 0.35, forestUpper = 0.6,               // 6
            rockLower = 0.6, rockUpper = 0.8,                    // 7
            snowLower = 0.8, snowUpper = 1.0;                    // 8

    protected static double removeExcess(double radians)
    {
        radians *= 0.6366197723675814;
        final int floor = (radians >= 0.0 ? (int) radians : (int) radians - 1);
        return (radians - (floor & -2) - ((floor & 1) << 1)) * (Math.PI);
//        if(radians < -Math.PI || radians > Math.PI)
//            System.out.println("UH OH, radians produced: " + radians);
//        if(Math.random() < 0.00001)
//            System.out.println(radians);
//        return radians;

    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, long, long)}).
     * Always makes a 256x256 map. If you were using {@link WorldMapGenerator#WorldMapGenerator(long, int, int)}, then
     * this would be the same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256}.
     */
    protected WorldMapGenerator()
    {
        this(0x1337BABE1337D00DL, 256, 256);
    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, long, long)}).
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
     * height of the map cannot be changed after the fact, but you can zoom in.
     *
     * @param mapWidth the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    protected WorldMapGenerator(int mapWidth, int mapHeight)
    {
        this(0x1337BABE1337D00DL, mapWidth, mapHeight);
    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, long, long)}).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
     * The width and height of the map cannot be changed after the fact, but you can zoom in.
     *
     * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
     * @param mapWidth the width of the map(s) to generate; cannot be changed later
     * @param mapHeight the height of the map(s) to generate; cannot be changed later
     */
    protected WorldMapGenerator(long initialSeed, int mapWidth, int mapHeight)
    {
        width = mapWidth;
        height = mapHeight;
        usedWidth = width;
        usedHeight = height;
        seedA = initialSeed;
        seedB = initialSeed + 0xC6BC279692B5C323L | 1L;
        cacheA = ~seedA;
        cacheB = ~seedB;
        rng = new LaserRandom(seedA, seedB);
        heightData = new double[width][height];
        heatData = new double[width][height];
        moistureData = new double[width][height];
        landData = new Region(width, height);
        heightCodeData = new int[width][height];

//        riverData = new Region(width, height);
//        lakeData = new Region(width, height);
//        partialRiverData = new Region(width, height);
//        partialLakeData = new Region(width, height);
//        workingData = new Region(width, height);
    }

    /**
     * Generates a world using a random RNG state and all parameters randomized.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     */
    public void generate()
    {
        generate(rng.nextLong(), rng.nextLong() | 1L);
    }

    /**
     * Generates a world using the specified RNG state as a long. Other parameters will be randomized, using the same
     * RNG state to start with.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param stateA the first part of state to give this generator's RNG; if the whole state is the same as the last call, this will reuse data
     * @param stateB the second part of state to give this generator's RNG; if the whole state is the same as the last call, this will reuse data
     */
    public void generate(long stateA, long stateB) {
        generate(-1.0, -1.0, stateA, stateB | 1L);
    }

    /**
     * Generates a world using the specified RNG state as a long, with specific land and heat modifiers that affect
     * the land-water ratio and the average temperature, respectively.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param landMod 1.0 is Earth-like, less than 1 is more-water, more than 1 is more-land; a random value will be used if this is negative
     * @param heatMod 1.125 is Earth-like, less than 1 is cooler, more than 1 is hotter; a random value will be used if this is negative
     * @param stateA the first part of state to give this generator's RNG; if the whole state is the same as the last call, this will reuse data
     * @param stateB the second part of state to give this generator's RNG; if the whole state is the same as the last call, this will reuse data
     */
    public void generate(double landMod, double heatMod, long stateA, long stateB)
    {
        if(cacheA != stateA || cacheB != (stateB | 1L) ||
                landMod != landModifier || heatMod != heatModifier)
        {
            seedA = stateA;
            seedB = stateB | 1L;
            zoom = 0;
            startCacheX.clear();
            startCacheY.clear();
            startCacheX.add(0);
            startCacheY.add(0);
            zoomStartX = width >> 1;
            zoomStartY = height >> 1;

        }
        //System.out.printf("generate, zoomStartX: %d, zoomStartY: %d\n", zoomStartX, zoomStartY);

        regenerate(startX = (zoomStartX >> zoom) - (width >> 1 + zoom), startY = (zoomStartY >> zoom) - (height >> 1 + zoom),
                //startCacheX.peek(), startCacheY.peek(),
                usedWidth = (width >> zoom), usedHeight = (height >> zoom), landMod, heatMod, seedA, seedB);
    }

    /**
     * Halves the resolution of the map and doubles the area it covers; the 2D arrays this uses keep their sizes. This
     * version of zoomOut always zooms out from the center of the currently used area.
     * <br>
     * Only has an effect if you have previously zoomed in using {@link #zoomIn(int, int, int)} or its overload.
     */
    public void zoomOut()
    {
        zoomOut(1, width >> 1, height >> 1);
    }
    /**
     * Halves the resolution of the map and doubles the area it covers repeatedly, halving {@code zoomAmount} times; the
     * 2D arrays this uses keep their sizes. This version of zoomOut allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then coordinates should be between 0 and
     * 255, and will refer to the currently used area and not necessarily the full world size).
     * <br>
     * Only has an effect if you have previously zoomed in using {@link #zoomIn(int, int, int)} or its overload.
     * @param zoomCenterX the center X position to zoom out from; if too close to an edge, this will stop moving before it would extend past an edge
     * @param zoomCenterY the center Y position to zoom out from; if too close to an edge, this will stop moving before it would extend past an edge
     */
    public void zoomOut(int zoomAmount, int zoomCenterX, int zoomCenterY)
    {
        zoomAmount = Math.min(zoom, zoomAmount);
        if(zoomAmount == 0) return;
        if(zoomAmount < 0) {
            zoomIn(-zoomAmount, zoomCenterX, zoomCenterY);
            return;
        }
        if(zoom > 0)
        {
            if(cacheA != seedA || cacheB != seedB)
            {
                generate(rng.nextLong(), rng.nextLong() | 1L);
            }
            zoomStartX = Math.min(Math.max(
                    (zoomStartX + (zoomCenterX - (width >> 1))) >> zoomAmount,
                    width >> 1), (width << zoom - zoomAmount) - (width >> 1));
            zoomStartY = Math.min(Math.max(
                    (zoomStartY + (zoomCenterY - (height >> 1))) >> zoomAmount,
                    height >> 1), (height << zoom - zoomAmount) - (height >> 1));
//            System.out.printf("zoomOut, zoomStartX: %d, zoomStartY: %d\n", zoomStartX, zoomStartY);
            zoom -= zoomAmount;
            startCacheX.pop();
            startCacheY.pop();
            startCacheX.add(Math.min(Math.max(startCacheX.pop() + (zoomCenterX >> zoom + 1) - (width >> zoom + 2),
                    0), width - (width >> zoom)));
            startCacheY.add(Math.min(Math.max(startCacheY.pop() + (zoomCenterY >> zoom + 1) - (height >> zoom + 2),
                    0), height - (height >> zoom)));
//            zoomStartX = Math.min(Math.max((zoomStartX >> 1) + (zoomCenterX >> zoom + 1) - (width >> zoom + 2),
//                    0), width - (width >> zoom));
//            zoomStartY = Math.min(Math.max((zoomStartY >> 1) + (zoomCenterY >> zoom + 1) - (height >> zoom + 2),
//                    0), height - (height >> zoom));
            regenerate(startX = (zoomStartX >> zoom) - (width >> zoom + 1), startY = (zoomStartY >> zoom) - (height >> zoom + 1),
                    //startCacheX.peek(), startCacheY.peek(),
                    usedWidth = width >> zoom,  usedHeight = height >> zoom,
                    landModifier, heatModifier, cacheA, cacheB);
            rng.setState(cacheA, cacheB);
        }

    }
    /**
     * Doubles the resolution of the map and halves the area it covers; the 2D arrays this uses keep their sizes. This
     * version of zoomIn always zooms in to the center of the currently used area.
     * <br>
     * Although there is no technical restriction on maximum zoom, zooming in more than 5 times (64x scale or greater)
     * will make the map appear somewhat less realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     */
    public void zoomIn()
    {
        zoomIn(1, width >> 1, height >> 1);
    }
    /**
     * Doubles the resolution of the map and halves the area it covers repeatedly, doubling {@code zoomAmount} times;
     * the 2D arrays this uses keep their sizes. This version of zoomIn allows you to specify where the zoom should be
     * centered, using the current coordinates (if the map size is 256x256, then coordinates should be between 0 and
     * 255, and will refer to the currently used area and not necessarily the full world size).
     * <br>
     * Although there is no technical restriction on maximum zoom, zooming in more than 5 times (64x scale or greater)
     * will make the map appear somewhat less realistic due to rounded shapes appearing more bubble-like and less like a
     * normal landscape.
     * @param zoomCenterX the center X position to zoom in to; if too close to an edge, this will stop moving before it would extend past an edge
     * @param zoomCenterY the center Y position to zoom in to; if too close to an edge, this will stop moving before it would extend past an edge
     */
    public void zoomIn(int zoomAmount, int zoomCenterX, int zoomCenterY)
    {
        if(zoomAmount == 0) return;
        if(zoomAmount < 0)
        {
            zoomOut(-zoomAmount, zoomCenterX, zoomCenterY);
            return;
        }
        if(seedA != cacheA || seedB != cacheB)
        {
            generate(rng.nextLong(), rng.nextLong() | 1L);
        }
        zoomStartX = Math.min(Math.max(
                (zoomStartX + zoomCenterX - (width >> 1) << zoomAmount),
                width >> 1), (width << zoom + zoomAmount) - (width >> 1));
//        int oldZoomY = zoomStartY;
        zoomStartY = Math.min(Math.max(
                (zoomStartY + zoomCenterY - (height >> 1) << zoomAmount),
                height >> 1), (height << zoom + zoomAmount) - (height >> 1));
//        System.out.printf("zoomIn, zoomStartX: %d, zoomStartY: %d, oldZoomY: %d, unedited: %d, upperCap: %d\n", zoomStartX, zoomStartY,
//                oldZoomY, (oldZoomY + zoomCenterY - (height >> 1) << zoomAmount), (height << zoom + zoomAmount) - (height >> 1));
        zoom += zoomAmount;
        if(startCacheX.isEmpty())
        {
            startCacheX.add(0);
            startCacheY.add(0);
        }
        else {
            startCacheX.add(Math.min(Math.max(startCacheX.peek() + (zoomCenterX >> zoom - 1) - (width >> zoom + 1),
                    0), width - (width >> zoom)));
            startCacheY.add(Math.min(Math.max(startCacheY.peek() + (zoomCenterY >> zoom - 1) - (height >> zoom + 1),
                    0), height - (height >> zoom)));
        }
        regenerate(startX = (zoomStartX >> zoom) - (width >> 1 + zoom), startY = (zoomStartY >> zoom) - (height >> 1 + zoom),
                //startCacheX.peek(), startCacheY.peek(),
                usedWidth = width >> zoom, usedHeight = height >> zoom,
                landModifier, heatModifier, cacheA, cacheB);
        rng.setState(cacheA, cacheB);
    }

    protected abstract void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                       double landMod, double heatMod, long stateA, long stateB);
    /**
     * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
     * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
     * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
     * the projection for a given lat-lon coordinate, this returns null. The default implementation always returns null.
     * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
     * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
     * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
     * @param latitude the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
     * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
     * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
     */
    public Coord project(double latitude, double longitude)
    {
        return null;
    }

    public int codeHeight(final double high)
    {
        if(high < deepWaterUpper)
            return 0;
        if(high < mediumWaterUpper)
            return 1;
        if(high < shallowWaterUpper)
            return 2;
        if(high < coastalWaterUpper)
            return 3;
        if(high < sandUpper)
            return 4;
        if(high < grassUpper)
            return 5;
        if(high < forestUpper)
            return 6;
        if(high < rockUpper)
            return 7;
        return 8;
    }
    public int wrapX(final int x, final int y)  {
        return (x + width) % width;
    }
    public int wrapY(final int x, final int y)  {
        return (y + height) % height;
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that distorts the map as it nears the poles, expanding the
     * smaller-diameter latitude lines in extreme north and south regions so they take up the same space as the equator.
     * Like all of the WorldMapGenerator implementations, this generator allows configuring a {@link Noise}, which is
     * used for most of the generation. This type of map is ideal for projecting onto a 3D
     * sphere, which could squash the poles to counteract the stretch this does. You might also want to produce an oval
     * map that more-accurately represents the changes in the diameter of a latitude line on a spherical world; you
     * should use EllipticalMap for this.
     * HyperellipticalMap is also a nice option because it can project onto a shape between a
     * rectangle (like this class) and an ellipse (like EllipticalMap), with all-round sides.
     * <a href="http://yellowstonegames.github.io/SquidLib/SphereWorld.png" >Example map</a>.
     */
    public static class StretchMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Always makes a 256x128 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link StretchMap#StretchMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1.0}.
         */
        public StretchMap() {
            this(0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public StretchMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public StretchMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
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
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public StretchMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public StretchMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise#instance}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public StretchMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];


            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }
        @Override
        public int wrapY(final int x, final int y)  {
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
         * @param latitude the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
         * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
         * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
         */
        @Override
        public Coord project(double latitude, double longitude) {
            int x = (int)((((longitude - getCenterLongitude()) + 12.566370614359172) % 6.283185307179586) * 0.15915494309189535 * width),
                    y = (int)((TrigTools.sin(latitude) * 0.5 + 0.5) * height);
            return Coord.get(
                    wrapX(x, y),
                    wrapY(x, y));
        }

        /**
         * Copies the StretchMap {@code other} to construct a new one that is exactly the same. References will only be
         * shared to Noise classes.
         * @param other a StretchMap to copy
         */
        public StretchMap(StretchMap other)
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
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
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

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 6.283185307179586 / width, i_h = 2.0 / (height+2.0),//(3.141592653589793) / (height+2.0),
                    xPos = startX, yPos, i_uw = usedWidth / (double)width, i_uh = usedHeight * i_h / (height+2.0);
            final double[] trigTable = new double[width << 1];
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w + centerLongitude;
                // 0.7978845608028654 1.2533141373155001
                trigTable[x<<1]   = TrigTools.sin(p);// * 1.2533141373155001;
                trigTable[x<<1|1] = TrigTools.cos(p);// * 0.7978845608028654;
            }
            yPos = startY * i_h + i_uh;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                qs = -1 + yPos;//-1.5707963267948966 + yPos;
                qc = TrigTools.cos(TrigTools.asin(qs));
                for (int x = 0, xt = 0; x < width; x++) {
                    ps = trigTable[xt++] * qc;//TrigTools.sin(p);
                    pc = trigTable[xt++] * qc;//TrigTools.cos(p);
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
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - 1.0);
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
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

    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto an ellipse that should be
     * twice as wide as it is tall (although you can stretch it by width and height that don't have that ratio).
     * This uses the <a href="https://en.wikipedia.org/wiki/Mollweide_projection">Mollweide projection</a>.
     * <a href="http://yellowstonegames.github.io/SquidLib/EllipseWorld.png" >Example map</a>.
     */
    public static class EllipticalMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
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
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link EllipticalMap#EllipticalMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0}.
         */
        public EllipticalMap() {
            this(0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise
         * implementation to use is {@link Noise#instance}.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in.  Noise will be the fastest 3D generator to use for
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
        public EllipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];

            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }

        /**
         * Copies the EllipticalMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other an EllipticalMap to copy
         */
        public EllipticalMap(EllipticalMap other)
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
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
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
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thx, thy, lon, lat, ipi = 0.99999 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                thx = TrigTools.asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / TrigTools.cos(thx);
                thy = thx * 2.0;
                lat = TrigTools.asin((thy + TrigTools.sin(thy)) * ipi);

                qc = TrigTools.cos(lat);
                qs = TrigTools.sin(lat);

                boolean inSpace = true;
                xPos = startX;
                for (int x = 0; x < width; x++, xPos += i_uw) {
                    th = lon * (xPos - hw);
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
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

    /**
     * An unusual map generator that imitates an existing map (such as a map of Earth, which it can do by default). It
     * uses the Mollweide projection (an elliptical map projection, the same as what EllipticalMap uses) for both its
     * input and output; <a href="https://yellowstonegames.github.io/SquidLib/MimicWorld.png">an example can be seen here</a>,
     * imitating Earth using a 512x256 world map as a Region for input.
     */
    public static class MimicMap extends EllipticalMap
    {
        private static final long serialVersionUID = 1L;
        public Region earth;
        public Region shallow;
        public Region coast;
        public Region earthOriginal;
        /**
         * Constructs a concrete WorldMapGenerator for a map that should look like Earth using an elliptical projection
         * (specifically, a Mollweide projection).
         * Always makes a 512x256 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link MimicMap#MimicMap(long, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, DEFAULT_NOISE, 1.0}.
         */
        public MimicMap() {
            this(0x1337BABE1337D00DL
                    , DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
         * you call {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         */
        public MimicMap(Region toMimic) {
            this(0x1337BABE1337D00DL, toMimic,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed and the Region containing land positions. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         */
        public MimicMap(long initialSeed, Region toMimic) {
            this(initialSeed, toMimic, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the Region containing land positions, and a multiplier that affects the level
         * of detail by increasing or decreasing the number of octaves of noise used. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public MimicMap(long initialSeed, Region toMimic, double octaveMultiplier) {
            this(initialSeed, toMimic, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the Region containing land positions, and parameters for noise generation (a
         * {@link Noise} implementation, which is usually {@link Noise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact. Both Noise
         * and Noise make sense to use for {@code noiseGenerator}, and the seed it's constructed with doesn't matter
         * because this will change the seed several times at different scales of noise (it's fine to use the static
         * {@link Noise#instance} or {@link Noise#instance} because they have no changing state between runs
         * of the program). Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
         */
        public MimicMap(long initialSeed, Region toMimic, Noise noiseGenerator) {
            this(initialSeed, toMimic, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the Region containing land positions, parameters for noise generation (a
         * {@link Noise} implementation, which is usually {@link Noise#instance}, and a multiplier on how many
         * octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers producing even more
         * detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
         * cannot be changed after the fact.  Noise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public MimicMap(long initialSeed, Region toMimic, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
            earth = toMimic;
            earthOriginal = earth.copy();
            coast   = earth.copy().not().fringe(2);
            shallow = earth.copy().fringe(2);
        }

        /**
         * Stores a 512x256 Region that shows an Earth map with elliptical (Mollweide) projection, in a format
         * that can be read back with {@link Region#decompress(String)}. By using Region's compression,
         * this takes up a lot less room than it would with most text-based formats, and even beats uncompressed binary
         * storage of the map by a factor of 5.8. The map data won't change here, so this should stay compatible.
         */
        public static final String EARTH_ENCODED = "Ƥ䊅⑃л䢤㱢ġ٤Ȩࠪ捘Ϝᇌᐰࡐဣ₈䁭âŴനð᱀ᄣϢĢሢŤ\u0087倰…䇍Ĉథ䠨䇰ᢐ࠲ࠨ¨I䗴₧≠だₐ‴䞑ห攠℡ı©ý 傠ʓᤣ窠猡ᄡᤣഡढ愠祒㈡戢ቴ摊╓̀⫀ᄡ亨䜅ਁ灑㢠扠㏁唼⽪䐡㘢ᄡ碠䑅♁儰\u0EA8ӤϻıĒ䁤\u0A7E〨ᠪ䔲ੂƲ您䀪⁝Ꭱ䮦⁄Њǰᅺ兎⊱๚䮯㞵⌱祥䆠ౠ↤\u2E75সסˀӄઃق※瀣ǥ呔²Œ屚ౠ⿈ᄦ䄸ˤűᕱÈ䁁ݕ㮱ࠥ否噂ő<ɠኰ`⤠䍠⸳Ӱᒛ ᠪ䐝⬬֨台䑆ื䊈摤အᐡ祰ᾳ䠠樢ぞĩ昫䋮♡⊓攔㲫₧䆨※ࠨ䉒Ṗࡊ孱⡠Ằܨ㔐湖⍰ᇊ֦٠灆䣝ཤĄ䂦\u2BE3Ƒ⪱ؤ掴ᅺ‥㠯䌙⮘⢯≓䅴፧▪ᄬͼ㊀མ㍹䍁刲⋹䋨掞䭠⁅Ỽᱭ䨡䲠ℶᧄđ\u0090怼䠤向⡍ڪ䋕ڊ佈䈱ޔ 䊱⮛ࣷ䔙:†涽ࢴ䵂ಀ愠Š⪠瘮囼䉐Ԭρ䧂ᡕ䞩y⁊ࠡ䨠㵞⚯灱僋㕰جɁ&⁕㓄eĂ厠䒓ᆶ㏣ᵢô䁘〩㈥⁃\u0E5C䧀ᆝ┽ឨ嚤ᴶ䗣䌰\u173Dᔄǅ$⇷[㏋ᤰ̴İȬ䈠䩀∨Ź¨အ䬡楩้ུ\u0ABA偒\u0A4A济Ђf‡С㪀⮱Ƞ彠⸨懚㦄焐෨䄠⧀ᒠ昤အ™穝䄻尮⠲ၠฤԴ琧吅ै䋱Yࠩ所䀡⠬䒈…吧̚ȴw\u2064\u0E66\u0096瀴䕡E倠䏁䀷ࠨ䄠Ḁᔤ\u0084䀨\u2062歵䮡灱ॢॴ*ၠՒԕT䠡欀㲡䈰䐺⤜摱₹ࢽ朆睐抈笹悜ȼ䝂䀺ᠭⴘܣ㔰҈ć\u0084ƤA䂄檢b 抸䨳⏠禡Ű㜳䀤ᰲ䁩扚С㒠稻႐䀻⠠ₔ‸ᆢ珎ӑ㬭ⷒ㨻䇚ഥჼ\u202DⰤ窵ㄧ眀塀䈼ᘣᢨဠآၠªѡE〱ȠN∨»¼҃㨭ဠ䄡灅ओ繶愊倷ޢ\u0092ᠾ䕹2瀫ኌ∰2Ġ❀×₠നྠ㓞ښѠښڐ曞掁湧́<倳Ƽ͡Җ䀳㠥僨⚪രጨ攠◌ტ抨န硆ᄦᛈဠ㌣⭤ࠢ流ࡀ͔ƄȲ揸䀢堮Ӡ壩ⴘㅘ䄠㝀ᔤâ䁅ୁ㕓仲噄泩戬ू噱♌㋿ٓ㜯࣪㒑٣⩥䢢\u009D恂ǋ棎ө㛁嚅⟓䂀ᗍ勥῀ʬɈ䲡卪卐䐰廼䋀᮲噰䀷䠤ℨ㨨砱㭀煐ٰⷤፗ愠㽀ᄤc䂐⡠獑䠣熡࠴枤䩀ȴΪ\u2D29ێ咨䋍ล戵ⱼ䝬Ⲫ䂀ೃʙ䐰ŊxḨÛºᔥ䂠ỰФᒕ丫㎻㏪桬ମ㈉Â⡎ޠⰄཐ梨『ဤ䝼࠸䴀䳓⠱慠\u0B98ЊṂ瑡⾀⊰䐧¿ᡌ灁\u2438徵-〱ᤰ俿煈崊㏀Ҭ䢆祡\u2D76䐺愎朰W8ԩᠡ䄞ԑ垰䋀㷵⺇Ӡ䀥㘥媄˰䎯棡暼匳ǆဠ㤠Ⓕ沋ἡ䤯恁㊴彬ƶ䘇梉戁Ῡ䐭嶱ㅕ䆰⍁ᢅᚐク䄈Ԩ㸳ǌᡀ㡀Ͼ3ᳲ¦≠捁呪䅐⭭擘ཀ䐡खᢠ䴡䮙\u18AC\u2060ՄȢっ獍Ϡ䌡Ẻ䇡罥樜劺偐⭈ᨄJ怤ᅐ‰⡓☨縊壎䓡ᨢᄠް\u0A49ᤰǘₐ䍠娿䴨䦠⠼ؠ㸈¢!倫\u2060⏰㩂ПᖃЦ㔰ࡈ¹䁝匒ⴠ㠰䂤尻ㅬ幤ࠫၺ↦\u0560τʀ⽀၆ᓰस㉢ఴ絪⤚䢪K〱ĠᅰÁ夿䵤ဣ媡劬䇓䡩縦ˣބ⽠дǢ⢦㔼䯀牥↿洍࠰\u009Dòပᤲ䳹ŃⰦ⧤Ḧ㲔Өᘅ⣂ɼ\u0A29儠ǘఢ挰ť\u0092᩵⠰Ø䀤Ս⬭屔⺲6࠭㴱崠碲䃰‡⨠ࡊ\u202D…ᘣ⋂䈠ᇀಊC戮䣑ဣ羡࠰Ò䄘\u0B59派䃮唢I瀿䎝ୱ療÷ᙥⳞ吠祀ᄤ;怿ͪ㥬y恀☠☨ᡂ5倩₱\u0984ᴧ砭⫃䑵Ӱ歌榚◠㜦䁘რ⻈劳\u1CA5ࡠ\u07BAŚ⾥穌䙀ㆥᢍ桠儣⍁᧡Әႈ尮᭪〞⅁䂙Ӱᤡᠸ\u0382瀴ᅯᙣ桠ьGᵁ睺煒榯䁵䄱\u0BBC㶜ጢ扩䁴榤摠ƺቀ⑨အ呠ଇ䠸ρᏩ䅸塂呧ͬਐ၊䲘፣䠺䨝ᄯ汯 ⶁ劄ᴧ猢جΧૡࠠ⺠ፉ䂠ϼ؞学撐䵃ⴹ恀\u2BF4䮅烒ࠨ⭬灰䀥␤\u2060۬ˀᎃ䑥Ӑ⅏゚ᛠ到č$ౢത秆ಠᗎࢹ⌤\u2060ö̂灳恄➦䐧傚Ⓚ嬹ᬲᄣ纥⛀Ⴠ稬ʮ溮䊫ฺ砥䢤啂倢⒠䆅羐♱ྷ淮Ȁුロ⟘⮌媚枧籠畴䐫ĕ戻ٰj䄈\u0ADC䆰Ԡࠐੈ໕Ġᴈਙ害素ரཔ爸ᠴ㖂剈䍬⍄E偘䊢畤桯\u08C0㎫䱤ঀᠰ䴠ᯨڈ稱ǆ⥡灔柔ണ㬧䉩䰨/䁾◬咨ý灠ጶħҠٴЊ竉烈̰᰾悋¢=࠱᧐㡈瑀º[ᐇ䝧㈑㍑擃䞈إচⶑ妷慈ဣŁ࠰Ĩ䁺ॠ㳢㳨ဠ甠☵⌈ᄢ3ဨ䇾ሃ嬰䈸➬惧༖㐊䂠तզ䰫䈌䲲㕯ƨ〡䬡䶯〮Ӳ[ࠨ㵘〣\u0081⠼㰏ƢF㠫䆨ᑡ䗕←‧ᡡ₀䱮ॅ砿䋸⫧‴Α㈿Ô\u1FF0伶䊄‧务瘵㈸֒ࡔܰᾤ\u20F1䊇᳃䃸ᔢ*〪入\u1CA9瀾䡨怢帠ࠫȠⓀᬗ改坊⍄ށؤبJ\u2064ݢ⎃夦␁磣憨㢠桽ሠᭀ\u0C65⨢㕈ᔇ犖\u0EFC甡₶χ峣壄ǁ℥傅\u1CA7䢭Ȱ⤭䢱đ䲳₭䭻搲㉺㎤ᾔӮ䘌攀丑≍⫉ʼ㌲沟ᐂ灘㿠ㆦ摌Áᤞ⃮䓲娾昱⪬ĮЮ℣ᐔ١廹曀■⁼ዔ․ḡ㨕䅬⥴㫞囘⤩䡅ডୣĠ\u1778ศ༠ᆄᎅ埀Ϛþ所濗♀†弢娐㗀怢䴢ᑊ←⭡屒☪⤤ᬌ\u0D51唰ǳ¤⠡庠Ըి᪡姲娑䅢R㠪嵘\u09DB攺݂…ㅁ䙆䂜∥㧏䤭䙭⠽懠㐭嵈š  ";

        /**
         * Constructs a 512x256 elliptical world map that will use land forms with a similar shape to Earth.
         * @param initialSeed
         * @param noiseGenerator
         * @param octaveMultiplier
         */
        public MimicMap(long initialSeed, Noise noiseGenerator, double octaveMultiplier)
        {
            this(initialSeed,
                    Region.decompress(EARTH_ENCODED), noiseGenerator, octaveMultiplier);
        }

        /**
         * Copies the MimicMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a MimicMap to copy
         */
        public MimicMap(MimicMap other)
        {
            super(other);
            earth = other.earth.copy();
            earthOriginal = other.earthOriginal.copy();
            coast   = other.coast.copy();
            shallow = other.shallow.copy();
        }



        /**
         * Meant for making maps conform to the Mollweide (elliptical) projection that MimicMap uses.
         * @param rectangular A Region where "on" represents land and "off" water, using any rectangular projection
         * @return a reprojected version of {@code rectangular} that uses an elliptical projection
         */
        public static Region reprojectToElliptical(Region rectangular) {
            int width = rectangular.width, height = rectangular.height;
            Region t = new Region(width, height);
            double yPos, xPos,
                    th, thx, thy, lon, lat, ipi = 0.99999 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = -ry;
            for (int y = 0; y < height; y++, yPos++) {
                thx = TrigTools.asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / TrigTools.cos(thx);
                thy = thx * 2.0;
                lat = TrigTools.asin((thy + TrigTools.sin(thy)) * ipi);
                xPos = 0;
                for (int x = 0; x < width; x++, xPos++) {
                    th = lon * (xPos - hw);
                    if (th >= -3.141592653589793 && th <= 3.141592653589793
                            && rectangular.contains((int) ((th + 1) * hw), (int) ((lat + 1) * ry))) {
                        t.insert(x, y);
                    }
                }
            }
            return t;
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
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
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

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

            earth.remake(earthOriginal);

            if(zoom > 0)
            {
                int stx = Math.min(Math.max((zoomStartX - (width  >> 1)) / ((2 << zoom) - 2), 0), width ),
                        sty = Math.min(Math.max((zoomStartY - (height >> 1)) / ((2 << zoom) - 2), 0), height);
                for (int z = 0; z < zoom; z++) {
                    earth.zoom(stx, sty).expand8way().fray(0.5f).expand();
                }
                coast.remake(earth).not().fringe(2 << zoom).expand().fray(0.5f);
                shallow.remake(earth).fringe(2 << zoom).expand().fray(0.5f);
            }
            else
            {
                coast.remake(earth).not().fringe(2);
                shallow.remake(earth).fringe(2);
            }
            double p,
                    ps, pc,
                    qs, qc,
                    h, temp, yPos, xPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thx, thy, lon, lat, ipi = 0.99999 / Math.PI,
                    rx = width * 0.25, irx = 1.0 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;
            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {

                thx = TrigTools.asin((yPos) * iry);
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? thx : Math.PI * irx * 0.5 / TrigTools.cos(thx);
                thy = thx * 2.0;
                lat = TrigTools.asin((thy + TrigTools.sin(thy)) * ipi);

                qc = TrigTools.cos(lat);
                qs = TrigTools.sin(lat);

                boolean inSpace = true;
                xPos = startX;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    th = lon * (xPos - hw);
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
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
                    if(earth.contains(x, y))
                    {
                        h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                                ps, qs, seedA)) * 0.85;
                        if(coast.contains(x, y))
                            h += 0.05;
                        else
                            h += 0.15;
                    }
                    else
                    {
                        h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(pc + terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                                ps, qs, seedA)) * -0.9;
                        if(shallow.contains(x, y))
                            h = (h - 0.08) * 0.375;
                        else
                            h = (h - 0.125) * 0.75;
                    }
                    heightData[x][y] = h;
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
            double heightDiff = 2.0 / (maxHeightActual - minHeightActual),
                    heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / (halfHeight);
            double minHeightActual0 = minHeightActual;
            double maxHeightActual0 = maxHeightActual;
            yPos = startY + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.pow(Math.abs(yPos - halfHeight) * i_half, 1.5);
                temp *= (2.4 - temp);
                temp = 2.2 - temp;
                for (int x = 0; x < width; x++) {
//                    heightData[x][y] = (h = (heightData[x][y] - minHeightActual) * heightDiff - 1.0);
//                    minHeightActual0 = Math.min(minHeightActual0, h);
//                    maxHeightActual0 = Math.max(maxHeightActual0, h);
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
    /**
     * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
     * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). This uses an
     * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
     * the latitude always at the equator.
     * <a href="http://yellowstonegames.github.io/SquidLib/SpaceViewMap.png" >Example map, showing circular shape as if viewed
     * from afar</a>
     */
    public static class SpaceViewMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Always makes a 100x100 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link SpaceViewMap#SpaceViewMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1.0}.
         */
        public SpaceViewMap() {
            this(0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public SpaceViewMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
        public SpaceViewMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];

            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }

        /**
         * Copies the SpaceViewMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a SpaceViewMap to copy
         */
        public SpaceViewMap(SpaceViewMap other)
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
        public int wrapX(int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            return Math.max(edges[y << 1], Math.min(x, edges[y << 1 | 1]));
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        //private static final double root2 = Math.sqrt(2.0), inverseRoot2 = 1.0 / root2, halfInverseRoot2 = 0.5 / root2;

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, long stateA, long stateB)
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
                    h, temp, yPos, xPos, iyPos, ixPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon, lat, rho,
                    rx = width * 0.5, irx = i_uw / rx,
                    ry = height * 0.5, iry = i_uh / ry;

            yPos = startY - ry;
            iyPos = yPos / ry;
            for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {

                boolean inSpace = true;
                xPos = startX - rx;
                ixPos = xPos / rx;
                for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                    rho = Math.sqrt(ixPos * ixPos + iyPos * iyPos);
                    if(rho > 1.0) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th = TrigTools.asin(rho); // c
                    lat = TrigTools.asin(iyPos);
                    lon = centerLongitude + TrigTools.atan2(ixPos * rho, rho * TrigTools.cos(th));

                    qc = TrigTools.cos(lat);
                    qs = TrigTools.sin(lat);

                    pc = TrigTools.cos(lon) * qc;
                    ps = TrigTools.sin(lon) * qc;

                    xPositions[x][y] = pc;
                    yPositions[x][y] = ps;
                    zPositions[x][y] = qs;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(pc +
                                    terrain.getNoiseWithSeed(pc, ps, qs,seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
//                    heightData[x][y] = (h = terrain4D.getNoiseWithSeed(pc, ps, qs,
//                            (terrainLayered.getNoiseWithSeed(pc, ps, qs, seedB - seedA)
//                                    + terrain.getNoiseWithSeed(pc, ps, qs, seedC - seedB)) * 0.5,
//                            seedA) * landModifier);
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
    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto a shape with a flat top
     * and bottom but near-circular sides. This is an equal-area projection, like EllipticalMap, so effects that fill
     * areas on a map like PoliticalMapper will fill (almost) equally on any part of the map. This has less
     * distortion on the far left and far right edges of the map than EllipticalMap, but the flat top and bottom are
     * probably very distorted in a small area near the poles.
     * This uses the <a href="https://en.wikipedia.org/wiki/Eckert_IV_projection">Eckert IV projection</a>.
     * <a href="https://yellowstonegames.github.io/SquidLib/RoundSideWorldMap.png">Example map</a>
     */
    public static class RoundSideMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
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
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link RoundSideMap#RoundSideMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0}.
         */
        public RoundSideMap() {
            this(0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public RoundSideMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise
         * implementation to use is {@link Noise#instance}
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
        public RoundSideMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];

            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }

        /**
         * Copies the RoundSideMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a RoundSideMap to copy
         */
        public RoundSideMap(RoundSideMap other)
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
                                  double landMod, double heatMod, long stateA, long stateB)
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
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, thb, thx, thy, lon, lat,
                    rx = width * 0.25, irx = 1.326500428177002 / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                thy = yPos * iry;//TrigTools.sin(thb);
                thb = TrigTools.asin(thy);
                thx = TrigTools.cos(thb);
                //1.3265004 0.7538633073600218  1.326500428177002
                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? 0x1.0p100 : irx / (0.42223820031577125 * (1.0 + thx));
                qs = (thb + (thx + 2.0) * thy) * 0.2800495767557787;
                lat = TrigTools.asin(qs);

                qc = TrigTools.cos(lat);

                boolean inSpace = true;
                xPos = startX - hw;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    th = lon * xPos;
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
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
    /**
     * A concrete implementation of {@link WorldMapGenerator} that projects the world map onto a shape that resembles a
     * mix part-way between an ellipse and a rectangle. This is an equal-area projection, like EllipticalMap, so effects that fill
     * areas on a map like PoliticalMapper will fill (almost) equally on any part of the map. This has less
     * distortion around all the edges than the other maps here, especially when comparing the North and South poles
     * with RoundSideMap.
     * This uses the <a href="https://en.wikipedia.org/wiki/Tobler_hyperelliptical_projection">Tobler hyperelliptical projection</a>.
     * <a href="">Example map</a>
     */
    public static class HyperellipticalMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise terrain, heat, moisture, otherRidged, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;
        private final double alpha, kappa, epsilon;
        private final double[] Z;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Always makes a 200x100 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link HyperellipticalMap#HyperellipticalMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0}.
         * <a href="http://yellowstonegames.github.io/SquidLib/HyperellipseWorld.png" >Example map, showing special shape</a>
         */
        public HyperellipticalMap() {
            this(0x1337BABE1337D00DL, 200, 100, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public HyperellipticalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail. The suggested Noise
         * implementation to use is {@link Noise#instance}.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier){
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, octaveMultiplier, 0.0625, 2.5);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to display a projection of a globe onto an
         * ellipse without distortion of the sizes of features but with significant distortion of shape.
         * Takes an initial seed, the width/height of the map, and parameters for noise generation (a
         * {@link Noise} implementation, where {@link Noise#instance} is suggested, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
         * @param alpha one of the Tobler parameters;  0.0625 is the default and this can range from 0.0 to 1.0 at least
         * @param kappa one of the Tobler parameters; 2.5 is the default but 2.0-5.0 range values are also often used
         */
        public HyperellipticalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator,
                                  double octaveMultiplier, double alpha, double kappa){
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];
            edges = new int[height << 1];

            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);

            this.alpha = alpha;
            this.kappa = kappa;
            this.Z = new double[height << 2];
            this.epsilon = ProjectionTools.simpsonIntegrateHyperellipse(0.0, 1.0, 0.25 / height, kappa);
            ProjectionTools.simpsonODESolveHyperellipse(1, this.Z, 0.25 / height, alpha, kappa, epsilon);
        }
        /**
         * Copies the HyperellipticalMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a HyperellipticalMap to copy
         */
        public HyperellipticalMap(HyperellipticalMap other)
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
            alpha = other.alpha;
            kappa = other.kappa;
            epsilon = other.epsilon;
            Z = Arrays.copyOf(other.Z, other.Z.length);
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

        /**
         * Given a latitude and longitude in radians (the conventional way of describing points on a globe), this gets the
         * (x,y) Coord on the map projection this generator uses that corresponds to the given lat-lon coordinates. If this
         * generator does not represent a globe (if it is toroidal, for instance) or if there is no "good way" to calculate
         * the projection for a given lat-lon coordinate, this returns null. This implementation never returns null.
         * If this is a supported operation and the parameters are valid, this returns a Coord with x between 0 and
         * {@link #width}, and y between 0 and {@link #height}, both exclusive. Automatically wraps the Coord's values using
         * {@link #wrapX(int, int)} and {@link #wrapY(int, int)}.
         *
         * @param latitude  the latitude, from {@code Math.PI * -0.5} to {@code Math.PI * 0.5}
         * @param longitude the longitude, from {@code 0.0} to {@code Math.PI * 2.0}
         * @return the point at the given latitude and longitude, as a Coord with x between 0 and {@link #width} and y between 0 and {@link #height}, or null if unsupported
         */
        @Override
        public Coord project(double latitude, double longitude) {
            final double z0 = Math.abs(TrigTools.sin(latitude));
            final int i = Arrays.binarySearch(Z, z0);
            final double y;
            if (i >= 0)
                y = i/(Z.length-1.);
            else if (-i-1 >= Z.length)
                y = Z[Z.length-1];
            else
                y = ((z0-Z[-i-2])/(Z[-i-1]-Z[-i-2]) + (-i-2))/(Z.length-1.);
            final int xx = (int)(((longitude - getCenterLongitude() + 12.566370614359172) % 6.283185307179586) * Math.abs(alpha + (1-alpha)*Math.pow(1 - Math.pow(Math.abs(y),kappa), 1/kappa)) + 0.5);
            final int yy = (int)(y * Math.signum(latitude) * height * 0.5 + 0.5);
            return Coord.get(wrapX(xx, yy), wrapY(xx, yy));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, long stateA, long stateB)
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
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon,
                    rx = width * 0.5, irx = Math.PI / rx, hw = width * 0.5,
                    ry = height * 0.5, iry = 1.0 / ry;

            yPos = startY - ry;
            for (int y = 0; y < height; y++, yPos += i_uh) {
//                thy = yPos * iry;//TrigTools.sin(thb);
//                thb = asin(thy);
//                thx = TrigTools.cos(thb);
//                //1.3265004 0.7538633073600218  1.326500428177002
//                lon = (thx == Math.PI * 0.5 || thx == Math.PI * -0.5) ? 0x1.0p100 : irx / (0.42223820031577125 * (1.0 + thx));
//                qs = (thb + (thx + 2.0) * thy) * 0.2800495767557787;
//                lat = asin(qs);
//
//                qc = TrigTools.cos(lat);

                lon = TrigTools.asin(Z[(int)(0.5 + Math.abs(yPos*iry)*(Z.length-1))])*Math.signum(yPos);
                qs = TrigTools.sin(lon);
                qc = TrigTools.cos(lon);

                boolean inSpace = true;
                xPos = startX - hw;
                for (int x = 0/*, xt = 0*/; x < width; x++, xPos += i_uw) {
                    //th = lon * xPos;
                    th = xPos * irx / Math.abs(alpha + (1-alpha)*ProjectionTools.hyperellipse(yPos * iry, kappa));
                    if(th < -3.141592653589793 || th > 3.141592653589793) {
                        //if(th < -2.0 || th > 2.0) {
                        heightCodeData[x][y] = 10000;
                        inSpace = true;
                        continue;
                    }
                    if(inSpace)
                    {
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
                                    terrain.getNoiseWithSeed(pc, ps, qs, seedB - seedA) * 0.5,
                            ps, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps
                                    + 0.375 * otherRidged.getNoiseWithSeed(pc, ps, qs, seedB + seedC)
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

    /**
     * A concrete implementation of {@link WorldMapGenerator} that imitates an infinite-distance perspective view of a
     * world, showing only one hemisphere, that should be as wide as it is tall (its outline is a circle). It should
     * look as a world would when viewed from space, and implements rotation differently to allow the planet to be
     * rotated without recalculating all the data, though it cannot zoom. Note that calling
     * {@link #setCenterLongitude(double)} does a lot more work than in other classes, but less than fully calling
     * {@link #generate()} in those classes, since it doesn't remake the map data at a slightly different rotation and
     * instead keeps a single map in use the whole time, using sections of it. This uses an
     * <a href="https://en.wikipedia.org/wiki/Orthographic_projection_in_cartography">Orthographic projection</a> with
     * the latitude always at the equator; the internal map is stored as a {@link StretchMap}, which uses a
     * <a href="https://en.wikipedia.org/wiki/Cylindrical_equal-area_projection#Discussion">cylindrical equal-area
     * projection</a>, specifically the Lambert equal-area projection.
     * <br>
     * <a href="https://i.imgur.com/WNa5nQ1.gifv">Example view of a planet rotating</a>.
     * <a href="https://i.imgur.com/NV5IMd6.gifv">Another example</a>.
     */
    public static class RotatingSpaceMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final double[][] xPositions,
                yPositions,
                zPositions;
        protected final int[] edges;
        public final StretchMap storedMap;
        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Always makes a 100x100 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link RotatingSpaceMap#RotatingSpaceMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1.0}.
         */
        public RotatingSpaceMap() {
            this(0x1337BABE1337D00DL, 100, 100, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public RotatingSpaceMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to view a spherical world from space,
         * showing only one hemisphere at a time.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
        public RotatingSpaceMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[mapWidth][mapHeight];
            yPositions = new double[mapWidth][mapHeight];
            zPositions = new double[mapWidth][mapHeight];
            edges = new int[height << 1];
            storedMap = new StretchMap(initialSeed, mapWidth << 1, mapHeight, noiseGenerator, octaveMultiplier);
        }

        /**
         * Copies the RotatingSpaceMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a RotatingSpaceMap to copy
         */
        public RotatingSpaceMap(RotatingSpaceMap other)
        {
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
            storedMap = new StretchMap(other.storedMap);
        }


        @Override
        public int wrapX(int x, int y) {
            y = Math.max(0, Math.min(y, height - 1));
            return Math.max(edges[y << 1], Math.min(x, edges[y << 1 | 1]));
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        @Override
        public void setCenterLongitude(double centerLongitude) {
            super.setCenterLongitude(centerLongitude);
            int ax, ay;
            double
                    ps, pc,
                    qs, qc,
                    h, yPos, xPos, iyPos, ixPos,
                    i_uw = usedWidth / (double)width,
                    i_uh = usedHeight / (double)height,
                    th, lon, lat, rho,
                    i_pi = 1.0 / Math.PI,
                    rx = width * 0.5, irx = i_uw / rx,
                    ry = height * 0.5, iry = i_uh / ry;

            yPos = startY - ry;
            iyPos = yPos / ry;
            for (int y = 0; y < height; y++, yPos += i_uh, iyPos += iry) {
                boolean inSpace = true;
                xPos = startX - rx;
                ixPos = xPos / rx;
                lat = TrigTools.asin(iyPos);
                for (int x = 0; x < width; x++, xPos += i_uw, ixPos += irx) {
                    rho = (ixPos * ixPos + iyPos * iyPos);
                    if(rho > 1.0) {
                        heightCodeData[x][y] = 1000;
                        inSpace = true;
                        continue;
                    }
                    rho = Math.sqrt(rho);
                    if(inSpace)
                    {
                        inSpace = false;
                        edges[y << 1] = x;
                    }
                    edges[y << 1 | 1] = x;
                    th = TrigTools.asin(rho); // c
                    lon = removeExcess((centerLongitude + (TrigTools.atan2(ixPos * rho, rho * TrigTools.cos(th)))) * 0.5);

                    qs = lat * 0.6366197723675814;
                    qc = qs + 1.0;
                    int sf = (qs >= 0.0 ? (int) qs : (int) qs - 1) & -2;
                    int cf = (qc >= 0.0 ? (int) qc : (int) qc - 1) & -2;
                    qs -= sf;
                    qc -= cf;
                    qs *= 2.0 - qs;
                    qc *= 2.0 - qc;
                    qs = qs * (-0.775 - 0.225 * qs) * ((sf & 2) - 1);
                    qc = qc * (-0.775 - 0.225 * qc) * ((cf & 2) - 1);


                    ps = lon * 0.6366197723675814;
                    pc = ps + 1.0;
                    sf = (ps >= 0.0 ? (int) ps : (int) ps - 1) & -2;
                    cf = (pc >= 0.0 ? (int) pc : (int) pc - 1) & -2;
                    ps -= sf;
                    pc -= cf;
                    ps *= 2.0 - ps;
                    pc *= 2.0 - pc;
                    ps = ps * (-0.775 - 0.225 * ps) * ((sf & 2) - 1);
                    pc = pc * (-0.775 - 0.225 * pc) * ((cf & 2) - 1);

                    ax = (int)((lon * i_pi + 1.0) * width);
                    ay = (int)((qs + 1.0) * ry);

//                    // Hammer projection, not an inverse projection like we usually use
//                    z = 1.0 / Math.sqrt(1 + qc * TrigTools.cos(lon * 0.5));
//                    ax = (int)((qc * TrigTools.sin(lon * 0.5) * z + 1.0) * width);
//                    ay = (int)((qs * z + 1.0) * height * 0.5);

                    if(ax >= storedMap.width || ax < 0 || ay >= storedMap.height || ay < 0)
                    {
                        heightCodeData[x][y] = 1000;
                        continue;
                    }
                    if(storedMap.heightCodeData[ax][ay] >= 1000) // for the seam we get when looping around
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
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            if(cacheA != stateA || cacheB != stateB)// || landMod != storedMap.landModifier || coolMod != storedMap.coolingModifier)
            {
                storedMap.regenerate(0, 0, width << 1, height, landMod, heatMod, stateA, stateB);
                minHeightActual = Double.POSITIVE_INFINITY;
                maxHeightActual = Double.NEGATIVE_INFINITY;

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
    }
    /**
     * A concrete implementation of {@link WorldMapGenerator} that does no projection of the map, as if the area were
     * completely flat or small enough that curvature is impossible to see. This also does not change heat levels at the
     * far north and south regions of the map, since it is meant for areas that are all about the same heat level.
     * <a href="http://yellowstonegames.github.io/SquidLib/LocalMap.png" >Example map, showing lack of polar ice</a>
     */
    public static class LocalMap extends WorldMapGenerator {
        private static final long serialVersionUID = 1L;
        protected static final float terrainFreq = 1.45f, terrainLayeredFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        protected double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise terrain, otherRidged, heat, moisture, terrainLayered;
        public final double[][] xPositions,
                yPositions,
                zPositions;


        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Always makes a 256x128 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link LocalMap#LocalMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1.0}.
         */
        public LocalMap() {
            this(0x1337BABE1337D00DL, 256, 128, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long, long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public LocalMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
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
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, double octaveMultiplier) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise}
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used to wrap a sphere (as with a texture on a
         * 3D model), with seamless east-west wrapping, no north-south wrapping, and distortion that causes the poles to
         * have significantly-exaggerated-in-size features while the equator is not distorted.
         * Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
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
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise#instance}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMap(long initialSeed, int mapWidth, int mapHeight, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            xPositions = new double[width][height];
            yPositions = new double[width][height];
            zPositions = new double[width][height];


            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrain.setFractalType(Noise.RIDGED_MULTI);

            terrainLayered = new Noise(noiseGenerator); terrainLayered.setFrequency(terrainLayered.getFrequency() * terrainLayeredFreq * 0.325f);
            terrainLayered.setNoiseType(terrainLayered.getNoiseType() | 1); terrainLayered.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }

        /**
         * Copies the LocalMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a LocalMap to copy
         */
        public LocalMap(LocalMap other)
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
        }

        @Override
        public int wrapX(final int x, final int y)  {
            return Math.max(0, Math.min(x, width - 1));
        }

        @Override
        public int wrapY(final int x, final int y)  {
            return Math.max(0, Math.min(y, height - 1));
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
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

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 1.0 / width, i_h = 1.0 / (height),  ii = Math.max(i_w, i_h),
                    i_uw = usedWidth * i_w * ii, i_uh = usedHeight * i_h * ii, xPos, yPos = startY * i_h;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                xPos = startX * i_w;
                for (int x = 0; x < width; x++, xPos += i_uw) {
                    xPositions[x][y] = xPos;
                    yPositions[x][y] = yPos;
                    zPositions[x][y] = 0.0;
                    heightData[x][y] = (h = terrainLayered.getNoiseWithSeed(xPos +
                                    terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                            yPos, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                    + 0.375 * otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                            seedB));
                    temp = 0.375 * otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

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
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
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
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6);
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
    /**
     * An unusual map generator that imitates an existing local map (such as a map of Australia, which it can do by
     * default), without applying any projection or changing heat levels in the polar regions or equator.
     * <a href="http://yellowstonegames.github.io/SquidLib/LocalMimicMap.png" >Example map, showing a variant on Australia</a>
     */
    public static class LocalMimicMap extends LocalMap
    {
        private static final long serialVersionUID = 1L;
        public Region earth;
        public Region shallow;
        public Region coast;
        public Region earthOriginal;
        /**
         * Constructs a concrete WorldMapGenerator for a map that should look like Australia, without projecting the
         * land positions or changing heat by latitude. Always makes a 256x256 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link LocalMimicMap#LocalMimicMap(long, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, DEFAULT_NOISE, 1.0}.
         */
        public LocalMimicMap() {
            this(0x1337BABE1337D00DL
                    , DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
         * The initial seed is set to the same large long every time, and it's likely that you would set the seed when
         * you call {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         */
        public LocalMimicMap(Region toMimic) {
            this(0x1337BABE1337D00DL, toMimic,  DEFAULT_NOISE,1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed and the Region containing land positions. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         */
        public LocalMimicMap(long initialSeed, Region toMimic) {
            this(initialSeed, toMimic, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed, the Region containing land positions, and a multiplier that affects the level
         * of detail by increasing or decreasing the number of octaves of noise used. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long, long)}.
         * The width and height of the map cannot be changed after the fact.
         * Uses Noise as its noise generator, with the given octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMimicMap(long initialSeed, Region toMimic, double octaveMultiplier) {
            this(initialSeed, toMimic, DEFAULT_NOISE, octaveMultiplier);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, without projecting the land positions or changing heat by latitude.
         * Takes an initial seed, the Region containing land positions, and parameters for noise generation (a
         * {@link Noise} implementation, which is usually {@link Noise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long, long)}. The width and height of the map cannot be changed after the fact. Both Noise
         * and Noise make sense to use for {@code noiseGenerator}, and the seed it's constructed with doesn't matter
         * because this will change the seed several times at different scales of noise (it's fine to use the static
         * {@link Noise#instance} or {@link Noise#instance} because they have no changing state between runs
         * of the program). Uses the given noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
         */
        public LocalMimicMap(long initialSeed, Region toMimic, Noise noiseGenerator) {
            this(initialSeed, toMimic, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that should have land in roughly the same places as the
         * given Region's "on" cells, using an elliptical projection (specifically, a Mollweide projection).
         * Takes an initial seed, the Region containing land positions, parameters for noise generation (a
         * {@link Noise} implementation, which is usually {@link Noise#instance}, and a multiplier on how many
         * octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers producing even more
         * detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long, long)}. The width and height of the map
         * cannot be changed after the fact.  Noise will be the fastest 3D generator to use for
         * {@code noiseGenerator}, and the seed it's constructed with doesn't matter because this will change the
         * seed several times at different scales of noise (it's fine to use the static {@link Noise#instance}
         * because it has no changing state between runs of the program). The {@code octaveMultiplier} parameter should
         * probably be no lower than 0.5, but can be arbitrarily high if you're willing to spend much more time on
         * generating detail only noticeable at very high zoom; normally 1.0 is fine and may even be too high for maps
         * that don't require zooming.
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param toMimic the world map to imitate, as a Region with land as "on"; the height and width will be copied
         * @param noiseGenerator an instance of a noise generator capable of 3D noise, usually {@link Noise} or {@link Noise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public LocalMimicMap(long initialSeed, Region toMimic, Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, toMimic.width, toMimic.height, noiseGenerator, octaveMultiplier);
            earth = toMimic;
            earthOriginal = earth.copy();
            coast   = earth.copy().not().fringe(2);
            shallow = earth.copy().fringe(2);
        }
        /**
         * Stores a 256x256 Region that shows an unprojected map of Australia, in a format that can be read back
         * with {@link Region#decompress(String)}. By using Region's compression, this takes up a lot less
         * room than it would with most text-based formats, and even beats uncompressed binary storage of the map by a
         * factor of 9.4. The map data won't change here, so this should stay compatible.
         */
        public static final String AUSTRALIA_ENCODED = "Ƥ䒅⒐᮰囨䈢ħ䐤࠰ࠨ•Ⱙအ䎢ŘňÆ䴣ȢؤF䭠゠ᔤ∠偰ഀ\u0560₠ኼܨā᭮笁\u242AЦᇅ扰रࠦ吠䠪ࠦ䠧娮⠬䠬❁Ềក\u1CAA͠敠ἒ慽Ê䄄洡儠䋻䨡㈠䙬坈མŨྈ䞻䛊哚晪⁞倰h·䡂Ļæ抂㴢္\u082E搧䈠ᇩᒠ᩠ɀ༨ʨڤʃ奲ࢠ፠ᆙả䝆䮳りĩ(ॠી᧰྄e॑ᤙ䒠剠⁌ဥࠩФΝ䂂⢴ᑠ㺀ᢣ䗨dBqÚ扜冢\u0FE5\u0A62䐠劣ေ¯䂍䞀ၰ\u0E67ᐓ〈ᄠ塠Ѡ̀ာ⠤ᡤŒęጓ憒‱〿䌳℔ᐼ䊢⁚䤿ӣ◚㙀\u0C74Ӹ抠⣀ĨǊǸ䁃း₺Ý䂁ᜤ䢑V⁄樫焠\u0A60\u2E78⎲Ĉ䁎勯戡璠悈ᠥ嘡⩩‰ನ檨㡕䶪၁@恑ࠣ䘣ࢠᅀᡎ劰桠Өॢಸ熛փࢸ䀹ఽ䅠勖ਰ۴̄ጺಢ䈠ᙠᨭ\u2FE0焠Ӡܼ䇂䒠ᯀԨĠ愜᪅䦥㶐ୀ\u09C5Ƣ*䂕ॹ∠咠р\u0604У無~⁆Г椠痠\u1CA9Ⱓס㩖ᝋ司楠२ญⳘ䬣汤ǿã㱩ᖷ掠Àݒ㑁c‾䮴,\u2452僢ᰣ缠ɋ乨\u0378䁡绑ס傓䁔瀾ሺÑ䀤ो刡开烀\u0A76Ё䈠䈰״Áj⁑䠡戢碠㘀አ䃉㪙嘈ʂø⸪௰₈㐲暤ƩDᬿ䂖剙書\u0FE0㴢\u0089㘩Ĉ䰵掀栰杁4〡Ƞ⭀\u1AE0㠰㹨Zコത\u009E䂖ࠠⴠ縣吠ᆠʡ㡀䀧否䣝Ӧ愠Ⓚ\u1CA2ಠո*①ӈԥ獀խ@㟬箬㐱\u31BE簽Ɛᩆᇞ稯禚⟶⣑аβǚ㥎Ḇ⌢㑆 搡⁗ဣ刣\u0C45䑒8怺₵⤦a5ਵ㏰ᩄ猢ฦ䬞㐷䈠呠カ愠ۀ\u1C92傠ᅼ߃ᙊ䢨ၠླྀš亀ƴ̰刷ʼ墨愠  ";

        /**
         * Constructs a 256x256 unprojected local map that will use land forms with a similar shape to Australia.
         * @param initialSeed
         * @param noiseGenerator
         * @param octaveMultiplier
         */
        public LocalMimicMap(long initialSeed, Noise noiseGenerator, double octaveMultiplier)
        {
            this(initialSeed,
                    Region.decompress(AUSTRALIA_ENCODED), noiseGenerator, octaveMultiplier);
        }

        /**
         * Copies the LocalMimicMap {@code other} to construct a new one that is exactly the same. References will only
         * be shared to Noise classes.
         * @param other a LocalMimicMap to copy
         */
        public LocalMimicMap(LocalMimicMap other)
        {
            super(other);
            earth = other.earth.copy();
            earthOriginal = other.earthOriginal.copy();
            coast   = other.coast.copy();
            shallow = other.shallow.copy();
        }



        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, long stateA, long stateB)
        {
            boolean fresh = false;
            if(cacheA != stateA || cacheB != stateB || landMod != landModifier || heatMod != heatModifier)
            {
                minHeight = Double.POSITIVE_INFINITY;
                maxHeight = Double.NEGATIVE_INFINITY;
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

            landModifier = (landMod <= 0) ? rng.nextDouble(0.29) + 0.91 : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

            earth.remake(earthOriginal);

            if(zoom > 0)
            {
                int stx = Math.min(Math.max((zoomStartX - (width  >> 1)) / ((2 << zoom) - 2), 0), width ),
                        sty = Math.min(Math.max((zoomStartY - (height >> 1)) / ((2 << zoom) - 2), 0), height);
                for (int z = 0; z < zoom; z++) {
                    earth.zoom(stx, sty).expand8way().fray(0.5f).expand();
                }
                coast.remake(earth).not().fringe(2 << zoom).expand().fray(0.5f);
                shallow.remake(earth).fringe(2 << zoom).expand().fray(0.5f);
            }
            else
            {
                coast.remake(earth).not().fringe(2);
                shallow.remake(earth).fringe(2);
            }
            double p,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 1.0 / width, i_h = 1.0 / (height),
                    i_uw = usedWidth * i_w * i_w, i_uh = usedHeight * i_h * i_h, xPos, yPos = startY * i_h;
            for (int y = 0; y < height; y++, yPos += i_uh) {
                xPos = startX * i_w;
                for (int x = 0; x < width; x++, xPos += i_uw) {
                    xPositions[x][y] = (xPos - .5) * 2.0;
                    yPositions[x][y] = (yPos - .5) * 2.0;
                    zPositions[x][y] = 0.0;

                    if(earth.contains(x, y))
                    {
                        h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                        terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                                yPos, seedA)) * 0.85;
                        if(coast.contains(x, y))
                            h += 0.05;
                        else
                            h += 0.15;
                    }
                    else
                    {
                        h = MathTools.swayTight(terrainLayered.getNoiseWithSeed(xPos +
                                        terrain.getNoiseWithSeed(xPos, yPos, seedB - seedA) * 0.5,
                                yPos, seedA)) * -0.9;
                        if(shallow.contains(x, y))
                            h = (h - 0.08) * 0.375;
                        else
                            h = (h - 0.125) * 0.75;
                    }
                    //h += landModifier - 1.0;
                    heightData[x][y] = h;
                    heatData[x][y] = (p = heat.getNoiseWithSeed(xPos, yPos
                                    + 0.375 * otherRidged.getNoiseWithSeed(xPos, yPos, seedB + seedC),
                            seedB));
                    temp = 0.375 * otherRidged.getNoiseWithSeed(xPos, yPos, seedC + seedA);
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(xPos - temp, yPos + temp, seedC));

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
                    hMod;
            yPos = startY * i_h + i_uh;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                for (int x = 0; x < width; x++) {
                    h = heightData[x][y];
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
                    heatData[x][y] = (h = ((heatData[x][y] - minHeat0) * heatDiff * hMod) + h + 0.6);
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

}
