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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.digital.Interpolations;
import com.github.yellowstonegames.grid.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class GridTest {
    @Test
    public void testCoord() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(ObjectList.class, new CollectionSerializer<ObjectList<?>>());
        ObjectList<Coord> data = ObjectList.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(-2, -3), Coord.get(100, 100));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            ObjectList data2 = kryo.readObject(input, ObjectList.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRegion() {
        Kryo kryo = new Kryo();
        kryo.register(Region.class, new RegionSerializer());
        Region data = new Region(120, 120, Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Region data2 = kryo.readObject(input, Region.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordSet() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordSet.class, new CoordSetSerializer());
        CoordSet data = CoordSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordSet data2 = kryo.readObject(input, CoordSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordOrderedSet() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordOrderedSet.class, new CoordOrderedSetSerializer());
        CoordOrderedSet data = CoordOrderedSet.with(Coord.get(0, 0), Coord.get(1, 1), Coord.get(2, 3), Coord.get(100, 100));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordOrderedSet data2 = kryo.readObject(input, CoordOrderedSet.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordObjectMap() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordObjectMap.class, new CoordObjectMapSerializer());
        CoordObjectMap<String> data = CoordObjectMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordObjectMap data2 = kryo.readObject(input, CoordObjectMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordObjectOrderedMap() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
        CoordObjectOrderedMap<String> data = CoordObjectOrderedMap.with(Coord.get(0, 0), "foo", Coord.get(1, 1), "bar", Coord.get(2, 3), "baz", Coord.get(100, 100), "quux");

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordObjectOrderedMap data2 = kryo.readObject(input, CoordObjectOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatMap() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordFloatMap.class, new CoordFloatMapSerializer());
        CoordFloatMap data = CoordFloatMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordFloatMap data2 = kryo.readObject(input, CoordFloatMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCoordFloatOrderedMap() {
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(CoordFloatOrderedMap.class, new CoordFloatOrderedMapSerializer());
        CoordFloatOrderedMap data = CoordFloatOrderedMap.with(Coord.get(0, 0), 42, Coord.get(1, 1), 123, Coord.get(2, 3), 6.66, Coord.get(100, 100), 3.14159);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CoordFloatOrderedMap data2 = kryo.readObject(input, CoordFloatOrderedMap.class);
            Assert.assertEquals(data, data2);
        }
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
        Kryo kryo = new Kryo();
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(IGI.class);
        kryo.register(SpatialMap.class, new SpatialMapSerializer());
        SpatialMap<IGI> data = new SpatialMap<>(8);
        data.add(new IGI(Coord.get(1, 2)));
        data.add(new IGI(Coord.get(2, 2)));
        data.add(new IGI(Coord.get(1, 3)));
        data.add(new IGI(Coord.get(2, 3)));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SpatialMap data2 = kryo.readObject(input, SpatialMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRadiance() {
        Kryo kryo = new Kryo();
        kryo.register(Radiance.class, new RadianceSerializer());

        Radiance data = new Radiance(5, 0xD0F055FF, 0.7f, 0.05f, 0.2f, 0.5f, -123);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Radiance data2 = kryo.readObject(input, Radiance.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManager() {
        Kryo kryo = new Kryo();
        kryo.register(int[].class);
        kryo.register(int[][].class);
        kryo.register(float[].class);
        kryo.register(float[][].class);
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(Radiance.class, new RadianceSerializer());
        kryo.register(Region.class, new RegionSerializer());
        kryo.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
        kryo.register(LightingManager.class, new LightingManagerSerializer());

        LightingManager data = new LightingManager(new float[10][10], 0x252033FF, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LightingManager data2 = kryo.readObject(input, LightingManager.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLightingManagerRgb() {
        Kryo kryo = new Kryo();
        kryo.register(int[].class);
        kryo.register(int[][].class);
        kryo.register(float[].class);
        kryo.register(float[][].class);
        kryo.register(Coord.class, new CoordSerializer());
        kryo.register(Radiance.class, new RadianceSerializer());
        kryo.register(Region.class, new RegionSerializer());
        kryo.register(CoordObjectOrderedMap.class, new CoordObjectOrderedMapSerializer());
        kryo.register(LightingManagerRgb.class, new LightingManagerRgbSerializer());

        LightingManagerRgb data = new LightingManagerRgb(new float[10][10], 0xFF858040, Radius.CIRCLE, 4f);
        data.addLight(5, 4, new Radiance(2f, 0x99DDFFFF, 0.2f, 0f, 0f, 0f));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LightingManagerRgb data2 = kryo.readObject(input, LightingManagerRgb.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoise() {
        Kryo kryo = new Kryo();
        kryo.register(Noise.class, new NoiseSerializer());

        Noise data = new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f);
        data.setFractalSpiral(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Noise data2 = kryo.readObject(input, Noise.class);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f), data2.getConfiguredNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f), data2.getConfiguredNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getConfiguredNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFoamNoise() {
        Kryo kryo = new Kryo();
        kryo.register(FoamNoise.class, new FoamNoiseSerializer());

        FoamNoise data = new FoamNoise(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            FoamNoise data2 = kryo.readObject(input, FoamNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPhantomNoise() {
        Kryo kryo = new Kryo();
        kryo.register(PhantomNoise.class, new PhantomNoiseSerializer());

        PhantomNoise data = new PhantomNoise(1234, 8, 7f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            PhantomNoise data2 = kryo.readObject(input, PhantomNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTaffyNoise() {
        Kryo kryo = new Kryo();
        kryo.register(TaffyNoise.class, new TaffyNoiseSerializer());

        TaffyNoise data = new TaffyNoise(1234, 8, 7f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            TaffyNoise data2 = kryo.readObject(input, TaffyNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testFlanNoise() {
        Kryo kryo = new Kryo();
        kryo.register(FlanNoise.class, new FlanNoiseSerializer());

        FlanNoise data = new FlanNoise(1234, 8, 7f, 2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            FlanNoise data2 = kryo.readObject(input, FlanNoise.class);
            Assert.assertEquals(data.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), data2.getNoise(new float[]{0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f}), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testCyclicNoise() {
        Kryo kryo = new Kryo();
        kryo.register(CyclicNoise.class, new CyclicNoiseSerializer());

        CyclicNoise data = new CyclicNoise(-9876543210L, 8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CyclicNoise data2 = kryo.readObject(input, CyclicNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSorbetNoise() {
        Kryo kryo = new Kryo();
        kryo.register(SorbetNoise.class, new SorbetNoiseSerializer());

        SorbetNoise data = new SorbetNoise(-9876543210L, 8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SorbetNoise data2 = kryo.readObject(input, SorbetNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoise() {
        Kryo kryo = new Kryo();
        kryo.register(SimplexNoise.class, new SimplexNoiseSerializer());

        SimplexNoise data = new SimplexNoise(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SimplexNoise data2 = kryo.readObject(input, SimplexNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoiseScaled() {
        Kryo kryo = new Kryo();
        kryo.register(SimplexNoiseScaled.class, new SimplexNoiseScaledSerializer());

        SimplexNoiseScaled data = new SimplexNoiseScaled(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SimplexNoiseScaled data2 = kryo.readObject(input, SimplexNoiseScaled.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSimplexNoiseHard() {
        Kryo kryo = new Kryo();
        kryo.register(SimplexNoiseHard.class, new SimplexNoiseHardSerializer());

        SimplexNoiseHard data = new SimplexNoiseHard(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SimplexNoiseHard data2 = kryo.readObject(input, SimplexNoiseHard.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testOpenSimplex2() {
        Kryo kryo = new Kryo();
        kryo.register(OpenSimplex2.class, new OpenSimplex2Serializer());

        OpenSimplex2 data = new OpenSimplex2(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            OpenSimplex2 data2 = kryo.readObject(input, OpenSimplex2.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testOpenSimplex2Smooth() {
        Kryo kryo = new Kryo();
        kryo.register(OpenSimplex2Smooth.class, new OpenSimplex2SmoothSerializer());

        OpenSimplex2Smooth data = new OpenSimplex2Smooth(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            OpenSimplex2Smooth data2 = kryo.readObject(input, OpenSimplex2Smooth.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testPerlinNoise() {
        Kryo kryo = new Kryo();
        kryo.register(PerlinNoise.class, new PerlinNoiseSerializer());

        PerlinNoise data = new PerlinNoise(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            PerlinNoise data2 = kryo.readObject(input, PerlinNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testValueNoise() {
        Kryo kryo = new Kryo();
        kryo.register(ValueNoise.class, new ValueNoiseSerializer());

        ValueNoise data = new ValueNoise(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            ValueNoise data2 = kryo.readObject(input, ValueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testBasicHashNoise() {
        Kryo kryo = new Kryo();
        kryo.register(com.github.yellowstonegames.grid.FlawedPointHash.FlowerHash.class);
        kryo.register(BasicHashNoise.class, new BasicHashNoiseSerializer());

        BasicHashNoise data = new BasicHashNoise(-987654321, new FlawedPointHash.FlowerHash(123456789));

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            BasicHashNoise data2 = kryo.readObject(input, BasicHashNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f), data2.getNoise(0.1f, 0.2f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f), data2.getNoise(0.1f, 0.2f, 0.3f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHighDimensionalValueNoise() {
        Kryo kryo = new Kryo();
        kryo.register(HighDimensionalValueNoise.class, new HighDimensionalValueNoiseSerializer());

        HighDimensionalValueNoise data = new HighDimensionalValueNoise(1234, 8);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            HighDimensionalValueNoise data2 = kryo.readObject(input, HighDimensionalValueNoise.class);
            Assert.assertEquals(data.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), data2.getNoise(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testNoiseWrapper() {
        Kryo kryo = new Kryo();
        kryo.register(Noise.class, new NoiseSerializer());
        kryo.register(NoiseWrapper.class, new NoiseWrapperSerializer());

        NoiseWrapper data = new NoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            NoiseWrapper data2 = kryo.readObject(input, NoiseWrapper.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testRadialNoiseWrapper() {
        Kryo kryo = new Kryo();
        kryo.register(Noise.class, new NoiseSerializer());
        kryo.register(RadialNoiseWrapper.class, new RadialNoiseWrapperSerializer());

        RadialNoiseWrapper data = new RadialNoiseWrapper(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), 123451234512345L, 0.2f, Noise.BILLOW, 3, true, 10f, 20.125f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            RadialNoiseWrapper data2 = kryo.readObject(input, RadialNoiseWrapper.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testNoiseAdjustment() {
        Kryo kryo = new Kryo();
        kryo.register(Noise.class, new NoiseSerializer());
        kryo.register(NoiseAdjustment.class, new NoiseAdjustmentSerializer());

        NoiseAdjustment data = new NoiseAdjustment(new Noise(-2345, 0.1f, Noise.VALUE_FRACTAL, 3, 2.5f, 0.4f), Interpolations.exp5In);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            NoiseAdjustment data2 = kryo.readObject(input, NoiseAdjustment.class);
            Assert.assertEquals(data.getNoise(1f, 1.5f), data2.getNoise(1f, 1.5f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f), data2.getNoise(1f, 1.5f, 2.25f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f), Float.MIN_NORMAL);
            Assert.assertEquals(data.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), data2.getNoise(1f, 1.5f, 2.25f, 3.125f, 4.0625f, 5.03125f), Float.MIN_NORMAL);
            Assert.assertEquals(data, data2);
        }
    }
}
