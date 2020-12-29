package com.github.yellowstonegames.place;

import java.io.Serializable;

import static com.github.yellowstonegames.place.Biome.Heat.*;
import static com.github.yellowstonegames.place.Biome.Moisture.*;

public class Biome implements Serializable {
    private static final long serialVersionUID = 0;

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
    public final String name, colorDescription;

    private Biome(){
        heat = Heat.COLDEST;
        moisture = Moisture.STRANGE;
        name = "Space";
        colorDescription = "darkmost dullmost cobalt";
    }

    private Biome(Heat h, Moisture m, String n, String c) {
        heat = h;
        moisture = m;
        name = n;
        colorDescription = c;
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
            new Biome(COLDEST, DRIEST, "Ice", "dullmost white"),
            new Biome(COLDER, DRIEST, "Ice", "dullmost white"),
            new Biome(COLD, DRIEST, "Grassland", "lightest duller olive"),
            new Biome(HOT, DRIEST, "Desert", "light duller butter"),
            new Biome(HOTTER, DRIEST, "Desert", "light duller butter"),
            new Biome(HOTTEST, DRIEST, "Desert", "light duller butter"),
            new Biome(COLDEST, DRIER, "Ice", "dullmost white"),
            new Biome(COLDER, DRIER, "Tundra", "lightmost dullmost fern"),
            new Biome(COLD, DRIER, "Grassland", "darker dullest chartreuse"),
            new Biome(HOT, DRIER, "Grassland", "dark dullest lime"),
            new Biome(HOTTER, DRIER, "Desert", "duller butter"),
            new Biome(HOTTEST, DRIER, "Desert", "duller butter"),
            new Biome(COLDEST, DRY, "Ice", "dullmost white"),
            new Biome(COLDER, DRY, "Tundra", "lighter dullmost jade"),
            new Biome(COLD, DRY, "Woodland", "lightest richer moss"),
            new Biome(HOT, DRY, "Woodland", "light duller cactus"),
            new Biome(HOTTER, DRY, "Savanna", "lightmost dull olive"),
            new Biome(HOTTEST, DRY, "Desert", "duller butter"),
            new Biome(COLDEST, WET, "Ice", "lightmost dullmost mint"),
            new Biome(COLDER, WET, "Tundra", "lightest dullmost cactus"),
            new Biome(COLD, WET, "SeasonalForest", "darkmost dull lime"),
            new Biome(HOT, WET, "SeasonalForest", "darkmost lime"),
            new Biome(HOTTER, WET, "Savanna", "lightmost dull olive"),
            new Biome(HOTTEST, WET, "Savanna", "dark duller pear"),
            new Biome(COLDEST, WETTER, "Ice", "lightmost dullmost sky"),
            new Biome(COLDER, WETTER, "Tundra", "lightest dullmost fern"),
            new Biome(COLD, WETTER, "BorealForest", "darker dullest cactus"),
            new Biome(HOT, WETTER, "TemperateRainforest", "darkmost dullest celery"),
            new Biome(HOTTER, WETTER, "TropicalRainforest", "light richest moss"),
            new Biome(HOTTEST, WETTER, "Savanna", "lightest dull olive"),
            new Biome(COLDEST, WETTEST, "Ice", "dullmost sage"),
            new Biome(COLDER, WETTEST, "BorealForest", "darkmost dullest lime"),
            new Biome(COLD, WETTEST, "BorealForest", "light moss"),
            new Biome(HOT, WETTEST, "TemperateRainforest", "darkest dullest jade"),
            new Biome(HOTTER, WETTEST, "TropicalRainforest", "richmost moss"),
            new Biome(HOTTEST, WETTEST, "TropicalRainforest", "darkmost dull cactus"),
            new Biome(COLDEST, COAST, "Rocky", "dark dullmost chartreuse"),
            new Biome(COLDER, COAST, "Rocky", "dullmost lime"),
            new Biome(COLD, COAST, "Beach", "duller butter"),
            new Biome(HOT, COAST, "Beach", "light duller butter"),
            new Biome(HOTTER, COAST, "Beach", "lighter duller butter"),
            new Biome(HOTTEST, COAST, "Beach", "lighter duller butter"),
            new Biome(COLDEST, RIVER, "Ice", "lightmost dullmost sage"),
            new Biome(COLDER, RIVER, "River", "lighter duller denim"),
            new Biome(COLD, RIVER, "River", "darker richer sky"),
            new Biome(HOT, RIVER, "River", "darker richer sky"),
            new Biome(HOTTER, RIVER, "River", "darker richer sky"),
            new Biome(HOTTEST, RIVER, "River", "darker richmost sky"),
            new Biome(COLDEST, LAKE, "Ice", "lightmost dullmost sky"),
            new Biome(COLDER, LAKE, "Lake", "lighter duller denim"),
            new Biome(COLD, LAKE, "Lake", "darker richer sky"),
            new Biome(HOT, LAKE, "Lake", "darker richer sky"),
            new Biome(HOTTER, LAKE, "Lake", "darker richmost sky"),
            new Biome(HOTTEST, LAKE, "Lake", "darker richmost sky"),
            new Biome(COLDEST, OCEAN, "Ocean", "light duller navy"),
            new Biome(COLDER, OCEAN, "Ocean", "darker duller cobalt"),
            new Biome(COLD, OCEAN, "Ocean", "darkest duller cobalt"),
            new Biome(HOT, OCEAN, "Ocean", "dark duller navy"),
            new Biome(HOTTER, OCEAN, "Ocean", "darkmost duller cobalt"),
            new Biome(HOTTEST, OCEAN, "Ocean", "darkest duller navy"),
            new Biome(COLDEST, STRANGE, "Empty", "darkmost dullmost cobalt"),
            new Biome(COLDER, STRANGE, "Moon", "dark white"),
            new Biome(COLD, STRANGE, "Cavern", "darkest dullmost chocolate"),
            new Biome(HOT, STRANGE, "Cavern", "darkmost dullmost plum"),
            new Biome(HOTTER, STRANGE, "Exotic", "lighter richmost raspberry"),
            new Biome(HOTTEST, STRANGE, "Volcano", "dark richmost ember"),
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
