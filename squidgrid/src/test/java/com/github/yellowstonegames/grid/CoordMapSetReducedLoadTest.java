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

/**
 * Current results as of May 10, 2026:
 * <pre>
 * Creating 100 sets with 1048576 Coord items each...
 * ObjectSet took 9628 ms with REDUCED_CAPACITY=1048576 and LOAD=0.5
 * Creating 100 sets with 1048576 Coord items each...
 * CoordSet took 6967 ms with REDUCED_CAPACITY=1048576 and LOAD=0.5
 * Creating 100 sets with 1048576 Coord items each...
 * HashSet took 7954 ms with REDUCED_CAPACITY=1048576 and LOAD=0.5
 * </pre>
 * <br>
 * <pre>
 * Creating 100 sets with 1048576 Coord items each...
 * ObjectSet took 8597 ms with REDUCED_CAPACITY=1048576 and LOAD=0.9
 * Creating 100 sets with 1048576 Coord items each...
 * CoordSet took 6494 ms with REDUCED_CAPACITY=1048576 and LOAD=0.9
 * Creating 100 sets with 1048576 Coord items each...
 * HashSet took 9164 ms with REDUCED_CAPACITY=1048576 and LOAD=0.9
 * </pre>
 * <br>
 */
//@Ignore
public class CoordMapSetReducedLoadTest {
    public static final int LIMIT = 100;
    public static final int SIZE = 2048;
    public static final int REDUCTION = 2;
    public static final int REDUCTION_MASK = (1 << REDUCTION) - 1;
    public static final int REDUCED_CAPACITY = SIZE * SIZE >>> REDUCTION;
    public static final float LOAD = 0.5f;

    @Test
    public void fillHugeObjectSetReducedTest() {
        Coord.expandPoolTo(2048, 2048);
        ObjectSet<Coord> set;
        {
            set = new ObjectSet<>(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE >>> REDUCTION) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new ObjectSet<>(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("ObjectSet took " + (System.currentTimeMillis() - startTime) +
                " ms with REDUCED_CAPACITY=" + REDUCED_CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.first());
    }

    @Test
    public void fillHugeCoordSetReducedTest() {
        Coord.expandPoolTo(2048, 2048);
        CoordSet set;
        {
            set = new CoordSet(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE >>> REDUCTION) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new CoordSet(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("CoordSet took " + (System.currentTimeMillis() - startTime) +
                " ms with REDUCED_CAPACITY=" + REDUCED_CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.first());
    }

    @Test
    public void fillHugeHashSetReducedTest() {
        Coord.expandPoolTo(2048, 2048);
        HashSet<Coord> set;
        {
            set = new HashSet<>(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("Creating " + LIMIT + " sets with " + (SIZE * SIZE >>> REDUCTION) + " Coord items each...");
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < LIMIT; i++) {
            set = new HashSet<>(REDUCED_CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if((NumberPairing.cantor(x, y) & REDUCTION_MASK) == 0)
                        set.add(Coord.get(x, y));
                }
            }
        }
        System.out.println("HashSet took " + (System.currentTimeMillis() - startTime) +
                " ms with REDUCED_CAPACITY=" + REDUCED_CAPACITY + " and LOAD=" + LOAD);
        System.out.println(set.iterator().next());
    }

}
