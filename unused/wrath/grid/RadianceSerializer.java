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

package com.github.yellowstonegames.wrath.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.Radiance;

/**
 * Serializer for {@link Radiance}; doesn't need anything else registered.
 */
public class RadianceSerializer extends Serializer<Radiance> {
    public RadianceSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Radiance data) {
        output.writeFloat(data.range);
        output.writeInt(data.color, false);
        output.writeFloat(data.flicker);
        output.writeFloat(data.strobe);
        output.writeFloat(data.delay);
        output.writeFloat(data.flare);
        output.writeInt(data.seed, false);
    }

    @Override
    public Radiance read(final Kryo kryo, final Input input, final Class<? extends Radiance> dataClass) {
        return new Radiance(input.readFloat(), input.readInt(false),
                input.readFloat(), input.readFloat(), input.readFloat(), input.readFloat(), input.readInt(false));
    }

    @Override
    public Radiance copy(Kryo kryo, Radiance original) {
        return new Radiance(original);
    }
}
