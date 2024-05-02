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

package com.github.yellowstonegames.wrath.old.v300;

import com.github.yellowstonegames.old.v300.*;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.Assert;
import org.junit.Test;

public class OldTest {
    @Test
    public void testLightRNG() {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(LightRNG.class);

        LightRNG data = new LightRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LightRNG data2 = fury.deserializeJavaObject(bytes, LightRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(DiverRNG.class);

        DiverRNG data = new DiverRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            DiverRNG data2 = fury.deserializeJavaObject(bytes, DiverRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(LinnormRNG.class);

        LinnormRNG data = new LinnormRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LinnormRNG data2 = fury.deserializeJavaObject(bytes, LinnormRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(ThrustAltRNG.class);

        ThrustAltRNG data = new ThrustAltRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            ThrustAltRNG data2 = fury.deserializeJavaObject(bytes, ThrustAltRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(GWTRNG.class);

        GWTRNG data = new GWTRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            GWTRNG data2 = fury.deserializeJavaObject(bytes, GWTRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(SilkRNG.class);

        SilkRNG data = new SilkRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            SilkRNG data2 = fury.deserializeJavaObject(bytes, SilkRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(LongPeriodRNG.class);

        LongPeriodRNG data = new LongPeriodRNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            LongPeriodRNG data2 = fury.deserializeJavaObject(bytes, LongPeriodRNG.class);
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
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(XoshiroStarPhi32RNG.class);

        XoshiroStarPhi32RNG data = new XoshiroStarPhi32RNG(-9876543210L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            XoshiroStarPhi32RNG data2 = fury.deserializeJavaObject(bytes, XoshiroStarPhi32RNG.class);
            Assert.assertEquals(data.nextLong(), data2.nextLong());
            Assert.assertEquals(data.next(31), data2.next(31));
            Assert.assertEquals(data.nextInt(12345), data2.nextInt(12345));
            Assert.assertEquals(data.nextLong(-12345, 12345), data2.nextLong(-12345, 12345));
            Assert.assertEquals(data.nextDouble(), data2.nextDouble(), Double.MAX_VALUE);
            Assert.assertEquals(data, data2);
        }
    }

//    @Test
//    public void testLowStorageShuffler() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(LowStorageShuffler.class, new LowStorageShufflerSerializer());
//
//        LowStorageShuffler data = new LowStorageShuffler(5, 12345, 67890);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            LowStorageShuffler data2 = kryo.readObject(input, LowStorageShuffler.class);
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data, data2);
//        }
//    }
//    
//    @Test
//    public void testCrossHashYolk() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(CrossHash.Yolk.class, new CrossHashYolkSerializer());
//
//        CrossHash.Yolk hasher = CrossHash.Yolk.psi;
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, hasher);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CrossHash.Yolk hasher2 = kryo.readObject(input, CrossHash.Yolk.class);
//            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
//            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
//            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
//            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
//            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
//            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
//            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
//        }
//    }
//    
//    @Test
//    public void testCrossHashCurlup() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(CrossHash.Curlup.class, new CrossHashCurlupSerializer());
//
//        CrossHash.Curlup hasher = CrossHash.Curlup.psi;
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, hasher);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CrossHash.Curlup hasher2 = kryo.readObject(input, CrossHash.Curlup.class);
//            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
//            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
//            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
//            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
//            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
//            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
//            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
//        }
//    }
//    
//    @Test
//    public void testCrossHashMist() {
//        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
//        fury.register(CrossHash.Mist.class, new CrossHashMistSerializer());
//
//        CrossHash.Mist hasher = CrossHash.Mist.psi;
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, hasher);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            CrossHash.Mist hasher2 = kryo.readObject(input, CrossHash.Mist.class);
//            Assert.assertEquals(hasher.hash64(""), hasher2.hash64(""));
//            Assert.assertEquals(hasher.hash(""), hasher2.hash(""));
//            Assert.assertEquals(hasher.hash64("You're a kitty!"), hasher2.hash64("You're a kitty!"));
//            Assert.assertEquals(hasher.hash("You're a kitty!"), hasher2.hash("You're a kitty!"));
//            long[] longs = {1L, ~1L, 12345L, ~12345L, 1234567890123456789L, ~1234567890123456789L};
//            Assert.assertEquals(hasher.hash64(longs), hasher2.hash64(longs));
//            Assert.assertEquals(hasher.hash(longs), hasher2.hash(longs));
//        }
//    }
}
