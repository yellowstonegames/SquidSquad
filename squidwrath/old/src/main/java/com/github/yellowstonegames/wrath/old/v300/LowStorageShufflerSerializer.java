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

package com.github.yellowstonegames.wrath.old.v300;

import com.github.yellowstonegames.old.v300.LowStorageShuffler;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

public class LowStorageShufflerSerializer extends Serializer<LowStorageShuffler> {

    public LowStorageShufflerSerializer(Fury fury) {
        super(fury, LowStorageShuffler.class);
    }

    @Override
    public void write(MemoryBuffer buffer, final LowStorageShuffler data) {
        buffer.writeVarUint32(data.getBound());
        buffer.writeInt32(data.getKey0());
        buffer.writeInt32(data.getKey1());
        buffer.writeVarUint32(data.getIndex());
    }

    @Override
    public LowStorageShuffler read(MemoryBuffer buffer) {
        return new LowStorageShuffler(buffer.readVarUint32(), buffer.readInt32(), buffer.readInt32()).setIndex(buffer.readVarUint32());
    }

}
