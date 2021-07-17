package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.yellowstonegames.old.v300.LightRNG;
import com.github.yellowstonegames.store.core.JsonCore;

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
                json.writeValue("#LigR`" + Long.toString(object.getSelectedState(0), 36) + "`");
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

}
