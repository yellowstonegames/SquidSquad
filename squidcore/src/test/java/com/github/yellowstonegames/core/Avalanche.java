package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
/*
v 0 0
Order 1, Best Ten with total:
#0: 27,42 with value 13.172428131103516
#1: 41,28 with value 13.451096534729004
#2: 16,54 with value 14.176259994506836
#3: 48,54 with value 14.213671684265137
#4: 7,18 with value 14.482749938964844
#5: 40,6 with value 14.483317375183105
#6: 47,5 with value 14.621721267700195
#7: 54,48 with value 14.692398071289062
#8: 9,30 with value 14.75678825378418
#9: 53,17 with value 14.758763313293457
Order 2, Best Ten with total:
#0: 27,42 with value 317.16664123535156
#1: 28,23 with value 317.52707386016846
#2: 59,40 with value 317.687536239624
#3: 53,17 with value 317.9339895248413
#4: 41,28 with value 317.9659032821655
#5: 48,54 with value 318.26753425598145
#6: 7,18 with value 318.3757858276367
#7: 9,30 with value 318.4265670776367
#8: 47,5 with value 318.4455728530884
#9: 27,21 with value 318.489990234375

v v v
Order 1, Best Ten with total:
#0: 35,11 with value 10.102267265319824
#1: 53,47 with value 10.124319076538086
#2: 42,6 with value 10.180037498474121
#3: 6,20 with value 10.195343017578125
#4: 42,27 with value 10.219003677368164
#5: 40,58 with value 10.222501754760742
#6: 23,28 with value 10.230522155761719
#7: 27,42 with value 10.230705261230469
#8: 48,10 with value 10.244318962097168
#9: 27,21 with value 10.251564025878906

v -1 -1
Order 1, Best Ten with total:
#0: 27,42 with value 14.377165794372559
#1: 40,6 with value 15.453439712524414
#2: 16,54 with value 15.527392387390137
#3: 41,28 with value 15.557866096496582
#4: 48,54 with value 15.617321968078613
#5: 20,50 with value 15.937841415405273
#6: 53,17 with value 15.964409828186035
#7: 6,21 with value 16.02928638458252
#8: 27,21 with value 16.036765098571777
#9: 47,5 with value 16.060653686523438

v 0xC6BC279692B5C323L 0xC6BC279692B5C323L
Order 1, Best Ten with total:
#0: 27,42 with value 12.171524047851562
#1: 58,42 with value 13.001543998718262
#2: 16,54 with value 13.012740135192871
#3: 47,5 with value 13.062094688415527
#4: 41,28 with value 13.078075408935547
#5: 48,54 with value 13.101665496826172
#6: 40,6 with value 13.250473022460938
#7: 18,5 with value 13.333516120910645
#8: 58,40 with value 13.396611213684082
#9: 27,21 with value 13.464906692504883
Order 2, Best Ten with total:
#0: 27,42 with value 316.110502243042
#1: 41,28 with value 316.59771633148193
#2: 48,54 with value 316.9004831314087
#3: 18,5 with value 317.26579666137695
#4: 53,17 with value 317.2700672149658
#5: 27,21 with value 317.27076721191406
#6: 7,18 with value 317.32630252838135
#7: 9,30 with value 317.48772716522217
#8: 47,5 with value 317.53620433807373
#9: 54,48 with value 317.56426429748535

 */
public class Avalanche {
    private static final long N = 1L << 10;

    public static long mix(final long v, final int shiftB, final int shiftC, final int iterations){
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
            final IntObjectOrderedMap<Double> res = new IntObjectOrderedMap<>(4096),
                    totals = new IntObjectOrderedMap<>(4096);
            for (int iterations = 9; iterations < 19; iterations++) {
                for (int sb = 1; sb < 64; sb++) {
                    for (int sc = 1; sc < 64; sc++) {
                        ArrayTools.fill(A, 0L);
                        for (long n = 0; n < N; n++) {
                            long v = rng.nextLong();
                            long w = mix(v, sb, sc, iterations);
                            for (int i = 0; i < 64; i++) {
                                long x = w ^ mix(v ^ (1L << i), sb, sc, iterations);
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
                        res.put(sb | sc << 6, result);
                        totals.put(sb | sc << 6, result + totals.getOrDefault(sb | sc << 6, 0.0));
                    }
                }
//            res.sortByValue(Double::compareTo);
//            System.out.println("Order 1, Best Ten with " + iterations + " iterations:");
//            for (int i = 0; i < 10; i++) {
//                final int k = res.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//            }
//            System.out.println("Order 1, Worst Ten with " + iterations + " iterations:");
//            for (int i = res.size() - 10; i < res.size(); i++) {
//                final int k = res.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//            }
            }
            totals.sortByValue(Double::compareTo);
            System.out.println("Order 1, Best Ten with total:");
            for (int i = 0; i < 10; i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
            }
            System.out.println("Order 1, Worst Ten with total:");
            for (int i = totals.size() - 10; i < totals.size(); i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
            }
        }
        // Order 2
        {
            final long[][] A = new long[2016][64];
            final IntObjectOrderedMap<Double> res = new IntObjectOrderedMap<>(4096),
                    totals = new IntObjectOrderedMap<>(4096);
            for (int iterations = 9; iterations < 19; iterations++) {
                for (int sb = 1; sb < 64; sb++) {
                    for (int sc = 1; sc < 64; sc++) {
                        ArrayTools.fill(A, 0L);
                        for (long n = 0; n < N; n++) {
                            long v = rng.nextLong();
                            long w = mix(v, sb, sc, iterations);
                            for (int i = 0, p = 0; i < 64; i++) {
                                for (int h = i + 1; h < 64; h++) {
                                    long x = w ^ mix(v ^ (1L << i) ^ (1L << h), sb, sc, iterations);
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
                        res.put(sb | sc << 6, result);
                        totals.put(sb | sc << 6, result + totals.getOrDefault(sb | sc << 6, 0.0));
                    }
                }
//            res.sortByValue(Double::compareTo);
//            System.out.println("Order 1, Best Ten with " + iterations + " iterations:");
//            for (int i = 0; i < 10; i++) {
//                final int k = res.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//            }
//            System.out.println("Order 1, Worst Ten with " + iterations + " iterations:");
//            for (int i = res.size() - 10; i < res.size(); i++) {
//                final int k = res.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + res.getAt(i));
//            }
            }
            totals.sortByValue(Double::compareTo);
            System.out.println("Order 2, Best Ten with total:");
            for (int i = 0; i < 10; i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
            }
            System.out.println("Order 2, Worst Ten with total:");
            for (int i = totals.size() - 10; i < totals.size(); i++) {
                final int k = totals.keyAt(i);
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
            }
        }
    }
}
