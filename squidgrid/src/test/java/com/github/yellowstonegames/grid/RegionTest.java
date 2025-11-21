package com.github.yellowstonegames.grid;

import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for at least some of the huge {@link Region} class.
 */
public class RegionTest {

    @Test
    public void testConstructors() {
        Region r, rf;
        r = new Region();
        Assert.assertEquals(4, r.width);
        Assert.assertEquals(64, r.height);
        Assert.assertEquals(0, r.size());
        rf = new Region().remake(r);
        Assert.assertEquals(4, rf.width);
        Assert.assertEquals(64, rf.height);
        Assert.assertEquals(0, rf.size());
        r.insert(2, 32);
        Assert.assertEquals(1, r.size());
        r.insertRectangle(2, 2, 2, 4);
        Assert.assertEquals(9, r.size());

        r = new Region(r);
        Assert.assertEquals(4, r.width);
        Assert.assertEquals(64, r.height);
        Assert.assertEquals(9, r.size());
        rf.empty().remake(r);
        Assert.assertEquals(4, rf.width);
        Assert.assertEquals(64, rf.height);
        Assert.assertEquals(9, rf.size());

        boolean[][] booleans = new boolean[6][6];
        booleans[5][5] = true;
        booleans[4][5] = true;
        r = new Region(booleans);
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.refill(booleans);
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());

        int[][] ints = new int[6][6];
        ints[5][5] = 1;
        ints[4][5] = 1;
        r = new Region(ints, 1);
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.empty().refill(ints, 1);
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());
        
        ints = new int[6][6];
        ints[5][5] = 1;
        ints[4][5] = 2;
        ints[0][0] = 3; // should be excluded by being >= upper bound
        r = new Region(ints, 1, 3);
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.empty().refill(ints, 1, 3);
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());

        char[][] chars = new char[6][6];
        chars[5][5] = '!';
        chars[4][5] = '!';
        r = new Region(chars, '!');
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.empty().refill(chars, '!');
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());

        chars = new char[6][6];
        chars[5][5] = '~';
        chars[4][5] = '.';
        char[] choices = new char[]{'.', '~', '!'};
        r = new Region(chars, choices);
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.empty().refill(chars, choices);
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());

        String[] lines = new String[]{
                "      ",
                "      ",
                "      ",
                "      ",
                "      ",
                "    !!",
        };
        r = new Region(lines, '!');
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());


        rf.empty().refill(lines, '!');
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());
        Assert.assertTrue(r.contains(4, 5));

        r = new Region((x, y) -> x >= 4 && y == 5, 6, 6);
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.empty().refill((x, y) -> x >= 4 && y == 5, 6, 6);
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());
        Assert.assertTrue(r.contains(4, 5));
        Assert.assertTrue(r.contains(5, 5));

        r = new Region(6, 6, Coord.get(4, 5), Coord.get(5, 5));
        Assert.assertEquals(6, r.width);
        Assert.assertEquals(6, r.height);
        Assert.assertEquals(2, r.size());

        rf.resizeAndEmpty(6, 6).insertSeveral(Coord.get(4, 5), Coord.get(5, 5));
        Assert.assertEquals(6, rf.width);
        Assert.assertEquals(6, rf.height);
        Assert.assertEquals(2, rf.size());

        r.insertRectangle(1, 1, 2, 4);
        Assert.assertEquals(10, r.size());
        Assert.assertTrue(r.contains(4, 5));
        Assert.assertTrue(r.contains(5, 5));
    }

    @Test
    public void testExpand() {
        String[] lines = new String[]{
                "      ",
                "   !! ",
                "  !!!!",
                "  !!!!",
                "  !!!!",
                "   !! ",
        };
        Region r = new Region(lines, '!');
        String[] targetLines = new String[]{
                "   !! ",
                "  !!!!",
                " !!!!!",
                " !!!!!",
                " !!!!!",
                "  !!!!",
        };
        Region t = new Region(targetLines, '!');
        r.expand();
        Assert.assertEquals(r, t);

        r.refill(lines, '!');
        Region old = new Region(t);
        r.expand(old);
        Assert.assertEquals(r, t);
        Assert.assertEquals(r.refill(lines, '!'), old);
    }

    @Test
    public void testFloodFills() {
        EnhancedRandom rng = new AceRandom(123456789);
        Region r = new Region(9, 9, Coord.get(5, 5));
        Region bounds = new Region(new String[]{
                "#########",
                "###...###",
                "##.....##",
                "#.......#",
                "#.......#",
                "#.......#",
                "##.....##",
                "###...###",
                "#########",
        }, '.');
        Assert.assertEquals(37, bounds.size());
        System.out.println("The bounds region looks like (should be an octagon): ");
        System.out.println(bounds);
        r.splash(bounds, rng);
        Assert.assertEquals(2, r.size());
        r.splash(bounds, rng);
        Assert.assertEquals(3, r.size());
        r.splash(bounds, rng);
        Assert.assertEquals(4, r.size());
        r.splash(bounds, rng);
        Assert.assertEquals(5, r.size());

        System.out.println("Should have exactly 5 items...");
        System.out.println(r);
        System.out.println("The bounds region looks like (should be an octagon): ");
        System.out.println(bounds);
        System.out.println("About to spill()...");
        r.spill(bounds, 34, rng);
        System.out.println("The spilled area should fill most of the bounds.");
        System.out.println(r);
        System.out.println("The spilled area actually has size: " + r.size());
        Assert.assertEquals(34, r.size());
        r.splash(bounds, rng);
        Assert.assertEquals(35, r.size());
        System.out.println("The splashed area should be a little larger now.");
        System.out.println(r);
    }
}
