package com.github.yellowstonegames.store.grid;

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
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.store.core.JsonCore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JsonGridTest {

    @Test
    public void testCoord() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoord(json);
        Coord coord, coord2;
        coord = Coord.get(0, 0);
        String data = json.toJson(coord);
        System.out.println(data);
        coord2 = json.fromJson(Coord.class, data);
        Assert.assertEquals(coord, coord2);

        coord = Coord.get(0, 100);
        data = json.toJson(coord);
        System.out.println(data);
        coord2 = json.fromJson(Coord.class, data);
        Assert.assertEquals(coord, coord2);

        coord = Coord.get(1000, 10);
        data = json.toJson(coord);
        System.out.println(data);
        coord2 = json.fromJson(Coord.class, data);
        Assert.assertEquals(coord, coord2);

        coord = Coord.get(-2, -2);
        data = json.toJson(coord);
        System.out.println(data);
        coord2 = json.fromJson(Coord.class, data);
        Assert.assertEquals(coord, coord2);
        
        coord = Coord.get(-20, -20);
        data = json.toJson(coord);
        System.out.println(data);
        coord2 = json.fromJson(Coord.class, data);
        Assert.assertEquals(coord, coord2);

        System.out.println();
    }

}
