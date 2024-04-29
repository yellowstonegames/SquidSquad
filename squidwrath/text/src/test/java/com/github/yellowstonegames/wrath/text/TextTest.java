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

import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Translator;
import io.fury.Fury;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

public class TextTest {
    @Test
    public void testLanguage() {
        Fury fury = Fury.builder().withLanguage(io.fury.config.Language.JAVA).build();
        fury.registerSerializer(Language.class, new LanguageSerializer(fury));

        Language data = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            Language data2 = fury.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.KOBOLD;
        bytes = fury.serializeJavaObject(data);
        {
            Language data2 = fury.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, Language.ARABIC_ROMANIZED, 5, Language.JAPANESE_ROMANIZED, 3).addModifiers(Language.Modifier.LISP);
        bytes = fury.serializeJavaObject(data);
        {
            Language data2 = fury.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLanguageSentenceForm() {
        Fury fury = Fury.builder().withLanguage(io.fury.config.Language.JAVA).build();
        fury.registerSerializer(Language.SentenceForm.class, new LanguageSentenceFormSerializer(fury));

        Language.SentenceForm data = new Language.SentenceForm(Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP), 1, 8), sf2;
        {
            byte[] bytes = fury.serializeJavaObject(data);
            Language.SentenceForm data2 = fury.deserializeJavaObject(bytes, Language.SentenceForm.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTranslator() {
        Fury fury = Fury.builder().withLanguage(io.fury.config.Language.JAVA).build();
        fury.registerSerializer(Language.class, new LanguageSerializer(fury));
        fury.registerSerializer(Translator.class, new TranslatorSerializer(fury));

        String sentence = "For you, I can translate; I will lower my steep rate; to something more affordable; since you are so adorable.";
        Translator data = new Translator(Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP), -1L);

        byte[] bytes = fury.serializeJavaObject(data);
        {
            Translator data2 = fury.deserializeJavaObject(bytes, Translator.class);
            Assert.assertEquals(data, data2);
            Assert.assertEquals(data.cipher(sentence), data2.cipher(sentence));
        }
    }
}
