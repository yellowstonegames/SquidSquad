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

package com.github.yellowstonegames.freeze.old.v300;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.old.v300.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class OldTest {
    @Test
    public void testLightRNG() {
        Kryo kryo = new Kryo();
        kryo.register(LightRNG.class, new LightRNGSerializer());

        LightRNG data = new LightRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LightRNG data2 = kryo.readObject(input, LightRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testDiverRNG() {
        Kryo kryo = new Kryo();
        kryo.register(DiverRNG.class, new DiverRNGSerializer());

        DiverRNG data = new DiverRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            DiverRNG data2 = kryo.readObject(input, DiverRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLinnormRNG() {
        Kryo kryo = new Kryo();
        kryo.register(LinnormRNG.class, new LinnormRNGSerializer());

        LinnormRNG data = new LinnormRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LinnormRNG data2 = kryo.readObject(input, LinnormRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testThrustAltRNG() {
        Kryo kryo = new Kryo();
        kryo.register(ThrustAltRNG.class, new ThrustAltRNGSerializer());

        ThrustAltRNG data = new ThrustAltRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            ThrustAltRNG data2 = kryo.readObject(input, ThrustAltRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testGWTRNG() {
        Kryo kryo = new Kryo();
        kryo.register(GWTRNG.class, new GWTRNGSerializer());

        GWTRNG data = new GWTRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            GWTRNG data2 = kryo.readObject(input, GWTRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testSilkRNG() {
        Kryo kryo = new Kryo();
        kryo.register(SilkRNG.class, new SilkRNGSerializer());

        SilkRNG data = new SilkRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            SilkRNG data2 = kryo.readObject(input, SilkRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLongPeriodRNG() {
        Kryo kryo = new Kryo();
        kryo.register(LongPeriodRNG.class, new LongPeriodRNGSerializer());

        LongPeriodRNG data = new LongPeriodRNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LongPeriodRNG data2 = kryo.readObject(input, LongPeriodRNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testXoshiroStarPhi32RNG() {
        Kryo kryo = new Kryo();
        kryo.register(XoshiroStarPhi32RNG.class, new XoshiroStarPhi32RNGSerializer());

        XoshiroStarPhi32RNG data = new XoshiroStarPhi32RNG(-9876543210L);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            XoshiroStarPhi32RNG data2 = kryo.readObject(input, XoshiroStarPhi32RNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLowStorageShuffler() {
        Kryo kryo = new Kryo();
        kryo.register(LowStorageShuffler.class, new LowStorageShufflerSerializer());

        LowStorageShuffler data = new LowStorageShuffler(5, 12345, 67890);

        Output output = new Output(32, -1);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            LowStorageShuffler data2 = kryo.readObject(input, LowStorageShuffler.class);
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data.next(), data2.next());
            Assert.assertEquals(data, data2);
        }
    }
    
    @Test
    public void testCrossHashYolk() {
        Kryo kryo = new Kryo();
        kryo.register(CrossHash.Yolk.class, new CrossHashYolkSerializer());

        CrossHash.Yolk hasher = CrossHash.Yolk.psi;

        Output output = new Output(32, -1);
        kryo.writeObject(output, hasher);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CrossHash.Yolk hasher2 = kryo.readObject(input, CrossHash.Yolk.class);
            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
        }
    }
    
    @Test
    public void testCrossHashCurlup() {
        Kryo kryo = new Kryo();
        kryo.register(CrossHash.Curlup.class, new CrossHashCurlupSerializer());

        CrossHash.Curlup hasher = CrossHash.Curlup.psi;

        Output output = new Output(32, -1);
        kryo.writeObject(output, hasher);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CrossHash.Curlup hasher2 = kryo.readObject(input, CrossHash.Curlup.class);
            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
        }
    }
    
    @Test
    public void testCrossHashMist() {
        Kryo kryo = new Kryo();
        kryo.register(CrossHash.Mist.class, new CrossHashMistSerializer());

        CrossHash.Mist hasher = CrossHash.Mist.psi;

        Output output = new Output(32, -1);
        kryo.writeObject(output, hasher);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            CrossHash.Mist hasher2 = kryo.readObject(input, CrossHash.Mist.class);
            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
        }
    }
}
