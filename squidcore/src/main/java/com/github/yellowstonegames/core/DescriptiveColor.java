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
     * This color constant "transparent" has RGBA8888 code {@code 00000000}, L 0.0, A 0.49803922, B 0.49803922, alpha 0.0, hue 0.6666667, and saturation 0.0.
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
     * This color constant "black" has RGBA8888 code {@code 000000FF}, L 0.0, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.6666667, and saturation 0.0.
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
     * This color constant "gray" has RGBA8888 code {@code 808080FF}, L 0.6313726, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.54913926, and saturation 0.023115247.
     * It has the encoded Oklab value 0xfe7f7fa1 .
     * <pre>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #808080'>&nbsp;@&nbsp;</font><font style='background-color: #808080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #808080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #808080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GRAY = 0xfe7f7fa1;
    static { NAMED.put("gray", 0xfe7f7fa1); LIST.add(0xfe7f7fa1); }

    /**
     * This color constant "silver" has RGBA8888 code {@code B6B6B6FF}, L 0.8, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.5489059, and saturation 0.025921047.
     * It has the encoded Oklab value 0xfe7f7fcc .
     * <pre>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B6B6B6'>&nbsp;@&nbsp;</font><font style='background-color: #B6B6B6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B6B6B6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B6B6B6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SILVER = 0xfe7f7fcc;
    static { NAMED.put("silver", 0xfe7f7fcc); LIST.add(0xfe7f7fcc); }

    /**
     * This color constant "white" has RGBA8888 code {@code FFFFFFFF}, L 1.0, A 0.49803922, B 0.49803922, alpha 1.0, hue 0.5, and saturation 0.0.
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
     * This color constant "red" has RGBA8888 code {@code FF0000FF}, L 0.627451, A 0.6117647, B 0.56078434, alpha 1.0, hue 0.9965429, and saturation 0.94052595.
     * It has the encoded Oklab value 0xfe8f9ca0 .
     * <pre>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF0000'>&nbsp;@&nbsp;</font><font style='background-color: #FF0000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF0000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF0000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RED = 0xfe8f9ca0;
    static { NAMED.put("red", 0xfe8f9ca0); LIST.add(0xfe8f9ca0); }

    /**
     * This color constant "orange" has RGBA8888 code {@code FF7F00FF}, L 0.74509805, A 0.54901963, B 0.57254905, alpha 1.0, hue 0.07449477, and saturation 0.88500965.
     * It has the encoded Oklab value 0xfe928cbe .
     * <pre>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF7F00'>&nbsp;@&nbsp;</font><font style='background-color: #FF7F00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF7F00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF7F00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ORANGE = 0xfe928cbe;
    static { NAMED.put("orange", 0xfe928cbe); LIST.add(0xfe928cbe); }

    /**
     * This color constant "yellow" has RGBA8888 code {@code FFFF00FF}, L 0.96862745, A 0.4627451, B 0.59607846, alpha 1.0, hue 0.16986547, and saturation 0.85278344.
     * It has the encoded Oklab value 0xfe9876f7 .
     * <pre>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFFF00'>&nbsp;@&nbsp;</font><font style='background-color: #FFFF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFFF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFFF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int YELLOW = 0xfe9876f7;
    static { NAMED.put("yellow", 0xfe9876f7); LIST.add(0xfe9876f7); }

    /**
     * This color constant "green" has RGBA8888 code {@code 00FF00FF}, L 0.8666667, A 0.38039216, B 0.5882353, alpha 1.0, hue 0.3486388, and saturation 1.0.
     * It has the encoded Oklab value 0xfe9661dd .
     * <pre>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FF00'>&nbsp;@&nbsp;</font><font style='background-color: #00FF00; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FF00;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FF00; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int GREEN = 0xfe9661dd;
    static { NAMED.put("green", 0xfe9661dd); LIST.add(0xfe9661dd); }

    /**
     * This color constant "blue" has RGBA8888 code {@code 0000FFFF}, L 0.4509804, A 0.48235294, B 0.34117648, alpha 1.0, hue 0.6666667, and saturation 1.0.
     * It has the encoded Oklab value 0xfe577b73 .
     * <pre>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0000FF'>&nbsp;@&nbsp;</font><font style='background-color: #0000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BLUE = 0xfe577b73;
    static { NAMED.put("blue", 0xfe577b73); LIST.add(0xfe577b73); }

    /**
     * This color constant "indigo" has RGBA8888 code {@code 520FE0FF}, L 0.46666667, A 0.53333336, B 0.36862746, alpha 1.0, hue 0.71970063, and saturation 0.8475976.
     * It has the encoded Oklab value 0xfe5e8877 .
     * <pre>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #520FE0'>&nbsp;@&nbsp;</font><font style='background-color: #520FE0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #520FE0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #520FE0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int INDIGO = 0xfe5e8877;
    static { NAMED.put("indigo", 0xfe5e8877); LIST.add(0xfe5e8877); }

    /**
     * This color constant "violet" has RGBA8888 code {@code 9040EFFF}, L 0.5882353, A 0.56078434, B 0.39607844, alpha 1.0, hue 0.7400259, and saturation 0.69078076.
     * It has the encoded Oklab value 0xfe658f96 .
     * <pre>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #9040EF'>&nbsp;@&nbsp;</font><font style='background-color: #9040EF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #9040EF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #9040EF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int VIOLET = 0xfe658f96;
    static { NAMED.put("violet", 0xfe658f96); LIST.add(0xfe658f96); }

    /**
     * This color constant "purple" has RGBA8888 code {@code C000FFFF}, L 0.61960787, A 0.60784316, B 0.3882353, alpha 1.0, hue 0.79168844, and saturation 1.0.
     * It has the encoded Oklab value 0xfe639b9e .
     * <pre>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C000FF'>&nbsp;@&nbsp;</font><font style='background-color: #C000FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C000FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C000FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PURPLE = 0xfe639b9e;
    static { NAMED.put("purple", 0xfe639b9e); LIST.add(0xfe639b9e); }

    /**
     * This color constant "brown" has RGBA8888 code {@code 8F573BFF}, L 0.54509807, A 0.5254902, B 0.5294118, alpha 1.0, hue 0.05623632, and saturation 0.29972398.
     * It has the encoded Oklab value 0xfe87868b .
     * <pre>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #8F573B'>&nbsp;@&nbsp;</font><font style='background-color: #8F573B; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #8F573B;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #8F573B; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BROWN = 0xfe87868b;
    static { NAMED.put("brown", 0xfe87868b); LIST.add(0xfe87868b); }

    /**
     * This color constant "pink" has RGBA8888 code {@code FFA0E0FF}, L 0.8352941, A 0.5568628, B 0.4745098, alpha 1.0, hue 0.87488794, and saturation 0.35551316.
     * It has the encoded Oklab value 0xfe798ed5 .
     * <pre>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA0E0'>&nbsp;@&nbsp;</font><font style='background-color: #FFA0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PINK = 0xfe798ed5;
    static { NAMED.put("pink", 0xfe798ed5); LIST.add(0xfe798ed5); }

    /**
     * This color constant "magenta" has RGBA8888 code {@code F500F5FF}, L 0.68235296, A 0.6313726, B 0.41568628, alpha 1.0, hue 0.82959664, and saturation 0.9094021.
     * It has the encoded Oklab value 0xfe6aa1ae .
     * <pre>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F500F5'>&nbsp;@&nbsp;</font><font style='background-color: #F500F5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F500F5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F500F5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAGENTA = 0xfe6aa1ae;
    static { NAMED.put("magenta", 0xfe6aa1ae); LIST.add(0xfe6aa1ae); }

    /**
     * This color constant "brick" has RGBA8888 code {@code D5524AFF}, L 0.6313726, A 0.5686275, B 0.53333336, alpha 1.0, hue 0.008957353, and saturation 0.512173.
     * It has the encoded Oklab value 0xfe8891a1 .
     * <pre>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D5524A'>&nbsp;@&nbsp;</font><font style='background-color: #D5524A; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D5524A;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D5524A; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRICK = 0xfe8891a1;
    static { NAMED.put("brick", 0xfe8891a1); LIST.add(0xfe8891a1); }

    /**
     * This color constant "ember" has RGBA8888 code {@code F55A32FF}, L 0.68235296, A 0.57254905, B 0.5568628, alpha 1.0, hue 0.037140083, and saturation 0.75178295.
     * It has the encoded Oklab value 0xfe8e92ae .
     * <pre>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #F55A32'>&nbsp;@&nbsp;</font><font style='background-color: #F55A32; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #F55A32;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #F55A32; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int EMBER = 0xfe8e92ae;
    static { NAMED.put("ember", 0xfe8e92ae); LIST.add(0xfe8e92ae); }

    /**
     * This color constant "salmon" has RGBA8888 code {@code FF6262FF}, L 0.7137255, A 0.5803922, B 0.53333336, alpha 1.0, hue 9.369861E-5, and saturation 0.5919734.
     * It has the encoded Oklab value 0xfe8894b6 .
     * <pre>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FF6262'>&nbsp;@&nbsp;</font><font style='background-color: #FF6262; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FF6262;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FF6262; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SALMON = 0xfe8894b6;
    static { NAMED.put("salmon", 0xfe8894b6); LIST.add(0xfe8894b6); }

    /**
     * This color constant "chocolate" has RGBA8888 code {@code 683818FF}, L 0.41960785, A 0.5254902, B 0.53333336, alpha 1.0, hue 0.06347794, and saturation 0.2938226.
     * It has the encoded Oklab value 0xfe88866b .
     * <pre>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #683818'>&nbsp;@&nbsp;</font><font style='background-color: #683818; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #683818;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #683818; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHOCOLATE = 0xfe88866b;
    static { NAMED.put("chocolate", 0xfe88866b); LIST.add(0xfe88866b); }

    /**
     * This color constant "tan" has RGBA8888 code {@code D2B48CFF}, L 0.80784315, A 0.5058824, B 0.5254902, alpha 1.0, hue 0.09748065, and saturation 0.23781472.
     * It has the encoded Oklab value 0xfe8681ce .
     * <pre>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2B48C'>&nbsp;@&nbsp;</font><font style='background-color: #D2B48C; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2B48C;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2B48C; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TAN = 0xfe8681ce;
    static { NAMED.put("tan", 0xfe8681ce); LIST.add(0xfe8681ce); }

    /**
     * This color constant "bronze" has RGBA8888 code {@code CE8E31FF}, L 0.7176471, A 0.5137255, B 0.56078434, alpha 1.0, hue 0.10091105, and saturation 0.5744981.
     * It has the encoded Oklab value 0xfe8f83b7 .
     * <pre>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #CE8E31'>&nbsp;@&nbsp;</font><font style='background-color: #CE8E31; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #CE8E31;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #CE8E31; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BRONZE = 0xfe8f83b7;
    static { NAMED.put("bronze", 0xfe8f83b7); LIST.add(0xfe8f83b7); }

    /**
     * This color constant "cinnamon" has RGBA8888 code {@code D2691DFF}, L 0.654902, A 0.54509807, B 0.56078434, alpha 1.0, hue 0.06806485, and saturation 0.69029737.
     * It has the encoded Oklab value 0xfe8f8ba7 .
     * <pre>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D2691D'>&nbsp;@&nbsp;</font><font style='background-color: #D2691D; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D2691D;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D2691D; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CINNAMON = 0xfe8f8ba7;
    static { NAMED.put("cinnamon", 0xfe8f8ba7); LIST.add(0xfe8f8ba7); }

    /**
     * This color constant "apricot" has RGBA8888 code {@code FFA828FF}, L 0.8156863, A 0.52156866, B 0.5764706, alpha 1.0, hue 0.09884546, and saturation 0.82258695.
     * It has the encoded Oklab value 0xfe9385d0 .
     * <pre>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFA828'>&nbsp;@&nbsp;</font><font style='background-color: #FFA828; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFA828;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFA828; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int APRICOT = 0xfe9385d0;
    static { NAMED.put("apricot", 0xfe9385d0); LIST.add(0xfe9385d0); }

    /**
     * This color constant "peach" has RGBA8888 code {@code FFBF81FF}, L 0.8666667, A 0.5176471, B 0.54509807, alpha 1.0, hue 0.08507312, and saturation 0.47791255.
     * It has the encoded Oklab value 0xfe8b84dd .
     * <pre>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFBF81'>&nbsp;@&nbsp;</font><font style='background-color: #FFBF81; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFBF81;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFBF81; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEACH = 0xfe8b84dd;
    static { NAMED.put("peach", 0xfe8b84dd); LIST.add(0xfe8b84dd); }

    /**
     * This color constant "pear" has RGBA8888 code {@code D3E330FF}, L 0.8862745, A 0.45882353, B 0.58431375, alpha 1.0, hue 0.18681404, and saturation 0.7025431.
     * It has the encoded Oklab value 0xfe9575e2 .
     * <pre>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #D3E330'>&nbsp;@&nbsp;</font><font style='background-color: #D3E330; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #D3E330;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #D3E330; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PEAR = 0xfe9575e2;
    static { NAMED.put("pear", 0xfe9575e2); LIST.add(0xfe9575e2); }

    /**
     * This color constant "saffron" has RGBA8888 code {@code FFD510FF}, L 0.89411765, A 0.4862745, B 0.5882353, alpha 1.0, hue 0.14080147, and saturation 0.84424806.
     * It has the encoded Oklab value 0xfe967ce4 .
     * <pre>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFD510'>&nbsp;@&nbsp;</font><font style='background-color: #FFD510; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFD510;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFD510; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAFFRON = 0xfe967ce4;
    static { NAMED.put("saffron", 0xfe967ce4); LIST.add(0xfe967ce4); }

    /**
     * This color constant "butter" has RGBA8888 code {@code FFF288FF}, L 0.95686275, A 0.4862745, B 0.5568628, alpha 1.0, hue 0.15134035, and saturation 0.44521707.
     * It has the encoded Oklab value 0xfe8e7cf4 .
     * <pre>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #FFF288'>&nbsp;@&nbsp;</font><font style='background-color: #FFF288; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #FFF288;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #FFF288; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int BUTTER = 0xfe8e7cf4;
    static { NAMED.put("butter", 0xfe8e7cf4); LIST.add(0xfe8e7cf4); }

    /**
     * This color constant "chartreuse" has RGBA8888 code {@code C8FF41FF}, L 0.9372549, A 0.4392157, B 0.58431375, alpha 1.0, hue 0.22184636, and saturation 0.71951014.
     * It has the encoded Oklab value 0xfe9570ef .
     * <pre>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #C8FF41'>&nbsp;@&nbsp;</font><font style='background-color: #C8FF41; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #C8FF41;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #C8FF41; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CHARTREUSE = 0xfe9570ef;
    static { NAMED.put("chartreuse", 0xfe9570ef); LIST.add(0xfe9570ef); }

    /**
     * This color constant "cactus" has RGBA8888 code {@code 30A000FF}, L 0.6431373, A 0.41960785, B 0.5647059, alpha 1.0, hue 0.30874372, and saturation 0.5568283.
     * It has the encoded Oklab value 0xfe906ba4 .
     * <pre>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #30A000'>&nbsp;@&nbsp;</font><font style='background-color: #30A000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #30A000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #30A000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CACTUS = 0xfe906ba4;
    static { NAMED.put("cactus", 0xfe906ba4); LIST.add(0xfe906ba4); }

    /**
     * This color constant "lime" has RGBA8888 code {@code 93D300FF}, L 0.8117647, A 0.43529412, B 0.5803922, alpha 1.0, hue 0.23129159, and saturation 0.71080446.
     * It has the encoded Oklab value 0xfe946fcf .
     * <pre>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #93D300'>&nbsp;@&nbsp;</font><font style='background-color: #93D300; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #93D300;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #93D300; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LIME = 0xfe946fcf;
    static { NAMED.put("lime", 0xfe946fcf); LIST.add(0xfe946fcf); }

    /**
     * This color constant "olive" has RGBA8888 code {@code 818000FF}, L 0.6117647, A 0.4745098, B 0.56078434, alpha 1.0, hue 0.17558604, and saturation 0.43590918.
     * It has the encoded Oklab value 0xfe8f799c .
     * <pre>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #818000'>&nbsp;@&nbsp;</font><font style='background-color: #818000; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #818000;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #818000; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int OLIVE = 0xfe8f799c;
    static { NAMED.put("olive", 0xfe8f799c); LIST.add(0xfe8f799c); }

    /**
     * This color constant "fern" has RGBA8888 code {@code 4E7942FF}, L 0.56078434, A 0.4627451, B 0.5294118, alpha 1.0, hue 0.31559366, and saturation 0.20097598.
     * It has the encoded Oklab value 0xfe87768f .
     * <pre>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #4E7942'>&nbsp;@&nbsp;</font><font style='background-color: #4E7942; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #4E7942;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #4E7942; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int FERN = 0xfe87768f;
    static { NAMED.put("fern", 0xfe87768f); LIST.add(0xfe87768f); }

    /**
     * This color constant "moss" has RGBA8888 code {@code 204608FF}, L 0.3764706, A 0.45882353, B 0.5372549, alpha 1.0, hue 0.28017563, and saturation 0.23527473.
     * It has the encoded Oklab value 0xfe897560 .
     * <pre>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #204608'>&nbsp;@&nbsp;</font><font style='background-color: #204608; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #204608;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #204608; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MOSS = 0xfe897560;
    static { NAMED.put("moss", 0xfe897560); LIST.add(0xfe897560); }

    /**
     * This color constant "celery" has RGBA8888 code {@code 7DFF73FF}, L 0.9019608, A 0.41960785, B 0.56078434, alpha 1.0, hue 0.3266647, and saturation 0.5390122.
     * It has the encoded Oklab value 0xfe8f6be6 .
     * <pre>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7DFF73'>&nbsp;@&nbsp;</font><font style='background-color: #7DFF73; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7DFF73;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7DFF73; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CELERY = 0xfe8f6be6;
    static { NAMED.put("celery", 0xfe8f6be6); LIST.add(0xfe8f6be6); }

    /**
     * This color constant "sage" has RGBA8888 code {@code ABE3C5FF}, L 0.88235295, A 0.46666667, B 0.50980395, alpha 1.0, hue 0.41779384, and saturation 0.24272788.
     * It has the encoded Oklab value 0xfe8277e1 .
     * <pre>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #ABE3C5'>&nbsp;@&nbsp;</font><font style='background-color: #ABE3C5; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #ABE3C5;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ABE3C5; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SAGE = 0xfe8277e1;
    static { NAMED.put("sage", 0xfe8277e1); LIST.add(0xfe8277e1); }

    /**
     * This color constant "jade" has RGBA8888 code {@code 3FBF3FFF}, L 0.7294118, A 0.41568628, B 0.5568628, alpha 1.0, hue 0.3623353, and saturation 0.57830364.
     * It has the encoded Oklab value 0xfe8e6aba .
     * <pre>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3FBF3F'>&nbsp;@&nbsp;</font><font style='background-color: #3FBF3F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3FBF3F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3FBF3F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int JADE = 0xfe8e6aba;
    static { NAMED.put("jade", 0xfe8e6aba); LIST.add(0xfe8e6aba); }

    /**
     * This color constant "cyan" has RGBA8888 code {@code 00FFFFFF}, L 0.90588236, A 0.42352942, B 0.47843137, alpha 1.0, hue 0.5, and saturation 1.0.
     * It has the encoded Oklab value 0xfe7a6ce7 .
     * <pre>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #00FFFF'>&nbsp;@&nbsp;</font><font style='background-color: #00FFFF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #00FFFF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #00FFFF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int CYAN = 0xfe7a6ce7;
    static { NAMED.put("cyan", 0xfe7a6ce7); LIST.add(0xfe7a6ce7); }

    /**
     * This color constant "mint" has RGBA8888 code {@code 7FFFD4FF}, L 0.92156863, A 0.4392157, B 0.50980395, alpha 1.0, hue 0.4465047, and saturation 0.5094682.
     * It has the encoded Oklab value 0xfe8270eb .
     * <pre>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #7FFFD4'>&nbsp;@&nbsp;</font><font style='background-color: #7FFFD4; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #7FFFD4;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #7FFFD4; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MINT = 0xfe8270eb;
    static { NAMED.put("mint", 0xfe8270eb); LIST.add(0xfe8270eb); }

    /**
     * This color constant "teal" has RGBA8888 code {@code 007F7FFF}, L 0.5686275, A 0.4509804, B 0.4862745, alpha 1.0, hue 0.50141567, and saturation 0.50450027.
     * It has the encoded Oklab value 0xfe7c7391 .
     * <pre>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #007F7F'>&nbsp;@&nbsp;</font><font style='background-color: #007F7F; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #007F7F;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #007F7F; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TEAL = 0xfe7c7391;
    static { NAMED.put("teal", 0xfe7c7391); LIST.add(0xfe7c7391); }

    /**
     * This color constant "turquoise" has RGBA8888 code {@code 2ED6C9FF}, L 0.80784315, A 0.43137255, B 0.49019608, alpha 1.0, hue 0.48925468, and saturation 0.7674718.
     * It has the encoded Oklab value 0xfe7d6ece .
     * <pre>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #2ED6C9'>&nbsp;@&nbsp;</font><font style='background-color: #2ED6C9; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #2ED6C9;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #2ED6C9; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int TURQUOISE = 0xfe7d6ece;
    static { NAMED.put("turquoise", 0xfe7d6ece); LIST.add(0xfe7d6ece); }

    /**
     * This color constant "sky" has RGBA8888 code {@code 10C0E0FF}, L 0.7607843, A 0.44313726, B 0.4627451, alpha 1.0, hue 0.5243008, and saturation 0.88374835.
     * It has the encoded Oklab value 0xfe7671c2 .
     * <pre>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #10C0E0'>&nbsp;@&nbsp;</font><font style='background-color: #10C0E0; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #10C0E0;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #10C0E0; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int SKY = 0xfe7671c2;
    static { NAMED.put("sky", 0xfe7671c2); LIST.add(0xfe7671c2); }

    /**
     * This color constant "cobalt" has RGBA8888 code {@code 0046ABFF}, L 0.44705883, A 0.48235294, B 0.4117647, alpha 1.0, hue 0.6005195, and saturation 0.6801619.
     * It has the encoded Oklab value 0xfe697b72 .
     * <pre>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #0046AB'>&nbsp;@&nbsp;</font><font style='background-color: #0046AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #0046AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #0046AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int COBALT = 0xfe697b72;
    static { NAMED.put("cobalt", 0xfe697b72); LIST.add(0xfe697b72); }

    /**
     * This color constant "denim" has RGBA8888 code {@code 3088B8FF}, L 0.62352943, A 0.46666667, B 0.45490196, alpha 1.0, hue 0.557327, and saturation 0.55407006.
     * It has the encoded Oklab value 0xfe74779f .
     * <pre>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #3088B8'>&nbsp;@&nbsp;</font><font style='background-color: #3088B8; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #3088B8;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #3088B8; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int DENIM = 0xfe74779f;
    static { NAMED.put("denim", 0xfe74779f); LIST.add(0xfe74779f); }

    /**
     * This color constant "navy" has RGBA8888 code {@code 000080FF}, L 0.28627452, A 0.4862745, B 0.4, alpha 1.0, hue 0.6633139, and saturation 0.509886.
     * It has the encoded Oklab value 0xfe667c49 .
     * <pre>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #000080'>&nbsp;@&nbsp;</font><font style='background-color: #000080; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #000080;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000080; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int NAVY = 0xfe667c49;
    static { NAMED.put("navy", 0xfe667c49); LIST.add(0xfe667c49); }

    /**
     * This color constant "lavender" has RGBA8888 code {@code B991FFFF}, L 0.7607843, A 0.53333336, B 0.43529412, alpha 1.0, hue 0.7186765, and saturation 0.42370677.
     * It has the encoded Oklab value 0xfe6f88c2 .
     * <pre>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #B991FF'>&nbsp;@&nbsp;</font><font style='background-color: #B991FF; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #B991FF;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #B991FF; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int LAVENDER = 0xfe6f88c2;
    static { NAMED.put("lavender", 0xfe6f88c2); LIST.add(0xfe6f88c2); }

    /**
     * This color constant "plum" has RGBA8888 code {@code BE0DC6FF}, L 0.58431375, A 0.6117647, B 0.42352942, alpha 1.0, hue 0.82369447, and saturation 0.75267494.
     * It has the encoded Oklab value 0xfe6c9c95 .
     * <pre>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #BE0DC6'>&nbsp;@&nbsp;</font><font style='background-color: #BE0DC6; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #BE0DC6;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #BE0DC6; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int PLUM = 0xfe6c9c95;
    static { NAMED.put("plum", 0xfe6c9c95); LIST.add(0xfe6c9c95); }

    /**
     * This color constant "mauve" has RGBA8888 code {@code AB73ABFF}, L 0.6627451, A 0.5411765, B 0.47058824, alpha 1.0, hue 0.8225035, and saturation 0.22910339.
     * It has the encoded Oklab value 0xfe788aa9 .
     * <pre>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #AB73AB'>&nbsp;@&nbsp;</font><font style='background-color: #AB73AB; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #AB73AB;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #AB73AB; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int MAUVE = 0xfe788aa9;
    static { NAMED.put("mauve", 0xfe788aa9); LIST.add(0xfe788aa9); }

    /**
     * This color constant "rose" has RGBA8888 code {@code E61E78FF}, L 0.6156863, A 0.6156863, B 0.49411765, alpha 1.0, hue 0.9223609, and saturation 0.7501173.
     * It has the encoded Oklab value 0xfe7e9d9d .
     * <pre>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #E61E78'>&nbsp;@&nbsp;</font><font style='background-color: #E61E78; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #E61E78;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #E61E78; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int ROSE = 0xfe7e9d9d;
    static { NAMED.put("rose", 0xfe7e9d9d); LIST.add(0xfe7e9d9d); }

    /**
     * This color constant "raspberry" has RGBA8888 code {@code 911437FF}, L 0.44705883, A 0.5803922, B 0.5137255, alpha 1.0, hue 0.9529095, and saturation 0.46955392.
     * It has the encoded Oklab value 0xfe839472 .
     * <pre>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #000000'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #888888; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #ffffff; color: #911437'>&nbsp;@&nbsp;</font><font style='background-color: #911437; color: #888888'>&nbsp;@&nbsp;</font>
     * <font style='background-color: #911437;'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #000000; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #888888; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #ffffff; color: #000000'>&nbsp;&nbsp;&nbsp;</font><font style='background-color: #911437; color: #ffffff'>&nbsp;@&nbsp;</font>
     * </pre>
     */
    public static final int RASPBERRY = 0xfe839472;
    static { NAMED.put("raspberry", 0xfe839472); LIST.add(0xfe839472); }

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
     * Converts a packed Oklab int color in the format used by constants in this class to an RGBA8888 int.
     * This format of int can be used with Pixmap and in some other places in libGDX.
     * @param oklab a packed int color, as from a constant in this class
     * @return an RGBA8888 int color
     */
    public static int toRGBA8888(final int oklab)
    {
        final float L = (oklab & 0xff) / 255f;
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
        final float L = (packed & 0xff) / 255f;
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
                Math.min(Math.max((int)((0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
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
                Math.min(Math.max((int)((0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
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
                Math.min(Math.max((int)((0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s) * 255.999f         ), 0), 255)
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
        final float L = (encoded & 0xff) / 255f;
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
        final float L = (encoded & 0xff) / 255f;
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
        final float L = (encoded & 0xff) / 255f;
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
        final float L = (encoded & 0xff) / 255f;
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
        final float L = (encoded & 0xff) / 255f;
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
        final float L = (encoded & 0xff) / 255f;
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
        return (float) Math.sqrt(A * A + B * B) * 512f / dist;
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
        return GAMUT_DATA[idx] * 0x1p-8f;
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
        if(dist * dist * 0x1p-16f >= (A * A + B * B))
            return oklab(L, A + 0.5f, B + 0.5f, alpha);
        return (int) (alpha * 127.999f) << 25 |
                        (int) (TrigTools.sin_(hue) * dist + 128f) << 16 |
                        (int) (TrigTools.cos_(hue) * dist + 128f) << 8 |
                        (int) (L * 255.999f);
    }


    private static final byte[] GAMUT_DATA = (new StringBuilder().append("\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\001\001\001\001\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\003\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\003\003\003\004\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\004\004\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\003\003\003\003\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\004\004\005\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\004\004\004\005\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\004\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\004\004\004\004\005\006\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\004\004\004\004\004\005\005\006\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\005\005\005\006\007\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\005\005\005\005\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\005\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\005\005\005\006\006\007\n\n\n\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\005\005\005\006\006\007\b\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\005\005\005\005\006\006\006\007\b\013\013\013\013\013\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\005\005\005\005\005\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\005\005\005\005\006\006\006\007\b\t\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\005\005\005\005\005\006\006\006\007\007\b\t\r\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\007\007\007\007\b\b\b\b\b\b\b\b\b\b\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\006\006\006\007\007\b\b\n\r\r\r\r\r\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\007\006\006\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\006\006\006\007\007\b\b\t\n\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\006\006\006\006\007\007\007\b\t\t\013\017\016\016\016\016\016\016\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\007\007\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\006\006\006\007\007\007\b\b\t\n\013\017\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\007\007\007\007\007\b\007\007\007\007\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\006\006\006\006\007\007\007\b\b\t\t\n\f\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\n\n\n\n\n\n\n\n\t\t\t\t\n\n\n\n\n\n\n\n\n\n\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\006\006\006\006\006\007\007\007\b\b\b\t\n\013\f\021\021\020\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\b\b\b\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\b\b\b\t\t\n\013\r\021\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\n\n\n\t\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\b\b\b\b\t\b\b\b\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\b\b\b\t\t\n\013\f\r\022\022\022\021\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\t\t\t\b\b\b\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\007\007\b\b\t\t\n\n\013\f\016\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\n\013\013\f\016\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\f\f\f\f\f\f\f\f\013\013\013\013\f\f\f\f\f\f\f\f\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\n\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\n\n\013\f\r\017\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\t\n\n\n\t\t\t\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\n\n\013\013\f\r\017\025\025\024\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\n\n\n\n\n\t\t\t\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\b\t\t\t\n\n\013\f\r\016\020\026\025\025\025\024\024\024\024\024\023\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\t\t\t\t\n\n\n\013\n\n\n\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\b\t\t\t\n\n\013\013\f\r\016\020\026\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\b\b\b\b\t\t\t\n\n\013\013\f\r\r\017\021\027\027\026\026\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\r\r\r\r\016\016\016\016\016\016\016\016\r\r\r\r\016\016\016\016\016\016\016\016\016\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\013\013\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\t\t\t\t\n\n\013\013\f\r\016\017\021\030\027\027\027\026\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\n\013\013\013\f\013\013\n\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\t\t\t\t\n\n\013\013\f\f\r\016\020\022\030\030\030\027\027\027\027\026\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\013\013\n\n\n\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\n\n\n\013\013\f\r\016\017\020\022\031\031\030\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\016\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\f\f\013\013\n\n\n\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\n\n\n\013\013\f\f\r\016\017\021\023\032\031\031\031\030\030\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\016\016\r\r\f\f\f\013\013\013\n\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\r\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\t\n\n\n\013\013\f\f\r\016\016\020\021\023\032\032\032\031\031\031\030\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\017\017\017\020\020\020\020\020\020\020\020\017\017\017\017\020\020\020\020\020\020\020\020\020\020\017\017\016\r\r\r\f\f\f\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\r\r\r\f\f\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\n\n\n\013\013\013\f\f\r\016\017\020\021\024\033\033\032\032\032\031\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\017\017\016\016\r\r\r\f\f\f\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\f\f\r\r\016\017\020\022\024\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\020\020\020\020\020\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\020\017\017\016\016\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\f\f\f\r\r\016\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\f\r\r\016\017\020\021\022\025\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\020\020\017\017\016\016\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\016\016\016\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\f\f\r\r\016\017\020\021\023\025\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\f\f\r\r\016\016\017\020\022\023\026\036\035\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\021\021\022\022\022\022\022\022\022\022\021\021\021\021\022\022\022\022\022\022\022\022\022\022\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\016\016\r\r\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\r\r\016\017\020\021\022\024\026\037\036\036\035\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\017\017\017\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\r\r\016\016\017\020\021\022\024\027\037\037\036\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\031\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\022\022\022\022\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\022\021\021\020\017\017\016\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\017\016\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\007\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\r\r\016\017\020\020\021\023\025\027  \037\037\036\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\022\022\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\020\017\017\016\016\r\r\f\f\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\r\r\016\016\017\020\021\022\023\025\030!  \037\037\037\036\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\017\017\017\020\020\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\r\r\r\016\017\017\020\021\022\024\025\030!!   \037\037\037\036\036\035\035\035\035\034\034\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\023\024\024\024\024\024\024\024\024\023\023\023\023\024\024\024\024\024\024\024\024\024\024\023\022\022\021\020\020\017\017\017\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\020\017\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\016\016\017\020\021\021\023\024\026\031\"\"!!   \037\037\036\036\036\035\035\035\035\034\034\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\020\020\017\016\016\r\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\016\016\017\017\020\021\022\023\024\026\031#\"\"!!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\024\024\024\024\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\024\023\022\022\021\021\020\020\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\020\021\021\020\017\017\016\016\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\r\016\016\017\020\020\021\022\023\025\027\032##\"\"\"!!   \037\037\037\036\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\024\023\023\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\017\020\020\021\021\021\020\020\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\016\016\017\017\020\021\022\023\024\025\027\032$$##\"\"!!!   \037\037\037\036\036\036\036\035\035\035\035\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\025\025\024\023\022\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\021\021\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\020\020\021\022\023\024\026\030\033%$$##\"\"\"!!!   \037\037\037\036\036\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\025\026\026\026\026\026\026\026\026\025\025\025\025\026\026\026\026\026\026\026\026\026\026\025\024\023\023\022\022\021\020\020\020\017\017\017\016\016\016\r\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\r\r\r\r\016\016\016\016\017\017\020\020\020\021\021\022\022\021\020\020\017\016\016\016\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\017\020\021\021\022\023\025\026\030\033&%$$$##\"\"\"!!    \037\037\037\036\036\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\025\025\024\023\022\022\021\021\020\020\020\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\022\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\016\016\016\017\017\020\021\022\023\024\025\027\031\034&&%%$$###\"\"!!!    \037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\026\025\024\023\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\022\022\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\017\017\020\020\021\022\023\024\025\027\031\034'&&%%$$$##\"\"\"!!!    \037\037\037\037\036\036\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\026\025\024\024\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\023\022\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\020\021\021\022\023\024\026\027\031\035(''&&%%$$###\"\"\"!!!    \037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\027\027\026\025\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\023\023\022\021\021\020\020\017\017\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\017\017\020\020\021\022\023\024\025\026\030\032\035((''&&%%$$$###\"\"\"!!!    \037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\027\027\027\027\030\030\030\030\030\030\030\030\030\030\027\026\025\024\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\016\016\016\016\017\017\017\017\020\020\021\021\021\022\022\023\024\023\022\022\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\f\r\r\r\016\016\017\017\017\020\021\021\022\023\024\025\026\030\032\036)((''&&%%%$$###\"\"\"\"!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\027\026\026\025\024\023\023\022\022\021\021\021\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\024\023\022\021\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\016\016\016\017\017\020\020\021\022\022\023\024\025\027\031\033\036*))('''&&%%$$$###\"\"\"\"!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\030\030\030\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\030\027\026\025\024\024\023\023\022\022\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\024\024\023\022\022\021\020\020\017\017\017\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\017\017\017\020\021\021\022\023\024\025\026\027\031\033\037**))((''&&%%%$$$###\"\"\"!!!!    \037\037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\030\027\026\026\025\024\024\023\022\022\022\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\025\024\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\022\023\024\025\026\030\031\034\037+**))((''&&&%%%$$$###\"\"\"!!!!    \037\037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\030\030\027\026\025\024\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\024\025\025\024\023\022\022\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\022\022\023\024\025\027\030\032\034 ,++*))(((''&&&%%$$$####\"\"\"!!!!     \037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\031\031\031\031\032\032\032\032\032\032\032\032\032\032\031\030\027\026\025\025\024\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\020\020\020\020\021\021\022\022\022\023\024\024\025\025\025\024\023\022\022\021\021\020\020\017\017\016\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\023\024\025\026\027\030\032\035 ,,++**))((''&&&%%%$$$####\"\"\"\"!!!     \037\037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\031\030\027\027\026\025\024\024\023\023\022\022\022\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\026\025\024\024\023\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\020\020\021\021\022\022\023\024\025\026\027\031\033\035!-,,++**))(('''&&&%%%$$$####\"\"\"\"!!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\032\032\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\032\031\030\027\026\025\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\025\026\026\025\024\023\022\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\023\023\024\025\026\030\031\033\036!.--,++**)))(('''&&&%%%$$$####\"\"\"\"!!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033").append("\033\033\033\033\033\033\033\033\033\033\033\033\033\033\032\031\030\027\026\026\025\025\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\026\026\026\025\024\023\023\022\021\021\020\020\020\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\020\020\021\021\022\022\023\024\025\026\027\030\032\033\036\"..--,,++**))(('''&&&%%%$$$$####\"\"\"\"!!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\032\031\030\030\027\026\025\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\025\026\027\026\025\024\024\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\024\025\026\027\030\032\034\036\"//.--,,++**))((('''&&&%%%$$$$####\"\"\"\"!!!!    \037\037\037\037\036\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\033\033\033\033\034\034\034\034\034\034\034\034\034\034\033\032\031\030\027\026\026\025\025\024\024\023\023\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\023\023\023\024\024\025\026\026\027\027\026\025\024\023\023\022\021\021\020\020\020\017\017\017\016\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\023\024\025\026\027\031\032\034\037#0//.--,,++**)))((('''&&&%%%$$$$####\"\"\"!!!!!    \037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\022\023\023\024\024\025\025\026\027\027\027\026\025\024\024\023\022\022\021\021\020\020\017\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\024\025\025\027\030\031\033\035\037#10//.--,,+++**))((('''&&&&%%%$$$$###\"\"\"\"!!!!     \037\037\037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\034\034\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\034\033\032\031\030\027\026\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\022\023\023\024\024\025\025\026\026\027\030\027\026\025\025\024\023\023\022\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\016\017\017\017\020\020\021\021\022\022\023\023\024\025\026\027\030\031\033\035 $110//..--,,++**)))((('''&&&%%%%$$$$###\"\"\"\"!!!!     \037\037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\027\027\030\030\027\026\025\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\024\024\025\026\027\030\032\033\036 $2110//..--,,++***)))((('''&&&%%%%$$$####\"\"\"\"!!!!     \037\037\037\037\037\036\036\036\036\036\036\036\035\035\035\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\034\033\032\031\031\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\022\022\023\023\024\024\025\025\026\026\027\030\030\030\027\026\025\024\024\023\022\022\021\021\021\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\021\021\021\022\023\023\024\025\025\026\027\031\032\034\036!%32110//..--,,+++**)))(((''''&&&%%%%$$$####\"\"\"\"!!!!     \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\035\035\035\035\036\036\036\036\036\036\036\036\036\036\035\034\033\032\031\030\027\027\026\025\025\024\024\024\023\023\022\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\027\027\030\031\030\027\026\025\025\024\023\023\022\022\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\025\026\027\030\031\032\034\036!%3321100/..--,,,++***)))((('''&&&&%%%$$$$####\"\"\"\"!!!!     \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\031\030\027\026\025\024\024\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\023\023\024\024\025\026\027\030\031\033\035\037\"&43321100//..--,,+++***)))((('''&&&&%%%$$$$####\"\"\"\"!!!!      \037\037\037\037\037\037\037\036\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037\036\034\033\032\032\031\030\027\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\031\031\030\027\026\025\024\024\023\023\022\022\021\021\020\020\020\017\017\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\025\025\026\027\030\032\033\035\037\"&543321100//..--,,,++***)))(((''''&&&%%%%$$$$####\"\"\"\"!!!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\031\030\027\026\025\025\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\022\023\024\024\025\026\027\030\031\032\034\035 \"'5543321100//..---,,+++***)))(((''''&&&%%%%$$$####\"\"\"\"\"!!!!!      \037\037\037 \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    \036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\025\026\027\027\030\031\031\032\032\030\027\027\026\025\024\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\025\025\026\027\030\031\032\034\036 #'65543321100//...--,,,++***))))((('''&&&&%%%$$$$####\"\"\"\"\"!!!!!               \037\037\037\037          \037\036\034\033\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\032\032\031\030\027\026\025\025\024\023\023\022\022\022\021\021\020\020\020\020\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\017\017\017\017\020\020\020\020\021\021\022\022\022\023\024\024\025\026\026\027\030\031\033\034\036 #(765543322100///..--,,,+++***)))(((('''&&&&%%%$$$$####\"\"\"\"\"!!!!!!                        ! \037\036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\033\032\031\030\027\026\026\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\016\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\026\027\030\031\032\033\035\037!$(77655433221100//..---,,,+++***)))(((''''&&&%%%%$$$$####\"\"\"\"\"!!!!!!! !!!             !!!!!!\037\036\035\034\033\032\032\031\030\030\027\026\026\025\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\025\025\025\026\026\027\027\030\031\031\032\033\033\031\030\027\027\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\021\021\021\022\022\022\023\023\024\025\025\026\027\030\031\032\033\035\037!$)877655433221100//...--,,,+++***))))((('''&&&&%%%%$$$$####\"\"\"\"\"\"!!!!!!!!!!!!!!!!!!!!!!!!!!! \037\036\034\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\033\033\033\032\031\030\027\026\025\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\026\026\027\030\031\032\034\035\037\"%)9877655433221100///..---,,,+++***)))(((('''&&&&%%%$$$$$####\"\"\"\"\"\"\"!!\"!!!!!!!!!!!!!!!!!\"\"\"\" \037\036\035\034\033\032\031\031\030\030\027\026\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\031\032\033\034\033\032\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\022\022\022\023\023\024\025\025\026\027\027\030\032\033\034\036 \"%*:98776554332211000//...--,,,+++****)))(((''''&&&%%%%$$$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"!!!!\"\"\"\"\"\"\"\"\"\"!\037\036\035\034\033\033\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\034\032\031\030\027\027\026\025\025\024\024\023\023\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\023\023\024\024\025\025\026\027\030\031\032\033\034\036 \"&*:998776554432211100//...---,,,+++***)))(((('''&&&&%%%%$$$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"##\"! \037\036\035\034\033\032\031\031\030\030\027\026\026\026\025\025\024\024\024\024\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\024\024\024\024\025\025\026\026\026\027\027\030\031\031\032\033\034\034\034\033\032\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\026\026\027\030\031\032\033\035\036 #&+;:998776554433221100///...--,,,++++***)))(((''''&&&&%%%%$$$$$######\"####\"\"\"\"\"\"\"\"\"\"\"#######! \037\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\022\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\022\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\034\033\032\031\030\027\026\026\025\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\025\026\027\027\030\031\032\034\035\037!#&+<;:9987765544332211000//...---,,,+++***))))(((''''&&&%%%%%$$$$$###########################\"!\037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\021\021\021\021\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\026\027\027\030\031\031\032\033\033\034\035\035\033\032\031\030\027\027\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\016\016\016\016\016\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\023\023\023\024\024\025\026\026\027\030\031\032\033\034\035\037!$',<;;:9887765544332211100///...---,,,+++***)))(((('''&&&&%%%%%$$$$$$##$#################$$$$\"! \037\036\035\034\033\032\032\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\034\035\035\035\034\033\032\031\030\027\026\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\022\023\023\024\024\025\025\026\026\027\030\031\032\033\034\036 \"$',=<;;:98877655443322211000//...---,,,+++***))))(((''''&&&&%%%%%$$$$$$$$$$$$$$####$$$$$$$$$$#! \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\027\030\031\031\032\032\033\034\035\036\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\027\027\030\031\032\033\035\036 \"%(->=<;;:98877655443332211000///...---,,,+++***)))((((''''&&&&%%%%%%$$$$$$$$$$$$$$$$$$$$$$%%$#\" \037\036\035\034\034\033\032\032\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\034\034\035\036\036\034\033\032\031\030\030\027\026\026\025\024\024\024\023\023\022\022\022\021\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\024\024\025\025\026\026\027\030\031\031\033\034\035\036 \"%(->>=<;::98877665544332211100///...---,,,+++***))))((((''''&&&&%%%%%%%%%%%$$$$$$$$$$$%%%%%%%#\"! \037\036\035\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\036\036\036\035\033\032\031\031\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\027\027\030\031\032\033\034\035\037!#%).?>==<;::988776655443322211000///...---,,,+++***)))((((''''&&&&&&%%%%%%%%%%%%%%%%%%%%%%%%&%$\"! \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\022\022\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\037\036\035\034\033\032\031\030\027\027\026\025\025\024\024\024\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\024\024\024\025\026\026\027\027\030\031\032\033\034\036\037!#&).@?>=<<;::988776655443322211100///...---,,,+++****)))((((''''&&&&&&%%&&%%%%%%%%%%%%%%%&&&&&$#\" \037\036\035\035\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\034\035\036\037\037\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\017\017\017\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\030\031\031\032\033\035\036 !#&*/@@?>=<<;::9887766554433322111000///..---,,,,+++***))))(((('''''&&&&&&&&&&&&&%%%%&&&&&&&&&&%#\"! \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\036\037\037\036\034\033\032\031\031\030\027\027\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\032\033\034\035\036 \"$'*/A@??>=<<;::9887766554443322211000///...---,,,+++****)))((((('''''&&&&&&&&&&&&&&&&&&&&&&'''%$\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 \037\036\035\034\033\032\031\030\027\027\026\026\025\025\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\033\034\035\037 \"$'+0BA@?>>=<<;::99877665554433222111000//...---,,,++++***))))(((((''''''''''&&&&&&&&&&&'''''''%$#! \037\036\035\035\034\033\033\032\031\031\030\030\030\027\027\026\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\024\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\034\035\035\036\037  \036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\030\030\031\032\033\034\036\037!\"%'+0CBA@?>>=<<;::99887766554433322111000///...---,,,+++****))))(((((''''''''''''''''''''''''('&$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\036\037\037  \036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\032\033\035\036\037!#%(+1CBAA@?>>=<;;::99887766554443322211100///...---,,,++++****))))(((((('(('''''''''''''''(((((&%#\"! \037\036\035\035\034\033\033\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037 ! \037\036\034\033\032\032\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\021\021\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\021\021\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\030\030\031\032\033\034\035\036 !#%(,1DCBA@@?>==<;;::998877665554433322111000///...---,,,+++****)))))(((((((((((((''''(((((((((('%$\"! \037\036\036\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !!\037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\031\032\033\034\035\036 \"#&),2EDCBA@@?>==<;;::998877665554433322211000///...---,,,++++****)))))(((((((((((((((((((((()))'%$#\"! \037\036\035\034\034\033\032\032\031\031\031\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037\037 !!\037\036\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\031\032\032\033\034\035\037 \"$&)-2EDCCBA@??>==<;;::998877666554443322211100///...---,,,,++++****))))))))))((((((((((()))))))'&$#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"! \036\035\034\033\032\032\031\030\030\027\026\026\026\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\033\034\035\036\037!\"$')-3FEDCBBA@??>==<;;::9988777665544433322111000///...---,,,++++*****))))))))))))))))))))))))*)(&%#\"! \037\036\036\035\034\034\033\032\032\031\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\034\034\035\036\036\037 !\"\" \037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\031\031\032\033\034\035\036\037!#%'*.3GFEDCBAA@??>==<<;::9988877665554433322211000///...---,,,,++++******)**)))))))))))))))*****('%$#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\034\035\036\037  !\"\" \037\036\035\034\033\032\031\031\030\027\027\026\026\026\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\026\026\026\027\030\030\031\032\032\033\034\035\036 !#%'*.4GFEEDCBAA@??>==<<;::9998877665554443322211100///....---,,,,++++*************))))**********('%$#\"! \037\036\036\035\034\034\033\032\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#\"!\037\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\033\033\034\035\037 \"#%(+/4HGFEDDCBA@@?>>==<<;;::9988776665544433222111000///...----,,,,+++++********************++++)'&%#\"! \037\037\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#\"! \036\035\034\033\033\032\031\031\030\027\027\026\026\026\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\026\026\026\027\027\030\031\031\032\033\034\035\036\037 \"$&(+/5IHGFEDCCBA@@?>>==<<;;::9988777665554433322111000///....---,,,,,+++++++++***********+++++++)(&%$#\"! \037\036\035\035\034\034\033\032\032\032\031\031\030\030\030\030\027\027\027\027\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\032\033\033\034\035\035\036\037  !\"$#! \037\036\035\034\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036\037!\"$&(,/5IHGGFEDCBBA@@?>>==<<;;::9988777665554433322211100////...----,,,,,+++++++++++++++++++++++,+*('%$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#$#\" \037\036\035\034\033\032\032\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032\033\033\034\035\036 !\"$&),06JIHGFEEDCBBA@@?>>==<<;;::99888776665544433222111000///....----,,,,,+,,+++++++++++++++,,,,,*('&$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\031\031\031\032\032\032\033\033\034\035\035\036\037\037 !\"#$#\"!\037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\034\034\035\037 !#%'),06KJIHGFEDDCBBA@@?>>==<<;;::99988776665544433322111000////...-----,,,,,,,,,,,,++++,,,,,,,,,,*)'&%$\"!! \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037  !\"#%$\"! \037\035\035\034\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032\032\033\034\035\036\037 \"#%'*-17LJIIHGFEDDCBAA@@?>>==<<;;::999887776655544333222111000///....-----,,-,,,,,,,,,,,,,,,,,----+)(&%$#\"! \037\036\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\033\034\034\035\036\036\037 !\"#$%$#! \037\036\035\034\033\032\032\031\030\030\027\027\027\026\026\025\025\025\025\024\024\024\024\023\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\023\024\024\024\024\025\025\025\025\026\026\027\027\027\030\031\031\032\032\033\034\035\036\037 \"#%'*-17LKJIHGGFEDCCBAA@@?>>==<<;;:::99887776655544333222111000////....---------,,,,,,,,,,,-------+*('%$#\"!  \037\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%$#! \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\031\032\033\034\034\035\036\037!\"$&(*.28MLKJIHGFFEDCCBAA@@?>>==<<;;;::998887766555444332221110000///.....----------------------..-,*('&%#\"!! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\033\033\033\034\034\035\036\036\037  !\"#$&%#\"!\037\036\035\034\034\033\032\032\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\027\027\027\030\030\031\032\032\033\034\035\036\037 !\"$&(+.28NMKKJIHGFEEDCCBAA@@??>==<<<;;::998887766655444333222111000////.....-..---------------.....,*)'&%$#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%&%$\"! \037\036\035\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\036\037 !#$&)+.39NMLKJIHHGFEEDCCBAA@@??>>==<<;;::9998877666554443332221110000////............----......../.,+)(&%$#\"!  \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\036\036\037  !\"#$%&%$\"! \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\037 \"#%'),/39ONMLKJIHGGFEDDCCBAA@@??>>==<<;;::9998877766555443332221111000/////../.................////-+)('%$#\"!! \037\037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037 !!\"#$%'&$#! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\035\036\037!\"#%'),/3:OONMLKJIHGGFEDDCBBAA@@??>>==<<;;:::9988777665554443322221110000/////////...........///////-+*('&%$#\"! \037\037\036\036\035\035\034\034\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\034\034\034\035\035\036\037\037 !\"#$%&'&$#\" \037\036\035\035\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\037 !\"$%'*,04:OONMLKJJIHGFFEDDCBBAA@@??>>==<<;;:::99887776655544433322211110000//////////////////////00/.,*)'&%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037  !\"#$%&'&%#\"! \037\036\035\034\033\033\032\031\031\030\030\030\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\030\030\030\031\032\032\033\033\034\035\036\037 !#$&(*-04;NNNNMLKJIIHGFFEDDCBBAA@@??>>==<<;;;::998887766655444333222211100000000///////////////00000.,+)(&%$#\"\"! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\034\035\035\036\037\037 !!\"#$%&('%$\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !#$&(*-05;MMMMNMLKJIHHGFEEDDCBBAA@@??>>==<<;;;::99888776665554433332221111000000000000////0000000010.-+)('&%$#\"!  \037\036\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !\"##$%'('%$#! \037\036\035\035\034\033\033\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\035\036\036\037!\"#%&(+-15<MMMMMMLKKJIHGGFEEDDCBBAA@@??>>==<<<;;::999887766655544433322221111001000000000000000001111/-+*('&%$#\"!! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\035\036\037\037  !\"#$%&'('&$#\" \037\036\036\035\034\033\033\032\032\031\031\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\036\037 !\"#%')+.16<LLLLLLMLKJJIHGGFEEDDCBBAA@@??>>==<<<;;::99988777665554443332222111111111000000000001111111/-,*)'&%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\030\030\030\030\030\030\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&')(&%#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\034\034\035\036\037 !\"$%')+.26=KKKKLLLLLKJIIHGGFEEDDCBBAA@@??>>===<<;;::9998877766555444333322221111111111111111111111222/.,*)('%$$#\"!  \037\037\036\036\035\035\034\034\033\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\033\034\034\035\035\035\036\036\037  !\"##$%&()(&%#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\025\025\025\025\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\025\026\026\026\026\027\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !#$&'),/27=KKKKKKKKLLKJIIHGGFEEDDCBBAA@@??>>===<<;;:::998877766655544433332222222211111111111112222220.,+)('&%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&'()('%$\"! \037\036\036\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\036\037\037 \"#$&(*,/27>JJJJJJJKKKKKJIHHGFFEEDCCBBAA@@??>>>==<<;;:::99888776665554443333322222222222111122222222320.-+*('&%$#\"\"!  \037\036\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\034\034\034\035\035\035\036\036\037  !\"\"#$%&'(*)'%$#\"  \037\036\035\034\034\033\032\032\031\031\031\030\030\030\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\031\031\031\032\032\033\034\034\035\036\037 !\"#%&(*,/38>IIIIJJJJJKKKJJIHHGFFEEDCCBBAA@@??>>>==<<;;:::9988877666555444433333232222222222222222233331/-,*)(&%$$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037  !\"##$%&')*)'&$#\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"#%'(*-038?IIIIIIIIJJJKKJJIHHGFFEEDCCBBAA@@???>>==<<;;:::998887776665554444333333332222222222233333331/-,*)('&%$#\"!!  \037\036\036\036\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\031\031\031\031\031\031\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037\037 !!\"#$%&'()*)(&%#\"! \037\036\035\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\034\034\035\036\036\037 !\"$%')+-048?HHHHHHHIIIJJJKJJIHHGFFEEDCCBBAA@@???>>==<<;;;::998887776665554444433333333333333333333344410.,+)('&%$#\"\"!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'()+*(&%$\"! \037\037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\032\032\033\033\034\034\035\036\037  \"#$%')+.149@GGGGHHHHHIIIJJKJIIHGGFFEEDCCBBAA@@@??>>==<<;;;::99988777666555544444444333333333333344444420.-+*('&%$$#\"!! \037\037\036\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\036\036\036\037\037 !!\"##$%&'(*+*('%$#\"! \037\036\035\035\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\025\025\025\025\025\025\026\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#$&')+.159@GGGGGGGGHHHIIJJKJIIHGGFFEEDDCBBAA@@@??>>==<<;;;::9998877766665555444444444443333444444445420/-+*)('&%$#\"\"!  \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"#$%%&()*+*)'%$#\"! \037\036\035\035\034\034\033\033\032\032\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\033\033\034\034\035\036\036\037 !\"#%&(*,.15:AFFFFFFFGGGHHHIIJKJIIHGGFFEEDDCBBAAA@@??>>==<<;;;::999888777666555554544444444444444444555531/-,*)('&%$##\"!! \037\037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$%&'()*,+)'&$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\037\037 !\"$%&(*,/25:AEEEEFFFFFGGGHHIIJJJIIHGGFFEEDDCBBBAA@@??>>==<<<;;::99988877766665555555554444444444555555531/.,+)('&%$$#\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\032\032\032\032\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\035\036\036\037\037  !!\"##$%&'()+,+)(&%#\"! \037\037\036\035\035\034\033\033\032\032\032\031\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\031\031\031\032\032\032\033\033\034\035\035\036\037 !\"#$%'(*,/26;BEEEEEEEEFFFGGHHIIJJJIHHGGFFEEDDCCBBAA@@??>>==<<<;;:::9988877776666555555555555555555555666310.,+*)'&&%$#\"\"!  \037\037\037\036\036\035\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$%%&'(*+,+*(&%$#\"! \037\036\035\035\034\034\033\033\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037 !\"#$%')+-/26;BDDDDDDDEEEFFFGGHIIJJJIHHGGFFEEDDCCBBAA@@??>>==<<<;;:::999888777666666665555555555555666666420.-+*)('&%$##\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !!\"##$%&'()*+-,*('%$#\"! \037\036\036\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037 !\"#$&')+-037<CDCCCDDDDDEEEFFGGHIIJJJIHHGGFFEEDDCCBBAA@@??>>==<<<;;:::99988877776666666666655556666666676420/-,*)('&%$$#\"\"!  \037\037\037\036\036\036\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\035\035\035\035\036\036\037\037\037  !!\"##$%&'()*,-,*('&$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\027\027\027\026\026\026\026\026\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\033\033\034\034\035\035\036\037\037 !\"#%&()+-037<CCCCCCCCCDDDEEEFGGHIIJJJIHHGGFFEEDDCCBBAA@@??>>==<<<;;:::9998888777767666666666666666677777531/-,+*('&%%$#\"\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%%&'()+,-,*)'&%#\"!  \037\036\035\035\034\034\033\033\032\032\032\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\035\036\037 !\"#$%&(*,.047<BBBBBBBCCCCDDDEEFFGHIIJJIIHHGGFFEEDDCCBBAA@@??>>===<<;;;::999988877777777766666666677777787531/.,+*)('&%$##\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\033\033\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037\037  !!\"##$%&'')*+,.-+)(&%$#\"! \037\036\036\035\034\034\033\033\033\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\033\033\033\034\035\035\036\036\037 !\"#$%'(*,.148=BBBABBBBBBCCCDDEEFFGHIJKJIIHHGGFFEEDDCCBBAA@@??>>===<<;;;:::99988887777777777777777777778885310.-+*)('&%$$#\"\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037  !!\"\"#$$%&'()*+-.-+)(&%$#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%'(*,/148=AAAAAAAAABBBCCCDDEFFGHIJKJIIHHGGFFEEDDCCBBAA@@??>>===<<;;;:::9999888888877777777777778888886420.-,*)('&%%$##\"!!   \037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"#$%%&'()*,-.-+*('%$#\"!  \037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\032\032\032\033\033\033\034\034\035\036\036\037  !\"#$&')+-/259>@@@@@@@AAAABBBCCDDEFFGHIJKJIIHHGGFFEEDCCCBBAA@@??>>===<<;;;:::999988888888888777788888889986420/-,+*)('&%$##\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\037\037\037  !!\"\"##$%&&'()+,-/.,*('&$#\"!! \037\036\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\036\037 !\"#$%&')+-/259>@@@@@@@@@@AAABBCCDDEFGGHIJJJIIHHGGFFEDDCCBBBAA@@??>>===<<<;;;:::9999899888888888888888999997431/.,+*)('&%$$#\"\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037   !!\"\"#$$%&'()*+,-/.,*)'&%$#\"! \037\037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\027\027\027\030\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\037\037 !\"#$%&()+-0259??????????@@@AAABBCDDEFGHIJKJJIIHHGGFEEDDCCBBBAA@@??>>===<<<;;;::::99999999888888888999999:97531/.-+*)('&%%$##\"\"!!  \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"##$%%&'()*+,./.,+)(&%$#\"! \037\037\036\036\035\034\034\034\033\033\032\032\032\032\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\032\033\033\034\034\034\035\036\036\037  !\"#$%'(*+.036:?>>>>>>>????@@@AABBCDDEFGHIJKJJIIHHGGFEEDDCCBBAAA@@??>>>==<<<;;;::::99999999999999999999::::75310.-,*)(''&%$$#\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037   !!\"\"##$%&&'()*,-.0/-+)(&%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037 !!\"#$&'(*,.036:>>>>>>>>>>>???@@AABBCDDEFGHIKKJJIIHHGFFEEDDCCBBAAA@@??>>>==<<<;;;;:::::::9999999999999::::::86420/-,+*)('&%%$##\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037\037  !!\"\"##$$%&'()*+,-.0/-+*('&$#\"!! \037\037\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\032\032\032\032\033\033\034\034\034\035\035\036\037\037 !\"#$%&')*,.137;=========>>>>???@@ABBCDEEFGIJKKJJIIHHGFFEEDDCCBBAAA@@??>>>===<<<;;;;::::::::::9999:::::::;;:86421/.,+*)('&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037\037   !!\"\"##$%%&'()*+,-/0/-,*('&%$#\"! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !\"#$%&')+,.147;=<<<<<<=====>>>??@@ABBCDEFGHIJKKJJIIHGGFFEEDDCCBBAAA@@??>>>===<<<;;;;:;;:::::::::::::::;;;;;86431/.-+*)(''&%$$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\035\035\036\036\036\036\036\036\036\037\037\037\037   !!\"\"\"#$$%&&'()*+,./10.,*)'&%$#\"!  \037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"#$%&()+-/147<<<<<<<<<<<<===>>??@@ABBCDEFGHIKKKJJIIHGGFFEEDDCCBBAAA@@??>>>===<<<<;;;;;;;;:::::::::;;;;;;<;975310.-,+*)('&%%$##\"\"!!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037   !!!\"\"##$%%&'(()*,-./10.,*)(&%$#\"!! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\030\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\035\035\036\036\037\037 !!\"#$%'()+-/248;;;;;;;;;;<<<<===>>?@@ABBCDEFGIJKKKJJIHHGGFFEEDDCCBBAAA@@???>>====<<<<;<;;;;;;;;;;;;;;;;;<<<<975320/-,+*)('&&%$$##\"\"!!   \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"\"#$$%%&'()*+,-.010.,+)('%$#\"\"! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !\"##%&'(*+-/258;;;;:::;;;;;;<<<==>>??@ABCCDFGHIKLKKJJIHHGGFFEEDDCCBBAAA@@???>>>===<<<<<<<;;;;;;;;;;;;;<<<<<<:75420/.,+*)(''&%$$##\"\"!!!   \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!!\"\"##$$%&&'()*+,-/021/-+)('&%$#\"!  \037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\033\034\034\035\035\035\036\036\037  !\"#$%&')*,.0259:::::::::::;;;;<<==>>??@ABCDEFGHJKLKKJIIHHGGFFEEDDCCBBAAA@@???>>>====<<<<<<<<<<;;;;<<<<<<<==<:86421/.-+*)(('&%%$$##\"\"!!!   \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037    !!\"\"\"##$%%&''()*+,./021/-+*('&%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037 !!\"#$%&')*,.0359:999999999::::;;;<<==>??@ABCDEFHIJLLKKJIIHHGGFFEEDDCCBBAAA@@???>>>=======<<<<<<<<<<<<<<<=====:86431/.-,+*)('&&%$$##\"\"!!!    \037\037\037\037\037\037\037\036\036\036\036\036\036\036\036\037\037\037\037\037\037\037    !!!\"\"##$$%&&'()*+,-./121/-,*)'&%$#\"\"! \037\037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037 !\"\"#$%&()+,.03699999999999999:::;;<<==>?@@ABCDFGHJKLLKJJIIHHGGFFEEDDCCBBAAA@@???>>>>========<<<<<<<<<======>=;965310.-,+*)(''&%%$###\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"\"##$$%&&'()*+,-.01320.,*)(&%$##\"!  \037\037\036\036\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037  !\"##$&'()+-/1369888888888889999::;;<<==>?@AACDEFGIJLLLKJJIIHHGGFFEEDDCCBBAAA@@@???>>>>=>=================>>>>;975320/-,+*)(('&%%$$##\"\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!!\"\"##$$%%&''()*+,-/01320.,+)('&%$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\034\034\034\034\035\035\036\036\037\037 !!\"#$%&'(*+-/147888777777788888999::;;<==>?@ABCDEGHJKMLKKJJIIHHGGFFEEDDCCBBAAA@@@???>>>>>>>=============>>>>>><975420/.-,**)('&&%$$###\"\"!!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%&&'())*+,./02320.,+)('&%$#\"!! \037\037\036\036\036\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037 !!\"#$%&'(*+-/1477777777777777888899::;;<==>?@ABCDFGIJLMLKKJJIIHHGGFFEEDDCCBBBAA@@@????>>>>>>>>>>====>>>>>>>???<:86421/.-,+*)(''&%%$$##\"\"\"!!!      \037\037\037\037\037\037\037\037\037\037\037\037      !!!\"\"\"##$$%%&''()*+,-./12430/-+*('&%$#\"\"!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&')*,./24776666666666777778899::;;<=>>?@ABDEFHIKMLLKKJJIIHHGGFFEEDDCCBBBAA@@@@??????>>>>>>>>>>>>>>>?????<:864310.-,+*)(('&&%$$###\"\"\"!!!       \037\037\037\037\037\037\037\037       !!!!\"\"###$$%%&''()*+,-.012431/-+*)'&%$##\"!! \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037 !!\"#$$%'()*,.02566666555566666677788899:;;<=>?@ABCDFGIJLMLLKKJJIIHGGFFEEEDDCCBBBAAA@@@????????>>>>>>>>>??????@?=:865310/-,+*))('&&%%$$##\"\"\"!!!!                    !!!!\"\"\"##$$%%&&'())*+,-/013431/-,*)('&%$#\"!!  \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !!\"#$%&'()+,.025655555555555556667778899:;;<=>?@ABCEFHIKMMLLKKJJIHHGGFFEEEDDCCBBBAAA@@@@?@?????????????????@@@@=;975320/.-,+*)(''&%%$$###\"\"\"!!!!!                 !!!!\"\"\"###$$%%&''()*++-./023541/.,+)('&%$#\"\"!  \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\032\032\032\032\032\032\032\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !\"\"#$%&'(*+-.0355554444444445555666778899:;;<=>?@ACDEGIJLMMLLKKJIIHHGGFFEEEDDCCBBBAAAA@@@@@@?????????????@@@@A@>;975421/.-,+*)(('&&%%$$###\"\"\"!!!!!              !!!!!\"\"\"\"##$$%%&&'(()*+,-./0235420.,+)('&%$##\"!! \037\037\036\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037 !!\"##$%&'(*+-/13544444444444444455566677899:;<<=>?ABCEFHIKMMMLLKJJIIHHGGFFEEEDDCCCBBBAAA@@@@@@@@@@????@@@@@@@AAA><9764210.-,+*))(''&%%$$###\"\"\"\"!!!!!            !!!!!\"\"\"\"###$$%%&&'())*+,-./1245420.-+*('&%$$#\"!!  \037\037\036\036\035\035\035\035\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\035\036\036\037\037  !!\"#$$%&()*,-/134443333333333344445556677899:;<=>?@ABDEGIJLNMMLLKJJIIHHGGFFEEEDDCCCBBBAAAAAA@@@@@@@@@@@@@@@AAAAA><:864310/.,++*)(''&&%%$$###\"\"\"\"!!!!!!!      !!!!!!!!\"\"\"###$$%%&&''()**+,-/01246520/-+*)(&&%$#\"\"!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\036\037\037  !\"\"#$%&'()*,-/1433333222222333333444556677899:;<=>?@BCDFHJLNNMMLKKJJIIHHGGFFEEEDDCCCBBBAAAAAAAA@@@@@@@@@A@@@@@").append("@@?<:865320/.-,+*)(('&&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!!!\"\"\"\"###$$%%&&'(()*+,-./01346531/-,*)('&%$#\"\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\035\036\036\037\037  !!\"\"#$%&'()+,.023322222222222222333344455677899:;<=>?ABDEGIKMNNMLLKKJJIIHHGGFFEEEDDCCCBBBBABAAAAAAAAA@@@@@????????=:875320/.-,+*))(''&&%%$$###\"\"\"\"\"!!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%%&''())*+,-./02356531/-,+)('&%$##\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"##$%&'(*+,.0222222111111111222223334455677899:;<=?@ACDFHJLNNMMLLKKJJIIHHGGFFEEEDDCCCCBBBBBBAAAAA@@?????>>>>>>>>=;9754210.-,+**)(('&&%%$$$###\"\"\"\"\"!!!!!!!!!!!!!!\"\"\"\"\"###$$$%%&&''()**+,-./12357631/.,+)('&%$$#\"\"!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037\037  !\"\"#$$%&')*+-.0221111111111111111222233445566789:;<=>?@BCEGIKMONMMLLKKJJIIHHGGFFEEEDDDCCCBBBBBBA@@@??>>>>>=========;9764210/.-,+*)((''&&%%$$####\"\"\"\"\"\"!!!!!!!!!!\"\"\"\"\"\"####$$%%%&&'(()*++,-/0124576420.,+*)'&&%$#\"\"!!  \037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%&'()*+-/01111000000000000011112233345567789:;<=>@ACDFHJLONNMMLLKKJJIIHHGGFFEEEDDDCCCCCBAA@???>>=====<<<<<<<<=<9864310/.-,+*))(''&&%%$$$####\"\"\"\"\"\"\"!!!!!!\"\"\"\"\"\"\"####$$$%%&&''())*+,-./0134676420.-+*)('&%$##\"!!  \037\037\037\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037\037  !!\"##$%&'()*,-/11000000///////000001112223344567789:;<>?@BCEGIKNONNMMLLKJJIIHHHGGFFEEEDDDDCCAA@@?>>>===<<<<;;;;;;;<<<:865320/.-,+**)(('&&%%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%%&&'(()**+,-./0234687420/-,*)('&%$$#\"\"!!  \037\037\036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\036\036\036\036\037\037   !\"\"#$$%&'()+,-/0000///////////////000111223344567789:<=>?ABDFHJMOONNMMLKKJJIIHHHGGFFFEEEDDCB@@?>>===<<<;;;;;::::::;;;:8753210.-,,+*)((''&&%%$$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%&&''(()*++,-./1235687521/-,+)('&%%$#\"\"!!  \037\037\037\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\037\037\037  !!\"\"#$%%&'(*+,./00////...........////0001122334456789:;<=?@BCEGILNOONNMLLKKJJIIHHHGGFFFEEDCBA??>==<<<;;;::::::9999:::::9754210/.-,+*))(''&&%%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%%&&''())*+,-./01245787531/.,+*('&&%$##\"!!   \037\037\036\036\036\036\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\036\036\036\036\037\037   !!\"##$%&'()*+,.0///.................///000112234456789:;<>?ABDFHKMPOONMMLLKKJJIIHHHGGFFDCCBA@>>=<<;;;:::9999999999999999764310/.-,+**)((''&&%%%$$$######\"\"\"\"\"\"\"\"\"\"######$$$%%%&&''(()**+,-./01245798531/.,+*)('&%$##\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\035\035\034\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"##$%&'()*+-.//....-------------....///00112234556789:<=>@BCEGJLOPOONMMLLKKJJIIHHHGEDCBAA@?==<;;:::9998888888888888889764320/.-,++*))(''&&&%%$$$$#######\"\"\"\"\"\"#######$$$$%%%&&''(()*++,-./013467986310.-+*)('&%$$#\"\"!!   \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037   !!\"\"#$$%&'()*,-/...------,,,,,,,------...//0011223455678:;<>?ABDFIKNPPONNMMLLKKJJIIHGFDCBA@??><<;::99988877777777777777788653210.--,+*))((''&&%%%$$$$##################$$$$%%%&&''(())*+,,-./123468986420.-,*)('&%%$##\"\"!!  \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037  !!!\"##$%%&'()+,-..----,,,,,,,,,,,,,,,----..///001223456789:;=>@BCEHJMOPOONNMMLLKKJJHGFDCBA@?>>=;;:9998877776666666666666777654210/.-,+**)((''&&&%%%$$$$################$$$$%%%&&&''(()**+,-./0123568:96420/-,+)('&&%$##\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037   !!\"\"##$%&''(*+,.---,,,,,+++++++++++,,,,----..//001223456789:<=?ABDGILNPPOONNMMLLKJHGFDCBA@?>==<::99887766665555555555555666654210/.-,++*))((''&&%%%%$$$$$#############$$$$$%%%&&''(())*++,-./0124578:97421/-,+*)('&%$$#\"\"!!   \037\037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037\037   !!\"\"#$$%&'()*+,--,,,,+++++++++++++++++,,,---..//00123345678:;<>@BDFHJMPPPOONNMLLJIGFDCBA@?>=<<;9988776665555444444444445555664320/.-,,+**)((''&&&%%%$$$$$$##########$$$$$$%%%&&&''(())*+,,-./0134579:97531/.,+*)('&%%$##\"\"!!   \037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\037\037\037   !!\"\"##$%%&'()*+--,,,++++*************++++,,,---.//00123345689:<=?ACEGILOQPPOONMMKIHFECBA@?>=<;;:88776655544443333333333344445543210/.-,+**))((''&&&%%%$$$$$$$$####$$$$$$$$%%%%&&'''(()**+,--./0234679;:7531/.-+*)('&%%$##\"\"!!   \037\037\037\037\036\036\036\036\036\036\036\036\035\035\035\035\035\036\036\036\036\036\036\036\036\037\037\037\037   !!\"\"##$%&&'()*,,,,+++******)))))))******+++,,,--../00123456789;<>@BDFHKNQQPPONMKJHFECBA@?>=<;::977665544433333222222222333334443210/.-,++*))((''&&&%%%%$$$$$$$$$$$$$$$$$$%%%%&&&''(())*++,-./01235689;:75310.-,*)('&&%$$#\"\"\"!!   \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037   !!!\"\"#$$%&''()+,,+++****)))))))))))))))****+++,,--../0012345679:;=?ACEGJMPQQPONLJHGEDBA@?>=<;:99866554433332222221111122222233344210/.-,,+**)(((''&&&%%%%$$$$$$$$$$$$$$$$%%%%&&&'''(()**+,,-./0123568:;:86420.-,+*)('&%$$##\"\"!!    \037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!\"\"##$$%&'()*+,++***))))))((((((((())))))***++,,--../0012345689;<>@BDFILORQPOLKIGEDBA@?>=<;:988755443332221111111111111111122233310/.--,+**))(('''&&&%%%%%$$$$$$$$$$$$%%%%%&&&&''(())**+,--./0124578:<;86420/-,+*)('&%%$##\"\"!!!   \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037   !!!\"\"##$%%&'()*+++**))))((((((((((((((((())))**+++,,-../011235678:;=?ACEHKNQROMKIGFDCA@?><;;:98776443322211110000000000000001111223210/.-,++**)(((''&&&&%%%%%%$$$$$$$$$%%%%%&&&&'''(())*++,-.//1234578:<;86421/.,+*)('&&%$$##\"\"!!    \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037    !!\"\"##$$%&&'()*+**))))(((('''''''''''''(((()))***++,,-../012345679:<>@BDGJMPPNLJHFDCA@?><;:998766533222110000//////////////00001112210/.-,,+**))(('''&&&&%%%%%%%%$$$%%%%%%%%&&&'''(())**+,,-./01234679;<;96431/.-+*)(''&%$$##\"\"!!!    \037\037\037\037\037\037\036\036\036\036\036\036\036\036\036\037\037\037\037\037\037    !!!\"\"##$$%&''()***)))(((''''''''&&&''''''''((()))**++,,-../01234578:;=?ACFHKOOMJIGECB@?><;:9887655422111000////............/////0001110/.--,++*))((('''&&&&%%%%%%%%%%%%%%%%%&&&&''((())**+,--./01235689;=<975310.-,*)(('&%%$##\"\"\"!!!   \037\037\037\037\037\037\037\037\036\036\036\036\036\037\037\037\037\037\037\037\037   !!!\"\"\"##$%%&'()**)))(((''''&&&&&&&&&&&&&&&''''((()))**+,,-../01235679:<>@BEGJMMKIGEDBA?>=;:98776544321100///.....----------.....///00010//.-,++**))(('''&&&&&%%%%%%%%%%%%%%&&&&&'''(())**++,-../01245689;=<975310.-,+*)('&&%$$##\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"##$$%&&'()*))((('''&&&&&&%%%%%%%%%&&&&&&'''((())**+,,-.//01345689;=?ADFILLJHFDBA?>=;:9876654332100//....------,,,,,,,------...//0000/.-,,+**))((('''&&&&&%%%%%%%%%%%%&&&&&'''((())**+,,-./01234578:<=<:75320/-,+*)('&&%$$##\"\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"\"##$$%&&'()))(('''&&&&%%%%%%%%%%%%%%%%%&&&&'''(())**+,,-./01234579:<>@CEHKKIGECA@>=<:987655432210//...---,,,,,,,,,,,,,,,,,,---...//00/.--,++**))((''''&&&&&&%%%%%%%%&&&&&&''''(())**++,--./01234678:<>=:86420/.,+*)(''&%%$$##\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"##$$%%&''())((''&&&&%%%%%$$$$$$$$$$$%%%%%&&&&''(())**+,,-./01235689;=?ADGJIGECB@>=<:987654432110/...--,,,,++++++++++++++++,,,,---..///..-,,+**))(((''''&&&&&&&&%%&&&&&&&&''''((())**++,-../01234679:<>=:86421/.-+*)(('&%%$$##\"\"\"!!!      \037\037\037\037\037\037\037\037\037\037\037\037     !!!\"\"\"##$$%%&'()(('''&&&%%%$$$$$$$$$$$$$$$$$$$%%%&&&'''(()**+,,-./0134578:<>@CFIHFDB@?=<;987654332110/.---,,,++++**************++++,,,---..//.-,,++**))(((''''&&&&&&&&&&&&&&&&''''((()))**+,,-.//01245689;<>=;86421/.-,+*)('&&%$$###\"\"!!!!      \037\037\037\037\037\037\037\037\037      !!!!\"\"###$$%&&'(((''&&&%%%$$$$$#############$$$$$%%%&&&''(()**+,--./1234679;=?BEHGECA?><;987654322100/.--,,+++******)))))))))))*****+++,,--../.--,++**))((((''''&&&&&&&&&&&&&&'''''((())**++,--./012345689;=?>;964310.-,+*)(''&%%$$##\"\"\"!!!!        \037\037\037        !!!!\"\"\"##$$%%&''((''&&%%%$$$$#######\"\"\"\"\"#######$$$$%%%&&''(()**+,-./0124578:<>ACFECA@><;:8765432110//..,,+++***))))))(((((((())))))***+++,,--...-,,+***))((('''''&&&&&&&&&&&&'''''((()))**+,,-../01234578:;=?>;975310/-,+*)(''&%%$$###\"\"!!!!!                  !!!!\"\"\"##$$%%&'((''&&%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%&&&'(()**+,-./0134679;=@BEDB@>=;:8765432100/..--++***))))(((((((((((((((((())))**+++,,-..-,,++**)))(((''''''&&&&&&&&''''''((()))**++,,-.//01234678:<=?><975320/.,+*))('&&%%$$##\"\"\"!!!!                 !!!!\"\"\"##$$$%&&'('&&%%%$$####\"\"\"\"\"\"!!!!!!!!!\"\"\"\"\"\"####$$$%%&&''()**+,-./023568:<?ADCA?=<:9765432100/.--,,***)))((((''''''''''''''''(((()))**++,,----,++***))((((''''''''''''''''''(((()))**++,--./001235679:<>@?<975420/.-,+*)('&&%%$$###\"\"\"!!!!!              !!!!\"\"\"\"##$$%%&&''&&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!\"\"\"\"####$$%%&&''()**+,-.0124579;>@CA?><:986543210//.--,++*))((((''''&&&&&&&&&&&&&'''''((()))**++,,--,,++**)))((((''''''''''''''''(((()))**++,,-../012345689;<>@?<:86421/.-,+*)(''&%%$$###\"\"\"!!!!!!           !!!!!!\"\"\"###$$%%&''&&%%$$###\"\"\"!!!!!!           !!!!!!\"\"\"###$$%%&&''()*+,-./013568:<?B@><;986543210/..-,,++*)((('''&&&&&&%%%%%%%%%&&&&&&''''((())**+,,--,++***)))((((''''''''''''''(((()))***++,,-../012345689;=>@?=:864210.-,+*)(('&&%%$$###\"\"\"!!!!!!         !!!!!!\"\"\"\"##$$%%&&'&&%$$$##\"\"\"!!!!                   !!!!\"\"\"###$$%%&''()*+,-./124579;>A?=;:87543210/.--,++**)((''&&&&%%%%%%%%%%%%%%%%%%%&&&&'''(())**++,-,,++**)))(((((('''''''''''((((()))**++,,--.//01234578:;=?A@=:864310.-,+*))('&&%%$$###\"\"\"\"!!!!!!!!!  !!!!!!!!\"\"\"\"###$$%%&&&&%$$##\"\"\"!!!!     \037\037\037\037\037\037\037\037\037\037\037\037\037      !!!\"\"\"##$$%%&''()*+,-.013468:=?=<:87643210/.--,++*))(''&&&%%%%$$$$$$$$$$$$$$$$$%%%%&&&'''(())*++,,,++***)))((((((''''''''(((((()))***++,,-../011235678:<=?A@=;865310/.,+**)(''&%%$$$###\"\"\"\"!!!!!!!!!!!!!!!!!\"\"\"\"###$$$%%&&&%$$##\"\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!\"\"##$$%%&'(()*+,./023579<><:97643210/.-,,+**))((&&&%%%$$$$$##############$$$$$%%%&&'''(()**+,,,++***)))(((((((((((((((((())))**++,,--../012345679:<>?A@=;975320/.-,+*)(('&&%%$$###\"\"\"\"\"!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&&%$$##\"\"!!!   \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037    !!\"\"\"#$$%%&'())*,-./12468;=;98653210/.-,++*))((''%%%$$$$######\"\"\"\"\"\"\"\"\"#######$$$%%%&&''(()**+,,++***))))(((((((((((((((())))***++,,--.//012345689;<>@BA>;975321/.-,+*)(('&&%%$$$###\"\"\"\"\"!!!!!!!!!!!!!\"\"\"\"\"###$$$%%&&%$$#\"\"!!!   \037\037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\035\036\036\036\036\036\036\036\037\037\037\037   !!!\"\"##$%%&'()*+,-/023579<:8654210/.-,+**)((''&&%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%%&&''())*+,,++***))))(((((((((((((())))***+++,,-../0012345789;<>@BA><975421/.-,+*))(''&&%%$$####\"\"\"\"\"!!!!!!!!!!!\"\"\"\"\"####$$%%&&%$##\"\"!!   \037\037\037\036\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\036\037\037\037   !!\"\"##$%%&'()*+,./12468:9754210/.-,+**)(('&&&%$$###\"\"\"\"!!!!!!!!!!!!!!!!!\"\"\"\"\"###$$%%&&''(()*+,+++***)))))(((((((((()))))****++,,--.//012335678:;=>@BA><:864210.-,+**)(''&&%%$$$###\"\"\"\"\"\"\"!!!!!!!\"\"\"\"\"\"\"###$$$%%&%$##\"\"!!  \037\037\037\036\036\036\036\035\035\035\035\035\035\035\034\034\034\034\034\034\034\034\034\035\035\035\035\035\035\035\036\036\036\036\037\037\037  !!\"\"##$%%&'()*+-.013579764310/.-,+*))(''&&%%$##\"\"\"\"!!!!!             !!!!!!\"\"\"###$$%%&&'(()*+,++****))))))((((((())))))***+++,,--.//012345678:;=?ACB?<:864310/.-,+*)(('&&%%%$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$%%%%$##\"\"!!  \037\037\036\036\036\036\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\037\037\037  !!\"##$%&&'()+,-/0246864320/.-,+*)(('&&%%$$#\"\"\"!!!!       \037\037\037\037\037\037\037       !!!!\"\"\"###$$%&&''()*++++***))))))))))))))))))***+++,,--../0012345679:<=?ACB?=:865320/.-,+*))(''&&%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"####$$$%%%$##\"!!  \037\037\037\036\036\036\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"##$%&'()*+,./13575320/.-,+*)(('&&%$$$##\"!!!    \037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037\037    !!!!\"\"##$$%%&''()*+++****))))))))))))))))****+++,,--.//0112345689:<>?ACB?=;875320/.-,+*))(''&&%%%$$$####\"\"\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%$##\"!!  \037\037\036\036\036\035\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037  !!\"##$%&'()*+-.0245421/.-,+*)(''&%%$$##\"\"!    \037\037\037\037\037\036\036\036\036\036\036\036\036\036\036\036\036\036\036\036\037\037\037\037\037    !!!\"\"##$$%&&'()*+++*****)))))))))))))****+++,,--..//0123456789;<>@BDC@=;975321/.-,+**)((''&&%%$$$#####\"\"\"\"\"\"\"\"\"\"\"\"\"#####$$$%%$$#\"!!  \037\037\036\036\035\035\035\034\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\033\034\034\034\034\035\035\035\036\036\036\037\037 !!\"##$%&'()+,./134310.-,+*)('&&%$$##\"\"!!  \037\037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\035\035\035\036\036\036\036\036\036\036\037\037\037\037   !!\"\"##$$%&&'()*+++*****))))))))))*****+++,,,--../0012345678:;=>@BDC@=;9754210/-,,+*)((''&&%%%$$$#####\"\"\"\"\"\"\"\"\"\"\"#####$$$$%%$#\"!! \037\037\036\036\036\035\035\034\034\034\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\035\035\035\036\036\037\037  !\"##$%&'(*+-.02310.-,+*)('&&%$$##\"\"!! \037\037\037\036\036\036\036\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\035\036\036\036\036\037\037\037   !!\"\"##$%&&'()*+++******))))))******++++,,--..//0112345679:;=?@BDC@>;9764310/.-,+*))(''&&%%%$$$$######\"\"\"\"\"\"\"######$$$$%%$#\"!! \037\037\036\036\035\035\035\034\034\033\033\033\033\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\036\036\037\037  !\"##$%&()*,-/120/-,+*)('&%%$##\"\"!!   \036\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\036\036\036\037\037\037   !!\"##$%%&'()*+++*****************+++,,,--..//0122345689:<=?ACEDA><:864310/.-,+**)((''&&%%%$$$###################$$$%%$#\"!! \037\037\036\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\033\033\033\033\034\034\035\035\035\036\037\037  !\"#$%&'()+,.01/.,+*)('&%%$##\"!!   \037\037\036\035\035\035\035\034\034\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\034\034\035\035\035\035\036\036\036\037\037  !!\"\"#$%%&'()*+++***************+++,,,---.//00123456789:<>?ACEDA><:865320/.-,++*)((''&&%%%$$$$#################$$$$%$#\"!! \037\037\036\036\035\035\034\034\033\033\033\032\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"#$%&'(*+-//.-+*)('&%$$#\"\"!!  \037\037\036\036\035\035\034\034\034\034\034\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\033\034\034\034\034\034\035\035\035\036\036\037\037\037  !\"\"#$$%&'()++++************+++++,,,--..//01123456789;<>@ACEDA?<:865321/.-,,+*))(''&&&%%%$$$$###############$$$$%$#\"\"! \037\037\036\035\035\034\034\033\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\027\027\027\027\027\027\030\030\030\030\030\030\030\030\030\031\031\031\031\032\032\032\033\033\033\034\034\035\035\036\036\037  !\"#$%&')*,..-+*)('&%$##\"!!  \037\037\036\036\036\035\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\034\034\034\035\035\035\036\036\037\037  !!\"#$$%&'(*++++**********+++++,,,---../00122345678:;=>@BDFDB?=:8754210/.-,+*))((''&&%%%$$$$$#############$$$$$%$#\"! \037\037\036\035\035\034\034\033\033\032\032\032\031\031\031\031\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\036\037  !\"#$%'()+--,*)('&%$##\"!! \037\037\036\036\036\035\035\035\034\033\033\033\033\032\032\032\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\032\032\033\033\033\033\034\034\034\035\035\035\036\036\037\037 !!\"#$$%&')*+++++******++++++,,,,--..//00123345679:;=>@BDFEB?=;9754210/.-,+**)((''&&&%%%$$$$$$##########$$$$$%$#\"! \037\036\036\035\035\034\034\033\033\032\032\031\031\031\031\030\030\030\030\027\027\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\032\032\032\033\033\034\034\035\036\036\037  !\"#$&'(*,,+)('&%$#\"\"!  \037\037\036\036\035\035\034\034\034\033\033\032\032\032\032\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\031\032\032\032\032\032\033\033\033\034\034\034\035\035\036\036\037\037  !\"#$$%'()*+++++++++++++++,,,,---..//01123456789:<=?@BDGEB@=;9764310/.-,++*))((''&&%%%%$$$$$$#######$$$$$$%$#\"! \037\036\036\035\035\034\033\033\032\032\032\031\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"\"$%&')++)('&%$#\"\"! \037\037\036\036\035\035\035\034\034\033\033\032\032\032\031\031\031\031\031\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\030\031\031\031\031\031\031\032\032\032\033\033\033\034\034\034\035\035\036\036\037  !\"#$$&'()++++++++++++++,,,,---..//001223456789;<=?ACEGEC@=;9764320/.-,,+*))((''&&&%%%%$$$$$$$$$$$$$$$$$$$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\031\030\030\030\027\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\027\027\030\030\030\030\031\031\032\032\033\033\034\034\035\035\036\037 !\"#$%&(**('&%$#\"!! \037\037\036\036\035\035\034\034\033\033\033\032\031\031\031\031\030\030\030\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\030\030\031\031\031\031\032\032\032\032\033\033\034\034\035\035\036\036\037\037 !\"#$%&'(*++++++++++++,,,,,---..//001233456789;<>?ACEGFC@><:865321/.--,+**)(('''&&%%%%$$$$$$$$$$$$$$$$$%#\"! \037\037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\027\027\027\030\030\030\031\031\031\032\032\033\033\034\035\035\036\037 !\"#$&'))'&%$#\"!  \037\036\036\035\035\034\034\033\033\033\032\032\032\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\030\031\031\031\031\032\032\032\033\033\034\034\035\035\036\037\037 !\"#$%&'(*++++++++++,,,,,---..//001123445679:;<>@ACEHFC@><:8653210/.-,+**))((''&&&%%%%$$$$$$$$$$$$$$$%$#! \037\037\036\035\034\034\033\032\032\031\031\031\030\030\027\027\027\027\026\026\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"#%&('&%$#\"!  \037\036\036\035\034\034\033\033\033\032\032\032\031\031\030\030\030\027\027\027\027\027\027\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\026\027\027\027\027\027\027\030\030\030\030\031\031\031\032\032\033\033\033\034\035\035\036\036\037 !\"#$%&')*,,+++++,,,,,,---...//001223456789:;=>@BDFHFCA><:8754210/.-,++*))(('''&&&%%%%$$$$$$$$$$$$$%$#\"! \037\036\035\034\034\033\032\032\031\031\030\030\030\027\027\027\026\026\026\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\026\027\027\027\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"$%&&%$#\"! \037\037\036\035\035\034\034\033\033\032\032\031\031\031\031\030\027\027\027\027\026\026\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\026\027\027\027\027\027\030\030\030\031\031\031\032\032\033\033\034\034\035\036\036\037 !\"#$%&()+,,,,,,,,,,,----..///011233456789:<=?@BDFHGDA?<:9754210/.-,,+**)(((''&&&%%%%%%$$$$$$$$$%%%#\"! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\033\033\034\035\035\036\037 !#$%%$#\"! \037\036\036\035\034\034\033\033\032\032\032\031\031\030\030\030\030\027\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\027\027\027\027\030\030\030\031\031\031\032\032\033\034\034\035\035\036\037 !\"#$%'(*,,,,,,,,,,----...//0011234456789;<=?@BDFIGDA?=;9754310/.--,+**))(('''&&&%%%%%%%$$$$$%%%%$\"! \037\036\035\034\033\033\032\032\031\030\030\027\027\027\026\026\026\025\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\030\030\031\031\032\032\033\034\035\036\036\037!\"#$$#\"! \037\036\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\025\025\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\025\025\026\026\026\026\027\027\027\030\030\030\031\031\032\032\033\033\034\035\035\036\037 !\"#$&')*,,,,,,,,----...///001223455678:;<>?ACEGIGDB?=;97643210/.-,++*))(('''&&&&%%%%%%%%%%%%%%$#! \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\026\025\025\025\024\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\024\024\024\024\025\025\025\025\026\026\027\027\030\030\031\031\032\032\033\034\035\036\037 !\"##\"! \037\036\035\035\034\033\033\032\032\031\031\030\030\030\027\027\027\026\026\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\025\025\025\026\026\026\027\027\027\030\030\030\031\031\032\033\033\034\034\035\036\037 !\"#$&')+,,,,,------...//0011233456789:;<>?ACEGIHEB?=;98653210/.-,++**))(('''&&&&%%%%%%%%%%%%%#\" \037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\024\025\025\025\026\026\026\027\027\030\030\031\032\032\033\034\035\036\037 !\"\"! \037\036\035\035\034\033\033\032\031\031\030\030\030\027\027\027\026\026\026\025\025\024\024\024\024\024\024\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\023\024\024\024\024\024\024\025\025\025\025\026\026\026\027\027\027\030\030\031\031\032\032\033\034\034\035\036\037 !\"#%&(*,,,-------...///0011233456789:;=>@ACEGJHEB@>;:8653210/.-,,+**))(('''&&&&&%%%%%%%%%%%$\"! \036\035\034\034\033\032\031\031\030\027\027\026\026\026\025\025\024\024\024\024\023\023\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\023\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036\037 !! \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\024\024\024\023\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\023\023\024\024\024\024\024\025\025\025\025\026\026\027\027\027\030\030\031\031\032\033\033\034\035\036\037 !\"$%')+--------....//00112234456789:<=>@BDEHJHEC@><:8654210/.--,++*))((('''&&&&&%%%%%%%%%$#! \037\036\034\034\033\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\026\027\030\030\031\031\032\033\034\035\036\037  \037\036\035\034\033\033\032\031\031\030\030\027\027\027\026\026\025\025\025\025\024\024\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\031\032\033\033\034\035\036\037 !#$&')+------....///00112334566789;<=?@BDFHJIFC@><:8754310/..-,++**))((''''&&&&&%%%%%%%%#\" \037\036\035\034\033\032\031\030\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\021\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\031\032\033\034\035\036\037\037\036\035\034\033\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\022\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\022\022\023\023\023\023\024\024\024\024\025\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036\037 \"#$&(*,----....///00112233456789:;<>?ABDFHHFDCA><:97643210/.-,,+**))((('''&&&&&&&%%%%%$\"!\037\036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\022\022\022\022\023\023\023\024\024\025\025\026\026\027\027\030\031\031\032\033\034\035\036\036\035\034\033\032\032\031\030\030\027\027\026\026\025\025\025\024\024\024\023\023\023\023\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\020\020\020\020\021\021\021\021\021\021\021\021\021\021\021\021\022\022\022\022\022\023\023\023\023\024\024\024\025\025\026\026\027\027\030\030\031\032\032\033\034\035\036\037 \"#%')+--.....////00112234456789:;<>?ACDFGECB@?=<;97643210/.-,,++*)))((''''&&&&&&&&&&%#! \036\035\034\033\032\031\030\030\027\026\026\025\025\024\024\023\023\023\022\022\022\021\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\021\021\021\021\022\022\022\022\023\023\024\024\024\025\025\026\027\027\030\031\031\032\033\034\035\035\034\033\032\031\031\030\030\027\026\026\026\025\025\024\024\024\023\023\023\023\022\022\021\021\021\021\021\021\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\020\021\021\021\021\021\021\022\022\022\022\022\023\023\023\024\024\024\025\025\026\026\027\027\030\031\031\032\033\034\035\036\037!\"$%'*,......///001112334556789:;=>@ACEGDBA?=<;:987653210/.--,++**))(((''''&&&&&&&&%#\" \036\035\034\033\032\031\030\027\027\026\026\025\024\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\021\022\022\022\022\023\023\024\024\025\025\026\026\027\030\031\031\032\033\034\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\021\021\020\020\020\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\020\020\020\021\021\021\021\021\022\022\022\022\023\023\023\024\024\024\025\025\026\027\027\030\031\031\032\033\034\035\036 !\"$&(+-....///0001122334567789;<=>@ACEDB@><;:97665433210/..-,,+**)))(('''''&&&&&&&$\" \037\035\034\033\032\031\030\027\027\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\023\024\024\025\026\026\027\030\031\031\032\033\033\032\031\030\030\027\026\026\025\025\024\024\024\023\023\023\022\022\022\021\021\021\021\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\020\020\021\021\021\021\022\022\022\023\023\023\024\024\025\025\026\026\027\030\030\031\032\033\034\035\036 !#%'),...////001112234456789:;<=?@BCCA?=;:976543321100///.-,,++**))((('''''&&&&&%#!\037\036\034\033\032\031\030\027\027\026\025\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\022\022\022\023\023\024\024\025\025\026\027\030\031\031\032\032\031\030\027\027\026\026\025\025\024\024\023\023\022\022\022\022\021\021\021\021\020\020\017\017\017\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\017\017\020\020\020\020\020\021\021\021\022\022\022\023\023\023\024\024\025\025\026\027\027\030\031\032\033\034\035\037 \"#%(*-.////0001122334556789:;<>?@BB@><:9765432110//..---,,,,++**)))((('''''&&&&$\" \036\035\033\032\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\021\021\021\022\022\023\023\023\024\025\025\026\027\030\031\032\031\030\027\027\026\025\025\024\024\023\023\022\022\022\021\021\021\021\020\020\020\020\020\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\017\017\017\017\017\017\020\020\020\020\021\021\021\021\022\022\023\023\024\024\025\025\026\027\027\030\031\032\033\034\035\037 \"$&),////00011122344566789:;=>?AB?=;986543210/..--,,++++*******)))((('''''''&%\" \037\035\034\032\031\030\027\026\026\025\024\024\023\023\022\022\021\021\020\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\021\022\022\023\023\024\024\025\026\027\030\031\030\027\026\026\025\024\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\016\016\016\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\016\017\017\017\017\017\020\020\020\020\021\021\021\022\022\023\023\024\024\025\026\026\027\030\031\032\033\034\036\037!#%'*-//000011223344567789:<=>@A><:8754310//.-,,++***)))((((((((((((((''''''&#!\037\035\034\033\031\030\027\026\026\025\024\023\023\022\022\021\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\024\025\026\027\030\027\026\025\025\024\024\023\023\022\022\021\021\021\020\020\020\020\017\017\017\017\016\016\016\r\r\r\r\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\r\r\r\r\r\016\016\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\022\023\024\024\025\025\026\027\030\031\032\033\034\036 !#&(+//00011122334556789:;<=>@>;9764310/.-,,+**))(((''''&&&&&&&&&&&''''''''$\" \036\034\033\031\030\027\026\025\025\024\023\023\022\022\021\021\020\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\023\024\025\026\027\026\025\024\024\023\023\022\022\021\021\021\020\020\020\017\017\017\017\016\016\016\016\016\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\r\016\016\016\016\017\017\017\017\020\020\020\021\021\022\022\023\023\024\025\025\026\027\030\031\032\033\035\036 \"$'*-000011223344566789:;<=?=:864310/.-,+**)(('''&&&%%%%%$$$$$$$%%%%%%&&&%# \036\035\033\032\030\027\026\025\025\024\023\022\022\021\021\020\020\020\017\017\017\016\016\016\r\r\r\r\r\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\016\017\017\017\020\020\021\021\022\022\023\023\024\025\026\025\024\024\023\022\022\021\021\021\020\020\020\017\017\017\016\016\016\016\016\r\r\r\f\f\f\f\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\f\f\f\r\r\r\r\r\r\016\016\016\016\017\017\017\020\020\020\021\021\022\022\023\024\024\025\026\027\030\031\032\033\035\037 #%(+/00111223344567789:;<><:75320/.-,+*)(('&&%%%$$$#################$$$%$!\037\035\033\032\031\027\026\025\024\024\023\022\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\017\020\020\021\021\022\022\023\024\025\024\023\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\r\r\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\f\f\r\r\r\r\r\016\016\016\017\017\017\020\020\020\021\022\022\023\023\024\025\026\027\030\031\032\034\035\037!#&)-01112223345567889:;=<96421/.,+*)(('&&%%$$##\"\"\"\"!!!!!!!!!!!!!!!\"\"\"\"#\" \036\034\032\031\027\026\025\024\023\023\022\021\021\020\020\017\017\017\016\016\016\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\017\017\017\020\020\021\022\022\023\024\023\022\022\021\021\020\020\017\017\017\016\016\016\016\r\r\r\r\f\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\013\013\013\f\f\f\f\f\r\r\r\r\016\016\016\016\017\017\020\020\021\021\022\022\023\024\025\026\027\030\031\032\034\036 \"$'+/111223344566789:;<;86310.,+*)('&&%$$##\"\"!!!     \037\037\037\037\037\037\037\037\037\037\037\037     ! \036\034\032\031\027\026\025\024\023\023\022\021\021\020\020\017\017\016\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\f\r\r\r\016\016\016\017\017\020\020\021\021\022\023\022\021\021\020\020\017\017\017\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\013\f\f\f\f\r\r\r\r\016\016\016\017\017\020\020\021\021\022\023\024\024\025\027\030\031\032\034\036 #&)-112223344566789:;:7520.-+*)('&%$##\"\"!!   \037\037\037\036\036\036\036\036\036\036\035\035\035\035\035\035\036\036\036\036\036\036\036\037\037\037\035\033\031\030\026\025\024\023\022\022\021\020\020\017\017\016\016\016\r\r\r\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\020\020\021\022\021\020\020\017\017\017\016\016\016\r\r\r\f\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\n\n\013\013\013\013\013\013\f\f\f\f\r\r\r\016\016\016\017\017\020\021\021\022\023\023\024\025\026\030\031\033\035\037!$'+022233445567789::741/-,*)'&%$$#\"!!  \037\037\036\036\036\035\035\035\035\035\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\034\035\035\035\035\035\033\031\030\026\025\024\023\022\021\021\020\017\017\016\016\016\r\r\f\f\f\f\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\017\020\020\021\020\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\n\n\013\013\013\013\013\f\f\f\r\r\r\016\016\017\017\020\020\021\022\022\023\024\025\026\030\031\033\035\037\"%)-222334456678999630.,*)'&%$#\"!! \037\037\036\036\035\035\035\034\034\034\033\033\033\033\033\033\032\032\032\032\032\032\032\032\032\032\032\032\032\033\033\033\033\033\034\032\030\026\025\024\023\022\021\020\020\017\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\r\r\r\016\016\017\017\020\017\017\016\016\r\r\r\f\f\f\013\013\013\013\013\n\n\n\n\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\f\f\r\r\016\016\017\017\020\021\021\022\023\024\025\026\030\031\033\036 #'+0233445566789952/-+)'&%$#\"! \037\037\036\035\035\034\034\034\033\033\033\032\032\032\032\031\031\031\031\031\031\031\031\030\030\030\030\031\031\031\031\031\031\031\031\031\032\032\030\027\025\024\023\022\021\020\020\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\n\013\013\013\013\f\f\r\r\r\016\016\017\016\016\r\r\r\f\f\f\013\013\013\013\n\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\t\n\n\n\n\013\013\013\013\f\f\f\r\r\016\016\017\017\020\021\022\023\024\025\026\030\032\034\036!%).33344556778841.,*(&%#\"! \037\037\036\035\035\034\033\033\033\032\032\031\031\031\031\030\030\030\030\030\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\027\030\030\030\030\027\025\024\023\022\021\020\017\017\016\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\t\n\n\n\n\013\013\013\f\f\r\r\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\n\013\013\013\f\f\f\r\r\016\016\017\020\021\022\023\024\025\026\030\032\034\037\"&+1344456678830-*(&%#\"! \037\036\035\034\034\033\033\032\032\031\031\030\030\030\027\027\027\027\026\026\026\026\026\026\026\025\025\025\025\025\025\025\025\025\025\025\026\026\026\026\026\026\026\025\024\023\021\021\020\017\016\016\r\r\f\f\013\013\013\n\n\n\t\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\b\t\t\t\t\t\n\n\n\013\013\013\f\f\r\r\f\f\f\013\013\013\n\n\n\n\t\t\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\b\t\t\t\t\n\n\n\n\013\013\013\f\f\r\r\016\017\017\020\021\022\024\025\026\030\032\035 $)/3445566772/,)'%#\" \037\036\035\034\034\033\032\032\031\031\030\030\027\027\027\026\026\026\025\025\025\025\025\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\024\025\025\024\022\021\020\017\017\016\r\r\f\f\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\n\n\n\013\013\013\f\f\f\013\013\n\n\n\n\t\t\t\t\b\b\b\b\b\b\b\b\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\007\b\b\b\b\b\b\t\t\t\t\n\n\n\013\013\013\f\f\r\016\016\017\020\021\022\023\025\027\031\033\036!&,344556771-*(%#\" \037\036\035\034\033\032\031\031\030\030\027\027\026\026\025\025\025\024\024\024\024\023\023\023\023\023\023\023\022\022\022\022\022\022\022\022\022\022\022\022\022\022\022\023\023\023\023\023\022\021\020\017\016\r\r\f\f\013\013\n\n\n\t\t\t\t\b\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\b\t\t\t\t\n\n\n\013\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\007\b\b\b\b\t\t\t\t\n\n\n\013\013\f\r\r\016\017\020\021\022\023\025\027\031\034\037#)04556660,)&$\" \037\035\034\033\032\031\031\030\027\027\026\026\025\025\024\024\024\023\023\023\022\022\022\022\022\022\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\021\020\017\016\r\f\f\013\013\n\n\n\t\t\t\b\b\b\b\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\007\007\b\b\b\b\t\t\t\n\n\n\n\t\t\t\t\b\b\b\b\007\007\007\007\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\b\t\t\t\n\n\n\013\013\f\r\r\016\017\020\022\023\025\027\031\035!&,45566/+'$\" \036\035\034\033\031\031\030\027\026\026\025\025\024\024\023\023\022\022\022\022\021\021\021\021\020\020\020\020\020\020\020\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\017\020\020\020\020\017\016\r\r\f\013\013\n\n\t\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\b\t\t\t\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\007\007\007\007\007\b\b\b\t\t\t\n\n\013\013\f\r\016\017\020\021\023\025\027\032\036\")1565.)%# \036\035\033\032\031\030\027\026\025\025\024\023\023\022\022\022\021\021\021\020\020\020\020\017\017\017\017\017\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\016\r\f\013\013\n\n\t\t\b\b\b\b\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\006\007\007\007\007\b\b\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\005\006\006\006\006\006\006\007\007\007\007\b\b\b\t\t\n\n\013\f\f\r\016\020\021\023\025\030\033\037%-54,'# \036\034\033\031\030\027\026\025\024\024\023\022\022\021\021\020\020\020\017\017\017\017\016\016\016\016\r\r\r\r\r\r\r\r\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\f\r\r\r\r\f\013\n\n\t\t\b\b\b\007\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\005\006\006\006\006\006\007\007\007\007\007\007\007\007\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\005\006\006\006\006\007\007\007\007\b\b\t\t\n\n\013\f\r\016\017\021\023\025\030\034!)3*%!\036\034\032\031\027\026\025\024\023\022\022\021\021\020\020\017\017\016\016\016\016\r\r\r\r\f\f\f\f\f\f\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\013\n\n\t\t\b\b\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\005\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\005\006\006\006\006\007\007\007\b\b\t\n\n\013\f\r\017\020\022\025\031\036%(\"\037\034\032\030\026\025\024\023\022\021\021\020\017\017\016\016\r\r\r\f\f\f\f\013\013\013\013\013\013\n\n\n\n\n\n\n\n\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\n\n\n\n\t\b\b\007\007\007\006\006\006\006\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\004\004\004\005\005\005\005\005\006\006\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\004\005\005\005\005\005\006\006\006\007\007\b\b\t\t\n\013\f\016\020\022\026\032 \037\034\031\027\025\024\023\022\021\020\017\016\016\r\r\f\f\f\013\013\013\013\n\n\n\n\t\t\t\t\t\t\t\t\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\007\007\006\006\006\005\005\005\005\004\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\004\005\005\005\005\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\005\005\005\005\006\006\007\007\b\b\t\n\f\r\017\022\026\034\030\026\024\022\021\020\017\016\016\r\f\f\013\013\013\n\n\n\t\t\t\t\b\b\b\b\b\b\b\007\007\007\007\007\007\007\007\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\006\007\007\007\007\007\006\006\005\005\005\005\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\003\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\004\004\004\004\005\005\005\006\006\007\007\b\t\013\f\017\022\024\022\021\017\016\r\f\f\013\013\n\n\t\t\t\b\b\b\b\007\007\007\007\007\006\006\006\006\006\006\006\006\006\006\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\005\004\004\004\004\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\003\003\003\003\003\003\004\004\004\005\005\006\007\b\t\013\016\016\r\f\013\n\n\t\b\b\b\007\007\007\006\006\006\006\006\005\005\005\005\005\005\005\005\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\004\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\002\002\002\002\002\002\002\003\003\003\003\004\004\005\006\007\t\t\b\007\007\006\006\006\005\005\005\005\004\004\004\004\004\004\004\003\003\003\003\003\003\003\003\003\003\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\001\001\001\001\001\001\001\001\001\001\002\002\002\003\003\004\004\004\003\003\003\003\003\002\002\002\002\002\002\002\002\002\002\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\001\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000\000").toString()).getBytes(StandardCharsets.ISO_8859_1);

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
        return g * g * 0x1p-18 >= (A * A + B * B);
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
        return g * g * 0x1p-18 >= (A2 * A2 + B2 * B2);
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
        return g * g * 0x1p-18 >= (A * A + B * B);
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
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
        final float dist = GAMUT_DATA[idx] * 0.5f;
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
    public static float oklabByHSL(float hue, float saturation, float lightness, float alpha) {
        lightness = Math.min(Math.max(lightness, 0f), 1f);
        saturation = Math.min(Math.max(saturation, 0f), 1f);
        hue -= (int)(hue + 0x1p14) - 0x4000;
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = GAMUT_DATA[idx] * saturation * 0.25f;
        return BitConversion.intBitsToFloat(
                (int) (alpha * 127.999f) << 25 |
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
        hue -= (int)(hue + 0x1p14) - 0x4000;
        alpha = Math.min(Math.max(alpha, 0f), 1f);
        final int idx = (int) (lightness * 255.999f) << 8 | (int) (256f * hue);
        final float dist = Math.min(chroma * 127.5f, GAMUT_DATA[idx] * 0.5f);
        return (
                (int) (alpha * 127.999f) << 25 |
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