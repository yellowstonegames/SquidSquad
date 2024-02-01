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
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.old.v300.LinnormRNG;

/**
 * Serializer for {@link LinnormRNG}; doesn't need anything else registered.
 */
public class LinnormRNGSerializer extends Serializer<LinnormRNG> {
    public LinnormRNGSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final LinnormRNG data) {
        output.writeLong(data.getState());
    }

    @Override
    public LinnormRNG read(final Kryo kryo, final Input input, final Class<? extends LinnormRNG> dataClass) {
        return new LinnormRNG(input.readLong());
    }

    @Override
    public LinnormRNG copy(Kryo kryo, LinnormRNG original) {
        return new LinnormRNG(original.getState());
    }
}
