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
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.apache.fory.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class CoreTest {
    @Test
    public void testDiceRule() {
        LoggerFactory.disableLogging();        
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Dice.Rule.class);

        Dice.Rule data = new Dice.Rule("3>4d6");

        byte[] bytes = fory.serializeJavaObject(data);
        Dice.Rule data2 = fory.deserializeJavaObject(bytes, Dice.Rule.class);
        Dice d1 = new Dice(123L);
        Dice d2 = new Dice(123L);
        Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
        Assert.assertEquals(d1.runRollRule(data), d2.runRollRule(data2));
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testGapShuffler() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(EnhancedRandom.class);
        fory.register(AceRandom.class);
        fory.registerSerializer(ObjectList.class, new ObjectListSerializer(fory));
        fory.register(GapShuffler.class);

        GapShuffler<String> data = new GapShuffler<>(new String[]{"Foo", "Bar", "Baz", "Quux"}, new AceRandom(123));

        byte[] bytes = fory.serializeJavaObject(data);
        GapShuffler data2 = fory.deserializeJavaObject(bytes, GapShuffler.class);
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data, data2);
    }

    @Test
    public void testProbabilityTable() {        
//        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(EnhancedRandom.class);
        fory.register(WhiskerRandom.class);
        fory.registerSerializer(ObjectList.class, new ObjectListSerializer(fory));
        fory.registerSerializer(NumberedSet.class, new NumberedSetSerializer(fory));
        fory.registerSerializer(IntList.class, new IntListSerializer(fory));
        fory.register(ProbabilityTable.class);

        ProbabilityTable<String> data = new ProbabilityTable<>(new WhiskerRandom(123));
        data.add("Foo", 5);
        data.add("Bar", 4);
        data.add("Baz", 3);
        data.add("Quux", 1);
        ProbabilityTable<String> bonus = new ProbabilityTable<>(new WhiskerRandom(456));
        bonus.add("Magic", 1);
        bonus.add("Normality", 10);
        data.add(bonus, 6);

        byte[] bytes = fory.serializeJavaObject(data);
        ProbabilityTable data2 = fory.deserializeJavaObject(bytes, ProbabilityTable.class);
        Assert.assertEquals(data.random(), data2.random());
        Assert.assertEquals(data.random(), data2.random());
        Assert.assertEquals(data, data2);
    }


    @Test
    public void testWeightedTable() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(WeightedTable.class);

        WeightedTable data = new WeightedTable(1f, 2f, 3f, 4f, 0.5f, 5.5f);

        byte[] bytes = fory.serializeJavaObject(data);
        WeightedTable data2 = fory.deserializeJavaObject(bytes, WeightedTable.class);
        Assert.assertEquals(data.random(0L), data2.random(0L));
        Assert.assertEquals(data.random(1L), data2.random(1L));
        Assert.assertEquals(data.random(2L), data2.random(2L));
        Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
        Assert.assertEquals(data, data2);

    }

    @Test
    public void testIntShuffler() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(IntShuffler.class);

        IntShuffler data = new IntShuffler(10, 123L);

        byte[] bytes = fory.serializeJavaObject(data);
        IntShuffler data2 = fory.deserializeJavaObject(bytes, IntShuffler.class);
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.next(), data2.next());
        Assert.assertEquals(data.stringSerialize(), data2.stringSerialize());
        Assert.assertEquals(data, data2);

    }
}
