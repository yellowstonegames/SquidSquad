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
import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.crux.PointPair;

/**
 * Needs the type of the points to be registered, such as with
 * {@link CoordSerializer} for {@link com.github.yellowstonegames.grid.Coord}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class PointPairSerializer extends com.esotericsoftware.kryo.Serializer<PointPair> {
    public PointPairSerializer() {
        setImmutable(false);
        setAcceptsNull(false);
    }

    @Override
    public void write (Kryo kryo, Output output, PointPair object) {
        Class valueClass = kryo.getGenerics().nextGenericClass();

        if (valueClass != null && kryo.isFinal(valueClass)) {
            Serializer serializer = kryo.getSerializer(valueClass);
            kryo.writeObject(output, object.a, serializer);
            kryo.writeObject(output, object.b, serializer);
        } else {
            kryo.writeClassAndObject(output, object.a);
            kryo.writeClassAndObject(output, object.b);
        }
        kryo.getGenerics().popGenericType();
    }

    @Override
    public PointPair read (Kryo kryo, Input input, Class<? extends PointPair> type) {
        Class valueClass = kryo.getGenerics().nextGenericClass();

        PointPair pair;

        if (valueClass != null && kryo.isFinal(valueClass)) {
            Serializer serializer = kryo.getSerializer(valueClass);
            pair = new PointPair((PointN) kryo.readObject(input, valueClass, serializer), (PointN) kryo.readObject(input, valueClass, serializer));
        } else
            pair = new PointPair((PointN)kryo.readClassAndObject(input), (PointN)kryo.readClassAndObject(input));

        kryo.getGenerics().popGenericType();
        return pair;
    }

    @Override
    public PointPair copy(Kryo kryo, PointPair original) {
        return new PointPair(original.a, original.b);
    }
}
