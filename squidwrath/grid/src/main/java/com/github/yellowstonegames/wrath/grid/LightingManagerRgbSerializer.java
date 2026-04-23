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
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@link Region}, {@link CoordSerializer}
 * for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque}, {@link RadianceSerializer} for
 * {@link Radiance}, and {@link LightSourceSerializer} for {@link LightSource} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightingManagerRgbSerializer extends Serializer<LightingManagerRgb> {
    public LightingManagerRgbSerializer(Fory fory) {
        super(fory.getConfig(), LightingManagerRgb.class);
    }

    @Override
    public void write(WriteContext fory, LightingManagerRgb data) {
        fory.writeRef(data.resistances);
        fory.writeInt32(data.backgroundColor);
        fory.writeVarUint32(data.radiusStrategy.ordinal());
        fory.writeFloat32(data.viewerRange);
        fory.writeRef(data.noticeable);
        fory.writeRef(data.lights);
        fory.writeRef(data.colorLighting);
        fory.writeRef(data.lightingStrength);
        fory.writeRef(data.fovResult);
        fory.writeRef(data.lightFromFOV);
        fory.writeRef(data.losResult);
    }

    @Override
    public LightingManagerRgb read(ReadContext fory) {
        LightingManagerRgb lm = new LightingManagerRgb((float[][]) fory.readRef(), fory.readInt32(), Radius.ALL[fory.readVarUint32()], fory.readFloat32());
        lm.noticeable = (Region) fory.readRef();
        lm.lights = (ObjectDeque<LightSource>) fory.readRef();
        lm.colorLighting = (int[][]) fory.readRef();
        lm.lightingStrength = (float[][]) fory.readRef();
        lm.fovResult = (float[][]) fory.readRef();
        lm.lightFromFOV = (float[][]) fory.readRef();
        lm.losResult = (float[][]) fory.readRef();
        return lm;
    }
}
