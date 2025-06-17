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
import org.apache.fory.Fory;
import org.apache.fory.memory.MemoryBuffer;
import org.apache.fory.serializer.Serializer;

public class TranslatorSerializer extends Serializer<Translator> {
    public TranslatorSerializer(Fory fory) {
        super(fory, Translator.class);
    }

    @Override
    public void write(MemoryBuffer buffer, Translator data) {
        fory.writeJavaString(buffer, data.language.stringSerialize());
        buffer.writeInt64(data.shift);
        buffer.writeVarUint32(data.cacheLevel);
        buffer.writeVarUint32(data.table.size());
        for(String k : data.table.keySet()){
            fory.writeJavaString(buffer, k);
        }
        for(String v : data.table.values()){
            fory.writeJavaString(buffer, v);
        }

    }

    @Override
    public Translator read(MemoryBuffer buffer) {
        Language lang = Language.stringDeserialize(fory.readJavaString(buffer));
        long shift = buffer.readInt64();
        int cacheLevel = buffer.readVarUint32();
        int tableSize = buffer.readVarUint32();
        String[] ks = new String[tableSize];
        String[] vs = new String[tableSize];

        for (int i = 0; i < tableSize; i++) {
            ks[i] = fory.readJavaString(buffer);
        }
        for (int i = 0; i < tableSize; i++) {
            vs[i] = fory.readJavaString(buffer);
        }
        return new Translator(lang, shift, cacheLevel, new ObjectObjectMap<>(ks, vs), new ObjectObjectMap<>(vs, ks));
    }
}
