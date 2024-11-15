package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point5;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A mutable 5D point with int components implementing {@link Point5}, {@link OfInt},
 * {@link PointNInt}, and {@link Externalizable}.
 */
public class Point5Int implements Point5<Point5Int>, PointNInt<Point5Int>, PrimitiveCollection.OfInt, Externalizable {

    public int x;
    public int y;
    public int z;
    public int w;
    public int u;

    public Point5Int() {
    }

    public Point5Int(int x, int y, int z, int w, int u) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
    }

    public Point5Int(Point5Int p) {
        this(p.x, p.y, p.z, p.w, p.u);
    }

    public Point5Int(Point5<?> p) {
        this(p.xi(), p.yi(), p.zi(), p.wi(), p.ui());
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
    public Point5Int cpy() {
        return new Point5Int(this);
    }

    public Point5Int copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w + u * u;
    }

    @Override
    public Point5Int set(Point5Int point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        u = point.u;
        return this;
    }
    public Point5Int set(Point5<?> point) {
        x = point.xi();
        y = point.yi();
        z = point.zi();
        w = point.wi();
        u = point.ui();
        return this;
    }

    @Override
    public Point5Int sub(Point5Int point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        u -= point.u;
        return this;
    }
    public Point5Int sub(Point5<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        u -= point.u();
        return this;
    }
    public Point5Int subtract(Point5<?> point) {
        return sub(point);
    }

    @Override
    public Point5Int add(Point5Int point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        u += point.u;
        return this;
    }
    public Point5Int add(Point5<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        u += point.u();
        return this;
    }

    @Override
    public Point5Int scl(Point5Int point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        u *= point.u;
        return this;
    }
    public Point5Int scl(Point5<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        u *= point.u();
        return this;
    }
    public Point5Int scale(Point5<?> point) {
        return scl(point);
    }
    public Point5Int mul(Point5Int point) {
        return scl(point);
    }
    public Point5Int mul(Point5<?> point) {
        return scl(point);
    }
    public Point5Int multiply(Point5<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point5Int point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w) +
                (point.u - u) * (point.u - u);
    }
    public int dst2(Point5<?> point) {
        return
                (point.xi() - x) * (point.xi() - x) +
                (point.yi() - y) * (point.yi() - y) +
                (point.zi() - z) * (point.zi() - z) +
                (point.wi() - w) * (point.wi() - w) +
                (point.ui() - u) * (point.ui() - u);
    }
    public float distance(Point5<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public int distanceSquared(Point5<?> point) {
        return dst2(point);
    }
    @Override
    public Point5Int setZero() {
        x = y = z = w = u = 0;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point5Int x(float next) {
        x = (int)next;
        return this;
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Point5Int xi(int next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point5Int y(float next) {
        y = (int)next;
        return this;
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Point5Int yi(int next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point5Int z(float next) {
        z = (int)next;
        return this;
    }

    @Override
    public int zi() {
        return z;
    }

    @Override
    public Point5Int zi(int next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point5Int w(float next) {
        w = (int)next;
        return this;
    }

    @Override
    public int wi() {
        return w;
    }

    @Override
    public Point5Int wi(int next) {
        w = next;
        return this;
    }

    @Override
    public float u() {
        return u;
    }

    @Override
    public Point5Int u(float next) {
        u = (int)next;
        return this;
    }

    @Override
    public int ui() {
        return u;
    }

    @Override
    public Point5Int ui(int next) {
        u = next;
        return this;
    }

    @Override
    public Point5Int set(float x, float y, float z, float w, float u) {
        this.x = (int)x;
        this.y = (int)y;
        this.z = (int)z;
        this.w = (int)w;
        this.u = (int)u;
        return this;
    }
    public Point5Int set(int x, int y, int z, int w, int u) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        return this;
    }

    public Point5Int add(float x, float y, float z, float w, float u) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        return this;
    }

    public Point5Int sub(float x, float y, float z, float w, float u) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        return this;
    }
    public Point5Int subtract(float x, float y, float z, float w, float u) {
        return sub(x, y, z, w, u);
    }
    public Point5Int scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        return this;
    }

    public Point5Int scale(float scalar) {
        return scl(scalar);
    }

    public Point5Int scl(float x, float y, float z, float w, float u) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        return this;
    }

    public Point5Int scale(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Int mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Int multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Int mul(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Int multiply(float x, float y, float z, float w, float u) {
        return scl(x, y, z, w, u);
    }

    public Point5Int add(int x, int y, int z, int w, int u) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        return this;
    }

    public Point5Int sub(int x, int y, int z, int w, int u) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        return this;
    }
    public Point5Int subtract(int x, int y, int z, int w, int u) {
        return sub(x, y, z, w, u);
    }
    public Point5Int scl(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        return this;
    }

    public Point5Int scale(int scalar) {
        return scl(scalar);
    }

    public Point5Int scl(int x, int y, int z, int w, int u) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        return this;
    }

    public Point5Int scale(int x, int y, int z, int w, int u) {
        return scl(x, y, z, w, u);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Int mul(int scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point5Int multiply(int scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Int mul(int x, int y, int z, int w, int u) {
        return scl(x, y, z, w, u);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point5Int multiply(int x, int y, int z, int w, int u) {
        return scl(x, y, z, w, u);
    }

    public Point5Int addProduct(Point5<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point5Int mulAdd(Point5<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        u += vec.u() * scalar;
        return this;
    }

    public Point5Int mulAdd(Point5<?> vec, Point5<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        u += vec.u() * mulVec.u();
        return this;
    }

    public Point5Int addProduct(Point5<?> vec, Point5<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point5Int limit(float limit) {
        return limit2(limit * limit);
    }

    public Point5Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point5Int limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point5Int clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point5Int setLength(float len) {
        return setLength2(len * len);
    }

    public Point5Int setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point5<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w() + u * other.u();
    }

    public static float dot(Point5<?> a, Point5<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w() + a.u() * b.u();
    }

    public Point5Int lerp(Point5<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (int)((x * invAlpha) + (target.x() * alpha));
        this.y = (int)((y * invAlpha) + (target.y() * alpha));
        this.z = (int)((z * invAlpha) + (target.z() * alpha));
        this.w = (int)((w * invAlpha) + (target.w() * alpha));
        this.u = (int)((u * invAlpha) + (target.u() * alpha));
        return this;
    }

    public Point5Int interpolate(Point5<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point5Int clampEach (int min, int max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        u = Math.min(Math.max(u, min), max);
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
    public Point5Int setAt(int index, int value){
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
        return x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
        out.writeInt(w);
        out.writeInt(u);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();
        w = in.readInt();
        u = in.readInt();
    }

    /** Converts this {@code Point5Int} to a string in the format {@code (x,y,z,w,u)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.signed(x) + ","
                   + Base.BASE10.signed(y) + ","
                   + Base.BASE10.signed(z) + ","
                   + Base.BASE10.signed(w) + ","
                   + Base.BASE10.signed(u) + ")";
    }

    /** Sets this {@code Point5Int} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point5Int fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        int s3 = s.indexOf(',', s2 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s3 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            int x = Base.BASE10.readInt(s, 1, s0);
            int y = Base.BASE10.readInt(s, s0 + 1, s1);
            int z = Base.BASE10.readInt(s, s1 + 1, s2);
            int w = Base.BASE10.readInt(s, s2 + 1, s3);
            int v = Base.BASE10.readInt(s, s3 + 1, s.length() - 1);
            return this.set(x, y, z, w, u);
        }

        throw new IllegalArgumentException("Not a valid format for a Point5Int: " + s);
    }

    @Override
    public boolean add(int c) {
        throw new UnsupportedOperationException("Point5Int is fixed-size.");
    }

    @Override
    public boolean remove(int c) {
        throw new UnsupportedOperationException("Point5Int is fixed-size.");
    }

    @Override
    public boolean contains(int c) {
        return (x == c || y == c || z == c || w == c || u == c);
    }

    @Override
    public int size() {
        return 5;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point5Int is fixed-size.");
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
