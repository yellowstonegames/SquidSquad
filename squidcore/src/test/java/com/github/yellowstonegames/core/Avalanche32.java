package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.support.DistinctRandom;

public class Avalanche32 {
    private static final long N = 1L << 6;
    //    private static final int lower = 9, upper = 19, inc = 1;
//    private static final int lower = 8, upper = 41, inc = 4;
//    private static final int lower = 1, upper = 15, inc = 1;
    // this is the one most of the worse-avalanche tests use.
//    private static final int lower = 10, upper = 20, inc = 1;
    // this is the one most of the better-avalanche tests use.
    private static final int lower = 8, upper = 19, inc = 1;

    public static int mix(final int v, final int shiftA, final int shiftB, final int shiftC, final int iterations) {
        int stateA = v;
        int stateB = 0;
        int stateC = 0;
        int stateD = 0;
        final int constantAdd = (int)(0xF1357AEA2E62A9C5L * shiftC >>> 32) | 1;
        final int constantMul = (int)(0xF1357AEA2E62A9C5L * shiftC >>> 44) | 1;
//        final long constant = 0xC6BC279692B5C323L;
        for (int i = 0; i < iterations; i++) {
            final int fa = stateA;
            final int fb = stateB;
            final int fc = stateC;
            final int fd = stateD;
/*
Using lower = 10:

Order 1, Best Ten with total:
#0: 28,9,30 with value 0.86126708984375
#1: 13,20,16 with value 0.8681640625
#2: 8,27,4 with value 0.881103515625
#3: 20,29,5 with value 0.8817138671875
#4: 21,14,4 with value 0.884033203125
#5: 29,20,18 with value 0.88580322265625
#6: 27,7,21 with value 0.88616943359375
#7: 20,13,10 with value 0.89453125
#8: 28,8,20 with value 0.8958740234375
#9: 29,9,23 with value 0.8975830078125
Order 2, Best Ten with total:
#0: 14,21,21 with value 14.815673828125
#1: 20,28,13 with value 14.9351806640625
#2: 20,13,4 with value 14.95001220703125
#3: 28,19,21 with value 14.96734619140625
#4: 8,27,21 with value 15.00360107421875
#5: 20,13,16 with value 15.00634765625
#6: 9,28,13 with value 15.010009765625
#7: 29,20,21 with value 15.010986328125
#8: 8,27,29 with value 15.0306396484375
#9: 28,9,2 with value 15.03155517578125
*/
//            stateA = Integer.rotateLeft(fb ^ fc, shiftA);
//            stateB = Integer.rotateLeft(fc ^ fd, shiftB);
//            stateC = fa + fb;
//            stateD = fd + constantAdd;

/*
Using lower = 8 (for better avalanche versions):

Order 1, Best Ten with total:
#0: 19,24,14 with value 0.83648681640625
#1: 4,11,11 with value 0.88922119140625
#2: 8,18,5 with value 0.89337158203125
#3: 13,4,7 with value 0.8935546875
#4: 15,11,20 with value 0.89434814453125
#5: 13,8,11 with value 0.900146484375
#6: 27,12,11 with value 0.9034423828125
#7: 4,10,8 with value 0.90545654296875
#8: 26,11,24 with value 0.90802001953125
#9: 11,24,21 with value 0.9097900390625
Order 2, Best Ten with total:
#0: 18,28,26 with value 14.81103515625
#1: 23,18,13 with value 14.94769287109375
#2: 19,24,23 with value 14.95556640625
#3: 26,8,21 with value 14.96832275390625
#4: 15,6,11 with value 14.97503662109375
#5: 22,18,19 with value 14.9775390625
#6: 14,21,29 with value 14.9959716796875
#7: 21,15,11 with value 15.008056640625
#8: 26,11,16 with value 15.0106201171875
#9: 19,24,27 with value 15.02093505859375

 */
            stateA = Integer.rotateLeft(fb ^ fc, shiftA);
            stateB = Integer.rotateLeft(fc ^ fd, shiftB);
            stateC = fa ^ fb + fc;
            stateD = fd + constantAdd;
        }
        return stateC;
    }

    public static void main(String[] args) {
        DistinctRandom rng = new DistinctRandom(123456789L);
        // Order 1
        if(true)
        {
            final long[][] A = new long[32][32];
            final IntObjectOrderedMap<Double> res = new IntObjectOrderedMap<>(4096),
                    totals = new IntObjectOrderedMap<>(4096);
            for (int iterations = lower; iterations < upper; iterations += inc) {
                int sa = 1, sb = 3, sc = 5;
                for (int p = 0; p < 0x4000; p++) {
                    sa = (sa * 5 + 3 & 31);
                    if(sa < 23) sb = (sb * 5 + 3 & 31);
                    if(sb < 11) sc = (sc * 5 + 3 & 31);
                        ArrayTools.fill(A, 0L);
                        for (long n = 0; n < N; n++) {
                            int v = rng.nextInt();
                            int w = mix(v, sa, sb, sc, iterations);
                            for (int i = 0; i < 32; i++) {
                                int x = w ^ mix(v ^ (1 << i), sa, sb, sc, iterations);
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
                        int id = (sa & 31) | (sb & 31) << 5 | (sc & 31) << 10;
                        res.put(id, result);
                        totals.put(id, result + totals.getOrDefault(id, 0.0));

                }
                System.out.println("Completed run for " + iterations + " iterations, order 1.");
                res.sortByValue(Double::compareTo);
                System.out.println("Order 1, Best Ten with " + iterations + " iterations:");
                for (int i = 0; i < 10; i++) {
                    final int k = res.keyAt(i);
                    System.out.println("#" + i + ": " + (k & 31) + "," + (k >>> 5 & 31) + "," + (k >>> 10) + " with value " + res.getAt(i));
                }

//                for (int sb = 1; sb < 64; sb++) {
//                    for (int sc = 1; sc < 64; sc++) {
//                        ArrayTools.fill(A, 0L);
//                        for (long n = 0; n < N; n++) {
//                            long v = rng.nextLong();
//                            long w = mix(v, sb, sc, iterations);
//                            for (int i = 0; i < 64; i++) {
//                                long x = w ^ mix(v ^ (1L << i), sb, sc, iterations);
//                                for (int j = 0; j < 64; j++) {
//                                    A[i][j] += ((x >>> j) & 1L);
//                                }
//                            }
//                        }
//                        double sumsq = 0.0;
//                        for (int i = 0; i < 64; i++) {
//                            for (int j = 0; j < 64; j++) {
//                                double v = A[i][j] - N * 0.5;
//                                sumsq += v * v;
//                            }
//                        }
//                        double result = sumsq * 0x1p-10 / N;
//                        res.put(sb | sc << 6, result);
//                        totals.put(sb | sc << 6, result + totals.getOrDefault(sb | sc << 6, 0.0));
//                    }
//                }
//                System.out.println("Completed run for " + iterations + " iterations, order 1.");
//                res.sortByValue(Double::compareTo);
//                System.out.println("Order 1, Best Ten with " + iterations + " iterations:");
//                for (int i = 0; i < 10; i++) {
//                    final int k = res.keyAt(i);
//                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//                }
//                System.out.println("Order 1, Worst Ten with " + iterations + " iterations:");
//                for (int i = res.size() - 10; i < res.size(); i++) {
//                    final int k = res.keyAt(i);
//                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//                }
            }
            totals.sortByValue(Double::compareTo);
            System.out.println("Order 1, Best Ten with total:");
            for (int i = 0; i < 10; i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 31) + "," + (k >>> 5 & 31) + "," + (k >>> 10) + " with value " + res.getAt(i));
            }
//            totals.sortByValue(Double::compareTo);
//            System.out.println("Order 1, Best Ten with total:");
//            for (int i = 0; i < 10; i++) {
//                final int k = totals.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
//            }

//            System.out.println("Order 1, Worst Ten with total:");
//            for (int i = totals.size() - 10; i < totals.size(); i++) {
//                final int k = totals.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
//            }
        }
        // Order 2
        if(true)
        {
            final long[][] A = new long[496][32];
            final IntObjectOrderedMap<Double> res = new IntObjectOrderedMap<>(4096),
                    totals = new IntObjectOrderedMap<>(4096);
            for (int iterations = lower; iterations < upper; iterations += inc) {
                int sa = 1, sb = 3, sc = 5;
                for (int e = 0; e < 0x4000; e++) {
                    sa = (sa >>> 1 ^ (-(sa & 1) & 0x30));
                    if (sa < 23) sb = (sb >>> 1 ^ (-(sb & 1) & 0x30));
                    if (sb < 11) sc = (sc >>> 1 ^ (-(sc & 1) & 0x30));
                    ArrayTools.fill(A, 0L);
                    for (long n = 0; n < N; n++) {
                        int v = rng.nextInt();
                        int w = mix(v, sa, sb, sc, iterations);
                        for (int i = 0, p = 0; i < 32; i++) {
                            for (int h = i + 1; h < 32; h++) {
                                int x = w ^ mix(v ^ (1 << i) ^ (1 << h), sa, sb, sc, iterations);
                                for (int j = 0; j < 32; j++) {
                                    A[p][j] += ((x >>> j) & 1);
                                }
                                ++p;
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
                    int id = (sa & 31) | (sb & 31) << 5 | (sc & 31) << 10;
                    res.put(id, result);
                    totals.put(id, result + totals.getOrDefault(id, 0.0));
                }
                System.out.println("Completed run for " + iterations + " iterations, order 2.");
                res.sortByValue(Double::compareTo);
                System.out.println("Order 2, Best Ten with " + iterations + " iterations:");
                for (int i = 0; i < 10; i++) {
                    final int k = res.keyAt(i);
                    System.out.println("#" + i + ": " + (k & 31) + "," + (k >>> 5 & 31) + "," + (k >>> 10) + " with value " + res.getAt(i));
                }
//                for (int sb = 1; sb < 64; sb++) {
//                    for (int sc = 1; sc < 64; sc++) {
//                        ArrayTools.fill(A, 0L);
//                        for (long n = 0; n < N; n++) {
//                            long v = rng.nextLong();
//                            long w = mix(v, sb, sc, iterations);
//                            for (int i = 0, p = 0; i < 64; i++) {
//                                for (int h = i + 1; h < 64; h++) {
//                                    long x = w ^ mix(v ^ (1L << i) ^ (1L << h), sb, sc, iterations);
//                                    for (int j = 0; j < 64; j++) {
//                                        A[p][j] += ((x >>> j) & 1L);
//                                    }
//                                    p++;
//                                }
//                            }
//                        }
//                        double sumsq = 0.0;
//                        for (int i = 0; i < 2016; i++) {
//                            for (int j = 0; j < 64; j++) {
//                                double v = A[i][j] - N * 0.5;
//                                sumsq += v * v;
//                            }
//                        }
//                        double result = sumsq * 0x1p-10 / N;
//                        res.put(sb | sc << 6, result);
//                        totals.put(sb | sc << 6, result + totals.getOrDefault(sb | sc << 6, 0.0));
//                    }
//                }
//                System.out.println("Completed run for " + iterations + " iterations, order 2.");
//                res.sortByValue(Double::compareTo);
//                System.out.println("Order 2, Best Ten with " + iterations + " iterations:");
//                for (int i = 0; i < 10; i++) {
//                    final int k = res.keyAt(i);
//                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//                }

//                System.out.println("Order 2, Worst Ten with " + iterations + " iterations:");
//                for (int i = res.size() - 10; i < res.size(); i++) {
//                    final int k = res.keyAt(i);
//                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//                }
            }
            totals.sortByValue(Double::compareTo);
            System.out.println("Order 2, Best Ten with total:");
            for (int i = 0; i < 10; i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 31) + "," + (k >>> 5 & 31) + "," + (k >>> 10) + " with value " + res.getAt(i));
            }
//            System.out.println("Order 2, Worst Ten with total:");
//            for (int i = totals.size() - 10; i < totals.size(); i++) {
//                final int k = totals.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
//            }
        }
    }
}