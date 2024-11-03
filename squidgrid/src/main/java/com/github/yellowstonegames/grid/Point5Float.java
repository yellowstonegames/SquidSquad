package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point5;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * A mutable 5D point with float components implementing {@link Point5}, {@link OfFloat},
 * {@link PointNFloat}, and {@link Externalizable}.
 */
public class Point5Float implements Point5<Point5Float>, PointNFloat<Point5Float>, PrimitiveCollection.OfFloat, Externalizable {

    public float x;
    public float y;
    public float z;
    public float w;
    public float u;

    public Point5Float() {
    }

    public Point5Float(float x, float y, float z, float w, float u) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
    }

    public Point5Float(Point5Float p) {
        this(p.x, p.y, p.z, p.w, p.u);
    }

    public Point5Float(Point5<?> p) {
        this(p.x(), p.y(), p.z(), p.w(), p.u());
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
    public Point5Float cpy() {
        return new Point5Float(this);
    }

    public Point5Float copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w + u * u;
    }

    @Override
    public Point5Float set(Point5Float point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        u = point.u;
        return this;
    }
    public Point5Float set(Point5<?> point) {
        x = point.x();
        y = point.y();
        z = point.z();
        w = point.w();
        u = point.u();
        return this;
    }

    @Override
    public Point5Float sub(Point5Float point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        u -= point.u;
        return this;
    }
    public Point5Float sub(Point5<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        u -= point.u();
        return this;
    }
    public Point5Float subtract(Point5<?> point) {
        return sub(point);
    }

    @Override
    public Point5Float add(Point5Float point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        u += point.u;
        return this;
    }
    public Point5Float add(Point5<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        u += point.u();
        return this;
    }

    @Override
    public Point5Float scl(Point5Float point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        u *= point.u;
        return this;
    }
    public Point5Float scl(Point5<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        u *= point.u();
        return this;
    }
    public Point5Float scale(Point5<?> point) {
        return scl(point);
    }
    public Point5Float mul(Point5Float point) {
        return scl(point);
    }
    public Point5Float mul(Point5<?> point) {
        return scl(point);
    }
    public Point5Float multiply(Point5<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point5Float point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w) +
                (point.u - u) * (point.u - u);
    }
    public float dst2(Point5<?> point) {
        return
                (point.x() - x) * (point.x() - x) +
                (point.y() - y) * (point.y() - y) +
                (point.z() - z) * (point.z() - z) +
                (point.w() - w) * (point.w() - w) +
                (point.u() - u) * (point.u() - u);
    }
    public float distance(Point5<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public float distanceSquared(Point5<?> point) {
        return dst2(point);
    }
    @Override
    public Point5Float setZero() {
        x = y = z = w = u = 0f;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point5Float x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point5Float y(float next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point5Float z(float next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point5Float w(float next) {
        w = next;
        return this;
    }

    @Override
    public float u() {
        return u;
    }

    @Override
    public Point5Float u(float next) {
        u = next;
        return this;
    }

    @Override
    public Point5Float set(float x, float y, float z, float w, float u) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        return this;
    }

    public Point5Float nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (float) Math.sqrt(l);
        x *= l;
        y *= l;
        z *= l;
        w *= l;
        u *= l;
        return this;
    }

    public Point5Float normalize() {
        return nor();
    }

    public Point5Float add(float x, float y, float z, float w, float u) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        return this;
    }

    public Point5Float sub(float x, float y, float z, float w, float u) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        return this;
    }
    public Point5Float subtract(float x, float y, float z, float w, float u) {
        return sub(x, y, z, w, u);
    }
    public Point5Float scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        return this;
    }

    public Point5Float scale(float scalar) {
        return scl(scalar);
    }

    public Point5Float scl(float x, float y, float z, float w, float u) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        return this;
    }

    public Point5Float scale(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Float mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Float multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Float mul(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Float multiply(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }

    public Point5Float addProduct(Point5<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point5Float mulAdd(Point5<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        u += vec.u() * scalar;
        return this;
    }

    public Point5Float mulAdd(Point5<?> vec, Point5<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        u += vec.u() * mulVec.u();
        return this;
    }

    public Point5Float addProduct(Point5<?> vec, Point5<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point5Float limit(float limit) {
        return limit2(limit * limit);
    }

    public Point5Float limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point5Float limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point5Float clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point5Float setLength(float len) {
        return setLength2(len * len);
    }

    public Point5Float setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point5<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w() + u * other.u();
    }

    public static float dot(Point5<?> a, Point5<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w() + a.u() * b.u();
    }

    public Point5Float lerp(Point5<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x() * alpha);
        this.y = (y * invAlpha) + (target.y() * alpha);
        this.z = (z * invAlpha) + (target.z() * alpha);
        this.w = (w * invAlpha) + (target.w() * alpha);
        this.u = (u * invAlpha) + (target.u() * alpha);
        return this;
    }

    public Point5Float interpolate(Point5<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    public Point5Float setToRandomDirection(Random random) {
        x = Distributor.linearNormalF(random.nextInt());
        y = Distributor.linearNormalF(random.nextInt());
        z = Distributor.linearNormalF(random.nextInt());
        w = Distributor.linearNormalF(random.nextInt());
        u = Distributor.linearNormalF(random.nextInt());
        return nor();
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point5Float clampEach (float min, float max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        u = Math.min(Math.max(u, min), max);
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
    public Point5Float fract () {
        x -= MathTools.floor(x);
        y -= MathTools.floor(y);
        z -= MathTools.floor(z);
        w -= MathTools.floor(w);
        u -= MathTools.floor(u);
        return this;
    }
    public Point5Float fractional () {
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
        }
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public Point5Float setAt(int index, float value){
        switch (index){
            default: x = value;
            case 1 : y = value;
            case 2 : z = value;
            case 3 : w = value;
            case 4 : u = value;
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int h = BitConversion.floatToIntBits(x) + 53  * BitConversion.floatToIntBits(y) +
                113 * BitConversion.floatToIntBits(z) + 151 * BitConversion.floatToIntBits(w) +
                211 * BitConversion.floatToIntBits(u);
        return h ^ h >>> 16;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
        out.writeFloat(w);
        out.writeFloat(u);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        w = in.readFloat();
        u = in.readFloat();
    }

    /** Converts this {@code Point5Float} to a string in the format {@code (x,y,z,w,u)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.general(x) + ","
                   + Base.BASE10.general(y) + ","
                   + Base.BASE10.general(z) + ","
                   + Base.BASE10.general(w) + ","
                   + Base.BASE10.general(u) + ")";
    }

    /** Sets this {@code Point5Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this vector for chaining */
    public Point5Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        int s3 = s.indexOf(',', s2 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s3 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Base.BASE10.readFloat(s, 1, s0);
            float y = Base.BASE10.readFloat(s, s0 + 1, s1);
            float z = Base.BASE10.readFloat(s, s1 + 1, s2);
            float w = Base.BASE10.readFloat(s, s2 + 1, s3);
            float u = Base.BASE10.readFloat(s, s3 + 1, s.length() - 1);
            return this.set(x, y, z, w, u);
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
        return (x == c || y == c || z == c || w == c || u == c);
    }

    @Override
    public int size() {
        return 5;
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
