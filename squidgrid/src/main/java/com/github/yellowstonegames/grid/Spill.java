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

    public Spill(Region passable, EnhancedRandom random) {
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

    public Region getSpillMap() {
        return spillMap;
    }

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
