package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.grid.LineTools;

public class WildernessGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom();
        WildernessGenerator gen = new WildernessGenerator(40, 40, random.nextInt(42), random);
        DungeonTools.debugPrint(gen.generate());
    }
}
