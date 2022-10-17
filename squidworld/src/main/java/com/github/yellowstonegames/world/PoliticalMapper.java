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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectSet;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.grid.Direction;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.util.Collection;
import java.util.Collections;

/**
 * When you have a world map as produced by {@link WorldMapGenerator}, you may want to fill it with claims by various
 * factions, where each faction may be hand-made and may consist of humans or some fantasy species, such as goblins,
 * elves, or demons. This can assign contiguous areas of land to various factions, while acknowledging any preferences
 * some species may have for specific types of land (elves may strongly prefer forest terrain, or flying demons may be
 * the ideal residents for difficult mountainous terrain). This needs both a {@link WorldMapGenerator} and a
 * {@link BiomeMapper} to allocate biomes and height/moisture info. If allocating human groups only, you might want to
 * decide where a faction would settle by what technologies they use regularly; a seafaring culture would have a
 * {@link Faction#preferredHeight} that includes 4, so they can settle on shorelines, while a horse-riding culture might
 * have {@link Faction#preferredBiomes} that include "Grassland" and "Savanna" so they can ride easily.
 * The WorldMapGenerator is commonly a {@link LocalMap} for continent/area maps, or a {@link StretchWorldMap} for a
 * world map for a familiar shape for world maps, but an {@link EllipticalWorldMap} or {@link HyperellipticalWorldMap}
 * can look better if important areas are in the corners of the rectangular area, though they are less familiar map
 * shapes). {@link LocalMap} and {@link MimicLocalMap} may be better if you don't want world-scale features like polar
 * ice caps or a warm equator. You probably don't want a {@link GlobeMap} or {@link RotatingGlobeMap} because those
 * don't show the edges of the map with the same size as the center, and don't show the back of the globe at all.
 */
public class PoliticalMapper {
    /**
     * Represents a group that claims territory on a world-map, such as a nation. Each Faction has a name, a short name
     * that may be the same as the regular name, a Language that would be used to generate place names in that
     * Faction's territory, and a number of (possibly null) arrays and Sets that represent what terrains this Faction
     * wants to claim and which it will avoid (e.g. elves may prefer claiming forests, while frost giants won't claim
     * anywhere that's even remotely warm). Humans in all-human settings probably won't have especially restrictive
     * preferences, but the technology they have already mastered may only be applicable in certain areas, like a height
     * of 4 (shoreline) for seafaring groups, or preferredBiomes that include "Grassland" for equestrians.
     */
    public static class Faction {
        /**
         * The (typically longer) formal name of the Faction, such as "The United States of America" or "The Noble
         * Fiefdom of Harjalia".
         */
        public String name;
        /**
         * The short name of the Faction, such as "America" or "Harjalia".
         */
        public String shortName;
        /**
         * What {@link Language} to use to generate place names and the names of nations.
         */
        public Language language;
        /**
         * An ObjectSet of String keys, where each key is the name of a biome this Faction wants to occupy.
         * May be null if no biomes are specifically preferred.
         */
        public ObjectSet<String> preferredBiomes;
        /**
         * An ObjectSet of String keys, where each key is the name of a biome this Faction will never occupy.
         * May be null if all biomes are available, but unless this is specified in a constructor, the default will be
         * to consider "Ocean" blocked.
         */
        public ObjectSet<String> blockedBiomes;

        /**
         * An int array of height codes that this Faction prefers; 0, 1, 2, and 3 are all oceans, while 4 is shoreline
         * or low-lying land and higher numbers (up to 8, inclusive) are used for increasing elevations.
         */
        public int[] preferredHeight;
        /**
         * An int array of heat codes that this Faction prefers; typically a 6-code scale is used where 0, 1, and 2 are
         * cold and getting progressively warmer, while 3, 4, and 5 are warm to warmest.
         */
        public int[] preferredHeat;
        /**
         * An int array of moisture codes that this Faction prefers; typically a 6-code scale is used where 0, 1, and 2
         * are dry and getting progressively more precipitation, while 3, 4, and 5 are wet to wettest.
         */
        public int[] preferredMoisture;

        /**
         * Zero-arg constructor that sets the language to a random Language (using
         * {@link Language#randomLanguage(long)}), then generates a name/shortName with that Language, and
         * makes the only blocked biome "Ocean".
         */
        public Faction()
        {
            language = Language.randomLanguage(EnhancedRandom.seedFromMath());
            shortName = name = language.word(true);
            this.blockedBiomes = ObjectSet.with("Ocean");
        }

        /**
         * Constructor that sets the language to the specified Language, then generates a name/shortName with
         * that Language, and makes the only blocked biome "Ocean".
         * @param language the Language to use for generating the name of the Faction and potentially place names
         */
        public Faction(Language language)
        {
            this.language = language;
            shortName = name = language.word(true);
            this.blockedBiomes = ObjectSet.with("Ocean");
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name and shortName to the
         * specified name, and makes the only blocked biome "Ocean".
         * @param language the Language to use for potentially generating place names
         * @param name the name of the Faction, such as "The United States of America"; will also be the shortName
         */
        public Faction(Language language, String name)
        {
            this.language = language;
            shortName = this.name = name;
            this.blockedBiomes = ObjectSet.with("Ocean");
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, and makes the only blocked biome "Ocean".
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         */
        public Faction(Language language, String name, String shortName)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.blockedBiomes = ObjectSet.with("Ocean");
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, and makes the only blocked biome "Ocean". The exact String names that are viable for
         * biomes can be obtained from a BiomeMapper with {@link BiomeMapper#getBiomeTable()}, or from the constants
         * ending in "_BIOME" in {@link com.github.yellowstonegames.place.Biome}.
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link BiomeMapper#getBiomeTable()} value
         *
         */
        public Faction(Language language, String name, String shortName, String[] preferredBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new ObjectSet<>(preferredBiomes);
            this.blockedBiomes = ObjectSet.with("Ocean");
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, and sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes. The exact String names that are viable for biomes can be obtained from a BiomeMapper with
         * {@link BiomeMapper#getBiomeTable()}.
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link BiomeMapper#getBiomeTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         */
        public Faction(Language language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new ObjectSet<>(preferredBiomes);
            this.blockedBiomes = new ObjectSet<>(blockedBiomes);
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, and sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and
         * 8 being the highest peaks). The exact String names that are viable for biomes can be obtained from a
         * BiomeMapper with {@link BiomeMapper#getBiomeTable()}.
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link BiomeMapper#getBiomeTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         */
        public Faction(Language language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes, int[] preferredHeight)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new ObjectSet<>(preferredBiomes);
            this.blockedBiomes = new ObjectSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
        }

        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and 8
         * being the highest peaks), and sets the preferred heat codes to the ints in preferredHeat (with the exact
         * values depending on the BiomeMapper, but usually 0-5 range from coldest to hottest). The exact String names
         * that are viable for biomes can be obtained from a BiomeMapper with
         * {@link BiomeMapper#getBiomeTable()}.
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link BiomeMapper#getBiomeTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         * @param preferredHeat an int array of heat codes this Faction prefers to claim; typically 0 is coldest and 5 is hottest
         */
        public Faction(Language language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes,
                       int[] preferredHeight, int[] preferredHeat)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new ObjectSet<>(preferredBiomes);
            this.blockedBiomes = new ObjectSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
            this.preferredHeat = preferredHeat;
        }
        /**
         * Constructor that sets the language to the specified Language, sets the name to the specified name and
         * the shortName to the specified shortName, sets the preferredBiomes to be a Set containing the given Strings
         * in preferredBiomes, sets the blocked biomes to be a Set containing exactly the given Strings in
         * blockedBiomes, sets the preferred height codes to the ints in preferredHeight (with 4 being sea level and 8
         * being the highest peaks), sets the preferred heat codes to the ints in preferredHeat (with the exact values
         * depending on the BiomeMapper, but usually 0-5 range from coldest to hottest), and sets the preferred moisture
         * codes to the ints in preferredMoisture (withe the exact values depending on the BiomeMapper, but usually 0-5
         * range from driest to wettest). The exact String names that are viable for biomes can be obtained from a
         * BiomeMapper with {@link BiomeMapper#getBiomeTable()}.
         * @param language the Language to use for potentially generating place names
         * @param name the formal name of the Faction, such as "The United States of America"
         * @param shortName the short name of the Faction, such as "America"
         * @param preferredBiomes a String array of biome names that this Faction prefers, typically taken from a BiomeMapper's {@link BiomeMapper#getBiomeTable()} value
         * @param blockedBiomes a String array of biome names that this Faction will never claim; if empty, this Faction may claim oceans
         * @param preferredHeight an int array of height codes this Faction prefers to claim; 4 is sea level and 8 is highest
         * @param preferredHeat an int array of heat codes this Faction prefers to claim; typically 0 is coldest and 5 is hottest
         * @param preferredMoisture an int array of moisture codes this Faction prefers to claim; typically 0 is driest and 5 is wettest
         */
        public Faction(Language language, String name, String shortName, String[] preferredBiomes, String[] blockedBiomes,
                       int[] preferredHeight, int[] preferredHeat, int[] preferredMoisture)
        {
            this.language = language;
            this.name = name;
            this.shortName = shortName;
            this.preferredBiomes = new ObjectSet<>(preferredBiomes);
            this.blockedBiomes = new ObjectSet<>(blockedBiomes);
            this.preferredHeight = preferredHeight;
            this.preferredHeat = preferredHeat;
            this.preferredMoisture = preferredMoisture;
        }
    }

    public int width;
    public int height;
    public EnhancedRandom rng;
    public String name;
    public char[][] politicalMap;
    public char[][] zoomedMap;
    public WorldMapGenerator wmg;
    public BiomeMapper biomeMapper;
    private static final IntList letters = IntList.with(
            '~', '%', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
            'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'À', 'Á', 'Â', 'Ã', 'Ä', 'Å', 'Æ', 'Ç', 'È', 'É', 'Ê', 'Ë', 'Ì', 'Í', 'Î', 'Ï', 'Ð', 'Ñ', 'Ò', 'Ó', 'Ô', 'Õ', 'Ö', 'Ø', 'Ù', 'Ú', 'Û',
            'Ü', 'Ý', 'Þ', 'ß', 'à', 'á', 'â', 'ã', 'ä', 'å', 'æ', 'ç', 'è', 'é', 'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ð', 'ñ', 'ò', 'ó', 'ô', 'õ', 'ö',
            'ø', 'ù', 'ú', 'û', 'ü', 'ý', 'þ', 'ÿ', 'Ā', 'ā', 'Ă', 'ă', 'Ą', 'ą', 'Ć', 'ć', 'Ĉ', 'ĉ', 'Ċ', 'ċ', 'Č', 'č', 'Ď', 'ď', 'Đ', 'đ', 'Ē',
            'ē', 'Ĕ', 'ĕ', 'Ė', 'ė', 'Ę', 'ę', 'Ě', 'ě', 'Ĝ', 'ĝ', 'Ğ', 'ğ', 'Ġ', 'ġ', 'Ģ', 'ģ', 'Ĥ', 'ĥ', 'Ħ', 'ħ', 'Ĩ', 'ĩ', 'Ī', 'ī', 'Ĭ', 'ĭ',
            'Į', 'į', 'İ', 'ı', 'Ĵ', 'ĵ', 'Ķ', 'ķ', 'ĸ', 'Ĺ', 'ĺ', 'Ļ', 'ļ', 'Ľ', 'ľ', 'Ŀ', 'ŀ', 'Ł', 'ł', 'Ń', 'ń', 'Ņ', 'ņ', 'Ň', 'ň', 'ŉ', 'Ō',
            'ō', 'Ŏ', 'ŏ', 'Ő', 'ő', 'Œ', 'œ', 'Ŕ', 'ŕ', 'Ŗ', 'ŗ', 'Ř', 'ř', 'Ś', 'ś', 'Ŝ', 'ŝ', 'Ş', 'ş', 'Š', 'š', 'Ţ', 'ţ', 'Ť', 'ť', 'Ŧ', 'ŧ',
            'Ũ', 'ũ', 'Ū', 'ū', 'Ŭ', 'ŭ', 'Ů', 'ů', 'Ű', 'ű', 'Ų', 'ų', 'Ŵ', 'ŵ', 'Ŷ', 'ŷ', 'Ÿ', 'Ź', 'ź', 'Ż', 'ż', 'Ž', 'ž', 'Ǿ', 'ǿ', 'Ș', 'ș',
            'Ț', 'ț', 'Γ', 'Δ', 'Θ', 'Λ', 'Ξ', 'Π', 'Σ', 'Φ', 'Ψ', 'Ω', 'α');
    /**
     * Maps chars, as found in the returned array from generate(), to Strings that store the full name of nations.
     */
    public IntObjectOrderedMap<Faction> atlas;

    /**
     * Constructs a FantasyPoliticalMapper, but doesn't do anything with a map; you need to call
     * {@link #generate(long, WorldMapGenerator, BiomeMapper, Collection, int, float)} for results.
     */
    public PoliticalMapper()
    {
        rng = new WhiskerRandom();
    }

    /**
     * For when you really don't care what arguments you give this, you can use this zero-parameter overload of
     * generate() to produce a 128x128 {@link LocalMap} world map with a
     * {@link BiomeMapper.SimpleBiomeMapper} biome mapper, filling it with 30 random
     * Factions and trying to avoid unclaimed land. You may need to use {@link #atlas} to make sense of the randomly
     * generated Factions. The seed will be random here.
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate() {
        wmg = new LocalMap(rng.nextLong(), 128, 128);
        wmg.generate();
        biomeMapper = new BiomeMapper.SimpleBiomeMapper();
        biomeMapper.makeBiomes(wmg);
        return generate(rng.nextLong(), wmg, biomeMapper, null, 30, 1f);
    }

    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds randomly generated
     * Factions to fill factionCount (the two default factions aren't counted against this limit). These Factions
     * typically claim contiguous spans of land stretching out from a starting point that matches the Faction's
     * preferences for biome, land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") 
     * and the world has none of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or
     * out, you should call {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area.
     * This overload tries to claim all land that can be reached by an existing Faction, though islands will often be
     * unclaimed.
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, BiomeMapper biomeMapper,
                             int factionCount) {
        return generate(seed, wmg, biomeMapper, null, factionCount, 1f);
    }
    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds randomly generated
     * Factions to fill factionCount (the two default factions aren't counted against this limit). These Factions
     * typically claim contiguous spans of land stretching out from a starting point that matches the Faction's
     * preferences for biome, land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") 
     * and the world has none of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or
     * out, you should call {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area.
     * This overload tries to claim the given {@code controlledFraction} of land in total, though 1.0 can rarely be
     * reached unless there are many factions and few islands.
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, BiomeMapper biomeMapper,
                             int factionCount, float controlledFraction) {
        return generate(seed, wmg, biomeMapper, null, factionCount, controlledFraction);
    }
    /**
     * Generates a 2D char array that represents the claims to the land described by the WorldMapGenerator {@code wmg}
     * and the BiomeMapper {@code biomeMapper} by various Factions, where {@link Faction} is an inner class.
     * This starts with two default Factions for "Ocean" and "Wilderness" (unclaimed land) and adds all of
     * {@code factions} until {@code factionCount} is reached; if it isn't reached, random Factions will be generated to
     * fill factionCount (the two default factions aren't counted against this limit). These Factions typically claim
     * contiguous spans of land stretching out from a starting point that matches the Faction's preferences for biome,
     * land height, heat, and moisture. If a Faction requires a biome (like "TropicalRainforest") and the world has none
     * of that type, then that Faction won't claim any land. If the WorldMapGenerator zooms in or out, you should call
     * {@link #adjustZoom()} to get a different 2D char array that represents the zoomed-in area. This overload tries to
     * claim the given {@code controlledFraction} of land in total, though 1.0 can rarely be reached unless there are
     * many factions and few islands.
     *
     * @param seed the seed that determines how Factions will randomly spread around the world
     * @param wmg a WorldMapGenerator, which must have produced a map by calling its generate() method
     * @param biomeMapper a WorldMapGenerator.BiomeMapper, which must have been initialized with wmg and refer to the same world
     * @param factions a Collection of {@link Faction} that will be copied, shuffled and used before adding any random Factions
     * @param factionCount the number of factions to have claiming land; cannot be negative or more than 253
     * @param controlledFraction between 0.0 and 1.0 inclusive; higher means more land has a letter, lower has more '%'
     * @return a 2D char array where each char can be used as a key into {@link #atlas} to find the Faction that claims it
     */
    public char[][] generate(long seed, WorldMapGenerator wmg, BiomeMapper biomeMapper,
                                  Collection<Faction> factions, int factionCount, float controlledFraction) {
        rng.setSeed(seed);
        factionCount = Math.abs(factionCount % 254);
        Thesaurus th = new Thesaurus(rng.nextLong());
        ObjectList<Faction> fact = factions == null ? new ObjectList<>() : new ObjectList<>(factions);
        for (int i = fact.size(); i < factionCount; i++) {
            String name = th.makeNationName(), shortName = th.latestGenerated;
            Language lang;
            if(th.randomLanguages == null || th.randomLanguages.isEmpty())
                lang = Language.randomLanguage(rng);
            else
                lang = th.randomLanguages.get(0);
            fact.add(new Faction(lang, name, shortName));
        }
        if(factionCount > 0)
            fact.shuffle(rng);
        fact.add(0, new Faction(Language.DEMONIC, "The Lost Wilderness", "Wilderness"));
        fact.add(0, new Faction(Language.ALIEN_O, "The Vast Domain of the Seafolk", "Seafolk", new String[]{"Ocean"}));
        IntList subLetters = new IntList(factionCount + 2);
        subLetters.addAll(letters, 0, factionCount + 2);
        atlas = new IntObjectOrderedMap<>(subLetters, fact.subList(0, factionCount + 2));
        this.wmg = wmg;
        this.biomeMapper = biomeMapper;
        width = wmg.width;
        height = wmg.height;
        Region land = new Region(wmg.heightCodeData, 4, 999);
        politicalMap = land.toChars('%', '~');
        int controlled = (int) (land.size() * Math.max(0.0, Math.min(1.0, controlledFraction)));

        int[] centers = land.copy().randomScatter(rng, (int) (Math.sqrt(width * height) * 0.1 + 0.999), factionCount).asTightEncoded();
        int cen, cx, cy, cx2, cy2, biome, high, hot, moist, count = centers.length, re;
        String biomeName;
        Biome[] biomeTable = biomeMapper.getBiomeTable();
        int[] reorder = new int[count];
        Faction current;
        int[] factionIndices = new int[count];
        for (int c = 0; c < count; c++) {
            cen = centers[c];
            cx = cen % width;
            cy = cen / width;
            biome = biomeMapper.getBiomeCode(cx, cy);
            biomeName = biomeTable[biome].name;
            high = wmg.heightCodeData[cx][cy];
            hot = biomeMapper.getHeatCode(cx, cy);
            moist = biomeMapper.getMoistureCode(cx, cy);
            rng.shuffle(ArrayTools.range(reorder));
            PER_FACTION:
            for (int i = 0; i < count; i++) {
                current = fact.get(re = reorder[i]);
                if(current.preferredBiomes == null || current.preferredBiomes.contains(biomeName))
                {
                    factionIndices[c] = re;
                    break;
                }
                if(current.blockedBiomes != null && current.blockedBiomes.contains(biomeName))
                    continue;
                if(current.preferredHeight != null)
                {
                    for (int j = 0; j < current.preferredHeight.length; j++) {
                        if(high == current.preferredHeight[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
                if(current.preferredHeat != null)
                {
                    for (int j = 0; j < current.preferredHeat.length; j++) {
                        if(hot == current.preferredHeat[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
                if(current.preferredMoisture != null)
                {
                    for (int j = 0; j < current.preferredMoisture.length; j++) {
                        if(moist == current.preferredMoisture[j])
                        {
                            factionIndices[c] = re;
                            break PER_FACTION;
                        }
                    }
                }
            }
        }
        IntList[] fresh = new IntList[count];
        int filled = 0;
        boolean hasFresh = false;
        int approximateArea = (controlled * 4) / (count * 3);
        char[] keys = new char[count];
        double[] biases = new double[count];
        for (int i = 0; i < count; i++) {
            fresh[i] = new IntList(approximateArea);
            cen = centers[i];
            fresh[i].add(cen);
            cx = cen % width;
            cy = cen / width;
            politicalMap[cx][cy] = keys[i] = (char) atlas.keyAt(factionIndices[i] + 2);
            biases[i] = rng.nextDouble() * rng.nextDouble() + rng.nextDouble() + 0.03125;
            hasFresh = true;
        }
        Direction[] dirs = Direction.CARDINALS;
        IntList currentFresh;
        Region anySpillMap = new Region(width, height),
                anyFreshMap = new Region(width, height);

        while (hasFresh && filled < controlled) {
            hasFresh = false;
            for (int i = 0; i < count && filled < controlled; i++) {
                currentFresh = fresh[i];
                if (currentFresh.isEmpty())
                    continue;
                else
                    hasFresh = true;
                if (rng.nextDouble() < biases[i]) {
                    int index = rng.nextInt(currentFresh.size()), cell = currentFresh.removeAt(index);
                    cx = cell % width;
                    cy = cell / width;


                    politicalMap[cx][cy] = keys[i];
                    filled++;
                    anySpillMap.insert(cx, cy);

                    for (int d = 0; d < dirs.length; d++) {
                        cx2 = wmg.wrapX(cx + dirs[d].deltaX, cy);
                        cy2 = wmg.wrapY(cx, cy + dirs[d].deltaY);
                        if (cx == cx2 && cy == cy2)
                            continue;
                        if (land.contains(cx2, cy2) && !anySpillMap.contains(cx2, cy2)) {
                            if(!anyFreshMap.contains(cx2, cy2)) {
                                currentFresh.add(cx2 + cy2 * width);
                                anyFreshMap.insert(cx2, cy2);
                            }
                        }
                    }
                    anyFreshMap.remove(cx, cy);
                }
            }
        }
        zoomedMap = ArrayTools.copy(politicalMap);
        name = Language.SIMPLISH.word(rng.getSelectedState(0), true);
        return politicalMap;
    }

    /**
     * If the WorldMapGenerator used by
     * {@link #generate(long, WorldMapGenerator, BiomeMapper, Collection, int, float)} zooms in or
     * out, you can call this method to make the {@link #zoomedMap} 2D char array match its zoom. The world-scale map,
     * {@link #politicalMap}, will remain unchanged unless generate() is called again, but zoomedMap will change each
     * time either generate() or adjustZoom() is called. This method isn't 100% precise on how it places borders; for
     * aesthetic reasons, the borders are tattered with {@link Region#fray(float)} so they don't look like a
     * wall of angular bubbles. Using fray() at each level of zoom is quasi-random, so if you zoom in on the same
     * sequence of points on two different occasions, the change from fray() will be the same, but it may be slightly
     * different if any point of zoom is different.
     * @return a direct reference to {@link #zoomedMap}, which will hold the correctly-zoomed version of {@link #politicalMap}
     */
    public char[][] adjustZoom() {
        if(wmg.zoom <= 0)
        {
            return ArrayTools.insert(politicalMap, zoomedMap, 0, 0);
        }
        ArrayTools.fill(zoomedMap, ' ');
        char c;
        int stx = Math.min(Math.max((wmg.zoomStartX - (width  >> 1)) / ((2 << wmg.zoom) - 2), 0), width ),
                sty = Math.min(Math.max((wmg.zoomStartY - (height >> 1)) / ((2 << wmg.zoom) - 2), 0), height);
        Region nation = new Region(wmg.landData);
        Region fillable = new Region(politicalMap, '~').not();
        for (int i = 0; i < wmg.zoom; i++) {
            fillable.zoom(stx, sty);
        }
        fillable.flood(nation, width + height);
        for (int i = 1; i < atlas.size(); i++) {
            nation.refill(politicalMap, c = (char) atlas.keyAt(i));
            if(nation.isEmpty()) continue;
            for (int z = 0; z < wmg.zoom; z++) {
                nation.zoom(stx, sty).expand8way().expand().fray(0.5f);
            }
            fillable.andNot(nation);
            nation.intoChars(zoomedMap, c);
        }
        for (int i = 1; i < atlas.size(); i++) {
            nation.refill(zoomedMap, c = (char) atlas.keyAt(i));
            if(nation.isEmpty()) continue;
            nation.flood(fillable, 4 << wmg.zoom).intoChars(zoomedMap, c);
        }
        nation.refill(wmg.heightCodeData, 4, 999).and(new Region(zoomedMap, ' ')).intoChars(zoomedMap, '%');
        nation.refill(wmg.heightCodeData, -999, 4).intoChars(zoomedMap, '~');
        return zoomedMap;
    }

}
