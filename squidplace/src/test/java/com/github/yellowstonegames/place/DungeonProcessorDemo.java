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
import com.github.yellowstonegames.place.tileset.TilesetType;

public class DungeonProcessorDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77BA77L);
        DungeonProcessor gen = new DungeonProcessor(60, 32, random);

//        gen.addGrass(0, 10);
//        gen.addWater(0, 6);
//        gen.addBoulders(3, 5);
//        gen.addDoors(25, true);
//        gen.addLake(20, '£', '¢');
//        gen.addMaze(40);

//        DungeonBoneGen bone = new DungeonBoneGen(random.copy());

        for(TilesetType tt : TilesetType.values()){
            random.setSeed(0xB0BAFE77BA77L);
            System.out.println(tt.name());
//            DungeonTools.debugPrint(LineTools.hashesToLines(gen.generate(tt), true));
//            bone.generate(tt, 60, 32);
//            DungeonTools.debugPrint(bone.region.not().thin().not().removeEdges().toChars());
            DungeonTools.debugPrint(gen.generate(tt));
        }
    }
}
