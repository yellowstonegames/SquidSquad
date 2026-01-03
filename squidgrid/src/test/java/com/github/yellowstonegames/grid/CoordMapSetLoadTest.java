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
    public static final int CAPACITY = 51;//SIZE * SIZE;//
    public static final float LOAD = 0.9f;
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
     * <br>
     * Testing Coord without the final imul()...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 50310 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 49753 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 33801 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 17504 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Changing multiplier to 0xcf1bbce7 ...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 60679 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 83915 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 37426 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 21435 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Using a multiplier of 0x9E3779B9 on a Cantor pairing function instead of Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 58454 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 72565 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 39070 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 19232 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Reverting Coord hash changes. Using a multiplier of 0x9E3779B9 on Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 53569 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 65286 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 34797 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 16228 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Different Coord hash; uses Xor-Rotate-Xor-Rotate instead of a multiplication, but still Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 74487 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 86166 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 42234 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * ObjectSet took 22702 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeObjectSetTest() {
        Coord.expandPoolTo(2048, 2048);
        ObjectSet<Coord> set;
        {
            set = new ObjectSet<>(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
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
     * <br>
     * Testing Coord without the final imul()...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 458318 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took (FOREVER!!!) ms with CAPACITY=51 and LOAD=0.9
     * (Over 25 minutes without finishing!!!)
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 18084 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 13670 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Changing multiplier to 0xcf1bbce7 ...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 40914 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 35343 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 30397 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 20689 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Using a multiplier of 0x9E3779B9 on a Cantor pairing function instead of Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 41149 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 39496 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 31836 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 21274 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Reverting Coord hash changes. Using a multiplier of 0x9E3779B9 on Rosenberg-Strong:
     * BUT also using a right shift instead of a mask in CoordSet:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 47615 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 45543 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 34973 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 22357 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Different Coord hash; uses Xor-Rotate-Xor-Rotate instead of a multiplication, but still Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 35421 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 73927 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 24360 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * CoordSet took 15901 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeCoordSetTest() {
        Coord.expandPoolTo(2048, 2048);
        CoordSet set;
        {
            set = new CoordSet(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
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
     * <br>
     * Testing Coord without the final imul()...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 23812 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 27500 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 22384 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 24134 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Changing multiplier to 0xcf1bbce7 ...
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 43773 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 61419 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 42549 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 48363 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Using a multiplier of 0x9E3779B9 on a Cantor pairing function instead of Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 43554 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 57627 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 43244 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 48983 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Reverting Coord hash changes. Using a multiplier of 0x9E3779B9 on Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 44936 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 57021 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 42745 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 48101 ms with CAPACITY=4194304 and LOAD=0.9
     * <br>
     * Different Coord hash; uses Xor-Rotate-Xor-Rotate instead of a multiplication, but still Rosenberg-Strong:
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 201115 ms with CAPACITY=51 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 204330 ms with CAPACITY=51 and LOAD=0.9
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 191694 ms with CAPACITY=4194304 and LOAD=0.5
     * <br>
     * Creating 100 sets with 4194304 Coord items each...
     * HashSet took 185862 ms with CAPACITY=4194304 and LOAD=0.9
     */
    @Test
    public void fillHugeHashSetTest() {
        Coord.expandPoolTo(2048, 2048);
        HashSet<Coord> set;
        {
            set = new HashSet<>(CAPACITY, LOAD);
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    set.add(Coord.get(x, y));
                }
            }
        }
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
