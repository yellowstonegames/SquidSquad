package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;

public class MixedGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        MixedGenerator gen = new MixedGenerator(60, 60, random);
        char[][] map = DungeonTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
