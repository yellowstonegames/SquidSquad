package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Collections;
import java.util.List;

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
public class MutantNoise implements INoise, ISerializersNeeded {
    /**
     * The INoise this uses for all its internal noise generation.
     * This defaults to a FoamNoise, but can be any INoise with {@link INoise#getMaxDimension()} greater than 2.
     */
    public INoise basis;
    /**
     * The extra input that is passed to {@link #basis}, in addition to the inputs to each noise call.
     * This typically changes only be a small amount at a time, to make minor continuous adjustments to a noise result,
     * instead of using {@link #setSeed(long)} to make a non-continuous adjustment that "jumps." This defaults to 0.
     */
    public float mutation = 0f;

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with 1234567890L) and a mutation of 0.0f .
     */
    public MutantNoise() {
        this(1234567890L);
    }

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with the given seed) and a mutation of 0.0f.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     */
    public MutantNoise(long seed)
    {
        this(new FoamNoise(seed), 0f);
    }

    /**
     * Makes a MutantNoise with a {@link FoamNoise} basis (seeded with the given seed) and the given mutation.
     * @param seed the seed to use for any noise call that doesn't take a seed already
     * @param mutation the initial value for the extra input to be passed in addition to the inputs to each noise call
     */
    public MutantNoise(long seed, float mutation)
    {
        this(new FoamNoise(seed), mutation);
    }

    /**
     * Makes a MutantNoise with the given basis INoise and a mutation of 0.0f.
     * @param base the INoise to use as a basis
     */
    public MutantNoise(INoise base)
    {
        this(base, 0f);
    }

    /**
     * Makes a MutantNoise with the given basis INoise and mutation.
     * @param base the INoise to use as a basis
     * @param mutation the initial value for the extra input to be passed in addition to the inputs to each noise call
     */
    public MutantNoise(INoise base, float mutation)
    {
        this.basis = base;
        this.mutation = mutation;
    }

    /**
     * Minimum dimension is 2, unless the basis INoise has a minimum dimension of 4 or greater.
     * With the default basis, a FoamNoise, this will be 2.
     * @return 2, in almost all cases
     */
    @Override
    public int getMinDimension() {
        return Math.min(basis.getMaxDimension() - 1, Math.max(2, basis.getMinDimension() - 1));
    }

    /**
     * Maximum dimension is one less than the maximum dimension of the basis INoise. With the default basis, a
     * FoamNoise, this will be 6.
     * @return one less than {@code basis.getMaxDimension()}
     */
    @Override
    public int getMaxDimension() {
        return basis.getMaxDimension() - 1;
    }

    /**
     * Setting the seed is efficient here if and only if it is efficient for the basis INoise.
     * @return {@code basis.hasEfficientSetSeed()}
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return basis.hasEfficientSetSeed();
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
     * The String "Muta" .
     * @return "Muta"
     */
    @Override
    public String getTag() {
        return "Muta";
    }

    @Override
    public String stringSerialize() {
        return "`" + mutation + "~" + Serializer.serialize(basis) + "`";
    }

    @Override
    public MutantNoise stringDeserialize(String data) {
        int idx = 2;
        mutation = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        basis = Serializer.deserialize(data.substring(idx+1, data.lastIndexOf('`')-1));
        return this;
    }

    public static MutantNoise recreateFromString(String data) {
        int idx = 2;
        float mutation = Base.BASE10.readFloat(data, 1, idx = data.indexOf('~', idx));
        INoise basis = Serializer.deserialize(data.substring(idx+1, data.lastIndexOf('`')-1));
        return new MutantNoise(basis, mutation);

    }

    @GwtIncompatible
    @Override
    public List<Class<?>> getSerializersNeeded() {
        return Collections.singletonList(basis.getClass());
    }

    @Override
    public MutantNoise copy() {
        return new MutantNoise(basis.copy(), mutation);
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
