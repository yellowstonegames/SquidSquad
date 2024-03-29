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
import com.github.yellowstonegames.grid.LineTools;

public class SerpentMapGeneratorDemo {

    public static void main(String[] args) {
        final int width = 60, height = 60, depth = 8;
        AceRandom random = new AceRandom(0xB0BAFE77BA77L);
        SerpentMapGenerator gen = new SerpentMapGenerator(width, height, random, 0.07);
        gen.putWalledBoxRoomCarvers(4);
        gen.putCaveCarvers(1);
        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");

        random.setSeed(0xB0BAFE77BA77L);
        SerpentDeepMapGenerator deepSerpent = new SerpentDeepMapGenerator(width, height, depth, random, 0.15);
        deepSerpent.putWalledBoxRoomCarvers(2);
        deepSerpent.putWalledRoundRoomCarvers(2);
        deepSerpent.putCaveCarvers(3);
        char[][][] map3D = deepSerpent.generate();
        DungeonProcessor[] gens = new DungeonProcessor[depth];
        for (int i = 0; i < depth; i++) {
            System.out.println("--------------------------------------------------- depth " + i);
            gens[i] = new DungeonProcessor(width, height, random);
            gens[i].addWater(3, random.nextInt(25));
            gens[i].addGrass(0, random.nextInt(15));
            gens[i].addBoulders(3, random.nextInt(30));
            gens[i].addDoors(random.nextInt(4, 10), false);
            gens[i].generateRespectingStairs(map3D[i], deepSerpent.getEnvironment(i));
            gens[i].setPlaceGrid(LineTools.hashesToLines(gens[i].getPlaceGrid(), true));
            System.out.println(gens[i]);
        }

    }
}
