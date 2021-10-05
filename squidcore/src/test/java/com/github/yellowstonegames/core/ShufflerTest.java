package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.support.FourWheelRandom;
import org.junit.Test;

import java.util.ArrayList;

public class ShufflerTest {
    @Test
    public void testGapShuffler() {
        for (int n = 8; n <= 48; n+= 8) {
            FourWheelRandom rng = new FourWheelRandom(Hasher.shax.hash64("SquidSquad!"));
            ArrayList<String> names = new ArrayList<>(n);
            Base base = Base.scrambledBase(rng);
            for (int i = 0; i < n; i++) {
                names.add(base.signed(rng.next(31)));
            }

            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            for (int i = 0; i < 200; i++) {
                System.out.println(gap.next());
            }

            // alternate API
            /*
            GapShuffler<String> gap = new GapShuffler<>(names, rng);
            Iterator<String> it = gap.iterator();
            for (int i = 0; i < 200; i++) {
                if(it.hasNext())
                    System.out.println(it.next());
            }*/

            System.out.println("\n");
        }
    }

    @Test
    public void testIntShufflerBounds()
    {
        for (int i = 3; i <= 80; i += 7) {
            IntShuffler lss = new IntShuffler(i, 31337);
            System.out.printf("Bound %02d: %d", i, lss.next());
            for (int j = 1; j < i; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testIntShufflerReseed()
    {
        IntShuffler lss = new IntShuffler(7, 0);
        for (int i = 0; i < 30; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
        }
    }
    @Test
    public void testIntShufflerReverse()
    {
        IntShuffler lss = new IntShuffler(7, 0);
        for (int i = 0; i < 10; i++) {
            lss.restart(i);
            System.out.printf("Seed %08X forward: %d", i, lss.next());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.next());
            }
            System.out.println();
            System.out.printf("Seed %08X reverse: %d", i, lss.previous());
            for (int j = 1; j < 7; j++) {
                System.out.print(", " + lss.previous());
            }
            System.out.println();

        }
    }
}
