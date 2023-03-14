/*
 * Copyright (c) 2023 See AUTHORS file.
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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.tommyettinger.ds.ObjectList;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.yellowstonegames.core.Interpolations;
import com.github.yellowstonegames.core.Interpolations.Interpolator;

import static com.badlogic.gdx.graphics.GL20.GL_LINES;

public class InterpolationsGraphing extends ApplicationAdapter {
    private static final int width = 210, height = 320;

    Interpolator current;
    Font font;
    ImmediateModeRenderer20 renderer;
    SpriteBatch batch;
    ScreenViewport view;
    @Override
    public void create() {
        font = KnownFonts.getAStarry().scaleTo(8, 16);
        view = new ScreenViewport();
        batch = new SpriteBatch();
        renderer = new ImmediateModeRenderer20(width * 3, false, true, 0);
        ObjectList<Interpolator> all = Interpolations.getInterpolatorList();
        current = all.first();
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.WHITE);
        renderer.begin(view.getCamera().combined, GL_LINES);

        // horizontal graph lines
        for (int i = 15; i < height; i+=30) {
            renderer.color(Color.CYAN);
            renderer.vertex(0, i, 0);
            renderer.color(Color.CYAN);
            renderer.vertex(width, i, 0);
        }
        // vertical graph lines
        for (int i = 20; i < width; i+=30) {
            renderer.color(Color.CYAN);
            renderer.vertex(i, 0, 0);
            renderer.color(Color.CYAN);
            renderer.vertex(i, height, 0);
        }
        // x-axis
        renderer.color(Color.NAVY);
        renderer.vertex(0, 75, 0);
        renderer.color(Color.NAVY);
        renderer.vertex(width, 75, 0);
        // y-axis
        renderer.color(Color.NAVY);
        renderer.vertex(50, 0, 0);
        renderer.color(Color.NAVY);
        renderer.vertex(50, height, 0);
        renderer.end();
        // line where y == 1
        renderer.color(Color.LIGHT_GRAY);
        renderer.vertex(0, 225, 0);
        renderer.color(Color.LIGHT_GRAY);
        renderer.vertex(width, 225, 0);
        // line where x == 1
        renderer.color(Color.LIGHT_GRAY);
        renderer.vertex(200, 0, 0);
        renderer.color(Color.LIGHT_GRAY);
        renderer.vertex(200, height, 0);
        renderer.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Demo: Interpolations");
        config.useVsync(true);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new InterpolationsGraphing(), config);
    }
}
