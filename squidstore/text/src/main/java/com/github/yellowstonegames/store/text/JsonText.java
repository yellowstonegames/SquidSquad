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
import com.github.yellowstonegames.text.Mnemonic;
import com.github.yellowstonegames.text.Translator;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class JsonText {
    private JsonText() {
    }

    /**
     * Registers SquidText's classes with the given Json object, allowing it to read and write SquidText types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidText types.
     */
    public static void registerAll(@NonNull Json json) {
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
    public static void registerLanguage(@NonNull Json json) {
        json.addClassTag("Lang", Language.class);
        JsonCore.registerPattern(json);
        json.setSerializer(Language.class, new Json.Serializer<Language>() {
            @Override
            public void write(Json json, Language object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public Language read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Language.stringDeserialize(jsonData.asString());
            }
        });
    }

    /**
     * Registers Language.SentenceForm with the given Json object, so Language.SentenceForm can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguageSentenceForm(@NonNull Json json) {
        json.addClassTag("LaSF", Language.SentenceForm.class);
        JsonSupport.registerEnhancedRandom(json);
        registerLanguage(json);
        json.setSerializer(Language.SentenceForm.class, new Json.Serializer<Language.SentenceForm>() {
            @Override
            public void write(Json json, Language.SentenceForm object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public Language.SentenceForm read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Language.SentenceForm.stringDeserialize(jsonData.asString());
            }
        });
    }

    /**
     * Registers Translator with the given Json object, so Translator can be written to and read from JSON.
     * Registers {@link ObjectObjectMap} with {@link JsonSupport#registerObjectObjectMap(Json)}, and registers
     * {@link Language} with {@link #registerLanguage(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTranslator(@NonNull Json json) {
        json.addClassTag("Tran", Translator.class);
        JsonSupport.registerObjectObjectMap(json);
        registerLanguage(json);
        json.setSerializer(Translator.class, new Json.Serializer<Translator>() {
            @Override
            public void write(Json json, Translator object, Class knownType) {
                json.writeArrayStart();
                json.writeValue(object.language, Language.class);
                json.writeValue((Object) DigitTools.hex(object.shift), String.class);
                json.writeValue(object.cacheLevel);
                json.writeValue(object.table, ObjectObjectMap.class);
                json.writeValue(object.reverse, ObjectObjectMap.class);
                json.writeArrayEnd();
            }

            @Override
            public Translator read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                JsonValue current = jsonData.child;
                Language language = json.readValue(Language.class, current);
                current = current.next;
                long shift = DigitTools.longFromHex(json.readValue(String.class, current));
                current = current.next;
                int cacheLevel = current.asInt();
                current = current.next;
                ObjectObjectMap<String, String> table = json.readValue(ObjectObjectMap.class, current);
                current = current.next;
                ObjectObjectMap<String, String> reverse = json.readValue(ObjectObjectMap.class, current);
                return new Translator(language, shift, cacheLevel, table, reverse);
            }
        });
    }

    /**
     * Registers Mnemonic with the given Json object, so Mnemonic can be written to and read from JSON.
     * Registers {@link NumberedSet} with {@link JsonSupport#registerNumberedSet(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerMnemonic(@NonNull Json json) {
        json.addClassTag("Mnem", Mnemonic.class);
        JsonSupport.registerNumberedSet(json);
        json.setSerializer(Mnemonic.class, new Json.Serializer<Mnemonic>() {
            @Override
            public void write(Json json, Mnemonic object, Class knownType) {
                json.writeObjectStart();
                json.writeType(Mnemonic.class);
                json.writeValue("i", object.items, NumberedSet.class);
                json.writeValue("a", object.allAdjectives, NumberedSet.class);
                json.writeValue("n", object.allNouns, NumberedSet.class);
                json.writeObjectEnd();
            }

            @Override
            public Mnemonic read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Mnemonic(json.readValue("i", NumberedSet.class, jsonData),
                        json.readValue("a", NumberedSet.class, jsonData),
                        json.readValue("n", NumberedSet.class, jsonData));
            }
        });
    }

}
