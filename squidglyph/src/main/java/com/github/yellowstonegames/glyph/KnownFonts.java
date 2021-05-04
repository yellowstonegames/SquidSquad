package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.graphics.Texture;

import static com.github.yellowstonegames.glyph.Font.DistanceFieldType.*;

public class KnownFonts implements LifecycleListener {
    private static KnownFonts instance;

    private KnownFonts() {
        if(Gdx.app == null)
            throw new IllegalStateException("Gdx.app cannot be null; initialize KnownFonts in create() or later.");
        Gdx.app.addLifecycleListener(this);
    }

    private static void initialize()
    {
        if(instance == null)
            instance = new KnownFonts();
    }

    private Font cozette;
    /**
     * Returns a Font configured to use a cozy fixed-width bitmap font,
     * <a href="https://github.com/slavfox/Cozette">Cozette by slavfox</a>. Cozette has broad coverage of Unicode,
     * including Greek, Cyrillic, Braille, and tech-related icons. This does not scale except to integer
     * multiples, but it should look very crisp at its default size of 7x13 pixels.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Cozette.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Cozette.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Cozette-license.txt</li>
     * </ul>
     * @return the Font object that represents the 7x13px font Cozette
     */
    public static Font getCozette()
    {
        initialize();
        if(instance.cozette == null)
        {
            try {
                instance.cozette = new Font("Cozette.fnt", "Cozette.png", STANDARD, 1, 1, 0, -1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cozette != null)
            return new Font(instance.cozette);
        throw new RuntimeException("Assets for getCozette() not found.");
    }

    private Font openSans;
    /**
     * Returns a Font configured to use a clean variable-width font, Open Sans. This makes an especially large
     * font by default, but can be scaled down nicely.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/OpenSans.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/OpenSans.png</li>
     * </ul>
     * @return the Font object that represents the variable-width font OpenSans
     */
    public static Font getOpenSans()
    {
        initialize();
        if(instance.openSans == null)
        {
            try {
                instance.openSans = new Font("OpenSans.fnt", "OpenSans.png", STANDARD, 2, 0, 0, 0);
                instance.openSans.setTextureFilter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.openSans != null)
            return new Font(instance.openSans);
        throw new RuntimeException("Assets for getOpenSans() not found.");
    }

    private Font astarry;
    /**
     * Returns a Font already configured to use a square font with 45-degree angled sections, based on the
     * typeface used on the Atari ST console, that should scale cleanly to many sizes. This font only supports ASCII,
     * but it supports all of it. Caches the result for later calls. The font is "a-starry", based on "Atari ST
     * (low-res)" by Damien Guard; it is available under a CC-BY-SA-3.0 license, which requires attribution to Damien
     * Guard (and technically Tommy Ettinger, because he made changes in a-starry) if you use it.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/AStarry-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/AStarry-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/AStarry-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font A Starry using MSDF
     */
    public static Font getAStarry()
    {
        initialize();
        if(instance.astarry == null)
        {
            try {
                instance.astarry = new Font("AStarry-msdf.fnt", "AStarry-msdf.png", MSDF, 0, 1, 0, 0).scaleTo(18, 18);
                instance.astarry.distanceFieldCrispness = 3f;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.astarry != null)
            return new Font(instance.astarry);
        throw new RuntimeException("Assets for getAStarry() not found.");
    }

    private Font cascadiaMono;
    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see https://github.com/microsoft/cascadia-code ). It supports a lot of glyphs,
     * including most extended Latin (though it doesn't support a handful of chars used by FakeLanguageGen), Greek,
     * Braille, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     *
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/CascadiaMono-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/CascadiaMono-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Cascadia-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Cascadia Code Mono using MSDF
     */
    public static Font getCascadiaMono()
    {
        initialize();
        if(instance.cascadiaMono == null)
        {
            try {
                instance.cascadiaMono = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", MSDF, 2f, 1f, -5.5f, -1.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cascadiaMono != null)
            return new Font(instance.cascadiaMono);
        throw new RuntimeException("Assets for getCascadiaMono() not found.");
    }

    private Font dejaVuSansMono;
    /**
     * A nice old standby font with very broad language support, DejaVu Sans Mono is fixed-width and can be clearly
     * readable but doesn't do anything unusual stylistically. It really does handle a lot of glyphs; not only does this
     * have practically all Latin glyphs in Unicode (enough to support everything from Icelandic to Vietnamese), it has
     * Greek (including Extended), Cyrillic (including some optional glyphs), IPA, Armenian (maybe the only font here to
     * do so), Georgian (which won't be treated correctly by some case-insensitive code, so it should only be used if
     * case doesn't matter), and Lao. It has full box drawing and Braille support, handles a wide variety of math
     * symbols, technical marks, and dingbats, etc.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/DejaVuSansMono-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/DejaVuSansMono-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/DejaVuSansMono-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC using MSDF
     */
    public static Font getDejaVuSansMono()
    {
        initialize();
        if(instance.dejaVuSansMono == null)
        {
            try {
                instance.dejaVuSansMono = new Font("DejaVuSansMono-msdf.fnt", "DejaVuSansMono-msdf.png", MSDF, 1f, 4f, -1.5f, -4.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.dejaVuSansMono != null)
            return new Font(instance.dejaVuSansMono);
        throw new RuntimeException("Assets for getDejaVuSansMono() not found.");
    }


    private Font inconsolataLGC;
    /**
     * A customized version of Inconsolata LGC, a fixed-width geometric font that supports a large range of Latin,
     * Greek, and Cyrillic glyphs, plus box drawing and some dingbat characters (like zodiac signs). The original font
     * Inconsolata is by Raph Levien, and various other contributors added support for other languages.
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Inconsolata-LGC-Custom-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Inconsolata-LGC-Custom-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Inconsolata-LGC-License.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Inconsolata LGC using MSDF
     */
    public static Font getInconsolataLGC()
    {
        initialize();
        if(instance.inconsolataLGC == null)
        {
            try {
                instance.inconsolataLGC = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", MSDF, 5f, 1f, -10f, -8f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.inconsolataLGC != null)
            return new Font(instance.inconsolataLGC);
        throw new RuntimeException("Assets for getInconsolataLGC() not found.");
    }

    private Font iosevka;
    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-License.md</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
     */
    public static Font getIosevka()
    {
        initialize();
        if(instance.iosevka == null)
        {
            try {
                instance.iosevka = new Font("Iosevka-msdf.fnt", "Iosevka-msdf.png", MSDF, 3f, 6, -4f, -7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevka != null)
            return new Font(instance.iosevka);
        throw new RuntimeException("Assets for getIosevka() not found.");
    }

    private Font iosevkaSlab;
    /**
     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
     * sharper edges and precise corners instead of rounded tips on strokes.
     * <br>
     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-Slab-msdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-Slab-msdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-License.md</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
     */
    public static Font getIosevkaSlab()
    {
        initialize();
        if(instance.iosevkaSlab == null)
        {
            try {
                instance.iosevkaSlab = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", MSDF, 3f, 6, -4f, -7);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.iosevkaSlab != null)
            return new Font(instance.iosevkaSlab);
        throw new RuntimeException("Assets for getIosevkaSlab() not found.");
    }

    private Font gentium;
    /**
     * Returns a Font already configured to use a variable-width serif font with excellent Unicode support, that should
     * scale cleanly to even very large sizes (using an SDF technique).
     * Caches the result for later calls. The font used is Gentium, an open-source (SIL Open Font
     * License) typeface by SIL (see https://software.sil.org/gentium/ ). It supports a lot of glyphs, including quite a
     * bit of extended Latin, Greek, and Cyrillic, as well as some less-common glyphs from various real languages. This
     * uses the Signed Distance Field (SDF) technique, which may be slightly fuzzy when zoomed in heavily.
     * <br>
     * Preview: <a href="https://i.imgur.com/JXGbHVf.png">Image link</a>
     * <br>
     * Needs files:
     * <ul>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Gentium-sdf.fnt</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Gentium-sdf.png</li>
     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Gentium-license.txt</li>
     * </ul>
     * @return the Font object that can represent many sizes of the font Gentium.ttf using SDF
     */
    public static Font getGentium()
    {
        initialize();
        if(instance.gentium == null)
        {
            try {
                instance.gentium = new Font("Gentium-sdf.fnt", "Gentium-sdf.png", SDF, 0f, 5f, 0f, -5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.gentium != null)
            return new Font(instance.gentium);
        throw new RuntimeException("Assets for getGentium() not found.");
    }


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

        if(cozette != null){
            cozette.dispose();
            cozette = null;
        }
        if(openSans != null){
            openSans.dispose();
            openSans = null;
        }
        if(astarry != null){
            astarry.dispose();
            astarry = null;
        }
        if(cascadiaMono != null){
            cascadiaMono.dispose();
            cascadiaMono = null;
        }
        if(dejaVuSansMono != null){
            dejaVuSansMono.dispose();
            dejaVuSansMono = null;
        }
        if(inconsolataLGC != null){
            inconsolataLGC.dispose();
            inconsolataLGC = null;
        }
        if(iosevka != null){
            iosevka.dispose();
            iosevka = null;
        }
        if(iosevkaSlab != null){
            iosevkaSlab.dispose();
            iosevkaSlab = null;
        }
        if(gentium != null){
            gentium.dispose();
            gentium = null;
        }
    }
}
