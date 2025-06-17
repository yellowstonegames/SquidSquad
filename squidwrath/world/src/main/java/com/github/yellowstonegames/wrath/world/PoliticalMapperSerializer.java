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

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.random.Deserializer;
import com.github.tommyettinger.tantrum.jdkgdxds.IntObjectOrderedMapSerializer;
import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.PoliticalMapper;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;
import com.github.yellowstonegames.world.WorldMapGenerator;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * ALlows {@link PoliticalMapper} to be serialized.
 * Needs {@link Faction} to be registered with {@link FactionSerializer},
 * {@link IntObjectOrderedMap} to be registered with {@link IntObjectOrderedMapSerializer},
 * the used {@link WorldMapGenerator} type to be registered with one of the serializers here,
 * and the {@link BiomeMapper} to also be registered with a serializer here.
 */
public class PoliticalMapperSerializer extends Serializer<PoliticalMapper> {
    public PoliticalMapperSerializer(Fory fory) {
        super(fory, PoliticalMapper.class);
    }

    @Override
    public void write(MemoryBuffer buffer, PoliticalMapper data) {
        fory.writeJavaString(buffer, data.rng.stringSerialize());
        fory.writeRef(buffer, data.atlas);
        fory.writeJavaString(buffer, data.name);
        fory.writeRef(buffer, data.politicalMap);
        fory.writeRef(buffer, data.zoomedMap);
        fory.writeRef(buffer, data.wmg);
        fory.writeRef(buffer, data.biomeMapper);
    }

    @Override
    public PoliticalMapper read(MemoryBuffer buffer) {
        PoliticalMapper p = new PoliticalMapper(Deserializer.deserialize(fory.readJavaString(buffer)));
        p.atlas = (IntObjectOrderedMap<Faction>)fory.readRef(buffer);
        p.name = fory.readJavaString(buffer);
        p.politicalMap = (char[][])fory.readRef(buffer);
        p.zoomedMap = (char[][])fory.readRef(buffer);
        p.wmg = (WorldMapGenerator)fory.readRef(buffer);
        p.biomeMapper = (BiomeMapper)fory.readRef(buffer);
        return p;
    }
}
