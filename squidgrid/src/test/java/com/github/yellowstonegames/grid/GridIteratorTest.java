package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.place.DungeonTools;
import org.junit.Test;

public class GridIteratorTest {
    /**
     * GridIterator.SquareSpiral definitely was broken... This originally output:
     * <pre>
     * .......
     * .......
     * .......
     * ...q...
     * .......
     * .......
     * .......
     * </pre>
     * Now it produces:
     * <pre>
     * efghijk
     * dQRSTUl
     * cPEFGVm
     * bODAHWn
     * aNCBIXo
     * `MLKJYp
     * _^]\[Zq
     * </pre>
     * Where it starts at the center, with A, and moves down, clockwise until it fully rotates, then down and clockwise
     * again until it finishes on q.
     */
    @Test
    public void testSquareSpiral() {
        GridIterator.SquareSpiral it = new GridIterator.SquareSpiral(7, 7, 3, 3);
        int index = 0;
        char[][] chars = ArrayTools.fill('.', 7, 7);
        while (it.hasNext() && index < 49){
            Coord c = it.next();
            chars[c.x][c.y] = (char) ('A' + index++);
        }
        DungeonTools.debugPrint(chars);
    }

    /**
     * This one works! It can limit the square size to a "radius" correctly already.
     * <pre>
     * .......
     * .UVWXY.
     * .PQRST.
     * .KLMNO.
     * .FGHIJ.
     * .ABCDE.
     * .......
     * </pre>
     */
    @Test
    public void testCenteredSquare() {
        GridIterator.CenteredSquare it = new GridIterator.CenteredSquare(7, 7, 3, 3, 2);
        int index = 0;
        char[][] chars = ArrayTools.fill('.', 7, 7);
        while (it.hasNext()){
            Coord c = it.next();
            chars[c.x][c.y] = (char) ('A' + index++);
        }
        DungeonTools.debugPrint(chars);
    }

}
