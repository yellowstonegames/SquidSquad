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
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.function.FloatFloatToFloatBiFunction;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.grid.*;

import java.util.Arrays;

import static com.badlogic.gdx.Input.Keys.*;

public class MathComparison extends ApplicationAdapter {

    public static float exp(float n) {
        return (1 + n * (1 + n / 2 * (1 + n / 3 * (1 + n / 4)))) * BitConversion.intBitsToFloat(1065353216 + (int)(12102203 * n));
//        return 1 + n * (1 + n / 2 * (1 + n / 3 * (1 + n / 4 * (1 + n / 5 * (1 + n / 6)))));
    }

    /**
     * Range is from 0.0051585725 to 1.
     * @param x
     * @param y
     * @return
     */
    public static float gaussian2D(float x, float y) {
        x = Math.max(-3.5f, (x * x + y * y) * -0.5f);
        x = (12 + x * (6 + x))/(12 + x * (-6 + x));
        return x * x;
    }

    /**
     * Range is from 0 to 1.
     * @param x
     * @param y
     * @return
     */
    public static float gaussianFinite2D(float x, float y) {
        x = Math.max(-3.4060497f, (x * x + y * y) * -0.5f);
        x = (12 + x * (6 + x))/(12 + x * (-6 + x));
        return (x * x - 0.00516498f) * 1.0051918f;
    }

    private final FloatFloatToFloatBiFunction[] functions = new FloatFloatToFloatBiFunction[]{
            (x, y) -> (float) Math.exp(-x*x-y*y),
            (x, y) -> {x = (x * x + y * y) * -0.125f; x = (2 + x * (2 + x))*0.5f; x *= x; x *= x; return x * x;},

            MathComparison::gaussian2D,
            MathComparison::gaussianFinite2D,
            (x, y) -> {x = Math.max(-8, (x * x + y * y) * -0.5f); x = (12 + x * (6 + x))/(12 + x * (-6 + x)); return x * x;},
            (x, y) -> {x = Math.max(-8, (x * x + y * y) * -0.5f); float t1 = x * 0.5f, t2 = t1 * t1 * 0.333333333f; x = (1f + t1 + t2) / (1f - t1 + t2); return x * x;},
            (x, y) -> {x = (x * x + y * y) * -0.5f; float t1 = x * (1f / 2f), t2 = t1 * t1 * (1f / 3f); x = (1f + t1 + t2) / (1f - t1 + t2); return x * x;},
            (x, y) -> {x = (x * x + y * y) * -0.25f; float t1 = x * (1f / 2f), t2 = t1 * t1 * (1f / 3f); x = (1f + t1 + t2) / (1f - t1 + t2); return (x *= x) * x;},
            (x, y) -> {x = (-x * x - y * y); float t1 = x * (1f / 2f), t2 = t1 * x * (3f / 14f), t3 = t2 * x * (1f / 9f), t4 = t3 * x * (1f / 20f); x = (1f + t1 + t2 + t3 + t4) / (1f - t1 + t2 - t3 + t4); return x;},
            (x, y) -> {x = (-x * x - y * y) * 0.5f; x = (120 + x * (60 + x * (12 + x)))/(120 + x * (-60 + x * (12 - x))); return x * x;},
            (x, y) -> {x = (-x * x - y * y) * 0.25f; x = (6 + x * (6 + x * (3 + x))) / 6f; return (x *= x) * x;},
            (x, y) -> {x = -(x * x + y * y); return (2 + x * (2 + x)) * BitConversion.intBitsToFloat(0x3f000000 + (int)(12102203 * x));},
            (x, y) -> BitConversion.intBitsToFloat(1065353216 - (int)(12102203 * (x * x + y * y))),
            (x, y) -> exp(-x*x-y*y),
            (x, y) -> BitConversion.intBitsToFloat(1065353216 - (int)((12102203 + 0x1.6p-14f * BitConversion.floatToIntBits(x = x * x + y * y)) * x)),
            (x, y) -> BitConversion.intBitsToFloat(1065353216 + (int)((12102203 - 0x1.6p-14f * BitConversion.floatToIntBits(x = -x * x - y * y)) * x)),
            (x, y) -> BitConversion.intBitsToFloat(1065353216 - (int)((12102203 + 0x5.8p17 / (x = x * x + y * y)) * x)),
    };
    private int index0 = 0;
    private int index1 = 4;

    private ImmediateModeRenderer20 renderer;
    private boolean hue = false;
    private float freq = 0x1p-6f;

    private static final int width = 256, height = 256;
    private static final float LIGHT_YELLOW = Color.toFloatBits(1f, 1f, 0.4f, 1f);

    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;

    private final int[] freq0 = new int[256];
    private final int[] freq1 = new int[256];

    public float prepare0(float n) {
        n = (n + 1) - (int)(n + 1);
        freq0[Math.min(Math.max((int)(n * 256), 0), freq0.length-1)]++;
        return n;
    }
    public float prepare1(float n) {
        n = (n + 1) - (int)(n + 1);
        freq1[Math.min(Math.max((int)(n * 256), 0), freq1.length-1)]++;
        return n;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height << 1, false, true, 0);
        view = new ScreenViewport();

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
                        index0 = (index0 + (UIUtils.shift() ? functions.length - 1 : 1)) % functions.length;
                        System.out.println("Left side: index " + index0 + ", right side: index " + index1);
                        break;
                    case MINUS:
                    case NUM_1:
                    case NUMPAD_1:
                        index1 = (index1 + (UIUtils.shift() ? functions.length - 1 : 1)) % functions.length;
                        System.out.println("Left side: index " + index0 + ", right side: index " + index1);
                        break;
                    case C:
                        if (UIUtils.shift()) ctr--;
                        else ctr++;
                        break;
                    case F: // frequency
                        freq *= (UIUtils.shift() ? 1.25f : 0.8f);
                        break;
                    case U:
                        hue = !hue;
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
        for (int x = 0; x < width; x++) {
            float xc = (x - width * 0.5f) * freq;
            for (int y = 0; y < height; y++) {
                float yc = (y - height * 0.5f) * freq;
                bright = prepare0(functions[index0].applyAsFloat(xc, yc));
                colorize(bright);
                renderer.vertex(x, y, 0);
                bright = prepare1(functions[index1].applyAsFloat(xc, yc));
                colorize(bright);
                renderer.vertex(x + width, y, 0);
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
    
    private void colorize(float v){
        if(hue)
            renderer.color(DescriptiveColor.oklabIntToFloat(DescriptiveColor.oklabByHCL(v, 0.2f, 0.5f, 1f)));
        else
            renderer.color(v, v, v, 1f);
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
        new Lwjgl3Application(new MathComparison(), config);
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
                public boolean hasEfficientSetSeed() {
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
                public boolean hasEfficientSetSeed() {
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
                public boolean hasEfficientSetSeed() {
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
                public boolean hasEfficientSetSeed() {
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
