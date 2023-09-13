/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordLongMap;

/**
 * Serializer for {@link CoordLongMap}; needs {@link CoordSerializer} registered for {@link Coord}.
 */
public class CoordLongMapSerializer extends Serializer<CoordLongMap> {

    public CoordLongMapSerializer() {
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CoordLongMap data) {
        int length = data.size();
        output.writeInt(length, true);
        for (CoordLongMap.EntryIterator<?> it = new CoordLongMap.EntryIterator<>(data); it.hasNext(); ) {
            CoordLongMap.Entry<?> ent = it.next();
            kryo.writeObject(output, ent.key);
            output.writeLong(ent.value);
        }
    }

    @Override
    public CoordLongMap read(final Kryo kryo, final Input input, final Class<? extends CoordLongMap> dataClass) {
        int length = input.readInt(true);
        CoordLongMap data = new CoordLongMap(length);
        for (int i = 0; i < length; i++)
            data.put(kryo.readObject(input, Coord.class), input.readLong());
        return data;
    }

    @Override
    public CoordLongMap copy(Kryo kryo, CoordLongMap original) {
        return new CoordLongMap(original);
    }
}