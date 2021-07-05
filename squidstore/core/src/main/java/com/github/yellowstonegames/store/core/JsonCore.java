package com.github.yellowstonegames.store.core;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.yellowstonegames.core.Dice;
import com.github.yellowstonegames.core.WeightedTable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.PrimitiveIterator;

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

        registerDiceRule(json);
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
}
