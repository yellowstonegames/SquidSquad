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
        font = new Font("Iosevka-Slab-Family-msdf.fnt", "Iosevka-Slab-Family-msdf.png", true).scale(2f, 2f);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float x = 5, y = 200;
        batch.begin();
        font.enableShader(batch);
        font.drawText(batch, "Hello, World!", x, y,
                DescriptiveColor.lerpColors(
                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
                ));
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
