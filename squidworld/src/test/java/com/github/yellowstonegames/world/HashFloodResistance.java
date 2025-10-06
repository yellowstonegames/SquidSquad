package com.github.yellowstonegames.world;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Meant to see how resilient different hashtable implementations are against inserting many different items with
 * identical hash codes.
 */
public class HashFloodResistance {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        java.util.HashSet<List<String>> set = new java.util.HashSet<>(1); // 250.749 seconds taken.
//        java.util.HashSet<List<String>> set = new java.util.HashSet<>(100000); // 236.602 seconds taken.
//        com.github.tommyettinger.ds.ObjectSet<@NotNull List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(1); // 49.855000000000004 seconds taken.
//        com.github.tommyettinger.ds.ObjectSet<@NotNull List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(100000); // 46.996 seconds taken.
//        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(1); // 37.43 seconds taken.
//        com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(100000); // 37.068 seconds taken.
        String s = "\0";
        for (int iter = 0; iter < 100; iter++) {
            for (int i = 0; i < 1000; i++)
                set.add(Collections.singletonList(s += '\0'));
            System.out.println(set.size());
            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
        }
    }
}
