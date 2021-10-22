package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.LineTools;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Layout;
public class FontTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    LaserRandom random;
    int[][] backgrounds;
    char[][] lines;
    Layout layout;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(800, 640);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new FontTest(), config);
    }

    @Override
    public void create() {
        random = new LaserRandom(1L);
        lines = LineTools.decode4x4(random.nextLong() | LineTools.exteriorSquare, LineTools.light);
//        lines = LineTools.decode4x4(random.nextLong() & LineTools.interiorSquare, LineTools.lightAlt);
        batch = new SpriteBatch();
//        font = KnownFonts.getInconsolataLGC().scaleTo(16, 32);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getOpenSans().scale(0.75f, 0.75f);
//        font = KnownFonts.getAStarry();
        font = KnownFonts.getGentium().scaleTo(48, 48);

//        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("dawnlike/Dawnlike.atlas"), Gdx.files.internal("dawnlike"));
//        font = new Font("dawnlike/PlainAndSimplePlus.fnt", atlas.findRegion("PlainAndSimplePlus"), false, 0, 0, 2, 2);
        layout = new Layout(font).setTargetWidth(670f);
        backgrounds = new int[(int) Math.ceil(800 / font.cellWidth)][(int) Math.ceil(640 / font.cellHeight)];
        int sw = DescriptiveColor.describe("darker sage"), se = DescriptiveColor.describe("dark rich cactus"),
                nw = DescriptiveColor.describe("dull peach butter"), ne = DescriptiveColor.describe("dark brown");
        backgrounds[0][0] = sw;
        backgrounds[0][backgrounds[0].length - 1] = nw;
        backgrounds[backgrounds.length - 1][0] = se;
        backgrounds[backgrounds.length - 1][backgrounds[0].length - 1] = ne;
        for (int x = 1; x < backgrounds.length - 1; x++) {
            backgrounds[x][0] = DescriptiveColor.lerpColors(sw, se, x / (float) backgrounds.length);
            backgrounds[x][backgrounds[0].length - 1] = DescriptiveColor.lerpColors(nw, ne, x / (float) backgrounds.length);
        }
        for (int x = 0; x < backgrounds.length; x++) {
            int s = backgrounds[x][0], e = backgrounds[x][backgrounds[0].length - 1];
            for (int y = 1; y < backgrounds[0].length - 1; y++) {
                backgrounds[x][y] = DescriptiveColor.lerpColors(s, e, y / (float) backgrounds[0].length);
            }
        }

//        font.markup("[#00FF00FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", glyphs[0] = new LongList());
//        font.markup("The [dark richer red]MAW[] of the [/][|lighter blue mint]wendigo[] [*]appears[*]!", glyphs[1] = new LongList());
//        font.markup("The [_][dark dull blue purple]BLADE[] of [*][/][|dark richest yellow]KINGS[] strikes!", glyphs[2] = new LongList());
//        font.markup("[;]Each cap[], [,]All lower[], [!]Caps lock[], [?]Unknown[]?", glyphs[3] = new LongList());

        font.markup("[#007711FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", layout);

        font.markup("\n[*]Водяной[] — в славянской мифологии дух, обитающий в воде, хозяин вод[^][BLUE][[2][]."
                + "\nВоплощение стихии воды как отрицательного и опасного начала[^][BLUE][[3][[citation needed][].", layout);

        font.markup("\nThe [dark rich red]MAW[] of the [/][lighter dull sky]wendigo[/] (wendigo)[] [*]appears[*]!"
                        + "\nThe [_][dark gray]BLADE[] of [*][/][rich light yellow]DYNAST-KINGS[] strikes!"
                        + "\n[_][;]Each cap, [,]All lower, [!]Caps lock[], [?]Unknown[]?"
                        + "\n[dark dull bronze yellow]φ[] = (1 + 5[^]0.5[^]) * 0.5"
                        + "\n[dull light orange]¿Qué son estos? ¡Arribate, mijo![]"
                        + "\nPchnąć[] w tę łódź [dark tan]jeża[] lub ośm skrzyń [rich purple]fig[]."
                , layout);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.5f, 0.9f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float x = 0, y = font.cellHeight * (layout.lines() - 1);
        batch.begin();
        font.enableShader(batch);

        font.drawBlocks(batch, backgrounds, 0f, 0f);
        for (int xx = 0; xx < 4; xx++) {
            for (int yy = 0; yy < 4; yy++) {
                font.drawGlyph(batch, 0xFFFFFFFE00000000L | lines[xx][yy], font.cellWidth * xx, y + font.cellHeight * (1 + yy));
            }
        }
//        long color = (long) DescriptiveColor.lerpColors(
//                DescriptiveColor.lerpColors(
//                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
//                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
//                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
//                ), 0x000000FF, 0.375f) << 32;
//        for (int i = 0, n = glyphs[0].size(); i < n; i++) {
//            glyphs[0].set(i, glyphs[0].get(i) & 0xFFFFFFFFL | color);
//        }
        font.drawGlyphs(batch, layout, x, y, Align.left);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
