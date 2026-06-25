/*
 * Copyright (c) 2026; see AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        {
            System.out.println("CLOCKWISE:");
            GridIterator.SquareSpiral it = new GridIterator.SquareSpiral(7, 7, 3, 3);
            int index = 0;
            char[][] chars = ArrayTools.fill('.', 7, 7);
            while (it.hasNext() && index < 49){
                Coord c = it.next();
                chars[c.x][c.y] = (char) ('A' + index++);
            }
            DungeonTools.debugPrint(chars);
        }
        {
            System.out.println("COUNTER-CLOCKWISE:");
            GridIterator.SquareSpiral it = new GridIterator.SquareSpiral(7, 7, 3, 3, false);
            int index = 0;
            char[][] chars = ArrayTools.fill('.', 7, 7);
            while (it.hasNext() && index < 49){
                Coord c = it.next();
                chars[c.x][c.y] = (char) ('A' + index++);
            }
            DungeonTools.debugPrint(chars);
        }
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

    /**
     * Starting off, this is badly broken, and throws an ArrayIndexOutOfBoundsException...
     * It's fixed now, but outputs this...:
     * <pre>
     * ...C...
     * ...B...
     * ...A...
     * .......
     * ...F...
     * ...E...
     * ...D...
     * </pre>
     * I think it's correct by the docs, I just don't know what would use this.
     */
    @Test
    public void testVerticalUp() {
        GridIterator.VerticalUp it = new GridIterator.VerticalUp(3, 3, 7, 7);
        int index = 0;
        char[][] chars = ArrayTools.fill('.', 7, 7);
        while (it.hasNext()){
            Coord c = it.next();
            chars[c.x][c.y] = (char) ('A' + index++);
        }
        DungeonTools.debugPrint(chars);
    }

}
