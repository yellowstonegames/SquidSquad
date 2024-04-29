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

package com.github.yellowstonegames.wrath.text;

import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Translator;
import io.fury.Fury;
import io.fury.memory.MemoryBuffer;
import io.fury.serializer.Serializer;

public class TranslatorSerializer extends Serializer<Translator> {
    public TranslatorSerializer(Fury fury) {
        super(fury, Translator.class);
    }

    @Override
    public void write(MemoryBuffer buffer, Translator data) {
        fury.writeString(buffer, data.language.stringSerialize());
        buffer.writeLong(data.shift);
        buffer.writePositiveVarInt(data.cacheLevel);
        buffer.writePositiveVarInt(data.table.size());
        for(String k : data.table.keySet()){
            fury.writeString(buffer, k);
        }
        for(String v : data.table.values()){
            fury.writeString(buffer, v);
        }

    }

    @Override
    public Translator read(MemoryBuffer buffer) {
        Language lang = Language.stringDeserialize(fury.readJavaString(buffer));
        long shift = buffer.readLong();
        int cacheLevel = buffer.readPositiveVarInt();
        int tableSize = buffer.readPositiveVarInt();
        String[] ks = new String[tableSize];
        String[] vs = new String[tableSize];

        for (int i = 0; i < tableSize; i++) {
            ks[i] = fury.readString(buffer);
        }
        for (int i = 0; i < tableSize; i++) {
            vs[i] = fury.readString(buffer);
        }
        return new Translator(lang, shift, cacheLevel, new ObjectObjectMap<>(ks, vs), new ObjectObjectMap<>(vs, ks));
    }
}
