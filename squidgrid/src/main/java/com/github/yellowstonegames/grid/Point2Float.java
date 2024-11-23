package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Random;

/**
 * A mutable 2D point with float components implementing {@link Point2}, {@link PrimitiveCollection.OfFloat},
 * {@link PointNFloat}, and {@link Externalizable}.
 */
public class Point2Float implements Point2<Point2Float>, PointNFloat<Point2Float, Point2<?>>, PrimitiveCollection.OfFloat, Externalizable {

    public float x;
    public float y;

    public Point2Float() {
    }

    public Point2Float(float x, float y) {
        this.x = x;
        this.y = y;
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
    public float len2() {
        return x * x + y * y;
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
    public Point2Float mul(Point2<?> point) {
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

    public Point2Float sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    public Point2Float subtract(float x, float y) {
        return sub(x, y);
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

    public Point2Float limit(float limit) {
        return limit2(limit * limit);
    }

    public Point2Float limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point2Float limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point2Float clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point2Float setLength(float len) {
        return setLength2(len * len);
    }

    public Point2Float setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    public Point2Float setAngleRad (float radians) {
        set(len(), 0f);
        return rotateRad(radians);
    }

    public Point2Float setAngleDeg(float degrees) {
        set(len(), 0f);
        return rotateDeg(degrees);
    }

    public Point2Float setAngleTurns(float turns) {
        set(len(), 0f);
        return rotateTurns(turns);
    }

    public Point2Float rotateRad(float radians) {
        float cos = TrigTools.cos(radians);
        float sin = TrigTools.sin(radians);

        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;

        this.x = newX;
        this.y = newY;

        return this;
    }

    public Point2Float rotateDeg(float degrees) {
        float cos = TrigTools.cosDeg(degrees);
        float sin = TrigTools.sinDeg(degrees);

        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;

        this.x = newX;
        this.y = newY;

        return this;
    }

    public Point2Float rotateTurns(float turns) {
        float cos = TrigTools.cosTurns(turns);
        float sin = TrigTools.sinTurns(turns);

        float newX = this.x * cos - this.y * sin;
        float newY = this.x * sin + this.y * cos;

        this.x = newX;
        this.y = newY;

        return this;
    }

    public Point2Float rotateAroundRad(Point2<?> reference, float radians) {
        return this.sub(reference).rotateRad(radians).add(reference);
    }

    public Point2Float rotateAroundDeg(Point2<?> reference, float degrees) {
        return this.sub(reference).rotateDeg(degrees).add(reference);
    }

    public Point2Float rotateAroundTurns(Point2<?> reference, float turns) {
        return this.sub(reference).rotateTurns(turns).add(reference);
    }

    public Point2Float rotate90(int sign) {
        float x = this.x;
        if (sign >= 0) {
            this.x = -y;
            y = x;
        } else {
            this.x = y;
            y = -x;
        }
        return this;
    }

    public float dot(Point2<?> other) {
        return x * other.x() + y * other.y();
    }

    public static float dot(Point2<?> a, Point2<?> b) {
        return a.x() * b.x() + a.y() * b.y();
    }

    public Point2Float lerp(Point2<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x() * alpha);
        this.y = (y * invAlpha) + (target.y() * alpha);
        return this;
    }

    public Point2Float interpolate(Point2<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    public Point2Float slerpGeometric(Point2Float target, float alpha) {
        return PointNFloat.slerpGeometric(this, target, alpha, this);
    }

    public Point2Float setToRandomDirection(Random random) {
        int index = random.nextInt() & TrigTools.TABLE_MASK;
        return this.set(TrigTools.COS_TABLE[index], TrigTools.SIN_TABLE[index]);
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
     * component is outside the safe range for {@link MathTools#floor(float)} (-16384.0 at the lowest).
     * <pre>
     * The result of fract() for a component with a value of  1.25 will be 0.25 .
     * The result of fract() for a component with a value of -1.25 will be 0.75 .
     * </pre>
     *
     * @return this, after modifications
     */
    public Point2Float fract () {
        x -= MathTools.floor(x);
        y -= MathTools.floor(y);
        return this;
    }
    public Point2Float fractional () {
        return fract();
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
        return "(" + Base.BASE10.general(x) + ","
                + Base.BASE10.general(y) + ")";
    }

    /** Sets this {@code Point2Float} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point2Float fromString (String s) {
        int s0 = s.indexOf(',', 1);
        if (s0 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            float x = Base.BASE10.readFloat(s, 1, s0);
            float y = Base.BASE10.readFloat(s, s0 + 1, s.length() - 1);
            return this.set(x, y);
        }

        throw new IllegalArgumentException("Not a valid format for a Point2Float: " + s);
    }

    @Override
    public boolean add(float c) {
        throw new UnsupportedOperationException("Point2Float is fixed-size.");
    }

    @Override
    public boolean remove(float c) {
        throw new UnsupportedOperationException("Point2Float is fixed-size.");
    }

    @Override
    public boolean contains(float c) {
        return (x == c || y == c);
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point2Float is fixed-size.");
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
