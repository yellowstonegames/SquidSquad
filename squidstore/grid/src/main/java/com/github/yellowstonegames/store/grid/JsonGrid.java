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
        registerNoiseWrapper(json);
        registerPhantomNoise(json);
        registerTaffyNoise(json);
        registerFlanNoise(json);
        registerCyclicNoise(json);
        registerSimplexNoise(json);
        registerSimplexNoiseScaled(json);
        registerValueNoise(json);
        registerHighDimensionalValueNoise(json);
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
     * This is a simple wrapper around Region's built-in {@link Region#stringSerialize()} and
     * {@link Region#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRegion(@NonNull Json json) {
        json.addClassTag("Regi", Region.class);
        json.setSerializer(Region.class, new Json.Serializer<Region>() {
            @Override
            public void write(Json json, Region object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public Region read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Region.stringDeserialize(jsonData.asString());
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
     * This is a simple wrapper around Radiance's built-in {@link Radiance#stringSerialize()} and
     * {@link Radiance#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRadiance(@NonNull Json json) {
        json.addClassTag("Radi", Radiance.class);
        json.setSerializer(Radiance.class, new Json.Serializer<Radiance>() {
            @Override
            public void write(Json json, Radiance object, Class knownType) {
                json.writeObjectStart(Radiance.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public Radiance read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return Radiance.stringDeserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers INoise with the given Json object, so INoise can be written to and read from JSON.
     * This is a simple wrapper around INoise's built-in {@link INoise#stringSerialize()} and
     * {@link INoise.Serializer#deserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerINoise(@NonNull Json json) {
        json.addClassTag("INoi", INoise.class);
        json.setSerializer(INoise.class, new Json.Serializer<INoise>() {
            @Override
            public void write(Json json, INoise object, Class knownType) {
                json.writeObjectStart(INoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public INoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return INoise.Serializer.deserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers Noise with the given Json object, so Noise can be written to and read from JSON.
     * This is a simple wrapper around Noise's built-in {@link Noise#stringSerialize()} and
     * {@link Noise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerNoise(@NonNull Json json) {
        json.addClassTag("Nois", Noise.class);
        json.setSerializer(Noise.class, new Json.Serializer<Noise>() {
            @Override
            public void write(Json json, Noise object, Class knownType) {
                json.writeObjectStart(Noise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public Noise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return new Noise(1).stringDeserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers NoiseWrapper with the given Json object, so NoiseWrapper can be written to and read from JSON.
     * This is a simple wrapper around NoiseWrapper's built-in {@link NoiseWrapper#stringSerialize()} and
     * {@link NoiseWrapper#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerNoiseWrapper(@NonNull Json json) {
        json.addClassTag("NoWr", NoiseWrapper.class);
        json.setSerializer(NoiseWrapper.class, new Json.Serializer<NoiseWrapper>() {
            @Override
            public void write(Json json, NoiseWrapper object, Class knownType) {
                json.writeObjectStart(NoiseWrapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public NoiseWrapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return new NoiseWrapper().stringDeserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers NoiseAdjustment with the given Json object, so NoiseAdjustment can be written to and read from JSON.
     * This is a simple wrapper around NoiseAdjustment's built-in {@link NoiseAdjustment#stringSerialize()} and
     * {@link NoiseAdjustment#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerNoiseAdjustment(@NonNull Json json) {
        json.addClassTag("NAdj", NoiseAdjustment.class);
        json.setSerializer(NoiseAdjustment.class, new Json.Serializer<NoiseAdjustment>() {
            @Override
            public void write(Json json, NoiseAdjustment object, Class knownType) {
                json.writeObjectStart(NoiseAdjustment.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public NoiseAdjustment read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return new NoiseAdjustment().stringDeserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers PhantomNoise with the given Json object, so PhantomNoise can be written to and read from JSON.
     * This is a simple wrapper around PhantomNoise's built-in {@link PhantomNoise#stringSerialize()} and
     * {@link PhantomNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPhantomNoise(@NonNull Json json) {
        json.addClassTag("PhaN", PhantomNoise.class);
        json.setSerializer(PhantomNoise.class, new Json.Serializer<PhantomNoise>() {
            @Override
            public void write(Json json, PhantomNoise object, Class knownType) {
                json.writeObjectStart(PhantomNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public PhantomNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return PhantomNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers TaffyNoise with the given Json object, so TaffyNoise can be written to and read from JSON.
     * This is a simple wrapper around TaffyNoise's built-in {@link TaffyNoise#stringSerialize()} and
     * {@link TaffyNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTaffyNoise(@NonNull Json json) {
        json.addClassTag("TafN", TaffyNoise.class);
        json.setSerializer(TaffyNoise.class, new Json.Serializer<TaffyNoise>() {
            @Override
            public void write(Json json, TaffyNoise object, Class knownType) {
                json.writeObjectStart(TaffyNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public TaffyNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return TaffyNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers FlanNoise with the given Json object, so FlanNoise can be written to and read from JSON.
     * This is a simple wrapper around FlanNoise's built-in {@link FlanNoise#stringSerialize()} and
     * {@link FlanNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFlanNoise(@NonNull Json json) {
        json.addClassTag("FlaN", FlanNoise.class);
        json.setSerializer(FlanNoise.class, new Json.Serializer<FlanNoise>() {
            @Override
            public void write(Json json, FlanNoise object, Class knownType) {
                json.writeObjectStart(FlanNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public FlanNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return FlanNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers CyclicNoise with the given Json object, so CyclicNoise can be written to and read from JSON.
     * This is a simple wrapper around CyclicNoise's built-in {@link CyclicNoise#stringSerialize()} and
     * {@link CyclicNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCyclicNoise(@NonNull Json json) {
        json.addClassTag("CycN", CyclicNoise.class);
        json.setSerializer(CyclicNoise.class, new Json.Serializer<CyclicNoise>() {
            @Override
            public void write(Json json, CyclicNoise object, Class knownType) {
                json.writeObjectStart(CyclicNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public CyclicNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return CyclicNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers SimplexNoise with the given Json object, so SimplexNoise can be written to and read from JSON.
     * This is a simple wrapper around SimplexNoise's built-in {@link SimplexNoise#stringSerialize()} and
     * {@link SimplexNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSimplexNoise(@NonNull Json json) {
        json.addClassTag("SimN", SimplexNoise.class);
        json.setSerializer(SimplexNoise.class, new Json.Serializer<SimplexNoise>() {
            @Override
            public void write(Json json, SimplexNoise object, Class knownType) {
                json.writeObjectStart(SimplexNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public SimplexNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SimplexNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers SimplexNoiseScaled with the given Json object, so SimplexNoiseScaled can be written to and read from JSON.
     * This is a simple wrapper around SimplexNoiseScaled's built-in {@link SimplexNoiseScaled#stringSerialize()} and
     * {@link SimplexNoiseScaled#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSimplexNoiseScaled(@NonNull Json json) {
        json.addClassTag("SmSN", SimplexNoiseScaled.class);
        json.setSerializer(SimplexNoiseScaled.class, new Json.Serializer<SimplexNoiseScaled>() {
            @Override
            public void write(Json json, SimplexNoiseScaled object, Class knownType) {
                json.writeObjectStart(SimplexNoiseScaled.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();

            }

            @Override
            public SimplexNoiseScaled read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SimplexNoiseScaled.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers HighDimensionalValueNoise with the given Json object, so HighDimensionalValueNoise can be written to and read from JSON.
     * This is a simple wrapper around HighDimensionalValueNoise's built-in {@link HighDimensionalValueNoise#stringSerialize()} and
     * {@link HighDimensionalValueNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerHighDimensionalValueNoise(@NonNull Json json) {
        json.addClassTag("HDVN", HighDimensionalValueNoise.class);
        json.setSerializer(HighDimensionalValueNoise.class, new Json.Serializer<HighDimensionalValueNoise>() {
            @Override
            public void write(Json json, HighDimensionalValueNoise object, Class knownType) {
                json.writeObjectStart(HighDimensionalValueNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public HighDimensionalValueNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return HighDimensionalValueNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers ValueNoise with the given Json object, so ValueNoise can be written to and read from JSON.
     * This is a simple wrapper around ValueNoise's built-in {@link ValueNoise#stringSerialize()} and
     * {@link ValueNoise#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerValueNoise(@NonNull Json json) {
        json.addClassTag("ValN", ValueNoise.class);
        json.setSerializer(ValueNoise.class, new Json.Serializer<ValueNoise>() {
            @Override
            public void write(Json json, ValueNoise object, Class knownType) {
                json.writeObjectStart(ValueNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public ValueNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return ValueNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    // To get BasicHashNoise to ser/deser, we would need all IPointHash to be possible to create from a String...

//    /**
//     * Registers BasicHashNoise with the given Json object, so BasicHashNoise can be written to and read from JSON.
//     * This is a simple wrapper around BasicHashNoise's built-in {@link BasicHashNoise#stringSerialize()} and
//     * {@link BasicHashNoise#stringDeserialize(String)} methods.
//     *
//     * @param json a libGDX Json object that will have a serializer registered
//     */
//    public static void registerBasicHashNoise(@NonNull Json json) {
//        json.addClassTag("ValN", BasicHashNoise.class);
//        json.setSerializer(BasicHashNoise.class, new Json.Serializer<BasicHashNoise>() {
//            @Override
//            public void write(Json json, BasicHashNoise object, Class knownType) {
//                json.writeObjectStart(BasicHashNoise.class, knownType);
//                json.writeValue("v", object.seed);
//                json.writeValue("h", object.pointHash);
//                json.writeObjectEnd();
//            }
//
//            @Override
//            public BasicHashNoise read(Json json, JsonValue jsonData, Class type) {
//                if (jsonData == null || jsonData.isNull() || !jsonData.has("v") || !jsonData.has("h")) return null;
//                return new BasicHashNoise(jsonData.get("v").asInt(), json.readValue("h", IPointHash.class, jsonData));
//            }
//        });
//    }

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
