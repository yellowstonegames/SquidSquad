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

import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordObjectOrderedMap}s.
 */
@SuppressWarnings("rawtypes")
public class CoordObjectOrderedMapSerializer extends Serializer<CoordObjectOrderedMap> {

    public CoordObjectOrderedMapSerializer(Fury fury) {
        super(fury, CoordObjectOrderedMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordObjectOrderedMap data) {
        output.writePositiveVarInt(data.size());
        for(Object k : data.order()){
            output.writeShort(((Coord)k).x);
            output.writeShort(((Coord)k).y);
        }
        for(Object v : data.values()){
            fury.writeRef(output, v);
        }
    }

    @Override
    public CoordObjectOrderedMap<?> read(MemoryBuffer input) {
        final int len = input.readPositiveVarInt();
        Coord[] ks = new Coord[len];
        Object[] vs = new Object[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readShort(), input.readShort());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = fury.readRef(input);
        }

        return new CoordObjectOrderedMap<>(ks, vs);
    }
}