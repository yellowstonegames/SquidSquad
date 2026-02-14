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

package com.github.yellowstonegames.freeze.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.ds.IntDeque;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.yellowstonegames.core.Cards;

/**
 * Needs {@code String[]} registered with {@code kryo.register(String[].class);},
 * {@link com.github.tommyettinger.kryo.juniper.EnhancedRandomSerializer} to be registered
 * and {@link com.github.tommyettinger.kryo.jdkgdxds.IntDequeSerializer} to be registered,
 * plus whatever type of EnhancedRandom the Cards uses to be registered (by default,
 * {@link com.github.tommyettinger.kryo.juniper.Xoshiro256MX3RandomSerializer}).
 */
public class CardsSerializer extends Serializer<Cards> {
    public CardsSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Cards data) {
        kryo.writeObject(output, data.names);
        kryo.writeClassAndObject(output, data.random);
        kryo.writeObject(output, data.deck);
    }

    @Override
    public Cards read(final Kryo kryo, final Input input, final Class<? extends Cards> dataClass) {
        String[] names = kryo.readObject(input, String[].class);
        Cards cs = new Cards(names, (EnhancedRandom) kryo.readClassAndObject(input));
        cs.deck = kryo.readObject(input, IntDeque.class);
        return cs;
    }

    @Override
    public Cards copy(Kryo kryo, Cards original) {
        return new Cards(original);
    }
}
