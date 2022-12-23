package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.function.FloatToFloatFunction;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class NoiseComparison extends ApplicationAdapter {

    private Noise noise = new Noise(1, 0.0625f, Noise.CUBIC_FRACTAL, 1);
    private Noise bare = new Noise(1, 1, Noise.CUBIC_FRACTAL, 1);
    private final float[] args = {
            1f,   // 0, spline shape
            0f,   // 1, spline turning
//            2f,   // 2, maelstrom exponent
//            4f/3f,// 3, maelstrom mul
//            1.25f,// 4, maelstrom sub
    };
    private FloatToFloatFunction fff = (f) -> INoise.noiseSpline(f, args[0], args[1]);
    private NoiseAdjustment adj = new NoiseAdjustment(bare, fff);
    private NoiseWrapper wrap = new NoiseWrapper(adj, 1, 0.0625f, Noise.FBM, 1);
    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 2;
    private float freq = 1f;
    private ImmediateModeRenderer20 renderer;
    
    private LongPointHash ph = new LongPointHash();
    private IntPointHash iph = new IntPointHash();
    private FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 16);
    private FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 32);
    private FlawedPointHash.SquishedCubeHash squish = new FlawedPointHash.SquishedCubeHash(1, 16);
    private FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private FlawedPointHash.LowLeaningHash low = new FlawedPointHash.LowLeaningHash(123);
    private IPointHash[] pointHashes = new IPointHash[] {ph, iph, fnv, rug, quilt, cube, squish, low};
    private int hashIndex = 6;

    private static final int width = 256, height = 256;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height << 1, false, true, 0);
        view = new ScreenViewport();

        bare.setFractalOctaves(1);
        noise.setFractalOctaves(octaves);
        wrap.setFractalOctaves(octaves);

        noise.setPointHash(pointHashes[hashIndex]);
        bare.setPointHash(pointHashes[hashIndex]);

        noise.setFractalType(Noise.DOMAIN_WARP);
        wrap.setFractalType(Noise.DOMAIN_WARP);

        noise.setInterpolation(Noise.QUINTIC);
        bare.setInterpolation(Noise.QUINTIC);

        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case P: //pause
                        keepGoing = !keepGoing;
                    case C:
                        ctr++;
                        break;
                    case E: //earlier seed
                        noise.setSeed(noise.getSeed() - 1);
                        bare.setSeed(noise.getSeed());
                        break;
                    case S: //seed
                        noise.setSeed(noise.getSeed() + 1);
                        bare.setSeed(noise.getSeed());
                        break;
                    case SLASH:
                        noise.setSeed((int) Hasher.randomize3(noise.getSeed()));
                        bare.setSeed(noise.getSeed());
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noise.setNoiseType((noise.getNoiseType() + (UIUtils.shift() ? 17 : 1)) % 18);
                        bare.setNoiseType(noise.getNoiseType());
                        break;
                    case M:
                    case MINUS:
                        noise.setNoiseType((noise.getNoiseType() + 17) % 18);
                        bare.setNoiseType(noise.getNoiseType());
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        wrap.setFrequency(noise.getFrequency());
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        wrap.setFractalType(noise.getFractalType());
                        break;
                    case G: // GLITCH!
                        noise.setPointHash(pointHashes[hashIndex = (hashIndex + (UIUtils.shift() ? pointHashes.length - 1 : 1)) % pointHashes.length]);
                        bare.setPointHash(noise.getPointHash());
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        wrap.setFractalOctaves(noise.getFractalOctaves());
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        wrap.setFractalOctaves(noise.getFractalOctaves());
                        break;
                    case COMMA: // sharpness
                        noise.setSharpness((float)Math.pow(TrigTools.sinDeg((System.currentTimeMillis() & 0xFFFF) * 0x1p-4f) + 1.5f, 3f));
                        bare.setSharpness(noise.getSharpness());
                        break;
                    case NUM_0:
                    case NUMPAD_0:
                        args[0] = Math.max(args[0] + (UIUtils.shift() ? 0.01f : -0.01f), 0.001f);
                        break;
                    case NUM_1:
                    case NUMPAD_1:
                        args[1] = Math.min(Math.max(args[1] + (UIUtils.shift() ? 0.01f : -0.01f), -1f), 1f);
                        break;
//                    case NUM_2:
//                    case NUMPAD_2:
//                    {
//                        args[2] = Math.max(args[2] + (UIUtils.shift() ? 0.01f : -0.01f), 0.001f);
//                        float lo = 1f / args[2];
//                        float halfDiff = 0.5f * (args[2] - lo);
//                        args[4] = halfDiff - args[2];
//                        args[3] = 1f / halfDiff;
//                    }
//                        break;
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
        float bright, c = ctr * 0.5f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x + ctr, y + ctr));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap.getNoiseWithSeed(x + ctr, y + ctr, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap.getNoiseWithSeed(x, y, c, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, ctr, 0x1p-4f * (x + y - ctr)));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap.getNoiseWithSeed(x, y, ctr, 0x1p-4f * (x + y - ctr), bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(
                                (y * 0.6f + x * 0.4f) + ctr * 1.4f,
                                (x * 0.6f - y * 0.4f) + ctr * 1.4f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.4f,
                                (y * 0.7f - x * 0.3f) - ctr * 1.4f,
                                (x * 0.35f - y * 0.25f) * 0.6f - (x * 0.25f - y * 0.35f) * 1.2f));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.4f,
                                (x * 0.6f - y * 0.4f) + ctr * 1.4f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.4f,
                                (y * 0.7f - x * 0.3f) - ctr * 1.4f,
                                (x * 0.35f - y * 0.25f) * 0.6f - (x * 0.25f - y * 0.35f) * 1.2f, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) { 
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(
                                (y * 0.6f + x * 0.4f) + ctr * 1.2f, (x * 0.6f - y * 0.4f) + ctr * 1.2f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.2f, (y * 0.7f - x * 0.3f) - ctr * 1.2f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f,
                                (y * 0.45f - x * 0.15f) * 0.75f - (y * 0.15f + x * 0.45f) * 1.25f));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.2f, (x * 0.6f - y * 0.4f) + ctr * 1.2f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.2f, (y * 0.7f - x * 0.3f) - ctr * 1.2f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f,
                                (y * 0.45f - x * 0.15f) * 0.75f - (y * 0.15f + x * 0.45f) * 1.25f, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
            }
                break;
        }
        renderer.end();

    }

    @Override
    public void render() {
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
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
        config.setTitle("SquidSquad Test: Noise/NoiseWrapper Comparison");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width << 1, height);
        new Lwjgl3Application(new NoiseComparison(), config);
    }
}
