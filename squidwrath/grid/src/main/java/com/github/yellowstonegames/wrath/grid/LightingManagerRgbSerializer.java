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
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@link Region}, {@link CoordSerializer}
 * for {@link Coord}, {@link CoordObjectOrderedMapSerializer} for {@link CoordObjectOrderedMap}, and
 * {@link RadianceSerializer} for {@link Radiance} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightingManagerRgbSerializer extends Serializer<LightingManagerRgb> {
    public LightingManagerRgbSerializer(Fury fury) {
        super(fury, LightingManagerRgb.class);
    }

    @Override
    public void write(MemoryBuffer buffer, LightingManagerRgb data) {
        fury.writeRef(buffer, data.resistances);
        buffer.writeInt32(data.backgroundColor);
        buffer.writeVarUint32(data.radiusStrategy.ordinal());
        buffer.writeFloat32(data.viewerRange);
        fury.writeRef(buffer, data.noticeable);
        fury.writeRef(buffer, data.lights);
        fury.writeRef(buffer, data.colorLighting);
        fury.writeRef(buffer, data.lightingStrength);
        fury.writeRef(buffer, data.fovResult);
        fury.writeRef(buffer, data.lightFromFOV);
        fury.writeRef(buffer, data.losResult);
    }

    @Override
    public LightingManagerRgb read(MemoryBuffer buffer) {
        LightingManagerRgb lm = new LightingManagerRgb((float[][]) fury.readRef(buffer), buffer.readInt32(), Radius.ALL[buffer.readVarUint32()], buffer.readFloat32());
        lm.noticeable = (Region) fury.readRef(buffer);
        lm.lights = (CoordObjectOrderedMap) fury.readRef(buffer);
        lm.colorLighting = (int[][]) fury.readRef(buffer);
        lm.lightingStrength = (float[][]) fury.readRef(buffer);
        lm.fovResult = (float[][]) fury.readRef(buffer);
        lm.lightFromFOV = (float[][]) fury.readRef(buffer);
        lm.losResult = (float[][]) fury.readRef(buffer);
        return lm;
    }
}
