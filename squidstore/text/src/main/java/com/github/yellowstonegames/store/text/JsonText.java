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

package com.github.yellowstonegames.store.text;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.NumberedSet;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.store.core.JsonCore;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Language.SentenceForm;
import com.github.yellowstonegames.text.Mnemonic;
import com.github.yellowstonegames.text.Translator;
import org.jetbrains.annotations.NotNull;

public final class JsonText {
    private JsonText() {
    }

    /**
     * Registers SquidText's classes with the given Json object, allowing it to read and write SquidText types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidText types.
     */
    public static void registerAll(@NotNull Json json) {
        registerLanguage(json);
        registerLanguageSentenceForm(json);
        registerTranslator(json);
        registerMnemonic(json);
    }
    
    
    /**
     * Registers Language with the given Json object, so Language can be written to and read from JSON.
     * This is a simple wrapper around Language's built-in {@link Language#stringSerialize()} and
     * {@link Language#stringDeserialize(String)} methods. Out of an abundance of caution, this also
     * registers Pattern from RegExodus, because that class serializes into a much more compact form with
     * custom registration than without, and because there's a risk with unregistered Pattern
     * serialization (using Json) of hitting an infinite cycle. There's no such risk with it registered.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguage(@NotNull Json json) {
        json.addClassTag("Lang", Language.class);
        JsonCore.registerPattern(json);
        json.setSerializer(Language.class, new Json.Serializer<Language>() {
            @Override
            public void write(Json json, Language object, Class knownType) {
                json.writeObjectStart(Language.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public Language read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return Language.stringDeserialize(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers Language.SentenceForm with the given Json object, so Language.SentenceForm can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguageSentenceForm(@NotNull Json json) {
        json.addClassTag("LaSF", SentenceForm.class);
        JsonSupport.registerEnhancedRandom(json);
        registerLanguage(json);
        json.setSerializer(SentenceForm.class, new Json.Serializer<SentenceForm>() {
            @Override
            public void write(Json json, SentenceForm object, Class knownType) {
                json.writeObjectStart(SentenceForm.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public SentenceForm read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SentenceForm.stringDeserialize(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers Translator with the given Json object, so Translator can be written to and read from JSON.
     * Registers {@link ObjectObjectMap} with {@link JsonSupport#registerObjectObjectMap(Json)}, and registers
     * {@link Language} with {@link #registerLanguage(Json)}. Adds a class tag for both this class and String,
     * using "str" as the tag for String.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTranslator(@NotNull Json json) {
        json.addClassTag("Tran", Translator.class);
        json.addClassTag("str", String.class);
        JsonSupport.registerObjectObjectMap(json);
        registerLanguage(json);
        json.setSerializer(Translator.class, new Json.Serializer<Translator>() {
            @Override
            public void write(Json json, Translator object, Class knownType) {
                json.writeObjectStart(Translator.class, knownType);
                json.writeValue("lang", object.language, Language.class);
                json.writeValue("shift", DigitTools.hex(object.shift), String.class);
                json.writeValue("cache", object.cacheLevel);
                json.writeValue("tableK", object.table.keySet().toArray(new String[0]), String[].class, String.class);
                json.writeValue("tableV", object.table.values().toArray(new String[0]), String[].class, String.class);
                json.writeValue("reverseK", object.reverse.keySet().toArray(new String[0]), String[].class, String.class);
                json.writeValue("reverseV", object.reverse.values().toArray(new String[0]), String[].class, String.class);
                json.writeObjectEnd();
            }

            @Override
            public Translator read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                Language language = json.readValue("lang", Language.class, jsonData);
                long shift = DigitTools.longFromHex(jsonData.getString("shift", "0"));
                int cacheLevel = jsonData.getInt("cache");
                ObjectObjectMap<String, String> table = new ObjectObjectMap<>(
                        json.readValue("tableK", String[].class, String.class, jsonData),
                        json.readValue("tableV", String[].class, String.class, jsonData)
                );
                ObjectObjectMap<String, String> reverse = new ObjectObjectMap<>(
                        json.readValue("reverseK", String[].class, String.class, jsonData),
                        json.readValue("reverseV", String[].class, String.class, jsonData)
                );
                return new Translator(language, shift, cacheLevel, table, reverse);
            }
        });
    }

    /**
     * Registers Mnemonic with the given Json object, so Mnemonic can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerMnemonic(@NotNull Json json) {
        json.addClassTag("Mnem", Mnemonic.class);
        json.setSerializer(Mnemonic.class, new Json.Serializer<Mnemonic>() {
            @Override
            public void write(Json json, Mnemonic object, Class knownType) {
                json.writeObjectStart(Mnemonic.class, knownType);
                json.writeValue("i", object.items.toArray(new String[0]), String[].class, String.class);
                json.writeValue("a", object.allAdjectives.toArray(new String[0]), String[].class, String.class);
                json.writeValue("n", object.allNouns.toArray(new String[0]), String[].class, String.class);
                json.writeObjectEnd();
            }

            @Override
            public Mnemonic read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Mnemonic(new NumberedSet<>(json.readValue("i", String[].class, String.class, jsonData)),
                        new NumberedSet<>(json.readValue("a", String[].class, String.class, jsonData)),
                        new NumberedSet<>(json.readValue("n", String[].class, String.class, jsonData)));
            }
        });
    }

}
