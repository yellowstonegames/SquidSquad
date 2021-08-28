package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.*;
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
        registerChar2D(json);
        registerInt2D(json);
        registerLong2D(json);
        registerFloat2D(json);

        JsonSupport.registerEnhancedRandom(json);

        registerDiceRule(json);
        registerGapShuffler(json);
        registerHasher(json);
        registerWeightedTable(json);
        registerBase(json);
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
                return new Hasher(Base.BASE36.readLong(jsonData.asString()));
            }
        });
    }


    /**
     * Registers Base with the given Json object, so Base can be written to and read from JSON.
     * This is a simple wrapper around Base's built-in {@link Base#serializeToString()} and
     * {@link Base#deserializeFromString(String)} methods.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerBase(@Nonnull Json json) {
        json.setSerializer(Base.class, new Json.Serializer<Base>() {
            @Override
            public void write(Json json, Base object, Class knownType) {
                json.writeValue(object.serializeToString());
            }

            @Override
            public Base read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                return Base.deserializeFromString(jsonData.asString());
            }
        });
    }

}
