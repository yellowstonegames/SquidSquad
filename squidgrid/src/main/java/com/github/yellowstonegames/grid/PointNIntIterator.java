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

import com.github.tommyettinger.ds.support.util.IntIterator;

/**
 * An {@link IntIterator} that iterates over the components in a PointNInt using {@link PointNInt#get(int)}.
 */
public class PointNIntIterator implements IntIterator {
    /**
     * The PointNInt this can iterate through.
     */
    public PointNInt<?, ?> pt;
    /**
     * Which index this will return next.
     */
    public int index;

    /**
     * Constructs a new PointNIntIterator over the given PointNInt that starts at index 0.
     * @param pt the PointNInt to iterate over
     */
    public PointNIntIterator(PointNInt<?, ?> pt) {
        this.pt = pt;
        index = 0;
    }

    /**
     * Returns the next int element in the iteration.
     * Unlike most IntIterator types, this typically won't throw an Exception if the iterator is exhausted.
     * You should still use {@link #hasNext()} to determine when the iterator has exhausted the available items.
     * @return the next int element in the iteration
     */
    @Override
    public int nextInt() {
        return pt.get(index++);
    }

    /**
     * Returns true if the iteration has more elements. (In other words, if {@link #nextInt()}) has been called
     * fewer times than {@link PointNInt#rank()}).
     * @return true if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return index < pt.rank();
    }

    /**
     * After {@link #nextInt()} has been called, you can call this to change the value at the same position last
     * returned by nextInt(). If nextInt() has not yet been called, this does not have defined behavior, and is
     * permitted to throw an Exception.
     * @param newValue the value to use at the last returned index
     */
    public void set(int newValue) {
        pt.setAt(index - 1, newValue);
    }

    public void reset() {
        index = 0;
    }
}
