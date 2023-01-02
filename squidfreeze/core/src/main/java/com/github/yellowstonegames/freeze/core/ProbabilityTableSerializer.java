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

package com.github.yellowstonegames.freeze.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.NumberedSet;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.ProbabilityTable;

/**
 * Needs {@link com.github.tommyettinger.kryo.juniper.EnhancedRandomSerializer} to be registered,
 * {@link com.esotericsoftware.kryo.serializers.CollectionSerializer} for {@link ObjectList} to be registered,
 * {@link com.esotericsoftware.kryo.serializers.CollectionSerializer} for {@link NumberedSet} to be registered,
 * and {@link com.github.tommyettinger.kryo.jdkgdxds.IntListSerializer} to be registered,
 * plus whatever type of EnhancedRandom the ProbabilityTable uses to be registered (by default,
 * {@link com.github.tommyettinger.kryo.juniper.WhiskerRandomSerializer}).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ProbabilityTableSerializer extends Serializer<ProbabilityTable> {
    public ProbabilityTableSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final ProbabilityTable data) {
        kryo.writeClassAndObject(output, data.rng);
        kryo.writeObject(output, data.table);
        kryo.writeObject(output, data.extraTable);
        kryo.writeObject(output, data.weights);
    }

    @Override
    public ProbabilityTable read(final Kryo kryo, final Input input, final Class<? extends ProbabilityTable> dataClass) {
        ProbabilityTable pt = new ProbabilityTable<>((EnhancedRandom) kryo.readClassAndObject(input));
        NumberedSet ns = kryo.readObject(input, NumberedSet.class);
        ObjectList ex = kryo.readObject(input, ObjectList.class);
        IntList wt = kryo.readObject(input, IntList.class);
        for (int i = 0; i < ns.size(); i++) {
            pt.add(ns.getAt(i), wt.get(i));
        }
        for (int i = 0, w = ns.size(); i < ex.size(); i++, w++) {
            pt.add((ProbabilityTable) ex.get(i), wt.get(w));
        }
        return pt;
    }


    @Override
    public ProbabilityTable copy(Kryo kryo, ProbabilityTable original) {
        return new ProbabilityTable(original);
    }
}
