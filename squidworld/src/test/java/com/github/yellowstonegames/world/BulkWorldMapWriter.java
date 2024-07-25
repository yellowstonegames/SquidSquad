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
public class BulkWorldMapWriter extends ApplicationAdapter {
    private static final int AA = 1;

//    private static final int width = 1920, height = 1080;
//    private static final int width = 256, height = 256; // localMimic
//    private static final int width = 400, height = 400;
    private static final int width = 512, height = 256; // mimic, elliptical
//    private static final int width = 256, height = 128; // mimic, elliptical
//    private static final int width = 2048, height = 1024; // mimic, elliptical
//    private static final int width = 128, height = 128; // space view, MimicLocal
//    private static final int width = 1200, height = 400; // squat
//    private static final int width = 300, height = 300;
    //private static final int width = 314 * 4, height = 400;

//    private static final int width = 512, height = 512;
//    private static final int width = 512 >>> AA, height = 256 >>> AA; // mimic world
//    private static final int width = 256 >>> AA, height = 256 >>> AA; // mimic local
//    private static final int width = 1024, height = 512; // elliptical

//    private static final boolean FLOWING_LAND = false;
//    private static final boolean ALIEN_COLORS = false;

    private Thesaurus thesaurus;

    private boolean classNameMode = true;
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

//        if(classNameMode)
//            rng = new DistinctRandom(0L);
//        else
//            rng = new DistinctRandom(Hasher.balam.hash64(date));
        rng = new DistinctRandom(Hasher.malphas.hash64(date));
        rng.setState(rng.nextLong() + 1000L); // change addend when you need different results on the same date
        seed = rng.getSelectedState(0);

        INoise[] noises = new INoise[]{
                new NoiseWrapper(new FoamNoise(seed), seed, 1.3f, NoiseWrapper.FBM, 1),
                new NoiseWrapper(new PerlinNoise(seed), seed, 0.6f, NoiseWrapper.FBM, 2),
                new NoiseWrapper(new PerlueNoise(seed), seed, 0.6f, NoiseWrapper.FBM, 2),
                new NoiseWrapper(new SimplexNoise(seed), seed, 0.3f, NoiseWrapper.FBM, 2),
                new NoiseWrapper(new HoneyNoise(seed), seed, 0.3f, NoiseWrapper.FBM, 2),
                new NoiseWrapper(new ValueNoise(seed), seed, 0.8f, NoiseWrapper.FBM, 3),
                new NoiseWrapper(new CyclicNoise(seed, 4, 1.5f), seed, 1f, NoiseWrapper.FBM, 1),
                new NoiseWrapper(new SorbetNoise(seed, 4, 1.4f), seed, 1f, NoiseWrapper.FBM, 1),
        };

        for(INoise noise : noises) {
            this.noise = noise;

            WorldMapGenerator[] generators = {
                    new RoundSideWorldMap(seed, width << AA, height << AA, noise, 2f),
                    new HexagonalWorldMap(seed, width << AA, height << AA, noise, 2f),
                    new HyperellipticalWorldMap(seed, width << AA, height << AA, noise, 2f),
                    new EllipticalWorldMap(seed, width << AA, height << AA, noise, 2f),
                    // LatLonWorldMap isn't quite the same as the others here.
//                    new LatLonWorldMap(seed, width << AA, height << AA, noise, 2f),
                    new StretchWorldMap(seed, width << AA, height << AA, noise, 2f),
                    // square, not rectangular
                    new TilingWorldMap(seed, width << AA, width << AA, noise, 2f),
                    new GlobeMap(seed, width << AA, width << AA, noise, 2f),
            };
            for (WorldMapGenerator world : generators) {
                this.world = world;
                thesaurus = new Thesaurus(rng);

                wmv = new BlendedWorldMapView(world);
                rng.setSeed(seed);

                path = "out/worlds/" + date + "/bulk/" + noise.stringSerialize() + "/";

                if (!Gdx.files.local(path).exists())
                    Gdx.files.local(path).mkdirs();

                Gdx.graphics.setContinuousRendering(false);
                putMap();
            }
        }
        Gdx.app.exit();
    }

    public void generate(final long seed)
    {
        long startTime = System.currentTimeMillis();
        //randomizeColors(seed);
//        world.generate(1, 1.125, seed); // mimic of Earth favors too-cold planets
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
        Pixmap temp = new Pixmap(world.width * cellWidth, world.height * cellHeight, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
        generate(hash);
        wmv.getBiomeMapper().makeBiomes(world);
        int[][] cm = wmv.show();
        temp.setColor(INK);
        temp.fill();

        final int bw = world.width, bh = world.height;
        for (int x = 0; x < bw; x++) {
            for (int y = 0; y < bh; y++) {
                temp.drawPixel(x, y, cm[x][y]);
            }
        }
        pm = new Pixmap(world.width * cellWidth, world.height * cellHeight, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);

        pm.setFilter(Pixmap.Filter.BiLinear);
        pm.drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm.getWidth(), pm.getHeight());
        png.write(Gdx.files.local(path + name + ".png"), pm);
//        PixmapIO.writePNG(Gdx.files.local(path + name + ".png"), pm);
        temp.dispose();
        pm.dispose();

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
        config.setTitle("SquidSquad: Bulk World Map Writer");
        config.setWindowedMode(400, 200);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new BulkWorldMapWriter(), config);
    }
}
