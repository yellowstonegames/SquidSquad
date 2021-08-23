package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * Runs more than one Glider in a sequence. Meant to be subclassed with a specific Glider type for T.
 * @param <T> a Glider type
 */
@Beta
public class SequenceGlider<T extends Glider> implements Glider {
    protected float change = 0f;
    protected @Nonnull Interpolation interpolation = Interpolation.linear;
    protected T[] sequence;
    protected float[] durations;
    protected int active;
    protected float passed;

    public SequenceGlider(T[] gliders, float[] lengths){
        active = 0;
        passed = 0f;
        final int len = Math.min(gliders.length, lengths.length);
        sequence = Arrays.copyOf(gliders, len);
        durations = Arrays.copyOf(lengths, len);
        for (int i = 0; i < len; i++) {
            durations[i] /= len;
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


}
