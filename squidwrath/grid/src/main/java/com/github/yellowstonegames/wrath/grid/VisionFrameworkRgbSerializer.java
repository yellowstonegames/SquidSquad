/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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
import org.apache.fory.serializer.Serializer;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link Region}, {@link CoordSerializer} for {@link Coord}, {@link ObjectDequeSerializer} for {@link ObjectDeque},
 * {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap}, {@link RadianceSerializer} for
 * {@link Radiance}, {@link LightingManagerRgbSerializer} for {@link LightingManagerRgb}, and
 * {@link LightSourceSerializer} for {@link LightSource} to be registered.
 */
public class VisionFrameworkRgbSerializer extends Serializer<VisionFrameworkRgb> {
    public VisionFrameworkRgbSerializer(Fory fory) {
        super(fory.getConfig(), VisionFrameworkRgb.class);
    }

    @Override
    public void write(WriteContext fory, VisionFrameworkRgb data) {
        fory.writeVarUInt32(data.placeWidth);
        fory.writeVarUInt32(data.placeHeight);
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
    public VisionFrameworkRgb read(ReadContext input) {
        VisionFrameworkRgb vf = new VisionFrameworkRgb();
        vf.placeWidth = input.readVarUInt32();
        vf.placeHeight = input.readVarUInt32();
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
        vf.lighting = (LightingManagerRgb)input.readRef();
        vf.viewers = (CoordFloatOrderedMap)input.readRef();
        return vf;
    }
}
