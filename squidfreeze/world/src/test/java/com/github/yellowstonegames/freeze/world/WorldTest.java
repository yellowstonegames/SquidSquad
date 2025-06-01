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

package com.github.yellowstonegames.freeze.world;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.world.*;
import org.junit.Assert;
import org.junit.Test;

public class WorldTest {
    @Test
    public void testDiagonalWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(DiagonalWorldMap.class, new DiagonalWorldMapSerializer());

        DiagonalWorldMap data = new DiagonalWorldMap(123, 200);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            DiagonalWorldMap data2 = kryo.readObject(input, DiagonalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testEllipticalWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(EllipticalWorldMap.class, new EllipticalWorldMapSerializer());

        EllipticalWorldMap data = new EllipticalWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            EllipticalWorldMap data2 = kryo.readObject(input, EllipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testGlobeMap() {
        Kryo kryo = new Kryo();
        kryo.register(GlobeMap.class, new GlobeMapSerializer());

        GlobeMap data = new GlobeMap(123, 200, 200);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            GlobeMap data2 = kryo.readObject(input, GlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHexagonalWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(HexagonalWorldMap.class, new HexagonalWorldMapSerializer());

        HexagonalWorldMap data = new HexagonalWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            HexagonalWorldMap data2 = kryo.readObject(input, HexagonalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testHyperellipticalWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(HyperellipticalWorldMap.class, new HyperellipticalWorldMapSerializer());

        HyperellipticalWorldMap data = new HyperellipticalWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            HyperellipticalWorldMap data2 = kryo.readObject(input, HyperellipticalWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLatLonWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(LatLonWorldMap.class, new LatLonWorldMapSerializer());

        LatLonWorldMap data = new LatLonWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LatLonWorldMap data2 = kryo.readObject(input, LatLonWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLocalMap() {
        Kryo kryo = new Kryo();
        kryo.register(LocalMap.class, new LocalMapSerializer());

        LocalMap data = new LocalMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LocalMap data2 = kryo.readObject(input, LocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicLocalMap() {
        Kryo kryo = new Kryo();
        kryo.register(MimicLocalMap.class, new MimicLocalMapSerializer());

        MimicLocalMap data = new MimicLocalMap(123);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            MimicLocalMap data2 = kryo.readObject(input, MimicLocalMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testMimicWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(MimicWorldMap.class, new MimicWorldMapSerializer());

        MimicWorldMap data = new MimicWorldMap(123);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            MimicWorldMap data2 = kryo.readObject(input, MimicWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRotatingGlobeMap() {
        Kryo kryo = new Kryo();
        kryo.register(RotatingGlobeMap.class, new RotatingGlobeMapSerializer());

        RotatingGlobeMap data = new RotatingGlobeMap(123, 200, 200);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            RotatingGlobeMap data2 = kryo.readObject(input, RotatingGlobeMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testRoundSideWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(RoundSideWorldMap.class, new RoundSideWorldMapSerializer());

        RoundSideWorldMap data = new RoundSideWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            RoundSideWorldMap data2 = kryo.readObject(input, RoundSideWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testStretchWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(StretchWorldMap.class, new StretchWorldMapSerializer());

        StretchWorldMap data = new StretchWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            StretchWorldMap data2 = kryo.readObject(input, StretchWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTilingWorldMap() {
        Kryo kryo = new Kryo();
        kryo.register(TilingWorldMap.class, new TilingWorldMapSerializer());

        TilingWorldMap data = new TilingWorldMap(123, 200, 100);
		data.generate();

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            TilingWorldMap data2 = kryo.readObject(input, TilingWorldMap.class);
            Assert.assertEquals(data, data2);
        }
    }
}
