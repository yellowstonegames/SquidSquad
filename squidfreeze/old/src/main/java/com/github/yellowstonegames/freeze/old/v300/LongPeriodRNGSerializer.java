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
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.old.v300.LongPeriodRNG;

/**
 * Serializer for {@link LongPeriodRNG}; doesn't need anything else registered.
 */
public class LongPeriodRNGSerializer extends Serializer<LongPeriodRNG> {
    public LongPeriodRNGSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final LongPeriodRNG data) {
        output.writeLongs(data.state, 0, 16, false);
        output.writeInt(data.choice, true);
    }

    @Override
    public LongPeriodRNG read(final Kryo kryo, final Input input, final Class<? extends LongPeriodRNG> dataClass) {
        return new LongPeriodRNG(input.readLongs(16, false), input.readInt(true));
    }

    @Override
    public LongPeriodRNG copy(Kryo kryo, LongPeriodRNG original) {
        return new LongPeriodRNG(original);
    }
}