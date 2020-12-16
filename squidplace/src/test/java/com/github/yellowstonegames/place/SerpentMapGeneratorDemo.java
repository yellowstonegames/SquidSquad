package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.grid.LineTools;

public class SerpentMapGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        SerpentMapGenerator gen = new SerpentMapGenerator(80, 40, random, 0.07);
        gen.putWalledBoxRoomCarvers(4);
        gen.putCaveCarvers(1);
        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
