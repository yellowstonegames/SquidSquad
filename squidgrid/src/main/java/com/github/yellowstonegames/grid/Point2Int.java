/*
 * Copyright (c) 2026; see AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.crux.Point2;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.PrimitiveCollection;

/**
 * A mutable 2D point with int components implementing {@link Point2}, {@link PrimitiveCollection.OfInt},
 * and {@link PointNInt}.
 * <br>
 * Contrast with {@link Coord}, which is immutable, stores values in a pool, and only allows {@code short} components.
 * Coord, being immutable, works better as a key in hashed maps and sets.
 */
public class Point2Int implements Point2<Point2Int>, PointNInt<Point2Int, Point2<?>>, PrimitiveCollection.OfInt {

    public int x;
    public int y;

    public Point2Int() {
    }

    public Point2Int(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point2Int(Point2Int p) {
        this(p.x, p.y);
    }

    public Point2Int(Point2<?> p) {
        this(p.xi(), p.yi());
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
    public Point2Int cpy() {
        return new Point2Int(this);
    }

    public Point2Int copy() {
        return cpy();
    }

    @Override
    public float len2() {
        return x * x + y * y;
    }

    @Override
    public Point2Int set(Point2Int point) {
        x = point.x;
        y = point.y;
        return this;
    }
    public Point2Int set(Point2<?> point) {
        x = point.xi();
        y = point.yi();
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point2Int plus(float scalar) {
        x += scalar;
        y += scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point2Int minus(float scalar) {
        x -= scalar;
        y -= scalar;
        return this;
    }

    @SuppressWarnings("lossy-conversions")
    @Override
    public Point2Int times(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    @Override
    public Point2Int sub(Point2Int point) {
        x -= point.x;
        y -= point.y;
        return this;
    }
    public Point2Int sub(Point2<?> point) {
        x -= point.xi();
        y -= point.yi();
        return this;
    }
    public Point2Int subtract(Point2<?> point) {
        return sub(point);
    }

    @Override
    public Point2Int add(Point2Int point) {
        x += point.x;
        y += point.y;
        return this;
    }
    public Point2Int add(Point2<?> point) {
        x += point.xi();
        y += point.yi();
        return this;
    }

    @Override
    public Point2Int scl(Point2Int point) {
        x *= point.x;
        y *= point.y;
        return this;
    }
    public Point2Int scl(Point2<?> point) {
        x *= point.xi();
        y *= point.yi();
        return this;
    }

    public Point2Int scl(int scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public Point2Int scale(Point2<?> point) {
        return scl(point);
    }
    public Point2Int mul(Point2Int point) {
        return scl(point);
    }
    public Point2Int mul(Point2<?> point) {
        return scl(point);
    }
    public Point2Int mul(int scalar) {
        return scl(scalar);
    }
    public Point2Int multiply(int scalar) {
        return scl(scalar);
    }
    public Point2Int multiply(Point2<?> point) {
        return scl(point);
    }

    @Override
    public float dst2(Point2Int point) {
        return (point.x - x) * (point.x - x) + (point.y - y) * (point.y - y);
    }
    public int dst2(Point2<?> point) {
        return (point.xi() - x) * (point.xi() - x) + (point.yi() - y) * (point.yi() - y);
    }
    public float distance(Point2<?> point) {
        return (int) Math.sqrt(dst2(point));
    }
    public int distanceSquared(Point2<?> point) {
        return dst2(point);
    }
    @Override
    public Point2Int setZero() {
        x = y = 0;
        return this;
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Point2Int x(float next) {
        x = (int)next;
        return this;
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Point2Int xi(int next) {
        x = next;
        return this;
    }

    @Override
    public Point2Int seti(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Point2Int y(float next) {
        y = (int)next;
        return this;
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Point2Int yi(int next) {
        y = next;
        return this;
    }

    @Override
    public Point2Int set(float nextX, float nextY) {
        x = (int) nextX;
        y = (int) nextY;
        return this;
    }

    public Point2Int set(int x, int y){
        this.x = x;
        this.y = y;
        return this;
    }

    public Point2Int nor() {
        float l = len2();
        if(l == 0 || l == 1) return this;
        l = 1f / (int) Math.sqrt(l);
        x *= l;
        y *= l;
        return this;
    }

    public Point2Int normalize() {
        return nor();
    }

    public Point2Int add(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Point2Int sub(int x, int y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public Point2Int add(Direction direction) {
        this.x += direction.deltaX;
        this.y += direction.deltaY;
        return this;
    }

    public Point2Int subtract(Direction direction) {
        this.x -= direction.deltaX;
        this.y -= direction.deltaY;
        return this;
    }

    public Point2Int subtract(int x, int y) {
        return sub(x, y);
    }
    public Point2Int scl(float scalar) {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public Point2Int scale(float scalar) {
        return scl(scalar);
    }

    public Point2Int scl(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Point2Int scale(float x, float y) {
        return scl(x, y);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point2Int mul(float scalar) {
        return scl(scalar);
    }

    /**
     * Multiplies each component of this by the given scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float)}.
     * @param scalar a float that will be multiplied with each component
     * @return this, for chaining
     */
    public Point2Int multiply(float scalar) {
        return scl(scalar);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point2Int mul(float x, float y) {
        return scl(x, y);
    }
    /**
     * Multiplies each component of this by the corresponding scalar, in-place, and returns this.
     * This is an alias for {@link #scl(float, float)}.
     * @param x a float that will be multiplied with x
     * @param y a float that will be multiplied with y
     * @return this, for chaining
     */
    public Point2Int multiply(float x, float y) {
        return scl(x, y);
    }

    public Point2Int div(Point2Int point) {
        x /= point.x;
        y /= point.y;
        return this;
    }

    public Point2Int divide(float scalar) {
        x /= scalar;
        y /= scalar;
        return this;
    }

    public Point2Int addProduct(Point2<?> vec, float scalar) {
        return mulAdd(vec, scalar);
    }
    public Point2Int mulAdd(Point2<?> vec, float scalar) {
        x += vec.x() * scalar;
        y += vec.y() * scalar;
        return this;
    }

    public Point2Int mulAdd(Point2<?> vec, Point2<?> mulVec) {
        x += vec.x() * mulVec.x();
        y += vec.y() * mulVec.y();
        return this;
    }

    public Point2Int addProduct(Point2<?> vec, Point2<?> mulVec) {
        return mulAdd(vec, mulVec);
    }

    public Point2Int limit(float limit) {
        return limit2(limit * limit);
    }

    public Point2Int limit2(float limit2) {
        float len2 = len2();
        if (len2 > limit2) {
            return scl((float)Math.sqrt(limit2 / len2));
        }
        return this;
    }
    public Point2Int limitSquared(float limit2) {
        return limit2(limit2);
    }

    public Point2Int clampLength(float min, float max) {
        final float len2 = len2();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return scl((float)Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return scl((float)Math.sqrt(min2 / len2));
        return this;
    }

    public Point2Int setLength(float len) {
        return setLength2(len * len);
    }

    public Point2Int setLength2(float len2) {
        float oldLen2 = len2();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : scl((float)Math.sqrt(len2 / oldLen2));
    }

    /**
     * Sets a Point2Int to the result of rotating the point {@code (rotatingX, rotatingY)} around the point
     * {@code (originX, originY)} by the given rotation in {@code radians}. This rounds the x and y
     * of {@code modifying} to the nearest integer values.
     *
     * @param modifying a Point2Int that will be modified in-place to hold the result of the calculation
     * @param rotatingX the x-coordinate of the point being rotated
     * @param rotatingY the y-coordinate of the point being rotated
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param radians the counterclockwise rotation, in radians, to move the rotating point around the origin
     * @return {@code modifying}, after changes
     */
    public static Point2Int rotateAroundRadians(Point2Int modifying, float rotatingX, float rotatingY, float originX, float originY, float radians){
        rotatingX -= originX;
        rotatingY -= originY;
        float c = TrigTools.cos(radians);
        float s = TrigTools.sin(radians);
        return modifying.set(MathTools.round(originX + rotatingX * c - rotatingY * s), MathTools.round(originY + rotatingX * s + rotatingY * c));
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code (originX, originY)} by the given rotation in {@code radians}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param radians the counterclockwise rotation, in radians, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundRadians(float originX, float originY, float radians){
        return rotateAroundRadians(this, this.x, this.y, originX, originY, radians);
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code origin} by the given rotation in {@code radians}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param origin the point to rotate around
     * @param radians the counterclockwise rotation, in radians, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundRadians(Point2<?> origin, float radians){
        return rotateAroundRadians(this, this.x, this.y, origin.x(), origin.y(), radians);
    }

    /**
     * Sets a Point2Int to the result of rotating the point {@code (rotatingX, rotatingY)} around the point
     * {@code (originX, originY)} by the given rotation in {@code degrees}. This rounds the x and y
     * of {@code modifying} to the nearest integer values.
     *
     * @param modifying a Point2Int that will be modified in-place to hold the result of the calculation
     * @param rotatingX the x-coordinate of the point being rotated
     * @param rotatingY the y-coordinate of the point being rotated
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param degrees the counterclockwise rotation, in degrees, to move the rotating point around the origin
     * @return {@code modifying}, after changes
     */
    public static Point2Int rotateAroundDegrees(Point2Int modifying, float rotatingX, float rotatingY, float originX, float originY, float degrees){
        rotatingX -= originX;
        rotatingY -= originY;
        float c = TrigTools.cosDeg(degrees);
        float s = TrigTools.sinDeg(degrees);
        return modifying.set(MathTools.round(originX + rotatingX * c - rotatingY * s), MathTools.round(originY + rotatingX * s + rotatingY * c));
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code (originX, originY)} by the given rotation in {@code degrees}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param degrees the counterclockwise rotation, in degrees, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundDegrees(float originX, float originY, float degrees){
        return rotateAroundDegrees(this, this.x, this.y, originX, originY, degrees);
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code origin} by the given rotation in {@code degrees}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param origin the point to rotate around
     * @param degrees the counterclockwise rotation, in degrees, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundDegrees(Point2<?> origin, float degrees){
        return rotateAroundDegrees(this, this.x, this.y, origin.x(), origin.y(), degrees);
    }

    /**
     * Sets a Point2Int to the result of rotating the point {@code (rotatingX, rotatingY)} around the point
     * {@code (originX, originY)} by the given rotation in {@code turns}. This rounds the x and y
     * of {@code modifying} to the nearest integer values.
     *
     * @param modifying a Point2Int that will be modified in-place to hold the result of the calculation
     * @param rotatingX the x-coordinate of the point being rotated
     * @param rotatingY the y-coordinate of the point being rotated
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param turns the counterclockwise rotation, in turns, to move the rotating point around the origin
     * @return {@code modifying}, after changes
     */
    public static Point2Int rotateAroundTurns(Point2Int modifying, float rotatingX, float rotatingY, float originX, float originY, float turns){
        rotatingX -= originX;
        rotatingY -= originY;
        float c = TrigTools.cosTurns(turns);
        float s = TrigTools.sinTurns(turns);
        return modifying.set(MathTools.round(originX + rotatingX * c - rotatingY * s), MathTools.round(originY + rotatingX * s + rotatingY * c));
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code (originX, originY)} by the given rotation in {@code turns}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param originX the x-coordinate of the point to rotate around
     * @param originY the y-coordinate of the point to rotate around
     * @param turns the counterclockwise rotation, in turns, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundTurns(float originX, float originY, float turns){
        return rotateAroundTurns(this, this.x, this.y, originX, originY, turns);
    }

    /**
     * Sets this Point2Int to its position after rotating this around the point
     * {@code origin} by the given rotation in {@code turns}. This rounds the x and y
     * of this Point2Int to the nearest integer values.
     * @param origin the point to rotate around
     * @param turns the counterclockwise rotation, in turns, to move this Point2Int around the origin
     * @return this Point2Int, after modification
     */
    public Point2Int rotateAroundTurns(Point2<?> origin, float turns){
        return rotateAroundTurns(this, this.x, this.y, origin.x(), origin.y(), turns);
    }

    public float dot(Point2<?> other) {
        return x * other.x() + y * other.y();
    }

    public static float dot(Point2<?> a, Point2<?> b) {
        return a.x() * b.x() + a.y() * b.y();
    }

    public Point2Int lerp(Point2<?> target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (int)((x * invAlpha) + (target.x() * alpha));
        this.y = (int)((y * invAlpha) + (target.y() * alpha));
        return this;
    }

    public Point2Int interpolate(Point2<?> target, float alpha, Interpolations.Interpolator interpolation) {
        return lerp(target, interpolation.apply(alpha));
    }

    /**
     * For each component, this clamps it between min and max, inclusive.
     * @param min the minimum value allowable for any component, inclusive
     * @param max the maximum value allowable for any component, inclusive
     * @return this, after modifications
     */
    public Point2Int clampEach (int min, int max) {
        x = Math.min(Math.max(x, min), max);
        y = Math.min(Math.max(y, min), max);
        return this;
    }

    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * @param index which component to get, in order
     * @return the component
     */
    @Override
    public int get (int index) {
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
    @Override
    public Point2Int setAt(int index, int value){
        switch (index){
            default: x = value; break;
            case 1 : y = value; break;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Point2Int)) return false;

        Point2Int point2Int = (Point2Int) o;
        return x == point2Int.x && y == point2Int.y;
    }

    @Override
    public int hashCode() {
        return x * 0x1827F5 ^ y * 0x123C21;
    }

    /** Converts this {@code Point2Int} to a string in the format {@code (x,y)}.
     * @return a string representation of this object. */
    @Override
    public String toString () {
        return "(" + Base.BASE10.signed(x) + ","
                + Base.BASE10.signed(y) + ")";
    }

    /** Sets this {@code Point2Int} to the value represented by the specified string according to the format of {@link #toString()}.
     * @param s the string.
     * @return this point for chaining */
    public Point2Int fromString (String s) {
        int s0 = s.indexOf(',', 1);
        if (s0 != -1 && s.charAt(0) == '(' && s.charAt(s.length() - 1) == ')') {
            int x = Base.BASE10.readInt(s, 1, s0);
            int y = Base.BASE10.readInt(s, s0 + 1, s.length() - 1);
            return this.set(x, y);
        }

        throw new IllegalArgumentException("Not a valid format for a Point2Int: " + s);
    }

    /**
     * Not supported; this collection is fixed-size.
     * @param c ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(int c) {
        throw new UnsupportedOperationException("Point2Int is fixed-size.");
    }

    /**
     * Not supported; this collection is fixed-size.
     * @param c ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean remove(int c) {
        throw new UnsupportedOperationException("Point2Int is fixed-size.");
    }

    /**
     * Not supported; this collection is fixed-size.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Point2Int is fixed-size.");
    }

    /**
     * Compares {@code c} with the components of this collection using {@code ==}; if any are considered equal, this
     * returns true.
     * @param c an int to be compared using {@code ==}
     * @return true if any component of this collection is equal to {@code c} via {@code ==} comparison
     */
    @Override
    public boolean contains(int c) {
        return (x == c || y == c);
    }

    @Override
    public int size() {
        return 2;
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
