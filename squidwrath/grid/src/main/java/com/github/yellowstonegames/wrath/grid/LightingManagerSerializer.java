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

import com.github.tommyettinger.ds.ObjectDeque;
import com.github.tommyettinger.tantrum.jdkgdxds.ObjectDequeSerializer;
import com.github.yellowstonegames.grid.*;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@link Region}, {@link CoordSerializer}
 * for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque}, {@link RadianceSerializer} for
 * {@link Radiance}, and {@link LightSourceSerializer} for {@link LightSource} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightingManagerSerializer extends Serializer<LightingManager> {
    public LightingManagerSerializer(Fory fory) {
        super(fory, LightingManager.class);
    }

    @Override
    public void write(MemoryBuffer buffer, LightingManager data) {
        fory.writeRef(buffer, data.resistances);
        buffer.writeInt32(data.backgroundColor);
        buffer.writeVarUint32(data.radiusStrategy.ordinal());
        buffer.writeFloat32(data.viewerRange);
        fory.writeRef(buffer, data.noticeable);
        fory.writeRef(buffer, data.lights);
        fory.writeRef(buffer, data.colorLighting);
        fory.writeRef(buffer, data.lightingStrength);
        fory.writeRef(buffer, data.fovResult);
        fory.writeRef(buffer, data.lightFromFOV);
        fory.writeRef(buffer, data.losResult);
    }

    @Override
    public LightingManager read(MemoryBuffer buffer) {
        LightingManager lm = new LightingManager((float[][]) fory.readRef(buffer), buffer.readInt32(), Radius.ALL[buffer.readVarUint32()], buffer.readFloat32());
        lm.noticeable = (Region) fory.readRef(buffer);
        lm.lights = (ObjectDeque<LightSource>) fory.readRef(buffer);
        lm.colorLighting = (int[][]) fory.readRef(buffer);
        lm.lightingStrength = (float[][]) fory.readRef(buffer);
        lm.fovResult = (float[][]) fory.readRef(buffer);
        lm.lightFromFOV = (float[][]) fory.readRef(buffer);
        lm.losResult = (float[][]) fory.readRef(buffer);
        return lm;
    }
}
