/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

/**
 * Various simple cellular-automata rules and the data they operate on. This has Conway's Game of Life, when you can run
 * on a {@link Region} given to the constructor previously with {@link #runGameOfLife()}; this version of Life doesn't
 * wrap at the edges. It has a smoothing method that rounds off hard edges, {@link #runBasicSmoothing()}. It also has
 * two methods that may be useful for ensuring maps are always possible to pass through by a creature moving
 * orthogonally-only (Manhattan metric); these are {@link #runDiagonalGapCleanup()} and {@link #runDiagonalGapWiden()}.
 */
public class CellularAutomaton {
    /**
     * Returned directly by some methods, but you may want to change this at some other point.
     */
    public Region current;
    private final Region[] neighbors = new Region[9];
    private int[][] sums;

    /**
     * Constructs a CellularAutomaton with an unfilled 64x64 Region, that can be altered later via {@link #current}.
     */
    public CellularAutomaton()
    {
        this(new Region(64, 64));
    }

    /**
     * Constructs a CellularAutomaton with an unfilled Region of the specified width and height, that can be
     * altered later via {@link #current}.
     * @param width the width of the CellularAutomaton
     * @param height the height of the CellularAutomaton
     */
    public CellularAutomaton(int width, int height)
    {
        this(new Region(Math.max(1, width), Math.max(1, height)));
    }

    /**
     * Stores a direct reference to {@code current} as this object's {@link #current} field, and initializes the other
     * necessary fields.
     * @param current a Region that will be used directly; changes will be shared
     */
    public CellularAutomaton(Region current) {
        this.current = current;
        for (int i = 0; i < 9; i++) {
            neighbors[i] = current.copy();
        }
        sums = new int[current.width][current.height];
    }

    /**
     * Re-initializes this CellularAutomaton using a different Region as a basis. If the previous Region
     * used has the same dimensions as {@code next}, then this performs no allocations and simply sets the existing
     * contents. Otherwise, it makes one new 2D array and also has all 9 of the internal Regions adjust in size,
     * which involves some allocation. If {@code next} is null, this does nothing and returns itself without changes.
     * @param next a Region to set this CellularAutomaton to read from and adjust
     * @return this, for chaining
     */
    public CellularAutomaton remake(Region next)
    {
        if(next == null)
            return this;
        if(current.width != next.width || current.height != next.height)
            sums = new int[next.width][next.height];
        else
            ArrayTools.fill(sums, 0);
        current = next;
        for (int i = 0; i < 9; i++) {
            neighbors[i].remake(current);
        }
        return this;
    }

    /**
     * Reduces the sharpness of corners by only considering a cell on if the previous version has 5 of the 9 cells in
     * the containing 3x3 area as "on." Typically, this method is run repeatedly. It does not return itself for
     * chaining, because it returns a direct reference to the {@link #current} Region that this will use for
     * any future calls to this, and changes to current will be used here.
     * @return a direct reference to the changed Region this considers its main state, {@link #current}
     */
    public Region runBasicSmoothing()
    {
        neighbors[0].remake(current).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
        neighbors[8].remake(current);
        ArrayTools.fill(sums, 0);
        Region.sumInto(sums, neighbors);
        return current.refill(sums, 5, 10);
    }

    /**
     * Runs one step of the simulation called Conway's Game of Life, which has relatively simple rules:
     * <ul>
     *     <li>Any "on" cell with fewer than two "on" neighbors becomes "off."</li>
     *     <li>Any "on" cell with two or three "on" neighbors (no more than three) stays "on."</li>
     *     <li>Any "on" cell with more than three "on" neighbors becomes "off."</li>
     *     <li>Any "off" cell with exactly three "on" neighbors becomes "on."</li>
     * </ul>
     * These rules can bring about complex multi-step patterns in many cases, eventually stabilizing to predictable
     * patterns in most cases. Filling the whole state of this CellularAutomaton won't produce interesting patterns
     * most of the time, even if the fill is randomized; you might have better results by using known patterns. Some
     * key well-known patterns are covered on <a href="https://en.wikipedia.org/wiki/Conway's_Game_of_Life">Wikipedia's
     * detailed article on Conway's Game of Life</a>.
     * @return a direct reference to the changed Region this considers its main state, {@link #current}
     */
    public Region runGameOfLife()
    {
        neighbors[0].remake(current).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
        neighbors[8].remake(current);
        ArrayTools.fill(sums, 0);
        Region.sumInto(sums, neighbors);
        return current.refill(sums,3).or(neighbors[0].refill(sums, 4).and(neighbors[8]));
    }

    /**
     * This takes the {@link #current} Region and removes any cells that have a diagonal neighbor if that
     * neighbor cannot be accessed from shared orthogonal neighbors. That is, if a 2x2 area contains two "off" cells
     * that are diagonally adjacent and contains two "on" cells that are diagonally adjacent, this sets that whole 2x2
     * area to "off."
     * @return {@link #current} after orthogonally-inaccessible pairs of diagonal "on" cells are removed
     */
    public Region runDiagonalGapCleanup()
    {
        neighbors[0].remake(current.not()).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current.not()).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
//        neighbors[8].remake(current);
        current.andNot(neighbors[4].and(neighbors[0]).and(neighbors[2]));
        current.andNot(neighbors[5].and(neighbors[0]).and(neighbors[3]));
        current.andNot(neighbors[6].and(neighbors[1]).and(neighbors[2]));
        current.andNot(neighbors[7].and(neighbors[1]).and(neighbors[3]));
        return current;
    }

    /**
     * This takes the {@link #current} Region, then takes any "on" cells that have an "on" diagonal neighbor and that
     * neighbor cannot be accessed from shared orthogonal neighbors, and sets the shared orthogonal neighbors to "on."
     * That is, if a 2x2 area contains two "off" cells that are diagonally adjacent and contains two "on" cells that are
     * diagonally adjacent, this sets that whole 2x2 area to "on."
     * @return {@link #current} after orthogonally-inaccessible pairs of diagonal "on" cells are widened
     */
    public Region runDiagonalGapWiden()
    {
        neighbors[0].remake(current).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
//        neighbors[8].remake(current);
        current.or(neighbors[4].notAnd(neighbors[0]).and(neighbors[2]));
        current.or(neighbors[5].notAnd(neighbors[0]).and(neighbors[3]));
        current.or(neighbors[6].notAnd(neighbors[1]).and(neighbors[2]));
        current.or(neighbors[7].notAnd(neighbors[1]).and(neighbors[3]));
        return current;
    }
}
