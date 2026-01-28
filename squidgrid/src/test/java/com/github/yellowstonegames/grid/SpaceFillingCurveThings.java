package com.github.yellowstonegames.grid;

public class SpaceFillingCurveThings {

    /**
     * Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param x
     * @param y
     * @return
     */
    public static int rs(final int x, final int y){
        final int m = Math.max(x, y);
        return m * m + m + x - y;
    }

    public static int rs(final int x, final int y, final int z){
        return rs(z, rs(x, y));
    }

    /**
     * Boustrophedonic Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param x
     * @param y
     * @return
     */
    public static int brs(final int x, final int y){
        final int m = Math.max(x, y);
        final int s = -(m & 1);
        return m * m + m + (x - y + s ^ s);
    }

    /**
     * <pre>
     * 000 001 008 009
     * 003 002 007 010
     * 064 065 066 067
     * 099 098 097 096
     *
     * 015 014 013 012
     * 004 005 006 011
     * 063 062 061 060
     * 100 101 102 103
     *
     * 016 017 018 019
     * 035 034 033 032
     * 036 037 038 039
     * 143 142 141 140
     *
     * 255 254 253 252
     * 196 197 198 199
     * 195 194 193 192
     * 144 145 146 147
     * </pre>
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static int brs(final int x, final int y, final int z){
        return brs(x, brs(y, z));
    }

    /**
     * Pigeon-Ettinger-Rosenberg-Strong Triple function.
     * @param x 3D coordinate, must be non-negative
     * @param y 3D coordinate, must be non-negative
     * @param z 3D coordinate, must be non-negative
     * @return the distance along a space-filling curve from the origin to the given point.
     */
    public static int pers(final int x, final int y, final int z) {
        final int m = Math.max(x, y); // max of lateral dimensions
        final int g = Math.max(m, z); // gnomon, or shell
        final int u = g + 1;          // up one gnomon
        final int s = -(m + z & 1);   // sign, used to flip direction
        if ((g & 1) == 0) {
            if (g == z)
                return (g * g * g) + m * m + m + (x - y + s ^ s);
            return (g * g * u) + (m + (x - y + s ^ s)) + (g + g + 1) * (g - z);
        } else {
            if (g == z) {
                return (u * u * u - 1) - (m * m + m + (y - x + s ^ s));
            }
            return (g * g * g) + (m + (x - y + s ^ s)) + (g + g + 1) * z;
        }
    }

    public interface TripleFunction {
        int triple(int x, int y, int z);
    }

    public static void main(String[] args) {
        int SIZE = 6;
        TripleFunction tri = SpaceFillingCurveThings::pers;
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    System.out.printf("%03d ", tri.triple(x, y, z));
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
