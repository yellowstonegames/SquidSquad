package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point6;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;

/**
 * A mutable 6D point with int components implementing {@link Point6}, {@link PrimitiveCollection.OfInt},
 * and {@link PointNInt}.
 */
public class Point6Int implements Point6<Point6Int>, PointNInt<Point6Int, Point6<?>>, PrimitiveCollection.OfInt {

    public int x;
    public int y;
    public int z;
    public int w;
    public int u;
    public int v;

    public Point6Int() {
    }

    public Point6Int(int x, int y, int z, int w, int u, int v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        this.v = v;
    }

    public Point6Int(Point6Int p) {
        this(p.x, p.y, p.z, p.w, p.u, p.v);
    }

    public Point6Int(Point6<?> p) {
        this(p.xi(), p.yi(), p.zi(), p.wi(), p.ui(), p.vi());
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
    public Point6Int cpy() {
        return new Point6Int(this);
    }

    public Point6Int copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z + w * w + u * u + v * v;
    }

    @Override
    public Point6Int set(Point6Int point) {
        x = point.x;
        y = point.y;
        z = point.z;
        w = point.w;
        u = point.u;
        v = point.v;
        return this;
    }
    public Point6Int set(Point6<?> point) {
        x = point.xi();
        y = point.yi();
        z = point.zi();
        w = point.wi();
        u = point.ui();
        v = point.vi();
        return this;
    }

    @Override
    public Point6Int sub(Point6Int point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        w -= point.w;
        u -= point.u;
        v -= point.v;
        return this;
    }
    public Point6Int sub(Point6<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        w -= point.w();
        u -= point.u();
        v -= point.v();
        return this;
    }
    public Point6Int subtract(Point6<?> point) {
        return sub(point);
    }

    @Override
    public Point6Int add(Point6Int point) {
        x += point.x;
        y += point.y;
        z += point.z;
        w += point.w;
        u += point.u;
        v += point.v;
        return this;
    }
    public Point6Int add(Point6<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        w += point.w();
        u += point.u();
        v += point.v();
        return this;
    }

    @Override
    public Point6Int scl(Point6Int point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        w *= point.w;
        u *= point.u;
        v *= point.v;
        return this;
    }
    public Point6Int scl(Point6<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        w *= point.w();
        u *= point.u();
        v *= point.v();
        return this;
    }
    public Point6Int scale(Point6<?> point) {
        return scl(point);
    }
    public Point6Int mul(Point6Int point) {
        return scl(point);
    }
    public Point6Int mul(Point6<?> point) {
        return scl(point);
    }
    public Point6Int multiply(Point6<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point6Int point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z) +
                (point.w - w) * (point.w - w) +
                (point.u - u) * (point.u - u) +
                (point.v - v) * (point.v - v);
    }
    public int dst2(Point6<?> point) {
        return
                (point.xi() - x) * (point.xi() - x) +
                (point.yi() - y) * (point.yi() - y) +
                (point.zi() - z) * (point.zi() - z) +
                (point.wi() - w) * (point.wi() - w) +
                (point.ui() - u) * (point.ui() - u) +
                (point.vi() - v) * (point.vi() - v);
    }
    public float distance(Point6<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public int distanceSquared(Point6<?> point) {
        return dst2(point);
    }
    @Override
    public Point6Int setZero() {
        x = y = z = w = u = v = 0;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point6Int x(float next) {
        x = (int)next;
        return this;
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Point6Int xi(int next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point6Int y(float next) {
        y = (int)next;
        return this;
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Point6Int yi(int next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point6Int z(float next) {
        z = (int)next;
        return this;
    }

    @Override
    public int zi() {
        return z;
    }

    @Override
    public Point6Int zi(int next) {
        z = next;
        return this;
    }

    @Override
    public float w() {
        return w;
    }

    @Override
    public Point6Int w(float next) {
        w = (int)next;
        return this;
    }

    @Override
    public int wi() {
        return w;
    }

    @Override
    public Point6Int wi(int next) {
        w = next;
        return this;
    }

    @Override
    public float u() {
        return u;
    }

    @Override
    public Point6Int u(float next) {
        u = (int)next;
        return this;
    }

    @Override
    public int ui() {
        return u;
    }

    @Override
    public Point6Int ui(int next) {
        u = next;
        return this;
    }

    @Override
    public float v() {
        return v;
    }

    @Override
    public Point6Int v(float next) {
        v = (int)next;
        return this;
    }

    @Override
    public int vi() {
        return v;
    }

    @Override
    public Point6Int vi(int next) {
        v = next;
        return this;
    }

    @Override
    public Point6Int set(float x, float y, float z, float w, float u, float v) {
        this.x = (int)x;
        this.y = (int)y;
        this.z = (int)z;
        this.w = (int)w;
        this.u = (int)u;
        this.v = (int)v;
        return this;
    }
    public Point6Int set(int x, int y, int z, int w, int u, int v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        this.u = u;
        this.v = v;
        return this;
    }

    public Point6Int add(float x, float y, float z, float w, float u, float v) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        this.v += v;
        return this;
    }

    public Point6Int sub(float x, float y, float z, float w, float u, float v) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        this.v -= v;
        return this;
    }
    public Point6Int subtract(float x, float y, float z, float w, float u, float v) {
        return sub(x, y, z, w, u, v);
    }
    public Point6Int scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        v *= scalar;
        return this;
    }

    public Point6Int scale(float scalar) {
        return scl(scalar);
    }

    public Point6Int scl(float x, float y, float z, float w, float u, float v) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        this.v *= v;
        return this;
    }

    public Point6Int scale(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Int mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Int multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Int mul(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float, float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Int multiply(float x, float y, float z, float w, float u, float v) {
        return scl(x, y, z, w, u, v);
    }

    public Point6Int add(int x, int y, int z, int w, int u, int v) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        this.u += u;
        this.v += v;
        return this;
    }

    public Point6Int sub(int x, int y, int z, int w, int u, int v) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        this.w -= w;
        this.u -= u;
        this.v -= v;
        return this;
    }
    public Point6Int subtract(int x, int y, int z, int w, int u, int v) {
        return sub(x, y, z, w, u, v);
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point6Int plus(float scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        w += scalar;
        u += scalar;
        v += scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point6Int minus(float scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        w -= scalar;
        u -= scalar;
        v -= scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point6Int times(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        v *= scalar;
        return this;
    }

    public Point6Int scl(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;
        u *= scalar;
        v *= scalar;
        return this;
    }

    public Point6Int scale(int scalar) {
        return scl(scalar);
    }

    public Point6Int scl(int x, int y, int z, int w, int u, int v) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        this.w *= w;
        this.u *= u;
        this.v *= v;
        return this;
    }

    public Point6Int scale(int x, int y, int z, int w, int u, int v) {
        return scl(x, y, z, w, u, v);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Int mul(int scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point6Int multiply(int scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Int mul(int x, int y, int z, int w, int u, int v) {
        return scl(x, y, z, w, u, v);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int, int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point6Int multiply(int x, int y, int z, int w, int u, int v) {
        return scl(x, y, z, w, u, v);
    }

    public Point6Int addProduct(Point6<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point6Int mulAdd(Point6<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        w += vec.w() * scalar;
        u += vec.u() * scalar;
        v += vec.v() * scalar;
        return this;
    }

    public Point6Int mulAdd(Point6<?> vec, Point6<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        w += vec.w() * mulVec.w();
        u += vec.u() * mulVec.u();
        v += vec.v() * mulVec.v();
        return this;
    }

    public Point6Int addProduct(Point6<?> vec, Point6<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point6Int limit(float limit) {
        return limit2(limit * limit);
    }

    public Point6Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point6Int limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point6Int clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point6Int setLength(float len) {
        return setLength2(len * len);
    }

    public Point6Int setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point6<?> other) {
        return x * other.x() + y * other.y() + z * other.z() + w * other.w() + u * other.u() + v * other.v();
    }

    public static float dot(Point6<?> a, Point6<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z() + a.w() * b.w() + a.u() * b.u() + a.v() * b.v();
    }

    public Point6Int lerp(Point6<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (int)((x * invAlpha) + (target.x() * alpha));
        this.y = (int)((y * invAlpha) + (target.y() * alpha));
        this.z = (int)((z * invAlpha) + (target.z() * alpha));
        this.w = (int)((w * invAlpha) + (target.w() * alpha));
        this.u = (int)((u * invAlpha) + (target.u() * alpha));
        this.v = (int)((v * invAlpha) + (target.v() * alpha));
        return this;
    }

    public Point6Int interpolate(Point6<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point6Int clampEach (int min, int max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
        w = Math.min(Math.max(w, min), max);
        u = Math.min(Math.max(u, min), max);
        v = Math.min(Math.max(v, min), max);
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
    public Point6Int setAt(int index, int value){
        switch (index){
            default: x = value; break;
            case 1 : y = value; break;
            case 2 : z = value; break;
            case 3 : w = value; break;
            case 4 : u = value; break;
            case 5 : v = value; break;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point6Int)) return false;

        Point6Int point6Int = (Point6Int) o;
        return x == point6Int.x && y == point6Int.y && z == point6Int.z && w == point6Int.w && u == point6Int.u && v == point6Int.v;
    }

    @Override
    public int hashCode() {
        return x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
    }

    /** Converts this {@code Point6Int} to a string in the format {@code (x,y,z,w,u,v)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.signed(x) + ","
                   + Base.BASE10.signed(y) + ","
                   + Base.BASE10.signed(z) + ","
                   + Base.BASE10.signed(w) + ","
                   + Base.BASE10.signed(u) + ","
                   + Base.BASE10.signed(v) + ")";
    }

    /** Sets this {@code Point6Int} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point6Int fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        int s2 = s.indexOf(',', s1 + 1);
        int s3 = s.indexOf(',', s2 + 1);
        int s4 = s.indexOf(',', s3 + 1);
        if (s0 != -1 && s1 != -1 && s2 != -1 && s3 != -1 && s4 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            int x = Base.BASE10.readInt(s, 1, s0);
            int y = Base.BASE10.readInt(s, s0 + 1, s1);
            int z = Base.BASE10.readInt(s, s1 + 1, s2);
            int w = Base.BASE10.readInt(s, s2 + 1, s3);
            int u = Base.BASE10.readInt(s, s3 + 1, s4);
            int v = Base.BASE10.readInt(s, s4 + 1, s.length() - 1);
            return this.set(x, y, z, w, u, v);
        }

        throw new IllegalArgumentException("Not a valid format for a Point6Int: " + s);
    }

    /**
     * Not supported; this collection is fixed-size.
     * @param c ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(int c) {
        throw new UnsupportedOperationException("Point6Int is fixed-size.");
    }

    /**
     * Not supported; this collection is fixed-size.
     * @param c ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean remove(int c) {
        throw new UnsupportedOperationException("Point6Int is fixed-size.");
    }

    /**
     * Not supported; this collection is fixed-size.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point6Int is fixed-size.");
    }

    /**
     * Compares {@code c} with the components of this collection using {@code ==}; if any are considered equal, this
     * returns true.
     * @param c an int to be compared using {@code ==}
     * @return true if any component of this collection is equal to {@code c} via {@code ==} comparison
     */
    @Override
    public boolean contains(int c) {
        return (x == c || y == c || z == c || w == c || u == c || v == c);
    }

    @Override
    public int size() {
        return 6;
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
