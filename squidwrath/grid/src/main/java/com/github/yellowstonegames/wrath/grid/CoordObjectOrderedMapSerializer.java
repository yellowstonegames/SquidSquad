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
import org.apache.fory.Fory;
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;
import org.apache.fory.serializer.collection.MapSerializer;

/**
 * Fory {@link Serializer} for jdkgdxds {@link CoordObjectOrderedMap}s.
 */
@SuppressWarnings("rawtypes")
public class CoordObjectOrderedMapSerializer extends MapSerializer<CoordObjectOrderedMap> {

    public CoordObjectOrderedMapSerializer(Fory fory) {
        super(fory.getTypeResolver(), CoordObjectOrderedMap.class);
    }

    @Override
    public void write(final WriteContext output, final CoordObjectOrderedMap data) {
        output.writeVarUInt32(data.size());
        for(Object k : data.order()){
            output.writeInt16(((Coord)k).x);
            output.writeInt16(((Coord)k).y);
        }
        for(Object v : data.values()){
            output.writeRef(v);
        }
        output.writeRef(data.getDefaultValue());
    }

    @Override
    public CoordObjectOrderedMap<?> read(ReadContext input) {
        final int len = input.readVarUInt32();
        Coord[] ks = new Coord[len];
        Object[] vs = new Object[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readInt16(), input.readInt16());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readRef();
        }

        CoordObjectOrderedMap data = new CoordObjectOrderedMap<>(ks, vs);
        data.setDefaultValue(input.readRef());
        return data;

    }
}