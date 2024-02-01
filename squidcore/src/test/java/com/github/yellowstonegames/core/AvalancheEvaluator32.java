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

package com.github.yellowstonegames.core;

import com.github.tommyettinger.random.DistinctRandom;
import com.github.tommyettinger.digital.ArrayTools;

public class AvalancheEvaluator32 {
    private static final long N = 1L << 11;
    private static final int lower = 8, upper = 19, inc = 1;

    // fb ^ fc, rotate; fc ^ fd, rotate; fa + fb; increment fd
//    private static final int shiftA = 8, shiftB = 27, shiftC = 4; //1178.2487239837646, 7197.45912361145
    // using shiftC as the constant, but otherwise the same as above
//    private static final int shiftA = 26, shiftB = 11, shiftC = 0xADB5B165; //1991.191421508789, 11950.504871368408

    // fb ^ fc, rotate; fc ^ fd, rotate; fa ^ fb + fc; increment fd
//    private static final int shiftA = 8, shiftB = 27, shiftC = 4; //26.931787490844727, 178.46607971191406
//    private static final int shiftA = 19, shiftB = 24, shiftC = 14; //15.269414901733398, 172.5810317993164
    // using shiftC as the constant, but otherwise the same as above
//    private static final int shiftA = 19, shiftB = 24, shiftC = 0x30ECB8CB; //15.500167846679688, 171.91223335266113
// Above constants appear fine until a sudden BRank failure in PractRand. Right shift by 8 could be an issue?
    private static final int shiftA = 26, shiftB = 11, shiftC = 0xADB5B165; //16.027650833129883, 172.77030181884766
// These constants above are fine to at least 16TB without anomalies.

    private static final int constantAdd = (int)(0xF1357AEA2E62A9C5L * shiftC >>> 32) | 1;
    private static final int constantMul = (int)(0xF1357AEA2E62A9C5L * shiftC >>> 44) | 1;

    public static int mix(final int v, final int iterations) {
        int stateA = v;
        int stateB = 0;
        int stateC = 0;
        int stateD = 0;
        for (int i = 0; i < iterations; i++) {
            final int fa = stateA;
            final int fb = stateB;
            final int fc = stateC;
            final int fd = stateD;
            final int bc = fb ^ fc;
            final int cd = fc ^ fd;
            stateA = (bc << shiftA | bc >>> -shiftA);
            stateB = (cd << shiftB | cd >>> -shiftB);
//            stateC = fa + fb;
            stateC = fa ^ fb + fc;
            stateD = fd + shiftC;
        }
        return stateC;
    }

    public static void main(String[] args) {
        DistinctRandom rng = new DistinctRandom(123456789L);
        // Order 1
        {
            final long[][] A = new long[32][32];
            double total = 0.0;
            for (int iterations = lower; iterations < upper; iterations += inc) {
                ArrayTools.fill(A, 0L);
                for (long n = 0; n < N; n++) {
                    int v = rng.nextInt();
                    int w = mix(v, iterations);
                    for (int i = 0; i < 32; i++) {
                        int x = w ^ mix(v ^ (1 << i), iterations);
                        for (int j = 0; j < 32; j++) {
                            A[i][j] += ((x >>> j) & 1);
                        }
                    }
                }
                double sumsq = 0.0;
                for (int i = 0; i < 32; i++) {
                    for (int j = 0; j < 32; j++) {
                        double v = A[i][j] - N * 0.5;
                        sumsq += v * v;
                    }
                }
                double result = sumsq * 0x1p-8 / N;
                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println("Order 1: " + shiftA + "," + shiftB + "," + shiftC + " with value " + total);
        }
        // Order 2
        {
            final long[][] A = new long[496][32];
            double total = 0.0;
            for (int iterations = lower; iterations < upper; iterations += inc) {
                ArrayTools.fill(A, 0L);
                for (long n = 0; n < N; n++) {
                    int v = rng.nextInt();
                    int w = mix(v, iterations);
                    for (int i = 0, p = 0; i < 32; i++) {
                        for (int h = i + 1; h < 32; h++) {
                            int x = w ^ mix(v ^ (1 << i) ^ (1 << h), iterations);
                            for (int j = 0; j < 32; j++) {
                                A[p][j] += ((x >>> j) & 1);
                            }
                            p++;
                        }
                    }
                }
                double sumsq = 0.0;
                for (int i = 0; i < 496; i++) {
                    for (int j = 0; j < 32; j++) {
                        double v = A[i][j] - N * 0.5;
                        sumsq += v * v;
                    }
                }
                double result = sumsq * 0x1p-8 / N;
                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println("Order 2: " + shiftA + "," + shiftB + "," + shiftC + " with value " + total);
        }
    }
}