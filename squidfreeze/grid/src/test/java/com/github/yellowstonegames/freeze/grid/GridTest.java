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
}
