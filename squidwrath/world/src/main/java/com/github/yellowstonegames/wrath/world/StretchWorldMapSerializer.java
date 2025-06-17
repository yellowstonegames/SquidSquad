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

import com.github.yellowstonegames.world.StretchWorldMap;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

public class StretchWorldMapSerializer extends Serializer<StretchWorldMap> {
    public StretchWorldMapSerializer(Fory fory) {
        super(fory, StretchWorldMap.class);
    }

    @Override
    public void write(MemoryBuffer buffer, StretchWorldMap data) {
        fory.writeJavaString(buffer, data.stringSerialize());
    }

    @Override
    public StretchWorldMap read(MemoryBuffer buffer) {
        return StretchWorldMap.recreateFromString(fory.readJavaString(buffer));
    }
}
