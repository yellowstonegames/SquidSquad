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

import com.github.yellowstonegames.old.v300.CrossHash;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

public class CrossHashMistSerializer extends Serializer<CrossHash.Mist> {

    public CrossHashMistSerializer(Fury fury) {
        super(fury, CrossHash.Mist.class);
    }

    @Override
    public void write(MemoryBuffer buffer, final CrossHash.Mist data) {
        buffer.writeInt64(data.l1);
        buffer.writeInt64(data.l2);
    }

    @Override
    public CrossHash.Mist read(MemoryBuffer buffer) {
        CrossHash.Mist data = new CrossHash.Mist();
        data.l1 = buffer.readInt64();
        data.l2 = buffer.readInt64();
        return data;
    }

}
