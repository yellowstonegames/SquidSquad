/*
 * Copyright (c) 2020-2026; see AUTHORS file.
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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.tommyettinger.ds.ObjectList;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class INoiseVisualizer extends ApplicationAdapter {

    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 0x1p-4f;
    private final ObjectList<INoise> noises = INoise.Serializer.copyNoises();
    private int noiseIndex = 0;
    private NoiseWrapperDetailed noise = new NoiseWrapperDetailed(noises.get(noiseIndex), freq, NoiseWrapperDetailed.FBM, octaves);
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
        return Math.min(Math.max(n * 0.5f + 0.5f, 0), 1);
    }

    public static float circleInPrepare(float n)
    {
        return Interpolations.circleIn.apply(n * 0.5f + 0.5f);
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
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
                        for (int c = 0; c < 256; c++) {
                            int w = 256, h = 256;
                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, inv = 1f / w;
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
//                                    float color = basicPrepare(noise.getConfiguredNoise(x, y, c));
                                    float color = circleInPrepare(
                                            noise.getNoise(
                                            x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH)))
                                    );
//                                            * 0.5f + 0.25f + TrigTools.sinTurns(c * 0x1p-7f) * 0.25f;
//                                    color = color * 0x0.FFp0f + 0x1p-8f;
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

//                            float hue = 0.94f; // pink
//                            float hue = 0.2f; // apricot
//                            float hue = 0.11f; // fire
//                            float hue = 0.625f; // a soft blue
//                            float hue = 0.45f; // bright green
//                            float hue = 0.425f; // Brazil green
//                            float hue = 0.08f; // bright red

                            float hueBase = 0.675f; // a slightly violet-ish blue
//                            float hueHigh = 0.15f; // apricot
//                            float hueBase = 0.11f; // embers
//                            float hueHigh = 0.2f; // orange glow
//                            float hueBase = 0.575f; // blue-cyan
//                            float hueHigh = 0.45f; // bright green
//                            float hueBase = 0.45f; // bright green
                            float hueHigh = 0.425f; // Brazil green
//                            float hueHigh = 0.27f; // gold


                        IntList g = ColorGradients.toRGBA8888(ColorGradients.appendGradientChain(new IntList(256), 256, Interpolations.smooth,
//                                  DescriptiveColor.oklabByHSL(0.375f, 0.6f, 0.2f, 1f)
//                                  DescriptiveColor.oklabByHSL(0.7f, 0.6f, 0.7f, 1f)
//                                  DescriptiveColor.oklabByHSL(0.85f, 0.9f, 0.2f, 1f)

//                                  DescriptiveColor.oklabByHSL(0.85f, 0.9f, 0.4f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.99f, 0.9f, 0.55f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.8f, 1f, 0.99f, 1f)

//                                , DescriptiveColor.oklabByHSL(0.1f, 0.85f, 0.95f, 1f)

                                DescriptiveColor.oklabByHSL(0.4f, 0.8f, 0.05f, 1f),
                                DescriptiveColor.oklabByHSL(0.42f, 0.8f, 0.16f, 1f),
                                DescriptiveColor.oklabByHSL(0.44f, 1f, 0.6f, 1f),
                                DescriptiveColor.oklabByHSL(0.41f, 0.75f, 0.75f, 1f)

//                                DescriptiveColor.oklabByHSL(0.94f, 0.8f, 0.05f, 1f),
//                                DescriptiveColor.oklabByHSL(0.97f, 0.8f, 0.2f, 1f),
//                                DescriptiveColor.oklabByHSL(0.04f, 1f, 0.65f, 1f),
//                                DescriptiveColor.oklabByHSL(0.2f, 0.75f, 0.9f, 1f)
//
//                                DescriptiveColor.oklabByHSL(0.14f, 0.9f, 0.3f, 1f),
//                                DescriptiveColor.oklabByHSL(0.07f, 0.65f, 0.03f, 1f),
//                                DescriptiveColor.oklabByHSL(0.1f, 1.0f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.05f, 0.85f, 0.8f, 1f)

//                                DescriptiveColor.oklabByHSL(0.52f, 0.9f, 0.35f, 1f),
//                                DescriptiveColor.oklabByHSL(0.47f, 0.8f, 0.0f, 1f),
//                                DescriptiveColor.oklabByHSL(0.54f, 0.9f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.4f, 0.85f, 0.8f, 1f)
//
//                                DescriptiveColor.oklabByHSL(0.02f, 0.9f, 0.35f, 1f),
//                                DescriptiveColor.oklabByHSL(0.97f, 0.8f, 0.0f, 1f),
//                                DescriptiveColor.oklabByHSL(0.04f, 0.9f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.1f, 0.85f, 0.8f, 1f)

//                                  DescriptiveColor.oklabByHSL(0.65f, 1f, 0.3f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.475f, 0.75f, 0.65f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.7f, 0.85f, 0.87f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.5f, 0.9f, 0.72f, 1f)

                        ));
                        g.toArray(gif.palette.paletteArray);
                        String ser = noise.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println(ser);
//                        gif.write(Gdx.files.local("out/cube" + System.currentTimeMillis() + ".gif"), frames, 16);
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
                        noise.setSeed((int) noise.getSeed() - 1);
                        break;
                    case S: //seed
                        noise.setSeed((int) noise.getSeed() + 1);
                        break;
                    case SLASH:
                        noise.setSeed((int) Hasher.randomize3(noise.getSeed()));
                        break;
                    case SEMICOLON: //fractal spiral, why not
                        noise.setFractalSpiral(!noise.isFractalSpiral());
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noise.setWrapped(noises.get(noiseIndex = (noiseIndex + (UIUtils.shift() ? noises.size() - 1 : 1)) % noises.size()));
                        break;
                    case M:
                    case MINUS:
                        noise.setWrapped(noises.get(noiseIndex = (noiseIndex + (UIUtils.shift() ? 1 : noises.size() - 1)) % noises.size()));
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 4 : 1)) % 5);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        break;
                    case LEFT_BRACKET:
                        noise.setLacunarity(noise.getLacunarity() * (UIUtils.shift() ? 1.25f : 0.8f));
                        break;
                    case RIGHT_BRACKET:
                        noise.setGain(noise.getGain() * (UIUtils.shift() ? 0.8f : 1.25f));
                        break;
                    case I: // inverse mode
//                        noise.setFractalLacunarity(1f/noise.getFractalLacunarity());
                        noise.setGain(1f/noise.getGain());
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case V:
                        System.out.println(noise.toString());
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
        float bright, c = ctr * 0.25f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getNoise(x + c, y + c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getNoise(x, y, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64 + c, xs = TrigTools.sinTurns(x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64 + c, ys = TrigTools.sinTurns(y * iHeight) * 64 + c;
                        bright = basicPrepare(noise.getNoise(xc, yc, xs, ys));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64, xs = TrigTools.sinTurns(x * iWidth) * 64;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64, ys = TrigTools.sinTurns(y * iHeight) * 64;
                        bright = basicPrepare(noise.getNoise(xc, yc, xs, ys, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64 + c, xs = TrigTools.sinTurns(x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64 + c, ys = TrigTools.sinTurns(y * iHeight) * 64 + c,
                                zc = TrigTools.cosTurns((x - y) * 0.5f * iWidth) * 64 - c, zs = TrigTools.sinTurns((x - y) * 0.5f * iWidth) * 64 - c;
                        bright = basicPrepare(noise.getNoise(xc, yc, zc, xs, ys, zs));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
        }
        renderer.end();

    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing)
            ctr++;
        putMap();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Test: INoise Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new INoiseVisualizer(), config);
    }
}
