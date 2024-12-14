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

package com.github.yellowstonegames.store.grid;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.ds.ObjectLongMap;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
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
        CoordObjectMap<String> points = CoordObjectMap.with(
                Coord.get(42, 42), "foo", Coord.get(23, 23), "bar", Coord.get(66, 66), "baz");
//        CoordObjectMap<String> points = new CoordObjectMap<>(
//                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
//                new String[]{"foo", "bar", "baz"});
        String data = json.toJson(points);
        System.out.println(data);
        CoordObjectMap<?> points2 = json.fromJson(CoordObjectMap.class, data);
        Assert.assertEquals(points, points2);
        for(Map.Entry<Coord, ?> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordObjectOrderedMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordObjectOrderedMap(json);
        {
            CoordObjectOrderedMap<String> points = CoordObjectOrderedMap.with(
                    Coord.get(42, 42), "foo", Coord.get(23, 23), "bar", Coord.get(66, 66), "baz");
            String data = json.toJson(points);
            System.out.println(data);
            CoordObjectOrderedMap<?> points2 = json.fromJson(CoordObjectOrderedMap.class, data);
            Assert.assertEquals(points, points2);
            for (Map.Entry<Coord, ?> pair : points2) {
                System.out.print(pair.getKey());
                System.out.print("=");
                System.out.print(pair.getValue());
                System.out.print("; ");
            }
        }
        {
            JsonGrid.registerRadiance(json);
            CoordObjectOrderedMap<Radiance> lights = new CoordObjectOrderedMap<>(
                    new Coord[]{Coord.get(42, 42)},
                    new Radiance[]{new Radiance(3, 0xF0F0B0FF, 1.5f, 0f, 0.1f, 0.5f)});
            String data = json.toJson(lights, CoordObjectOrderedMap.class, Radiance.class);
            System.out.println(data);
            CoordObjectOrderedMap<?> lights2 = json.fromJson(CoordObjectOrderedMap.class, data);
            Assert.assertEquals(lights, lights2);
            for (Map.Entry<Coord, ?> pair : lights2) {
                System.out.print(pair.getKey());
                System.out.print("=");
                System.out.print(pair.getValue());
                System.out.print("; ");
            }
        }
        System.out.println();
    }

    @Test
    public void testCoordFloatMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordFloatMap(json);
        CoordFloatMap points = new CoordFloatMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new float[]{42.42f, 23.23f, 66.66f});
        String data = json.toJson(points);
        System.out.println(data);
        CoordFloatMap points2 = json.fromJson(CoordFloatMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectFloatMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordFloatOrderedMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordFloatOrderedMap(json);
        CoordFloatOrderedMap points = new CoordFloatOrderedMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new float[]{42.42f, 23.23f, 66.66f});
        String data = json.toJson(points);
        System.out.println(data);
        CoordFloatOrderedMap points2 = json.fromJson(CoordFloatOrderedMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectFloatMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }


    @Test
    public void testCoordLongMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordLongMap(json);
        CoordLongMap points = new CoordLongMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new long[]{4242, 2323, 6666});
        String data = json.toJson(points);
        System.out.println(data);
        CoordLongMap points2 = json.fromJson(CoordLongMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectLongMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordLongOrderedMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordLongOrderedMap(json);
        CoordLongOrderedMap points = new CoordLongOrderedMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new long[]{4242, 2323, 6666});
        String data = json.toJson(points);
        System.out.println(data);
        CoordLongOrderedMap points2 = json.fromJson(CoordLongOrderedMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectLongMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordIntMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordIntMap(json);
        CoordIntMap points = new CoordIntMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new int[]{4242, 2323, 6666});
        String data = json.toJson(points);
        System.out.println(data);
        CoordIntMap points2 = json.fromJson(CoordIntMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectIntMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordIntOrderedMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordIntOrderedMap(json);
        CoordIntOrderedMap points = new CoordIntOrderedMap(
                new Coord[]{Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66)},
                new int[]{4242, 2323, 6666});
        String data = json.toJson(points);
        System.out.println(data);
        CoordIntOrderedMap points2 = json.fromJson(CoordIntOrderedMap.class, data);
        Assert.assertEquals(points, points2);
        for(ObjectIntMap.Entry<Coord> pair : points2) {
            System.out.print(pair.getKey());
            System.out.print("=");
            System.out.print(pair.getValue());
            System.out.print("; ");
        }
        System.out.println();
    }

    @Test
    public void testCoordSet() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordSet(json);
        CoordSet points = CoordSet.with(Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66));
        String data = json.toJson(points);
        System.out.println(data);
        CoordSet points2 = json.fromJson(CoordSet.class, data);
        Assert.assertEquals(points, points2);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
        System.out.println();
    }

    @Test
    public void testCoordOrderedSet() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoordOrderedSet(json);
        CoordOrderedSet points = CoordOrderedSet.with(Coord.get(42, 42), Coord.get(23, 23), Coord.get(66, 66));
        String data = json.toJson(points);
        System.out.println(data);
        CoordOrderedSet points2 = json.fromJson(CoordOrderedSet.class, data);
        Assert.assertEquals(points, points2);
        for(Object point : points2) {
            System.out.print(point);
            System.out.print(", ");
        }
        System.out.println();
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

    @Test
    public void testNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerNoise(json);
        Noise noise, noise2;
        noise = new Noise(123321, 0.4f, Noise.FOAM_FRACTAL, 2, 0.625f, 1.6f);
        noise.setFoamSharpness(0.8f);
        noise.setFractalType(Noise.RIDGED_MULTI);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(Noise.class, data);
        Assert.assertEquals(noise, noise2);
        System.out.println();
    }

    @Test
    public void testNoiseWrapper() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerNoiseWrapper(json);
        NoiseWrapper noise, noise2;
        noise = new NoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(NoiseWrapper.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f, -1.12f), noise2.getNoise(-123f, 0.4f, 0.625f, -1.12f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testNoiseAdjustment() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerNoiseAdjustment(json);
        NoiseAdjustment noise, noise2;
        noise = new NoiseAdjustment(new SimplexNoise(123), Interpolations.smooth2);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(NoiseAdjustment.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f, -1.12f), noise2.getNoise(-123f, 0.4f, 0.625f, -1.12f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testFoamNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerFoamNoise(json);
        FoamNoise noise, noise2;
        noise = new FoamNoise(-9876543210L);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(FoamNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testFoamplexNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerFoamplexNoise(json);
        FoamplexNoise noise, noise2;
        noise = new FoamplexNoise(-9876543210L);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(FoamplexNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testPhantomNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerPhantomNoise(json);
        PhantomNoise noise, noise2;
        noise = new PhantomNoise(-123L, 4, 0.625f);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(PhantomNoise.class, data);
        Assert.assertEquals(noise, noise2);
        float[] args = {-123f, 0.4f, 0.625f, -1.12f};
        Assert.assertEquals(noise.getNoise(args), noise2.getNoise(args), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testTaffyNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerTaffyNoise(json);
        TaffyNoise noise, noise2;
        noise = new TaffyNoise(-123L, 4, 0.625f);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(TaffyNoise.class, data);
        Assert.assertEquals(noise, noise2);
        float[] args = {-123f, 0.4f, 0.625f, -1.12f};
        Assert.assertEquals(noise.getNoise(args), noise2.getNoise(args), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testFlanNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerFlanNoise(json);
        FlanNoise noise, noise2;
        noise = new FlanNoise(-123L, 4, 0.625f);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(FlanNoise.class, data);
        Assert.assertEquals(noise, noise2);
        float[] args = {-123f, 0.4f, 0.625f, -1.12f};
        Assert.assertEquals(noise.getNoise(args), noise2.getNoise(args), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testCyclicNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCyclicNoise(json);
        CyclicNoise noise, noise2;
        noise = new CyclicNoise(-9876543210L, 4, 0.9f);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(CyclicNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testSimplexNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerSimplexNoise(json);
        SimplexNoise noise, noise2;
        noise = new SimplexNoise(-9876543210L);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(SimplexNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testSimplexNoiseScaled() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerSimplexNoiseScaled(json);
        SimplexNoiseScaled noise, noise2;
        noise = new SimplexNoiseScaled(-9876543210L);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(SimplexNoiseScaled.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testValueNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerValueNoise(json);
        ValueNoise noise, noise2;
        noise = new ValueNoise(-9876543210L);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(ValueNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

//    @Test
//    public void testBasicHashNoise() {
//        Json json = new Json(JsonWriter.OutputType.minimal);
//        JsonGrid.registerBasicHashNoise(json);
//        BasicHashNoise noise, noise2;
//        noise = new BasicHashNoise(-987654321, new FlawedPointHash.FlowerHash(123456789));
//        String data = json.toJson(noise);
//        System.out.println(data);
//        noise2 = json.fromJson(BasicHashNoise.class, data);
//        Assert.assertEquals(noise, noise2);
//        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
//        System.out.println();
//    }

    @Test
    public void testHighDimensionalValueNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerHighDimensionalValueNoise(json);
        HighDimensionalValueNoise noise, noise2;
        noise = new HighDimensionalValueNoise(-9876543210L, 4);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(HighDimensionalValueNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testSorbetNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerSorbetNoise(json);
        SorbetNoise noise, noise2;
        noise = new SorbetNoise(-9876543210L, 4, 0.9f);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(SorbetNoise.class, data);
        System.out.println(json.toJson(noise2));
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testWhiteNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerWhiteNoise(json);
        WhiteNoise noise, noise2;
        noise = new WhiteNoise(-987654321);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(WhiteNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }


    @Test
    public void testPerlueNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerPerlueNoise(json);
        PerlueNoise noise, noise2;
        noise = new PerlueNoise(-987654321);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(PerlueNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testBadgerNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerBadgerNoise(json);
        BadgerNoise noise, noise2;
        noise = new BadgerNoise(-987654321);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(BadgerNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    @Test
    public void testSnakeNoise() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerSnakeNoise(json);
        SnakeNoise noise, noise2;
        noise = new SnakeNoise(-987654321);
        String data = json.toJson(noise);
        System.out.println(data);
        noise2 = json.fromJson(SnakeNoise.class, data);
        Assert.assertEquals(noise, noise2);
        Assert.assertEquals(noise.getNoise(-123f, 0.4f, 0.625f), noise2.getNoise(-123f, 0.4f, 0.625f), Double.MIN_NORMAL);
        System.out.println();
    }

    public static class IGI implements IGridIdentified {
        public final int id;
        public Coord position;
        private static int COUNTER = 0;

        public IGI(){
            id = COUNTER++;
            position = Coord.get(0, 0);
        }
        public IGI(Coord pos){
            id = COUNTER++;
            position = pos;
        }
        public IGI(int id, Coord pos){
            this.id = id;
            position = pos;
        }

        @Override
        public int getIdentifier() {
            return id;
        }

        @Override
        public Coord getCoordPosition() {
            return position;
        }

        @Override
        public void setCoordPosition(Coord position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "IGI{" +
                    "id=" + id +
                    ", position=" + position +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IGI igi = (IGI) o;

            if (id != igi.id) return false;
            return position != null ? position.equals(igi.position) : igi.position == null;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }
    @Test
    public void testSpatialMap() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerSpatialMap(json);
        SpatialMap<IGI> points = new SpatialMap<>(8);
        points.add(new IGI(Coord.get(1, 2)));
        points.add(new IGI(Coord.get(2, 2)));
        points.add(new IGI(Coord.get(1, 3)));
        points.add(new IGI(Coord.get(2, 3)));
        String data = json.toJson(points);
        System.out.println(data);
        SpatialMap<IGI> points2 = json.fromJson(SpatialMap.class, IGI.class, data);
        Assert.assertEquals(points, points2);
    }

    @Test
    public void testLightingManager() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerLightingManager(json);
        LightingManager lm = new LightingManager(new float[10][10], 0x252033FF, Radius.CIRCLE, 4f);
        lm.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));
        String data = json.toJson(lm);
        System.out.println(data);
        LightingManager lm2 = json.fromJson(LightingManager.class, data);
        System.out.println();
        System.out.println(lm.lights);
        System.out.println(lm2.lights);
        Assert.assertEquals(lm, lm2);
    }

    @Test
    public void testLightingManagerRgb() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerLightingManagerRgb(json);
        LightingManagerRgb lm = new LightingManagerRgb(new float[10][10], 0xFF858040, Radius.CIRCLE, 4f);
        lm.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));
        String data = json.toJson(lm);
        System.out.println(data);
        LightingManagerRgb lm2 = json.fromJson(LightingManagerRgb.class, data);
        System.out.println();
        System.out.println(lm.lights);
        System.out.println(lm2.lights);
        Assert.assertEquals(lm, lm2);
    }

    @Test
    public void testVisionFramework() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerVisionFramework(json);
        VisionFramework vf = new VisionFramework();
        vf.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColor.describeOklab("darker gray 9 yellow"));
        vf.lighting.addLight(3, 3, new Radiance(3f, 0xFF9966AA, 0.2f, 0f, 0f, 0f));
        String data = json.toJson(vf);
        System.out.println(data);
        VisionFramework vf2 = json.fromJson(VisionFramework.class, data);
        System.out.println();
        System.out.println(vf);
        System.out.println(vf2);
        Assert.assertEquals(vf, vf2);
    }
    
    @Test
    public void testVisionFrameworkRgb() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerVisionFrameworkRgb(json);
        VisionFrameworkRgb vf = new VisionFrameworkRgb();
        vf.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColorRgb.describe("darker gray 9 yellow"));
        vf.lighting.addLight(3, 3, new Radiance(3f, 0x9966AAFF, 0.2f, 0f, 0f, 0f));
        String data = json.toJson(vf);
        System.out.println(data);
        VisionFrameworkRgb vf2 = json.fromJson(VisionFrameworkRgb.class, data);
        System.out.println();
        System.out.println(vf);
        System.out.println(vf2);
        Assert.assertEquals(vf, vf2);
    }

    @Test
    public void testPointPair() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonGrid.registerCoord(json);
        JsonGrid.registerPointPair(json);
        Coord coordA, coordB;
        PointPair<Coord> pair, pair2;
        coordA = Coord.get(0, 0);
        coordB = Coord.get(1, 2);
        pair = new PointPair<>(coordA, coordB);
        String data = json.toJson(pair);
        System.out.println(data);
        pair2 = json.fromJson(PointPair.class, data);
        Assert.assertEquals(pair, pair2);

        coordA = Coord.get(0, 100);
        coordB = Coord.get(10, 40);
        pair.a = coordA;
        pair.b = coordB;
        data = json.toJson(pair);
        System.out.println(data);
        pair2 = json.fromJson(PointPair.class, data);
        Assert.assertEquals(pair, pair2);

        System.out.println();
    }

}
