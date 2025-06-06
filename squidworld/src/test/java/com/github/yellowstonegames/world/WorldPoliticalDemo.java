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

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.*;
import com.github.tommyettinger.digital.MathTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.grid.QuasiRandomTools;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.place.DungeonTools;

/**
 * Port of <a href="https://github.com/zacharycarter/mapgen">Zachary Carter's world generation technique</a>, with
 * substantial changes.
 * Biome, moisture, heat, and height maps can all be requested from the generated world.
 * You can press 's' or 'p' to play a spinning animation of the world turning.
 * <a href="https://i.imgur.com/z3rlN53.gifv">Preview GIF of a spinning planet.</a>
 */
public class WorldPoliticalDemo extends ApplicationAdapter {

    //private static final int width = 314 * 3, height = 300;
    private static final int width = 1024, height = 512;
//    private static final int width = 512, height = 256; // mimic
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 400, height = 400; // fast rotations
//    private static final int width = 300, height = 300;
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
    private WorldMapGenerator world;
    private WorldMapGenerator inner;
    private WorldMapView wmv;
    private PoliticalMapper pm;
    private char[][] political;
    private boolean spinning;
    private float nation = 0f;
    private long ttg; // time to generate
    
//    public int noiseCalls = 0, pixels = 0;  // debug

    private final int[] NATION_COLORS = new int[49];
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));


    @Override
    public void create() {
        for (int i = 0; i < 49; i++) {
//            NATION_COLORS[i] = DescriptiveColor.COLORS_BY_HUE.get((i % 49) + 1);
            NATION_COLORS[i] = FullPalette.VARIED_PALETTE[i];
//            NATION_COLORS[i] = DescriptiveColorRgb.hsb2rgb(QuasiRandomTools.vanDerCorput(3, i), QuasiRandomTools.vanDerCorput(5, i) * 0.375f + 0.5f, QuasiRandomTools.vanDerCorput(7, i) * 0.35f + 0.6f, 1f);
        }
        //// you will probably want to change batch to use whatever rendering system is appropriate
        //// for your game; here it always renders pixels
        batch = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new StretchViewport(width, height);
        seed = 1337;
        rng = new DistinctRandom(seed);

        Noise noise = new Noise(rng.nextInt(), 1f, Noise.HONEY_FRACTAL, 2);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.25);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, noise, 0.6);
        //world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.75);
//        world = new WorldMapGenerator.MimicMap(seed, new FastNoise(rng.nextInt(), 1f, FastNoise.SIMPLEX_FRACTAL, 2), 0.7);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.7);
//        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, new FastNoise(rng.nextInt(), 1f, FastNoise.SIMPLEX_FRACTAL, 2), 0.7);
        //world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
        world = new HyperellipticalWorldMap(seed, width, height, noise, 1.2f, 0.0625f, 2.5f);
//        world = new EllipticalWorldMap(seed, width, height, noise, 1.2f);
//        world = new WorldMapGenerator.SphereMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.6);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 0.65);
//        world = new WorldMapGenerator.LocalMimicMap(seed, ((WorldMapGenerator.LocalMimicMap) world).earth.not(), WorldMapGenerator.DEFAULT_NOISE, 0.9);
        inner = new LocalMap(seed, width, height, new Noise(rng.nextInt(), 0.5f, Noise.FOAM_FRACTAL, 2), 0.8f);
        wmv = new BlendedWorldMapView(world);
        pm = new PoliticalMapper();
//        wmv.initialize(SColor.CW_FADED_RED, SColor.AURORA_BRICK, SColor.DEEP_SCARLET, SColor.DARK_CORAL,
//                SColor.LONG_SPRING, SColor.WATER_PERSIMMON, SColor.AURORA_HOT_SAUCE, SColor.PALE_CARMINE,
//                SColor.AURORA_LIGHT_SKIN_3, SColor.AURORA_PINK_SKIN_2,
//                SColor.AURORA_PUTTY, SColor.AURORA_PUTTY, SColor.ORANGUTAN, SColor.SILVERED_RED, null);
        input = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.ENTER:
                        seed = rng.nextLong();
                        generate(seed);
                        rng.setSelectedState(0, seed);
                        break;
                    case Input.Keys.PLUS:
                    case Input.Keys.NUMPAD_ADD:
                    case Input.Keys.EQUALS:
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
                    zoomOut(screenX, screenY);
                }
                else
                {
                    zoomIn(screenX, screenY);
                }
                return true;

            }
        };
        generate(seed);
        rng.setSelectedState(0, seed);
        Gdx.input.setInputProcessor(input);
    }

    public void zoomIn() {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomIn();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomIn(int zoomX, int zoomY)
    {
        if(world.heightCodeData[zoomX][zoomY] >= 100)
            return;
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomIn(1, zoomX, zoomY);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);


//        inner.rng.setState(LongPointHash.hashAll(zoomX, zoomY, world.rng.getStateA()), LongPointHash.hashAll(zoomX, zoomY, world.rng.getStateB()));
//        int[][] colors = wmv.getColorMapOklab();
//        wmv.match(colors[zoomX][zoomY],
//                colors[(zoomX + 2) % colors.length][zoomY],
//                colors[zoomX][(zoomY + 2) % colors[0].length],
//                colors[(zoomX + colors.length - 2) % colors.length][zoomY],
//                colors[zoomX][(zoomY + colors[0].length - 2) % colors[0].length]
//        );
//        wmv.setWorld(inner);
//        wmv.generate(inner.rng.getStateA(), inner.rng.getStateB(), world.heightCodeData[zoomX][zoomY] < 4 ? 0f : 4f +
//                        world.heightData[zoomX][zoomY],
//                world.heatData[zoomX][zoomY] + 0.5f);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomOut()
    {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomOut();
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void zoomOut(int zoomX, int zoomY)
    {
        long startTime = System.nanoTime();
//        noiseCalls = 0;
        world.zoomOut(1, zoomX, zoomY);
        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);

//        wmv.initialize();
//        wmv.setWorld(world);
//        wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void generate(final long seed)
    {
        long startTime = System.nanoTime();
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
        political = pm.generate(seed + 1000L, world, wmv.getBiomeMapper(), null, 50, 1f);
        System.out.println(pm.atlas.toString("\n"));
        DungeonTools.debugPrint(pm.politicalMap);
        // earlier settings
//        wmv.generate((int)(seed & 0xFFFFFFFFL), (int) (seed >>> 32),
//                0.9 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.3,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.55 + 0.9);
        // implementation used in WorldMapView.generate() with no args
//        wmv.generate(world.seedA, world.seedB,
//                1.0 + NumberTools.formCurvedDouble((world.seedA ^ 0x123456789ABCDL) * 0x12345689ABL ^ world.seedB) * 0.25,
//                DiverRNG.determineDouble(world.seedB * 0x12345L + 0x54321L ^ world.seedA) * 0.25 + 1.0);

        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }
    public void rotate()
    {
        long startTime = System.currentTimeMillis();
        world.setCenterLongitude((startTime & 0xFFFL) * 0x1p-12f);
        //// maybe comment in next line if using something other than RotatingSpaceView
        //wmv.generate(world.seedA, world.seedB, world.landModifier, world.heatModifier);
        //// comment out next line if using something other than RotatingSpaceView
        wmv.getBiomeMapper().makeBiomes(world);
        wmv.show();
        ttg = System.nanoTime() - startTime >> 20;
    }


    public void putMap() { 
        int[][] cm = wmv.getColorMapOklab();
        int[][] bio = wmv.getBiomeMapper().getBiomeCodeData();
        batch.begin(view.getCamera().combined, GL20.GL_POINTS);
        int c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if((bio[x][y] & 1023) < 42 && wmv.getWorld() != inner)
                    c = DescriptiveColor.lerpColors(cm[x][y], NATION_COLORS[political[x][y] % 49], nation);
                else
                    c = cm[x][y];
                batch.color(DescriptiveColor.rgbaIntToFloat(DescriptiveColor.toRGBA8888(c)));
                batch.vertex(x, height - 1 - y, 0f);
            }
        }
        batch.end();
    }
    
    @Override
    public void render() {
        // standard clear the background routine for libGDX
        ScreenUtils.clear(INK);
        Gdx.gl.glDisable(GL20.GL_BLEND);
        nation = MathTools.swayTight((System.nanoTime() & 0xFFFFFFFFL) * 0x1p-31f);
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
        config.setTitle("SquidSquad Demo: Political World Map");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        new Lwjgl3Application(new WorldPoliticalDemo(), config);
    }
}
