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
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Runs more than one Glider in a sequence. Meant to be subclassed with a specific Glider type for T.
 * @param <T> an IGlider type
 */
@Beta
public class SequenceGlider<T extends IGlider> implements IGlider {
    protected float change = 0f;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected T[] sequence;
    protected float[] durations;
    protected int active = 0;
    protected float passed = 0f;
    protected @Nullable Runnable completeRunner;

    protected SequenceGlider(){
        sequence = null;
        durations = null;
    }
    public SequenceGlider(T[] gliders, float[] lengths){
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
    public float getChange() {
        return change;
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
            if(active == sequence.length) {
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
        this.interpolation = interpolation;
        change = 0f;
        active = 0;
        passed = 0f;
    }

    @Override
    public void onComplete() {
        if(completeRunner != null)
            completeRunner.run();
    }

    @Nullable
    public Runnable getCompleteRunner() {
        return completeRunner;
    }

    public void setCompleteRunner(@Nullable Runnable completeRunner) {
        this.completeRunner = completeRunner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceGlider<?> that = (SequenceGlider<?>) o;

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
}
