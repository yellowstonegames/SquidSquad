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

/**
 * A serializer for everybody's favorite immutable pair of ints, {@link Coord}! Doesn't need anything else registered.
 */
public class CoordSerializer extends Serializer<Coord> {
    public CoordSerializer() {
        setImmutable(true);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Coord data) {
        output.writeShort(data.x);
        output.writeShort(data.y);
    }

    @Override
    public Coord read(final Kryo kryo, final Input input, final Class<? extends Coord> dataClass) {
        return Coord.get(input.readShort(), input.readShort());
    }
}
