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
import org.apache.fory.context.ReadContext;
import org.apache.fory.context.WriteContext;
import org.apache.fory.serializer.Serializer;

public class TranslatorSerializer extends Serializer<Translator> {
    public TranslatorSerializer(Fory fory) {
        super(fory.getConfig(), Translator.class);
    }

    @Override
    public void write(WriteContext output, Translator data) {
        output.writeString(data.language.stringSerialize());
        output.writeInt64(data.shift);
        output.writeVarUInt32(data.cacheLevel);
        output.writeVarUInt32(data.table.size());
        for(String k : data.table.keySet()){
            output.writeString(k);
        }
        for(String v : data.table.values()){
            output.writeString(v);
        }

    }

    @Override
    public Translator read(ReadContext input) {
        Language lang = Language.stringDeserialize(input.readString());
        long shift = input.readInt64();
        int cacheLevel = input.readVarUInt32();
        int tableSize = input.readVarUInt32();
        String[] ks = new String[tableSize];
        String[] vs = new String[tableSize];

        for (int i = 0; i < tableSize; i++) {
            ks[i] = input.readString();
        }
        for (int i = 0; i < tableSize; i++) {
            vs[i] = input.readString();
        }
        return new Translator(lang, shift, cacheLevel, new ObjectObjectMap<>(ks, vs), new ObjectObjectMap<>(vs, ks));
    }
}
