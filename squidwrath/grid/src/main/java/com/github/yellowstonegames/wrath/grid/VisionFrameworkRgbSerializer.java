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
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link Region}, {@link CoordSerializer} for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque},
 * {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap}, {@link RadianceSerializer} for
 * {@link Radiance}, {@link LightingManagerRgbSerializer} for {@link LightingManagerRgb}, and
 * {@link LightSourceSerializer} for {@link LightSource} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class VisionFrameworkRgbSerializer extends Serializer<VisionFrameworkRgb> {
    public VisionFrameworkRgbSerializer(Fory fory) {
        super(fory, VisionFrameworkRgb.class);
    }

    @Override
    public void write(MemoryBuffer buffer, VisionFrameworkRgb data) {
        buffer.writeVarUint32(data.placeWidth);
        buffer.writeVarUint32(data.placeHeight);
        buffer.writeInt32(data.rememberedColor);
        fory.writeRef(buffer, data.linePlaceMap);
        fory.writeRef(buffer, data.prunedPlaceMap);
        fory.writeRef(buffer, data.backgroundColors);
        fory.writeRef(buffer, data.previousLightLevels);
        fory.writeRef(buffer, data.blockage);
        fory.writeRef(buffer, data.inView);
        fory.writeRef(buffer, data.justHidden);
        fory.writeRef(buffer, data.justSeen);
        fory.writeRef(buffer, data.seen);
        fory.writeRef(buffer, data.newlyVisible);
        fory.writeRef(buffer, data.lighting);
        fory.writeRef(buffer, data.viewers);
    }

    @Override
    public VisionFrameworkRgb read(MemoryBuffer input) {
        VisionFrameworkRgb vf = new VisionFrameworkRgb();
        vf.placeWidth = input.readVarUint32();
        vf.placeHeight = input.readVarUint32();
        vf.rememberedColor = input.readInt32();
        vf.linePlaceMap = (char[][])fory.readRef(input);
        vf.prunedPlaceMap = (char[][])fory.readRef(input);
        vf.backgroundColors = (int[][])fory.readRef(input);
        vf.previousLightLevels = (float[][])fory.readRef(input);
        vf.blockage = (Region)fory.readRef(input);
        vf.inView = (Region)fory.readRef(input);
        vf.justHidden = (Region)fory.readRef(input);
        vf.justSeen = (Region)fory.readRef(input);
        vf.seen = (Region)fory.readRef(input);
        vf.newlyVisible = (Region)fory.readRef(input);
        vf.lighting = (LightingManagerRgb)fory.readRef(input);
        vf.viewers = (CoordFloatOrderedMap)fory.readRef(input);
        return vf;
    }
}
