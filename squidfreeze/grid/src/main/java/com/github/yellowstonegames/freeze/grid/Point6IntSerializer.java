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
import com.github.yellowstonegames.grid.Point6Int;

/**
 * Serializer for {@link Point6Int}; doesn't need anything else registered.
 */
public class Point6IntSerializer extends Serializer<Point6Int> {
    public Point6IntSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Point6Int data) {
        output.writeInt(data.xi());
        output.writeInt(data.yi());
        output.writeInt(data.zi());
        output.writeInt(data.wi());
        output.writeInt(data.ui());
        output.writeInt(data.vi());
    }

    @Override
    public Point6Int read(final Kryo kryo, final Input input, final Class<? extends Point6Int> dataClass) {
        return new Point6Int(input.readInt(), input.readInt(), input.readInt(),
                input.readInt(), input.readInt(), input.readInt());
    }

    @Override
    public Point6Int copy(Kryo kryo, Point6Int original) {
        return new Point6Int(original);
    }
}
