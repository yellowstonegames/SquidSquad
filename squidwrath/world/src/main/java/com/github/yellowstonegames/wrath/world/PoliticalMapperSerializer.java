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

package com.github.yellowstonegames.wrath.world;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.random.Deserializer;
import com.github.tommyettinger.tantrum.jdkgdxds.IntObjectOrderedMapSerializer;
import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.PoliticalMapper;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;
import com.github.yellowstonegames.world.WorldMapGenerator;
import org.apache.fory.Fory;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
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
        super(fory.getConfig(), PoliticalMapper.class);
    }

    @Override
    public void write(WriteContext fory, PoliticalMapper data) {
        fory.writeString(data.rng.stringSerialize());
        fory.writeRef(data.atlas);
        fory.writeString(data.name);
        fory.writeRef(data.politicalMap);
        fory.writeRef(data.zoomedMap);
        fory.writeRef(data.wmg);
        fory.writeRef(data.biomeMapper);
    }

    @Override
    public PoliticalMapper read(ReadContext fory) {
        PoliticalMapper p = new PoliticalMapper(Deserializer.deserialize(fory.readString()));
        p.atlas = (IntObjectOrderedMap<Faction>)fory.readRef();
        p.name = fory.readString();
        p.politicalMap = (char[][])fory.readRef();
        p.zoomedMap = (char[][])fory.readRef();
        p.wmg = (WorldMapGenerator)fory.readRef();
        p.biomeMapper = (BiomeMapper)fory.readRef();
        return p;
    }
}
