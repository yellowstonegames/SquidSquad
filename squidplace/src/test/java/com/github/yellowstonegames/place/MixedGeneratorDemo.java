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
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.grid.PoissonDisk;

public class MixedGeneratorDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77);
        MixedGenerator gen = new MixedGenerator(40, 40, random, PoissonDisk.sampleRectangle(Coord.get(1,1), Coord.get(38, 38), 15, 20, random));
        gen.putCaveCarvers(1);
//        gen.putWalledBoxRoomCarvers(4);
//        gen.putWalledRoundRoomCarvers(1);
        char[][] map = gen.generate();
        map = LineTools.hashesToLines(new DungeonProcessor(40, 40, random).generate(map, gen.environment));
        DungeonTools.debugPrint(map);
    }
}
