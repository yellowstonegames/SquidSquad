package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

public class KnownFonts implements LifecycleListener {
    private static KnownFonts instance;

    private Font cozette, cascadiaMono, dejaVuSansMono, inconsolataLGC, iosevka, iosevkaSlab;

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

    /**
     * Returns a Font already configured to use a quirky fixed-width font with good Unicode support
     * and a humanist style, that should scale cleanly to even very large sizes (using an MSDF technique).
     * Caches the result for later calls. The font used is Cascadia Code Mono, an open-source (SIL Open Font
     * License) typeface by Microsoft (see https://github.com/microsoft/cascadia-code ). It supports a lot of glyphs,
     * including most extended Latin (though it doesn't support a handful of chars used by  FakeLanguageGen), Greek,
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
                instance.cascadiaMono = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", true, 2f, 1f, -4.5f, -1.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.cascadiaMono != null)
            return new Font(instance.cascadiaMono);
        throw new RuntimeException("Assets for getCascadiaMono() not found.");
    }

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
                instance.inconsolataLGC = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", true, 5f, 1f, -4f, -7.5f);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(instance.inconsolataLGC != null)
            return new Font(instance.inconsolataLGC);
        throw new RuntimeException("Assets for getInconsolataLGC() not found.");
    }

//    /**
//     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
//     * and a sans-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
//     * Caches the result for later calls. The font used is Iosevka, an open-source (SIL Open Font
//     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
//     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
//     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
//     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
//     * sharper edges and precise corners instead of rounded tips on strokes.
//     * <br>
//     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
//     * <br>
//     * Needs files:
//     * <ul>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-msdf.fnt</li>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-msdf.png</li>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-License.md</li>
//     * </ul>
//     * @return the Font object that can represent many sizes of the font Iosevka.ttf using MSDF
//     */
//    public static Font getIosevka()
//    {
//        initialize();
//        if(instance.iosevka == null)
//        {
//            try {
//                instance.iosevka = new Font("Iosevka-msdf.fnt", "Iosevka-msdf.png", true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if(instance.iosevka != null)
//            return new Font(instance.iosevka);
//        throw new RuntimeException("Assets for getIosevka() not found.");
//    }
//
//    /**
//     * Returns a Font already configured to use a highly-legible fixed-width font with good Unicode support
//     * and a slab-serif geometric style, that should scale cleanly to even very large sizes (using an MSDF technique).
//     * Caches the result for later calls. The font used is Iosevka with Slab style, an open-source (SIL Open Font
//     * License) typeface by Belleve Invis (see https://be5invis.github.io/Iosevka/ ), and it uses several customizations
//     * thanks to Iosevka's special build process. It supports a lot of glyphs, including quite a bit of extended Latin,
//     * Greek, and Cyrillic, but also the necessary box drawing characters. This uses the Multi-channel Signed Distance
//     * Field (MSDF) technique as opposed to the normal Signed Distance Field technique, which gives the rendered font
//     * sharper edges and precise corners instead of rounded tips on strokes.
//     * <br>
//     * Preview: <a href="https://i.imgur.com/YlzFEVX.png">Image link</a>
//     * <br>
//     * Needs files:
//     * <ul>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-Slab-msdf.fnt</li>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-Slab-msdf.png</li>
//     *     <li>https://github.com/yellowstonegames/SquidSquad/blob/master/assets/Iosevka-License.md</li>
//     * </ul>
//     * @return the Font object that can represent many sizes of the font Iosevka-Slab.ttf using MSDF
//     */
//    public static Font getIosevkaSlab()
//    {
//        initialize();
//        if(instance.iosevkaSlab == null)
//        {
//            try {
//                instance.iosevkaSlab = new Font("Iosevka-Slab-msdf.fnt", "Iosevka-Slab-msdf.png", true);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        if(instance.iosevkaSlab != null)
//            return new Font(instance.iosevkaSlab);
//        throw new RuntimeException("Assets for getIosevkaSlab() not found.");
//    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        if(cascadiaMono != null){
            cascadiaMono.dispose();
            cascadiaMono = null;
        }
        if(iosevka != null){
            iosevka.dispose();
            iosevka = null;
        }
        if(inconsolataLGC != null){
            inconsolataLGC.dispose();
            inconsolataLGC = null;
        }
        if(iosevkaSlab != null){
            iosevkaSlab.dispose();
            iosevkaSlab = null;
        }
    }
}
