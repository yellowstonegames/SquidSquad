package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.yellowstonegames.core.annotations.Beta;

@Beta
public class MutantNoise implements INoise {

    public FoamNoise basis;
    public float mutation = 0f;

    public MutantNoise() {
        this(1234567890L);
    }

    public MutantNoise(long seed)
    {
        this.basis = new FoamNoise(seed);
    }

    public MutantNoise(long seed, float mutation)
    {
        this.basis = new FoamNoise(seed);
        this.mutation = mutation;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

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

    public float getMutation() {
        return mutation;
    }

    public void setMutation(float mutation) {
        this.mutation = mutation;
    }

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
