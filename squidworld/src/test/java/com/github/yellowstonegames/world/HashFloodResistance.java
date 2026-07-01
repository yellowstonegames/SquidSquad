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


import com.github.tommyettinger.digital.Hasher;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import static com.github.tommyettinger.digital.Hasher.*;

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
//        {
//            // testing 1 to 30000 NUL chars in a set of Lists:
////            java.util.HashSet<List<String>> set = new java.util.HashSet<>(1, 0.75f); // 12.206 seconds taken.
////            java.util.HashSet<List<String>> set = new java.util.HashSet<>(30000, 0.75f); // 13.275 seconds taken.
////            com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(1, 0.75f); // 4.127 seconds taken.
////            com.github.tommyettinger.ds.ObjectSet<List<String>> set = new com.github.tommyettinger.ds.ObjectSet<>(30000, 0.75f); // 3.774 seconds taken.
////            com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(1, 0.75f); // 3.594 seconds taken.
//            com.badlogic.gdx.utils.ObjectSet<List<String>> set = new com.badlogic.gdx.utils.ObjectSet<>(30000, 0.75f); // 3.282 seconds taken.
//            String s = "\0";
//            for (int iter = 0; iter < 30; iter++) {
//                for (int i = 0; i < 1000; i++)
//                    set.add(Collections.singletonList(s += '\0'));
//            }
//            System.out.println(set.size);
//            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
//        }
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

//        {
//            // testing 1 to 30000 NUL chars in a set of Strings:
//            // HashSet takes advantage of String being Comparable and falls back to a TreeSet.
////            java.util.HashSet<String> set = new java.util.HashSet<>(1, 0.75f); // 1.063 seconds taken.
////            java.util.HashSet<String> set = new java.util.HashSet<>(30000, 0.75f); // 0.983 seconds taken.
////            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<>(1, 0.75f); // 3.024 seconds taken.
////            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<>(30000, 0.75f); // 2.949 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return Hasher.hashBulk(1L, (String) item) & mask;
//                }
//            };// 0.728 seconds taken.
////            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
////                @Override
////                protected int place(Object item) {
////                    return Hasher.hash(1L, (String) item) & mask;
////                }
////            };// 0.644 seconds taken.
////            com.badlogic.gdx.utils.ObjectSet<String> set = new com.badlogic.gdx.utils.ObjectSet<>(1, 0.75f); // 2.677 seconds taken.
////            com.badlogic.gdx.utils.ObjectSet<String> set = new com.badlogic.gdx.utils.ObjectSet<>(30000, 0.75f); // 2.724 seconds taken.
//            String s = "\0";
//            for (int iter = 0; iter < 30; iter++) {
//                for (int i = 0; i < 1000; i++)
//                    set.add(s += '\0');
//            }
//            System.out.println(set.size());
//            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
//        }
//
        {
            // testing 1 to 30000 'a'' chars in a set of Strings:
            // HashSet can take advantage of String being Comparable and falls back to a TreeSet if needed. It shouldn't here.
//            java.util.HashSet<String> set = new java.util.HashSet<>(1, 0.75f); // 0.688 seconds taken.
//            java.util.HashSet<String> set = new java.util.HashSet<>(30000, 0.75f); // 0.682 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<>(1, 0.75f); // 0.707 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<>(30000, 0.75f); // 0.721 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return Hasher.hashBulk(1L, (String) item) & mask;
//                }
//            };// 0.73 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return Hasher.hash(1L, (String) item) & mask;
//                }
//            };// 0.656 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return hashAdze(1111111111111111111L, (String) item) & mask;
//                }
//            };// 0.724 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return hashPairAAT(1111111111111111111L, (String) item) & mask;
//                }
//            };// 0.631 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return hashCurlup((String) item) & mask;
//                }
//            };// 0.646 seconds taken.
//            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
//                @Override
//                protected int place(Object item) {
//                    return hashWisp((String) item) & mask;
//                }
//            };// 0.641 seconds taken.
            com.github.tommyettinger.ds.ObjectSet<String> set = new com.github.tommyettinger.ds.ObjectSet<String>(30000, 0.75f){
                @Override
                protected int place(Object item) {
                    return hashWispL((String) item) & mask;
                }
            };// 0.647 seconds taken.
//            com.badlogic.gdx.utils.ObjectSet<String> set = new com.badlogic.gdx.utils.ObjectSet<>(1, 0.75f); // 0.727 seconds taken.
//            com.badlogic.gdx.utils.ObjectSet<String> set = new com.badlogic.gdx.utils.ObjectSet<>(30000, 0.75f); // 0.707 seconds taken.
            String s = "a";
            for (int iter = 0; iter < 30; iter++) {
                for (int i = 0; i < 1000; i++)
                    set.add(s += 'a');
            }
            System.out.println(set.size());
            System.out.println(((System.currentTimeMillis() - startTime) * 1E-3) + " seconds taken.");
        }

    }

    public static int hashAdze(final long seed, final String data) {
        if (data == null)
            return 0;
        int len = data.length(), start = 0;
        long h = len ^ seed;
        while(len >= 14){
            len -= 14;
            h *= C;
            h += mixMultiple(data.charAt(start), data.charAt(start + 1), data.charAt(start + 2), data.charAt(start + 3), data.charAt(start + 4), data.charAt(start + 5), data.charAt(start + 6));
            h = (h << 39 | h >>> 25);
            h += mixMultiple(data.charAt(start + 7), data.charAt(start + 8), data.charAt(start + 9), data.charAt(start + 10), data.charAt(start + 11), data.charAt(start + 12), data.charAt(start + 13));
            start += 14;
        }
        while(len >= 4){
            len -= 4;
            h = mixMultiple(h, data.charAt(start), data.charAt(start + 1), data.charAt(start + 2), data.charAt(start + 3));
            start += 4;
        }
        switch (len) {
            case 1 :  h = mixMultiple(h, data.charAt(start)); break;
            case 2 :  h = mixMultiple(h, data.charAt(start), data.charAt(start + 1)); break;
            case 3 :  h = mixMultiple(h, data.charAt(start), data.charAt(start + 1), data.charAt(start + 2)); break;
        }
        return (int) mix(h);
    }
    /**
     * Hashes a String.
     * Gets a 32-bit hash. Uses 64-bit math so this behaves correctly, though slowly, on GWT.
     * <br>
     * Based on Sokolov Yura's (funny_falcon's) GoodOAAT hash for bytes, but made to work on Java's 16-bit chars
     * instead. GoodOAAT is MIT-licensed. Like GoodOAAT, this is non-multiplicative.
     * <br>
     * This hash passes SMHasher 3 testing. It is called "PairAAT_C" in
     * <a href="https://github.com/tommyettinger/smhasher-with-junk/tree/master/smhasher3">the test repo</a>.
     *
     * @param seed any change to the seed should change the hashes of non-null, non-empty data
     * @param data  the String to hash
     * @return a 32-bit hash of data
     */
    public static int hashPairAAT(long seed, String data) {
        if (data == null)
            return 0;
        int h1 = (int)seed + 0xB92266DD;
        int h2 = (int) (seed >>> 32 ^ (seed << 22 | seed >>> 42) ^ seed >>> 17) ^ 0x9E3779B9;
        final int len = data.length();
        for (int i = 1; i < len; i+= 2) {
            h1 += data.charAt(i - 1);
            h1 ^= h1 << 8;
            h2 += h1 ^ data.charAt(i);
            h2 = (h2 << 7 | h2 >>> 25);
            h2 += h2 << 2;
        }

        if((len & 1) == 1) {
            h1 += data.charAt(len - 1);
            h1 ^= h1 << 8;
            h2 += h1;
            h2 = (h2 << 7 | h2 >>> 25);
            h2 += h2 << 2;
        }

        h1 ^= h2;
        h1 += (h2 << 25 | h2 >>> 7);
        h2 ^= h1; h2 += (h1 << 21 | h1 >>> 11);
        h1 ^= h2; h1 += (h2 << 11 | h2 >>> 21);
        h2 ^= h1; h2 += (h1 << 14 | h1 >>> 18);
        h1 ^= h2; h1 += (h2 << 5 | h2 >>> 27);
        h2 ^= h1; h2 += (h1 << 19 | h1 >>> 13);
        return h2 ^ h1;
    }

    public static int hashCurlup(final String data) {
        if (data == null) return 0;
        final int length = data.length();
        long result = length * 0x9E3779B97F4A7C15L;
        int i = 7;
        for (; i < length; i += 8) {
            result =  0xEBEDEED9D803C815L * result
                + 0xD96EB1A810CAAF5FL * data.charAt(i - 7)
                + 0xC862B36DAF790DD5L * data.charAt(i - 6)
                + 0xB8ACD90C142FE10BL * data.charAt(i - 5)
                + 0xAA324F90DED86B69L * data.charAt(i - 4)
                + 0x9CDA5E693FEA10AFL * data.charAt(i - 3)
                + 0x908E3D2C82567A73L * data.charAt(i - 2)
                + 0x8538ECB5BD456EA3L * data.charAt(i - 1)
                + 0xD1B54A32D192ED03L * data.charAt(i)
            ;
        }
        for (int r = (length & 7); r > 0; r--) {
            result = 0x9E3779B97F4A7C15L * result + data.charAt(length - r);
        }
        result *= 0x94D049BB133111EBL;
        result ^= (result << 41 | result >>> 23) ^ (result << 17 | result >>> 47);
        result *= 0x369DEA0F31A53F85L;
        result ^= result >>> 31;
        result *= 0xDB4F0B9175AE2165L;
        return (int)(result ^ result >>> 28);
    }

    public static int hashWisp(final String data) {
        if (data == null)
            return 0;
        int result = 0x9E3779B9, a = 0x632BE5AB;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result = (result << 5 | result >>> 27) + (a ^= 0x85157AF5 * data.charAt(i));
        }
        return result * (a | 1) ^ (result >>> 11 | result << 21);
    }

    public static int hashWispL(final String data) {
        if (data == null)
            return 0;
        long result = 0x9E3779B97F4A7C15L, a = 0xD1B54A32D192ED03L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0xF1357AEA2E62A9C5L * data.charAt(i));
        }
        return (int) (result * (a | 1) ^ (result >>> 43 | result << 21));
    }

}
