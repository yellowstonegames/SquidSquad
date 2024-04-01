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

package com.github.yellowstonegames.wrath.core;

import com.github.yellowstonegames.core.Dice;
import io.fury.Fury;
import io.fury.config.Language;
import org.junit.Assert;
import org.junit.Test;

public class CoreTest {
    @Test
    public void testDiceRule() {
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.registerSerializer(Dice.Rule.class, new DiceRuleSerializer(fury));

        Dice.Rule data = new Dice.Rule("3>4d6");

        byte[] bytes = fury.serializeJavaObject(data);
        Dice.Rule data2 = fury.deserializeJavaObject(bytes, Dice.Rule.class);
            Dice d1 = new Dice(123L);
            Dice d2 = new Dice(123L);
            Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
            Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
            Assert.assertEquals(data, data2);
    }

//    @Test
//    public void testGapShuffler() {
//        Kryo kryo = new Kryo();
//        kryo.register(EnhancedRandom.class, new EnhancedRandomSerializer());
//        kryo.register(WhiskerRandom.class, new WhiskerRandomSerializer());
//        kryo.register(ObjectList.class, new CollectionSerializer<ObjectList<?>>());
//        kryo.register(GapShuffler.class, new GapShufflerSerializer());
//
//        GapShuffler<String> data = new GapShuffler<>(new String[]{"Foo", "Bar", "Baz", "Quux"}, new WhiskerRandom(123));
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            GapShuffler data2 = kryo.readObject(input, GapShuffler.class);
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testProbabilityTable() {
//        Kryo kryo = new Kryo();
//        kryo.register(EnhancedRandom.class, new EnhancedRandomSerializer());
//        kryo.register(WhiskerRandom.class, new WhiskerRandomSerializer());
//        kryo.register(ObjectList.class, new CollectionSerializer<ObjectList<?>>());
//        kryo.register(NumberedSet.class, new CollectionSerializer<NumberedSet<?>>());
//        kryo.register(IntList.class, new IntListSerializer());
//        kryo.register(ProbabilityTable.class, new ProbabilityTableSerializer());
//
//        ProbabilityTable<String> data = new ProbabilityTable<>(new WhiskerRandom(123));
//        data.add("Foo", 5);
//        data.add("Bar", 4);
//        data.add("Baz", 3);
//        data.add("Quux", 1);
//        ProbabilityTable<String> bonus = new ProbabilityTable<>(new WhiskerRandom(456));
//        bonus.add("Magic", 1);
//        bonus.add("Normality", 10);
//        data.add(bonus, 6);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            ProbabilityTable data2 = kryo.readObject(input, ProbabilityTable.class);
//            Assert.assertEquals(data.random(), data2.random());
//            Assert.assertEquals(data.random(), data2.random());
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testWeightedTable() {
//        Kryo kryo = new Kryo();
//        kryo.register(WeightedTable.class, new WeightedTableSerializer());
//
//        WeightedTable data = new WeightedTable(1f, 2f, 3f, 4f, 0.5f, 5.5f);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            WeightedTable data2 = kryo.readObject(input, WeightedTable.class);
//            Assert.assertEquals(data.random(0L), data2.random(0L));
//            Assert.assertEquals(data.random(1L), data2.random(1L));
//            Assert.assertEquals(data.random(2L), data2.random(2L));
//            Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
//            Assert.assertEquals(data, data2);
//        }
//    }
//
//    @Test
//    public void testIntShuffler() {
//        Kryo kryo = new Kryo();
//        kryo.register(IntShuffler.class, new IntShufflerSerializer());
//
//        IntShuffler data = new IntShuffler(10, 123L);
//
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
//        Output output = new Output(baos);
//        kryo.writeObject(output, data);
//        byte[] bytes = output.toBytes();
//        try (Input input = new Input(bytes)) {
//            IntShuffler data2 = kryo.readObject(input, IntShuffler.class);
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.next(), data2.next());
//            Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
//            Assert.assertEquals(data, data2);
//        }
//    }
}
