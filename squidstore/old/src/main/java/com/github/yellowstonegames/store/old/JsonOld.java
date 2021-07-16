package com.github.yellowstonegames.store.old;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.github.tommyettinger.ds.ObjectObjectMap;
import com.github.tommyettinger.ds.interop.JsonSupport;
import com.github.tommyettinger.ds.support.EnhancedRandom;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.store.core.JsonCore;
import com.github.yellowstonegames.old.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

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
    }
}
