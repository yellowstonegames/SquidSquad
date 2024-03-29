/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.github.tommyettinger.digital.*;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.random.PouchRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.grid.*;

import java.util.List;

public abstract class GridAction extends TemporalAction {
    /**
     * A randomly-seeded random number generator that is meant to be used only for visual effects that don't change
     * gameplay or other logic. This uses the fastest pseudo-random number generator in Juniper right now,
     * {@link PouchRandom}, but because the output of a randomly-seeded generator can't be relied upon, the
     * implementation could change in a future version.
     * <br>
     * The main thing this is used for currently is to assign seeds to actions with a pseudo-random component.
     */
    public static final PouchRandom GUI_RANDOM = new PouchRandom();
    public GlyphGrid grid;
    public Region valid;

    protected GridAction(GlyphGrid targeting)
    {
        grid = targeting;
        valid = new Region(targeting.gridWidth, targeting.gridHeight).allOn();
    }
    protected GridAction(GlyphGrid targeting, float duration)
    {
        grid = targeting;
        setDuration(duration);
        valid = new Region(targeting.gridWidth, targeting.gridHeight).allOn();
    }
    protected GridAction(GlyphGrid targeting, float duration, Region valid)
    {
        grid = targeting;
        setDuration(duration);
        this.valid = valid == null ? new Region(targeting.gridWidth, targeting.gridHeight).allOn() : valid;
    }
    
    public static class ExplosionAction extends GridAction {
        /**
         * Normally you should set this in the constructor, and not change it later.
         */
        public Coord center;
        /**
         * Normally you should set this in the constructor, and not change it later.
         */
        public int radius = 2;
        /**
         * The default explosion colors are normal for (non-chemical, non-electrical) fire and smoke, going from orange
         * at the start to yellow, very light yellow, and then back to a different orange before going to smoke and
         * fading out to translucent and then transparent by the end.
         * <br>
         * If you want to change the colors the explosion uses, you can either pass an IntList of RGBA8888 colors to the
         * constructor or change this array directly.
         */
        public int[] colors = {
                (0xFF4F00FF), // SColor.INTERNATIONAL_ORANGE
                (0xFFB94EFF), // SColor.FLORAL_LEAF
                (0xFDE910FF), // SColor.LEMON
                (0xFFFACDFF), // SColor.LEMON_CHIFFON
                (0xFF6600EE), // SColor.SAFETY_ORANGE
                (0x595652DD), // SColor.DB_SOOT
                (0x59565299)  // SColor.DB_SOOT
        };
        /**
         * The internal representation of how affected each cell is by the explosion, based on proximity to center.
         */
        public float[][] lightMap;
        /**
         * The raw list of Coords that might be affected by the explosion; may include some cells that aren't going to
         * show as exploding (it usually has some false positives), but shouldn't exclude any cells that should show as
         * such (no false negatives). You can edit this if you need to, but it isn't recommended.
         */
        public List<Coord> affected;

        /**
         * An int that determines how any pseudo-random effects in this will look. This is usually set at creation by
         * the constructor (using {@link #GUI_RANDOM} to get a random int), but you can set this manually to replicate
         * a particular appearance, such as for replays.
         */
        public int seed;
        /**
         * Constructs an ExplosionAction with explicit settings for some fields. The valid cells this can affect will be
         * the full expanse of the GlyphGrid. The duration will be 1 second.
         * @param targeting the GlyphGrid to affect
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public ExplosionAction(GlyphGrid targeting, Coord center, int radius)
        {
            this(targeting, 1f, center, radius);
        }
        /**
         * Constructs an ExplosionAction with explicit settings for some fields. The valid cells this can affect will be
         * the full expanse of the GlyphGrid.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Coord center, int radius)
        {
            super(targeting, duration);
            seed = GUI_RANDOM.nextInt();
            this.center = center;
            this.radius = radius;
            float[][] resMap = new float[valid.width][valid.height];
            lightMap = new float[valid.width][valid.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius + 0.5f);
            affected = Radius.inCircle(center.x, center.y, radius, false, valid.width, valid.height);
        }
        /**
         * Constructs an ExplosionAction with explicit settings for most fields.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius)
        {
            super(targeting, duration, valid);
            seed = GUI_RANDOM.nextInt();
            this.center = center;
            this.radius = radius;
            float[][] resMap = ArrayTools.fill(1f, valid.width, valid.height);
            valid.writeFloatsInto(resMap, 0f);
            lightMap = new float[valid.width][valid.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius + 0.5f);
            valid.not().writeFloatsInto(lightMap, 0f);
            valid.not();
            affected = Radius.inCircle(center.x, center.y, radius, false, valid.width, valid.height);
        }

        /**
         * Constructs an ExplosionAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an IntList of RGBA8888 colors that will replace the default fire/smoke colors here
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring)
        {
            this(targeting, duration, valid, center, radius);
            if(colors.length != coloring.size())
                colors = new int[coloring.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = coloring.get(i);
            }
        }

        /**
         * Constructs an ExplosionAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an array of RGBA8888 int colors that will replace the default fire/smoke colors here
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring)
        {
            this(targeting, duration, valid, center, radius);
            if(coloring == null) return;
            if(colors.length != coloring.length)
                colors = new int[coloring.length];
            System.arraycopy(coloring, 0, colors, 0, coloring.length);
        }
        /**
         * Constructs an ExplosionAction with explicit settings for most fields; this constructor allows the case where
         * an explosion is directed in a cone or sector shape. It will center the sector on {@code angle} (in degrees)
         * and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span)
        {
            super(targeting, duration, valid);
            seed = GUI_RANDOM.nextInt();
            this.center = center;
            this.radius = radius;
            float[][] resMap = ArrayTools.fill(1f, valid.width, valid.height);
            valid.writeFloatsInto(resMap, 0f);
            lightMap = new float[valid.width][valid.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius + 0.5f, Radius.CIRCLE, angle, span);
            valid.not().writeFloatsInto(lightMap, 0f);
            valid.not();
            affected = Radius.inCircle(center.x, center.y, radius, false, valid.width, valid.height);
        }

        /**
         * Constructs an ExplosionAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an IntList of RGBA8888 colors that will replace the default fire/smoke colors here
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring)
        {
            this(targeting, duration, valid, center, radius, angle, span);
            if(colors.length != coloring.size())
                colors = new int[coloring.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = coloring.get(i);
            }
        }

        /**
         * Constructs an ExplosionAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an array of RGBA8888 int colors that will replace the default fire/smoke colors here
         */
        public ExplosionAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring)
        {
            this(targeting, duration, valid, center, radius, angle, span);
            if(coloring == null) return;
            if(colors.length != coloring.length)
                colors = new int[coloring.length];
            System.arraycopy(coloring, 0, colors, 0, coloring.length);
        }
        /**
         * Called each frame.
         *
         * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
         *                {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0.
         */
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f, light;
            int color;
            int idx;
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if((light = lightMap[c.x][c.y]) <= 0.0f)
                    continue;
                f = SimplexNoise.noise(c.x * 1.5f, c.y * 1.5f, percent * 5f, seed)
                        * 0.17f + percent * 1.2f;
                if(f < 0f || 0.5 * light + f < 0.4)
                    continue;
                idx = (int) (f * colors.length);
                if(idx >= colors.length - 1)
                    color = DescriptiveColor.lerpColors(colors[colors.length-1], grid.backgrounds[c.x][c.y], MathTools.fract(Math.min(0.99f, f) * colors.length));
                else
                    color = DescriptiveColor.lerpColors(colors[idx], colors[idx+1], MathTools.fract(f * colors.length));
                grid.backgrounds[c.x][c.y] = DescriptiveColor.lerpColors(grid.backgrounds[c.x][c.y], color, DescriptiveColor.alpha(color) * light * 0.25f + 0.75f);
            }
        }
        /**
         * Sets the colors this ExplosionAction uses to go from through various shades of gray-purple before fading.
         * Meant for electrical bursts, this will affect character foregrounds in a GibberishAction. This should look
         * like sparks in GibberishAction if the chars in its {@link GibberishAction#choices} are selected in a way that
         * fits that theme.
         */
        public ExplosionAction useElectricColors()
        {
            colors[0] = (0xCCCCFFEE); // SColor.PERIWINKLE
            colors[1] = (0xBF00FFFF); // SColor.ELECTRIC_PURPLE
            colors[2] = (0xCC99CCFF); // SColor.MEDIUM_LAVENDER_MAGENTA
            colors[3] = (0xC8A2C8EE); // SColor.LILAC
            colors[4] = (0xBF00FFDD); // SColor.ELECTRIC_PURPLE
            colors[5] = (0x6022EEBB); // SColor.ELECTRIC_INDIGO
            colors[6] = (0x4B008277); // SColor.INDIGO
            return this;
        }

        /**
         * Sets the colors this ExplosionAction uses to go from orange, to yellow, to orange, to dark gray, then fade.
         * Meant for fiery explosions with smoke, this will affect character foregrounds in a GibberishAction.
         * This may look more like a fiery blast if used with an ExplosionAction than a GibberishAction.
         */
        public ExplosionAction useFieryColors()
        {
            colors[0] = (0xFF4F00FF); // SColor.INTERNATIONAL_ORANGE
            colors[1] = (0xFFB94EFF); // SColor.FLORAL_LEAF
            colors[2] = (0xFDE910FF); // SColor.LEMON
            colors[3] = (0xFFFACDFF); // SColor.LEMON_CHIFFON
            colors[4] = (0xFF6600EE); // SColor.SAFETY_ORANGE
            colors[5] = (0x595652DD); // SColor.DB_SOOT
            colors[6] = (0x59565299); // SColor.DB_SOOT
            return this;
        }
        /**
         * Sets the colors this ExplosionAction uses to go from through various shades of pale blue before fading.
         * Meant for mist or fog, especially with {@link CloudAction}.
         */
        public ExplosionAction useMistyColors()
        {
            colors[0] = (0xAACCFFCC);
            colors[1] = (0xBBDDFFEE);
            colors[2] = (0xCCEEFFFF);
            colors[3] = (0x99CCEEEE);
            colors[4] = (0xBBEEFFCC);
            colors[5] = (0xAACCEEAA);
            colors[6] = (0x99BBEE77);
            return this;
        }
        /**
         * Sets the colors this ExplosionAction uses to go from through various shades of yellow-green before fading.
         * Meant for acidic effects or some poisons, especially with {@link CloudAction}.
         */
        public ExplosionAction useAcridColors()
        {
            colors[0] = (0xCCFF55CC);
            colors[1] = (0xDDFF66EE);
            colors[2] = (0xEEFF77FF);
            colors[3] = (0xDDEE55EE);
            colors[4] = (0xDDFF66CC);
            colors[5] = (0xCCEE55AA);
            colors[6] = (0xBBEE4477);
            return this;
        }
        /**
         * Sets the colors this ExplosionAction uses to go from through various shades of vivid purple before fading.
         * Meant for poison effects.
         */
        public ExplosionAction useToxicColors()
        {
            colors[0] = (0xDD55FFEE);
            colors[1] = (0xDD00FFFF);
            colors[2] = (0xCC00DDFF);
            colors[3] = (0xCC00CCFF);
            colors[4] = (0xBB22BBFF);
            colors[5] = (0x882299FF);
            colors[6] = (0x551166FF);
            return this;
        }
    }

    /**
     * An effect that acts like an {@link ExplosionAction}, but instead of changing the background colors, this changes
     * the foreground glyph to a randomly-selected character from {@link #choices}. The colors used for the foreground
     * glyphs can be modified using the same methods that ExplosionAction has, such as {@link #useAcridColors()}.
     */
    public static class GibberishAction extends ExplosionAction
    {
        /**
         * This char array contains all characters that can be used in the foreground of this effect. You can assign
         * another char array, such as if you take {@link com.github.yellowstonegames.core.StringTools#PUNCTUATION} and
         * call {@link String#toCharArray()} on it, to this at any time between calls to {@link #update(float)} (which
         * is usually called indirectly via Stage's {@link com.badlogic.gdx.scenes.scene2d.Stage#act()} method if this
         * has been added to an Actor on that Stage). These chars are pseudo-randomly selected every time the percent
         * completed changes.
         */
        public char[] choices = "`~!@#$%^&*()-_=+\\|][}{'\";:/?.>,<".toCharArray();

        /**
         * Constructs an GibberishAction with explicit settings for some fields.
         * {@link #useElectricColors() Uses electric colors}. Has a duration of 1 second.
         * @param targeting the GlyphGrid to affect
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public GibberishAction(GlyphGrid targeting, Coord center, int radius)
        {
            super(targeting, 1f, center, radius);
            useElectricColors();
        }
        /**
         * Constructs an GibberishAction with explicit settings for some fields.
         * {@link #useElectricColors() Uses electric colors}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public GibberishAction(GlyphGrid targeting, float duration, Coord center, int radius) {
            super(targeting, duration, center, radius);
            useElectricColors();
        }
        /**
         * Constructs an GibberishAction with explicit settings for some fields.
         * {@link #useElectricColors() Uses electric colors}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param choices all characters that can be used in the foreground of this effect
         */
        public GibberishAction(GlyphGrid targeting, float duration, Coord center, int radius, char[] choices) {
            super(targeting, duration, center, radius);
            this.choices = choices;
            useElectricColors();
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields.
         * {@link #useElectricColors() Uses electric colors}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius)
        {
            super(targeting, duration, valid, center, radius);
            useElectricColors();
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields.
         * {@link #useElectricColors() Uses electric colors}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, char[] choices)
        {
            super(targeting, duration, valid, center, radius);
            this.choices = choices;
            useElectricColors();
        }

        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring a List of Color or subclasses thereof that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring)
        {
            super(targeting, duration, valid, center, radius, coloring);
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring a List of Color or subclasses thereof that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring, char[] choices)
        {
            super(targeting, duration, valid, center, radius, coloring);
            this.choices = choices;
        }

        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an array of colors as packed floats that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring)
        {
            super(targeting, duration, valid, center, radius, coloring);
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an array of colors as packed floats that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring, char[] choices)
        {
            super(targeting, duration, valid, center, radius, coloring);
            this.choices = choices;
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, char[] choices)
        {
            super(targeting, duration, valid, center, radius, angle, span);
            this.choices = choices;
            useElectricColors();
        }

        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring a List of Color or subclasses thereof that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring)
        {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring a List of Color or subclasses thereof that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring, char[] choices)
        {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
            this.choices = choices;
        }

        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an array of colors as packed floats that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring)
        {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }
        /**
         * Constructs an GibberishAction with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using purple spark colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an array of colors as packed floats that will replace the default purple spark colors here
         */
        public GibberishAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring, char[] choices)
        {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
            this.choices = choices;
        }
        /**
         * Called each frame.
         *
         * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
         *                {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0.
         */
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f;
            int color;
            int idx;
            final int clen = choices.length;
            final long tick = Hasher.randomize1(BitConversion.doubleToLongBits(percent) + seed);
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if(lightMap[c.x][c.y] <= 0.0)
                    continue;
                f = SimplexNoise.noise(c.x * 1.5f, c.y * 1.5f, percent * 5, seed)
                        * 0.17f + percent * 1.2f;
                if(f < 0f || 0.5 * lightMap[c.x][c.y] + f < 0.4)
                    continue;
                idx = (int) (f * colors.length);
                if(idx >= colors.length - 1)
                    color = DescriptiveColor.lerpColors(colors[colors.length-1], (colors[colors.length-1] & 0xFFFFFF00), MathTools.fract(Math.min(0.99f, f) * colors.length));
                else
                    color = DescriptiveColor.lerpColors(colors[idx], colors[idx+1], MathTools.fract(f * colors.length));
                grid.put(c.x, c.y, choices[Hasher.randomize1Bounded(tick + i, clen)], color);
            }
        }
    }
    public static class CloudAction extends ExplosionAction
    {
        public CloudAction(GlyphGrid targeting, Coord center, int radius) {
            super(targeting, center, radius);
        }

        public CloudAction(GlyphGrid targeting, float duration, Coord center, int radius) {
            super(targeting, duration, center, radius);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius) {
            super(targeting, duration, valid, center, radius);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring) {
            super(targeting, duration, valid, center, radius, coloring);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring) {
            super(targeting, duration, valid, center, radius, coloring);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span) {
            super(targeting, duration, valid, center, radius, angle, span);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring) {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }

        public CloudAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring) {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f, light;
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if((light = lightMap[c.x][c.y]) <= 0f)// || 0.6 * (lightMap[c.x][c.y] + percent) < 0.25)
                    continue;
                f = SimplexNoise.noise(c.x * 0.3f, c.y * 0.3f, percent * 1.3f, seed)
                        * 0.498f + 0.4999f;
                grid.backgrounds[c.x][c.y] = DescriptiveColor.lerpColors(grid.backgrounds[c.x][c.y],
                        DescriptiveColor.lerpColors(colors[(int) (f * colors.length)],
                                colors[((int) (f * colors.length) + 1) % colors.length],
                                MathTools.fract(f * colors.length)), MathTools.swayTight(percent * 2f) * light);
            }
        }
    }
    public static class PulseAction extends ExplosionAction
    {
        public PulseAction(GlyphGrid targeting, Coord center, int radius) {
            super(targeting, center, radius);
        }

        public PulseAction(GlyphGrid targeting, float duration, Coord center, int radius) {
            super(targeting, duration, center, radius);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius) {
            super(targeting, duration, valid, center, radius);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring) {
            super(targeting, duration, valid, center, radius, coloring);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring) {
            super(targeting, duration, valid, center, radius, coloring);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span) {
            super(targeting, duration, valid, center, radius, angle, span);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring) {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }

        public PulseAction(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring) {
            super(targeting, duration, valid, center, radius, angle, span, coloring);
        }
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f, light;
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if((light = lightMap[c.x][c.y]) <= 0f)// || 0.6 * (lightMap[c.x][c.y] + percent) < 0.25)
                    continue;
                f = Math.min(0.999f, Math.max(0f, TrigTools.sinTurns((c.distance(center) - percent * 4f) * 0.25f)));
                grid.backgrounds[c.x][c.y] = DescriptiveColor.lerpColors(grid.backgrounds[c.x][c.y],
                        DescriptiveColor.lerpColors(colors[(int) (f * colors.length)],
                                colors[((int) (f * colors.length) + 1) % colors.length],
                                MathTools.fract(f * colors.length)), MathTools.swayTight(percent * 2f) * light);
            }
        }
    }

    public static class TintAction extends GridAction {
        /**
         * A 2D array of what RGBA8888 colors to tint what cells; alpha is used to determine how much each cell is
         * affected. If an int color is 0, then it is fully transparent, and that cell won't change.
         */
        public int[][] colorGrid;
        /**
         * The raw list of Coords that might be affected by the action. You can edit this if you need to, but it isn't
         * recommended.
         */
        public List<Coord> affected;

        /**
         * Constructs an TintAction with explicit settings for most fields.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this GridAction in seconds, as a float
         * @param valid the valid cells that can be changed by this GridAction, as a Region
         * @param colorGrid a 2D array of RGBA8888 int colors that will be used to determine how to tint cells; alpha affects how strong the tint will be
         */
        public TintAction(GlyphGrid targeting, float duration, Region valid, int[][] colorGrid)
        {
            super(targeting, duration, valid);
            this.colorGrid = ArrayTools.copy(colorGrid);
            this.valid.not().writeIntsInto(this.colorGrid, 0);
            this.valid.not();
            affected = new ObjectList<>(this.valid.size());
            for (int x = 0; x < colorGrid.length; x++) {
                for (int y = 0; y < colorGrid[x].length; y++) {
                    if((this.colorGrid[x][y] & 255) != 0)
                        affected.add(Coord.get(x, y));
                }
            }
        }
        /**
         * Called each frame.
         *
         * @param percent The percentage of completion for this action, growing from 0 to 1 over the duration. If
         *                {@link #setReverse(boolean) reversed}, this will shrink from 1 to 0.
         */
        @Override
        protected void update(float percent) {
            int len = affected.size();
            Coord c;
            float f = (percent < 0.5f) ? Interpolations.sineIn.apply(percent * 2f) : Interpolations.sineOut.apply(2f - percent * 2f);
            int color;
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if(((color = colorGrid[c.x][c.y]) & 255) == 0)
                    continue;
                grid.backgrounds[c.x][c.y] = DescriptiveColorRgb.lerpColorsBlended(grid.backgrounds[c.x][c.y], color, f);
            }
        }
    }
}
