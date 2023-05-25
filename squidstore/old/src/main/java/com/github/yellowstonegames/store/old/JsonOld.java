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

package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.old.v300.*;

import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("rawtypes")
public final class JsonOld {
    private JsonOld() {
    }

    /**
     * Registers SquidOld's classes with the given Json object, allowing it to read and write SquidOld types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidOld types.
     */
    public static void registerAll(@NonNull Json json) {
        registerLightRNG(json);
        registerDiverRNG(json);
        registerGWTRNG(json);
        registerSilkRNG(json);
        registerLinnormRNG(json);
        registerThrustAltRNG(json);
        registerLongPeriodRNG(json);
        registerXoshiroStarPhi32RNG(json);
        JsonSupport.registerEnhancedRandom(json);
    }

    /**
     * Registers LightRNG with the given Json object, so LightRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLightRNG(@NonNull Json json) {
        json.addClassTag("LigR", LightRNG.class);
        json.setSerializer(LightRNG.class, new Json.Serializer<LightRNG>() {
            @Override
            public void write(Json json, LightRNG object, Class knownType) {
                json.writeValue("LigR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public LightRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new LightRNG(state);
            }
        });
    }

    /**
     * Registers DiverRNG with the given Json object, so DiverRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDiverRNG(@NonNull Json json) {
        json.addClassTag("DivR", DiverRNG.class);
        json.setSerializer(DiverRNG.class, new Json.Serializer<DiverRNG>() {
            @Override
            public void write(Json json, DiverRNG object, Class knownType) {
                json.writeValue("DivR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public DiverRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new DiverRNG(state);
            }
        });
    }

    /**
     * Registers GWTRNG with the given Json object, so GWTRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerGWTRNG(@NonNull Json json) {
        json.addClassTag("GWTR", GWTRNG.class);
        json.setSerializer(GWTRNG.class, new Json.Serializer<GWTRNG>() {
            @Override
            public void write(Json json, GWTRNG object, Class knownType) {
                json.writeValue("GWTR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public GWTRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new GWTRNG(state);
            }
        });
    }

    /**
     * Registers LinnormRNG with the given Json object, so LinnormRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLinnormRNG(@NonNull Json json) {
        json.addClassTag("LinR", LinnormRNG.class);
        json.setSerializer(LinnormRNG.class, new Json.Serializer<LinnormRNG>() {
            @Override
            public void write(Json json, LinnormRNG object, Class knownType) {
                json.writeValue("LinR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public LinnormRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new LinnormRNG(state);
            }
        });
    }

    /**
     * Registers SilkRNG with the given Json object, so SilkRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerSilkRNG(@NonNull Json json) {
        json.addClassTag("SilR", SilkRNG.class);
        json.setSerializer(SilkRNG.class, new Json.Serializer<SilkRNG>() {
            @Override
            public void write(Json json, SilkRNG object, Class knownType) {
                json.writeValue("SilR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public SilkRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new SilkRNG(state);
            }
        });
    }

    /**
     * Registers ThrustAltRNG with the given Json object, so ThrustAltRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerThrustAltRNG(@NonNull Json json) {
        json.addClassTag("ThAR", ThrustAltRNG.class);
        json.setSerializer(ThrustAltRNG.class, new Json.Serializer<ThrustAltRNG>() {
            @Override
            public void write(Json json, ThrustAltRNG object, Class knownType) {
                json.writeValue("ThAR`" + JsonSupport.getNumeralBase().unsigned(object.getState()) + "`");
            }

            @Override
            public ThrustAltRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 7) return null;
                final int tick = s.indexOf('`', 5);
                final long state = JsonSupport.getNumeralBase().readLong(s, 5, tick);
                return new ThrustAltRNG(state);
            }
        });
    }

    /**
     * Registers LongPeriodRNG with the given Json object, so LongPeriodRNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLongPeriodRNG(@NonNull Json json) {
        json.addClassTag("LPeR", LongPeriodRNG.class);
        json.setSerializer(LongPeriodRNG.class, new Json.Serializer<LongPeriodRNG>() {
            @Override
            public void write(Json json, LongPeriodRNG object, Class knownType) {
                StringBuilder sb = new StringBuilder(280);
                JsonSupport.getNumeralBase().appendUnsigned(sb, object.choice);
                for (int i = 0; i < 16; i++) {
                    sb.append('~');
                    JsonSupport.getNumeralBase().appendUnsigned(sb, object.state[i]);
                }
                json.writeValue("LPeR`" + sb + "`");
            }

            @Override
            public LongPeriodRNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 39) return null;
                int delim = 5;
                LongPeriodRNG rng = new LongPeriodRNG(1L);
                rng.choice = JsonSupport.getNumeralBase().readInt(s, delim, delim = s.indexOf('~', delim + 1));
                for (int i = 0; i < 16; i++) {
                    rng.state[i] = JsonSupport.getNumeralBase().readLong(s, delim + 1, delim = s.indexOf('~', delim + 1));
                }
                return rng;
            }
        });
    }

    /**
     * Registers XoshiroStarPhi32RNG with the given Json object, so XoshiroStarPhi32RNG can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerXoshiroStarPhi32RNG(@NonNull Json json) {
        json.addClassTag("XSPR", XoshiroStarPhi32RNG.class);
        json.setSerializer(XoshiroStarPhi32RNG.class, new Json.Serializer<XoshiroStarPhi32RNG>() {
            @Override
            public void write(Json json, XoshiroStarPhi32RNG object, Class knownType) {
                StringBuilder sb = new StringBuilder(67);
                JsonSupport.getNumeralBase().appendUnsigned(sb, object.getStateA());
                sb.append('~');
                JsonSupport.getNumeralBase().appendUnsigned(sb, object.getStateB());
                sb.append('~');
                JsonSupport.getNumeralBase().appendUnsigned(sb, object.getStateC());
                sb.append('~');
                JsonSupport.getNumeralBase().appendUnsigned(sb, object.getStateD());
                json.writeValue("XSPR`" + sb + "`");
            }

            @Override
            public XoshiroStarPhi32RNG read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 13) return null;
                int delim = 5;
                int stateA = JsonSupport.getNumeralBase().readInt(s, delim, delim = s.indexOf('~', delim + 1));
                int stateB = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('~', delim + 1));
                int stateC = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('~', delim + 1));
                int stateD = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('`', delim + 1));
                return new XoshiroStarPhi32RNG(stateA, stateB, stateC, stateD);
            }
        });
    }


    /**
     * Registers LowStorageShuffler with the given Json object, so LowStorageShuffler can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLowStorageShuffler(@NonNull Json json) {
        json.addClassTag("LSSh", LowStorageShuffler.class);
        json.setSerializer(LowStorageShuffler.class, new Json.Serializer<LowStorageShuffler>() {
            @Override
            public void write(Json json, LowStorageShuffler object, Class knownType) {
                StringBuilder sb = new StringBuilder(64);
                sb.append("LSSh`");
                JsonSupport.getNumeralBase().appendSigned(sb, object.getBound());
                sb.append('~');
                JsonSupport.getNumeralBase().appendSigned(sb, object.getKey0());
                sb.append('~');
                JsonSupport.getNumeralBase().appendSigned(sb, object.getKey1());
                sb.append('~');
                JsonSupport.getNumeralBase().appendSigned(sb, object.getIndex());
                sb.append('`');
                json.writeValue(sb.toString());
            }

            @Override
            public LowStorageShuffler read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 13) return null;
                int delim = 5;
                int bound = JsonSupport.getNumeralBase().readInt(s, delim, delim = s.indexOf('~', delim + 1));
                int key0  = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('~', delim + 1));
                int key1  = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('~', delim + 1));
                int index = JsonSupport.getNumeralBase().readInt(s, delim + 1, delim = s.indexOf('`', delim + 1));
                return new LowStorageShuffler(bound, key0, key1).setIndex(index);
            }
        });
    }

}
