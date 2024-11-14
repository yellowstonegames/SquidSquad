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
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link Region}, {@link CoordSerializer} for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque},
 * {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap}, {@link RadianceSerializer} for
 * {@link Radiance}, {@link LightingManagerRgbSerializer} for {@link LightingManagerRgb}, and
 * {@link LightSourceSerializer} for {@link LightSource} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class VisionFrameworkRgbSerializer extends Serializer<VisionFrameworkRgb> {
    public VisionFrameworkRgbSerializer(Fury fury) {
        super(fury, VisionFrameworkRgb.class);
    }

    @Override
    public void write(MemoryBuffer buffer, VisionFrameworkRgb data) {
        buffer.writeVarUint32(data.placeWidth);
        buffer.writeVarUint32(data.placeHeight);
        buffer.writeInt32(data.rememberedColor);
        fury.writeRef(buffer, data.linePlaceMap);
        fury.writeRef(buffer, data.prunedPlaceMap);
        fury.writeRef(buffer, data.backgroundColors);
        fury.writeRef(buffer, data.previousLightLevels);
        fury.writeRef(buffer, data.blockage);
        fury.writeRef(buffer, data.inView);
        fury.writeRef(buffer, data.justHidden);
        fury.writeRef(buffer, data.justSeen);
        fury.writeRef(buffer, data.seen);
        fury.writeRef(buffer, data.newlyVisible);
        fury.writeRef(buffer, data.lighting);
        fury.writeRef(buffer, data.viewers);
    }

    @Override
    public VisionFrameworkRgb read(MemoryBuffer input) {
        VisionFrameworkRgb vf = new VisionFrameworkRgb();
        vf.placeWidth = input.readVarUint32();
        vf.placeHeight = input.readVarUint32();
        vf.rememberedColor = input.readInt32();
        vf.linePlaceMap = (char[][])fury.readRef(input);
        vf.prunedPlaceMap = (char[][])fury.readRef(input);
        vf.backgroundColors = (int[][])fury.readRef(input);
        vf.previousLightLevels = (float[][])fury.readRef(input);
        vf.blockage = (Region)fury.readRef(input);
        vf.inView = (Region)fury.readRef(input);
        vf.justHidden = (Region)fury.readRef(input);
        vf.justSeen = (Region)fury.readRef(input);
        vf.seen = (Region)fury.readRef(input);
        vf.newlyVisible = (Region)fury.readRef(input);
        vf.lighting = (LightingManagerRgb)fury.readRef(input);
        vf.viewers = (CoordFloatOrderedMap)fury.readRef(input);
        return vf;
    }
}
