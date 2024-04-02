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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.grid.*;

/**
 * Needs {@code int[]}, {@code int[][]},  {@code float[]}, {@code float[][]}, {@link CoordSerializer} for {@link Coord},
 * {@link CoordObjectOrderedMapSerializer} for {@link CoordObjectOrderedMap}, {@link RegionSerializer} for
 * {@link Region}, and {@link RadianceSerializer} for {@link Radiance} to be registered.
 */
@SuppressWarnings({"unchecked"})
public class LightingManagerRgbSerializer extends Serializer<LightingManagerRgb> {
    public LightingManagerRgbSerializer() {
        super();
    }

    @Override
    public LightingManagerRgb copy(Kryo kryo, LightingManagerRgb original) {
        LightingManagerRgb lm = new LightingManagerRgb(ArrayTools.copy(original.resistances), original.backgroundColor, original.radiusStrategy,
                original.viewerRange);
        lm.noticeable.remake(original.noticeable);
        lm.lights.putAll(original.lights);
        lm.colorLighting = ArrayTools.copy(original.colorLighting);
        lm.lightingStrength = ArrayTools.copy(original.lightingStrength);
        lm.fovResult = ArrayTools.copy(original.fovResult);
        lm.lightFromFOV = ArrayTools.copy(original.lightFromFOV);
        lm.losResult = ArrayTools.copy(original.losResult);
        return lm;
    }

    @Override
    public void write(Kryo kryo, Output output, LightingManagerRgb object) {
        kryo.writeObject(output, object.resistances);
        output.writeInt(object.backgroundColor);
        output.writeVarInt(object.radiusStrategy.ordinal(), true);
        output.writeFloat(object.viewerRange);
        kryo.writeObject(output, object.noticeable);
        kryo.writeObject(output, object.lights);
        kryo.writeObject(output, object.colorLighting);
        kryo.writeObject(output, object.lightingStrength);
        kryo.writeObject(output, object.fovResult);
        kryo.writeObject(output, object.lightFromFOV);
        kryo.writeObject(output, object.losResult);
    }

    @Override
    public LightingManagerRgb read(Kryo kryo, Input input, Class<? extends LightingManagerRgb> type) {
        LightingManagerRgb lm = new LightingManagerRgb(kryo.readObject(input, float[][].class), input.readInt(), Radius.ALL[input.readVarInt(true)], input.readFloat());
        lm.noticeable = kryo.readObject(input, Region.class);
        lm.lights = kryo.readObject(input, CoordObjectOrderedMap.class);
        lm.colorLighting = kryo.readObject(input, int[][].class);
        lm.lightingStrength = kryo.readObject(input, float[][].class);
        lm.fovResult = kryo.readObject(input, float[][].class);
        lm.lightFromFOV = kryo.readObject(input, float[][].class);
        lm.losResult = kryo.readObject(input, float[][].class);
        return lm;
    }
}
