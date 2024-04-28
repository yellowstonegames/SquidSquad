/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.github.yellowstonegames.grid.Radiance;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

public class RadianceSerializer extends Serializer<Radiance> {
    public RadianceSerializer(Fury fury) {
        super(fury, Radiance.class);
    }

    @Override
    public void write(MemoryBuffer buffer, Radiance data) {
        buffer.writeFloat(data.range);
        buffer.writeInt(data.color);
        buffer.writeFloat(data.flicker);
        buffer.writeFloat(data.strobe);
        buffer.writeFloat(data.delay);
        buffer.writeFloat(data.flare);
        buffer.writeInt(data.seed);

    }

    @Override
    public Radiance read(MemoryBuffer buffer) {
        return new Radiance(buffer.readFloat(), buffer.readInt(),
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readFloat(), buffer.readInt());
    }
}
