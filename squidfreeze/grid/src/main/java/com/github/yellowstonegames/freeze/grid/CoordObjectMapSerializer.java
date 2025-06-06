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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import com.github.yellowstonegames.grid.CoordObjectMap;

/**
 * Serializer for {@link CoordObjectMap}; needs {@link CoordSerializer} registered for Coord, and a serializer
 * registered for whatever Object type the Map has for its values.
 */
public class CoordObjectMapSerializer extends MapSerializer<CoordObjectMap<?>> {
    public CoordObjectMapSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    protected void writeHeader(Kryo kryo, Output output, CoordObjectMap<?> map) {
        kryo.writeClassAndObject(output, map.getDefaultValue());
    }

    @Override
    protected CoordObjectMap<?> create(Kryo kryo, Input input, Class<? extends CoordObjectMap<?>> type, int size) {
        CoordObjectMap data = new CoordObjectMap<>(size);
        data.setDefaultValue(kryo.readClassAndObject(input));
        return data;

    }

    @Override
    protected CoordObjectMap<?> createCopy(Kryo kryo, CoordObjectMap<?> original) {
        CoordObjectMap data = new CoordObjectMap<>(original.size(), original.getLoadFactor());
        data.setDefaultValue(original.getDefaultValue());
        return data;

    }
}
