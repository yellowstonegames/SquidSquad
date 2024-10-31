package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.ds.support.util.FloatIterator;

public interface PointNFloat<P extends PointNFloat<P>> extends PointN<P> {
    @Override
    default boolean floatingPoint() {
        return true;
    }
    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    float get (int index);

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    P setAt(int index, float value);

    class PointNFloatIterator implements FloatIterator {
        public PointNFloat<?> pt;
        public int index;
        public PointNFloatIterator(PointNFloat<?> pt){
            this.pt = pt;
            index = 0;
        }
        @Override
        public float nextFloat() {
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
