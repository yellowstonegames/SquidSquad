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
 * while the maps from {@link TilingMap} are for toroidal world projections and will wrap from edge to opposite edge
 * seamlessly thanks to <a href="https://www.gamedev.net/blog/33/entry-2138456-seamless-noise/">a technique from the
 * Accidental Noise Library</a> that involves getting a 2D slice of 4D Simplex noise. Because of how Simplex noise
 * works, this also allows extremely high zoom levels for all types of map as long as certain parameters are within
 * reason. Other world maps produce more conventional shapes, like SpaceViewMap and RotatingSpaceMap
 * make a view of a marble-like world from space, and others make more unconventional shapes, like EllipticalMap
 * or EllipticalHammerMap, which form a 2:1 ellipse shape that accurately keeps sizes but not relative shapes,
 * RoundSideMap, which forms a pill-shape, and HyperellipticalMap, which takes parameters so it can fit
 * any shape between a circle or ellipse and a rectangle (the default is a slightly squared-off ellipse). You can access
 * the height map with the {@link #heightData} field, the heat map with the {@link #heatData} field, the moisture map
 * with the {@link #moistureData} field, and a special map that stores ints representing the codes for various ranges of
 * elevation (0 to 8 inclusive, with 0 the deepest ocean and 8 the highest mountains) with {@link #heightCodeData}. The
 * last map should be noted as being the simplest way to find what is land and what is water; any height code 4 or
 * greater is land, and any height code 3 or less is water.
 * <br>
 * Biome mapping is likely to need customization per-game, but some good starting points are {@link SimpleBiomeMapper},
 * which stores one biome per cell, and {@link DetailedBiomeMapper}, which gives each cell a midway value between two
 * biomes.
 */
public abstract class WorldMapGenerator implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width, height;
    public int seedA, seedB, cacheA, cacheB;
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
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Always makes a 256x256 map. If you were using {@link WorldMapGenerator#WorldMapGenerator(long, int, int)}, then
     * this would be the same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256}.
     */
    protected WorldMapGenerator()
    {
        this(0x1337BABE1337D00DL, 256, 256);
    }
    /**
     * Constructs a WorldMapGenerator (this class is abstract, so you should typically call this from a subclass or as
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Takes only the width/height of the map. The initial seed is set to the same large long
     * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
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
     * part of an anonymous class that implements {@link #regenerate(int, int, int, int, double, double, int, int)}).
     * Takes an initial seed and the width/height of the map. The {@code initialSeed}
     * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
        seedA = (int) (initialSeed & 0xFFFFFFFFL);
        seedB = (int) (initialSeed >>> 32);
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
        generate(rng.nextLong());
    }

    /**
     * Generates a world using the specified RNG state as a long. Other parameters will be randomized, using the same
     * RNG state to start with.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param state the state to give this generator's RNG; if the same as the last call, this will reuse data
     */
    public void generate(long state) {
        generate(-1.0, -1.0, state);
    }

    /**
     * Generates a world using the specified RNG state as a long, with specific land and heat modifiers that affect
     * the land-water ratio and the average temperature, respectively.
     * The worlds this produces will always have width and height as specified in the constructor (default 256x256).
     * You can call {@link #zoomIn(int, int, int)} to double the resolution and center on the specified area, but the width
     * and height of the 2D arrays this changed, such as {@link #heightData} and {@link #moistureData} will be the same.
     * @param landMod 1.0 is Earth-like, less than 1 is more-water, more than 1 is more-land; a random value will be used if this is negative
     * @param heatMod 1.125 is Earth-like, less than 1 is cooler, more than 1 is hotter; a random value will be used if this is negative
     * @param state the state to give this generator's RNG; if the same as the last call, this will reuse data
     */
    public void generate(double landMod, double heatMod, long state)
    {
        if(cacheA != (int) (state & 0xFFFFFFFFL) || cacheB != (int) (state >>> 32) ||
                landMod != landModifier || heatMod != heatModifier)
        {
            seedA = (int) (state & 0xFFFFFFFFL);
            seedB = (int) (state >>> 32);
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
                generate(rng.nextLong());
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
            generate(rng.nextLong());
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
                                       double landMod, double heatMod, int stateA, int stateB);
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
    protected final int decodeX(final int coded)
    {
        return coded % width;
    }
    protected final int decodeY(final int coded)
    {
        return coded / width;
    }
    public int wrapX(final int x, final int y)  {
        return (x + width) % width;
    }
    public int wrapY(final int x, final int y)  {
        return (y + height) % height;
    }

    public interface BiomeMapper
    {
        /**
         * Gets the most relevant biome code for a given x,y point on the map. Some mappers can store more than one
         * biome at a location, but only the one with the highest influence will be returned by this method. Biome codes
         * are always ints, and are typically between 0 and 60, both inclusive; they are meant to be used as indices
         * into a table of names or other objects that identify a biome, accessible via {@link #getBiomeNameTable()}.
         * Although different classes may define biome codes differently, they should all be able to be used as indices
         * into the String array returned by getBiomeNameTable().
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used as an index into the array returned by {@link #getBiomeNameTable()}
         */
        int getBiomeCode(int x, int y);

        /**
         * Gets a heat code for a given x,y point on a map, usually as an int between 0 and 5 inclusive. Some
         * implementations may use more or less detail for heat codes, but 0 is always the coldest code used, and the
         * highest value this can return for a given implementation refers to the hottest code used.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used to categorize how hot an area is, with 0 as coldest
         */
        int getHeatCode(int x, int y);
        /**
         * Gets a moisture code for a given x,y point on a map, usually as an int between 0 and 5 inclusive. Some
         * implementations may use more or less detail for moisture codes, but 0 is always the driest code used, and the
         * highest value this can return for a given implementation refers to the wettest code used. Some
         * implementations may allow seasonal change in moisture, e.g. monsoon seasons, to be modeled differently from
         * average precipitation in an area, but the default assumption is that this describes the average amount of
         * moisture (rain, humidity, and possibly snow/hail or other precipitation) that an area receives annually.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return an int that can be used to categorize how much moisture an area tends to receive, with 0 as driest
         */
        int getMoistureCode(int x, int y);

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to. A
         * sample table is in {@link SimpleBiomeMapper#biomeTable}; the 61-element array format documented for that
         * field is encouraged for implementing classes if they use 6 levels of heat and 6 levels of moisture, and track
         * rivers, coastlines, lakes, and oceans as potentially different types of terrain. Biome codes can be obtained
         * with {@link #getBiomeCode(int, int)}, or for some implementing classes other methods may provide more
         * detailed information.
         * @return a String array that often contains 61 elements, to be used with biome codes as indices.
         */
        String[] getBiomeNameTable();
        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * retrieved with {@link #getBiomeCode(int, int)} and used as indices into {@link #getBiomeNameTable()} or a
         * custom biome table.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        void makeBiomes(WorldMapGenerator world);
    }
    /**
     * A way to get biome information for the cells on a map when you only need a single value to describe a biome, such
     * as "Grassland" or "TropicalRainforest".
     * <br>
     * To use: 1, Construct a SimpleBiomeMapper (constructor takes no arguments). 2, call
     * {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has already produced at least one world map.
     * 3, get biome codes from the {@link #biomeCodeData} field, where a code is an int that can be used as an index
     * into the {@link #biomeTable} static field to get a String name for a biome type, or used with an alternate biome
     * table of your design. Biome tables in this case are 61-element arrays organized into groups of 6 elements, with
     * the last element reserved for empty space where the map doesn't cover (as with some map projections). Each
     * group goes from the coldest temperature first to the warmest temperature last in the group. The first group of 6
     * contains the dryest biomes, the next 6 are medium-dry, the next are slightly-dry, the next slightly-wet, then
     * medium-wet, then wettest. After this first block of dry-to-wet groups, there is a group of 6 for coastlines, a
     * group of 6 for rivers, a group of 6 for lakes, a group of 6 for oceans, and then one element for space outside
     * the map. The last element, with code 60, is by convention the String "Empty", but normally the code should be
     * enough to tell that a space is off-map. This also assigns moisture codes and heat codes from 0 to 5 for each
     * cell, which may be useful to simplify logic that deals with those factors.
     */
    public static class SimpleBiomeMapper implements BiomeMapper
    {
        /**
         * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest and 5 hottest.
         */
        public int[][] heatCodeData,
        /**
         * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest and 5 wettest.
         */
        moistureCodeData,
        /**
         * The biome codes for the analyzed map, from 0 to 60 inclusive. You can use {@link #biomeTable} to look up
         * String names for biomes, or construct your own table as you see fit (see docs in {@link SimpleBiomeMapper}).
         */
        biomeCodeData;

        @Override
        public int getBiomeCode(int x, int y) {
            return biomeCodeData[x][y];
        }

        @Override
        public int getHeatCode(int x, int y) {
            return heatCodeData[x][y];
        }

        @Override
        public int getMoistureCode(int x, int y) {
            return moistureCodeData[x][y];
        }

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to.
         * This table uses 6 levels of heat and 6 levels of moisture, and tracks rivers, coastlines, lakes, and oceans
         * as potentially different types of terrain. Biome codes can be obtained with {@link #getBiomeCode(int, int)}.
         * This method returns a direct reference to {@link #biomeTable}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * @return a direct reference to {@link #biomeTable}, a String array containing names of biomes
         */
        @Override
        public String[] getBiomeNameTable() {
            return biomeTable;
        }

        public static final double
                coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
                colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
                coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
                warmValueLower = 0.5,      warmValueUpper = 0.69,    // 3
                warmerValueLower = 0.69,   warmerValueUpper = 0.85,  // 4
                warmestValueLower = 0.85,  warmestValueUpper = 1.0,  // 5

        driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
                drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
                dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
                wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
                wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
                wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

        /**
         * The default biome table to use with biome codes from {@link #biomeCodeData}. Biomes are assigned based on
         * heat and moisture for the first 36 of 61 elements (coldest to warmest for each group of 6, with the first
         * group as the dryest and the last group the wettest), then the next 6 are for coastlines (coldest to warmest),
         * then rivers (coldest to warmest), then lakes (coldest to warmest), then oceans (coldest to warmest), and
         * lastly a single "biome" for empty space outside the map (meant for projections that don't fill a rectangle).
         */
        public static final String[] biomeTable = {
                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
                "Empty",                                                                                                       //SPACE
        };

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public SimpleBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and used as indices into {@link #biomeTable} or a custom biome table.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        @Override
        public void makeBiomes(WorldMapGenerator world) {
            if(world == null || world.width <= 0 || world.height <= 0)
                return;
            if(heatCodeData == null || (heatCodeData.length != world.width || heatCodeData[0].length != world.height))
                heatCodeData = new int[world.width][world.height];
            if(moistureCodeData == null || (moistureCodeData.length != world.width || moistureCodeData[0].length != world.height))
                moistureCodeData = new int[world.width][world.height];
            if(biomeCodeData == null || (biomeCodeData.length != world.width || biomeCodeData[0].length != world.height))
                biomeCodeData = new int[world.width][world.height];
            final double i_hot = (world.maxHeat == world.minHeat) ? 1.0 : 1.0 / (world.maxHeat - world.minHeat);
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {
                    final double hot = (world.heatData[x][y] - world.minHeat) * i_hot, moist = world.moistureData[x][y];
                    final int heightCode = world.heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        continue;
                    }
                    int hc, mc;
                    boolean isLake = false,// world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
                            isRiver = false;// world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
                    if(heightCode < 4) {
                        mc = 9;
                    }
                    else if (moist > wetterValueUpper) {
                        mc = 5;
                    } else if (moist > wetValueUpper) {
                        mc = 4;
                    } else if (moist > dryValueUpper) {
                        mc = 3;
                    } else if (moist > drierValueUpper) {
                        mc = 2;
                    } else if (moist > driestValueUpper) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot > warmerValueUpper) {
                        hc = 5;
                    } else if (hot > warmValueUpper) {
                        hc = 4;
                    } else if (hot > coldValueUpper) {
                        hc = 3;
                    } else if (hot > colderValueUpper) {
                        hc = 2;
                    } else if (hot > coldestValueUpper) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    biomeCodeData[x][y] = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : isLake ? hc + 48 : heightCode == 4 ? hc + 36 : hc + mc * 6;
                }
            }
        }
    }
    /**
     * A way to get biome information for the cells on a map when you want an area's biome to be a combination of two
     * main biome types, such as "Grassland" or "TropicalRainforest", with the biomes varying in weight between areas.
     * <br>
     * To use: 1, Construct a DetailedBiomeMapper (constructor takes no arguments). 2, call
     * {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has already produced at least one world map.
     * 3, get biome codes from the {@link #biomeCodeData} field, where a code is an int that can be used with the
     * extract methods in this class to get various information from it (these are {@link #extractBiomeA(int)},
     * {@link #extractBiomeB(int)}, {@link #extractPartA(int)}, {@link #extractPartB(int)}, and
     * {@link #extractMixAmount(int)}). You can get predefined names for biomes using the extractBiome methods (these
     * names can be changed in {@link #biomeTable}), or raw indices into some (usually 61-element) collection or array
     * with the extractPart methods. The extractMixAmount() method gets a float that is the amount by which biome B
     * affects biome A; if this is higher than 0.5, then biome B is the "dominant" biome in the area.
     */
    public static class DetailedBiomeMapper implements BiomeMapper
    {
        /**
         * The heat codes for the analyzed map, from 0 to 5 inclusive, with 0 coldest and 5 hottest.
         */
        public int[][] heatCodeData,
        /**
         * The moisture codes for the analyzed map, from 0 to 5 inclusive, with 0 driest and 5 wettest.
         */
        moistureCodeData,
        /**
         * The biome codes for the analyzed map, using one int to store the codes for two biomes and the degree by which
         * the second biome affects the first. These codes can be used with methods in this class like
         * {@link #extractBiomeA(int)}, {@link #extractBiomeB(int)}, and {@link #extractMixAmount(int)} to find the two
         * dominant biomes in an area, called biome A and biome B, and the mix amount, for finding how much biome B
         * affects biome A.
         */
        biomeCodeData;


        /**
         * Gets the biome code for the dominant biome at a given x,y position. This is equivalent to getting the raw
         * biome code from {@link #biomeCodeData}, calling {@link #extractMixAmount(int)} on that raw biome code, and
         * chooosing whether to call {@link #extractPartA(int)} or {@link #extractPartB(int)} based on whether the mix
         * amount is lower than 0.5 (yielding part A) or higher (yielding part B).
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return the biome code for the dominant biome part at the given location
         */
        @Override
        public int getBiomeCode(int x, int y) {
            int code = biomeCodeData[x][y];
            if(code < 0x2000000) return code & 1023;
            return (code >>> 10) & 1023;
        }

        @Override
        public int getHeatCode(int x, int y) {
            return heatCodeData[x][y];
        }

        @Override
        public int getMoistureCode(int x, int y) {
            return moistureCodeData[x][y];
        }

        /**
         * Gets a String array where biome codes can be used as indices to look up a name for the biome they refer to.
         * This table uses 6 levels of heat and 6 levels of moisture, and tracks rivers, coastlines, lakes, and oceans
         * as potentially different types of terrain. Biome codes can be obtained with {@link #getBiomeCode(int, int)}.
         * This method returns a direct reference to {@link #biomeTable}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * @return a direct reference to {@link #biomeTable}, a String array containing names of biomes
         */
        @Override
        public String[] getBiomeNameTable() {
            return biomeTable;
        }

        public static final double
                coldestValueLower = 0.0,   coldestValueUpper = 0.15, // 0
                colderValueLower = 0.15,   colderValueUpper = 0.31,  // 1
                coldValueLower = 0.31,     coldValueUpper = 0.5,     // 2
                warmValueLower = 0.5,      warmValueUpper = 0.69,     // 3
                warmerValueLower = 0.69,    warmerValueUpper = 0.85,   // 4
                warmestValueLower = 0.85,   warmestValueUpper = 1.0,  // 5

        driestValueLower = 0.0,    driestValueUpper  = 0.27, // 0
                drierValueLower = 0.27,    drierValueUpper   = 0.4,  // 1
                dryValueLower = 0.4,       dryValueUpper     = 0.6,  // 2
                wetValueLower = 0.6,       wetValueUpper     = 0.8,  // 3
                wetterValueLower = 0.8,    wetterValueUpper  = 0.9,  // 4
                wettestValueLower = 0.9,   wettestValueUpper = 1.0;  // 5

        /**
         * The default biome table to use with parts of biome codes from {@link #biomeCodeData}. Biomes are assigned by
         * heat and moisture for the first 36 of 61 elements (coldest to warmest for each group of 6, with the first
         * group as the dryest and the last group the wettest), then the next 6 are for coastlines (coldest to warmest),
         * then rivers (coldest to warmest), then lakes (coldest to warmest). The last is reserved for empty space.
         * <br>
         * Unlike with {@link SimpleBiomeMapper}, you cannot use a biome code directly from biomeCodeData as an index
         * into this in almost any case; you should pass the biome code to one of the extract methods.
         * {@link #extractBiomeA(int)} or {@link #extractBiomeB(int)} will work if you want a biome name, or
         * {@link #extractPartA(int)} or {@link #extractPartB(int)} should be used if you want a non-coded int that
         * represents one of the biomes' indices into something like this. You can also get the amount by which biome B
         * is affecting biome A with {@link #extractMixAmount(int)}.
         */
        public static final String[] biomeTable = {
                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
                "Empty",                                                                                                       //SPACE
        };

        /**
         * Gets the int stored in part A of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 60 (both inclusive), so collections this is used as an index
         * for should have a length of at least 61.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part A; almost always between 0 and 60, inclusive.
         */
        public int extractPartA(int biomeCode)
        {
            return biomeCode & 1023;
        }
        /**
         * Gets a String from {@link #biomeTable} that names the appropriate biome in part A of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part A of biomeCode, or "Empty" if none can be found
         */
        public String extractBiomeA(int biomeCode)
        {
            biomeCode &= 1023;
            if(biomeCode < 60)
                return biomeTable[biomeCode];
            return "Empty";
        }
        /**
         * Gets the int stored in part B of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 60 (both inclusive), so collections this is used as an index
         * for should have a length of at least 61.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part B; almost always between 0 and 60, inclusive.
         */
        public int extractPartB(int biomeCode)
        {
            return (biomeCode >>> 10) & 1023;
        }

        /**
         * Gets a String from {@link #biomeTable} that names the appropriate biome in part B of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part B of biomeCode, or "Ocean" if none can be found
         */
        public String extractBiomeB(int biomeCode)
        {
            biomeCode = (biomeCode >>> 10) & 1023;
            if(biomeCode < 60)
                return biomeTable[biomeCode];
            return "Empty";
        }

        /**
         * This gets the portion of a biome code that represents the amount of mixing between two biomes.
         * Biome codes are normally obtained from the {@link #biomeCodeData} field, and aren't very usable on their own
         * without calling methods like this, {@link #extractBiomeA(int)}, and {@link #extractBiomeB(int)}. This returns
         * a float between 0.0f (inclusive) and 1.0f (exclusive), with 0.0f meaning biome B has no effect on an area and
         * biome A is the only one used, 0.5f meaning biome A and biome B have equal effect, and 0.75f meaning biome B
         * has most of the effect, three-fourths of the area, and biome A has less, one-fourth of the area.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a float between 0.0f (inclusive) and 1.0f (exclusive) representing mixing of biome B into biome A
         */
        public float extractMixAmount(int biomeCode)
        {
            return (biomeCode >>> 20) * 0x1p-10f;
        }

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public DetailedBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and used with methods in this class like {@link #extractBiomeA(int)},
         * {@link #extractBiomeB(int)}, and {@link #extractMixAmount(int)} to find the two dominant biomes in an area,
         * called biome A and biome B, and the mix amount, for finding how much biome B affects biome A.
         * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
         */
        @Override
        public void makeBiomes(WorldMapGenerator world) {
            if(world == null || world.width <= 0 || world.height <= 0)
                return;
            if(heatCodeData == null || (heatCodeData.length != world.width || heatCodeData[0].length != world.height))
                heatCodeData = new int[world.width][world.height];
            if(moistureCodeData == null || (moistureCodeData.length != world.width || moistureCodeData[0].length != world.height))
                moistureCodeData = new int[world.width][world.height];
            if(biomeCodeData == null || (biomeCodeData.length != world.width || biomeCodeData[0].length != world.height))
                biomeCodeData = new int[world.width][world.height];
            final int[][] heightCodeData = world.heightCodeData;
            final double[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
            int hc, mc, heightCode, bc;
            double hot, moist, high, i_hot = 1.0 / world.maxHeat;
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {

                    heightCode = heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        continue;
                    }
                    hot = heatData[x][y];
                    moist = moistureData[x][y];
                    high = heightData[x][y];
//                    fresh = world.freshwaterData[x][y];
                    boolean isLake = false,//world.generateRivers && heightCode >= 4 && fresh > 0.65 && fresh + moist * 2.35 > 2.75,//world.partialLakeData.contains(x, y) && heightCode >= 4,
                            isRiver = false;//world.generateRivers && !isLake && heightCode >= 4 && fresh > 0.55 && fresh + moist * 2.2 > 2.15;//world.partialRiverData.contains(x, y) && heightCode >= 4;
                    if (moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2)) {
                        mc = 5;
                    } else if (moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2)) {
                        mc = 4;
                    } else if (moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2)) {
                        mc = 3;
                    } else if (moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2)) {
                        mc = 2;
                    } else if (moist >= (drierValueUpper - (driestValueUpper) * 0.2)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                        hc = 4;
                    } else if (hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                        hc = 3;
                    } else if (hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                        hc = 2;
                    } else if (hot >= (colderValueUpper - (coldestValueUpper) * 0.2) * i_hot) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    bc = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : isLake ? hc + 48 : heightCode == 4 ? hc + 36 : hc + mc * 6;

                    if(heightCode < 4) {
                        mc = 9;
                    }
                    else if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2)) {
                        mc = 5;
                    } else if (moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2)) {
                        mc = 4;
                    } else if (moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2)) {
                        mc = 3;
                    } else if (moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2)) {
                        mc = 2;
                    } else if (moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2) * i_hot) {
                        hc = 4;
                    } else if (hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2) * i_hot) {
                        hc = 3;
                    } else if (hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2) * i_hot) {
                        hc = 2;
                    } else if (hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2) * i_hot) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    bc |= (hc + mc * 6) << 10;
                    if(heightCode < 4)
                        biomeCodeData[x][y] = bc | (int)((heightData[x][y] + 1.0) * 1000.0) << 20;
                    else biomeCodeData[x][y] = bc | (int) ((heightCode == 4)
                            ? (sandUpper - high) * 10240.0 // multiplier affected by changes to sandLower
                            : MathTools.sway((high + moist) * (4.1 + high - hot)) * 512 + 512) << 20;
                }
            }
        }
    }

    /**
     * A concrete implementation of {@link WorldMapGenerator} that tiles both east-to-west and north-to-south. It tends
     * to not appear distorted like StretchMap does in some areas, even though this is inaccurate for a
     * rectangular projection of a spherical world (that inaccuracy is likely what players expect in a map, though).
     * You may want LocalMap instead, for non-world maps that don't tile.
     * <a href="http://yellowstonegames.github.io/SquidLib/DetailedWorldMapDemo.png" >Example map</a>.
     */
    public static class TilingMap extends WorldMapGenerator {
        protected static final float terrainFreq = 0.95f, terrainRidgedFreq = 2.6f, heatFreq = 2.1f, moistureFreq = 2.125f, otherFreq = 3.375f;
        private double minHeat0 = Double.POSITIVE_INFINITY, maxHeat0 = Double.NEGATIVE_INFINITY,
                minHeat1 = Double.POSITIVE_INFINITY, maxHeat1 = Double.NEGATIVE_INFINITY,
                minWet0 = Double.POSITIVE_INFINITY, maxWet0 = Double.NEGATIVE_INFINITY;

        public final Noise terrain, terrainRidged, heat, moisture, otherRidged;

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Always makes a 256x256 map.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         * If you were using {@link TilingMap#TilingMap(long, int, int, Noise, double)}, then this would be the
         * same as passing the parameters {@code 0x1337BABE1337D00DL, 256, 256, WorldMapGenerator.DEFAULT_NOISE, 1.0}.
         */
        public TilingMap() {
            this(0x1337BABE1337D00DL, 256, 256, WorldMapGenerator.DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south.
         * Takes only the width/height of the map. The initial seed is set to the same large long
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
         * height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param mapWidth  the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         */
        public TilingMap(int mapWidth, int mapHeight) {
            this(0x1337BABE1337D00DL, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south.
         * Takes an initial seed and the width/height of the map. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
         * The width and height of the map cannot be changed after the fact, but you can zoom in.
         * Uses Noise as its noise generator, with 1.0 as the octave multiplier affecting detail.
         *
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth    the width of the map(s) to generate; cannot be changed later
         * @param mapHeight   the height of the map(s) to generate; cannot be changed later
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight) {
            this(initialSeed, mapWidth, mapHeight, DEFAULT_NOISE, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Takes an initial seed, the width/height of the map, and a noise generator (a
         * {@link Noise} implementation, which is usually {@link Noise#instance}. The {@code initialSeed}
         * parameter may or may not be used, since you can specify the seed to use when you call
         * {@link #generate(long)}. The width and height of the map cannot be changed after the fact, but you can zoom
         * in. Any seed supplied to the Noise given to this (if it takes one) will be ignored, and
         * {@link Noise#getNoiseWithSeed(double, double, double, double, long)} will be used to specify the seed many
         * times. The detail level, which is the {@code octaveMultiplier} parameter that can be passed to another
         * constructor, is always 1.0 with this constructor.
         *
         * @param initialSeed      the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth         the width of the map(s) to generate; cannot be changed later
         * @param mapHeight        the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator   an instance of a noise generator capable of 4D noise, recommended to be {@link Noise#instance}
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight, final Noise noiseGenerator) {
            this(initialSeed, mapWidth, mapHeight, noiseGenerator, 1.0);
        }

        /**
         * Constructs a concrete WorldMapGenerator for a map that can be used as a tiling, wrapping east-to-west as well
         * as north-to-south. Takes an initial seed, the width/height of the map, and parameters for noise
         * generation (a {@link Noise} implementation, which is usually {@link Noise#instance}, and a
         * multiplier on how many octaves of noise to use, with 1.0 being normal (high) detail and higher multipliers
         * producing even more detailed noise when zoomed-in). The {@code initialSeed} parameter may or may not be used,
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
         * cannot be changed after the fact, but you can zoom in. Any seed supplied to the Noise given to this (if it takes one) will be ignored, and
         * {@link Noise#getNoiseWithSeed(double, double, double, double, long)} will be used to specify the seed many
         * times. The {@code octaveMultiplier} parameter should probably be no lower than 0.5, but can be arbitrarily
         * high if you're willing to spend much more time on generating detail only noticeable at very high zoom;
         * normally 1.0 is fine and may even be too high for maps that don't require zooming.
         * @param initialSeed the seed for the LaserRandom this uses; this may also be set per-call to generate
         * @param mapWidth the width of the map(s) to generate; cannot be changed later
         * @param mapHeight the height of the map(s) to generate; cannot be changed later
         * @param noiseGenerator an instance of a noise generator capable of 4D noise, almost always {@link Noise}
         * @param octaveMultiplier used to adjust the level of detail, with 0.5 at the bare-minimum detail and 1.0 normal
         */
        public TilingMap(long initialSeed, int mapWidth, int mapHeight, final Noise noiseGenerator, double octaveMultiplier) {
            super(initialSeed, mapWidth, mapHeight);
            terrain = new Noise(noiseGenerator); terrain.setFrequency(terrain.getFrequency() * terrainFreq);
            terrain.setNoiseType(terrain.getNoiseType() | 1); terrain.setFractalOctaves((int)(0.5 + octaveMultiplier * 8));
            terrain.setFractalLacunarity(0.5f); terrain.setFractalGain(2f);

            terrainRidged = new Noise(noiseGenerator); terrainRidged.setFrequency(terrainRidged.getFrequency() * terrainRidgedFreq);
            terrainRidged.setNoiseType(terrainRidged.getNoiseType() | 1); terrainRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 10));
            terrainRidged.setFractalType(Noise.RIDGED_MULTI);

            heat = new Noise(noiseGenerator); heat.setFrequency(heat.getFrequency() * heatFreq);
            heat.setNoiseType(heat.getNoiseType() | 1); heat.setFractalOctaves((int)(0.5 + octaveMultiplier * 3));
            heat.setFractalLacunarity(0.5f); heat.setFractalGain(2f);

            moisture = new Noise(noiseGenerator); moisture.setFrequency(moisture.getFrequency() * moistureFreq);
            moisture.setNoiseType(moisture.getNoiseType() | 1); moisture.setFractalOctaves((int)(0.5 + octaveMultiplier * 4));
            moisture.setFractalLacunarity(0.5f); moisture.setFractalGain(2f);

            otherRidged = new Noise(noiseGenerator); otherRidged.setFrequency(otherRidged.getFrequency() * otherFreq);
            otherRidged.setNoiseType(otherRidged.getNoiseType() | 1); otherRidged.setFractalOctaves((int)(0.5 + octaveMultiplier * 6));
            otherRidged.setFractalType(Noise.RIDGED_MULTI);
        }

        /**
         * Copies the TilingMap {@code other} to construct a new one that is exactly the same. References will only be
         * shared to Noise classes.
         * @param other a TilingMap to copy
         */
        public TilingMap(TilingMap other)
        {
            super(other);
            terrain = other.terrain;
            terrainRidged = other.terrainRidged;
            heat = other.heat;
            moisture = other.moisture;
            otherRidged = other.otherRidged;
            minHeat0 = other.minHeat0;
            maxHeat0 = other.maxHeat0;
            minHeat1 = other.minHeat1;
            maxHeat1 = other.maxHeat1;
            minWet0 = other.minWet0;
            maxWet0 = other.maxWet0;
        }

        protected void regenerate(int startX, int startY, int usedWidth, int usedHeight,
                                  double landMod, double heatMod, int stateA, int stateB)
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

            landModifier = (landMod <= 0) ? rng.nextDouble(0.1875) + 0.99 : landMod;
            heatModifier = (heatMod <= 0) ? rng.nextDouble(0.45) * (rng.nextDouble()-0.5) + 1.1 : heatMod;

            double p, q,
                    ps, pc,
                    qs, qc,
                    h, temp,
                    i_w = 6.283185307179586 / width, i_h = 6.283185307179586 / height,
                    xPos = startX, yPos = startY, i_uw = usedWidth / (double)width, i_uh = usedHeight / (double)height;
            double[] trigTable = new double[width << 1];
            for (int x = 0; x < width; x++, xPos += i_uw) {
                p = xPos * i_w;
                trigTable[x<<1]   = TrigTools.sin(p);
                trigTable[x<<1|1] = TrigTools.cos(p);
            }
            for (int y = 0; y < height; y++, yPos += i_uh) {
                q = yPos * i_h;
                qs = TrigTools.sin(q);
                qc = TrigTools.cos(q);
                for (int x = 0, xt = 0; x < width; x++) {
                    ps = trigTable[xt++];//TrigTools.sin(p);
                    pc = trigTable[xt++];//TrigTools.cos(p);
                    heightData[x][y] = (h = terrain.getNoiseWithSeed(pc +
                                    terrainRidged.getNoiseWithSeed(pc, ps, qc, qs,seedB - seedA) * 0.25,
                            ps, qc, qs, seedA) + landModifier - 1.0);
                    heatData[x][y] = (p = heat.getNoiseWithSeed(pc, ps, qc
                                    + 0.375 * otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedB + seedC)
                            , qs, seedB));
                    moistureData[x][y] = (temp = moisture.getNoiseWithSeed(pc, ps, qc, qs
                                    + 0.375 * otherRidged.getNoiseWithSeed(pc, ps, qc, qs, seedC + seedA)
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
            double
                    heatDiff = 0.8 / (maxHeat0 - minHeat0),
                    wetDiff = 1.0 / (maxWet0 - minWet0),
                    hMod,
                    halfHeight = (height - 1) * 0.5, i_half = 1.0 / halfHeight;
            yPos = startY;
            ps = Double.POSITIVE_INFINITY;
            pc = Double.NEGATIVE_INFINITY;

            for (int y = 0; y < height; y++, yPos += i_uh) {
                temp = Math.abs(yPos - halfHeight) * i_half;
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
     * A concrete implementation of {@link WorldMapGenerator} that distorts the map as it nears the poles, expanding the
     * smaller-diameter latitude lines in extreme north and south regions so they take up the same space as the equator.
     * Like all of the WorldMapGenerator implementations, this generator allows configuring a {@link Noise}, which is
     * used for most of the generation. This type of map is ideal for projecting onto a 3D
     * sphere, which could squash the poles to counteract the stretch this does. You might also want to produce an oval
     * map that more-accurately represents the changes in the diameter of a latitude line on a spherical world; you
     * should use EllipticalMap or EllipticalHammerMap for this.
     * HyperellipticalMap is also a nice option because it can project onto a shape between a
     * rectangle (like this class) and an ellipse (like EllipticalMap), with all-round sides.
     * <a href="http://yellowstonegames.github.io/SquidLib/SphereWorld.png" >Example map</a>.
     */
    public static class StretchMap extends WorldMapGenerator {
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
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
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
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
                                  double landMod, double heatMod, int stateA, int stateB)
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
         * every time, and it's likely that you would set the seed when you call {@link #generate(long)}. The width and
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
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
         * parameter may or may not be used, since you can specify the seed to use when you call {@link #generate(long)}.
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
         * since you can specify the seed to use when you call {@link #generate(long)}. The width and height of the map
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
                                  double landMod, double heatMod, int stateA, int stateB)
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


}
