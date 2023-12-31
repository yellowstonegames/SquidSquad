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

package com.github.yellowstonegames.place;

import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.function.IntToIntFunction;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.grid.Coord;
import com.github.yellowstonegames.grid.CoordOrderedSet;
import com.github.yellowstonegames.grid.Direction;

/**
 * A maze generator that can be configured using an {@link IntToIntFunction}, which can be customized for the app.
 * Based in part on code from
 * <a href="http://weblog.jamisbuck.org/2011/1/27/maze-generation-growing-tree-algorithm">Jamis Buck's blog</a>.
 * This defaults to {@link #newest} for its IntToIntFunction, but {@link #random} is also good to try; you can specify a
 * IntToIntFunction with {@link #generate(IntToIntFunction)}.
 * <br>
 * Here, an IntToIntFunction used to choose cells takes a size (exclusive upper bound) and returns some int between 0
 * (inclusive) and that size (exclusive). These are typically lambdas.
 */
public class GrowingTreeMazeGenerator implements PlaceGenerator {

    private EnhancedRandom rng;
    private int width, height;
    public char[][] dungeon;
    public int[][] environment;

    public GrowingTreeMazeGenerator() {
        this(80, 80, new WhiskerRandom());
    }
    public GrowingTreeMazeGenerator(int width, int height) {
        this(width, height, new WhiskerRandom());
    }
    public GrowingTreeMazeGenerator(int width, int height, EnhancedRandom rng) {
        this.width = width;
        this.height = height;
        this.rng = rng;
    }

    /**
     * Gets the most recently-produced maze as a 2D char array, usually produced by calling {@link #generate()} or
     * {@link #generate(IntToIntFunction)}. This passes a direct reference and not a copy, so you can normally modify the
     * returned array to propagate changes back into this PlaceGenerator.
     *
     * @return the most recently-produced dungeon/map as a 2D char array
     */
    @Override
    public char[][] getPlaceGrid() {
        return dungeon;
    }

    @Override
    public int[][] getEnvironment() {
        return environment;
    }

    /**
     * Builds and returns a 2D char array maze by using {@link #newest} with {@link #generate(IntToIntFunction)}.
     * 
     * @return {@link #dungeon}, after filling it with a maze
     */
    @Override
    public char[][] generate() {
        return generate(newest);
    }

    /**
     * Builds and returns a 2D char array maze using the provided chooser method object. The most maze-like dungeons
     * use {@link #newest}, the least maze-like use {@link #oldest}, and the most jumbled use {@link #random} or a
     * mix of others using {@link #mix(IntToIntFunction, double, IntToIntFunction, double)}.
     * 
     * @param choosing the callback object for making the split decision
     * @return {@link #dungeon}, after filling it with a maze
     */
    public char[][] generate(IntToIntFunction choosing) {
        if(dungeon == null || dungeon.length != width || dungeon[0].length != height)
            dungeon = ArrayTools.fill('#', width, height);
        else 
            ArrayTools.fill(dungeon, '#');
        if(environment == null || environment.length != width || environment[0].length != height)
            environment = ArrayTools.fill(DungeonTools.CORRIDOR_WALL, width, height);
        else 
            ArrayTools.fill(environment, DungeonTools.CORRIDOR_WALL);
        
        int x = rng.nextInt(width - 1) | 1;
        int y = rng.nextInt(height - 1) | 1;
        dungeon[x][y] = '.';
        CoordOrderedSet deck = new CoordOrderedSet();
        deck.add(Coord.get(x, y));

        Direction[] dirs = new Direction[4];
        System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
        OUTER:
        while (!deck.isEmpty()) {
            int i = choosing.applyAsInt(deck.size());
            Coord p = deck.getAt(i);
            rng.shuffle(dirs);

            for (Direction dir : dirs) {
                x = p.x + dir.deltaX * 2;
                y = p.y + dir.deltaY * 2;
                if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                    if (dungeon[x][y] == '#' && deck.add(Coord.get(x, y))) {
                        dungeon[x][y] = '.';
                        environment[x][y] = DungeonTools.CORRIDOR_FLOOR;
                        dungeon[p.x + dir.deltaX][p.y + dir.deltaY] = '.';
                        environment[p.x + dir.deltaX][p.y + dir.deltaY] = DungeonTools.CORRIDOR_FLOOR;
                        continue OUTER;
                    }
                }
            }
            
            deck.remove(p);
        }

        return dungeon;
    }

    @Override
    public String toString() {
        return "GrowingTreeMazeGenerator{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }

    /**
     * Produces high-quality mazes that are very similar to those produced by a recursive back-tracking algorithm.
     * <br>
     * Example:
     * <pre>
     * ┌─────┬───────┬───┬─────────┬─────┬───┐
     * │.....│.......│...│.........│.....│...│
     * │.────┘.┌──.│.│.│.└───┐.┌──.│.──┐.│.│.│
     * │.......│...│.│.│.....│.│...│...│.│.│.│
     * │.──────┤.──┤.│.└─┬──.│.└─┐.└─┐.│.│.│.│
     * │.......│...│.│...│...│...│...│.│.│.│.│
     * ├─────┐.├──.│.└─┐.└─┐.├──.├─┐.│.│.│.│.│
     * │.....│.│...│...│...│.│...│.│...│...│.│
     * │.│.──┘.│.──┼───┼──.│.│.──┤.└───┼───┘.│
     * │.│.....│...│...│...│.│...│.....│.....│
     * │.└─┬───┴─┐.└─┐.│.┌─┘.│.│.└───┐.│.──┬─┤
     * │...│.....│...│...│...│.│.....│.│...│.│
     * ├──.│.┌──.│.──┴───┤.──┴─┤.──┐.│.└─┐.│.│
     * │...│.│...│.......│.....│...│.│...│.│.│
     * │.┌─┴─┘.│.├─┐.│.┌─┴───┐.├───┘.│.│.│.│.│
     * │.│.....│.│.│.│.│.....│.│.....│.│.│.│.│
     * │.│.┌───┘.│.│.└─┘.──┐.│.│.┌───┘.│.│.│.│
     * │...│.......│.......│.....│.....│.....│
     * └───┴───────┴───────┴─────┴─────┴─────┘
     * </pre>
     */
    public final IntToIntFunction newest = (size) -> size - 1;
    /**
     * Produces mostly straight corridors that dead-end at the map's edge; probably only useful with
     * {@link #mix(IntToIntFunction, double, IntToIntFunction, double)}.
     * <br>
     * Example:
     * <pre>
     * ┌─┬─┬─┬─┬─┬─┬─┬───────────────────────┐
     * │.│.│.│.│.│.│.│.......................│
     * │.│.│.│.│.│.│.│.──────────────────────┤
     * │.│.│.│.│.│.│.│.......................│
     * │.│.│.│.│.│.│.│.──────────────────────┤
     * │.│.│.│.│.│.│.│.......................│
     * │.│.│.│.│.│.│.│.──────────────────────┤
     * │.........│.│.│.......................│
     * ├────────.│.│.│.──────────────────────┤
     * │.....................................│
     * ├────────────.│.──────────────────────┤
     * │.............│.......................│
     * ├────────────.│.──────────────────────┤
     * │.............│.......................│
     * ├────────────.│.│.────────────────────┤
     * │.............│.│.....................│
     * ├────────.│.│.│.│.────────────────────┤
     * │.........│.│.│.│.....................│
     * └─────────┴─┴─┴─┴─────────────────────┘
     * </pre>
     */
    public final IntToIntFunction oldest = (size) -> 0;
    /**
     * Produces chaotic, jumbled spans of corridors, often with dead-ends, that are similar to those produced by Prim's
     * algorithm. This works well when mixed with {@link #newest} using
     * {@link #mix(IntToIntFunction, double, IntToIntFunction, double)}, and not as well when mixed with {@link #oldest}.
     * <br>
     * Example:
     * <pre>
     * ┌─────────────┬───────────┬───────────┐
     * │.............│...........│...........│
     * ├─┬─┬─┬────.┌─┘.┌─┐.──┐.──┤.──┐.──┐.│.│
     * │.│.│.│.....│...│.│...│...│...│...│.│.│
     * │.│.│.└─┬──.│.┌─┘.└─┐.└─┬─┘.──┴───┤.│.│
     * │.│.│...│...│.│.....│...│.........│.│.│
     * │.│.└─┐.└─┐.│.│.┌─┬─┤.┌─┤.──┐.────┴─┼─┤
     * │.....│...│.....│.│.│.│.│...│.......│.│
     * ├────.├──.└──.──┘.│.└─┘.│.──┴─┐.│.──┘.│
     * │.....│...........│...........│.│.....│
     * ├────.└──.│.│.│.──┘.──────────┴─┴─┐.──┤
     * │.........│.│.│...................│...│
     * ├──────.│.│.│.│.────────────────┐.└─┬─┤
     * │.......│.│.│.│.................│...│.│
     * ├────.│.│.│.│.├─┬─┐.│.│.│.──┐.──┴───┘.│
     * │.....│.│.│.│.│.│.│.│.│.│...│.........│
     * │.┌──.├─┴─┴─┘.│.│.└─┴─┴─┴─┐.└─┐.│.│.│.│
     * │.│...│...................│...│.│.│.│.│
     * └─┴───┴───────────────────┴───┴─┴─┴─┴─┘
     * </pre>
     */
    public final IntToIntFunction random = (size) -> rng.nextInt(size);

    /**
     * Mixes two IntToIntFunction values, like {@link #newest} and {@link #random}, given a weight for each, and produces
     * a new IntToIntFunction that randomly (respecting weight) picks one of those IntToIntFunctions each time it is used.
     * @param methodA the first IntToIntFunction to mix; must not be null
     * @param chanceA the weight to favor choosing methodA
     * @param methodB the second IntToIntFunction to mix; must not be null
     * @param chanceB the weight to favor choosing methodB
     * @return a IntToIntFunction that randomly picks between {@code methodA} and {@code methodB} each time it is used
     */
    public IntToIntFunction mix(final IntToIntFunction methodA, final double chanceA,
                              final IntToIntFunction methodB, final double chanceB) {
        final double a = Math.max(0.0, chanceA);
        final double sum = a + Math.max(0.0, chanceB);
        if(sum <= 0.0) return random;
        return (size) -> rng.nextDouble(sum) < a ? methodA.applyAsInt(size) : methodB.applyAsInt(size);
    }
}
