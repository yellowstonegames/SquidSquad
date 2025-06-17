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
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

public class CrossHashCurlupSerializer extends Serializer<CrossHash.Curlup> {

    public CrossHashCurlupSerializer(Fory fory) {
        super(fory, CrossHash.Curlup.class);
    }

    @Override
    public void write(MemoryBuffer buffer, final CrossHash.Curlup data) {
        buffer.writeInt64(data.seed);
    }

    @Override
    public CrossHash.Curlup read(MemoryBuffer buffer) {
        CrossHash.Curlup data = new CrossHash.Curlup();
        data.seed = buffer.readInt64();
        return data;
    }

}
