package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point4;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A mutable 4D point with int components implementing {@link Point4}, {@link OfInt},
 * {@link PointNInt}, and {@link Externalizable}.
 */
public class Point4Int implements Point4<Point4Int>, PointNInt<Point4Int, Point4<?>>, PrimitiveCollection.OfInt, Externalizable {

    public int x;
    public int y;
    public int z;
    public int w;

    public Point4Int() {
    }

    public Point4Int(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Point4Int(Point4Int p) {
        this(p.x, p.y, p.z, p.w);
    }

    public Point4Int(Point4<?> p) {
        this(p.xi(), p.yi(), p.zi(), p.wi());
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
    public Point4Int cpy() {
        return new Point4Int(this);
    }

    public Point4Int copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w;
    }

    @Override
    public Point4Int set(Point4Int point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        return this;
    }
    public Point4Int set(Point4<?> point) {
        x = point.xi();
        y = point.yi();
        z = point.zi();
        w = point.wi();
        return this;
    }

    @Override
    public Point4Int sub(Point4Int point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        return this;
    }
    public Point4Int sub(Point4<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        return this;
    }
    public Point4Int subtract(Point4<?> point) {
        return sub(point);
    }

    @Override
    public Point4Int add(Point4Int point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        return this;
    }
    public Point4Int add(Point4<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        return this;
    }

    @Override
    public Point4Int scl(Point4Int point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        return this;
    }
    public Point4Int scl(Point4<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        return this;
    }
    public Point4Int scale(Point4<?> point) {
        return scl(point);
    }
    public Point4Int mul(Point4Int point) {
        return scl(point);
    }
    public Point4Int mul(Point4<?> point) {
        return scl(point);
    }
    public Point4Int multiply(Point4<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point4Int point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w);
    }
    public int dst2(Point4<?> point) {
        return
                (point.xi() - x) * (point.xi() - x) +
                (point.yi() - y) * (point.yi() - y) +
                (point.zi() - z) * (point.zi() - z) +
                (point.wi() - w) * (point.wi() - w);
    }
    public float distance(Point4<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public int distanceSquared(Point4<?> point) {
        return dst2(point);
    }
    @Override
    public Point4Int setZero() {
        x = y = z = w = 0;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point4Int x(float next) {
        x = (int)next;
        return this;
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Point4Int xi(int next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point4Int y(float next) {
        y = (int)next;
        return this;
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Point4Int yi(int next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point4Int z(float next) {
        z = (int)next;
        return this;
    }

    @Override
    public int zi() {
        return z;
    }

    @Override
    public Point4Int zi(int next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point4Int w(float next) {
        w = (int)next;
        return this;
    }

    @Override
    public int wi() {
        return w;
    }

    @Override
    public Point4Int wi(int next) {
        w = next;
        return this;
    }
    @Override
    public Point4Int set(float x, float y, float z, float w) {
        this.x = (int)x;
        this.y = (int)y;
        this.z = (int)z;
        this.w = (int)w;
        return this;
    }
    public Point4Int set(int x, int y, int z, int w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Point4Int add(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Point4Int sub(float x, float y, float z, float w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }
    public Point4Int subtract(float x, float y, float z, float w) {
        return sub(x, y, z, w);
    }
    public Point4Int scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        return this;
    }

    public Point4Int scale(float scalar) {
        return scl(scalar);
    }

    public Point4Int scl(float x, float y, float z, float w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Point4Int scale(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Int mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Int multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Int mul(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Int multiply(float x, float y, float z, float w) {
        return scl(x, y, z, w);
    }

    public Point4Int add(int x, int y, int z, int w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Point4Int sub(int x, int y, int z, int w) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        return this;
    }
    public Point4Int subtract(int x, int y, int z, int w) {
        return sub(x, y, z, w);
    }
    public Point4Int scl(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        return this;
    }

    public Point4Int scale(int scalar) {
        return scl(scalar);
    }

    public Point4Int scl(int x, int y, int z, int w) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        return this;
    }

    public Point4Int scale(int x, int y, int z, int w) {
        return scl(x, y, z, w);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Int mul(int scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point4Int multiply(int scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Int mul(int x, int y, int z, int w) {
        return scl(x, y, z, w);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point4Int multiply(int x, int y, int z, int w) {
        return scl(x, y, z, w);
    }

    public Point4Int addProduct(Point4<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point4Int mulAdd(Point4<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        return this;
    }

    public Point4Int mulAdd(Point4<?> vec, Point4<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        return this;
    }

    public Point4Int addProduct(Point4<?> vec, Point4<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point4Int limit(float limit) {
        return limit2(limit * limit);
    }

    public Point4Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point4Int limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point4Int clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point4Int setLength(float len) {
        return setLength2(len * len);
    }

    public Point4Int setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point4<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w();
    }

    public static float dot(Point4<?> a, Point4<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w();
    }

    public Point4Int lerp(Point4<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (int)((x * invAlpha) + (target.x() * alpha));
        this.y = (int)((y * invAlpha) + (target.y() * alpha));
        this.z = (int)((z * invAlpha) + (target.z() * alpha));
        this.w = (int)((w * invAlpha) + (target.w() * alpha));
        return this;
    }

    public Point4Int interpolate(Point4<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point4Int clampEach (int min, int max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        return this;
    }

    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public int get (int index) {
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
    public Point4Int setAt(int index, int value){
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
        return x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeInt(w);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();
        w = in.readInt();
    }

    /** Converts this {@code Point4Int} to a string in the format {@code (x,y,z,w)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.signed(x) + ","
                   + Base.BASE10.signed(y) + ","
                   + Base.BASE10.signed(z) + ","
                   + Base.BASE10.signed(w) + ")";
    }

    /** Sets this {@code Point4Int} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point4Int fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            int x = Base.BASE10.readInt(s, 1, s0);
            int y = Base.BASE10.readInt(s, s0 + 1, s1);
            int z = Base.BASE10.readInt(s, s1 + 1, s2);
            int w = Base.BASE10.readInt(s, s2 + 1, s.length() - 1);
            return this.set(x, y, z, w);
        }

        throw new IllegalArgumentException("Not a valid format for a Point4Int: " + s);
    }

    @Override
    public boolean add(int c) {
        throw new UnsupportedOperationException("Point4Int is fixed-size.");
    }

    @Override
    public boolean remove(int c) {
        throw new UnsupportedOperationException("Point4Int is fixed-size.");
    }

    @Override
    public boolean contains(int c) {
        return (x == c || y == c || z == c || w == c);
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point4Int is fixed-size.");
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
    public PointNIntIterator iterator() {
        return new PointNIntIterator(this);
    }

}
