package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.github.yellowstonegames.core.annotations.Beta;

import javax.annotation.Nonnull;

/**
 * Very experimental; allows chaining a sequence of VectorGlider movements.
 */
@Beta
public class VectorSequenceGlider extends SequenceGlider<VectorGlider> {

    public VectorSequenceGlider(VectorGlider[] gliders, float[] lengths) {
        super(gliders, lengths);
    }

    public float getX()
    {
        if(active > sequence.length)
            return sequence[sequence.length - 1].end.x;
        if(sequence[active].change >= 1f)
            return sequence[active].end.x;
        return interpolation.apply(sequence[active].start.x, sequence[active].end.x, change);
    }

    public float getY()
    {
        if(active > sequence.length)
            return sequence[sequence.length - 1].end.y;
        if(sequence[active].change >= 1f)
            return sequence[active].end.y;
        return interpolation.apply(sequence[active].start.y, sequence[active].end.y, change);
    }

    @Override
    public float getChange() {
        return super.getChange();
    }

    @Override
    public void setChange(float change) {
        super.setChange(change);
    }

    @Nonnull
    @Override
    public Interpolation getInterpolation() {
        return super.getInterpolation();
    }

    @Override
    public void setInterpolation(@Nonnull Interpolation interpolation) {
        super.setInterpolation(interpolation);
    }
}
