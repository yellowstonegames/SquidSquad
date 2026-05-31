/*
 * Copyright (c) 2024 See AUTHORS file.
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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.ObjectIntMap;
import com.github.tommyettinger.random.AceRandom;
import org.junit.Test;

import static com.github.yellowstonegames.CoreInternals.PRINTING;

public class TableTests {
    @Test
    public void testWeightedTable() {
        float[] odds = {1.1f, 2.2f, 3.3f, 4.4f, 5.5f};
        int[] frequencies = new int[odds.length];
        AceRandom random = new AceRandom(123L);
        WeightedTable table = new WeightedTable(odds);
        for (int i = 0; i < 10000; i++) {
            frequencies[table.random(random)]++;
        }
        if(PRINTING)
            System.out.println(Base.BASE10.join(", ", frequencies));


    }

    @Test
    public void testProbabilityTable() {
        String[] keys = {"-3 Rusty Spoon", "+1 Thunder Cudgel", "+5 Vorpal Blade of Untimely Demise"};
        int[] vals = {10, 3, 1};
        ObjectIntMap<String> odds = new ObjectIntMap<>(keys, vals);
        ObjectIntMap<String> frequencies = new ObjectIntMap<>(keys, new int[3]);
        AceRandom random = new AceRandom(123L);
        ProbabilityTable<String> table = new ProbabilityTable<>(random);
        table.addAll(odds);
        for (int i = 0; i < 10000; i++) {
            frequencies.getAndIncrement(table.random(), 0, 1);
        }
        if(PRINTING)
            System.out.println(frequencies);

        ProbabilityTable<String> wood = new ProbabilityTable<>("wood"),
                carpenter = new ProbabilityTable<>("carpenter");
        wood.add("splinter", 10).add("twig", 4).add("branch", 2).add("plank", 1).add("twig", 3);
        carpenter.add("table", 3).add("shelf", 7).add(wood, 5).add("chair", 3).add("chair", 2);

        ObjectIntMap<String> woodCounts = new ObjectIntMap<>(
                new String[]{"splinter", "twig", "branch", "plank"}, new int[]{0, 0, 0, 0}),
                carpenterCounts = new ObjectIntMap<>(
                        new String[]{"splinter", "twig", "branch", "plank", "table", "shelf", "chair"},
                        new int[]{0, 0, 0, 0, 0, 0, 0});
        String current;
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = wood.random();
                woodCounts.getAndIncrement(current, 0, 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(woodCounts);
            System.out.println('\n');
        }
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.getAndIncrement(current, 0, 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(carpenterCounts);
            System.out.println('\n');
        }
        carpenterCounts = new ObjectIntMap<>(
                new String[]{"splinter", "twig", "branch", "plank", "table", "shelf", "chair"},
                new int[]{0, 0, 0, 0, 0, 0, 0});
        carpenter.remove("shelf", 4);
        carpenter.remove("chair");
        for (int l = 0; l < 10; l++) {
            for (int i = 0; i < 20; i++) {
                current = carpenter.random();
                carpenterCounts.getAndIncrement(current, 0, 1);
                if (PRINTING) {
                    System.out.print(current);
                    System.out.print(' ');
                }
            }
            if (PRINTING) System.out.println();
        }
        if (PRINTING) {
            System.out.println();
            System.out.println(carpenterCounts);
        }
    }

}
