package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.core.TrigTools;
import com.github.yellowstonegames.grid.IPointHash;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

/**
 * Writes one or more spinning globes to the out/ folder.
 */
public class FlowingWorldMapWriter extends ApplicationAdapter {
    private static final int width = 300, height = 300;

    private static final int LIMIT = 5;
    private static final boolean FLOWING_LAND = true;
//    private static final boolean ALIEN_COLORS = false;
    private int baseSeed = 1234567890;

    private Thesaurus thesaurus;
    private String makeName(final Thesaurus thesaurus)
    {
        return StringTools.capitalize(thesaurus.makePlantName(Language.MALAY).replaceAll("'s", "")).replaceAll("\\W", "");
    }

    private Pixmap[] pm;
    private int counter;
    private static final int cellWidth = 1, cellHeight = 1;
    private Viewport view;
    private DistinctRandom rng;
    private long seed;
    private long ttg, worldTime; // time to generate, world starting time
    private WorldMapGenerator world;
    private WorldMapView wmv;
    private AnimatedGif writer;
    
    private String date, path;
    private float mutationA, mutationB;
    private static final Color INK = new Color(DescriptiveColor.describe("darkmost dullest mauve"));
    @Override
    public void create() {
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        date = DateFormat.getDateInstance().format(new Date());
        path = "out/worldsAnimated/" + date + "/FlowingClassic/";
//        path = "out/worldsAnimated/" + date + "/FlowingFoam/";
//        path = "out/worldsAnimated/" + date + "/FlowingSimplex/";
//        path = "out/worldsAnimated/" + date + "/FlowingHoney/";

        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();

        pm = new Pixmap[360];
        for (int i = 0; i < pm.length; i++) {
            pm[i] = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
            pm[i].setBlending(Pixmap.Blending.None);
        }

        writer = new AnimatedGif();
//        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.PATTERN);
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.SCATTER);
        writer.setFlipY(false);
        rng = new DistinctRandom(Hasher.balam.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getSelectedState(0);
        
        thesaurus = new Thesaurus(rng);

//        Noise fn = new Noise((int) seed, 1.4f, Noise.FOAM_FRACTAL, 1);
        Noise fn = new Noise((int) seed, 1f, Noise.PERLIN_FRACTAL, 1);

        Noise terrainNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }

            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        }, terrainLayeredNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }

            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        }, heatNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }

            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        }, moistureNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }

            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        }, otherRidgedNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }

            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        };
//        WorldMapGenerator.DEFAULT_NOISE.setNoiseType(FastNoise.HONEY);
//        WorldMapGenerator.DEFAULT_NOISE.setFrequency(1.25f);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalOctaves(1);
//        WorldMapGenerator.DEFAULT_NOISE.setFractalType(FastNoise.BILLOW);
        
//        world = new WorldMapGenerator.RotatingSpaceMap(seed, width, height, noise, 1.0);
        
//        world = new WorldMapGenerator.SphereMap(seed, width, height, noise, 1.0);
//        world = new WorldMapGenerator.TilingMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, noise, 1.75);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, noise, 1.3);
        world = new GlobeMap(seed, width, height, terrainNoise, terrainLayeredNoise, heatNoise, moistureNoise, otherRidgedNoise, 0.75f);
//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8, 0.03125, 2.5);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.5, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.8, 0.03125, 2.5);
        wmv = new WorldMapView(world);

        //generate(seed);
        rng.setSeed(seed);
        Gdx.graphics.setContinuousRendering(false);
        for (int i = 0; i < LIMIT; i++) {
            putMap();
        }
        Gdx.app.exit();
    }

    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        //randomizeColors(seed);
//        world.generate(1, 1.125, seed); // mimic of Earth favors too-cold planets
//        dbm.makeBiomes(world);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, FastNoise.instance, octaveCounter * 0.001);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, ClassicNoise.instance, octaveCounter * 0.001, 0.0625, 2.5);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, ClassicNoise.instance, octaveCounter * 0.001);
//        world.generate(0.95 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.15,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 1.0, seed);
//        dbm.makeBiomes(world);
        world.rng.setSeed(seed);
        world.seedA = world.rng.getStateA();
        world.seedB = world.rng.getStateB();
//        if(ALIEN_COLORS) {
//            wmv.initialize(world.rng.nextFloat() * 0.7f - 0.35f, world.rng.nextFloat() * 0.2f - 0.1f, world.rng.nextFloat() * 0.3f - 0.15f, world.rng.nextFloat() + 0.2f);
//        }
        wmv.generate(0.9f, 1.25f);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".gif").exists())
            name = makeName(thesaurus);
        long hash;
        hash = Hasher.balam.hash64(name);
        worldTime = System.currentTimeMillis();
        for (int i = 0; i < pm.length; i++) {
            float angle = i / (float)pm.length;
            mutationA = TrigTools.cos_(angle) * 0.125f;
            mutationB = TrigTools.sin_(angle) * 0.125f;
            world.setCenterLongitude(angle * MathUtils.PI2);
            generate(hash);
            wmv.getBiomeMapper().makeBiomes(world);
            int[][] cm = wmv.show();
            pm[i].setColor(INK);
            pm[i].fill();

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    pm[i].drawPixel(x, y, cm[x][y]);
                }
            }
            if(i % 36 == 35) System.out.print(((i + 1) * 10 / 36) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }
        Array<Pixmap> pms = new Array<>(pm);
        writer.palette = new PaletteReducer(pms);
        writer.palette.setDitherStrength(0.5f);
        writer.write(Gdx.files.local(path + name + ".gif"), pms, 30);

        System.out.println();
        System.out.println("World #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
    }
    @Override
    public void render() {
        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
    }

    @Override
    public void resize(int width, int height) {
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidLib Demo: Animated World Map Writer");
        config.setWindowedMode(width * cellWidth, height * cellHeight);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new FlowingWorldMapWriter(), config);
    }
}
