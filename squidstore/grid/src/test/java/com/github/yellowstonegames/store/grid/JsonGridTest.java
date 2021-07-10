package com.github.yellowstonegames.store.grid;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.grid.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

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

    @Test
    public void testRegion() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerRegion(json);
        Region region, region2;
        region = new Region(Coord.get(3, 3), 8, 8);
        String data = json.toJson(region);
        System.out.println(data);
        region2 = json.fromJson(Region.class, data);
        Assert.assertEquals(region, region2);

        region.not();
        data = json.toJson(region);
        System.out.println(data);
        region2 = json.fromJson(Region.class, data);
        Assert.assertEquals(region, region2);

        region.retract(2);
        data = json.toJson(region);
        System.out.println(data);
        region2 = json.fromJson(Region.class, data);
        Assert.assertEquals(region, region2);

        region.not().expand8way();
        data = json.toJson(region);
        System.out.println(data);
        region2 = json.fromJson(Region.class, data);
        Assert.assertEquals(region, region2);

        System.out.println();
    }

    @Test
    public void testCoordObjectMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordObjectMap(json);
        CoordObjectMap<String> points = new CoordObjectMap<>(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(666, 666)},
                new String[]{"foo", "bar", "baz"});
        String data = json.toJson(points);
        System.out.println(data);
        CoordObjectMap<?> points2 = json.fromJson(CoordObjectMap.class, data);
        for(Map.Entry<Coord, ?> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
    }

    @Test
    public void testCoordObjectOrderedMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordObjectOrderedMap(json);
        CoordObjectOrderedMap<String> points = new CoordObjectOrderedMap<>(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(666, 666)},
                new String[]{"foo", "bar", "baz"});
        String data = json.toJson(points);
        System.out.println(data);
        CoordObjectOrderedMap<?> points2 = json.fromJson(CoordObjectOrderedMap.class, data);
        for(Map.Entry<Coord, ?> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
    }

    @Test
    public void testCoordSet() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordSet(json);
        CoordSet points = CoordSet.with(Coord.get(42, 42), Coord.get(23, 23), Coord.get(666, 666));
        String data = json.toJson(points);
        System.out.println(data);
        CoordSet points2 = json.fromJson(CoordSet.class, data);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
    }

    @Test
    public void testCoordOrderedSet() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordOrderedSet(json);
        CoordOrderedSet points = CoordOrderedSet.with(Coord.get(42, 42), Coord.get(23, 23), Coord.get(666, 666));
        String data = json.toJson(points);
        System.out.println(data);
        CoordOrderedSet points2 = json.fromJson(CoordOrderedSet.class, data);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
    }

    @Test
    public void testRadiance() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerRadiance(json);
        Radiance radiance, radiance2;
        radiance = new Radiance(3, 0xF0F0B0FF, 1.5f, 0f, 0.1f, 0.5f);
        String data = json.toJson(radiance);
        System.out.println(data);
        radiance2 = json.fromJson(Radiance.class, data);
        Assert.assertEquals(radiance, radiance2);
        System.out.println();
    }
}
