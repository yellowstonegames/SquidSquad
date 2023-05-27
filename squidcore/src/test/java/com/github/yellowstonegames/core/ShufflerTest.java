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

package com.github.yellowstonegames.core;

import com.github.tommyettinger.random.FourWheelRandom;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;

public class ShufflerTest {
    @Test
    public void testGapShuffler() {
        for (int n = 8; n <= 48; n+= 8) {
            FourWheelRandom rng = new FourWheelRandom(Hasher.shax.hash64("SquidSquad!"));
            ArrayList<String> names = new ArrayList<>(n);
            Base base = Base.scrambledBase(rng);
            for (int i = 0; i < n; i++) {
                names.add(base.signed(rng.next(31)));
            }

            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            for (int i = 0; i < 200; i++) {
                System.out.println(gap.next());
            }

            // alternate API
            /*
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            Iterator<String> it = gap.iterator();
            for (int i = 0; i < 200; i++) {
                if(it.hasNext())
                    System.out.println(it.next());
            }*/

            System.out.println("\n");
        }
    }

    @Test
    public void testIntShufflerBounds() {
        for (int i = 3; i <= 80; i++) {
            IntShuffler lss = new IntShuffler(i, 31337+i);
            System.out.printf("Bound %02d: %d", i, lss.next());
            for (int j = 1; j < i; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testIntShufflerReseed() {
        IntShuffler lss = new IntShuffler(7, 0);
        for (int i = 0; i < 30; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testIntShufflerReverse() {
        IntShuffler lss = new IntShuffler(7, 0);
        for (int i = 0; i < 10; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X forward: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
            System.out.printf("Seed %08X reverse: %d", i, lss.previous());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.previous());
            }
            System.out.println();

        }
    }

    /**
     * May take about a minute to run, possibly longer on older hardware.
     */
    @Test
    @Ignore
    public void testManyBounds() {
        for (int outer = 0; outer < 50; outer++) {
            for (int bound = 3; bound <= 42; bound++) {
                long seed = Hasher.randomize3(outer);
                IntShuffler is = new IntShuffler(bound, seed);
                int[] buckets = new int[bound];
                for (int i = 0; i < 1000000; i++) {
                    is.restart(seed++);
                    buckets[is.next()]++;
                }
                int mn = Integer.MAX_VALUE, mx = Integer.MIN_VALUE;
                for (int i = 0; i < bound; i++) {
                    mn = Math.min(mn, buckets[i]);
                    mx = Math.max(mx, buckets[i]);
                }
                if ((mx - mn) * bound >= 85000) {
                    System.out.println("Got through: " + outer);
                    System.out.println("Worst: " + (mx - mn) * bound);
                    System.out.println("Bound: " + bound);
                    System.out.println("Seed: " + seed);
                    System.out.println("Max: " + mx);
                    System.out.println("Min: " + mn);
                    System.out.println(StringTools.join(", ", buckets));
                }
                Assert.assertTrue((mx - mn) * bound < 85000);
            }
        }
    }

    public static void main(String[] args) {
        int bound = 16, seed = 0;
        IntShuffler is = new IntShuffler(bound, seed);
        int[] buckets = new int[bound];
        for (int i = 0; i < 1000000; i++) {
            is.restart(seed++);
            buckets[is.next()]++;
        }
        int mn = Integer.MAX_VALUE, mx = Integer.MIN_VALUE;
        for (int i = 0; i < bound; i++) {
            int count = Math.round(buckets[i] * bound / 10000f);
            mn = Math.min(mn, buckets[i]);
            mx = Math.max(mx, buckets[i]);
            System.out.printf("% 3d : %6d , %0"+count+"d\n", i, count, 0);
        }
        System.out.println("Smallest bucket     : " + mn);
        System.out.println("Largest bucket      : " + mx);
        System.out.println("Adjusted difference : " + (mx - mn) * bound / 10000f);
    }
}
