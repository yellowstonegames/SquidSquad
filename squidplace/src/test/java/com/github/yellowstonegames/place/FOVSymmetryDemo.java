/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
import com.github.yellowstonegames.grid.*;

public class FOVSymmetryDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77);
        DungeonProcessor gen = new DungeonProcessor(70, 70, random);
        gen.addBoulders(DungeonProcessor.ALL, 8);
        char[][] dungeon = gen.generate();
        System.out.println(gen);
        float[][] res = FOV.generateSimpleResistances(dungeon);
        float[][] light = new float[gen.width][gen.height];
        Region floorRegion = new Region(dungeon, '.'), working = floorRegion.copy();
        Coord[] floors = floorRegion.asCoords();
//        for (int i = 0; i < floors.length; i++) {
//            Coord center = floors[i];
//            for (int j = 0; j < floors.length; j++) {
//                if(i == j) continue;
//                Coord target = floors[j];
//                if(BresenhamLine.reachable(center, target, res) != BresenhamLine.reachable(target, center, res))
//                    System.out.println("Asymmetry between " + center + " and " + target);
//            }
//        }
        for (int i = 0; i < floors.length; i++) {
            Coord center = floors[i];
            FOV.reuseFOVSymmetrical(res, light, center.x, center.y, 7f, Radius.CIRCLE);
            working.refill(light, 0.001f, 100f).and(floorRegion);
            for(Coord c : working) {
                if(FOV.reuseFOVSymmetrical(res, light, c.x, c.y, 7f, Radius.CIRCLE)[center.x][center.y] <= 0f)
                {
                    System.out.println(working);
                    System.out.println("Not symmetrical between " + center + " and " + c + "!");
                    System.out.println(floorRegion.refill(light, 0.001f, 100f));
                    return;
                }
            }
        }
    }
}
