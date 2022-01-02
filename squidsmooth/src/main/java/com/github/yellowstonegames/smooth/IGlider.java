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

import javax.annotation.Nonnull;

/**
 * An interface for properties that change smoothly as {@link #setChange(float)} is called.
 */
public interface IGlider {
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
