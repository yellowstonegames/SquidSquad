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
import com.github.yellowstonegames.grid.PhantomNoise;

/**
 * Serializer for {@link PhantomNoise}; doesn't need anything else registered.
 */
public class PhantomNoiseSerializer extends Serializer<PhantomNoise> {
    public PhantomNoiseSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final PhantomNoise data) {
        output.writeLong(data.hasher.seed);
        output.writeInt(data.dim, true);
        output.writeFloat(data.sharpness);
    }

    @Override
    public PhantomNoise read(final Kryo kryo, final Input input, final Class<? extends PhantomNoise> dataClass) {
        return new PhantomNoise(input.readLong(), input.readInt(true),
                input.readFloat());
    }

    @Override
    public PhantomNoise copy(Kryo kryo, PhantomNoise original) {
        return new PhantomNoise(original.hasher.seed, original.dim, original.sharpness);
    }
}
