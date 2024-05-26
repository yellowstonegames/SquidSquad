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

package com.github.yellowstonegames.wrath.grid;

import com.github.yellowstonegames.grid.Coord;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

/**
 * Fury {@link Serializer} for digital {@link Coord}s.
 * You should expand the Coord pool first, if possible, if the maximum values for a Coord's x and y are known.
 * You can use {@link Coord#expandPoolTo(int, int)} to do this.
 */
public class CoordSerializer extends Serializer<Coord> {

    public CoordSerializer(Fury fury) {
        super(fury, Coord.class);
    }

    @Override
    public void write(MemoryBuffer buffer, final Coord data) {
        buffer.writeInt16(data.x);
        buffer.writeInt16(data.y);
    }

    @Override
    public Coord read(MemoryBuffer buffer) {
        return Coord.get(buffer.readInt16(), buffer.readInt16());
    }
}