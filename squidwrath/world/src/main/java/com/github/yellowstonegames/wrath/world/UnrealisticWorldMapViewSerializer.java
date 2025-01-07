/*
 * Copyright (c) 2020-2025 See AUTHORS file.
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

package com.github.yellowstonegames.wrath.world;

import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.UnrealisticWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Needs the type of the WorldMapGenerator used in {@link UnrealisticWorldMapView#getWorld()} to be registered.
 */
public class UnrealisticWorldMapViewSerializer extends Serializer<UnrealisticWorldMapView> {
    public UnrealisticWorldMapViewSerializer(Fury fury) {
        super(fury, UnrealisticWorldMapView.class);
    }

    @Override
    public void write(MemoryBuffer buffer, UnrealisticWorldMapView data) {
        fury.writeRef(buffer, data.getWorld());
        fury.writeJavaString(buffer, data.getBiomeMapper().stringSerialize());
    }

    @Override
    public UnrealisticWorldMapView read(MemoryBuffer buffer) {
        UnrealisticWorldMapView wmv = new UnrealisticWorldMapView((WorldMapGenerator) fury.readRef(buffer));
        wmv.setBiomeMapper(BiomeMapper.UnrealisticBiomeMapper.recreateFromString(fury.readJavaString(buffer)));

        return wmv;
    }
}
