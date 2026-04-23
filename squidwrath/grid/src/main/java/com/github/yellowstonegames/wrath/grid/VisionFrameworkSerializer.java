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
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link Region}, {@link CoordSerializer} for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque},
 * {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap}, {@link RadianceSerializer} for
 * {@link Radiance}, {@link LightingManagerSerializer} for {@link LightingManager}, and {@link LightSourceSerializer}
 * for {@link LightSource} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class VisionFrameworkSerializer extends Serializer<VisionFramework> {
    public VisionFrameworkSerializer(Fory fory) {
        super(fory.getConfig(), VisionFramework.class);
    }

    @Override
    public void write(WriteContext fory, VisionFramework data) {
        fory.writeVarUint32(data.placeWidth);
        fory.writeVarUint32(data.placeHeight);
        fory.writeInt32(data.rememberedColor);
        fory.writeRef(data.linePlaceMap);
        fory.writeRef(data.prunedPlaceMap);
        fory.writeRef(data.backgroundColors);
        fory.writeRef(data.previousLightLevels);
        fory.writeRef(data.blockage);
        fory.writeRef(data.inView);
        fory.writeRef(data.justHidden);
        fory.writeRef(data.justSeen);
        fory.writeRef(data.seen);
        fory.writeRef(data.newlyVisible);
        fory.writeRef(data.lighting);
        fory.writeRef(data.viewers);
    }

    @Override
    public VisionFramework read(ReadContext input) {
        VisionFramework vf = new VisionFramework();
        vf.placeWidth = input.readVarUint32();
        vf.placeHeight = input.readVarUint32();
        vf.rememberedColor = input.readInt32();
        vf.linePlaceMap = (char[][])input.readRef();
        vf.prunedPlaceMap = (char[][])input.readRef();
        vf.backgroundColors = (int[][])input.readRef();
        vf.previousLightLevels = (float[][])input.readRef();
        vf.blockage = (Region)input.readRef();
        vf.inView = (Region)input.readRef();
        vf.justHidden = (Region)input.readRef();
        vf.justSeen = (Region)input.readRef();
        vf.seen = (Region)input.readRef();
        vf.newlyVisible = (Region)input.readRef();
        vf.lighting = (LightingManager)input.readRef();
        vf.viewers = (CoordFloatOrderedMap)input.readRef();
        return vf;
    }
}
