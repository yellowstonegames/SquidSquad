package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.LaserRandom;
import com.github.yellowstonegames.grid.LineTools;

public class MixedGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        MixedGenerator gen = new MixedGenerator(60, 60, random);
        gen.putWalledBoxRoomCarvers(4);
        gen.putWalledRoundRoomCarvers(1);
        char[][] map = gen.generate();
        map = LineTools.hashesToLines(new DungeonProcessor(60, 60, random).generate(map, gen.environment));
        DungeonTools.debugPrint(map);
    }
}
