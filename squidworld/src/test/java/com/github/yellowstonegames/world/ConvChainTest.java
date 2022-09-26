/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.world;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.grid.ConvChain;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.place.DungeonTools;

public class ConvChainTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        DistinctRandom rng = new DistinctRandom(2252637788195L);
        long time2d = 0L, time, junk = 0L;

        rng.setState(2252637788195L);

        Region world = new MimicWorldMap(1L, Noise.instance, 0.1f).earthOriginal.copy();
        Region doubleWorld = new Region(world.width, world.width);
        doubleWorld.insert(0, 0, world);
        doubleWorld.insert(0, world.height, world);
        Region result = new Region(140, 140);
        result = ConvChain.fill(result, doubleWorld, 0.2, 5, rng);

        DungeonTools.debugPrint(LineTools.hashesToLines(ConvChain.sampleToMap(result, '.', '#'), true));
        System.out.println();


        time = System.currentTimeMillis();
        for(Region sample : ConvChain.samples)
        {
            ConvChain.fill(result, sample, 0.2, 3, rng);

            DungeonTools.debugPrint(LineTools.hashesToLines(ConvChain.sampleToMap(result, '.', '#'), true));
            System.out.println();

        }
        time2d += System.currentTimeMillis() - time;
        junk += rng.nextLong();

        System.out.println("2D time: " + time2d);
        System.out.println("Extra data, irrelevant except that it forces calculations: " + junk);
    }
}
