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

package com.github.yellowstonegames.smooth.dawnlike;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import static com.github.yellowstonegames.smooth.dawnlike.SunriseDemo.*;

public class PairedDemo extends ApplicationAdapter {
    @Override
    public void create() {
        SunriseDemo sunriseOklab;
        SunriseRgbDemo sunriseRgb;
        Lwjgl3Window oklab = ((Lwjgl3Application) Gdx.app).newWindow(sunriseOklab = new SunriseDemo(), getDefaultConfiguration("Oklab"));
        InputProcessor oklabProcessor = Gdx.input.getInputProcessor();
        Lwjgl3Window rgb   = ((Lwjgl3Application) Gdx.app).newWindow(sunriseRgb = new SunriseRgbDemo(), getDefaultConfiguration("RGB"));
        InputProcessor rgbProcessor = Gdx.input.getInputProcessor();
        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean keyUp(int keycode) {
                oklab.focusWindow();
                oklabProcessor.keyUp(keycode);
                rgb.focusWindow();
                return rgbProcessor.keyUp(keycode);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                oklab.focusWindow();
                oklabProcessor.touchUp(screenX, screenY, pointer, button);
                rgb.focusWindow();
                return rgbProcessor.touchUp(screenX, screenY, pointer, button);
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                oklab.focusWindow();
                oklabProcessor.touchDragged(screenX, screenY, pointer);
                rgb.focusWindow();
                return rgbProcessor.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                oklab.focusWindow();
                oklabProcessor.mouseMoved(screenX, screenY);
                rgb.focusWindow();
                return rgbProcessor.mouseMoved(screenX, screenY);
            }
        });
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.useVsync(true);
        cfg.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        cfg.setTitle("Parent");
        cfg.setWindowedMode(300, 50);
        new Lwjgl3Application(new PairedDemo(), cfg);
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration(String title) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setResizable(true);
        configuration.useVsync(false);
        //// this matches the maximum foreground FPS to the refresh rate of the active monitor.
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        configuration.setTitle(title);
        //// useful to know if something's wrong in a shader.
        //// you should remove the next line for a release.
//        configuration.enableGLDebugOutput(true, System.out);
        ShaderProgram.prependVertexCode = "#version 110\n";
        ShaderProgram.prependFragmentCode = "#version 110\n";
        configuration.setWindowedMode(cellWidth * shownWidth, cellHeight * shownHeight);
        return configuration;
    }

}
