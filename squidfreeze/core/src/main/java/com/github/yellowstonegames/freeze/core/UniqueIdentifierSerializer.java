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

package com.github.yellowstonegames.freeze.core;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.yellowstonegames.core.UniqueIdentifier;

public class UniqueIdentifierSerializer extends Serializer<UniqueIdentifier> {
    public UniqueIdentifierSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final UniqueIdentifier data) {
        output.writeInt(data.getA());
        output.writeInt(data.getB());
        output.writeInt(data.getC());
        output.writeInt(data.getD());
    }

    @Override
    public UniqueIdentifier read(final Kryo kryo, final Input input, final Class<? extends UniqueIdentifier> dataClass) {
        return new UniqueIdentifier(input.readInt(), input.readInt(), input.readInt(), input.readInt());
    }

    @Override
    public UniqueIdentifier copy(Kryo kryo, UniqueIdentifier original) {
        return new UniqueIdentifier(original.getA(), original.getB(), original.getC(), original.getD());
    }
}
