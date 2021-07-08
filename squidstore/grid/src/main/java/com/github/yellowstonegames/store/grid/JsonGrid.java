package com.github.yellowstonegames.store.grid;

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
public final class JsonGrid {
    private JsonGrid() {
    }

    /**
     * Registers SquidGrid's classes with the given Json object, allowing it to read and write SquidGrid types.
     *
     * @param json a libGDX Json object that will have serializers registered for all SquidGrid types.
     */
    public static void registerAll(@Nonnull Json json) {
    }
}
