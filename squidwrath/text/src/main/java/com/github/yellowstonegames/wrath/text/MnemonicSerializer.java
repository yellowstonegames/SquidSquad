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

import com.github.tommyettinger.ds.NumberedSet;
import com.github.yellowstonegames.text.Mnemonic;
import org.apache.fury.Fury;
import org.apache.fury.memory.MemoryBuffer;
import org.apache.fury.serializer.Serializer;

public class MnemonicSerializer extends Serializer<Mnemonic> {
    public MnemonicSerializer(Fury fury) {
        super(fury, Mnemonic.class);
    }

    @Override
    public void write(MemoryBuffer buffer, Mnemonic data) {
        buffer.writeVarUint32(data.items.size());
        for(String k : data.items.order()){
            fury.writeJavaString(buffer, k);
        }
        buffer.writeVarUint32(data.allAdjectives.size());
        for(String k : data.allAdjectives.order()){
            fury.writeJavaString(buffer, k);
        }
        buffer.writeVarUint32(data.allNouns.size());
        for(String k : data.allNouns.order()){
            fury.writeJavaString(buffer, k);
        }
    }

    @Override
    public Mnemonic read(MemoryBuffer buffer) {
        int itemSize = buffer.readVarUint32();
        NumberedSet<String> items = new NumberedSet<>(itemSize);
        for (int i = 0; i < itemSize; i++) {
            items.add(fury.readJavaString(buffer));
        }
        int adjSize = buffer.readVarUint32();
        NumberedSet<String> adj = new NumberedSet<>(adjSize);
        for (int i = 0; i < adjSize; i++) {
            adj.add(fury.readJavaString(buffer));
        }
        int nounSize = buffer.readVarUint32();
        NumberedSet<String> noun = new NumberedSet<>(nounSize);
        for (int i = 0; i < nounSize; i++) {
            noun.add(fury.readJavaString(buffer));
        }
        return new Mnemonic(items, adj, noun);
    }
}
