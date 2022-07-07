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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Runs more than one MultiGlider in a sequence. Each individual MultiGlider will run one after the next, so it is
 * likely that you will want to use a sequence of all the same type of MultiGlider (that is, all can have their "x"
 * or "color" coordinate queried and have it mean the same thing for any member of the sequence, for example).
 */
@Beta
public class MultiSequenceGlider extends MultiGlider {
    protected MultiGlider[] sequence;
    protected float[] durations;
    protected int active = 0;
    protected float passed = 0f;

    protected MultiSequenceGlider(){
        sequence = null;
        durations = null;
    }
    public MultiSequenceGlider(MultiGlider[] gliders, float[] lengths){
        this(gliders, lengths, Interpolation.linear, null);
    }
    public MultiSequenceGlider(MultiGlider[] gliders, float[] lengths, Interpolation interpolation,
                               @Nullable Runnable completeRunner){
        this.interpolation = interpolation;
        this.completeRunner = completeRunner;
        final int len = Math.min(gliders.length, lengths.length);
        sequence = Arrays.copyOf(gliders, len);
        durations = Arrays.copyOf(lengths, len);
        float sum = 0f;
        for (int i = 0; i < len; i++) {
            sum += durations[i];
        }
        if(sum > 0) {
            for (int i = 0; i < len; i++) {
                durations[i] /= sum;
            }
        }
    }

    @Override
    public float getFloat(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.interpolatorF.apply(c.startF, c.endF, interpolation.apply(sequence[active].getChange()));
    }

    @Override
    public int getInt(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.interpolatorI.apply(c.startI, c.endI, interpolation.apply(sequence[active].getChange()));
    }

    @Override
    public float getStartFloat(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.startF;
    }

    @Override
    public void setStartFloat(@Nonnull String name, float start) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.startF = start;
        change = 0f;
        passed = 0f;
    }

    @Override
    public float getEndFloat(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.endF;
    }

    @Override
    public void setEndFloat(@Nonnull String name, float end) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.endF = end;
        change = 0f;
        passed = 0f;
    }

    @Override
    public int getStartInt(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.startI;
    }

    @Override
    public void setStartInt(@Nonnull String name, int start) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.startI = start;
        change = 0f;
        passed = 0f;
    }

    @Override
    public int getEndInt(@Nonnull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.endI;
    }

    @Override
    public void setEndInt(@Nonnull String name, int end) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.endI = end;
        change = 0f;
        passed = 0f;
    }

    @Override
    public void setChange(float change) {
        this.change = Math.max(0f, Math.min(1f, change));
        if(active < sequence.length) {
            sequence[active].setChange((this.change - passed) / durations[active]);
            if (sequence[active].getChange() == 1f) {
                passed += durations[active];
                ++active;
            }
            if(this.change == 1f) {
                passed = 0f;
                active = sequence.length;
                onComplete();
            }
        }
    }

    @Override
    @Nonnull
    public Interpolation getInterpolation() {
        return interpolation;
    }

    @Override
    public void setInterpolation(@Nonnull Interpolation interpolation) {
        super.setInterpolation(interpolation);
        active = 0;
        passed = 0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiSequenceGlider that = (MultiSequenceGlider) o;

        if (Float.compare(that.change, change) != 0) return false;
        if (active != that.active) return false;
        if (Float.compare(that.passed, passed) != 0) return false;
        if (!interpolation.equals(that.interpolation)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(sequence, that.sequence)) return false;
        return Arrays.equals(durations, that.durations);
    }

    @Override
    public int hashCode() {
        int result = BitConversion.floatToIntBits(change);
        result = 31 * result + interpolation.hashCode();
        result = 31 * result + Hasher.haagenti.hash(sequence);
        result = 31 * result + Hasher.haagenti.hash(durations);
        result = 31 * result + active;
        result = 31 * result + BitConversion.floatToIntBits(passed);
        return result;
    }

    @Override
    public String toString() {
        return "MultiSequenceGlider{" +
                "sequence=" + Arrays.toString(sequence) +
                ", durations=" + Arrays.toString(durations) +
                ", active=" + active +
                ", passed=" + passed +
                ", changers=" + changers +
                ", change=" + change +
                '}';
    }
}
