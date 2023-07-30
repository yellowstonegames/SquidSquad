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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.FastGif;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.digital.ArrayTools;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.ds.IntList;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.tommyettinger.digital.Interpolations;
import com.github.yellowstonegames.grid.BlueNoise;
import com.github.yellowstonegames.grid.CyclicNoise;
import com.github.yellowstonegames.grid.FlanNoise;
import com.github.yellowstonegames.grid.FlawedPointHash;
import com.github.yellowstonegames.grid.HighDimensionalValueNoise;
import com.github.yellowstonegames.grid.IPointHash;
import com.github.yellowstonegames.grid.IntPointHash;
import com.github.yellowstonegames.grid.LongPointHash;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.PhantomNoise;
import com.github.yellowstonegames.grid.TaffyNoise;
import com.github.yellowstonegames.grid.ValueNoise;
import com.github.yellowstonegames.world.random.SpectatorPointHash;

import java.util.Arrays;
import java.util.Comparator;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;
import static com.github.yellowstonegames.grid.BlueNoise.TILE_NOISE;

/**
 */
public class FFTVisualizer extends ApplicationAdapter {

    private final Noise noise = new Noise(322420472, 0.25f);
    private final IntPointHash iph = new IntPointHash();
    private final FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private final FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 32);
    private final FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 64);
    private final FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private final SpectatorPointHash spec = new SpectatorPointHash();
    private final IPointHash[] pointHashes = new IPointHash[] {iph, fnv, cube, rug, quilt, spec};
    private final PhantomNoise[] phantoms = new PhantomNoise[7];
    private final TaffyNoise[] taffies = new TaffyNoise[7];
    private final FlanNoise[] flans = new FlanNoise[7];
    private final HighDimensionalValueNoise[] hdvs = new HighDimensionalValueNoise[7];
    private final ValueNoise val = new ValueNoise(noise.getSeed());
    private final CyclicNoise cyclic = new CyclicNoise(noise.getSeed(), 1);
    private final float[][] points = new float[][]{new float[2], new float[3], new float[4], new float[5], new float[6]};
    private int hashIndex = 5;
    private static final int MODE_LIMIT = 26;
    private int mode = 24;
    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 3;
    private float freq = 0.125f;
    private float threshold = 0.5f;
    private boolean inverse;
    private boolean paused;
    private ImmediateModeRenderer20 renderer;

//    private static final int width = 400, height = 400;
//    private static final int width = 512, height = 512;
    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private final double[][] real = new double[width][height], imag = new double[width][height];
    private final double[][] realKnown = new double[width][height], imagKnown = new double[width][height];
    private final float[][] colors = new float[width][height];
    private final int[] freq0 = new int[256];
    private static final float LIGHT_YELLOW = Color.toFloatBits(1f, 1f, 0.4f, 1f);

    private InputAdapter input;

    private Viewport view;
    private long ctr = -128, startTime;
    private float changeZ = 0f, changeW = 0f, changeU = 0f, changeV = 0f;

    private Pixmap pm;
    private static final Comparator<Double> doubleComparator = new Comparator<Double>(){
        @Override
        public int compare(Double o1, Double o2) {
            return BitConversion.doubleToHighIntBits(o1 - o2);
        }
    };

    private FastGif gif;
    private Array<Pixmap> frames = new Array<>(256);

    public float basicPrepare(float n)
    {
        n = n * 0.5f + 0.5f;
        freq0[Math.min(Math.max((int)(n * 256), 0), freq0.length -1)]++;
        return n;
    }

    public static double basicPrepare(double n)
    {
        return n * 0.5 + 0.5;
    }

    private static final double root2pi = Math.sqrt(TrigTools.PI2_D);
    private static final double invRoot2pi = 1.0 / root2pi;
    public static double normalPDF(double x) {
        return Math.exp(-0.5*x*x)*invRoot2pi;
    }
    public static double normalPDFStigler(double x) {
        return Math.exp(-TrigTools.PI_D*x*x);
    }

    /**
     * Inverse to the {@link com.github.tommyettinger.random.EnhancedRandom#probit(double)} function; takes a normal-distributed input and returns a value between 0.0
     * and 1.0, both inclusive. This is based on a scaled error function approximation; the original approximation has a
     * maximum error of {@code 3.0e-7}, and scaling it shouldn't change that too drastically. The CDF of the normal
     * distribution is essentially the same as this method.
     * <br>
     * Equivalent to a scaled error function from Abramowitz and Stegun, 1964; equation 7.1.27 .
     * See <a href="https://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions">Wikipedia</a>.
     * @param x any finite double, typically normal-distributed but not necessarily
     * @return a double between 0 and 1, inclusive
     */
    public double normalCDF(final double x) {
        final double a1 = 0.0705230784, a2 = 0.0422820123, a3 = 0.0092705272, a4 = 0.0001520143, a5 = 0.0002765672, a6 = 0.0000430638;
        final double sign = Math.signum(x), y1 = sign * x * 0.7071067811865475, y2 = y1 * y1, y3 = y1 * y2, y4 = y2 * y2, y5 = y2 * y3, y6 = y3 * y3;
        double n = 1.0 + a1 * y1 + a2 * y2 + a3 * y3 + a4 * y4 + a5 * y5 + a6 * y6;
        n *= n;
        n *= n;
        n *= n;
        return sign * (0.5 - 0.5 / (n * n)) + 0.5;
    }

    /**
     * Error function from Abramowitz and Stegun, 1964; equation 7.1.27 .
     * See <a href="https://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions">Wikipedia</a>.
     * @param x any finite double
     * @return a double between -1 and 1, inclusive
     */
    public double erf(final double x) {
        final double a1 = 0.0705230784, a2 = 0.0422820123, a3 = 0.0092705272, a4 = 0.0001520143, a5 = 0.0002765672, a6 = 0.0000430638;
        final double sign = Math.signum(x), y1 = sign * x, y2 = y1 * y1, y3 = y1 * y2, y4 = y2 * y2, y5 = y2 * y3, y6 = y3 * y3;
        double n = 1.0 + a1 * y1 + a2 * y2 + a3 * y3 + a4 * y4 + a5 * y5 + a6 * y6;
        n *= n;
        n *= n;
        n *= n;
        return sign * (1.0 - 1.0 / (n * n));
    }

    @Override
    public void create() {
        for (int i = 0; i < 7; i++) {
            phantoms[i] = new PhantomNoise(noise.getSeed() + ~i * 55555555L, 2 + i);
            taffies[i] = new TaffyNoise(noise.getSeed()+ ~i * 55555555L, 2 + i);
            flans[i] = new FlanNoise(noise.getSeed()+ ~i * 55555555L, 2 + i);
            hdvs[i] = new HighDimensionalValueNoise(noise.getSeed()+ ~i * 55555555L, 2 + i);
        }
        noise.setNoiseType(Noise.TAFFY_FRACTAL);
        noise.setPointHash(pointHashes[hashIndex]);
//        Pixmap pm = new Pixmap(Gdx.files.internal("special/BlueNoise512x512.png"));
//        pm = new Pixmap(Gdx.files.internal("special/BlueNoiseTri256x256.png"));
        pm = new Pixmap(Gdx.files.local("out/blueNoise/BlueNoiseOmniTiling8x8.png"));
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                realKnown[x][y] = ((pm.getPixel(x, y) >> 24) + 0.5);
            }
        }
        Fft.transformWindowless2D(realKnown, imagKnown);
        gif = new FastGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.GRADIENT_NOISE);
        gif.setDitherStrength(0.2f);
        gif.palette = new PaletteReducer(
                new int[] {
                        0x000000FF, 0x010101FF, 0x020202FF, 0x030303FF, 0x040404FF, 0x050505FF, 0x060606FF, 0x070707FF,
                        0x080808FF, 0x090909FF, 0x0A0A0AFF, 0x0B0B0BFF, 0x0C0C0CFF, 0x0D0D0DFF, 0x0E0E0EFF, 0x0F0F0FFF,
                        0x101010FF, 0x111111FF, 0x121212FF, 0x131313FF, 0x141414FF, 0x151515FF, 0x161616FF, 0x171717FF,
                        0x181818FF, 0x191919FF, 0x1A1A1AFF, 0x1B1B1BFF, 0x1C1C1CFF, 0x1D1D1DFF, 0x1E1E1EFF, 0x1F1F1FFF,
                        0x202020FF, 0x212121FF, 0x222222FF, 0x232323FF, 0x242424FF, 0x252525FF, 0x262626FF, 0x272727FF,
                        0x282828FF, 0x292929FF, 0x2A2A2AFF, 0x2B2B2BFF, 0x2C2C2CFF, 0x2D2D2DFF, 0x2E2E2EFF, 0x2F2F2FFF,
                        0x303030FF, 0x313131FF, 0x323232FF, 0x333333FF, 0x343434FF, 0x353535FF, 0x363636FF, 0x373737FF,
                        0x383838FF, 0x393939FF, 0x3A3A3AFF, 0x3B3B3BFF, 0x3C3C3CFF, 0x3D3D3DFF, 0x3E3E3EFF, 0x3F3F3FFF,
                        0x404040FF, 0x414141FF, 0x424242FF, 0x434343FF, 0x444444FF, 0x454545FF, 0x464646FF, 0x474747FF,
                        0x484848FF, 0x494949FF, 0x4A4A4AFF, 0x4B4B4BFF, 0x4C4C4CFF, 0x4D4D4DFF, 0x4E4E4EFF, 0x4F4F4FFF,
                        0x505050FF, 0x515151FF, 0x525252FF, 0x535353FF, 0x545454FF, 0x555555FF, 0x565656FF, 0x575757FF,
                        0x585858FF, 0x595959FF, 0x5A5A5AFF, 0x5B5B5BFF, 0x5C5C5CFF, 0x5D5D5DFF, 0x5E5E5EFF, 0x5F5F5FFF,
                        0x606060FF, 0x616161FF, 0x626262FF, 0x636363FF, 0x646464FF, 0x656565FF, 0x666666FF, 0x676767FF,
                        0x686868FF, 0x696969FF, 0x6A6A6AFF, 0x6B6B6BFF, 0x6C6C6CFF, 0x6D6D6DFF, 0x6E6E6EFF, 0x6F6F6FFF,
                        0x707070FF, 0x717171FF, 0x727272FF, 0x737373FF, 0x747474FF, 0x757575FF, 0x767676FF, 0x777777FF,
                        0x787878FF, 0x797979FF, 0x7A7A7AFF, 0x7B7B7BFF, 0x7C7C7CFF, 0x7D7D7DFF, 0x7E7E7EFF, 0x7F7F7FFF,
                        0x808080FF, 0x818181FF, 0x828282FF, 0x838383FF, 0x848484FF, 0x858585FF, 0x868686FF, 0x878787FF,
                        0x888888FF, 0x898989FF, 0x8A8A8AFF, 0x8B8B8BFF, 0x8C8C8CFF, 0x8D8D8DFF, 0x8E8E8EFF, 0x8F8F8FFF,
                        0x909090FF, 0x919191FF, 0x929292FF, 0x939393FF, 0x949494FF, 0x959595FF, 0x969696FF, 0x979797FF,
                        0x989898FF, 0x999999FF, 0x9A9A9AFF, 0x9B9B9BFF, 0x9C9C9CFF, 0x9D9D9DFF, 0x9E9E9EFF, 0x9F9F9FFF,
                        0xA0A0A0FF, 0xA1A1A1FF, 0xA2A2A2FF, 0xA3A3A3FF, 0xA4A4A4FF, 0xA5A5A5FF, 0xA6A6A6FF, 0xA7A7A7FF,
                        0xA8A8A8FF, 0xA9A9A9FF, 0xAAAAAAFF, 0xABABABFF, 0xACACACFF, 0xADADADFF, 0xAEAEAEFF, 0xAFAFAFFF,
                        0xB0B0B0FF, 0xB1B1B1FF, 0xB2B2B2FF, 0xB3B3B3FF, 0xB4B4B4FF, 0xB5B5B5FF, 0xB6B6B6FF, 0xB7B7B7FF,
                        0xB8B8B8FF, 0xB9B9B9FF, 0xBABABAFF, 0xBBBBBBFF, 0xBCBCBCFF, 0xBDBDBDFF, 0xBEBEBEFF, 0xBFBFBFFF,
                        0xC0C0C0FF, 0xC1C1C1FF, 0xC2C2C2FF, 0xC3C3C3FF, 0xC4C4C4FF, 0xC5C5C5FF, 0xC6C6C6FF, 0xC7C7C7FF,
                        0xC8C8C8FF, 0xC9C9C9FF, 0xCACACAFF, 0xCBCBCBFF, 0xCCCCCCFF, 0xCDCDCDFF, 0xCECECEFF, 0xCFCFCFFF,
                        0xD0D0D0FF, 0xD1D1D1FF, 0xD2D2D2FF, 0xD3D3D3FF, 0xD4D4D4FF, 0xD5D5D5FF, 0xD6D6D6FF, 0xD7D7D7FF,
                        0xD8D8D8FF, 0xD9D9D9FF, 0xDADADAFF, 0xDBDBDBFF, 0xDCDCDCFF, 0xDDDDDDFF, 0xDEDEDEFF, 0xDFDFDFFF,
                        0xE0E0E0FF, 0xE1E1E1FF, 0xE2E2E2FF, 0xE3E3E3FF, 0xE4E4E4FF, 0xE5E5E5FF, 0xE6E6E6FF, 0xE7E7E7FF,
                        0xE8E8E8FF, 0xE9E9E9FF, 0xEAEAEAFF, 0xEBEBEBFF, 0xECECECFF, 0xEDEDEDFF, 0xEEEEEEFF, 0xEFEFEFFF,
                        0xF0F0F0FF, 0xF1F1F1FF, 0xF2F2F2FF, 0xF3F3F3FF, 0xF4F4F4FF, 0xF5F5F5FF, 0xF6F6F6FF, 0xF7F7F7FF,
                        0xF8F8F8FF, 0xF9F9F9FF, 0xFAFAFAFF, 0xFBFBFBFF, 0xFCFCFCFF, 0xFDFDFDFF, 0xFEFEFEFF, 0xFFFFFFFF,
                });
        IntList g = ColorGradients.toRGBA8888(ColorGradients.appendGradientChain(new IntList(256), 256, Interpolation.smooth::apply,
                // cool blue
                DescriptiveColor.oklabByHSL(0.68f, 0.85f, 0.2f, 1f),
                DescriptiveColor.oklabByHSL(0.70f, 0.95f, 0.4f, 1f),
                DescriptiveColor.oklabByHSL(0.62f, 1f, 0.55f, 1f),
                DescriptiveColor.oklabByHSL(0.65f, 0.7f, 0.8f, 1f)
                // rosy
//                DescriptiveColor.oklabByHSL(0.98f, 0.85f, 0.2f, 1f),
//                DescriptiveColor.oklabByHSL(0.00f, 0.95f, 0.4f, 1f),
//                DescriptiveColor.oklabByHSL(0.02f, 1f, 0.55f, 1f),
//                DescriptiveColor.oklabByHSL(0.01f, 0.7f, 0.8f, 1f)
        ));
        g.toArray(gif.palette.paletteArray);

        startTime = TimeUtils.millis();
        renderer = new ImmediateModeRenderer20(width * height * 2, false, true, 0);
        view = new ScreenViewport();
        input = new InputAdapter(){
            @Override
            public boolean keyUp(int keycode) {
                int s;
                long ls;
                switch (keycode) {
                    case MINUS:
                        mode = (mode + MODE_LIMIT - 1) % MODE_LIMIT;
                        break;
                    case EQUALS:
                        mode++;
                        mode %= MODE_LIMIT;
                        break;
                    case SPACE:
                        paused = !paused;
                        break;
                    case C:
                        startTime--;
                        break;
                    case E: //earlier seed
                        s = (int)(ls = noise.getSeed() - 1);
                        noise.setSeed(s);
                        cube.setState(s);
                        rug.setState(s);
                        quilt.setState(s);
                        cyclic.setSeed(ls);
                        val.setSeed(s);
                        for (int i = 0; i < taffies.length; i++) {
                            taffies[i].setSeed(ls);
                            flans[i].setSeed(ls);
                            hdvs[i].setSeed(ls);
                        }
                        System.out.println("Using seed " + s);
                        break;
                    case S: //seed after
                        s = (int)(ls = noise.getSeed() + 1);
                        noise.setSeed(s);
                        cube.setState(s);
                        rug.setState(s);
                        quilt.setState(s);
                        cyclic.setSeed(ls);
                        val.setSeed(s);
                        for (int i = 0; i < taffies.length; i++) {
                            taffies[i].setSeed(ls);
                            flans[i].setSeed(ls);
                            hdvs[i].setSeed(ls);
                        }
                        System.out.println("Using seed " + s);
                        break;
                    case N: // noise type
                        if(mode == 0 || mode >= 12)
                            noise.setNoiseType((noise.getNoiseType() + (UIUtils.shift() ? 17 : 1)) % 18);
                        break;
                    case ENTER:
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case U: //blUr
                        noise.setSharpness(noise.getSharpness() + (UIUtils.shift() ? 0.05f : -0.05f));
                        break;
                    case F: // frequency
                        noise.setFrequency((float) Math.sin(freq += 0.125f) * 0.1f + 0.11f);
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 3 : 1)) & 3);
                        break;
                    case G: // Glitch!
                        noise.setPointHash(pointHashes[hashIndex = (hashIndex + (UIUtils.shift() ? pointHashes.length - 1 : 1)) % pointHashes.length]);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        cyclic.setOctaves(octaves + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        cyclic.setOctaves(octaves + 1);
                        break;
                    case I: // inverse mode
                        if (inverse = !inverse) {
                            noise.setFractalLacunarity(0.5f);
                            noise.setFractalGain(2f);
                        } else {
                            noise.setFractalLacunarity(2f);
                            noise.setFractalGain(0.5f);
                        }
                        break;
                    case K: // sKip
                        startTime -= 1000000L;
                        break;
                    case W: // whirl, like a spiral
                        noise.setFractalSpiral(!noise.isFractalSpiral());
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                    break;
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        Arrays.fill(freq0, 0);
        if(Gdx.input.isKeyPressed(UP))
            threshold = Math.min(1, threshold + (1f/255f));
        else if(Gdx.input.isKeyPressed(DOWN))
            threshold = Math.max((1f/255f), threshold - (1f/255f));
        if(Gdx.input.isKeyPressed(M))
            noise.setMutation(noise.getMutation() + (UIUtils.shift() ? -Gdx.graphics.getDeltaTime() : Gdx.graphics.getDeltaTime()));
//// specific thresholds: 32, 96, 160, 224
//        threshold = (TimeUtils.millis() >>> 10 & 3) * 0x40p-8f + 0x20p-8f;
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, nf = noise.getFrequency(), c = (paused ? startTime : TimeUtils.timeSinceMillis(startTime))
                * (noise.getNoiseType() >= Noise.MUTANT ? 0x3p-10f : 0x1p-10f) / nf, xx, yy, cc;
        double db;
        ArrayTools.fill(imag, 0.0);
        if(mode == 0) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x + c, y + c));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, c));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, c, 0x1p-4f * (x + y - c)));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        xx = x * 0.5f;
                        for (int y = 0; y < height; y++) {
                            yy = y * 0.5f;
                            bright = basicPrepare(noise.getConfiguredNoise(
                                    c + xx, xx - c, yy - c,
                                    c - yy, xx + yy));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    for (int x = 0; x < width; x++) {
                        xx = x * 0.5f;
                        for (int y = 0; y < height; y++) {
                            yy = y * 0.5f;
                            bright = basicPrepare(noise.getConfiguredNoise(
                                    c + xx, xx - c, yy - c,
                                    c - yy, xx + yy, yy - xx));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 1){
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * odd256(x, y, (int) noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * IntPointHash.hash256(x, y, (int)noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * LongPointHash.hash256(x, y, noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * Noise.HastyPointHash.hash256(x, y, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * fancy256(x - (width >>> 1), y - (height >>> 1), (int)noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * castle256(x, y, noise.getSeed()));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
//                case 1:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * hash256(x, y, ctr, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
//                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * hash256(x, y, ctr, x + y - ctr, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
//                case 3:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * hash256(ctr + x, x - ctr, y - ctr,
//                                    ctr - y, x + y, y - x, noise.getSeed()));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
//                    break;
            }
        }
        else if(mode == 2) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 5);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x + ct, y + ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct, 1)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & rug.hash(x, y, ct, 1, 11, 111)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 3) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 5);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x + ct, y + ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct, 1)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * (0xFFFFFFFFL & quilt.hash(x, y, ct, 1, 11, 111)));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 4) {
            int ct = (int)(paused ? startTime : TimeUtils.timeSinceMillis(startTime) >>> 9);
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x + ct, y + ct))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct, 1))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = 0x1p-32 * ((cube.hash(x, y, ct, 1, 11, 111))&0xFFFFFFFFL));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 5) {
//            norm.clear();
            double maxReal = 0.0, maxImag = 0.0;
            //// Set up an initial Fourier transform for this to invert
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    real[x][y] = realKnown[x][y];
                    imag[x][y] = imagKnown[x][y];
                 }
            }

//            //// This is likely incorrect... imag probably also needs some values.
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height >>> 1; y++) {
//                    final double hx = 1.0 - Math.abs(x - width * 0.5 + 0.5) / 255.5, hy = 1.0 - (height * 0.5 - 0.5 - y) / 255.5;
//                    final double a = Math.sqrt(hx * hx + hy * hy);
//                    norm.put(Coord.get(x + 256 & 511, y + 256 & 511),
//                            real[x][y] = real[width - 1 - x][height - 1 - y] =
//                            (1f/255f) * IntPointHash.hash256(x, y, noise.getSeed()) * MathUtils.clamp((a * a * a * (a * (a * 6.0 -15.0) + 10.0) - 0.125), 0.0, 1.0));
//                 }
//            }
//            real[width >>> 1][height >>> 1] = 1.0;
//            real[(width >>> 1)-1][(height >>> 1)] = 1.0;
//            real[(width >>> 1)-1][(height >>> 1)-1] = 1.0;
//            real[(width >>> 1)][(height >>> 1)-1] = 1.0;
//            norm.put(Coord.get(width >>> 1, (height >>> 1)-1), 1.0);
//            norm.put(Coord.get((width >>> 1) - 1, (height >>> 1)-1), 1.0);

            //// Done setting up the initial Fourier transform

            Fft.transformWindowless2D(imag, real);

            //// re-normalize

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    maxReal = Math.max(Math.abs(real[x][y]), maxReal);
                    maxImag = Math.max(Math.abs(imag[x][y]), maxImag);
                }
            }
            maxReal = 1.0 / maxReal;
//            maxImag = 1.0 / maxImag;

            for (int x = 0; x < real.length; x++) {
                for (int y = 0; y < real[x].length; y++) {
                    real[x][y] = normalPDFStigler(real[x][y] * maxReal);
//                    imag[x][y] = normalPDFStigler(imag[x][y] * maxImag);
                }
            }
//
//            norm.shuffle(shuffler);
//            norm.sortByValue(doubleComparator);
//            final int ns = norm.size();
//            final double den = (ns - 1.0);
//            for (int i = 0; i < ns; i++) {
//                final Coord co = norm.keyAt(i);
//                real[co.x][co.y] = real[width - 1 - co.x][height - 1 - co.y] = i / den;
//            }
//            shuffler.setState(0x1234567890ABCDEFL);
            //// done re-normalizing

            switch (dim & 1) {
                case 0:
                    Fft.getColors(real, imag, colors);
                    if(Gdx.input.isKeyJustPressed(P)) {
                        for (int i = 0; i < 256; i++) {
                            System.out.printf("%3d: %d\n", i, Fft.histogram[i]);
                        }
                    }
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        renderer.color(colors[x][y]);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
                case 1:
                    Fft.getColorsThreshold(real, imag, colors, threshold);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        renderer.color(colors[x][y]);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            }

//            Color color = new Color(255);
//            int ic;
//            for (int x = 0; x < width; x++) {
//                for (int y = 0; y < height; y++) {
//                    ic = pm.getPixel(x, y);
//                    real[x][y] = (ic >>> 24) / 255.0;
//                    Color.rgba8888ToColor(color, ic);
//                    renderer.color(color);
//                    renderer.vertex(x, y, 0);
//                }
//            }
        }
        else if(mode == 6){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (BlueNoise.get(x, y, TILE_NOISE[(int)noise.getSeed() & 63]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * (BlueNoise.getSeeded(x, y, noise.getSeed()) + 128));
                            bright = (float) (db = (1f/255f) * (BlueNoise.getSeeded(x, y, (int)noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (BlueNoise.getSeeded(x, y, (int)noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (getSeededOmniTiling(x, y, (int)noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 7){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (BlueNoise.get(x, y, BlueNoise.TILE_TRI_NOISE[(int)noise.getSeed() & 63]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (BlueNoise.get(x, y, BlueNoise.TILE_TRI_NOISE[0]) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (float) (db = (1f/255f) * (BlueNoise.getSeededTriangular(x, y, noise.getSeed()) + 128));
//                            real[x][y] = db;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
                    Color color = new Color(255);
                    int ic;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            ic = pm.getPixel(x, y);
                            real[x][y] = (ic >>> 24) / 255.0;
                            Color.rgba8888ToColor(color, ic);
                            renderer.color(color);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f/255f) * (BlueNoise.getSeededTriangular(x, y, (int)noise.getSeed()) + 128));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 8){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.get(x, y, TILE_NOISE[(int)noise.getSeed() & 63]) & 255) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
//                            bright = (1f/255f) * (BlueNoise.getSeeded(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            bright = (1f/255f) * (BlueNoise.getSeeded(x, y, (int)noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.getSeeded(x, y, (int) noise.getSeed()) & 255) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.getSeeded(x, y, (int) noise.getSeed()) + 128 ^ 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 9){
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.get(x, y, BlueNoise.TILE_TRI_NOISE[(int) noise.getSeed() & 63]) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.get(x, y, BlueNoise.TILE_TRI_NOISE[0]) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bright = (1f/255f) * (BlueNoise.getSeededTriangular(x, y, noise.getSeed()) + 128) <= threshold ? 1 : 0;
//                            real[x][y] = bright;
//                            renderer.color(bright, bright, bright, 1f);
//                            renderer.vertex(x, y, 0);
//                        }
//                    }
                    int ic;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            ic = pm.getPixel(x, y);
                            real[x][y] = bright = (ic >>> 24) * (1f/255f) <= threshold ? 1f : 0f;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (1f/255f) * (BlueNoise.getSeededTriangular(x, y, (int) noise.getSeed()) + 128) <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }
        else if(mode == 10) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (getWobbled(x, y) + 128) / 255f);
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (getWobbled(x, y) + getWobbled(y + 421, x + 107) + 256) / 510f);
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
            }
        }
        else if(mode == 11) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (getWobbled(x, y) + 128) / 255f <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                default:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (getWobbled(x, y) + getWobbled(y + 421, x + 107) + 256) / 510f <= threshold ? 1 : 0;
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }

            }
        } else if(mode == 12) {
            switch (dim) {
                case 0:
                    changeZ += Gdx.graphics.getDeltaTime() * 10f;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, changeZ, changeW));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    changeW += Gdx.graphics.getDeltaTime() * 10f;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, changeZ, changeW));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    changeZ += Gdx.graphics.getDeltaTime() * 10f;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, changeZ, changeW, changeU));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    changeW += Gdx.graphics.getDeltaTime() * 10f;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, changeZ, changeW, changeU));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    changeU += Gdx.graphics.getDeltaTime() * 10f;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = basicPrepare(noise.getConfiguredNoise(x, y, changeZ, changeW, changeU));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 13) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
//                    long rs = Hasher.randomize1(noise.getSeed());
//                    int sx = (int)(rs >>> 32), sy = (int)rs;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            float cx = (c+x)*fr;
                            float cy = (c+y)*fr;
//                            int idx = (int) (sx + cx * 1357 + cy * 421);
//                            float tx = (cos(cx)
//                                    + SIN_TABLE[idx & TABLE_MASK]
//                                    + SIN_TABLE[idx + 4096 & TABLE_MASK]
//                                    + SIN_TABLE[sx & TABLE_MASK]*cy + sin(SIN_TABLE[sx + 4096 & TABLE_MASK]*cx)
//                            );
//                            idx = (int) (sy + cy * 1357 + cx * 421);
//                            float ty = (cos(cy)
//                                    + SIN_TABLE[idx & TABLE_MASK]
//                                    + SIN_TABLE[idx + 4096 & TABLE_MASK]
//                                    + SIN_TABLE[sy & TABLE_MASK]*cx + sin(SIN_TABLE[sy + 4096 & TABLE_MASK]*cy)
//                            );
//                            bright = MathTools.swayTight((tx+ty) * 0.25f);
                            bright = basicPrepare(phantoms[dim].getNoise2D(cx, cy));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(phantoms[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(phantoms[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(phantoms[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(phantoms[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }

        } else if(mode == 14) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            points[dim][0] = (c+x)*fr;
                            points[dim][1] = (c+y)*fr;
//                            float cx = (c+x)*fr;
//                            float cy = (c+y)*fr;
//                            bright = basicPrepare(taffies[dim].getNoise(points[dim]));
                            bright = basicPrepare(taffies[dim].getNoise2D(points[dim][0], points[dim][1]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(taffies[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(taffies[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(taffies[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(taffies[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 15) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            float cx = (c+x)*fr;
                            float cy = (c+y)*fr;
                            bright = basicPrepare(noise.singleTaffy((int) noise.getSeed(), cx, cy));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(noise.singleTaffyVarargs((int) noise.getSeed(), points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(noise.singleTaffyVarargs((int) noise.getSeed(), points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(noise.singleTaffyVarargs((int) noise.getSeed(), points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(noise.singleTaffyVarargs((int) noise.getSeed(), points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 16) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = (c+x)*fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = (c+y)*fr;
                            bright = basicPrepare(flans[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(flans[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(flans[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(flans[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(flans[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if (mode == 17) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        points[0][0] = (c+x)*fr;
                        for (int y = 0; y < height; y++) {
                            points[0][1] = (c+y)*fr;
                            bright = basicPrepare(cyclic.getNoise(points[0][0], points[0][1]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[1][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[1][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[1][1] = y * fr;
                            bright = basicPrepare(cyclic.getNoise(points[1][0], points[1][1], points[1][2]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(cyclic.getNoise(points[2][0], points[2][1], points[2][2], points[2][3]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(cyclic.getNoise(points[3][0], points[3][1], points[3][2], points[3][3], points[3][4]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                default:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(cyclic.getNoise(points[4][0], points[4][1], points[4][2], points[4][3], points[4][4], points[4][5]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;

            }
        } else if(mode == 18) {
            if((dim & 1) == 0) {
                for (int x = 0; x < width; x++) {
                    float distX = x - (width >>> 1);
                    for (int y = 0; y < height; y++) {
                        float distY = y - (height >>> 1);
                        float theta = TrigTools.atan2Turns(distY, distX) * 8f;
                        int flip = -((int)theta & 1) | 1;
                        theta *= flip;
                        float len = (float) Math.sqrt(distX * distX + distY * distY);
                        float shrunk = len * 0.125f;
                        bright = basicPrepare(noise.getConfiguredNoise(TrigTools.cosTurns(theta) * shrunk, TrigTools.sinTurns(theta) * shrunk, len - c));
                        real[x][y] = bright;
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            } else {
                for (int x = 0; x < width; x++) {
                    float distX = x - (width >>> 1);
                    for (int y = 0; y < height; y++) {
                        float distY = y - (height >>> 1);
                        float len = (float) Math.sqrt(distX * distX + distY * distY);
                        float shrunk = len * 0.08f;
                        float theta = TrigTools.atan2Turns(distY, distX) * 12f + 0.01f * c + 0.1f * shrunk;
                        int flip = -((int)theta & 1) | 1;
                        theta *= flip;
                        bright = basicPrepare(noise.getConfiguredNoise(TrigTools.cosTurns(theta) * shrunk, TrigTools.sinTurns(theta) * shrunk, len - c));
                        real[x][y] = bright;
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
        } else if(mode == 19) {
            for (int x = 0; x < width; x++) {
                float distX = x - (width>>>1);
                for (int y = 0; y < height; y++) {
                    float distY = y - (height>>>1);
                    float theta = TrigTools.atan2Turns(distY, distX) * (3+dim+dim);
                    float len = (float)Math.sqrt(distX * distX + distY * distY);
                    float shrunk = len / (3f+dim+dim);
                    bright = basicPrepare(noise.getConfiguredNoise(TrigTools.cosTurns(theta) * shrunk, TrigTools.sinTurns(theta) * shrunk, len - c));
                    real[x][y] = bright;
                    renderer.color(bright, bright, bright, 1f);
                    renderer.vertex(x, y, 0);
                }
            }
        } else if(mode == 20) {
            for (int x = 0; x < width; x++) {
                float distX = x - (width>>>1);
                for (int y = 0; y < height; y++) {
                    float distY = y - (height>>>1);
                    float theta = TrigTools.atan2Turns(distY, distX) * (3+dim+dim);
                    float len = (float)Math.sqrt(distX * distX + distY * distY);
                    float shrunk = len / (3f+dim+dim);
                    len = (len - c) * 0x1p-8f;
                    bright = basicPrepare(noise.getConfiguredNoise(TrigTools.cosTurns(theta) * shrunk,
                            TrigTools.sinTurns(theta) * shrunk, TrigTools.cosTurns(len) * 32f, TrigTools.sinTurns(len) * 32f));
                    real[x][y] = bright;
                    renderer.color(bright, bright, bright, 1f);
                    renderer.vertex(x, y, 0);
                }
            }
            if(Gdx.input.isKeyJustPressed(W)){
                for (int ct = 0; ct < 256; ct++) {
                    int w = 256, h = 256;
                    Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                    for (int x = 0; x < width; x++) {
                        float distX = x - (width>>>1);
                        for (int y = 0; y < height; y++) {
                            float distY = y - (height>>>1);
                            float theta = TrigTools.atan2Turns(distY, distX) * (3+dim+dim) + (ct * 0x3p-8f);
                            float len = (float)Math.sqrt(distX * distX + distY * distY);
                            float shrunk = len / (3f+dim+dim);
                            len = (len - ct) * 0x1p-8f;
                            float color = Interpolations.pow2In.apply(noise.getConfiguredNoise(TrigTools.cosTurns(theta) * shrunk,
                                    TrigTools.sinTurns(theta) * shrunk, TrigTools.cosTurns(len) * 32f, TrigTools.sinTurns(len) * 32f) * 0.5f + 0.5f);
                            p.setColor(color, color, color, 1f);
                            p.drawPixel(x, y);
                        }
                    }
                    frames.add(p);
                }
                Gdx.files.local("out/").mkdirs();
                String ser = noise.serializeToString() + "_" + dim + "_" + System.currentTimeMillis();
                System.out.println(ser);
//                        gif.write(Gdx.files.local("out/cube" + System.currentTimeMillis() + ".gif"), frames, 16);
                gif.write(Gdx.files.local("out/" + ser + ".gif"), frames, 16);
                for (int i = 0; i < frames.size; i++) {
                    frames.get(i).dispose();
                }
                frames.clear();

            }
        } else if(mode == 21) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = (c+x)*fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = (c+y)*fr;
                            bright = basicPrepare(val.getNoise(points[dim][0], points[dim][1]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(val.getNoise(points[dim][0], points[dim][1], points[dim][2]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(val.getNoise(points[dim][0], points[dim][1], points[dim][2], points[dim][3]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(val.getNoise(points[dim][0], points[dim][1], points[dim][2], points[dim][3], points[dim][4]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(val.getNoise(points[dim][0], points[dim][1], points[dim][2], points[dim][3], points[dim][4], points[dim][5]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 22) {
            float fr = noise.getFrequency();
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = (c+x)*fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = (c+y)*fr;
                            bright = basicPrepare(hdvs[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    points[dim][2] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[dim][0] = x * fr;
                        for (int y = 0; y < height; y++) {
                            points[dim][1] = y * fr;
                            bright = basicPrepare(hdvs[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[2][0] = TrigTools.cosTurns(x * iWidth) * 4 + cc;
                        points[2][1] = TrigTools.sinTurns(x * iWidth) * 4 + cc;
                        for (int y = 0; y < height; y++) {
                            points[2][2] = TrigTools.cosTurns(y * iHeight) * 4 + cc;
                            points[2][3] = TrigTools.sinTurns(y * iHeight) * 4 + cc;
                            bright = basicPrepare(hdvs[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    points[3][4] = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[3][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[3][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[3][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[3][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            bright = basicPrepare(hdvs[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    cc = c * fr;
                    for (int x = 0; x < width; x++) {
                        points[4][0] = TrigTools.cosTurns(x * iWidth) * 4;
                        points[4][1] = TrigTools.sinTurns(x * iWidth) * 4;
                        for (int y = 0; y < height; y++) {
                            points[4][2] = TrigTools.cosTurns(y * iHeight) * 4;
                            points[4][3] = TrigTools.sinTurns(y * iHeight) * 4;
                            points[4][4] = TrigTools.cosTurns(cc * -0x1.8p-6f) * 4;
                            points[4][5] = TrigTools.sinTurns(cc * -0x1.8p-6f) * 4;
                            bright = basicPrepare(hdvs[dim].getNoise(points[dim]));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 23) {
            switch (dim) {
                case 0:
                    for (int x = 0; x < width; x++) {
                        double ix = x * 200 * iWidth - 100;
                        for (int y = 0; y < height; y++) {
                            double iy = y * 200 * iHeight - 100;
                            bright = basicPrepare((int) Math.signum(Math.sin(1000.0 * (ix * ix + iy * iy))));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        double ix = x * 200 * iWidth - 100;
                        for (int y = 0; y < height; y++) {
                            double iy = y * 200 * iHeight - 100;
                            bright = basicPrepare((int) Math.signum(Math.sin((1000.0 + threshold * 0x1p-8) * (ix * ix + iy * iy))));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        double ix = x * 200 * iWidth - 100;
                        for (int y = 0; y < height; y++) {
                            double iy = y * 200 * iHeight - 100;
                            bright = basicPrepare((int) Math.signum(Math.sin(1000.0 * (ix * ix + iy * iy) + c * 0.25f)));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        double ix = x * 200 * iWidth - 100;
                        for (int y = 0; y < height; y++) {
                            double iy = y * 200 * iHeight - 100;
                            bright = basicPrepare((int) Math.signum(Math.sin((1000.0 + threshold * 0x1p-8) * (ix * ix + iy * iy) + c * 0.25f)));
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    // This points out some issues with https://blog.rahix.de/004-spaceships/ , in particular how it
                    // uses modulus on a 0-9 value to choose an option in a small range. The 'A' key to analyze is
                    // good to use here.
                    int iters = (int)(c) & 63;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            int n1 = x / 25, n2 = y / 25;
                            for (int i = 0; i <= iters; i++) {
                                int n = (n1 + n2) % 10;
                                n2 = n1;
                                n1 = n;
                            }
                            bright = basicPrepare((n1 % 4 + Hasher.randomize3Float(System.nanoTime() ^ x << 16 ^ y)) / 2f - 1f);
                            real[x][y] = bright;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 24) {
            int time = (int)(System.currentTimeMillis() >>> 10) & 15;
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, (int) noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, (int) noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, (int) noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, time >>> 2 & 1, (int) noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, time >>> 2 & 1, time >>> 3 & 1, (int) noise.getSeed()) & 255));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        } else if(mode == 25) {
            int time = (int)(System.currentTimeMillis() >>> 10) & 15;
            switch (dim){
                case 0:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, (int) noise.getSeed()) >>> 24));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 1:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, (int) noise.getSeed()) >>> 24));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 2:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, (int) noise.getSeed()) >>> 24));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 3:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, time >>> 2 & 1, (int) noise.getSeed()) >>> 24));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
                case 4:
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bright = (float) (db = (1f / 255f) * (pointHashes[hashIndex].hashWithState(x, y, time & 1, time >>> 1 & 1, time >>> 2 & 1, time >>> 3 & 1, (int) noise.getSeed()) >>> 24));
                            real[x][y] = db;
                            renderer.color(bright, bright, bright, 1f);
                            renderer.vertex(x, y, 0);
                        }
                    }
                    break;
            }
        }

        Fft.transform2D(real, imag);
        Fft.getColors(real, imag, colors);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                renderer.color(colors[x][y]);
                renderer.vertex(x + width, y, 0);
            }
        }
        renderer.end();
        if(Gdx.input.isKeyPressed(A)){ // Analysis
            renderer.begin(view.getCamera().combined, GL_LINES);
            for (int i = 0; i < 255; i++) {
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i, freq0[i] * 0x1p-3f, 0);
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+1, freq0[i+1] * 0x1p-3f, 0);
            }
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(255, 0 * 0x1p-3f, 0);
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(256, 0 * 0x1p-3f, 0);
            renderer.end();
        }

    }

    /**
     * For whatever reason, this makes output that looks like castles or carpets, kinda.
     * The explanation seems to be that each input is XORed with a large constant plus itself.
     * The constant does not seem to matter; here we use the golden ratio times 2 to the 64,
     * but earlier versions used 3 different numbers. Without the xor-by-constant-plus-itself,
     * this is an ordinary point hash, though not the highest quality.
     * @param x x position, as any long
     * @param y y position, as any long
     * @param s seed, as any long
     * @return a highly artifact-laden hash from 0 to 255, both inclusive
     */
    public static long castle256(long x, long y, long s) {
        x = (x + 0x9E3779B97F4A7C15L ^ x) * (s + y);
        y = (y + 0x9E3779B97F4A7C15L ^ y) * (x + s);
        s = (s + 0x9E3779B97F4A7C15L ^ s) * (y + x);
        return s >>> 56;
    }

    public static int odd16(int x, int y) {
        return ((x * 11 >>> 2 ^ y * 7 >>> 2 ^ (y - x) * 5) & 15);
    }
    public static int odd256(int x, int y, int s) {
        s <<= 2;
        return (((x+s ^ x >>> 1) + (y+s ^ y >>> 1)) * 0xDE4D >>> 4 & 15) * 17;
//        return (((x+s) * 151 >>> 1 ^ (s-y) * 107 >>> 2 ^ (y - x ^ s) * 83 ^ s * 3 ^ x * 5 ^ y * 7) & 255);
//         (((x + s) * 11 >>> 2 ^ (y - s) * 7 >>> 2 ^ (y - x + s) * 5) & 255) ^
//                (((x - s) * 5 >>> 1 ^ (s - y) * 11 >>> 1 ^ (y + x - s) * 7) & 255);
    }

    public static int fancy256(int x, int y, int s) {
//        x = Math.abs(x);
//        y = Math.abs(y);
        s ^= (x >> 6) * 0xD1B5;
        s ^= (y >> 6) * 0xABC9;
        x *= x;
        y *= y;
        x = x >>> 1 & 63;
        y = y >>> 1 & 63;
        int a, b;
        if(x > y){
            a = x;
            b = y;
        }
        else {
            a = y;
            b = x;
        }
        a = (a + 0x9E3779B9 ^ a) * (s ^ b);
        b = (b + 0x9E3779B9 ^ b) * (a ^ s);
        s = (s + 0x9E3779B9 ^ s) * (b ^ a);
        return s >>> 24;
    }
//        x *= 0xD1B54A32D192ED03L;
//        y *= 0xABC98388FB8FAC03L;
//        s *= 0x8CB92BA72F3D8DD7L;
//        return (s ^ s >>> 25) & 0xFF;

    public static byte getBlue(int x, int y, int s){
        final int m = Integer.bitCount(TILE_NOISE[(x + 23 >>> 6) + (y + 41 >>> 6) + (s >>> 6) & 63][(x + 23 << 6 & 0xFC0) | (y + 41 & 0x3F)] + 128) 
                * Integer.bitCount(TILE_NOISE[(y + 17 >>> 6) - (x + 47 >>> 7) + (s >>> 12) & 63][(y + 17 << 6 & 0xFC0) | (x + 47 & 0x3F)] + 128)
                * Integer.bitCount(TILE_NOISE[(y + 33 >>> 7) + (x - 31 >>> 6) + (s >>> 18) & 63][(y + 33 << 6 & 0xFC0) | (x - 31 & 0x3F)] + 128)
                >>> 1;
        final int n = Integer.bitCount(TILE_NOISE[(x + 53 >>> 6) - (y + 11 >>> 6) + (s >>> 9) & 63][(x + 53 << 6 & 0xFC0) | (y + 11 & 0x3F)] + 128)
                * Integer.bitCount(TILE_NOISE[(y - 27 >>> 6) + (x - 37 >>> 7) + (s >>> 15) & 63][(y - 27 << 6 & 0xFC0) | (x - 37 & 0x3F)] + 128)
                * Integer.bitCount(TILE_NOISE[-(x + 35 >>> 6) - (y - 29 >>> 7) + (s >>> 21) & 63][(x + 35 << 6 & 0xFC0) | (y - 29 & 0x3F)] + 128)
                >>> 1;
        return (byte) (TILE_NOISE[s & 63][(y + (m >>> 7) - (n >>> 7) << 6 & 0xFC0) | (x + (n >>> 7) - (m >>> 7) & 0x3F)] ^ (m ^ n));
    }

    public static byte getChosen(int x, int y, int seed){
        return getChosen(x, y, seed, TILE_NOISE);
    }
    public static byte getChosen(int x, int y, int seed, final byte[][] noises){
        seed ^= (x >>> 6) * 0x1827F5 ^ (y >>> 6) * 0x123C21;
        // hash for a 64x64 tile on the "normal grid"
        int h = (seed = (seed ^ (seed << 19 | seed >>> 13) ^ (seed << 5 | seed >>> 27) ^ 0xD1B54A35) * 0x125493) ^ seed >>> 11;
        // choose from 64 noise tiles in TILE_NOISE and get the exact pixel for our x,y in its 64x64 area
        final int xc = noises[h & 0x3F][(y << 6 & 0xFC0) | (x & 0x3F)];
        // likely to be a different noise tile, and the x,y position is transposed
        final int yc = noises[h >>> 6 & 0x3F][(x << 6 & 0xFC0) | (y & 0x3F)];
        // altered x/y; here we choose a start position for the "offset grid" based on the previously sampled noise
        final int ax = ((xc) * (xc+1) << 6 < ((x & 0x3F) - 32) * ((x & 0x3F) - 31)) ? x - 32 : x + 32;
        final int ay = ((yc) * (yc+1) << 6 < ((y & 0x3F) - 32) * ((y & 0x3F) - 31)) ? y - 32 : y + 32;
        // get a tile based on the "offset grid" position we chose and the hash for the normal grid, then a pixel
        // this transposes x and y again, it seems to help with the particular blue noise textures we have
        h ^= (ax >>> 6) * 0x1827F5 ^ (ay >>> 6) * 0x123C21;
        return noises[(h ^ (h << 19 | h >>> 13) ^ (h << 5 | h >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26][(x << 6 & 0xFC0) | (y & 0x3F)];
    }

    public static byte getWobbled(int x, int y) {
//        return (byte) (15 - (x & 15) | (y & 15) << 4);
        final int h = Integer.reverse(posToHilbertNoLUT(x, y));
//        final int h = Integer.reverse(CoordPacker.mortonEncode(x, y));
        return (byte) ((h >>> 24 ^ h >>> 23 ^ h >>> 22 ^ y ^ x)); // ^ CoordPacker.mortonEncode(511 - (x & 511), (y & 511) << 9)
    }
    private static int posToHilbertNoLUT(int x, int y )
    {
        x &= 511;
        y &= 511;
        int hilbert = 0, remap = 0xb4, mcode, hcode;

        mcode = ( ( x >>> 8 ) & 1 ) | ( ( ( y >>> ( 8 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( (hilbert << 2) + hcode );

        mcode = ( ( x >>> 7 ) & 1 ) | ( ( ( y >>> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( (hilbert << 2) + hcode );

        mcode = ( ( x >>> 6 ) & 1 ) | ( ( ( y >>> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 5 ) & 1 ) | ( ( ( y >>> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 4 ) & 1 ) | ( ( ( y >>> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 3 ) & 1 ) | ( ( ( y >>> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 2 ) & 1 ) | ( ( ( y >>> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >>> 1 ) & 1 ) | ( ( ( y >>> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >>> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >>> ( mcode << 1 ) ) & 3 );

        hilbert = ( ( hilbert << 2 ) + hcode );

        return hilbert;
    }

    public static byte getSeededOmniTiling(final int x, final int y, final int seed) {
        final int a = x >>> 6;
        final int b = y >>> 6;
        return TILE_NOISE[seed + b + ((a + b) * (a + b + 1) >> 1) & 63][(y << 6 & 0xFC0) | (x & 0x3F)];
    }


    @Override
    public void render() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS on mode " + mode + ", dim " + dim);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
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
        config.setTitle("SquidSquad Test: FFT Visualization");
        config.setWindowedMode(width << 1, height);
        config.useVsync(false);
        config.setForegroundFPS(120);
        config.setResizable(false);
        new Lwjgl3Application(new FFTVisualizer(), config);
    }
}
