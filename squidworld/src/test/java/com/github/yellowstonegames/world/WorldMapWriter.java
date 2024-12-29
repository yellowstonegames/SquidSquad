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

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.FastPNG;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

/**
 * Writes one or more still globes to the out/ folder.
 */
public class WorldMapWriter extends ApplicationAdapter {
    private static final int AA = 1;

//    private static final int width = 1920, height = 1080;
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 400, height = 400;
//    private static final int width = 512, height = 256; // mimic, elliptical
//    private static final int width = 256, height = 128; // mimic, elliptical
//    private static final int width = 2048, height = 1024; // mimic, elliptical
//    private static final int width = 128, height = 128; // space view, MimicLocal
//    private static final int width = 1200, height = 400; // squat
//    private static final int width = 300, height = 300;
    private static final int width = 512, height = 256;

//    private static final int width = 512, height = 512;
//    private static final int width = 512 >>> AA, height = 256 >>> AA; // mimic world
//    private static final int width = 256 >>> AA, height = 256 >>> AA; // mimic local
//    private static final int width = 1024, height = 512; // elliptical

    private static final int LIMIT = 5;
//    private static final boolean FLOWING_LAND = true;
//    private static final boolean ALIEN_COLORS = false;

    private int baseSeed = 1234567890;

    private Thesaurus thesaurus;

    private boolean classNameMode = false;
    private String makeName(final Thesaurus thesaurus)
    {
        return StringTools.capitalize(thesaurus.makePlantName(Language.MALAY).replaceAll("'s", "")).replaceAll("\\W", "");
    }

    private Pixmap pm;
    private int counter;
    private static final int cellWidth = 1, cellHeight = 1;
    private Viewport view;
    private DistinctRandom rng;
    private long seed;
    private long ttg, worldTime; // time to generate, world starting time
    private WorldMapGenerator world;
    private WorldMapView wmv;

    private String date, path;
    private INoise noise;
    private FastPNG png;
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));
    @Override
    public void create() {
        view = new StretchViewport(width * cellWidth, height * cellHeight);
        date = DateFormat.getDateInstance().format(new Date());
        png = new FastPNG();
        png.setFlipY(false);

        pm = new Pixmap(width * cellWidth, height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);


        if(classNameMode)
            rng = new DistinctRandom(0L);
        else
            rng = new DistinctRandom(Hasher.balam.hash64(date));
        //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date
        seed = rng.getSelectedState(0);
        
        thesaurus = new Thesaurus(rng);

//        final Noise fn = new Noise((int) seed, 1.5f, Noise.TAFFY_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 1, 3f, 1f/3f);
        Noise fn = new Noise((int) seed, 1.35f, Noise.VALUE_FRACTAL, 3);
//        final Noise fn = new Noise((int) seed, 0.625f, Noise.SIMPLEX_FRACTAL, 2);
//        Noise fn = new Noise((int) seed, 1.666f, Noise.FOAM_FRACTAL, 2);
//        Noise fn = new Noise((int) seed, 1.666f, Noise.FOAM_FRACTAL, 2, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 0.9f, Noise.HONEY_FRACTAL, 2);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 2);
//        Noise fn = new Noise((int) seed, 1f, Noise.SIMPLEX_FRACTAL, 2);
//        Noise fn = new Noise((int) seed, 1f, Noise.SIMPLEX_FRACTAL, 5, 1.6f, 0.625f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 5, 2f, 0.5f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 5, 2f, 1f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 5, 1f, 1f);
//        fn.setFractalType(Noise.DOMAIN_WARP);
        fn.setFractalSpiral(false);
//        noise = fn;

//        noise = new CyclicNoise(seed, 4, 1.9f);
        noise = new NoiseWrapper(new FoamNoise(seed), seed, 1.6f, NoiseWrapper.FBM, 1);

//        if(FLOWING_LAND)
//            noise = new Noise.Adapted3DFrom5D(fn);
//        else

//        world = new MimicWorldMap(seed, noise, 2f);
//        world = new MimicLocalMap(seed, noise, 2f);
//        world = new LocalMap(seed, width << AA, height << AA, noise, 2f);
//        world = new TilingWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new RoundSideWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new HexagonalWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new HyperellipticalWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new EllipticalWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new LatLonWorldMap(seed, width << AA, height << AA, noise, 2f);
        world = new StretchWorldMap(seed, width << AA, height << AA, noise, 2f);
//        world = new GlobeMap(seed, width << AA, height << AA, noise, 1f);
//        world = new RotatingGlobeMap(seed, width << AA, height << AA, noise, 1.25f);
//        world = new WorldMapGenerator.MimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.SpaceViewMap(seed, width, height, noise, 1.3);
//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8, 0.03125, 2.5);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.5, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.8, 0.03125, 2.5);
//        wmv = new DetailedWorldMapView(world);
        wmv = new BlendedWorldMapView(world);

//        Gdx.files.local("EarthFlipped.txt").writeString(Region.decompress(MimicWorldMap.EARTH_ENCODED).flip(false, true).toCompressedString(), false, "UTF8");

        //generate(seed);
        rng.setSeed(seed);
//        path = "out/worlds/" + date + "/EllipseFoam/";
//        path = "out/worlds/" + date + "/EllipseTaffy/";
//        path = "out/worlds/" + date + "/HyperellipseTaffy/";
//        path = "out/worlds/" + date + "/HyperellipseSimplex/";
//        path = "out/worlds/" + date + "/HyperellipseFoam/";
//        path = "out/worlds/" + date + "/HyperellipseFoam/";
//        path = "out/worlds/" + date + "/EllipseFoam/";
//        path = "out/worlds/" + date + "/HexagonFoam/";
//        path = "out/worlds/" + date + "/HexagonTaffy/";
//        path = "out/worlds/" + date + "/HexagonSimplex2/";
//        path = "out/worlds/" + date + "/LocalSimplex2/";
//        path = "out/worlds/" + date + "/MimicLocalSimplex2/";
//        path = "out/worlds/" + date + "/MimicSimplex2/";
//        path = "out/worlds/" + date + "/RoundSideSimplex2/";
//        path = "out/worlds/" + date + "/RoundSideFoam/";
//        path = "out/worlds/" + date + "/TilingFoam/";
//        path = "out/worlds/" + date + "/LocalFoam/";
//        path = "out/worlds/" + date + "/StretchFoam/";
//        path = "out/worlds/" + date + "/LatLonFoam/";
//        path = "out/worlds/" + date + "/GlobePerlinNormal/";

        path = "out/worlds/" + date + "/" + world.getClass().getSimpleName() + "_"
                    + (noise == fn ? (Noise.NOISE_TYPES.getOrDefault(fn.getNoiseType(), "Unknown")) : noise.getTag()) + "_"
                    + wmv.getClass().getSimpleName() + "/";

//        path = "out/worlds/" + date + "/" + world.getClass().getSimpleName() + "_"
//                    + Noise.NOISE_TYPES.getOrDefault(fn.getNoiseType(), "Unknown") + "_"
//                    + wmv.getClass().getSimpleName() + "AlternateFlat" + "/";
//
//        path = "out/worlds/" + date + "/" + world.getClass().getSimpleName() + "_"
//                    + Noise.NOISE_TYPES.getOrDefault(fn.getNoiseType(), "Unknown") + "_"
//                    + wmv.getClass().getSimpleName() + "AlternateFlatter" + "/";

        if(!Gdx.files.local(path).exists())
            Gdx.files.local(path).mkdirs();

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
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, PerlinNoise.instance, octaveCounter * 0.001, 0.0625, 2.5);
//        world = new WorldMapGenerator.EllipticalMap(seed, width, height, PerlinNoise.instance, octaveCounter * 0.001);
//        world.generate(0.95 + NumberTools.formCurvedDouble((seed ^ 0x123456789ABCDL) * 0x12345689ABL) * 0.15,
//                DiverRNG.determineDouble(seed * 0x12345L + 0x54321L) * 0.2 + 1.0, seed);
//        dbm.makeBiomes(world);
        world.rng.setSeed(seed);
        world.seedA = world.rng.getStateA();
        world.seedB = world.rng.getStateB();
//        if(ALIEN_COLORS) {
//            wmv.initialize(world.rng.nextFloat() * 0.7f - 0.35f, world.rng.nextFloat() * 0.2f - 0.1f, world.rng.nextFloat() * 0.3f - 0.15f, world.rng.nextFloat() + 0.2f);
//        }
        wmv.generate(1f, 1.35f);
        ttg = System.currentTimeMillis() - startTime;
    }

    public void putMap() {
        ++counter;
        String name, genName;
        if(classNameMode) {
            name = world.getClass().getSimpleName();
            genName = makeName(thesaurus);
        }
        else {
            genName = name = makeName(thesaurus);
            while (Gdx.files.local(path + name + ".png").exists())
                genName = name = makeName(thesaurus);
        }
        long hash = Hasher.balam.hash64(genName);
        worldTime = System.currentTimeMillis();
        Pixmap temp = new Pixmap(width * cellWidth << AA, height * cellHeight << AA, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
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
        pm.setFilter(Pixmap.Filter.BiLinear);
        pm.drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm.getWidth(), pm.getHeight());
        png.write(Gdx.files.local(path + name + ".png"), pm);
//        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
        temp.dispose();

        System.out.println();
        System.out.println("World #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
        System.out.println("Remaking...");
        worldTime = System.currentTimeMillis();
        String ser = ((StretchWorldMap) world).stringSerialize();

        world = StretchWorldMap.recreateFromString(ser);
        wmv.setWorld(world);
        wmv.getBiomeMapper().makeBiomes(world);
        cm = wmv.show();
        temp = new Pixmap(width * cellWidth << AA, height * cellHeight << AA, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
        temp.setColor(INK);
        temp.fill();

        for (int x = 0; x < bw; x++) {
            for (int y = 0; y < bh; y++) {
                temp.drawPixel(x, y, cm[x][y]);
            }
        }
        pm.setFilter(Pixmap.Filter.BiLinear);
        pm.drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm.getWidth(), pm.getHeight());
        png.write(Gdx.files.local(path + name + "_remade.png"), pm);
        temp.dispose();
        System.out.println("Remake #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
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
        config.setTitle("SquidSquad Demo: World Map Writer");
        config.setWindowedMode(400, 200);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new WorldMapWriter(), config);
    }
}
