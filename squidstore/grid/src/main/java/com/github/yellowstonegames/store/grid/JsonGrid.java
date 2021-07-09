package com.github.yellowstonegames.store.grid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.*;
import com.github.yellowstonegames.core.*;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.store.core.JsonCore;

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public final class JsonGrid {
    private JsonGrid() {
    }

    /**
     * Registers SquidGrid's classes with the given Json object, allowing it to read and write SquidGrid types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidGrid types.
     */
    public static void registerAll(@Nonnull Json json) {
        registerCoord(json);
    }

    /**
     * Registers Coord with the given Json object, so Coord can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoord(@Nonnull Json json) {
        json.setSerializer(Coord.class, new Json.Serializer<Coord>() {
            @Override
            public void write(Json json, Coord object, Class knownType) {
                json.writeArrayStart();
                json.writeValue(object.x);
                json.writeValue(object.y);
                json.writeArrayEnd();
            }

            @Override
            public Coord read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Coord.get(jsonData.getInt(0), jsonData.getInt(1));
            }
        });
    }

}
