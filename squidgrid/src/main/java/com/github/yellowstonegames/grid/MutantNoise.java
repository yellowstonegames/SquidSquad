package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.yellowstonegames.core.annotations.Beta;

/**
 * A variant on {@link FoamNoise} that allows an extra degree of freedom to adjust the inputs by a small amount,
 * continuously rather than in jumps (as adjusting the seed would do). You can change the {@link #mutation} by any
 * amount at a time, though usually it is a small change you can preview to see how each adjustment affects some result.
 * <br>
 * Internally this works by calling the same method you call here on an internal FoamNoise, {@link #basis}, but using
 * a higher dimension and passing {@link #mutation} in as that higher-dimensional input. This means if you call
 * {@link #getNoise(float, float)}, passing x and y, internally this calls
 * {@link FoamNoise#getNoise(float, float, float)} and passes x, y, and mutation.
 */
@Beta
public class MutantNoise implements INoise {
    /**
     * The FoamNoise this uses for all its internal noise generation.
     * This can be changed to a subclass of FoamNoise if you want to assign one here.
     */
    public FoamNoise basis;
    /**
     * The extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     */
    public float mutation = 0f;

    /**
     * Makes a MutantNoise with a default seed, 1234567890L, and a mutation of 0.0f .
     */
    public MutantNoise() {
        this(1234567890L);
    }

    /**
     * Makes a MutantNoise with the given seed and a mutation of 0.0f.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     */
    public MutantNoise(long seed)
    {
        this.basis = new FoamNoise(seed);
    }

    /**
     * Makes a MutantNoise with the given seed and mutation.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     * @param mutation the initial value for the extra input to be passed in addition to the inputs to each noise call
     */
    public MutantNoise(long seed, float mutation)
    {
        this.basis = new FoamNoise(seed);
        this.mutation = mutation;
    }

    /**
     * Minimum dimension is 2.
     * @return 2
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Maximum dimension is 6.
     * @return 6
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Setting the seed is efficient here.
     * @return true
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public float getNoise(float x, float y) {
        return basis.getNoise(x, y, mutation);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return basis.getNoise(x, y, z, mutation);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return basis.getNoise(x, y, z, w, mutation);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return basis.getNoise(x, y, z, w, u, mutation);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return basis.getNoise(x, y, z, w, u, v, mutation);
    }

    @Override
    public void setSeed(long seed) {
        basis.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return basis.getSeed();
    }

    /**
     * Gets the mutation value.
     * The mutation is an extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     */
    public float getMutation() {
        return mutation;
    }

    /**
     * Sets the mutation value.
     * The mutation is an extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps."
     * @param mutation the float value to use for the mutation
     */
    public void setMutation(float mutation) {
        this.mutation = mutation;
    }

    /**
     * The String "MutN" .
     * @return "MutN"
     */
    @Override
    public String getTag() {
        return "MutN";
    }

    @Override
    public String stringSerialize() {
        return "`" + basis.getSeed() + "~" + mutation + "`";
    }

    @Override
    public MutantNoise stringDeserialize(String data) {
        int idx = 2;
        basis.setSeed(Base.BASE10.readLong(data, 1, idx = data.indexOf('~', idx)));
        mutation = Base.BASE10.readFloat(data, idx + 1, data.indexOf('`', idx + 1));
        return this;
    }

    public static MutantNoise recreateFromString(String data) {
        int idx = 2;
        return new MutantNoise(Base.BASE10.readLong(data, 1, idx = data.indexOf('~', idx)),
                Base.BASE10.readFloat(data, idx + 1, data.indexOf('`', idx + 1)));

    }

    @Override
    public MutantNoise copy() {
        return new MutantNoise(basis.getSeed(), mutation);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return basis.getNoiseWithSeed(x, y, mutation, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return basis.getNoiseWithSeed(x, y, z, mutation, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, mutation, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, u, mutation, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return basis.getNoiseWithSeed(x, y, z, w, u, v, mutation, seed);
    }
}
