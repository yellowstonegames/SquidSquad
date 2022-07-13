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

import com.badlogic.gdx.Gdx;
import com.github.tommyettinger.textra.Font;

/*
 */

/**
 * Preconfigured static {@link Font} instances, with any important metric adjustments already applied. This uses the
 * {@link com.github.tommyettinger.textra.KnownFonts} class from textratypist, which is also the dependency that
 * provides Font here. All of the {@code getXyz()} methods that return some Font {@code Xyz} here are very simple; they
 * call the method of the same name in textratypist to get a Font (which is cached inside textratypist and copied to get
 * the returned value), set a {@link com.github.tommyettinger.textra.ColorLookup} on that Font so the syntax from
 * {@link com.github.yellowstonegames.core.DescriptiveColor#describe(CharSequence)} can be used, and return the modified
 * Font. If you use the KnownFonts class from textratypist, each Font will still work, but you have to use colors known
 * by {@link com.badlogic.gdx.graphics.Colors} if you look them up by name, and can't modify lightness or saturation.
 * <br>
 * Typical usage involves calling one of the static methods like {@link #getCozette()} or {@link #getGentium()} to get a
 * particular Font. This knows a fair amount of fonts, but it doesn't require the image assets for all of those to be
 * present in a game -- only the files mentioned in the documentation for a method are needed, and only if you call that
 * method. It's likely that many games would only use one font file, and so would generally only need a .fnt file, a
 * .png file, and some kind of license file. They could ignore all other assets required by other fonts.
 * <br>
 * There are some special features in Font that are easier to use with parts of this class. {@link #getStandardFamily()}
 * pre-populates a FontFamily so you can switch between different fonts with the {@code [@Sans]} syntax.
 * {@link #addEmoji(Font)} adds all of Twitter's emoji from the <a href="https://github.com/twitter/twemoji">Twemoji</a>
 * project to a given font, which lets you enter emoji with the {@code [+man scientist, dark skin tone]} syntax or the
 * generally-easier {@code [+👨🏿‍🔬]} syntax. If you want to use names for emoji, you may want to consult "Twemoji.atlas"
 * for the exact names used; some names changed from the standard because of technical restrictions.
 * <br>
 * There's some documentation for every known Font, including a link to a preview image and a listing of all
 * required files to use a Font. The required files include any license you need to abide by; this doesn't necessarily
 * belong in the {@code assets} folder like the rest of the files! Most of these fonts are either licensed under the OFL
 * or some Creative Commons license; the CC ones typically require attribution, but none of the fonts restrict usage to
 * noncommercial projects.
 * <br>
 * The links to required font files (.fnt, .png, in at least one case .font, and various forms of license documents) go
 * to the <a href="https://github.com/tommyettinger/textratypist">textratypist repo</a> and its assets, rather than the
 * copies here. Because this class just wraps textratypist now, if there's a problem with some asset or its
 * configuration, you can update the textratypist dependency in your project and use the most recent (and hopefully
 * fixed) font files from that repo. This may be more reliable than using duplicate font files from SquidSquad's repo,
 * since those might not be as up-to-date.
 */
public final class KnownFonts {
    private KnownFonts() {
        if (Gdx.app == null)
            throw new IllegalStateException("Gdx.app cannot be null; initialize KnownFonts in create() or later.");
    }


    /**
     * Returns a very large fixed-width Font already configured to use a square font with 45-degree angled sections,
     * based on the typeface used on the Atari ST console. This font only supports ASCII, but it supports all of it.
     * Caches the result for later calls. The font is "a-starry", based on "Atari ST (low-res)" by Damien Guard; it is
     * available under a CC-BY-SA-3.0 license, which requires attribution to Damien Guard (and technically Tommy
     * Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     * Preview: <a href="https://i.imgur.com/9P1Zpfr.png">Image link</a> (uses width=8, height=8)
     * <br>
     * This also looks good if you scale it so its height is twice its width. For small sizes, you should stick to
     * multiples of 8. Preview: <a href="https://i.imgur.com/a7ZUIDX.png">Image link</a> (uses width=8, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.fnt">AStarry-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-standard.png">AStarry-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-license.txt">AStarry-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry
     */
    public static Font getAStarry()
    {
        return com.github.tommyettinger.textra.KnownFonts.getAStarry().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a square font with 45-degree angled sections, based on the
     * typeface used on the Atari ST console, that should scale cleanly to many sizes. This font only supports ASCII,
     * but it supports all of it. Caches the result for later calls. The font is "a-starry", based on "Atari ST
     * (low-res)" by Damien Guard; it is available under a CC-BY-SA-3.0 license, which requires attribution to Damien
     * Guard (and technically Tommy Ettinger, because he made changes in a-starry) if you use it. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/x2RQU34.png">Image link</a> (uses width=10, height=10)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.fnt">AStarry-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-msdf.png">AStarry-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/AStarry-license.txt">AStarry-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarryMSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getAStarryMSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a light-weight variable-width slab serif font with good Latin and
     * Cyrillic script support, that should scale pretty well from a height of about 160 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Bitter, a free (OFL) typeface by <a href="https://github.com/solmatas/BitterPro">The Bitter Project</a>.
     * It supports quite a lot of Latin-based scripts and Cyrillic, but does not really cover Greek or any other
     * scripts. This font can look good at its natural size, which uses width roughly equal to height,
     * or squashed so height is slightly smaller. Bitter looks very similar to {@link #getGentium()}, except that Bitter
     * is quite a bit lighter, with thinner strokes and stylistic flourishes on some glyphs.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect. Unlike most other
     * fonts here, this does not use makeGridGlyphs, because it would make underline and strikethrough much thicker than
     * other strokes in the font. This does mean that strikethrough starts too far to the left, and extends too far to
     * the right, unfortunately, but its weight matches.
     * <br>
     * Preview: <a href="https://i.imgur.com/kuMpRJy.png">Image link</a> (uses width=33, height=30, adjustLineHeight(1.225f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.fnt">Bitter-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-standard.png">Bitter-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Bitter-License.txt">Bitter-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Bitter-Light.ttf
     */
    public static Font getBitter()
    {
        return com.github.tommyettinger.textra.KnownFonts.getBitter().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a very-legible variable-width font with strong support for Canadian
     * Aboriginal Syllabic, that should scale pretty well from a height of about 86 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Canada1500, a free (public domain, via CC0) typeface by Ray
     * Larabie. It supports quite a lot of Latin-based scripts, Greek, Cyrillic, Canadian Aboriginal Syllabic, arrows,
     * many dingbats, and more. This font can look good at its natural size, which uses width roughly equal to height,
     * or narrowed down so width is smaller.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/4o8y4pA.png">Image link</a> (uses width=30, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.fnt">Canada1500-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-standard.png">Canada1500-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Canada1500-License.txt">Canada1500-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Canada1500.ttf
     */
    public static Font getCanada()
    {
        return com.github.tommyettinger.textra.KnownFonts.getCanada().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see <a href="https://github.com/microsoft/cascadia-code">Microsoft's page</a>).
     * It supports a lot of glyphs,
     * including most extended Latin, Greek, Braille, and Cyrillic. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/zOXbBEJ.png">Image link</a> (uses width=9, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.fnt">CascadiaMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/CascadiaMono-msdf.png">CascadiaMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cascadia-license.txt">Cascadia-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMono()
    {
        return com.github.tommyettinger.textra.KnownFonts.getCascadiaMono().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font configured to use a cozy fixed-width bitmap font,
     * <a href="https://github.com/slavfox/Cozette">Cozette by slavfox</a>. Cozette has broad coverage of Unicode,
     * including Greek, Cyrillic, Braille, and tech-related icons. This does not scale well except to integer
     * multiples, but it should look very crisp at its default size of 7x13 pixels. This defaults to having
     * {@link Font#integerPosition} set to true, which helps keep it pixel-perfect if 1 world unit is 1 pixel, but can
     * cause major visual issues if 1 world unit corresponds to much more than 1 pixel.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/z0UCjaU.png">Image link</a> (uses width=7, height=13; this is small enough
     * to make the scaled text look bad in some places)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.fnt">Cozette-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-standard.png">Cozette-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Cozette-license.txt">Cozette-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the 7x13px font Cozette
     */
    public static Font getCozette()
    {
        return com.github.tommyettinger.textra.KnownFonts.getCozette().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. It really does handle a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/dLv1VVy.png">Image link</a> (uses width=9, height=20)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.fnt">DejaVuSansMono-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-msdf.png">DejaVuSansMono-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/DejaVuSansMono-License.txt">DejaVuSansMono-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font DejaVu Sans Mono using MSDF
     */
    public static Font getDejaVuSansMono()
    {
        return com.github.tommyettinger.textra.KnownFonts.getDejaVuSansMono().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale well from a height of about 132 down to a height of 34. Caches the result for later calls. The font used is
     * Gentium, an open-source (SIL Open Font License) typeface by SIL (see
     * <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It supports a lot of glyphs,
     * including quite a bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various
     * real languages. This does not use a distance field effect, as opposed to {@link #getGentiumSDF()}. You may want
     * to stick using just fonts that avoid distance fields if you have a family of fonts.
     * <br>
     * Preview: <a href="https://i.imgur.com/ovp08uR.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.fnt">Gentium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-standard.png">Gentium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-license.txt">Gentium-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium.ttf
     */
    public static Font getGentium()
    {
        return com.github.tommyettinger.textra.KnownFonts.getGentium().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique). You usually will want to reduce the line height
     * of this Font after you scale it; using {@code KnownFonts.getGentium().scaleTo(55, 45).adjustLineHeight(0.8f)}
     * usually works. Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see <a href="https://software.sil.org/gentium/">SIL's page on Gentium here</a>). It
     * supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily, but should be
     * crisp enough when zoomed out.
     * <br>
     * Preview: <a href="https://i.imgur.com/Mgxytp3.png">Image link</a> (uses width=50, height=45, adjustLineHeight(0.8f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.fnt">Gentium-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-sdf.png">Gentium-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Gentium-license.txt">Gentium-license.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Gentium.ttf using SDF
     */
    public static Font getGentiumSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getGentiumSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow font with nearly-complete CJK character
     * coverage, plus Latin, Greek, and Cyrillic, that shouldm scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Hanazono (HanMinA, specifically), a free (OFL) typeface.
     * This uses a somewhat-small standard bitmap font because of how many glyphs are present (over 34000); it might not
     * scale as well as other standard bitmap fonts here.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/6frmNrt.png">Image link</a> (uses width=17, height=21, adjustLineHeight(1.3f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.fnt">Hanazono-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-standard.png">Hanazono-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Hanazono-License.txt">Hanazono-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font HanMinA.ttf
     */
    public static Font getHanazono()
    {
        return com.github.tommyettinger.textra.KnownFonts.getHanazono().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font configured to use a classic, nostalgic fixed-width bitmap font,
     * IBM 8x16 from the early, oft-beloved computer line. This font is notably loaded
     * from a SadConsole format file, which shouldn't affect how it looks (but in reality,
     * it might). This does not scale except to integer multiples, but it should look very
     * crisp at its default size of 8x16 pixels. This supports some extra characters, but
     * not at the typical Unicode codepoints.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * This does not include a license because the source, <a href="https://github.com/Thraka/SadConsole/tree/master/Fonts">SadConsole's fonts</a>,
     * did not include one. It is doubtful that IBM would have any issues with respectful use
     * of their signature font throughout the 1980s, but if the legality is concerning, you
     * can use {@link #getCozette()} for a different bitmap font.
     * <br>
     * Preview: <a href="https://i.imgur.com/jbacFxO.png">Image link</a> (uses width=8, height=16)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.font">IBM-8x16-standard.font</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/IBM-8x16-standard.png">IBM-8x16-standard.png</a></li>
     * </ul>
     *
     * @return the Font object that represents an 8x16 font included with early IBM computers
     */
    public static Font getIBM8x16()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIBM8x16().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This does not
     * use a distance field effect, as opposed to {@link #getInconsolataMSDF()}.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/tIeZgyd.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.fnt">Inconsolata-LGC-Custom-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-standard.png">Inconsolata-LGC-Custom-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom
     */
    public static Font getInconsolata()
    {
        return com.github.tommyettinger.textra.KnownFonts.getInconsolata().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages. This uses the
     * Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field technique,
     * which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/6p2nvxZ.png">Image link</a> (uses width=10, height=26)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.fnt">Inconsolata-LGC-Custom-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-Custom-msdf.png">Inconsolata-LGC-Custom-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Inconsolata-LGC-License.txt">Inconsolata-LGC-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Inconsolata LGC Custom using MSDF
     */
    public static Font getInconsolataMSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getInconsolataMSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style. Does not use a distance field effect, and is sized best at 9x25 pixels.
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/nzr6ZU4.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.fnt">Iosevka-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-standard.png">Iosevka-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf
     */
    public static Font getIosevka()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevka().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * However, using a distance field makes it effectively impossible to mix fonts using a FontFamily (any variation in
     * distance field settings would make some fonts in the family blurry and others too sharp).
     * <br>
     * Preview: <a href="https://i.imgur.com/HS8kdna.png">Image link</a> (uses .scaleTo(12, 25).fitCell(9, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.fnt">Iosevka-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-msdf.png">Iosevka-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
     */
    public static Font getIosevkaMSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevkaMSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to fairly large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font License) typeface by
     * <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Signed Distance Field (SDF) technique as opposed to the Multi-channel Signed Distance Field
     * technique that {@link #getIosevkaMSDF()} uses, which isn't as sharp at large sizes but can look a little better
     * at small sizes. However, using a distance field makes it effectively impossible to mix fonts using a FontFamily
     * (any variation in distance field settings would make some fonts in the family blurry and others too sharp).
     * <br>
     * Preview: <a href="https://i.imgur.com/shaPl7F.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.fnt">Iosevka-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-sdf.png">Iosevka-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using SDF
     */
    public static Font getIosevkaSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevkaSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style. Does not use a distance field effect, and is sized best at 9x25 pixels.
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/47tODT0.png">Image link</a> (uses .scaleTo(12, 26).fitCell(10, 26, false).adjustLineHeight(0.9f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.fnt">Iosevka-Slab-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-standard.png">Iosevka-Slab-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf
     */
    public static Font getIosevkaSlab()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevkaSlab().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique as opposed to the normal Signed Distance Field
     * technique, which gives the rendered font sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/O5iVUYb.png">Image link</a> (uses .scaleTo(12, 25).fitCell(9, 25, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.fnt">Iosevka-Slab-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-msdf.png">Iosevka-Slab-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static Font getIosevkaSlabMSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevkaSlabMSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to fairly large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by <a href="https://be5invis.github.io/Iosevka/">Belleve Invis</a>, and it uses several
     * customizations thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of
     * extended Latin, Greek, and Cyrillic.
     * This Font is already configured with {@link Font#fitCell(float, float, boolean)}, and repeated calls to fitCell()
     * have an unknown effect; you may want to stick to scaling this and not re-fitting if you encounter issues.
     * This uses the Signed Distance Field (SDF) technique as opposed to the Multi-channel Signed Distance Field
     * technique that {@link #getIosevkaSlabMSDF()} uses, which isn't as sharp at large sizes but can look a little
     * better at small sizes.
     * <br>
     * Preview: <a href="https://i.imgur.com/D0gXlaf.png">Image link</a> (uses scaleTo(12, 26).fitCell(10, 26, false))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.fnt">Iosevka-Slab-sdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-Slab-sdf.png">Iosevka-Slab-sdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Iosevka-License.md">Iosevka-License.md</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using SDF
     */
    public static Font getIosevkaSlabSDF()
    {
        return com.github.tommyettinger.textra.KnownFonts.getIosevkaSlabSDF().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a fairly-legible variable-width ornamental/medieval font, that should
     * scale pretty well from a height of about 90 down to a height of maybe 30.
     * Caches the result for later calls. The font used is Kingthings Foundation, a free (custom permissive license)
     * typeface; this has faux-bold applied already in order to make some ornamental curls visible at more sizes. You
     * can still apply bold again using markup. It supports only ASCII.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/35UXplI.png">Image link</a> (uses width=23, height=30)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.fnt">KingthingsFoundation-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-standard.png">KingthingsFoundation-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     * You may instead want the non-bold version, but this doesn't have a pre-made instance in KnownFonts:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-Light-standard.fnt">KingthingsFoundation-Light-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/KingthingsFoundation-Light-standard.png">KingthingsFoundation-Light-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Kingthings-License.txt">Kingthings-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font KingthingsFoundation.ttf
     */
    public static Font getKingthingsFoundation()
    {
        return com.github.tommyettinger.textra.KnownFonts.getKingthingsFoundation().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width serif font with good Unicode support, that should
     * scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Libertinus Serif, an open-source (SIL Open Font
     * License) typeface. It supports a lot of glyphs, including quite a bit of extended Latin, Greek, and Cyrillic.
     * This uses the Multi-channel Signed Distance Field (MSDF) technique, which should be very sharp. This probably
     * needs to be scaled so that it has much larger width than height; the default is 150x32.
     * <br>
     * Preview: <a href="https://i.imgur.com/Npfrzuu.png">Image link</a> (uses width=132, height=28)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.fnt">LibertinusSerif-Regular-msdf.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-Regular-msdf.png">LibertinusSerif-Regular-msdf.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/LibertinusSerif-License.txt">LibertinusSerif-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font LibertinusSerif.ttf using MSDF
     */
    public static Font getLibertinusSerif()
    {
        return com.github.tommyettinger.textra.KnownFonts.getLibertinusSerif().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. It has good extended-Latin coverage, but
     * does not support Greek, Cyrillic, or other scripts. This makes an especially large font by default, but can be
     * scaled down nicely.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/F9tv884.png">Image link</a> (uses width=20, height=28, adjustLineHeight(0.8f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.fnt">OpenSans-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-standard.png">OpenSans-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/OpenSans-License.txt">OpenSans-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans()
    {
        return com.github.tommyettinger.textra.KnownFonts.getOpenSans().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width "science-fiction/high-tech" font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Oxanium, a free (OFL) typeface. It supports a lot of Latin
     * and extended Latin, but not Greek or Cyrillic.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/rXUuB0x.png">Image link</a> (uses width=31, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.fnt">Oxanium-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-standard.png">Oxanium-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Oxanium-License.txt">Oxanium-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font Oxanium.ttf
     */
    public static Font getOxanium()
    {
        return com.github.tommyettinger.textra.KnownFonts.getOxanium().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a very-legible condensed variable-width font with excellent Unicode
     * support, that should scale pretty well from a height of about 62 down to a height of maybe 20.
     * Caches the result for later calls. The font used is Roboto Condensed, a free (Apache 2.0) typeface by Christian
     * Robertson. It supports Latin-based scripts almost entirely, plus Greek, (extended) Cyrillic, and more.
     * This font is meant to be condensed in its natural appearance, but can be scaled to be wider if desired.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/90ODH7T.png">Image link</a> (uses width=25, height=35, adjustLineHeight(0.9f))
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.fnt">RobotoCondensed-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-standard.png">RobotoCondensed-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/RobotoCondensed-License.txt">RobotoCondensed-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font RobotoCondensed.ttf
     */
    public static Font getRobotoCondensed()
    {
        return com.github.tommyettinger.textra.KnownFonts.getRobotoCondensed().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Returns a Font already configured to use a variable-width, narrow, humanist font, that should
     * scale pretty well down, but not up.
     * Caches the result for later calls. The font used is Yanone Kaffeesatz, a free (OFL) typeface. It supports a lot
     * of Latin, Cyrillic, and some extended Latin, but not Greek.
     * This uses a very-large standard bitmap font, which lets it be scaled down nicely but not scaled up very well.
     * This may work well in a font family with other fonts that do not use a distance field effect.
     * <br>
     * Preview: <a href="https://i.imgur.com/G2bspGv.png">Image link</a> (uses width=30, height=35)
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.fnt">YanoneKaffeesatz-standard.fnt</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-standard.png">YanoneKaffeesatz-standard.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/YanoneKaffeesatz-License.txt">YanoneKaffeesatz-License.txt</a></li>
     * </ul>
     *
     * @return the Font object that can represent many sizes of the font YanoneKaffeesatz.ttf
     */
    public static Font getYanoneKaffeesatz()
    {
        return com.github.tommyettinger.textra.KnownFonts.getYanoneKaffeesatz().setColorLookup(GlyphMap::getRgba);
    }

    /**
     * Takes a Font and adds the Twemoji icon set to it, making the glyphs available using {@code [+name]} syntax.
     * You can use the name of an emoji, such as {@code [+clown face]}, or equivalently use the actual emoji, such as
     * {@code [+🤡]}, with the latter preferred because the names can be unwieldy or hard to get right. This caches the
     * Twemoji atlas for later calls. This tries to load the files "Twemoji.atlas" and "Twemoji.png" from the internal
     * storage first, and if that fails, it tries to load them from local storage in the current working directory.
     * <br>
     * Preview: <a href="https://i.imgur.com/Mw0fWA7.png">Image link</a> (uses the font {@link #getYanoneKaffeesatz()})
     * <br>
     * Needs files:
     * <ul>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.atlas">Twemoji.atlas</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji.png">Twemoji.png</a></li>
     *     <li><a href="https://github.com/tommyettinger/textratypist/blob/main/knownFonts/Twemoji-License.txt">Twemoji-License.txt</a></li>
     * </ul>
     *
     * @param changing a Font that will have over 3000 emoji added to it, with more aliases
     * @return {@code changing}, after the emoji atlas has been added
     */
    public static Font addEmoji(Font changing) {
        return com.github.tommyettinger.textra.KnownFonts.addEmoji(changing);
    }
    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any Font.
     * This will only function at all if all the assets (for every known Font) are present and load-able.
     *
     * @return a new array containing all Font instances this knows
     */
    public static Font[] getAll() {
        return new Font[]{getAStarry(), getAStarryMSDF(), getBitter(), getCanada(), getCascadiaMono(), getCozette(),
                getDejaVuSansMono(), getGentium(), getGentiumSDF(), getHanazono(), getIBM8x16(), getInconsolata(),
                getInconsolataMSDF(), getIosevka(), getIosevkaMSDF(), getIosevkaSDF(), getIosevkaSlab(),
                getIosevkaSlabMSDF(), getIosevkaSlabSDF(), getKingthingsFoundation(), getLibertinusSerif(),
                getOpenSans(), getOxanium(), getRobotoCondensed(), getYanoneKaffeesatz()
        };
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any
     * non-distance-field Font.
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     *
     * @return a new array containing all non-distance-field Font instances this knows
     */
    public static Font[] getAllStandard() {
        return new Font[]{getAStarry(), getBitter(), getCanada(), getCozette(), getGentium(), getHanazono(),
                getIBM8x16(), getInconsolata(), getIosevka(), getIosevkaSlab(), getKingthingsFoundation(),
                getOpenSans(), getOxanium(), getRobotoCondensed(), getYanoneKaffeesatz()
        };
    }

    /**
     * Returns a Font ({@link #getGentium()}) with a FontFamily configured so that all non-distance-field Fonts can be
     * used with syntax like {@code [@Sans]}. The names this supports can be accessed with code using
     * {@code getStandardFamily().family.fontAliases.keys()}. These names so far are:
     * <ul>
     *     <li>{@code Serif}, which is {@link #getGentium()},</li>
     *     <li>{@code Sans}, which is {@link #getOpenSans()},</li>
     *     <li>{@code Mono}, which is {@link #getInconsolata()},</li>
     *     <li>{@code Condensed}, which is {@link #getRobotoCondensed()},</li>
     *     <li>{@code Humanist}, which is {@link #getYanoneKaffeesatz()},</li>
     *     <li>{@code Retro}, which is {@link #getIBM8x16()},</li>
     *     <li>{@code Slab}, which is {@link #getIosevkaSlab()},</li>
     *     <li>{@code Bitter}, which is {@link #getBitter()},</li>
     *     <li>{@code Canada}, which is {@link #getCanada()},</li>
     *     <li>{@code Cozette}, which is {@link #getCozette()},</li>
     *     <li>{@code Iosevka}, which is {@link #getIosevka()},</li>
     *     <li>{@code Medieval}, which is {@link #getKingthingsFoundation()},</li>
     *     <li>{@code Future}, which is {@link #getOxanium()},</li>
     *     <li>{@code Console}, which is {@link #getAStarry()}, and</li>
     *     <li>{@code Hanazono}, which is {@link #getHanazono()}.</li>
     * </ul>
     * You can also always use the full name of one of these fonts, which can be obtained using {@link Font#getName()}.
     * {@code Serif}, which is {@link #getGentium()}, will always be the default font used after a reset.
     * <br>
     * This will only function at all if all the assets (for every known standard Font) are present and load-able.
     *
     * @return a Font that can switch between 15 different Fonts in its FontFamily, to any non-distance-field Font this knows
     */
    public static Font getStandardFamily() {
        Font.FontFamily family = new Font.FontFamily(
                new String[]{"Serif", "Sans", "Mono", "Condensed", "Humanist",
                        "Retro", "Slab", "Bitter", "Canada", "Cozette", "Iosevka",
                        "Medieval", "Future", "Console", "Hanazono"},
                new Font[]{getGentium(), getOpenSans(), getInconsolata(), getRobotoCondensed(), getYanoneKaffeesatz(),
                        getIBM8x16(), getIosevkaSlab(), getBitter(), getCanada(), getCozette(), getIosevka(),
                        getKingthingsFoundation(), getOxanium(), getAStarry(), getHanazono()});
        return family.connected[0].setFamily(family);
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any SDF Font.
     * This will only function at all if all the assets (for every known SDF Font) are present and load-able.
     *
     * @return a new array containing all SDF Font instances this knows
     */
    public static Font[] getAllSDF() {
        return new Font[]{getGentiumSDF(), getIosevkaSDF(), getIosevkaSlabSDF()};
    }

    /**
     * Returns a new array of Font instances, calling each getXyz() method in this class that returns any MSDF Font.
     * This will only function at all if all the assets (for every known MSDF Font) are present and load-able.
     *
     * @return a new array containing all MSDF Font instances this knows
     */
    public static Font[] getAllMSDF() {
        return new Font[]{getAStarryMSDF(), getCascadiaMono(), getDejaVuSansMono(), getInconsolataMSDF(), getIosevkaMSDF(), getIosevkaSlabMSDF(), getLibertinusSerif()};
    }
}
