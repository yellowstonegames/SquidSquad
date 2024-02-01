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
import com.github.yellowstonegames.grid.RadialNoiseWrapper;

/**
 * Serializer for {@link RadialNoiseWrapper}; needs whatever INoise implementation this uses to be registered as well.
 */
public class RadialNoiseWrapperSerializer extends Serializer<RadialNoiseWrapper> {
    public RadialNoiseWrapperSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final RadialNoiseWrapper data) {
        kryo.writeClassAndObject(output, data.getWrapped());
        output.writeLong(data.getSeed());
        output.writeFloat(data.getFrequency());
        output.writeInt(data.getFractalType(), true);
        output.writeInt(data.getFractalOctaves(), true);
        output.writeBoolean(data.isFractalSpiral());
        output.writeFloat(data.getCenterX());
        output.writeFloat(data.getCenterY());
    }

    @Override
    public RadialNoiseWrapper read(final Kryo kryo, final Input input, final Class<? extends RadialNoiseWrapper> dataClass) {
        return new RadialNoiseWrapper((INoise) kryo.readClassAndObject(input), input.readLong(), input.readFloat(),
                input.readInt(true), input.readInt(true), input.readBoolean(), input.readFloat(), input.readFloat());
    }

    @Override
    public RadialNoiseWrapper copy(Kryo kryo, RadialNoiseWrapper original) {
        return new RadialNoiseWrapper(original);
    }
}
