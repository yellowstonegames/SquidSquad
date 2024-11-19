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

package com.github.yellowstonegames.store.grid;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.crux.PointPair;
import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.store.core.JsonCore;

import org.checkerframework.checker.nullness.qual.NonNull;

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
        registerPoint2Int(json);
        registerPoint3Int(json);
        registerPoint4Int(json);
        registerRegion(json);
        registerCoordSet(json);
        registerCoordOrderedSet(json);
        registerCoordObjectMap(json);
        registerCoordObjectOrderedMap(json);
        registerCoordFloatMap(json);
        registerCoordFloatOrderedMap(json);
        registerCoordIntMap(json);
        registerCoordIntOrderedMap(json);
        registerCoordLongMap(json);
        registerCoordLongOrderedMap(json);
        registerSpatialMap(json);
        registerRadiance(json);
        registerLightingManager(json);
        registerLightingManagerRgb(json);
        registerVisionFramework(json);
        registerVisionFrameworkRgb(json);
//        registerBasicHashNoise(json); // Cannot be serialized to JSON without IPointHash being serializable, too.
        registerBadgerNoise(json);
        registerCyclicNoise(json);
        registerFlanNoise(json);
        registerFoamNoise(json);
        registerFoamplexNoise(json);
        registerHighDimensionalValueNoise(json);
        registerHoneyNoise(json);
        registerNoiseAdjustment(json);
        registerNoise(json);
        registerNoiseWrapper(json);
        registerOpenSimplex2(json);
        registerOpenSimplex2Smooth(json);
        registerPerlinNoise(json);
        registerPerlueNoise(json);
        registerPhantomNoise(json);
        registerRadialNoiseWrapper(json);
        registerSimplexNoise(json);
        registerSimplexNoiseHard(json);
        registerSimplexNoiseScaled(json);
        registerSnakeNoise(json);
        registerSorbetNoise(json);
        registerTaffyNoise(json);
        registerValueNoise(json);
        registerWhiteNoise(json);
        registerINoise(json);
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
                json.writeObjectStart(Coord.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeObjectEnd();
            }

            @Override
            public Coord read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Coord.get(jsonData.getInt("x", 0), jsonData.getInt("y", 0));
            }
        });
    }

    /**
     * Registers Point2Int with the given Json object, so Point2Int can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPoint2Int(@NonNull Json json) {
        json.addClassTag("Pt2I", Point2Int.class);
        json.setSerializer(Point2Int.class, new Json.Serializer<Point2Int>() {
            @Override
            public void write(Json json, Point2Int object, Class knownType) {
                json.writeObjectStart(Point2Int.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeObjectEnd();
            }

            @Override
            public Point2Int read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Point2Int(jsonData.getInt("x", 0), jsonData.getInt("y", 0));
            }
        });
    }

    /**
     * Registers Point3Int with the given Json object, so Point3Int can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPoint3Int(@NonNull Json json) {
        json.addClassTag("Pt3I", Point3Int.class);
        json.setSerializer(Point3Int.class, new Json.Serializer<Point3Int>() {
            @Override
            public void write(Json json, Point3Int object, Class knownType) {
                json.writeObjectStart(Point3Int.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeValue("z", object.z);
                json.writeObjectEnd();
            }

            @Override
            public Point3Int read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Point3Int(jsonData.getInt("x", 0), jsonData.getInt("y", 0), jsonData.getInt("z", 0));
            }
        });
    }

    /**
     * Registers Point4Int with the given Json object, so Point4Int can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPoint4Int(@NonNull Json json) {
        json.addClassTag("Pt4I", Point4Int.class);
        json.setSerializer(Point4Int.class, new Json.Serializer<Point4Int>() {
            @Override
            public void write(Json json, Point4Int object, Class knownType) {
                json.writeObjectStart(Point4Int.class, knownType);
                json.writeValue("x", object.x);
                json.writeValue("y", object.y);
                json.writeValue("z", object.z);
                json.writeValue("w", object.w);
                json.writeObjectEnd();
            }

            @Override
            public Point4Int read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Point4Int(jsonData.getInt("x", 0), jsonData.getInt("y", 0),
                        jsonData.getInt("z", 0), jsonData.getInt("w", 0));
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
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<Map.Entry<Coord, ?>> es = new CoordObjectMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    Map.Entry<Coord, ?> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), null);
                }
                json.writeObjectEnd();
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
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<Map.Entry<Coord, ?>> es = new ObjectObjectOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    Map.Entry<Coord, ?> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), null);
                }
                json.writeObjectEnd();
            }

            @Override
            public CoordObjectOrderedMap<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordObjectOrderedMap<?> data = new CoordObjectOrderedMap<>(jsonData.size);
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
                json.writeObjectStart();
                Iterator<ObjectFloatMap.Entry<Coord>> es = new CoordFloatMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectFloatMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Float.TYPE);
                }
                json.writeObjectEnd();
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
                json.writeObjectStart();
                Iterator<ObjectFloatMap.Entry<Coord>> es = new CoordFloatOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectFloatMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Float.TYPE);
                }
                json.writeObjectEnd();
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
     * Registers CoordLongMap with the given Json object, so CoordLongMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordLongMap(@NonNull Json json) {
        json.addClassTag("ClM", CoordLongMap.class);
        registerCoord(json);
        json.setSerializer(CoordLongMap.class, new Json.Serializer<CoordLongMap>() {
            @Override
            public void write(Json json, CoordLongMap object, Class knownType) {
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<ObjectLongMap.Entry<Coord>> es = new CoordLongMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectLongMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Long.TYPE);
                }
                json.writeObjectEnd();
            }

            @Override
            public CoordLongMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordLongMap data = new CoordLongMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Long.TYPE, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordLongOrderedMap with the given Json object, so CoordLongOrderedMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordLongOrderedMap(@NonNull Json json) {
        json.addClassTag("ClOM", CoordLongOrderedMap.class);
        registerCoord(json);
        json.setSerializer(CoordLongOrderedMap.class, new Json.Serializer<CoordLongOrderedMap>() {
            @Override
            public void write(Json json, CoordLongOrderedMap object, Class knownType) {
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<ObjectLongMap.Entry<Coord>> es = new CoordLongOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectLongMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Long.TYPE);
                }
                json.writeObjectEnd();
            }

            @Override
            public CoordLongOrderedMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordLongOrderedMap data = new CoordLongOrderedMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Long.TYPE, value));
                }
                return data;
            }
        });
    }


    /**
     * Registers CoordIntMap with the given Json object, so CoordIntMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordIntMap(@NonNull Json json) {
        json.addClassTag("CiM", CoordIntMap.class);
        registerCoord(json);
        json.setSerializer(CoordIntMap.class, new Json.Serializer<CoordIntMap>() {
            @Override
            public void write(Json json, CoordIntMap object, Class knownType) {
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<ObjectIntMap.Entry<Coord>> es = new CoordIntMap.Entries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectIntMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Integer.TYPE);
                }
                json.writeObjectEnd();
            }

            @Override
            public CoordIntMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordIntMap data = new CoordIntMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Integer.TYPE, value));
                }
                return data;
            }
        });
    }

    /**
     * Registers CoordIntOrderedMap with the given Json object, so CoordIntOrderedMap can be written to and read from JSON.
     * This also registers Coord with the given Json object, since it is used for the keys.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerCoordIntOrderedMap(@NonNull Json json) {
        json.addClassTag("CiOM", CoordIntOrderedMap.class);
        registerCoord(json);
        json.setSerializer(CoordIntOrderedMap.class, new Json.Serializer<CoordIntOrderedMap>() {
            @Override
            public void write(Json json, CoordIntOrderedMap object, Class knownType) {
                Writer writer = json.getWriter();
                json.writeObjectStart();
                Iterator<ObjectIntMap.Entry<Coord>> es = new CoordIntOrderedMap.OrderedMapEntries<>(object).iterator();
                while (es.hasNext()) {
                    ObjectIntMap.Entry<Coord> e = es.next();
                    String k = json.toJson(e.getKey());
                    json.setWriter(writer);
                    json.writeValue(k, e.getValue(), Integer.TYPE);
                }
                json.writeObjectEnd();
            }

            @Override
            public CoordIntOrderedMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                CoordIntOrderedMap data = new CoordIntOrderedMap(jsonData.size);
                for (JsonValue value = jsonData.child; value != null; value = value.next) {
                    data.put(json.fromJson(Coord.class, value.name), json.readValue(Integer.TYPE, value));
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
     * {@link Radiance#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRadiance(@NonNull Json json) {
        json.addClassTag("Rada", Radiance.class);
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
                return Radiance.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers LightSource with the given Json object, so LightSource can be written to and read from JSON.
     * This is a simple wrapper around LightSource's built-in {@link LightSource#stringSerialize()} and
     * {@link LightSource#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightSource(@NonNull Json json) {
        json.addClassTag("LiSo", LightSource.class);
        json.setSerializer(LightSource.class, new Json.Serializer<LightSource>() {
            @Override
            public void write(Json json, LightSource object, Class knownType) {
                json.writeObjectStart(LightSource.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public LightSource read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return LightSource.recreateFromString(jsonData.get("v").asString());
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
     * Registers RadialNoiseWrapper with the given Json object, so RadialNoiseWrapper can be written to and read from JSON.
     * This is a simple wrapper around RadialNoiseWrapper's built-in {@link RadialNoiseWrapper#stringSerialize()} and
     * {@link RadialNoiseWrapper#stringDeserialize(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRadialNoiseWrapper(@NonNull Json json) {
        json.addClassTag("NAdj", RadialNoiseWrapper.class);
        json.setSerializer(RadialNoiseWrapper.class, new Json.Serializer<RadialNoiseWrapper>() {
            @Override
            public void write(Json json, RadialNoiseWrapper object, Class knownType) {
                json.writeObjectStart(RadialNoiseWrapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public RadialNoiseWrapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return new RadialNoiseWrapper().stringDeserialize(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers PhantomNoise with the given Json object, so PhantomNoise can be written to and read from JSON.
     * This is a simple wrapper around PhantomNoise's built-in {@link PhantomNoise#stringSerialize()} and
     * {@link PhantomNoise#recreateFromString(String)} methods.
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
     * {@link TaffyNoise#recreateFromString(String)} methods.
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
     * {@link FlanNoise#recreateFromString(String)} methods.
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
     * Registers BadgerNoise with the given Json object, so BadgerNoise can be written to and read from JSON.
     * This is a simple wrapper around BadgerNoise's built-in {@link BadgerNoise#stringSerialize()} and
     * {@link BadgerNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerBadgerNoise(@NonNull Json json) {
        json.addClassTag("BdgN", BadgerNoise.class);
        json.setSerializer(BadgerNoise.class, new Json.Serializer<BadgerNoise>() {
            @Override
            public void write(Json json, BadgerNoise object, Class knownType) {
                json.writeObjectStart(BadgerNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public BadgerNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return BadgerNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers SnakeNoise with the given Json object, so SnakeNoise can be written to and read from JSON.
     * This is a simple wrapper around SnakeNoise's built-in {@link SnakeNoise#stringSerialize()} and
     * {@link SnakeNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSnakeNoise(@NonNull Json json) {
        json.addClassTag("SnkN", SnakeNoise.class);
        json.setSerializer(SnakeNoise.class, new Json.Serializer<SnakeNoise>() {
            @Override
            public void write(Json json, SnakeNoise object, Class knownType) {
                json.writeObjectStart(SnakeNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public SnakeNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SnakeNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers CyclicNoise with the given Json object, so CyclicNoise can be written to and read from JSON.
     * This is a simple wrapper around CyclicNoise's built-in {@link CyclicNoise#stringSerialize()} and
     * {@link CyclicNoise#recreateFromString(String)} methods.
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
     * Registers FoamNoise with the given Json object, so FoamNoise can be written to and read from JSON.
     * This is a simple wrapper around FoamNoise's built-in {@link FoamNoise#stringSerialize()} and
     * {@link FoamNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFoamNoise(@NonNull Json json) {
        json.addClassTag("FoaN", FoamNoise.class);
        json.setSerializer(FoamNoise.class, new Json.Serializer<FoamNoise>() {
            @Override
            public void write(Json json, FoamNoise object, Class knownType) {
                json.writeObjectStart(FoamNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public FoamNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return FoamNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers FoamplexNoise with the given Json object, so FoamplexNoise can be written to and read from JSON.
     * This is a simple wrapper around FoamplexNoise's built-in {@link FoamplexNoise#stringSerialize()} and
     * {@link FoamplexNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFoamplexNoise(@NonNull Json json) {
        json.addClassTag("FoaN", FoamplexNoise.class);
        json.setSerializer(FoamplexNoise.class, new Json.Serializer<FoamplexNoise>() {
            @Override
            public void write(Json json, FoamplexNoise object, Class knownType) {
                json.writeObjectStart(FoamplexNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public FoamplexNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return FoamplexNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers HoneyNoise with the given Json object, so HoneyNoise can be written to and read from JSON.
     * This is a simple wrapper around HoneyNoise's built-in {@link HoneyNoise#stringSerialize()} and
     * {@link HoneyNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerHoneyNoise(@NonNull Json json) {
        json.addClassTag("FoaN", HoneyNoise.class);
        json.setSerializer(HoneyNoise.class, new Json.Serializer<HoneyNoise>() {
            @Override
            public void write(Json json, HoneyNoise object, Class knownType) {
                json.writeObjectStart(HoneyNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public HoneyNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return HoneyNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers SimplexNoise with the given Json object, so SimplexNoise can be written to and read from JSON.
     * This is a simple wrapper around SimplexNoise's built-in {@link SimplexNoise#stringSerialize()} and
     * {@link SimplexNoise#recreateFromString(String)} methods.
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
     * {@link SimplexNoiseScaled#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSimplexNoiseScaled(@NonNull Json json) {
        json.addClassTag("SiSN", SimplexNoiseScaled.class);
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
     * Registers SimplexNoiseHard with the given Json object, so SimplexNoiseHard can be written to and read from JSON.
     * This is a simple wrapper around SimplexNoiseHard's built-in {@link SimplexNoiseHard#stringSerialize()} and
     * {@link SimplexNoiseHard#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSimplexNoiseHard(@NonNull Json json) {
        json.addClassTag("SiHN", SimplexNoiseHard.class);
        json.setSerializer(SimplexNoiseHard.class, new Json.Serializer<SimplexNoiseHard>() {
            @Override
            public void write(Json json, SimplexNoiseHard object, Class knownType) {
                json.writeObjectStart(SimplexNoiseHard.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();

            }

            @Override
            public SimplexNoiseHard read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SimplexNoiseHard.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers OpenSimplex2 with the given Json object, so OpenSimplex2 can be written to and read from JSON.
     * This is a simple wrapper around OpenSimplex2's built-in {@link OpenSimplex2#stringSerialize()} and
     * {@link OpenSimplex2#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerOpenSimplex2(@NonNull Json json) {
        json.addClassTag("OSFN", OpenSimplex2.class);
        json.setSerializer(OpenSimplex2.class, new Json.Serializer<OpenSimplex2>() {
            @Override
            public void write(Json json, OpenSimplex2 object, Class knownType) {
                json.writeObjectStart(OpenSimplex2.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();

            }

            @Override
            public OpenSimplex2 read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return OpenSimplex2.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers OpenSimplex2Smooth with the given Json object, so OpenSimplex2Smooth can be written to and read from JSON.
     * This is a simple wrapper around OpenSimplex2Smooth's built-in {@link OpenSimplex2Smooth#stringSerialize()} and
     * {@link OpenSimplex2Smooth#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerOpenSimplex2Smooth(@NonNull Json json) {
        json.addClassTag("OSSN", OpenSimplex2Smooth.class);
        json.setSerializer(OpenSimplex2Smooth.class, new Json.Serializer<OpenSimplex2Smooth>() {
            @Override
            public void write(Json json, OpenSimplex2Smooth object, Class knownType) {
                json.writeObjectStart(OpenSimplex2Smooth.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();

            }

            @Override
            public OpenSimplex2Smooth read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return OpenSimplex2Smooth.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers HighDimensionalValueNoise with the given Json object, so HighDimensionalValueNoise can be written to and read from JSON.
     * This is a simple wrapper around HighDimensionalValueNoise's built-in {@link HighDimensionalValueNoise#stringSerialize()} and
     * {@link HighDimensionalValueNoise#recreateFromString(String)} methods.
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
     * {@link ValueNoise#recreateFromString(String)} methods.
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

    /**
     * Registers PerlinNoise with the given Json object, so PerlinNoise can be written to and read from JSON.
     * This is a simple wrapper around PerlinNoise's built-in {@link PerlinNoise#stringSerialize()} and
     * {@link PerlinNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPerlinNoise(@NonNull Json json) {
        json.addClassTag("PerN", PerlinNoise.class);
        json.setSerializer(PerlinNoise.class, new Json.Serializer<PerlinNoise>() {
            @Override
            public void write(Json json, PerlinNoise object, Class knownType) {
                json.writeObjectStart(PerlinNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public PerlinNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return PerlinNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers PerlueNoise with the given Json object, so PerlueNoise can be written to and read from JSON.
     * This is a simple wrapper around PerlueNoise's built-in {@link PerlueNoise#stringSerialize()} and
     * {@link PerlueNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPerlueNoise(@NonNull Json json) {
        json.addClassTag("PluN", PerlueNoise.class);
        json.setSerializer(PerlueNoise.class, new Json.Serializer<PerlueNoise>() {
            @Override
            public void write(Json json, PerlueNoise object, Class knownType) {
                json.writeObjectStart(PerlueNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public PerlueNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return PerlueNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers SorbetNoise with the given Json object, so SorbetNoise can be written to and read from JSON.
     * This is a simple wrapper around SorbetNoise's built-in {@link SorbetNoise#stringSerialize()} and
     * {@link SorbetNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSorbetNoise(@NonNull Json json) {
        json.addClassTag("SorN", SorbetNoise.class);
        json.setSerializer(SorbetNoise.class, new Json.Serializer<SorbetNoise>() {
            @Override
            public void write(Json json, SorbetNoise object, Class knownType) {
                json.writeObjectStart(SorbetNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public SorbetNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SorbetNoise.recreateFromString(jsonData.get("v").asString());
            }
        });
    }

    /**
     * Registers WhiteNoise with the given Json object, so WhiteNoise can be written to and read from JSON.
     * This is a simple wrapper around WhiteNoise's built-in {@link WhiteNoise#stringSerialize()} and
     * {@link WhiteNoise#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerWhiteNoise(@NonNull Json json) {
        json.addClassTag("WhtN", WhiteNoise.class);
        json.setSerializer(WhiteNoise.class, new Json.Serializer<WhiteNoise>() {
            @Override
            public void write(Json json, WhiteNoise object, Class knownType) {
                json.writeObjectStart(WhiteNoise.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public WhiteNoise read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return WhiteNoise.recreateFromString(jsonData.get("v").asString());
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
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightingManager(@NonNull Json json) {
        json.addClassTag("LiMo", LightingManager.class);
        registerRegion(json);
        registerLightSource(json);
        JsonSupport.registerObjectDeque(json);
        JsonCore.registerFloat2D(json);
        JsonCore.registerInt2D(json);

        json.setSerializer(LightingManager.class, new Json.Serializer<LightingManager>() {
            @Override
            public void write(Json json, LightingManager object, Class knownType) {
                json.writeObjectStart(LightingManager.class, knownType);
                json.writeValue("width", object.width);
                json.writeValue("height", object.height);
                json.writeValue("backgroundColor", object.backgroundColor);
                json.writeValue("viewerRange", object.viewerRange);
                json.writeValue("resistances", object.resistances, float[][].class);
                json.writeValue("fovResult", object.fovResult, float[][].class);
                json.writeValue("lightFromFOV", object.lightFromFOV, float[][].class);
                json.writeValue("lightingStrength", object.lightingStrength, float[][].class);
                json.writeValue("losResult", object.losResult, float[][].class);
                json.writeValue("colorLighting", object.colorLighting, int[][].class);
                json.writeValue("fovLightColors", object.fovLightColors, int[][].class);
                json.writeValue("noticeable", object.noticeable, Region.class);
                json.writeValue("radiusStrategy", object.radiusStrategy.name());
                json.writeValue("symmetry", object.symmetry.name());
                json.writeValue("lights", object.lights, ObjectDeque.class, LightSource.class);
                json.writeObjectEnd();
            }

            @SuppressWarnings("unchecked")
            @Override
            public LightingManager read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                int width = json.readValue(int.class, jsonData.get("width"));
                int height = json.readValue(int.class, jsonData.get("height"));
                int backgroundColor = json.readValue(int.class, jsonData.get("backgroundColor"));
                float viewerRange = json.readValue(float.class, jsonData.get("viewerRange"));
                float[][] resistances = json.readValue(float[][].class, jsonData.get("resistances"));
                float[][] fovResult = json.readValue(float[][].class, jsonData.get("fovResult"));
                float[][] lightFromFOV = json.readValue(float[][].class, jsonData.get("lightFromFOV"));
                float[][] lightingStrength = json.readValue(float[][].class, jsonData.get("lightingStrength"));
                float[][] losResult = json.readValue(float[][].class, jsonData.get("losResult"));
                int[][] colorLighting = json.readValue(int[][].class, jsonData.get("colorLighting"));
                int[][] fovLightColors = json.readValue(int[][].class, jsonData.get("fovLightColors"));
                Region noticeable = json.readValue(Region.class, jsonData.get("noticeable"));
                Radius radiusStrategy = Radius.valueOf(json.readValue(String.class, jsonData.get("radiusStrategy")));
                LightingManager.SymmetryMode symmetry = LightingManager.SymmetryMode.valueOf(json.readValue(String.class, jsonData.get("symmetry")));
                ObjectDeque<LightSource> lights = json.readValue(ObjectDeque.class, LightSource.class, jsonData.get("lights"));
                LightingManager data = new LightingManager(resistances, backgroundColor, radiusStrategy, viewerRange, symmetry);
                data.width = width;
                data.height = height;
                data.fovResult = fovResult;
                data.lightFromFOV = lightFromFOV;
                data.lightingStrength = lightingStrength;
                data.losResult = losResult;
                data.colorLighting = colorLighting;
                data.fovLightColors = fovLightColors;
                data.noticeable = noticeable;
                data.lights = lights;
                return data;
            }
        });
    }

    /**
     * Registers LightingManagerRgb with the given Json object, so LightingManagerRgb can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightingManagerRgb(@NonNull Json json) {
        json.addClassTag("LiMr", LightingManagerRgb.class);
        registerRegion(json);
        registerLightSource(json);
        JsonSupport.registerObjectDeque(json);
        JsonCore.registerFloat2D(json);
        JsonCore.registerInt2D(json);

        json.setSerializer(LightingManagerRgb.class, new Json.Serializer<LightingManagerRgb>() {
            @Override
            public void write(Json json, LightingManagerRgb object, Class knownType) {
                json.writeObjectStart(LightingManagerRgb.class, knownType);
                json.writeValue("width", object.width);
                json.writeValue("height", object.height);
                json.writeValue("backgroundColor", object.backgroundColor);
                json.writeValue("viewerRange", object.viewerRange);
                json.writeValue("resistances", object.resistances, float[][].class);
                json.writeValue("fovResult", object.fovResult, float[][].class);
                json.writeValue("lightFromFOV", object.lightFromFOV, float[][].class);
                json.writeValue("lightingStrength", object.lightingStrength, float[][].class);
                json.writeValue("losResult", object.losResult, float[][].class);
                json.writeValue("colorLighting", object.colorLighting, int[][].class);
                json.writeValue("fovLightColors", object.fovLightColors, int[][].class);
                json.writeValue("noticeable", object.noticeable, Region.class);
                json.writeValue("radiusStrategy", object.radiusStrategy.name());
                json.writeValue("symmetry", object.symmetry.name());
                json.writeValue("lights", object.lights, ObjectDeque.class, LightSource.class);
                json.writeObjectEnd();
            }

            @SuppressWarnings("unchecked")
            @Override
            public LightingManagerRgb read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                int width = json.readValue(int.class, jsonData.get("width"));
                int height = json.readValue(int.class, jsonData.get("height"));
                int backgroundColor = json.readValue(int.class, jsonData.get("backgroundColor"));
                float viewerRange = json.readValue(float.class, jsonData.get("viewerRange"));
                float[][] resistances = json.readValue(float[][].class, jsonData.get("resistances"));
                float[][] fovResult = json.readValue(float[][].class, jsonData.get("fovResult"));
                float[][] lightFromFOV = json.readValue(float[][].class, jsonData.get("lightFromFOV"));
                float[][] lightingStrength = json.readValue(float[][].class, jsonData.get("lightingStrength"));
                float[][] losResult = json.readValue(float[][].class, jsonData.get("losResult"));
                int[][] colorLighting = json.readValue(int[][].class, jsonData.get("colorLighting"));
                int[][] fovLightColors = json.readValue(int[][].class, jsonData.get("fovLightColors"));
                Region noticeable = json.readValue(Region.class, jsonData.get("noticeable"));
                Radius radiusStrategy = Radius.valueOf(json.readValue(String.class, jsonData.get("radiusStrategy")));
                LightingManagerRgb.SymmetryMode symmetry = LightingManagerRgb.SymmetryMode.valueOf(json.readValue(String.class, jsonData.get("symmetry")));
                ObjectDeque<LightSource> lights = json.readValue(ObjectDeque.class, LightSource.class, jsonData.get("lights"));
                LightingManagerRgb data = new LightingManagerRgb(resistances, backgroundColor, radiusStrategy, viewerRange, symmetry);
                data.width = width;
                data.height = height;
                data.fovResult = fovResult;
                data.lightFromFOV = lightFromFOV;
                data.lightingStrength = lightingStrength;
                data.losResult = losResult;
                data.colorLighting = colorLighting;
                data.fovLightColors = fovLightColors;
                data.noticeable = noticeable;
                data.lights = lights;
                return data;
            }
        });
    }

    /**
     * Registers VisionFramework with the given Json object, so VisionFramework can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerVisionFramework(@NonNull Json json) {
        json.addClassTag("ViFo", VisionFramework.class);
        registerLightingManager(json);
        JsonCore.registerChar2D(json);
        JsonGrid.registerCoordFloatOrderedMap(json);

        json.setSerializer(VisionFramework.class, new Json.Serializer<VisionFramework>() {
            @Override
            public void write(Json json, VisionFramework object, Class knownType) {
                json.writeObjectStart(VisionFramework.class, knownType);
                json.writeValue("placeWidth", object.placeWidth);
                json.writeValue("placeHeight", object.placeHeight);
                json.writeValue("rememberedColor", object.rememberedColor);
                json.writeValue("backgroundColors", object.backgroundColors, int[][].class);
                json.writeValue("previousLightLevels", object.previousLightLevels, float[][].class);
                json.writeValue("linePlaceMap", object.linePlaceMap, char[][].class);
                json.writeValue("prunedPlaceMap", object.prunedPlaceMap, char[][].class);
                json.writeValue("blockage", object.blockage, Region.class);
                json.writeValue("inView", object.inView, Region.class);
                json.writeValue("justHidden", object.justHidden, Region.class);
                json.writeValue("justSeen", object.justSeen, Region.class);
                json.writeValue("seen", object.seen, Region.class);
                json.writeValue("newlyVisible", object.newlyVisible, Region.class);
                json.writeValue("lighting", object.lighting, LightingManager.class);
                json.writeValue("viewers", object.viewers, CoordFloatOrderedMap.class);
                json.writeObjectEnd();
            }

            @Override
            public VisionFramework read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                VisionFramework data = new VisionFramework();
                data.placeWidth = json.readValue(int.class, jsonData.get("placeWidth"));
                data.placeHeight = json.readValue(int.class, jsonData.get("placeHeight"));
                data.rememberedColor = json.readValue(int.class, jsonData.get("rememberedColor"));
                data.backgroundColors = json.readValue(int[][].class, jsonData.get("backgroundColors"));
                data.previousLightLevels = json.readValue(float[][].class, jsonData.get("previousLightLevels"));
                data.linePlaceMap = json.readValue(char[][].class, jsonData.get("linePlaceMap"));
                data.prunedPlaceMap = json.readValue(char[][].class, jsonData.get("prunedPlaceMap"));
                data.blockage = json.readValue(Region.class, jsonData.get("blockage"));
                data.inView = json.readValue(Region.class, jsonData.get("inView"));
                data.justHidden = json.readValue(Region.class, jsonData.get("justHidden"));
                data.justSeen = json.readValue(Region.class, jsonData.get("justSeen"));
                data.seen = json.readValue(Region.class, jsonData.get("seen"));
                data.newlyVisible = json.readValue(Region.class, jsonData.get("newlyVisible"));
                data.lighting = json.readValue(LightingManager.class, jsonData.get("lighting"));
                data.viewers = json.readValue(CoordFloatOrderedMap.class, jsonData.get("viewers"));
                return data;
            }
        });
    }

    /**
     * Registers VisionFrameworkRgb with the given Json object, so VisionFrameworkRgb can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerVisionFrameworkRgb(@NonNull Json json) {
        json.addClassTag("ViFr", VisionFrameworkRgb.class);
        registerLightingManagerRgb(json);
        JsonCore.registerChar2D(json);
        JsonGrid.registerCoordFloatOrderedMap(json);

        json.setSerializer(VisionFrameworkRgb.class, new Json.Serializer<VisionFrameworkRgb>() {
            @Override
            public void write(Json json, VisionFrameworkRgb object, Class knownType) {
                json.writeObjectStart(VisionFrameworkRgb.class, knownType);
                json.writeValue("placeWidth", object.placeWidth);
                json.writeValue("placeHeight", object.placeHeight);
                json.writeValue("rememberedColor", object.rememberedColor);
                json.writeValue("backgroundColors", object.backgroundColors, int[][].class);
                json.writeValue("previousLightLevels", object.previousLightLevels, float[][].class);
                json.writeValue("linePlaceMap", object.linePlaceMap, char[][].class);
                json.writeValue("prunedPlaceMap", object.prunedPlaceMap, char[][].class);
                json.writeValue("blockage", object.blockage, Region.class);
                json.writeValue("inView", object.inView, Region.class);
                json.writeValue("justHidden", object.justHidden, Region.class);
                json.writeValue("justSeen", object.justSeen, Region.class);
                json.writeValue("seen", object.seen, Region.class);
                json.writeValue("newlyVisible", object.newlyVisible, Region.class);
                json.writeValue("lighting", object.lighting, LightingManagerRgb.class);
                json.writeValue("viewers", object.viewers, CoordFloatOrderedMap.class);
                json.writeObjectEnd();
            }

            @Override
            public VisionFrameworkRgb read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                VisionFrameworkRgb data = new VisionFrameworkRgb();
                data.placeWidth = json.readValue(int.class, jsonData.get("placeWidth"));
                data.placeHeight = json.readValue(int.class, jsonData.get("placeHeight"));
                data.rememberedColor = json.readValue(int.class, jsonData.get("rememberedColor"));
                data.backgroundColors = json.readValue(int[][].class, jsonData.get("backgroundColors"));
                data.previousLightLevels = json.readValue(float[][].class, jsonData.get("previousLightLevels"));
                data.linePlaceMap = json.readValue(char[][].class, jsonData.get("linePlaceMap"));
                data.prunedPlaceMap = json.readValue(char[][].class, jsonData.get("prunedPlaceMap"));
                data.blockage = json.readValue(Region.class, jsonData.get("blockage"));
                data.inView = json.readValue(Region.class, jsonData.get("inView"));
                data.justHidden = json.readValue(Region.class, jsonData.get("justHidden"));
                data.justSeen = json.readValue(Region.class, jsonData.get("justSeen"));
                data.seen = json.readValue(Region.class, jsonData.get("seen"));
                data.newlyVisible = json.readValue(Region.class, jsonData.get("newlyVisible"));
                data.lighting = json.readValue(LightingManagerRgb.class, jsonData.get("lighting"));
                data.viewers = json.readValue(CoordFloatOrderedMap.class, jsonData.get("viewers"));
                return data;
            }
        });
    }

    /**
     * Registers PointPair with the given Json object, so PointPair can be written to and read from JSON.
     * This does not automatically register the type of {@link PointN} used for each member of the PointPair.
     * You may need to call {@link #registerCoord(Json)} or use the default serializer for a point type.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPointPair(@NonNull Json json) {
        json.addClassTag("PntP", PointPair.class);
        json.setSerializer(PointPair.class, new Json.Serializer<PointPair>() {
            @Override
            public void write(Json json, PointPair object, Class knownType) {
                json.writeObjectStart(PointPair.class, knownType);
                json.writeValue("a", object.a, null);
                json.writeValue("b", object.b, null);
                json.writeObjectEnd();
            }

            @Override
            public PointPair<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new PointPair<>(json.readValue("a", null, jsonData), json.readValue("b", null, jsonData));
            }
        });
    }

}
