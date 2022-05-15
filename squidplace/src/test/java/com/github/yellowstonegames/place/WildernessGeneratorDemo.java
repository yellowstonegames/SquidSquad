package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.TricycleRandom;

public class WildernessGeneratorDemo {

    public static void main(String[] args) {
        TricycleRandom random = new TricycleRandom();
        WildernessGenerator gen = new WildernessGenerator(40, 40, Biome.TABLE[random.nextInt(42)], random);
        DungeonTools.debugPrint(gen.generate());
    }
}
