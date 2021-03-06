package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.DistinctRandom;

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

/*
N 1024
lower 16, upper 43, inc 1

C A^C A^B return A^B
Order 1: 41,53 with value 26.896333694458008
Order 2: 41,53 with value 850.7179107666016

Order 1: 12,45 with value 27.10381507873535
Order 2: 12,45 with value 851.2522020339966

Order 1: 39,61 with value 27.751235008239746
Order 2: 39,61 with value 851.8352279663086

Order 1: 46,17 with value 28.045936584472656
Order 2: 46,17 with value 850.9402856826782

Order 1: 12,16 with value 27.202072143554688
Order 2: 12,16 with value 851.1701011657715

Order 1: 23,56 with value 26.92930030822754
Order 2: 23,56 with value 850.3286733627319

Order 1: 21,56 with value 27.27608013153076
Order 2: 21,56 with value 850.0220127105713

N 2048
lower 5, upper 12, inc 1

Marge 7

With 5 iterations: 925.2249054908752
With 6 iterations: 100.5061149597168
With 7 iterations: 57.004836082458496
With 8 iterations: 2.2849631309509277
With 9 iterations: 1.0802512168884277
With 10 iterations: 0.9916057586669922
With 11 iterations: 1.0030179023742676
Order 1: 7,0 with value 1088.0956945419312
With 5 iterations: 17900.08853340149
With 6 iterations: 981.1459832191467
With 7 iterations: 533.8662786483765
With 8 iterations: 33.63921117782593
With 9 iterations: 31.39405870437622
With 10 iterations: 31.39041566848755
With 11 iterations: 31.50369882583618
Order 2: 7,0 with value 19543.02817964554


 */
public class AvalancheEvaluator {
    private static final long N = 1L << 11;
//        private static final int lower = 9, upper = 19, inc = 1;
    private static final int lower = 8, upper = 19, inc = 1;

//    private static final int shiftB = 42, shiftC = 27;
//    private static final int shiftB = 27, shiftC = 42;
//    private static final int shiftB = 27, shiftC = 21; // good contender
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

//    private static final int shiftB = 41, shiftC = 53;
//    private static final int shiftB = 12, shiftC = 45;
//    private static final int shiftB = 12, shiftC = 16;
//    private static final int shiftB = 39, shiftC = 61;
//    private static final int shiftB = 46, shiftC = 17;
//    private static final int shiftB = 12, shiftC = 44;
//    private static final int shiftB = 26, shiftC = 9; // great contender
//    private static final int shiftB = 27, shiftC = 9;  // current best
//    private static final int shiftB = 34, shiftC = 42;
    private static final int shiftB = 21, shiftC = 0;
//    private static final int shiftB = 11, shiftC = 0;
//    private static final int shiftB = 7, shiftC = 0;
//    private static final int shiftB = 42, shiftC = 0;

//    private static final int shiftB = 47, shiftC = 9; // best for Pico
//    private static final int shiftB = 47, shiftC = 55;
//    private static final int shiftB = 20, shiftC = 55;

    public static long mix(final long v, final int iterations) {
        long stateA = v;
        long stateB = 0L;
        long stateC = 0L;
        for (int i = 0; i < iterations; i++) {
//            final long a0 = stateA;
//            final long b0 = stateB;
//            final long c0 = stateC;
//            stateA = 0xC6BC279692B5C323L + c0;
//            stateB = Long.rotateLeft(a0, shiftB) + c0;
//            stateC = Long.rotateLeft(b0, shiftC) ^ a0;

            ////romutrio-like but with a XOR to make part of it an XLCG instead of an MCG.
//            final long a0 = 0xD1342543DE82EF95L ^ stateC;
//            final long b0 = stateA ^ stateC;
//            final long c0 = stateB + stateA;
//            stateA = 0xD1B54A32D192ED03L * a0;
//            stateB = Long.rotateLeft(b0, shiftB);
//            stateC = Long.rotateLeft(c0, shiftC);
            
            final long fa = stateA;
            final long fb = stateB;
            final long fc = stateC;
            stateA = 0xD1342543DE82EF95L * fc;
            stateB = fa ^ fb ^ fc;
            stateC = Long.rotateLeft(fb, shiftB) + 0xC6BC279692B5C323L;

//            final long xp = stateA;
//            final long yp = stateB;
//            final long zp = stateC;
//            stateA = 0xD3833E804F4C574BL * zp;
//            stateB = yp - xp;
//            stateB = Long.rotateLeft(stateB, shiftB);
//            stateC = zp - yp;
//            stateC = Long.rotateLeft(stateC, shiftC);
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
                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println("Order 1: " + shiftB + "," + shiftC + " with value " + total);
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
                System.out.println("With " + iterations + " iterations: " + result);
                total += result;
            }
            System.out.println("Order 2: " + shiftB + "," + shiftC + " with value " + total);
        }
    }
}