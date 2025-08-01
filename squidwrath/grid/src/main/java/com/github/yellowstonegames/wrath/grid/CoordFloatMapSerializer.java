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
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

/**
 * Fory {@link Serializer} for jdkgdxds {@link CoordFloatMap}s.
 */
public class CoordFloatMapSerializer extends Serializer<CoordFloatMap> {

    public CoordFloatMapSerializer(Fory fory) {
        super(fory, CoordFloatMap.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordFloatMap data) {
        output.writeVarUint32(data.size());
        for(Coord k : data.keySet()){
            output.writeInt16(k.x);
            output.writeInt16(k.y);
        }
        FloatIterator it = data.values().iterator();
        while (it.hasNext()) {
            output.writeFloat32(it.nextFloat());
        }
        output.writeFloat32(data.getDefaultValue());
    }

    @Override
    public CoordFloatMap read(MemoryBuffer input) {
        final int len = input.readVarUint32();
        Coord[] ks = new Coord[len];
        float[] vs = new float[len];
        for (int i = 0; i < len; i++) {
            ks[i] = Coord.get(input.readInt16(), input.readInt16());
        }
        for (int i = 0; i < len; i++) {
            vs[i] = input.readFloat32();
        }

        CoordFloatMap data = new CoordFloatMap(ks, vs);
        data.setDefaultValue(input.readFloat32());
        return data;
    }
}