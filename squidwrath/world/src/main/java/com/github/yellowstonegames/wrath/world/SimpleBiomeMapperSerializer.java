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

import com.github.yellowstonegames.world.BiomeMapper.SimpleBiomeMapper;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

public class SimpleBiomeMapperSerializer extends Serializer<SimpleBiomeMapper> {
    public SimpleBiomeMapperSerializer(Fury fury) {
        super(fury, SimpleBiomeMapper.class);
    }

    @Override
    public void write(MemoryBuffer buffer, SimpleBiomeMapper data) {
        fury.writeJavaString(buffer, data.stringSerialize());
    }

    @Override
    public SimpleBiomeMapper read(MemoryBuffer buffer) {
        return SimpleBiomeMapper.recreateFromString(fury.readJavaString(buffer));
    }
}
