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
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.GapShuffler;

/**
 * Needs {@link com.github.tommyettinger.kryo.juniper.EnhancedRandomSerializer} to be registered,
 * {@link com.esotericsoftware.kryo.serializers.CollectionSerializer} for {@link ObjectList} to be registered,
 * and whatever type of EnhancedRandom the GapShuffler uses to be registered (by default,
 * {@link com.github.tommyettinger.kryo.juniper.WhiskerRandomSerializer}).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GapShufflerSerializer extends Serializer<GapShuffler> {
    public GapShufflerSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final GapShuffler data) {
        ObjectList items = new ObjectList();
        data.fillInto(items);
        kryo.writeObject(output, items);
        kryo.writeClassAndObject(output, data.random);
        output.writeInt(data.getIndex(), true);
    }

    @Override
    public GapShuffler read(final Kryo kryo, final Input input, final Class<? extends GapShuffler> dataClass) {
        return new GapShuffler<>(kryo.readObject(input, ObjectList.class),
                (EnhancedRandom) kryo.readClassAndObject(input),
                input.readInt(true),
                true, false);
    }


    @Override
    public GapShuffler copy(Kryo kryo, GapShuffler original) {
        return new GapShuffler(original);
    }
}
