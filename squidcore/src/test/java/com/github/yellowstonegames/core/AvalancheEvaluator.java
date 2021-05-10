package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.IntObjectOrderedMap;
/*
lower=10, upper=43, inc=1, N=1L<<10

42,27 with value 32.96325206756592
42,27 with value 1038.8377265930176

27,21 with value 33.158623695373535
27,21 with value 1039.9528322219849

27,42 with value 33.130043029785156
27,42 with value 1039.803771018982

20,49 with value 32.97402095794678
20,49 with value 1040.877278327942

47,5 with value 33.04857158660889
47,5 with value 1040.0553369522095

23,39 with value 41.84683799743652
23,39 with value 1045.4830961227417

23,46 with value 98.36502075195312
23,46 with value 1183.221848487854

19,27 with value 36.011539459228516
19,27 with value 1040.9575328826904

45,27 with value 37.226468086242676
45,27 with value 1044.4009790420532

45,37 with value 38.16234016418457
45,37 with value 1043.5275011062622

23,47 with value 38.90561389923096
23,47 with value 1045.0505142211914

47,23 with value 34.5958309173584
47,23 with value 1040.4845209121704

43,27 with value 33.33030128479004
43,27 with value 1040.811201095581

lower=10, upper=43, inc=1, N=1L<<13

42,27 with value 34.37620997428894
42,27 with value 1039.8712446689606

27,42 with value 33.527759313583374
27,42 with value 1040.093014240265

20,49 with value 33.67428004741669
20,49 with value 1039.8196394443512

49,20 with value 36.126816391944885
49,20 with value 1039.27807366848

23,46 with value 550.7339763641357
23,46 with value 2202.4629291296005

lower=10, upper=43, inc=1, N=1L<<14

42,27 with value 35.93620681762695
42,27 with value 1039.2831701636314

27,42 with value 33.95515334606171
27,42 with value 1040.4130591750145

27,21 with value 34.83214694261551
27,21 with value 1039.2323777079582

27,19 with value 121.4011600613594
27,19 with value 1132.775380551815

20,49 with value 33.97506892681122
20,49 with value 1039.899165213108

20,50 with value 36.060500144958496
20,50 with value 1040.1772978901863

5,49 with value 42.28814917802811
5,49 with value 1042.9201802015305

^ + + ^, N=1L<<14

stateA
42,27 with value 33.04507076740265
42,27 with value 1040.020654797554

stateA
27,42 with value 33.162183344364166
27,42 with value 1038.5555713772774

stateB
42,27 with value 33.051907658576965
42,27 with value 1039.887927532196

stateB
27,42 with value 32.75392371416092
27,42 with value 1040.356209218502

stateC
27,42 with value 33.104141652584076
27,42 with value 1039.4659236073494
*/
public class AvalancheEvaluator {
    private static final long N = 1L << 14;
//        private static final int lower = 9, upper = 19, inc = 1;
    private static final int lower = 10, upper = 43, inc = 1;

    private static final int shiftB = 42, shiftC = 27;
//    private static final int shiftB = 27, shiftC = 42;
//    private static final int shiftB = 27, shiftC = 21;
//    private static final int shiftB = 20, shiftC = 49;
//    private static final int shiftB = 20, shiftC = 50;
//    private static final int shiftB = 49, shiftC = 20;
//    private static final int shiftB = 23, shiftC = 39;
//    private static final int shiftB = 23, shiftC = 46;
//    private static final int shiftB = 19, shiftC = 27;
//    private static final int shiftB = 27, shiftC = 19;
//    private static final int shiftB = 45, shiftC = 27;
//    private static final int shiftB = 45, shiftC = 37;
//    private static final int shiftB = 47, shiftC = 5;
//    private static final int shiftB = 47, shiftC = 23;
//    private static final int shiftB = 23, shiftC = 47;
//    private static final int shiftB = 43, shiftC = 27;
//    private static final int shiftB = 5, shiftC = 49;

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
            stateC = Long.rotateLeft(b0, shiftC) ^ a0;
        }
        return stateB;
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