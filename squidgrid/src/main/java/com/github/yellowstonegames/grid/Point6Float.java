package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point6;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;

import java.util.Random;

/**
 * A mutable 6D point with float components implementing {@link Point6}, {@link OfFloat},
 * and {@link PointNFloat}.
 */
public class Point6Float implements Point6<Point6Float>, PointNFloat<Point6Float, Point6<?>>, PrimitiveCollection.OfFloat {

    public float x;
    public float y;
    public float z;
    public float w;
    public float u;
    public float v;

    public Point6Float() {
    }

    public Point6Float(float x, float y, float z, float w, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        this.v = v;
    }

    public Point6Float(Point6Float p) {
        this(p.x, p.y, p.z, p.w, p.u, p.v);
    }

    public Point6Float(Point6<?> p) {
        this(p.x(), p.y(), p.z(), p.w(), p.u(), p.v());
    }

    /**
     * Returns true if this type of point uses {@code float} or {@code double} for its components, or false otherwise.
     * This always returns false.
     *
     * @return false
     */
    @Override
    public boolean floatingPoint() {
        return false;
    }

    @Override
    public Point6Float cpy() {
        return new Point6Float(this);
    }

    public Point6Float copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w + u * u + v * v;
    }

    @Override
    public Point6Float set(Point6Float point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        u = point.u;
        v = point.v;
        return this;
    }
    public Point6Float set(Point6<?> point) {
        x = point.x();
        y = point.y();
        z = point.z();
        w = point.w();
        u = point.u();
        v = point.v();
        return this;
    }

    @Override
    public Point6Float sub(Point6Float point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        u -= point.u;
        v -= point.v;
        return this;
    }
    public Point6Float sub(Point6<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        u -= point.u();
        v -= point.v();
        return this;
    }
    public Point6Float subtract(Point6<?> point) {
        return sub(point);
    }

    @Override
    public Point6Float add(Point6Float point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        u += point.u;
        v += point.v;
        return this;
    }
    public Point6Float add(Point6<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        u += point.u();
        v += point.v();
        return this;
    }

    @Override
    public Point6Float scl(Point6Float point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        u *= point.u;
        v *= point.v;
        return this;
    }
    public Point6Float scl(Point6<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        u *= point.u();
        v *= point.v();
        return this;
    }
    public Point6Float scale(Point6<?> point) {
        return scl(point);
    }
    public Point6Float mul(Point6Float point) {
        return scl(point);
    }
    public Point6Float mul(Point6<?> point) {
        return scl(point);
    }
    public Point6Float multiply(Point6<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point6Float point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w) +
                (point.u - u) * (point.u - u) +
                (point.v - v) * (point.v - v);
    }
    public float dst2(Point6<?> point) {
        return
                (point.x() - x) * (point.x() - x) +
                (point.y() - y) * (point.y() - y) +
                (point.z() - z) * (point.z() - z) +
                (point.w() - w) * (point.w() - w) +
                (point.u() - u) * (point.u() - u) +
                (point.v() - v) * (point.v() - v);
    }
    public float distance(Point6<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public float distanceSquared(Point6<?> point) {
        return dst2(point);
    }
    @Override
    public Point6Float setZero() {
        x = y = z = w = u = v = 0f;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point6Float x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point6Float y(float next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point6Float z(float next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point6Float w(float next) {
        w = next;
        return this;
    }

    @Override
    public float u() {
        return u;
    }

    @Override
    public Point6Float u(float next) {
        u = next;
        return this;
    }

    @Override
    public float v() {
        return v;
    }

    @Override
    public Point6Float v(float next) {
        v = next;
        return this;
    }

    @Override
    public Point6Float set(float x, float y, float z, float w, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        this.v = v;
        return this;
    }

    public Point6Float nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (float) Math.sqrt(l);
        x *= l;
        y *= l;
        z *= l;
        w *= l;
        u *= l;
        v *= l;
        return this;
    }

    public Point6Float normalize() {
        return nor();
    }

    public Point6Float add(float x, float y, float z, float w, float u, float v) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        this.v += v;
        return this;
    }

    public Point6Float sub(float x, float y, float z, float w, float u, float v) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        this.v -= v;
        return this;
    }
    public Point6Float subtract(float x, float y, float z, float w, float u, float v) {
        return sub(x, y, z, w, u, v);
    }

    public Point6Float plus(float scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        w += scalar;
        u += scalar;
        v += scalar;
        return this;
    }

    public Point6Float minus(float scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        w -= scalar;
        u -= scalar;
        v -= scalar;
        return this;
    }

    public Point6Float times(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        v *= scalar;
        return this;
    }

    public Point6Float scale(float scalar) {
        return times(scalar);
    }

    public Point6Float scl(float x, float y, float z, float w, float u, float v) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        this.v *= v;
        return this;
    }

    public Point6Float scale(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #times(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Float mul(float scalar) {
        return times(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #times(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Float multiply(float scalar) {
        return times(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Float mul(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Float multiply(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }

    public Point6Float addProduct(Point6<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point6Float mulAdd(Point6<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        u += vec.u() * scalar;
        v += vec.v() * scalar;
        return this;
    }

    public Point6Float mulAdd(Point6<?> vec, Point6<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        u += vec.u() * mulVec.u();
        v += vec.v() * mulVec.v();
        return this;
    }

    public Point6Float addProduct(Point6<?> vec, Point6<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point6Float limit(float limit) {
        return limit2(limit * limit);
    }

    public Point6Float limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return times((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point6Float limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point6Float clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return times((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return times((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point6Float setLength(float len) {
        return setLength2(len * len);
    }

    public Point6Float setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : times((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point6<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w() + u * other.u() + v * other.v();
    }

    public static float dot(Point6<?> a, Point6<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w() + a.u() * b.u() + a.v() * b.v();
    }

    public Point6Float lerp(Point6<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x() * alpha);
        this.y = (y * invAlpha) + (target.y() * alpha);
        this.z = (z * invAlpha) + (target.z() * alpha);
        this.w = (w * invAlpha) + (target.w() * alpha);
        this.u = (u * invAlpha) + (target.u() * alpha);
        this.v = (v * invAlpha) + (target.v() * alpha);
        return this;
    }

    public Point6Float interpolate(Point6<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    public Point6Float slerpGeometric(Point6Float target, float alpha) {
        return PointNFloat.slerpGeometric(this, target, alpha, this);
    }

    public Point6Float setToRandomDirection(Random random) {
        x = Distributor.probitI(random.nextInt());
        y = Distributor.probitI(random.nextInt());
        z = Distributor.probitI(random.nextInt());
        w = Distributor.probitI(random.nextInt());
        u = Distributor.probitI(random.nextInt());
        v = Distributor.probitI(random.nextInt());
        return nor();
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point6Float clampEach (float min, float max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        u = Math.min(Math.max(u, min), max);
        v = Math.min(Math.max(v, min), max);
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
    public Point6Float fract () {
        x -= MathTools.floor(x);
        y -= MathTools.floor(y);
        z -= MathTools.floor(z);
        w -= MathTools.floor(w);
        u -= MathTools.floor(u);
        v -= MathTools.floor(v);
        return this;
    }
    public Point6Float fractional () {
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
            case 4 : return u;
            case 5 : return v;
        }
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public Point6Float setAt(int index, float value){
        switch (index){
            default: x = value;
            case 1 : y = value;
            case 2 : z = value;
            case 3 : w = value;
            case 4 : u = value;
            case 5 : v = value;
        }
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Point6Float)) return false;

        Point6Float that = (Point6Float) o;
        return Float.compare(x, that.x) == 0 && Float.compare(y, that.y) == 0 && Float.compare(z, that.z) == 0 && Float.compare(w, that.w) == 0 && Float.compare(u, that.u) == 0 && Float.compare(v, that.v) == 0;
    }

    @Override
    public int hashCode() {
        final int h = BitConversion.floatToIntBits(x) + 53  * BitConversion.floatToIntBits(y) +
                113 * BitConversion.floatToIntBits(z) + 151 * BitConversion.floatToIntBits(w) +
                211 * BitConversion.floatToIntBits(u) + 253 * BitConversion.floatToIntBits(v);
        return h ^ h >>> 16;
    }

    /** Converts this {@code Point6Float} to a string in the format {@code (x,y,z,w,u,v)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.general(x) + ","
                   + Base.BASE10.general(y) + ","
                   + Base.BASE10.general(z) + ","
                   + Base.BASE10.general(w) + ","
                   + Base.BASE10.general(u) + ","
                   + Base.BASE10.general(v) + ")";
    }

    /** Sets this {@code Point6Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point6Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        int s3 = s.indexOf(',', s2 + 1);
        int s4 = s.indexOf(',', s3 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s3 != -1 && s4 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Base.BASE10.readFloat(s, 1, s0);
            float y = Base.BASE10.readFloat(s, s0 + 1, s1);
            float z = Base.BASE10.readFloat(s, s1 + 1, s2);
            float w = Base.BASE10.readFloat(s, s2 + 1, s3);
            float u = Base.BASE10.readFloat(s, s3 + 1, s4);
            float v = Base.BASE10.readFloat(s, s4 + 1, s.length() - 1);
            return this.set(x, y, z, w, u, v);
        }

        throw new IllegalArgumentException("Not a valid format for a Point6Float: " + s);
    }

    @Override
    public boolean add(float c) {
        throw new UnsupportedOperationException("Point6Float is fixed-size.");
    }

    @Override
    public boolean remove(float c) {
        throw new UnsupportedOperationException("Point6Float is fixed-size.");
    }

    @Override
    public boolean contains(float c) {
        return (x == c || y == c || z == c || w == c || u == c || v == c);
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point6Float is fixed-size.");
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
