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

package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.LaserRandom;
import com.github.tommyettinger.random.MizuchiRandom;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.Interpolations;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class CorrelationVisualizer extends ApplicationAdapter {

    private ImmediateModeRenderer20 renderer;
    private static final int width = 256, height = 256;
    private static final float[][] previousGrid = new float[width][height];
    public static EnhancedRandom[][] makeGrid(EnhancedRandom base){
        EnhancedRandom[][] g = new EnhancedRandom[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                g[x][y] = base.copy();
                g[x][y].setState(x << 1, y << 1);
            }
        }
        return g;
    }
    private static final EnhancedRandom[][][] randoms = new EnhancedRandom[][][]{
            makeGrid(new LaserRandom(1, 1)),
            makeGrid(new MizuchiRandom(1, 1)),
    };
    int currentRandom = 0, randomCount = 2;
    int currentMode = 0, modeCount = 3;

    public static void refreshGrid() {
        for (int i = 0, n = randoms.length; i < n; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    randoms[i][x][y].setState(x << 1, y << 1);
                }
            }
        }
    }

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = 0;
    private boolean keepGoing = true;
    
    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case P: //pause
                        keepGoing = !keepGoing;
                    case SPACE:
                        ctr++;
                        break;
                    case N: // next
                    case EQUALS:
                    case ENTER:
                        currentRandom = ((currentRandom + (UIUtils.shift() ? randomCount - 1: 1)) % randomCount);
                        break;
                    case M:
                        currentMode = ((currentMode + (UIUtils.shift() ? modeCount - 1: 1)) % modeCount);
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        renderer.begin(view.getCamera().combined, GL_POINTS);
        int bt;
        switch (currentMode) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = (int) randoms[currentRandom][x][y].nextLong() & 255;
                        renderer.color(previousGrid[x][y] = BitConversion.intBitsToFloat(0xFE000000 | bt << 16 | bt << 8 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = -((int) randoms[currentRandom][x][y].nextLong() & 1) >>> 8;
                        renderer.color(previousGrid[x][y] = BitConversion.intBitsToFloat(0xFE000000 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bt = -(int) (randoms[currentRandom][x][y].nextLong() >>> 63) >>> 8;
                        renderer.color(previousGrid[x][y] = BitConversion.intBitsToFloat(0xFE000000 | bt));
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
        }
        renderer.end();
    }

    @Override
    public void render() {
        // not sure if this is always needed...
//        Gdx.gl.glDisable(GL20.GL_BLEND);
        ScreenUtils.clear(Color.BLACK);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            ctr++;
            putMap();
        }
        else {
            renderer.begin(view.getCamera().combined, GL_POINTS);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    renderer.color(previousGrid[x][y]);
                    renderer.vertex(x, y, 0);
                }
            }
            renderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Test: Random Correlation Visualization");
        config.useVsync(true);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new CorrelationVisualizer(), config);
    }
}
