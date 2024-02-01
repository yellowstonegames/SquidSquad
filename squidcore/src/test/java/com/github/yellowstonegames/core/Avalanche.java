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

import com.github.tommyettinger.ds.IntObjectOrderedMap;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.tommyettinger.digital.ArrayTools;

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

N 1024
lower 10 upper 41 inc 1
bijection, C A^C A^B

Order 1, Best Ten with total:
#0: 9,42 with value 717.4657564163208
#1: 9,40 with value 750.3822650909424
#2: 9,43 with value 751.0811157226562
#3: 47,55 with value 756.5535402297974
#4: 54,18 with value 756.6095085144043
#5: 41,8 with value 762.801344871521
#6: 56,25 with value 762.9224586486816
#7: 41,7 with value 766.8172025680542
#8: 9,41 with value 767.7774839401245
#9: 47,54 with value 768.7563028335571
Order 2, Best Ten with total:
#0: 9,42 with value 7203.092739105225
#1: 47,55 with value 7451.971041679382
#2: 54,18 with value 7461.610792160034
#3: 9,43 with value 7545.473958015442
#4: 56,26 with value 7548.539187431335
#5: 9,40 with value 7555.166534423828
#6: 41,8 with value 7561.018929481506
#7: 56,25 with value 7563.591048240662
#8: 9,44 with value 7590.688142776489
#9: 41,7 with value 7614.589632987976

N 256
lower 16 upper 21 inc 1
C A^C A^B return A^B

Order 1, Best Ten with total:
#0: 12,16 with value 4.879127502441406
#1: 39,34 with value 4.883136749267578
#2: 49,60 with value 4.884212493896484
#3: 44,58 with value 4.8884735107421875
#4: 23,56 with value 4.897991180419922
#5: 12,59 with value 4.899467468261719
#6: 36,51 with value 4.900272369384766
#7: 41,53 with value 4.901332855224609
#8: 36,57 with value 4.902675628662109
#9: 45,32 with value 4.903526306152344
Order 2, Best Ten with total:
#0: 17,42 with value 156.59167098999023
#1: 39,61 with value 156.6676368713379
#2: 58,25 with value 156.69777297973633
#3: 52,22 with value 156.76671981811523
#4: 47,56 with value 156.77505111694336
#5: 8,28 with value 156.79352188110352
#6: 21,56 with value 156.80806350708008
#7: 55,26 with value 156.81796646118164
#8: 58,39 with value 156.83041381835938
#9: 40,35 with value 156.84626007080078


N 1024
lower 15 upper 21 inc 1
B^C A A^B

Order 1, Best Ten with total:
#0: 54,7 with value 5.862697601318359
#1: 42,60 with value 5.870357513427734
#2: 34,58 with value 5.87115478515625
#3: 30,23 with value 5.896766662597656
#4: 18,30 with value 5.898929595947266
#5: 39,48 with value 5.9075469970703125
#6: 18,44 with value 5.908058166503906
#7: 22,49 with value 5.911647796630859
#8: 33,26 with value 5.915760040283203
#9: 34,41 with value 5.921047210693359


N 64
Pico
lower 13 upper 20 inc 1

Order 1, Best Ten with total:
#0: 47,55 with value 7.00250244140625
#1: 47,9 with value 7.009857177734375
#2: 20,55 with value 7.011016845703125
#3: 15,9 with value 7.05023193359375
#4: 4,49 with value 7.0688629150390625
#5: 43,9 with value 7.0709381103515625
#6: 61,18 with value 7.071746826171875
#7: 61,14 with value 7.0733489990234375
#8: 49,24 with value 7.076629638671875
#9: 18,10 with value 7.0789031982421875
Order 2, Best Ten with total:
#0: 41,29 with value 219.73733520507812
#1: 22,51 with value 219.78207397460938
#2: 37,16 with value 219.80178833007812
#3: 37,45 with value 219.852294921875
#4: 17,9 with value 219.8617401123047
#5: 15,8 with value 219.97645568847656
#6: 59,23 with value 219.98419189453125
#7: 7,29 with value 219.988525390625
#8: 38,36 with value 220.01429748535156
#9: 52,17 with value 220.0272216796875

 */
public class Avalanche {
    private static final long N = 1L << 6;
    //    private static final int lower = 9, upper = 19, inc = 1;
//    private static final int lower = 8, upper = 41, inc = 4;
//    private static final int lower = 1, upper = 15, inc = 1;
    // this is the one most of the tests use.
    private static final int lower = 10, upper = 20, inc = 1;

    public static long mix(final long v, final int shiftA, final int shiftB, final int shiftC, final int iterations) {
        long stateA = v;
        long stateB = 0L;
        long stateC = 0L;
        long stateD = 0L;
        final long constant = 0xC6BC279692B5C323L * shiftC | 1L;
//        final long constant = 0xC6BC279692B5C323L;
        for (int i = 0; i < iterations; i++) {
//            final long a0 = stateA;
//            final long b0 = stateB;
//            final long c0 = stateC;
//            stateA = b0 ^ c0 + 0xC6BC279692B5C323L;
//            stateB = Long.rotateLeft(a0, shiftB) + c0;
//            stateC = Long.rotateLeft(b0, shiftC) + a0;

//            final long a0 = stateB ^ stateC;
//            final long b0 = stateA;
//            final long c0 = stateA ^ stateB;
//            stateA = 0xC6BC279692B5C323L + a0;
//            stateB = Long.rotateLeft(b0, shiftB);
//            stateC = Long.rotateLeft(c0, shiftC);

//            final long a0 = stateC;
//            final long b0 = stateA ^ stateC;
//            final long c0 = stateA ^ stateB;
//            stateA = 0xC6BC279692B5C323L + a0;
//            stateB = Long.rotateLeft(b0, shiftB);
//            stateC = Long.rotateLeft(c0, shiftC);


//            final long a0 = 0xD1342543DE82EF95L ^ stateC;
//            final long b0 = stateA ^ stateC;
//            final long c0 = stateB + stateA;
//            stateA = 0xD1B54A32D192ED03L * a0;
//            stateB = Long.rotateLeft(b0, shiftB);
//            stateC = Long.rotateLeft(c0, shiftC);

//            final long a0 = stateA;
//            final long b0 = stateB;
//            final long c0 = stateC;
//            stateA = 0xC6BC279692B5C323L + c0;
//            stateB = Long.rotateLeft(a0, shiftB) + c0;
//            stateC = Long.rotateLeft(b0, shiftC) ^ a0;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = fa + 0xC6BC279692B5C323L;
//            stateB = Long.rotateLeft(fd, shiftA) ^ fa;
//            stateC = Long.rotateLeft(fb, shiftB) + fd;
//            stateD = Long.rotateLeft(fc, shiftC) ^ fb;

//            Order 1, Best Ten with total:
//            #0: 33,24,18 with value 0.9357757568359375
//            #1: 34,58,36 with value 0.9372100830078125
//            #2: 35,46,58 with value 0.9389801025390625
//            #3: 56,31,3 with value 0.9401092529296875
//            #4: 34,26,63 with value 0.9407196044921875
//            #5: 57,32,3 with value 0.9438934326171875
//            #6: 56,10,23 with value 0.94439697265625
//            #7: 8,57,11 with value 0.944580078125
//            #8: 45,58,11 with value 0.9450836181640625
//            #9: 33,57,61 with value 0.9460906982421875
//            Order 2, Best Ten with total:
//            #0: 35,46,58 with value 31.112884521484375
//            #1: 45,58,11 with value 31.152130126953125
//            #2: 34,58,36 with value 31.155364990234375
//            #3: 56,10,23 with value 31.165542602539062
//            #4: 56,31,3 with value 31.182052612304688
//            #5: 39,56,20 with value 31.184097290039062
//            #6: 44,57,16 with value 31.186752319335938
//            #7: 57,10,32 with value 31.186767578125
//            #8: 58,19,63 with value 31.194747924804688
//            #9: 19,57,16 with value 31.197494506835938

//            // 35,46,58 is best? constant is 0x06A0F81D3D2E35EFL
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
//            stateC = ab ^ ab >>> shiftC;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fb ^ fa ^ fa >>> shiftC;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fb ^ fa + fd;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fb + fa + fd;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb + fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fc + fb ^ fa;
//            stateD = fd + constant;
//
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
//
////            stateA = Long.rotateLeft(fb + fc, shiftA);
////            stateB = Long.rotateLeft(fc + fd, shiftB);
////            stateC = fb ^ fa;
////            stateD = fd + constant;
//
////            stateA = Long.rotateLeft(fb ^ fc, shiftA);
////            stateB = Long.rotateLeft(fc ^ fd, shiftB);
////            stateC = fb + fa;
////            stateD = fd + constant;
//
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fb + fa ^ fc;
//            stateD = fd + constant;
//
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = Long.rotateLeft(fc ^ fd, shiftB);
//            stateC = fa * 0xD1342543DE82EF95L;
//            stateD = fd + constant;

//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            stateA = Long.rotateLeft(fb ^ fc, shiftA);
//            stateB = fc ^ fd;
//            stateC = fa * (0xD1342543DE82EF95L * shiftB | 1L);
//            stateD = fd + constant;

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

            // returning stateA:
//Order 1, Best Ten with total:
//#0: 35,10,23 with value 0.92730712890625
//#1: 58,14,33 with value 0.9279022216796875
//#2: 57,41,63 with value 0.9326171875
//#3: 56,14,26 with value 0.933013916015625
//#4: 59,15,58 with value 0.9342498779296875
//#5: 44,35,36 with value 0.9376068115234375
//#6: 17,38,55 with value 0.94024658203125
//#7: 20,8,24 with value 0.9407958984375
//#8: 35,21,26 with value 0.9410400390625
//#9: 27,42,48 with value 0.9411163330078125
//Order 2, Best Ten with total:
//#0: 43,36,41 with value 31.115707397460938
//#1: 59,48,33 with value 31.121994018554688
//#2: 25,9,44 with value 31.125213623046875
//#3: 19,6,11 with value 31.146881103515625
//#4: 56,11,61 with value 31.149017333984375
//#5: 18,58,55 with value 31.149673461914062
//#6: 15,19,48 with value 31.167098999023438
//#7: 25,18,1 with value 31.171463012695312
//#8: 24,53,63 with value 31.173187255859375
//#9: 59,15,58 with value 31.174072265625
            //returning stateC:
//#0: 6,23,21 with value 0.9275665283203125
//#1: 27,42,48 with value 0.9281158447265625
//#2: 23,59,61 with value 0.9303131103515625
//#3: 42,49,21 with value 0.9332275390625
//#4: 52,44,26 with value 0.9333648681640625
//#5: 20,5,33 with value 0.9366302490234375
//#6: 14,23,16 with value 0.9368896484375
//#7: 58,19,63 with value 0.9385223388671875
//#8: 35,40,21 with value 0.940277099609375
//#9: 41,50,45 with value 0.9408416748046875
//Order 2, Best Ten with total:
//#0: 49,5,54 with value 31.050888061523438
//#1: 7,33,61 with value 31.08087158203125
//#2: 11,18,63 with value 31.087722778320312
//#3: 24,29,16 with value 31.1005859375
//#4: 25,17,38 with value 31.102676391601562
//#5: 51,7,57 with value 31.111785888671875
//#6: 9,30,58 with value 31.1458740234375
//#7: 48,6,1 with value 31.1522216796875
//#8: 6,49,24 with value 31.153335571289062
//#9: 52,44,26 with value 31.15582275390625
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            final long bc = fb + fc;
//            final long cd = fc ^ fd;
//            stateA = (bc << shiftA | bc >>> -shiftA);
//            stateB = (cd << shiftB | cd >>> -shiftB);
//            stateC = fa ^ bc;
//            stateD = fd + constant;

            // +, +, ^, return c
//Order 1, Best Ten with total:
//#0: 58,33,1 with value 0.9268646240234375
//#1: 56,10,23 with value 0.9282684326171875
//#2: 10,56,6 with value 0.9307403564453125
//#3: 34,58,36 with value 0.93084716796875
//#4: 46,19,16 with value 0.93328857421875
//#5: 20,47,16 with value 0.933349609375
//#6: 57,11,26 with value 0.9339752197265625
//#7: 57,32,3 with value 0.9380035400390625
//#8: 18,45,16 with value 0.9398345947265625
//#9: 19,57,16 with value 0.940582275390625
            // -, +, ^, return c
//Order 1, Best Ten with total:
//#0: 58,33,1 with value 0.928070068359375
//#1: 57,11,26 with value 0.9337921142578125
//#2: 46,19,16 with value 0.9358367919921875
//#3: 56,10,23 with value 0.9406280517578125
//#4: 20,47,16 with value 0.9412384033203125
//#5: 19,57,16 with value 0.943511962890625
//#6: 19,46,3 with value 0.94378662109375
//#7: 34,58,36 with value 0.946319580078125
//#8: 10,56,6 with value 0.9465484619140625
//#9: 33,45,3 with value 0.9467010498046875
            // +, +, ^, return a
//Order 1, Best Ten with total:
//#0: 46,19,16 with value 0.926025390625
//#1: 56,32,16 with value 0.930206298828125
//#2: 10,56,6 with value 0.93359375
//#3: 26,36,13 with value 0.9371337890625
//#4: 18,45,16 with value 0.9378662109375
//#5: 39,30,22 with value 0.9383544921875
//#6: 19,47,1 with value 0.9385223388671875
//#7: 38,28,9 with value 0.938690185546875
//#8: 45,18,13 with value 0.9429779052734375
//#9: 38,30,45 with value 0.945465087890625
            // -, +, ^, return a
//Order 1, Best Ten with total:
//#0: 10,56,6 with value 0.92449951171875
//#1: 46,19,16 with value 0.935577392578125
//#2: 56,32,16 with value 0.9356231689453125
//#3: 26,36,13 with value 0.9380340576171875
//#4: 39,30,22 with value 0.9395751953125
//#5: 45,18,13 with value 0.9408721923828125
//#6: 57,33,14 with value 0.941314697265625
//#7: 25,35,16 with value 0.9418487548828125
//#8: 19,47,1 with value 0.942413330078125
//#9: 35,25,8 with value 0.9427490234375
            // b + c, d - c, a ^ b, return a
//Order 1, Best Ten with total:
//#0: 56,32,16 with value 0.9224853515625
//#1: 46,19,16 with value 0.9303741455078125
//#2: 10,56,6 with value 0.931884765625
//#3: 26,36,13 with value 0.9321136474609375
//#4: 39,30,22 with value 0.93603515625
//#5: 25,35,16 with value 0.9367218017578125
//#6: 19,47,1 with value 0.9394989013671875
//#7: 26,35,3 with value 0.9400634765625
//#8: 18,45,16 with value 0.9414215087890625
//#9: 35,25,8 with value 0.943023681640625
            // b + c, c + d, a ^ b + c, return a
//Order 1, Best Ten with total:
//#0: 57,18,22 with value 0.9314117431640625
//#1: 22,16,3 with value 0.933319091796875
//#2: 44,35,36 with value 0.9342193603515625
//#3: 35,41,52 with value 0.935638427734375
//#4: 55,20,61 with value 0.9368896484375
//#5: 36,47,3 with value 0.9379425048828125
//#6: 21,6,30 with value 0.9386444091796875
//#7: 58,16,36 with value 0.94012451171875
//#8: 18,57,26 with value 0.94012451171875
//#9: 47,53,21 with value 0.940826416015625
//            final long fa = stateA;
//            final long fb = stateB;
//            final long fc = stateC;
//            final long fd = stateD;
//            final long bc = fb + fc;
//            final long cd = fd + fc;
//            stateA = (bc << shiftA | bc >>> -shiftA);
//            stateB = (cd << shiftB | cd >>> -shiftB);
//            stateC = fa ^ bc;
//            stateD = fd + constant;
            //return a
//Order 1, Best Ten with total:
//#0: 58,16,36 with value 0.92620849609375
//#1: 11,45,11 with value 0.9319915771484375
//#2: 57,18,22 with value 0.93206787109375
//#3: 42,51,52 with value 0.9333953857421875
//#4: 53,45,11 with value 0.9354095458984375
//#5: 23,57,21 with value 0.9383697509765625
//#6: 28,23,21 with value 0.9388580322265625
//#7: 30,40,61 with value 0.938995361328125
//#8: 25,6,30 with value 0.9390411376953125
//#9: 35,11,16 with value 0.9402923583984375
            //return c
//Order 1, Best Ten with total:
//#0: 50,9,57 with value 0.9289093017578125
//#1: 43,6,36 with value 0.93212890625
//#2: 16,26,21 with value 0.9378204345703125
//#3: 58,47,61 with value 0.938201904296875
//#4: 55,20,61 with value 0.938751220703125
//#5: 17,52,3 with value 0.9394683837890625
//#6: 16,58,26 with value 0.9407501220703125
//#7: 29,45,3 with value 0.9414825439453125
//#8: 7,47,3 with value 0.943572998046875
//#9: 6,20,63 with value 0.94390869140625
            final long fa = stateA;
            final long fb = stateB;
            final long fc = stateC;
            final long fd = stateD;
            final long bc = fb ^ fc;
            final long cd = fc ^ fd;
            stateA = (bc << shiftA | bc >>> -shiftA);
            stateB = (cd << shiftB | cd >>> -shiftB);
            stateC = fa + bc;
            stateD = fd + constant;
        }
        return stateC;
    }

    public static void main(String[] args) {
        DistinctRandom rng = new DistinctRandom(123456789L);
        // Order 1
        if(true)
        {
            final long[][] A = new long[64][64];
            final IntObjectOrderedMap<Double> res = new IntObjectOrderedMap<>(4096),
                    totals = new IntObjectOrderedMap<>(4096);
            for (int iterations = lower; iterations < upper; iterations += inc) {
                int sa = 1, sb = 3, sc = 5;
                for (int p = 0; p < 0x4000; p++) {
                    sa = (sa >>> 1 ^ (-(sa & 1) & 0x30));
                    if(sa < 23) sb = (sb >>> 1 ^ (-(sb & 1) & 0x30));
                    if(sb < 11) sc = (sc >>> 1 ^ (-(sc & 1) & 0x30));
                        ArrayTools.fill(A, 0L);
                        for (long n = 0; n < N; n++) {
                            long v = rng.nextLong();
                            long w = mix(v, sa, sb, sc, iterations);
                            for (int i = 0; i < 64; i++) {
                                long x = w ^ mix(v ^ (1L << i), sa, sb, sc, iterations);
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
                        int id = (sa & 63) | (sb & 63) << 6 | (sc & 63) << 12;
                        res.put(id, result);
                        totals.put(id, result + totals.getOrDefault(id, 0.0));

                }
                System.out.println("Completed run for " + iterations + " iterations, order 1.");
                res.sortByValue(Double::compareTo);
                System.out.println("Order 1, Best Ten with " + iterations + " iterations:");
                for (int i = 0; i < 10; i++) {
                    final int k = res.keyAt(i);
                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6 & 63) + "," + (k >>> 12) + " with value " + res.getAt(i));
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
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6 & 63) + "," + (k >>> 12) + " with value " + res.getAt(i));
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
            final long[][] A = new long[2016][64];
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
                        long v = rng.nextLong();
                        long w = mix(v, sa, sb, sc, iterations);
                        for (int i = 0, p = 0; i < 64; i++) {
                            for (int h = i + 1; h < 64; h++) {
                                long x = w ^ mix(v ^ (1L << i) ^ (1L << h), sa, sb, sc, iterations);
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
                    int id = (sa & 63) | (sb & 63) << 6 | (sc & 63) << 12;
                    res.put(id, result);
                    totals.put(id, result + totals.getOrDefault(id, 0.0));
                }
                System.out.println("Completed run for " + iterations + " iterations, order 2.");
                res.sortByValue(Double::compareTo);
                System.out.println("Order 2, Best Ten with " + iterations + " iterations:");
                for (int i = 0; i < 10; i++) {
                    final int k = res.keyAt(i);
                    System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6 & 63) + "," + (k >>> 12) + " with value " + res.getAt(i));
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
                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6 & 63) + "," + (k >>> 12) + " with value " + res.getAt(i));
            }
//            System.out.println("Order 2, Worst Ten with total:");
//            for (int i = totals.size() - 10; i < totals.size(); i++) {
//                final int k = totals.keyAt(i);
//                System.out.println("#" + i + ": " + (k & 63) + "," + (k >>> 6) + " with value " + totals.getAt(i));
//            }
        }
    }
}