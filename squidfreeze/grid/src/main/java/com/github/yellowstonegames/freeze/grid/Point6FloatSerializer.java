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
import com.github.yellowstonegames.grid.Point6Float;

/**
 * Serializer for {@link Point6Float}; doesn't need anything else registered.
 */
public class Point6FloatSerializer extends Serializer<Point6Float> {
    public Point6FloatSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Point6Float data) {
        output.writeFloat(data.x());
        output.writeFloat(data.y());
        output.writeFloat(data.z());
        output.writeFloat(data.w());
        output.writeFloat(data.u());
        output.writeFloat(data.v());
    }

    @Override
    public Point6Float read(final Kryo kryo, final Input input, final Class<? extends Point6Float> dataClass) {
        return new Point6Float(input.readFloat(), input.readFloat(), input.readFloat(),
                input.readFloat(), input.readFloat(), input.readFloat());
    }

    @Override
    public Point6Float copy(Kryo kryo, Point6Float original) {
        return new Point6Float(original);
    }
}
