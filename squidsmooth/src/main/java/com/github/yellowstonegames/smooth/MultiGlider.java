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

    public float getFloat(@Nonnull String name){
        Changer c = changers.get(name);
        if(c == null) return Float.NaN;
        return c.interpolatorF.apply(c.startF, c.endF, interpolation.apply(change));
    }

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
