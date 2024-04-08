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
import com.github.yellowstonegames.grid.CoordIntMap;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordIntMap}s.
 */
public class CoordIntMapSerializer extends Serializer<CoordIntMap> {

    public CoordIntMapSerializer(Fury fury) {
        super(fury, CoordIntMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordIntMap data) {
        output.writePositiveVarInt(data.size());
        for(Coord k : data.keySet()){
            output.writeShort(k.x);
            output.writeShort(k.y);
        }
        IntIterator it = data.values().iterator();
        while (it.hasNext()) {
            output.writeInt(it.nextInt());
        }
    }

    @Override
    public CoordIntMap read(MemoryBuffer input) {
        final int len = input.readPositiveVarInt();
        Coord[] ks = new Coord[len];
        int[] vs = new int[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readShort(), input.readShort());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readInt();
        }

        return new CoordIntMap(ks, vs);
    }
}