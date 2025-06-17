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
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Serializes {@link Radiance}; doesn't need anything else registered.
 */
public class RadianceSerializer extends Serializer<Radiance> {
    public RadianceSerializer(Fory fory) {
        super(fory, Radiance.class);
    }

    @Override
    public void write(MemoryBuffer buffer, Radiance data) {
        buffer.writeFloat32(data.range);
        buffer.writeInt32(data.color);
        buffer.writeFloat32(data.flicker);
        buffer.writeFloat32(data.strobe);
        buffer.writeFloat32(data.delay);
        buffer.writeFloat32(data.flare);
        buffer.writeInt32(data.seed);

    }

    @Override
    public Radiance read(MemoryBuffer buffer) {
        return new Radiance(buffer.readFloat32(), buffer.readInt32(),
                buffer.readFloat32(), buffer.readFloat32(), buffer.readFloat32(), buffer.readFloat32(), buffer.readInt32());
    }
}
