package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import static com.badlogic.gdx.graphics.GL20.*;

/**
 */
public class SimplexComparison extends ApplicationAdapter {

    private INoise noise = new SimplexNoise(1L);
    private INoise scaled = new SimplexNoiseScaled(1L);//new Noise(1, 1, Noise.SIMPLEX_FRACTAL, 1);//
    private NoiseWrapper wrap0 = new NoiseWrapper(noise, 1, 0.0625f, Noise.FBM, 1);
    private NoiseWrapper wrap1 = new NoiseWrapper(scaled, 1, 0.0625f, Noise.FBM, 1);
    private int dim = 3; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 1f/32f;
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;
    private static final float LIGHT_YELLOW = Color.toFloatBits(1f, 1f, 0.4f, 1f);

    private InputAdapter input;
    
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

    public float basicPrepare(float n)
    {
//        if(n < -1f || n > 1f) {
//            System.out.println(n);
//            return Float.MAX_VALUE;
//        }
        return n * 0.5f + 0.5f;
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height << 1, false, true, 0);
        view = new ScreenViewport();

        wrap0.setFractalOctaves(octaves);
        wrap1.setFractalOctaves(octaves);

        wrap0.setFractalType(Noise.FBM);
        wrap1.setFractalType(Noise.FBM);

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
                        scaled.setSeed(noise.getSeed());
                        break;
                    case S: //seed
                        noise.setSeed(noise.getSeed() + 1);
                        scaled.setSeed(noise.getSeed());
                        break;
                    case SLASH:
                        noise.setSeed((int) Hasher.randomize3(noise.getSeed()));
                        scaled.setSeed(noise.getSeed());
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
                        freq *= (UIUtils.shift() ? 1.25f : 0.8f);
                        wrap0.setFrequency(freq);
                        wrap1.setFrequency(freq);
                        break;
                    case R: // fRactal type
                        wrap0.setFractalType((wrap0.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
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
        float bright, c = ctr * 0.25f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare0(wrap0.getNoiseWithSeed(x + c, y + c, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap1.getNoiseWithSeed(x + c, y + c, scaled.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = prepare0(wrap0.getNoiseWithSeed(x, y, c, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap1.getNoiseWithSeed(x, y, c, scaled.getSeed()));
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
                        bright = prepare0(wrap0.getNoiseWithSeed(xc, yc, xs, ys, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap1.getNoiseWithSeed(xc, yc, xs, ys, scaled.getSeed()));
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
                        bright = prepare0(wrap0.getNoiseWithSeed(xc, yc, xs, ys, c, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap1.getNoiseWithSeed(xc, yc, xs, ys, c, scaled.getSeed()));
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
                                xc, yc, zc, xs, ys, zs, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = prepare1(wrap1.getNoiseWithSeed(
                                xc, yc, zc, xs, ys, zs, scaled.getSeed()));
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
        new Lwjgl3Application(new SimplexComparison(), config);
    }
}
