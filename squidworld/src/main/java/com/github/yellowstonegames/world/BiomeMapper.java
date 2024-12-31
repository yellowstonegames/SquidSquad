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
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.place.Biome;

import static com.github.tommyettinger.digital.MathTools.zigzag;
import static com.github.yellowstonegames.core.DescriptiveColor.*;

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


    int[][] getHeatCodeData();

    int[][] getMoistureCodeData();

    int[][] getBiomeCodeData();


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
        public int[][] getHeatCodeData() {
            return heatCodeData;
        }

        @Override
        public int[][] getMoistureCodeData() {
            return moistureCodeData;
        }

        @Override
        public int[][] getBiomeCodeData() {
            return biomeCodeData;
        }

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
         * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}.
         * @param serialized should have been produced by {@link #stringSerialize()}
         */
        public SimpleBiomeMapper(String serialized) {
            String[] parts = TextTools.split(serialized, "\n");

            int i = 0;
            String part;
            // Fields of this class:
            biomeCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            heatCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            moistureCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
        }

        /**
         * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
         * type with {@link #SimpleBiomeMapper(String)} or {@link #recreateFromString(String)}.
         * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
         * tends to be very long because it includes some 2D arrays as Strings.
         * @return a String that stores the entire state of this generator
         */
        public String stringSerialize(){
            StringBuilder sb = new StringBuilder(1024);
            Base b = Base.BASE86;
            // Fields of this class:
            b.appendJoined2D(sb, "\t", " ", biomeCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", heatCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", moistureCodeData);
            return sb.toString();
        }

        /**
         * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
         * This is here for compatibility with other classes that use String serialization, but you can just use
         * {@link #SimpleBiomeMapper(String)} instead.
         * @param data the output of {@link #stringSerialize()}
         * @return the map that was serialized, as a new generator
         */
        public static SimpleBiomeMapper recreateFromString(String data) {
            return new SimpleBiomeMapper(data);
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
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    biomeCodeData[x][y] = heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : heightCode == 4 ? hc + 36 : hc + mc * 6;
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


        @Override
        public int[][] getHeatCodeData() {
            return heatCodeData;
        }

        @Override
        public int[][] getMoistureCodeData() {
            return moistureCodeData;
        }

        @Override
        public int[][] getBiomeCodeData() {
            return biomeCodeData;
        }

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
            if(code < 0x20000000) return code & 1023;
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
         * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}.
         * @param serialized should have been produced by {@link #stringSerialize()}
         */
        public DetailedBiomeMapper(String serialized) {
            String[] parts = TextTools.split(serialized, "\n");

            int i = 0;
            String part;
            // Fields of this class:
            biomeCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            heatCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            moistureCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
        }

        /**
         * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
         * type with {@link #DetailedBiomeMapper(String)} or {@link #recreateFromString(String)}.
         * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
         * tends to be very long because it includes some 2D arrays as Strings.
         * @return a String that stores the entire state of this generator
         */
        public String stringSerialize(){
            StringBuilder sb = new StringBuilder(1024);
            Base b = Base.BASE86;
            // Fields of this class:
            b.appendJoined2D(sb, "\t", " ", biomeCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", heatCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", moistureCodeData);
            return sb.toString();
        }

        /**
         * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
         * This is here for compatibility with other classes that use String serialization, but you can just use
         * {@link #DetailedBiomeMapper(String)} instead.
         * @param data the output of {@link #stringSerialize()}
         * @return the map that was serialized, as a new generator
         */
        public static DetailedBiomeMapper recreateFromString(String data) {
            return new DetailedBiomeMapper(data);
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
                    bc = heightCode == 3 && hc == 0 ? 48 : heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                            : heightCode == 4 ? hc + 36 : hc + mc * 6;

                    if(heightCode < 4) {
                        mc = 9;
                    } else if (moist >= (wetterValueUpper + (wettestValueUpper - wettestValueLower) * 0.2f)) {
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

//                    bc |= (hc + mc * 6) << 10;
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    bc |= (heightCode == 3 && hc == 0 ? 48 : heightCode < 4 ? hc + 54 // 54 == 9 * 6, 9 is used for Ocean groups
                           : hc + mc * 6) << 10;

                    if(heightCode < 4)
                        biomeCodeData[x][y] = bc | (int)((heightData[x][y] + 1.0f) * 1000.0f) << 20;
                    else {
                        biomeCodeData[x][y] = bc | (int) (
                                (heightCode == 4)
                                ? 1024.0f - (WorldMapGenerator.sandUpper - high) * 10240.0f
                                : MathTools.zigzag((high + moist) * (4.1f + high - hot)) * 512.0f + 512.0f
                        ) << 20;
                    }
                }
            }
        }
    }


    /**
     * A very-smoothly-blending BiomeMapper. Unlike the more-complicated usage of {@link DetailedBiomeMapper}, this does
     * not have fine-grained access to which two biomes are mixed at any given point. The reason for this is partly that
     * more than two biomes can affect any given point, and partly that just accessing a mixed color may be much easier.
     * After you call {@link #makeBiomes(WorldMapGenerator)}, the {@link #colorDataOklab} and {@link #colorDataRgba}
     * fields will be usable, and will match points on the WorldMapGenerator's latest map with colors appropriate for a
     * biome at that point. Calling makeBiomes() also assigns to {@link #biomeCodeData}, though this only stores small
     * int values here (between 0 and 65, inclusive) that correspond to indices into {@link Biome#TABLE}.
     * <br>
     * This is often used via {@link BlendedWorldMapView}.
     */
    class BlendedBiomeMapper implements BiomeMapper
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
         * The biome codes for the analyzed map, using one int to store the dominant biome only.
         */
        biomeCodeData,
        /**
         * Packed Oklab colors as ints, one for each cell in the analyzed map, representing a smooth blend between the
         * biomes closest to the given cell.
         */
        colorDataOklab,
        /**
         * Packed RGBA8888 colors as ints, one for each cell in the analyzed map, representing a smooth blend between
         * the biomes closest to the given cell. This is typically generated from {@link #colorDataOklab}.
         */
        colorDataRgba;

        /**
         * The colors for each biome, with each as a packed oklab int.
         * Always has 66 items, corresponding to the biomes in {@link Biome#TABLE} in order.
         */
        public final int[] colorTable = new int[66];

        public float contrast = 1f;

        public static final float
                coldestValueUpper = 0.15f, // 0
                colderValueUpper = 0.31f,  // 1
                coldValueUpper = 0.5f,     // 2
                warmValueUpper = 0.69f,    // 3
                warmerValueUpper = 0.85f,  // 4
                warmestValueUpper = 1.0f,  // 5

                driestValueUpper  = 0.27f, // 0
                drierValueUpper   = 0.4f,  // 1
                dryValueUpper     = 0.6f,  // 2
                wetValueUpper     = 0.8f,  // 3
                wetterValueUpper  = 0.9f,  // 4
                wettestValueUpper = 1.0f;  // 5

        public static final float[] HEAT_UPPER = new float[]{
                coldestValueUpper,
                colderValueUpper,
                coldValueUpper,
                warmValueUpper,
                warmerValueUpper,
                warmestValueUpper,
        };
        public static final float[] MOISTURE_UPPER = new float[]{
                driestValueUpper,
                drierValueUpper,
                dryValueUpper,
                wetValueUpper,
                wetterValueUpper,
                wettestValueUpper,
        };
        public static final float[] HEAT_MID = new float[]{
                (coldestValueUpper) * 0.5f,
                (colderValueUpper + coldestValueUpper) * 0.5f,
                (coldValueUpper + colderValueUpper) * 0.5f,
                (warmValueUpper + coldValueUpper) * 0.5f,
                (warmerValueUpper + warmValueUpper) * 0.5f,
                (warmestValueUpper + warmerValueUpper) * 0.5f,
        };

        public static final float[] MOISTURE_MID = new float[]{
                (driestValueUpper) * 0.5f,
                (drierValueUpper + driestValueUpper) * 0.5f,
                (dryValueUpper + drierValueUpper) * 0.5f,
                (wetValueUpper + dryValueUpper) * 0.5f,
                (wetterValueUpper + wetValueUpper) * 0.5f,
                (wettestValueUpper + wetterValueUpper) * 0.5f,
        };

        @Override
        public int[][] getHeatCodeData() {
            return heatCodeData;
        }

        @Override
        public int[][] getMoistureCodeData() {
            return moistureCodeData;
        }

        @Override
        public int[][] getBiomeCodeData() {
            return biomeCodeData;
        }

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public BlendedBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
            colorDataOklab = null;
            initialize();
        }

        /**
         * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}.
         * @param serialized should have been produced by {@link #stringSerialize()}
         */
        public BlendedBiomeMapper(String serialized) {
            String[] parts = TextTools.split(serialized, "\n");

            int i = 0;
            String part;
            // Fields of this class:
            biomeCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            heatCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            moistureCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            colorDataOklab = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            colorDataRgba = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            System.arraycopy(Base.BASE86.intSplit(parts[i++], " "), 0, colorTable, 0, 66);
            contrast = Base.BASE86.readFloatExact(parts[i++]);
        }

        /**
         * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
         * type with {@link #BlendedBiomeMapper(String)} or {@link #recreateFromString(String)}.
         * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
         * tends to be very long because it includes some 2D arrays as Strings.
         * @return a String that stores the entire state of this generator
         */
        public String stringSerialize(){
            StringBuilder sb = new StringBuilder(1024);
            Base b = Base.BASE86;
            // Fields of this class:
            b.appendJoined2D(sb, "\t", " ", biomeCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", heatCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", moistureCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", colorDataOklab).append('\n');
            b.appendJoined2D(sb, "\t", " ", colorDataRgba).append('\n');
            b.appendJoined(sb, " ", colorTable).append('\n');
            b.appendUnsigned(sb, contrast);
            return sb.toString();
        }

        /**
         * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
         * This is here for compatibility with other classes that use String serialization, but you can just use
         * {@link #BlendedBiomeMapper(String)} instead.
         * @param data the output of {@link #stringSerialize()}
         * @return the map that was serialized, as a new generator
         */
        public static BlendedBiomeMapper recreateFromString(String data) {
            return new BlendedBiomeMapper(data);
        }

        public void setColorTable(int[] oklabColors) {
            if(oklabColors == null || oklabColors.length < 66)
                throw new IllegalArgumentException("The array of colors must be non-null and have length of at least 66.");
            System.arraycopy(oklabColors, 0, colorTable, 0, 66);
        }

        public void initialize()
        {
            for (int i = 0; i < 66; i++) {
                colorTable[i] = Biome.TABLE[i].colorOklab;
            }
        }

        /**
         * Initializes the color tables this uses for all biomes, but allows rotating all hues and adjusting
         * brightness/saturation/contrast to produce maps of non-Earth-like planets.
         * @param hue hue rotation; 0.0 and 1.0 are no rotation, and 0.5 is maximum rotation
         * @param saturation added to the saturation of a biome color; usually close to 0.0, always between -1 and 1
         * @param brightness added to the lightness of a biome color; often close to 0.0, always between -1 and 1
         * @param contrast multiplied with the soft shading that applies to all land biomes
         */
        public void initialize(float hue, float saturation, float brightness, float contrast)
        {
            int b;
            for (int i = 0; i < 66; i++) {
                b = (Biome.TABLE[i].colorOklab);
                if (i == 60)
                    colorTable[i] = b;
                else {
                    colorTable[i] = oklabByHSL(hue + hue(b), saturation + saturation(b), brightness + channelL(b), 1f);
                }
            }
            this.contrast = contrast;
        }

        /**
         * Uses the current colors in {@link #colorTable} to partly-randomize themselves, and also incorporates three
         * random floats from the given {@code rng}.
         * This should map similar colors in the input color table, like varieties of dark green forest, into similar output
         * colors. It will not change color 60 (empty space), but will change everything else. Typically, colors like white
         * ice will still map to white, and different shades of ocean blue will become different shades of some color (which
         * could still be some sort of blue). This can be a useful alternative to
         * {@link #initialize(float, float, float, float)}, because that method hue-rotates all colors by the same amount,
         * while this method adjusts each input hue differently and based on their original value. You may want to call
         * {@link #initialize()} (either with no arguments or with four) before each call to this, because changes this
         * makes to the color table would be read back the second time this is called without reinitialization.
         * @param rng any non-null EnhancedRandom
         */
        public void alter(EnhancedRandom rng)
        {
            int b;
            float h = rng.nextFloat(0.5f) + 1f, s = rng.nextFloat(0.5f) + 1f, l = rng.nextFloat(0.5f) + 1f;
            for (int i = 0; i < 66; i++) {
                b = colorTable[i];
                if (i != 60) {
                    float hue = hue(b), saturation = saturation(b), lightness = channelL(b);
                    colorTable[i] = oklabByHSL(
                            zigzag((hue * h + saturation + lightness) * 0.5f),
                            saturation + zigzag(lightness * s) * 0.1f,
                            lightness + zigzag(saturation * l) * 0.1f, 1f);
                }
            }
        }

        /**
         * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
         * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
         * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
         * {@link DescriptiveColor#describeOklab(String)}, or might be made manually, in advanced cases, with
         * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
         * @param similarColors an array or vararg of packed int Oklab colors with at least one element
         */
        public void match(long seed, int... similarColors)
        {
            for (int i = 0; i < 66; i++) {
                colorTable[i] = (similarColors[(Hasher.hash(seed, Biome.TABLE[i].name) >>> 1) % similarColors.length]);
            }
        }

        /**
         * Gets the biome code for the dominant biome at a given x,y position.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return the biome code for the dominant biome part at the given location
         */
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
         * <br>
         * Like with {@link SimpleBiomeMapper}, you can use a biome code directly from biomeCodeData as an index
         * into this.
         * @return a direct reference to {@link Biome#TABLE}, a Biome array with 66 items
         */
        @Override
        public Biome[] getBiomeTable() {
            return Biome.TABLE;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and (packed Oklab int) colors from {@link #colorDataOklab}.
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
            if(colorDataOklab == null || (colorDataOklab.length != world.width || colorDataOklab[0].length != world.height))
                colorDataOklab = new int[world.width][world.height];
            if(colorDataRgba == null || (colorDataRgba.length != world.width || colorDataRgba[0].length != world.height))
                colorDataRgba = new int[world.width][world.height];
            final int[][] heightCodeData = world.heightCodeData;
            final float[][] heatData = world.heatData, moistureData = world.moistureData;
            final float i_hot = 1f / world.maxHeat;
            final float con = 0.2f * contrast;
            int hc, mc, heightCode;
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {

                    heightCode = heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        colorDataRgba[x][y] = DescriptiveColor.toRGBA8888(colorDataOklab[x][y] = colorTable[60]);
                        continue;
                    }
                    float hot, moist;
                    float hotMix = 1f, moistMix = 1f;
                    int wetLow = 5, wetHigh = 5, hotLow = 5, hotHigh = 5;
                    hot = heatData[x][y] * i_hot;
                    moist = moistureData[x][y];
                    hc = 5;
                    mc = 5;
                    for (int i = 0; i < 6; i++) {
                        if(moist <= MOISTURE_UPPER[i]) {
                            mc = i;
                            if(moist <= MOISTURE_MID[0]) {
                                moistMix = 0;
//                                moistMix = MathTools.norm(world.minWet, MOISTURE_MID[0], moist);
                                wetLow = 0;
                                wetHigh = 0;
                            }
                            else if(moist > MOISTURE_MID[5]) {
                                moistMix = 1;
//                                moistMix = MathTools.norm(MOISTURE_MID[5], world.maxWet, moist);;
                                wetLow = 5;
                                wetHigh = 5;
                            }
                            else if(moist <= MOISTURE_MID[i]) {
                                moistMix = MathTools.norm(MOISTURE_MID[i - 1], MOISTURE_MID[i], moist);
                                wetLow = i-1;
                                wetHigh = i;
                            }
                            else {
                                moistMix = MathTools.norm(MOISTURE_MID[i], MOISTURE_MID[i + 1], moist);
                                wetLow = i;
                                wetHigh = i+1;
                            }
                            break;
                        }
                    }
                    for (int i = 0; i < 6; i++) {
                        if(hot <= HEAT_UPPER[i]) {
                            hc = i;
                            if(hot <= HEAT_MID[0]) {
                                hotMix = MathTools.norm(world.minHeat, HEAT_MID[0], hot);
                                hotLow = 0;
                                hotHigh = 0;
                            }
                            else if(hot > HEAT_MID[5]) {
                                hotMix = MathTools.norm(HEAT_MID[5], world.maxHeat, hot);;
                                hotLow = 5;
                                hotHigh = 5;
                            }
                            else if(hot <= HEAT_MID[i]) {
                                hotMix = MathTools.norm(HEAT_MID[i - 1], HEAT_MID[i], hot);
                                hotLow = i-1;
                                hotHigh = i;
                            }
                            else {
                                hotMix = MathTools.norm(HEAT_MID[i], HEAT_MID[i + 1], hot);
                                hotLow = i;
                                hotHigh = i+1;
                            }
                            break;
                        }
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    biomeCodeData[x][y]
                            = heightCode == 3 && hc == 0 ? 48 : heightCode < 4 ? hc + 54
                            : heightCode == 4 ? hc + 36 : hc + mc * 6;
//                    moistMix = Interpolations.smoother.apply(moistMix);
//                    hotMix = Interpolations.smoother.apply(hotMix);
                    if(hc == 0 && heightCode <= 3){
                        colorDataOklab[x][y] = lerpColors(colorTable[50], colorTable[12],
                                ((world.heightData[x][y] + 1f) / (WorldMapGenerator.sandLower + 1f)));
                    }
                    else if(heightCode <= 3) {
                        colorDataOklab[x][y] = lerpColors(colorTable[56], colorTable[43],
                                Math.min(Math.max(((world.heightData[x][y] + 0.1f) * 7f)
                                        / (WorldMapGenerator.sandLower + 1f), 0f), 1f));
                    }
                    else {
                        int c = lerpColors(
                                lerpColors(
                                        colorTable[hotLow + wetLow * 6],
                                        colorTable[hotLow + wetHigh * 6], moistMix),
                                lerpColors(
                                        colorTable[hotHigh + wetLow * 6],
                                        colorTable[hotHigh + wetHigh * 6], moistMix),
                                hotMix
                        );
                        if(heightCode == 4) {
                            int beach =
                                    lerpColors(
                                    lerpColors(
                                            colorTable[hotLow + 36],
                                            colorTable[hotLow + 36], moistMix),
                                    lerpColors(
                                            colorTable[hotHigh + 36],
                                            colorTable[hotHigh + 36], moistMix),
                                    hotMix
                                    );
                            if(hot < coldestValueUpper)
                                beach = lerpColors(beach, colorTable[0],
                                    ((coldestValueUpper - hot)
                                            / coldestValueUpper));
                            c = lerpColors(beach, c,
                                    ((world.heightData[x][y] - WorldMapGenerator.sandLower)
                                            / (WorldMapGenerator.sandUpper - WorldMapGenerator.sandLower)));
                                    //1f - 10f * (WorldMapGenerator.sandUpper - world.heightData[x][y]));
                        }

                        colorDataOklab[x][y] = DescriptiveColor.adjustLightness(c, 0.02f - moist * con);
                    }
                    colorDataRgba[x][y] = DescriptiveColor.toRGBA8888(colorDataOklab[x][y]);
                }
            }
        }
    }

    /**
     * Primarily useful for debugging, this makes every biome a wildly different color. It still blends between them,
     * very much like how {@link BlendedBiomeMapper} works, but is meant to make discontinuities in the map easier to
     * spot and then diagnose. It could also be useful for a psychedelic alien world, possibly.
     */
    class UnrealisticBiomeMapper implements BiomeMapper
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
         * The biome codes for the analyzed map, using one int to store the dominant biome only.
         */
        biomeCodeData,
        /**
         * Packed Oklab colors as ints, one for each cell in the analyzed map, representing a smooth blend between the
         * biomes closest to the given cell.
         */
        colorDataOklab,
        /**
         * Packed RGBA8888 colors as ints, one for each cell in the analyzed map, representing a smooth blend between
         * the biomes closest to the given cell. This is typically generated from {@link #colorDataOklab}.
         */
        colorDataRgba;

        /**
         * The colors for each biome, with each as a packed oklab int.
         * Always has 66 items, corresponding to the biomes in {@link Biome#TABLE} in order.
         */
        public final int[] colorTable = new int[66];

        public float contrast = 1f;

        public static final float
                coldestValueUpper = 0.15f, // 0
                colderValueUpper = 0.31f,  // 1
                coldValueUpper = 0.5f,     // 2
                warmValueUpper = 0.69f,    // 3
                warmerValueUpper = 0.85f,  // 4
                warmestValueUpper = 1.0f,  // 5

                driestValueUpper  = 0.27f, // 0
                drierValueUpper   = 0.4f,  // 1
                dryValueUpper     = 0.6f,  // 2
                wetValueUpper     = 0.8f,  // 3
                wetterValueUpper  = 0.9f,  // 4
                wettestValueUpper = 1.0f;  // 5

        public static final float[] HEAT_UPPER = new float[]{
                coldestValueUpper,
                colderValueUpper,
                coldValueUpper,
                warmValueUpper,
                warmerValueUpper,
                warmestValueUpper,
        };
        public static final float[] MOISTURE_UPPER = new float[]{
                driestValueUpper,
                drierValueUpper,
                dryValueUpper,
                wetValueUpper,
                wetterValueUpper,
                wettestValueUpper,
        };
        public static final float[] HEAT_MID = new float[]{
                (coldestValueUpper) * 0.5f,
                (colderValueUpper + coldestValueUpper) * 0.5f,
                (coldValueUpper + colderValueUpper) * 0.5f,
                (warmValueUpper + coldValueUpper) * 0.5f,
                (warmerValueUpper + warmValueUpper) * 0.5f,
                (warmestValueUpper + warmerValueUpper) * 0.5f,
        };

        public static final float[] MOISTURE_MID = new float[]{
                (driestValueUpper) * 0.5f,
                (drierValueUpper + driestValueUpper) * 0.5f,
                (dryValueUpper + drierValueUpper) * 0.5f,
                (wetValueUpper + dryValueUpper) * 0.5f,
                (wetterValueUpper + wetValueUpper) * 0.5f,
                (wettestValueUpper + wetterValueUpper) * 0.5f,
        };

        @Override
        public int[][] getHeatCodeData() {
            return heatCodeData;
        }

        @Override
        public int[][] getMoistureCodeData() {
            return moistureCodeData;
        }

        @Override
        public int[][] getBiomeCodeData() {
            return biomeCodeData;
        }

        /**
         * Simple constructor; pretty much does nothing. Make sure to call {@link #makeBiomes(WorldMapGenerator)} before
         * using fields like {@link #biomeCodeData}.
         */
        public UnrealisticBiomeMapper()
        {
            heatCodeData = null;
            moistureCodeData = null;
            biomeCodeData = null;
            colorDataOklab = null;
            initialize();
        }

        /**
         * Creates a new generator from the given serialized String, produced by {@link #stringSerialize()}.
         * @param serialized should have been produced by {@link #stringSerialize()}
         */
        public UnrealisticBiomeMapper(String serialized) {
            String[] parts = TextTools.split(serialized, "\n");

            int i = 0;
            String part;
            // Fields of this class:
            biomeCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            heatCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            moistureCodeData = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            colorDataOklab = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            colorDataRgba = (part = parts[i++]).isEmpty() ? null : Base.BASE86.intSplit2D(part, "\t", " ");
            System.arraycopy(Base.BASE86.intSplit(parts[i++], " "), 0, colorTable, 0, 66);
            contrast = Base.BASE86.readFloatExact(parts[i++]);
        }

        /**
         * Serializes this generator's entire state to a String; it can be read back when creating a new instance of this
         * type with {@link #UnrealisticBiomeMapper(String)} or {@link #recreateFromString(String)}.
         * Uses {@link Base#BASE86} to represent values very concisely, but not at all readably. The String this produces
         * tends to be very long because it includes some 2D arrays as Strings.
         * @return a String that stores the entire state of this generator
         */
        public String stringSerialize(){
            StringBuilder sb = new StringBuilder(1024);
            Base b = Base.BASE86;
            // Fields of this class:
            b.appendJoined2D(sb, "\t", " ", biomeCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", heatCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", moistureCodeData).append('\n');
            b.appendJoined2D(sb, "\t", " ", colorDataOklab).append('\n');
            b.appendJoined2D(sb, "\t", " ", colorDataRgba).append('\n');
            b.appendJoined(sb, " ", colorTable).append('\n');
            b.appendUnsigned(sb, contrast);
            return sb.toString();
        }

        /**
         * Creates a new instance of this class from a serialized String produced by {@link #stringSerialize()}.
         * This is here for compatibility with other classes that use String serialization, but you can just use
         * {@link #UnrealisticBiomeMapper(String)} instead.
         * @param data the output of {@link #stringSerialize()}
         * @return the map that was serialized, as a new generator
         */
        public static UnrealisticBiomeMapper recreateFromString(String data) {
            return new UnrealisticBiomeMapper(data);
        }

        public void setColorTable(int[] oklabColors) {
            if(oklabColors == null || oklabColors.length < 66)
                throw new IllegalArgumentException("The array of colors must be non-null and have length of at least 66.");
            System.arraycopy(oklabColors, 0, colorTable, 0, 66);
        }

        public void initialize()
        {
            for (int m = 0; m < 10; m++) {
                for (int h = 0; h < 6; h++) {
                    // when showing only moisture changes, the noise is smooth and continuous.
//                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(m * -0.42f + 0.66f, 1f, 0.5f, 1f);
                    // when showing only heat, the noise has disjoined areas.
//                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(h * -0.42f + 0.66f, 1f, 0.5f, 1f);
//                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(h * -0.42f + 0.66f, 1f, m * 0.1f + 0.25f, 1f);
                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(m * -0.42f + 0.66f, 1f, h * 0.1f + 0.25f, 1f);
//                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(h * -0.42f + 0.66f, 1f, 0.375f + m * 0.05f, 1f);
                }
            }
            for (int i = 60; i < 66; i++) {
                colorTable[i] = Biome.TABLE[i].colorOklab;
            }
        }

        /**
         * Initializes the color tables this uses for all biomes, but allows rotating all hues and adjusting
         * brightness/saturation/contrast to produce maps of non-Earth-like planets.
         * @param hue hue rotation; 0.0 and 1.0 are no rotation, and 0.5 is maximum rotation
         * @param saturation added to the saturation of a biome color; usually close to 0.0, always between -1 and 1
         * @param brightness added to the lightness of a biome color; often close to 0.0, always between -1 and 1
         * @param contrast multiplied with the soft shading that applies to all land biomes
         */
        public void initialize(float hue, float saturation, float brightness, float contrast)
        {
            for (int m = 0; m < 10; m++) {
                for (int h = 0; h < 6; h++) {
                    colorTable[m * 6 + h] = DescriptiveColor.oklabByHSL(m * -0.42f + 0.66f + hue, 1f + saturation, h * 0.1f + 0.25f + brightness, 1f);
                }
            }
            for (int i = 60; i < 66; i++) {
                colorTable[i] = Biome.TABLE[i].colorOklab;
            }
            this.contrast = contrast;
        }

        /**
         * Uses the current colors in {@link #colorTable} to partly-randomize themselves, and also incorporates three
         * random floats from the given {@code rng}.
         * This should map similar colors in the input color table, like varieties of dark green forest, into similar output
         * colors. It will not change color 60 (empty space), but will change everything else. Typically, colors like white
         * ice will still map to white, and different shades of ocean blue will become different shades of some color (which
         * could still be some sort of blue). This can be a useful alternative to
         * {@link #initialize(float, float, float, float)}, because that method hue-rotates all colors by the same amount,
         * while this method adjusts each input hue differently and based on their original value. You may want to call
         * {@link #initialize()} (either with no arguments or with four) before each call to this, because changes this
         * makes to the color table would be read back the second time this is called without reinitialization.
         * @param rng any non-null EnhancedRandom
         */
        public void alter(EnhancedRandom rng)
        {
            int b;
            float h = rng.nextFloat(0.5f) + 1f, s = rng.nextFloat(0.5f) + 1f, l = rng.nextFloat(0.5f) + 1f;
            for (int i = 0; i < 66; i++) {
                b = colorTable[i];
                if (i != 60) {
                    float hue = hue(b), saturation = saturation(b), lightness = channelL(b);
                    colorTable[i] = oklabByHSL(
                            zigzag((hue * h + saturation + lightness) * 0.5f),
                            saturation + zigzag(lightness * s) * 0.1f,
                            lightness + zigzag(saturation * l) * 0.1f, 1f);
                }
            }
        }

        /**
         * Initializes the colors to use in some combination for all biomes, without regard for what the biome really is.
         * There should be at least one packed int Oklab color given in similarColors, but there can be many of them. This
         * type of color can be any of the color constants from {@link DescriptiveColor}, may be produced by
         * {@link DescriptiveColor#describeOklab(String)}, or might be made manually, in advanced cases, with
         * {@link DescriptiveColor#limitToGamut(int, int, int)} and specifying the L, A, and B channels.
         * @param similarColors an array or vararg of packed int Oklab colors with at least one element
         */
        public void match(long seed, int... similarColors)
        {
            for (int i = 0; i < 66; i++) {
                colorTable[i] = (similarColors[(Hasher.hash(seed, Biome.TABLE[i].name) >>> 1) % similarColors.length]);
            }
        }

        /**
         * Gets the biome code for the dominant biome at a given x,y position.
         * @param x the x-coordinate on the map
         * @param y the y-coordinate on the map
         * @return the biome code for the dominant biome part at the given location
         */
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
         * <br>
         * Like with {@link SimpleBiomeMapper}, you can use a biome code directly from biomeCodeData as an index
         * into this.
         * @return a direct reference to {@link Biome#TABLE}, a Biome array with 66 items
         */
        @Override
        public Biome[] getBiomeTable() {
            return Biome.TABLE;
        }

        /**
         * Analyzes the last world produced by the given WorldMapGenerator and uses all of its generated information to
         * assign biome codes for each cell (along with heat and moisture codes). After calling this, biome codes can be
         * taken from {@link #biomeCodeData} and (packed Oklab int) colors from {@link #colorDataOklab}.
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
            if(colorDataOklab == null || (colorDataOklab.length != world.width || colorDataOklab[0].length != world.height))
                colorDataOklab = new int[world.width][world.height];
            if(colorDataRgba == null || (colorDataRgba.length != world.width || colorDataRgba[0].length != world.height))
                colorDataRgba = new int[world.width][world.height];
            final int[][] heightCodeData = world.heightCodeData;
            final float[][] heatData = world.heatData, moistureData = world.moistureData;
            final float i_hot = 1f / world.maxHeat;
            int hc, mc, heightCode;
            for (int x = 0; x < world.width; x++) {
                for (int y = 0; y < world.height; y++) {

                    heightCode = heightCodeData[x][y];
                    if(heightCode == 1000) {
                        biomeCodeData[x][y] = 60;
                        colorDataRgba[x][y] = DescriptiveColor.toRGBA8888(colorDataOklab[x][y] = colorTable[60]);
                        continue;
                    }
                    float hot, moist;
                    float hotMix = 1f, moistMix = 1f;
                    int wetLow = 5, wetHigh = 5, hotLow = 5, hotHigh = 5;
                    hot = heatData[x][y] * i_hot;
                    moist = moistureData[x][y];
                    hc = 5;
                    mc = 5;
                    for (int i = 0; i < 6; i++) {
                        if(moist <= MOISTURE_UPPER[i]) {
                            mc = i;
                            if(moist <= MOISTURE_MID[0]) {
                                moistMix = 0;
//                                moistMix = MathTools.norm(world.minWet, MOISTURE_MID[0], moist);
                                wetLow = 0;
                                wetHigh = 0;
                            }
                            else if(moist > MOISTURE_MID[5]) {
                                moistMix = 1;
//                                moistMix = MathTools.norm(MOISTURE_MID[5], world.maxWet, moist);;
                                wetLow = 5;
                                wetHigh = 5;
                            }
                            else if(moist <= MOISTURE_MID[i]) {
                                moistMix = MathTools.norm(MOISTURE_MID[i - 1], MOISTURE_MID[i], moist);
                                wetLow = i-1;
                                wetHigh = i;
                            }
                            else {
                                moistMix = MathTools.norm(MOISTURE_MID[i], MOISTURE_MID[i + 1], moist);
                                wetLow = i;
                                wetHigh = i+1;
                            }
                            break;
                        }
                    }
                    for (int i = 0; i < 6; i++) {
                        if(hot <= HEAT_UPPER[i]) {
                            hc = i;
                            if(hot <= HEAT_MID[0]) {
                                hotMix = MathTools.norm(world.minHeat, HEAT_MID[0], hot);
                                hotLow = 0;
                                hotHigh = 0;
                            }
                            else if(hot > HEAT_MID[5]) {
                                hotMix = MathTools.norm(HEAT_MID[5], world.maxHeat, hot);;
                                hotLow = 5;
                                hotHigh = 5;
                            }
                            else if(hot <= HEAT_MID[i]) {
                                hotMix = MathTools.norm(HEAT_MID[i - 1], HEAT_MID[i], hot);
                                hotLow = i-1;
                                hotHigh = i;
                            }
                            else {
                                hotMix = MathTools.norm(HEAT_MID[i], HEAT_MID[i + 1], hot);
                                hotLow = i;
                                hotHigh = i+1;
                            }
                            break;
                        }
                    }

                    heatCodeData[x][y] = hc;
                    moistureCodeData[x][y] = mc;
                    // 54 == 9 * 6, 9 is used for Ocean groups
                    biomeCodeData[x][y] = hc + mc * 6;
//                    moistMix = Interpolations.smoother.apply(moistMix);
//                    hotMix = Interpolations.smoother.apply(hotMix);
                    int c = lerpColors(
                            lerpColors(
                                    colorTable[hotLow + wetLow * 6],
                                    colorTable[hotLow + wetHigh * 6], moistMix),
                            lerpColors(
                                    colorTable[hotHigh + wetLow * 6],
                                    colorTable[hotHigh + wetHigh * 6], moistMix),
                            hotMix
                    );
                    colorDataOklab[x][y] = c;//DescriptiveColor.dullen(c, 0.3f * (world.heightData[x][y] + 1f));
                    colorDataRgba[x][y] = DescriptiveColor.toRGBA8888(c);
                }
            }
        }
    }
}
