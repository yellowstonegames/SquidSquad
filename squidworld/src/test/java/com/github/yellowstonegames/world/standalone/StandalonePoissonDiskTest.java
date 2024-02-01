/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.world.standalone;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedMap;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.place.DungeonTools;
import com.github.yellowstonegames.place.tileset.DungeonBoneGen;

/**
 * A test for the randomized spraying of Coords in the PoissonDisk class.
 * Needs to be in SquidPlace because the dungeon map generation is here.
 */
public class StandalonePoissonDiskTest {

    public static void main(String[] args) {
        AceRandom rng = new AceRandom();
        DungeonBoneGen dg = new DungeonBoneGen(rng);
        char[][] dun = new char[80][80];

        ArrayTools.fill(dun, '.');
        DungeonTools.wallWrap(dun);

        OrderedMap<GridPoint2, Array<GridPoint2>> points = StandalonePoissonDisk.sampleRectangle(
                new GridPoint2(1, 1), new GridPoint2(78, 78), 2.5f,
                80, 80, 30, rng);
        for (int i = 0; i < points.size; i++) {
            GridPoint2 c = points.orderedKeys().get(i);
            dun[c.x][c.y] = (char) ('0' + points.get(c).size);
        }
        dg.setDungeon(dun);
        System.out.println(dg);
        System.out.println();
    }

}
