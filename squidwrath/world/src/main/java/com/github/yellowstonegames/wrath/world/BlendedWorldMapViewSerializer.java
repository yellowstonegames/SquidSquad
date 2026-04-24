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
import com.github.yellowstonegames.world.BlendedWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;
import org.apache.fory.Fory;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

/**
 * Needs the type of the WorldMapGenerator used in {@link BlendedWorldMapView#getWorld()} to be registered.
 */
public class BlendedWorldMapViewSerializer extends Serializer<BlendedWorldMapView> {
    public BlendedWorldMapViewSerializer(Fory fory) {
        super(fory.getConfig(), BlendedWorldMapView.class);
    }

    @Override
    public void write(WriteContext fory, BlendedWorldMapView data) {
        fory.writeRef(data.getWorld());
        fory.writeString(data.getBiomeMapper().stringSerialize());
    }

    @Override
    public BlendedWorldMapView read(ReadContext fory) {
        BlendedWorldMapView wmv = new BlendedWorldMapView((WorldMapGenerator) fory.readRef());
        wmv.setBiomeMapper(BiomeMapper.BlendedBiomeMapper.recreateFromString(fory.readString()));

        return wmv;
    }
}
