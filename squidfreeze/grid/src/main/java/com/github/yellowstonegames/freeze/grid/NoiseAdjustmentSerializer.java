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
import com.github.tommyettinger.digital.Interpolations;
import com.github.yellowstonegames.grid.INoise;
import com.github.yellowstonegames.grid.NoiseAdjustment;

/**
 * Serializer for {@link NoiseAdjustment}; needs whatever INoise implementation this uses to be registered as well.
 */
public class NoiseAdjustmentSerializer extends Serializer<NoiseAdjustment> {
    public NoiseAdjustmentSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final NoiseAdjustment data) {
        kryo.writeClassAndObject(output, data.getWrapped());
        output.writeString(data.getAdjustment().getTag());
    }

    @Override
    public NoiseAdjustment read(final Kryo kryo, final Input input, final Class<? extends NoiseAdjustment> dataClass) {
        return new NoiseAdjustment((INoise) kryo.readClassAndObject(input), Interpolations.get(input.readString()));
    }

    @Override
    public NoiseAdjustment copy(Kryo kryo, NoiseAdjustment original) {
        return new NoiseAdjustment(original);
    }
}
