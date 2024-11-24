package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point3;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * A mutable 3D point with float components implementing {@link Point3}, {@link OfFloat},
 * {@link PointNFloat}, and {@link Externalizable}.
 */
public class Point3Float implements Point3<Point3Float>, PointNFloat<Point3Float, Point3<?>>, PrimitiveCollection.OfFloat, Externalizable {

    public float x;
    public float y;
    public float z;

    public Point3Float() {
    }

    public Point3Float(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3Float(Point3Float p) {
        this(p.x, p.y, p.z);
    }

    public Point3Float(Point3<?> p) {
        this(p.x(), p.y(), p.z());
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
    public Point3Float cpy() {
        return new Point3Float(this);
    }

    public Point3Float copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y + z * z;
    }

    @Override
    public Point3Float set(Point3Float point) {
        x = point.x;
        y = point.y;
        z = point.z;
        return this;
    }
    public Point3Float set(Point3<?> point) {
        x = point.x();
        y = point.y();
        z = point.z();
        return this;
    }

    @Override
    public Point3Float sub(Point3Float point) {
        x -= point.x;
        y -= point.y;
        z -= point.z;
        return this;
    }
    public Point3Float sub(Point3<?> point) {
        x -= point.x();
        y -= point.y();
        z -= point.z();
        return this;
    }
    public Point3Float subtract(Point3<?> point) {
        return sub(point);
    }

    @Override
    public Point3Float add(Point3Float point) {
        x += point.x;
        y += point.y;
        z += point.z;
        return this;
    }
    public Point3Float add(Point3<?> point) {
        x += point.x();
        y += point.y();
        z += point.z();
        return this;
    }

    @Override
    public Point3Float scl(Point3Float point) {
        x *= point.x;
        y *= point.y;
        z *= point.z;
        return this;
    }
    public Point3Float scl(Point3<?> point) {
        x *= point.x();
        y *= point.y();
        z *= point.z();
        return this;
    }
    public Point3Float scale(Point3<?> point) {
        return scl(point);
    }
    public Point3Float mul(Point3Float point) {
        return scl(point);
    }
    public Point3Float mul(Point3<?> point) {
        return scl(point);
    }
    public Point3Float multiply(Point3<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point3Float point) {
        return
                (point.x - x) * (point.x - x) +
                (point.y - y) * (point.y - y) +
                (point.z - z) * (point.z - z);
    }
    public float dst2(Point3<?> point) {
        return
                (point.x() - x) * (point.x() - x) +
                (point.y() - y) * (point.y() - y) +
                (point.z() - z) * (point.z() - z);
    }
    public float distance(Point3<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public float distanceSquared(Point3<?> point) {
        return dst2(point);
    }
    @Override
    public Point3Float setZero() {
        x = y = z = 0f;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point3Float x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point3Float y(float next) {
        y = next;
        return this;
    }

    @Override
    public float z() {
        return z;
    }

    @Override
    public Point3Float z(float next) {
        z = next;
        return this;
    }

    @Override
    public Point3Float set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Point3Float nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (float) Math.sqrt(l);
        x *= l;
        y *= l;
        z *= l;
        return this;
    }

    public Point3Float normalize() {
        return nor();
    }

    public Point3Float add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Point3Float sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }
    public Point3Float subtract(float x, float y, float z) {
        return sub(x, y, z);
    }

    public Point3Float plus(float scalar) {
        x += scalar;
        y += scalar;
        z += scalar;
        return this;
    }

    public Point3Float minus(float scalar) {
        x -= scalar;
        y -= scalar;
        z -= scalar;
        return this;
    }

    public Point3Float times(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        return this;
    }

    public Point3Float scale(float scalar) {
        return times(scalar);
    }

    public Point3Float scl(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Point3Float scale(float x, float y, float z) {
        return scl(x, y, z);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #times(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Float mul(float scalar) {
        return times(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #times(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point3Float multiply(float scalar) {
        return times(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @param z a float that will be multiplied with z
     * @return this, for chaining
     */
    public Point3Float mul(float x, float y, float z) {
        return scl(x, y, z);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @param z a float that will be multiplied with z
     * @return this, for chaining
     */
    public Point3Float multiply(float x, float y, float z) {
        return scl(x, y, z);
    }

    public Point3Float addProduct(Point3<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point3Float mulAdd(Point3<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        z += vec.z() * scalar;
        return this;
    }

    public Point3Float mulAdd(Point3<?> vec, Point3<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        z += vec.z() * mulVec.z();
        return this;
    }

    public Point3Float addProduct(Point3<?> vec, Point3<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point3Float limit(float limit) {
        return limit2(limit * limit);
    }

    public Point3Float limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return times((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point3Float limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point3Float clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return times((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return times((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point3Float setLength(float len) {
        return setLength2(len * len);
    }

    public Point3Float setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : times((float)Math.sqrt(len2 / oldLen2));
    }

    public float dot(Point3<?> other) {
        return x * other.x() + y * other.y() + z * other.z();
    }

    public static float dot(Point3<?> a, Point3<?> b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }

    /** Sets this point to the cross product between it and the other point.
     * @param point The other point
     * @return This point for chaining */
    public Point3Float crs (final Point3<?> point) {
        return this.set(y * point.z() - z * point.y(), z * point.x() - x * point.z(), x * point.y() - y * point.x());
    }

    /** Sets this point to the cross product between it and the other point.
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @return This point for chaining */
    public Point3Float crs (float x, float y, float z) {
        return this.set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
    }

    /** Sets this point to the cross product between it and the other point.
     * This is an alias for {@link #crs(Point3)}.
     * @param point The other point
     * @return This point for chaining */
    public Point3Float cross (final Point3<?> point) {
        return crs(point);
    }

    /** Sets this point to the cross product between it and the other point.
     * This is an alias for {@link #crs(float, float, float)}.
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @return This point for chaining */
    public Point3Float cross (float x, float y, float z) {
        return crs(x, y, z);
    }

    public Point3Float lerp(Point3<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x() * alpha);
        this.y = (y * invAlpha) + (target.y() * alpha);
        this.z = (z * invAlpha) + (target.z() * alpha);
        return this;
    }

    public Point3Float interpolate(Point3<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }
    /**
     * Spherically interpolates between this point and the target point by alpha, which must be in the range [0,1].
     * The result is stored in this point.
     *
     * @see PointNFloat#slerpGeometric(PointNFloat, PointNFloat, float, PointNFloat) An alternative for other dimensions.
     * @param target The target point
     * @param alpha The interpolation coefficient
     * @return This Point3Float for chaining
     */
    public Point3Float slerpInPlace (final Point3<?> target, float alpha) {
        final float dot = dot(target);
        // If the inputs are too close for comfort, simply linearly interpolate.
        if (dot > 0.9995 || dot < -0.9995) return lerp(target, alpha);

        // theta0 = angle between input points
        final float theta0 = TrigTools.acos(dot);
        // theta = angle between this point and result
        final float theta = theta0 * alpha;

        final float st = TrigTools.sinSmoother(theta);
        final float tx = target.x() - x * dot;
        final float ty = target.y() - y * dot;
        final float tz = target.z() - z * dot;
        final float l2 = tx * tx + ty * ty + tz * tz;
        final float dl = st * ((l2 < 0.0001f) ? 1f : 1f / (float)Math.sqrt(l2));

        return times(TrigTools.cosSmoother(theta)).add(tx * dl, ty * dl, tz * dl).nor();
    }

    public Point3Float slerpGeometric(Point3Float target, float alpha) {
        return PointNFloat.slerpGeometric(this, target, alpha, this);
    }

    /** Sets the components from the given spherical coordinates in radians.
     * @param azimuthalAngle The angle around the pole in radians (related to longitude), [0, 2pi]
     * @param polarAngle The angle between the North and South Pole in radians (related to latitude), [0, pi]
     * @return This point for chaining
     */
    public Point3Float setFromSpherical (float azimuthalAngle, float polarAngle) {
        float cosPolar = TrigTools.cos(polarAngle);
        float sinPolar = TrigTools.sin(polarAngle);

        float cosAzim = TrigTools.cos(azimuthalAngle);
        float sinAzim = TrigTools.sin(azimuthalAngle);

        return this.set(cosAzim * sinPolar, sinAzim * sinPolar, cosPolar);
    }

    /** Sets the components from the given spherical coordinates in degrees.
     * @param azimuthalAngle the angle around the pole in degrees (related to longitude), [0, 360]
     * @param polarAngle The angle between the North and South Pole in degrees (related to latitude), [0, 180]
     * @return This point for chaining
     */
    public Point3Float setFromSphericalDeg (float azimuthalAngle, float polarAngle) {
        float cosPolar = TrigTools.cosDeg(polarAngle);
        float sinPolar = TrigTools.sinDeg(polarAngle);

        float cosAzim = TrigTools.cosDeg(azimuthalAngle);
        float sinAzim = TrigTools.sinDeg(azimuthalAngle);

        return this.set(cosAzim * sinPolar, sinAzim * sinPolar, cosPolar);
    }

    /** Sets the components from the given spherical coordinates in turns.
     * @param azimuthalAngle the angle around the pole in turns (related to longitude), [0, 1]
     * @param polarAngle The angle between the North and South Pole in turns (related to latitude), [0, 0.5]
     * @return This point for chaining
     */
    public Point3Float setFromSphericalTurns (float azimuthalAngle, float polarAngle) {
        float cosPolar = TrigTools.cosTurns(polarAngle);
        float sinPolar = TrigTools.sinTurns(polarAngle);

        float cosAzim = TrigTools.cosTurns(azimuthalAngle);
        float sinAzim = TrigTools.sinTurns(azimuthalAngle);

        return this.set(cosAzim * sinPolar, sinAzim * sinPolar, cosPolar);
    }

    public Point3Float setToRandomDirection(Random random) {
        float polarAngle = TrigTools.acosTurns(2f * (random.nextFloat() - 0.5f)); // polar angle

        float cosPolar = TrigTools.cosTurns(polarAngle);
        float sinPolar = TrigTools.sinTurns(polarAngle);

        int index = random.nextInt() & TrigTools.TABLE_MASK;

        float cosAzim = TrigTools.COS_TABLE[index];
        float sinAzim = TrigTools.SIN_TABLE[index];

        return this.set(cosAzim * sinPolar, sinAzim * sinPolar, cosPolar);

    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point3Float clampEach (float min, float max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        z = Math.min(Math.max(z, min), max);
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
    public Point3Float fract () {
        x -= MathTools.floor(x);
        y -= MathTools.floor(y);
        z -= MathTools.floor(z);
        return this;
    }
    public Point3Float fractional () {
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
        }
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch"})
    public Point3Float setAt(int index, float value){
        switch (index){
            default: x = value;
            case 1 : y = value;
            case 2 : z = value;
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int h = BitConversion.floatToIntBits(x) + 53  * BitConversion.floatToIntBits(y) +
                113 * BitConversion.floatToIntBits(z);
        return h ^ h >>> 16;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
        out.writeFloat(z);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
    }

    /** Converts this {@code Point3Float} to a string in the format {@code (x,y,z)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.general(x) + ","
                   + Base.BASE10.general(y) + ","
                   + Base.BASE10.general(z) + ")";
    }

    /** Sets this {@code Point3Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point3Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        int s1 = s.indexOf(',', s0 + 1);
        if (s0 != -1 && s1 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Base.BASE10.readFloat(s, 1, s0);
            float y = Base.BASE10.readFloat(s, s0 + 1, s1);
            float z = Base.BASE10.readFloat(s, s1 + 1, s.length() - 1);
            return this.set(x, y, z);
        }

        throw new IllegalArgumentException("Not a valid format for a Point3Float: " + s);
    }

    @Override
    public boolean add(float c) {
        throw new UnsupportedOperationException("Point3Float is fixed-size.");
    }

    @Override
    public boolean remove(float c) {
        throw new UnsupportedOperationException("Point3Float is fixed-size.");
    }

    @Override
    public boolean contains(float c) {
        return (x == c || y == c || z == c);
    }

    @Override
    public int size() {
        return 3;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point3Float is fixed-size.");
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
