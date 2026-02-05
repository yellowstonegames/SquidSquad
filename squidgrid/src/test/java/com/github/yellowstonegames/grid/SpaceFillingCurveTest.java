package com.github.yellowstonegames.grid;

import org.junit.Assert;
import org.junit.Test;

public class SpaceFillingCurveTest {

    /**
     * Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param x non-negative horizontal input
     * @param y non-negative vertical input
     * @return distance on the Rosenberg-Strong curve, from the origin
     */
    public static int rs(final int x, final int y){
        final int m = Math.max(x, y);
        return m * m + m + y - x;
    }


    /**
     * Inverse of the Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d the distance on the Rosenberg-Strong curve, from the origin (usually an integer)
     * @return {@code p}, after modifications
     */
    public static Point2Int rsInverse(Point2Int p, double d) {
        final int g = (int) Math.sqrt(d);
        final int r = (int) (d - g * g);
        int x, y;
        if (r <= g) {
            x = g;
            y = r;
        } else {
            x = g + g - r;
            y = g;
        }
        return p.set(x, y);
    }

    @Test
    public void testRs() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                double dist = rs(i, j);
                rsInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
    }

    public static int rs(final int x, final int y, final int z){
        return rs(z, rs(x, y));
    }

    /**
     * Boustrophedonic Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param x non-negative horizontal input
     * @param y non-negative vertical input
     * @return distance on the Boustrophedonic Rosenberg-Strong curve, from the origin
     */
    public static int brs(final int x, final int y){
        final int m = Math.max(x, y);
        final int s = -(m & 1);
        return m * m + m + (x - y + s ^ s);
    }

    /**
     * Inverse of the Boustrophedonic Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d the distance on the Boustrophedonic Rosenberg-Strong curve, from the origin (usually an integer)
     * @return {@code p}, after modifications
     */
    public static Point2Int brsInverse(Point2Int p, double d){
        final int g = (int)Math.sqrt(d);
        final int r = (int)(d - g * g);
        int i, j;
        if(r <= g){
            i = g;
            j = r;
        } else {
            i = g + g - r;
            j = g;
        }
        if((Math.max(i, j) & 1) == 1)
            p.set(i, j);
        else p.set(j, i);
        return p;
    }

    @Test
    public void testBrs() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                double dist = brs(i, j);
                brsInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
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
            // start at the top, wind over the top face, then snake down as z falls. Ends on x,y,z == g,0,0 .
            if (g == z)
                return (g * g * g) + m * m + m + (x - y + s ^ s);
            return (g * g * u) + (m + (x - y + s ^ s)) + (g + g + 1) * (g - z);
        }
        // start at the bottom, snake up as z rises, then wind over the top face until we reach x,y,z == 0,0,g .
        if (g == z) {
            return (u * u * u - 1) - (m * m + m + (y - x + s ^ s));
        }
        return (g * g * g) + (m + (x - y + s ^ s)) + (g + g + 1) * z;
    }

    public static Point3Int persInverse(Point3Int p, double d){
        final int g = (int)(Math.cbrt(d)); // which gnomon, or shell, we are in
        final int u = g + 1;               // up one gnomon
        final double b = (double) g * g * g;           // base of the current gnomon
        if ((g & 1) == 0) {
            if(d - b < (double) u * u)
            {
                // here we are on the top.
                final int t = (int)Math.sqrt(d - b); // top gnomon
                final int r = (int)(d - b - t * t);
                int i, j;
                if(r <= t){
                    i = t;
                    j = r;
                } else {
                    i = t + t - r;
                    j = t;
                }
                if((Math.max(i, j) & 1) == 1)
                    p.set(i, j, g);
                else p.set(j, i, g);
            } else {
                // winding down.
                int bend = g + g + 1;
                int remain = (int)(d - b - (double) u * u); // remaining
                int k = g - remain / bend;
                int i, j;
                int r = remain % bend;
                if(r <= g){
                    i = g;
                    j = r;
                } else {
                    i = g + g - r;
                    j = g;
                }
                if((g + k & 1) == 1)
                    p.set(i, j, k);
                else
                    p.set(j, i, k);
            }
        } else {
            // FIXME: placeholder
            p.set(0, 0, 0);
        }
        return p;
    }

    @Test
    public void testPers() {
        Point3Int store = new Point3Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    if((Math.max(i, Math.max(j, k)) & 1) == 1)
                        continue;
                    double dist = pers(i, j, k);
                    persInverse(store, dist);
//                    Assert.assertEquals("Failure at distance " + (int)dist, i, store.x);
//                    Assert.assertEquals("Failure at distance " + (int)dist, j, store.y);
//                    Assert.assertEquals("Failure at distance " + (int)dist, k, store.z);
                }
            }
        }

    }

    public interface TripleFunction {
        int triple(int x, int y, int z);
    }

    public static void main(String[] args) {
        int SIZE = 6;
        TripleFunction tri = SpaceFillingCurveTest::pers;
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
