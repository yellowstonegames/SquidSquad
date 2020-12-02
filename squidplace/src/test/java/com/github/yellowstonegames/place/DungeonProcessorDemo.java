package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.place.tileset.TilesetType;

public class DungeonProcessorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        DungeonProcessor gen = new DungeonProcessor(100, 50, random);
        char[][] map;
        gen.addGrass(0, 10);
        gen.addWater(0, 6);
        gen.addBoulders(3, 5);
        gen.addDoors(25, true);
        gen.addLake(20, '£', '¢');
        //gen.addMaze(40);
        for(TilesetType tt : TilesetType.values()){
            System.out.println(tt.name());
            map = DungeonTools.hashesToLines(gen.generate(tt), true);
            DungeonTools.debugPrint(map);
        }
    }
}
