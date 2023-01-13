package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
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

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class SimplexComparison extends ApplicationAdapter {

    private INoise noise = new SimplexNoise(1L);
    private INoise scaled = new SimplexNoiseScaled(1L);//new Noise(1, 1, Noise.SIMPLEX_FRACTAL, 1);//
    private NoiseWrapper wrap0 = new NoiseWrapper(noise, 1, 0.0625f, Noise.FBM, 1);
    private NoiseWrapper wrap1 = new NoiseWrapper(scaled, 1, 0.0625f, Noise.FBM, 1);
    private int dim = 0; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 1;
    private float freq = 1f/32f;
    private ImmediateModeRenderer20 renderer;

    private static final int width = 256, height = 256;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    public static float basicPrepare(float n)
    {
        if(n < -1f || n > 1f) {
            System.out.println(n);
            return Float.MAX_VALUE;
        }
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
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 0.5f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(wrap0.getNoiseWithSeed(x + ctr, y + ctr, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap1.getNoiseWithSeed(x + ctr, y + ctr, scaled.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(wrap0.getNoiseWithSeed(x, y, c, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap1.getNoiseWithSeed(x, y, c, scaled.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(wrap0.getNoiseWithSeed(x, y, ctr, 0x1p-4f * (x + y - ctr), noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap1.getNoiseWithSeed(x, y, ctr, 0x1p-4f * (x + y - ctr), scaled.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(wrap0.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.4f,
                                (x * 0.6f - y * 0.4f) + ctr * 1.4f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.4f,
                                (y * 0.7f - x * 0.3f) - ctr * 1.4f,
                                (x * 0.35f - y * 0.25f) * 0.6f - (x * 0.25f - y * 0.35f) * 1.2f, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap1.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.4f,
                                (x * 0.6f - y * 0.4f) + ctr * 1.4f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.4f,
                                (y * 0.7f - x * 0.3f) - ctr * 1.4f,
                                (x * 0.35f - y * 0.25f) * 0.6f - (x * 0.25f - y * 0.35f) * 1.2f, scaled.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x + width, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) { 
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(wrap0.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.2f, (x * 0.6f - y * 0.4f) + ctr * 1.2f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.2f, (y * 0.7f - x * 0.3f) - ctr * 1.2f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f,
                                (y * 0.45f - x * 0.15f) * 0.75f - (y * 0.15f + x * 0.45f) * 1.25f, noise.getSeed()));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                        bright = basicPrepare(wrap1.getNoiseWithSeed(
                                (y * 0.6f + x * 0.4f) + ctr * 1.2f, (x * 0.6f - y * 0.4f) + ctr * 1.2f,
                                (x * 0.7f + y * 0.3f) - ctr * 1.2f, (y * 0.7f - x * 0.3f) - ctr * 1.2f,
                                (x * 0.35f - y * 0.25f) * 0.65f - (x * 0.25f + y * 0.35f) * 1.35f,
                                (y * 0.45f - x * 0.15f) * 0.75f - (y * 0.15f + x * 0.45f) * 1.25f, scaled.getSeed()));
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
