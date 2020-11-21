package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;
import org.junit.Test;

public class ConnectingMapGeneratorTest {
    @Test
    public void testOutput() {
        if (!"true".equals(System.getenv("printing"))) return;
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        ConnectingMapGenerator gen = new ConnectingMapGenerator(61, 31, 4, 4, random, 1, 0.5);
        char[][] map = DungeonTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
