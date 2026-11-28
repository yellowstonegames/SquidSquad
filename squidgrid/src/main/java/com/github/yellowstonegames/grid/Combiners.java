/*
 * Copyright (c) 2026; see AUTHORS file.
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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.RoughMath;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.function.FloatFloatToFloatBiFunction;

/**
 * Provides predefined {@link Combiner} constants and ways to generate {@link FloatFloatToFloatBiFunction} instances, as
 * well as acting as the registry for known Combiner values so that they can be looked up by name.
 */
@SuppressWarnings("Convert2MethodRef")
public final class Combiners {
    /**
     * No need to instantiate.
     */
    private Combiners() {
    }

    private static final ObjectObjectOrderedMap<String, Combiner> REGISTRY = new ObjectObjectOrderedMap<>(128);

    /**
     * Looks up the given {@code tag} in a registry of Combiners, and if there exists one with that name, returns
     * it. Otherwise, this returns null.
     * @param tag a tag used to register a Combiner here
     * @return the Combiner registered with the given tag, or null if none exists for that tag
     */
    public static Combiner get(String tag) {
        return REGISTRY.get(tag);
    }

    /**
     * Looks up the given {@code tag} in a registry of Combiners, and if there exists one with that name, returns
     * it. Otherwise, this returns {@code defaultValue}.
     * @param tag a tag used to register a Combiner here
     * @param defaultValue the default Combiner to use if the tag is not registered
     * @return the Combiner registered with the given tag, or {@code defaultValue} if none exists for that tag
     */
    public static Combiner getOrDefault(String tag, Combiner defaultValue) {
        return REGISTRY.getOrDefault(tag, defaultValue);
    }

    /**
     * Allocates a new String array, fills it with every tag registered for a Combiner, and returns that array.
     * @return an array containing every String tag registered for a Combiner
     */
    public static String[] getTagArray() {
        return REGISTRY.keySet().toArray(new String[0]);
    }

    /**
     * Allocates a new Combiner array, fills it with every registered Combiner, and returns that array.
     * @return an array containing every Combiner registered
     */
    public static Combiner[] getCombinerArray() {
        return REGISTRY.values().toArray(new Combiner[0]);
    }

    /**
     * A simple wrapper around an {@link FloatFloatToFloatBiFunction} so it is associated with a String {@link #tag}.
     * This also implements FloatFloatToFloatBiFunction. Creating a Combiner registers it in {@link Combiners}.
     */
    public static class Combiner implements FloatFloatToFloatBiFunction {
        /**
         * A unique String that identifies this object.
         * @see #getTag()
         */
        public final String tag;
        /**
         * The FloatFloatToFloatBiFunction this actually uses to do its math work.
         * @see #getFn()
         */
        public final FloatFloatToFloatBiFunction fn;

        /**
         * Calls {@link #Combiner(String, FloatFloatToFloatBiFunction)} with {@code "add"} and {@link #addFunction}.
         * Because {@link #add} is already registered with that tag and function, this isn't very useful.
         */
        public Combiner() {
            this("add", addFunction);
        }

        /**
         * Creates an Combiner that will use the given {@code fn} and registers it with the given tag. The tag must
         * be unique; if {@link Combiners#get(String)} returns a non-null value when looking up the given tag, then
         * if you create an Combiner with that tag, the existing value will be overwritten.
         * @param tag a unique String that can be used as a key to access this with {@link Combiners#get(String)}
         * @param fn an {@link FloatFloatToFloatBiFunction} to wrap
         */
        public Combiner(String tag, FloatFloatToFloatBiFunction fn) {
            this.tag = tag;
            this.fn = fn;
            REGISTRY.put(tag, this);
        }

        @Override
        public float applyAsFloat(float first, float second) {
            return fn.applyAsFloat(first, second);
        }

        /**
         * Gets the tag for this Combiner, which is a unique String that identifies this object. If another
         * Combiner tries to use the same tag, this Combiner will be un-registered and will no longer be
         * returnable from {@link #get(String)}.
         * @return the tag String
         */
        public String getTag() {
            return tag;
        }

        /**
         * Gets the FloatFloatToFloatBiFunction this actually uses to do its math work.
         *
         * @return the FloatFloatToFloatBiFunction this uses
         */
        public FloatFloatToFloatBiFunction getFn() {
            return fn;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Combiner that = (Combiner) o;

            return tag.equals(that.tag);
        }

        @Override
        public int hashCode() {
            return tag.hashCode();
        }

        @Override
        public String toString() {
            return tag;
        }
    }

    /**
     * Adds its arguments and returns them, without further changes.
     */
    public static final FloatFloatToFloatBiFunction addFunction = (a, b) -> a + b;

    /**
     * Plain addition of the two arguments.
     */
    public static final Combiner add = new Combiner("add", addFunction);

    /**
     * Subtracts second from first and returns that, without further changes.
     */
    public static final FloatFloatToFloatBiFunction subtractFunction = (a, b) -> a - b;

    /**
     * Plain subtraction of the two arguments.
     */
    public static final Combiner subtract = new Combiner("subtract", subtractFunction);

    /**
     * Adds its arguments and clamps that if it exceeds the -1 to 1 range.
     */
    public static final FloatFloatToFloatBiFunction addClampedFunction = (a, b) -> Math.min(Math.max(a + b, -1), 1);

    /**
     * Adds its arguments and clamps them if they exceed the -1 to 1 range.
     */
    public static final Combiner addClamped = new Combiner("addClamped", addClampedFunction);

    /**
     * Subtracts second from first and clamps that if it exceeds the -1 to 1 range.
     */
    public static final FloatFloatToFloatBiFunction subtractClampedFunction = (a, b) -> Math.min(Math.max(a - b, -1), 1);

    /**
     * Subtracts second from first and clamps that if it exceeds the -1 to 1 range.
     */
    public static final Combiner subtractClamped = new Combiner("subtractClamped", subtractClampedFunction);

    /**
     * Averages the two arguments. Just adds first and second, then multiplies by 0.5f .
     */
    public static final Combiner average = new Combiner("average", (a, b) -> (a + b) * 0.5f);

    /**
     * Like {@link #average}, but subtracts instead of adding.
     * Subtracts second from first, then multiplies by 0.5f .
     */
    public static final Combiner halfSubtract = new Combiner("halfSubtract", (a, b) -> (a - b) * 0.5f);

    /**
     * Returns the minimum of the two arguments.
     */
    public static final FloatFloatToFloatBiFunction minFunction = (a, b) -> Math.min(a, b);

    /**
     * Returns the minimum of the two arguments.
     */
    public static final Combiner min = new Combiner("min", minFunction);

    /**
     * Returns the maximum of the two arguments.
     */
    public static final FloatFloatToFloatBiFunction maxFunction = (a, b) -> Math.max(a, b);

    /**
     * Returns the maximum of the two arguments.
     */
    public static final Combiner max = new Combiner("max", maxFunction);

    /**
     * Range is from -1 to 1, but can only gradually approach -1, while this returns 1 when x and y are both 0.
     */
    public static final Combiner gaussian = new Combiner("gaussian", (x, y) -> 2 * RoughMath.expRough(-x * x - y * y) - 1);

    /**
     * Range is from -1 to 1; this returns -1 when x and y are both 0, and returns 1 when both x and y are 1 or -1.
     */
    public static final Combiner parabola = new Combiner("parabola", (x, y) -> (x * x + y * y) - 1);
    /**
     * Range is from -1 to 1; this returns 1 when x or y is 0, and returns -1 when both x and y are 1 or -1.
     */
    public static final Combiner cosine = new Combiner("cosine", (x, y) -> TrigTools.cosSmootherTurns(x * y * 0.5f));
    /**
     * A <a href="https://en.wikipedia.org/wiki/Smooth_maximum#LogSumExp">smooth max</a> function, LogSumExp.
     */
    public static final Combiner logSumExp = new Combiner("logSumExp", (x, y) -> RoughMath.logRough(RoughMath.expRough(x) + RoughMath.expRough(y)));

}
