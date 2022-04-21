/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.place;

import static com.github.yellowstonegames.place.Biome.Heat.*;
import static com.github.yellowstonegames.place.Biome.Moisture.*;

/**
 * A data class that represents a particular kind of ecosystem that appears at a given combination of average moisture,
 * average temperature, and elevation. This stores a name for each biome (always as one word in the constants this
 * provides), a char to represent the biome (typically in char arrays, and not necessarily displayed), and a String
 * color description to be used by {@link com.github.yellowstonegames.core.DescriptiveColor#describe(CharSequence)} or
 * describeOklab(), which is probably preferable. Using the Oklab color allows randomly or gradually changing the color
 * as one biome approaches another, which looks more natural in many cases.
 * <br>
 * Heat and moisture determine many biomes based on a biome mapper (one such mapper is present in squidworld). The
 * presence of an ocean generally overrides the heat/moisture effects, unless the ocean is cold enough to freeze.
 * A lot of usage will depend on {@link #TABLE}, and its ordering; see its docs for more.
 */
public class Biome {
    /**
     * Represents 6 average temperature levels that can determine a biome.
     */
    public enum Heat {
        COLDEST("coldest"), COLDER("colder"), COLD("cold"), HOT("hot"), HOTTER("hotter"), HOTTEST("hottest");
        public final String name;
        Heat(final String n){
            name = n;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Represents 6 average precipitation/moisture levels that can determine a biome.
     */
    public enum Moisture {
        DRIEST("driest"), DRIER("drier"), DRY("dry"), WET("wet"), WETTER("wetter"), WETTEST("wettest"),
        COAST("coast"), RIVER("river"), LAKE("lake"), OCEAN("ocean"), STRANGE("strange");
        public final String name;
        Moisture(final String n) {
            name = n;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public final Heat heat;
    public final Moisture moisture;
    /**
     * Should always be one word in the constants here; recommended to be one word in any user-defined Biomes.
     */
    public final String name;
    /**
     * Meant to be used with {@link com.github.yellowstonegames.core.DescriptiveColor}.
     */
    public final String colorDescription;
    /**
     * How to represent the Biome in a char format, such as a 2D char array for a map.
     */
    public final char representation;

    private Biome(){
        heat = Heat.COLDEST;
        moisture = Moisture.STRANGE;
        name = "Space";
        colorDescription = "darkmost dullmost cobalt";
        representation = '.';
    }

    private Biome(Heat h, Moisture m, String n, String c) {
        heat = h;
        moisture = m;
        name = n;
        colorDescription = c;
        representation = '.';
    }


    private Biome(Heat h, Moisture m, char representation, String n, String c) {
        heat = h;
        moisture = m;
        name = n;
        colorDescription = c;
        this.representation = representation;
    }

    /**
     * Hot, dry, barren land; may or may not be sandy.
     */
    public static final String DESERT_BIOME = "Desert";
    /**
     * Hot, mostly-dry land with some parched vegetation; also called scrub or chaparral.
     */
    public static final String SAVANNA_BIOME = "Savanna";
    /**
     * Hot, mostly-dry land with some parched vegetation; also called scrub or chaparral.
     */
    public static final String TROPICAL_RAINFOREST_BIOME = "TropicalRainforest";
    /**
     * Prairies that are dry and usually wind-swept, but not especially hot or cold.
     */
    public static final String GRASSLAND_BIOME = "Grassland";
    /**
     * Part-way between a prairie and a forest; not especially hot or cold.
     */
    public static final String WOODLAND_BIOME = "Woodland";
    /**
     * Forest that becomes barren in winter (deciduous trees); not especially hot or cold.
     */
    public static final String SEASONAL_FOREST_BIOME = "SeasonalForest";
    /**
     * Forest that tends to be slightly warm but very wet
     */
    public static final String TEMPERATE_RAINFOREST_BIOME = "TemperateRainforest";
    /**
     * Forest that tends to be cold and very wet.
     */
    public static final String BOREAL_FOREST_BIOME = "BorealForest";
    /**
     * Very cold plains that still have some low-lying vegetation; also called taiga
     */
    public static final String TUNDRA_BIOME = "Tundra";
    /**
     * Cold barren land covered in permafrost; also used for rivers and lakes that are frozen
     */
    public static final String ICE_BIOME = "Ice";
    /**
     * Sandy or otherwise light-colored shorelines; here, these are more common in warmer places.
     */
    public static final String BEACH_BIOME = "Beach";
    /**
     * Rocky or otherwise rugged shorelines; here, these are more common in colder places.
     */
    public static final String ROCKY_BIOME = "Rocky";
    /**
     * Flowing freshwater of any size; may sometimes be considered the same as {@link #LAKE_BIOME}.
     */
    public static final String RIVER_BIOME = "River";
    /**
     * Still freshwater that lasts for at least the majority of a season; also used for swamps and marshes.
     */
    public static final String LAKE_BIOME = "Lake";
    /**
     * Saltwater of any size; typically not inland.
     */
    public static final String OCEAN_BIOME = "Ocean";
    /**
     * The final frontier; empty areas not covered by the map.
     */
    public static final String SPACE_BIOME = "Space";
    /**
     * One small step for man, one giant leap for mankind; a barren, pale, dusty plain devoid of life.
     */
    public static final String MOON_BIOME = "Moon";
    /**
     * Underground ecosystems may use this as a kind of wilderness biome.
     */
    public static final String CAVERN_BIOME = "Cavern";
    /**
     * A catch-all for any unusual biomes with no real-world equivalents, like cyberspace or another dimension.
     */
    public static final String EXOTIC_BIOME = "Exotic";
    /**
     * What kind of game doesn't have a volcano battle, as unrealistic as it might be?
     */
    public static final String VOLCANO_BIOME = "Volcano";

    /**
     * 66 Biome constants, organized in groups of 6 from coldest to hottest, coldest to hottest, and so on. The first 6
     * of these groups are organized from driest to wettest, then the groups after that are coastlines, rivers, lakes,
     * oceans, and lastly "strange" biomes. The strange biomes aren't organized by coldest to hottest, and they are
     * empty space, moonscape, cavern, "exotic" (a catch-all for supernatural biomes), and volcano biomes.
     */
    public static final Biome[] TABLE = new Biome[]{
            new Biome(COLDEST, DRIEST, '-', "Ice", "lightmost dullmost butter"),               //0
            new Biome(COLDER, DRIEST, '-', "Ice", "lightmost dullmost butter"),
            new Biome(COLD, DRIEST, '\"',  "Grassland", "dark dullest pear"),
            new Biome(HOT, DRIEST, '…',  "Desert", "lightest dullest saffron"),
            new Biome(HOTTER, DRIEST, '…',  "Desert", "lightest dullest saffron"),
            new Biome(HOTTEST, DRIEST, '…',  "Desert", "lightest dullest saffron"),
            new Biome(COLDEST, DRIER, '-', "Ice", "lightmost dullmost butter"),
            new Biome(COLDER, DRIER, '.',  "Tundra", "lightmost dullest fern"),
            new Biome(COLD, DRIER, '\"',  "Grassland", "lightest fern"),
            new Biome(HOT, DRIER, '\"',  "Grassland", "dark dullest lime"),
            new Biome(HOTTER, DRIER, '…',  "Desert", "lightest dullest saffron"),               //10
            new Biome(HOTTEST, DRIER, '…',  "Desert", "lightest dullest saffron"),
            new Biome(COLDEST, DRY, '-', "Ice", "lightmost dullmost butter"),
            new Biome(COLDER, DRY, '.',  "Tundra", "dark dullmost celery"),
            new Biome(COLD, DRY, '♣',  "Woodland", "dull cactus"),
            new Biome(HOT, DRY, '♣',  "Woodland", "lighter richest fern"),
            new Biome(HOTTER, DRY, '„',  "Savanna", "dark duller yellow"),
            new Biome(HOTTEST, DRY, '…',  "Desert", "lightest dullest saffron"),
            new Biome(COLDEST, WET, '-', "Ice", "lightmost dullmost mint"),
            new Biome(COLDER, WET, '.',  "Tundra", "light dullmost jade"),
            new Biome(COLD, WET, '♣',  "SeasonalForest", "lighter rich moss"),                  //20
            new Biome(HOT, WET, '♣',  "SeasonalForest", "dark duller cactus"),
            new Biome(HOTTER, WET, '„',  "Savanna", "dark duller yellow"),
            new Biome(HOTTEST, WET, '„',  "Savanna", "lightmost dull olive"),
            new Biome(COLDEST, WETTER, '-',  "Ice", "lightmost rich silver"),
            new Biome(COLDER, WETTER, '.',  "Tundra", "lightest dullest fern"),
            new Biome(COLD, WETTER, '♠',  "BorealForest", "darkest dullest celery"),
            new Biome(HOT, WETTER, '¥',  "TemperateRainforest", "darker duller jade"),
            new Biome(HOTTER, WETTER, '¶',  "TropicalRainforest", "darkest dull celery"),
            new Biome(HOTTEST, WETTER, '„',  "Savanna", "dark duller pear"),
            new Biome(COLDEST, WETTEST, '-', "Ice", "dark rich white"),                        //30
            new Biome(COLDER, WETTEST, '♠',  "BorealForest", "light dull moss"),
            new Biome(COLD, WETTEST, '♠',  "BorealForest", "dark fern"),
            new Biome(HOT, WETTEST, '¥',  "TemperateRainforest", "dark rich fern"),
            new Biome(HOTTER, WETTEST, '¶',  "TropicalRainforest", "darkest green"),
            new Biome(HOTTEST, WETTEST, '¶',  "TropicalRainforest", "darker cactus"),
            new Biome(COLDEST, COAST, '‰',  "Rocky", "lightmost dullmost denim"),
            new Biome(COLDER, COAST, '‰',  "Rocky", "lightest dullmost cactus"),
            new Biome(COLD, COAST, '…',  "Beach", "lighter dullest saffron"),
            new Biome(HOT, COAST, '…',  "Beach", "lightmost dullest saffron"),
            new Biome(HOTTER, COAST, '…',  "Beach", "lightmost dullest saffron"),               //40
            new Biome(HOTTEST, COAST, '…',  "Beach", "lightmost dullest saffron"),
            new Biome(COLDEST, RIVER, '-', "Ice", "lightest dullmost sage"),
            new Biome(COLDER, RIVER, '~',  "River", "dull teal"), //"light rich denim"
            new Biome(COLD, RIVER, '~',  "River", "light rich denim"),
            new Biome(HOT, RIVER, '~',  "River", "dark dull sky"),
            new Biome(HOTTER, RIVER, '~',  "River", "dark dull sky"),
            new Biome(HOTTEST, RIVER, '~',  "River", "rich denim"),
            new Biome(COLDEST, LAKE, '-', "Ice", "lightmost silver"),
            new Biome(COLDER, LAKE, '~', "Lake", "light dull denim"),
            new Biome(COLD, LAKE, '~', "Lake", "light rich denim"),                            //50
            new Biome(HOT, LAKE, '~', "Lake", "dark dull sky"),
            new Biome(HOTTER, LAKE, '~', "Lake", "rich denim"),
            new Biome(HOTTEST, LAKE, '~', "Lake", "richer denim"),
            new Biome(COLDEST, OCEAN, '≈', "Ocean", "dark duller cobalt"),
            new Biome(COLDER, OCEAN, '≈', "Ocean", "light duller navy"),
            new Biome(COLD, OCEAN, '≈', "Ocean", "darkmost dullest cyan"), //"dark duller cobalt"
            new Biome(HOT, OCEAN, '≈', "Ocean", "darker dull cobalt"),
            new Biome(HOTTER, OCEAN, '≈', "Ocean", "rich navy"),
            new Biome(HOTTEST, OCEAN, '≈', "Ocean", "dark rich navy"),
            new Biome(COLDEST, STRANGE, ' ', "Space", "darkmost dullest mauve"),               //60
            new Biome(COLDER, STRANGE, '¬', "Moon", "dark dullmost white"),
            new Biome(COLD, STRANGE, '□', "Cavern", "darkmost dullmost ember"),
            new Biome(HOT, STRANGE, '□', "Cavern", "darker dullest chocolate"),
            new Biome(HOTTER, STRANGE, '?', "Exotic", "lighter richest raspberry"),
            new Biome(HOTTEST, STRANGE, '∆', "Volcano", "dark ember"),
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Biome biome = (Biome) o;

        if (heat != biome.heat) return false;
        if (moisture != biome.moisture) return false;
        if (!name.equals(biome.name)) return false;
        return colorDescription.equals(biome.colorDescription);
    }

    @Override
    public int hashCode() {
        return (29 * 29 * 29) * heat.hashCode() + (29 * 29) * moisture.hashCode() + (29) * name.hashCode() + colorDescription.hashCode();
    }

    @Override
    public String toString() {
        return heat.name + " " + moisture.name + " " + name;
    }
}
