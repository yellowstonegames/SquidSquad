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
    @Test
    public void testLanguageSentenceForm() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonText.registerLanguageSentenceForm(json);
        Language.SentenceForm sf = new Language.SentenceForm(Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP), 1, 8), sf2;
        String data = json.toJson(sf);
        System.out.println(data);
        sf2 = json.fromJson(Language.SentenceForm.class, data);
        System.out.println(sf.sentence());
        System.out.println(sf2.sentence());
        Assert.assertEquals(sf, sf2);
        System.out.println();
    }
    @Test
    public void testTranslator() {
        Json json = new Json(JsonWriter.OutputType.minimal);
        JsonText.registerTranslator(json);
        Language lang = Language.randomLanguage(1L).addModifiers(Language.Modifier.LISP);
        String sentence = "For you, I can translate; I will lower my steep rate; to something affordable; since you are adorable.";
        Translator t = new Translator(lang, -1L), t2;
        String cipher1 = t.cipher(sentence);
        String data = json.toJson(t);
        System.out.println(data);
        t2 = json.fromJson(Translator.class, data);
        System.out.println(cipher1);
        System.out.println(t2.cipher(sentence));
        Assert.assertEquals(t, t2);
        System.out.println();
    }

}
