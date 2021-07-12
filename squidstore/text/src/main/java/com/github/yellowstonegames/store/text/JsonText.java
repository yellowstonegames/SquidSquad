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
    }
}
