/*
 * Copyright (c) 2026; see AUTHORS file.
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


import java.util.Collections;
import java.util.List;

/**
 * Meant to see how resilient different hashtable implementations are against inserting many different items with
 * identical hash codes.
 */
public class HashFloodResistance {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
//        java.util.HashSet<List<String>> set = new java.util.HashSet<>(1); // 250.749 seconds taken.
//        java.util.HashSet<List<String>> set = new java.util.HashSet<>(100000); // 236.602 seconds taken.
//        com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(1); // 49.855000000000004 seconds taken.
//        com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(100000); // 46.996 seconds taken.
//        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(1); // 37.43 seconds taken.
//        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(100000); // 37.068 seconds taken.
        {
            // testing 1 to 30000 NUL chars in a set of Lists:
//            java.util.HashSet<List<String>> set = new java.util.HashSet<>(1, 0.75f); // 12.206 seconds taken.
//            java.util.HashSet<List<String>> set = new java.util.HashSet<>(30000, 0.75f); // 13.275 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(1, 0.75f); // 4.127 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(30000, 0.75f); // 3.774 seconds taken.
//            com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(1, 0.75f); // 3.594 seconds taken.
            com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(30000, 0.75f); // 3.282 seconds taken.
            String s = "\0";
            for (int iter = 0; iter < 30; iter++) {
                for (int i = 0; i < 1000; i++)
                    set.add(Collections.singletonList(s += '\0'));
            }
            System.out.println(set.size);
            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
        }
//        {
//            // testing aaaaaaaaaaaa... up to 100000 chars per String:
//            java.util.HashSet<List<String>> set = new java.util.HashSet<>(1, 0.75f); // 6.558 seconds taken.
////        java.util.HashSet<List<String>> set = new java.util.HashSet<>(100000, 0.75f); // 6.7 seconds taken.
////        com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(1, 0.75f); // 6.695 seconds taken.
////        com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(100000, 0.75f); // 6.354 seconds taken.
////        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(1, 0.75f); // 6.685 seconds taken.
////        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(100000, 0.75f); // 6.495 seconds taken.
//            String s = "a";
//            for (int iter = 0; iter < 100; iter++) {
//                for (int i = 0; i < 1000; i++)
//                    set.add(Collections.singletonList(s += 'a'));
//            }
//            System.out.println(set.size());
//            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
//        }
    }
}
