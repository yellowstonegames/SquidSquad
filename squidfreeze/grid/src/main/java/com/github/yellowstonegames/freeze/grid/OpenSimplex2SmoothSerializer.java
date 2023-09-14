/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
import com.github.yellowstonegames.grid.OpenSimplex2Smooth;

/**
 * Serializer for {@link OpenSimplex2Smooth}; doesn't need anything else registered.
 */
public class OpenSimplex2SmoothSerializer extends Serializer<OpenSimplex2Smooth> {
    public OpenSimplex2SmoothSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final OpenSimplex2Smooth data) {
        output.writeLong(data.getSeed());
    }

    @Override
    public OpenSimplex2Smooth read(final Kryo kryo, final Input input, final Class<? extends OpenSimplex2Smooth> dataClass) {
        return new OpenSimplex2Smooth(input.readLong());
    }

    @Override
    public OpenSimplex2Smooth copy(Kryo kryo, OpenSimplex2Smooth original) {
        return new OpenSimplex2Smooth(original.getSeed());
    }
}
