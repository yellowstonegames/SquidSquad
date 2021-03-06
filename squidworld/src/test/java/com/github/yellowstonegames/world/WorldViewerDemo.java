package com.github.yellowstonegames.world;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.tommyettinger.ds.support.LaserRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DigitTools;
import com.github.yellowstonegames.grid.LongPointHash;
import com.github.yellowstonegames.grid.Noise;

/**
 * Port of Zachary Carter's world generation technique, https://github.com/zacharycarter/mapgen
 * Biome, moisture, heat, and height maps can all be requested from the generated world.
 * You can press 's' or 'p' to play a spinning animation of the world turning.
 * <a href="https://i.imgur.com/mQFk2Fm.gif">Preview GIF of a spinning planet.</a>
 */
public class WorldViewerDemo extends ApplicationAdapter {

    //private static final int width = 314 * 3, height = 300;
//    private static final int width = 1024, height = 512;
//    private static final int width = 512, height = 256; // mimic
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 400, height = 400; // fast rotations
    private static final int width = 300, height = 300;
//    private static final int width = 1600, height = 800;
//    private static final int width = 900, height = 900;
//    private static final int width = 700, height = 700;
//    private static final int width = 512, height = 512;
//    private static final int width = 128, height = 128;
    
    private ImmediateModeRenderer20 batch;
    private InputProcessor input;
    private Viewport view;
    private DistinctRandom rng;
    private long seed;
    private WorldMapGenerator world, inner;
    private WorldMapView wmv;
    
    private boolean spinning;

    private long ttg; // time to generate
    
//    public int noiseCalls = 0, pixels = 0;  // debug
    private final Color INK = new Color(DescriptiveColor.describe("darkmost dullest mauve"));
    @Override
    public void create() {
        // matching the new ocean colors
//        System.out.println(DescriptiveColor.bestMatch(DescriptiveColor.fromRGBA8888(0x08404CFF), 1));
//        System.out.println(DescriptiveColor.bestMatch(DescriptiveColor.fromRGBA8888(0x187880FF), 1));

        //// you will probably want to change batch to use whatever rendering system is appropriate
        //// for your game; here it always renders pixels
        batch = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new StretchViewport(width, height);
        seed = 42;
        rng = new DistinctRandom(seed);
        //// NOTE: this FastNoise has a different frequency (1f) than the default (1/32f), and that
        //// makes a huge difference on world map quality. It also uses extra octaves.
        //WorldMapGenerator.DEFAULT_NOISE.setFractalOctaves(3);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalLacunarity(0.625f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalGain(1.6f);
        
//        {
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z, float w) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z, w);
//            }
//
//            @Override
//            public float singleSimplex(int seed, float x, float y, float z, float w, float u, float v) {
//                noiseCalls++;
//                super.setSeed(seed);
//                return super.getSimplexFractal(x, y, z, w, u, v);
//            }
//        };
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.25);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.6);
        //world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.75);
//        world = new WorldMapGenerator.MimicMap(seed, new FastNoise(rng.nextInt(), 1f, FastNoise.SIMPLEX_FRACTAL, 2), 0.7);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.7);
//        world = new RotatingGlobeMap(seed, width, height, new Noise(rng.nextInt(), 2f, Noise.FOAM_FRACTAL, 2), 0.6f);
        world = new RotatingGlobeMap(seed, width, height, new Noise(rng.nextInt(), 2f, Noise.FOAM_FRACTAL, 2), 0.8f);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.2, 0.0625, 2.5);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.2, 0.0, 2.0);
//        world = new WorldMapGenerator.SphereMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.6);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 0.65);
//        world = new WorldMapGenerator.LocalMimicMap(seed, ((WorldMapGenerator.LocalMimicMap) world).earth.not(), WorldMapGenerator.DEFAULT_NOISE, 0.9);
        inner = new LocalMap(seed, width, height, new Noise(rng.nextInt(), 1f, Noise.FOAM_FRACTAL, 2), 0.8f);
        wmv = new WorldMapView(world);
//        wmv.initialize(SColor.CW_FADED_RED, SColor.AURORA_BRICK, SColor.DEEP_SCARLET, SColor.DARK_CORAL,
//                SColor.LONG_SPRING, SColor.WATER_PERSIMMON, SColor.AURORA_HOT_SAUCE, SColor.PALE_CARMINE,
//                SColor.AURORA_LIGHT_SKIN_3, SColor.AURORA_PINK_SKIN_2,
//                SColor.AURORA_PUTTY, SColor.AURORA_PUTTY, SColor.ORANGUTAN, SColor.SILVERED_RED, null);
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode){
                    case Input.Keys.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setSelectedState(0, seed);
                        break;
                    case Input.Keys.EQUALS:
                    case Input.Keys.NUMPAD_EQUALS:
                    case Input.Keys.PLUS:
                    case Input.Keys.NUMPAD_ADD:
                        zoomIn();
                        break;
                    case Input.Keys.MINUS:
                    case Input.Keys.NUMPAD_SUBTRACT:
                        zoomOut();
                        break;
                    case Input.Keys.P:
                    case Input.Keys.S:
                        spinning = !spinning;
                        break;
                    case Input.Keys.Q:
                    case Input.Keys.ESCAPE:
                        Gdx.app.exit();
                        break;
                    default:
                        return false;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if(button == Input.Buttons.RIGHT)
                {
                    zoomOut(screenX, height - 1 - screenY);
                }
                else
                {
                    zoomIn(screenX, height - 1 - screenY);
                }
                return true;
            }
        };

        generate(seed);
        rng.setSelectedState(0, seed);
        Gdx.input.setInputProcessor(input);
    }

    public void zoomIn() {
        long startTime = System.currentTimeMillis();
//        noiseCalls = 0;
        world.zoomIn();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        if(world.heightCodeData[zoomX][zoomY] >= 100)
            return;
        long startTime = System.currentTimeMillis();
//        noiseCalls = 0;
//        world.zoomIn(1, zoomX, zoomY);
        inner.rng.setState(LongPointHash.hashAll(zoomX, zoomY, world.rng.getStateA()), LongPointHash.hashAll(zoomX, zoomY, world.rng.getStateB()));
        int[][] colors = wmv.getColorMapOklab();
        wmv.match(colors[zoomX][zoomY],
                colors[(zoomX + 2) % colors.length][zoomY],
                colors[zoomX][(zoomY + 2) % colors[0].length],
                colors[(zoomX + colors.length - 2) % colors.length][zoomY],
                colors[zoomX][(zoomY + colors[0].length - 2) % colors[0].length]
        );
        wmv.setWorld(inner);
        wmv.generate(inner.rng.getStateA(), inner.rng.getStateB(), world.heightCodeData[zoomX][zoomY] < 4 ? 0.0f : 4.0f +
                        world.heightData[zoomX][zoomY],
                world.heatData[zoomX][zoomY] + 0.5f);
        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomOut()
    {
        long startTime = System.currentTimeMillis();
//        noiseCalls = 0;
        world.zoomOut();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.currentTimeMillis();
//        noiseCalls = 0;
//        world.zoomOut(1, zoomX, zoomY);
        wmv.initialize();
        wmv.setWorld(world);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        System.out.println("Seed used: 0x" + DigitTools.hex(seed) + "L");
//        noiseCalls = 0; // debug
        //// parameters to generate() are seedA, seedB, landModifier, heatModifier.
        //// seeds can be anything (if both 0, they'll be changed so seedA is 1, otherwise used as-is).
        //// higher landModifier means more land, lower means more water; the middle is 1.0.
        //// higher heatModifier means hotter average temperature, lower means colder; the middle is 1.0.
        //// heatModifier defaults to being higher than 1.0 on average here so polar ice caps are smaller.
        world.seedA = (int)(seed & 0xFFFFFFFFL);
        world.seedB = (int) (seed >>> 32);
        wmv.generate();
        // earlier settings
//        wmv.generate((int)(seed & 0xFFFFFFFFL), (int) (seed >>> 32),
//                0.9 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.55 + 0.9);
        // implementation used in WorldMapView.generate() with no args
//        wmv.generate(world.seedA, world.seedB,
//                1.0 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.25,
//                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.25 + 1.0);

        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }
    public void rotate()
    {
        long startTime = System.currentTimeMillis();
        world.setCenterLongitude((startTime & 0x7FFFFFL) * 0x1p-12f);
        //// maybe comment in next line if using something other than RotatingSpaceView
//        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        //// comment out next line if using something other than RotatingSpaceView
        wmv.getBiomeMapper().makeBiomes(world);
        wmv.show();
        ttg = System.currentTimeMillis() - startTime;
    }


    public void putMap() { 
        int[][] cm = wmv.getColorMap();
        //// everything after this part of putMap() should be customized to your rendering setup
        batch.begin(view.getCamera().combined, GL20.GL_POINTS);
//        pixels = 0;                              // for debugging how many pixels are drawn
        int c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = cm[x][y];
//                if(c != WorldMapView.emptyColor) // more debug
//                    pixels++;                    // more debug
//                if(c != WorldMapView.emptyColor) {
                    batch.color(DescriptiveColor.rgbaIntToFloat(c));
                    batch.vertex(x, y, 0f);
//                }
            }
        }
        batch.end();
//        if(Gdx.input.isKeyJustPressed(Input.Keys.D)) // debug
//            System.out.println((float) (noiseCalls) / pixels);
    }
    
    @Override
    public void render() {
        // standard clear the background routine for libGDX

        Gdx.gl.glClearColor(INK.r, INK.g, INK.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        if(spinning) 
            rotate();
        // need to display the map every frame, since we clear the screen to avoid artifacts.
        putMap();
        Gdx.graphics.setTitle("Took " + ttg + " ms to generate");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Detailed World Map");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        new Lwjgl3Application(new WorldViewerDemo(), config);
    }
}
