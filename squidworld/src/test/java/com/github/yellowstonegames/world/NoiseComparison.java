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
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.function.FloatToFloatFunction;
import com.github.yellowstonegames.grid.*;

import java.util.Arrays;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_LINES;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class NoiseComparison extends ApplicationAdapter {

    private Noise noise = new Noise(1, 0.0625f, Noise.SIMPLEX_FRACTAL, 1);
    private Noise bare = new Noise(1, 1, Noise.SIMPLEX_FRACTAL, 1);
    private final float[] args = {
            1f,   // 0, spline shape
            0f,   // 1, spline turning
            2f,   // 2, maelstrom exponent
            4f/3f,// 3, maelstrom mul
            5f/3f,// 4, maelstrom sub
    };
//    private FloatToFloatFunction fff = (f) -> INoise.noiseSpline(f, args[0], args[1]);
//    private FloatToFloatFunction fff = (f) -> (TrigTools.cos(f * 4f * TrigTools.PI2) > 0.9f) ? f < 0.1f ? 1f : 0.5f : -0.2f;
    private FloatToFloatFunction[] adjustments =
        {
                (f) -> f,
                (f) -> INoise.noiseSpline(f, args[0], args[1]),
                (f) -> MathTools.clamp((TrigTools.cos(f * 20) - 0.7f) * 4f, -0.6f, 1f - Math.abs(f)),
                (f) -> (float) Math.pow(args[2], f) * args[3] - args[4],
                (f) -> INoise.noiseSpline((float) Math.pow(args[2], f) * args[3] - args[4], args[0], args[1]),
        };
    private int currentAdjustment = 0;

//    private FloatToFloatFunction fff = (f) -> {
//        if(f > 0.7f && f < 0.8f) return TrigTools.cos((f - 0.75f) * 9f * TrigTools.PI2);
//        if(f > 0.4f && f < 0.5f) return TrigTools.cos((f - 0.45f) * 9f * TrigTools.PI2);
//        if(f > 0.1f && f < 0.2f) return TrigTools.cos((f - 0.15f) * 9f * TrigTools.PI2);
//        if(f > -.2f && f < -.1f) return TrigTools.cos((f + 0.15f) * 9f * TrigTools.PI2);
//        if(f > -.5f && f < -.4f) return TrigTools.cos((f + 0.45f) * 9f * TrigTools.PI2);
//        if(f > -.8f && f < -.7f) return TrigTools.cos((f + 0.75f) * 9f * TrigTools.PI2);
//        return -1f;
//    };
    private NoiseAdjustment adj = new NoiseAdjustment(bare, adjustments[currentAdjustment]);
    private NoiseWrapper wrap = new NoiseWrapper(adj, 1, 0.0625f, Noise.FBM, 1);
    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 1f/32f;
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
    private static final float iWidth = 1f/width, iHeight = 1f/height;
    private static final float LIGHT_YELLOW = Color.toFloatBits(1f, 1f, 0.4f, 1f);

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
                    case SPACE:
                        keepGoing = !keepGoing;
                    case C:
                        if(UIUtils.shift())ctr--;
                        else ctr++;
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
                    case J: // adjustment
                        adj.setAdjustment(adjustments[
                                currentAdjustment = (currentAdjustment + (UIUtils.shift() ? adjustments.length - 1 : 1)) % adjustments.length]);
                        System.out.println(currentAdjustment);
                        break;
                    case COMMA: // sharpness
                        noise.setSharpness((float)Math.pow(TrigTools.sinDeg((System.currentTimeMillis() & 0xFFFF) * 0x1p-4f) + 1.5f, 3f));
                        bare.setSharpness(noise.getSharpness());
                        break;
                    case NUM_0:
                    case NUMPAD_0:
                        args[0] = Math.max(args[0] + (UIUtils.shift() ? 0.05f : -0.05f), 0.001f);
                        break;
                    case NUM_1:
                    case NUMPAD_1:
                        args[1] = Math.min(Math.max(args[1] + (UIUtils.shift() ? 0.05f : -0.05f), -1f), 1f);
                        break;
                    case NUM_2:
                    case NUMPAD_2:
                    {
                        args[2] = Math.max(args[2] + (UIUtils.shift() ? 0.05f : -0.05f), 0.001f);
                        float lo = 1f / args[2];
                        float halfDiff = 0.5f * (args[2] - lo);
                        args[3] = 1f / halfDiff;
                        args[4] = args[2] * args[3] - 1f;
                    }
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
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 0.5f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare0(noise.getConfiguredNoise(x + ctr, y + ctr));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap.getNoiseWithSeed(x + ctr, y + ctr, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare0(noise.getConfiguredNoise(x, y, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap.getNoiseWithSeed(x, y, c, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 32 + c, xs = TrigTools.sinTurns(x * iWidth) * 32 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 32 + c, ys = TrigTools.sinTurns(y * iHeight) * 32 + c;
                        bright = prepare0(noise.getConfiguredNoise(xc, yc, xs, ys));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap.getNoiseWithSeed(xc, yc, xs, ys, bare.getSeed()));
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
                        bright = prepare0(noise.getConfiguredNoise(xc, yc, xs, ys, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap.getNoiseWithSeed(xc, yc, xs, ys, c, bare.getSeed()));
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
                        bright = prepare0(noise.getConfiguredNoise(xc, yc, zc, xs, ys, zs));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap.getNoiseWithSeed(xc, yc, zc, xs, ys, zs, bare.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
            }
                break;
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
            for (int i = 0; i < 255; i++) {
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+width, freq1[i] * 0x1p-3f, 0);
                renderer.color(LIGHT_YELLOW);
                renderer.vertex(i+1+width, freq1[i+1] * 0x1p-3f, 0);
            }
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(255+width, 0 * 0x1p-3f, 0);
            renderer.color(LIGHT_YELLOW);
            renderer.vertex(256+width, 0 * 0x1p-3f, 0);
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
        new Lwjgl3Application(new NoiseComparison(), config);
    }
}
