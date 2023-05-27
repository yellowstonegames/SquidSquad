package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.glyph.rexpaint.XPIO;
import com.github.yellowstonegames.glyph.rexpaint.XPFile;

public class REXPaintTest extends ApplicationAdapter {

    Font font;
    Stage stage;
    GlyphGrid gg;

    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("REXPaint test");
        config.setWindowedMode(720, 640);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new REXPaintTest(), config);
    }

    @Override
    public void create() {
        stage = new Stage();
        font = KnownFonts.getAStarry().scaleTo(16f, 16f);
        XPFile room = XPIO.loadXP(Gdx.files.internal("Dungeon1685158102701.xp"));
//        XPFile room = XPIO.loadXP(Gdx.files.internal("Room.xp"));

        gg = new GlyphGrid(font, room.getLayer(0).width, room.getLayer(0).height);

        room.intoGlyphGrid(gg);
        stage.addActor(gg);
    }

    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            Gdx.app.exit();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.gridWidth * 0.5f, gg.gridHeight * 0.5f, 0f);
        camera.update();
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
    }
}
