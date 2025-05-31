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
import com.github.yellowstonegames.world.SimpleWorldMapView;
import com.github.yellowstonegames.world.WorldMapGenerator;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Needs the type of the WorldMapGenerator used in {@link SimpleWorldMapView#getWorld()} to be registered.
 */
public class SimpleWorldMapViewSerializer extends Serializer<SimpleWorldMapView> {
    public SimpleWorldMapViewSerializer(Fury fury) {
        super(fury, SimpleWorldMapView.class);
    }

    @Override
    public void write(MemoryBuffer buffer, SimpleWorldMapView data) {
        fury.writeRef(buffer, data.getWorld());
        fury.writeJavaString(buffer, data.getBiomeMapper().stringSerialize());
        fury.writeRef(buffer, data.getColorMap());
        fury.writeRef(buffer, data.getColorMapOklab());
        fury.writeRef(buffer, data.biomeColorTable);
        fury.writeRef(buffer, data.biomeDarkColorTable);
    }

    @Override
    public SimpleWorldMapView read(MemoryBuffer buffer) {
        SimpleWorldMapView wmv = new SimpleWorldMapView((WorldMapGenerator) fury.readRef(buffer));
        wmv.setBiomeMapper(BiomeMapper.SimpleBiomeMapper.recreateFromString(fury.readJavaString(buffer)));
        wmv.setColorMap((int[][])fury.readRef(buffer));
        wmv.setColorMapOklab((int[][])fury.readRef(buffer));
        System.arraycopy((int[])fury.readRef(buffer), 0, wmv.biomeColorTable, 0, 66);
        System.arraycopy((int[])fury.readRef(buffer), 0, wmv.biomeDarkColorTable, 0, 66);

        return wmv;
    }
}
