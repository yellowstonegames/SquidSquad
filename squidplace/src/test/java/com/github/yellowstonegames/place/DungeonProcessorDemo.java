package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.LaserRandom;
import com.github.yellowstonegames.place.tileset.TilesetType;

public class DungeonProcessorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        DungeonProcessor gen = new DungeonProcessor(60, 32, random);

//        gen.addGrass(0, 10);
//        gen.addWater(0, 6);
//        gen.addBoulders(3, 5);
//        gen.addDoors(25, true);
//        gen.addLake(20, '£', '¢');
//        gen.addMaze(40);

//        DungeonBoneGen bone = new DungeonBoneGen(random.copy());

        for(TilesetType tt : TilesetType.values()){
            System.out.println(tt.name());
//            DungeonTools.debugPrint(LineTools.hashesToLines(gen.generate(tt), true));
//            bone.generate(tt, 60, 32);
//            DungeonTools.debugPrint(bone.region.not().thin().not().removeEdges().toChars());
            DungeonTools.debugPrint(gen.generate(tt));
        }
    }
}
