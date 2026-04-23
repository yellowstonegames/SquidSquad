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
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Needs {@link CoordSerializer} for {@link Coord} and {@link RadianceSerializer} for {@link Radiance} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightSourceSerializer extends Serializer<LightSource> {
    public LightSourceSerializer(Fory fory) {
        super(fory.getConfig(), LightSource.class);
    }

    @Override
    public void write(WriteContext fory, LightSource data) {
        fory.writeRef(data.position);
        fory.writeRef(data.radiance);
        fory.writeFloat32(data.span);
        fory.writeFloat32(data.direction);
    }

    @Override
    public LightSource read(ReadContext fory) {
        return new LightSource((Coord) fory.readRef(), (Radiance)fory.readRef(),
                fory.readFloat32(), fory.readFloat32());
    }
}
