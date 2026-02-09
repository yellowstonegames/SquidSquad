package com.github.yellowstonegames.grid;

import org.junit.Assert;
import org.junit.Test;

import static com.github.yellowstonegames.grid.NumberPairing.*;

public class SpaceFillingCurveTest {
    @Test
    public void testCantor() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = cantor(i, j);
                cantorInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = cantor(32766, 32766);
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 32766, store.x);
        Assert.assertEquals("Failure at distance " + limit, 32766, store.y);

        limit = cantor(0, 46340);
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46340, store.y);

        limit = cantor(46340, 0);
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46340, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);

        int highest = 65533;
        limit = cantor(0, highest);
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, highest, store.y);

        limit = cantor(highest, 0);
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, highest, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);

        limit = 2147395599;
        cantorInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
    }

    @Test
    public void testRs() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = rosenbergStrong(i, j);
                rosenbergStrongInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = rosenbergStrong(46339, 46339);
        rosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = rosenbergStrong(0, 46339);
        rosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = rosenbergStrong(46339, 0);
        rosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);
    }

    /**
     * Attempt at a 3D Rosenberg-Strong triple function; doesn't work as hoped.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static int rs(final int x, final int y, final int z){
        return rosenbergStrong(z, rosenbergStrong(x, y));
    }

    @Test
    public void testBrs() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = windingRosenbergStrong(i, j);
                windingRosenbergStrongInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = windingRosenbergStrong(46339, 46339);
        windingRosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = windingRosenbergStrong(0, 46339);
        windingRosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = windingRosenbergStrong(46339, 0);
        windingRosenbergStrongInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);
    }

    /**
     * Attempt at a 3D Boustrophedonic Rosenberg-Strong triple function; doesn't work as hoped.
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
        return windingRosenbergStrong(x, windingRosenbergStrong(y, z));
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

    @Test
    public void testSe() {
        Point2Int store = new Point2Int();
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                int dist = szudzik(i, j);
                szudzikInverse(store, dist);
                Assert.assertEquals(i, store.x);
                Assert.assertEquals(j, store.y);
            }
        }
        int limit;

        limit = szudzik(46339, 46339);
        szudzikInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = szudzik(0, 46339);
        szudzikInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 0, store.x);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.y);

        limit = szudzik(46339, 0);
        szudzikInverse(store, limit);
//        System.out.println(store + " has distance " + limit);
        Assert.assertEquals("Failure at distance " + limit, 46339, store.x);
        Assert.assertEquals("Failure at distance " + limit, 0, store.y);

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
