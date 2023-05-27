/*
 * Copyright (c) 2022-2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        config.setWindowedMode(40<<5, 25<<5);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new REXPaintTest(), config);
    }

    @Override
    public void create() {
        stage = new Stage();
        font = KnownFonts.getIosevkaSlab().scaleTo(20f, 32f).adjustLineHeight(1.25f);
        XPFile room = XPIO.loadXP(Gdx.files.internal("Dungeon1685158844401.xp"));
//        XPFile room = XPIO.loadXP(Gdx.files.internal("Room.xp"));

        gg = new GlyphGrid(font, room.getLayer(0).width, room.getLayer(0).height, true);

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
