package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.PointN;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
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

    /**
     * A geometric "slerp" (spherical linear interpolation) from the input n-dimensional float point {@code start} to
     * the point {@code end} with the same type, moving a fraction of the distance equal to {@code alpha}, and placing
     * the result in {@code output} (modifying it in-place). Unlike most slerp() implementations, this works for any
     * PointNFloat type, including 2D points on a unit circle and 4D, 5D, etc. points on hyperspheres. This does not
     * allocate. This has undefined behavior if start and end are polar opposites; that is, points where for any
     * coordinate {@code a} in start, that coordinate in end is {@code -a} or any positive linear scale of the point
     * where that is true. This degenerates to a linear interpolation if either start or end is the origin, and simply
     * returns the start if both are the origin. Otherwise, this can smoothly move points that aren't already on the
     * unit sphere towards the distance of the other point from the origin.
     * <br>
     * Based on the non-approximation code from
     * <a href="https://observablehq.com/@mourner/approximating-geometric-slerp">an article by Volodymyr Agafonkin</a>.
     * Note that this is the "geometric slerp" rather than the version using quaternions in 3D (or rotors in other
     * dimensions). It has been augmented slightly to handle start and end vectors that don't have unit length. This is
     * very similar to {@link RotationTools#slerp(int, float[], int, float[], int, float, float[], int)}.
     *
     * @param start an n-dimensional float point to rotate from; will not be modified
     * @param end another n-dimensional float point to rotate to; will not be modified
     * @param alpha between 0 and 1, inclusive; how much to travel from start towards end
     * @param output will be modified in-place so this is set to the result
     * @return output, after modifications.
     */
    static <P extends PointNFloat<P>> P slerp(P start, P end,
                                float alpha, P output) {
        final int n = start.rank();
        float magS = 0f, magE = 0f;
        for (int i = 0; i < n; i++) {
            magS += start.get(i) * start.get(i);
            magE += end.get(i) * end.get(i);
        }
        // if both start and end are the origin
        if(MathTools.isZero(magS + magE)) {
            output.set(start);
        }
        // if only the start is the origin
        else if(MathTools.isZero(magS)){
            for (int i = 0; i < n; i++) {
                output.setAt(i, end.get(i) * alpha);
            }
        }
        // if only the end is the origin
        else if(MathTools.isZero(magE)){
            for (int i = 0; i < n; i++) {
                output.setAt(i, start.get(i) * (1f - alpha));
            }
        }
        else {
            magS = (float) Math.sqrt(magS);
            magE = (float) Math.sqrt(magE);

            float k = 0, invDistance = 1f / (magS * (1f - alpha) + magE * alpha);
            for (int i = 0; i < n; i++) {
                k += (start.get(i) / magS) * (end.get(i) / magE);
            }
            k = TrigTools.acos(k);
            float s = TrigTools.sinSmoother(k * (1f - alpha));
            float e = TrigTools.sinSmoother(k * alpha);

            for (int i = 0; i < n; i++) {
                output.setAt(i, (start.get(i) * s + end.get(i) * e) * invDistance);
            }
        }
        return output;
    }

}
