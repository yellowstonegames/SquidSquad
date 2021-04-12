package com.github.yellowstonegames.old;

import com.github.yellowstonegames.old.v300.DiverRNG;
import com.github.yellowstonegames.old.v300.squidmath.RNG;
import org.junit.Assert;
import org.junit.Ignore;
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
    }

}
