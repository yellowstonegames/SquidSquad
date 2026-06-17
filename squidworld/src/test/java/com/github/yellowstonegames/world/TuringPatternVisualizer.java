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

package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
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
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.random.FlowRandom;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class TuringPatternVisualizer extends ApplicationAdapter {

    int mode = 0;
    int modes = 4;

    private final float[] turing = TuringPattern.initialize(width, height);
    private final int[][] turingActivate = TuringPattern.offsetsCircle(width, height, 4),
            turingInhibit = TuringPattern.offsetsCircle(width, height, 8);

    private FlowRandom random = new FlowRandom(123456789L);
    private NoiseWrapper noise = new NoiseWrapper(new FoamNoise(12345L), 0.01f, NoiseWrapper.FBM, 1);
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(256);

    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    public static float circleInPrepare(float n)
    {
//        return Math.max(0f, n);
        return Interpolations.circleIn.apply(n * 0.5f + 0.5f);
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();

        random.setSeed(ctr);
        TuringPattern.initializeInto(turing, random);

        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x01010100 | 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.NONE);
        gif.setDitherStrength(0.5f);
        gif.palette = new PaletteReducer(gray256);

        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case BACKSLASH:
                        noise.stringDeserialize(Gdx.app.getClipboard().getContents());
                        break;
                    case W:
                        random.setSeed(123L);
                        TuringPattern.initializeInto(turing, random);
                        for (int c = 0; c < 192; c++) {
                            int w = 256, h = 256;
                            TuringPattern.distort(turingActivate, w, h, noise, c, 778899);
                            TuringPattern.distort(turingInhibit, w, h, noise, c, 556677);
                            TuringPattern.step(turing, turingActivate, 0.2f, turingInhibit, -0.2f);
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
                                    float color = basicPrepare(turing[x * h + y]);
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/turing/").mkdirs();

                        String ser = noise.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println(ser);
                        gif.write(Gdx.files.local("out/turing/" + ser + ".gif"), frames, 16);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
                        break;
                    case P: //pause
                        keepGoing = !keepGoing;
                    case SPACE:
                        ctr++;
                        break;
                    case E: //earlier seed
                        noise.setSeed((int) noise.getSeed() - 1);
                        break;
                    case S: //seed
                        noise.setSeed((int) noise.getSeed() + 1);
                        break;
                    case SLASH:
                        noise.setSeed((int) Hasher.randomize3(noise.getSeed()));
                        break;
                    case EQUALS:
                    case ENTER:
                        mode = ((mode + (UIUtils.shift() ? modes - 1 : 1)) % modes);
                        switch (mode){
                            case 0:
                            case 1:
                                TuringPattern.initializeInto(turing, random);
                                break;
                            case 2:
                                TuringPattern.offsetsCircleInto(turingActivate, width, height, 4);
                                TuringPattern.offsetsCircleInto(turingInhibit, width, height, 8);
                                TuringPattern.initializeInto(turing, width, height, noise, ctr);
                                break;
                        }
                        break;
                    case F: // frequency
                        noise.setFrequency(noise.getFrequency() * (UIUtils.shift() ? 1.25f : 0.8f));
                        break;
                    case H: // higher octaves
                        noise.setOctaves((noise.getOctaves() & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setOctaves((noise.getOctaves() + 6 & 7) + 1);
                        break;
                    case K: // sKip
                        ctr += 1000;
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
        float bright;
        switch (mode) {
            case 0:
                Gdx.graphics.setTitle("3D noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(turing[x * height + y]);
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                Gdx.graphics.setTitle("3D noise at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                TuringPattern.distort(turingActivate, width, height, noise, ctr, 778899);
                TuringPattern.distort(turingInhibit, width, height, noise, ctr, 556677);
                TuringPattern.step(turing, turingActivate, 0.2f, turingInhibit, -0.2f);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(turing[x * height + y]);
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                Gdx.graphics.setTitle("Offsets at " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                TuringPattern.step(turing, turingActivate, 0.1f, turingInhibit, -0.1f);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(turing[x * height + y]);
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3:
                Gdx.graphics.setTitle("Plain noise " + Gdx.graphics.getFramesPerSecond()  + " FPS");
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getNoise(x, y, ctr * 0.25f));
                        renderer.color(bright, bright, bright, 1f);
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
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if (keepGoing) {
            // standard clear the background routine for libGDX
            ScreenUtils.clear(0f, 0f, 0f, 1f);
            ctr++;
            putMap();
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
        config.setTitle("SquidSquad Test: Noise Visualization");
        config.useVsync(true);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new TuringPatternVisualizer(), config);
    }
}
