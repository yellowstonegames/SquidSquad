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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.*;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

import static com.github.tommyettinger.anim8.Dithered.DitherAlgorithm.*;

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
    private static final int width = 240, height = 240; // space view
//    private static final int width = 1200, height = 400; // squat
//    private static final int width = 300, height = 300;
    //private static final int width = 314 * 4, height = 400;
//    private static final int width = 512, height = 512;

    private static final int LIMIT = 3;
    private static final int FRAMES = 300;
    private static final boolean FLOWING_LAND = false;
    private static final boolean ALIEN_COLORS = false;
    private static final boolean MANY_STILL = false;
    private static final boolean SEEDY = false;
    private static final boolean SHADOW = true;
    private int baseSeed = 1234567890;
//    private final int AA = 0;
    private final int AA = 1;

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
    private FastPNG png;
    
    private String date, path;
    private INoise noise;
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));
    private static final int ATMOSPHERE = DescriptiveColorRgb.describeRgb("silver 6 white 9 sky 1");
    @Override
    public void create() {
        view = new StretchViewport(width * cellWidth + 10, height * cellHeight + 10);
        date = DateFormat.getDateInstance().format(new Date());

        pm = new Pixmap[FRAMES];
        for (int i = 0; i < pm.length; i++) {
            pm[i] = new Pixmap(width * cellWidth + 10, height * cellHeight + 10, Pixmap.Format.RGBA8888);
//            pm[i].setBlending(Pixmap.Blending.SourceOver);
        }

        writer = new AnimatedGif();
        writer.palette = new QualityPalette();
        writer.setFlipY(false);
        apng = new AnimatedPNG();
        apng.setFlipY(false);
        if(MANY_STILL){
            png = new FastPNG();
            png.setFlipY(false);
        }

        for(DitherAlgorithm dither : new DitherAlgorithm[]{GOURD, GRADIENT_NOISE, BLUE_NOISE, NONE,}) {
//        for(DitherAlgorithm dither : new DitherAlgorithm[]{NONE, GOURD, BLUE_NOISE, ROBERTS, GRADIENT_NOISE, PATTERN}) {
            writer.setDitherAlgorithm(dither);
            writer.setDitherStrength(0.5f);
            rng = new DistinctRandom(Hasher.balam.hash64(date) + 1L);
            seed = rng.state;
            thesaurus = new Thesaurus(rng);
            //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date
            //rng = new StatefulRNG(0L);

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

//        Noise fn = new Noise((int) seed, 3.5f, Noise.TAFFY_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 2f, Noise.PERLIN_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.FOAM_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 1);

//        fn.setInterpolation(Noise.QUINTIC);

            INoise fn = new NoiseWrapper(new PerlueNoise(seed), 0.8f, NoiseWrapper.FBM, 3);
//        INoise fn = new CyclicNoise(seed, 3, 1.5f);

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

            world = new RotatingGlobeMap(seed, width << AA, height << AA, noise, 1.2f);
//        world = new GlobeMap(seed, width << AA, height << AA, noise, 0.75f);

//        world = new WorldMapGenerator.RoundSideMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8, 0.03125, 2.5);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.5, 0.03125, 2.5);
//        world = new WorldMapGenerator.EllipticalHammerMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.LocalMap(seed, width, height, WorldMapGenerator.DEFAULT_NOISE, 0.8);
//        world = new WorldMapGenerator.LocalMimicMap(seed, WorldMapGenerator.DEFAULT_NOISE, 1.75);
//        world = new WorldMapGenerator.HyperellipticalMap(seed, width, height, noise, 0.8, 0.03125, 2.5);

            wmv = new BlendedWorldMapView(world);

            path = "out/worldsAnimated/" + date + "/" + dither.legibleName + "/" +
                    world.getClass().getSimpleName() + noise.getTag() + (SHADOW ? "_shadow/" : "/");

            if (!Gdx.files.local(path).exists())
                Gdx.files.local(path).mkdirs();

            //generate(seed);
            rng.setSeed(seed);
            Gdx.graphics.setContinuousRendering(false);
            for (int i = 0; i < LIMIT; i++) {
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
        FileHandle stills = null;
        if(MANY_STILL){
            stills = Gdx.files.local(path + "/"+name+"_stills/");
            stills.mkdirs();
        }

        StringBuilder sb = new StringBuilder(256 * 12);
        worldTime = System.currentTimeMillis();
        Pixmap temp = new Pixmap(width * cellWidth + 10 << AA, height * cellHeight + 10 << AA, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
        if(world instanceof RotatingGlobeMap) {
            world.setCenterLongitude(0f);
            generate(hash);
        }

        for (int i = 0; i < pm.length; i++) {
            float angle = i / (float)pm.length;
//            if(FLOWING_LAND) {
//                ((Noise.Adapted3DFrom5D)noise).w = TrigTools.cosTurns(angle) * 0.3125f;
//                ((Noise.Adapted3DFrom5D)noise).u = TrigTools.sinTurns(angle) * 0.3125f;
//            }
            world.setCenterLongitude(angle * MathUtils.PI2);
            if(!(world instanceof RotatingGlobeMap)) {
                generate(hash);
            }
            else
                wmv.getBiomeMapper().makeBiomes(world);
            int[][] cm = wmv.show();

            final int bw = temp.getWidth(), bh = temp.getHeight();
            if(SHADOW){
                final int padding = (5<<AA), margin = (1<<AA);
                final double center = (temp.getWidth() - 1) * 0.5, radius2 = (center - margin) * (center - margin),
                        innerRadius2 = (center - padding -margin) * (center - padding - margin);
                temp.setColor(ATMOSPHERE & 0xFFFFFF00);
                temp.fill();
//                temp.setColor(ATMOSPHERE);
//                temp.fillCircle((temp.getWidth() >> 1), (temp.getHeight() >> 1), (temp.getWidth() >> 1) - margin);
                final float iw = 0.9f / bw, ih = 0.9f / bh;
                for (int x = 0; x < bw; x++) {
                    for (int y = 0; y < bh; y++) {
                        int mapColor = x >= padding && x < bw - padding && y >= padding && y < bh - padding
                                ? cm[x - padding][y - padding] : 0;
                        if((mapColor & 0xFF) == 0){
                            double xx = x - center, yy = y - center, distance2 = xx * xx + yy * yy;
                            if(distance2 >= innerRadius2 && distance2 <= radius2)
                                mapColor = ATMOSPHERE;
                            else continue;
                        }
                        float change = Math.min(Math.max(MathTools.barronSpline(Math.min(Math.max(1.1f - (x * iw + y * ih), 0f), 1f), 0.75f, 0.75f) * 1.65f - 1f, -1f), 1f);
                        int adjusted = DescriptiveColorRgb.adjustLightness(mapColor, change);
//                        if(change < -0.5f) System.out.println("LOW WITH ADJUSTED 0x" + Base.BASE16.unsigned(adjusted) + " COLOR 0x" + Base.BASE16.unsigned(mapColor) + " AND CHANGE " + Base.BASE10.decimal(change, 10, 8) + " AT " + x + "," + y + " FOR DISTANCE " + Base.BASE10.decimal(1.1f - (x * iw + y * ih), 10, 8) + " (OR, " + (1.1f - (x * iw + y * ih)) + ")");
                        temp.drawPixel(x, y, adjusted);
                    }
                }
            }
            else {
                for (int x = 0; x < bw; x++) {
                    for (int y = 0; y < bh; y++) {
                        temp.drawPixel(x, y, cm[x][y]);
                    }
                }
            }
            // for whatever reason, drawPixmap will replace existing pixels with transparent ones in the drawn Pixmap.
//            temp.setColor(INK);
//            pm[i].fill();
            pm[i].setFilter(Pixmap.Filter.BiLinear);
            pm[i].setBlending(Pixmap.Blending.None);
            pm[i].drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm[i].getWidth(), pm[i].getHeight());
            if(i % (FRAMES/10) == (FRAMES/10-1)) System.out.print(((i + 1) * 10 / (FRAMES/10)) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }

        if(MANY_STILL){
            for (int i = 0; i < pm.length; i++) {
                png.write(stills.child("world_" + i + ".png"), pm[i]);
            }
        }

        Array<Pixmap> pms = new Array<>(pm);
        writer.palette.analyzeHueWise(pms, 15.0);
        writer.write(Gdx.files.local(path + name + ".gif"), pms, 20);
//        apng.write(Gdx.files.local(path + name + ".png"), pms, 24);
        temp.dispose();

        System.out.println("\nUsing dither: " + writer.getDitherAlgorithm().legibleName + " on " + writer.palette.colorCount + " colors:");

        sb.setLength(0);
        int idx = 0;
        for (;idx < writer.palette.paletteArray.length;) {
            for (int i = 0; i < 8 && idx < writer.palette.paletteArray.length; i++, idx++) {
                Base.BASE16.appendUnsigned(sb.append("0x"), writer.palette.paletteArray[idx]).append(", ");
            }
            sb.setCharAt(sb.length() - 1, '\n');
        }
        System.out.println(sb);
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
        config.setTitle("SquidSquad Demo: Animated World Map Writer");
        config.setWindowedMode(width * cellWidth + 10, height * cellHeight + 10);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new AnimatedWorldMapWriter(), config);
    }
}
