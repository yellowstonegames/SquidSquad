package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;

public class ConnectingMapGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        ConnectingMapGenerator gen = new ConnectingMapGenerator(61, 31, 3, 4, random, 2, 0.5);
        char[][] map = DungeonTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
