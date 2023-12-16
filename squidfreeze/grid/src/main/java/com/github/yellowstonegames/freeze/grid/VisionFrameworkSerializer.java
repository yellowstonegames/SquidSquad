/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.grid.*;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@code char[]}, {@code char[][]},
 * {@link CoordSerializer} for {@link Coord}, {@link CoordObjectOrderedMapSerializer} for {@link CoordObjectOrderedMap},
 * {@link CoordFloatOrderedMapSerializer} for {@link CoordFloatOrderedMap}, {@link RegionSerializer} for
 * {@link Region}, {@link RadianceSerializer} for {@link Radiance}, and {@link LightingManagerSerializer} for
 * {@link LightingManager} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class VisionFrameworkSerializer extends Serializer<VisionFramework> {
    public VisionFrameworkSerializer() {
        super();
    }

    @Override
    public VisionFramework copy(Kryo kryo, VisionFramework original) {
        VisionFramework vf = new VisionFramework();
        vf.linePlaceMap = ArrayTools.copy(original.linePlaceMap);
        vf.prunedPlaceMap = ArrayTools.copy(original.prunedPlaceMap);
        vf.backgroundColors = ArrayTools.copy(original.backgroundColors);
        vf.previousLightLevels = ArrayTools.copy(original.previousLightLevels);
        vf.placeWidth = original.placeWidth;
        vf.placeHeight = original.placeHeight;
        vf.rememberedColor = original.rememberedColor;
        vf.blockage = original.blockage.copy();
        vf.inView = original.inView.copy();
        vf.justHidden = original.justHidden.copy();
        vf.justSeen = original.justSeen.copy();
        vf.seen = original.seen.copy();
        vf.newlyVisible = original.newlyVisible.copy();
        vf.lighting = kryo.copy(original.lighting);
        vf.viewers = kryo.copy(original.viewers);
        return vf;
    }

    @Override
    public void write(Kryo kryo, Output output, VisionFramework vf) {
        output.writeInt(vf.placeWidth, true);
        output.writeInt(vf.placeHeight, true);
        output.writeInt(vf.rememberedColor, false);
        kryo.writeObject(output, vf.linePlaceMap);
        kryo.writeObject(output, vf.prunedPlaceMap);
        kryo.writeObject(output, vf.backgroundColors);
        kryo.writeObject(output, vf.previousLightLevels);
        kryo.writeObject(output, vf.blockage);
        kryo.writeObject(output, vf.inView);
        kryo.writeObject(output, vf.justHidden);
        kryo.writeObject(output, vf.justSeen);
        kryo.writeObject(output, vf.seen);
        kryo.writeObject(output, vf.newlyVisible);
        kryo.writeObject(output, vf.lighting);
        kryo.writeObject(output, vf.viewers);
    }

    @Override
    public VisionFramework read(Kryo kryo, Input input, Class<? extends VisionFramework> type) {
        VisionFramework vf = new VisionFramework();
        vf.placeWidth = input.readInt(true);
        vf.placeHeight = input.readInt(true);
        vf.rememberedColor = input.readInt(false);
        vf.linePlaceMap = kryo.readObject(input, char[][].class);
        vf.prunedPlaceMap = kryo.readObject(input, char[][].class);
        vf.backgroundColors = kryo.readObject(input, int[][].class);
        vf.previousLightLevels = kryo.readObject(input, float[][].class);
        vf.blockage = kryo.readObject(input, Region.class);
        vf.inView = kryo.readObject(input, Region.class);
        vf.justHidden = kryo.readObject(input, Region.class);
        vf.justSeen = kryo.readObject(input, Region.class);
        vf.seen = kryo.readObject(input, Region.class);
        vf.newlyVisible = kryo.readObject(input, Region.class);
        vf.lighting = kryo.readObject(input, LightingManager.class);
        vf.viewers = kryo.readObject(input, CoordFloatOrderedMap.class);
        return vf;
    }
}
