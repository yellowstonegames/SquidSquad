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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.*;
import com.github.tommyettinger.anim8.Dithered.DitherAlgorithm;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.random.DistinctRandom;
import com.github.tommyettinger.digital.Hasher;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.core.DescriptiveColorRgb;
import com.github.yellowstonegames.core.StringTools;
import com.github.yellowstonegames.grid.*;
import com.github.yellowstonegames.text.Language;
import com.github.yellowstonegames.text.Thesaurus;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;

import static com.github.tommyettinger.anim8.Dithered.DitherAlgorithm.*;

/**
 * Writes one or more spinning globes to the out/ folder.
 */
public class AnimatedWorldMapWriter extends ApplicationAdapter {
//    private static final int width = 480, height = 480; // BIG space view
    private static final int width = 240, height = 240; // space view
//    private static final int width = 300, height = 300;
//    private static final int width = 512, height = 512;

    private static final int LIMIT = 5;
    private static final int FRAMES = 240;
    private static final boolean ALIEN_COLORS = true;
    private static final boolean MANY_STILL = false;
    private static final boolean SHADOW = false;
    private int baseSeed = 1234567890;
//    private final int AA = 0;
    private final int AA = 1;

    public float getShadowStrength() {
        return shadowStrength;
    }

    public void setShadowStrength(float shadowStrength) {
        this.shadowStrength = shadowStrength;
    }

    public float getShadowAngle() {
        return shadowAngle;
    }

    public void setShadowAngle(float shadowAngle) {
        this.shadowAngle = shadowAngle;
    }

    private Thesaurus thesaurus;
    /**
     * How much stronger or weaker the shadowing effect should be; this is usually in the -1 to 1 range. The default
     * value is -0.1f, which weakens the shadow and so makes the lit section of the planet slightly larger than the
     * darkened section.
     */
    private float shadowStrength = -0.1f;
    /**
     * The angle, in degrees, for a shadow to be case across the planetary surface. From the center of the world,
     * traveling a positive distance along this angle will get progressively darker, and a negative distance will become
     * lighter. A traditional example is -45 degrees, which is also the default; it casts a shadow from the upper left
     * (lightest) to the lower right (darkest).
     */
    private float shadowAngle = -45f;

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
    private BlendedWorldMapView wmv;
    private AnimatedGif writer;
//    private AnimatedPNG apng;
    private FastPNG png;
    
    private String date, path;
    private INoise noise;
    private static final int SPACE = DescriptiveColorRgb.TRANSPARENT;
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
//        apng = new AnimatedPNG();
//        apng.setFlipY(false);
        if(MANY_STILL){
            png = new FastPNG();
            png.setFlipY(false);
        }

//        for(DitherAlgorithm dither : new DitherAlgorithm[]{GOURD, GRADIENT_NOISE, BLUE_NOISE, NONE,}) {
        for(DitherAlgorithm dither : new DitherAlgorithm[]{MARTEN, }) {
//        for(DitherAlgorithm dither : new DitherAlgorithm[]{NONE, GOURD, BLUE_NOISE, ROBERTS, GRADIENT_NOISE, PATTERN}) {
            writer.setDitherAlgorithm(dither);
            writer.setDitherStrength(0.6f);
            rng = new DistinctRandom(Hasher.balam.hash64(date) + 1L);
            seed = rng.state;
            thesaurus = new Thesaurus(rng);
            //rng.setState(rng.nextLong() + 2000L); // change addend when you need different results on the same date

//            INoise fn = new NoiseWrapper(new FoamNoise(seed), 1.4f, NoiseWrapper.FBM, 1);
//            INoise fn = new NoiseWrapper(new PerlueNoise(seed), 0.8f, NoiseWrapper.FBM, 3);
//        INoise fn = new CyclicNoise(seed, 3, 1.5f);
//        INoise fn = new PuffyNoise(seed, 2, 2.6f);
        INoise fn = new HuskyNoise(seed, 2, 2.6f);

            noise = fn;

            world = new RotatingGlobeMap(seed, width << AA, height << AA, noise, 1.2f);
//        world = new GlobeMap(seed, width << AA, height << AA, noise, 0.75f);

            wmv = new BlendedWorldMapView(world);

            path = "out/worldsAnimated/" + date + "/" + dither.legibleName + "/" +
                    world.getClass().getSimpleName() + noise.getTag() + (SHADOW ? "_shadow/" : "/");

            if (!Gdx.files.local(path).exists())
                Gdx.files.local(path).mkdirs();

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
        world.rng.setSeed(seed);
        if(ALIEN_COLORS) {
            int liquidLevel = world.rng.nextInt(6);

            wmv.biomeMapper.initialize(AlienBiomes.generateAlienBiomeTable(world.rng, Language.HLETKIP, liquidLevel,
                    DescriptiveColor.oklabByHSL(world.rng.nextFloat(), world.rng.nextFloat(0.3f, 0.9f), world.rng.nextFloat(0.3f, 0.7f), 1f),
                    DescriptiveColor.oklabByHSL(world.rng.nextFloat(), world.rng.nextFloat(0.0f, 0.6f), world.rng.nextFloat(0.1f, 0.7f), 1f),
                    DescriptiveColor.oklabByHSL(world.rng.nextFloat(), world.rng.nextFloat(0.6f, 1.0f), world.rng.nextFloat(0.4f, 0.8f), 1f)
            ));
            world.seedA = world.rng.getStateA();
            world.seedB = world.rng.getStateB();
            wmv.generate(1.5f - liquidLevel * 0.125f, 1.35f);
        } else {
            world.seedA = world.rng.getStateA();
            world.seedB = world.rng.getStateB();
            wmv.generate(1f, 1.25f);
        }
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

        worldTime = System.currentTimeMillis();
        Pixmap temp;
        if(SHADOW) temp = new Pixmap(width * cellWidth + 10 << AA, height * cellHeight + 10 << AA, Pixmap.Format.RGBA8888);
        else temp = new Pixmap(width * cellWidth << AA, height * cellHeight << AA, Pixmap.Format.RGBA8888);
        temp.setFilter(Pixmap.Filter.BiLinear);
        if(world instanceof RotatingGlobeMap) {
            world.setCenterLongitude(0f);
            generate(hash);
        }

        for (int i = 0; i < pm.length; i++) {
            float angle = i / (float)pm.length;
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
                final float center = (temp.getWidth() - 1) * 0.5f,
                        rim2 = center * center + 4,
                        radius2 = (center - margin) * (center - margin),
                        innerRadius2 = (center - padding - margin) * (center - padding - margin);
                temp.setColor(ATMOSPHERE & 0xFFFFFF00);
                temp.fill();

                shadowAngle = -45 + TrigTools.sinSmootherTurns(angle) * 180f;
                shadowStrength = TrigTools.cosSmootherTurns(angle) * 0.4f;

                final float iw = 1.5f * TrigTools.cosSmootherDeg(shadowAngle) / bw,
                        ih = -1.5f * TrigTools.sinSmootherDeg(shadowAngle) / bh;
                for (int x = 0; x < bw; x++) {
                    for (int y = 0; y < bh; y++) {
                        int mapColor = x >= padding && x < bw - padding && y >= padding && y < bh - padding
                                ? cm[x - padding][y - padding] : 0;
                        float xx = x - center, yy = y - center;
                        if((mapColor & 0xFF) == 0){
                            double distance2 = xx * xx + yy * yy;
                            if(distance2 >= innerRadius2 && distance2 <= rim2) {
                                mapColor = (distance2 <= radius2) ? ATMOSPHERE : ATMOSPHERE & 0xFFFFFF00;
                            }
                            else continue;
                        }
                        float change = Math.min(Math.max(MathTools.barronSpline(Math.min(Math.max(-shadowStrength - (xx * iw + yy * ih), 0f), 1f), 0.75f, 0.75f) * 1.65f - 1f, -1f), 1f);
                        int adjusted = DescriptiveColorRgb.adjustLightness(mapColor, change);
                        adjusted |= adjusted >>> 7 & 1;
                        // debug to find any non-opaque planet pixels
//                        if((adjusted & 0xFF) != 0xFF)
//                            adjusted = DescriptiveColorRgb.RED;
                        temp.drawPixel(x, y, adjusted);
                    }
                }
            }
            else {
                for (int x = 0; x < bw; x++) {
                    for (int y = 0; y < bh; y++) {
                        int mapColor = cm[x][y];
                        temp.drawPixel(x, y, mapColor | (mapColor >>> 7 & 1));
                    }
                }
            }
            // for whatever reason, drawPixmap will replace existing pixels with transparent ones in the drawn Pixmap.
//            pm[i].setColor(INK);
//            pm[i].fill();
            pm[i].setFilter(Pixmap.Filter.BiLinear);
            pm[i].setBlending(Pixmap.Blending.SourceOver);
            pm[i].drawPixmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), 0, 0, pm[i].getWidth(), pm[i].getHeight());
            ByteBuffer buf = pm[i].getPixels();
            for (int pos = 3, lim = buf.limit(); pos < lim; pos += 4) {
                int alpha = ~(buf.get(pos) & 0xFF) + 250 >> 31;
                buf.put(pos, (byte)alpha);
            }
            if(i % (FRAMES/10) == (FRAMES/10-1)) System.out.print(((i + 1) * 10 / (FRAMES/10)) + "% (" + (System.currentTimeMillis() - worldTime) + " ms)... ");
        }

        if(MANY_STILL){
            for (int i = 0; i < pm.length; i++) {
                png.write(stills.child("world_" + i + ".png"), pm[i]);
            }
        }

        Array<Pixmap> pms = new Array<>(pm);
        writer.palette.analyze(pms);
        writer.write(Gdx.files.local(path + name + ".gif"), pms, 20);
//        apng.write(Gdx.files.local(path + name + "_apng.png"), pms, 20);
        temp.dispose();

        System.out.println("\nUsing dither: " + writer.getDitherAlgorithm().legibleName + " on " + writer.palette.colorCount + " colors:");

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
