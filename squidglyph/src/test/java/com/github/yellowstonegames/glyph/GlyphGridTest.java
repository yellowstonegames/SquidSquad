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
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.WildernessGenerator;

public class GlyphGridTest extends ApplicationAdapter {

    Font font;
    Stage stage;
    WhiskerRandom random;
    GlyphGrid gg;
    WildernessGenerator wilderness;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(720, 640);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new GlyphGridTest(), config);
    }

    @Override
    public void create() {
        random = new WhiskerRandom();
        stage = new Stage();
        font = KnownFonts.getIosevkaSlab().scaleTo(12f, 20f);
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getAStarry();
        gg = new GlyphGrid(font, 60, 32);

        wilderness = new WildernessGenerator(60, 32, Biome.TABLE[random.nextInt(42)], random);
        regenerate();
        stage.addActor(gg);
    }

    public void regenerate(){
        wilderness.biome = Biome.TABLE[random.nextInt(42)];
        wilderness.floorTypes = WildernessGenerator.floorsByBiome(wilderness.biome, random);
        wilderness.contentTypes = WildernessGenerator.contentByBiome(wilderness.biome, random);
        wilderness.generate();
        gg.backgrounds = wilderness.colors;
        for (int y = 0; y < wilderness.height; y++) {
            for (int x = 0; x < wilderness.width; x++) {
                gg.put(x, y, wilderness.glyphs[x][y]);
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
