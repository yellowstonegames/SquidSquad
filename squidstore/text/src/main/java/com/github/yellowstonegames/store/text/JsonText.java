package com.github.yellowstonegames.store.text;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
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

}
