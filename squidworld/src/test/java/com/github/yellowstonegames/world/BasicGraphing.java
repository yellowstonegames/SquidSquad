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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.digital.BitConversion;
import com.github.tommyettinger.digital.MathTools;
import com.github.tommyettinger.digital.RoughMath;
import com.github.tommyettinger.ds.ObjectObjectOrderedMap;
import com.github.tommyettinger.function.FloatToFloatFunction;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.Layout;
import com.github.yellowstonegames.core.DescriptiveColor;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BasicGraphing extends ApplicationAdapter {
    static final boolean WRITE = false;

    private static final int width = 800, height = 420;
    private static final float SCALE_X = 4, SCALE_Y = 1f;

    Font font;
    ShapeDrawer sd;
    SpriteBatch batch;
    StretchViewport view;
    Layout name;
    ObjectObjectOrderedMap<String, FloatToFloatFunction> functions;
    int[] hsl;

    public static float logisticRough (float x)
    {
        // 1f / (1f + exp(-x))
        final float clip = Math.max(-126.0f, x * -1.442695040f);
        final float z = clip - (int)(clip + 126.0f) + 126.0f;
        return 1.0f / (1.0f + BitConversion.intBitsToFloat((int) (0x800000 * (clip + 121.2740575f + 27.7280233f / (4.84252568f - z) - 1.49012907f * z))));
    }
    public static float logisticRougher (float x)
    {
        // 1f / (1f + exp(-x))
        return 1.0f / (1.0f + BitConversion.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -1.442695040f * x) + 126.94269504f))));
    }

    public static float iDunno (float x)
    {
        int i = BitConversion.floatToIntBits(x * 0x1p-120f);
        return (BitConversion.intBitsToFloat((i & 0x80000000) | 0x3F800000 | (i & 0x7FFFFFFF) >>> 8) - (i >> 31 | 1));
    }

    @Override
    public void create() {
        font = KnownFonts.getCozette();
        view = new StretchViewport(width, height);
        batch = new SpriteBatch();
        sd = new ShapeDrawer(batch, font.mapping.get(font.solidBlock));

        functions = new ObjectObjectOrderedMap<>(new String[] {
                "logistic (Math.exp)",
                "logisticRough",
                "logisticRougher",
//                "logistic (MathTools.exp)",
//                "logistic (MathTools.expHasty)",
//                "scalb thing",
                "iDunno",
        }, new FloatToFloatFunction[]{
                (float x) -> 1f / (1f + (float) Math.exp(-x)),
                BasicGraphing::logisticRough,
                BasicGraphing::logisticRougher,
//                (float x) -> 1f / (1f + MathTools.exp(-x)),
//                (float x) -> 1f / (1f + MathTools.expHasty(-x)),
//                (float x) -> 1.0f / (1.0f + BitConversion.intBitsToFloat( (int)(Math.scalb(Math.max(-126.0f, -1.442695040f * x) + 126.94269504f, 23)))),
                BasicGraphing::iDunno
        });
        hsl = new int[functions.size()];
        name = new Layout(font);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hsl.length; i++) {
            hsl[i] = DescriptiveColor.oklabByHSL(MathTools.fract(MathTools.GOLDEN_RATIO_INVERSE * i), 1f, 0.5f, 1f);
            sb.append("[#");
            Base.BASE16.appendUnsigned(sb, DescriptiveColor.toRGBA8888(hsl[i]));
            sb.append("]").append(functions.keyAt(i)).append("\n");
        }
        name = font.markup(sb.toString(), name);

        if(WRITE){
            System.out.println("<table>\n<tr><th>Graph A</th><th>Name A</th><th>Graph B</th><th>Name B</th><th>Graph C</th><th>Name C</th></tr>");
            int i = 0;
            for (; i < functions.size(); i++) {
                if((i % 3) == 0) System.out.println("<tr>");
                String tag = functions.keyAt(i);
                render();
                Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
                PixmapIO.writePNG(Gdx.files.local("out/functions/" + tag + ".png"), pixmap, 9, true);
                pixmap.dispose();
                System.out.println("<td><img src=\"functions/"+tag+".png\" alt=\""+tag+"\" /></td><td>"+tag+"</td>");
                if((i % 3) == 2) System.out.println("</tr>");
            }
            if((i % 3) == 0)
                System.out.println("</table>");
            else
                System.out.println("</tr>\n</table>");
            Gdx.app.exit();
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.WHITE);
        batch.begin();

        // horizontal graph lines
        for (int i = 0; i < height; i+=40) {
            sd.line(0, i, width, i, Color.CYAN, 1f);
        }
        // vertical graph lines
        for (int i = 0; i < width; i+=40) {
            sd.line(i, 0, i, height, Color.CYAN, 1f);
        }
        // x-axis
        sd.line(0, 80, width, 80, Color.NAVY, 1f);
        // y-axis
        sd.line(60 + 340, 0, 60 + 340, height, Color.NAVY, 1f);
        // line where y == 1
        sd.line(0, 280, width, 280, Color.LIGHT_GRAY, 1f);
        // line where x == 1
        sd.line(260 + 340, 0, 260 + 340, height, Color.LIGHT_GRAY, 1f);

        for (int f = 0; f < functions.size(); f++) {
            float gradient = DescriptiveColor.oklabIntToFloat(hsl[f]);
            float h0, h1 = functions.getAt(f).applyAsFloat(-2f * SCALE_X) * 200 * SCALE_Y + 81;
            for (int i = -400; i <= 400; i++) {
//            for (int i = -400 + f; i <= 400; i+= functions.size()) {
                float pt = i * SCALE_X / 200f;
                h0 = h1;
                if(i * i == 1){
                    System.out.println("applying function " + functions.keyAt(f));
                    float result = functions.getAt(f).applyAsFloat(Float.MAX_VALUE);
                    System.out.printf("Calling on %1.10f (0x%08X), the result is %1.20f (0x%08X)\n", Float.MAX_VALUE, BitConversion.floatToIntBits(Float.MAX_VALUE),
                            result, BitConversion.floatToIntBits(result));
                }
                h1 = functions.getAt(f).applyAsFloat(pt) * 200 * SCALE_Y + 81;
                sd.setColor(gradient);
                sd.line(399 + i, h0, 400 + i, h1, 1f);
            }
        }

        float level = -0.4f;
        for (int i = 5; i <= 400; i+=40) {
            font.drawMarkupText(batch, String.format("[darker gray]%4.1f", level), 372, i);
            level += 0.2f;
        }
        level = 0f;
        for (int i = 46 + 340; i <= 260 + 340; i+=40) {
            font.drawMarkupText(batch, String.format("[darker gray]%4.1f", level), i, 72);
            level += 0.2f;
        }
        font.drawGlyphs(batch, name, 50, height - 16f, Align.left);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, true);
        view.apply(true);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Graphing");
        config.useVsync(true);
        config.setResizable(false);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new BasicGraphing(), config);
    }
}
