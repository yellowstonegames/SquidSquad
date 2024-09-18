/*
 * Copyright (c) 2020-2024 See AUTHORS file.
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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.random.PouchRandom;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.WildernessGenerator;

public class WildernessGridTest extends ApplicationAdapter {

    Font font;
    Stage stage;
    PouchRandom random;
    GlyphGrid gg;
    WildernessGenerator wilderness;
    long startTime;

    static int[] colorRanges = {
            0,       // Only used for before night starts
            299,     // Night (midnight to dawn)
            419,     // Early Morning (dawn to sunrise)
            689,     // Morning (sunrise to mid-morning)
            1019,    // Day (mid-morning to afternoon)
            1139,    // Late Afternoon (afternoon to sunset)
            1249,    // Early Evening (sunset to 1.5 hours after)
            1439     // Evening (1.5 hours after sunset to midnight)
    };

    static int[] rgbValues = {
            Integer.reverseBytes(Color.toIntBits(90, 20, 70, 255)),          // Evening (dark purple)
            Integer.reverseBytes(Color.toIntBits(0, 0, 0, 255)),             // Night (black)
            Integer.reverseBytes(Color.toIntBits(140, 180, 210, 255)),       // Early Morning (light sky blue)
            Integer.reverseBytes(Color.toIntBits(173, 216, 230, 255)),       // Morning (very light sky blue)
            Integer.reverseBytes(Color.toIntBits(228, 232, 240, 255)),       // Day (very light sky blue)
            Integer.reverseBytes(Color.toIntBits(255, 200, 150, 255)),       // Late Afternoon (light orange)
            Integer.reverseBytes(Color.toIntBits(205, 160, 195, 255)),       // Early Evening (medium purple)
            Integer.reverseBytes(Color.toIntBits(90, 20, 90, 255))           // Evening (dark purple)
    };

    static int[] oklabValues = new int[rgbValues.length];
    static {
        for (int i = 0; i < rgbValues.length; i++) {
            oklabValues[i] = DescriptiveColor.fromRGBA8888(rgbValues[i]);
        }
    }
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(60 * 22, 32 * 22);
        config.disableAudio(true);
        config.useVsync(false);
        new Lwjgl3Application(new WildernessGridTest(), config);
    }

    @Override
    public void create() {
        random = new PouchRandom(12345);
        stage = new Stage();
        font = KnownFonts.getIosevkaSlab();
//        font = KnownFonts.getCascadiaMono().scale(0.5f, 0.5f);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getAStarry();
        gg = new GlyphGrid(font, 60, 32, true);

        wilderness = new WildernessGenerator(60, 32, Biome.TABLE[random.nextInt(42)], random);
        regenerate();
        stage.addActor(gg);
        startTime = TimeUtils.nanoTime();
    }

    public void regenerate(){
        wilderness.biome = Biome.TABLE[random.nextInt(42)];
        wilderness.floorTypes = WildernessGenerator.floorsByBiome(wilderness.biome, random);
        wilderness.contentTypes = WildernessGenerator.contentByBiome(wilderness.biome, random);
        wilderness.generate();
//        gg.backgrounds = wilderness.colors;
        gg.backgrounds = new int[wilderness.width][wilderness.height];
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE))
            regenerate();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Camera camera = gg.viewport.getCamera();
        camera.position.set(gg.gridWidth * 0.5f, gg.gridHeight * 0.5f, 0f);
        camera.update();
        long time = (TimeUtils.timeSinceNanos(startTime) >>> 24) % 1440L;
        int range = 1;
        for (; range < colorRanges.length;) {
            if (time <= colorRanges[range])
                break;
            ++range;
        }
        float interpolant = MathTools.norm(colorRanges[range-1], colorRanges[range], time);
//        int dayColor = DescriptiveColorRgb.lerpColors(rgbValues[range-1], rgbValues[range], interpolant);
        int dayColor = DescriptiveColor.lerpColors(oklabValues[range-1], oklabValues[range], interpolant);
        for (int x = 0; x < gg.gridWidth; x++) {
            for (int y = 0; y < gg.gridHeight; y++) {
//                gg.backgrounds[x][y] = DescriptiveColorRgb.lerpColors(wilderness.colors[x][y], dayColor, 0.25f);
                gg.backgrounds[x][y] = DescriptiveColor.toRGBA8888(
                        DescriptiveColor.lerpColors(wilderness.colorsOklab[x][y], dayColor, 0.25f));
            }

        }
        stage.draw();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        gg.resize(width, height);
    }
}
