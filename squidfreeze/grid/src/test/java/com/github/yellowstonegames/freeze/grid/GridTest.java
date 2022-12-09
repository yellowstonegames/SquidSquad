/*
 * Copyright (c) 2022 See AUTHORS file.
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
import com.github.yellowstonegames.grid.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class GridTest {
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
            Assert.assertEquals(data.serializeToString(), data2.serializeToString());
            Assert.assertEquals(data, data2);
        }
    }
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

}
