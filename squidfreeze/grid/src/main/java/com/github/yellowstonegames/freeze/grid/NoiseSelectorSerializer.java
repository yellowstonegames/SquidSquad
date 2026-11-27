/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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
import com.github.yellowstonegames.grid.NoiseSelector;

/**
 * Serializer for {@link NoiseSelector}; needs whatever INoise implementation this uses to be registered as well.
 */
public class NoiseSelectorSerializer extends Serializer<NoiseSelector> {
    public NoiseSelectorSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final NoiseSelector data) {
        kryo.writeClassAndObject(output, data.getHighNoise());
        kryo.writeClassAndObject(output, data.getLowNoise());
        kryo.writeClassAndObject(output, data.getSelector());
        output.writeFloat(data.getThreshold());
    }

    @Override
    public NoiseSelector read(final Kryo kryo, final Input input, final Class<? extends NoiseSelector> dataClass) {
        return new NoiseSelector((INoise) kryo.readClassAndObject(input),
            (INoise) kryo.readClassAndObject(input),
            (INoise) kryo.readClassAndObject(input),
            input.readFloat());
    }

    @Override
    public NoiseSelector copy(Kryo kryo, NoiseSelector original) {
        return new NoiseSelector(original);
    }
}
