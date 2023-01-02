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

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.freeze.core.JsonCore;
import com.github.yellowstonegames.text.*;

import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("rawtypes")
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
    }
    
    
    /**
     * Registers Language with the given Json object, so Language can be written to and read from JSON.
     * This is a simple wrapper around Language's built-in {@link Language#serializeToString()} and
     * {@link Language#deserializeFromString(String)} methods. Out of an abundance of caution, this also
     * registers Pattern from RegExodus, because that class serializes into a much more compact form with
     * custom registration than without, and because there's a risk with unregistered Pattern
     * serialization (using Json) of hitting an infinite cycle. There's no such risk with it registered.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguage(@NonNull Json json) {
        JsonCore.registerPattern(json);
        json.setSerializer(Language.class, new Json.Serializer<Language>() {
            @Override
            public void write(Json json, Language object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Language read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Language.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers Language.SentenceForm with the given Json object, so Language.SentenceForm can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguageSentenceForm(@NonNull Json json) {
        JsonSupport.registerEnhancedRandom(json);
        registerLanguage(json);
        json.setSerializer(Language.SentenceForm.class, new Json.Serializer<Language.SentenceForm>() {
            @Override
            public void write(Json json, Language.SentenceForm object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Language.SentenceForm read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Language.SentenceForm.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers Translator with the given Json object, so Translator can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTranslator(@NonNull Json json) {
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

}
