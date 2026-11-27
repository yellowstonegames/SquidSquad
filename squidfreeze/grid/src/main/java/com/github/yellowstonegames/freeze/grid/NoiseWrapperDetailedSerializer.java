/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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
import com.github.yellowstonegames.grid.NoiseWrapperDetailed;

/**
 * Serializer for {@link NoiseWrapperDetailed}; needs whatever INoise implementation this uses to be registered as well.
 */
public class NoiseWrapperDetailedSerializer extends Serializer<NoiseWrapperDetailed> {
    public NoiseWrapperDetailedSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final NoiseWrapperDetailed data) {
        kryo.writeClassAndObject(output, data.getWrapped());
        output.writeLong(data.getSeed());
        output.writeFloat(data.getFrequency());
        output.writeInt(data.getFractalType(), true);
        output.writeInt(data.getFractalOctaves(), true);
        output.writeBoolean(data.isFractalSpiral());
        output.writeFloat(data.getLacunarity());
        output.writeFloat(data.getGain());
    }

    @Override
    public NoiseWrapperDetailed read(final Kryo kryo, final Input input, final Class<? extends NoiseWrapperDetailed> dataClass) {
        return new NoiseWrapperDetailed((INoise) kryo.readClassAndObject(input), input.readLong(), input.readFloat(),
                input.readInt(true), input.readInt(true), input.readBoolean(), input.readFloat(), input.readFloat());
    }

    @Override
    public NoiseWrapperDetailed copy(Kryo kryo, NoiseWrapperDetailed original) {
        return new NoiseWrapperDetailed(original);
    }
}
