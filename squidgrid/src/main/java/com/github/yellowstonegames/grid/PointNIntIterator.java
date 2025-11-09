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
