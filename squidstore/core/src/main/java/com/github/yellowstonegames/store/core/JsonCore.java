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

        registerEnhancedRandom(json);

        registerDiceRule(json);
        registerGapShuffler(json);
        registerHasher(json);
        registerWeightedTable(json);
    }

    /**
     * Registers FourWheelRandom with the given Json object, so FourWheelRandom can be written to and read from JSON.
     * This is (currently) different from the registration for this class in jdkgdxds-interop, because this needs to be
     * in a format that can be read into an EnhancedRandom value by using a type tag stored in the serialized JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerFourWheelRandom(@Nonnull Json json) {
        json.addClassTag("#FoWR", FourWheelRandom.class);
        json.setSerializer(FourWheelRandom.class, new Json.Serializer<FourWheelRandom>() {
            @Override
            public void write(Json json, FourWheelRandom object, Class knownType) {
                json.writeValue("#FoWR`" + Long.toString(object.getStateA(), 36) + "~" + Long.toString(object.getStateB(), 36) + "~" + Long.toString(object.getStateC(), 36) + "~" + Long.toString(object.getStateD(), 36) + "`");
            }

            @Override
            public FourWheelRandom read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 14) return null;
                int tilde = s.indexOf('~', 6);
                final long stateA = Long.parseLong(s.substring(6, tilde), 36);
                final long stateB = Long.parseLong(s.substring(tilde + 1, tilde = s.indexOf('~', tilde + 1)), 36);
                final long stateC = Long.parseLong(s.substring(tilde + 1, tilde = s.indexOf('~', tilde + 1)), 36);
                final long stateD = Long.parseLong(s.substring(tilde + 1, s.indexOf('`', tilde)), 36);
                return new FourWheelRandom(stateA, stateB, stateC, stateD);
            }
        });
    }

    /**
     * Registers TricycleRandom with the given Json object, so TricycleRandom can be written to and read from JSON.
     * This is (currently) different from the registration for this class in jdkgdxds-interop, because this needs to be
     * in a format that can be read into an EnhancedRandom value by using a type tag stored in the serialized JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerTricycleRandom(@Nonnull Json json) {
        json.addClassTag("#TriR", TricycleRandom.class);
        json.setSerializer(TricycleRandom.class, new Json.Serializer<TricycleRandom>() {
            @Override
            public void write(Json json, TricycleRandom object, Class knownType) {
                json.writeValue("#TriR`" + Long.toString(object.getStateA(), 36) + "~" + Long.toString(object.getStateB(), 36) + "~" + Long.toString(object.getStateC(), 36) + "`");
            }

            @Override
            public TricycleRandom read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 12) return null;
                int tilde = s.indexOf('~', 6);
                final long stateA = Long.parseLong(s.substring(6, tilde), 36);
                final long stateB = Long.parseLong(s.substring(tilde + 1, tilde = s.indexOf('~', tilde + 1)), 36);
                final long stateC = Long.parseLong(s.substring(tilde + 1, s.indexOf('`', tilde)), 36);
                return new TricycleRandom(stateA, stateB, stateC);
            }
        });
    }

    /**
     * Registers LaserRandom with the given Json object, so LaserRandom can be written to and read from JSON.
     * This is (currently) different from the registration for this class in jdkgdxds-interop, because this needs to be
     * in a format that can be read into an EnhancedRandom value by using a type tag stored in the serialized JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerLaserRandom(@Nonnull Json json) {
        json.addClassTag("#LasR", LaserRandom.class);
        json.setSerializer(LaserRandom.class, new Json.Serializer<LaserRandom>() {
            @Override
            public void write(Json json, LaserRandom object, Class knownType) {
                json.writeValue("#LasR`" + Long.toString(object.getStateA(), 36) + "~" + Long.toString(object.getStateB() >>> 1, 36) + "`");
            }

            @Override
            public LaserRandom read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 10) return null;
                final int tilde = s.indexOf('~', 6);
                final long stateA = Long.parseLong(s.substring(6, tilde), 36);
                final long stateB = Long.parseLong(s.substring(tilde + 1, s.indexOf('`', tilde)), 36) << 1;
                return new LaserRandom(stateA, stateB);
            }
        });
    }

    /**
     * Registers DistinctRandom with the given Json object, so DistinctRandom can be written to and read from JSON.
     *
     * @param json a libGDX Json object that will have a serializer registered
     */
    public static void registerDistinctRandom(@Nonnull Json json) {
        json.addClassTag("#DisR", DistinctRandom.class);
        json.setSerializer(DistinctRandom.class, new Json.Serializer<DistinctRandom>() {
            @Override
            public void write(Json json, DistinctRandom object, Class knownType) {
                json.writeValue("#DisR`" + Long.toString(object.getSelectedState(0), 36) + "`");
            }

            @Override
            public DistinctRandom read(Json json, JsonValue jsonData, Class type) {
                String s;
                if (jsonData == null || jsonData.isNull() || (s = jsonData.asString()) == null || s.length() < 8) return null;
                final int tick = s.indexOf('`', 6);
                final long state = Long.parseLong(s.substring(6, tick), 36);
                return new DistinctRandom(state);
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
    public static void registerEnhancedRandom(@Nonnull Json json) {
        JsonSupport.registerAtomicLong(json);
        registerDistinctRandom(json);
        registerLaserRandom(json);
        registerTricycleRandom(json);
        registerFourWheelRandom(json);
        json.setSerializer(EnhancedRandom.class, new Json.Serializer<EnhancedRandom>() {
            @Override
            public void write(Json json, EnhancedRandom object, Class knownType) {
                json.writeArrayStart();
                Class impl = object.getClass();
                String tag = json.getTag(impl);
                if(tag == null) tag = impl.getName();
                json.writeValue(tag);
                json.writeValue(object);
                json.writeArrayEnd();
            }

            @Override
            public EnhancedRandom read(Json json, JsonValue jsonData, Class type) {
                if (jsonData == null || jsonData.isNull()) return null;
                try {
                    String tag = jsonData.asString().substring(0, 5);
                    Class<?> impl = json.getClass(tag);
                    if(impl == null) impl = ClassReflection.forName(tag);
                    return (EnhancedRandom) json.readValue(impl, jsonData);
                } catch (ReflectionException | ClassCastException e) {
                    if(Gdx.app != null)
                        Gdx.app.error("squidstore", "Error reading an EnhancedRandom value from " + jsonData);
                    else
                        System.out.println("[squidstore] Error reading an EnhancedRandom value from " + jsonData);
                    return null;
                }
            }
        });
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
        registerEnhancedRandom(json);
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
                return new Hasher(Long.valueOf(jsonData.asString(), 36));
            }
        });
    }
}
