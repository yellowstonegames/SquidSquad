/*
 * Copyright (c) 2022 See AUTHORS file.
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

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.ds.HolderSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A general-purpose IGlider that can interpolate multiple values, either float or int, using any rules for the
 * interpolations (specifiable by {@link FloatInterpolator} and {@link IntInterpolator} functions).
 */
public class MultiGlider implements IGlider {
    /**
     * A named variable that can change either (or both) a float and/or an int value between start and end values.
     * Typical usage only will have reasonable results when using one type, either float or int, and will have the start
     * and end values for that type used with the appropriate interpolator by {@link #getFloat(String)} or
     * {@link #getInt(String)}. The name is important; you look up the current value for a Changer by its name.
     */
    public static class Changer {
        @Nonnull public final String name;
        public float startF;
        public float endF;
        public int startI;
        public int endI;
        @Nonnull public FloatInterpolator interpolatorF;
        @Nonnull public IntInterpolator interpolatorI;

        public Changer(@Nonnull String name, float initialF){
            this(name, initialF, initialF, FloatInterpolator.LINEAR);
        }
        public Changer(@Nonnull String name, float startF, float endF){
            this(name, startF, endF, FloatInterpolator.LINEAR);
        }

        public Changer(@Nonnull String name, float startF, float endF, @Nonnull FloatInterpolator interpolatorF) {
            this.name = name;
            this.startF = startF;
            this.endF = endF;
            this.interpolatorF = interpolatorF;
            this.interpolatorI = IntInterpolator.LINEAR;
        }

        public Changer(@Nonnull String name, int startI, int endI, @Nonnull IntInterpolator interpolatorI) {
            this.name = name;
            this.startI = startI;
            this.endI = endI;
            this.interpolatorI = interpolatorI;
            this.interpolatorF = FloatInterpolator.LINEAR;
        }

        @Nonnull
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Changer changer = (Changer) o;

            if (Float.compare(changer.startF, startF) != 0) return false;
            if (Float.compare(changer.endF, endF) != 0) return false;
            if (startI != changer.startI) return false;
            if (endI != changer.endI) return false;
            return name.equals(changer.name);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + BitConversion.floatToIntBits(startF);
            result = 31 * result + BitConversion.floatToIntBits(endF);
            result = 31 * result + startI;
            result = 31 * result + endI;
            return result;
        }
    }

    @Nonnull public final HolderSet<Changer, String> changers = new HolderSet<>(Changer::getName);
    protected float change = 0f;
    protected @Nonnull Interpolation interpolation;
    protected @Nullable Runnable completeRunner;

    public MultiGlider() {
        this.interpolation = Interpolation.linear;
    }
    public MultiGlider(Changer changer) {
        this.interpolation = Interpolation.linear;
        this.changers.add(changer);
    }

    public MultiGlider(Changer... changers) {
        this.interpolation = Interpolation.linear;
        this.changers.addAll(changers);
    }

    public MultiGlider(@Nonnull Interpolation interpolation, Changer changer) {
        this.interpolation = interpolation;
        this.changers.add(changer);
    }

    public MultiGlider(@Nonnull Interpolation interpolation, Changer... changers) {
        this.interpolation = interpolation;
        this.changers.addAll(changers);
    }

    public MultiGlider(@Nonnull Interpolation interpolation, @Nullable Runnable completeRunner, Changer changer) {
        this.interpolation = interpolation;
        this.completeRunner = completeRunner;
        this.changers.add(changer);
    }

    public MultiGlider(@Nonnull Interpolation interpolation, @Nullable Runnable completeRunner, Changer... changers) {
        this.interpolation = interpolation;
        this.completeRunner = completeRunner;
        this.changers.addAll(changers);
    }

    public MultiGlider(@Nonnull MultiGlider other) {
        this.change = other.change;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
        this.changers.addAll(other.changers);
    }

    public MultiGlider addChanger(Changer changer) {
        this.changers.add(changer);
        change = 0f;
        return this;
    }

    public MultiGlider addChangers(Changer... changers) {
        this.changers.addAll(changers);
        change = 0f;
        return this;
    }
    public MultiGlider addChangers(Collection<Changer> changers) {
        this.changers.addAll(changers);
        change = 0f;
        return this;
    }

    public MultiGlider removeChanger(String name) {
        //noinspection SuspiciousMethodCalls
        this.changers.remove(name);
        change = 0f;
        return this;
    }

    /**
     * Adds all Changers in {@code other} to this MultiGlider, and potentially changes the
     * {@link #getCompleteRunner() runner} so that it performs other's runner, if non-null, after this MultiGlider's
     * runner (or just run's other's if this has none).
     * @param other another MultiGlider to merge into this one; will not be changed, but this may share references
     * @return this, after modifications, for chaining
     */
    public MultiGlider merge(final @Nonnull MultiGlider other) {
        this.changers.addAll(other.changers);
        if(this.completeRunner == null) this.completeRunner = other.completeRunner;
        else if(other.completeRunner != null) this.completeRunner = () -> {
            this.completeRunner.run();
            other.completeRunner.run();
        };
        change = 0f;
        return this;
    }
    /**
     * Gets the current float value for the Changer with the given {@code name} by interpolating between its start and
     * end float values. The {@link #getInterpolation() interpolation} will be applied to {@link #getChange() change}
     * before it is passed to the Changer's {@link Changer#interpolatorF}. If this cannot locate a Changer by the given
     * name, this returns {@link Float#NaN}.
     * @param name the name of the Changer to look up
     * @return the current float value of the located Changer, or {@link Float#NaN} if lookup fails
     */
    public float getFloat(@Nonnull String name){
        Changer c = changers.get(name);
        if(c == null) return Float.NaN;
        return c.interpolatorF.apply(c.startF, c.endF, interpolation.apply(change));
    }

    /**
     * Gets the current int value for the Changer with the given {@code name} by interpolating between its start and
     * end int values. The {@link #getInterpolation() interpolation} will be applied to {@link #getChange() change}
     * before it is passed to the Changer's {@link Changer#interpolatorI}. If this cannot locate a Changer by the given
     * name, this returns {@link Integer#MIN_VALUE}.
     * @param name the name of the Changer to look up
     * @return the current int value of the located Changer, or {@link Integer#MIN_VALUE} if lookup fails
     */
    public int getInt(@Nonnull String name){
        Changer c = changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.interpolatorI.apply(c.startI, c.endI, interpolation.apply(change));
    }

    @Override
    public float getChange() {
        return change;
    }

    @Override
    public void setChange(float change) {
        if(this.change != (this.change = Math.max(0f, Math.min(1f, change))) && this.change == 1f) {
            onComplete();
        }
    }

    /**
     * Looks up the {@link Changer#startF start float} value for the Changer with the given {@code name}.
     * If this cannot locate a Changer by the given name, this returns {@link Float#NaN}.
     * @param name the name of the Changer to look up
     * @return the start float value of the located Changer, or {@link Float#NaN} if lookup fails
     */
    public float getStartFloat(@Nonnull String name) {
        Changer c = changers.get(name);
        if(c == null) return Float.NaN;
        return c.startF;
    }

    /**
     * Sets the {@link Changer#startF start float} value for the Changer with the given {@code name} to {@code start}.
     * If this cannot locate a Changer by the given name, this does nothing. Otherwise, sets the Changer's start float
     * value and also sets {@link #change} to 0.
     * @param name the name of the Changer to look up
     * @param start value to use for the located Changer's start float
     */
    public void setStartFloat(@Nonnull String name, float start) {
        Changer c = changers.get(name);
        if(c == null) return;
        c.startF = start;
        change = 0f;
    }
    
    /**
     * Looks up the {@link Changer#endF end float} value for the Changer with the given {@code name}.
     * If this cannot locate a Changer by the given name, this returns {@link Float#NaN}.
     * @param name the name of the Changer to look up
     * @return the end float value of the located Changer, or {@link Float#NaN} if lookup fails
     */
    public float getEndFloat(@Nonnull String name) {
        Changer c = changers.get(name);
        if(c == null) return Float.NaN;
        return c.endF;
    }

    /**
     * Sets the {@link Changer#endF end float} value for the Changer with the given {@code name} to {@code end}.
     * If this cannot locate a Changer by the given name, this does nothing. Otherwise, sets the Changer's end float
     * value and also sets {@link #change} to 0.
     * @param name the name of the Changer to look up
     * @param end value to use for the located Changer's end float
     */
    public void setEndFloat(@Nonnull String name, float end) {
        Changer c = changers.get(name);
        if(c == null) return;
        c.endF = end;
        change = 0f;
    }

    /**
     * Looks up the {@link Changer#startI start int} value for the Changer with the given {@code name}.
     * If this cannot locate a Changer by the given name, this returns {@link Integer#MIN_VALUE}.
     * @param name the name of the Changer to look up
     * @return the start int value of the located Changer, or {@link Integer#MIN_VALUE} if lookup fails
     */
    public int getStartInt(@Nonnull String name) {
        Changer c = changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.startI;
    }

    /**
     * Sets the {@link Changer#startI start int} value for the Changer with the given {@code name} to {@code start}.
     * If this cannot locate a Changer by the given name, this does nothing. Otherwise, sets the Changer's start int
     * value and also sets {@link #change} to 0.
     * @param name the name of the Changer to look up
     * @param start value to use for the located Changer's start int
     */
    public void setStartInt(@Nonnull String name, int start) {
        Changer c = changers.get(name);
        if(c == null) return;
        c.startI = start;
        change = 0f;
    }

    /**
     * Looks up the {@link Changer#endI end int} value for the Changer with the given {@code name}.
     * If this cannot locate a Changer by the given name, this returns {@link Integer#MIN_VALUE}.
     * @param name the name of the Changer to look up
     * @return the end int value of the located Changer, or {@link Integer#MIN_VALUE} if lookup fails
     */
    public int getEndInt(@Nonnull String name) {
        Changer c = changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.endI;
    }

    /**
     * Sets the {@link Changer#endI end int} value for the Changer with the given {@code name} to {@code end}.
     * If this cannot locate a Changer by the given name, this does nothing. Otherwise, sets the Changer's end int
     * value and also sets {@link #change} to 0.
     * @param name the name of the Changer to look up
     * @param end value to use for the located Changer's end int
     */
    public void setEndInt(@Nonnull String name, int end) {
        Changer c = changers.get(name);
        if(c == null) return;
        c.endI = end;
        change = 0f;
    }

    @Override
    @Nonnull
    public Interpolation getInterpolation() {
        return interpolation;
    }

    @Override
    public void setInterpolation(@Nonnull Interpolation interpolation) {
        this.interpolation = interpolation;
        change = 0f;
    }

    @Nullable
    public Runnable getCompleteRunner() {
        return completeRunner;
    }

    public void setCompleteRunner(@Nullable Runnable completeRunner) {
        this.completeRunner = completeRunner;
    }

    @Override
    public void onComplete() {
        for(Changer c : changers){
            c.startF = c.endF;
            c.startI = c.endI;
        }
        if(completeRunner != null) {
            completeRunner.run();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiGlider that = (MultiGlider) o;

        if (Float.compare(that.change, change) != 0) return false;
        return changers.equals(that.changers);
    }

    @Override
    public int hashCode() {
        int result = changers.hashCode();
        result = 31 * result + BitConversion.floatToIntBits(change);
        return result;
    }
}
