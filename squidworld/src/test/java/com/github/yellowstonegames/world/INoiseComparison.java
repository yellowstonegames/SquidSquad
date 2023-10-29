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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.*;

import java.util.Arrays;

import static com.badlogic.gdx.Input.Keys.*;

/**
 */
public class INoiseComparison extends ApplicationAdapter {

    private final PerlinNoiseAnalysis analysis = new PerlinNoiseAnalysis(1L);
    private static final Interpolations.Interpolator watcher = new Interpolations.Interpolator("WATCHER", Interpolations.linearFunction){
        @Override
        public float apply(float alpha) {
            if(alpha < 0f) System.out.println(alpha + " is too low!");
            if(alpha > 1f) System.out.println(alpha + " is too high!");
            return super.apply(alpha);
        }
    };
    private final INoise[] noises = new INoise[]{
            new SimplexNoise(1L),
            new FoamNoise(1L),
            new FoamplexNoise(1L),
//            new NoiseAdjustment(new SimplexNoise(1L), watcher),
            new SimplexNoiseHard(1L),
            new SimplexNoiseScaled(1L),
//            new OpenSimplex2(1L),
//            new OpenSimplex2Smooth(1L),
            new PerlinNoise(1L),
            new NoiseAdjustment(new PerlinNoise(1L), watcher),
            new CyclicNoise(1L, 5, 4),
            new FlanNoise(1L, 6),
            new TaffyNoise(1L, 6),
            new ValueNoise(1L),
            new HighDimensionalValueNoise(1L, 6),
            new BasicHashNoise(1, new FlawedPointHash.FlowerHash(1)),
            new CyclicNoise(1L, 1),
            new CyclicNoise(1L, 3),
            new SorbetNoise(1L, 1),
            new SorbetNoise(1L, 3),
            new NoiseAdjustment(analysis, Interpolations.linear), // limits output range
            new ToothNoise(),
    };
    private int index0 = 0;
    private int index1 = 4;
    private final NoiseWrapper wrap0 = new NoiseWrapper(noises[index0], 1, 0.0625f, Noise.FBM, 1);
    private final NoiseWrapper wrap1 = new NoiseWrapper(noises[index1], 1, 0.0625f, Noise.FBM, 1);
    private int dim = 0; // this can be 0 through 4 inclusive; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 1f/32f;
    private boolean slice = true;

    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;
    private static final float LIGHT_YELLOW = Color.toFloatBits(1f, 1f, 0.4f, 1f);

    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;

    private final int[] freq0 = new int[256];
    private final int[] freq1 = new int[256];

    public float prepare0(float n) {
        n = n * 0.5f + 0.5f;
        freq0[Math.min(Math.max((int)(n * 256), 0), freq0.length-1)]++;
        return n;
    }
    public float prepare1(float n) {
        n = n * 0.5f + 0.5f;
        freq1[Math.min(Math.max((int)(n * 256), 0), freq1.length-1)]++;
        return n;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height << 1, false, true, 0);
        view = new ScreenViewport();

        wrap0.setFractalOctaves(octaves);
        wrap1.setFractalOctaves(octaves);

        wrap0.setFractalType(Noise.FBM);
        wrap1.setFractalType(Noise.FBM);

        InputAdapter input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case P: //pause
                    case SPACE:
                        keepGoing = !keepGoing;
                        break;
                    case NUM_0:
                    case NUMPAD_0:
                        wrap0.setWrapped(noises[index0 = (index0 + (UIUtils.shift() ? noises.length - 1 : 1)) % noises.length]);
                        break;
                    case MINUS:
                    case NUM_1:
                    case NUMPAD_1:
                        wrap1.setWrapped(noises[index1 = (index1 + (UIUtils.shift() ? noises.length - 1 : 1)) % noises.length]);
                        break;
                    case C:
                        if (UIUtils.shift()) ctr--;
                        else ctr++;
                        break;
                    case E: {//earlier seed
                        long seed = wrap0.getSeed() - 1;
                        wrap0.setSeed(seed);
                        wrap1.setSeed(seed);
                        for (int i = 0; i < noises.length; i++) {
                            noises[i].setSeed(seed);
                        }
                        break;
                    }
                    case S: {//seed
                        long seed = wrap0.getSeed() + 1;
                        wrap0.setSeed(seed);
                        wrap1.setSeed(seed);
                        for (int i = 0; i < noises.length; i++) {
                            noises[i].setSeed(seed);
                        }
                        break;
                    }
                    case SLASH: {
                        long seed = Hasher.randomize3(wrap0.getSeed());
                        wrap0.setSeed(seed);
                        wrap1.setSeed(seed);
                        for (int i = 0; i < noises.length; i++) {
                            noises[i].setSeed(seed);
                        }
                        break;
                    }
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
                        freq *= (UIUtils.shift() ? 1.25f : 0.8f);
                        wrap0.setFrequency(freq);
                        wrap1.setFrequency(freq);
                        break;
                    case W: // fwactaw spiwaw
                        wrap0.setFractalSpiral(!wrap0.isFractalSpiral());
                        wrap1.setFractalSpiral(!wrap1.isFractalSpiral());
                        break;
                    case R: // fRactal type
                        wrap0.setFractalType((wrap0.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        if(!UIUtils.ctrl())
                            wrap1.setFractalType((wrap1.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        break;
                    case H: // higher octaves
                        wrap0.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        wrap1.setFractalOctaves(octaves + 1);
                        break;
                    case L: // lower octaves
                        wrap0.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        wrap1.setFractalOctaves(octaves + 1);
                        break;
                    case BACKSLASH:
                        slice = !slice;
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
        Arrays.fill(freq0, 0);
        Arrays.fill(freq1, 0);
        renderer.begin(view.getCamera().combined, GL20.GL_POINTS);
        float bright, c = ctr * 0.25f;
        if(slice){
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x + c, y + c, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x + c, y + c, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, 1, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, 1, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 3: {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, 1, 1, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, 1, 1, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                }
                break;
                case 4: {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, 1, 1, 1, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, 1, 1, 1, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                }
                break;
//            case 3:
//            case 4:
//            case 5:
//                float mul = (TrigTools.sin(c * 0.01f) + 1.5f) * 4f;
//                for (int a = 0, x = -width >> 1; a < width; a++, x++) {
//                    for (int b = 0, y = -height >> 1; b < height; b++, y++) {
//                        bright = prepare0(wrap0.getNoiseWithSeed(x * mul, y * mul, wrap0.getSeed()));
//                        renderer.color(bright, bright, bright, 1f);
//                        renderer.vertex(a, b, 0);
//                        bright = prepare1(wrap1.getNoiseWithSeed(x * mul, y * mul, wrap1.getSeed()));
//                        renderer.color(bright, bright, bright, 1f);
//                        renderer.vertex(a + width, b, 0);
//                    }
//                }
//                break;
            }
        } else {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x + c, y + c, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x + c, y + c, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int a = 0, x = -width >> 1; a < width; a++, x++) {
                        for (int b = 0, y = -height >> 1; b < height; b++, y++) {
                            bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(a, b, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(a + width, b, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        float xc = TrigTools.cosTurns(x * iWidth) * 32 + c, xs = TrigTools.sinTurns(x * iWidth) * 32 + c;
                        for (int y = 0; y < height; y++) {
                            float yc = TrigTools.cosTurns(y * iHeight) * 32 + c, ys = TrigTools.sinTurns(y * iHeight) * 32 + c;
                            bright = prepare0(wrap0.getNoiseWithSeed(xc, yc, xs, ys, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(xc, yc, xs, ys, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 3: {
                    for (int x = 0; x < width; x++) {
                        float xc = TrigTools.cosTurns(x * iWidth) * 32, xs = TrigTools.sinTurns(x * iWidth) * 32;
                        for (int y = 0; y < height; y++) {
                            float yc = TrigTools.cosTurns(y * iHeight) * 32, ys = TrigTools.sinTurns(y * iHeight) * 32;
                            bright = prepare0(wrap0.getNoiseWithSeed(xc, yc, xs, ys, c, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(xc, yc, xs, ys, c, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                }
                break;
                case 4: {
                    for (int x = 0; x < width; x++) {
                        float xc = TrigTools.cosTurns(x * iWidth) * 32 + c, xs = TrigTools.sinTurns(x * iWidth) * 32 + c;
                        for (int y = 0; y < height; y++) {
                            float yc = TrigTools.cosTurns(y * iHeight) * 32 + c, ys = TrigTools.sinTurns(y * iHeight) * 32 + c,
                                    zc = TrigTools.cosTurns((x - y) * 0.5f * iWidth) * 32 - c, zs = TrigTools.sinTurns((x - y) * 0.5f * iWidth) * 32 - c;
                            bright = prepare0(wrap0.getNoiseWithSeed(
                                    xc, yc, zc, xs, ys, zs, wrap0.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(wrap1.getNoiseWithSeed(
                                    xc, yc, zc, xs, ys, zs, wrap1.getSeed()));
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                }
                break;
//            case 3:
//            case 4:
//            case 5:
//                float mul = (TrigTools.sin(c * 0.01f) + 1.5f) * 4f;
//                for (int a = 0, x = -width >> 1; a < width; a++, x++) {
//                    for (int b = 0, y = -height >> 1; b < height; b++, y++) {
//                        bright = prepare0(wrap0.getNoiseWithSeed(x * mul, y * mul, wrap0.getSeed()));
//                        renderer.color(bright, bright, bright, 1f);
//                        renderer.vertex(a, b, 0);
//                        bright = prepare1(wrap1.getNoiseWithSeed(x * mul, y * mul, wrap1.getSeed()));
//                        renderer.color(bright, bright, bright, 1f);
//                        renderer.vertex(a + width, b, 0);
//                    }
//                }
//                break;
            }

        }
        renderer.end();
        if(Gdx.input.isKeyPressed(A)){ // Analysis
            renderer.begin(view.getCamera().combined, GL20.GL_LINES);
            for (int i = 0; i < 255; i++) {
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i, freq0[i] * 0x1p-3f, 0);
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+1, freq0[i+1] * 0x1p-3f, 0);
            }
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(255, 0, 0);
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(256, 0, 0);
            for (int i = 0; i < 255; i++) {
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+width, freq1[i] * 0x1p-3f, 0);
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+1+width, freq1[i+1] * 0x1p-3f, 0);
            }
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(255+width, 0, 0);
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(256+width, 0, 0);
            renderer.end();
        }
    }

    @Override
    public void render() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
        // standard clear the background routine for libGDX
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            ctr++;
        }
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
        config.setTitle("SquidSquad Test: Noise/NoiseWrapper Comparison");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width << 1, height);
        new Lwjgl3Application(new INoiseComparison(), config);
    }


    /*
        private final INoise[] noises = new INoise[]{
            new SimplexNoise(1L),
            new SimplexNoiseHard(1L),
            new SimplexNoiseScaled(1L),
            new OpenSimplex2(1L),
            new OpenSimplex2Smooth(1L),
            new PerlinNoise(1L),
            new CyclicNoise(1L, 5, 4),
            new FlanNoise(1L, 6),
            new TaffyNoise(1L, 6),
            new ValueNoise(1L),
            new HighDimensionalValueNoise(1L, 6),
            new BasicHashNoise(1, new FlawedPointHash.FlowerHash(1)),
            new INoise() {
                public final FoamNoiseStandalone standalone = new FoamNoiseStandalone(1L, 1.0);

                @Override
                public String getTag() {
                    return "FoSN";
                }

                @Override
                public long getSeed() {
                    return standalone.getSeed();
                }

                @Override
                public void setSeed(long seed) {
                    standalone.setSeed(seed);
                }

                @Override
                public int getMinDimension() {
                    return 2;
                }

                @Override
                public int getMaxDimension() {
                    return 4;
                }

                @Override
                public boolean canUseSeed() {
                    return true;
                }

                @Override
                public float getNoise(float x, float y) {
                    return (float)standalone.getNoise(x, y);
                }

                @Override
                public float getNoise(float x, float y, float z) {
                    return (float)standalone.getNoise(x, y, z);
                }

                @Override
                public float getNoise(float x, float y, float z, float w) {
                    return (float)standalone.getNoise(x, y, z, w);
                }

                @Override
                public float getNoiseWithSeed(float x, float y, long seed) {
                    return (float) FoamNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, long seed) {
                    return (float) FoamNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), z * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
                    return (float) FoamNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(),
                            z * standalone.getFrequency(), w * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u) {
                    return 0;
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u, float v) {
                    return 0;
                }
            },
            new INoise() {
                public final SimplexNoiseStandalone standalone = new SimplexNoiseStandalone(1L, 1.0);

                @Override
                public String getTag() {
                    return "SiSN";
                }

                @Override
                public long getSeed() {
                    return standalone.getSeed();
                }

                @Override
                public void setSeed(long seed) {
                    standalone.setSeed(seed);
                }

                @Override
                public int getMinDimension() {
                    return 2;
                }

                @Override
                public int getMaxDimension() {
                    return 4;
                }

                @Override
                public boolean canUseSeed() {
                    return true;
                }

                @Override
                public float getNoise(float x, float y) {
                    return (float)standalone.getNoise(x, y);
                }

                @Override
                public float getNoise(float x, float y, float z) {
                    return (float)standalone.getNoise(x, y, z);
                }

                @Override
                public float getNoise(float x, float y, float z, float w) {
                    return (float)standalone.getNoise(x, y, z, w);
                }

                @Override
                public float getNoiseWithSeed(float x, float y, long seed) {
                    return (float) SimplexNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, long seed) {
                    return (float) SimplexNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), z * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
                    return (float) SimplexNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(),
                            z * standalone.getFrequency(), w * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u) {
                    return 0;
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u, float v) {
                    return 0;
                }
            },
            new INoise() {
                public final ValueNoiseStandalone standalone = new ValueNoiseStandalone(1L, 1.0);

                @Override
                public String getTag() {
                    return "VaSN";
                }

                @Override
                public long getSeed() {
                    return standalone.getSeed();
                }

                @Override
                public void setSeed(long seed) {
                    standalone.setSeed(seed);
                }

                @Override
                public int getMinDimension() {
                    return 2;
                }

                @Override
                public int getMaxDimension() {
                    return 4;
                }

                @Override
                public boolean canUseSeed() {
                    return true;
                }

                @Override
                public float getNoise(float x, float y) {
                    return (float)standalone.getNoise(x, y);
                }

                @Override
                public float getNoise(float x, float y, float z) {
                    return (float)standalone.getNoise(x, y, z);
                }

                @Override
                public float getNoise(float x, float y, float z, float w) {
                    return (float)standalone.getNoise(x, y, z, w);
                }

                @Override
                public float getNoiseWithSeed(float x, float y, long seed) {
                    return (float) ValueNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, long seed) {
                    return (float) ValueNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), z * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
                    return (float) ValueNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(),
                            z * standalone.getFrequency(), w * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u) {
                    return 0;
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u, float v) {
                    return 0;
                }
            },
            new INoise() {
                public final DollopNoiseStandalone standalone = new DollopNoiseStandalone(1L, 1.0);

                @Override
                public String getTag() {
                    return "DoSN";
                }

                @Override
                public long getSeed() {
                    return standalone.getSeed();
                }

                @Override
                public void setSeed(long seed) {
                    standalone.setSeed(seed);
                }

                @Override
                public int getMinDimension() {
                    return 2;
                }

                @Override
                public int getMaxDimension() {
                    return 4;
                }

                @Override
                public boolean canUseSeed() {
                    return true;
                }

                @Override
                public float getNoise(float x, float y) {
                    return (float)standalone.getNoise(x, y);
                }

                @Override
                public float getNoise(float x, float y, float z) {
                    return (float)standalone.getNoise(x + z, y + z);
                }

                @Override
                public float getNoise(float x, float y, float z, float w) {
                    return (float)standalone.getNoise(x + z, y + w);
                }

                @Override
                public float getNoiseWithSeed(float x, float y, long seed) {
                    return (float) DollopNoiseStandalone.noiseWithSeed(
                            x * standalone.getFrequency(), y * standalone.getFrequency(), standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, long seed) {
                    return (float) DollopNoiseStandalone.noiseWithSeed(
                            (x+z) * standalone.getFrequency(), (y+z) * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
                    return (float) DollopNoiseStandalone.noiseWithSeed(
                            (x+z) * standalone.getFrequency(), (y+w) * standalone.getFrequency(),
                            standalone.getSeed());
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u) {
                    return 0;
                }

                @Override
                public float getNoise(float x, float y, float z, float w, float u, float v) {
                    return 0;
                }
            },
    };

     */
}
