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
import com.github.yellowstonegames.old.v300.CrossHash;

/**
 * Serializer for {@link CrossHash.Curlup}; doesn't need anything else registered.
 */
public class CrossHashCurlupSerializer extends Serializer<CrossHash.Curlup> {
    public CrossHashCurlupSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CrossHash.Curlup data) {
        output.writeLong(data.seed);
    }

    @Override
    public CrossHash.Curlup read(final Kryo kryo, final Input input, final Class<? extends CrossHash.Curlup> dataClass) {
        CrossHash.Curlup hash = new CrossHash.Curlup();
        hash.seed = input.readLong();
        return hash;
    }

    @Override
    public CrossHash.Curlup copy(Kryo kryo, CrossHash.Curlup original) {
        CrossHash.Curlup hash = new CrossHash.Curlup();
        hash.seed = original.seed;
        return hash;
    }
}
