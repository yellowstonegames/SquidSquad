/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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
//    private static final int shiftB = 21, shiftC = 0;
//    private static final int shiftB = 11, shiftC = 0;
//    private static final int shiftB = 7, shiftC = 0;
//    private static final int shiftB = 42, shiftC = 0;

//    private static final int shiftB = 47, shiftC = 9; // best for Pico
//    private static final int shiftB = 47, shiftC = 55;
//    private static final int shiftB = 20, shiftC = 55;

//    private static final int shiftA = 60, shiftB = 17, shiftC = 38; //627.3542709350586, 4471.4318108558655
//    private static final int shiftA = 23, shiftB = 35, shiftC = 44; //644.3578462600708, 4726.132109165192
//    private static final int shiftA = 8, shiftB = 46, shiftC = 51; //684.567777633667, 4961.644707202911
//    private static final int shiftA = 40, shiftB = 60, shiftC = 25; //836.2562255859375, 5996.780225276947
//    private static final int shiftA = 38, shiftB = 22, shiftC = 26; //1098.985773563385, 7984.284327983856

//    private static final int shiftA = 41, shiftB = 31, shiftC = 36; //1888.7793316841125, 21652.011834144592
//    private static final int shiftA = 26, shiftB = 15, shiftC = 54; //478.7903299331665, 3286.795153617859

    // uses a xorshift by stateC (not shiftC) on fa + fb
//    private static final int shiftA = 35, shiftB = 46, shiftC = 58; //2653.063545703888, 39629.37545967102
//    private static final int shiftA = 26, shiftB = 15, shiftC = 54; //2623.6071248054504, 38092.00607776642
//    private static final int shiftA = 41, shiftB = 55, shiftC = 33; //889.4648361206055, 9337.738212108612
//    private static final int shiftA = 51, shiftB = 39, shiftC = 11; //441.1982617378235, 3610.378014087677
//    private static final int shiftA = 48, shiftB = 61, shiftC = 21; //297.73691511154175, 2426.498821735382
//    private static final int shiftA = 49, shiftB = 13, shiftC = 21; //261.5510506629944, 1977.7154269218445
//    private static final int shiftA = 27, shiftB = 41, shiftC = 11; //250.93736696243286, 1598.0102729797363
//    private static final int shiftA = 48, shiftB = 33, shiftC = 21; //217.55797004699707, 1501.3805575370789
//    private static final int shiftA = 50, shiftB = 15, shiftC = 9; //135.1098837852478, 873.8072996139526
//    private static final int shiftA = 17, shiftB = 43, shiftC = 9; //131.5948634147644, 876.6185884475708
//    private static final int shiftA = 34, shiftB = 52, shiftC = 11; //83.50280618667603, 613.0883617401123

    // uses (0xC6BC279692B5C323L * shiftC | 1L); for the increment
//    private static final int shiftA = 35, shiftB = 46, shiftC = 58; //4560.8842606544495, 75829.18932628632

    // uses (Hashers.determine(shiftC) | 1L); for the increment
//    private static final int shiftA = 33, shiftB = 57, shiftC = 61; //4583.470088005066, 77454.88206100464
//    private static final int shiftA = 49, shiftB = 13, shiftC = 21; //4565.860271930695, 76767.28993225098
//    private static final int shiftA = 38, shiftB = 55, shiftC = 54; //4709.632997512817, 77462.94840621948

    // uses three rotations
//    private static final int shiftA = 48, shiftB = 61, shiftC = 21; //5380.808502674103, 85902.72891759872
//    private static final int shiftA = 41, shiftB = 34, shiftC = 9; //4833.024730682373, 80877.52810287476
//    private static final int shiftA = 18, shiftB = 57, shiftC = 26; //4472.422231197357, 74673.06864023209
//    private static final int shiftA = 54, shiftB = 29, shiftC = 61; //4468.296925067902, 74011.22927713394

    // uses two rotations and a xorshift on fa
//    private static final int shiftA = 25, shiftB = 28, shiftC = 6; //2448.0621132850647, 38512.7782664299
//    private static final int shiftA = 40, shiftB = 34, shiftC = 6; //2441.77822637558, 38680.46908569336

    // uses two rotations and a xorshift on fa xored with fb
//    private static final int shiftA = 40, shiftB = 34, shiftC = 6; //3015.966280937195, 43838.31883382797
//    private static final int shiftA = 25, shiftB = 28, shiftC = 6; //1494.4292368888855, 20432.62754869461
//    private static final int shiftA = 50, shiftB = 28, shiftC = 6; //637.3507990837097, 5952.376519203186

    // uses fb + fa ^ fd
//    private static final int shiftA = 57, shiftB = 11, shiftC = 26; //3316.846664428711, 52582.95613670349

//    // uses fb ^ fa + fd
//    private static final int shiftA = 55, shiftB = 10, shiftC = 44; //3503.1398515701294, 54842.517048835754

//    // uses fb + fa + fd
//    private static final int shiftA = 56, shiftB = 31, shiftC = 3; //3572.523464202881, 56615.87005329132

    // uses fb + fc, fc ^ fd, fa ^ fb + fc, return fc
//    private static final int shiftA = 18, shiftB = 38, shiftC = 21; //169.10442113876343, 887.2168755531311
//    private static final int shiftA = 25, shiftB = 19, shiftC = 21; //162.84991025924683, 814.8368101119995
//    private static final int shiftA = 17, shiftB = 58, shiftC = 58; //131.76441717147827, 658.4919333457947

    // uses fb + fc, fc + fd, fa ^ fb + fc, return fc
//    private static final int shiftA = 18, shiftB = 38, shiftC = 21; //169.10442113876343, 887.2168755531311
//    private static final int shiftA = 25, shiftB = 19, shiftC = 21; //162.84991025924683, 814.8368101119995
//    private static final int shiftA = 17, shiftB = 58, shiftC = 58; //131.76441717147827, 658.4919333457947
    // uses  b ^ c, a ^ d, b + (a ^ d), return a
//    private static final int shiftA = 38, shiftB = 10, shiftC = 47; //2963.597212791443, 39302.08335542679
    // uses fb ^ fc, fc ^ fd, fa + fb, return fc
//    private static final int shiftA = 57, shiftB = 11, shiftC = 47; //3574.329375267029, 47901.35697364807
    // with constant 0x97C7894D00D8F3A3L and above shifts: 125.68258333206177, 643.3451714515686

    // uses b + c, c ^ d, a ^ b + c
//    private static final int shiftA = 35, shiftB = 10, shiftC = 23; //130.53586387634277, 740.2652406692505
// with constant 0xD5D1BDE118FDADE3L
    //127.29248857498169, 739.1366038322449
//    private static final int shiftA = 58, shiftB = 14, shiftC = 33; //156.01983261108398, 834.2125835418701
//    private static final int shiftA = 57, shiftB = 41, shiftC = 63; //165.92603731155396, 918.2142691612244
//    private static final int shiftA = 43, shiftB = 36, shiftC = 41; //159.943865776062, 905.73122215271
    // returning c
    private static final int shiftA = 6, shiftB = 23, shiftC = 21; //83.90083456039429, 514.3165535926819
// with constant 0xEB2A65F78A5C978BL
    //82.26387596130371, 511.67639112472534
//    private static final int shiftA = 52, shiftB = 44, shiftC = 26; //117.96543455123901, 623.2754197120667

    // uses fb + fc, fc + fd, fa ^ fb, return fa
//    private static final int shiftA = 46, shiftB = 19, shiftC = 16; //2491.0482816696167, 31176.99477672577
    // with constant 0xE3955173459932CDL and above shifts: 2444.460814476013, 30105.145670890808
//    private static final int shiftA = 56, shiftB = 32, shiftC = 16; //2528.6353096961975, 31876.023625850677

//
//    private static final long constant = 0xC6BC279692B5C323L;
//    private static final long constant = 0xCA762D55332BD5AFL;//0xA697335663D548B5L
    private static final long constant = (0xC6BC279692B5C323L * shiftC | 1L);
//    private static final long constant = 0xE3B1B6599529F247L;
//    private static final long constant = 0xEB2A65F78A5C978BL;//0xBC723139968EE63BL
//    private static final long constant = 0xADB5B12149E93C39L;
//    private static final long constant = 0xD5D1BDE118FDADE3L; //0xCA4BCD2E220F1B75L
//    private static final long constant = Hasher.determine(shiftC) | 1L;

    public static long mix(final long v, final int iterations) {
        long stateA = v;
        long stateB = 0L;
        long stateC = 0L;
        long stateD = 0L;
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

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = fa + 0xC6BC279692B5C323L;
//            stateB = Long.rotateLeft(fd, shiftA) ^ fa;
//            stateC = Long.rotateLeft(fb, shiftB) + fd;
//            stateD = Long.rotateLeft(fc, shiftC) ^ fb;

//            stateA = 0xD1342543DE82EF95L * fc;
//            stateB = fa ^ fb ^ fc;
//            stateC = Long.rotateLeft(fb, shiftB) + 0xC6BC279692B5C323L;

//            final long xp = stateA;
//            final long yp = stateB;
//            final long zp = stateC;
//            stateA = 0xD3833E804F4C574BL * zp;
//            stateB = yp - xp;
//            stateB = Long.rotateLeft(stateB, shiftB);
//            stateC = zp - yp;
//            stateC = Long.rotateLeft(stateC, shiftC);
//
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            final long ab = fa + fb;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = ab ^ ab >>> shiftC;
//            stateD = fd + 0xC6BC279692B5C323L;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fa + fb;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            final long ab = fa + fb;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = (ab ^ ab >>> shiftC);
//            stateD = fd + 0xC6BC279692B5C323L;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = Long.rotateLeft(fa + fb, shiftC);
//            stateD = fd + 0xC6BC279692B5C323L;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fa ^ fa >>> shiftC;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fb + fa;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fa * MassAvalancheEvaluator.goldenLong[shiftA];;
//            stateD = fd + constant;
//
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC ^= fb + fa;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc + fd, shiftB);
//            stateC = fa ^ fb + fc;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc + fd, shiftB);
//            stateC = fb ^ fa;
//            stateD = fd + constant;

//            //4560.8842606544495, 75829.18932628632
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, 35);
//            stateB = Long.rotateLeft(fc ^ fd, 46);
//            stateC = fa + fb;
//            stateD = fd + 0x06A0F81D3D2E35EFL;
////            return fc;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, 46);
//            stateB = Long.rotateLeft(fc + fd, 19);
//            stateC = fb ^ fa;
//            stateD = fd + 0xE3955173459932CDL;
//            return fa;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, 17);
//            stateB = Long.rotateLeft(fc + fd, 58);
//            stateC = fa ^ fb + fc;
//            stateD = fd + 0x97C7894D00D8F3A3L;
//            return fc;

//            //3140.449239730835, 48716.98849582672
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, 57);
//            stateB = Long.rotateLeft(fc ^ fd, 11);
//            stateC = fa + fb;
//            stateD = fd + 0xADB5B12149E93C39L;
////            return fc;
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            final long bc = fb ^ fc;
//            final long ad = fa ^ fd;
//            stateA = Long.rotateLeft(bc, shiftA);
//            stateB = Long.rotateLeft(ad, shiftB);
//            stateC = ad + fb;
//            stateD = fd + constant;

            final long fa = stateA;
            final long fb = stateB;
            final long fc = stateC;
            final long fd = stateD;
            final long bc = fb + fc;
            final long cd = fc ^ fd;
            stateA = (bc << shiftA | bc >>> -shiftA);
            stateB = (cd << shiftB | cd >>> -shiftB);
            stateC = fa ^ bc;
            stateD = fd + constant;
        }
        return stateC;
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
            System.out.println("Order 1: " + shiftA + "," + shiftB + "," + shiftC + " with value " + total);
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
            System.out.println("Order 2: " + shiftA + "," + shiftB + "," + shiftC + " with value " + total);
        }
    }
}