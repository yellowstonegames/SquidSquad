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
import com.github.yellowstonegames.world.UnrealisticWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;

/**
 * Serializer for {@link UnrealisticWorldMapView}; needs the {@link WorldMapGenerator} implementation used by any given
 * UnrealisticWorldMapView to also be registered.
 */
public class UnrealisticWorldMapViewSerializer extends Serializer<UnrealisticWorldMapView> {
    public UnrealisticWorldMapViewSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UnrealisticWorldMapView data) {
        kryo.writeClassAndObject(output, data.getWorld());
        output.writeString(data.getBiomeMapper().stringSerialize());
    }

    @Override
    public UnrealisticWorldMapView read(final Kryo kryo, final Input input, final Class<? extends UnrealisticWorldMapView> dataClass) {
        UnrealisticWorldMapView view = new UnrealisticWorldMapView((WorldMapGenerator) kryo.readClassAndObject(input));
        view.setBiomeMapper(BiomeMapper.UnrealisticBiomeMapper.recreateFromString(input.readString()));
        return view;
    }
}
