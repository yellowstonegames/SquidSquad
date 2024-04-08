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

import com.github.tommyettinger.ds.support.util.FloatIterator;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordFloatMap;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordFloatMap}s.
 */
@SuppressWarnings("rawtypes")
public class CoordFloatMapSerializer extends Serializer<CoordFloatMap> {

    public CoordFloatMapSerializer(Fury fury) {
        super(fury, CoordFloatMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordFloatMap data) {
        output.writePositiveVarInt(data.size());
        for(Object k : data.keySet()){
            output.writeShort(((Coord)k).x);
            output.writeShort(((Coord)k).y);
        }
        FloatIterator it = data.values().iterator();
        for(; it.hasNext(); ){
            output.writeFloat(it.nextFloat());
        }
    }

    @Override
    public CoordFloatMap read(MemoryBuffer input) {
        final int len = input.readPositiveVarInt();
        Coord[] ks = new Coord[len];
        float[] vs = new float[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readShort(), input.readShort());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readFloat();
        }

        return new CoordFloatMap(ks, vs);
    }
}