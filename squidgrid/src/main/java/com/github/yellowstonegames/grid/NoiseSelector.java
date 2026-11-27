/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.yellowstonegames.core.ISerializersNeeded;
import com.github.yellowstonegames.core.annotations.GwtIncompatible;

import java.util.Arrays;
import java.util.List;

/**
 * Switches between two possible {@link INoise} objects based on the result of a third compared to a threshold.
 */
public class NoiseSelector implements INoise, ISerializersNeeded {
    public INoise highNoise;
    public INoise lowNoise;
    public INoise selector;
    public float threshold;

    /**
     * The same as calling {@code new NoiseSelector(new SimplexNoise(123), new PerlinNoise(1234), new FoamNoise(12345), 0.1f)}.
     */
    public NoiseSelector(){
        this(new SimplexNoise(123), new PerlinNoise(1234), new FoamNoise(12345), 0.1f);
    }

    /**
     * Creates a NoiseSelector that selects between {@code highNoise} or {@code lowNoise} based on the output of
     * {@code selector}; this chooses highNoise when selector returns {@code threshold} or greater.
     * @param highNoise an INoise, such as a {@link Noise} or {@link SimplexNoise}, to use when selector produces high results
     * @param lowNoise an INoise, such as a {@link Noise} or {@link SimplexNoise}, to use when selector produces low results
     * @param selector an INoise, such as a {@link Noise} or {@link SimplexNoise}, to choose whether to use highNoise or lowNoise
     * @param threshold generally between -1 and 1, this is the minimum result selector produces that will select highNoise
     */
    public NoiseSelector(INoise highNoise, INoise lowNoise, INoise selector, float threshold) {
        this.highNoise = highNoise;
        this.lowNoise = lowNoise;
        this.selector = selector;
        this.threshold = threshold;
    }

    /**
     * Copies the given NoiseSelector, using {@link INoise#copy()} to deep-copy the wrapped INoise values.
     * @param other another NoiseSelector to copy
     */
    public NoiseSelector(NoiseSelector other) {
        this.highNoise = other.highNoise.copy();
        this.highNoise = other.lowNoise.copy();
        this.highNoise = other.selector.copy();
        this.threshold = other.threshold;
    }

    public INoise getHighNoise() {
        return highNoise;
    }

    public NoiseSelector setHighNoise(INoise highNoise) {
        this.highNoise = highNoise;
        return this;
    }

    public INoise getLowNoise() {
        return lowNoise;
    }

    public NoiseSelector setLowNoise(INoise lowNoise) {
        this.lowNoise = lowNoise;
        return this;
    }

    public INoise getSelector() {
        return selector;
    }

    public NoiseSelector setSelector(INoise selector) {
        this.selector = selector;
        return this;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    @Override
    public int getMinDimension() {
        return Math.max(highNoise.getMinDimension(), Math.max(lowNoise.getMinDimension(), selector.getMinDimension()));
    }

    @Override
    public int getMaxDimension() {
        return Math.min(highNoise.getMaxDimension(), Math.min(lowNoise.getMaxDimension(), selector.getMaxDimension()));
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return highNoise.hasEfficientSetSeed() && lowNoise.hasEfficientSetSeed() && selector.hasEfficientSetSeed();
    }

    @Override
    public NoiseSelector copy() {
        return new NoiseSelector(this);
    }

    @Override
    public float getNoise(float x, float y) {
        return selector.getNoise(x, y) >= threshold
            ? highNoise.getNoise(x, y)
            : lowNoise.getNoise (x, y);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return selector.getNoise(x, y, z) >= threshold
            ? highNoise.getNoise(x, y, z)
            : lowNoise.getNoise (x, y, z);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return selector.getNoise(x, y, z, w) >= threshold
            ? highNoise.getNoise(x, y, z, w)
            : lowNoise.getNoise (x, y, z, w);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return selector.getNoise(x, y, z, w, u) >= threshold
            ? highNoise.getNoise(x, y, z, w, u)
            : lowNoise.getNoise (x, y, z, w, u);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return selector.getNoise(x, y, z, w, u, v) >= threshold
            ? highNoise.getNoise(x, y, z, w, u, v)
            : lowNoise.getNoise (x, y, z, w, u, v);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v, float m) {
        return selector.getNoise(x, y, z, w, u, v, m) >= threshold
            ? highNoise.getNoise(x, y, z, w, u, v, m)
            : lowNoise.getNoise (x, y, z, w, u, v, m);
    }

    @Override
    public void setSeed(long seed) {
        highNoise.setSeed(seed);
    }

    @Override
    public long getSeed() {
        return highNoise.getSeed();
    }

    @Override
    public float getNoiseWithSeed(float x, float y, long seed) {
        return selector.getNoiseWithSeed(x, y, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, seed)
            : lowNoise.getNoiseWithSeed (x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, long seed) {
        return selector.getNoiseWithSeed(x, y, z, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, z, seed)
            : lowNoise.getNoiseWithSeed (x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
        return selector.getNoiseWithSeed(x, y, z, w, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, z, w, seed)
            : lowNoise.getNoiseWithSeed (x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
        return selector.getNoiseWithSeed(x, y, z, w, u, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, z, w, u, seed)
            : lowNoise.getNoiseWithSeed (x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
        return selector.getNoiseWithSeed(x, y, z, w, u, v, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, z, w, u, v, seed)
            : lowNoise.getNoiseWithSeed (x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, float m, long seed) {
        return selector.getNoiseWithSeed(x, y, z, w, u, v, m, seed) >= threshold
            ? highNoise.getNoiseWithSeed(x, y, z, w, u, v, m, seed)
            : lowNoise.getNoiseWithSeed (x, y, z, w, u, v, m, seed);
    }

    /**
     * @return "NSlc"
     */
    @Override
    public String getTag() {
        return "NSlc";
    }

    /**
     * Produces a String that describes everything needed to recreate this INoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another INoise. The syntax here
     * should always start and end with the {@code `} character, which is used by
     * {@link Serializer#deserialize(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another INoise that this uses.
     *
     * @return a String that describes this INoise for serialization
     */
    @Override
    public String stringSerialize() {
        return "`" + Serializer.serialize(highNoise) + '~' + Serializer.serialize(lowNoise) + '~' + Serializer.serialize(selector) + "~" + threshold + "`";
    }

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this INoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     *
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this INoise, after being modified (if possible)
     */
    @Override
    public NoiseSelector stringDeserialize(String data) {
        int pos = data.indexOf('`', data.indexOf('`', 2) + 1)+1;
        setHighNoise(Serializer.deserialize(data.substring(1, pos)));
        setLowNoise(Serializer.deserialize(data.substring(pos+1, pos = data.indexOf('`', data.indexOf('`', pos+2)+1)+1)));
        setSelector(Serializer.deserialize(data.substring(pos+1, pos = data.indexOf('`', data.indexOf('`', pos+2)+1)+1)));
        setThreshold(Base.BASE10.readFloat(data, pos+1, data.indexOf('`', pos + 1)));
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof NoiseSelector)) return false;

        NoiseSelector that = (NoiseSelector) o;
        return Float.compare(threshold, that.threshold) == 0 && highNoise.equals(that.highNoise) && lowNoise.equals(that.lowNoise) && selector.equals(that.selector);
    }

    @Override
    public int hashCode() {
        int result = highNoise.hashCode();
        result = 31 * result + lowNoise.hashCode();
        result = 31 * result + selector.hashCode();
        result = 31 * result + BitConversion.floatToIntBits(threshold);
        return result;
    }

    @Override
    @GwtIncompatible
    public List<Class<?>> getSerializersNeeded() {
        return Arrays.asList(highNoise.getClass(), lowNoise.getClass(), selector.getClass());
    }
}
