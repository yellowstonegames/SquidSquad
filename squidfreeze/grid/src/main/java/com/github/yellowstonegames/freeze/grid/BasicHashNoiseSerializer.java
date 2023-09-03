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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.BasicHashNoise;
import com.github.yellowstonegames.grid.IPointHash;

/**
 * Serializer for {@link BasicHashNoise}; may need the used implementation of {@link IPointHash} registered.
 */
public class BasicHashNoiseSerializer extends Serializer<BasicHashNoise> {
    public BasicHashNoiseSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final BasicHashNoise data) {
        output.writeInt(data.seed);
        kryo.writeClassAndObject(output, data.pointHash);
    }

    @Override
    public BasicHashNoise read(final Kryo kryo, final Input input, final Class<? extends BasicHashNoise> dataClass) {
        return new BasicHashNoise(input.readInt(), (IPointHash) kryo.readClassAndObject(input));
    }

    @Override
    public BasicHashNoise copy(Kryo kryo, BasicHashNoise original) {
        return new BasicHashNoise(original.seed, original.pointHash);
    }
}
