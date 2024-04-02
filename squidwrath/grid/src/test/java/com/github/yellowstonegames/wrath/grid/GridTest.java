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

package com.github.yellowstonegames.wrath.grid;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.tantrum.jdkgdxds.ObjectListSerializer;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectMap;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.Assert;
import org.junit.Test;

public class GridTest {
    @Test
    public void testCoord() {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Coord.class, new CoordSerializer(fury));
        fury.registerSerializer(ObjectList.class, new ObjectListSerializer(fury));
        ObjectList<Coord> data = ObjectList.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(-2, -3), Coord.get(100, 100));

        byte[] bytes = fury.serializeJavaObject(data);
        ObjectList data2 = fury.deserializeJavaObject(bytes, ObjectList.class);
        Assert.assertEquals(data, data2);

    }

//    @Test
//    public void testRegion() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Region.class, new RegionSerializer());
//        Region data = new Region(120, 120, Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            Region data2 = fury.readObject(input, Region.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCoordSet() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(CoordSet.class, new CoordSetSerializer());
//        CoordSet data = CoordSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CoordSet data2 = fury.readObject(input, CoordSet.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCoordOrderedSet() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(CoordOrderedSet.class, new CoordOrderedSetSerializer());
//        CoordOrderedSet data = CoordOrderedSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CoordOrderedSet data2 = fury.readObject(input, CoordOrderedSet.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCoordObjectOrderedMap() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
//        CoordObjectOrderedMap<String> data = CoordObjectOrderedMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CoordObjectOrderedMap data2 = fury.readObject(input, CoordObjectOrderedMap.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCoordFloatMap() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(CoordFloatMap.class, new CoordFloatMapSerializer());
//        CoordFloatMap data = CoordFloatMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CoordFloatMap data2 = fury.readObject(input, CoordFloatMap.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCoordFloatOrderedMap() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer());
//        CoordFloatOrderedMap data = CoordFloatOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CoordFloatOrderedMap data2 = fury.readObject(input, CoordFloatOrderedMap.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    public static class IGI implements IGridIdentified {
//        public final int id;
//        public Coord position;
//        private static int COUNTER = 0;
//
//        public IGI(){
//            id = COUNTER++;
//            position = Coord.get(0, 0);
//        }
//        public IGI(Coord pos){
//            id = COUNTER++;
//            position = pos;
//        }
//        public IGI(int id, Coord pos){
//            this.id = id;
//            position = pos;
//        }
//
//        @Override
//        public int getIdentifier() {
//            return id;
//        }
//
//        @Override
//        public Coord getCoordPosition() {
//            return position;
//        }
//
//        @Override
//        public void setCoordPosition(Coord position) {
//            this.position = position;
//        }
//
//        @Override
//        public String toString() {
//            return "IGI{" +
//                    "id=" + id +
//                    ", position=" + position +
//                    '}';
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            IGI igi = (IGI) o;
//
//            if (id != igi.id) return false;
//            return position != null ? position.equals(igi.position) : igi.position == null;
//        }
//
//        @Override
//        public int hashCode() {
//            return id;
//        }
//    }
//    @Test
//    public void testSpatialMap() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(IGI.class);
//        fury.register(SpatialMap.class, new SpatialMapSerializer());
//        SpatialMap<IGI> data = new SpatialMap<>(8);
//        data.add(new IGI(Coord.get(1, 2)));
//        data.add(new IGI(Coord.get(2, 2)));
//        data.add(new IGI(Coord.get(1, 3)));
//        data.add(new IGI(Coord.get(2, 3)));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SpatialMap data2 = fury.readObject(input, SpatialMap.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testRadiance() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Radiance.class, new RadianceSerializer());
//
//        Radiance data = new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            Radiance data2 = fury.readObject(input, Radiance.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testLightingManager() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(int[].class);
//        fury.register(int[][].class);
//        fury.register(float[].class);
//        fury.register(float[][].class);
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(Radiance.class, new RadianceSerializer());
//        fury.register(Region.class, new RegionSerializer());
//        fury.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
//        fury.register(LightingManager.class, new LightingManagerSerializer());
//
//        LightingManager data = new LightingManager(new float[10][10], 0x252033FF, Radius.CIRCLE, 4f);
//        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            LightingManager data2 = fury.readObject(input, LightingManager.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testLightingManagerRgb() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(int[].class);
//        fury.register(int[][].class);
//        fury.register(float[].class);
//        fury.register(float[][].class);
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(Radiance.class, new RadianceSerializer());
//        fury.register(Region.class, new RegionSerializer());
//        fury.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
//        fury.register(LightingManagerRgb.class, new LightingManagerRgbSerializer());
//
//        LightingManagerRgb data = new LightingManagerRgb(new float[10][10], 0xFF858040, Radius.CIRCLE, 4f);
//        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            LightingManagerRgb data2 = fury.readObject(input, LightingManagerRgb.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testVisionFramework() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(int[].class);
//        fury.register(int[][].class);
//        fury.register(float[].class);
//        fury.register(float[][].class);
//        fury.register(char[].class);
//        fury.register(char[][].class);
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(Radiance.class, new RadianceSerializer());
//        fury.register(Region.class, new RegionSerializer());
//        fury.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
//        fury.register(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer());
//        fury.register(LightingManager.class, new LightingManagerSerializer());
//        fury.register(VisionFramework.class, new VisionFrameworkSerializer());
//
//        VisionFramework data = new VisionFramework();
//        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColor.describeOklab("darker gray 9 yellow"));
//        data.lighting.addLight(3, 3, new Radiance(3f, 0xFF9966AA, 0.2f, 0f, 0f, 0f));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            VisionFramework data2 = fury.readObject(input, VisionFramework.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//
//    @Test
//    public void testVisionFrameworkRgb() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(int[].class);
//        fury.register(int[][].class);
//        fury.register(float[].class);
//        fury.register(float[][].class);
//        fury.register(char[].class);
//        fury.register(char[][].class);
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(Radiance.class, new RadianceSerializer());
//        fury.register(Region.class, new RegionSerializer());
//        fury.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
//        fury.register(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer());
//        fury.register(LightingManagerRgb.class, new LightingManagerRgbSerializer());
//        fury.register(VisionFrameworkRgb.class, new VisionFrameworkRgbSerializer());
//
//        VisionFrameworkRgb data = new VisionFrameworkRgb();
//        data.restart(ArrayTools.fill('.', 10, 10), Coord.get(3, 3), 2f, DescriptiveColorRgb.describe("darker gray 9 yellow"));
//        data.lighting.addLight(3, 3, new Radiance(3f, 0x9966AAFF, 0.2f, 0f, 0f, 0f));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            VisionFrameworkRgb data2 = fury.readObject(input, VisionFrameworkRgb.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Noise.class, new NoiseSerializer());
//
//        Noise data = new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f);
//        data.setFractalSpiral(true);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            Noise data2 = fury.readObject(input, Noise.class);
//            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f), data2.getConfiguredNoise(1f, 1.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f), data2.getConfiguredNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testFoamNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(FoamNoise.class, new FoamNoiseSerializer());
//
//        FoamNoise data = new FoamNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            FoamNoise data2 = fury.readObject(input, FoamNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testFoamplexNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(FoamplexNoise.class, new FoamplexNoiseSerializer());
//
//        FoamplexNoise data = new FoamplexNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            FoamplexNoise data2 = fury.readObject(input, FoamplexNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testPhantomNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(PhantomNoise.class, new PhantomNoiseSerializer());
//
//        PhantomNoise data = new PhantomNoise(1234, 8, 7f);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            PhantomNoise data2 = fury.readObject(input, PhantomNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testTaffyNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(TaffyNoise.class, new TaffyNoiseSerializer());
//
//        TaffyNoise data = new TaffyNoise(1234, 8, 7f);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            TaffyNoise data2 = fury.readObject(input, TaffyNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testFlanNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(FlanNoise.class, new FlanNoiseSerializer());
//
//        FlanNoise data = new FlanNoise(1234, 8, 7f, 2);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            FlanNoise data2 = fury.readObject(input, FlanNoise.class);
//            Assert.assertEquals(data.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), data2.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testCyclicNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(CyclicNoise.class, new CyclicNoiseSerializer());
//
//        CyclicNoise data = new CyclicNoise(-9876543210L, 8);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CyclicNoise data2 = fury.readObject(input, CyclicNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testSorbetNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(SorbetNoise.class, new SorbetNoiseSerializer());
//
//        SorbetNoise data = new SorbetNoise(-9876543210L, 8);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SorbetNoise data2 = fury.readObject(input, SorbetNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testSimplexNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(SimplexNoise.class, new SimplexNoiseSerializer());
//
//        SimplexNoise data = new SimplexNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SimplexNoise data2 = fury.readObject(input, SimplexNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testSimplexNoiseScaled() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(SimplexNoiseScaled.class, new SimplexNoiseScaledSerializer());
//
//        SimplexNoiseScaled data = new SimplexNoiseScaled(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SimplexNoiseScaled data2 = fury.readObject(input, SimplexNoiseScaled.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testSimplexNoiseHard() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(SimplexNoiseHard.class, new SimplexNoiseHardSerializer());
//
//        SimplexNoiseHard data = new SimplexNoiseHard(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SimplexNoiseHard data2 = fury.readObject(input, SimplexNoiseHard.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testOpenSimplex2() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(OpenSimplex2.class, new OpenSimplex2Serializer());
//
//        OpenSimplex2 data = new OpenSimplex2(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            OpenSimplex2 data2 = fury.readObject(input, OpenSimplex2.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testOpenSimplex2Smooth() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(OpenSimplex2Smooth.class, new OpenSimplex2SmoothSerializer());
//
//        OpenSimplex2Smooth data = new OpenSimplex2Smooth(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            OpenSimplex2Smooth data2 = fury.readObject(input, OpenSimplex2Smooth.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testPerlinNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(PerlinNoise.class, new PerlinNoiseSerializer());
//
//        PerlinNoise data = new PerlinNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            PerlinNoise data2 = fury.readObject(input, PerlinNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testValueNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(ValueNoise.class, new ValueNoiseSerializer());
//
//        ValueNoise data = new ValueNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            ValueNoise data2 = fury.readObject(input, ValueNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testBasicHashNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(com.github.yellowstonegames.grid.FlawedPointHash.FlowerHash.class);
//        fury.register(BasicHashNoise.class, new BasicHashNoiseSerializer());
//
//        BasicHashNoise data = new BasicHashNoise(-987654321, new FlawedPointHash.FlowerHash(123456789));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            BasicHashNoise data2 = fury.readObject(input, BasicHashNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testHighDimensionalValueNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(HighDimensionalValueNoise.class, new HighDimensionalValueNoiseSerializer());
//
//        HighDimensionalValueNoise data = new HighDimensionalValueNoise(1234, 8);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            HighDimensionalValueNoise data2 = fury.readObject(input, HighDimensionalValueNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testWhiteNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(WhiteNoise.class, new WhiteNoiseSerializer());
//
//        WhiteNoise data = new WhiteNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            WhiteNoise data2 = fury.readObject(input, WhiteNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testBadgerNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(BadgerNoise.class, new BadgerNoiseSerializer());
//
//        BadgerNoise data = new BadgerNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            BadgerNoise data2 = fury.readObject(input, BadgerNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testSnakeNoise() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(SnakeNoise.class, new SnakeNoiseSerializer());
//
//        SnakeNoise data = new SnakeNoise(-9876543210L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            SnakeNoise data2 = fury.readObject(input, SnakeNoise.class);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testNoiseWrapper() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Noise.class, new NoiseSerializer());
//        fury.register(NoiseWrapper.class, new NoiseWrapperSerializer());
//
//        NoiseWrapper data = new NoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            NoiseWrapper data2 = fury.readObject(input, NoiseWrapper.class);
//            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testRadialNoiseWrapper() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Noise.class, new NoiseSerializer());
//        fury.register(RadialNoiseWrapper.class, new RadialNoiseWrapperSerializer());
//
//        RadialNoiseWrapper data = new RadialNoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true, 10f, 20.125f);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            RadialNoiseWrapper data2 = fury.readObject(input, RadialNoiseWrapper.class);
//            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testNoiseAdjustment() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Noise.class, new NoiseSerializer());
//        fury.register(NoiseAdjustment.class, new NoiseAdjustmentSerializer());
//
//        NoiseAdjustment data = new NoiseAdjustment(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), Interpolations.exp5In);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            NoiseAdjustment data2 = fury.readObject(input, NoiseAdjustment.class);
//            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
//            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testPointPair() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(Coord.class, new CoordSerializer());
//        fury.register(PointPair.class, new PointPairSerializer());
//        PointPair<Coord> data = new PointPair<>(Coord.get(0, 0), Coord.get(1, 1));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        fury.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            PointPair<?> data2 = fury.readObject(input, PointPair.class);
//            Assert.assertEquals(data, data2);
//        }
//    }
}
