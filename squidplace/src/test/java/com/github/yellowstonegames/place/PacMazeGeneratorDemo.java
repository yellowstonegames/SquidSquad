package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.LaserRandom;
import com.github.yellowstonegames.grid.LineTools;

public class PacMazeGeneratorDemo {

    public static void main(String[] args) {
        LaserRandom random = new LaserRandom(0xB0BAFE77);
        PacMazeGenerator gen = new PacMazeGenerator(60, 60, random);
        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
