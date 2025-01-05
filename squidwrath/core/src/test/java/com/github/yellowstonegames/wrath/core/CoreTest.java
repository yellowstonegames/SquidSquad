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

import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.NumberedSet;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.tantrum.jdkgdxds.IntListSerializer;
import com.github.tommyettinger.tantrum.jdkgdxds.NumberedSetSerializer;
import com.github.tommyettinger.tantrum.jdkgdxds.ObjectListSerializer;
import com.github.yellowstonegames.core.*;
import org.apache.fury.Fury;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class CoreTest {
    @Test
    public void testDiceRule() {
        LoggerFactory.disableLogging();        
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(Dice.Rule.class);

        Dice.Rule data = new Dice.Rule("3>4d6");

        byte[] bytes = fury.serializeJavaObject(data);
        Dice.Rule data2 = fury.deserializeJavaObject(bytes, Dice.Rule.class);
        Dice d1 = new Dice(123L);
        Dice d2 = new Dice(123L);
        Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
        Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testGapShuffler() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(EnhancedRandom.class);
        fury.register(AceRandom.class);
        fury.registerSerializer(ObjectList.class, new ObjectListSerializer(fury));
        fury.register(GapShuffler.class);

        GapShuffler<String> data = new GapShuffler<>(new String[]{"Foo", "Bar", "Baz", "Quux"}, new AceRandom(123));

        byte[] bytes = fury.serializeJavaObject(data);
        GapShuffler data2 = fury.deserializeJavaObject(bytes, GapShuffler.class);
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testProbabilityTable() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(EnhancedRandom.class);
        fury.register(WhiskerRandom.class);
        fury.registerSerializer(ObjectList.class, new ObjectListSerializer(fury));
        fury.registerSerializer(NumberedSet.class, new NumberedSetSerializer(fury));
        fury.registerSerializer(IntList.class, new IntListSerializer(fury));
        fury.register(ProbabilityTable.class);

        ProbabilityTable<String> data = new ProbabilityTable<>(new WhiskerRandom(123));
        data.add("Foo", 5);
        data.add("Bar", 4);
        data.add("Baz", 3);
        data.add("Quux", 1);
        ProbabilityTable<String> bonus = new ProbabilityTable<>(new WhiskerRandom(456));
        bonus.add("Magic", 1);
        bonus.add("Normality", 10);
        data.add(bonus, 6);

        byte[] bytes = fury.serializeJavaObject(data);
        ProbabilityTable data2 = fury.deserializeJavaObject(bytes, ProbabilityTable.class);
        Assert.assertEquals(data.random(), data2.random());
        Assert.assertEquals(data.random(), data2.random());
        Assert.assertEquals(data, data2);
    }


    @Test
    public void testWeightedTable() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(WeightedTable.class);

        WeightedTable data = new WeightedTable(1f, 2f, 3f, 4f, 0.5f, 5.5f);

        byte[] bytes = fury.serializeJavaObject(data);
        WeightedTable data2 = fury.deserializeJavaObject(bytes, WeightedTable.class);
        Assert.assertEquals(data.random(0L), data2.random(0L));
        Assert.assertEquals(data.random(1L), data2.random(1L));
        Assert.assertEquals(data.random(2L), data2.random(2L));
        Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
        Assert.assertEquals(data, data2);

    }

    @Test
    public void testIntShuffler() {        
        LoggerFactory.disableLogging();
        Fury fury = Fury.builder().withLanguage(Language.JAVA).build();
        fury.register(IntShuffler.class);

        IntShuffler data = new IntShuffler(10, 123L);

        byte[] bytes = fury.serializeJavaObject(data);
        IntShuffler data2 = fury.deserializeJavaObject(bytes, IntShuffler.class);
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
        Assert.assertEquals(data, data2);

    }
}
