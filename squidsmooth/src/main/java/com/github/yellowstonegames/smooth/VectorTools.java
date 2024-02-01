/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.EnhancedRandom;

/**
 * Utilities for handling floating-point positions, to be added to as needed.
 */
public class VectorTools {
    /**
     * Generates one random int and uses it as an angle to fill receiving's x and y with a position on the unit circle.
     * @param receiving a Vector2 that will be completely modified
     * @param random any EnhancedRandom object
     * @return receiving, after modification
     */
    public static Vector2 randomUnit(Vector2 receiving, EnhancedRandom random) {
        final int index = random.next(14);
        return receiving.set(TrigTools.SIN_TABLE[index], TrigTools.SIN_TABLE[index + 4096 & TrigTools.TABLE_MASK]);
    }
    private static final StringBuilder sb = new StringBuilder(50);

    /**
     * Appends the serialized representation of the given Vector2 to the given StringBuilder.
     * @param sb a non-null StringBuilder
     * @param vec a non-null Vector2 to serialize
     * @return sb, after modifications
     */
    public static StringBuilder appendSerializedString(StringBuilder sb, Vector2 vec) {
        return Base.BASE10.appendFriendly(Base.BASE10.appendFriendly(sb, vec.x).append('~'), vec.y);
    }

    /**
     * Serializes the given Vector2 and returns it as a new String.
     * @param vec a non-null Vector2 to serialize
     * @return the serialized String version of vec
     */
    public static String serializeString(Vector2 vec) {
        sb.setLength(0);
        return appendSerializedString(sb, vec).toString();
    }

    /**
     * Deserializes the given String to get the values this assigns to the Vector2 {@code receiving}.
     * @param data a String containing a serialized Vector2
     * @param receiving a non-null Vector2 that will have its contents reassigned
     * @return receiving, after modifications
     */
    public static Vector2 deserializeString(String data, Vector2 receiving) {
        int idx = data.indexOf('~');
        receiving.x = Base.BASE10.readFloat(data, 0, idx);
        receiving.y = Base.BASE10.readFloat(data, idx+1, data.length());
        return receiving;
    }

    /**
     * Deserializes a Vector2 from the given String using {@link #deserializeString(String, Vector2)}, allocating a new
     * Vector2 that this returns.
     * @param data a String containing a serialized Vector2
     * @return a new Vector2 using the values from data
     */
    public static Vector2 deserializeString(String data) {
        return deserializeString(data, new Vector2());
    }
}
