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

package com.github.yellowstonegames.store.world;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.world.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonTextTest {
    @Test
    public void testLanguage() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        Language lang, lang2;
        lang = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);
//        JsonCore.registerPattern(json);
        // If you really want to see what this looks like without registerLanguage... Uncomment the next line.
//        System.out.println(json.toJson(lang));
        JsonText.registerLanguage(json);
        String data = json.toJson(lang);
        System.out.println(data);
        lang2 = json.fromJson(Language.class, data);
        Assert.assertEquals(lang, lang2);
        System.out.println();
        lang = Language.KOBOLD;
        data = json.toJson(lang);
        System.out.println(data);
        lang2 = json.fromJson(Language.class, data);
        Assert.assertEquals(lang, lang2);
        System.out.println();
        lang = Language.randomLanguage(0x1337BEEFCAFEBABEL).mix(4, Language.ARABIC_ROMANIZED, 5, Language.JAPANESE_ROMANIZED, 3).addModifiers(Language.Modifier.LISP);
        data = json.toJson(lang);
        System.out.println(data);
        lang2 = json.fromJson(Language.class, data);
        Assert.assertEquals(lang, lang2);
        System.out.println();
    }
}
