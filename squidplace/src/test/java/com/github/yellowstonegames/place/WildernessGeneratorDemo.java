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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.AceRandom;

public class WildernessGeneratorDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom();
        WildernessGenerator gen = new WildernessGenerator(40, 40, Biome.TABLE[random.nextInt(42)], random);
        DungeonTools.debugPrint(gen.generate());
    }
}
