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
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.store.core.JsonCore;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class JsonText {
    private JsonText() {
    }

    /**
     * Registers SquidWorld's classes with the given Json object, allowing it to read and write SquidWorld types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidWorld types.
     */
    public static void registerAll(@NonNull Json json) {
        registerLanguage(json);
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
}
