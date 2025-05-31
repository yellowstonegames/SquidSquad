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

package com.github.yellowstonegames.world.standalone;

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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class DisasterSimplexVisualizer extends ApplicationAdapter {

    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 0x1p-4f;
    private boolean inverse;
    private DisasterSimplexNoiseStandalone noise = new DisasterSimplexNoiseStandalone(0, freq);
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
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x010101 << 8 | 0xFF;
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
                        noise.setSeed(Base.BASE10.readInt(Gdx.app.getClipboard().getContents()));
                        break;
                    case W:
                        noise.setFrequency(0x1p-4f);
                        for (int c = 0; c < 256; c++) {
                            int w = 256, h = 256;
                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, inv = 1f / w;
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
                                    float color = basicPrepare((float) noise.getNoise(x, y, c));
//                                    float color = circleInPrepare(
//                                            (float) noise.getNoise(
//                                            x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH)))
//                                    );
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
//                        g.toArray(gif.palette.paletteArray);


//                        for (int i = 0; i < 256; i++) {
//                            int hiLo = Math.round(
////                                    MathTools.square(
//                                            (MathTools.swayTight(i * 0x1p-6f + 0.5f))
////                                    )
//                            );
//                            gif.palette.paletteArray[i]
////                            &= 0xFF0000FF;
//                                    = DescriptiveColor.toRGBA8888(
////                                            DescriptiveColor.oklabByHSL((i + (noise.getSeed() & 255)) * 0x2p-8f, 0.75f, 0.6f, 1f)
//                                            DescriptiveColor.oklabByHSL(hueBase, 1f, i * 0x3.7p-10f, 1f)
//                            );

//                                    DescriptiveColor.oklabByHSL(
//                                            hiLo * (hueHigh - hueBase + 1f) + hueBase, // hueBase to hueHigh
////                                            hiLo * 0.19f + hue, // red to gold
////                                            (i & 255) * 0x1p-12f - 0x1p-5f + hue, // small hue variation
////                                            (i & 255) * 0x1p-11f - 0x3p-7f + hue, // smallish hue variation
////                                            (i & 127) * 0x1p-7f + hue, // widest hue variation
////                                            (i + 90 & 255) * 0x1p-9f + 0.9f,
////                                            (i + 90 & 255) * 0x3p-10f + 0.125f,
////                                            (i + 90 & 255) * 0x3p-10f + 0.2f,
////                                            1f - (i + 90 & 255) * 0x1p-11f,
////                                            1f - (i + 90 & 255) * 0x1p-13f,
////                                            1f - (i & 255) * 0x1p-13f,
//                                            1f,
////                                            hiLo == 1 && i < 160 ? 0.5f + (i * 0x1p-9f) :
////                                            hiLo == 1 && i < 160 ? 0.6f + (i * 0x1p-9f) :
//
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.2f, 0.35f) * 0.75f, // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.2f, 0.35f) * 0.7f + 0.125f, // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.4f, 0.4f), // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 1.5f, 0.7f) * 0.85f + 0.05f, // biased
//                                            MathTools.square(MathTools.barronSpline(i * 0x1p-8f, 0.8f, 0.6f) - 0.3f) * 1.95f, // biased
////                                            (i * i * 0x1p-16f), //very dark
////                                            0.6f + TrigTools.cosTurns(i * 0x1p-9f) * 0.3f, // light, from 0.3f to 0.9f
////                                            0.55f + TrigTools.cosTurns(i * 0x1p-9f) * -0.35f, // light, from 0.2f to 0.9f
////                                            0.65f + TrigTools.cosTurns(i * 0x1p-10f) * 0.2f, // very light, from 0.65f to 0.85f, more are high
//                                            1f));
//                            gif.palette.paletteArray[i] = DescriptiveColor.toRGBA8888(DescriptiveColor.oklabByHSL((i + 100 & 255) * 0x1p-8f, 1f, i * 0x1p-10f + 0.5f, 1f));
//                        }
                        String ser = noise.getSeed() + "_" + System.currentTimeMillis();
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
                    case D: //dimension
                        dim = (dim ^ 1);
                        break;
                    case F: // frequency
//                        noise.setFrequency(NumberTools.sin(freq += 0.125f) * 0.25f + 0.25f + 0x1p-7f);
//                        noise.setFrequency((float) Math.exp((System.currentTimeMillis() >>> 9 & 7) - 5));
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
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
        float bright, c = (ctr & 1023) * 0.25f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare((float) noise.getNoise(x - 128, y - 128));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare((float) noise.getNoise(x - 128, y - 128, c));
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
        config.setTitle("SquidSquad Test: Noise Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new DisasterSimplexVisualizer(), config);
    }
}
