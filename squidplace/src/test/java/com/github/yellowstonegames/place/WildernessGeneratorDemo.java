package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.AceRandom;

public class WildernessGeneratorDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom();
        WildernessGenerator gen = new WildernessGenerator(40, 40, Biome.TABLE[random.nextInt(42)], random);
        DungeonTools.debugPrint(gen.generate());
    }
}
