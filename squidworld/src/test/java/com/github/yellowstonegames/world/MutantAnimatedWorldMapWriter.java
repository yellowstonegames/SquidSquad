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

/**
 * Writes one or more spinning globes to the out/ folder.
 */
public class MutantAnimatedWorldMapWriter extends ApplicationAdapter {
//    private static final int width = 400, height = 400;
//    private static final int width = 256, height = 256; // space view
    private static final int width = 300, height = 300;
//    private static final int width = 512, height = 512;

    private static final int LIMIT = 8;
//    private static final boolean FLOWING_LAND = true;
    private static final boolean ALIEN_COLORS = true;

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
    private RotatingGlobeMap world;
    private WorldMapView wmv;
    private AnimatedGif writer;
    private String name;
    
    private String date, path;
    private Noise noise;
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));
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
//        path = "out/worldsAnimated/" + date + "/SpaceViewValue/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewClassic/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewSeedy/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewPerlin/";
//        path = "out/worldsAnimated/" + date + "/SpaceViewHoney/";
//        path = "out/worldsAnimated/" + date + "/Mutant/";
        path = "out/worldsAnimated/" + date + "/MutantAlien/";
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

        pm = new Pixmap[360];
        for (int i = 0; i < pm.length; i++) {
            pm[i] = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
            pm[i].setBlending(Pixmap.Blending.None);
        }

        writer = new AnimatedGif();
//        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.PATTERN);
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.NEUE);
        writer.setFlipY(false);
        rng = new DistinctRandom(Hasher.balam.hash64(date));
//        rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date
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
        
        noise = new Noise((int) seed, 1.4f, Noise.MUTANT_FRACTAL, 1);
        noise.setMutation(0.015625f);
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
        world = new RotatingGlobeMap(seed, width, height, noise, 0.95f);
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
        Gdx.graphics.setContinuousRendering(false);
        name = makeName(thesaurus);
        while (Gdx.files.local(path + name + "-0_015625.gif").exists())
            name = makeName(thesaurus);

        for (int i = 0; i < LIMIT; i++) {
            rng.setSeed(seed);
            putMap();
            noise.setMutation(noise.getMutation() + 0.015625f);
            world = new RotatingGlobeMap(seed, width, height, noise, 0.95f);
            wmv.setWorld(world);
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
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, PerlinNoise.instance, octaveCounter * 0.001, 0.0625, 2.5);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, PerlinNoise.instance, octaveCounter * 0.001);
//        world.generate(0.95 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.15,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 1.0, seed);
//        dbm.makeBiomes(world);
        world.rng.setSeed(seed);
        world.seedA = world.rng.getStateA();
        world.seedB = world.rng.getStateB();
        wmv.generate(0.9f, 1.25f);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        long hash;
        hash = Hasher.balam.hash64(name);
        worldTime = System.currentTimeMillis();
        world.rng.setSeed(hash);
        if(ALIEN_COLORS) {
//            wmv.getWorld().rng.setSeed(hash);
            wmv.initialize();
            wmv.alter();
//            wmv.initialize(world.rng.nextFloat(), world.rng.nextFloat() * 0.2f - 0.1f, world.rng.nextFloat() * 0.3f - 0.15f, world.rng.nextFloat() * 0.2f + 0.9f);
        }

        for (int i = 0; i < pm.length; i++) {
            float angle = i / (float)pm.length;
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
        writer.palette.setDitherStrength(1f);
        writer.write(Gdx.files.local(path + name + String.format("-%1.5f.gif", noise.getMutation()).replaceFirst("\\.", "_")), pms, 30);

        System.out.println();
        System.out.println("World #" + counter + ", " + name + " with mutation " + noise.getMutation() + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
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
        config.setTitle("SquidSquad Demo: Mutant World Map Writer");
        config.setWindowedMode(width * cellWidth, height * cellHeight);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new MutantAnimatedWorldMapWriter(), config);
    }
}
