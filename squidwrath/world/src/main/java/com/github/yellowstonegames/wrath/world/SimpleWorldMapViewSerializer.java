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
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Needs the type of the WorldMapGenerator used in {@link SimpleWorldMapView#getWorld()} to be registered.
 */
public class SimpleWorldMapViewSerializer extends Serializer<SimpleWorldMapView> {
    public SimpleWorldMapViewSerializer(Fory fory) {
        super(fory, SimpleWorldMapView.class);
    }

    @Override
    public void write(MemoryBuffer buffer, SimpleWorldMapView data) {
        fory.writeRef(buffer, data.getWorld());
        fory.writeJavaString(buffer, data.getBiomeMapper().stringSerialize());
        fory.writeRef(buffer, data.getColorMap());
        fory.writeRef(buffer, data.getColorMapOklab());
        fory.writeRef(buffer, data.biomeColorTable);
        fory.writeRef(buffer, data.biomeDarkColorTable);
    }

    @Override
    public SimpleWorldMapView read(MemoryBuffer buffer) {
        SimpleWorldMapView wmv = new SimpleWorldMapView((WorldMapGenerator) fory.readRef(buffer));
        wmv.setBiomeMapper(BiomeMapper.SimpleBiomeMapper.recreateFromString(fory.readJavaString(buffer)));
        wmv.setColorMap((int[][])fory.readRef(buffer));
        wmv.setColorMapOklab((int[][])fory.readRef(buffer));
        System.arraycopy(fory.readRef(buffer), 0, wmv.biomeColorTable, 0, 66);
        System.arraycopy(fory.readRef(buffer), 0, wmv.biomeDarkColorTable, 0, 66);

        return wmv;
    }
}
