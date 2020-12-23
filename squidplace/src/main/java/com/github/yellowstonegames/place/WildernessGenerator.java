package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectLongMap;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.Maker;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.yellowstonegames.grid.Direction;

import java.io.Serializable;

/**
 * A finite 2D area map for some kind of wilderness, adapting to different ecosystems by changing its output.
 * Regional maps for wilderness areas have very different requirements from mostly-artificial dungeons. This is intended
 * to work alongside the squidworld module to produce local outdoor maps for specified biomes in a large map, for
 * example, very sparse maps with an occasional cactus in a desert, or very dense maps with many trees and shrubs for a
 * forest.
 * <br>
 * Using this code mostly involves constructing a WildernessGenerator with a width, height, biome, and optionally a random number
 * generator, an ObjectList of floor types (as Strings) that can appear, and an ObjectList of terrain content that can
 * appear (also as Strings). Then you can call {@link #generate()}, which assigns indices into {@link #content} and
 * {@link #floors}, where an index can look up a value from {@link #contentTypes} or {@link #floorTypes}. The biome is
 * currently an int; this will probably change to an enum. The
 * {@link #contentTypes} field is an ObjectList; you can have and are encouraged to have duplicates when an object should
 * appear more often. An index of -1 in content indicates nothing of note is present there. There is also a String array
 * of {@link #floorTypes} that is not typically user-set unless you subclass WildernessGenerator yourself; it is used to look up the
 * indices in {@link #floors}. The floors are set to reasonable values for the particular biome, so a forest has "dirt"
 * and "leaves" among others, while a desert might only have "sand". Again, only the indices matter, so you could change
 * the values in {@link #floorTypes} to match names of textures in a graphical game and make lookup easier, or to a char
 * followed by the name of a color (as in SColor in the display module) for a text-based game.
 * <br>
 * This is marked Beta because there's still some work to be done, and the actual output will change even if the API
 * doesn't have any breaks. While the wilderness maps this produces are usable, they don't have paths or areas that a
 * character would have to find a way around (like a cliff). This is meant to be added at some point, probably in
 * conjunction with some system for connecting WildernessGenerators.
 * <br>
 * Created by Tommy Ettinger on 10/16/2019.
 */
public class WildernessGenerator implements Serializable {
    private static final long serialVersionUID = 1L;
    public final int width, height;
    public int biome;
    public LaserRandom rng;
    public ObjectList<String> contentTypes;
    public ObjectList<String> floorTypes;
    public ObjectLongMap<String> viewer;
    public final int[][] content, floors;

    /**
     * Meant for generating large ObjectLists of Strings where an individual String may occur quite a few times.
     * The rest parameter is a vararg (it may also be an Object array) of alternating String and Integer values, where
     * an Integer is how many times to repeat the preceding String in the returned ObjectList.
     * @param rest a vararg (or Object array) of alternating String and Integer values
     * @return an ObjectList of Strings, probably with some or most of them repeated; you may want to shuffle this result
     */
    public static ObjectList<String> makeRepeats(Object... rest)
    {
        if(rest == null || rest.length < 2)
        {
            return new ObjectList<>(0);
        }
        ObjectList<String> al = new ObjectList<>(rest.length);

        for (int i = 0; i < rest.length - 1; i+=2) {
            try {
                int count = (int)rest[i+1];
                String v = (String) rest[i];
                for (int j = 0; j < count; j++) {
                    al.add(v);
                }
            }catch (ClassCastException ignored) {
            }
        }
        return al;
    }
    public static ObjectList<String> makeShuffledRepeats(LaserRandom rng, Object... rest) 
    {
        final ObjectList<String> al = makeRepeats(rest);
        al.shuffle(rng);
        return al;
    }
//    
//    public static ObjectList<String> makeVegetation(LaserRandom rng, int size, double monoculture, FakeLanguageGen naming)
//    {
//        Thesaurus t = new Thesaurus(rng);
//        ObjectList<String> al = new ObjectList<>(size);
//        String latest;
//        for (int i = size; i > 0; i--) {
//            al.add(latest = t.makePlantName(naming));
//            for (double j = rng.nextDouble(monoculture * 2.0 * size); j >= 1 && i > 0; j--, i--) {
//                al.add(latest);
//            }
//        }
//        rng.shuffleInPlace(al);
//        return al;
//    }
//
    /**
     * Gets a list of Strings that are really just the names of types of floor tile for wilderness areas.
     * @param biome an index into some biome table, WIP
     * @param rng a LaserRandom, can be seeded
     * @return a shuffled ObjectList that typically contains repeats of the kinds of floor that can appear here
     */
    public static ObjectList<String> floorsByBiome(int biome, LaserRandom rng) {
        biome &= 1023;
        switch (biome) {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24:
            case 30:
            case 42:
            case 48:
                return makeShuffledRepeats(rng, "snow", 3, "ice", 1);
            case 7: //Tundra
            case 13:
            case 19:
            case 25:
                return makeShuffledRepeats(rng, "dirt", 6, "pebbles", 1, "snow", 9, "dry grass", 4);
            case 26: //BorealForest
            case 31:
            case 32:
                return makeShuffledRepeats(rng, "dirt", 3, "pebbles", 1, "snow", 11);
            case 43: //River
            case 44:
            case 45:
            case 46:
            case 47:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
                return Maker.list("fresh water");
            case 54: //Ocean
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
                return Maker.list("salt water");
            case 3: //Desert
            case 4:
            case 5:
            case 10:
            case 11:
            case 17:
            case 38: //Beach
            case 39:
            case 40:
            case 41:
                return Maker.list("sand");
            case 2: //Grassland
            case 8:
            case 9:
                return makeShuffledRepeats(rng, "dirt", 8, "dry grass", 13, "grass", 2);
            case 14: //Woodland
            case 15:
                return makeShuffledRepeats(rng, "dirt", 11, "leaves", 3, "dry grass", 8);
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return makeShuffledRepeats(rng, "dirt", 4, "dry grass", 17);
            case 20: //SeasonalForest
            case 21:
                return makeShuffledRepeats(rng, "dirt", 9, "leaves", 6, "grass", 14);
            case 27: //TemperateRainforest
            case 33:
                return makeShuffledRepeats(rng, "mud", 3, "leaves", 8, "grass", 10, "moss", 5);
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return makeShuffledRepeats(rng, "mud", 7, "leaves", 6, "grass", 4, "moss", 11);
            case 36: // Rocky
            case 37:
                return makeShuffledRepeats(rng, "pebbles", 5, "rubble", 1);
            default:
                return Maker.list("empty space");
        }
    }
    /**
     * Gets a list of Strings that are really just the names of types of path tile for wilderness areas.
     * Not currently used.
     * @param biome an index into some biome table, WIP
     * @return an ObjectList that typically contains just the one or few types of path that can appear here
     */
    public static ObjectList<String> pathsByBiome(int biome) {
        biome &= 1023;
        switch (biome) {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24:
            case 30:
            case 42:
            case 48:
                return Maker.list("snow path");
            case 7: //Tundra
            case 13:
            case 19:
            case 25: 
            case 26: //BorealForest
            case 31:
            case 32:
                return Maker.list("snow path", "dirt path");
//            case 43: //River
//            case 44:
//            case 45:
//            case 46:
//            case 47:
//            case 49:
//            case 50:
//            case 51:
//            case 52:
//            case 53:
//            case 54: //Ocean
//            case 55:
//            case 56:
//            case 57:
//            case 58:
//            case 59:
//                return Maker.list("wooden bridge");
            case 3: //Desert
            case 4:
            case 5:
            case 10:
            case 11:
            case 17:
            case 38: //Beach
            case 39:
            case 40:
            case 41:
                return Maker.list("sand path");
            case 2: //Grassland
            case 8:
            case 9:
            case 14: //Woodland
            case 15:
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return Maker.list("dirt path");
            case 20: //SeasonalForest
            case 21:
                return Maker.list("dirt path", "grass path");
            case 27: //TemperateRainforest
            case 33:
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return Maker.list("grass path");
            case 36: // Rocky
            case 37:
                return Maker.list("stone path");
            default:
                return Maker.list("wooden bridge");

        }
    }
    /**
     * Gets a list of Strings that are really just the names of types of terrain feature for wilderness areas.
     * @param biome an index into some biome table, WIP
     * @param rng a LaserRandom, can be seeded
     * @return a shuffled ObjectList that typically contains repeats of the kinds of terrain feature that can appear here
     */
    public static ObjectList<String> contentByBiome(int biome, LaserRandom rng) {
        biome &= 1023;
        switch (biome) {
            case 0: //Ice
            case 1:
            case 6:
            case 12:
            case 18:
            case 24:
            case 30:
            case 42:
            case 48:
                return makeShuffledRepeats(rng, "snow mound", 5, "icy divot", 2, "powder snowdrift", 5);
            case 7: //Tundra
            case 13:
            case 19:
            case 25:
                return makeShuffledRepeats(rng, "snow mound", 4, "hillock", 6, "animal burrow", 5, "small bush 1", 2);
            case 26: //BorealForest
            case 31:
            case 32:
                return makeShuffledRepeats(rng, "snow mound", 3, "small bush 1", 5, "large bush 1", 3, "evergreen tree 1", 17, "evergreen tree 2", 12);
//                case 43: //River
//                case 44:
//                case 45:
//                case 46:
//                case 47:
//                case 49:
//                case 50:
//                case 51:
//                case 52:
//                case 53:
//                case 54: //Ocean
//                case 55:
//                case 56:
//                case 57:
//                case 58:
//                case 59:
//                    return new ObjectList<>(0);
            case 3: //Desert
            case 4:
            case 5:
            case 10:
            case 11:
            case 17:
                return makeShuffledRepeats(rng, "small cactus 1", 2, "large cactus 1", 2, "succulent 1", 1, "animal burrow", 2);
            case 38: //Beach
            case 39:
            case 40:
            case 41:
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 3, "seashell 3", 3, "seashell 4", 3, "driftwood", 5, "boulder", 3);
            case 2: //Grassland
            case 8:
            case 9:
                return makeShuffledRepeats(rng, "deciduous tree 1", 3, "small bush 1", 5, "small bush 2", 4, "large bush 1", 5, "animal burrow", 8, "hillock", 4);
            case 14: //Woodland
            case 15:
                return makeShuffledRepeats(rng, "deciduous tree 1", 12, "deciduous tree 2", 9, "deciduous tree 3", 6, "small bush 1", 4, "small bush 2", 3, "animal burrow", 3);
            case 16: //Savanna
            case 22:
            case 23:
            case 29:
                return makeShuffledRepeats(rng, "small bush 1", 8, "small bush 2", 5, "large bush 1", 2, "animal burrow", 3, "hillock", 6);
            case 20: //SeasonalForest
            case 21:
                return makeShuffledRepeats(rng, "deciduous tree 1", 15, "deciduous tree 2", 13, "deciduous tree 3", 12, "small bush 1", 3, "large bush 1", 5, "large bush 2", 4, "animal burrow", 3);
            case 27: //TemperateRainforest
            case 33:
                return makeShuffledRepeats(rng, "tropical tree 1", 6, "tropical tree 2", 5, "deciduous tree 1", 13, "deciduous tree 2", 12, "small bush 1", 8, "large bush 1", 7, "large bush 2", 7, "large bush 3", 3, "animal burrow", 3);
            case 28: //TropicalRainforest
            case 34:
            case 35:
                return makeShuffledRepeats(rng, "tropical tree 1", 12, "tropical tree 2", 11, "tropical tree 3", 10, "tropical tree 4", 9, "small bush 1", 6, "small bush 2", 5, "large bush 1", 6, "large bush 2", 5, "large bush 3", 3, "animal burrow", 9, "boulder", 1);
            case 36: // Rocky
            case 37:
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 2, "seashell 3", 2, "driftwood", 6, "boulder", 9);
            default:
                return new ObjectList<>(0);
        }
    }

    public static ObjectLongMap<String> defaultViewer(){
        ObjectLongMap<String> viewer = new ObjectLongMap<>(64);
        // find:    new Basic\((.+?),
        // replace: $1 | parse("") << 32);//
        /*
        viewer.put("snow path", '.' | parse("") << 32);//ALICE_BLUE.toEditedFloat(0.0f, -0.2f, -0.15f)));
        viewer.put("dirt path", '.' | parse("") << 32);//CLOVE_BROWN.toEditedFloat(-0.005f, -0.275f, 0.17f)));
        viewer.put("sand path", '.' | parse("") << 32);//CW_PALE_ORANGE.toEditedFloat(0.05f, -0.17f, -0.075f)));
        viewer.put("grass path", '.' | parse("") << 32);//AURORA_DUSTY_GREEN.toEditedFloat(0.0f, -0.15f, -0.1f)));
        viewer.put("stone path", '.' | parse("") << 32);//AURORA_CHIPPED_GRANITE.toEditedFloat(-0.09f, -0.05f, 0.1f)));
        viewer.put("wooden bridge", ':' | parse("") << 32);//BRUSHWOOD_DYED.toEditedFloat(0.0f, -0.275f, 0.05f)));

        viewer.put("ice ledge", '¬' | parse("") << 32);//SColor.toEditedFloat(PALE_CORNFLOWER_BLUE, 0.0f, -0.1f, 0.1f)));
        viewer.put("dirt ledge", '¬' | parse("") << 32);//CLOVE_BROWN.toEditedFloat(-0.005f, -0.175f, -0.18f)));
        viewer.put("sand ledge", '¬' | parse("") << 32);//CW_PALE_ORANGE.toEditedFloat(0.05f, -0.15f, -0.125f)));
        viewer.put("grass ledge", '¬' | parse("") << 32);//AURORA_DUSTY_GREEN.toEditedFloat(0.0f, -0.025f, -0.45f)));
        viewer.put("stone ledge", '¬' | parse("") << 32);//AURORA_CHIPPED_GRANITE.toEditedFloat(-0.07f, -0.1f, -0.25f)));

        viewer.put("snow", '…' | parse("") << 32);//ALICE_BLUE));
        viewer.put("ice", '-' | parse("") << 32);//SColor.lightenFloat(PALE_CORNFLOWER_BLUE, 0.3f)));
        viewer.put("dirt", '·' | parse("") << 32);//CLOVE_BROWN.toEditedFloat(-0.005f, -0.075f, 0.02f)));
        viewer.put("pebbles", '…' | parse("") << 32);//AURORA_WET_STONE.toEditedFloat(0.0f, 0.0f, 0.0f)));
        viewer.put("dry grass", '\'' | parse("") << 32);//CW_FADED_BROWN.toEditedFloat(0.06f, 0.05f, 0.05f)));
        viewer.put("fresh water", '~' | parse("") << 32);//AURORA_BLUE_EYE));
        viewer.put("salt water", '≈' | parse("") << 32);//AURORA_PRUSSIAN_BLUE));
        viewer.put("sand", '…' | parse("") << 32);//CW_PALE_ORANGE.toEditedFloat(0.05f, -0.05f, 0.075f)));
        viewer.put("leaves", '…' | parse("") << 32);//CHINESE_TEA_YELLOW.toEditedFloat(0.02f, -0.025f, 0.0f)));
        viewer.put("grass", '"' | parse("") << 32);//AURORA_DUSTY_GREEN.toEditedFloat(0.0f, 0.075f, -0.25f)));
        viewer.put("mud", ',' | parse("") << 32);//DB_EARTH.toEditedFloat(0.03f, -0.15f, -0.03f)));
        viewer.put("moss", '˝' | parse("") << 32);//AURORA_FERN_GREEN.toEditedFloat(0f, 0.0f, 0.0f)));
        viewer.put("rubble", '‰' | parse("") << 32);//AURORA_CHIPPED_GRANITE.toEditedFloat(-0.07f, 0.0f, -0.05f)));
        viewer.put("empty space", '_' | parse("") << 32);//DB_INK));
        viewer.put("snow mound", '∆' | parse("") << 32);//ALICE_BLUE.toEditedFloat(0f, 0.05f, -0.1f)));
        viewer.put("icy divot", '°' | parse("") << 32);//ALICE_BLUE.toEditedFloat(0.05f, 0.075f, 0.06f)));
        viewer.put("powder snowdrift", '¨' | parse("") << 32);//ALICE_BLUE.toEditedFloat(0.0f, 0.0f, -0.07f)));
        viewer.put("hillock", '∆' | parse("") << 32);//CW_DRAB_BROWN.toEditedFloat(0.1f, -0.05f, 0.25f)));
        viewer.put("animal burrow", '¸' | parse("") << 32);//AURORA_ARMY_GREEN.toEditedFloat(0.05f, 0.0f, -0.05f)));
        viewer.put("small bush 1", '♣' | parse("") << 32);//AURORA_AVOCADO.toEditedFloat(-0.055f, -0.025f, -0.225f)));
        viewer.put("large bush 1", '♣' | parse("") << 32);//AURORA_FOREST_GLEN.toEditedFloat(-0.055f, -0.125f, -0.225f)));
        viewer.put("evergreen tree 1", '♠' | parse("") << 32);//PINE_GREEN.toEditedFloat(-0.13f, -0.03f, -0.05f)));
        viewer.put("evergreen tree 2", '♠' | parse("") << 32);//AURORA_EUCALYPTUS.toEditedFloat(-0.035f, -0.045f, -0.75f)));
        viewer.put("small cactus 1", '‡' | parse("") << 32);//AURORA_FROG_GREEN.toEditedFloat(0.035f, 0.065f, -0.06f)));
        viewer.put("large cactus 1", '‡' | parse("") << 32);//AURORA_MARSH.toEditedFloat(0.04f, 0.11f, -0.03f)));
        viewer.put("succulent 1", '§' | parse("") << 32);//CW_FLUSH_JADE.toEditedFloat(-0.045f, -0.1f, 0.0f)));
        viewer.put("seashell 1", 'ˋ' | parse("") << 32);//CW_LIGHT_APRICOT.toEditedFloat(0.0f, -0.095f, 0.07f)));
        viewer.put("seashell 2", 'ˋ' | parse("") << 32);//CW_PALE_RED.toEditedFloat(0.0f, -0.2f, 0.1f)));
        viewer.put("seashell 3", 'ˋ' | parse("") << 32);//CW_PALE_YELLOW.toEditedFloat(0.0f, 0.02f, 0.05f)));
        viewer.put("seashell 4", 'ˋ' | parse("") << 32);//CW_PALE_VIOLET.toEditedFloat(0.0f, -0.080f, 0.11f)));
        viewer.put("driftwood", '¿' | parse("") << 32);//AURORA_DRIFTWOOD.toEditedFloat(0.0f, -0.25f, 0.04f)));
        viewer.put("boulder", '●' | parse("") << 32);//AURORA_SLOW_CREEK.toEditedFloat(0.0f, -0.01f, 0.0f)));
        viewer.put("deciduous tree 1", '¥' | parse("") << 32);//AURORA_AVOCADO.toEditedFloat(-0.065f, 0.0f, -0.3f)));
        viewer.put("small bush 2", '♣' | parse("") << 32);//AURORA_WOODLANDS.toEditedFloat(-0.045f, -0.05f, -0.025f)));
        viewer.put("deciduous tree 2", '¥' | parse("") << 32);//AURORA_IVY_GREEN.toEditedFloat(-0.02f, 0.0f, 0.0f)));
        viewer.put("deciduous tree 3", '¥' | parse("") << 32);//AURORA_ASPARAGUS.toEditedFloat(-0.015f, 0.055f, 0.02f)));
        viewer.put("large bush 2", '♣' | parse("") << 32);//AURORA_VIRIDIAN.toEditedFloat(-0.03f, -0.05f, 0.03f)));
        viewer.put("tropical tree 1", '¶' | parse("") << 32);//AURORA_FLORAL_FOAM.toEditedFloat(-0.05f, 0.025f, 0.075f)));
        viewer.put("tropical tree 2", '¶' | parse("") << 32);//AURORA_MAIDENHAIR_FERN.toEditedFloat(0.0f, 0.0f, 0.02f)));
        viewer.put("large bush 3", '♣' | parse("") << 32);//AURORA_KELLY_GREEN.toEditedFloat(0.0f, 0.025f, 0.02f)));
        viewer.put("tropical tree 3", '¶' | parse("") << 32);//AURORA_SOFT_TEAL.toEditedFloat(-0.15f, -0.07f, -0.03f)));
        viewer.put("tropical tree 4", '¶' | parse("") << 32);//AURORA_PRASE.toEditedFloat(-0.04f, -0.02f, -0.02f)));
        */
        return viewer;
    }

//                //COLDEST //COLDER        //COLD            //HOT                  //HOTTER              //HOTTEST
//                "Ice",    "Ice",          "Grassland",      "Desert",              "Desert",             "Desert",             //DRYEST
//                "Ice",    "Tundra",       "Grassland",      "Grassland",           "Desert",             "Desert",             //DRYER
//                "Ice",    "Tundra",       "Woodland",       "Woodland",            "Savanna",            "Desert",             //DRY
//                "Ice",    "Tundra",       "SeasonalForest", "SeasonalForest",      "Savanna",            "Savanna",            //WET
//                "Ice",    "Tundra",       "BorealForest",   "TemperateRainforest", "TropicalRainforest", "Savanna",            //WETTER
//                "Ice",    "BorealForest", "BorealForest",   "TemperateRainforest", "TropicalRainforest", "TropicalRainforest", //WETTEST
//                "Rocky",  "Rocky",        "Beach",          "Beach",               "Beach",              "Beach",              //COASTS
//                "Ice",    "River",        "River",          "River",               "River",              "River",              //RIVERS
//                "Ice",    "River",        "River",          "River",               "River",              "River",              //LAKES
//                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
//                "Empty",                                                                                                       //SPACE
    
    public WildernessGenerator()
    {
        this(128, 128, 21);
    }
    public WildernessGenerator(int width, int height, int biome)
    {
        this(width, height, biome, new LaserRandom());
    }
    public WildernessGenerator(int width, int height, int biome, long seedA, long seedB)
    {
        this(width, height, biome, new LaserRandom(seedA, seedB));
    }
    public WildernessGenerator(int width, int height, int biome, LaserRandom rng)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentByBiome(biome, rng), defaultViewer());
    }
    public WildernessGenerator(int width, int height, int biome, LaserRandom rng, ObjectList<String> contentTypes)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentTypes, defaultViewer());
    }
    public WildernessGenerator(int width, int height, int biome, LaserRandom rng, ObjectList<String> floorTypes, ObjectList<String> contentTypes, ObjectLongMap<String> viewer)
    {
        this.width = width;
        this.height = height;
        this.biome = biome;
        this.rng = rng;
        content = ArrayTools.fill(-1, width, height);
        floors = new int[width][height];
        this.floorTypes = floorTypes;
        this.contentTypes = contentTypes;
        this.viewer = viewer;
    }

    /**
     * Produces a map by filling the {@link #floors} 2D array with indices into {@link #floorTypes}, and similarly
     * filling the {@link #content} 2D array with indices into {@link #contentTypes}. You only need to call this method
     * when you first generate a map with the specific parameters you want, such as biome, and later if you want another
     * map with the same parameters.
     * <br>
     * Virtually all of this method is a wrapper around functionality provided by {@link BlueNoise}, adjusted to fit
     * wilderness maps slightly.
     */
    public void generate() {
        ArrayTools.fill(content, -1);
        final int seed = rng.nextInt();
        final int limit = contentTypes.size(), floorLimit = floorTypes.size();
        int b;
        BlueNoise.blueSpill(floors, floorLimit, rng);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if((b = BlueNoise.getChosen(x, y, seed) + 128) < limit)
                    content[x][y] = b;
            }
        }
    }

    /**
     * A subclass of {@link WildernessGenerator} that serves as a ragged edge between 2, 3, or 4 WildernessGenerators in
     * a square intersection. You almost always supply 4 WildernessGenerators to this (typically not other
     * MixedWildernessGenerators), one for each corner of the map, and this generates an uneven border between them.
     * Make sure to look up the indices in the {@link #content} and {@link #floors} using this
     * MixedWildernessGenerator's {@link #contentTypes} and {@link #floorTypes}, not the ones in the inner
     * ]WildernessGenerators, because the indices in the MixedWildernessGenerator are different.
     */
    public static class MixedWildernessGenerator extends WildernessGenerator implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public final int[][] pieceMap;
        public final WildernessGenerator[] pieces;
        protected final int[] minFloors, maxFloors, minContents, maxContents;
        
        public MixedWildernessGenerator()
        {
            this(new WildernessGenerator(), new WildernessGenerator(), new WildernessGenerator(), new WildernessGenerator(), new LaserRandom());
        }
        
        public MixedWildernessGenerator(WildernessGenerator northeast, WildernessGenerator southeast, WildernessGenerator southwest, WildernessGenerator northwest, LaserRandom rng)
        {
            super(northeast.width, northeast.height, northeast.biome, rng, new ObjectList<>(northeast.floorTypes), new ObjectList<>(northeast.contentTypes), northeast.viewer);
            minFloors = new int[4];
            maxFloors = new int[4];
            minContents = new int[4];
            maxContents = new int[4];
            floorTypes.addAll(southeast.floorTypes);
            floorTypes.addAll(southwest.floorTypes);
            floorTypes.addAll(northwest.floorTypes);
            contentTypes.addAll(southeast.contentTypes);
            contentTypes.addAll(southwest.contentTypes);
            contentTypes.addAll(northwest.contentTypes);
            minFloors[1]   = maxFloors[0]   = northeast.floorTypes.size();
            minContents[1] = maxContents[0] = northeast.contentTypes.size();
            minFloors[2]   = maxFloors[1]   = maxFloors[0]    + southeast.floorTypes.size();
            minContents[2] = maxContents[1] = maxContents[0]  + southeast.contentTypes.size();
            minFloors[3]   = maxFloors[2]   = maxFloors[1]    + southwest.floorTypes.size();
            minContents[3] = maxContents[2] = maxContents[1]  + southwest.contentTypes.size();
            maxFloors[3]   = maxFloors[2]    + northwest.floorTypes.size();
            maxContents[3] = maxContents[2]  + northwest.contentTypes.size();
            pieces = new WildernessGenerator[]{northeast, southeast, southwest, northwest};
            pieceMap = new int[width][height];
        }
        
        protected void preparePieceMap()
        {
            ArrayTools.fill(pieceMap, 255);
            pieceMap[width - 1][0] = 0; // northeast
            pieceMap[width - 1][height - 1] = 1; // southeast
            pieceMap[0][height - 1] = 2; // southwest
            pieceMap[0][0] = 3; //northwest
            final int spillerLimit = 4;
            final Direction[] dirs = Direction.CARDINALS;
            Direction d;
            int t, rx, ry, ctr;
            int[] ox = ArrayTools.range(width), oy = ArrayTools.range(height);
            boolean anySuccesses = false;
            do {
                ctr = 0;
                ArrayTools.shuffle(ox, rng);
                ArrayTools.shuffle(oy, rng);
                for (int x = 0; x < width; x++) {
                    rx = ox[x];
                    for (int y = 0; y < height; y++) {
                        ry = oy[y];
                        if ((t = pieceMap[rx][ry]) < spillerLimit) {
                            d = dirs[rng.next(2)];
                            if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                ctr++;
                            }
                            d = dirs[rng.next(2)];
                            if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                ctr++;
                            }
                        }

                    }
                }
                if(!anySuccesses && ctr == 0)
                {
                    ArrayTools.fill(pieceMap, 0);
                    return;
                }
                else
                    anySuccesses = true;
            } while (ctr > 0);
            do {
                ctr = 0;
                ArrayTools.shuffle(ox, rng);
                ArrayTools.shuffle(oy, rng);
                for (int x = 0; x < width; x++) {
                    rx = ox[x];
                    for (int y = 0; y < height; y++) {
                        ry = oy[y];
                        if ((t = pieceMap[rx][ry]) < spillerLimit) {
                            for (int i = 0; i < 4; i++) {
                                d = dirs[i];
                                if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                        pieceMap[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                    pieceMap[rx + d.deltaX][ry + d.deltaY] = t;
                                    ctr++;
                                }
                            }
                        }

                    }
                }
            } while (ctr > 0);

        }

        @Override
        public void generate() {
            ArrayTools.fill(content, -1);
            for (int i = 0; i < pieces.length; i++) {
                pieces[i].generate();
            }
            preparePieceMap();
            int p, c;
            WildernessGenerator piece;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    p = pieceMap[x][y];
                    piece = pieces[p];
                    floors[x][y] = piece.floors[x][y] + minFloors[p];
                    if((c = piece.content[x][y]) >= 0)
                    {
                        content[x][y] = c + minContents[p];
                    }
                }
            }
        }
    }
}
