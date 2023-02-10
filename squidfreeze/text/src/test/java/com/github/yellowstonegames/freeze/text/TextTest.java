/*
 * Copyright (c) 2022 See AUTHORS file.
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
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.text.Language;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class TextTest {
    @Test
    public void testLanguage() {
        Kryo kryo = new Kryo();
        kryo.register(Language.class, new LanguageSerializer());

        Language data = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(32);
        Output output = new Output(baos);
        kryo.writeObject(output, data);
        byte[] bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Language data2 = kryo.readObject(input, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.KOBOLD;
        output.flush();
        baos.reset();
        kryo.writeObject(output, data);
        bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Language data2 = kryo.readObject(input, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, Language.ARABIC_ROMANIZED, 5, Language.JAPANESE_ROMANIZED, 3).addModifiers(Language.Modifier.LISP);
        output.flush();
        baos.reset();
        kryo.writeObject(output, data);
        bytes = output.toBytes();
        try (Input input = new Input(bytes)) {
            Language data2 = kryo.readObject(input, Language.class);
            Assert.assertEquals(data, data2);
        }
    }
}
