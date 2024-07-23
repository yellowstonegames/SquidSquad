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
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.RoughMath;
import com.github.tommyettinger.digital.TrigTools;
import com.github.yellowstonegames.grid.*;

import java.util.Arrays;

import static com.badlogic.gdx.Input.Keys.*;

/**
 */
public class INoiseComparison extends ApplicationAdapter {
    public static float mul =  6.03435f, gamma = 1f, mix = 0.5f, mixRaw = 0, bias = 0.5f, favor = 0f;
    /**
     * A decent approximation of {@link Math#exp(double)} for small float arguments, meant to be faster than Math's
     * version for floats at the expense of accuracy. This uses the 2/2 Padé
     * approximant to {@code Math.exp(power)}, but halves the argument to exp() before approximating, and squares it
     * before returning.The halving/squaring keeps the calculation in a more precise span for a larger domain. You
     * should not use this if your {@code power} inputs will be much higher than about 3 or lower than -3 .
     * <br>
     * Pretty much all the work for this was done by Wolfram Alpha.
     *
     * @param power what exponent to raise {@link com.github.tommyettinger.digital.MathTools#E} to
     * @return a rough approximation of E raised to the given power
     */
    public static float exp(float power) {
        power *= 0.5f;
        power = (12 + power * (6 + power)) / (12 + power * (-6 + power));
        return power * power;
    }

    /**
     * The cumulative distribution function for the normal distribution, with the range expanded to {@code [-1,1]}
     * instead of the usual {@code [0,1]} . This might be useful to bring noise functions (which sometimes have a range
     * of {@code -1,1}) from a very-centrally-biased form to a more uniformly-distributed form. The math here doesn't
     * exactly match the normal distribution's CDF because the goal was to handle inputs between -1 and 1, not the full
     * range of a normal-distributed variate (which is infinite). The distribution is very slightly different here from
     * the double-based overload, because this clamps inputs that would produce incorrect results from its approximation
     * of {@link Math#exp(double)} otherwise, whereas the double-based method uses the actual Math.exp().
     *
     * @param x a float between -1 and 1; will be clamped if outside that domain
     * @return a more-uniformly distributed value between -1 and 1
     */
    public static float redistributeNormal(float x) {
        final float xx = Math.min(x * x * 6.03435f, 6.03435f), axx = 0.1400122886866665f * xx;
        return Math.copySign((float) Math.sqrt(1.0051551f - exp(xx * (-1.2732395447351628f - axx) / (0.9952389057917015f + axx))), x);
    }
    public static float redistributeNormal(float x, float mul) {
        final float xx = Math.min(x * x * mul, 6.03435f), axx = 0.1400122886866665f * xx;
        return Math.copySign((float) Math.sqrt(1.0051551f - exp(xx * (-1.2732395447351628f - axx) / (0.9952389057917015f + axx))), x);
    }
    public static float redistributeNormalPrecise(float x) {
        final double xx = x * x * mul, axx = 0.1400122886866665 * xx;
        return Math.copySign((float) Math.sqrt(1.0 - Math.exp(xx * (-1.2732395447351628 - axx) / (1.0 + axx))), x);
    }
    public static float redistributeNormalPrecise(float x, double mul) {
        final double xx = x * x * mul, axx = 0.1400122886866665 * xx;
        return Math.copySign((float) Math.sqrt(1.0 - Math.exp(xx * (-1.2732395447351628 - axx) / (1.0 + axx))), x);
    }

    public static float redistributeCauchy(float x) {
        return TrigTools.atan((x-0.5f)/gamma)*TrigTools.PI_INVERSE + 0.5f;
    }
    public static float redistributeCauchyPrecise(float x) {
        return (float)(Math.atan((x-0.5)/gamma)/Math.PI+0.5);
    }

    /**
     * Redistributes a noise value {@code n} using the given {@code mul} and {@code mix} parameters. This is meant to
     * push high-octave noise results from being centrally biased to being closer to uniform. Getting the values right
     * probably requires tweaking them; for {@link SimplexNoise}, mul=2.3f and mix=0.75f works well with 2 or more
     * octaves (and not at all well for one octave, which can use mix=0.0f to avoid redistributing at all). This
     * variation takes n in the 0f to 1f range, inclusive, and returns a value in the same range.
     *
     * @param n a prepared noise value, between 0f and 1f inclusive
     * @param mul a positive multiplier where higher values make extreme results more likely; often around 2.3f
     * @param mix a blending amount between 0f and 1f where lower values keep {@code n} more; often around 0.75f
     * @return a noise value between 0f and 1f, inclusive
     */
    public static float redistributeConfigurable(float n, float mul, float mix) {
        final float x = (n - 0.5f) * 2f, xx = x * x * mul, axx = 0.1400122886866665f * xx;
        final float denormal = Math.copySign((float) Math.sqrt(1.0f - Math.exp(xx * (-1.2732395447351628f - axx) / (1.0f + axx))), x);
        return MathTools.lerp(n, denormal * 0.5f + 0.5f, mix);
    }

    /**
     * Redistributes a noise value {@code n} using the given {@code mul}, {@code mix}, and {@code bias} parameters. This
     * is meant to push high-octave noise results from being centrally biased to being closer to uniform. Getting the
     * values right probably requires tweaking them manually; for {@link SimplexNoise}, using mul=2.3f, mix=0.75f, and
     * bias=1f works well with 2 or more octaves (and not at all well for one octave, which can use mix=0.0f to avoid
     * redistributing at all). This variation takes n in the 0f to 1f range, inclusive, and returns a value in the same
     * range. You can give different bias values at different times to make noise that is more often high (when bias is
     * above 1) or low (when bias is between 0 and 1). Using negative bias has undefined results. Bias should typically
     * be calculated only when its value needs to change. If you have a variable {@code favor} that can have
     * any float value and high values for favor should produce higher results from this function, you can get bias with
     * {@code bias = (float)Math.exp(-favor);} .
     *
     * @param n a prepared noise value, between 0f and 1f inclusive
     * @param mul a positive multiplier where higher values make extreme results more likely; often around 2.3f
     * @param mix a blending amount between 0f and 1f where lower values keep {@code n} more; often around 0.75f
     * @param bias should be 1 to have no bias, between 0 and 1 for lower results, and above 1 for higher results
     * @return a noise value between 0f and 1f, inclusive
     */
    public static float redistributeConfigurable(float n, float mul, float mix, float bias) {
        final float x = (n - 0.5f) * 2f, xx = x * x * mul, axx = 0.1400122886866665f * xx;
        final float denormal = Math.copySign((float) Math.sqrt(1.0f - Math.exp(xx * (-1.2732395447351628f - axx) / (1.0f + axx))), x);
        return (float) Math.pow(MathTools.lerp(n, denormal * 0.5f + 0.5f, mix), bias);
    }

    public static float redistributeRough(float n, float mul, float mix) {
        final float x = (n - 0.5f) * 2f, xx = x * x * mul, axx = 0.1400122886866665f * xx;
        final float denormal = Math.copySign((float) Math.sqrt(1.0f - RoughMath.expRough(xx * (-1.2732395447351628f - axx) / (1.0f + axx))), x);
        return MathTools.lerp(n, denormal * 0.5f + 0.5f, mix);
    }
    public static float redistributeRougher(float n, float mul, float mix) {
        final float x = (n - 0.5f) * 2f, xx = x * x * mul, axx = 0.1400122886866665f * xx;
        final float denormal = Math.copySign((float) Math.sqrt(1.0f - RoughMath.expRougher(xx * (-1.2732395447351628f - axx) / (1.0f + axx))), x);
        return MathTools.lerp(n, denormal * 0.5f + 0.5f, mix);
    }

    /**
     *
     * @param n
     * @param mul often 3.3
     * @param mix often 0.91
     * @return
     */
    public static float redistributeTumble(float n, float mul, float mix) {
        final float x = (n - 0.5f) * 2f, xx = x * x * mul, axx = 0.1400122886866665f * xx;
        final float denormal = Math.copySign((float) Math.sqrt(1.0f - RoughMath.pow2Rough(xx * (-1.2732395447351628f - axx) / (1.0f + axx))), x);
        return MathTools.lerp(n, denormal * 0.5f + 0.5f, mix);
    }


    private final PerlinNoiseAnalysis analysis = new PerlinNoiseAnalysis(1L);
    private static final Interpolations.Interpolator watcher = new Interpolations.Interpolator("WATCHER", Interpolations.linearFunction){
        @Override
        public float apply(float alpha) {
            if(alpha < 0f) System.out.println(alpha + " is too low!");
            if(alpha > 1f) System.out.println(alpha + " is too high!");
            return super.apply(alpha);
        }
    };
    private static final Interpolations.Interpolator redistributor = new Interpolations.Interpolator("REDISTRIBUTOR",
            alpha -> redistributeNormal((alpha - 0.5f) * 2f) * 0.5f + 0.5f);
    private static final Interpolations.Interpolator redistributor2 = new Interpolations.Interpolator("REDISTRIBUTOR2",
            alpha -> redistributeNormalPrecise((alpha - 0.5f) * 2f) * 0.5f + 0.5f);
    private static final Interpolations.Interpolator redistributorC = new Interpolations.Interpolator("REDISTRIBUTORC",
            INoiseComparison::redistributeCauchy);
    private static final Interpolations.Interpolator redistributorC2 = new Interpolations.Interpolator("REDISTRIBUTORC2",
            INoiseComparison::redistributeCauchyPrecise);
    private static final Interpolations.Interpolator redistributorCC = new Interpolations.Interpolator("REDISTRIBUTORCC",
            x -> redistributeCauchy(redistributeCauchy(x)));
    private static final Interpolations.Interpolator redistributorL = new Interpolations.Interpolator("REDISTRIBUTORL",
            alpha -> MathTools.lerp(alpha, redistributeNormal((alpha - 0.5f) * 2f, mul) * 0.5f + 0.5f, mix));
    private static final Interpolations.Interpolator redistributorL2 = new Interpolations.Interpolator("REDISTRIBUTORL2",
            alpha -> MathTools.lerp(alpha, redistributeNormalPrecise((alpha - 0.5f) * 2f, mul) * 0.5f + 0.5f, mix));
    private static final Interpolations.Interpolator redistributorX = new Interpolations.Interpolator("REDISTRIBUTORX",
            x -> redistributeConfigurable(x, mul, mix));
    private static final Interpolations.Interpolator redistributorR = new Interpolations.Interpolator("REDISTRIBUTORR",
            x -> redistributeRough(x, mul, mix));
    private static final Interpolations.Interpolator redistributorT = new Interpolations.Interpolator("REDISTRIBUTORT",
            x -> redistributeTumble(x, mul, mix));
    private static final Interpolations.Interpolator redistributorRR = new Interpolations.Interpolator("REDISTRIBUTORRR",
            x -> redistributeRougher(x, mul, mix));
    private static final Interpolations.Interpolator redistributorXB = new Interpolations.Interpolator("REDISTRIBUTORXB",
            x -> redistributeConfigurable(x, mul, mix, bias));

    private static final Interpolations.Interpolator siney = new Interpolations.Interpolator("SINEY",
            x -> 0.5f + 0.5f * TrigTools.sinTurns(x));

    private static final Interpolations.Interpolator siney2 = new Interpolations.Interpolator("SINEY2",
            x -> 0.5f + Math.abs(x - 0.5f) * TrigTools.sinTurns(x - 0.5f));

    private static final Interpolations.Interpolator[] PREPARATIONS = {Interpolations.linear, Interpolations.smooth,
            Interpolations.smoother, redistributor, redistributor2, redistributorC, redistributorC2, redistributorCC,
            redistributorL, redistributorL2, redistributorX, redistributorR, redistributorT, redistributorRR,
            redistributorXB, siney, siney2};
    private int prep0 = 0;
    private int prep1 = 0;//PREPARATIONS.length - 1;

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
            new PerlueNoise(1L),
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
            new HoneyNoise(1L, 0.3f),
            new HoneyNoise(1L, 0.4f),
            new HoneyNoise(1L, 0.5f),
            new HoneyNoise(1L, 0.6f),
            new HoneyNoise(1L, 0.7f),
            new ColorNoise(1L),
            new TriplexNoise(1L),
            new BadgerNoise(1L),
            new SnakeNoise(1L),
            new Noise(1)
    };
    private int index0 = 5;
    private int index1 = 6;
    private final NoiseWrapper wrap0 = new NoiseWrapper(noises[index0], 1, 0.0625f, Noise.FBM, 1);
    private final NoiseWrapper wrap1 = new NoiseWrapper(noises[index1], 1, 0.0625f, Noise.FBM, 1);
    private final NoiseAdjustment adj0 = new NoiseAdjustment(wrap0, PREPARATIONS[prep0]);
    private final NoiseAdjustment adj1 = new NoiseAdjustment(wrap1, PREPARATIONS[prep1]);
    private int dim = 0; // this can be 0 through 4 inclusive; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 1f/32f;
    private boolean slice = false;
    private boolean hue = false;

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
        if(hue){
            n = (n + 1) - (int)(n + 1);
        } else {
//            n = TrigTools.sinTurns(n) * 0.5f + 0.5f;
            n = n * 0.5f + 0.5f;
        }
        freq0[Math.min(Math.max((int)(n * 256), 0), freq0.length-1)]++;
        return n;
    }
    public float prepare1(float n) {
        if(!hue){
//            n = TrigTools.sinTurns(n) * 0.5f + 0.5f;
            n = n * 0.5f + 0.5f;
            freq1[Math.min(Math.max((int)(n * 256), 0), freq1.length-1)]++;
        }
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
                    case NUM_9:
                    case NUMPAD_9:
                        adj0.setAdjustment(PREPARATIONS[prep0 = (prep0 + (UIUtils.shift() ? PREPARATIONS.length - 1 : 1)) % PREPARATIONS.length]);
                        break;
                    case EQUALS:
                    case NUM_2:
                    case NUMPAD_2:
                        adj1.setAdjustment(PREPARATIONS[prep1 = (prep1 + (UIUtils.shift() ? PREPARATIONS.length - 1 : 1)) % PREPARATIONS.length]);
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
                        wrap0.setFractalType((wrap0.getFractalType() + (UIUtils.shift() ? 4 : 1)) % 5);
                        if(!UIUtils.ctrl())
                            wrap1.setFractalType((wrap1.getFractalType() + (UIUtils.shift() ? 4 : 1)) % 5);
                        break;
                    case H: // higher octaves
                        wrap0.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        wrap1.setFractalOctaves(octaves + 1);
                        break;
                    case L: // lower octaves
                        wrap0.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        wrap1.setFractalOctaves(octaves + 1);
                        break;
                    case U:
                        hue = !hue;
                        break;
                    case BACKSLASH:
                        slice = !slice;
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case V: // view
                        System.out.println("mul = " + mul);
                        System.out.println("gamma = " + gamma);
                        System.out.println("mix = " + mix);
                        System.out.println("bias = " + bias);
                        System.out.println("Using " + PREPARATIONS[prep0].tag + " and " + PREPARATIONS[prep1].tag);
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
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
                            bright = prepare0(adj0.getNoiseWithSeed(x + c, y + c, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x + c, y + c, MathTools.GOLDEN_RATIO, MathTools.ROOT2, MathTools.ROOT3, MathTools.ROOT5, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(adj0.getNoiseWithSeed(x, y, c, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x, y, c, MathTools.GOLDEN_RATIO, MathTools.ROOT2, MathTools.ROOT3, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(adj0.getNoiseWithSeed(x, y, c, 1, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x, y, c, 1, MathTools.GOLDEN_RATIO, MathTools.ROOT2, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 3: {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(adj0.getNoiseWithSeed(x, y, c, 1, 1, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x, y, c, 1, 1, MathTools.GOLDEN_RATIO, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                }
                break;
                case 4: {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = prepare0(adj0.getNoiseWithSeed(x, y, c, 1, 1, 1, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x, y, c, 1, 1, 1, wrap1.getSeed()));
                            colorize(bright);
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
//                        bright = prepare0(adj0.getNoiseWithSeed(x * mul, y * mul, wrap0.getSeed()));
//                        colorize(bright);
//                        renderer.vertex(a, b, 0);
//                        bright = prepare1(adj1.getNoiseWithSeed(x * mul, y * mul, wrap1.getSeed()));
//                        colorize(bright);
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
                            bright = prepare0(adj0.getNoiseWithSeed(x + c, y + c, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x + c, y + c, MathTools.GOLDEN_RATIO, MathTools.ROOT2, MathTools.ROOT3, MathTools.ROOT5, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int a = 0, x = -width >> 1; a < width; a++, x++) {
                        for (int b = 0, y = -height >> 1; b < height; b++, y++) {
                            bright = prepare0(adj0.getNoiseWithSeed(x, y, c, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(a, b, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(x, y, c, MathTools.GOLDEN_RATIO, MathTools.ROOT2, MathTools.ROOT3, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(a + width, b, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        float xc = TrigTools.cosTurns(x * iWidth) * 32 + c, xs = TrigTools.sinTurns(x * iWidth) * 32 + c;
                        for (int y = 0; y < height; y++) {
                            float yc = TrigTools.cosTurns(y * iHeight) * 32 + c, ys = TrigTools.sinTurns(y * iHeight) * 32 + c;
                            bright = prepare0(adj0.getNoiseWithSeed(xc, yc, xs, ys, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(xc, yc, xs, ys, MathTools.GOLDEN_RATIO, MathTools.ROOT2, wrap1.getSeed()));
                            colorize(bright);
                            renderer.vertex(x + width, y, 0);
                        }
                    }
                    break;
                case 3: {
                    for (int x = 0; x < width; x++) {
                        float xc = TrigTools.cosTurns(x * iWidth) * 32, xs = TrigTools.sinTurns(x * iWidth) * 32;
                        for (int y = 0; y < height; y++) {
                            float yc = TrigTools.cosTurns(y * iHeight) * 32, ys = TrigTools.sinTurns(y * iHeight) * 32;
                            bright = prepare0(adj0.getNoiseWithSeed(xc, yc, xs, ys, c, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(xc, yc, xs, ys, c, MathTools.GOLDEN_RATIO, wrap1.getSeed()));
                            colorize(bright);
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
                            bright = prepare0(adj0.getNoiseWithSeed(
                                    xc, yc, zc, xs, ys, zs, wrap0.getSeed()));
                            colorize(bright);
                            renderer.vertex(x, y, 0);
                            bright = prepare1(adj1.getNoiseWithSeed(
                                    xc, yc, zc, xs, ys, zs, wrap1.getSeed()));
                            colorize(bright);
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
//                        bright = prepare0(adj0.getNoiseWithSeed(x * mul, y * mul, wrap0.getSeed()));
//                        colorize(bright);
//                        renderer.vertex(a, b, 0);
//                        bright = prepare1(adj1.getNoiseWithSeed(x * mul, y * mul, wrap1.getSeed()));
//                        colorize(bright);
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
    
    private void colorize(float v){
        if(hue)
            renderer.color(v);
        else
            renderer.color(v, v, v, 1f);
    }
    @Override
    public void render() {
        if(Gdx.input.isKeyPressed(M)) // multiplier for redistributor2
             mul *= (UIUtils.shift() ? 1.03125f : 1f/1.03125f);
        if(Gdx.input.isKeyPressed(G)) // multiplier for redistributorC
             gamma *= (UIUtils.shift() ? 1.03125f : 1f/1.03125f);
        if(Gdx.input.isKeyPressed(X)) // mixer for redistributorL
             mix = (float)Math.tanh(mixRaw += (UIUtils.shift() ? 0.03125f : -0.03125f)) * 0.5f + 0.5f;
        if(Gdx.input.isKeyPressed(B)) // bias
            bias = RoughMath.pow2Rougher(-(favor += (UIUtils.shift() ? 0.03125f : -0.03125f)));
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
