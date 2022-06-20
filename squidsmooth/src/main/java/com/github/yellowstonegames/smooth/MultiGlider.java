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
import com.github.tommyettinger.ds.HolderSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    }
    @Nonnull public final HolderSet<Changer, String> changers = new HolderSet<>(Changer::getName);
    protected float change = 0f;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected @Nullable Runnable completeRunner;

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

}
