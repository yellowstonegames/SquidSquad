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

package com.github.yellowstonegames.freeze.old.v300;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.old.v300.XoshiroStarPhi32RNG;

/**
 * Serializer for {@link XoshiroStarPhi32RNG}; doesn't need anything else registered.
 */
public class XoshiroStarPhi32RNGSerializer extends Serializer<XoshiroStarPhi32RNG> {
    public XoshiroStarPhi32RNGSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final XoshiroStarPhi32RNG data) {
        output.writeInt(data.getStateA());
        output.writeInt(data.getStateB());
        output.writeInt(data.getStateC());
        output.writeInt(data.getStateD());
    }

    @Override
    public XoshiroStarPhi32RNG read(final Kryo kryo, final Input input, final Class<? extends XoshiroStarPhi32RNG> dataClass) {
        return new XoshiroStarPhi32RNG(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    @Override
    public XoshiroStarPhi32RNG copy(Kryo kryo, XoshiroStarPhi32RNG original) {
        return original.copy();
    }
}

