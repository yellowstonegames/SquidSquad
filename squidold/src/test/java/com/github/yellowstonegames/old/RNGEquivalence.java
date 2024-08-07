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

package com.github.yellowstonegames.old;

import com.github.yellowstonegames.old.v300.*;
import com.github.yellowstonegames.old.v300.squidmath.RNG;
import org.junit.Assert;
import org.junit.Test;

public class RNGEquivalence {
    @Test
    public void testDiverRNG() {
        DiverRNG enh = new DiverRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.DiverRNG old = new com.github.yellowstonegames.old.v300.squidmath.DiverRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testSilkRNG() {
        SilkRNG enh = new SilkRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.SilkRNG old = new com.github.yellowstonegames.old.v300.squidmath.SilkRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testGWTRNG() {
        GWTRNG enh = new GWTRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.GWTRNG old = new com.github.yellowstonegames.old.v300.squidmath.GWTRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testXoshiroStarPhi32RNG() {
        XoshiroStarPhi32RNG enh = new XoshiroStarPhi32RNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.XoshiroStarPhi32RNG old = new com.github.yellowstonegames.old.v300.squidmath.XoshiroStarPhi32RNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testLightRNG() {
        LightRNG enh = new LightRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.LightRNG old = new com.github.yellowstonegames.old.v300.squidmath.LightRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }

    @Test
    public void testLinnormRNG() {
        LinnormRNG enh = new LinnormRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.LinnormRNG old = new com.github.yellowstonegames.old.v300.squidmath.LinnormRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testThrustAltRNG() {
        ThrustAltRNG enh = new ThrustAltRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.ThrustAltRNG old = new com.github.yellowstonegames.old.v300.squidmath.ThrustAltRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }
    @Test
    public void testLongPeriodRNG() {
        LongPeriodRNG enh = new LongPeriodRNG(0L);
        com.github.yellowstonegames.old.v300.squidmath.LongPeriodRNG old = new com.github.yellowstonegames.old.v300.squidmath.LongPeriodRNG(0L);
        RNG rng = new RNG(old);
        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextLong(), enh.nextLong());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextLong(i), enh.nextLong(i));
        }

        for (int i = 0; i < 20; i++) {
            Assert.assertEquals(rng.nextInt(), enh.nextInt());
        }
        for (int i = 20; i < 200; i++) {
            Assert.assertEquals(rng.nextInt(i), enh.nextInt(i));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedLong(i), enh.nextSignedLong(i));
            long bound = DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedLong(bound), enh.nextSignedLong(bound));
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextSignedInt(i), enh.nextSignedInt(i));
            int bound = (int) DiverRNG.randomize(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextSignedInt(bound), enh.nextSignedInt(bound));
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextDouble(), enh.nextDouble(), Double.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextDouble(i), enh.nextDouble(i), Double.MIN_VALUE);
            double bound = DiverRNG.randomizeDouble(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextDouble(bound), enh.nextDouble(bound), Double.MIN_VALUE);
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextFloat(), enh.nextFloat(), Float.MIN_VALUE);
        }
        for (int i = -300; i < 300; i++) {
            Assert.assertEquals("Failed with bound " + i, rng.nextFloat(i), enh.nextFloat(i), Float.MIN_VALUE);
            float bound = DiverRNG.randomizeFloat(i);
            Assert.assertEquals("Failed with bound " + bound, rng.nextFloat(bound), enh.nextFloat(bound), Float.MIN_VALUE);
        }
        for (int bits = 1; bits <= 32; bits++) {
            for (int i = 0; i < 300; i++) {
                Assert.assertEquals("Failed with bits " + bits, rng.next(bits), enh.next(bits));
            }
        }
        for (int i = 0; i < 300; i++) {
            Assert.assertEquals(rng.nextBoolean(), enh.nextBoolean());
        }
    }

}
