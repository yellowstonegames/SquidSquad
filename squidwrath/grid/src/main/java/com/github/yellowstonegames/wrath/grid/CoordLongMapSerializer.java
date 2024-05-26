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

import com.github.tommyettinger.ds.support.util.LongIterator;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordLongMap;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordLongMap}s.
 */
public class CoordLongMapSerializer extends Serializer<CoordLongMap> {

    public CoordLongMapSerializer(Fury fury) {
        super(fury, CoordLongMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordLongMap data) {
        output.writeVarUint32(data.size());
        for(Coord k : data.keySet()){
            output.writeInt16(k.x);
            output.writeInt16(k.y);
        }
        LongIterator it = data.values().iterator();
        while (it.hasNext()) {
            output.writeInt64(it.nextLong());
        }
    }

    @Override
    public CoordLongMap read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        Coord[] ks = new Coord[len];
        long[] vs = new long[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readInt16(), input.readInt16());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readInt64();
        }

        return new CoordLongMap(ks, vs);
    }
}