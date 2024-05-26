/*
 * Copyright (c) 2024 See AUTHORS file.
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

package com.github.yellowstonegames.wrath.grid;

import com.github.yellowstonegames.grid.BasicHashNoise;
import com.github.yellowstonegames.grid.IPointHash;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

public class BasicHashNoiseSerializer extends Serializer<BasicHashNoise> {
    public BasicHashNoiseSerializer(Fury fury) {
        super(fury, BasicHashNoise.class);
    }

    @Override
    public void write(MemoryBuffer buffer, BasicHashNoise value) {
        buffer.writeInt32(value.seed);
        fury.writeRef(buffer, value.pointHash);
    }

    @Override
    public BasicHashNoise read(MemoryBuffer buffer) {
        return new BasicHashNoise(buffer.readInt32(), (IPointHash) fury.readRef(buffer));
    }
}
