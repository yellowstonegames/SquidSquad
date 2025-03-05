package com.github.yellowstonegames.grid;

public class CoordTest {
//    @Test
    public void testRosenbergStrongUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int index = Coord.signedRosenbergStrongHashCode(x, y);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    public static void main(String[] args) {
        new CoordTest().testRosenbergStrongUniqueness();
    }
}
