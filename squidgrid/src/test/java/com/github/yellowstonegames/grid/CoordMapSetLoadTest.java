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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.ds.ObjectSet;
import org.junit.Test;

import java.util.HashSet;

public class CoordMapSetLoadTest {
    public static final int LIMIT = 100;
    public static final int SIZE = 2048;
    public static final int CAPACITY = SIZE * SIZE;
    public static final float LOAD = 0.5f;
    /**
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 54576 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 66185 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 49825 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 15660 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeObjectSetTest() {
        Coord.expandPoolTo(2048, 2048);
        ObjectSet<Coord> set = ObjectSet.with(Coord.get(0,0));
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new ObjectSet<>(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("ObjectSet took " + (System.currentTimeMillis() - startTime) +
                " ms with CAPACITY=" + CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.first());
    }
    /**
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 36681 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 31913 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 26515 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 14927 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeCoordSetTest() {
        Coord.expandPoolTo(2048, 2048);
        CoordSet set = CoordSet.with(Coord.get(0,0));
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new CoordSet(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("CoordSet took " + (System.currentTimeMillis() - startTime) +
                " ms with CAPACITY=" + CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.first());
    }

    /**
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 44468 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 58013 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 42864 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 48833 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeHashSetTest() {
        Coord.expandPoolTo(2048, 2048);
        HashSet<Coord> set = new HashSet<>(1);
        set.add(Coord.get(0,0));
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new HashSet<>(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("HashSet took " + (System.currentTimeMillis() - startTime) +
                " ms with CAPACITY=" + CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.iterator().next());
    }
}
