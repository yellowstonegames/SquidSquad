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

import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;

/**
 * A palette, the same as the one in {@link DescriptiveColor}, just using RGBA8888 ints instead of Oklab ints for
 * each color entry. This provides the same ways of looking up colors, but nothing currently to modify int colors.
 * There is considerably more existing code available elsewhere to modify RGBA colors, though.
 */
public final class DescriptiveColorRgb {
    /**
     * No need to extend this.
     */
    private DescriptiveColorRgb() {
    }

    /**
     * You can look up colors by name here; the names are lower-case, and the colors are packed ints in Oklab format.
     */
    public static final ObjectIntOrderedMap<String> NAMED = new ObjectIntOrderedMap<>(51);
    /**
     * Stores alternative names for colors in {@link #NAMED}, like "grey" as an alias for {@link #GRAY} or "gold" as an
     * alias for {@link #SAFFRON}. Currently, the list of aliases is as follows:
     * <ul>
     * <li>"grey" maps to {@link #GRAY},</li>
     * <li>"gold" maps to {@link #SAFFRON},</li>
     * <li>"puce" maps to {@link #MAUVE},</li>
     * <li>"sand" maps to {@link #TAN},</li>
     * <li>"skin" maps to {@link #PEACH},</li>
     * <li>"coral" maps to {@link #SALMON},</li>
     * <li>"azure" maps to {@link #SKY}, and</li>
     * <li>"ocean" maps to {@link #TEAL}, and</li>
     * <li>"sapphire" maps to {@link #COBALT}.</li>
     * </ul>
     * Note that these aliases are not duplicated in {@link #NAMES}, {@link #NAMES_BY_HUE}, or
     * {@link #NAMES_BY_LIGHTNESS}; they are primarily there so blind attempts to name a color might still work.
     */
    public static final ObjectIntOrderedMap<String> ALIASES = new ObjectIntOrderedMap<>(10);

    public static final IntList LIST = new IntList(51);

    /**
     * A fully-transparent color that is not exactly transparent black (rather, transparent very dark blue), to be
     * used as a placeholder for colors that aren't valid in some way.
     */
    public static final int PLACEHOLDER = 0x00000100;


    /**
     * This color constant "black" has RGBA8888 code {@code 0x000000FF}, red 0.0, green 0.0, blue 0.0, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLACK = 0x000000ff;

    static {
        NAMED.put("black", 0x000000ff);
        LIST.add(0x000000ff);
    }

    /**
     * This color constant "gray" has RGBA8888 code {@code 0x808080FF}, red 0.5019608, green 0.5019608, blue 0.5019608, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.5019608.
     * <pre>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GRAY = 0x808080ff;

    static {
        NAMED.put("gray", 0x808080ff);
        LIST.add(0x808080ff);
    }

    /**
     * This color constant "silver" has RGBA8888 code {@code 0xB6B6B6FF}, red 0.7137255, green 0.7137255, blue 0.7137255, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.7137255.
     * <pre>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SILVER = 0xb6b6b6ff;

    static {
        NAMED.put("silver", 0xb6b6b6ff);
        LIST.add(0xb6b6b6ff);
    }

    /**
     * This color constant "white" has RGBA8888 code {@code 0xFFFFFFFF}, red 1.0, green 1.0, blue 1.0, alpha 1.0, hue 0.0, saturation 0.0, and lightness 1.0.
     * <pre>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int WHITE = 0xffffffff;

    static {
        NAMED.put("white", 0xffffffff);
        LIST.add(0xffffffff);
    }

    /**
     * This color constant "red" has RGBA8888 code {@code 0xFF0000FF}, red 1.0, green 0.0, blue 0.0, alpha 1.0, hue 0.0, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RED = 0xff0000ff;

    static {
        NAMED.put("red", 0xff0000ff);
        LIST.add(0xff0000ff);
    }

    /**
     * This color constant "orange" has RGBA8888 code {@code 0xFF7F00FF}, red 1.0, green 0.49803922, blue 0.0, alpha 1.0, hue 0.08300654, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ORANGE = 0xff7f00ff;

    static {
        NAMED.put("orange", 0xff7f00ff);
        LIST.add(0xff7f00ff);
    }

    /**
     * This color constant "yellow" has RGBA8888 code {@code 0xFFFF00FF}, red 1.0, green 1.0, blue 0.0, alpha 1.0, hue 0.16666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int YELLOW = 0xffff00ff;

    static {
        NAMED.put("yellow", 0xffff00ff);
        LIST.add(0xffff00ff);
    }

    /**
     * This color constant "green" has RGBA8888 code {@code 0x00FF00FF}, red 0.0, green 1.0, blue 0.0, alpha 1.0, hue 0.33333334, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GREEN = 0x00ff00ff;

    static {
        NAMED.put("green", 0x00ff00ff);
        LIST.add(0x00ff00ff);
    }

    /**
     * This color constant "blue" has RGBA8888 code {@code 0x0000FFFF}, red 0.0, green 0.0, blue 1.0, alpha 1.0, hue 0.6666667, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLUE = 0x0000ffff;

    static {
        NAMED.put("blue", 0x0000ffff);
        LIST.add(0x0000ffff);
    }

    /**
     * This color constant "indigo" has RGBA8888 code {@code 0x520FE0FF}, red 0.32156864, green 0.05882353, blue 0.8784314, alpha 1.0, hue 0.7200957, saturation 0.81960785, and lightness 0.46862745.
     * <pre>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int INDIGO = 0x520fe0ff;

    static {
        NAMED.put("indigo", 0x520fe0ff);
        LIST.add(0x520fe0ff);
    }

    /**
     * This color constant "violet" has RGBA8888 code {@code 0x9040EFFF}, red 0.5647059, green 0.2509804, blue 0.9372549, alpha 1.0, hue 0.74285716, saturation 0.6862745, and lightness 0.59411764.
     * <pre>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int VIOLET = 0x9040efff;

    static {
        NAMED.put("violet", 0x9040efff);
        LIST.add(0x9040efff);
    }

    /**
     * This color constant "purple" has RGBA8888 code {@code 0xC000FFFF}, red 0.7529412, green 0.0, blue 1.0, alpha 1.0, hue 0.7921569, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PURPLE = 0xc000ffff;

    static {
        NAMED.put("purple", 0xc000ffff);
        LIST.add(0xc000ffff);
    }

    /**
     * This color constant "brown" has RGBA8888 code {@code 0x8F573BFF}, red 0.56078434, green 0.34117648, blue 0.23137255, alpha 1.0, hue 0.055555552, saturation 0.3294118, and lightness 0.39607844.
     * <pre>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BROWN = 0x8f573bff;

    static {
        NAMED.put("brown", 0x8f573bff);
        LIST.add(0x8f573bff);
    }

    /**
     * This color constant "pink" has RGBA8888 code {@code 0xFFA0E0FF}, red 1.0, green 0.627451, blue 0.8784314, alpha 1.0, hue 0.8877193, saturation 0.372549, and lightness 0.8137255.
     * <pre>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PINK = 0xffa0e0ff;

    static {
        NAMED.put("pink", 0xffa0e0ff);
        LIST.add(0xffa0e0ff);
    }

    /**
     * This color constant "magenta" has RGBA8888 code {@code 0xF500F5FF}, red 0.9607843, green 0.0, blue 0.9607843, alpha 1.0, hue 0.8333333, saturation 0.9607843, and lightness 0.48039216.
     * <pre>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAGENTA = 0xf500f5ff;

    static {
        NAMED.put("magenta", 0xf500f5ff);
        LIST.add(0xf500f5ff);
    }

    /**
     * This color constant "brick" has RGBA8888 code {@code 0xD5524AFF}, red 0.8352941, green 0.32156864, blue 0.2901961, alpha 1.0, hue 0.009592325, saturation 0.54509807, and lightness 0.5627451.
     * <pre>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRICK = 0xd5524aff;

    static {
        NAMED.put("brick", 0xd5524aff);
        LIST.add(0xd5524aff);
    }

    /**
     * This color constant "ember" has RGBA8888 code {@code 0xF55A32FF}, red 0.9607843, green 0.3529412, blue 0.19607843, alpha 1.0, hue 0.034188036, saturation 0.7647059, and lightness 0.5784313.
     * <pre>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int EMBER = 0xf55a32ff;

    static {
        NAMED.put("ember", 0xf55a32ff);
        LIST.add(0xf55a32ff);
    }

    /**
     * This color constant "salmon" has RGBA8888 code {@code 0xFF6262FF}, red 1.0, green 0.38431373, blue 0.38431373, alpha 1.0, hue 0.0, saturation 0.6156863, and lightness 0.69215685.
     * <pre>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SALMON = 0xff6262ff;

    static {
        NAMED.put("salmon", 0xff6262ff);
        LIST.add(0xff6262ff);
    }

    /**
     * This color constant "chocolate" has RGBA8888 code {@code 0x683818FF}, red 0.40784314, green 0.21960784, blue 0.09411765, alpha 1.0, hue 0.066666655, saturation 0.3137255, and lightness 0.25098038.
     * <pre>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHOCOLATE = 0x683818ff;

    static {
        NAMED.put("chocolate", 0x683818ff);
        LIST.add(0x683818ff);
    }

    /**
     * This color constant "tan" has RGBA8888 code {@code 0xD2B48CFF}, red 0.8235294, green 0.7058824, blue 0.54901963, alpha 1.0, hue 0.0952381, saturation 0.2745098, and lightness 0.6862745.
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TAN = 0xd2b48cff;

    static {
        NAMED.put("tan", 0xd2b48cff);
        LIST.add(0xd2b48cff);
    }

    /**
     * This color constant "bronze" has RGBA8888 code {@code 0xCE8E31FF}, red 0.80784315, green 0.5568628, blue 0.19215687, alpha 1.0, hue 0.09872612, saturation 0.6156863, and lightness 0.49999997.
     * <pre>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRONZE = 0xce8e31ff;

    static {
        NAMED.put("bronze", 0xce8e31ff);
        LIST.add(0xce8e31ff);
    }

    /**
     * This color constant "cinnamon" has RGBA8888 code {@code 0xD2691DFF}, red 0.8235294, green 0.4117647, blue 0.11372549, alpha 1.0, hue 0.06998159, saturation 0.70980394, and lightness 0.46862742.
     * <pre>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CINNAMON = 0xd2691dff;

    static {
        NAMED.put("cinnamon", 0xd2691dff);
        LIST.add(0xd2691dff);
    }

    /**
     * This color constant "apricot" has RGBA8888 code {@code 0xFFA828FF}, red 1.0, green 0.65882355, blue 0.15686275, alpha 1.0, hue 0.09922481, saturation 0.84313726, and lightness 0.57843137.
     * <pre>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int APRICOT = 0xffa828ff;

    static {
        NAMED.put("apricot", 0xffa828ff);
        LIST.add(0xffa828ff);
    }

    /**
     * This color constant "peach" has RGBA8888 code {@code 0xFFBF81FF}, red 1.0, green 0.7490196, blue 0.5058824, alpha 1.0, hue 0.08201058, saturation 0.49411762, and lightness 0.7529412.
     * <pre>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEACH = 0xffbf81ff;

    static {
        NAMED.put("peach", 0xffbf81ff);
        LIST.add(0xffbf81ff);
    }

    /**
     * This color constant "pear" has RGBA8888 code {@code 0xD3E330FF}, red 0.827451, green 0.8901961, blue 0.1882353, alpha 1.0, hue 0.18156426, saturation 0.7019608, and lightness 0.5392157.
     * <pre>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEAR = 0xd3e330ff;

    static {
        NAMED.put("pear", 0xd3e330ff);
        LIST.add(0xd3e330ff);
    }

    /**
     * This color constant "saffron" has RGBA8888 code {@code 0xFFD510FF}, red 1.0, green 0.8352941, blue 0.0627451, alpha 1.0, hue 0.13737796, saturation 0.9372549, and lightness 0.53137255.
     * <pre>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAFFRON = 0xffd510ff;

    static {
        NAMED.put("saffron", 0xffd510ff);
        LIST.add(0xffd510ff);
    }

    /**
     * This color constant "butter" has RGBA8888 code {@code 0xFFF288FF}, red 1.0, green 0.9490196, blue 0.53333336, alpha 1.0, hue 0.14845939, saturation 0.46666664, and lightness 0.76666665.
     * <pre>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BUTTER = 0xfff288ff;

    static {
        NAMED.put("butter", 0xfff288ff);
        LIST.add(0xfff288ff);
    }

    /**
     * This color constant "chartreuse" has RGBA8888 code {@code 0xC8FF41FF}, red 0.78431374, green 1.0, blue 0.25490198, alpha 1.0, hue 0.21491227, saturation 0.745098, and lightness 0.627451.
     * <pre>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHARTREUSE = 0xc8ff41ff;

    static {
        NAMED.put("chartreuse", 0xc8ff41ff);
        LIST.add(0xc8ff41ff);
    }

    /**
     * This color constant "cactus" has RGBA8888 code {@code 0x30A000FF}, red 0.1882353, green 0.627451, blue 0.0, alpha 1.0, hue 0.28333336, saturation 0.627451, and lightness 0.3137255.
     * <pre>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CACTUS = 0x30a000ff;

    static {
        NAMED.put("cactus", 0x30a000ff);
        LIST.add(0x30a000ff);
    }

    /**
     * This color constant "lime" has RGBA8888 code {@code 0x93D300FF}, red 0.5764706, green 0.827451, blue 0.0, alpha 1.0, hue 0.21721959, saturation 0.827451, and lightness 0.4137255.
     * <pre>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIME = 0x93d300ff;

    static {
        NAMED.put("lime", 0x93d300ff);
        LIST.add(0x93d300ff);
    }

    /**
     * This color constant "olive" has RGBA8888 code {@code 0x818000FF}, red 0.5058824, green 0.5019608, blue 0.0, alpha 1.0, hue 0.16537468, saturation 0.5058824, and lightness 0.2529412.
     * <pre>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int OLIVE = 0x818000ff;

    static {
        NAMED.put("olive", 0x818000ff);
        LIST.add(0x818000ff);
    }

    /**
     * This color constant "fern" has RGBA8888 code {@code 0x4E7942FF}, red 0.30588236, green 0.4745098, blue 0.25882354, alpha 1.0, hue 0.2969697, saturation 0.21568626, and lightness 0.36666664.
     * <pre>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FERN = 0x4e7942ff;

    static {
        NAMED.put("fern", 0x4e7942ff);
        LIST.add(0x4e7942ff);
    }

    /**
     * This color constant "moss" has RGBA8888 code {@code 0x204608FF}, red 0.1254902, green 0.27450982, blue 0.03137255, alpha 1.0, hue 0.26881722, saturation 0.24313727, and lightness 0.15294118.
     * <pre>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MOSS = 0x204608ff;

    static {
        NAMED.put("moss", 0x204608ff);
        LIST.add(0x204608ff);
    }

    /**
     * This color constant "celery" has RGBA8888 code {@code 0x7DFF73FF}, red 0.49019608, green 1.0, blue 0.4509804, alpha 1.0, hue 0.32142857, saturation 0.5490196, and lightness 0.7254902.
     * <pre>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CELERY = 0x7dff73ff;

    static {
        NAMED.put("celery", 0x7dff73ff);
        LIST.add(0x7dff73ff);
    }

    /**
     * This color constant "sage" has RGBA8888 code {@code 0xABE3C5FF}, red 0.67058825, green 0.8901961, blue 0.77254903, alpha 1.0, hue 0.4107143, saturation 0.21960783, and lightness 0.78039217.
     * <pre>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAGE = 0xabe3c5ff;

    static {
        NAMED.put("sage", 0xabe3c5ff);
        LIST.add(0xabe3c5ff);
    }

    /**
     * This color constant "jade" has RGBA8888 code {@code 0x3FBF3FFF}, red 0.24705882, green 0.7490196, blue 0.24705882, alpha 1.0, hue 0.33333334, saturation 0.5019608, and lightness 0.49803922.
     * <pre>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int JADE = 0x3fbf3fff;

    static {
        NAMED.put("jade", 0x3fbf3fff);
        LIST.add(0x3fbf3fff);
    }

    /**
     * This color constant "cyan" has RGBA8888 code {@code 0x00FFFFFF}, red 0.0, green 1.0, blue 1.0, alpha 1.0, hue 0.5, saturation 1.0, and lightness 0.5.
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CYAN = 0x00ffffff;

    static {
        NAMED.put("cyan", 0x00ffffff);
        LIST.add(0x00ffffff);
    }

    /**
     * This color constant "mint" has RGBA8888 code {@code 0x7FFFD4FF}, red 0.49803922, green 1.0, blue 0.83137256, alpha 1.0, hue 0.44401044, saturation 0.50196075, and lightness 0.7490196.
     * <pre>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MINT = 0x7fffd4ff;

    static {
        NAMED.put("mint", 0x7fffd4ff);
        LIST.add(0x7fffd4ff);
    }

    /**
     * This color constant "teal" has RGBA8888 code {@code 0x007F7FFF}, red 0.0, green 0.49803922, blue 0.49803922, alpha 1.0, hue 0.5, saturation 0.49803922, and lightness 0.24901961.
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TEAL = 0x007f7fff;

    static {
        NAMED.put("teal", 0x007f7fff);
        LIST.add(0x007f7fff);
    }

    /**
     * This color constant "turquoise" has RGBA8888 code {@code 0x2ED6C9FF}, red 0.18039216, green 0.8392157, blue 0.7882353, alpha 1.0, hue 0.48710316, saturation 0.65882355, and lightness 0.5098039.
     * <pre>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TURQUOISE = 0x2ed6c9ff;

    static {
        NAMED.put("turquoise", 0x2ed6c9ff);
        LIST.add(0x2ed6c9ff);
    }

    /**
     * This color constant "sky" has RGBA8888 code {@code 0x10C0E0FF}, red 0.0627451, green 0.7529412, blue 0.8784314, alpha 1.0, hue 0.5256411, saturation 0.8156863, and lightness 0.47058824.
     * <pre>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SKY = 0x10c0e0ff;

    static {
        NAMED.put("sky", 0x10c0e0ff);
        LIST.add(0x10c0e0ff);
    }

    /**
     * This color constant "cobalt" has RGBA8888 code {@code 0x0046ABFF}, red 0.0, green 0.27450982, blue 0.67058825, alpha 1.0, hue 0.5984406, saturation 0.67058825, and lightness 0.33529413.
     * <pre>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int COBALT = 0x0046abff;

    static {
        NAMED.put("cobalt", 0x0046abff);
        LIST.add(0x0046abff);
    }

    /**
     * This color constant "denim" has RGBA8888 code {@code 0x3088B8FF}, red 0.1882353, green 0.53333336, blue 0.72156864, alpha 1.0, hue 0.5588235, saturation 0.53333336, and lightness 0.45490196.
     * <pre>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int DENIM = 0x3088b8ff;

    static {
        NAMED.put("denim", 0x3088b8ff);
        LIST.add(0x3088b8ff);
    }

    /**
     * This color constant "navy" has RGBA8888 code {@code 0x000080FF}, red 0.0, green 0.0, blue 0.5019608, alpha 1.0, hue 0.6666667, saturation 0.5019608, and lightness 0.2509804.
     * <pre>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int NAVY = 0x000080ff;

    static {
        NAMED.put("navy", 0x000080ff);
        LIST.add(0x000080ff);
    }

    /**
     * This color constant "lavender" has RGBA8888 code {@code 0xB991FFFF}, red 0.7254902, green 0.5686275, blue 1.0, alpha 1.0, hue 0.72727275, saturation 0.43137252, and lightness 0.78431374.
     * <pre>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LAVENDER = 0xb991ffff;

    static {
        NAMED.put("lavender", 0xb991ffff);
        LIST.add(0xb991ffff);
    }

    /**
     * This color constant "plum" has RGBA8888 code {@code 0xBE0DC6FF}, red 0.74509805, green 0.050980393, blue 0.7764706, alpha 1.0, hue 0.82612616, saturation 0.7254902, and lightness 0.4137255.
     * <pre>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PLUM = 0xbe0dc6ff;

    static {
        NAMED.put("plum", 0xbe0dc6ff);
        LIST.add(0xbe0dc6ff);
    }

    /**
     * This color constant "mauve" has RGBA8888 code {@code 0xAB73ABFF}, red 0.67058825, green 0.4509804, blue 0.67058825, alpha 1.0, hue 0.8333334, saturation 0.21960786, and lightness 0.56078434.
     * <pre>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAUVE = 0xab73abff;

    static {
        NAMED.put("mauve", 0xab73abff);
        LIST.add(0xab73abff);
    }

    /**
     * This color constant "rose" has RGBA8888 code {@code 0xE61E78FF}, red 0.9019608, green 0.11764706, blue 0.47058824, alpha 1.0, hue 0.925, saturation 0.78431374, and lightness 0.5098039.
     * <pre>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ROSE = 0xe61e78ff;

    static {
        NAMED.put("rose", 0xe61e78ff);
        LIST.add(0xe61e78ff);
    }

    /**
     * This color constant "raspberry" has RGBA8888 code {@code 0x911437FF}, red 0.5686275, green 0.078431375, blue 0.21568628, alpha 1.0, hue 0.9533333, saturation 0.4901961, and lightness 0.32352945.
     * <pre>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RASPBERRY = 0x911437ff;

    static {
        NAMED.put("raspberry", 0x911437ff);
        LIST.add(0x911437ff);
    }

    /**
     * This color constant "transparent" has RGBA8888 code {@code 0x00000000}, red 0.0, green 0.0, blue 0.0, alpha 0.0, hue 0.0, saturation 0.0, and lightness 0.0.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TRANSPARENT = 0x00000000;

    static {
        NAMED.put("transparent", 0x00000000);
        LIST.add(0x00000000);
    }

    /**
     * All names for colors in this palette, in alphabetical order. You can fetch the corresponding packed int color
     * by looking up a name in {@link #NAMED}.
     */
    public static final ObjectList<String> NAMES = NAMED.order();

    static {
        NAMED.setDefaultValue(PLACEHOLDER);
        NAMES.sort(null);
    }

    /**
     * All names for colors in this palette, with grayscale first, then sorted by hue from red to yellow to green to
     * blue. You can fetch the corresponding packed int color by looking up a name in {@link #NAMED}. This does not
     * include aliases.
     */
    public static final ObjectList<String> NAMES_BY_HUE = new ObjectList<>(NAMES);
    /**
     * The packed Oklab int colors that correspond to items in {@link #NAMES_BY_HUE}, with the same order.
     */
    public static final IntList COLORS_BY_HUE = new IntList(NAMES_BY_HUE.size());
    /**
     * All names for colors in this palette, sorted by lightness from black to white. You can fetch the
     * corresponding packed int color by looking up a name in {@link #NAMED}. This does not include aliases.
     */
    public static final ObjectList<String> NAMES_BY_LIGHTNESS = new ObjectList<>(NAMES);

    /**
     * Gets an RGBA int color given a float per channel.
     * This method maps each channel from the 0 to 1 float range here, to the 0 to 255 int range in the result.
     * @param r red channel; will be clamped between 0 and 1 if it isn't already
     * @param g green channel; will be clamped between 0 and 1 if it isn't already
     * @param b blue channel; will be clamped between 0 and 1 if it isn't already
     * @param a alpha channel; will be clamped between 0 and 1 if it isn't already
     * @return the RGBA color matching the given channels, as an RGBA8888 int
     */
    public static int rgba(float r, float g, float b, float a) {
        return
                Math.min(Math.max((int)(r * 255.999f), 0), 255) << 24
                        | Math.min(Math.max((int)(g * 255.999f), 0), 255) << 16
                        | Math.min(Math.max((int)(b * 255.999f), 0), 255) << 8
                        | Math.min(Math.max((int)(a * 255.999f), 0), 255);
    }
    /**
     * Converts a packed float color in the format libGDX uses (ABGR7888) to an RGBA8888 int as this class uses.
     * @param packed a packed float color in ABGR7888 format
     * @return an RGBA8888 int color
     */
    public static int fromFloat(final float packed) {
        final int rev = BitConversion.floatToReversedIntBits(packed);
        return rev | (rev >>> 7 & 1);
    }

    /**
     * Takes a color encoded as an RGBA8888 int and converts to a packed float in the ABGR7888 format libGDX uses.
     * @param rgba an int with the channels (in order) red, green, blue, alpha; should have 8 bits per channel
     * @return a packed float as ABGR7888
     */
    public static float toFloat(final int rgba) {
        return BitConversion.reversedIntBitsToFloat(rgba & 0xFFFFFFFE);
    }

    public static int redInt(int rgba) {
        return rgba >>> 24;
    }
    
    public static float red(int rgba) {
        return (rgba >>> 24) * (1f/255f);
    }
    
    public static int greenInt(int rgba) {
        return rgba >>> 16 & 255;
    }
    
    public static float green(int rgba) {
        return (rgba >>> 16 & 255) * (1f/255f);
    }

    public static int blueInt(int rgba) {
        return rgba >>> 8 & 255;
    }

    public static float blue(int rgba) {
        return (rgba >>> 8 & 255) * (1f/255f);
    }

    public static int alphaInt(int rgba) {
        return rgba & 255;
    }

    public static float alpha(int rgba) {
        return (rgba & 254) * (1f/254f);
    }

    /**
     * Given a packed int color and a channel value from 0 to 3, gets the value of that channel as a float from 0.0f
     * to 1.0f . Channel 0 refers to R in RGBA8888 and H in {@link #rgb2hsl(float, float, float, float) HSLA} ints,
     * channel 1 refers to G or S, 2 refers to B or L, and 3 always refers to A.
     *
     * @param color   a packed int color in any 32-bit, 4-channel format
     * @param channel which channel to access, as an index from 0 to 3 inclusive
     * @return the non-packed float value of the requested channel, from 0.0f to 1.0f inclusive
     */
    public static float channel(int color, int channel) {
        return (color >>> 24 - ((channel & 3) << 3) & 255) / 255f;
    }

    /**
     * Given a packed int color and a channel value from 0 to 3, gets the value of that channel as an int from 0 to
     * 255 . Channel 0 refers to R in RGBA8888 and H in {@link #rgb2hsl(float, float, float, float) HSLA} ints,
     * channel 1 refers to G or S, 2 refers to B or L, and 3 always refers to A.
     *
     * @param color   a packed int color in any 32-bit, 4-channel format
     * @param channel which channel to access, as an index from 0 to 3 inclusive
     * @return the int value of the requested channel, from 0 to 255 inclusive
     */
    public static int channelInt(int color, int channel) {
        return color >>> 24 - ((channel & 3) << 3) & 255;
    }

    /**
     * Converts the four HSLA components, each in the 0.0 to 1.0 range, to an int in RGBA8888 format.
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSL(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     * The {@code h} parameter for hue can be lower than 0.0 or higher than 1.0 because the hue "wraps around;" only the
     * fractional part of h is used. The other parameters must be between 0.0 and 1.0 (inclusive) to make sense.
     *
     * @param h hue, usually from 0.0 to 1.0, but only the fractional part is used
     * @param s saturation, from 0.0 to 1.0
     * @param l lightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsl2rgb(final float h, final float s, final float l, final float a) {
        float hue = h - MathTools.fastFloor(h);
        float x = Math.min(Math.max(Math.abs(hue * 6f - 3f) - 1f, 0f), 1f);
        float y = hue + 2f / 3f;
        float z = hue + 1f / 3f;
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        float v = l + s * Math.min(l, 1f - l);
        float d = 2f * (1f - l / (v + 1e-10f));
        v *= 255.999f;
        return (int)(v*MathTools.lerp(1f, x, d)) << 24 | (int)(v*MathTools.lerp(1f, y, d)) << 16 | (int)(v*MathTools.lerp(1f, z, d)) << 8 | (int)(a * 255.999f);
    }

    /**
     * Converts the four RGBA components, each in the 0.0 to 1.0 range, to an int in HSLA format (hue,
     * saturation, lightness, alpha). This format is exactly like RGBA8888 but treats what would normally be red as hue,
     * green as saturation, and blue as lightness; alpha is the same.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an "HSLA-format" int
     */
    public static int rgb2hsl(final float r, final float g, final float b, final float a) {
        float x, y, z, w;
        if (g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        } else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if (r < x) {
            z = w;
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        float l = x * (1f - 0.5f * d / (x + 1e-10f));
        return (int)(Math.abs(z + (w - y) / (6f * d + 1e-10f)) * 255.999f) << 24 | (int)((x - l) / (Math.min(l, 1f - l) + 1e-10f) * 255.999f) << 16 | (int)(l * 255.999f) << 8 | (int)(a * 255.999f);
    }

    /**
     * Converts the four HSBA/HSVA components, each in the 0.0 to 1.0 range, to an int in RGBA8888 format.
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSL(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     * HSV and HSB are synonyms; it makes a little more sense to call the third channel brightness.
     * The {@code h} parameter for hue can be lower than 0.0 or higher than 1.0 because the hue "wraps around;" only the
     * fractional part of h is used. The other parameters must be between 0.0 and 1.0 (inclusive) to make sense.
     *
     * @param h hue, from 0.0 to 1.0
     * @param s saturation, from 0.0 to 1.0
     * @param b brightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an RGBA8888-format int
     */
    public static int hsb2rgb(final float h, final float s, final float b, final float a) {
        float hue = h - MathTools.fastFloor(h);
        float x = Math.min(Math.max(Math.abs(hue * 6f - 3f) - 1f, 0f), 1f);
        float y = hue + 2f / 3f;
        float z = hue + 1f / 3f;
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        float v = b * 255.999f;
//        return ((int)(r * 255.999f) << 24) | ((int)(g * 255.999f) << 16) | ((int)(b * 255.999f) << 8) | (int)(a * 255.999f);
        return (int) (v * MathTools.lerp(1f, x, s)) << 24 | (int) (v * MathTools.lerp(1f, y, s)) << 16 | (int) (v * MathTools.lerp(1f, z, s)) << 8 | (int)(a * 255.999f);
    }

    /**
     * Converts the four RGBA components, each in the 0.0 to 1.0 range, to an int in HSBA/HSVA format (hue,
     * saturation, brightness/value, alpha). This format is exactly like RGBA8888 but treats what would normally be red
     * as hue, green as saturation, and blue as brightness/value; alpha is the same. HSV and HSB are synonyms; it makes
     * a little more sense to call the third channel brightness.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return an "HSBA/HSVA-format" int
     */
    public static int rgb2hsb(final float r, final float g, final float b, final float a) {
        float v = Math.max(Math.max(r, g), b);
        float n = Math.min(Math.min(r, g), b);
        float c = v - n;
        float h;
        if (c == 0) h = 0f;
        else if (v == r) h = (g - b) / c / 6f;
        else if (v == g) h = ((b - r) / c + 2f) / 6f;
        else h = ((r - g) / c + 4f) / 6f;
        return (int)(h * 255.999f) << 24 | (int)((v == 0 ? 0f : c / v) * 255.999f) << 16 | (int)(v * 255.999f) << 8 | (int)(a * 255.999f);
//        return ((int)(r * 255.999f) << 24) | ((int)(g * 255.999f) << 16) | ((int)(b * 255.999f) << 8) | (int)(a * 255.999f);
    }

    /**
     * Gets the hue of the given RGBA8888 color, as a float from 0f (inclusive, red and approaching orange
     * if increased) to 1f (exclusive, red and approaching purple if decreased).
     * @param rgba a color as an RGBA8888 int
     * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
     * eventually to purple before looping back to almost the same red (1.0, exclusive)
     */
    public static float hue(final int rgba) {
        final float r = (rgba >>> 24 & 0xff) / 255f;
        final float g = (rgba >>> 16 & 0xff) / 255f;
        final float b = (rgba >>> 8  & 0xff) / 255f;
        float x, y, z, w;
        if(g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        }
        else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if(r < x) {
            z = w;
            w = r;
        }
        else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        return Math.abs(z + (w - y) / (6f * d + 1e-10f));
    }

    /**
     * Gets the saturation of the given RGBA8888 color, as a float ranging from 0.0f to 1.0f, inclusive.
     * @param rgba a color as an RGBA8888 int
     * @return the saturation of the color from 0.0 (a grayscale color; inclusive) to 1.0 (a bright color, inclusive)
     */
    public static float saturation(final int rgba) {
        final float r = (rgba >>> 24 & 0xff) / 255f;
        final float g = (rgba >>> 16 & 0xff) / 255f;
        final float b = (rgba >>> 8  & 0xff) / 255f;
        float x, y, w;
        if(g < b) {
            x = b;
            y = g;
        }
        else {
            x = g;
            y = b;
        }
        if(r < x) {
            w = r;
        }
        else {
            w = x;
            x = r;
        }
        return x - Math.min(w, y);
    }

    /**
     * Gets the lightness of the given RGBA8888 color, as a float ranging from 0.0f to 1.0f, inclusive.
     * This uses the same measurement as {@link #rgb2hsl(float, float, float, float)}.
     * @param rgba a color as an RGBA8888 int
     * @return the lightness of the color from 0.0 (black; inclusive) to 1.0 (white; inclusive)
     */
    public static float lightness(final int rgba) {
        final float r = (rgba >>> 24 & 0xff) / 255f;
        final float g = (rgba >>> 16 & 0xff) / 255f;
        final float b = (rgba >>> 8  & 0xff) / 255f;
        float x, y, w;
        if(g < b) {
            x = b;
            y = g;
        }
        else {
            x = g;
            y = b;
        }
        if(r < x) {
            w = r;
        }
        else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        return x * (1f - 0.5f * d / (x + 1e-10f));
    }

    /**
     * Gets the brightness of the given RGBA8888 color, as a float ranging from 0.0f to 1.0f, inclusive.
     * This uses the same measurement as {@link #rgb2hsb(float, float, float, float)}. Note that while
     * {@link #lightness(int)} returns 1.0 only if given white as its input, this returns 1.0 for white,
     * all colors that are as colorful as possible (the ones with the highest chroma), and all colors in
     * between white and one of those high-chroma colors. Most usage that compares colors for perceptual
     * lightness will want {@link #lightness(int)}, not this method.
     * @param rgba a color as an RGBA8888 int
     * @return the brightness of the color from 0.0 (black; inclusive) to 1.0 (any bright color and/or white; inclusive)
     */
    public static float brightness(final int rgba) {
        final int r = (rgba >>> 24 & 0xff);
        final int g = (rgba >>> 16 & 0xff);
        final int b = (rgba >>> 8  & 0xff);
        return Math.max(Math.max(r, g), b) / 255f;
    }
    /**
     * Interpolates from the RGBA8888 color start towards white by change. While change should be between 0f (return
     * start as-is) and 1f (return white), start should be an RGBA8888 int color, as from
     * {@link #rgba(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors, and
     * is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp towards
     * white. Unlike {@link #lerpColors(int, int, float)}, this keeps the alpha of start as-is.
     * @see #darken(int, float) the counterpart method that darkens a float color
     * @param start the starting color as an RGBA8888 int
     * @param change how much to go from start toward white, as a float between 0 and 1; higher means closer to white
     * @return an RGBA8888 int that represents a color between start and white
     */
    public static int lighten(final int start, final float change) {
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF, a = start & 0x000000FE;
        return (a |
                ((int) (b + (0xFF - b) * change) & 0xFF) << 8 |
                        ((int) (g + (0xFF - g) * change) & 0xFF) << 16 |
                        ((int) (r + (0xFF - r) * change) & 0xFF) << 24
        );
    }

    /**
     * Interpolates from the RGBA8888 color start towards black by change. While change should be between 0f (return
     * start as-is) and 1f (return black), start should be an RGBA8888 int color, as from
     * {@link #rgba(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors, and
     * is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp towards
     * black. Unlike {@link #lerpColors(int, int, float)}, this keeps the alpha of start as-is.
     * @see #lighten(int, float) the counterpart method that darkens a float color
     * @param start the starting color as an RGBA8888 int
     * @param change how much to go from start toward black, as a float between 0 and 1; higher means closer to black
     * @return an RGBA8888 int that represents a color between start and black
     */
    public static int darken(final int start, final float change) {
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF, a = start & 0x000000FE;
        return (
                ((int) (r * (1f - change)) & 0xFF) << 24 |
                        ((int) (g * (1f - change)) & 0xFF) << 16 |
                        ((int) (b * (1f - change)) & 0xFF) << 8 |
                        a);
    }

    /**
     * Interpolates from the RGBA8888 int color rgba towards either white or black, depending on change. The value
     * for change should be between -1f and 1f; negative values interpolate toward black, while positive ones
     * interpolate toward white. The value for rgba should be n RGBA8888 int, as from a constant here.
     *
     * @param rgba      the starting color as an RGBA8888 int
     * @param change how much to go away from rgba, as a float between -1 and 1; negative to black, positive to white
     * @return an RGBA8888 int that represents a color between start and black or white
     * @see #darken(int, float) the counterpart method that darkens a float color
     * @see #lighten(int, float) the counterpart method that lightens a float color
     */
    public static int adjustLightness(final int rgba, final float change) {
        if(change < 0.0f) return darken(rgba, -change);
        return lighten(rgba, change);
    }

    /**
     * Brings the chromatic components of {@code start} closer to grayscale by {@code change} (desaturating them). While
     * change should be between 0f (return start as-is) and 1f (return fully gray), start should be an RGBA8888 int color, as
     * from {@link #rgba(float, float, float, float)}. This leaves alpha alone.
     * <br>
     * <a href="http://www.graficaobscura.com/matrix/index.html">The algorithm used is from here</a>.
     * @see #enrich(int, float) the counterpart method that makes a float color more saturated
     * @param start the starting color as an RGBA8888 int
     * @param change how much to change start to a desaturated color, as a float between 0 and 1; higher means a less saturated result
     * @return an RGBA8888 int that represents a color between start and a desaturated color
     */
    public static int dullen(final int start, final float change) {
        final float rc = 0.32627f, gc = 0.3678f, bc = 0.30593001f;
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF, a = start & 0x000000FE;
        final float ch = 1f - change, rw = change * rc, gw = change * gc, bw = change * bc;
        return (
                (int) Math.min(Math.max(r * (rw+ch) + g * rw + b * rw, 0), 255) << 24 |
                        (int) Math.min(Math.max(r * gw + g * (gw+ch) + b * gw, 0), 255) << 16 |
                        (int) Math.min(Math.max(r * bw + g * bw + b * (bw+ch), 0), 255) << 8 |
                        a);
    }

    /**
     * Pushes the chromatic components of {@code start} away from grayscale by change (saturating them). While change
     * should be between 0f (return start as-is) and 1f (return maximally saturated), start should be an RGBA8888 int
     * color, as from {@link #rgba(float, float, float, float)}.
     * <br>
     * <a href="http://www.graficaobscura.com/matrix/index.html">The algorithm used is from here</a>.
     * @see #dullen(int, float) the counterpart method that makes a float color less saturated
     * @param start the starting color as an RGBA8888 int
     * @param change how much to change start to a saturated color, as a float between 0 and 1; higher means a more saturated result
     * @return an RGBA8888 int that represents a color between start and a saturated color
     */
    public static int enrich(final int start, final float change) {
        final float rc = 0.32627f, gc = 0.3678f, bc = 0.30593001f;
        final int r = start >>> 24, g = start >>> 16 & 0xFF, b = start >>> 8 & 0xFF, a = start & 0x000000FE;        final float ch = 1f + change, rw = (-change) * rc, gw = (-change) * gc, bw = (-change) * bc;
        return (
                (int) Math.min(Math.max(r * (rw+ch) + g * rw + b * rw, 0), 255) << 24 |
                        (int) Math.min(Math.max(r * gw + g * (gw+ch) + b * gw, 0), 255) << 16 |
                        (int) Math.min(Math.max(r * bw + g * bw + b * (bw+ch), 0), 255) << 8 |
                        a);
    }

    /**
     * Interpolates from the RGBA8888 int color start towards end by change. Both start and end should be RGBA8888
     * ints, and change can be between 0f (keep start) and 1f (only use end). This is a good way to reduce allocations
     * of temporary Colors.
     *
     * @param s      the starting color as a packed int
     * @param e      the end/target color as a packed int
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return an RGBA8888 int that represents a color between start and end
     */
    public static int lerpColors(final int s, final int e, final float change) {
        final int
                sA = s & 0xFE, sB = s >>> 8 & 0xFF, sG = s >>> 16 & 0xFF, sR = s >>> 24 & 0xFF,
                eA = e & 0xFE, eB = e >>> 8 & 0xFF, eG = e >>> 16 & 0xFF, eR = e >>> 24 & 0xFF;
        return ((int) (sR + change * (eR - sR)) & 0xFF) << 24
                | ((int) (sG + change * (eG - sG)) & 0xFF) << 16
                | ((int) (sB + change * (eB - sB)) & 0xFF) << 8
                | (int) (sA + change * (eA - sA)) & 0xFE;
    }


    /**
     * Given several colors, this gets an even mix of all colors in equal measure.
     * If {@code colors} is null or has no items, this returns {@link #PLACEHOLDER}.
     * This is mostly useful in conjunction with {@link IntList}, using its {@code items}
     * for colors, typically 0 for offset, and its {@code size} for size.
     * @param colors an array of RGBA8888 int colors; all should use the same color space
     * @param offset the index of the first item in {@code colors} to use
     * @param size how many items from {@code colors} to use
     * @return an even mix of all colors given, as an RGBA8888 int color
     */
    public static int mix(int[] colors, int offset, int size) {
        int end = offset + size;
        if(colors == null || colors.length < end || offset < 0 || size <= 0)
            return PLACEHOLDER; // transparent super-dark-blue, used to indicate "not found"
        int result = PLACEHOLDER;
        while(colors[offset] == PLACEHOLDER)
        {
            offset++;
        }
        if(offset < end)
            result = colors[offset];
        for (int i = offset + 1, o = end, denom = 2; i < o; i++, denom++) {
            if(colors[i] != PLACEHOLDER)
                result = lerpColors(result, colors[i], 1f / denom);
            else --denom;
        }
        return result;
    }

    /**
     * Mixes any number of colors with arbitrary weights per-color. Takes an array of varargs of alternating ints
     * representing colors and weights, as with {@code color, weight, color, weight...}.
     * If {@code colors} is null or has no items, this returns {@link #PLACEHOLDER}. Each color
     * should be an RGBA8888 int, and each weight should be greater than 0.
     * @param colors an array or varargs that should contain alternating {@code color, weight, color, weight...} ints
     * @return the mixed color, as an RGBA8888 int
     */
    public static int unevenMix(int... colors) {
        if(colors == null || colors.length == 0) return PLACEHOLDER;
        if(colors.length <= 2) return colors[0];
        return unevenMix(colors, 0, colors.length);
    }

    /**
     * Mixes any number of colors with arbitrary weights per-color. Takes an array of alternating ints representing
     * colors and weights, as with {@code color, weight, color, weight...}, starting at {@code offset} in the array and
     * continuing for {@code size} indices in the array. The {@code size} should be an even number 2 or greater,
     * otherwise it will be reduced by 1. The weights can be any non-negative int values; this method handles
     * normalizing them internally. Each color should an RGBA8888 int, and each weight should be greater than 0.
     * If {@code colors} is null or has no items, or if size &lt;= 0, this returns {@link #PLACEHOLDER}.
     *
     * @param colors starting at {@code offset}, this should contain alternating {@code color, weight, color, weight...} ints
     * @param offset where to start reading from in {@code colors}
     * @param size how many indices to read from {@code colors}; must be an even number
     * @return the mixed color, as an RGBA8888 int
     */
    public static int unevenMix(int[] colors, int offset, int size) {
        size &= -2;
        final int end = offset + size;
        if(colors == null || colors.length < end || offset < 0 || size <= 0)
            return PLACEHOLDER;
        while(colors[offset] == PLACEHOLDER)
        {
            if((offset += 2) >= end) return PLACEHOLDER;
        }
        int result = colors[offset];
        float current = colors[offset + 1], total = current;
        for (int i = offset+3; i < end; i += 2) {
            if(colors[i-1] != PLACEHOLDER)
                total += colors[i];
        }
        total = 1f / total;
        current *= total;
        for (int i = offset+3; i < end; i += 2) {
            int mixColor = colors[i-1];
            if(mixColor == PLACEHOLDER)
                continue;
            float weight = colors[i] * total;
            result = lerpColors(result, mixColor, weight / (current += weight));
        }
        return result;
    }

    static {
        NAMES_BY_HUE.sort((o1, o2) -> {
            final int c1 = NAMED.get(o1), c2 = NAMED.get(o2);
            if (c1 >= 0) return -10000;
            else if (c2 >= 0) return 10000;

            final float s1 = DescriptiveColor.chroma(c1), s2 = DescriptiveColor.chroma(c2);
            if (s1 <= 0x1p-6f && s2 > 0x1p-6f)
                return -1000;
            else if (s1 > 0x1p-6f && s2 <= 0x1p-6f)
                return 1000;
            else if (s1 <= 0x1p-6f && s2 <= 0x1p-6f)
                return (c1 & 255) - (c2 & 255);
            else
                return ((int) Math.signum(DescriptiveColor.hue(c1) - DescriptiveColor.hue(c2)) << 8)
                        + (c1 & 255) - (c2 & 255);
        });
        for (String name : NAMES_BY_HUE) {
            COLORS_BY_HUE.add(NAMED.get(name));
        }
        NAMES_BY_LIGHTNESS.sort((o1, o2) -> (NAMED.get(o1) + 0x80000000 & 0x800000FF) - (NAMED.get(o2) + 0x80000000 & 0x800000FF));

        ALIASES.put("grey", GRAY);
        ALIASES.put("gold", SAFFRON);
        ALIASES.put("puce", MAUVE);
        ALIASES.put("sand", TAN);
        ALIASES.put("skin", PEACH); // Yes, I am aware that there is more than one skin color, but this can only map to one.
        ALIASES.put("coral", SALMON);
        ALIASES.put("azure", SKY);
        ALIASES.put("ocean", TEAL);
        ALIASES.put("sapphire", COBALT);
        NAMED.putAll(ALIASES);
    }
}
