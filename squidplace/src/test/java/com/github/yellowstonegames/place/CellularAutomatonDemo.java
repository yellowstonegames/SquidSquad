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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.random.AceRandom;
import com.github.yellowstonegames.grid.*;

public class CellularAutomatonDemo {

    public static void main(String[] args) {
        AceRandom rng = new AceRandom(0xB0BAFE77);
        Region region = new Region(rng, 0.6f, 40, 40);
        CellularAutomaton ca = new CellularAutomaton(40, 40);
        ca.remake(region);
        region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.8f);
        region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.83f);
        ca.current.remake(region.deteriorate(rng, 0.86f));
        region.or(ca.runBasicSmoothing());
        ca.current.remake(region.removeEdges().largestPart());
        region.remake(ca.runDiagonalGapCleanup());
        char[][] map = region.toChars('.', '#');
        map = LineTools.hashesToLines(new DungeonProcessor(40, 40, rng).generate(map, region.toInts(DungeonTools.ROOM_FLOOR, DungeonTools.ROOM_WALL)));
        DungeonTools.debugPrint(map);
    }
}
