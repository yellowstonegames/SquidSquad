package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.grid.LineTools;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.WildernessGenerator;

public class GlyphMapTest extends ApplicationAdapter {

    Font font;
    SpriteBatch batch;
    LaserRandom random;
    GlyphMap gm;
    WildernessGenerator wilderness;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(720, 640);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new GlyphMapTest(), config);
    }

    @Override
    public void create() {
        random = new LaserRandom();
        batch = new SpriteBatch();
        font = KnownFonts.getIosevkaSlab().scaleTo(12f, 20f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getAStarry();
        gm = new GlyphMap(font, 60, 32);

        wilderness = new WildernessGenerator(60, 32, Biome.TABLE[random.nextInt(42)], random);
        regenerate();
    }

    public void regenerate(){
        wilderness.biome = Biome.TABLE[random.nextInt(42)];
        wilderness.floorTypes = WildernessGenerator.floorsByBiome(wilderness.biome, random);
        wilderness.contentTypes = WildernessGenerator.contentByBiome(wilderness.biome, random);
        wilderness.generate();
        gm.backgrounds = wilderness.colors;
        for (int y = 0; y < wilderness.height; y++) {
            for (int x = 0; x < wilderness.width; x++) {
                gm.put(x, y, wilderness.glyphs[x][y]);
            }
        }

    }

    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        if(Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY))
            regenerate();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Camera camera = gm.viewport.getCamera();
        camera.position.set(gm.gridWidth * 0.5f, gm.gridHeight * 0.5f, 0f);
        camera.update();
        gm.viewport.apply(false);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gm.draw(batch, 0, 0);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gm.resize(width, height);
    }
}
