/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.digital.Base;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.ObjectLongMap;
import com.github.tommyettinger.ds.ObjectLongOrderedMap;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.MizuchiRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.Direction;

import java.util.Arrays;

import static com.github.yellowstonegames.core.DescriptiveColor.*;

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
public class WildernessGenerator implements PlaceGenerator {
    public final int width, height;
    public Biome biome;
    public EnhancedRandom rng;
    public ObjectList<String> contentTypes;
    public ObjectList<String> floorTypes;
    public ObjectLongMap<String> viewer;
    public final int[][] content, floors, colors, environment;
    public final char[][] grid;
    public final long[][] glyphs;


    /**
     * Generates a 2D int array (as with {@code new int[width][height]}) and fills it with the (seeded variant) blue
     * noise of this class, finding any points with values less than {@code spillerLimit} when brought into a 0-255
     * range, and then expanding those points pseudo-randomly while keeping the same value for any expanded range as its
     * original point.
     * @param width the width of the 2D array to return
     * @param height the height of the 2D array to return
     * @param spillerLimit the upper exclusive bound for the values that will be present in toFill when this finishes
     * @param rng an EnhancedRandom, such as a MizuchiRandom, to generate random values during expansion
     * @return a 2D int array containing ints from 0 (inclusive) to {@code spillerLimit} (exclusive)
     */
    public static int[][] blueSpill(int width, int height, int spillerLimit, EnhancedRandom rng) {
        return blueSpill(new int[width][height], spillerLimit, rng);
    }

    /**
     * Modifies {@code toFill} in-place by filling it with the (seeded variant) blue noise of this class, finding any
     * points with values less than {@code spillerLimit} when brought into a 0-255 range, and then expanding those
     * points pseudo-randomly while keeping the same value for any expanded range as its original point.
     * @param toFill a 2D int array that will be modified in-place and entirely replaced; its contents don't matter
     * @param spillerLimit the upper exclusive bound for the values that will be present in toFill when this finishes
     * @param rng an EnhancedRandom, such as a MizuchiRandom, to generate random values during expansion
     * @return {@code toFill}, after modifications
     */
    public static int[][] blueSpill(int[][] toFill, int spillerLimit, EnhancedRandom rng) {
        return blueSpill(toFill, spillerLimit, rng, false);
    }
    /**
     * Modifies {@code toFill} in-place by filling it with the (seeded variant) blue noise of this class, finding any
     * points with values less than {@code spillerLimit} when brought into a 0-255 range, and then expanding those
     * points pseudo-randomly while keeping the same value for any expanded range as its original point.
     * @param toFill a 2D int array that will be modified in-place and entirely replaced; its contents don't matter
     * @param spillerLimit the upper exclusive bound for the values that will be present in toFill when this finishes
     * @param rng an EnhancedRandom, such as a MizuchiRandom, to generate random values during expansion
     * @return {@code toFill}, after modifications
     */
    public static int[][] blueSpill(int[][] toFill, int spillerLimit, EnhancedRandom rng, boolean triangular) {
        final int width = toFill.length, height = toFill[0].length,
                xOffset = rng.next(5) + rng.next(5) + 1, yOffset = rng.next(5) + rng.next(5) + 1,
                seed = rng.nextInt();
        final Direction[] dirs = Direction.CARDINALS;
        Direction d;
        int t, rx, ry, ctr;
        if(triangular){
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    toFill[x][y] = BlueNoise.getSeededTriangular(x + xOffset, y + yOffset, seed) + 128;
                }
            }
        }
        else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    toFill[x][y] = BlueNoise.getSeeded(x + xOffset, y + yOffset, seed) + 128;
                }
            }
        }
        int[] ox = new int[width], oy = new int[height];
        boolean anySuccesses = false;
        do {
            ctr = 0;
            ArrayTools.range(ox);
            ArrayTools.range(oy);
            rng.shuffle(ox);
            rng.shuffle(oy);
            for (int x = 0; x < width; x++) {
                rx = ox[x];
                for (int y = 0; y < height; y++) {
                    ry = oy[y];
                    if ((t = toFill[rx][ry]) < spillerLimit) {
                        d = dirs[rng.next(2)];
                        if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                toFill[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                            toFill[rx + d.deltaX][ry + d.deltaY] = t;
                            ctr++;
                        }
                        d = dirs[rng.next(2)];
                        if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                toFill[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                            toFill[rx + d.deltaX][ry + d.deltaY] = t;
                            ctr++;
                        }
                    }

                }
            }
            if(!anySuccesses && ctr == 0)
            {
                ArrayTools.fill(toFill, 0);
                return toFill;
            }
            else
                anySuccesses = true;
        } while (ctr > 0);
        do {
            ctr = 0;
            ArrayTools.range(ox);
            ArrayTools.range(oy);
            rng.shuffle(ox);
            rng.shuffle(oy);
            for (int x = 0; x < width; x++) {
                rx = ox[x];
                for (int y = 0; y < height; y++) {
                    ry = oy[y];
                    if ((t = toFill[rx][ry]) < spillerLimit) {
                        for (int i = 0; i < 4; i++) {
                            d = dirs[i];
                            if (rx + d.deltaX >= 0 && rx + d.deltaX < width && ry + d.deltaY >= 0 && ry + d.deltaY < height &&
                                    toFill[rx + d.deltaX][ry + d.deltaY] >= spillerLimit) {
                                toFill[rx + d.deltaX][ry + d.deltaY] = t;
                                ctr++;
                            }
                        }
                    }

                }
            }
        } while (ctr > 0);
        return toFill;
    }


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
    public static ObjectList<String> makeShuffledRepeats(EnhancedRandom rng, Object... rest)
    {
        final ObjectList<String> al = makeRepeats(rest);
        al.shuffle(rng);
        return al;
    }
//    
//    public static ObjectList<String> makeVegetation(EnhancedRandom rng, int size, double monoculture, FakeLanguageGen naming)
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
     * @param rng an EnhancedRandom, such as a MizuchiRandom, can be seeded
     * @return a shuffled ObjectList that typically contains repeats of the kinds of floor that can appear here
     */
    public static ObjectList<String> floorsByBiome(Biome biome, EnhancedRandom rng) {
        if(biome == null || biome.name == null) return new ObjectList<>(0);
        switch (biome.name) {
            case "Ice":
                return makeShuffledRepeats(rng, "snow", 3, "ice", 1);
            case "Tundra":
                return makeShuffledRepeats(rng, "dirt", 6, "pebbles", 1, "snow", 9, "dry grass", 4);
            case "BorealForest":
                return makeShuffledRepeats(rng, "dirt", 3, "pebbles", 1, "snow", 11);
            case "River":
            case "Lake":
                return ObjectList.with("fresh water");
            case "Ocean":
                return ObjectList.with("salt water");
            case "Desert":
            case "Beach":
                return makeShuffledRepeats(rng, "sand", 9, "pebbles", 1);
            case "Grassland":
                return makeShuffledRepeats(rng, "dirt", 8, "dry grass", 13, "grass", 2);
            case "Woodland":
                return makeShuffledRepeats(rng, "dirt", 11, "leaves", 3, "dry grass", 8);
            case "Savanna":
                return makeShuffledRepeats(rng, "dirt", 4, "dry grass", 17);
            case "SeasonalForest":
                return makeShuffledRepeats(rng, "dirt", 9, "leaves", 6, "grass", 14);
            case "TemperateRainforest":
                return makeShuffledRepeats(rng, "mud", 3, "leaves", 8, "grass", 10, "moss", 5);
            case "TropicalRainforest":
                return makeShuffledRepeats(rng, "mud", 7, "leaves", 6, "grass", 4, "moss", 11);
            case "Rocky":
                return makeShuffledRepeats(rng, "pebbles", 5, "rubble", 1);
            default:
                return ObjectList.with("empty space");
        }
    }
    /**
     * Gets a list of Strings that are really just the names of types of path tile for wilderness areas.
     * Not currently used.
     * @param biome an index into some biome table, WIP
     * @return an ObjectList that typically contains just the one or few types of path that can appear here
     */
    public static ObjectList<String> pathsByBiome(Biome biome) {
        if(biome == null || biome.name == null) return new ObjectList<>(0);
        switch (biome.name) {
            case "Ice":
                return ObjectList.with("snow path");
            case "Tundra":
            case "BorealForest":
                return ObjectList.with("snow path", "dirt path");
            case "Desert":
                return ObjectList.with("sand path");
            case "Beach":
                return ObjectList.with("sand path", "stone path");
            case "Grassland":
            case "Woodland":
            case "Savanna":
            case "TropicalRainforest":
                return ObjectList.with("dirt path");
            case "SeasonalForest":
                return ObjectList.with("dirt path", "grass path");
            case "TemperateRainforest":
                return ObjectList.with("grass path");
            case "Rocky":
            case "Moon":
            case "Volcano":
            case "Exotic":
            case "Cavern":
                return ObjectList.with("stone path");
            default:
                return ObjectList.with("wooden bridge");

        }
    }
    /**
     * Gets a list of Strings that are really just the names of types of terrain feature for wilderness areas.
     * @param biome an index into some biome table, WIP
     * @param rng an EnhancedRandom, such as a MizuchiRandom, can be seeded
     * @return a shuffled ObjectList that typically contains repeats of the kinds of terrain feature that can appear here
     */
    public static ObjectList<String> contentByBiome(Biome biome, EnhancedRandom rng) {
        if(biome == null || biome.name == null) return new ObjectList<>(0);
        switch (biome.name) {
            case "Ice":
                return makeShuffledRepeats(rng, "snow mound", 5, "icy divot", 2, "powder snowdrift", 5);
            case "Tundra":
                return makeShuffledRepeats(rng, "snow mound", 4, "hillock", 6, "animal burrow", 5, "small bush 1", 2);
            case "BorealForest":
                return makeShuffledRepeats(rng, "snow mound", 3, "small bush 1", 5, "large bush 1", 3, "evergreen tree 1", 17, "evergreen tree 2", 12);
//                case "River":
//                case "Ocean":
//                    return new ObjectList<>(0);
            case "Desert":
                return makeShuffledRepeats(rng, "small cactus 1", 2, "large cactus 1", 2, "succulent 1", 1, "animal burrow", 2);
            case "Beach":
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 3, "seashell 3", 3, "seashell 4", 3, "driftwood", 5, "boulder", 3);
            case "Grassland":
                return makeShuffledRepeats(rng, "deciduous tree 1", 3, "small bush 1", 5, "small bush 2", 4, "large bush 1", 5, "animal burrow", 8, "hillock", 4);
            case "Woodland":
                return makeShuffledRepeats(rng, "deciduous tree 1", 12, "deciduous tree 2", 9, "deciduous tree 3", 6, "small bush 1", 4, "small bush 2", 3, "animal burrow", 3);
            case "Savanna":
                return makeShuffledRepeats(rng, "small bush 1", 8, "small bush 2", 5, "large bush 1", 2, "animal burrow", 3, "hillock", 6);
            case "SeasonalForest":
                return makeShuffledRepeats(rng, "deciduous tree 1", 15, "deciduous tree 2", 13, "deciduous tree 3", 12, "small bush 1", 3, "large bush 1", 5, "large bush 2", 4, "animal burrow", 3);
            case "TemperateRainforest":
                return makeShuffledRepeats(rng, "tropical tree 1", 6, "tropical tree 2", 5, "deciduous tree 1", 13, "deciduous tree 2", 12, "small bush 1", 8, "large bush 1", 7, "large bush 2", 7, "large bush 3", 3, "animal burrow", 3);
            case "TropicalRainforest":
                return makeShuffledRepeats(rng, "tropical tree 1", 12, "tropical tree 2", 11, "tropical tree 3", 10, "tropical tree 4", 9, "small bush 1", 6, "small bush 2", 5, "large bush 1", 6, "large bush 2", 5, "large bush 3", 3, "animal burrow", 9, "boulder", 1);
            case "Rocky":
                return makeShuffledRepeats(rng, "seashell 1", 3, "seashell 2", 2, "seashell 3", 2, "driftwood", 6, "boulder", 9);
            default:
                return new ObjectList<>(0);
        }
    }

    public static ObjectLongOrderedMap<String> defaultViewer(){
        ObjectLongOrderedMap<String> viewer = new ObjectLongOrderedMap<>(64);

        viewer.put("snow path", '.' | (long)describeOklab("light dullmost silver") << 32);
        viewer.put("dirt path", '.' | (long)describeOklab("lightmost dullest chocolate") << 32);
        viewer.put("sand path", '.' | (long)describeOklab("dark dullmost butter") << 32);
        viewer.put("grass path", '.' | (long)describeOklab("darker dullmost lime") << 32);
        viewer.put("stone path", '.' | (long)describeOklab("lightmost dullest cyan") << 32);
        viewer.put("wooden bridge", ':' | (long)describeOklab("dark dullmost ember") << 32);
        viewer.put("ice ledge", '¬' | (long)describeOklab("lightest dullmost cyan") << 32);
        viewer.put("dirt ledge", '¬' | (long)describeOklab("dark duller chocolate") << 32);
        viewer.put("sand ledge", '¬' | (long)describeOklab("dark dullmost saffron") << 32);
        viewer.put("grass ledge", '¬' | (long)describeOklab("darker dull moss") << 32);
        viewer.put("stone ledge", '¬' | (long)describeOklab("lighter dullmost cobalt") << 32);
        viewer.put("snow", '…' | (long)describeOklab("lightmost dullmost sky white") << 32);
        viewer.put("ice", '-' | (long)describeOklab("lightmost dull mint") << 32);
        viewer.put("dirt", '·' | (long)describeOklab("dark duller cinnamon brown") << 32);
        viewer.put("pebbles", '…' | (long)describeOklab("dark dullmost peach") << 32);
        viewer.put("dry grass", '\'' | (long)describeOklab("lighter duller brown") << 32);
        viewer.put("fresh water", '~' | (long)describeOklab("rich denim") << 32);
        viewer.put("salt water", '≈' | (long)describeOklab("dull cobalt") << 32);
        viewer.put("sand", '…' | (long)describeOklab("lightest dullest butter") << 32);
        viewer.put("leaves", '…' | (long)describeOklab("pear chocolate brown") << 32);
        viewer.put("grass", '"' | (long)describeOklab("darkest dullest green") << 32);
        viewer.put("mud", ',' | (long)describeOklab("dull chocolate") << 32);
        viewer.put("moss", '˝' | (long)describeOklab("darkest fern") << 32);
        viewer.put("rubble", '‰' | (long)describeOklab("dullmost tan turquoise") << 32);
        viewer.put("empty space", ' ' | (long)describeOklab("darkest dullmost indigo") << 32);
        viewer.put("snow mound", '∆' | (long)describeOklab("lightmost dullmost sky") << 32);
        viewer.put("icy divot", '°' | (long)describeOklab("lighter white denim") << 32);
        viewer.put("powder snowdrift", '¨' | (long)describeOklab("lightmost rich silver") << 32);
        viewer.put("hillock", '∆' | (long)describeOklab("lighter dullest pear") << 32);
        viewer.put("animal burrow", '¸' | (long)describeOklab("darkest duller lime") << 32);
        viewer.put("small bush 1", '♣' | (long)describeOklab("darkest duller pear") << 32);
        viewer.put("large bush 1", '♣' | (long)describeOklab("darker richer fern") << 32);
        viewer.put("evergreen tree 1", '♠' | (long)describeOklab("darkest green") << 32);
        viewer.put("evergreen tree 2", '♠' | (long)describeOklab("darkmost dullmost black") << 32);
        viewer.put("small cactus 1", '‡' | (long)describeOklab("light rich cactus") << 32);
        viewer.put("large cactus 1", '‡' | (long)describeOklab("darker duller celery") << 32);
        viewer.put("succulent 1", '§' | (long)describeOklab("light rich jade") << 32);
        viewer.put("seashell 1", 'ˋ' | (long)describeOklab("lightest dullest saffron") << 32);
        viewer.put("seashell 2", 'ˋ' | (long)describeOklab("lightmost dullest tan") << 32);
        viewer.put("seashell 3", 'ˋ' | (long)describeOklab("lightmost dullest yellow") << 32);
        viewer.put("seashell 4", 'ˋ' | (long)describeOklab("lightest dullest pink") << 32);
        viewer.put("driftwood", '¿' | (long)describeOklab("dullmost gray") << 32);
        viewer.put("boulder", '●' | (long)describeOklab("lighter dullmost teal") << 32);
        viewer.put("deciduous tree 1", '¥' | (long)describeOklab("darker dull olive") << 32);
        viewer.put("small bush 2", '♣' | (long)describeOklab("darkest duller pear") << 32);
        viewer.put("deciduous tree 2", '¥' | (long)describeOklab("dark cactus") << 32);
        viewer.put("deciduous tree 3", '¥' | (long)describeOklab("lightest moss") << 32);
        viewer.put("large bush 2", '♣' | (long)describeOklab("darker dullmost green") << 32);
        viewer.put("tropical tree 1", '¶' | (long)describeOklab("richest fern") << 32);
        viewer.put("tropical tree 2", '¶' | (long)describeOklab("dull fern") << 32);
        viewer.put("large bush 3", '♣' | (long)describeOklab("duller cactus") << 32);
        viewer.put("tropical tree 3", '¶' | (long)describeOklab("richmost fern") << 32);
        viewer.put("tropical tree 4", '¶' | (long)describeOklab("light rich cactus") << 32);

        //// old values
//        viewer.put("snow path", '.' | (long)describe("lightest silver") << 32);
//        viewer.put("dirt path", '.' | (long)describe("lighter dullmost brick") << 32);
//        viewer.put("sand path", '.' | (long)describe("lightmost dullmost bronze") << 32);
//        viewer.put("grass path", '.' | (long)describe("lighter dullmost fern") << 32);
//        viewer.put("stone path", '.' | (long)describe("lighter dullmost cyan") << 32);
//        viewer.put("wooden bridge", ':' | (long)describe("darkest dullmost sage") << 32);
//        viewer.put("ice ledge", '¬' | (long)describe("lightmost dullmost cyan") << 32);
//        viewer.put("dirt ledge", '¬' | (long)describe("darkmost dullmost ember") << 32);
//        viewer.put("sand ledge", '¬' | (long)describe("dullest tan") << 32);
//        viewer.put("grass ledge", '¬' | (long)describe("darkmost dullmost fern") << 32);
//        viewer.put("stone ledge", '¬' | (long)describe("lighter dullmost fern") << 32);
//        viewer.put("snow", '…' | (long)describe("dullmost white") << 32);
//        viewer.put("ice", '-' | (long)describe("rich white") << 32);
//        viewer.put("dirt", '·' | (long)describe("lighter duller chocolate") << 32);
//        viewer.put("pebbles", '…' | (long)describe("lighter dullmost mauve") << 32);
//        viewer.put("dry grass", '\'' | (long)describe("light dullmost saffron") << 32);
//        viewer.put("fresh water", '~' | (long)describe("lightmost duller navy") << 32);
//        viewer.put("salt water", '≈' | (long)describe("light duller cobalt") << 32);
//        viewer.put("sand", '…' | (long)describe("lightest dullest butter") << 32);
//        viewer.put("leaves", '…' | (long)describe("darker dull peach") << 32);
//        viewer.put("grass", '"' | (long)describe("dark dullmost cactus") << 32);
//        viewer.put("mud", ',' | (long)describe("darkmost dullest peach") << 32);
//        viewer.put("moss", '˝' | (long)describe("darker dullmost cactus") << 32);
//        viewer.put("rubble", '‰' | (long)describe("dark dullmost cyan") << 32);
//        viewer.put("empty space", ' ' | (long)describe("lighter rich black") << 32);
//        viewer.put("snow mound", '∆' | (long)describe("lightest dullmost sage") << 32);
//        viewer.put("icy divot", '°' | (long)describe("rich white") << 32);
//        viewer.put("powder snowdrift", '¨' | (long)describe("lightmost dullmost sage") << 32);
//        viewer.put("hillock", '∆' | (long)describe("lighter dullmost saffron") << 32);
//        viewer.put("animal burrow", '¸' | (long)describe("darkmost dullmost silver") << 32);
//        viewer.put("small bush 1", '♣' | (long)describe("darkmost dullmost chartreuse") << 32);
//        viewer.put("large bush 1", '♣' | (long)describe("darkest dullmost cactus") << 32);
//        viewer.put("evergreen tree 1", '♠' | (long)describe("darkest silver") << 32);
//        viewer.put("evergreen tree 2", '♠' | (long)describe("darkmost dullmost black") << 32);
//        viewer.put("small cactus 1", '‡' | (long)describe("light duller jade") << 32);
//        viewer.put("large cactus 1", '‡' | (long)describe("lighter duller fern") << 32);
//        viewer.put("succulent 1", '§' | (long)describe("lightmost dullest cactus") << 32);
//        viewer.put("seashell 1", 'ˋ' | (long)describe("lighter dullest butter") << 32);
//        viewer.put("seashell 2", 'ˋ' | (long)describe("lightmost dullest salmon") << 32);
//        viewer.put("seashell 3", 'ˋ' | (long)describe("dullmost white") << 32);
//        viewer.put("seashell 4", 'ˋ' | (long)describe("lightmost dullest lavender") << 32);
//        viewer.put("driftwood", '¿' | (long)describe("darkest white") << 32);
//        viewer.put("boulder", '●' | (long)describe("dark silver") << 32);
//        viewer.put("deciduous tree 1", '¥' | (long)describe("darkmost dullmost lime") << 32);
//        viewer.put("small bush 2", '♣' | (long)describe("dullmost brown") << 32);
//        viewer.put("deciduous tree 2", '¥' | (long)describe("darkest dullmost green") << 32);
//        viewer.put("deciduous tree 3", '¥' | (long)describe("darkest dullmost butter") << 32);
//        viewer.put("large bush 2", '♣' | (long)describe("darker silver") << 32);
//        viewer.put("tropical tree 1", '¶' | (long)describe("darkest dullest sage") << 32);
//        viewer.put("tropical tree 2", '¶' | (long)describe("light dullmost fern") << 32);
//        viewer.put("large bush 3", '♣' | (long)describe("dark dullmost lime") << 32);
//        viewer.put("tropical tree 3", '¶' | (long)describe("lighter dullmost fern") << 32);
//        viewer.put("tropical tree 4", '¶' | (long)describe("lightest dull fern") << 32);

        /* //// incomplete, used to fill in data
        viewer.put("snow path", '.'
        viewer.put("dirt path", '.'
        viewer.put("sand path", '.'
        viewer.put("grass path", '.'
        viewer.put("stone path", '.'
        viewer.put("wooden bridge", ':'
        viewer.put("ice ledge", '¬'
        viewer.put("dirt ledge", '¬'
        viewer.put("sand ledge", '¬'
        viewer.put("grass ledge", '¬'
        viewer.put("stone ledge", '¬'
        viewer.put("snow", '…'
        viewer.put("ice", '-'
        viewer.put("dirt", '·'
        viewer.put("pebbles", '…'
        viewer.put("dry grass", '\''
        viewer.put("fresh water", '~'
        viewer.put("salt water", '≈'
        viewer.put("sand", '…'
        viewer.put("leaves", '…'
        viewer.put("grass", '"'
        viewer.put("mud", ','
        viewer.put("moss", '˝'
        viewer.put("rubble", '‰'
        viewer.put("empty space", ' '
        viewer.put("snow mound", '∆'
        viewer.put("icy divot", '°'
        viewer.put("powder snowdrift", '¨'
        viewer.put("hillock", '∆'
        viewer.put("animal burrow", '¸'
        viewer.put("small bush 1", '♣'
        viewer.put("large bush 1", '♣'
        viewer.put("evergreen tree 1", '♠'
        viewer.put("evergreen tree 2", '♠'
        viewer.put("small cactus 1", '‡'
        viewer.put("large cactus 1", '‡'
        viewer.put("succulent 1", '§'
        viewer.put("seashell 1", 'ˋ'
        viewer.put("seashell 2", 'ˋ'
        viewer.put("seashell 3", 'ˋ'
        viewer.put("seashell 4", 'ˋ'
        viewer.put("driftwood", '¿'
        viewer.put("boulder", '●'
        viewer.put("deciduous tree 1", '¥'
        viewer.put("small bush 2", '♣'
        viewer.put("deciduous tree 2", '¥'
        viewer.put("deciduous tree 3", '¥'
        viewer.put("large bush 2", '♣'
        viewer.put("tropical tree 1", '¶'
        viewer.put("tropical tree 2", '¶'
        viewer.put("large bush 3", '♣'
        viewer.put("tropical tree 3", '¶'
        viewer.put("tropical tree 4", '¶'
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
//                "Ice",    "Lake",         "Lake",           "Lake",                "Lake",               "Lake",               //LAKES
//                "Ocean",  "Ocean",        "Ocean",          "Ocean",               "Ocean",              "Ocean",              //OCEAN
//                "Space",  "Moon",         "Cavern",         "Cavern",              "Exotic",             "Volcano"             //STRANGE

    public WildernessGenerator()
    {
        this(80, 80, Biome.TABLE[21]);
    }
    public WildernessGenerator(int width, int height, Biome biome)
    {
        this(width, height, biome, new MizuchiRandom());
    }
    public WildernessGenerator(int width, int height, Biome biome, long seedA, long seedB)
    {
        this(width, height, biome, new MizuchiRandom(seedA, seedB));
    }
    public WildernessGenerator(int width, int height, Biome biome, EnhancedRandom rng)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentByBiome(biome, rng), defaultViewer());
    }
    public WildernessGenerator(int width, int height, Biome biome, EnhancedRandom rng, ObjectList<String> contentTypes)
    {
        this(width, height, biome, rng, floorsByBiome(biome, rng), contentTypes, defaultViewer());
    }
    public WildernessGenerator(int width, int height, Biome biome, EnhancedRandom rng, ObjectList<String> floorTypes, ObjectList<String> contentTypes, ObjectLongMap<String> viewer)
    {
        this.width = width;
        this.height = height;
        this.biome = biome;
        this.rng = rng;
        content = ArrayTools.fill(-1, width, height);
        environment = ArrayTools.fill(DungeonTools.NATURAL_FLOOR, width, height);
        floors = new int[width][height];
        colors = new int[width][height];
        grid = new char[width][height];
        glyphs = new long[width][height];
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
    public char[][] generate() {
        ArrayTools.fill(content, -1);
        final int seed = rng.nextInt();
        final int limit = contentTypes.size(), floorLimit = floorTypes.size();
        int b, color;
        long pair;
        blueSpill(floors, floorLimit, rng);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if((b = BlueNoise.getSeeded(x, y, seed) + 128) < limit)
                {
                    pair = viewer.get(contentTypes.get(content[x][y] = b));
                    grid[x][y] = (char) pair;
                    colors[x][y] = toRGBA8888(color = (rng.nextInt() & 0x00000007) ^ (int) (viewer.get(floorTypes.get(floors[x][y])) >>> 32));
                    glyphs[x][y] = (pair & 0xFFFFFFFFL) | (long) toRGBA8888(DescriptiveColor.differentiateLightness((int)(pair >>> 32), color)) << 32;
                }
                else {
                    pair = viewer.get(floorTypes.get(floors[x][y]));
                    grid[x][y] = (char) pair;
                    colors[x][y] = toRGBA8888(color = (rng.nextInt() & 0x00000007) ^ (int) (pair >>> 32));
                    glyphs[x][y] = (pair & 0xFFFFFFFFL) | (long) toRGBA8888(DescriptiveColor.offsetLightness(color)) << 32;
                }
            }
        }
        return grid;
    }

    /**
     * Gets the most recently-produced place as a 2D char array, usually produced by calling {@link #generate()} or
     * some similar method present in a specific implementation. This normally passes a direct reference and not a
     * copy, so you can normally modify the returned array to propagate changes back into this IPlaceGenerator.
     *
     * @return the most recently-produced dungeon/place as a 2D char array
     */
    @Override
    public char[][] getPlaceGrid() {
        return grid;
    }

    /**
     * Gets the most recently-produced place's environment as a 2D int array, where each int is typically a constant
     * in {@link DungeonTools} like {@link DungeonTools#NATURAL_WALL} or {@link DungeonTools#ROOM_FLOOR}.
     *
     * @return the environment of the most recently-produced place, as a 2D int array
     */
    @Override
    public int[][] getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        return "WildernessGenerator{" +
                "width=" + width +
                ", height=" + height +
                ", biome=" + biome +
                ", idHash=" + Base.BASE16.unsigned(System.identityHashCode(this)) +
                '}';
    }

    /**
     * A subclass of {@link WildernessGenerator} that serves as a ragged edge between 2, 3, or 4 WildernessGenerators in
     * a square intersection. You almost always supply 4 WildernessGenerators to this (typically not other
     * MixedWildernessGenerators), one for each corner of the map, and this generates an uneven border between them.
     * Make sure to look up the indices in the {@link #content} and {@link #floors} using this
     * MixedWildernessGenerator's {@link #contentTypes} and {@link #floorTypes}, not the ones in the inner
     * ]WildernessGenerators, because the indices in the MixedWildernessGenerator are different.
     */
    public static class MixedWildernessGenerator extends WildernessGenerator {
        public final int[][] pieceMap;
        public final WildernessGenerator[] pieces;
        protected final int[] minFloors, maxFloors, minContents, maxContents;
        
        public MixedWildernessGenerator() {
            this(new WildernessGenerator(), new WildernessGenerator(), new WildernessGenerator(), new WildernessGenerator(), new MizuchiRandom());
        }
        
        public MixedWildernessGenerator(WildernessGenerator northeast, WildernessGenerator southeast, WildernessGenerator southwest, WildernessGenerator northwest, EnhancedRandom rng) {
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
        
        protected void preparePieceMap() {
            ArrayTools.fill(pieceMap, 255);
            pieceMap[width - 1][height - 1] = 0; // northeast
            pieceMap[width - 1][0] = 1; // southeast
            pieceMap[0][0] = 2; // southwest
            pieceMap[0][height - 1] = 3; //northwest
            final int spillerLimit = 4;
            final Direction[] dirs = Direction.CARDINALS;
            Direction d;
            int t, rx, ry, ctr;
            int[] ox = ArrayTools.range(width), oy = ArrayTools.range(height);
            boolean anySuccesses = false;
            do {
                ctr = 0;
                rng.shuffle(ox);
                rng.shuffle(oy);
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
                rng.shuffle(ox);
                rng.shuffle(oy);
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
        public char[][] generate() {
            ArrayTools.fill(content, -1);
            for (int i = 0; i < pieces.length; i++) {
                pieces[i].generate();
            }
            preparePieceMap();
            int p, c, color, floor;
            long pair;
            WildernessGenerator piece;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    p = pieceMap[x][y];
                    piece = pieces[p];
                    floors[x][y] = floor = piece.floors[x][y] + minFloors[p];
                    if ((c = piece.content[x][y]) >= 0) {
                        pair = viewer.get(contentTypes.get(content[x][y] = c + minContents[p]));
                        grid[x][y] = (char) pair;
                        colors[x][y] = toRGBA8888(color = (rng.nextInt() & 0x00000007) ^ (int) (viewer.get(floorTypes.get(floor)) >>> 32));
                        glyphs[x][y] = (pair & 0xFFFFFFFFL) | (long) toRGBA8888(DescriptiveColor.differentiateLightness((int)(pair >>> 32), color)) << 32;

                    } else {
                        pair = viewer.get(floorTypes.get(floors[x][y]));
                        grid[x][y] = (char) pair;
                        colors[x][y] = toRGBA8888(color = (rng.nextInt() & 0x00000007) ^ (int) (pair >>> 32));
                        glyphs[x][y] = (pair & 0xFFFFFFFFL) | (long) toRGBA8888(DescriptiveColor.offsetLightness(color)) << 32;
                    }
                }
            }
            return grid;
        }

        @Override
        public String toString() {
            return "MixedWildernessGenerator{" +
                    "pieces=" + Arrays.toString(pieces) +
                    ", idHash=" + Base.BASE16.unsigned(System.identityHashCode(this)) +
                    '}';
        }
    }
}
