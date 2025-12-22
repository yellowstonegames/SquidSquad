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

package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.random.*;
import com.github.yellowstonegames.core.*;
import org.junit.Assert;
import org.junit.Test;
import regexodus.Pattern;

import java.util.Arrays;

public class JsonCoreTest {
    @Test
    public void testChar2D() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerChar2D(json);
        char[][] map = new char[][]{
                " ####  #### ".toCharArray(),
                "##..####..##".toCharArray(),
                "#..........#".toCharArray(),
                "#...<......#".toCharArray(),
                "##........##".toCharArray(),
                " #........# ".toCharArray(),
                " #........# ".toCharArray(),
                "##........##".toCharArray(),
                "#..........#".toCharArray(),
                "#..........#".toCharArray(),
                "##..####>.##".toCharArray(),
                " ####  #### ".toCharArray(),
        };
        String data = json.toJson(map);
        System.out.println(data);
        char[][] map2 = json.fromJson(char[][].class, data);
        for (int y = 0; y < map2.length; y++) {
            System.out.println(map2[y]);
        }
        Assert.assertTrue(Arrays.deepEquals(map, map2));
        System.out.println();
    }

    @Test
    public void testInt2D() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerInt2D(json);
        int[][] map = new int[8][];
        FourWheelRandom random = new FourWheelRandom(123L);
        for (int i = 0; i < map.length; i++) {
            map[i] = ArrayTools.range(8);
            random.shuffle(map[i]);
        }
        String data = json.toJson(map);
        System.out.println(data);
        int[][] map2 = json.fromJson(int[][].class, data);
        for (int y = 0; y < map2.length; y++) {
            for (int x = 0; x < map2[0].length; x++) {
                System.out.print(map[y][x]);
                System.out.print(' ');
            }
            System.out.println();
        }
        Assert.assertTrue(Arrays.deepEquals(map, map2));
        System.out.println();
    }

    @Test
    public void testLong2D() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerLong2D(json);
        long[][] map = new long[8][8];
        FourWheelRandom random = new FourWheelRandom(123L);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < 8; j++) {
                map[i][j] = random.nextLong();
            }
            random.shuffle(map[i]);
        }
        String data = json.toJson(map);
        System.out.println(data);
        long[][] map2 = json.fromJson(long[][].class, data);
        for (int y = 0; y < map2.length; y++) {
            for (int x = 0; x < map2[0].length; x++) {
                System.out.print(map[y][x]);
                System.out.print(' ');
            }
            System.out.println();
        }
        Assert.assertTrue(Arrays.deepEquals(map, map2));
        System.out.println();
    }

    /**
     * JSON length: 250
     * Raw length : 336
     */
    @Test
    public void testFloat2D() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerFloat2D(json);
        float[][] map = new float[8][8];
        float f = -0.125f;
        AceRandom random = new AceRandom(12345L);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < 8; j++) {
//                map[i][j] = random.nextFloat();
                map[i][j] = (f += 0.125f);
            }
            random.shuffle(map[i]);
        }
        String data = json.toJson(map);
        System.out.println(data);
        float[][] map2 = json.fromJson(float[][].class, data);
        StringBuilder sb = new StringBuilder(512);
        for (int y = 0; y < map2.length; y++) {
            for (int x = 0; x < map2[0].length; x++) {
                sb.append(map[y][x]);
                if(x + 1 != map2[0].length)
                    sb.append(' ');
            }
            sb.append('\n');
        }
        System.out.println(sb);
        Assert.assertTrue(Arrays.deepEquals(map, map2));
        System.out.println("JSON length: " + data.length());
        System.out.println("Raw length : " + sb.length());
        System.out.println();
    }

    @Test
    public void testDiceRule() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerDiceRule(json);
        Dice.Rule rule = Dice.parseRollRule("3>4d6");
        Dice dice = new Dice(12345L);
        String data = json.toJson(rule);
        System.out.println(data);
        Dice.Rule rule2 = json.fromJson(Dice.Rule.class, data);
        Dice dice2 = new Dice(12345L);
        Assert.assertEquals(rule, rule2);
        Assert.assertEquals(dice.runRollRule(rule), dice2.runRollRule(rule2));
    }

    @Test
    public void testGapShuffler() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerGapShuffler(json);
        {
            FourWheelRandom random = new FourWheelRandom(123L);
            GapShuffler<String> gs = new GapShuffler<>(new String[]{"foo", "bar", "baz", "quux", "meep", "glin"}, random, false);
            String data = json.toJson(gs);
            System.out.println(data);
            GapShuffler<?> gs2 = json.fromJson(GapShuffler.class, data);
            Assert.assertEquals(gs, gs2);
            Assert.assertEquals(gs.next(), gs2.next());
        }
        {
            TricycleRandom random = new TricycleRandom(123L);
            GapShuffler<String> gs = new GapShuffler<>(new String[]{"foo", "bar", "baz", "quux", "meep", "glin"}, random, false);
            String data = json.toJson(gs);
            System.out.println(data);
            GapShuffler<?> gs2 = json.fromJson(GapShuffler.class, data);
            Assert.assertEquals(gs, gs2);
            Assert.assertEquals(gs.next(), gs2.next());
        }
        {
            LaserRandom random = new LaserRandom(123L);
            GapShuffler<String> gs = new GapShuffler<>(new String[]{"foo", "bar", "baz", "quux", "meep", "glin"}, random, false);
            String data = json.toJson(gs);
            System.out.println(data);
            GapShuffler<?> gs2 = json.fromJson(GapShuffler.class, data);
            Assert.assertEquals(gs, gs2);
            Assert.assertEquals(gs.next(), gs2.next());
        }
        {
            DistinctRandom random = new DistinctRandom(123L);
            GapShuffler<String> gs = new GapShuffler<>(new String[]{"foo", "bar", "baz", "quux", "meep", "glin"}, random, false);
            String data = json.toJson(gs);
            System.out.println(data);
            GapShuffler<?> gs2 = json.fromJson(GapShuffler.class, data);
            Assert.assertEquals(gs, gs2);
            Assert.assertEquals(gs.next(), gs2.next());
        }
    }

    @Test
    public void testProbabilityTable(){
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerProbabilityTable(json);
        ProbabilityTable<String> earth = new ProbabilityTable<>("earth");
        ProbabilityTable<String> air = new ProbabilityTable<>("air");
        ProbabilityTable<String> water = new ProbabilityTable<>("water");
        ProbabilityTable<String> fire = new ProbabilityTable<>("fire");
        earth.add("rock", 1).add("dirt", 1);
        air.add("wind", 1).add("cloud", 1);
        water.add("wave", 1).add("depths", 1);
        fire.add("disco", 1).add("inferno", 1);
        ProbabilityTable<String> all = new ProbabilityTable<>("aether");
        all.add(earth, 1).add(air, 1).add(water, 1).add(fire, 1);
        ProbabilityTable<String> copy = all.copy();
        Assert.assertNotNull(all.random());
        Assert.assertNotNull(copy.random());

        String data = json.toJson(all);
        System.out.println(data);
        ProbabilityTable readBack = json.fromJson(ProbabilityTable.class, data);
        Assert.assertEquals(copy.random(), readBack.random());
    }

    @Test
    public void testWeightedTable(){
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerWeightedTable(json);
        String[] items = {"earth", "air", "water", "fire", "aether"};
        WeightedTable table = new WeightedTable(1.0f, 2.0f, 3.5f, 4.1f, 0.6f);
        WeightedTable copy = table.copy();
        int index = table.random(123);
        Assert.assertTrue(index >= 0 && index < items.length);
        index = copy.random(123);
        Assert.assertTrue(index >= 0 && index < items.length);

        String data = json.toJson(table);
        System.out.println(data);
        WeightedTable readBack = json.fromJson(WeightedTable.class, data);
        Assert.assertEquals(copy.random(124), readBack.random(124));
    }

    @Test
    public void testPattern() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerPattern(json);
        Pattern pat = Pattern.compile("(\\w+\\s*){4}", "ui");
        String data = json.toJson(pat);
        Pattern pat2 = json.fromJson(Pattern.class, data);
        Assert.assertEquals(pat, pat2);
        System.out.println(pat.matches("cold chopped beef salad"));
        Assert.assertEquals(pat.matches("cold chopped beef salad"), pat2.matches("cold chopped beef salad"));
        System.out.println(pat.matches("aаαΛe езξεЗΣiτ ιyуλγУo оюσοuμυνv"));
        Assert.assertEquals(pat.matches("aаαΛe езξεЗΣiτ ιyуλγУo оюσοuμυνv"), pat2.matches("aаαΛe езξεЗΣiτ ιyуλγУo оюσοuμυνv"));
    }
    @Test
    public void testIntShuffler() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonCore.registerIntShuffler(json);
        IntShuffler shuffler = new IntShuffler(10, 123456789);
        shuffler.next();
        String data = json.toJson(shuffler);
        System.out.println(data);
        IntShuffler shuffler2 = json.fromJson(IntShuffler.class, data);
        Assert.assertEquals(shuffler, shuffler2);
        Assert.assertEquals(shuffler.next(), shuffler2.next());
        Assert.assertEquals(shuffler.next(), shuffler2.next());
        Assert.assertEquals(shuffler.next(), shuffler2.next());
    }

    @Test
    public void testUniqueIdentifier() {
        Json json = new Json();
        UniqueIdentifier orig = UniqueIdentifier.next();
        String ser = json.toJson(orig);
        System.out.println(ser);
        UniqueIdentifier dser = json.fromJson(UniqueIdentifier.class, ser);
        System.out.println(ser + "   " + json.toJson(dser));
        Assert.assertEquals("Failure with " + ser, orig, dser);
        String serG = json.toJson(UniqueIdentifier.GENERATOR);
        System.out.println(serG);
        orig = UniqueIdentifier.next();
        orig = UniqueIdentifier.next();
        UniqueIdentifier.GENERATOR = json.fromJson(UniqueIdentifier.Generator.class, serG);
        dser = UniqueIdentifier.next();
        dser = UniqueIdentifier.next();
        Assert.assertEquals(orig, dser);
    }

}
