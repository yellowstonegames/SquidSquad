package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.support.util.IntIterator;

public interface PointNInt<P extends PointNInt<P>> extends PointN<P> {
    @Override
    default boolean floatingPoint() {
        return false;
    }
    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    int get (int index);

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    P setAt(int index, int value);

    class PointNIntIterator implements IntIterator {
        public PointNInt<?> pt;
        public int index;
        public PointNIntIterator(PointNInt<?> pt){
            this.pt = pt;
            index = 0;
        }
        @Override
        public int nextInt() {
            return pt.get(index++);
        }

        @Override
        public boolean hasNext() {
            return index < pt.rank();
        }

        public void reset(){
            index = 0;
        }
    }
}
