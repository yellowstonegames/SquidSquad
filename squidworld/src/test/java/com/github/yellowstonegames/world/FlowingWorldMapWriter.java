package com.github.yellowstonegames.world;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.AnimatedPNG;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

import static com.github.tommyettinger.digital.BitConversion.longBitsToDouble;

/**
 * Writes one or more spinning globes to the out/ folder.
 */
public class FlowingWorldMapWriter extends ApplicationAdapter {
    private static final int width = 256, height = 256;

    private static final int FRAMES = 240;
    private static final int LIMIT = 3;
    private static final boolean FLOWING_LAND = true;
    private static final boolean ALIEN_COLORS = false;
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
    private AnimatedPNG apng;
    private PixmapIO.PNG pngWriter;

    private String date, path;
    private float mutationA, mutationB;
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));
    @Override
    public void create() {
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        date = DateFormat.getDateInstance().format(new Date());
//        path = "out/worldsAnimated/" + date + "/FlowingClassic/";
//        path = "out/worldsAnimated/" + date + "/Foam/";
//        path = "out/worldsAnimated/" + date + "/Classic/";
//        path = "out/worldsAnimated/" + date + "/FlowingFoamMaelstrom/";
//        path = "out/worldsAnimated/" + date + "/FlowingFoamAlien/";
//        path = "out/worldsAnimated/" + date + "/FlowingPear/";
//        path = "out/worldsAnimated/" + date + "/FlowingFlan/";
//        path = "out/worldsAnimated/" + date + "/FlowingTaffy/";
//        path = "out/worldsAnimated/" + date + "/FlowingFoam/";
        path = "out/worldsAnimated/" + date + "/FlowingSimplex/";
//        path = "out/worldsAnimated/" + date + "/FlowingClassic/";
//        path = "out/worldsAnimated/" + date + "/FlowingValue/";
//        path = "out/worldsAnimated/" + date + "/FlowingHoney/";

        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();

        pm = new Pixmap[FRAMES];
        for (int i = 0; i < FRAMES; i++) {
            pm[i] = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
            pm[i].setBlending(Pixmap.Blending.None);
        }

        writer = new AnimatedGif();
//        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.PATTERN);
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.NEUE);
        writer.setDitherStrength(0.75f);
        writer.fastAnalysis = true;
        writer.setFlipY(false);
        apng = new AnimatedPNG();
        apng.setFlipY(false);
        apng.setCompression(7);
        pngWriter = new PixmapIO.PNG();
        pngWriter.setFlipY(false);
        rng = new DistinctRandom(Hasher.balam.hash64(date));
//        rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date
        //rng = new StatefulRNG(0L);
        seed = rng.getSelectedState(0);
        
        thesaurus = new Thesaurus(rng);

//        Noise fn = new Noise((int) seed, 0.8f, Noise.PEAR_FRACTAL, 1);    // between 43797ms and 45564ms
//        Noise fn = new Noise((int) seed, 2f, Noise.FLAN_FRACTAL, 1);    // between 50908ms and 52215ms
//        Noise fn = new Noise((int) seed, 3.5f, Noise.TAFFY_FRACTAL, 1); // between 69783ms and 72929ms
//        Noise fn = new Noise((int) seed, 1f, Noise.FOAM_FRACTAL, 1);    // between 130930ms and 131995ms
//        Noise fn = new Noise((int) seed, 0.75f, Noise.SIMPLEX_FRACTAL, 2);   // between 34428ms and 38706ms
        Noise fn = new Noise((int) seed, 1f, Noise.SIMPLEX_FRACTAL, 1);   //
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 3, 2.6f, 1f/2.6f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1f, Noise.HONEY_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1f, Noise.HONEY_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1f, Noise.PERLIN_FRACTAL, 1);  // between 35894ms and 42264ms

//        fn.setInterpolation(Noise.HERMITE);

        Noise terrainRidgedNoise = new Noise(fn) {
            @Override
            public float getNoise(float x, float y, float z) {
                return getNoise(x, y, z, mutationA, mutationB);
            }
            @Override
            public float getNoiseWithSeed(float x, float y, float z, long seed) {
                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
            }
        }, terrainBasicNoise = new Noise(fn) {
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
//        terrainBasicNoise.setMutation(1.618f);
        
        //// Maelstrom
//        Noise terrainRidgedNoise = new Noise(fn) {
//            @Override
//            public float getNoise(float x, float y, float z) {
//                return (float) (Math.exp(getNoise(x, y, z, mutationA, mutationB)) * 0.850918 - 1.31303495);
//            }
//            @Override
//            public float getNoiseWithSeed(float x, float y, float z, long seed) {
//                return (float) (Math.exp(getNoiseWithSeed(x, y, z, mutationA, mutationB, seed)) * 0.850918 - 1.31303495);
//            }
//        }, terrainBasicNoise = new Noise(fn) {
//            @Override
//            public float getNoise(float x, float y, float z) {
//                return getNoise(x, y, z, mutationA, mutationB);
//            }
//
//            @Override
//            public float getNoiseWithSeed(float x, float y, float z, long seed) {
//                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
//            }
//        }, heatNoise = new Noise(fn) {
//            @Override
//            public float getNoise(float x, float y, float z) {
//                return getNoise(x, y, z, mutationA, mutationB);
//            }
//
//            @Override
//            public float getNoiseWithSeed(float x, float y, float z, long seed) {
//                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
//            }
//        }, moistureNoise = new Noise(fn) {
//            @Override
//            public float getNoise(float x, float y, float z) {
//                return getNoise(x, y, z, mutationA, mutationB);
//            }
//
//            @Override
//            public float getNoiseWithSeed(float x, float y, float z, long seed) {
//                return getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
//            }
//        }, otherRidgedNoise = new Noise(fn) {
//            @Override
//            public float getNoise(float x, float y, float z) {
//                return (float) (Math.exp(getNoise(x, y, z, mutationA, mutationB)) * 0.850918 - 1.31303495);
//            }
//            @Override
//            public float getNoiseWithSeed(float x, float y, float z, long seed) {
//                return (float) (Math.exp(getNoiseWithSeed(x, y, z, mutationA, mutationB, seed)) * 0.850918 - 1.31303495);
//            }
//        };

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
//        world = new GlobeMap(seed, width, height, terrainBasicNoise, 1f);
        world = new GlobeMap(seed, width, height, terrainRidgedNoise, terrainBasicNoise, heatNoise, moistureNoise, otherRidgedNoise, 0.75f);
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
    /**
     * A different kind of determine-like method that expects to be given a random long and produces a random double
     * with a curved distribution that centers on 0 (where it has a bias) and can (rarely) approach -1f and 1f.
     * The distribution for the values is similar to Irwin-Hall, and is frequently near 0 but not too-rarely near -1.0
     * or 1.0. It cannot produce 1.0, -1.0, or any values further from 0 than those bounds.
     * @param start a long, usually random, such as one produced by any RandomnessSource; all bits will be used
     * @return a deterministic double between -1.0 (exclusive) and 1.0 (exclusive); very likely to be close to 0.0
     */
    public static double formCurvedDouble(long start) {
        return    longBitsToDouble((start >>> 12) | 0x3fe0000000000000L)
                + longBitsToDouble(((start *= 0xF1357AEA2E62A9C5L) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *= 0xF1357AEA2E62A9C5L) >>> 12) | 0x3fe0000000000000L)
                - longBitsToDouble(((start *  0xF1357AEA2E62A9C5L) >>> 12) | 0x3fe0000000000000L)
                ;
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
//        wmv.generate(0.9f, wmv.world.heatModifier);
        wmv.generate(
                (float) (1.0 + formCurvedDouble(world.seedA * 0x123456789ABCDEFL ^ world.seedB) * 0.1875),
                (float) (1.0625 + Hasher.randomize1Double(world.seedB * 0x123456789ABL ^ world.seedA) * 0.375));
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".gif").exists())
            name = makeName(thesaurus);
//        Gdx.files.local(path + name + "_frames/").mkdirs();
        long hash;
        hash = Hasher.balam.hash64(name);
        worldTime = System.currentTimeMillis();
        world.rng.setSeed(hash);
        if (ALIEN_COLORS) {
            wmv.initialize(world.rng.nextFloat(), world.rng.nextFloat() * 0.2f - 0.1f, world.rng.nextFloat() * 0.3f - 0.15f, world.rng.nextFloat() * 0.2f + 0.9f);
        }
//        wmv.world.heatModifier = world.rng.nextFloat(1.15f, 1.5f);
//        try {
            for (int i = 0; i < FRAMES; i++) {
                float angle = i / (float) FRAMES;
                mutationA = TrigTools.sinTurns(angle) * 0.3125f;
                mutationB = TrigTools.cosTurns(angle) * 0.3125f;

                world.setCenterLongitude(angle * TrigTools.PI2);
                generate(hash);
                int[][] cm = wmv.show();
                pm[i].setColor(INK);
                pm[i].fill();

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        pm[i].drawPixel(x, y, cm[x][y]);
                    }
                }
//                pngWriter.write(Gdx.files.local(path + name + "_frames/frame_" + i + ".png"), pm[i]);
                if(i % (FRAMES / 10) == (FRAMES / 10) - 1) System.out.print(((i + 1) * 100 / FRAMES) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");

//                if (i % 18 == 17)
//                    System.out.print(((i + 1) * 10 / 18) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
            }
            Array<Pixmap> pms = new Array<>(pm);
            writer.setDitherStrength(0.75f);
            writer.palette = new PaletteReducer(pms);
            writer.write(Gdx.files.local(path + name + ".gif"), pms, 16);
            apng.write(Gdx.files.local(path + name + ".png"), pms, 16);
//            writer.write(Gdx.files.local(path + name + ".png"), pms, 20);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println();
        System.out.println("World #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
    }
    @Override
    public void render() {
//        Gdx.graphics.setTitle("Map! Took " + ttg + " ms to generate");
    }

    @Override
    public void resize(int width, int height) {
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Demo: Flowing World Map Writer");
        config.setWindowedMode(width * cellWidth, height * cellHeight);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new FlowingWorldMapWriter(), config);
    }
}
