/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;

import com.github.tommyettinger.ds.PrimitiveCollection;
import com.github.tommyettinger.ds.support.util.IntIterator;
import com.github.tommyettinger.ds.support.util.PartialParser;

/**
 * A 2D coordinate with (constant) x and y fields. Coord objects are immutable; a single pool of Coord values, with
 * x and y each ranging from -3 to 255, is shared by all users of Coord (the upper limit of 255 can be increased as
 * needed). This pool helps reduce pressure on the garbage collector when many Coord values would have been created for
 * some purpose and quickly discarded; instead of creating a new Coord with a constructor, you use the static method
 * {@link #get(int, int)}, which retrieves an already-existing Coord from the pool if possible, and always returns a
 * usable Coord. This class has various instance methods, but none of them modify the current Coord; if they change
 * anything, they do so by returning another Coord with different x and y values via {@link #get(int, int)}.
 * <br>
 * More on the Coord pool used by this class: Coords can't always be retrieved from the pool; Coord.get() constructs a
 * new Coord if one of x or y is unusually large (greater than 255) or too negative (below -3). The upper limit of 255
 * is not a hard rule; you can increase the limit on the pool by calling {@link #expandPoolTo(int, int)} or
 * {@link #expandPool(int, int)}, which cause more memory to be spent initially on storing Coords but can save memory
 * or ease GC pressure over the long term by preventing duplicate Coords from being created many times. The pool can
 * never shrink because allowing that would cause completely unpredictable results if existing Coords were in use, or
 * could easily cause crashes on Android after resuming an application that had previously shrunken the pool due to
 * platform quirks. Long story short, you should only expand the pool size when your game needs a larger set of 2D
 * points it will commonly use, and in many cases you shouldn't need to change it at all.
 * <br>
 * The x and y fields are internally {@code short}s, since most usage of Coord should use pooled Coord values, and there
 * isn't much of a possible way to store more than 32767x32767 (where 32767 is {@link Short#MAX_VALUE}) Coords in any
 * Java application due to limits on array size. There is also an immutable, precalculated result for
 * {@link #hashCode()} to return without needing to recalculate anything. The precalculated hash won't overlap with the
 * hash for any other Coord as long as all Coord values have x and y each in the range from {@link Short#MIN_VALUE} to
 * {@link Short#MAX_VALUE}. Larger ranges than a 8192x8192 grid of Coord items tend to exhaust all of Java's heap
 * (using only Coord items), so supporting larger sizes isn't at all a priority.
 * <br>
 * This implements {@link Point2}, allowing it to interoperate with some other libraries that also use the interfaces
 * from the library <a href="https://github.com/tommyettinger/crux">crux</a>. A side effect of this is that using the
 * field {@link #x} gets a {@code short} value, while calling {@link #x()} uses the interface and gets a {@code float}.
 * If this causes problems in, for instance, Kotlin code, you could create an extension method that gets the type you
 * want and has the name you want.
 */
public final class Coord implements Point2<Coord>, PointNInt<Coord, Point2<?>>, PrimitiveCollection.OfInt{
    /**
     * The x-coordinate.
     */
    public final short x;

    /**
     * The y-coordinate (the ordinate)
     */
    public final short y;

    /**
     * Also accessible via {@link #hashCode()}, this is a precalculated hashCode() result. It is only assigned when a
     * Coord is first created. It uses a method of computing a hash value that won't collide in full (over all 32 bits)
     * for any possible Coord values. Even using a smaller portion of the hash, if a hash table can fit 8192x8192 Coord
     * values with load factor 0.5f, an appropriate mask will still not cause collisions here. This does randomize the
     * upper bits of the hash somewhat, but the lower bits (where collisions are most important, because masks typically
     * keep only some range of the lower bits) are more orderly.
     * <br>
     * The actual method used to assign this involves passing x and y to the Rosenberg-Strong pairing function, then
     * multiplying that by 0x9E3779B9, or -1640531527 in decimal. The Rosenberg-Strong pairing function is discussed
     * more <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">here (a good introduction)</a> and
     * <a href="https://arxiv.org/abs/1706.04129">here (a more technical paper)</a>. 0x9E3779B9 is used because it is
     * (2 to the 32) divided by the golden ratio, and because of properties of the golden ratio, 0x9E3779B9 helps ensure
     * "sub-random" bit patterns in its multiples. Because 0x9E3779B9 is odd, if every possible hashCode() is taken and
     * multiplied by 0x9E3779B9, the full set of (2 to the 32) numbers will just be rearranged; nothing will collide.
     *
     * @see #signedRosenbergStrongMultiplyHashCode(int, int)
     */
    public transient final int hash;

    private Coord() {
        this(0, 0);
    }

    private Coord(final int x, final int y) {
        this.x = (short)x;
        this.y = (short)y;

        // Calculates a hash that won't overlap until very, very many Coords have been produced.
        // the signs for x and y; each is either -1 or 0
        int xs = this.x >> 31, ys = this.y >> 31;
        // makes mx equivalent to -1 ^ this.x if this.x is negative; this means mx is never negative
        int mx = this.x ^ xs;
        // same for my; it is also never negative
        int my = this.y ^ ys;
        // Math.max can be branchless on modern JVMs, which may help if the Coord pool is expanded a lot or often.
        final int max = Math.max(mx, my);
        // imul uses * on most platforms, but instead uses the JS Math.imul() function on GWT
        this.hash = BitConversion.imul(
                // Rosenberg-Strong pairing function; produces larger values in a "ripple" moving away from the origin
                (max * max + max + mx - my)
                        // XOR with every odd-index bit of xs and every even-index bit of ys
                        // this makes negative x, negative y, positive both, and negative both all get different bits XORed or not
                        ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555)
                // use imul() to multiply by a golden-ratio-based number to randomize upper bits
                , 0x9E3779B9);
    }

    public static Coord get(final int x, final int y) {
        if (x >= -3 && y >= -3 && x < POOL_WIDTH && y < POOL_HEIGHT)
            return POOL[ORIGIN + y * PADDED_POOL_WIDTH + x];
        else return new Coord(x, y);
    }

    /**
     * Gets the angle in radians to go between two Coords.
     * When only x is different and {@code to.x} is greater than {@code from.x}, this returns 0.
     * When only y is different and {@code to.y} is greater than {@code from.y}, this returns {@link TrigTools#HALF_PI}.
     * When only x is different and {@code to.x} is less than {@code from.x}, this returns {@link TrigTools#PI}.
     * When only y is different and {@code to.y} is less than {@code from.y}, this returns negative {@link TrigTools#HALF_PI}.
     * In cases between these, the angle is between those values, except that values change from positive
     * {@link TrigTools#PI} to negative {@link TrigTools#PI} as the angle crosses the y-axis. This can often
     * return a negative angle. Keep in mind, "up" depends on how your code orients the y-axis, and SquidSquad generally
     * defaults to positive y going toward the top of the screen, like in most textbook geometry.
     *
     * @param from the starting Coord to measure from
     * @param to   the ending Coord to measure to
     * @return the angle in counterclockwise radians from {@code from} to {@code to}; 0 is to the right
     */
    public static float radians(final Coord from, final Coord to) {
        return TrigTools.atan2(to.y - from.y, to.x - from.x);
    }

    /**
     * Gets the angle in degrees to go between two Coords.
     * When only x is different and {@code to.x} is greater than {@code from.x}, this returns 0.
     * When only y is different and {@code to.y} is greater than {@code from.y}, this returns 90.
     * When only x is different and {@code to.x} is less than {@code from.x}, this returns 180.
     * When only y is different and {@code to.y} is less than {@code from.y}, this returns 270.
     * In cases between these, the angle is between those values; it cannot be 360, but it can be very close. This never
     * returns a negative angle. Keep in mind, "up" depends on how your code orients the y-axis, and SquidSquad generally
     * defaults to positive y going toward the top of the screen, like in most textbook geometry.
     *
     * @param from the starting Coord to measure from
     * @param to   the ending Coord to measure to
     * @return the angle in counterclockwise degrees from {@code from} to {@code to}; 0 is to the right
     */
    public static float degrees(final Coord from, final Coord to) {
        return TrigTools.atan2Deg360(to.y - from.y, to.x - from.x);
    }

    /**
     * Gets the angle in turns to go between two Coords.
     * When only x is different and {@code to.x} is greater than {@code from.x}, this returns 0f.
     * When only y is different and {@code to.y} is greater than {@code from.y}, this returns 0.25f.
     * When only x is different and {@code to.x} is less than {@code from.x}, this returns 0.5f.
     * When only y is different and {@code to.y} is less than {@code from.y}, this returns 0.75f.
     * In cases between these, the angle is between those values; it cannot be 1f, but it can be very close. This never
     * returns a negative angle. Keep in mind, "up" depends on how your code orients the y-axis, and SquidSquad generally
     * defaults to positive y going toward the top of the screen, like in most textbook geometry.
     *
     * @param from the starting Coord to measure from
     * @param to   the ending Coord to measure to
     * @return the angle in counterclockwise turns from {@code from} to {@code to}; 0 is to the right
     */
    public static float turns(final Coord from, final Coord to) {
        return TrigTools.atan2Turns(to.y - from.y, to.x - from.x);
    }

    /**
     * Provided for compatibility with earlier code that used the AWT Point API.
     *
     * @return this Coord, without changes
     */
    public Coord getLocation() {
        return this;
    }

    /**
     * Takes this Coord, adds x to its x and y to its y, and returns the Coord at that position.
     *
     * @param x the amount of x distance to move
     * @param y the amount of y distance to move
     * @return a Coord (usually cached and not a new instance) that has been moved the specified distance
     */
    public Coord translate(final int x, final int y) {
        return get(this.x + x, this.y + y);
    }

    /**
     * Takes this Coord, adds x to its x and y to its y, limiting x from 0 to width and limiting y from 0 to height,
     * and returns the Coord at that position.
     *
     * @param x      the amount of x distance to move
     * @param y      the amount of y distance to move
     * @param width  one higher than the maximum x value this can use; typically the length of an array
     * @param height one higher than the maximum y value this can use; typically the length of an array
     * @return a Coord (usually cached and not a new instance) that has been moved the specified distance
     */
    public Coord translateCapped(final int x, final int y, final int width, final int height) {
        return get(Math.min(Math.max(0, this.x + x), width - 1), Math.min(Math.max(0, this.y + y), height - 1));
    }

    @Override
    public boolean floatingPoint() {
        return false;
    }

    /**
     * Gets the component at the specified index.
     * Kotlin-compatible using square-bracket indexing.
     * Getting index 1 gets y; anything else gets x.
     *
     * @param index which component to get, in order
     * @return the component
     */
    @Override
    public int get(int index) {
        return index == 1 ? y : x;
    }

    /**
     * Sets the component at the specified index to the specified value, obtaining a Coord
     * that has the requested value from the pool if possible.
     * Setting index 1 sets y; anything else sets x.
     * This can sometimes return the same Coord reference it was called upon (when the changed
     * component already had the requested value), but it usually returns a different one.
     *
     * @param index which component to set, in order
     * @param value the value to assign at index
     * @return a Coord retrieved from the pool with the given value at the given index
     */
    @Override
    public Coord setAt(int index, int value) {
        return index == 1 ? Coord.get(x, value) : Coord.get(value, y);
    }

    @Override
    public boolean mutable() {
        return false;
    }

    @Override
    public Coord cpy() {
        return this;
    }

    @Override
    public float len2() {
        return (x * x + y * y);
    }

    @Override
    public Coord set(Coord coord) {
        return coord;
    }

    @Override
    public Coord setZero() {
        return get(0, 0);
    }

    @Override
    public Coord sub(final Coord coord) {
        return subtract(coord);
    }

    /**
     * Separately combines the x and y positions of this Coord and other, producing a different Coord as their "sum."
     *
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + other.x; y = this.y + other.y}
     */
    public Coord add(final Coord other) {
        return get(x + other.x, y + other.y);
    }

    @Override
    public Coord scl(Coord coord) {
        return multiply(coord);
    }

    @Override
    public Coord div(Coord point) {
        return get(x / point.x, y / point.y);
    }

    @Override
    public float dst2(Coord coord) {
        return distanceSq(coord);
    }

    /**
     * Separately adds the x and y positions of this Coord to operand, producing a different Coord as their
     * "sum." This is called "plus" and not "add" because {@link PrimitiveCollection.OfInt#add(int)} was already used.
     *
     * @param operand a value to add each of x and y to
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + operand; y = this.y + operand}
     */
    public Coord plus(final int operand) {
        return get(x + operand, y + operand);
    }

    /**
     * Always throws an {@link UnsupportedOperationException} because Coord is fixed-size.
     * If you want to add {@code i} to both components, use {@link #plus(int)} or {@link #plus(float)} instead.
     * @param i ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(int i) {
        throw new UnsupportedOperationException("Coord is fixed-size. Did you want Coord.plus(int) instead?");
    }

    /**
     * Always throws an {@link UnsupportedOperationException} because Coord is fixed-size.
     * @param i ignored
     * @return never returns
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean remove(int i) {
        throw new UnsupportedOperationException("Coord is fixed-size.");
    }

    @Override
    public boolean contains(int i) {
        return x == i || y == i;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public IntIterator iterator() {
        return new PointNIntIterator(this);
    }

    @Override
    public void clear() {

    }

    /**
     * Separately adds the x and y positions of this Coord to operand, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "sum."
     *
     * @param operand a value to add each of x and y to
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x + operand; y = this.y +
     * operand}, with both x and y rounded accordingly
     */
    public Coord plus(final float operand) {
        return get(Math.round(x + operand), Math.round(y + operand));
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "difference."
     *
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y -
     * operand}, with both x and y rounded accordingly
     */
    public Coord minus(final float operand) {
        return get(Math.round(x - operand), Math.round(y - operand));
    }

    /**
     * Separately multiplies operand with the x and y positions of this Coord, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "product."
     *
     * @param operand a value to multiply with each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y *
     * operand}, with both x and y rounded accordingly
     */
    public Coord times(final float operand) {
        return get(Math.round(x * operand), Math.round(y * operand));
    }

    /**
     * Separately subtracts the x and y positions of other from this Coord, producing a different Coord as their
     * "difference."
     *
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - other.x; y = this.y - other.y}
     */
    public Coord subtract(final Coord other) {
        return get(x - other.x, y - other.y);
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, producing a different Coord as their
     * "difference."
     *
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y - operand}
     */
    public Coord subtract(final int operand) {
        return get(x - operand, y - operand);
    }

    /**
     * Separately subtracts operand from the x and y positions of this Coord, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "difference."
     *
     * @param operand a value to subtract from each of x and y
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x - operand; y = this.y -
     * operand}, with both x and y rounded accordingly
     */
    public Coord subtract(final float operand) {
        return get(Math.round(x - operand), Math.round(y - operand));
    }

    /**
     * Separately multiplies the x and y positions of other from this Coord, producing a different Coord as their
     * "product."
     *
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * other.x; y = this.y * other.y}
     */
    public Coord multiply(final Coord other) {
        return get(x * other.x, y * other.y);
    }

    /**
     * Separately multiplies the x and y positions of this Coord by operand, producing a different Coord as their
     * "product."
     *
     * @param operand a value to multiply each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y * operand}
     */
    public Coord multiply(final int operand) {
        return get(x * operand, y * operand);
    }

    /**
     * Separately multiplies the x and y positions of this Coord by operand, rounding to the nearest int for each of x
     * and y and producing a different Coord as their "product."
     *
     * @param operand a value to multiply each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x * operand; y = this.y *
     * operand}, with both x and y rounded accordingly
     */
    public Coord multiply(final float operand) {
        return get(Math.round(x * operand), Math.round(y * operand));
    }

    /**
     * Separately divides the x and y positions of this Coord by other, producing a different Coord as their
     * "quotient." If other has 0 for x or y, this will throw an exception, as dividing by 0 is expected to do.
     *
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / other.x; y = this.y / other.y}
     */
    public Coord divide(final Coord other) {
        return get(x / other.x, y / other.y);
    }

    /**
     * Separately divides the x and y positions of this Coord by operand, producing a different Coord as their
     * "quotient." If operand is 0, this will throw an exception, as dividing by 0 is expected to do.
     *
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y / operand}
     */
    public Coord divide(final int operand) {
        return get(x / operand, y / operand);
    }

    /**
     * Separately divides the x and y positions of this Coord by operand, truncating closer to 0 for non-integer x and
     * y and producing a different Coord as their "quotient." If operand is 0.0, expect strange results (infinity and
     * NaN are both possibilities).
     *
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y /
     * operand}, with both x and y rounded accordingly
     */
    public Coord divide(final float operand) {
        return get((int) (x / operand), (int) (y / operand));
    }

    /**
     * Separately divides the x and y positions of this Coord by operand, rounding to the nearest int for each of x and
     * y and producing a different Coord as their "quotient." If operand is 0.0, expect strange results (infinity and
     * NaN are both possibilities).
     *
     * @param operand a value to divide each of x and y by
     * @return a Coord (usually cached and not a new instance) with {@code x = this.x / operand; y = this.y /
     * operand}, with both x and y rounded accordingly
     */
    public Coord divideRounding(final float operand) {
        return get(Math.round(x / operand), Math.round(y / operand));
    }

    /**
     * Separately averages the x and y positions of this Coord with other, producing a different Coord as their
     * "midpoint."
     *
     * @param other another Coord
     * @return a Coord (usually cached and not a new instance) halfway between this and other, rounded nearest.
     */
    public Coord average(final Coord other) {
        return get(Math.round((x + other.x) / 2.0f), Math.round((y + other.y) / 2.0f));
    }

    /**
     * @param d A non-{@code null} direction.
     * @return The coordinate obtained by applying {@code d} on {@code this}.
     */
    public Coord translate(final Direction d) {
        return add(d.coord);
    }

    /**
     * @param i scale factor
     * @return {@code (x*i,y*i)}.
     */
    public Coord scale(final int i) {
        return Coord.get(x * i, y * i);
    }

    /**
     * @param i scale factor for x
     * @param j scale factor for y
     * @return {@code (x*i,y*j)}.
     */
    public Coord scale(final int i, final int j) {
        return Coord.get(x * i, y * j);
    }

    /**
     * Gets the distance from this Coord to the given x2,y2 Coord, as a float.
     *
     * @param x2 x of a different Coord
     * @param y2 y of a different Coord
     * @return Euclidean distance from this Coord to the other given Coord, as a float
     */
    public float distance(final float x2, final float y2) {
        return (float) Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }

    /**
     * Gets the distance from this Coord to the given Coord, as a float.
     *
     * @param co a different Coord
     * @return Euclidean distance from this Coord to the other given Coord, as a float
     */
    public float distance(final Coord co) {
        return (float) Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }

    /**
     * Gets the squared distance from this Coord to the given x2,y2 Coord, as a float.
     *
     * @param x2 x of a different Coord
     * @param y2 y of a different Coord
     * @return squared Euclidean distance from this Coord to the other given Coord, as a float
     */
    public float distanceSq(final float x2, final float y2) {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }

    /**
     * Gets the squared distance from this Coord to the given Coord, as a float.
     *
     * @param co a different Coord
     * @return squared Euclidean distance from this Coord to the other given Coord, as a float
     */
    public float distanceSq(final Coord co) {
        return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y);
    }

    /**
     * Gets the distance from this Coord to the given x2,y2 Coord, as a double.
     *
     * @param x2 x of a different Coord
     * @param y2 y of a different Coord
     * @return Euclidean distance from this Coord to the other given Coord, as a double
     */
    public double distanceD(final double x2, final double y2) {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }

    /**
     * Gets the distance from this Coord to the given Coord, as a double.
     *
     * @param co a different Coord
     * @return Euclidean distance from this Coord to the other given Coord, as a double
     */
    public double distanceD(final Coord co) {
        return Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }

    /**
     * Gets the squared distance from this Coord to the given x2,y2 Coord, as a double.
     *
     * @param x2 x of a different Coord
     * @param y2 y of a different Coord
     * @return squared Euclidean distance from this Coord to the other given Coord, as a double
     */
    public double distanceSqD(final double x2, final double y2) {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }

    /**
     * Gets the squared distance from this Coord to the given Coord, as a double.
     *
     * @param co a different Coord
     * @return squared Euclidean distance from this Coord to the other given Coord, as a double
     */
    public double distanceSqD(final Coord co) {
        return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y);
    }

    /**
     * Gets a Coord based off this instance but with odd values for x and/or y decreased to the nearest even number
     *
     * @return a Coord (probably from the pool) with even x and even y, changing (decrementing) only if they are odd
     */
    public Coord makeEven() {
        return get(x & -2, y & -2);
    }

    /**
     * Gets a Coord based off this instance but with even values for x and/or y increased to the nearest odd number.
     *
     * @return a Coord (probably from the pool) with odd x and odd y, changing (incrementing) only if they are even
     */
    public Coord makeOdd() {
        return get(x | 1, y | 1);
    }

    /**
     * @param c another Coord that could be adjacent; must not be null
     * @return Whether {@code this} is adjacent to {@code c}. Not that a cell is
     * not adjacent to itself with this method.
     */
    public boolean isAdjacent(final Coord c) {
        switch (Math.abs(x - c.x)) {
            case 0:
                return Math.abs(y - c.y) == 1;
            case 1:
                return y == c.y || Math.abs(y - c.y) == 1;
            default:
                return false;
        }
    }

    /**
     * Gets the {@link Direction} needed to get to {@code target} from this; typically this is more useful when target
     * and this are adjacent (by {@link #isAdjacent(Coord)}) since that should make it possible to go to target.
     * <br>
     * Internally, this delegates to {@link Direction#toGoTo(Coord, Coord)}, and some code may prefer using the method
     * in Direction instead of this one. Earlier versions of this code only worked for adjacent Coords, which seemed
     * like an unnecessary limitation since Direction's version worked for any arguments.
     *
     * @param target a non-null {@link Coord}
     * @return the direction to go from {@code this} to {@code target}
     */
    public Direction toGoTo(final Coord target) {
        return Direction.toGoTo(this, target);
    }

    /**
     * Returns true if x is between 0 (inclusive) and width (exclusive) and y is between 0 (inclusive) and height
     * (exclusive), false otherwise.
     *
     * @param width  the upper limit on x to check, exclusive
     * @param height the upper limit on y to check, exclusive
     * @return true if this Coord is within the limits of width and height and has non-negative x and y
     */
    public boolean isWithin(final int width, final int height) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /**
     * Returns true if x is between minX (inclusive) and maxX (exclusive) and y is between minY (inclusive) and maxY
     * (exclusive), false otherwise.
     *
     * @param minX the lower limit on x to check, inclusive
     * @param minY the lower limit on y to check, inclusive
     * @param maxX the upper limit on x to check, exclusive
     * @param maxY the upper limit on y to check, exclusive
     * @return true if this Coord is within the limits of the given parameters
     */
    public boolean isWithinRectangle(int minX, int minY, int maxX, int maxY) {
        return x >= minX && y >= minY && x < maxX && y < maxY;
    }

    public int getX() {
        return x;
    }

    public Coord setX(final int x) {
        return get(x, y);
    }

    public int getY() {
        return y;
    }

    public Coord setY(final int y) {
        return get(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * If x and y are valid {@code short} numbers, then this will return a unique {@code int} hash code for those two.
     * If either is not a valid short, this cannot be guaranteed to produce a unique result. If you compare the results
     * for two nearby x,y points, the upper bits of the hash codes this produces will be more random than the lower
     * bits. This helps avoid collisions in dense sets or maps of Coord.
     * <br>
     * The actual method this uses involves masking x and y to fit in 16-bit unsigned numbers, then passing them to the
     * Rosenberg-Strong pairing function. The Rosenberg-Strong pairing function is discussed
     * more <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">here (a good introduction)</a> and
     * <a href="https://arxiv.org/abs/1706.04129">here (a more technical paper)</a>. Unlike
     * {@link #signedRosenbergStrongMultiplyHashCode(int, int)}, this does not finish by multiplying by a constant. You
     * can always do so yourself, but you should use {@link BitConversion#imul(int, int)} if targeting GWT at all.
     * <br>
     * This is similar to the algorithm used to precalculate the hash returned by {@link #hashCode()}. Unlike most of
     * the other hashCode() variants here, this acts fine with negative inputs, and should still return random-enough
     * hashes when x or y isn't in the short range (just not guaranteed to be unique).
     * <br>
     * Calculating this is branchless if calculating {@link Math#max(int, int)} is branchless. This is true on modern
     * desktop JVMs with sufficient optimization, and may be true on other platforms as well.
     *
     * @param x should usually be in the range for a valid short (from {@link Short#MIN_VALUE} to {@link Short#MAX_VALUE})
     * @param y should usually be in the range for a valid short (from {@link Short#MIN_VALUE} to {@link Short#MAX_VALUE})
     * @return an int hash code that will be unique for any combination of short x and short y
     */
    public static int signedRosenbergStrongHashCode(int x, int y) {
        // Calculates a hash that won't overlap until Coords reach 32768 or higher in x or y.
        // (Or if they reach -32769 or lower in x or y.)

        // Masks x and y to the (non-negative) 16-bit range.
        // This is synonymous to casting x and y each to char.
        x &= 0xFFFF;
        y &= 0xFFFF;
        // Math.max can be branchless on modern JVMs, which may speed this method up a little if called often.
        final int max = Math.max(x, y);
        // Rosenberg-Strong pairing function; produces larger values in a square-shaped "ripple" moving away from the origin.
        return (max * max + max + x - y);
    }

    /**
     * Given an int that may have been returned by {@link #signedRosenbergStrongHashCode(int, int)}, this finds the
     * Coord (as {@code short x} and {@code short y}) that would produce that int if passed to
     * {@link #signedRosenbergStrongHashCode(int, int)}.
     * <br>
     * Calculating this is branchless if calculating {@link Math#min(int, int)} is branchless. This is true on modern
     * desktop JVMs with sufficient optimization, and may be true on other platforms as well.
     * <br>
     * The inverse algorithm, like the forward algorithm, was modified slightly from
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">this article by Steven Pigeon</a>.
     *
     * @param code typically a result of {@link #signedRosenbergStrongHashCode(int, int)}
     * @return a Coord that contains the x and y that would have been passed to {@link #signedRosenbergStrongHashCode(int, int)}
     */
    public static Coord signedRosenbergStrongInverse(final int code) {
        final int b = (int)Math.sqrt(code & 0xFFFFFFFFL);
        final int r = code - b * b;
        final int min = Math.min(b, r);
        return Coord.get(min, b - r + min);
    }

    /**
     * If x and y are valid {@code short} numbers, then this will return a unique {@code int} hash code for those two.
     * If either is not a valid short, this cannot be guaranteed to produce a unique result. If you compare the results
     * for two nearby x,y points, the upper bits of the hash codes this produces will be more random than the lower
     * bits. This helps avoid collisions in dense sets or maps of Coord.
     * <br>
     * The actual method this uses involves passing x and y to the Rosenberg-Strong pairing function, then
     * multiplying that by 0x9E3779B9, or -1640531527 in decimal. The Rosenberg-Strong pairing function is discussed
     * more <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">here (a good introduction)</a> and
     * <a href="https://arxiv.org/abs/1706.04129">here (a more technical paper)</a>. 0x9E3779B9 is used because it is
     * (2 to the 32) divided by the golden ratio, and because of properties of the golden ratio, 0x9E3779B9 helps ensure
     * "sub-random" bit patterns in its multiples. Because 0x9E3779B9 is odd, if every possible int is taken and
     * multiplied by 0x9E3779B9, the full set of (2 to the 32) numbers will just be rearranged; nothing will collide.
     * <br>
     * This is the same as the algorithm used to precalculate the hash returned by {@link #hashCode()}. Unlike most of
     * the other hashCode() variants here, this acts fine with negative inputs, and should still return random-enough
     * hashes when x or y isn't in the short range (just not guaranteed to be unique).
     * <br>
     * Calculating this is branchless if calculating {@link Math#max(int, int)} is branchless. This is true on modern
     * desktop JVMs with sufficient optimization, and may be true on other platforms as well.
     *
     * @param x should usually be in the range for a valid short (from {@link Short#MIN_VALUE} to {@link Short#MAX_VALUE})
     * @param y should usually be in the range for a valid short (from {@link Short#MIN_VALUE} to {@link Short#MAX_VALUE})
     * @return an int hash code that will be unique for any combination of short x and short y
     */
    public static int signedRosenbergStrongMultiplyHashCode(int x, int y) {
        // Calculates a hash that won't overlap until very, very many Coords have been produced.
        // the signs for x and y; each is either -1 or 0
        int xs = x >> 31, ys = y >> 31;
        // makes x equivalent to -1 ^ x if x is negative; this means x is never negative after this
        x ^= xs;
        // same for y; it is also never negative
        y ^= ys;
        // Math.max can be branchless on modern JVMs, which may speed this method up a little if called often
        final int max = Math.max(x, y);
        // imul uses * on most platforms, but instead uses the JS Math.imul() function on GWT
        return BitConversion.imul(
                // Rosenberg-Strong pairing function; produces larger values in a square-shaped "ripple" moving away from the origin
                (max * max + max + x - y)
                        // XOR with every odd-index bit of xs and every even-index bit of ys
                        // this makes negative x, negative y, positive both, and negative both all get different bits XORed or not
                        ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555)
                // use imul() to multiply by a golden-ratio-based number to randomize upper bits
                , 0x9E3779B9);
    }
    /**
     * Given an int that may have been returned by {@link #signedRosenbergStrongMultiplyHashCode(int, int)}, this finds
     * the Coord (as {@code short x} and {@code short y}) that would produce that int if passed to
     * {@link #signedRosenbergStrongMultiplyHashCode(int, int)}.
     * <br>
     * Calculating this is branchless if calculating {@link Math#min(int, int)} is branchless. This is true on modern
     * desktop JVMs with sufficient optimization, and may be true on other platforms as well.
     * <br>
     * The inverse algorithm, like the forward algorithm, was modified from
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">this article by Steven Pigeon</a>.
     *
     * @param code typically a result of {@link #signedRosenbergStrongMultiplyHashCode(int, int)}
     * @return a Coord that contains the x and y that would have been passed to {@link #signedRosenbergStrongMultiplyHashCode(int, int)}
     */
    public static Coord signedRosenbergStrongMultiplyInverse(int code) {
        code = BitConversion.imul(code, 0x144CBC89);
        final int xs = code >> 31, ys = (code << 1) >> 31;
        code ^= (xs & 0xAAAAAAAA) ^ (ys & 0x55555555);
        final int b = (int)Math.sqrt(code);
        final int r = code - b * b;
        final int min = Math.min(b, r);
        return Coord.get(min ^ xs, b - r + min ^ ys);
    }

    /**
     * This is just like {@link #signedRosenbergStrongMultiplyHashCode(int, int)}, but using the Cantor pairing function
     * instead of the Rosenberg-Strong pairing function, and without the finalizing multiplication the other hash code
     * uses. Like that hash code, this will produce different results for {@code (x,y)}, {@code (-x,y)} {@code (x,-y)},
     * and {@code (-x,-y)}. You should see unique results if you give this only (x,y) points where each of x and y is
     * between {@code -23170} and {@code 23169}, inclusive. This is a smaller range than
     * {@link #signedRosenbergStrongMultiplyHashCode(int, int)} guarantees uniqueness for, by over 9000 at each end.
     * <br>
     * Calculating this is always branchless.
     *
     * @param x should usually be in the range from {@code -23170} to {@code 23169}
     * @param y should usually be in the range from {@code -23170} to {@code 23169}
     * @return an int hash code that should be unique for any combination of short x and short y
     */
    public static int signedCantorHashCode(int x, int y) {
        // Calculates a hash that won't overlap until very, very many Coords have been produced.
        // the signs for x and y; each is either -1 or 0
        int xs = x >> 31, ys = y >> 31;
        // makes x equivalent to -1 ^ x if x is negative; this means x is never negative after this
        x ^= xs;
        // same for y; it is also never negative
        y ^= ys;
        return  // Cantor pairing function; produces larger values in a diamond-shaped "ripple" moving away from the origin
                (y + ((x + y) * (x + y + 1) >> 1))
                        // XOR with every odd-index bit of xs and every even-index bit of ys
                        // this makes negative x, negative y, positive both, and negative both all get different bits XORed or not
                        ^ (xs & 0xAAAAAAAA) ^ (ys & 0x55555555);
    }

    /**
     * Gets a variant hash code for this Coord; does not use the standard "auto-complete" style of hash that most IDEs
     * will generate, but instead uses a specific technique based on the
     * <a href="https://arxiv.org/abs/1706.04129">Rosenberg-Strong pairing function</a>. This technique will generally
     * return all low values before it returns high values, if small Coord components are hashed first. The bits of the
     * results will not be especially random, but they won't collide much at all, so in this case we may not want the
     * most-random hashes. It does much better when Coords are in the default pooled range of -3 or greater.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">The Rosenberg-Strong pairing function is discussed more here</a>;.
     * <br>
     * This works best if the Coords hashed are within the pooled range, including negative values between -3 and -1. If
     * There are no negative x or y values, this does not perform as well as it could, and will probably perform worse
     * than {@link #hashCode()}. It probably performs worse than the precalculated hashCode() in general, too.
     *
     * @return an int that should, for most different Coord values, be significantly different from the other hash codes
     * @see #rosenbergStrongHashCode(int, int) A static method that gets the same results as this method without involving a Coord
     * @see #rosenbergStrongRandomizedHashCode(int, int) A static method that gets similar but more-random results than this method without involving a Coord
     */
    public int denseHashCode() {
        //// for Coord, since it can be as low as -3, and Rosenberg-Strong works only for positive integers
//        final int x = this.x + 3; // These are incorporated into the math below.
//        final int y = this.y + 3;
        //// Rosenberg-Strong pairing function; has excellent traits for keeping the hash gap-less while the
        //// inputs fit inside a square, and is still good for rectangles.
//        final int n = (x >= y ? x * (x + 2) - y : y * y + x);
        //// This modifies Rosenberg-Strong so here, x is effectively (x+3) and y is (y+3).
        //// This requires less addition sometimes, and shouldn't ever require more.
        return (x >= y ? x * (x + 8) - y + 12 : y * (y + 6) + x + 12);
    }

    /**
     * A specialized hashing function that is meant to pack Coord items extremely densely in hash-based Maps or Sets, at
     * the expense of any random-seeming quality in the hash. This is simply the
     * <a href="https://en.wikipedia.org/wiki/Pairing_function">Cantor pairing function</a>, and while it
     * does not behave particularly well with negative x or negative y, it does very well at not wasting space or
     * computation time in a hash table with Coord keys that are very densely packed. This will be slower than just
     * calling {@link #hashCode()} in most cases, though, because that method uses a precomputed value.
     * <br>
     * This can produce negative results for some negative x,y inputs, but usually produces small positive results when
     * both x and y are small and positive, and large positive results if either x or y is even moderately large.
     * <br>
     * This is "hasty" because it is meant to be fast, but is no longer the fastest option, so it's just fairly fast and
     * has perhaps-not-the-best quality possible. This is identical to calling {@link #cantorHashCode(int, int)} on this
     * Coord's x and y.
     *
     * @return an int that should, for different non-negative Coord values, be at least a little different from other hash codes
     */
    public int hastyHashCode() {
        return y + ((x + y) * (x + y + 1) >> 1);
    }

    /**
     * Returns the int result of the <a href="https://en.wikipedia.org/wiki/Pairing_function">Cantor pairing function</a>
     * for two int inputs. This is a way of getting a unique int
     * result for small enough x and y values, where "small enough" can safely be considered "between 0 and 23000." This
     * can overflow if the sum of x and y is greater than 46340, so it can't reasonably deal with all int inputs. In
     * that case it still produces a result, it just may be negative or be a duplicate of another hash result.
     * @param x the x coordinate of the "imaginary Coord" to hash
     * @param y the y coordinate of the "imaginary Coord" to hash
     * @return the result of the Cantor pairing function on x and y
     */
    public static int cantorHashCode(int x, int y) {
        return y + ((x + y) * (x + y + 1) >> 1);
    }
    /**
     * A static version of a prior {@link #hashCode()} method of this class, taking x and y as parameters instead of
     * requiring a Coord object. Like that prior hashCode() method, this involves the close-to-optimal mathematical
     * Rosenberg-Strong pairing function to distribute x and y without overlap until they get very large. The
     * Rosenberg-Strong pairing function can be written simply as {@code ((x >= y) ? x * (x + 2) - y : y * y + x)}; it
     * produces sequential results for a sequence of positive points traveling in square "shells" away from the origin.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">the algorithm is discussed more here</a>; the only
     * changes made here are adding 3 to x and y (to account for the minimum of -3 in most cases for a Coord).
     *
     * @param x the x coordinate of the "imaginary Coord" to hash
     * @param y the y coordinate of the "imaginary Coord" to hash
     * @return the equivalent to an older hashCode() of an "imaginary Coord"
     */
    public static int rosenbergStrongHashCode(int x, int y) {
        //// for Coord, since it can be as low as -3, and Rosenberg-Strong works only for positive integers
//        x += 3; // These are incorporated into the math below.
//        y += 3;
        //// Rosenberg-Strong pairing function; has excellent traits for keeping the hash gap-less while the
        //// inputs fit inside a square, and is still good for rectangles.
        return (x >= y ? x * (x + 8) - y + 12 : y * (y + 6) + x + 12);
    }

    /**
     * A static version of an earlier {@link #hashCode()} method of this class, taking x and y as parameters instead of
     * requiring a Coord object. Like the earlier hashCode() method, this involves the close-to-optimal mathematical
     * Rosenberg-Strong pairing function to distribute x and y without overlap until they get very large. The
     * Rosenberg-Strong pairing function can be written simply as {@code ((x >= y) ? x * (x + 2) - y : y * y + x)}; it
     * produces sequential results for a sequence of positive points traveling in square "shells" away from the origin.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">the algorithm is discussed more here</a>; the only
     * changes made here are adding 3 to x and y (to account for the minimum of -3 in most cases for a Coord), and some
     * finalizing steps that help randomize the upper bits of the hash code (the lower bits are quite non-random because
     * they can't permit any gaps while optimizing collision rates). This method is "Randomized" because of these final
     * steps, and they may slow it down as a hash code somewhat, but make the result more chaotic.
     *
     * @param x the x coordinate of the "imaginary Coord" to hash
     * @param y the y coordinate of the "imaginary Coord" to hash
     * @return the equivalent to the hashCode() of an "imaginary Coord"
     */
    public static int rosenbergStrongRandomizedHashCode(int x, int y) {
        //// for Coord, since it can be as low as -3, and Rosenberg-Strong works only for positive integers
//        x += 3; // These are incorporated into the math below.
//        y += 3;
        //// Rosenberg-Strong pairing function; has excellent traits for keeping the hash gap-less while the
        //// inputs fit inside a square, and is still good for rectangles.
        final int n = (x >= y ? x * (x + 8) - y + 12 : y * (y + 6) + x + 12);
        //// Gray code, XLCG, XLCG (ending on a XOR to stay within int range on GWT).
        //// The Gray code moves bits around just a little, but keeps the same power-of-two upper bound.
        //// the XLCGs together only really randomize the upper bits; they don't change the lower bit at all.
        //// (recall from RNG class that an XLCG is a XOR by a constant, then a multiply by a constant, where
        //// the XOR constant, mod 8, is 5, while the multiplier, mod 8, is 3; the order can be reversed too.)
        //// ending on a XOR helps mostly for GWT.
        return ((n ^ n >>> 1) * 0x9E373 ^ 0xD1B54A35) * 0x125493 ^ 0x91E10DA5;
    }

    /**
     * An earlier hashCode() implementation used by this class, now standalone in case you want to replicate the results
     * of the older code. This uses only bitwise operations, which tend to be fairly fast on all platforms, and when
     * used in a collection it has comparable collision rates to the current hashCode() method (very, very low rates),
     * but if used for procedural generation it's simply terrible, with large blocks of nearby x,y points having
     * identical values for several bits and all changes happening in a repetitive checkerboard pattern.
     *
     * @param x the x coordinate of the "imaginary Coord" to hash
     * @param y the y coordinate of the "imaginary Coord" to hash
     * @return the equivalent to the hashCode() of an "imaginary Coord"
     */
    public static int xoroHashCode(final int x, final int y) {
        int r = x ^ y;
        r ^= (x << 13 | x >>> 19) ^ (r << 5) ^ (r << 28 | r >>> 4);
        r = x ^ (r << 11 | r >>> 21);
        return r ^ (r << 25 | r >>> 7);
    }

    /**
     * Something like hashCode(), but reversible with {@code Coord.decode()}. Works for Coords between roughly -256 and
     * 32000 in each of x and y, but will probably only decode to pooled Coords if x and y are both between -3 and 255
     * (inclusive for both).
     *
     * @return an int as a unique code for this Coord
     */
    public int encode() {
        return ((x + 256) << 16) ^ (y + 256);
    }

    /**
     * An alternative to getting a Coord with Coord.get() only to encode() it as the next step. This doesn't create a
     * Coord in the middle step. Can be decoded with Coord.decode() to get the (x,y) Coord.
     *
     * @param x the x position to encode
     * @param y the y position to encode
     * @return the coded int that a Coord at (x,y) would produce with encode()
     */
    public static int pureEncode(final int x, final int y) {
        return ((x + 256) << 16) ^ (y + 256);
    }

    /**
     * This can take an int produced by {@code someCoord.encode()} and get the original Coord back out of it. It
     * works for all pooled Coords where the pool hasn't been expanded past about 32,000 in either dimension. It even
     * works for Coords with negative x or y as well, if they are no lower than -256 in either dimension. This will
     * almost certainly fail (producing a gibberish Coord that probably won't be pooled) on hashes produced by any other
     * class, including subclasses of Coord.
     *
     * @param code an encoded int from a Coord, but not a subclass of Coord
     * @return the Coord that gave hash as its hashCode()
     */
    public static Coord decode(final int code) {
        return get((code >>> 16) - 256, (code & 0xFFFF) - 256);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coord) {
            Coord other = (Coord) o;
            return x == other.x && y == other.y;
        } else {
            return false;
        }
    }

    private static int POOL_WIDTH = 256;
    private static int POOL_HEIGHT = 256;
    private static int PADDED_POOL_WIDTH = POOL_WIDTH + 3;
    private static int PADDED_POOL_HEIGHT = POOL_HEIGHT + 3;
    /**
     * The index for the point 0,0 .
     */
    private static int ORIGIN = PADDED_POOL_WIDTH * 3 + 3;
    private static Coord[] POOL = new Coord[PADDED_POOL_WIDTH * PADDED_POOL_HEIGHT];

    static {
        for (int y = -3, i = 0; y < POOL_HEIGHT; y++) {
            for (int x = -3; x < POOL_WIDTH; x++) {
                POOL[i++] = new Coord(x, y);
            }
        }
    }

    /**
     * Gets the width of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for x values that can be used efficiently in Coords.
     * Requesting a Coord with a x greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     *
     * @return the width of the Coord cache, disregarding negative Coords
     */
    public static int getCacheWidth() {
        return POOL_WIDTH;
    }

    /**
     * Gets the height of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for y values that can be used efficiently in Coords.
     * Requesting a Coord with a y greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     *
     * @return the height of the Coord cache, disregarding negative Coords
     */
    public static int getCacheHeight() {
        return POOL_HEIGHT;
    }

    /**
     * Gets the width of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for x values that can be used efficiently in Coords.
     * Requesting a Coord with a x greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     * <br>
     * This is an alias for {@link #getCacheWidth()}.
     *
     * @return the width of the Coord cache, disregarding negative Coords
     */
    public static int getPoolWidth() {
        return POOL_WIDTH;
    }

    /**
     * Gets the height of the pool used as a cache for Coords, not including negative Coords.
     * Unless expandPool() has been called, this should be 256.
     * Useful for finding the upper (exclusive) bound for y values that can be used efficiently in Coords.
     * Requesting a Coord with a y greater than or equal to this value will result in a new Coord being allocated and
     * not cached, which may cause problems with code that expects the normal reference equality of Coords to be upheld
     * and in extreme cases may require more time garbage collecting than is normally necessary.
     * <br>
     * This is an alias for {@link #getCacheHeight()}.
     *
     * @return the height of the Coord cache, disregarding negative Coords
     */
    public static int getPoolHeight() {
        return POOL_HEIGHT;
    }

    /**
     * Enlarges the pool of cached Coords to the given width and height, and doesn't change
     * a dimension if it would be reduced in size.
     * Cached Coord values will be reused by Coord.get instead of re-allocated each time.
     * The default pool allows Coords with x and y each between -3 and 255, inclusive, to
     * be cached, and is considered to have width and height of 256 to begin with. Giving a
     * width greater than 256 will allow Coords with x greater than 255 to be cached;
     * likewise for height. If width or height is smaller than the current cache width or
     * height, that dimension will not change, but the other still may if it is valid. You
     * cannot shrink the pool size.
     *
     * @param width  the new width for the pool of cached Coords; will be ignored if smaller than the current width
     * @param height the new height for the pool of cached Coords; will be ignored if smaller than the current height
     */
    public static void expandPoolTo(final int width, final int height) {
        expandPool(Math.max(0, width - POOL_WIDTH), Math.max(0, height - POOL_HEIGHT));
    }

    /**
     * Enlarges the pool of cached Coords by the given amount of expansion for x and y.
     * Cached Coord values will be reused by Coord.get instead of re-allocated each time.
     * The default pool allows Coords with x and y each between -3 and 255, inclusive, to
     * be cached, and this can increase the size in the positive direction. If either
     * xIncrease or yIncrease is negative, this method returns immediately and does nothing
     * else; the same is true of both arguments are zero. You cannot shrink the pool size.
     *
     * @param xIncrease the amount to increase cache's width by
     * @param yIncrease the amount to increase cache's height by
     */
    public static void expandPool(final int xIncrease, final int yIncrease) {
        if (xIncrease < 0 || yIncrease < 0 || (xIncrease | yIncrease) == 0)
            return;
        int oldWidth = PADDED_POOL_WIDTH, oldHeight = PADDED_POOL_HEIGHT;
        int newWidth = oldWidth + xIncrease, newHeight = oldHeight + yIncrease;
        Coord[] POOL2 = new Coord[newWidth * newHeight];
        // here, the width is the same. we can directly copy rows before the increased area.
        if (xIncrease == 0) {
            System.arraycopy(POOL, 0, POOL2, 0, POOL.length);
            for (int y = oldHeight, i = POOL.length; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    POOL2[i++] = new Coord(x - 3, y - 3);
                }
            }
        }
        // here, the width is different, so we need to copy one row at a time and fill in the newly-added space.
        else {
            for (int y = 0; y < oldHeight; y++) {
                System.arraycopy(POOL, y * oldWidth, POOL2, y * newWidth, oldWidth);
                for (int x = oldWidth, i = y * newWidth + oldWidth; x < newWidth; x++) {
                    POOL2[i++] = new Coord(x - 3, y - 3);
                }
            }
            for (int y = oldHeight, i = oldHeight * newWidth; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    POOL2[i++] = new Coord(x - 3, y - 3);
                }
            }
            PADDED_POOL_WIDTH = newWidth;
            POOL_WIDTH = newWidth - 3;
            ORIGIN = newWidth * 3 + 3;
        }
        PADDED_POOL_HEIGHT = newHeight;
        POOL_HEIGHT = newHeight - 3;
        POOL = POOL2;
    }

    /**
     * Gets a (usually cached) Coord linearly-interpolated between this Coord and {@code end}, with the actual position
     * relative to this based on {@code amountTraveled}. If amountTraveled is 0, this simply returns a Coord equal to
     * this; if amountTraveled is 1, this returns a Coord equal to end, and values in between 0 and 1 give Coords
     * between this and end.
     *
     * @param end            another Coord that acts as the "far" endpoint, where this is the "near" start point
     * @param amountTraveled a float between 0 and 1 inclusive, with lower meaning closer to this, higher meaning closer to end
     * @return a Coord that is between this and end as long as amountTraveled is between 0 and 1
     */
    @Override
    public Coord lerp(Point2<?> end, float amountTraveled) {
        return Coord.get(x + Math.round((end.x() - x) * amountTraveled),
                y + Math.round((end.y() - y) * amountTraveled));
    }

    @Override
    public float x() {
        return x;
    }

    @Override
    public Coord x(float nextX) {
        return get((short)nextX, y);
    }

    @Override
    public int xi() {
        return x;
    }

    @Override
    public Coord xi(int next) {
        return Coord.get(next, y);
    }

    @Override
    public float y() {
        return y;
    }

    @Override
    public Coord y(float nextY) {
        return get(x, (short)nextY);
    }

    @Override
    public int yi() {
        return y;
    }

    @Override
    public Coord yi(int next) {
        return Coord.get(x, next);
    }

    @Override
    public Coord set(float nextX, float nextY) {
        return get((short)nextX, (short)nextY);
    }

    @Override
    public Coord seti(int x, int y) {
        return Coord.get(x, y);
    }

    /**
     * A constant PartialParser that can read in the {@link Coord#toString()} of a printed Coord to get that Coord back.
     * This is mostly useful with {@link CoordSet#addLegible(String, String, PartialParser)} and similar methods on
     * Coord-based data structures.
     */
    public static final PartialParser<Coord> COORD_PARSER = (text, start, end) -> {
        final int comma = text.indexOf(',');
        return Coord.get(Base.BASE10.readShort(text, start + 1, comma), Base.BASE10.readShort(text, comma + 1, text.indexOf(')')));
    };
}
