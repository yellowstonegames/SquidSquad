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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.digital.BitConversion;

import static com.github.yellowstonegames.place.Biome.Heat.*;
import static com.github.yellowstonegames.place.Biome.Moisture.*;

/**
 * A data class that represents a particular kind of ecosystem that appears at a given combination of average moisture,
 * average temperature, and elevation. This stores a name for each biome (always as one word in the constants this
 * provides), a char to represent the biome (typically in char arrays, and not necessarily displayed), and a String
 * color description to be used by {@link com.github.yellowstonegames.core.DescriptiveColor#describe(String)} or
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
        /**
         * The cached result of {@link #values()}, so you can avoid repeatedly allocating {@code Heat[]} objects.
         * DO NOT MODIFY THIS ARRAY.
         */
        public static final Heat[] ALL = values();

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
        /**
         * The cached result of {@link #values()}, so you can avoid repeatedly allocating {@code Moisture[]} objects.
         * DO NOT MODIFY THIS ARRAY.
         */
        public static final Moisture[] ALL = values();

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

    public final int colorOklab;

    /**
     * How to represent the Biome in a char format, such as a 2D char array for a map.
     */
    public final char representation;

    private Biome(){
        heat = Heat.COLDEST;
        moisture = Moisture.STRANGE;
        name = "Space";
        representation = '.';
        colorOklab = 0xFF7A8121; // inky almost-black
    }

    private Biome(Heat h, Moisture m, String n, int o) {
        heat = h;
        moisture = m;
        name = n;
        representation = '.';
        colorOklab = o;
    }


    private Biome(Heat h, Moisture m, String n, int o, char representation) {
        heat = h;
        moisture = m;
        name = n;
        this.representation = representation;
        colorOklab = o;
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
            new Biome(COLDEST, DRIEST, "Ice", 0xFF7E7EF3, '-'),                        //0
            new Biome(COLDER, DRIEST, "Ice", 0xFF7E7EF2, '-'  ),
            new Biome(COLD, DRIEST, "Grassland", 0xFF8979A8, '\"' ),
            new Biome(HOT, DRIEST, "Desert", 0xFF877FE3, '…'  ),
            new Biome(HOTTER, DRIEST, "Desert", 0xFF877FE3, '…'  ),
            new Biome(HOTTEST, DRIEST, "Desert", 0xFF877FE3, '…'  ),
            new Biome(COLDEST, DRIER, "Ice", 0xFF7E7EF2, '-'  ),
            new Biome(COLDER, DRIER, "Tundra", 0xFF817BB3, '.'  ),
            new Biome(COLD, DRIER, "Grassland", 0xFF8979A1, '\"' ),
            new Biome(HOT, DRIER, "Grassland", 0xFF897996, '\"' ),
            new Biome(HOTTER, DRIER, "Desert", 0xFF877FE2, '…'),                         //  10
            new Biome(HOTTEST, DRIER, "Desert", 0xFF877FE2, '…'  ),
            new Biome(COLDEST, DRY, "Ice", 0xFF7E7EF2, '-'  ),
            new Biome(COLDER, DRY, "Tundra", 0xFF817BAF, '.'  ),
            new Biome(COLD, DRY, "Woodland", 0xFF8B718A, '♣'  ),
            new Biome(HOT, DRY, "Woodland", 0xFF8B7191, '♣'  ),
            new Biome(HOTTER, DRY, "Savanna", 0xFF8C7AC1, '„'  ),
            new Biome(HOTTEST, DRY, "Desert", 0xFF877FE1, '…'  ),
            new Biome(COLDEST, WET, "Ice", 0xFF7E7EEC, '-'  ),
            new Biome(COLDER, WET, "Tundra", 0xFF817BAA, '.'  ),
            new Biome(COLD, WET, "SeasonalForest", 0xFF8B7364, '♣'),                         //  20
            new Biome(HOT, WET, "SeasonalForest", 0xFF8B736A, '♣'  ),
            new Biome(HOTTER, WET, "Savanna", 0xFF8C7ABE, '„'  ),
            new Biome(HOTTEST, WET, "Savanna", 0xFF8C7AB7, '„'  ),
            new Biome(COLDEST, WETTER, "Ice", 0xFF7E7EDF, '-'  ),
            new Biome(COLDER, WETTER, "Tundra", 0xFF817BA1, '.'  ),
            new Biome(COLD, WETTER, "BorealForest", 0xFF89775C, '♠'  ),
            new Biome(HOT, WETTER, "TemperateRainforest", 0xFF877460, '¥'  ),
            new Biome(HOTTER, WETTER, "TropicalRainforest", 0xFF8B715A, '¶'  ),
            new Biome(HOTTEST, WETTER, "Savanna", 0xFF8C7AAD, '„'  ),
            new Biome(COLDEST, WETTEST, "Ice", 0xFF7E7ED2, '-'),                        //  30
            new Biome(COLDER, WETTEST, "BorealForest", 0xFF897753, '♠'  ),
            new Biome(COLD, WETTEST, "BorealForest", 0xFF897757, '♠'  ),
            new Biome(HOT, WETTEST, "TemperateRainforest", 0xFF87745B, '¥'  ),
            new Biome(HOTTER, WETTEST, "TropicalRainforest", 0xFF8B7153, '¶'  ),
            new Biome(HOTTEST, WETTEST, "TropicalRainforest", 0xFF8B714E, '¶'  ),
            new Biome(COLDEST, COAST, "Rocky", 0xFF847DB6, '‰'  ),
            new Biome(COLDER, COAST, "Rocky", 0xFF847DAF, '‰'  ),
            new Biome(COLD, COAST, "Beach", 0xFF887FE1, '…'  ),
            new Biome(HOT, COAST, "Beach", 0xFF887FE6, '…'  ),
            new Biome(HOTTER, COAST, "Beach", 0xFF887FE7, '…'),                         //  40
            new Biome(HOTTEST, COAST, "Beach", 0xFF887FE8, '…'  ),
            new Biome(COLDEST, RIVER, "Ice", 0xFF7E7EE6, '-'  ),
            new Biome(COLDER, RIVER, "River", 0xFF7B757C, '~'  ),
            new Biome(COLD, RIVER, "River", 0xFF7B7576, '~'  ),
            new Biome(HOT, RIVER, "River", 0xFF7B7570, '~'  ),
            new Biome(HOTTER, RIVER, "River", 0xFF7B756C, '~'  ),
            new Biome(HOTTEST, RIVER, "River", 0xFF7B7569, '~'  ),
            new Biome(COLDEST, LAKE, "Ice", 0xFF7E7EDF, '-'  ),
            new Biome(COLDER, LAKE, "Lake", 0xFF7B757C, '~'  ),
            new Biome(COLD, LAKE, "Lake", 0xFF7B7572, '~'),                        //  50
            new Biome(HOT, LAKE, "Lake", 0xFF7B756C, '~'  ),
            new Biome(HOTTER, LAKE, "Lake", 0xFF7B7569, '~'  ),
            new Biome(HOTTEST, LAKE, "Lake", 0xFF7B7566, '~'  ),
            new Biome(COLDEST, OCEAN, "Ocean", 0xFF7A784E, '≈'  ),
            new Biome(COLDER, OCEAN, "Ocean", 0xFF7A7846, '≈'  ),
            new Biome(COLD, OCEAN, "Ocean", 0xFF7A783E, '≈'  ),
            new Biome(HOT, OCEAN, "Ocean", 0xFF7A7837, '≈'  ),
            new Biome(HOTTER, OCEAN, "Ocean", 0xFF7A7835, '≈'  ),
            new Biome(HOTTEST, OCEAN, "Ocean", 0xFF7A7833, '≈'  ),
            new Biome(COLDEST, STRANGE, "Space", 0x007A8121, ' '),                        //  60
            new Biome(COLDER, STRANGE, "Moon", 0xFF7F7FCA, '¬'  ),
            new Biome(COLD, STRANGE, "Cavern", 0xFF82822D, '□'  ),
            new Biome(HOT, STRANGE, "Cavern", 0xFF828230, '□'  ),
            new Biome(HOTTER, STRANGE, "Exotic", 0xFF859F76, '?'  ),
            new Biome(HOTTEST, STRANGE, "Volcano", 0xFF8E927E, '∆'  ),
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Biome biome = (Biome) o;

        if (heat != biome.heat) return false;
        if (moisture != biome.moisture) return false;
        if (!name.equals(biome.name)) return false;
        return BitConversion.floatToIntBits(colorOklab) == BitConversion.floatToIntBits(biome.colorOklab);
    }

    @Override
    public int hashCode() {
        return (29 * 29 * 29) * heat.hashCode() + (29 * 29) * moisture.hashCode() + (29) * name.hashCode() + BitConversion.floatToIntBits(colorOklab);
    }

    @Override
    public String toString() {
        return heat.name + " " + moisture.name + " " + name;
    }
}
