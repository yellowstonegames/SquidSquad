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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.Interpolations.Interpolator;
import com.github.yellowstonegames.core.annotations.Beta;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import java.util.Arrays;

/**
 * Runs more than one Glider in a sequence. Each individual Glider will run one after the next, so it is
 * likely that you will want to use a sequence of all the same type of Glider (that is, all can have their "x"
 * or "color" coordinate queried and have it mean the same thing for any member of the sequence, for example).
 */
@Beta
public class SequenceGlider extends Glider {
    protected Glider[] sequence;
    protected float[] durations;
    protected int active = 0;
    protected float passed = 0f;

    protected SequenceGlider(){
        sequence = null;
        durations = null;
    }
    public SequenceGlider(Glider[] gliders, float[] lengths){
        this(gliders, lengths, Interpolations.linear, null);
    }
    public SequenceGlider(Glider[] gliders, float[] lengths, @NonNull Interpolator interpolation,
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
    public float getFloat(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.interpolatorF.apply(c.startF, c.endF, interpolation.apply(sequence[active].getChange()));
    }

    @Override
    public int getInt(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.interpolatorI.apply(c.startI, c.endI, interpolation.apply(sequence[active].getChange()));
    }

    @Override
    public float getStartFloat(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.startF;
    }

    @Override
    public void setStartFloat(@NonNull String name, float start) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.startF = start;
        change = 0f;
        passed = 0f;
    }

    @Override
    public float getEndFloat(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Float.NaN;
        return c.endF;
    }

    @Override
    public void setEndFloat(@NonNull String name, float end) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.endF = end;
        change = 0f;
        passed = 0f;
    }

    @Override
    public int getStartInt(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.startI;
    }

    @Override
    public void setStartInt(@NonNull String name, int start) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return;
        c.startI = start;
        change = 0f;
        passed = 0f;
    }

    @Override
    public int getEndInt(@NonNull String name) {
        Changer c = sequence[active].changers.get(name);
        if(c == null) return Integer.MIN_VALUE;
        return c.endI;
    }

    @Override
    public void setEndInt(@NonNull String name, int end) {
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
    public @NonNull Interpolator getInterpolation() {
        return interpolation;
    }

    @Override
    public void setInterpolation(@NonNull Interpolator interpolation) {
        super.setInterpolation(interpolation);
        active = 0;
        passed = 0f;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceGlider that = (SequenceGlider) o;

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
        return "SequenceGlider{" +
                "sequence=" + Arrays.toString(sequence) +
                ", durations=" + Arrays.toString(durations) +
                ", active=" + active +
                ", passed=" + passed +
                ", changers=" + changers +
                ", change=" + change +
                '}';
    }
}
