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

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.LineWobble;
import com.github.tommyettinger.random.PouchRandom;
import com.github.yellowstonegames.core.DescriptiveColor;

import static com.github.tommyettinger.digital.MathTools.PHI;

/**
 * Grouping of qualities related to glow and light emission. When a Radiance variable in some object is null, it
 * means that object doesn't emit light; if a Radiance variable is non-null, it will probably emit light unless the
 * color of light it produces is fully transparent. Light may take up one cell or extend into nearby cells, and the
 * radius may change over time in up to two patterns (flicker, which randomly increases and decreases lighting radius,
 * and/or strobe, which increases and decreases lighting radius in an orderly retract-expand-retract-expand pattern).
 * You can set the {@link #flare} variable to some value between 0.0f and 1.0f to temporarily expand the minimum radius
 * for strobe and/or flare, useful for gameplay-dependent brightening of a Radiance.
 * <br>
 * This object has 6 fields, each a float:
 * <ul>
 * <li>range, how far the light extends; 0f is "just this cell"</li>
 * <li>color, the color of the light as a float; typically opaque and lighter than the glowing object's color</li>
 * <li>flicker, the rate of random continuous change to radiance range</li>
 * <li>strobe, the rate of non-random continuous change to radiance range</li>
 * <li>flare, used to suddenly increase the minimum radius of lighting; expected to be changed after creation</li>
 * <li>delay, which delays the pattern of effects like strobe so a sequence can be formed with multiple Radiance</li>
 * </ul>
 * These all have defaults; if no parameters are specified the light will be white, affect only the current cell, and
 * won't flicker or strobe.
 * <br>
 * Created by Tommy Ettinger on 6/16/2018.
 */
public class Radiance {
    /**
     * Randomly-seeded and only used for things that should be visually random, but won't matter for equality.
     */
    private static final PouchRandom random = new PouchRandom();

    /**
     * How far the radiated light extends; 0f is "just this cell", anything higher can go into neighboring cells.
     * This is permitted to be a non-integer value, which will make this extend into further cells partially.
     */
    public float range;
    /**
     * The color of light as an int; will be interpreted as Oklab by {@link LightingManager}, but as RGBA by
     * {@link LightingManagerRgb}. Typically opaque and lighter than the glowing object's symbol.
     */
    public int color;
    /**
     * The rate of random continuous change to radiance range, like the light from a campfire. The random component of
     * the change is determined by a unique seed produced by an internal {@link PouchRandom}, which will
     * probably make all flicker effects different when flicker is non-0.
     */
    public float flicker;
    /**
     * The rate of non-random continuous change to radiance range, like a mechanical strobe effect. This looks like a
     * strobe light when the value is high enough, but at lower values it will smoothly pulse, which can be less
     * distracting to players.
     */
    public float strobe;

    /**
     * A time delay that applies to when the strobe and flicker effects change; useful with strobe to make a strobe
     * expand its lit radius at one point, then expand at a slightly later time at another Radiance with a delay. The
     * range for delay should be considered 0f to 1f, with 0f the default (no delay) and values between 0 and 1f that
     * fraction of a full strobe delayed from that default.
     */
    public float delay;
    /**
     * A temporary increase to the minimum radiance range, meant to brighten a glow during an effect.
     * This should be a float between 0f and 1f, with 0f meaning no change and 1f meaning always max radius.
     */
    public float flare;
    /**
     * Assigned during construction by an internal {@link PouchRandom}, this is used for flickering effects, but does
     * not affect {@link #equals(Object)} or {@link #hashCode()}. You can synchronize the flickering effect of two
     * different Radiance objects by setting their seed to the same int. Seeds do not have to be globally unique.
     * <br>
     * In earlier versions, this was private and transient, but that preventing synchronizing flicker effects. Now, it
     * is public and generally should be serialized.
     */
    public int seed;

    /**
     * All-default constructor; makes a single-cell unchanging white light. This assumes the color is being treated as
     * Oklab; if you treat this as RGBA8888 without first converting with {@link DescriptiveColor#toRGBA8888(int)}, this
     * will look like light red. Because this defaults to using Oklab, it is meant for use with {@link LightingManager},
     * not {@link LightingManagerRgb}.
     */
    public Radiance()
    {
        this(0f, DescriptiveColor.WHITE, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes an unchanging white light with the specified range in cells. This assumes the color is being treated as
     * Oklab; if you treat this as RGBA8888 without first converting with {@link DescriptiveColor#toRGBA8888(int)}, this
     * will look like light red. Because this defaults to using Oklab, it is meant for use with {@link LightingManager},
     * not {@link LightingManagerRgb}.
     * @param range possibly-non-integer radius to light, in cells
     */
    public Radiance(float range)
    {
        this(range, DescriptiveColor.WHITE, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes an unchanging light with the given color (as an Oklab int) and the specified range in cells.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     */
    public Radiance(float range, int color)
    {
        this(range, color, 0f, 0f, 0f, 0f);
    }

    /**
     * Makes a flickering light with the given color (as an Oklab int) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     * @param flicker the rate at which to flicker, as a non-negative float
     */
    public Radiance(float range, int color, float flicker)
    {
        this(range, color, flicker, 0f, 0f, 0f);
    }
    /**
     * Makes a flickering light with the given color (as an Oklab int) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     */
    public Radiance(float range, int color, float flicker, float strobe)
    {
        this(range, color, flicker, strobe, 0f, 0f);
    }
    /**
     * Makes a flickering light with the given color (as a String that can be interpreted by {@link DescriptiveColor})
     * and the specified range in cells; the flicker parameter affects the rate at which this will randomly reduce its
     * range and return to normal, and the strobe parameter affects the rate at which this will steadily reduce its
     * range and return to normal. Usually one of flicker or strobe is 0; if both are non-0, the radius will be smaller
     * than normal. You can only give this a description that will be translated to Oklab, so this is meant to be used
     * only with {@link LightingManager}, not {@link LightingManagerRgb}.
     * @param range possibly-non-integer radius to light, in cells
     * @param color a String that describes a color as per {@link DescriptiveColor#describeOklab(String)}
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     */
    public Radiance(float range, String color, float flicker, float strobe)
    {
        this(range, DescriptiveColor.describeOklab(color), flicker, strobe, 0f, 0f);
    }

    /**
     * Makes a flickering light with the given color (as an Oklab int) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal. The delay parameter is usually
     * from 0f to 1f, and is almost always 0f unless this is part of a group of related Radiance objects; it affects
     * when strobe and flicker hit "high points" and "low points", and should usually be used with strobe.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     * @param delay a delay applied to the "high points" and "low points" of strobe and flicker, from 0f to 1f
     */
    public Radiance(float range, int color, float flicker, float strobe, float delay)
    {
        this(range, color, flicker, strobe, delay, 0f);
    }
    /**
     * Makes a flickering light with the given color (as an Oklab int) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal. The delay parameter is usually
     * from 0f to 1f, and is almost always 0f unless this is part of a group of related Radiance objects; it affects
     * when strobe and flicker hit "high points" and "low points", and should usually be used with strobe. This allows
     * setting flare, where flare is used to create a sudden increase in the minimum radius for the Radiance, but flare
     * makes the most sense to set when an event should brighten a Radiance, not in the constructor. Valid values for
     * flare are usually between 0f and 1f.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     * @param delay a delay applied to the "high points" and "low points" of strobe and flicker, from 0f to 1f
     * @param flare affects the minimum radius for the Radiance, from 0f to 1f with a default of 0f
     */
    public Radiance(float range, int color, float flicker, float strobe, float delay, float flare)
    {
        this(range, color, flicker, strobe, delay, flare, random.nextInt());
    }
    /**
     * Makes a flickering light with the given color (as an Oklab int) and the specified range in cells; the flicker
     * parameter affects the rate at which this will randomly reduce its range and return to normal, and the strobe
     * parameter affects the rate at which this will steadily reduce its range and return to normal. Usually one of
     * flicker or strobe is 0; if both are non-0, the radius will be smaller than normal. The delay parameter is usually
     * from 0f to 1f, and is almost always 0f unless this is part of a group of related Radiance objects; it affects
     * when strobe and flicker hit "high points" and "low points", and should usually be used with strobe. This allows
     * setting flare, where flare is used to create a sudden increase in the minimum radius for the Radiance, but flare
     * makes the most sense to set when an event should brighten a Radiance, not in the constructor. Valid values for
     * flare are usually between 0f and 1f.
     * You can give this an Oklab color (to use with {@link LightingManager}) or an RGBA8888 color (to use with
     * {@link LightingManagerRgb}).
     * @param range possibly-non-integer radius to light, in cells
     * @param color an Oklab or RGBA8888 int color
     * @param flicker the rate at which to flicker, as a non-negative float
     * @param strobe the rate at which to strobe or pulse, as a non-negative float
     * @param delay a delay applied to the "high points" and "low points" of strobe and flicker, from 0f to 1f
     * @param flare affects the minimum radius for the Radiance, from 0f to 1f with a default of 0f
     * @param seed a forced seed that can be used to synchronize flicker effects between Radiance objects
     */
    public Radiance(float range, int color, float flicker, float strobe, float delay, float flare, int seed)
    {
        this.range = range;
        this.color = color;
        this.flicker = flicker;
        this.strobe = strobe;
        this.delay = delay;
        this.flare = flare;
        this.seed = seed;
    }

    /**
     * Copies another Radiance exactly, except for the pattern its flicker may have, if any.
     * @param other another Radiance to copy
     */
    public Radiance(Radiance other)
    {
        this(other.range, other.color, other.flicker, other.strobe, other.delay, other.flare);
    }

    /**
     * Provides the calculated current range adjusted for flicker and strobe at the current time in milliseconds, with
     * flicker seeded with the identity hash code of this Radiance. Higher values of flicker and strobe will increase
     * the frequency at which the range changes but will not allow it to exceed its starting range, only to diminish
     * temporarily. If both flicker and strobe are non-0, the range will usually be smaller than if only one was non-0,
     * and if both are 0, this simply returns range.
     * @return the current range, adjusting for flicker and strobe using the current time
     */
    public float currentRange()
    {
        final float time = (System.currentTimeMillis() & 0x3ffffL) * 0x3.1p-9f;
        float current = range;
        if(flicker != 0f) 
            current *=
                    LineWobble.splobble(seed, time * flicker + delay) * 0.25f +
                    LineWobble.splobble(seed ^ 0x9E3779B9, PHI * (time * flicker + delay + PHI)) * 0.125f +
                            0.5f;
        if(strobe != 0f)
            current *= TrigTools.sinTurns(time * strobe + delay) * 0.25f + 0.75f;
        return Math.max(current, range * flare);
    }

    /**
     * Makes a chain of Radiance objects that will pulse in a sequence, expanding from one to the next.
     * This chain is an array of Radiance where the order matters.
     * @param length how many Radiance objects should be in the returned array
     * @param range in cells, how far each Radiance should expand from its start at its greatest radius
     * @param color as an Oklab or RGBA8888 int color
     * @param strobe the rate at which the chain will pulse; should be greater than 0
     * @return an array of Radiance objects that will pulse in sequence.
     */
    public static Radiance[] makeChain(int length, float range, int color, float strobe)
    {
        if(length <= 1)
            return new Radiance[]{new Radiance(range, color, 0f, strobe)};
        Radiance[] chain = new Radiance[length];
        float d = -2f / (length);
        for (int i = 0; i < length; i++) {
            chain[i] = new Radiance(range, color, 0f, strobe, d * i);
        }
        return chain;
    }

    @Override
    public String toString() {
        return "Radiance{" +
                "range=" + range +
                ", color=0x" + Base.BASE16.unsigned(color) +
                ", flicker=" + flicker +
                ", strobe=" + strobe +
                ", delay=" + delay +
                ", flare=" + flare +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Radiance radiance = (Radiance) o;

        if (Float.compare(radiance.range, range) != 0) return false;
        if (radiance.color != color) return false;
        if (Float.compare(radiance.flicker, flicker) != 0) return false;
        if (Float.compare(radiance.strobe, strobe) != 0) return false;
        if (Float.compare(radiance.delay, delay) != 0) return false;
        return Float.compare(radiance.flare, flare) == 0;
    }

    @Override
    public int hashCode() {
        int result = BitConversion.floatToIntBits(range);
        result = (result ^ (result << 11 | result >>> 21) ^ (result << 19 | result >>> 13)) + BitConversion.floatToIntBits(flicker);
        result = (result ^ (result << 11 | result >>> 21) ^ (result << 19 | result >>> 13)) + BitConversion.floatToIntBits(strobe);
        result = (result ^ (result << 11 | result >>> 21) ^ (result << 19 | result >>> 13)) + BitConversion.floatToIntBits(delay);
        result = (result ^ (result << 11 | result >>> 21) ^ (result << 19 | result >>> 13)) + BitConversion.floatToIntBits(flare);
        result = (result ^ (result << 11 | result >>> 21) ^ (result << 19 | result >>> 13)) + color;
        return result;
    }

    public String stringSerialize()
    {
        StringBuilder sb = new StringBuilder(48);
        sb.append('`');
        Base.SIMPLE64.appendSigned(sb, range).append('~');
        Base.SIMPLE64.appendSigned(sb, color).append('~');
        Base.SIMPLE64.appendSigned(sb, flicker).append('~');
        Base.SIMPLE64.appendSigned(sb, strobe).append('~');
        Base.SIMPLE64.appendSigned(sb, delay).append('~');
        Base.SIMPLE64.appendSigned(sb, flare).append('~');
        Base.SIMPLE64.appendSigned(sb, seed).append('`');
        return sb.toString();
    }
    
    public static Radiance stringDeserialize(String data)
    {
        if(data == null) return null;
        int idx = 0;
        return new Radiance(Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readInt(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readFloatExact(data, idx + 1, idx = data.indexOf('~', idx + 1)),
        Base.SIMPLE64.readInt(data, idx + 1, data.indexOf('`', idx + 1)));
    }
}
