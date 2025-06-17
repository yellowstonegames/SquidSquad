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

import com.github.yellowstonegames.grid.*;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Needs {@link CoordSerializer} for {@link Coord} and {@link RadianceSerializer} for {@link Radiance} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightSourceSerializer extends Serializer<LightSource> {
    public LightSourceSerializer(Fory fory) {
        super(fory, LightSource.class);
    }

    @Override
    public void write(MemoryBuffer buffer, LightSource data) {
        fory.writeRef(buffer, data.position);
        fory.writeRef(buffer, data.radiance);
        buffer.writeFloat32(data.span);
        buffer.writeFloat32(data.direction);
    }

    @Override
    public LightSource read(MemoryBuffer buffer) {
        return new LightSource((Coord) fory.readRef(buffer), (Radiance)fory.readRef(buffer),
                buffer.readFloat32(), buffer.readFloat32());
    }
}
