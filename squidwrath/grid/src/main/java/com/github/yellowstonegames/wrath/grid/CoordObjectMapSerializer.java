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
import com.github.yellowstonegames.grid.CoordObjectMap;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordObjectMap}s.
 */
@SuppressWarnings("rawtypes")
public class CoordObjectMapSerializer extends Serializer<CoordObjectMap> {

    public CoordObjectMapSerializer(Fury fury) {
        super(fury, CoordObjectMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordObjectMap data) {
        output.writeVarUint32(data.size());
        for(Object k : data.keySet()){
            output.writeInt16(((Coord)k).x);
            output.writeInt16(((Coord)k).y);
        }
        for(Object v : data.values()){
            fury.writeRef(output, v);
        }
        fury.writeRef(output, data.getDefaultValue());
    }

    @Override
    public CoordObjectMap<?> read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        Coord[] ks = new Coord[len];
        Object[] vs = new Object[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readInt16(), input.readInt16());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = fury.readRef(input);
        }

        CoordObjectMap data = new CoordObjectMap<>(ks, vs);
        data.setDefaultValue(fury.readRef(input));
        return data;
    }
}