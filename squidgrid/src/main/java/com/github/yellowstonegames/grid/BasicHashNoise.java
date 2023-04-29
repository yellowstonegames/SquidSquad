package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.core.annotations.Beta;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.tommyettinger.digital.MathTools.lerp;
import static com.github.yellowstonegames.grid.Noise.*;

@Beta
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
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #deserializeFromString(String)} to reassign the described state to another INoise. The syntax here
     * should always start and end with the {@code `} character, which is used by
     * {@link Serializer#deserializeFromString(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another INoise that this uses.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #deserializeFromString(String)}, and
     * {@link #copy()}.
     *
     * @return a String that describes this INoise for serialization
     */
    @Override
    public String serializeToString() {
        return "`" + seed + "`";
    }

    /**
     * Given a serialized String produced by {@link #serializeToString()}, reassigns this INoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. INoise classes do not have to
     * implement any serialization methods, but they aren't serializable by the methods in this class or in
     * {@link Serializer} unless they do implement this, {@link #getTag()}, {@link #deserializeFromString(String)}, and
     * {@link #copy()}.
     *
     * @param data a serialized String, typically produced by {@link #serializeToString()}
     * @return this INoise, after being modified (if possible)
     */
    @Override
    public BasicHashNoise deserializeFromString(String data) {
        seed = (DigitTools.intFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static BasicHashNoise recreateFromString(String data) {
        return new BasicHashNoise(DigitTools.intFromDec(data, 1, data.length() - 1));
    }


    /**
     * Creates a copy of this PerlinNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     *
     * @return a copy of this PerlinNoise
     */
    @Override
    public BasicHashNoise copy() {
        return new BasicHashNoise(this.seed);
    }

    protected static float gradCoord5D(long seed, int x, int y, int z, int w, int u,
                                        float xd, float yd, float zd, float wd, float ud) {
        final int hash =
                (int)((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z ^ 0xA6F5777F6F88983FL * w ^ 0x9609C71EB7D03F7BL * u)
                        * (seed) >>> 56) * 5;
        return xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash + 1] + zd * GRADIENTS_5D[hash + 2]
                + wd * GRADIENTS_5D[hash + 3] + ud * GRADIENTS_5D[hash + 4];
    }
    
    protected static float gradCoord6D(long seed, int x, int y, int z, int w, int u, int v,
                                        float xd, float yd, float zd, float wd, float ud, float vd) {
        final int hash =
                (int)(
                        (seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L *
                                z ^ 0xA6F5777F6F88983FL * w ^ 0x9609C71EB7D03F7BL * u ^ 0x86D516E50B04AB1BL * v) * (seed)
                        >>> 56) * 6;
        return xd * GRADIENTS_6D[hash] + yd * GRADIENTS_6D[hash + 1] + zd * GRADIENTS_6D[hash + 2]
                + wd * GRADIENTS_6D[hash + 3] + ud * GRADIENTS_6D[hash + 4] + vd * GRADIENTS_6D[hash + 5];
    }
    /**
     * Given a float {@code a} from -1.0 to 1.0 (both inclusive), this gets a float that adjusts a to be closer to the
     * end points of that range (if less than 0, it gets closer to -1.0, otherwise it gets closer to 1.0).
     * <br>
     * Used to increase the frequency of high and low results, which
     * improves the behavior of ridged and billow noise.
     * @param a a float between -1.0f and 1.0f inclusive
     * @return a float between -1.0f and 1.0f inclusive that is more likely to be near the extremes
     */
    public float emphasizeSigned(float a)
    {
        a = a * 0.5f + 0.5f;
        return a * a * (6.0f - 4.0f * a) - 1.0f;
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
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
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
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
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
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
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
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
        u = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 10.0f);
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
        x = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        y = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        z = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        w = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
        u = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 10.0f);
        v = vf * vf * vf * (vf * (vf * 6.0f - 15.0f) + 10.0f);
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
}
