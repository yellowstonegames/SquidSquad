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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.QualityPalette;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class PlasmaVisualizer extends ApplicationAdapter {

    private ElfPlasma elf;
    private Point3Float inPt = new Point3Float(), outPt = new Point3Float();
    private Color color = new Color();
    private int octaves = 1;
    private float freq = 0x1p-10f;
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(256);

    public static float basicPrepare(float n)
    {
        return n * 0.5f + 0.5f;
    }

    public static float circleInPrepare(float n)
    {
        return Interpolations.circleIn.apply(n * 0.5f + 0.5f);
    }

    @Override
    public void create() {
        elf = new ElfPlasma(0.2f, 0.4f, 0.6f);

        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.GOURD);
        gif.setDitherStrength(0.3f);
        gif.palette = new QualityPalette();

        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case BACKSLASH:
                        elf.stringDeserialize(Gdx.app.getClipboard().getContents());
                        break;
                    case W:
                        for (int c = 0; c < 128; c++) {
                            int w = 256, h = 256;
                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, tm = c * 0x1p-13f, xf, yf;
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                xf = x * freq - halfW;
                                for (int y = 0; y < h; y++) {
                                    yf = y * freq - halfH;
                                    elf.plasma(outPt, inPt.set(xf, yf, tm));
                                    color.set(outPt.x, outPt.y, outPt.z, 1f);
                                    p.setColor(color);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

                        String ser = elf.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println(ser);
                        gif.palette.analyzeHueWise(frames);
                        gif.write(Gdx.files.local("out/" + ser + ".gif"), frames, 16);
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
                        elf.seed.add(-0.01f, -0.01f, -0.01f);
                        break;
                    case S: //seed
                        elf.seed.add(0.01f, 0.01f, 0.01f);
                        break;
                    case SLASH:
                        int hash = elf.seed.hashCode();
                        elf.seed.set(Hasher.randomize1Float(hash), Hasher.randomize2Float(hash), Hasher.randomize3Float(hash));
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        System.out.println("noise type not implemented yet");
                        break;
                    case M:
                    case MINUS:
                        System.out.println("noise type not implemented yet");
                        break;
                    case F: // frequency
                        freq *= (UIUtils.shift() ? 1.25f : 0.8f);
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
        float bright, tm = ctr * 0x1p-4f * freq, xf, yf;
        for (int x = 0; x < width; x++) {
            xf = x * freq - width * 0.5f;
            for (int y = 0; y < height; y++) {
                yf = y * freq - height * 0.5f;
                elf.plasma(outPt, inPt.set(xf, yf, tm));
                color.set(outPt.x, outPt.y, outPt.z, 1f);
                renderer.color(color);
                renderer.vertex(x, y, 0);
            }
        }
        renderer.end();

    }

    @Override
    public void render() {
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
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
        config.setTitle("SquidSquad Test: Plasma Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new PlasmaVisualizer(), config);
    }
}
