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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.anim8.AnimatedGif;
import com.github.tommyettinger.anim8.Dithered;
import com.github.tommyettinger.anim8.PaletteReducer;
import com.github.tommyettinger.digital.Hasher;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.TrigTools;
import com.github.tommyettinger.ds.IntList;
import com.github.yellowstonegames.core.ColorGradients;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.yellowstonegames.grid.*;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_POINTS;

/**
 */
public class NoiseVisualizer extends ApplicationAdapter {

    private int dim = 1; // this can be 0, 1, 2, 3, or 4; add 2 to get the actual dimensions
    private int octaves = 2;
    private float freq = 0x1p-4f;
    private boolean inverse;
    private Noise noise = new Noise(1, freq, Noise.CUBIC_FRACTAL, octaves);
    private ImmediateModeRenderer20 renderer;
    
    private LongPointHash ph = new LongPointHash();
    private IntPointHash iph = new IntPointHash();
    private FlawedPointHash.RugHash rug = new FlawedPointHash.RugHash(1);
    private FlawedPointHash.QuiltHash quilt = new FlawedPointHash.QuiltHash(1, 16);
    private FlawedPointHash.CubeHash cube = new FlawedPointHash.CubeHash(1, 16);
    private FlawedPointHash.SquishedCubeHash squish = new FlawedPointHash.SquishedCubeHash(1, 16);
    private FlawedPointHash.FNVHash fnv = new FlawedPointHash.FNVHash(1);
    private FlawedPointHash.LowLeaningHash low = new FlawedPointHash.LowLeaningHash(123);
    private IPointHash[] pointHashes = new IPointHash[] {ph, iph, fnv, rug, quilt, cube, squish, low};
    private int hashIndex = 5; // 6 for squish, best for looping square gifs

    private static final int width = 256, height = 256;
    private static final float iWidth = 1f/width, iHeight = 1f/height;

    private InputAdapter input;
    
    private Viewport view;
    private int ctr = -256;
    private boolean keepGoing = true;
    
    private AnimatedGif gif;
    private Array<Pixmap> frames = new Array<>(256);

    public static float basicPrepare(float n)
    {
//        return Math.max(0f, n);
        return n * 0.5f + 0.5f;
    }

    public static float circleInPrepare(float n)
    {
//        return Math.max(0f, n);
        return Interpolations.circleIn.apply(n * 0.5f + 0.5f);
    }

    @Override
    public void create() {
        renderer = new ImmediateModeRenderer20(width * height, false, true, 0);
        view = new ScreenViewport();
        noise.setPointHash(pointHashes[hashIndex]);
        noise.setFractalType(Noise.RIDGED_MULTI);
        noise.setInterpolation(Noise.QUINTIC);
//        int[] palette = new int[]
//                {
//                0x000000FF, 0x120e14FF, 0x312b30FF, 0x504347FF, 0x726361FF, 0x9a877eFF, 0xc2b1a9FF, 0xe2d3cfFF,
//                0xc9ada3FF, 0xa87d7bFF, 0x84555eFF, 0x5f3a49FF, 0x422639FF, 0x251427FF, 0x421f3fFF, 0x633257FF,
//                0x824b6cFF, 0xb06b87FF, 0xd1949cFF, 0xeec2bfFF, 0xe09b98FF, 0xca737bFF, 0x9f5366FF, 0x763d4eFF,
//                0x562137FF, 0x361031FF, 0x54234aFF, 0x7d3564FF, 0xa94e80FF, 0xca719fFF, 0xe197c6FF, 0xf8bcedFF,
//                0xea8cf5FF, 0xc25dd4FF, 0x9337b8FF, 0x6f278aFF, 0x4f1661FF, 0x2e0931FF, 0x541359FF, 0x7e1f89FF,
//                0xb526b1FF, 0xdb4ad0FF, 0xf578d3FF, 0xfbaccdFF, 0xff7dc1FF, 0xf248aaFF, 0xc52589FF, 0x8a0f62FF,
//                0x570f4aFF, 0x330b2fFF, 0x571b44FF, 0x8a2457FF, 0xbd2f68FF, 0xff6385FF, 0xff9999FF, 0xf0c5b3FF,
//                0xf29780FF, 0xf06260FF, 0xcc2f41FF, 0x962037FF, 0x65142bFF, 0x3a0c1bFF, 0x650e18FF, 0x951f1cFF,
//                0xc43226FF, 0xe25941FF, 0xff8d70FF, 0xffbf99FF, 0xe3906fFF, 0xb5695bFF, 0x8e4b42FF, 0x6d3232FF,
//                0x4a1f1eFF, 0x2b1210FF, 0x48281aFF, 0x64422fFF, 0x876141FF, 0xae8659FF, 0xd1ab78FF, 0xf0d399FF,
//                0xe1b16aFF, 0xcc7f3fFF, 0xab572cFF, 0x8f3622FF, 0x66281cFF, 0x3d1b14FF, 0x612514FF, 0x8c3e19FF,
//                0xb55c21FF, 0xe1882fFF, 0xffbb55FF, 0xffe488FF, 0xe1c266FF, 0xb9954aFF, 0x997333FF, 0x7b5122FF,
//                0x5a3714FF, 0x3d1f0cFF, 0x6a2b09FF, 0x96420aFF, 0xc1660bFF, 0xe99507FF, 0xf9c72fFF, 0xfaef6bFF,
//                0xe8bd3eFF, 0xce922dFF, 0xaa6928FF, 0x82491eFF, 0x592f15FF, 0x352113FF, 0x4e3823FF, 0x695534FF,
//                0x867148FF, 0xaa9660FF, 0xcfc078FF, 0xe1e0a4FF, 0xc2c187FF, 0x9b9662FF, 0x7b7448FF, 0x5e5637FF,
//                0x433c25FF, 0x2e2714FF, 0x453a11FF, 0x6b581aFF, 0x8f7e21FF, 0xb3a72eFF, 0xd2d957FF, 0xebff8eFF,
//                0xc4e053FF, 0x91b82eFF, 0x689425FF, 0x49731fFF, 0x2d4a18FF, 0x172b10FF, 0x274220FF, 0x3b6332FF,
//                0x568042FF, 0x7ca65dFF, 0xa6cf78FF, 0xd2f4a4FF, 0xa1e673FF, 0x55d629FF, 0x2eab27FF, 0x1b7d1bFF,
//                0x0f4d1fFF, 0x0a2b1cFF, 0x114533FF, 0x186b47FF, 0x259451FF, 0x32c962FF, 0x75ff88FF, 0xbeffaeFF,
//                0x87c787FF, 0x62a16fFF, 0x3f7552FF, 0x2f5443FF, 0x1b3630FF, 0x0d1f24FF, 0x11373dFF, 0x175c5bFF,
//                0x218a73FF, 0x2dbf94FF, 0x62f9b9FF, 0xb6fedcFF, 0x65f1d6FF, 0x12d1c8FF, 0x1693a0FF, 0x135e71FF,
//                0x09394fFF, 0x071b25FF, 0x203e46FF, 0x305f69FF, 0x4a848aFF, 0x6ab0adFF, 0x8cdbcbFF, 0xbaf3eaFF,
//                0x82dce3FF, 0x51b0d5FF, 0x2784b6FF, 0x245699FF, 0x17326fFF, 0x101633FF, 0x202e40FF, 0x354b59FF,
//                0x496975FF, 0x6a969eFF, 0x93c6caFF, 0xc7ebf3FF, 0xa1cbffFF, 0x7094ffFF, 0x4f62bdFF, 0x3c3c99FF,
//                0x28255cFF, 0x190f29FF, 0x262342FF, 0x3c3b66FF, 0x585b8aFF, 0x767eb8FF, 0xa6a2dbFF, 0xd9caffFF,
//                0xab99cfFF, 0x8472adFF, 0x5c4f7dFF, 0x3f385eFF, 0x2a2247FF, 0x180e29FF, 0x381b58FF, 0x59288aFF,
//                0x7c35bdFF, 0xa55be8FF, 0xcd88f2FF, 0xe4b7eeFF, 0xbf95b3FF, 0x9e7498FF, 0x785780FF, 0x584066FF,
//                0x3e2c4bFF, 0x23172dFF, 0x362f3cFF, 0x504758FF, 0x6d6778FF, 0x938b9eFF, 0xbdb5c8FF, 0xffffffFF,
//                0xf0ebf7FF, 0xd2ced9FF, 0xb6b8c7FF, 0x868d9bFF, 0x626773FF, 0x414752FF, 0x292f38FF, 0x13161cFF,
//                0x272f30FF, 0x394646FF, 0x576863FF, 0x788d82FF, 0xa6b9aaFF, 0xcce1bbFF, 0xa7c296FF, 0x829c72FF,
//                0x5c7759FF, 0x405741FF, 0x253a2dFF, 0x121f17FF,
//        };
        int[] gray256 = new int[256];
        for (int i = 0; i < 256; i++) {
            gray256[i] = i * 0x010101 << 8 | 0xFF;
        }

        gif = new AnimatedGif();
        gif.setDitherAlgorithm(Dithered.DitherAlgorithm.WREN);
        gif.setDitherStrength(0.2f);
        gif.palette = new PaletteReducer(gray256);

//                0x00000000, 0x000000FF, 0x081820FF, 0x132C2DFF, 0x1E403BFF, 0x295447FF, 0x346856FF, 0x497E5BFF,
//                0x5E9463FF, 0x73AA69FF, 0x88C070FF, 0x9ECE88FF, 0xB4DCA0FF, 0xCAEAB8FF, 0xE0F8D0FF, 0xEFFBE7FF,
//                0xFFFFFFFF,

//                0x000000ff, 0x000033ff, 0x000066ff, 0x000099ff, 0x0000ccff, 0x0000ffff, 0x003300ff,
//                0x003333ff, 0x003366ff, 0x003399ff, 0x0033ccff, 0x0033ffff, 0x006600ff, 0x006633ff, 0x006666ff,
//                0x006699ff, 0x0066ccff, 0x0066ffff, 0x009900ff, 0x009933ff, 0x009966ff, 0x009999ff, 0x0099ccff,
//                0x0099ffff, 0x00cc00ff, 0x00cc33ff, 0x00cc66ff, 0x00cc99ff, 0x00ccccff, 0x00ccffff, 0x00ff00ff,
//                0x00ff33ff, 0x00ff66ff, 0x00ff99ff, 0x00ffccff, 0x00ffffff, 0x330000ff, 0x330033ff, 0x330066ff,
//                0x330099ff, 0x3300ccff, 0x3300ffff, 0x333300ff, 0x333333ff, 0x333366ff, 0x333399ff, 0x3333ccff,
//                0x3333ffff, 0x336600ff, 0x336633ff, 0x336666ff, 0x336699ff, 0x3366ccff, 0x3366ffff, 0x339900ff,
//                0x339933ff, 0x339966ff, 0x339999ff, 0x3399ccff, 0x3399ffff, 0x33cc00ff, 0x33cc33ff, 0x33cc66ff,
//                0x33cc99ff, 0x33ccccff, 0x33ccffff, 0x33ff00ff, 0x33ff33ff, 0x33ff66ff, 0x33ff99ff, 0x33ffccff,
//                0x33ffffff, 0x660000ff, 0x660033ff, 0x660066ff, 0x660099ff, 0x6600ccff, 0x6600ffff, 0x663300ff,
//                0x663333ff, 0x663366ff, 0x663399ff, 0x6633ccff, 0x6633ffff, 0x666600ff, 0x666633ff, 0x666666ff,
//                0x666699ff, 0x6666ccff, 0x6666ffff, 0x669900ff, 0x669933ff, 0x669966ff, 0x669999ff, 0x6699ccff,
//                0x6699ffff, 0x66cc00ff, 0x66cc33ff, 0x66cc66ff, 0x66cc99ff, 0x66ccccff, 0x66ccffff, 0x66ff00ff,
//                0x66ff33ff, 0x66ff66ff, 0x66ff99ff, 0x66ffccff, 0x66ffffff, 0x990000ff, 0x990033ff, 0x990066ff,
//                0x990099ff, 0x9900ccff, 0x9900ffff, 0x993300ff, 0x993333ff, 0x993366ff, 0x993399ff, 0x9933ccff,
//                0x9933ffff, 0x996600ff, 0x996633ff, 0x996666ff, 0x996699ff, 0x9966ccff, 0x9966ffff, 0x999900ff,
//                0x999933ff, 0x999966ff, 0x999999ff, 0x9999ccff, 0x9999ffff, 0x99cc00ff, 0x99cc33ff, 0x99cc66ff,
//                0x99cc99ff, 0x99ccccff, 0x99ccffff, 0x99ff00ff, 0x99ff33ff, 0x99ff66ff, 0x99ff99ff, 0x99ffccff,
//                0x99ffffff, 0xcc0000ff, 0xcc0033ff, 0xcc0066ff, 0xcc0099ff, 0xcc00ccff, 0xcc00ffff, 0xcc3300ff,
//                0xcc3333ff, 0xcc3366ff, 0xcc3399ff, 0xcc33ccff, 0xcc33ffff, 0xcc6600ff, 0xcc6633ff, 0xcc6666ff,
//                0xcc6699ff, 0xcc66ccff, 0xcc66ffff, 0xcc9900ff, 0xcc9933ff, 0xcc9966ff, 0xcc9999ff, 0xcc99ccff,
//                0xcc99ffff, 0xcccc00ff, 0xcccc33ff, 0xcccc66ff, 0xcccc99ff, 0xccccccff, 0xccccffff, 0xccff00ff,
//                0xccff33ff, 0xccff66ff, 0xccff99ff, 0xccffccff, 0xccffffff, 0xff0000ff, 0xff0033ff, 0xff0066ff,
//                0xff0099ff, 0xff00ccff, 0xff00ffff, 0xff3300ff, 0xff3333ff, 0xff3366ff, 0xff3399ff, 0xff33ccff,
//                0xff33ffff, 0xff6600ff, 0xff6633ff, 0xff6666ff, 0xff6699ff, 0xff66ccff, 0xff66ffff, 0xff9900ff,
//                0xff9933ff, 0xff9966ff, 0xff9999ff, 0xff99ccff, 0xff99ffff, 0xffcc00ff, 0xffcc33ff, 0xffcc66ff,
//                0xffcc99ff, 0xffccccff, 0xffccffff, 0xffff00ff, 0xffff33ff, 0xffff66ff, 0xffff99ff, 0xffffccff,
//                0xffffffff, 
        input = new InputAdapter(){
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case BACKSLASH:
                        noise.stringDeserialize(Gdx.app.getClipboard().getContents());
                        break;
                    case W:
                        if((noise.getNoiseType() & -2) == Noise.CUBIC) noise.setFrequency(0x1p-4f);
//                        for (int c = 0; c < 32; c++) {
//                        for (int c = 0; c < 128; c++) {
                        for (int c = 0; c < 256; c++) {
                            int w = 256, h = 256;
                            float halfW = (w-1) * 0.5f, halfH = (h-1) * 0.5f, inv = 1f / w;
                            Pixmap p = new Pixmap(w, h, Pixmap.Format.RGBA8888);
                            for (int x = 0; x < w; x++) {
                                for (int y = 0; y < h; y++) {
//                                    float color = basicPrepare(noise.getConfiguredNoise(x, y, c));
                                    float color = circleInPrepare(
                                            noise.getConfiguredNoise(
                                            x, y, c - inv * ((x - halfW) * (x - halfW) + (y - halfH) * (y - halfH)))
                                    );
//                                            * 0.5f + 0.25f + TrigTools.sinTurns(c * 0x1p-7f) * 0.25f;
//                                    color = color * 0x0.FFp0f + 0x1p-8f;
                                    p.setColor(color, color, color, 1f);
                                    p.drawPixel(x, y);
                                }
                            }
                            frames.add(p);
                        }
                        Gdx.files.local("out/").mkdirs();

//                            float hue = 0.94f; // pink
//                            float hue = 0.2f; // apricot
//                            float hue = 0.11f; // fire
//                            float hue = 0.625f; // a soft blue
//                            float hue = 0.45f; // bright green
//                            float hue = 0.425f; // Brazil green
//                            float hue = 0.08f; // bright red

                            float hueBase = 0.675f; // a slightly violet-ish blue
//                            float hueHigh = 0.15f; // apricot
//                            float hueBase = 0.11f; // embers
//                            float hueHigh = 0.2f; // orange glow
//                            float hueBase = 0.575f; // blue-cyan
//                            float hueHigh = 0.45f; // bright green
//                            float hueBase = 0.45f; // bright green
                            float hueHigh = 0.425f; // Brazil green
//                            float hueHigh = 0.27f; // gold


                        IntList g = ColorGradients.toRGBA8888(ColorGradients.appendGradientChain(new IntList(256), 256, Interpolations.smooth,
//                                  DescriptiveColor.oklabByHSL(0.375f, 0.6f, 0.2f, 1f)
//                                  DescriptiveColor.oklabByHSL(0.7f, 0.6f, 0.7f, 1f)
//                                  DescriptiveColor.oklabByHSL(0.85f, 0.9f, 0.2f, 1f)

//                                  DescriptiveColor.oklabByHSL(0.85f, 0.9f, 0.4f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.99f, 0.9f, 0.55f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.8f, 1f, 0.99f, 1f)

//                                , DescriptiveColor.oklabByHSL(0.1f, 0.85f, 0.95f, 1f)

                                DescriptiveColor.oklabByHSL(0.4f, 0.8f, 0.05f, 1f),
                                DescriptiveColor.oklabByHSL(0.42f, 0.8f, 0.16f, 1f),
                                DescriptiveColor.oklabByHSL(0.44f, 1f, 0.6f, 1f),
                                DescriptiveColor.oklabByHSL(0.41f, 0.75f, 0.75f, 1f)

//                                DescriptiveColor.oklabByHSL(0.94f, 0.8f, 0.05f, 1f),
//                                DescriptiveColor.oklabByHSL(0.97f, 0.8f, 0.2f, 1f),
//                                DescriptiveColor.oklabByHSL(0.04f, 1f, 0.65f, 1f),
//                                DescriptiveColor.oklabByHSL(0.2f, 0.75f, 0.9f, 1f)
//
//                                DescriptiveColor.oklabByHSL(0.14f, 0.9f, 0.3f, 1f),
//                                DescriptiveColor.oklabByHSL(0.07f, 0.65f, 0.03f, 1f),
//                                DescriptiveColor.oklabByHSL(0.1f, 1.0f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.05f, 0.85f, 0.8f, 1f)

//                                DescriptiveColor.oklabByHSL(0.52f, 0.9f, 0.35f, 1f),
//                                DescriptiveColor.oklabByHSL(0.47f, 0.8f, 0.0f, 1f),
//                                DescriptiveColor.oklabByHSL(0.54f, 0.9f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.4f, 0.85f, 0.8f, 1f)
//
//                                DescriptiveColor.oklabByHSL(0.02f, 0.9f, 0.35f, 1f),
//                                DescriptiveColor.oklabByHSL(0.97f, 0.8f, 0.0f, 1f),
//                                DescriptiveColor.oklabByHSL(0.04f, 0.9f, 0.5f, 1f),
//                                DescriptiveColor.oklabByHSL(0.1f, 0.85f, 0.8f, 1f)

//                                  DescriptiveColor.oklabByHSL(0.65f, 1f, 0.3f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.475f, 0.75f, 0.65f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.7f, 0.85f, 0.87f, 1f)
//                                , DescriptiveColor.oklabByHSL(0.5f, 0.9f, 0.72f, 1f)

                        ));
                        g.toArray(gif.palette.paletteArray);
//                        for (int i = 0; i < 256; i++) {
//                            int hiLo = Math.round(
////                                    MathTools.square(
//                                            (MathTools.swayTight(i * 0x1p-6f + 0.5f))
////                                    )
//                            );
//                            gif.palette.paletteArray[i]
////                            &= 0xFF0000FF;
//                                    = DescriptiveColor.toRGBA8888(
////                                            DescriptiveColor.oklabByHSL((i + (noise.getSeed() & 255)) * 0x2p-8f, 0.75f, 0.6f, 1f)
//                                            DescriptiveColor.oklabByHSL(hueBase, 1f, i * 0x3.7p-10f, 1f)
//                            );

//                                    DescriptiveColor.oklabByHSL(
//                                            hiLo * (hueHigh - hueBase + 1f) + hueBase, // hueBase to hueHigh
////                                            hiLo * 0.19f + hue, // red to gold
////                                            (i & 255) * 0x1p-12f - 0x1p-5f + hue, // small hue variation
////                                            (i & 255) * 0x1p-11f - 0x3p-7f + hue, // smallish hue variation
////                                            (i & 127) * 0x1p-7f + hue, // widest hue variation
////                                            (i + 90 & 255) * 0x1p-9f + 0.9f,
////                                            (i + 90 & 255) * 0x3p-10f + 0.125f,
////                                            (i + 90 & 255) * 0x3p-10f + 0.2f,
////                                            1f - (i + 90 & 255) * 0x1p-11f,
////                                            1f - (i + 90 & 255) * 0x1p-13f,
////                                            1f - (i & 255) * 0x1p-13f,
//                                            1f,
////                                            hiLo == 1 && i < 160 ? 0.5f + (i * 0x1p-9f) :
////                                            hiLo == 1 && i < 160 ? 0.6f + (i * 0x1p-9f) :
//
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.2f, 0.35f) * 0.75f, // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.2f, 0.35f) * 0.7f + 0.125f, // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 0.4f, 0.4f), // biased
////                                            MathTools.barronSpline(i * 0x1p-8f, 1.5f, 0.7f) * 0.85f + 0.05f, // biased
//                                            MathTools.square(MathTools.barronSpline(i * 0x1p-8f, 0.8f, 0.6f) - 0.3f) * 1.95f, // biased
////                                            (i * i * 0x1p-16f), //very dark
////                                            0.6f + TrigTools.cosTurns(i * 0x1p-9f) * 0.3f, // light, from 0.3f to 0.9f
////                                            0.55f + TrigTools.cosTurns(i * 0x1p-9f) * -0.35f, // light, from 0.2f to 0.9f
////                                            0.65f + TrigTools.cosTurns(i * 0x1p-10f) * 0.2f, // very light, from 0.65f to 0.85f, more are high
//                                            1f));
//                            gif.palette.paletteArray[i] = DescriptiveColor.toRGBA8888(DescriptiveColor.oklabByHSL((i + 100 & 255) * 0x1p-8f, 1f, i * 0x1p-10f + 0.5f, 1f));
//                        }
                        String ser = noise.stringSerialize() + "_" + System.currentTimeMillis();
                        System.out.println(ser);
//                        gif.write(Gdx.files.local("out/cube" + System.currentTimeMillis() + ".gif"), frames, 16);
                        gif.write(Gdx.files.local("out/" + ser + ".gif"), frames, 16);
                        for (int i = 0; i < frames.size; i++) {
                            frames.get(i).dispose();
                        }
                        frames.clear();
                        break;
                    case P: //pause
                        keepGoing = !keepGoing;
                    case SPACE:
                        ctr++;
                        break;
                    case C:
                        if(UIUtils.ctrl())
                            noise.setCellularDistanceFunction((noise.getCellularDistanceFunction() + (UIUtils.shift() ? 2 : 1)) % 3);
                        else
                            noise.setCellularReturnType((noise.getCellularReturnType() + (UIUtils.shift() ? 8: 1)) % 9);
                        break;
                    case E: //earlier seed
                        noise.setSeed((int) noise.getSeed() - 1);
                        break;
                    case S: //seed
                        noise.setSeed((int) noise.getSeed() + 1);
                        break;
                    case SLASH:
                        noise.setSeed((int) Hasher.randomize3(noise.getSeed()));
                        break;
                    case SEMICOLON: //seed
                        noise.setFractalSpiral(!noise.isFractalSpiral());
                        break;
                    case N: // noise type
                    case EQUALS:
                    case ENTER:
                        noise.setNoiseType((noise.getNoiseType() + (UIUtils.shift() ? 17 : 1)) % 18);
                        break;
                    case M:
                    case MINUS:
                        noise.setNoiseType((noise.getNoiseType() + 17) % 18);
                        break;
                    case U:
                        noise.setMutation(noise.getMutation() + (UIUtils.shift() ? noise.getFrequency() : -noise.getFrequency()));
                        break;
                    case D: //dimension
                        dim = (dim + (UIUtils.shift() ? 4 : 1)) % 5;
                        break;
                    case F: // frequency
//                        noise.setFrequency(NumberTools.sin(freq += 0.125f) * 0.25f + 0.25f + 0x1p-7f);
//                        noise.setFrequency((float) Math.exp((System.currentTimeMillis() >>> 9 & 7) - 5));
                        noise.setFrequency(freq *= (UIUtils.shift() ? 1.25f : 0.8f));
                        break;
                    case R: // fRactal type
                        noise.setFractalType((noise.getFractalType() + (UIUtils.shift() ? 3 : 1)) % 4);
                        break;
                    case G: // GLITCH!
                        noise.setPointHash(pointHashes[hashIndex = (hashIndex + (UIUtils.shift() ? pointHashes.length - 1 : 1)) % pointHashes.length]);
                        break;
                    case H: // higher octaves
                        noise.setFractalOctaves((octaves = octaves + 1 & 7) + 1);
                        break;
                    case L: // lower octaves
                        noise.setFractalOctaves((octaves = octaves + 7 & 7) + 1);
                        break;
                    case COMMA: // sharpness
                        noise.setSharpness((float)Math.pow(TrigTools.sinDeg((System.currentTimeMillis() & 0xFFFF) * 0x1p-4f) + 1.5f, 3f));
                        System.out.println(noise.getSharpness());
                        break;
                    case I: // inverse mode
                        if (inverse = !inverse) {
                            noise.setFractalLacunarity(0.5f);
                            noise.setFractalGain(2f);
                        } else {
                            noise.setFractalLacunarity(2f);
                            noise.setFractalGain(0.5f);
                        }
                        break;
                    case K: // sKip
                        ctr += 1000;
                        break;
                    case Q:
                    case ESCAPE: {
                        Gdx.app.exit();
                    }
                }
                return true;
            }
        };
        Gdx.input.setInputProcessor(input);
    }

    public void putMap() {
        renderer.begin(view.getCamera().combined, GL_POINTS);
        float bright, c = ctr * 0.25f;
        switch (dim) {
            case 0:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x + c, y + c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 1:
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bright = basicPrepare(noise.getConfiguredNoise(x, y, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 2:
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64 + c, xs = TrigTools.sinTurns(x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64 + c, ys = TrigTools.sinTurns(y * iHeight) * 64 + c;
                        bright = basicPrepare(noise.getConfiguredNoise(xc, yc, xs, ys));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
                break;
            case 3: {
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64, xs = TrigTools.sinTurns(x * iWidth) * 64;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64, ys = TrigTools.sinTurns(y * iHeight) * 64;
                        bright = basicPrepare(noise.getConfiguredNoise(xc, yc, xs, ys, c));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
            case 4: {
                for (int x = 0; x < width; x++) {
                    float xc = TrigTools.cosTurns(x * iWidth) * 64 + c, xs = TrigTools.sinTurns(x * iWidth) * 64 + c;
                    for (int y = 0; y < height; y++) {
                        float yc = TrigTools.cosTurns(y * iHeight) * 64 + c, ys = TrigTools.sinTurns(y * iHeight) * 64 + c,
                                zc = TrigTools.cosTurns((x - y) * 0.5f * iWidth) * 64 - c, zs = TrigTools.sinTurns((x - y) * 0.5f * iWidth) * 64 - c;
                        bright = basicPrepare(noise.getConfiguredNoise(xc, yc, zc, xs, ys, zs));
                        renderer.color(bright, bright, bright, 1f);
                        renderer.vertex(x, y, 0);
                    }
                }
            }
                break;
        }
        renderer.end();

    }

    @Override
    public void render() {
        // not sure if this is always needed...
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.graphics.setTitle(String.valueOf(Gdx.graphics.getFramesPerSecond()));
        if (keepGoing) {
            // standard clear the background routine for libGDX
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            ctr++;
            putMap();
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("SquidSquad Test: Noise Visualization");
        config.useVsync(false);
        config.setResizable(false);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new NoiseVisualizer(), config);
    }
}
