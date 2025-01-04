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
import com.github.yellowstonegames.store.core.JsonCore;
import com.github.yellowstonegames.world.*;
import com.github.yellowstonegames.world.BiomeMapper.*;
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
        registerWorldMapGenerators(json);

        registerSimpleBiomeMapper(json);
        registerDetailedBiomeMapper(json);
        registerBlendedBiomeMapper(json);
        registerUnrealisticBiomeMapper(json);

        registerDetailedWorldMapView(json);
    }

    public static void registerWorldMapGenerators(@NonNull Json json) {
        registerEllipticalWorldMap(json);
        registerGlobeMap(json);
        registerHexagonalWorldMap(json);
        registerHyperellipticalWorldMap(json);
        registerLatLonWorldMap(json);
        registerLocalMap(json);
        registerMimicLocalMap(json);
        registerMimicWorldMap(json);
        registerRotatingGlobeMap(json);
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
                json.writeObjectStart(EllipticalWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public EllipticalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return EllipticalWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(GlobeMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public GlobeMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return GlobeMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(HexagonalWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public HexagonalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return HexagonalWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(HyperellipticalWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public HyperellipticalWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return HyperellipticalWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(LatLonWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public LatLonWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return LatLonWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(LocalMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public LocalMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return LocalMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(MimicLocalMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public MimicLocalMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return MimicLocalMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(MimicWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public MimicWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return MimicWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(RotatingGlobeMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public RotatingGlobeMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return RotatingGlobeMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(RoundSideWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public RoundSideWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return RoundSideWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(StretchWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public StretchWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return StretchWorldMap.recreateFromString(jsonData.getString("v"));
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
                json.writeObjectStart(TilingWorldMap.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public TilingWorldMap read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return TilingWorldMap.recreateFromString(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers SimpleBiomeMapper with the given Json object, so SimpleBiomeMapper can be written to and read from JSON.
     * This is a simple wrapper around SimpleBiomeMapper's built-in {@link SimpleBiomeMapper#stringSerialize()} and
     * {@link SimpleBiomeMapper#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSimpleBiomeMapper(@NonNull Json json) {
        json.addClassTag("SiBM", SimpleBiomeMapper.class);
        json.setSerializer(SimpleBiomeMapper.class, new Json.Serializer<SimpleBiomeMapper>() {
            @Override
            public void write(Json json, SimpleBiomeMapper object, Class knownType) {
                json.writeObjectStart(SimpleBiomeMapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public SimpleBiomeMapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return SimpleBiomeMapper.recreateFromString(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers DetailedBiomeMapper with the given Json object, so DetailedBiomeMapper can be written to and read from JSON.
     * This is a Detailed wrapper around DetailedBiomeMapper's built-in {@link DetailedBiomeMapper#stringSerialize()} and
     * {@link DetailedBiomeMapper#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDetailedBiomeMapper(@NonNull Json json) {
        json.addClassTag("DeBM", DetailedBiomeMapper.class);
        json.setSerializer(DetailedBiomeMapper.class, new Json.Serializer<DetailedBiomeMapper>() {
            @Override
            public void write(Json json, DetailedBiomeMapper object, Class knownType) {
                json.writeObjectStart(DetailedBiomeMapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public DetailedBiomeMapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return DetailedBiomeMapper.recreateFromString(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers BlendedBiomeMapper with the given Json object, so BlendedBiomeMapper can be written to and read from JSON.
     * This is a Blended wrapper around BlendedBiomeMapper's built-in {@link BlendedBiomeMapper#stringSerialize()} and
     * {@link BlendedBiomeMapper#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerBlendedBiomeMapper(@NonNull Json json) {
        json.addClassTag("DeBM", BlendedBiomeMapper.class);
        json.setSerializer(BlendedBiomeMapper.class, new Json.Serializer<BlendedBiomeMapper>() {
            @Override
            public void write(Json json, BlendedBiomeMapper object, Class knownType) {
                json.writeObjectStart(BlendedBiomeMapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public BlendedBiomeMapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return BlendedBiomeMapper.recreateFromString(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers UnrealisticBiomeMapper with the given Json object, so UnrealisticBiomeMapper can be written to and read from JSON.
     * This is a Unrealistic wrapper around UnrealisticBiomeMapper's built-in {@link UnrealisticBiomeMapper#stringSerialize()} and
     * {@link UnrealisticBiomeMapper#recreateFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerUnrealisticBiomeMapper(@NonNull Json json) {
        json.addClassTag("DeBM", UnrealisticBiomeMapper.class);
        json.setSerializer(UnrealisticBiomeMapper.class, new Json.Serializer<UnrealisticBiomeMapper>() {
            @Override
            public void write(Json json, UnrealisticBiomeMapper object, Class knownType) {
                json.writeObjectStart(UnrealisticBiomeMapper.class, knownType);
                json.writeValue("v", object.stringSerialize());
                json.writeObjectEnd();
            }

            @Override
            public UnrealisticBiomeMapper read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("v")) return null;
                return UnrealisticBiomeMapper.recreateFromString(jsonData.getString("v"));
            }
        });
    }

    /**
     * Registers DetailedWorldMapView with the given Json object, so DetailedWorldMapView can be written to and read from JSON.
     * This is a simple wrapper around the serialization for the WorldMapGenerator and BiomeMapper used here.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDetailedWorldMapView(@NonNull Json json) {
        json.addClassTag("DeWV", DetailedWorldMapView.class);
        registerDetailedBiomeMapper(json);
        registerWorldMapGenerators(json);
        JsonCore.registerInt2D(json);
        json.setSerializer(DetailedWorldMapView.class, new Json.Serializer<DetailedWorldMapView>() {
            @Override
            public void write(Json json, DetailedWorldMapView object, Class knownType) {
                json.writeObjectStart(DetailedWorldMapView.class, knownType);
                json.writeValue("w", object.getWorld(), null);
                json.writeValue("m", object.getBiomeMapper(), DetailedBiomeMapper.class);
                json.writeValue("r", object.getColorMap(), int[][].class);
                json.writeValue("o", object.getColorMapOklab(), int[][].class);
                json.writeValue("C", object.BIOME_COLOR_TABLE, int[].class);
                json.writeValue("D", object.BIOME_DARK_COLOR_TABLE, int[].class);
                json.writeObjectEnd();
            }

            @Override
            public DetailedWorldMapView read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("w") || !jsonData.has("m")) return null;
                DetailedWorldMapView wmv = new DetailedWorldMapView();
                wmv.setWorld(json.readValue("w", null, jsonData));
                wmv.setBiomeMapper(json.readValue("m", DetailedBiomeMapper.class, jsonData));
                wmv.setColorMap(json.readValue("r", int[][].class, jsonData));
                wmv.setColorMapOklab(json.readValue("o", int[][].class, jsonData));
                System.arraycopy(json.readValue("C", int[].class, jsonData), 0, wmv.BIOME_COLOR_TABLE, 0, 66);
                System.arraycopy(json.readValue("D", int[].class, jsonData), 0, wmv.BIOME_DARK_COLOR_TABLE, 0, 66);

                return wmv;
            }
        });
    }

    /**
     * Registers BlendedWorldMapView with the given Json object, so BlendedWorldMapView can be written to and read from JSON.
     * This is a simple wrapper around the serialization for the WorldMapGenerator and BiomeMapper used here.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerBlendedWorldMapView(@NonNull Json json) {
        json.addClassTag("DeWV", BlendedWorldMapView.class);
        registerBlendedBiomeMapper(json);
        registerWorldMapGenerators(json);
        json.setSerializer(BlendedWorldMapView.class, new Json.Serializer<BlendedWorldMapView>() {
            @Override
            public void write(Json json, BlendedWorldMapView object, Class knownType) {
                json.writeObjectStart(BlendedWorldMapView.class, knownType);
                json.writeValue("w", object.getWorld(), null);
                json.writeValue("m", object.getBiomeMapper(), BlendedBiomeMapper.class);
                json.writeObjectEnd();
            }

            @Override
            public BlendedWorldMapView read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull() || !jsonData.has("w") || !jsonData.has("m")) return null;
                BlendedWorldMapView wmv = new BlendedWorldMapView();
                wmv.setWorld(json.readValue("w", null, jsonData));
                wmv.setBiomeMapper(json.readValue("m", BlendedBiomeMapper.class, jsonData));
                return wmv;
            }
        });
    }
}
