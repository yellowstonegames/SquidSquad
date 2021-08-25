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

    /**
     * Typically called in {@link #setChange(float)} when change reaches at least 1.0. The default implementation does
     * nothing, but implementing classes are encouraged to have this call a user-supplied {@link Runnable}, to give the
     * most flexibility to the user.
     */
    default void onComplete() {
    }
}
