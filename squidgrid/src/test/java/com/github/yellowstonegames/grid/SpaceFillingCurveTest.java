package com.github.yellowstonegames.grid;

import org.junit.Assert;
import org.junit.Test;

public class SpaceFillingCurveTest {

    /**
     * 2D Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param x non-negative horizontal input, between 0 and 46339, inclusive
     * @param y non-negative vertical input, between 0 and 46339, inclusive
     * @return distance on the Rosenberg-Strong curve, from the origin
     */
    public static int rs(final int x, final int y){
        final int m = Math.max(x, y);
        return m * m + m + y - x;
    }


    /**
     * Inverse of the 2D Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d the distance on the Rosenberg-Strong curve, from the origin, between 0 and 2147395599, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int rsInverse(Point2Int p, int d) {
        final int g = (int) Math.sqrt(d);
        final int r = d - g * g;
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
                int dist = rs(i, j);
                rsInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = rs(46339, 46339);
        rsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = rs(0, 46339);
        rsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = rs(46339, 0);
        rsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);

    }

    public static int rs(final int x, final int y, final int z){
        return rs(z, rs(x, y));
    }

    /**
     * 2D Boustrophedonic Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param x non-negative horizontal input, between 0 and 46339, inclusive
     * @param y non-negative vertical input, between 0 and 46339, inclusive
     * @return distance on the Boustrophedonic Rosenberg-Strong curve, from the origin
     */
    public static int brs(final int x, final int y){
        final int m = Math.max(x, y);
        final int s = -(m & 1);
        return m * m + m + (x - y + s ^ s);
    }

    /**
     * Inverse of the 2D Boustrophedonic Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d the distance on the Boustrophedonic Rosenberg-Strong curve, from the origin, between 0 and 2147395599, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int brsInverse(Point2Int p, int d){
        final int g = (int)Math.sqrt(d);
        final int r = d - g * g;
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
                int dist = brs(i, j);
                brsInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = brs(46339, 46339);
        brsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = brs(0, 46339);
        brsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = brs(46339, 0);
        brsInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);
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
     * 3D Pigeon-Ettinger-Rosenberg-Strong Triple function.
     * This gets a single integer that encodes three non-negative ints (between 0 and 1289, inclusive) into one distance
     * along a space-filling curve starting at (0,0,0). The curve is organized into cube-shaped "shells" or "gnomons,"
     * each shell being completely returned before an outer shell can be returned. That is, the distance to a point
     * inside a cube will always be less than the distance to a point that is outside that cube (if both have inputs
     * that are in range). Two different shells will connect at one of two edges, either y=0,z=0 for an even shell
     * connecting to a larger odd shell, or z=0,y=0 for an odd shell connecting to a larger even shell.
     * <br>
     * Notably, the distance will always be less than {@code Math.pow(Math.max(x, Math.max(y, z)) + 1, 3)} for inputs
     * that are in range.
     * <br>
     * This is based on a boustrophedonic Rosenberg-Strong pairing function (see
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">Steven Pigeon's blog</a> for more), but extended
     * to three dimensions. The blog post suggests using the pair of x with the pair of y and z as a triple function,
     * but that doesn't act like the 2D pairing function regarding shells.
     * <br>
     * "We could, of course, devise some complicated function for 3..." -- Steven Pigeon
     * <br>
     * "Yes! It works!" -- Tommy Ettinger
     *
     * @param x 3D coordinate, between 0 and 1289, inclusive
     * @param y 3D coordinate, between 0 and 1289, inclusive
     * @param z 3D coordinate, between 0 and 1289, inclusive
     * @return the distance along a space-filling curve from the origin to the given point.
     */
    public static int pers(final int x, final int y, final int z) {
        final int m = Math.max(x, y); // max of lateral dimensions
        final int g = Math.max(m, z); // gnomon, or shell
        final int u = g + 1;          // up one gnomon
        final int s = -(m + z & 1);   // sign, used to flip direction
        final int w = x - y + s ^ s;  // wind, used in all branches
        if ((g & 1) == 0) {
            // start at the top, wind over the top face, then snake down as z falls. Ends on x,y,z == g,0,0
            if (g == z)
                return (g * g * g) + m * m + m + w;
            return (g * g * u) + m + w + (g + g + 1) * (g - z);
        }
        // start at the bottom, snake up as z rises, then wind over the top face until we reach x,y,z == 0,0,g
        if (g == z) {
            return (u * u * u - 1) - m * m - m + w;
        }
        return (g * g * g) + m + w + (g + g + 1) * z;
    }

    /**
     * Inverse of the 3D Pigeon-Ettinger-Rosenberg-Strong Triple function.
     * This takes a single double {@code d} for distance and a {@link Point3Int} {@code p} that will be modified
     * in-place, and sets p to hold the coordinates of the point at distance {@code Math.floor(d)} along the
     * Pigeon-Ettinger-Rosenberg-Strong space-filling curve, starting at the origin (0,0,0).
     * The curve is organized into cube-shaped "shells" or "gnomons," each shell being completely returned before an
     * outer shell can be returned. That is, the distance to a point inside a cube will always be less than the distance
     * to a point that is outside that cube (if both have inputs that are in range). Two different shells will connect
     * at one of two edges, either y=0,z=0 for an even shell connecting to a larger odd shell, or z=0,y=0 for an odd
     * shell connecting to a larger even shell.
     * <br>
     * Notably, the distance will always be less than {@code Math.pow(Math.max(x, Math.max(y, z)) + 1, 3)} for inputs
     * that are between 0 and 2146688999, inclusive.
     * <br>
     * This is based on a boustrophedonic Rosenberg-Strong pairing function (see
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">Steven Pigeon's blog</a> for more), but extended
     * to three dimensions. The blog post suggests using the pair of x with the pair of y and z as a triple function,
     * but that doesn't act like the 2D pairing function regarding shells.
     * <br>
     * "We could, of course, devise some complicated function for 3..." -- Steven Pigeon
     * <br>
     * "Yes! It works!" -- Tommy Ettinger
     *
     * @param p a non-null Point3Int that will be modified in-place
     * @param d the distance along the space-filling curve from the origin, between 0 and 2146688999, inclusive
     * @return {@code p}, containing the point at distance {@code d} along the curve
     */
    public static Point3Int persInverse(Point3Int p, int d){
        final int g = (int)(Math.cbrt(d)); // which gnomon, or shell, we are in
        final int u = g + 1;               // up one gnomon
        final int b = g * g * g;           // base of the current gnomon
        if ((g & 1) == 0) {
            if(d - b < u * u)
            {
                // here we are on the top.
                final int t = (int)Math.sqrt(d - b); // top gnomon
                final int r = d - b - t * t;
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
                // winding down
                int bend = g + g + 1; // length of one bend; we will have g-many bends
                int remain = d - b - u * u; // remaining after the top has been traversed
                int ratio = remain / bend;
                int k = g - 1 - ratio;
                int i, j;
                int r = remain - ratio * bend;
                if(r <= g){
                    i = g;
                    j = r;
                } else {
                    i = g + g - r;
                    j = g;
                }
                // alternate clockwise and counterclockwise winds
                if((g + k & 1) == 1)
                    p.set(i, j, k);
                else
                    p.set(j, i, k);
            }
        } else {
            int lower = u * u * g;
            if(d >= lower){
                // finishing at the top, wind back to x,y == 0,0
                int top = u * u * u - 1 - d;
                int t = (int) Math.sqrt(top); // top gnomon
                int r = top - t * t;
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
                // winding up
                int bend = g + g + 1; // length of one bend; we will have g-many bends
                int remain = d - b; // remaining on the total winding section
                int k = remain / bend;
                int i, j;
                int r = remain - k * bend;
                if(r <= g){
                    i = g;
                    j = r;
                } else {
                    i = g + g - r;
                    j = g;
                }
                // alternate clockwise and counterclockwise winds
                if((g + k & 1) == 1)
                    p.set(i, j, k);
                else
                    p.set(j, i, k);
            }
        }
        return p;
    }

    @Test
    public void testPers() {
        Point3Int store = new Point3Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
//                    if((Math.max(i, Math.max(j, k)) & 1) == 1)
//                        continue;
                    int dist = pers(i, j, k);
                    persInverse(store, dist);
                    Assert.assertEquals("Failure at distance " + dist, i, store.x);
                    Assert.assertEquals("Failure at distance " + dist, j, store.y);
                    Assert.assertEquals("Failure at distance " + dist, k, store.z);
                }
            }
        }
        int limit = pers(1289, 1289, 1289);
        persInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 1289, store.x);
        Assert.assertEquals("Failure at distance " + limit, 1289, store.y);
        Assert.assertEquals("Failure at distance " + limit, 1289, store.z);

        limit = pers(0, 0, 1289);
        persInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);
        Assert.assertEquals("Failure at distance " + limit, 1289, store.z);

        limit = pers(1289, 0, 0);
        persInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 1289, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);
        Assert.assertEquals("Failure at distance " + limit, 0, store.z);

//        persInverse(store, Integer.MAX_VALUE);
//        System.out.println(store + " has distance " + limit);
    }

    public interface TripleFunction {
        int triple(int x, int y, int z);
    }

    public static void main(String[] args) {
        int SIZE = 6;
//        TripleFunction tri = SpaceFillingCurveTest::pers;
//        for (int z = 0; z < SIZE; z++) {
//            for (int y = 0; y < SIZE; y++) {
//                for (int x = 0; x < SIZE; x++) {
//                    System.out.printf("%03d ", tri.triple(x, y, z));
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }
//        System.out.println();
        int[][][] distances = new int[SIZE][SIZE][SIZE];
        Point3Int store = new Point3Int();
        for (int i = 0, n = SIZE * SIZE * SIZE; i < n; i++) {
            persInverse(store, i);
            distances[store.x][store.y][store.z] = i;
        }
        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    System.out.printf("%03d ", distances[x][y][z]);
                }
                System.out.println();
            }
            System.out.println();
        }
    }
}
