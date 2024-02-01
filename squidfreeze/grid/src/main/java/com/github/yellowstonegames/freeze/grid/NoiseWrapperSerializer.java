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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.NoiseWrapper;

/**
 * Serializer for {@link NoiseWrapper}; needs whatever INoise implementation this uses to be registered as well.
 */
public class NoiseWrapperSerializer extends Serializer<NoiseWrapper> {
    public NoiseWrapperSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final NoiseWrapper data) {
        kryo.writeClassAndObject(output, data.getWrapped());
        output.writeLong(data.getSeed());
        output.writeFloat(data.getFrequency());
        output.writeInt(data.getFractalType(), true);
        output.writeInt(data.getFractalOctaves(), true);
        output.writeBoolean(data.isFractalSpiral());
    }

    @Override
    public NoiseWrapper read(final Kryo kryo, final Input input, final Class<? extends NoiseWrapper> dataClass) {
        return new NoiseWrapper((INoise) kryo.readClassAndObject(input), input.readLong(), input.readFloat(),
                input.readInt(true), input.readInt(true), input.readBoolean());
    }

    @Override
    public NoiseWrapper copy(Kryo kryo, NoiseWrapper original) {
        return new NoiseWrapper(original);
    }
}
