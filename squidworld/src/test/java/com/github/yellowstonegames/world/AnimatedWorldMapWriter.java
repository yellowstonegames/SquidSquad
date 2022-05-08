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
import com.github.tommyettinger.anim8.AnimatedPNG;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.ds.support.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.Hasher;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.IPointHash;
import com.github.yellowstonegames.grid.Noise;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

/**
 * Writes one or more spinning globes to the out/ folder.
 */
public class AnimatedWorldMapWriter extends ApplicationAdapter {
//    private static final int width = 1920, height = 1080;
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 420, height = 210; // mimic, elliptical
//    private static final int width = 512, height = 256; // mimic, elliptical
//    private static final int width = 1024, height = 512; // mimic, elliptical
//    private static final int width = 2048, height = 1024; // mimic, elliptical
    private static final int width = 256, height = 256; // space view
//    private static final int width = 1200, height = 400; // squat
//    private static final int width = 300, height = 300;
    //private static final int width = 314 * 4, height = 400;
//    private static final int width = 512, height = 512;

    private static final int LIMIT = 5;
    private static final int FRAMES = 240;
//    private static final boolean FLOWING_LAND = true;
//    private static final boolean ALIEN_COLORS = false;
    private static final boolean SEEDY = false;
    private int baseSeed = 1234567890;
    private int AA = 1;

    private Thesaurus thesaurus;
    private String makeName(final Thesaurus thesaurus)
    {
        if(SEEDY) return String.valueOf(++baseSeed);
        else return StringTools.capitalize(thesaurus.makePlantName(Language.MALAY).replaceAll("'s", "")).replaceAll("\\W", "");
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
    
    private String date, path;
    private Noise noise;
    private static final Color INK = new Color(DescriptiveColor.describe("darkmost dullest mauve"));
    @Override
    public void create() {
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        date = DateFormat.getDateInstance().format(new Date());
//        path = "out/worldsAnimated/" + date + "/Sphere/";
//        path = "out/worldsAnimated/" + date + "/SphereQuilt/";
//        path = "out/worldsAnimated/" + date + "/SphereQuilt/";
//        path = "out/worldsAnimated/" + date + "/SphereExpo/";
//        path = "out/worldsAnimated/" + date + "/Ellipse/";
//        path = "out/worldsAnimated/" + date + "/EllipseExpo/";
//        path = "out/worldsAnimated/" + date + "/Mimic/";
//        path = "out/worldsAnimated/" + date + "/SpaceView/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantClassic/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantFoam/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewMutantHoney/";
        path = "out/worldsAnimated/" + date + "/SpaceViewValue/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewValueCrescent/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewClassic/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewSeedy/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewPerlin/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewHoney/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewFoam/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewSimplex/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewRidged/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseWrithing/";
//        path = "out/worldsAnimated/" + date + "/Sphere_Classic/";
//        path = "out/worldsAnimated/" + date + "/Hyperellipse/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseExpo/";
//        path = "out/worldsAnimated/" + date + "/HyperellipseQuilt/";
//        path = "out/worldsAnimated/" + date + "/Tiling/";
//        path = "out/worldsAnimated/" + date + "/RoundSide/";
//        path = "out/worldsAnimated/" + date + "/Local/";
//        path = "out/worldsAnimated/" + date + "/LocalSquat/";
//        path = "out/worldsAnimated/" + date + "/LocalMimic/";
//        path = "out/worldsAnimated/" + date + "/EllipseHammer/";
        
        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();

        pm = new Pixmap[FRAMES];
        for (int i = 0; i < pm.length; i++) {
            pm[i] = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
            pm[i].setBlending(Pixmap.Blending.None);
        }

        writer = new AnimatedGif();
//        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.PATTERN);
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.NEUE);
        writer.setDitherStrength(0.75f);
        writer.setFlipY(false);
        apng = new AnimatedPNG();
        apng.setFlipY(false);
        rng = new DistinctRandom(Hasher.balam.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date  
        //rng = new StatefulRNG(0L);
        seed = rng.getSelectedState(0);
        
        thesaurus = new Thesaurus(rng);

//        final Noise.Noise3D noise = new Noise.Noise3D() {
//            @Override
//            public double getNoise(double x, double y, double z) {
//                return WorldMapGenerator.DEFAULT_NOISE.getNoiseWithSeed(x, y, z, mutationA, mutationB, 123456789);
//            }
//
//            @Override
//            public double getNoiseWithSeed(double x, double y, double z, long seed) {
//                return WorldMapGenerator.DEFAULT_NOISE.getNoiseWithSeed(x, y, z, mutationA, mutationB, seed);
//            }
//        };
        
        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.FOAM_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 1);

        fn.setInterpolation(Noise.QUINTIC);

        if(SEEDY) {
            fn.setPointHash(new IPointHash.IntImpl() {
                @Override
                public int hashWithState(int x, int y, int state) {
                    return (int) (0xC13FA9A902A6328FL * x + 0x91E10DA5C79E7B1DL * y + 0x9E3779B97F4A7C15L * state);
                }

                @Override
                public int hashWithState(int x, int y, int z, int state) {
                    return (int) (0xD1B54A32D192ED03L * x + 0xABC98388FB8FAC03L * y + 0x8CB92BA72F3D8DD7L * z + 0x9E3779B97F4A7C15L * state);
                }

                @Override
                public int hashWithState(int x, int y, int z, int w, int state) {
                    return (int) (0xDB4F0B9175AE2165L * x + 0xBBE0563303A4615FL * y + 0xA0F2EC75A1FE1575L * z + 0x89E182857D9ED689L * w + 0x9E3779B97F4A7C15L * state);
                }

                @Override
                public int hashWithState(int x, int y, int z, int w, int u, int state) {
                    return (int) (0xE19B01AA9D42C633L * x + 0xC6D1D6C8ED0C9631L * y + 0xAF36D01EF7518DBBL * z + 0x9A69443F36F710E7L * w + 0x881403B9339BD42DL * u + 0x9E3779B97F4A7C15L * state);
                }

                @Override
                public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
                    return (int) (0xE60E2B722B53AEEBL * x + 0xCEBD76D9EDB6A8EFL * y + 0xB9C9AA3A51D00B65L * z + 0xA6F5777F6F88983FL * w + 0x9609C71EB7D03F7BL * u + 0x86D516E50B04AB1BL * v + 0x9E3779B97F4A7C15L * state);
                }
            });
        }
//        if(FLOWING_LAND)
//            noise = new Noise.Adapted3DFrom5D(fn);
//        else
            noise = fn;
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
        world = new RotatingGlobeMap(seed, width << AA, height << AA, noise, 1.25f);
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
        if(SEEDY){
            wmv.generate(1.0f, 1.25f);
        }
        else {
            wmv.generate(0.9f, 1.25f);
        }
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name = makeName(thesaurus);
        while (Gdx.files.local(path + name + ".gif").exists())
            name = makeName(thesaurus);
        long hash;
        if(SEEDY) hash = baseSeed;
        else hash = Hasher.balam.hash64(name);
        worldTime = System.currentTimeMillis();
        Pixmap temp = new Pixmap(width * cellWidth << AA, height * cellHeight << AA, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
        for (int i = 0; i < pm.length; i++) {
            float angle = i / (float)pm.length;
//            if(FLOWING_LAND) {
//                ((Noise.Adapted3DFrom5D)noise).w = TrigTools.cosTurns(angle) * 0.3125f;
//                ((Noise.Adapted3DFrom5D)noise).u = TrigTools.sinTurns(angle) * 0.3125f;
//            }
            world.setCenterLongitude(angle * MathUtils.PI2);
            generate(hash);
            wmv.getBiomeMapper().makeBiomes(world);
            int[][] cm = wmv.show();
            temp.setColor(INK);
            temp.fill();

            final int bw = width << AA, bh = height << AA;
            for (int x = 0; x < bw; x++) {
                for (int y = 0; y < bh; y++) {
                    temp.drawPixel(x, y, cm[x][y]);
                }
            }
            pm[i].setFilter(Pixmap.Filter.BiLinear);
            pm[i].drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm[i].getWidth(), pm[i].getHeight());
            if(i % (FRAMES/10) == (FRAMES/10-1)) System.out.print(((i + 1) * 10 / (FRAMES/10)) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }
        Array<Pixmap> pms = new Array<>(pm);
        writer.palette = new PaletteReducer(pms);
        writer.write(Gdx.files.local(path + name + ".gif"), pms, 16);
        apng.write(Gdx.files.local(path + name + ".png"), pms, 16);
        temp.dispose();

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
        new Lwjgl3Application(new AnimatedWorldMapWriter(), config);
    }
}
