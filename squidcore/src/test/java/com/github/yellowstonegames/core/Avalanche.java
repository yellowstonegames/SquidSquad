package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.ds.support.DistinctRandom;
/*
lower=9, upper=19, inc=1, N=1L<<10

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

/*
lower=8, upper=41, inc=4, N=1L<<9

v 0 0
Order 1, Best Ten with total:
#0: 20,6 with value 22.667417526245117
#1: 27,21 with value 22.742338180541992
#2: 20,49 with value 23.08631134033203
#3: 51,42 with value 23.67423439025879
#4: 6,42 with value 23.6773681640625
#5: 41,7 with value 24.245046615600586
#6: 37,42 with value 24.394433975219727
#7: 17,59 with value 24.6146297454834
#8: 13,42 with value 24.623638153076172
#9: 20,50 with value 24.688993453979492
Order 2, Best Ten with total:
#0: 20,49 with value 313.23787117004395
#1: 27,21 with value 313.6917018890381
#2: 20,6 with value 313.79674911499023
#3: 55,30 with value 315.06591415405273
#4: 6,42 with value 316.889347076416
#5: 51,42 with value 317.2352352142334
#6: 37,42 with value 317.81798553466797
#7: 17,5 with value 318.23436546325684
#8: 48,37 with value 318.4336643218994
#9: 13,42 with value 318.71700286865234
 */
/*
lower=10, upper=41, inc=1, N=1L<<11

// note, this takes over 12 hours to run.

v 0 0

#0: 37,57 with value 30.90493154525757
#1: 13,55 with value 30.92151927947998
#2: 47,52 with value 31.059566497802734
#3: 49,55 with value 31.07398748397827
#4: 17,59 with value 31.08048677444458
#5: 9,30 with value 31.082557678222656
#6: 36,48 with value 31.088653087615967
#7: 6,40 with value 31.102781295776367
#8: 25,54 with value 31.10760498046875
#9: 37,42 with value 31.108074188232422
Order 2, Best Ten with total:
#0: 20,50 with value 974.9239659309387
#1: 5,49 with value 975.0130786895752
#2: 59,26 with value 975.1056275367737
#3: 39,30 with value 975.1386919021606
#4: 50,59 with value 975.2236862182617
#5: 58,27 with value 975.2764978408813
#6: 6,50 with value 975.3469920158386
#7: 27,7 with value 975.3521447181702
#8: 48,42 with value 975.384928226471
#9: 23,10 with value 975.3943996429443
 */
public class Avalanche {
    private static final long N = 1L << 11;
    //    private static final int lower = 9, upper = 19, inc = 1;
//    private static final int lower = 8, upper = 41, inc = 4;
    private static final int lower = 10, upper = 41, inc = 1;

    public static long mix(final long v, final int shiftB, final int shiftC, final int iterations) {
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
            for (int iterations = lower; iterations < upper; iterations += inc) {
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
                System.out.println("Completed run for " + iterations + " iterations, order 1.");
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
            for (int iterations = lower; iterations < upper; iterations += inc) {
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
                System.out.println("Completed run for " + iterations + " iterations, order 2.");
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