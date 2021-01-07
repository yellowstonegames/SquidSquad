package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.grid.LineTools;

public class GlyphMapTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    LaserRandom random;
    GlyphMap gm;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(800, 640);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new GlyphMapTest(), config);
    }

    @Override
    public void create() {
        random = new LaserRandom(1L);
        batch = new SpriteBatch();
        font = KnownFonts.getInconsolataLGC().scale(0.375f, 0.375f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getAStarry();

        gm = new GlyphMap(font);

        int[][] backgrounds = new int[(int)Math.ceil(800 / font.cellWidth)][(int)Math.ceil(640 / font.cellHeight)];
        gm.backgrounds = backgrounds;
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
        char[][] lines = LineTools.decode4x4(random.nextLong() | LineTools.exteriorSquare, LineTools.light);
//        char[][] lines = LineTools.decode4x4(random.nextLong() & LineTools.interiorSquare, LineTools.lightAlt);
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                gm.put(x, y, lines[x][3 - y], random.nextInt() << 1 | 0xFE);
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        gm.draw(batch, 0, 0);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
