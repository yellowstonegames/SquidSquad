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

package com.github.yellowstonegames.freeze.text;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Translator;

/**
 * Needs {@link LanguageSerializer} registered for Language and
 * {@link com.github.tommyettinger.kryo.jdkgdxds.ObjectObjectMapSerializer} registered for ObjectObjectMap.
 */
public class TranslatorSerializer extends Serializer<Translator> {
    public TranslatorSerializer() {
        setImmutable(true);
        setAcceptsNull(false);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final Translator data) {
        kryo.writeObject(output, data.language);
        output.writeLong(data.shift);
        output.writeInt(data.cacheLevel, true);
        kryo.writeObject(output, data.table);
        kryo.writeObject(output, data.reverse);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Translator read(final Kryo kryo, final Input input, final Class<? extends Translator> dataClass) {
        return new Translator(kryo.readObject(input, Language.class), input.readLong(), input.readInt(true),
                kryo.readObject(input, ObjectObjectMap.class), kryo.readObject(input, ObjectObjectMap.class));
    }
}
