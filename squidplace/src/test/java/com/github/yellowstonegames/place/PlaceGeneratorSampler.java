package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.place.tileset.TilesetType;

public class PlaceGeneratorSampler {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77BA77L);
        DungeonProcessor proc = new DungeonProcessor(42, 42, random);

        PlaceGenerator[] generators = {
                new ConnectingMapGenerator(42, 42, random),
                new FlowingCaveGenerator(42, 42, TilesetType.DEFAULT_DUNGEON, random),
                new GrowingTreeMazeGenerator(42, 42, random),
                new MixedGenerator(42, 42, random),
                new PacMazeGenerator(42, 42, random),
                new SerpentMapGenerator(42, 42, random),
                new SymmetryDungeonGenerator(42, 42, random),
        };

        for(PlaceGenerator gen : generators){
            random.setSeed(0xB0BAFE77BA77L);
            System.out.println(gen);
            DungeonTools.debugPrint(proc.generate(gen.generate(), gen.getEnvironment()));
            System.out.println();
            DungeonTools.debugPrint(LineTools.hashesToLines(proc.dungeon));
        }
    }
}
