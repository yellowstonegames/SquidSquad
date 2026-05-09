package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.yellowstonegames.place.DungeonTools;
import org.junit.Test;

public class GridIteratorTest {
    /**
     * GridIterator.SquareSpiral definitely seems broken... This outputs:
     * <pre>
     * .......
     * .......
     * .......
     * ...q...
     * .......
     * .......
     * .......
     * </pre>
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
}
