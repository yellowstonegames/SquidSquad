package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.TricycleRandom;
import com.github.yellowstonegames.core.*;

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public final class JsonCore {
    private JsonCore() {
    }

    /**
     * Registers JDKGDXDS' classes with the given Json object, allowing it to read and write JDKGDXDS types.
     *
     * @param json a libGDX Json object that will have serializers registered for all JDKGDXDS types.
     */
    public static void registerAll(@Nonnull Json json) {
        JsonSupport.registerDistinctRandom(json);
        JsonSupport.registerLaserRandom(json);
        JsonSupport.registerTricycleRandom(json);
        JsonSupport.registerFourWheelRandom(json);

        registerChar2D(json);
        registerInt2D(json);

        registerDiceRule(json);
        registerGapShuffler(json);
        registerHasher(json);
        registerWeightedTable(json);
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
     * Registers Dice.Rule with the given Json object, so Dice.Rule can be written to and read from JSON.
     * This registers serialization/deserialization for IntList as well, since Dice.Rule requires it.
     * <br>
     * Note that Dice itself can be serialized using Json without a serializer, as long as either the EnhancedRandom
     * implementation used with it has been registered, or {@link JsonSupport#registerAtomicLong(Json)} has been called.
     * Calling either {@link #registerAll(Json)} or {@link JsonSupport#registerAll(Json)} will also suffice.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDiceRule(@Nonnull Json json) {
        JsonSupport.registerIntList(json);
        json.setSerializer(Dice.Rule.class, new Json.Serializer<Dice.Rule>() {
            @Override
            public void write(Json json, Dice.Rule object, Class knownType) {
                json.writeObjectStart();
                json.writeValue(object.rollCode, object.instructions, IntList.class);
                json.writeObjectEnd();
            }

            @Override
            public Dice.Rule read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                Dice.Rule data = new Dice.Rule(jsonData.child.name);
                data.instructions = json.readValue(null, jsonData.child);
                return data;
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
            public GapShuffler read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new GapShuffler<>(json.readValue("items", null, jsonData.child), json.readValue("rng", null, jsonData.child), jsonData.getChild("idx").asInt(), true);
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
     * Registers Hasher with the given Json object, so Hasher can be written to and read from JSON.
     * This just stores the seed (which is a single {@code long}) as a base-36 String.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerHasher(@Nonnull Json json) {
        json.setSerializer(Hasher.class, new Json.Serializer<Hasher>() {
            @Override
            public void write(Json json, Hasher object, Class knownType) {
                json.writeValue(Long.toString(object.seed, 36));
            }

            @Override
            public Hasher read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return new Hasher(Long.valueOf(jsonData.asString(), 36));
            }
        });
    }
}
