package com.github.yellowstonegames.store.text;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.yellowstonegames.store.core.JsonCore;
import com.github.yellowstonegames.text.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

@SuppressWarnings("rawtypes")
public final class JsonText {
    private JsonText() {
    }

    /**
     * Registers SquidText's classes with the given Json object, allowing it to read and write SquidText types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidText types.
     */
    public static void registerAll(@Nonnull Json json) {
        registerLanguage(json);
        registerLanguageSentenceForm(json);
    }
    
    
    /**
     * Registers Language with the given Json object, so Language can be written to and read from JSON.
     * This is a simple wrapper around Language's built-in {@link Language#serializeToString()} and
     * {@link Language#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLanguage(@Nonnull Json json) {
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
    public static void registerLanguageSentenceForm(@Nonnull Json json) {
        JsonCore.registerEnhancedRandom(json);
        registerLanguage(json);
        json.setSerializer(Language.SentenceForm.class, new Json.Serializer<Language.SentenceForm>() {
            @Override
            public void write(Json json, Language.SentenceForm object, Class knownType) {
                json.writeArrayStart();
                json.writeValue(object.language, Language.class);
                json.writeValue(object.rng, EnhancedRandom.class);
                json.writeValue(object.minWords);
                json.writeValue(object.maxWords);
                json.writeValue(object.midPunctuation);
                json.writeValue(object.endPunctuation);
                json.writeValue(object.midPunctuationFrequency);
                json.writeValue(object.maxChars);
                json.writeArrayEnd();
            }

            @Override
            public Language.SentenceForm read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                JsonValue current = jsonData.child;
                Language language = json.readValue(Language.class, current);
                current = current.next;
                EnhancedRandom rng = json.readValue(EnhancedRandom.class, current);
                current = current.next;
                int minWords = current.asInt();
                current = current.next;
                int maxWords = current.asInt();
                current = current.next;
                String[] midPunctuation = current.asStringArray();
                current = current.next;
                String[] endPunctuation = current.asStringArray();
                current = current.next;
                double midPunctuationFrequency = current.asDouble();
                current = current.next;
                int maxChars = current.asInt();
                return new Language.SentenceForm(language, rng, minWords, maxWords, midPunctuation, endPunctuation, midPunctuationFrequency, maxChars);
            }
        });
    }

}
