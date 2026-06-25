/*
 * Copyright (c) 2024-2026; see AUTHORS file.
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
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

/**
 * Serializes {@link Radiance}; doesn't need anything else registered.
 */
public class RadianceSerializer extends Serializer<Radiance> {
    public RadianceSerializer(Fory fory) {
        super(fory.getConfig(), Radiance.class);
    }

    @Override
    public void write(WriteContext fory, Radiance data) {
        fory.writeFloat32(data.range);
        fory.writeInt32(data.color);
        fory.writeFloat32(data.flicker);
        fory.writeFloat32(data.strobe);
        fory.writeFloat32(data.delay);
        fory.writeFloat32(data.flare);
        fory.writeInt32(data.seed);

    }

    @Override
    public Radiance read(ReadContext fory) {
        return new Radiance(fory.readFloat32(), fory.readInt32(),
                fory.readFloat32(), fory.readFloat32(), fory.readFloat32(), fory.readFloat32(), fory.readInt32());
    }
}
