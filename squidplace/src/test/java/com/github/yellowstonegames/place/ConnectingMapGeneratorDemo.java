package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;

public class ConnectingMapGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        ConnectingMapGenerator gen = new ConnectingMapGenerator(31, 31, 5, 5, random, 1, 0.5);
        char[][] map = gen.generate();
//        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
