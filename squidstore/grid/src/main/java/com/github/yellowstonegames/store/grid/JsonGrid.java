/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.store.grid;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.github.tommyettinger.ds.ObjectFloatMap;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.store.core.JsonCore;

import org.checkerframework.checker.nullness.qual.NonNull;
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
    public static void registerAll(@NonNull Json json) {
        registerCoord(json);
        registerRegion(json);
        registerCoordSet(json);
        registerCoordOrderedSet(json);
        registerCoordObjectMap(json);
        registerCoordObjectOrderedMap(json);
        registerCoordFloatMap(json);
        registerCoordFloatOrderedMap(json);
        registerSpatialMap(json);
        registerRadiance(json);
        registerLightingManager(json);
        registerNoise(json);
        registerPhantomNoise(json);
        registerTaffyNoise(json);
        registerFlanNoise(json);
        registerCyclicNoise(json);
        registerSorbetNoise(json);
    }

    /**
     * Registers Coord with the given Json object, so Coord can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoord(@NonNull Json json) {
        json.addClassTag("Coor", Coord.class);
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
    public static void registerRegion(@NonNull Json json) {
        json.addClassTag("Regi", Region.class);
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
    public static void registerCoordObjectMap(@NonNull Json json) {
        json.addClassTag("CoM", CoordObjectMap.class);
        registerCoord(json);
        json.setSerializer(CoordObjectMap.class, new Json.Serializer<CoordObjectMap>() {
            @Override
            public void write(Json json, CoordObjectMap object, Class knownType) {
                JsonWriter writer = json.getWriter();
                try {
                    writer.object();
                } catch (IOException ignored) {
                }
                Iterator<Map.Entry<Coord, ?>> es = new CoordObjectMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    Map.Entry<Coord, ?> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), null);
                }
                try {
                    writer.pop();
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

    /**
     * Registers CoordObjectOrderedMap with the given Json object, so CoordObjectOrderedMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordObjectOrderedMap(@NonNull Json json) {
        json.addClassTag("CoOM", CoordObjectOrderedMap.class);
        registerCoord(json);
        json.setSerializer(CoordObjectOrderedMap.class, new Json.Serializer<CoordObjectOrderedMap>() {
            @Override
            public void write(Json json, CoordObjectOrderedMap object, Class knownType) {
                JsonWriter writer = json.getWriter();
                try {
                    writer.object();
                } catch (IOException ignored) {
                }
                Iterator<Map.Entry<Coord, ?>> es = new CoordObjectOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    Map.Entry<Coord, ?> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), null);
                }
                try {
                    writer.pop();
                } catch (IOException ignored) {
                }
            }

            @Override
            public CoordObjectOrderedMap<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordObjectOrderedMap data = new CoordObjectOrderedMap<>(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(null, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordFloatMap with the given Json object, so CoordFloatMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordFloatMap(@NonNull Json json) {
        json.addClassTag("CfM", CoordFloatMap.class);
        registerCoord(json);
        json.setSerializer(CoordFloatMap.class, new Json.Serializer<CoordFloatMap>() {
            @Override
            public void write(Json json, CoordFloatMap object, Class knownType) {
                Writer writer = json.getWriter();
                try {
                    writer.write('{');
                } catch (IOException ignored) {
                }
                Iterator<ObjectFloatMap.Entry<Coord>> es = new ObjectFloatMap.Entries<Coord>(object).iterator();
                while (es.hasNext()) {
                    ObjectFloatMap.Entry<Coord> e = es.next();
                    try {
                        String k = json.toJson(e.getKey());
                        json.setWriter(writer);
                        json.writeValue(k);
                        writer.write(':');
                        json.writeValue(e.getValue(), Float.TYPE);
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
            public CoordFloatMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordFloatMap data = new CoordFloatMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Float.TYPE, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordFloatOrderedMap with the given Json object, so CoordFloatOrderedMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordFloatOrderedMap(@NonNull Json json) {
        json.addClassTag("CfOM", CoordFloatOrderedMap.class);
        registerCoord(json);
        json.setSerializer(CoordFloatOrderedMap.class, new Json.Serializer<CoordFloatOrderedMap>() {
            @Override
            public void write(Json json, CoordFloatOrderedMap object, Class knownType) {
                Writer writer = json.getWriter();
                try {
                    writer.write('{');
                } catch (IOException ignored) {
                }
                Iterator<ObjectFloatMap.Entry<Coord>> es = new CoordFloatOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectFloatMap.Entry<Coord> e = es.next();
                    try {
                        String k = json.toJson(e.getKey());
                        json.setWriter(writer);
                        json.writeValue(k);
                        writer.write(':');
                        json.writeValue(e.getValue(), Float.TYPE);
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
            public CoordFloatOrderedMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordFloatOrderedMap data = new CoordFloatOrderedMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Float.TYPE, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordSet with the given Json object, so CoordSet can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the items.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordSet(@NonNull Json json) {
        json.addClassTag("CS", CoordSet.class);
        registerCoord(json);
        json.setSerializer(CoordSet.class, new Json.Serializer<CoordSet>() {
            @Override
            public void write(Json json, CoordSet object, Class knownType) {
                json.writeArrayStart();
                for (Object o : object) {
                    json.writeValue(o);
                }
                json.writeArrayEnd();
            }

            @Override
            public CoordSet read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordSet data = new CoordSet(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.add(json.readValue(Coord.class, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordOrderedSet with the given Json object, so CoordOrderedSet can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the items.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordOrderedSet(@NonNull Json json) {
        json.addClassTag("COS", CoordOrderedSet.class);
        registerCoord(json);
        json.setSerializer(CoordOrderedSet.class, new Json.Serializer<CoordOrderedSet>() {
            @Override
            public void write(Json json, CoordOrderedSet object, Class knownType) {
                json.writeArrayStart();
                for (Object o : object) {
                    json.writeValue(o);
                }
                json.writeArrayEnd();
            }

            @Override
            public CoordOrderedSet read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordOrderedSet data = new CoordOrderedSet(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.add(json.readValue(Coord.class, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers Radiance with the given Json object, so Radiance can be written to and read from JSON.
     * This is a simple wrapper around Radiance's built-in {@link Radiance#serializeToString()} and
     * {@link Radiance#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRadiance(@NonNull Json json) {
        json.addClassTag("Radi", Radiance.class);
        json.setSerializer(Radiance.class, new Json.Serializer<Radiance>() {
            @Override
            public void write(Json json, Radiance object, Class knownType) {
                json.writeObjectStart(Radiance.class, knownType);
                json.writeValue("v", object.serializeToString());
                json.writeObjectEnd();
            }

            @Override
            public Radiance read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Radiance.deserializeFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers Noise with the given Json object, so Noise can be written to and read from JSON.
     * This is a simple wrapper around Noise's built-in {@link Noise#serializeToString()} and
     * {@link Noise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerNoise(@NonNull Json json) {
        json.addClassTag("Nois", Noise.class);
        json.setSerializer(Noise.class, new Json.Serializer<Noise>() {
            @Override
            public void write(Json json, Noise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Noise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Noise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers PhantomNoise with the given Json object, so PhantomNoise can be written to and read from JSON.
     * This is a simple wrapper around PhantomNoise's built-in {@link PhantomNoise#serializeToString()} and
     * {@link PhantomNoise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPhantomNoise(@NonNull Json json) {
        json.addClassTag("PhaN", PhantomNoise.class);
        json.setSerializer(PhantomNoise.class, new Json.Serializer<PhantomNoise>() {
            @Override
            public void write(Json json, PhantomNoise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public PhantomNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return PhantomNoise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers TaffyNoise with the given Json object, so TaffyNoise can be written to and read from JSON.
     * This is a simple wrapper around TaffyNoise's built-in {@link TaffyNoise#serializeToString()} and
     * {@link TaffyNoise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTaffyNoise(@NonNull Json json) {
        json.addClassTag("TafN", TaffyNoise.class);
        json.setSerializer(TaffyNoise.class, new Json.Serializer<TaffyNoise>() {
            @Override
            public void write(Json json, TaffyNoise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public TaffyNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return TaffyNoise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers FlanNoise with the given Json object, so FlanNoise can be written to and read from JSON.
     * This is a simple wrapper around FlanNoise's built-in {@link FlanNoise#serializeToString()} and
     * {@link FlanNoise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFlanNoise(@NonNull Json json) {
        json.addClassTag("FlaN", FlanNoise.class);
        json.setSerializer(FlanNoise.class, new Json.Serializer<FlanNoise>() {
            @Override
            public void write(Json json, FlanNoise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public FlanNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return FlanNoise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers CyclicNoise with the given Json object, so CyclicNoise can be written to and read from JSON.
     * This is a simple wrapper around CyclicNoise's built-in {@link CyclicNoise#serializeToString()} and
     * {@link CyclicNoise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCyclicNoise(@NonNull Json json) {
        json.addClassTag("CycN", CyclicNoise.class);
        json.setSerializer(CyclicNoise.class, new Json.Serializer<CyclicNoise>() {
            @Override
            public void write(Json json, CyclicNoise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public CyclicNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return CyclicNoise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers SorbetNoise with the given Json object, so SorbetNoise can be written to and read from JSON.
     * This is a simple wrapper around SorbetNoise's built-in {@link SorbetNoise#serializeToString()} and
     * {@link SorbetNoise#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSorbetNoise(@NonNull Json json) {
        json.addClassTag("SorN", SorbetNoise.class);
        json.setSerializer(SorbetNoise.class, new Json.Serializer<SorbetNoise>() {
            @Override
            public void write(Json json, SorbetNoise object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public SorbetNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return SorbetNoise.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers SpatialMap with the given Json object, so SpatialMap can be written to and read from JSON.
     * This also registers Coord, CoordObjectOrderedMap, and IntObjectOrderedMap with the given Json object.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSpatialMap(@NonNull Json json) {
        json.addClassTag("SpaM", SpatialMap.class);
        registerCoordObjectOrderedMap(json);
        JsonSupport.registerIntObjectOrderedMap(json);
        json.setSerializer(SpatialMap.class, new Json.Serializer<SpatialMap>() {
            @Override
            public void write(Json json, SpatialMap object, Class knownType) {
                json.writeArrayStart();
                for (Object o : object.values()) {
                    json.writeValue(o, null);
                }
                json.writeArrayEnd();
            }

            @Override
            public SpatialMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                SpatialMap<?> data = new SpatialMap<>(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.add(json.readValue(null, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers LightingManager with the given Json object, so LightingManager can be written to and read from JSON.
     * This does not register a custom serializer for LightingManager, but instead allows Json to handle it and
     * registers all types of field in a LightingManager in one go.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightingManager(@NonNull Json json) {
        json.addClassTag("LiMa", LightingManager.class);
        registerRegion(json);
        registerRadiance(json);
        registerCoordObjectOrderedMap(json);
        JsonCore.registerFloat2D(json);
        JsonCore.registerInt2D(json);
    }
}
