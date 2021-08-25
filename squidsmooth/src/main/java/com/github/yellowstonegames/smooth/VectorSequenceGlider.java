package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.yellowstonegames.core.annotations.Beta;
import com.github.yellowstonegames.grid.Direction;

import javax.annotation.Nonnull;

import static com.github.yellowstonegames.grid.Direction.*;

/**
 * Very experimental; allows chaining a sequence of VectorGlider movements.
 */
@Beta
public class VectorSequenceGlider extends SequenceGlider<VectorGlider> {

    public VectorSequenceGlider(){
        this(new VectorGlider[0], new float[0]);
    }
    public VectorSequenceGlider(VectorGlider[] gliders, float[] lengths) {
        super(gliders, lengths);
    }

    public VectorSequenceGlider(VectorSequenceGlider other){
        super(new VectorGlider[other.sequence.length], new float[other.sequence.length]);
        for (int i = 0; i < other.sequence.length; i++) {
            sequence[i] = new VectorGlider(other.sequence[i]);
            durations[i] = other.durations[i];
        }
        this.active = other.active;
        this.change = other.change;
        this.passed = other.passed;
        this.interpolation = other.interpolation;
        this.completeRunner = other.completeRunner;
    }

    public VectorSequenceGlider copy(){
        return new VectorSequenceGlider(this);
    }

    public float getX()
    {
        if(sequence.length == 0) return 0;
        if(active > sequence.length)
            return sequence[sequence.length - 1].end.x;
        if(sequence[active].change >= 1f)
            return sequence[active].end.x;
        return interpolation.apply(sequence[active].start.x, sequence[active].end.x, change);
    }

    public float getY()
    {
        if(sequence.length == 0) return 0;
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
