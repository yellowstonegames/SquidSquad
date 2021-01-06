package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.support.BitConversion;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.grid.LineTools;

public class FontTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    LaserRandom random;
    int[][] backgrounds;
    char[][] lines;
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
//        font = KnownFonts.getInconsolataLGC().scale(0.375f, 0.375f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = new Font("Cozette.fnt", "Cozette.png", false);
//        font = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", true).scale(0.375f, 0.375f);
//        font = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", true).scale(0.55f, 0.55f);
//        font = new Font("Iosevka-Slab-Family-msdf.fnt", "Iosevka-Slab-Family-msdf.png", true).scale(0.75f, 0.75f);
        backgrounds = new int[(int)Math.ceil(800 / font.cellWidth)][(int)Math.ceil(640 / font.cellHeight)];
        int sw = DescriptiveColor.describe("darker sage"), se = DescriptiveColor.describe("dark rich cactus"),
                nw = DescriptiveColor.describe("dull peach butter"), ne = DescriptiveColor.describe("dark brown");
        backgrounds[0][0] = sw;
        backgrounds[0][backgrounds[0].length - 1] = nw;
        backgrounds[backgrounds.length - 1][0] = se;
        backgrounds[backgrounds.length - 1][backgrounds[0].length - 1] = ne;
        for (int x = 1; x < backgrounds.length - 1; x++) {
            backgrounds[x][0] = DescriptiveColor.lerpColors(sw, se, x / (float)backgrounds.length);
            backgrounds[x][backgrounds[0].length - 1] = DescriptiveColor.lerpColors(nw, ne, x / (float)backgrounds.length);
        }
        for (int x = 0; x < backgrounds.length; x++) {
            int s = backgrounds[x][0], e = backgrounds[x][backgrounds[0].length - 1];
            for (int y = 1; y < backgrounds[0].length - 1; y++) {
                backgrounds[x][y] = DescriptiveColor.lerpColors(s, e, y / (float)backgrounds[0].length);
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float x = 0, y = font.cellHeight * 4;
        batch.begin();
        font.enableShader(batch);

        font.drawBlocks(batch, backgrounds, 0f, 0f);
        for (int xx = 0; xx < 4; xx++) {
            for (int yy = 0; yy < 4; yy++) {
                font.drawGlyph(batch, 0xFFFFFFFE00000000L | lines[xx][yy], font.cellWidth * xx, font.cellHeight * (9 - yy));
            }
        }
        int color = DescriptiveColor.lerpColors(
                DescriptiveColor.lerpColors(
                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
                ), 0x000000FF, 0.375f);
        font.drawMarkupText(batch, "[#"+ DigitTools.hex(color) +"]Hello, [~]World[~]Universe[.]α[^]2[^]!", x, y);
        font.drawMarkupText(batch, "The [dark richer red]MAW[] of the [/][|lighter blue mint]WENDIGO[] [*]appears[*]!", 0, font.cellHeight * 3);
        font.drawMarkupText(batch, "The [_][dark dull blue purple]BLADE[] of [*][/][|darker richest yellow]KINGS[] strikes!", 0, font.cellHeight * 2);
        font.drawMarkupText(batch, "[;]Each cap[], [,]All lower[], [!]Caps lock[], [?]Unknown[]?", 0, font.cellHeight);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
