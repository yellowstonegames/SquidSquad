/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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
import com.github.yellowstonegames.grid.CoordIntMap;

/**
 * Serializer for {@link CoordIntMap}; needs {@link CoordSerializer} registered for {@link Coord}.
 */
public class CoordIntMapSerializer extends Serializer<CoordIntMap> {

    public CoordIntMapSerializer() {
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CoordIntMap data) {
        int length = data.size();
        output.writeInt(length, true);
        for (CoordIntMap.EntryIterator<?> it = new CoordIntMap.EntryIterator<>(data); it.hasNext(); ) {
            CoordIntMap.Entry<?> ent = it.next();
            kryo.writeObject(output, ent.key);
            output.writeInt(ent.value);
        }
    }

    @Override
    public CoordIntMap read(final Kryo kryo, final Input input, final Class<? extends CoordIntMap> dataClass) {
        int length = input.readInt(true);
        CoordIntMap data = new CoordIntMap(length);
        for (int i = 0; i < length; i++)
            data.put(kryo.readObject(input, Coord.class), input.readInt());
        return data;
    }

    @Override
    public CoordIntMap copy(Kryo kryo, CoordIntMap original) {
        return new CoordIntMap(original);
    }
}