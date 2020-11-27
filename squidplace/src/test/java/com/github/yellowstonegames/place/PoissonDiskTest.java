package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectOrderedSet;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.PoissonDisk;
import com.github.yellowstonegames.place.tileset.DungeonBoneGen;
import com.github.yellowstonegames.place.tileset.TilesetType;

/**
 * A test for the randomized spraying of Coords in the PoissonDisk class.
 * Needs to be in SquidPlace because the dungeon map generation is here.
 */
public class PoissonDiskTest {

    public static void main(String[] args) {
        if (!"true".equals(System.getenv("printing"))) return;
        LaserRandom rng = new LaserRandom(0xBEEFBABEL);
        DungeonBoneGen dg = new DungeonBoneGen(rng);
        char[][] dun = DungeonTools.wallWrap(dg.generate(TilesetType.DEFAULT_DUNGEON, 80, 80));

        // System.out.println(dg);

        ObjectOrderedSet<Coord> disks = PoissonDisk.sampleMap(dun, 4f, rng, '#');

        for (Coord c : disks) {
            if (dun[c.x][c.y] != '#')
                dun[c.x][c.y] = 'o';
        }
        //hl[entry.x][entry.y] = '@';
        dg.setDungeon(dun);
        System.out.println(dg);

        System.out.println();
    }

}
