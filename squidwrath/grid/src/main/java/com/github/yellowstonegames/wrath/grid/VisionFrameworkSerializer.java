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
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link Region}, {@link CoordSerializer} for {@link Coord}, {@link CoordObjectOrderedMapSerializer} for
 * {@link CoordObjectOrderedMap}, {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap},
 * {@link RadianceSerializer} for {@link Radiance}, and {@link LightingManagerSerializer} for
 * {@link LightingManager} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class VisionFrameworkSerializer extends Serializer<VisionFramework> {
    public VisionFrameworkSerializer(Fury fury) {
        super(fury, VisionFramework.class);
    }

    @Override
    public void write(MemoryBuffer buffer, VisionFramework data) {
        buffer.writePositiveVarInt(data.placeWidth);
        buffer.writePositiveVarInt(data.placeHeight);
        buffer.writeInt(data.rememberedColor);
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
    public VisionFramework read(MemoryBuffer input) {
        VisionFramework vf = new VisionFramework();
        vf.placeWidth = input.readPositiveVarInt();
        vf.placeHeight = input.readPositiveVarInt();
        vf.rememberedColor = input.readInt();
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
        vf.lighting = (LightingManager)fury.readRef(input);
        vf.viewers = (CoordFloatOrderedMap)fury.readRef(input);
        return vf;
    }
}
