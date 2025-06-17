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
import com.github.yellowstonegames.text.Mnemonic;
import com.github.yellowstonegames.text.Translator;
import org.apache.fory.Fory;
import org.apache.fory.logging.LoggerFactory;
import org.junit.Assert;
import org.junit.Test;

public class TextTest {
    @Test
    public void testLanguage() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Language.class, new LanguageSerializer(fory));

        Language data = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            Language data2 = fory.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.KOBOLD;
        bytes = fory.serializeJavaObject(data);
        {
            Language data2 = fory.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
        data = Language.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, Language.ARABIC_ROMANIZED, 5, Language.JAPANESE_ROMANIZED, 3).addModifiers(Language.Modifier.LISP);
        bytes = fory.serializeJavaObject(data);
        {
            Language data2 = fory.deserializeJavaObject(bytes, Language.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testLanguageSentenceForm() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Language.SentenceForm.class, new LanguageSentenceFormSerializer(fory));

        Language.SentenceForm data = new Language.SentenceForm(Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP), 1, 8);
        {
            byte[] bytes = fory.serializeJavaObject(data);
            Language.SentenceForm data2 = fory.deserializeJavaObject(bytes, Language.SentenceForm.class);
            Assert.assertEquals(data, data2);
        }
    }

    @Test
    public void testTranslator() {        
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Language.class, new LanguageSerializer(fory));
        fory.registerSerializer(Translator.class, new TranslatorSerializer(fory));

        String sentence = "For you, I can translate; I will lower my steep rate; to something more affordable; since you are so adorable.";
        Translator data = new Translator(Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP), -1L);

        byte[] bytes = fory.serializeJavaObject(data);
        {
            Translator data2 = fory.deserializeJavaObject(bytes, Translator.class);
            Assert.assertEquals(data, data2);
            Assert.assertEquals(data.cipher(sentence), data2.cipher(sentence));
        }
    }

    @Test
    public void testMnemonic() {
        LoggerFactory.disableLogging();
        Fory fory = Fory.builder().withLanguage(org.apache.fory.config.Language.JAVA).build();
        fory.registerSerializer(Mnemonic.class, new MnemonicSerializer(fory));

        Mnemonic data = new Mnemonic(1234567L);
        {
            byte[] bytes = fory.serializeJavaObject(data);
            Mnemonic data2 = fory.deserializeJavaObject(bytes, Mnemonic.class);
            Assert.assertEquals(data, data2);
        }
    }

}
