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

import com.github.yellowstonegames.grid.IGridIdentified;
import com.github.yellowstonegames.grid.SpatialMap;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;
import org.apache.fory.serializer.collection.CollectionSerializer;

/**  
 * Fory {@link Serializer} for jdkgdxds {@link SpatialMap}s. Needs the value type of the SpatialMap to be registered
 * (the class that implements IGridIdentified).
 */
@SuppressWarnings({"rawtypes", "unchecked"}) 
public class SpatialMapSerializer extends CollectionSerializer<SpatialMap> {

    public SpatialMapSerializer(Fory fory) {
        super(fory, SpatialMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final SpatialMap data) {
        final int len = data.size();
        output.writeVarUint32(len);
        for (Object item : data) {
            fory.writeRef(output, item);
        }
    }

    @Override
    public SpatialMap<? extends IGridIdentified> read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        SpatialMap data = new SpatialMap(len);
        for (int i = 0; i < len; i++) {
            data.add((IGridIdentified) fory.readRef(input));
        }
        return data;
    }
}