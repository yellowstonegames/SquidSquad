package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.tommyettinger.ds.support.FourWheelRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.tommyettinger.ds.support.util.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.concurrent.atomic.AtomicLong;

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
        System.out.println();
    }
}
