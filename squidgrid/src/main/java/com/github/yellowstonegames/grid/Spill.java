/*
 * Copyright (c) 2023 See AUTHORS file.
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

import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Performs randomized flood-fill operations on arbitrarily-shaped areas, until a volume is reached.
 * <br>
 * This is a simple wrapper around {@link Region#spill(Region, int, EnhancedRandom, Region, Region)} to help handle its
 * several arguments more cleanly.
 */
public class Spill {
    private Region buffer, buffer2;
    protected Region spillMap, passable;
    protected EnhancedRandom random;

    /**
     * Calls {@link #Spill(Region)} by creating a Region from {@code passable} with
     * {@link Region#Region(char[][], char)} and a "yes" char of {@code '.'}.
     * Uses a randomly-seeded {@link WhiskerRandom}.
     * @param passable a rectangular 2D char array where {@code '.'} represents a walkable cell
     */
    public Spill(char[][] passable) {
        this(new Region(passable, '.'));
    }

    /**
     * Calls {@link #Spill(Region)} by creating a Region from {@code passable} with
     * {@link Region#Region(char[][], char)} and a "yes" char of {@code floorChar}.
     * Uses a randomly-seeded {@link WhiskerRandom}.
     * @param passable a rectangular 2D char array where {@code floorChar} represents a walkable cell
     * @param floorChar the char that is considered walkable in {@code passable}
     */
    public Spill(char[][] passable, char floorChar) {
        this(new Region(passable, floorChar));
    }

    /**
     * Calls {@link #Spill(Region)} by creating a Region from {@code passable} with
     * {@link Region#Region(char[][], char)} and a "yes" char of {@code '.'}.
     * Uses the given EnhancedRandom, or a randomly-seeded {@link WhiskerRandom} if it is null.
     * @param passable a rectangular 2D char array where {@code '.'} represents a walkable cell
     * @param random the random number generator to use; if null, this will use a random seed
     */
    public Spill(char[][] passable, @Nullable EnhancedRandom random) {
        this(new Region(passable, '.'), random);
    }

    /**
     * Calls {@link #Spill(Region)} by creating a Region from {@code passable} with
     * {@link Region#Region(char[][], char)} and a "yes" char of {@code floorChar}.
     * Uses the given EnhancedRandom, or a randomly-seeded {@link WhiskerRandom} if it is null.
     * @param passable a rectangular 2D char array where {@code floorChar} represents a walkable cell
     * @param floorChar the char that is considered walkable in {@code passable}
     * @param random the random number generator to use; if null, this will use a random seed
     */
    public Spill(char[][] passable, char floorChar,  @Nullable EnhancedRandom random) {
        this(new Region(passable, floorChar), random);
    }

    /**
     * Constructs a Spill from the given Region, where "on" cells represent passable areas that the spill can move
     * through and occupy.
     * Uses a randomly-seeded {@link WhiskerRandom}.
     * @param passable a Region where "on" cells can be moved through; will be referenced but not modified
     */
    public Spill(Region passable) {
        this(passable, null);
    }

    /**
     * Constructs a Spill from the given Region, where "on" cells represent passable areas that the spill can move
     * through and occupy.
     * Uses the given EnhancedRandom, or a randomly-seeded {@link WhiskerRandom} if it is null.
     * @param passable a Region where "on" cells can be moved through; will be referenced but not modified
     * @param random the random number generator to use; if null, this will use a random seed
     */
    public Spill(Region passable,  @Nullable EnhancedRandom random) {
        this.random = random == null ? new WhiskerRandom() : random;
        this.passable = passable == null ? new Region(32, 32) : passable;
        this.spillMap = new Region(this.passable.width, this.passable.height);
        this.buffer = new Region(this.passable.width, this.passable.height);
        this.buffer2 = new Region(this.passable.width, this.passable.height);
    }

    /**
     * Runs a spill outwards from one {@code entrance} point, until {@code volume} cells are present in the result, or
     * until no more cells can be added. You should usually call {@link Region#copy()} on the result, unless you intend
     * to discard it, because the result is a direct reference that will be reused in future calls to run().
     * @param entrance the single initial cell the spill will move out from
     * @param volume how many "on" cells the result can hold at most before returning
     * @return the {@link #getSpillMap() spillMap}, which this can reuse; call {@link Region#copy()} to avoid losing this
     */
    public Region run(Coord entrance, int volume) {
        spillMap.empty().add(entrance);
        return spillMap.spill(passable, volume, random, buffer, buffer2);
    }

    /**
     * Runs a spill outwards from one or more {@code entrances}, until {@code volume} cells are present in the result,
     * or until no more cells can be added. You should usually call {@link Region#copy()} on the result, unless you
     * intend to discard it, because the result is a direct reference that will be reused in future calls to run().
     * <br>
     * The "on" cells in {@code entrances} do not need to be contiguous.
     * @param entrances a Region holding typically one or more initial cells the spill will move out from
     * @param volume how many "on" cells the result can hold at most before returning
     * @return the {@link #getSpillMap() spillMap}, which this can reuse; call {@link Region#copy()} to avoid losing this
     */
    public Region run(Region entrances, int volume) {
        return spillMap.empty().insert(0, 0, entrances).spill(passable, volume, random, buffer, buffer2);
    }

    /**
     * Gets the last spillMap produced by {@link #run(Coord, int)}.
     * @return the last spillMap, a Region, produced by one of the run methods
     */
    public Region getSpillMap() {
        return spillMap;
    }

    /**
     * This probably won't be used often, but you can change the spillMap this uses here. This is returned by
     * {@link #run(Coord, int)}.
     * @param spillMap a non-null Region that should be the same size as {@link #getPassable()}
     */
    public void setSpillMap(Region spillMap) {
        if(spillMap != null)
            this.spillMap = spillMap;
    }

    public Region getPassable() {
        return passable;
    }

    public void setPassable(Region passable) {
        if(passable != null)
            this.passable = passable;
    }

    public EnhancedRandom getRandom() {
        return random;
    }

    public void setRandom(EnhancedRandom random) {
        if(random != null)
            this.random = random;
    }
}
