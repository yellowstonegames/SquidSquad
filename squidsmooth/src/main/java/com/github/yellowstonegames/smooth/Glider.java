package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.math.Interpolation;

import javax.annotation.Nonnull;

/**
 * An interface for properties that change smoothly as {@link #setChange(float)} is called.
 */
public interface Glider {
    float getChange();

    void setChange(float change);

    @Nonnull
    Interpolation getInterpolation();

    void setInterpolation(@Nonnull Interpolation interpolation);
}
