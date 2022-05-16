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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Vector2;
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
}
