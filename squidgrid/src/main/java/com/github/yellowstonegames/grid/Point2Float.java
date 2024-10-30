package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * A mutable point with float components implementing {@link Point2}
 * and {@link Externalizable}. This does have some more additions to those interfaces, such as
 * {@link #get(int)} and {@link #setAt(int, float)}.
 */
public class Point2Float implements Point2<Point2Float>, Externalizable {

    public float x;
    public float y;

    public Point2Float() {
    }

    public Point2Float(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point2Float(Coord p) {
        this(p.x, p.y);
    }

    public Point2Float(Point2Float p) {
        this(p.x, p.y);
    }

    public Point2Float(Point2<?> p) {
        this(p.x(), p.y());
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
    public Point2Float cpy() {
        return new Point2Float(this);
    }
    public Point2Float copy() {
        return cpy();
    }

    @Override
    public Point2Float set(Point2Float point) {
        x = point.x;
        y = point.y;
        return this;
    }
    public Point2Float set(Point2<?> point) {
        x = point.x();
        y = point.y();
        return this;
    }

    @Override
    public Point2Float sub(Point2Float point) {
        x -= point.x;
        y -= point.y;
        return this;
    }
    public Point2Float sub(Point2<?> point) {
        x -= point.x();
        y -= point.y();
        return this;
    }
    public Point2Float subtract(Point2<?> point) {
        return sub(point);
    }

    @Override
    public Point2Float add(Point2Float point) {
        x += point.x;
        y += point.y;
        return this;
    }
    public Point2Float add(Point2<?> point) {
        x += point.x();
        y += point.y();
        return this;
    }

    @Override
    public Point2Float scl(Point2Float point) {
        x *= point.x;
        y *= point.y;
        return this;
    }
    public Point2Float scl(Point2<?> point) {
        x *= point.x();
        y *= point.y();
        return this;
    }
    public Point2Float scale(Point2<?> point) {
        return scl(point);
    }
    public Point2Float mul(Point2Float point) {
        return scl(point);
    }
    public Point2Float multiply(Point2<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point2Float point) {
        return (point.x - x) * (point.x - x) + (point.y - y) * (point.y - y);
    }
    public float dst2(Point2<?> point) {
        return (point.x() - x) * (point.x() - x) + (point.y() - y) * (point.y() - y);
    }
    public float distance(Point2<?> point) {
        return (float) Math.sqrt(dst2(point));
    }
    public float distanceSquared(Point2<?> point) {
        return dst2(point);
    }
    @Override
    public Point2Float setZero() {
        x = y = 0f;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point2Float x(float next) {
        x = next;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point2Float y(float next) {
        y = next;
        return this;
    }

    public Point2Float set(float x, float y){
        this.x = x;
        this.y = y;
        return this;
    }

    public Point2Float nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (float) Math.sqrt(l);
        x *= l;
        y *= l;
        return this;
    }

    public Point2Float normalize() {
        return nor();
    }

    public Point2Float add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Point2Float scl(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public Point2Float scale(float scalar) {
        return scl(scalar);
    }

    public Point2Float scl(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Point2Float scale(float x, float y) {
        return scl(x, y);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point2Float mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point2Float multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point2Float mul(float x, float y) {
        return scl(x, y);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point2Float multiply(float x, float y) {
        return scl(x, y);
    }

    public Point2Float addProduct(Point2<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point2Float mulAdd(Point2<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        return this;
    }

    public Point2Float mulAdd(Point2<?> vec, Point2<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        return this;
    }

    public Point2Float addProduct(Point2<?> vec, Point2<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    @Override
    public Point2Float limit(float limit) {
        super.limit(limit);
        return this;
    }

    @Override
    public Point2Float limit2(float limit2) {
        super.limit2(limit2);
        return this;
    }

    @Override
    public Point2Float clamp(float min, float max) {
        super.clamp(min, max);
        return this;
    }

    @Override
    public Point2Float setLength(float len) {
        super.setLength(len);
        return this;
    }

    @Override
    public Point2Float setLength2(float len2) {
        super.setLength2(len2);
        return this;
    }

    @Override
    public Point2Float mul(Matrix3 mat) {
        super.mul(mat);
        return this;
    }

    @Override
    public Point2Float setAngleDeg(float degrees) {
        super.setAngleDeg(degrees);
        return this;
    }

    @Override
    public Point2Float setAngleRad(float radians) {
        super.setAngleRad(radians);
        return this;
    }

    @Override
    public Point2Float rotateDeg(float degrees) {
        super.rotateDeg(degrees);
        return this;
    }

    @Override
    public Point2Float rotateRad(float radians) {
        super.rotateRad(radians);
        return this;
    }

    @Override
    public Point2Float rotateAroundDeg(Vector2 reference, float degrees) {
        super.rotateAroundDeg(reference, degrees);
        return this;
    }

    @Override
    public Point2Float rotateAroundRad(Vector2 reference, float radians) {
        super.rotateAroundRad(reference, radians);
        return this;
    }

    @Override
    public Point2Float rotate90(int dir) {
        super.rotate90(dir);
        return this;
    }

    @Override
    public Point2Float lerp(Vector2 target, float alpha) {
        super.lerp(target, alpha);
        return this;
    }

    @Override
    public Point2Float interpolate(Vector2 target, float alpha, Interpolation interpolation) {
        super.interpolate(target, alpha, interpolation);
        return this;
    }

    @Override
    public Point2Float setToRandomDirection() {
        return setToRandomDirection(MathUtils.random);
    }

    public Point2Float setToRandomDirection(Random random) {
        float theta = random.nextFloat() * MathUtils.PI2;
        return this.set(MathUtils.cos(theta), MathUtils.sin(theta));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point2Float clampEach (float min, float max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        return this;
    }

    /**
     * Sets each component so it only has a fractional value, by subtracting the floor from each component.
     * This produces a non-negative float for each component, between 0.0 inclusive and 1.0 exclusive, unless a
     * component is outside the safe range for {@link MathUtils#floor(float)} (-16384.0 at the lowest).
     * <pre>
     * The result of fract() for a component with a value of  1.25 will be 0.25 .
     * The result of fract() for a component with a value of -1.25 will be 0.75 .
     * </pre>
     *
     * @return this, after modifications
     */
    public Point2Float fract () {
        x -= MathUtils.floor(x);
        y -= MathUtils.floor(y);
        return this;
    }

    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    public float get (int index) {
        if (index == 1)
            return y;
        return x;
    }

    /**
     * Sets the component at the specified index to the specified value.
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return this, for chaining
     */
    @SuppressWarnings({"DefaultNotLastCaseInSwitch", "SwitchStatementWithTooFewBranches"})
    public Point2Float setAt(int index, float value){
        switch (index){
            default: x = value;
            case 1 : y = value;
        }
        return this;
    }

    @Override
    public int hashCode() {
        final int h = BitConversion.floatToIntBits(x) + 53 * BitConversion.floatToIntBits(y);
        return h ^ h >>> 16;
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
    }

    /** Converts this {@code Point2Float} to a string in the format {@code (x,y)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + x + "," + y + ")";
    }

    /** Sets this {@code Point2Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this vector for chaining */
    public Point2Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        if (s0 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Float.parseFloat(s.substring(1, s0));
            float y = Float.parseFloat(s.substring(s0 + 1, s.length() - 1));
            return this.set(x, y);
        }
        throw new IllegalArgumentException("Not a valid format for a Point2Float: " + s);
    }
}
