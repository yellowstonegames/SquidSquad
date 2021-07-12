package com.github.yellowstonegames.store.text;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.yellowstonegames.text.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonTextTest {
    @Test
    public void testLanguage() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonText.registerLanguage(json);
        Language lang, lang2;
        lang = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);
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
