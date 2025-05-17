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
import com.github.yellowstonegames.grid.CoordFloatMap;

/**
 * Serializer for {@link CoordFloatMap}; needs {@link CoordSerializer} registered for {@link Coord}.
 */
public class CoordFloatMapSerializer extends Serializer<CoordFloatMap> {

    public CoordFloatMapSerializer() {
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final CoordFloatMap data) {
        int length = data.size();
        output.writeInt(length, true);
        for (CoordFloatMap.EntryIterator<?> it = new CoordFloatMap.EntryIterator<>(data); it.hasNext(); ) {
            CoordFloatMap.Entry<?> ent = it.next();
            kryo.writeObject(output, ent.key);
            output.writeFloat(ent.value);
        }
        output.writeFloat(data.getDefaultValue());
    }

    @Override
    public CoordFloatMap read(final Kryo kryo, final Input input, final Class<? extends CoordFloatMap> dataClass) {
        int length = input.readInt(true);
        CoordFloatMap data = new CoordFloatMap(length);
        for (int i = 0; i < length; i++)
            data.put(kryo.readObject(input, Coord.class), input.readFloat());
        data.setDefaultValue(input.readFloat());
        return data;
    }

    @Override
    public CoordFloatMap copy(Kryo kryo, CoordFloatMap original) {
        return new CoordFloatMap(original);
    }
}