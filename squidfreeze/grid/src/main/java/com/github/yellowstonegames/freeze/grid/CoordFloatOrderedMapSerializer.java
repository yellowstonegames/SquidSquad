/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.freeze.grid;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordFloatOrderedMap;

/**
 * Serializer for {@link CoordFloatOrderedMap}; needs {@link CoordSerializer} registered for {@link Coord}.
 */
public class CoordFloatOrderedMapSerializer extends Serializer<CoordFloatOrderedMap> {

    public CoordFloatOrderedMapSerializer() {
        setAcceptsNull(true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CoordFloatOrderedMap data) {
        int length = data.size();
        output.writeInt(length, true);
        for (CoordFloatOrderedMap.EntryIterator<?> it = new CoordFloatOrderedMap.EntryIterator<>(data); it.hasNext(); ) {
            CoordFloatOrderedMap.Entry<?> ent = it.next();
            kryo.writeObject(output, ent.key);
            output.writeFloat(ent.value);
        }
    }

    @Override
    public CoordFloatOrderedMap read(final Kryo kryo, final Input input, final Class<? extends CoordFloatOrderedMap> dataClass) {
        int length = input.readInt(true);
        CoordFloatOrderedMap data = new CoordFloatOrderedMap(length);
        for (int i = 0; i < length; i++)
            data.put(kryo.readObject(input, Coord.class), input.readFloat());
        return data;
    }

    @SuppressWarnings({"rawtypes"})
    @Override
    public CoordFloatOrderedMap copy(Kryo kryo, CoordFloatOrderedMap original) {
        CoordFloatOrderedMap map = new CoordFloatOrderedMap(original.size(), original.getLoadFactor());
        for (CoordFloatOrderedMap.Entry ent : original) {
            map.put((Coord) ent.key, ent.value);
        }
        return map;
    }
}