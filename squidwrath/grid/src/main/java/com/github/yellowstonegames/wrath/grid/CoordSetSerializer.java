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

import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordSet;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;
import org.apache.fory.serializer.collection.CollectionSerializer;

/**
 * Fory {@link Serializer} for jdkgdxds {@link CoordSet}s.
 */
public class CoordSetSerializer extends CollectionSerializer<CoordSet> {

    public CoordSetSerializer(Fory fory) {
        super(fory, CoordSet.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordSet data) {
        final int len = data.size();
        output.writeVarUint32(len);
        for (Coord item : data) {
            output.writeInt16(item.x);
            output.writeInt16(item.y);
        }
    }

    @Override
    public CoordSet read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        CoordSet data = new CoordSet(len);
        for (int i = 0; i < len; i++) {
            data.add(Coord.get(input.readInt16(), input.readInt16()));
        }
        return data;
    }
}