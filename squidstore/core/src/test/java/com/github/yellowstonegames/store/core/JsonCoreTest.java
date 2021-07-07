package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.yellowstonegames.core.ArrayTools;
import com.github.yellowstonegames.core.Dice;
import com.github.yellowstonegames.core.GapShuffler;
import org.junit.Assert;
import org.junit.Test;

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
}
