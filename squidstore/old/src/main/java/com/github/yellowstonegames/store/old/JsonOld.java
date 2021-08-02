package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.old.v300.*;

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public final class JsonOld {
    private JsonOld() {
    }

    /**
     * Registers SquidOld's classes with the given Json object, allowing it to read and write SquidOld types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidOld types.
     */
    public static void registerAll(@Nonnull Json json) {
        registerLightRNG(json);
        registerDiverRNG(json);
        registerGWTRNG(json);
        registerSilkRNG(json);
        registerLinnormRNG(json);
        JsonSupport.registerEnhancedRandom(json);
    }

    /**
     * Registers LightRNG with the given Json object, so LightRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightRNG(@Nonnull Json json) {
        json.addClassTag("#LigR", LightRNG.class);
        json.setSerializer(LightRNG.class, new Json.Serializer<LightRNG>() {
            @Override
            public void write(Json json, LightRNG object, Class knownType) {
                json.writeValue("#LigR`" + Long.toString(object.getState(), 36) + "`");
            }

            @Override
            public LightRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new LightRNG(state);
            }
        });
    }

    /**
     * Registers DiverRNG with the given Json object, so DiverRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDiverRNG(@Nonnull Json json) {
        json.addClassTag("#DivR", DiverRNG.class);
        json.setSerializer(DiverRNG.class, new Json.Serializer<DiverRNG>() {
            @Override
            public void write(Json json, DiverRNG object, Class knownType) {
                json.writeValue("#DivR`" + Long.toString(object.getState(), 36) + "`");
            }

            @Override
            public DiverRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new DiverRNG(state);
            }
        });
    }

    /**
     * Registers GWTRNG with the given Json object, so GWTRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerGWTRNG(@Nonnull Json json) {
        json.addClassTag("#GWTR", GWTRNG.class);
        json.setSerializer(GWTRNG.class, new Json.Serializer<GWTRNG>() {
            @Override
            public void write(Json json, GWTRNG object, Class knownType) {
                json.writeValue("#GWTR`" + Long.toString(object.getState(), 36) + "`");
            }

            @Override
            public GWTRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new GWTRNG(state);
            }
        });
    }

    /**
     * Registers LinnormRNG with the given Json object, so LinnormRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLinnormRNG(@Nonnull Json json) {
        json.addClassTag("#LinR", LinnormRNG.class);
        json.setSerializer(LinnormRNG.class, new Json.Serializer<LinnormRNG>() {
            @Override
            public void write(Json json, LinnormRNG object, Class knownType) {
                json.writeValue("#LinR`" + Long.toString(object.getState(), 36) + "`");
            }

            @Override
            public LinnormRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new LinnormRNG(state);
            }
        });
    }

    /**
     * Registers SilkRNG with the given Json object, so SilkRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSilkRNG(@Nonnull Json json) {
        json.addClassTag("#SilR", SilkRNG.class);
        json.setSerializer(SilkRNG.class, new Json.Serializer<SilkRNG>() {
            @Override
            public void write(Json json, SilkRNG object, Class knownType) {
                json.writeValue("#SilR`" + Long.toString(object.getState(), 36) + "`");
            }

            @Override
            public SilkRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new SilkRNG(state);
            }
        });
    }

}
