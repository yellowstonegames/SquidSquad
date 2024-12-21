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
import com.github.yellowstonegames.grid.Point2Float;

/**
 * Serializer for {@link Point2Float}; doesn't need anything else registered.
 */
public class Point2FloatSerializer extends Serializer<Point2Float> {
    public Point2FloatSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Point2Float data) {
        output.writeFloat(data.x());
        output.writeFloat(data.y());
    }

    @Override
    public Point2Float read(final Kryo kryo, final Input input, final Class<? extends Point2Float> dataClass) {
        return new Point2Float(input.readFloat(), input.readFloat());
    }

    @Override
    public Point2Float copy(Kryo kryo, Point2Float original) {
        return new Point2Float(original);
    }
}
