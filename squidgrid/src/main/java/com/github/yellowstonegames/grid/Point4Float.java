package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point4;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * A mutable 4D point with float components implementing {@link Point4}, {@link OfFloat},
 * {@link PointNFloat}, and {@link Externalizable}.
 */
public class Point4Float implements Point4<Point4Float>, PointNFloat<Point4Float>, PrimitiveCollection.OfFloat, Externalizable {

    public float x;
    public float y;
    public float z;
    public float w;

    public Point4Float() {
    }

    public Point4Float(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Point4Float(Point4Float p) {
        this(p.x, p.y, p.z, p.w);
    }

    public Point4Float(Point4<?> p) {
        this(p.x(), p.y(), p.z(), p.w());
    }

    /**
     * Returns true if this type of point uses {@code float} or {@code double} for its components, or false otherwise.
     * This always returns true.
     *
     * @return true
     */
    @Override
    public boolean floatingPoint() {
        return true;
    }

    @Override
    public Point4Float cpy() {
        return new Point4Float(this);
    }

    public Point4Float copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w;
    }

    @Override
    public Point4Float set(Point4Float point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        return this;
    }
    public Point4Float set(Point4<?> point) {
        x = point.x();
        y = point.y();
        z = point.z();
        w = point.w();
        return this;
    }

    @Override
    public Point4Float sub(Point4Float point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        return this;
    }
    public Point4Float sub(Point4<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        return this;
    }
    public Point4Float subtract(Point4<?> point) {
        return sub(point);
    }

    @Override
    public Point4Float add(Point4Float point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        return this;
    }
    public Point4Float add(Point4<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        return this;
    }

    @Override
    public Point4Float scl(Point4Float point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        return this;
    }
    public Point4Float scl(Point4<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        return this;
    }
    public Point4Float scale(Point4<?> point) {
        return scl(point);
    }
    public Point4Float mul(Point4Float point) {
        return scl(point);
    }
    public Point4Float mul(Point4<?> point) {
        return scl(point);
    }
    public Point4Float multiply(Point4<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point4Float point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w);
    }
    public float dst2(Point4<?> point) {
        return
                (point.x() - x) * (point.x() - x) +
                (point.y() - y) * (point.y() - y) +
                (point.z() - z) * (point.z() - z) +
                (point.w() - w) * (point.w() - w);
    }
    public float distance(Point4<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public float distanceSquared(Point4<?> point) {
        return dst2(point);
    }
    @Override
    public Point4Float setZero() {
        x = y = z = w = 0f;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point4Float x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point4Float y(float next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point4Float z(float next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point4Float w(float next) {
        w = next;
        return this;
    }

    @Override
    public Point4Float set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Point4Float nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (float) Math.sqrt(l);
        x *= l;
        y *= l;
        z *= l;
        w *= l;
        return this;
    }

    public Point4Float normalize() {
        return nor();
    }

    public Point4Float add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Point4Float sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }
    public Point4Float subtract(float x, float y, float z, float w) {
        return sub(x, y, z, w);
    }
    public Point4Float scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        return this;
    }

    public Point4Float scale(float scalar) {
        return scl(scalar);
    }

    public Point4Float scl(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Point4Float scale(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Float mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Float multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Float mul(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Float multiply(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }

    public Point4Float addProduct(Point4<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point4Float mulAdd(Point4<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        return this;
    }

    public Point4Float mulAdd(Point4<?> vec, Point4<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        return this;
    }

    public Point4Float addProduct(Point4<?> vec, Point4<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point4Float limit(float limit) {
        return limit2(limit * limit);
    }

    public Point4Float limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point4Float limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point4Float clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point4Float setLength(float len) {
        return setLength2(len * len);
    }

    public Point4Float setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point4<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w();
    }

    public static float dot(Point4<?> a, Point4<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w();
    }

    public Point4Float lerp(Point4<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x() * alpha);
        this.y = (y * invAlpha) + (target.y() * alpha);
        this.z = (z * invAlpha) + (target.z() * alpha);
        this.w = (w * invAlpha) + (target.w() * alpha);
        return this;
    }

    public Point4Float interpolate(Point4<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    public Point4Float setToRandomDirection(Random random) {
        x = Distributor.linearNormalF(random.nextInt());
        y = Distributor.linearNormalF(random.nextInt());
        z = Distributor.linearNormalF(random.nextInt());
        w = Distributor.linearNormalF(random.nextInt());
        return nor();
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point4Float clampEach (float min, float max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        return this;
    }

    /**
     * Sets each component so it only has a fractional value, by subtracting the floor from each component.
     * This produces a non-negative float for each component, between 0.0 inclusive and 1.0 exclusive, unless a
     * component is outside the safe range for {@link MathTools#floor(float)} (-16384.0 at the lowest).
     * <pre>
     * The result of fract() for a component with a value of  1.25 will be 0.25 .
     * The result of fract() for a component with a value of -1.25 will be 0.75 .
     * </pre>
     *
     * @return this, after modifications
     */
    public Point4Float fract () {
        x -= MathTools.floor(x);
        y -= MathTools.floor(y);
        z -= MathTools.floor(z);
        w -= MathTools.floor(w);
        return this;
    }
    public Point4Float fractional () {
        return fract();
    }
    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public float get (int index) {
        switch (index) {
            default: return x;
            case 1 : return y;
            case 2 : return z;
            case 3 : return w;
        }
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public Point4Float setAt(int index, float value){
        switch (index){
            default: x = value;
            case 1 : y = value;
            case 2 : z = value;
            case 3 : w = value;
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int h = BitConversion.floatToIntBits(x) + 53  * BitConversion.floatToIntBits(y) +
                113 * BitConversion.floatToIntBits(z) + 151 * BitConversion.floatToIntBits(w);
        return h ^ h >>> 16;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
        out.writeFloat(w);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        w = in.readFloat();
    }

    /** Converts this {@code Point5Float} to a string in the format {@code (x,y,z,w,u)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.general(x) + ","
                   + Base.BASE10.general(y) + ","
                   + Base.BASE10.general(z) + ","
                   + Base.BASE10.general(w) + ")";
    }

    /** Sets this {@code Point5Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point4Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Base.BASE10.readFloat(s, 1, s0);
            float y = Base.BASE10.readFloat(s, s0 + 1, s1);
            float z = Base.BASE10.readFloat(s, s1 + 1, s2);
            float w = Base.BASE10.readFloat(s, s2 + 1, s.length() - 1);
            return this.set(x, y, z, w);
        }

        throw new IllegalArgumentException("Not a valid format for a Point5Float: " + s);
    }

    @Override
    public boolean add(float c) {
        throw new UnsupportedOperationException("Point5Float is fixed-size.");
    }

    @Override
    public boolean remove(float c) {
        throw new UnsupportedOperationException("Point5Float is fixed-size.");
    }

    @Override
    public boolean contains(float c) {
        return (x == c || y == c || z == c || w == c);
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point5Float is fixed-size.");
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean notEmpty() {
        return true;
    }

    @Override
    public PointNFloatIterator iterator() {
        return new PointNFloatIterator(this);
    }

}
