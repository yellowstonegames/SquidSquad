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

package com.github.yellowstonegames.glyph;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Layout;
import com.github.yellowstonegames.core.DescriptiveColor;
import com.github.tommyettinger.textra.KnownFonts;

public class FontTest extends ApplicationAdapter {

    Font font;
    Font[] fonts;
    SpriteBatch batch;
//    LaserRandom random;
    int[][] backgrounds;
    char[][] lines;
    Layout layout;
    public static void main(String[] args){
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Font test");
        config.setWindowedMode(800, 640);
        config.disableAudio(true);
        config.useVsync(true);
        new Lwjgl3Application(new FontTest(), config);
    }

    @Override
    public void create() {
//        random = new LaserRandom(1L);
//        long seed = random.nextLong();
//        char[][] lines0 = LineTools.decode4x4(seed | LineTools.exteriorSquare, LineTools.light);

        lines = new char[][]
                {
                "┼┤┤├".toCharArray(),
                "┴││┴".toCharArray(),
                "─└├┼".toCharArray(),
                "┤├│┐".toCharArray()};

//        for (int x = 0; x < 4; x++) {
//            for (int y = 0; y < 4; y++) {
//                lines[y][3 - x] = lines0[x][y];
//            }
//        }
//        DungeonTools.debugPrint(lines0);
//        DungeonTools.debugPrint(LineTools.decode4x4(seed | LineTools.exteriorSquare, LineTools.light));
//        lines = LineTools.decode4x4(random.nextLong() & LineTools.interiorSquare, LineTools.lightAlt);
        batch = new SpriteBatch();
//        font = KnownFonts.getInconsolataMSDF().scaleTo(16, 32);
//        font = KnownFonts.getIosevkaSlabMSDF().scaleTo(16, 32);
//        font = KnownFonts.getIosevka().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlab().scale(0.75f, 0.75f);
//        font = KnownFonts.getIosevkaSlabMSDF().scaleTo(20, 20);
//        font = KnownFonts.getDejaVuSansMono().scale(0.75f, 0.75f);
//        font = KnownFonts.getCozette();
//        font = KnownFonts.getOpenSans().scale(0.75f, 0.75f);
//        font = KnownFonts.getAStarry();
//        font = KnownFonts.getGentium().scaleTo(48, 48);
//        font = KnownFonts.getLibertinusSerif();
//        font = KnownFonts.getKingthingsFoundation().scaleTo(45, 60);
//        font = KnownFonts.getOxanium().scaleTo(40, 50);
//        font = KnownFonts.getYanoneKaffeesatz().scaleTo(45, 60);
//        font = KnownFonts.getCanada().scaleTo(40, 58);
//        font = KnownFonts.getRobotoCondensed().scaleTo(37, 53);
        fonts = KnownFonts.getAll();
        for(Font f : fonts)
            f.scaleTo(32f * f.originalCellWidth / f.originalCellHeight, 32).fitCell(16, 32, true);
        font = fonts[0];

//        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("dawnlike/Dawnlike.atlas"), Gdx.files.internal("dawnlike"));
//        font = new Font("dawnlike/PlainAndSimplePlus.fnt", atlas.findRegion("PlainAndSimplePlus"), false, 0, 0, 2, 2);
        layout = new Layout(font).setTargetWidth(Gdx.graphics.getWidth());
        font.resizeDistanceField(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgrounds = new int[(int) Math.ceil(800 / font.cellWidth)][(int) Math.ceil(640 / font.cellHeight)];
        int sw = DescriptiveColor.describe("darker sage"), se = DescriptiveColor.describe("dark rich cactus"),
                nw = DescriptiveColor.describe("dull peach butter"), ne = DescriptiveColor.describe("dark brown^3 purple");
        backgrounds[0][0] = sw;
        backgrounds[0][backgrounds[0].length - 1] = nw;
        backgrounds[backgrounds.length - 1][0] = se;
        backgrounds[backgrounds.length - 1][backgrounds[0].length - 1] = ne;
        for (int x = 1; x < backgrounds.length - 1; x++) {
            backgrounds[x][0] = DescriptiveColor.lerpColors(sw, se, x / (float) backgrounds.length);
            backgrounds[x][backgrounds[0].length - 1] = DescriptiveColor.lerpColors(nw, ne, x / (backgrounds.length - 1F));
        }
        for (int x = 0; x < backgrounds.length; x++) {
            int s = backgrounds[x][0], e = backgrounds[x][backgrounds[0].length - 1];
            for (int y = 1; y < backgrounds[0].length - 1; y++) {
                backgrounds[x][y] = DescriptiveColor.lerpColors(s, e, y / (backgrounds[0].length - 1f));
            }
        }

//        font.markup("[#00FF00FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", glyphs[0] = new LongList());
//        font.markup("The [dark richer red]MAW[ ] of the [/][|lighter blue mint]wendigo[ ] [*]appears[*]!", glyphs[1] = new LongList());
//        font.markup("The [_][dark dull blue purple]BLADE[ ] of [*][/][|dark richest yellow]KINGS[ ] strikes!", glyphs[2] = new LongList());
//        font.markup("[;]Each cap[ ], [,]All lower[ ], [!]Caps lock[ ], [?]Unknown[ ]?", glyphs[3] = new LongList());

//        font.markup("[#007711FF]Hello, [~]World[~]Universe[.]$[=]$[^]$[^]!", layout);
//
//        font.markup("\n[*]Водяной[ ] — в славянской мифологии дух, обитающий в воде, хозяин вод[^][BLUE][[2][ ]."
//                + "\nВоплощение стихии воды как отрицательного и опасного начала[^][BLUE][[3][[citation needed][ ].", layout);
//
        font.markup("\nThe [dark rich red]MAW[ ] of the [/][lighter dull sky]wendigo[/] (wendigo)[ ] [*]appears[*]!"
                        + "\nThe [_][dark gray]BLADE[ ] of [*][/][rich light yellow]DYNAST-KINGS[ ] strikes!"
                        + "\n[_][;]Each cap, [,]All lower, [!]Caps lock[ ], [?]Unknown[ ]?"
                        + "\n[dark dull bronze yellow]φ[ ] = (1 + 5[^]0.5[^]) * 0.5"
                        + "\n[dull light orange]¿Qué son estos? ¡Arribate, mijo![ ]"
                        + "\nPchnąć[ ] w tę łódź [dark tan]jeża[ ] lub ośm skrzyń [rich purple]fig[ ]."
                , layout);

//        font.markup("\"You are ever more the [/]fool[/] than the pitiable cutpurse who [*]dares waylay[*] my castle road!\" the [dark rich gold]King[ ] admonished."
//                +" \"Forsooth! Had [_]I[_] my right mind, I would have [dark red]both of [_]your heads[ ] by morning. But alas, I am stricken with" +
//                " unreasonable mercy for your [~]wretched[~] souls. To [darker grey][*]the Trappists[ ] ye shall go; I am in need of" +
//                " a [darkest bronze]stout brew[ ].\"", layout);

//        font.markup("\"[/][~]HOSTILE ACTION DETECTED[ ].\" The computerized voice was barely audible over the klaxons blaring throughout [darker rich purple][_]Starship Andromalius[ ]."
//                +" \"[!]Would somebody shut that thing off[!]? We're quite aware by now!\" [orange]Captain Luiz Tigre[ ] shouted at no one in particular, while frantically flipping the remaining" +
//                " switches on the capacitor controls. \"Sir, we need to get the [silver]teleprojector[ ] online. Send a party aboard, say they're negotiators.\" [light sky]First Admiral Zototh[ ] said with urgency." +
//                " \"[*]Negotiators[*]? Are you serious?\" \"I said to [/]say[/] they're negotiators... just with really big guns.\"", layout);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.5f, 0.9f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        font = fonts[(int) (System.currentTimeMillis() >>> 10 & 0x7FFFFFFF) % fonts.length];
        font.resizeDistanceField(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
//        layout.setFont(font);
//        font.calculateSize(layout);
        float x = 0, y = layout.getHeight();
        batch.begin();
        font.enableShader(batch);
        batch.setPackedColor(Color.WHITE_FLOAT_BITS);
        font.drawBlocks(batch, backgrounds, 0f, 0f);
        for (int xx = 0; xx < 4; xx++) {
            for (int yy = 0; yy < 4; yy++) {
                font.drawGlyph(batch, 0xFFFFFFFE00000000L | lines[xx][yy], font.cellWidth * xx, y + font.cellHeight * (1 + yy));
            }
        }
//        long color = (long) DescriptiveColor.lerpColors(
//                DescriptiveColor.lerpColors(
//                        (int)((System.currentTimeMillis() >>> 10) * 0x9E3779B0 | 0xFE),
//                        (int)(((System.currentTimeMillis() >>> 10) + 1L) * 0x9E3779B0 | 0xFE),
//                        (System.currentTimeMillis() & 0x3FFL) * 0x1p-10f
//                ), 0x000000FF, 0.375f) << 32;
//        for (int i = 0, n = glyphs[0].size(); i < n; i++) {
//            glyphs[0].set(i, glyphs[0].get(i) & 0xFFFFFFFFL | color);
//        }
        font.drawGlyphs(batch, layout, x, y, Align.left);
        batch.end();
        Gdx.graphics.setTitle(Gdx.graphics.getFramesPerSecond() + " FPS");
    }
}
