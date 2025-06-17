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
 *
 */

package com.github.yellowstonegames.wrath.grid;

import com.github.tommyettinger.ds.support.util.IntIterator;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordIntOrderedMap;
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Fory {@link Serializer} for jdkgdxds {@link CoordIntOrderedMap}s.
 */
public class CoordIntOrderedMapSerializer extends Serializer<CoordIntOrderedMap> {

    public CoordIntOrderedMapSerializer(Fory fory) {
        super(fory, CoordIntOrderedMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordIntOrderedMap data) {
        output.writeVarUint32(data.size());
        for(Coord k : data.order()){
            output.writeInt16(k.x);
            output.writeInt16(k.y);
        }
        IntIterator it = data.values().iterator();
        while (it.hasNext()) {
            output.writeInt32(it.nextInt());
        }
        output.writeInt32(data.getDefaultValue());
    }

    @Override
    public CoordIntOrderedMap read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        Coord[] ks = new Coord[len];
        int[] vs = new int[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readInt16(), input.readInt16());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readInt32();
        }

        CoordIntOrderedMap data = new CoordIntOrderedMap(ks, vs);
        data.setDefaultValue(input.readInt32());
        return data;
    }
}