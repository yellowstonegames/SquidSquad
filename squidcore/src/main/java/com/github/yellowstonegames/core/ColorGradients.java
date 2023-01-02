/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

package com.github.yellowstonegames.core;

import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.IntList;
import com.github.yellowstonegames.core.Interpolations.InterpolationFunction;

/**
 * Static methods for handling gradients of smoothly-changing colors, typically inside of {@link IntList}s.
 * The intent is for the IntList to be used as a sequence of packed int Oklab colors. You can create a new
 * IntList gradient with {@link #makeGradient(int, int, int, InterpolationFunction)}, but any IntList will work
 * (although it only makes sense if it contains packed Oklab colors or is empty). Once you have a IntList, you can
 * pass it to {@link #appendGradient(IntList, int, int, int, InterpolationFunction)} to make a gradient between two
 * colors, or {@link #appendGradientChain(IntList, int, InterpolationFunction, int...)} to make a gradient between more
 * than two colors. You can also customize each section between colors with
 * {@link #appendPartialGradient(IntList, int, int, int, InterpolationFunction)}, which is just like appendGradient()
 * but doesn't add the end color (since it is the start color of the next partial gradient, until you finally end by
 * appending just the end). Using appendPartialGradient(), you can have each transition use a different number of steps.
 */
public class ColorGradients {
    /**
     * No need to instantiate.
     */
    private ColorGradients(){
    }

    /**
     * Creates a IntList gradient from the packed int Oklab color {@code start} to the packed int Oklab color
     * {@code end}, taking the specified number of steps and using linear interpolation.
     * This limits individual steps of color to the correct Oklab gamut, so even interpolations between colors at
     * extreme points in the color space will stay in-gamut.
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end on
     * @param steps how many steps the gradient should use; usually greater than 2, and must be non-negative
     * @return a new IntList that contains the requested gradient
     */
    public static IntList makeGradient(int start, int end, int steps) {
        return makeGradient(start, end, steps, Interpolations.linear);
    }
    /**
     * Creates a IntList gradient from the packed int Oklab color {@code start} to the packed int Oklab color
     * {@code end}, taking the specified number of steps and using the specified Interpolation for how it transitions.
     * This limits individual steps of color to the correct Oklab gamut, so even interpolations between colors at
     * extreme points in the color space will stay in-gamut.
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end on
     * @param steps how many steps the gradient should use; usually greater than 2, and must be non-negative
     * @param interpolation a libGDX Interpolation that can be used to customize how start transitions to end
     * @return a new IntList that contains the requested gradient
     */
    public static IntList makeGradient(int start, int end, int steps, InterpolationFunction interpolation) {
        IntList appending = new IntList(steps);
        if(steps <= 0) {
            return appending;
        }
        if(steps == 1) {
            appending.add(start);
            return appending;
        }
        appendPartialGradient(appending, start, end, steps - 1, interpolation).add(end);
        return appending;
    }

    /**
     * Appends a gradient from the packed int Oklab color {@code start} to the packed int Oklab color {@code end},
     * taking the specified number of steps and using linear Interpolation for how it transitions. This limits
     * individual steps of color to the correct Oklab gamut, so even interpolations between colors at extreme points in
     * the color space will stay in-gamut.
     * @param appending a IntList that will be appended to
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end on
     * @param steps how many steps the gradient should use; usually greater than 2
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradient(IntList appending, int start, int end, int steps) {
        return appendGradient(appending, start, end, steps, Interpolations.linear);
    }
    /**
     * Appends a gradient from the packed int Oklab color {@code start} to the packed int Oklab color {@code end},
     * taking the specified number of steps and using the specified Interpolation for how it transitions. This limits
     * individual steps of color to the correct Oklab gamut, so even interpolations between colors at extreme points in
     * the color space will stay in-gamut.
     * @param appending a IntList that will be appended to
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end on
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param interpolation a libGDX Interpolation that can be used to customize how start transitions to end
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradient(IntList appending, int start, int end, int steps, InterpolationFunction interpolation) {
        if(appending == null)
            return null;
        if(steps <= 0) {
            return appending;
        }
        if(steps == 1) {
            appending.add(start);
            return appending;
        }
        appending.ensureCapacity(steps);
        appendPartialGradient(appending, start, end, steps - 1, interpolation).add(end);
        return appending;
    }

    /**
     * Appends a gradient between several packed int Oklab colors provided in {@code chain}. This uses linear
     * Interpolation for the whole gradient. Appends to the end of {@code appending} and produces a total of
     * {@code steps} colors.
     * @param appending a IntList that will be appended to
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param chain an array or varargs of packed int Oklab colors that this will interpolate through in order
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradientChain(IntList appending, int steps, int... chain) {
        return appendGradientChain(appending, steps, Interpolations.linear, chain);
    }

    /**
     * Appends a gradient between several packed int Oklab colors provided in {@code chain}. This uses linear
     * Interpolation for the whole gradient. Appends to the end of {@code appending} and produces a total of
     * {@code steps} colors.
     * @param appending a IntList that will be appended to
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param chain a IntList of packed int Oklab colors that this will interpolate through in order
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradientChain(IntList appending, int steps, IntList chain) {
        return appendGradientChain(appending, steps, Interpolations.linear, chain);
    }

    /**
     * Appends a gradient between several packed int Oklab colors provided in {@code chain}. This uses the specified
     * Interpolation for the whole gradient, which can make some colors use smaller sections than others. Appends to the
     * end of {@code appending} and produces a total of {@code steps} colors.
     * @param appending a IntList that will be appended to
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param interpolation a libGDX Interpolation that can be used to customize how start transitions to end
     * @param chain a IntList of packed int Oklab colors that this will interpolate through in order
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradientChain(IntList appending, int steps, InterpolationFunction interpolation, IntList chain) {
        if (appending == null)
            return null;
        if(chain == null)
            return appending;
        if (steps <= 0 || chain.size() == 0) {
            return appending;
        }
        if (steps == 1 || chain.size() == 1) {
            appending.add(chain.first());
            return appending;
        }
        appending.ensureCapacity(steps);
        int limit = steps - 1, splits = chain.size() - 1;
        float step = 1f / steps, change = 0f;
        for (int i = 0; i < limit; i++) {
            float interp = interpolation.apply(change);
            float splint = Math.min(Math.max(interp * splits, 0f), splits - 0.000001f);
            int idx = (int)splint;
            appending.add(DescriptiveColor.limitToGamut(DescriptiveColor.lerpColors(chain.get(idx), chain.get(idx+1), MathTools.norm(idx, idx +1, splint))));
            change += step;
        }
        appending.add(chain.get(splits));
        return appending;
    }

    /**
     * Appends a gradient between several packed int Oklab colors provided in {@code chain}. This uses the specified
     * Interpolation for the whole gradient, which can make some colors use smaller sections than others. Appends to the
     * end of {@code appending} and produces a total of {@code steps} colors.
     * @param appending a IntList that will be appended to
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param interpolation a libGDX Interpolation that can be used to customize how start transitions to end
     * @param chain an array or varargs of packed int Oklab colors that this will interpolate through in order
     * @return {@code appending}, after adding the gradient to the end
     */
    public static IntList appendGradientChain(IntList appending, int steps, InterpolationFunction interpolation, int... chain) {
        if (appending == null)
            return null;
        if(chain == null)
            return appending;
        if (steps <= 0 || chain.length == 0) {
            return appending;
        }
        if (steps == 1 || chain.length == 1) {
            appending.add(chain[0]);
            return appending;
        }
        appending.ensureCapacity(steps);
        int limit = steps - 1, splits = chain.length - 1;
        float step = 1f / steps, change = 0f;
        for (int i = 0; i < limit; i++) {
            float interp = interpolation.apply(change);
            float splint = Math.min(Math.max(interp * splits, 0f), splits - 0.000001f);
            int idx = (int)splint;
            appending.add(DescriptiveColor.limitToGamut(DescriptiveColor.lerpColors(chain[idx], chain[idx+1], MathTools.norm(idx, idx +1, splint))));
            change += step;
        }
        appending.add(chain[splits]);
        return appending;
    }

    /**
     * Exactly like {@link #appendGradient(IntList, int, int, int)}, but does not include
     * {@code end} in what it appends to {@code appending}. This is intended for the implementation of chained
     * gradients, where the end of a previous gradient becomes the start of the next one. This still uses the specified
     * number of steps, it just doesn't append {@code end} in the last step.
     * @param appending a IntList that will be appended to
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end just before
     * @param steps how many steps the gradient should use; usually greater than 2
     * @return {@code appending}, after adding the gradient to its end
     */
    public static IntList appendPartialGradient(IntList appending, int start, int end, int steps) {
        return appendPartialGradient(appending, start, end, steps, Interpolations.linear);
    }
    /**
     * Exactly like {@link #appendGradient(IntList, int, int, int, InterpolationFunction)}, but does not include
     * {@code end} in what it appends to {@code appending}. This is intended for the implementation of chained
     * gradients, where the end of a previous gradient becomes the start of the next one. This still uses the specified
     * number of steps, it just doesn't append {@code end} in the last step.
     * @param appending a IntList that will be appended to
     * @param start the packed int Oklab color to start with
     * @param end the packed int Oklab color to end just before
     * @param steps how many steps the gradient should use; usually greater than 2
     * @param interpolation a libGDX Interpolation that can be used to customize how start transitions toward end
     * @return {@code appending}, after adding the gradient to its end
     */
    public static IntList appendPartialGradient(IntList appending, int start, int end, int steps, InterpolationFunction interpolation){
        if(appending == null)
            return null;
        if(steps <= 0) {
            return appending;
        }
        if(steps == 1) {
            appending.add(start);
            return appending;
        }
        int limit = steps;
        float step = 1f / steps, change = 0f;
        for (int i = 0; i < limit; i++) {
            appending.add(DescriptiveColor.limitToGamut(DescriptiveColor.lerpColors(start, end, interpolation.apply(change))));
            change += step;
        }
        return appending;
    }
}
