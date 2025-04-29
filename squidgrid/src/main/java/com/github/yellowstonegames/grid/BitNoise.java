package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * An unusual kind of INoise that typically only outputs -1 or 1, for cases where you want binary results of blobby
 * noise, but can also produce more bits if constructed as such. If you shift the noise around and have multiple calls,
 * you can use this to get approximately fair distributions of bits by getting just the sign bit from each -1 or 1
 * result and concatenating bits to make a 2-bit, 3-bit, etc. value.
 * <br>
 * In SquidLib, this was called MerlinNoise, and acted similarly. (Merlin because I felt like a wizard writing it.)
 */
public class BitNoise implements INoise {

    public long seed;
    protected int bits = 1, resolution = 4;
    private long resSize = 1L << resolution;
    /**
     * Constructor for a default BitNoise instance with 1-bit output and resolution 3 (yielding 8x8-cell zones that
     * share their corners). The seed can be set at any point, but it will start at 1.
     */
    public BitNoise() {
        seed = 1L;
    }

    /**
     * Constructor for a BitNoise instance that allows specification of all parameters.
     * @param seed the seed to use to alter the generated noise in {@link #noise2D(long, long)} and {@link #noise3D(long, long, long)}
     * @param bits the number of bits to output; typically 1 to produce only -1 or 1
     * @param resolution an exponent that determines the size of a "zone" of cells that blend between the values at the zone's corners; commonly 1-6
     */
    public BitNoise(long seed, int bits, int resolution)
    {
        this.seed = seed;
        this.bits = bits;
        this.resolution = resolution & 63;
        resSize = 1L << this.resolution;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public long getSeed() {
        return seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }

    public int getBits() {
        return bits;
    }

    /**
     * Sets the number of bits this will output; 8 is common to produce byte-sized values between 0 and 255.
     * This value can be between 1 and 64. If bits is 8, then this should produce values of 255 or 0, plus or minus 1.
     * If bits is some other value, then it may produce more than two values, or only produce one.
     * @param bits the number of bits of output each call should generate.
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    public int getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution, which is an exponent that determines the width/height of each zone that shares the same four
     * corners (where only the corners have their own hashed values). If resolution is 1, the size of a zone is 2x2, if
     * it is 2, then the size of a zone is 4x4, if it is 3, then 8x8, and so on by powers of 2. The resolution can be as
     * low as 0 (which won't blend corners' hashes at all) or as high as 63, but cannot easily be increased above that
     * (10 is a really large size for a cell at 1024x1024, and 63 is over 9 quintillion square). This doesn't slow
     * down significantly (or at all) if resolution is particularly high or low, but this is often between 1 and 6.
     * @param resolution an int between 0 and 63
     */
    public void setResolution(int resolution) {
        this.resolution = resolution & 63;
        resSize = 1L << this.resolution;
    }

    private static long lorp(long start, long end, long a, long resolution) {
        return ((1L << resolution) - a) * start + a * end >>> resolution;
    }

    /**
     * 2D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     */
    public long noise2D(long x, long y)
    {
        return noise2D(x, y, seed, resolution, bits);
    }

    /**
     * 2D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param seed the seed to use to alter the generated noise
     */
    public long noise2D(long x, long y, long seed)
    {
        return noise2D(x, y, seed, resolution, bits);
    }

    /**
     * 3D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     */
    public long noise3D(long x, long y, long z)
    {
        return noise3D(x, y, z, seed, resolution, bits);
    }

    /**
     * 4D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param w w input
     */
    public long noise4D(long x, long y, long z, long w)
    {
        return noise4D(x, y, z, w, seed, resolution, bits);
    }

    /**
     * 3D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param seed the seed to use to alter the generated noise
     */
    public long noise3D(long x, long y, long z, long seed)
    {
        return noise3D(x, y, z, seed, resolution, bits);
    }

    /**
     * 4D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param w w input
     * @param seed the seed to use to alter the generated noise
     */
    public long noise4D(long x, long y, long z, long w, long seed)
    {
        return noise4D(x, y, z, w, seed, resolution, bits);
    }

    /**
     * 2D bit noise; black and white much of the time but curving instead of angular.
     *
     * @param x x input
     * @param y y input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise2D(long x, long y, long state, int resolution, int bits) {
        long xb = (x >> resolution) + (0xC13FA9A902A6328FL ^ state), yb = (y >> resolution) + (0x91E10DA5C79E7B1DL ^ state),
                xr = (x & ~(-1L << resolution)), yr = (y & ~(-1L << resolution)),
                x0 = Hasher.randomize3(xb), x1 = Hasher.randomize3(xb + 1),
                y0 = Hasher.randomize3(yb), y1 = Hasher.randomize3(yb + 1),
                x0y0 = (x0 * y0 ^ x0 + y0) >>> resolution, x1y0 = (x1 * y0 ^ x1 + y0) >>> resolution,
                x0y1 = (x0 * y1 ^ x0 + y1) >>> resolution, x1y1 = (x1 * y1 ^ x1 + y1) >>> resolution;
        return lorp(lorp(x0y0, x1y0, xr, resolution), lorp(x0y1, x1y1, xr, resolution), yr, resolution)
                >>> -resolution - bits; // >> (- bits - resolution & 63)
    }

    /**
     * 3D bit noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise3D(long x, long y, long z, long state, int resolution, int bits) {
        long xb = (x >> resolution) + (0xD1B54A32D192ED03L ^ state), yb = (y >> resolution) + (0xABC98388FB8FAC03L ^ state), zb = (z >> resolution) + (0x8CB92BA72F3D8DD7L ^ state),
                xr = x & ~(-1L << resolution), yr = y & ~(-1L << resolution), zr = z & ~(-1L << resolution),
                x0 = Hasher.randomize3(xb), x1 = Hasher.randomize3(xb + 1),
                y0 = Hasher.randomize3(yb), y1 = Hasher.randomize3(yb + 1),
                z0 = Hasher.randomize3(zb), z1 = Hasher.randomize3(zb + 1),
                x0y0z0 = (x0 * y0 * z0 ^ x0 + y0 + z0) >>> resolution, x1y0z0 = (x1 * y0 * z0 ^ x1 + y0 + z0) >>> resolution,
                x0y1z0 = (x0 * y1 * z0 ^ x0 + y1 + z0) >>> resolution, x1y1z0 = (x1 * y1 * z0 ^ x1 + y1 + z0) >>> resolution,
                x0y0z1 = (x0 * y0 * z1 ^ x0 + y0 + z1) >>> resolution, x1y0z1 = (x1 * y0 * z1 ^ x1 + y0 + z1) >>> resolution,
                x0y1z1 = (x0 * y1 * z1 ^ x0 + y1 + z1) >>> resolution, x1y1z1 = (x1 * y1 * z1 ^ x1 + y1 + z1) >>> resolution;

        return lorp(lorp(lorp(x0y0z0, x1y0z0, xr, resolution), lorp(x0y1z0, x1y1z0, xr, resolution), yr, resolution),
                lorp(lorp(x0y0z1, x1y0z1, xr, resolution), lorp(x0y1z1, x1y1z1, xr, resolution), yr, resolution), zr, resolution)
                >>> -resolution - bits;
    }


    /**
     * 4D bit noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param w w input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise4D(long x, long y, long z, long w, long state, int resolution, int bits) {
        long xb = (x >> resolution) + (0xDB4F0B9175AE2165L ^ state), yb = (y >> resolution) + (0xBBE0563303A4615FL ^ state), zb = (z >> resolution) + (0xA0F2EC75A1FE1575L ^ state), wb = (w >> resolution) + (0x89E182857D9ED689L ^ state),
                xr = x & ~(-1L << resolution), yr = y & ~(-1L << resolution), zr = z & ~(-1L << resolution), wr = w & ~(-1L << resolution),
                x0 = Hasher.randomize3(xb), x1 = Hasher.randomize3(xb + 1),
                y0 = Hasher.randomize3(yb), y1 = Hasher.randomize3(yb + 1),
                z0 = Hasher.randomize3(zb), z1 = Hasher.randomize3(zb + 1),
                w0 = Hasher.randomize3(wb), w1 = Hasher.randomize3(wb + 1),
                x0y0z0w0 = (x0 * y0 * z0 * w0 ^ x0 + y0 + z0 + w0) >>> resolution, x1y0z0w0 = (x1 * y0 * z0 * w0 ^ x1 + y0 + z0 + w0) >>> resolution,
                x0y1z0w0 = (x0 * y1 * z0 * w0 ^ x0 + y1 + z0 + w0) >>> resolution, x1y1z0w0 = (x1 * y1 * z0 * w0 ^ x1 + y1 + z0 + w0) >>> resolution,
                x0y0z1w0 = (x0 * y0 * z1 * w0 ^ x0 + y0 + z1 + w0) >>> resolution, x1y0z1w0 = (x1 * y0 * z1 * w0 ^ x1 + y0 + z1 + w0) >>> resolution,
                x0y1z1w0 = (x0 * y1 * z1 * w0 ^ x0 + y1 + z1 + w0) >>> resolution, x1y1z1w0 = (x1 * y1 * z1 * w0 ^ x1 + y1 + z1 + w0) >>> resolution,
                x0y0z0w1 = (x0 * y0 * z0 * w1 ^ x0 + y0 + z0 + w1) >>> resolution, x1y0z0w1 = (x1 * y0 * z0 * w1 ^ x1 + y0 + z0 + w1) >>> resolution,
                x0y1z0w1 = (x0 * y1 * z0 * w1 ^ x0 + y1 + z0 + w1) >>> resolution, x1y1z0w1 = (x1 * y1 * z0 * w1 ^ x1 + y1 + z0 + w1) >>> resolution,
                x0y0z1w1 = (x0 * y0 * z1 * w1 ^ x0 + y0 + z1 + w1) >>> resolution, x1y0z1w1 = (x1 * y0 * z1 * w1 ^ x1 + y0 + z1 + w1) >>> resolution,
                x0y1z1w1 = (x0 * y1 * z1 * w1 ^ x0 + y1 + z1 + w1) >>> resolution, x1y1z1w1 = (x1 * y1 * z1 * w1 ^ x1 + y1 + z1 + w1) >>> resolution;

        return lorp(
                lorp(lorp(lorp(x0y0z0w0, x1y0z0w0, xr, resolution), lorp(x0y1z0w0, x1y1z0w0, xr, resolution), yr, resolution),
                        lorp(lorp(x0y0z1w0, x1y0z1w0, xr, resolution), lorp(x0y1z1w0, x1y1z1w0, xr, resolution), yr, resolution), zr, resolution),
                lorp(lorp(lorp(x0y0z0w1, x1y0z0w1, xr, resolution), lorp(x0y1z0w1, x1y1z0w1, xr, resolution), yr, resolution),
                        lorp(lorp(x0y0z1w1, x1y0z1w1, xr, resolution), lorp(x0y1z1w1, x1y1z1w1, xr, resolution), yr, resolution), zr, resolution),
                wr, resolution) >>> -resolution - bits;
    }
    
    /**
     * 5D bit noise.
     *
     * @param x x input
     * @param y y input
     * @param z z input
     * @param w w input
     * @param u u input
     * @param state state to adjust the output
     * @param resolution the number of cells between "vertices" where one hashed value is used fully
     * @param bits how many bits should be used for each (signed long) output; often this is 8 to output a byte
     * @return noise from {@code -(1L << bits)} to {@code (1L << bits) - 1L}, both inclusive
     */
    public static long noise5D(long x, long y, long z, long w, long u, long state, int resolution, int bits) {
        long xb = (x >> resolution) + (0xE19B01AA9D42C633L ^ state),
                yb = (y >> resolution) + (0xC6D1D6C8ED0C9631L ^ state),
                zb = (z >> resolution) + (0xAF36D01EF7518DBBL ^ state),
                wb = (w >> resolution) + (0x9A69443F36F710E7L ^ state),
                ub = (u >> resolution) + (0x881403B9339BD42DL ^ state),
                xr = x & ~(-1L << resolution), yr = y & ~(-1L << resolution), zr = z & ~(-1L << resolution),
                wr = w & ~(-1L << resolution), ur = u & ~(-1L << resolution),
                x0 = Hasher.randomize3(xb), x1 = Hasher.randomize3(xb + 1),
                y0 = Hasher.randomize3(yb), y1 = Hasher.randomize3(yb + 1),
                z0 = Hasher.randomize3(zb), z1 = Hasher.randomize3(zb + 1),
                w0 = Hasher.randomize3(wb), w1 = Hasher.randomize3(wb + 1),
                u0 = Hasher.randomize3(ub), u1 = Hasher.randomize3(ub + 1),
                x0y0z0w0u0 = (x0 * y0 * z0 * w0 * u0 ^ x0 + y0 + z0 + w0 + u0) >>> resolution, x1y0z0w0u0 = (x1 * y0 * z0 * w0 * u0 ^ x1 + y0 + z0 + w0 + u0) >>> resolution,
                x0y1z0w0u0 = (x0 * y1 * z0 * w0 * u0 ^ x0 + y1 + z0 + w0 + u0) >>> resolution, x1y1z0w0u0 = (x1 * y1 * z0 * w0 * u0 ^ x1 + y1 + z0 + w0 + u0) >>> resolution,
                x0y0z1w0u0 = (x0 * y0 * z1 * w0 * u0 ^ x0 + y0 + z1 + w0 + u0) >>> resolution, x1y0z1w0u0 = (x1 * y0 * z1 * w0 * u0 ^ x1 + y0 + z1 + w0 + u0) >>> resolution,
                x0y1z1w0u0 = (x0 * y1 * z1 * w0 * u0 ^ x0 + y1 + z1 + w0 + u0) >>> resolution, x1y1z1w0u0 = (x1 * y1 * z1 * w0 * u0 ^ x1 + y1 + z1 + w0 + u0) >>> resolution,
                x0y0z0w1u0 = (x0 * y0 * z0 * w1 * u0 ^ x0 + y0 + z0 + w1 + u0) >>> resolution, x1y0z0w1u0 = (x1 * y0 * z0 * w1 * u0 ^ x1 + y0 + z0 + w1 + u0) >>> resolution,
                x0y1z0w1u0 = (x0 * y1 * z0 * w1 * u0 ^ x0 + y1 + z0 + w1 + u0) >>> resolution, x1y1z0w1u0 = (x1 * y1 * z0 * w1 * u0 ^ x1 + y1 + z0 + w1 + u0) >>> resolution,
                x0y0z1w1u0 = (x0 * y0 * z1 * w1 * u0 ^ x0 + y0 + z1 + w1 + u0) >>> resolution, x1y0z1w1u0 = (x1 * y0 * z1 * w1 * u0 ^ x1 + y0 + z1 + w1 + u0) >>> resolution,
                x0y1z1w1u0 = (x0 * y1 * z1 * w1 * u0 ^ x0 + y1 + z1 + w1 + u0) >>> resolution, x1y1z1w1u0 = (x1 * y1 * z1 * w1 * u0 ^ x1 + y1 + z1 + w1 + u0) >>> resolution,
                x0y0z0w0u1 = (x0 * y0 * z0 * w0 * u1 ^ x0 + y0 + z0 + w0 + u1) >>> resolution, x1y0z0w0u1 = (x1 * y0 * z0 * w0 * u1 ^ x1 + y0 + z0 + w0 + u1) >>> resolution,
                x0y1z0w0u1 = (x0 * y1 * z0 * w0 * u1 ^ x0 + y1 + z0 + w0 + u1) >>> resolution, x1y1z0w0u1 = (x1 * y1 * z0 * w0 * u1 ^ x1 + y1 + z0 + w0 + u1) >>> resolution,
                x0y0z1w0u1 = (x0 * y0 * z1 * w0 * u1 ^ x0 + y0 + z1 + w0 + u1) >>> resolution, x1y0z1w0u1 = (x1 * y0 * z1 * w0 * u1 ^ x1 + y0 + z1 + w0 + u1) >>> resolution,
                x0y1z1w0u1 = (x0 * y1 * z1 * w0 * u1 ^ x0 + y1 + z1 + w0 + u1) >>> resolution, x1y1z1w0u1 = (x1 * y1 * z1 * w0 * u1 ^ x1 + y1 + z1 + w0 + u1) >>> resolution,
                x0y0z0w1u1 = (x0 * y0 * z0 * w1 * u1 ^ x0 + y0 + z0 + w1 + u1) >>> resolution, x1y0z0w1u1 = (x1 * y0 * z0 * w1 * u1 ^ x1 + y0 + z0 + w1 + u1) >>> resolution,
                x0y1z0w1u1 = (x0 * y1 * z0 * w1 * u1 ^ x0 + y1 + z0 + w1 + u1) >>> resolution, x1y1z0w1u1 = (x1 * y1 * z0 * w1 * u1 ^ x1 + y1 + z0 + w1 + u1) >>> resolution,
                x0y0z1w1u1 = (x0 * y0 * z1 * w1 * u1 ^ x0 + y0 + z1 + w1 + u1) >>> resolution, x1y0z1w1u1 = (x1 * y0 * z1 * w1 * u1 ^ x1 + y0 + z1 + w1 + u1) >>> resolution,
                x0y1z1w1u1 = (x0 * y1 * z1 * w1 * u1 ^ x0 + y1 + z1 + w1 + u1) >>> resolution, x1y1z1w1u1 = (x1 * y1 * z1 * w1 * u1 ^ x1 + y1 + z1 + w1 + u1) >>> resolution;

        return lorp(
                lorp(
                        lorp(lorp(lorp(x0y0z0w0u0, x1y0z0w0u0, xr, resolution), lorp(x0y1z0w0u0, x1y1z0w0u0, xr, resolution), yr, resolution),
                                lorp(lorp(x0y0z1w0u0, x1y0z1w0u0, xr, resolution), lorp(x0y1z1w0u0, x1y1z1w0u0, xr, resolution), yr, resolution), zr, resolution),
                        lorp(lorp(lorp(x0y0z0w1u0, x1y0z0w1u0, xr, resolution), lorp(x0y1z0w1u0, x1y1z0w1u0, xr, resolution), yr, resolution),
                                lorp(lorp(x0y0z1w1u0, x1y0z1w1u0, xr, resolution), lorp(x0y1z1w1u0, x1y1z1w1u0, xr, resolution), yr, resolution), zr, resolution),
                        wr, resolution),
                lorp(
                        lorp(lorp(lorp(x0y0z0w0u1, x1y0z0w0u1, xr, resolution), lorp(x0y1z0w0u1, x1y1z0w0u1, xr, resolution), yr, resolution),
                                lorp(lorp(x0y0z1w0u1, x1y0z1w0u1, xr, resolution), lorp(x0y1z1w0u1, x1y1z1w0u1, xr, resolution), yr, resolution), zr, resolution),
                        lorp(lorp(lorp(x0y0z0w1u1, x1y0z0w1u1, xr, resolution), lorp(x0y1z0w1u1, x1y1z0w1u1, xr, resolution), yr, resolution),
                                lorp(lorp(x0y0z1w1u1, x1y0z1w1u1, xr, resolution), lorp(x0y1z1w1u1, x1y1z1w1u1, xr, resolution), yr, resolution), zr, resolution),
                        wr, resolution),
                ur, resolution) >>> -resolution - bits;
    }

    @Override
    public float getNoise(float x, float y) {
        return 1 - (noise2D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return 1 - (noise2D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return 1 - (noise3D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return 1 - (noise3D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return 1 - (noise4D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize), MathTools.longFloor(w * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return 1 - (noise4D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize), MathTools.longFloor(w * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return 1 - (noise5D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize),
                MathTools.longFloor(w * resSize), MathTools.longFloor(u * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return 1 - (noise5D(MathTools.longFloor(x * resSize), MathTools.longFloor(y * resSize), MathTools.longFloor(z * resSize),
                MathTools.longFloor(w * resSize), MathTools.longFloor(u * resSize), seed, resolution << 1, bits) * 4f) / (1 << bits);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return 0;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return INoise.super.getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 5;
    }

    @Override
    public String getTag() {
        return "BitN";
    }

    /**
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another INoise.
     * @return a String that describes this PerlinNoise for serialization
     */
    @Override
    public String stringSerialize() {
        return "`" + seed + "~" + bits + "~" + resolution + "`";
    }

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this PerlinNoise to have the
     * described state from the given String. The serialized String must have been produced by a PerlinNoise.
     *
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this PerlinNoise, after being modified (if possible)
     */
    @Override
    public BitNoise stringDeserialize(String data) {
        int pos;
        seed = Base.BASE10.readLong(data, 1, pos = data.indexOf('~'));
        bits = Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1));
        resolution = Base.BASE10.readInt(data, pos+1, data.indexOf('`', pos+1));
        return this;
    }

    public static BitNoise recreateFromString(String data) {
        int pos;
        return new BitNoise(Base.BASE10.readLong(data, 1, pos = data.indexOf('~')),
                Base.BASE10.readInt(data, pos+1, pos = data.indexOf('~', pos+1)),
                Base.BASE10.readInt(data, pos+1, data.indexOf('`', pos+1)));
    }


    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(seed);
        out.writeInt(bits);
        out.writeInt(resolution);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException {
        setSeed(in.readLong());
        setBits(in.readInt());
        setResolution(in.readInt());
    }

    @Override
    public BitNoise copy() {
        return new BitNoise(seed, bits, resolution);
    }

    @Override
    public String toString() {
        return "BitNoise{" +
                "seed=" + seed +
                ", bits=" + bits +
                ", resolution=" + resolution +
                '}';
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof BitNoise)) return false;

        BitNoise bitNoise = (BitNoise) o;
        return seed == bitNoise.seed && bits == bitNoise.bits && resolution == bitNoise.resolution;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(seed);
        result = 31 * result + bits;
        result = 31 * result + resolution;
        return result;
    }


}
