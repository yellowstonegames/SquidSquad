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

package com.github.yellowstonegames.freeze.old.v300;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.old.v300.DiverRNG;
import com.github.yellowstonegames.old.v300.LightRNG;
import com.github.yellowstonegames.old.v300.LinnormRNG;
import com.github.yellowstonegames.old.v300.ThrustAltRNG;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class OldTest {
    @Test
    public void testLightRNG() {
        Kryo kryo = new Kryo();
        kryo.register(LightRNG.class, new LightRNGSerializer());

        LightRNG data = new LightRNG(-9876543210L);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
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
}
