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

package com.github.yellowstonegames.freeze.world;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.BlendedWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;

/**
 * Serializer for {@link BlendedWorldMapView}; needs the {@link WorldMapGenerator} implementation used by any given
 * BlendedWorldMapView to also be registered.
 */
public class BlendedWorldMapViewSerializer extends Serializer<BlendedWorldMapView> {
    public BlendedWorldMapViewSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final BlendedWorldMapView data) {
        kryo.writeClassAndObject(output, data.getWorld());
        output.writeString(data.getBiomeMapper().stringSerialize());
    }

    @Override
    public BlendedWorldMapView read(final Kryo kryo, final Input input, final Class<? extends BlendedWorldMapView> dataClass) {
        BlendedWorldMapView view = new BlendedWorldMapView((WorldMapGenerator) kryo.readClassAndObject(input));
        view.setBiomeMapper(BiomeMapper.BlendedBiomeMapper.recreateFromString(input.readString()));
        return view;
    }
}
