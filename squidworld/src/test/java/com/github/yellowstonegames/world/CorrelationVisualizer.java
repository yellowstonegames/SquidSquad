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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.random.AceRandom;
import com.github.tommyettinger.random.EnhancedRandom;
import com.github.tommyettinger.random.FourWheelRandom;
import com.github.tommyettinger.random.LaserRandom;
import com.github.tommyettinger.random.MizuchiRandom;
import com.github.tommyettinger.random.PasarRandom;
import com.github.tommyettinger.random.ScruffRandom;
import com.github.tommyettinger.random.TricycleRandom;
import com.github.tommyettinger.random.TrimRandom;
import com.github.tommyettinger.random.WhiskerRandom;
import com.github.yellowstonegames.world.random.ScarfRandom;
import com.github.yellowstonegames.world.random.SplurgeRandom;
import com.github.yellowstonegames.world.random.SpoonRandom;
import com.github.yellowstonegames.world.random.SportyRandom;
import com.github.yellowstonegames.world.random.SpritzRandom;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class CorrelationVisualizer extends ApplicationAdapter {

    private String title = "";
    private ImmediateModeRenderer20 renderer;
    private static final int width = 256, height = 256;
    private static final float[][] previousGrid = new float[width][height];
    public static EnhancedRandom[][] makeGrid(EnhancedRandom base){
        EnhancedRandom[][] g = new EnhancedRandom[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                g[x][y] = base.copy();
                switch (g[x][y].getStateCount()) {
                    case 1:
                        g[x][y].setState(x << 17 ^ y << 1);
                        break;
                    case 2:
                        g[x][y].setState(x << 1, y << 1);
                        break;
                    case 3:
                        g[x][y].setState(x << 1, y << 1, 1L);
                        break;
                    case 4:
                        g[x][y].setState(x << 1, y << 1, 1L, 1L);
                        break;
                    case 5:
                        g[x][y].setState(x << 1, y << 1, 1L, 1L, 1L);
                        break;
                }
            }
        }
        return g;
    }
    private static final EnhancedRandom[][][] randoms = new EnhancedRandom[][][]{
            makeGrid(new LaserRandom(1, 1)),
            makeGrid(new MizuchiRandom(1, 1)),
            makeGrid(new TricycleRandom(1, 1, 1)),
            makeGrid(new FourWheelRandom(1, 1, 1, 1)),
            makeGrid(new TrimRandom(1, 1, 1, 1)),
            makeGrid(new WhiskerRandom(1, 1, 1, 1)),
            makeGrid(new ScruffRandom(1, 1, 1, 1)),
            makeGrid(new ScarfRandom(1, 1, 1, 1)),
            makeGrid(new PasarRandom(1, 1, 1, 1, 1)),
            makeGrid(new AceRandom(1, 1, 1, 1, 1)),
            makeGrid(new SplurgeRandom(1, 1)),
            makeGrid(new SportyRandom(1, 1)),
            makeGrid(new SpoonRandom(1, 1)),
            makeGrid(new SpritzRandom(1, 1)),
    };
    int currentRandom = 0, randomCount = randoms.length;
    int currentMode = 0, modeCount = 3;

    public static void refreshGrid() {
        for (int i = 0, n = randoms.length; i < n; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    switch (randoms[i][x][y].getStateCount()) {
                        case 1:
                            randoms[i][x][y].setState(x << 17 ^ y << 1);
                            break;
                        case 2:
                            randoms[i][x][y].setState(x << 1, y << 1);
                            break;
                        case 3:
                            randoms[i][x][y].setState(x << 1, y << 1, 1L);
                            break;
                        case 4:
                            randoms[i][x][y].setState(x << 1, y << 1, 1L, 1L);
                            break;
                        case 5:
                            randoms[i][x][y].setState(x << 1, y << 1, 1L, 1L, 1L);
                            break;
                    }
                }
            }
        }
    }


    public static void seedGrid() {
        for (int i = 0, n = randoms.length; i < n; i++) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    randoms[i][x][y].setSeed(x << 17 ^ y << 1);
                }
            }
        }
    }

    private Viewport view;
    private boolean keepGoing = true;

    @Override
    public void create() {
        title = randoms[currentRandom][0][0].getClass().getSimpleName() + " on mode " + currentMode;
        System.out.println(title);
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case SPACE:
                    case P: // pause
                        keepGoing = !keepGoing;
                        break;
                    case S: // step
                        putMap();
                        break;
                    case V: // vertical
                        System.out.println("Viewing column 0:");
                        System.out.println(randoms[currentRandom][0][0]);
                        System.out.println(randoms[currentRandom][0][1]);
                        System.out.println(randoms[currentRandom][0][2]);
                        System.out.println(randoms[currentRandom][0][3]);
                        break;
                    case H: // horizontal
                        System.out.println("Viewing row 0:");
                        System.out.println(randoms[currentRandom][0][0]);
                        System.out.println(randoms[currentRandom][1][0]);
                        System.out.println(randoms[currentRandom][2][0]);
                        System.out.println(randoms[currentRandom][3][0]);
                        break;
                    case NUM_1:
                    case NUMPAD_1:
                        seedGrid();
                        putMap();
                        break;
                    case N: // next
                    case EQUALS:
                    case ENTER:
                        currentRandom = ((currentRandom + (UIUtils.shift() ? randomCount - 1 : 1)) % randomCount);
                        refreshGrid();
                        title = randoms[currentRandom][0][0].getClass().getSimpleName()
                                + " on mode " + currentMode;
                        System.out.println(title);
                        if (!keepGoing) putMap();
                        break;
                    case M: // mode
                        currentMode = ((currentMode + (UIUtils.shift() ? modeCount - 1 : 1)) % modeCount);
                        refreshGrid();
                        title = randoms[currentRandom][0][0].getClass().getSimpleName()
                                + " on mode " + currentMode;
                        System.out.println(title);
                        if (!keepGoing) putMap();
                        break;
                    case Q: // quit
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
        ScreenUtils.clear(Color.BLACK);
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS showing " + title);
        if (keepGoing) {
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
        config.setForegroundFPS(0);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new CorrelationVisualizer(), config);
    }
}
