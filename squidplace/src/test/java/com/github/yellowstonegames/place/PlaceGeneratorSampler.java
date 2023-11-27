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
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.place.tileset.TilesetType;

public class PlaceGeneratorSampler {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77BA77L);
        DungeonProcessor proc = new DungeonProcessor(42, 42, random);

        PlaceGenerator[] generators = {
                new DungeonProcessor(42, 42, random),
                new FlowingCaveGenerator(42, 42, TilesetType.DEFAULT_DUNGEON, random),
                new ConnectingMapGenerator(42, 42, random),
                new GrowingTreeMazeGenerator(42, 42, random),
                new MixedGenerator(42, 42, random),
                new PacMazeGenerator(42, 42, random),
                new SerpentMapGenerator(42, 42, random),
                new SymmetryDungeonGenerator(42, 42, random),
                new SlashMazeGenerator(42, 42, random).setThin(true),
        };

        for(PlaceGenerator gen : generators){
            random.setSeed(0xB0BAFE77BA77L);
            System.out.println(gen);
            DungeonTools.debugPrint(gen.generate());
//            DungeonTools.debugPrint(proc.generate(gen.generate(), gen.getEnvironment()));
            System.out.println();
//            DungeonTools.debugPrint(LineTools.hashesToLines(proc.dungeon));
            DungeonTools.debugPrint(LineTools.hashesToLines(gen.getPlaceGrid()));
        }
    }
}
