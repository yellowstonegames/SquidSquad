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
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.*;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.place.Biome;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.text.DateFormat;
import java.util.Date;

import static com.github.tommyettinger.digital.BitConversion.longBitsToDouble;

/**
 * Writes one or more spinning globes to the out/ folder.
 * <br>
 * This has different behavior from the old FlowingWorldMapWriter, before the switch to using an INoise that gets passed
 * to NoiseWrapper s in the world map code. For reference purposes, the reason old globes look more "agitated" is
 * because they affected the two "change coordinates" in dimensions w and u like the spatial coordinates in x, y, and z;
 * that is, when spatial coordinates had high detail, they also changed very aggressively with small-scale shifts. Here,
 * w and u are not treated like x, y, and z, which took some effort to finally notice. Because the NoiseWrapper handles
 * the change to its coordinates, and NoiseWrapper only knows about x, y, and z, the w and u values that our custom
 * INoise introduces aren't visible to the NoiseWrapper. This ends up looking a lot better, so I'm not complaining, but
 * it is something to be aware of.
 */
public class FlowingWorldMapWriter extends ApplicationAdapter {

    // generating a 256x256 gif and apng, 240 frames, foam noise, blended world map:
    // World #1, LimeTobacco, completed in         242466 ms
    // same as above, but with a detailed world map:
    // World #1, BrownsapAlder, completed in       241347 ms
    // with a detailed world map and Perlin noise:
    // World #1, TuftedBudEucalyptus, completed in  57233 ms
    // with blended world map and Cyclic noise (3 octaves, 2f frequency):
    // World #1, GrayLarch, completed in           111201 ms


    private static final int width = 256, height = 256;
//    private static final int width = 300, height = 300;

    private static final int FRAMES = 240;
    private static final int LIMIT = 3;
    private static final float SPEED = 0.35f;
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
//    private AnimatedPNG apng;
//    private PixmapIO.PNG pngWriter;

    private String date, path;
    private static final Color INK = new Color(DescriptiveColor.toRGBA8888(Biome.TABLE[60].colorOklab));

    public static class Noise3DFrom5D implements INoise {
        public INoise noise;
        public float c, s;
        public Noise3DFrom5D(){
            noise = new Noise();
        }

        public Noise3DFrom5D(INoise iNoise) {
            this.noise = iNoise;
        }

        @Override
        public long getSeed() {
            return noise.getSeed();
        }

        public void setSeed(int seed) {
            noise.setSeed(seed);
        }

        @Override
        public boolean hasEfficientSetSeed() {
            return noise.hasEfficientSetSeed();
        }

        @Override
        public int getMinDimension() {
            return 3;
        }

        @Override
        public int getMaxDimension() {
            return 3;
        }

        @Override
        public void setSeed(long seed) {
            noise.setSeed(seed);
        }

        @Override
        public float getNoise(float x, float y) {
            return noise.getNoise(x, y);
        }

        @Override
        public float getNoise(float x, float y, float z) {
            return noise.getNoise(x, y, z, s, c);
        }

        @Override
        public float getNoise(float x, float y, float z, float w) {
            return noise.getNoise(x, y, z, w);
        }

        @Override
        public float getNoise(float x, float y, float z, float w, float u) {
            return noise.getNoise(x, y, z, w, u);
        }

        @Override
        public float getNoise(float x, float y, float z, float w, float u, float v) {
            return noise.getNoise(x, y, z, w, u, v);
        }

        public float getNoiseWithSeed(float x, float y, int seed) {
            return noise.getNoiseWithSeed(x, y, seed);
        }

        public float getNoiseWithSeed(float x, float y, float z, int seed) {
            return noise.getNoiseWithSeed(x, y, z, s, c, seed);
        }

        public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
            return noise.getNoiseWithSeed(x, y, z, w, seed);
        }

        public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
            return noise.getNoiseWithSeed(x, y, z, w, u, seed);
        }

        public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
            return noise.getNoiseWithSeed(x, y, z, w, u, v, seed);
        }

        @Override
        public float getNoiseWithSeed(float x, float y, long seed) {
            return noise.getNoiseWithSeed(x, y, seed);
        }

        @Override
        public float getNoiseWithSeed(float x, float y, float z, long seed) {
            return noise.getNoiseWithSeed(x, y, z, s, c, seed);
        }

        @Override
        public float getNoiseWithSeed(float x, float y, float z, float w, long seed) {
            return noise.getNoiseWithSeed(x, y, z, w, seed);
        }

        @Override
        public float getNoiseWithSeed(float x, float y, float z, float w, float u, long seed) {
            return noise.getNoiseWithSeed(x, y, z, w, u, seed);
        }

        @Override
        public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, long seed) {
            return noise.getNoiseWithSeed(x, y, z, w, u, v, seed);
        }
    }

    public Noise3DFrom5D iNoise;
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
//        path = "out/worldsAnimated/" + date + "/FlowingSorbet/";
        path = "out/worldsAnimated/" + date + "/FlowingCyclic/";
//        path = "out/worldsAnimated/" + date + "/FlowingSimplex/";
//        path = "out/worldsAnimated/" + date + "/FlowingSimplexCentral/";
//        path = "out/worldsAnimated/" + date + "/FlowingSimplexOuter/";
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
        writer.setDitherAlgorithm(Dithered.DitherAlgorithm.WREN);
        writer.setDitherStrength(0.6f);
        writer.setFlipY(false);
//        apng = new AnimatedPNG();
//        apng.setFlipY(false);
//        apng.setCompression(7);
//        pngWriter = new PixmapIO.PNG();
//        pngWriter.setFlipY(false);
        rng = new DistinctRandom(Hasher.balam.hash64(date));
//        rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date
        seed = rng.getSelectedState(0);

        thesaurus = new Thesaurus(rng);

//        Noise fn = new Noise((int) seed, 3.5f, Noise.TAFFY_FRACTAL, 1); // between 69783ms and 72929ms
//        Noise fn = new Noise((int) seed, 1f, Noise.FOAM_FRACTAL, 1);    // between 130930ms and 131995ms
//        Noise fn = new Noise((int) seed, 0.75f, Noise.SIMPLEX_FRACTAL, 2);   // between 34428ms and 38706ms
//        Noise fn = new Noise((int) seed, 1.5f, Noise.VALUE_FRACTAL, 3, 2.6f, 1f/2.6f);
//        Noise fn = new Noise((int) seed, 1.4f, Noise.PERLIN_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1f, Noise.HONEY_FRACTAL, 1);
//        Noise fn = new Noise((int) seed, 1f, Noise.HONEY_FRACTAL, 1, 3f, 1f/3f);
//        Noise fn = new Noise((int) seed, 1f, Noise.PERLIN_FRACTAL, 1);  // between 35894ms and 42264ms

//        fn.setInterpolation(Noise.HERMITE); // the default

//        INoise fn = new CyclicNoise(seed, 3, 2.3f);
        INoise fn = new NoiseWrapper(new SorbetNoise(seed, 3, 1.4f), seed, 1.2f, NoiseWrapper.EXO, 2, false);
        iNoise = new Noise3DFrom5D(fn);
//        iNoise = new Noise3DFrom5D(new SimplexNoise(seed)); // between 33709ms and 45305ms
//        iNoise = new Noise3DFrom5D(new ValueNoise(seed)); // between  and
//        iNoise = new Noise3DFrom5D(new FlanNoise(seed, 5)); // between 53757ms and 59479ms
//        iNoise = new Noise3DFrom5D(new CyclicNoise(seed, 4, 2f)); // between 53757ms and 59479ms
//        iNoise = new Noise3DFrom5D(new HighDimensionalValueNoise(seed, 5)); // between 69009ms and 94373ms
//        iNoise = new Noise3DFrom5D(new Noise((int) seed, 1f, Noise.FOAM, 1)); // between 126331ms and 128884ms
//        iNoise = new Noise3DFrom5D(new NoiseAdjustment(new Noise((int) seed, 0.75f, Noise.SIMPLEX_FRACTAL, 2),
//                f -> INoise.noiseSpline(f, 1.2f, 0f))); // between ms and ms
//                f -> (float)(Math.pow(2f, f) - 1.25f) * (4f/3f))); // between ms and ms
//        iNoise = new Noise3DFrom5D(new Noise((int) seed, 1f, Noise.SIMPLEX, 1)); // between 31682ms and 36851ms


        world = new GlobeMap(seed, width, height, iNoise, 0.45f);


        wmv = new BlendedWorldMapView(world);
//        wmv = new DetailedWorldMapView(world);

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
        world.rng.setSeed(seed);
        world.seedA = world.rng.getStateA();
        world.seedB = world.rng.getStateB();
        wmv.generate(
                (float) (0.98 + formCurvedDouble(world.seedA * 0x123456789ABCDEFL ^ world.seedB) * 0.1875),
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
//        try {
        for (int i = 0; i < FRAMES; i++) {
            float angle = i / (float) FRAMES;
            iNoise.s = TrigTools.sinTurns(angle) * SPEED;// 0.4f;// 0.3125f;
            iNoise.c = TrigTools.cosTurns(angle) * SPEED;// 0.4f;// 0.3125f;

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
            if(FRAMES >= 10)
                if(i % (FRAMES / 10) == (FRAMES / 10) - 1) System.out.print(((i + 1) * 100 / FRAMES) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");

//                if (i % 18 == 17)
//                    System.out.print(((i + 1) * 10 / 18) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }
        Array<Pixmap> pms = new Array<>(pm);
        writer.palette = new QualityPalette(pms);
        writer.write(Gdx.files.local(path + name + ".gif"), pms, 16);
//        apng.write(Gdx.files.local(path + name + ".png"), pms, 16);
//            writer.write(Gdx.files.local(path + name + ".png"), pms, 20);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        System.out.println();
        System.out.println("World #" + counter + ", " + name + ", completed in " + (System.currentTimeMillis() - worldTime) + " ms");
    }
    @Override
    public void render() {
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
