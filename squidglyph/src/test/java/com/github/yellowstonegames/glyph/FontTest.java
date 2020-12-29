package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FontTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(640, 480);
        config.disableAudio(true);
        new Lwjgl3Application(new FontTest(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new Font("Cozette.fnt", "Cozette.png", false);
    }

    @Override
    public void render() {
        int x = 20, y = 200;
        batch.begin();
        batch.draw(font.mapping.get('H'), x += 7, y);
        batch.draw(font.mapping.get('e'), x += 7, y);
        batch.draw(font.mapping.get('l'), x += 7, y);
        batch.draw(font.mapping.get('l'), x += 7, y);
        batch.draw(font.mapping.get('o'), x += 7, y);
        batch.draw(font.mapping.get(','), x += 7, y);
        x += 7;
        batch.draw(font.mapping.get('W'), x += 7, y);
        batch.draw(font.mapping.get('o'), x += 7, y);
        batch.draw(font.mapping.get('r'), x += 7, y);
        batch.draw(font.mapping.get('l'), x += 7, y);
        batch.draw(font.mapping.get('d'), x += 7, y);
        batch.draw(font.mapping.get('!'), x += 7, y);
        batch.end();
    }
}
