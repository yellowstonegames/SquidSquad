package com.github.yellowstonegames.grid;

public class CoordTest {
//    @Test

    /**
     * All unique!
     */
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

    /**
     * Not all unique...
     */
    public void testCantorUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int index = Coord.signedCantorHashCode(x, y);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    /**
     * Not all unique...
     */
    public void testLimitedCantorUniqueness(int limit) {
        long[] bits = new long[1<<26];
        for (int x = -limit; x <= limit; x++) {
            for (int y = -limit; y <= limit; y++) {
                int index = Coord.signedCantorHashCode(x, y);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    public static void main(String[] args) {
//        new CoordTest().testRosenbergStrongUniqueness(); // passes!
//        new CoordTest().testCantorUniqueness(); // fails!
//        new CoordTest().testLimitedCantorUniqueness(16384); // passes!
//        new CoordTest().testLimitedCantorUniqueness(17000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(18000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(19000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(20000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(21000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(22000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(23000); // passes!
//        new CoordTest().testLimitedCantorUniqueness(23125); // passes!
//        new CoordTest().testLimitedCantorUniqueness(23160); // passes!
        new CoordTest().testLimitedCantorUniqueness(23169); // passes!
//        new CoordTest().testLimitedCantorUniqueness(23170); // passes if exclusive on positive, fails if inclusive!
//        new CoordTest().testLimitedCantorUniqueness(23171); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23173); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23175); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23180); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23200); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23250); // fails!
//        new CoordTest().testLimitedCantorUniqueness(23500); // fails!
//        new CoordTest().testLimitedCantorUniqueness(24000); // fails!
    }
}
