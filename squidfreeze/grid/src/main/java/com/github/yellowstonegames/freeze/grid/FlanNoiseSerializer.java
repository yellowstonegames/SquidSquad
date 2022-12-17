/*
 * Copyright (c) 2022 See AUTHORS file.
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
import com.github.yellowstonegames.grid.FlanNoise;

/**
 * Serializer for {@link FlanNoise}; doesn't need anything else registered.
 */
public class FlanNoiseSerializer extends Serializer<FlanNoise> {
    public FlanNoiseSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final FlanNoise data) {
        output.writeLong(data.seed);
        output.writeInt(data.dim, true);
        output.writeFloat(data.sharpness);
        output.writeInt(data.detail);
    }

    @Override
    public FlanNoise read(final Kryo kryo, final Input input, final Class<? extends FlanNoise> dataClass) {
        return new FlanNoise(input.readLong(), input.readInt(true), 0.625f / input.readFloat(), input.readInt());
    }

    @Override
    public FlanNoise copy(Kryo kryo, FlanNoise original) {
        return new FlanNoise(original.seed, original.dim, 0.625f / original.sharpness, original.detail);
    }
}
