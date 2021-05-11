package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.grid.LineTools;

public class SerpentMapGeneratorDemo {

    public static void main(String[] args) {
        final int width = 60, height = 60, depth = 8;
        LaserRandom random = new LaserRandom(0xB0BAFE77, 0xB055);
        SerpentMapGenerator gen = new SerpentMapGenerator(width, height, random, 0.07);
        gen.putWalledBoxRoomCarvers(4);
        gen.putCaveCarvers(1);
        char[][] map = LineTools.hashesToLines(gen.generate());
        DungeonTools.debugPrint(map);
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");

        random.setState(0xB0BAFE77, 0xB055BA77L);
        SerpentDeepMapGenerator deepSerpent = new SerpentDeepMapGenerator(width, height, depth, random, 0.15);
        deepSerpent.putWalledBoxRoomCarvers(2);
        deepSerpent.putWalledRoundRoomCarvers(2);
        deepSerpent.putCaveCarvers(3);
        char[][][] map3D = deepSerpent.generate();
        DungeonProcessor[] gens = new DungeonProcessor[depth];
        for (int i = 0; i < depth; i++) {
            System.out.println("--------------------------------------------------- depth " + i);
            gens[i] = new DungeonProcessor(width, height, random);
            gens[i].addWater(3, random.nextInt(25));
            gens[i].addGrass(0, random.nextInt(15));
            gens[i].addBoulders(3, random.nextInt(30));
            gens[i].addDoors(random.nextInt(4, 10), false);
            gens[i].generateRespectingStairs(map3D[i], deepSerpent.getEnvironment(i));
            gens[i].setPlaceGrid(LineTools.hashesToLines(gens[i].getPlaceGrid(), true));
            System.out.println(gens[i]);
        }

    }
}
