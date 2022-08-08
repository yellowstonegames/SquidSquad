/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.NumberedSet;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.TricycleRandom;
import com.github.yellowstonegames.core.Dice;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.DistributedRandom;
import com.github.yellowstonegames.core.GapShuffler;
import com.github.yellowstonegames.core.IntShuffler;
import com.github.yellowstonegames.core.ProbabilityTable;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.core.WeightedTable;
import regexodus.Pattern;

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public final class JsonCore {
    private JsonCore() {
    }

    /**
     * Registers the classes from jdkgdxds and from SquidSquad with the given Json object, allowing it to read and write
     * types from both libraries.
     *
     * @param json a libGDX Json object that will have serializers registered for all jdkgdxds and SquidSquad types this is aware of
     */
    public static void registerAll(@Nonnull Json json) {
        registerChar2D(json);
        registerInt2D(json);
        registerLong2D(json);
        registerFloat2D(json);

        JsonSupport.registerAll(json);

        registerDiceRule(json);
        registerGapShuffler(json);
        registerWeightedTable(json);
        registerIntShuffler(json);
        registerDistributedRandom(json);

        registerPattern(json);
    }

    /**
     * Registers char[][] with the given Json object, so char[][] can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerChar2D(@Nonnull Json json) {
        json.setSerializer(char[][].class, new Json.Serializer<char[][]>() {
            @Override
            public void write(Json json, char[][] object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                int sz = object.length;
                json.writeArrayStart();
                for (int i = 0; i < sz; i++) {
                    json.writeValue(String.valueOf(object[i]));
                }
                json.writeArrayEnd();
            }

            @Override
            public char[][] read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                int sz = jsonData.size;
                char[][] data = new char[sz][];
                JsonValue c = jsonData.child();
                for (int i = 0; i < sz && c != null; i++, c = c.next()) {
                    data[i] = c.asString().toCharArray();
                }
                return data;
            }
        });
    }

    /**
     * Registers int[][] with the given Json object, so int[][] can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerInt2D(@Nonnull Json json) {
        json.setSerializer(int[][].class, new Json.Serializer<int[][]>() {
            @Override
            public void write(Json json, int[][] object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                int sz = object.length;
                json.writeArrayStart();
                for (int i = 0; i < sz; i++) {
                    json.writeValue(StringTools.join("&", object[i]));
                }
                json.writeArrayEnd();
            }

            @Override
            public int[][] read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                int sz = jsonData.size;
                int[][] data = new int[sz][];
                JsonValue c = jsonData.child();
                for (int i = 0; i < sz && c != null; i++, c = c.next()) {
                    data[i] = DigitTools.splitIntFromDec(c.asString(), "&");
                }
                return data;
            }
        });
    }

    /**
     * Registers float[][] with the given Json object, so float[][] can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFloat2D(@Nonnull Json json) {
        json.setSerializer(float[][].class, new Json.Serializer<float[][]>() {
            @Override
            public void write(Json json, float[][] object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                int sz = object.length;
                json.writeArrayStart();
                for (int i = 0; i < sz; i++) {
                    json.writeValue(DigitTools.joinFloatsBits("&", object[i]));
                }
                json.writeArrayEnd();
            }

            @Override
            public float[][] read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                int sz = jsonData.size;
                float[][] data = new float[sz][];
                JsonValue c = jsonData.child();
                for (int i = 0; i < sz && c != null; i++, c = c.next()) {
                    data[i] = DigitTools.splitFloatFromBits(c.asString(), "&");
                }
                return data;
            }
        });
    }

    /**
     * Registers long[][] with the given Json object, so long[][] can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLong2D(@Nonnull Json json) {
        json.setSerializer(long[][].class, new Json.Serializer<long[][]>() {
            @Override
            public void write(Json json, long[][] object, Class knownType) {
                if(object == null)
                {
                    json.writeValue(null);
                    return;
                }
                int sz = object.length;
                json.writeArrayStart();
                for (int i = 0; i < sz; i++) {
                    json.writeValue(StringTools.join("&", object[i]));
                }
                json.writeArrayEnd();
            }

            @Override
            public long[][] read(Json json, JsonValue jsonData, Class type) {
                if(jsonData == null || jsonData.isNull())
                    return null;
                int sz = jsonData.size;
                long[][] data = new long[sz][];
                JsonValue c = jsonData.child();
                for (int i = 0; i < sz && c != null; i++, c = c.next()) {
                    data[i] = DigitTools.splitLongFromDec(c.asString(), "&");
                }
                return data;
            }
        });
    }

    /**
     * Registers Dice.Rule with the given Json object, so Dice.Rule can be written to and read from JSON.
     * This stores a Rule succinctly, using the String representation of its rollCode only.
     * <br>
     * Note that Dice itself can be serialized using Json without a serializer, as long as either the EnhancedRandom
     * implementation used with it has been registered, or {@link JsonSupport#registerAtomicLong(Json)} has been called.
     * Calling either {@link #registerAll(Json)} or {@link JsonSupport#registerAll(Json)} will also suffice.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDiceRule(@Nonnull Json json) {
        json.setSerializer(Dice.Rule.class, new Json.Serializer<Dice.Rule>() {
            @Override
            public void write(Json json, Dice.Rule object, Class knownType) {
                json.writeValue(object.rollCode);
            }

            @Override
            public Dice.Rule read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Dice.Rule(jsonData.asString());
            }
        });
    }

    /**
     * Registers GapShuffler with the given Json object, so GapShuffler can be written to and read from JSON.
     * This registers serialization/deserialization for ObjectList as well, since GapShuffler requires it.
     * You should either register the EnhancedRandom you use with this (which is {@link TricycleRandom} if unspecified),
     * use {@link JsonSupport#registerAtomicLong(Json)} (if you don't know what type the random number generator uses),
     * or just call {@link #registerAll(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerGapShuffler(@Nonnull Json json) {
        JsonSupport.registerEnhancedRandom(json);
        JsonSupport.registerObjectList(json);
        json.setSerializer(GapShuffler.class, new Json.Serializer<GapShuffler>() {
            @Override
            public void write(Json json, GapShuffler object, Class knownType) {
                json.writeObjectStart();
                json.writeValue("rng", object.random, null);
                ObjectList items = new ObjectList();
                object.fillInto(items);
                json.writeValue("items", items, null);
                json.writeValue("idx", object.getIndex());
                json.writeObjectEnd();
            }

            @Override
            public GapShuffler<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new GapShuffler<>(json.readValue("items", ObjectList.class, jsonData), json.readValue("rng", EnhancedRandom.class, jsonData), jsonData.get("idx").asInt(), true, false);
            }
        });
    }

    /**
     * Registers ProbabilityTable with the given Json object, so ProbabilityTable can be written to and read from JSON.
     * This registers serialization/deserialization for ObjectList as well, since ProbabilityTable requires it.
     * You should either register the EnhancedRandom you use with this (which is {@link TricycleRandom} if unspecified),
     * use {@link JsonSupport#registerAtomicLong(Json)} (if you don't know what type the random number generator uses),
     * or just call {@link #registerAll(Json)}.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerProbabilityTable(@Nonnull Json json) {
        JsonSupport.registerEnhancedRandom(json);
        JsonSupport.registerObjectList(json);
        JsonSupport.registerIntList(json);
        JsonSupport.registerNumberedSet(json);
        json.addClassTag("#PrbT", ProbabilityTable.class);
        json.setSerializer(ProbabilityTable.class, new Json.Serializer<ProbabilityTable>() {
            @Override
            public void write(Json json, ProbabilityTable object, Class knownType) {
                json.writeObjectStart();
                json.writeType(ProbabilityTable.class);
                json.writeValue("rng", object.rng, null);
                json.writeValue("table", object.table, NumberedSet.class);
                json.writeValue("extra", object.extraTable, ObjectList.class, ProbabilityTable.class);
                json.writeValue("weight", object.weights, IntList.class);
                json.writeObjectEnd();
            }

            @Override
            public ProbabilityTable<?> read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                ProbabilityTable pt = new ProbabilityTable<>(json.readValue("rng", EnhancedRandom.class, jsonData));
                NumberedSet ns = json.readValue("table", NumberedSet.class, jsonData);
                ObjectList ex = json.readValue("extra", ObjectList.class, ProbabilityTable.class, jsonData);
                IntList wt = json.readValue("weight", IntList.class, jsonData);
                for (int i = 0; i < ns.size(); i++) {
                    pt.add(ns.getAt(i), wt.get(i));
                }
                for (int i = 0, w = ns.size(); i < ex.size(); i++, w++) {
                    pt.add((ProbabilityTable) ex.get(i), wt.get(w));
                }
                return pt;
            }
        });
    }

    /**
     * Registers WeightedTable with the given Json object, so WeightedTable can be written to and read from JSON.
     * This is a simple wrapper around WeightedTable's built-in {@link WeightedTable#serializeToString()} and
     * {@link WeightedTable#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerWeightedTable(@Nonnull Json json) {
        json.setSerializer(WeightedTable.class, new Json.Serializer<WeightedTable>() {
            @Override
            public void write(Json json, WeightedTable object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public WeightedTable read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return WeightedTable.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers IntShuffler with the given Json object, so IntShuffler can be written to and read from JSON.
     * This is a simple wrapper around IntShuffler's built-in {@link IntShuffler#serializeToString()} and
     * {@link IntShuffler#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerIntShuffler(@Nonnull Json json) {
        json.setSerializer(IntShuffler.class, new Json.Serializer<IntShuffler>() {
            @Override
            public void write(Json json, IntShuffler object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public IntShuffler read(Json json, JsonValue jsonData, Class type) {
                return IntShuffler.deserializeFromString(jsonData.asString());
            }
        });
    }

    /**
     * Registers DistributedRandom with the given Json object, so DistributedRandom can be written to and read from JSON.
     * This is a simple wrapper around DistributedRandom's built-in {@link DistributedRandom#stringSerialize(Base)} and
     * {@link DistributedRandom#stringDeserialize(String, Base)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDistributedRandom(@Nonnull Json json) {
        JsonSupport.registerEnhancedRandom(json);
        JsonSupport.registerDistribution(json);
        json.setSerializer(DistributedRandom.class, new Json.Serializer<DistributedRandom>() {
            @Override
            public void write(Json json, DistributedRandom object, Class knownType) {
                json.writeValue(object.stringSerialize(JsonSupport.getNumeralBase()));
            }

            @Override
            public DistributedRandom read(Json json, JsonValue jsonData, Class type) {
                return new DistributedRandom().stringDeserialize(jsonData.asString(), JsonSupport.getNumeralBase());
            }
        });
    }

    /**
     * Registers Pattern with the given Json object, so Pattern can be written to and read from JSON.
     * This is a simple wrapper around Pattern's built-in {@link Pattern#serializeToString()} and
     * {@link Pattern#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerPattern(@Nonnull Json json) {
        json.setSerializer(Pattern.class, new Json.Serializer<Pattern>() {
            @Override
            public void write(Json json, Pattern object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Pattern read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Pattern.deserializeFromString(jsonData.asString());
            }
        });
    }

}
