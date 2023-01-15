package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.LineTools;

public class PacMazeGeneratorDemo {

    public static void main(String[] args) {
        AceRandom random = new AceRandom(0xB0BAFE77);
        PacMazeGenerator gen = new PacMazeGenerator(60, 60, random);
        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
    }
}
