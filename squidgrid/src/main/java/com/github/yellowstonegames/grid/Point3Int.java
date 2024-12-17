package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point3;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.PrimitiveCollection;

/**
 * A mutable 3D point with int components implementing {@link Point3}, {@link OfInt},
 * and {@link PointNInt}.
 */
public class Point3Int implements Point3<Point3Int>, PointNInt<Point3Int, Point3<?>>, PrimitiveCollection.OfInt {

    public int x;
    public int y;
    public int z;

    public Point3Int() {
    }

    public Point3Int(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3Int(Point3Int p) {
        this(p.x, p.y, p.z);
    }

    public Point3Int(Point3<?> p) {
        this(p.xi(), p.yi(), p.zi());
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
    public Point3Int cpy() {
        return new Point3Int(this);
    }

    public Point3Int copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z;
    }

    @Override
    public Point3Int set(Point3Int point) {
        x = point.x;
        y = point.y;
        z = point.z;
        return this;
    }
    public Point3Int set(Point3<?> point) {
        x = point.xi();
        y = point.yi();
        z = point.zi();
        return this;
    }

    @Override
    public Point3Int sub(Point3Int point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        return this;
    }
    public Point3Int sub(Point3<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        return this;
    }
    public Point3Int subtract(Point3<?> point) {
        return sub(point);
    }

    @Override
    public Point3Int add(Point3Int point) {
        x += point.x;
        y += point.y;
        z += point.z;
        return this;
    }
    public Point3Int add(Point3<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        return this;
    }

    @Override
    public Point3Int scl(Point3Int point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        return this;
    }
    public Point3Int scl(Point3<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        return this;
    }
    public Point3Int scale(Point3<?> point) {
        return scl(point);
    }
    public Point3Int mul(Point3Int point) {
        return scl(point);
    }
    public Point3Int mul(Point3<?> point) {
        return scl(point);
    }
    public Point3Int multiply(Point3<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point3Int point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z);
    }
    public int dst2(Point3<?> point) {
        return
                (point.xi() - x) * (point.xi() - x) +
                (point.yi() - y) * (point.yi() - y) +
                (point.zi() - z) * (point.zi() - z);
    }
    public float distance(Point3<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public int distanceSquared(Point3<?> point) {
        return dst2(point);
    }
    @Override
    public Point3Int setZero() {
        x = y = z = 0;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point3Int x(float next) {
        x = (int)next;
        return this;
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Point3Int xi(int next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point3Int y(float next) {
        y = (int)next;
        return this;
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Point3Int yi(int next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point3Int z(float next) {
        z = (int)next;
        return this;
    }

    @Override
    public int zi() {
        return z;
    }

    @Override
    public Point3Int zi(int next) {
        z = next;
        return this;
    }

    @Override
    public Point3Int set(float x, float y, float z) {
        this.x = (int)x;
        this.y = (int)y;
        this.z = (int)z;
        return this;
    }
    public Point3Int set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Point3Int add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Point3Int sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }
    public Point3Int subtract(float x, float y, float z) {
        return sub(x, y, z);
    }
    public Point3Int scl(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Point3Int scale(float scalar) {
        return scl(scalar);
    }

    public Point3Int scl(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Point3Int scale(float x, float y, float z) {
        return scl(x, y, z);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Int mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Int multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point3Int mul(float x, float y, float z) {
        return scl(x, y, z);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point3Int multiply(float x, float y, float z) {
        return scl(x, y, z);
    }

    public Point3Int add(int x, int y, int z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Point3Int sub(int x, int y, int z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }
    public Point3Int subtract(int x, int y, int z) {
        return sub(x, y, z);
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point3Int plus(float scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point3Int minus(float scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point3Int times(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Point3Int scl(int scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Point3Int scale(int scalar) {
        return scl(scalar);
    }

    public Point3Int scl(int x, int y, int z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Point3Int scale(int x, int y, int z) {
        return scl(x, y, z);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Int mul(int scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int)}.
     * @param scalar an int that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Int multiply(int scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point3Int mul(int x, int y, int z) {
        return scl(x, y, z);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(int, int, int)}.
     * @param x an int that will be multiplied with x
     * @param y an int that will be multiplied with y
     * @return this, for chaining
     */
    public Point3Int multiply(int x, int y, int z) {
        return scl(x, y, z);
    }

    public Point3Int addProduct(Point3<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point3Int mulAdd(Point3<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        return this;
    }

    public Point3Int mulAdd(Point3<?> vec, Point3<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        return this;
    }

    public Point3Int addProduct(Point3<?> vec, Point3<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point3Int limit(float limit) {
        return limit2(limit * limit);
    }

    public Point3Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point3Int limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point3Int clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point3Int setLength(float len) {
        return setLength2(len * len);
    }

    public Point3Int setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point3<?> other) {
        return x * other.x() + y * other.y() + z * other.z();
    }

    public static float dot(Point3<?> a, Point3<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }

    public Point3Int lerp(Point3<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (int)((x * invAlpha) + (target.x() * alpha));
        this.y = (int)((y * invAlpha) + (target.y() * alpha));
        this.z = (int)((z * invAlpha) + (target.z() * alpha));
        return this;
    }

    public Point3Int interpolate(Point3<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point3Int clampEach (int min, int max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
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
        }
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public Point3Int setAt(int index, int value){
        switch (index){
            default: x = value;
            case 1 : y = value;
            case 2 : z = value;
        }
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Point3Int)) return false;

        Point3Int point3Int = (Point3Int) o;
        return x == point3Int.x && y == point3Int.y && z == point3Int.z;
    }

    @Override
    public int hashCode() {
        return x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
    }

    /** Converts this {@code Point3Int} to a string in the format {@code (x,y,z)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.signed(x) + ","
                   + Base.BASE10.signed(y) + ","
                   + Base.BASE10.signed(z) + ")";
    }

    /** Sets this {@code Point3Int} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point3Int fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        if (s0 != -1 && s1 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            int x = Base.BASE10.readInt(s, 1, s0);
            int y = Base.BASE10.readInt(s, s0 + 1, s1);
            int z = Base.BASE10.readInt(s, s1 + 1, s.length() - 1);
            return this.set(x, y, z);
        }

        throw new IllegalArgumentException("Not a valid format for a Point3Int: " + s);
    }

    @Override
    public boolean add(int c) {
        throw new UnsupportedOperationException("Point3Int is fixed-size.");
    }

    @Override
    public boolean remove(int c) {
        throw new UnsupportedOperationException("Point3Int is fixed-size.");
    }

    @Override
    public boolean contains(int c) {
        return (x == c || y == c || z == c);
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point3Int is fixed-size.");
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
