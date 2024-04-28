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
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for jdkgdxds {@link CoordSet}s.
 */
public class CoordSetSerializer extends Serializer<CoordSet> {

    public CoordSetSerializer(Fury fury) {
        super(fury, CoordSet.class);
    }

    @Override
    public void write(final MemoryBuffer output, final CoordSet data) {
        final int len = data.size();
        output.writePositiveVarInt(len);
        for (Coord item : data) {
            output.writeShort(item.x);
            output.writeShort(item.y);
        }
    }

    @Override
    public CoordSet read(MemoryBuffer input) {
        final int len = input.readPositiveVarInt();
        CoordSet data = new CoordSet(len);
        for (int i = 0; i < len; i++) {
            data.add(Coord.get(input.readShort(), input.readShort()));
        }
        return data;
    }
}