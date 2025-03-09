package com.github.yellowstonegames.grid;

public class CoordTest {
    /**
     * All unique!
     */
    public void testSignedRosenbergStrongMultiplyUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int index = Coord.signedRosenbergStrongMultiplyHashCode(x, y);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    public static int signedRosenbergStrongHashCode(int x, int y) {
        // Calculates a hash that won't overlap until Coords reach 65536 or higher in x or y.

        // Masks x and y to the (non-negative) 16-bit range.
        // This is synonymous to casting x and y each to char.
        x &= 0xFFFF;
        y &= 0xFFFF;
        // Math.max can be branchless on modern JVMs, which may speed this method up a little if called often.
        final int max = Math.max(x, y);
        // Rosenberg-Strong pairing function; produces larger values in a square-shaped "ripple" moving away from the origin.
        return (max * max + max + x - y);
    }

    public void testSignedWeirdRosenbergStrongUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int index = signedRosenbergStrongHashCode(x, y);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    public void testRosenbergStrongUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int max = Math.max(x&0xFFFF, y&0xFFFF);
                int index = (max * max + max + (x&0xFFFF) - (y&0xFFFF));
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
    public void testSignedCantorUniqueness() {
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
     * Not all unique, but with a small enough limit... 23169 passes.
     */
    public void testLimitedSignedCantorUniqueness(int limit) {
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

    /**
     * Not all unique... at all...
     */
    public void testCantorUniqueness() {
        long[] bits = new long[1<<26];
        for (int x = -32768; x <= 32767; x++) {
            for (int y = -32768; y <= 32767; y++) {
                int index = Coord.cantorHashCode(x&0xFFFF, y&0xFFFF);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    /**
     * Fails for any cases where lowerLimit is negative and upperLimit is positive!!!
     * When using a mask, fails if the range requires more than 8 bits!
     * @param lowerLimit lower inclusive limit
     * @param upperLimit upper inclusive limit
     */
    public void testLimitedCantorUniqueness(int lowerLimit, int upperLimit) {
        long[] bits = new long[1<<26];
        for (int x = lowerLimit; x <= upperLimit; x++) {
            for (int y = lowerLimit; y <= upperLimit; y++) {
                int index = Coord.cantorHashCode(x&0xFFFF, y&0xFFFF);
                if((bits[index>>>6] & (1L << index)) != 0) {
                    throw new RuntimeException("Point at " + x + "," + y + " collided at index " + index);
                }
                bits[index>>>6] |= (1L << index);
            }
        }
    }

    public static void main(String[] args) {
//        new CoordTest().testSignedRosenbergStrongUniqueness(); // passes!
        new CoordTest().testSignedWeirdRosenbergStrongUniqueness(); // passes! but, requires 16-bit x and y.
//        new CoordTest().testRosenbergStrongUniqueness(); // passes!
//        new CoordTest().testSignedCantorUniqueness(); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(16384); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(17000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(18000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(19000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(20000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(21000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(22000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(23000); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(23125); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(23160); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(23169); // passes!
//        new CoordTest().testLimitedSignedCantorUniqueness(23170); // passes if exclusive on positive, fails if inclusive!
//        new CoordTest().testLimitedSignedCantorUniqueness(23171); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23173); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23175); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23180); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23200); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23250); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(23500); // fails!
//        new CoordTest().testLimitedSignedCantorUniqueness(24000); // fails!

//        new CoordTest().testCantorUniqueness(); // fails! also fails masked!
//        new CoordTest().testLimitedCantorUniqueness(0, 32767); // passes!
//        new CoordTest().testLimitedCantorUniqueness(0, 32768); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-1, 1); // fails! passes masked
//        new CoordTest().testLimitedCantorUniqueness(-128, 127); // passes masked!
//        new CoordTest().testLimitedCantorUniqueness(-128, 128); // fails! fails masked!
//        new CoordTest().testLimitedCantorUniqueness(-256, 255); // fails! fails masked!
//        new CoordTest().testLimitedCantorUniqueness(-4096, 4095); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-8192, 8191); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-16000, 16000); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-16384, 16383); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-20000, 20000); // fails!
//        new CoordTest().testLimitedCantorUniqueness(-23169, 23169); // fails!
    }
}
