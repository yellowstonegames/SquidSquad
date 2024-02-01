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
import com.github.yellowstonegames.old.v300.CrossHash;

/**
 * Serializer for {@link CrossHash.Mist}; doesn't need anything else registered.
 */
public class CrossHashMistSerializer extends Serializer<CrossHash.Mist> {
    public CrossHashMistSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CrossHash.Mist data) {
        output.writeLong(data.l1);
        output.writeLong(data.l2);
    }

    @Override
    public CrossHash.Mist read(final Kryo kryo, final Input input, final Class<? extends CrossHash.Mist> dataClass) {
        CrossHash.Mist hash = new CrossHash.Mist();
        hash.l1 = input.readLong();
        hash.l2 = input.readLong();
        return hash;
    }

    @Override
    public CrossHash.Mist copy(Kryo kryo, CrossHash.Mist original) {
        CrossHash.Mist hash = new CrossHash.Mist();
        hash.l1 = original.l1;
        hash.l2 = original.l2;
        return hash;
    }
}
