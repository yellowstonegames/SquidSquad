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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectSet;
import org.junit.Test;

public class CoordMapSetLoadTest {
    /**
     * Using new ObjectSet<>(51, 0.7f):
     * Took 2628 ms.
     * Using new ObjectSet<>(51, 0.5f):
     * Took 3092 ms.
     */
    @Test
    public void fillHugeObjectSetTest() {
        Coord.expandPoolTo(2048, 2048);
        long startTime = System.currentTimeMillis();
        ObjectSet<Coord> set = new ObjectSet<>(51, 0.5f);
        for (int x = 0; x < 2048; x++) {
            for (int y = 0; y < 2048; y++) {
                set.add(Coord.get(x, y));
            }
        }
        System.out.println("Took " + (System.currentTimeMillis() - startTime) + " ms.");
        System.out.println(set.first());
    }
    /**
     * Using new CoordSet(51, 0.7f), place() commented out:
     * Took 2279 ms.
     * Same as above, but place() intact:
     * Took 2257 ms.
     * Using new CoordSet(51, 0.5f), place() intact:
     * Took 1929 ms.
     */
    @Test
    public void fillHugeCoordSetTest() {
        Coord.expandPoolTo(2048, 2048);
        long startTime = System.currentTimeMillis();
        CoordSet set = new CoordSet(51, 0.5f);
        for (int x = 0; x < 2048; x++) {
            for (int y = 0; y < 2048; y++) {
                set.add(Coord.get(x, y));
            }
        }
        System.out.println("Took " + (System.currentTimeMillis() - startTime) + " ms.");
        System.out.println(set.first());
    }
}
