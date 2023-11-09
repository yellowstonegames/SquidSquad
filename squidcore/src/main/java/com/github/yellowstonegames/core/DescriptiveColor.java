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

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectIntOrderedMap;
import com.github.tommyettinger.ds.ObjectList;
import org.checkerframework.checker.nullness.qual.NonNull;
import regexodus.MatchResult;
import regexodus.Pattern;
import regexodus.Replacer;
import regexodus.Substitution;
import regexodus.TextBuffer;

import static com.github.tommyettinger.digital.MathTools.fastFloor;
import static com.github.yellowstonegames.core.Gamut.GAMUT_DATA;

/**
 * A palette of predefined colors as packed Oklab ints, and tools for obtaining Oklab int colors from a description.
 * An Oklab int color isn't especially usable on its own, but you can pass one to {@link #toRGBA8888(int)} to get an RGBA
 * color as an int (typically used with {@link DigitTools#hex(int)} to get a value like
 * FF7F00FF, which in RGBA8888 format is orange). The Oklab color space is used here because allows mixing two or more
 * colors very smoothly, without introducing erroneous hues or having dips or ridges in lightness. Oklab is a whole
 * complex color space, but most usage is probably going to only need {@link #describe(CharSequence)}, which sidesteps
 * Oklab entirely and returns an RGBA8888 int, or maybe some operations on entries in the palette. The aforementioned
 * describe() is used by {@link #processColorMarkup(CharSequence)}, which also takes a color description and translates
 * it to a libGDX-compatible color markup tag. The syntax for color descriptions is in
 * {@link #describe(CharSequence)}; it looks like "lighter rich pink orange", to give an idea of it.
 * <br>
 * For the palette, you can access colors by their constant name, such as {@code cactus}, by the {@link #NAMED} map
 * using {@code NAMED.get("cactus")}, by getting a color by its name's position in alphabetical order with
 * {@code NAMED.getAt(12)}, or by index in the IntList called {@link #LIST}. When accessing a color with
 * {@link ObjectIntOrderedMap#get(Object)}, if the name is not found, get() will return {@link #TRANSPARENT}. If you
 * want to control the not-found value, you can use {@link ObjectIntOrderedMap#getOrDefault(Object, int)}. You can
 * access the names in a specific order with {@link #NAMES} (which is alphabetical), {@link #NAMES_BY_HUE} (which is
 * sorted by the hue of the matching color, from red to yellow to blue (with gray around here) to purple to red again),
 * or {@link #NAMES_BY_LIGHTNESS} (which is sorted by the intensity of the matching color, from darkest to lightest).
 * Having a name lets you look up the matching color in {@link #NAMED}. You can also modify or re-order NAMED if you
 * want to, such as to add more named colors.
 */
public final class DescriptiveColor {
    /**
     * No need to extend this.
     */
    private DescriptiveColor() {
    }
    /**
     * You can look up colors by name here; the names are lower-case, and the colors are packed ints in Oklab format.
     */
    public static final ObjectIntOrderedMap<String> NAMED = new ObjectIntOrderedMap<>(50);
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

    public static final IntList LIST = new IntList(50);

    /**
     * A fully-transparent color that is out-of-range for valid colors in Oklab, to be used as a placeholder for colors
     * that aren't valid in some way.
     */
    public static final int PLACEHOLDER = 0x00000100;

    /**
     * This color constant "transparent" has RGBA8888 code {@code 0x00000000}, L 0.0, A 0.49803922, B 0.49803922, alpha 0.0, hue 0.0, saturation 0.0, and chroma 0.0055242716.
     * It has the encoded Oklab value {@code 0x007f7f00}.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TRANSPARENT = 0x007f7f00;
    static { NAMED.put("transparent", 0x007f7f00); LIST.add(0x007f7f00); }

    /**
     * This color constant "black" has RGBA8888 code {@code 0x000000FF}, L 0.0, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.0, saturation 0.0, and chroma 0.0055242716.
     * It has the encoded Oklab value {@code 0xff7f7f00}.
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLACK = 0xff7f7f00;
    static { NAMED.put("black", 0xff7f7f00); LIST.add(0xff7f7f00); }

    /**
     * This color constant "gray" has RGBA8888 code {@code 0x808080FF}, L 0.5019608, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.0, saturation 0.0, and chroma 0.0055242716.
     * It has the encoded Oklab value {@code 0xff7f7f80}.
     * <pre>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GRAY = 0xff7f7f80;
    static { NAMED.put("gray", 0xff7f7f80); LIST.add(0xff7f7f80); }

    /**
     * This color constant "silver" has RGBA8888 code {@code 0xB6B6B6FF}, L 0.7137255, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.0, saturation 0.0, and chroma 0.0055242716.
     * It has the encoded Oklab value {@code 0xff7f7fb6}.
     * <pre>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SILVER = 0xff7f7fb6;
    static { NAMED.put("silver", 0xff7f7fb6); LIST.add(0xff7f7fb6); }

    /**
     * This color constant "white" has RGBA8888 code {@code 0xFFFFFFFF}, L 1.0, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.0, saturation 0.0, and chroma 0.0055242716.
     * It has the encoded Oklab value {@code 0xff7f7fff}.
     * <pre>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int WHITE = 0xff7f7fff;
    static { NAMED.put("white", 0xff7f7fff); LIST.add(0xff7f7fff); }

    /**
     * This color constant "red" has RGBA8888 code {@code 0xFF0000FF}, L 0.49803922, A 0.6117647, B 0.56078434, alpha 1.0, hue 0.0802403, saturation 0.98868626, and chroma 0.25345513.
     * It has the encoded Oklab value {@code 0xff8f9c7f}.
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RED = 0xff8f9c7f;
    static { NAMED.put("red", 0xff8f9c7f); LIST.add(0xff8f9c7f); }

    /**
     * This color constant "orange" has RGBA8888 code {@code 0xFF7F00FF}, L 0.6431373, A 0.54901963, B 0.57254905, alpha 1.0, hue 0.1544989, saturation 0.9208691, and chroma 0.17443058.
     * It has the encoded Oklab value {@code 0xff928ca4}.
     * <pre>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ORANGE = 0xff928ca4;
    static { NAMED.put("orange", 0xff928ca4); LIST.add(0xff928ca4); }

    /**
     * This color constant "yellow" has RGBA8888 code {@code 0xFFFF00FF}, L 0.9529412, A 0.4627451, B 0.59607846, alpha 1.0, hue 0.30499697, saturation 0.94895214, and chroma 0.20529193.
     * It has the encoded Oklab value {@code 0xff9876f3}.
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int YELLOW = 0xff9876f3;
    static { NAMED.put("yellow", 0xff9876f3); LIST.add(0xff9876f3); }

    /**
     * This color constant "green" has RGBA8888 code {@code 0x00FF00FF}, L 0.80784315, A 0.38039216, B 0.5882353, alpha 1.0, hue 0.39589924, saturation 0.9947925, and chroma 0.29610303.
     * It has the encoded Oklab value {@code 0xff9661ce}.
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GREEN = 0xff9661ce;
    static { NAMED.put("green", 0xff9661ce); LIST.add(0xff9661ce); }

    /**
     * This color constant "blue" has RGBA8888 code {@code 0x0000FFFF}, L 0.3019608, A 0.48235294, B 0.34117648, alpha 1.0, hue 0.7341372, saturation 0.9925803, and chroma 0.31835338.
     * It has the encoded Oklab value {@code 0xff577b4d}.
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLUE = 0xff577b4d;
    static { NAMED.put("blue", 0xff577b4d); LIST.add(0xff577b4d); }

    /**
     * This color constant "indigo" has RGBA8888 code {@code 0x520FE0FF}, L 0.31764707, A 0.53333336, B 0.36862746, alpha 1.0, hue 0.79237556, saturation 0.95014614, and chroma 0.27001202.
     * It has the encoded Oklab value {@code 0xff5e8851}.
     * <pre>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int INDIGO = 0xff5e8851;
    static { NAMED.put("indigo", 0xff5e8851); LIST.add(0xff5e8851); }

    /**
     * This color constant "violet" has RGBA8888 code {@code 0x9040EFFF}, L 0.4509804, A 0.56078434, B 0.39607844, alpha 1.0, hue 0.8377986, saturation 0.74460185, and chroma 0.23984502.
     * It has the encoded Oklab value {@code 0xff658f73}.
     * <pre>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int VIOLET = 0xff658f73;
    static { NAMED.put("violet", 0xff658f73); LIST.add(0xff658f73); }

    /**
     * This color constant "purple" has RGBA8888 code {@code 0xC000FFFF}, L 0.4862745, A 0.6039216, B 0.3882353, alpha 1.0, hue 0.8721067, saturation 0.96042717, and chroma 0.30403575.
     * It has the encoded Oklab value {@code 0xff639a7c}.
     * <pre>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PURPLE = 0xff639a7c;
    static { NAMED.put("purple", 0xff639a7c); LIST.add(0xff639a7c); }

    /**
     * This color constant "brown" has RGBA8888 code {@code 0x8F573BFF}, L 0.4, A 0.5254902, B 0.5294118, alpha 1.0, hue 0.13559444, saturation 0.5315073, and chroma 0.07753685.
     * It has the encoded Oklab value {@code 0xff878666}.
     * <pre>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BROWN = 0xff878666;
    static { NAMED.put("brown", 0xff878666); LIST.add(0xff878666); }

    /**
     * This color constant "pink" has RGBA8888 code {@code 0xFFA0E0FF}, L 0.7647059, A 0.5568628, B 0.4745098, alpha 1.0, hue 0.9394406, saturation 0.3365728, and chroma 0.124142565.
     * It has the encoded Oklab value {@code 0xff798ec3}.
     * <pre>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PINK = 0xff798ec3;
    static { NAMED.put("pink", 0xff798ec3); LIST.add(0xff798ec3); }

    /**
     * This color constant "magenta" has RGBA8888 code {@code 0xF500F5FF}, L 0.5647059, A 0.6313726, B 0.41568628, alpha 1.0, hue 0.9119405, saturation 0.96295136, and chroma 0.31098264.
     * It has the encoded Oklab value {@code 0xff6aa190}.
     * <pre>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAGENTA = 0xff6aa190;
    static { NAMED.put("magenta", 0xff6aa190); LIST.add(0xff6aa190); }

    /**
     * This color constant "brick" has RGBA8888 code {@code 0xD5524AFF}, L 0.5019608, A 0.5686275, B 0.53333336, alpha 1.0, hue 0.07379155, saturation 0.60073465, and chroma 0.15199278.
     * It has the encoded Oklab value {@code 0xff889180}.
     * <pre>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRICK = 0xff889180;
    static { NAMED.put("brick", 0xff889180); LIST.add(0xff889180); }

    /**
     * This color constant "ember" has RGBA8888 code {@code 0xF55A32FF}, L 0.56078434, A 0.57254905, B 0.5568628, alpha 1.0, hue 0.10636183, saturation 0.80691457, and chroma 0.1836353.
     * It has the encoded Oklab value {@code 0xff8e928f}.
     * <pre>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int EMBER = 0xff8e928f;
    static { NAMED.put("ember", 0xff8e928f); LIST.add(0xff8e928f); }

    /**
     * This color constant "salmon" has RGBA8888 code {@code 0xFF6262FF}, L 0.6, A 0.5803922, B 0.53333336, alpha 1.0, hue 0.06444036, saturation 0.6092618, and chroma 0.17337766.
     * It has the encoded Oklab value {@code 0xff889499}.
     * <pre>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SALMON = 0xff889499;
    static { NAMED.put("salmon", 0xff889499); LIST.add(0xff889499); }

    /**
     * This color constant "chocolate" has RGBA8888 code {@code 0x683818FF}, L 0.27058825, A 0.5254902, B 0.53333336, alpha 1.0, hue 0.14479145, saturation 0.76011693, and chroma 0.0835974.
     * It has the encoded Oklab value {@code 0xff888645}.
     * <pre>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHOCOLATE = 0xff888645;
    static { NAMED.put("chocolate", 0xff888645); LIST.add(0xff888645); }

    /**
     * This color constant "tan" has RGBA8888 code {@code 0xD2B48CFF}, L 0.7254902, A 0.5058824, B 0.5254902, alpha 1.0, hue 0.20570697, saturation 0.3165265, and chroma 0.052115876.
     * It has the encoded Oklab value {@code 0xff8681b9}.
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TAN = 0xff8681b9;
    static { NAMED.put("tan", 0xff8681b9); LIST.add(0xff8681b9); }

    /**
     * This color constant "bronze" has RGBA8888 code {@code 0xCE8E31FF}, L 0.60784316, A 0.5137255, B 0.56078434, alpha 1.0, hue 0.21101025, saturation 0.8246211, and chroma 0.124142565.
     * It has the encoded Oklab value {@code 0xff8f839b}.
     * <pre>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRONZE = 0xff8f839b;
    static { NAMED.put("bronze", 0xff8f839b); LIST.add(0xff8f839b); }

    /**
     * This color constant "cinnamon" has RGBA8888 code {@code 0xD2691DFF}, L 0.53333336, A 0.5411765, B 0.56078434, alpha 1.0, hue 0.15414284, saturation 0.88256764, and chroma 0.14626285.
     * It has the encoded Oklab value {@code 0xff8f8a88}.
     * <pre>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CINNAMON = 0xff8f8a88;
    static { NAMED.put("cinnamon", 0xff8f8a88); LIST.add(0xff8f8a88); }

    /**
     * This color constant "apricot" has RGBA8888 code {@code 0xFFA828FF}, L 0.73333335, A 0.52156866, B 0.5764706, alpha 1.0, hue 0.20361295, saturation 0.90785277, and chroma 0.1582875.
     * It has the encoded Oklab value {@code 0xff9385bb}.
     * <pre>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int APRICOT = 0xff9385bb;
    static { NAMED.put("apricot", 0xff9385bb); LIST.add(0xff9385bb); }

    /**
     * This color constant "peach" has RGBA8888 code {@code 0xFFBF81FF}, L 0.8039216, A 0.5176471, B 0.54509807, alpha 1.0, hue 0.18716717, saturation 0.50980395, and chroma 0.096477255.
     * It has the encoded Oklab value {@code 0xff8b84cd}.
     * <pre>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEACH = 0xff8b84cd;
    static { NAMED.put("peach", 0xff8b84cd); LIST.add(0xff8b84cd); }

    /**
     * This color constant "pear" has RGBA8888 code {@code 0xD3E330FF}, L 0.8352941, A 0.45882353, B 0.58431375, alpha 1.0, hue 0.31789964, saturation 0.911928, and chroma 0.18692946.
     * It has the encoded Oklab value {@code 0xff9575d5}.
     * <pre>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEAR = 0xff9575d5;
    static { NAMED.put("pear", 0xff9575d5); LIST.add(0xff9575d5); }

    /**
     * This color constant "saffron" has RGBA8888 code {@code 0xFFD510FF}, L 0.84313726, A 0.4862745, B 0.5882353, alpha 1.0, hue 0.2706426, saturation 0.9467276, and chroma 0.17789528.
     * It has the encoded Oklab value {@code 0xff967cd7}.
     * <pre>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAFFRON = 0xff967cd7;
    static { NAMED.put("saffron", 0xff967cd7); LIST.add(0xff967cd7); }

    /**
     * This color constant "butter" has RGBA8888 code {@code 0xFFF288FF}, L 0.93333334, A 0.4862745, B 0.5568628, alpha 1.0, hue 0.2814164, saturation 0.5772475, and chroma 0.11653464.
     * It has the encoded Oklab value {@code 0xff8e7cee}.
     * <pre>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BUTTER = 0xff8e7cee;
    static { NAMED.put("butter", 0xff8e7cee); LIST.add(0xff8e7cee); }

    /**
     * This color constant "chartreuse" has RGBA8888 code {@code 0xC8FF41FF}, L 0.90588236, A 0.4392157, B 0.58431375, alpha 1.0, hue 0.34524146, saturation 0.8589372, and chroma 0.2070681.
     * It has the encoded Oklab value {@code 0xff9570e7}.
     * <pre>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHARTREUSE = 0xff9570e7;
    static { NAMED.put("chartreuse", 0xff9570e7); LIST.add(0xff9570e7); }

    /**
     * This color constant "cactus" has RGBA8888 code {@code 0x30A000FF}, L 0.5137255, A 0.41960785, B 0.5647059, alpha 1.0, hue 0.38787603, saturation 0.95450217, and chroma 0.20558903.
     * It has the encoded Oklab value {@code 0xff906b83}.
     * <pre>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CACTUS = 0xff906b83;
    static { NAMED.put("cactus", 0xff906b83); LIST.add(0xff906b83); }

    /**
     * This color constant "lime" has RGBA8888 code {@code 0x93D300FF}, L 0.7294118, A 0.43529412, B 0.5803922, alpha 1.0, hue 0.35362232, saturation 0.94288415, and chroma 0.20558903.
     * It has the encoded Oklab value {@code 0xff946fba}.
     * <pre>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIME = 0xff946fba;
    static { NAMED.put("lime", 0xff946fba); LIST.add(0xff946fba); }

    /**
     * This color constant "olive" has RGBA8888 code {@code 0x818000FF}, L 0.47843137, A 0.4745098, B 0.56078434, alpha 1.0, hue 0.30710015, saturation 0.9493337, and chroma 0.13131043.
     * It has the encoded Oklab value {@code 0xff8f797a}.
     * <pre>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int OLIVE = 0xff8f797a;
    static { NAMED.put("olive", 0xff8f797a); LIST.add(0xff8f797a); }

    /**
     * This color constant "fern" has RGBA8888 code {@code 0x4E7942FF}, L 0.41960785, A 0.4627451, B 0.5294118, alpha 1.0, hue 0.38435107, saturation 0.51240826, and chroma 0.0945603.
     * It has the encoded Oklab value {@code 0xff87766b}.
     * <pre>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FERN = 0xff87766b;
    static { NAMED.put("fern", 0xff87766b); LIST.add(0xff87766b); }

    /**
     * This color constant "moss" has RGBA8888 code {@code 0x204608FF}, L 0.23137255, A 0.45882353, B 0.5372549, alpha 1.0, hue 0.375, saturation 0.94280905, and chroma 0.11062346.
     * It has the encoded Oklab value {@code 0xff89753b}.
     * <pre>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MOSS = 0xff89753b;
    static { NAMED.put("moss", 0xff89753b); LIST.add(0xff89753b); }

    /**
     * This color constant "celery" has RGBA8888 code {@code 0x7DFF73FF}, L 0.85490197, A 0.41960785, B 0.56078434, alpha 1.0, hue 0.39261138, saturation 0.64841765, and chroma 0.20078278.
     * It has the encoded Oklab value {@code 0xff8f6bda}.
     * <pre>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CELERY = 0xff8f6bda;
    static { NAMED.put("celery", 0xff8f6bda); LIST.add(0xff8f6bda); }

    /**
     * This color constant "sage" has RGBA8888 code {@code 0xABE3C5FF}, L 0.83137256, A 0.46666667, B 0.50980395, alpha 1.0, hue 0.44289985, saturation 0.31069103, and chroma 0.069218926.
     * It has the encoded Oklab value {@code 0xff8277d4}.
     * <pre>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAGE = 0xff8277d4;
    static { NAMED.put("sage", 0xff8277d4); LIST.add(0xff8277d4); }

    /**
     * This color constant "jade" has RGBA8888 code {@code 0x3FBF3FFF}, L 0.62352943, A 0.41568628, B 0.5568628, alpha 1.0, hue 0.40128404, saturation 0.806468, and chroma 0.20259848.
     * It has the encoded Oklab value {@code 0xff8e6a9f}.
     * <pre>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int JADE = 0xff8e6a9f;
    static { NAMED.put("jade", 0xff8e6a9f); LIST.add(0xff8e6a9f); }

    /**
     * This color constant "cyan" has RGBA8888 code {@code 0x00FFFFFF}, L 0.8627451, A 0.42352942, B 0.47843137, alpha 1.0, hue 0.5409546, saturation 0.9583845, and chroma 0.1582875.
     * It has the encoded Oklab value {@code 0xff7a6cdc}.
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CYAN = 0xff7a6cdc;
    static { NAMED.put("cyan", 0xff7a6cdc); LIST.add(0xff7a6cdc); }

    /**
     * This color constant "mint" has RGBA8888 code {@code 0x7FFFD4FF}, L 0.88235295, A 0.4392157, B 0.50980395, alpha 1.0, hue 0.4685836, saturation 0.6118823, and chroma 0.12265874.
     * It has the encoded Oklab value {@code 0xff8270e1}.
     * <pre>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MINT = 0xff8270e1;
    static { NAMED.put("mint", 0xff8270e1); LIST.add(0xff8270e1); }

    /**
     * This color constant "teal" has RGBA8888 code {@code 0x007F7FFF}, L 0.42745098, A 0.4509804, B 0.4862745, alpha 1.0, hue 0.5389897, saturation 0.95148593, and chroma 0.10141215.
     * It has the encoded Oklab value {@code 0xff7c736d}.
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TEAL = 0xff7c736d;
    static { NAMED.put("teal", 0xff7c736d); LIST.add(0xff7c736d); }

    /**
     * This color constant "turquoise" has RGBA8888 code {@code 0x2ED6C9FF}, L 0.7254902, A 0.43137255, B 0.49019608, alpha 1.0, hue 0.5186383, saturation 0.9009075, and chroma 0.1381068.
     * It has the encoded Oklab value {@code 0xff7d6eb9}.
     * <pre>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TURQUOISE = 0xff7d6eb9;
    static { NAMED.put("turquoise", 0xff7d6eb9); LIST.add(0xff7d6eb9); }

    /**
     * This color constant "sky" has RGBA8888 code {@code 0x10C0E0FF}, L 0.6627451, A 0.44313726, B 0.4627451, alpha 1.0, hue 0.59093124, saturation 0.9246287, and chroma 0.13542919.
     * It has the encoded Oklab value {@code 0xff7671a9}.
     * <pre>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SKY = 0xff7671a9;
    static { NAMED.put("sky", 0xff7671a9); LIST.add(0xff7671a9); }

    /**
     * This color constant "cobalt" has RGBA8888 code {@code 0x0046ABFF}, L 0.3019608, A 0.48235294, B 0.4117647, alpha 1.0, hue 0.72137564, saturation 0.9126808, and chroma 0.1792624.
     * It has the encoded Oklab value {@code 0xff697b4d}.
     * <pre>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int COBALT = 0xff697b4d;
    static { NAMED.put("cobalt", 0xff697b4d); LIST.add(0xff697b4d); }

    /**
     * This color constant "denim" has RGBA8888 code {@code 0x3088B8FF}, L 0.49411765, A 0.46666667, B 0.45490196, alpha 1.0, hue 0.64992374, saturation 0.7772269, and chroma 0.11172148.
     * It has the encoded Oklab value {@code 0xff74777e}.
     * <pre>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int DENIM = 0xff74777e;
    static { NAMED.put("denim", 0xff74777e); LIST.add(0xff74777e); }

    /**
     * This color constant "navy" has RGBA8888 code {@code 0x000080FF}, L 0.15294118, A 0.4862745, B 0.4, alpha 1.0, hue 0.73099244, saturation 0.96843684, and chroma 0.20108652.
     * It has the encoded Oklab value {@code 0xff667c27}.
     * <pre>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int NAVY = 0xff667c27;
    static { NAMED.put("navy", 0xff667c27); LIST.add(0xff667c27); }

    /**
     * This color constant "lavender" has RGBA8888 code {@code 0xB991FFFF}, L 0.6627451, A 0.53333336, B 0.43529412, alpha 1.0, hue 0.83154917, saturation 0.34313196, and chroma 0.14500555.
     * It has the encoded Oklab value {@code 0xff6f88a9}.
     * <pre>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LAVENDER = 0xff6f88a9;
    static { NAMED.put("lavender", 0xff6f88a9); LIST.add(0xff6f88a9); }

    /**
     * This color constant "plum" has RGBA8888 code {@code 0xBE0DC6FF}, L 0.44313726, A 0.60784316, B 0.42352942, alpha 1.0, hue 0.90511185, saturation 0.9399402, and chroma 0.26337513.
     * It has the encoded Oklab value {@code 0xff6c9b71}.
     * <pre>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PLUM = 0xff6c9b71;
    static { NAMED.put("plum", 0xff6c9b71); LIST.add(0xff6c9b71); }

    /**
     * This color constant "mauve" has RGBA8888 code {@code 0xAB73ABFF}, L 0.5372549, A 0.5411765, B 0.47058824, alpha 1.0, hue 0.9098022, saturation 0.32193592, and chroma 0.1008085.
     * It has the encoded Oklab value {@code 0xff788a89}.
     * <pre>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAUVE = 0xff788a89;
    static { NAMED.put("mauve", 0xff788a89); LIST.add(0xff788a89); }

    /**
     * This color constant "rose" has RGBA8888 code {@code 0xE61E78FF}, L 0.48235294, A 0.6156863, B 0.49411765, alpha 1.0, hue 0.99469686, saturation 0.90959585, and chroma 0.23076649.
     * It has the encoded Oklab value {@code 0xff7e9d7b}.
     * <pre>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ROSE = 0xff7e9d7b;
    static { NAMED.put("rose", 0xff7e9d7b); LIST.add(0xff7e9d7b); }

    /**
     * This color constant "raspberry" has RGBA8888 code {@code 0x911437FF}, L 0.29803923, A 0.5803922, B 0.5137255, alpha 1.0, hue 0.029956235, saturation 0.9096833, and chroma 0.16247371.
     * It has the encoded Oklab value {@code 0xff83944c}.
     * <pre>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RASPBERRY = 0xff83944c;
    static { NAMED.put("raspberry", 0xff83944c); LIST.add(0xff83944c); }

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
     * Used when converting from Oklab to RGB, as an intermediate step.
     * Really just {@code x * x * x}.
     * @param x one of the LMS Prime channels to be converted to LMS
     * @return an LMS channel value, which can be converted to RGB
     */
    private static float cube(final float x) {
        return x * x * x;
    }

    /**
     * An approximation of the cube-root function for float inputs and outputs.
     * This can be about twice as fast as {@link Math#cbrt(double)}. This
     * version does not tolerate negative inputs, because in the narrow use
     * case it has in this class, it is never given negative inputs.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between 0 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">Wikipedia</a> for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds,
     * <a href="https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/master/src/Posts/ballcube.c#L182-L197">posted in his "Stand-alone-junk" repo</a>.
     * It's worth noting that while hardware instructions for finding the
     * square root of a float have gotten extremely fast, the same is not
     * true for the cube root (which has to allow negative inputs), so while
     * the bit-twiddling inverse square root is no longer a beneficial
     * optimization on current hardware, this does seem to help cube roots.
     * <br>
     * This is used when converting from RGB to Oklab, as an intermediate step.
     * @param x any non-negative finite float to find the cube root of
     * @return the cube root of x, approximated
     */
    private static float cbrtPositive(float x) {
        int ix = BitConversion.floatToRawIntBits(x);
        final float x0 = x;
        ix = (ix>>>2) + (ix>>>4);
        ix += (ix>>>4);
        ix += (ix>>>8) + 0x2A5137A0;
        x  = BitConversion.intBitsToFloat(ix);
        x  = 0.33333334f*(2f * x + x0/(x*x));
        x  = 0.33333334f*(2f * x + x0/(x*x));
        return x;
    }

    /**
     * Used when given non-linear sRGB inputs to make them linear, approximating with gamma 2.0.
     * Really just {@code component * component}.
     * @param component any non-linear channel of a color, to be made linear
     * @return a linear version of component
     */
    private static float forwardGamma(final float component) {
        return component * component;
    }

    /**
     * Used to return from a linear, gamma-corrected input to an sRGB, non-linear output, using gamma 2.0.
     * Really just gets the square root of component, as a float.
     * @param component a linear channel of a color, to be made non-linear
     * @return a non-linear version of component
     */
    private static float reverseGamma(final float component) {
        return (float)Math.sqrt(component);
    }

    /**
     * Changes the curve of a requested L value so that it matches the internally-used curve. This takes a curve with a
     * very-dark area similar to sRGB (a very small one), and makes it significantly larger. This is typically used on
     * "to Oklab" conversions.
     * <br>
     * Internally, this is similar to {@code (float)Math.pow(L, 1.5f)}. At one point it used a modified "Barron spline"
     * to get its curvature mostly right, but changed so it now seems nearly indistinguishable from an ideal curve.
     * @param L lightness, from 0 to 1 inclusive
     * @return an adjusted L value that can be used internally
     */
    public static float forwardLight(final float L) {
        return (float) Math.sqrt(L * L * L);
    }

    /**
     * Changes the curve of the internally-used lightness when it is output to another format. This makes the very-dark
     * area smaller, matching (closely) the curve that the standard sRGB lightness uses. This is typically used on "from
     * Oklab" conversions.
     * <br>
     * Internally, this is similar to {@code (float)Math.pow(L, 2f/3f)}. At one point it used a modified "Barron spline"
     * to get its curvature mostly right, but changed so it now seems nearly indistinguishable from an ideal curve.
     * <br>
     * This specific code uses a modified cube root approximation (based on {@link #cbrtPositive(float)}) originally by
     * Marc B. Reynolds.
     * @param L lightness, from 0 to 1 inclusive
     * @return an adjusted L value that can be fed into a conversion to RGBA or something similar
     */
    public static float reverseLight(float L) {
        int ix = BitConversion.floatToIntBits(L);
        final float x0 = L;
        ix = (ix>>>2) + (ix>>>4);
        ix += (ix>>>4);
        ix += (ix>>>8) + 0x2A5137A0;
        L  = BitConversion.intBitsToFloat(ix);
        L  = 0.33333334f * (2f * L + x0/(L*L));
        L  = 0.33333334f * (1.9999999f * L + x0/(L*L));
        return L * L;
    }

    /**
     * Gets an Oklab packed int color that is within the gamut of viable colors by using
     * {@link #limitToGamut(int, int, int, int)}. This method just calls limitToGamut() with the parameters adjusted
     * from the 0 to 1 float range here, to the 0 to 255 int range in limitToGamut.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int, int, int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int oklab(float L, float A, float B, float alpha) {
        return limitToGamut((int)(L * 255.999f), (int)(A * 255.999f), (int)(B * 255.999f), (int)(alpha * 255.999f));
    }
    /**
     * Converts a packed Oklab int color in the format used by constants in this class to an RGBA8888 int.
     * This format of int can be used with Pixmap and in some other places in libGDX.
     * @param oklab a packed int color, as from a constant in this class
     * @return an RGBA8888 int color
     */
    public static int toRGBA8888(final int oklab)
    {
        final float L = reverseLight((oklab & 0xff) / 255f);
        final float A = ((oklab >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((oklab >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        final int r = (int)(reverseGamma(Math.min(Math.max(+4.0767245293f * l - 3.3072168827f * m + 0.2307590544f * s, 0f), 1f)) * 255.999f);
        final int g = (int)(reverseGamma(Math.min(Math.max(-1.2681437731f * l + 2.6093323231f * m - 0.3411344290f * s, 0f), 1f)) * 255.999f);
        final int b = (int)(reverseGamma(Math.min(Math.max(-0.0041119885f * l - 0.7034763098f * m + 1.7068625689f * s, 0f), 1f)) * 255.999f);
        return r << 24 | g << 16 | b << 8 | (oklab & 0xfe000000) >>> 24 | oklab >>> 31;
    }

    /**
     * Converts an Oklab int color in the format produced by {@link #oklab(float, float, float, float)}
     * to a packed float in RGBA format.
     * This format of float can be used with the standard SpriteBatch and in some other places in libGDX.
     * @param packed a packed int color, as produced by {@link #oklab(float, float, float, float)}
     * @return a packed float color as ABGR, the format setPackedColor uses
     */
    public static float oklabIntToFloat(final int packed)
    {
        final float L = reverseLight((packed & 0xff) / 255f);
        final float A = ((packed >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((packed >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        final int r = (int)(reverseGamma(Math.min(Math.max(+4.0767245293f * l - 3.3072168827f * m + 0.2307590544f * s, 0f), 1f)) * 255.999f);
        final int g = (int)(reverseGamma(Math.min(Math.max(-1.2681437731f * l + 2.6093323231f * m - 0.3411344290f * s, 0f), 1f)) * 255.999f);
        final int b = (int)(reverseGamma(Math.min(Math.max(-0.0041119885f * l - 0.7034763098f * m + 1.7068625689f * s, 0f), 1f)) * 255.999f);
        return BitConversion.intBitsToFloat(r | g << 8 | b << 16 | (packed & 0xFE000000));
    }

    /**
     * Takes a color encoded as an RGBA8888 int and converts to a packed float in the Oklab format this uses.
     * @param rgba an int with the channels (in order) red, green, blue, alpha; should have 8 bits per channel
     * @return a packed int as Oklab, which this class can use
     */
    public static int fromRGBA8888(final int rgba) {
        final float r = forwardGamma((rgba >>> 24) * 0x1.010101010101p-8f);
        final float g = forwardGamma((rgba >>> 16 & 0xFF) * 0x1.010101010101p-8f);
        final float b = forwardGamma((rgba >>> 8 & 0xFF) * 0x1.010101010101p-8f);

        final float l = cbrtPositive(0.4121656120f * r + 0.5362752080f * g + 0.0514575653f * b);
        final float m = cbrtPositive(0.2118591070f * r + 0.6807189584f * g + 0.1074065790f * b);
        final float s = cbrtPositive(0.0883097947f * r + 0.2818474174f * g + 0.6302613616f * b);

        return (
                       Math.min(Math.max((int)(forwardLight(0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f), 0), 255)
                        | Math.min(Math.max((int)((1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s) * 127.999f + 127.5f), 0), 255) << 8
                        | Math.min(Math.max((int)((0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s) * 127.999f + 127.5f), 0), 255) << 16
                        | (rgba) << 24);
    }

    /**
     * Takes a color encoded as an RGBA8888 packed float and converts to a packed int in the Oklab format this uses.
     * @param packed a packed float in RGBA8888 format, with A in the MSB and R in the LSB
     * @return a packed int as Oklab, which this class can use
     */
    public static int fromRGBA(final float packed) {
        final int abgr = BitConversion.floatToRawIntBits(packed);
        final float r = forwardGamma((abgr & 0xFF) * 0x1.010101010101p-8f);
        final float g = forwardGamma((abgr >>> 8 & 0xFF) * 0x1.010101010101p-8f);
        final float b = forwardGamma((abgr >>> 16 & 0xFF) * 0x1.010101010101p-8f);
        final float l = cbrtPositive(0.4121656120f * r + 0.5362752080f * g + 0.0514575653f * b);
        final float m = cbrtPositive(0.2118591070f * r + 0.6807189584f * g + 0.1074065790f * b);
        final float s = cbrtPositive(0.0883097947f * r + 0.2818474174f * g + 0.6302613616f * b);
        return (
                Math.min(Math.max((int)(forwardLight(0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
                        | Math.min(Math.max((int)((1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s) * 127.999f + 127.5f), 0), 255) << 8
                        | Math.min(Math.max((int)((0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s) * 127.999f + 127.5f), 0), 255) << 16
                        | (abgr & 0xFE000000));
    }

    /**
     * Takes RGBA components from 0.0 to 1.0 each and converts to a packed int in the Oklab format this uses.
     * @param r red, from 0.0 to 1.0 (both inclusive)
     * @param g green, from 0.0 to 1.0 (both inclusive)
     * @param b blue, from 0.0 to 1.0 (both inclusive)
     * @param a alpha, from 0.0 to 1.0 (both inclusive)
     * @return a packed int as Oklab, which this class can use
     */
    public static int fromRGBA(float r, float g, float b, final float a) {
        r = forwardGamma(r);
        g = forwardGamma(g);
        b = forwardGamma(b);
        final float l = cbrtPositive(0.4121656120f * r + 0.5362752080f * g + 0.0514575653f * b);
        final float m = cbrtPositive(0.2118591070f * r + 0.6807189584f * g + 0.1074065790f * b);
        final float s = cbrtPositive(0.0883097947f * r + 0.2818474174f * g + 0.6302613616f * b);
        return (
                Math.min(Math.max((int)(forwardLight(0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
                        | Math.min(Math.max((int)((1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s) * 127.999f + 127.5f), 0), 255) << 8
                        | Math.min(Math.max((int)((0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s) * 127.999f + 127.5f), 0), 255) << 16
                        | ((int)(a * 255f) << 24));
    }

    /**
     * Gets the red channel value of the given encoded color, as an int ranging from 0 to 255, inclusive.
     * @param encoded a color as a packed int that can be obtained by {@link #oklab(float, float, float, float)}
     * @return an int from 0 to 255, inclusive, representing the red channel value of the given encoded color
     */
    public static int redInt(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return (int)(reverseGamma(Math.min(Math.max(+4.0767245293f * l - 3.3072168827f * m + 0.2307590544f * s, 0f), 1f)) * 255.999f);
    }

    /**
     * Gets the green channel value of the given encoded color, as an int ranging from 0 to 255, inclusive.
     * @param encoded a color as a packed int that can be obtained by {@link #oklab(float, float, float, float)}
     * @return an int from 0 to 255, inclusive, representing the green channel value of the given encoded color
     */
    public static int greenInt(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return (int)(reverseGamma(Math.min(Math.max(-1.2681437731f * l + 2.6093323231f * m - 0.3411344290f * s, 0f), 1f)) * 255.999f);
    }

    /**
     * Gets the blue channel value of the given encoded color, as an int ranging from 0 to 255, inclusive.
     * @param encoded a color as a packed int that can be obtained by {@link #oklab(float, float, float, float)}
     * @return an int from 0 to 255, inclusive, representing the blue channel value of the given encoded color
     */
    public static int blueInt(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return (int)(reverseGamma(Math.min(Math.max(-0.0041119885f * l - 0.7034763098f * m + 1.7068625689f * s, 0f), 1f)) * 255.999f);
    }

    /**
     * Gets the alpha channel value of the given encoded color, as an int ranging from 0 to 255, inclusive. Because
     * of how alpha is stored in libGDX, no odd-number values are possible for alpha if it has been stored as a float,
     * but colors used with classes like Pixmap permit full alpha.
     * @param encoded a color as a packed int that can be obtained by {@link #oklab(float, float, float, float)}
     * @return an even int from 0 to 255, inclusive, representing the alpha channel value of the given encoded color
     */
    public static int alphaInt(final int encoded)
    {
        return encoded >>> 24;
    }

    /**
     * Gets the red channel value of the given encoded color, as a float from 0.0f to 1.0f, inclusive.
     * @param encoded a color as a packed float that can be obtained by {@link #oklab(float, float, float, float)}
     * @return a float from 0.0f to 1.0f, inclusive, representing the red channel value of the given encoded color
     */
    public static float red(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return reverseGamma(Math.min(Math.max(+4.0767245293f * l - 3.3072168827f * m + 0.2307590544f * s, 0f), 1f));
    }

    /**
     * Gets the green channel value of the given encoded color, as a float from 0.0f to 1.0f, inclusive.
     * @param encoded a color as a packed float that can be obtained by {@link #oklab(float, float, float, float)}
     * @return a float from 0.0f to 1.0f, inclusive, representing the green channel value of the given encoded color
     */
    public static float green(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return reverseGamma(Math.min(Math.max(-1.2681437731f * l + 2.6093323231f * m - 0.3411344290f * s, 0f), 1f));
    }

    /**
     * Gets the blue channel value of the given encoded color, as a float from 0.0f to 1.0f, inclusive.
     * @param encoded a color as a packed float that can be obtained by {@link #oklab(float, float, float, float)}
     * @return a float from 0.0f to 1.0f, inclusive, representing the blue channel value of the given encoded color
     */
    public static float blue(final int encoded)
    {
        final float L = reverseLight((encoded & 0xff) / 255f);
        final float A = ((encoded >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = ((encoded >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float l = cube(L + 0.3963377774f * A + 0.2158037573f * B);
        final float m = cube(L - 0.1055613458f * A - 0.0638541728f * B);
        final float s = cube(L - 0.0894841775f * A - 1.2914855480f * B);
        return reverseGamma(Math.min(Math.max(-0.0041119885f * l - 0.7034763098f * m + 1.7068625689f * s, 0f), 1f));
    }

    /**
     * Gets the alpha channel value of the given encoded color, as a float from 0.0f to 1.0f, inclusive.
     * @param encoded a color as a packed float that can be obtained by {@link #oklab(float, float, float, float)}
     * @return a float from 0.0f to 1.0f, inclusive, representing the alpha channel value of the given encoded color
     */
    public static float alpha(final int encoded)
    {
        return (encoded >>> 24) / 255f;
    }

    /**
     * The "L" channel of the given packed int in Oklab format, which is its lightness; ranges from 0.0f to
     * 1.0f . You can edit the L of a color with {@link #lighten(int, float)} and {@link #darken(int, float)}.
     *
     * @param encoded a color encoded as a packed int, as by {@link #oklab(float, float, float, float)}
     * @return the L value as a float from 0.0f to 1.0f
     */
    public static float channelL(final int encoded)
    {
        return (encoded & 0xff) / 255f;
    }

    /**
     * The "L" channel of the given packed int in Oklab format, which is its lightness; ranges from 0.0f to
     * 1.0f . You can edit the L of a color with {@link #lighten(int, float)} and {@link #darken(int, float)}.
     * <br>
     * This is exactly the same as {@link #channelL(int)}.
     * @param encoded a color encoded as a packed int, as by {@link #oklab(float, float, float, float)}
     * @return the L value as a float from 0.0f to 1.0f
     */
    public static float lightness(final int encoded)
    {
        return (encoded & 0xff) / 255f;
    }

    /**
     * The "A" channel of the given packed int in Oklab format, which when combined with the B channel describes the
     * hue and saturation of a color; ranges from 0f to 1f . If A is 0f, the color will be cooler, more green or
     * blue; if A is 1f, the color will be warmer, from magenta to orange. You can edit the A of a color with
     * {@link #raiseA(int, float)} and {@link #lowerA(int, float)}.
     * @param encoded a color encoded as a packed int, as by {@link #oklab(float, float, float, float)}
     * @return the A value as a float from 0.0f to 1.0f
     */
    public static float channelA(final int encoded)
    {
        return (encoded >>> 8 & 0xff) / 255f;
    }

    /**
     * The "B" channel of the given packed int in Oklab format, which when combined with the A channel describes the
     * hue and saturation of a color; ranges from 0f to 1f . If B is 0f, the color will be more "artificial", more
     * blue or purple; if B is 1f, the color will be more "natural", from green to yellow to orange. You can edit
     * the B of a color with {@link #raiseB(int, float)} and {@link #lowerB(int, float)}.
     * @param encoded a color encoded as a packed int, as by {@link #oklab(float, float, float, float)}
     * @return the B value as a float from 0.0f to 1.0f
     */
    public static float channelB(final int encoded)
    {
        return (encoded >>> 16 & 0xff) / 255f;
    }

    /**
     * Gets the "chroma" or "colorfulness" of a given Oklab color. Chroma is similar to saturation in that grayscale
     * values have 0 saturation and 0 chroma, while brighter colors have high saturation and chroma. The difference is
     * that colors that are perceptually more-colorful have higher chroma than colors that are perceptually
     * less-colorful, regardless of hue, whereas saturation changes its meaning depending on the hue and lightness. That
     * is, the most saturated color for a given hue and lightness always has a saturation of 1, but if that color
     * isn't perceptually very colorful (as is the case for very dark and very light colors), it will have a chroma that
     * is much lower than the maximum. The result of this method can't be negative, grayscale values have very close to
     * 0 chroma, and the most colorful values (all very close to magenta) should have 0.31613f chroma.
     * @param oklab a color as an Oklab int that can be obtained from any of the constants in this class.
     * @return a float between 0.0f and 0.31613f that represents how colorful the given value is
     */
    public static float chroma(final int oklab) {
        final float a = ((oklab >>> 7 & 0x1FE) - 255) * 0x1p-8f;
        final float b = ((oklab >>> 15 & 0x1FE) - 255) * 0x1p-8f;
        return (float) Math.sqrt(a * a + b * b);
    }

    /**
     * Given a hue and lightness, this gets the (approximate) maximum chroma possible for that hue-lightness
     * combination. This is useful to know the bounds of {@link #chroma(int)}. This should be no greater than 0.334f .
     * This usually takes its hue from {@link #hue(int)} and its lightness from {@link #lightness(int)}.
     * @param hue the hue, typically between 0.0f and 1.0f, to look up
     * @param lightness the lightness, clamped between 0.0f and 1.0f, to look up
     * @return the maximum possible chroma for the given hue and lightness, between 0.0f and 0.334f
     */
    public static float chromaLimit(final float hue, final float lightness){
        final int idx = (int) (Math.min(Math.max(lightness, 0f), 1f) * 255.999f) << 8
                | (int) (256f * (hue - fastFloor(hue)));
        return (GAMUT_DATA[idx] + 1.5f) * 0x1p-8f;
    }

    /**
     * Gets the saturation of the given Oklab float color, but as Oklab understands saturation rather than how HSL does.
     * Saturation here is a fraction of the chroma limit (see {@link #chromaLimit(float, float)}) for a given hue and
     * lightness, and is between 0 and 1 almost all the time. Saturation should always be between 0 (inclusive) and 1
     * (inclusive).
     *
     * @param packed a packed Oklab float color
     * @return a float between 0 (inclusive) and 1 (inclusive) that represents saturation in the Oklab color space
     */
    public static float saturation(final int packed) {
        final float A = ((packed >>> 8 & 0xff) - 127.5f);
        final float B = ((packed >>> 16 & 0xff) - 127.5f);
        final float hue = TrigTools.atan2Turns(B, A);
        final int idx = (packed & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] + 1.5f;
        return dist == 3.5f ? 0f : (A * A + B * B) * 4f / (dist * dist);
    }

    /**
     * Gets the hue of the given encoded color, as a float from 0f (inclusive, red and approaching orange if increased)
     * to 1f (exclusive, red and approaching purple if decreased). This is not exactly the same as HSL's or HSV's hue.
     *
     * @param oklab a color as an Oklab int that can be obtained from any of the constants in this class.
     * @return The hue of the color from 0.0 (red, inclusive) towards orange, then yellow, and
     * eventually to purple before looping back to almost the same red (1.0, exclusive)
     */
    public static float hue(final int oklab) {
        final float a = ((oklab >>> 7 & 0x1FE) - 255);
        final float b = ((oklab >>> 15 & 0x1FE) - 255);
        return TrigTools.atan2Turns(a, b);
    }

    /**
     * Interpolates from the packed Oklab int color oklab towards white by change. While change should be between 0f
     * (return oklab as-is) and 1f (return white), oklab should be a packed color, as from a constant here. This method
     * does not necessarily keep the resulting color in-gamut; after performing some changes with this or other
     * component-editing methods, you may want to call {@link #limitToGamut(int)} to make sure the color can be rendered
     * correctly.
     *
     * @param oklab      the starting color as an Oklab int
     * @param change how much to go from oklab toward white, as a float between 0 and 1; higher means closer to white
     * @return a packed Oklab int that represents a color between start and white
     * @see #darken(int, float) the counterpart method that darkens a float color
     */
    public static int lighten(final int oklab, final float change) {
        final int L = oklab & 255, other = oklab & 0xFFFFFF00;
        return (((int) (L + (255 - L) * change) & 255) | other);
    }

    /**
     * Interpolates from the packed Oklab int color oklab towards black by change. While change should be between 0f
     * (return oklab as-is) and 1f (return black), oklab should be a packed color, as from a constant here. This method
     * does not necessarily keep the resulting color in-gamut; after performing some changes with this or other
     * component-editing methods, you may want to call {@link #limitToGamut(int)} to make sure the color can be rendered
     * correctly.
     *
     * @param oklab      the starting color as a packed float
     * @param change how much to go from oklab toward black, as a float between 0 and 1; higher means closer to black
     * @return a packed float that represents a color between start and black
     * @see #lighten(int, float) the counterpart method that lightens a float color
     */
    public static int darken(final int oklab, final float change) {
        final int i = oklab & 255, other = oklab & 0xFFFFFF00;
        return (((int) (i * (1f - change)) & 255) | other);
    }

    /**
     * Interpolates from the packed Oklab int color oklab towards either white or black, depending on change. The value
     * for change should be between -1f and 1f; negative values interpolate toward black, while positive ones
     * interpolate toward white. The value for oklab should be a packed color, as from a constant here. This method
     * does not necessarily keep the resulting color in-gamut; after performing some changes with this or other
     * component-editing methods, you may want to call {@link #limitToGamut(int)} to make sure the color can be rendered
     * correctly.
     *
     * @param oklab      the starting color as a packed float
     * @param change how much to go away from oklab, as a float between -1 and 1; negative to black, positive to white
     * @return a packed float that represents a color between start and black or white
     * @see #darken(int, float) the counterpart method that darkens a float color
     * @see #lighten(int, float) the counterpart method that lightens a float color
     */
    public static int adjustLightness(final int oklab, final float change) {
        if(change < 0.0f) return darken(oklab, -change);
        return lighten(oklab, change);
    }

    /**
     * Interpolates from the packed float color start towards a warmer color (orange to magenta) by change. While change
     * should be between 0f (return start as-is) and 1f (return fully warmed), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors,
     * and is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to
     * lerp towards a warmer color. Unlike {@link #lerpColors(int, int, float)}, this keeps the
     * alpha and L of start as-is.
     * @see #lowerA(int, float) the counterpart method that cools a float color
     * @param start the starting color as a packed float
     * @param change how much to warm start, as a float between 0 and 1; higher means a warmer result
     * @return a packed float that represents a color between start and a warmer color
     */
    public static int raiseA(final int start, final float change) {
        final int p = start >>> 8 & 0xFF, other = start & 0xFFFF00FF;
        return (((int) (p + (0xFF - p) * change) << 8 & 0xFF00) | other);
    }

    /**
     * Interpolates from the packed float color start towards a cooler color (green to blue) by change. While change
     * should be between 0f (return start as-is) and 1f (return fully cooled), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors, and
     * is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp
     * towards a cooler color. Unlike {@link #lerpColors(int, int, float)}, this keeps the alpha and
     * L of start as-is.
     * @see #raiseA(int, float) the counterpart method that warms a float color
     * @param start the starting color as a packed float
     * @param change how much to cool start, as a float between 0 and 1; higher means a cooler result
     * @return a packed float that represents a color between start and a cooler color
     */
    public static int lowerA(final int start, final float change) {
        final int p = start >>> 8 & 0xFF, other = start & 0xFFFF00FF;
        return (((int) (p * (1f - change)) & 0xFF) << 8 | other);
    }

    /**
     * Interpolates from the packed float color start towards a "natural" color (between green and orange) by change.
     * While change should be between 0f (return start as-is) and 1f (return fully natural), start should be a packed color, as
     * from {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary
     * Colors, and is a little more efficient and clear than using
     * {@link #lerpColors(int, int, float)} to lerp towards a more natural color. Unlike
     * {@link #lerpColors(int, int, float)}, this keeps the alpha and L of start as-is.
     * @see #lowerB(int, float) the counterpart method that makes a float color less natural
     * @param start the starting color as a packed float
     * @param change how much to change start to a natural color, as a float between 0 and 1; higher means a more natural result
     * @return a packed float that represents a color between start and a more natural color
     */
    public static int raiseB(final int start, final float change) {
        final int t = start >>> 16 & 0xFF, other = start & 0xFF00FFFF;
        return (((int) (t + (0xFF - t) * change) << 16 & 0xFF0000) | other);
    }

    /**
     * Interpolates from the packed float color start towards an "artificial" color (between blue and purple) by change.
     * While change should be between 0f (return start as-is) and 1f (return fully artificial), start should be a packed color, as
     * from {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary
     * Colors, and is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp
     * towards a more artificial color. Unlike {@link #lerpColors(int, int, float)}, this keeps the
     * alpha and L of start as-is.
     * @see #raiseB(int, float) the counterpart method that makes a float color less artificial
     * @param start the starting color as a packed float
     * @param change how much to change start to a bolder color, as a float between 0 and 1; higher means a more artificial result
     * @return a packed int that represents a color between start and a more artificial color
     */
    public static int lowerB(final int start, final float change) {
        final int t = start >>> 16 & 0xFF, other = start & 0xFF00FFFF;
        return (((int) (t * (1f - change)) & 0xFF) << 16 | other);
    }

    /**
     * Interpolates from the packed float color start towards that color made opaque by change. While change should be
     * between 0f (return start as-is) and 1f (return start with full alpha), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors.
     * This won't change the L, A, or B of the color.
     * @see #fade(int, float) the counterpart method that makes a float color more translucent
     * @param start the starting color as a packed float
     * @param change how much to go from start toward opaque, as a float between 0 and 1; higher means closer to opaque
     * @return a packed float that represents a color between start and its opaque version
     */
    public static int blot(final int start, final float change) {
        final int opacity = start >>> 24, other = start & 0x00FFFFFF;
        return (((int) (opacity + (0xFF - opacity) * change) & 0xFF) << 24 | other);
    }

    /**
     * Interpolates from the packed float color start towards transparent by change. While change should be between 0
     * (return start as-is) and 1f (return the color with 0 alpha), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors.
     * This won't change the L, A, or B of the color.
     * @see #blot(int, float) the counterpart method that makes a float color more opaque
     * @param start the starting color as a packed float
     * @param change how much to go from start toward transparent, as a float between 0 and 1; higher means closer to transparent
     * @return a packed float that represents a color between start and transparent
     */
    public static int fade(final int start, final float change) {
        final int opacity = start >>> 24, other = start & 0x00FFFFFF;
        return (((int) (opacity * (1f - change)) & 0xFF) << 24 | other);
    }

    /**
     * Brings the chromatic components of {@code oklab} closer to grayscale by {@code change} (desaturating them). While
     * change should be between 0f (return oklab as-is) and 1f (return fully gray), oklab should be a packed Oklab int
     * color, as from a constant in this class.
     *
     * @param oklab      the starting color as a packed Oklab int
     * @param change how much to change oklab to a desaturated color, as a float between 0 and 1; higher means a less saturated result
     * @return a packed float that represents a color between start and a desaturated color
     * @see #enrich(int, float) the counterpart method that makes an Oklab int color more saturated
     */
    public static int dullen(final int oklab, final float change) {
        return
                (int) (((oklab >>> 8 & 255) - 127.5f) * (1f - change) + 127.5f) << 8 |
                        (int) (((oklab >>> 16 & 255) - 127.5f) * (1f - change) + 127.5f) << 16 |
                        (oklab & 0xFF0000FF);
    }

    /**
     * Pushes the chromatic components of {@code oklab} away from grayscale by change (saturating them). While change
     * should be between 0f (return oklab as-is) and 1f (return maximally saturated), oklab should be a packed color, as
     * from a constant in this class. This usually changes only A and B, but higher values for {@code change} can
     * force the color out of the gamut, which this corrects using {@link #limitToGamut(int)} (and that can change L
     * somewhat). If the color stays in-gamut, then L won't change; alpha never changes.
     *
     * @param oklab      the starting color as a packed float
     * @param change how much to change oklab to a saturated color, as a float between 0 and 1; higher means a more saturated result
     * @return a packed float that represents a color between start and a saturated color
     * @see #dullen(int, float) the counterpart method that makes a float color less saturated
     */
    public static int enrich(final int oklab, final float change) {
        return limitToGamut(
                (int) (((oklab >>> 8 & 255) - 127.5f) * (1f - change) + 127.5f) << 8 |
                        (int) (((oklab >>> 16 & 255) - 127.5f) * (1f - change) + 127.5f) << 16 |
                        (oklab & 0xFF0000FF));
    }

    /**
     * Given a packed int Oklab color, this edits its L, A, B, and alpha channels by adding the corresponding "add"
     * parameter and then clamping. This returns a different int value (of course, the given int can't be edited
     * in-place). You can give a value of 0 for any "add" parameter you want to stay unchanged. This clamps the
     * resulting color to remain in-gamut, so it should be safe to convert it back to RGBA.
     * @param oklab a packed int Oklab color
     * @param addL how much to add to the L channel; typically in the -1 to 1 range
     * @param addA how much to add to the A channel; typically in the -1 to 1 range
     * @param addB how much to add to the B channel; typically in the -1 to 1 range
     * @param addAlpha how much to add to the alpha channel; typically in the -1 to 1 range
     * @return a packed int Oklab color with the requested edits applied to {@code oklab}
     */
    public static int edit(int oklab, float addL, float addA, float addB, float addAlpha) {
        return edit(oklab, addL, addA, addB, addAlpha, 1f, 1f, 1f, 1f);
    }
    /**
     * Given a packed int Oklab color, this edits its L, A, B, and alpha channels by first multiplying each channel by
     * the corresponding "mul" parameter and then adding the corresponding "add" parameter, before clamping. This means
     * the lightness value {@code L} is multiplied by {@code mulL}, then has {@code addL} added, and then is clamped to
     * the normal range for L (0 to 1). This returns a different int value (of course, the given int can't be edited
     * in-place). You can give a value of 0 for any "add" parameter you want to stay unchanged, or a value of 1 for any
     * "mul" parameter that shouldn't change. Note that this manipulates A and B in the -0.5 to 0.5 range, so if you
     * multiply by a small number like {@code 0.25f}, then this will produce a less-saturated color, and if you multiply
     * by a larger number like {@code 4f}, then you will get a much more-saturated color. This clamps the resulting
     * color to remain in-gamut, so it should be safe to convert it back to RGBA.
     * @param oklab a packed int Oklab color
     * @param addL how much to add to the L channel; typically in the -1 to 1 range
     * @param addA how much to add to the A channel; typically in the -1 to 1 range
     * @param addB how much to add to the B channel; typically in the -1 to 1 range
     * @param addAlpha how much to add to the alpha channel; typically in the -1 to 1 range
     * @param mulL how much to multiply the L channel by; should be non-negative
     * @param mulA how much to multiply the A channel by; usually non-negative (not always)
     * @param mulB how much to multiply the B channel by; usually non-negative (not always)
     * @param mulAlpha how much to multiply the alpha channel by; should be non-negative
     * @return a packed int Oklab color with the requested edits applied to {@code oklab}
     */
    public static int edit(int oklab, float addL, float addA, float addB, float addAlpha,
                           float mulL, float mulA, float mulB, float mulAlpha) {
        float L = (oklab & 0xff) / 255f;
        float A = ((oklab >>> 8 & 0xff) - 127.5f) / 127.5f;
        float B = ((oklab >>> 16 & 0xff) - 127.5f) / 127.5f;
        float alpha = (oklab >>> 24) / 255f;

        L = Math.min(Math.max(L * mulL + addL, 0f), 1f);
        A = Math.min(Math.max(A * mulA + addA * 2f, -1f), 1f) * 0.5f;
        B = Math.min(Math.max(B * mulB + addB * 2f, -1f), 1f) * 0.5f;
        alpha = Math.min(Math.max(alpha * mulAlpha + addAlpha, 0f), 1f);
        final float hue = TrigTools.atan2Turns(B, A);
        final int idx = (int) (L * 255.999f) << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        if(dist * dist * 0x1p-16f + 0x1p-14f >= (A * A + B * B))
            return (int) (alpha * 255.999f) << 24 |
                    (int) ((B + 0.5f) * 255.999f) << 16 |
                    (int) ((A + 0.5f) * 255.999f) << 8 |
                    (int) (L * 255.999f);
        return (int) (alpha * 255.999f) << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        (int) (L * 255.999f);
    }

    /**
     * ADVANCED USE: Given a 1D int index between 0 and 65535 (both inclusive), this treats the 1D index as two parts
     * (lightness and hue angle, both from 0 to 255) and gets the distance from grayscale to the edge of the gamut at
     * that lightness and hue. The index can be constructed from a lightness value {@code L} from 0 to 255, and a hue
     * value {@code H} from 0 to 255 with: {@code (L << 8 | H)} or the simpler equivalent {@code (L * 256 + H)}. These
     * assume L and H have been limited to the 0 to 255 range already. This does not bounds-check index. Because hue is
     * not typically measured between 0 and 255, getting that value is a bit different; you can use {@link #hue(int)} on
     * an Oklab int color, and multiply that hue by 256 to get H.
     * <br>
     * The distance this returns is a byte between 0 and 82 (both inclusive), as the Euclidean distance from the center
     * grayscale value at the lightness in the index, to the edge of the gamut at the same lightness, along the hue in
     * the index. This is measured in a space from -1 to 1 for both A and B, with the 0 in the center meaning grayscale,
     * and multiplied by 256 to get a meaningful byte value. To return to the A and B values Oklab uses here, you would
     * need to use some trigonometry on the hue (if it's in the 0 to 1 range, you can call
     * {@link TrigTools#cosTurns(float)} on the hue to almost get A, and {@link TrigTools#sinTurns(float)} to almost get B),
     * then multiply each of those by the distance, divide each by 256.0, and add 0.5.
     * <br>
     * Only intended for the narrow cases where external code needs read-only access to the internal Oklab gamut data.
     * The gamut data is quite large (this DescriptiveColor file is 283 KB at the time of writing, while a similar file
     * without the gamut is just 56 KB), so it's better to have direct read access to it without being able to
     * accidentally rewrite it.
     * @param index must be between 0 and 65535; the upper 8 bits are lightness and the lower 8 are hue angle.
     * @return a byte (always between 0 and 82, inclusive) representing the Euclidean distance between a grayscale value and the most saturated value possible, using the above measurements
     */
    public static byte getRawGamutValue(int index){
        return GAMUT_DATA[index];
    }

    /**
     * Returns true if the given packed int color, as Oklab, is valid to convert losslessly back to RGBA.
     * @param packed a packed int color as Oklab
     * @return true if the given packed int color can be converted back and forth to RGBA
     */
    public static boolean inGamut(final int packed)
    {
        final float A = ((packed >>> 8 & 0xff) - 127f) / 255f;
        final float B = ((packed >>> 16 & 0xff) - 127f) / 255f;
        final float g = GAMUT_DATA[(packed & 0xff) << 8 | (int)(256f * TrigTools.atan2Turns(B, A))];
        return g * g * 0x1p-18 + 0x1p-14 >= (A * A + B * B);
    }

    /**
     * Returns true if the given Oklab values are valid to convert losslessly back to RGBA.
     * @param L lightness channel, as an int from 0 to 255
     * @param A green-to-red chromatic channel, as an int from 0 to 255
     * @param B blue-to-yellow chromatic channel, as an int from 0 to 255
     * @return true if the given Oklab channels can be converted back and forth to RGBA
     */
    public static boolean inGamut(int L, int A, int B)
    {
        float A2 = (A - 127f) / 255f;
        float B2 = (B - 127f) / 255f;
        final float g = GAMUT_DATA[(L & 0xFF) << 8 | (int)(256f * TrigTools.atan2Turns(B2, A2))];
        return g * g * 0x1p-18 + 0x1p-14 >= (A2 * A2 + B2 * B2);
    }

    /**
     * Returns true if the given Oklab values are valid to convert losslessly back to RGBA.
     * @param L lightness channel, as a float from 0 to 1
     * @param A green-to-red chromatic channel, as a float from 0 to 1
     * @param B blue-to-yellow chromatic channel, as a float from 0 to 1
     * @return true if the given Oklab channels can be converted back and forth to RGBA
     */
    public static boolean inGamut(float L, float A, float B)
    {
        A = (A - 0.5f);
        B = (B - 0.5f);
        final byte g = GAMUT_DATA[((int)(L * 255.999f) << 8 & 0xFF00) | (int)(256f * TrigTools.atan2Turns(B, A))];
        return g * g * 0x1p-18 + 0x1p-14 >= (A * A + B * B);
    }

    /**
     * Gets the color with the same L as the Oklab color stored in the given packed int, but the furthest A
     * B from gray possible for that lightness while keeping the same hue as the given color. This is very
     * similar to calling {@link #enrich(int, float)} with a very large {@code change} value.
     * @param packed a packed int color in Oklab format; does not need to be in-gamut
     * @return the color that is as far from grayscale as this can get while keeping the L and hue of packed
     * @see #limitToGamut(int) You can use limitToGamut() if you only want max saturation for out-of-gamut colors.
     */
    public static int maximizeSaturation(final int packed) {
        final float A = ((packed >>> 8 & 0xff) - 127.5f);
        final float B = ((packed >>> 16 & 0xff) - 127.5f);
        final float hue = TrigTools.atan2Turns(B, A);
        final int idx = (packed & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        return ((packed & 0xFF0000FF) |
                (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16);
    }

    /**
     * Gets the color with the same L as in the given Oklab color channels, but the furthest A
     * B from gray possible for that lightness while keeping the same hue as the given color. This is very
     * similar to calling {@link #enrich(int, float)} with a very large {@code change} value.
     * @param L lightness component; will be clamped between 0 and 255 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 255 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 255 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 255 if it isn't already
     * @return the color that is as far from grayscale as this can get while keeping the L and hue of packed
     * @see #limitToGamut(int, int, int, int) You can use limitToGamut() if you only want max saturation for out-of-gamut colors.
     */
    public static int maximizeSaturation(int L, int A, int B, int alpha) {
        L = Math.min(Math.max(L, 0), 255);
        A = Math.min(Math.max(A, 0), 255);
        B = Math.min(Math.max(B, 0), 255);
        alpha = Math.min(Math.max(alpha, 0), 255);
        final float A2 = (A - 127.5f);
        final float B2 = (B - 127.5f);
        final float hue = TrigTools.atan2Turns(B2, A2);
        final int idx = L << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        return (
                alpha << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        L);
    }

    /**
     * Gets the color with the same L as the Oklab color stored in the given packed float, but the furthest A
     * B from gray possible for that lightness while keeping the same hue as the given color. This is very
     * similar to calling {@link #enrich(int, float)} with a very large {@code change} value.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 1 if it isn't already
     * @return the color that is as far from grayscale as this can get while keeping the L and hue of packed
     * @see #oklab(float, float, float, float) You can use oklab() if you only want max saturation for out-of-gamut colors.
     */
    public static float maximizeSaturation(float L, float A, float B, float alpha) {
        L = Math.min(Math.max(L, 0f), 1f);
        A = Math.min(Math.max(A, 0f), 1f);
        B = Math.min(Math.max(B, 0f), 1f);
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final float A2 = (A - 0.5f);
        final float B2 = (B - 0.5f);
        final float hue = TrigTools.atan2Turns(B2, A2);
        final int idx = (int) (L * 255.999f) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        return BitConversion.intBitsToFloat(
                (int) (alpha * 255.999f) << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        (int) (L * 255.999f));
    }

    /**
     * Checks whether the given Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness, or if it is already in-gamut, returns the color as-is.
     * @param packed a packed int color in Oklab format; often this color is not in-gamut
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(final int packed) {
        final float A = ((packed >>> 8 & 0xff) - 127.5f);
        final float B = ((packed >>> 16 & 0xff) - 127.5f);
        final float hue = TrigTools.atan2Turns(B, A);
        final int idx = (packed & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        if (dist * dist >= (A * A + B * B))
            return packed;
        return (
                (packed & 0xFF0000FF) |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16);
    }

    /**
     * Checks whether the given Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness, or if it is already in-gamut, returns the color as-is. This always produces
     * an opaque color.
     * @param L lightness component; will be clamped between 0 and 255 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 255 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 255 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int, int, int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(int L, int A, int B) {
        return limitToGamut(L, A, B, 255);
    }

    /**
     * Checks whether the given Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness, or if it is already in-gamut, returns the color as-is.
     * @param L lightness component; will be clamped between 0 and 255 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 255 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 255 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 255 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int, int, int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(int L, int A, int B, int alpha) {
        L = Math.min(Math.max(L, 0), 255);
        A = Math.min(Math.max(A, 0), 255);
        B = Math.min(Math.max(B, 0), 255);
        alpha = Math.min(Math.max(alpha, 0), 255);
        final float A2 = (A - 127.5f) / 255f;
        final float B2 = (B - 127.5f) / 255f;
        final float hue = TrigTools.atan2Turns(B2, A2);
        final int idx = L << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx] * 0.5f;
        if(dist * dist * 0x1p-16f + 0x1p-14f >= (A2 * A2 + B2 * B2))
            return L | A << 8 | B << 16 | alpha << 24;
        return (
                alpha << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        L);
    }


    /**
     * A different way to specify an Oklab color, using hue, saturation, lightness, and alpha like a normal HSL(A) color
     * but calculating them directly in the Oklab color space. Most colors between 0.5 and 0.75 hue that also have less
     * than 0.5 lightness are extremely desaturated and close to gray, regardless of what you give for saturation, and
     * these colors suddenly jump to very saturated around 0.75 hue and higher. To avoid this issue, you may prefer
     * using {@link #oklabByHCL(float, float, float, float)}, which takes an absolute chroma as opposed to the
     * saturation here (which is a fraction of the maximum chroma).
     * <br>
     * The saturation here refers to what fraction the chroma should be of the maximum
     * chroma for the given hue and lightness. You can use {@link #hue(int)}, {@link #saturation(int)},
     * and {@link #lightness(int)} to get the hue, saturation, and lightness values from an existing color that
     * this will understand ({@link #alpha(int)} too).
     * @param hue between 0 and 1, usually, but this will automatically wrap if too high or too low
     * @param saturation will be clamped between 0 and 1
     * @param lightness will be clamped between 0 and 1
     * @param alpha will be clamped between 0 and 1
     * @return a packed Oklab float color that tries to match the requested hue, saturation, and lightness
     */
    public static int oklabByHSL(float hue, float saturation, float lightness, float alpha) {
        lightness = Math.min(Math.max(lightness, 0f), 1f);
        saturation = Math.min(Math.max(saturation, 0f), 1f);
        hue -= fastFloor(hue);
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * saturation * 0.5f;
        return (
                (int) (alpha * 255.999f) << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        (int) (lightness * 255.999f));
    }

    /**
     * A different way to specify an Oklab color, using hue, chroma, lightness, and alpha something like a normal HSL(A)
     * color but calculating them directly in the Oklab color space. This has you specify the desired chroma directly,
     * as obtainable with {@link #chroma(int)}, rather than the saturation, which is a fraction of the maximum chroma
     * (saturation is what {@link #oklabByHSL(float, float, float, float)} uses). The hue is distributed as with
     * {@link #hue(int)}, and the lightness should be equivalent to {@link #lightness(int)}.
     * If you use this to get two colors with the same chroma and lightness, but different
     * hue, then the resulting colors should have similar colorfulness unless one or both chroma values exceeded the
     * gamut limit (you can get this limit with {@link #chromaLimit(float, float)}). If a chroma value given is greater
     * than the chroma limit, this clamps chroma to that limit. You can use {@link #hue(int)},
     * {@link #chroma(int)}, and {@link #lightness(int)} to get the hue, chroma, and lightness values from an
     * existing color that this will understand ({@link #alpha(int)} too).
     * @param hue between 0 and 1, usually, but this will automatically wrap if too high or too low
     * @param chroma will be clamped between 0 and the maximum chroma possible for the given hue and lightness
     * @param lightness will be clamped between 0 and 1
     * @param alpha will be clamped between 0 and 1
     * @return a packed Oklab float color that tries to match the requested hue, chroma, and lightness
     */
    public static int oklabByHCL(float hue, float chroma, float lightness, float alpha) {
        lightness = Math.min(Math.max(lightness, 0f), 1f);
        chroma = Math.max(chroma, 0f);
        hue -= fastFloor(hue);
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = Math.min(chroma * 127.5f, GAMUT_DATA[idx] * 0.5f);
        return (
                (int) (alpha * 255.999f) << 24 |
                        (int) (TrigTools.sinTurns(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cosTurns(hue) * dist + 128f) << 8 |
                        (int) (lightness * 255.999f));
    }

    /**
     * Interpolates from the packed int color start towards end by change. The colors (start and end) can be either
     * packed Oklab ints or RGBA8888 ints, and change can be between 0f (keep start) and 1f (only use end). This is a
     * good way to reduce allocations of temporary Colors. If the inputs were Oklab colors, you will
     * probably want to convert the color for rendering with {@link #toRGBA8888(int)}.
     *
     * @see #mix(int[], int, int)
     * @param start    the starting color as a packed int; may be RGBA or Oklab
     * @param end      the end/target color as a packed int; must be the same color space as start
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return a packed int in the same color space as start and end, that represents a color between start and end
     */
    public static int lerpColors(final int start, final int end, final float change) {
        final int
                sL = (start & 0xFF), sA = (start >>> 8) & 0xFF, sB = (start >>> 16) & 0xFF, sAlpha = start >>> 24 & 0xFF,
                eL = (end & 0xFF), eA = (end >>> 8) & 0xFF, eB = (end >>> 16) & 0xFF, eAlpha = end >>> 24 & 0xFF;
        return (((int) (sL + change * (eL - sL)) & 0xFF)
                | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | (((int) (sAlpha + change * (eAlpha - sAlpha)) & 0xFF) << 24));
    }

    /**
     * Interpolates from the packed Oklab int color start towards the Oklab int color end by change, but keeps the alpha
     * of start and uses the alpha of end as an extra factor that can affect how much to change. The colors (start and
     * end) must be packed Oklab ints, and change can be between 0f (keep start) and 1f (only use end). This is a good
     * way to reduce allocations of temporary Colors. Since the inputs are Oklab colors, you will probably want to
     * convert the color for rendering with {@link #toRGBA8888(int)}.
     *
     * @see #mix(int[], int, int)
     * @param start the starting color as a packed Oklab int; alpha will be preserved
     * @param end the target color as a packed Oklab int; alpha will not be used directly, and will instead be multiplied with change
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return a packed float that represents a color between start and end
     */
    public static int lerpColorsBlended(final int start, final int end, float change) {
        final int
                sL = (start & 0xFF), sA = (start >>> 8) & 0xFF, sB = (start >>> 16) & 0xFF, sAlpha = start & 0xFF000000,
                eL = (end & 0xFF), eA = (end >>> 8) & 0xFF, eB = (end >>> 16) & 0xFF, eAlpha = end >>> 24;
        change *= eAlpha * (1f / 255f);
        return (((int) (sL + change * (eL - sL)) & 0xFF)
                | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | sAlpha);
    }

    /**
     * Mixes the packed int color start and the packed in color end additively. The colors (start and end)
     * should be packed Oklab ints. This is a good way to reduce allocations of temporary Colors. This
     * clamps each component to the valid range for a packed Oklab int, but not to the Oklab gamut. You will
     * probably want to convert the color for rendering with {@link #toRGBA8888(int)}.
     * The additive mixing this does will amplify any difference from {@link #GRAY}, which is the neutral color here.
     * If you mix several int colors that are each, approximately, grayish-green, then with enough colors the result
     * will be a vivid, saturated green.
     * <br>
     * This is probably faster than {@link #lerpColors(int, int, float)} because it uses
     * entirely int math, though the clamping it does might not help speed.
     *
     * @see #additiveMix(int[], int, int)
     * @param start    the starting color as a packed int
     * @param end      the end/target color as a packed int
     * @return a packed Oklab int that represents a color between start and end
     */
    public static int addColors(final int start, final int end) {
        final int
                sL = (start & 0xFF), sA = (start >>> 8 & 0xFF), sB = (start >>> 16 & 0xFF), sAlpha = start >>> 24 & 0xFF,
                eL = (end & 0xFF), eA = (end >>> 8 & 0xFF), eB = (end >>> 16 & 0xFF), eAlpha = end >>> 24 & 0xFF;
        return (Math.min(Math.max(sL + eL - 0x80, 0), 255)
                | (Math.min(Math.max(sA + eA - 0x7F, 0), 255) << 8)
                | (Math.min(Math.max(sB + eB - 0x7F, 0), 255) << 16)
                | (Math.min(Math.max(sAlpha + eAlpha - 0x7F, 0), 255) << 24));
    }

    /**
     * Given several colors, this gets an even mix of all colors in equal measure.
     * If {@code colors} is null or has no items, this returns {@link #PLACEHOLDER} (a transparent placeholder used by
     * TextraTypist for "no color found").
     * This is mostly useful in conjunction with {@link IntList}, using its {@code items}
     * for colors, typically 0 for offset, and its {@code size} for size.
     *
     * @see #lerpColors(int, int, float)
     * @param colors an array of packed int colors; all should use the same color space
     * @param offset the index of the first item in {@code colors} to use
     * @param size how many items from {@code colors} to use
     * @return an even mix of all colors given, as a packed int color with the same color space as the input(s)
     */
    public static int mix(int[] colors, int offset, int size) {
        int end = offset + size;
        if(colors == null || colors.length < end || offset < 0 || size <= 0)
            return PLACEHOLDER; // transparent super-dark-blue, used to indicate "not found"
        int result = PLACEHOLDER;
        while(colors[offset] == PLACEHOLDER) {
            offset++;
        }
        if(offset < end)
            result = colors[offset];
        for (int i = offset + 1, denom = 2; i < end; i++, denom++) {
            if(colors[i] != PLACEHOLDER)
                result = lerpColors(result, colors[i], 1f / denom);
            else --denom;
        }
        return result;
    }

    /**
     * Mixes any number of colors with arbitrary weights per-color. Takes an array of varargs of alternating ints
     * representing colors and weights, as with {@code color, weight, color, weight...}.
     * If {@code colors} is null or has no items, this returns 0 (usually transparent in most color spaces). Each color
     * can be a packed Oklab int or an RGBA8888 int, but you can't use both of those kinds of color in one call.
     *
     * @see #lerpColors(int, int, float)
     * @param colors an array or varargs that should contain alternating {@code color, weight, color, weight...} ints
     * @return the mixed color, as a packed int in the same color space as the given int colors
     */
    public static int unevenMix(int... colors) {
        if(colors == null || colors.length == 0) return 0;
        if(colors.length <= 2) return colors[0];
        return unevenMix(colors, 0, colors.length);
    }

    /**
     * Mixes any number of colors with arbitrary weights per-color. Takes an array of alternating ints representing
     * colors and weights, as with {@code color, weight, color, weight...}, starting at {@code offset} in the array and
     * continuing for {@code size} indices in the array. The {@code size} should be an even number 2 or greater,
     * otherwise it will be reduced by 1. The weights can be any non-negative int values; this method handles
     * normalizing them internally. Each color can be a packed Oklab int or an RGBA8888 int, but you can't use both of
     * those kinds of color in one call.
     *
     * @see #lerpColors(int, int, float)
     * @param colors starting at {@code offset}, this should contain alternating {@code color, weight, color, weight...} ints
     * @param offset where to start reading from in {@code colors}
     * @param size how many indices to read from {@code colors}; must be an even number
     * @return the mixed color, as a packed int in the same color space as the given int colors
     */
    public static int unevenMix(int[] colors, int offset, int size) {
        size &= -2;
        final int end = offset + size;
        if(colors == null || colors.length < end || offset < 0 || size <= 0)
            return PLACEHOLDER; // transparent super-dark-blue, used to indicate "not found"
        while(colors[offset] == PLACEHOLDER) {
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

    /**
     * Given several colors, this gets an even mix of all colors in equal measure, mixing them additively and clamping.
     * If {@code colors} is null or has no items, this returns {@link #PLACEHOLDER} (a transparent placeholder used to
     * mean "no color found").
     * This is mostly useful in conjunction with {@link IntList}, using its {@code items}
     * for colors, typically 0 for offset, and its {@code size} for size.
     * The additive mixing this does will amplify any difference from {@link #GRAY}, which is the neutral color here.
     * If you mix several int colors that are each, approximately, grayish-green, then with enough colors the result
     * will be a vivid, saturated green.
     * <br>
     * This is probably faster than {@link #mix(int[], int, int)} because this uses only int math, and should also be
     * faster than individually mixing colors with {@link #addColors(int, int)} because it only clamps once. This does
     * produce different results than either of those.
     *
     * @see #addColors(int, int)
     * @param colors an array of packed int colors; all should use the same color space
     * @param offset the index of the first item in {@code colors} to use
     * @param size how many items from {@code colors} to use
     * @return an even mix of all colors given, as a packed int color with the same color space as the input(s)
     */
    public static int additiveMix(int[] colors, int offset, int size) {
        int end = offset + size;
        if(colors == null || colors.length < end || offset < 0 || size <= 0)
            return PLACEHOLDER; // transparent super-dark-blue, used to indicate "not found"
        int L = 0x80, A = 0x7F, B = 0x7F, O = 0x7F;
        while(colors[offset] == PLACEHOLDER) {
            offset++;
        }
        if(offset < end) {
            int t = colors[offset];
            L += (t >>> 24      ) - 0x80;
            A += (t >>> 16 & 255) - 0x7F;
            B += (t >>> 8  & 255) - 0x7F;
            O += (t        & 255) - 0x7F;
        }
        else
            return PLACEHOLDER;
        for (int i = offset + 1, denom = 2; i < end; i++, denom++) {
            if(colors[i] != PLACEHOLDER){
                int t = colors[i];
                L += (t >>> 24      ) - 0x80;
                A += (t >>> 16 & 255) - 0x7F;
                B += (t >>> 8  & 255) - 0x7F;
                O += (t        & 255) - 0x7F;
            }
            else --denom;
        }
        return Math.min(Math.max(L, 0), 255)
                | Math.min(Math.max(A, 0), 255) << 8
                | Math.min(Math.max(B, 0), 255) << 16
                | Math.min(Math.max(O, 0), 255) << 24
                ;
    }

    static {
        NAMES_BY_HUE.sort((o1, o2) -> {
            final int c1 = NAMED.get(o1), c2 = NAMED.get(o2);
            if(c1 >= 0) return -10000;
            else if(c2 >= 0) return 10000;

            final float s1 = chroma(c1), s2 = chroma(c2);
            if (s1 <= 0x1p-6f && s2 > 0x1p-6f)
                return -1000;
            else if (s1 > 0x1p-6f && s2 <= 0x1p-6f)
                return 1000;
            else if (s1 <= 0x1p-6f && s2 <= 0x1p-6f)
                return (c1 & 255) - (c2 & 255);
            else
                return ((int) Math.signum(hue(c1) - hue(c2)) << 8)
                        + (c1 & 255) - (c2 & 255);
        });
        for(String name : NAMES_BY_HUE) {
            COLORS_BY_HUE.add(NAMED.get(name));
        }
        NAMES_BY_LIGHTNESS.sort((o1, o2) -> (NAMED.get(o1) + 0x80000000 & 0x800000FF) - (NAMED.get(o2) + 0x80000000 & 0x800000FF));
    }

    private static final IntList mixing = new IntList(4);

    /**
     * Parses a color description and returns the approximate color it describes, as a packed RGBA8888 int color.
     * Color descriptions consist of one or more alphabetical words, separated by non-alphanumeric characters (typically
     * spaces and/or hyphens, though the underscore is treated as a letter). Any word that is the name of a color in
     * this palette will be looked up in {@link #NAMED} and tracked; if there is more than one of these color name
     * words, the colors will be mixed using {@link #unevenMix(int[], int, int)}, or if there is just one color name
     * word, then the corresponding color will be used. A number can be present after a color name (separated by any
     * non-alphanumeric character(s) other than the underscore); if so, it acts as a positive weight for that color name
     * when mixed with other named colors. The recommended separator between a color name and its weight is the char
     * {@code '^'}, but other punctuation like {@code ':'} is equally valid. You can also repeat a color name to
     * increase its weight.
     * <br>
     * The special adjectives "light" and "dark" change the lightness of the described color; likewise, "rich" and
     * "dull" change the saturation (how different the color is from grayscale). All of these adjectives can have "-er"
     * or "-est" appended to make their effect twice or three times as strong. Technically, the chars appended to an
     * adjective don't matter, only their count, so "lightaa" is the same as "lighter" and "richcat" is the same as
     * "richest". There's an unofficial fourth level as well, used when any 4 characters are appended to an adjective
     * (as in "darkmost"); it has four times the effect of the original adjective. There are also the adjectives
     * "bright" (equivalent to "light rich"), "pale" ("light dull"), "deep" ("dark rich"), and "weak" ("dark dull").
     * These can be amplified like the other four, except that "pale" goes to "paler", "palest", and then to
     * "palemax" or (its equivalent) "palemost", where only the word length is checked. The case of adjectives doesn't
     * matter here; they can be all-caps, all lower-case, or mixed-case without issues. The names of colors, however,
     * are case-sensitive, because you can combine other named color palettes with the one here, and at least in one
     * common situation (merging libGDX Colors with the palette here), the other palette uses all-caps names only.
     * <br>
     * If part of a color name or adjective is invalid, it is not considered; if the description is empty or fully
     * invalid, this returns the invalid "color" {@link #PLACEHOLDER} (also used by TextraTypist, and suggested as a
     * placeholder for other code).
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * "lightest richer apricot-olive", "bright magenta", "palest cyan blue", "deep fern black", "weakmost celery",
     * "red^3 orange", and "dark deep blue^7 cyan^3".
     * <br>
     * This method will check the first character of description and may change how it parses depending on that char.
     * If the first char is {@code #}, and there are 6 characters remaining, this parses those 6 characters as a hex
     * color in RGB888 format (treating it as opaque). If the first char is {@code #} and there are 8 or more characters
     * remaining, it parses 8 of those characters as an RGBA8888 hex color. If the first char is {@code |}, that char is
     * ignored and the rest of the CharSequence is treated as a color description (this is to ease parsing markup for
     * {@link #processColorMarkup(CharSequence)}). Otherwise, the whole CharSequence is parsed as a color description,
     * and the result is converted to an RGBA int.
     *
     * @param description a color description, as a String or other CharSequence matching the above format, or a {@code #}-prefixed hex color
     * @return a packed RGBA int color as described
     */
    public static int describe(final CharSequence description) {
        if(description == null || description.length() == 0) return 0;
        final char initial = description.charAt(0);
        if(initial == '#') {
            if (description.length() >= 7 && description.length() < 9)
                return DigitTools.intFromHex(description, 1, 7) << 8 | 0xFF;
            else if(description.length() >= 9)
                return DigitTools.intFromHex(description, 1, 9);
            return 0;
        }
        return toRGBA8888(describeOklab(description, initial == '|' ? 1 : 0, description.length()));
    }

    /**
     * Parses a color description and returns the approximate color it describes, as a packed Oklab int color.
     * Color descriptions consist of one or more alphabetical words, separated by non-alphanumeric characters (typically
     * spaces and/or hyphens, though the underscore is treated as a letter). Any word that is the name of a color in
     * this palette will be looked up in {@link #NAMED} and tracked; if there is more than one of these color name
     * words, the colors will be mixed using {@link #unevenMix(int[], int, int)}, or if there is just one color name
     * word, then the corresponding color will be used. A number can be present after a color name (separated by any
     * non-alphanumeric character(s) other than the underscore); if so, it acts as a positive weight for that color name
     * when mixed with other named colors. The recommended separator between a color name and its weight is the char
     * {@code '^'}, but other punctuation like {@code ':'} is equally valid. You can also repeat a color name to
     * increase its weight.
     * <br>
     * The special adjectives "light" and "dark" change the lightness of the described color; likewise, "rich" and
     * "dull" change the saturation (how different the color is from grayscale). All of these adjectives can have "-er"
     * or "-est" appended to make their effect twice or three times as strong. Technically, the chars appended to an
     * adjective don't matter, only their count, so "lightaa" is the same as "lighter" and "richcat" is the same as
     * "richest". There's an unofficial fourth level as well, used when any 4 characters are appended to an adjective
     * (as in "darkmost"); it has four times the effect of the original adjective. There are also the adjectives
     * "bright" (equivalent to "light rich"), "pale" ("light dull"), "deep" ("dark rich"), and "weak" ("dark dull").
     * These can be amplified like the other four, except that "pale" goes to "paler", "palest", and then to
     * "palemax" or (its equivalent) "palemost", where only the word length is checked. The case of adjectives doesn't
     * matter here; they can be all-caps, all lower-case, or mixed-case without issues. The names of colors, however,
     * are case-sensitive, because you can combine other named color palettes with the one here, and at least in one
     * common situation (merging libGDX Colors with the palette here), the other palette uses all-caps names only.
     * <br>
     * If part of a color name or adjective is invalid, it is not considered; if the description is empty or fully
     * invalid, this returns the invalid "color" {@link #PLACEHOLDER} (also used by TextraTypist, and suggested as a
     * placeholder for other code).
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * "lightest richer apricot-olive", "bright magenta", "palest cyan blue", "deep fern black", "weakmost celery",
     * "red^3 orange", and "dark deep blue^7 cyan^3".
     * <br>
     * This overload always considers its input a color description, and won't parse hex colors. It only handles the
     * simplest case, where the full provided {@code description} is only a color description.
     *
     * @param description a color description, as a String or other CharSequence matching the above format
     * @return a packed Oklab int color as described
     */
    public static int describeOklab(final CharSequence description) {
        return describeOklab(description, 0, description.length());
    }
    /**
     * Parses a color description and returns the approximate color it describes, as a packed Oklab int color.
     * Color descriptions consist of one or more alphabetical words, separated by non-alphanumeric characters (typically
     * spaces and/or hyphens, though the underscore is treated as a letter). Any word that is the name of a color in
     * this palette will be looked up in {@link #NAMED} and tracked; if there is more than one of these color name
     * words, the colors will be mixed using {@link #unevenMix(int[], int, int)}, or if there is just one color name
     * word, then the corresponding color will be used. A number can be present after a color name (separated by any
     * non-alphanumeric character(s) other than the underscore); if so, it acts as a positive weight for that color name
     * when mixed with other named colors. The recommended separator between a color name and its weight is the char
     * {@code '^'}, but other punctuation like {@code ':'} is equally valid. You can also repeat a color name to
     * increase its weight.
     * <br>
     * The special adjectives "light" and "dark" change the lightness of the described color; likewise, "rich" and
     * "dull" change the saturation (how different the color is from grayscale). All of these adjectives can have "-er"
     * or "-est" appended to make their effect twice or three times as strong. Technically, the chars appended to an
     * adjective don't matter, only their count, so "lightaa" is the same as "lighter" and "richcat" is the same as
     * "richest". There's an unofficial fourth level as well, used when any 4 characters are appended to an adjective
     * (as in "darkmost"); it has four times the effect of the original adjective. There are also the adjectives
     * "bright" (equivalent to "light rich"), "pale" ("light dull"), "deep" ("dark rich"), and "weak" ("dark dull").
     * These can be amplified like the other four, except that "pale" goes to "paler", "palest", and then to
     * "palemax" or (its equivalent) "palemost", where only the word length is checked. The case of adjectives doesn't
     * matter here; they can be all-caps, all lower-case, or mixed-case without issues. The names of colors, however,
     * are case-sensitive, because you can combine other named color palettes with the one here, and at least in one
     * common situation (merging libGDX Colors with the palette here), the other palette uses all-caps names only.
     * <br>
     * If part of a color name or adjective is invalid, it is not considered; if the description is empty or fully
     * invalid, this returns the invalid "color" {@link #PLACEHOLDER} (also used by TextraTypist, and suggested as a
     * placeholder for other code).
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * "lightest richer apricot-olive", "bright magenta", "palest cyan blue", "deep fern black", "weakmost celery",
     * "red^3 orange", and "dark deep blue^7 cyan^3".
     * <br>
     * This overload always considers its input a color description, and won't parse hex colors. You can specify a
     * starting index in {@code description} to read from and a maximum {@code length} to read before stopping. If
     * {@code length} is negative, this reads the rest of {@code description} after {@code start}.
     *
     * @param description a color description, as a String or other CharSequence matching the above format
     * @param start the first character index of the description to read from
     * @param length how much of description to attempt to parse; if negative, this parses until the end
     * @return a packed Oklab int color as described
     */
    public static int describeOklab(final CharSequence description, int start, int length) {
        float lightness = 0f, saturation = 0f;
        final String[] terms = description.toString().substring(start,
                length < 0 ? description.length() - start : Math.min(description.length(), start + length))
                .split("[^a-zA-Z0-9_]+");
        mixing.clear();
        for (int i = 0; i < terms.length; i++) {
            String term = terms[i];
            if (term == null || term.isEmpty()) continue;
            final int len = term.length();
            switch (term.charAt(0)) {
                case 'L':
                case 'l':
                    if (len > 2 && (term.charAt(2) == 'g' || term.charAt(2) == 'G')) { // light
                        switch (len) {
                            case 9:
                                lightness += 0.150f;
                            case 8:
                                lightness += 0.150f;
                            case 7:
                                lightness += 0.150f;
                            case 5:
                                lightness += 0.150f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
                case 'B':
                case 'b':
                    if (len > 3 && (term.charAt(3) == 'g' || term.charAt(3) == 'G')) { // bright
                        switch (len) {
                            case 10:
                                lightness += 0.150f;
                                saturation += 00.20000f;
                            case 9:
                                lightness += 0.150f;
                                saturation += 00.2000f;
                            case 8:
                                lightness += 0.150f;
                                saturation += 00.20f;
                            case 6:
                                lightness += 0.150f;
                                saturation += 00.20f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
                case 'P':
                case 'p':
                    if (len > 2 && (term.charAt(2) == 'l' || term.charAt(2) == 'L')) { // pale
                        switch (len) {
                            case 8: // palemost
                            case 7: // palerer
                                lightness += 0.150f;
                                saturation -= 00.20000f;
                            case 6: // palest
                                lightness += 0.150f;
                                saturation -= 00.2000f;
                            case 5: // paler
                                lightness += 0.150f;
                                saturation -= 00.200f;
                            case 4: // pale
                                lightness += 0.150f;
                                saturation -= 00.20f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
                case 'W':
                case 'w':
                    if (len > 3 && (term.charAt(3) == 'k' || term.charAt(3) == 'K')) { // weak
                        switch (len) {
                            case 8:
                                lightness -= 0.150f;
                                saturation -= 00.20000f;
                            case 7:
                                lightness -= 0.150f;
                                saturation -= 00.2000f;
                            case 6:
                                lightness -= 0.150f;
                                saturation -= 00.200f;
                            case 4:
                                lightness -= 0.150f;
                                saturation -= 00.20f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
                case 'R':
                case 'r':
                    if (len > 1 && (term.charAt(1) == 'i' || term.charAt(1) == 'I')) { // rich
                        switch (len) {
                            case 8:
                                saturation += 00.20000f;
                            case 7:
                                saturation += 00.2000f;
                            case 6:
                                saturation += 00.200f;
                            case 4:
                                saturation += 00.20f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
                case 'D':
                case 'd':
                    if (len > 1 && (term.charAt(1) == 'a' || term.charAt(1) == 'A')) { // dark
                        switch (len) {
                            case 8:
                                lightness -= 0.150f;
                            case 7:
                                lightness -= 0.150f;
                            case 6:
                                lightness -= 0.150f;
                            case 4:
                                lightness -= 0.150f;
                                continue;
                        }
                    } else if (len > 1 && (term.charAt(1) == 'u' || term.charAt(1) == 'U')) { // dull
                        switch (len) {
                            case 8:
                                saturation -= 00.20000f;
                            case 7:
                                saturation -= 00.2000f;
                            case 6:
                                saturation -= 00.200f;
                            case 4:
                                saturation -= 00.20f;
                                continue;
                        }
                    } else if (len > 3 && (term.charAt(3) == 'p' || term.charAt(3) == 'P')) { // deep
                        switch (len) {
                            case 8:
                                lightness -= 0.150f;
                                saturation += 00.20000f;
                            case 7:
                                lightness -= 0.150f;
                                saturation += 00.2000f;
                            case 6:
                                lightness -= 0.150f;
                                saturation += 00.200f;
                            case 4:
                                lightness -= 0.150f;
                                saturation += 00.20f;
                                continue;
                        }
                    }
                    mixing.add(NAMED.get(term), 1);
                    break;
            case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if(mixing.size() >= 2)
                        mixing.set((mixing.size() & -2) - 1, Base.BASE10.readInt(term));
                    break;
                default:
                    mixing.add(NAMED.get(term), 1);
                    break;
            }
        }
        if(mixing.size() < 2) return PLACEHOLDER;

        int result = unevenMix(mixing.items, 0, mixing.size());
        if(result == PLACEHOLDER) return result;

        if(saturation != 0f) {
            saturation = Math.min(Math.max(saturation + 1, 0), 1000);
            result = edit(result, 0f, 0f, 0f, 0f, 1f, saturation, saturation, 1f);
        }
        if(lightness == 0f)
            return result;
        if (lightness > 0f)
            return lerpColorsBlended(result, WHITE, lightness);
        return lerpColorsBlended(result, BLACK, -lightness);
    }


    private static final ObjectList<String> namesByHue = new ObjectList<>(NAMES_BY_HUE);
    private static final IntList colorsByHue = new IntList(COLORS_BY_HUE);
    static {
        int trn = namesByHue.indexOf("transparent");
        namesByHue.removeAt(trn);
        colorsByHue.removeAt(trn);
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

    /**
     * Given a color as a packed Oklab int, this finds the closest description it can to match the given color while
     * using at most {@code mixCount} colors to mix in. You should only use small numbers for mixCount, like 1 to 3;
     * this can take quite a while to run otherwise. This returns a String description that can be passed to
     * {@link #describe(CharSequence)}. It is likely that this will use very contrasting colors if mixCount is 2 or
     * greater and the color to match is desaturated or brownish.
     * @param oklab a packed Oklab int color to attempt to match
     * @param mixCount how many color names this will use in the returned description
     * @return a description that can be fed to {@link #describe(CharSequence)} to get a similar color
     */
    @NonNull
    public static String bestMatch(final int oklab, int mixCount) {
        mixCount = Math.max(1, mixCount);
        float bestDistance = Float.POSITIVE_INFINITY;
        final int paletteSize = namesByHue.size(), colorTries = (int)Math.pow(paletteSize, mixCount), totalTries = colorTries * 81;
        final float targetL = channelL(oklab), targetA = channelA(oklab), targetB = channelB(oklab);
        final String[] lightAdjectives = {"darkmost ", "darkest ", "darker ", "dark ", "", "light ", "lighter ", "lightest ", "lightmost "};
        final String[] satAdjectives = {"dullmost ", "dullest ", "duller ", "dull ", "", "rich ", "richer ", "richest ", "richmost "};
        mixing.clear();
        for (int i = 0; i < mixCount; i++) {
            mixing.add(colorsByHue.get(0));
        }
        int bestCode = 0;
        for (int c = 0; c < totalTries; c++) {
            for (int i = 0, e = 1; i < mixCount; i++, e *= paletteSize) {
                mixing.set(i, colorsByHue.get((c / e) % paletteSize));
            }
            int idxI = ((c / colorTries) % 9 - 4), idxS = (c / (colorTries * 9) - 4);

            int result = mix(mixing.items, 0, mixCount);
            if(idxI > 0) result = lighten(result, 0.125f * idxI);
            else if(idxI < 0) result = darken(result, -0.15f * idxI);

            if(idxS > 0) result = limitToGamut(enrich(result, 0.2f * idxS));
            else if(idxS < 0) result = dullen(result, -0.2f * idxS);
            else result = limitToGamut(result);

            float dL = channelL(result) - targetL, dA = channelA(result) - targetA, dB = channelB(result) - targetB;
            if(bestDistance > (bestDistance = Math.min(dL * dL + dA * dA + dB * dB, bestDistance)))
                bestCode = c;
        }

        StringBuilder description = new StringBuilder(lightAdjectives[(bestCode / colorTries) % 9] + satAdjectives[bestCode / (colorTries * 9)]);
        for (int i = 0, e = 1; i < mixCount; e *= paletteSize) {
            description.append(namesByHue.get((bestCode / e) % paletteSize));
            if(++i < mixCount)
                description.append(' ');
        }
        return description.toString();
    }

    private static final StringBuilder builder = new StringBuilder(80);
    private static final Substitution sub = (MatchResult matchResult, TextBuffer textBuffer) -> {
        builder.setLength(0);
        matchResult.getGroup(1, builder);
        textBuffer.append('[');
        textBuffer.append('#');
        textBuffer.append(DigitTools.hex(describe(builder)));
        textBuffer.append(']');
    };
    private static final Replacer rep = new Replacer(Pattern.compile("(?<!\\[)\\[(\\|[^\\]]*)(?:\\]|$)"), sub);

    /**
     * Processes color markup of the form {@code [|description]}, where {@code description} is in the format that
     * {@link #describe(CharSequence)} understands (such as "light dullest green cyan"), and changes the markup
     * to a format libGDX can use, {@code [#FF7F00FF]}. The only addition this makes to libGDX markup is the {@code [|}
     * starting character combination, which here marks a color description rather than a named color from libGDX's
     * Colors class (which are all upper-case, so they wouldn't conflict anyway).
     * @param markupString a CharSequence, most commonly a String, that may contain color markup starting with {@code [|}
     * @return a String that has the {@code [|} markup changed to libGDX-compatible {@code [#}
     */
    public static String processColorMarkup(CharSequence markupString) {
        builder.setLength(0);
        return rep.replace(markupString);
    }

    /**
     * Given a packed int Oklab color {@code mainColor} and another Oklab color that it should be made to contrast with,
     * gets a packed int Oklab color with L that should be quite different from {@code contrastingColor}'s L, but the
     * same chromatic channels and opacity (A and B are likely to be clamped if the result gets close to white or
     * black). This allows most of the colors this method produces to contrast well as a foreground when displayed on a
     * background of {@code contrastingColor}, or vice versa.
     * @param mainColor a packed Oklab int color; this is the color that will be adjusted
     * @param contrastingColor a packed Oklab int color; the adjusted mainColor will contrast with the L of this
     * @return a different packed Oklab int color, based on mainColor but typically with different lightness
     */
    public static int differentiateLightness(final int mainColor, final int contrastingColor)
    {
        return limitToGamut((mainColor & 0xFFFFFF00) | (contrastingColor + 128 & 0xFF) + (mainColor & 0xFF) >>> 1);
    }

    /**
     * Pretty simple; adds 128 (or 0.5) to the given color's L and wraps it around if it would go above 255 (or 1.0),
     * then averages that with the original L. This means light colors become darker, and dark colors become lighter,
     * with almost all results in the middle-range of possible lightness.
     * @param oklab a packed Oklab int color
     * @return a different packed Oklab int color, with its L channel changed and limited to the correct gamut
     */
    public static int offsetLightness(final int oklab) {
        return limitToGamut((oklab & 0xFFFFFF00) | (oklab + 128 & 0xFF) + (oklab & 0xFF) >>> 1);
    }

    /**
     * Given an RGBA8888 int such as one returned by {@link #describe(CharSequence)}, this returns a packed float color
     * that can be given in some places to libGDX. This does not involve Oklab at all.
     * @param rgba an RGBA8888 int
     * @return an ABGR packed float color
     */
    public static float rgbaIntToFloat(final int rgba){
        return BitConversion.reversedIntBitsToFloat(rgba & -2);
    }

    /**
     * Gets the squared Euclidean distance between two colors as packed Oklab ints. This is a very approximate measure
     * of how different two colors are from each other, but in Oklab this measurement is relatively more accurate than
     * if you used plain RGB as the color space, or even IPT_HQ. The result should typically be rather small; lightness
     * can only be 1 unit of distance apart at most (from black to white), and the A and B channels are usually just a
     * tiny bit away from the vertical lightness axis. A result of 1 may be the largest this can produce, but it is very
     * possible that somewhat larger results can happen. Note that this is squared, so it isn't an admissible metric for
     * some usage, like summing up distances.
     * @param colorA a packed Oklab int color
     * @param colorB a packed Oklab int color
     * @return the squared Euclidean distance between encodedA and encodedB; usually less than 1.0f
     */
    public static float distanceSquared(final int colorA, final int colorB) {
        final float LA = reverseLight((colorA & 0xff) / 255f);
        final float AA = ((colorA >>> 8 & 0xff) - 127f) / 127f;
        final float BA = ((colorA >>> 16 & 255) - 127f) / 127f;
        final float LDiff = reverseLight((colorB & 0xff) / 255f) - LA;
        final float ADiff = ((colorB >>> 8 & 0xff) - 127f) / 127f - AA;
        final float BDiff = ((colorB >>> 16 & 255) - 127f) / 127f - BA;
        return LDiff * LDiff + ADiff * ADiff + BDiff * BDiff;
    }

    /**
     * Gets the actual Euclidean distance between two colors as packed Oklab ints. This is an approximate measure
     * of how different two colors are from each other, but in Oklab this measurement is relatively more accurate than
     * if you used plain RGB as the color space. The result should typically be rather small; lightness can only be 1
     * unit of distance apart at most (from black to white), and the A and B channels are usually just a tiny bit away
     * from the vertical lightness axis. A result of 1 may be the largest this can produce, but it is very possible that
     * somewhat larger results can happen.
     * @param encodedA a packed Oklab int color
     * @param encodedB a packed Oklab int color
     * @return the Euclidean distance between encodedA and encodedB; usually less than 1.0f
     */
    public static float distance(final int encodedA, final int encodedB) {
        return (float) Math.sqrt(distanceSquared(encodedA, encodedB));
    }

    /**
     * A palette, the same as the one in {@link DescriptiveColor}, just using RGBA8888 ints instead of Oklab ints for
     * each color entry. This provides the same ways of looking up colors, but nothing currently to modify int colors.
     * There is considerably more existing code available elsewhere to modify RGBA colors, though.
     */
    public static final class Rgb {
        /**
         * No need to extend this.
         */
        private Rgb() {
        }

        /**
         * You can look up colors by name here; the names are lower-case, and the colors are packed ints in Oklab format.
         */
        public static final ObjectIntOrderedMap<String> NAMED = new ObjectIntOrderedMap<>(50);
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

        public static final IntList LIST = new IntList(50);

        /**
         * A fully-transparent color that is out-of-range for valid colors in Oklab, to be used as a placeholder for colors
         * that aren't valid in some way.
         */
        public static final int PLACEHOLDER = 0x00000100;


        /**
         * This color constant "transparent" has RGBA8888 code {@code 0x00000000}, red 0.0, green 0.0, blue 0.0, alpha 0.0, hue 0.0, saturation 0.0, and lightness 0.0.
         * <pre>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int TRANSPARENT = 0x00000000;
        static { NAMED.put("transparent", 0x00000000); LIST.add(0x00000000); }

        /**
         * This color constant "black" has RGBA8888 code {@code 0x000000FF}, red 0.0, green 0.0, blue 0.0, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.0.
         * <pre>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BLACK = 0x000000ff;
        static { NAMED.put("black", 0x000000ff); LIST.add(0x000000ff); }

        /**
         * This color constant "gray" has RGBA8888 code {@code 0x808080FF}, red 0.5019608, green 0.5019608, blue 0.5019608, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.5019608.
         * <pre>
         * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int GRAY = 0x808080ff;
        static { NAMED.put("gray", 0x808080ff); LIST.add(0x808080ff); }

        /**
         * This color constant "silver" has RGBA8888 code {@code 0xB6B6B6FF}, red 0.7137255, green 0.7137255, blue 0.7137255, alpha 1.0, hue 0.0, saturation 0.0, and lightness 0.7137255.
         * <pre>
         * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int SILVER = 0xb6b6b6ff;
        static { NAMED.put("silver", 0xb6b6b6ff); LIST.add(0xb6b6b6ff); }

        /**
         * This color constant "white" has RGBA8888 code {@code 0xFFFFFFFF}, red 1.0, green 1.0, blue 1.0, alpha 1.0, hue 0.0, saturation 0.0, and lightness 1.0.
         * <pre>
         * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int WHITE = 0xffffffff;
        static { NAMED.put("white", 0xffffffff); LIST.add(0xffffffff); }

        /**
         * This color constant "red" has RGBA8888 code {@code 0xFF0000FF}, red 1.0, green 0.0, blue 0.0, alpha 1.0, hue 0.0, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int RED = 0xff0000ff;
        static { NAMED.put("red", 0xff0000ff); LIST.add(0xff0000ff); }

        /**
         * This color constant "orange" has RGBA8888 code {@code 0xFF7F00FF}, red 1.0, green 0.49803922, blue 0.0, alpha 1.0, hue 0.08300654, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int ORANGE = 0xff7f00ff;
        static { NAMED.put("orange", 0xff7f00ff); LIST.add(0xff7f00ff); }

        /**
         * This color constant "yellow" has RGBA8888 code {@code 0xFFFF00FF}, red 1.0, green 1.0, blue 0.0, alpha 1.0, hue 0.16666667, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int YELLOW = 0xffff00ff;
        static { NAMED.put("yellow", 0xffff00ff); LIST.add(0xffff00ff); }

        /**
         * This color constant "green" has RGBA8888 code {@code 0x00FF00FF}, red 0.0, green 1.0, blue 0.0, alpha 1.0, hue 0.33333334, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int GREEN = 0x00ff00ff;
        static { NAMED.put("green", 0x00ff00ff); LIST.add(0x00ff00ff); }

        /**
         * This color constant "blue" has RGBA8888 code {@code 0x0000FFFF}, red 0.0, green 0.0, blue 1.0, alpha 1.0, hue 0.6666667, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BLUE = 0x0000ffff;
        static { NAMED.put("blue", 0x0000ffff); LIST.add(0x0000ffff); }

        /**
         * This color constant "indigo" has RGBA8888 code {@code 0x520FE0FF}, red 0.32156864, green 0.05882353, blue 0.8784314, alpha 1.0, hue 0.7200957, saturation 0.81960785, and lightness 0.46862745.
         * <pre>
         * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int INDIGO = 0x520fe0ff;
        static { NAMED.put("indigo", 0x520fe0ff); LIST.add(0x520fe0ff); }

        /**
         * This color constant "violet" has RGBA8888 code {@code 0x9040EFFF}, red 0.5647059, green 0.2509804, blue 0.9372549, alpha 1.0, hue 0.74285716, saturation 0.6862745, and lightness 0.59411764.
         * <pre>
         * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int VIOLET = 0x9040efff;
        static { NAMED.put("violet", 0x9040efff); LIST.add(0x9040efff); }

        /**
         * This color constant "purple" has RGBA8888 code {@code 0xC000FFFF}, red 0.7529412, green 0.0, blue 1.0, alpha 1.0, hue 0.7921569, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int PURPLE = 0xc000ffff;
        static { NAMED.put("purple", 0xc000ffff); LIST.add(0xc000ffff); }

        /**
         * This color constant "brown" has RGBA8888 code {@code 0x8F573BFF}, red 0.56078434, green 0.34117648, blue 0.23137255, alpha 1.0, hue 0.055555552, saturation 0.3294118, and lightness 0.39607844.
         * <pre>
         * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BROWN = 0x8f573bff;
        static { NAMED.put("brown", 0x8f573bff); LIST.add(0x8f573bff); }

        /**
         * This color constant "pink" has RGBA8888 code {@code 0xFFA0E0FF}, red 1.0, green 0.627451, blue 0.8784314, alpha 1.0, hue 0.8877193, saturation 0.372549, and lightness 0.8137255.
         * <pre>
         * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int PINK = 0xffa0e0ff;
        static { NAMED.put("pink", 0xffa0e0ff); LIST.add(0xffa0e0ff); }

        /**
         * This color constant "magenta" has RGBA8888 code {@code 0xF500F5FF}, red 0.9607843, green 0.0, blue 0.9607843, alpha 1.0, hue 0.8333333, saturation 0.9607843, and lightness 0.48039216.
         * <pre>
         * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int MAGENTA = 0xf500f5ff;
        static { NAMED.put("magenta", 0xf500f5ff); LIST.add(0xf500f5ff); }

        /**
         * This color constant "brick" has RGBA8888 code {@code 0xD5524AFF}, red 0.8352941, green 0.32156864, blue 0.2901961, alpha 1.0, hue 0.009592325, saturation 0.54509807, and lightness 0.5627451.
         * <pre>
         * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BRICK = 0xd5524aff;
        static { NAMED.put("brick", 0xd5524aff); LIST.add(0xd5524aff); }

        /**
         * This color constant "ember" has RGBA8888 code {@code 0xF55A32FF}, red 0.9607843, green 0.3529412, blue 0.19607843, alpha 1.0, hue 0.034188036, saturation 0.7647059, and lightness 0.5784313.
         * <pre>
         * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int EMBER = 0xf55a32ff;
        static { NAMED.put("ember", 0xf55a32ff); LIST.add(0xf55a32ff); }

        /**
         * This color constant "salmon" has RGBA8888 code {@code 0xFF6262FF}, red 1.0, green 0.38431373, blue 0.38431373, alpha 1.0, hue 0.0, saturation 0.6156863, and lightness 0.69215685.
         * <pre>
         * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int SALMON = 0xff6262ff;
        static { NAMED.put("salmon", 0xff6262ff); LIST.add(0xff6262ff); }

        /**
         * This color constant "chocolate" has RGBA8888 code {@code 0x683818FF}, red 0.40784314, green 0.21960784, blue 0.09411765, alpha 1.0, hue 0.066666655, saturation 0.3137255, and lightness 0.25098038.
         * <pre>
         * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CHOCOLATE = 0x683818ff;
        static { NAMED.put("chocolate", 0x683818ff); LIST.add(0x683818ff); }

        /**
         * This color constant "tan" has RGBA8888 code {@code 0xD2B48CFF}, red 0.8235294, green 0.7058824, blue 0.54901963, alpha 1.0, hue 0.0952381, saturation 0.2745098, and lightness 0.6862745.
         * <pre>
         * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int TAN = 0xd2b48cff;
        static { NAMED.put("tan", 0xd2b48cff); LIST.add(0xd2b48cff); }

        /**
         * This color constant "bronze" has RGBA8888 code {@code 0xCE8E31FF}, red 0.80784315, green 0.5568628, blue 0.19215687, alpha 1.0, hue 0.09872612, saturation 0.6156863, and lightness 0.49999997.
         * <pre>
         * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BRONZE = 0xce8e31ff;
        static { NAMED.put("bronze", 0xce8e31ff); LIST.add(0xce8e31ff); }

        /**
         * This color constant "cinnamon" has RGBA8888 code {@code 0xD2691DFF}, red 0.8235294, green 0.4117647, blue 0.11372549, alpha 1.0, hue 0.06998159, saturation 0.70980394, and lightness 0.46862742.
         * <pre>
         * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CINNAMON = 0xd2691dff;
        static { NAMED.put("cinnamon", 0xd2691dff); LIST.add(0xd2691dff); }

        /**
         * This color constant "apricot" has RGBA8888 code {@code 0xFFA828FF}, red 1.0, green 0.65882355, blue 0.15686275, alpha 1.0, hue 0.09922481, saturation 0.84313726, and lightness 0.57843137.
         * <pre>
         * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int APRICOT = 0xffa828ff;
        static { NAMED.put("apricot", 0xffa828ff); LIST.add(0xffa828ff); }

        /**
         * This color constant "peach" has RGBA8888 code {@code 0xFFBF81FF}, red 1.0, green 0.7490196, blue 0.5058824, alpha 1.0, hue 0.08201058, saturation 0.49411762, and lightness 0.7529412.
         * <pre>
         * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int PEACH = 0xffbf81ff;
        static { NAMED.put("peach", 0xffbf81ff); LIST.add(0xffbf81ff); }

        /**
         * This color constant "pear" has RGBA8888 code {@code 0xD3E330FF}, red 0.827451, green 0.8901961, blue 0.1882353, alpha 1.0, hue 0.18156426, saturation 0.7019608, and lightness 0.5392157.
         * <pre>
         * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int PEAR = 0xd3e330ff;
        static { NAMED.put("pear", 0xd3e330ff); LIST.add(0xd3e330ff); }

        /**
         * This color constant "saffron" has RGBA8888 code {@code 0xFFD510FF}, red 1.0, green 0.8352941, blue 0.0627451, alpha 1.0, hue 0.13737796, saturation 0.9372549, and lightness 0.53137255.
         * <pre>
         * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int SAFFRON = 0xffd510ff;
        static { NAMED.put("saffron", 0xffd510ff); LIST.add(0xffd510ff); }

        /**
         * This color constant "butter" has RGBA8888 code {@code 0xFFF288FF}, red 1.0, green 0.9490196, blue 0.53333336, alpha 1.0, hue 0.14845939, saturation 0.46666664, and lightness 0.76666665.
         * <pre>
         * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int BUTTER = 0xfff288ff;
        static { NAMED.put("butter", 0xfff288ff); LIST.add(0xfff288ff); }

        /**
         * This color constant "chartreuse" has RGBA8888 code {@code 0xC8FF41FF}, red 0.78431374, green 1.0, blue 0.25490198, alpha 1.0, hue 0.21491227, saturation 0.745098, and lightness 0.627451.
         * <pre>
         * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CHARTREUSE = 0xc8ff41ff;
        static { NAMED.put("chartreuse", 0xc8ff41ff); LIST.add(0xc8ff41ff); }

        /**
         * This color constant "cactus" has RGBA8888 code {@code 0x30A000FF}, red 0.1882353, green 0.627451, blue 0.0, alpha 1.0, hue 0.28333336, saturation 0.627451, and lightness 0.3137255.
         * <pre>
         * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CACTUS = 0x30a000ff;
        static { NAMED.put("cactus", 0x30a000ff); LIST.add(0x30a000ff); }

        /**
         * This color constant "lime" has RGBA8888 code {@code 0x93D300FF}, red 0.5764706, green 0.827451, blue 0.0, alpha 1.0, hue 0.21721959, saturation 0.827451, and lightness 0.4137255.
         * <pre>
         * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int LIME = 0x93d300ff;
        static { NAMED.put("lime", 0x93d300ff); LIST.add(0x93d300ff); }

        /**
         * This color constant "olive" has RGBA8888 code {@code 0x818000FF}, red 0.5058824, green 0.5019608, blue 0.0, alpha 1.0, hue 0.16537468, saturation 0.5058824, and lightness 0.2529412.
         * <pre>
         * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int OLIVE = 0x818000ff;
        static { NAMED.put("olive", 0x818000ff); LIST.add(0x818000ff); }

        /**
         * This color constant "fern" has RGBA8888 code {@code 0x4E7942FF}, red 0.30588236, green 0.4745098, blue 0.25882354, alpha 1.0, hue 0.2969697, saturation 0.21568626, and lightness 0.36666664.
         * <pre>
         * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int FERN = 0x4e7942ff;
        static { NAMED.put("fern", 0x4e7942ff); LIST.add(0x4e7942ff); }

        /**
         * This color constant "moss" has RGBA8888 code {@code 0x204608FF}, red 0.1254902, green 0.27450982, blue 0.03137255, alpha 1.0, hue 0.26881722, saturation 0.24313727, and lightness 0.15294118.
         * <pre>
         * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int MOSS = 0x204608ff;
        static { NAMED.put("moss", 0x204608ff); LIST.add(0x204608ff); }

        /**
         * This color constant "celery" has RGBA8888 code {@code 0x7DFF73FF}, red 0.49019608, green 1.0, blue 0.4509804, alpha 1.0, hue 0.32142857, saturation 0.5490196, and lightness 0.7254902.
         * <pre>
         * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CELERY = 0x7dff73ff;
        static { NAMED.put("celery", 0x7dff73ff); LIST.add(0x7dff73ff); }

        /**
         * This color constant "sage" has RGBA8888 code {@code 0xABE3C5FF}, red 0.67058825, green 0.8901961, blue 0.77254903, alpha 1.0, hue 0.4107143, saturation 0.21960783, and lightness 0.78039217.
         * <pre>
         * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int SAGE = 0xabe3c5ff;
        static { NAMED.put("sage", 0xabe3c5ff); LIST.add(0xabe3c5ff); }

        /**
         * This color constant "jade" has RGBA8888 code {@code 0x3FBF3FFF}, red 0.24705882, green 0.7490196, blue 0.24705882, alpha 1.0, hue 0.33333334, saturation 0.5019608, and lightness 0.49803922.
         * <pre>
         * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int JADE = 0x3fbf3fff;
        static { NAMED.put("jade", 0x3fbf3fff); LIST.add(0x3fbf3fff); }

        /**
         * This color constant "cyan" has RGBA8888 code {@code 0x00FFFFFF}, red 0.0, green 1.0, blue 1.0, alpha 1.0, hue 0.5, saturation 1.0, and lightness 0.5.
         * <pre>
         * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int CYAN = 0x00ffffff;
        static { NAMED.put("cyan", 0x00ffffff); LIST.add(0x00ffffff); }

        /**
         * This color constant "mint" has RGBA8888 code {@code 0x7FFFD4FF}, red 0.49803922, green 1.0, blue 0.83137256, alpha 1.0, hue 0.44401044, saturation 0.50196075, and lightness 0.7490196.
         * <pre>
         * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int MINT = 0x7fffd4ff;
        static { NAMED.put("mint", 0x7fffd4ff); LIST.add(0x7fffd4ff); }

        /**
         * This color constant "teal" has RGBA8888 code {@code 0x007F7FFF}, red 0.0, green 0.49803922, blue 0.49803922, alpha 1.0, hue 0.5, saturation 0.49803922, and lightness 0.24901961.
         * <pre>
         * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int TEAL = 0x007f7fff;
        static { NAMED.put("teal", 0x007f7fff); LIST.add(0x007f7fff); }

        /**
         * This color constant "turquoise" has RGBA8888 code {@code 0x2ED6C9FF}, red 0.18039216, green 0.8392157, blue 0.7882353, alpha 1.0, hue 0.48710316, saturation 0.65882355, and lightness 0.5098039.
         * <pre>
         * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int TURQUOISE = 0x2ed6c9ff;
        static { NAMED.put("turquoise", 0x2ed6c9ff); LIST.add(0x2ed6c9ff); }

        /**
         * This color constant "sky" has RGBA8888 code {@code 0x10C0E0FF}, red 0.0627451, green 0.7529412, blue 0.8784314, alpha 1.0, hue 0.5256411, saturation 0.8156863, and lightness 0.47058824.
         * <pre>
         * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int SKY = 0x10c0e0ff;
        static { NAMED.put("sky", 0x10c0e0ff); LIST.add(0x10c0e0ff); }

        /**
         * This color constant "cobalt" has RGBA8888 code {@code 0x0046ABFF}, red 0.0, green 0.27450982, blue 0.67058825, alpha 1.0, hue 0.5984406, saturation 0.67058825, and lightness 0.33529413.
         * <pre>
         * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int COBALT = 0x0046abff;
        static { NAMED.put("cobalt", 0x0046abff); LIST.add(0x0046abff); }

        /**
         * This color constant "denim" has RGBA8888 code {@code 0x3088B8FF}, red 0.1882353, green 0.53333336, blue 0.72156864, alpha 1.0, hue 0.5588235, saturation 0.53333336, and lightness 0.45490196.
         * <pre>
         * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int DENIM = 0x3088b8ff;
        static { NAMED.put("denim", 0x3088b8ff); LIST.add(0x3088b8ff); }

        /**
         * This color constant "navy" has RGBA8888 code {@code 0x000080FF}, red 0.0, green 0.0, blue 0.5019608, alpha 1.0, hue 0.6666667, saturation 0.5019608, and lightness 0.2509804.
         * <pre>
         * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int NAVY = 0x000080ff;
        static { NAMED.put("navy", 0x000080ff); LIST.add(0x000080ff); }

        /**
         * This color constant "lavender" has RGBA8888 code {@code 0xB991FFFF}, red 0.7254902, green 0.5686275, blue 1.0, alpha 1.0, hue 0.72727275, saturation 0.43137252, and lightness 0.78431374.
         * <pre>
         * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int LAVENDER = 0xb991ffff;
        static { NAMED.put("lavender", 0xb991ffff); LIST.add(0xb991ffff); }

        /**
         * This color constant "plum" has RGBA8888 code {@code 0xBE0DC6FF}, red 0.74509805, green 0.050980393, blue 0.7764706, alpha 1.0, hue 0.82612616, saturation 0.7254902, and lightness 0.4137255.
         * <pre>
         * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int PLUM = 0xbe0dc6ff;
        static { NAMED.put("plum", 0xbe0dc6ff); LIST.add(0xbe0dc6ff); }

        /**
         * This color constant "mauve" has RGBA8888 code {@code 0xAB73ABFF}, red 0.67058825, green 0.4509804, blue 0.67058825, alpha 1.0, hue 0.8333334, saturation 0.21960786, and lightness 0.56078434.
         * <pre>
         * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int MAUVE = 0xab73abff;
        static { NAMED.put("mauve", 0xab73abff); LIST.add(0xab73abff); }

        /**
         * This color constant "rose" has RGBA8888 code {@code 0xE61E78FF}, red 0.9019608, green 0.11764706, blue 0.47058824, alpha 1.0, hue 0.925, saturation 0.78431374, and lightness 0.5098039.
         * <pre>
         * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int ROSE = 0xe61e78ff;
        static { NAMED.put("rose", 0xe61e78ff); LIST.add(0xe61e78ff); }

        /**
         * This color constant "raspberry" has RGBA8888 code {@code 0x911437FF}, red 0.5686275, green 0.078431375, blue 0.21568628, alpha 1.0, hue 0.9533333, saturation 0.4901961, and lightness 0.32352945.
         * <pre>
         * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
         * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
         * </pre>
         */
        public static final int RASPBERRY = 0x911437ff;
        static { NAMED.put("raspberry", 0x911437ff); LIST.add(0x911437ff); }

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

        static {
            NAMES_BY_HUE.sort((o1, o2) -> {
                final int c1 = NAMED.get(o1), c2 = NAMED.get(o2);
                if(c1 >= 0) return -10000;
                else if(c2 >= 0) return 10000;

                final float s1 = chroma(c1), s2 = chroma(c2);
                if (s1 <= 0x1p-6f && s2 > 0x1p-6f)
                    return -1000;
                else if (s1 > 0x1p-6f && s2 <= 0x1p-6f)
                    return 1000;
                else if (s1 <= 0x1p-6f && s2 <= 0x1p-6f)
                    return (c1 & 255) - (c2 & 255);
                else
                    return ((int) Math.signum(hue(c1) - hue(c2)) << 8)
                            + (c1 & 255) - (c2 & 255);
            });
            for(String name : NAMES_BY_HUE) {
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

}