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

import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.ConvChain;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.Region;
import com.github.yellowstonegames.place.DungeonTools;

public class ConvChainTest {
    public static void main(String[] args)
    {
        //seed is, in base 36, the number SQUIDLIB
        WhiskerRandom rng = new WhiskerRandom(2252637788195L);
        long timeSamples, time, junk = 0L;

        for (int o = 2; o <= 5; o++) {
            rng.setState(2252637788195L);

            Region world = new MimicLocalMap(1L, Noise.instance, 0.1f).earthOriginal.copy();
            Region result = new Region(140, 140);
            result = ConvChain.fill(result, world, 0.125, 15, rng, o);

            DungeonTools.debugPrint(LineTools.hashesToLines(ConvChain.sampleToMap(result, '.', '#'), true));
            System.out.println();


            time = System.currentTimeMillis();
            for (Region sample : ConvChain.samples) {
                ConvChain.fill(result, sample, 0.125, 15, rng, o);

                DungeonTools.debugPrint(LineTools.hashesToLines(ConvChain.sampleToMap(result, '.', '#'), true));
                System.out.println();

            }
            timeSamples = System.currentTimeMillis() - time;
            junk += rng.nextLong();

            System.out.println("Time for all samples: " + timeSamples);
            System.out.println();
        }
        System.out.println("Extra data, irrelevant except that it forces calculations: " + junk);
    }
}
