/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import static com.github.tommyettinger.digital.MathTools.fastFloor;

/**
 * Makes 2D through 6D value noise that simply preserves the behavior of an {@link IPointHash}, including intentional
 * (or unintentional) artifacts that the point hash produces. This is more useful with intentionally-flawed point hashes
 * from {@link FlawedPointHash}, such as {@link com.github.yellowstonegames.grid.FlawedPointHash.FlowerHash}. Note that
 * because this uses an arbitrary IPointHash (and that interface doesn't define any way to serialize itself), you can't
 * serialize or deserialize a BasicHashNoise to or from a String. Binary serialization (such as using Kryo) still works.
 */
public class BasicHashNoise implements INoise {
    public static final BasicHashNoise instance = new BasicHashNoise();
    public int seed;
    public IPointHash pointHash;
    public BasicHashNoise() {
        this(0xBEEFCAFE);
    }

    public BasicHashNoise(final int seed) {
        this(seed, new IntPointHash(seed));
    }
    public BasicHashNoise(final int seed, IPointHash hash) {
        this.seed = seed;
        pointHash = hash == null ? new IntPointHash(seed) : hash;
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, 2
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, 6
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true because this generator can be seeded with {@link #setSeed(long)} and retrieved with
     * {@link #getSeed()}.
     *
     * @return true
     */
    @Override
    public boolean canUseSeed() {
        return true;
    }

    /**
     * Sets the seed to the given long, if long seeds are supported, or {@code (int)seed} if only int seeds are
     * supported. If {@link #canUseSeed()} returns true, this must be implemented and must set the seed given a long
     * input. If this generator cannot be seeded, this is permitted to either do nothing or throw an
     * {@link UnsupportedOperationException}. If this operation allocates or is time-intensive, then that performance
     * cost will be passed along to {@link #getNoiseWithSeed}, since that calls this twice unless overridden. In the
     * case where seeding is expensive to perform, setSeed() can still be implemented while {@link #canUseSeed()}
     * returns false. This makes the {@link #getNoiseWithSeed} methods avoid reseeding, and instead move their inputs
     * around in space.
     *
     * @param seed a long or int seed, with no restrictions unless otherwise documented
     */
    @Override
    public void setSeed(long seed) {
        this.seed = (int)seed;
    }

    /**
     * Gets the current seed of the generator, as a long.
     *
     * @return the current seed, as a long
     */
    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the constant String {@code "BaHN"} that identifies this in serialized Strings.
     *
     * @return a short String constant that identifies this INoise type, {@code "BaHN"}
     */
    @Override
    public String getTag() {
        return "BaHN";
    }

    /**
     * Because this needs to write an {@link IPointHash}, but that interface can't be serialized, this throws an
     * {@link UnsupportedOperationException} only.
     *
     * @return never returns normally
     */
    @Override
    public String stringSerialize() {
        throw new UnsupportedOperationException("BasicHashNoise cannot be serialized to a String because there \n" +
                "is no way to serialize or deserialize an arbitrary IPointHash, which BasicHashNoise uses.");
    }

    /**
     * Because this needs to read an {@link IPointHash}, but that interface can't be deserialized, this throws an
     * {@link UnsupportedOperationException} only.
     *
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return never returns normally
     */
    @Override
    public BasicHashNoise stringDeserialize(String data) {
        throw new UnsupportedOperationException("BasicHashNoise cannot be deserialized from a String because there \n" +
                "is no way to serialize or deserialize an arbitrary IPointHash, which BasicHashNoise uses.");
    }

    /**
     * Creates a copy of this BasicHashNoise, which is a shallow copy here and assumes {@link #pointHash} has no mutable
     * state that could need a deep copy.
     * @return a copy of this BasicHashNoise
     */
    @Override
    public BasicHashNoise copy() {
        return new BasicHashNoise(this.seed, pointHash);
    }

    @Override
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, final long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y);
        final float xf = x - x0, yf = y - y0;
        
        int s = (int) seed;
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        return ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, s) + x * pointHash.hashWithState(x0+1, y0, s))
                + y * ((1 - x) * pointHash.hashWithState(x0, y0+1, s) + x * pointHash.hashWithState(x0+1, y0+1, s)))
                * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, final long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z);
        final float xf = x - x0, yf = y - y0, zf = z - z0;

        int s = (int) seed;
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        return ((1 - z) *
                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, s))
                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, s)))
                + z *
                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, s))
                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, s)))
        ) * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, final long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0;

        int s = (int) seed;
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, s))
                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, s)))
                        + z *
                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, s))
                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, s))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, s))
                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, s)))
                        + z *
                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, s))
                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, s)))
                ))) * 0x1p-31f;
    }


    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0;

        int s = (int) seed;
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        u = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 9.999998f);
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0, s)))
                                + z *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0, s))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0, s)))
                                + z *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, s)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0 + 1, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0 + 1, s)))
                                + z *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0 + 1, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, s))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0 + 1, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, s)))
                                + z *
                                ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, s))
                                        + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, s)))
                        ))))
        ) * 0x1p-31f;
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u),
                v0 = fastFloor(v);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0, vf = v - v0;

        int s = (int) seed;
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 9.999998f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 9.999998f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 9.999998f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 9.999998f);
        u = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 9.999998f);
        v = vf * vf * vf * (vf * (vf * 6.0f - 15.0f) + 9.999998f);
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0, v0, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0, v0, s))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0, v0, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0, s)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0 + 1, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0 + 1, v0, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0 + 1, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0, s))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0 + 1, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, s)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0, v0 + 1, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0, v0 + 1, s))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0, v0 + 1, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, s)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0, u0 + 1, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0, u0 + 1, v0 + 1, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0, u0 + 1, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, s))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0, w0 + 1, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0, w0 + 1, u0 + 1, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, s)))
                                        + z *
                                        ((1 - y) * ((1 - x) * pointHash.hashWithState(x0, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, s))
                                                + y * ((1 - x) * pointHash.hashWithState(x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, s) + x * pointHash.hashWithState(x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, s)))
                                ))))))
        ) * 0x1p-31f;
    }

    @Override
    public String toString() {
        return "BasicHashNoise{" +
                "seed=" + seed +
                ", pointHash=" + pointHash +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicHashNoise that = (BasicHashNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }
}
