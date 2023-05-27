/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.place;

import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordObjectOrderedMap;
import com.github.yellowstonegames.grid.CoordOrderedSet;
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
        AceRandom rng = new AceRandom(0xBEEFBABEL);
        DungeonBoneGen dg = new DungeonBoneGen(rng);
        char[][] dun = DungeonTools.wallWrap(dg.generate(TilesetType.DEFAULT_DUNGEON, 80, 80));

        // System.out.println(dg);

        CoordOrderedSet disks = PoissonDisk.sampleMap(dun, 4f, rng, '#');

        for (Coord c : disks) {
            if (dun[c.x][c.y] != '#')
                dun[c.x][c.y] = 'o';
        }
        //hl[entry.x][entry.y] = '@';
        dg.setDungeon(dun);
        System.out.println(dg);
        System.out.println();

        ArrayTools.fill(dun, '.');
        DungeonTools.wallWrap(dun);

        CoordObjectOrderedMap<ObjectList<Coord>> points = PoissonDisk.sampleRectangle(
                Coord.get(1, 1), Coord.get(78, 78), 2.5f,
                80, 80, 30, rng);
        for (int i = 0; i < points.size(); i++) {
            Coord c = points.keyAt(i);
            dun[c.x][c.y] = (char) ('0' + points.getAt(i).size());
        }
        dg.setDungeon(dun);
        System.out.println(dg);
        System.out.println();

    }

}
