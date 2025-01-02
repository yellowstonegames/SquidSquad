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
import com.github.yellowstonegames.world.*;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class JsonWorld {
    private JsonWorld() {
    }

    /**
     * Registers SquidWorld's classes with the given Json object, allowing it to read and write SquidWorld types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidWorld types.
     */
    public static void registerAll(@NonNull Json json) {
        registerEllipticalWorldMap(json);
        registerHexagonalWorldMap(json);
        registerHyperellipticalWorldMap(json);
        registerLatLonWorldMap(json);
        registerMimicWorldMap(json);
        registerRoundSideWorldMap(json);
        registerStretchWorldMap(json);
        registerTilingWorldMap(json);
    }
    
    /**
     * Registers EllipticalWorldMap with the given Json object, so EllipticalWorldMap can be written to and read from JSON.
     * This is a simple wrapper around EllipticalWorldMap's built-in {@link EllipticalWorldMap#stringSerialize()} and
     * {@link EllipticalWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerEllipticalWorldMap(@NonNull Json json) {
        json.addClassTag("EllW", EllipticalWorldMap.class);
        json.setSerializer(EllipticalWorldMap.class, new Json.Serializer<EllipticalWorldMap>() {
            @Override
            public void write(Json json, EllipticalWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public EllipticalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return EllipticalWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }
    
    /**
     * Registers GlobeMap with the given Json object, so GlobeMap can be written to and read from JSON.
     * This is a simple wrapper around GlobeMap's built-in {@link GlobeMap#stringSerialize()} and
     * {@link GlobeMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerGlobeMap(@NonNull Json json) {
        json.addClassTag("GlbW", GlobeMap.class);
        json.setSerializer(GlobeMap.class, new Json.Serializer<GlobeMap>() {
            @Override
            public void write(Json json, GlobeMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public GlobeMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return GlobeMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers HexagonalWorldMap with the given Json object, so HexagonalWorldMap can be written to and read from JSON.
     * This is a simple wrapper around HexagonalWorldMap's built-in {@link HexagonalWorldMap#stringSerialize()} and
     * {@link HexagonalWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerHexagonalWorldMap(@NonNull Json json) {
        json.addClassTag("HexW", HexagonalWorldMap.class);
        json.setSerializer(HexagonalWorldMap.class, new Json.Serializer<HexagonalWorldMap>() {
            @Override
            public void write(Json json, HexagonalWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public HexagonalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return HexagonalWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers HyperellipticalWorldMap with the given Json object, so HyperellipticalWorldMap can be written to and read from JSON.
     * This is a simple wrapper around HyperellipticalWorldMap's built-in {@link HyperellipticalWorldMap#stringSerialize()} and
     * {@link HyperellipticalWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerHyperellipticalWorldMap(@NonNull Json json) {
        json.addClassTag("HyeW", HyperellipticalWorldMap.class);
        json.setSerializer(HyperellipticalWorldMap.class, new Json.Serializer<HyperellipticalWorldMap>() {
            @Override
            public void write(Json json, HyperellipticalWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public HyperellipticalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return HyperellipticalWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers LatLonWorldMap with the given Json object, so LatLonWorldMap can be written to and read from JSON.
     * This is a simple wrapper around LatLonWorldMap's built-in {@link LatLonWorldMap#stringSerialize()} and
     * {@link LatLonWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLatLonWorldMap(@NonNull Json json) {
        json.addClassTag("LaLW", LatLonWorldMap.class);
        json.setSerializer(LatLonWorldMap.class, new Json.Serializer<LatLonWorldMap>() {
            @Override
            public void write(Json json, LatLonWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public LatLonWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return LatLonWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers LocalMap with the given Json object, so LocalMap can be written to and read from JSON.
     * This is a simple wrapper around LocalMap's built-in {@link LocalMap#stringSerialize()} and
     * {@link LocalMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLocalMap(@NonNull Json json) {
        json.addClassTag("LocW", LocalMap.class);
        json.setSerializer(LocalMap.class, new Json.Serializer<LocalMap>() {
            @Override
            public void write(Json json, LocalMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public LocalMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return LocalMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers MimicLocalMap with the given Json object, so MimicLocalMap can be written to and read from JSON.
     * This is a simple wrapper around MimicLocalMap's built-in {@link MimicLocalMap#stringSerialize()} and
     * {@link MimicLocalMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerMimicLocalMap(@NonNull Json json) {
        json.addClassTag("MmLW", MimicLocalMap.class);
        json.setSerializer(MimicLocalMap.class, new Json.Serializer<MimicLocalMap>() {
            @Override
            public void write(Json json, MimicLocalMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public MimicLocalMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return MimicLocalMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers MimicWorldMap with the given Json object, so MimicWorldMap can be written to and read from JSON.
     * This is a simple wrapper around MimicWorldMap's built-in {@link MimicWorldMap#stringSerialize()} and
     * {@link MimicWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerMimicWorldMap(@NonNull Json json) {
        json.addClassTag("MmWW", MimicWorldMap.class);
        json.setSerializer(MimicWorldMap.class, new Json.Serializer<MimicWorldMap>() {
            @Override
            public void write(Json json, MimicWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public MimicWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return MimicWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers RotatingGlobeMap with the given Json object, so RotatingGlobeMap can be written to and read from JSON.
     * This is a simple wrapper around RotatingGlobeMap's built-in {@link RotatingGlobeMap#stringSerialize()} and
     * {@link RotatingGlobeMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRotatingGlobeMap(@NonNull Json json) {
        json.addClassTag("RoGW", RotatingGlobeMap.class);
        json.setSerializer(RotatingGlobeMap.class, new Json.Serializer<RotatingGlobeMap>() {
            @Override
            public void write(Json json, RotatingGlobeMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public RotatingGlobeMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return RotatingGlobeMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers RoundSideWorldMap with the given Json object, so RoundSideWorldMap can be written to and read from JSON.
     * This is a simple wrapper around RoundSideWorldMap's built-in {@link RoundSideWorldMap#stringSerialize()} and
     * {@link RoundSideWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerRoundSideWorldMap(@NonNull Json json) {
        json.addClassTag("RoSW", RoundSideWorldMap.class);
        json.setSerializer(RoundSideWorldMap.class, new Json.Serializer<RoundSideWorldMap>() {
            @Override
            public void write(Json json, RoundSideWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public RoundSideWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return RoundSideWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers StretchWorldMap with the given Json object, so StretchWorldMap can be written to and read from JSON.
     * This is a simple wrapper around StretchWorldMap's built-in {@link StretchWorldMap#stringSerialize()} and
     * {@link StretchWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerStretchWorldMap(@NonNull Json json) {
        json.addClassTag("SteW", StretchWorldMap.class);
        json.setSerializer(StretchWorldMap.class, new Json.Serializer<StretchWorldMap>() {
            @Override
            public void write(Json json, StretchWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public StretchWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return StretchWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers TilingWorldMap with the given Json object, so TilingWorldMap can be written to and read from JSON.
     * This is a simple wrapper around TilingWorldMap's built-in {@link TilingWorldMap#stringSerialize()} and
     * {@link TilingWorldMap#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTilingWorldMap(@NonNull Json json) {
        json.addClassTag("TilW", TilingWorldMap.class);
        json.setSerializer(TilingWorldMap.class, new Json.Serializer<TilingWorldMap>() {
            @Override
            public void write(Json json, TilingWorldMap object, Class knownType) {
                json.writeValue(object.stringSerialize());
            }

            @Override
            public TilingWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return TilingWorldMap.recreateFromString(jsonData.asString());
            }
        });
    }
}
