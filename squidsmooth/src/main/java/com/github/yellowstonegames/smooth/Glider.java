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

package com.github.yellowstonegames.smooth;

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.ds.HolderSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;

/**
 * A general-purpose group of smoothly-changing values, either float or int, using any rules for the
 * smooth-movement interpolations (specifiable by {@link FloatSmoother} and {@link IntSmoother} functions).
 */
public class Glider {
    /**
     * A named variable that can change either (or both) a float and/or an int value between start and end values.
     * Typical usage only will have reasonable results when using one type, either float or int, and will have the start
     * and end values for that type used with the appropriate interpolator by {@link #getFloat(String)} or
     * {@link #getInt(String)}. The name is important; you look up the current value for a Changer by its name.
     */
    public static class Changer {
        @NotNull public final String name;
        public float startF;
        public float endF;
        public int startI;
        public int endI;
        @NotNull public FloatSmoother interpolatorF;
        @NotNull public IntSmoother interpolatorI;

        public Changer(@NotNull String name, float initialF){
            this(name, initialF, initialF, FloatSmoother.LINEAR);
        }
        public Changer(@NotNull String name, float startF, float endF){
            this(name, startF, endF, FloatSmoother.LINEAR);
        }

        public Changer(@NotNull String name, float startF, float endF, @NotNull FloatSmoother interpolatorF) {
            this.name = name;
            this.startF = startF;
            this.endF = endF;
            this.interpolatorF = interpolatorF;
            this.interpolatorI = IntSmoother.LINEAR;
        }

        public Changer(@NotNull String name, int startI, int endI, @NotNull IntSmoother interpolatorI) {
            this.name = name;
            this.startI = startI;
            this.endI = endI;
            this.interpolatorI = interpolatorI;
            this.interpolatorF = FloatSmoother.LINEAR;
        }

        @NotNull
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

    @NotNull public final HolderSet<Changer, String> changers = new HolderSet<>(Changer::getName);
    protected float change = 0f;
    protected Interpolations.@NotNull Interpolator interpolation;
    protected @Nullable Runnable completeRunner;

    public Glider() {
        this.interpolation = Interpolations.linear;
    }
    public Glider(Changer changer) {
        this.interpolation = Interpolations.linear;
        this.changers.add(changer);
    }

    public Glider(Changer... changers) {
        this.interpolation = Interpolations.linear;
        this.changers.addAll(changers);
    }

    public Glider(Interpolations.@NotNull Interpolator interpolation, Changer changer) {
        this.interpolation = interpolation;
        this.changers.add(changer);
    }

    public Glider(Interpolations.@NotNull Interpolator interpolation, Changer... changers) {
        this.interpolation = interpolation;
        this.changers.addAll(changers);
    }

    public Glider(Interpolations.@NotNull Interpolator interpolation, @Nullable Runnable completeRunner, Changer changer) {
        this.interpolation = interpolation;
        this.completeRunner = completeRunner;
        this.changers.add(changer);
    }

    public Glider(Interpolations.@NotNull Interpolator interpolation, @Nullable Runnable completeRunner, Changer... changers) {
        this.interpolation = interpolation;
        this.completeRunner = completeRunner;
        this.changers.addAll(changers);
    }

    public Glider(@NotNull Glider other) {
        this.change = other.change;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
        this.changers.addAll(other.changers);
    }

    public Glider addChanger(Changer changer) {
        this.changers.add(changer);
        change = 0f;
        return this;
    }

    public Glider addChangers(Changer... changers) {
        this.changers.addAll(changers);
        change = 0f;
        return this;
    }
    public Glider addChangers(Collection<Changer> changers) {
        this.changers.addAll(changers);
        change = 0f;
        return this;
    }

    public Glider removeChanger(String name) {
        //noinspection SuspiciousMethodCalls
        this.changers.remove(name);
        change = 0f;
        return this;
    }

    /**
     * Adds all Changers in {@code other} to this Glider, and potentially changes the
     * {@link #getCompleteRunner() runner} so that it performs other's runner, if non-null, after this Glider's
     * runner (or just run's other's if this has none).
     * @param other another Glider to merge into this one; will not be changed, but this may share references
     * @return this, after modifications, for chaining
     */
    public Glider merge(final @NotNull Glider other) {
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
    public float getFloat(@NotNull String name){
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
    public int getInt(@NotNull String name){
        Changer c = changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.interpolatorI.apply(c.startI, c.endI, interpolation.apply(change));
    }

    public float getChange() {
        return change;
    }

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
    public float getStartFloat(@NotNull String name) {
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
    public void setStartFloat(@NotNull String name, float start) {
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
    public float getEndFloat(@NotNull String name) {
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
    public void setEndFloat(@NotNull String name, float end) {
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
    public int getStartInt(@NotNull String name) {
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
    public void setStartInt(@NotNull String name, int start) {
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
    public int getEndInt(@NotNull String name) {
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
    public void setEndInt(@NotNull String name, int end) {
        Changer c = changers.get(name);
        if(c == null) return;
        c.endI = end;
        change = 0f;
    }

    public Interpolations.@NotNull Interpolator getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolations.@NotNull Interpolator interpolation) {
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

    public void onComplete() {
        if(completeRunner != null) {
            completeRunner.run();
        }
    }

    public void resetToCurrent() {
        for(Changer c : changers){
            c.startF = c.interpolatorF.apply(c.startF, c.endF, change);
            c.startI = c.interpolatorI.apply(c.startI, c.endI, change);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Glider that = (Glider) o;

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
