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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.UIUtils;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.digital.Interpolations;
import com.github.tommyettinger.digital.Interpolations.Interpolator;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.Layout;
import com.github.yellowstonegames.core.DescriptiveColor;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class InterpolationsGraphing extends ApplicationAdapter {
    static final boolean WRITE = true;

    private static final int width = 300, height = 420;

    Interpolator current;
    Font font;
    ShapeDrawer sd;
    SpriteBatch batch;
    StretchViewport view;
    Layout name;
    Interpolator[] interpolators;
    int index;
    double[] k = new double[]{1.0, 1.0};
//    /**
//     * Moves like a sine wave does; starts slowly, rises quickly, then ends slowly.
//     */
//    public static final Interpolator sine = new Interpolator("sine_", a -> (a = SIN_TABLE[(int) (a * 4096f) & TABLE_MASK]) * a);
//    /**
//     * Moves like a sine wave does; starts slowly and rises quickly.
//     */
//    public static final Interpolator sineIn = new Interpolator("sineIn_", a -> (1f - COS_TABLE[(int) (a * 4096f) & TABLE_MASK]));
//    /**
//     * Moves like a sine wave does; starts quickly and slows down.
//     */
//    public static final Interpolator sineOut = new Interpolator("sineOut_", a -> SIN_TABLE[(int) (a * 4096f) & TABLE_MASK]);

    @Override
    public void create() {
        font = KnownFonts.getCozette();
        view = new StretchViewport(width, height);
        batch = new SpriteBatch();
        sd = new ShapeDrawer(batch, font.mapping.get(font.solidBlock));

//        Interpolator adjusted = new Interpolations.Interpolator("adjustedHue", a -> MathTools.barronSpline(a, 1.7f, 0.9f));
//        Interpolator kAdaptive = new Interpolations.Interpolator("kAdaptive", x -> (float) Math.pow(1.0 - Math.pow(1.0 - x, 1.0/k[1]), 1.0/k[0]));
//        Interpolator reverseLight = new Interpolator("reverseLight", DescriptiveColor::reverseLight);
//        Interpolator forwardLight = new Interpolator("forwardLight", DescriptiveColor::forwardLight);
//        Interpolator reverseLightO = new Interpolator("reverseLightO", InterpolationsGraphing::reverseLightOttosson);
//        Interpolator forwardLightO = new Interpolator("forwardLightO", InterpolationsGraphing::forwardLightOttosson);
//        Interpolator forwardReverseLight = new Interpolator("forwardReverseLight", f -> DescriptiveColor.reverseLight(DescriptiveColor.forwardLight(f)));
//        Interpolator forwardReverseLightO = new Interpolator("forwardReverseLightO", f -> reverseLightOttosson(forwardLightOttosson(f)));
//        Interpolator pow0_625In = new Interpolator("pow0_625In", Interpolations.powInFunction(0.625f));
//        Interpolator pow1_6In = new Interpolator("pow1_6In", Interpolations.powInFunction(1.6f));
//        Interpolator pow0_6In = new Interpolator("pow0_6In", Interpolations.powInFunction(0.6f));
//        Interpolator pow1_666In = new Interpolator("pow1_666In", Interpolations.powInFunction(1.666f));
//        Interpolator pow0_666In = new Interpolator("pow0_666In", Interpolations.powInFunction(0.666f));
//        Interpolator pow1_5In = new Interpolator("pow1_5In", Interpolations.powInFunction(1.5f));
        interpolators = Interpolations.getInterpolatorArray();
//        for (int i = 0; i < interpolators.length; i++) {
//            System.out.println(i + " " + interpolators[i].tag);
//        }

//        interpolators[54] = sine;
//        interpolators[55] = sineIn;
//        interpolators[56] = sineOut;

        index = interpolators.length - 1;
        current = interpolators[index];
        name = font.markup("[BLACK]"+ current.tag, name = new Layout(font));
        if(WRITE){
            System.out.println("<table>\n<tr><th>Graph A</th><th>Name A</th><th>Graph B</th><th>Name B</th><th>Graph C</th><th>Name C</th></tr>");
            int i = 0;
            for (; i < interpolators.length; i++) {
                if((i % 3) == 0) System.out.println("<tr>");
                current = interpolators[index = i];
                name = font.markup("[BLACK]"+ current.tag, name = new Layout(font));
                render();
                Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, width, height);
                PixmapIO.writePNG(Gdx.files.local("out/interpolators/" + current.tag + ".png"), pixmap, 9, true);
                pixmap.dispose();
                System.out.println("<td><img src=\"interpolators/"+current.tag+".png\" alt=\""+current.tag+"\" /></td><td>"+current.tag+"</td>");
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            current = interpolators[index = (index + 1) % interpolators.length];
            name = font.markup("[BLACK]"+ current.tag, name.clear());
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            current = interpolators[index = (index + interpolators.length - 1) % interpolators.length];
            name = font.markup("[BLACK]"+ current.tag, name.clear());
        }

        if(Gdx.input.isKeyPressed(Input.Keys.A)){
            k[0] = Math.max(0.001, k[0] + Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? -1 : 1));
            name = font.markup(String.format("[BLACK]a=%.8f, b=%.8f", k[0], k[1]), name.clear());
        }
        if(Gdx.input.isKeyPressed(Input.Keys.B)){
            k[1] = Math.max(0.001, k[1] + Gdx.graphics.getDeltaTime() * (UIUtils.shift() ? -1 : 1));
            name = font.markup(String.format("[BLACK]a=%.8f, b=%.8f", k[0], k[1]), name.clear());
        }
        //print
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)){
            System.out.printf("a = %.8f , b = %.8f \n", k[0], k[1]);
        }
        ScreenUtils.clear(Color.WHITE);
//        batch.setProjectionMatrix(view.getCamera().combined);
        batch.begin();

        // horizontal graph lines
        for (int i = 20; i < height; i+=40) {
            sd.line(0, i, width, i, Color.CYAN, 1f);
        }
        // vertical graph lines
        for (int i = 20; i < width; i+=40) {
            sd.line(i, 0, i, height, Color.CYAN, 1f);
        }
        // x-axis
        sd.line(0, 100, width, 100, Color.NAVY, 1f);
        // y-axis
        sd.line(60, 0, 60, height, Color.NAVY, 1f);
        // line where y == 1
        sd.line(0, 300, width, 300, Color.LIGHT_GRAY, 1f);
        // line where x == 1
        sd.line(260, 0, 260, height, Color.LIGHT_GRAY, 1f);

        float h0, h1 = 101;
        for (int i = 0; i <= 200; i++) {
            float f = i / 200f;
            h0 = h1;
            h1 = current.apply(101, 301, f);
            float gradient = DescriptiveColor.oklabIntToFloat(DescriptiveColor.oklabByHSL(0.75f + 0.25f * f, 1f, 0.5f, 1f));
            sd.setColor(gradient);
            sd.line(59 + i, h0, 60 + i, h1, 3f);
        }

        /* These are for if you use ShapeRenderer, and not ShapeDrawer. */
//        renderer.begin(view.getCamera().combined, GL_LINES);
//
//        // horizontal graph lines
//        for (int i = 20; i < height; i+=40) {
//            renderer.color(Color.CYAN);
//            renderer.vertex(0, i, 0);
//            renderer.color(Color.CYAN);
//            renderer.vertex(width, i, 0);
//        }
//        // vertical graph lines
//        for (int i = 20; i < width; i+=40) {
//            renderer.color(Color.CYAN);
//            renderer.vertex(i, 0, 0);
//            renderer.color(Color.CYAN);
//            renderer.vertex(i, height, 0);
//        }
//        // x-axis
//        renderer.color(Color.NAVY);
//        renderer.vertex(0, 100, 0);
//        renderer.color(Color.NAVY);
//        renderer.vertex(width, 100, 0);
//        // y-axis
//        renderer.color(Color.NAVY);
//        renderer.vertex(60, 0, 0);
//        renderer.color(Color.NAVY);
//        renderer.vertex(60, height, 0);
//        renderer.end();
//        // line where y == 1
//        renderer.color(Color.LIGHT_GRAY);
//        renderer.vertex(0, 300, 0);
//        renderer.color(Color.LIGHT_GRAY);
//        renderer.vertex(width, 300, 0);
//        // line where x == 1
//        renderer.color(Color.LIGHT_GRAY);
//        renderer.vertex(260, 0, 0);
//        renderer.color(Color.LIGHT_GRAY);
//        renderer.vertex(260, height, 0);
//
//        float h0, h1 = 101;
//        for (int i = 0; i <= 200; i++) {
//            float f = i / 200f;
//            h0 = h1;
//            h1 = current.apply(101, 301, f);
//            float gradient = DescriptiveColor.oklabIntToFloat(DescriptiveColor.oklabByHSL(0.75f + 0.25f * f, 1f, 0.5f, 1f));
//            renderer.color(gradient);
//            renderer.vertex(59 + i, h0, 0);
//            renderer.color(gradient);
//            renderer.vertex(60 + i, h1, 0);
//        }
//        renderer.end();

        float level = -0.4f;
        for (int i = 15; i <= 400; i+=40) {
            font.drawMarkupText(batch, String.format("[darker gray]%4.1f", level), 32, i);
            level += 0.2f;
        }
        level = 0f;
        for (int i = 46; i <= 260; i+=40) {
            font.drawMarkupText(batch, String.format("[darker gray]%4.1f", level), i, 82);
            level += 0.2f;
        }
        font.drawGlyphs(batch, name, width * 0.5f, height - 16f, Align.center);
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
        config.setTitle("Graphing Interpolations");
        config.useVsync(true);
        config.setResizable(false);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new InterpolationsGraphing(), config);
    }
}
