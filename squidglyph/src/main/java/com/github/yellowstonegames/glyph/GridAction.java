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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.ds.IntList;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import java.util.List;

public abstract class GridAction extends TemporalAction {
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
    
    public static class ExplosionEffect extends GridAction {
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
         * Constructs an ExplosionEffect with explicit settings for some fields. The valid cells this can affect will be
         * the full expanse of the GlyphGrid. The duration will be 1 second.
         * @param targeting the GlyphGrid to affect
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */

        public ExplosionEffect(GlyphGrid targeting, Coord center, int radius)
        {
            this(targeting, 1f, center, radius);
        }
        /**
         * Constructs an ExplosionEffect with explicit settings for some fields. The valid cells this can affect will be
         * the full expanse of the GlyphGrid.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Coord center, int radius)
        {
            super(targeting, duration);
            this.center = center;
            this.radius = radius;
            float[][] resMap = new float[valid.width][valid.height];
            lightMap = new float[valid.width][valid.height];
            FOV.reuseFOV(resMap, lightMap, center.x, center.y, radius + 0.5f);
            affected = Radius.inCircle(center.x, center.y, radius, false, valid.width, valid.height);
        }
        /**
         * Constructs an ExplosionEffect with explicit settings for most fields.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius)
        {
            super(targeting, duration, valid);
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
         * Constructs an ExplosionEffect with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an IntList of RGBA8888 colors that will replace the default fire/smoke colors here
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, IntList coloring)
        {
            this(targeting, duration, valid, center, radius);
            if(colors.length != coloring.size())
                colors = new int[coloring.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = coloring.get(i);
            }
        }

        /**
         * Constructs an ExplosionEffect with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param coloring an array of RGBA8888 int colors that will replace the default fire/smoke colors here
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, int[] coloring)
        {
            this(targeting, duration, valid, center, radius);
            if(coloring == null) return;
            if(colors.length != coloring.length)
                colors = new int[coloring.length];
            System.arraycopy(coloring, 0, colors, 0, coloring.length);
        }
        /**
         * Constructs an ExplosionEffect with explicit settings for most fields; this constructor allows the case where
         * an explosion is directed in a cone or sector shape. It will center the sector on {@code angle} (in degrees)
         * and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span)
        {
            super(targeting, duration, valid);
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
         * Constructs an ExplosionEffect with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an IntList of RGBA8888 colors that will replace the default fire/smoke colors here
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, IntList coloring)
        {
            this(targeting, duration, valid, center, radius, angle, span);
            if(colors.length != coloring.size())
                colors = new int[coloring.size()];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = coloring.get(i);
            }
        }

        /**
         * Constructs an ExplosionEffect with explicit settings for most fields but also an alternate group of Color
         * objects that it will use to color the explosion instead of using fiery/smoke colors; this constructor allows
         * the case where an explosion is directed in a cone or sector shape. It will center the sector on {@code angle}
         * (in degrees) and will cover an amount of the circular area (in degrees) equal to {@code span}.
         * @param targeting the GlyphGrid to affect
         * @param duration the duration of this PanelEffect in seconds, as a float
         * @param valid the valid cells that can be changed by this PanelEffect, as a Region
         * @param center the center of the explosion
         * @param radius the radius of the explosion, in cells
         * @param angle the angle, in degrees, that will be the center of the sector-shaped effect
         * @param span the span, in degrees, of the full arc at the end of the sector-shaped effect
         * @param coloring an array of RGBA8888 int colors that will replace the default fire/smoke colors here
         */
        public ExplosionEffect(GlyphGrid targeting, float duration, Region valid, Coord center, int radius, float angle, float span, int[] coloring)
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
            int idx, seed = System.identityHashCode(this);
            for (int i = 0; i < len; i++) {
                c = affected.get(i);
                if((light = lightMap[c.x][c.y]) <= 0.0f)
                    continue;
                f = Noise.instance.singleSimplex(seed, c.x * 1.5f, c.y * 1.5f, percent * 5f)
                        * 0.17f + percent * 1.2f;
                if(f < 0f || 0.5 * light + f < 0.4)
                    continue;
                idx = (int) (f * colors.length);
                if(idx >= colors.length - 1)
                    color = DescriptiveColor.lerpColors(colors[colors.length-1], (colors[colors.length-1] & 0xFFFFFF00), (Math.min(0.99f, f) * colors.length) % 1f);
                else
                    color = DescriptiveColor.lerpColors(colors[idx], colors[idx+1], (f * colors.length) % 1f);
                grid.backgrounds[c.x][c.y] = DescriptiveColor.lerpColors(grid.backgrounds[c.x][c.y], color, DescriptiveColor.alpha(color) * light * 0.25f + 0.75f);
            }
        }
        /**
         * Sets the colors this ExplosionEffect uses to go from through various shades of gray-purple before fading.
         * Meant for electrical bursts, this will affect character foregrounds in a GibberishEffect. This should look
         * like sparks in GibberishEffect if the chars in its choices are selected in a way that
         * fits that theme.
         */
        public ExplosionEffect useElectricColors()
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
         * Sets the colors this ExplosionEffect uses to go from orange, to yellow, to orange, to dark gray, then fade.
         * Meant for fiery explosions with smoke, this will affect character foregrounds in a GibberishEffect.
         * This may look more like a fiery blast if used with an ExplosionEffect than a GibberishEffect.
         */
        public ExplosionEffect useFieryColors()
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

    }

}
