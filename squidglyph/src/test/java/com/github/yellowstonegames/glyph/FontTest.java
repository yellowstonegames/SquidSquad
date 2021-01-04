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

public class FontTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    LaserRandom random;
    int[][] backgrounds;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(640, 480);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new FontTest(), config);
    }

    @Override
    public void create() {
        random = new LaserRandom();
        batch = new SpriteBatch();
//        font = new Font("Cozette.fnt", "Cozette.png", false);
        font = new Font("Inconsolata-LGC-Custom-msdf.fnt", "Inconsolata-LGC-Custom-msdf.png", true).scale(0.375f, 0.375f);
//        font = new Font("CascadiaMono-msdf.fnt", "CascadiaMono-msdf.png", true).scale(0.75f, 0.75f);
//        font = new Font("Iosevka-Slab-Family-msdf.fnt", "Iosevka-Slab-Family-msdf.png", true).scale(0.75f, 0.75f);
        backgrounds = new int[(int)Math.ceil(640 / font.cellWidth)][(int)Math.ceil(480 / font.cellHeight)];
        int nw = DescriptiveColor.describe("darker sage"), ne = DescriptiveColor.describe("dark rich cactus"),
                sw = DescriptiveColor.describe("dull peach butter"), se = DescriptiveColor.describe("dark brown");
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
        float x = 0, y = font.cellHeight * 3;
        batch.begin();
        font.enableShader(batch);

        font.drawBlocks(batch, backgrounds, 0f, 0f);

        font.drawText(batch, "Hello, World!", x, y,
                DescriptiveColor.lerpColors(
                DescriptiveColor.lerpColors(
                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
                ), 0x000000FF, 0.375f));
        font.drawMarkupText(batch, "The [dark richer red]MAW[] of the [|lighter blue mint]WENDIGO[] [*]appears[*]!", 0, font.cellHeight);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
