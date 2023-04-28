package com.github.yellowstonegames.grid;

import com.github.yellowstonegames.core.DigitTools;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.tommyettinger.digital.MathTools.lerp;
import static com.github.yellowstonegames.grid.Noise.GRADIENTS_2D;
import static com.github.yellowstonegames.grid.Noise.GRADIENTS_3D;
import static com.github.yellowstonegames.grid.Noise.GRADIENTS_4D;
import static com.github.yellowstonegames.grid.Noise.GRADIENTS_5D;
import static com.github.yellowstonegames.grid.Noise.GRADIENTS_6D;

/**
 * "Classic" Perlin noise, written by Ken Perlin before he created {@link SimplexNoise Simplex Noise}, with minor
 * adjustments. This uses quintic interpolation throughout (which was an improvement found in Simplex Noise), and has a
 * single {@code long} seed. Perlin Noise can have significant grid-aligned and 45-degree-diagonal artifacts when too
 * few octaves are used, but sometimes this is irrelevant, such as when sampling 3D noise on the surface of a sphere.
 */
public class PerlinNoise implements INoise {
    public static final PerlinNoise instance = new PerlinNoise();
    public long seed;
    public PerlinNoise() {
        this(0x1337BEEFCAFEL);
    }

    public PerlinNoise(final long seed) {
        this.seed = seed;
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
        this.seed = seed;
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
     * Returns the constant String {@code "PerN"} that identifies this in serialized Strings.
     *
     * @return a short String constant that identifies this INoise type, {@code "PerN"}
     */
    @Override
    public String getTag() {
        return "PerN";
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
    public PerlinNoise deserializeFromString(String data) {
        seed = (DigitTools.longFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static PerlinNoise recreateFromString(String data) {
        return new PerlinNoise(DigitTools.longFromDec(data, 1, data.length() - 1));
    }


    /**
     * Creates a copy of this PerlinNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     *
     * @return a copy of this PerlinNoise
     */
    @Override
    public PerlinNoise copy() {
        return new PerlinNoise(this.seed);
    }

    //0xE60E2B722B53AEEBL, 0xCEBD76D9EDB6A8EFL, 0xB9C9AA3A51D00B65L, 0xA6F5777F6F88983FL, 0x9609C71EB7D03F7BL, 0x86D516E50B04AB1BL
    protected static float gradCoord2D(long seed, int x, int y,
                                        float xd, float yd) {
        final int hash = (int) ((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y) * (seed) >>> 55 & 510);
        return xd * GRADIENTS_2D[hash] + yd * GRADIENTS_2D[hash + 1];
    }
    protected static float gradCoord3D(long seed, int x, int y, int z, float xd, float yd, float zd) {
        final int hash =
                (int)((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z) * (seed)
                        >>> 59) * 3;
        return (xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2]);
    }
    protected static float gradCoord4D(long seed, int x, int y, int z, int w,
                                        float xd, float yd, float zd, float wd) {
        final int hash =
                (int) ((seed ^= 0xE60E2B722B53AEEBL * x ^ 0xCEBD76D9EDB6A8EFL * y ^ 0xB9C9AA3A51D00B65L * z ^ 0xA6F5777F6F88983FL * w) * (seed)
                        >>> 56) & -4;
        return xd * GRADIENTS_4D[hash] + yd * GRADIENTS_4D[hash + 1] + zd * GRADIENTS_4D[hash + 2] + wd * GRADIENTS_4D[hash + 3];
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
        x *= 2.0f;
        y *= 2.0f;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y);
        final float xf = x - x0, yf = y - y0;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        return
                emphasizeSigned(lerp(lerp(gradCoord2D(seed, x0, y0, xf, yf), gradCoord2D(seed, x0+1, y0, xf - 1, yf), xa),
                                lerp(gradCoord2D(seed, x0, y0+1, xf, yf-1), gradCoord2D(seed, x0+1, y0+1, xf - 1, yf - 1), xa),
                                ya) * 1.4142f);//* 0.875;// * 1.4142;
    }

    @Override
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, final long seed) {
        x *= 2.0f;
        y *= 2.0f;
        z *= 2.0f;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z);
        final float xf = x - x0, yf = y - y0, zf = z - z0;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
         return
                 emphasizeSigned(
                         lerp(
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0, xf, yf, zf),
                                                 gradCoord3D(seed, x0+1, y0, z0, xf - 1, yf, zf),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+1, z0, xf, yf-1, zf),
                                                 gradCoord3D(seed, x0+1, y0+1, z0, xf - 1, yf - 1, zf),
                                                 xa),
                                         ya),
                                 lerp(
                                         lerp(
                                                 gradCoord3D(seed, x0, y0, z0+1, xf, yf, zf-1),
                                                 gradCoord3D(seed, x0+1, y0, z0+1, xf - 1, yf, zf-1),
                                                 xa),
                                         lerp(
                                                 gradCoord3D(seed, x0, y0+1, z0+1, xf, yf-1, zf-1),
                                                 gradCoord3D(seed, x0+1, y0+1, z0+1, xf - 1, yf - 1, zf-1),
                                                 xa),
                                         ya),
                                 za) * 1.0625f);
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, final long seed) {
        x *= 2.0f;
        y *= 2.0f;
        z *= 2.0f;
        w *= 2.0f;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
        return
                emphasizeSigned(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0, xf, yf, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0, xf - 1, yf, zf, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0, xf, yf-1, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0, xf - 1, yf - 1, zf, wf),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0, xf, yf, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0, xf - 1, yf, zf-1, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0, xf, yf-1, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0, xf - 1, yf - 1, zf-1, wf),
                                                        xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0+1, xf, yf, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0+1, xf - 1, yf, zf, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0+1, xf, yf-1, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0+1, xf - 1, yf - 1, zf, wf - 1),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0+1, xf, yf, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0+1, xf - 1, yf, zf-1, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0+1, xf, yf-1, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0+1, xf - 1, yf - 1, zf-1, wf - 1),
                                                        xa),
                                                ya),
                                        za),
                                wa) * 0.555f);
    }


    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        x *= 2.0f;
        y *= 2.0f;
        z *= 2.0f;
        w *= 2.0f;
        u *= 2.0f;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0;

        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
        final float ua = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 10.0f);
        return
                emphasizeSigned(
                lerp(lerp(
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0, u0, xf, yf, zf, wf, uf),
                                                gradCoord5D(seed, x0+1, y0, z0, w0, u0, xf-1, yf, zf, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0, w0, u0, xf, yf-1, zf, wf, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0, w0, u0, xf-1, yf-1, zf, wf, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+1, w0, u0, xf, yf, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+1, y0, z0+1, w0, u0, xf-1, yf, zf-1, wf, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0, u0, xf, yf-1, zf-1, wf, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0+1, w0, u0, xf-1, yf-1, zf-1, wf, uf), xa),
                                        ya),
                                za),
                        lerp(
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0, w0+1, u0, xf, yf, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0, z0, w0+1, u0, xf-1, yf, zf, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0, w0+1, u0, xf, yf-1, zf, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0, w0+1, u0, xf-1, yf-1, zf, wf-1, uf), xa),
                                        ya),
                                lerp(
                                        lerp(gradCoord5D(seed, x0, y0, z0+1, w0+1, u0, xf, yf, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0, z0+1, w0+1, u0, xf-1, yf, zf-1, wf-1, uf), xa),
                                        lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0+1, u0, xf, yf-1, zf-1, wf-1, uf),
                                                gradCoord5D(seed, x0+1, y0+1, z0+1, w0+1, u0, xf-1, yf-1, zf-1, wf-1, uf), xa),
                                        ya),
                                za),
                        wa),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0, u0+1, xf, yf, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0, w0, u0+1, xf-1, yf, zf, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0, w0, u0+1, xf, yf-1, zf, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0, w0, u0+1, xf-1, yf-1, zf, wf, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+1, w0, u0+1, xf, yf, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0+1, w0, u0+1, xf-1, yf, zf-1, wf, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0, u0+1, xf, yf-1, zf-1, wf, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0+1, w0, u0+1, xf-1, yf-1, zf-1, wf, uf-1), xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0, w0+1, u0+1, xf, yf, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0, w0+1, u0+1, xf-1, yf, zf, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0, w0+1, u0+1, xf, yf-1, zf, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0, w0+1, u0+1, xf-1, yf-1, zf, wf-1, uf-1), xa),
                                                ya),
                                        lerp(
                                                lerp(gradCoord5D(seed, x0, y0, z0+1, w0+1, u0+1, xf, yf, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0, z0+1, w0+1, u0+1, xf-1, yf, zf-1, wf-1, uf-1), xa),
                                                lerp(gradCoord5D(seed, x0, y0+1, z0+1, w0+1, u0+1, xf, yf-1, zf-1, wf-1, uf-1),
                                                        gradCoord5D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, xf-1, yf-1, zf-1, wf-1, uf-1), xa),
                                                ya),
                                        za),
                                wa),
                        ua) * 0.7777777f);
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        x *= 2.0f;
        y *= 2.0f;
        z *= 2.0f;
        w *= 2.0f;
        u *= 2.0f;
        v *= 2.0f;
        final int
                x0 = fastFloor(x),
                y0 = fastFloor(y),
                z0 = fastFloor(z),
                w0 = fastFloor(w),
                u0 = fastFloor(u),
                v0 = fastFloor(v);
        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0, vf = v - v0;
        final float xa = xf * xf * xf * (xf * (xf * 6.0f - 15.0f) + 10.0f);
        final float ya = yf * yf * yf * (yf * (yf * 6.0f - 15.0f) + 10.0f);
        final float za = zf * zf * zf * (zf * (zf * 6.0f - 15.0f) + 10.0f);
        final float wa = wf * wf * wf * (wf * (wf * 6.0f - 15.0f) + 10.0f);
        final float ua = uf * uf * uf * (uf * (uf * 6.0f - 15.0f) + 10.0f);
        final float va = vf * vf * vf * (vf * (vf * 6.0f - 15.0f) + 10.0f);
        return emphasizeSigned(
                lerp(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xf, yf, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0, v0, xf - 1, yf, zf, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0, v0, xf, yf - 1, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0, v0, xf - 1, yf - 1, zf, wf, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0, v0, xf, yf, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0, v0, xf - 1, yf, zf - 1, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0, v0, xf, yf - 1, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0, v0, xf - 1, yf - 1, zf - 1, wf, uf, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0, v0, xf, yf, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0, v0, xf - 1, yf, zf, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0, v0, xf, yf - 1, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0, v0, xf - 1, yf - 1, zf, wf - 1, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0, v0, xf, yf, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0, v0, xf - 1, yf, zf - 1, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0, v0, xf, yf - 1, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0 + 1, v0, xf, yf, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0 + 1, v0, xf - 1, yf, zf, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0 + 1, v0, xf, yf - 1, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0 + 1, v0, xf - 1, yf - 1, zf, wf, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0 + 1, v0, xf, yf, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0 + 1, v0, xf - 1, yf, zf - 1, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0 + 1, v0, xf, yf - 1, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0 + 1, v0, xf, yf, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0 + 1, v0, xf - 1, yf, zf, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0 + 1, v0, xf, yf - 1, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0 + 1, v0, xf, yf, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0 + 1, xf, yf, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0, v0 + 1, xf - 1, yf, zf, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0, v0 + 1, xf, yf - 1, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0, v0 + 1, xf - 1, yf - 1, zf, wf, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0, v0 + 1, xf, yf, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0, v0 + 1, xf - 1, yf, zf - 1, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0, v0 + 1, xf, yf - 1, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0, v0 + 1, xf - 1, yf - 1, zf - 1, wf, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0, v0 + 1, xf, yf, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0, v0 + 1, xf - 1, yf, zf, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0, v0 + 1, xf, yf - 1, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0, v0 + 1, xf - 1, yf - 1, zf, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0, v0 + 1, xf, yf, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0, v0 + 1, xf - 1, yf, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, xf, yf - 1, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, v0 + 1, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0 + 1, v0 + 1, xf, yf, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0, u0 + 1, v0 + 1, xf - 1, yf, zf, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0, u0 + 1, v0 + 1, xf, yf - 1, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0, u0 + 1, v0 + 1, xf - 1, yf - 1, zf, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0, u0 + 1, v0 + 1, xf, yf, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0, u0 + 1, v0 + 1, xf - 1, yf, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, xf, yf - 1, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, v0 + 1, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0 + 1, u0 + 1, v0 + 1, xf, yf, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, xf, yf - 1, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf, yf, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, v0 + 1, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        va) * 1.61f);
    }
}
