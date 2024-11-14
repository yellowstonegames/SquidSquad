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
import com.github.yellowstonegames.grid.LightSource;
import com.github.yellowstonegames.grid.Radiance;

/**
 * Serializer for {@link LightSource}; needs {@link CoordSerializer} registered for {@link Coord} and
 * {@link RadianceSerializer} registered for {@link Radiance}.
 */
public class LightSourceSerializer extends Serializer<LightSource> {
    public LightSourceSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final LightSource data) {
        kryo.writeObject(output, data.position);
        kryo.writeObject(output, data.radiance);
        output.writeFloat(data.span);
        output.writeFloat(data.direction);
    }

    @Override
    public LightSource read(final Kryo kryo, final Input input, final Class<? extends LightSource> dataClass) {
        return new LightSource(kryo.readObject(input, Coord.class), kryo.readObject(input, Radiance.class),
                input.readFloat(), input.readFloat());
    }

    @Override
    public LightSource copy(Kryo kryo, LightSource original) {
        return new LightSource(original.position, new Radiance(original.radiance), original.span, original.direction);
    }
}
