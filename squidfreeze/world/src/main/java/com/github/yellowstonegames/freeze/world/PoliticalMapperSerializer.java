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
import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.kryo.jdkgdxds.IntObjectOrderedMapSerializer;
import com.github.tommyettinger.random.Deserializer;
import com.github.yellowstonegames.world.BiomeMapper;
import com.github.yellowstonegames.world.PoliticalMapper;
import com.github.yellowstonegames.world.PoliticalMapper;
import com.github.yellowstonegames.world.PoliticalMapper.Faction;
import com.github.yellowstonegames.world.WorldMapGenerator;

/**
 * Serializer for {@link PoliticalMapper}; needs the {@link WorldMapGenerator} implementation used by any given
 * PoliticalMapper to also be registered, as well as the {@link BiomeMapper} implementation used. Needs {@link Faction}
 * registered (likely with {@link FactionSerializer}), {@link com.github.tommyettinger.ds.IntObjectOrderedMap} (using
 * {@link com.github.tommyettinger.kryo.jdkgdxds.IntObjectOrderedMapSerializer}. Also needs {@code int[]},
 * {@code char[]}, {@code char[][]}, and {@code String[]} registered (with no serializer needed).
 */
public class PoliticalMapperSerializer extends Serializer<PoliticalMapper> {
    public PoliticalMapperSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final PoliticalMapper data) {
        output.writeString(data.rng.stringSerialize());
        kryo.writeObjectOrNull(output, data.atlas, IntObjectOrderedMap.class);
        output.writeString(data.name);
        kryo.writeObjectOrNull(output, data.politicalMap, char[][].class);
        kryo.writeObjectOrNull(output, data.zoomedMap, char[][].class);
        kryo.writeClassAndObject(output, data.wmg);
        kryo.writeClassAndObject(output, data.biomeMapper);
    }

    @Override
    public PoliticalMapper read(final Kryo kryo, final Input input, final Class<? extends PoliticalMapper> dataClass) {
        PoliticalMapper p = new PoliticalMapper(Deserializer.deserialize(input.readString()));
        p.atlas = kryo.readObjectOrNull(input, IntObjectOrderedMap.class);
        p.name = input.readString();
        p.politicalMap = kryo.readObjectOrNull(input, char[][].class);
        p.zoomedMap = kryo.readObjectOrNull(input, char[][].class);
        p.wmg = (WorldMapGenerator) kryo.readClassAndObject(input);
        p.biomeMapper = (BiomeMapper)kryo.readClassAndObject(input);
        return p;
    }
}
