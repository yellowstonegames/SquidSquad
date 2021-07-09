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
import com.github.yellowstonegames.grid.CoordObjectMap;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.store.core.JsonCore;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

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
        registerRegion(json);
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

    /**
     * Registers Region with the given Json object, so Region can be written to and read from JSON.
     * This is a simple wrapper around Region's built-in {@link Region#serializeToString()} and
     * {@link Region#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRegion(@Nonnull Json json) {
        json.setSerializer(Region.class, new Json.Serializer<Region>() {
            @Override
            public void write(Json json, Region object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Region read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Region.deserializeFromString(jsonData.asString());
            }
        });
    }
    /**
     * Registers CoordObjectMap with the given Json object, so CoordObjectMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordObjectMap(@Nonnull Json json) {
        registerCoord(json);
        json.setSerializer(CoordObjectMap.class, new Json.Serializer<CoordObjectMap>() {
            @Override
            public void write(Json json, CoordObjectMap object, Class knownType) {
                Writer writer = json.getWriter();
                try {
                    writer.write('{');
                } catch (IOException ignored) {
                }
                Iterator<Map.Entry<Coord, ?>> es = new CoordObjectMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    Map.Entry<Coord, ?> e = es.next();
                    try {
                        String k = json.toJson(e.getKey());
                        json.setWriter(writer);
                        json.writeValue(k);
                        writer.write(':');
                        json.writeValue(e.getValue());
                        if (es.hasNext())
                            writer.write(',');
                    } catch (IOException ignored) {
                    }
                }
                try {
                    writer.write('}');
                } catch (IOException ignored) {
                }
            }

            @Override
            public CoordObjectMap<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordObjectMap<?> data = new CoordObjectMap<>(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(null, value));
                }
                return data;
            }
        });
    }


}
