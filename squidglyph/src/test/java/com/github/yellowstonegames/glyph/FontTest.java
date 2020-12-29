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
        float x = 0, y = 200, w = font.cellWidth, h = font.cellHeight;
        batch.begin();
        font.enableShader(batch);
        batch.setPackedColor(BitConversion.reversedIntBitsToFloat(DescriptiveColor.lerpColors(
                (int)((System.currentTimeMillis() >>> 10) * 0x9E377B00 | 0xFE),
                (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E377B00 | 0xFE),
                (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
                )));
        batch.draw(font.mapping.get('H'), x += w, y, w, h);
        batch.draw(font.mapping.get('e'), x += w, y, w, h);
        batch.draw(font.mapping.get('l'), x += w, y, w, h);
        batch.draw(font.mapping.get('l'), x += w, y, w, h);
        batch.draw(font.mapping.get('o'), x += w, y, w, h);
        batch.draw(font.mapping.get(','), x += w, y, w, h);
        x += w;
        batch.draw(font.mapping.get('W'), x += w, y, w, h);
        batch.draw(font.mapping.get('o'), x += w, y, w, h);
        batch.draw(font.mapping.get('r'), x += w, y, w, h);
        batch.draw(font.mapping.get('l'), x += w, y, w, h);
        batch.draw(font.mapping.get('d'), x += w, y, w, h);
        batch.draw(font.mapping.get('!'), x += w, y, w, h);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
