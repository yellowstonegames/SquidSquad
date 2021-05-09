package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
/*
lower=10, upper=43, inc=1, N=1L<<10

27,21 with value 33.158623695373535
27,21 with value 1039.9528322219849

27,42 with value 33.130043029785156
27,42 with value 1039.803771018982

20,49 with value 32.97402095794678
20,49 with value 1040.877278327942

23,39 with value 41.84683799743652
23,39 with value 1045.4830961227417

23,46 with value 98.36502075195312
23,46 with value 1183.221848487854
 */
public class AvalancheEvaluator {
    private static final long N = 1L << 10;
//        private static final int lower = 9, upper = 19, inc = 1;
    private static final int lower = 10, upper = 43, inc = 1;

//    private static final int shiftB = 27, shiftC = 42;
//    private static final int shiftB = 27, shiftC = 21;
//    private static final int shiftB = 20, shiftC = 49;
//    private static final int shiftB = 23, shiftC = 39;
    private static final int shiftB = 23, shiftC = 46;

    public static long mix(final long v, final int iterations) {
        long stateA = v;
        long stateB = 0L;
        long stateC = 0L;
        for (int i = 0; i < iterations; i++) {
            final long a0 = stateA;
            final long b0 = stateB;
            final long c0 = stateC;
            stateA = b0 ^ c0 + 0xC6BC279692B5C323L;
            stateB = Long.rotateLeft(a0, shiftB) + c0;
            stateC = Long.rotateLeft(b0, shiftC) + a0;
        }
        return stateA;
    }

    public static void main(String[] args) {
        DistinctRandom rng = new DistinctRandom(123456789L);
        // Order 1
        {
            final long[][] A = new long[64][64];
            double total = 0.0;
            for (int iterations = lower; iterations < upper; iterations += inc) {
                ArrayTools.fill(A, 0L);
                for (long n = 0; n < N; n++) {
                    long v = rng.nextLong();
                    long w = mix(v, iterations);
                    for (int i = 0; i < 64; i++) {
                        long x = w ^ mix(v ^ (1L << i), iterations);
                        for (int j = 0; j < 64; j++) {
                            A[i][j] += ((x >>> j) & 1L);
                        }
                    }
                }
                double sumsq = 0.0;
                for (int i = 0; i < 64; i++) {
                    for (int j = 0; j < 64; j++) {
                        double v = A[i][j] - N * 0.5;
                        sumsq += v * v;
                    }
                }
                double result = sumsq * 0x1p-10 / N;
//                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println(shiftB + "," + shiftC + " with value " + total);
        }
        // Order 2
        {
            final long[][] A = new long[2016][64];
            double total = 0.0;
            for (int iterations = lower; iterations < upper; iterations += inc) {
                ArrayTools.fill(A, 0L);
                for (long n = 0; n < N; n++) {
                    long v = rng.nextLong();
                    long w = mix(v, iterations);
                    for (int i = 0, p = 0; i < 64; i++) {
                        for (int h = i + 1; h < 64; h++) {
                            long x = w ^ mix(v ^ (1L << i) ^ (1L << h), iterations);
                            for (int j = 0; j < 64; j++) {
                                A[p][j] += ((x >>> j) & 1L);
                            }
                            p++;
                        }
                    }
                }
                double sumsq = 0.0;
                for (int i = 0; i < 2016; i++) {
                    for (int j = 0; j < 64; j++) {
                        double v = A[i][j] - N * 0.5;
                        sumsq += v * v;
                    }
                }
                double result = sumsq * 0x1p-10 / N;
//                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println(shiftB + "," + shiftC + " with value " + total);
        }
    }
}