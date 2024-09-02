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
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TextraLabel;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class IntersectorTest extends ApplicationAdapter {
    private static final int width = 800, height = 800;

    Font font;
    ShapeDrawer sd;
    SpriteBatch batch;
    StretchViewport view;
    Array<TextraLabel> tags;
    Polygon trapezoid, square, overlap;

    private TextraLabel getTag(float x, float y){
        TextraLabel r = new TextraLabel("[BLACK]" + x + "," + y, font);
        r.setPosition(x, y);
        return r;
    }

    @Override
    public void create() {
        font = KnownFonts.getGentiumMSDF().scaleHeightTo(1f);
        view = new StretchViewport(40, 40);
        view.getCamera().position.set(5, 0, 0);
        batch = new SpriteBatch();
        sd = new ShapeDrawer(batch, font.mapping.get(font.solidBlock));
        tags = new Array<>(TextraLabel.class);
        trapezoid = new Polygon(new float[]{-2.8626027f, -3.054608f, 0.09345288f, 14.654785f, 18.01647f, 4.9480224f, 4.8018613f, -7.2055407f});
        square = new Polygon(new float[]{-8.879811f, -14.852028f, -8.879811f, 5.1479716f, 11.120189f, 5.1479716f, 11.120189f, -14.852028f});
        overlap = new Polygon();
// Initialize trapezoid.vertices and square.vertices with xy coordinates in CLOCKWISE order.
        Intersector.intersectPolygons(trapezoid, square, overlap);
        Vector2 pos = new Vector2();
        for(Polygon poly : new Polygon[]{trapezoid, square, overlap}) {
            for (int i = 0; i < poly.getVertexCount(); i++) {
                poly.getVertex(i, pos);
                tags.add(getTag(pos.x, pos.y));
            }
        }
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.WHITE);
        batch.setProjectionMatrix(view.getCamera().combined);
        view.apply(false);
        batch.begin();

        sd.setColor(Color.GRAY);
        sd.filledPolygon(square);
        sd.setColor(Color.CYAN);
        sd.filledPolygon(trapezoid);
        sd.setColor(Color.RED);
        sd.filledPolygon(overlap);
        for (int i = 0; i < tags.size; i++) {
            tags.get(i).draw(batch, 1f);
        }
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        view.update(width, height, false);
        font.resizeDistanceField(width, height, view);
    }

    public static void main(String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Intersector Test");
        config.useVsync(true);
        config.setResizable(false);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        config.setWindowedMode(width, height);
        config.disableAudio(true);
        new Lwjgl3Application(new IntersectorTest(), config);
    }
}
