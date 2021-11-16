package com.github.yellowstonegames.core;

import com.github.tommyettinger.ds.*;
import com.github.tommyettinger.ds.support.BitConversion;
import regexodus.MatchResult;
import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.Replacer;
import regexodus.Substitution;
import regexodus.TextBuffer;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
     * This color constant "transparent" has RGBA8888 code {@code 00000000}, L 0.0, Chro 0.0055242716, CLim 0.31640625, alpha 0.0, hue 0.625, and saturation 0.01797164.
     * It has the encoded Oklab value 0x007f7f00 .
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TRANSPARENT = 0x007f7f00;
    static { NAMED.put("transparent", 0x007f7f00); LIST.add(0x007f7f00); }

    /**
     * This color constant "black" has RGBA8888 code {@code 000000FF}, L 0.0, Chro 0.0055242716, CLim 0.31640625, alpha 1.0, hue 0.625, and saturation 0.01797164.
     * It has the encoded Oklab value 0xfe7f7f00 .
     * <pre>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;@&nbsp;</font><font style='background-color: #000000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLACK = 0xfe7f7f00;
    static { NAMED.put("black", 0xfe7f7f00); LIST.add(0xfe7f7f00); }

    /**
     * This color constant "gray" has RGBA8888 code {@code 808080FF}, L 0.49411765, Chro 0.0055242716, CLim 0.1328125, alpha 1.0, hue 0.625, and saturation 0.04436749.
     * It has the encoded Oklab value 0xfe7f7f7e .
     * <pre>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GRAY = 0xfe7f7f7e;
    static { NAMED.put("gray", 0xfe7f7f7e); LIST.add(0xfe7f7f7e); }

    /**
     * This color constant "silver" has RGBA8888 code {@code B6B6B6FF}, L 0.69411767, Chro 0.0055242716, CLim 0.1484375, alpha 1.0, hue 0.625, and saturation 0.039437767.
     * It has the encoded Oklab value 0xfe7f7fb1 .
     * <pre>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SILVER = 0xfe7f7fb1;
    static { NAMED.put("silver", 0xfe7f7fb1); LIST.add(0xfe7f7fb1); }

    /**
     * This color constant "white" has RGBA8888 code {@code FFFFFFFF}, L 1.0, Chro 0.0055242716, CLim 0.0078125, alpha 1.0, hue 0.625, and saturation 0.0.
     * It has the encoded Oklab value 0xfe7f7fff .
     * <pre>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFFFF'>&nbsp;@&nbsp;</font><font style='background-color: #FFFFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int WHITE = 0xfe7f7fff;
    static { NAMED.put("white", 0xfe7f7fff); LIST.add(0xfe7f7fff); }

    /**
     * This color constant "red" has RGBA8888 code {@code FF0000FF}, L 0.49019608, Chro 0.25345513, CLim 0.26171875, alpha 1.0, hue 0.07928106, and saturation 1.0021379.
     * It has the encoded Oklab value 0xfe8f9c7d .
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RED = 0xfe8f9c7d;
    static { NAMED.put("red", 0xfe8f9c7d); LIST.add(0xfe8f9c7d); }

    /**
     * This color constant "orange" has RGBA8888 code {@code FF7F00FF}, L 0.627451, Chro 0.17443058, CLim 0.1796875, alpha 1.0, hue 0.15541562, and saturation 1.0188487.
     * It has the encoded Oklab value 0xfe928ca0 .
     * <pre>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ORANGE = 0xfe928ca0;
    static { NAMED.put("orange", 0xfe928ca0); LIST.add(0xfe928ca0); }

    /**
     * This color constant "yellow" has RGBA8888 code {@code FFFF00FF}, L 0.94509804, Chro 0.20529193, CLim 0.21484375, alpha 1.0, hue 0.30886024, and saturation 0.99548733.
     * It has the encoded Oklab value 0xfe9876f1 .
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int YELLOW = 0xfe9876f1;
    static { NAMED.put("yellow", 0xfe9876f1); LIST.add(0xfe9876f1); }

    /**
     * This color constant "green" has RGBA8888 code {@code 00FF00FF}, L 0.7882353, Chro 0.29610303, CLim 0.3046875, alpha 1.0, hue 0.39883053, and saturation 1.0013111.
     * It has the encoded Oklab value 0xfe9661c9 .
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GREEN = 0xfe9661c9;
    static { NAMED.put("green", 0xfe9661c9); LIST.add(0xfe9661c9); }

    /**
     * This color constant "blue" has RGBA8888 code {@code 0000FFFF}, L 0.32156864, Chro 0.31835338, CLim 0.32421875, alpha 1.0, hue 0.7323789, and saturation 1.0100996.
     * It has the encoded Oklab value 0xfe577b52 .
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLUE = 0xfe577b52;
    static { NAMED.put("blue", 0xfe577b52); LIST.add(0xfe577b52); }

    /**
     * This color constant "indigo" has RGBA8888 code {@code 520FE0FF}, L 0.33333334, Chro 0.27001202, CLim 0.2890625, alpha 1.0, hue 0.7895446, and saturation 0.96380764.
     * It has the encoded Oklab value 0xfe5e8855 .
     * <pre>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int INDIGO = 0xfe5e8855;
    static { NAMED.put("indigo", 0xfe5e8855); LIST.add(0xfe5e8855); }

    /**
     * This color constant "violet" has RGBA8888 code {@code 9040EFFF}, L 0.4509804, Chro 0.23984502, CLim 0.27734375, alpha 1.0, hue 0.8342395, and saturation 0.89334947.
     * It has the encoded Oklab value 0xfe658f73 .
     * <pre>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int VIOLET = 0xfe658f73;
    static { NAMED.put("violet", 0xfe658f73); LIST.add(0xfe658f73); }

    /**
     * This color constant "purple" has RGBA8888 code {@code C000FFFF}, L 0.48235294, Chro 0.30940855, CLim 0.3125, alpha 1.0, hue 0.8721605, and saturation 1.019477.
     * It has the encoded Oklab value 0xfe639b7b .
     * <pre>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PURPLE = 0xfe639b7b;
    static { NAMED.put("purple", 0xfe639b7b); LIST.add(0xfe639b7b); }

    /**
     * This color constant "brown" has RGBA8888 code {@code 8F573BFF}, L 0.40392157, Chro 0.07753685, CLim 0.15625, alpha 1.0, hue 0.13634071, and saturation 0.52440196.
     * It has the encoded Oklab value 0xfe878667 .
     * <pre>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BROWN = 0xfe878667;
    static { NAMED.put("brown", 0xfe878667); LIST.add(0xfe878667); }

    /**
     * This color constant "pink" has RGBA8888 code {@code FFA0E0FF}, L 0.74509805, Chro 0.124142565, CLim 0.13671875, alpha 1.0, hue 0.9329364, and saturation 0.966822.
     * It has the encoded Oklab value 0xfe798ebe .
     * <pre>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PINK = 0xfe798ebe;
    static { NAMED.put("pink", 0xfe798ebe); LIST.add(0xfe798ebe); }

    /**
     * This color constant "magenta" has RGBA8888 code {@code F500F5FF}, L 0.5529412, Chro 0.31098264, CLim 0.32421875, alpha 1.0, hue 0.9091779, and saturation 0.98671305.
     * It has the encoded Oklab value 0xfe6aa18d .
     * <pre>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAGENTA = 0xfe6aa18d;
    static { NAMED.put("magenta", 0xfe6aa18d); LIST.add(0xfe6aa18d); }

    /**
     * This color constant "brick" has RGBA8888 code {@code D5524AFF}, L 0.49411765, Chro 0.15199278, CLim 0.2578125, alpha 1.0, hue 0.07195936, and saturation 0.6103554.
     * It has the encoded Oklab value 0xfe88917e .
     * <pre>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRICK = 0xfe88917e;
    static { NAMED.put("brick", 0xfe88917e); LIST.add(0xfe88917e); }

    /**
     * This color constant "ember" has RGBA8888 code {@code F55A32FF}, L 0.54901963, Chro 0.1836353, CLim 0.21484375, alpha 1.0, hue 0.10581406, and saturation 0.8904716.
     * It has the encoded Oklab value 0xfe8e928c .
     * <pre>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int EMBER = 0xfe8e928c;
    static { NAMED.put("ember", 0xfe8e928c); LIST.add(0xfe8e928c); }

    /**
     * This color constant "salmon" has RGBA8888 code {@code FF6262FF}, L 0.58431375, Chro 0.17337766, CLim 0.18359375, alpha 1.0, hue 0.06254672, and saturation 0.99019426.
     * It has the encoded Oklab value 0xfe889495 .
     * <pre>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SALMON = 0xfe889495;
    static { NAMED.put("salmon", 0xfe889495); LIST.add(0xfe889495); }

    /**
     * This color constant "chocolate" has RGBA8888 code {@code 683818FF}, L 0.2901961, Chro 0.0835974, CLim 0.12109375, alpha 1.0, hue 0.14608383, and saturation 0.7408573.
     * It has the encoded Oklab value 0xfe88864a .
     * <pre>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHOCOLATE = 0xfe88864a;
    static { NAMED.put("chocolate", 0xfe88864a); LIST.add(0xfe88864a); }

    /**
     * This color constant "tan" has RGBA8888 code {@code D2B48CFF}, L 0.7058824, Chro 0.052115876, CLim 0.171875, alpha 1.0, hue 0.21390288, and saturation 0.3189044.
     * It has the encoded Oklab value 0xfe8681b4 .
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TAN = 0xfe8681b4;
    static { NAMED.put("tan", 0xfe8681b4); LIST.add(0xfe8681b4); }

    /**
     * This color constant "bronze" has RGBA8888 code {@code CE8E31FF}, L 0.5921569, Chro 0.124142565, CLim 0.15625, alpha 1.0, hue 0.21465261, and saturation 0.8396086.
     * It has the encoded Oklab value 0xfe8f8397 .
     * <pre>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRONZE = 0xfe8f8397;
    static { NAMED.put("bronze", 0xfe8f8397); LIST.add(0xfe8f8397); }

    /**
     * This color constant "cinnamon" has RGBA8888 code {@code D2691DFF}, L 0.52156866, Chro 0.15078327, CLim 0.17578125, alpha 1.0, hue 0.14839543, and saturation 0.9012068.
     * It has the encoded Oklab value 0xfe8f8b85 .
     * <pre>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CINNAMON = 0xfe8f8b85;
    static { NAMED.put("cinnamon", 0xfe8f8b85); LIST.add(0xfe8f8b85); }

    /**
     * This color constant "apricot" has RGBA8888 code {@code FFA828FF}, L 0.7137255, Chro 0.1582875, CLim 0.16015625, alpha 1.0, hue 0.20625468, and saturation 1.04309.
     * It has the encoded Oklab value 0xfe9385b6 .
     * <pre>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int APRICOT = 0xfe9385b6;
    static { NAMED.put("apricot", 0xfe9385b6); LIST.add(0xfe9385b6); }

    /**
     * This color constant "peach" has RGBA8888 code {@code FFBF81FF}, L 0.78431374, Chro 0.096477255, CLim 0.1015625, alpha 1.0, hue 0.19064914, and saturation 1.0331265.
     * It has the encoded Oklab value 0xfe8b84c8 .
     * <pre>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEACH = 0xfe8b84c8;
    static { NAMED.put("peach", 0xfe8b84c8); LIST.add(0xfe8b84c8); }

    /**
     * This color constant "pear" has RGBA8888 code {@code D3E330FF}, L 0.8156863, Chro 0.18692946, CLim 0.203125, alpha 1.0, hue 0.32230157, and saturation 0.9608321.
     * It has the encoded Oklab value 0xfe9575d0 .
     * <pre>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEAR = 0xfe9575d0;
    static { NAMED.put("pear", 0xfe9575d0); LIST.add(0xfe9575d0); }

    /**
     * This color constant "saffron" has RGBA8888 code {@code FFD510FF}, L 0.827451, Chro 0.17789528, CLim 0.1875, alpha 1.0, hue 0.27457327, and saturation 0.99390835.
     * It has the encoded Oklab value 0xfe967cd3 .
     * <pre>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAFFRON = 0xfe967cd3;
    static { NAMED.put("saffron", 0xfe967cd3); LIST.add(0xfe967cd3); }

    /**
     * This color constant "butter" has RGBA8888 code {@code FFF288FF}, L 0.9254902, Chro 0.11653464, CLim 0.1015625, alpha 1.0, hue 0.28769454, and saturation 1.2479109.
     * It has the encoded Oklab value 0xfe8e7cec .
     * <pre>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BUTTER = 0xfe8e7cec;
    static { NAMED.put("butter", 0xfe8e7cec); LIST.add(0xfe8e7cec); }

    /**
     * This color constant "chartreuse" has RGBA8888 code {@code C8FF41FF}, L 0.8901961, Chro 0.2070681, CLim 0.22265625, alpha 1.0, hue 0.34942675, and saturation 0.96758753.
     * It has the encoded Oklab value 0xfe9570e3 .
     * <pre>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHARTREUSE = 0xfe9570e3;
    static { NAMED.put("chartreuse", 0xfe9570e3); LIST.add(0xfe9570e3); }

    /**
     * This color constant "cactus" has RGBA8888 code {@code 30A000FF}, L 0.5058824, Chro 0.20558903, CLim 0.21484375, alpha 1.0, hue 0.39212817, and saturation 0.9969281.
     * It has the encoded Oklab value 0xfe906b81 .
     * <pre>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CACTUS = 0xfe906b81;
    static { NAMED.put("cactus", 0xfe906b81); LIST.add(0xfe906b81); }

    /**
     * This color constant "lime" has RGBA8888 code {@code 93D300FF}, L 0.70980394, Chro 0.20558903, CLim 0.21484375, alpha 1.0, hue 0.3578718, and saturation 0.9969281.
     * It has the encoded Oklab value 0xfe946fb5 .
     * <pre>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIME = 0xfe946fb5;
    static { NAMED.put("lime", 0xfe946fb5); LIST.add(0xfe946fb5); }

    /**
     * This color constant "olive" has RGBA8888 code {@code 818000FF}, L 0.4745098, Chro 0.13131043, CLim 0.140625, alpha 1.0, hue 0.31318712, and saturation 0.9925676.
     * It has the encoded Oklab value 0xfe8f7979 .
     * <pre>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int OLIVE = 0xfe8f7979;
    static { NAMED.put("olive", 0xfe8f7979); LIST.add(0xfe8f7979); }

    /**
     * This color constant "fern" has RGBA8888 code {@code 4E7942FF}, L 0.42352942, Chro 0.0945603, CLim 0.19140625, alpha 1.0, hue 0.3936267, and saturation 0.51707166.
     * It has the encoded Oklab value 0xfe87766c .
     * <pre>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FERN = 0xfe87766c;
    static { NAMED.put("fern", 0xfe87766c); LIST.add(0xfe87766c); }

    /**
     * This color constant "moss" has RGBA8888 code {@code 204608FF}, L 0.25490198, Chro 0.11062346, CLim 0.1328125, alpha 1.0, hue 0.38294512, and saturation 0.88845825.
     * It has the encoded Oklab value 0xfe897541 .
     * <pre>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MOSS = 0xfe897541;
    static { NAMED.put("moss", 0xfe897541); LIST.add(0xfe897541); }

    /**
     * This color constant "celery" has RGBA8888 code {@code 7DFF73FF}, L 0.8392157, Chro 0.20078278, CLim 0.22265625, alpha 1.0, hue 0.39695174, and saturation 0.9382175.
     * It has the encoded Oklab value 0xfe8f6bd6 .
     * <pre>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CELERY = 0xfe8f6bd6;
    static { NAMED.put("celery", 0xfe8f6bd6); LIST.add(0xfe8f6bd6); }

    /**
     * This color constant "sage" has RGBA8888 code {@code ABE3C5FF}, L 0.8117647, Chro 0.069218926, CLim 0.203125, alpha 1.0, hue 0.45448297, and saturation 0.35579073.
     * It has the encoded Oklab value 0xfe8277cf .
     * <pre>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAGE = 0xfe8277cf;
    static { NAMED.put("sage", 0xfe8277cf); LIST.add(0xfe8277cf); }

    /**
     * This color constant "jade" has RGBA8888 code {@code 3FBF3FFF}, L 0.6039216, Chro 0.20259848, CLim 0.25, alpha 1.0, hue 0.4055531, and saturation 0.8398162.
     * It has the encoded Oklab value 0xfe8e6a9a .
     * <pre>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int JADE = 0xfe8e6a9a;
    static { NAMED.put("jade", 0xfe8e6a9a); LIST.add(0xfe8e6a9a); }

    /**
     * This color constant "cyan" has RGBA8888 code {@code 00FFFFFF}, L 0.84705883, Chro 0.1582875, CLim 0.16796875, alpha 1.0, hue 0.54374534, and saturation 0.9922075.
     * It has the encoded Oklab value 0xfe7a6cd8 .
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CYAN = 0xfe7a6cd8;
    static { NAMED.put("cyan", 0xfe7a6cd8); LIST.add(0xfe7a6cd8); }

    /**
     * This color constant "mint" has RGBA8888 code {@code 7FFFD4FF}, L 0.87058824, Chro 0.12265874, CLim 0.1328125, alpha 1.0, hue 0.47453672, and saturation 0.98511803.
     * It has the encoded Oklab value 0xfe8270de .
     * <pre>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MINT = 0xfe8270de;
    static { NAMED.put("mint", 0xfe8270de); LIST.add(0xfe8270de); }

    /**
     * This color constant "teal" has RGBA8888 code {@code 007F7FFF}, L 0.43137255, Chro 0.10141215, CLim 0.109375, alpha 1.0, hue 0.543443, and saturation 1.0024354.
     * It has the encoded Oklab value 0xfe7c736e .
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TEAL = 0xfe7c736e;
    static { NAMED.put("teal", 0xfe7c736e); LIST.add(0xfe7c736e); }

    /**
     * This color constant "turquoise" has RGBA8888 code {@code 2ED6C9FF}, L 0.7058824, Chro 0.1381068, CLim 0.15234375, alpha 1.0, hue 0.52259654, and saturation 0.959297.
     * It has the encoded Oklab value 0xfe7d6eb4 .
     * <pre>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TURQUOISE = 0xfe7d6eb4;
    static { NAMED.put("turquoise", 0xfe7d6eb4); LIST.add(0xfe7d6eb4); }

    /**
     * This color constant "sky" has RGBA8888 code {@code 10C0E0FF}, L 0.64705884, Chro 0.13542919, CLim 0.14453125, alpha 1.0, hue 0.59232193, and saturation 0.99445236.
     * It has the encoded Oklab value 0xfe7671a5 .
     * <pre>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SKY = 0xfe7671a5;
    static { NAMED.put("sky", 0xfe7671a5); LIST.add(0xfe7671a5); }

    /**
     * This color constant "cobalt" has RGBA8888 code {@code 0046ABFF}, L 0.31764707, Chro 0.1792624, CLim 0.1953125, alpha 1.0, hue 0.71857655, and saturation 0.95981544.
     * It has the encoded Oklab value 0xfe697b51 .
     * <pre>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int COBALT = 0xfe697b51;
    static { NAMED.put("cobalt", 0xfe697b51); LIST.add(0xfe697b51); }

    /**
     * This color constant "denim" has RGBA8888 code {@code 3088B8FF}, L 0.4862745, Chro 0.11172148, CLim 0.14453125, alpha 1.0, hue 0.6486837, and saturation 0.82036746.
     * It has the encoded Oklab value 0xfe74777c .
     * <pre>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int DENIM = 0xfe74777c;
    static { NAMED.put("denim", 0xfe74777c); LIST.add(0xfe74777c); }

    /**
     * This color constant "navy" has RGBA8888 code {@code 000080FF}, L 0.18431373, Chro 0.20108652, CLim 0.23046875, alpha 1.0, hue 0.7282781, and saturation 0.9066671.
     * It has the encoded Oklab value 0xfe667c2f .
     * <pre>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int NAVY = 0xfe667c2f;
    static { NAMED.put("navy", 0xfe667c2f); LIST.add(0xfe667c2f); }

    /**
     * This color constant "lavender" has RGBA8888 code {@code B991FFFF}, L 0.6431373, Chro 0.14500555, CLim 0.15234375, alpha 1.0, hue 0.82570946, and saturation 1.0072162.
     * It has the encoded Oklab value 0xfe6f88a4 .
     * <pre>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LAVENDER = 0xfe6f88a4;
    static { NAMED.put("lavender", 0xfe6f88a4); LIST.add(0xfe6f88a4); }

    /**
     * This color constant "plum" has RGBA8888 code {@code BE0DC6FF}, L 0.44313726, Chro 0.26978588, CLim 0.28125, alpha 1.0, hue 0.90448654, and saturation 0.9905147.
     * It has the encoded Oklab value 0xfe6c9c71 .
     * <pre>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PLUM = 0xfe6c9c71;
    static { NAMED.put("plum", 0xfe6c9c71); LIST.add(0xfe6c9c71); }

    /**
     * This color constant "mauve" has RGBA8888 code {@code AB73ABFF}, L 0.5254902, Chro 0.1008085, CLim 0.3203125, alpha 1.0, hue 0.90127134, and saturation 0.32385224.
     * It has the encoded Oklab value 0xfe788a86 .
     * <pre>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAUVE = 0xfe788a86;
    static { NAMED.put("mauve", 0xfe788a86); LIST.add(0xfe788a86); }

    /**
     * This color constant "rose" has RGBA8888 code {@code E61E78FF}, L 0.47843137, Chro 0.23076649, CLim 0.2578125, alpha 1.0, hue 0.99192464, and saturation 0.92668587.
     * It has the encoded Oklab value 0xfe7e9d7a .
     * <pre>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ROSE = 0xfe7e9d7a;
    static { NAMED.put("rose", 0xfe7e9d7a); LIST.add(0xfe7e9d7a); }

    /**
     * This color constant "raspberry" has RGBA8888 code {@code 911437FF}, L 0.3137255, Chro 0.16247371, CLim 0.1875, alpha 1.0, hue 0.02692472, and saturation 0.90774745.
     * It has the encoded Oklab value 0xfe839450 .
     * <pre>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RASPBERRY = 0xfe839450;
    static { NAMED.put("raspberry", 0xfe839450); LIST.add(0xfe839450); }

    /**
     * All names for colors in this palette, in alphabetical order. You can fetch the corresponding packed int color
     * by looking up a name in {@link #NAMED}.
     */
    public static final ObjectList<String> NAMES = NAMED.order();

    static {
        NAMED.setDefaultValue(TRANSPARENT);
        NAMES.sort(null);
    }

    /**
     * All names for colors in this palette, with grayscale first, then sorted by hue from red to yellow to green to
     * blue. You can fetch the corresponding packed int color by looking up a name in {@link #NAMED}.
     */
    public static final ObjectList<String> NAMES_BY_HUE = new ObjectList<>(NAMES);
    /**
     * The packed Oklab int colors that correspond to items in {@link #NAMES_BY_HUE}, with the same order.
     */
    public static final IntList COLORS_BY_HUE = new IntList(NAMES_BY_HUE.size());
    /**
     * All names for colors in this palette, sorted by lightness from black to white. You can fetch the
     * corresponding packed int color by looking up a name in {@link #NAMED}.
     */
    public static final ObjectList<String> NAMES_BY_LIGHTNESS = new ObjectList<>(NAMES);

    /**
     *  Linearly interpolates between fromValue to toValue on progress position.
     * @param fromValue starting float value; can be any finite float
     * @param toValue ending float value; can be any finite float
     * @param progress how far the interpolation should go, between 0 (equal to fromValue) and 1 (equal to toValue)
     */
    public static float lerp (final float fromValue, final float toValue, final float progress) {
        return fromValue + (toValue - fromValue) * progress;
    }
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
     * case it has in this class, it never is given negative inputs.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between 0 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * https://en.wikipedia.org/wiki/Fast_inverse_square_root for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds, posted in his "Stand-alone-junk"
     * repo: https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/master/src/Posts/ballcube.c#L182-L197 .
     * It's worth noting that while hardware instructions for finding the
     * square root of a float have gotten extremely fast, the same is not
     * true for the cube root (which has to allow negative inputs), so while
     * the bit-twiddling inverse square root is no longer a beneficial
     * optimization on current hardware, this does seem to help.
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
     * @param L lightness, from 0 to 1 inclusive
     * @return an adjusted L value that can be used internally
     */
    public static float forwardLight(final float L) {
        return (L - 1f) / (1f - L * 0.4285714f) + 1f;
    }

    /**
     * Changes the curve of the internally-used lightness when it is output to another format. This makes the very-dark
     * area smaller, matching (kind-of) the curve that the standard sRGB lightness uses. This is typically used on "from
     * Oklab" conversions.
     * @param L lightness, from 0 to 1 inclusive
     * @return an adjusted L value that can be fed into a conversion to RGBA or something similar
     */
    private static float reverseLight(final float L) {
        return (L - 1f) / (1f + L * 0.75f) + 1f;
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
     * Gets an encoded Oklab int color given its Oklab channel values: L (lightness), A (green to red), B (blue to
     * yellow), and alpha (same as in RGBA). Lower L is darker, lower A is more green, and lower B is more blue.
     * Checks whether the specified Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness before returning it, or if it is already in-gamut, returns the specified color.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     */
    public static int oklab(float L, float A, float B, float alpha) {
        L = Math.min(Math.max(L, 0f), 1f);
        A = Math.min(Math.max(A, 0f), 1f);
        B = Math.min(Math.max(B, 0f), 1f);
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final float A2 = (A - 0.5f);
        final float B2 = (B - 0.5f);
        final float hue = TrigTools.atan2_(B2, A2);
        final int idx = (int) (L * 255.999f) << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx];
        if(dist * 0x1p-9f >= (float) Math.sqrt(A2 * A2 + B2 * B2))
            return (((int) (alpha * 255) << 24 & 0xFE000000) | ((int) (B * 255) << 16 & 0xFF0000)
                    | ((int) (A * 255) << 8 & 0xFF00) | ((int) (L * 255) & 0xFF));
        return (
                (int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
                        (int) (L * 255.999f));
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
        return BitConversion.intBitsToFloat(r | g << 8 | b << 16 | (packed & 0xfe000000));
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
                Math.min(Math.max((int)(forwardLight(0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
                        | Math.min(Math.max((int)((1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s) * 127.999f + 127.5f), 0), 255) << 8
                        | Math.min(Math.max((int)((0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s) * 127.999f + 127.5f), 0), 255) << 16
                        | (rgba & 0xFE) << 24);
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
                        | ((int)(a * 255f) << 24 & 0xFE000000));
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
     * Gets the alpha channel value of the given encoded color, as an even int ranging from 0 to 254, inclusive. Because
     * of how alpha is stored in libGDX, no odd-number values are possible for alpha.
     * @param encoded a color as a packed int that can be obtained by {@link #oklab(float, float, float, float)}
     * @return an even int from 0 to 254, inclusive, representing the alpha channel value of the given encoded color
     */
    public static int alphaInt(final int encoded)
    {
        return encoded >>> 24 & 0xfe;
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
        return (encoded >>> 24 & 0xfe) / 254f;
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
        final float a = ((oklab >>> 7 & 0x1FE) - 255) / 510f;
        final float b = ((oklab >>> 15 & 0x1FE) - 255) / 510f;
        return (float) Math.sqrt(a * a + b * b);
    }
    /**
     * Gets the saturation of the given Oklab float color, but as Oklab understands saturation rather than how HSL does.
     * Saturation here is a fraction of the chroma limit (see {@link #chromaLimit(float, float)}) for a given hue and
     * lightness, and is between 0 and 1. This gives a float between 0 (inclusive) and 1 (inclusive).
     *
     * @param oklab a packed Oklab float color
     * @return a float between 0 (inclusive) and 1 (inclusive) that represents saturation in the Oklab color space
     */
    public static float saturation(final int oklab) {
        final float A = (((oklab) >>> 8 & 0xff) - 127.5f) / 127.5f;
        final float B = (((oklab) >>> 16 & 0xff) - 127.5f) / 127.5f;
        final float hue = TrigTools.atan2_(B, A);
        final int idx = ((oklab) & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx];
        return dist == 0f ? 0f : (float) Math.sqrt(A * A + B * B) * 256f / dist;
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
        return TrigTools.atan2_(a, b);
    }

    /**
     * Given a hue and lightness, this gets the (approximate) maximum chroma possible for that hue-lightness
     * combination. This is useful to know the bounds of {@link #chroma(int)}. This should be no greater than 0.31613f .
     * The hue and lightness correspond to {@link #hue(int)} and {@link #channelL(int)}, respectively.
     * @param hue the hue, typically between 0.0f and 1.0f, to look up
     * @param lightness the lightness, clamped between 0.0f and 1.0f, to look up
     * @return the maximum possible chroma for the given hue and lightness, between 0.0f and 0.31613f
     */
    public static float chromaLimit(final float hue, final float lightness){
        final int idx = (int) (Math.min(Math.max(lightness, 0f), 1f) * 255.999f) << 8
                | (int) (256f * (hue - ((int)(hue + 0x1p14) - 0x4000)));
        return (GAMUT_DATA[idx] + 2) * 0x1p-8f;
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
        final int L = oklab & 255, other = oklab & 0xFEFFFF00;
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
        final int i = oklab & 255, other = oklab & 0xFEFFFF00;
        return (((int) (i * (1f - change)) & 255) | other);
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
    public static float raiseA(final int start, final float change) {
        final int p = start >>> 8 & 0xFF, other = start & 0xFEFF00FF;
        return BitConversion.intBitsToFloat(((int) (p + (0xFF - p) * change) << 8 & 0xFF00) | other);
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
    public static float lowerA(final int start, final float change) {
        final int p = start >>> 8 & 0xFF, other = start & 0xFEFF00FF;
        return BitConversion.intBitsToFloat(((int) (p * (1f - change)) & 0xFF) << 8 | other);
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
    public static float raiseB(final int start, final float change) {
        final int t = start >>> 16 & 0xFF, other = start & 0xFE00FFFF;
        return BitConversion.intBitsToFloat(((int) (t + (0xFF - t) * change) << 16 & 0xFF0000) | other);
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
     * @return a packed float that represents a color between start and a more artificial color
     */
    public static float lowerB(final int start, final float change) {
        final int t = start >>> 16 & 0xFF, other = start & 0xFE00FFFF;
        return BitConversion.intBitsToFloat(((int) (t * (1f - change)) & 0xFF) << 16 | other);
    }

    /**
     * Interpolates from the packed float color start towards that color made opaque by change. While change should be
     * between 0f (return start as-is) and 1f (return start with full alpha), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors, and
     * is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp towards
     * transparent. This won't change the L, A, or B of the color.
     * @see #fade(int, float) the counterpart method that makes a float color more translucent
     * @param start the starting color as a packed float
     * @param change how much to go from start toward opaque, as a float between 0 and 1; higher means closer to opaque
     * @return a packed float that represents a color between start and its opaque version
     */
    public static float blot(final int start, final float change) {
        final int opacity = start >>> 24 & 0xFE, other = start & 0x00FFFFFF;
        return BitConversion.intBitsToFloat(((int) (opacity + (0xFE - opacity) * change) & 0xFE) << 24 | other);
    }

    /**
     * Interpolates from the packed float color start towards transparent by change. While change should be between 0
     * (return start as-is) and 1f (return the color with 0 alpha), start should be a packed color, as from
     * {@link #oklab(float, float, float, float)}. This is a good way to reduce allocations of temporary Colors,
     * and is a little more efficient and clear than using {@link #lerpColors(int, int, float)} to lerp towards
     * transparent. This won't change the L, A, or B of the color.
     * @see #blot(int, float) the counterpart method that makes a float color more opaque
     * @param start the starting color as a packed float
     * @param change how much to go from start toward transparent, as a float between 0 and 1; higher means closer to transparent
     * @return a packed float that represents a color between start and transparent
     */
    public static float fade(final int start, final float change) {
        final int opacity = start & 0xFE, other = start & 0x00FFFFFF;
        return BitConversion.intBitsToFloat(((int) (opacity * (1f - change)) & 0xFE) << 24 | other);
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
                        (oklab & 0xFE0000FF);
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
                        (oklab & 0xFE0000FF));
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
        float alpha = (oklab >>> 25) / 127f;

        L = Math.min(Math.max(L * mulL + addL, 0f), 1f);
        A = Math.min(Math.max(A * mulA + addA * 2f, -1f), 1f) * 0.5f;
        B = Math.min(Math.max(B * mulB + addB * 2f, -1f), 1f) * 0.5f;
        alpha = Math.min(Math.max(alpha * mulAlpha + addAlpha, 0f), 1f);
        final float hue = TrigTools.atan2_(B, A);
        final int idx = (int) (L * 255.999f) << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx];
        if(dist * dist * 0x1p-16f >= (A * A + B * B))
            return oklab(L, A + 0.5f, B + 0.5f, alpha);
        return (int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
                        (int) (L * 255.999f);
    }


    private static final byte[] GAMUT_DATA;

    static{
        GAMUT_DATA = new StringBuilder().append("hgfeeddded]VQLGD@=:86420/-,+)(''&%$##\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!!\"##$%%&'()*+-.013579<?BFJOTZ`^][YWVTSQPNMLKJIHGFFEEDDDCCCCCDDDEEFGHIJLNPRUROLJGFDBA?>=<;::988776655554444444444555566677889:;;<=>?@ACDFGIKLOQSUX[^adgjmpsvxz||||{zxwusqomkjecbba````aaZTOKGC@=;86421/.-+*)(''&%$$##\"\"!!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"##$$%&&'()*+,-/02457:<?BEIMRX^ecb`^\\ZYWUTSQPONMLKJJIIHHHGGGHHHHIJJKLNOQSVXTPMKHFDBA?>=<;:98876655444333333333333333444555667889:;;<=?@ABDEGIJLNQSUX[]`cfiloqtuwwwwvutrpnmkigfa_^]]\\\\\\\\\\\\]XSNJFC@=;975310/-,+*)(('&%%$$##\"\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&''()*+,-./12468:<?BEHLQV\\bjhgeca_][ZXVUTSQPPONMMLLLKKKLLLLMNOPQRTVX[WSOLJGECA@>=<;:987765544333222211111111122223334445667789:;<=>?@BCEFHJLNPSUXZ]`behjmopqrrrqpomljhgecb\\[ZYYXWWWWWXXVQMIFC@=;975321/.-,+*)(('&&%%$$###\"\"\"\"!!!!!!         !!!!!!\"\"\"\"###$$%%&&'(()*+,-./013568:<?ADHKPTY_fnmkigeca_]\\ZYWVUTSRQQPPPOOOOOPPQRRTUVXZ][VROLIFDBA?><;:9876654433222111000000000000111122233445567889:;<=?@ACDFHJKNPRTWY\\^acfhjklmmmlkjhgedba_^XWVUTTSSRRRRSSTPLHEB@=;9764310/.-,+*))(''&&%%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%&&''(()*++,-.0123578:<?ADGKNSW]cjsqomkigeca_]\\[YXWVUUTSSSSSSSSTTUVWYZ\\^a[VRNKIFDB@?=<:98766544322111000//////////////000011122334556789:;<=>?ABDEGIKMOQSVXZ]_acdfgggggfecba_^\\[YTSRQPOONNNNNNNOOOKHEB@=;98653210..-,+**)((''&&&%%%$$$$$##############$$$$$%%%&&''(())*++,-./0134579;=?ADGJMQV[`gnwusqnljhfdb`_]\\[ZYXWWVVVVVVVWWXYZ\\^`bb\\WROKIFDB@>=;:987654432211000///...............////001122334556789:;<=?@BCEFHJLNPRTVXZ\\^_`abbba`_^]\\ZYXVUPONMLKKJJIIIIIIJKKKGEB@><:8654210/.--,+**))(('''&&&%%%%%%$$$$$$$$$$%%%%%&&&&''(())**+,,-./01234679;=?ACFILPTY^djrzxvsqomjhfdca`^]\\[ZZYYYYYYYZZ[\\]_aced]XSOLIFDB@>=;:9876543221100///...---------------....///00112334566789:;=>?ABDEGIKMOPRTVXYZ[\\\\\\\\\\[ZYXWVTSRQKJJIHGGFFEEEEEEEEFGGGDB@><:87643210/.--,++**))(((''''&&&&&&&&&&&&&&&&&&''''(()))**+,,-.//01245689;=?ACFHLOSW\\agnu|zxvsqoljhfeca`_^]\\\\[[[[[[\\\\]^`acehg`ZUQMJGECA?=<:987654322100//...----,,,,,,,,,,,,,,----...//0011233456789:;<=>@ACDFHIKMNPRSTUVWWWWWVUTSRQPONLGFFEDCCBBAAAA@@@AAABCCDB@><:976543210/.--,,++**)))((((''''''''''''''''(((()))**++,,-../012345789;=?ACEHKNQUZ^djqy~|zwurpnkigfdca`_^^]]]]]]^^_`acegilc\\WROKHFCA?><;987654321100//..---,,,,++++++++++++,,,,,---..//0011233456789:;<>?@BCEFHIKLMOPQQRRRRQQPONMLKJIHCCBA@@??>>====<<<===>>?@A@><;9865432100/..-,,+++***)))))(((((((((((()))))***++,,--.//012345678:;=?ACEGJMPTX\\agmt|\177}zxuspnljhfdcba`__^^^^^__`abdfhknh`ZUQMJGEB@?=;:87654322100/..---,,,++++++********++++++,,,---..//011233456789:;<>?@BCDFGHIJKLMMMMMMLKKJIHGFED@?>==<<;;::9999999999::;;<=><;:8765432100/..--,,,+++*****)))))))))******+++,,---.//0112345679:;=?@BEGILOSVZ_dipw\177~|zwurpmkigedca``_^^^^^__`acdfiknne^XTPLIFDB@><;98765432100/..---,,++++*****************++++,,---..//011234456789:;<>?@ABCDEFGHHHHHHHGGFEEDCBA@<;;:998877766665555566667789:;;:97654332100//..--,,,,+++++++****+++++++,,,---..//01123456789:<=?@BDFIKNQUX]aflrz~|zxurpmkigecba`_^^]]]]^_`abdfhknrlc]WSOLIFDA?><:9865432110//..--,,+++*****))))))))))))*****+++,,,--..//011234456789:;<=>?@ABBCCDDDDDDCCBAA@?>>=987766554443333222222333344556789987654322110///..----,,,,,,,,,,,,,,,,----..//0011233456789:<=?@BDFHKMPSW[_cinu{zxvsqnligeca`^]\\\\[[[[[\\\\]_`bdfimptjb\\WSOKHFCA?=<:9765432100/..--,,+++****))))))))))))))))))****+++,,--..//0112344567899:;<=>>??@@@@@@@??>==<;;:9655433322111000000000000001122334568765543221100///....--------------....///00112234556789;<=?@BDFHJLORUY]afkqtsrpnkifdb_^\\ZYXWVVVVVVWXYZ\\^`cfjmrsib\\WSOKIFCA?=<:9765432100/..--,,++****)))))((((((((((())))))***+++,,--..//01123345667899::;;<<<<<<<<;;;:99887632211000///....----------...///01123457654432211100////..............////001112234456789:;<=?@BCEGILNQTW[_chiihgeca^\\YWUSRPONMMMLMMNNPQSUWZ]`dinsrib\\WSOLIFDA?><:9865432100/..-,,+++***))))((((((((((((((((())))***+++,,--..//001223445567788899999998887766554300///..---,,,,,+++++++++++,,,,--..//01245654433221110000////////////0000111223344566789:;<=?@ACEGIKMPSVY]]\\[ZXVTRPMKIGEDBA@@??>>>??@ACDGIMPUZ_elsrib]XSPLIGDB@><;9865432100/..-,,+++**))))(((((((''''''(((((((())))***++,,,--..//0112233445556666666655544332211..--,,,+++*****)))))))))))****+++,,--./01245544332221111000000000000111122233445567789:;<=?@ACDFHJLOQTUTRQNLJGEC@>=;9876543322111112234579<AIS]gqsjc]XTPMJGEB@>=;:876432210/..--,+++**))))(((((''''''''''''(((((())))***+++,,--..//00011222333333333332221100///,,++****)))((((((((''''((((((()))**++,,-./0134544433222221111111111122223334455667899:;<=?@ACDFHILNPQOMJHEB@=;9754210/.-,++**))))((()))**+,-.14Cjtkd^YUQNKHECA?=<:976543210//.--,,++**))))((((''''''''''''''''((((())))***+++,,,--...//00001111111111000///..--,**)))(((('''''&&&&&&&&&&&&&'''''((())**+,,-./12455444333322222222222333344455667789::;<=>@ABDEGIKMOMJGDB?<97531/.,+*)(('&&%%$$############$$%%&'(rle`[VROLIFDB@><;9875432100/..-,,++***)))(((('''''''''''''''''''((((())))***+++,,,---....//////////....--,,,+++)((('''&&&&&%%%%%%%%%%%%%%%%%&&&&'''(())*++,-./12455544443333333333344445556677889:;<<=>@ABCEFHJLNKGDA=:8531/-+*)('&%$##\"!!!   \037\037\037\037\037\037\036\036\037\037\037\037\037\037   !\"nga\\WTPMJGECA?=;:876543210//.--,,++**)))(((('''''''''''''''''''''((((())))****+++,,,,-------------,,,,+++***))'''&&&%%%%%$$$$$$$$$$$$$$$$$$$%%%%&&'''(()**+,-.013565554444444444444555666778899:;<<=>?ABCDFGIKMIFB>;8520.,*)'&%$#\"!! \037\037\036\036\036\035\035\035\034\034\034\034\034\034\034\033\033\033\034\034\034\034\034\034\035\035\035\036ic]YUQNKHFDA@><;9875432100/..-,,++***)))((((''''''''&&&&&''''''''''((((())))****++++++,,,,,,,,+++++***)))((('&&%%%%$$$$$##################$$$$$%%%&&''(()*+,-./02466655555555555556666778889::;<<=>?@ACDEGHJLJEA=:630.,*('%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033d_ZVSOLIGEB@?=;:876543210//.--,,++**)))((((''''''''&&&&&&&&'''''''''(((((()))))*****************))))(((''''&%%$$$$######\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%%&&'(()**+,-/013666666666666666677778899::;<==>?@ABCEFHIKKFA=9620-+)'&$#\"! \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031XTQNKHFDB@><;9876432210/..--,,++**)))(((('''''''&&&&&&&&&&&'''''''''(((((())))))))))))))))))((((''''&&&%%$$#####\"\"\"\"\"\"\"!!!!!!!!!!!\"\"\"\"\"\"####$$$%%&&''())*+,-/0135777766666677777888999:;;<<=>?@ABCDEGHJLHC>:62/-*(&%#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\031OLIGECA?=<:9865432110/..-,,+++**)))(((('''''''&&&&&&&&&&&&''''''''''(((((((((((((((((((('''''&&&%%%%$$###\"\"\"\"\"\"!!!!!!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&''())*+,-/0246877777777778888999::;;<<=>?@@ABDEFGIKJE@;730-*(&%#\" \037\036\035\035\034\033\033\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\031\031DB@>=;:8765432100/..-,,++***)))((((''''''''&&&&&&&&&&&&&''''''''''''((((((((''''''''&&&&%%%%$$$$#\"\"\"\"\"!!!!!!!!            !!!!!!\"\"\"\"###$$%%&&''()*+,-./12478888888888889999::;;<<=>>?@ABCDEGHIKHB=840-+(&%#\" \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\030\030\031\031\032><;9876543210//.--,,++***)))((((('''''''&&&&&&&&&&&&&&&''''''''''''''''''''&&&&&&%%%%$$$$####\"\"!!!!!!                    !!!!!\"\"\"###$$%%&&'(()*+,-/013588888888889999:::;;<<==>?@@ABCDFGHJKF@:62.,)'%#\" \037\036\035\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032<:9865433210//.--,,+++**))))((((''''''''&&&&&&&&&&&&&&&&&'''''''''&&&&&&&&&%%%%%$$$$#####\"\"!!!!!!                       !!!!!\"\"\"##$$$%%&''()*+,-./1246999999999999:::;;;<<=>>?@AABCDFGHJKD>840-*(%$\"!\037\036\035\034\033\032\032\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\027\027\030\031\031\032\0339765432210//.--,,+++**))))(((((''''''''&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&%%%%%%$$$$####\"\"\"\"\"!!!        \037\037\037\037\037\037\037\037\037\037\037\037       !!!!\"\"\"###$$%%&''()**+-./024689999999999:::;;;<<==>>?@AABCDEGHIIB<72/+)&$#! \036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\031\032\033\034765432110//.--,,+++***)))((((('''''''''&&&&&&&&&&&&&&&&&&&&&&&&&%%%%%%$$$$$#####\"\"\"\"!!!!       \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037      !!!!\"\"\"##$$%%&&'())*+,-/02357999999999::::;;;<<==>>?@@ABCDEFGHG@:51-*(&$\" \037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\026\026\027\027\030\031\031\032\034\03565432110//..-,,,++***))))(((((''''''''''&&&&&&&&&&&&&&&&&&&%%%%%%%$$$$$#####\"\"\"\"\"!!!       \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!!\"\"\"##$$%%&&'())*+,-/01357999999999:::::;;;<<==>>?@@ABCDEFGF?940,)'%#! \036\035\034\033\032\031\031\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\030\030\031\032\033\03565432110//..--,,+++***))))(((((''''''''''&&&&&&&&&&&&&&&&%%%%%%%$$$$$#####\"\"\"\"!!!!!      \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!!\"\"\"##$$%%&&'())*+,-/01357999999999999::::;;;<<==>>??@AABCCD>83/,)&$\"!\037\036\035\034\033\032\031\030\030\027\026\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\030\031\031\032\034\035\0375432110//..--,,+++***))))((((((''''''''''&&&&&&&&&&&&&%%%%%%%$$$$$#####\"\"\"\"!!!!!      \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!!\"\"\"##$$%%&&'())*+,-/01357999988888888999999:::;;;;<<<<;:81.--,-+(&$\" \037\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\027\027\030\031\032\033\034\035\0375432110//..--,,,++****))))((((((''''''''''&&&&&&&&&&%%%%%%%$$$$$#####\"\"\"\"\"!!!!!      \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!!\"\"###$$%%&''()**+,./023578887777777766666666553&%$$$$$$$$%%%&&'((%#! \036\035\034\033\032\031\030\030\027\027\026\025\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\025\025\026\026\027\027\030\031\032\033\034\036 54321100/..---,,+++****))))((((((('''''''''&&&&&&&&&%%%%%%$$$$$#####\"\"\"\"!!!!!       \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037      !!!\"\"\"###$$%&&''()*+,-./023576655444321\037\037\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037   !!\"\"##$%&%#!\037\036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\031\031\032\034\035\037!44321100//..--,,,+++***)))))(((((((''''''''&&&&&&&&%%%%%%$$$$$####\"\"\"\"\"!!!!!       \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037      !!!!\"\"\"##$$%%&&'(()*+,-./122\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"#$%#!\037\036\035\034\033\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\025\025\026\026\027\027\030\031\032\033\034\036 #44322100//..--,,,+++****)))))(((((((''''''''&&&&&&&%%%%%$$$$$####\"\"\"\"\"!!!!!        \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037      !!!!\"\"\"###$#\"! \037\037\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\033\034\034\035\035\036\036\037\037 !!\"#\"!\037\036\035\033\032\032\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\031\032\033\034\035\037!544322100//..---,,,+++****))))))(((((('''''''&&&&&&%%%%%$$$$$#####\"\"\"\"!!!!!!        \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037      !!!!\"\"\"\"\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\036\037  !\"\" \037\036\034\033\032\032\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\033\035\036 #544322110//...--,,,++++****))))))(((((('''''''&&&&&%%%%%$$$$$####\"\"\"\"\"!!!!!          \037\037\037\037\037\037\037\037\037\037        !!!!!\"\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037  !\" \037\036\034\033\032\031\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\027\030\031\032\033\034\036 \"65543221100//..---,,,++++****))))))((((((''''''&&&&&%%%%%$$$$#####\"\"\"\"!!!!!!!                         !!!!!\"! \037\037\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037 !\" \037\036\034\033\032\031\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\032\033\034\036\037!%65543321100//...---,,,++++*****)))))(((((('''''&&&&&%%%%$$$$$####\"\"\"\"\"!!!!!!!                       !!!!!\"! \037\036\035\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\033\033\034\034\035\036\036\037 ! \037\036\034\033\032\031\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\032\033\034\035\037!$765543321100///..---,,,,++++*****)))))((((('''''&&&&&%%%%$$$$$####\"\"\"\"\"!!!!!!!!                   !!!!!\"\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 ! \037\036\034\033\032\032\031\030\027\027\026\026\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\032\033\034\035\037!#'7655433221100//...---,,,,++++*****)))))((((('''''&&&&%%%%$$$$#####\"\"\"\"\"!!!!!!!!!              !!!!!!!\"\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\033\033\034\034\035\036\037\037  \037\036\034\033\032\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\031\032\032\034\035\036 \"&87655433221100///...---,,,,++++*****)))))(((('''''&&&&%%%%$$$$#####\"\"\"\"\"!!!!!!!!!!!!      !!!!!!!!!!\"\"! \037\036\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\036\036\037 !\037\036\035\034\033\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\031\032\034\035\036 \"%9876654432211000//...----,,,,++++*****))))(((((''''&&&&%%%%$$$$#####\"\"\"\"\"\"\"!!!!!!!!!!!!!!!!!!!!!!!\"\"\"\"! \037\036\035\034\034\033\033\032\031\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !\037\036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\025\025\025\026\027\027\030\031\031\032\034\035\036 \"%)9876654433221100///...----,,,,++++****)))))((((''''&&&&%%%%$$$$$####\"\"\"\"\"\"\"\"\"!!!!!!!!!!!!!!!!!!\"\"\"\"\"\" \037\037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !\037\036\035\034\033\032\031\030\030\027\026\026\025\025\025\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\023\024\024\025\025\025\026\027\027\030\031\032\032\034\035\036 \"$(998766544332211000///...----,,,,++++****))))((((''''&&&&%%%%$$$$$#####\"\"\"\"\"\"\"\"\"\"!!!!!!!!!!!\"\"\"\"\"\"\"\"\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\034\035\036\037 !\037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\020\020\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\032\034\035\036 \"$':9987665543322211000///...----,,,+++++****))))((((''''&&&&%%%%$$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"##\"! \037\036\035\035\034\033\033\032\031\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\034\035\036\037 ! \036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\033\034\035\036 \"$',:9987765544332211100///....---,,,,++++****))))((((''''&&&&%%%%$$$$$########\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\034\035\036\037 ! \036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\033\034\035\036 \"$'+;:99877655443322211000///....---,,,,++++****))))(((''''&&&&%%%%$$$$$##########\"\"\"\"\"\"\"\"\"\"\"\"########\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\035\035\036\037 ! \037\035\034\033\032\032\031\030\030\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\024\024\024\025\025\025\026\026\027\030\030\031\032\033\034\035\036 \"$'+<;:998776654433322111000///...----,,,,++++***))))((((''''&&&%%%%%$$$$$##########################$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\031\032\033\033\034\035\035\036\037 ! \037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\033\034\035\036 \"$&*=<;::98776655443322211000////...----,,,,+++****)))((((''''&&&&%%%%$$$$$$$$$##################$$$$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 ! \037\036\035\034\033\032\031\031\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\033\034\035\036 \"$&*>=<;::988766554433322111000///....----,,,++++***))))(((''''&&&&%%%%%$$$$$$$$$$############$$$$$$$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\036\036\037 !!\037\036\035\034\033\032\031\031\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\031\032\033\034\035\037 \"$&*0==<;::9887765544433222111000///....---,,,,+++****)))((((''''&&&&%%%%%$$$$$$$$$$$$$$$$$$$$$$$$$%$#\"! \037\036\035\034\034\033\033\032\031\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037\037 \"!\037\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\024\024\024\024\023\023\023\023\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\032\032\033\034\035\037 \"$'*/>==<;::98877665544333221110000///...----,,,++++***))))(((''''&&&&&%%%%%%%%$$$$$$$$$$$$$$$$$%%%%%#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037 !\"! \036\035\034\033\033\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\022\022\022\023\023\023\023\024\024\024\024\025\025\026\026\027\027\030\030\031\032\033\033\034\036\037 \"$'*/?>==<;::98877665544433222111000////...---,,,,+++***))))((((''''&&&&%%%%%%%%%%%$$$$$$$$$%%%%%%%%%#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\034\035\036\037 !\"! \037\036\035\034\033\032\031\031\030\027\027\026\026\026\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\033\034\035\036\037 \"$'*/@?>==<;::998776655544333222111000///....---,,,+++****)))((((''''&&&&&&%&%%%%%%%%%%%%%%%%%%%%%&&%#\"! \037\036\036\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"\" \037\036\035\034\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\026\026\026\027\027\030\031\031\032\033\034\035\036\037!\"$'*/A@?>==<;;:9988776655444332221110000///...---,,,,+++***))))((((''''&&&&&&&&&%%%%%%%%%%%%%%%&&&&&%$\"! \037\036\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\036\036\037 !\"\" \037\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\034\035\036\037!#%'*.BA@?>==<;;:9988776655544333222111000///....---,,,+++****)))(((('''''&&&&&&&&&&&&&%%%&&&&&&&&&&'%$#! \037\037\036\035\034\034\033\033\032\031\031\031\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\025\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\034\034\035\036\037\037 !#\"!\037\036\035\034\033\033\032\031\031\030\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036 !#%'*.BAA@?>==<;;::9887766655444333222111000///...---,,,++++***))))(((('''''&''&&&&&&&&&&&&&&&&&&&'''%$#\"! \037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\024\024\024\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\034\035\036\037 !\"#\"! \037\035\034\034\033\032\031\031\030\030\027\027\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032\033\033\034\035\036 !#%'*.6BA@@?>==<;;::9988776655544333222111000///...----,,,+++****)))(((((''''''''''&&&&&&&&&&&'''''''%$#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"##! \037\036\035\034\033\032\032\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\034\035\036\037 !#%'*/6CBA@@?>==<;;::99887766655444333222111000///...---,,,+++****))))(((((''''''''''''''''''''''''('&$#\"! \037\036\036\035\034\034\033\032\032\031\031\031\030\030\030\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\032\033\034\034\035\036\036\037 !\"##! \037\036\035\034\033\032\032\031\031\030\030\027\027\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032\032\033\034\035\036\037 \"#%(+/5DCBA@@?>==<<;::99887776655544433222111000///...---,,,,+++****))))((((((((''''''''''''''''(((('&%#\"! \037\036\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\034\035\036\037\037 !\"##\" \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\027\027\027\030\030\031\032\032\033\034\035\036\037 \"$&(+/5EDCBA@@?>>=<<;;:999887766655444333222111000///...---,,,+++****)))))(((((((((((''''''((((((((((&%$\"!  \037\036\035\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#$$\"! \036\035\035\034\033\032\032\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037!\"$&(+/5EEDCBA@@?>>=<<;;::99887776655544433222111000///...---,,,++++****)))))())(((((((((((((((((()))(&%$#\"! \037\036\035\035\034\034\033\032\032\032\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\033\033\034\034\035\036\036\037 !\"#$$\"! \037\036\035\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\033\034\034\035\036 !\"$&(+/5FEDDCBA@@?>>=<<;;::99888776665544433322211100///...---,,,,+++*****)))))))))(((((((((((())))))('%$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#$$#! \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\030\031\032\032\033\034\035\036\037 !#$&),/5GFEDCCBA@@?>>==<;;::999887766655544333222111000///...---,,,++++*****)))))))))))))))))))))))**('&$#\"! \037\037\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\032\032\032\033\033\034\035\035\036\037  !\"#%$#\" \037\036\035\034\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\025\025\025\025\026\026\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037 !#%'),05HGFEDCCBA@@?>>==<<;;::99887776655544433322111000///...---,,,,++++********))))))))))))))))****)'&%#\"!  \037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#$%%#\"!\037\036\035\035\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\037 \"#%'),05IHGFEDCBBA@@?>>==<<;;::99888776665554433322211100///...----,,,+++++***********))))))********+)'&%$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037 !\"#$%%#\"! \037\036\035\034\033\033\032\031\031\030\030\030\027\027\026\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\023\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\026\026\026\026\027\027\030\030\031\031\032\032\033\034\034\035\036\037!\"#%'),05JHHGFEDCBBA@@?>>==<<;;::999887776655544433222111000///...---,,,,+++++*++******************+++)(&%$#\"! \037\036\036\035\035\034\033\033\032\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037\037 !\"#$%%$\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\032\033\034\035\036\037 !\"$%'*-06JIHGFFEDCBBA@@??>==<<;;:::99888776665544433322111000///...----,,,,+++++++++************++++++*('%$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037 !!\"#%&&$#! \037\036\035\035\034\033\032\032\031\031\030\030\030\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\035\036\037 !\"$&(*-06KJIHGFEEDCBBA@@??>>==<<;;::99888776665554433322211000///....---,,,,,+++++++++++++++++++++++,,*('&$#\"!  \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\027\030\030\030\030\031\031\031\031\032\032\033\033\034\034\035\035\036\036\037 !\"#$%&&$#\" \037\036\036\035\034\033\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !#$&(*-16LKJIHGFEEDCBBA@@??>>==<<;;::999887776655544433222111000///...----,,,,,,,,+++++++++++++++,,,,,*)'&%$#\"! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%&&%#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\035\035\036\037 \"#$&(*-16MLKJIHGFEDDCBBA@@??>>==<<;;:::99888776665544433222111000///....----,,,,,,,,,,,,++++,,,,,,,,,,+)'&%$#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\033\034\034\035\036\036\037  !\"#$%'&%#\"! \037\036\035\034\034\033\033\032\031\031\031\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\036\036\037!\"#%&(+.16@LKJIHHGFEDDCBBAA@??>>==<<;;;::99888776665544433322111000////....----,--,,,,,,,,,,,,,,,,,----+)(&%$#\"!  \037\036\036\035\034\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"\"#%&''%$\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\024\024\024\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037 !\"#%')+.27@MLKJIHGGFEDDCBBAA@??>>==<<<;;::999887776655544333222111000///....----------,,,,,,,,,,-------+*('&$#\"!! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\037\037 !\"#$%&''%$#\" \037\037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\033\033\034\034\035\036\037 !\"$%')+.27@NMLKJIHGGFEDDCBBAA@@??>>==<<;;:::99887776655544333222111000////....--.-------------------..-+*('&%$#\"! \037\037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\031\032\032\032\032\033\033\034\034\035\035\036\036\037  !\"#$%&('&$#\"! \037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !#$%'),/27@ONMLKJIHGFFEDDCBBAA@@??>>==<<;;:::998887766655444332221110000////........--------------.....,*)'&%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\027\027\027\027\027\027\027\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\037\037 !\"\"#$%'((&%#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\034\034\035\036\036\037 \"#$&'*,/27@ONMLKJJIHGFFEDDCBBAA@@??>>==<<;;;::998887766655444333222111000////.............--........./.,*)(&%$#\"!! \037\037\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !\"#$%&'((&%$\"! \037\036\036\035\034\034\033\032\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\034\035\036\037 !\"#$&(*,/38@PONMLKJIIHGFFEDDCBBAA@@??>>==<<<;;::9998877666554443332221110000///////.................///.,+)('%$#\"\"! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037  !\"#$%&')('%$#!  \037\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#%&(*-/38@QPONMLKJIIHGFFEDDCCBAA@@??>>===<<;;::99988777665554433322211110000/////////.........////////-+*('&%$#\"!  \037\036\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !!\"#$%&())'%$#\"! \037\036\035\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\025\025\025\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\032\032\032\033\034\034\035\036\036\037 !\"$%'(*-038@RQPONMLKJIHHGFFEDDCCBAA@@??>>>==<<;;:::99887776655544433222211100000/0///////////////////00/-+*)'&%$#\"!! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"##$&'())'&$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\037\037 !#$%')+-049ARQPONMLLKJIHHGFFEDDCCBBA@@???>>==<<;;:::998887766555444333222111100000000/////////////00000/-,*)(&%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\035\035\035\036\037\037  !\"#$%&'(*)(&%#\"! \037\037\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#$%')+-049AQQQPONMLKKJIHHGFFEDDCCBBAA@@??>>==<<;;;::998887766655444333222211110000000000000000000000010.,+)('&$$#\"!  \037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&')**(&%$#!  \037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037 !\"#$&')+.149APPPPPONMLKJJIHGGFFEDDCCBBAA@@??>>==<<<;;::99888776665544433332221111111000000000000000011110.,+)('&%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\033\034\034\035\035\035\036\036\037  !\"##$%&()**('%$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#%&(),.15:AOOOOOONNMLKJJIHGGFFEDDCCBBAA@@??>>==<<<;;::9998877666555444333222211111111100000000011111110.-+*('&%$#\"\"!  \037\036\036\035\035\035\034\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&'()+*('%$#\"! \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\035\035\036\037\037 !\"$%&(*,.15:BOOOOOOONMMLKJIIHGGFFEDDCCBBAA@@??>>===<<;;::999887776655544433332222121111111111111111112221/-+*)'&%$##\"!  \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037  !!\"#$%&'()+*)'&$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\027\027\027\027\026\026\026\027\027\027\027\027\027\027\027\027\027\030\030\030\030\031\031\031\031\032\032\033\033\034\034\035\035\036\037 !\"#$%'(*,/25:BNNNNNNNNNMLLKJIIHGGFFEDDCCBBAA@@??>>===<<;;::99988777665554443333222222221111111111111222221/-,*)('&%$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037  !\"##$%&'(*++)'&%$\"!! \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037 !\"#$%'(*-/26;BMMMMMMMMNNMLKKJIIHGGFFEDDCCBBAA@@??>>>==<<;;::9998877766655544433332222222222222222222222331/.,+)('&%$#\"\"!  \037\037\036\036\035\035\034\034\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\031\031\031\031\032\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037\037 !!\"#$$%&()*,+)(&%$#\"! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$&')+-/26;BLLLLLLMMMMNMLKKJIIHGGFFEDDCCBBAA@@??>>>==<<;;:::998877766655544443333333222222222222222333320.,+*('&%$##\"!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'()*,+*('%$#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037  !\"#%&')+-036;CLLLLLLLLLLMMMLKKJIIHGGFFEDDCCBBAA@@???>>==<<;;:::99887776665554444333333333222222222333333420.-+*)'&%$$#\"!!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"##$%&'()+,,*('&$#\"!  \037\036\036\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\032\032\032\033\033\033\034\034\035\036\036\037 !\"#$%&()+-037<CKKKKKKKKKLLLMMLKJJIHHGGFFEDDCCBBAA@@???>>==<<;;:::9988877666555544443433333333333333333344420/-,*)('&%$#\"\"!  \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"#$$%&'(*+,,*)'&%#\"!! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\036\037 !\"#$%&(*,.037<CJJJJJJJJKKKLLMLLKJJIHHGGFFEDDCCBBAA@@???>>==<<;;:::998887776665554444444433333333333334444431/-,+)('&%$##\"!! \037\037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\033\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$%&'()*+-,+)'&%$#\"! \037\037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\027\027\027\030\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\037\037 !\"#$%'(*,.147<DIIIIIIJJJJKKKLLLLKJJIHHGGFFEDDCCBBAA@@@??>>==<<;;:::99888777666555544444444444444444444445531/.,+*('&%$$#\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\032\032\032\032\032\032\032\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"##$%&'()*,--+)(&%$#\"!  \037\036\036\035\035\034\034\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\032\033\033\034\034\035\035\036\036\037  !\"#$&'(*,.148=DIIIIIIIIIJJJKKLLLLKJJIHHGGFFEEDCCBBAA@@@??>>==<<;;:::99888777666655555554444444444444445555420.-+*)(&&%$#\"\"!  \037\037\037\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037  !!\"#$$%&'()+,--+)('%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037 !\"\"#%&')*,/148=DHHHHHHHHIIIIJJKKLLKKJJIHHGGFFEEDCCBBAA@@@??>>==<<;;:::9998877776665555555554444444445555556420.-+*)('&%$##\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\035\036\036\037\037\037  !\"\"#$%%&'(*+,.-+*('&$#\"\"! \037\037\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\032\033\033\034\034\034\035\035\036\037\037 !\"#$%&')+-/258=EGGGGGGGHHHHIIJJKKLLKKJJIHHGGFFEEDCCBBAA@@@??>>==<<;;;::999888777666656555555555555555555666420/-,*)('&%$$#\"\"!  \037\037\037\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"##$%&'()*+-..,*)'&%$#\"! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037  !\"#$%&()+-/259>EGGGFGGGGGGHHHIIJJKLLKKJIIHHGGFFEDDCCBBAA@@@??>>==<<;;;::99988877776666666555555555555566666531/-,+)('&%%$#\"\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\035\035\035\036\036\036\037\037\037  !\"\"#$$%&'()*,-..,*)'&%$#\"!  \037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\031\032\032\032\033\033\033\034\034\034\035\035\036\036\037 !!\"#$%'(*+-0259>EFFFFFFFFFGGGHHIIJJKLLKKJIIHHGGFFEDDCCBBAA@@???>>==<<;;;::9998887777666666666666566666666677531/.,+*)('&%$##\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%%&'()+,-/.,+)(&%$#\"!! \037\037\036\035\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\030\030\030\030\030\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !\"\"#$&'(*,.0269>EEEEEEEEEFFFGGGHHIJJKLLKKJIIHHGGFFEDDCCBBAA@@???>>==<<;;;::9998888777777666666666666666677775310.-+*)('&%$$#\"\"!!  \037\037\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037\037  !!\"##$%&'')*+,./.-+)('%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !\"#$%&'(*,.036:?EEDDDDEEEEEFFFGGHHIJJKLLKKJIIHHGGFEEDDCCBBAA@@??>>>==<<;;;:::99988877777777766666666677777786420.-,*)('&%%$#\"\"!!  \037\037\037\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037  !!\"\"#$$%&'()*+,.//-+*('&%$#\"!  \037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"#$%&')*,.036:?DDDDDDDDDDEEEFFFGHHIJJKLLKJJIIHHGGFEEDDCCBBAA@@??>>>==<<;;;:::9998888787777777777777777778886420/-,+)('&&%$##\"!!   \037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"#$%%&'()*+-.0/-+*('&%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&()+,.137:@CCCCCCCCCDDDDEEFFGGHIJJKLLKJJIIHHGFFEEDDCCBBAA@@??>>>==<<;;;:::999988888887777777777777888886421/.,+*)('&%$$#\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\037\037\037  !!\"\"##$%&&'()*,-.0/-,*)'&%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !\"\"#$%'()+-/147;@CCBBBBBCCCCCDDEEFFGGHIJKLLLKJJIIHHGFFEEDDCCBBAA@@??>>>==<<;;;:::99998888888888777778888888997531/.,+*)('&%$$##\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"#$$%&'()*+,-/00.,*)(&%$#\"\"!  \037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037  !\"#$%&'(*+-/147;@BBBBBBBBBBBCCCDDEEFGGHIJKLLKKJJIIHGGFFEEDDCCBBAA@@??>>>==<<<;;::::9999998888888888888888999975310.-+*)('&%%$##\"\"!!  \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\034\034\034\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!!\"##$%%&'()*+,./10.,+)('&%$#\"!  \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\031\031\031\031\031\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&'(*+-/248<AAAAAAAAAABBBBCCDDEEFGGHIJKLLKKJJIIHGGFFEEDDCCBBAA@@??>>>==<<<;;;:::999999998888888888999999:75320.-,+)(''&%$$#\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037   !!\"\"##$%%&'()*+-./10.-+)('&%$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037\037 !!\"#$%&')*,-0258<AAA@@@@@AAAAABBBCCDEEFGHHIJKLLKKJJIHHGGFFEEDDCCBBAA@@??>>>==<<<;;;::::9:999999999999999999:::86420/-,+*)('&%$$##\"\"!!  \037\037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037\037  !!!\"\"#$$%&''()*,-.011/-+*('&%$#\"\"!  \037\037\036\036\035\035\034\034\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\034\035\035\036\036\037\037  !\"\"#$%&()*,.0258<@@@@@@@@@@@@AAABBCCDEEFGHIJKLLLKKJJIHHGGFFEEDDCCBBAA@@??>>>==<<<;;;:::::::99999999999999:::::86421/.,+*)('&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037   !!\"\"##$%%&'()*+,-/021/-+*)'&%$##\"!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"##$%'()+,.0259=??????????@@@@AABBCCDDEFGHIJKLLLKKJIIHHGGFFEEDDCCBBAA@@??>>>==<<<;;;;:::::::::9999999::::::;;86421/.-+*)('&&%$$#\"\"!!!   \037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037\037  !!!\"\"##$%%&'()*+,-/021/-,*)(&%$$#\"!! \037\037\036\036\036\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\036\036\036\037\037 !!\"#$%&'()+,.0369=???>>>>??????@@@AABBCDEEFGHIJLMLLKKJIIHHGGFFEEDCCBBAAA@@??>>>==<<<<;;;;;;::::::::::::::::;;;;975310.-,*)(('&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\036\036\035\035\035\035\035\035\036\036\036\036\036\036\036\036\037\037\037\037   !!\"\"##$$%&&'()*+,./121/.,*)('&%$#\"!!  \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'(*+-/1369=>>>>>>>>>>>????@@AABBCDEEFGHJKLMLLKJJIIHHGGFEEDDCCBBAAA@@??>>>===<<<;;;;;;;::::::::::::;;;;;<975310.-,+*)('&%%$##\"\"!!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037   !!!\"\"##$%%&'(()*,-./1320.,+)('&%$#\"\"!  \037\037\036\036\036\035\035\034\034\034\034\033\033\033\033\033\033\033\033\032\032\032\032\032\032\032\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'(*+-/136:>>=========>>>>??@@AABBCDEFGHIJKMMLKKJJIIHHGFFEEDDCCBBAA@@@??>>>===<<<<;;;;;;;;;;::;;;;;;;;;<<:75320/-,+*)('&&%$$##\"\"!!    \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"\"#$$%%&'()*+,-.01320.-+*('&%$##\"!! \037\037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037 !!\"##$%&')*,-/146:==============>>>??@@ABBCDEFGHIJLMMLKKJJIIHGGFFEEDDCCBBAA@@@??>>>===<<<<<<<;;;;;;;;;;;;;;;<<<<:86420/.,+*)(''&%%$##\"\"!!!   \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!!\"\"##$$%&&'()*+,-/02320.-+*)'&%$$#\"!!  \037\037\036\036\035\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\035\036\036\037\037  !!\"#$%&'()*,./247:<<<<<<<<<<<<====>>??@@ABBCDEFGHJKLMLLKKJJIHHGGFFEEDDCCBBAA@@@??>>>====<<<<<<<;;;;;;;;;;<<<<<<=:86421/.-,*))('&%%$$##\"\"!!!   \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037    !!\"\"\"##$%%&''()*+,./02431/-,*)('&%$#\"\"!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$%&'()+,.0247:<<<;;;;;;;<<<<<==>>??@@ABCCDEFHIJLMMLLKKJIIHHGGFFEEDDCCBBAA@@@??>>>>===<=<<<<<<<<<<<<<<<<<<===;864310.-,+*)('&&%$$##\"\"!!!    \037\037\037\037\037\037\037\036\036\036\036\036\036\036\036\037\037\037\037\037\037\037    !!!\"\"##$$%%&'())*+-./12431/-,*)('&%$#\"\"!!  \037\037\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\035\036\036\037\037  !!\"##$%&'()+,.0257;;;;;;;;;;;;;;<<<===>>?@@ABCDEFGHIKLMMLLKKJIIHHGGFFEEDDCCBBAA@@@???>>>======<<<<<<<<<<<<<<=====;975310.-,+*)(''&%%$###\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"\"##$$%&&'()*+,-./12431/.,+)('&%$##\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"##$%&'(*+-.0258;;:::::::::::;;;<<<==>>?@@ABCDEFGIJLMMMLLKJJIIHHGGFEEDDCCCBBAA@@@???>>>=========<<<<<<<======>>;975320/.,+*)(('&%%$$##\"\"!!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"##$$%%&''()*+,-.0134410.,+*('&%$$#\"\"!  \037\037\037\036\036\036\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\036\036\037\037\037  !\"\"#$%%&')*+-/0358:::::99999:::::;;;<<==>>?@@ABCDEGHIKLNMMLKKJJIIHHGFFEEDDCCBBBAA@@@???>>>>>>================>>>><975420/.-+**)('&&%$$##\"\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&'(()*+,-/0235420.-+*)'&%%$#\"\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\033\033\033\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%&'()*,-/1358:9999999999999:::;;;<<=>>?@ABBDEFGHJKMNMLLKKJJIHHGGFFEEDDCCBBBAA@@@???>>>>>>>============>>>>>?<:86421/.-,+*)(''&%%$$##\"\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!!\"\"###$$%&&'()**+-./0235420.-+*)('&%$##\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037  !!\"##$%&'()*,-/13699998888888889999:::;;<<=>>?@ABCDEFHIKLNNMLLKKJIIHHGGFFEEDDCCBBBAA@@@????>?>>>>>>>====>>>>>>>???<:864310.-,+*)(''&%%$$##\"\"\"!!!!      \037\037\037\037\037\037\037\037\037\037      !!!!\"\"\"##$$%%&''()*+,-./1245420/-,*)('&%$##\"\"!  \037\037\037\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\037\037\037  !\"\"#$$%&'()+,./1369888888888888888999::;;<<=>>?@ABCDFGHJLMNMMLLKJJIIHHGGFFEEDDCCBBBAA@@@??????>>>>>>>>>>>>>>>>????=:864310/-,+*)(('&&%%$$##\"\"\"!!!!       \037\037\037\037\037\037        !!!\"\"\"###$$%&&'(()*+,-.01246531/-,*)('&%$$#\"\"!!  \037\037\037\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\036\037\037  !!\"\"#$$%&'()+,.024688877777777777888899::;;<<=>??@ABDEFHIKLNNMMLLKJJIIHHGGFFEEDDCCBBBAA@@@@???????>>>>>>>>>>?????@@=;975320/.,+**)(''&%%$$##\"\"\"!!!!!                   !!!!\"\"\"##$$%%&&'())*+,-/01346531/-,+)('&%%$#\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\037\037\037  !!\"\"#$%&&')*+,.02477777776666677777888999::;<<=>?@ABCDEGHJLMNNMMLKKJJIIHGGFFEEDDDCCBBBAAA@@@?@??????????????????@@@=;975320/.-,+*)(''&%%$$###\"\"\"!!!!!                 !!!!\"\"\"###$$%%&''()**+,./01356531/.,+*)('&%$##\"!!   \037\037\037\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\036\037\037   !!\"##$%&'()*+-.024777666666666666677788899::;<<=>?@ABCEFGIKMONNMLLKKJJIHHGGFFEEDDDCCBBBAAA@@@@@@??????????????@").append("@@@@>;975421/.-,+*)(('&&%%$$###\"\"\"!!!!!              !!!!!\"\"\"\"##$$$%&&''()*+,-./023576310.-+*)('&%$$#\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"#$$%&'()*+-/0257666655555555566667778899::;<==>?@ACDEGHJLNONMMLLKKJIIHHGGFFEEDDCCCBBBAAA@@@@@@@@????????@@@@@@AA><9764210.-,+*))(''&%%$$###\"\"\"\"!!!!!            !!!!!!\"\"\"###$$%%&&'(()*+,-./124576420.-+*)('&%$$#\"\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037   !!\"\"#$$%&'()*,-/13566555555555555555666778899::;<=>?@ABCDFGIKMONNMMLLKJJIIHHGGFFEEDDCCCBBBAAAAAA@@@@@@@@@@@@@@@@AAAA><:864310/-,++*)(''&&%%$$###\"\"\"!!!!!!!        !!!!!!!\"\"\"\"##$$$%%&''())*+,-.0124576420.-,*)('&%%$##\"!!!  \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!!\"##$%%&'()+,-/135555544444444444555566677889::;<=>?@ABDEGHJLNONNMMLKKJJIIHHGGFFEEDDCCCBBBAAAAAAA@@@@@@@@@@@@@@@@@@?<:865310/.-,+*)(('&&%%$$###\"\"\"\"!!!!!!!!!!!!!!!!!!!!\"\"\"\"###$$%%&&''()*++,-/0134686420/-,+)('&&%$##\"\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037\037  !!\"\"##$%&''(*+,./1355444444443444444455556677889:;;<=>?@BCDFHIKMOONNMLLKKJJIHHGGFFFEEDDCCCBBBBAAAAAAAA@@@@@@@?????????=:865320/.-,+*))(''&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!!\"\"\"\"###$$$%%&&'(()*+,-./0134687421/-,+*)('&%$$#\"\"!!   \037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037   !!\"\"#$$%&'()*+,.013444433333333333344445556677889:;<<=?@ABDEGHJLOOONMMLLKJJIIHHGGFFEEEDDCCCBBBBBBAAAAA@@@????>>>>>>>>>=;975321/.-,+**)(''&&%%$$####\"\"\"\"!!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&''())*+,-./0235687531/.,+*)('&%$$##\"\"!!  \037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037\037  !!!\"##$$%&'()*+-.0244433333222222333333444556677899:;<=>?@ACDFHJLNPONNMMLKKJJIIHHGGFFEEEDDCCCCBBBBBBA@@@??>>>>>=========;9754210.-,++*)(('&&%%$$$###\"\"\"\"\"!!!!!!!!!!!!!!\"\"\"\"\"###$$$%%&&''()**+,-./1235787531/.-+*)('&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037\037   !!\"\"##$%%&'()*+-.02433332222222222222333344555677899:;<=>?ABDEGIKMOOONNMLLKKJJIIHHGGFFEEEDDDCCCBCBAA@@??>>>====<<<<<<<<=;9764310/.-,+*)((''&&%%$$####\"\"\"\"\"\"!!!!!!!!!!\"\"\"\"\"\"####$$%%%&&'(()*++,-.012457975310.-+*)('&&%$$#\"\"!!!  \037\037\037\037\036\036\036\036\036\036\036\036\036\035\035\035\036\036\036\036\036\036\036\036\036\037\037\037\037   !!\"\"#$$%&&'()*,-/02332222211111111122223334445567789::;<>?@ACDFHJLNPOONMMLLKKJJIHHHGGFFEEEDDDCCCBA@@??>>===<<<<<;;;;;;<<<:864310/.-,+*))(''&&%%$$$####\"\"\"\"\"\"\"!!!!!!!\"\"\"\"\"\"####$$$%%&&''())*+,-./012467985320.-,*)(''&%$$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037   !!\"\"##$$%&'((*+,-/022222111111111111111222333445567789:;<=>?ABDEGIKMPPONNMMLLKJJIIHHGGGFFEEEDDDCBA@??>>==<<<;;;;;;;;;;;;;;:865320/.-,+**)(('&&%%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$%%%&&''())*+,-./013468986420/-,+*)('&%%$##\"\"!!    \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"##$%%&'()*+,./1222111100000000001111122233445567789:;<=?@ACDFHJLOPOONNMMLKKJJIIHHGGFFFEEEDCBA@?>>==<<;;;;::::::::::::::865321/.-,++*)((''&&%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%&&''(()**+,-./023468:86420/-,+*)('&%%$##\"\"!!!   \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!!\"\"##$%%&'()*+,./12111100000000000000011112233445567899:;=>?ABDEGILNPPOONMMLLKKJJIIHHGGFFFEDCBA@?>==<<;;;:::9999999999999:9754210/.-,+*))(''&&%%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%%&&''(()*++,-./123568:96421/.,+*)('&&%$$##\"\"!!    \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037    !!\"\"##$$%&&'()*+-./11100000///////////0000111223344556789:;<=>@ACEGIKMPPPONNMMLLKKJJIIHHGGFEDCBA@?>=<<;;:::999988888888888899754210/.-,+**)((''&&%%$$$$#####\"\"\"\"\"\"\"\"\"\"\"\"######$$$%%%&&''())*+,-./0124578:97431/.-+*)(''&%$$##\"\"!!!   \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037   !!!\"\"##$$%&''()*,-.011000/////////////////00011122344566789:;<>?ABDFHJLOQPOONNMMLKKJJIIHHGFEDCBA@?>=<<;:::99988887777777777888764310/.-,+**)((''&&%%%$$$######\"\"\"\"\"\"\"\"\"\"######$$$%%%&&''(()**+,-./0124579;975310.-,*)(('&%%$##\"\"\"!!    \037\037\037\037\037\037\037\036\036\036\036\036\036\036\037\037\037\037\037\037\037    !!!\"\"##$%%&'(()+,-.0000////.............////0001122344567789;<=>@ACEGIKNPQPOONNMLLKKJJIIHFEDCBA@?>=<;;::9988877777666666677777864320/.-,++*))(''&&&%%$$$$#######\"\"\"\"\"\"#######$$$$%%%&&''(()**+,-./0134679;975310.-,+*)('&%%$$##\"\"!!!    \037\037\037\037\037\037\037\037\037\036\037\037\037\037\037\037\037\037\037    !!!\"\"##$$%%&'()*+,-/00////......-----......///000112234456789:;<>?ABDFHJMPQPPONNMMLLKKJIHGEDCBA@?>==;::9988777766666665666666667653210.--,+*))((''&&%%%$$$$##################$$$$%%%&&&''())*++,-./0234679;:75320/-,+*)('&&%$$##\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"##$$%&&'()*+,-/0///....-------------....///00112234456789:<=>@ACEGJLOQQPOONNMMLLKJHGEDCBA@?>=<<:998877766655555555555555566653210/.-,+**)((''&&&%%%$$$$################$$$$$%%%&&''(())*+,--./123568:;:85420/-,+*)('&&%$$##\"\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"\"##$$%&&'()*+,.///...------,,,,,,------....//0011223456679:;<>?ABDFIKNQQPPOONNMLLJIGFDCBA@?>=<;;9987776655554444444444444555554210/.-,++*))('''&&%%%$$$$$##############$$$$$%%%&&&''(()**+,-./0123568:<:86420/.,+*)(''&%%$$##\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"###$%%&''()*+-.//...----,,,,,,,,,,,,,----...//0011233456789:;=>@BDFHJMPQQPPOONMMKIHFECBA@?>=<;;:8877665554444433333333334444554310/.-,,+*))((''&&&%%%$$$$$############$$$$$%%%%&&''(())*++,-./0124578:<:86421/.-+*)(('&%%$$##\"\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&'())*,-....---,,,,,,+++++++,,,,,,---..///011233456789;<>?ACEGILORQQPOONMLJHFEDBA@?>=<;::97766555444333332222222333334444320/.--,+**)((''&&&%%%%$$$$$$#########$$$$$$%%%&&&''(())*+,,-./0134579:<;86421/.-,+*)('&&%$$##\"\"\"!!!!     \037\037\037\037\037\037\037\037\037\037\037      !!!\"\"\"##$$%&&'()*+,-...--,,,,+++++++++++++++,,,,--...//01123455789:;=>@BDFHKNQRQPPONLJIGEDCA@?>=<;:99876555443333222222222222222333343210/.-,+**))((''&&&%%%$$$$$$$$####$$$$$$$$%%%%&&'''(()**+,--./0234679;=;964310.-,+*)('&&%%$$##\"\"\"!!!       \037\037\037\037\037\037\037       !!!!\"\"###$%%&&'()*+,-.---,,,++++++*******++++++,,,---..//01123456789;<>?ACEHJMPRQQPOMKIGFDCB@?>=<;:998765544333222211111111111112222333210/.-,++*))((''&&&%%%%$$$$$$$$$$$$$$$$$$%%%%&&&''(())**+,-./01234679;=;975310.-,+*)(''&%%$$##\"\"\"!!!!         \037         !!!!\"\"\"##$$%%&''()*+,---,,,++++***************++++,,,--..//0122345679:;=?@BDGILORRQPNLJHFECB@?>=<;:9887654433322111110000000000011112223210/.-,,+**)(('''&&&%%%%$$$$$$$$$$$$$$$$%%%%&&&'''(())*++,-./01235689;=<975320/-,+*)(('&%%$$###\"\"\"!!!!                  !!!!\"\"\"##$$%%&'(()*+,--,,,+++******)))))))******+++,,,--../00123456789;<>@ADFHKNQRQOMKIGEDBA?>=<;:988765443322111000000///////0000011112210/.--,+**))((''&&&%%%%%$$$$$$$$$$$$$$%%%%%&&&''((()**+,,-./0124568:;=<975320/.,+*))('&&%%$$##\"\"\"!!!!                 !!!!\"\"\"###$$%&&'())*+--,,+++****)))))))))))))))****+++,,--../0012345679:;=?ACEGJMPSPNLJHFDCA@>=<;:98776553322111000////////////////000011120/..-,++*))(('''&&&%%%%%$$$$$$$$$$$$%%%%%&&&'''(())**+,--./0134578:<><:75420/.-,**)('&&%%$$##\"\"\"\"!!!!               !!!!!\"\"\"##$$%%&&'()*+,-,,++****)))))((((((((()))))****++,,--../0112345789;<>@BDFILOQOMJHGECB@?><;:9877654422111000////.............////0001110/.-,++**)(((''&&&&%%%%%%$$$$$$$$$%%%%%&&&&'''(())*++,-../0234578:<><:86421/.-,+*)(''&%%$$###\"\"\"!!!!!             !!!!!\"\"\"###$$%%&''()*+,,,++***))))((((((((((((((())))***++,,--../012345679:<=?ACFHKNPNKIGEDBA?>=;:98776544321100///......---------.....///00010/.-,,+**))(('''&&&%%%%%%%%$$$$%%%%%%%%&&&'''(())**++,-./01234679:<>=:86421/.-,+*)(''&&%$$$##\"\"\"\"!!!!!           !!!!!\"\"\"\"##$$$%&&''()*+,,++**))))(((((('''''''(((((())))**++,,--.//012345689;<>@BEGJMOLJHFDCA@>=<:9876654332100///....--------,--------....//000/.--,++*))(((''&&&&%%%%%%%%%%%%%%%%%%&&&&'''(())**+,,-./01234679;=?=:864310.-,+*)(('&&%%$$###\"\"\"!!!!!!!       !!!!!!!\"\"\"###$$%%&&'(()*+,++**)))(((('''''''''''''''(((()))**++,,--./001235679:<>?BDFILNKIGECB@?=<;:877654332100//...----,,,,,,,,,,,,,,,----..///0/..-,++**))(('''&&&&%%%%%%%%%%%%%%%%&&&&'''((())*++,--./01235689;=?=;864310/-,+*))('&&%%$$###\"\"\"\"!!!!!!!!   !!!!!!!!\"\"\"\"###$$%%&&'())*++***))((((''''''&&&&&&&''''''(((())***+,,--./012345689;=?ACEHKLJHFDBA?><;:98765432211//...---,,,,,+++++++++++,,,,,---..//0/.-,,+**))((''''&&&&%%%%%%%%%%%%%%&&&&&'''(())**++,-../01245689;=?=;975310/.,+**)(''&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!!\"\"\"\"##$$$%%&''()**+**)))(((''''&&&&&&&&&&&&&&&''''((()))**+,,-../01234679:<>@BEGJKIGECA?>=;:987654322100/..--,,,,+++++++*****++++++,,,,---..//.-,,+**))((('''&&&&&%%%%%%%%%%%%&&&&&'''((())**+,,-./01234578:;=?>;975320/.-,+*)(''&&%%$$###\"\"\"\"!!!!!!!!!!!!!!!!!\"\"\"\"###$$%%&&''()*+**)))((''''&&&&&&%%%%%%%&&&&&&''''((())**+,,-./002345689;=?ADFIJHFDB@>=<:987654322100/.--,,,++++****************++++,,,--../.--,++**))((''''&&&&&%%%%%%%%%%&&&&&''''(()))*++,,-./01234578:<>@>;975320/.-,+*)(('&&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&'(()*+*))((('''&&&&%%%%%%%%%%%%%%%&&&&'''((())**+,,-./01234679:<>@CEHIGDCA?=<;98765432110//.--,,+++*****))))))))))))*****+++,,---...-,++**))((('''&&&&&&&%%%%%%&&&&&&&'''((())**++,--./01234679:<>@><975421/.-,+*))(''&%%$$$###\"\"\"\"\"!!!!!!!!!!!!!\"\"\"\"\"###$$$%%&&'(()**))(('''&&&&%%%%%%%$$$$$%%%%%%%&&&&'''(())*++,--./0124568:;=@BDGHECA@><;:8765432110//..,,+++****)))))))((((()))))))***+++,,--..-,,+**)))((''''&&&&&&&&&&&&&&&&&&''''((())**+,,-../01235679:<>@><:764210.-,+*))(''&&%%$$####\"\"\"\"\"!!!!!!!!!!!\"\"\"\"\"####$$%%&&''())*))(('''&&&%%%%%$$$$$$$$$$$$$$%%%%&&&'''(())*++,-./01234679;=?ADFFDB@?=;:9765432110//.--,++***))))(((((((((((((((())))***++,,--.-,,++**))(((''''&&&&&&&&&&&&&&&&''''((())**++,,-.//01245689;<>A?<:864310/-,+**)(''&&%%$$$###\"\"\"\"\"\"!!!!!!!!!\"\"\"\"\"\"###$$$%%&&''()*))((''&&&%%%%$$$$$$$$$##$$$$$$$$%%%%&&&''(())*++,-./0124578:<>@CEECA?><;9875432110/..--,+***)))(((((''''''''''''((((()))***++,,---,++**)))(((''''&&&&&&&&&&&&&&'''''((())**++,--./012345689;=?A?<:864310/.,,+*)(('&&%%$$$####\"\"\"\"\"\"\"!!!!!\"\"\"\"\"\"\"####$$$%%&&'(()*)((''&&&%%%$$$$$#############$$$$$%%%&&&''(()**+,--./1234679;=?BEDB@>=;:876532210/..-,,+**)))((((''''''''''''''''''(((()))**++,,--,,+**)))((('''''&&&&&&&&&&&&'''''((()))**++,--./01234578:;=?A?=:865310/.-,+*)((''&&%%$$$###\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$%%%&''(())((''&&%%%$$$$###################$$$$%%%&&''(()**+,-./0124578:<?ADCA?=<:976543210/..-,,++)))(((''''&&&&&&&&&&&&&&&'''''((())**++,,-,,++**))(((('''''&&&&&&&&&&'''''(((())**++,,-../01234578:;=?A?=:865320/.-,+*))(''&&%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%&&''()))(''&&%%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%%&&''(()*++,-./023468:<>@CB@><;986543210/..-,,+**)((('''&&&&&&%%%%%%%%%%&&&&&&'''((())**+,,-,++**)))((('''''''&&&&&&'''''''((()))**++,,-.//01234678:<>?B@=;975320/.-,+*))(''&&%%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%&&''())(''&&%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%&&''())*+,-./0124579;=?BA?=;:87543210/.--,++**)(('''&&&&%%%%%%%%%%%%%%%%%%&&&&'''(())**++,,,+***))((((''''''''''''''''''(((()))**++,--./001235679:<>@B@=;975421/.-,+**)(('&&%%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%%&&'(()(''&&%%$$$###\"\"\"\"\"\"!!!!!!!!!!!\"\"\"\"\"\"###$$$%%&&''())*+,-./023568:<>A@><:97653210/.--,++**))'''&&&%%%%%$$$$$$$$$$$$$$%%%%%&&&''((()**++,,++**)))((((''''''''''''''''(((()))**++,,--./012345679:<>@B@=;9754210.-,++*)((''&&%%$$$$#####\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%&&''(((('&&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&''()**+,-.0124579;=@?=;98654210/..-,++*))(('&&&%%%%$$$$$$$######$$$$$$$%%%%&&&''(())*++,++***)))((((''''''''''''''((((()))**++,,-../012345689;<>@B@>;9764210/.-,+*))(''&&%%%$$$######\"\"\"\"\"\"\"\"\"######$$$%%%&&''((('&&%%$$###\"\"\"\"!!!!!!         !!!!!!\"\"\"\"###$$%%&&'(()*+,-./023568:=?><:8754320//.-,++*))(('&&%%%$$$$$################$$$$%%%%&&''(())*+,,++**)))(((((''''''''''''((((()))***++,--.//012345789;=?ACA><9864310/.-,+*))(''&&%%%$$$$######\"\"\"\"\"\"\"######$$$$%%%&&''(('&&%%$$###\"\"\"!!!!!                !!!!\"\"\"\"##$$%%&&'(()*+,-.012468:<>=;97643210/.-,+**))('''%%%$$$$######\"\"\"\"\"\"\"\"\"\"######$$$%%%&&''(())*+,++**))))(((((''''''''''(((((()))**++,,--./001234678:;=?ACA><:864310/.-,+**)((''&&%%%$$$#########\"#########$$$$%%&&''((''&%%$$##\"\"\"!!!!        \037\037\037\037\037\037       !!!!\"\"\"##$$%%&&'()**+-./023579;=<:8654210/.-,+**)((''&&%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%%&&''())*+++***)))((((((''''''''(((((()))***++,,-../011235678:;=?ACA><:865320/.-,+**)((''&&%%%$$$$#################$$$$%%%&&''(('&%%$$##\"\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&''()*+,-.013468:=;9764310/.-,+**)((''&&%$$####\"\"\"\"\"!!!!!!!!!!!!!\"\"\"\"\"\"###$$$%%&&''())*+++**))))(((((((('''((((((())))**+++,,-../012345679:<=?ACA?<:865321/.-,++*))(''&&&%%$$$$$################$$$$%%%&&''('&&%$$##\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"##$$%%&'(()*+,./023579<:865321/.-,++*)(('&&%%%$###\"\"\"\"!!!!!!!!!  !!!!!!!!!\"\"\"\"###$$%%&&''()**++***))))((((((((((((((((()))***++,,--.//012345679:<>@BDB?<:8753210.-,,+*))((''&&%%%$$$$###############$$$$%%%&&&'''&&%$$##\"\"!!!    \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037   !!!\"\"##$$%&&'()*+,-.013468;9754210/-,++*)(('&&%%$$##\"\"\"!!!!!                !!!!\"\"\"\"##$$$%%&''()**++**))))(((((((((((((((())))***++,,--.//012345689;<>@BDB?=;9754210/.-,+*))((''&&%%%$$$$$#############$$$$$%%%&&''''&%$$##\"\"!!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037    !!\"\"##$$%&&'()*+,./02468:864310/.-,+*)(('&&%%$$#\"\"\"!!!!       \037\037\037\037\037\037\037\037       !!!\"\"\"###$$%%&''()*++***))))(((((((((((((())))***+++,,-../0012345789;<>@BDB?=;9754210/.-,+**)((''&&&%%%$$$$$###########$$$$$%%%&&&'''&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037\037   !!\"\"##$$%&'(()+,-.01357975321/.-,+*)(('&&%$$$##\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&''()*+****))))(((((((((((()))))***++,,--../0112346789;=>@BEB@=;9754310/.-,+**))(''&&&%%%$$$$$$#########$$$$$$%%%&&&''&&%$##\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037   !!\"\"##$%%&'()*+,./1246864210.-,+*)(('&&%$$##\"\"!!    \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037    !!!\"\"##$$%%&'(()*+***))))))((((((((()))))***+++,,--.//012345678:;=?@CEB@=;9764310/.-,++*))((''&&%%%%$$$$$$$######$$$$$$%%%%&&'''&%$$#\"\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\035\034\034\034\034\034\034\034\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"##$%&&'()*,-.013575320/.,+*)(('&%%$$##\"\"!    \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"\"#$$%%&'())*****))))))(((((())))))****+++,,-..//012345678:;=?ACEC@>;9864320/.-,,+*))((''&&&%%%%$$$$$$$$$$$$$$$$$$%%%&&&''&%$$#\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\037\037\037  !!\"\"#$$%&'()*+,./1346421/.-,+*)('&%%$$##\"\"!!  \037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037\037  !!!\"\"##$%%&'()*+***))))))))))))))))))***+++,,--../0012345679:<=?ACEC@><:865321/.--,+**)((''&&&%%%%$$$$$$$$$$$$$$$$$%%%%&&&'&&%$##\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037  !!\"\"#$$%&'()*,-.0245310.-,+*)('&%%$$#\"\"!!! \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037   !!\"\"##$%%&'()*****))))))))))))))))****+++,,--../0112345689:<>?ACFC@><:8653210/.-,+**))('''&&&%%%%$$$$$$$$$$$$$$$%%%%&&&''&%$##\"!!  \037\037\037\036\036\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%&&')*+,./13420/.,+*)('&&%$##\"\"!!   \037\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"##$%&&'()*****))))))))))))))****+++,,,--.//0122345789;<>@ADFCA><:8653210/.-,++*))((''&&&%%%%%$$$$$$$$$$$$$$%%%%&&&'&%$$#\"\"!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !!\"##$%&'()*,-/0231/.-+*)('&&%$##\"\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037  !!\"\"##$%&'()******))))))))))))*****+++,,--..//0123456789;<>@BDFDA><:8754210/.-,++*))(('''&&&%%%%$$$$$$$$$$$$$%%%%%&&&'&%$#\"\"!  \037\037\036\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"#$$%&'(*+,.0120/-,+*(''&%$##\"\"!!  \037\037\036\036\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\035\035\035\036\036\036\037\037\037  !\"\"#$$%&'()******))))))))))*****+++,,,--../0012345678:;=>@BDFDA?<:9754210/.-,,+**)(('''&&&%%%%%$$$$$$$$$$$%%%%%&&&'&%$##\"!  \037\037\036\036\036\035\035\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'()*,-/11/.,+*)('&%$##\"!!  \037\037\037\036\036\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$$%&'()******))))))))******+++,,,--.//0112345678:;=>@BDGDA?=;9754310/.-,,+**))((''&&&&%%%%%$$$$$$$$$%%%%%%&&&&%$$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'(*+,.00.-,*)('&%$##\"!!  \037\037\036\036\036\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"\"#$%%&')********))))*******++++,,--..//0112345679:;=?@BEGDB?=;9764320/.--,+**))(('''&&&%%%%%%%$$$$$%%%%%%%&&&&&%$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$$%'()*,-//-,+)('&%$$#\"!!  \037\037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'()******************+++,,,--..//0122345689:<=?ACEGEB?=;97643210/.-,++*))(('''&&&&%%%%%%%%%%%%%%%%%&&&&&%$#\"\"!  \037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&'(*+-..-+*)'&%%$#\"!!  \037\037\036\036\035\035\035\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037  !\"\"#$%&'(*****************++++,,---../00123445789:<=?ACEGEB@=;98653210/.-,++**)(((''&&&&%%%%%%%%%%%%%%%%%&&&&&%$#\"!  \037\036\036\035\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !\"\"#$%&()*,.-,*)('&%$#\"!!  \037\036\036\035\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\033\034\034\035\035\036\036\037\037  !\"##$%&()***************++++,,,--..//00123456789;<>?ACEHEB@=;98653210/.-,,+**))(('''&&&&%%%%%%%%%%%%%%%&&&&&%$#\"!! \037\037\036\035\035\034\034\034\033\033\033\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037  !\"#$%&'(*+-,+)('&%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"#$%&'()+************+++++,,,--..//01123456789;<>@ACFHEB@><:8654210/.-,,+**))(('''&&&&&%%%%%%%%%%%%%%&&&&&$##\"! \037\037\036\036\035\034\034\034\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037 !!\"#$%&()*,+*)'&%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\033\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\032\033\033\034\034\034\035\035\036\036\037  !\"#$%&')*+**********+++++,,,---../00122345678:;<>@BDFHFC@><:8754210/.--,++*))((('''&&&&%%%%%%%%%%%%%&&&&&%$#\"!  \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\037\037 !\"##%&'(*++)('&$##\"! \037\037\036\036\035\035\034\034\033\033\033\032\032\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037 !!\"#$%&()++********++++++,,,--..//00123345679:;=>@BDFHFC@><:8754310/..-,++**))(('''&&&&&%%%%%%%%%%%&&&&&&$#\"!! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\033\033\033\034\034\035\035\036\037\037 !\"#$%&()+*('&%$#\"!  \037\036\036\035\035\034\034\033\033\032\032\032\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%&'(*+++****+++++++,,,---..//00123445689:;=>@BDFIFCA><:8754320//.-,++**))((''''&&&&&%%%%%%%%%&&&&&&%$#\"! \037\036\036\035\035\034\034\033\033\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037  !\"#$&'(*)(&%$#\"!  \037\036\036\035\034\034\033\033\033\032\032\032\031\031\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%&()+++++++++++++,,,,---..//01123456789:<=?@BDFIFCA><:97643210/.-,,+**))((('''&&&&&&%%%%%%%&&&&&&&$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037 !\"#$%&()('%$#\"!  \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037\037 !\"#$%'(*++++++++++++,,,,--..//001123456789:<=?ABEGIFDA?=;97643210/.-,,+**))(((''''&&&&&&&&&&&&&&&&&&%$#\"! \037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\032\032\032\033\033\034\035\035\036\037\037 !\"#$&'('&%$#\"! \037\036\036\035\034\034\033\033\032\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\032\033\034\034\035\035\036\037  !\"$%&')+++++++++++,,,,---..//001223456789;<=?ACEGIGDA?=;97653210/.--,++**))((''''&&&&&&&&&&&&&&&&&&$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\032\032\032\033\033\034\035\035\036\037 !\"#$%&(&%$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\034\034\035\035\036\037 !\"#$%'(*+++++++++,,,,,--...//001233456789;<>?ACEGJGDA?=;98653210/.--,++**))(((''''&&&&&&&&&&&&&&&&%$\"! \037\037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\034\034\035\036\036\037 !\"#$&'&$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\034\034\035\036\036\037 !\"#$&')++++++++,,,,,---...//01123445678:;<>@ACEGJGDB?=;98654210/..-,++**))(((''''&&&&&&&&&&&&&&&&$#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\024\024\024\024\024\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\031\032\032\033\034\034\035\036\037\037 !\"$%&%$\"! \037\037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\036\037 !\"$%&(*++++++,,,,,----..//001123455689:;=>@ACEHJGDB?=;:8654310//.-,,+**)))(((''''&&&&&&&&&&&&&&%$\"! \037\036\036\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\031\031\031\032\033\033\034\034\035\036\037 !\"#$%$#\"! \037\036\035\034\034\033\033\032\031\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !\"#$&')++++,,,,,,,---...//001223456789:;=>@BDFHJHEB@><:87543210/.-,,++**))((('''''&&&&&&&&&&&'&$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"$$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !\"#%&(*,,,,,,,,,----..///011223456789:<=>@BDFHKHEB@><:87543210/.--,++**))(((('''''&&&&&&&&&''%$\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\036\037 !\"#$\"! \037\036\035\035\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\035\036\037 !#$&')+,,,,,,,,---...//0011233456789:<=?@BDFHKHEB@><:87543210/.--,++**)))(((''''''&&&&&&&''&$#\" \037\036\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\026\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !\"#\" \037\036\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\025\025\025\025\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\036\037 !\"#%&(*,,,,,,,----...//0011234456789;<=?ABDFIKHEC@><:97643210/..-,,+**)))(((('''''''&&&&'''%$\"! \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\035\036\037 \"\"! \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037 !\"$%')+,,,,,----...///001223455678:;<>?ACDGIKHEC@><:97653210/..-,,++**))(((('''''''''''''&$#\" \037\036\035\034\034\033\032\032\031\030\030\027\027\027\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037 !! \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\027\030\031\031\032\033\033\034\035\036\037 \"#%&(*,,,,-----...//0011223456789:;<>?ACEGIKIFCA><;97653210//.-,,++**)))((((''''''''''''%$\"! \037\036\035\034\033\032\032\031\030\030\027\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037  \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\026\026\026\027\027\030\031\031\032\033\034\035\035\037 !\"$%'),,,-----....//0011233456789:;=>?ACEGILIFCA?=;986542100/.--,++**)))(((('''''''''''&%#\" \037\036\035\034\033\033\032\031\031\030\027\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\034\035\036\037  \037\035\035\034\033\032\031\031\030\027\027\026\026\026\025\025\024\024\024\024\023\023\023\023\022\022\022\021\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\031\032\033\034\035\036\037 \"#%&(+-------...///0011233456789:;=>@ACEGIKIFCA?=;986543210/.--,++***)))((((''''''''''&$\"! \037\035\035\034\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036\037\037\036\035\034\033\032\031\031\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\023\024\024\025\025\025\026\026\027\027\030\031\032\032\033\034\035\036\037!\"$&(*,-----....//00012234456789:<=>@BCEGJIGEDA?=;986543210/.--,,++**)))((((('''''''''%#\" \037\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\033\033\034\035\036\036\035\034\033\032\031\031\030\027\027\026\026\025\025\025\024\024\023\023\023\023\022\022\022\022\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\030\031\032\033\033\034\035\037 !#%')+-----...///00112234556789:<=?@BDFHIGEDBA?=;:87543210/..-,,++**)))(((((''''''''&$\"! \036\035\034\033\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\020\020\020\020\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\031\032\033\034\035\036\035\034\033\032\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\033\034\035\036\037!\"$&(*----....///00112334566789;<=?@BDFHGECB@?><;:87543210/..-,,++***)))((((('''''''%#\" \037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\031\031\032\033\034\035\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\033\034\035\036 !#%'),--....///00011233456789:;<=?@BDFGECB@?=<;:987643210//.--,++***)))(((((('''''&$#! \036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\030\031\032\033\034\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\017\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\022\022\022\023\023\024\024\025\025\026\026\027\030\031\031\032\033\034\036\037 \"$&(+-.....///00112234456789:;<>?ABDFECA@>=<:988765532100/.--,,++**))))((((((('''%#\" \037\036\035\033\033\032\031\030\027\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\030\031\032\033\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\025\026\026\027\030\031\032\033\034\035\036 !#%'*,.....///00112234456789:;<>?ACDFCA@>=;:9876554332110/.--,,++***)))((((((((('%#! \036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\024\024\024\025\025\026\027\027\030\031\031\032\033\032\031\031\030\027\026\026\025\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\020\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\023\023\024\024\025\025\026\027\027\030\031\032\033\034\035\037 \"$&)+....///000112234556789:;=>?ACEDB@><;:87655433211100/..-,,++***))))((((((((&$\" \037\036\034\033\032\031\031\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\024\024\024\025\025\026\027\027\030\031\032\033\032\031\030\027\026\026\025\024\024\023\023\023\022\022\021\021\021\020\020\020\020\020\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\025\026\027\027\030\031\032\033\035\036\037!#%(*-..////001122334556789:<=>@ACDB@><;9876543221100///...-,,+++**))))((((((('%#! \036\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\023\022\022\022\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\022\023\023\023\024\024\025\026\026\027\030\030\031\032\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\022\023\023\024\025\025\026\027\030\031\032\033\034\035\037 \"$'),..///0001122334566789;<=>@ACB@><:98654332100//...------,,++***))))((((((&$\" \037\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\023\024\024\025\026\026\027\030\031\031\030\027\027\026\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\022\023\023\024\025\025\026\027\030\031\032\033\034\036\037!#%(+.////000112234456789:;<=?@BB@><:9765432100//..--,,,,,++++++***)))))(((((%#! \036\035\034\032\031\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\023\023\023\024\025\025\026\026\027\030\030\030\027\026\025\024\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\020\020\020\021\021\021\022\022\023\023\024\025\025\026\027\030\031\032\034\035\036 \"$'*-////001112334456789:;<=?@B@><:876432100/..--,,,+++************))))(((('%\"!\037\035\034\033\032\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\025\026\027\027\030\027\026\025\025\024\023\023\022\022\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\025\026\026\027\030\031\033\034\036\037!#&),///0001122334556789:;<>?@A><:87543210/..-,,+++***))))))))))))))))))(((&$\" \036\035\033\032\031\030\027\027\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\024\025\025\026\027\027\026\025\025\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\024\024\025\026\027\030\031\032\033\035\036 \"%(+///0001122334556789:;<>?A?<:8754210/..-,,++**)))((((((((((((((())))))(%#!\037\035\034\033\032\031\030\027\026\025\025\024\023\023\022\022\022\021\021\020\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\024\025\025\026\026\025\025\024\023\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\024\024\025\026\027\030\031\032\034\035\037!$&*-/00011122344566789:;=>??<:8653210/.-,,+**)))(((''''''&&&&''''''((())'$\" \036\035\033\032\031\030\027\026\025\025\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\024\025\026\026\025\024\023\023\022\021\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\022\023\024\024\025\026\027\030\032\033\035\036 #%(,/00011223344567789:<=>?=:865321/.-,,+**))(('''&&&&&%%%%%%%%&&&&&'''(&#!\037\035\034\033\031\030\027\026\026\025\024\024\023\022\022\021\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\024\025\025\024\023\023\022\021\021\020\020\020\017\017\016\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\022\022\023\024\025\025\026\030\031\032\034\035\037\"$'+/00011223344567889;<=>=;865320/.-,+**)((''&&&%%%%%$$$$$$$$$$%%%%%&&'%\" \036\035\033\032\031\030\027\026\025\024\024\023\022\022\021\021\021\020\020\017\017\017\017\016\016\016\016\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\023\024\025\024\023\023\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\022\022\023\024\025\026\027\030\031\033\034\036!#&).0011122334556789:;<=>;965310/-,++*)((''&&%%%$$$$############$$$$%%%$!\037\036\034\033\031\030\027\026\025\025\024\023\023\022\021\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\017\020\020\021\021\022\022\023\023\024\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\017\017\017\020\021\021\022\022\023\024\025\026\027\030\032\034\035\037\"%(,0011222344556789:;<=<975310.-,+*)(('&&%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$#!\037\035\033\032\031\027\026\026\025\024\023\023\022\021\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\020\020\021\022\023\023\024\025\026\030\031\033\034\036!$'+/111223344566789:;<<97531/.-,*))('&&%%$$###\"\"\"\"!!!!!!!!!!!!!!!\"\"\"\"#\" \036\034\032\031\030\027\026\025\024\023\023\022\022\021\020\020\020\017\017\017\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\022\023\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\016\016\016\017\017\020\020\021\022\023\024\024\026\027\030\032\033\035 \"&).111223344567789:;<:7531/.,+*)('&&%$$##\"\"\"!!!!                !!!!\"!\037\035\033\032\030\027\026\025\024\024\023\022\022\021\020\020\020\017\017\016\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\017\020\020\021\022\023\024\025\026\027\031\032\034\037!$(,112223345567789:;;8531/-,+*)('&%$$##\"\"!!!   \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     \036\034\032\031\030\027\026\025\024\023\022\022\021\021\020\020\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\021\021\020\017\017\016\016\016\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\f\r\r\016\016\017\017\020\021\021\022\023\024\025\026\030\031\033\035 #'+012233445567889:<8531/-,*)('&%$$##\"!!!  \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\035\033\032\030\027\026\025\024\023\022\022\021\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\016\016\017\017\020\021\021\022\023\024\025\027\030\032\034\037\"%).1223344556789:;9631/-,*)('&%$#\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\034\034\034\035\035\035\035\035\035\035\035\036\036\036\036\034\032\031\027\026\025\024\023\023\022\021\021\020\020\017\017\016\016\r\r\r\r\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\f\r\r\r\016\016\016\017\017\020\020\020\017\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\013\f\f\r\r\r\016\017\017\020\021\021\022\023\025\026\027\031\033\036 $(-2223344566789::741/-+*)'&%$##\"!!  \037\037\036\036\035\035\035\035\034\034\034\034\034\034\034\034\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\033\032\030\027\026\025\024\023\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\017\017\020\017\017\016\016\r\r\f\f\f\013\013\013\n\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\r\r\016\017\017\020\021\022\023\024\025\026\030\032\034\037\"&+1223344566789:741/-+*('&%$#\"!! \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\032\032\032\032\032\032\032\033\033\033\033\033\033\033\034\034\034\032\031\027\026\025\024\023\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\017\017\017\016\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\n\013\013\013\f\f\r\r\016\017\017\020\021\022\023\024\026\027\031\033\036!%)/233445567789852/-+)('%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\031\031\031\031\031\031\031\032\032\032\032\032\032\032\033\033\033\031\030\026\025\024\023\022\022\021\020\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\016\r\r\f\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\f\f\r\r\016\016\017\020\021\022\023\025\026\030\032\035 #(.23344556788952/-+)(&%$#\"! \037\037\036\035\035\034\034\034\033\033\033\032\032\032\031\031\031\031\031\031\031\031\030\030\030\030\030\030\030\031\031\031\031\031\031\031\031\032\032\031\027\026\025\023\023\022\021\020\020\017\016\016\016\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\f\f\f\f\r\r\016\016\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\n\n\n\013\013\f\f\r\r\016\016\017\020\021\022\024\025\027\031\033\036\"&,23344566789630-+)'&%#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\030\026\025\024\023\022\021\020\020\017\016\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\f\f\f\r\r\r\r\f\f\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\n\n\n\013\013\013\f\f\r\016\017\017\020\021\023\024\026\030\032\035 $*1334456678840-+)'&$#\"! \037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\027\025\024\023\022\021\020\020\017\016\016\r\r\r\f\f\f\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\f\f\f\r\r\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\n\n\n\013\013\f\f\r\016\017\017\020\022\023\025\026\031\033\037#(/34455667851.+)'%$\"! \037\036\036\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\026\025\023\022\021\021\020\017\017\016\r\r\r\f\f\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\013\f\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\b\b\b\b\b\t\t\t\n\n\013\013\f\f\r\016\017\020\021\022\023\025\027\032\035!&-3445567762.+)'%$\"! \037\036\035\034\033\033\032\032\031\031\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\024\024\024\024\024\024\025\025\025\025\025\025\025\025\025\026\025\024\023\022\021\020\017\017\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\013\013\n\n\t\t\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\b\b\b\t\t\t\n\n\n\013\f\f\r\016\017\020\021\022\024\026\031\034\037$*344556773/,)'%#\" \037\036\035\034\034\033\032\032\031\030\030\027\027\027\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\023\023\023\023\024\024\024\024\024\024\024\024\024\024\025\024\023\022\021\020\017\017\016\r\r\f\f\f\013\013\013\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\013\013\013\013\n\n\t\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\t\n\n\013\013\f\r\016\017\020\021\023\025\027\032\036\"(14456674/,)'%#! \037\036\035\034\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\022\021\020\017\017\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\n\n\n\n\n\n\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\n\n\013\013\f\r\016\017\020\022\024\026\030\034 &.4556660,)'%#! \036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\022\020\020\017\016\r\r\f\f\013\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\t\t\t\t\n\n\n\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\007\b\b\t\t\t\n\013\013\f\r\016\017\021\022\024\027\032\036$,455662-*'$\"!\037\036\035\033\033\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\021\020\017\016\r\r\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\t\t\b\b\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\006\006\006\006\006\007\007\007\b\b\b\t\t\n\n\013\f\r\016\017\021\023\025\030\034\")45563.*'$\" \037\035\034\033\032\031\030\027\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\020\017\016\r\r\f\f\013\013\n\n\n\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\b\b\b\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\006\006\006\007\007\007\b\b\b\t\n\n\013\f\r\016\020\021\024\027\032\037&1566/+'$\" \036\035\033\032\031\030\030\027\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\017\016\r\r\f\f\013\013\n\n\t\t\t\t\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\b\b\007\007\007\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\006\006\006\006\007\007\007\b\b\t\t\n\013\f\r\016\020\022\025\030\035$.561+'$\" \036\034\033\032\031\030\027\026\025\025\024\024\023\023\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\016\016\r\f\f\013\013\n\n\t\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\b\b\007\007\007\006\006\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\005\005\005\005\005\006\006\006\007\007\007\b\t\t\n\013\f\r\017\021\023\026\033!+53,($\"\037\035\034\032\031\030\027\026\025\025\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\r\f\013\013\n\n\n\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\005\005\005\005\006\006\006\007\007\b\b\t\n\013\f\r\017\021\024\030\036'5.($!\037\035\033\032\030\027\026\025\025\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\f\013\013\n\n\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\007\007\006\006\006\005\005\005\005\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\005\005\005\006\006\006\007\007\b\t\t\013\f\r\017\022\026\034$0)%!\037\034\033\031\030\027\026\025\024\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\013\013\n\n\t\t\b\b\b\b\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\005\005\005\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\004\004\004\004\004\005\005\005\006\006\006\007\b\b\t\n\f\016\020\024\031!*%!\036\034\032\030\027\026\025\024\023\022\022\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\013\013\013\n\n\t\t\b\b\b\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\006\006\005\005\005\005\004\004\004\004\004\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\004\004\004\004\005\005\005\006\006\007\b\t\n\f\016\021\026\035%!\036\033\031\030\026\025\024\023\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\b\b\b\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\004\004\004\004\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\004\004\004\005\005\005\006\007\b\t\n\f\017\023\031!\035\033\031\027\025\024\023\022\021\021\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\007\007\007\007\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\004\004\004\004\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\004\004\004\005\005\006\007\b\n\f\020\025\035\032\030\026\024\023\022\021\020\020\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\b\b\007\007\007\006\006\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\003\003\003\004\004\005\005\006\b\t\f\021\031\027\025\024\022\021\020\017\017\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\007\007\007\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\003\003\003\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\003\003\004\004\005\007\t\r\025\024\022\021\020\017\016\r\r\f\f\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\006\006\006\006\005\005\005\005\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\002\002\002\003\004\005\b\016\021\020\017\016\r\f\f\013\013\n\n\t\t\t\t\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\002\002\002\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\002\003\005\017\r\r\f\013\013\n\n\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\013\n\n\t\t\b\b\b\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\b\007\007\007\006\006\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\005\005\005\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toString().getBytes(StandardCharsets.ISO_8859_1);

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
     * {@link TrigTools#cos_(float)} on the hue to almost get A, and {@link TrigTools#sin_(float)} to almost get B),
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
        final float A = ((packed >>> 8 & 0xff) - 127.5f) / 255f;
        final float B = ((packed >>> 16 & 0xff) - 127.5f) / 255f;
        final float g = GAMUT_DATA[(packed & 0xff) << 8 | (int)(256f * TrigTools.atan2_(B, A))];
        return g * g * 0x1p-16 + 4.0 >= (A * A + B * B);
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
        float A2 = (A - 127.5f) / 255f;
        float B2 = (B - 127.5f) / 255f;
        final float g = GAMUT_DATA[(L << 8 & 0xFF00) | (int)(256f * TrigTools.atan2_(B2, A2))];
        return g * g * 0x1p-16 + 4.0 >= (A2 * A2 + B2 * B2);
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
        final byte g = GAMUT_DATA[((int)(L * 255.999f) << 8 & 0xFF00) | (int)(256f * TrigTools.atan2_(B, A))];
        return g * g * 0x1p-16 + 4.0 >= (A * A + B * B);
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
        final float hue = TrigTools.atan2_(B, A);
        final int idx = (packed & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx];
        return (
                (packed & 0xFF0000FF) |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16);
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
        final float hue = TrigTools.atan2_(B2, A2);
        final int idx = L << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx];
        return (
                alpha << 24 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
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
        final float hue = TrigTools.atan2_(B2, A2);
        final int idx = (int) (L * 255.999f) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx];
        return BitConversion.intBitsToFloat(
                (int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
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
        final float hue = TrigTools.atan2_(B, A);
        final int idx = (packed & 0xff) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx];
        if (dist * dist >= (A * A + B * B))
            return packed;
        return (
                (packed & 0xFF0000FF) |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16);
    }

    /**
     * Checks whether the given Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness, or if it is already in-gamut, returns the color as-is. This always produces
     * an opaque color.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int, int, int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(int L, int A, int B) {
        return limitToGamut(L, A, B, 255);
    }

    /**
     * Checks whether the given Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness, or if it is already in-gamut, returns the color as-is.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(int, int, int) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(int L, int A, int B, int alpha) {
        L = Math.min(Math.max(L, 0), 255);
        A = Math.min(Math.max(A, 0), 255);
        B = Math.min(Math.max(B, 0), 255);
        alpha = Math.min(Math.max(alpha, 0), 255);
        final float A2 = (A - 127.5f);
        final float B2 = (B - 127.5f);
        final float hue = TrigTools.atan2_(B2, A2);
        final int idx = L << 8 | (int)(256f * hue);
        final float dist = GAMUT_DATA[idx];
        if(dist * dist >= (A2 * A2 + B2 * B2))
            return (L | A << 8 | B << 16 | alpha << 24);
        return (
                alpha << 24 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
                        L);
    }
    /**
     * Checks whether the specified Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness and returns it, or if it is already in-gamut, returns the color as-specified.
     * This always produces an opaque color.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(float, float, float) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(float L, float A, float B) {
        return oklab(L, A, B, 1f);
    }

    /**
     * Checks whether the specified Oklab color is in-gamut; if it isn't in-gamut, brings the color just inside
     * the gamut at the same lightness and returns it, or if it is already in-gamut, returns the color as-specified.
     * <br>
     * This is equivalent to {@link #oklab(float, float, float, float)}.
     * @param L lightness component; will be clamped between 0 and 1 if it isn't already
     * @param A green-to-red chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param B blue-to-yellow chromatic component; will be clamped between 0 and 1 if it isn't already
     * @param alpha alpha component; will be clamped between 0 and 1 if it isn't already
     * @return the first color this finds that is in-gamut, as if it was moving toward a grayscale color with the same L
     * @see #inGamut(float, float, float) You can use inGamut() if you just want to check whether a color is in-gamut.
     */
    public static int limitToGamut(float L, float A, float B, float alpha) {
        return oklab(L, A, B, alpha);
    }

    /**
     * A different way to specify an Oklab color, using hue, saturation, lightness, and alpha like a normal HSL(A) color
     * but calculating them directly in the Oklab color space. This is efficient, but for colors with less than 0.5f
     * lightness, it can be very unpredictable in how it handles saturation. Most colors between 0.5 and 0.75 hue that
     * also have less than 0.5 lightness are extremely desaturated and close to gray, regardless of what you give for
     * saturation, and these colors suddenly jump to very saturated around 0.75 hue and higher. To avoid this issue, you
     * may prefer using {@link #oklabByHCL(float, float, float, float)}, which takes an absolute chroma as opposed to
     * the saturation here (which is a fraction of the maximum chroma).
     * <br>
     * You can use {@link #hue(int)} , {@link #saturation(int)}, and {@link #channelL(int)} to get the hue, saturation,
     * and lightness values from an existing color that this will understand ({@link #alpha(int)} too).
     * @param hue between 0 and 1, usually, but this will automatically wrap if too high or too low
     * @param saturation will be clamped between 0 and 1
     * @param lightness will be clamped between 0 and 1
     * @param alpha will be clamped between 0 and 1
     * @return a packed Oklab int color that tries to match the requested hue, saturation, and lightness
     */
    public static int oklabByHSL(float hue, float saturation, float lightness, float alpha) {
        lightness = Math.min(Math.max(lightness, 0f), 1f);
        saturation = Math.min(Math.max(saturation, 0f), 1f);
        hue -= (int)(hue + 0x1p14) - 0x4000; // subtracts floor
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * saturation * 0.5f;
        return ((int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
                        (int) (lightness * 255.999f));
    }

    /**
     * A different way to specify an Oklab color, using hue, chroma, lightness, and alpha something like a normal HSL(A)
     * color but calculating them directly in the Oklab color space. This has you specify the desired chroma directly,
     * as obtainable with {@link #chroma(int)}, rather than the saturation, which is a fraction of the maximum chroma
     * (saturation is what {@link #oklabByHSL(float, float, float, float)} uses). If you use this to get two colors with
     * the same chroma and lightness, but different hue, then the resulting colors should have similar colorfulness
     * unless one or both chroma values exceeded the gamut limit (you can get this limit with
     * {@link #chromaLimit(float, float)}). If a chroma value given is greater than the chroma limit, this clamps chroma
     * to that limit. You can use {@link #hue(int)}, {@link #chroma(int)}, and {@link #channelL(int)} to get the hue,
     * chroma, and lightness values from an existing color that this will understand ({@link #alpha(int)} too).
     * @param hue between 0 and 1, usually, but this will automatically wrap if too high or too low
     * @param chroma will be clamped between 0 and the maximum chroma possible for the given hue and lightness
     * @param lightness will be clamped between 0 and 1
     * @param alpha will be clamped between 0 and 1
     * @return a packed Oklab int color that tries to match the requested hue, chroma, and lightness
     */
    public static int oklabByHCL(float hue, float chroma, float lightness, float alpha) {
        lightness = Math.min(Math.max(lightness, 0f), 1f);
        chroma = Math.max(chroma, 0f);
        hue -= (int)(hue + 0x1p14) - 0x4000; // subtracts floor
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = Math.min(chroma * 127.5f, GAMUT_DATA[idx]);
        return ((int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 127.999f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 127.999f) << 8 |
                        (int) (lightness * 255.999f));
    }

    /**
     * Interpolates from the packed int color start towards end by change. Both start and end should be packed Oklab
     * ints, and change can be between 0f (keep start) and 1f (only use end). This is a good way to reduce allocations
     * of temporary Colors.
     *
     * @param s      the starting color as a packed int
     * @param e      the end/target color as a packed int
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return a packed Oklab int that represents a color between start and end
     */
    public static int lerpColors(final int s, final int e, final float change) {
        final int
                sL = (s & 0xFF), sA = (s >>> 8) & 0xFF, sB = (s >>> 16) & 0xFF, sAlpha = s >>> 24 & 0xFE,
                eL = (e & 0xFF), eA = (e >>> 8) & 0xFF, eB = (e >>> 16) & 0xFF, eAlpha = e >>> 24 & 0xFE;
        return (((int) (sL + change * (eL - sL)) & 0xFF)
                | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | (((int) (sAlpha + change * (eAlpha - sAlpha)) & 0xFE) << 24));
    }

    /**
     * Interpolates from the packed Oklab int color start towards the Oklan int color end by change, but keeps the alpha
     * of start and uses the alpha of end as an extra factor that can affect how much to change. Both start and end
     * should be packed Oklab int colors, as from {@link #describeOklab(CharSequence)} or any of the color constants
     * here, and change can be between 0f (keep start) and 1f (only use end). This is a good way to reduce allocations
     * of temporary Colors. You will probably want to convert the color for rendering with {@link #toRGBA8888(int)}.
     * @param start the starting color as a packed Oklab int; alpha will be preserved
     * @param end the target color as a packed Oklab int; alpha will not be used directly, and will instead be multiplied with change
     * @param change how much to go from start toward end, as a float between 0 and 1; higher means closer to end
     * @return a packed float that represents a color between start and end
     */
    public static int lerpColorsBlended(final int start, final int end, float change) {
        final int
                sL = (start & 0xFF), sA = (start >>> 8) & 0xFF, sB = (start >>> 16) & 0xFF, sAlpha = start & 0xFE000000,
                eL = (end & 0xFF), eA = (end >>> 8) & 0xFF, eB = (end >>> 16) & 0xFF, eAlpha = end >>> 25;
        change *= eAlpha * 0.007874016f;
        return (((int) (sL + change * (eL - sL)) & 0xFF)
                | (((int) (sA + change * (eA - sA)) & 0xFF) << 8)
                | (((int) (sB + change * (eB - sB)) & 0xFF) << 16)
                | sAlpha);
    }

    /**
     * Given several colors, this gets an even mix of all colors in equal measure.
     * If {@code colors} is null or has no items, this returns {@link #TRANSPARENT}.
     *
     * @param colors an array or varargs of packed int colors; all should use the same color space
     * @param offset the index of the first item in {@code colors} to use
     * @param size   how many items from {@code colors} to use
     * @return an even mix of all colors given, as a packed int color
     */
    public static int mix(int[] colors, int offset, int size) {
        if (colors == null || colors.length < offset + size || offset < 0 || size <= 0)
            return TRANSPARENT;
        int result = colors[offset];
        for (int i = offset + 1, o = offset + size, denom = 2; i < o; i++, denom++) {
            result = lerpColors(result, colors[i], 1f / denom);
        }
        return result;
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
    private static final Matcher wordMatcher = Pattern.compile("[a-z]+").matcher();

    /**
     * Parses a color description and returns the approximate color it describes, as a packed RGBA8888 int color.
     * Color descriptions consist of one or more lower-case words, separated by non-alphabetical characters (typically
     * spaces and/or hyphens). Any word that is the name of a color in this palette will be looked up in
     * {@link #NAMED} and tracked; if there is more than one of these color name words, the colors will be mixed using
     * {@link #mix(int[], int, int)}, or if there is just one color name word, then the corresponding color
     * will be used. The special adjectives "light" and "dark" change the intensity of the described color; likewise,
     * "rich" and "dull" change the saturation (the difference of the chromatic channels from grayscale). All of these
     * adjectives can have "-er" or "-est" appended to make their effect twice or three times as strong. If a color name
     * or adjective is invalid, it is considered the same as adding the color {@link #TRANSPARENT}.
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * and "lightest richer apricot-olive".
     * <br>
     * This method will check the first character of description and may change how it parses depending on that char.
     * If the first char is {@code #}, and there are 6 characters remaining, this parses those 6 characters as a hex
     * color in RGB888 format. If the first char is {@code #} and there are 8 or more characters remaining, it parses
     * 8 of those characters as an RGBA8888 hex color. If the first char is {@code |}, that char is ignored and the rest
     * of the CharSequence is treated as a color description (this is to ease parsing markup for
     * {@link #processColorMarkup(CharSequence)}). Otherwise, the whole CharSequence is parsed as a color description,
     * and the result is converted to an RGBA int.
     *
     * @param description a color description, as a lower-case String matching the above format, or a {@code #}-prefixed hex color
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
     * Color descriptions consist of one or more lower-case words, separated by non-alphabetical characters (typically
     * spaces and/or hyphens). Any word that is the name of a color in this palette will be looked up in
     * {@link #NAMED} and tracked; if there is more than one of these color name words, the colors will be mixed using
     * {@link #mix(int[], int, int)}, or if there is just one color name word, then the corresponding color
     * will be used. The special adjectives "light" and "dark" change the intensity of the described color; likewise,
     * "rich" and "dull" change the saturation (the difference of the chromatic channels from grayscale). All of these
     * adjectives can have "-er" or "-est" appended to make their effect twice or three times as strong. Technically,
     * the chars appended to an adjective don't matter, only their count, so "lightaa" is the same as "lighter" and
     * "richcat" is the same as "richest". There's an unofficial fourth level as well, used when any 4 characters are
     * appended to an adjective (as in "darkmost"); it has four times the effect of the original adjective. If a color
     * name or adjective is invalid, it is considered the same as adding the color {@link #TRANSPARENT}.
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * and "lightest richer apricot-olive".
     * <br>
     * This always considers its input a color description, won't parse hex colors, and always uses the full text.
     *
     * @param description a color description, as a lower-case String matching the above format
     * @return a packed Oklab int color as described
     */
    public static int describeOklab(final CharSequence description) {
        return describeOklab(description, 0, description.length());
    }
    /**
     * Parses a color description and returns the approximate color it describes, as a packed Oklab int color.
     * Color descriptions consist of one or more lower-case words, separated by non-alphabetical characters (typically
     * spaces and/or hyphens). Any word that is the name of a color in this palette will be looked up in
     * {@link #NAMED} and tracked; if there is more than one of these color name words, the colors will be mixed using
     * {@link #mix(int[], int, int)}, or if there is just one color name word, then the corresponding color
     * will be used. The special adjectives "light" and "dark" change the intensity of the described color; likewise,
     * "rich" and "dull" change the saturation (the difference of the chromatic channels from grayscale). All of these
     * adjectives can have "-er" or "-est" appended to make their effect twice or three times as strong. Technically,
     * the chars appended to an adjective don't matter, only their count, so "lightaa" is the same as "lighter" and
     * "richcat" is the same as "richest". There's an unofficial fourth level as well, used when any 4 characters are
     * appended to an adjective (as in "darkmost"); it has four times the effect of the original adjective. If a color
     * name or adjective is invalid, it is considered the same as adding the color {@link #TRANSPARENT}.
     * <br>
     * Examples of valid descriptions include "blue", "dark green", "duller red", "peach pink", "indigo purple mauve",
     * and "lightest richer apricot-olive".
     * <br>
     * This overload always considers its input a color description, and won't parse hex colors.
     *
     * @param description a color description, as a lower-case String matching the above format
     * @param start the first character of the description to read from
     * @param length how much of description to attempt to parse
     * @return a packed Oklab int color as described
     */
    public static int describeOklab(final CharSequence description, int start, int length) {
        float intensity = 0f, saturation = 0f;
        wordMatcher.setTarget(description, start, length);
        final List<String> terms = wordMatcher.foundStrings();
        mixing.clear();
        for (int i = 0; i < terms.size(); i++) {
            String term = terms.get(i);
            if (term == null || term.isEmpty()) continue;
            final int len = term.length();
            switch (term.charAt(0)) {
                case 'l':
                    if (len > 2 && term.charAt(2) == 'g') {
                        switch (len) {
                            case 9:
                                intensity += 0.125f;
                            case 8:
                                intensity += 0.125f;
                            case 7:
                                intensity += 0.125f;
                            case 5:
                                intensity += 0.125f;
                                break;
                            default:
                                mixing.add(TRANSPARENT);
                                break;
                        }
                    } else {
                        mixing.add(NAMED.getOrDefault(term, 0));
                    }
                    break;
                case 'r':
                    if (len > 1 && term.charAt(1) == 'i') {
                        switch (len) {
                            case 8:
                                saturation += 0.2f;
                            case 7:
                                saturation += 0.2f;
                            case 6:
                                saturation += 0.2f;
                            case 4:
                                saturation += 0.2f;
                                break;
                            default:
                                mixing.add(TRANSPARENT);
                                break;
                        }
                    } else {
                        mixing.add(NAMED.getOrDefault(term, 0));
                    }
                    break;
                case 'd':
                    if (len > 1 && term.charAt(1) == 'a') {
                        switch (len) {
                            case 8:
                                intensity -= 0.15f;
                            case 7:
                                intensity -= 0.15f;
                            case 6:
                                intensity -= 0.15f;
                            case 4:
                                intensity -= 0.15f;
                                break;
                            default:
                                mixing.add(TRANSPARENT);
                                break;
                        }
                    } else if (len > 1 && term.charAt(1) == 'u') {
                        switch (len) {
                            case 8:
                                saturation -= 0.2f;
                            case 7:
                                saturation -= 0.2f;
                            case 6:
                                saturation -= 0.2f;
                            case 4:
                                saturation -= 0.2f;
                                break;
                            default:
                                mixing.add(TRANSPARENT);
                                break;
                        }
                    } else {
                        mixing.add(NAMED.getOrDefault(term, 0));
                    }
                    break;
                default:
                    mixing.add(NAMED.getOrDefault(term, 0));
                    break;
            }
        }
        if(mixing.size() == 0) return 0;
        int result = mix(mixing.items, 0, mixing.size());
        if(result == 0) return 0;

        if (intensity > 0) result = lighten(result, intensity);
        else if (intensity < 0) result = darken(result, -intensity);

        if (saturation > 0) result = enrich(result, saturation);
        else if (saturation < 0) result = limitToGamut(dullen(result, -saturation));
        else result = limitToGamut(result);

        return result;
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
    @Nonnull
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
}