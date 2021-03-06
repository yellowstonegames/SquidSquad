package com.github.yellowstonegames.world;

import com.github.yellowstonegames.core.MathTools;
import com.github.yellowstonegames.place.Biome;

/**
 * Provides a way to assign biomes to points on a world or area map, and retrieve those biomes or the heat/moisture
 * levels those biomes depend on. After constructing a BiomeMapper and generating a world with a
 * {@link WorldMapGenerator}, you can call {@link #makeBiomes(WorldMapGenerator)} to assign the biomes for that world.
 * After that, you can call {@link #getBiomeCode(int, int)} to get the primary biome code for an area; more specialized
 * methods or fields may be available in implementations.
 */
public interface BiomeMapper {
    /**
     * Gets the most relevant biome code for a given x,y point on the map. Some mappers can store more than one
     * biome at a location, but only the one with the highest influence will be returned by this method. Biome codes
     * are always ints, and are typically between 0 and 60, both inclusive; they are meant to be used as indices
     * into a table of names or other objects that identify a biome, accessible via {@link #getBiomeTable()}.
     * Although different classes may define biome codes differently, they should all be able to be used as indices
     * into the String array returned by getBiomeNameTable().
     *
     * @param x the x-coordinate on the map
     * @param y the y-coordinate on the map
     * @return an int that can be used as an index into the array returned by {@link #getBiomeTable()}
     */
    int getBiomeCode(int x, int y);

    /**
     * Gets a heat code for a given x,y point on a map, usually as an int between 0 and 5 inclusive. Some
     * implementations may use more or less detail for heat codes, but 0 is always the coldest code used, and the
     * highest value this can return for a given implementation refers to the hottest code used.
     *
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
     *
     * @param x the x-coordinate on the map
     * @param y the y-coordinate on the map
     * @return an int that can be used to categorize how much moisture an area tends to receive, with 0 as driest
     */
    int getMoistureCode(int x, int y);

    /**
     * Gets a {@link Biome} array where biome codes can be used as indices to look up a name for the biome they refer
     * to. It's common for implementations to use the existing table in {@link Biome#TABLE}; the 66-element array format
     * that uses is encouraged for implementing classes if they use 6 levels of heat and 6 levels of moisture, and track
     * rivers, coastlines, lakes, and oceans as potentially different types of terrain. Biome codes can be obtained
     * with {@link #getBiomeCode(int, int)}, or for some implementing classes other methods may provide more
     * detailed information.
     *
     * @return a Biome array that often contains 66 elements, to be used with biome codes as indices.
     */
    Biome[] getBiomeTable();

    /**
     * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
     * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
     * retrieved with {@link #getBiomeCode(int, int)} and used as indices into {@link #getBiomeTable()} or a
     * custom biome table.
     *
     * @param world a WorldMapGenerator that should have generated at least one map; it may be at any zoom
     */
    void makeBiomes(WorldMapGenerator world);

    /**
     * A way to get biome information for the cells on a map when you only need a single value to describe a biome, such
     * as "Grassland" or "TropicalRainforest".
     * <br>
     * To use: 1, Construct a SimpleBiomeMapper (constructor takes no arguments). 2, call
     * {@link #makeBiomes(WorldMapGenerator)} with a WorldMapGenerator that has already produced at least one world map.
     * 3, get biome codes from the {@link #biomeCodeData} field, where a code is an int that can be used as an index
     * into {@link Biome#TABLE} to get a Biome instance, or used with an alternate Biome table of your design. Biome
     * tables in this case are 66-element arrays organized into groups of 6 elements, with the last 6 elements reserved
     * for unusual areas, like 60 for empty space where the map doesn't cover. Each group goes from the coldest
     * temperature first to the warmest temperature last in the group. The first group of 6
     * contains the dryest biomes, the next 6 are medium-dry, the next are slightly-dry, the next slightly-wet, then
     * medium-wet, then wettest. After this first block of dry-to-wet groups, there is a group of 6 for coastlines, a
     * group of 6 for rivers, a group of 6 for lakes, a group of 6 for oceans, and then one element for space outside
     * the map. The last 6 elements, starting with code 60, lead with the String "Empty", and the rest after that are
     * not expected to be encountered randomly on the surface of a world. This also assigns moisture codes and heat
     * codes from 0 to 5 for each cell, which may be useful to simplify logic that deals with those factors.
     */
    class SimpleBiomeMapper implements BiomeMapper
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
         * The biome codes for the analyzed map, from 0 to 65 inclusive. You can use {@link Biome#TABLE} to look up
         * corresponding Biomes, or construct your own table as you see fit (see docs in {@link SimpleBiomeMapper}).
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
         * This method returns a direct reference to {@link Biome#TABLE}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * @return a direct reference to {@link Biome#TABLE}, a String array containing names of biomes
         */
        @Override
        public Biome[] getBiomeTable() {
            return Biome.TABLE;
        }

        public static final float
                coldestValueLower = 0.0f,   coldestValueUpper = 0.15f, // 0
                colderValueLower = 0.15f,   colderValueUpper = 0.31f,  // 1
                coldValueLower = 0.31f,     coldValueUpper = 0.5f,     // 2
                warmValueLower = 0.5f,      warmValueUpper = 0.69f,    // 3
                warmerValueLower = 0.69f,   warmerValueUpper = 0.85f,  // 4
                warmestValueLower = 0.85f,  warmestValueUpper = 1.0f,  // 5

        driestValueLower = 0.0f,    driestValueUpper  = 0.27f, // 0
                drierValueLower = 0.27f,    drierValueUpper   = 0.4f,  // 1
                dryValueLower = 0.4f,       dryValueUpper     = 0.6f,  // 2
                wetValueLower = 0.6f,       wetValueUpper     = 0.8f,  // 3
                wetterValueLower = 0.8f,    wetterValueUpper  = 0.9f,  // 4
                wettestValueLower = 0.9f,   wettestValueUpper = 1.0f;  // 5

        /**
         * The default biome table to use with biome codes from {@link #biomeCodeData}. Biomes are assigned based on
         * heat and moisture for the first 36 of 66 elements (coldest to warmest for each group of 6, with the first
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
                "Empty",  "Moon",         "Cavern",         "Cavern",              "Exotic",             "Volcano"             //STRANGE
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
         * taken from {@link #biomeCodeData} and used as indices into {@link Biome#TABLE} or a custom biome table.
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
            final float i_hot = (world.maxHeat == world.minHeat) ? 1f : 1f / (world.maxHeat - world.minHeat);
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {
                    final float hot = (world.heatData[x][y] - world.minHeat) * i_hot, moist = world.moistureData[x][y];
                    final int heightCode = world.heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        continue;
                    }
                    int hc, mc;
                    boolean isLake = false;
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
     * {@link #extractMixAmount(int)}). You can get predefined Biomes using the extractBiome methods, or raw indices
     * into some (usually 66-element) collection or array with the extractPart methods. The extractMixAmount() method
     * gets a float that is the amount by which biome B affects biome A; if this is higher than 0.5f, then biome B is the
     * "dominant" biome in the area.
     */
    class DetailedBiomeMapper implements BiomeMapper
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
         * amount is lower than 0.5f (yielding part A) or higher (yielding part B).
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
         * This method returns a direct reference to {@link Biome#TABLE}, so modifying the returned array is discouraged
         * (you should implement {@link BiomeMapper} using this class as a basis if you want to change its size).
         * <br>
         * Unlike with {@link SimpleBiomeMapper}, you cannot use a biome code directly from biomeCodeData as an index
         * into this in almost any case; you should pass the biome code to one of the extract methods.
         * {@link #extractBiomeA(int)} or {@link #extractBiomeB(int)} will work if you want a biome name, or
         * {@link #extractPartA(int)} or {@link #extractPartB(int)} should be used if you want a non-coded int that
         * represents one of the biomes' indices into something like this. You can also get the amount by which biome B
         * is affecting biome A with {@link #extractMixAmount(int)}.
         * @return a direct reference to {@link Biome#TABLE}, a Biome array with 66 items
         */
        @Override
        public Biome[] getBiomeTable() {
            return Biome.TABLE;
        }

        public static final float
                coldestValueLower = 0.0f,   coldestValueUpper = 0.15f, // 0
                colderValueLower = 0.15f,   colderValueUpper = 0.31f,  // 1
                coldValueLower = 0.31f,     coldValueUpper = 0.5f,     // 2
                warmValueLower = 0.5f,      warmValueUpper = 0.69f,     // 3
                warmerValueLower = 0.69f,    warmerValueUpper = 0.85f,   // 4
                warmestValueLower = 0.85f,   warmestValueUpper = 1.0f,  // 5

        driestValueLower = 0.0f,    driestValueUpper  = 0.27f, // 0
                drierValueLower = 0.27f,    drierValueUpper   = 0.4f,  // 1
                dryValueLower = 0.4f,       dryValueUpper     = 0.6f,  // 2
                wetValueLower = 0.6f,       wetValueUpper     = 0.8f,  // 3
                wetterValueLower = 0.8f,    wetterValueUpper  = 0.9f,  // 4
                wettestValueLower = 0.9f,   wettestValueUpper = 1.0f;  // 5
        /**
         * Gets the int stored in part A of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 65 (both inclusive), so collections this is used as an index
         * for should have a length of at least 66.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part A; almost always between 0 and 65, inclusive.
         */
        public int extractPartA(int biomeCode)
        {
            return biomeCode & 1023;
        }
        /**
         * Gets a String from {@link Biome#TABLE} that names the appropriate biome in part A of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part A of biomeCode, or "Empty" if none can be found
         */
        public Biome extractBiomeA(int biomeCode)
        {
            biomeCode &= 1023;
            if(biomeCode < 66)
                return Biome.TABLE[biomeCode];
            return Biome.TABLE[60];
        }
        /**
         * Gets the int stored in part B of the given biome code, which can be used as an index into other collections.
         * This int should almost always range from 0 to 65 (both inclusive), so collections this is used as an index
         * for should have a length of at least 66.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return an int stored in the biome code's part B; almost always between 0 and 65, inclusive.
         */
        public int extractPartB(int biomeCode)
        {
            return (biomeCode >>> 10) & 1023;
        }

        /**
         * Gets a String from {@link Biome#TABLE} that names the appropriate biome in part B of the given biome code.
         * @param biomeCode a biome code that was probably received from {@link #biomeCodeData}
         * @return a String that names the biome in part B of biomeCode, or "Ocean" if none can be found
         */
        public Biome extractBiomeB(int biomeCode)
        {
            biomeCode = (biomeCode >>> 10) & 1023;
            if(biomeCode < 66)
                return Biome.TABLE[biomeCode];
            return Biome.TABLE[60];
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
            final float[][] heatData = world.heatData, moistureData = world.moistureData, heightData = world.heightData;
            int hc, mc, heightCode, bc;
            float hot, moist, high, i_hot = 1f / world.maxHeat;
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
                    boolean isLake = false;
                    if (moist >= (wettestValueUpper - (wetterValueUpper - wetterValueLower) * 0.2f)) {
                        mc = 5;
                    } else if (moist >= (wetterValueUpper - (wetValueUpper - wetValueLower) * 0.2f)) {
                        mc = 4;
                    } else if (moist >= (wetValueUpper - (dryValueUpper - dryValueLower) * 0.2f)) {
                        mc = 3;
                    } else if (moist >= (dryValueUpper - (drierValueUpper - drierValueLower) * 0.2f)) {
                        mc = 2;
                    } else if (moist >= (drierValueUpper - (driestValueUpper) * 0.2f)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmestValueUpper - (warmerValueUpper - warmerValueLower) * 0.2f) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmerValueUpper - (warmValueUpper - warmValueLower) * 0.2f) * i_hot) {
                        hc = 4;
                    } else if (hot >= (warmValueUpper - (coldValueUpper - coldValueLower) * 0.2f) * i_hot) {
                        hc = 3;
                    } else if (hot >= (coldValueUpper - (colderValueUpper - colderValueLower) * 0.2f) * i_hot) {
                        hc = 2;
                    } else if (hot >= (colderValueUpper - (coldestValueUpper) * 0.2f) * i_hot) {
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
                    else if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2f)) {
                        mc = 5;
                    } else if (moist >= (wetValueUpper + (wetterValueUpper - wetterValueLower) * 0.2f)) {
                        mc = 4;
                    } else if (moist >= (dryValueUpper + (wetValueUpper - wetValueLower) * 0.2f)) {
                        mc = 3;
                    } else if (moist >= (drierValueUpper + (dryValueUpper - dryValueLower) * 0.2f)) {
                        mc = 2;
                    } else if (moist >= (driestValueUpper + (drierValueUpper - drierValueLower) * 0.2f)) {
                        mc = 1;
                    } else {
                        mc = 0;
                    }

                    if (hot >= (warmerValueUpper + (warmestValueUpper - warmestValueLower) * 0.2f) * i_hot) {
                        hc = 5;
                    } else if (hot >= (warmValueUpper + (warmerValueUpper - warmerValueLower) * 0.2f) * i_hot) {
                        hc = 4;
                    } else if (hot >= (coldValueUpper + (warmValueUpper - warmValueLower) * 0.2f) * i_hot) {
                        hc = 3;
                    } else if (hot >= (colderValueUpper + (coldValueUpper - coldValueLower) * 0.2f) * i_hot) {
                        hc = 2;
                    } else if (hot >= (coldestValueUpper + (colderValueUpper - colderValueLower) * 0.2f) * i_hot) {
                        hc = 1;
                    } else {
                        hc = 0;
                    }

                    bc |= (hc + mc * 6) << 10;
                    if(heightCode < 4)
                        biomeCodeData[x][y] = bc | (int)((heightData[x][y] + 1.0) * 1000.0) << 20;
                    else biomeCodeData[x][y] = bc | (int) ((heightCode == 4)
// multiplier affected by changes to sandUpper, not sure why this needs to be different from SquidLib
                            ? (high - WorldMapGenerator.sandLower) * 10240.0
                            : MathTools.sway((high + moist) * (4.1 + high - hot)) * 512 + 512) << 20;
                }
            }
        }
    }
}
